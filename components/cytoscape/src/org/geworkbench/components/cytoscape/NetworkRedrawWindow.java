package org.geworkbench.components.cytoscape;

import giny.model.Node;
import giny.view.EdgeView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.events.ProjectNodeAddedEvent;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
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
	 * redrawNetwork action
	 * 
	 * @param ActionEvent
	 * @return void
	 */
	private void redrawNetwork_actionPerformed(ActionEvent e) {

		CytoscapeWidget.getInstance().resetNetwork();

		CyNetworkView view = Cytoscape.getCurrentNetworkView();

		HashMap<String, Double> pearsonCorrelations = pearsonCorrelationsMap
				.get(view.getIdentifier());

		if (pearsonCorrelations == null) {
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
				double value = pearsonCorrelations.get(
						edgeView.getEdge().getIdentifier()).doubleValue();
				if (Math.abs(value) < selectedSliderValue) {
					view.hideGraphObject(edgeView);
				} else if (value < 0)
					edgeView.setUnselectedPaint(Color.BLUE);
				else
					edgeView.setUnselectedPaint(Color.RED);

			}
			CytoscapeWidget.getInstance().getComponent().repaint();
		}
	}

	/**
	 * @param ActionEvent
	 * @return void
	 */
	private void graph_actionPerformed(ActionEvent e) {

		Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
		frame.setCursor(hourglassCursor);

		CyNetworkView view = Cytoscape.getCurrentNetworkView();
		HashMap<String, Double> pearsonCorrelations = pearsonCorrelationsMap
				.get(view.getIdentifier());

		if (pearsonCorrelations == null) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please compute edge correlations for the currently selected network view.",

							"Information", JOptionPane.INFORMATION_MESSAGE);

			return;
		}

		Object[] values = pearsonCorrelations.values().toArray();
		double[] dlist = new double[values.length];
		for (int i = 0; i < dlist.length; i++) {
			dlist[i] = (Double) values[i];
		}
		String networkName = view.getTitle();
		HistogramGraph.CreateInstance("Edge correlations - " + networkName,
				dlist);

		Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
		frame.setCursor(normalCursor);

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
	 * @param ActionEvent
	 * @return void
	 */
	private void createSubNetworkSelections_actionPerformed(ActionEvent e) {

		HashMap<String, Double> pearsonCorrelations = pearsonCorrelationsMap
				.get(Cytoscape.getCurrentNetworkView().getIdentifier());

		if (pearsonCorrelations == null) {
			JOptionPane
					.showMessageDialog(
							null,
							"Please compute edge correlations for the currently selected network view.",

							"Information", JOptionPane.INFORMATION_MESSAGE);

			return;
		}

		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;
		AdjacencyMatrix origMatrix = CytoscapeWidget.getInstance()
				.getAdjMatrix();
		AdjacencyMatrix matrix = new AdjacencyMatrix(null, origMatrix
				.getMicroarraySet(), origMatrix.getInteractionTypeSifMap(), origMatrix.getInteractionEvidenceMap());

		Double value = new Double(thresholdSlider.getValue());
		value = value / 100;

		if (value == 0) {
			adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(origMatrix,
					0.5f, "Adjacency Matrix",
					CytoscapeWidget.getInstance().maSet.getLabel(),
					CytoscapeWidget.getInstance().maSet);
 
		} else {
			CyNetworkView view = Cytoscape.getCurrentNetworkView();
			if (view != null && Cytoscape.getCurrentNetwork() != null) {			 
				CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
				Iterator<?> iter = view.getEdgeViewsIterator();

				while (iter.hasNext()) {

					EdgeView edgeView = (EdgeView) iter.next();
					String edgeIdentifier = edgeView.getEdge().getIdentifier();
					if (Math.abs(pearsonCorrelations.get(edgeIdentifier)) >= value) {
						Node source = edgeView.getEdge().getSource();
						Node target = edgeView.getEdge().getTarget();
						String gene1 = source.getIdentifier();
						String gene2 = target.getIdentifier();

						String interactionType = null;
						String evidence = "0";
						String confidenceValue = "0.8";

						interactionType = edgeAttrs.getStringAttribute(edgeView
								.getEdge().getIdentifier(), "type");
						evidence = edgeAttrs.getStringAttribute(edgeView
								.getEdge().getIdentifier(), "evidence source");
						confidenceValue = edgeAttrs.getStringAttribute(edgeView
								.getEdge().getIdentifier(), "confidence value");
						
						if (evidence != null
								&& !evidence.trim().equals("")) {

							String evId = CytoscapeWidget.getInstance().interactionEvidenceMap
									.get(evidence);
							if (evId != null && !evId.trim().equals(""))
								evidence = evId;							 
						}
						
						if (interactionType != null
								&& !interactionType.trim().equals("")) {

							String type = CytoscapeWidget.getInstance().interactionTypeSifMap
									.get(interactionType);
							if (type != null && !type.trim().equals(""))
								interactionType = type;

						}	
						
						if (confidenceValue == null || confidenceValue.trim().equals(""))
							confidenceValue = "0.8";
						
					 
						AdjacencyMatrix.Node node1 = null;
						AdjacencyMatrix.Node node2 = null;						
						 
					    node1 = new AdjacencyMatrix.Node(
									NodeType.GENE_SYMBOL, gene1);						
						 
						node2 = new AdjacencyMatrix.Node(
									NodeType.GENE_SYMBOL, gene2);

						matrix.add(node1, node2, new Float(confidenceValue), interactionType, new Short(evidence));
					}

				}
			}
			if (matrix.getNodeNumber() == 0) {
				JOptionPane.showMessageDialog(null,
						"The sub-network you want to create is empty.",

						"Information", JOptionPane.INFORMATION_MESSAGE);

				return;
			}

			adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 0.0f,
					"Adjacency Matrix", CytoscapeWidget.getInstance().maSet
							.getLabel(), CytoscapeWidget.getInstance().maSet);

		}

		CytoscapeWidget.getInstance().publishProjectNodeAddedEvent(
				new ProjectNodeAddedEvent("Adjacency Matrix Added", null,
						adjacencyMatrixdataSet));

	}

	void thresholdSlider_stateChanged() {
		Double value = new Double(thresholdSlider.getValue());
		value = value / 100;
		selectedValueLabel.setText(myFormatter.format(value));

	}

}