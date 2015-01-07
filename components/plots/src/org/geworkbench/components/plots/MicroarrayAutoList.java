package org.geworkbench.components.plots;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.components.plots.ScatterPlot.PlotType;
import org.geworkbench.util.JAutoList;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * The microarray JAutoList type.
 */
class MicroarrayAutoList extends JAutoList {
	private static final long serialVersionUID = 2260567334770861847L;

	private final MicroarrayListModel microarrayModel;
	private final ScatterPlot scatterPlot;
	public MicroarrayAutoList(MicroarrayListModel microarrayModel, ScatterPlot scatterPlot) {
        super(microarrayModel);
        this.microarrayModel =microarrayModel;
        this.scatterPlot= scatterPlot;
    }

    @Override protected void elementClicked(int index, MouseEvent e) {
        arrayItemClicked(index);
        microarrayModel.refresh();
    }

    @Override protected void elementRightClicked(int index, MouseEvent e) {
    	scatterPlot.popupType = PlotType.ARRAY;
        scatterPlot.popupIndex = index;
        scatterPlot.popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

	/**
	 * Called when an item is clicked in either the microarray or marker list.
	 *
	 * @param type  the type of item clicked.
	 * @param index the index of the item.
	 */
	private void arrayItemClicked(int index) {
		String clickedLabel = "";
			Object o = microarrayModel.getElementAt(index);
			if(o instanceof DSMicroarray){
				clickedLabel = ((DSMicroarray)o).getLabel();
			}
	
		if(StringUtils.isEmpty(clickedLabel)){
			return;
		}
		ChartGroup group = scatterPlot.getGroup(PlotType.ARRAY);
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = microarrayModel.dataSetView;
	
	    if (group.xIndex == -1) {
	        // This item goes on the x-axis.
	    		group.xIndex = findMAIndex(clickedLabel);
	    } else {
	    	int currentIndex = -1;
	    		currentIndex = findMAIndex(clickedLabel);
	
	        if ((currentIndex >= 0) && (currentIndex == group.xIndex)) {
	            // Clicked on the x-axis item-- ignore.
	        } else {
	            // Is it already plotted on the y-axis?
	            Chart existing = null;
	            if(currentIndex >= 0) existing = scatterPlot.getChartForTypeAndIndex(PlotType.ARRAY, currentIndex);
	            if (existing == null) {
	                // Put item on the y-axis.
	                // Already reach maximum plots? Replace the oldest one.
	                Chart chart;
	                if (group.charts.size() == ScatterPlot.MAXIMUM_CHARTS) {
	                	chart = group.charts.remove(0);
	                } else {
	                    // Otherwise, create a new one.
	                	chart = new Chart(dataSetView);
	                }
	                //attributes.index = index;
	                group.charts.add(chart);
	                chart.index = findMAIndex(clickedLabel);
            		JFreeChart jfreechart = scatterPlot.createChart(PlotType.ARRAY, group.xIndex, chart);
	
	                if (chart.panel == null) {
	                	chart.panel = new ChartPanel(jfreechart);
	                	chart.panel.addChartMouseListener(new ScatterPlotMouseListener(chart, scatterPlot, PlotType.ARRAY));
	
	                    /* This following is necessary to make tooltip aware of chart panel, which is out of the 'regular" chain of info flow. */
                        XYPlot xyPlot = jfreechart.getXYPlot();
	                    MicroarrayXYToolTip tooltips = new MicroarrayXYToolTip(dataSetView, chart, xyPlot);
	                    XYItemRenderer renderer = xyPlot.getRenderer();                      
	                    Rectangle2D bound = renderer.getBaseShape().getBounds2D();
	                    tooltips.setShapeBound(bound);
	                    renderer. setBaseToolTipGenerator(tooltips);
	                    xyPlot.setRenderer(renderer);
	                    /* END of section to handle tooltip */
	                } else {
	                	chart.panel.setChart(jfreechart);
	                }
	            } else {
	                // Otherwise, remove the chart
	                group.charts.remove(existing);
	            }
	            // Lay out the chart panel again (as the number of charts has changed)
	            scatterPlot.packChartPanel(PlotType.ARRAY);
	        }
	    }
	}
	
	/* find the index of matching microarray in the entire dataset, not in the view */
	private int findMAIndex(String label){
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = microarrayModel.dataSetView;
	
		DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
		for(int i = 0; i < maSet.size(); i++){
			if(StringUtils.equals(maSet.get(i).getLabel(), label))
				return i;
		}
		return -1;
	}

}