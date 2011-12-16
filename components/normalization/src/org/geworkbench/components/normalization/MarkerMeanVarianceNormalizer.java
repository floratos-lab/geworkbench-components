package org.geworkbench.components.normalization;

import java.util.Arrays;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import org.geworkbench.builtin.projects.history.HistoryPanel;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version $Id$
 */

/**
 * For every profile, subtracts the mean profile value from each
 * profile measurement and then divides the resulting value with the profile
 * standard deviation. The mean and sd are computed only from the non-missing
 * values in the profile. The normalizer also offers options for handling missing
 * values. In particular, the available choices are:
 * <UL>
 * <LI>Min: replace with the smallest profile value resulting after the
 * mean/deviation adjustment ,</LI>
 * <LI>Max: replace with the largest profile value resulting after the
 * mean/deviation adjustment,</LI>
 * <LI>Zero: replace with 0,</LI>
 * <LI>Ignore: No change.</LI>.
 * </UL>
 */
public class MarkerMeanVarianceNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
	private static final long serialVersionUID = -2513689098793608007L;
	
	// Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    public static final int MINIMUM = 0;
    public static final int MAXIMUM = 1;
    public static final int ZERO = 2;
    public static final int IGNORE = 3;
    int missingValues;

    public MarkerMeanVarianceNormalizer() {
        setDefaultPanel(new MarkerMeanVarianceNormalizerPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.MARKER_MEAN_VARIANCE_NORMALIZER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null || !(input instanceof DSMicroarraySet))
            return new AlgorithmExecutionResults(false, "Invalid input.", null);

        // Collect the parameters needed for the execution of the normalizer
        missingValues = ((MarkerMeanVarianceNormalizerPanel) aspp).getMissingValueTreatment();

        DSMicroarraySet maSet = (DSMicroarraySet) input;
        double[] profile = null;
        DSMutableMarkerValue markerValue = null;
        double signal = 0.0d;
        double meanValue = 0.0d;
        double deviation = 0.0d;
        double minValue = 0.0d;
        double maxValue = 0.0d;
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        // Go over each marker and compute that marker's mean & variance from all
        // its values across all microarrays in the set. Then, treat all the values
        // for this marker by substracting the computed mean and dividing by the computed
        // deviation.
        for (int i = 0; i < markerCount; i++) {
            profile = getProfile(maSet, i);
            meanValue = getMean(profile);
            deviation = getDeviation(profile);
            // Calculate the minimum & maximum profile values (post-mean/variance adjustment).
            if (profile != null) {


                for (int j = 0; j < profile.length; ++j)
                    profile[j] -= meanValue;
                if (deviation != 0)
                    for (int j = 0; j < profile.length; ++j)
                        profile[j] /= deviation;
                Arrays.sort(profile);
                minValue = profile[0];
                maxValue = profile[profile.length - 1];
            } else {
                System.out.println("In microarraydataset at MVNormalizer:" + i + "th profile is null");
            }
            for (int j = 0; j < arrayCount; j++) {
                markerValue = (DSMutableMarkerValue) maSet.get(j).getMarkerValue(i);
                if (!markerValue.isMissing()) {
                    signal = markerValue.getValue() - meanValue;
                    if (deviation != 0)
                        signal /= deviation;
                    markerValue.setValue(signal);
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

		// add to history
        HistoryPanel.addHistoryDetail(maSet,((MarkerMeanVarianceNormalizerPanel) aspp).getParamDetail());

        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * Obtain the non-missing values for the index-th marker within the array set.
     *
     * @param microarraySet The reference microarray set.
     * @param indedx        The index of the marker under consideration.
     * @return A <code>double[]</code> array containing only the
     *         non-missing values for the marker.
     */
    double[] getProfile(DSMicroarraySet maSet, int index) {
        if (maSet == null || index < 0 || index >= maSet.getMarkers().size())
            return null;
        int arrayCount = maSet.size();
        int nonMissing = 0;
        // For space allocation purposes, first comput the number of non-missing values.
        for (int i = 0; i < arrayCount; i++)
            if (!maSet.get(i).getMarkerValue(index).isMissing())
                ++nonMissing;
        // Allocate the necessary space
        double[] profile = new double[nonMissing];
        // Fill-in the data.
        for (int i = 0, j = 0; i < arrayCount; i++) {
            DSMarkerValue mv = maSet.get(i).getMarkerValue(index);
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

        double sum = 0.0d;
        for (int i = 0; i < profile.length; i++)
            sum += profile[i];
        if (profile.length > 0)
            sum /= profile.length;
        return (double) sum;
    }

    /**
     * Compute the deviation for the numbers in <code>profile</code>.
     *
     * @param profile
     * @return
     */
    private double getDeviation(double[] profile) {
        if (profile == null)
            return 0.0;
        double meanValue = getMean(profile);
        double deviation = 0.0;
        for (int i = 0; i < profile.length; ++i)
            deviation += (profile[i] - meanValue) * (profile[i] - meanValue);
        if (profile.length > 1)
            deviation /= profile.length - 1;
        return Math.sqrt(deviation);
    }

}

