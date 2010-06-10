/**
 * 
 */
package org.geworkbench.components.geneontology2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.GoAnalysisResult;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.BrowserLauncher;

/**
 * Visual component to show the result from GO Term Analysis.
 * 
 * @author zji
 * @version $Id$
 *
 */
@AcceptTypes({GoAnalysisResult.class})
public class GoAnalysisResultView extends JPanel implements VisualPlugin {
	private static final int COLUMN_COUNT = 7;
	private static final long serialVersionUID = -579377200878351871L;
	static Log log = LogFactory.getLog(GoAnalysisResultView.class);
	
	private DefaultTreeModel treeModel = null;
	private DefaultTreeModel singleGeneModel = null;
	
	private JTree tree = null;
	
	private GoTableModel tableModel = null;
	
	private JTree singleGeneTree = null;
	private SingleGeneTreeNode singleGeneTreeRoot = null;
	private DefaultTableModel geneListTableModel = null;
	private JEditorPane geneDetails = null;
	private JTable table = null;
	
	private static Object[] geneListHeaders = new String[]{"Gene Symbol", "Expression change", "Description"};
	
	private JTabbedPane primaryView = null;
	private JPanel tableTab = null;
	private JPanel treeTab = null;
	private TableRowSorter<GoTableModel> sorter = null;
	
	private GoTreeNode root = new GoTreeNode (); // root

