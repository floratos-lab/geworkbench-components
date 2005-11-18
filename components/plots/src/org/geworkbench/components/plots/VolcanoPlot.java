package org.geworkbench.components.plots;

import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.util.pathwaydecoder.RankSorter;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.util.BusySwingWorker;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Volcano plot.
 *
 * @author Matt Hall, John Watkinson
 */
@AcceptTypes({DSSignificanceResultSet.class}) public class VolcanoPlot implements VisualPlugin {

    static Log log = LogFactory.getLog(VolcanoPlot.class);

    /**
     * Maximum number of charts that can be viewed at once.
     */
    public static final int MAXIMUM_CHARTS = 6;

    private static final int TAB_ARRAY = 0;
    private static final int TAB_MARKER = 1;

    /**
     * Struct for the objects associated with a chart.
     */
    private class Chart {

        public Chart() {
            chartData = new ChartData();
        }

        public int index;
        public ChartPanel panel;
        public ChartData chartData;
    }

    /**
     * Struct for a group of charts.
     */
    private static class ChartGroup {
        public ChartGroup() {
            charts = new ArrayList<Chart>();
        }

        public int xIndex = -1;
        public ArrayList<Chart> charts;
        public boolean referenceLineEnabled = true;
        public double slope = 1.0;
//        public XYLineAnnotation lineAnnotation = getXYLineAnnotation(slope);
    }

    /**
     * Responsible for handling microarray selection in a gene scatter plot.
     */
    private class GeneChartMouseListener implements ChartMouseListener {

        private ChartData chartData;

        public GeneChartMouseListener(ChartData data) {
            this.chartData = data;
        }

        public void chartMouseClicked(ChartMouseEvent event) {
            // Currently a no-op, but could select the microarray
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                DSMicroarray microarray = chartData.getMicroarray(series, item);
                PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
                publishPhenotypeSelectedEvent(pse);
            }
        }

