package org.geworkbench.components.genspace.ui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.geworkbench.components.genspace.server.stubs.Tool;

public class NameRenderer extends JLabel implements ListCellRenderer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7600527294850408384L;
	public NameRenderer() {
		setOpaque(true);
	}
	@Override
	 public Component getListCellRendererComponent(
	         JList list,
	         Object value,
	         int index,
	         boolean isSelected,
	         boolean cellHasFocus)
	     {
		if(value instanceof String)
			setText((String) value);
		else if(value instanceof Tool)
			setText(((Tool) value).getName());

		setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
		return this;
	}

}
