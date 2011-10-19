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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.StatUtils;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.util.ProgressBar;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
 

@SuppressWarnings("unchecked")
public class ArraysSelectionPanel extends JPanel   {
	 
	private static final long serialVersionUID = 6362289910874562077L;
	private Log log = LogFactory.getLog(this.getClass());
	public JDialog parent = null;

	private JList list;
	private List<Object> arraySetList = new ArrayList<Object>();
	 
	private ProgressBar computePb = null;
	 
	private final DSMicroarraySet maSet;
	private CalculationWorker worker = null;

	public ArraysSelectionPanel(JDialog parent) {
		setLayout(new BorderLayout());

		this.parent = parent;
		this.maSet = CytoscapeWidget.getInstance().maSet;
		init();

	}

	private void init() {

		String s = "Use all arrays                                                         ";
		arraySetList.add(s);
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = (DSAnnotationContext<DSMicroarray>) manager.getCurrentContext(maSet);

		DSItemList<DSPanel<DSMicroarray>> itemList = context.getLabelTree().panels();

		for (DSPanel<DSMicroarray> dp : itemList) {
			if (dp.getNumberOfProperItems() > 1)
				arraySetList.add(dp);
		}

		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Collections.sort(arraySetList);
		JScrollPane pane = new JScrollPane(list);
		JButton continueButton = new JButton("Continue");
		JButton cancelButton = new JButton("Cancel");

		continueButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				continueButtonActionPerformed();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.dispose();
			}
		});

		add(pane, BorderLayout.NORTH);
		add(continueButton, BorderLayout.WEST);
		add(cancelButton, BorderLayout.EAST);

		list.setSelectedIndex(0);
	}
	 
	private void continueButtonActionPerformed() {

		parent.dispose();
		computePb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);

		worker = new CalculationWorker(computePb);
		worker.execute();

	}

	public double[] getValues(Node node, CyAttributes attrs, int[] serials) {
		double[] values = null;
		
		String nodeId = node.getIdentifier();
		Object geneId = attrs.getAttribute(nodeId,"geneID");						
		if (geneId == null || geneId.toString().trim().equals(""))
		   return null;
		
		String nodeDisplayedName = attrs.getStringAttribute(nodeId, "displayedName");
	     
		Vector<DSGeneMarker> markerSet = null;	 
	    
		
		markerSet = ((CSMicroarraySet)maSet).getMarkers().getMatchingMarkers(nodeDisplayedName);
		 
	    if (markerSet == null || markerSet.size() == 0)
			return null;

		if (serials != null && serials.length > 0) {
			values = new double[serials.length];
			double[] valuesForOneArray = new double[markerSet.size()];
			for (int i = 0; i < serials.length; i++) {

				for (int j=0; j< markerSet.size(); j++)
					valuesForOneArray[j] = maSet.getValue(markerSet.get(j).getSerial(),
							serials[i]);
				values[i] = StatUtils.mean(valuesForOneArray);
			}
		} else // use all arrays
		{
			int num = maSet.size();

			values = new double[num];
			double[] valuesForOneArray = new double[markerSet.size()];

			for (int i = 0; i < num; i++) {

				for (int j = 0; j < markerSet.size(); j++) {
					valuesForOneArray[j] = maSet.getValue(markerSet.get(j).getSerial(), i);
				}

				values[i] = StatUtils.mean(valuesForOneArray);
			}
		}

		return values;

	}

	private double calculate(double[] x_values, double[] y_values) {
		double result = 0;
		if (x_values != null && y_values != null) {
			double sum_x = 0.0, sum_y = 0.0, sum_xy = 0.0, sum_x_power2 = 0.0, sum_y_power2 = 0.0;
			for (int i = 0; i < x_values.length; i++) {
				sum_x += x_values[i];
				sum_y += y_values[i];
				sum_xy += x_values[i] * y_values[i];
				sum_x_power2 += x_values[i] * x_values[i];
				sum_y_power2 += y_values[i] * y_values[i];
			}

			double a = sum_xy - (sum_x * sum_y) / x_values.length;
			double b = sum_x_power2 - (sum_x * sum_x) / x_values.length;
			double c = sum_y_power2 - (sum_y * sum_y) / x_values.length;

			result = a / (Math.sqrt(b * c));
		}
		return result;
	}  
	
	
	ListModel listModel = new AbstractListModel() {	 
		private static final long serialVersionUID = 4583841683367907053L;

		public Object getElementAt(int index) {
			return arraySetList.get(index);
		}

		public int getSize() {
			return arraySetList.size();
		}
	};

	private class CalculationWorker extends SwingWorker<Void, Void> implements
			Observer {
		ProgressBar pb = null;
		HashMap<String, Double> pearsonCorrelationMap = null;

		CalculationWorker(ProgressBar pb) {
			super();
			this.pb = pb;
		}

		@Override
		protected void done() {
			if (this.isCancelled()) {
				log.info("Calculation task is cancel.");
				
			} else {
				log.info("Calculation task is done.");
				parent.dispose();
				pb.dispose();
				NetworkRedrawWindow.load(Cytoscape.getCurrentNetworkView()
						.getIdentifier(), pearsonCorrelationMap);
			}
		}

		@Override
		protected Void doInBackground() {

			pb.addObserver(this);
			pb.setTitle("Compute Pearson's correlatio");
			pb.setMessage("Process computation ...");
			pb.start();
			pb.toFront();

			int[] arraySerials = null;

			Object selectedObject = list.getSelectedValue();

			if (!selectedObject.toString().trim().equalsIgnoreCase(
					"Use all arrays")) {
				CSPanel<DSMicroarray> panel = (CSPanel<DSMicroarray>) selectedObject;
				arraySerials = new int[panel.getNumberOfProperItems()];
				for (int i = 0; i < panel.getNumberOfProperItems(); i++) {
					DSMicroarray item = (DSMicroarray) panel.getProperItem(i);
					arraySerials[i] = item.getSerial();
				}

			}

			CyNetworkView view = Cytoscape.getCurrentNetworkView();

			if (view != null && Cytoscape.getCurrentNetwork() != null) {

				CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
				Iterator<?> iter = view.getEdgeViewsIterator();
				// cytoscapeWidget.publishEnabled = false;
				pearsonCorrelationMap = new HashMap<String, Double>();
				while (iter.hasNext()) {
					if (this.isCancelled() == true) {
						pearsonCorrelationMap.clear();
						pb.dispose();
						break;
					}
					EdgeView edgeView = (EdgeView) iter.next();
					Node source = edgeView.getEdge().getSource();
					Node target = edgeView.getEdge().getTarget();

					double[] source_values = getValues(source, nodeAttrs,
							arraySerials);
					double[] target_values = getValues(target, nodeAttrs,
							arraySerials);
					double pcResult = 0;
					pcResult = calculate(source_values, target_values);
					pcResult = ((double) Math.round(pcResult * 100000)) / 100000;

					if (log.isDebugEnabled()) {
						if (pcResult > 1 || pcResult < -1) {
							JOptionPane.showMessageDialog(null,
									"The pearson correlation value for edge "
											+ source.getIdentifier() + "-"
											+ target.getIdentifier() + " is "
											+ pcResult
											+ ". It does not look correct.",

									"Information",
									JOptionPane.INFORMATION_MESSAGE);
						}
					}

					pearsonCorrelationMap.put(edgeView.getEdge()
							.getIdentifier(), pcResult);
				}
			}

			return null;
		}

		public void update(Observable o, Object arg) {		 
			worker.cancel(true);		 
		}

	}

}