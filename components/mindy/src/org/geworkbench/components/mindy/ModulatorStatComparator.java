package org.geworkbench.components.mindy;

import java.util.Comparator;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.mindy.MindyPlugin.ModulatorSort;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

/**
 * Compare M#, M+, or M- of two gene markers (for sorting).
 *
 * @author mhall
 * @version $Id$
 */
class ModulatorStatComparator implements Comparator<DSGeneMarker> {

	private MindyData data;

	private ModulatorSort sortType;

	/**
	 * Constructor.
	 *
	 * @param data -
	 *            MINDY data
	 * @param sortType -
	 *            specifies whether to sort by M#, M+, or M-
	 */
	public ModulatorStatComparator(MindyData data, ModulatorSort sortType) {
		this.data = data;
		this.sortType = sortType;
	}

	/**
	 * Compare two gene markers based on M#, M+, or M-. The choice is
	 * determined by sort type specified in the constructor.
	 *
	 * @param dsGeneMarker -
	 *            the first gene marker to be compared
	 * @param dsGeneMarker1 -
	 *            the second gene marker to be compared
	 * @return A negative integer if the first gene marker precedes the
	 *         second. Zero if the two markers are the same. A positive
	 *         integer if the second marker precedes the first.
	 */
	public int compare(DSGeneMarker dsGeneMarker, DSGeneMarker dsGeneMarker1) {
		if (sortType == ModulatorSort.Aggregate) {
			return data.getStatistics(dsGeneMarker1).getCount()
					- data.getStatistics(dsGeneMarker).getCount();
		} else if (sortType == ModulatorSort.Enhancing) {
			return data.getStatistics(dsGeneMarker1).getMover()
					- data.getStatistics(dsGeneMarker).getMover();
		} else {
			return data.getStatistics(dsGeneMarker1).getMunder()
					- data.getStatistics(dsGeneMarker).getMunder();
		}
	}

}