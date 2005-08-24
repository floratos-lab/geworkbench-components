package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSAffyMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;


/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * Set to "missing" all measurements from an Affy array whose detection call is
 * among a user-specified subset of P (present), A (absent) or M (marginal).
 * The user preferences are collected in this filter's associate parameters
 * GUI (<code>AffyDetectionCallFilterPanel</code>).
 */
public class AffyDetectionCallFilter extends AbstractAnalysis implements FilteringAnalysis {
    protected boolean filterPresent = false;
    protected boolean filterAbsent = false;
    protected boolean filterMarginal = false;

    public AffyDetectionCallFilter() {
        setLabel("Affy detection call filter");
        setDefaultPanel(new AffyDetectionCallFilterPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.AFFY_DETECTION_CALL_FILTER;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null) return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) input;
        // Collect the parameters needed for the execution of the filter
        filterPresent = ((AffyDetectionCallFilterPanel) aspp).isPresentSelected();
        filterAbsent = ((AffyDetectionCallFilterPanel) aspp).isAbsentSelected();
        filterMarginal = ((AffyDetectionCallFilterPanel) aspp).isMarginalSelected();
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        for (int i = 0; i < arrayCount; i++) {
            DSMicroarray mArray = maSet.get(i);
            for (int j = 0; j < markerCount; ++j) {
                DSMutableMarkerValue mv = (DSMutableMarkerValue) mArray.getMarkerValue(j);
                if ((mv instanceof DSAffyMarkerValue)) {
                    if (shouldBeFiltered((mv)))
                        mv.setMissing(true);
                } else
                    return new AlgorithmExecutionResults(false, "This filter can only be used with Affymetrix datasets", null);
            }

        }

        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * Check is the argument marker value meets the criteria to be set to "missing".
     *
     * @param mv
     * @return
     */
    private boolean shouldBeFiltered(DSMarkerValue mv) {
        if (mv instanceof DSAffyMarkerValue) {
            DSAffyMarkerValue amv = (DSAffyMarkerValue) mv;
            if (amv == null || mv.isMissing()) {
                return false;
            }
            if (amv.isPresent() && filterPresent) {
                return true;
            }
            if (amv.isAbsent() && filterAbsent) {
                return true;
            }
            if (amv.isMarginal() && filterMarginal) {
                return true;
            }
            return false;
        } else {
            if (mv == null || mv.isMissing()) return false;
            if (!mv.isMissing() && filterPresent) return true;
            if (mv.isMissing() && filterAbsent) return true;
            if ((mv.getConfidence() > 0.33) && (mv.getConfidence() <= 0.66) && filterMarginal) return true;
            return false;
        }
    }

    public String getType() {
        return "Affy detection call filter";
    }
}
