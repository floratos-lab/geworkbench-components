package org.geworkbench.components.mindy;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
	 * State of selections and overrides for the entire component
	 * 
	 * seems to be not used any more
	 *
	 * @author mhall
	 * @version $ID$
	 */
	class MarkerLimitState {
		/**
		 * Represents the markers in the marker sets selected by the user.
		 */
		public List<DSGeneMarker> globalUserSelection = new ArrayList<DSGeneMarker>();

		/**
		 * Whether the user has made a global "All Markers On" selection
		 */
//		public boolean allMarkerOverride = true;
	}