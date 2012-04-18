package org.geworkbench.components.sam;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author zm2165 
 * @version $Id$
 */

public class SAMPanel extends AbstractSaveableParameterPanel {	

	private static final long serialVersionUID = -405631194646019182L;
	private JPanel selectionPanel = null;
    private JTextField deltaInc = new JTextField(5);
    private JTextField deltaMax = new JTextField(5);
    private static final float DELTAINC_DEFAULT = 0.3f;
    private static final float DELTAMAX_DEFAULT = 5.0f;
    private static final String MAX_PERM="4000";
    private static final String INIT_PERM="100";
    
    private JCheckBox log2CheckBox=new JCheckBox();
    private ButtonGroup group1 = new ButtonGroup();   
    private JRadioButton maxPerm=new JRadioButton();
    private JRadioButton specifyPerm=new JRadioButton();
    private JTextField permText = new JTextField(5);
    
   // private JLabel noteTitle=new JLabel();
   // private JLabel aNote=new JLabel();
    
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

			if (key.equals("deltaInc")){
				deltaInc.setText((String)value);
			}
			if (key.equals("deltaMax")){
				deltaMax.setText((String)value);
			}
			if (key.equals("log2CheckBox")){
				log2CheckBox.setSelected((Boolean)value);
			}
			if (key.equals("maxPerm")){
				maxPerm.setSelected((Boolean)value);
			}
			if (key.equals("specifyPerm")){
				specifyPerm.setSelected((Boolean)value);
			}
			if (key.equals("permText")){
				permText.setText((String)value);
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
		parameters.put("deltaInc", deltaInc.getText());
		parameters.put("deltaMax", deltaMax.getText());
		parameters.put("log2CheckBox", log2CheckBox.isSelected());
		parameters.put("maxPerm", maxPerm.isSelected());
		parameters.put("specifyPerm", specifyPerm.isSelected());		
		parameters.put("permText", permText.getText());
		
		return parameters;
	}

    
    public SAMPanel() {
    	log2CheckBox.setSelected(false);
    	permText.setText(INIT_PERM);
    	maxPerm.setText("Maximum allowable - "+MAX_PERM);    	
    	specifyPerm.setText("Specify");
    	group1.add(maxPerm);
    	group1.add(specifyPerm);
    	specifyPerm.setSelected(true);
    	maxPerm.setSelected(false);    	
    	
    	this.setLayout(new BorderLayout());		
		selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());
		this.add(selectionPanel, BorderLayout.CENTER);	 
		
		FormLayout layout = new FormLayout(
				"left:max(10dlu;pref), 3dlu, pref, 7dlu, "
						+ "left:max(10dlu;pref), 3dlu, pref, 7dlu, ");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("SAM Analysis Parameters");	
		builder.append("Delta increment");		
		deltaInc.setText(Float.toString(DELTAINC_DEFAULT));
		builder.append(deltaInc);
		builder.nextLine();
		builder.append("Delta max");
		deltaMax.setText(Float.toString(DELTAMAX_DEFAULT));
		builder.append(deltaMax);
		builder.nextLine();
		builder.append("Data log2 transformed");
		builder.append(log2CheckBox);
		builder.nextLine();
				
		builder.appendSeparator("Number of label permutations");
		builder.nextLine();
		builder.append(maxPerm);
		builder.nextLine();
		builder.append(specifyPerm);
		builder.append(permText);
       
		selectionPanel.add(builder.getPanel(), BorderLayout.CENTER);
		
		maxPerm.addActionListener( new BtnActionListener());
		specifyPerm.addActionListener( new BtnActionListener());		
       
    }
    
    private class BtnActionListener implements
	java.awt.event.ActionListener {
		
		public void actionPerformed(ActionEvent e) {
			if(specifyPerm.isSelected()) {
				permText.setEnabled(true);
			}
			else{
				permText.setEnabled(false);
			}
		}
	}

    @Override
	public String getDataSetHistory() {
    	String histStr = "";
		// Header
		histStr += "SAM Analysis with the following parameters:\n";
		histStr += "----------------------------------------------------------\n";

		histStr += "Input Parameters:" + "\n";			
		histStr += "\t" + "Delta increment: " + this.getDeltaInc() + "\n";
		histStr += "\t" + "Delta max: " + this.getDeltaMax() + "\n";
		histStr += "\t" + "Data log2 transformed: " + this.needUnLog() + "\n";
		histStr += "\t" + "Number of permutations: " + this.getPermutation() + "\n";	
		return histStr.toString();
	}
    
    
    public String getDeltaInc() {
       return deltaInc.getText();
    }
    public String getDeltaMax() {
        return deltaMax.getText();
     }
    
    public boolean needUnLog() {
        return log2CheckBox.isSelected();
    }
    
    public String getPermutation() {
        String s=MAX_PERM;
        if(specifyPerm.isSelected())
        	s=permText.getText();
        return s;
     }  
    
   
	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
}
