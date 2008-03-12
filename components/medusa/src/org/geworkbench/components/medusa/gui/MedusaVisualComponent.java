package org.geworkbench.components.medusa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneLayout;

import net.eleritec.docking.defaults.DefaultDockingPort;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
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
 * @version $Id: MedusaVisualComponent.java,v 1.10 2007/07/10 17:24:34 keshav
 *          Exp $
 */
@AcceptTypes(MedusaDataSet.class)
public class MedusaVisualComponent implements VisualPlugin {
	private Log log = LogFactory.getLog(this.getClass());

	private MedusaDataSet dataSet;

	private JPanel component;

	private MedusaVisualizationPanel medusaVisualizationPanel;

	public static final int IMAGE_HEIGHT = 300;
	public static final int IMAGE_WIDTH = 675;

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
						dataSet.getData(), dataSet.getPath());
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
	public org.geworkbench.events.ImageSnapshotEvent publishScreenSnapshot() {
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
			JViewport viewPort0=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).getComponent(1)).getComponent(1)).getComponent(0));
			JViewport viewPort1=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).getComponent(1)).getComponent(2)).getComponent(0));			
			JViewport viewPort2=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(1)).getComponent(2)).getComponent(0));
			JViewport viewPort3=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(2)).getComponent(1)).getComponent(1)).getComponent(0));
			JViewport viewPort4=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(2)).getComponent(1)).getComponent(2)).getComponent(0));			
			JViewport viewPort5=((JViewport)((JScrollPane)((JSplitPane)((JSplitPane)((JPanel)((JTabbedPane)medusaVisualizationPanel.getComponent(0)).getComponent(0)).getComponent(0)).getComponent(2)).getComponent(2)).getComponent(0));
			image = new BufferedImage(viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth()+viewPort5.getComponent(0).getWidth(),
					viewPort1.getComponent(0).getHeight()+viewPort4.getComponent(0).getHeight()+15,
					BufferedImage.TYPE_INT_RGB);
			
			/*
			 * get the Graphics context from the image so we can paint it off
			 * screen
			 */
			
			
			Graphics g = image.getGraphics();
			Color tempColor=g.getColor();
			g.setColor(this.getComponent().getBackground());
			//g.fillRect(0, 0, viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth()+viewPort5.getComponent(0).getWidth(), viewPort1.getComponent(0).getHeight()+viewPort4.getComponent(0).getHeight());
			//for speed, above line changed to following line.
			g.fillRect(0, 0, viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth()+viewPort5.getComponent(0).getWidth(), viewPort1.getComponent(0).getHeight()+15);
			g.setColor(tempColor);
			
			Image bufImage0 = new BufferedImage(viewPort0.getComponent(0).getWidth(), viewPort0.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort0.paintComponents(bufImage0.getGraphics());
			g.drawImage(bufImage0,0,0,viewPort0.getComponent(0).getWidth(),viewPort0.getComponent(0).getHeight(),this.getComponent());

			Image bufImage1 = new BufferedImage(viewPort1.getComponent(0).getWidth(), viewPort1.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort1.paintComponents(bufImage1.getGraphics());
			g.drawImage(bufImage1,viewPort0.getComponent(0).getWidth(),0,viewPort1.getComponent(0).getWidth(),viewPort1.getComponent(0).getHeight(),this.getComponent());

			Image bufImage2 = new BufferedImage(viewPort2.getComponent(0).getWidth(), viewPort2.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort2.paintComponents(bufImage2.getGraphics());
			g.drawImage(bufImage2,viewPort0.getComponent(0).getWidth()+viewPort1.getComponent(0).getWidth(),0,viewPort2.getComponent(0).getWidth(),viewPort2.getComponent(0).getHeight(),this.getComponent());

			Image bufImage3 = new BufferedImage(viewPort3.getComponent(0).getWidth(), viewPort3.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort3.paintComponents(bufImage3.getGraphics());
			g.drawImage(bufImage3,0,viewPort1.getComponent(0).getHeight()+15,viewPort3.getComponent(0).getWidth(),viewPort3.getComponent(0).getHeight(),this.getComponent());
			
			Image bufImage4 = new BufferedImage(viewPort4.getComponent(0).getWidth(), viewPort4.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort4.paintComponents(bufImage4.getGraphics());
			g.drawImage(bufImage4,viewPort3.getComponent(0).getWidth(),viewPort1.getComponent(0).getHeight()+15,viewPort4.getComponent(0).getWidth(),viewPort4.getComponent(0).getHeight(),this.getComponent());

			Image bufImage5 = new BufferedImage(viewPort5.getComponent(0).getWidth(), viewPort5.getComponent(0).getHeight(), BufferedImage.TYPE_INT_RGB);
			viewPort5.paintComponents(bufImage5.getGraphics());
			g.drawImage(bufImage5,viewPort3.getComponent(0).getWidth()+viewPort4.getComponent(0).getWidth(),viewPort1.getComponent(0).getHeight()+15,viewPort5.getComponent(0).getWidth(),viewPort5.getComponent(0).getHeight(),this.getComponent());
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
	public void exportMotifs(List<SerializedRule> srules, String filePath) {
		// FIXME allowing a null param for now, but I don't like it
		MedusaUtil.writePssmToFile(filePath, srules);
	}

}
