package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.CellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.rpc.ServiceException;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.BasicClientConfig;
import org.apache.commons.collections15.set.ListOrderedSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import org.geworkbench.builtin.projects.ProjectPanel;


/**
 * @author manjunath at genomecenter dot columbia dot edu, xiaoqing zhang
 */
@AcceptTypes( { DSMicroarraySet.class })
public class CellularNetworkKnowledgeWidget extends javax.swing.JScrollPane
		implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	private static String GOTERMCOLUMN = "GO Description";

	private static String INCLUDEPDLABEL = "Include Protein-DNA";

	private static final String ACTIVETABLE = "ACTIVETABLE";

	private static final String DETAILTABLE = "DETAILTABLE";

	private static String INCLUDEPPLABEL = "Include Protein-Protein";

	private static String MARKERLABEL = "Marker";

	private static String GENELABEL = "Gene";

	private static String GENETYPELABEL = "Gene Type";

	private static String PDNUMBERLABEL = "# of Protein-DNA Interactions";

	private static String PPNUMBERLABEL = "# of Protein-Protein Interactions";

	private static String[] columnLabels = new String[] { INCLUDEPDLABEL,
			INCLUDEPPLABEL, MARKERLABEL, GENELABEL, GENETYPELABEL,
			GOTERMCOLUMN, PDNUMBERLABEL, PPNUMBERLABEL };

	private static TableColumn[] tableColumns;

	private boolean cancelAction = false;

	// public static final int THRESHOLD = 5000;

	/**
	 * Creates new form Interactions
	 */
	public CellularNetworkKnowledgeWidget() {
		initComponents();
		initConnections();
		ppInteractions.setSelected(true);
		ppInteractions.setToolTipText("Include Protein-Protein Interactions");
		pdInteractions.setSelected(true);
		pdInteractions.setEnabled(true);
		pdInteractions.setToolTipText("Include Protein-DNA Interactions");
		// activatedMarkerTable.getTableHeader().setDefaultRenderer(activeMarkersTableModel);
		activatedMarkerTable.getTableHeader().setEnabled(true);
		// setting the size of the table and its columns
		activatedMarkerTable.setPreferredScrollableViewportSize(new Dimension(
				280, 100));
		activatedMarkerTable.getColumnModel().getColumn(0).setPreferredWidth(
				200);
		activatedMarkerTable.getColumnModel().getColumn(1)
				.setPreferredWidth(50);
		activatedMarkerTable.getColumnModel().getColumn(2)
				.setPreferredWidth(30);

		// detailTable.getTableHeader().setDefaultRenderer(tableHeaderRenderer);
		detailTable.getTableHeader().setEnabled(true);
		detailTable.setDefaultRenderer(String.class, new ColorRenderer(true));
		detailTable
				.setDefaultRenderer(Integer.class, new IntegerRenderer(true));
	}

	public Component getComponent() {
		return this;
	}

	private void cancelCellEditing() {
		CellEditor ce = detailTable.getCellEditor();
		if (ce != null) {
			ce.cancelCellEditing();
		}
	}

	/**
	 * Create a popup menu to display thge name of different Go Terms in 3
	 * catagories.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	private JPopupMenu createPopMenu(final int rowIndex, final int columnIndex,
			final int[] selectedRows, final String tableName) {
		JPopupMenu contextMenu = new JPopupMenu();
		JMenuItem removeMutipleItemMenu = new JMenuItem();
		JMenuItem removeAllItemsMenu = new JMenuItem();
		if (tableName.equalsIgnoreCase(ACTIVETABLE)) {
			removeMutipleItemMenu
					.setText("Add Selected Markers to Selected Markers List");
			removeAllItemsMenu
					.setText("Add All Markers to Selected Markers List");
		} else {
			removeMutipleItemMenu
					.setText("Move Selected Markers back to Activated Markers List");
			removeAllItemsMenu
					.setText("Move All Markers back to Activated Markers List");
		}
		removeMutipleItemMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeMutipleRows(selectedRows, tableName);

			}

		});
		removeAllItemsMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeAllRows(tableName);

			}

		});
		contextMenu.add(removeMutipleItemMenu);
		contextMenu.add(removeAllItemsMenu);

		return contextMenu;
	}

	protected void removeAllRows(String tableName) {
		if (ACTIVETABLE.equalsIgnoreCase(tableName)) {
			for (DSGeneMarker marker : allGenes) {
				if (marker != null) {
					// !hits.contains(new
					// CellularNetWorkElementInformation(marker)))
					CellularNetWorkElementInformation cellularNetWorkElementInformation = new CellularNetWorkElementInformation(
							marker);
					boolean newEntry = true;
					for (CellularNetWorkElementInformation cell : hits) {
						if (cell.equals(cellularNetWorkElementInformation)) {
							newEntry = false;
							break;
						}
					}
					if (newEntry) {
						hits.addElement(new CellularNetWorkElementInformation(
								marker));
					}
				}
			}
			allGenes.removeAllElements();
		} else {

			for (CellularNetWorkElementInformation marker : hits) {
				if (marker != null
						&& !allGenes.contains(marker.getdSGeneMarker())) {
					allGenes.add(marker.getdSGeneMarker());

				}
			}
			hits.removeAllElements();
		}

		activatedMarkerTable.revalidate();
		detailTable.revalidate();
	}

	protected void removeMutipleRows(int[] selectedRows, String tableName) {
		if (ACTIVETABLE.equals(tableName)) {
			for (int i = selectedRows.length - 1; i >= 0; i--) {
				int row = selectedRows[i];
				if (row < allGenes.size()) {
					DSGeneMarker marker = allGenes.get(row);
					if (marker != null) {
						// !hits.contains(new
						// CellularNetWorkElementInformation(marker)))
						CellularNetWorkElementInformation cellularNetWorkElementInformation = new CellularNetWorkElementInformation(
								marker);
						boolean newEntry = true;
						allGenes.remove(row);
						for (CellularNetWorkElementInformation cell : hits) {
							if (cell.equals(cellularNetWorkElementInformation)) {
								newEntry = false;
								break;
							}
						}
						if (newEntry) {
							hits
									.addElement(new CellularNetWorkElementInformation(
											marker));
						}

					}
				}

			}
		} else {
			for (int i = selectedRows.length - 1; i >= 0; i--) {
				int row = selectedRows[i];
				if (row < hits.size()) {
					CellularNetWorkElementInformation marker = hits.get(row);
					hits.remove(row);
					if (marker != null
							&& !allGenes.contains(marker.getdSGeneMarker())) {
						allGenes.add(marker.getdSGeneMarker());

					}
				}
			}
		}
		activatedMarkerTable.revalidate();
		detailTable.revalidate();
	}

	/**
	 * Create a popup menu to display thge name of different Go Terms in 3
	 * catagories.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	private JPopupMenu createContextMenu(final int rowIndex,
			final int columnIndex) {
		JPopupMenu contextMenu = new JPopupMenu();
		final CellularNetWorkElementInformation hit = hits.get(rowIndex);
		JMenu componentMenu = new JMenu();
		componentMenu.setText("Component");
		JMenu goFunctionMenu = new JMenu();
		goFunctionMenu.setText("Function");
		JMenu processMenu = new JMenu();
		processMenu.setText("Process");
		if (hit.getTreeMapForComponent() != null
				&& hit.getTreeMapForComponent().size() > 0) {
			addGoTermMenuItem(componentMenu, hit.getTreeMapForComponent());
			contextMenu.add(componentMenu);
		}
		if (hit.getTreeMapForFunction() != null
				&& hit.getTreeMapForFunction().size() > 0) {
			addGoTermMenuItem(goFunctionMenu, hit.getTreeMapForFunction());
			contextMenu.add(goFunctionMenu);
		}
		if (hit.getTreeMapForProcess() != null
				&& hit.getTreeMapForProcess().size() > 0) {
			addGoTermMenuItem(processMenu, hit.getTreeMapForProcess());
			contextMenu.add(processMenu);
		}

		return contextMenu;
	}

	/**
	 * @param treeMap
	 * @return
	 */
	private JPopupMenu displayGoTermItemMenu(
			TreeMap<String, Set<GOTerm>> treeMap) {
		JPopupMenu contextMenu = new JPopupMenu();
		if (treeMap != null && treeMap.size() > 0) {
			Object[] array = treeMap.keySet().toArray();
			JMenuItem[] jMenuItems = new JMenuItem[array.length];
			for (int i = 0; i < array.length; i++) {
				jMenuItems[i] = new JMenuItem((String) array[i]);
				contextMenu.add(jMenuItems[i]);
			}
		}
		return contextMenu;
	}

	/**
	 * Display a tree for a specific go term.
	 * 
	 * @param jMenu
	 * @param treeMap
	 */
	private void addGoTermMenuItem(JMenu jMenu,
			TreeMap<String, List<GOTerm>> treeMap) {

		if (treeMap != null && treeMap.size() > 0) {
			Object[] array = treeMap.keySet().toArray();
			JMenuItem[] jMenuItems = new JMenuItem[array.length];
			for (int i = 0; i < array.length; i++) {
				jMenuItems[i] = new JMenuItem((String) array[i]);
				jMenu.add(jMenuItems[i]);
				final List<GOTerm> set = treeMap.get(array[i]);
				jMenuItems[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						displayGoTree(set);
					}

				});
			}
		}

	}

	/**
	 * Create a TreeNode from the Go Term data.
	 * 
	 * @param set
	 * @return
	 */
	private DefaultMutableTreeNode createNodes(List<GOTerm> set) {
		Object[] array = set.toArray();
		DefaultMutableTreeNode node = null;
		if (array != null && array.length > 0) {
			node = new DefaultMutableTreeNode(array[array.length - 1]);
			DefaultMutableTreeNode[] childnode = new DefaultMutableTreeNode[array.length - 1];
			if (array.length > 1) {
				childnode[array.length - 2] = new DefaultMutableTreeNode(
						array[array.length - 2]);
				node.add(childnode[array.length - 2]);

				for (int i = array.length - 3; i >= 0; i--) {
					childnode[i] = new DefaultMutableTreeNode(array[i]);
					childnode[i + 1].add(childnode[i]);
				}
			}
		}
		return node;
	}

	/**
	 * Display Go Tree.
	 * 
	 * @param set
	 */
	private void displayGoTree(List<GOTerm> set) {
		if (set == null || set.size() == 0) {
			return;
		}
		Frame frame = JOptionPane.getFrameForComponent(this);
		goDialog = new JDialog(frame, "Display Gene Ontology Tree", true);

		// Create a tree that allows one selection at a time.
		JTree tree = new JTree(createNodes(set));
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(set.get(set
				.size() - 1));
		// Make sure the last node is selected.
		tree.scrollPathToVisible(new TreePath(node));
		tree.setSelectionPath(new TreePath(node.getPath()));

		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(tree);
		goDialog.getContentPane().add(treeView);
		goDialog.setMinimumSize(new Dimension(100, 100));
		goDialog.setPreferredSize(new Dimension(300, 300));
		goDialog.pack();
		goDialog.setLocationRelativeTo(null);
		goDialog.setVisible(true);
	}

	/**
	 * The old methold to create the GUI. It was generated by IDE than edited
	 * manually.
	 */

	private void initComponents() {
		new javax.swing.JPanel();
		jPanel2 = new javax.swing.JPanel();
		JPanel topPanel = new JPanel();
		throttlePanel = new JPanel();
		jLabel2 = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		allGeneList = new javax.swing.JList();
		new javax.swing.JScrollPane();
		selectedGenesList = new javax.swing.JList();
		addButton = new javax.swing.JButton();
		removeButton = new javax.swing.JButton();
		refreshButton = new javax.swing.JButton();
		displayPreferenceButton = new JButton("Display Preference");
		jPanel1 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jScrollPane4 = new javax.swing.JScrollPane();
		detailTable = new javax.swing.JTable();
		jLabel1 = new javax.swing.JLabel();
		createNetWorkButton = new javax.swing.JButton();
		topPane = new JSplitPane();
		upPanel = new JSplitPane();
		activatedMarkerTable = new JTable();
		commandToolBar = new JToolBar();
		progressDisplayBar = new JToolBar();

		graphToolBar = new JToolBar();
		thresholdLabel = new JLabel("Threshold");
		thresholdTextField = new JTextField(".00", 4);
		thresholdTextField.setMaximumSize(new Dimension(20, 20));
		thresholdTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jThresholdTextField_actionPerformed();
			}
		});
		thresholdTextField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				jThresholdTextField_actionPerformed();

			}
		});

		thresholdSlider = new JSlider();
		thresholdSlider.setValue(0);
		thresholdSlider.setMinimum(0);
		thresholdSlider.setMaximum(100);
		thresholdSlider.setSnapToTicks(true);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setMinorTickSpacing(1);
		thresholdSlider.setMajorTickSpacing(5);
		thresholdSlider.setCursor(java.awt.Cursor
				.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		thresholdSlider
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						thresholdSlider_stateChanged(e);
					}
				});
		thresholdSlider
				.setToolTipText("Move the slider to change the threshold of org.geworkbench.components.interactions.cellularnetwork");

		graphToolBar.add(thresholdLabel);
		graphToolBar.add(Box.createHorizontalStrut(10));
		graphToolBar.add(thresholdTextField);
		graphToolBar.add(thresholdSlider);
		cancelButton = new JButton();
		allProteinCheckbox = new JCheckBox("All Protein-Protein");
		allProteinCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jProteinCheckBox_actionPerformed(e);
			}
		});
		displayPreferenceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				displayPreferenceButton_actionPerformed(e);
			}
		});
		allProteinDNACheckbox = new JCheckBox("All Protein-DNA");
		allProteinDNACheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				jProteinCheckBox_actionPerformed(e);
			}
		});
		jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(
				204, 204, 255)));
		// jPanel2.setMaximumSize(new Dimension(200, 80));
		jPanel2.setMinimumSize(new Dimension(230, 80));
		jPanel2.setPreferredSize(new Dimension(230, 80));
		throttlePanel.setBorder(javax.swing.BorderFactory
				.createLineBorder(new Color(204, 204, 255)));
		throttlePanel.setMinimumSize(new Dimension(230, 100));
		throttlePanel.setPreferredSize(new Dimension(230, 300));
		jLabel2.setText("Obtain Interactions for Gene(s):");

		allGeneList.setToolTipText("Available Genes");
		allGeneList.setModel(allGeneModel);
		allGeneList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				allGeneListHandler(evt);
			}
		});

		jScrollPane1.setViewportView(allGeneList);

		selectedGenesList.setToolTipText("Selected Genes");
		selectedGenesList.setModel(selectedGenesModel);
		selectedGenesList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				selectedGenesListHandler(evt);
			}
		});

		// jScrollPane2.setViewportView(selectedGenesList);

		addButton.setText(">>");
		addButton.setToolTipText("Add to selection");
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addButtonHandler(evt);
			}
		});

		removeButton.setText("<<");
		removeButton.setToolTipText("Remove From Selection");
		removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				removeButtonHandler(evt);
			}
		});

		refreshButton.setText("Refresh");
		refreshButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				previewSelectionsHandler(evt);
			}
		});

		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelTheAction(evt);
			}
		});

		jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(
				204, 204, 255)));
		// jPanel1.setMaximumSize(new Dimension(587, 382));
		jPanel1.setMinimumSize(new Dimension(300, 50));
		jPanel1.setPreferredSize(new Dimension(587, 182));

		detailTable.setModel(previewTableModel);
		detailTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if (row < hits.size()) {
						CellularNetWorkElementInformation marker = hits
								.get(row);
						hits.remove(row);
						if (marker != null
								&& !allGenes.contains(marker.getdSGeneMarker())) {
							allGenes.add(marker.getdSGeneMarker());

						}
						activatedMarkerTable.revalidate();
						detailTable.revalidate();
					}
				}
			}

			private void maybeShowPopup(MouseEvent e) {

				if (e.isPopupTrigger() && detailTable.isEnabled()) {
					Point p = new Point(e.getX(), e.getY());
					int col = detailTable.columnAtPoint(p);
					int row = detailTable.rowAtPoint(p);

					// translate table index to model index
					int mcol = detailTable.getColumn(
							detailTable.getColumnName(col)).getModelIndex();

					if (row >= 0 && row < detailTable.getRowCount()) {
						cancelCellEditing();
						if (detailTable.getColumnName(col).equalsIgnoreCase(
								GOTERMCOLUMN)) {
							// display popupmenu for go term
							JPopupMenu contextMenu = createContextMenu(row,
									mcol);
							if (contextMenu != null
									&& contextMenu.getComponentCount() > 0) {
								contextMenu.show(detailTable, p.x, p.y);
							}
						} else {
							int[] selectedRows = detailTable.getSelectedRows();
							// display popup menu for row editing.
							JPopupMenu contextMenu = createPopMenu(row, mcol,
									selectedRows, DETAILTABLE);
							if (contextMenu != null
									&& contextMenu.getComponentCount() > 0) {
								contextMenu.show(detailTable, p.x, p.y);
							}
						}
					}
				}
			}

			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

		});
		activatedMarkerTable.setModel(activeMarkersTableModel);
		activatedMarkerTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				boolean isDirty = true;
				if (e.getClickCount() == 2) {
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if (row < allGenes.size()) {
						DSGeneMarker marker = allGenes.get(row);
						if (marker != null) {
							// !hits.contains(new
							// CellularNetWorkElementInformation(marker)))
							CellularNetWorkElementInformation cellularNetWorkElementInformation = new CellularNetWorkElementInformation(
									marker);
							boolean newEntry = true;
							allGenes.remove(row);
							for (CellularNetWorkElementInformation cell : hits) {
								if (cell
										.equals(cellularNetWorkElementInformation)) {
									newEntry = false;
									break;
								}
							}
							if (newEntry) {
								hits
										.addElement(new CellularNetWorkElementInformation(
												marker));
							}
							activatedMarkerTable.revalidate();
							detailTable.revalidate();
						}
					}
				}
			}

			private void maybeShowPopup(MouseEvent e) {

				if (e.isPopupTrigger() && activatedMarkerTable.isEnabled()) {
					Point p = new Point(e.getX(), e.getY());
					int col = activatedMarkerTable.columnAtPoint(p);
					int row = activatedMarkerTable.rowAtPoint(p);

					// translate table index to model index
					int mcol = activatedMarkerTable.getColumn(
							activatedMarkerTable.getColumnName(col))
							.getModelIndex();
					int[] selectedRows = activatedMarkerTable.getSelectedRows();
					if (row >= 0 && row < activatedMarkerTable.getRowCount()
							&& selectedRows != null) {
						cancelCellEditing();

						// create popup menu...
						JPopupMenu contextMenu = createPopMenu(row, mcol,
								selectedRows, ACTIVETABLE);
						contextMenu.show(activatedMarkerTable, p.x, p.y);

					}
				}
			}

			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

		});

		jScrollPane3.setViewportView(detailTable);
		jScrollPane4.setViewportView(activatedMarkerTable);
		jLabel1
				.setText("<html><font color=blue><B>Selected Marker List: </b></font></html>");
		jLabel1.setMaximumSize(new Dimension(90, 40));
		createNetWorkButton.setText("Create Network");
		createNetWorkButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						createNetworks(evt);
					}
				});

		jPanel1.setLayout(new BorderLayout());
		progressDisplayBar.add(jLabel1);
		jProgressBar1.setForeground(Color.green);
		jProgressBar1.setMinimumSize(new Dimension(10, 16));
		jProgressBar1.setBorderPainted(true);
		jProgressBar1.setMaximum(100);
		jProgressBar1.setMinimum(0);
		progressDisplayBar.add(jProgressBar1);
		progressDisplayBar.add(displayPreferenceButton);
		jPanel1.add(progressDisplayBar, BorderLayout.NORTH);
		jPanel1.add(jScrollPane3, BorderLayout.CENTER);

		// jPanel1.add(commandToolBar, BorderLayout.SOUTH);

		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(upPanel, BorderLayout.CENTER);
		topPanel.add(commandToolBar, BorderLayout.SOUTH);
		this.getViewport().add(topPanel);
		upPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		activeMarkersLabel = new JLabel(
				"<html><font color=blue><B>Activated Marker List: </b></font></html>");
		jPanel2.setLayout(new BorderLayout());
		jPanel2.add(activeMarkersLabel, BorderLayout.NORTH);
		jPanel2.add(jScrollPane4, BorderLayout.CENTER);
		topPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		topPane.add(jPanel2, JSplitPane.LEFT);
		topPane.add(throttlePanel, JSplitPane.RIGHT);
		chart = ChartFactory.createXYLineChart("Throttle Graph", "likelihood",
				"# interactions", null, PlotOrientation.VERTICAL, true, true,
				true); // Title, X-Axis label, Y-Axis label, Dataset, Show
		// legend, show ToolTips

		graph = new ChartPanel(chart, true);
		XYPlot newPlot = (XYPlot) chart.getPlot();
		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) newPlot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		throttlePanel.setLayout(new BorderLayout());
		throttlePanel.add(graph, BorderLayout.CENTER);
		upPanel.add(topPane, JSplitPane.TOP);
		upPanel.add(jPanel1, JSplitPane.BOTTOM);
		upPanel.setOneTouchExpandable(true);
		throttlePanel.add(graphToolBar, BorderLayout.SOUTH);
		commandToolBar.add(allProteinDNACheckbox);
		commandToolBar.add(allProteinCheckbox);
		commandToolBar.add(refreshButton);
		commandToolBar.add(createNetWorkButton);
		cancelButton.setText("Cancel");
		commandToolBar.add(cancelButton);
		hits = new Vector<CellularNetWorkElementInformation>();

		mainDialogPanel.setLayout(borderLayout1);
		tableColumns = new TableColumn[columnLabels.length];
		TableColumnModel model = detailTable.getColumnModel();
		for (int i = 0; i < jCheckBoxes.length; i++) {
			jCheckBoxes[i] = new JCheckBox(columnLabels[i]);
			jCheckBoxes[i].setSelected(true);
			tableColumns[i] = model.getColumn(i);
		}

		jCenterPanel.setLayout(gridLayout2);
		gridLayout2.setColumns(2);
		gridLayout2.setRows(4);
		jButton1.setText("Ok");
		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePreferenceButton_actionPerformed(e);
			}
		});

		jSouthPanel.add(jButton1);
		for (int i = 0; i < jCheckBoxes.length; i++) {
			jCheckBoxes[i].setPreferredSize(new Dimension(60, 22));
			jCenterPanel.add(jCheckBoxes[i], null);
		}

		mainDialogPanel.add(jCenterPanel, java.awt.BorderLayout.CENTER);
		mainDialogPanel.add(jSouthPanel, java.awt.BorderLayout.SOUTH);
		Frame frame = JOptionPane.getFrameForComponent(this);
		dialog = new JDialog(frame, "Display Preference", false);
		dialog.getContentPane().add(mainDialogPanel);
		dialog.setMinimumSize(new Dimension(100, 100));
		dialog.setPreferredSize(new Dimension(300, 300));
		dialog.pack();
		dialog.setLocationRelativeTo(null);

	}// </editor-fold>

	/**
	 * Generate the data to draw the curve.
	 * 
	 * @param min
	 * @param max
	 * @param selectedId
	 * @param active
	 * @return
	 */

	public XYSeriesCollection createCollection(double min, double max,
			int selectedId, boolean active) {
		int binSize = CellularNetWorkElementInformation.getBinNumber();
		XYSeriesCollection plots = new XYSeriesCollection();
		try {
			XYSeries dataSeries = new XYSeries("Total Distribution");
			XYSeries ppDataSeries = new XYSeries("Protein-Protein");
			XYSeries pdDataSeries = new XYSeries("Protein-DNA");
			int[] basketValues = new int[binSize];
			int[] pdBasketValues = new int[binSize];
			int[] ppBasketValues = new int[binSize];
			for (int i = 0; i < binSize; i++) {
				basketValues[i] = 0;
			}
			if (hits != null && hits.size() > 0) {
				for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
					int[] distributionArray = cellularNetWorkElementInformation
							.getDistribution();
					int[] pdDistributionArray = cellularNetWorkElementInformation
							.getPdDistribution();
					int[] ppDistributionArray = cellularNetWorkElementInformation
							.getPpDistribution();

					for (int i = 0; i < binSize; i++) {
						basketValues[i] += distributionArray[i];
						pdBasketValues[i] += pdDistributionArray[i];
						ppBasketValues[i] += ppDistributionArray[i];
					}

				}
			}

			if (basketValues == null) {
				return null;
			}
			for (int i = 0; i < binSize; i++) {
				dataSeries.add(i
						* CellularNetWorkElementInformation
								.getSmallestIncrement(), basketValues[i]);
				pdDataSeries.add(i
						* CellularNetWorkElementInformation
								.getSmallestIncrement(), pdBasketValues[i]);
				ppDataSeries.add(i
						* CellularNetWorkElementInformation
								.getSmallestIncrement(), ppBasketValues[i]);
			}

			plots.addSeries(dataSeries);
			plots.addSeries(pdDataSeries);
			plots.addSeries(ppDataSeries);

		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return plots;
	}

	/**
	 * Respond to the select/unselect of Protein Protein interaction checkbox.
	 * 
	 * @param e
	 */
	public void jProteinCheckBox_actionPerformed(ActionEvent e) {
		if (hits != null && hits.size() > 0) {
			if (((JCheckBox) (e.getSource())).getText().equalsIgnoreCase(
					"All Protein-Protein")) {
				for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
					cellularNetWorkElementInformation
							.setIncludePPInteraction(allProteinCheckbox
									.isSelected());
				}

			} else {
				for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
					cellularNetWorkElementInformation
							.setIncludePDInteraction(allProteinDNACheckbox
									.isSelected());
				}
			}
			previewTableModel.fireTableDataChanged();
			repaint();
		}

	}

	/**
	 * Respond to the change of the Threshold Text field.
	 * 
	 * @param
	 */
	public void jThresholdTextField_actionPerformed() {
		double newvalue = 0;
		try {
			newvalue = new Double(thresholdTextField.getText().trim());
		} catch (NumberFormatException e1) {
			JOptionPane.showMessageDialog(null, "The input is not a number.",
					"Please check your input.", JOptionPane.ERROR_MESSAGE);
			return;
		}
		XYPlot plot = this.chart.getXYPlot();
		double newSliderValue = newvalue
				* (CellularNetWorkElementInformation.getBinNumber() - 1);
		thresholdSlider.setValue((int) newSliderValue);
		plot.setDomainCrosshairValue(newvalue);
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			cellularNetWorkElementInformation.setThreshold(newvalue);
		}
		previewTableModel.fireTableDataChanged();
		detailTable.revalidate();
	}

	/**
	 * Respond to display the table perference. Make the selection dialog
	 * visible.
	 * 
	 * @param e
	 */
	public void displayPreferenceButton_actionPerformed(ActionEvent e) {
		Frame frame = JOptionPane.getFrameForComponent(this);

		dialog.setVisible(true);

	}

	/**
	 * @param e
	 */
	public void updatePreferenceButton_actionPerformed(ActionEvent e) {

		dialog.setVisible(false);
		TableColumnModel model = detailTable.getColumnModel();

		for (int i = 0; i < jCheckBoxes.length; i++) {
			model.removeColumn(tableColumns[i]);
			boolean item = jCheckBoxes[i].isSelected();
			if (jCheckBoxes[i].isSelected()) {
				model.addColumn(tableColumns[i]);
			}
			// System.out.println(tableColumns.length + "model size" +
			// model.getColumnCount());
		}

		detailTable.tableChanged(new TableModelEvent(previewTableModel));
		detailTable.repaint();

	}

	/**
	 * Create the plot for the throttle graph.
	 * 
	 * @param plots
	 * @param title
	 */
	public void drawPlot(final XYSeriesCollection plots, String title) {
		if (plots == null) {
			return;
		}
		boolean isToolTipEnabled = true;

		chart = ChartFactory.createXYLineChart(title, "likelihood",
				"#interactions", plots, PlotOrientation.VERTICAL, true, true,
				true);
		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		XYPlot newPlot = (XYPlot) chart.getPlot();
		Color c = UIManager.getColor("Panel.background");
		if (c != null) {
			newPlot.setBackgroundPaint(c);
		} else {
			c = Color.white;
		}
		newPlot.setBackgroundPaint(c);
		newPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		newPlot.setDomainGridlinePaint(Color.white);
		newPlot.setRangeGridlinePaint(Color.white);
		newPlot.setDomainCrosshairVisible(true);
		newPlot.setDomainCrosshairLockedOnData(true);
		// Set up fixed ranges.
		// ValueAxis xaxis = new NumberAxis();
		// xaxis.setRange(minValue, maxValue);
		// newPlot.setRangeAxis(xaxis);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) newPlot
				.getRenderer();
		renderer.setShapesVisible(true);
		renderer.setShapesFilled(true);
		if (isToolTipEnabled) {

			renderer.setToolTipGenerator(new XYToolTipGenerator() {

				public String generateToolTip(XYDataset dataset, int series,
						int item) {
					String resultStr = "";
					Object[] result = new Object[3];
					String label = (String) (plots.getSeries(series).getKey());
					double x = dataset.getXValue(series, item);
					if (Double.isNaN(x) && dataset.getX(series, item) == null) {
						return resultStr;
					}

					double y = dataset.getYValue(series, item);
					if (Double.isNaN(y) && dataset.getX(series, item) == null) {
						return resultStr;
					}
					String xStr = myFormatter.format(x);

					return resultStr = label + ": ([" + xStr + ", "
							+ myFormatter.format(x + 0.01) + "), " + (int) y
							+ ")";
				}
			});
		}
		renderer.setSeriesLinesVisible(0, true);
		for (int i = 1; i < newPlot.getDatasetCount(); i++) {
			renderer.setSeriesLinesVisible(i, true);
		}

		// base color & shape
		// renderer.setSeriesPaint(0, baseColor);
		// renderer.setSeriesShape(0, baseShape);

		newPlot.setRenderer(renderer);

		// change the auto tick unit selection to integer units only...
		NumberAxis rangeAxis = (NumberAxis) newPlot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		ValueAxis xAxis = newPlot.getDomainAxis();

		// xAxis.setAutoRange(false);
		// xAxis.setRange(dateRange);

		xAxis.setRange(0, 1);
		// OPTIONAL CUSTOMISATION COMPLETED.
		graph.setChart(chart);

	}

	void thresholdSlider_stateChanged(ChangeEvent e) {
		int value = thresholdSlider.getValue();
		double maxValue = thresholdSlider.getMaximum();
		XYPlot plot = this.chart.getXYPlot();
		ValueAxis domainAxis = plot.getDomainAxis();
		Range range = domainAxis.getRange();
		// double c = domainAxis.getLowerBound()
		// + (value / maxValue) * range.getLength();

		double lowValue = (double) value
				/ (CellularNetWorkElementInformation.getBinNumber() - 1);

		plot.setDomainCrosshairValue(lowValue);
		String s = myFormatter.format(lowValue);
		thresholdTextField.setText(s);

		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			cellularNetWorkElementInformation.setThreshold(lowValue);

		}
		previewTableModel.fireTableDataChanged();
		detailTable.revalidate();
	}

	void updateProgressBar(final double percent, final String text) {
		Runnable r = new Runnable() {
			public void run() {
				try {
					jProgressBar1.setForeground(Color.GREEN);
					jProgressBar1.setString(text);
					jProgressBar1.setValue((int) (percent * 100));
					if (text.startsWith("Stop")) {
						jProgressBar1.setForeground(Color.RED);
					}
				} catch (Exception e) {
				}
			}
		};
		SwingUtilities.invokeLater(r);
	}

	private void createNetworks(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_loadfromDBHandler
		DSItemList<DSGeneMarker> markers = dataset.getMarkers();
		DSItemList<DSGeneMarker> copy = new CSItemList<DSGeneMarker>();
		copy.addAll(markers);
		CellularNetworkKnowledgeWidget.EntrezIdComparator eidc = new CellularNetworkKnowledgeWidget.EntrezIdComparator();
		Collections.sort(copy, eidc);
		AdjacencyMatrix matrix = new AdjacencyMatrix();
		AdjacencyMatrixDataSet dataSet = null;
		matrix.setMicroarraySet(dataset);
		int serial = 0;
		boolean isEmpty = true;
		String historyStr = "";
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions();
			if (arrayList != null && arrayList.size() > 0) {
				int index = Collections.binarySearch(copy,
						cellularNetWorkElementInformation.getdSGeneMarker(),
						eidc);
				serial = copy.get(index).getSerial();
				matrix.addGeneRow(serial);
				 

				for (InteractionDetail interactionDetail : arrayList) {
					DSGeneMarker marker = new CSGeneMarker();
					marker.setGeneId(new Integer(interactionDetail
							.getdSGeneMarker2()));
					index = Collections.binarySearch(copy, marker, eidc);
					if (index >= 0) {
						isEmpty = false;
						int serial2 = copy.get(index).getSerial();
						matrix.add(serial, serial2, 0.8f);
						if (interactionDetail
								.getInteractionType()
								.equalsIgnoreCase(
										InteractionDetail.PROTEINPROTEININTERACTION)) {
							matrix.addDirectional(serial, serial2, "pp");
							matrix.addDirectional(serial2, serial, "pp");

						} else {
							matrix.addDirectional(serial, serial2, "pd");
							matrix.addDirectional(serial2, serial, "pd");
						}
					} else {
						System.out.println("Marker "
								+ interactionDetail.getdSGeneMarker2()
								+ " does not exist at the dataset. ");
					}
				}
			}
           
			dataSet = new AdjacencyMatrixDataSet(matrix, serial, 0.5f, 2,
					"Adjacency Matrix", dataset.getLabel(), dataset);

			if (cellularNetWorkElementInformation.isIncludePDInteraction() == true || cellularNetWorkElementInformation.isIncludePPInteraction() == true)
			{
				historyStr += "           " + cellularNetWorkElementInformation.getdSGeneMarker().getLabel() + ": " ;
				if (cellularNetWorkElementInformation.isIncludePDInteraction())
				   historyStr += "Include Protein-DNA(true): ";
				else
				   historyStr += "Include Protein-DNA(false): ";
				historyStr += cellularNetWorkElementInformation.getPdInteractionNum() + ", ";
			    
				if (cellularNetWorkElementInformation.isIncludePPInteraction())
					   historyStr += "Include Protein-Protein(true): ";
					else
					   historyStr += "Include Protein-Protein(false): ";
				historyStr += cellularNetWorkElementInformation.getPpInteractionNum() + "\n";
				
			}
			 
			
		} // end for loop
		
		
		 

		if (dataSet != null && !isEmpty) {
			
			historyStr = "Cellular Network Parameters: \n" + "      Threshold: " + thresholdTextField.getText()+ "\n" + "      Selected Marker List: \n" + historyStr +"\n";  
			ProjectPanel.addToHistory(dataSet, historyStr);
			publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
					"Adjacency Matrix Added", null, dataSet));
			publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(matrix,
					"Interactions from knowledgebase", -1, 2, 0.5f,
					AdjacencyMatrixEvent.Action.DRAW_NETWORK));
		} else {
			JOptionPane.showMessageDialog(null,
					"No interactions exists in the current databaset",
					"Empty Set", JOptionPane.ERROR_MESSAGE);
		}

	}// GEN-LAST:event_loadfromDBHandler

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JButton addButton;

	private JButton cancelButton;

	private JList allGeneList;

	private JButton refreshButton;

	private JButton createNetWorkButton;

	private JLabel jLabel1;

	private JLabel jLabel2;

	private JPanel jPanel1;

	private JPanel jPanel2;

	private JPanel throttlePanel;

	private JFreeChart chart;

	private ChartPanel graph;

	private JScrollPane jScrollPane1;

	private JScrollPane jScrollPane3;

	private JScrollPane jScrollPane4;

	private JTable detailTable;

	private JButton removeButton;

	private JButton displayPreferenceButton;

	private JList selectedGenesList;

	private JSplitPane topPane = new JSplitPane();

	private JSplitPane upPanel = new JSplitPane();

	private JTable activatedMarkerTable;

	private JProgressBar jProgressBar1 = new JProgressBar();

	private JToolBar progressDisplayBar;

	private JToolBar commandToolBar;

	private JToolBar graphToolBar;

	private JLabel thresholdLabel;

	private JLabel activeMarkersLabel;

	private JTextField thresholdTextField;

	private JSlider thresholdSlider;

	private JCheckBox allProteinCheckbox;

	private JCheckBox allProteinDNACheckbox;

	private DecimalFormat myFormatter = new DecimalFormat("0.00");

	// End of variables declaration//GEN-END:variables

	// added by xz
	private JCheckBox ppInteractions = new JCheckBox(
			"Include  Protein-Protein Interactions");

	private JCheckBox pdInteractions = new JCheckBox(
			"Include Protein-DNA Interactions");

	JPanel mainDialogPanel = new JPanel();

	JCheckBox[] jCheckBoxes = new JCheckBox[columnLabels.length];

	GridLayout gridLayout1 = new GridLayout();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JPanel jCenterPanel = new JPanel();

	JPanel jSouthPanel = new JPanel();

	GridLayout gridLayout2 = new GridLayout();

	JButton jButton1 = new JButton();

	BorderLayout borderLayout1 = new BorderLayout();

	JDialog dialog = new JDialog();

	JDialog goDialog = new JDialog();

	private void allGeneListHandler(MouseEvent evt) {
		if (evt.getClickCount() == 2) {
			int index = allGeneList.locationToIndex(evt.getPoint());
			DSGeneMarker m = allGenes.get(index);
			selectedGenes.add(m);
			allGenes.remove(m);
			allGeneList.setModel(new DefaultListModel());
			allGeneList.setModel(allGeneModel);
			selectedGenesList.setModel(new DefaultListModel());
			selectedGenesList.setModel(selectedGenesModel);
		}
	}

	private void selectedGenesListHandler(MouseEvent evt) {
		if (evt.getClickCount() == 2) {
			int index = selectedGenesList.locationToIndex(evt.getPoint());
			if (index >= 0) {
				DSGeneMarker m = selectedGenes.get(index);
				allGenes.add(m);
				selectedGenes.remove(m);
				allGeneList.setModel(new DefaultListModel());
				allGeneList.setModel(allGeneModel);
				selectedGenesList.setModel(new DefaultListModel());
				selectedGenesList.setModel(selectedGenesModel);
			}
		}
	}

	private void addButtonHandler(ActionEvent e) {
		int[] indices = allGeneList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			Vector<DSGeneMarker> markers = new Vector<DSGeneMarker>();
			for (int index : indices) {
				DSGeneMarker marker = allGenes.get(index);
				selectedGenes.add(marker);
				markers.add(marker);
			}
			for (DSGeneMarker marker : markers) {
				allGenes.remove(marker);
			}
			allGeneList.setModel(new DefaultListModel());
			allGeneList.setModel(allGeneModel);
			selectedGenesList.setModel(new DefaultListModel());
			selectedGenesList.setModel(selectedGenesModel);
		}
	}

	private void removeButtonHandler(ActionEvent e) {
		int[] indices = selectedGenesList.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			Vector<DSGeneMarker> markers = new Vector<DSGeneMarker>();
			for (int index : indices) {
				DSGeneMarker marker = selectedGenes.get(index);
				allGenes.add(marker);
				markers.add(marker);
			}
			for (DSGeneMarker marker : markers) {
				selectedGenes.remove(marker);
			}
			allGeneList.setModel(new DefaultListModel());
			allGeneList.setModel(allGeneModel);
			selectedGenesList.setModel(new DefaultListModel());
			selectedGenesList.setModel(selectedGenesModel);
		}
	}

	private void cancelTheAction(ActionEvent e) {
		cancelAction = true;
	}

	private void previewSelectionsHandler(ActionEvent e) {
		refreshButton.setEnabled(false);
		cancelAction = false;
		Runnable r = new Runnable() {
			public void run() {
				try {
					updateProgressBar(0, "Querying the Knowledge Base...");
					InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
					int retrievedQueryNumber = 0;
					for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
						retrievedQueryNumber++;
						updateProgressBar(((double) retrievedQueryNumber)
								/ hits.size(), "Querying the Knowledge Base...");

						DSGeneMarker marker = cellularNetWorkElementInformation
								.getdSGeneMarker();
						if (cancelAction) {
							break;
						}

						if (marker != null && marker.getGeneId() != -1
								&& cellularNetWorkElementInformation.isDirty()) {
							BigDecimal id = new BigDecimal(marker.getGeneId());
							ArrayList<InteractionDetail> interactionDetails = interactionsConnection
									.getPairWiseInteraction(id);
							cellularNetWorkElementInformation
									.setInteractionDetails(interactionDetails);
							cellularNetWorkElementInformation.setDirty(false);
						}
					}
					if (!cancelAction) {
						updateProgressBar(1, "Query is finished.");
						drawPlot(createCollection(0, 1, 1, true),
								"Throttle Graph");
						throttlePanel.repaint();

					} else {
						updateProgressBar(1, "Stopped");
					}
					previewTableModel.fireTableDataChanged();
					detailTable.revalidate();
					refreshButton.setEnabled(true);

				} catch (Exception e) {
				}
			}
		};

		Thread thread = new Thread(r);
		thread.start();

	}

	/**
	 * Create a connection with the server.
	 */
	private void initConnections() {
		EngineConfiguration ec = new BasicClientConfig();
		INTERACTIONSServiceLocator service = new INTERACTIONSServiceLocator(ec);
		service.setinteractionsEndpointAddress(System
				.getProperty("interactions.endpoint"));
		try {
			interactionsService = service.getinteractions();
		} catch (ServiceException se) {
			se.printStackTrace();
		}
	}

	ListModel allGeneModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return allGenes.get(index);
		}

		public int getSize() {
			return allGenes.size();
		}
	};

	ListModel selectedGenesModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return selectedGenes.get(index);
		}

		public int getSize() {
			return selectedGenes.size();
		}
	};

	DefaultTableModel activeMarkersTableModel = new DefaultTableModel() {

		@Override
		public boolean isCellEditable(int r, int c) {
			return false;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			if (allGenes != null)
				return allGenes.size();
			return 0;
		}

		@Override
		public String getColumnName(int index) {
			switch (index) {
			case 0:
				return "Marker ";
			case 1:
				return "Gene";
			case 2:
				return "Type";

			default:
				return "";
			}
		}

		@Override
		synchronized public Object getValueAt(int row, int column) {
			Thread.currentThread().setContextClassLoader(
					CellularNetworkKnowledgeWidget.this.getClass()
							.getClassLoader());
			if (allGenes != null) {

				DSGeneMarker value = allGenes.get(row);
				if (value != null) {
					switch (column) {
					case 0: {

						return value.getLabel();

					}
					case 1: {
						if (value.getGeneName() != null) {
							return value.getGeneName();
						} else {
							return AnnotationParser.getGeneName(value
									.getLabel());

						}
					}
					case 2: {

						return GeneOntologyUtil.getOntologyUtil()
								.checkMarkerFunctions(value);
					}
					case 3: {

						return cachedPreviewData.get(row).get(3);
					}
					default:
						return "loading ...";
					}
				}

			}
			// TableWorker worker = new TableWorker(row, column);
			// worker.start();
			return "loading ...";
		}
	};

	DefaultTableModel previewTableModel;

	{
		previewTableModel = new DefaultTableModel() {

			@Override
			public int getColumnCount() {
				return 8;
			}

			@Override
			public int getRowCount() {
				if (hits != null)
					return hits.size();
				return 0;
			}

			@Override
			public String getColumnName(int index) {
				switch (index) {
				case 0:
					return INCLUDEPDLABEL;
				case 1:
					return INCLUDEPPLABEL;
				case 2:
					return MARKERLABEL;
				case 3:
					return GENELABEL;
				case 4:
					return GENETYPELABEL;
				case 5:
					return GOTERMCOLUMN;
				case 6:
					return PDNUMBERLABEL;
				case 7:
					return PPNUMBERLABEL;
				default:
					return "";
				}
			}

			/* get the Object data to be displayed at (row, col) in table */
			@Override
			public Object getValueAt(int row, int col) {

				CellularNetWorkElementInformation hit = hits.get(row);
				/* display data depending on which column is chosen */
				switch (col) {
				case 0:
					return hit.isIncludePDInteraction();
				case 1:
					return hit.isIncludePPInteraction();
				case 2:
					return hit.getdSGeneMarker().getLabel();
				case 3:
					return hit.getdSGeneMarker().getGeneName();
				case 4:
					return hit.getGeneType();

				case 5:
					return hit.getGoInfoStr();
				case 6:
					return hit.getPdInteractionNum();
				case 7:
					return hit.getPpInteractionNum();
				}
				return "Loading";
			}

			/* returns the Class type of the column c */
			@Override
			public Class getColumnClass(int c) {
				if (getValueAt(0, c) != null) {
					return getValueAt(0, c).getClass();
				}
				return String.class;
			}

			/*
			 * returns if the cell is editable; returns false for all cells in
			 * columns except column 6
			 */
			@Override
			public boolean isCellEditable(int row, int col) {
				// Note that the data/cell address is constant,
				// no matter where the cell appears onscreen.
				if (col >= detailTable.getColumnCount()) {
					return false;
				}
				TableColumn tableColumn = detailTable.getColumnModel()
						.getColumn(col);
				return tableColumn.getHeaderValue().toString()
						.equalsIgnoreCase(INCLUDEPDLABEL)
						|| tableColumn.getHeaderValue().toString()
								.equalsIgnoreCase(INCLUDEPPLABEL);
			}

			/*
			 * detect change in cell at (row, col); set cell to value; update
			 * the table
			 */
			@Override
			public void setValueAt(Object value, int row, int col) {
				CellularNetWorkElementInformation hit = hits.get(row);
				TableColumn tableColumn = detailTable.getColumnModel()
						.getColumn(col);
				if (tableColumn.getHeaderValue().toString().equalsIgnoreCase(
						INCLUDEPDLABEL)) {

					hit.setIncludePDInteraction((Boolean) value);
					if (!(Boolean) value) {
						allProteinDNACheckbox.setSelected(false);
					}
				} else if (tableColumn.getHeaderValue().toString()
						.equalsIgnoreCase(INCLUDEPPLABEL)) {
					hit.setIncludePPInteraction((Boolean) value);
					if (!(Boolean) value) {
						allProteinCheckbox.setSelected(false);
					}
				}
				fireTableCellUpdated(row, col);
				commandToolBar.repaint();
				detailTable.repaint();
			}

		};
	}

	public class IntegerRenderer extends JLabel implements TableCellRenderer {
		Border unselectedBorder = null;

		Border selectedBorder = null;

		boolean isBordered = true;

		public IntegerRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}

		public IntegerRenderer() {
			this(true);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {
			TableColumn tableColumn = detailTable.getColumnModel().getColumn(
					column);
			if (hits != null && hits.size() > row) {
				CellularNetWorkElementInformation cellularNetWorkElementInformation = hits
						.get(row);
				boolean isDirty = cellularNetWorkElementInformation.isDirty();
				if (isDirty) {
					if (isSelected) {
						if (selectedBorder == null) {
							selectedBorder = BorderFactory.createMatteBorder(2,
									5, 2, 5, table.getSelectionBackground());
						}
						setBorder(selectedBorder);

						// setForeground(list.getSelectionForeground());
						setText("<html><font color=blue><i>" + "Unknown"
								+ "</i></font></html>");
					} else {

						// setForeground(Color.red);
						// setForeground(list.getSelectionForeground());
						// setText("<html><font color=RED><i>" + "Unknown" +
						// "</i></font></html>");
						setText("<html><font><i>" + "Unknown"
								+ "</i></font></html>");
						setToolTipText("Please push the Refresh button to retrieve related information.");
						if (unselectedBorder == null) {
							unselectedBorder = BorderFactory.createMatteBorder(
									2, 5, 2, 5, table.getBackground());
						}
						setBorder(unselectedBorder);
					}
				} else {

					setBackground(Color.white);
					// System.out.println( tableColumn.getHeaderValue() +"PD= "
					// +
					// !cellularNetWorkElementInformation.isIncludePDInteraction()
					// +
					// tableColumn.getHeaderValue().toString().equalsIgnoreCase(PDNUMBERLABEL)
					// );
					if (!cellularNetWorkElementInformation
							.isIncludePDInteraction()
							&& tableColumn.getHeaderValue().toString()
									.equalsIgnoreCase(PDNUMBERLABEL)) {
						setBackground(Color.gray);
					}
					// else if (column == 6) {
					// setBackground(Color.white);
					// }
					if (!cellularNetWorkElementInformation
							.isIncludePPInteraction()
							&& tableColumn.getHeaderValue().toString()
									.equalsIgnoreCase(PPNUMBERLABEL)) {
						setBackground(Color.gray);
					}
					// else if (column == 7) {
					// setBackground(Color.white);
					// }
					if (isSelected) {
						if (selectedBorder == null) {
							selectedBorder = BorderFactory.createMatteBorder(2,
									5, 2, 5, table.getSelectionBackground());
						}
						setBorder(selectedBorder);
						// setBackground(Color.blue);
						// setForeground(list.getSelectionForeground());
						setText("<html><font color=blue><b>" + color
								+ "</b></font></html>");
						setToolTipText(color.toString());
					} else {

						// setForeground(Color.red);
						// setForeground(list.getSelectionForeground());
						setText("<html><font color=blue><b>" + color
								+ "<b></font></html>");
						if (unselectedBorder == null) {
							unselectedBorder = BorderFactory.createMatteBorder(
									2, 5, 2, 5, table.getBackground());
						}
						setBorder(unselectedBorder);
					}
				}
			}

			return this;
		}
	}

	public class ColorRenderer extends JLabel implements TableCellRenderer {
		Border unselectedBorder = null;

		Border selectedBorder = null;

		boolean isBordered = true;

		public ColorRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}

		public ColorRenderer() {
			this(true);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {

			if (hits != null && hits.size() > row) {
				CellularNetWorkElementInformation cellularNetWorkElementInformation = hits
						.get(row);
				boolean isDirty = cellularNetWorkElementInformation.isDirty();
				if (isDirty) {
					if (isSelected) {
						if (selectedBorder == null) {
							selectedBorder = BorderFactory.createMatteBorder(2,
									5, 2, 5, table.getSelectionBackground());
						}
						setBorder(selectedBorder);

						// setForeground(list.getSelectionForeground());
						setText("<html><font color=blue><i>" + color
								+ "</i></font></html>");
					} else {

						setForeground(Color.black);
						// setForeground(list.getSelectionForeground());
						if (color != null)
							setText("<html><font color=red><i>" + color
									+ "</i></font></html>");
						if (unselectedBorder == null) {
							unselectedBorder = BorderFactory.createMatteBorder(
									2, 5, 2, 5, table.getBackground());
						}
						setBorder(unselectedBorder);
					}
				} else {
					if (isSelected) {
						if (selectedBorder == null) {
							selectedBorder = BorderFactory.createMatteBorder(2,
									5, 2, 5, table.getSelectionBackground());
						}
						setBorder(selectedBorder);

						// setForeground(list.getSelectionForeground());
						setText("<html><font color=blue><b>" + color
								+ "</b></font></html>");
					} else {

						setForeground(Color.black);
						// setForeground(list.getSelectionForeground());
						setText("<html><font color=blue><b>" + color
								+ "<b></font></html>");
						if (unselectedBorder == null) {
							unselectedBorder = BorderFactory.createMatteBorder(
									2, 5, 2, 5, table.getBackground());
						}
						setBorder(unselectedBorder);
					}
				}
			}
			setToolTipText("Value:  " + color);
			return this;
		}
	}

	TableCellRenderer tableHeaderRenderer = new TableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			return new JLabel("<html><b> " + (String) value + "</b></html>");
		}
	};

	private INTERACTIONS interactionsService = null;

	private Vector<DSGeneMarker> allGenes = new Vector<DSGeneMarker>();

	private Vector<DSGeneMarker> selectedGenes = new Vector<DSGeneMarker>();

	private Vector<BigDecimal> entrezIds = new Vector<BigDecimal>();

	private Vector<String> geneNames;

	{
		geneNames = new Vector<String>();
	}

	private Vector<Vector<Object>> cachedPreviewData = new Vector<Vector<Object>>();

	private Vector<CellularNetWorkElementInformation> hits = new Vector<CellularNetWorkElementInformation>();

	private DSMicroarraySet dataset = null;

	@Subscribe
	public void receive(GeneSelectorEvent gse, Object source) {
		log.debug("received GeneSelectorEvent::source="
				+ source.getClass().getName());
		final DSPanel<DSGeneMarker> panel = gse.getPanel();
		final Runnable processEventThread = new Runnable() {
			public void run() {

				processData(panel);
			}
		};
		Thread t = new Thread(processEventThread);
		t.setPriority(t.MAX_PRIORITY);
		t.start();
		log.debug("end GeneSelectorEvent at CNKW");
	}

	synchronized void processData(DSPanel<DSGeneMarker> panel) {
		log.debug("start processData" );
		if (panel != null) {
			if (panel.size() == 0) {

				allGenes.clear();
				selectedGenes.clear();
				hits.clear();
			}
			allGenes.clear();
			ListOrderedSet<DSGeneMarker> orderedSet = new ListOrderedSet<DSGeneMarker>();

			for (DSGeneMarker marker : panel) {
				orderedSet.add(marker);
			}
			updateAllMarkersList(orderedSet, hits);

			activeMarkersTableModel.fireTableDataChanged();
			checkSelectedTableWithNewDataSet(panel);

			allGeneList.setModel(new DefaultListModel());
			allGeneList.setModel(allGeneModel);
			selectedGenesList.setModel(new DefaultListModel());
			selectedGenesList.setModel(selectedGenesModel);

		}
		repaint();
		log.debug("end processData" );
	}

	private boolean checkSelectedTableWithNewDataSet(DSPanel<DSGeneMarker> panel) {
		if (hits == null) {
			return false;
		}
		Vector<CellularNetWorkElementInformation> willRemoved = new Vector<CellularNetWorkElementInformation>();
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

			DSGeneMarker marker = cellularNetWorkElementInformation
					.getdSGeneMarker();
			if (!panel.contains(marker)) {
				willRemoved.add(cellularNetWorkElementInformation);
			}

		}
		if (willRemoved.size() > 0)
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : willRemoved) {
				hits.remove(cellularNetWorkElementInformation);
			}
		return true;
	}

	private void updateAllMarkersList(ListOrderedSet<DSGeneMarker> orderedSet,
			Vector<CellularNetWorkElementInformation> vector) {
		if (vector != null && orderedSet != null && vector.size() > 0
				&& orderedSet.size() > 0) {
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : vector) {
				if (cellularNetWorkElementInformation != null) {
					DSGeneMarker gene = cellularNetWorkElementInformation
							.getdSGeneMarker();
					if (orderedSet.contains(gene)) {
						orderedSet.remove(gene);
					}
				}
			}

		}
		for (Iterator<DSGeneMarker> markerIter = orderedSet.iterator(); markerIter
				.hasNext();) {
			allGenes.add(markerIter.next());
		}
	}

	// private boolean includeMarker(DSGeneMarker marker,
	// Vector<CellularNetWorkElementInformation> vector) {
	//
	// // if (java.util.Collections.binarySearch(vector, marker) >= 0) {
	// // return true;
	// // }
	// for (CellularNetWorkElementInformation cellularNetWorkElementInformation
	// : vector) {
	// if (cellularNetWorkElementInformation != null
	// && cellularNetWorkElementInformation.getdSGeneMarker()
	// .equals(marker)) {
	// return true;
	// }
	// }
	// return false;
	// }

	@Subscribe
	public void receive(ProjectEvent pe, Object source) {
		DSDataSet ds = pe.getDataSet();
		if (ds != null && ds instanceof DSMicroarraySet) {
			dataset = (DSMicroarraySet) ds;
		}
	}

	@Publish
	public AdjacencyMatrixEvent publishAdjacencyMatrixEvent(
			AdjacencyMatrixEvent ae) {
		return ae;
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent pe) {
		return pe;
	}

	class EntrezIdComparator implements Comparator<DSGeneMarker> {
		public int compare(DSGeneMarker m1, DSGeneMarker m2) {
			return (new Integer(m1.getGeneId())).compareTo(new Integer(m2
					.getGeneId()));
		}
	}

	class ColumnKeeper implements ActionListener {
		protected TableColumn m_column;

		public ColumnKeeper(TableColumn column) {
			m_column = column;

		}

		public void actionPerformed(ActionEvent e) {
			JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
			TableColumnModel model = detailTable.getColumnModel();
			if (item.isSelected()) {
				model.addColumn(m_column);
			} else {
				model.removeColumn(m_column);
			}
			detailTable.tableChanged(new TableModelEvent(previewTableModel));
			detailTable.repaint();
		}
	}
}
