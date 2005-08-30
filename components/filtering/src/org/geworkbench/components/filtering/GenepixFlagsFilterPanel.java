package org.geworkbench.components.filtering;


import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia.</p>
 * @author Xiaoqing Zhang
 * @version 1.0
 */

/**
 * The parameters panel for the <code>GenepixFlagsFilter</code>
 * filter. The measures will be removed based on their flags.
 */
public class GenepixFlagsFilterPanel extends AbstractSaveableParameterPanel implements
        Serializable {
    /**
     * Inner class to represent FlagDetail.
     */
    private class FlagDetail {
        String label;
        String number;
        boolean isFiltered;
        String description;
        HashMap knownDescriptions = new HashMap() {};

        FlagDetail(String theLabel, String theNumber) {
            label = theLabel;
            number = theNumber;
            isFiltered = false;
        }

        FlagDetail(String theLabel) {
            label = theLabel;
            isFiltered = false;
            number = "0";
        }

        public String getLabel() {
            return label;
        }

        public String getNumber() {
            return number;
        }

        public void setIsFiltered(boolean filtered) {
            isFiltered = filtered;
        }
    }


    String nonApplicableReminder =
            "No data is loaded.";
    String nonAppReminder =
            "The dataset is not applicable for Genepix Flags filter.";
    String noFlagDetectedReminder =
            "The Genepix file has no flags.";
    int unflaggedProbeNum = 0;
    Map flaggedProbeNum;
    final static Hashtable<String,
            String> flagExplanationTable = new Hashtable<String, String>();
    static {
        flagExplanationTable.put("-100", "Bad");
        flagExplanationTable.put("100", "Good");
        flagExplanationTable.put("-50", "Not Found");
        flagExplanationTable.put("-75", "Absent");
        flagExplanationTable.put("0", "Unflagged");
    }

    final String PRESENT_OPTION = "P";
    final String ABSENT_OPTION = "A";
    final String MARGINAL_OPTION = "M";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel callSelectionLabel = new JLabel(
            "<html><p>Select flags to</p><p>be filtered out.</p></html>");
    private JCheckBox presentButton = new JCheckBox(PRESENT_OPTION);
    private JCheckBox absentButton = new JCheckBox(ABSENT_OPTION);
    private JCheckBox marginalButton = new JCheckBox(MARGINAL_OPTION);
    private JCheckBox[] flagsBox;
    private boolean presentButtonStatus;
    private boolean absentButtonStatus;
    private boolean marginalButtonStatus;
    JScrollPane flagInfoPane = new JScrollPane();
    private ArrayList hits = new ArrayList<FlagDetail>();
    private JPanel flagInfoPanel = new JPanel();
    private JLabel infoLabel;
    JPanel container = new JPanel();

    JTable table1;
    public GenepixFlagsFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        this.setLayout(new FlowLayout());

        gridLayout1.setColumns(1);

        gridLayout1.setRows(2);

        container.setLayout(new BorderLayout());
        infoLabel = new JLabel(nonApplicableReminder);

        // container.add(callSelectionLabel);

        container.add(BorderLayout.NORTH, infoLabel);
        container.setPreferredSize(new Dimension(450, 150));
        this.add(container);
    }

    /**
     * setFlagInfoPanel
     */
    public void setFlagInfoPanel() {
        container.removeAll();
        container.add(BorderLayout.CENTER, new Label(nonApplicableReminder));
    }

    /**
     * setFlagInfoPanel
     *
     * @param aString String
     */
    public void setFlagInfoPanel(String aString) {
        container.removeAll();
        container.add(BorderLayout.NORTH, new Label(nonAppReminder));
        container.add(BorderLayout.CENTER, new Label(aString));
        repaint();
    }


    /**
     * Returns a JScrollpane containing Flag results in table format.
     *
     * @return a JScrollpane containing table of Flags.
     */
    public void setFlagInfoPanel(Set values) {
        hits.clear();

        for (Object ob : values) {
            FlagDetail newFlag = new FlagDetail((String) ob);
            hits.add(newFlag);
        }
        container.removeAll();

        FlagsTableModel myModel = new FlagsTableModel();
        JTable table1 = new JTable(myModel);
        table1.setPreferredScrollableViewportSize(new Dimension(115, 90));
        table1.getColumnModel().getColumn(0).setPreferredWidth(15);
        table1.getColumnModel().getColumn(1).setPreferredWidth(30);
        table1.getColumnModel().getColumn(2).setPreferredWidth(30);
        table1.getColumnModel().getColumn(3).setPreferredWidth(30);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = table1.getSelectionModel();
        rowSM.addListSelectionListener(new listSelectionListener());
        table1.setSelectionModel(rowSM);

        JScrollPane hitsPane = new JScrollPane(table1);
        container.add(BorderLayout.NORTH,
                      new Label("Please select flags to filter:"));
        container.add(BorderLayout.CENTER, hitsPane);
        //setFlagInfoPanel(hitsPane);
        repaint();
        revalidate();

    }

    /**
     * getSelectedFlags
     */
    public ArrayList getSelectedFlags() {
        ArrayList selectedFlags = new ArrayList();
        for (Object fd : hits) {
            if (((FlagDetail) fd).isFiltered) {
                selectedFlags.add(((FlagDetail) fd).getLabel());
            }
        }
        return selectedFlags;
    }


    /**
     * For the FlagsTable.
     */
    private class listSelectionListener implements ListSelectionListener {
        int selectedRow;

        public void valueChanged(ListSelectionEvent e) {
            //Ignore extra messages.
            if (e.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
            } else {
                selectedRow = lsm.getMinSelectionIndex();

            }
        }
    }


    /**
     * This class extends AbstractTableModel and creates a table view of Flags.
     */
    private class FlagsTableModel extends AbstractTableModel {

        /* array of the column names in order from left to right*/
        final String[] columnNames = {"Filter", "Flags Name", "Description",
                                     "# of probes",
        };
        FlagDetail hit;

        /* returns the number of columns in table*/
        public int getColumnCount() {
            return columnNames.length;
        }

        /* returns the number of rows in table*/
        public int getRowCount() {
            return (hits.size());
        }

        /* return the header for the column number */
        public String getColumnName(int col) {
            return columnNames[col];
        }

        /* get the Object data to be displayed at (row, col) in table*/
        public Object getValueAt(int row, int col) {
            /*get specific BlastObj based on row number*/
            hit = (FlagDetail) hits.get(row);
            /*display data depending on which column is chosen*/
            switch (col) {
            case 0:
                return hit.isFiltered;
            case 1:
                return hit.getLabel(); //accesion number
            case 2:
                if (flagExplanationTable.get(hit.getLabel()) != null) {
                    return flagExplanationTable.get(hit.getLabel());
                }
                return "N/A";
            case 3:
                if (flaggedProbeNum.get(hit.getLabel()) != null) {
                    return flaggedProbeNum.get(hit.getLabel());
                }
                return "1"; //description
            }
            return null;
        }

        /*returns the Class type of the column c*/
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*returns if the cell is editable; returns false for all cells in columns except column 6*/
        public boolean isCellEditable(int row, int col) {

            if (col == 0) {
                return true;
            } else {
                return false;
            }
        }

        /*detect change in cell at (row, col); set cell to value; update the table */
        public void setValueAt(Object value, int row, int col) {
            hit = (FlagDetail) hits.get(row);
            hit.isFiltered = (((Boolean) value).booleanValue());
            fireTableCellUpdated(row, col);
        }

    }


    public Component getComponent() {
        return this;
    };
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

    public void setFlagInfoPanel(JScrollPane FlagInfoPanel) {
        this.flagInfoPane = FlagInfoPanel;

        revalidate();
    }

    public JLabel getInfoLabel() {
        return infoLabel;
    }

    public void setInfoLabel(String infoLabelString) {
        this.infoLabel = new JLabel(infoLabelString);
        revalidate();
    }

    /**
     * setflaggedProbeNum
     *
     * @param flagsProbeNum Map
     */
    public void setflaggedProbeNum(Map flagsProbeNum) {
        this.flaggedProbeNum = flagsProbeNum;
    }

    /**
     * setUnflaggedProbeNum
     *
     * @param unflaggedProbeNum int
     */
    public void setUnflaggedProbeNum(int unflaggedProbeNum) {
        this.unflaggedProbeNum = unflaggedProbeNum;
    }


}
