package org.geworkbench.components.analysis.classification.svm;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.util.svm.SupportVectorMachine;
import org.geworkbench.util.svm.KernelFunction;
import org.geworkbench.util.svm.ClassifierException;
import org.geworkbench.util.svm.TrainingProgressListener;
import org.geworkbench.util.ProgressGraph;
import org.geworkbench.util.threading.SwingWorker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.text.DecimalFormat;

public class SVMOptimizationPanel extends AbstractSaveableParameterPanel implements Serializable, TrainingProgressListener {
    static Log log = LogFactory.getLog(SVMOptimizationPanel.class);

    public static final String DEFAULT_TRAINING_MESSAGE = "Training Progress";

    private static final double DEFAULT_EPSILON_VALUE = 0.001;
    private static final double DEFAULT_C_VALUE = 1;

    private JFormattedTextField epsilon = new JFormattedTextField();
    private JFormattedTextField C = new JFormattedTextField();
    private JFormattedTextField numberFolds = new JFormattedTextField();
    private JComboBox kernelFunctionCombo = new JComboBox();
    private ProgressGraph progressGraph = new ProgressGraph(0, 20, 100);
    private JLabel trainingMessage = new JLabel(DEFAULT_TRAINING_MESSAGE);
    private JLabel falsePositives = new JLabel();
    private JLabel falseNegatives = new JLabel();
    private JLabel truePositives = new JLabel();
    private JLabel trueNegatives = new JLabel();

    private JButton crossTest = new JButton("Test via Cross Validation");
    private JButton cancelTest = new JButton("Cancel Test");

    private DSMicroarraySet maSet = null;

    private ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
    private ArrayList<JCheckBox> classifyCheckboxes = new ArrayList<JCheckBox>();
    private SwingWorker worker;

    private static class SerializedInstance implements Serializable {

        private Number epsilon;
        private Number C;

        public SerializedInstance(Number epsilon, Number c) {
            this.epsilon = epsilon;
            this.C = c;
        }

        Object readResolve() throws ObjectStreamException {
            SVMOptimizationPanel panel = new SVMOptimizationPanel();
            panel.epsilon.setValue(epsilon);
            panel.C.setValue(C);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance((Number) epsilon.getValue(), (Number) C.getValue());
    }

    public SVMOptimizationPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        epsilon = new JFormattedTextField(new DecimalFormat());
        epsilon.setValue(DEFAULT_EPSILON_VALUE);
        C = new JFormattedTextField(new DecimalFormat());
        C.setValue(DEFAULT_C_VALUE);
        numberFolds = new JFormattedTextField(3);
        kernelFunctionCombo.addItem(SupportVectorMachine.LINEAR_KERNAL_FUNCTION.toString());
        kernelFunctionCombo.addItem(SupportVectorMachine.LINEAR_KERNAL_FUNCTION_2ND_POWER.toString());
        kernelFunctionCombo.addItem(SupportVectorMachine.RADIAL_BASIS_KERNEL.toString());

        final JComponent workerParent = this;

        crossTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                worker = new SwingWorker() {
                    int numTruePositives = 0, numFalseNegatives = 0, numFalsePositives = 0, numTrueNegatives = 0;
                    String errorString = null;

                    SupportVectorMachine svm = null;

                    protected Object doInBackground() throws Exception {
                        truePositives.setText("Working...");
                        truePositives.repaint();
                        falsePositives.setText("Working...");
                        falsePositives.repaint();
                        trueNegatives.setText("Working...");
                        trueNegatives.repaint();
                        falseNegatives.setText("Working...");
                        falseNegatives.repaint();

                        DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(maSet);

                        java.util.List<float[]> caseData = new ArrayList<float[]>();
                        SVMAnalysis.addMicroarrayData(context.getItemsForClass(CSAnnotationContext.CLASS_CASE), caseData);
                        java.util.List<float[]> controlData = new ArrayList<float[]>();
                        SVMAnalysis.addMicroarrayData(context.getItemsForClass(CSAnnotationContext.CLASS_CONTROL), controlData);
                        java.util.List<float[]> testData = new ArrayList<float[]>();
                        SVMAnalysis.addMicroarrayData(context.getItemsForClass(CSAnnotationContext.CLASS_TEST), testData);

                        int numFolds = ((Number) numberFolds.getValue()).intValue();
                        KFoldCrossValidation cross = new KFoldCrossValidation(numFolds, caseData, controlData);


                        try {
                            for (int i = 0; i < cross.getNumFolds() && !isCancelled(); i++) {
                                KFoldCrossValidation.CrossValidationData crossData = cross.getData(i);
                                log.debug("Training classifier data set " + (i+1) + "/"+numFolds);
                                trainingMessage.setText(DEFAULT_TRAINING_MESSAGE + " (fold "+ (i+1) + "/"+numFolds +")");

                                svm = new SupportVectorMachine(crossData.getTrainingCaseData(), crossData.getTrainingControlData(),
                                        getSelectedKernel(), 0.1f);
                                svm.setTrainingProgressListener((TrainingProgressListener) workerParent);
                                // Non-SMO
                                // svm.buildSupportVectors(1000, 1e-6);
                                // SMO
                                svm.buildSupportVectorsSMO(((Number) C.getValue()).floatValue(), ((Number) epsilon.getValue()).floatValue());
                                log.debug("Classifier training complete.");

                                int numInClass1 = 0;
                                for (float[] values : crossData.getTestCaseData()) {
                                    if (svm.evaluate(values)) {
                                        numInClass1++;
                                    }
                                }
                                numTruePositives += numInClass1;
                                numFalseNegatives += (crossData.getTestCaseData().size()-numInClass1);

                                numInClass1 = 0;
                                for (float[] values : crossData.getTestControlData()) {
                                    if (svm.evaluate(values)) {
                                        numInClass1++;
                                    }
                                }
                                numFalsePositives += numInClass1;
                                numTrueNegatives += (crossData.getTestControlData().size()-numInClass1);
                            }

                            if (!isCancelled()) {
                                log.debug("Results of "+numFolds+" fold analysis: ");
                                log.debug("FP\tFN\tTP\tTN");
                                log.debug(numFalsePositives + "\t" + numFalseNegatives + "\t" + numTruePositives + "\t" + numTrueNegatives);
                            } else {
                                log.debug("Training cancelled.");
                            }

                        } catch (ClassifierException e1) {
                            errorString = e1.getMessage();
                            setTrainingStatus("Error");
                        }

                        return null;
                    }

                    public boolean cancel(boolean mayInterruptIfRunning) {
                        if (svm != null) {
                            svm.setCancelled(true);
                            svm.setTrainingProgressListener(null);
                        }
                        setTrainingStatus("");
                        progressGraph.clearPoints();
                        progressGraph.setDescription("");
                        progressGraph.repaint();
                        return super.cancel(mayInterruptIfRunning);
                    }

                    public void finished() {
                        if (errorString != null) {
                            JOptionPane.showMessageDialog(workerParent, errorString);
                            setTrainingStatus("");
                        } else {
                            trainingMessage.setText(DEFAULT_TRAINING_MESSAGE);
                            truePositives.setText("" + numTruePositives);
                            truePositives.repaint();
                            falsePositives.setText("" + numFalsePositives);
                            falsePositives.repaint();
                            trueNegatives.setText("" + numTrueNegatives);
                            trueNegatives.repaint();
                            falseNegatives.setText("" + numFalseNegatives);
                            falseNegatives.repaint();
                        }
                        progressGraph.clearPoints();
                        progressGraph.repaint();
                    }
                };

                worker.execute();
            }
        });

        cancelTest.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!worker.isDone()) {
                    worker.cancel(true);
                }
            }
        });

        setLayout(new BorderLayout());
        rebuildForm();
    }

    private void setTrainingStatus(String message) {
        trainingMessage.setText(DEFAULT_TRAINING_MESSAGE);
        truePositives.setText(message);
        truePositives.repaint();
        falsePositives.setText(message);
        falsePositives.repaint();
        trueNegatives.setText(message);
        trueNegatives.repaint();
        falseNegatives.setText(message);
        falseNegatives.repaint();
    }

    public void rebuildForm() {
        removeAll();
        checkBoxes.clear();
        classifyCheckboxes.clear();
        FormLayout layout = new FormLayout(
                "right:max(80dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Support Vector Machine Parameters");

        builder.append("Epsilon", epsilon);
        builder.append("Alpha Bound (C)", C);
        builder.append("Kernel Function", kernelFunctionCombo);
        builder.nextLine();

        builder.appendSeparator("Test Classifier Accuracy");
        builder.append("Number of Cross Validation Folds", numberFolds);
        builder.append("", crossTest);
        builder.append(trainingMessage, progressGraph);
        builder.append("", cancelTest);
        builder.nextLine();
        builder.appendSeparator("Cross Validation Results");
        builder.append("True Positives", truePositives);
        builder.append("False Positives", falsePositives);
        builder.append("True Negatives", trueNegatives);
        builder.append("False Negatives", falseNegatives);

        add(builder.getPanel());
        invalidate();
    }

    public float getEpsilon() {
        return ((Number) epsilon.getValue()).floatValue();
    }

    public float getC() {
        return ((Number) C.getValue()).floatValue();
    }

    public int getNumberFolds() {
        return ((Number) numberFolds.getValue()).intValue();
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

    public void setMaSet(DSMicroarraySet maSet) {
        this.maSet = maSet;
    }

    public void stepUpdate(float value) {
        progressGraph.addPoint((int) value);
        progressGraph.repaint();
    }

    public void stepUpdate(String message, float value) {
        progressGraph.setDescription(message);
        stepUpdate(value);
    }
}

