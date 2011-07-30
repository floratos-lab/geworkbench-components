package org.geworkbench.components.genspace.workflowRepository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.GregorianCalendar;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.wrapper.WorkflowWrapper;
import org.geworkbench.engine.config.VisualPlugin;

public class WorkflowDetailsPanel extends JPanel implements VisualPlugin,
ActionListener {


	private static final long serialVersionUID = -220085507992207251L;
	private WorkflowRepository workflowRepository;
	private JTextArea textArea;
//	private Workflow workflow;
	private JButton sendButton = new JButton("Send Selected Workflow");
	private JButton importButton = new JButton("Import");
	private JButton exportButton = new JButton("Export");
	private JButton refreshButton = new JButton("Refresh Screen");
	private final JFileChooser fc = new JFileChooser();

	public WorkflowDetailsPanel(WorkflowRepository workflowRepository) {
		super(new BorderLayout());
		this.workflowRepository = workflowRepository;
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		sendButton.addActionListener(this);
		buttonPanel.add(sendButton);
//		importButton.addActionListener(this);
//		buttonPanel.add(importButton);
//		exportButton.addActionListener(this);
//		buttonPanel.add(exportButton);
//		publishButton.addActionListener(this);
//		buttonPanel.add(publishButton);
		refreshButton.addActionListener(this);
//		buttonPanel.add(refreshButton);
		add(buttonPanel, BorderLayout.NORTH);
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setText("Select a Workflow from the Repository to display its information.\n");
		textArea.setBorder(BorderFactory
				.createTitledBorder("Workflow Information"));
		JScrollPane sp = new JScrollPane(textArea);
		add(sp, BorderLayout.CENTER);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	public void setAndPrintWorkflow(Workflow workflow) {
//		this.workflow = workflow;
		if (workflow != null)
			textArea.setText(getWorkflowDetailsString(workflow));
		else
			clearData();
	}

	public void clearData() {
//		workflow = null;
		textArea.setText("");
	}

	public String getWorkflowDetailsString(Workflow w) {
		WorkflowWrapper wr = new WorkflowWrapper(w);
		String result = "ID: " + w.getId() + "\n";
		result += "Creator: " + ( w.getCreator() == null ? "system" : wr.getCreator().getUsername()) + "\n";
		if(w.getCreatedAt() != null)
			result += "Creation date: " + wr.getCreatedAt().toString()	 + "\n";
		result += "Average rating: " + wr.getAvgRating() + "\n";
		result += "Usage count: " + wr.getUsageCount() + "\n";
		result += "Comments count: " + wr.getNumComments() + "\n";
		result += "Ratings count: " + wr.getNumRating()+ "\n";
		result += "Tools list: " + wr.toString() + "\n";
		return result;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Object o = e.getSource();

//		String result = "";
			if (o.equals(sendButton)) {
				send();
			} else if (o.equals(refreshButton)) {
				workflowRepository.updateFormFields();
			} else if (o.equals(importButton)) {
				importWorkflow();
			} else if (o.equals(exportButton)) {
				exportWorkflow();
			}
	}
	private void messageUser(String m)
	{
		JOptionPane.showMessageDialog(null, m);
	}
	private void importWorkflow() {
		int returnVal = fc.showOpenDialog(workflowRepository);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			SwingWorker<UserWorkflow, Void> worker = new SwingWorker<UserWorkflow, Void>() {
				protected UserWorkflow doInBackground() throws Exception {
					File file = fc.getSelectedFile();
					ObjectInputStream input = new ObjectInputStream(
							new FileInputStream(file));
					UserWorkflow w = (UserWorkflow) input.readObject();
					input.close();
					w.setId(0);
					w.setOwner(GenSpaceServerFactory.getUser());
					UserWorkflow ret = GenSpaceServerFactory.getWorkflowOps()
							.importWorkflow(w);
					GenSpaceServerFactory.updateCachedUser();
					return ret;
				};

				protected void done() {
					try {
						UserWorkflow ret = get();
						if (ret != null) {
							workflowRepository.repositoryPanel.tree
								.recalculateAndReload();
						}
						else
							messageUser( "Operation cancelled: Make sure that the Workflow is not already present in your repository");
					} catch (InterruptedException e) {
						GenSpace.logger.warn("Unable to talk to server", e);
					} catch (ExecutionException e) {
						GenSpaceServerFactory.handleExecutionException(e);
						return;
					}
				};
			};
			worker.execute();

		}
	}

	private void exportWorkflow() {
		TreePath currentSelection = workflowRepository.repositoryPanel.tree.tree
		.getSelectionPath();
		if (currentSelection != null) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
					.getLastPathComponent());
			if (currentNode instanceof WorkflowNode) {// it can't be a
				// folder
				int returnVal = fc.showSaveDialog(workflowRepository);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					WorkflowNode wn = (WorkflowNode) currentNode;
					ObjectOutputStream output;
					try {
						output = new ObjectOutputStream(
								new FileOutputStream(file));
						output.writeObject(wn.userWorkflow);
						output.flush();
						output.close();
					} catch (FileNotFoundException e) {
						messageUser("Unable to write file");
					} catch (IOException e) {
						messageUser("Unable to write file");
					}
					
				}
			} else
				messageUser("An entire folder can't be exported");
		} else
			messageUser("Select a workflow from the repository");
		messageUser("Workflow exported successfully");
	}


	private void send() {
		TreePath currentSelection = workflowRepository.repositoryPanel.tree.tree
		.getSelectionPath();
		if (currentSelection != null) {
			DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) (currentSelection
					.getLastPathComponent());
			if (currentNode instanceof WorkflowNode) {// it can't be a
				// folder
				final String receiver = JOptionPane
				.showInputDialog("Input the receiver username");
				if (receiver != null && !receiver.trim().equals("")) {
					final WorkflowNode wn = (WorkflowNode) currentNode;
					

					SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
						protected Boolean doInBackground() throws Exception {
							IncomingWorkflow newW = new IncomingWorkflow();
							newW.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
							newW.setName(wn.userWorkflow.getName());
							newW.setWorkflow(wn.userWorkflow.getWorkflow());
							newW.setSender(GenSpaceServerFactory.getUser());
							return GenSpaceServerFactory.getWorkflowOps().sendWorkflow(newW,receiver);
						};

						protected void done() {
							try {
								Boolean ret = get();
								if (ret) {
									messageUser("Workflow sent successfully!");
								}
								else
									messageUser( "Operation cancelled: Make sure that the Workflow is not already present in your repository");
							} catch (InterruptedException e) {
								GenSpace.logger.warn("Unable to talk to server", e);
							} catch (ExecutionException e) {
								GenSpaceServerFactory.handleExecutionException(e);
								return;
							}
						};
					};
					worker.execute();
					
				} else
					messageUser("Input a valid username");
			} else
				messageUser("An entire folder can't be sent");
		} else
			messageUser("Select a workflow from the repository");
	}// SEND


}
