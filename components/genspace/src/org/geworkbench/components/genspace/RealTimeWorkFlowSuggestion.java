package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.MatteBorder;

import org.geworkbench.components.genspace.entity.Tool;
import org.geworkbench.components.genspace.entity.Workflow;
import org.geworkbench.components.genspace.rating.WorkflowVisualizationPopup;
import org.geworkbench.engine.config.VisualPlugin;

public class RealTimeWorkFlowSuggestion extends JPanel implements VisualPlugin,
		Runnable {

	private static final long serialVersionUID = 4806046453151557609L;
	JRadioButton log, logAnon, noLog;
	ButtonGroup group;
	JPanel radioPanel, saveReset;
	JButton save, reset;
	private static ImageIcon arrow = new ImageIcon(
			"components/genspace/src/org/geworkbench/components/genspace/rating/arrow_right.png");

	public static Workflow cwf = null;
	public static ArrayList<Workflow> usedWorkFlowToday = new ArrayList<Workflow>();
//	private static String currentTid = null;

	private static JPanel workflowViewerPanel = new JPanel();
	private static JPanel workflowNodePanel = new JPanel(new FlowLayout());
	private static JPanel workflowInfoPanel = new JPanel(new BorderLayout());

	private static WorkflowVisualizationPopup popup = new WorkflowVisualizationPopup();

	private static JLabel viewerStatus = new JLabel();

	private static JTextArea infoArea = new JTextArea();

	static final String PROPERTY_KEY = "genSpace_logging_preferences"; // the
																		// key
																		// in
																		// the
																		// properties
																		// file
	int preference; // the logging preference

	public RealTimeWorkFlowSuggestion() {

	}

	@Override
	public void run() {
		initComponents();
	}

	private void initComponents() {
		try {

			/*
			 * 
			 * labelNC = new JLabel("Analysis Tools Suggestion Center:");
			 * 
			 * labelNC1 = new JLabel(
			 * "The genSpace server currently has the information for the following tools:"
			 * );
			 * 
			 * ArrayList allAnalysisTools = getAllAnalysisTools(); labelNC2 =
			 * new JLabel(allAnalysisTools.toString());
			 * 
			 * labelNC3 = new JLabel("Top 3 most popular tools:"); ArrayList
			 * top3MostPopularTools = getTop3MostPopularTools(); labelNC4 = new
			 * JLabel(top3MostPopularTools.get(0) + " " +
			 * top3MostPopularTools.get(1) + " " + top3MostPopularTools.get(2));
			 * 
			 * 
			 * labelNC5 = new
			 * JLabel("Top 3 most popular tools as work flow head:"); ArrayList
			 * top3MostPopularWFHead = getTop3MostPopularWFHead(); labelNC6 =
			 * new JLabel(top3MostPopularWFHead.get(0) + " " +
			 * top3MostPopularWFHead.get(1) + " " +
			 * top3MostPopularWFHead.get(2));
			 * 
			 * labelNC7 = new JLabel("Top 3 most popular work flow :");
			 * ArrayList top3MostPopularWF = getTop3MostPopularWF(); labelNC8 =
			 * new JLabel(top3MostPopularWF.get(0) + " " +
			 * top3MostPopularWF.get(1) + " " + top3MostPopularWF.get(2));
			 */

			// ISBUPanel = new JPanel(new GridLayout(3,1));

		} catch (Exception e) {
			e.printStackTrace();
		}

		/**
		 * setup general layout of panel
		 * */

		// the viewer panel
		workflowViewerPanel.setLayout(new BorderLayout());
		workflowViewerPanel.setBorder(new MatteBorder(10, 10, 10, 10,
				new Color(35, 35, 142)));
		workflowViewerPanel.setBackground(new Color(35, 35, 142));

		// setup viewer status
		viewerStatus.setForeground(Color.WHITE);
		viewerStatus.setText("No analysis has occured yet.");
		workflowViewerPanel.add(viewerStatus, BorderLayout.NORTH);
		workflowNodePanel.setBackground(new Color(35, 35, 142));
		workflowNodePanel.setBorder(new MatteBorder(10, 10, 10, 10, new Color(
				35, 35, 142)));
		workflowViewerPanel.add(workflowNodePanel, BorderLayout.CENTER);

		// the info panel
		workflowInfoPanel.add(new JScrollPane(infoArea));
		infoArea.setFont(new Font("Verdana", Font.PLAIN, 10));
		infoArea.append("You haven't used any tools!\n");
		infoArea.append("Next best rated tool to use: none.");

		// add both panels
		this.setLayout(new BorderLayout());
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setDividerLocation(100);
		splitter.setResizeWeight(0.5);

		JScrollPane scroller = new JScrollPane(workflowViewerPanel);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		splitter.add(scroller);

		splitter.add(workflowInfoPanel);
		add(splitter, BorderLayout.CENTER);

	}

	public static void displayCWF() {

		// -----------------show finished work flows today
		String finishedWF = "";
		for (int i = 0; i < usedWorkFlowToday.size(); i++) {
			

			finishedWF = finishedWF + usedWorkFlowToday.get(i)
					+ "\n";

		}
		if (usedWorkFlowToday.size() == 0) {
			finishedWF = "No finished work flows!";
		}

		List<Workflow> suggestions = getRealTimeWorkFlowSuggestion(cwf);

		int curIndexIntoTools = cwf.getTools().size();
		String nextSteps = "";		
		Tool nextBestRated = null;
		HashMap<Tool, Integer> toolRatings = new HashMap<Tool, Integer>();
		for(Workflow w : suggestions)
		{
			if(curIndexIntoTools < w.getTools().size())
			{
				Tool t = w.getTools().get(curIndexIntoTools).getTool();
				if(toolRatings.containsKey(t))
					toolRatings.put(t, toolRatings.get(t) + w.getUsageCount());
				else
					toolRatings.put(t, w.getUsageCount());
			}
			for(int i = curIndexIntoTools; i < w.getTools().size(); i++)
			{
				nextSteps += w.getTools().get(i) + ", ";
			}
			nextSteps = nextSteps.substring(0,nextSteps.length()-2) + "\n";
		}
		int bestRating = 0;
		for(Tool t : toolRatings.keySet())
		{
			if(toolRatings.get(t) > bestRating)
			{
				bestRating = toolRatings.get(t);
				nextBestRated = t;
			}
		}
		
		infoArea.setText("");
		// infoArea.append("\n\n\n\n\n\n\n");
		infoArea.append("Your current workflow: \n");
		infoArea.append(cwf + "\n\n");
		infoArea.append("Previous workflows: \n");
		infoArea.append(finishedWF + "\n\n\n");
		// infoArea.append("Your current workflow activity so far: \n" +
		// cwfSeparate + "\n\n");
		// infoArea.append(finishedWF + "\n\n\n");

		infoArea.append("Suggestions for your next steps: "
				+ "\n");
		infoArea.append("-----------------------------------------------------------------------\n\n");
		infoArea.append("Current workflow usage:" + "\n");
		infoArea.append("Your current work flow has been used " + cwf.getUsageCount()
				+ " times by genSpace users." + "\n\n");
		infoArea.append("Next steps:" + "\n");
		infoArea.append(nextSteps + "\n\n");
//		infoArea.append("How users have gotten here: " + "\n");
//		infoArea.append(statBDisplay + "\n\n");

		if (nextBestRated != null)
			infoArea.append("Next best rated tool to use: " + nextBestRated
					+ ".\n\n");
	}

	private static boolean emptyPanel = true;
	public static void cwfUpdated(Workflow newCWF) {

		Workflow last = cwf;
		RealTimeWorkFlowSuggestion.cwf = newCWF;
		viewerStatus.setText("You most reecently used " + cwf.getTools().get(cwf.getTools().size() -1 ).getTool());
		displayCWF();
		
		if(emptyPanel)
		{
			
			emptyPanel = false;
		}
		else if(last.equals(newCWF))
		{
			workflowNodePanel.add(new JLabel(arrow));
		}
		else //this is a different workflow
		{
			usedWorkFlowToday.add(last);
			workflowNodePanel.removeAll();
		}
		displayCWF();
		
		WorkflowViewerPanelNode newNode = new WorkflowViewerPanelNode(
			newCWF.getLastTool(), newCWF.getTools().size() - 1,newCWF);

				newNode.addMouseListener(new MouseListener() {

					// @Override
					@Override
					public void mouseClicked(MouseEvent event) {


						WorkflowViewerPanelNode node = (WorkflowViewerPanelNode) event
								.getSource();


						popup.initialize(node.getTool(), node.getWorkflow());
						popup.show(node, event.getX(), event.getY());

					}

					@Override
					public void mouseEntered(MouseEvent e) {
					}

					@Override
					public void mouseExited(MouseEvent e) {
					}

					@Override
					public void mousePressed(MouseEvent e) {
					}

					@Override
					public void mouseReleased(MouseEvent e) {
					}

				});
				workflowNodePanel.add(newNode);
	}

	private static List<Workflow> getRealTimeWorkFlowSuggestion(Workflow cwf) {
		
		return LoginFactory.getUsageOps().getToolSuggestion(cwf);
	}

	/*
	 * 
	 * 
	 * public void actionPerformed(ActionEvent e) { if (e.getSource() == save) {
	 * //System.out.println("Save pressed with " +
	 * group.getSelection().getActionCommand()); preference =
	 * Integer.parseInt(group.getSelection().getActionCommand());
	 * ObjectHandler.setLogStatus(preference); save.setEnabled(false); // write
	 * it to the properties file try { PropertiesManager properties =
	 * PropertiesManager.getInstance();
	 * properties.setProperty(GenSpaceLogPreferences.class, PROPERTY_KEY,
	 * group.getSelection().getActionCommand()); } catch (Exception ex) { }
	 * 
	 * } else if (e.getSource() == reset) {
	 * //System.out.println("Reset pressed"); logAnon.setSelected(true);
	 * save.setEnabled(true); } else if (e.getSource() == log) { if (preference
	 * == 0) { save.setEnabled(false); } else { save.setEnabled(true); } } else
	 * if (e.getSource() == logAnon) { if (preference == 1) {
	 * save.setEnabled(false); } else { save.setEnabled(true); } } else if
	 * (e.getSource() == noLog) { if (preference == 2) { save.setEnabled(false);
	 * } else { save.setEnabled(true); } } }
	 */

	@Override
	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
	}

	

}

class WorkflowViewerPanelNode extends JButton {

	private static final long serialVersionUID = -1326037460164805701L;
	private int index;
	private Workflow wkflw;
	private Tool tool;
	public WorkflowViewerPanelNode(Tool tool, int index,Workflow workflow) {
		super(tool.getName());
		this.putClientProperty("is3DEnabled", Boolean.FALSE);
		this.index = index;
		this.wkflw = workflow;
		this.tool = tool;
	}

	public Tool getTool() {
		return tool;
	}
	public Workflow getWorkflow() {
		return wkflw;
	}
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
