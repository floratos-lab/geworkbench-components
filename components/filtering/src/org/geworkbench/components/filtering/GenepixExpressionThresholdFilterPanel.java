package org.geworkbench.components.filtering;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
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
 * 
 * The parameters panel for the <code>GenepixExpressionThresholdFilter</code>
 * filter. Collects ranges bounds for the values of the green channel (Cy3)
 * and the red channel (Cy5). Individual marker values from a Genepix array
 * will be compared against these ranges and filtered appropriatelly.
 *
 * @author First Genetic Trust, yc2480
 * @version $Id$
 */
public class GenepixExpressionThresholdFilterPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = -3835388346988546797L;
	
	final String INSIDE_RANGE = "Inside of range";
    final String OUTSIDE_RANGE = "Outside of range";
    private JLabel Cy3MinLabel = new JLabel("Cy3 Range Min");
    private JLabel Cy3MaxLabel = new JLabel("Cy3 Range Max");
    private JLabel Cy5MinLabel = new JLabel("Cy5 Range Min");
    private JLabel Cy5MaxLabel = new JLabel("Cy5 Range Max");
    private JLabel optionLabel = new JLabel("Filter-out values");
    private JFormattedTextField Cy3MinValue = new JFormattedTextField();
    private JFormattedTextField Cy3MaxValue = new JFormattedTextField();
    private JFormattedTextField Cy5MinValue = new JFormattedTextField();
    private JFormattedTextField Cy5MaxValue = new JFormattedTextField();
    private JComboBox optionSelection = new JComboBox(new String[]{INSIDE_RANGE, OUTSIDE_RANGE});
    private GridLayout gridLayout1 = new GridLayout();
    private ParameterActionListener parameterActionListener = null;

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    @Override
	public void setParameters(Map<Serializable, Serializable> parameters){
    	if ((getStopNotifyAnalysisPanelTemporaryFlag()==true)&&(parameterActionListener.getCalledFromProgramFlag()==true)) return;
    	stopNotifyAnalysisPanelTemporary(true);
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("Cy3MinValue")){
	            this.Cy3MinValue.setValue((Double)value);
			} else if (key.equals("Cy3MaxValue")){
				this.Cy3MaxValue.setValue((Double)value);
			} else if (key.equals("Cy5MinValue")){
				this.Cy5MinValue.setValue((Double)value);
			} else if (key.equals("Cy5MaxValue")){
				this.Cy5MaxValue.setValue((Double)value);
			} else if (key.equals("optionSelection")){
				this.optionSelection.setSelectedIndex((Integer)value);
			} else if (key.equals("numberThreshold")){
	            this.filterOptionPanel.numberField.setText(value.toString());	           
	            this.filterOptionPanel.numberRemovalButton.setSelected(true);
	            this.revalidate();
			} else if (key.equals("percentThreshold")){
	            this.filterOptionPanel.percentField.setText(value.toString());	            
	            this.filterOptionPanel.percentRemovalButton.setSelected(true);
	            this.revalidate();
			}
		}
        stopNotifyAnalysisPanelTemporary(false);
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 *      Since HierClustPanel only has three parameters, we return metric,
	 *      dimension and method in the format same as getBisonParameters().
	 */
    @Override
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		if (this.filterOptionPanel.numberRemovalButton.isSelected())
			parameters.put("numberThreshold", (Integer) this.filterOptionPanel.getNumberThreshold());
		else
			parameters.put("percentThreshold", (Double) this.filterOptionPanel.getPercentThreshold()*100);
		parameters.put("Cy3MinValue", (Double)Cy3MinValue.getValue());
		parameters.put("Cy3MaxValue", (Double)Cy3MaxValue.getValue());
		parameters.put("Cy5MinValue", (Double)Cy5MinValue.getValue());
		parameters.put("Cy5MaxValue", (Double)Cy5MaxValue.getValue());
		parameters.put("optionSelection", optionSelection.getSelectedIndex());
		return parameters;
	}

    public GenepixExpressionThresholdFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private FilterOptionPanel filterOptionPanel = new FilterOptionPanel();

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
        
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(filterOptionPanel);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(container);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel wrapperPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS);
        wrapperPanel.setLayout(boxLayout);
        wrapperPanel.add(topPanel);
        wrapperPanel.add(bottomPanel);
        this.add(wrapperPanel);

        Cy3MinValue.setValue(new Double(0.0));
        Cy3MinValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        Cy3MaxValue.setValue(new Double(0.0));
        Cy3MaxValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        Cy5MinValue.setValue(new Double(0.0));
        Cy5MinValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        Cy5MaxValue.setValue(new Double(0.0));
        Cy5MaxValue.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        parameterActionListener = new ParameterActionListener(this);
        Cy3MinValue.addPropertyChangeListener(parameterActionListener);
        Cy3MaxValue.addPropertyChangeListener(parameterActionListener);
        Cy5MinValue.addPropertyChangeListener(parameterActionListener);
        Cy5MaxValue.addPropertyChangeListener(parameterActionListener);
        optionSelection.addActionListener(parameterActionListener);
        filterOptionPanel.numberField.addActionListener(parameterActionListener);
        filterOptionPanel.percentField.addActionListener(parameterActionListener);
        filterOptionPanel.numberRemovalButton.addActionListener(parameterActionListener);
        filterOptionPanel.percentRemovalButton.addActionListener(parameterActionListener);
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
    @Override
	public ParamValidationResults validateParameters() {
    	String error = filterOptionPanel.validateParameters();
    	if ( error != null)
    		 return new ParamValidationResults(false, error);
    	else if (getCy3Min() > getCy3Max())
            return new ParamValidationResults(false, "Invalid range for Cy3 channel.");
        else if (getCy5Min() > getCy5Max())
            return new ParamValidationResults(false, "Invalid range for Cy5 channel.");
        else
            return new ParamValidationResults(true, "No Error");
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDataSetHistory() {		
		Map<Serializable, Serializable>parameters = null;
		parameters = getParameters();
		GenepixExpressionThresholdFilterPanel genepixExpressionThresholdFilterPanel = this;
		String histStr = "";
		// Header
		histStr += "GenepixExpressionThresholdFilter parameters:\n";		
		if(this.filterOptionPanel.numberRemovalButton.isSelected()){
			histStr += "number threshold of missing arrays: ";
			histStr += parameters.get("numberThreshold");			
		}else{
			histStr += "percentage threshold of missing arrays: ";
			histStr += parameters.get("percentThreshold");
		}	
		histStr += "\n";
		// Cy3 Range Min
		histStr += "Cy3 Range Min: ";
		histStr += genepixExpressionThresholdFilterPanel.getCy3Min() + "\n";

		// Cy3 Range Max
		histStr += "Cy3 Range Max: ";
		histStr += genepixExpressionThresholdFilterPanel.getCy3Max() + "\n";

		// Cy5 Range Min
		histStr += "Cy5 Range Min: ";
		histStr += genepixExpressionThresholdFilterPanel.getCy5Min() + "\n";

		// Cy5 Range Max
		histStr += "Cy5 Range Max: ";
		histStr += genepixExpressionThresholdFilterPanel.getCy5Max() + "\n";

		// Filter values
		histStr += "Filter values: ";
		histStr += genepixExpressionThresholdFilterPanel.optionSelection.getSelectedItem() + "\n";

		return histStr;
	}

	FilterOptionPanel getFilterOptionPanel() {
		return filterOptionPanel;
	}
	
}
