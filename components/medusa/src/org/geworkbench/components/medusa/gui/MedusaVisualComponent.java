package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.medusa.MedusaDataSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

/**
 * The visual component for MEDUSA. When receiving a project event, the
 * {@link MedusaVisualizationPanel} is created and added.
 * 
 * @author keshav
 * @version $Id: MedusaVisualComponent.java,v 1.8 2007-06-26 15:05:15 keshav Exp $
 */
@AcceptTypes(MedusaDataSet.class)
public class MedusaVisualComponent implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	private MedusaDataSet dataSet;

	private JPanel component;

	private MedusaVisualizationPanel medusaVisualizationPanel;

	private ArrayList<DSGeneMarker> selectedMarkers;

	/**
	 * 
	 * 
	 */
	public MedusaVisualComponent() {
		component = new JPanel(new BorderLayout());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	public Component getComponent() {
		return component;
	}

	/**
	 * 
	 * @param projectEvent
	 * @param source
	 */
	@Subscribe
	public void receive(ProjectEvent projectEvent, Object source) {
		log.debug("MEDUSA received project event.");
		DSDataSet data = projectEvent.getDataSet();
		if ((data != null) && (data instanceof MedusaDataSet)) {
			ProgressBar pBar = Util.createProgressBar("Medusa Analysis");
			pBar.setMessage("Rendering images");
			pBar.start();
			if (dataSet != data) {
				dataSet = ((MedusaDataSet) data);
				component.removeAll();
				medusaVisualizationPanel = new MedusaVisualizationPanel(this,
						dataSet.getData());
				// medusaPlugin.limitMarkers(selectedMarkers);
				component.add(medusaVisualizationPanel, BorderLayout.CENTER);
				component.revalidate();
				component.repaint();
			}
			pBar.stop();
		}
	}

	// @Subscribe
	// public void receive(GeneSelectorEvent e, Object source) {
	// if (dataSet != null && e.getPanel() != null) {
	// DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new
	// CSMicroarraySetView<DSGeneMarker, DSMicroarray>(
	// dataSet.getData().getArraySet());
	// maView.setMarkerPanel(e.getPanel());
	// maView.useMarkerPanel(true);
	// if (maView.getMarkerPanel().activeSubset().size() == 0) {
	// selectedMarkers = null;
	// } else {
	// DSItemList<DSGeneMarker> uniqueMarkers = maView
	// .getUniqueMarkers();
	// if (uniqueMarkers.size() > 0) {
	// selectedMarkers = new ArrayList<DSGeneMarker>();
	// for (Iterator<DSGeneMarker> iterator = uniqueMarkers
	// .iterator(); iterator.hasNext();) {
	// DSGeneMarker marker = iterator.next();
	// log.debug("Selected " + marker.getShortName());
	// selectedMarkers.add(marker);
	// }
	// }
	// }
	// // medusaPlugin.limitMarkers(selectedMarkers);
	// } else {
	// log
	// .error("Dataset in this component is null, or selection sent was null");
	// }
	// }

	/**
	 * @param event
	 * @return SubpanelChangedEvent
	 */
	@Publish
	public SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	/**
	 * 
	 * @return {@link ImageSnapshotEvent}
	 */
	@Publish
	public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshot() {
		// Dimension panelSize = graph.getSize();
		// BufferedImage image = new BufferedImage(panelSize.width,
		// panelSize.height,
		// BufferedImage.TYPE_INT_RGB);
		// Graphics g = image.getGraphics();
		// graph.paint(g);
		// ImageIcon icon = new ImageIcon(image, "EVD Plot");
		ImageIcon icon = new ImageIcon("Medusa");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"Medusa Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

}
