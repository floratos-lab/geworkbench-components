package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.medusa.MedusaDataSet;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

/**
 * The visual component for MEDUSA. When receiving a project event, the
 * {@link MedusaPlugin} is created and added.
 * 
 * @author keshav
 * @version $Id: MedusaVisualComponent.java,v 1.3 2007-06-15 16:52:42 keshav Exp $
 */
@AcceptTypes(MedusaDataSet.class)
public class MedusaVisualComponent implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	private MedusaDataSet dataSet;

	private JPanel plugin;

	private MedusaPlugin medusaPlugin;

	private ArrayList<DSGeneMarker> selectedMarkers;

	/**
	 * 
	 * 
	 */
	public MedusaVisualComponent() {
		plugin = new JPanel(new BorderLayout());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.engine.config.VisualPlugin#getComponent()
	 */
	public Component getComponent() {
		return plugin;
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
				plugin.removeAll();
				medusaPlugin = new MedusaPlugin(dataSet.getData());
				// medusaPlugin.limitMarkers(selectedMarkers);
				plugin.add(medusaPlugin, BorderLayout.CENTER);
				plugin.revalidate();
				plugin.repaint();
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

}
