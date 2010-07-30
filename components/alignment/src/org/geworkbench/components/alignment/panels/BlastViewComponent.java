package org.geworkbench.components.alignment.panels;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.components.alignment.blast.NCBIBlastParser;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author XZ
 * @version $Id$
 */
@AcceptTypes( {DSAlignmentResultSet.class})
public class BlastViewComponent implements
        VisualPlugin {
	private static Log log = LogFactory.getLog(BlastViewComponent.class);

    private BlastViewPanel blastViewPanel;

    @Publish public org.geworkbench.events.ProjectNodeAddedEvent
            publishProjectNodeAddedEvent(org.geworkbench.events.
                                         ProjectNodeAddedEvent event) {
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
     * @param e ProjectEvent
     */
    @SuppressWarnings("unchecked")
	@Subscribe public void receive(org.geworkbench.events.ProjectEvent e,
                                   Object source) {

        Container parent = blastViewPanel.getParent();
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedComponent(blastViewPanel);
        }

        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DSAncillaryDataSet<DSBioObject> df = selection.getDataSubSet();
        //Get the sequenceDb from DAncillaryDataset not from project.
        DSDataSet<? extends DSSequence> sequenceDB = selection.getDataSet();
        if (df != null && df instanceof CSAlignmentResultSet) {
          //  sequenceDB = df.getParentDataSet();
            sequenceDB = ((CSAlignmentResultSet)df).getBlastedParentDataSet();
        }
        if (sequenceDB instanceof CSSequenceSet && df != null) {
            if (df instanceof DSAlignmentResultSet) {
            	log.debug("update blast result view panel");
               	int totalSequenceNumber = ((CSSequenceSet<? extends DSSequence>) sequenceDB)
						.size();
				NCBIBlastParser nbp = new NCBIBlastParser(totalSequenceNumber,
						((DSAlignmentResultSet) df).getResultFilePath());

				blastViewPanel
						.setSequenceDB((CSSequenceSet<DSSequence>) sequenceDB);
				blastViewPanel.setBlastDataSet(nbp.parseResults());

				String summary = nbp.getSummary();
				blastViewPanel.setSummary(summary);
				df.addDescription(summary);
            } else {
                blastViewPanel.resetToWhite();
            }

        }

    }

}
