package org.geworkbench.components.filtering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSGenepixMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version $Id$
 */

/**
 * Implementation of an expression threshold filter for 2 channel data. The
 * filter will set to "Missing" all markers whose measurement levels are
 * outside or inside a user defined range. The "outside"/"inside" option
 * is also a user defined.
 */
public class GenepixExpressionThresholdFilter extends FilteringAnalysis {
	private static final long serialVersionUID = -7467339732013411000L;
	private static Log log = LogFactory.getLog(GenepixExpressionThresholdFilter.class);

	// Static fields used to designate the user option available from the
    // combo box within the normalizer's parameters panel.
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

    public GenepixExpressionThresholdFilter() {
        setDefaultPanel(new GenepixExpressionThresholdFilterPanel());
    }

    @Override
	protected boolean expectedType() {
        // assuming the first marker has the same type as all other markers
        DSMicroarray mArray = maSet.get(0);
        CSMarkerValue mv = (CSMarkerValue) mArray.getMarkerValue(0);
        return (mv instanceof DSGenepixMarkerValue);
	}
    
	protected String expectedTypeName = "Genepix";

    /**
     * Check if the 2 channel signals for the argument marker value respect the
     * range restrictions specified by the user.
     *
     */
    @Override
    protected boolean isMissing(int arrayIndex, int markerIndex) {
        DSMicroarray mArray = maSet.get(arrayIndex);
        DSGenepixMarkerValue mv = (DSGenepixMarkerValue) mArray.getMarkerValue(markerIndex);

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

	@Override
	protected void getParametersFromPanel() {
		GenepixExpressionThresholdFilterPanel parameterPanel = (GenepixExpressionThresholdFilterPanel) aspp;
        cy3Min = parameterPanel.getCy3Min();
        cy3Max = parameterPanel.getCy3Max();
        cy5Min = parameterPanel.getCy5Min();
        cy5Max = parameterPanel.getCy5Max();
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
