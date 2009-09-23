/**
 * 
 */
package org.geworkbench.components.geneontology2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
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

/**
 * @author zji
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
	private JTextArea geneDetails = null;
	private JTable table = null;
//	private JScrollPane geneDetailsScrollPane = null;
//	private JScrollBar verticalScrollBar;
	protected String namespaceFilter = null;
	
	private static Object[] geneListHeaders = new String[]{"Gene Symbol", "Expression change", "Description"};

	/**
	 * 
	 */
	public GoAnalysisResultView() {
		super();
		JPanel leftPanel = new JPanel();
		
		JTabbedPane primaryView = new JTabbedPane();
		primaryView.setMinimumSize(new Dimension(600, 500));

		JPanel geneListWindow = new JPanel();
		JPanel singelGeneView = new JPanel();
		JPanel detailPanel = new JPanel();
		
		JPanel tableTab = new JPanel();
		JPanel treeTab = new JPanel();
		primaryView.add(tableTab, "Table Browser");
		primaryView.add(treeTab, "Tree Browser");
		
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.add(primaryView);
		leftPanel.add(geneListWindow);

		JPanel rightPanel = new JPanel();
		rightPanel.setMinimumSize(new Dimension(300, 500));
		rightPanel.setLayout(new BorderLayout());
		rightPanel.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				singelGeneView, detailPanel), BorderLayout.CENTER);
		
		setLayout(new BorderLayout());
		add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                leftPanel, rightPanel), BorderLayout.CENTER);
		
		// more detail on left side
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
					table.repaint();
				} else {
					for(int i=0; i<3; i++){
						if(namespaceButton[i].isSelected()) {
							tableModel.filter(namespace[i]);
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
			namespaceButton[i] = new JRadioButton(namespace[i]);
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
		prepareSorting();
		tableTab.add(new JScrollPane(table));
		
		ListSelectionModel listSelectionModel = table.getSelectionModel();
		listSelectionModel.addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent e) {
				int index = table.getSelectedRow();
				if(index>=0 && index<=tableModel.getRowCount()) { // in case the selection is not in the new range
					Integer goId = (Integer)tableModel.getValueAt(index, 0);
					populateGeneList(goId);
	
					populateSingleGeneTree(goId);
					
					showGeneDetail(goId);
				}
			}
			
		});

		treeTab.setLayout(new BorderLayout());
		JPanel mapPanel = new JPanel();
		mapPanel.add(new JLabel("Map array genes as reference list"));
		mapPanel.add(new JButton("Map")); // TODO not implemented, pending decision on the coverage of the tree view
		treeTab.add(mapPanel, BorderLayout.NORTH);

		GoTreeNode root = new GoTreeNode (); // root
		treeModel = new DefaultTreeModel(root);
		namespaceId2Node = new HashMap<Integer, GoTreeNode>();
		for(int namespaceId: GoAnalysis.NAMESPACE_ID) {
			GoTreeNode namespaceNode = new GoTreeNode(namespaceId); 
			root.add(namespaceNode);
			namespaceId2Node.put(namespaceId, namespaceNode);
		}

		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		
		treeTab.add(new JScrollPane(tree), BorderLayout.CENTER);

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
				int index = table.getSelectedRow();
				if(index>=0 && index<=tableModel.getRowCount()) { // in case the selection is not in the new range
					Integer goId = (Integer)tableModel.getValueAt(index, 0);
					populateGeneList(goId);
				}
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
		geneListWindow.add(new JScrollPane(geneListTable));
		
		// more details on right side
		singelGeneView.setLayout(new BoxLayout(singelGeneView, BoxLayout.Y_AXIS));
		singelGeneView.add(new JLabel("Single Gene View"));
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
		geneDetails = new JTextArea();
		geneDetails.setEditable(false);
		detailPanel.add(new JScrollPane(geneDetails));
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

	protected void showGeneDetail(int goId) {
		if(GoAnalysis.term2Gene.get(goId)==null) {
			geneDetails.setText("No gene annotated to GO ID "+goId);
			return;
		}
			
		StringBuffer sb = new StringBuffer("Term GO ID: "+goId+"\nGenes annotated:\n");
	
		// here are the genes annotated to this term only, not to descendants.
//		log.debug("show gene detail for GO term "+goId);
		int i=0;
		for(String gene: GoAnalysis.term2Gene.get(goId)) {
			sb.append(gene).append("\n   Gene title: ").append(
					GoAnalysis.geneDetails.get(gene))
					// TODO .append("\n").append("... more details for the gene: link to CGAP")
					.append("\n\n");
			i++;
		}
//		log.debug(i+" genes' detail shown");
		
		geneDetails.setText(sb.toString());
		geneDetails.setCaretPosition(0); // move the scroll pane to top if the text is long
	}

	/* (non-Javadoc)
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	// this is just something you must do
	public Component getComponent() {
		return this;
	}
	
	private GoAnalysisResult result = null;

	private static Map<Integer, GoTreeNode> namespaceId2Node = null;
	private AbstractButton allButton;
	
	private void populateTreeRrepresentation() {
		/* three namespace nodes are implemented differently from regular GO terms */
		for(int namespaceId: GoAnalysis.NAMESPACE_ID) {
			GoTreeNode namespaceNode = namespaceId2Node.get(namespaceId);
			namespaceNode.removeAllChildren();
			for(Integer child: GoAnalysis.ontologyChild.get(namespaceId)) {
				addChildren(child, namespaceNode);
			}
		}
	}
	
	// this does the similar thing as populateTreeRrepresentation with slightly different approach.
	private void populateSingleGeneTree(int geneId) {
		singleGeneTreeRoot.removeAllChildren();

		for(int namespaceId: GoAnalysis.NAMESPACE_ID) {
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

		List<Integer> grandchildren = GoAnalysis.ontologyChild.get(childId);
		if(grandchildren==null) return found;
		
		for(Integer grandchild: grandchildren) {
			boolean foundInSubtree = findAndAddChildren(targetGene, grandchild, childNode);
			if(foundInSubtree)found = true;
		}
		if(found)
			parent.add(childNode);

		return found;
	}
	
	private void addChildren(Integer childId, GoTreeNode parent) {
		GoTreeNode childNode = new GoTreeNode(childId, parent); 
		parent.add(childNode);

		List<Integer> grandchildren = GoAnalysis.ontologyChild.get(childId);
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
		Set<String> genes = genesFomrTermAndDescendants(goId, includeDescendants);
		
		if(changedGeneListButton.isSelected()) {
			genes.retainAll(GoAnalysis.changedGenes);
		} else if(referenceListButton.isSelected()) {
			genes.retainAll(GoAnalysis.referenceGenes);
		} else {
			log.error("'Show genes from' not set");
		}
		
		Object[][] dataVector = new Object[genes.size()][3];
		int i = 0;
		for(String gene: genes) {
			dataVector[i][0] = gene;
			dataVector[i][1] = "";
			dataVector[i][2] = GoAnalysis.geneDetails.get(gene);
			i++;
		}

		geneListTableModel.setDataVector(dataVector, geneListHeaders);
	}
	
	private Set<String> genesFomrTermAndDescendants(int goId, boolean includeDescendants) {
		Set<String> genes = new HashSet<String>();
		Set<String> annotatedGenes = GoAnalysis.term2Gene.get(goId);
		if(annotatedGenes!=null)
			genes.addAll(annotatedGenes);
		
		if(includeDescendants) {
			List<Integer> children = GoAnalysis.ontologyChild.get(goId);
			if(children!=null) {
				for(Integer child: children) {
					genes.addAll(genesFomrTermAndDescendants(child, includeDescendants));
				}
			}
		}
		return genes;
	}
	
	/* set the sorting functionality */
	private void prepareSorting() {
		JTableHeader header = table.getTableHeader();
		header.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel colModel = table.getColumnModel();
				int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
				int modelIndex = colModel.getColumn(columnModelIndex)
						.getModelIndex();

				if (modelIndex < 0)
					return;

				log.debug("sorting on column #" + modelIndex);

				if (sortCol == modelIndex)
					isSortAsc = !isSortAsc;
				else
					sortCol = modelIndex;

				tableModel.sort(sortCol, isSortAsc);

				table.repaint();
			}

		});
	}

	private int sortCol = 0;
	private boolean isSortAsc = true;

	private static class GoTableModel extends AbstractTableModel {
	    /**
		 * 
		 */
		private static final long serialVersionUID = -7009237666149228067L;
		
		private String[] columnNames = { "GO:ID", "Name", "Namespace",
				"P-value", "Adjusted P-value", "Population Count", "Study Count" };
	    private Object[][] data = new Object[0][COLUMN_COUNT];

		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		public void sort(int sortCol, boolean isSortAsc) {
			List<Object[]> rows = new ArrayList<Object[]>();
			for(int row=0; row<getRowCount(); row++) {
				rows.add(data[row]);
			}
			Collections.sort(rows, new GoAnalysisComparator(sortCol, isSortAsc));
			int row = 0;
			for(Object[] rowData: rows) {
				data[row++] = rowData;
			}
			fireTableDataChanged();
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
		
		private static NumberFormat nf = new DecimalFormat("0.0000");

		public void populateNewResult(GoAnalysisResult result) {
			int rowCount = result.getCount();
			data = new Object[rowCount][COLUMN_COUNT];
			
			int row = 0;
			for(Integer goId: result.getResult().keySet()) {
				GoAnalysisResult.ResultRow resultRow = result.getRow(goId);
//				log.trace("GO:"+goId+"|"+resultRow.toString());
				data[row][0] = goId;
				data[row][1] = resultRow.name;
				data[row][2] = resultRow.namespace;
				data[row][3] = nf.format( resultRow.p );
				data[row][4] = nf.format(resultRow.pAdjusted );
				data[row][5] = resultRow.popCount;
				data[row][6] = resultRow.studyCount;
				row++;
			}
			log.debug("total rows: "+rowCount);
			fireTableDataChanged();

		}
		
		// filter is supported by java 6. this solution is only to support the functionality until we move to java 6. 
		void filter(String filter) {
			if(filter==null)return;
			
			List<Object[]> filteredData = new ArrayList<Object[]>();
			int rowCount = 0;
			for(int row=0; row<getRowCount(); row++) {
				String namespaceLetter = (String)getValueAt(row, 2);
				if(filter.startsWith(namespaceLetter)) {
					filteredData.add(data[row]);
					rowCount++;
				}
			}
			data = filteredData.toArray(new Object[rowCount][COLUMN_COUNT]);
			fireTableDataChanged();
		}

	}

	private static class GoAnalysisComparator implements Comparator<Object[]> {
		private int sortCol;;
		protected boolean isSortAsc;

		public GoAnalysisComparator(int sortCol, boolean isSortAsc) {
			this.sortCol = sortCol;
			this.isSortAsc = isSortAsc;
		}

		public int compare(Object[] o1, Object[] o2) {
			int result = 0;
			Object object1 = o1[sortCol];
			Object object2 = o2[sortCol];
			if(sortCol==0 || sortCol==5 || sortCol==6) {
				Integer s1 = (Integer) object1;
				Integer s2 = (Integer) object2;
				result = s1.compareTo(s2);
			} else if(sortCol==1 || sortCol==2) {
				String s1 = (String) object1;
				String s2 = (String) object2;
				result = s1.compareTo(s2);
			} else if(sortCol==3 || sortCol==4) {
				Double s1 = (Double) object1;
				Double s2 = (Double) object2;
				result = s1.compareTo(s2);
			}
			if (!isSortAsc)
				result = -result;
			return result;
		}
	}
	
	private static String[] namespace = {"Molecular Function", "Biological Process", "Cellular Component"};
	private JRadioButton termButton;
	private JRadioButton termAndDewscendantsButton;
	private JRadioButton changedGeneListButton;
	private JRadioButton referenceListButton;
	private JTable geneListTable;

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
			else if (goId<0) {
				return namespace[-goId-1]; // valid 'goId' here are -1, -2, -3  <=> valid index are 0, 1, 2
			}
			
			return GoAnalysis.termDetail.get(goId).name; 
		}
	}

	private class GoTreeNode implements TreeNode {
		protected int goId; // because memory efficiency is a real concern here, we only include GO ID in the node implementation
		private GoTreeNode parent;
		private List<GoTreeNode> children;
		
		
		GoTreeNode(int goId, GoTreeNode parent) {
			this.goId = goId;
			this.parent = parent;
			this.children = null;
		}

	    public void removeAllChildren() {
	    	children = null;
        }

		/* constructor only for root*/
		GoTreeNode() {
			this.goId = 0;
		}

		/* constructor only for three namespace. they don't have a real GO ID*/
		/* this technique avoids manipulate the real GO ID's */
		GoTreeNode(int namespaceId) {
			this.goId = namespaceId;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if(goId==0)return "ROOT"; // this string does not matter because it is not visible 
			else if (goId<0) {
				return namespace[-goId-1]; // valid 'goId' here are -1, -2, -3  <=> valid index are 0, 1, 2
			}
			
			GoAnalysisResult.ResultRow row = GoAnalysisResultView.this.result.getRow(goId);
			if(row==null) {// not in the result 
				return GoAnalysis.termDetail.get(goId).name; 
			} else { // in the result from ontologizer
				return row.name+" ("+row.studyCount+"/"+row.popCount+") ("+row.pAdjusted+")";
			}
		}
		
		public void add(GoTreeNode goTreeNode) {
			if(children==null) {
				children = new ArrayList<GoTreeNode>();
			}
			children.add(goTreeNode);
			
		}

		public Enumeration<GoTreeNode> children() {
			Vector<GoTreeNode> v = new Vector<GoTreeNode>(children);
			return v.elements();
		}

		public boolean getAllowsChildren() {
			return true;
		}

		public TreeNode getChildAt(int childIndex) {
			if(children==null)return null;
			return children.get(childIndex);
		}

		public int getChildCount() {
			if(children==null)return 0;
			return children.size();
		}

		public int getIndex(TreeNode node) {
			if(children==null)return -1;
			return children.indexOf(node); // return -1 if not contain
		}

		public TreeNode getParent() {
			return parent;
		}

		public boolean isLeaf() {
			if(getChildCount()==0)return true;
			else return false;
		}
	}
}
