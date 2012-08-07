package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
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

import javax.swing.BoxLayout;
import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GeneOntologyTree;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
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
import org.geworkbench.util.AnnotationLookupHelper;
import org.geworkbench.util.BasicAuthenticator;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.ResultSetlUtil;
import org.geworkbench.util.UnAuthenticatedException;
import org.geworkbench.util.network.CellularNetWorkElementInformation;
import org.geworkbench.util.network.CellularNetworkPreference;
import org.geworkbench.util.network.InteractionDetail;

/**
 * @author manjunath at genomecenter dot columbia dot edu, xiaoqing zhang
 * @version $Id: CellularNetworkKnowledgeWidget.java 7531 2011-03-03 15:19:05Z
 *          zji $
 */
@AcceptTypes({ DSMicroarraySet.class })
public class CellularNetworkKnowledgeWidget extends javax.swing.JScrollPane
		implements VisualPlugin, Closable, ActionListener {

	private static final long serialVersionUID = 7095407341852521909L;

	private Log log = LogFactory.getLog(CellularNetworkKnowledgeWidget.class);

	// this flag chooses one of the two ways of interpret interaction edges. currently only option 1 is used
	private int interaction_flag = 1;

	private static final String[] firstFourColumnLabels = new String[] {
			Constants.MARKERLABEL, Constants.GENELABEL,
			Constants.GENETYPELABEL, Constants.GOTERMCOLUMN };

	private static String[] columnLabels = new String[] {
			Constants.MARKERLABEL, Constants.GENELABEL,
			Constants.GENETYPELABEL, Constants.GOTERMCOLUMN };;

	private static TableColumn[] tableColumns;

	private Integer cnkbSelectionIndex = 0;

	private boolean cancelAction = false;

	private boolean needRedraw = false;

	private boolean isQueryRuning = false;

	private Vector<CellularNetWorkElementInformation> hits = null;

	private JButton refreshButton;

	private JButton createNetWorkButton;

	private ThrottleGraph throttleGraph;

	private CellularNetworkPreferencePanel jPreferencePanel;

	private CellSelectionTable detailTable;

	private JTable activatedMarkerTable;

	private JProgressBar jProgressBar1 = new JProgressBar();

	private Vector<DSGeneMarker> allGenes = new Vector<DSGeneMarker>();

	private Vector<DSGeneMarker> selectedGenes = new Vector<DSGeneMarker>();

	private DSMicroarraySet dataset = null;

	private final DetailTableModel detailTableModel;
	private final DefaultTableModel activeMarkersTableModel = new ActiveMarkersTableModel(allGenes);

	/**
	 * Creates new form Interactions
	 */
	public CellularNetworkKnowledgeWidget() {

		loadApplicationProperty();
		initComponents();

		tableColumns = new TableColumn[columnLabels.length];
		detailTableModel = new DetailTableModel(this, CellularNetworkKnowledgeWidget.columnLabels);
		detailTable.setModel(detailTableModel);

		TableColumnModel model = detailTable.getColumnModel();
		for (int i = 0; i < columnLabels.length; i++) {

			tableColumns[i] = model.getColumn(i);
		}

		activatedMarkerTable.getTableHeader().setEnabled(true);
		activatedMarkerTable.setPreferredScrollableViewportSize(new Dimension(
				280, 100));
		activatedMarkerTable.getColumnModel().getColumn(0)
				.setPreferredWidth(200);
		activatedMarkerTable.getColumnModel().getColumn(1)
				.setPreferredWidth(50);
		activatedMarkerTable.getColumnModel().getColumn(2)
				.setPreferredWidth(30);

		detailTable.getTableHeader().setEnabled(true);
		detailTable.setDefaultRenderer(String.class, new DetailTableStringRenderer(this));
		detailTable.setDefaultRenderer(Integer.class, new DetailTableIntegerRenderer(this));

		GeneOntologyTree instance = GeneOntologyTree.getInstance();
		if (instance == null) {
			final int ONE_SECOND = 1000;
			timer = new Timer(ONE_SECOND, this);
			timer.start();
		}
	}

	// the sole purpose of this timer is to update when GO tree is ready
	// this cannot be deserialized
	transient private Timer timer;

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

		Map<String, List<DSGeneMarker>> geneNameToMarkerMap = AnnotationLookupHelper
				.getGeneNameToMarkerMapping(dataset);
		Map<String, List<DSGeneMarker>> geneIdToMarkerMap = AnnotationLookupHelper
				.getGeneIdToMarkerMapping(dataset);

		int rowCount = detailTable.getRowCount();
		int colCount = detailTable.getColumnCount();
		for (int i = 0; i < rowCount; i++) {
			for (int j = 0; j < colCount; j++) {
				if (detailTable.isCellSelected(i, j)) {
					int row = detailTable.convertRowIndexToModel(i);
					String columnName = detailTable.getColumnName(j);
					CellularNetWorkElementInformation c = hits.get(row);
					ArrayList<InteractionDetail> arrayList = c
							.getSelectedInteractions(columnName.substring(
									0,
									columnName.length()
											- Constants.COLUMNLABELPOSTFIX
													.length()));
					if (arrayList == null || arrayList.size() <= 0) {
						continue;
					}

					Integer geneId = c.getdSGeneMarker().getGeneId();
					Collection<DSGeneMarker> markers = geneIdToMarkerMap
							.get(geneId.toString());
					selectedMarkers.addAll(markers);

					for (InteractionDetail detail : arrayList) {
						String dbSource = detail.getDbSource();
						Collection<DSGeneMarker> markers2 = null;
						if (dbSource.equalsIgnoreCase(Constants.ENTREZ_GENE))
							markers2 = geneIdToMarkerMap.get(detail
									.getdSGeneId());
						else
							markers2 = geneNameToMarkerMap.get(detail
									.getdSGeneName());
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

			throttleGraph.repaint(false, true);

		}

		activatedMarkerTable.revalidate();
		detailTableModel.fireTableDataChanged();
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
							hits.addElement(new CellularNetWorkElementInformation(
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

			throttleGraph.repaint(false, true);

		}

		activatedMarkerTable.revalidate();
		detailTableModel.fireTableDataChanged();
		detailTable.revalidate();

	}

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
	 * Create a popup menu to display the name of different Go Terms in 3
	 * categories.
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
						CNKBGoTermUtil.displayGoTree(set,
								CellularNetworkKnowledgeWidget.this);
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

		Authenticator.setDefault(new BasicAuthenticator());
		JPanel jPanel2 = new javax.swing.JPanel();
		JPanel topPanel = new JPanel();

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

		JButton cancelButton = new JButton();

		jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(
				204, 204, 255)));
		// jPanel2.setMaximumSize(new Dimension(200, 80));
		jPanel2.setMinimumSize(new Dimension(230, 80));
		jPanel2.setPreferredSize(new Dimension(230, 80));

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
						detailTableModel.fireTableDataChanged();
						detailTable.revalidate();

						throttleGraph.repaint(false, true);

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
								if (cell.equals(cellularNetWorkElementInformation)) {
									newEntry = false;
									break;
								}
							}
							if (newEntry) {
								hits.addElement(new CellularNetWorkElementInformation(
										marker));
							}
							activatedMarkerTable.revalidate();
							detailTableModel.fireTableDataChanged();
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
		jLabel1.setText("<html><font color=blue><B>Selected Marker List: </b></font></html>");
		jLabel1.setMaximumSize(new Dimension(90, 40));
		createNetWorkButton.setText("Create Network");
		createNetWorkButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(java.awt.event.ActionEvent evt) {

						double threshold = 0;
						try {
							threshold = Double.parseDouble(throttleGraph.getThresholdText());
						} catch (NumberFormatException e1) {
							JOptionPane.showMessageDialog(null,
									"The Threshold field is not a number.",
									"Please check your input.",
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						
						createNetWorkButton.setEnabled(false);

						ProgressBar createNetworkPb = null;
						createNetworkPb = ProgressBar
								.create(ProgressBar.INDETERMINATE_TYPE);

						boolean isRestrictToGenesPresentInMicroarray = jPreferencePanel
								.isNetworkJCheckBox1Selected();
						List<String> selectedTypes = jPreferencePanel
								.getNetworkSelectedInteractionTypes();
						String selectedContext = jPreferencePanel.getSelectedContext();
						String selectedVersion = jPreferencePanel.getSelectedVersion();
						NetworkCreator createNetworkHandler = new NetworkCreator(
								createNetworkPb, createNetWorkButton,
								threshold, dataset,
								isRestrictToGenesPresentInMicroarray,
								selectedTypes, selectedContext,
								selectedVersion,
								CellularNetworkKnowledgeWidget.this);
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

		throttleGraph = new ThrottleGraph(this);
		topPane.add(throttleGraph, JSplitPane.RIGHT);

		upPanel.add(topPane, JSplitPane.TOP);
		upPanel.add(jPanel1, JSplitPane.BOTTOM);
		upPanel.setOneTouchExpandable(true);

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

	} // end of initComponents

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

		detailTable.tableChanged(new TableModelEvent(detailTableModel));
		detailTable.repaint();

		if (needRedraw) {
			throttleGraph.repaint(false, true);

		}
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

	public boolean isQueryRuning() {
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

						} else {
							hasRereivedRecord = true;
						}

					}

					List<Short> confidenceTypeList = CellularNetWorkElementInformation
							.getConfidenceTypeList();
					throttleGraph.refreshThresholdTypes(confidenceTypeList, hasRereivedRecord);

					if (!cancelAction) {
						updateProgressBar(1, "Query is finished.");
						boolean needNewProperties = throttleGraph
								.updatePreference(context, version);
						throttleGraph.repaint(needNewProperties, true);

					} else {
						updateProgressBar(1, "Stopped");
					}

					detailTableModel.fireTableDataChanged();
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

			int timeout = Integer.parseInt(
					iteractionsProp
							.getProperty(Constants.INTERACTIONS_SERVLET_CONNECTION_TIMEOUT));

			interaction_flag = new Integer(
					iteractionsProp.getProperty(Constants.INTERACTIONS_FLAG));

			String interactionsServletUrl = PropertiesManager.getInstance()
					.getProperty(this.getClass(), "url", "");
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

	// this method is invoked from two places. both are problematic.
	// 1. from InitPrefWorker; 2. from CellularNetworkPreferencePanel.reInitPreferences();
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

			detailTableModel.setColumnLabels(columnLabels);
			detailTableModel.fireTableStructureChanged();
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

	// to support renderer
	public CellularNetWorkElementInformation getOneRow(int row) {
		row = detailTable.convertRowIndexToModel(row);

		if (hits == null || row >= hits.size()) {
			return null;
		}

		return hits.get(row);
	}
	
	// to support renderer
	public String getDetailTableHeader(int column) {
		TableColumn tableColumn = detailTable.getColumnModel()
				.getColumn(column);
		return tableColumn.getHeaderValue().toString();
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

			if (dataset.getValuesForName(Constants.CNKB_PREFERENCE) == null)
				dataset.addNameValuePair(Constants.CNKB_PREFERENCE,
						new CellularNetworkPreference("Throttle Graph"));
			throttleGraph.setPreference((CellularNetworkPreference) dataset
					.getValuesForName(Constants.CNKB_PREFERENCE)[0]);

			needDraw = true;

		}

		int currentCount = 0;
		if (hits != null)
			currentCount = hits.size();

		if (panel != null) {
			if (panel.size() == 0) {
				allGenes.clear();
				selectedGenes.clear();
			}
			allGenes.clear();
			Set<DSGeneMarker> orderedSet = new HashSet<DSGeneMarker>();

			for (int j = 0; j < panel.panels().size(); j++) {
				DSPanel<DSGeneMarker> mrk = panel.panels().get(j);
				if (mrk.isActive()) {
					for (int i = 0; i < mrk.size(); i++) {
						orderedSet.add(mrk.get(i));

					}
				}

			}

			updateAllMarkersList(orderedSet, hits);

			activeMarkersTableModel.fireTableDataChanged();

			detailTableModel.fireTableDataChanged();
			detailTable.revalidate();

			checkSelectedTableWithNewDataSet(panel);

			if (needDraw == true || hits == null || hits.size() == 0
					|| hits.size() != currentCount) {
				throttleGraph.repaint(false, true);
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
		return throttleGraph.createImageSnapshot();
	}

	// TODO actually only clear is needed
	public LegendObjectCollection getLegendItems() {
		return throttleGraph.getLegendItems();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() != timer)
			return;

		GeneOntologyTree instance = GeneOntologyTree.getInstance();
		if (instance != null) {
			activeMarkersTableModel.fireTableDataChanged();
			detailTableModel.fireTableDataChanged();

			timer.stop();
			timer = null;
		}
	}

	// following four methods are used by ThrottleGraph only
	List<String> getDisplaySelectedInteractionTypes() {
		return jPreferencePanel.getDisplaySelectedInteractionTypes();
	}

	String getSelectedContext() {
		return jPreferencePanel.getSelectedContext();
	}

	String getSelectedVersion() {
		return jPreferencePanel.getSelectedVersion();
	}	
	
	void detailTableDataChanged() {
		detailTableModel.fireTableDataChanged();
		detailTable.revalidate();
	}
}