	private void initializePrimaryView() {
		primaryView = new JTabbedPane();

		tableTab = new JPanel();
		treeTab = new JPanel();
		primaryView.add(tableTab, "Table Browser");
		primaryView.add(treeTab, "Tree Browser");

		// more details following
		tableTab.setLayout(new BoxLayout(tableTab, BoxLayout.Y_AXIS));
		JPanel namespacePanel = new JPanel();
		namespacePanel.add(new JLabel("GO Subontology (Namespaces)"));
		final JRadioButton[] namespaceButton = new JRadioButton[3];
		allButton = new JRadioButton("All");
		allButton.setSelected(true); // initial status
		ActionListener namespaceListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				tableModel.populateNewResult(result);
				if(allButton.isSelected()) {
					sorter.setRowFilter(null);
					table.repaint();
				} else {
					for(int i=0; i<3; i++){
						if(namespaceButton[i].isSelected()) {
							RowFilter<GoTableModel, Integer> filter = new NamespaceFilter(
											namespaceLabels[i]);
							sorter.setRowFilter(filter);
							table.repaint();
							return;
						}
					}
				}
			}
			
		};
		allButton.addActionListener(namespaceListener );
		namespacePanel.add(allButton);
		ButtonGroup namespaceGroup = new ButtonGroup();
		namespaceGroup.add(allButton);
		for(int i=0; i<3; i++){
			namespaceButton[i] = new JRadioButton(namespaceLabels[i]);
			namespacePanel.add(namespaceButton[i]);
			namespaceGroup.add(namespaceButton[i]);
			namespaceButton[i].addActionListener(namespaceListener );
		}
		
		JPanel alternateOntologyPanel = new JPanel();
		alternateOntologyPanel.add(new JLabel("Alternate Ontology"));
		JTextField alternateOntology = new JTextField(20);
		alternateOntology.setEnabled(false);
		alternateOntologyPanel.add(alternateOntology);
		tableTab.add(namespacePanel);
		tableTab.add(alternateOntologyPanel);
		
		tableModel = new GoTableModel();
		table = new JTable(tableModel);
		sorter = new TableRowSorter<GoTableModel>(tableModel);
		table.setRowSorter(sorter);
		table.setDefaultRenderer(Double.class, new DoubleRenderer(4));
		tableTab.add(new JScrollPane(table));
		
		ListSelectionModel listSelectionModel = table.getSelectionModel();
		listSelectionModel.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting() == true) return; // avoid process twice

				if(table.getSelectedRow()==-1) {
					geneListTableModel.setDataVector(new Object[0][0], null);
					singleGeneTreeRoot.removeAllChildren();
					singleGeneModel.reload();
					geneDetails.setText("");
					return; // no selection, clear the responding display
				}

				int index = table.convertRowIndexToModel( table.getSelectedRow() );
				if(index>=0 && index<=tableModel.getRowCount()) { // in case the selection is not in the new range
					Integer goId = (Integer)tableModel.getValueAt(index, 0);
					populateGeneList(goId);
	
					populateSingleGeneTree(goId);
					
					showTermDetail(goId);
				}
			}
			
		});
		
		// this listener is particularly to support the feature requested in bug 2266
		table.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				int index = table.convertRowIndexToModel( table.getSelectedRow() );
				if(index>=0 && index<=tableModel.getRowCount()) { // in case the selection is not in the new range
					Integer goId = (Integer)tableModel.getValueAt(index, 0);
					showTermDetail(goId);
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
		
		});

		treeTab.setLayout(new BorderLayout());
		JPanel searchPanel = new JPanel();
		searchPanel.add(new JLabel("Search by GO term ID or name"));
		searchId = new JTextField(20);
		searchPanel.add(searchId);
		JButton searchButton = new JButton("Search");
		searchPanel.add(searchButton); 
		treeTab.add(searchPanel, BorderLayout.NORTH);
		
		searchButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String searchText = searchId.getText();
				
				List<GoTreeNode> treePathList = null;
				TreePath selectedPath = tree.getSelectionPath();
				if(selectedPath==null) { // find the first one if no node is selected
					GoTreeNode root = (GoTreeNode)(treeModel.getRoot());
					treePathList = searchMatchingNode(root, searchText);
				} else { // find the next
					treePathList = new ArrayList<GoTreeNode>();
					for(Object obj: selectedPath.getPath()) {
						treePathList.add((GoTreeNode)obj);
					}
					searchNext(treePathList, searchText);
				}
				if(treePathList==null || treePathList.size()==0)
					log.debug("go term not found for ID/name "+ searchText);
				else {
					TreePath treePath = new TreePath(treePathList.toArray());
					tree.setSelectionPath(treePath);
					tree.scrollPathToVisible(treePath);
					log.debug("go term found "+treePath.toString());
				}
			}
			
		});

		treeModel = new DefaultTreeModel(root);

		tree = new JTree(treeModel);
		tree.setExpandsSelectedPaths(true);
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				GoTreeNode node = (GoTreeNode) tree.getLastSelectedPathComponent();

				if (node == null || node.goId==0) { //Nothing or ROOT is selected.	
					geneListTableModel.setDataVector(null, geneListHeaders);
					return;
				}
				
				Integer goId = node.goId;
				populateGeneList(goId);
				populateSingleGeneTree(goId);
				showTermDetail(goId);
			}
			
		});

		treeTab.add(new JScrollPane(tree), BorderLayout.CENTER);
	}
	
	private JPanel initializeLeftPanel() {
		JPanel leftPanel = new JPanel();

		initializePrimaryView();
		JPanel geneListWindow = new JPanel();

		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		JSplitPane splitPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				primaryView, geneListWindow);
		splitPane.setDividerLocation(500);
		leftPanel.add(splitPane, BorderLayout.CENTER);

		geneListWindow.setLayout(new BoxLayout(geneListWindow, BoxLayout.Y_AXIS));
		JPanel showGeneForPanel = new JPanel();
		showGeneForPanel.add(new JLabel("Show genes for"));
		termButton = new JRadioButton("Term");
		termButton.setSelected(true);
		termAndDewscendantsButton = new JRadioButton("Term and its descendants");
		showGeneForPanel.add(termButton);
		showGeneForPanel.add(termAndDewscendantsButton);
		
		ActionListener genesForListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshGeneView();
			}
		};
		termButton.addActionListener(genesForListener);
		termAndDewscendantsButton.addActionListener(genesForListener);
		
		ButtonGroup geneForGroup = new ButtonGroup();
		geneForGroup.add(termButton);
		geneForGroup.add(termAndDewscendantsButton);
		
		geneListWindow.add(showGeneForPanel);

		JPanel showGeneFromPanel = new JPanel();
		showGeneFromPanel.add(new JLabel("Show genes from"));
		changedGeneListButton = new JRadioButton("Changed gene list");
		changedGeneListButton.setSelected(true);
		referenceListButton = new JRadioButton("Reference list");
		showGeneFromPanel.add(changedGeneListButton);
		showGeneFromPanel.add(referenceListButton);
		changedGeneListButton.addActionListener(genesForListener);
		referenceListButton.addActionListener(genesForListener);
		
		ButtonGroup geneFromGroup = new ButtonGroup();
		geneFromGroup.add(changedGeneListButton);
		geneFromGroup.add(referenceListButton);
		
		geneListWindow.add(showGeneFromPanel);

		Object[][] data = new Object[0][3]; // empty data at initialization
		geneListTableModel = new DefaultTableModel(data, geneListHeaders);
		geneListTable = new JTable(geneListTableModel);
		prepareCopyToSet();
		ListSelectionModel geneListModel = geneListTable.getSelectionModel();
		geneListModel.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				// refresh gene detail panel
				int index = geneListTable.getSelectedRow();
				if(index>=0 && index<=geneListTableModel.getRowCount()) { // in case the selection is not in the new range
					showGeneDetail((String)geneListTableModel.getValueAt(index, 0));
				}
			}
			
		});
		geneListTable.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// refresh gene detail panel
				int index = geneListTable.getSelectedRow();
				if(index>=0 && index<=geneListTableModel.getRowCount()) { // in case the selection is not in the new range
					showGeneDetail((String)geneListTableModel.getValueAt(index, 0));
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
			}
			
		});
		geneListWindow.add(new JScrollPane(geneListTable));
		
		primaryView.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				refreshGeneView();
			}
			
		});
		return leftPanel;
	}
	
	private void refreshGeneView () {
		if(primaryView.getSelectedComponent()==tableTab) {
			if(table.getSelectedRow()==-1)return;
			
			int index = table.convertRowIndexToModel( table.getSelectedRow() );
			if(index>=0 && index<=tableModel.getRowCount()) { // in case the selection is not in the new range
				Integer goId = (Integer)tableModel.getValueAt(index, 0);

				populateGeneList(goId);
				populateSingleGeneTree(goId);
				showTermDetail(goId);
			}
		} else if (primaryView.getSelectedComponent()==treeTab) {
			GoTreeNode node = (GoTreeNode) tree.getLastSelectedPathComponent();
			if(node==null || node.goId==0) {
				geneListTableModel.setDataVector(null, geneListHeaders);
				singleGeneTreeRoot.removeAllChildren();
				singleGeneModel.reload();
				geneDetails.setText("");
				return;
			}
			Integer goId = node.goId;

			populateGeneList(goId);
			populateSingleGeneTree(goId);
			showTermDetail(goId);
		} else {
			log.error("invalid selection of primaryView");
		}
	}

	private JPanel initializeRightPanel() {
		JPanel rightPanel = new JPanel();
		JPanel singelGeneView = new JPanel();
		JPanel detailPanel = new JPanel();
		
		rightPanel.setMinimumSize(new Dimension(300, 500));
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				singelGeneView, detailPanel), BorderLayout.CENTER);

		// more details following
		singelGeneView.setLayout(new BoxLayout(singelGeneView, BoxLayout.Y_AXIS));
		singelGeneView.add(new JLabel("Single Term View"));
		singleGeneTreeRoot = new SingleGeneTreeNode (); // root
		singleGeneModel = new DefaultTreeModel(singleGeneTreeRoot);

		singleGeneTree = new JTree(singleGeneModel) {
			private static final long serialVersionUID = 8852424763575859252L;

			// completely disable collapsing
			protected void setExpandedState(TreePath path, boolean state) {
	            // Ignore all collapse requests; collapse events will not be fired
	            if (state) {
	                super.setExpandedState(path, state);
	            }
	        }
		};
		singleGeneTree.setRootVisible(false);
		singelGeneView.add(new JScrollPane(singleGeneTree));

		detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
		detailPanel.add(new JLabel("Term/Gene Details"));
		geneDetails = new JEditorPane();
		geneDetails.setEditable(false);
		detailPanel.add(new JScrollPane(geneDetails));
		
		geneDetails.addHyperlinkListener(new HyperlinkListener() {

			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED) {
					try {
						BrowserLauncher
								.openURL(e.getURL().toString());
					} catch (IOException e1) {
						e1.printStackTrace();
						log.error("BrowserLauncher failed on "+e.getURL().toString());
					}
				}
			}

		});
		
		return rightPanel;
	}
	
	/**
	 * 
	 */
	public GoAnalysisResultView() {
		super();

		JPanel leftPanel = initializeLeftPanel();
		JPanel rightPanel = initializeRightPanel();
		
		setLayout(new BorderLayout());
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel), BorderLayout.CENTER);
	}

	private void prepareCopyToSet() {
		geneListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
					JPopupMenu popup = new JPopupMenu();
					JMenuItem menuItem = new JMenuItem("Copy to set");
					menuItem.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent e) {
							Set<String> genes = new HashSet<String>();
							TableModel model = geneListTable.getModel();
							for (int i = 0; i < model.getRowCount(); i++)
								genes.add((String) (model.getValueAt(i, 0)));
							publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
									DSGeneMarker.class,
									GeneToMarkers(genes),
									org.geworkbench.events.SubpanelChangedEvent.SET_CONTENTS));

						}

					});
					popup.add(menuItem);

					if (e.isPopupTrigger()) {
						popup.show(e.getComponent(), e.getX(), e.getY());
					} else {
						// do nothing: popup menu cannot be triggered on this
						// platform
					}
				}
			};
		});
	}

	@SuppressWarnings("unchecked")
	private DSPanel<DSGeneMarker> GeneToMarkers(Set<String> genes) {
		DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(
				"Selected Genes", "Go Terms Anlaysis");
		DSMicroarraySet<DSMicroarray> dataset = (DSMicroarraySet<DSMicroarray>) (ProjectPanel
				.getInstance().getSelection().getDataSet());
		for (Object obj : dataset.getMarkers()) {
			DSGeneMarker marker = (DSGeneMarker) obj;
			if (genes.contains(marker.getGeneName()))
				selectedMarkers.add(marker);
		}

		return selectedMarkers;
	}

	@Publish
	protected SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			SubpanelChangedEvent<DSGeneMarker> subpanelChangedEvent) {
		return subpanelChangedEvent;
		
	}

	private void showTermDetail(int goId) {
		geneDetails.setContentType("text/plain");
		if(GoAnalysisResult.getAnnotatedGenes(goId)==null) {
			geneDetails.setText("No gene annotated to GO ID "+goId);
			return;
		}
			
		StringBuffer sb = new StringBuffer("Term GO ID: "+goId+"\nGenes annotated:\n");
	
		// here are the genes annotated to this term only, not to descendants.
		int i=0;
		for(String gene: GoAnalysisResult.getAnnotatedGenes(goId)) {
			sb.append(gene).append("\n   Gene title: ").append(
					GoAnalysisResult.getGeneDetail(gene))
					.append("\n\n");
			i++;
		}
		
		geneDetails.setText(sb.toString());
		geneDetails.setCaretPosition(0); // move the scroll pane to top if the text is long
	}
	
	protected void showGeneDetail(String geneSymbol) {
		int geneId = GoAnalysisResult.getEntrezId(geneSymbol);
		geneDetails.setContentType("text/html");
		geneDetails.setText("Details of Gene " + geneSymbol
				+ "<p>Entrez ID: "+geneId+" <a href=http://www.ncbi.nlm.nih.gov/gene/" + geneId
				+ ">Link to Entrez Gene Database</a>");
	}
	
	/* (non-Javadoc)
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	// this is just something you must do
	public Component getComponent() {
		return this;
	}
	
	GoAnalysisResult result = null;

	private static Map<Integer, GoTreeNode> namespaceId2Node = null;
	private AbstractButton allButton;
	
	private void populateTreeRrepresentation() {
		/* in case the first time */
		if(namespaceId2Node==null)
			namespaceId2Node = new HashMap<Integer, GoTreeNode>();
		if(namespaceId2Node.size()==0) {
			GoTreeNode root = (GoTreeNode)(treeModel.getRoot());
			for(int namespaceId: GoAnalysisResult.getNamespaceIds()) {
				GoTreeNode namespaceNode = new GoTreeNode(result, namespaceId, root); 
				root.add(namespaceNode);
				namespaceId2Node.put(namespaceId, namespaceNode);
			}
		}

		for(int namespaceId: GoAnalysisResult.getNamespaceIds()) {
			GoTreeNode namespaceNode = namespaceId2Node.get(namespaceId);
			namespaceNode.removeAllChildren();
			for(Integer child: GoAnalysisResult.getOntologyChildren(namespaceId)) {
				addChildren(child, namespaceNode);
			}
		}
	}
	
	private void searchNext(List<GoTreeNode> treePathList, String searchText) {
		int lastIndex = treePathList.size()-1;
		GoTreeNode node = treePathList.remove(lastIndex); // remove the last matching node
		if(node==treeModel.getRoot()) {  // path list is empty now: no more found
			treePathList.clear();
			List<GoTreeNode> list = searchMatchingNode(node, searchText);
			if(list!=null)
				treePathList.addAll( list );
			return;
		}

		GoTreeNode parent = (GoTreeNode)node.getParent();
		for(int index = parent.getIndex(node)+1; index <parent.getChildCount(); index++) {
			GoTreeNode sibling = (GoTreeNode)parent.getChildAt(index);
			
			List<GoTreeNode> list = searchMatchingNode(sibling, searchText);
			if(list!=null) {
				treePathList.addAll(list);
				return;
			}
		}
		// if not in siblings
		searchNext(treePathList, searchText);
	}
	
	/* recursive call. terminate at either found or not children*/
	private List<GoTreeNode> searchMatchingNode(GoTreeNode node, String searchText) {
		GoTermMatcher matcher = GoTermMatcher.createMatcher(node, searchText);
		if(matcher.match()){
			List<GoTreeNode> list= new ArrayList<GoTreeNode>();
			list.add(node);
			return list;
		}
		for(int i=0; i<node.getChildCount(); i++) {
			GoTreeNode child = (GoTreeNode)(node.getChildAt(i));
			List<GoTreeNode> list = searchMatchingNode(child, searchText); 
			if(list!=null) {
				list.add(0, node);
				return list;
			}
		}
		return null;
	}

	// this does the similar thing as populateTreeRrepresentation with slightly different approach.
	private void populateSingleGeneTree(int geneId) {
		singleGeneTreeRoot.removeAllChildren();

		for(int namespaceId: GoAnalysisResult.getNamespaceIds()) {
			findAndAddChildren(geneId, namespaceId, singleGeneTreeRoot);
		}
		singleGeneModel.reload();
        // Traverse tree from root
		expandAll(new TreePath(singleGeneTreeRoot));
		//singleGeneTree.setEnabled(false); // this is a super simple way to disable collapsing for this tree, but it will gray out the display
	}
	
	/* expand entire single gene tree*/
    private void expandAll(TreePath parent) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		for (int i = 0; i < node.getChildCount(); i++) {
			TreeNode n = (TreeNode) node.getChildAt(i);
			TreePath path = parent.pathByAddingChild(n);
			expandAll(path);
		}

		singleGeneTree.expandPath(parent);
	}

	private boolean findAndAddChildren(Integer targetGene, Integer childId, SingleGeneTreeNode parent) {
		boolean found = false;
		SingleGeneTreeNode childNode = new SingleGeneTreeNode(childId); 
		if(childId.equals(targetGene)) {
			found = true;
		}

		List<Integer> grandchildren = GoAnalysisResult.getOntologyChildren(childId);
		if(grandchildren==null) {
			if(found)
				parent.add(childNode);
			return found;
		}
		
		for(Integer grandchild: grandchildren) {
			boolean foundInSubtree = findAndAddChildren(targetGene, grandchild, childNode);
			if(foundInSubtree)found = true;
		}
		if(found)
			parent.add(childNode);

		return found;
	}
	
	private void addChildren(Integer childId, GoTreeNode parent) {
		GoTreeNode childNode = new GoTreeNode(result, childId, parent); 
		parent.add(childNode);

		List<Integer> grandchildren = GoAnalysisResult.getOntologyChildren(childId);
		if(grandchildren==null) return;
		
		for(Integer grandchild: grandchildren) {
			addChildren(grandchild, childNode);
		}
	}
	
	// listen to the even that the user switches between data/result nodes, or new result node is created
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<CSMicroarray> dataSet = e.getDataSet();
		if (dataSet instanceof GoAnalysisResult) {
			result = (GoAnalysisResult)dataSet;
			tableModel.populateNewResult( result );
			
			populateTreeRrepresentation();
			// clean out gene list, single gene tree, gene detail
//			populateGeneList();
//			populateSingleGeneTree();
			allButton.setSelected(true); // show all three namespace at switching result node
			repaint();
		}
	 }
	
	private void populateGeneList(int goId) {
		boolean includeDescendants = false;
		if (termButton.isSelected()) {
			includeDescendants = false;
		} else if (termAndDewscendantsButton.isSelected()) {
			includeDescendants = true;
		} else {
			log.error("'Show genes for' not set");
		}
		Set<Integer> processedTerms = new TreeSet<Integer>();
		Set<String> genes = genesFomrTermAndDescendants(processedTerms, goId, includeDescendants);
		
		if(changedGeneListButton.isSelected()) {
			genes.retainAll(result.getChangedGenes());
		} else if(referenceListButton.isSelected()) {
			genes.retainAll(result.getReferenceGenes());
		} else {
			log.error("'Show genes from' not set");
		}
		
		Object[][] dataVector = new Object[genes.size()][3];
		int i = 0;
		for(String gene: genes) {
			dataVector[i][0] = gene;
			dataVector[i][1] = "";
			dataVector[i][2] = GoAnalysisResult.getGeneDetail(gene);
			i++;
		}

		geneListTableModel.setDataVector(dataVector, geneListHeaders);
	}
	
	private Set<String> genesFomrTermAndDescendants(Set<Integer> processedTerms, int goId, boolean includeDescendants) {
		Set<String> genes = new HashSet<String>();
		Set<String> annotatedGenes = GoAnalysisResult.getAnnotatedGenes(goId);
		if(annotatedGenes!=null)
			genes.addAll(annotatedGenes);
		
		if(includeDescendants) {
			List<Integer> children = GoAnalysisResult.getOntologyChildren(goId);
			if(children!=null) {
				for(Integer child: children) {
					if(!processedTerms.contains(child)) {
						genes.addAll(genesFomrTermAndDescendants(processedTerms, child, includeDescendants));
					}
				}
			}
		}
		processedTerms.add(goId);
		return genes;
	}

	private static class GoTableModel extends AbstractTableModel {
	    /**
		 * 
		 */
		private static final long serialVersionUID = -7009237666149228067L;

		private static final int TABLE_COLUMN_INDEX_GO_ID = 0;
		private static final int TABLE_COLUMN_INDEX_GO_TERM_NAME = 1;
		private static final int TABLE_COLUMN_INDEX_NAMESPACE = 2;
		private static final int TABLE_COLUMN_INDEX_P = 3;
		private static final int TABLE_COLUMN_INDEX_ADJUSTED_P = 4;
		private static final int TABLE_COLUMN_INDEX_POP_COUNT = 5;
		private static final int TABLE_COLUMN_INDEX_STUDY_COUNT = 6;
		
		private String[] columnNames = { "GO:ID", "Name", "Namespace",
				"P-value", "Adjusted P-value", "Population Count", "Study Count" };
	    private Object[][] data = new Object[0][COLUMN_COUNT];

		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		public int getRowCount() {
			return data.length;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}
		
		public void populateNewResult(GoAnalysisResult result) {
			data = result.getResultAsArray();
			
			fireTableDataChanged();
		}
		
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex==TABLE_COLUMN_INDEX_ADJUSTED_P || columnIndex==TABLE_COLUMN_INDEX_P)
				return Double.class;
			else if(columnIndex==TABLE_COLUMN_INDEX_GO_ID || columnIndex==TABLE_COLUMN_INDEX_POP_COUNT || columnIndex==TABLE_COLUMN_INDEX_STUDY_COUNT)
				return Integer.class;
			else if(columnIndex==TABLE_COLUMN_INDEX_GO_TERM_NAME || columnIndex==TABLE_COLUMN_INDEX_NAMESPACE )
				return String.class;
			else {
				log.warn("Unspecified column type");
				return Object.class;
			}
		}
	}
	
	private static class DoubleRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = -3198188622862469457L;
		private NumberFormat formatter = null;

		public DoubleRenderer(int digits) { 
			super(); 
			StringBuffer sb = new StringBuffer("0.");
			for(int i=0; i<digits; i++)sb.append("0");
			formatter = new DecimalFormat(sb.toString());
		}

	    public void setValue(Object value) {
	        if (formatter==null) {
	            formatter = NumberFormat.getNumberInstance();
	        }
	        setText((value == null) ? "" : formatter.format(value));
	    }
	}

	private static final String[] namespaceLabels = {"Molecular Function", "Biological Process", "Cellular Component"};
	private JRadioButton termButton;
	private JRadioButton termAndDewscendantsButton;
	private JRadioButton changedGeneListButton;
	private JRadioButton referenceListButton;
	private JTable geneListTable;
	private JTextField searchId;

	private class SingleGeneTreeNode extends DefaultMutableTreeNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = -3425259236048104986L;
		protected int goId;
		
		public SingleGeneTreeNode(Integer goId) {
			super();
			this.goId = goId;
		}

		public SingleGeneTreeNode() {
			super();
		}

		public String toString() {
			if(goId==0)return "ROOT"; // this string does not matter because it is not visible 
			
			// if it is namespace, we may want to format it differently
			return GoAnalysisResult.getGoTermName(goId); 
		}
	}
	
	static private class NamespaceFilter extends RowFilter<GoTableModel, Integer> {
		private String filter;
		
		NamespaceFilter(String filter) {
			this.filter = filter;
		}

		@Override
		public boolean include(
				Entry<? extends GoTableModel, ? extends Integer> entry) {
			GoTableModel model = entry.getModel();
			String namespaceLetter = (String)model.getValueAt( entry.getIdentifier().intValue(), GoTableModel.TABLE_COLUMN_INDEX_NAMESPACE );
			
			if(filter.startsWith(namespaceLetter)) 
				return true;
			else
				return false;
		}
	}

}
