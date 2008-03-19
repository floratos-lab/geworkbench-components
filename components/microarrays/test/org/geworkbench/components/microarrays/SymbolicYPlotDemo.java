package org.geworkbench.components.microarrays;


import java.awt.Color;
import java.awt.GradientPaint;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.SymbolicXYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XisSymbolic;
import org.jfree.ui.RefineryUtilities;


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
 * @author Min You
 * @version $Id: SymbolicYPlotDemo.java,v 1.1 2008-03-19 20:12:18 my2248 Exp $
 */
 
/**
 * A demonstration application for the symbolic axis plots.
 *
 * @author Anthony Boulestreau
 */
public class SymbolicYPlotDemo {

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    * 
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************
    
    /**
     * Displays an XYPlot with Y symbolic data.
     *
     * @param frameTitle  the frame title.
     * @param data  the data.
     * @param chartTitle  the chart title.
     * @param xAxisLabel  the x-axis label.
     * @param yAxisLabel  the y-axis label.
     */
    private static void displayYSymbolic(String frameTitle,
                                         XYDataset data, String chartTitle,
                                         String xAxisLabel, String yAxisLabel) {
    	 
        JFreeChart chart = createYSymbolicPlot(chartTitle, xAxisLabel, yAxisLabel, data, true);
        chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000, 0, Color.green));

        JFrame frame = new ChartFrame(frameTitle, chart);
        frame.pack();
        RefineryUtilities.positionFrameRandomly(frame);
        frame.show();

    }

    
      
     

     
    /**
     * Creates a XY graph with symbolic value on Y axis.
     *
     * @param title  the chart title.
     * @param xAxisLabel  the x-axis label.
     * @param yAxisLabel  the y-axis label.
     * @param data  the data.
     * @param legend  a flag controlling whether or not the legend is created for the chart.
     *
     * @return the chart.
     */
    public static JFreeChart createYSymbolicPlot(String title, String xAxisLabel,
                                                 String yAxisLabel, XYDataset data,
                                                 boolean legend) {

        /*ValueAxis valueAxis = new NumberAxis(xAxisLabel);
        SymbolAxis symbolicAxis
            = new SymbolAxis(yAxisLabel, ((XisSymbolic) data).getXSymbolicValues());

        XYPlot plot = new XYPlot(data, valueAxis, symbolicAxis, null);
        XYItemRenderer renderer = new StandardXYItemRenderer(
            StandardXYItemRenderer.SHAPES, new SymbolicXYItemLabelGenerator()
        );
        plot.setRenderer(renderer);
        JFreeChart ch = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
        */
        
    	ValueAxis valueAxis = new NumberAxis(xAxisLabel);
        SymbolAxis symbolicAxis
            = new SymbolAxis(yAxisLabel, ((XisSymbolic) data).getXSymbolicValues());

        XYPlot plot = new XYPlot(data, valueAxis, symbolicAxis, null);
        XYItemRenderer renderer = new StandardXYItemRenderer(
                StandardXYItemRenderer.LINES,
                new SymbolicXYItemLabelGenerator());

        JFreeChart ch = ChartFactory.createXYLineChart(null, // Title
                "Experiment", // X-Axis label
                "Value", // Y-Axis label
                data, // Dataset
                PlotOrientation.VERTICAL, false, // Show legend
                true, true);
         
        ch.getXYPlot().setRenderer(renderer);
        
        
        
        
        return ch;

    }

    /**
     * Creates a sample symbolic dataset.
     *
     * @return the dataset.
     */
    public static XSymbolicDataset createYSymbolicSample1() {

        String[] sData = {"Lion", "Elephant", "Monkey", "Hippopotamus", "Giraffe"};
        XSymbolicDataset data
            = new XSymbolicDataset("AY Sample", 20, sData, 4, 2,
                new String[] {"A Fall", "A Spring", "A Summer", "A Winter"});
        return data;

    }

     
    /**
     * The starting point for the demonstration application.
     *
     * @param args  ignored.
     */
    public static void main(String[] args) {

        /*XSymbolicDataset s1 = createYSymbolicSample1();        

        displayYSymbolic("Example 1", s1, "Animal A", "Miles", "Animal");
        */
        /*CategoryTest barchartdemo8 = new CategoryTest("Bar Chart Demo 8");
        barchartdemo8.pack();
        RefineryUtilities.centerFrameOnScreen(barchartdemo8);
        barchartdemo8.setVisible(true);*/
        
    	xySeriseTest();
    }
    
    public static void xySeriseTest()
    {
    
    	XSymbolicDataset collection = new XSymbolicDataset();
    	//XYSeriesCollection collection = new XYSeriesCollection();
    	XYSeries series1 = new XYSeries("series1");
    	 XYSeries series2 = new XYSeries("series2");

    	 series1.add(0,1);
    	 series1.add(1,1);
    	 series1.add(1,2);
    	 series1.add(1,3);
    	 series1.add(2,2);

    	 series2.add(0,3);
    	 series2.add(1,1);
    	 series2.add(2,1);
    	 series2.add(2,0);

    	 collection.addSeries(series1);
    	 collection.addSeries(series2);

    	 JFreeChart chart = ChartFactory.createXYLineChart(null,"X","Y",collection, PlotOrientation.VERTICAL, true,true, true);
    	 XYPlot plot = (XYPlot)chart.getPlot();
    	 //plot.setRangeAxis(new HorizontalSymbolicAxis("X",new String[]{"bad","better","best"})); //bad, better, best are the symbolic values
    	 //the line above throws an AxisNotCompatibleException: Plot.setRangeAxis(...): axis not compatible with plot
    	 //plot.setRangeAxis(new SymbolAxis("X",new String[]{"bad","better","best"}));
    	 plot.setDomainAxis(new SymbolAxis("X",new String[]{"bad","better","best"}));
    	 plot.getRenderer().setSeriesItemLabelsVisible(0, Boolean.TRUE);
    	 
    	 
    	 
    	 ChartPanel chartPanel = new ChartPanel(chart);

    	 JFrame frame = new JFrame();
    	 frame.setSize(500,500);
    	 frame.getContentPane().add(chartPanel);
    	 frame.show();

    }
    
    

    

}