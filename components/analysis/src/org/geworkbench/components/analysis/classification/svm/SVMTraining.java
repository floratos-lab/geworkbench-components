package org.geworkbench.components.analysis.classification.svm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.algorithm.classification.Classifier;
import org.geworkbench.components.analysis.classification.AbstractTraining;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.svm.SVMPhenotypeClassifier;
import org.geworkbench.util.svm.SupportVectorMachine;

import javax.swing.*;
import java.util.List;
import java.util.Random;

public class SVMTraining extends AbstractTraining implements ClusteringAnalysis {

    static Log log = LogFactory.getLog(SVMTraining.class);

    Random rand = new Random();

    private static class Indexable implements Comparable {

        private double[] data;
        private int index;

        public Indexable(double[] data, int index) {
            this.data = data;
            this.index = index;
        }

        public int compareTo(Object o) {
            // Assumes that the other object is an indexable referencing the same data
            Indexable other = (Indexable) o;
            if (data[index] > data[other.index]) {
                return 1;
            } else if (data[index] < data[other.index]) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    public SVMTraining() {
        setLabel("SVM Classifier");
        panel = new SVMTrainingPanel();
        setDefaultPanel(panel);
    }

    public int getAnalysisType() {
        return AbstractAnalysis.TTEST_TYPE;
    }

    protected Classifier trainClassifier(List<float[]> caseData, List<float[]> controlData) {
        log.debug("Training classifier.");
        // Get params
        float epsilon = panel.getEpsilon();
        float c = panel.getC();
        SupportVectorMachine svm = null;
        SVMPhenotypeClassifier classifier = null;
        try {
            svm = new SupportVectorMachine(caseData, controlData, panel.getSelectedKernel(), 0.1f);
            // SMO
            svm.buildSupportVectorsSMO(c, epsilon);
            log.debug("Classifier training complete.");
            classifier = svm.getClassifier(null, "Phenotype Classifier");
        } catch (ClassifierException e) {
            JOptionPane.showMessageDialog(panel, e.getMessage());
            log.warn(e);
        }
        classifier.setLabel("SVM Phenotype Classifier");
        return classifier;
    }

    private void warnOnInvalidData(List<float[]> data) {
        for (float[] floats : data) {
            for (float v : floats) {
                if (Float.isNaN(v)) {
                    log.warn("NaN at location ");
                } else if (Float.isInfinite(v)) {
                    log.warn("Infinite.");
                }
            }
        }
    }

}
