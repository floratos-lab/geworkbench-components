package org.geworkbench.components.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;

/**
 * A panel that allows T-Profiler results to be viewed in a table.
 */
public class JProfileTable extends JPanel {

    private class TableModel extends AbstractTableModel {

        public int getRowCount() {
            if (results == null) {
                return 0;
            }
            return results.getNumberOfGroups();
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (results != null) {
                ProfileGroup group = results.getGroup(rowIndex);
                if (columnIndex == 0) {
                    return group.getGroupName();
                } else if (columnIndex == 1) {
                    return group.getTValue();
                }
            }
            return null;
        }
    }

    private TProfilerResults results;

    private JTable table;
    private TableModel model;

    public JProfileTable() {
        super(new BorderLayout());
        model = new TableModel();
        table = new JTable(model);
        add(table, BorderLayout.CENTER);
    }

    public void setResults(TProfilerResults results) {
        this.results = results;
        model.fireTableStructureChanged();
    }

    public void sortByTValue() {
        results.sort(TProfilerResults.T_VALUE_COMPARATOR);
    }

    public void sortByName() {
        results.sort(TProfilerResults.NAME_COMPARATOR);
    }

    // Testing
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        TProfilerResults results = new TProfilerResults();
//        ProfileGroup test1 = new ProfileGroup("Group 1", );
    }
}
