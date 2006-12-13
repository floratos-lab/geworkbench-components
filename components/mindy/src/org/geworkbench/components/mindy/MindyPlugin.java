package org.geworkbench.components.mindy;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.List;
import java.awt.*;

import org.jdesktop.swingx.JXTable;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.List;
import java.awt.*;

/**
 * @author mhall
 */
public class MindyPlugin extends JPanel {

    public MindyPlugin(MindyData mindyData) {
        String[] modulatorNames = {"PPAP2B", "HCK"};
        String[] descriptions = {"Phosphatidic acid", "Hemopoietic cell kinase"};
        int[] miCounts = {205, 120};
        int[] miOver = {205, 0};
        int[] miUnder = {0, 120};

        List<DSGeneMarker> modulators = mindyData.getModulators();
        List<DSGeneMarker> transFactors = mindyData.getTranscriptionFactors();
        Model model = new Model(modulators, miCounts, miOver, miUnder);

        JTabbedPane tabs = new JTabbedPane();

        {
            // Modulator Table

            JXTable jxTable = new JXTable(model);
            JScrollPane scrollPane = new JScrollPane(jxTable);
            jxTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
//        jxTable.setColumnControlVisible(true);
            jxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jxTable.packAll();
            tabs.add("Modulator", scrollPane);
        }

        {
            // Heat Maps
            JPanel panel = new JPanel(new BorderLayout());
            ModulatorHeatMap heatmap = new ModulatorHeatMap(modulators.iterator().next(), transFactors.iterator().next(), mindyData);
            JScrollPane scrollPane = new JScrollPane(heatmap);
            tabs.add("Heat Map", scrollPane);
        }

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    static private class Model extends DefaultTableModel {

        private boolean[] enabled;
        private List<DSGeneMarker> modulators;
        private int[] mCount;
        private int[] mOver;
        private int[] mUnder;
        private String[] columnNames = new String[]{"", "Modulator", "M#", "M+", "M-", "Mode", "Modulator Description"};

        public Model(List<DSGeneMarker> modulator, int[] mCount, int[] mOver, int[] mUnder) {
            this.enabled = new boolean[modulator.size()];
            this.modulators = modulator;
            this.mCount = mCount;
            this.mOver = mOver;
            this.mUnder = mUnder;
        }

        public int getColumnCount() {
            return 7;
        }

        public int getRowCount() {
            if (enabled == null) {
                return 0;
            } else {
                return modulators.size();
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return true;
            } else {
                return false;
            }
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else if (columnIndex == 1) {
                return String.class;
            } else if (columnIndex == getColumnCount() - 1 || columnIndex == getColumnCount() - 2) {
                return String.class;
            } else {
                return Integer.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return enabled[rowIndex];
            } else if (columnIndex == 1) {
                return modulators.get(rowIndex).getGeneName();
            } else if (columnIndex == 2) {
                return mCount[rowIndex];
            } else if (columnIndex == 3) {
                return mOver[rowIndex];
            } else if (columnIndex == 4) {
                return mUnder[rowIndex];
            } else if (columnIndex == 5) {
                if (mOver[rowIndex] > mUnder[rowIndex]) {
                    return "+";
                } else if (mOver[rowIndex] < mUnder[rowIndex]) {
                    return "-";
                } else {
                    return "=";
                }
            } else {
                return modulators.get(rowIndex).getDescription();
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                enabled[rowIndex] = (Boolean) aValue;
            }
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }
    }


    static class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckboxRenderer() {
            super();
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            if (((JCheckBox) value).isSelected()) {
                setSelected(true);
            } else {
                setSelected(false);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            if (table.getSelectedRow() == row) {
                setBackground(Color.blue);
            } else {
                setBackground(Color.white);
            }
            return this;
        }
    }
}
