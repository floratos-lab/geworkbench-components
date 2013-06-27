package org.geworkbench.components.cytoscape;

import java.awt.Color;
import giny.view.NodeView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import cytoscape.Cytoscape; 
import cytoscape.view.CyNetworkView; 
import cytoscape.visual.mappings.DiscreteMapping; 
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;

public class CytoscapeUtil {

	public static Color calculateColor(int numMarkers,
			int numOfPositiveTValues, double minTValue, double maxTValue,
			int rank, double tValue) {
		int maxAbs = (int) Math.max(Math.abs(minTValue), Math.abs(maxTValue));
		// System.out.println(numMarkers+","+minTValue+","+maxTValue+","+rank+","+tValue);

		Color result = null;

		if (maxAbs != 0) {
			int colorindex = (int) (255 * (tValue) / Math.abs(maxAbs));
			if (colorindex < 0) {
				colorindex = Math.abs(colorindex);
				if (colorindex > 255)
					colorindex = 255;
				result = (new Color(255 - colorindex, 255 - colorindex, 255));
			} else if (colorindex <= 255) {
				result = (new Color(255, 255 - colorindex, 255 - colorindex));
			} else { // if (colorindex > 255)
				colorindex = 255;
				result = (new Color(255, 255 - colorindex, 255 - colorindex));
			}

		}
		return result;
	}

	public static Map<String, Color> getDiffExprColorMap(
			Map<String, Integer> geneRankingMap) {
		double minTValue = -10d;
		double maxTValue = 10d;
		int numOfPositiveTValues = 0; // included 0
		Map<String, Color> diffExprColorMap = new HashMap<String, Color>();
		Map<String, Double> diffExprTValueMap = new HashMap<String, Double>();

		int n = geneRankingMap.size();
		double s = 20 / (double) (geneRankingMap.size() - 1);
		for (String key : geneRankingMap.keySet()) {
			int i = geneRankingMap.get(key).intValue();
			if (i == 0)
				diffExprTValueMap.put(key, 10d);
			else
				diffExprTValueMap.put(key, 10 - i * s);
			if (diffExprTValueMap.get(key) >= 0)
				numOfPositiveTValues++;
		}

		for (String key : geneRankingMap.keySet()) {

			Color c = calculateColor(n, numOfPositiveTValues, minTValue,
					maxTValue, geneRankingMap.get(key),
					diffExprTValueMap.get(key));

			diffExprColorMap.put(key, c);

		}

		return diffExprColorMap;

	}

	@SuppressWarnings("deprecation")
	public static void colorCytoscapeNodes(VisualStyle visualStyle,
			Map<String, Color> colorMap) {

		NodeAppearanceCalculator nac = visualStyle
				.getNodeAppearanceCalculator();
		Vector<?> v = nac.getCalculator(
				VisualPropertyType.NODE_FILL_COLOR).getMappings();
		DiscreteMapping nodeColorDm = null;
		for (int i = 0; i < v.size(); i++) {
			if (v.get(i) instanceof DiscreteMapping) {
				nodeColorDm = (DiscreteMapping) v.get(i);
				break;
			}

		}
		nodeColorDm.setControllingAttributeName("ID", Cytoscape
				.getCurrentNetwork(), false);
		CyNetworkView view = Cytoscape.getCurrentNetworkView();
		if (view != null && Cytoscape.getCurrentNetwork() != null) {			 
			Iterator<?> iter = view.getNodeViewsIterator();
			CytoscapeWidget.getInstance().publishEnabled = false;
			while (iter.hasNext()) {
				NodeView nodeView = (NodeView) iter.next();
				String id = nodeView.getNode().getIdentifier();
				
			 
				if (colorMap.containsKey(id)) {
					Color c = colorMap.get(id);
					nodeColorDm.putMapValue(id, c);
				 
				}  
			}
		  
		}
	}
}
