package org.geworkbench.components.alignment.panels;

import org.geworkbench.components.alignment.blast.BlastParser;
import org.geworkbench.components.alignment.blast.TextResultParser;
import org.geworkbench.bison.datastructure.bioobjects.sequence.
        DSAlignmentResultSet;
import org.geworkbench.components.alignment.client.HMMDataSet;
import org.geworkbench.components.alignment.client.SWDataSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;
import java.util.ArrayList;
import org.geworkbench.bison.datastructure.bioobjects.sequence.
        CSAlignmentResultSet;
import org.geworkbench.components.alignment.blast.NCBIBlastParser;

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

    private BlastParser bp;
    private Vector hits;
    private BlastViewPanel blastViewPanel;
    private String DEFAULT_FILENAME = "output.txt";

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
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
      public void projectNodeAdded(ProjectNodeAddedEvent pnae) {

        String resultFileName = DEFAULT_FILENAME;

        AncillaryDataSet ads = pnae.ancDataSet;
        if (ads != null && ads instanceof CSAlignmentResultSet) {

          resultFileName = ( (CSAlignmentResultSet) ads).getResultFilePath();
         bp = new BlastParser(resultFileName);
         if (ads instanceof SWDataSet)
         bp = new TextResultParser(resultFileName);
          boolean isResultFine = bp.parseResults();
          if(isResultFine){
          hits = bp.getHits();
          blastViewPanel.setResults(hits);
          }else{
            JOptionPane.showMessageDialog(null,
     "Mismatch, please check your database and program setting.",
                                          "Mismatch Error.",
                                          JOptionPane.ERROR_MESSAGE);


          }
          // throw new java.lang.UnsupportedOperationException("Method projectNodeAdded() not yet implemented.");
        }
      }
     */

    /**
     * getComponent
     *
     * @return Component
     */
    public Component getComponent() {
        return blastViewPanel;
    }

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
        if (df != null) {
            sequenceDB = df.getParentDataSet();
        }
        if (sequenceDB instanceof CSSequenceSet && df != null) {
            //update db with the selected file in the project
            if (df instanceof SWDataSet) {
                bp = new TextResultParser(((SWDataSet) df).getResultFilePath());
                bp.parseResults();
                hits = bp.getHits();

                blastViewPanel.setResults(hits);

            } else if (df instanceof DSAlignmentResultSet) {
                if (!((CSAlignmentResultSet) df).getLabel().equals("NCBIBLAST")) {
                    bp = new BlastParser(((DSAlignmentResultSet) df).
                                         getResultFilePath(), sequenceDB);

                    bp.parseResults();
                    hits = bp.getHits();
                    blastViewPanel.setResults(hits);
                    blastViewPanel.setSequenceDB((CSSequenceSet) sequenceDB);
                    blastViewPanel.setBlastDataSet(bp.getBlastDataset());
                    String summary = bp.getSummary();
                    blastViewPanel.displayResults(summary);
                } else {
                    NCBIBlastParser nbp = new NCBIBlastParser((CSSequenceSet)
                            sequenceDB, ((DSAlignmentResultSet) df).
                            getResultFilePath());
                    nbp.parseResults();
                    hits = nbp.getHits();
                    blastViewPanel.setResults(hits);
                    blastViewPanel.setSequenceDB((CSSequenceSet) sequenceDB);
                    blastViewPanel.setBlastDataSet(null);
                    blastViewPanel.displayResults("NCBI Blast Result");
                }
            } else if (df instanceof HMMDataSet) {

                blastViewPanel.setResults(((HMMDataSet) df).getResultFilePath());

            } else {
                blastViewPanel.resetToWhite();
            }

        }

    }

    private void jbInit() throws Exception {
    }

}
