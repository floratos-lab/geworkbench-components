package org.geworkbench.components.cytoscape;

import giny.view.EdgeView;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

import javax.swing.event.ChangeEvent;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap; 
 
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
 
import cytoscape.Cytoscape; 
import cytoscape.view.CyNetworkView;

/**
 * 
 * @author my2248
 * @version $Id$
 */
public class NetworkRedrawWindow {
 
	private JFrame frame;
	private JPanel topPanel;

	private JPanel bottompanel;

	private JButton redrawNetworkButton = new JButton("Redraw network");
	private JButton graphButton = new JButton("Graph correlation values");
	private JButton createSubNetworkButton = new JButton("Create subnetwork");
	private JButton resetButton = new JButton("Reset network");

	private JLabel sliderLabel = new JLabel("Correlation Threshold: ",
			JLabel.CENTER);
	
	private DecimalFormat myFormatter = new DecimalFormat("0.00");

	private static JLabel selectedValueLabel = new JLabel("0.00");

	private static JSlider thresholdSlider;
	private static Map<String, HashMap<String, Double>> pearsonCorrelationsMap = null;

	private static NetworkRedrawWindow networkRedrawWindow = null;

	/**
	 * Constructor Provides a call-back to the
	 * {@link ComponentConfigurationManagerMenu}.
	 * 
	 * @param ComponentConfigurationManagerMenu
	 */
	private NetworkRedrawWindow() {

		initComponents();
	}

	/**
	 * Load method
	 */
	public static void load(String networkId,
			HashMap<String, Double> correlationResultMap) {
		if (networkRedrawWindow == null) {
			networkRedrawWindow = new NetworkRedrawWindow();
			pearsonCorrelationsMap = new HashMap<String, HashMap<String, Double>>();
		}

		
		thresholdSlider.setValue(0);
		selectedValueLabel.setText("0.00");	 
		pearsonCorrelationsMap.put(networkId, correlationResultMap);

		networkRedrawWindow.frame.setExtendedState(Frame.NORMAL);
		networkRedrawWindow.frame.setVisible(true);
		networkRedrawWindow.frame.toFront();
	}

	/**
	 * Set up the GUI
	 * 
	 * @param void
	 * @return void
	 */
	private void initComponents() {
		frame = new JFrame("Network Redraw");

		topPanel = new JPanel();

		bottompanel = new JPanel();

		sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		thresholdSlider = new JSlider();
		thresholdSlider.setValue(0);
		thresholdSlider.setMinimum(0);
		thresholdSlider.setMaximum(100);
		thresholdSlider.setSnapToTicks(true);
		thresholdSlider.setPaintTicks(true);
		thresholdSlider.setMinorTickSpacing(1);
		thresholdSlider.setMajorTickSpacing(5);
		thresholdSlider.setCursor(java.awt.Cursor
				.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
		thresholdSlider
				.setToolTipText("Move the slider to change the threshold value");
		thresholdSlider
				.addChangeListener(new javax.swing.event.ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						thresholdSlider_stateChanged();
					}
				});

		topPanel.add(sliderLabel);
	 
		topPanel.add(thresholdSlider);

