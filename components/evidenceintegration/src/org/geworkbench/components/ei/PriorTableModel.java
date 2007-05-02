package org.geworkbench.components.ei;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * @author mhall
 */
public class PriorTableModel extends DefaultTableModel {

    static Log log = LogFactory.getLog(PriorTableModel.class);

    List<Evidence> evidenceSet;
    String[] headers = new String[]{"", "Name"};

    public PriorTableModel(List<Evidence> evidenceSet) {
        this.evidenceSet = evidenceSet;
    }

    public String getColumnName(int i) {
        return headers[i];
    }

    public int getColumnCount() {
        return headers.length;
    }

    public int getRowCount() {
        if (evidenceSet != null) {
            return evidenceSet.size();
        } else {
            return 0;
        }
    }

    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        } else if (columnIndex == 1) {
            return String.class;
        } else {
            return Integer.class;
        }
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            return false;
        } else {
            return true;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return evidenceSet.get(rowIndex).isEnabled();
        } else if (columnIndex == 1) {
            return evidenceSet.get(rowIndex).getName();
        } else if (columnIndex == 2) {
            return evidenceSet.get(rowIndex).getBins();
        } else {
            PriorTableModel.log.error("Requested value for column " + columnIndex + " which doesn't exist.");
            return "";
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            evidenceSet.get(rowIndex).setEnabled((Boolean) aValue);
        } else if (columnIndex == 2) {
            evidenceSet.get(rowIndex).setBins((Integer) aValue);
        }
    }

}
