package org.geworkbench.components.mindy;

import java.util.Comparator;

/**
 * Compare two float arrays based on marker position (for sorting).
 * @author mhall
 * @version $Id: ArrayIndexComparator.java,v 1.3 2007-07-10 18:02:07 hungc Exp $
 */
public class ArrayIndexComparator implements Comparator<float[]> {

    private int markerPosition;
    private boolean ascending;

    /** 
     * @param arrayIndex - marker position with which to sort the float data array
     * @param ascending - (for sorting).  If true, the gene markers are sorted in
	 * ascending order based on the marker position.
     */
    public ArrayIndexComparator(int arrayIndex, boolean ascending) {
        this.markerPosition = arrayIndex;
        this.ascending = ascending;
    }

    /**
     * Compares two arrays based on the marker position specified in the constructor.
     * This method is for Arrays sorting to call.
     * 
     * @param array1 - first data array to be compared
     * @param array2 - second data array to be compared
     * @return A negative integer if the first array precedes the second.
     * Zero if the two arrays are the same.  
     * A positive integer if the second array precedes the first.
     */
    public int compare(float[] array1, float[] array2) {
        if (ascending) {
            return Float.compare(array1[markerPosition], array2[markerPosition]);
        } else {
            return Float.compare(array2[markerPosition], array1[markerPosition]);
        }
    }
}