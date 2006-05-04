package org.geworkbench.components.alignment.synteny;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class FeatureMatrixObj {

    private static String featureName;
    private static String xsource;
    private static String ysource;
    private static int xnum;
    private static int ynum;
    private static int max_value;
    private static int min_value;
    private static String[] xnames;
    private static String[] ynames;
    private static int[] xstart;
    private static int[] xend;
    private static int[] ystart;
    private static int[] yend;
    private static int[][] array;
    private static String[] URL;

    public FeatureMatrixObj(String fn, String srcx, String srcy, int nx, int ny,
                            String[] xnam,
                            String[] ynam, int[] xs, int[] xe, int[] ys,
                            int[] ye, int[][] ar) {

        int i, j, max, min;

        featureName = fn;
        xsource = srcx;
        ysource = srcy;
        xnum = nx;
        ynum = ny;
        array = ar;
        xnames = xnam;
        ynames = ynam;
        xstart = xs;
        ystart = ys;
        xend = xe;
        yend = ye;

        max = (Integer.MIN_VALUE);
        min = (Integer.MAX_VALUE);
        for (i = 0; i < xnum; i++) {
            for (j = 0; j < ynum; j++) {
                if (max < array[i][j]) {
                    max = array[i][j];
                }
                if (min > array[i][j]) {
                    min = array[i][j];
                }
            }
        }
    max_value=max;
    min_value=min;
    }

    /* Accessors */
    public String getFeatureName() {
        return featureName;
    }

    /* Source: file or chromosome num+genome, or some comments */
    public String getXsource() {
        return xsource;
    }
    public String getYsource() {
        return ysource;
    }

    /* Number of features */
    public int getXnum() {
        return xnum;
    }

    public int getYnum() {
        return ynum;
    }

    /* Array */
    public int[][] getArray() {
        return array;
    }

    /* Array cell */
    public int getArrayCell(int i, int j) {
        return array[i][j];
    }

    /* Feature coordinates */
    public int getXfeatureStart(int i) {
        if (i < xnum) {
            return xstart[i];
        }
        else {
            return -1;
        }
    }

    public int getXfeatureEnd(int i) {
        if (i < xnum) {
            return xend[i];
        }
        else {
            return -1;
        }
    }

    public int getYfeatureStart(int i) {
        if (i < ynum) {
            return ystart[i];
        }
        else {
            return -1;
        }
    }

    public int getYfeatureEnd(int i) {
        if (i < ynum) {
            return yend[i];
        }
        else {
            return -1;
        }
    }

    /* Feature names */
    public String getXname(int i) {
        if (i < xnum) {
            return xnames[i];
        }
        else {
            return "Wrong index";
        }
    }

    public String getYname(int i) {
        if (i < ynum) {
            return ynames[i];
        }
        else {
            return "Wrong index";
        }
    }

    /* min and max values */
    public int getMax() {
        return max_value;
    }

    public int getMin() {
        return min_value;
    }

}
