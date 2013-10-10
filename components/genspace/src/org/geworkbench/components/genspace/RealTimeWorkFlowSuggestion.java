package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;

import org.geworkbench.components.genspace.server.stubs.Tool;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.wrapper.WorkflowWrapper;
import org.geworkbench.components.genspace.ui.NameRenderer;
import org.geworkbench.components.genspace.ui.WorkflowVisualizationPanel;
import org.geworkbench.engine.config.VisualPlugin;

public class RealTimeWorkFlowSuggestion extends JPanel implements VisualPlugin,
		ActionListener, Runnable {

	private static final long serialVersionUID = 4806046453151557609L;
	JRadioButton log, logAnon, noLog;
	ButtonGroup group;
	JPanel radioPanel, saveReset;
	JButton save, reset;
//	private static ImageIcon arrow = new ImageIcon("components/genspace/classes/org/geworkbench/components/genspace/rating/arrow_right.png");

	public static WorkflowWrapper cwf = null;
	public static ArrayList<WorkflowWrapper> usedWorkFlowToday = new ArrayList<WorkflowWrapper>();
//	private static String currentTid = null;

	private static WorkflowVisualizationPanel workflowVisualizationPanel = new WorkflowVisualizationPanel();
	private static JPanel workflowViewerPanel = new JPanel();
//	private static JPanel workflowNodePanel = new JPanel(new FlowLayout());
	private static JPanel workflowInfoPanel = new JPanel(new BorderLayout());

//	private static WorkflowVisualizationPopup popup = new WorkflowVisualizationPopup();

	private static JLabel viewerStatus = new JLabel();

	private static JTextArea infoArea = new JTextArea();
	
	private static JList toolListing = new JList();
	
	private JButton button = new JButton("Search");
	private JTextPane wfsPane;

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
				new Color(215,217,223)));
		workflowViewerPanel.setBackground(new Color(215, 217, 223));

		// setup viewer status
//		viewerStatus.setForeground(Color.WHITE);
		viewerStatus.setText("No analysis has occured yet.");
		workflowViewerPanel.add(viewerStatus, BorderLayout.NORTH);
//		workflowNodePanel.setBackground(new Color(35, 35, 142));
//		workflowNodePanel.setBorder(new MatteBorder(10, 10, 10, 10, new Color(
//				35, 35, 142)));
		workflowVisualizationPanel.setSize(workflowViewerPanel.getSize());
		workflowViewerPanel.add(workflowVisualizationPanel, BorderLayout.CENTER);
		workflowVisualizationPanel.setSize(workflowViewerPanel.getSize());

		// the info panel
		JPanel suggestionsPanel = new JPanel();
		JPanel toolListPanel = new JPanel();
		/*
		toolListPanel.setBorder(new MatteBorder(10, 10, 10, 10,
				new Color(215,217,223)));
		*/
		
		toolListPanel.setBorder(BorderFactory.createEtchedBorder());
		JLabel label1 = new JLabel("Advanced suggestions");
		label1.setHorizontalAlignment(JLabel.CENTER);
		JLabel label2 = new JLabel("Get suggestions from people who use these tools:");
		label2.setHorizontalAlignment(JLabel.CENTER);
		
		JPanel headerPanel = new JPanel(new GridLayout(2,1));
		JPanel resultsPanel = new JPanel(new BorderLayout());
		wfsPane = new JTextPane();
		
		JLabel resultsLabel = new JLabel("Results:");
		JPanel toolPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		
		headerPanel.add(label1);
		headerPanel.add(label2);
		
		resultsLabel.setPreferredSize(new Dimension(500, 30));
		resultsPanel.add(resultsLabel, BorderLayout.NORTH);
		resultsPanel.add(new JScrollPane(wfsPane), BorderLayout.CENTER);
		
		button.addActionListener(this);
		buttonPanel.add(button);
		
		suggestionsPanel.setLayout(new GridLayout(1,2));
		
		toolListPanel.setLayout(new BorderLayout());
	
		updateAllToolList();
		
		JScrollPane sp = new JScrollPane();
		sp.getViewport().add(toolListing);
		sp.setPreferredSize(new Dimension (80, 200));
		
		toolPanel.add(sp, BorderLayout.CENTER);
		toolPanel.add(buttonPanel, BorderLayout.EAST);
		
		headerPanel.setPreferredSize(new Dimension(500, 60));
		toolPanel.setPreferredSize(new Dimension(500, 240));
		resultsPanel.setPreferredSize(new Dimension(500, 210));
		
		wfsPane.setContentType("text/html");
		wfsPane.setEditable(false);
		wfsPane.setDisabledTextColor(Color.black);
		wfsPane.setEnabled(false);
		wfsPane.setBackground(new Color(214,217,223));
		
		Font font = UIManager.getFont("Label.font");
        String bodyRule = "body { font-family: " + font.getFamily() + "; " +
                "font-size: " + font.getSize() + "pt; padding: 0; margin: 0; background-color: #d6d9df } ";
        ((HTMLDocument) wfsPane.getDocument()).getStyleSheet().addRule(bodyRule);
        String liRule = "ol { font-family: " + font.getFamily() + "; " +
        	"font-size: " + font.getSize() + "pt; padding: 1em; margin: 20px;} ";
        ((HTMLDocument) wfsPane.getDocument()).getStyleSheet().addRule(liRule);
		Style newStyle = ((HTMLDocument) wfsPane.getDocument()).addStyle("BGStyle", null);
		StyleConstants.setBackground(newStyle, new Color(214,217,223));
		
		toolListPanel.add(headerPanel, BorderLayout.NORTH);
		toolListPanel.add(toolPanel, BorderLayout.CENTER);
		toolListPanel.add(resultsPanel, BorderLayout.SOUTH);
		
		workflowInfoPanel.add(new JScrollPane(infoArea));
		infoArea.setFont(new Font("Verdana", Font.PLAIN, 10));
		infoArea.append("You haven't used any tools!\n");
		infoArea.append("Next best rated tool to use: none.");

		// add both panels
		this.setLayout(new BorderLayout());
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.setDividerLocation(150);
		splitter.setResizeWeight(0.5);

