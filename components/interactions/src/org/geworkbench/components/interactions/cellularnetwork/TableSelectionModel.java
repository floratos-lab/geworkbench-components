package org.geworkbench.components.interactions.cellularnetwork;
import java.util.Vector; 
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import javax.swing.ListSelectionModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.event.*;

/**
  * This class represents the current state of the selection of
  * the CellSelectionTable.
  * It keeps the information in a list of ListSelectionModels, where
  * each Model represents the selection of a column.
  * @author Min You
  * @version $Id$
  */
  // This class is added to the Table as aPropertyChangeListener. It
  //  will be noticed when the Table has a new TableModel. So it can
  //  adjust itself to the new TableModel and can add itself
  //  as a TableModelListener to the new TableModel.
  // This class is added to the TableModel as a TableModelListener to
  //  be noticed when the TableModel changes. The number of
  //  ListSelectionModels in this class must be the same as the
  //  number of rows in the TableModel.
  // This class implements a ListSelectionListener and is added to
  //  all its ListSelectionModels. If this class
  //  changes one of its ListSelectionModels, the ListSelectionModel
  //  fires a new ListSelectionEvent which is received by this class.
  //  After receiving that event, this class fires a TableSelectionEvent.
  //  That approach saves me from calculating 'firstIndex' and 'lastIndex'
  //  for myself and makes sure that a TableSelectionEvent is fired
  //  whenever the selection changes.
public class TableSelectionModel
      implements PropertyChangeListener, TableModelListener {

  /** contains a ListSelectionModel for each column */
  private Vector<ListSelectionModel> listSelectionModels = new Vector<ListSelectionModel>();

  public TableSelectionModel() {
  }

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void addSelection(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.addSelectionInterval(row, row);
  }

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void setSelection(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.setSelectionInterval(row, row);
  }

  /**
   * Forwards the request to the ListSelectionModel
   * at the specified row.
   */
  
  public void setRowSelection(int row) {
	    for (ListSelectionModel lsm : listSelectionModels)
	    { 	     
	        lsm.setSelectionInterval(row, row);
	    }
  }

  /**
   * Forwards the request to the ListSelectionModel
   * at the specified row.
   */
  
  public void addRowSelection(int row) {
	    for (ListSelectionModel lsm : listSelectionModels)
	    { 	     
	        lsm.addSelectionInterval(row, row);
	    }
  }

  
  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void setSelectionInterval(int row1, int row2, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.setSelectionInterval(row1, row2);
  }

  /**
   * Forwards the request to the ListSelectionModel
   * at the specified row.
   */
 public void setRowSelectionInterval(int row1, int row2) {
	 for(ListSelectionModel lsm : listSelectionModels)	   
         lsm.setSelectionInterval(row1, row2);
 }
  
  
  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void setLeadSelectionIndex(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    if (lsm.isSelectionEmpty())
      lsm.setSelectionInterval(row, row);
    else
      //calling that method throws an IndexOutOfBoundsException when selection is empty (?, JDK 1.1.8, Swing 1.1)
      lsm.setLeadSelectionIndex(row);
  }
  
  /**
   * Forwards the request to the ListSelectionModel
   * at the specified row.
   */
 public void setRowLeadSelectionIndex(int row) {
	 for(ListSelectionModel lsm : listSelectionModels)
	 {
	 
         if (lsm.isSelectionEmpty())
           lsm.setSelectionInterval(row, row);
         else
           //calling that method throws an IndexOutOfBoundsException when selection is empty (?, JDK 1.1.8, Swing 1.1)
           lsm.setLeadSelectionIndex(row);
	 }
 }
  
  

  /**
    * Forwards the request to the ListSelectionModel
    * at the specified column.
    */
  public void removeSelection(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);
    lsm.removeSelectionInterval(row, row);
  }
  
  /**
   * Forwards the request to the ListSelectionModel
   * at the specified row.
   */
 public void removeRowSelection(int row) {
   for (ListSelectionModel lsm : listSelectionModels)    
      lsm.removeSelectionInterval(row, row);
 }
 

  /**
    * Calls clearSelection() of all ListSelectionModels.
    */
  public void clearSelection() {	 
	  for (ListSelectionModel lsm : listSelectionModels)  
         lsm.clearSelection();
     
  }

  /**
    * @return true, if the specified cell is selected.
    */
  public boolean isSelected(int row, int column) {
    ListSelectionModel lsm = getListSelectionModelAt(column);    
    return lsm.isSelectedIndex(row);
  }

  /**
    * Returns the ListSelectionModel at the specified column
    * @param index the column
    */
  public ListSelectionModel getListSelectionModelAt(int index) {
    return (ListSelectionModel)(listSelectionModels.elementAt(index));
  }

  /**
    * Set the number of columns.
    * @param count the number of columns
    */
  public void setColumns(int count) {
    listSelectionModels = new Vector<ListSelectionModel>();
    for (int i=0; i<count; i++) {
      addColumn();
    }
  }

  /**
    * Add a column to the end of the model.
    */
  private void addColumn() {
    DefaultListSelectionModel newListModel = new DefaultListSelectionModel();
    listSelectionModels.addElement(newListModel);
  }

  /**
    * Remove last column from model.
    */
  private void removeColumn() {
    //get last element
    DefaultListSelectionModel removedModel = (DefaultListSelectionModel)listSelectionModels.lastElement();
    listSelectionModels.removeElement(removedModel);

  }

  /**
    * When the TableModel changes, the TableSelectionModel
    * has to adapt to the new Model. This method is called
    * if a new TableModel is set to the JTable.
    */
  // implements PropertyChangeListener
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    if ("model".equals(evt.getPropertyName())) {
      TableModel newModel = (TableModel)(evt.getNewValue());
      setColumns(newModel.getColumnCount());
      TableModel oldModel = (TableModel)(evt.getOldValue());
      if (oldModel != null)
        oldModel.removeTableModelListener(this);
      //TableSelectionModel must be aware of changes in the TableModel
      newModel.addTableModelListener(this);
    }
  }

  /**
    * Is called when the TableModel changes. If the number of columns
    * had changed this class will adapt to it.
    */
  //implements TableModelListener
  @Override
  public void tableChanged(TableModelEvent e) {
    TableModel tm = (TableModel)e.getSource();
    int count = listSelectionModels.size();
    int tmCount = tm.getColumnCount();
    //works, because you can't insert columns into a TableModel (only add/romove(?)):
    //if columns were removed from the TableModel
    while (count-- > tmCount) {
      removeColumn();
    }
    //count == tmCount if was in the loop, else count < tmCount
    //if columns were added to the TableModel
    while (tmCount > count++) {
      addColumn();
    }
  }

  @Override
  public String toString() {
    String ret = "[\n";
    for (int col=0; col<listSelectionModels.size(); col++) {
      ret += "\'"+col+"\'={";
      ListSelectionModel lsm = getListSelectionModelAt(col);
      int startRow = lsm.getMinSelectionIndex();
      int endRow = lsm.getMaxSelectionIndex();
      for (int row=startRow; row<endRow; row++) {
        if (lsm.isSelectedIndex(row))
          ret += row + ", ";
      }
      if (lsm.isSelectedIndex(endRow))
        ret += endRow;
      ret += "}\n";
    }
    ret += "]";
    /*String ret = "";
    for (int col=0; col<listSelectionModels.size(); col++) {
      ret += "\'"+col+"\'={"+getListSelectionModelAt(col)+"}";
    }*/
    return ret;
  }

}
