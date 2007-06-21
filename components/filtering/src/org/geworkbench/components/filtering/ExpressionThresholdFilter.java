package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.engine.management.Script;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * Implementation of an expression threshold filter that sets to "missing"
 * all marker values whose measurement levels are outside a user defined
 * range.
 */
public class ExpressionThresholdFilter extends AbstractAnalysis implements FilteringAnalysis {
    // Static fields used to designate the user option available from the
    // combo box within the normalizer's parameters panel.
    public static final int INSIDE_RANGE = 0;
    public static final int OUTSIDE_RANGE = 1;
    /**
     * The lower end of the allowed expression range.
     */
    protected double minValue;
    /**
     * The upper end of the allowed expression range.
     */
    protected double maxValue;
    /**
     * The option (outside or inside) to use when evaluation a marker value
     * against a range.
     */
    protected int rangeOption;

    public ExpressionThresholdFilter() {
        setLabel("Expression threshold filter");
        setDefaultPanel(new ExpressionThresholdFilterPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.EXPRESSION_THRESHOLD_FILTER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) input;
        // Collect the parameters needed for the execution of the filter
        minValue = ((ExpressionThresholdFilterPanel) aspp).getLowerBound();
        maxValue = ((ExpressionThresholdFilterPanel) aspp).getUpperBound();
        rangeOption = ((ExpressionThresholdFilterPanel) aspp).getRangeOption();
        int markerCount = maSet.getMarkers().size();
        int arrayCount = maSet.size();
        DSMicroarray microarray = null;
        for (int i = 0; i < arrayCount; i++) {
            DSMicroarray mArray = maSet.get(i);
            for (int j = 0; j < markerCount; ++j) {
                DSMutableMarkerValue mv = (DSMutableMarkerValue) mArray.getMarkerValue(j);
                if (shouldBeFiltered(mv))
                    mv.setMissing(true);
            }

        }

        return new AlgorithmExecutionResults(true, "No errors.", input);
    }

    /**
     * Check if the signal for the argument marker value respect the
     * range restrictions specified by the user.
     *
     * @param mv
     * @return
     */
    private boolean shouldBeFiltered(DSMarkerValue mv) {
        if (mv == null || mv.isMissing())
            return false;
        double signal = mv.getValue();
        if (rangeOption == INSIDE_RANGE)
            if ((signal >= minValue && signal <= maxValue))
                return true;
        if (rangeOption == OUTSIDE_RANGE)
            if ((signal < minValue || signal > maxValue))
                return true;
        return false;
    }

    @Script
    public boolean filter(DSMicroarraySet input, int the_minValue, int the_maxValue, String type){
        minValue = the_minValue;
        maxValue = the_maxValue;
        if(type.equalsIgnoreCase("Inside range")){
            rangeOption = INSIDE_RANGE;
        }else{
            rangeOption = OUTSIDE_RANGE;
        }
        execute(input);
        return true;
    }
}

