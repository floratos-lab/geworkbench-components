package org.geworkbench.components.annotations;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

public class MarkerData implements Comparable {

    public String name;
    public DSGeneMarker marker;
    public String numOutOfNum;

    public MarkerData(DSGeneMarker marker, String numOutOfNum) {
        this.name = marker.getLabel();
        this.marker = marker;
        this.numOutOfNum = numOutOfNum;
    }

    public int compareTo(Object o) {
        if (o instanceof MarkerData) {
            return name.compareTo(((MarkerData) o).name);
        }
        return -1;
    }
}