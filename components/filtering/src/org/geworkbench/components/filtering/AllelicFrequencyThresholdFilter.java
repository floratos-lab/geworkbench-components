/**
 * AllelicFrequencyThresholdFilter.java
 */

package org.geworkbench.components.filtering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;

/**
 * @author manjunath at genomecenter dot columbia dot edu
 */
public class AllelicFrequencyThresholdFilter extends AbstractAnalysis implements FilteringAnalysis {
    
    /** Creates a new instance of AllelicFrequencyThresholdFilter */
    public AllelicFrequencyThresholdFilter() {
        setLabel("Frequency threshold filter");
        setDefaultPanel(new AllelicFrequencyThresholdFilterPanel());
    }    

    public int getAnalysisType() {
        return AbstractAnalysis.ALLELIC_FREQUENCY_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> data = (DSMicroarraySet<DSMicroarray>)input;
        double lowerBound = ((AllelicFrequencyThresholdFilterPanel)aspp).getLowerFilterBound();
        ArrayList<DSGeneMarker> removedMarkers = new ArrayList<DSGeneMarker>();
        for (DSGeneMarker marker : data.getMarkers()) {
            double[] profile = data.getRow(marker);
            Arrays.sort(profile);
            Vector<Integer> counts = new Vector<Integer>();
            for (int i = 1, count = 1; i < profile.length; i++) {
                if (profile[i] == profile[i - 1]) {
                    count++;
                }
                else {
                    counts.add(count);
                    count = 1;
                }
                if (i == profile.length - 1)
                    counts.add(count);
            }
            int maxCount = Collections.max(counts);
            if ( (((double)maxCount) / profile.length) >= (lowerBound / 100d)) {
                removedMarkers.add(marker);
            }
        }
        int index = 0;
        for (DSGeneMarker marker : removedMarkers){
            int oldIndex = marker.getSerial();
            marker.setSerial(oldIndex - index);
            for (DSMicroarray microarray : data) {
                DSMarkerValue[] mValues = microarray.getMarkerValues();
                DSMarkerValue mv = microarray.getMarkerValue(marker);
                ArrayList<DSMarkerValue> mvList = new ArrayList<DSMarkerValue>(Arrays.asList(mValues));
                mvList.remove(mv);
                microarray.resize(mvList.size());
                for (int i = 0; i < mvList.size(); i++){
                    microarray.setMarkerValue(i, mvList.get(i));
                }
            }
            marker.setSerial(oldIndex);
            data.getMarkers().remove(marker);
            index++;
        }
        index = 0;
        for (DSGeneMarker m : data.getMarkers()){
            m.setSerial(index++);
        }
        return new AlgorithmExecutionResults(true, "", data);
    }
}