package org.geworkbench.components.plots;

import java.util.ArrayList;

import org.jfree.chart.annotations.XYLineAnnotation;
import org.jfree.chart.plot.XYPlot;

/**
 * Struct for a group of charts.
 */
class ChartGroup {
    public ChartGroup() {
        charts = new ArrayList<Chart>();
    }

    public int xIndex = -1;
    public ArrayList<Chart> charts;
    public boolean referenceLineEnabled = true;
    public double slope = 1.0;
    public XYLineAnnotation lineAnnotation = getXYLineAnnotation(slope);
    
    void slopeChanged() {
        // Change annotations on all charts in this chart group
        XYLineAnnotation newAnnotation;
        if (referenceLineEnabled) {
            newAnnotation = getXYLineAnnotation(slope);
        } else {
            newAnnotation = null;
        }
        for (int i = 0; i < charts.size(); i++) {
            Chart chart = charts.get(i);
            XYPlot plot = chart.panel.getChart().getXYPlot();
            if (lineAnnotation != null) {
                plot.removeAnnotation(lineAnnotation);
            }
            if (newAnnotation != null) {
                plot.addAnnotation(newAnnotation);
            }
        }
        lineAnnotation = newAnnotation;
    }
    
    private static XYLineAnnotation getXYLineAnnotation(double slope) {
        // Lines must unfortunately be limited in length due to bug in JFreeChart.
        double low = -100.0;
        double high = 1000.0;
        if (slope > 1) {
            double highX = high / slope;
            double lowX = low / slope;
            return new XYLineAnnotation(lowX, low, highX, high);
        } else {
            double highY = high * slope;
            double lowY = low * slope;
            return new XYLineAnnotation(low, lowY, high, highY);
        }
    }

}