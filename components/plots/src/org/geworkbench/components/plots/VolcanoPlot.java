package org.geworkbench.components.plots;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections15.map.ReferenceMap;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.BusySwingWorker;
import org.jfree.chart.*;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Volcano plot.
 *
 * @author Matt Hall, John Watkinson
 */
@AcceptTypes({DSSignificanceResultSet.class})
public class VolcanoPlot implements VisualPlugin {

    static Log log = LogFactory.getLog(VolcanoPlot.class);

    private class MarkerXYToolTipGenerator extends StandardXYToolTipGenerator implements ChartMouseListener {

        private class MarkerAndStats implements Comparable<MarkerAndStats> {

            DSGeneMarker marker;
            double fold;
            double pValue;

            public MarkerAndStats(DSGeneMarker marker, double fold, double pValue) {
                this.marker = marker;
                this.fold = fold;
                this.pValue = pValue;
            }

            public int compareTo(MarkerAndStats o) {
                if (fold > o.fold) {
                    return 1;
                } else if (fold < o.fold) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }

        private SortedSet<MarkerAndStats> markers;
        private List<MarkerAndStats> markerList;
        private DSSignificanceResultSet<DSGeneMarker> sigSet;

        public MarkerXYToolTipGenerator(DSSignificanceResultSet<DSGeneMarker> sigSet) {
            this.sigSet = sigSet;
            markers = new TreeSet<MarkerAndStats>();
        }

        public void chartMouseClicked(ChartMouseEvent event) {
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int item = xyEntity.getItem();
                MarkerAndStats markerStats = markerList.get(item);
                if (markerStats != null) {
                    publishMarkerSelectedEvent(new MarkerSelectedEvent(markerStats.marker));
                }
            }
        }

        public void chartMouseMoved(ChartMouseEvent chartMouseEvent) {
            // No-op
        }

        public void addMarkerAndStats(DSGeneMarker marker, double fold, double pValue) {
            markers.add(new MarkerAndStats(marker, fold, pValue));
        }

        public void processTooltips() {
            markerList = new ArrayList<MarkerAndStats>(markers);
        }

        public String generateToolTip(XYDataset data, int series, int item) {
            String result = "Unknown: ";
            DecimalFormat df = new DecimalFormat("0.###E0");

            MarkerAndStats markerStats = markerList.get(item);
            if (markerStats != null) {
                result = markerStats.marker.getLabel() + " (" + markerStats.marker.getGeneName() + "): " + df.format(markerStats.pValue);
            }
            return result;
        }
    }

    /**
     * Maximum number of charts that can be viewed at once.
     */
    public static final int MAXIMUM_CHARTS = 6;

    private JPanel mainPanel;
    private JPanel parentPanel;
    private ReferenceMap<DSDataSet, Boolean> userOverrideMap = new ReferenceMap<DSDataSet, Boolean>(ReferenceMap.SOFT, ReferenceMap.HARD);

    /**
     * The dataset that holds the microarrayset and panels.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();

    private boolean isLogNormalized = false;

    private JCheckBox logCheckbox;

    /**
     * The significance results we're plotting
     */
    private DSSignificanceResultSet<DSGeneMarker> significance = null;

