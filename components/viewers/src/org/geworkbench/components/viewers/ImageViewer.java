package org.geworkbench.components.viewers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.geworkbench.builtin.projects.ImageData;
import org.geworkbench.builtin.projects.ImageNode;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

/**
 * A simple image viewer.
 * 
 * @author First Genetic Trust Inc.
 * @version $Id$
 */
@AcceptTypes( { ImageData.class })
public class ImageViewer extends JPanel implements VisualPlugin {
	private static final long serialVersionUID = 8164752383066595089L;

	/**
	 * Canvas on which Image is painted
	 */
	ImageDisplay display = new ImageDisplay();
	/**
	 * Visual Widgets
	 */
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jScrollPane1 = new JScrollPane();

	public ImageViewer() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Interface <code>VisualPlugin</code> method
	 * 
	 * @return visual representation of this component
	 */
	public Component getComponent() {
		return this;
	}

	@Subscribe
	public void receive(ProjectEvent event, Object source) {
		if(event.getTreeNode() instanceof ImageNode) {
			ImageNode imageNode = (ImageNode)event.getTreeNode();
			ImageIcon image = imageNode.image;
			display.setImage(image);
			if (image != null) {
				display.setSize(new Dimension(image.getIconWidth(), image
						.getIconHeight()));
				display.setPreferredSize(new Dimension(image.getIconWidth(),
						image.getIconHeight()));
			}

			repaint();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jScrollPane1.getViewport().add(display, null);
		this.add(jScrollPane1, BorderLayout.CENTER);
	}

}
