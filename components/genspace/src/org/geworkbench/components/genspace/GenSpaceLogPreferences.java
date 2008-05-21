package org.geworkbench.components.genspace;

import javax.swing.*;
import org.geworkbench.engine.config.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.geworkbench.engine.properties.PropertiesManager;

public class GenSpaceLogPreferences extends JPanel implements VisualPlugin, ActionListener {

	JRadioButton log, logAnon, noLog;
	ButtonGroup group;
	JPanel radioPanel, saveReset;
	JButton save, reset;
	static final String PROPERTY_KEY = "genSpace_logging_preferences"; // the key in the properties file
	
	public GenSpaceLogPreferences()
	{	
		// read the preferences from the properties file
        try 
        {
        	PropertiesManager properties = PropertiesManager.getInstance();
        	String pref = properties.getProperty(GenSpaceLogPreferences.class, PROPERTY_KEY, null);
        	if (pref == null)
        	{
        		// if the preferences are not set, then show the pop up window
        		
        		// ideally this should also be in the properties file
        		String message = "geWorkbench now includes a component called genSpace,\n" +
        				"which will provide social networking capabilities and allow\n" +
        				"you to connect with other geWorkbench users.\n\n" +
        				"In order for it to be effective, genSpace must log which analysis\n" +
        				"tools you use during your geWorkbench session.\n\n" +
        				"Please go to the genSpace Logging Preference window to configure \n" +
        				"your preference. You can later change it at any time.";
        		String title = "Please set your genSpace logging preferences.";
        		JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
        		
        		// set the default to "log anonymously"
        		pref = "1";
        		// write it to the properties file
    			properties.setProperty(GenSpaceLogPreferences.class, PROPERTY_KEY, pref);
        	}

       		// set the logging level
       		ObjectHandler.setLogStatus(Integer.parseInt(pref));
        } 
        catch (Exception e) { }		

		
		initComponents();
	}

	
	private void initComponents()
	{	
		
		log = new JRadioButton("Log my analysis events");
		log.setActionCommand("0");
		logAnon = new JRadioButton("Log my analysis events anonymously", true);
		logAnon.setActionCommand("1");
		noLog = new JRadioButton("Do not log my analysis events");
		noLog.setActionCommand("2");
		
		group = new ButtonGroup();
		
		save = new JButton("Save");
		reset = new JButton("Reset");
		
		group.add(log);
		group.add(logAnon);
		group.add(noLog);
		
		save.addActionListener(this);
		reset.addActionListener(this);
		
		radioPanel = new JPanel(new GridLayout(0, 1));
        radioPanel.add(log);
        radioPanel.add(logAnon);
        radioPanel.add(noLog);
        
        saveReset = new JPanel(new GridLayout(0, 2));
        saveReset.add(save);
        saveReset.add(reset);
        radioPanel.add(saveReset);
        
        add(radioPanel);
	}
	
	
    public void actionPerformed(ActionEvent e) 
    {
    	if (e.getSource() == save) {
    		//System.out.println("Save pressed with " + group.getSelection().getActionCommand());
    		ObjectHandler.setLogStatus(Integer.parseInt(group.getSelection().getActionCommand()));
    		// write it to the properties file
    		try
    		{
    			PropertiesManager properties = PropertiesManager.getInstance();
    			properties.setProperty(GenSpaceLogPreferences.class, PROPERTY_KEY, group.getSelection().getActionCommand());
    		}
    		catch (Exception ex) { }
    		
    	}
    	else if (e.getSource() == reset) {
    		//System.out.println("Reset pressed");
    		logAnon.setSelected(true);
    	}
    }
    
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }
}
