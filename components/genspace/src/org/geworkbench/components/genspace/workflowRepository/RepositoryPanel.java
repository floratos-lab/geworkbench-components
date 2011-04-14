package org.geworkbench.components.genspace.workflowRepository;

import java.awt.Component;
import java.awt.event.ActionListener;

import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;

/**
 * Based on org.geworkbench.builtin.projects.ProjectPanel Displays workflows
 * saved in the repository of a user, in a tree-based folder hierarchy TODO: the
 * folder hierarchy is not completed
 * 
 * @author flavio
 * 
 */
public class RepositoryPanel implements VisualPlugin, MenuListener {

	public DynamicTree tree = new DynamicTree(this);
	WorkflowRepository workflowRepository;

	public RepositoryPanel(WorkflowRepository wr) {
		super();
		workflowRepository = wr;
	}

	@Override
	public Component getComponent() {
		return tree;
	}

	@Override
	public ActionListener getActionListener(String var) {
		return null;
	}

}