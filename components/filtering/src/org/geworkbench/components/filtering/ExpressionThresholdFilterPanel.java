package org.geworkbench.components.filtering;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * The parameters panel for the <code>ExpressionThresholdFilter</code> filter.
 * @author unknown, yc2480
 * @version $ID$
 */
public class ExpressionThresholdFilterPanel extends AbstractSaveableParameterPanel {
    final String INSIDE_RANGE = "Inside range";
    final String OUTSIDE_RANGE = "Outside range";
    private JLabel rangeMinLabel = new JLabel("Range Min");
    private JLabel rangeMaxLabel = new JLabel("Range Max");
    private JLabel rangeOptionLabel = new JLabel("Filter values");
    private JFormattedTextField rangeMinValue = new JFormattedTextField();
    private JFormattedTextField rangeMaxValue = new JFormattedTextField(NumberFormat.getNumberInstance());
    private JComboBox optionSelection = new JComboBox(new String[]{INSIDE_RANGE, OUTSIDE_RANGE});
    private GridLayout gridLayout1 = new GridLayout();

    ParameterActionListener parameterActionListener = new ParameterActionListener(this);

    /*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if ((getStopNotifyAnalysisPanelTemporaryFlag()==true)&&(parameterActionListener.getCalledFromProgramFlag()==true)) return;
    	stopNotifyAnalysisPanelTemporary(true);
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("rangeMin")){
	            this.rangeMinValue.setValue((Number)value);
			}
			if (key.equals("rangeMax")){
				this.rangeMaxValue.setValue((Number)value);
			}
			if (key.equals("isInside")){
				this.optionSelection.setSelectedIndex((Boolean)value ? 0 : 1);
			}
		}
        stopNotifyAnalysisPanelTemporary(false);
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("rangeMin", (Number) rangeMinValue.getValue());
		parameters.put("rangeMax", (Number) rangeMaxValue.getValue());
		parameters.put("isInside", (optionSelection.getSelectedIndex() == 0));
		return parameters;
	}

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
        rangeMinValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        rangeMaxValue.setValue(new Double(0.0));
        rangeMaxValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        
        rangeMinValue.addPropertyChangeListener(parameterActionListener);
        rangeMaxValue.addPropertyChangeListener(parameterActionListener);
        optionSelection.addActionListener(parameterActionListener);
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

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}
