package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.engine.model.analysis.ParamValidationResults;

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
 * The parameters panel for the <code>GenepixExpressionThresholdFilter</code>
 * filter. Collects ranges bounds for the values of the green channel (Cy3)
 * and the red channel (Cy5). Individual marker values from a Genepix array
 * will be compared against these ranges and filtered appropriatelly.
 */
public class GenepixExpressionThresholdFilterPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String INSIDE_RANGE = "Inside range";
    final String OUTSIDE_RANGE = "Outside range";
    private JLabel Cy3MinLabel = new JLabel("Cy3 Range Min");
    private JLabel Cy3MaxLabel = new JLabel("Cy3 Range Max");
    private JLabel Cy5MinLabel = new JLabel("Cy5 Range Min");
    private JLabel Cy5MaxLabel = new JLabel("Cy5 Range Max");
    private JLabel optionLabel = new JLabel("Filter values");
    private JFormattedTextField Cy3MinValue = new JFormattedTextField();
    private JFormattedTextField Cy3MaxValue = new JFormattedTextField();
    private JFormattedTextField Cy5MinValue = new JFormattedTextField();
    private JFormattedTextField Cy5MaxValue = new JFormattedTextField();
    private JComboBox optionSelection = new JComboBox(new String[]{INSIDE_RANGE, OUTSIDE_RANGE});
    private GridLayout gridLayout1 = new GridLayout();

    public GenepixExpressionThresholdFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        this.setLayout(new FlowLayout());
        JPanel container = new JPanel();
        gridLayout1.setColumns(4);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        container.add(Cy3MinLabel);
        container.add(Cy3MinValue);
        container.add(Cy3MaxLabel);
        container.add(Cy3MaxValue);
        container.add(Cy5MinLabel);
        container.add(Cy5MinValue);
        container.add(Cy5MaxLabel);
        container.add(Cy5MaxValue);
        container.add(optionLabel);
        container.add(optionSelection);
        container.setPreferredSize(new Dimension(400, 80));
        this.add(container);
        Cy3MinValue.setValue(new Double(0.0));
        Cy3MinValue.setFocusLostBehavior(JFormattedTextField.COMMIT);
        Cy3MaxValue.setValue(new Double(0.0));
        Cy3MaxValue.setFocusLostBehavior(JFormattedTextField.COMMIT);
        Cy5MinValue.setValue(new Double(0.0));
        Cy5MinValue.setFocusLostBehavior(JFormattedTextField.COMMIT);
        Cy5MaxValue.setValue(new Double(0.0));
        Cy5MaxValue.setFocusLostBehavior(JFormattedTextField.COMMIT);
    }

    /**
     * Get the desired user-specifed min value for the green channel.
     *
     * @return
     */
    public double getCy3Min() {
        return ((Number) Cy3MinValue.getValue()).doubleValue();
    }

    /**
     * Get the desired user-specifed max value for the green channel.
     *
     * @return
     */
    public double getCy3Max() {
        return ((Number) Cy3MaxValue.getValue()).doubleValue();
    }

    /**
     * Get the desired user-specifed min value for the red channel.
     *
     * @return
     */
    public double getCy5Min() {
        return ((Number) Cy5MinValue.getValue()).doubleValue();
    }

    /**
     * Get the desired user-specifed max value for the red channel.
     *
     * @return
     */
    public double getCy5Max() {
        return ((Number) Cy5MaxValue.getValue()).doubleValue();
    }

    /**
     * The user-specified parameter that designates how marker channel values
     * should filtered. There are two options:  channel values will be
     * filtered if they are (1) *INSIDE* the designated range, or
     * (2) *OUTSIDE* the desiganted range.
     *
     * @return
     */
    public int getRangeOption() {
        if (optionSelection.getSelectedItem().equals(INSIDE_RANGE))
            return GenepixExpressionThresholdFilter.INSIDE_RANGE;
        else
            return GenepixExpressionThresholdFilter.OUTSIDE_RANGE;
    }

    /**
     * Overrides the method from <code>AbstractSaveableParameterPanel</code>.
     * Provides an error message if the user provided parameter values are
     * outside their permitted ranges.
     *
     * @return
     */
    public ParamValidationResults validateParameters() {
        if (getCy3Min() > getCy3Max())
            return new ParamValidationResults(false, "Invalid range for Cy3 channel.");
        else if (getCy5Min() > getCy5Max())
            return new ParamValidationResults(false, "Invalid range for Cy5 channel.");
        else
            return new ParamValidationResults(true, "No Error");
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}