package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;

import org.geworkbench.components.genspace.server.stubs.TasteUser;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.wrapper.WorkflowWrapper;
import org.geworkbench.engine.config.VisualPlugin;

public class MahoutRecommendationPanel extends JPanel implements VisualPlugin, 
	ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9025891419883690754L;
	private static JPanel mahoutSuggestionsPanel;
	private static JPanel workflowsPanel;
	private static JPanel peoplePanel;
	private static JTextPane workflowSuggestionsArea;
	private static JTextPane peopleSuggestionsArea;
	private static JPanel networkFilteringPanel;
	private JCheckBox networkFilterCheckBox;
	
	public MahoutRecommendationPanel() {
		
		initComponents();
	}
	
	private void initComponents() {
		this.setLayout(new BorderLayout());
		mahoutSuggestionsPanel = new JPanel();
		mahoutSuggestionsPanel.setLayout(new GridLayout(1,2));
		mahoutSuggestionsPanel.setBorder(new MatteBorder(5, 5, 5, 5,
				new Color(215,217,223)));
		mahoutSuggestionsPanel.setBackground(new Color(215, 217, 223));
		//mahoutSuggestionsPanel.setBorder(BorderFactory.createEtchedBorder());
		
		
		workflowsPanel = new JPanel();
		peoplePanel = new JPanel();
		
		workflowsPanel.setLayout(new BorderLayout());
		peoplePanel.setLayout(new BorderLayout());
		
		final JLabel wfLabel = new JLabel();
        wfLabel.setFont(new Font(wfLabel.getFont().getName(), Font.BOLD, 14));
        wfLabel.setText("Your Recommended Workflows");
        wfLabel.setHorizontalAlignment(JLabel.CENTER);
        
        final JLabel pplLabel = new JLabel();
        pplLabel.setFont(new Font(pplLabel.getFont().getName(), Font.BOLD, 14));
        pplLabel.setText("People Like You");
        pplLabel.setHorizontalAlignment(JLabel.CENTER);
        
		workflowSuggestionsArea = new JTextPane();
		peopleSuggestionsArea = new JTextPane();
		
		workflowSuggestionsArea.setContentType("text/html");
		workflowSuggestionsArea.setEditable(false);
		
		peopleSuggestionsArea.setContentType("text/html");
		peopleSuggestionsArea.setEditable(false);
		
		networkFilteringPanel = new JPanel();
		
		workflowSuggestionsArea.setDisabledTextColor(Color.black);
		workflowSuggestionsArea.setEnabled(false);
		workflowSuggestionsArea.setBackground(new Color(214,217,223));
		
		peopleSuggestionsArea.setDisabledTextColor(Color.black);
		peopleSuggestionsArea.setEnabled(false);
		peopleSuggestionsArea.setBackground(new Color(214,217,223));
		
		Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; padding: 0; margin: 0; background-color: #d6d9df } ";
        ((HTMLDocument) workflowSuggestionsArea.getDocument()).getStyleSheet().addRule(bodyRule);
        ((HTMLDocument) peopleSuggestionsArea.getDocument()).getStyleSheet().addRule(bodyRule);
        
        String liRule = "ol { font-family: " + font.getFamily() + "; " +
        	"font-size: " + font.getSize() + "pt; padding: 1em; margin: 20px;} ";
        
        ((HTMLDocument) workflowSuggestionsArea.getDocument()).getStyleSheet().addRule(liRule);
        ((HTMLDocument) peopleSuggestionsArea.getDocument()).getStyleSheet().addRule(liRule);
        
		Style newStyle = ((HTMLDocument) workflowSuggestionsArea.getDocument()).addStyle("BGStyle", null);
		((HTMLDocument) peopleSuggestionsArea.getDocument()).addStyle("BGStyle", null);
		
		StyleConstants.setBackground(newStyle, new Color(214,217,223));
		
		wfLabel.setPreferredSize(new Dimension(300, 40));
		workflowsPanel.setBorder(BorderFactory.createEtchedBorder());
		workflowsPanel.add(wfLabel, BorderLayout.NORTH);
		workflowsPanel.add(new JScrollPane(workflowSuggestionsArea), BorderLayout.CENTER);
		
		pplLabel.setPreferredSize(new Dimension(300, 40));
		peoplePanel.setBorder(BorderFactory.createEtchedBorder());
		peoplePanel.add(pplLabel, BorderLayout.NORTH);
		peoplePanel.add(new JScrollPane(peopleSuggestionsArea), BorderLayout.CENTER);
		
		mahoutSuggestionsPanel.add(workflowsPanel);
		mahoutSuggestionsPanel.add(peoplePanel);
		//mahoutSuggestionsPanel.add(networkFilteringPanel);
		
		JLabel lb = new JLabel("Filter to My Networks");
		lb.setVerticalAlignment(JLabel.CENTER);
		networkFilterCheckBox = new JCheckBox();
		networkFilterCheckBox.setSelected(false);
		networkFilterCheckBox.addActionListener(this);
		
		networkFilteringPanel.add(lb);
		networkFilteringPanel.add(networkFilterCheckBox);
		//networkFilteringPanel.setVisible(false);
		
		this.add(mahoutSuggestionsPanel, BorderLayout.CENTER, 0);
		
		this.setMinimumSize(new Dimension(400, 150));
		//this.setVisible(true);
	}
	
	public void handleLogin() {
		//networkFilteringPanel.setVisible(true);
		this.add(networkFilteringPanel, BorderLayout.EAST, 1);
		displayRecommedations();
	}
	
	public void handleLogout() {
		//networkFilteringPanel.setVisible(false);
		networkFilterCheckBox.setSelected(false);
		this.remove(1);
		displayRecommedations();
	}
	
	public void displayRecommedations() {
		
		User user = GenSpaceServerFactory.getUser();
		
		TasteUser tu = null;
		if (user != null) {
			tu = GenSpaceServerFactory.getPublicFacade().getTasteUserByUser(user);
		} else {
			String hostname = "";
			try {
				hostname = InetAddress.getLocalHost().getHostName();
				
				tu = GenSpaceServerFactory.getPublicFacade().getTasteUserByHostname(hostname);
			} catch (UnknownHostException e) {
				//e.printStackTrace();
				System.out.println("Unknown host");
			}
		}
	
		String wfs = "";
		String peopleInNetwork = "";
		String workflowsWithinNetworkString = "";
		String people = "";
		
		if (tu != null) {
	
			List<Workflow> mahoutSuggestions = getRealTimeMahoutToolSuggestion(tu);

			int lim = 10;
			for(Workflow wa : mahoutSuggestions)
			{
				WorkflowWrapper w = new WorkflowWrapper(wa);
				w.loadToolsFromCache();
				wfs = wfs + "<li>" + w.toString() + "</li>";
				lim--;
				if (lim <= 0)
					break;
			}
			//wfs = wfs.substring(0, wfs.length()-5);
		
			List<TasteUser> peopleLikeYou = getRealTimeMahoutUserSuggestion(tu);
		
			for (TasteUser user1 : peopleLikeYou) {
			if (user1.getUser() == null)
				people += "<li> Anonymous " + user1.getId() + "</li>";
			else
				people += "<li>" + user1.getUser().getUsername() + "</li>";
			}
		
			if (GenSpaceServerFactory.getUser() != null) {
				List<TasteUser> peopleLikeYouInNetwork = getRealTimeMahoutUserWithinNetworkSuggestion(tu);
			
				for (TasteUser user1 : peopleLikeYouInNetwork) {
					if (user1.getUser() == null)
						peopleInNetwork += "<li> Anonymous " + user1.getId() + "</li>";
					else
						peopleInNetwork += "<li>" + user1.getUser().getUsername() + "</li>";
				}
			
				lim = 10;
				List<Workflow> workflowsWithinNetwork = getRealTimeMahoutNetworkWorkflowSuggestion(tu);
				for(Workflow wa : workflowsWithinNetwork)
				{
					WorkflowWrapper w = new WorkflowWrapper(wa);
					w.loadToolsFromCache();
					workflowsWithinNetworkString += "<li>" + w.toString() + "</li>";
					lim--;
					if (lim <= 0)
						break;
				}
			}
			
			if (!networkFilterCheckBox.isSelected()) {
			
				workflowSuggestionsArea.setText("<html><body><ol>"+wfs+"</ol></body></html>");
				
				peopleSuggestionsArea.setText("<html><body><ol>"+people+"</ol></body></html>");
			
			} else {  
			
				if (GenSpaceServerFactory.getUser() != null) {
	
					peopleSuggestionsArea.setText("<html><body><ol>"+peopleInNetwork+"</ol></body></html>");
			
					workflowSuggestionsArea.setText("<html><body><ol>"+workflowsWithinNetworkString+"</ol></body></html>");
				}
			}
		}
	}
	
	private static List<org.geworkbench.components.genspace.server.stubs.Workflow> getRealTimeMahoutToolSuggestion(TasteUser tu) {
		
		try {
			return (GenSpaceServerFactory.getUsageOps().getMahoutToolSuggestion(tu.getId(), 0));
			
		} catch (Exception e) {
			return null;
		}
	}
	
	private static List<org.geworkbench.components.genspace.server.stubs.TasteUser> getRealTimeMahoutUserSuggestion(TasteUser tu) {
		
		try {
			return (GenSpaceServerFactory.getUsageOps().getMahoutUserSuggestion(tu.getId(), 0));
		} catch (Exception e) {
			return null;
		}
	}
	
	private static List<org.geworkbench.components.genspace.server.stubs.TasteUser> 
		getRealTimeMahoutUserWithinNetworkSuggestion(TasteUser tu) {
		
		try {
			return (GenSpaceServerFactory.getUsageOps().getMahoutUserSuggestion(tu.getId(), 1));
		} catch (Exception e) {
			return null;
		}
	}
	
	private static List<org.geworkbench.components.genspace.server.stubs.Workflow> 
		getRealTimeMahoutNetworkWorkflowSuggestion(TasteUser tu) {
		
		try {
			return (GenSpaceServerFactory.getUsageOps().getMahoutUserWorkflowsSuggestion(tu.getId(), 1));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		displayRecommedations();		
	}

}
