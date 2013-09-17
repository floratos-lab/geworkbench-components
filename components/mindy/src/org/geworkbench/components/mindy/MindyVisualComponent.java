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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
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

/**
 * @author mhall
 * @author ch2514
 * @author oshteynb
 * @version $Id$
 */
@AcceptTypes(MindyDataSet.class)
public class MindyVisualComponent implements VisualPlugin, java.util.Observer {
	static Log log = LogFactory.getLog(MindyVisualComponent.class);

	private MindyDataSet dataSet;

	private JPanel plugin;

	private List<DSGeneMarker> selectedMarkers;

	private DSPanel<DSGeneMarker> selectorPanel;

	private HashMap<ProjectTreeNode, MindyPlugin> ht;

	private ProgressBar progressBar = null;

	private volatile boolean stopDrawing = false;

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
	public void receive(ProjectEvent projectEvent, final Object source) {
		/*
		 * added to preconditions checks, until preconditions issue is resolved
		 */
		if (projectEvent == null) {
			return;
		}

		if (source == null) {
			return;
		}

		final DSDataSet<?> data = projectEvent.getDataSet();
		if (!(data instanceof MindyDataSet))
			return;

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				processProjectEvent((MindyDataSet) data, source);
			}

		});
	}

	// invoke from EDT
	private void processProjectEvent(MindyDataSet data, Object source) {

		if (plugin == null) {
			return;
		}

		ProjectSelection ps = ((ProjectPanel) source).getSelection();
		if (ps == null) {
			return;
		}

		ProjectTreeNode node = ps.getSelectedNode();

		MindyPlugin mindyPlugin = null;

		// Check to see if the hashtable has a mindy plugin associated with
		// the selected project tree node
		if (ht.containsKey(node)) {
			// if so, set mindyPlugin to the one stored in the hashtable
			mindyPlugin = ht.get(node);
			log.debug("plugin already exists for node [" + node.toString()
					+ "]");
		} else if (dataSet != data) {
			// if not, create a brand new mindyPlugin, add to the hashtable
			// (with key=selected project tree node)
			dataSet = data;

			log.debug("Creating new mindy plugin for node [" + node.toString()
					+ "]");
			// Create mindy gui and display an indeterminate progress
			// bar in the foreground
			progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
			progressBar.addObserver(this);
			progressBar.setTitle("MINDY");
			progressBar.setMessage("Creating Display");
			progressBar.start();

			MindyData mindyData = dataSet.getData();
			mindyPlugin = new MindyPlugin(mindyData, this);
			mindyPlugin.populateTableTab();
			mindyPlugin.populateModulatorModel();

			// Register the mindy plugin with our hashtable for keeping
			// track
			ht.put(node, mindyPlugin);

			if (stopDrawing) {
				stopDrawing = false;
				progressBar.stop();
				log.warn("Cancelling Mindy GUI.");
				return;
			}
		} else {
			return; // no-op
		}

		// Display the plugin
		plugin.removeAll();
		plugin.add(mindyPlugin, BorderLayout.CENTER);
		progressBar.stop();
		plugin.revalidate();
		plugin.repaint();
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
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {
		if (dataSet == null)
			return;

		final DSPanel<DSGeneMarker> panel = e.getPanel();

		if (panel == null)
			return;

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				processGeneSelectorEvent(panel);
			}

		});
	}

	// invoke from EDT
	private void processGeneSelectorEvent(DSPanel<DSGeneMarker> panel) {
		selectorPanel = panel;

		Iterator<MindyPlugin> it = ht.values().iterator();
		while (it.hasNext()) {
			MindyPlugin p = it.next();
			p.setFilteringSelectorPanel(selectorPanel);
			p.getTableTab().setFirstColumnWidth(30);
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
	public SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			SubpanelChangedEvent<DSGeneMarker> e) {
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
		try{
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
		} catch (OutOfMemoryError err){
			JOptionPane.showMessageDialog(null, "There is not enough memory for this operation.",
					"Warning", JOptionPane.WARNING_MESSAGE);
			log.error("Not enough memory for image snapshot:" + err.getMessage());
			return null;
		}
	}

	DSPanel<DSGeneMarker> getSelectorPanel() {
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
