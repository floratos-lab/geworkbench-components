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
	
	private int maxMissingMicroarrays;

    public MissingValuesFilter() {
        setDefaultPanel(new MissingValuesFilterPanel());
    }

    @Override
    protected void getParametersFromPanel() {
        maxMissingMicroarrays = ((MissingValuesFilterPanel) aspp).getMaxMissingArrays();
        filterOption = FilterOption.REMOVAL;
    }
    
    /**
     * Check if the <code>index</code>-th marker in the microarrays set
     * <code>maSet</code> is missing in more than <code>maxAllowed</code>
     * microarrays.
     *
     * @param maSet      The reference microarray set.
     * @param index      The index of a marker(or Microarray based on rowMajor parameter).
     * @param maxAllowed The cutoff number of microarrays (or Markers).
     * @param rowMajor   <code>true</code> for marker based indexing(see index parameter) and <code>false</code>
     *                   for Microarray based indexing
     * @return <code>true</code> if the designated marker (or Microarray) is missing in more than
     *         <code>maxAllowed</code> microarrays (or Markers). <code>false</code> otherwise.
     */
    protected boolean isMissing(int arrayIndex, int markerIndex) {
    	// arrayIndex not used
            int arrayCount = maSet.size();
            DSMarkerValue markerValue = null;
            int numMissing = 0;
            for (int i = 0; i < arrayCount; i++) {
                markerValue = maSet.get(i).getMarkerValue(markerIndex);
                if (markerValue.isMissing())
                    ++numMissing;
            }
            if (numMissing > maxMissingMicroarrays)
                return true;

        return false;
    }

}