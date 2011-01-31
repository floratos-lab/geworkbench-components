package org.geworkbench.components.cytoscape;

/**
 * @author my2248
 * @version $Id$ 
 */

import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List;
import java.util.Iterator; 
import java.util.Observable;
import java.util.Observer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; 

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList; 
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.StatUtils;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
 
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
 
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.ProjectNodeAddedEvent;
 
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;
  
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
 
import giny.view.EdgeView;
 
import giny.model.Node;

/* @author yc2480
* @version $Id
*/

@SuppressWarnings("unchecked")
public class MarkerSelectionPanel extends JPanel implements Observer {
	private Log log = LogFactory.getLog(this.getClass());
	public JDialog parent = null;
 
	private JList list;
	private List<Object> markerSetList = new ArrayList<Object>();
 
	private ProgressBar computePb = null;
   // private boolean cancelAction = false;
	protected DSMicroarraySet<? extends DSMicroarray> maSet;

	public MarkerSelectionPanel(JDialog parent, List<Object>markerSetList ) {
		setLayout(new BorderLayout());

		this.parent = parent;	 
		this.maSet = CytoscapeWidget.getInstance().maSet;
		this.markerSetList =  markerSetList;
		init();

	}

	@SuppressWarnings("unchecked")
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
		AdjacencyMatrix matrix = new AdjacencyMatrix();
		AdjacencyMatrix origMatrix = CytoscapeWidget.getInstance().getAdjMatrix();	 
	    matrix.setMicroarraySet(origMatrix.getMicroarraySet());	 
	   
	    DSPanel<DSGeneMarker> selectedObject = (DSPanel<DSGeneMarker>)list.getSelectedValue();
        List selectedGeneList = new ArrayList();
        for (int i = 0; i < selectedObject.size(); i++) {
        	DSGeneMarker marker =   selectedObject.get(i);					 
        	selectedGeneList.add(marker.getGeneName());
		}
        
    	CyNetworkView view = Cytoscape.getCurrentNetworkView();

		if (view != null && Cytoscape.getCurrentNetwork() != null) {
			 
		 
			Iterator<?> iter = view.getEdgeViewsIterator();
		 		
			while (iter.hasNext()) {
				 
				EdgeView edgeView = (EdgeView) iter.next();
				Node source = edgeView.getEdge().getSource();
				Node target = edgeView.getEdge().getTarget();
                String gene1 =  source.getIdentifier();
                String gene2 =  target.getIdentifier();
			    
                String[] list = edgeView.getEdge().getIdentifier().split("/");
	    		list = list[0].split("\\.");	    	 
	    		int serial1 = new Integer(list[0]);
	    		int serial2 = new Integer(list[2]);
	    		String  interactionType= list[1];
	    		
	    		if ( selectedGeneList.contains(gene1) && selectedGeneList.contains(gene2) )
	    		{
	    			matrix.add(serial1, serial2, 0.8f);

					matrix.addDirectional(serial1, serial2,
							interactionType);
					matrix.addDirectional(serial2, serial1,
							interactionType);
	    		}
	    		
                
			}
			
		 }
        
        
         if (matrix.getGeneRows().size() == 0)
         {
        	 JOptionPane
				.showMessageDialog(
						null,
						"There is zero interaction based on your marker set selection.",
						"Information", JOptionPane.INFORMATION_MESSAGE);
        
              return;
         }
	 
	   
         
        adjacencyMatrixdataSet = new AdjacencyMatrixDataSet(matrix, 0, 0.5f, 2,
					"Adjacency Matrix", CytoscapeWidget.getInstance().maSet
							.getLabel(), CytoscapeWidget.getInstance().maSet);
			 
		    
		
	    parent.dispose();
	
		CytoscapeWidget.getInstance().publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
					"Adjacency Matrix Added", null, adjacencyMatrixdataSet));

	
		

	}

 

	public void update(Observable o, Object arg) {
		//cancelAction = true;
		this.computePb.dispose();

	}

	 
	ListModel listModel = new AbstractListModel() {
		public Object getElementAt(int index) {
			return markerSetList.get(index);
		}

		public int getSize() {
			return markerSetList.size();
		}
	};

}