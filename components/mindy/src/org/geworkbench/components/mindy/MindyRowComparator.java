package org.geworkbench.components.mindy;

import java.util.Comparator;

import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyResultRow;



/**
 * Compare two MINDY result rows (for sorting).
 * @author ch2514
 * @version $Id: MindyRowComparator.java,v 1.10 2009-04-29 19:55:33 oshteynb Exp $
 */
public class MindyRowComparator implements Comparator<MindyResultRow> {
	/**
	 * Sorting mode - compare MINDY result rows based on their scores.
	 */
	public static final int SCORE = 1;
	public static final int DELTA_I = 2;
	public static final int PEARSON_CORRELATION = 3;
	public static final int MODULATOR = 4;
	public static final int TARGET = 5;

	// variable
    private int mode;
    private boolean ascending;

    // keeps comparison keys
	private MindyData mindyData;
	// probe name or symbol name to use for gene comparison
	private boolean showProbeName = false;

    /**
     * @param mode - how to compare two rows
     * @param ascending (for sorting) - If true, the rows are sorted in ascending order
     * as per sorting mode.
     * @param mindyData - keeps collation keys
     */
    public MindyRowComparator(int mode, boolean ascending, MindyData mindyData){
    	this.mode = mode;
    	this.ascending = ascending;
    	this.mindyData = mindyData;
    }

    /**
     * @param mode - how to compare two rows
     * @param ascending (for sorting) - If true, the rows are sorted in ascending order
     * as per sorting mode.
     * @param mindyData - keeps collation keys
     * @param showProbeName if true use probe name for comparison
     */
    public MindyRowComparator(int mode, boolean ascending, MindyData mindyData, boolean showProbeName){
    	this.mode = mode;
    	this.ascending = ascending;
    	this.mindyData = mindyData;
    	this.showProbeName = showProbeName;
    }

    /**
     * Compares two MINDY result rows based on the mode specified in the constructor.
     * This method is for Collections sorting to call.
     *
     * @param x - the first MINDY result row to be compared
     * @param y - the second MINDY result row to be compared
     * @return A negative integer if the first MINDY result row precedes the second.
     * Zero if the two rows are the same.
     * A positive integer if the second row precedes the first.
     */
	public int compare(MindyResultRow x, MindyResultRow y) {
        switch(mode){
        case SCORE:
        	if(ascending)
        		return Float.compare(x.getScore(), y.getScore());
        	else
        		return Float.compare(y.getScore(), x.getScore());
        case DELTA_I:
        	if(ascending)
        		return Float.compare(Math.abs(x.getScore()), Math.abs(y.getScore()));
        	else
        		return Float.compare(Math.abs(y.getScore()), Math.abs(x.getScore()));
        case PEARSON_CORRELATION:
        	if(ascending)
        		return Double.compare(mindyData.getCorrelation(x.getTarget()),mindyData.getCorrelation( y.getTarget()));
        	else
        		return Double.compare(mindyData.getCorrelation(y.getTarget()),mindyData.getCorrelation( x.getTarget()));

        	 // tmp fix to sort by symbol names, can now use MindyPlugin.getMarkerDisplayName
        case MODULATOR:
        	if (showProbeName){
        		if(ascending)
            		return mindyData.getGeneNameSortKey(x.getModulator()).compareTo(mindyData.getGeneNameSortKey(y.getModulator()));
            	else
            		return mindyData.getGeneNameSortKey(y.getModulator()).compareTo(mindyData.getGeneNameSortKey(x.getModulator()));
        	} else {
            	if(ascending)
            		return (x.getModulator().getGeneName()).compareTo(y.getModulator().getGeneName());
            	else
            		return (y.getModulator().getGeneName()).compareTo(x.getModulator().getGeneName());
        	}
        case TARGET:
        	if (showProbeName){
            	if(ascending)
            		return mindyData.getGeneNameSortKey(x.getTarget()).compareTo(mindyData.getGeneNameSortKey(y.getTarget()));
            	else
            		return mindyData.getGeneNameSortKey(y.getTarget()).compareTo(mindyData.getGeneNameSortKey(x.getTarget()));
        	} else {
            	if(ascending)
            		return (x.getTarget().getGeneName()).compareTo(y.getTarget().getGeneName());
            	else
            		return (y.getTarget().getGeneName()).compareTo(x.getTarget().getGeneName());
        	}
        }
		return 0;
	}

}
