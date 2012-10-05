package org.geworkbench.components.filtering;

import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
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
 * The parameters panel for the <code>MissingValuesFilter</code>. Prompts
 * the user to enter a number X. Markers whose value is missing in more than X
 * microarrays will be removed.
 * 
 * @author First Genetic Trust
 * @author yc2480
 * @version $Id$
 */
public class MissingValuesFilterPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 1391701528215000956L;
	
    public MissingValuesFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private FilterOptionPanel filterOptionPanel = new FilterOptionPanel("arrays with missing values");

    private void jbInit() throws Exception {
        JPanel container = new JPanel();
        this.setLayout(new FlowLayout());
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.add(filterOptionPanel);
        this.add(container);
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        filterOptionPanel.numberField.addActionListener(parameterActionListener);
        filterOptionPanel.percentField.addActionListener(parameterActionListener);
        filterOptionPanel.numberRemovalButton.addActionListener(parameterActionListener);
        filterOptionPanel.percentRemovalButton.addActionListener(parameterActionListener);
    }

    /**
     * Overrides the method from <code>AbstractSaveableParameterPanel</code>.
     * Provides an error message if the designated number of microarrays is a
     * negative number.
     *
     * @return
     */
    public ParamValidationResults validateParameters() {
   	 String error = filterOptionPanel.validateParameters();
   	 if ( error == null)
           return new ParamValidationResults(true, "No Error");
   	 else   	  	
   		return new ParamValidationResults(false, error);
   	 
   }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("numberThreshold")){
	            this.filterOptionPanel.numberField.setText(value.toString());	           
	            this.filterOptionPanel.numberRemovalButton.setSelected(true);
	            this.revalidate();
			} else if (key.equals("percentThreshold")){
	            this.filterOptionPanel.percentField.setText(value.toString());	            
	            this.filterOptionPanel.percentRemovalButton.setSelected(true);
	            this.revalidate();
			}
		}
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
		return parameters;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getDataSetHistory() {
		String histStr = "Missing Values Filter parameter:\n";
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
		return histStr;
	}

	FilterOptionPanel getFilterOptionPanel() {
		return filterOptionPanel;
	}
}
