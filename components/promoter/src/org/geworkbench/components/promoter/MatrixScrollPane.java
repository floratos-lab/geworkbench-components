package org.geworkbench.components.promoter;

import java.text.DecimalFormat;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author not attributable
 * @version $Id$
 */
public class MatrixScrollPane {

    public static JTable createMatrixTable(boolean showCounts, Matrix matrix) {

        final String[] cols = createColumnsName(matrix);
        final Object[][] rowData = createRowsData(showCounts, matrix);
        if(cols==null || rowData==null){
            return null;
        }

        TableModel tableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 7240133465615903832L;

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

        return new JTable(tableModel);
    }

    /**
     * createRowsData
     *
     * @param matrix Matrix
     * @return Object[][]
     */
    private static Object[][] createRowsData(boolean showCounts, Matrix matrix) {
    	DecimalFormat formatPV = null; 
    	
    	if (showCounts){
    		formatPV = new DecimalFormat("0");
    	}else{
    		formatPV = new DecimalFormat("0.00");
    	}
    	
    	
        if (matrix != null && matrix.getLength() > 0) {
            char[] symbols = matrix.getSymbols();
            int rowLength = symbols.length;
            int columnlength = matrix.getLength();
            Object[][] cols = new Object[rowLength][columnlength + 1];
            
            Distribution[] dis = null;
            if (showCounts){
            	dis = matrix.getRawCountTable();
            
            }else{
            	dis = matrix.getNormalizedCountTable();	
            }
            
            for (int i = 0; i < rowLength; i++) {
                cols[i][0] = new Character(symbols[i]);
                for (int j = 0; j < columnlength; j++) {
                	char sym = symbols[i];
                	double symbol = dis[j].get(sym);
                    cols[i][j + 1] = formatPV.format(symbol);//)matrix.getMatch(j, symbols[i]);
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
    private static String[] createColumnsName(Matrix matrix) {
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
