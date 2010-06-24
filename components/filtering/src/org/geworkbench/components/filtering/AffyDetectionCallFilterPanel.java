package org.geworkbench.components.filtering;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

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
 * Parameters panel for the <code>AffyDetectionCallFilter</code>. Prompts
 * the user to designate which markers (those whose detection call is "Present",
 * "Absent" or "Marginal") should be filtered out.
 *
 * @author unknown, yc2480
 * @version $ID$
 */
public class AffyDetectionCallFilterPanel extends AbstractSaveableParameterPanel implements ItemListener{
	/**
	 *
	 */
	private static final long serialVersionUID = -1144320550372709784L;

    private static final String NOT_FILTERED = "not filtered";
	private static final String FILTERED = "filtered";

	final String PRESENT_OPTION = "P";
    final String ABSENT_OPTION = "A";
    final String MARGINAL_OPTION = "M";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel callSelectionLabel = new JLabel("<html><p>Detection calls to</p><p>be filtered out.</p></html>");
    private JCheckBox presentButton = new JCheckBox(PRESENT_OPTION);
    private JCheckBox absentButton = new JCheckBox(ABSENT_OPTION);
    private JCheckBox marginalButton = new JCheckBox(MARGINAL_OPTION);
    private boolean presentButtonStatus;
    private boolean absentButtonStatus;
    private boolean marginalButtonStatus;

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

			if (key.equals("present")){
	            this.presentButton.setSelected((Boolean)value);
			} else if (key.equals("absent")){
				this.absentButton.setSelected((Boolean)value);
			} else if (key.equals("marginal")){
				this.marginalButton.setSelected((Boolean)value);
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
			parameters.put("percentThreshold", (Double)this.filterOptionPanel.getPercentThreshold() * 100);
		parameters.put("present", presentButton.isSelected());
		parameters.put("absent", absentButton.isSelected());
		parameters.put("marginal", marginalButton.isSelected());
		return parameters;
	}


    public AffyDetectionCallFilterPanel() {
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
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(1);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        // Initialize the selection status for the check box buttons.
        presentButton.setSelected(false);
        presentButtonStatus = false;
        absentButton.setSelected(false);
        absentButtonStatus = false;
        marginalButton.setSelected(false);
        marginalButtonStatus = false;
        //Put the check boxes in a column in a panel
        JPanel buttonContainer = new JPanel(new GridLayout(0, 1));
        buttonContainer.add(presentButton);
        buttonContainer.add(marginalButton);
        buttonContainer.add(absentButton);
        // Set the button item selection listener.
        presentButton.addItemListener(this);
        absentButton.addItemListener(this);
        marginalButton.addItemListener(this);
        container.add(callSelectionLabel);
        container.add(buttonContainer);
        container.setPreferredSize(new Dimension(250, 55));
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        presentButton.addActionListener(parameterActionListener);
        absentButton.addActionListener(parameterActionListener);
        marginalButton.addActionListener(parameterActionListener);
        filterOptionPanel.numberField.addActionListener(parameterActionListener);
        filterOptionPanel.percentField.addActionListener(parameterActionListener);
        filterOptionPanel.numberRemovalButton.addActionListener(parameterActionListener);
        filterOptionPanel.percentRemovalButton.addActionListener(parameterActionListener);
        
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(filterOptionPanel);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(container);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setMaximumSize(new Dimension(250, 80));
        
        JPanel wrapperPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS);
        wrapperPanel.setLayout(boxLayout);
        wrapperPanel.add(topPanel);
        wrapperPanel.add(bottomPanel);
        this.add(wrapperPanel);

    }
    
    private JRadioButton moreButton;

    boolean removeIfMore() {
    	return moreButton.isSelected();
    }

    /**
     * Check if the "Present" option is selected.
     */
    public boolean isPresentSelected() {
        return presentButtonStatus;
    }

    /**
     * Check if the "Absent" option is selected.
     */
    public boolean isAbsentSelected() {
        return absentButtonStatus;
    }

    /**
     * Check if the "Marginal" option is selected.
     */
    public boolean isMarginalSelected() {
        return marginalButtonStatus;
    }

    /**
     * Listens to the check boxes.
     */
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();
        if (source == presentButton)
            presentButtonStatus = !presentButtonStatus;
        else if (source == absentButton)
            absentButtonStatus = !absentButtonStatus;
        else if (source == marginalButton)
            marginalButtonStatus = !marginalButtonStatus;
    }

    @Override
    public String toString(){
    	return
    	"----------------------------------------\n"+
    	"present: "+isPresentSelected()+"\n"+
        "marginal: "+isMarginalSelected()+"\n"+
        "absent: "+isAbsentSelected()+"\n"+
    	"========================================\n";
    }
    
    @Override
    public ParamValidationResults validateParameters() {
    	 String error = filterOptionPanel.validateParameters();
    	 if ( error == null)
            return new ParamValidationResults(true, "No Error");
    	 else   	  	
    		return new ParamValidationResults(false, error);
    	 
    }


	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}
	
	

	@Override
	public String getDataSetHistory() {
		String histStr = "";
		Map<Serializable, Serializable>parameters = null;
		parameters = getParameters();
		// Header
		histStr += "AffyDetectionCallFilter parameters:\n";
				
		if(this.filterOptionPanel.numberRemovalButton.isSelected()){
			histStr += "number threshold of missing arrays: ";
			histStr += parameters.get("numberThreshold");			
		}else{
			histStr += "percentage threshold of missing arrays: ";
			histStr += parameters.get("percentThreshold");			
		}		
		histStr += "\n";
		// present
		histStr += PRESENT_OPTION + ": ";
		histStr += isPresentSelected()? FILTERED : NOT_FILTERED;
		histStr += "\n";

		// marginal
		histStr += MARGINAL_OPTION + ": ";
		histStr += isMarginalSelected()? FILTERED : NOT_FILTERED;
		histStr += "\n";

		// absent
		histStr += ABSENT_OPTION + ": ";
		histStr += isAbsentSelected()? FILTERED : NOT_FILTERED;
		histStr += "\n";

		return histStr;
	}
	
	public FilterOptionPanel getFilterOptionPanel() {
		return filterOptionPanel;
	}
}
