package org.geworkbench.components.cytoscape;

/**
 * 
 * Visual component to display t-test result.  
 * @author my2248
 * @version $Id$ 
 */


import giny.view.NodeView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.converter.ValueToStringConverterManager; 

public class TTestResultSelectionPanel extends JPanel {
	 
	private static final long serialVersionUID = 8102056731009933410L;
	private Log log = LogFactory.getLog(this.getClass());
	public JDialog parent = null;
	//public CytoscapeWidget cytoscapeWidget = null;
	private JList list;
	private List<String> TTestNameList = new ArrayList<String>();
	protected Map<String, CSSignificanceResultSet<DSGeneMarker>> ttestResultMap;
    protected DSMicroarraySet maSet;
	public TTestResultSelectionPanel(JDialog parent, Map<String, CSSignificanceResultSet<DSGeneMarker>> map, DSMicroarraySet maSet) {
		setLayout(new BorderLayout());

		this.parent = parent;		 
		this.ttestResultMap = map;
		this.maSet = maSet;		
		init();

	}

	private void init() {
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		TTestNameList.addAll(ttestResultMap.keySet());
		String s = TTestNameList.get(0) + "                                                                    ";
		TTestNameList.set(0, s);
		Collections.sort(TTestNameList);
		JScrollPane pane = new JScrollPane(list);
		JButton selectButton = new JButton("Select");
		JButton cancelButton = new JButton("Cancel");

		selectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectButtonActionPerformed();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.dispose();
			}
		});

		add(pane, BorderLayout.NORTH);
		add(selectButton, BorderLayout.WEST);
		add(cancelButton, BorderLayout.EAST);
		
	
		list.setSelectedIndex(0);
	}

	private void selectButtonActionPerformed() {
		
		// clear all node color
		
		String selectedTTestName = list.getSelectedValue().toString().trim();
		Map<String, List<Object>> tTestResultSetColorMap = new HashMap<String, List<Object>>();

		CSSignificanceResultSet<DSGeneMarker> ttestResultSet =
			ttestResultMap
				.get(selectedTTestName);		
		
		tTestResultSetColorMap = getTTestResultSetColorMap(ttestResultSet);
		
		CyNetworkView view = Cytoscape.getCurrentNetworkView();
	    if (view != null && Cytoscape.getCurrentNetwork() != null) {
			CyAttributes attrs = Cytoscape.getNodeAttributes();			 
			Iterator<?> iter = view.getNodeViewsIterator();
			CytoscapeWidget.getInstance().publishEnabled = false;
			while (iter.hasNext()) {
				NodeView nodeView = (NodeView) iter.next();				 
				String id = nodeView.getNode().getIdentifier();
				String displayedName = attrs.getStringAttribute(id, "displayedName").trim().toUpperCase();
				if (attrs.hasAttribute(id, CytoscapeWidget.NODE_FILL_COLOR))
					attrs.deleteAttribute(id, CytoscapeWidget.NODE_FILL_COLOR);
			 
				if (tTestResultSetColorMap.containsKey(displayedName)) {												 
						Color c = (Color)tTestResultSetColorMap.get(displayedName).get(1);						 
						attrs.setAttribute(id, CytoscapeWidget.NODE_FILL_COLOR, ValueToStringConverterManager.manager.toString(c));
						 
						//nodeView.setUnselectedPaint(c);
						//nodeView.setSelectedPaint(c);
						//nodeView.select();
						nodeView.unselect();
				} else
					    nodeView.unselect();
		
			}	
			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);			 
			CytoscapeWidget.getInstance().publishEnabled = true; 
		}		
		
		this.parent.dispose();

	
	}
	
	
	private Map<String, List<Object>> getTTestResultSetColorMap(CSSignificanceResultSet<DSGeneMarker> ttestResultSet)	
	{
		double minTValue = 0;
		double maxTValue = 0;
		int numOfPositiveTValues = 0; //included 0
		Map<String, List<Object>> tTestResultSetColorMap = new HashMap<String, List<Object>>();		
		DSPanel<DSGeneMarker> significantPanel = ttestResultSet
		.getSignificantMarkers();
		
		Hashtable<Double, DSGeneMarker> hash = new Hashtable<Double, DSGeneMarker>();
		for (DSGeneMarker m : significantPanel)  {		 
			double tValue = ttestResultSet.getTValue(m);			
			hash.put(tValue,m);
			if (minTValue > tValue) minTValue = tValue;
			if (maxTValue < tValue) maxTValue = tValue;
			if (tValue>=0) numOfPositiveTValues++;
		}
		Double[] keys = (Double[]) hash.keySet().toArray(new Double[0]);
		Arrays.sort(keys);
		// hash map to store rank of genes.		 
		Map<DSGeneMarker, Integer> Gene2RankMap = new HashMap<DSGeneMarker, Integer>();
		int cx = keys.length-1; //decreasing order
		for (Double key : keys) {
			DSGeneMarker marker = hash.get(key);		 
			Gene2RankMap.put(marker, new Integer(cx));
			log.debug(marker.getShortName()+" ranked "+cx);
			cx--;
		}		
	
		for (DSGeneMarker m : significantPanel) {
			String name = m.getShortName().trim().toUpperCase();
			int rank = Gene2RankMap.get(m);
			Double tValue = ttestResultSet.getTValue(m);	
			Color c = CytoscapeUtil.calculateColor(keys.length, numOfPositiveTValues,minTValue, maxTValue, rank, tValue);			 
			List<Object> list = new ArrayList<Object>();
			list.add(tValue);
			list.add(c);		 
			if (name.contains(CytoscapeWidget.GENE_SEPARATOR)) {
				String[] names = name.split(CytoscapeWidget.GENE_SEPARATOR);
				for (int i = 0; i < names.length; i++) {	
					String geneName = names[i].trim().toUpperCase();
					if (tTestResultSetColorMap.containsKey(geneName) && needReplace((Double)tTestResultSetColorMap.get(geneName).get(0), tValue)== false)
					    continue;
					tTestResultSetColorMap.put(geneName, list);
				}
			} else {
				if (tTestResultSetColorMap.containsKey(name) && needReplace((Double)tTestResultSetColorMap.get(name).get(0), tValue)== false)
				    continue;
				tTestResultSetColorMap.put(name, list);
			}

		}
		
		return tTestResultSetColorMap;

	}
	
	private boolean needReplace(Double d1, Double d2)
	{
		if (Math.abs(d2.doubleValue()) > Math.abs(d1.doubleValue()))
		{
			return true;
		}
		else
			return false;
	}
	
	 
   
	ListModel listModel = new AbstractListModel() {	 
	 
		private static final long serialVersionUID = 8144764953969882081L;

		public Object getElementAt(int index) {
			return TTestNameList.get(index);
		}

		public int getSize() {
			return TTestNameList.size();
		}
	};

}