package org.geworkbench.components.genspace.ui;

import org.geworkbench.components.genspace.bean.DataVisibilityBean;
import org.geworkbench.components.genspace.bean.NetworkVisibilityBean;
import org.geworkbench.components.genspace.ui.LoginManager;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.properties.PropertiesManager;
import org.geworkbench.components.genspace.ObjectHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingworker.*;

/**
 * This class build the panel for setting user's log preferences and data
 * visibility.
 */
public class DataVisibility extends JPanel implements VisualPlugin, ActionListener,
ListSelectionListener {

	static final String PROPERTY_KEY = "genSpace_logging_preferences"; // the key in the properties file

	private JComboBox logPreferences;

	private JComboBox dataVisibilityOptions;

	private JList networks;

	private JButton save;

	private List selectedNetworks = new ArrayList();
	private String username = ""; 
	int preference;

	public DataVisibility() {
//		read the preferences from the properties file
		try 
		{
			PropertiesManager properties = PropertiesManager.getInstance();
			String pref = properties.getProperty(DataVisibility.class, PROPERTY_KEY, null);

			LoginManager lm = new LoginManager();
			username = lm.getLoggedInUser();

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
				properties.setProperty(DataVisibility.class, PROPERTY_KEY, pref);
			}

			// set the logging level
			ObjectHandler.setLogStatus(Integer.parseInt(pref));
			ObjectHandler.setUserName(username);
			preference = Integer.parseInt(pref);
		} 
		catch (Exception e) { }		

		initComponents();
	}

	public DataVisibility(String uName) {
		//System.out.println("Data Visibility Options");
		LoginManager manager = new LoginManager();
		DataVisibilityBean bean = manager.getDataVisibilityBean(uName);
		username = uName;
		if(null == bean)
			initComponents();
		else
			initComponents(bean);
	}
	private void initComponents() {

		logPreferences = new JComboBox();
		logPreferences.addItem("-- Select Log Preferences --");
		logPreferences.addItem("Log My Analysis Events");
		logPreferences.addItem("Log My Analysis Events Anonymously");
		logPreferences.addItem("Do Not Log My Analysis Events");
		add(logPreferences);
		logPreferences.addActionListener(this);

		/*
		dataVisibilityOptions = new JComboBox();
		dataVisibilityOptions.addItem("-- Select Data Visibility Options --");
		dataVisibilityOptions.addItem("Data Not Visible At All");
		dataVisibilityOptions.addItem("Data Visible Within My Network");
		dataVisibilityOptions.addItem("Data Visible In Networks");

		add(dataVisibilityOptions);
		dataVisibilityOptions.addActionListener(this);

		ArrayList<String> allNetworks = getAllNetworks();
		networks = new JList(allNetworks.toArray());
		networks.setVisibleRowCount(3);
		networks
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		networks.setEnabled(false);
		add(new JScrollPane(networks));
		networks.addListSelectionListener(this);
		 */

		save = new JButton("Save");
		add(save);
		save.addActionListener(this);
	}


	private void initComponents(DataVisibilityBean bean) {
		this.setSize(500, 600);

		GridBagLayout gridbag = new GridBagLayout();
		this.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();
		c.ipady = 5;
		JLabel blank = new JLabel(" ");

		logPreferences = new JComboBox();
		logPreferences.addItem("-- Select Log Preferences --");
		logPreferences.addItem("Log My Analysis Events");
		logPreferences.addItem("Log My Analysis Events Anonymously");
		logPreferences.addItem("Do Not Log My Analysis Events");
		int preference = bean.getLogData();
		logPreferences.setSelectedIndex(preference+1);
		
		try {
		PropertiesManager properties = PropertiesManager.getInstance();
		properties.setProperty(DataVisibility.class, PROPERTY_KEY, ""+(logPreferences.getSelectedIndex()-1));
		} catch (Exception e) {}
		
		ObjectHandler.setLogStatus(preference);
		ObjectHandler.setUserName(username);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(logPreferences, c);
		add(logPreferences);
		logPreferences.addActionListener(this);

		c.gridwidth = GridBagConstraints.REMAINDER;		
		gridbag.setConstraints(blank, c);
		add(blank);		


		dataVisibilityOptions = new JComboBox();
		dataVisibilityOptions.addItem("-- Select Data Visibility Options --");
		dataVisibilityOptions.addItem("Data Not Visible At All");
		dataVisibilityOptions.addItem("Data Visible Within My Network");
		dataVisibilityOptions.addItem("Data Visible In Networks");
		/*
		System.out.println("Second one"+bean.getDataVisibility());
		dataVisibilityOptions.setSelectedIndex(bean.getDataVisibility()+1);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(dataVisibilityOptions, c);		
		add(dataVisibilityOptions);
		dataVisibilityOptions.addActionListener(this);

		c.gridwidth = GridBagConstraints.REMAINDER;		
        gridbag.setConstraints(blank, c);
    	add(blank);				

		ArrayList<String> allNetworks = getAllNetworks();
		networks = new JList(allNetworks.toArray());
		networks.setVisibleRowCount(3);
		networks
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if(bean.getDataVisibility() != 2)
			networks.setEnabled(false);
		else
			networks.setEnabled(true);
		List selectedNetworks = bean.getSelectedNetworks();
		int[] selectedIndices = new int[selectedNetworks.size()] ;
		int j = 0;
		for(int i=0; i<allNetworks.size(); i++)
		{
			if(selectedNetworks.contains(allNetworks.get(i)))
			{
				selectedIndices[j]=i;
				j++;
			}
		}
		if (selectedNetworks.size() > 0)
			networks.setSelectedIndices(selectedIndices);

		JScrollPane jp = new JScrollPane(networks);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(jp, c);				
		add(jp);
		networks.addListSelectionListener(this);

		c.gridwidth = GridBagConstraints.REMAINDER;		
        gridbag.setConstraints(blank, c);
    	add(blank);				
		 */

		save = new JButton("Save");
		add(save);
		save.addActionListener(this);}

	/**
	 * This method gets the list of all the networks.
	 * 
	 * @return returns List of networks.
	 */
	private ArrayList<String> getAllNetworks() {

		ArrayList<String> allNetworks = new ArrayList<String>();

		NetworkVisibilityBean bean = new NetworkVisibilityBean();
		bean.setMessage("getAllNetworks");

		LoginManager manager = new LoginManager(bean); 
		allNetworks = manager.getAllNetworks();

		return allNetworks;
	}

	public void actionPerformed(ActionEvent e) {

		String option = "";

		/*
		if (e.getSource() == dataVisibilityOptions) {
			option = dataVisibilityOptions.getSelectedItem().toString();
			networks.setEnabled(false);
		}

		if (option.equals("Data Visible In Networks")) {
			networks.setEnabled(true);
		}
		 */

		if (e.getSource() == save) {

			org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
				public Void doInBackground() {

					if (logPreferences.getSelectedIndex() == 0) {
						JOptionPane.showMessageDialog(null,
								"Please Select Log Preferences", "",
								JOptionPane.INFORMATION_MESSAGE);
					} /*else if (dataVisibilityOptions.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(this,
						"Please Select Data Visibility Option", "",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				if (dataVisibilityOptions.getSelectedIndex() == 3
						&& selectedNetworks.isEmpty()) {
					JOptionPane.showMessageDialog(this,
							"Please Select Atleast One Network", "",
							JOptionPane.INFORMATION_MESSAGE);
					dataVisibilityOptions.setSelectedIndex(2);
				}*/
					else {

						preference = logPreferences.getSelectedIndex()-1;
						ObjectHandler.setLogStatus(preference);
						ObjectHandler.setUserName(username);

						// write it to the properties file
						try
						{
							PropertiesManager properties = PropertiesManager.getInstance();
							properties.setProperty(DataVisibility.class, PROPERTY_KEY, ""+(logPreferences.getSelectedIndex()-1));
						}
						catch (Exception ex) { }

						DataVisibilityBean dvb = new DataVisibilityBean();
						dvb.setUName(username);

						//temporary hack to display success at the end
						//remove the next line when we use the full blown version of the security module
						dataVisibilityOptions.setSelectedIndex(2);

						dvb.setDataVisibility((short) (dataVisibilityOptions.getSelectedIndex()-1));

						dvb.setLogData((short) (logPreferences.getSelectedIndex()-1));
						dvb.setSelectedNetworks(selectedNetworks);
						dvb.setMessage("saveDataVisibility");

						LoginManager manager = new LoginManager(dvb); 

						boolean isSaved = manager.saveDataVisibility();

						if (isSaved) { 
							String msg="Data Visibility Saved";

							JOptionPane.showMessageDialog(null, msg);
						} else {
							String msg="Data Visibility update failed";

							JOptionPane.showMessageDialog(null, msg);

						}
						//}
					}
					return null;
				}
			};
			worker.execute();
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		selectedNetworks = Arrays.asList((Object[]) networks
				.getSelectedValues());

	}

	/**
	 * This method fulfills the contract of the {@link VisualPlugin} interface.
	 * It returns the GUI component for this visual plugin.
	 */
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	public static void main(String args[]) {
		DataVisibility dv = new DataVisibility();
		JFrame test = new JFrame();
		test.add(dv);
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		test.setSize(400, 200);
		test.setVisible(true);
	}
}
