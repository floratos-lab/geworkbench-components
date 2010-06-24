package org.geworkbench.components.filtering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSAffyMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;


/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version $Id$
 */

/**
 * Set to "missing" all measurements from an Affy array whose detection call is
 * among a user-specified subset of P (present), A (absent) or M (marginal).
 * The user preferences are collected in this filter's associate parameters
 * GUI (<code>AffyDetectionCallFilterPanel</code>).
 */
public class AffyDetectionCallFilter extends FilteringAnalysis {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6590700967249955520L;
	private static Log log = LogFactory.getLog(AffyDetectionCallFilter.class);
	
	protected boolean filterPresent = false;
    protected boolean filterAbsent = false;
    protected boolean filterMarginal = false;

    public AffyDetectionCallFilter() {
        setDefaultPanel(new AffyDetectionCallFilterPanel());
    }

    
    @Override
	protected boolean expectedType() {
        // assuming the first marker has the same type as all other markers
        DSMicroarray mArray = maSet.get(0);
        CSMarkerValue mv = (CSMarkerValue) mArray.getMarkerValue(0);
        return (mv instanceof DSAffyMarkerValue);
	}
    
	protected String expectedTypeName = "Affymetrix";

    @Override
    protected boolean isMissing(int arrayIndex, int markerIndex) {
        DSMicroarray mArray = maSet.get(arrayIndex);
    	CSMarkerValue mv = (CSMarkerValue) mArray.getMarkerValue(markerIndex);

    	if (mv == null || mv.isMissing()) {
            return false;
        }
        if (mv.isPresent() && filterPresent) {
            return true;
        }
        if (mv.isAbsent() && filterAbsent) {
            return true;
        }
        if (mv.isMarginal() && filterMarginal) {
            return true;
        }
        return false;
    }
    
	@Override
	protected void getParametersFromPanel() {
		AffyDetectionCallFilterPanel affyDetectionCallFilterPanel = (AffyDetectionCallFilterPanel) aspp; 
        filterPresent = affyDetectionCallFilterPanel.isPresentSelected();
        filterAbsent = affyDetectionCallFilterPanel.isAbsentSelected();
        filterMarginal = affyDetectionCallFilterPanel.isMarginalSelected();
        
        FilterOptionPanel filterOptionPanel = affyDetectionCallFilterPanel.getFilterOptionPanel();
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
