package org.geworkbench.components.annotations;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.ExampleFilter;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AnnotationsEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.annotation.Pathway;
import org.jfree.ui.SortableTable;
import org.jfree.ui.SortableTableModel;

/**
 * <p>
 * Title: Bioworks
 * </p>
 * <p>
 * Description: Modular Application Framework for Gene Expession, Sequence and
 * Genotype Analysis
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 -2004
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * 
 * Component responsible for displaying Gene Annotation obtained from caBIO
 * Displays data in a Tabular format with 2 columns. The first column contains
 * The Gene Discription and the second column contains a list of known Pathways
 * that this gene's product participates in.
 * 
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id: AnnotationsPanel.java,v 1.20 2007-01-23 18:58:24 keshav Exp $
 * 
 * 
 */
@AcceptTypes({DSMicroarraySet.class})
public class AnnotationsPanel implements VisualPlugin {
    static Log log = LogFactory.getLog(AnnotationsPanel.class);

    private class TableModel extends SortableTableModel {

        public static final int COL_MARKER = 0;
        public static final int COL_GENE = 1;
        public static final int COL_PATHWAY = 2;

        private MarkerData[] markerData;
        private GeneData[] geneData;
        private PathwayData[] pathwayData;

        private Integer[] indices;
        private int size;

        public TableModel(MarkerData[] markerData, GeneData[] geneData, PathwayData[] pathwayData) {
            this.markerData = markerData;
            this.geneData = geneData;
            this.pathwayData = pathwayData;
            size = pathwayData.length;
            indices = new Integer[size];
            resetIndices();
        }

        public TableModel() {
            this.markerData = new MarkerData[0];
            this.geneData = new GeneData[0];
            this.pathwayData = new PathwayData[0];
            size = 0;
            indices = new Integer[0];
        }


        private void resetIndices() {
            for (int i = 0; i < size; i++) {
                indices[i] = i;
            }
        }

        public int getRowCount() {
            return size;
        }

        public int getColumnCount() {
            return 3;
        }

        private String wrapInHTML(String s) {
//            return "<html><u><font color=\"#0000FF\">" + s + "</font></u></html>";
            return "<html><a href=\"__noop\">" + s + "</a></html>";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                    return markerData[indices[rowIndex]].name;
                case COL_GENE:
                    return wrapInHTML(geneData[indices[rowIndex]].name);
                case COL_PATHWAY:
                    return wrapInHTML(pathwayData[indices[rowIndex]].name);
            }
            return null;
        }

        public void sortByColumn(final int column, final boolean ascending) {
            resetIndices();
            final Comparable[][] columns = {markerData, geneData, pathwayData};
            Comparator<Integer> comparator = new Comparator<Integer>() {
                public int compare(Integer i, Integer j) {
                    if (ascending) {
                        return columns[column][i].compareTo(columns[column][j]);
                    } else {
                        return columns[column][j].compareTo(columns[column][i]);
                    }
                }
            };
            Arrays.sort(indices, comparator);
            super.sortByColumn(column, ascending);
        }

        public boolean isSortable(int i) {
            return true;
        }

