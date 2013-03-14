package org.geworkbench.components.cytoscape;

import giny.model.Node;
import giny.view.NodeView;

import java.awt.event.ActionEvent; 
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList; 
import java.util.HashSet;
import java.util.List; 
import java.util.Set;
import javax.swing.AbstractAction; 
import javax.swing.JMenu;
import javax.swing.JMenuItem; 
import javax.swing.JPopupMenu;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory; 
import java.net.URLEncoder;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker; 
import org.geworkbench.bison.datastructure.complex.panels.CSPanel; 
import org.geworkbench.bison.datastructure.complex.panels.DSPanel; 
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.BrowserLauncher;

import cytoscape.Cytoscape;
import ding.view.DNodeView;
import ding.view.NodeContextMenuListener;

public class CompoundExpandMenuListener implements NodeContextMenuListener,
		MouseListener {
	final static Log log = LogFactory.getLog(CompoundExpandMenuListener.class);
    //final static String PUBCHEM_URL= "http://www.ncbi.nlm.nih.gov/pccompound/?db=pccompound&term=";
    final static String PUBCHEM_URL= "http://www.ncbi.nlm.nih.gov/pccompound/?db=pccompound&term=";
    final static String DRUGBANK_URL= "http://www.drugbank.ca/search?query=";
 

	public CompoundExpandMenuListener() {
	 
		 

	}

	/**
	 * @param nodeView
	 *            The clicked NodeView
	 * @param menu
	 *            popup menu to add the Bypass menu
	 */
	public void addNodeContextMenuItems(final NodeView nodeView, JPopupMenu menu) {
		try{
		if (menu == null) {
			menu = new JPopupMenu();
		}
		
		int count = menu.getComponentCount();
        menu.remove(count-1);
        menu.remove(count-2);
        
        String nodeId = nodeView.getNode().getIdentifier().trim();
        JMenu linkOutMenu = new JMenu("LinkOut");
        
		JMenu menuItemCompound = new JMenu("Compound databases");
	 
		linkOutMenu.add(menuItemCompound);
		JMenuItem menuItemPubchem = new JMenuItem(new LinkOutActionListener("Pubchem", PUBCHEM_URL  + URLEncoder.encode(nodeId, "UTF-8")));		
		JMenuItem menuItemDrugbank = new JMenuItem(new LinkOutActionListener("Drugbank",  DRUGBANK_URL  + URLEncoder.encode(nodeId, "UTF-8")));
        menuItemCompound.add(menuItemPubchem);
        menuItemCompound.add(menuItemDrugbank);
        
		JMenu addToSetMenu = new JMenu("Add to set ");
		JMenuItem menuItemIntersection = new JMenuItem(new IntersectionAction(
				"Intersection"));
		JMenuItem menuItemUnion = new JMenuItem(new UnionAction("Union"));
		addToSetMenu.add(menuItemIntersection);
		addToSetMenu.add(menuItemUnion);
		menu.add(linkOutMenu);
		menu.add(addToSetMenu);
		
		}catch(Exception ex)
		{}
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void mouseClicked(MouseEvent e) {	 
	}

	private class IntersectionAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1559843540544628381L;

		public IntersectionAction(String name) {
			super(name);
		}

		@SuppressWarnings( { "unchecked" })
		public void actionPerformed(ActionEvent actionEvent) {
			if (Cytoscape.getCurrentNetworkView() != null
					&& Cytoscape.getCurrentNetwork() != null) {
				java.util.List<DNodeView> nodes = Cytoscape.getCurrentNetworkView()
						.getSelectedNodes();

				if (nodes.size() == 0)
					return;

				log.debug(nodes.size() + " node(s) selected");

				DSPanel<DSGeneMarker> IntersectionMarkers = new CSPanel<DSGeneMarker>(
						"Intersection Genes", "Cytoscape");
				Set<Node> neighborsOfAllNodes = new HashSet<Node>();
				/*
				 * If we have N nodes, we'll need N lists to hold their
				 * neighbors
				 */
				List<Node>[] neighborsOfNodes = new ArrayList[nodes.size()];
				for (int i = 0; i < nodes.size(); i++) {
					DNodeView pnode = nodes.get(i);
					Node node = pnode.getNode();
					List<Node> neighbors = Cytoscape.getCurrentNetworkView()
							.getNetwork().neighborsList(node);
					neighborsOfNodes[i] = neighbors;
				}
				/* Then, we'll need to get the intersection from those lists. */
				/*
				 * The logic here is, if a node does not existing in one of the
				 * lists, it does not exist in the intersection.
				 */
				for (int i = 0; i < neighborsOfNodes[0].size(); i++) {
					boolean atListOneNotContains = false;
					for (int n = 0; n < nodes.size(); n++) {
						if (!neighborsOfNodes[n].contains(neighborsOfNodes[0]
								.get(i))) {
							atListOneNotContains = true;
						}
					}
					if (!atListOneNotContains)// this node exist in all lists
						neighborsOfAllNodes.add((Node) neighborsOfNodes[0]
								.get(i));
				}

				log.debug("neighborsOfAllNodes:#" + neighborsOfAllNodes.size());
				IntersectionMarkers.addAll(CytoscapeWidget.getInstance().nodesToMarkers(neighborsOfAllNodes));
				IntersectionMarkers.setActive(true);
				/*
				 * skip if GeneTaggedEvent is being processed, to avoid event
				 * cycle.
				 */
				if (CytoscapeWidget.getInstance().publishEnabled)
					publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
							DSGeneMarker.class,
							IntersectionMarkers,
							org.geworkbench.events.SubpanelChangedEvent.SET_CONTENTS));

			}
		}

	}

	private void publishSubpanelChangedEvent(
			SubpanelChangedEvent<DSGeneMarker> subpanelChangedEvent) {
		CytoscapeWidget.getInstance().publishSubpanelChangedEvent(
				subpanelChangedEvent);

	}

	private class UnionAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5057482753345747180L;

		public UnionAction(String name) {
			super(name);
		}

		@SuppressWarnings( { "unchecked" })
		public void actionPerformed(ActionEvent actionEvent) {
			if (Cytoscape.getCurrentNetworkView() != null
					&& Cytoscape.getCurrentNetwork() != null) {
				java.util.List<DNodeView> nodes = Cytoscape.getCurrentNetworkView()
						.getSelectedNodes();
				log.debug(nodes.size() + " node(s) selected");

				DSPanel<DSGeneMarker> UnionMarkers = new CSPanel<DSGeneMarker>(
						"Union Genes", "Cytoscape");
				Set<Node> neighborsOfAllNodes = new HashSet<Node>();
				/* Add all neighbors */
				for (int i = 0; i < nodes.size(); i++) {
					DNodeView pnode = nodes.get(i);
					Node node = pnode.getNode();
					List<Node> neighbors = Cytoscape.getCurrentNetworkView()
							.getNetwork().neighborsList(node);
					if (neighbors != null) {
						neighborsOfAllNodes.addAll(neighbors);
					}
				}
				/* Remove selected nodes if exist in neighbor nodes. */
				for (int i = 0; i < nodes.size(); i++) {
					neighborsOfAllNodes.remove(((DNodeView) nodes.get(i))
							.getNode());
				}
				log.debug("neighborsOfAllNodes:#" + neighborsOfAllNodes.size());
				UnionMarkers.addAll(CytoscapeWidget.getInstance().nodesToMarkers(neighborsOfAllNodes));
				UnionMarkers.setActive(true);
				/*
				 * Skip if GeneTaggedEvent is being processed, to avoid event
				 * cycle.
				 */
				if (CytoscapeWidget.getInstance().publishEnabled)
					publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
							DSGeneMarker.class,
							UnionMarkers,
							org.geworkbench.events.SubpanelChangedEvent.SET_CONTENTS));

			}
		}
	}

	class LinkOutActionListener extends AbstractAction {
 
		private static final long serialVersionUID = 1L;
		String urlStr = "";

		public LinkOutActionListener(String name, String urlStr) {
			super(name);
			this.urlStr = urlStr;
		}

		public void actionPerformed(ActionEvent actionEvent) {
			try {

				log.info("Opening " + urlStr);
				BrowserLauncher.openURL(urlStr);
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

}