package org.geworkbench.components.promoter;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.*;

@AcceptTypes({CSSequenceSet.class}) public class PromoterView implements VisualPlugin {

    PromoterViewPanel main = new PromoterViewPanel();

    public PromoterView() {

        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void jbInit() {
        main.setSequenceDB(new CSSequenceSet());
    }

    /**
     * getComponent
     *
     * @return Component
     */
    public Component getComponent() {
        return main;
    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe public void receive(org.geworkbench.events.GeneSelectorEvent e, Object publisher) {
        main.markers = e.getPanel();
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe public void receive(ProjectEvent e, Object source) {
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet dataFile = selection.getDataSet();
            if (dataFile instanceof DSSequenceSet) {
                main.setSequenceDB((DSSequenceSet) dataFile);
            }
    }

    /**
     * sequenceDiscoveryTableRowSelected
     * receives signal from sequence discovery panel
     *
     * @param e SequenceDiscoveryTableEvent
     */
    @Subscribe public void receive(org.geworkbench.events.SequenceDiscoveryTableEvent e, Object publisher) {
        main.sequenceDiscoveryTableRowSelected(e);
    }
}
