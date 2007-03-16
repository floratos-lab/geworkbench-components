package org.geworkbench.components.cascript;

import java.util.Collections;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * <p>Title: CasJTableModel</p>
 *
 * <p>Description: Creates a table model that is uneditable and sortable by column</p>
 *
 * @author Behrooz Badii
 */
public class CasJTableModel extends DefaultTableModel {
    public CasJTableModel() {
        super();
    }
    public CasJTableModel(int rowCount, int columnCount) {
        super();
    }
                         
    public CasJTableModel(Vector columnNames, int rowCount) {
        super();
    }
                         
    public CasJTableModel(Object[] columnNames, int rowCount) {
        super();
    }
                         
    public CasJTableModel(Vector data, Vector columnNames) {
        super();
    }
                         
    public CasJTableModel(Object[][] data, Object[] columnNames) {
        super();
    }
    
    public boolean isCellEditable(int rowNumber, int columnNumber) {
       return false;
    }
    
    public void sortAllRowsBy(int colIndex, boolean ascending) {
        Vector data = getDataVector();
        Collections.sort(data, new JTableModelSort(colIndex, ascending));
        fireTableStructureChanged();
    }
}
