package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.border.MatteBorder;

import org.geworkbench.components.genspace.rating.WorkflowVisualizationPopup;
import org.geworkbench.components.genspace.ui.LoginManager;
import org.geworkbench.engine.config.VisualPlugin;

import org.jdesktop.swingworker.*;



public class RealTimeWorkFlowSuggestion extends JPanel implements VisualPlugin, Runnable{

	JRadioButton log, logAnon, noLog;
	ButtonGroup group;
	JPanel radioPanel, saveReset;
	JButton save, reset;
	private static ImageIcon arrow = new ImageIcon("components/genspace/src/org/geworkbench/components/genspace/rating/arrow_right.png");


	//###
	private static String clientSideID = null;
	private static String serverIP = RuntimeEnvironmentSettings.ISBU_SERVER.getHost();
	private static int serverPort = RuntimeEnvironmentSettings.ISBU_SERVER.getPort();


	private static ArrayList <TransactionElement> currentWorkFlow = new ArrayList();
	private static String currentTid = null;

	private static boolean firstTimeFlag = true;//used for checking the beginning of new transactions
	private static ArrayList <String> usedWorkFlowToday = new ArrayList();


	private static JPanel workflowViewerPanel = new JPanel();
	private static JPanel workflowNodePanel = new JPanel(new FlowLayout());
	private static JScrollPane workflowViewerScrollPane = new JScrollPane(workflowViewerPanel);
	private static JPanel workflowInfoPanel = new JPanel(new BorderLayout());

	private static WorkflowVisualizationPopup popup = new WorkflowVisualizationPopup();

	/**
	 * Components for the viewer panel
	 */

	private static JLabel viewerStatus = new JLabel();

	/**
	 * Components for the info panel
	 */
	private static JTextArea infoArea = new JTextArea();

	static final String PROPERTY_KEY = "genSpace_logging_preferences"; // the key in the properties file
	int preference; // the logging preference

	public RealTimeWorkFlowSuggestion() {	

	}

	public void run() {
		initComponents();
	}

