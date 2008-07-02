package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.geworkbench.util.threading.SwingWorker;

/**
 * @author mhall
 * @author ch2514
 * @version $ID$
 */
@AcceptTypes(MindyDataSet.class)
public class MindyVisualComponent implements VisualPlugin, java.util.Observer {
	static Log log = LogFactory.getLog(MindyVisualComponent.class);

	private MindyDataSet dataSet;

	private JPanel plugin;

	private MindyPlugin mindyPlugin;

	private List<DSGeneMarker> selectedMarkers;

	private DSPanel<DSGeneMarker> selectorPanel;

	private HashMap<ProjectTreeNode, MindyPlugin> ht;

	private ProgressBar progressBar = null;

	private int prevStateMarkers = -1;

	private int currentStateMarkers = -1;

	private int maxMarkers = -1;

	private boolean stopDrawing = false;

	/**
	 * Constructor. Includes a place holder for a MINDY result view (i.e. class
	 * MindyPlugin).
	 * 
	 */
	public MindyVisualComponent() {
		// Just a place holder
		ht = new HashMap<ProjectTreeNode, MindyPlugin>(5);
		plugin = new JPanel(new BorderLayout());
		selectorPanel = null;
		selectedMarkers = null;
	}

	/**
	 * @return The MINDY result view component (of class MindyPlugin)
	 */
	public Component getComponent() {
		return plugin;
	}

