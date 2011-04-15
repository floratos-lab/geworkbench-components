package org.geworkbench.components.genspace.rating;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;

import org.geworkbench.components.genspace.BrowserLauncher;
import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.RuntimeEnvironmentSettings;
import org.geworkbench.components.genspace.entity.Tool;
import org.geworkbench.components.genspace.entity.User;
import org.geworkbench.components.genspace.entity.UserWorkflow;
import org.geworkbench.components.genspace.entity.Workflow;

public class WorkflowVisualizationPopup extends JPopupMenu implements
		ActionListener {


	private static final long serialVersionUID = 2674054526848758204L;


	// tool options
	private JMenuItem gotoToolPage = new JMenuItem();
	private JMenuItem contactEU = new JMenuItem();

	// tool rating
	private StarRatingPanel toolSRP = new StarRatingPanel();

	// workflow specific
	private JMenuItem viewWorkflowCommentsPage = new JMenuItem(
			"View/add workflow comments");
	private StarRatingPanel workflowSRP = new StarRatingPanel();
	private JMenuItem addWorkflowRepository = new JMenuItem(
			"Add workflow to your repository");

	// we store the tool name each time the menu is invoked
	// so we can speed up the process
	private Tool tool;
	private Workflow workflow;

	public WorkflowVisualizationPopup() {
		super();

		gotoToolPage.addActionListener(this);
		contactEU.addActionListener(this);
		viewWorkflowCommentsPage.addActionListener(this);
		addWorkflowRepository.addActionListener(this);
	}

	public JPopupMenu getThisPopupMenu() {
		return this;
	}
	private User expert;
	public void initialize(final Tool tn, Workflow workflow) {
		this.workflow = workflow;
		this.tool = tn;
//		getThisPopupMenu().removeAll();
		removeAll();

		if (tool != null) {

			tool = tn;
			toolSRP.setTitle("Rate " + tn.getName());

				gotoToolPage
						.setText("Goto GenSpace page for " + tn.getName());
				add(gotoToolPage);

				// add username for expert user request
					
				SwingWorker<User, Void> wk = new SwingWorker<User, Void>()
				{

					@Override
					protected User doInBackground() throws Exception {
						return GenSpaceServerFactory.getUsageOps().getExpertUserFor(tn.getId());
					}
					@Override
					protected void done() {
						try {
							expert = get();
						} catch (InterruptedException e) {

						} catch (ExecutionException e) {

						}
						if(expert != null)
						contactEU.setText("Contact Expert User - ("
								+ expert.getFullName() + ")");
					}
					
				};
				wk.execute();

				if (expert != null) {
					contactEU.setText("Contact Expert User (Loading)");
					add(contactEU);
				}


			if (tool.getId() > 0) {
				toolSRP = new StarRatingPanel();
				toolSRP.setTitle("Rate " + tn);
				toolSRP.loadRating(tn);
				add(new JMenuItem().add(toolSRP));
				add(toolSRP);
			}
		}

		if (workflow != null) {
			
				// display only if we have a good id
				if (workflow.getId() > 0) {
					addWorkflowRepository.setVisible(true);
					add(addWorkflowRepository);
					viewWorkflowCommentsPage.setVisible(true);
					add(viewWorkflowCommentsPage);
				} else
					viewWorkflowCommentsPage.setVisible(false);

			if (workflow.getId() > 0) {
				workflowSRP = new StarRatingPanel();
				workflowSRP.setTitle("Rate workflow until here");
				workflowSRP.loadRating(workflow);
				add(workflowSRP);
			}
		}
		
	}


	@Override
	public void actionPerformed(ActionEvent event) {
		String args = "";

		JMenuItem item = (JMenuItem) event.getSource();
		boolean browser = true;
		if (item == gotoToolPage && tool.getId() > 0)
			args = "tool/index/" + tool.getId();
		else if (item == viewWorkflowCommentsPage && workflow.getId() > 0)
			args = "workflow/index/" + workflow.getId();
		else if (item == contactEU) {
			if (GenSpaceServerFactory.isLoggedIn()) {
				GenSpace.bringUpProfile(expert);
			} else {
				JOptionPane
						.showMessageDialog(null,
								"You need to be logged in to use GenSpace's social features.");
			}
			return;
		} else if (item == addWorkflowRepository && workflow != null && workflow.getId() > 0) {
			browser = false;
			addWorkFlowToRepository();
		}

		if (browser) {
				BrowserLauncher.openURL(RuntimeEnvironmentSettings.GS_WEB_ROOT
						+ args);

		}
	}

	private void addWorkFlowToRepository() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
				if (GenSpaceServerFactory.isLoggedIn()) {
					String name = JOptionPane.showInputDialog("Type a name for the workflow to be added:",
							"");
					if (name != null && name.trim().length() > 0) {
						UserWorkflow uw = new UserWorkflow();
						uw.setName(name);
						uw.setWorkflow(workflow);
						uw.setFolder(GenSpaceServerFactory.getUser().getRootFolder());
						uw.setOwner(GenSpaceServerFactory.getUser());
						uw.setCreatedAt(new Date());
						
						GenSpaceServerFactory.getWorkflowOps().addWorkflow(uw, GenSpaceServerFactory.getUser().getRootFolder().getId());
						GenSpaceServerFactory.updateCachedUser();
						GenSpace.getInstance().getWorkflowRepository().updateUser();
							JOptionPane
							.showMessageDialog(null,
									"Workflow added succesfully to repository");
						
					} else {
						JOptionPane
								.showMessageDialog(null,
										"Operation cancelled: A valid name has to be specified");
					}
				} else {
					JOptionPane
							.showMessageDialog(null,
									"You need to be logged in to manage the repository.");
				}
				return null;
			}
		};
		worker.execute();
	}

}
