package org.geworkbench.components.sequenceretriever;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.util.sequences.SequenceViewWidgetPanel;

/**
 * 
 * @author not attributable
 * @version $Id$
 */
public final class SingleSequenceViewPanel extends SequenceViewWidgetPanel {
	private static final long serialVersionUID = 7100195393198518647L;

	private JMenuItem imageSnapshotItem = new JMenuItem("Image Snapshot");

	public SingleSequenceViewPanel() {
			imageSnapshotItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createImageSnapshot();
				}

			});
	}

	public void setRetrievedSequencesPanel(
			RetrievedSequencesPanel retrievedSequencesPanel) {
		// no effect
	}

	private org.geworkbench.events.ImageSnapshotEvent createImageSnapshot() {
		Dimension panelSize = this.getSize();
		BufferedImage image = new BufferedImage(panelSize.width,
				panelSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		this.paint(g);
		ImageIcon icon = new ImageIcon(image, "Promoter Snapshot");
		org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent(
				"Promoter Snapshot", icon,
				org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		return event;
	}

	public void setSingleSequenceView(boolean singleSequenceView) {
		this.singleSequenceView = singleSequenceView;
	}

	public void setSequenceDB(DSSequenceSet<DSSequence> sequenceDB) {
		this.sequenceDB = sequenceDB;
	}

	public void setLineView(boolean lineView) {
		this.lineView = lineView;
		revalidate();
	}

	public void setSeqXclickPoint(int seqXclickPoint) {
		this.seqXclickPoint = seqXclickPoint;
	}

	/**
	 * initialize
	 */
	public void initialize() {
		this.removeAll();
		this.setSequenceDB(null);
		this.setSelectedSequence(null);
		this.setSeqXclickPoint(-1);
		this.repaint();
	}

}
