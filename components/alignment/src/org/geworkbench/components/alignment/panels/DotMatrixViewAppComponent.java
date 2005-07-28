package org.geworkbench.components.alignment.panels;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.engine.parsers.FileFormat;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.events.SequencePanelEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.util.sequences.SequenceViewWidgetByXQ;
import org.geworkbench.engine.parsers.sequences.SequenceFileFormat;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * <p>SequenceViewAppComponent controls all notification and communication for SequenceViewWidget</p>
 * <p>Loads FASTA file </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab </p>
 *
 * @author
 * @version 1.0
 */
public class DotMatrixViewAppComponent implements VisualPlugin, org.geworkbench.engine.config.MenuListener, PropertyChangeListener {
    SequenceViewWidgetByXQ sViewWidget;
    EventListenerList listenerList = new EventListenerList();
    JMenuItem jOpenFASTAItem = new JMenuItem();
    JMenuItem jOpenFileItem = new JMenuItem();
    //This registers listeners for menu items.
    HashMap listeners = new HashMap();
    SequencePanelEvent spe = null;
    ActionListener listener = null;
    SequenceDB sequenceDB = null;

    public void DotMatrixAppComponent() {

        sViewWidget = new org.geworkbench.util.sequences.SequenceViewWidgetByXQ();
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

    @Subscribe public void receive(org.geworkbench.events.SequenceDiscoveryTableEvent e, Object source) {
        sViewWidget.patternSelectionHasChanged(e);
    }

    public Component getComponent() {
        return sViewWidget;
    }

    public ActionListener getActionListener(String var) {

        return (ActionListener) getListeners().get(var);

    } //implementation of org.geworkbench.engine.config.MenuListener interface

    @Subscribe public void receive(ProjectEvent e, Object source) {
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet dataFile = selection.getDataSet();
            if (dataFile instanceof SequenceDB) {
                sViewWidget.setSequenceDB((SequenceDB) dataFile);
                System.out.println(dataFile.getFile() + " at pro");
            }
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
    }

    void jOpenFASTAItem_actionPerformed(ActionEvent e) {
        String defPath = org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor().getDefPath();
        JFileChooser fc = new JFileChooser(defPath);
        String FASTAFilename = null;
        org.geworkbench.engine.parsers.FileFormat format = new SequenceFileFormat();
        FileFilter filter = format.getFileFilter();
        fc.setFileFilter(filter);
        fc.setDialogTitle("Open FASTA file");
        int choice = fc.showOpenDialog(sViewWidget.getParent());
        if (choice == JFileChooser.APPROVE_OPTION) {
            org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor().setDefPath(fc.getCurrentDirectory().getAbsolutePath());
            FASTAFilename = fc.getSelectedFile().getAbsolutePath();
            sequenceDB = SequenceDB.getSequenceDB(fc.getSelectedFile());
            if (sequenceDB != null) {
                sViewWidget.setSequenceDB(sequenceDB);
            }
        }
    }

    public HashMap getListeners() {
        return listeners;
    }
}
