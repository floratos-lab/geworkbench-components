package org.geworkbench.components.microarrays;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Vector;

import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XisSymbolic; 
import org.jfree.data.xy.XYSeriesCollection;
 
/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author Min You
 * @version $Id: XSymbolicDataset.java,v 1.1 2008-03-19 20:12:18 my2248 Exp $
 */
public class XSymbolicDataset extends XYSeriesCollection
                                    implements XisSymbolic {
  
	 /** The series count. */
    private static final int DEFAULT_SERIES_COUNT = 1;

    /** The item count. */
    private static final int DEFAULT_ITEM_COUNT = 50;
    
    /** The series index. */
    private int serie;
    
    /** The item index. */
    private int item;

    /** The series names. */
    private String[] serieNames;

    /** The x values. */
    private Double[][] yValues;

    /** The y values. */
    private Integer[][] xValues;

    /** The y symbolic values. */
    private String[] xSymbolicValues;

    /** The dataset name. */
    private String datasetName;

    public XSymbolicDataset(){
    	super();
    };
    public XSymbolicDataset(String datasetName, int xRange, String[] tabString) {
        this(datasetName, xRange, tabString, DEFAULT_SERIES_COUNT, DEFAULT_ITEM_COUNT, null);
    }

    /**
     * Creates a new sample dataset.
     *
     * @param datasetName  the dataset name.
     * @param yRange  the upper limit of the (random) x-values.
     * @param tabString  the symbolic y-values.
     * @param seriesCount  the number of series to create.
     * @param itemCount  the number of items to create for each series.
     * @param serieNames  the series names.
     */
    public XSymbolicDataset(String datasetName, 
                                  int yRange,
                                  String[] tabString, 
                                  int seriesCount, 
                                  int itemCount, 
                                  String[] serieNames) {

        this.datasetName = datasetName;
        this.xSymbolicValues = tabString;
        this.serie = seriesCount;
        this.item = itemCount;
        this.serieNames = serieNames;
        this.yValues = new Double[seriesCount][itemCount];
        this.xValues = new Integer[seriesCount][itemCount];

        for (int s = 0; s < seriesCount; s++) {
            for (int i = 0; i < itemCount; i++) {
                double y = Math.random() * yRange;
                double x = Math.random() * tabString.length;
                this.yValues[s][i] = new Double(y);
                this.xValues[s][i] = new Integer((int) x);
            }
        }
    }
    
    
    
    
    /**
     * Creates a new sample dataset.
     *
     * @param datasetName  the dataset name.
     * @param xValues  the x values.
     * @param yValues  the y values.
     * @param ySymbolicValues  the y symbols
     * @param seriesCount  the series count.
     * @param itemCount  the item count.
     * @param serieNames  the series names.
     */
    public XSymbolicDataset(String datasetName, 
                                  Double[][] yValues,
                                  Integer[][] xValues, 
                                  String[] xSymbolicValues,
                                  int seriesCount, 
                                  int itemCount, 
                                  String[] serieNames) {
        
    	this.datasetName = datasetName;
        this.yValues = yValues;
        this.xValues = xValues;
        this.xSymbolicValues = xSymbolicValues;
        this.serie = seriesCount;
        this.item = itemCount;
        this.serieNames = serieNames;

    }


    /**
     * Returns the x-value for the specified series and item.  Series are
     * numbered 0, 1, ...
     *
     * @param series  the index (zero-based) of the series.
     * @param item  the index (zero-based) of the required item.
     *
     * @return the x-value for the specified series and item.
     */
    public double getYValue(int series, int item) {
        return this.yValues[series][item];
    }

    /**
     * Returns the x-value for the specified series and item.  Series are
     * numbered 0, 1, ...
     *
     * @param series  the index (zero-based) of the series.
     * @param item  the index (zero-based) of the required item.
     *
     * @return the x-value for the specified series and item.
     */
    public double getXValue(int series, int item) {
        return this.xValues[series][item];
    }
    
    public Number getX(int series, int item) {
        return this.xValues[series][item];
    }
    
    
    public Number getY(int series, int item) {
        return this.yValues[series][item];
    }

    /**
     * Sets the y-value for the specified series and item with the specified
     * new <CODE>Number</CODE> value.  Series are numbered 0, 1, ...
     * <P>
     * This method is used by combineYSymbolicDataset to modify the reference
     * to the symbolic value ...
     *
     * @param series  the index (zero-based) of the series.
     * @param item  the index (zero-based) of the required item.
     * @param newValue the value to set.
     */
    public void setXValue(int series, int item, Number newValue) {
        this.xValues[series][item] = (Integer) newValue;
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return The number of series in the dataset.
     */
    public int getSeriesCount() {
        return this.serie;
    }

    /**
     * Returns the name of the series.
     *
     * @param series  the index (zero-based) of the series.
     *
     * @return the name of the series.
     */
    public String getSeriesName(int series) {
        if (this.serieNames != null) {
            return this.serieNames[series];
        }
        else {
            return this.datasetName + series;
        }
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param series The index (zero-based) of the series.
     *
     * @return the number of items in the specified series.
     */
    public int getItemCount(int series) {
        return this.item;
    }

    /**
     * Returns the list of symbolic values.
     *
     * @return array of symbolic value.
     */
    public String[] getXSymbolicValues() {
        return this.xSymbolicValues;
    }

    /**
     * Sets the list of symbolic values.
     *
     * @param sValues the new list of symbolic value.
     */
    public void setXSymbolicValues(String[] sValues) {
        this.xSymbolicValues = sValues;
    }

    /**
     * Returns the symbolic value of the data set specified by
     * <CODE>series</CODE> and <CODE>item</CODE> parameters.
     *
     * @param series value of the serie.
     * @param item value of the item.
     *
     * @return the symbolic value.
     */
    public String getXSymbolicValue(int series, int item) {
        Integer intValue = new Integer((new Double(getXValue(series, item))).intValue());
        return getXSymbolicValue(intValue);
    }

    /**
     * Returns the symbolic value linked with the specified <CODE>Integer</CODE>.
     *
     * @param val value of the integer linked with the symbolic value.
     *
     * @return the symbolic value.
     */
    public String getXSymbolicValue(Integer val) {
        return this.xSymbolicValues[val.intValue()];
    }

    
   

    /**
     * Clone the SampleYSymbolicDataset object
     *
     * @return the cloned object.
     */
    public Object clone() {
        String nDatasetName = new String(this.datasetName);
        Double[][] nYValues = (Double[][]) cloneArray(this.yValues);
        Integer[][] nXValues = (Integer[][]) cloneArray(this.xValues);
        String[] nXSymbolicValues = (String[]) cloneArray(this.xSymbolicValues);
        int serie = this.serie;
        int item = this.item;
        String[] serieNames = (String[]) cloneArray(this.serieNames);
        return new XSymbolicDataset(nDatasetName, nYValues, nXValues,
                nXSymbolicValues, serie, item, serieNames);
    }

    /**
     * Clones the array.
     *
     * @param arr  the array.
     *
     * @return an array.
     */
    private static Object cloneArray(Object arr) {

        if (arr == null) {
            return arr;
        }

        Class cls = arr.getClass();
        if (!cls.isArray()) {
            return arr;
        }

        int length = Array.getLength(arr);
        Object[] newarr = (Object[]) Array.newInstance(cls.getComponentType(), length);

        Object obj;

        for (int i = 0; i < length; i++) {
            obj = Array.get(arr, i);
            if (obj.getClass().isArray()) {
                newarr[i] = cloneArray(obj);
            }
            else {
                newarr[i] = obj;
            }
        }

        return newarr;
     }
    
    
    
}