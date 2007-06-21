package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.FilteringAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.engine.management.Script;
import org.geworkbench.engine.management.Documentation;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.FilteringEvent;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust Inc.
 * @version 1.0
 */

/**
 * Modifies a microarray set by discarding all markers whose value is missing in
 * more than a user-specified number of microarrays.
 */
public class MissingValuesFilter extends AbstractAnalysis implements FilteringAnalysis {
    int maxMissingMicroarrays;

    public MissingValuesFilter() {
        setLabel("Missing values filter");
        setDefaultPanel(new MissingValuesFilterPanel());
    }

    public int getAnalysisType() {
        return MISSING_VALUES_FILTER_TYPE;
    }

    @Script
    public void filter(Object input, int maxMissingMicoarraysValue) {
        ((MissingValuesFilterPanel) aspp).settMaxMissingArrays(maxMissingMicoarraysValue);
        execute(input);
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null)
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) input;
        // Collect the parameters needed for the execution of the filter
        maxMissingMicroarrays = ((MissingValuesFilterPanel) aspp).getMaxMissingArrays();
        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        // Identify the markers that do not meet the cutoff value.
        List<Integer> removeList = new ArrayList<Integer>();
        for (int i = 0; i < markerCount; i++) {
            if (isMissing(maSet, i, maxMissingMicroarrays, true)) {
                removeList.add(i);
            }
        }
        int removeCount = removeList.size();
        int finalCount = markerCount - removeCount;
        DSItemList<DSGeneMarker> markers = maSet.getMarkers();
        for (int i = 0; i < removeCount; i++) {
            // Account for already-removed markers
            int index = removeList.get(i) - i;
            // Remove the marker
            markers.remove(markers.get(index));
        }
        // Resize each microarray
        for (DSMicroarray microarray : maSet) {
            DSMarkerValue[] newValues = new DSMarkerValue[finalCount];
            int index = 0;
            for (int i = 0; i < markerCount; i++) {
                if (!removeList.contains(i)) {
                    newValues[index] = microarray.getMarkerValue(i);
                    index++;
                }
            }
            microarray.resize(finalCount);
            for (int i = 0; i < finalCount; i++) {
                microarray.setMarkerValue(i, newValues[i]);
            }
        }
        /*
        Vector markersToPrune = new Vector();
        for (int i = 0; i < markerCount; i++)
            if (isMissing(maSet, i, maxMissingMicroarrays))
                markersToPrune.add(maSet.getMarkers().get(i));
        // Remove all identified markers.
        for (int i = 0; i < markersToPrune.size(); i++) {
            maSet.getMarkers().remove((DSGeneMarker) markersToPrune.get(i));
        }
         */
        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * Check if the <code>index</code>-th marker in the microarrays set
     * <code>maSet</code> is missing in more than <code>maxAllowed</code>
     * microarrays.
     *
     * @param maSet      The reference microarray set.
     * @param index      The index of a marker(or Microarray based on rowMajor paramater).
     * @param maxAllowed The cutoff number of microarrays (or Markers).
     * @param rowMajor   <code>true</code> for marker based indexing(see index parameter) and <code>false</code>
     *                   for Microarray based indexing
     * @return <code>true</code> if the designated marker (or Microarray) is missing in more than
     *         <code>maxAllowed</code> microarrays (or Markers). <code>false</code> otherwise.
     */
    private boolean isMissing(DSMicroarraySet<DSMicroarray> maSet, int index, int maxAllowed, boolean rowMajor) {
        if (rowMajor) {
            int arrayCount = maSet.size();
            DSMarkerValue markerValue = null;
            int numMissing = 0;
            for (int i = 0; i < arrayCount; i++) {
                markerValue = maSet.get(i).getMarkerValue(index);
                if (markerValue.isMissing())
                    ++numMissing;
            }
            if (numMissing > maxAllowed)
                return true;
        } else {
            int markerCount = maSet.getMarkers().size();
            DSMutableMarkerValue markerValue = null;
            int numMissing = 0;
            for (int i = 0; i < markerCount; i++) {
                markerValue = maSet.get(index).getMarkerValue(i);
                if (markerValue.isMissing())
                    ++numMissing;
            }
            if (numMissing > maxAllowed)
                return true;
        }
        return false;
    }

    public String getType() {
        return "Missing Values Filter";
    }

    /**
     * Method removes either Markers or Microarrays from a dataset.
     *
     * @param data      the input Microarray Set
     * @param maxMissing The threshold number of microarrays (or Markers) that should have missing
     *                   values for a particular marker (or microarray) to be removed
     * @param rowMajor   Specifies if removal is based on Markers or Microarrays. A
     *                   <code>true</code> would remove markers across all arrays. A
     *                   <code>false</code> value removes microarrays from the dataset.
     */
    @Documentation("<html><BODY BGCOLOR=\"white\"><A NAME=\"filter(, int, boolean)\">" +
            "<!-- --></A><br><H3>filter</H3><br><PRE><DD>Method removes either " +
            "Markers or Microarrays from a dataset.<P><DD><DL><DT><B>Parameters:" +
            "</B><DD><B><CODE>maSet</CODE></B> - the input Microarray Set<DD>" +
            "<B><CODE>maxMissing</CODE></B> - The threshold number of microarrays (or Markers) " +
            "that <br>should have missing values for a particular marker (or microarray) to be removed.<DD>" +
            "<B><CODE>rowMajor</CODE></B> - Specifies if removal is based on Markers or Microarrays." +
            " A <code>true</code> would remove markers across all arrays.<br> A <code>false</code> " +
            "value removes microarrays from the dataset.</DL></DD></DL></BODY></html>")
    @Script
    public void filter(DSDataSet data, int maxMissing, boolean rowMajor) {
        DSMicroarraySet<DSMicroarray> maSet = null;
        if (data instanceof DSMicroarraySet)
            maSet = (DSMicroarraySet<DSMicroarray>) data;
        else
            return;
        if (rowMajor) {
            int arrayCount = maSet.size();
            int markerCount = maSet.getMarkers().size();
            List<Integer> removeList = new ArrayList<Integer>();
            for (int i = 0; i < markerCount; i++) {
                if (isMissing(maSet, i, maxMissing, true)) {
                    removeList.add(i);
                }
            }
            int removeCount = removeList.size();
            int finalCount = markerCount - removeCount;
            DSItemList<DSGeneMarker> markers = maSet.getMarkers();
            for (int i = 0; i < removeCount; i++) {
                int index = removeList.get(i) - i;
                markers.remove(markers.get(index));
            }
            for (DSMicroarray microarray : maSet) {
                DSMarkerValue[] newValues = new DSMarkerValue[finalCount];
                int index = 0;
                for (int i = 0; i < markerCount; i++) {
                    if (!removeList.contains(i)) {
                        newValues[index] = microarray.getMarkerValue(i);
                        index++;
                    }
                }
                microarray.resize(finalCount);
                for (int i = 0; i < finalCount; i++) {
                    microarray.setMarkerValue(i, newValues[i]);
                }
            }
        } else {
            int arrayCount = maSet.size();
            int markerCount = maSet.getMarkers().size();
            List<Integer> removeList = new ArrayList<Integer>();
            for (int i = 0; i < arrayCount; i++) {
                if (isMissing(maSet, i, maxMissing, false)) {
                    removeList.add(i);
                }
            }
            int removeCount = removeList.size();
            int finalCount = arrayCount - removeCount;
            for (int i = 0; i < removeCount; i++) {
                int index = removeList.get(i) - i;
                maSet.remove(index);
            }
            publishFilteringEvent(new FilteringEvent(maSet, maSet, getLabel() + "@Script filter: maxMissing = " + maxMissing + ", rowMajor = " + rowMajor));
        }
    }

    /**
     * Method to filter dataset so that it contains a maximum specified number of arrays.
     * A shuffle <code>java.util.Collections.shuffle()</code> is performed before removing arrays having index larger than the size needed.
     * If the dataset contains less than the specified number of arrays, the method terminates
     *
     * @param data the input dataset to be filtered
     * @param size the maximum size to which the dataset needs to be pruned
     */
    @Documentation("<html><BODY BGCOLOR=\"white\"><A NAME=\"prune(DSDataSet, int)\"><!-- --></A><br><H3>prune</H3><br>" +
            "<PRE><DD>Method to filter dataset so that it contains a maximum specified number of arrays.<br>" +
            "A shuffle <code>java.util.Collections.shuffle()</code> is performed before removing <br>arrays having index larger than the size needed.<br>" +
            "If the dataset contains less than the specified number of arrays, the method terminates<P>" +
            "<DD><DL><DT><B>Parameters:</B><DD><CODE>data</CODE> - the input dataset to be filtered<DD><CODE>size</CODE>" +
            " - the maximum size to which the dataset needs to be pruned</DL></DD></DL></BODY></html>")
    @Script
    public void prune(DSDataSet data, int size) {
        DSMicroarraySet<DSMicroarray> maSet = null;
        if (data instanceof DSMicroarraySet)
            maSet = (DSMicroarraySet<DSMicroarray>) data;
        else
            return;
        if (maSet.size() > size) {
            Collections.shuffle(maSet);
            int count = 0;
            for (DSMicroarray array : maSet) {
                array.setSerial(count++);
            }
            int initialSize = maSet.size();
            for (int i = initialSize - 1; i >= size; i--) {
                maSet.remove(i);
            }
            publishFilteringEvent(new FilteringEvent(maSet, maSet, getLabel() + "@Script prune: size = " + size));
        }
    }

    @Publish
    public FilteringEvent publishFilteringEvent(FilteringEvent event) {
        return event;
    }
}