        public void activateCell(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COL_MARKER:
                    MarkerData marker = markerData[indices[rowIndex]];
                    activateMarker(marker);
                    break;
                case COL_GENE:
                    GeneData gene = geneData[indices[rowIndex]];
                    if (gene.annotation.getCGAPGeneURLs().size() > 0) {
                        activateGene(gene);
                    }
                    break;
                case COL_PATHWAY:
                    PathwayData pathway = pathwayData[indices[rowIndex]];
                    // Could be the blank "(none)" pathway.
                    if (pathway.pathway != null) {
                        activatePathway(pathway);
                    }
                    break;
            }
        }
    }

    private static class MarkerData implements Comparable {

        public String name;
        public DSGeneMarker marker;

        public MarkerData(DSGeneMarker marker) {
            this.name = marker.getLabel();
            this.marker = marker;
        }

        public int compareTo(Object o) {
            if (o instanceof MarkerData) {
                return name.compareTo(((MarkerData) o).name);
            }
            return -1;
        }
    }

    private static class GeneData implements Comparable {

        public String name;
        public GeneAnnotation annotation;

        public GeneData(String name, GeneAnnotation annotation) {
            this.name = name;
            this.annotation = annotation;
        }

        public int compareTo(Object o) {
            if (o instanceof GeneData) {
                return name.compareTo(((GeneData) o).name);
            }
            return -1;
        }
    }

    private static class PathwayData implements Comparable {

        public String name;
        public Pathway pathway;

        public PathwayData(String name, Pathway pathway) {
            this.name = name;
            this.pathway = pathway;
        }

        public int compareTo(Object o) {
            if (o instanceof PathwayData) {
                return name.compareTo(((PathwayData) o).name);
            }
            return -1;
        }
    }

    /**
     * Default Constructor
     */
    public AnnotationsPanel() {
        try {
            jbInit();
        }

        catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        annotationsPanel.setLayout(borderLayout1);
        showPanels.setHorizontalAlignment(SwingConstants.CENTER);
        showPanels.setText("Retrieve annotations");
        showPanels.setToolTipText("Retrieve gene and pathway information for markers in activated panels");
        showPanels.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showPanels_actionPerformed(e);
            }

        });
        // buttonPanel.setLayout(borderLayout2);
        clearButton.setForeground(Color.black);
        clearButton.setToolTipText("");
        clearButton.setFocusPainted(true);
        clearButton.setText("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearButton_actionPerformed(e);
            }
        });
        annotationsPanel.add(jScrollPane1, BorderLayout.CENTER);
        annotationsPanel.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.add(showPanels);
        buttonPanel.add(clearButton);
        model = new TableModel();
        table = new SortableTable(model);
        table.getColumnModel().getColumn(0).setHeaderValue("Marker");
        table.getColumnModel().getColumn(1).setHeaderValue("Gene");
        table.getColumnModel().getColumn(2).setHeaderValue("Pathway");
        table.setCellSelectionEnabled(false);
        table.setRowSelectionAllowed(false);
        table.setColumnSelectionAllowed(false);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    model.activateCell(row, column);
                }
            }
        });
        table.addMouseMotionListener(new MouseMotionAdapter() {
            private boolean isHand = false;

            public void mouseMoved(MouseEvent e) {
                int column = table.columnAtPoint(e.getPoint());
                int row = table.rowAtPoint(e.getPoint());
                if ((column >= 0) && (row >= 0)) {
                    if ((column == TableModel.COL_GENE) || (column == TableModel.COL_PATHWAY)) {
                        if (!isHand) {
                            isHand = true;
                            table.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    } else {
                        if (isHand) {
                            isHand = false;
                            table.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        });
        jScrollPane1.getViewport().add(table, null);
    }

    /**
     * Interface <code>VisualPlugin</code> method that returns a
     * <code>Component</code> which is the visual representation of
     * the this plugin.
     *
     * @return <code>Component</code> visual representation of
     *         <code>AnnotationsPanel</code>
     */
    public Component getComponent() {
        return annotationsPanel;
    }

    /**
     * Performs caBIO queries and constructs HTML display of the results
     */
    private void showAnnotation() {
        if (criteria == null) {
            try {
                criteria = new GeneSearchCriteriaImpl();
            } catch (Exception e) {
                log.error("Exception: could not create caBIO search criteria in Annotation Panel. Exception is: ");
                e.printStackTrace();
                return;
            }
        }

        pathways = new Pathway[0];
        try {
            Runnable query = new Runnable() {
                public void run() {
                    ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                    pb.setMessage("Connecting to server...");
                    ArrayList<MarkerData> markerData = new ArrayList<MarkerData>();
                    ArrayList<GeneData> geneData = new ArrayList<GeneData>();
                    ArrayList<PathwayData> pathwayData = new ArrayList<PathwayData>();
                    if (selectedMarkerInfo != null) {
                        pb.setTitle("Querying caBIO..");
                        pb.start();
                        int index = 0;
                        for (int i = 0; i < selectedMarkerInfo.size(); i++) {
                            String geneName = selectedMarkerInfo.get(i).getGeneName();
                            String probeLabel = selectedMarkerInfo.get(i).getLabel();
                            GeneAnnotation[] annotations;
                            if ("".equals(geneName) || geneName.equals(probeLabel)) {
//                                useGeneName = false;
                                annotations = criteria.searchByProbeId(probeLabel);
                            } else {
                                annotations = criteria.searchByName(geneName);
                            }

                            pb.setMessage("Getting Marker Annotation and Pathways: " + selectedMarkerInfo.get(i).getLabel());
                            MarkerData marker = new MarkerData(selectedMarkerInfo.get(i));
//                            GeneAnnotation[] annotations = criteria.searchByName();
                            if (annotations.length > 0) {
                                for (int j = 0; j < annotations.length; j++) {
                                    Pathway[] pways = annotations[j].getPathways();
                                    Pathway[] temp = new Pathway[pathways.length + pways.length];
                                    System.arraycopy(pathways, 0, temp, 0, pathways.length);
                                    System.arraycopy(pways, 0, temp, pathways.length, pways.length);
                                    pathways = temp;
                                    //geneAnnotation +=
                                    //  "<table width=\"90%\" border=\"1\" cellspacing=\"0\" "
                                    //+ "cellpadding=\"2\"><tr valign=\"top\">";
                                    GeneData gene = new GeneData(annotations[j].getGeneName(), annotations[j]);
                                    if (pways.length > 0) {
                                        for (int k = 0; k < pways.length; k++) {
                                            pathwayData.add(new PathwayData(pways[k].getPathwayName(), pways[k]));
                                            geneData.add(gene);
                                            markerData.add(marker);
                                        }
                                    } else {
                                        pathwayData.add(new PathwayData("", null));
                                        geneData.add(gene);
                                        markerData.add(marker);
                                    }
                                }
                            } else {
                                pathwayData.add(new PathwayData("", null));
                                geneData.add(new GeneData("", null));
                                markerData.add(marker);
                            }
                        }
                        pb.stop();
                        pb.dispose();
                    }
                    MarkerData[] markers = markerData.toArray(new MarkerData[0]);
                    GeneData[] genes = geneData.toArray(new GeneData[0]);
                    PathwayData[] pathways = pathwayData.toArray(new PathwayData[0]);
                    model = new TableModel(markers, genes, pathways);
                    table.setSortableModel(model);
                    table.getColumnModel().getColumn(0).setHeaderValue("Marker");
                    table.getColumnModel().getColumn(1).setHeaderValue("Gene");
                    table.getColumnModel().getColumn(2).setHeaderValue("Pathway");
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            table.getTableHeader().repaint();
                        }
                    });
                }
            };
            Thread t = new Thread(query);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearButton_actionPerformed(ActionEvent e) {
        table.setSortableModel(new TableModel());
        table.getColumnModel().getColumn(0).setHeaderValue("Marker");
        table.getColumnModel().getColumn(1).setHeaderValue("Gene");
        table.getColumnModel().getColumn(2).setHeaderValue("Pathway");
        table.getTableHeader().revalidate();
    }

    private void showPanels_actionPerformed(ActionEvent e) {
        if (selectedMarkerInfo == null || selectedMarkerInfo.size() == 0) {
            JOptionPane.showMessageDialog(annotationsPanel, "Please activate marker panels to retrieve annotations.");
        }
        showAnnotation();
    }

    private void activateMarker(MarkerData markerData) {
        publishMarkerSelectedEvent(new MarkerSelectedEvent(markerData.marker));
    }

    private void activateGene(final GeneData gene) {
        JPopupMenu popup = new JPopupMenu();
//        JPopupMenu CGAPPopup = new JPopupMenu("CGAP");
        java.util.List<GeneAnnotationImpl.CGAPUrl> cgapGeneURLs = gene.annotation.getCGAPGeneURLs();
        for (final GeneAnnotationImpl.CGAPUrl cgapUrl : cgapGeneURLs) {
            JMenuItem jMenuItem = new JMenuItem("CGAP > " + cgapUrl.getOrganismName());
            jMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        log.debug("Opening " + cgapUrl.getUrl().toString());
                        BrowserLauncher.openURL(cgapUrl.getUrl().toString());
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });

            popup.add(jMenuItem);
        }

//        popup.add(CGAPPopup);

        popup.show(table, (int) (MouseInfo.getPointerInfo().getLocation().getX() - table.getLocationOnScreen().getX()),
                (int) (MouseInfo.getPointerInfo().getLocation().getY() - table.getLocationOnScreen().getY()));
    }

    private void activatePathway(final PathwayData pathwayData) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem viewDiagram = new JMenuItem("View Diagram");
        viewDiagram.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                publishAnnotationsEvent(new AnnotationsEvent("Pathway Selected", pathwayData.pathway));
            }
        });
        popup.add(viewDiagram);

        JMenuItem makeSet = new JMenuItem("Add pathway genes to set");
        makeSet.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

                Thread thread = new Thread(new Runnable() {
                    public void run() {

                        String tmpSetLabel = JOptionPane.showInputDialog("Panel Set Label:", pathwayData.pathway.getPathwayName());
                        // String tmpLabel = JOptionPane.showInputDialog("Set Label:", "");
                        if (tmpSetLabel == null) {
                            // User hit cancel
                            return;
                        }
                        if (tmpSetLabel.equals("") || tmpSetLabel == null) {
                            tmpSetLabel = pathwayData.pathway.getPathwayName();
                        }

                        ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                        pb.setTitle("Genes For Pathway");
                        pb.setMessage("Retrieving from server...");
                        pb.start();

                        GeneAnnotation[] genesInPathway = criteria.getGenesInPathway(pathwayData.pathway);

                        pb.stop();
                        pb.dispose();

                        DSPanel<DSGeneMarker> selectedMarkers = new CSPanel<DSGeneMarker>(tmpSetLabel, tmpSetLabel);
                        for (int i = 0; i < genesInPathway.length; i++) {
                            GeneAnnotation geneAnnotation = genesInPathway[i];
                            log.info(geneAnnotation.getGeneSymbol() + " : " + geneAnnotation.getGeneName());
                            DSItemList markers = maSet.getMarkers();
                            for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
                                DSGeneMarker marker = (DSGeneMarker) iterator.next();
                                if (marker.getShortName().equals(geneAnnotation.getGeneSymbol())) {
                                    log.debug("Found " + geneAnnotation.getGeneSymbol() + " in set.");
                                    selectedMarkers.add(marker);
                                    break;
                                }
                            }
                        }

                        selectedMarkers.setActive(true);
                        publishSubpanelChangedEvent(new SubpanelChangedEvent(DSGeneMarker.class, selectedMarkers, SubpanelChangedEvent.SET_CONTENTS));

                    }
                });
                thread.start();

            }
        });
        popup.add(makeSet);

        JMenuItem export = new JMenuItem("Export genes to CSV");
        export.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {

                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        JFileChooser chooser = new JFileChooser(pathwayData.pathway.getPathwayName() + ".csv");
                        ExampleFilter filter = new ExampleFilter();
                        filter.addExtension("csv");
                        filter.setDescription("CSV Files");
                        chooser.setFileFilter(filter);
                        int returnVal = chooser.showSaveDialog(null);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            try {
                                ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                                pb.setTitle("Genes For Pathway");
                                pb.setMessage("Retrieving from server...");
                                pb.start();

                                GeneAnnotation[] genesInPathway = criteria.getGenesInPathway(pathwayData.pathway);

                                pb.stop();
                                pb.dispose();

                                saveGenesInPathway(chooser.getSelectedFile(), genesInPathway);
                            } catch (IOException ex) {
                                log.error(ex);
                            }
                        }
                    }
                });
                thread.start();
            }

        });
        popup.add(export);

        popup.show(table, (int) (MouseInfo.getPointerInfo().getLocation().getX() - table.getLocationOnScreen().getX()),
                (int) (MouseInfo.getPointerInfo().getLocation().getY() - table.getLocationOnScreen().getY()));
    }

    private void saveGenesInPathway(File selectedFile, GeneAnnotation[] genesInPathway) throws IOException {
        FileWriter writer = new FileWriter(selectedFile);
        for (int i = 0; i < genesInPathway.length; i++) {
            GeneAnnotation geneAnnotation = genesInPathway[i];
            writer.write(geneAnnotation.getGeneSymbol() + ", " + geneAnnotation.getGeneName() + "\n");
        }
        writer.close();
    }

    @Publish
    public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent event) {
        return event;
    }

    @Publish
    public AnnotationsEvent publishAnnotationsEvent(AnnotationsEvent ae) {
        return ae;
    }

    @Publish
    public MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    /**
     * The Visual Component on which the annotation results are shown
     */
    private JPanel annotationsPanel = new JPanel();

    /**
     * Visual Widget
     */
    private JScrollPane jScrollPane1 = new JScrollPane();

    /**
     * Visual Widget
     */
    private BorderLayout borderLayout1 = new BorderLayout();

    /**
     * Visual Widget
     */
    private SortableTable table;
    private TableModel model;

    /**
     * Visual Widget
     */
    private JPanel buttonPanel = new JPanel();

    /**
     * Visual Widget
     */
    private JButton showPanels = new JButton();
    private DSItemList<DSGeneMarker> selectedMarkerInfo = null;
    private DSGeneMarker singleMarker = null;
    private GeneSearchCriteria criteria = null;
    private boolean showMarkers = true;
    private Pathway[] pathways = new Pathway[0];

    private DSMicroarraySet maSet = null;
    JButton clearButton = new JButton();

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe
    public void receive(GeneSelectorEvent e, Object source) {
        if (maSet != null && e.getPanel() != null) {
            DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(maSet);
            maView.setMarkerPanel(e.getPanel());
            maView.useMarkerPanel(true);
            if (maView.getMarkerPanel().activeSubset().size() == 0) {
                selectedMarkerInfo = new CSItemList<DSGeneMarker>();
            } else {
                selectedMarkerInfo = maView.getUniqueMarkers();
            }
        }
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe
    public void receive(ProjectEvent e, Object source) {
        DSDataSet data = e.getDataSet();
        if (data != null && data instanceof DSMicroarraySet) {
            maSet = (DSMicroarraySet) data;
        }
    }
}
