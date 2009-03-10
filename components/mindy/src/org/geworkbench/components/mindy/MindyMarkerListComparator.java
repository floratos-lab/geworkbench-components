package org.geworkbench.components.mindy;

import java.util.Comparator;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyGeneMarker;

public class MindyMarkerListComparator implements Comparator<MindyGeneMarker> {
	/**
	 * Sorting mode - compare two gene markers based on their short names.
	 */
    public static final int SHORT_NAME = 1;
    /**
     * Sorting mode - compare two gene markers based on their descriptions.
     */
    public static final int DESCRIPTION = 2;
    
    // variables
    private int mode;
    private boolean ascending;
    
    /** 
     * @param mode - sorting mode
     * @param ascending - (for sorting).  If true, the gene markers are sorted in
	 * ascending order based on the sorting mode.
     */
	public MindyMarkerListComparator(int mode, boolean ascending){
		this.mode = mode;
		this.ascending = ascending;
	}

	/**
	 * Compares two gene markers based on the mode specified in the constructor.
	 * This method is for Collections sorting to call.
	 * 
	 * @param x - the first gene marker to be compared
     * @param y - the second gene marker to be compared
     * @return A negative integer if the first gene marker precedes the second.
     * Zero if the two markers are the same.  
     * A positive integer if the second marker precedes the first.
	 */
	public int compare(MindyGeneMarker x, MindyGeneMarker y) {
        switch(mode){
        case SHORT_NAME:
        	if(ascending)
        		return x.getNameSortKey().compareTo(y.getNameSortKey());
        	else
        		return y.getNameSortKey().compareTo(x.getNameSortKey());
        case DESCRIPTION:
        	if(ascending)
        		return x.getDescriptionSortKey().compareTo(y.getDescriptionSortKey());
        	else
        		return y.getDescriptionSortKey().compareTo(x.getDescriptionSortKey());
        }
        return 0;
	}
        		

}
