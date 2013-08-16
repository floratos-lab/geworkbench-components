package org.geworkbench.components.annotations;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * AnnotData: data structure for annotation/pathway results.
 *  
 * @version $Id$
 */
public class AnnotData {
	public final DSGeneMarker[] markerData;
	public final GeneAnnotation[] geneData;
	public final int pathwayCount;

	public AnnotData(DSGeneMarker[] marker, GeneAnnotation[] gene) {
		markerData = marker;
		geneData = gene;
		int c = 0;
		for(GeneAnnotation g : gene) {
			c += g.getPathways().length;
		}
		pathwayCount = c;
	}
}
