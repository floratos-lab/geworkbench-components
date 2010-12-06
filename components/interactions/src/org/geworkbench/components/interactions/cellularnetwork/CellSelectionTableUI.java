package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor; 
import javax.swing.plaf.basic.BasicTableUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.event.MouseInputListener;

/**
 * This class doesn't change the L&F of the JTable but listens to mouseclicks
 * and updates the TableSelectionModel.
 * 
 * @author Min You
 */
public class CellSelectionTableUI extends BasicTableUI {	 
	public static ComponentUI createUI(JComponent c) {
		return new CellSelectionTableUI();
	}

	protected MouseInputListener createMouseInputListener() {		 
		return new AnySelectionMouseInputHandler();		 
	}

	/**
	 * to get access to the table from the inner class MyMouseInputHandler
	 */
	protected JTable getTable() {
		return table;
	}

	/**
	 * updates the TableSelectionModel.
	 */
	protected void updateTableSelectionModel(int row, int column,
			MouseEvent e ) {

		CellSelectionTable t = (CellSelectionTable) getTable();
		column = t.convertColumnIndexToModel(column);
		TableSelectionModel tsm = t.getTableSelectionModel();

		int anchorIndex = tsm.getListSelectionModelAt(column)
				.getAnchorSelectionIndex();
		
		 
	 
		if (isMenuShortcutKeyDown(e)) {
			if (tsm.isSelected(row, column)) {
				if (column == 0)
					tsm.removeRowSelection(row);
				else
					tsm.removeSelection(row, column);

			} else {
				if (column == 0)
					tsm.addRowSelection(row);				   
				else
					tsm.addSelection(row, column);
			}
			 
			 
		} else if ((e.isShiftDown()) && (anchorIndex != -1)) {
			if (column == 0)
				tsm.setRowSelectionInterval(anchorIndex, row);
			else
				tsm.setSelectionInterval(anchorIndex, row, column);
		} else {
			tsm.clearSelection();
			if (column == 0)
				tsm.setRowSelection(row);
			else
				tsm.setSelection(row, column);

		} 
	} // updateTableSelectionModel()

	/**
	 * Almost the same implementation as its super class. Except updating the
	 * TableSelectionModel rather than the default ListSelectionModel.
	 */
	// Some methods which are called in the super class are private.
	// Thus I couldn't call them. Calling the method of the super
	// class itself should do it, but you never know. Sideeffects may occur...
	public class AnySelectionMouseInputHandler extends MouseInputHandler {

		public void mousePressed(MouseEvent e) {			 
			super.mousePressed(e);
 
			if (!SwingUtilities.isLeftMouseButton(e)) {
				return;
			}

			/*
			 * if (phantomMousePressed == true) { return; } phantomMousePressed =
			 * true;
			 */

			Point p = e.getPoint();
			int row = getTable().rowAtPoint(p);
			int column = getTable().columnAtPoint(p);
			// The autoscroller can generate drag events outside the Table's
			// range.
			if ((column == -1) || (row == -1)) {
				return;
			}

			/*
			 * Adjust the selection if the event was not forwarded to the editor
			 * above *or* the editor declares that it should change selection
			 * even when events are forwarded to it.
			 */
			// PENDING(philip): Ought to convert mouse event, e, here.
			// if (!repostEvent || table.getCellEditor().shouldSelectCell(e)) {
			TableCellEditor tce = getTable().getCellEditor();
			if ((tce == null) || (tce.shouldSelectCell(e))) {

				updateTableSelectionModel(row, column, e );
						 
				getTable().repaint();

			}
		}// mousePressed()
	}
	
	
	boolean isMenuShortcutKeyDown(InputEvent event) {
        return (event.getModifiers() & 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
    }
	
}
