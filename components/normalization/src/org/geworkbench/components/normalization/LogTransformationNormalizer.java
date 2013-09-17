package org.geworkbench.components.normalization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version $Id$
 */

/**
 * Applies the log2 transformation to all microarray values.
 * It fails if there are any negative measurements. Missing values remain
 * missing after the transformation. For 2 channel data, the transformation is
 * applied to the ratio and *NOT* the individual channel intensities.
 */
public class LogTransformationNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
	private static final long serialVersionUID = -4074483295931773960L;
	
	private static Log log = LogFactory.getLog(LogTransformationNormalizer.class);
	
	int analysisType;

    public LogTransformationNormalizer() {
        analysisType = AbstractAnalysis.LOG_TRANSFORMATION_NORMALIZER_TYPE;
    }

    public int getAnalysisType() {
        return analysisType;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null || !(input instanceof DSMicroarraySet))
            return new AlgorithmExecutionResults(false, "Invalid input.", null);

        DSMicroarraySet maSet = (DSMicroarraySet) input;
        DSItemList<DSGeneMarker> markerInfo = maSet.getMarkers();
        int count = maSet.size();
        DSMicroarray microarray = null;
        double signal;

        // Before applying any transformation, check there are no non-positive values
        for (int i = 0; i < markerInfo.size(); i++) {
            for (int j = 0; j < count; j++) {
                microarray = maSet.get(j);
                DSMarkerValue markerValue = microarray.getMarkerValue(markerInfo.get(i));
                signal = markerValue.getValue();
                if (!markerValue.isMissing() && signal <= 0.0d)
					return new AlgorithmExecutionResults(
							false,
							"The dataset contains negative and/or zero values.\n"
									+ "Negative or zero values need to be replaced or filtered out before a log transformation can be performed.",
							null);
            }
        }

        // Now proceed with the actual transformations.
        for (int i = 0; i < markerInfo.size(); i++) {
            for (int j = 0; j < count; j++) {
                microarray = maSet.get(j);
                DSMarkerValue v = microarray.getMarkerValue(markerInfo.get(i));
            	if(! (v instanceof DSMutableMarkerValue) ) {
            		// this may happen after future design improvement. in that case the the value need to set at microarray set level
            		log.error("markerValue is "+ v.getClass().getName()+", not DSMutableMarkerValue");
            		return new AlgorithmExecutionResults(false, "mutable marker value expected", null);
            	}

                DSMutableMarkerValue markerValue = (DSMutableMarkerValue)v;
                signal = markerValue.getValue();
                if ((!markerValue.isMissing())) {
                    markerValue.setValue(Math.log(signal) / Math.log(2.0));
                }

            }

        }

        return new AlgorithmExecutionResults(true, "No errors", input);
    }

}

