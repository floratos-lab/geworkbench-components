package org.geworkbench.components.alignment.panels;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.StatusChangeListener;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.*;


public class SyntenyAppComponent implements VisualPlugin, StatusChangeListener {

    private SyntenyViewWidget tWidget = null;

    public SyntenyAppComponent() {
        try {
            tWidget = new SyntenyViewWidget();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // the method for VisualPlugin interface
    public Component getComponent() {
        return tWidget;
    }

    public void progressBarChanged(ProgressBarEvent evt) {
    };
    public void statusBarChanged(StatusBarEvent evt) {
    };

    @Subscribe public void receiveProjectSelection(org.geworkbench.events.ProjectEvent e, Object source) {
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet df = selection.getDataSet();
            if (df != null) {
                if (df instanceof SequenceDB) {
                    /*
                            System.out.println("fasta file id..." + df.getID());
                           tWidget.setFastaFile((SequenceDB) df);
                    */
                }
            }
    }

}