		topPanel.add(selectedValueLabel);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {

				networkRedrawWindow.frame.dispose();
				pearsonCorrelationsMap.clear();
				networkRedrawWindow = null;
			}
		});

		redrawNetworkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				redrawNetwork_actionPerformed(e);
			}
		});

		graphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				graph_actionPerformed(e);
			}
		});

		createSubNetworkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createSubNetworkSelections_actionPerformed(e);
			}

		});

		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset_actionPerformed(e);
			}

		});

		// ======== frame ========
		{
			Container frameContentPane = frame.getContentPane();
			frameContentPane.setLayout(new BorderLayout());

			// ======== outerPanel ========
			{

				frameContentPane
						.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
							public void propertyChange(
									java.beans.PropertyChangeEvent e) {
								if ("border".equals(e.getPropertyName()))
									throw new RuntimeException();
							}
						});

				frameContentPane.add(topPanel, BorderLayout.NORTH);

				// ======== bottompanel ========
				{

					bottompanel.add(redrawNetworkButton);

					bottompanel.add(graphButton);

					bottompanel.add(createSubNetworkButton);

					bottompanel.add(resetButton);

				} // ======== bottompanel ========.
				frameContentPane.add(bottompanel, BorderLayout.SOUTH);
			} // ======== outerPanel ========
			frame.pack();
			frame.setLocationRelativeTo(frame.getOwner());
		} // ============ frame ============

		topPanel.setVisible(true);

		bottompanel.setVisible(true);

		frame.setVisible(true);

	}

	/**
	 * Display a dialog box with a components license in it.
	 * 
	 * @param ActionEvent
	 * @return void
	 */
	private void redrawNetwork_actionPerformed(ActionEvent e) {
		
		CytoscapeWidget.getInstance().resetNetwork();

		CyNetworkView view = Cytoscape.getCurrentNetworkView();
		
		HashMap<String, Double> pearsonCorrelations  = pearsonCorrelationsMap
		.get(view.getIdentifier());
		
		if (pearsonCorrelations == null)
		{
			JOptionPane
			.showMessageDialog(
					null,
					"Please compute edge correlations for the currently selected network view.",

					"Information", JOptionPane.INFORMATION_MESSAGE);
		
		    return;
		}
		if (view != null && Cytoscape.getCurrentNetwork() != null) {
			double selectedSliderValue = (double) thresholdSlider.getValue() / 100;
			Iterator<?> iter = view.getEdgeViewsIterator();
			while (iter.hasNext()) {
				EdgeView edgeView = (EdgeView) iter.next();
				double value = pearsonCorrelations
						.get(edgeView.getEdge().getIdentifier()).doubleValue();
				if (Math.abs(value) < selectedSliderValue)
				{	
					//edgeView.setUnselectedPaint(Color.WHITE);
					//edgeView.setSelectedPaint(Color.WHITE);
					view.hideGraphObject(edgeView);
				
				}
				else if (value < 0)
					edgeView.setUnselectedPaint(Color.BLUE);
				else
					edgeView.setUnselectedPaint(Color.RED);

			}
			CytoscapeWidget.getInstance().getComponent().repaint();
		}
	}

	/**
	 * Persist users component selections Add newly selected components Remove
	 * newly unselected components Leave CCM Window open
	 * 
	 * @param ActionEvent
	 * @return void
	 */
	private void graph_actionPerformed(ActionEvent e) {
		
		
		Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
		frame.setCursor(hourglassCursor);

		Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		frame.setCursor(normalCursor);		

		CyNetworkView view = Cytoscape.getCurrentNetworkView();
		HashMap<String, Double> pearsonCorrelations  = pearsonCorrelationsMap
		.get(view.getIdentifier());
		
		if (pearsonCorrelations == null)
		{
			JOptionPane
			.showMessageDialog(
					null,
					"Please compute edge correlations for the currently selected network view.",

					"Information", JOptionPane.INFORMATION_MESSAGE);
		
		    return;
		}
	
		 Object[] values = pearsonCorrelations.values().toArray();
		 double[] dlist = new double[values.length];
	     for(int i=0; i<dlist.length; i++)
	        {
	        	dlist[i] = (Double)values[i];
	        }
	     String networkName = view.getTitle();
		HistogramGraph.CreateInstance("Edge correlations - " + networkName, dlist);
    	 
		
	}

	/**
	 * Reset selections. Leave Window open
	 * 
	 * @param ActionEvent
	 * @return void
	 */
	private void reset_actionPerformed(ActionEvent e) {

		CytoscapeWidget.getInstance().resetNetwork();

	}

	/**
	 * Reset selections Close CCM Window
	 * 
	 * @param ActionEvent
	 * @return void
	 */
	private void createSubNetworkSelections_actionPerformed(ActionEvent e) {
		 
		HashMap<String, Double> pearsonCorrelations  = pearsonCorrelationsMap
		.get(Cytoscape.getCurrentNetworkView().getIdentifier());
		
		if (pearsonCorrelations == null)
		{
			JOptionPane
			.showMessageDialog(
					null,
					"Please compute edge correlations for the currently selected network view.",

					"Information", JOptionPane.INFORMATION_MESSAGE);
		
		    return;
		}
		
		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;
		AdjacencyMatrix origMatrix = CytoscapeWidget.getInstance().getAdjMatrix();
		AdjacencyMatrix matrix = new AdjacencyMatrix(null, origMatrix.getMicroarraySet());
	 
		Double value = new Double(thresholdSlider.getValue());
		value = value / 100;
		
		if (value == 0)
		{
			adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(origMatrix, 0, 0.5f, 2,
					"Adjacency Matrix", CytoscapeWidget.getInstance().maSet
							.getLabel(), CytoscapeWidget.getInstance().maSet);
			 
			adjacencyMatrixdataSet.addNameValuePair("GENEMAP", CytoscapeWidget.getInstance().getGeneIdToNameMap());
		}
		else
		{
			 
		    for (String key : pearsonCorrelations.keySet())
		    {
		    	if (Math.abs(pearsonCorrelations.get(key)) >= value)
		    	{
		    		String[] list1 = key.split("/");
		    		String[] list2 = list1[0].split("\\.");
		    		int serial1 = -1, serial2 = -1;		    		 
		    		String  interactionType = null;
		    		if (list2.length == 3)
		    		{
		    			serial1 = new Integer(list2[0]);		    		 
		    		    serial2 = new Integer(list2[2]);
		    		    interactionType= list2[1];
		    		}
		    		else if (list2.length == 2)
		    		{
		    			serial1 = new Integer(list2[0]);		    		 
		    		    serial2 = new Integer(list2[1]);
		    		}
		    		
		    		
		    		matrix.add(serial1, serial2, 0.8f);
		    	 
					matrix.addDirectional(serial1, serial2,
							interactionType);
					matrix.addDirectional(serial2, serial1,
							interactionType);				
			
		    		
		    	}
		    	 
		    }
		    
		    if (matrix.getGeneRows().size()==0 && matrix.getGeneRowsNotInMicroarray().size()==0)
			{
				JOptionPane
				.showMessageDialog(
						null,
						"The sub-network you want to create is empty.",

						"Information", JOptionPane.INFORMATION_MESSAGE);
			
			    return;
			}
			
		    
		    
			adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 0, 0.5f, 2,
					"Adjacency Matrix", CytoscapeWidget.getInstance().maSet
							.getLabel(), CytoscapeWidget.getInstance().maSet);
			 
		    
		
		}
	
		CytoscapeWidget.getInstance().publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
					"Adjacency Matrix Added", null, adjacencyMatrixdataSet));

	
	
	}

	void thresholdSlider_stateChanged() {
		Double value = new Double(thresholdSlider.getValue());
		value = value / 100;
		selectedValueLabel.setText(myFormatter.format(value));

	}

}