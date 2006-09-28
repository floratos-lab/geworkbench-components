package org.geworkbench.components.promoter;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.text.DecimalFormat;

/**
 * <p>Title: </p>
 * <p/>
 * <p>Description: </p>
 * <p/>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p/>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MatrixScrollPane extends JScrollPane {
    private JTable matrixTable;
    private static final DecimalFormat formatPV = new DecimalFormat("0.00");

    public MatrixScrollPane() {
        super();
        matrixTable = new JTable();

    }

    public MatrixScrollPane(JTable table) {
        super(table);
    }

    public JTable createMatrixTable(Matrix matrix) {

        final String[] cols = createColumnsName(matrix);
        final Object[][] rowData = createRowsData(matrix);
        if(cols==null || rowData==null){
            return null;
        }

        TableModel tableModel = new AbstractTableModel() {
            public String getColumnName(int column) {
                return cols[column].toString();
            }

            public int getRowCount() {
                return rowData.length;
            }

            public int getColumnCount() {
                return cols.length;
            }

            public Object getValueAt(int row, int col) {
                return rowData[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        matrixTable = new JTable(tableModel);
        return matrixTable;
    }

    /**
     * createRowsData
     *
     * @param matrix Matrix
     * @return Object[][]
     */
    private Object[][] createRowsData(Matrix matrix) {
        if (matrix != null && matrix.getLength() > 0) {
            char[] symbols = matrix.getSymbols();
            int rowLength = symbols.length;
            int columnlength = matrix.getLength();
            Object[][] cols = new Object[rowLength][columnlength + 1];
            Distribution[] dis = matrix.getRawCountTable();
            for (int i = 0; i < rowLength; i++) {
                cols[i][0] = new Character(symbols[i]);
                for (int j = 0; j < columnlength; j++) {
                    cols[i][j + 1] = formatPV.format(dis[j].get(symbols[i]));//)matrix.getMatch(j, symbols[i]);
                }
            }
            return cols;

        }
        return null;
    }

    /**
     * createColumnsName
     *
     * @param matrix Matrix
     * @return String[]
     */
    private String[] createColumnsName(Matrix matrix) {
        if (matrix != null && matrix.getLength() > 0) {
            int length = matrix.getLength() + 1;
            String[] cols = new String[length];
            cols[0] = "    ";
            for (int i = 1; i < length; i++) {
                cols[i] = i + "  ";
            }
            return cols;
        }
        return null;

    }
}
