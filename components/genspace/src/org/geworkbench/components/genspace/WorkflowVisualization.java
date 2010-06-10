package org.geworkbench.components.genspace;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.components.genspace.rating.WorkflowVisualizationPopup;
import org.geworkbench.engine.config.VisualPlugin;
import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;




public class WorkflowVisualization extends JPanel implements VisualPlugin, ActionListener, Runnable {

	private Log log = LogFactory.getLog(this.getClass());
	private JComboBox tools = new JComboBox();
	private JComboBox actions = new JComboBox();
	private JButton button = new JButton("Search");
	private JCheckBox checkbox = new JCheckBox("My social networks only");
	private JLabel label = new JLabel();
	private JPanel selectPanel = new JPanel();
	private JPanel graphPanel = new JPanel();
	private JScrollPane scroller = new JScrollPane();;
	private HashMap<String, String> actionKeywords = new HashMap<String, String>();
	private ArrayList<String> workflows;
	private JGraph graph;
	private DefaultGraphCell[] cells;

	private WorkflowVisualizationPopup popup = new WorkflowVisualizationPopup();

	public static final String[] NUMBERS = {"No", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten" };

	private String login = "chris"; // TODO: WHERE DOES THIS COME FROM???


	public WorkflowVisualization()
	{
		log.debug("Workflow Visualization started");

	}

	public void run() {
		initComponents();
	}


	private void initComponents()
	{
		setLayout(new BorderLayout());

		tools.addItem("-- select tool --");
		// TODO: maybe do this in a different thread so that it doesn't hold up the rest of the app
		ArrayList<String> allTools = getAllTools();
		for (String tool : allTools)
			tools.addItem(tool);

		actions.addItem("-- select action --");
		actions.addItem("Most common workflow starting with");
		actions.addItem("Most common workflow including");
		actions.addItem("All workflows including");

		actionKeywords.put("Most common workflow starting with", "START");
		actionKeywords.put("Most common workflow including", "INCLUDE");
		actionKeywords.put("All workflows including", "ALL");

		//selectPanel.add(checkbox);

		selectPanel.add(actions);
		selectPanel.add(tools);

		button.addActionListener(this);
		selectPanel.add(button);

		label.setText("Please select an action and a tool to search for");
		selectPanel.add(label);


		add(selectPanel, BorderLayout.NORTH);

		add(scroller, BorderLayout.CENTER);
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
			s = new Socket(RuntimeEnvironmentSettings.WORKFLOW_SERVER.getHost(),
					RuntimeEnvironmentSettings.WORKFLOW_SERVER.getPort());
			out = new java.io.PrintWriter(s.getOutputStream());

			// send the action keyword and the name of the tool
			out.write("TOOLS\n");
			out.flush();

			// read in the response and store the values in an ArrayList
			Scanner in = new Scanner(s.getInputStream());
			while (in.hasNext())
			{
				String line = in.nextLine();
				log.debug(line);
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

		org.jdesktop.swingworker.SwingWorker<Void, Void> worker = new org.jdesktop.swingworker.SwingWorker<Void, Void>() {
			public Void doInBackground() {

				// get the name of the selected tool and the action
				String tool = tools.getSelectedItem().toString();
				String action = actions.getSelectedItem().toString();

				// to store the workflows that come back from the server
				workflows = new ArrayList<String>();


				// connect to the server and get the info we need
				PrintWriter out = null;
				Socket s = null;
				try
				{
					s = new Socket(RuntimeEnvironmentSettings.WORKFLOW_SERVER.getHost(), 
							RuntimeEnvironmentSettings.WORKFLOW_SERVER.getPort());

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
						log.debug(line);
						if (line.equals("END")) break;
						if (!(line.equals("null"))) workflows.add(line);
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


				/*
				 * This is just sample data!!!!
    	if (action.equals("All workflows including"))
    	{
    		if (tool.equals("Log2 Normalizer"))
    		{
    			workflows.add("Affymetrix Filter,Log2 Normalizer,T-test,Hierarchical Clustering,GO Terms");
    			workflows.add("Quantile Normalization,Log2 Normalizer,Aracne,Sequence Retriever,Promoter,MEDUSA");
    			workflows.add("Quantile Normalization,Log2 Normalizer,BLAST,Pattern Discovery");
    		}
    		else
    		{
    			workflows.add("Quantile Normalization,Log2 Normalizer,BLAST,Pattern Discovery");
    			workflows.add("BLAST,Pattern Discovery");
    		}
    	}
    	else if (action.equals("Most common workflow including"))
    	{
    		if (tool.equals("Log2 Normalizer"))
    			workflows.add("Quantile Normalization,Log2 Normalizer,Aracne,Sequence Retriever,Promoter,MEDUSA");
    		else
    			workflows.add("Quantile Normalization,Log2 Normalizer,BLAST,Pattern Discovery");
    	}
    	else if (action.equals("Most common workflow starting with"))
    	{
    		if (tool.equals("Log2 Normalizer"))
    		{

    		}
    		else
    			workflows.add("BLAST, Pattern Discovery");
    	}
				 */

				// make sure we got some results!
				if (workflows.size() == 0)
				{
					// no results came back!
					JOptionPane.showMessageDialog(null, "There are no workflows matching that criteria");
					label.setText("No Workflows found");
					return null;
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
				return null;
			}
		};
		worker.execute();
	}

	public Component getComponent() {
		// In this case, this object is also the GUI component.
		return this;
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
		DirectedOrderedSparseMultigraph<Integer, Integer> backGraph = new DirectedOrderedSparseMultigraph<Integer, Integer>();
		final ArrayList<String> reverseMap = new ArrayList<String>();
		
		GraphLayoutCache view = new GraphLayoutCache(model, new DefaultCellViewFactory());
		graph = new JGraph(model, view);

		// figure out the size of the Cell array
		int size = 0;
		if (edges == null) size = nodes.length * 2 - 1;
		else size = nodes.length + edges.length;
		cells = new DefaultGraphCell[size];
		DefaultGraphCell[] extraCells = new DefaultGraphCell[size];
		
		// to keep track of the mapping between node numbers and the names
		HashMap<String, Integer> map = new HashMap<String, Integer>();


		// get the height and width of the window so we can show the components
		int height = graphPanel.getHeight() - 100; // subtract 100 to have a little buffer
		int width = graphPanel.getWidth();
		// the average width per node
		double avgWidth = ((double)width)/nodes.length;

		// create the Nodes. Do a preliminary (random) layout.
		int count = 0;
		for (int i = 0; i < nodes.length; i++)
		{
			String node = nodes[i].value;

			// figure out the factor to multiply by the height
			double factor = (edges == null) ? 0.5 : Math.random();

			// figure out which color - default is gray
			Color myColor = Color.gray;
			if (node.equals(target)) myColor = Color.yellow;
//			System.out.println("Node is " + node + "; target is " + target);
			//else if (nodes[i].isStart) color = Color.green;
			//else if (count == nodes.length - 1) color = Color.red;

			int myHeight = (node.equals(target)) ? 60 : 40;
			// use the number of characters to figure out the width
			int minWidth = 80;
			int tempWidth = node.trim().length() * 7; // figure 7 pixels per character
			int myWidth = (tempWidth < minWidth) ? minWidth : tempWidth;
			if (node.equals(target)) myWidth *= 1.5;
			backGraph.addVertex(count);
			cells[count] = createNode(node,node, new Rectangle2D.Double(avgWidth * count + 50, height * factor, myWidth, myHeight), myColor);
			map.put(node, count);
			reverseMap.add(node);
			// increment the counter
			count++;
		}
		// create the Edges - preliminary
		if (edges == null)
		{
			// this is for the case when we don't have a tree, just a sequence of nodes
			int max = count-1; // the maximum node number
			for (; count < size; count++)
			{
			
				Pair<Integer> temp = new Pair<Integer>((count-max-1),(count-max));
				backGraph.addEdge(count, count-max-1, count-max);
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
				
				backGraph.addEdge(count, new Pair(src,dest));
				count++;
			}
		}
		

//		Now actually do the layout, then recreate the display graph using the calculated co-ords.
		CircleLayout<Integer, Integer> layout = new CircleLayout<Integer,Integer>(backGraph);
		layout.setSize(new Dimension(scroller.getWidth()-100,scroller.getHeight()-100));
		count = 0;
		int[] shifts = new int[nodes.length];
		for (int i = 0; i < nodes.length; i++)
		{
			String node = nodes[i].value;

			
			// figure out which color - default is gray
			Color myColor = Color.gray;
			if (node.equals(target)) myColor = Color.yellow;
			
			int myHeight = (node.equals(target)) ? 60 : 40;
			// use the number of characters to figure out the width
			int minWidth = 80;
			String toDisplay = node;
			if(node.trim().length() > 21)
			{
				//Add a line break
				toDisplay = "<html>"+node.trim().substring(0,20)+"-<br>"+node.trim().substring(20) + "</html>";
			}
			int tempWidth = 23 * 7; // figure 7 pixels per character
			int myWidth = (tempWidth < minWidth) ? minWidth : tempWidth;
			if (node.equals(target)) myWidth *= 1.5;
			double myX = layout.getX(i);
			double myY = layout.getY(i);
			shifts[i] = 0;
			if(i>0)
			{
				//Check to see if the guy to the left is too close
				
				if(Math.abs(layout.getY(i-1) - layout.getY(i)) < 60)
				{
					//They are relatively "on top" vertically
					if(layout.getY(i-1) > layout.getY(i))
						myY-=40;
					else
						myY+=40;
					myX-=myWidth/2 + shifts[i-1];
					shifts[i] += (myWidth/2 + shifts[i-1]);
				}
			}
			cells[count] = createNode(node,toDisplay, new Rectangle2D.Double(myX, myY, myWidth, myHeight), myColor);

			// increment the counter
			count++;
		}
		// create the Edges for REAL
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
		
		
		graph.setDoubleBuffered(true);

		// to handle any changes to the graph
		GraphListener listener = new GraphListener(this);
		graph.getModel().addGraphModelListener(listener);
		graph.getSelectionModel().addGraphSelectionListener(listener);

		
		// load up the graph
		graph.getGraphLayoutCache().insert(cells);
		

		graphPanel = new JPanel();
		graphPanel.setLayout(new BorderLayout());
		graphPanel.add(graph, BorderLayout.CENTER);
		remove(scroller);
	    scroller = new JScrollPane(graphPanel);

		add(scroller, BorderLayout.CENTER);
		graphPanel.setVisible(true);
		scroller.setVisible(true);

		validate();
		repaint();
	}

	private DefaultEdge createEdge(DefaultGraphCell source, DefaultGraphCell target)
	{
		GraphEdge edge = new GraphEdge(); //new DefaultEdge(source.getUserObject() + ":" + target.getUserObject());
		edge.setSource(source.getChildAt(0));
		edge.setTarget(target.getChildAt(0));
		edge.sourceNode = ((GraphNode)source).toolName.toString();
		edge.destNode = ((GraphNode)target).toolName.toString();
		int arrow = GraphConstants.ARROW_CLASSIC;
		GraphConstants.setLineEnd(edge.getAttributes(), arrow);
		GraphConstants.setEndFill(edge.getAttributes(), true);
		return edge;
	}

	private DefaultGraphCell createNode(String label, String toDisplay, Rectangle2D bounds, Color color)
	{
		GraphNode cell = new GraphNode(label, toDisplay,color);

		GraphConstants.setBounds(cell.getAttributes(), bounds);
		GraphConstants.setGradientColor(cell.getAttributes(), color);
		GraphConstants.setOpaque(cell.getAttributes(), true);
		GraphConstants.setAutoSize(cell.getAttributes(), false);
		GraphConstants.setResize(cell.getAttributes(), false);
		GraphConstants.setEditable(cell.getAttributes(), false);
		GraphConstants.setSizeable(cell.getAttributes(), false);
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
				if (highlightedNodes.size() == 1) highlighted = highlightedNodes.get(0).getToolName();
				else if (highlightedNodes.size() == 2)
					highlighted = highlightedNodes.get(0).getToolName()+ " and " + highlightedNodes.get(1).getToolName();
				else
				{
					for (int i = 0; i < highlightedNodes.size(); i++)
					{
						if (i > 0) highlighted += ", ";
						if (i == highlightedNodes.size() - 1) highlighted += "and ";
						highlighted += highlightedNodes.get(i).getToolName();
					}
				}
				// TODO: alphabetize them?
				log.debug("highlighted=" + highlighted);


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



				//Display the right click menu for rating

				Rectangle2D rect = GraphConstants.getBounds(selectedNode.getAttributes());

				popup.showToolOptions();
				popup.showToolRating();
				popup.hideWorkflowOptions();
				popup.hideWorkflowRating();
				popup.initialize(selectedNode.getToolName(), null);
				popup.show(WorkflowVisualization.this, (int)rect.getCenterX(), (int)rect.getCenterY());
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
				if (workflow.contains(node.getToolName()) == false) return false;
			
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
		String toolName;
		String toDisplay;
		public String getToolName() { return toolName; }
		public GraphNode (String label, String toDisplay, Color c)
		{
			super(toDisplay);
			toolName = label;
			this.toDisplay = toDisplay;
			color = c;
		}
	
	}

}
