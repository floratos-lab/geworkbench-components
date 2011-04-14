package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.apache.log4j.Logger;
import org.geworkbench.components.genspace.entity.User;
import org.geworkbench.components.genspace.ui.SocialNetworksHome;
import org.geworkbench.components.genspace.ui.StatusBar;
import org.geworkbench.components.genspace.ui.WorkflowStatistics;
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
	public static Logger logger = Logger.getLogger(GenSpace.class);
	private static InitialContext ctx;
	public static boolean instrument = true;
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		 return " " + ((double) baos.size())/(1024) + " KB";

	}

	public static Object getRemote(String remoteName)
	{
		if(Thread.currentThread().getName().contains("AWT-EventQueue"))
		{
			System.err.println(">>>>>>>>>"+Thread.currentThread().getName());
			throw new IllegalThreadStateException("You may not attempt to access the remote server from an AWT/Swing worker thread");
		}	
		try {
			System.setProperty("org.omg.CORBA.ORBInitialHost", RuntimeEnvironmentSettings.SERVER);
			System.setProperty("com.sun.CORBA.encoding.ORBEnableJavaSerialization","true");
			System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
			System.setProperty("com.sun.corba.ee.transport.ORBMaximumReadByteBufferSize", "3000000");
			System.setProperty("java.naming.factory.initial", "com.sun.enterprise.naming.SerialInitContextFactory");
			System.setProperty("java.naming.factory.url.pkgs", "com.sun.enterprise.naming");
			System.setProperty("java.naming.factory.state", "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
			System.out.println(System.getProperty("java.naming.factory.initial"));
//			System.setProperty("com.sun.appserv.iiop.orbconnections", "10");
			if(ctx == null)
			{
				ctx = new InitialContext();

				System.out.println("Getting IC");
			}
			return ctx.lookup("org.geworkbench.components.genspace.server."+remoteName+"Remote");
		} catch (NamingException e) {
			logger.fatal("Unable find remote object for " + remoteName,e);
		
		}
		return null;
	}
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		GenSpace g = new GenSpace();
	}

	public static SocialNetworksHome networksPanels = new SocialNetworksHome();

	public GenSpace() {
		instance = this;
		initComponents();
	}
	public static void bringUpProfile(User u)
	{
		getInstance().jtp.setSelectedComponent(networksPanels.$$$getRootComponent$$$());
		networksPanels.bringUpProfile(u);
	}
	public static GenSpace getInstance() {
		return instance;
	}

	public WorkflowRepository getWorkflowRepository() {
		return workflowRepository;
	}
	JPanel needLoginPanel;
	public void handleLogin()
	{
		jtp.setComponentAt(5, workflowRepository);
	}
	public void handleLogout()
	{
		jtp.setComponentAt(5, needLoginPanel);
	}
	private StatusBar statusBar;
	
	private void initComponents() {
		jframe = new JFrame("genSpace");

		statusBar = new StatusBar();
		jtp = new JTabbedPane();
		jtp.setSize(1024,768);
		jtp.setPreferredSize(new Dimension(1024,768));
		WorkflowVisualization wv = new WorkflowVisualization();

		WorkflowStatistics stats = new WorkflowStatistics();

		RealTimeWorkFlowSuggestion rtwfs = new RealTimeWorkFlowSuggestion();

		workflowRepository = new WorkflowRepository(jframe);
		needLoginPanel = new JPanel();
		needLoginPanel.add(new JLabel("Please login to genSpace to access this area."));
		org.geworkbench.components.genspace.ui.GenSpaceLogin login = new org.geworkbench.components.genspace.ui.GenSpaceLogin();
		// login.run();

		jtp.addTab("genSpace Login", login);
		jtp.addTab("Workflow Visualization", wv);
		jtp.addTab("Real Time Workflow Suggestion", rtwfs);
		jtp.addTab("Workflow Statistics", stats);
		jtp.addTab("Social Center", networksPanels.$$$getRootComponent$$$());
		jtp.addTab("Workflow Repository", needLoginPanel);
		// jtp.addTab("Message", new Message());
		jframe.setSize(1024,768);
		jframe.setLocation(200, 0);
		jframe.getContentPane().setLayout(new BorderLayout());
		jframe.add(jtp,BorderLayout.CENTER);
		jframe.add(statusBar,BorderLayout.SOUTH);

		// Added by Flavio
		jframe.pack();
//		jframe.setExtendedState(Frame.MAXIMIZED_BOTH);
		jframe.setVisible(true);

		// Moved here by Flavio
		Thread wv_thread = new Thread(wv);
		wv_thread.start();

		Thread rtwfs_thread = new Thread(rtwfs);
		rtwfs_thread.start();

		Thread wfr_thread = new Thread(workflowRepository);
		wfr_thread.start();
//		login.test();
		

		/*
		 * System.out.println("wv: " + wv_thread.getId());
		 * System.out.println("isbu: " + isbu_thread.getId());
		 * System.out.println("rtwfs: " + rtwfs_thread.getId());
		 * System.out.println("login: " + login_thread.getId());
		 */
	}
}
