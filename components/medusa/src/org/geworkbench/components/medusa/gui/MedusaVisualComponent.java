package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.medusa.MedusaDataSet;
import org.geworkbench.components.medusa.MedusaUtil;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ImageSnapshotEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;

import edu.columbia.ccls.medusa.io.SerializedRule;

/**
 * The visual component for MEDUSA. When receiving a project event, the
 * {@link MedusaVisualizationPanel} is created and added.
 * 
 * @author keshav
 * @version $Id: MedusaVisualComponent.java,v 1.10 2007-07-10 17:24:34 keshav Exp $
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

	/**
	 * Publish a subpanel changed event. An example is creating a selection set
	 * when an "Add to set" button is clicked.
	 * 
	 * @param event
	 * @return SubpanelChangedEvent
	 */
	@Publish
	public SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	/**
	 * Publish a snapshot. When taking an image snapshot, the {@link Image} is
	 * first created from the {@link JComponent} of interest. The
	 * {@link Graphics} context is then retrieved from the {@link Image} to
	 * allow off-screen painting to occur.
	 * 
	 * @return {@link ImageSnapshotEvent}
	 */
	@Publish
	public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshot() {
		Image image = null;
		try {
			/* set up the image width, height, and type */
			image = new BufferedImage(medusaVisualizationPanel.getWidth(),
					medusaVisualizationPanel.getHeight(),
					BufferedImage.TYPE_INT_RGB);
			/*
			 * get the Graphics context from the image so we can paint it off
			 * screen
			 */
			Graphics g = image.getGraphics();
			medusaVisualizationPanel.paint(g);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		ImageIcon icon = new ImageIcon(image, "Medusa");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"Medusa Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	/**
	 * Export motifs.
	 */
	public void exportMotifs(List<SerializedRule> srules) {
		// FIXME allowing a null param for now, but I don't like it
		MedusaUtil.writePssmToFile(null, srules);
	}

}
