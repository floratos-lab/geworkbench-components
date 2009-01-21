package org.geworkbench.components.genspace;

import org.geworkbench.builtin.projects.remoteresources.query.GeWorkbenchCaARRAYAdaptor;
import org.geworkbench.engine.config.*;
import org.jgraph.*;
import org.jgraph.graph.*;
import org.jgraph.event.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.net.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class ISBUWorkFlowVisualization extends JPanel implements VisualPlugin, ActionListener {
	
	// "ALL TOOLS" - control panel
	private JPanel top3ControlPanel = new JPanel();
	private JLabel top3Label = new JLabel("ALL TOOLS: ");
	private JButton top3Button = new JButton("See the top 3 of all tools");	
	
	// "ALL TOOLS" - result panels	
	private JPanel top3Panel = new JPanel();
	private JPanel top3Panel_1 = new JPanel(); 
	private JPanel top3Panel_2 = new JPanel(); 
	private JPanel top3SubPanel1 = new JPanel(); 
	private JPanel top3SubPanel2 = new JPanel();
	private JPanel top3SubPanel3 = new JPanel();	
	private JLabel top3Tool = new JLabel("Top3 Most Popular Tools");
	private JLabel top3WF = new JLabel("Top3 Most Popular Workflows");
	private JLabel top3ToolAsHead = new JLabel("Top3 Most Popular Tools at Start of Workflows");
	private JLabel top1 = new JLabel("1: ");
	private JLabel top2 = new JLabel("2: ");
	private JLabel top3 = new JLabel("3: ");
	private JLabel top11 = new JLabel("1: ");
	private JLabel top22 = new JLabel("2: ");
	private JLabel top33 = new JLabel("3: ");
	private JLabel top111 = new JLabel("1: ");
	private JLabel top222 = new JLabel("2: ");
	private JLabel top333 = new JLabel("3: ");
	
	// AN INDIVISUAL TOOL
	private JPanel singleControlPanel = new JPanel();	
	private JLabel singleLabel = new JLabel("AN INDIVIDUAL TOOL: ");	
	private JPanel singleToolPanel = new JPanel(); 	
	private JPanel subSingleToolPanel = new JPanel(); 	
	private JLabel single1 = new JLabel("Total usage rate: ");
	private JLabel single2 = new JLabel("Total usage rate at start of workflow: ");
	private JLabel single3 = new JLabel("The most popular tool used next to this tool: ");
	private JLabel single4 = new JLabel("The most popular tool used before this tool: ");
	
	// All the above Panels are addded to the northPanel
	private JPanel northPanel = new JPanel(); 
	
	// Workflow Visualizer - control panel
	private JPanel selectPanel = new JPanel();
	private JPanel selectSubPanel = new JPanel();
	private JPanel selectSubPanel2 = new JPanel();
	private JLabel selectLabel = new JLabel("Workflow Visualizer: ");
	private JComboBox tools = new JComboBox();
	private JComboBox actions = new JComboBox();
	private JButton button = new JButton("Search");
	private JCheckBox checkbox = new JCheckBox("My social networks only");
	private JLabel label = new JLabel();
	private JComboBox tools2 = new JComboBox();
	
	// Workflow Visualizer - graph panel
	private JPanel graphPanel = new JPanel();
	private HashMap<String, String> actionKeywords = new HashMap<String, String>();
	private ArrayList<String> workflows;
	private JGraph graph;
	private DefaultGraphCell[] cells;

	public static final String[] NUMBERS = {"No", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten" };

	// Netwroks
	private static final int PORT = RuntimeEnvironmentSettings.WORKFLOW_VIS_SERVER.getPort();
	//private static final String HOST = "bambi.cs.columbia.edu";
	private static final String HOST = RuntimeEnvironmentSettings.WORKFLOW_VIS_SERVER.getHost();	
	private String login = "chris"; // TODO: WHERE DOES THIS COME FROM???	
	
	private static String clientSideID = null; //K
	private static String serverIP = RuntimeEnvironmentSettings.ISBU_SERVER.getHost();
	private static int serverPort = RuntimeEnvironmentSettings.ISBU_SERVER.getPort(); //K
	private ArrayList top3ToolString; //K
	private ArrayList top3ToolAsHeadString; //K
	private ArrayList top3WFString; //K
	
	ArrayList <String> allAnalysisTools = null; //K	
	ArrayList <String> allAnalysisTools2 = null; //K
	
	public ISBUWorkFlowVisualization()
	{
		System.out.println("Workflow Visualization started");

		initComponents();
	}


	private void initComponents()
	{
		setLayout(new BorderLayout());
		tools.addItem("-- select tool --");
		tools2.addItem("-- select tool --");
		
		// TODO: maybe do this in a different thread so that it doesn't hold up the rest of the app
		/* K added (START)*/
		try{
			allAnalysisTools = getAllAnalysisTools();	
			
			// TODO: maybe do this in a different thread so that it doesn't hold up the rest of the app
			ArrayList<String> allTools = allAnalysisTools;
			for (String toolEach : allTools)
				tools.addItem(toolEach);
		}
		catch(Exception ex1){
			
		}		
		try{
			allAnalysisTools2 = getAllAnalysisTools();	
			
			// TODO: maybe do this in a different thread so that it doesn't hold up the rest of the app
			ArrayList<String> allTools = allAnalysisTools2;
			for (String toolEach : allTools)
				tools2.addItem(toolEach);
		}
		catch(Exception ex2){
			
		}
		/* K added (END)*/
		
		/* K added (START)-singleTool*/
		tools.addItemListener(
        		new ItemListener() {
        			public void itemStateChanged(ItemEvent event) {        				
        				try {        					
        					//selectedIndex = toolComboBox.getSelectedIndex();
            				String toolBeingRequested = tools.getSelectedItem().toString();
            				System.out.println("(((((((((( addItemLister" + tools.toString());
            				//labelIndex.setText((String)allAnalysisTools.get(selectedIndex));
            				
            				System.out.println("SELECTED$$$$" + toolBeingRequested);
            				
            				if (toolBeingRequested.equals("-- select tool --")) {
            					single1.setText("Total usage rate: ");    					
            					
            					single2.setText("Total usage rate at start of workflow: ");
            					
            					single3.setText("The most popular tool used next to this tool: ");
            					
            					single4.setText("The most popular tool used before this tool: ");
            					return;
            					
            				}
            				else
            				{ 
            					//now we talk to the server
                				String usageRate = getUsageRate(toolBeingRequested);
                				single1.setText("Total usage rate: " + usageRate);                				
                				
                				String usageRateAsWFHead = getUsageRateAsWFHead(toolBeingRequested);
                				single2.setText("Total usage rate at start of workflow: " + usageRateAsWFHead);
                				
                				String mostPopularNextTool = getMostPopularNextTool(toolBeingRequested);
                				single3.setText("The most popular tool used next to this tool: " + mostPopularNextTool);
                				
                				String mostPopularPreviousTool = getMostPopularPreviousTool(toolBeingRequested);
                				single4.setText("The most popular tool used before this tool: " + mostPopularPreviousTool);
                				
                				//workflows = getTop3MostPopularWFForThisTool(toolBeingRequested);
                				//labelNC20.setText(top3WFForThisTool.get(0) + " and " + top3WFForThisTool.get(1) + " and " + top3WFForThisTool.get(2));
                				           					
            				}            
        				}
        				catch (Exception e) {
        					e.printStackTrace();
        				}       			
        			}
        		}
        );
		/* K added (END)-singleTool*/
		

		actions.addItem("-- select action --");
		actions.addItem("Most common workflow starting with");
		actions.addItem("Most common workflow including");
		actions.addItem("All workflows including");

		actionKeywords.put("Most common workflow starting with", "START");
		actionKeywords.put("Most common workflow including", "INCLUDE");
		actionKeywords.put("All workflows including", "ALL");
		
		checkbox.setBackground(Color.LIGHT_GRAY);
		selectSubPanel2.add(checkbox);
		selectSubPanel2.add(actions);
		selectSubPanel2.add(tools2);
		button.addActionListener(this);
		
		selectLabel.setFont(new Font("Courier New", Font.BOLD, 20));
		selectSubPanel.setBackground(Color.LIGHT_GRAY);
		selectSubPanel.add(selectLabel);
		selectSubPanel2.setBackground(Color.LIGHT_GRAY);
		selectSubPanel2.add(button);
		
		selectSubPanel2.add(label);
		selectPanel.setLayout(new BoxLayout (selectPanel, BoxLayout.Y_AXIS));
		selectPanel.add(selectSubPanel);
		selectPanel.add(selectSubPanel2);
		
		
		top3Panel.setLayout(new BoxLayout (top3Panel, BoxLayout.Y_AXIS)); //K		
		//top3SubPanel1.setBackground(Color.RED); //K
		
		
		top3ControlPanel.setBackground(Color.LIGHT_GRAY);
		top3Button.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent topE)
					{						
					 	/* K added START*/
				    	//KTop3 top3 = new KTop3();
				    	top3ToolString = new ArrayList();
				    	try{
				    		top3ToolString = getTop3MostPopularTools();
				        	for(int i = 0; i < 3; i++){
				        		System.out.println("****************");    		
				        		System.out.println(top3ToolString.get(i));
				        		System.out.println("****************");  
				        		if(i == 0)
				        			top1.setText("1: "+ (String)top3ToolString.get(i));
				        		if(i == 1)
				        			top2.setText("2: "+ (String)top3ToolString.get(i));
				        		if(i == 2)
				        			top3.setText("3: "+ (String)top3ToolString.get(i));
				        	}
				    	}
				    	catch(Exception except){
				    		
				    	}				    	
				    	top3ToolAsHeadString = new ArrayList();
				    	try{
				    		top3ToolAsHeadString = getTop3MostPopularWFHead();
				        	for(int i = 0; i < 3; i++){
				        		System.out.println("****************");    		
				        		System.out.println((String)top3ToolString.get(i));				        		
				        		System.out.println("****************");  
				        		if(i == 0)
				        			top11.setText("1: "+ (String)top3ToolAsHeadString.get(i));
				        		if(i == 1)
				        			top22.setText("2: "+ (String)top3ToolAsHeadString.get(i));
				        		if(i == 2)
				        			top33.setText("3: "+ (String)top3ToolAsHeadString.get(i));
				        	}
				    	}
				    	catch(Exception except){
				    		
				    	}				    	
				    	top3WFString = new ArrayList();
				    	try{
				    		top3WFString = getTop3MostPopularWF();
				    		System.out.println(top3WFString.size());
				    		//top111.setText("1: "+ (String)top3WFString.get(0));
				    		//top222.setText("2: "+ (String)top3WFString.get(1));
				    		//top333.setText("3: "+ (String)top3WFString.get(2));
				    		System.out.println((String)top3WFString.get(0));
				    		System.out.println((String)top3WFString.get(1));
				    		System.out.println((String)top3WFString.get(2));
				    		String s3 = (String)top3WFString.get(2);
				    		
				        	for(int i = 0; i < 3; i++){
				        		System.out.println("****************");    		
				        		//System.out.println(top3WFString.get(i));
				        		System.out.println("****************");  
				        		if(i == 0){
				        			System.out.println("No1No1No1No1");
				        			top111.setText("1: "+ (String)top3WFString.get(i));
				        		}				        		
				        		if(i == 1){
				        			System.out.println("No2No2No2No2");
				        			top222.setText("2: "+ (String)top3WFString.get(i));
				        		} 						 //ARACNE,MRA Analysis,MRA Analysis,T Test Analysis,T Test Analysis,T Test Analysis,T Test Analysis,
				        		// "123456789 123456789 123456789 123456789 123456789 123456789"
				        		if(i == 2){
				        			System.out.println("No3No3No3No3" + s3);
				        			top333.setText("3: "+ s3);
				        		}        		
				        		
				        			
				        	}
				        	
				    	}
				    	catch(Exception except){
				    		System.out.println("$$$$$$$$$$$$$$$$Exception");
				    		except.printStackTrace();
				    	}				    	
				    	/* Koichrio added END*/
					}
				}
	    );
		top3Label.setFont(new Font("Courier New", Font.BOLD, 20));
		top3Label.setForeground(Color.RED);
	    
		top3ControlPanel.setLayout(new FlowLayout() );
		
		top3ControlPanel.add(top3Label);
		top3ControlPanel.add(top3Button);
		
		singleControlPanel.setBackground(Color.LIGHT_GRAY);
		singleControlPanel.setLayout(new FlowLayout() );
		singleLabel.setFont(new Font("Courier New", Font.BOLD, 20));
		singleLabel.setForeground(Color.BLUE);
		singleControlPanel.add(singleLabel);
		singleControlPanel.add(tools);
				
		top3SubPanel1.setBackground(Color.PINK); //K
		top3SubPanel2.setBackground(Color.PINK); //K
		top3SubPanel3.setBackground(Color.PINK); //K
		top3SubPanel1.setBorder(new TitledBorder(new EtchedBorder(), ""));
		top3SubPanel2.setBorder(new TitledBorder(new EtchedBorder(), ""));
		top3SubPanel3.setBorder(new TitledBorder(new EtchedBorder(), ""));
		
		
		top3Tool.setFont(new Font("Courier New", Font.BOLD, 12));
		top3SubPanel1.setLayout(new BoxLayout (top3SubPanel1, BoxLayout.Y_AXIS));
		top3SubPanel1.add(top3Tool);		
		top3SubPanel1.add(top1);
		top3SubPanel1.add(top2);
		top3SubPanel1.add(top3);		
		
		top3ToolAsHead.setFont(new Font("Courier New", Font.BOLD, 12));
		top3SubPanel2.setLayout(new BoxLayout (top3SubPanel2, BoxLayout.Y_AXIS));
		top3SubPanel2.add(top3ToolAsHead);
		top3SubPanel2.add(top11);
		top3SubPanel2.add(top22);
		top3SubPanel2.add(top33);
		
		top3WF.setFont(new Font("Courier New", Font.BOLD, 12));
		top3SubPanel3.setLayout(new BoxLayout (top3SubPanel3, BoxLayout.Y_AXIS));
		top3SubPanel3.add(top3WF);
		top3SubPanel3.add(top111);
		top3SubPanel3.add(top222);
		top3SubPanel3.add(top333);

		top3Panel_1.add(top3SubPanel1);
		top3Panel_1.add(top3SubPanel2);
		top3Panel_2.add(top3SubPanel3);
		top3Panel.add(top3Panel_1);
		top3Panel.add(top3Panel_2);
		
		subSingleToolPanel.setBorder(new TitledBorder(new EtchedBorder(), ""));
		subSingleToolPanel.setBackground(new Color(149, 174, 226));
		subSingleToolPanel.setLayout(new BoxLayout (subSingleToolPanel, BoxLayout.Y_AXIS));
		//singleToolPanel.add(singleTool);
		single1.setFont(new Font("Courier New", Font.BOLD, 12));
		single2.setFont(new Font("Courier New", Font.BOLD, 12));
		single3.setFont(new Font("Courier New", Font.BOLD, 12));
		single4.setFont(new Font("Courier New", Font.BOLD, 12));
		subSingleToolPanel.add(single1);
		subSingleToolPanel.add(single2);
		subSingleToolPanel.add(single3);
		subSingleToolPanel.add(single4);
		
		singleToolPanel.setLayout(new FlowLayout());
		singleToolPanel.add(subSingleToolPanel);			
		
		northPanel.setLayout(new BoxLayout (northPanel, BoxLayout.Y_AXIS)); // K		
		northPanel.add(top3ControlPanel); //K
		northPanel.add(top3Panel); // K		
		northPanel.add(singleControlPanel); //K
		northPanel.add(singleToolPanel); //K		
		northPanel.add(selectPanel); // K
				
		setBackground(Color.CYAN);
		add(northPanel, BorderLayout.NORTH); //K		
		add(graphPanel, BorderLayout.CENTER);
		
	}


	/**
	 * Connects to the server and gets a list of all the analysis tools
	 */
	private ArrayList<String> getAllTools()
	{
		ArrayList<String> allTools = new ArrayList<String>();

    	// connect to the server and get the info we need
    	PrintWriter out = null;
    	Socket s = null;
    	try
    	{
    		s = new Socket(HOST, PORT);
    		out = new java.io.PrintWriter(s.getOutputStream());

    		// send the action keyword and the name of the tool
    		out.write("TOOLS\n");
       		out.flush();

    		// read in the response and store the values in an ArrayList
    		Scanner in = new Scanner(s.getInputStream());
    		while (in.hasNext())
    		{
    			String line = in.nextLine();
    			System.out.println(line);
    			if (line.equals("END")) break;
    			allTools.add(line);
    		}
    	}
    	catch (Exception ex)
    	{
    		ex.printStackTrace();
    		// TODO: handle the error more gracefully
    	}
    	finally
    	{
    		try { out.close(); } catch (Exception ex) { }
    		try { s.close(); } catch (Exception ex) { }
    	}
    	return allTools;
	}


	
	
	
	
	public void actionPerformed(ActionEvent e)
    {
    	// get the name of the selected tool and the action
    	String tool = tools2.getSelectedItem().toString();    	
    	String action = actions.getSelectedItem().toString();
    	
    	System.out.println("tool:********************" + tool);
    	System.out.println("tool:********************" + action);
    	if(tool.equals("-- select tool --") || action.equals("-- select action --")){
    		
    	} else {
    		// to store the workflows that come back from the server
        	workflows = new ArrayList<String>();


        	// connect to the server and get the info we need
        	PrintWriter out = null;
        	Socket s = null;
        	try
        	{
        		s = new Socket(HOST, PORT);
        		out = new java.io.PrintWriter(s.getOutputStream());

        		// send the action keyword and the name of the tool
        		out.write(actionKeywords.get(action) + "\n");
        		out.write(tool + "\n");
        		// send the username and whether or not to limit to that user's networks
        		out.println(login);
        		out.println(checkbox.isSelected());
        		out.flush();

        		// read in the response and store the values in an ArrayList
        		Scanner in = new Scanner(s.getInputStream());
        		while (in.hasNext())
        		{
        			String line = in.nextLine();
        			System.out.println(line);
        			if (line.equals("END")) break;
        			System.out.println("AAAAAAAAAAA" + line);
        			if (!line.equals("null")) {
        				workflows.add(line);
        			}
        			
        			
        		}
        	}
        	catch (Exception ex)
        	{
        		ex.printStackTrace();
        		// TODO: handle the error more gracefully
        	}
        	finally
        	{
        		try { out.close(); } catch (Exception ex) { }
        		try { s.close(); } catch (Exception ex) { }
        	}

        	System.out.println("the work flows acquired from chris' server this time: " + workflows);


        	// make sure we got some results!
        	if (workflows.size() == 0)
        	{
        		// no results came back!
        		JOptionPane.showMessageDialog(this, "There are no workflows matching that criteria");
        		return;
        	}

        	// update the status
        	String noun = "workflow";
        	if (workflows.size() > 1) noun = "workflows";
        	label.setText(workflows.size() + " " + noun + " found");

        	// different method calls to handle the different types of return values
        	if (action.equals("All workflows including"))
        	{
        		HashMap<String, Node> nodeMap = new HashMap<String, Node>();
        		ArrayList<Edge> edges = new ArrayList<Edge>();

        		// go through the workflows and rip out the individual parts
        		for (String workflow : workflows)
        		{
        			String[] n = workflow.split(",");
        			for (int i = 0; i < n.length; i++)
        			{
        				// add the individual node, but only if it's not already there
        				if (nodeMap.keySet().contains(n[i]) == false)
        				{
        					nodeMap.put(n[i], new Node(n[i]));
        				}
        				// update the position
        				nodeMap.get(n[i]).addPosition(i);

        				// mark the starting node
        				if (i == 0) nodeMap.get(n[i]).isStart = true;

        				// create an edge between this one and the next, only if the edge doesn't already exist
        				if (i < n.length - 1)
        				{
        					Edge edge = new Edge(n[i], n[i+1]);
        					if (edges.contains(edge) == false)
        					{
        						edges.add(edge);
        					}
        				}
        			}
        		}

        		/*
        		for (Node node : nodeMap.values())
        		{
        			System.out.println(node.value + " " + node.avgPos);
        		}
        		*/


        		// now sort the nodes based on their average places in the workflows
        		Node[] nodes = nodeMap.values().toArray(new Node[nodeMap.size()]);

        		// when in doubt, use selection sort!
        		for (int i = 0; i < nodes.length-1; i++)
        		{
        			double min = nodes[i].avgPos;
        			int minIndex = i;

        			for (int j = i; j < nodes.length; j++)
        			{
        				if (nodes[j].avgPos < min)
        				{
        					min = nodes[j].avgPos;
        					minIndex = j;
        				}
        			}

        			Node temp = nodes[i];
        			nodes[i] = nodes[minIndex];
        			nodes[minIndex] = temp;
        		}

        		/*
        		System.out.println("AFTER SORTING");
        		for (Node node : nodes)
        		{
        			System.out.println(node.value + " " + node.avgPos);
        		}
        		*/

        		// finally, we draw the thing
        		draw(nodes, edges.toArray(new Edge[edges.size()]), tool);
        	}
        	else
        	{
        		// if it's not all workflows, then it's just one, which means we just have a single entry in the arraylist
        		String workflow = workflows.get(0);
        		// separate all the names
        		String[] nodeNames = workflow.split(",");
        		// create an array of Nodes
        		Node[] nodes = new Node[nodeNames.length];

        		// populate the array of Nodes
        		for (int i = 0; i < nodes.length; i++)
        		{
        			nodes[i] = new Node(nodeNames[i]);
        		}

        		// mark the starting node
        		nodes[0].isStart = true;

        		// draw!
        		draw(nodes, tool);
       		}
    	}
    	
    }    
    
    public Component getComponent() {
        // In this case, this object is also the GUI component.
        return this;
    }
    
    // K added
    private static ArrayList getTop3MostPopularTools() throws Exception {
    	
		//now we test the method "getTop3MostPopularTools"
		clientSideID = "getTop3MostPopularTools";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		System.out.println("response from server received: Top 3 most Pop Tools: " + listBack.toString());
		System.out.println();
		return listBack;
	}
    
    // K added
    private static ArrayList getTop3MostPopularWFHead() throws Exception {
		
		//now we test the method "getTop3MostPopularWFHead"
		clientSideID = "getTop3MostPopularWFHead";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		System.out.println("response from server received: top 3 most Pop WF heads: " + listBack.toString());
		System.out.println();
		return listBack;
	}
    
    // K added
	private static ArrayList getTop3MostPopularWF() throws Exception {
		
		//now we test the method "getTop3MostPopularWF"
		clientSideID = "getTop3MostPopularWF";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		System.out.println("@@@@@response from server received: top 3 most Pop WFs: " + listBack.toString());
		System.out.println();
		return listBack;
	}
	
	// K added from SuggestionBasedOnSingleTool
	private static ArrayList getAllAnalysisTools() throws Exception {
		
		//now we test the method "getAllAnalysisTools"
		clientSideID = "getAllAnalysisTools";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		System.out.println("response from server received: All Analysis Tools: " + listBack.toString());
		System.out.println();
		return listBack;
	}
	
	// K added from SuggestionBasedOnSingleTool
	private static String getUsageRate(String toolApplied) throws Exception {
		
		clientSideID = "feature2,getUsageRate," + toolApplied;//zhu yi kong ge shi zi dai de ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		System.out.println("response from server received: usage rate for this tool: " + resultValue);
		System.out.println();
		return resultValue;
		
	}
	
	// K added from SuggestionBasedOnSingleTool
	private static String getUsageRateAsWFHead(String toolApplied) throws Exception {
		
		clientSideID = "feature2,getUsageRateAsWFHead," + toolApplied;//zhu yi kong ge shi zi dai de ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		System.out.println("response from server received: usage rate for this tool as WF head: " + resultValue);
		System.out.println();
		return resultValue;
	}
	
	// K added from SuggestionBasedOnSingleTool	
	private static String getMostPopularNextTool(String toolApplied) throws Exception {
		
		clientSideID = "feature2,getMostPopularNextTool," + toolApplied;//zhu yi kong ge shi zi dai de ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		System.out.println("response from server received: most Pop next tool of this tool: " + resultValue);
		System.out.println();
		return resultValue;
	}
	
	// K added from SuggestionBasedOnSingleTool	
	private static String getMostPopularPreviousTool(String toolApplied) throws Exception {
		
		clientSideID = "feature2,getMostPopularPreviousTool," + toolApplied;//zhu yi kong ge shi zi dai de ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		System.out.println("client request sent");
		
		//waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		System.out.println("response from server received: most Pop previous tool of this tool: " + resultValue);
		System.out.println();
		return resultValue;
	}
	
	
    /**
     * Method to draw the graph when we only have a single sequence of nodes and not an entire tree
     */
	public void draw(Node[] nodes, String tool)
	{
		draw(nodes, null, tool);
	}


	/**
	 * This version will be used for when we want to draw an entire tree.
	 */
	public void draw(Node[] nodes, Edge[] edges, String target)
	{
		// general setup stuff
		GraphModel model = new DefaultGraphModel();
		GraphLayoutCache view = new GraphLayoutCache(model, new DefaultCellViewFactory());
		graph = new JGraph(model, view);

		// figure out the size of the Cell array
		int size = 0;
		if (edges == null) size = nodes.length * 2 - 1;
		else size = nodes.length + edges.length;
		cells = new DefaultGraphCell[size];

		// to keep track of the mapping between node numbers and the names
		HashMap<String, Integer> map = new HashMap<String, Integer>();


		// get the height and width of the window so we can show the components
		int height = graphPanel.getHeight() - 100; // subtract 100 to have a little buffer
		int width = graphPanel.getWidth();
		// the average width per node
		double avgWidth = ((double)width)/nodes.length;

		// create the Nodes
		int count = 0;
		for (int i = 0; i < nodes.length; i++)
		{
			String node = nodes[i].value;

			// figure out the factor to multiply by the height
			double factor = (edges == null) ? 0.5 : Math.random();

			// figure out which color - default is gray
			Color myColor = Color.gray;
			if (node.equals(target)) myColor = Color.yellow;
			//System.out.println("Node is " + node + "; target is " + target);
			//else if (nodes[i].isStart) color = Color.green;
			//else if (count == nodes.length - 1) color = Color.red;

			int myHeight = (node.equals(target)) ? 60 : 40;
			// use the number of characters to figure out the width
			int minWidth = 80;
			int tempWidth = node.trim().length() * 7; // figure 7 pixels per character
			int myWidth = (tempWidth < minWidth) ? minWidth : tempWidth;
			if (node.equals(target)) myWidth *= 1.5;
			cells[count] = createNode(node, new Rectangle2D.Double(avgWidth * count + 40, height * factor, myWidth, myHeight), myColor);
			map.put(node, count);
			// increment the counter
			count++;
		}

		// create the Edges
		if (edges == null)
		{
			// this is for the case when we don't have a tree, just a sequence of nodes
			int max = count-1; // the maximum node number
			for (; count < size; count++)
			{
				cells[count] = createEdge(cells[count-max-1], cells[count-max]);
			}
		}
		else
		{
			// this is for the case when we have a full tree
			for (Edge edge : edges)
			{
				// get the source and destination from the Edge array, using the map
				int src = map.get(edge.src);
				int dest = map.get(edge.dest);
				cells[count] = createEdge(cells[src], cells[dest]);
				count++;
			}
		}

		// to handle any changes to the graph
		GraphListener listener = new GraphListener(this);
		graph.getModel().addGraphModelListener(listener);
		graph.getSelectionModel().addGraphSelectionListener(listener);

		// load up the graph
		graph.getGraphLayoutCache().insert(cells);

		remove(graphPanel);
		graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(graph, BorderLayout.CENTER);
		add(graphPanel, BorderLayout.CENTER);
		graphPanel.setVisible(true);

		validate();
		repaint();
	}

	private DefaultEdge createEdge(DefaultGraphCell source, DefaultGraphCell target)
	{
		GraphEdge edge = new GraphEdge(); //new DefaultEdge(source.getUserObject() + ":" + target.getUserObject());
		edge.setSource(source.getChildAt(0));
		edge.setTarget(target.getChildAt(0));
		edge.sourceNode = source.getUserObject().toString();
		edge.destNode = target.getUserObject().toString();
		int arrow = GraphConstants.ARROW_CLASSIC;
		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
		GraphConstants.setEndFill(edge.getAttributes(), true);
		return edge;
	}

	private DefaultGraphCell createNode(String label, Rectangle2D bounds, Color color)
	{
		GraphNode cell = new GraphNode(label, color);
		GraphConstants.setBounds(cell.getAttributes(), bounds);
		GraphConstants.setGradientColor(cell.getAttributes(), color);
		GraphConstants.setOpaque(cell.getAttributes(), true);
		GraphConstants.setAutoSize(cell.getAttributes(), false);
		GraphConstants.setResize(cell.getAttributes(), false);
		GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
		DefaultPort port0 = new DefaultPort();
		cell.add(port0);
		return cell;

	}


	class Edge
	{
		String src, dest;

		public Edge(String a, String b)
		{
			src = a;
			dest = b;
		}

		public boolean equals(Object o)
		{
			if (o instanceof Edge)
			{
				Edge other = (Edge)o;
				return other.src.equals(src) && other.dest.equals(dest);
			}
			else
				return false;
		}
	}

	class Node
	{
		// the value to be displayed in the node
		String value;
		// the total sum of the position numbers for this node
		private int totalPos = 0;
		// the number of workflows in which this node appears
		private int workflows = 0;
		// the average position
		double avgPos;
		// whether it's a starting node
		boolean isStart = false;

		public Node (String v)
		{
			value = v;
		}

		public void addPosition(int p)
		{
			totalPos += p;
			workflows++;
			avgPos = ((double)totalPos)/workflows;
		}
	}


	class GraphListener implements GraphModelListener, GraphSelectionListener
	{
		JPanel parent;

		public GraphListener(JPanel p)
		{
			parent = p;
		}

		// stores the edges that have been highlighted in the graph
		ArrayList<GraphEdge> highlightedEdges = new ArrayList<GraphEdge>();
		// stores the nodes that have been highlighted in the graph
		ArrayList<GraphNode> highlightedNodes = new ArrayList<GraphNode>();

		/**
		 * This method will be called when the whole graph is changed, like if a node gets moved
		 */
		public void graphChanged(GraphModelEvent e)
		{
			//System.out.println("GRAPH CHANGED! " + e.toString());
		}

		// stores the time when "valueChanged" was last called
		private long lastChange = 0;

		/**
		 * This method is called when a node or edge is simply selected
		 */
		public void valueChanged(GraphSelectionEvent e)
		{
			// make sure we don't register a double-click
			long now = System.currentTimeMillis();
			if (now - lastChange < 20) return;
			else lastChange = now;

			// get the thing that was changed
			Object o = e.getCell();

			// if they click on a node, highlight all workflows going through that node
			if (o instanceof GraphNode)
			{
				GraphNode selectedNode = (GraphNode)o;

				// if it's highlighted already, unhighlight it
				if (highlightedNodes.contains(selectedNode))
					highlightedNodes.remove(selectedNode);
				else
					highlightedNodes.add(selectedNode);
				/*
				if (selectedNode.highlighted)
				{
					selectedNode.highlighted = false;
					highlightedNodes.remove(selectedNode);
				}
				else
				{
					selectedNode.highlighted = true;
					highlightedNodes.add(selectedNode);
				}
				*/


				// remove all the highlighted edges
				graph.getGraphLayoutCache().remove(highlightedEdges.toArray(new GraphEdge[highlightedEdges.size()]));

				// store the Edges
				ArrayList<Edge> workflowEdges = new ArrayList<Edge>();

				// loop through all the workflows and all the highlighted nodes to get the set of edges
				int count = 0;
				for (String workflow : workflows)
				{
					// see if it contains the nodes that have been highlighted
					if (contains(workflow, highlightedNodes))
					{
						count++;
						// if so, get all the edges for that workflow
						String[] nodes = workflow.split(",");
						for (int i = 0; i < nodes.length; i++)
						{
		    				// create an edge between this one and the next, only if the edge doesn't already exist
		    				if (i < nodes.length - 1)
		    				{
		    					Edge edge = new Edge(nodes[i], nodes[i+1]);
		    					if (workflowEdges.contains(edge) == false)
		    					{
		    						workflowEdges.add(edge);
		    					}
		    				}
						}
					}
				}

				// put together the String with the list of all the selected nodes
				String highlighted = "";
				if (highlightedNodes.size() == 1) highlighted = highlightedNodes.get(0).getUserObject().toString();
				else if (highlightedNodes.size() == 2)
					highlighted = highlightedNodes.get(0).getUserObject() + " and " + highlightedNodes.get(1).getUserObject();
				else
				{
					for (int i = 0; i < highlightedNodes.size(); i++)
					{
							if (i > 0) highlighted += ", ";
							if (i == highlightedNodes.size() - 1) highlighted += "and ";
							highlighted += highlightedNodes.get(i).getUserObject();
					}
				}
				// TODO: alphabetize them?
				System.out.println("highlighted=" + highlighted);


				// update the status
				if (highlightedNodes.isEmpty())
				{
					label.setText("Please select tools in the graph to refine the workflows");
				}
				else if (count == 0)
				{
					label.setText("No workflows were found including the set of selected tools (" + highlighted + "). Please choose another set of tools");
				}
				else if (count == 1)
				{
					String text = "One workflow found including " + highlighted;
					label.setText(text);
				}
				else
				{
					String word = null;
					if (count < NUMBERS.length)
						word = NUMBERS[count];
					else
						word = Integer.toString(count);
					label.setText(word + " workflows found including " + highlighted);
				}


				// if there are no workflows for the selected nodes, then just "reset" everything
				// this should be done AFTER the part that prints out the status message, though
				if (workflowEdges.isEmpty())
				{
					highlightedNodes = new ArrayList<GraphNode>();
				}


				// temp variable that is needed to force a "refresh" of the graph
				GraphEdge temp = null;

				// get all the objects in the graph - nodes and edges
				Object[] views = graph.getGraphLayoutCache().getCells(false, true, false, true);
				// now loop through them to update
				for (Object cell : views)
				{
					// if it's an edge, we figure out whether we want to highlight it
					if (cell instanceof GraphEdge)
					{
						GraphEdge edge = (GraphEdge)cell;
						// System.out.println("edge: " + edge.sourceNode + " " + edge.destNode);

						// see if it's in the list of edges for these workflows
						if (workflowEdges.contains(new Edge(edge.sourceNode, edge.destNode)))
						{
							// make a copy of the edge
							GraphEdge newEdge = (GraphEdge)(edge.clone());
							// make the line bold
							GraphConstants.setLineWidth(newEdge.getAttributes(), 5);
							// add it to the graph... note that this just lays it on top of the existing one
							graph.getGraphLayoutCache().insertEdge(newEdge, edge.getSource(), edge.getTarget());
							// add it to the list of highlighted edges, so it can be removed later
							highlightedEdges.add(newEdge);
						}
						temp = edge;
					}
					// if it's a node, we might need to change its color
					else if (cell instanceof GraphNode)
					{
						GraphNode theCell = (GraphNode)cell;

						/*
						// if there are no workflows, then un-highlight any cell
						if (workflowEdges.isEmpty())
						{
							theCell.highlighted = false;
						}
						*/

						// now determine how to color the cells
						if (highlightedNodes.contains(theCell))
						{
							GraphConstants.setGradientColor(theCell.getAttributes(), Color.blue);
						}
						// otherwise, set it back to its original color
						else
						{
							GraphConstants.setGradientColor(theCell.getAttributes(), theCell.color);
							//System.out.println("reset " + theCell.getUserObject());
						}

					}
				}

				/*
				 * This is a bit of a hack, but I "save" one of the edges to display until after everything is
				 * done, because this seems to cause a refresh whereas simply changing the cell colors does NOT
				 * automatically refresh the entire graph
				 */
				graph.getGraphLayoutCache().insertEdge(temp, temp.getSource(), temp.getTarget());


			}
			/*
			// if they click on an edge... though a DefaultEdge *is* a DefaultGraphCell, so this would need to move up!!!!!
			else if (o instanceof DefaultEdge)
				System.out.println("clicked on " + ((DefaultEdge)o).getUserObject());
			*/
		}

		/**
		 * A helper method to make sure that the String argument contains all of the Strings
		 * stored in the ArrayList
		 */
		private boolean contains(String workflow, ArrayList<GraphNode> nodes)
		{
			if (nodes.isEmpty()) return false;
			for (GraphNode node : nodes)
				if (workflow.contains(node.getUserObject().toString()) == false) return false;
			return true;
		}
	}

	class GraphEdge extends DefaultEdge
	{
		String sourceNode, destNode;
	}

	class GraphNode extends DefaultGraphCell
	{
		Color color;
		//boolean highlighted = false;

		public GraphNode (String label, Color c)
		{
			super(label);
			color = c;
		}
	}

}

