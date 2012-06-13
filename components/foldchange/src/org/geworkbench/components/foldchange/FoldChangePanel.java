package org.geworkbench.components.foldchange;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author zm2165 
 * @version $Id$
 */

public class FoldChangePanel extends AbstractSaveableParameterPanel {
	
	private static final long serialVersionUID = -6317805205581289163L;
	
	private JPanel selectionPanel = null;
    private JTextField alpha = new JTextField(5);
    private static final double ALPHA_DEFAULT = 2.0f;
    
    private ButtonGroup group1 = new ButtonGroup();
    private ButtonGroup group2 = new ButtonGroup();
    private JRadioButton linearBtn=new JRadioButton();
    private JRadioButton logBtn=new JRadioButton();
    private JRadioButton ratioBtn=new JRadioButton();
    private JRadioButton diffBtn=new JRadioButton();
    private JLabel noteTitle=new JLabel();
    private JLabel aNote=new JLabel();
    
    private ParameterActionListener parameterActionListener;
    
	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
       
    	if (getStopNotifyAnalysisPanelTemporaryFlag() == true)
			return;
		stopNotifyAnalysisPanelTemporary(true);	
	
    	Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			if (key.equals("alpha")){
				alpha.setText((String)value);
			}
			if (key.equals("linearBtn")){
				linearBtn.setSelected((Boolean)value);
			}
			if (key.equals("logBtn")){
				logBtn.setSelected((Boolean)value);
			}
			if (key.equals("ratioBtn")){
				ratioBtn.setSelected((Boolean)value);
			}
			if (key.equals("diffBtn")){
				diffBtn.setSelected((Boolean)value);
			}		 
			
			ratioBtnActionPerformed();
			
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
		parameters.put("alpha", alpha.getText());
		parameters.put("linearBtn", linearBtn.isSelected());
		parameters.put("logBtn", logBtn.isSelected());
		parameters.put("ratioBtn", ratioBtn.isSelected());
		parameters.put("diffBtn", diffBtn.isSelected());
		 
		return parameters;
	}

    
    public FoldChangePanel() {
    	linearBtn.setText("Linear");    	
    	logBtn.setText("Log2-transformed");    	
    	group1.add(linearBtn);
    	group1.add(logBtn);
    	logBtn.setSelected(false);
    	linearBtn.setSelected(true);
    	ratioBtn.setText("Ratio");    	
    	diffBtn.setText("Difference of average log2 values");    	
    	group2.add(ratioBtn);
    	group2.add(diffBtn);
    	diffBtn.setSelected(false);
    	ratioBtn.setSelected(true);
    	
    	this.setLayout(new BorderLayout());		
		selectionPanel = new JPanel();
		selectionPanel.setLayout(new BorderLayout());
		this.add(selectionPanel, BorderLayout.CENTER);	 
		
		FormLayout layout = new FormLayout(
				"right:max(10dlu;pref), 3dlu, pref, 7dlu, "
						+ "left:max(10dlu;pref), 3dlu, pref, 7dlu, "
						+ "right:max(10dlu;pref), 3dlu, pref, 7dlu ", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Fold Change Parameters");	
		builder.append("Criterion");		
		alpha.setText(Double.toString(ALPHA_DEFAULT));
		alpha.setToolTipText("The value is given in linear measure, regardless of the state of the data.");
		builder.append(alpha);
		builder.nextLine();
		builder.append(new JLabel("Input Data Format: "));
		builder.append(linearBtn);
		builder.append(logBtn);
		builder.nextLine();
		builder.append(new JLabel("Calculation Method: "));
		builder.append(ratioBtn);
		builder.append(diffBtn);
		builder.nextLine();
		builder.append(new JLabel(" "));
		builder.nextLine();
		builder.append(" ",noteTitle,aNote);
		noteTitle.setVisible(false);
		aNote.setVisible(false);
       
		selectionPanel.add(builder.getPanel(), BorderLayout.CENTER);
		
		linearBtn.addActionListener( new BtnActionListener());
		logBtn.addActionListener( new BtnActionListener());
		ratioBtn.addActionListener( new BtnActionListener());
		diffBtn.addActionListener( new BtnActionListener());
		
		parameterActionListener = new ParameterActionListener(this);
		
		alpha.addActionListener(parameterActionListener);
		alpha.addFocusListener(parameterActionListener);
		linearBtn.addActionListener(parameterActionListener);
		logBtn.addActionListener(parameterActionListener);
		ratioBtn.addActionListener(parameterActionListener);
		diffBtn.addActionListener(parameterActionListener); 
		
       
    }
    
    private class BtnActionListener implements
	java.awt.event.ActionListener {
		public void actionPerformed(ActionEvent e) {
			ratioBtnActionPerformed();
		}
	}
    
    private void ratioBtnActionPerformed() {
		if(isLinear()&&!isRatio()) {
			noteTitle.setText("Please note:");
			aNote.setText("Log2 of values will be taken.            ");
			noteTitle.setVisible(true);
			aNote.setVisible(true);
		}
		else if(!isLinear()&&isRatio()) {
			noteTitle.setText("Please note:");
			aNote.setText("Antilog2 of values will be taken.");
			noteTitle.setVisible(true);
			aNote.setVisible(true);
		}
		else {
			noteTitle.setVisible(false);
			aNote.setVisible(false);
		}
	}  
    
    
    

    public String getAlpha() {
       return alpha.getText();
    }
    public boolean isLinear() {
        return linearBtn.isSelected();
    }
    public boolean isLog() {
        return logBtn.isSelected();
    }
    public boolean isRatio() {
        return ratioBtn.isSelected();
    }
    public boolean isDiff() {
        return diffBtn.isSelected();
    }
    
   
	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		//parameters.put("linearBtn", true);		
		//parameters.put("ratioBtn", ratioBtn.isSelected());		
		/*
		alpha.setText(Double.toString(ALPHA_DEFAULT));
		linearBtn.setSelected(true);
		ratioBtn.setSelected(true);
		*/
	}
}
