package org.geworkbench.components.ei;

import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * @author mhall, xiaoqing zhang
 */
@AcceptTypes(EvidenceIntegrationDataSet.class)
public class EvidenceIntegrationVisualization implements VisualPlugin {

    static Log log = LogFactory.getLog(EvidenceIntegrationVisualization.class);

    private JPanel plugin;
    private EvidenceIntegrationDataSet dataSet;

    public EvidenceIntegrationVisualization() {
        plugin = new JPanel(new GridLayout(0, 2));
    }

    public Component getComponent() {
        return plugin;
    }

    @Subscribe
    public void receive(ProjectEvent projectEvent, Object source) {
        log.debug("MINDY received project event.");
        DSDataSet data = projectEvent.getDataSet();
        if ((data != null) && (data instanceof EvidenceIntegrationDataSet)) {
            if (dataSet != data) {
                dataSet = ((EvidenceIntegrationDataSet) data);
                plugin.removeAll();
                java.util.List arrayList = dataSet.getEvidence();
                Map<Integer, String> goldStandardSources = dataSet.getGoldStandardSources();
                GenericDisplayPanel performancePanel = new GenericDisplayPanel(this, GenericDisplayPanel.PlotType.PERF, arrayList, goldStandardSources);

                GenericDisplayPanel rocPanel = new GenericDisplayPanel(this, GenericDisplayPanel.PlotType.ROC, arrayList, goldStandardSources);
                rocPanel.setCurrentMaximumCharts(1);
                performancePanel.setCurrentMaximumCharts(4);
                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.add("Performance Graphs", performancePanel);
                tabbedPane.add("ROC Graph", rocPanel);
                plugin.setLayout(new BorderLayout());
                plugin.add(tabbedPane, BorderLayout.CENTER);

                HashMap<Integer, XYSeriesCollection> gsPlotData = new HashMap<Integer, XYSeriesCollection>();

                for (Evidence evidence : dataSet.getEvidence()) {
                    Map<Integer, Map<Integer, Float>> binPerformance = evidence.getBinPerformance();
                    for (Map.Entry<Integer, Map<Integer, Float>> gsValues : binPerformance.entrySet()) {
                        Integer goldStandardID = gsValues.getKey();
                        XYSeriesCollection gsSeries = gsPlotData.get(goldStandardID);
                        if (gsSeries == null) {
                            gsSeries = new XYSeriesCollection();
                        }
                        XYSeries series = new XYSeries("" + evidence.getName());
                        for (Map.Entry<Integer, Float> binValue : gsValues.getValue().entrySet()) {
                            series.add(binValue.getKey(), binValue.getValue());
                        }
                        gsSeries.addSeries(series);
                        gsPlotData.put(goldStandardID, gsSeries);
                    }

                }

                for (Map.Entry<Integer, XYSeriesCollection> gsEntry : gsPlotData.entrySet()) {
                    // Draw graphs for each Gold Standard set
                    JFreeChart ch = ChartFactory.createXYLineChart(dataSet.getGoldStandardSources().get(gsEntry.getKey()), // Title
                            "Bin #", // X-Axis label
                            "Value", // Y-Axis label
                            gsEntry.getValue(), // Dataset
                            PlotOrientation.VERTICAL, false, // Show legend
                            true, true);
                    ChartPanel chartPanel = new ChartPanel(ch);
                  //  plugin.add(chartPanel);   //Disabled by xz.
                    
                }

                //ch.getXYPlot().setRenderer(renderer);

                plugin.revalidate();
                plugin.repaint();
            } else {
                //redraw? xz.
            }
        }
    }

    @Publish
        public ImageSnapshotEvent createImageSnapshot(ImageSnapshotEvent event) {
                 return  event;
       }

       @Publish
       public ImageSnapshotEvent createMatrix( ImageSnapshotEvent event){
              return event;
       }


}