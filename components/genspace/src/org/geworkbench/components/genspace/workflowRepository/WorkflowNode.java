package org.geworkbench.components.genspace.workflowRepository;

import javax.swing.tree.DefaultMutableTreeNode;

import org.geworkbench.components.genspace.entity.UserWorkflow;


public class WorkflowNode extends DefaultMutableTreeNode {

	public UserWorkflow userWorkflow;

	public WorkflowNode(UserWorkflow uw) {
		super(uw.getName());
		userWorkflow = uw;
	}
}
