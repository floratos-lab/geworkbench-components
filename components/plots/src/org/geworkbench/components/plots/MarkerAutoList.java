package org.geworkbench.components.plots;

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.components.plots.ScatterPlot.PlotType;
import org.geworkbench.util.JAutoList;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 * The marker JAutoList type.
 */
class MarkerAutoList extends JAutoList {
	private static final long serialVersionUID = -2486840260492993013L;

	private final MarkerListModel markerModel;
	private final ScatterPlot scatterPlot;
	public MarkerAutoList(MarkerListModel markerModel, ScatterPlot scatterPlot) {
        super(markerModel);
        this.markerModel =markerModel;
        this.scatterPlot= scatterPlot;
    }

    @Override protected void elementClicked(int index, MouseEvent e) {
        markerItemClicked(index);
        markerModel.refresh();
    }

    @Override protected void elementRightClicked(int index, MouseEvent e) {
    	scatterPlot.popupType = PlotType.MARKER;
        scatterPlot.popupIndex = index;
        scatterPlot.popupMenu.show(e.getComponent(), e.getX(), e.getY());

    }
    
    /**
     * Called when an item is clicked in either the microarray or marker list.
     *
     * @param type  the type of item clicked.
     * @param index the index of the item.
     */
    private void markerItemClicked(int index) {
    	String clickedLabel = "";
    		Object o = this.markerModel.getElementAt(index);
    		if(o != null){
    			clickedLabel = ((DSGeneMarker) o).getLabel();
    		}
    	if(StringUtils.isEmpty(clickedLabel)){
    		return;
    	}
    	
    	DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = markerModel.dataSetView;
    	
    	ChartGroup group = scatterPlot.getGroup(PlotType.MARKER);
        if (group.xIndex == -1) {
            // This item goes on the x-axis.
        		group.xIndex = findMarkerIndex(clickedLabel);
        } else {
        	int currentIndex = -1;
            	currentIndex = findMarkerIndex(clickedLabel);
            if ((currentIndex >= 0) && (currentIndex == group.xIndex)) {
                // Clicked on the x-axis item-- ignore.
            } else {
                // Is it already plotted on the y-axis?
                Chart existing = null;
                if(currentIndex >= 0) existing = scatterPlot.getChartForTypeAndIndex(PlotType.MARKER, currentIndex);
                if (existing == null) {
                    // Put item on the y-axis.
                    // Already reach maximum plots? Replace the oldest one.
                    Chart attributes;
                    if (group.charts.size() == ScatterPlot.MAXIMUM_CHARTS) {
                        attributes = group.charts.remove(0);
                    } else {
                        // Otherwise, create a new one.
                        attributes = new Chart(dataSetView);
                    }
                    //attributes.index = index;
                    group.charts.add(attributes);
                    JFreeChart chart;
                		attributes.index = findMarkerIndex(clickedLabel);
                        chart = scatterPlot.createChart(PlotType.MARKER, group.xIndex, attributes);

                    if (attributes.panel == null) {
                        attributes.panel = new ChartPanel(chart);
                            
                        attributes.panel.addChartMouseListener(new GeneChartMouseListener(attributes.chartData, scatterPlot));

                            /* This following is necessary to make tooltip aware of chart panel, which is out of the 'regular" chain of info flow. */
                        MicroarrayXYToolTip tooltips = new MicroarrayXYToolTip(dataSetView, attributes.chartData, attributes.panel, chart.getXYPlot());
                        XYItemRenderer renderer = chart.getXYPlot().getRenderer();                      
                        Rectangle2D bound = renderer.getBaseShape().getBounds2D();
                        tooltips.setShapeBound(bound);
                        renderer. setBaseToolTipGenerator(tooltips);
                        chart.getXYPlot().setRenderer(renderer);
                        /* END of section to handle tooltip */
                    } else {
                        attributes.panel.setChart(chart);
                    }
                } else {
                    // Otherwise, remove the chart
                    group.charts.remove(existing);
                }
                // Lay out the chart panel again (as the number of charts has changed)
                scatterPlot.packChartPanel(PlotType.MARKER);
            }
        }
    }
    
    private int findMarkerIndex(String label){
    	DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView = markerModel.dataSetView;
    	DSItemList<DSGeneMarker> markers = dataSetView.getMicroarraySet().getMarkers();
    	for(int i = 0; i < markers.size(); i++){
    		if(StringUtils.equals(markers.get(i).getLabel().trim(), label.trim())){
    			return i;
    		}
    	}
    	return -1;
    }
}