	private void initComponents()
	{	  
		try {

			/*

        	labelNC = new JLabel("Analysis Tools Suggestion Center:");

        	labelNC1 = new JLabel("The genSpace server currently has the information for the following tools:");

        	ArrayList allAnalysisTools = getAllAnalysisTools();
        	labelNC2 = new JLabel(allAnalysisTools.toString());

        	labelNC3 = new JLabel("Top 3 most popular tools:");
        	ArrayList top3MostPopularTools =  getTop3MostPopularTools();
            labelNC4 = new JLabel(top3MostPopularTools.get(0) + " " + top3MostPopularTools.get(1) + " " + top3MostPopularTools.get(2));


            labelNC5 = new JLabel("Top 3 most popular tools as work flow head:");
            ArrayList top3MostPopularWFHead = getTop3MostPopularWFHead(); 
            labelNC6 = new JLabel(top3MostPopularWFHead.get(0) + " " + top3MostPopularWFHead.get(1) + " " + top3MostPopularWFHead.get(2));

            labelNC7 = new JLabel("Top 3 most popular work flow :");
            ArrayList top3MostPopularWF = getTop3MostPopularWF();
            labelNC8 = new JLabel(top3MostPopularWF.get(0) + " " + top3MostPopularWF.get(1) + " " + top3MostPopularWF.get(2));

			 */

			//ISBUPanel = new JPanel(new GridLayout(3,1));

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * setup general layout of panel 
		 * */

		//the viewer panel
		workflowViewerPanel.setLayout(new BorderLayout());
		workflowViewerPanel.setBorder(new MatteBorder(10,10,10,10, new Color(35, 35, 142)));
		workflowViewerPanel.setBackground( new Color(35, 35, 142));


		//setup viewer status
		viewerStatus.setForeground(Color.WHITE);
		viewerStatus.setText("No analysis has occured yet.");
		workflowViewerPanel.add(viewerStatus, BorderLayout.NORTH);
		workflowNodePanel.setBackground( new Color(35, 35, 142));
		workflowNodePanel.setBorder(new MatteBorder(10,10,10,10, new Color(35, 35, 142)));
		workflowViewerPanel.add(workflowNodePanel, BorderLayout.CENTER);


		//the info panel
		workflowInfoPanel.add(new JScrollPane(infoArea));
		infoArea.setFont(new Font( "Verdana", Font.PLAIN, 10 ));
		infoArea.append("You haven't used any tools!\n");
		infoArea.append("Next best rated tool to use: none.");


		//add both panels
		this.setLayout(new BorderLayout());
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setDividerLocation(100);
		splitter.setResizeWeight(0.5);

		JScrollPane scroller = new JScrollPane(workflowViewerPanel);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		splitter.add(scroller);

		splitter.add(workflowInfoPanel);
		add(splitter, BorderLayout.CENTER);



	}

	public static void displayCWF() {

		//---------------
		String cwfSeparate = "";//to store the details of the current user transaction
		for (int i = 0; i < currentWorkFlow.size(); i ++) {
			cwfSeparate = cwfSeparate + "\t\t" +
			((TransactionElement) currentWorkFlow.get(i)).getHour() + 
			":" + 
			((TransactionElement) currentWorkFlow.get(i)).getMinute() + 
			":" +
			((TransactionElement) currentWorkFlow.get(i)).getSecond() + 
			" " + 
			((TransactionElement) currentWorkFlow.get(i)).getToolName() + 
			"\n";

		}



		//-----------------
		String cwfString = "";
		for (int i = 0; i < currentWorkFlow.size(); i ++) {
			cwfString = cwfString + ((TransactionElement) currentWorkFlow.get(i)).getToolName() + "," ;
		}

		//first we print out the finished workflow
		//System.out.println("");
		//System.out.println("now we print all finished work flow:");
		for (int i = 0; i < usedWorkFlowToday.size(); i ++) {
			//we get the current finished work flow from the finished WF list
			String tempWF = (String)usedWorkFlowToday.get(i);


			/*
			for (int j = 0; j < tempWF.size(); j ++) {
				System.out.print(((TransactionElement)tempWF.get(j)).getToolName() + " -> ");							  
			}
			 */
			//System.out.println("Old work flow: " + tempWF);						  
		}

		//-----------------show finished work flows today
		String finishedWF = "";
		for (int i = 0; i < usedWorkFlowToday.size(); i ++) {
			String tempWF = (String)usedWorkFlowToday.get(i);

			finishedWF = finishedWF  + tempWF.substring(0, tempWF.length()-3) + "\n";

		}
		if (usedWorkFlowToday.size() == 0) {
			finishedWF = "No finished work flows!";
		}


		boolean statA,statB;
		int statAValue = 0;
		ArrayList <String> statBValue = new ArrayList();
		ArrayList <String> statCValue = new ArrayList();


		//----------------------
		//Now we use cwfString to talk to ISBUServer to get real time suggestion
		try {
			ArrayList RealTimeWFSuggestionResults = getRealTimeWorkFlowSuggestion(cwfString);

			//get statA
			statAValue = ((Integer)RealTimeWFSuggestionResults.get(0)).intValue();
			//System.out.println(statAValue);
			if (statAValue == 0) {
				statA = false;
			}
			else {
				statA = true;
			}

			//get statB
			statBValue = (ArrayList)RealTimeWFSuggestionResults.get(1);
			//System.out.println(statBValue);
			if (statBValue.size() == 0) {
				statB = false;
			}
			else {
				statB = true;
			}

			//get statC
			statCValue = (ArrayList)RealTimeWFSuggestionResults.get(2);
			//System.out.println("this time! Stat C: " + statCValue);

		}
		catch (Exception e) {
			e.printStackTrace();
		}



		String statBDisplay = "";

		for (int i = 0; i < statBValue.size(); i ++) {
			String temp = (String)statBValue.get(i);

			StringTokenizer tokens = new StringTokenizer(temp, "#");

			String superWF = tokens.nextToken();
			String times = tokens.nextToken();

			statBDisplay = statBDisplay + superWF.substring(0, superWF.length()-1) + "   (" + times + " times)\n";

		}
		statBDisplay = statBDisplay.replace(",", " -> ");
		if (statBValue.size() == 0) {
			statBDisplay = "No super work flows!";
		}



		String statCDisplay = "";
		//we need to first go through statCValue once to calculate the total times...
		float totalTimes = 0;
		for (int j = 0; j < statCValue.size(); j++) {
			String temp = (String)statCValue.get(j);

			StringTokenizer tokens = new StringTokenizer(temp, "#");

			tokens.nextToken();
			float times = Float.parseFloat(tokens.nextToken());
			totalTimes += times;
		}

		for (int i = 0; i < statCValue.size(); i ++) {
			String temp = (String)statCValue.get(i);

			StringTokenizer tokens = new StringTokenizer(temp, "#");

			String superWF = tokens.nextToken();
			float times = (float)Integer.parseInt(tokens.nextToken());

			//System.out.println("times: " + times);
			//System.out.println("totalTimes: " + totalTimes);
			float percentageToDisplay = (times / totalTimes) ;

			if (totalTimes == 0) {
				percentageToDisplay = 0;
			}

			//still some problem, we temporarily do not display the percentage
			//statCDisplay = statCDisplay + superWF + "    " + percentageToDisplay * 100 + "% " + "<br>";
			statCDisplay = statCDisplay + superWF + "\n";

		}
		if (statCValue.size() == 0) {
			statCDisplay = "No next steps!";
		}















		/*
		//------
		String finishedWF = "";
		for (int i = 0; i < usedWorkFlowToday.size(); i ++) {
			ArrayList tempWF = (ArrayList)usedWorkFlowToday.get(i);

			for (int j = 0; j < tempWF.size(); j ++) {
				finishedWF = finishedWF +  tempWF.get(j) +  "->";
			}									  
		}
		 */

		infoArea.setText("");
		//infoArea.append("\n\n\n\n\n\n\n");
		infoArea.append("YOUR CURRENT WORK FLOW ACTIVITIES SO FAR: \n");
		infoArea.append(cwfSeparate + "\n\n");
		infoArea.append("THE WORK FLOW YOU HAVE FINISHED TODAY: \n");
		infoArea.append(finishedWF + "\n\n\n");
		//infoArea.append("Your current workflow activity so far: \n" + cwfSeparate + "\n\n");
		//infoArea.append(finishedWF + "\n\n\n");

		infoArea.append("THE FOLLOWING ARE THE SUGGESTIONS FOR YOUR CURRENT WORK FLOW: " + "\n");
		infoArea.append("-----------------------------------------------------------------------\n\n");
		infoArea.append("HISTORICAL TIMES OF USE:" + "\n");
		infoArea.append("Your current work flow has been used " + statAValue + " times in history." + "\n\n");
		infoArea.append("HISTORICAL SUPER FLOWS: " + "\n");
		infoArea.append(statBDisplay + "\n\n");
		infoArea.append("POSSIBLE NEXT STEPS:" + "\n");
		infoArea.append(statCDisplay + "\n\n");






		ArrayList <String> currentWorkflowTools = new ArrayList();
		for (TransactionElement t : currentWorkFlow){
			currentWorkflowTools.add(t.getToolName());
		}

		ArrayList arguments = new ArrayList();
		arguments.add(currentWorkflowTools);
		arguments.add(LoginManager.getLoggedInUser());

		String nextBestRated = ServerRequest.get(RuntimeEnvironmentSettings.ISBU_SERVER, "nextBestRatedTool", arguments).toString();

		if (nextBestRated != null && !nextBestRated.equals("none"))
			infoArea.append("Next best rated tool to use: " + nextBestRated + ".\n\n");
	}







	public static void updateCWFStatus(final int hour, final int minute, final int second, final String toolName, final String transactionID) {

		org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
			public Void doInBackground() {

				//System.out.println(" ");
				//System.out.println("Current Transaction ID: " + transactionID);
				//System.out.println(" ");


				viewerStatus.setText("Recently used " + toolName);
				//first we check that whether we have started a new transaction (a new work flow)
				if (firstTimeFlag == true) { //we are about to begin the first transaction today, now currentTid is still "null", rather than an earlier value
					currentTid = transactionID;
					TransactionElement element = new TransactionElement(hour, minute, second, toolName);


					currentWorkFlow.add(element);




					displayCWF();
					firstTimeFlag = false;
				}
				else {//if this is now the first time...

					//we will compare the incoming id with the "old" one

					if (transactionID.equals(currentTid)) { //we are still in the same transaction, just go ahead

						//System.out.println(" ");
						//System.out.println("Incoming Transaction ID: " + transactionID);
						//System.out.println("current Transaction ID: " + currentTid);
						//System.out.println(" ");

						TransactionElement element = new TransactionElement(hour, minute, second, toolName);

						currentWorkFlow.add(element);
						//System.out.println("Current work flow expanded by one more node!");

						workflowNodePanel.add(new JLabel(arrow));

						displayCWF();

					}
					else { //if we are starting a new transaction (wf)

						//System.out.println(" ");
						//System.out.println("Incoming Transaction ID: " + transactionID);
						//System.out.println("current Transaction ID: " + currentTid);
						//System.out.println("We need to clear the cwf and start a new one. ");

						currentTid = transactionID;

						String cwfString = "";
						for (int i = 0; i < currentWorkFlow.size(); i ++) {
							cwfString = cwfString + ((TransactionElement) currentWorkFlow.get(i)).getToolName() + " -> " ;
						}


						//save the current WF into historical record
						usedWorkFlowToday.add(cwfString); 

						//and then empty it
						currentWorkFlow.clear();
						workflowNodePanel.removeAll();

						//System.out.println("Current work flow cleared!");

						//and then add in new work flow node

						TransactionElement element = new TransactionElement(hour, minute, second, toolName);

						currentWorkFlow.add(element);

						//System.out.println("We add a new starting node to cleared CWF!");
						//System.out.println(" ");

						displayCWF();
					}


				}


				WorkflowViewerPanelNode newNode 
				= new WorkflowViewerPanelNode(toolName, currentWorkFlow.size() - 1);

				newNode.addMouseListener(new MouseListener(){

					//@Override
					public void mouseClicked(MouseEvent event) {
						popup.showToolOptions();
						popup.showToolRating();
						popup.showWorkflowOptions();
						popup.showWorkflowRating();

						WorkflowViewerPanelNode node = (WorkflowViewerPanelNode)event.getSource();

						ArrayList<String> workflow = new ArrayList();
						for (int i = 0; i <= node.getIndex(); i++)
							workflow.add(currentWorkFlow.get(i).getToolName());

						popup.initialize(node.getText(), workflow);
						popup.show(node, event.getX(), event.getY());

					}

					public void mouseEntered(MouseEvent e) {}
					public void mouseExited(MouseEvent e) {	}
					public void mousePressed(MouseEvent e) {}
					public void mouseReleased(MouseEvent e) {}

				});
				workflowNodePanel.add(newNode);


				return null;

			}
		};
		worker.execute();
	}


