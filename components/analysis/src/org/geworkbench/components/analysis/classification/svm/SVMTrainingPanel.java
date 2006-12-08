package org.geworkbench.components.analysis.classification.svm;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.algorithm.classification.CSClassifier;
import org.geworkbench.util.ClassifierException;
import org.geworkbench.util.svm.KernelFunction;
import org.geworkbench.util.svm.SupportVectorMachine;
import org.geworkbench.algorithms.AbstractTrainingPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.DecimalFormat;

public class SVMTrainingPanel extends AbstractTrainingPanel {

    static Log log = LogFactory.getLog(SVMTrainingPanel.class);

    private static final double DEFAULT_EPSILON_VALUE = 0.001;
    private static final double DEFAULT_C_VALUE = 1;
    private static final double DEFAULT_GAMMA_VALUE = 1;

    private JFormattedTextField epsilon = new JFormattedTextField();
    private JFormattedTextField c = new JFormattedTextField();
    private JComboBox kernelFunctionCombo = new JComboBox();
    private JFormattedTextField gamma = new JFormattedTextField();

    private static class SerializedInstance implements Serializable {

        private Number epsilon;
        private Number c;
        private Number gamma;

        public SerializedInstance(Number epsilon, Number c,  Number gamma) {
            this.epsilon = epsilon;
            this.c = c;
            this.gamma = gamma;
        }

        Object readResolve() throws ObjectStreamException {
            SVMTrainingPanel panel = new SVMTrainingPanel();
            panel.epsilon.setValue(epsilon);
            panel.c.setValue(c);
            panel.gamma.setValue(gamma);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance((Number) epsilon.getValue(), (Number) c.getValue(), (Number)gamma.getValue());
    }

    public SVMTrainingPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void initUI() {
        epsilon = new JFormattedTextField(new DecimalFormat("0.#####E0"));
        epsilon.setValue(DEFAULT_EPSILON_VALUE);
        c = new JFormattedTextField(new DecimalFormat());
        c.setValue(DEFAULT_C_VALUE);
        gamma = new JFormattedTextField(new DecimalFormat("0.#####E0"));
        gamma.setEnabled(false);
        gamma.setValue(DEFAULT_GAMMA_VALUE);
        numberFolds = new JFormattedTextField(3);
        kernelFunctionCombo.addItem(SupportVectorMachine.LINEAR_KERNAL_FUNCTION.toString());
        kernelFunctionCombo.addItem(SupportVectorMachine.LINEAR_KERNAL_FUNCTION_2ND_POWER.toString());
        kernelFunctionCombo.addItem(SupportVectorMachine.RADIAL_BASIS_KERNEL.toString());

        kernelFunctionCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (kernelFunctionCombo.getSelectedItem().equals(SupportVectorMachine.RADIAL_BASIS_KERNEL.toString())) {
                    gamma.setEnabled(true);
                } else {
                    gamma.setEnabled(false);
                }
            }
        });
    }

    protected CSClassifier trainForValidation(java.util.List<float[]> trainingCaseData, java.util.List<float[]> trainingControlData) throws ClassifierException {
        SupportVectorMachine svm = new SupportVectorMachine(trainingCaseData, trainingControlData, getSelectedKernel(), 0.1f);
        setTrainingTask(svm);
        // Non-SMO
        // svm.buildSupportVectors(1000, 1e-6);
        // SMO
        svm.buildSupportVectorsSMO(((Number) c.getValue()).floatValue(), ((Number) epsilon.getValue()).floatValue());
        CSClassifier classifier = svm.getClassifier(null, "");
        return classifier;
    }

    protected void addParameters(DefaultFormBuilder builder) {
        builder.appendSeparator("Support Vector Machine Parameters");

        builder.append("Epsilon", epsilon);
        builder.append("Alpha Bound (C)", c);
        builder.append("Kernel Function", kernelFunctionCombo);
        builder.append("Gamma", gamma);
    }

    public float getEpsilon() {
        return ((Number) epsilon.getValue()).floatValue();
    }

    public float getC() {
        return ((Number) c.getValue()).floatValue();
    }

    public KernelFunction getSelectedKernel() {
        if (kernelFunctionCombo.getSelectedItem().equals(SupportVectorMachine.LINEAR_KERNAL_FUNCTION.toString())) {
            return SupportVectorMachine.LINEAR_KERNAL_FUNCTION;
        } else if (kernelFunctionCombo.getSelectedItem().equals(SupportVectorMachine.LINEAR_KERNAL_FUNCTION_2ND_POWER.toString())) {
            return SupportVectorMachine.LINEAR_KERNAL_FUNCTION_2ND_POWER;
        } else if (kernelFunctionCombo.getSelectedItem().equals(SupportVectorMachine.RADIAL_BASIS_KERNEL.toString())) {
            return SupportVectorMachine.RADIAL_BASIS_KERNEL;
        }
        return null;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}

