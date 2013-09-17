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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

/**
 * The parameters panel for the <code>ExpressionThresholdFilter</code> filter.
 * @author unknown, yc2480
 * @version $Id$
 */
public class ExpressionThresholdFilterPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 5725991435412934513L;
	
	final String INSIDE_RANGE = "Inside of range";
    final String OUTSIDE_RANGE = "Outside of range";
    private JLabel rangeMinLabel = new JLabel("Range Min");
    private JLabel rangeMaxLabel = new JLabel("Range Max");
    private JLabel rangeOptionLabel = new JLabel("Filter-out values");
    private JTextField rangeMinValue = new JTextField("0");
    private JTextField rangeMaxValue = new JTextField("0");
    private JComboBox optionSelection = new JComboBox(new String[]{INSIDE_RANGE, OUTSIDE_RANGE});
    private GridLayout gridLayout1 = new GridLayout();

    private ParameterActionListener parameterActionListener = null;

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
	            this.rangeMinValue.setText(value.toString());
			} else if (key.equals("rangeMax")){
				this.rangeMaxValue.setText(value.toString());
			} else if (key.equals("isInside")){
				this.optionSelection.setSelectedIndex((Boolean)value ? 0 : 1);
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
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		if (this.filterOptionPanel.numberRemovalButton.isSelected())
			parameters.put("numberThreshold", (Integer) this.filterOptionPanel.getNumberThreshold());
		else
			parameters.put("percentThreshold", (Double) this.filterOptionPanel.getPercentThreshold()*100);
		parameters.put("rangeMin", getLowerBound());
		parameters.put("rangeMax", getUpperBound());
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

    private FilterOptionPanel filterOptionPanel = new FilterOptionPanel();
    
    private void jbInit() throws Exception {
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
        
        parameterActionListener = new ParameterActionListener(this);
        rangeMinValue.addActionListener(parameterActionListener);
        rangeMaxValue.addActionListener(parameterActionListener);
        optionSelection.addActionListener(parameterActionListener);
        filterOptionPanel.numberField.addActionListener(parameterActionListener);
        filterOptionPanel.percentField.addActionListener(parameterActionListener);
        filterOptionPanel.numberRemovalButton.addActionListener(parameterActionListener);
        filterOptionPanel.percentRemovalButton.addActionListener(parameterActionListener);
    }

    /**
     * Get the user-specifed lower bound for the expression value of a marker so
     * that the marker does not get filtered out.
     *
     * @return
     */
    public Double getLowerBound() {
    	try {
			return (new Double(rangeMinValue.getText()));
		} catch (Exception ex) {
			 
			return null;
		}
    	 
    }

    /**
     * Get the user-specifed upper bound for the expression value of a marker so
     * that the marker does not get filtered out.
     *
     * @return
     */
    public Double getUpperBound() {        
        try {
			return (new Double(rangeMaxValue.getText()));
		} catch (Exception ex) {
			 
			return null;
		}
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
    	 String error = filterOptionPanel.validateParameters();
    	 if ( error != null)
    		 return new ParamValidationResults(false, error);
    	 else if (getLowerBound() == null)
    		 return new ParamValidationResults(false, "Lower bound must be a number.");
    	 else if (getUpperBound() == null)
    		 return new ParamValidationResults(false, "Upper bound must be a number.");
    	 else if (getUpperBound() < getLowerBound())    	 
            return new ParamValidationResults(false, "Upper bound must be larger than lower bound.");
         else return new ParamValidationResults(true, "No Error");
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDataSetHistory() {
		String histStr = "Expression Threshold Filter:\n";
		
		Map<Serializable, Serializable>parameters = null;
		parameters = getParameters();
		
		if(this.filterOptionPanel.numberRemovalButton.isSelected()){
			histStr += "number threshold of missing arrays: ";
			histStr += parameters.get("numberThreshold");			
		}else{
			histStr += "percentage threshold of missing arrays: ";
			histStr += parameters.get("percentThreshold");
		}	
		histStr += "\n";
		histStr += "Range Min: ";
		histStr += parameters.get("rangeMin");
		histStr += "\nRange Max: ";
		histStr += parameters.get("rangeMax");
		histStr += "\nFilter Values: ";

		Boolean isInside = (Boolean)parameters.get("isInside");
		if ( isInside.booleanValue() ){
			histStr += "Inside Range";
		}
		else{
			histStr += "Outside Range";
		}
		histStr += "\n";		
		return histStr;
	}

	FilterOptionPanel getFilterOptionPanel() {
		return filterOptionPanel;
	}
	
}
