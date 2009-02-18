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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Pararameters panel for the <code>AffyDetectionCallFilter</code>. Prompts
 * the user to designate which markers (those whose detection call is "Present",
 * "Absent" or "Marginal") should be filtered out.
 * 
 * @author unknown, yc2480
 * @version $ID$
 */
public class AffyDetectionCallFilterPanel extends AbstractSaveableParameterPanel implements ItemListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = -1144320550372709784L;
	
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
			}
			if (key.equals("absent")){
				this.absentButton.setSelected((Boolean)value);
			}
			if (key.equals("marginal")){
				this.marginalButton.setSelected((Boolean)value);
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
        this.add(container);
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        presentButton.addActionListener(parameterActionListener);
        absentButton.addActionListener(parameterActionListener);
        marginalButton.addActionListener(parameterActionListener);
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
}

