package org.geworkbench.components.pudge;

import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.complex.pattern.SoapParmsDataSet;
import org.geworkbench.bison.datastructure.bioobjects.structure.CSProteinStructure;
import org.geworkbench.bison.datastructure.bioobjects.structure.DSProteinStructure;
import org.geworkbench.events.SequencePanelEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;

import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.parsers.sequences.SequenceFileFormat;

import javax.swing.event.EventListenerList;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.io.File;

/**
 * <p>SequenceViewAppComponent controls all notification and communication for SequenceViewWidget</p>
 * <p>Loads FASTA file </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab </p>
 *
 * @author
 * @version 1.0
 */
@AcceptTypes({CSSequenceSet.class, SoapParmsDataSet.class})
public class
        GenetegrateViewAppComponent implements VisualPlugin,
        org.geworkbench.engine.config.MenuListener, PropertyChangeListener {
    GenetegrateViewWidget sViewWidget;
    EventListenerList listenerList = new EventListenerList();
    JMenuItem jOpenFASTAItem = new JMenuItem();
    JMenuItem jOpenFileItem = new JMenuItem();
    //This registers listeners for menu items.
    HashMap listeners = new HashMap();
    SequencePanelEvent spe = null;
    ActionListener listener = null;
    DSSequenceSet sequenceDB = null;

    public GenetegrateViewAppComponent() {
        sViewWidget = new GenetegrateViewWidget(this);
        sViewWidget.addPropertyChangeListener(this);

        jOpenFASTAItem.setText("FASTA File");
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jOpenFASTAItem_actionPerformed(e);
            }
        };
        listeners.put("File.Open.FASTA File", listener);
        jOpenFASTAItem.addActionListener(listener);
        //sViewWidget.add(jOpenFASTAItem);
        jOpenFileItem.setText("File");

        listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                jOpenFASTAItem_actionPerformed(e);
            }
        };
        listeners.put("File.Open.File", listener);
        jOpenFASTAItem.addActionListener(listener);
    }

    @Subscribe
    public void sequenceDiscoveryTableRowSelected(org.geworkbench.
            events.SequenceDiscoveryTableEvent e, Object publisher) {
        sViewWidget.patternSelectionHasChanged(e);
    }


    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe(Asynchronous.class)
    public void receive(GeneSelectorEvent e,
                        Object source) {
        sViewWidget.sequenceDBUpdate(e);
    }

    public Component getComponent() {
        return sViewWidget;
    }

    public ActionListener getActionListener(String var) {

        return (ActionListener) getListeners().get(var);

    } //implementation of org.geworkbench.engine.config.MenuListener interface

    @Subscribe
    public void receiveProjectSelection(org.geworkbench.events.
            ProjectEvent e, Object source) {
        ProjectSelection selection = ((ProjectPanel) source).getSelection();
         DSDataSet newsequenceDB = selection.getDataSet();
        if (newsequenceDB instanceof DSSequenceSet) {
            sequenceDB = (DSSequenceSet)newsequenceDB;
            sViewWidget.setSequenceDB(sequenceDB);
        }
    }

    public void publishPDBDataSet(File pdbFile) {

        String name = pdbFile.getName();
        int index = name.lastIndexOf('.');
        if (index != -1) {
            name = name.substring(0, index);
        }
        DSProteinStructure dataSet = new CSProteinStructure(sequenceDB, name);
        dataSet.setFile(pdbFile);

        ProjectNodeAddedEvent event =
                new ProjectNodeAddedEvent(null, null,
                        dataSet);
        publishProjectNodeAddedEvent(event);

    }

    /**
     * publishProjectNodeAddedEvent
     *
     * @param event ProjectNodeAddedEvent
     */
    @Publish
    public org.geworkbench.events.ProjectNodeAddedEvent
            publishProjectNodeAddedEvent(org.geworkbench.events.
            ProjectNodeAddedEvent event) {
        return event;
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
    }

    void jOpenFASTAItem_actionPerformed(ActionEvent e) {
        String defPath = PropertiesMonitor.getPropertiesMonitor().getDefPath();
        JFileChooser fc = new JFileChooser(defPath);
        String FASTAFilename = null;
        org.geworkbench.components.parsers.FileFormat format = new
                SequenceFileFormat();
        FileFilter filter = format.getFileFilter();
        fc.setFileFilter(filter);
        fc.setDialogTitle("Open FASTA file");
        int choice = fc.showOpenDialog(sViewWidget.getParent());
        if (choice == JFileChooser.APPROVE_OPTION) {
            PropertiesMonitor.getPropertiesMonitor().setDefPath(fc.
                    getCurrentDirectory().getAbsolutePath());
            FASTAFilename = fc.getSelectedFile().getAbsolutePath();
            sequenceDB = CSSequenceSet.getSequenceDB(fc.getSelectedFile());
            if (sequenceDB != null) {
                sViewWidget.setSequenceDB(sequenceDB);
            }
        }
    }

    public HashMap getListeners() {
        return listeners;
    }
}
