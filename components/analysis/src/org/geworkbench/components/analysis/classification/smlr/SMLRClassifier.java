package org.geworkbench.components.analysis.classification.smlr;

import org.geworkbench.bison.algorithm.classification.Classifier;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import edu.duke.cs.smlr.learn.results.LearnResult;
import edu.duke.cs.smlr.learn.results.TestResult;
import edu.duke.cs.smlr.learn.properties.LearnProperty;
import edu.duke.cs.smlr.learn.properties.TestProperty;
import edu.duke.cs.smlr.learn.interfaces.IMatrix;
import edu.duke.cs.smlr.learn.Tester;
import edu.duke.cs.smlr.util.matrix.CernMatrix;

/**
 * @author John Watkinson
 */
public class SMLRClassifier extends Classifier {

    private LearnResult learnResult;
    private LearnProperty learnProperty;

    private static final String CLASS_CONTROL = "Control";
    private static final String CLASS_CASE = "Case";

    public SMLRClassifier(DSDataSet parent, String label, LearnResult learnResult, LearnProperty learnProperty) {
        super(parent, label, new String[] {CLASS_CASE, CLASS_CONTROL});
        this.learnResult = learnResult;
        this.learnProperty = learnProperty;
    }

    public String classify(float[] data) {
        TestProperty testProperty = new TestProperty(learnProperty);
        IMatrix testData = new CernMatrix(1, data.length);
        for (int i = 0; i < data.length; i++) {
            testData.set(data[i], 0, i);
        }
        IMatrix testClassifications = new CernMatrix(1, 1);
        testClassifications.set(SMLRTraining.CLASS_CASE, 0, 0);
        testProperty.setClassifications(testClassifications);
        testProperty.setWeights(learnResult.getWeights());
        testProperty.setTestData(testData);
        TestResult testResult = Tester.test(testProperty);
        if (testResult.getNumCorrect() == 1) {
            return CLASS_CASE;
        } else {
            return CLASS_CONTROL;
        }
    }
}
