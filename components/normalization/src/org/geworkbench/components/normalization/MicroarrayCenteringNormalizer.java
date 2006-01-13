package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;

import java.util.Arrays;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * For every microarray, computes the mean or median value of the microarray
 * and proceed to subtract this value from each marker value in the array.
 * The mean/median is computed only from the non-missing values in the array.
 * The normalizer also offers options for handling missing
 * values. In particular, the available choices are:
 * <UL>
 * <LI>�Min�: replace with the smallest microarray value resulting after the
 * mean/median subtraction),</LI>
 * <LI>�Max�: replace with the largest microarray value resulting after the
 * mean/median subtraction),</LI>
 * <LI>�Zero�: replace with 0,</LI>
 * <LI>�Ignore�: No change.</LI>.
 * </UL>
 */
public class MicroarrayCenteringNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
    // Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    public static final int MEAN = 0;
    public static final int MEDIAN = 1;
    public static final int MINIMUM = 0;
    public static final int MAXIMUM = 1;
    public static final int ZERO = 2;
    public static final int IGNORE = 3;
    int meanMedianType;
    int missingValues;

    public MicroarrayCenteringNormalizer() {
        setLabel("Array-based centering");
        setDefaultPanel(new MarkerCenteringNormalizerPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.MICROARRAY_MEAN_MEDIAN_CENTERING_NORMALIZER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        // Collect the parameters needed for the execution of the normalizer
        meanMedianType = ((MarkerCenteringNormalizerPanel) aspp).getAveragingSelection();
        missingValues = ((MarkerCenteringNormalizerPanel) aspp).getMissingValueTreatment();
        // Variables needed for the computations.
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) input;
        DSMicroarray mArray = null;
        double[] arrayValues = null;
        DSMutableMarkerValue markerValue = null;
        double signal = 0.0d;
        double meanMedian = 0.0d;
        double minValue;
        double maxValue;
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        // Go over each array and compute that arrays's "center" point using all
        // marker values in the microarray. Then, substract this
        // center point from the values of all markers within the microarray.
        for (int i = 0; i < arrayCount; i++) {
            arrayValues = getNonMissingValues(maSet, i);
            Arrays.sort(arrayValues);
            meanMedian = (meanMedianType == MEAN ? getMean(arrayValues) : getMedian(arrayValues));
            // Calculate the (post-mean/median subtraction) minimum & maximum values.
            for (int j = 0; j < arrayValues.length; ++j)
                arrayValues[j] -= meanMedian;
            minValue = arrayValues[0];
            maxValue = arrayValues[arrayValues.length - 1];
            mArray = maSet.get(i);
            for (int j = 0; j < markerCount; j++) {
                markerValue = (DSMutableMarkerValue) mArray.getMarkerValue(j);
                if (!markerValue.isMissing()) {
                    signal = markerValue.getValue();
                    markerValue.setValue(signal - meanMedian);
                } else {
                    if (missingValues == MINIMUM)
                        markerValue.setValue(minValue);
                    else if (missingValues == MAXIMUM)
                        markerValue.setValue(maxValue);
                    else if (missingValues == ZERO)
                        markerValue.setValue(0.0d);
                    else if (missingValues == IGNORE) {
                        //Do nothing
                    }

                    if (missingValues != IGNORE)
                        markerValue.setMissing(false);
                }

            }

        }

        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * Obtain the non-missing values for the index-th microarray within the array set.
     *
     * @param microarraySet The reference microarray set.
     * @param indedx        The index of a microarray.
     * @return A <code>double[]</code> array containing only the
     *         non-missing values for the designated microarray.
     */
    double[] getNonMissingValues(DSMicroarraySet<DSMicroarray> maSet, int index) {
        if (maSet == null || index < 0 || index >= maSet.size())
            return null;
        int markerCount = maSet.getMarkers().size();
        int nonMissing = 0;
        DSMicroarray mArray = maSet.get(index);
        // For space allocation purposes, first compute the number of non-missing values.
        for (int i = 0; i < markerCount; i++)
            if (!mArray.getMarkerValue(i).isMissing())
                ++nonMissing;
        // Allocate the necessary space
        double[] profile = new double[nonMissing];
        // Fill-in the data.
        for (int i = 0, j = 0; i < markerCount; i++) {
            DSMarkerValue mv = mArray.getMarkerValue(i);
            if (!mv.isMissing())
                profile[j++] = mv.getValue();
        }

        return profile;
    }

    /**
     * Compute the mean value for the numbers in <code>profile</code>.
     *
     * @param profile
     * @return
     */
    private double getMean(double[] profile) {
        if (profile == null)
            return 0.0;
        int totalPresent = 0;
        double sum = 0.0d;
        for (int i = 0; i < profile.length; i++)
            sum += profile[i];
        if (profile.length > 0)
            sum /= profile.length;
        return (double) sum;
    }


    /**
     * Compute the median value for the numbers in <code>profile</code>.
     *
     * @param profile
     * @return
     */
    private double getMedian(double[] profile) {
        if (profile == null || profile.length == 0)
            return 0.0;

        Arrays.sort(profile);
        return profile[profile.length/2];
    }

}

