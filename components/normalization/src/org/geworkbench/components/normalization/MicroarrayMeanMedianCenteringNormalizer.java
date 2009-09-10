package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;

import java.util.Arrays;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * For every microarray, subtracts the mean or median marker value from marker
 * measurement. The mean/median is computed only from the non-missing
 * marker values. The filter also offers the option to replace missing
 * values.
 */
public class MicroarrayMeanMedianCenteringNormalizer extends AbstractAnalysis {
    int _localAnalysisType;
    int _meanMedianType;
    int _missingType;

    public MicroarrayMeanMedianCenteringNormalizer() {
        this(AbstractAnalysis.MEAN_TYPE, AbstractAnalysis.IGNORE_TYPE);
    }

    public MicroarrayMeanMedianCenteringNormalizer(int meanMedianType) {
        this(meanMedianType, AbstractAnalysis.IGNORE_TYPE);
    }

    public MicroarrayMeanMedianCenteringNormalizer(int meanMedianType, int missingType) {
        _localAnalysisType = AbstractAnalysis.MICROARRAY_MEAN_MEDIAN_CENTERING_NORMALIZER_TYPE;
        _meanMedianType = meanMedianType;
        _missingType = missingType;
    }

    public int getAnalysisType() {
        return _localAnalysisType;
    }

    public AlgorithmExecutionResults execute(Object input) {
        assert input instanceof DSMicroarraySet;
        DSItemList<DSGeneMarker> markerInfo = ((DSMicroarraySet) input).getMarkers();
        int numMarkers = markerInfo.size();
        double[] profile;
        double variance = 0.0d;
        double meanMedian = 0.0d;
        DSMicroarray microarray = null;
        DSMutableMarkerValue markerValue = null;
        double signal = 0.0d;
        double minValue;
        double maxValue;
        int count = ((DSMicroarraySet) input).size();
        for (int i = 0; i < count; i++) {
            microarray = ((DSMicroarraySet<DSMicroarray>) input).get(i);
            //Need to make sure that the value for missing values don't get into the profile array
            profile = getMicroarrayProfile(microarray, markerInfo);
            //See how the sort in getMedian can be gotten rid of
            Arrays.sort(profile);
            minValue = profile[0];
            maxValue = profile[profile.length - 1];
            if (_meanMedianType == AbstractAnalysis.MEAN_TYPE)
                meanMedian = getMean(profile);
            else if (_meanMedianType == AbstractAnalysis.MEDIAN_TYPE)
                meanMedian = getMedian(profile);
            for (int j = 0; j < numMarkers; j++) {
                //markerValue = microarray.getValueForMarkerInfo(markerInfo[j]);
                markerValue = (DSMutableMarkerValue) microarray.getMarkerValue(j);
                if (!markerValue.isMissing()) {
                    signal = markerValue.getValue();
                    markerValue.setValue(signal - meanMedian);
                } else {
                    if (_missingType == AbstractAnalysis.MIN_TYPE) {
                        markerValue.setValue(minValue - meanMedian);
                    } else if (_missingType == AbstractAnalysis.MAX_TYPE) {
                        markerValue.setValue(maxValue - meanMedian);
                    } else if (_missingType == AbstractAnalysis.ZERO_TYPE) {
                        markerValue.setValue(0.0d);
                    } else if (_missingType == AbstractAnalysis.IGNORE_TYPE) {
                        //Do nothing
                    }

                }

            }

        }

        return new AlgorithmExecutionResults(true, "No errors.", input);
    }

    double[] getMicroarrayProfile(DSMicroarray microarray, DSItemList<DSGeneMarker> markers) {
        int numMarkers = markers.size();
        double[] profile = new double[numMarkers];
        for (int i = 0; i < numMarkers; i++) {
            profile[i] = microarray.getMarkerValue(markers.get(i)).getValue();
        }

        return profile;
    }

    double getMean(double[] profile) {
        int numElements = profile.length;
        double sum = 0.0d;
        for (int i = 0; i < numElements; i++) {
            sum += profile[i];
        }

        return (double) sum / numElements;
    }

    double getMedian(double[] profile) {
        Arrays.sort(profile);
        int length = profile.length;
        if (length % 2 == 1)
            return profile[length / 2 + 1];
        else
            return profile[length / 2];
    }

}

