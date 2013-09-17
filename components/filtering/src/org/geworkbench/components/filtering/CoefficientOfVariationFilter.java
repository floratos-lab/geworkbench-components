package org.geworkbench.components.filtering;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;

/**
 * 
 * @author zji
 * @version $Id$
 */
public class CoefficientOfVariationFilter extends FilteringAnalysis{
	private static final long serialVersionUID = -5549903964783210746L;
	
	private static Log log = LogFactory.getLog(CoefficientOfVariationFilter.class);
			
	// Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    static final int MARKER = 0;
    static final int MICROARRAY = 1;
    static final int IGNORE = 2;
    
    static final double SMALLDOUBLE=0.00000000000001;
    /**
     * User-specified value for deviation bound (comes from the
     * <code>CoefficientVariationFilterPanel</code>).
     */
    protected double deviationBound = 0.0;
    /**
     * User-specified approach to follow for dealing with missing values.
     */
    protected int missingValues;
    /**
     * Stores the mean marker value calculations for the individual microarrays.
     */
    protected double[] microarrayAverages = null;
    
    public CoefficientOfVariationFilter() {
        setDefaultPanel(new CoefficientOfVariationFilterPanel());
    }
    
    private boolean[] missingMarkers;
    
    /**
     * This filter is different from other filters in that the marker are recognized as missing directly
     * instead of being based on count or percentage. So this method is in fact ignored.
     */
    @Override
    public boolean isMissing(int arrayIndex, int markerIndex) {
    	return true; // does not matter
    }
    
    /**
     * This filter is different from other filters in that the marker are recognized as missing directly
     * instead of being based on count or percentage. So this method just uses special value to control switch.
     * This way, we don't have to change remove() method, which is more complicated and used by other filters.
     */
    @Override
    protected int countMissing(int markerIndex) {
    	if(missingMarkers[markerIndex])return numberThreshold+1; // remove
    	else return numberThreshold-1; // not to remove
    }
    
    /**
     * Compute the mean value for the numbers in <code>profile</code>.
     *
     * @param profile
     * @return
     */
    private double getMean(double[] profile) {
        if (profile == null)
            return 0.0;
        
        double sum = 0.0d;
        for (int i = 0; i < profile.length; i++)
            sum += profile[i];
        if (profile.length > 0)
            sum /= profile.length;
        return (double) sum;
    }
    
    /**
     * Compute the sample standard deviation for the numbers in <code>profile</code>.
     *
     * @param profile
     * @return
     */
    private double getCV(double[] profile) {
        if (profile == null)
            return 0.0;
        double meanValue = getMean(profile);
        double deviation = 0.0;
		double diff = 0.0;
        for (int i = 0; i < profile.length; i++) {
			diff = (profile[i] - meanValue);
            deviation += diff*diff;
		}
        if (profile.length > 1)
            deviation /= (profile.length - 1);
        double returnValue=0.0;
        if(Math.abs(meanValue)>SMALLDOUBLE) returnValue=Math.sqrt(deviation)/meanValue;
        return returnValue;
    }
    
    /**
     * Obtain the profile values for the index-th marker within the array set.
     *
     * @param microarraySet The reference microarray set.
     * @param index         The index of the marker under consideration.
     * @return A <code>double[]</code> array containing the
     *         marker values across all microarrays.
     */
    double[] getProfile(DSMicroarraySet maSet, int index) {
        if (maSet == null || index < 0 || index >= maSet.getMarkers().size())
            return null;
        int arrayCount = maSet.size();
        // Compute the profile average (using non-missing values).
        double average = 0.0;
        int nonMissing = 0;
        for (int i = 0; i < arrayCount; i++) {
            DSMarkerValue mv = maSet.get(i).getMarkerValue(index);
            if (!mv.isMissing()) {
                average += mv.getValue();
                ++nonMissing;
            }
        }
        
        if (nonMissing > 0)
            average /= nonMissing;
        // Allocate the necessary space
        double[] profile = null;
        if (missingValues != IGNORE)
            profile = new double[arrayCount];
        else
            profile = new double[nonMissing];
        // Fill-in the data.
        for (int i = 0, j = 0; i < arrayCount; i++) {
            DSMicroarray microarray = maSet.get(i);
            DSMarkerValue mv = microarray.getMarkerValue(index);
            if (!mv.isMissing())
                profile[j++] = mv.getValue();
            else if (missingValues == MARKER)
                profile[j++] = average;
            else if (missingValues == MICROARRAY)
                profile[j++] = microarrayAverages[microarray.getSerial()];
        }
        
        return profile;
    }
    
