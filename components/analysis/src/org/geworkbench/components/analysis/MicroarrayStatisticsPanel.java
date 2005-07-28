package org.geworkbench.components.analysis;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.util.annotation.AnnotationParser;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MicroarrayStatisticsPanel extends JPanel implements VisualPlugin {
    DSMicroarraySet mArraySet;
    //    DefaultTableModel tableModel = new DefaultTableModel();
    BorderLayout borderLayout1 = new BorderLayout();
    JPanel jPanel1 = new JPanel();
    JButton jButton1 = new JButton();
    JPanel jPanel2 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTable jTable1 = new JTable();

    public void setMArraySet(DSMicroarraySet mArraySet) {

        this.mArraySet = mArraySet;
    }

    public DSMicroarraySet getMArraySet() {
        return mArraySet;
    }

    public MicroarrayStatisticsPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        jButton1.setText("Calculate Statistics");
        jButton1.addActionListener(new MicroarrayStatisticsPanel_jButton1_actionAdapter(this));
        jPanel2.setLayout(borderLayout2);
        this.add(jPanel1, BorderLayout.SOUTH);
        this.add(jButton1, BorderLayout.NORTH);
        this.add(jPanel2, BorderLayout.CENTER);
        jPanel2.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(jTable1, null);
    }


    void jButton1_actionPerformed(ActionEvent e) {
        //      CDC25
        String[] goTerms = AnnotationParser.getInfo("YLR310C", AnnotationParser.GOTERM);
        //        String[] goTerms = AnnotationParser.getInfo("10025_at", AnnotationParser.GOTERM);
        //      String[] goTerms = AnnotationParser.getInfo("1000_at", AnnotationParser.GOTERM);
        //      String[] goTerms = AnnotationParser.getInfo("10001_at", AnnotationParser.GOTERM);

        System.out.println("Printing terms");
        if (goTerms != null) {
            for (int i = 0; i < goTerms.length; i++) {
                System.out.println(goTerms[i]);
            }
        }

        //      Vector headerNames = new Vector();
        //      headerNames.add("Gene Accession");
        //      headerNames.add("Gene Name");
        //      headerNames.add("Mean");
        //      headerNames.add("SD");
        //
        //      Vector tableData = new Vector();
        //
        //      jTable1.setModel(new DefaultTableModel(tableData, headerNames));
    }

    /**
     * getComponent
     *
     * @return Component
     */
    public Component getComponent() {
        return this;
    }

    /**
     * receiveProjectSelection
     *
     * @param projectEvent ProjectEvent
     */
    @Subscribe public void receive(org.geworkbench.events.ProjectEvent projectEvent, Object source) {
        if (projectEvent.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
            setMArraySet(null);
        }
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet dataFile = selection.getDataSet();
            if (dataFile != null && dataFile instanceof DSMicroarraySet) {
                DSMicroarraySet set = (DSMicroarraySet) dataFile;
                setMArraySet(set);
            } else {
                setMArraySet(null);
            }

    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe public void receive(GeneSelectorEvent e) {
        System.out.println("Gene selector action");
    }

}

class MicroarrayStatisticsPanel_jButton1_actionAdapter implements java.awt.event.ActionListener {
    MicroarrayStatisticsPanel adaptee;

    MicroarrayStatisticsPanel_jButton1_actionAdapter(MicroarrayStatisticsPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton1_actionPerformed(e);
    }
}