    /**
     * Constructor lays out the component and adds behaviors.
     */
    public VolcanoPlot() {
        parentPanel = new JPanel(new BorderLayout());
        mainPanel = new JPanel(new BorderLayout());
        parentPanel.add(mainPanel, BorderLayout.CENTER);
        JPanel lowerPanel = new JPanel(new FlowLayout());
        logCheckbox = new JCheckBox("Analyzed data was log2-transformed", false);
        lowerPanel.add(logCheckbox);
        parentPanel.add(lowerPanel, BorderLayout.SOUTH);
        logCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((significance != null) && (dataSetView != null)) {
                    userOverrideMap.put(significance.getParentDataSet(), logCheckbox.isSelected());
                    isLogNormalized = logCheckbox.isSelected();
                    generateChart();
                }
            }
        });
    }

    @Publish public ImageSnapshotEvent createImageSnapshot() {
        // todo - fix
//        Dimension panelSize = chartPanel.getSize();
//        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
//        Graphics g = image.getGraphics();
//        chartPanel.paint(g);
//        ImageIcon icon = new ImageIcon(image, "Volcano Plot");
//        ImageSnapshotEvent event = new ImageSnapshotEvent("Volcano Plot Snapshot", icon, ImageSnapshotEvent.Action.SAVE);
//        return event;
        return null;
    }

    /**
     * The component for the GUI engine.
     */
    public Component getComponent() {
        return parentPanel;
    }

    private void guessLogNormalized(DSMicroarraySet<DSMicroarray> set) {
        double minValue = Double.POSITIVE_INFINITY;
        double maxValue = Double.NEGATIVE_INFINITY;
        for (DSMicroarray microarray : set) {
            DSMutableMarkerValue[] values = microarray.getMarkerValues();
            double v;
            for (DSMutableMarkerValue value : values) {
                v = value.getValue();
                if (v < minValue) {
                    minValue = v;
                }
                if (v > maxValue) {
                    maxValue = v;
                }
            }
        }
        if (maxValue - minValue < 100) {
            isLogNormalized = true;
        } else {
            isLogNormalized = false;
        }
        logCheckbox.setSelected(isLogNormalized);
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
            	if ((significance==null)||(significance.getLabels(DSSignificanceResultSet.CONTROL)==null)||(significance.getLabels(DSSignificanceResultSet.CASE)==null)){
            		//since there are no control or no case, volcanoplot will not work, then we skip this one.
            		//and we should show some messages for user.
            		JLabel msg=new JLabel(
            				"<html>Due to this result has more then two groups, or miss one of Control and Case groups,<br>"+
            				"this result can not be shown in this version of Volcano Plot.</html>");
            		mainPanel.add(msg, BorderLayout.NORTH);
            	}else{
	                significance = (DSSignificanceResultSet<DSGeneMarker>) dataFile;
	                DSMicroarraySet<DSMicroarray> set = significance.getParentDataSet();
	                Boolean userOverride = userOverrideMap.get(set);
	                if (userOverride != null) {
	                    isLogNormalized = userOverride;
	                    logCheckbox.setSelected(isLogNormalized);
	                } else {
	                    guessLogNormalized(set);
	                }
	                generateChart();
            	}
            }
        }
    }

    private void generateChart() {
        DSMicroarraySet<DSMicroarray> set = significance.getParentDataSet();
        String[] caseLabels = significance.getLabels(DSSignificanceResultSet.CASE);
        String[] controlLabels = significance.getLabels(DSSignificanceResultSet.CONTROL);
        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(set);
        DSPanel<DSMicroarray> casePanel = new CSPanel<DSMicroarray>("Case");
        for (int i = 0; i < caseLabels.length; i++) {
            String label = caseLabels[i];
            casePanel.addAll(context.getItemsWithLabel(label));
        }
        casePanel.setActive(true);
        DSPanel<DSMicroarray> controlPanel = new CSPanel<DSMicroarray>("Control");
        for (int i = 0; i < controlLabels.length; i++) {
            String label = controlLabels[i];
            controlPanel.addAll(context.getItemsWithLabel(label));
        }
        casePanel.setActive(true);
        DSPanel<DSGeneMarker> significantGenes = significance.getSignificantMarkers();
        DSPanel<DSMicroarray> itemPanel = new CSPanel<DSMicroarray>();
        itemPanel.panels().add(casePanel);
        itemPanel.panels().add(controlPanel);
        significantGenes.setActive(true);
        dataSetView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(set);
        dataSetView.getMarkerPanel().panels().add(significantGenes);
        dataSetView.setItemPanel(itemPanel);
        dataSetView.useMarkerPanel(true);
        dataSetView.useItemPanel(true);
        log.debug("Generating graph.");
        generateChartAndDisplay();
    }

    private void generateChartAndDisplay() {
        mainPanel.removeAll();
        final BusySwingWorker worker = new BusySwingWorker() {
            ChartPanel cpanel = null;

            public Object construct() {
                setShowProgress(true);
                setBusy(mainPanel);
                MarkerXYToolTipGenerator toolTipGenerator = new MarkerXYToolTipGenerator(significance);
                cpanel = new ChartPanel(createVolcanoChart(dataSetView, significance, false, false, this, toolTipGenerator));
                cpanel.addChartMouseListener(toolTipGenerator);
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

    public JFreeChart createVolcanoChart(
            DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView,
            DSSignificanceResultSet<DSGeneMarker> significance,
            boolean showAllArrays,
            boolean showAllMarkers,
            BusySwingWorker worker,
            MarkerXYToolTipGenerator toolTipGenerator
    ) throws SeriesException {
        DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
        DSAnnotationContext<DSMicroarray> context = manager.getCurrentContext(dataSetView.getDataSet());
        XYSeriesCollection plots = new XYSeriesCollection();

        //        int microarrayNo = maSet.size();

        // First put all the gene pairs in the xyValues array
        int numMarkers = dataSetView.getMarkerPanel().size();

        if (worker != null) {
            worker.setProgressMax(numMarkers * 2);
        }
        DSPanel<DSMicroarray> controlPanel = dataSetView.getItemPanel().panels().get(2);
        DSPanel<DSMicroarray> casePanel = dataSetView.getItemPanel().panels().get(1);

        // First pass to determine negative value correction amount
        double minValue = Double.MAX_VALUE;
        for (int i = 0; i < numMarkers; i++) {
            DSGeneMarker marker = dataSetView.getMarkerPanel().get(i);
            for (DSMicroarray microarray : casePanel) {
                if (microarray.getMarkerValue(marker).getValue() < minValue) {
                    minValue = microarray.getMarkerValue(marker).getValue();
                }
            }

            for (DSMicroarray microarray : controlPanel) {
                if (microarray.getMarkerValue(marker).getValue() < minValue) {
                    minValue = microarray.getMarkerValue(marker).getValue();
                }
            }
            if (worker != null) {
                worker.setCurrentProgress(i);
            }
        }

        if (minValue < 0) {
            // Minimum value adjust to get us above 0 values
            minValue = Math.abs(minValue) + 1;
        } else {
            minValue = 0;
        }

        XYSeries series = new XYSeries("All");
        List<Integer> underflowLocations = new ArrayList<Integer>();
        double validMinSigValue = Double.MAX_VALUE;
        double minPlotValue = Double.MAX_VALUE;
        double validMaxSigValue = Double.MIN_VALUE;
        double maxPlotValue = Double.MIN_VALUE;
        for (int i = 0; i < numMarkers; i++) {
            DSGeneMarker marker = dataSetView.getMarkerPanel().get(i);
            // Calculate fold change
            double caseMean = 0;
            for (DSMicroarray microarray : casePanel) {
                caseMean += microarray.getMarkerValue(marker).getValue();
            }
            caseMean = caseMean / casePanel.size() + minValue;

            double controlMean = 0;
            for (DSMicroarray microarray : controlPanel) {
                controlMean += microarray.getMarkerValue(marker).getValue();
            }
            controlMean = controlMean / controlPanel.size() + minValue;

            double sigValue = significance.getSignificance(marker);
            if (sigValue <= 0) {
                log.debug("Significance less than or equal to 0, (" + sigValue + ") setting to 1 for the moment.");
                sigValue = 1;
            } else {
                if (sigValue < validMinSigValue) {
                    validMinSigValue = sigValue;
                }
                if (sigValue > validMaxSigValue) {
                    validMaxSigValue = sigValue;
                }
            }

            double xVal = 0;
            if (!isLogNormalized) {
                double ratio = caseMean / controlMean;
                if (ratio < 0) {
                    log.debug("Should not get a negative ratio, but got one.");
                    xVal = -Math.log(-ratio) / Math.log(2.0);
                } else {
                    xVal = Math.log(ratio) / Math.log(2.0);
                }
            } else {
                xVal = caseMean - controlMean;
            }
            if (!Double.isNaN(xVal) && !Double.isInfinite(xVal)) {
//                log.debug("xVal = " + caseMean + " / " + controlMean);
                double yVal = -Math.log10(sigValue);
//                log.debug("Adding "+xVal+", "+yVal);
                double plotVal = Math.abs(xVal) * Math.abs(yVal);
                if (plotVal < minPlotValue) {
                    minPlotValue = plotVal;
                }
                if (plotVal > maxPlotValue) {
                    maxPlotValue = plotVal;
                }

                series.add(xVal, yVal);
                toolTipGenerator.addMarkerAndStats(marker, xVal, sigValue);
            } else {
                log.debug("Marker " + i + " was infinite or NaN.");
            }

            if (worker != null) {
                worker.setCurrentProgress(numMarkers + i);
            }
            //            map.put(new Integer(dataSetView.get(i).getSerial()), xyValues[i]);
        }

        // Fix underflow values
        for (Integer fixIndex : underflowLocations) {
            series.getDataItem(fixIndex).setY(validMinSigValue);
        }

        toolTipGenerator.processTooltips();

        plots.addSeries(series);


        JFreeChart mainChart = ChartFactory.createScatterPlot(significance.getLabel(), "Fold Change (Log-2 Difference)", "Neg. Log-10 Significance", plots, PlotOrientation.VERTICAL, false, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        //        mainChart.getXYPlot().setDomainAxis(new LogarithmicAxis("Fold Change"));
        //        mainChart.getXYPlot().setRangeAxis(new LogarithmicAxis("Significance"));
        //        XYLineAnnotation annotation = chartGroup.get(PlotType.MARKER).lineAnnotation;
        //        if (annotation != null) {
        //            mainChart.getXYPlot().addAnnotation(annotation);
        //        }
        //        chartData.setXyPoints(xyPoints);
        mainChart.getXYPlot().setRenderer(new VolcanoRenderer(plots, minPlotValue, maxPlotValue, toolTipGenerator));
        //BufferedImage image = mainChart.createBufferedImage(width, height);
        //return image;
        return mainChart;
    }

    private static class VolcanoRenderer extends StandardXYItemRenderer {
        XYDataset dataset;
        GMTColorPalette colormap;

        public VolcanoRenderer(XYDataset dataset, double min, double max, MarkerXYToolTipGenerator toolTipGenerator) {
            super(StandardXYItemRenderer.SHAPES, toolTipGenerator);
            this.dataset = dataset;
            GMTColorPalette.ColorRange[] range = {new GMTColorPalette.ColorRange(min, Color.BLUE.brighter(), max - (max / 3), Color.BLUE),
                    new GMTColorPalette.ColorRange(max - (max / 3), Color.BLUE, max, Color.RED)};
            this.colormap = new GMTColorPalette(range);
            this.setSeriesShape(0, new Rectangle(6, 6));
        }

        public Paint getItemPaint(int series, int item) {
            double x = dataset.getXValue(series, item);
            double y = dataset.getYValue(series, item);
            return colormap.getColor(Math.abs(x) * Math.abs(y));
            //            return colormap.getColor(dataset.getZValue(series, item));
        }

    }


}