//		JScrollPane scroller = new JScrollPane(workflowViewerPanel);
//		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		
		suggestionsPanel.add(workflowInfoPanel);
		suggestionsPanel.add(toolListPanel);
		
		splitter.add(workflowViewerPanel);

		splitter.add(suggestionsPanel);
		add(splitter, BorderLayout.CENTER);
		
	}
	
	private void updateAllToolList() {
		SwingWorker<List<Tool>, Void> worker = new SwingWorker<List<Tool>, Void>() {

			@Override
			protected void done() {
				try {
					List<Tool>results = get();
					DefaultComboBoxModel m = new DefaultComboBoxModel();
					toolListing.setCellRenderer(new NameRenderer());
					if(results != null)
					for (Tool s : results) {
						m.addElement(s);
					}
					toolListing.setModel(m);
				
				} catch (InterruptedException e) {
					GenSpace.logger.debug("Error talking to server: ",e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
				super.done();
			}

			@Override
			protected List<Tool> doInBackground() {
				try {
					return (GenSpaceServerFactory.getUsageOps().getAllTools());
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

		};
		worker.execute();
	}


	public static void displayCWF() {

		// -----------------show finished work flows today
		String finishedWF = "";
		for (int i = 0; i < usedWorkFlowToday.size(); i++) {
			

			finishedWF = finishedWF + usedWorkFlowToday.get(i)
					+ "\n";

		}
		if (usedWorkFlowToday.size() == 0) {
			finishedWF = "No finished workflows!";
		}

		List<Workflow> suggestions = getRealTimeWorkFlowSuggestion(cwf);
		String nextSteps = "";
		Tool nextBestRated = null;

		if(suggestions != null)
		{
			int curIndexIntoTools = cwf.getTools().size();
			
			HashMap<Tool, Integer> toolRatings = new HashMap<Tool, Integer>();
			for(Workflow wa : suggestions)
			{
				WorkflowWrapper w = new WorkflowWrapper(wa);
				w.loadToolsFromCache();
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
					nextSteps += w.getTools().get(i).getTool().getName() + ", ";
				}
				if(nextSteps.length() > 2)
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
		}
		infoArea.setText("");		
		
		// infoArea.append("\n\n\n\n\n\n\n");
		infoArea.append("Your current workflow: \n");
		infoArea.append(cwf + "\n\n");
		//infoArea.append("Previous workflows: \n");
		//infoArea.append(finishedWF + "\n\n\n");
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
			infoArea.append("Next best rated tool to use: " + nextBestRated.getName()
					+ ".\n\n");
	}

	private static boolean emptyPanel = true;
	public static void cwfUpdated(Workflow newCWF) {
	
		WorkflowWrapper last = cwf;
		RealTimeWorkFlowSuggestion.cwf = new WorkflowWrapper(newCWF);
		//System.out.println("Check new workflow: " + RealTimeWorkFlowSuggestion.cwf.toString());
		viewerStatus.setText("You recently used " + cwf.getTools().get(cwf.getTools().size() -1 ).getTool().getName());
		displayCWF();
		
		if(emptyPanel)
		{
			
			emptyPanel = false;
		}
		else //this is a different workflow
		{
			usedWorkFlowToday.add(last);
		}
		displayCWF();
		
		workflowVisualizationPanel.render(RealTimeWorkFlowSuggestion.cwf );
	}

	private static List<org.geworkbench.components.genspace.server.stubs.Workflow> getRealTimeWorkFlowSuggestion(WorkflowWrapper cwf) {
		
		try {
			return (GenSpaceServerFactory.getUsageOps().getToolSuggestion(cwf.getId()));
			
		} catch (Exception e) {
			return null;
		}
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

	@Override
	public void actionPerformed(ActionEvent e) {
		SwingWorker<List<Workflow>, Void> worker = new SwingWorker<List<Workflow>, Void>() {
			int evt;
			
			@Override
			protected void done() {
				List<Workflow> reta = null;
				try {
					reta = get();
				} catch (InterruptedException e) {
					GenSpace.getStatusBar().stop(evt);
					GenSpace.logger.warn("Unable to talk to server: ", e);
				} catch (ExecutionException e) {
					GenSpace.getStatusBar().stop(evt);
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
				// make sure we got some results!
				if (reta == null || reta.size() == 0) {
					// no results came back!
					JOptionPane.showMessageDialog(null,
							"There are no workflows matching that criteria");
					wfsPane.setText("No Workflows found");
				}
				
				if (reta != null) {
					int lim = 10;
					String wfs = "";
					for(Workflow zz : reta)
					{
						WorkflowWrapper za = new WorkflowWrapper(zz);
						za.loadToolsFromCache();
						wfs = wfs + "<li>" + za.toString() + "</li>";
						lim--;
						if (lim <= 0)
							break;
					}
					wfsPane.setText("<html><body><ol>"+wfs+"</ol></body></html>");
				}
				
				GenSpace.getStatusBar().stop(evt);
				super.done();
			}
			
			@Override
			public List<Workflow> doInBackground() {

				List<Tool> tools = new ArrayList<Tool>();
				
				// Get the index of all the selected items
				int[] selectedIx = toolListing.getSelectedIndices();
				
				// Get all the selected items using the indices
				for (int i=0; i<selectedIx.length; i++) {
				    Tool tool = (Tool) toolListing.getModel().getElementAt(selectedIx[i]);
				    tools.add(tool);
				}
				
				// get the name of the selected tool and the action
				if(selectedIx.length > 0)
				{
					evt = GenSpace.getStatusBar().start("Retrieving workflow information");
					try {
						return (GenSpaceServerFactory.getUsageOps().getMahoutSimilarWorkflowsSuggestion(tools));
					}
					catch(Exception e)
					{
						GenSpaceServerFactory.handleExecutionException(e);
						GenSpace.getStatusBar().stop(evt);
					}
				} else {
					GenSpace.getStatusBar().stop(evt);
				}
				return null;
			}
		};
		worker.execute();	
	}
}

class WorkflowViewerPanelNode extends JButton {

	private static final long serialVersionUID = -1326037460164805701L;
	private int index;
	private WorkflowWrapper wkflw;
	private Tool tool;
	public WorkflowViewerPanelNode(Tool tool, int index,WorkflowWrapper workflow) {
		super(tool.getName());
		this.putClientProperty("is3DEnabled", Boolean.FALSE);
		this.index = index;
		this.wkflw = workflow;
		this.tool = tool;
	}

	public Tool getTool() {
		return tool;
	}
	public WorkflowWrapper getWorkflow() {
		return wkflw;
	}
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
