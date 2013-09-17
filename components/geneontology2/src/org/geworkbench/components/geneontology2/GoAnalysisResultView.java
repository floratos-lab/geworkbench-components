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
import java.util.HashSet;
import java.util.List;
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
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationManager;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
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
@AcceptTypes({GoAnalysisResult.class, DSMicroarraySet.class})
public class GoAnalysisResultView extends JPanel implements VisualPlugin {
	private static final long serialVersionUID = -579377200878351871L;
	static Log log = LogFactory.getLog(GoAnalysisResultView.class);
	
	private GoTermTreeModel treeModel = null;
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
					log.debug(".. start with no selection or root");
					List<GoTreeNode> startingList = new ArrayList<GoTreeNode>();
					startingList.add((GoTreeNode) treeModel.getRoot());
					treePathList = searchMatchingNode(startingList, searchText);
				} else { // find the next
					log.debug("selectedPath=="+ selectedPath);
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

		treeModel = new GoTermTreeModel();

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
		TableRowSorter<DefaultTableModel> geneTableSorter = new TableRowSorter<DefaultTableModel>(geneListTableModel);
		geneListTable.setRowSorter(geneTableSorter);

		prepareCopyToSet();
		ListSelectionModel geneListModel = geneListTable.getSelectionModel();
		geneListModel.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				if(geneListTable.getSelectedRow()==-1) return;
				
				// refresh gene detail panel
				int index = geneListTable.convertRowIndexToModel( geneListTable.getSelectedRow() );
				if(index>=0 && index<=geneListTableModel.getRowCount()) { // in case the selection is not in the new range
					showGeneDetail((String)geneListTableModel.getValueAt(index, 0));
				}
			}
			
		});
		geneListTable.addFocusListener(new FocusListener() {

			@Override
			public void focusGained(FocusEvent e) {
				// refresh gene detail panel
				int index = geneListTable.convertRowIndexToModel( geneListTable.getSelectedRow() );
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
			if(table.getSelectedRow()==-1) {
				geneListTableModel.setDataVector(null, geneListHeaders);
				singleGeneTreeRoot.removeAllChildren();
				singleGeneModel.reload();
				geneDetails.setText("");
				return;
			}
			
			int index = table.convertRowIndexToModel( table.getSelectedRow() );
			if(index>=0 && index<=tableModel.getRowCount()) { // in case the selection is not in the new range
				Integer goId = (Integer)tableModel.getValueAt(index, 0);

				populateGeneList(goId);
				populateSingleGeneTree(goId);
				showTermDetail(goId);
			}
		} else if (primaryView.getSelectedComponent()==treeTab) {
			tree.updateUI(); // trick to force repaint.
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
									GeneToMarkers(getMarkerSetName(), genes),
									org.geworkbench.events.SubpanelChangedEvent.NEW));

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

	// choose the name for the new marker set to be created
	private String getMarkerSetName() {
		if(primaryView.getSelectedComponent()==tableTab) {
			int modelIndex = table.convertRowIndexToModel(table.getSelectedRow()); 
			String goTermName = (String)tableModel.getValueAt(modelIndex, GoTableModel.TABLE_COLUMN_INDEX_GO_TERM_NAME);
			return goTermName;
		} else if (primaryView.getSelectedComponent()==treeTab) {
			GoTreeNode node = (GoTreeNode) tree.getLastSelectedPathComponent();
			if(node==null) {
				return "No Term Selected";
			}
			return node.goTerm.getName();
		} else {
			log.error("invalid selection of primaryView");
			return null;
		}
	}
	
	private DSPanel<DSGeneMarker> GeneToMarkers(String setLabel, Set<String> genes) {
		DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(
				setLabel, "Go Terms Anlaysis");
		DSMicroarraySet dataset = (DSMicroarraySet) (ProjectPanel
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

	private Set<String> getAnnotatedGenes(int goTermId) {
		if(result!=null) {
			return result.getAnnotatedGenes(goTermId);
		} else {
			return AnnotationManager.getAnnotatedGenes(dataSet, goTermId);
		}
	}
	
	private void showTermDetail(int goId) {
		geneDetails.setContentType("text/plain");
		if(getAnnotatedGenes(goId)==null) {
			geneDetails.setText("No gene annotated to GO ID "+goId);
			return;
		}
			
		StringBuffer sb = new StringBuffer("Term GO ID: "+goId+"\nGenes annotated:\n");
	
		// here are the genes annotated to this term only, not to descendants.
		for(String gene: getAnnotatedGenes(goId)) {
			sb.append(gene).append("\n   Gene title: ").append(
					AnnotationManager.getGeneDetail(dataSet, gene))
					.append("\n\n");
		}
		
		geneDetails.setText(sb.toString());
		geneDetails.setCaretPosition(0); // move the scroll pane to top if the text is long
	}
	
	protected void showGeneDetail(String geneSymbol) {
		int geneId = AnnotationManager.getEntrezId(dataSet, geneSymbol);
		geneDetails.setContentType("text/html");
		if(geneId!=-1) {
			geneDetails.setText("Details of Gene " + geneSymbol
				+ "<p>Entrez ID: "+geneId+" <a href=http://www.ncbi.nlm.nih.gov/gene/" + geneId
				+ ">Link to Entrez Gene Database</a>");
		} else {
			geneDetails.setText( geneSymbol+" is not a gene symbol" );
		}
	}
	
	/* (non-Javadoc)
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	// this is just something you must do
	public Component getComponent() {
		return this;
	}
	
	private GoAnalysisResult result = null;

	private AbstractButton allButton;
	
	private void searchNext(List<GoTreeNode> treePathList, String searchText) {
		int lastIndex = treePathList.size()-1;
		GoTreeNode node = treePathList.get(lastIndex); // check the last matching node
		if(node==treeModel.getRoot()) {  // path list is empty now: no more found
			List<GoTreeNode> foundList = searchMatchingNode(treePathList, searchText);
			if(foundList==null) // not found
				treePathList.clear();
			return; // if found, treePathList has been updated.
		}

		treePathList.remove(lastIndex); // remove the last matching node
		GoTreeNode parent = treePathList.get(treePathList.size()-1);
		for(int index = treeModel.getIndexOfChild(parent, node)+1; index <treeModel.getChildCount(parent); index++) {
			GoTreeNode sibling = (GoTreeNode)treeModel.getChild(parent, index);
			treePathList.add(sibling);
			List<GoTreeNode> list = searchMatchingNode(treePathList, searchText);
			if(list!=null) {
				// found
				return;
			} else {
				treePathList.remove(sibling);
			}
		}
		// if not in siblings
		searchNext(treePathList, searchText);
	}

	/* recursive call. terminate at either found or not children*/
	private List<GoTreeNode> searchMatchingNode(List<GoTreeNode> path, String searchText) {
		// it is important to search in model instead of tree so tree does not need to be expanded (a costly process)
		GoTreeNode node = path.get(path.size()-1);
		GoTermMatcher matcher = GoTermMatcher.createMatcher(node, searchText);
		if(matcher.match()){
			return path;
		}
		for(int i=0; i<treeModel.getChildCount(node); i++) {
			GoTreeNode child = (GoTreeNode) treeModel.getChild(node, i);
			path.add(child);
			List<GoTreeNode> foundPath = searchMatchingNode(path, searchText); 
			if(foundPath!=null) {
				return foundPath;
			} else {
				path.remove(child);
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
	
	// this is only used in the case of expression data set instead of ontology analysis result
	private DSMicroarraySet dataSet = null;
	
	// listen to the even that the user switches between data/result nodes, or new result node is created
	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<?> dataSet = e.getDataSet();
		if (dataSet instanceof GoAnalysisResult) {
			result = (GoAnalysisResult)dataSet;
			dataSet = null; // not used in case of ontology analysis result
			tableModel.populateNewResult( result );
			
			treeModel.setResult(result);
			allButton.setSelected(true); // show all three namespace at switching result node
			
			changedGeneListButton.setEnabled(true);
			referenceListButton.setEnabled(true);
			repaint();
		} else if (dataSet instanceof DSMicroarraySet) {
			result = null;
			this.dataSet = (DSMicroarraySet)dataSet;
			if(GeneOntologyTree.getInstance()==null) {
				updateFromBackground();
				return;
			}
			tableModel.populateFromDataSet( this.dataSet );
			
			treeModel.setResult(null);
			allButton.setSelected(true); // show all three namespace at switching result node
			
			changedGeneListButton.setEnabled(false);
			referenceListButton.setEnabled(false);
			repaint();
		}
	 }
	
	private void updateFromBackground() {
		final long ONE_SECOND = 1000;
		final long LIMIT = 200;
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				long count = 0;
				while (GeneOntologyTree.getInstance() == null && count < LIMIT) {
					try {
						Thread.sleep(ONE_SECOND);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					count++;
				}
				return null;
			}

			@Override
			protected void done() {
				// do similar thing to receive(ProjectEvent e, Object source)
				// but only when GeneOntologyTree is available
				tableModel.populateFromDataSet(dataSet);

				treeModel.setResult(null);
				allButton.setSelected(true);

				changedGeneListButton.setEnabled(false);
				referenceListButton.setEnabled(false);
				repaint();
			}
		};
		worker.execute();
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
		
		if (result != null) {
			if (changedGeneListButton.isSelected()) {
				genes.retainAll(result.getChangedGenes());
			} else if (referenceListButton.isSelected()) {
				genes.retainAll(result.getReferenceGenes());
			} else {
				log.error("'Show genes from' not set");
			}
		} else {
			// no op
		}
		
		Object[][] dataVector = new Object[genes.size()][3];
		int i = 0;
		for(String gene: genes) {
			dataVector[i][0] = gene;
			dataVector[i][1] = "";
			dataVector[i][2] = AnnotationManager.getGeneDetail(dataSet, gene);
			i++;
		}

		geneListTableModel.setDataVector(dataVector, geneListHeaders);
	}
	
	private Set<String> genesFomrTermAndDescendants(Set<Integer> processedTerms, int goId, boolean includeDescendants) {
		Set<String> genes = new HashSet<String>();
		Set<String> annotatedGenes = getAnnotatedGenes(goId);
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
