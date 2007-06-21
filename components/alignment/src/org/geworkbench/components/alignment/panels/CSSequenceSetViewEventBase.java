package org.geworkbench.components.alignment.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.engine.management.*;
import org.geworkbench.events.*;
import org.geworkbench.engine.config.VisualPlugin;

import java.awt.Dimension;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CSSequenceSetViewEventBase implements VisualPlugin {

    // There should be some new class similar to MicroarraySetView

    protected DSSequenceSet sequenceDB = null;
    protected CSSequenceSet activeSequenceDB = null;
    /**
     * The reference microarray set.
     */
    protected DSMicroarraySet<DSMicroarray> refMASet = null;
    protected DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView = null;


    protected boolean activateMarkers = true;
    // protected boolean activateArrays = false;
    protected JCheckBox chkAllMarkers = new JCheckBox("All Markers");
    protected JPanel mainPanel;
    protected JToolBar displayToolBar;
    protected DSPanel<? extends DSGeneMarker> activatedMarkers = null;
    // protected DSPanel activatedArrays = null;
    JTextField sequenceNumberField;

    public CSSequenceSetViewEventBase() {
        try {
            jbInit();
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
        return mainPanel;
    }

    @Publish
    public SubpanelChangedEvent publishSubpanelChangedEvent(org.
            geworkbench.events.SubpanelChangedEvent event) {
        return event;
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe
    public void receive(org.geworkbench.events.ProjectEvent e,
                        Object source) {
        if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
            refMASet = null;
            fireModelChangedEvent(null);
        } else {
            DSDataSet dataSet = e.getDataSet();
            if (dataSet instanceof DSSequenceSet) {
                if (sequenceDB != dataSet) {
                    this.sequenceDB = (DSSequenceSet) dataSet;

                    activatedMarkers = null;
                }
            }
            //refreshMaSetView();
        }
        refreshMaSetView();
    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe(Asynchronous.class)
    public void receive(GeneSelectorEvent e,
                        Object source) {
        if (e.getPanel() != null && e.getPanel().size() > 0) {
            activatedMarkers = e.getPanel().activeSubset();
        } else {
            activatedMarkers = null;
        }
        refreshMaSetView();
    }



    
    protected void refreshMaSetView() {
        getDataSetView();
        fireModelChangedEvent(null);
    }

    protected void fireModelChangedEvent(MicroarraySetViewEvent event) {

    }

    protected void jbInit() throws Exception {
        mainPanel = new JPanel();

        displayToolBar = new JToolBar();
        displayToolBar.setLayout(new BoxLayout(displayToolBar, BoxLayout.X_AXIS));
        chkAllMarkers.setToolTipText("USe All Markers.");
        chkAllMarkers.setSelected(false);
        chkAllMarkers.addActionListener(new
                CSSequenceSetViewEventBase_chkActivateMarkers_actionAdapter(this));

        BorderLayout borderLayout2 = new BorderLayout();
        mainPanel.setLayout(borderLayout2);
        sequenceNumberField = new JTextField(20) {
            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
            public void setEditable(boolean isEditable){
                super.setEditable(false);
            }

        };

        sequenceNumberField.setText("Total Sequence Number:");
        sequenceNumberField.setEditable(false);
        displayToolBar.add(chkAllMarkers, null);
        displayToolBar.add(Box.createHorizontalStrut(5), null);
        displayToolBar.add(sequenceNumberField);
        sequenceNumberField.setEditable(false);
        mainPanel.add(displayToolBar, java.awt.BorderLayout.SOUTH);
        this.activateMarkers = !chkAllMarkers.isSelected();

    }

//    void chkShowArrays_actionPerformed(ActionEvent e) {
//        activateArrays = ((JCheckBox) e.getSource()).isSelected();
//        refreshMaSetView();
//    }

    void chkActivateMarkers_actionPerformed(ActionEvent e) {
        activateMarkers = !((JCheckBox) e.getSource()).isSelected();
        refreshMaSetView();
    }

    public void getDataSetView() {
        activateMarkers = !chkAllMarkers.isSelected();
        if (activateMarkers) {
            if (activatedMarkers != null &&
                    activatedMarkers.size() > 0) {


                if (activateMarkers && (sequenceDB != null)) {
                    // createActivatedSequenceSet();
                    activeSequenceDB = (CSSequenceSet) ((CSSequenceSet)
                            sequenceDB).
                            getActiveSequenceSet(activatedMarkers);
                    sequenceNumberField.setText("Activated Sequence Number: " +
                            activeSequenceDB.size());
                         sequenceNumberField.setEditable(false);
                }

            } else if (sequenceDB != null) {
                sequenceNumberField.setText("Total Sequence Number: " +
                        sequenceDB.size());


                activeSequenceDB = (CSSequenceSet) sequenceDB;
            }

        } else if (sequenceDB != null) {
            sequenceNumberField.setText("Total Sequence Number: " +
                    sequenceDB.size());
              sequenceNumberField.setEditable(false);
        }


    }

    /**
     * createActivatedMarkers
     */
    public void createActivatedSequenceSet() {

    }

}


class CSSequenceSetViewEventBase_chkActivateMarkers_actionAdapter implements
        ActionListener {
    private CSSequenceSetViewEventBase adaptee;

    CSSequenceSetViewEventBase_chkActivateMarkers_actionAdapter(
            CSSequenceSetViewEventBase adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.chkActivateMarkers_actionPerformed(e);
    }
}
