package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSGenepixMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University.</p>
 * @author Xiaoqing Zhang
 * @version 1.0
 */

/**
 * Implementation of an flag-based filter for 2 channel data. The
 * class will filter out measures based on its flags.
 */
public class GenepixFlagsFilter extends AbstractAnalysis implements FilteringAnalysis {

    public static final int INSIDE_RANGE = 0;

    public static final int OUTSIDE_RANGE = 1;

    /**
     * The lower end of the Cy3 expression range.
     */
    protected double cy3Min;

    /**
     * The lower end of the Cy3 expression range.
     */
    protected double cy3Max;

    /**
     * The lower end of the Cy5 expression range.
     */
    protected double cy5Min;

    /**
     * The lower end of the Cy5 expression range.
     */
    protected double cy5Max;

    /**
     * The option (outside or inside) to use when evaluation a marker value
     * against a range.
     */
    protected int rangeOption;

    public GenepixFlagsFilter() {
        setLabel("Genepix Flags Filter");
        setDefaultPanel(new GenepixFlagsFilterPanel());
    }

    public int getAnalysisType() {
        return AbstractAnalysis.TWO_CHANNEL_THRESHOLD_FILTER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) input;
        // Collect the parameters needed for the execution of the filter
//        cy3Min = ((GenepixExpressionThresholdFilterPanel) aspp).getCy3Min();
//        cy3Max = ((GenepixExpressionThresholdFilterPanel) aspp).getCy3Max();
//        cy5Min = ((GenepixExpressionThresholdFilterPanel) aspp).getCy5Min();
//        cy5Max = ((GenepixExpressionThresholdFilterPanel) aspp).getCy5Max();
//        rangeOption = ((GenepixExpressionThresholdFilterPanel) aspp).getRangeOption();
        int markerCount = maSet.getMarkers().size();
        int arrayCount = maSet.size();
        for (int i = 0; i < arrayCount; i++) {
            DSMicroarray mArray = maSet.get(i);
            for (int j = 0; j < markerCount; ++j) {
                DSMutableMarkerValue mv = (DSMutableMarkerValue) mArray.getMarkerValue(j);
                if ((mv instanceof DSGenepixMarkerValue)) {
                    if (shouldBeFiltered((DSGenepixMarkerValue) mv))
                        mv.setMissing(true);
                } else
                    return new AlgorithmExecutionResults(false, "This filter can only be used with Genepix datasets", null);
            }
        }

        return new AlgorithmExecutionResults(true, "No errors.", input);
    }

    /**
     * Check if the 2 channel signals for the argument marker value respect the
     * range restrictions specified by the user.
     *
     * @param mv
     * @return
     */
    private boolean shouldBeFiltered(DSGenepixMarkerValue mv) {
        if (mv == null || mv.isMissing())
            return false;
        double cy3Signal = mv.getCh1Fg() - mv.getCh1Bg();
        double cy5Signal = mv.getCh2Fg() - mv.getCh2Bg();
        if (rangeOption == INSIDE_RANGE)
            if ((cy3Signal >= cy3Min && cy3Signal <= cy3Max) || ((cy5Signal >= cy5Min && cy5Signal <= cy5Max)))
                return true;
        if (rangeOption == OUTSIDE_RANGE)
            if ((cy3Signal < cy3Min || cy3Signal > cy3Max) || ((cy5Signal < cy5Min || cy5Signal > cy5Max)))
                return true;
        return false;
    }
}
