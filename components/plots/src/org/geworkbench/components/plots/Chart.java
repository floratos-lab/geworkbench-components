package org.geworkbench.components.plots;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.jfree.chart.ChartPanel;

/**
 * Struct for the objects associated with a chart.
 */
class Chart {

    public Chart(DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
        chartData = new ChartData(view);
    }

    public int index;
    public ChartPanel panel;
    public ChartData chartData;
}