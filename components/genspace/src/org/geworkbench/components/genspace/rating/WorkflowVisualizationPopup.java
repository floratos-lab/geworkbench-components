package org.geworkbench.components.genspace.rating;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.MatteBorder;

import org.geworkbench.components.genspace.RuntimeEnvironmentSettings;
import org.geworkbench.components.genspace.ServerRequest;
import org.geworkbench.components.genspace.ui.LoginManager;
import org.geworkbench.util.BrowserLauncher;

import org.jdesktop.swingworker.*;

public class WorkflowVisualizationPopup extends JPopupMenu implements ActionListener{

	private boolean toolOptions = false;
	private boolean toolRating = false;
	private boolean workflowOptions = false;
	private boolean workflowRating = false;

	//tool options
	private JMenuItem gotoToolPage = new JMenuItem();
	private JMenuItem contactEU = new JMenuItem();

	//tool rating
	private StarRatingPanel toolSRP = new StarRatingPanel(this,
			RuntimeEnvironmentSettings.TOOL_SERVER, 
			"getToolRating", 
	"writeToolRating");

	//workflow specific
	private TitleItem workflowTitle = new TitleItem("Workflow");
	private JMenuItem viewWorkflowCommentsPage = new JMenuItem("View/add workflow comments");
	private StarRatingPanel workflowSRP = new StarRatingPanel(this,
			RuntimeEnvironmentSettings.ISBU_SERVER, 
			"getWFRating", 
	"writeWFRating");


	//we store the tool name each time the menu is invoked 
	//so we can speed up the process
	private String toolName;
	private int toolId = -1;
	private ArrayList<String> workflow;
	private int wni = -1;


	public WorkflowVisualizationPopup(){
		super();

		gotoToolPage.addActionListener(this);
		contactEU.addActionListener(this);
		viewWorkflowCommentsPage.addActionListener(this);
	}
	
	public JPopupMenu getThisPopupMenu() {
		return this;
	}

	public void initialize(final String tn, final ArrayList<String> workflow){

		org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
			public Void doInBackground() {

				//only perform setup if we need to
				if (toolName == null || !toolName.equals(tn)){

					getThisPopupMenu().removeAll();
					toolName = tn;
					toolSRP.setTitle("Rate " + tn);

					if (toolOptions){

						ArrayList toolServerArgs = new ArrayList();
						toolServerArgs.add(tn);
						Integer id = 
							(Integer)ServerRequest.get(RuntimeEnvironmentSettings.TOOL_SERVER, "getToolId", toolServerArgs);
						if (id != null && id.intValue() != 0){
							toolId = id;
							gotoToolPage.setText("Goto GenSpace page for " + tn);
							add(gotoToolPage);
						}
						else{
							toolId = -1;
						}

						//add username for expert user request
						toolServerArgs.add(LoginManager.getLoggedInUser());
						String expertUser = 
							(String)ServerRequest.get(RuntimeEnvironmentSettings.TOOL_SERVER, "getExpertUser", toolServerArgs);

						if (expertUser != null && !expertUser.equals("none")){
							contactEU.setText("Contact Expert User - (" + expertUser + ")");
							add(contactEU);
						}
					}

					if (toolRating && toolId > 0){
						toolSRP.setTitle("Rate " + tn);
						toolSRP.loadRating(toolId);
						add(toolSRP);
					}
				}

				if (workflow != null){		
					if (workflowOptions){
						add(workflowTitle);
						Integer id = 
							(Integer)ServerRequest.get(RuntimeEnvironmentSettings.ISBU_SERVER, "getWFId", workflow);

						if (id != null) 
							wni = id;
						else wni = -1;

						//display only if we have a good id
						if (id != null && id > 0){
							viewWorkflowCommentsPage.setVisible(true);
							add(viewWorkflowCommentsPage);
						}
						else
							viewWorkflowCommentsPage.setVisible(false);

					}
					if (workflowRating && wni > 0){
						workflowSRP.setTitle("Rate workflow until here");
						workflowSRP.loadRating(wni);
						add(workflowSRP);
					}
				}


				return null;
			}
		};
		worker.execute();
	}

	public void showWorkflowRating() { 
		workflowRating = true;
		workflowTitle.setVisible(true);
		workflowSRP.setVisible(true);
	}
	public void showWorkflowOptions() {
		workflowOptions = true;
		workflowTitle.setVisible(true);
		viewWorkflowCommentsPage.setVisible(true);
	}
	public void showToolOptions() { 
		toolOptions = true; 

		gotoToolPage.setVisible(true);
		contactEU.setVisible(true);
	}
	public void showToolRating() { 
		toolRating = true; 
		toolSRP.setVisible(true);
	}



	public void hideWorkflowRating() { 
		workflowRating = false;
		workflowTitle.setVisible(false);
		workflowSRP.setVisible(false);
	}
	public void hideWorkflowOptions() {
		workflowOptions = false;
		workflowTitle.setVisible(false);
		viewWorkflowCommentsPage.setVisible(false);
	}
	public void hideToolOptions() { 
		toolOptions = false; 

		gotoToolPage.setVisible(false);
		contactEU.setVisible(false);
	}
	public void hideToolRating() { 
		toolRating = false; 
		toolSRP.setVisible(false);
	}

	public void actionPerformed(ActionEvent event) {
		String args = "";

		JMenuItem item = (JMenuItem)event.getSource();

		if (item == gotoToolPage && toolId > 0) 
			args="tool/index/" + toolId;
		else if (item == viewWorkflowCommentsPage && wni > 0) 
			args="workflow/index/" + wni;
		else if (item == contactEU){
			JOptionPane.showMessageDialog(this, 
					"This functionality has not yet been enabled.\n" +
					"Please use the messaging plugin to contact the expert user.", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}


		try {
			BrowserLauncher.openURL(RuntimeEnvironmentSettings.GS_WEB_ROOT + args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}


class TitleItem extends JPanel{
	Color backgroundColor = new Color(35, 35, 142);
	Color foregroundColor = Color.WHITE;
	Font font = new Font("Verdana", Font.BOLD, 9);
	JLabel label = new JLabel();

	public TitleItem (String title){
		setBackground(backgroundColor);
		MatteBorder border = new MatteBorder(3, 3, 3, 3, backgroundColor);
		setBorder(border);
		label.setText(title.toUpperCase());
		label.setFont(font);
		label.setBackground(foregroundColor);
		label.setForeground(foregroundColor);
		setLayout(new BorderLayout());
		add(label);
	}

	public void setTitle(String title){
		label.setText(title.toUpperCase());
	}
}