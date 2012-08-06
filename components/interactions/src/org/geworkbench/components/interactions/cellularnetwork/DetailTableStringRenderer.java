package org.geworkbench.components.interactions.cellularnetwork;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.util.network.CellularNetWorkElementInformation;

class DetailTableStringRenderer extends JLabel implements TableCellRenderer {

	private final CellularNetworkKnowledgeWidget cellularNetworkKnowledgeWidget;

	private static final long serialVersionUID = 8232307195673766041L;

	private static final Map<String, String> geneTypeMap = new HashMap<String, String>();;

	static {
		geneTypeMap.put(Constants.TF, Constants.TRANSCRIPTION_FACTOR);
		geneTypeMap.put(Constants.K, Constants.KINASE);
		geneTypeMap.put(Constants.P, Constants.PHOSPHATASE);
	}

	private Border unselectedBorder = null;

	private Border selectedBorder = null;

	public DetailTableStringRenderer(
			CellularNetworkKnowledgeWidget cellularNetworkKnowledgeWidget) {

		this.cellularNetworkKnowledgeWidget = cellularNetworkKnowledgeWidget;
		setOpaque(true); // MUST do this for background to show up.

	}

	private static String insertLineBreaker(String tooltip) {
		if (tooltip.length() <= 100) {
			return tooltip;
		}

		StringBuffer toolTipText = new StringBuffer("<html>");
		int startIndex = 0;
		final int LENGTH = tooltip.length();
		while (startIndex < LENGTH) {
			int endIndex = startIndex + 100;
			if (endIndex < LENGTH) {
				while (tooltip.charAt(endIndex) != ' ') {
					endIndex++;
					if (endIndex == LENGTH) {
						endIndex--;
						break;
					}
				}

				toolTipText.append(tooltip.substring(startIndex, endIndex))
						.append("<br>");
			} else {
				toolTipText.append(tooltip.substring(startIndex, LENGTH))
						.append("</html>");
			}

			startIndex = endIndex;
		}

		return toolTipText.toString();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table,
			final Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {

		CellularNetWorkElementInformation element = cellularNetworkKnowledgeWidget
				.getOneRow(row);
		if (element == null)
			return this;

		final String selectedText = "<html><font color=blue><b>" + value
				+ "</b></font></html>";
		String unselectedText = selectedText;

		if (element.isDirty()) {
			unselectedText = "<html><font color=red><i>" + value
					+ "</i></font></html>";
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

		// prepare tooltip
		String tooltip = (String) value;
		String headerStr = cellularNetworkKnowledgeWidget
				.getDetailTableHeader(column);
		if (headerStr.equalsIgnoreCase(Constants.GENETYPELABEL)) {
			if (!tooltip.trim().equalsIgnoreCase("")) {
				String s = geneTypeMap.get(value);
				if (s != null)
					tooltip = s;
			}
		} else if (headerStr.equalsIgnoreCase(Constants.GENELABEL)) {
			String[] list = AnnotationParser.getInfo(element.getdSGeneMarker()
					.getLabel(), AnnotationParser.DESCRIPTION);
			if (list != null && list.length > 0)
				tooltip = list[0];
		}
		tooltip = insertLineBreaker(tooltip);
		setToolTipText(tooltip);

		return this;
	}
}