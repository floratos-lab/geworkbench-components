package org.geworkbench.components.interactions.cellularnetwork;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.markers.goterms.GOTerm;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Jan 5, 2007
 * Time: 12:31:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CellularNetWorkElementInformation {
    private int ppInteractionNum;
    private int pdInteractionNum;
    private boolean includePPInteraction;
    private boolean includePDInteraction;
    private DSGeneMarker dSGeneMarker;
    private String goInfoStr;
    private String geneType;
    private InteractionDetail[] interactionDetails;
    private static double threshold;
    private int[] distribution;
    private int[] ppDistribution;
    private int[] pdDistribution;
    private static double smallestIncrement;
    private static Double defaultSmallestIncrement = 0.01;
    private static int binNumber;
    private boolean isDirty;

    public TreeMap<String, Set<GOTerm>> getTreeMapForComponent() {
        return treeMapForComponent;
    }

    public void setTreeMapForComponent(TreeMap<String, Set<GOTerm>> treeMapForComponent) {
        this.treeMapForComponent = treeMapForComponent;
    }

    public TreeMap<String, Set<GOTerm>> getTreeMapForFunction() {
        return treeMapForFunction;
    }

    public void setTreeMapForFunction(TreeMap<String, Set<GOTerm>> treeMapForFunction) {
        this.treeMapForFunction = treeMapForFunction;
    }

    public TreeMap<String, Set<GOTerm>> getTreeMapForProcess() {
        return treeMapForProcess;
    }

    public void setTreeMapForProcess(TreeMap<String, Set<GOTerm>> treeMapForProcess) {
        this.treeMapForProcess = treeMapForProcess;
    }

    //For go terms
   private TreeMap<String, Set<GOTerm>> treeMapForComponent;
    private TreeMap<String, Set<GOTerm>> treeMapForFunction;
    private TreeMap<String, Set<GOTerm>> treeMapForProcess;

    public CellularNetWorkElementInformation(int ppInteractionNum, int pdInteractionNum, boolean includePPInteraction, boolean includePDInteraction, DSGeneMarker dSGeneMarker, String goInfoStr, String geneType) {
        this.ppInteractionNum = ppInteractionNum;
        this.pdInteractionNum = pdInteractionNum;
        this.includePPInteraction = includePPInteraction;
        this.includePDInteraction = includePDInteraction;
        this.dSGeneMarker = dSGeneMarker;
        this.goInfoStr = goInfoStr;
        this.geneType = geneType;
    }

    public CellularNetWorkElementInformation(DSGeneMarker dSGeneMarker) {
        this.dSGeneMarker = dSGeneMarker;
        smallestIncrement = defaultSmallestIncrement;
        isDirty = true;
        goInfoStr = "";
        Set<GOTerm> set = GeneOntologyUtil.getOntologyUtil().getAllGOTerms(dSGeneMarker);
       treeMapForComponent = GeneOntologyUtil.getOntologyUtil().getAllGoTerms(dSGeneMarker, AnnotationParser.GENE_ONTOLOGY_CELLULAR_COMPONENT);
        treeMapForFunction = GeneOntologyUtil.getOntologyUtil().getAllGoTerms(dSGeneMarker, AnnotationParser.GENE_ONTOLOGY_MOLECULAR_FUNCTION);
        treeMapForProcess = GeneOntologyUtil.getOntologyUtil().getAllGoTerms(dSGeneMarker, AnnotationParser.GENE_ONTOLOGY_BIOLOGICAL_PROCESS);


        if (set != null && set.size() > 0) {
            for (GOTerm goTerm : set) {
                goInfoStr += goTerm.getName() + "; ";
            }
        }
        geneType = GeneOntologyUtil.getOntologyUtil().checkMarkerFunctions(dSGeneMarker);
        reset();

    }

    public void reset() {
        // isDirty = true;
        ppInteractionNum = 0;
        pdInteractionNum = 0;
        binNumber = (int) (1 / smallestIncrement) + 1;
        distribution = new int[binNumber];
        ppDistribution = new int[binNumber];
        pdDistribution = new int[binNumber];
        for (int i = 0; i < binNumber; i++) {
            distribution[i] = 0;
            pdDistribution[i] = ppDistribution[i] = 0;
        }
    }

    public ArrayList<InteractionDetail> getSelectedInteractions() {
        ArrayList<InteractionDetail> arrayList = new ArrayList<InteractionDetail>();
        if (interactionDetails != null && interactionDetails.length > 0) {
            for (int i = 0; i < interactionDetails.length; i++) {
                InteractionDetail interactionDetail = interactionDetails[i];
                if (interactionDetail != null && interactionDetail.getConfidence() >= threshold) {
                    if (isIncludePDInteraction() && interactionDetail.getInteraactionType().equalsIgnoreCase(InteractionDetail.PROTEINDNAINTERACTION))
                    {
                        arrayList.add(interactionDetail);
                    }
                    if (isIncludePPInteraction() && interactionDetail.getInteraactionType().equalsIgnoreCase(InteractionDetail.PROTEINPROTEININTERACTION))
                    {
                        arrayList.add(interactionDetail);
                    }
                }
            }
        }
        return arrayList;
    }

    public static double getSmallestIncrement() {
        return smallestIncrement;
    }

    public void setSmallestIncrement(double smallestIncrement) {
        this.smallestIncrement = smallestIncrement;
    }

    public int[] getDistribution() {
        return distribution;
    }

    public void setDistribution(int[] distribution) {
        this.distribution = distribution;
    }

    public static int getBinNumber() {
        return binNumber;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public static void setBinNumber(int binNumber) {
        CellularNetWorkElementInformation.binNumber = binNumber;
    }

    public CellularNetWorkElementInformation(int pdInteractionNum, int ppInteractionNum, DSGeneMarker dSGeneMarker) {
        this.pdInteractionNum = pdInteractionNum;
        this.ppInteractionNum = ppInteractionNum;
        this.dSGeneMarker = dSGeneMarker;
    }

    public int getPpInteractionNum() {
        return ppInteractionNum;
    }

    public static double getThreshold() {
        return threshold;
    }

    public void setThreshold(double _threshold) {
        if (this.threshold != _threshold) {
            this.threshold = _threshold;

        }
        update();
    }

    public InteractionDetail[] getInteractionDetails() {
        return interactionDetails;
    }

    public void setInteractionDetails(ArrayList<InteractionDetail> arrayList) {

        if (arrayList != null && arrayList.size() > 0) {
            interactionDetails = new InteractionDetail[2];
            this.interactionDetails = arrayList.toArray(interactionDetails);
        }
        if (interactionDetails != null) {
            update();
        }
    }

    /**
     * Update the number of interaction based on the new threshold or new InteractionDetails.
     */
    private void update() {
        if (interactionDetails == null || interactionDetails.length == 0) {
            return;
        }

        reset();
        for (InteractionDetail interactionDetail : interactionDetails) {
            if (interactionDetail != null) {
                int confidence = (int) (interactionDetail.getConfidence() * 100);
                if (confidence < distribution.length && confidence >= 0) {
                    for (int i = 0; i <= confidence; i++) {
                        distribution[i]++;
                    }
                    if (interactionDetail.getInteraactionType().equalsIgnoreCase(InteractionDetail.PROTEINPROTEININTERACTION))
                    {
                        for (int i = 0; i <= confidence; i++) {
                            ppDistribution[i]++;
                        }
                    } else {
                        for (int i = 0; i <= confidence; i++) {
                            pdDistribution[i]++;
                        }
                    }
                }
                if (confidence >= 100 * threshold) {
                    if (interactionDetail.getInteraactionType().equals(InteractionDetail.PROTEINPROTEININTERACTION)) {
                        ppInteractionNum++;
                    } else {
                        pdInteractionNum++;
                    }

                }
            }
        }
    }

    public int[] getPpDistribution() {
        return ppDistribution;
    }

    public void setPpDistribution(int[] ppDistribution) {
        this.ppDistribution = ppDistribution;
    }

    public int[] getPdDistribution() {
        return pdDistribution;
    }

    public void setPdDistribution(int[] pdDistribution) {
        this.pdDistribution = pdDistribution;
    }

    public void setPpInteractionNum(int ppInteractionNum) {
        this.ppInteractionNum = ppInteractionNum;
    }

    public int getPdInteractionNum() {
        return pdInteractionNum;
    }

    public void setPdInteractionNum(int pdInteractionNum) {
        this.pdInteractionNum = pdInteractionNum;
    }

    public boolean isIncludePPInteraction() {
        return includePPInteraction;
    }

    public void setIncludePPInteraction(boolean includePPInteraction) {
        this.includePPInteraction = includePPInteraction;
    }

    public boolean isIncludePDInteraction() {
        return includePDInteraction;
    }

    public void setIncludePDInteraction(boolean includePDInteraction) {
        this.includePDInteraction = includePDInteraction;
    }

    public DSGeneMarker getdSGeneMarker() {
        return dSGeneMarker;
    }

    public void setdSGeneMarker(DSGeneMarker dSGeneMarker) {
        this.dSGeneMarker = dSGeneMarker;
    }

    public String getGoInfoStr() {
        return goInfoStr;
    }

    public void setGoInfoStr(String goInfoStr) {
        this.goInfoStr = goInfoStr;
    }

    public String getGeneType() {
        return geneType;
    }

    public void setGeneType(String geneType) {
        this.geneType = geneType;
    }
}
