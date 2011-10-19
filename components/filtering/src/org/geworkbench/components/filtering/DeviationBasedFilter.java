package org.geworkbench.components.filtering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
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
 * Provides an implementation for a deviation based filter. In particular,
 * for every marker profile (i.e., for the vector defined by the
 * marker values across all chips in the experiment) it calculates
 * the vector's sample standard deviation (divides by N-1). 
 * If this deviation is less than the user provided
 * value, the corresponding marker is set to "Missing" across all arrays
 * in the dataset.
 * <p/>
 * The filter provides for the following ways to handle with missing values in
 * a marker profile:
 * <UL>
 * <LI>Marker average: replace the missing value with the average of the
 * (non-missing) profile values before proceeding with the deviation
 * calculation.</LI>
 * <LI>Microarray average: replace the missing value at microarray X with the
 * average of the (non-missing) marker values in X before proceeding with
 * the profile deviation calculation.</LI>
 * <LI>Ignore missing: do not take into account missing values in the profile
 * deviation calculation.</LI>
 * </UL>
 */
 
public class DeviationBasedFilter extends FilteringAnalysis {
	private static final long serialVersionUID = -3948591097666762748L;
	private static Log log = LogFactory.getLog(DeviationBasedFilter.class);
			
	// Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    static final int MARKER = 0;
    static final int MICROARRAY = 1;
    static final int IGNORE = 2;
    /**
     * User-specified value for deviation bound (comes from the
     * <code>DeviationBasedFilterPanel</code>).
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
    
    public DeviationBasedFilter() {
        setDefaultPanel(new DeviationBasedFilterPanel());
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
    private double getDeviation(double[] profile) {
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
        return Math.sqrt(deviation);
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
		DeviationBasedFilterPanel deviationBasedFilterPanel = (DeviationBasedFilterPanel)aspp;
        deviationBound = deviationBasedFilterPanel.getDeviationCutoff();
        missingValues = deviationBasedFilterPanel.getMissingValueTreatment();
        int arrayCount = maSet.size();

        // Go over all markers, compute their deviation, and decide which to filter.
        microarrayAverages = new double[arrayCount];
        computeMicroarrayAverages(maSet);
        
        // this filter is implemented a little different because missing always applies to the entire marker
        int markerCount = maSet.getMarkers().size();
        missingMarkers = new boolean[markerCount];
        for(int i=0; i<markerCount; i++) {
        	double[] profile = getProfile(maSet, i);
            double markerDeviation = getDeviation(profile);
            if (markerDeviation <= deviationBound) 
            	missingMarkers[i] = true;
            else
            	missingMarkers[i] = false;
        	
        }
        log.debug("pre-calcaultation done");
 
        // not used for this particular filter
//        numberThreshold = deviationBasedFilterPanel.getNumberThreshold();
//        percentThreshold = deviationBasedFilterPanel.getPercentThreshold();
        
        criterionOption = CriterionOption.COUNT;
        numberThreshold = 0;
      }

}