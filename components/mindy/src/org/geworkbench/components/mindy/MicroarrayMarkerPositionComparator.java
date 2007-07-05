package org.geworkbench.components.mindy;

import java.util.*;
import org.geworkbench.bison.datastructure.bioobjects.microarray.*;
import org.geworkbench.bison.datastructure.biocollections.microarrays.*;

public class MicroarrayMarkerPositionComparator implements Comparator<DSMicroarray> {
	private int markerPosition;
	private boolean ascending;

	public MicroarrayMarkerPositionComparator(int markerPosition, boolean ascending){
		this.markerPosition = markerPosition;
		this.ascending = ascending;
	}

	public int compare(DSMicroarray ma1, DSMicroarray ma2) {
		if(ascending){
			return Float.compare(ma1.getRawMarkerData()[markerPosition], ma2.getRawMarkerData()[markerPosition]);
		} else {
			return Float.compare(ma2.getRawMarkerData()[markerPosition], ma1.getRawMarkerData()[markerPosition]);
		}
	}

}
