package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.geworkbench.util.network.CellularNetWorkElementInformation;

class DetailTableIntegerRenderer extends JLabel implements TableCellRenderer {

	private final CellularNetworkKnowledgeWidget cellularNetworkKnowledgeWidget;

	private static final long serialVersionUID = 1399618132721043696L;

	private Border unselectedBorder = null;

	private Border selectedBorder = null;

	public DetailTableIntegerRenderer(
			CellularNetworkKnowledgeWidget cellularNetworkKnowledgeWidget) {
		this.cellularNetworkKnowledgeWidget = cellularNetworkKnowledgeWidget;
		setOpaque(true); // MUST do this for background to show up.
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		CellularNetWorkElementInformation element = cellularNetworkKnowledgeWidget
				.getOneRow(row);
		if (element == null) {
			return this;
		}

		boolean isDirty = element.isDirty();
		setBackground(table.getBackground());

		String tooltip = value.toString();
		String selectedText = "<html><font color=blue><b>" + value
				+ "</b></font></html>";
		String unselectedText = "<html><font color=blue><b>" + value
				+ "<b></font></html>";

		if (isDirty) {
			setForeground(Color.red);

			tooltip = "Please push the Refresh button to retrieve related information.";
			selectedText = "<html><font color=blue><i>Unknown"
					+ "</i></font></html>";
			unselectedText = "<html><font><i>Unknown" + "</i></font></html>";
		}

		if (isSelected) {
			if (selectedBorder == null) {
				selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
						table.getSelectionBackground());
			}
			setBorder(selectedBorder);

			setText(selectedText);
		} else {
			if (unselectedBorder == null) {
				unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
						table.getBackground());
			}
			setBorder(unselectedBorder);

			setText(unselectedText);
		}

		setToolTipText(tooltip);

		return this;
	}
}