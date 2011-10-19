package org.geworkbench.components.cytoscape;

import giny.model.Node;
import giny.view.NodeView;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.DataSetNode;
import org.geworkbench.builtin.projects.DataSetSubNode;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.Util;

import cytoscape.Cytoscape;
import ding.view.DNodeView;
import ding.view.NodeContextMenuListener;

public class ExpandMenuListener implements NodeContextMenuListener,
		MouseListener {
	final static Log log = LogFactory.getLog(ExpandMenuListener.class);
 
	final private DSMicroarraySet maSet;

	public ExpandMenuListener(final CytoscapeWidget cytoscapeWidget) {
	 
		maSet = cytoscapeWidget.maSet;

	}

	/**
	 * @param nodeView
	 *            The clicked NodeView
	 * @param menu
	 *            popup menu to add the Bypass menu
	 */
	public void addNodeContextMenuItems(final NodeView nodeView, JPopupMenu menu) {

		if (menu == null) {
			menu = new JPopupMenu();
		}

		JMenu addToSetMenu = new JMenu("Add to set ");
		JMenuItem menuItemIntersection = new JMenuItem(new IntersectionAction(
				"Intersection"));
		JMenuItem menuItemUnion = new JMenuItem(new UnionAction("Union"));
		addToSetMenu.add(menuItemIntersection);
		addToSetMenu.add(menuItemUnion);
		menu.add(addToSetMenu);
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

		if (e.isMetaDown()) {

			JPopupMenu menu = new JPopupMenu();

			JMenuItem menuItemTTestResults = new JMenuItem(
					new ShowTTestResultAction("Show t-test results"));

			JMenuItem menuItemComputeEdgeCorrelations = new JMenuItem(
					new ComputeEdgeCorrelationsAction(
							"Compute edge correlations"));

			JMenuItem menuItemCreateSubNetwork = new JMenuItem(
					new CreateSubnetworkAction("Create subnetwork"));

			JMenuItem menuItemClear = new JMenuItem(new ClearNodeColorAction(
					"Restore network"));
			menu.add(menuItemTTestResults);
			menu.add(menuItemComputeEdgeCorrelations);
			menu.add(menuItemCreateSubNetwork);
			menu.add(menuItemClear);

			menu.show(e.getComponent(), e.getPoint().x, e.getPoint().y);

		}
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

	private class ShowTTestResultAction extends AbstractAction {

		private static final long serialVersionUID = 5057482753345747183L;

		public ShowTTestResultAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent actionEvent) {

			Map<String, CSSignificanceResultSet<DSGeneMarker>> map = new HashMap<String, CSSignificanceResultSet<DSGeneMarker>>();
			DataSetNode dataSetNode = ProjectPanel.getInstance().getSelection()
					.getSelectedDataSetNode();
			searchTestResultNodes(dataSetNode, map, CSTTestResultSet.class);

			if (map.size() == 0) {
				JOptionPane
						.showMessageDialog(
								null,
								"There are no T-Test result nodes associated with the currently selected microarray set.",

								"Information", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JDialog dialog = new JDialog();
				dialog.add(new TTestResultSelectionPanel(dialog, map, maSet));
				dialog.setModal(true);
				dialog.setTitle("Please Select T-Test Result");
				dialog.pack();
				Util.centerWindow(dialog);
				dialog.setVisible(true);
			}

		}
	}

	private class ComputeEdgeCorrelationsAction extends AbstractAction {

		private static final long serialVersionUID = 5057482753345747184L;

		public ComputeEdgeCorrelationsAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent actionEvent) {

			JDialog dialog = new JDialog();
			dialog.add(new ArraysSelectionPanel(dialog));
			dialog.setModal(true);
			dialog.setTitle("Select microarrays to use");
			dialog.pack();
			Util.centerWindow(dialog);
			dialog.setVisible(true);
		}
	}

	private class CreateSubnetworkAction extends AbstractAction {

		private static final long serialVersionUID = 5057482753345747185L;

		public CreateSubnetworkAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent actionEvent) {
			List<Object> markerSetList = new ArrayList<Object>();

			DSAnnotationContextManager manager = CSAnnotationContextManager
					.getInstance();
		 
			DSAnnotationContext<DSGeneMarker> markerGroups = manager
					.getCurrentContext(maSet.getMarkers());
			DSItemList<DSPanel<DSGeneMarker>> itemList = markerGroups
					.getLabelTree().panels();
			for (DSPanel<DSGeneMarker> dp : itemList) {
				if (dp.getNumberOfProperItems() > 0)
					markerSetList.add(dp);
			}

			if (markerSetList.size() == 0) {
				JOptionPane
						.showMessageDialog(
								null,
								"You need to define non-empty marker sets in order to create a subnetwork.",
								"Information", JOptionPane.INFORMATION_MESSAGE);

			} else {
			
				JDialog dialog = new JDialog();
				dialog.add(new MarkerSelectionPanel(dialog, markerSetList));
				dialog.setModal(true);
				dialog.setTitle("Select markers to use");
				dialog.pack();
				Util.centerWindow(dialog);
				dialog.setVisible(true);
			}
		}
	}

	private class ClearNodeColorAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5057482753345747182L;

		public ClearNodeColorAction(String name) {
			super(name);
		}

		public void actionPerformed(ActionEvent actionEvent) {
			CytoscapeWidget.getInstance().resetNetwork();
		}
	}	 
 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void searchTestResultNodes(ProjectTreeNode pnode,
			Map<String, CSSignificanceResultSet<DSGeneMarker>> map,
			Class<? extends CSSignificanceResultSet> clazz) {
		if (pnode instanceof DataSetSubNode) {
			DSAncillaryDataSet<DSBioObject> dNodeFile = ((DataSetSubNode) pnode)._aDataSet;
			if (clazz.isInstance(dNodeFile)) {
				map.put(dNodeFile.getDataSetName(),
						(CSSignificanceResultSet) dNodeFile);
			}
		}

		Enumeration children = pnode.children();
		while (children.hasMoreElements()) {
			ProjectTreeNode child = (ProjectTreeNode) children.nextElement();
			searchTestResultNodes(child, map, clazz);
		}
	}

}