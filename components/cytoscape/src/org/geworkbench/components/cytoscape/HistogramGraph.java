 package org.geworkbench.components.cytoscape;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent; 
import java.util.Random;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType; 
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RefineryUtilities;

import javax.swing.JFrame;

/**
 * 
 * @author my2248
 * @version $Id$
 */

public class HistogramGraph {
    
	private JFrame frame;
	private static HistogramGraph histogramGraph = null;
    /** For generating random numbers. */ 
    static Random random = new Random();

    /**
     * Creates a new demo.
     * 
     * @param title  the frame title.
     */
    private HistogramGraph(String title) {    	
    	
    	frame = new JFrame(title);    	 
    	frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				
				 frame.dispose();
				 histogramGraph = null;
			}
		});

    }
    
    public static void CreateInstance(String title, double[] values)
		  {
		if (histogramGraph == null) {
			histogramGraph = new HistogramGraph(title);
			 
		}		
	    HistogramDataset dataset = createDataset(values);
	    JFreeChart chart = createChart(dataset);
	    ChartPanel chartPanel = new ChartPanel(chart);
	    chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
	    chartPanel.setMouseZoomable(true, false);
	    histogramGraph.frame.setContentPane(chartPanel);
		
		
		histogramGraph.frame.setExtendedState(Frame.NORMAL);
		histogramGraph.frame.pack();
		histogramGraph.frame.setVisible(true);		 
		RefineryUtilities.centerFrameOnScreen(histogramGraph.frame);
		
     }
    
    /**
     * Creates a sample {@link HistogramDataset}.
     * 
     * @return The dataset.
     */
    static private HistogramDataset createDataset(double[] values) {
      
    	HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        dataset.addSeries("", values , 20);      
        return dataset;     
    }
    
   
    /**
     * Creates a chart.
     * 
     * @param dataset  a dataset.
     * 
     * @return The chart.
     */
    static private JFreeChart createChart(HistogramDataset dataset) {
        JFreeChart chart = ChartFactory.createHistogram(
            "Edge correlations", 
            "Pearson's correlation", 
            "Frequency", 
            dataset, 
            PlotOrientation.VERTICAL, 
            true, 
            false, 
            false
        );
        
        chart.getXYPlot().getDomainAxis().setRange(-1.00, 1.00);
        
        chart.getXYPlot().getRenderer().setBaseToolTipGenerator(new XYToolTipGenerator() {

			public String generateToolTip(XYDataset dataset, int series,
					int item) {
				String resultStr = "";
				 
				double x = dataset.getXValue(series, item);
				if (Double.isNaN(x) && dataset.getX(series, item) == null) {
					return resultStr;
				}

				double y = dataset.getYValue(series, item);
				if (Double.isNaN(y) && dataset.getX(series, item) == null) {
					return resultStr;
				}
			 

				return resultStr = "("
						+  (double)Math.round(x*100000)/100000 + "," + (double)Math.round(y*100000)/100000
						+ ")";
			}
		});
        chart.getXYPlot().setForegroundAlpha(0.75f);
        chart.getXYPlot().getRenderer().setBaseSeriesVisibleInLegend(false);
        return chart;
    }
    
     
    
}
