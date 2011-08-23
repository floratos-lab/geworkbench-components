/**
 * 
 */
package org.geworkbench.components.interactions.cellularnetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;

/**
 * @author zji
 * @version $Id$
 * 
 */
// CellularNetworkKnowledgeWidget became extremely bloated, so I put more code
// out of it.
// more logical class design is preferred in long term
public class CNKBUtil {

	static List<DSGeneMarker> getMarkersForGivenGeneId(
			DSMicroarraySet<DSMicroarray> microarraySet, String gene) {

		List<DSGeneMarker> list = new ArrayList<DSGeneMarker>();

		for (DSGeneMarker marker : microarraySet.getMarkers()) {
			if (marker != null && marker.getLabel() != null) {
				Set<String> geneSet = AnnotationParser.getGeneIDs(marker
						.getLabel());
				if (geneSet.contains(gene)) {
					list.add(marker);
				}
			}
		}
		return list;
	}
}
