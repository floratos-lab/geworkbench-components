package org.geworkbench.components.genomespace;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.genomespace.client.ConfigurationUrls;
import org.genomespace.client.DataManagerClient;
import org.genomespace.client.GsSession;
import org.genomespace.client.ui.GSLoginDialog;
import org.genomespace.datamanager.core.GSDataFormat;
import org.genomespace.datamanager.core.GSDirectoryListing;
import org.genomespace.datamanager.core.GSFileMetadata;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.parsers.DataSetFileFormat;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;
import org.geworkbench.util.ProgressTask;

/**
 * Integration of GenomeSpace from the Broad Institute
 * $Id$
 */
public class GenomeSpace implements VisualPlugin {

	private static Log log = LogFactory.getLog(GenomeSpace.class);
	private JButton loginbutton = new JButton("Login");
	private static ProjectTreeNode genomeRoot = new ProjectTreeNode("GenomeSpace");
	private static DefaultTreeModel genomeTreeModel = new DefaultTreeModel(genomeRoot);
	private static JTree genomeTree = new JTree(genomeTreeModel);
	private static ProjectPanel projpane = ProjectPanel.getInstance();
	private DefaultTreeModel projectTreeModel = projpane.getTreeModel();
	private JTree projectTree    = new JTree(projectTreeModel);
	private JPopupMenu uploadPopupMenu = new JPopupMenu();
	private JPopupMenu gsfilePopupMenu = new JPopupMenu();
	private JPopupMenu gsdirPopupMenu = new JPopupMenu();
	private JPopupMenu gsrootPopupMenu = new JPopupMenu();
	private JMenuItem mkdirItem = new JMenuItem("Create subdirectory");
	private JMenuItem deldirItem = new JMenuItem("Delete");
	private JMenuItem downloadItem = new JMenuItem("Download to geWorkbench");
	private JMenu convertItem = new JMenu("Convert to");
	private JMenuItem renameItem = new JMenuItem("Rename");
	private JMenuItem deleteItem = new JMenuItem("Delete");
	private JSplitPane mainpanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private static final String expFormatName = "geWorkbench exp";
	private static final String genomespaceUsrDir = FilePathnameUtils.getUserSettingDirectoryPath()
														+ "genomespace" + FilePathnameUtils.FILE_SEPARATOR;
	private static final String uploadDir   = genomespaceUsrDir + "upload" + FilePathnameUtils.FILE_SEPARATOR;
	private static final String downloadDir = genomespaceUsrDir + "download" + FilePathnameUtils.FILE_SEPARATOR;
	private static GSLoginDialog loginDialog = null;
	private static ProgressDialog pd = ProgressDialog.getInstance(false);

