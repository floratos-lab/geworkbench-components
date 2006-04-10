package org.geworkbench.components.analysis.classification.smlr;

import org.geworkbench.components.analysis.classification.AbstractTraining;
import org.geworkbench.components.analysis.classification.svm.SVMTrainingPanel;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.algorithm.classification.Classifier;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.util.svm.SupportVectorMachine;
import org.geworkbench.util.svm.SVMClassifier;
import org.geworkbench.util.ClassifierException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.util.List;

import edu.duke.cs.smlr.util.Constants;
import edu.duke.cs.smlr.util.matrix.CernMatrix;
import edu.duke.cs.smlr.learn.interfaces.IMatrix;
import edu.duke.cs.smlr.learn.properties.LearnProperty;
import edu.duke.cs.smlr.learn.results.LearnResult;
import edu.duke.cs.smlr.learn.Learner;

public class SMLRTraining extends AbstractTraining implements ClusteringAnalysis {

    static Log log = LogFactory.getLog(SMLRTraining.class);

    public static final String KERNEL_DIRECT = "Direct";
    public static final String KERNEL_RBF = "Radial Basis Function";
    public static final String KERNEL_LINEAR = "Linear";
    public static final String KERNEL_POLYNOMIAL = "2nd-Degree Polynomial";
    public static final String KERNEL_COSINE = "Cosine";

    public static final String[] KERNELS =
            {
                    KERNEL_DIRECT,
                    KERNEL_RBF,
                    KERNEL_LINEAR,
                    KERNEL_POLYNOMIAL,
                    KERNEL_COSINE
            };

    public static final String PRIOR_LAPLACIAN = "Laplacian";
    public static final String PRIOR_GAUSSIAN = "Gaussian";
    public static final String PRIOR_NONE = "None";

    public static final String[] PRIORS =
            {
                    PRIOR_LAPLACIAN,
                    PRIOR_GAUSSIAN,
                    PRIOR_NONE
            };

    public static final int CLASS_CASE = 1;
    public static final int CLASS_CONTROL = 2;

    public SMLRTraining() {
        setLabel("SMLR Classifier");
        panel = new SMLRTrainingPanel(this);
        setDefaultPanel(panel);
    }

    public int getAnalysisType() {
        return AbstractAnalysis.TTEST_TYPE;
    }

    protected Classifier trainClassifier(List<float[]> caseData, List<float[]> controlData) {
        SMLRTraining.log.debug("Training classifier.");
        SMLRTrainingPanel smlrPanel = (SMLRTrainingPanel) panel;
        // Get params
        float epsilon = smlrPanel.getEpsilon();
        String prior = smlrPanel.getPrior();
        String kernel = smlrPanel.getSelectedKernel();
        float gamma = smlrPanel.getGamma();
        int kernelValue = Constants.KERNEL_DIRECT;
        double kernelParameter = 0;
        if (KERNEL_LINEAR.equals(kernel)) {
            kernelValue = Constants.KERNEL_LINEAR;
        } else if (KERNEL_POLYNOMIAL.equals(kernel)) {
            kernelValue = Constants.KERNEL_POLYNOMIAL;
            kernelParameter = 2;
        } else if (KERNEL_RBF.equals(kernel)) {
            kernelValue = Constants.KERNEL_RBF;
            kernelParameter = gamma;
        } else if (KERNEL_COSINE.equals(kernel)) {
            kernelValue = Constants.KERNEL_COSINE;
        }
        int priorValue = Constants.PRIOR_LAPLACIAN;
        if (PRIOR_GAUSSIAN.equals(prior)) {
            priorValue = Constants.PRIOR_GAUSSIAN;
        } else if (PRIOR_NONE.equals(prior)) {
            priorValue = Constants.PRIOR_NONE;
        }
        int rows = caseData.size() + controlData.size();
        int columns = controlData.get(0).length;
        IMatrix data = new CernMatrix(rows, columns);
        IMatrix classifications = new CernMatrix(rows, 1);
        // Prepare training data
        {
            for (int r = 0; r < caseData.size(); r++) {
                float[] values = caseData.get(r);
                classifications.set(CLASS_CASE, r, 0);
                for (int c = 0; c < columns; c++) {
                    data.set(values[c], r, c);
                }
            }
            int offset = caseData.size();
            for (int r = 0; r < controlData.size(); r++) {
                float[] values = controlData.get(r);
                classifications.set(CLASS_CONTROL, r + offset, 0);
                for (int c = 0; c < columns; c++) {
                    data.set(values[c], r + offset, c);
                }
            }
        }
        // Set up learn properties
        LearnProperty learnProperty = new LearnProperty();
        learnProperty.setAddBias(true);
        learnProperty.setCacheExp(true);
        learnProperty.setTrainingData(data);
        learnProperty.setClassifications(classifications);
        learnProperty.setConvTol(epsilon);
        learnProperty.setDisplay(true);
        learnProperty.setFileName("testResults.txt"); // Not used
        learnProperty.setKernel(kernelValue);
        learnProperty.setKernelPar(kernelParameter);
        learnProperty.setKillThreshold(1E-4);
        learnProperty.setLambda(0.1);
        learnProperty.setMaxIters(10000);
        learnProperty.setMethod(Constants.ALGORITHM_COMPONENT);
        learnProperty.setNormalize(true);
        learnProperty.setPrior(priorValue);
        learnProperty.setUseOverrelaxation(false);
        LearnResult learnResult = Learner.learn(learnProperty);
        return new SMLRClassifier(null, "SMLR Phenotype Classifier", learnResult, learnProperty);
    }

}
