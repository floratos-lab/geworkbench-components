package org.geworkbench.components.poshistogram;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.complex.pattern.SoapParmsDataSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.events.ImageSnapshotEvent;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
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

@AcceptTypes({SoapParmsDataSet.class})
public class PositionHistogramAppComponent implements VisualPlugin, MenuListener {

    PositionHistogramWidget pHistogramWidget = null;
    HashMap listeners = new HashMap();

    public PositionHistogramAppComponent() {
        pHistogramWidget = new PositionHistogramWidget(this);
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pHistogramWidget.imageSnapshotAction(e);
            }
        };

        listeners.put("File.Image snapshot", listener);

    }

    @Subscribe
    public void sequenceDiscoveryTableRowSelected(SequenceDiscoveryTableEvent e, Object publisher) {
        /** todo Fix patterns */
        pHistogramWidget.setPatterns(e.getPatternMatchCollection());
    }

    public Component getComponent() {
        return pHistogramWidget;
    }

    public ActionListener getActionListener(String var) {
        return (ActionListener) getListeners().get(var);
    }

    @Subscribe
    public void receiveProjectSelection(ProjectEvent e, Object source) {
        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DSDataSet dataFile = selection.getDataSet();
        if (dataFile instanceof DSSequenceSet) {
            pHistogramWidget.setSequenceDB((DSSequenceSet) dataFile);
        }
    }

    @Publish
    public org.geworkbench.events.ImageSnapshotEvent publishImageSnapshotEvent(ImageSnapshotEvent event) {
        return event;
    }

    public HashMap getListeners() {
        return listeners;
    }
}
