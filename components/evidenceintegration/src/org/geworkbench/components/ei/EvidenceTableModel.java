package org.geworkbench.components.ei;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.table.DefaultTableModel;
import java.util.List;

import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * @author mhall
 */
public class EvidenceTableModel extends DefaultTableModel {

	private static final long serialVersionUID = -4326917002220116210L;

	static Log log = LogFactory.getLog(EvidenceTableModel.class);

    List<Evidence> evidenceSet;
    String[] headers = new String[]{"", "Name", "Bins"};

    public EvidenceTableModel(List<Evidence> evidenceSet) {
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
            log.error("Requested value for column " + columnIndex + " which doesn't exist.");
            return "";
        }
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            evidenceSet.get(rowIndex).setEnabled((Boolean) aValue);
        } else if (columnIndex == 2) {
            if (aValue instanceof String) {
                int bins = Integer.parseInt((String) aValue);
                evidenceSet.get(rowIndex).setBins(bins);
            } else {
                evidenceSet.get(rowIndex).setBins((Integer) aValue);
            }
        }
    }

}
