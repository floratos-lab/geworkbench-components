package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.CellEditor; 
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel; 
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.GeneOntologyUtil;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.config.Closable;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.events.AdjacencyMatrixCancelEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.BasicAuthenticator;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.annotation.AffyAnnotationUtil;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.CellularNetworkPreference;
import org.geworkbench.util.network.InteractionDetail;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

/**
 * @author manjunath at genomecenter dot columbia dot edu, xiaoqing zhang
 * @version $Id: CellularNetworkKnowledgeWidget.java 7531 2011-03-03 15:19:05Z
 *          zji $
 */
@AcceptTypes( { DSMicroarraySet.class })
public class CellularNetworkKnowledgeWidget extends javax.swing.JScrollPane
		implements VisualPlugin, Closable, ActionListener {

	private static final long serialVersionUID = 7095407341852521909L;

	private Log log = LogFactory.getLog(CellularNetworkKnowledgeWidget.class);

	private int timeout = 0;
	 
	private int interaction_flag = 1;

	private static final String[] firstFourColumnLabels = new String[] {
			Constants.MARKERLABEL, Constants.GENELABEL,
			Constants.GENETYPELABEL, Constants.GOTERMCOLUMN };

	private static final int ONE_SECOND = 1000;

	private static String[] columnLabels = new String[] {
			Constants.MARKERLABEL, Constants.GENELABEL,
			Constants.GENETYPELABEL, Constants.GOTERMCOLUMN };;

	private static TableColumn[] tableColumns;

	private Integer cnkbSelectionIndex = 0;

	private CellularNetworkPreference tgPreference = null;

	private boolean cancelAction = false;

	private boolean needRedraw = false;

	private boolean isQueryRuning = false;

	private Vector<CellularNetWorkElementInformation> hits = null;

	private JButton refreshButton;

	private JButton createNetWorkButton;

	private JPanel throttlePanel;

	private JFreeChart chart;

	private ChartPanel graph;
	
	private JPanel chartPanel;

	private LengendCheckBox[] lengendCheckBoxList;
	private JButton[] jButtonList;

	private JPanel legendPanel;

	private LegendObjectCollection legendList;

	private CellularNetworkPreferencePanel jPreferencePanel;

	private CellSelectionTable detailTable;

	private JTable activatedMarkerTable;

	private JProgressBar jProgressBar1 = new JProgressBar();

	private CreateNetworkHandler createNetworkHandler;

	private JTextField thresholdTextField;
	
	private JComboBox thresholdTypes = new JComboBox();

	private JSlider thresholdSlider;

	private DecimalFormat myFormatter = new DecimalFormat("0.00");

	private Vector<DSGeneMarker> allGenes = new Vector<DSGeneMarker>();

	private Vector<DSGeneMarker> selectedGenes = new Vector<DSGeneMarker>();

	private Vector<Vector<Object>> cachedPreviewData = new Vector<Vector<Object>>();

	private DSMicroarraySet dataset = null;

	private Map<String, String> geneTypeMap = null;

	/**
	 * Creates new form Interactions
	 */
	public CellularNetworkKnowledgeWidget() {

		loadApplicationProperty();
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

		detailTable.getTableHeader().setEnabled(true);
		detailTable.setDefaultRenderer(String.class, new ColorRenderer());
		detailTable.setDefaultRenderer(Integer.class, new IntegerRenderer());

		GeneOntologyTree instance = GeneOntologyTree.getInstance();
		if(instance==null) {
			timer = new Timer(ONE_SECOND, this);
			timer.start();
		}
	}
	
	private Timer timer;

	public Component getComponent() {
		return this;
	}

	public void closing() {
		jPreferencePanel.savePreferences();
	}

	private void cancelCellEditing() {
		CellEditor ce = detailTable.getCellEditor();
		if (ce != null) {
			ce.cancelCellEditing();
		}
	}

	/**
	 * Create a popup menu to display the name of different Go Terms in 3
	 * categories.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	private JPopupMenu createSetPopMenu() {
		JPopupMenu contextMenu = new JPopupMenu();
		JMenuItem addToSetItemMenu = new JMenuItem();

		addToSetItemMenu.setText("Copy selected genes to new marker set");
		addToSetItemMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addMarkersToSet();
			}

		});

		contextMenu.add(addToSetItemMenu);

		return contextMenu;
	}

	private void addMarkersToSet() {
		DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(
				Constants.CNKB_SELECTION + " " + cnkbSelectionIndex,
				"Cellular Network Knowledge Base");
		
		Map<String, List<DSGeneMarker>> geneNameToMarkerMap = AffyAnnotationUtil.getGeneNameToMarkerMapping(dataset);
		Map<String, List<DSGeneMarker>> geneIdToMarkerMap = AffyAnnotationUtil.getGeneIdToMarkerMapping(dataset);
		
		int rowCount = detailTable.getRowCount();
		int colCount = detailTable.getColumnCount();
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < colCount; j++) {
				if (detailTable.isCellSelected(i, j)) {
					int row = detailTable.convertRowIndexToModel(i);
					String columnName = detailTable.getColumnName(j);
					CellularNetWorkElementInformation c = hits.get(row);
					ArrayList<InteractionDetail> arrayList = c
							.getSelectedInteractions(columnName.substring(0,
									columnName.length()
											- Constants.COLUMNLABELPOSTFIX
													.length()));
					if (arrayList == null || arrayList.size() <= 0) {
						continue;
					}

					Integer geneId = c.getdSGeneMarker().getGeneId();
					Collection<DSGeneMarker> markers = geneIdToMarkerMap.get(geneId.toString());
					selectedMarkers.addAll(markers);

					for (InteractionDetail detail : arrayList) {
						String dbSource = detail.getDbSource();
						Collection<DSGeneMarker> markers2 = null;
						if (dbSource.equalsIgnoreCase(Constants.ENTREZ_GENE))								 
						   markers2 =
							   geneIdToMarkerMap.get(detail.getdSGeneId());
						else
						   markers2 = 
							   geneNameToMarkerMap.get(detail.getdSGeneName() );
						if (markers2 != null)
						     selectedMarkers.addAll(markers2);
					}

				}
			}

		}

		selectedMarkers.setActive(true);

		publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
				DSGeneMarker.class, selectedMarkers,
				org.geworkbench.events.SubpanelChangedEvent.SET_CONTENTS));

		cnkbSelectionIndex++;
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
		if (tableName.equalsIgnoreCase(Constants.ACTIVETABLE)) {
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

	private void removeAllRows(String tableName) {
		if (!isProcessingAllowed())
			return;
		if (Constants.ACTIVETABLE.equalsIgnoreCase(tableName)) {
			activatedMarkerTable.clearSelection();
			for (DSGeneMarker marker : allGenes) {
				if (marker != null) {
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
			detailTable.getTableSelectionModel().clearSelection();
			for (CellularNetWorkElementInformation marker : hits) {
				if (marker != null
						&& !allGenes.contains(marker.getdSGeneMarker())) {
					allGenes.add(marker.getdSGeneMarker());

				}
			}
			hits.removeAllElements();

			drawPlot(createCollection(0, 1, 1, true), false, true);
			throttlePanel.repaint();

		}

		activatedMarkerTable.revalidate();
		previewTableModel.fireTableDataChanged();
		detailTable.revalidate();

	}

	private void removeMutipleRows(int[] selectedRows, String tableName) {

		if (!isProcessingAllowed())
			return;
		if (Constants.ACTIVETABLE.equals(tableName)) {
			activatedMarkerTable.clearSelection();
			for (int i = selectedRows.length - 1; i >= 0; i--) {
				int row = selectedRows[i];
				if (row < allGenes.size()) {
					DSGeneMarker marker = allGenes.get(row);
					if (marker != null) {
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
			detailTable.getTableSelectionModel().clearSelection();
			List<CellularNetWorkElementInformation> removeRows = new ArrayList<CellularNetWorkElementInformation>();
			for (int i = selectedRows.length - 1; i >= 0; i--) {
				int row = selectedRows[i];
				row = detailTable.convertRowIndexToModel(row);
				selectedRows[i] = row;
				if (row < hits.size()) {
					CellularNetWorkElementInformation marker = hits.get(row);
					removeRows.add(marker);
					if (marker != null
							&& !allGenes.contains(marker.getdSGeneMarker())) {
						allGenes.add(marker.getdSGeneMarker());

					}
				}
			}
			if (removeRows.size() > 0)
				hits.removeAll(removeRows);
			removeRows.clear();

			drawPlot(createCollection(0, 1, 1, true), false, true);
			throttlePanel.repaint();

		}

		activatedMarkerTable.revalidate();
		previewTableModel.fireTableDataChanged();
		detailTable.revalidate();

	}

	/**
	 * Create a popup menu to display thge name of different Go Terms in 3
	 * catagories.
	 * 
	 * @param rowIndex
	 * @return
	 */
	private JPopupMenu createGenePopMenu(final int rowIndex) {

		JPopupMenu contextMenu = new JPopupMenu();
		JMenuItem geneCardsItemMenu = new JMenuItem();
		JMenuItem entrezItemsMenu = new JMenuItem();

		geneCardsItemMenu.setText("Go to GeneCards");
		entrezItemsMenu.setText("Go to Entrez");

		final CellularNetWorkElementInformation hit = hits.get(rowIndex);
		String geneName = hit.getdSGeneMarker().getShortName();
		int geneId = hit.getdSGeneMarker().getGeneId();

		class MyActionListener implements ActionListener {

			String urlStr = "";

			public MyActionListener(String urlStr) {
				super();
				this.urlStr = urlStr;
			}

			public void actionPerformed(ActionEvent actionEvent) {
				try {

					log.debug("Opening " + urlStr);
					BrowserLauncher.openURL(urlStr);
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

		if (geneName != null && !geneName.trim().equals("")
				&& !geneName.trim().equals("---")) {
			geneCardsItemMenu.addActionListener(new MyActionListener(
					Constants.GeneCards_PREFIX + geneName));
			contextMenu.add(geneCardsItemMenu);
		}

		if (geneId > 0) {
			entrezItemsMenu.addActionListener(new MyActionListener(
					Constants.Entrez_PREFIX + geneId));
			contextMenu.add(entrezItemsMenu);
		}

		return contextMenu;
	}

	/**
	 * Create a popup menu to display thge name of different Go Terms in 3
	 * catagories.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	private JPopupMenu createGOTermContextMenu(final int rowIndex,
			final int columnIndex) {

		JPopupMenu contextMenu = new JPopupMenu();
		final CellularNetWorkElementInformation hit = hits.get(rowIndex);
		JMenu componentMenu = new JMenu();
		componentMenu.setText("Component");
		JMenu goFunctionMenu = new JMenu();
		goFunctionMenu.setText("Function");
		JMenu processMenu = new JMenu();
		processMenu.setText("Process");
		TreeMap<String, Set<GOTerm>> treeMapForComponent = null;
		TreeMap<String, Set<GOTerm>> treeMapForFunction = null;
		TreeMap<String, Set<GOTerm>> treeMapForProcess = null;

		treeMapForComponent = hit
				.getAllAncestorGoTerms(AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT);
		treeMapForFunction = hit
				.getAllAncestorGoTerms(AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION);
		treeMapForProcess = hit
				.getAllAncestorGoTerms(AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS);

		if (treeMapForComponent != null && treeMapForComponent.size() > 0) {
			addGoTermMenuItem(componentMenu, treeMapForComponent);
			contextMenu.add(componentMenu);
		}
		if (treeMapForFunction != null && treeMapForFunction.size() > 0) {
			addGoTermMenuItem(goFunctionMenu, treeMapForFunction);
			contextMenu.add(goFunctionMenu);
		}
		if (treeMapForProcess != null && treeMapForProcess.size() > 0) {
			addGoTermMenuItem(processMenu, treeMapForProcess);
			contextMenu.add(processMenu);
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
			TreeMap<String, Set<GOTerm>> treeMap) {

		if (treeMap != null && treeMap.size() > 0) {
			Object[] array = treeMap.keySet().toArray();
			JMenuItem[] jMenuItems = new JMenuItem[array.length];
			for (int i = 0; i < array.length; i++) {
				String s = (String) array[i];
				jMenuItems[i] = new JMenuItem(s);
				jMenu.add(jMenuItems[i]);
				final Set<GOTerm> set = treeMap.get(array[i]);

				jMenuItems[i].addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						CNKBGoTermUtil.displayGoTree(set, CellularNetworkKnowledgeWidget.this);
					}

				});
			}
		}

	}

	/**
	 * Initialize GUI.
	 * 
	 */
	private void initComponents() {

		initGeneTypeMap();
		Authenticator.setDefault(new BasicAuthenticator());
		JPanel jPanel2 = new javax.swing.JPanel();
		JPanel topPanel = new JPanel();
		throttlePanel = new JPanel();
		JLabel jLabel2 = new javax.swing.JLabel();
		refreshButton = new javax.swing.JButton();
		JPanel jPanel1 = new javax.swing.JPanel();
		JScrollPane jScrollPane3 = new javax.swing.JScrollPane();
		jScrollPane3
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jPreferencePanel = new CellularNetworkPreferencePanel(this);
		JScrollPane jScrollPane4 = new javax.swing.JScrollPane();
		JScrollPane jScrollPane5 = new javax.swing.JScrollPane();
		final JTabbedPane jTabbedPane1 = new JTabbedPane();
		detailTable = new CellSelectionTable();
		detailTable.setAutoCreateRowSorter(true);
		JLabel jLabel1 = new javax.swing.JLabel();
		createNetWorkButton = new javax.swing.JButton();
		JSplitPane topPane = new JSplitPane();
		JSplitPane upPanel = new JSplitPane();
		activatedMarkerTable = new JTable();
		JToolBar commandToolBar = new JToolBar();
		JToolBar progressDisplayBar = new JToolBar();

		JToolBar graphToolBar = new JToolBar();

		lengendCheckBoxList = new LengendCheckBox[8];
		jButtonList = new JButton[8];

		for (int i = 0; i < 8; i++) {
			lengendCheckBoxList[i] = new LengendCheckBox("", true);

			jButtonList[i] = new JButton();
			jButtonList[i].setPreferredSize(new java.awt.Dimension(10, 10));
		}
		legendList = new LegendObjectCollection();
		
		
		thresholdTextField = new JTextField("", 7);
		thresholdTextField.setMaximumSize(new Dimension(100, 50));
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
						thresholdSlider_stateChanged();
					}
				});
		thresholdSlider
				.setToolTipText("Move the slider to change the threshold for the throttle graph");
		 
		graphToolBar
				.setLayout(new BoxLayout(graphToolBar, BoxLayout.LINE_AXIS));
		graphToolBar.add(Box.createRigidArea(new Dimension(10, 0)));
		graphToolBar.add(new JLabel("Threshold: "));		 
		thresholdTypes.setPreferredSize(new Dimension(140, 25));
		thresholdTypes.setMaximumSize(new Dimension(140, 25));
		graphToolBar.add(thresholdTypes);
		
		thresholdTypes.addItemListener(new ItemListener()
		{
			 public void itemStateChanged(ItemEvent itemEvent) {
		         System.out.println("itemStateChanged is called...");
				 if (thresholdTypes.getSelectedItem() != null)
				 {
					String type =  CellularNetworkPreferencePanel.interactionConfidenceTypeMap.get(thresholdTypes.getSelectedItem().toString());
					CellularNetWorkElementInformation.setUsedConfidenceType(new Short(type));					
				    
					//drawPlot(createCollection(0, 1, 1, true), false, true);
					//throttlePanel.repaint();
				 
				 }
				 
		      
			 }
			
		});
		graphToolBar.add(thresholdTextField);
		graphToolBar.add(thresholdSlider);
		JButton cancelButton = new JButton();

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

		JButton imageSnapshotButton = new JButton("Throttle Graph Snapshot");
		imageSnapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createImageSnapshot();
			}
		});

		jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(
				204, 204, 255)));

		jPanel1.setMinimumSize(new Dimension(300, 50));
		jPanel1.setPreferredSize(new Dimension(587, 182));

		detailTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & Toolkit.getDefaultToolkit()
						.getMenuShortcutKeyMask()) != 0)
					return;
				if (e.getClickCount() == 2) {

					if (!isProcessingAllowed())
						return;
					JTable target = (JTable) e.getSource();

					int row = target.getSelectedRow();
					if (row == -1)
						return;
					row = detailTable.convertRowIndexToModel(row);
					detailTable.getTableSelectionModel().clearSelection();
					if (row < hits.size()) {
						CellularNetWorkElementInformation marker = hits
								.get(row);
						hits.remove(row);
						if (marker != null
								&& !allGenes.contains(marker.getdSGeneMarker())) {
							allGenes.add(marker.getdSGeneMarker());

						}
						activatedMarkerTable.revalidate();
						previewTableModel.fireTableDataChanged();
						detailTable.revalidate();

						drawPlot(createCollection(0, 1, 1, true), false, true);
						throttlePanel.repaint();

					}
				}
			}

			private void maybeShowPopup(MouseEvent e) {

				if (e.isPopupTrigger() && detailTable.isEnabled()) {
					Point p = new Point(e.getX(), e.getY());
					int col = detailTable.columnAtPoint(p);
					int row = detailTable.rowAtPoint(p);
					row = detailTable.convertRowIndexToModel(row);
					// translate table index to model index
					int mcol = detailTable.getColumn(
							detailTable.getColumnName(col)).getModelIndex();

					if (row >= 0 && row < detailTable.getRowCount()) {
						cancelCellEditing();
						if (detailTable.getColumnName(col).equalsIgnoreCase(
								Constants.GOTERMCOLUMN)) {
							// display popupmenu for go term
							JPopupMenu contextMenu = createGOTermContextMenu(
									row, mcol);
							if (contextMenu != null
									&& contextMenu.getComponentCount() > 0) {
								contextMenu.show(detailTable, p.x, p.y);
							}
						} else if (detailTable.getColumnName(col)
								.equalsIgnoreCase(Constants.GENELABEL)) {
							// display popupmenu for gene
							JPopupMenu contextMenu = createGenePopMenu(row);

							if (contextMenu != null
									&& contextMenu.getComponentCount() > 0) {
								contextMenu.show(detailTable, p.x, p.y);
							}
						} else if (detailTable.getColumnName(col).endsWith(
								Constants.COLUMNLABELPOSTFIX)) {
							JPopupMenu contextMenu = createSetPopMenu();

							if (contextMenu != null
									&& contextMenu.getComponentCount() > 0) {
								contextMenu.show(detailTable, p.x, p.y);
							}
						} else if (detailTable.getColumnName(col)
								.equalsIgnoreCase(Constants.MARKERLABEL)) {
							int[] selectedRows = detailTable.getSelectedRows();
							// display popup menu for row editing.
							JPopupMenu contextMenu = createPopMenu(row, mcol,
									selectedRows, Constants.DETAILTABLE);
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
				if ((e.getModifiers() & Toolkit.getDefaultToolkit()
						.getMenuShortcutKeyMask()) != 0)
					return;
				if (e.getClickCount() == 2) {
					if (!isProcessingAllowed())
						return;
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					if (row == -1)
						return;
					if (row < allGenes.size()) {
						DSGeneMarker marker = allGenes.get(row);
						if (marker != null) {
							CellularNetWorkElementInformation cellularNetWorkElementInformation = new

							CellularNetWorkElementInformation(marker);
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
							previewTableModel.fireTableDataChanged();
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
								selectedRows, Constants.ACTIVETABLE);
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

						createNetWorkButton.setEnabled(false);

						ProgressBar createNetworkPb = null;
						createNetworkPb = ProgressBar
								.create(ProgressBar.INDETERMINATE_TYPE);

						createNetworkHandler = new CreateNetworkHandler(
								createNetworkPb);
						createNetworkHandler.start();

						createNetworkPb.setTitle("Create network");
						createNetworkPb.setMessage("Create network...");
						createNetworkPb.setModal(true);
						createNetworkPb.start();
					}
				});

		jPanel1.setLayout(new BorderLayout());
		progressDisplayBar.setLayout(new BoxLayout(progressDisplayBar,
				BoxLayout.LINE_AXIS));
		progressDisplayBar.add(jLabel1);

		jProgressBar1.setMinimumSize(new Dimension(10, 16));
		jProgressBar1.setBorderPainted(true);
		jProgressBar1.setMaximum(100);
		jProgressBar1.setMinimum(0);

		progressDisplayBar.add(jProgressBar1);
		jPanel1.add(progressDisplayBar, BorderLayout.NORTH);

		jPanel1.add(jTabbedPane1, BorderLayout.CENTER);

		jTabbedPane1.add("Main", jScrollPane3);
		jTabbedPane1.setSelectedIndex(0);
		jTabbedPane1.add("Preferences", jScrollPane5);

		jTabbedPane1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {

				if (jTabbedPane1.getSelectedIndex() == 1
						&& (jPreferencePanel.getAllInteractionTypes() == null || jPreferencePanel
								.getAllInteractionTypes().size() == 0)) {
					InitPrefWorker worker = new InitPrefWorker();
					worker.execute();

				}

			}
		});

		jScrollPane5.setViewportView(jPreferencePanel);

		jPanel1.add(commandToolBar, BorderLayout.SOUTH);
		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.add(upPanel, BorderLayout.CENTER);
		topPanel.add(commandToolBar, BorderLayout.SOUTH);
		this.getViewport().add(topPanel);
		upPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		JLabel activeMarkersLabel = new JLabel(
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
		upPanel.add(topPane, JSplitPane.TOP);
		upPanel.add(jPanel1, JSplitPane.BOTTOM);
		upPanel.setOneTouchExpandable(true);
		chartPanel = new JPanel();

		chartPanel.setLayout(new BorderLayout());

		chartPanel.add(graph);

		legendPanel = new JPanel();
		legendPanel.setLayout(new java.awt.FlowLayout());
		legendPanel.setBackground(Color.WHITE);

		chartPanel.add(legendPanel, BorderLayout.SOUTH);

		throttlePanel.add(chartPanel, BorderLayout.CENTER);
		throttlePanel.add(graphToolBar, BorderLayout.SOUTH);

		commandToolBar.add(refreshButton);
		commandToolBar.add(createNetWorkButton);
		cancelButton.setText("Cancel");
		commandToolBar.add(cancelButton);
		commandToolBar.add(imageSnapshotButton);

		List<String> displaySelectedInteractionTypes = jPreferencePanel
				.getDisplaySelectedInteractionTypes();
		columnLabels = new String[firstFourColumnLabels.length
				+ displaySelectedInteractionTypes.size()];

		for (int i = 0; i < firstFourColumnLabels.length; i++)
			columnLabels[i] = firstFourColumnLabels[i];
		for (int i = 0; i < displaySelectedInteractionTypes.size(); i++)
			columnLabels[i + 4] = displaySelectedInteractionTypes.get(i)
					+ Constants.COLUMNLABELPOSTFIX;
		tableColumns = new TableColumn[columnLabels.length];
		previewTableModel = new PreviewTableModel();
		detailTable.setModel(previewTableModel);

		TableColumnModel model = detailTable.getColumnModel();
		for (int i = 0; i < columnLabels.length; i++) {

			tableColumns[i] = model.getColumn(i);
		}

	} // end of initComponents

	/**
	 * Generate the data to draw the curve.
	 * 
	 * @param min
	 * @param max
	 * @param selectedId
	 * @param active
	 * @return
	 */

	private XYSeriesCollection createCollection(double min, double max,
			int selectedId, boolean active) {
		 
		boolean needDraw = false;
		if (hits != null && hits.size() > 0)
			updateLegendList();
		else
		{
			this.legendList.clear();
			thresholdTypes.removeAllItems();
			CellularNetWorkElementInformation.clearConfidenceTypes();
		}
		
		double maxConfidenceValue = CellularNetWorkElementInformation.getMaxConfidenceValue();
		if (maxConfidenceValue > 1)
		{
			int a = (int)Math.log10(maxConfidenceValue);
			double b = maxConfidenceValue / (Math.pow(10, a));
			double maxX = Math.round(b);
			maxX =  maxX * (Math.pow(10, a));			 
			long smallestIncrement = (long)maxX/100;		 	    
			 
		    CellularNetWorkElementInformation.setSmallestIncrement(smallestIncrement);
		    CellularNetWorkElementInformation.setMaxX((long)maxX);
		     
		}
		else
		{
			    CellularNetWorkElementInformation.setSmallestIncrement(0.01);
			    CellularNetWorkElementInformation.setMaxX(1);			     
		}
		
		XYSeries dataSeries = new XYSeries("Total Distribution");	 
		int binSize = CellularNetWorkElementInformation.getBinNumber();
		XYSeriesCollection plots = new XYSeriesCollection();
		try {

			Map<String, XYSeries> interactionDataSeriesMap = new HashMap<String, XYSeries>();
			List<String> displaySelectedInteractionTypes = jPreferencePanel
					.getDisplaySelectedInteractionTypes();

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
					DSGeneMarker marker = cellularNetWorkElementInformation
							.getdSGeneMarker();

					if (marker == null || marker.getGeneId() == -1
							|| cellularNetWorkElementInformation.isDirty())
						continue;
					needDraw = true;
					int[] distributionArray = cellularNetWorkElementInformation
							.getDistribution(displaySelectedInteractionTypes);

					for (int i = 0; i < binSize; i++)
						basketValues[i] += distributionArray[i];

					for (String interactionType : displaySelectedInteractionTypes) {
						int[] interactionDistribution = cellularNetWorkElementInformation
								.getInteractionDistribution(interactionType);
						int[] interactionBasketValues = interactionBasketValuesMap
								.get(interactionType);
						for (int i = 0; i < binSize; i++)
							interactionBasketValues[i] += interactionDistribution[i];

					}

				}
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

			if (hits != null && hits.size() > 0 && needDraw == true) {

				plots.addSeries(dataSeries);
				for (String interactionType : displaySelectedInteractionTypes) {
					plots.addSeries(interactionDataSeriesMap
							.get(interactionType));
				}

			} else
			{
				this.legendList.clear();
				thresholdTypes.removeAllItems();
				CellularNetWorkElementInformation.clearConfidenceTypes();
			}
		 
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
		return plots;
	}

	/**
	 * Respond to the change of the Threshold Text field.
	 * 
	 * @param
	 */
	private void jThresholdTextField_actionPerformed() {
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
			     * 100 / CellularNetWorkElementInformation.getMaxX();
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

		if (jPreferencePanel.isMarkerJCheckBoxSelected())
			model.addColumn(tableColumns[0]);
		if (jPreferencePanel.isGeneJCheckBoxSelected())
			model.addColumn(tableColumns[1]);
		if (jPreferencePanel.isGeneTypeLCheckBoxSelected())
			model.addColumn(tableColumns[2]);
		if (jPreferencePanel.isGoTermJCheckBoxSelected())
			model.addColumn(tableColumns[3]);

		List<String> displaySelectedInteractionTypes = jPreferencePanel
				.getDisplaySelectedInteractionTypes();
		for (int i = 4; i < columnLabels.length; i++) {

			if (displaySelectedInteractionTypes.contains(columnLabels[i]
					.substring(0,

					columnLabels[i].length()
							- Constants.COLUMNLABELPOSTFIX.length()))) {
				model.addColumn(tableColumns[i]);
			}

		}

		detailTable.tableChanged(new TableModelEvent(previewTableModel));
		detailTable.repaint();

		if (needRedraw) {
			drawPlot(createCollection(0, 1, 1, true), false, true);
			throttlePanel.repaint();

		}
	}

	private void setThresholdSliderValue() {
		double threshhold = 0;
		if (hits != null && hits.size() > 0)
			threshhold = hits.get(0).getThreshold();
		double newSliderValue = threshhold
				*100/CellularNetWorkElementInformation.getMaxX();

		thresholdSlider.setValue((int) newSliderValue);

	}

	/**
	 * Create the plot for the throttle graph.
	 * 
	 * @param plots
	 * @param title
	 */
	public void drawPlot(final XYSeriesCollection plots, boolean needRedraw,
			boolean needCreateLegendItems) {		
		 
		if (plots == null) {
			return;
		}

		boolean isToolTipEnabled = true;
		XYPlot xyPlot = null;
		XYLineAndShapeRenderer renderer = null;

		if (needRedraw || plots.getSeriesCount() == 0) {
			String context = jPreferencePanel.getSelectedContext();
			String version = jPreferencePanel.getSelectedVersion();

			if (plots.getSeriesCount() > 0) {
				tgPreference.setTitle("Throttle Graph(" + context + " v"
						+ version + ")");
			} else
				tgPreference.setTitle("Throttle Graph");

			Object selectedType = thresholdTypes.getSelectedItem();
			String xAxisLabel = "likelihood";
			if (selectedType != null && !selectedType.toString().equals(""))
				xAxisLabel  = selectedType.toString();
			chart = ChartFactory.createXYLineChart(tgPreference.getTitle(),
					xAxisLabel, "#interactions", plots,
					PlotOrientation.VERTICAL, true, true, true);
			xyPlot = (XYPlot) chart.getPlot();
			chart.setBackgroundPaint(Color.white);
			Color c = UIManager.getColor("Panel.background");
			if (c != null) {
				xyPlot.setBackgroundPaint(c);
			} else {
				c = Color.white;
			}
			xyPlot.setBackgroundPaint(c);

			renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
			renderer.setShapesVisible(true);
			renderer.setShapesFilled(true);
			if (isToolTipEnabled) {

				renderer.setToolTipGenerator(new XYToolTipGenerator() {

					public String generateToolTip(XYDataset dataset,
							int series, int item) {
						String resultStr = "";
						
						String label = (String) (dataset.getSeriesKey(series));
								 
						double x = dataset.getXValue(series, item);
						if (Double.isNaN(x)
								&& dataset.getX(series, item) == null) {
							return resultStr;
						}

						double y = dataset.getYValue(series, item);
						if (Double.isNaN(y)
								&& dataset.getX(series, item) == null) {
							return resultStr;
						}
						String xStr = myFormatter.format(x);

						return resultStr = label + ": ([" + xStr + ", "
								+ myFormatter.format(x + CellularNetWorkElementInformation.getSmallestIncrement()) + "], "
								+ (int) y + ")";
					}
				});
			}

			xyPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			xyPlot.setDomainGridlinePaint(Color.white);
			xyPlot.setRangeGridlinePaint(Color.white);
			xyPlot.setDomainCrosshairVisible(true);
			xyPlot.setDomainCrosshairLockedOnData(true);

			// change the auto tick unit selection to integer units only...
			NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
			rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
			 
			ValueAxis xAxis = xyPlot.getDomainAxis();
			 xAxis.setAutoRange(true);
            
			 if (CellularNetWorkElementInformation.getMaxX() <= 1)			  
				 xAxis.setRange(0, 1);
          
			// OPTIONAL CUSTOMISATION COMPLETED.

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
							       /CellularNetWorkElementInformation.getMaxX() * 100; 
										 
						thresholdSlider.setValue((int) newSliderValue);
					}
				}

			});
			
			chart.addChangeListener(new ChartChangeListener() {
	            
				public void chartChanged(ChartChangeEvent event) {
					if (event instanceof TitleChangeEvent)
						tgPreference.setTitle(chart.getTitle().getText());
				}

			});

		} else {
			xyPlot = (XYPlot) chart.getPlot();
			xyPlot.setDataset(plots);
			renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
			chart.setTitle(tgPreference.getTitle());
		}

		for (int i = 0; i < xyPlot.getDatasetCount(); i++) {
			renderer.setSeriesLinesVisible(i, true);
		}

		for (int i = 0; i < plots.getSeriesCount(); i++) {
			renderer.setSeriesVisible(i, true);
		}
		renderer.setSeriesVisibleInLegend(true);

		LegendItemCollection legendItems = renderer.getLegendItems();
		for (int i = 0; i < legendItems.getItemCount(); i++) {
			LegendItem lg = legendItems.get(i);
			LegendObject lo = legendList.get(lg.getLabel());
			lo.setColor((Color) lg.getFillPaint());

		}

		renderer.setSeriesVisibleInLegend(false);
		graph.setChart(chart);
   
		for (int i = 0; i < legendItems.getItemCount(); i++) {
			LegendItem lg = legendItems.get(i);
			LegendObject lo = legendList.get(lg.getLabel());
			if (!lo.isChecked())
				renderer.setSeriesVisible(lg.getSeriesIndex(), false);

		}

		if (needCreateLegendItems)
			createLegendPanel();

		setThresholdSliderValue();
		thresholdSlider_stateChanged();
	 
	} // end of drawPlot

	private void thresholdSlider_stateChanged() {
		int value = thresholdSlider.getValue();
		XYPlot plot = chart.getXYPlot();

		double lowValue = (double) value * 
			   CellularNetWorkElementInformation.getMaxX()/100;

		plot.setDomainCrosshairValue(lowValue);
		String s = myFormatter.format(lowValue);
		thresholdTextField.setText(s);

		for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
			cellularNetWorkElementInformation.setThreshold(lowValue);

		}

		previewTableModel.fireTableDataChanged();
		detailTable.revalidate();
	}

	private void updateProgressBar(final double percent, final String text) {
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

	public Vector<CellularNetWorkElementInformation> getHits() {
		return hits;
	}

	private void cancelTheAction(ActionEvent e) {
		cancelAction = true;
		createNetWorkButton.setEnabled(true);
	}
	
	public boolean isQueryRuning()
	{
		return isQueryRuning;
	}


	private boolean isProcessingAllowed() {
		boolean isAllow = true;
		if (isQueryRuning) {
			String theMessage = "You cannot delete or add markers during a query run. Would you like to stop the current query? \nClick on \"Yes\" and you can change your marker list. \nClick on \"No\" and continue with the current selection.";

			int result = JOptionPane.showConfirmDialog((Component) null,
					theMessage, "alert", JOptionPane.YES_NO_OPTION);
			isAllow = false;
			if (result == JOptionPane.YES_OPTION)
				cancelAction = true;
		}

		return isAllow;
	}

	private void previewSelectionsHandler(ActionEvent e) {

		refreshButton.setEnabled(false);
		cancelAction = false;
		Runnable r = new Runnable() {
			public void run() {

				InteractionsConnectionImpl interactionsConnection = new InteractionsConnectionImpl();

				try {

					String context = null;
					if (jPreferencePanel.getContextJList().getSelectedValue() != null) {
						context = jPreferencePanel.getContextJList()
								.getSelectedValue().toString().split(" \\(")[0]
								.trim();
					}

					String version = null;
					if (jPreferencePanel.getVersionJList().getSelectedValue() != null)
						version = ((VersionDescriptor) jPreferencePanel
								.getVersionJList().getSelectedValue())
								.getVersion();

					if (context == null
							|| context.trim().equals("")
							|| context
									.equalsIgnoreCase(Constants.SELECTCONTEXT)) {
						JOptionPane
								.showMessageDialog(
										null,
										"Please go to Preferences window to make sure that you select the correct interactome.",

										"Information",
										JOptionPane.INFORMATION_MESSAGE);
						refreshButton.setEnabled(true);
						return;
					}

					if (version == null
							|| version.trim().equals("")
							|| version
									.equalsIgnoreCase(Constants.SELECTVERSION)) {
						JOptionPane
								.showMessageDialog(
										null,
										"Please go to Preferences window to make sure that you select the correct interactome version.",

										"Information",
										JOptionPane.INFORMATION_MESSAGE);
						refreshButton.setEnabled(true);
						return;
					}

					updateProgressBar(0, "Querying the Knowledge Base...");

					int retrievedQueryNumber = 0;
					isQueryRuning = true;	
					boolean hasRereivedRecord = false;
					for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
						retrievedQueryNumber++;
						updateProgressBar(((double) retrievedQueryNumber)
								/ hits.size(), "Querying the Knowledge Base...");

						DSGeneMarker marker = cellularNetWorkElementInformation
								.getdSGeneMarker();
						if (cancelAction) {
							break;
						}

						if (marker != null && marker.getGeneId() != 0
								&& cellularNetWorkElementInformation.isDirty()) {
							List<InteractionDetail> interactionDetails = null;

							try {

								if (interaction_flag == 0) {
									interactionDetails = interactionsConnection
											.getInteractionsByEntrezIdOrGeneSymbol_1(
													marker, context, version);
								} else {
									interactionDetails = interactionsConnection
											.getInteractionsByEntrezIdOrGeneSymbol_2(
													marker, context, version);
								}

							} catch (UnAuthenticatedException uae) {
								cancelAction = true;
								break;
							} catch (ConnectException ce) {
								JOptionPane
										.showMessageDialog(
												null,
												"No service running. Please check with the administrator of your service infrastructure.",
												"Error",
												JOptionPane.ERROR_MESSAGE);
								cancelAction = true;
								break;

							} catch (SocketTimeoutException se) {
								JOptionPane
										.showMessageDialog(
												null,
												"No service running. Please check with the administrator of your service infrastructure.",
												"Error",
												JOptionPane.ERROR_MESSAGE);
								cancelAction = true;
								break;

							} catch (IOException ie) {
								JOptionPane
										.showMessageDialog(
												null,
												"CNKB service has an internal error, Please contact with geWorkbench developer ...",
												"Error",
												JOptionPane.ERROR_MESSAGE);
								cancelAction = true;
								break;

							}
							cellularNetWorkElementInformation.setDirty(false);
							cellularNetWorkElementInformation
									.setInteractionDetails(interactionDetails);
							
						}
						else
						{
							hasRereivedRecord =true;	
						}

					}
					

					
					List<Short> confidenceTypeList = CellularNetWorkElementInformation.getConfidenceTypeList();
					Object selectItem = thresholdTypes.getSelectedItem();
					thresholdTypes.removeAllItems();
					for(int i=0; i<confidenceTypeList.size(); i++)
					{
						Short typeId =  confidenceTypeList.get(i);
						String typeName = CellularNetworkPreferencePanel.interactionConfidenceTypeMap.get(typeId.toString());
						thresholdTypes.addItem(typeName);						
					}
					if (hasRereivedRecord &&  thresholdTypes.getItemCount() > 0 && selectItem != null)
						thresholdTypes.setSelectedItem(selectItem.toString());
					else
					{
						if (hits.size() > 0 && thresholdTypes.getItemCount() > 0 && selectItem != null)
						   thresholdTypes.setSelectedIndex(0);
					}


					if (!cancelAction) {
						updateProgressBar(1, "Query is finished.");
						boolean needNewProperties = false;
						 if (tgPreference.getContext() == null
								|| !tgPreference.getContext().equals(context)
								|| !tgPreference.getVersion().equals(version) || tgPreference.getTitle().equals("Throttle Graph")) {
							 needNewProperties = true;
							 tgPreference.setContext(context);
							 tgPreference.setVersion(version);							 
						} 
						 
					
						drawPlot(createCollection(0, 1, 1, true), needNewProperties,
								true);
						throttlePanel.repaint();

					} else {
						updateProgressBar(1, "Stopped");
					}

					previewTableModel.fireTableDataChanged();
					detailTable.revalidate();

				} catch (java.util.ConcurrentModificationException ce) {
					// this exception may be caught when marker set in
					// selector panel is deactivated.
					updateProgressBar(1, "Stopped");
				} catch (Exception e) {
					log.error("$Runnable.run()", e); //$NON-NLS-1$					 

				} finally {
					// try to close connection
					isQueryRuning = false;
					interactionsConnection.closeDbConnection();
					refreshButton.setEnabled(true);
				}
			}
		};

		// SwingUtilities.invokeLater(r);
		Thread thread = new Thread(r);
		thread.start();
	}   

	/**
	 * Create a connection with the server.
	 */
	private void loadApplicationProperty() {
		Properties iteractionsProp = new Properties();
		try {
			iteractionsProp
					.load(new FileInputStream(Constants.PROPERTIES_FILE));

			timeout = new Integer(
					iteractionsProp
							.getProperty(Constants.INTERACTIONS_SERVLET_CONNECTION_TIMEOUT));
		 
			interaction_flag = new Integer(iteractionsProp
					.getProperty(Constants.INTERACTIONS_FLAG));

			String interactionsServletUrl = PropertiesManager.getInstance().getProperty(this.getClass(),
					"url", "");
			if (interactionsServletUrl == null
					|| interactionsServletUrl.trim().equals("")) {

				interactionsServletUrl = iteractionsProp
						.getProperty(Constants.INTERACTIONS_SERVLET_URL);
			}
			ResultSetlUtil.setUrl(interactionsServletUrl);
			ResultSetlUtil.setTimeout(timeout);
		} catch (java.io.IOException ie) {
			log.error(ie.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private void initGeneTypeMap() {
		geneTypeMap = new HashMap<String, String>();
		geneTypeMap.put(Constants.TF, Constants.TRANSCRIPTION_FACTOR);
		geneTypeMap.put(Constants.K, Constants.KINASE);
		geneTypeMap.put(Constants.P, Constants.PHOSPHATASE);
	}

	public void initDetailTable() {

		try {

			List<String> allInteractionTypes = jPreferencePanel
					.getAllInteractionTypes();

			if (allInteractionTypes != null && allInteractionTypes.size() > 0) {

				columnLabels = new String[firstFourColumnLabels.length
						+ allInteractionTypes.size()];

				for (int i = 0; i < firstFourColumnLabels.length; i++)
					columnLabels[i] = firstFourColumnLabels[i];
				for (int i = 0; i < allInteractionTypes.size(); i++)
					columnLabels[i + 4] = allInteractionTypes.get(i)
							+ Constants.COLUMNLABELPOSTFIX;

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

			needRedraw = false;
			updateColumnPref();
			needRedraw = true;

		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

	private DefaultTableModel activeMarkersTableModel = new DefaultTableModel() {

		private static final long serialVersionUID = 2700694309070316774L;

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
						} else { // this should never happen
							return null;
						}
					}
					case 2: {
						GeneOntologyTree instance = GeneOntologyTree.getInstance();
						if(instance==null) return "pending";

						return GeneOntologyUtil.checkMarkerFunctions(value);
					}
					case 3: {

						return cachedPreviewData.get(row).get(3);
					}
					default:
						return "loading ...";
					}
				}

			}

			return "loading ...";
		}
	};

	private PreviewTableModel previewTableModel = new PreviewTableModel();

	private class PreviewTableModel extends DefaultTableModel

	{

		private static final long serialVersionUID = -3314439294428139176L;

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

			if (index >= 0 && index < columnLabels.length)
				return columnLabels[index];
			else
				return "";

		}

		/* get the Object data to be displayed at (row, col) in table */
		@Override
		public Object getValueAt(int row, int col) {

			if (hits==null || hits.size() == 0)
				return null;
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
				String interactionType = columnLabels[col].substring(0,
						columnLabels[col].length() -

						Constants.COLUMNLABELPOSTFIX.length());
				Integer num = hit.getInteractionNum(interactionType);
				if (num != null)
					return num;
				else
					return 0;

			}

		}

		/* returns the Class type of the column c */
		@Override
		public Class<?> getColumnClass(int c) {
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

	private class IntegerRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 1399618132721043696L;

		private Border unselectedBorder = null;

		private Border selectedBorder = null;

		public IntegerRenderer() {
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {

			row = detailTable.convertRowIndexToModel(row);
			TableColumn tableColumn = detailTable.getColumnModel().getColumn(
					column);

			if (hits != null && hits.size() > row) {
				CellularNetWorkElementInformation cellularNetWorkElementInformation = hits
						.get(row);
				boolean isDirty = cellularNetWorkElementInformation.isDirty();
				if (isDirty) {
					setBackground(table.getBackground());
					setForeground(Color.red);
					String headerStr = tableColumn.getHeaderValue().toString();
					if (!jPreferencePanel.getNetworkSelectedInteractionTypes()
							.contains(
									headerStr.substring(0,

									headerStr.length()
											- Constants.COLUMNLABELPOSTFIX
													.length()))) {
						setBackground(Color.gray);
					}
					if (isSelected) {
						if (selectedBorder == null) {
							selectedBorder = BorderFactory.createMatteBorder(2,
									5, 2, 5, table.getSelectionBackground());
						}
						setBorder(selectedBorder);

						setText("<html><font color=blue><i>" + "Unknown"
								+ "</i></font></html>");
					} else {

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

					setBackground(table.getBackground());
					// setBackground(Color.white);
					String headerStr = tableColumn.getHeaderValue().toString();
					if (!jPreferencePanel.getNetworkSelectedInteractionTypes()
							.contains(
									headerStr.substring(0,

									headerStr.length()
											- Constants.COLUMNLABELPOSTFIX
													.length()))) {
						setBackground(Color.gray);
					}

					if (isSelected) {
						if (selectedBorder == null) {
							selectedBorder = BorderFactory.createMatteBorder(2,
									5, 2, 5, table.getSelectionBackground());
						}
						setBorder(selectedBorder);

						setText("<html><font color=blue><b>" + color
								+ "</b></font></html>");
						setToolTipText(color.toString());
					} else {

						setText("<html><font color=blue><b>" + color
								+ "<b></font></html>");
						if (unselectedBorder == null) {
							unselectedBorder = BorderFactory.createMatteBorder(
									2, 5, 2, 5, table.getBackground());
						}
						setBorder(unselectedBorder);
						setToolTipText(color.toString());
					}
				}
			}

			return this;
		}
	}

	private class ColorRenderer extends JLabel implements TableCellRenderer {

		private static final long serialVersionUID = 8232307195673766041L;

		Border unselectedBorder = null;

		Border selectedBorder = null;

		public ColorRenderer() {

			setOpaque(true); // MUST do this for background to show up.

		}

		private String insertLineBreaker(Object value) {
			String toolTipText = "";
			String str = null;
			if (value == null)
				return toolTipText;
			str = value.toString();
			if (str.length() <= 100)
				toolTipText = str;
			else {
				int startIndex = 0;
				while (startIndex < str.length()) {
					int endIndex = startIndex + 100;
					if (endIndex < str.length()) {
						while (str.charAt(endIndex) != ' ') {
							endIndex++;
							if (endIndex == str.length()) {
								endIndex--;
								break;
							}
						}

						toolTipText += str.substring(startIndex, endIndex)
								+ "<br>";
					} else
						toolTipText += str.substring(startIndex, str.length());

					startIndex = endIndex;
				}
			}

			return toolTipText;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column) {

			row = detailTable.convertRowIndexToModel(row);
			TableColumn tableColumn = detailTable.getColumnModel().getColumn(
					column);
			CellularNetWorkElementInformation cellularNetWorkElementInformation = null;
			if (hits != null && hits.size() > row) {
				cellularNetWorkElementInformation = hits.get(row);
				if (cellularNetWorkElementInformation == null)
					return this;
				boolean isDirty = cellularNetWorkElementInformation.isDirty();
				if (isDirty) {
					if (isSelected) {
						if (selectedBorder == null) {
							selectedBorder = BorderFactory.createMatteBorder(2,
									5, 2, 5, table.getSelectionBackground());
						}
						setBorder(selectedBorder);

						setText("<html><font color=blue><i>" + color
								+ "</i></font></html>");
					} else {

						setForeground(Color.black);

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

						setText("<html><font color=blue><b>" + color
								+ "</b></font></html>");
					} else {

						setForeground(Color.black);
						setText("<html><font color=blue><b>" + color
								+ "<b></font></html>");
						if (unselectedBorder == null) {
							unselectedBorder = BorderFactory.createMatteBorder(
									2, 5, 2, 5, table.getBackground());
						}
						setBorder(unselectedBorder);
					}
				}

				String headerStr = tableColumn.getHeaderValue().toString();
				if (headerStr.equalsIgnoreCase(Constants.GENETYPELABEL)) {
					if (color != null
							&& !color.toString().trim().equalsIgnoreCase("")) {
						String s = geneTypeMap.get(color);
						if (s != null)
							color = s;
					}
				} else if (headerStr.equalsIgnoreCase(Constants.GENELABEL)) {
					String[] list = AnnotationParser.getInfo(
							cellularNetWorkElementInformation.getdSGeneMarker()
									.getLabel(), AnnotationParser.DESCRIPTION);
					if (list != null && list.length > 0)
						color = list[0];
				}
				String toolTipText = insertLineBreaker(color);
				setToolTipText("<html>" + toolTipText + "<html>");

			}

			return this;
		}
	}

	private class InitPrefWorker extends SwingWorker<Void, Void> implements
			Observer {

		ProgressBar pb = null;

		InitPrefWorker() {
			super();

		}

		@Override
		protected void done() {
			if (this.isCancelled()) {
				log.info("Init task is cancel.");

			} else {
				log.info("Init task is done.");

				pb.dispose();

			}
		}

		@Override
		protected Void doInBackground() {
			pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);

			pb.addObserver(this);
			pb.setTitle("Initialize preference data");
			pb.setMessage("Retrieve data from database ...");
			pb.start();
			pb.toFront();

			try {
				jPreferencePanel.initPreferences();
				if (!this.isCancelled())
					initDetailTable();

			} catch (Exception ex) {
				log.error(ex.getMessage());
			}

			return null;
		}

		public void update(Observable o, Object arg) {
			cancel(true);
		}

	}

	private class CreateNetworkHandler extends Thread implements Observer {
		AdjacencyMatrix matrix = null;
		ProgressBar pb = null;
		boolean cancel = false;

		public CreateNetworkHandler(ProgressBar createNetworkPb) {
			super();
			this.pb = createNetworkPb;
		}

		public void run() {
			pb.addObserver(this);
			createNetworks();
			//pb.dispose();
		}
		
		private void createNetworks()  { 

			double threshold = 0;
			try {
				threshold = new Double(thresholdTextField.getText().trim());
			} catch (NumberFormatException e1) {
				createNetWorkButton.setEnabled(true);
				pb.dispose();
				JOptionPane.showMessageDialog(null, "The Threshold field is not a number.",
						"Please check your input.", JOptionPane.ERROR_MESSAGE);
				
				return;
			}			
			 
			DSItemList<DSGeneMarker> markers = dataset.getMarkers();
			DSItemList<DSGeneMarker> copy = new CSItemList<DSGeneMarker>();
			copy.addAll(markers);
			CellularNetworkKnowledgeWidget.EntrezIdComparator eidc = new CellularNetworkKnowledgeWidget.EntrezIdComparator();
			Collections.sort(copy, eidc);

			Map<String, List<DSGeneMarker>> geneNameToMarkerMap = AffyAnnotationUtil
					.getGeneNameToMarkerMapping(dataset);

			AdjacencyMatrix matrix = new AdjacencyMatrix(null, dataset,
					CellularNetworkPreferencePanel.interactionTypeSifMap, CellularNetworkPreferencePanel.interactionEvidenceMap);
			setAdjacencyMatrix(matrix);
			AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;

			int interactionNum = 0;
			boolean createNetwork = false;
			boolean needBreak = false;			 
			boolean isGene2InMicroarray = true;
			StringBuffer historyStr = new StringBuffer();
			boolean isRestrictToGenesPresentInMicroarray = jPreferencePanel
					.isNetworkJCheckBox1Selected();

			short usedConfidenceType = CellularNetWorkElementInformation.getUsedConfidenceType();
			
			for (CellularNetWorkElementInformation cellularNetWorkElementInformation : hits) {
				if (needBreak)
					break;
				if (cellularNetWorkElementInformation.isDirty() == true)
					continue;
				ArrayList<InteractionDetail> arrayList = cellularNetWorkElementInformation
						.getSelectedInteractions(jPreferencePanel
								.getNetworkSelectedInteractionTypes());

				List<String> networkSelectedInteractionTypes = jPreferencePanel
						.getNetworkSelectedInteractionTypes();
				if (networkSelectedInteractionTypes.size() > 0)
					historyStr
							.append("           ")
							.append(cellularNetWorkElementInformation
									.getdSGeneMarker().getLabel()).append(": \n");
				for (String interactionType : networkSelectedInteractionTypes)
					historyStr
							.append("\t Include ")
							.append(interactionType)
							.append(": ")
							.append(cellularNetWorkElementInformation
									.getInteractionNum(interactionType))
							.append("\n");

				if (arrayList == null || arrayList.size() <= 0) {
					continue;
				}				
				
				DSGeneMarker marker1 = cellularNetWorkElementInformation.getdSGeneMarker();
				
				for (InteractionDetail interactionDetail : arrayList) {
					if (isCancelled() == true)
						return;				 
					isGene2InMicroarray = true;
					DSGeneMarker marker = new CSGeneMarker();
					String mid2 = interactionDetail.getdSGeneId();						 
					AdjacencyMatrix.Node node2 = null;

					if (interactionDetail.getDbSource().equalsIgnoreCase(
							Constants.ENTREZ_GENE)) {
						try {
							marker.setGeneId(new Integer(mid2));
						} catch (NumberFormatException ne) {
							if (log.isErrorEnabled()) {
								log.error("ms_id2 is expect to be an integer: "
										+ mid2
										+ "This interaction is going to be dropped");
							}
							continue;
						}
						int index = Collections.binarySearch(copy, marker, eidc);
						if (index >= 0) {							 					 
							node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, copy.get(index).getGeneName());
						} else {
							isGene2InMicroarray = false;
						}

					} else {
						Collection<DSGeneMarker> dSGeneMarkerList = geneNameToMarkerMap
								.get(interactionDetail.getdSGeneName());
						if (dSGeneMarkerList != null && !dSGeneMarkerList.isEmpty()) {
							for (DSGeneMarker dSGeneMarker : dSGeneMarkerList) {								 
								node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, dSGeneMarker.getGeneName());								 
								if (interactionDetail.getDbSource()
										.equalsIgnoreCase(Constants.UNIPORT)) {
									Set<String> SwissProtIds = new HashSet<String>();
									String[] ids = AnnotationParser.getInfo(dSGeneMarker
											.getLabel(), AnnotationParser.SWISSPROT);
									for (String s : ids) {
										SwissProtIds.add(s.trim());
									}
									if (SwissProtIds.contains(interactionDetail
											.getdSGeneId())) {										 
										break;
									}
								} else { 
									 
									break;
								}						
								 
							}
						} else {
							isGene2InMicroarray = false;
						}
					}

					if (isGene2InMicroarray == false) {
						log.info("Marker " + interactionDetail.getdSGeneId()
								+ " does not exist at the dataset. ");
						if (isRestrictToGenesPresentInMicroarray)
							continue;

						if (interactionDetail.getdSGeneName() != null
								&& !interactionDetail.getdSGeneName().trim()
										.equals("")
								&& !interactionDetail.getdSGeneName().trim()
										.equals("null")) {					 
							node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, interactionDetail.getdSGeneName(), 0);
						} else {					 
							node2 = new AdjacencyMatrix.Node(NodeType.STRING, mid2);
						}
					}					
					AdjacencyMatrix.Node node1 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL, marker1.getGeneName());					
				 
					String shortNameType = CellularNetworkPreferencePanel.interactionTypeSifMap
							.get(interactionDetail.getInteractionType());
				 
				    matrix.add(node1, node2, new Float(interactionDetail.getConfidenceValue(usedConfidenceType)), shortNameType, interactionDetail.getEvidenceId());
	 
					interactionNum++;			 
				}
			} // end for loop

			if (interactionNum > 0) {
				createNetwork = true;
			} else if (interactionNum == 0) {
				JOptionPane.showMessageDialog(null,
						"No interactions exist in the current database.",
						"Empty Set", JOptionPane.ERROR_MESSAGE);
				createNetwork = false;

			}
			if (createNetwork == true) {

				adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix,
						threshold, "Adjacency Matrix", dataset.getLabel(), dataset);
			 
				String history = "Cellular Network Parameters: \n"
						+ "      URL Used:     "
						+ ResultSetlUtil.getUrl() + "\n"
						+ "      Selected Interactome:     "
						+ jPreferencePanel.getSelectedContext() + "\n"
						+ "      Selected Version:     "
						+ jPreferencePanel.getSelectedVersion() + "\n"
						+ "      Threshold:     " + thresholdTextField.getText()
						+ "\n" + "      Selected Marker List: \n" + historyStr
						+ "\n";
				HistoryPanel.addToHistory(adjacencyMatrixdataSet, history);

				if (isCancelled())
					return;
				else {
					pb.setTitle("Draw cytoscape graph");
					pb.setMessage("Draw cytoscape graph ...");

				}
				publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
						"Adjacency Matrix Added", null, adjacencyMatrixdataSet));

			}

			if (!isCancelled()) {
				log.info("task is completed");
				createNetWorkButton.setEnabled(true);
				pb.dispose();
			} else {
				log.info("task is canceled");

			}

		}// end of createNetworks
		

	 
		private void setAdjacencyMatrix(AdjacencyMatrix matrix) {
			this.matrix = matrix;
		}

		private boolean isCancelled() {
			return this.cancel;
		}

		private void cancel(boolean cancel) {
			this.cancel = cancel;
		}	 
		

		public void update(Observable o, Object arg) {
			cancel(true);
			if (pb.getTitle().equals("Draw cytoscape graph")) {
				pb.dispose();
				publishAdjacencyMatrixCancelEvent(new AdjacencyMatrixCancelEvent(
						matrix) );

			} else {
				pb.dispose();

			}
			createNetWorkButton.setEnabled(true);
			log.info("Create network canceled.");
		}

	}
	
	 

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

 
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
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		log.debug("end GeneSelectorEvent at CNKW");
	}

	@SuppressWarnings("unchecked")
	synchronized void processData(DSPanel<DSGeneMarker> panel) {
		log.debug("start processData");
		DSDataSet<?> ds = ProjectPanel.getInstance().getDataSet();
		boolean needDraw = false;
		if (ds == null || !(ds instanceof DSMicroarraySet))
			return;

		if ((dataset == null) || (dataset.hashCode() != ds.hashCode())) {
			dataset = (DSMicroarraySet) ds;
			if (dataset.getValuesForName(Constants.CNKB_HITS) == null)
				dataset.addNameValuePair(Constants.CNKB_HITS,
						new Vector<CellularNetWorkElementInformation>());
			hits = (Vector<CellularNetWorkElementInformation>) (dataset
					.getValuesForName(Constants.CNKB_HITS)[0]);

			if (dataset.getValuesForName(Constants.CNKB_SELECTION_INDEX) == null)
				dataset.addNameValuePair(Constants.CNKB_SELECTION_INDEX,
						new Integer(0));
			cnkbSelectionIndex = (Integer) (dataset
					.getValuesForName(Constants.CNKB_SELECTION_INDEX)[0]);

			if (dataset
					.getValuesForName(Constants.CNKB_PREFERENCE) == null)
				dataset.addNameValuePair(
						Constants.CNKB_PREFERENCE,
						new CellularNetworkPreference("Throttle Graph"));
			tgPreference = (CellularNetworkPreference) dataset
					.getValuesForName(Constants.CNKB_PREFERENCE)[0];

			needDraw = true;

		}

		int currentCount = 0;
		if(hits!=null) currentCount = hits.size();

		if (panel != null) {
			if (panel.size() == 0) {
				allGenes.clear();
				selectedGenes.clear();
			}
			allGenes.clear();
			Set<DSGeneMarker> orderedSet = new HashSet<DSGeneMarker>();

			for (DSGeneMarker marker : panel) {
				orderedSet.add(marker);
			}
			updateAllMarkersList(orderedSet, hits);

			activeMarkersTableModel.fireTableDataChanged();

			previewTableModel.fireTableDataChanged();
			detailTable.revalidate();

			checkSelectedTableWithNewDataSet(panel);

			if (needDraw == true || hits == null || hits.size() == 0
					|| hits.size() != currentCount) {
				drawPlot(createCollection(0, 1, 1, true), false, true);
				throttlePanel.repaint();
			}

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

	private void updateAllMarkersList(Set<DSGeneMarker> orderedSet,
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

	@Publish
	public AdjacencyMatrixCancelEvent publishAdjacencyMatrixCancelEvent(
			AdjacencyMatrixCancelEvent ae) {
		return ae;
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent pe) {
		return pe;
	}

	@Publish
	public ImageSnapshotEvent createImageSnapshot() {
		Dimension panelSize = chartPanel.getSize();
		BufferedImage image = new BufferedImage(panelSize.width,
				panelSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		chartPanel.paint(g);
		ImageIcon icon = new ImageIcon(image, "CNKB Throttle Graph");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"CNKB Throttle Graph Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	private static class EntrezIdComparator implements Comparator<DSGeneMarker> {
		@Override
		public int compare(DSGeneMarker m1, DSGeneMarker m2) {
			return m1.getGeneId() - m2.getGeneId();
		}
	}

	private class LengendCheckBox extends JCheckBox {

		private static final long serialVersionUID = -8657497943937239528L;

		public LengendCheckBox(String label, boolean isSelected) {

			super(label, isSelected);

			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent me) {

					if (me.getSource() instanceof LengendCheckBox) {
						LengendCheckBox cb = (LengendCheckBox) me.getSource();
						String label = ((LengendCheckBox) me.getSource())
								.getText();
						LegendObject item = legendList.get(label);
						if (cb.isSelected())
							item.setSelected(true);
						else {
							item.setSelected(false);
							item.setColor(Color.GRAY);
						}

						drawPlot(createCollection(0, 1, 1, true), false, false);
						throttlePanel.repaint();

					}
				}
			});

		}
	}

	private void updateLegendList() {
		List<String> displaySelectedInteractionTypes = jPreferencePanel
				.getDisplaySelectedInteractionTypes();

		if (legendList.getItemCount() == 0)
			legendList.add(new LegendObject("Total Distribution"));

		int c = legendList.getItemCount();
		for (int i = c - 1; i > 0; i--) {
			if (!displaySelectedInteractionTypes.contains(legendList.get(i)
					.getLabel()))
				legendList.remove(i);

		}
		for (String interactionType : displaySelectedInteractionTypes) {
			LegendObject item = new LegendObject(interactionType);
			if (!legendList.contains(item))
				legendList.add(item);
		}
	}

	public LegendObjectCollection getLegendItems() {
		return this.legendList;
	}

	public void createLegendPanel() {

		if (legendPanel != null) {
			legendPanel.removeAll();

		}

		for (int i = 0; i < legendList.getItemCount(); i++) {

			Color color = (Color) legendList.get(i).getColor();

			jButtonList[i].setBackground(color);

			legendPanel.add(jButtonList[i]);

			lengendCheckBoxList[i].setText(legendList.get(i).getLabel());
			lengendCheckBoxList[i].setSelected(legendList.get(i).isChecked());

			legendPanel.add(lengendCheckBoxList[i]);
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()!=timer) return;
		
		GeneOntologyTree instance = GeneOntologyTree.getInstance();
		if (instance != null) {
			activeMarkersTableModel.fireTableDataChanged();
			previewTableModel.fireTableDataChanged();

			timer.stop();
			timer = null;
		}
	}

}
