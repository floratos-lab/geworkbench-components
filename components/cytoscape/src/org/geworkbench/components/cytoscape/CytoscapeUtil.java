package org.geworkbench.components.cytoscape;

import java.awt.Color;
import giny.view.NodeView;

 
import java.util.HashMap; 
import java.util.Iterator; 
import java.util.Map;
 
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.converter.ValueToStringConverterManager;

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
		double s = 20 / (double)(geneRankingMap.size() - 1);
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

	public static void colorCytoscapeNodes(Map<String, Color> colorMap) {
		CyNetworkView view = Cytoscape.getCurrentNetworkView();
		if (view != null && Cytoscape.getCurrentNetwork() != null) {
			CyAttributes attrs = Cytoscape.getNodeAttributes();
			Iterator<?> iter = view.getNodeViewsIterator();
			CytoscapeWidget.getInstance().publishEnabled = false;
			while (iter.hasNext()) {
				NodeView nodeView = (NodeView) iter.next();
				String id = nodeView.getNode().getIdentifier();
				String displayedName = attrs
						.getStringAttribute(id, "displayedName").trim()
						.toUpperCase();
				if (attrs.hasAttribute(id, CytoscapeWidget.NODE_FILL_COLOR))
					attrs.deleteAttribute(id, CytoscapeWidget.NODE_FILL_COLOR);

				if (colorMap.containsKey(displayedName)) {
					Color c = colorMap.get(displayedName);
					attrs.setAttribute(id, CytoscapeWidget.NODE_FILL_COLOR,
							ValueToStringConverterManager.manager.toString(c));

					nodeView.unselect();
				} else
					nodeView.unselect();

			}
			Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
			// CytoscapeWidget.getInstance().getComponent().repaint();
			CytoscapeWidget.getInstance().publishEnabled = true;
		}
	}
}
