package org.geworkbench.components.ei;

import org.jfree.data.xy.XYSeries;

import edu.columbia.c2b2.evidenceinegration.Edge;
import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Aug 11, 2007
 * Time: 12:59:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class ROCComputation {

    public XYSeries getXYSeries(double miniumIncreaseForThreshold, Evidence evidence, int currentGoldStandNumber) {
        XYSeries series = new XYSeries("" + evidence.getName());
        if (evidence != null && currentGoldStandNumber >= 0) {
            int totalBinNumber = (int) (1 / miniumIncreaseForThreshold) + 1;

            int[] pp = new int[totalBinNumber];
            int[] np = new int[totalBinNumber];
            int totalPP = 0;
            int totalNP = 0;
            for (int i = 0; i < totalBinNumber; i++) {
                pp[i] = np[i] = 0;
            }
            for (Edge edge : evidence.getEdges()) {
                float lr = edge.getLikelihoodRatio();
                int startBinNumber = (int) (lr / miniumIncreaseForThreshold);
                int[] realValues = edge.getRealValue();
                if (realValues != null && realValues.length > currentGoldStandNumber) {
                    if (realValues[currentGoldStandNumber] > 0) {
                        totalPP++;
                        for (int i = startBinNumber; i < totalBinNumber; i++) {
                            pp[i]++;
                        }
                    }


                    if (realValues[currentGoldStandNumber] < 0) {
                        totalNP++;
                        for (int i = startBinNumber; i < totalBinNumber; i++) {
                            np[i]++;
                        }
                    }
                }
            }
            for (int i = 0; i < totalBinNumber; i++) {
                double noise = 0.000000000001;

                series.add(((double) pp[i]) / (totalPP + noise), ((double) np[i]) / (totalNP + noise));
            }
            //           System.out.println("For the gs " + currentGoldStandNumber + " total edge = " + totalEdgeNumber + " " + totalPP + " " + totalNP);

        }


        return series;
    }

}
