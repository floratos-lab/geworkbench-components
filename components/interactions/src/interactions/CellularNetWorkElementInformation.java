package interactions;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

import java.util.ArrayList;

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
        reset();

    }

    public void reset() {
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
        System.out.println("PP: " + ppInteractionNum + " at the begiin update." + threshold);
        for (InteractionDetail interactionDetail : interactionDetails) {
            if(interactionDetail!=null){
            int confidence = (int) (interactionDetail.getConfidence() * 100);
            if (confidence < distribution.length && confidence >= 0) {
                for (int i = 0; i <= confidence; i++) {
                    distribution[i]++;
                }
                if (interactionDetail.getInteraactionType().equalsIgnoreCase(InteractionDetail.PROTEINPRETEININTERACTION))
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
                if (interactionDetail.getInteraactionType().equals(InteractionDetail.PROTEINPRETEININTERACTION)) {
                    ppInteractionNum++;
                } else {
                    pdInteractionNum++;
                }

            }
        }
        }
        System.out.println("PP: " + ppInteractionNum + " after update.");
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
