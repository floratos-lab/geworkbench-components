package org.geworkbench.components.genspace.ui;

import org.geworkbench.components.genspace.bean.*;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.geworkbench.components.genspace.bean.NetworkVisibilityBean;

/**
 * This class is used to build panel for setting user's visibility.
 */
public class NetworkVisibility extends JPanel implements VisualPlugin, ActionListener,
		ListSelectionListener {

	private JComboBox networkVisibilityOptions;

	private JList networks;

	private JButton save;

	private List selectedNetworks = new ArrayList();
	private String username = ""; 

	public NetworkVisibility() {
		System.out.println("Data Visibility Options");
		initComponents();
	}
	
	public NetworkVisibility(String uName) {
		System.out.println("Data Visibility Options");
		LoginManager manager = new LoginManager();
		NetworkVisibilityBean bean = manager.getNWVisibilityBean(uName);
		username = uName;
		if(null == bean)
			initComponents();
		else
			initComponents(bean);
	}

	private void initComponents() {

		networkVisibilityOptions = new JComboBox();
		networkVisibilityOptions.addItem("-- Select Visibility Options --");
		networkVisibilityOptions.addItem("Not Visible At All");
		networkVisibilityOptions.addItem("Visible Within My Network");
		networkVisibilityOptions.addItem("Visible In Networks");
		add(networkVisibilityOptions);
		networkVisibilityOptions.addActionListener(this);

		ArrayList<String> allNetworks = getAllNetworks();
		networks = new JList(allNetworks.toArray());
		networks.setVisibleRowCount(3);
		networks
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		networks.setEnabled(false);
		add(new JScrollPane(networks));
		networks.addListSelectionListener(this);

		save = new JButton("Save");
		add(save);
		save.addActionListener(this);
	}
	
	private void initComponents(NetworkVisibilityBean bean) {
		this.setSize(500, 600);
		
		GridBagLayout gridbag = new GridBagLayout();
    	this.setLayout(gridbag);
    	
    	GridBagConstraints c = new GridBagConstraints();
    	c.ipady = 5;
    	JLabel blank = new JLabel(" ");
    	
		networkVisibilityOptions = new JComboBox();
		networkVisibilityOptions.addItem("-- Select Visibility Options --");
		networkVisibilityOptions.addItem("Not Visible At All");
		networkVisibilityOptions.addItem("Visible Within My Network");
		networkVisibilityOptions.addItem("Visible In Networks");

		networkVisibilityOptions.setSelectedIndex(bean.getUserVisibility()+1);
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(networkVisibilityOptions, c);
		add(networkVisibilityOptions);
		networkVisibilityOptions.addActionListener(this);

		c.gridwidth = GridBagConstraints.REMAINDER;		
        gridbag.setConstraints(blank, c);
    	add(blank);	
    	
		ArrayList<String> allNetworks = getAllNetworks();
		networks = new JList(allNetworks.toArray());
		networks.setVisibleRowCount(3);
		networks
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		if(bean.getUserVisibility()!=2)
			networks.setEnabled(false);
		else
			networks.setEnabled(true);
		List selectedNetworks = bean.getSelectedNetworks();
		int[] selectedIndices = new int[selectedNetworks.size()] ;
		int j = 0;
		for(int i=0; i<allNetworks.size(); i++)
		{
			System.out.println("Checking on"+allNetworks.get(i));
			if(selectedNetworks.contains(allNetworks.get(i)))
			{
				System.out.println("Found in selected");
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
    	
		save = new JButton("Save");
		add(save);
		save.addActionListener(this);
	}
	
    /**
     * This method fulfills the contract of the {@link VisualPlugin} interface.
     * It returns the GUI component for this visual plugin.
     */
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }	

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
		networks.setEnabled(false);

		if (e.getSource() == networkVisibilityOptions) {
			option = networkVisibilityOptions.getSelectedItem().toString();
		}

		if (option.equals("Visible In Networks")) {
			networks.setEnabled(true);
		}

		if (e.getSource() == save) {
			if (networkVisibilityOptions.getSelectedIndex() == 0) {
				JOptionPane.showMessageDialog(this,
						"Please Select Visibility Option", "",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				if (networkVisibilityOptions.getSelectedIndex() == 2
						&& selectedNetworks.isEmpty()) {
					JOptionPane.showMessageDialog(this,
							"Please Select Atleast One Network", "",
							JOptionPane.INFORMATION_MESSAGE);
					networkVisibilityOptions.setSelectedIndex(2);
				} else {

					NetworkVisibilityBean nvb = new NetworkVisibilityBean();
					nvb.setUName(username);
					nvb.setUserVisibility((short) (networkVisibilityOptions
							.getSelectedIndex()-1));
					nvb.setSelectedNetworks(selectedNetworks);
										
					 LoginManager manager = new LoginManager();
					 if(manager.saveNetworkVisibility(nvb))
							JOptionPane.showMessageDialog(this,
									"Network visibility updated", "",
									JOptionPane.INFORMATION_MESSAGE);
					 else
							JOptionPane.showMessageDialog(this,
									"Network visibility update failed", "",
									JOptionPane.INFORMATION_MESSAGE);

						 
						 
						 
					 
				}
			}
		}
	}

	public void valueChanged(ListSelectionEvent e) {
		selectedNetworks = Arrays.asList((Object[]) networks
				.getSelectedValues());
	}

	public static void main(String args[]) {
		NetworkVisibility dv = new NetworkVisibility();
		JFrame test = new JFrame();
		test.add(dv);
		test.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		test.setSize(400, 200);
		test.setVisible(true);
	}
}
