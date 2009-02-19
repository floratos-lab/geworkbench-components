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

	
	private void initComponents() {
		jtp = new JTabbedPane();
		
		WorkflowVisualization wv = new WorkflowVisualization();
		Thread wv_thread = new Thread(wv);
		wv_thread.start();
		
		ISBUWorkFlowVisualization isbu= new ISBUWorkFlowVisualization();
		Thread isbu_thread = new Thread(isbu);
		isbu_thread.start();
		
		RealTimeWorkFlowSuggestion rtwfs = new RealTimeWorkFlowSuggestion();
		Thread rtwfs_thread = new Thread(rtwfs);
		rtwfs_thread.start();
		
		org.geworkbench.components.genspace.ui.GenSpaceLogin login = new org.geworkbench.components.genspace.ui.GenSpaceLogin();
		Thread login_thread = new Thread(login);
		login_thread.start();
		
		jtp.addTab("GenSpace Login", login);
		jtp.addTab("Workflow Visualization", wv);
		jtp.addTab("Real Time Workflow Suggestion", rtwfs);
		jtp.addTab("Workflow Statistics", isbu);
		//jtp.addTab("Message", new Message());		
		
		add(jtp);
		
		/*
		System.out.println("wv: " + wv_thread.getId());
		System.out.println("isbu: " + isbu_thread.getId());
		System.out.println("rtwfs: " + rtwfs_thread.getId());
		System.out.println("login: " + login_thread.getId());
		*/
    }
	
	
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }
}
