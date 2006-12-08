package org.geworkbench.components.analysis.classification.svm.test;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.algorithms.KFoldCrossValidation;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.svm.SupportVectorMachine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Test SVM training and results.
 * User: mhall
 * Date: Jan 5, 2006
 * Time: 11:54:57 AM
 */
public class SVMTest extends TestCase {

    static Log log = LogFactory.getLog(SVMTest.class);

    private static int INDEX_CLASSIFICATIONS = 2;
    private static int INDEX_DATA = 8;


    public List<SVMData> getMArraySet(File file) {

        List<SVMData> returnList = new ArrayList<SVMData>();

        // First line contains titles and marker names
        BufferedReader fin = null;
        try {
            fin = new BufferedReader(new FileReader(file));
            String[] header = fin.readLine().split("\t");
            int markerCount = header.length - INDEX_DATA;


            List<String[]> dataLines = new ArrayList<String[]>();
            String line = fin.readLine();
            while (line != null) {
                dataLines.add(line.split("\t"));
                line = fin.readLine();
            }

            int count = 0;
            for (String[] values : dataLines) {
                // Handle class data
                int dataClassification = -1;
                for (int i = INDEX_CLASSIFICATIONS; i < INDEX_DATA; i++) {
                    String classification = values[i];
                    try {
                        if (Integer.parseInt(classification) == 1) {
                            if (dataClassification < 0) {
                                dataClassification = i - INDEX_CLASSIFICATIONS;
//                                log.debug(values[0]+" is class "+(i - INDEX_CLASSIFICATIONS));
                            } else {
                                log.warn("More than one classification for data.");
                            }
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Couldn't parse class value " + header[i] + " on line " + count);
                    }
                }

                float[] data = new float[header.length - INDEX_DATA];

                for (int i = INDEX_DATA; i < values.length; i++) {
                    String value = values[i];
                    try {
                        data[i - INDEX_DATA] = Float.parseFloat(value);
                    } catch (NumberFormatException e) {
//                        log.warn("Couldn't format value at " + (i - INDEX_DATA) + " for " + values[0]+", setting to 0.");
                        data[i - INDEX_DATA] = 0;
                    }
                }

                returnList.add(new SVMData(values[0], dataClassification, data));

                count++;
            }
        } catch (Exception e) {
            log.error(e);
        }

        return returnList;
    }


    private void normalizeData(List<SVMData> maSet) {
        float max = 0f;

    }

    public void testSVM() {
        List<SVMData> data = getMArraySet(new File("data/yeastORFwithClass.txt"));
        log.debug("Data size: " + data.size());

        // 0 = TCA	 1 = Resp	2 = Ribo	3 = Proteas	4 = Hist	5 = HTH

        List<float[]> caseData = new ArrayList<float[]>();
        List<float[]> controlData = new ArrayList<float[]>();

        int targetClass = 2;
        for (int i = 0; i < data.size(); i++) {
            SVMData svmData = data.get(i);
            if (svmData.classification == targetClass) {
                caseData.add(svmData.data);
            } else {
                controlData.add(svmData.data);
            }
        }

        log.debug("Class "+targetClass+" data size is "+caseData.size());
        log.debug("Control data size is "+controlData.size());

        KFoldCrossValidation cross = new KFoldCrossValidation(3, caseData, controlData);

        int truePositives = 0, falseNegatives = 0, falsePositives = 0, trueNegatives = 0;

        for (int i = 0; i < cross.getNumFolds(); i++) {
            KFoldCrossValidation.CrossValidationData crossData = cross.getData(i);
            log.debug("Training classifier data set on fold " + i);
            SupportVectorMachine svm = null;
            try {
                svm = new SupportVectorMachine(crossData.getTrainingCaseData(), crossData.getTrainingControlData(),
                        SupportVectorMachine.LINEAR_KERNAL_FUNCTION, 0.1f);
            } catch (ClassifierException e) {
                log.error(e);
            }
            // Non-SMO
            // svm.buildSupportVectors(1000, 1e-6);
            // SMO
            svm.buildSupportVectorsSMO(1);
            log.debug("Classifier training complete.");

//            log.debug("Classifying test case data for set #" + i);
            int numInClass1 = 0;
            for (float[] values : crossData.getTestCaseData()) {
                if (svm.evaluate(values)) {
                    numInClass1++;
                }
            }
            truePositives += numInClass1;
            falseNegatives += (crossData.getTestCaseData().size()-numInClass1);
            log.debug("True positives: "+numInClass1+",  false negatives: "+(crossData.getTestCaseData().size()-numInClass1));

//            log.debug("Classifying test control data for set #" + i);
            numInClass1 = 0;
            for (float[] values : crossData.getTestControlData()) {
                if (svm.evaluate(values)) {
                    numInClass1++;
                }
            }
            falsePositives += numInClass1;
            trueNegatives += (crossData.getTestControlData().size()-numInClass1);
            log.debug("False positives: "+numInClass1+", true negatives:  "+(crossData.getTestControlData().size()-numInClass1));
        }

        log.debug("Results: ");
        log.debug("FP\tFN\tTP\tTN");
        log.debug(falsePositives + "\t" + falseNegatives + "\t" + truePositives + "\t" + trueNegatives);

    }

    public static class SVMData {
        // @todo Support multiple classifications
        String label;
        int classification;
        float[] data;

        public SVMData(String label, int classification, float[] data) {
            this.label = label;
            this.classification = classification;
            this.data = data;
        }
    }
}
