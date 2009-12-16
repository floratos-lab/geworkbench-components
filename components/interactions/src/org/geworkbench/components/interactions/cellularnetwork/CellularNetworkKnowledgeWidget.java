package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Properties;
import java.io.FileInputStream;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.CellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.JTabbedPane;
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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.GeneOntologyUtil;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.Util;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author manjunath at genomecenter dot columbia dot edu, xiaoqing zhang
 */
@AcceptTypes( { DSMicroarraySet.class })
public class CellularNetworkKnowledgeWidget extends javax.swing.JScrollPane
		implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	private static String PROPERTIES_FILE = "conf/application.properties";

	private static final String INTERACTIONS_SERVLET_URL = "interactions_servlet_url";

	private Properties iteractionsProp;

	private static final String GOTERMCOLUMN = "GO Annotation";	 

	private static final String ACTIVETABLE = "ACTIVETABLE";

	private static final String DETAILTABLE = "DETAILTABLE";	 

	private static final String MARKERLABEL = "Marker";

	private static final String GENELABEL = "Gene";

	private static final String GENETYPELABEL = "Gene Type";

	private static final String COLUMNLABELPOSTFIX = " #";

	private static final String SELECTCONTEXT = "Select Contex";

	private static final String SELECTVERSION = "Select Version";

	private static String[] firstFourColumnLabels = new String[] { MARKERLABEL,
			GENELABEL, GENETYPELABEL, GOTERMCOLUMN };

	private static String[] columnLabels = new String[] { MARKERLABEL,
		GENELABEL, GENETYPELABEL, GOTERMCOLUMN };;

	private static TableColumn[] tableColumns;

	private boolean cancelAction = false;


	// public static final int THRESHOLD = 5000;

	/**
	 * Creates new form Interactions
	 */
	public CellularNetworkKnowledgeWidget() {

		loadProperty();
		initComponents();
		 
		activatedMarkerTable.getTableHeader().setEnabled(true);
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
							marker, allInteractionTypes);
					boolean newEntry = true;
					for (CellularNetWorkElementInformation cell : hits) {
						if (cell.equals(cellularNetWorkElementInformation)) {
							newEntry = false;
							break;
						}
					}
					if (newEntry) {
						hits.addElement(new CellularNetWorkElementInformation(
								marker, allInteractionTypes));
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
						CellularNetWorkElementInformation cellularNetWorkElementInformation = new

						CellularNetWorkElementInformation(marker,
								allInteractionTypes);
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
											marker, allInteractionTypes));
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
		availableInteractionTypeList = new javax.swing.JList();
		selectedInteractionTypeList = new javax.swing.JList();
		availableNetworkInteractionTypeList = new javax.swing.JList();
		selectedNetworkInteractionTypeList = new javax.swing.JList();
		addButton = new javax.swing.JButton();
		removeButton = new javax.swing.JButton();
		networkAddButton = new javax.swing.JButton();
		networkRemoveButton = new javax.swing.JButton();
		refreshButton = new javax.swing.JButton();
		changeButton = new javax.swing.JButton();
		jPanel1 = new javax.swing.JPanel();
		jScrollPane3 = new javax.swing.JScrollPane();
		jScrollPane3
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jPreferencePanel = new javax.swing.JPanel();
		jScrollPane4 = new javax.swing.JScrollPane();
		jScrollPane5 = new javax.swing.JScrollPane();
		jTabbedPane1 = new JTabbedPane();
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
				.setToolTipText("Move the slider to change the threshold for the throttle graph");

		graphToolBar.add(thresholdLabel);
		graphToolBar.add(Box.createHorizontalStrut(10));
		graphToolBar.add(thresholdTextField);
		graphToolBar.add(thresholdSlider);
		cancelButton = new JButton();

		contextComboBox.setSize(60, 10);
		if (contextList != null) {
			contextList.add(0, SELECTCONTEXT);
		} else {
			contextList = new ArrayList<String>();
			contextList.add(SELECTCONTEXT);
		}
		contextComboBox
				.setModel(new DefaultComboBoxModel(contextList.toArray()));

		versionComboBox.addItem(SELECTVERSION);
		versionComboBox.setSelectedItem(SELECTVERSION);
		versionComboBox.setSize(80, 10);

		contextComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				Object selectedVersion = versionComboBox.getSelectedItem();
				String selectedCoxtext = SELECTCONTEXT;
				if( selectedVersion != null)
				   selectedCoxtext = contextComboBox.getSelectedItem().toString();
				List<VersionDescriptor> versionList = null;
				if (selectedCoxtext != SELECTCONTEXT)
				{				 
					InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
				 
				    versionList = interactionsConnection
						.getVersionDescriptor(selectedCoxtext);
				}
				versionComboBox.removeAllItems();
				versionComboBox.addItem(SELECTVERSION);
				if (versionList != null && versionList.size() > 0) {
					for (int i = 0; i < versionList.size(); i++)
						versionComboBox.addItem(versionList.get(i).getVersion());

				}
				versionComboBox.revalidate();

				for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

					cellularNetWorkElementInformation.setDirty(true);
				}

			}

		});

		versionComboBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				
				Object selectedVersion = versionComboBox.getSelectedItem();
				if (selectedVersion != null && selectedVersion.toString() != SELECTVERSION )
				{
						for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits)  
         					cellularNetWorkElementInformation.setDirty(true);
				}

			}

		});
		
		networkJCheckBox2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				networkJCheckBox2_actionPerformed(e);
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

		availableInteractionTypeList.setModel(availableInteractionTypeModel);
		availableInteractionTypeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					interactionTypeListHandler(evt,

					availableInteractionTypeList, selectedInteractionTypeList,
							displayAvailInteractionTypes,
							displaySelectedInteractionTypes,

							availableInteractionTypeModel,
							selectedInteractionTypeModel);
					if (networkJCheckBox2.isSelected() == true) {
						populatesNetworkPrefFromColumnPref();
					}
					updateColumnPref();
				}
			}
		});

		availableNetworkInteractionTypeList
				.setModel(availNetworkInteractionTypeModel);
		availableNetworkInteractionTypeList
				.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {

						if (evt.getClickCount() == 2
								&& networkJCheckBox2.isSelected() == false) {
							interactionTypeListHandler(evt,
							        availableNetworkInteractionTypeList,
									selectedNetworkInteractionTypeList,
									networkAvailInteractionTypes,
									networkSelectedInteractionTypes,
									availNetworkInteractionTypeModel,
									selectedNetworkInteractionTypeModel);
							if (hits != null && hits.size() > 0) {

								for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits)

									cellularNetWorkElementInformation
											.setIncludedInteractionTypeList(networkSelectedInteractionTypes);

							}

						}

					}
				});

		selectedInteractionTypeList.setModel(selectedInteractionTypeModel);
		selectedInteractionTypeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					interactionTypeListHandler(evt,
							selectedInteractionTypeList,
							availableInteractionTypeList,
							displaySelectedInteractionTypes,
							displayAvailInteractionTypes,
							selectedInteractionTypeModel,
							availableInteractionTypeModel);
					if (networkJCheckBox2.isSelected() == true) {
						populatesNetworkPrefFromColumnPref();
					}
					updateColumnPref();
				}
			}
		});

		selectedNetworkInteractionTypeList
				.setModel(selectedNetworkInteractionTypeModel);
		selectedNetworkInteractionTypeList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2
						&& networkJCheckBox2.isSelected() == false) {
					interactionTypeListHandler(evt,
							selectedNetworkInteractionTypeList,

							availableNetworkInteractionTypeList,
							networkSelectedInteractionTypes,

							networkAvailInteractionTypes,
							selectedNetworkInteractionTypeModel,
							availNetworkInteractionTypeModel);
					if (hits != null && hits.size() > 0) {
						for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits)

							cellularNetWorkElementInformation
									.setIncludedInteractionTypeList(networkSelectedInteractionTypes);

					}
				}

			}
		});

		addButton.setText(">>>");
		addButton.setToolTipText("Add to selection");
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addRemoveButtonHandler(evt,

				availableInteractionTypeList, selectedInteractionTypeList,
						displayAvailInteractionTypes,
						displaySelectedInteractionTypes,

						availableInteractionTypeModel,
						selectedInteractionTypeModel);
				if (networkJCheckBox2.isSelected() == true) {
					populatesNetworkPrefFromColumnPref();
				}

				updateColumnPref();

			}
		});

		networkAddButton.setText(">>>");
		networkAddButton.setToolTipText("Add to selection");
		networkAddButton.setEnabled(false);
		networkAddButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addRemoveButtonHandler(evt,

				availableNetworkInteractionTypeList,
						selectedNetworkInteractionTypeList,
						networkAvailInteractionTypes,
						networkSelectedInteractionTypes,

						availNetworkInteractionTypeModel,
						selectedNetworkInteractionTypeModel);
				if (hits != null && hits.size() > 0) {

					for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits)

						cellularNetWorkElementInformation
								.setIncludedInteractionTypeList(networkSelectedInteractionTypes);

				}
			}
		});

		removeButton.setText("<<<");
		removeButton.setToolTipText("Remove From Selection");
		removeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addRemoveButtonHandler(evt, selectedInteractionTypeList,

				availableInteractionTypeList, displaySelectedInteractionTypes,

				displayAvailInteractionTypes, selectedInteractionTypeModel,
						availableInteractionTypeModel);
				if (networkJCheckBox2.isSelected() == true) {
					populatesNetworkPrefFromColumnPref();
				}

				updateColumnPref();

			}
		});

		networkRemoveButton.setText("<<<");
		networkRemoveButton.setToolTipText("Remove From Selection");
		networkRemoveButton.setEnabled(false);
		networkRemoveButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						addRemoveButtonHandler(evt,
								selectedNetworkInteractionTypeList,

								availableNetworkInteractionTypeList,
								networkSelectedInteractionTypes,

								networkAvailInteractionTypes,
								selectedNetworkInteractionTypeModel,
								availNetworkInteractionTypeModel);
						if (hits != null && hits.size() > 0) {

							for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits)

								cellularNetWorkElementInformation
										.setIncludedInteractionTypeList(networkSelectedInteractionTypes);

						}
					}
				});

		changeButton.setText("Change");
		changeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				changeButtonHandler(evt);
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
							CellularNetWorkElementInformation cellularNetWorkElementInformation = new

							CellularNetWorkElementInformation(marker,
									allInteractionTypes);
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
												marker, allInteractionTypes));
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
		jPanel1.add(progressDisplayBar, BorderLayout.NORTH);
		// jPanel1.add(jScrollPane3, BorderLayout.CENTER);
		jPanel1.add(jTabbedPane1, BorderLayout.CENTER);
		jTabbedPane1.add("Main", jScrollPane3);
		jTabbedPane1.setSelectedIndex(0);
		
		jTabbedPane1.add("Preferences", jScrollPane5);
        
		jTabbedPane1.addChangeListener(new ChangeListener() {
		      public void stateChanged(ChangeEvent e) {
		          
		          if (jTabbedPane1.getSelectedIndex() == 1 && (allInteractionTypes == null || allInteractionTypes.size() == 0 ))
		          {		       
		        	  initPreferences();
		        	  
		          }
		      
		      
		      }
		});
		
		
		
		jPreferencePanel.setLayout(new BorderLayout());
		jPreferencePanel.add(buildInteractionsDatabasePanel(),
				BorderLayout.NORTH);
		jPreferencePanel.add(buildColumnDisplayPreferencesPanel(),
				BorderLayout.CENTER);
		jPreferencePanel.add(buildNetworkGenerationPreferencesPanel(),
				BorderLayout.SOUTH);

		jScrollPane5.setViewportView(jPreferencePanel);

		jPanel1.add(commandToolBar, BorderLayout.SOUTH);
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

		commandToolBar.add(refreshButton);
		commandToolBar.add(createNetWorkButton);
		cancelButton.setText("Cancel");
		commandToolBar.add(cancelButton);
		hits = new Vector<CellularNetWorkElementInformation>();

		tableColumns = new TableColumn[columnLabels.length];
		TableColumnModel model = detailTable.getColumnModel();
		for (int i = 0; i < columnLabels.length; i++) {

			tableColumns[i] = model.getColumn(i);
		}
		 
		jCenterPanel.setLayout(gridLayout2);
		gridLayout2.setColumns(2);
		gridLayout2.setRows(4);

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

			Map<String, XYSeries> interactionDataSeriesMap = new HashMap<String, XYSeries>();
			for (String interactionType : displaySelectedInteractionTypes)
				interactionDataSeriesMap.put(interactionType, new XYSeries(
						interactionType));
			int[] basketValues = new int[binSize];

			Map<String, int[]> interactionBasketValuesMap = new HashMap<String, int[]>();
			for (String interactionType : displaySelectedInteractionTypes)
				interactionBasketValuesMap.put(interactionType,
						new int[binSize]);
			for (int i = 0; i < binSize; i++) {
				basketValues[i] = 0;
			}
			if (hits != null && hits.size() > 0) {
				for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
					int[] distributionArray = cellularNetWorkElementInformation
							.getDistribution();

					for (int i = 0; i < binSize; i++) {
						basketValues[i] += distributionArray[i];

						for (String interactionType : displaySelectedInteractionTypes) {
							interactionBasketValuesMap.get(interactionType)[i] += cellularNetWorkElementInformation
									.getInteractionDistribution(interactionType)[i];

						}

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

				for (String interactionType : displaySelectedInteractionTypes) {
					(interactionDataSeriesMap.get(interactionType)).add(i
							* CellularNetWorkElementInformation
									.getSmallestIncrement(),
							interactionBasketValuesMap.get(interactionType)[i]);
				}
			}

			plots.addSeries(dataSeries);
			for (String interactionType : displaySelectedInteractionTypes) {
				plots.addSeries(interactionDataSeriesMap.get(interactionType));
			}

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
	public void networkJCheckBox2_actionPerformed(ActionEvent e) {
		if (networkJCheckBox2.isSelected() == true) {
			networkAddButton.setEnabled(false);
			networkRemoveButton.setEnabled(false);
			populatesNetworkPrefFromColumnPref();
		} else {
			networkAddButton.setEnabled(true);
			networkRemoveButton.setEnabled(true);
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

	public void updateColumnPref() {

		TableColumnModel model = detailTable.getColumnModel();

		for (int i = 0; i < columnLabels.length; i++)
			model.removeColumn(tableColumns[i]);

		if (markerJCheckBox.isSelected())
			model.addColumn(tableColumns[0]);
		if (geneJCheckBox.isSelected())
			model.addColumn(tableColumns[1]);
		if (geneTypeLCheckBox.isSelected())
			model.addColumn(tableColumns[2]);
		if (goTermJCheckBox.isSelected())
			model.addColumn(tableColumns[3]);

		for (int i = 4; i < columnLabels.length; i++) {

			if (displaySelectedInteractionTypes.contains(columnLabels[i]
					.substring(0,

					columnLabels[i].length() - COLUMNLABELPOSTFIX.length()))) {
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

		chart.addProgressListener(new ChartProgressListener() {

			public void chartProgress(ChartProgressEvent event) {
				if (event.getType() == ChartProgressEvent.DRAWING_FINISHED) {
					// set text field and slider
					JFreeChart chart = event.getChart();
					XYPlot plot = (XYPlot) chart.getPlot();
					double aCrosshair = plot.getDomainCrosshairValue();

					String s = myFormatter.format(aCrosshair);
					thresholdTextField.setText(s);

					double newSliderValue = aCrosshair
							* (CellularNetWorkElementInformation.getBinNumber() - 1);
					thresholdSlider.setValue((int) newSliderValue);
				}
			}

		});

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
					log.error("updateProgressBar() ", e); //$NON-NLS-1$
				}
			}
		};
		SwingUtilities.invokeLater(r);
	}

	private void createNetworks(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_loadfromDBHandler
		HashMap<Integer, String> geneIdToNameMap = new HashMap<Integer, String>();	 
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
		boolean isRestrictToGenesPresentInMicroarray = networkJCheckBox1
				.isSelected();
		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			if (cellularNetWorkElementInformation.isDirty() == true)
				continue;
			ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
					.getSelectedInteractions();
			if (arrayList != null && arrayList.size() > 0) {
				int index = Collections.binarySearch(copy,
						cellularNetWorkElementInformation.getdSGeneMarker(),
						eidc);
				serial = copy.get(index).getSerial();
				matrix.addGeneRow(serial);
				log.debug(" index:" + index + ",serial:" + serial + ",CNKB#"
						+ cellularNetWorkElementInformation.getdSGeneMarker());
				for (InteractionDetail interactionDetail : arrayList) {
					DSGeneMarker marker = new CSGeneMarker();
					String mid2 = interactionDetail.getdSGeneMarker2();
					marker.setGeneId(new Integer(mid2));
					index = Collections.binarySearch(copy, marker, eidc);
					int serial2 = 0;
					if (index >= 0) {
						serial2 = copy.get(index).getSerial();
					} else {
						log.info("Marker "
								+ interactionDetail.getdSGeneMarker2()
								+ " does not exist at the dataset. ");
						if (isRestrictToGenesPresentInMicroarray)
							continue;
						 
						serial2 = (new Integer(mid2)) * (-1);
					 
						if (interactionDetail.getdSGeneName2() != null
								&& !interactionDetail.getdSGeneName2().trim()
										.equals("")
								&& !interactionDetail.getdSGeneName2().trim()
										.equals("null"))
							geneIdToNameMap.put(serial2*(-1), interactionDetail
									.getdSGeneName2());
					}

					isEmpty = false;
					String mid1 = interactionDetail.getdSGeneMarker1();
					DSGeneMarker marker1 = new CSGeneMarker();
					marker1.setGeneId(Integer.parseInt(mid1));
					int index1 = Collections.binarySearch(copy, marker1, eidc);
					int serial1 = 0;
					if (index1 < 0) {
						log.info("Marker "
								+ interactionDetail.getdSGeneMarker1()
								+ " does not exist at the dataset. ");
						if (isRestrictToGenesPresentInMicroarray)
							continue;

						serial1 = Integer.parseInt(mid1) * (-1);
					 
						if (interactionDetail.getdSGeneName1() != null
								&&

								!interactionDetail.getdSGeneName1().trim()
										.equals("")
								&& !interactionDetail.getdSGeneName1().trim()
										.equals("null"))
							geneIdToNameMap.put(serial1 * (-1), interactionDetail
									.getdSGeneName1());
					} else {
						serial1 = copy.get(index1).getSerial();
					}
					matrix.addGeneRow(serial1);

					matrix.add(serial1, serial2, 0.8f);

					matrix.addDirectional(serial1, serial2, interactionDetail
							.getInteractionType());
					matrix.addDirectional(serial2, serial1, interactionDetail
							.getInteractionType());

				}
			}

			Set<String> interactionIncludedSet = cellularNetWorkElementInformation
					.getIncludledInteractionSet();
			if (interactionIncludedSet.size() > 0)
				historyStr += "           "
						+ cellularNetWorkElementInformation.getdSGeneMarker()
								.getLabel() + ": \n";
			for (String interactionType : interactionIncludedSet)
				historyStr += "\t Include "
						+ interactionType
						+ ": "
						+

						cellularNetWorkElementInformation
								.getInteractionNum(interactionType) + "\n";

		} // end for loop
		 
		
		dataSet = new AdjacencyMatrixDataSet(matrix, serial, 0.5f, 2,
				"Adjacency Matrix", dataset.getLabel(), dataset);
		dataSet.clearName("GENEMAP");
		dataSet.addNameValuePair("GENEMAP", geneIdToNameMap);
		
		if (dataSet != null && !isEmpty) {

			historyStr = "Cellular Network Parameters: \n"
					+ "      Threshold: " + thresholdTextField.getText() + "\n"
					+ "      Selected Marker List: \n" + historyStr + "\n";
			ProjectPanel.addToHistory(dataSet, historyStr);
			publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
					"Adjacency Matrix Added", null, dataSet));
			publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(matrix,
					"Interactions from knowledgebase", -1, 2, 0.5f,
					AdjacencyMatrixEvent.Action.DRAW_NETWORK));
		} else {
			JOptionPane.showMessageDialog(null,
					"No interactions exist in the current database.",
					"Empty Set", JOptionPane.ERROR_MESSAGE);
		}

	}// GEN-LAST:event_loadfromDBHandler

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JButton addButton;
	private JButton networkAddButton;

	private JButton cancelButton;

	private JList availableInteractionTypeList;
	private JList availableNetworkInteractionTypeList;

	private JButton refreshButton;

	private JButton createNetWorkButton;

	private JLabel jLabel1;

	private JLabel jLabel2;

	private JPanel jPanel1;

	private JPanel jPanel2;

	private JPanel throttlePanel;

	private JFreeChart chart;

	private ChartPanel graph;

	private JScrollPane jScrollPane3;
	private JScrollPane jScrollPane4;
	private JScrollPane jScrollPane5;

	private JTabbedPane jTabbedPane1;

	private JPanel jPreferencePanel;

	private JTable detailTable;

	private JButton removeButton;
	private JButton networkRemoveButton;

	private JButton changeButton;

	private JList selectedInteractionTypeList;
	private JList selectedNetworkInteractionTypeList;

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

	private DecimalFormat myFormatter = new DecimalFormat("0.00");

	// End of variables declaration//GEN-END:variables

 
	JPanel mainDialogPanel = new JPanel();

	private preferenceJCheckBox markerJCheckBox = new preferenceJCheckBox(
			MARKERLABEL, true);
	private preferenceJCheckBox geneTypeLCheckBox = new preferenceJCheckBox(
			GENETYPELABEL, true);
	private preferenceJCheckBox geneJCheckBox = new preferenceJCheckBox(
			GENELABEL, true);
	private preferenceJCheckBox goTermJCheckBox = new preferenceJCheckBox(
			GOTERMCOLUMN, true);

	private JCheckBox networkJCheckBox1 = new JCheckBox(
			"Restrict to genes prsent in microarray set", false);
	private JCheckBox networkJCheckBox2 = new JCheckBox(
			"Use setting  from Column Display Preferences", true);

	// index service label
	private JLabel serviceLabel = null;

	GridLayout gridLayout1 = new GridLayout();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JPanel jCenterPanel = new JPanel();

	JPanel jSouthPanel = new JPanel();

	GridLayout gridLayout2 = new GridLayout();

	JButton jButton1 = new JButton();

	BorderLayout borderLayout1 = new BorderLayout();

	JDialog dialog = new JDialog();

	JDialog goDialog = new JDialog();

	JDialog changeServicesDialog = null;

	private void interactionTypeListHandler(MouseEvent evt, JList jList1,
			JList jList2, List<String> types1, List<String> types2, ListModel

			listModel1, ListModel listModel2) {

		int index = jList1.locationToIndex(evt.getPoint());
		if (index >= 0) {
			String type = types1.get(index);
			types2.add(type);
			types1.remove(type);
			Collections.sort(types1);
			Collections.sort(types2);
			jList1.setModel(new DefaultListModel());
			jList1.setModel(listModel1);
			jList2.setModel(new DefaultListModel());
			jList2.setModel(listModel2);

		}

	}

	private void populatesNetworkPrefFromColumnPref() {

		networkAvailInteractionTypes.clear();
		networkAvailInteractionTypes.addAll(displayAvailInteractionTypes);
		networkSelectedInteractionTypes.clear();
		networkSelectedInteractionTypes.addAll(displaySelectedInteractionTypes);

		if (hits != null && hits.size() > 0) {

			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {

				cellularNetWorkElementInformation
						.setIncludedInteractionTypeList(networkSelectedInteractionTypes);

			}

		}

		availableNetworkInteractionTypeList.setModel(new DefaultListModel());
		availableNetworkInteractionTypeList
				.setModel(availNetworkInteractionTypeModel);
		selectedNetworkInteractionTypeList.setModel(new DefaultListModel());
		selectedNetworkInteractionTypeList
				.setModel(selectedNetworkInteractionTypeModel);

	}

	private void addRemoveButtonHandler(ActionEvent e, JList jList1,
			JList jList2, List<String> types1, List<String> types2, ListModel

			listModel1, ListModel listModel2) {
		int[] indices = jList1.getSelectedIndices();
		if (indices != null && indices.length > 0) {
			Vector<String> types = new Vector<String>();
			for (int index : indices) {
				String aType = types1.get(index);
				types2.add(aType);
				types.add(aType);
			}
			for (String type : types) {
				types1.remove(type);
			}

			Collections.sort(types1);
			Collections.sort(types2);
			jList1.setModel(new DefaultListModel());
			jList1.setModel(listModel1);
			jList2.setModel(new DefaultListModel());
			jList2.setModel(listModel2);
		}

	}

	private void changeButtonHandler(ActionEvent e) {
		log.debug("changing url");

		String host = ResultSetlUtil.INTERACTIONS_SERVLET_URL;

		changeServicesDialog = new JDialog();

		DefaultFormBuilder indexServerPanelBuilder = new DefaultFormBuilder(
				new FormLayout("right:20dlu"));

		final JTextField hostField = new JTextField(host);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton submitButton = new JButton("Submit");
		submitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String urlStr = hostField.getText();
				Boolean isValidUrl = true;			    
				if (InteractionsConnectionImpl.isValidUrl(urlStr)) {
					ResultSetlUtil.setUrl(urlStr);
					serviceLabel.setText(urlStr);
					reInitPreferences();
				} else {
					isValidUrl = false;
				}

				changeServicesDialog.dispose();

				if (isValidUrl == false) {
					JOptionPane
							.showMessageDialog(
									null,
									"No service running. Please check with the administrator of your service infrastructure.",
									"Error", JOptionPane.ERROR_MESSAGE);
				}

			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeServicesDialog.dispose();
			}
		});

		/* add to button panel */
		buttonPanel.add(submitButton);
		buttonPanel.add(cancelButton);

		/* the builder */
		indexServerPanelBuilder.appendColumn("5dlu");
		indexServerPanelBuilder.appendColumn("250dlu");

		indexServerPanelBuilder.append("URL", hostField);

		JPanel changeServicesPanel = new JPanel(new BorderLayout());
		changeServicesPanel.add(indexServerPanelBuilder.getPanel());
		changeServicesPanel.add(buttonPanel, BorderLayout.SOUTH);
		changeServicesDialog.add(changeServicesPanel);
		changeServicesDialog.setModal(true);
		changeServicesDialog.pack();
		Util.centerWindow(changeServicesDialog);
		changeServicesDialog.setVisible(true);

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
					
				 
					String context = contextComboBox.getSelectedItem()
							.toString();
					String version = versionComboBox.getSelectedItem()
							.toString();
					if (context.equalsIgnoreCase(SELECTCONTEXT)) {
						JOptionPane
								.showMessageDialog(
										null,
										"Please go to Preferences wondow to select database context.",

										"Information",
										JOptionPane.INFORMATION_MESSAGE);
						refreshButton.setEnabled(true);
						return;
					}

					if (version.equalsIgnoreCase(SELECTVERSION)) {
						JOptionPane
								.showMessageDialog(
										null,
										"Please go to Preferences wondow to select database version.",

										"Information",
										JOptionPane.INFORMATION_MESSAGE);
						refreshButton.setEnabled(true);
						return;
					}

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
							ArrayList<InteractionDetail> interactionDetails = null;

							try {
								interactionDetails = interactionsConnection
										.getPairWiseInteraction(id, context,
												version);
							} catch (UnAuthenticatedException uae) {
								cancelAction = true;
								break;
							}

							cellularNetWorkElementInformation
									.setIncludedInteractionTypeList(networkSelectedInteractionTypes);
							 
							cellularNetWorkElementInformation
									.setInteractionDetails(interactionDetails, allInteractionTypes);
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
					log.error("$Runnable.run()", e); //$NON-NLS-1$
					refreshButton.setEnabled(true);

				}
			}
		};

		Thread thread = new Thread(r);
		thread.start();

	}

	/**
	 * Create a connection with the server.
	 */
	private void loadProperty() {
		iteractionsProp = new Properties();
		try {
			iteractionsProp.load(new FileInputStream(PROPERTIES_FILE));
			String interactionsServletUrl = iteractionsProp
					.getProperty(INTERACTIONS_SERVLET_URL);
			ResultSetlUtil.setUrl(interactionsServletUrl);
	 	
		 

		} catch (java.io.IOException ie) {
			log.error(ie.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	
	
	private void initPreferences() {

		try {					
			 
			InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();
		 
			contextList = interactionsConnection.getDatasetNames();
			
			allInteractionTypes = interactionsConnection.getInteractionTypes();
			
			if (contextList != null) {
				contextList.add(0, SELECTCONTEXT);
			} else {
				contextList = new ArrayList<String>();
				contextList.add(SELECTCONTEXT);
			}
			contextComboBox.setModel(new DefaultComboBoxModel(contextList
					.toArray()));

			versionComboBox.removeAllItems();
			versionComboBox.addItem(SELECTVERSION);	


			if (allInteractionTypes != null) {
				
				Collections.sort(allInteractionTypes);
				displayAvailInteractionTypes.addAll(allInteractionTypes);
				networkAvailInteractionTypes.addAll(displayAvailInteractionTypes);
			 
				columnLabels = new String[firstFourColumnLabels.length
						+ displayAvailInteractionTypes.size()];
			 			 
				for (int i = 0; i < firstFourColumnLabels.length; i++)
					columnLabels[i] = firstFourColumnLabels[i];
				for (int i = 0; i < displayAvailInteractionTypes.size(); i++)
					columnLabels[i + 4] = displayAvailInteractionTypes.get(i)
							+ COLUMNLABELPOSTFIX;

			} else {
				for (int i = 0; i < firstFourColumnLabels.length; i++)
					columnLabels[i] = firstFourColumnLabels[i];
			}
			
			previewTableModel = new PreviewTableModel();			 
			detailTable.setModel(previewTableModel);
			tableColumns = new TableColumn[columnLabels.length];
			TableColumnModel model = detailTable.getColumnModel();
			for (int i = 0; i < columnLabels.length; i++) {

				tableColumns[i] = model.getColumn(i);
			}

			for (int i = 0; i < columnLabels.length; i++)
				model.removeColumn(tableColumns[i]);
			for (int i = 0; i < 4; i++)
				model.addColumn(tableColumns[i]);
			
			availableInteractionTypeList.setModel(new DefaultListModel());
			availableInteractionTypeList
					.setModel(availableInteractionTypeModel);
			availableNetworkInteractionTypeList
					.setModel(new DefaultListModel());
			availableNetworkInteractionTypeList
					.setModel(availNetworkInteractionTypeModel);
			selectedInteractionTypeList.setModel(new DefaultListModel());
			selectedInteractionTypeList.setModel(selectedInteractionTypeModel);
			selectedNetworkInteractionTypeList.setModel(new DefaultListModel());
			selectedNetworkInteractionTypeList
					.setModel(selectedNetworkInteractionTypeModel);

			updateColumnPref();

		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}
	
	private void reInitPreferences() {

		try {
			 
			contextList.clear();
			allInteractionTypes.clear();
			displayAvailInteractionTypes.clear();
			displaySelectedInteractionTypes.clear();
			networkAvailInteractionTypes.clear();
			networkSelectedInteractionTypes.clear();
		    
			initPreferences();
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	ListModel availableInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return displayAvailInteractionTypes.get(index);
		}

		public int getSize() {
			return displayAvailInteractionTypes.size();
		}
	};

	ListModel availNetworkInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return networkAvailInteractionTypes.get(index);
		}

		public int getSize() {
			return networkAvailInteractionTypes.size();
		}
	};

	ListModel selectedInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return displaySelectedInteractionTypes.get(index);
		}

		public int getSize() {
			return displaySelectedInteractionTypes.size();
		}
	};

	ListModel selectedNetworkInteractionTypeModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return networkSelectedInteractionTypes.get(index);
		}

		public int getSize() {
			return networkSelectedInteractionTypes.size();
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
	
	PreviewTableModel previewTableModel = new PreviewTableModel();
 
 	 
	
	private class PreviewTableModel extends DefaultTableModel

	{
	 
			@Override
			public int getColumnCount() {
				return columnLabels.length;
			}

			@Override
			public int getRowCount() {
				if (hits != null)
					return hits.size();
				return 0;
			}

			@Override
			public String getColumnName(int index) {
				 
				if ( index >= 0 && index < columnLabels.length)
				    return columnLabels[index];
				else
					return "";
				 
			}

			/* get the Object data to be displayed at (row, col) in table */
			@Override
			public Object getValueAt(int row, int col) {

				CellularNetWorkElementInformation hit = hits.get(row);
				/* display data depending on which column is chosen */
				switch (col) {
			 
				case 0:
					return hit.getdSGeneMarker().getLabel();
				case 1:
					return hit.getdSGeneMarker().getGeneName();
				case 2:
					return hit.getGeneType();
				case 3:
					return hit.getGoInfoStr();
				default:				 
					String interactionType = columnLabels[col].substring(0, columnLabels[col].length()- 

COLUMNLABELPOSTFIX.length());
				    Integer num = hit.getInteractionNum(interactionType);					 
				    if (num != null)
				    	return num;
				    else
				    	return 0;
				    	
				}
				
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
				 return false;
			}

			
	 
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
					setBackground(Color.gray);
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
					String headerStr = tableColumn.getHeaderValue().toString();
					if (!cellularNetWorkElementInformation
							.getIncludledInteractionSet().contains(
									headerStr.substring(0,

									headerStr.length()
											- COLUMNLABELPOSTFIX.length()))) {
						setBackground(Color.gray);
					}
					// else if (column == 6) {
					// setBackground(Color.white);
					// }

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

	public class preferenceJCheckBox extends JCheckBox {

		public preferenceJCheckBox(String label, boolean isSelected) {

			super(label, isSelected);
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {
					updateColumnPref();
				}
			});

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

	private JComboBox contextComboBox = new JComboBox();
	private JComboBox versionComboBox = new JComboBox();

	private List<String> contextList = new ArrayList<String>();

	private List<String> allInteractionTypes = new ArrayList<String>();

	private List<String> displayAvailInteractionTypes = new ArrayList<String>();

	private List<String> displaySelectedInteractionTypes = new ArrayList<String>();

	private List<String> networkAvailInteractionTypes = new ArrayList<String>();

	private List<String> networkSelectedInteractionTypes = new ArrayList<String>();

	// private INTERACTIONS interactionsService = null;

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
		log.debug("start processData");
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
			previewTableModel.fireTableDataChanged();
			checkSelectedTableWithNewDataSet(panel);

			// allGeneList.setModel(new DefaultListModel());
			// allGeneList.setModel(allGeneModel);
			// selectedGenesList.setModel(new DefaultListModel());
			// selectedGenesList.setModel(selectedInteractionTypeModel);

		}
		repaint();
		log.debug("end processData");
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

	private JPanel buildInteractionsDatabasePanel() {
		FormLayout layout = new FormLayout(
				"right:pref, 20dlu, left:pref, 3dlu, " + "left:pref, 7dlu",

				"");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Interactions Database");
		builder.append(changeButton);
		// builder.append(urlLabel);
		serviceLabel = new JLabel("Url: "
				+ ResultSetlUtil.INTERACTIONS_SERVLET_URL);
		builder.append(serviceLabel);
		builder.nextLine();

		builder.append(contextComboBox);
		builder.append(versionComboBox);
		builder.nextLine();

		serviceLabel.setForeground(Color.BLUE);

		return builder.getPanel();

	}

	private JPanel buildColumnCheckBoxPanel() {
		FormLayout layout = new FormLayout(
				"left:pref, 20dlu, left:pref, 20dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.append(markerJCheckBox);
		builder.append(geneTypeLCheckBox);
		builder.nextLine();
		builder.append(geneJCheckBox);
		builder.append(goTermJCheckBox);
		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildNetworkPrefCheckBoxPanel() {
		FormLayout layout = new FormLayout("left:pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.append(networkJCheckBox1);
		builder.nextLine();
		builder.append(networkJCheckBox2);
		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildAddRemoveButtonPanel(JButton addButton,
			JButton removeButton) {
		FormLayout layout = new FormLayout("left:pref, 20dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.append(addButton);
		builder.nextLine();
		builder.append(removeButton);

		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildJListPanel(String title, JList aJlist) {
		FormLayout layout = new FormLayout("left:pref", "");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();
		JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
		jScrollPane1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setPreferredSize(new java.awt.Dimension(150, 90));
		jScrollPane1.setViewportView(aJlist);
		builder.append(new JLabel(title));
		builder.append(jScrollPane1);

		return builder.getPanel();

	}

	private JPanel buildColumnDisplayPreferencesPanel() {
		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 3dlu, "
				+ "left:pref, 3dlu, left:pref, 3dlu",

		"");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Column Display Preference");
		builder.append(buildColumnCheckBoxPanel());
		builder.append(buildJListPanel("Available Interaction Types",
				availableInteractionTypeList));
		builder.append(buildAddRemoveButtonPanel(addButton, removeButton));
		builder.append(buildJListPanel("Selected Interaction Types",
				selectedInteractionTypeList));

		builder.nextLine();

		return builder.getPanel();

	}

	private JPanel buildNetworkGenerationPreferencesPanel() {
		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 3dlu, "
				+ "left:pref, 3dlu, left:pref, 3dlu",

		"");

		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setLeftToRight(true);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Network Generation Preferences");

		builder.append(buildNetworkPrefCheckBoxPanel());
		builder.append(buildJListPanel("Available Interaction Types",
				availableNetworkInteractionTypeList));
		builder.append(buildAddRemoveButtonPanel(networkAddButton,
				networkRemoveButton));
		builder.append(buildJListPanel("Selected Interaction Types",
				selectedNetworkInteractionTypeList));

		builder.nextLine();

		return builder.getPanel();

	}
}
