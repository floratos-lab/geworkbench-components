package org.geworkbench.components.genspace.ui;
import org.geworkbench.components.genspace.ui.*;
import org.geworkbench.components.genspace.bean.*;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.properties.PropertiesManager;

public class GenSpaceSecurityPanel extends JPanel implements VisualPlugin, ActionListener {

	private JButton logout;
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}
	public GenSpaceSecurityPanel(String uName)
	{
		JTabbedPane mainPanel = new JTabbedPane();
		DataVisibility dataPanel = new DataVisibility(uName);
		mainPanel.addTab("Data Visibility", dataPanel);
		
		//NetworkVisibility nwPanel = new NetworkVisibility(uName);
		//mainPanel.addTab("User Visibility", nwPanel);
		
		GenSpaceGeneralProfile genPanel = new GenSpaceGeneralProfile();
		mainPanel.addTab("General Profile", genPanel);
		
		add(mainPanel);
		logout = new JButton("logout");
		add(logout);
		logout.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
	if (e.getSource() == logout) {
		LoginManager mgr = new LoginManager();
		mgr.logout();
		GenSpaceLogin p = new GenSpaceLogin();
		this.removeAll();
		this.add(p);
		this.setSize(500, 500);
		this.revalidate();
		}
	}
	
	     

}
