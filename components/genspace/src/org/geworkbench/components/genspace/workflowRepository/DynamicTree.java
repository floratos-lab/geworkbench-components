package org.geworkbench.components.genspace.workflowRepository;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.LoginFactory;
import org.geworkbench.components.genspace.RuntimeEnvironmentSettings;
import org.geworkbench.components.genspace.entity.User;
import org.geworkbench.components.genspace.entity.UserWorkflow;
import org.geworkbench.components.genspace.entity.WorkflowFolder;

public class DynamicTree extends JPanel implements ActionListener,
		TreeSelectionListener {

	protected DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
	protected DefaultTreeModel treeModel;
	protected JTree tree;
	private Toolkit toolkit = Toolkit.getDefaultToolkit();
	private static String REMOVE_COMMAND = "remove";
	private static String NEW_COMMAND = "new";
	RepositoryPanel repositoryPanel;

	public DynamicTree(RepositoryPanel rp) {
		super(new BorderLayout());
		repositoryPanel = rp;
		JPanel treePanel = new JPanel(new GridLayout(1, 0));


//
//		JButton newButton = new JButton("New Folder");
//		newButton.setActionCommand(NEW_COMMAND);
//		newButton.addActionListener(this);

		JButton removeButton = new JButton("Delete Selected");
		removeButton.setActionCommand(REMOVE_COMMAND);
		removeButton.addActionListener(this);

		// Lay everything out.
		// treePanel.setPreferredSize(new Dimension(300, 150));
		add(treePanel, BorderLayout.CENTER);

//		panel.add(newButton);
//		panel.add(removeButton);
		add(removeButton, BorderLayout.SOUTH);

		recalculateTree();
		treeModel = new DefaultTreeModel(rootNode);
		treeModel.addTreeModelListener(new MyTreeModelListener());
		tree = new JTree(treeModel);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);
		tree.setShowsRootHandles(true);
		JScrollPane scrollPane = new JScrollPane(tree);
		add(scrollPane);
	}

	public void recalculateTree() {
		User u = LoginFactory.getUser();
		if (u != null) {
			WorkflowFolder root = LoginFactory.getUserOps().getRootFolder();
			rootNode = new DefaultMutableTreeNode(root);
			addUserWorkflowTree(root);
			repaint();
		}
	}

	private void addUserWorkflowTree(WorkflowFolder r) {

		HashMap<WorkflowFolder, DefaultMutableTreeNode> folders = new HashMap<WorkflowFolder, DefaultMutableTreeNode>();
		folders.put(r, rootNode);
		// first add all folders
		// Whenever a folder was added in the ADD function the list of folders
		// is ordered by name
		// so we don't worry about it here.Å
		
		ArrayList<WorkflowFolder> flders = new ArrayList<WorkflowFolder>();
		flders.add(r);
		System.out.println(r.getChildren());
		for(WorkflowFolder f : r.getChildren())
		{
			flders.add(f);
		}
		for (WorkflowFolder f : flders) {
			DefaultMutableTreeNode fnode;
			System.out.println(f.getChildren());
			if(f.getParent() != null)
			{
				fnode = new DefaultMutableTreeNode(f);
				folders.put(f, fnode);
				folders.get(f.getParent()).add(fnode);
				System.out.println("Adding " + f.getName() + " to " + folders.get(f.getParent()).getUserObject());
			}
			else
				fnode = rootNode;
			for(UserWorkflow w : f.getWorkflows())
			{
				fnode.add(new WorkflowNode(w));
			}
		}
	}
	public void recalculateAndReload() {
		recalculateTree();
		treeModel.setRoot(rootNode);
		// treeModel.reload();
		// this.repaint();
	}

	/** Remove the currently selected node. */
	public void removeCurrentNode() {
		TreePath currentSelection = tree.getSelectionPath();
		if (currentSelection != null) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
					.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
			if (parent != null) {// Don't delete the root
				treeModel.removeNodeFromParent(currentNode);
				return;
			}
		}

		// Either there was no selection, or the root was selected.
		toolkit.beep();
	}

	/** Add child to the root */
	public void addWorkflowToRoot(UserWorkflow child) {
		addWorkflowObject(rootNode, child, true);
	}

	private void addWorkflowObject(DefaultMutableTreeNode parent, UserWorkflow workflow,
			boolean visibile) {
		WorkflowNode childNode = new WorkflowNode(workflow);
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

		// Make sure the user can see the lovely new node.
		if (visibile) {
			tree.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
	}

	/** Add child to the currently selected node. */
	public DefaultMutableTreeNode addObjectToSelected(Object child) {
		DefaultMutableTreeNode parentNode = null;
		TreePath parentPath = tree.getSelectionPath();

		if (parentPath == null) {
			parentNode = rootNode;
		} else {
			parentNode = (DefaultMutableTreeNode) (parentPath
					.getLastPathComponent());
		}

		return addObject(parentNode, child, true);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
			Object child) {
		return addObject(parent, child, true);
	}

	public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
			Object child, boolean shouldBeVisible) {
		DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);

		if (parent == null) {
			parent = rootNode;
		}

		// It is key to invoke this on the TreeModel, and NOT
		// DefaultMutableTreeNode
		treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

		// Make sure the user can see the lovely new node.
		if (shouldBeVisible) {
			tree.scrollPathToVisible(new TreePath(childNode.getPath()));
		}
		return childNode;
	}

	class MyTreeModelListener implements TreeModelListener {
		@Override
		public void treeNodesChanged(TreeModelEvent e) {
			DefaultMutableTreeNode node;
			node = (DefaultMutableTreeNode) (e.getTreePath()
					.getLastPathComponent());

			/*
			 * If the event lists children, then the changed node is the child
			 * of the node we've already gotten. Otherwise, the changed node and
			 * the specified node are the same.
			 */

			int index = e.getChildIndices()[0];
//			node = (DefaultMutableTreeNode) (node.getChildAt(index));

			// System.out.println("The user has finished editing the node.");
			// System.out.println("New value: " + node.getUserObject());
		}

		@Override
		public void treeNodesInserted(TreeModelEvent e) {
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent e) {
		}

		@Override
		public void treeStructureChanged(TreeModelEvent e) {
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (LoginFactory.isLoggedIn()) {

			if (NEW_COMMAND.equals(command)) {
				newCommand();
			} else if (REMOVE_COMMAND.equals(command)) {
				removeCommand();
			}

		}
	}

	private void removeCommand() {
		TreePath currentSelection = tree.getSelectionPath();
		if (currentSelection != null) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
					.getLastPathComponent());
			MutableTreeNode parent = (MutableTreeNode) (currentNode.getParent());
			if (parent != null) {// Don't delete the root
				if (currentNode instanceof WorkflowNode) {
					// Removing a workflow
					WorkflowNode wf = (WorkflowNode) currentNode;
					final UserWorkflow uw = wf.userWorkflow;
					SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
						protected Boolean doInBackground() throws Exception {
							boolean ret = LoginFactory.getWorkflowOps()
									.deleteMyWorkflow(uw);
							if(ret)
								System.out.println("success");
							else
								System.out.println("ret false");
							LoginFactory.userUpdate();
							GenSpace.getInstance().getWorkflowRepository().updateUser();

							return ret;
						};

						protected void done() {
							try {
								if (get()) {
									recalculateAndReload();
									repositoryPanel.workflowRepository
											.clearWorkflowData();
								}
							} catch (InterruptedException e) {
								GenSpace.logger.error("Unable to talk to server",e);
							} catch (ExecutionException e) {
								GenSpace.logger.error("Unable to talk to server",e);
							}
						};
					};
					worker.execute();
				} else {
					// removing a folder
					int option = JOptionPane
							.showConfirmDialog(null,
									"All workflows in the folder will be lost. Continue?");
					if (option == JOptionPane.YES_OPTION) {
						final WorkflowFolder folder = (WorkflowFolder) currentNode.getUserObject();
						
						SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
							protected Boolean doInBackground() throws Exception {
								boolean ret = LoginFactory.getWorkflowOps()
										.deleteMyFolder(folder);
								LoginFactory.updateCachedUser();
								return ret;
							};

							protected void done() {
								try {
									if (get()) {
										recalculateAndReload();
									}
								} catch (InterruptedException e) {
									GenSpace.logger.error("Unable to talk to server",e);
								} catch (ExecutionException e) {
									GenSpace.logger.error("Unable to talk to server",e);
								}
							};
						};
						worker.execute();
						} 
					}
				}
			}
		}
	

	private void newCommand() {
		// Adds a folder as a child of the root folder
		// Add button clicked
		final String folderName = JOptionPane
				.showInputDialog("Select a folder name");
		if (folderName != null && !folderName.trim().equals("")
				&& !LoginFactory.getUser().containsFolderByName(folderName)) {
			// send add_folder to the server
			SwingWorker<WorkflowFolder, Void> worker = new SwingWorker<WorkflowFolder, Void>() {
				protected WorkflowFolder doInBackground() {
					WorkflowFolder folder = new WorkflowFolder();
					folder.setName(folderName);
					folder.setOwner(LoginFactory.getUser());
					folder.setParent(LoginFactory.getUser().getRootFolder());
					WorkflowFolder ret = LoginFactory.getWorkflowOps().addFolder(folder);
					LoginFactory.updateCachedUser();
					GenSpace.getInstance().getWorkflowRepository().updateUser();
					return ret;
				};

				protected void done() {
					WorkflowFolder result = null;
					try {
						result = get();
					} catch (InterruptedException e) {
						e.printStackTrace();
						GenSpace.logger.error("Error talking to server", e);
					} catch (ExecutionException e) {
						e.printStackTrace();
						GenSpace.logger.error("Error talking to server", e);
					}
					if (result == null || result.equals("")) {
//						LoginFactory.getUser().getFolders().add(result);
						Collections.sort(LoginFactory.getUser().getFolders());
						recalculateAndReload();
						// addObject(rootNode, folderName);
					} else {
						JOptionPane.showMessageDialog(null, "Success");
					}
				};
			};
			worker.execute();
		}

	}

	/**
	 * Invoked when a node of the tree is selected (the rename is performed by
	 * myTreeModeListener though.
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		if (tree.getSelectionPath() != null
				&& tree.getSelectionPath().getPathCount() > 1) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
					.getSelectionPath().getLastPathComponent();
			if (node instanceof WorkflowNode) {
				WorkflowNode wf = (WorkflowNode) node;
				repositoryPanel.workflowRepository.graphPanel
						.render(wf.userWorkflow.getWorkflow());
				repositoryPanel.workflowRepository.workflowDetailsPanel
						.setAndPrintWorkflow(wf.userWorkflow.getWorkflow());
				repositoryPanel.workflowRepository.workflowCommentsPanel
						.setData(wf.userWorkflow.getWorkflow());
			}
		}
	}
}