	/**
	 * Receives the general ProjectEvent from the framework. Creates MINDY's
	 * data set based on data from the ProjectEvent.
	 * 
	 * @param projectEvent
	 * @param source -
	 *            source of the ProjectEvent
	 */
	@Subscribe(Asynchronous.class)
	public void receive(ProjectEvent projectEvent, Object source) {
		log.debug("MINDY received project event.");
		plugin.removeAll();
		DSDataSet data = projectEvent.getDataSet();
		ProjectTreeNode node = ((ProjectPanel) source).getSelection()
				.getSelectedNode();
		log.debug("event is from node [" + node.toString() + "]");
		if ((data != null) && (data instanceof MindyDataSet)) {
			// Check to see if the hashtable has a mindy plugin associated with
			// the selected project tree node
			if (ht.containsKey(node)) {
				// if so, set mindyPlugin to the one stored in the hashtable
				mindyPlugin = (MindyPlugin) ht.get(node);
				log.debug("plugin already exists for node ["
						+ node.toString() + "]");
			} else {
				// if not, create a brand new mindyPlugin, add to the hashtable
				// (with key=selected project tree node)
				if (dataSet != data) {
					dataSet = ((MindyDataSet) data);

					log.debug("Creating new mindy plugin for node ["
							+ node.toString() + "]");
					// Create mindy gui and display an indeterminate progress
					// bar in the foreground
					progressBar = ProgressBar
							.create(ProgressBar.INDETERMINATE_TYPE);
					progressBar.addObserver(this);
					progressBar.setTitle("MINDY");
					progressBar.setMessage("Creating Display");
					progressBar.start();

					MindyData mindyData = dataSet.getData();
					// List<MindyData.MindyResultRow> mindyRows =
					// mindyData.getData();
					mindyPlugin = new MindyPlugin(mindyData, this);

					// Incorporate selections from marker set selection panel
					DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
							dataSet.getData().getArraySet());
					DSItemList<DSGeneMarker> uniqueMarkers = maView
							.getUniqueMarkers();
					if (stopDrawing) {
						stopDrawing = false;
						progressBar.stop();
						log.warn("Cancelling Mindy GUI.");
						return;
					}
					maxMarkers = uniqueMarkers.size();
					currentStateMarkers = maxMarkers;
					maView.useMarkerPanel(true);

					// Register the mindy plugin with our hashtable for keeping
					// track
					if (mindyPlugin != null)
						ht.put(node, mindyPlugin);
					else {
						log
								.error("Failed to create MINDY plugin for project node: "
										+ node);
						return;
					}
					if (stopDrawing) {
						stopDrawing = false;
						progressBar.stop();
						log.warn("Cancelling Mindy GUI.");
						return;
					}
				}

			}
			// Display the plugin
			plugin.add(mindyPlugin, BorderLayout.CENTER);
			progressBar.stop();
			plugin.revalidate();
			plugin.repaint();
		}
	}

	/**
	 * Receives GeneSelectorEvent from the framework. Extracts markers in the
	 * selected marker sets from the Selector Panel.
	 * 
	 * @param e -
	 *            GeneSelectorEvent
	 * @param source -
	 *            source of the GeneSelectorEvent
	 */
	@SuppressWarnings("unchecked")
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {
		if (dataSet != null) {
			if (e.getPanel() != null)
				this.selectorPanel = e.getPanel();
			else
				log
						.debug("Received Gene Selector Event: Selection panel sent was null");

			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
					dataSet.getData().getArraySet());
			maView.setMarkerPanel(e.getPanel());
			maView.useMarkerPanel(true);
			if ((maView.getMarkerPanel() != null)
					&& (maView.getMarkerPanel().activeSubset() != null)
					&& (maView.getMarkerPanel().activeSubset().size() == 0)) {
				selectedMarkers = null;
				this.prevStateMarkers = this.currentStateMarkers;
				this.currentStateMarkers = this.maxMarkers;
				if (this.currentStateMarkers == this.prevStateMarkers)
					return;
			} else {
				try {
					if ((maView != null) && (maView.getUniqueMarkers() != null)) {
						DSItemList<DSGeneMarker> uniqueMarkers = maView
								.getUniqueMarkers();
						if (uniqueMarkers.size() > 0) {
							selectedMarkers = (List<DSGeneMarker>) uniqueMarkers;
							this.prevStateMarkers = this.currentStateMarkers;
							this.currentStateMarkers = selectedMarkers.size();
						}
					}
				} catch (NullPointerException npe) {
					selectedMarkers = null;
					this.prevStateMarkers = this.currentStateMarkers;
					this.currentStateMarkers = this.maxMarkers;
					log.debug("Gene Selector Event contained no marker data.");
				}
			}

			Iterator it = ht.values().iterator();
			if (selectedMarkers != null) {
				while (it.hasNext()) {
					log
							.debug("***received gene selector event::calling limitMarkers: "
									+ selectedMarkers.size());
					((MindyPlugin) it.next()).limitMarkers(selectedMarkers);
				}
			} else {
				while (it.hasNext()) {
					log
							.debug("***received gene selector event::calling limitMarkers with null");
					((MindyPlugin) it.next()).limitMarkers(null);
				}
			}
		} else {
			log
					.debug("Received Gene Selector Event: Dataset in this component is null");
		}
	}

	/**
	 * Publish SubpanelChangedEvent to the framework to add selected markers to
	 * the marker set(s) on the Selector Panel.
	 * 
	 * @param e -
	 *            SubpanelChangedEvent
	 * @return
	 */
	@Publish
	public SubpanelChangedEvent publishSubpanelChangedEvent(
			SubpanelChangedEvent e) {
		return e;
	}

	/**
	 * Publish ImageSnapshotEvent to the framework to capture an image of the
	 * heat map.
	 * 
	 * @param heatmap -
	 *            MINDY's heat map panel
	 * @return ImageSnapshotEvent
	 */
	@Publish
	public ImageSnapshotEvent createImageSnapshot(Component heatmap) {
		Dimension panelSize = heatmap.getSize();
		BufferedImage image = new BufferedImage(panelSize.width,
				panelSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		heatmap.print(g);
		ImageIcon icon = new ImageIcon(image, "MINDY Heat Map");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"MINDY Heat Map", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	DSPanel getSelectorPanel() {
		return this.selectorPanel;
	}

	List<DSGeneMarker> getSelectedMarkers() {
		return this.selectedMarkers;
	}

	public void update(java.util.Observable ob, Object o) {
		log.debug("initiated close");
		stopDrawing = true;
		progressBar.stop();
	}
}
