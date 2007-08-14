package org.geworkbench.components.mindy;

import java.util.Comparator;

import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;

/**
 * Compare two MINDY result rows (for sorting).
 * @author ch2514
 * @version $Id: MindyRowComparator.java,v 1.4 2007-08-14 19:53:23 hungc Exp $
 */
public class MindyRowComparator implements Comparator<MindyData.MindyResultRow> {
	/**
	 * Sorting mode - compare MINDY result rows based on their scores. 
	 */
	public static final int SCORE = 1;
	public static final int DELTA_I = 2;
	public static final int PEARSON_CORRELATION = 3;
    
	// variable
    private int mode;
    private boolean ascending;
    
    /**
     * @param mode - how to compare two rows
     * @param ascending (for sorting) - If true, the rows are sorted in ascending order
     * as per sorting mode.
     */
    public MindyRowComparator(int mode, boolean ascending){
    	this.mode = mode;
    	this.ascending = ascending;
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
	public int compare(MindyData.MindyResultRow x, MindyData.MindyResultRow y) {
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
        		return Double.compare(x.getCorrelation(), y.getCorrelation());
        	else
        		return Double.compare(y.getCorrelation(), x.getCorrelation());
        }
		return 0;
	}

}