    /**
     * Populates <code>microarrayAverages[j]</code> with the average marker
     * value in the j-th microarray (using only non-missing values).
     *
     * @param maSet The reference microarray set.
     */
    private void computeMicroarrayAverages(DSMicroarraySet maSet) {
        if (maSet == null)
            return;
        int markerCount = maSet.getMarkers().size();
        for (int j = 0; j < maSet.size(); ++j) {
            int nonMissing = 0;
            DSMicroarray mArray = maSet.get(j);
            double average = 0.0;
            // Compute the microarray average.
            for (int i = 0; i < markerCount; i++){
                if (!mArray.getMarkerValue(i).isMissing()) {
                    average += mArray.getMarkerValue(i).getValue();
                    ++nonMissing;
                }
            }
            if (nonMissing > 0)
                average /= nonMissing;
            microarrayAverages[j] = average;
        }
    }

	@Override
	protected void getParametersFromPanel() {
		CoefficientOfVariationFilterPanel coefficientVariationFilterPanel = (CoefficientOfVariationFilterPanel)aspp;
        deviationBound = coefficientVariationFilterPanel.getDeviationCutoff();
        missingValues = coefficientVariationFilterPanel.getMissingValueTreatment();
        int arrayCount = maSet.size();

        // Go over all markers, compute their deviation, and decide which to filter.
        microarrayAverages = new double[arrayCount];
        computeMicroarrayAverages(maSet);
        
        // this filter is implemented a little different because missing always applies to the entire marker
        int markerCount = maSet.getMarkers().size();
        missingMarkers = new boolean[markerCount];
        for(int i=0; i<markerCount; i++) {
        	double[] profile = getProfile(maSet, i);
            double markerDeviation = getCV(profile);
            if (markerDeviation <= deviationBound) 
            	missingMarkers[i] = true;
            else
            	missingMarkers[i] = false;
        	
        }
        log.debug("pre-calcaultation done");
 
        
        criterionOption = CriterionOption.COUNT;
        numberThreshold = 0;
      }

	@Override
	public List<Integer> getMarkersToBeRemoved(DSMicroarraySet input) {

		maSet = (DSMicroarraySet) input;
		
		int markerCount = maSet.getMarkers().size();
        int arrayCount = maSet.size();
       
        for(int i=0; i<markerCount; i++) {        	
            for (int j = 0; j < arrayCount; j++) {
                DSMarkerValue mv = maSet.get(j).getMarkerValue(i);                
               	double v = mv.getValue();
               	if(v<0){
               		JOptionPane.showMessageDialog(null, "This dataset contains negative value, so Coefficient of Variant filter does not apply.",
        					"Coefficient of Variation Error", JOptionPane.ERROR_MESSAGE);  		
               		return null; 
               		
               	} 
            }
           
        }

		getParametersFromPanel();
		

		// Identify the markers that do not meet the cutoff value.
		List<Integer> removeList = new ArrayList<Integer>();
		for (int i = 0; i < markerCount; i++) {
			if ((criterionOption == CriterionOption.COUNT && countMissing(i) > numberThreshold)
					|| (criterionOption == CriterionOption.PERCENT && 
							(double) countMissing(i) / arrayCount > percentThreshold)) {
				removeList.add(i);
			}
		}
		return removeList;
	}

}
