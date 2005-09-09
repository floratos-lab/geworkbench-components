package org.geworkbench.components.normalization;

import java.util.Vector;

import javax.swing.JOptionPane;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.CSMarkerVector;
import org.geworkbench.bison.datastructure.biocollections.microarrays.
        CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.
        DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.*;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.NormalizingAnalysis;
import java.util.ArrayList;
import java.util.TreeSet;

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


    public HouseKeepingGeneNormalizer() {
        setLabel("HouseKeeping Genes Normalizer");
        setDefaultPanel(new HouseKeepingGeneNormalizerPanel());
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

        if (haveNonExistMarker) {

            int choice = JOptionPane.showConfirmDialog(null,
                    "Some of the designated genes are not in the dataset. Proceed? \n Genes: " +
                    errorMessage.toString(), "Warning",
                    JOptionPane.OK_CANCEL_OPTION);
            ((HouseKeepingGeneNormalizerPanel) aspp).setHighlightedMarkers(
                    nonFoundGenes);
            if (choice == 2) {
                return new AlgorithmExecutionResults(true, "No errors", input);
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
        return new AlgorithmExecutionResults(true, "No errors", input);
    }

    /**
     * adjustForHousekeepingGenes
     *
     * @param markerValue DSMutableMarkerValue
     */
    private void adjustMarkerValue(CSGenepixMarkerValue markerValue,
                                   int arrayLocation) {
        if (ch1bArray[arrayLocation] != 0) {
            markerValue.setCh1Bg(markerValue.getCh1Bg() /
                                 ch1bArray[arrayLocation]);
        }
        if (ch2bArray[arrayLocation] != 0) {
            markerValue.setCh2Bg(markerValue.getCh2Bg() /
                                 ch2bArray[arrayLocation]);
        }
        if (ch1fArray[arrayLocation] != 0) {
            markerValue.setCh1Fg(markerValue.getCh1Fg() /
                                 ch1fArray[arrayLocation]);
        }
        if (ch2fArray[arrayLocation] != 0) {
            markerValue.setCh2Fg(markerValue.getCh2Fg() /
                                 ch2fArray[arrayLocation]);
        }

    }

    /**
     * getRatioArrary, caculate the adjust factor for aff_type data.
     *
     * @param maSet DSMicroarraySet
     * @param markerPanel DSPanel
     * @param baseArray int
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
     * @param maSet DSMicroarraySet
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
//                System.out.println("Something wrong" +
//                                   matchedMarkersVector.size() +
//                                   matchedMarkersVector.get(0));

            }
//            System.out.println("Something wrong" + matchedMarkersVector.size() +
//                               csMarkerVector.contains((CSGeneMarker)
//                    markerPanel.get(0)));

        }

        DSMicroarray dsm = (DSMicroarray) maSet.get(0);

//        csMarkerVector.correctMaps();

        double baseTotal = 0d;
        // DSMicroarray mArray = null;
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

                if (Double.isNaN(arrays[j][k])) {
                    errorMessage.append(csgMarker.getLabel() + " ");
                    nonFoundGenes.add(csgMarker.getLabel());
                    ExistedMarkers[j] = false;
                    haveNonExistMarker = true;
                    break;
                }
            }
        }

        for (int i = 0; i < arrayCount; i++) {
            ratio[i] = 0d;

            for (int j = 0; j < markerCount; j++) {
                if (ExistedMarkers[j]) {
                    ratio[i] += arrays[j][i];
                }
            }
        }
        return ratio;
    }

    /**
     * getRatioForGenepix
     *
     * @param maSet DSMicroarraySet
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
        for (DSGeneMarker dsgMarker : csMarkerVector) {
            System.out.println(dsgMarker.getGeneName() + "|" +
                               dsgMarker.getLabel());
        }

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
            boolean needNormalization = false;
            double newch1f = 0d;
            double newch2f = 0d;
            double newch1b = 0d;
            double newch2b = 0d;
            ch1fArray[i] = 0;
            ch1bArray[i] = 0;
            ch2fArray[i] = 0;
            ch2bArray[i] = 0;
            DSMicroarray microarray = (DSMicroarray) maSet.get(i);
            for (int j = 0; j < markerCount; j++) {
                //for the bug list above.
                String csgMarkerString = ((CSGeneMarker) markerPanel.get(j)).
                                         getLabel();
                Vector<DSGeneMarker>
                        marchedMarkersVector = csMarkerVector.
                                               getMatchingMarkers(
                        csgMarkerString);

                if (marchedMarkersVector != null) {
                    for (DSGeneMarker dsgMarker : marchedMarkersVector) {
                        CSGenepixMarkerValue csgMarkerValue = (
                                CSGenepixMarkerValue)
                                microarray.getMarkerValue(
                                        dsgMarker);

                        if (csgMarkerValue != null && !csgMarkerValue.isMissing()) {
                            needNormalization = true;
                            ch1fArray[i] += csgMarkerValue.getCh1Fg();
                            ch2fArray[i] += csgMarkerValue.getCh2Fg();
                            ch1bArray[i] += csgMarkerValue.getCh1Bg();
                            ch2bArray[i] += csgMarkerValue.getCh2Bg();
                        }

                    }

                }

            }
            if (needNormalization) {
                if (newch1f != newch1b) {
                    ratio[i] = (newch2f - newch2b) / (newch1f - newch1b);
                } else {
                    ratio[i] = newch2f - newch2b;
                }
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
