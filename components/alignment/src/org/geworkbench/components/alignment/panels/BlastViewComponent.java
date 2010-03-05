package org.geworkbench.components.alignment.panels;

import java.awt.Component;
import java.awt.Container;
import java.util.Vector;

import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSAlignmentResultSet;
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
 * @version 1.0
 */
//@AcceptTypes( {SequenceDB.class})public class BlastViewComponent implements
@AcceptTypes( {DSAlignmentResultSet.class})public class BlastViewComponent implements
        VisualPlugin {
	private static Log log = LogFactory.getLog(BlastViewComponent.class);

    private Vector hits;
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
     * Get the last sequence's Blast result.
     * @return Vector
     */
    public Vector getResults() {
        return hits;

    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent e,
                                   Object source) {

        Container parent = blastViewPanel.getParent();
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedComponent(blastViewPanel);
        }

        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DSAncillaryDataSet df = selection.getDataSubSet();
        //DSDataSet sequenceDB = selection.getDataSet();
        //Get the sequenceDb from DAncillaryDataset not from project.
        DSDataSet sequenceDB = selection.getDataSet();
        if (df != null && df instanceof CSAlignmentResultSet) {
          //  sequenceDB = df.getParentDataSet();
            sequenceDB = ((CSAlignmentResultSet)df).getBlastedParentDataSet();
        }
        if (sequenceDB instanceof CSSequenceSet && df != null) {
            //update db with the selected file in the project
            if (df instanceof DSAlignmentResultSet) {
                if ( ((CSAlignmentResultSet) df).getLabel().equals(BlastAppComponent.NCBILABEL) ) {
                    NCBIBlastParser nbp = new NCBIBlastParser((CSSequenceSet)
                            sequenceDB, ((DSAlignmentResultSet) df).
                            getResultFilePath());
                    nbp.parseResults();
                    hits = nbp.getHits();

                   //blastViewPanel.setResults(hits);
                    blastViewPanel.setSequenceDB((CSSequenceSet) sequenceDB);
                    blastViewPanel.setBlastDataSet(nbp.getBlastDataset());

                    String summary = nbp.getSummary();
                   blastViewPanel.setSummary(summary);
                   df.addDescription(summary);
                } else {
                	log.error("Unexpected CSAlignmentResultSet label "+((CSAlignmentResultSet) df).getLabel());
                }
            } else {
                blastViewPanel.resetToWhite();
            }

        }

    }

}
