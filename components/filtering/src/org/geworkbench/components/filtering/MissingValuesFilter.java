package org.geworkbench.components.filtering;

import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version $Id$
 */

/**
 * Modifies a microarray set by discarding all markers whose value is missing in
 * more than a user-specified number of microarrays.
 */
public class MissingValuesFilter extends FilteringAnalysis {
	private static final long serialVersionUID = -7753521003532896836L;
	
    public MissingValuesFilter() {
        setDefaultPanel(new MissingValuesFilterPanel());
    }

    @Override
    protected void getParametersFromPanel() {
        numberThreshold = ((MissingValuesFilterPanel) aspp).getMaxMissingArrays();
        criterionOption = CriterionOption.COUNT;
    }
    
    protected boolean isMissing(int arrayIndex, int markerIndex) {
		DSMarkerValue markerValue = maSet.get(arrayIndex).getMarkerValue(
				markerIndex);
		return markerValue.isMissing();
	}
}