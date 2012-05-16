package org.geworkbench.components.cytoscape;

/**
 * @author my2248
 * @version $Id$ 
 */

import giny.model.Node;
import giny.view.EdgeView;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;

/**
 * @author yc2480
 * 
 * @version $Id$
 */

@SuppressWarnings("unchecked")
public class MarkerSelectionPanel extends JPanel implements Observer {

	private static final long serialVersionUID = -4774315363368554985L;

	final private JDialog parent;

	private JList list;
	private List<Object> markerSetList = new ArrayList<Object>();

	private ProgressBar computePb = null;

	public MarkerSelectionPanel(JDialog parent, List<Object> markerSetList) {
		setLayout(new BorderLayout());

		this.parent = parent;
		this.markerSetList = markerSetList;
		init();

	}

	private void init() {

		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane pane = new JScrollPane(list);
		JButton continueButton = new JButton("Continue");
		JLabel blankLabel = new JLabel("                                      ");
		JButton cancelButton = new JButton("Cancel");

		continueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				continueButtonActionPerformed();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				markerSetList.clear();
				parent.dispose();

			}
		});

		add(pane, BorderLayout.NORTH);
		add(continueButton, BorderLayout.WEST);
		add(blankLabel);
		add(cancelButton, BorderLayout.EAST);

		list.setSelectedIndex(0);
	}

	private void continueButtonActionPerformed() {

		AdjacencyMatrixDataSet adjacencyMatrixdataSet = null;
		AdjacencyMatrix origMatrix = CytoscapeWidget.getInstance()
				.getAdjMatrix();
		AdjacencyMatrix matrix = new AdjacencyMatrix(null,
				origMatrix.getInteractionTypeSifMap(), origMatrix.getInteractionEvidenceMap());

		DSPanel<DSGeneMarker> selectedObject = (DSPanel<DSGeneMarker>) list
				.getSelectedValue();

		List<String> selectedGeneNameList = new ArrayList<String>();
		for (int i = 0; i < selectedObject.size(); i++) {
			DSGeneMarker marker = selectedObject.get(i);
			selectedGeneNameList.add(marker.getGeneName());
		}

		List<String> selectedGeneLabelList = new ArrayList<String>();
		for (int i = 0; i < selectedObject.size(); i++) {
			DSGeneMarker marker = selectedObject.get(i);
			selectedGeneLabelList.add(marker.getLabel());
		}

		CyNetworkView view = Cytoscape.getCurrentNetworkView();

		if (view != null && Cytoscape.getCurrentNetwork() != null) {
			CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
			CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
			Iterator<?> iter = view.getEdgeViewsIterator();

			while (iter.hasNext()) {

				EdgeView edgeView = (EdgeView) iter.next();
				Node source = edgeView.getEdge().getSource();
				Node target = edgeView.getEdge().getTarget();
				String gene1 = source.getIdentifier();
				String gene2 = target.getIdentifier();

				String interactionType = null;
				String evidence = null;
				String confidenceValue = null;

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
					else
						evidence="0";
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

				Object geneId1 = nodeAttrs.getAttribute(gene1, "geneID");				
				Object geneId2 = nodeAttrs.getAttribute(gene2, "geneID");
				AdjacencyMatrix.Node node1 = null;
				AdjacencyMatrix.Node node2 = null;
				
				if (geneId1 != null && !geneId1.toString().trim().equals(""))
				     node1 = new AdjacencyMatrix.Node(
							NodeType.GENE_SYMBOL, gene1);
				else
					 node1 = new AdjacencyMatrix.Node(
							NodeType.GENE_SYMBOL, gene1, 0);
				
				if (geneId2 != null && !geneId2.toString().trim().equals(""))
				     node2 = new AdjacencyMatrix.Node(
							NodeType.GENE_SYMBOL, gene2);
				else
					 node2 = new AdjacencyMatrix.Node(
							NodeType.GENE_SYMBOL, gene2, 0);
				

				if (((selectedGeneNameList.contains(gene1) || selectedGeneLabelList
						.contains(gene1)) && node1.intId != 0)
						&& ((selectedGeneNameList.contains(gene2) || selectedGeneLabelList
								.contains(gene2))&& node2.intId != 0)) {					
					matrix.add(node1, node2, new Float(confidenceValue), interactionType,  new Short(evidence));
				} else if ((selectedGeneNameList.contains(gene1)
						|| selectedGeneLabelList.contains(gene1)) && node1.intId != 0) {
					matrix.addGeneRow(node1);
				} else if ((selectedGeneNameList.contains(gene2)
						|| selectedGeneLabelList.contains(gene2)) && node2.intId != 0) {
					matrix.addGeneRow(node2);
				}

			}

		}

		if (matrix.getNodeNumber() == 0) {
			JOptionPane
					.showMessageDialog(
							null,
							"There is zero interaction based on your marker set selection.",
							"Information", JOptionPane.INFORMATION_MESSAGE);

			return;
		}

		adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 0.0f,
				"Adjacency Matrix", CytoscapeWidget.getInstance().maSet
						.getLabel(), CytoscapeWidget.getInstance().maSet);

		parent.dispose();

		CytoscapeWidget.getInstance().publishProjectNodeAddedEvent(
				new ProjectNodeAddedEvent("Adjacency Matrix Added", null,
						adjacencyMatrixdataSet));

	}

	public void update(Observable o, Object arg) {
		// cancelAction = true;
		this.computePb.dispose();

	}

	ListModel listModel = new AbstractListModel() {

		private static final long serialVersionUID = 7949364305898483395L;

		public Object getElementAt(int index) {
			return markerSetList.get(index);
		}

		public int getSize() {
			return markerSetList.size();
		}
	};

}