	private static ArrayList getRealTimeWorkFlowSuggestion(String cwf) throws Exception {

		//System.out.println(" ");
		//System.out.println("Current CWF being sent: " + cwf);
		clientSideID = "RTWFS#getRealTimeWorkFlowSuggestion#" + cwf;//zhu yi kong ge shi zi dai de ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		//System.out.println("client request sent (RTWFS)");

		//waiting for server response...

		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		//System.out.println("waiting response from server (RTWFS).....");
		ArrayList resultList = (ArrayList) ois.readObject();
		//System.out.println("Real Time suggestion Info aquired from ISBU server!!");
		//System.out.println();
		return resultList;

	}





	/*


    public void actionPerformed(ActionEvent e) 
    {
    	if (e.getSource() == save) {
    		//System.out.println("Save pressed with " + group.getSelection().getActionCommand());
    		preference = Integer.parseInt(group.getSelection().getActionCommand());
    		ObjectHandler.setLogStatus(preference);
    		save.setEnabled(false);
    		// write it to the properties file
    		try
    		{
    			PropertiesManager properties = PropertiesManager.getInstance();
    			properties.setProperty(GenSpaceLogPreferences.class, PROPERTY_KEY, group.getSelection().getActionCommand());
    		}
    		catch (Exception ex) { }

    	}
    	else if (e.getSource() == reset) {
    		//System.out.println("Reset pressed");
    		logAnon.setSelected(true);
    		save.setEnabled(true);
    	}
    	else if (e.getSource() == log) {
    		if (preference == 0) {
    			save.setEnabled(false);
    		}
    		else {
    			save.setEnabled(true);
    		}
    	}
    	else if (e.getSource() == logAnon) {
    		if (preference == 1) {
    			save.setEnabled(false);
    		}
    		else {
    			save.setEnabled(true);
    		}
    	}
    	else if (e.getSource() == noLog) {
    		if (preference == 2) {
    			save.setEnabled(false);
    		}
    		else {
    			save.setEnabled(true);
    		}
    	}
    }

	 */



	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}







	public static void main(String args[]){
		JFrame theFrame = new JFrame();
		theFrame.setTitle("Real-time workflow evaluation plugin tester");
		RealTimeWorkFlowSuggestion plugin = new RealTimeWorkFlowSuggestion();
		theFrame.add(plugin);
		plugin.setVisible(true);

		theFrame.setSize(700, 300);
		theFrame.setVisible(true);

		plugin.updateCWFStatus(10, 20, 30, "ARACNE", "ABCDEFG");
		plugin.updateCWFStatus(10, 20, 30, "ARACNE", "ABCDEFG");
		//plugin.updateCWFStatus(10, 20, 30, "T Test Analysis", "ABCDEFG");
	}

}

class WorkflowViewerPanelNode extends JButton {

	private int index;

	public WorkflowViewerPanelNode(String title, int index){
		super(title);
		this.putClientProperty("is3DEnabled", Boolean.FALSE);
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
