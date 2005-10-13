package org.geworkbench.components.microarrays;

import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.Publish;
import org.jfree.chart.*;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.text.NumberFormat;

/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author Adam Margolin
 * @version 3.0
 */

public class ExpressionProfilePanel extends MicroarrayViewEventBase implements MenuListener, VisualPlugin {

    private JFreeChart chart;
    ChartPanel graph;

    public ExpressionProfilePanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void jbInit() throws Exception {
        super.jbInit();
        chart = ChartFactory.createXYLineChart(null, // Title
                "Experiment", // X-Axis label
                "Value", // Y-Axis label
                new XYSeriesCollection(), // Dataset
                PlotOrientation.VERTICAL, false, // Show legend
                true, true);
        graph = new ChartPanel(chart, true);

        mainPanel.add(graph, BorderLayout.CENTER);

        chkActivateMarkers.setSelected(true);
        this.activateMarkers = true;

        chkShowArrays.setSelected(true);
        this.activateArrays = true;
    }

    protected void fireModelChangedEvent(MicroarraySetViewEvent event) {
        if (maSetView == null) {
            return;
        }

        Thread t = new Thread() {
            public void run() {
                DSPanel<DSGeneMarker> genes = new CSPanel<DSGeneMarker>("");
                genes.addAll(maSetView.markers());
                DSPanel<DSMicroarray> arrays = new CSPanel<DSMicroarray>("");
                arrays.addAll(maSetView.items());
                double log2 = Math.log(2.0);
                XYSeriesCollection plots = new XYSeriesCollection();
                int numGenes = (genes.size() > 500) ? 500 : genes.size();
                for (int geneCtr = 0; geneCtr < numGenes; geneCtr++) {
                    XYSeries dataSeries = new XYSeries(genes.get(geneCtr).getLabel());
                    for (int maCtr = 0; maCtr < arrays.size(); maCtr++) {
                        double value = arrays.get(maCtr).getMarkerValue(geneCtr).getValue();
                        if (Double.isNaN(value) || value <= 0) {
                            dataSeries.add(maCtr, 0);
                        } else {
                            if (value > 0) {
                                dataSeries.add(maCtr, Math.log(value) / log2);
                            } else {
                                dataSeries.add(maCtr, 0);
                            }
                        }
                    }
                    plots.addSeries(dataSeries);
                }
                StandardXYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.LINES, new ExpressionnXYToolTip());
                chart = ChartFactory.createXYLineChart(null, // Title
                        "Experiment", // X-Axis label
                        "Value", // Y-Axis label
                        plots, // Dataset
                        PlotOrientation.VERTICAL, false, // Show legend
                        true, true);
                chart.getXYPlot().setRenderer(renderer);
                graph.setChart(chart);
                graph.addChartMouseListener(new MicroarrayChartMouseListener());
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /**
     * Responsible for handling marker selection in a microarray scatter plot.
     */
    private class MicroarrayChartMouseListener implements ChartMouseListener {

        public void chartMouseClicked(ChartMouseEvent event) {
            ChartEntity entity = event.getEntity();
            if ((entity != null) && (entity instanceof XYItemEntity)) {
                XYItemEntity xyEntity = (XYItemEntity) entity;
                int series = xyEntity.getSeriesIndex();
                int item = xyEntity.getItem();
                DSGeneMarker marker = maSetView.markers().get(series);
                if (marker != null) {
                    MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
                    publishMarkerSelectedEvent(mse);
                }
                DSMicroarray array = maSetView.items().get(item);
                if (array != null) {
                    PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(array);
                    publishPhenotypeSelectedEvent(pse);
                }
            }
        }

        public void chartMouseMoved(ChartMouseEvent event) {
            // No-op
        }
    }

    /**
     * Tool-tip renderer for gene charts.
     */
    private class ExpressionnXYToolTip extends StandardXYToolTipGenerator {

        public String generateToolTip(XYDataset data, int series, int item) {
            String result = "Unknown: ";
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);

            DSGeneMarker marker = maSetView.markers().get(series);
            DSMicroarray array = maSetView.items().get(item);
            String tooltip = "";
            if (marker != null) {
                tooltip += "Marker: "+marker.getGeneName();
            }
            if (array != null) {
                tooltip += " Array: "+array.getLabel();
            }
            return tooltip;
        }
    }

    public ActionListener getActionListener(String var) {
        return null;
    }

    @Publish public org.geworkbench.events.MarkerSelectedEvent publishMarkerSelectedEvent(MarkerSelectedEvent event) {
        return event;
    }

    @Publish public PhenotypeSelectedEvent publishPhenotypeSelectedEvent(PhenotypeSelectedEvent event) {
        return event;
    }

}
