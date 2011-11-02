package org.geworkbench.components.alignment.panels;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.sequence.BlastObj; 
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection; 
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;

/**
 * <p>
 * Title: Bioworks
 * </p>
 * <p>
 * Description: Modular Application Framework for Gene Expession, Sequence and
 * Genotype Analysis
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 -2004
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * 
 * @author XZ
 * @version $Id$
 */
@AcceptTypes( { DSAlignmentResultSet.class })
public class BlastViewComponent implements VisualPlugin {
	private static Log log = LogFactory.getLog(BlastViewComponent.class);

	private BlastViewPanel blastViewPanel;

	@Publish
	public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			org.geworkbench.events.ProjectNodeAddedEvent event) {
		return event;
	}

	public BlastViewComponent() {
		try {
			blastViewPanel = new BlastViewPanel();
			blastViewPanel.setBlastViewComponent(this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * getComponent
	 * 
	 * @return Component
	 */
	public Component getComponent() {
		return blastViewPanel;
	}

	/**
	 * receiveProjectSelection
	 * 
	 * @param e
	 *            ProjectEvent
	 */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

		Container parent = blastViewPanel.getParent();
		if (parent instanceof JTabbedPane) {
			((JTabbedPane) parent).setSelectedComponent(blastViewPanel);
		}

		ProjectSelection selection = ((ProjectPanel) source).getSelection();
		DSAncillaryDataSet<DSBioObject> df = selection.getDataSubSet();
		// Get the sequenceDb from DAncillaryDataset not from project.
		DSDataSet<? extends DSSequence> sequenceDB = selection.getDataSet();
		if (df != null && df instanceof DSAlignmentResultSet) {
			// sequenceDB = df.getParentDataSet();
			sequenceDB = ((DSAlignmentResultSet) df).getBlastedParentDataSet();
		}
		if (sequenceDB instanceof CSSequenceSet && df != null) {
			if (df instanceof DSAlignmentResultSet) {
				log.debug("update blast result view panel");
				DSAlignmentResultSet resultSet = (DSAlignmentResultSet) df;
				ArrayList<Vector<BlastObj>> blastDataSet = resultSet
						.getBlastDataSet();

				blastViewPanel
						.setSequenceDB((CSSequenceSet<DSSequence>) sequenceDB);
				blastViewPanel.setBlastDataSet(blastDataSet);

				String summary = resultSet.getSummary();
				blastViewPanel.setSummary(summary);
				df.setDescription(summary);

				if (resultSet.getHitCount() == 0) {
					blastViewPanel.resetToWhite("No alignment hit is found.");
					blastViewPanel.setSummaryPanelOff();
				} else {
					blastViewPanel.setSummaryPanelOn();
				}
			} else {
				blastViewPanel.resetToWhite();
			}

		}

	}

}
