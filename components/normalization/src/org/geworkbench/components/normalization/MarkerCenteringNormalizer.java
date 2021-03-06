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
 * For every profile, subtracts the mean or median profile value from each
 * profile measurement. The mean/median is computed only from the non-missing
 * values in the profile. The normalizer also offers options for handling missing
 * values. In particular, the available choices are:
 * <UL>
 * <LI>Min: replace with the smallest profile value resulting after the
 * mean/median subtraction,</LI>
 * <LI>Max: replace with the largest profile value resulting after the
 * mean/median subtraction,</LI>
 * <LI>Zero: replace with 0,</LI>
 * <LI>Ignore: No change.</LI>.
 * </UL>
 */
public class MarkerCenteringNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
	private static final long serialVersionUID = -6083405113656563521L;
	
	int meanMedianType;
    int missingValues;

    public MarkerCenteringNormalizer() {
        setDefaultPanel(new CenteringNormalizerPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.MARKER_MEAN_MEDIAN_CENTERING_NORMALIZER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null || !(input instanceof DSMicroarraySet))
            return new AlgorithmExecutionResults(false, "Invalid input.", null);

        // Collect the parameters needed for the execution of the normalizer
        meanMedianType = ((CenteringNormalizerPanel) aspp).getAveragingSelection();
        missingValues = ((CenteringNormalizerPanel) aspp).getMissingValueTreatment();

        DSMicroarraySet maSet = (DSMicroarraySet) input;
        double[] profile = null;
        DSMutableMarkerValue markerValue = null;
        double signal = 0.0d;
        double meanMedian = 0.0d;
        double minValue;
        double maxValue;
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        // Go over each marker and compute that marker's "center" point from all
        // its values across all microarrays in the set. Then, substract this
        // cetner point from all that marker's values.

        for (int i = 0; i < markerCount; i++) {
            profile = getProfile(maSet, i);
            Arrays.sort(profile);
            meanMedian = (meanMedianType == CenteringNormalizerPanel.MEAN ? getMean(profile) : getMedian(profile));
            // Calculate the post-mean/median subtraction minimum & maximum values.
            for (int j = 0; j < profile.length; ++j)
                profile[j] -= meanMedian;
            minValue = profile[0];
            maxValue = profile[profile.length - 1];
            for (int j = 0; j < arrayCount; j++) {
                markerValue = (DSMutableMarkerValue) maSet.get(j).getMarkerValue(i);
                if (!markerValue.isMissing()) {
                    signal = markerValue.getValue();
                    markerValue.setValue(signal - meanMedian);
                } else {
                    if (missingValues == CenteringNormalizerPanel.MINIMUM)
                        markerValue.setValue(minValue);
                    else if (missingValues == CenteringNormalizerPanel.MAXIMUM)
                        markerValue.setValue(maxValue);
                    else if (missingValues == CenteringNormalizerPanel.ZERO)
                        markerValue.setValue(0.0d);
                    else if (missingValues == CenteringNormalizerPanel.IGNORE) {
                        //Do nothing
                    }

                    if (missingValues != CenteringNormalizerPanel.IGNORE)
                        markerValue.setMissing(false);
                }

            }

        }

		// add to history
        HistoryPanel.addHistoryDetail(maSet,((CenteringNormalizerPanel) aspp).getParamDetail());

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

    /*
    * @param profile
    * @return
    */
   private double getMedian(double[] profile) {
       if (profile == null)
           return 0.0;
       int  modVal = 0;
       double median = 0.0d;

       modVal = profile.length % 2;

       if ( modVal == 0 )
       {
    	  median = profile[profile.length / 2 - 1] + profile[profile.length / 2 ];
    	  median = median / 2;

       }
       else if ( modVal == 1 )
    	   median =  profile[profile.length / 2];

       return median;
   }




}

