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
 * The parameters panel for the <code>ExpressionThresholdFilter</code> filter.
 */
public class ExpressionThresholdFilterPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String INSIDE_RANGE = "Inside range";
    final String OUTSIDE_RANGE = "Outside range";
    private JLabel rangeMinLabel = new JLabel("Range Min");
    private JLabel rangeMaxLabel = new JLabel("Range Max");
    private JLabel rangeOptionLabel = new JLabel("Filter values");
    private JFormattedTextField rangeMinValue = new JFormattedTextField();
    private JFormattedTextField rangeMaxValue = new JFormattedTextField();
    private JComboBox optionSelection = new JComboBox(new String[]{INSIDE_RANGE, OUTSIDE_RANGE});
    private GridLayout gridLayout1 = new GridLayout();

    public ExpressionThresholdFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        this.setLayout(new FlowLayout());
        JPanel container = new JPanel();
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(3);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        container.add(rangeMinLabel);
        container.add(rangeMinValue);
        container.add(rangeMaxLabel);
        container.add(rangeMaxValue);
        container.add(rangeOptionLabel);
        container.add(optionSelection);
        container.setPreferredSize(new Dimension(250, 80));
        this.add(container);
        rangeMinValue.setValue(new Double(0.0));
        rangeMinValue.setFocusLostBehavior(JFormattedTextField.COMMIT);
        rangeMaxValue.setValue(new Double(0.0));
        rangeMaxValue.setFocusLostBehavior(JFormattedTextField.COMMIT);
    }

    /**
     * Get the user-specifed lower bound for the expression value of a marker so
     * that the marker does not get filtered out.
     *
     * @return
     */
    public double getLowerBound() {
        return ((Number) rangeMinValue.getValue()).doubleValue();
    }

    /**
     * Get the user-specifed upper bound for the expression value of a marker so
     * that the marker does not get filtered out.
     *
     * @return
     */
    public double getUpperBound() {
        return ((Number) rangeMaxValue.getValue()).doubleValue();
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
            return ExpressionThresholdFilter.INSIDE_RANGE;
        else
            return ExpressionThresholdFilter.OUTSIDE_RANGE;
    }

    /**
     * Overrides the method from <code>AbstractSaveableParameterPanel</code>.
     * Provides an error message if the user provided parameter values are
     * outside their permitted ranges.
     *
     * @return
     */
    public ParamValidationResults validateParameters() {
        if (getUpperBound() < getLowerBound())
            return new ParamValidationResults(false, "Upper bound must be larger than lower bound.");
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