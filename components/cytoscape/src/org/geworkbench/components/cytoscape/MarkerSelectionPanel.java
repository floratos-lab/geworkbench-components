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

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

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
		AdjacencyMatrix origMatrix = CytoscapeWidget.getInstance().getAdjMatrix();	 
		AdjacencyMatrix matrix = new AdjacencyMatrix(null, origMatrix.getMicroarraySet(), origMatrix.getInteractionTypeSifMap());
	   
	    DSPanel<DSGeneMarker> selectedObject = (DSPanel<DSGeneMarker>)list.getSelectedValue();
        List<String> selectedGeneList = new ArrayList<String>();
        for (int i = 0; i < selectedObject.size(); i++) {
        	DSGeneMarker marker =   selectedObject.get(i);					 
        	selectedGeneList.add(marker.getLabel());
		}
        
    	CyNetworkView view = Cytoscape.getCurrentNetworkView();

		if (view != null && Cytoscape.getCurrentNetwork() != null) {		 
			CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();	 
			CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();	 
			Iterator<?> iter = view.getEdgeViewsIterator();
		 		
			while (iter.hasNext()) {
				 
				EdgeView edgeView = (EdgeView) iter.next();
				Node source = edgeView.getEdge().getSource();
				Node target = edgeView.getEdge().getTarget();
                String gene1 =  source.getIdentifier();
                String gene2 =  target.getIdentifier();
              
                Integer serial1 = null, serial2 = null;		    		 
	    		String  interactionType = null;
	    		
	    		serial1 = nodeAttrs.getIntegerAttribute(gene1, "serial");
	    		serial2 = nodeAttrs.getIntegerAttribute(gene2, "serial");
	    		interactionType = edgeAttrs.getStringAttribute(edgeView.getEdge().getIdentifier(), "type");
	    	    if (interactionType != null && !interactionType.trim().equals(""))
	    	    {
	    	    	interactionType = CytoscapeWidget.getInstance().interactionTypeSifMap.get(interactionType);
	    	    }
	    	    
	    		if ( selectedGeneList.contains(gene1) && selectedGeneList.contains(gene2) )
	    		{
	    			matrix.add(serial1, serial2, 0.8f, interactionType);
	    		}
	    		else if ( selectedGeneList.contains(gene1))
	    		{
	    			matrix.addGeneRow(serial1);
	    		}
	    		else if ( selectedGeneList.contains(gene2))
	    		{
	    			matrix.addGeneRow(serial2);
	    		}
                
			}
			
		 }
        
        
         if (matrix.getNodeNumber() == 0)
         {
        	 JOptionPane
				.showMessageDialog(
						null,
						"There is zero interaction based on your marker set selection.",
						"Information", JOptionPane.INFORMATION_MESSAGE);
        
              return;
         }
	 
	   
         
        adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 0.5f,
					"Adjacency Matrix", CytoscapeWidget.getInstance().maSet
							.getLabel(), CytoscapeWidget.getInstance().maSet);
			 
		    
		
	    parent.dispose();
	
		CytoscapeWidget.getInstance().publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
					"Adjacency Matrix Added", null, adjacencyMatrixdataSet));

	
		

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