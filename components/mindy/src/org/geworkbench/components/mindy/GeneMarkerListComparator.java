package org.geworkbench.components.mindy;

import java.util.Comparator;
import org.geworkbench.bison.datastructure.bioobjects.markers.*;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

/**
 * Compare two gene markers (for sorting).
 * @author ch2514
 * @version $Id: GeneMarkerListComparator.java,v 1.7 2009-04-29 19:55:33 oshteynb Exp $
 */
public class GeneMarkerListComparator implements Comparator<DSGeneMarker> {
	/**
     * Sorting mode - compare two gene markers based on their M# stat.
     */
    public static final int M_POUND = 1;
    /**
     * Sorting mode - compare two gene markers based on their M+ stat.
     */
    public static final int M_PLUS = 2;
    /**
     * Sorting mode - compare two gene markers based on their M- stat.
     */
    public static final int M_MINUS = 3;
    /**
     * Sorting mode - compare two gene markers based on their scores.
     */
    public static final int SCORE = 4;
    /**
     * Sorting mode - compare two gene markers based on their modes.
     */
    public static final int MODE = 5;

    // variables
    private MindyData md;
    private int mode;
    private boolean ascending;
    private DSGeneMarker modulator;

    /**
     * @param md - MINDY data
     * @param mode - sorting mode
     * @param ascending - (for sorting).  If true, the gene markers are sorted in
	 * ascending order based on the sorting mode.
     */
	public GeneMarkerListComparator(MindyData md, int mode, boolean ascending){
		this.md = md;
		this.mode = mode;
		this.ascending = ascending;
	}

	/**
	 * @param md - MINDY data
	 * @param modulator - modulator with which to calculate the score of a marker
	 * @param mode - sorting mode
	 * @param ascending - (for sorting).  If true, the gene markers are sorted in
	 * ascending order based on the sorting mode.
	 */
	public GeneMarkerListComparator(MindyData md, DSGeneMarker modulator, int mode, boolean ascending){
		this(md, mode, ascending);
		this.modulator = modulator;
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
	public int compare(DSGeneMarker x, DSGeneMarker y) {
        switch(mode){
        case M_POUND:
        	if(ascending)
        		return new Integer(md.getStatistics(x).getCount()).compareTo(new Integer(md.getStatistics(y).getCount()));
        	else
        		return new Integer(md.getStatistics(y).getCount()).compareTo(new Integer(md.getStatistics(x).getCount()));
        case M_PLUS:
        	if(ascending)
        		return new Integer(md.getStatistics(x).getMover()).compareTo(new Integer(md.getStatistics(y).getMover()));
        	else
        		return new Integer(md.getStatistics(y).getMover()).compareTo(new Integer(md.getStatistics(x).getMover()));
        case M_MINUS:
        	if(ascending)
        		return new Integer(md.getStatistics(x).getMunder()).compareTo(new Integer(md.getStatistics(y).getMunder()));
        	else
        		return new Integer(md.getStatistics(y).getMunder()).compareTo(new Integer(md.getStatistics(x).getMunder()));
        case SCORE:
        	if(modulator != null){
        		if(ascending)
        			return Float.compare(md.getScore(modulator, x), md.getScore(modulator, y));
        		else
        			return Float.compare(md.getScore(modulator, y), md.getScore(modulator, x));
        	}
        	break;
        case MODE:
        	int xmover = md.getStatistics(x).getMover();
        	int xmunder = md.getStatistics(x).getMunder();
        	int ymover = md.getStatistics(y).getMover();
        	int ymunder = md.getStatistics(y).getMunder();
        	int xmode = 0;
        	int ymode = 0;
        	if(xmover > xmunder) xmode = 1;
        	else if(xmover < xmunder) xmode = -1;
        	else xmode = 0;
        	if(ymover > ymunder) ymode = 1;
        	else if(ymover < ymunder) ymode = -1;
        	else ymode = 0;
        	if(ascending)
        		return new Integer(xmode).compareTo(new Integer(ymode));
        	else
        		return new Integer(ymode).compareTo(new Integer(xmode));
        }
		return 0;
	}

}
