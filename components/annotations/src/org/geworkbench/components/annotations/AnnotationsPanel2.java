package org.geworkbench.components.annotations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.CsvFileFilter;
import org.geworkbench.util.ProgressDialog;
import org.geworkbench.util.ProgressItem;

/**
 * <p>
 * Marker Annotation Component.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 -2013
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * 
 * Component responsible for displaying Gene Annotation obtained from bioDBnet
 * Displays data in a Tabular format with 3 columns. First column contains
 * marker information. The second column contains The Gene Description and the
 * third column contains a list of known Pathways that this gene's product
 * participates in.
 * 
 * @author yc2480
 * @version $Id$
 * 
 */
@AcceptTypes({ DSMicroarraySet.class })
public class AnnotationsPanel2 implements VisualPlugin {

	private static final String[] Human_Mouse = { "Human", "Mouse" };
	private static final int[] taxon_ID = { 9606, 10090 };
	private static final int PATHWAY_TAB_INDEX = 1;

	private static Log log = LogFactory.getLog(AnnotationsPanel2.class);

	private int selectedOrganismIndex = 0; // default to Human

	// member variables to keep track of the status of different datasets
	final private HashMap<Integer, AnnotationTableModel> annotationTableList;
	final private HashMap<Integer, ArrayList<String>> pathwayListMap;
	final private HashMap<Integer, Integer> tabPanelSelectedMap;
	final private HashMap<Integer, Integer> pathwayComboItemSelectedMap;

	private ArrayList<String> pathwayList = null;

	// GUI/Swing members
	final private JPanel mainPanel = new JPanel();
	final private JTabbedPane tabbedPane = new JTabbedPane();
	final private JTable annotationTable;

	final private JPanel pathwayPanel = new JPanel();
	final private JComboBox pathwayComboBox = new JComboBox();
	final private JScrollPane jscrollPanePathway = new JScrollPane();

	private AnnotationTableModel annotationModel;

	final ProgressDialog pd = ProgressDialog.getInstance(false);

	private AnnotTask annotTask = null;

	private DSMicroarraySet maSet = null;
	private DSItemList<DSGeneMarker> selectedMarkerInfo = null;
	private int oldHashCode = 0;

	DSItemList<DSGeneMarker> getAllMarkers() {
		if (maSet != null)
			return maSet.getMarkers();
		else
			return null;
	}

	DSItemList<DSGeneMarker> getSelectedMarkers() {
		return selectedMarkerInfo;
	}

	int getTaxonId() {
		return taxon_ID[selectedOrganismIndex];
	}

	/**
	 * Default Constructor
	 */
	public AnnotationsPanel2() {
		annotationModel = new AnnotationTableModel();
		annotationTable = new JTable(annotationModel);
		annotationTable.setAutoCreateRowSorter(true);

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

		annotationTableList = new HashMap<Integer, AnnotationTableModel>();
		pathwayListMap = new HashMap<Integer, ArrayList<String>>();
		tabPanelSelectedMap = new HashMap<Integer, Integer>();
		pathwayComboItemSelectedMap = new HashMap<Integer, Integer>();
	}

