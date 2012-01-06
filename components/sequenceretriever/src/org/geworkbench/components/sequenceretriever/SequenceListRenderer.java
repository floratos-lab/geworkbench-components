package org.geworkbench.components.sequenceretriever;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class SequenceListRenderer extends JLabel implements
		ListCellRenderer {
	private static final long serialVersionUID = 1764773552579977347L;
	
	final SequenceRetriever sequenceRetriever;
	
	public SequenceListRenderer(SequenceRetriever sequenceRetriever) {
		this.sequenceRetriever = sequenceRetriever;
	}

	public Component getListCellRendererComponent(JList list, 
			Object value, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // the list and the cell have the focus
	{
		String s = value.toString();

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			setText("<html><font color=RED>" + s + "</font></html>");
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			if (sequenceRetriever.getCurrentRetrievedSequences().containsKey(s)) {
				setText("<html><font color=blue>" + s + "</font></html>");
			} else {
				setText(s);
			}
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		return this;
	}
}