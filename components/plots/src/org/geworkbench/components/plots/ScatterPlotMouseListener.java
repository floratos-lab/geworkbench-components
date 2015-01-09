package org.geworkbench.components.plots;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.components.plots.ScatterPlot.PlotType;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;

/**
 * Responsible for handling selection in a scatter plot of the given type.
 */
class ScatterPlotMouseListener implements ChartMouseListener {

    final private Chart chartData;
    final private ScatterPlot scatterPlot;
	final private PlotType type;
    
    public ScatterPlotMouseListener(Chart data, ScatterPlot scatterPlot, PlotType type) {
        this.chartData = data;
        this.scatterPlot = scatterPlot;
        this.type = type;
    }

    public void chartMouseClicked(ChartMouseEvent event) {
        // Currently a no-op, but could select the microarray
        ChartEntity entity = event.getEntity();
        if ((entity != null) && (entity instanceof XYItemEntity)) {
            XYItemEntity xyEntity = (XYItemEntity) entity;
            int series = xyEntity.getSeriesIndex();
            int item = xyEntity.getItem();
            if(type==PlotType.MARKER) {
	            DSMicroarray microarray = chartData.getMicroarray(series, item);
	            PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
	            scatterPlot.publishPhenotypeSelectedEvent(pse);
            } else if(type==PlotType.ARRAY) {
                DSGeneMarker marker = chartData.getMarker(series, item);
                if (marker != null) {
                    MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
                    scatterPlot.publishMarkerSelectedEvent(mse);
                }
            }
        }
    }

    public void chartMouseMoved(ChartMouseEvent event) {
        // No-op
    }
}