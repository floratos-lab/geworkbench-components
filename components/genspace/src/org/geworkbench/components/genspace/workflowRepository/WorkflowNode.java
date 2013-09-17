package org.geworkbench.components.genspace.workflowRepository;

import javax.swing.tree.DefaultMutableTreeNode;

import org.geworkbench.components.genspace.server.stubs.UserWorkflow;


public class WorkflowNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5668768611145012428L;
	public UserWorkflow userWorkflow;

	public WorkflowNode(UserWorkflow uw) {
		super(uw.getName());
		userWorkflow = uw;
	}
}
