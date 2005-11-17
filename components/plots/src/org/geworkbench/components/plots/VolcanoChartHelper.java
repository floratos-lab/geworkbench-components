package org.geworkbench.components.plots;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYDataset;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSDataSetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.geworkbench.util.pathwaydecoder.RankSorter;
import org.geworkbench.util.BusySwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.*;

/**
 * The methods necessary to create volcano charts.
 * User: mhall
 * Date: Nov 15, 2005
 * Time: 4:00:56 PM
 */
public class VolcanoChartHelper {

    static Log log = LogFactory.getLog(VolcanoChartHelper.class);

    public static JFreeChart createVolcanoChart(DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView, DSSignificanceResultSet significance,
                                                boolean showAllArrays, boolean showAllMarkers, BusySwingWorker worker) throws SeriesException {
        DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
        DSAnnotationContext<DSMicroarray> context = manager.getCurrentContext(dataSetView.getDataSet());
        XYSeriesCollection plots = new XYSeriesCollection();
        ArrayList seriesList = new ArrayList();
        ArrayList<PanelVisualProperties> propertiesList = new ArrayList<PanelVisualProperties>();
        PanelVisualPropertiesManager propertiesManager = PanelVisualPropertiesManager.getInstance();
        dataSetView.useItemPanel(!showAllArrays);
        dataSetView.useMarkerPanel(showAllMarkers);

        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) dataSetView.getDataSet();
        HashMap map = new HashMap();
        //        int microarrayNo = maSet.size();
        int microarrayNo = dataSetView.size();

        // First put all the gene pairs in the xyValues array
        RankSorter[] xyValues = new RankSorter[microarrayNo];
        int arraySize = dataSetView.get(0).getMarkerValues().length;

        if (worker != null) {
            worker.setProgressMax(arraySize * 2);
        }

        // First pass to determine negative value correction amount
        double minValue = Double.MAX_VALUE;
        for (int i = 0; i < arraySize; i++) {
            DSPanel<DSMicroarray> casePanel = context.getItemsForClass(CSAnnotationContext.CLASS_CASE);
            for (DSMicroarray microarray : casePanel) {
                if (microarray.getMarkerValue(i).getValue() < minValue) {
                    minValue = microarray.getMarkerValue(i).getValue();
                }
            }

            DSPanel<DSMicroarray> controlPanel = context.getItemsForClass(CSAnnotationContext.CLASS_CONTROL);
            for (DSMicroarray microarray : controlPanel) {
                if (microarray.getMarkerValue(i).getValue() < minValue) {
                    minValue = microarray.getMarkerValue(i).getValue();
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
        for (int i = 0; i < arraySize; i++) {
            // Calculate fold change
            DSPanel<DSMicroarray> casePanel = context.getItemsForClass(CSAnnotationContext.CLASS_CASE);
            double caseMean = 0;
            for (DSMicroarray microarray : casePanel) {
                caseMean += microarray.getMarkerValue(i).getValue() + minValue;
            }
            caseMean = caseMean / casePanel.size();

            DSPanel<DSMicroarray> controlPanel = context.getItemsForClass(CSAnnotationContext.CLASS_CONTROL);
            double controlMean = 0;
            for (DSMicroarray microarray : controlPanel) {
                controlMean += microarray.getMarkerValue(i).getValue() + minValue;
            }
            controlMean = controlMean / controlPanel.size();

            double sigValue = significance.getSignificance(dataSetView.markers().get(i));
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

            double ratio = caseMean / controlMean;
            double xVal = 0;
            if (ratio < 0) {
                xVal = -Math.log(-ratio) / Math.log(2.0);
            } else {
                xVal = Math.log(ratio) / Math.log(2.0);
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
            } else {
                log.debug("Gene " + i + " was infinite or NaN.");
            }

            if (worker != null) {
                worker.setCurrentProgress(arraySize + i);
            }
            //            map.put(new Integer(dataSetView.get(i).getSerial()), xyValues[i]);
        }

        // Fix underflow values
        for (Integer fixIndex : underflowLocations) {
            series.getDataItem(fixIndex).setY(validMinSigValue);
        }

        plots.addSeries(series);

        JFreeChart mainChart = ChartFactory.createScatterPlot("", "log2(Fold Change)", "log10(Significance)", plots, PlotOrientation.VERTICAL, true, true, false); // Title, (, // X-Axis label,  Y-Axis label,  Dataset,  Show legend
        //        mainChart.getXYPlot().setDomainAxis(new LogarithmicAxis("Fold Change"));
        //        mainChart.getXYPlot().setRangeAxis(new LogarithmicAxis("Significance"));
        //        XYLineAnnotation annotation = chartGroup.get(PlotType.MARKER).lineAnnotation;
        //        if (annotation != null) {
        //            mainChart.getXYPlot().addAnnotation(annotation);
        //        }
        //        chartData.setXyPoints(xyPoints);
        StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES);
        for (int i = 0; i < propertiesList.size(); i++) {
            PanelVisualProperties panelVisualProperties = propertiesList.get(i);
            // Note: "i+1" because we skip the default 'other' series.
            int index = showAllArrays ? i + 1 : i;
            renderer.setSeriesPaint(index, panelVisualProperties.getColor());
            renderer.setSeriesShape(index, panelVisualProperties.getShape());

        }
        mainChart.getXYPlot().setRenderer(new VolcanoRenderer(plots, minPlotValue, maxPlotValue));
        //BufferedImage image = mainChart.createBufferedImage(width, height);
        //return image;
        return mainChart;
    }

    private static class VolcanoRenderer extends StandardXYItemRenderer {
        XYDataset dataset;
        GMTColorPalette colormap;

        public VolcanoRenderer(XYDataset dataset, double min, double max) {
            super(StandardXYItemRenderer.SHAPES);
            this.dataset = dataset;
            GMTColorPalette.ColorRange[] range = {  new GMTColorPalette.ColorRange(min, Color.BLUE.brighter(), max - (max/3), Color.BLUE),
                                                    new GMTColorPalette.ColorRange(max - (max/3), Color.BLUE, max, Color.RED)};
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

    //    public static double log2(double d) {
    //      return Math.log(d)/Math.log(2.0);
    //    }
}
