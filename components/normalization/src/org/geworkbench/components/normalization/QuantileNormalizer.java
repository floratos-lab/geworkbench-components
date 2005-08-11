package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.engine.model.analysis.NormalizingAnalysis;
import java.util.Comparator;
import java.util.Arrays;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University</p>
 * @author non attributable
 * @version 1.0
 */

/**
 * Applies the quantile multi-array normalization which induces the same distribution
 * of values across all microarrays within a microarray set. In this implementation,
 * normalization is applied at the probeset level data (rathern than the individual
 * probes).
 *
 * Given a microarray set with N arrays and M markers, the normalizations works
 * by building an NxM matrix, with one row for each array and one column for
 * each marker. First each row is sorted. In the resulting array, each value
 * across a column is replaced by the column average. Finally, the values within each
 * row are returned to their original order (before the sorting), thus
 * reinstatating the correspondence of each matrix row to a single marker.
 *
 * Details of the method can be found in a short paper by Ben Bolstad:
 *    "Probe Level Quantile Normalization of High Density Oligonucleotide Array Data"
 *
 * Although the description is given in the context of probe-level normalization,
 * the exact same approach can be used ar the probeset level.
 */
public class QuantileNormalizer extends AbstractAnalysis implements NormalizingAnalysis {
    int analysisType;
    int arrayCount;
    int markerCount;

    public QuantileNormalizer() {
        analysisType = AbstractAnalysis.LOG_TRANSFORMATION_NORMALIZER_TYPE;
        setLabel("Quantile Normalization");
    }

    public int getAnalysisType() {
        return analysisType;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return null;
        assert input instanceof DSMicroarraySet;

        arrayCount  = ((DSMicroarraySet) input).size();
        markerCount = ((DSMicroarraySet) input).getMarkers().size();
        DSMutableMarkerValue [][] arrays = new DSMutableMarkerValue[arrayCount][markerCount];
        for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex){
            DSMutableMarkerValue[] anArray = ((DSMicroarray)((DSMicroarraySet) input).
                                              get(arrayIndex)).getMarkerValues();
            for (int markerIndex = 0; markerIndex < markerCount; ++markerIndex)
                arrays[arrayIndex][markerIndex] = anArray[markerIndex];

            Arrays.sort(arrays[arrayIndex], new MarkerValueComparator());
        }

        for (int markerIndex = 0; markerIndex < markerCount; ++markerIndex){
            double mean = getMeanValue(arrays, markerIndex);
            for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex)
                arrays[arrayIndex][markerIndex].setValue(mean);
        }

        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * Return the mean value of the <code>colIndex</code>-th column of the
     * <code>arrays[][]</code> matrix.
     *
     * @param arrays DSMutableMarkerValue[][]
     * @param colIndex int
     * @return double
     */
    private double getMeanValue(DSMutableMarkerValue [][] arrays, int colIndex){
        double   sumOfValues = 0.0;
        int      nonMissing  = 0;

        // Computation excludes missing values
        for (int arrayIndex = 0; arrayIndex < arrayCount; arrayIndex++)
            if (!arrays[arrayIndex][colIndex].isMissing()){
                ++nonMissing;
                sumOfValues += arrays[arrayIndex][colIndex].getValue();
            }

        return (nonMissing != 0 ? sumOfValues / nonMissing : 0);
    }


    static class MarkerValueComparator<T extends DSMutableMarkerValue> implements Comparator<T>{
        public int compare(T v1, T v2){
            if (v1 != null && v2!= null){
                if (v1.getValue() == v2.getValue())
                    return 0;
                else if (v1.getValue() < v2.getValue())
                    return -1;
                else if (v1.getValue() > v2.getValue())
                    return 1;
            }
            return 0;
        }


    }
}

