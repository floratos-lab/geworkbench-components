package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.SwingWorker;

public class ISBUWorkFlowVisualization extends JPanel implements VisualPlugin,
Runnable {

	private Log log = LogFactory.getLog(this.getClass());

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
	private JLabel top3ToolAsHead = new JLabel(
	"Top3 Most Popular Tools at Start of Workflows");
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
	private JLabel single2 = new JLabel(
	"Total usage rate at start of workflow: ");
	private JLabel single3 = new JLabel(
	"The most popular tool used next to this tool: ");
	private JLabel single4 = new JLabel(
	"The most popular tool used before this tool: ");
	private JLabel message = new JLabel("*The ranking of tools and workflows in the \"ALL TOOLS\" section is based on an exponential time-decay function.");
	private JLabel message1 = new JLabel("The \"INDIVIDUAL TOOL\" section shows the raw data for a tool usage, which is not exponentially weighted.");

	// All the above Panels are addded to the northPanel
	private JPanel northPanel = new JPanel();
	
	//JPanel to display the disclaimer message
	private JPanel messagePanel = new JPanel();

	private JComboBox tools = new JComboBox();

	// Netwroks
	private static final int PORT = RuntimeEnvironmentSettings.WORKFLOW_VIS_SERVER
	.getPort();
	private static final String HOST = RuntimeEnvironmentSettings.WORKFLOW_VIS_SERVER
	.getHost();
	private String login = "chris"; // TODO: WHERE DOES THIS COME FROM???

	private static String clientSideID = null; // K
	private static String serverIP = RuntimeEnvironmentSettings.ISBU_SERVER
	.getHost();
	private static int serverPort = RuntimeEnvironmentSettings.ISBU_SERVER
	.getPort(); // K
	private ArrayList top3ToolString; // K
	private ArrayList top3ToolAsHeadString; // K
	private ArrayList top3WFString; // K

	ArrayList<String> allAnalysisTools = null; // K

	// ArrayList <String> allAnalysisTools2 = null; //K

	public ISBUWorkFlowVisualization() {
		log.debug("Workflow Visualization started");
	}

	public void run() {
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		tools.addItem("-- select tool --");

		// TODO: maybe do this in a different thread so that it doesn't hold up
		// the rest of the app
		/* K added (START) */
		try {
			allAnalysisTools = getAllAnalysisTools();

			// TODO: maybe do this in a different thread so that it doesn't hold
			// up the rest of the app
			ArrayList<String> allTools = allAnalysisTools;
			for (String toolEach : allTools)
				tools.addItem(toolEach);
		} catch (Exception ex1) {

		}

		/* K added (END) */

		/* K added (START)-singleTool */
		tools.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {

				javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<Void, Void>() {
					public Void doInBackground() {

						try {
							// selectedIndex = toolComboBox.getSelectedIndex();
							String toolBeingRequested = tools.getSelectedItem()
							.toString();
							// System.out.println("(((((((((( addItemLister" +
							// tools.toString());
							// labelIndex.setText((String)allAnalysisTools.get(selectedIndex));

							// System.out.println("SELECTED$$$$" + toolBeingRequested);

							if (toolBeingRequested.equals("-- select tool --")) {
								single1.setText("Total usage rate: ");

								single2
								.setText("Total usage rate at start of workflow: ");

								single3
								.setText("The most popular tool used next to this tool: ");

								single4
								.setText("The most popular tool used before this tool: ");
								return null;

							} else {
								// now we talk to the server
								String usageRate = getUsageRate(toolBeingRequested);
								single1.setText("Total usage rate: " + usageRate);

								String usageRateAsWFHead = getUsageRateAsWFHead(toolBeingRequested);
								single2
								.setText("Total usage rate at start of workflow: "
										+ usageRateAsWFHead);

								String mostPopularNextTool = getMostPopularNextTool(toolBeingRequested);
								single3
								.setText("The most popular tool used next to this tool: "
										+ mostPopularNextTool);

								String mostPopularPreviousTool = getMostPopularPreviousTool(toolBeingRequested);
								single4
								.setText("The most popular tool used before this tool: "
										+ mostPopularPreviousTool);

								// workflows =
								// getTop3MostPopularWFForThisTool(toolBeingRequested);
								// labelNC20.setText(top3WFForThisTool.get(0) + " and "
								// + top3WFForThisTool.get(1) + " and " +
								// top3WFForThisTool.get(2));

							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				};
				worker.execute();
			}
		});
		/* K added (END)-singleTool */

		top3Panel.setLayout(new BoxLayout(top3Panel, BoxLayout.Y_AXIS)); // K
		// top3SubPanel1.setBackground(Color.RED); //K

		top3ControlPanel.setBackground(Color.LIGHT_GRAY);
		top3Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent topE) {

				javax.swing.SwingWorker<Void, Void> worker = new javax.swing.SwingWorker<Void, Void>() {
					public Void doInBackground() {


						/* K added START */
						// KTop3 top3 = new KTop3();
						top3ToolString = new ArrayList();
						try {
							top3ToolString = getTop3MostPopularTools();
							for (int i = 0; i < 3; i++) {
								// System.out.println("****************");
								// System.out.println(top3ToolString.get(i));
								// System.out.println("****************");
								if (i == 0)
									top1
									.setText("1: "
											+ (String) top3ToolString.get(i));
								if (i == 1)
									top2
									.setText("2: "
											+ (String) top3ToolString.get(i));
								if (i == 2)
									top3
									.setText("3: "
											+ (String) top3ToolString.get(i));
							}
						} catch (Exception except) {

						}
						top3ToolAsHeadString = new ArrayList();
						try {
							top3ToolAsHeadString = getTop3MostPopularWFHead();
							for (int i = 0; i < 3; i++) {
								// System.out.println("****************");
								// System.out.println((String)top3ToolString.get(i));
								// System.out.println("****************");
								if (i == 0)
									top11.setText("1: "
											+ (String) top3ToolAsHeadString.get(i));
								if (i == 1)
									top22.setText("2: "
											+ (String) top3ToolAsHeadString.get(i));
								if (i == 2)
									top33.setText("3: "
											+ (String) top3ToolAsHeadString.get(i));
							}
						} catch (Exception except) {

						}
						top3WFString = new ArrayList();
						try {
							top3WFString = getTop3MostPopularWF();
							// System.out.println(top3WFString.size());
							// top111.setText("1: "+ (String)top3WFString.get(0));
							// top222.setText("2: "+ (String)top3WFString.get(1));
							// top333.setText("3: "+ (String)top3WFString.get(2));
							// System.out.println((String)top3WFString.get(0));
							// System.out.println((String)top3WFString.get(1));
							// System.out.println((String)top3WFString.get(2));
							String s3 = (String) top3WFString.get(2);

							for (int i = 0; i < 3; i++) {
								// System.out.println("****************");
								// System.out.println(top3WFString.get(i));
								// System.out.println("****************");
								if (i == 0) {
									// System.out.println("No1No1No1No1");
									top111
									.setText("1: "
											+ (String) top3WFString.get(i));
								}
								if (i == 1) {
									// System.out.println("No2No2No2No2");
									top222
									.setText("2: "
											+ (String) top3WFString.get(i));
								} // ARACNE,MRA Analysis,MRA Analysis,T Test Analysis,T
								// Test Analysis,T Test Analysis,T Test Analysis,
								// "123456789 123456789 123456789 123456789 123456789 123456789"
								if (i == 2) {
									// System.out.println("No3No3No3No3" + s3);
									top333.setText("3: " + s3);
								}

							}

						} catch (Exception except) {
							System.out.println("$$$$$$$$$$$$$$$$Exception");
							except.printStackTrace();
						}
						/* Koichrio added END */
						return null;
					}
				};
				worker.execute();
			}
		});
		top3Label.setFont(new Font("Courier New", Font.BOLD, 20));
		top3Label.setForeground(Color.RED);

		top3ControlPanel.setLayout(new FlowLayout());

		top3ControlPanel.add(top3Label);
		top3ControlPanel.add(top3Button);

		singleControlPanel.setBackground(Color.LIGHT_GRAY);
		singleControlPanel.setLayout(new FlowLayout());
		singleLabel.setFont(new Font("Courier New", Font.BOLD, 20));
		singleLabel.setForeground(Color.BLUE);
		singleControlPanel.add(singleLabel);
		singleControlPanel.add(tools);

		top3SubPanel1.setBackground(Color.PINK); // K
		top3SubPanel2.setBackground(Color.PINK); // K
		top3SubPanel3.setBackground(Color.PINK); // K
		top3SubPanel1.setBorder(new TitledBorder(new EtchedBorder(), ""));
		top3SubPanel2.setBorder(new TitledBorder(new EtchedBorder(), ""));
		top3SubPanel3.setBorder(new TitledBorder(new EtchedBorder(), ""));

		top3Tool.setFont(new Font("Courier New", Font.BOLD, 12));
		top3SubPanel1.setLayout(new BoxLayout(top3SubPanel1, BoxLayout.Y_AXIS));
		top3SubPanel1.add(top3Tool);
		top3SubPanel1.add(top1);
		top3SubPanel1.add(top2);
		top3SubPanel1.add(top3);

		top3ToolAsHead.setFont(new Font("Courier New", Font.BOLD, 12));
		top3SubPanel2.setLayout(new BoxLayout(top3SubPanel2, BoxLayout.Y_AXIS));
		top3SubPanel2.add(top3ToolAsHead);
		top3SubPanel2.add(top11);
		top3SubPanel2.add(top22);
		top3SubPanel2.add(top33);

		top3WF.setFont(new Font("Courier New", Font.BOLD, 12));
		top3SubPanel3.setLayout(new BoxLayout(top3SubPanel3, BoxLayout.Y_AXIS));
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
		subSingleToolPanel.setLayout(new BoxLayout(subSingleToolPanel,
				BoxLayout.Y_AXIS));
		// singleToolPanel.add(singleTool);
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

		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS)); // K
		northPanel.add(top3ControlPanel); // K
		northPanel.add(top3Panel); // K
		northPanel.add(singleControlPanel); // K
		northPanel.add(singleToolPanel); // K
		// northPanel.add(selectPanel); // K
		
		messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
		messagePanel.add(message);
		messagePanel.add(message1);

		//setBackground(Color.CYAN);
		add(northPanel, BorderLayout.CENTER); // K
		add(messagePanel, BorderLayout.SOUTH);
		// add(graphPanel, BorderLayout.CENTER);

	}

	/**
	 * Connects to the server and gets a list of all the analysis tools
	 */
	private ArrayList<String> getAllTools() {
		ArrayList<String> allTools = new ArrayList<String>();

		// connect to the server and get the info we need
		PrintWriter out = null;
		Socket s = null;
		try {
			s = new Socket(HOST, PORT);
			out = new java.io.PrintWriter(s.getOutputStream());

			// send the action keyword and the name of the tool
			out.write("TOOLS\n");
			out.flush();

			// read in the response and store the values in an ArrayList
			Scanner in = new Scanner(s.getInputStream());
			while (in.hasNext()) {
				String line = in.nextLine();
				// System.out.println(line);
				if (line.equals("END"))
					break;
				allTools.add(line);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			// TODO: handle the error more gracefully
		} finally {
			try {
				out.close();
			} catch (Exception ex) {
			}
			try {
				s.close();
			} catch (Exception ex) {
			}
		}
		return allTools;
	}

	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	// K added
	private static ArrayList getTop3MostPopularTools() throws Exception {

		// now we test the method "getTop3MostPopularTools"
		clientSideID = "getTop3MostPopularTools";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		// System.out.println("response from server received: Top 3 most Pop Tools: "
		// + listBack.toString());
		// System.out.println();
		return listBack;
	}

	// K added
	private static ArrayList getTop3MostPopularWFHead() throws Exception {

		// now we test the method "getTop3MostPopularWFHead"
		clientSideID = "getTop3MostPopularWFHead";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		// System.out.println("response from server received: top 3 most Pop WF heads: "
		// + listBack.toString());
		// System.out.println();
		return listBack;
	}

	// K added
	private static ArrayList getTop3MostPopularWF() throws Exception {

		// now we test the method "getTop3MostPopularWF"
		clientSideID = "getTop3MostPopularWF";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		// System.out.println("@@@@@response from server received: top 3 most Pop WFs: "
		// + listBack.toString());
		// System.out.println();
		return listBack;
	}

	// K added from SuggestionBasedOnSingleTool
	private static ArrayList getAllAnalysisTools() throws Exception {

		// now we test the method "getAllAnalysisTools"
		clientSideID = "getAllAnalysisTools";
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		ArrayList listBack = (ArrayList) ois.readObject();
		// System.out.println("response from server received: All Analysis Tools: "
		// + listBack.toString());
		// System.out.println();
		return listBack;
	}

	// K added from SuggestionBasedOnSingleTool
	private static String getUsageRate(String toolApplied) throws Exception {

		clientSideID = "feature2,getUsageRate," + toolApplied;// zhu yi kong ge
		// shi zi dai de
		// ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		// System.out.println("response from server received: usage rate for this tool: "
		// + resultValue);
		// System.out.println();
		return resultValue;

	}

	// K added from SuggestionBasedOnSingleTool
	private static String getUsageRateAsWFHead(String toolApplied)
	throws Exception {

		clientSideID = "feature2,getUsageRateAsWFHead," + toolApplied;// zhu yi
		// kong
		// ge
		// shi
		// zi
		// dai
		// de
		// ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		// System.out.println("response from server received: usage rate for this tool as WF head: "
		// + resultValue);
		// System.out.println();
		return resultValue;
	}

	// K added from SuggestionBasedOnSingleTool
	private static String getMostPopularNextTool(String toolApplied)
	throws Exception {

		clientSideID = "feature2,getMostPopularNextTool," + toolApplied;// zhu
		// yi
		// kong
		// ge
		// shi
		// zi
		// dai
		// de
		// ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		// System.out.println("response from server received: most Pop next tool of this tool: "
		// + resultValue);
		// System.out.println();
		return resultValue;
	}

	// K added from SuggestionBasedOnSingleTool
	private static String getMostPopularPreviousTool(String toolApplied)
	throws Exception {

		clientSideID = "feature2,getMostPopularPreviousTool," + toolApplied;// zhu
		// yi
		// kong
		// ge
		// shi
		// zi
		// dai
		// de
		// ...
		Socket s = new Socket(serverIP, serverPort);
		ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(clientSideID);
		oos.flush();
		// System.out.println("client request sent");

		// waiting for server response...
		ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
		// System.out.println("waiting response from server.....");
		String resultValue = (String) ois.readObject();
		// System.out.println("response from server received: most Pop previous tool of this tool: "
		// + resultValue);
		// System.out.println();
		return resultValue;
	}
}
