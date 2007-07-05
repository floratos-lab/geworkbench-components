package org.geworkbench.components.mindy;

import java.util.Comparator;

/**
 * @author mhall
 */
public class ArrayIndexComparator implements Comparator<float[]> {

    private int markerPosition;
    private boolean ascending;

    public ArrayIndexComparator(int arrayIndex, boolean ascending) {
        this.markerPosition = arrayIndex;
        this.ascending = ascending;
    }

    public int compare(float[] array1, float[] array2) {
        if (ascending) {
            return Float.compare(array1[markerPosition], array2[markerPosition]);
        } else {
            return Float.compare(array2[markerPosition], array1[markerPosition]);
        }
    }
}