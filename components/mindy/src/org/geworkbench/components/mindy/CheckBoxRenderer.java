package org.geworkbench.components.mindy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * For rendering modulator checkboxes on the targets table column headers.
 *
 * Refactored from MindyPlugin.java
 *
 * @author ch2514
 * @author os2201
 * @version $Id$
 */
class CheckBoxRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -2798656880742337015L;

	private static Log log = LogFactory.getLog(CheckBoxRenderer.class);

	private final MindyPlugin mindyPlugin;

	/**
	 * @param mindyPlugin
	 */
	CheckBoxRenderer(MindyPlugin mindyPlugin) {
		this.mindyPlugin = mindyPlugin;
	}

	/**
	 * Specifies how to render targets table column headers.
	 *
	 * @param table
	 *            - targets table
	 * @param value
	 *            - the value of the cell to be rendered
	 * @param isSelected
	 *            - true if the cell is to be rendered with the selection
	 *            highlighted; otherwise false
	 * @param hasFocus
	 *            - true if the header cell has focus, and false otherwise
	 * @param row
	 *            - the row index of the cell being drawn. When drawing the
	 *            header, the value of row is -1
	 * @param column
	 *            - the column index of the cell being drawn
	 * @return
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		Component c = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);
		Object o = table.getModel();
		if (o instanceof AggregateTableModel) {
			AggregateTableModel atm = (AggregateTableModel) o;
			Border loweredetched = BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED);
			this.mindyPlugin.getTableTab().setHeaderCheckBoxes(
					new JCheckBox[table.getColumnCount()]);
			if (column == 0) {
				JPanel blank = new JPanel();
				JLabel blankLabel = new JLabel("  ");
				blankLabel.setBackground(c.getBackground());
				blank.setBorder(loweredetched);
				blank.add(blankLabel);
				int maxSize = (int) blank.getSize().getWidth();
				blank.setMaximumSize(new Dimension(maxSize, 10));
				table.getColumnModel().getColumn(column).setMinWidth(
						MindyPlugin.MIN_CHECKBOX_WIDTH);
				return blank;
			} else if (column == 1) {
				JLabel jl = new JLabel(atm.getColumnName(column),
						SwingConstants.LEFT);
				jl.setBackground(c.getBackground());
				JPanel blank = new JPanel();
				blank.setBorder(loweredetched);
				blank.add(jl);
				blank.setMaximumSize(new Dimension((int) blank.getSize()
						.getWidth(), 10));
				table.getColumnModel().getColumn(column).setMinWidth(
						MindyPlugin.MIN_MARKER_NAME_WIDTH);
				return blank;
			} else if (column < this.mindyPlugin.getTableTab()
					.getHeaderCheckBoxes().length) {
				// TODO resizing
				/*
				 * if (w > scrollPane.getWidth()) {
				 * table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); } else {
				 * table
				 * .setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS); }
				 */
				this.mindyPlugin.getTableTab().getHeaderCheckBoxes()[column] = new JCheckBox();
				this.mindyPlugin.getTableTab().getHeaderCheckBoxes()[column]
						.setEnabled(true);
				if (column < atm.getNumberOfModulatorCheckBoxes())
					this.mindyPlugin.getTableTab().getHeaderCheckBoxes()[column]
							.setSelected(atm.getModulatorCheckBoxState(column));
				else
					log.error("column ["
									+ column
									+ "] does not have a corresponding checkbox state.");
				JLabel jl = new JLabel("  " + atm.getColumnName(column));
				jl.setBackground(c.getBackground());
				JPanel p = new JPanel(new BorderLayout());
				p.setBorder(loweredetched);
				if (this.mindyPlugin.tableTab.getSelectionEnabledCheckBoxTarget()
						.isSelected())
					p.add(
							this.mindyPlugin.getTableTab()
									.getHeaderCheckBoxes()[column],
							BorderLayout.WEST);
				p.add(jl, BorderLayout.CENTER);
				p.setSize((int) p.getSize().getWidth(), 50);
				return p;
			}
		}

		if (o instanceof ModulatorModel) {
			if (column == 0) {
				JPanel blank = new JPanel();
				JLabel blankLabel = new JLabel("  ");
				blank.add(blankLabel);
				int maxSize = (int) blank.getSize().getWidth();
				blank.setMaximumSize(new Dimension(maxSize, 10));
				table.getColumnModel().getColumn(column).setMinWidth(
						MindyPlugin.MIN_CHECKBOX_WIDTH);
				return blank;
			}
		}

		if (o instanceof ModulatorTargetModel) {
			if ((column == 0) || (column == 2)) {
				JPanel blank = new JPanel();
				JLabel blankLabel = new JLabel("  ");
				blank.add(blankLabel);
				int maxSize = (int) blank.getSize().getWidth();
				blank.setMaximumSize(new Dimension(maxSize, 10));
				table.getColumnModel().getColumn(column).setMinWidth(
						MindyPlugin.MIN_CHECKBOX_WIDTH);
				return blank;
			}
		}

		return c;
	}
}