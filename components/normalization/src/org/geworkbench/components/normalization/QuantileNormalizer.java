package org.geworkbench.components.normalization;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University</p>
 * @author non attributable
 * @version $Id$
 */

/**
 * Applies the quantile multi-array normalization which induces the same distribution
 * of values across all microarrays within a microarray set. In this implementation,
 * normalization is applied at the probeset level data (rathern than the individual
 * probes).
 *
 * Given a microarray set with N arrays and M markers, the normalizations works
 * by building an NxM matrix, with one row for each array and one column for
 * each marker. First each row is sorted. In the resulting array, each value
 * across a column is replaced by the column average. Finally, the values within each
 * row are returned to their original order (before the sorting), thus
 * reinstatating the correspondence of each matrix row to a single marker.
 *
 * Details of the method can be found in a short paper by Ben Bolstad:
 *    "Probe Level Quantile Normalization of High Density Oligonucleotide Array Data"
 *
 * Although the description is given in the context of probe-level normalization,
 * the exact same approach can be used ar the probeset level.
 */
public class QuantileNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5147146459001795596L;
	
	private static Log log = LogFactory.getLog(QuantileNormalizer.class);
	
	// Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    public static final int MARKER_PROFILE_MEAN = 0;
    public static final int MICROARRAY_MEAN = 1;
    /**
     * Code describing the user-specified preference for the averaging method to
     * use. It takes one of the values <code>MARKER_PROFILE_MEAN</code>,
     * <code>MICROARRAY_MEAN</code>.
     */
    int averagingType = -1;

    int arrayCount;   // The number of arrays in the input dataset.
    int markerCount;  // The number of markers in the input dataset.

    public QuantileNormalizer() {
        setDefaultPanel(new QuantileNormalizerPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.QUANTILE_NORMALIZER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null || !(input instanceof DSMicroarraySet))
            return new AlgorithmExecutionResults(false, "Invalid input.", null);

        DSMicroarraySet maSet = (DSMicroarraySet) input;
        
        arrayCount  = maSet.size();
        markerCount = maSet.getMarkers().size();
        if (arrayCount < 2)
            return new AlgorithmExecutionResults(false, "Data set must have at least 2 microarrays", input);

        DSMutableMarkerValue [][] arrays = new DSMutableMarkerValue[arrayCount][markerCount];

        // Collect the parameters needed for the execution of the normalizer
        averagingType = ((QuantileNormalizerPanel) aspp).getAveragingType();

        // Replace missing values with the average specified
        replaceMissingValues(maSet);

        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex){
            DSMarkerValue[] anArray = maSet.
                                              get(arrayIndex).getMarkerValues();
            for (int markerIndex = 0; markerIndex < markerCount; ++markerIndex) {
            	if(! (anArray[markerIndex] instanceof DSMutableMarkerValue) ) {
            		// this may happen after future design improvement. in that case the the value need to set at microarray set level
            		log.error("anArray[markerIndex] is "+ arrays[arrayIndex][markerIndex].getClass().getName()+", not DSMutableMarkerValue");
            		return new AlgorithmExecutionResults(false, "mutable marker value expected", null);
            	}
                arrays[arrayIndex][markerIndex] = (DSMutableMarkerValue) anArray[markerIndex]; //new CSExpressionMarkerValue((float) anArray[markerIndex].getValue());
            }

            Arrays.sort(arrays[arrayIndex], new MarkerValueComparator<DSMarkerValue>());
        }

        for (int markerIndex = 0; markerIndex < markerCount; ++markerIndex){
            double mean = getMeanValue(arrays, markerIndex);
            for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex)
                arrays[arrayIndex][markerIndex].setValue(mean);
        }

		// add to history
        HistoryPanel.addHistoryDetail(maSet,((QuantileNormalizerPanel) aspp).getParamDetail());

        return new AlgorithmExecutionResults(true, "No errors", input);
    }


    /**
     * Replace missing values in the input microarray set with either the average
     * value of either their corresponding marker or their correponding microarray,
     * per the preference set in the parameters panel.
     */
    private void replaceMissingValues(DSMicroarraySet maSet){
        DSMutableMarkerValue markerValue = null;
        double meanValue = 0.0;
        int nonMissingCount;

        if (averagingType == MARKER_PROFILE_MEAN) {
            for (int i = 0; i < markerCount; ++i) {
                meanValue = 0.0;
                nonMissingCount = 0;
                for (int j = 0; j < arrayCount; ++j) {
                    markerValue = (DSMutableMarkerValue) maSet.get(j).
                                  getMarkerValue(i);
                    if (!markerValue.isMissing()) {
                        meanValue += markerValue.getValue();
                        ++nonMissingCount;
                    }
                }

                // Calculate the mean.
                if (nonMissingCount > 0)
                    meanValue /= nonMissingCount;
                // Check if there are missing values for the marker at hand before
                // proceeding with the replacement.
                if (nonMissingCount < arrayCount)
                    for (int j = 0; j < arrayCount; ++j) {
                        markerValue = (DSMutableMarkerValue) maSet.get(j).
                                      getMarkerValue(i);
                        if (markerValue.isMissing()) {
                            markerValue.setValue(meanValue);
                            markerValue.setMissing(false);
                        }
                    }
            }
        } else { // that is, if avaragingType == MICROARRAY_MEAN
            for (int i = 0; i < arrayCount; ++i) {
                meanValue = 0.0;
                nonMissingCount = 0;
                for (int j = 0; j < markerCount; ++j) {
                    markerValue = (DSMutableMarkerValue) maSet.get(i).
                                  getMarkerValue(j);
                    if (!markerValue.isMissing()) {
                        meanValue += markerValue.getValue();
                        ++nonMissingCount;
                    }

                }

                // Calculate the mean.
                if (nonMissingCount > 0)
                    meanValue /= nonMissingCount;
                // Check if there are missing values for the marker at hand before
                // proceeding with the replacement.
                if (nonMissingCount < markerCount)
                    for (int j = 0; j < markerCount; ++j) {
                        markerValue = (DSMutableMarkerValue) maSet.get(i).
                                      getMarkerValue(j);
                        if (markerValue.isMissing()) {
                            markerValue.setValue(meanValue);
                            markerValue.setMissing(false);
                        }
                    }
            }
        }
    }
    /**
     * Return the mean value of the <code>colIndex</code>-th column of the
     * <code>arrays[][]</code> matrix.
     *
     * @param arrays DSMutableMarkerValue[][]
     * @param colIndex int
     * @return double
     */
    private double getMeanValue(DSMutableMarkerValue [][] arrays, int colIndex){
        double   sumOfValues = 0.0;
        int      nonMissing  = 0;

        // Computation excludes missing values
        for (int arrayIndex = 0; arrayIndex < arrayCount; arrayIndex++)
            if (!arrays[arrayIndex][colIndex].isMissing()){
                ++nonMissing;
                sumOfValues += arrays[arrayIndex][colIndex].getValue();
            }

        return (nonMissing != 0 ? sumOfValues / nonMissing : 0);
    }

    static class MarkerValueComparator<T extends DSMarkerValue> implements Comparator<T>{
        public int compare(T v1, T v2){
            if (v1 != null && v2!= null){
                if (v1.getValue() == v2.getValue())
                    return 0;
                else if (v1.getValue() < v2.getValue())
                    return -1;
                else if (v1.getValue() > v2.getValue())
                    return 1;
            }
            return 0;
        }


    }
}

