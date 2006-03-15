package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.engine.management.Script;
import org.geworkbench.engine.management.Documentation;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * Provides an implementation for a deviation based filter. In particularm
 * for every marker profile (i.e., for the vector defined by the
 * marker values across all chips in the experiment) it calculates
 * the vector's deviation. If this deviation is less than the user provided
 * value, the corresponding marker is set to "Missing" accros all arrays
 * in the dataset.
 * <p/>
 * The filter provides for the following ways to handle with missing values in
 * a marker profile:
 * <UL>
 * <LI>Marker average: replace the missing value with the average of the
 * (non-missing) profile values defore proceeding with the deviation
 * calculation.</LI>
 * <LI>Microarray average: replace the missing value at microarray X with the
 * average of the (non-missing) marker values in X defore proceeding with
 * the profile deviation calculation.</LI>
 * <LI>Ignore missing: do not take into account missing values in the profile
 * deviation calculation.</LI>
 * </UL>
 */
public class DeviationBasedFilter extends AbstractAnalysis implements FilteringAnalysis {
    // Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    public static final int MARKER = 0;
    public static final int MICROARRAY = 1;
    public static final int IGNORE = 2;
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
        setLabel("Deviation filter");
        setDefaultPanel(new DeviationBasedFilterPanel());
    }
    
    public int getAnalysisType() {
        return DEVIATION_BASED_FILTER_TYPE;
    }
    
    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) input;
        // Collect the parameters needed for the execution of the filter
        deviationBound = ((DeviationBasedFilterPanel) aspp).getDeviationCutoff();
        missingValues = ((DeviationBasedFilterPanel) aspp).getMissingValueTreatment();
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        double[] profile = null;
        // Go over all markers, compute their deviation, and decide which to filter.
        microarrayAverages = new double[arrayCount];
        computeMicroarrayAverages(maSet);
        for (int i = 0; i < markerCount; i++) {
            profile = getProfile(maSet, i);
            double markerDeviation = getDeviation(profile);
            if (markerDeviation <= deviationBound)
                for (int j = 0; j < arrayCount; ++j)
                    ((DSMutableMarkerValue) maSet.get(j).getMarkerValue(i)).setMissing(true);
        }
        
        return new AlgorithmExecutionResults(true, "No errors", input);
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
     * Compute the deviation for the numbers in <code>profile</code>.
     *
     * @param profile
     * @return
     */
    private double getDeviation(double[] profile) {
        if (profile == null)
            return 0.0;
        double meanValue = getMean(profile);
        double deviation = 0.0;
        for (int i = 0; i < profile.length; ++i)
            deviation += (profile[i] - meanValue) * (profile[i] - meanValue);
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
    double[] getProfile(DSMicroarraySet<DSMicroarray> maSet, int index) {
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
    private void computeMicroarrayAverages(DSMicroarraySet<DSMicroarray> maSet) {
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
    
    /**
     * Method sets either Markers or Microarrays for removal by setting their <code>status</code>
     * to be missing.
     * @param maSet the input Microarray Set
     * @param deviations Marker(Microarray) status is set missing based on values being a certains
     *        number of deviations away from the Marker(Microarray) mean.
     * @param inside Specifies that values inside the deviation range should be set missing. A
     *        <code>false</code> value would set values outside the deviation range as missing
     * @param rowMajor Specifies if deviations based on Marker means or Microarray means should be used. A
     *        <code>true</code> would compute means for markers across all arrays and set markers as missing
     *        in arrays as missing based on deviation from this mean. A <code>false</code> value implies
     *        microarray means being used.
     */
    
    @Documentation("<html><BODY BGCOLOR=\"white\"><A NAME=\"mask(, int, boolean, boolean)\"><!-- --></A><H3>mask</H3><PRE><DD>Method sets either " +
            "Markers or Microarrays for removal by setting their <code>status</code><br>to be missing. <P><DD>" +
            "<DL><DT><B>Parameters:</B><DD><B><CODE>maSet</CODE></B> - the input Microarray Set<DD><B><CODE>deviations</CODE></B>" +
            " - Marker(Microarray) status is set missing based on values being a certains <br>number of deviations " +
            "away from the Marker(Microarray) mean.<DD><B><CODE>inside</CODE></B> - Specifies that values inside the deviation " +
            "range should be set missing. A <br><code>false</code> value would set values outside the deviation range as " +
            "missing<DD><B><CODE>rowMajor</CODE></B> - Specifies if deviations based on Marker means or Microarray means should " +
            "be used. A <br><code>true</code> would compute means for markers across all arrays and set markers as missing " +
            "<br>in arrays as missing based on deviation from this mean. A <code>false</code> value implies <br>microarray " +
            "means being used.</DL><br><br></DD></DL></BODY></html>")
    @Script public void mask(DSDataSet data, int deviations, boolean inside, boolean rowMajor){
        DSMicroarraySet<DSMicroarray> maSet = null;
        if (data instanceof DSMicroarraySet)
            maSet = (DSMicroarraySet<DSMicroarray>)data;
        else
            return;
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        double[] profile = null;
        if (rowMajor){
            for (int i = 0; i < markerCount; i++) {
                profile = getProfile(maSet, i);
                double mean = getMean(profile);
                double markerDeviation = getDeviation(profile);
                for (int j = 0; j < arrayCount ; j++){
                    double value = maSet.get(j).getMarkerValue(i).getValue();
                    if (inside){
                        if ((value - mean) <= deviations * markerDeviation){
                            maSet.get(j).getMarkerValue(i).setMissing(true);
                        }
                    }
                    else if ((value - mean) > deviations * markerDeviation){
                            maSet.get(j).getMarkerValue(i).setMissing(true);
                    }
                }
            }
        } else {
            for (int i = 0; i < arrayCount; i++) {
                float[] rawData = maSet.get(i).getRawMarkerData();;
                profile = new double[rawData.length];
                int j = 0;
                for (float rd : rawData){
                    profile[j++] = (double)rd;
                }
                double mean = getMean(profile);
                double arrayDeviation = getDeviation(profile);
                for (int k = 0; k < markerCount; ++j){
                    double value = maSet.get(i).getMarkerValue(k).getValue();
                    if (inside){
                        if ((value - mean) <= deviations * arrayDeviation){
                            maSet.get(j).getMarkerValue(i).setMissing(true);
                        }
                    }
                    else if ((value - mean) > deviations * arrayDeviation){
                            maSet.get(j).getMarkerValue(i).setMissing(true);
                    }
                }
            }
        }
    }
}