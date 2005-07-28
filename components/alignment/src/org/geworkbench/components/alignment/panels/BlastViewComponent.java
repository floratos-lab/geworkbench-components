package org.geworkbench.components.alignment.panels;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.components.alignment.blast.BlastParser;
import org.geworkbench.components.alignment.blast.TextResultParser;
import org.geworkbench.components.alignment.client.BlastDataSet;
import org.geworkbench.components.alignment.client.HMMDataSet;
import org.geworkbench.components.alignment.client.SWDataSet;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author XZ
 * @version 1.0
 */

public class BlastViewComponent implements VisualPlugin {

    private BlastParser bp;
    private Vector hits;
    private BlastViewPanel blastViewPanel;
    private String DEFAULT_FILENAME = "output.txt";

    @Publish public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(org.geworkbench.events.ProjectNodeAddedEvent event) {
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
        if (ads != null && ads instanceof BlastDataSet) {

          resultFileName = ( (BlastDataSet) ads).getResultFilePath();
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
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

        //String fileName = "c:/data/output.txt";

        Container parent = blastViewPanel.getParent();
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedComponent(blastViewPanel);
        }

            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSAncillaryDataSet df = selection.getDataSubSet();
            DSDataSet sequenceDB = selection.getDataSet();

            if (sequenceDB instanceof SequenceDB) {

            }
            if (sequenceDB instanceof SequenceDB && df != null) {
                //update db with the selected file in the project
                if (df instanceof SWDataSet) {
                    bp = new TextResultParser(((SWDataSet) df).getResultFilePath());
                    bp.parseResults();
                    hits = bp.getHits();

                    blastViewPanel.setResults(hits);

                } else if (df instanceof BlastDataSet) {
                    //bp = new BlastParser( ( (BlastDataSet) df).getResultFilePath());
                    bp = new BlastParser(((BlastDataSet) df).getResultFilePath());
                    bp.parseResults();
                    hits = bp.getHits();
                    blastViewPanel.setResults(hits);
                    String summary = bp.getSummary();
                    blastViewPanel.displayResults(summary);

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
