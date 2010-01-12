package org.geworkbench.components.mindy;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.components.mindy.MindyPlugin.ModulatorTargetModel;

/**
 * Handles column sorting in MINDY tables. Also handles modulator selection
 * for the targets table.
 * 
 * Refactored from MindyPlugin.java
 *
 * @author ch2514
 * @author os2201
 * @version $Id: $
 */
class ColumnHeaderListener extends MouseAdapter {
	private static Log log = LogFactory.getLog(ColumnHeaderListener.class);
	
	private final MindyPlugin mindyPlugin;

	/**
	 * @param mindyPlugin
	 */
	ColumnHeaderListener(MindyPlugin mindyPlugin) {
		this.mindyPlugin = mindyPlugin;
	}

	/**
	 * Handles mouse clicks on table column headers.
	 *
	 * @param evt -
	 *            MouseEvent
	 */
	public void mouseClicked(MouseEvent evt) {
		JTable table = ((JTableHeader) evt.getSource()).getTable();
		TableColumnModel colModel = table.getColumnModel();
		TableModel model = table.getModel();

		// The index of the column whose header was clicked
		int vColIndex = colModel.getColumnIndexAtX(evt.getX());
		int mColIndex = table.convertColumnIndexToModel(vColIndex);

		// Return if not clicked on any column header
		if (vColIndex == -1) {
			return;
		}

		// Determine if mouse was clicked between column heads
		Rectangle headerRect = table.getTableHeader().getHeaderRect(
				vColIndex);
		if (vColIndex == 0) {
			headerRect.width -= 3; // Hard-coded constant
		} else {
			headerRect.grow(-3, 0); // Hard-coded constant
		}
		if (!headerRect.contains(evt.getX(), evt.getY())) {
			// Mouse was clicked between column heads
			// vColIndex is the column head closest to the click

			// vLeftColIndex is the column head to the left of the click
			int vLeftColIndex = vColIndex;
			if (evt.getX() < headerRect.x) {
				vLeftColIndex--;
			}
		}

		if (model instanceof ModulatorModel) {
			// sort
			ModulatorModel mm = (ModulatorModel) model;
			boolean[] states = mm.getAscendSortStates();
			if (mColIndex < states.length) {
				boolean tmp = states[mColIndex];
				states[mColIndex] = !tmp;
				mm.sort(mColIndex, states[mColIndex]);
			}

		}
		if (model instanceof AggregateTableModel) {
			AggregateTableModel atm = (AggregateTableModel) model;
			boolean clickedCheckbox = false;
			// checkbox
			if ((this.mindyPlugin.getSelectionEnabledCheckBoxTarget().isSelected())
					&& (mColIndex >= 2)
					&& (evt.getX() >= headerRect.getX())
					&& (evt.getX() <= (headerRect.getX() + 15))) {
				clickedCheckbox = true;
//				JCheckBox cb = this.mindyPlugin.getHeaderCheckBoxes()[mColIndex];
				JCheckBox cb = mindyPlugin.getTableTab().getHeaderCheckBoxes()[mColIndex];
				if ((cb != null)
						&& (mColIndex < atm
								.getNumberOfModulatorCheckBoxes())) {
					boolean tmp = atm.getModulatorCheckBoxState(mColIndex);
					atm.setModulatorCheckBoxState(mColIndex, !tmp);
					cb
							.setSelected(atm
									.getModulatorCheckBoxState(mColIndex));
					DSGeneMarker m = atm.getEnabledModulatorAtPosition(mColIndex
							- AggregateTableModel.EXTRA_COLS);
					int tRowIndex = atm.getActiveTargets().indexOf(m);
					if (tRowIndex >= 0) {
						atm.setValueAt(!tmp, tRowIndex, 0);
						atm.fireTableDataChanged();
					}

					this.mindyPlugin.getSelectionEnabledCheckBoxTarget().setText(MindyPlugin.ENABLE_SELECTION
							+ " " + atm.getNumberOfMarkersSelected());

					if (atm.getCheckedModulators().size() == atm
							.getEnabledModulatorsSize())
						this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(true);
					else
						this.mindyPlugin.getSelectAllModsCheckBoxTarget().setSelected(false);
					this.mindyPlugin.getTableTab()
							.setTargetCheckboxesVisibility(this.mindyPlugin.getSelectionEnabledCheckBoxTarget()
									.isSelected());
					atm.fireTableStructureChanged();
				}
				if (mColIndex >= atm.getNumberOfModulatorCheckBoxes())
					log.error("check box index [" + mColIndex
							+ "] not in check box state");
			}

			// sort
			if ((mColIndex == 1)
					|| ((mColIndex >= 2) && (!clickedCheckbox))) {
				boolean[] states = atm.getAscendSortStates();
				if (mColIndex < states.length) {
					boolean tmp = states[mColIndex];
					states[mColIndex] = !tmp;
					atm.sort(mColIndex, states[mColIndex]);
					this.mindyPlugin.getAggregateModel().fireTableStructureChanged();
				}
			}
		}
		if (model instanceof ModulatorTargetModel) {
			// sort
			ModulatorTargetModel mtm = (ModulatorTargetModel) model;
			boolean[] states = mtm.getAscendSortStates();
			if (mColIndex < states.length) {
				boolean tmp = states[mColIndex];
				states[mColIndex] = !tmp;
				mtm.sort(mColIndex, states[mColIndex]);
			}
		}
	}
}