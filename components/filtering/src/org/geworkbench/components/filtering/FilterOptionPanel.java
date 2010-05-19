/**
 * 
 */
package org.geworkbench.components.filtering;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author zji
 * @version $Id$
 *
 */
public class FilterOptionPanel extends JPanel {
	private static Log log = LogFactory.getLog(FilterOptionPanel.class);
	enum Option { PERCENT_REMOVAL, NUMBER_REMOVAL };

	private static final long serialVersionUID = -580698616635439456L;
	JRadioButton percentRemovalButton;
	JRadioButton numberRemovalButton;
	
	JFormattedTextField percentField = null;
	JFormattedTextField numberField = null;
	
	FilterOptionPanel() {
		super(new GridBagLayout());
		
		percentRemovalButton = new JRadioButton("Remove the marker if the percentage of matching arrays is more than");
		numberRemovalButton = new JRadioButton("Remove the marker if the number of matching arrays is more than");
		
		percentField = new JFormattedTextField();
		percentField.setValue(40.0);
		percentField.setColumns(5);
		numberField = new JFormattedTextField();
		numberField.setValue(new Integer(0));
		numberField.setColumns(5);

		GridBagConstraints c = new GridBagConstraints();
		add(percentRemovalButton);
		add(percentField);
		add(new JLabel("%"));
		c.gridy = 1;
		c.anchor = GridBagConstraints.LINE_START;
		add(numberRemovalButton, c);
		add(numberField, c);
		c.gridy = 2;
		c.anchor = GridBagConstraints.LINE_START;
		
		//Group the radio buttons.
	    ButtonGroup group = new ButtonGroup();
	    group.add(percentRemovalButton);
	    group.add(numberRemovalButton);
	    
	    // default choice 
	    percentRemovalButton.setSelected(true);	    
	    numberField.setEnabled(false);
	    
	    ActionListener actionListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(percentRemovalButton.isSelected()) {
					percentField.setEnabled(true);
					numberField.setEnabled(false);
				} else { 
					percentField.setEnabled(false);
					numberField.setEnabled(true);
				}
			}
	    	
	    };
		percentRemovalButton.addActionListener(actionListener );
	    numberRemovalButton.addActionListener(actionListener);

	    Border border1 = BorderFactory.createEtchedBorder(Color.white,
	            new Color(165, 163, 151));
	    setBorder(new TitledBorder(border1, "Filtering Options"));

	    percentRemovalButton.setSelected(true);
	}
	
	Option getSelectedOption() {
		if(percentRemovalButton.isSelected()) return Option.PERCENT_REMOVAL;
		else if(numberRemovalButton.isSelected()) return Option.NUMBER_REMOVAL;
		else {
			log.error("Invalid selection of filter option");
			return null;
		}
		
	}
	
	int getNumberThreshold() {
		return Integer.parseInt(numberField.getText());
	}

	double getPercentThreshold() {
		return Double.parseDouble(percentField.getText())*0.01;
	}

}
