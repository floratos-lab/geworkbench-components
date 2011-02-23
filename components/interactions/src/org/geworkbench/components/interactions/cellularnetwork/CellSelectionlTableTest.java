package org.geworkbench.components.interactions.cellularnetwork;
import javax.swing.*; 
import javax.swing.table.*; 
import java.awt.event.*;
 

/**
  * @author min you
  */
public class CellSelectionlTableTest {
  CellSelectionTable table;
  
  public CellSelectionlTableTest() {
    //Create sample content for the JTable, don't care
    String[][] data = new String[7][5];
    String[] headers = new String[5];
    for (int col=0; col<data[0].length; col++) {
      headers[col] = "- "+col+" -";
      for (int row=0; row<data.length; row++)
        data[row][col] = "("+row+","+col+")";
    }
    DefaultTableModel dataModel = new DefaultTableModel(data, headers);
    //new Table
    table = new CellSelectionTable(dataModel);
    //Add a TableSelectionListener to the table which is part of this distribution
    table.getTableSelectionModel().addTableSelectionListener(new MyTableSelectionListener());

    //Some tests: add a column, remove a row.
    //dataModel.addColumn("- x -", new Object[]{"1", "2", "3", "4", "5", "6", "7"});
    //dataModel.removeRow(3);
    
   

    JScrollPane scrollpane = new JScrollPane(table);

    //Put it into a Frame
    JFrame frame = new JFrame();
    frame.addWindowListener(
      new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      }
    );
    frame.getContentPane().add(scrollpane);
    frame.pack();
    frame.setVisible(true);
  }

  
  /**
    * Sample TableSelectionListener.
    */
  public class MyTableSelectionListener implements TableSelectionListener {

    public void valueChanged(TableSelectionEvent e) {
      
      TableSelectionModel tsm = (TableSelectionModel)(e.getSource());
      int column = e.getColumnIndex();
      int firstIndex = e.getFirstIndex();
      int lastIndex = e.getLastIndex();
      boolean isAdjusting = e.getValueIsAdjusting();
      
      System.out.println("tsm="+tsm);
      System.out.println("column="+column);
      System.out.println("firstIndex="+firstIndex);
      System.out.println("lastIndex="+lastIndex);
      System.out.println("isAdjusting="+isAdjusting);
      //System.out.println("strValue="+strValue);

      TableModel tm = table.getModel();
      String columnId = tm.getColumnName(column);
      System.out.println("Changes at Column: " + columnId);
      System.out.println("selected rows:" + table.getSelectedRows().length);
      System.out.println("selected cols:" + table.getSelectedColumns().length);
      
    }
  }
}
