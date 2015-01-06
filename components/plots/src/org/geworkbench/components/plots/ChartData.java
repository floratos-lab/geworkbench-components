package org.geworkbench.components.plots;

import java.util.ArrayList;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

class ChartData {
    ArrayList<ArrayList<RankSorter>> xyPoints;
    String xLabel = "";
    private String yLabel = "";

    private final DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView;
    public ChartData(DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView) {
    	this.dataSetView = dataSetView;
    }

    public void setXyPoints(ArrayList<ArrayList<RankSorter>> xyPoints) {
        this.xyPoints = xyPoints;
    }

    public DSGeneMarker getMarker(int series, int item) {
        ArrayList<RankSorter> list = xyPoints.get(series);
        RankSorter rs = (RankSorter) list.get(item);
        DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
        if (maSet != null) {
            if (rs.id < maSet.getMarkers().size()) {
                DSGeneMarker marker = maSet.getMarkers().get(rs.id);
                return marker;
            }
        }
        return null;
    }

    public DSMicroarray getMicroarray(int series, int item) {
        ArrayList<RankSorter> list = xyPoints.get(series);
        RankSorter rs = list.get(item);
        DSMicroarraySet maSet = (DSMicroarraySet) dataSetView.getDataSet();
        if (maSet != null) {
            if (rs.id < maSet.size()) {
                DSMicroarray ma = maSet.get(rs.id);
                return ma;
            }
        }
        return null;
    }

    public RankSorter getRankSorter(int series, int item) {
        ArrayList<RankSorter> list = xyPoints.get(series);
        RankSorter rs = (RankSorter) list.get(item);
        return rs;
    }
    
    public String getXLabel(){
    	return this.xLabel;
    }
    
    public void setXLabel(String x){
    	this.xLabel = x;
    }
    
    public String getYLabel(){
    	return this.yLabel;
    }
    
    public void setYLabel(String y){
    	this.yLabel = y;
    }
}