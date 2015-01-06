package org.geworkbench.components.plots;

import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;

/**
 * Responsible for handling microarray selection in a gene scatter plot.
 */
class GeneChartMouseListener implements ChartMouseListener {

    private ChartData chartData;

    final private ScatterPlot scatterPlot;
    public GeneChartMouseListener(ChartData data, ScatterPlot scatterPlot) {
        this.chartData = data;
        this.scatterPlot = scatterPlot;
    }

    public void chartMouseClicked(ChartMouseEvent event) {
        // Currently a no-op, but could select the microarray
        ChartEntity entity = event.getEntity();
        if ((entity != null) && (entity instanceof XYItemEntity)) {
            XYItemEntity xyEntity = (XYItemEntity) entity;
            int series = xyEntity.getSeriesIndex();
            int item = xyEntity.getItem();
            DSMicroarray microarray = chartData.getMicroarray(series, item);
            PhenotypeSelectedEvent pse = new PhenotypeSelectedEvent(microarray);
            scatterPlot.publishPhenotypeSelectedEvent(pse);
        }
    }

    public void chartMouseMoved(ChartMouseEvent event) {
        // No-op
    }
}