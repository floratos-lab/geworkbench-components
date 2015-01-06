package org.geworkbench.components.plots;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.events.MarkerSelectedEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;

/**
 * Responsible for handling marker selection in a microarray scatter plot.
 */
class MicroarrayChartMouseListener implements ChartMouseListener {

    private ChartData chartData;
    final private ScatterPlot scatterPlot;

    public MicroarrayChartMouseListener(ChartData data, ScatterPlot scatterPlot) {
        this.scatterPlot = scatterPlot;
    }

    public void chartMouseClicked(ChartMouseEvent event) {
        ChartEntity entity = event.getEntity();
        if ((entity != null) && (entity instanceof XYItemEntity)) {
            XYItemEntity xyEntity = (XYItemEntity) entity;
            int series = xyEntity.getSeriesIndex();
            int item = xyEntity.getItem();
            DSGeneMarker marker = chartData.getMarker(series, item);
            if (marker != null) {
                MarkerSelectedEvent mse = new org.geworkbench.events.MarkerSelectedEvent(marker);
                scatterPlot.publishMarkerSelectedEvent(mse);
            }
        }
    }

    public void chartMouseMoved(ChartMouseEvent event) {
        // No-op
    }
}