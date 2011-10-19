package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
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
 * Replaces missing values with either the mean value of their corresponding
 * marker or the mean value of the microarray they belong to.
 */
public class MissingValueNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
	private static final long serialVersionUID = -1660053432440941051L;
	
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

    public MissingValueNormalizer() {
        setDefaultPanel(new MissingValueNormalizerPanel());
    }

    public int getAnalysisType() {
        return MISSING_VALUE_NORMALIZER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null || !(input instanceof DSMicroarraySet))
            return new AlgorithmExecutionResults(false, "Invalid input.", null);

        // Set things up for the calculations to begin.
        DSMicroarraySet maSet = (DSMicroarraySet) input;
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();

        DSMutableMarkerValue markerValue = null;
        double meanValue = 0.0;
        int totalPresent;
        // Collect the parameters needed for the execution of the normalizer
        averagingType = ((MissingValueNormalizerPanel) aspp).getAveragingType();
        if (averagingType == MARKER_PROFILE_MEAN) {
            for (int i = 0; i < markerCount; ++i) {
                meanValue = 0.0;
                totalPresent = 0;
                for (int j = 0; j < arrayCount; ++j) {
                    markerValue = (DSMutableMarkerValue) maSet.get(j).getMarkerValue(i);
                    if (!markerValue.isMissing()) {
                        meanValue += markerValue.getValue();
                        ++totalPresent;
                    }

                }

                // Calculate the mean.
                if (totalPresent > 0)
                    meanValue /= totalPresent;
                // Check if there are missing values for the marker at hand before
                // proceeding with the replacement.
                if (totalPresent < arrayCount)
                    for (int j = 0; j < arrayCount; ++j) {
                        markerValue = (DSMutableMarkerValue) maSet.get(j).getMarkerValue(i);
                        if (markerValue.isMissing()) {
                            markerValue.setValue(meanValue);
                            markerValue.setMissing(false);
                        }

                    }

            }

        } else {  // that is, if avaragingType == MICROARRAY_MEAN
            for (int i = 0; i < arrayCount; ++i) {
                meanValue = 0.0;
                totalPresent = 0;
                for (int j = 0; j < markerCount; ++j) {
                    markerValue = (DSMutableMarkerValue) maSet.get(i).getMarkerValue(j);
                    if (!markerValue.isMissing()) {
                        meanValue += markerValue.getValue();
                        ++totalPresent;
                    }

                }

                // Calculate the mean.
                if (totalPresent > 0)
                    meanValue /= totalPresent;
                // Check if there are missing values for the marker at hand before
                // proceeding with the replacement.
                if (totalPresent < markerCount)
                    for (int j = 0; j < markerCount; ++j) {
                        markerValue = (DSMutableMarkerValue) maSet.get(i).getMarkerValue(j);
                        if (markerValue.isMissing()) {
                            markerValue.setValue(meanValue);
                            markerValue.setMissing(false);
                        }

                    }

            }

        }

        // add to history
        HistoryPanel.addHistoryDetail(maSet,((MissingValueNormalizerPanel) aspp).getParamDetail());

        return new AlgorithmExecutionResults(true, "No errors", maSet);
    }

    public String getType() {
        return "Missing Value Normalizer";
    }

}

