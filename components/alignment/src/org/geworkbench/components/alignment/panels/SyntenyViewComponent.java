package org.geworkbench.components.alignment.panels;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.components.alignment.synteny.DotsParser;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.*;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Application Framework for SyntenyVisualizationPanel</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author XZ
 * @version 1.0
 */

public class SyntenyViewComponent implements VisualPlugin {

    private DotsParser dp;
    private SyntenyViewPanel syntenyViewPanel;
    private String DEFAULT_FILENAME = "output.txt";

    public SyntenyViewComponent() {
        try {
            syntenyViewPanel = new SyntenyViewPanel();
            syntenyViewPanel.setSyntenyViewComponent(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Subscribe public void receive(ProjectNodeAddedEvent pnae, Object publisher) {
        /**@todo Implement this medusa.components.listeners.ProjectNodeAddedListener method*/
        String resultFileName = DEFAULT_FILENAME;

        DSAncillaryDataSet ads = pnae.getAncillaryDataSet();
        resultFileName = ads.getDataSetFile().getAbsolutePath();

        //fileName = "c:/data/output.txt";

        dp = new DotsParser(resultFileName);
        System.out.println("project node added on visual area.");
        // throw new java.lang.UnsupportedOperationException("Method projectNodeAdded() not yet implemented.");
    }

    /**
     * getComponent
     *
     * @return Component
     */
    public Component getComponent() {
        return syntenyViewPanel;
    }

    /**
     * receiveProjectSelection
     *
     * @param event ProjectEvent
     */
    @Subscribe public void receive(ProjectEvent event, Object source) {

        String fileName = "c:/data/output.txt";
        dp = new DotsParser(fileName);

        System.out.println("Dotmatrix results parsed\n");
        System.out.println("Event at visual area\n ");
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet df = selection.getDataSet();


            if (df != null) {
                //update db with the selected file in the project
                if (df instanceof SequenceDB) {

                    syntenyViewPanel.resetToWhite();
                    // currentSessionID = df.getID() + df.getDataSetName();
                    //  tWidget.setFastaFile((SequenceDB) df);
                }
            }


    }
}
