package org.geworkbench.components.normalization;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.Serializable;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Parameters panel used by the <code>ThresholdNormalizer</code>.
 */
public class ThresholdNormalizerPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String MIN_OPTION = "Minimum";
    final String MAX_OPTION = "Maximum";
    final String IGNORE_OPTION = "Ignore";
    final String REPLACE_OPTION = "Replace";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel cutoffLabel = new JLabel("Cut-off value");
    private JLabel minMaxLabel = new JLabel("Cut-off type");
    private JLabel missingValuesLabel = new JLabel("Missing values");
    private JFormattedTextField cutoffEdit = new JFormattedTextField();
    private JComboBox cutoffTypeSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION});
    private JComboBox missingValuesSelection = new JComboBox(new String[]{IGNORE_OPTION, REPLACE_OPTION});

    public ThresholdNormalizerPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        JPanel container = new JPanel();
        this.setLayout(new FlowLayout());
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        container.add(cutoffLabel);
        container.add(cutoffEdit);
        container.add(minMaxLabel);
        container.add(cutoffTypeSelection);
        container.add(missingValuesLabel);
        container.add(missingValuesSelection);
        container.setPreferredSize(new Dimension(220, 80));
        this.add(container);
        cutoffEdit.setValue(new Double(0.0));
        cutoffEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
    }

    /**
     * Get the cutoff threashold that will be used to bound the array values.
     *
     * @return The cutoff value.
     */
    public double getCutoffValue() {
        return ((Number) cutoffEdit.getValue()).doubleValue();
    }

    /**
     * The user-specified parameter that designates if the cutoff value is
     * expected to be the minimum or the maximum value for the microarray set
     * values.
     *
     * @return <code>MINIMUM</code> or <code>MAXIMUM</code>.
     */
    public int getCutoffType() {
        if (cutoffTypeSelection.getSelectedItem().equals(MIN_OPTION))
            return ThresholdNormalizer.MINIMUM;
        else
            return ThresholdNormalizer.MAXIMUM;
    }

    /**
     * The user-specified parameter that designates how the missing values
     * should be treated by this normalizer (should they be ignored or set to
     * the prescribed cutoff).
     *
     * @return <code>IGNORE</code> or <code>REPLACE</code>.
     */
    public int getMissingValueTreatment() {
        if (missingValuesSelection.getSelectedItem().equals(IGNORE_OPTION))
            return ThresholdNormalizer.IGNORE;
        else
            return ThresholdNormalizer.REPLACE;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}

