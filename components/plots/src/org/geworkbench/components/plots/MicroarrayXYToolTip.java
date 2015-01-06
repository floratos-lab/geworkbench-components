package org.geworkbench.components.plots;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

/**
 * Tool-tip renderer for microarray charts.
 */
class MicroarrayXYToolTip extends StandardXYToolTipGenerator {
	private static final long serialVersionUID = -896282253416405020L;
	
	private ChartData chartData;
    private ChartPanel chartPanel;
    private Rectangle2D shapeBound;
    private XYPlot xyPlot;
    
    
    final DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView;
    
    public MicroarrayXYToolTip(DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView, ChartData data, ChartPanel chartPanel, XYPlot xyPlot) {
    	this.dataSetView = dataSetView;
    	
        this.chartData = data;
        this.chartPanel = chartPanel;
        this.xyPlot = xyPlot;
        shapeBound = null;
    }

    public String generateToolTip(XYDataset data, int series, int item) {
    	/*
		 * because this customized tooltip needs to know the chart panel
		 * size, it is NOT supposed to called in any case when chartPanel is
		 * null - which was OK for a more regular tool tip.
		 */
		if (chartPanel == null) {
			throw new RuntimeException("tooltip error: no ChartPanel");
			}
		/* this customized tooltip also needs to know the the symbol shape */
		if (shapeBound == null){
			throw new RuntimeException("tooltip error: shape bound unknown");}
    	
        ValueAxis domainAxis = xyPlot.getDomainAxis();
        ValueAxis rangeAxis = xyPlot.getRangeAxis();
        double rangeLower = rangeAxis.getLowerBound();
        double rangeHeight = rangeAxis.getUpperBound() - rangeLower;
        double domainLower = domainAxis.getLowerBound();
        double domainWidth = domainAxis.getUpperBound() - domainLower;

       	/*
		 * generateToolTip should be called only when the chartPanel is not
		 * null
		 */
		Rectangle2D area = chartPanel.getScreenDataArea();

		RankSorter rs = chartData
				.getRankSorter(series, item);
		double x0 = (rs.x - domainLower) / domainWidth * area.getWidth()
				- 0.5 * shapeBound.getWidth();
		double y0 = (rs.y - rangeLower) / rangeHeight * area.getHeight()
				- 0.5 * shapeBound.getHeight();
		Rectangle2D bound = new Rectangle2D.Double(x0, y0, shapeBound
				.getWidth(), shapeBound.getHeight());

		DSPanel<DSGeneMarker> panel = dataSetView.getMarkerPanel();

		DSMicroarraySet maSet = (DSMicroarraySet) dataSetView
		.getDataSet();
		if (maSet == null)
			return ""; /*
							 * only process if rankSorter is interesting.
							 * Behavior copied from method
							 * chartData.getMarker(series, item)
							 */
		
		StringBuilder sb = new StringBuilder("<html>");
		int count = 0;
		final int MAXIMUM_NUMBER_FOR_SMART_TOOLTIP = 100;
		final int MAXIMUM_ITEMS_IN_A_TOOLTIP = 10;
		for (ArrayList<RankSorter> list : chartData.xyPoints) {

			/* If a list has too many points, use the simple tooltip to avoid slow response. */
			if (list.size() > MAXIMUM_NUMBER_FOR_SMART_TOOLTIP) {
				DSGeneMarker marker = chartData.getMarker(series, item);
				String label = "No Panel, ";
				DSPanel<DSGeneMarker> value = null;
				if(panel!=null)
					value = panel.getPanel(marker);
				if (value != null)
					label = value.getLabel();
				return tooltipForMarker(marker, label, rs);
			}

			for (RankSorter rankSorter : list) {
				if (rankSorter.id < maSet.getMarkers().size()) {
					double x = (rankSorter.x - domainLower) / domainWidth
							* area.getWidth();
					double y = (rankSorter.y - rangeLower) / rangeHeight
							* area.getHeight();
					if (bound.contains(new Point2D.Double(x, y))) {
						DSGeneMarker markerNear = maSet.getMarkers().get(
								rankSorter.id);
						String label = "No Panel, ";
						DSPanel<DSGeneMarker> value = null;
						if (panel != null)
							value = panel.getPanel(markerNear);
						if (value != null)
							label = value.getLabel();
						count++;
						if(count>MAXIMUM_ITEMS_IN_A_TOOLTIP) /* If there are too many points for this tool tip, show the first group.*/
							return sb.append("...</html>").toString();
						sb.append(
								tooltipForMarker(markerNear, label,
										rankSorter)).append("<br>");
					}
				}
			}
		}
		return sb.append("</html>").toString();
    }
    
    private String tooltipForMarker(DSGeneMarker marker, String label, RankSorter rs) {
    	return marker.getLabel() + ": " + label + " [" + rs.x + "," + rs.y + "]";
    }

	public void setShapeBound(Rectangle2D bound) {
		shapeBound = bound;
	}
}