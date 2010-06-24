package org.geworkbench.components.filtering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version $Id$
 */

/**
 * Implementation of an expression threshold filter that sets to "missing"
 * all marker values whose measurement levels are outside a user defined
 * range.
 */
public class ExpressionThresholdFilter extends FilteringAnalysis {
	private static final long serialVersionUID = 4346087654519386037L;
	private static Log log = LogFactory.getLog(ExpressionThresholdFilter.class);
	
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
        setDefaultPanel(new ExpressionThresholdFilterPanel());
    }

    /**
     * Check if the signal for the argument marker value respect the
     * range restrictions specified by the user.
     */
    @Override
    protected boolean isMissing(int arrayIndex, int markerIndex) {
        DSMicroarray mArray = maSet.get(arrayIndex);
    	DSMarkerValue mv = (DSMarkerValue) mArray.getMarkerValue(markerIndex);

    	if (mv == null)
            return false;
    	if (mv.isMissing()) // the case already marked
            return true;

        double signal = mv.getValue();
        if (rangeOption == INSIDE_RANGE)
            if ((signal >= minValue && signal <= maxValue))
                return true;
        if (rangeOption == OUTSIDE_RANGE)
            if ((signal < minValue || signal > maxValue))
                return true;
        return false;
    }

	@Override
	protected void getParametersFromPanel() {
		ExpressionThresholdFilterPanel parameterPanel = (ExpressionThresholdFilterPanel) aspp; 
        minValue = parameterPanel.getLowerBound();
        maxValue = parameterPanel.getUpperBound();
        rangeOption = parameterPanel.getRangeOption();

        FilterOptionPanel filterOptionPanel = parameterPanel.getFilterOptionPanel();
		if(filterOptionPanel.getSelectedOption()==FilterOptionPanel.Option.NUMBER_REMOVAL) {
	        criterionOption = CriterionOption.COUNT;	
	        numberThreshold = filterOptionPanel.getNumberThreshold();
		} else if(filterOptionPanel.getSelectedOption()==FilterOptionPanel.Option.PERCENT_REMOVAL) {
	        criterionOption = CriterionOption.PERCENT;	       
	        percentThreshold = filterOptionPanel.getPercentThreshold();
		} else {
	        log.error("Invalid filtering option");
		}

       
       
	}

}
