package org.geworkbench.components.analysis.classification.svm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.algorithms.AbstractTraining;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.svm.SVMClassifier;
import org.geworkbench.util.svm.SupportVectorMachine;

import javax.swing.*;
import java.util.List;

public class SVMTraining extends AbstractTraining {

    static Log log = LogFactory.getLog(SVMTraining.class);

    public SVMTraining() {
        setLabel("SVM Classifier");
        panel = new SVMTrainingPanel();
        setDefaultPanel(panel);
    }

    protected CSClassifier trainClassifier(List<float[]> caseData, List<float[]> controlData) {
        log.debug("Training classifier.");
        // Get params
        SVMTrainingPanel svmPanel = (SVMTrainingPanel) panel;
        float epsilon = svmPanel.getEpsilon();
        float c = svmPanel.getC();
        SupportVectorMachine svm = null;
        SVMClassifier classifier = null;
        try {
            svm = new SupportVectorMachine(caseData, controlData, svmPanel.getSelectedKernel(), 0.1f);
            // SMO
            svm.buildSupportVectorsSMO(c, epsilon);
            log.debug("Classifier training complete.");
            classifier = svm.getClassifier(null, "Phenotype Classifier");
        } catch (ClassifierException e) {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }
        if (classifier != null)
            classifier.setLabel("SVM Phenotype Classifier");
        return classifier;
    }

}
