package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * Applies the log2 transformation to all microarray values.
 * It fails if there are any negative measurements. Missing values remain
 * missing after the transformation. For 2 channel data, the transformation is
 * applied to the ratio and *NOT* the individual channel intensities.
 */
public class LogTransformationNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
    int analysisType;

    public LogTransformationNormalizer() {
        analysisType = AbstractAnalysis.LOG_TRANSFORMATION_NORMALIZER_TYPE;
        setLabel("Log2 Transformation");
    }

    public int getAnalysisType() {
        return analysisType;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return null;
        assert input instanceof DSMicroarraySet;
        DSItemList<DSGeneMarker> markerInfo = ((DSMicroarraySet) input).getMarkers();
        int count = ((DSMicroarraySet) input).size();
        DSMicroarray microarray = null;
        DSMutableMarkerValue markerValue = null;
        double signal;
        for (int i = 0; i < markerInfo.size(); i++) {
            for (int j = 0; j < count; j++) {
                microarray = ((DSMicroarraySet<DSMicroarray>) input).get(j);
                markerValue = microarray.getMarkerValue(markerInfo.get(i));
                signal = markerValue.getValue();
                if (signal <= 0.0d)
                    return new AlgorithmExecutionResults(false, "The dataset contains data non-positive data points", null);
                if ((!markerValue.isMissing())) {
                    markerValue.setValue(Math.log(signal) / Math.log(2.0));
                }

            }

        }

        return new AlgorithmExecutionResults(true, "No errors", input);
    }

}