        public void chartMouseMoved(ChartMouseEvent event) {
            // No-op
        }
    }

    /**
     * Responsible for handling marker selection in a microarray scatter plot.
     */
    private class MicroarrayChartMouseListener implements ChartMouseListener {

        private ChartData chartData;

        public MicroarrayChartMouseListener(ChartData data) {
            this.chartData = data;
        }

        public void chartMouseClicked(ChartMouseEvent event) {
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                DSGeneMarker marker = chartData.getMarker(series, item);
                if (marker != null) {
                    MarkerSelectedEvent mse = new MarkerSelectedEvent(marker);
                    publishMarkerSelectedEvent(mse);
                }
            }
        }

        public void chartMouseMoved(ChartMouseEvent event) {
            // No-op
        }
    }

    private class ChartData {
        private ArrayList<ArrayList<RankSorter>> xyPoints;

        public ChartData() {
        }

        public void setXyPoints(ArrayList<ArrayList<RankSorter>> xyPoints) {
            this.xyPoints = xyPoints;
        }

        public DSGeneMarker getMarker(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            RankSorter rs = (RankSorter) list.get(item);
            DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
            if (maSet != null) {
                if (rs.id < maSet.getMarkers().size()) {
                    DSGeneMarker marker = maSet.getMarkers().get(rs.id);
                    return marker;
                }
            }
            return null;
        }

        public DSMicroarray getMicroarray(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            RankSorter rs = (RankSorter) list.get(item);
            DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
            if (maSet != null) {
                if (rs.id < maSet.size()) {
                    DSMicroarray ma = maSet.get(rs.id);
                    return ma;
                }
            }
            return null;
        }

        public RankSorter getRankSorter(int series, int item) {
            ArrayList<RankSorter> list = xyPoints.get(series);
            RankSorter rs = (RankSorter) list.get(item);
            return rs;
        }
    }

    private JPanel mainPanel;
    private JCheckBox allArraysCheckBox;
    private JCheckBox allMarkersCheckBox;
    private JPanel chartPanel, topChartPanel, bottomChartPanel;
    private JCheckBox rankStatisticsCheckbox;
    private JCheckBox referenceLineCheckBox;
    private JButton clearButton;
    private JButton printButton;
    private JButton imageSnapshotButton;
    private JTextField slopeField;
    private JPopupMenu popupMenu;
    private int popupIndex;

    /**
     * The dataset that holds the microarrayset and panels.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();

    /**
     * The significance results we're plotting
     */
    private DSSignificanceResultSet<DSGeneMarker> significance = null;

    /**
     * Constructor lays out the component and adds behaviors.
     */
    public VolcanoPlot() {
        mainPanel = new JPanel(new BorderLayout());
        allArraysCheckBox = new JCheckBox();
        allMarkersCheckBox = new JCheckBox();
    }

    @Publish public ImageSnapshotEvent createImageSnapshot() {
        Dimension panelSize = chartPanel.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        chartPanel.paint(g);
        ImageIcon icon = new ImageIcon(image, "Volcano Plot");
        ImageSnapshotEvent event = new ImageSnapshotEvent("Volcano Plot Snapshot", icon, ImageSnapshotEvent.Action.SAVE);
        return event;
    }

    /**
     * The component for the GUI engine.
     */
    public Component getComponent() {
        return mainPanel;
    }

    /**
     * Receives a project event.
     *
     * @param e      the event.
     * @param source the source of the event (unused).
     */
    @Subscribe public void receive(ProjectEvent e, Object source) {
        DSDataSet dataFile = e.getDataSet();

        if (dataFile != null) {
            if (dataFile instanceof DSMicroarraySet) {
                DSMicroarraySet set = (DSMicroarraySet) dataFile;
                // If it is the same dataset as before, then don't reset everything
                if (dataSetView.getDataSet() != set) {
                    dataSetView.setMicroarraySet(set);
                }
            } else if (dataFile instanceof DSSignificanceResultSet) {
                significance = (DSSignificanceResultSet<DSGeneMarker>) dataFile;
                DSMicroarraySet<DSMicroarray> set = significance.getParentDataSet();
                String[] caseLabels = significance.getLabels(DSSignificanceResultSet.CASE);
                String[] controlLabels = significance.getLabels(DSSignificanceResultSet.CONTROL);
                DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(set);
                DSPanel<DSMicroarray> casePanel = context.getItemsWithAnyLabel(caseLabels);
                casePanel.setActive(true);
                DSPanel<DSMicroarray> controlPanel = context.getItemsWithAnyLabel(controlLabels);
                controlPanel.setActive(true);
                DSPanel<DSGeneMarker> significantGenes = significance.getSignificantMarkers();
                DSPanel<DSMicroarray> itemPanel = new CSPanel<DSMicroarray>();
                itemPanel.panels().add(casePanel);
                itemPanel.panels().add(controlPanel);
                significantGenes.setActive(true);
                dataSetView = new CSMicroarraySetView<DSGeneMarker,DSMicroarray>(set);
                dataSetView.setMarkerPanel(significantGenes);
                dataSetView.setItemPanel(itemPanel);
                dataSetView.useMarkerPanel(true);
                dataSetView.useItemPanel(true);
                log.debug("Generating graph.");
                generateChartAndDisplay();
            }
        }
    }

    private void generateChartAndDisplay() {
        mainPanel.removeAll();
        final BusySwingWorker worker = new BusySwingWorker() {
            ChartPanel cpanel = null;
            public Object construct() {
                setShowProgress(true);
                setBusy(mainPanel);
                cpanel = new ChartPanel(VolcanoChartHelper.createVolcanoChart(dataSetView, significance, false, false, this));
                return cpanel;
            }

            public void finished() {
                mainPanel.removeAll();
                mainPanel.add(cpanel);
                mainPanel.revalidate();
            }
        };
        worker.start();
    }


    @Publish public MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    @Publish public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(PhenotypeSelectedEvent event) {
        return event;
    }

}
