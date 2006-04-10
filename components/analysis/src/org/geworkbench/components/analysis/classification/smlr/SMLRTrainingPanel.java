package org.geworkbench.components.analysis.classification.smlr;

import org.geworkbench.components.analysis.classification.AbstractTrainingPanel;
import org.geworkbench.components.analysis.classification.svm.SVMTrainingPanel;
import org.geworkbench.util.svm.SupportVectorMachine;
import org.geworkbench.util.svm.KernelFunction;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.bison.algorithm.classification.Classifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class SMLRTrainingPanel extends AbstractTrainingPanel {

    static Log log = LogFactory.getLog(SMLRTrainingPanel.class);

    private static final double DEFAULT_EPSILON_VALUE = 0.001;
    private static final double DEFAULT_GAMMA_VALUE = 1;

    private JFormattedTextField epsilon = new JFormattedTextField();
    private JComboBox priorCombo = new JComboBox(SMLRTraining.PRIORS);
    private JComboBox kernelFunctionCombo = new JComboBox(SMLRTraining.KERNELS);;
    private JFormattedTextField gamma = new JFormattedTextField();

    private SMLRTraining smlrTraining;

    private static class SerializedInstance implements Serializable {

        private Number epsilon;
        private Number gamma;

        public SerializedInstance(Number epsilon, Number gamma) {
            this.epsilon = epsilon;
            this.gamma = gamma;
        }

        Object readResolve() throws ObjectStreamException {
            SMLRTrainingPanel panel = new SMLRTrainingPanel(null);
            panel.epsilon.setValue(epsilon);
            panel.gamma.setValue(gamma);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SMLRTrainingPanel.SerializedInstance((Number) epsilon.getValue(), (Number)gamma.getValue());
    }

    public SMLRTrainingPanel(SMLRTraining smlrTraining) {
        this.smlrTraining = smlrTraining;
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initUI() {
        epsilon = new JFormattedTextField(new DecimalFormat("0.#####E0"));
        epsilon.setValue(SMLRTrainingPanel.DEFAULT_EPSILON_VALUE);
        gamma = new JFormattedTextField(new DecimalFormat("0.#####E0"));
        gamma.setEnabled(false);
        gamma.setValue(SMLRTrainingPanel.DEFAULT_GAMMA_VALUE);

        kernelFunctionCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (kernelFunctionCombo.getSelectedItem().equals(SMLRTraining.KERNEL_RBF.toString())) {
                    gamma.setEnabled(true);
                } else {
                    gamma.setEnabled(false);
                }
            }
        });
    }

    protected Classifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException {
        return smlrTraining.trainClassifier(trainingCaseData, trainingControlData);
    }

    protected void addParameters(DefaultFormBuilder builder) {
        builder.appendSeparator("SMLR Parameters");

        builder.append("Prior", priorCombo);
        builder.append("Epsilon", epsilon);
        builder.append("Kernel Function", kernelFunctionCombo);
        builder.append("Gamma", gamma);
    }

    public float getEpsilon() {
        return ((Number) epsilon.getValue()).floatValue();
    }

    public String getPrior() {
        return SMLRTraining.PRIORS[priorCombo.getSelectedIndex()];
    }

    public float getGamma() {
        return ((Number) gamma.getValue()).floatValue();
    }

    public String getSelectedKernel() {
        return SMLRTraining.KERNELS[kernelFunctionCombo.getSelectedIndex()];
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}
