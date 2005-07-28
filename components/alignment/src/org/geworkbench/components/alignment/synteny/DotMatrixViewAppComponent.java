package org.geworkbench.components.alignment.synteny;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

/**
 * <p>DotMatrixViewAppComponent controls all notification and communication for DotMatrixViewWidget</p>
 * <p>Loads FASTA file </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab </p>
 *
 * @author
 * @version 1.0
 */

public class DotMatrixViewAppComponent implements VisualPlugin, org.geworkbench.engine.config.MenuListener, PropertyChangeListener {

    EventListenerList listenerList = new EventListenerList();
    DotMatrixViewWidget dmViewWidget;
    //  This registers listeners for menu items.
    HashMap listeners = new HashMap();
    //  SequencePanelEvent spe = null;
    ActionListener listener = null;
    //  SequenceDB sequenceDB = null;

    public DotMatrixViewAppComponent() {

        dmViewWidget = new DotMatrixViewWidget();
        dmViewWidget.addPropertyChangeListener(this);

        //  New dotmatrix arrives;
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jNewDotMatrix_actionPerformed(e);
            }
        };

        listeners.put("Processing new dotmatrix", listener);

        //    jOpenFASTAItem.addActionListener(listener);
        //    sViewWidget.add(jOpenFASTAItem);
        //    jOpenFileItem.setText("File");

        listeners.put("File.Open.File", listener);
        //    jOpenFASTAItem.addActionListener(listener);
    }

    @Subscribe public void receive(SequenceDiscoveryTableEvent e, Object source) {
        //    sViewWidget.patternSelectionHasChanged(e);
    }

    public Component getComponent() {
        return dmViewWidget;
    }

    public ActionListener getActionListener(String var) {

        return (ActionListener) getListeners().get(var);

    } //implementation of org.geworkbench.engine.config.MenuListener interface

    @Subscribe public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet dataFile = selection.getDataSet();
            if (dataFile instanceof SequenceDB) {
                //        sViewWidget.setSequenceDB( (SequenceDB) dataFile);
            }
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
    }

    void jNewDotMatrix_actionPerformed(ActionEvent e) {
        dmViewWidget = new DotMatrixViewWidget();

        /*
            String defPath = PropertiesMonitor.getPropertiesMonitor().getDefPath();
            JFileChooser fc = new JFileChooser(defPath);
            String FASTAFilename = null;
            FileFormat format = new SequenceFileFormat();
            FileFilter filter = format.getFileFilter();
            fc.setFileFilter(filter);
            fc.setDialogTitle("Open FASTA file");
            int choice = fc.showOpenDialog(sViewWidget.getParent());
            if (choice == JFileChooser.APPROVE_OPTION) {
              PropertiesMonitor.getPropertiesMonitor().setDefPath(
                  fc.getCurrentDirectory().getAbsolutePath());
        //      FASTAFilename = fc.getSelectedFile().getAbsolutePath();
        //      sequenceDB = SequenceDB.getSequenceDB(fc.getSelectedFile());
        //      if (sequenceDB != null) {
        //        sViewWidget.setSequenceDB(sequenceDB);
        //      }
            }
              }
        */
    }

    public HashMap getListeners() {
        return listeners;
    }
}