	/**
	 * Configures the Graphical User Interface and Listeners
	 * 
	 * @throws Exception
	 */
	private void jbInit() throws Exception {

		mainPanel.setLayout(new GridLayout());
		mainPanel.add(tabbedPane);

		// three buttons on annotation button panel (annoButtonPanel)
		JButton annoRetrieveButton = new JButton("Retrieve Annotations");
		annoRetrieveButton.setHorizontalAlignment(SwingConstants.CENTER);
		annoRetrieveButton
				.setToolTipText("Retrieve gene and disease information for markers in activated panels");
		annoRetrieveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				retrieveAnnotation();
			}

		});

		JButton annoClearButton = new JButton("Clear");
		annoClearButton.setForeground(Color.black);
		annoClearButton.setFocusPainted(true);
		annoClearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AnnotationTableModel model = new AnnotationTableModel();
				annotationTable.setModel(model);
				annotationTableList.put(new Integer(maSet.hashCode()), model);
			}
		});

		JButton annotationExportButton = new JButton("Export");
		annotationExportButton.setForeground(Color.black);
		annotationExportButton.setToolTipText("Export to CSV files");
		annotationExportButton.setFocusPainted(true);
		annotationExportButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				exportAnnotation();
			}
		});

		JComboBox annoHumanOrMouseComboBox = new JComboBox(Human_Mouse);
		annoHumanOrMouseComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectedOrganismIndex = ((JComboBox) e.getSource())
						.getSelectedIndex();
			}
		});

		JPanel annoButtonPanel = new JPanel();
		annoButtonPanel.add(annoHumanOrMouseComboBox);
		annoButtonPanel.add(annoRetrieveButton);
		annoButtonPanel.add(annoClearButton);
		annoButtonPanel.add(annotationExportButton);

		annotationTable.getTableHeader().setPreferredSize(new Dimension(0, 25));

		JPanel annotationPanel = new JPanel(); // for annotation
		annotationPanel.setLayout(new BorderLayout());
		annotationPanel.add(new JScrollPane(annotationTable),
				BorderLayout.CENTER);
		annotationPanel.add(annoButtonPanel, BorderLayout.SOUTH);

		tabbedPane.add("Annotations", annotationPanel);
		tabbedPane.add("Pathway", pathwayPanel);

		annotationTable.setCellSelectionEnabled(false);
		annotationTable.setRowSelectionAllowed(false);
		annotationTable.setColumnSelectionAllowed(false);
		annotationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = annotationTable.columnAtPoint(e.getPoint());
				int row = annotationTable.rowAtPoint(e.getPoint());
				if ((column >= 0) && (row >= 0)) {
					int modelRow = annotationTable.convertRowIndexToModel(row);
					annotationModel.activateCell(modelRow, column);
				}
			}
		});
		annotationTable.addMouseMotionListener(new MouseMotionAdapter() {
			private boolean isHand = false;

			@Override
			public void mouseMoved(MouseEvent e) {
				int column = annotationTable.columnAtPoint(e.getPoint());
				int row = annotationTable.rowAtPoint(e.getPoint());
				if ((column >= 0) && (row >= 0)) {
					if ((column == AnnotationTableModel.COL_GENE)
							|| (column == AnnotationTableModel.COL_PATHWAY)) {
						if (!isHand) {
							isHand = true;
							annotationTable.setCursor(new Cursor(
									Cursor.HAND_CURSOR));
						}
					} else {
						if (isHand) {
							isHand = false;
							annotationTable.setCursor(new Cursor(
									Cursor.DEFAULT_CURSOR));
						}
					}
				}
			}
		});

		initializePathwayPanel();
	}

	@Override
	public Component getComponent() {
		return mainPanel;
	}

	private void exportAnnotation() {
		JFileChooser jFC = new JFileChooser();

		// We remove "all files" from filter, since we only allow CSV format
		FileFilter ft = jFC.getAcceptAllFileFilter();
		jFC.removeChoosableFileFilter(ft);

		jFC.setFileFilter(new CsvFileFilter());

		// Save model to CSV file
		jFC.setDialogTitle("Save annotations table");
		int returnVal = jFC.showSaveDialog(this.getComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String tabFilename = jFC.getSelectedFile().getAbsolutePath();
			if (!tabFilename.toLowerCase().endsWith(".csv")) {
				tabFilename += ".csv";
			}
			annotationModel.toCSV(tabFilename);
		}
	}

	private void retrieveAnnotation() {
		if (selectedMarkerInfo == null || selectedMarkerInfo.size() == 0) {
			JOptionPane.showMessageDialog(tabbedPane,
					"Please activate a marker set to retrieve annotations.");
		} else {
			if (annotTask != null && !annotTask.isDone()) {
				annotTask.cancel(true);
			}
			annotTask = new AnnotTask(ProgressItem.BOUNDED_TYPE,
					"Connecting to server...", this);
			pd.executeTask(annotTask);
		}
	}

	@Publish
	org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

	@Publish
	public MarkerSelectedEvent publishMarkerSelectedEvent(
			MarkerSelectedEvent event) {
		return event;
	}

	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (maSet != null && e.getPanel() != null) {
			DSPanel<DSGeneMarker> markerPanel = e.getPanel().activeSubset();
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
					maSet);
			maView.setMarkerPanel(markerPanel);
			if (maView.getMarkerPanel().activeSubset().size() == 0) {
				selectedMarkerInfo = new CSItemList<DSGeneMarker>();
			} else {
				selectedMarkerInfo = maView.getUniqueMarkers();
			}
		}
	}

	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<?> data = e.getDataSet();
		int hashcode = 0;

		if (data != null && data instanceof DSMicroarraySet) {
			maSet = (DSMicroarraySet) data;
		}

		if (maSet != null) {
			hashcode = maSet.hashCode();
		}

		if (oldHashCode == 0) {
			oldHashCode = hashcode;
			return;
		}

		pathwayComboItemSelectedMap.put(new Integer(oldHashCode), new Integer(
				pathwayComboBox.getSelectedIndex()));
		pathwayListMap.put(new Integer(oldHashCode), new ArrayList<String>(
				pathwayList));
		tabPanelSelectedMap.put(new Integer(oldHashCode),
				tabbedPane.getSelectedIndex());

		if (hashcode == oldHashCode) {
			return;
		} else {
			oldHashCode = hashcode;
		}

		if (annotationTableList.containsKey(new Integer(hashcode))) {
			annotationModel = annotationTableList.get(new Integer(hashcode));
			annotationTable.setModel(annotationModel);
		} else {
			annotationTable.setModel(new AnnotationTableModel());
		}

		if (pathwayListMap.containsKey(new Integer(hashcode))) {
			int selectIndex = pathwayComboItemSelectedMap.get(new Integer(
					hashcode));

			pathwayList = pathwayListMap.get(new Integer(hashcode));
			pathwayComboBox.setModel(new DefaultComboBoxModel(pathwayList
					.toArray()));
			pathwayComboBox.setSelectedIndex(selectIndex);
			pathwayComboBox.revalidate();
		} else {
			pathwayComboBox.removeAllItems();
			pathwayList.clear();
		}

		if (tabPanelSelectedMap.containsKey(new Integer(hashcode))) {
			tabbedPane.setSelectedIndex(tabPanelSelectedMap.get(new Integer(
					hashcode)));
		} else {
			tabbedPane.setSelectedIndex(0);
		}
	}

	private void initializePathwayPanel() {
		JToolBar pathwayTool = new JToolBar();

		pathwayPanel.setLayout(new BorderLayout());
		pathwayPanel.add(jscrollPanePathway, BorderLayout.CENTER);
		pathwayPanel.add(pathwayTool, BorderLayout.NORTH);

		pathwayComboBox.setMaximumSize(new Dimension(130, 25));
		pathwayComboBox.setMinimumSize(new Dimension(130, 25));
		pathwayComboBox.setPreferredSize(new Dimension(130, 25));
		pathwayComboBox.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPathway();
			}
		});

		JButton clearDiagramButton = new JButton();
		clearDiagramButton.setForeground(Color.black);
		clearDiagramButton.setFocusPainted(true);
		clearDiagramButton.setText("Clear Diagram");
		clearDiagramButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearDiagram();
			}
		});

		JButton clearHistButton = new JButton();
		clearHistButton.setForeground(Color.black);
		clearHistButton.setFocusPainted(true);
		clearHistButton.setText("Clear History");
		clearHistButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearHistory();
			}
		});

		JButton imagePathwayButton = new JButton();
		imagePathwayButton.setForeground(Color.black);
		imagePathwayButton.setFocusPainted(true);
		imagePathwayButton.setText("Image Snapshot");
		imagePathwayButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createImageSnapshot();
			}
		});

		pathwayTool.add(pathwayComboBox, null);
		pathwayTool.add(clearDiagramButton);
		pathwayTool.add(clearHistButton);
		pathwayTool.add(Box.createVerticalStrut(8));
		pathwayTool.add(imagePathwayButton);

		pathwayList = new ArrayList<String>();
	}

	private void selectPathway() {
		String pathwayName = (String) pathwayComboBox.getSelectedItem();
		if (pathwayName != null && pathwayName.trim().length() > 0) {
			try {
				ImageIcon icon = new ImageIcon(new URL(
						"http://www.biocarta.com/pathfiles/" + pathwayName
								+ ".gif"));
				JViewport v = jscrollPanePathway.getViewport();
				v.removeAll();
				v.add(new JLabel(icon), null);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			pathwayPanel.revalidate();
			tabbedPane.setTitleAt(PATHWAY_TAB_INDEX, pathwayName);
		} else {
			jscrollPanePathway.getViewport().removeAll();
			pathwayPanel.revalidate();
			tabbedPane.setTitleAt(PATHWAY_TAB_INDEX, "Pathway");
		}
	}

	private void clearDiagram() {
		String pathwayName = (String) pathwayComboBox.getSelectedItem();
		if (pathwayName != null && pathwayName.trim().length() > 0) {
			pathwayList.remove(pathwayName);
			pathwayComboBox.removeItem(pathwayName);
			if (pathwayComboBox.getItemCount() > 0) {
				pathwayComboBox.setSelectedIndex(pathwayList.size() - 1);
			}
			pathwayPanel.revalidate();
			tabbedPane.setTitleAt(PATHWAY_TAB_INDEX, "Pathway");
		}
	}

	private void clearHistory() {
		pathwayComboBox.removeAllItems();
		pathwayList.clear();
		pathwayPanel.revalidate();
		tabbedPane.setTitleAt(PATHWAY_TAB_INDEX, "Pathway");
	}

	@Publish
	public ImageSnapshotEvent createImageSnapshot() {
		String pathwayName = (String) pathwayComboBox.getSelectedItem();
		if (pathwayName != null && pathwayName.trim().length() > 0) {
			Component diagram = jscrollPanePathway.getViewport()
					.getComponent(0);
			Dimension panelSize = diagram.getSize();
			BufferedImage image = new BufferedImage(panelSize.width,
					panelSize.height, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			diagram.paint(g);
			ImageIcon icon = new ImageIcon(image, pathwayName.toString());
			org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
					pathwayName.toString(), icon,
					org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
			return event;
		} else {
			log.debug("no pathway to take snapshot");
			return null;
		}
	}

	/* invoked by AnnotTask */
	void setTableData(AnnotData annotData) {
		if (annotData == null) {
			annotationModel = new AnnotationTableModel();
		} else {
			annotationModel = new AnnotationTableModel(this, annotData);
		}
		annotationTableList.put(new Integer(maSet.hashCode()), annotationModel);
		annotationTable.setModel(annotationModel);
	}

	// show pathway diagram, invoked from AnnotationTableModel
	void showPathwayDiagram(final String pathway) {
		if (!pathwayList.contains(pathway)) {
			pathwayList.add(pathway);
			pathwayComboBox.addItem(pathway);
		}
		pathwayComboBox.setSelectedItem(pathway);
		pathwayComboBox.revalidate();

		tabbedPane.setSelectedComponent(pathwayPanel);
		tabbedPane.setTitleAt(PATHWAY_TAB_INDEX, pathway);
	}
}
