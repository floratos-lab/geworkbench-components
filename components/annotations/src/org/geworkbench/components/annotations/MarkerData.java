package org.geworkbench.components.annotations;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

public class MarkerData implements Comparable<MarkerData> {

    public String name;
    public DSGeneMarker marker;
    public String numOutOfNum;

    public MarkerData(DSGeneMarker marker, String numOutOfNum) {
        this.name = marker.getLabel();
        this.marker = marker;
        this.numOutOfNum = numOutOfNum;
    }

    @Override
    public int compareTo(MarkerData markerData) {
    	return name.compareTo(markerData.name);
    }
}