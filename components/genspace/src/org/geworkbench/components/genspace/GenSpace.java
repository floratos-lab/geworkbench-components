package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.ui.SocialNetworksHome;
import org.geworkbench.components.genspace.ui.StatusBar;
import org.geworkbench.components.genspace.ui.UpdateablePanel;
import org.geworkbench.components.genspace.ui.WorkflowStatistics;
import org.geworkbench.components.genspace.ui.WorkflowVisualization;
import org.geworkbench.components.genspace.ui.notebook.NotebookPanel;
import org.geworkbench.components.genspace.workflowRepository.WorkflowRepository;



/**
 * This is the main class for genspace. This is a visual plugin and will be a
 * tabbed pane. All other genspace components will be part of the tabbed pane.
 * 
 * @author sheths
 */
public class GenSpace {

	private JTabbedPane jtp;
	public JFrame jframe;
	private static GenSpace instance;
	private WorkflowRepository workflowRepository;
	NotebookPanel notebookPanel;
	public static Logger logger = Logger.getLogger(GenSpace.class);
	public static boolean instrument = false;
	
//	static
//	{
//		logger.setLevel(Level.INFO);
//	}
//	
	public static StatusBar getStatusBar()
	{
		return getInstance().statusBar;
	}
	public static String getObjectSize(Serializable s)
	{
		 ByteArrayOutputStream baos = new ByteArrayOutputStream();
		 ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(s);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		 
		 return " " + ((double) baos.size())/(1024) + " KB";
	}

	
	static java.util.logging.Logger glassfishLogger;
	static java.util.logging.Logger glassfishLogger2;
	static java.util.logging.Logger glassfishLogger3;
	public static void main(String[] args) throws Exception {
		new GenSpace();
//		Thread.sleep(1500);
//		GenSpace.getInstance().login.test();
	}

	public static SocialNetworksHome networksPanels = new SocialNetworksHome();

	public GenSpace() {
		instance = this;
		SwingWorker<Void, Void> wk = new SwingWorker<Void, Void>()
				{
				protected Void doInBackground() throws Exception {
					GenSpaceServerFactory.init();
					return null;
				};
				protected void done() {
					initComponents();

				};
				};
				wk.execute();
		}
	
	public static void bringUpProfile(Object o)
	{
		getInstance().jtp.setSelectedComponent(networksPanels.$$$getRootComponent$$$());
		networksPanels.bringUpProfile((User) (o));
	}
	public static GenSpace getInstance() {
		return instance;
	}

	public WorkflowRepository getWorkflowRepository() {
		return workflowRepository;
	}
	
	JPanel needLoginPanel;
	JPanel needLoginPanel2;

	public void handleLogin()
	{

		jtp.setComponentAt(5, notebookPanel);
		jtp.setComponentAt(6, workflowRepository);
		notebookPanel.init();
		//mahoutRecommendationPanel.handleLogin();
	}
	
	public void handleLogout()
	{
		jtp.setComponentAt(5, needLoginPanel);
		jtp.setComponentAt(6, needLoginPanel2);
		//mahoutRecommendationPanel.handleLogout();
	}
	private StatusBar statusBar;
	org.geworkbench.components.genspace.ui.GenSpaceLogin login;
	private void initComponents() {
		jframe = new JFrame("genSpace");

		statusBar = new StatusBar();
		jtp = new JTabbedPane();
		jtp.setSize(1024,768);
		jtp.setPreferredSize(new Dimension(1024,768));
		WorkflowVisualization wv = new WorkflowVisualization();

		WorkflowStatistics stats = new WorkflowStatistics();

		RealTimeWorkFlowSuggestion rtwfs = new RealTimeWorkFlowSuggestion();

		notebookPanel = new NotebookPanel();
		workflowRepository = new WorkflowRepository(jframe);
		needLoginPanel = new JPanel();
		needLoginPanel.add(new JLabel("Please login to genSpace to access this area."));
		needLoginPanel2 = new JPanel();
		needLoginPanel2.add(new JLabel("Please login to genSpace to access this area."));
		
		login = new org.geworkbench.components.genspace.ui.GenSpaceLogin();
		login.addMahoutPanel();

		try {
			login.autoGSLogin();
		} catch (NoSuchFieldException | SecurityException
				| ClassNotFoundException | IllegalArgumentException
				| IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		SequenceAlignmentPanel saPanel = SequenceAlignmentPanel.getInstance();
		
		jtp.addTab("genSpace Login", login);
		jtp.addTab("Workflow Visualization", wv);
		jtp.addTab("Workflow Suggestions", rtwfs);
		jtp.addTab("Workflow Statistics", stats);
		jtp.addTab("Social Center", networksPanels.$$$getRootComponent$$$());
		jtp.addTab("Research Notebook", needLoginPanel2);
		jtp.addTab("Workflow Repository", needLoginPanel);
//		jtp.addTab("MSA Recommender", saPanel);
		
		jtp.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent ev) {
				if(((JTabbedPane) (ev.getSource())).getSelectedComponent() instanceof UpdateablePanel)
				{
					((UpdateablePanel) ((JTabbedPane) (ev.getSource())).getSelectedComponent()).updateFormFields();
				}
			}
		});
		
		jframe.setSize(1024,768);
		jframe.setLocation(200, 0);
		jframe.getContentPane().setLayout(new BorderLayout());
		jframe.add(jtp, BorderLayout.CENTER);
	
		//statusBar.setPreferredSize(new Dimension(1024, 50));
		jframe.add(statusBar, BorderLayout.SOUTH);

		jframe.pack();
		jframe.setVisible(true);

		Thread wv_thread = new Thread(wv);
		wv_thread.start();

		Thread rtwfs_thread = new Thread(rtwfs);
		rtwfs_thread.start();

		Thread wfr_thread = new Thread(workflowRepository);
		wfr_thread.start();

	}
}
