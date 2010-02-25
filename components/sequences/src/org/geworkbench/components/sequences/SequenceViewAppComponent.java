package org.geworkbench.components.sequences;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.complex.pattern.SoapParmsDataSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.components.parsers.sequences.SequenceFileFormat;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.util.sequences.SequenceViewWidget;

/**
 * <p>SequenceViewAppComponent controls all notification and communication for SequenceViewWidget</p>
 * <p>Loads FASTA file </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab </p>
 *
 * @author
 * @version "Id"
 */
@AcceptTypes( {CSSequenceSet.class, SoapParmsDataSet.class})public class
        SequenceViewAppComponent implements VisualPlugin,
        org.geworkbench.engine.config.MenuListener {
    private SequenceViewWidget sViewWidget;

    private JMenuItem jOpenFASTAItem = new JMenuItem();
    private JMenuItem jOpenFileItem = new JMenuItem();
    //This registers listeners for menu items.
    private Map<String, ActionListener> listeners = new HashMap<String, ActionListener>();

    public SequenceViewAppComponent() {
        sViewWidget = new SequenceViewWidget();

        jOpenFASTAItem.setText("FASTA File");
        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jOpenFASTAItem_actionPerformed(e);
            }
        };
        listeners.put("File.Open.FASTA File", listener);
        jOpenFASTAItem.addActionListener(listener);

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

        return listeners.get(var);

    } //implementation of org.geworkbench.engine.config.MenuListener interface

    @Subscribe 
    public void receiveProjectSelection(org.geworkbench.events.
            ProjectEvent e, Object source) {
        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DSDataSet<?> dataFile = selection.getDataSet();
        if (dataFile instanceof DSSequenceSet) {
            sViewWidget.setSequenceDB((DSSequenceSet<?>) dataFile);
        }
    }

    private void jOpenFASTAItem_actionPerformed(ActionEvent e) {
        String defPath = PropertiesMonitor.getPropertiesMonitor().getDefPath();
        JFileChooser fc = new JFileChooser(defPath);

        org.geworkbench.components.parsers.FileFormat format = new
                SequenceFileFormat();
        FileFilter filter = format.getFileFilter();
        fc.setFileFilter(filter);
        fc.setDialogTitle("Open FASTA file");
        int choice = fc.showOpenDialog(sViewWidget.getParent());
        if (choice == JFileChooser.APPROVE_OPTION) {
            PropertiesMonitor.getPropertiesMonitor().setDefPath(fc.
                    getCurrentDirectory().getAbsolutePath());

            DSSequenceSet<?> sequenceDB = CSSequenceSet.getSequenceDB(fc.getSelectedFile());
            if (sequenceDB != null) {
                sViewWidget.setSequenceDB(sequenceDB);
            }
        }
    }

}
