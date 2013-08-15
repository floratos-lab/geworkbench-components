package org.geworkbench.components.annotations;

import java.util.ArrayList;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * AnnotData: data structure for annotation/pathway results.
 *  
 * @version $Id$
 */
public class AnnotData {
	public final ArrayList<DSGeneMarker> markerData;
	public final ArrayList<GeneAnnotation> geneData;
	public final int pathwayCount;

	public AnnotData(ArrayList<DSGeneMarker> marker,
			ArrayList<GeneAnnotation> gene) {
		markerData = marker;
		geneData = gene;
		int c = 0;
		for(int i=0; i<gene.size(); i++) {
			c += gene.get(i).getPathways().length;
		}
		pathwayCount = c;
	}
}
