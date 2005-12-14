package org.geworkbench.components.analysis.classification.svm;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SVMOptimizationPanel extends AbstractSaveableParameterPanel implements Serializable {
    private static final double DEFAULT_P_VALUE = 0.01;

    private JFormattedTextField pValue = new JFormattedTextField();

    private DSMicroarraySet maSet = null;

    private ArrayList<JCheckBox> checkBoxes = new ArrayList<JCheckBox>();
    private ArrayList<JCheckBox> classifyCheckboxes = new ArrayList<JCheckBox>();

    private static class SerializedInstance implements Serializable {

        private Number pValue;

        public SerializedInstance(Number pValue) {
            this.pValue = pValue;
        }

        Object readResolve() throws ObjectStreamException {
            SVMOptimizationPanel panel = new SVMOptimizationPanel();
            panel.pValue.setValue(pValue);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance((Number) pValue.getValue());
    }

    public SVMOptimizationPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        pValue = new JFormattedTextField(DEFAULT_P_VALUE);
        setLayout(new BorderLayout());
        rebuildForm();
    }

    public void rebuildForm() {
        removeAll();
        Set<String> selectedTraining = getTrainingtLabels();
        Set<String> selectedClassify = getClassifyLabels();
        checkBoxes.clear();
        classifyCheckboxes.clear();
        FormLayout layout = new FormLayout(
                "right:max(80dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref), 3dlu, max(70dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Support Vector Machine Parameters");

        builder.append("Critical P-Value", pValue);
        builder.nextLine();

        builder.appendSeparator("Training Classifications");

        if (maSet != null) {
            // Get existing selections, if any
            DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
            DSAnnotationContext context = manager.getCurrentContext(maSet);
            int n = context.getNumberOfLabels();
            for (int i = 0; i < n; i++) {
                String label = context.getLabel(i);
                JCheckBox checkBox = new JCheckBox(label, false);
                if (selectedTraining.contains(label)) {
                    checkBox.setSelected(true);
                }
                if (i % 3 == 0) {
                    builder.append("");
                }
                builder.append(checkBox);
                checkBoxes.add(checkBox);
            }
        }

        builder.nextLine();
        builder.appendSeparator("To Classify");
        builder.nextLine();

        if (maSet != null) {
            // Get existing selections, if any
            DSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();
            DSAnnotationContext context = manager.getCurrentContext(maSet);
            int n = context.getNumberOfLabels();
            for (int i = 0; i < n; i++) {
                String label = context.getLabel(i);
                JCheckBox checkBox = new JCheckBox(label, false);
                if (selectedClassify.contains(label)) {
                    checkBox.setSelected(true);
                }
                if (i % 3 == 0) {
                    builder.append("");
                }
                builder.append(checkBox);
                classifyCheckboxes.add(checkBox);
            }
        }

        add(builder.getPanel());
        invalidate();
    }

    /**
     * Get the cutoff threashold that will be used to bound the array values.
     *
     * @return The cutoff value.
     */
    public double getPValue() {
        return ((Number) pValue.getValue()).doubleValue();
    }

    public Set<String> getTrainingtLabels() {
        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            JCheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isSelected()) {
                result.add(checkBox.getText());
            }
        }
        return result;
    }

    public Set<String> getClassifyLabels() {
        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < classifyCheckboxes.size(); i++) {
            JCheckBox checkBox = classifyCheckboxes.get(i);
            if (checkBox.isSelected()) {
                result.add(checkBox.getText());
            }
        }
        return result;
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
}