	public GenomeSpace() {
		JScrollPane jsp1 = new JScrollPane();
		jsp1.setBorder(BorderFactory.createLoweredBevelBorder());
		jsp1.getViewport().add(genomeTree, null);
		genomeTree.setCellRenderer(new DirRenderer());

		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
		JLabel label = new JLabel("GenomeSpace Directories");
		header.add(label);
		header.add(Box.createRigidArea(new Dimension(10, 0)));
		header.add(loginbutton);
		loginbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (loginbutton.getText().equals("Login"))
					initLoginDialog();
				else{
					loginDialog.getGsSession().logout();
					loginbutton.setText("Login");
					genomeRoot.removeAllChildren();
					genomeTreeModel.reload(genomeRoot);
				}
			}
		});

		JPanel panel1 = new JPanel(new BorderLayout());
		panel1.add(header, BorderLayout.NORTH);
		panel1.add(jsp1, BorderLayout.CENTER);
		mainpanel.setTopComponent(panel1);

		JScrollPane jsp2 = new JScrollPane();
		jsp2.setBorder(BorderFactory.createLoweredBevelBorder());
		jsp2.getViewport().add(projectTree, null);

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(new JLabel("geWorkbench Workspace Mirror"), BorderLayout.NORTH);
		panel2.add(jsp2, BorderLayout.CENTER);
		mainpanel.setBottomComponent(panel2);
		mainpanel.setDividerLocation(300);

		File d = new File(uploadDir);
		if (!d.exists()) d.mkdir();
		d = new File(downloadDir);
		if (!d.exists()) d.mkdir();

		JMenuItem uploadItem = new JMenuItem("Upload to GenomeSpace");
		uploadItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!validate(projectTree)) return;
				UploadTask uploadTask = new UploadTask(ProgressItem.INDETERMINATE_TYPE, "Uploading...");
		    	pd.executeTask(uploadTask);
			}
		});

		uploadPopupMenu.add(uploadItem);
		projectTree.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				TreePath path = projectTree.getPathForLocation(e.getX(), e.getY());
				if (path == null) return;

				projectTree.setSelectionPath(path);
				ProjectTreeNode mNode = (ProjectTreeNode) path.getLastPathComponent();
				if (e.getButton() == MouseEvent.BUTTON3 || e.getClickCount() >= 2) {
					// exp data node only
					if (mNode instanceof DataSetNode && 
							((DataSetNode)mNode).getDataset() instanceof DSMicroarraySet ) {
						uploadPopupMenu.show(projectTree, e.getX(), e.getY());
					}
				}
			}
		});

		downloadItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!validate(genomeTree)) return;
				// we used to check to make sure a project node is selected before continuing. not necessary anymore.

				DownloadTask downloadTask = new DownloadTask(ProgressItem.INDETERMINATE_TYPE, "Downloading...");
				pd.executeTask(downloadTask);
			}
		});

		convertItem.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent e) {
				convertItem.removeAll();
			}
			public void menuDeselected(MenuEvent e) {
				convertItem.removeAll();
			}
			public void menuSelected(MenuEvent e) {
				
				if (!validate(genomeTree)) return;
				
				ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();
				if (node.isLeaf()) {
					DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
					GSFileMetadata meta = dmClient.getMetadata(node.getDescription());
					
					//FIXME: check for null meta
					GSDataFormat currentFormat = meta.getDataFormat();
					for(GSDataFormat format : meta.getAvailableDataFormats()){
						if (!format.equals(currentFormat)){
							JMenuItem item = new JMenuItem(format.getName());
							convertItem.add(item);
							item.addActionListener(new ConvertActionListener(format));
						}
					}
					if (convertItem.getItemCount()==0){
						JMenuItem none = new JMenuItem("none"); 
						none.setEnabled(false);
						convertItem.add(none);
					}
				}
			}
		});
		
		deleteItem.addActionListener(new DelActionListener());

		renameItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!validate(genomeTree)) return;
				ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();
				ProjectTreeNode parent = (ProjectTreeNode)node.getParent();
				DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
				GSFileMetadata source = dmClient.getMetadata(node.getDescription());
				GSFileMetadata parentdir = dmClient.getMetadata(parent.getDescription());

				String newname = (String)JOptionPane.showInputDialog(null,
						"Enter new name:\n", "Rename", JOptionPane.PLAIN_MESSAGE,
                        null, null, node.getUserObject());
				if (newname == null || newname.trim().isEmpty() || source==null || parentdir==null) return;
				GSFileMetadata newGsFile = dmClient.copy(source, parentdir, newname);
				if (newGsFile != null){
					dmClient.delete(source);
					node.setUserObject(newname);
					node.setDescription(newGsFile.getPath());
					genomeTreeModel.reload(node);
				}
			}
		});

		JMenuItem copyurlItem = new JMenuItem("Copy URL to clipboard");
		copyurlItem.addActionListener(new CopyurlActionListener());

		gsfilePopupMenu.add(downloadItem);
		gsfilePopupMenu.add(convertItem);
		gsfilePopupMenu.add(renameItem);
		gsfilePopupMenu.add(deleteItem);
		gsfilePopupMenu.add(copyurlItem);

		mkdirItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (!validate(genomeTree)) return;
				ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();
				DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
				GSFileMetadata meta = dmClient.getMetadata(node.getDescription());

				String newname = (String)JOptionPane.showInputDialog(null, "Enter directory name:\n",
						"Create subdirectory", JOptionPane.PLAIN_MESSAGE);//null, null, null);
				if (newname == null || newname.trim().isEmpty() || meta == null) return;
				GSFileMetadata newmeta = dmClient.createDirectory(meta, newname);
				if (newmeta!=null){
					ProjectTreeNode newnode = metaToNode(newmeta);
					genomeTreeModel.insertNodeInto(newnode, node, node.getChildCount());
					showNode(genomeTree, newnode);
				}
			}
		});
		
		deldirItem.addActionListener(new DelActionListener());
		
		JMenuItem copyurldirItem = new JMenuItem("Copy URL to clipboard");
		copyurldirItem.addActionListener(new CopyurlActionListener());
		
		JMenuItem refreshItem = new JMenuItem("Refresh GS");
		refreshItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (!validate(genomeTree)) return;
				ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();
				RefreshTask refreshTask = new RefreshTask(ProgressItem.INDETERMINATE_TYPE, "Retrieving GenomeSpace content", node);
		    	pd.executeTask(refreshTask);
			}
		});
		
		gsdirPopupMenu.add(refreshItem);
		gsdirPopupMenu.add(mkdirItem);
		gsdirPopupMenu.add(deldirItem);
		gsdirPopupMenu.add(copyurldirItem);
		
		JMenuItem refreshRootItem = new JMenuItem("Refresh GS");
		refreshRootItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if (!validate()) return;
				RefreshTask refreshTask = new RefreshTask(ProgressItem.INDETERMINATE_TYPE, "Retrieving GenomeSpace content");
		    	pd.executeTask(refreshTask);
			}
		});
		gsrootPopupMenu.add(refreshRootItem);

		genomeTree.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				TreePath path = genomeTree.getPathForLocation(e.getX(), e.getY());
				if (path == null) return;

				genomeTree.setSelectionPath(path);
				ProjectTreeNode mNode = (ProjectTreeNode) path.getLastPathComponent();
				if (e.getButton() == MouseEvent.BUTTON3 || e.getClickCount() >= 2) {
					if (mNode.isRoot()){
						gsrootPopupMenu.show(genomeTree, e.getX(), e.getY());
					} else if (mNode.isLeaf() && !mNode.getAllowsChildren()) {
						gsfilePopupMenu.show(genomeTree, e.getX(), e.getY());
						DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
						GSFileMetadata meta = dmClient.getMetadata(mNode.getDescription());
						boolean downloadable = false;
						for(GSDataFormat format : meta.getAvailableDataFormats()){
							if (format.getName().equals(expFormatName))
								downloadable = true;
						}
						if (!downloadable)
							downloadItem.setEnabled(false);
						else downloadItem.setEnabled(true);

						// public & shared files are read-only
						if(!meta.getOwner().getName().equals(loginDialog.getGsUser().getUsername())){
							convertItem.setEnabled(false);
							renameItem.setEnabled(false);
							deleteItem.setEnabled(false);
						}else{
							if (meta.getAvailableDataFormats().size()<2)
								convertItem.setEnabled(false);
							else convertItem.setEnabled(true);
							renameItem.setEnabled(true);
							deleteItem.setEnabled(true);
						}
					} else if (mNode.getAllowsChildren()){
						gsdirPopupMenu.show(genomeTree, e.getX(), e.getY());
						// public & shared folders are read-only
						DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
						GSFileMetadata meta = dmClient.getMetadata(mNode.getDescription());
						if(!meta.getOwner().getName().equals(loginDialog.getGsUser().getUsername())){
							mkdirItem.setEnabled(false);
							deldirItem.setEnabled(false);
						}else{
							mkdirItem.setEnabled(true);
							// cannot delete non-empty dir
							if (mNode.getChildCount() > 0)
								deldirItem.setEnabled(false);
							else deldirItem.setEnabled(true);
						}
					}
				}
			}
		});
	}

	private class DelActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if (!validate(genomeTree)) return;
			ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();

			//FIXME: check owner
			int choice = JOptionPane.showConfirmDialog(null, 
					"Are you sure to delete "+node.getUserObject()+"?", 
					"Confirm File Deletion in GenomeSpace", 
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (choice == JOptionPane.NO_OPTION || choice == JOptionPane.CLOSED_OPTION) return;
			
			DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();

			dmClient.delete(node.getDescription());
			ProjectTreeNode parent = (ProjectTreeNode)node.getParent();
			if (parent != null){
				genomeTreeModel.removeNodeFromParent(node);
				showNode(genomeTree, parent);
			}
		}
	}
	
	private class CopyurlActionListener implements ActionListener, ClipboardOwner{
		public void actionPerformed(ActionEvent e){
			if (!validate(genomeTree)) return;
			ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();
			DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
			GSFileMetadata meta = dmClient.getMetadata(node.getDescription());
			URL url = meta.getUrl();
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection stringSelection = new StringSelection(url.toString());
            clipboard.setContents(stringSelection, this);
		}

		public void lostOwnership(Clipboard clipboard, Transferable contents) {
			// do nothing
		}
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
	}
	
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		log.debug("Source object " + source);
		ProjectSelection selection = projpane.getSelection();
		if (selection != null)
			showNode(projectTree, selection.getSelectedNode());
	}

	private void initLoginDialog(){
		/**
		 * IMPORTANT:
		 * 
		 * ConfigurationUrls.init("test"); To use against production:
		 * ConfigurationUrls.init("prod")
		 * 
		 * prior to any login
		 */
		ConfigurationUrls.init("prod");
		loginDialog = new GSLoginDialog();
		loginDialog.setSize(400, 250);
		
		loginDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// skip login dialog showing if there is a cached session
		loginDialog.setVisible(true);

		if(loginDialog.getGsSession().isLoggedIn())loginDialog.getGsSession().logout();	

		loginDialog.setPostLoginListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (loginDialog.getGsSession().isLoggedIn()) {
					log.info("Logged in now as " + loginDialog.getGsUser().getUsername()
							+ "\n\t files for this user in the DM are ...\n");
					loginDialog.dispose();
					loginbutton.setText("Logout "+loginDialog.getGsUser().getUsername());
					RefreshTask refreshTask = new RefreshTask(ProgressItem.INDETERMINATE_TYPE, "Retrieving GenomeSpace content");
			    	pd.executeTask(refreshTask);
				}
			}
		});
	}
	
	private class RefreshTask extends ProgressTask<Void,Void>{
		private ProjectTreeNode root = null;
		public RefreshTask(int pbtype, String message){
			super(pbtype, message);

			genomeRoot.removeAllChildren();
			genomeTree.setEnabled(false);
			
			GsSession session = loginDialog.getGsSession();
			DataManagerClient dmClient = session.getDataManagerClient();
			GSDirectoryListing defaultdir = dmClient.listDefaultDirectory();
			GSFileMetadata defaultdirmeta = defaultdir.getDirectory();
			root = metaToNode(defaultdirmeta);
			genomeTreeModel.insertNodeInto(root, genomeRoot, genomeRoot.getChildCount());
			genomeTreeModel.reload(genomeRoot);
		}
		public RefreshTask(int pbtype, String message, ProjectTreeNode root){
    		super(pbtype, message + " in " + root.getUserObject());
    		this.root = root;
 
    		root.removeAllChildren();
    		genomeTree.setEnabled(false);
    	}	
		@Override
		protected Void doInBackground(){
			if (isCancelled()) return null;
			refreshGSlist(root);
			return null;
		}
		@Override
    	protected void done(){
			pd.removeTask(this);
			genomeTreeModel.reload(root);
			if (root.getChildCount()==0) showNode(genomeTree, root);
			else showNode(genomeTree, (ProjectTreeNode)root.getNextNode());
			genomeTree.setEnabled(true);
		}
	}
	
	private void refreshGSlist(ProjectTreeNode root){
		if(root == null) return;
		
		GsSession session = loginDialog.getGsSession();
		DataManagerClient dmClient = session.getDataManagerClient();
		GSFileMetadata rootmeta =  dmClient.getMetadata(root.getDescription());

		addDirectoryContents(dmClient, dmClient.list(rootmeta), root);
	}
	
	/**
	 * recursively add the directory contents to the tree
	 * populate directory list
	 * 
	 * @param dmClient
	 * @param dirList
	 * @param parent
	 */
	public void addDirectoryContents(DataManagerClient dmClient, GSDirectoryListing dirList, ProjectTreeNode parent) {
		for (GSFileMetadata aDir : dirList.findDirectories()) {
			ProjectTreeNode dirnode = metaToNode(aDir);
			parent.add(dirnode);
			//userdir gets deep copy
			if(aDir.getOwner().getName().equals(loginDialog.getGsUser().getUsername())){
				GSDirectoryListing subDir = dmClient.list(aDir);
				addDirectoryContents(dmClient, subDir, dirnode);
			}
		}
		for (GSFileMetadata aFile : dirList.findFiles()) {
			ProjectTreeNode filenode = metaToNode(aFile);
			parent.add(filenode);
		}
	}

	private ProjectTreeNode metaToNode(GSFileMetadata meta){
		if (meta == null) return null;
		ProjectTreeNode node = new ProjectTreeNode(meta.getName());
		node.setDescription(meta.getPath());
		if (!meta.isDirectory())
			node.setAllowsChildren(false);
		return node;
	}
	
	private void showNode(JTree tree, ProjectTreeNode node){
		if (tree == null || node == null) return;
		TreePath path = new TreePath(node.getPath());
		tree.scrollPathToVisible(path);
		tree.setSelectionPath(path);
	}

	private class DirRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1637133536068447059L;
		public Component getTreeCellRendererComponent(JTree tree, Object value, 
	    		boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,hasFocus);
	        ProjectTreeNode node = (ProjectTreeNode)value;
	        // if a directory is a leaf node, draw it as an unopened non-leaf node
	        if (node.isLeaf() && node.getAllowsChildren())
	            setIcon(this.closedIcon);
	        return this;
	    }
	}
	
	//user logged in & a tree node is selected
	private boolean validate(JTree tree){
		if (loginDialog == null || !(loginDialog.getGsSession().isLoggedIn())){
			JOptionPane.showMessageDialog(null, "You must login before using this function.");
			return false;
		}
		if (tree != null && tree.getSelectionPath() == null){
			JOptionPane.showMessageDialog(null, "You must select a tree node.");
			return false;
		}
		return true;
	}
	
	//user logged in
	private boolean validate(){
		return validate(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	public Component getComponent() {
		return mainpanel;
	}
	
	private class UploadTask extends ProgressTask<ProjectTreeNode, Void>{
		public UploadTask(int pbtype, String message){
    		super(pbtype, message);
    	}	
		@Override
    	protected ProjectTreeNode doInBackground(){
			if (isCancelled()) return null;
			ProjectTreeNode newnode = null;

			ProjectTreeNode node = (ProjectTreeNode)projectTree.getSelectionPath().getLastPathComponent();
			if (node instanceof DataSetNode){
				DataSetNode dsnode = (DataSetNode)node;
				if (dsnode.getDataset() instanceof DSMicroarraySet){
					DSMicroarraySet mset = (DSMicroarraySet)dsnode.getDataset();

					DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();

					ArrayList<ProjectTreeNode> dirnodes = getDirNodes();

					if (dirnodes.size()==0) return null;
					String[] dirs = new String[dirnodes.size()];
					int i = 0;
					for (ProjectTreeNode dirnode : dirnodes){
						dirs[i++] = dirnode.getDescription();
					}
					String choice = (String)JOptionPane.showInputDialog(null, "Upload to", "Upload to GenomeSpace", 
							JOptionPane.QUESTION_MESSAGE, null, dirs, dirs[0]);
					if (choice == null) return null;
					ProjectTreeNode parentnode = null;
					for (i = 0; i < dirs.length; i++){
						if (choice.equals(dirs[i])){
							parentnode = dirnodes.get(i);
							break;
						}
					}
					if (parentnode == null || dmClient.getMetadata(parentnode.getDescription())==null) {
						JOptionPane.showMessageDialog(null, "Cannot upload file to GenomeSpace: destination directory not found");
						return null;
					}

					// check if the file to be uploaded exists on genome space
					for (i = 0; i < parentnode.getChildCount(); i++){
						ProjectTreeNode n = (ProjectTreeNode)parentnode.getChildAt(i);
						if (n.getUserObject().equals(mset.getLabel())){
							newnode = n;
							int response = JOptionPane.showConfirmDialog(null,
									"Overwrite existing "+mset.getLabel()+" in genomespace?",
									"Confirm Overwrite",
									JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if (response == JOptionPane.YES_OPTION)
								break;
							else return null;
						}
					}

					// upload local maset to genome space
					String localfname = uploadDir + mset.getLabel(); 
					mset.writeToFile(localfname);
					File localfile = new File(localfname);
					if (!localfile.exists()) {
						JOptionPane.showMessageDialog(null, "Cannot upload file to GenomeSpace: local dataset file not found");
						return null;
					}
					String saxdriver = System.getProperty("org.xml.sax.driver");
					System.setProperty("org.xml.sax.driver", "com.sun.org.apache.xerces.internal.parsers.SAXParser");
					try{
						GSFileMetadata filemeta = dmClient.uploadFile(localfile, dmClient.getMetadata(parentnode.getDescription()));
						//FIXME: delete on exception
						localfile.deleteOnExit();
	
						if (newnode == null){
							newnode = metaToNode(filemeta);
							genomeTreeModel.insertNodeInto(newnode, parentnode, parentnode.getChildCount());
						}
					}catch(Exception e){
						JOptionPane.showMessageDialog(null, "Cannot upload "+mset.getLabel()+" to genomespace",
								"Upload Error", JOptionPane.ERROR_MESSAGE);
					}
					System.setProperty("org.xml.sax.driver", saxdriver);
				}
			}

			return newnode;
		}
		@Override
    	protected void done(){
			pd.removeTask(this);
			ProjectTreeNode newnode = null;
			try{
				newnode = get();
			}catch(ExecutionException e){
    			e.printStackTrace();
    		}catch(InterruptedException e){
    			e.printStackTrace();
    		}
    		if (newnode == null) return;
			showNode(genomeTree, newnode);			
		}
	}
	
	private ArrayList<ProjectTreeNode> getDirNodes(){
		ArrayList<ProjectTreeNode> dirnodes = new ArrayList<ProjectTreeNode>();
		if(genomeRoot.getChildCount()==0) return dirnodes;

		ProjectTreeNode home = (ProjectTreeNode) genomeRoot.getChildAt(0);
		for (int i = 0; i < home.getChildCount(); i++){
			ProjectTreeNode userhome = (ProjectTreeNode)home.getChildAt(i);
			if(userhome.getUserObject().equals(loginDialog.getGsUser().getUsername())){
				getDir(userhome, dirnodes);
			}
		}
		return dirnodes;
	}
	
	private void getDir(ProjectTreeNode rootdir,  ArrayList<ProjectTreeNode> dirnodes){
		dirnodes.add(rootdir);
		for (int i = 0; i < rootdir.getChildCount(); i++){
			ProjectTreeNode subdir = (ProjectTreeNode)rootdir.getChildAt(i);
			if(subdir!=null && subdir.getAllowsChildren())
				getDir(subdir, dirnodes);
		}
	}

	private class ConvertActionListener implements ActionListener{
		private GSDataFormat format = null;
		public ConvertActionListener(GSDataFormat fmt){
			format = fmt;
		}
		public void actionPerformed(ActionEvent e){
			if (!validate(genomeTree)) return;
			ConvertTask convertTask = new ConvertTask(ProgressItem.INDETERMINATE_TYPE, "Converting...", format);
			pd.executeTask(convertTask);
		}
	}

	private class ConvertTask extends ProgressTask<ProjectTreeNode,Void>{
		private GSDataFormat format = null;
		public ConvertTask(int pbtype, String message, GSDataFormat fmt){
    		super(pbtype, message);
    		format = fmt;

    	}	
		@Override
		protected ProjectTreeNode doInBackground(){
			if (isCancelled()) return null;
			ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();
			ProjectTreeNode parent = (ProjectTreeNode)node.getParent();
			DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
			GSFileMetadata source = dmClient.getMetadata(node.getDescription());
			GSFileMetadata parentdir = dmClient.getMetadata(parent.getDescription());

			if (source==null || parentdir==null) return null;
			String newname = source.getName();
			if (newname == null || newname.trim().isEmpty()) return null;
			if (newname.contains("."))
				newname = newname.substring(0, newname.lastIndexOf("."));
			newname += "." + format.getFileExtension();
			
			// check if the file to be converted into exists on genome space
			for (int i = 0; i < parent.getChildCount(); i++){
				ProjectTreeNode n = (ProjectTreeNode)parent.getChildAt(i);
				if (n.getUserObject().equals(newname)){
					JOptionPane.showMessageDialog(null, "Cannot convert file in genomespace: "+newname+" already exists",
							"Convertion Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
			
			ProjectTreeNode newnode = null;
			int newid = findInsertPosition(parent, newname);

			try{
				GSFileMetadata newGsFile = dmClient.copy(source, parentdir, newname, format);
				newnode = metaToNode(newGsFile);
				genomeTreeModel.insertNodeInto(newnode, parent, newid);

			}catch(Exception e){
				JOptionPane.showMessageDialog(null, "Cannot convert "+node.getUserObject()+" to "+format.getName(),
						"Convertion Error", JOptionPane.ERROR_MESSAGE);
			}
			return newnode;
		}
		@Override
    	protected void done(){
			pd.removeTask(this);
			ProjectTreeNode node = null;
			try{
				node = get();
			}catch(ExecutionException e){
    			e.printStackTrace();
    		}catch(InterruptedException e){
    			e.printStackTrace();
    		}
    		if (node == null) return;
    		genomeTreeModel.reload(node);
			showNode(genomeTree, node);
		}
	}

	private int findInsertPosition(ProjectTreeNode parent, String newname){
		int start = 0, end = parent.getChildCount();
		for (int i = 0; i < end; i++){
			ProjectTreeNode n = (ProjectTreeNode)parent.getChildAt(i);
			if(n.getAllowsChildren()) start++;
		}
		
		while(start <= end){
			int mid = (start+end)/2;
			ProjectTreeNode n = (ProjectTreeNode)parent.getChildAt(mid);
			if(newname.compareTo((String)n.getUserObject()) <= 0) end = mid-1;
			else start = mid+1;
		}
		return start;
	}
	
	private class DownloadTask extends ProgressTask<Void,Void>{
		public DownloadTask(int pbtype, String message){
    		super(pbtype, message);
    	}	
		@Override
		protected Void doInBackground(){
			if (isCancelled()) return null;
			ProjectTreeNode node = (ProjectTreeNode)genomeTree.getSelectionPath().getLastPathComponent();
			if (node.isLeaf()) {
				DataManagerClient dmClient = loginDialog.getGsSession().getDataManagerClient();
				GSFileMetadata meta = dmClient.getMetadata(node.getDescription());
				GSDataFormat expFormat = null;
				for(GSDataFormat format : meta.getAvailableDataFormats()){
				    if(format.getName().equals("geWorkbench exp")){
				        expFormat = format;
				    }
				}
				if (expFormat == null) {
					JOptionPane.showMessageDialog(null, "Cannot download "+node.getUserObject()+" in geworkbench format",
							"No converter available", JOptionPane.ERROR_MESSAGE);
					return null;
				}
				String targetname = (String)node.getUserObject();
				if (targetname == null || targetname.trim().isEmpty()) return null;
				if (meta.getDataFormat() != expFormat){
					if (targetname.contains("."))
						targetname = targetname.substring(0, targetname.lastIndexOf("."));
					targetname += "." + expFormat.getFileExtension();
				}
				File targetfile = new File(downloadDir + targetname);
				try{
					dmClient.downloadFile(meta, expFormat, targetfile, true);
					//FIXME: delete on exception
					targetfile.deleteOnExit();
				}catch(Exception e){
					JOptionPane.showMessageDialog(null, "Cannot download "+node.getUserObject()+" to geworkbench",
							"Download Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}

				try{
					DataSetFileFormat fileFormat = new org.geworkbench.parsers.ExpressionFileFormat();
					CSMicroarraySet dataSet = (CSMicroarraySet)fileFormat.getDataFile(targetfile);
					publishProjectNodeAddedEvent(new ProjectNodeAddedEvent("", dataSet, null));
				}catch(Exception e){
					JOptionPane.showMessageDialog(null, "Cannot open "+node.getUserObject()+" in geworkbench",
							"Open File Error", JOptionPane.ERROR_MESSAGE);
				}
			}
			return null;
		}
		@Override
    	protected void done(){
			pd.removeTask(this);
		}
	}
}
