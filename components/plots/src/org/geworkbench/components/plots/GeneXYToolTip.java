package org.geworkbench.components.plots;

import java.text.NumberFormat;

import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * Tool-tip renderer for gene charts.
 */
class GeneXYToolTip extends StandardXYToolTipGenerator {
	private static final long serialVersionUID = 3928716706611595907L;
	
	private ChartData chartData;

	private final DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView;
    public GeneXYToolTip(DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView, ChartData data) {
    	this.dataSetView = dataSetView;
        this.chartData = data;
    }

    public String generateToolTip(XYDataset data, int series, int item) {
        String result = "Unknown: ";
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

        DSMicroarray ma = chartData.getMicroarray(series, item);
        if (ma != null) {
            RankSorter rs = chartData.getRankSorter(series, item);
            DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
            DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);
            String xLabel = nf.format(rs.x);
            String yLabel = nf.format(rs.y);
            String[] labels = context.getLabelsForItem(ma);

            if (labels.length > 0) {
                result = ma.getLabel() + ": " + labels[0] + " [" + xLabel + "," + yLabel + "]";
            } else {
                result = ma.getLabel() + ": " + "No Panel, " + " [" + xLabel + "," + yLabel + "]";
            }
            return result;
        } else {
            return "";
        }
    }
}