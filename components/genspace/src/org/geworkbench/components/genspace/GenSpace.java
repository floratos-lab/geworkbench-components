package org.geworkbench.components.genspace;

import javax.swing.*;

import org.geworkbench.engine.config.*;
import java.awt.*;

/**
 * This is the main class for genspace. 
 * This is a visual plugin and will be a tabbed pane. All other genspace components will be part of the tabbed pane. 
 * @author swapneel
 */
public class GenSpace extends JPanel implements VisualPlugin {

	private JTabbedPane jtp;
	
	public GenSpace()
	{	
		initComponents();
	}

	
	private void initComponents()
	{	
		jtp = new JTabbedPane();
		
		jtp.addTab("GenSpace Login", new org.geworkbench.components.genspace.ui.GenSpaceLogin());
		jtp.addTab("Workflow Visualization", new WorkflowVisualization());
		jtp.addTab("Real Time Workflow Suggestion", new RealTimeWorkFlowSuggestion());
		jtp.addTab("ISBU WorkFlow Visualization", new ISBUWorkFlowVisualization());
		jtp.addTab("Message", new Message());		
		
		add(jtp);
    }
    
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }
}
