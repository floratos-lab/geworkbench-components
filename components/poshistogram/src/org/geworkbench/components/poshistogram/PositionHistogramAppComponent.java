package org.geworkbench.components.poshistogram;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author Saroja Hanasoge
 * @version 1.0
 */

@AcceptTypes({CSSequenceSet.class}) public class PositionHistogramAppComponent implements VisualPlugin {

    PositionHistogramWidget pHistogramWidget = null;
    HashMap listeners = new HashMap();

    public PositionHistogramAppComponent() {
        pHistogramWidget = new PositionHistogramWidget();
    }

    @Subscribe public void sequenceDiscoveryTableRowSelected(SequenceDiscoveryTableEvent e, Object publisher) {
        /** @todo Fix patterns */
          pHistogramWidget.setPatterns(e.getPatternMatchCollection());
    }

    public Component getComponent() {

        return pHistogramWidget;
    }

    public ActionListener getActionListener(String var) {

        return (ActionListener) getListeners().get(var);

    }

    @Subscribe public void receiveProjectSelection(ProjectEvent e, Object source) {
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet dataFile = selection.getDataSet();
            if (dataFile instanceof DSSequenceSet) {
                pHistogramWidget.setSequenceDB((DSSequenceSet) dataFile);
            }
    }

    public HashMap getListeners() {
        return listeners;
    }
}
