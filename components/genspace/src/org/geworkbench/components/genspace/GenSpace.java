package org.geworkbench.components.genspace;

import javax.swing.*;

import org.geworkbench.engine.config.*;
import java.awt.*;

/**
 * This is the main class for genspace. 
 * This is a visual plugin and will be a tabbed pane. All other genspace components will be part of the tabbed pane. 
 * @author sheths
 */
public class GenSpace {

	private JTabbedPane jtp;
	private JFrame jframe;
	
	public GenSpace()
	{	
		initComponents();
		
	}

	
	private void initComponents() {
		jframe = new JFrame("genSpace");
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
		//login.run();
		
		
		jtp.addTab("genSpace Login", login);
		jtp.addTab("Workflow Visualization", wv);
		jtp.addTab("Real Time Workflow Suggestion", rtwfs);
		jtp.addTab("Workflow Statistics", isbu);
		//jtp.addTab("Message", new Message());		
		
		jframe.add(jtp);
		
		jframe.setSize(800, 600);
		jframe.setVisible(true);
		
		/*
		System.out.println("wv: " + wv_thread.getId());
		System.out.println("isbu: " + isbu_thread.getId());
		System.out.println("rtwfs: " + rtwfs_thread.getId());
		System.out.println("login: " + login_thread.getId());
		*/
    }
}
