package org.geworkbench.components.microarrays;

import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.util.microarrayutils.MicroarrayViewEventBase;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.config.MenuListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.*;

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

public class ExpressionProfilePanel extends MicroarrayViewEventBase implements MenuListener {

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
                chart = ChartFactory.createXYLineChart(null, // Title
                        "Experiment", // X-Axis label
                        "Value", // Y-Axis label
                        plots, // Dataset
                        PlotOrientation.VERTICAL, false, // Show legend
                        true, true);
                graph.setChart(chart);
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public ActionListener getActionListener(String var) {
        return null;
    }
}
