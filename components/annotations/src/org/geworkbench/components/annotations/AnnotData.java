package org.geworkbench.components.annotations;

import java.util.ArrayList;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * AnnotData: data structure for annotation/pathway results $Id: AnnotData.java
 * 7184 2010-11-10 21:09:20Z wangmen 
 * @version $Id$
 */
public class AnnotData {
	public final ArrayList<DSGeneMarker> markerData;
	public final ArrayList<GeneAnnotation> geneData;
	public final ArrayList<Pathway> pathwayData;

	public AnnotData(ArrayList<DSGeneMarker> marker, ArrayList<GeneAnnotation> gene,
			ArrayList<Pathway> pathway) {
		markerData = marker;
		geneData = gene;
		pathwayData = pathway;
	}
}
