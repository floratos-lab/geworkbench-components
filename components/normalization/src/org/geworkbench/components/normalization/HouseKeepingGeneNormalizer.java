package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.CSMarkerVector;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSGenepixMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Subscribe;

import javax.swing.*;
import java.util.TreeSet;
import java.util.Vector;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University.</p>
 * @author Xiaoqing Zhang
 * @version 3.0
 */

/**
 * Normalization the expression values based on the assumption that the expression level of housekeeping genes
 * are constant in different experiments.
 */



public class HouseKeepingGeneNormalizer extends AbstractAnalysis implements
        NormalizingAnalysis {

    // normalizer's parameters panel.

    public static final int BASEARRAY = 0;
    public static StringBuffer errorMessage = new StringBuffer();
    private DSPanel<DSGeneMarker> markerPanel;
    private TreeSet<String> nonFoundGenes = new TreeSet<String>();
    private boolean[] ExistedMarkers;
    private boolean haveNonExistMarker = false;
    double[] ch1fArray = null;
    double[] ch1bArray = null;
    double[] ch2fArray = null;
    double[] ch2bArray = null;
    boolean ignoreMissingValues = true;
    DSMicroarraySet refMASet;
    HouseKeepingGeneNormalizerPanel houseKeepingGeneNormalizerPanel = new HouseKeepingGeneNormalizerPanel();

    public HouseKeepingGeneNormalizer() {
        setDefaultPanel(houseKeepingGeneNormalizerPanel);
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public int getAnalysisType() {
        return HOUSEKEEPINGGENES_VALUE_NORMALIZER_TYPE;
    }

    public AlgorithmExecutionResults execute(Object input) {
        if (input == null) {
            return new AlgorithmExecutionResults(false, "Invalid input.", null);
        }
        assert input instanceof DSMicroarraySet;
        DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet) input;
        errorMessage = new StringBuffer();
        haveNonExistMarker = false;
        // Collect the parameters needed for the execution of the normalizer
        markerPanel = ((HouseKeepingGeneNormalizerPanel) aspp).getPanel();
        ignoreMissingValues = ((HouseKeepingGeneNormalizerPanel) aspp).isMissingValueIgnored();
        int houseKeepgeneNumber = markerPanel.size();
        ExistedMarkers = new boolean[houseKeepgeneNumber];
        double[] ratioArray = null;

        if (maSet instanceof CSMicroarraySet &&
                maSet.getCompatibilityLabel().equals("Genepix")) {
            ratioArray = getRatioForGenepix(maSet, markerPanel);
        } else {
            //ratioArray = getRatioArrary(maSet, markerPanel, BASEARRAY);
            ratioArray = getHouseKeepingGenesValue(maSet, markerPanel);
        }
        ((HouseKeepingGeneNormalizerPanel) aspp).setHighlightedMarkers(
                nonFoundGenes);
        if (haveNonExistMarker) {

            int choice = JOptionPane.showConfirmDialog(null,
                    "Some of the designated genes are not in the dataset or have missing values. Proceed?  ",
                    "Warning",
                    JOptionPane.OK_CANCEL_OPTION);

            if (choice == 2) {
                return new AlgorithmExecutionResults(false, "Cancelled by user.", input);
            }
        }


        int arrayCount = maSet.size();
        int markerCount = maSet.getMarkers().size();
        DSMicroarray microarray = null;
        DSMutableMarkerValue markerValue = null;

        for (int i = 0; i < arrayCount; i++) {
            if (Double.isNaN(ratioArray[i])) {
                continue;
            }
            for (int j = 0; j < markerCount; j++) {
                microarray = maSet.get(i);
                markerValue = (DSMutableMarkerValue) microarray.getMarkerValue(
                        j);
                if (!markerValue.isMissing()) {
                    if (markerValue instanceof CSGenepixMarkerValue) {

                        adjustMarkerValue((CSGenepixMarkerValue) markerValue, i);
                    } else {
                        if (ratioArray[i] != 0) {
                            markerValue.setValue(markerValue.getValue() /
                                    ratioArray[i]);
                        }

                    }

                }
            }

        }
       
        ProjectPanel.addHistoryDetail(maSet,((HouseKeepingGeneNormalizerPanel) aspp).getParamDetail());
        
        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * adjustForHousekeepingGenes
     *
     * @param markerValue DSMutableMarkerValue
     */
    private void adjustMarkerValue(CSGenepixMarkerValue markerValue,
                                   int arrayLocation) {
        boolean recompute = false;
        if (ch1bArray[arrayLocation] != 0) {
            markerValue.setCh1Bg(markerValue.getCh1Bg() /
                    ch1bArray[arrayLocation]);
            recompute = true;
        }
        if (ch2bArray[arrayLocation] != 0) {
            markerValue.setCh2Bg(markerValue.getCh2Bg() /
                    ch2bArray[arrayLocation]);
            recompute = true;
        }
        if (ch1fArray[arrayLocation] != 0) {
            markerValue.setCh1Fg(markerValue.getCh1Fg() /
                    ch1fArray[arrayLocation]);
            recompute = true;
        }
        if (ch2fArray[arrayLocation] != 0) {
            markerValue.setCh2Fg(markerValue.getCh2Fg() /
                    ch2fArray[arrayLocation]);
            recompute = true;
        }
        if (recompute) {
            markerValue.computeSignal();
        }

    }

    /**
     * getRatioArrary, caculate the adjust factor for aff_type data.
     *
     * @param maSet       DSMicroarraySet
     * @param markerPanel DSPanel
     * @param baseArray   int
     * @return double[]
     */
    public double[] getRatioArrary(DSMicroarraySet maSet, DSPanel markerPanel,
                                   int baseArray) {
        int arrayCount = maSet.size();
        double[] rawValues = getHouseKeepingGenesValue(maSet, markerPanel);
        double[] ratioArray = new double[arrayCount];
        for (int i = 0; i < arrayCount; i++) {
            ratioArray[i] = rawValues[BASEARRAY] / rawValues[i];
        }
        return ratioArray;
    }


    /**
     * sum the housekeeping genes expression values together.
     *
     * @param maSet       DSMicroarraySet
     * @param markerPanel DSPanel
     * @return double[]
     */

    public double[] getHouseKeepingGenesValue(DSMicroarraySet maSet,
                                              DSPanel markerPanel) {
        int markerCount = markerPanel.size();

        CSMarkerVector csMarkerVector = ((CSMicroarraySet) maSet).
                getMarkerVector();

        DSGeneMarker dsgmarker = csMarkerVector.get(0);
        //DSGeneMarker dsgmarker = (CSGeneMarker) markerPanel.get(0);
        if (csMarkerVector.contains(dsgmarker)) {
            Vector<DSGeneMarker>
                    matchedMarkersVector = csMarkerVector.
                    getMatchingMarkers(
                            dsgmarker);
            // System.out.println("Something wrong" + matchedMarkersVector);
            if (matchedMarkersVector == null ||
                    matchedMarkersVector.size() == 0) {
                matchedMarkersVector = csMarkerVector.
                        getMatchingMarkers(
                                dsgmarker.getLabel());
            }

        }

        DSMicroarray dsm = (DSMicroarray) maSet.get(0);

//        csMarkerVector.correctMaps();

        int arrayCount = maSet.size();
        DSMutableMarkerValue markerValue = null;
        double[][] arrays = new double[markerCount][arrayCount];
        double ratio[] = new double[arrayCount];

        //Because of a bug in CSMicroarray, getMarkerValue(DSGeneMarker mInfo)
        //does not return correct value, it always return the first marker when the CSGeneMarker
        // does not have a serial number >0. so instead, CSMicroarraySet.getRow(DSGeneMakrer)
        //is used here.

        for (int j = 0; j < markerCount; j++) {
            ExistedMarkers[j] = true;
            CSGeneMarker csgMarker = (CSGeneMarker) markerPanel.get(j);

            Vector<DSGeneMarker>
                    matchedMarkersVector = csMarkerVector.
                    getMatchingMarkers(
                            csgMarker.getLabel());
            if (matchedMarkersVector != null && matchedMarkersVector.size() > 0) {
                csgMarker = (CSGeneMarker) matchedMarkersVector.get(0);
            }

            for (int k = 0; k < arrayCount; k++) {
                arrays[j][k] = maSet.getValue(csgMarker, k);
                // DSMutableMarkerValue dsMutableMarkerValue = maSet.getValuesForName()
                if (Double.isNaN(arrays[j][k]) || arrays[j][k] == 0) {
                    if (ignoreMissingValues) {
                        errorMessage.append(csgMarker.getLabel() + " ");
                        nonFoundGenes.add(csgMarker.getLabel());
                        ExistedMarkers[j] = false;
                        haveNonExistMarker = true;
                        break;
                    } else {
                        ExistedMarkers[j] = updateMissingValue(csgMarker, maSet);
                        arrays[j][k] = maSet.getValue(csgMarker, k);
                        if (!ExistedMarkers[j]) {
                            errorMessage.append(csgMarker.getLabel() + " ");
                            nonFoundGenes.add(csgMarker.getLabel());
                            haveNonExistMarker = true;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < arrayCount; i++) {
            ratio[i] = 0d;
            int existedHousegenesNum = 0;
            for (int j = 0; j < markerCount; j++) {
                if (ExistedMarkers[j]) {
                    existedHousegenesNum++;
                    ratio[i] += arrays[j][i];
                }
            }
            //For bug 334 use avg not sum of housekeeping genes
            if (existedHousegenesNum > 0) {
                ratio[i] /= existedHousegenesNum;
            }
        }
        return ratio;
    }

    /**
     * Update the missing value of the housekeeping genes to mean value for non-genpix platforms.
     *
     * @param csgMarker
     * @param maSet
     * @return
     */
    public boolean updateMissingValue(CSGeneMarker csgMarker, DSMicroarraySet maSet) {
        int arrayCount = maSet.size();
        DSMutableMarkerValue markerValue = null;
        int existingValues = 0;
        double meanValue = 0;

        for (int k = 0; k < arrayCount; k++) {
            DSMicroarray microarray = (DSMicroarray) (maSet.get(k));
//            markerValue = (DSMutableMarkerValue) microarray.getMarkerValue(
//                    csgMarker);
            double newValue = maSet.getValue(csgMarker, k);
            if (!Double.isNaN(newValue)) {
                meanValue += newValue;
                existingValues++;
            }
        }
        if (existingValues == 0) {
            return false;
        } else {
            meanValue = meanValue / existingValues;
            for (int k = 0; k < arrayCount; k++) {
                DSMicroarray microarray = (DSMicroarray) (maSet.get(k));
                markerValue = (DSMutableMarkerValue) microarray.getMarkerValue(
                        csgMarker);
                if (markerValue.isMissing()) {
                    markerValue.setMissing(false);
                    markerValue.setValue(meanValue);
                }
            }
        }

        return true;
    }

    /**
     * Update the missing value of the housekeeping genes to mean value for genpix platforms.
     *
     * @param csgMarkerStr
     * @param maSet
     * @return
     */
    public boolean updateMissingValue(String csgMarkerStr, DSMicroarraySet maSet) {
        CSMarkerVector csMarkerVector = ((CSMicroarraySet) maSet).
                getMarkerVector();
        int arrayCount = maSet.size();
        DSMutableMarkerValue markerValue = null;
        int existingValues = 0;
        double meanValue = 0;
        double theCh1f = 0;
        double theCh1b = 0;
        double theCh2f = 0;
        double theCh2b = 0;
        String csgMarkerString = ((CSGeneMarker) markerPanel.get(csgMarkerStr)).
                getLabel();
        Vector<DSGeneMarker>
                marchedMarkersVector = csMarkerVector.
                getMatchingMarkers(
                        csgMarkerString);

        for (int i = 0; i < arrayCount; i++) {
            DSMicroarray microarray = (DSMicroarray) maSet.get(i);
            if (marchedMarkersVector != null && marchedMarkersVector.size() > 0) {
                for (DSGeneMarker dsgMarker : marchedMarkersVector) {
                    CSGenepixMarkerValue csgMarkerValue = (
                            CSGenepixMarkerValue)
                            microarray.getMarkerValue(
                                    dsgMarker);

                    if (csgMarkerValue != null && !csgMarkerValue.isMissing()) {
                        existingValues++;
                        theCh1f += csgMarkerValue.getCh1Fg();
                        theCh2f += csgMarkerValue.getCh2Fg();
                        theCh1b += csgMarkerValue.getCh1Bg();
                        theCh2b += csgMarkerValue.getCh2Bg();
                    }

                }

            }

        }
        if (existingValues == 0) {
            return false;
        } else {
            theCh1f /= existingValues;
            theCh2f /= existingValues;
            theCh1b /= existingValues;
            theCh2b /= existingValues;

            for (int k = 0; k < arrayCount; k++) {
                DSMicroarray microarray = (DSMicroarray) (maSet.get(k));

                if (marchedMarkersVector != null && marchedMarkersVector.size() > 0) {
                    for (DSGeneMarker dsgMarker : marchedMarkersVector) {
                        CSGenepixMarkerValue csgMarkerValue = (
                                CSGenepixMarkerValue)
                                microarray.getMarkerValue(
                                        dsgMarker);
                        if (csgMarkerValue.isMissing()) {
                            csgMarkerValue.setMissing(false);
                            csgMarkerValue.setCh1Bg(theCh1b);
                            csgMarkerValue.setCh1Fg(theCh1f);
                            csgMarkerValue.setCh2Bg(theCh2b);
                            csgMarkerValue.setCh2Fg(theCh2f);
                        }
                    }
                }

            }
        }

        return true;
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe
    @SuppressWarnings("unchecked")
    public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

        //log.debug("Source object " + source);

        if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {

        } else {
            DSDataSet dataSet = e.getDataSet();

            if (dataSet instanceof DSMicroarraySet) {
                if (refMASet == null) {
                    refMASet = (DSMicroarraySet) dataSet;
                } else if (refMASet != dataSet) {
                    houseKeepingGeneNormalizerPanel.clearAllHightlightsPressed();
                }

            }
        }
    }


    /**
     * getRatioForGenepix
     *
     * @param maSet       DSMicroarraySet
     * @param markerPanel DSPanel
     * @return double[]
     */
    //There is a bug related to CSMarkerVector,  similar to the bug listed above.
    //geneNameMap and geneIDMap in CSMarkerVector always are empty (size =1 or 0)
    //add a new correctMaps() method in CSMarkerVector to temp fix the probelm.
    public double[] getRatioForGenepix(DSMicroarraySet maSet,
                                       DSPanel markerPanel) {

        CSMarkerVector csMarkerVector = ((CSMicroarraySet) maSet).
                getMarkerVector();
        csMarkerVector.correctMaps();
        int markerCount = markerPanel.size();
        int arrayCount = maSet.size();
        double ratio[] = new double[arrayCount];

        ch1fArray = new double[arrayCount];
        ch1bArray = new double[arrayCount];
        ch2fArray = new double[arrayCount];
        ch2bArray = new double[arrayCount];

        for (int i = 0; i < arrayCount; i++) {
            ratio[i] = 1;
            int existedHousegenesNum = 0;
            ch1fArray[i] = 0;
            ch1bArray[i] = 0;
            ch2fArray[i] = 0;
            ch2bArray[i] = 0;
            DSMicroarray microarray = (DSMicroarray) maSet.get(i);
            for (int j = 0; j < markerCount; j++) {
                //for the bug list above.
                ExistedMarkers[j] = true;
                String csgMarkerString = ((CSGeneMarker) markerPanel.get(j)).
                        getLabel();
                Vector<DSGeneMarker>
                        marchedMarkersVector = csMarkerVector.
                        getMatchingMarkers(
                                csgMarkerString);

                if (marchedMarkersVector != null && marchedMarkersVector.size() > 0) {
                    for (DSGeneMarker dsgMarker : marchedMarkersVector) {
                        CSGenepixMarkerValue csgMarkerValue = (
                                CSGenepixMarkerValue)
                                microarray.getMarkerValue(
                                        dsgMarker);

                        if (csgMarkerValue != null && !csgMarkerValue.isMissing()) {
                            existedHousegenesNum++;
                            ch1fArray[i] += csgMarkerValue.getCh1Fg();
                            ch2fArray[i] += csgMarkerValue.getCh2Fg();
                            ch1bArray[i] += csgMarkerValue.getCh1Bg();
                            ch2bArray[i] += csgMarkerValue.getCh2Bg();
                        } else {
                            if (ignoreMissingValues) {
                                nonFoundGenes.add(dsgMarker.getLabel());
                                ExistedMarkers[j] = false;
                                haveNonExistMarker = true;
                            } else {
                                // updateMissingValue(csgMarkerValue,  maSet);
                                if (updateMissingValue(csgMarkerString, maSet)) {
                                    ch1fArray[i] += csgMarkerValue.getCh1Fg();
                                    ch2fArray[i] += csgMarkerValue.getCh2Fg();
                                    ch1bArray[i] += csgMarkerValue.getCh1Bg();
                                    ch2bArray[i] += csgMarkerValue.getCh2Bg();
                                } else {
                                    ExistedMarkers[j] = false;
                                }
                            }
                        }

                    }

                } else {

                    nonFoundGenes.add(csgMarkerString);
                    ExistedMarkers[j] = false;
                    haveNonExistMarker = true;

                }


            }
            //For bug 334 use ave not sum of housekeeping genes
            if (existedHousegenesNum > 0) {
                ch1fArray[i] /= existedHousegenesNum;
                ch2fArray[i] /= existedHousegenesNum;
                ch1bArray[i] /= existedHousegenesNum;
                ch1bArray[i] /= existedHousegenesNum;
            }

        }

        return ratio;
    }

    public String getType() {
        return "HouseKeepingGene Normalizer";
    }

    private void jbInit() throws Exception {
    }

}
