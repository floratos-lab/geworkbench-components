package org.geworkbench.components.genspace.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.MahoutRecommendationPanel;
import org.geworkbench.components.genspace.RealTimeWorkFlowSuggestion;
import org.geworkbench.components.genspace.ui.chat.ChatWindow;
import org.geworkbench.engine.config.VisualPlugin;

public class GenSpaceSecurityPanel extends JPanel implements VisualPlugin,
		ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9094613520505076713L;
	private JButton logout;
	public MahoutRecommendationPanel mahoutRecommendationPanel;

	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	public GenSpaceSecurityPanel(String uName) {
		setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(1024, 500));

		JTabbedPane mainPanel = new JTabbedPane();
		DataVisibility dataPanel = new DataVisibility();
		dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
		mainPanel.addTab("Data Visibility", dataPanel);

		// NetworkVisibility nwPanel = new NetworkVisibility(uName);
		// mainPanel.addTab("User Visibility", nwPanel);

		GenSpaceGeneralProfile genPanel = new GenSpaceGeneralProfile();
		mainPanel.addTab("General Profile", genPanel);
		mainPanel.setMaximumSize(new Dimension(500,500));
		panel.setMaximumSize(new Dimension(500,500));
		panel.add(mainPanel);
		logout = new JButton("logout");
		panel.add(logout);
		logout.addActionListener(this);
		
		add(panel, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == logout) {
			
			if (GenSpaceLogin.chatHandler != null) {
				GenSpaceLogin.chatHandler.logout();
				if (GenSpaceLogin.chatHandler.rf != null) {
					GenSpaceLogin.chatHandler.rf.setVisible(false);
					GenSpaceLogin.chatHandler.rf = null;
				}
				for (ChatWindow w : GenSpaceLogin.chatHandler.chats.values()) {
					if (w != null) {
						w.setVisible(false);
						w = null;
					}
				}
				GenSpaceLogin.chatHandler.chats.clear();
			}
			GenSpaceServerFactory.logout();
			GenSpace.getInstance().handleLogout();
			//GenSpaceLogin.mahoutRecommendationPanel.handleLogout();
			//GenSpaceLogin.mahoutRecommendationPanel.removeAll();
			GenSpaceLogin p = new GenSpaceLogin();
			p.addMahoutPanel();
			GenSpace.getInstance().getWorkflowRepository().updateFormFields();
			RealTimeWorkFlowSuggestion.cwf = null;
			GenSpace.networksPanels.updateFormFields();
			this.removeAll();
			this.add(p);
			this.setSize(500, 500);
			this.revalidate();
		}
	}
	
	public void addMahoutPanel() {
		mahoutRecommendationPanel = new MahoutRecommendationPanel();
		mahoutRecommendationPanel.displayRecommedations();
		mahoutRecommendationPanel.setPreferredSize(new Dimension(1000, 250));
		add(mahoutRecommendationPanel, BorderLayout.SOUTH);
	}
	
	public void handleLogin() {
		mahoutRecommendationPanel.handleLogin();
	}

}
