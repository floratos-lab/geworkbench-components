package org.geworkbench.components.genspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.geworkbench.engine.config.MenuListener;

/**
 * A menu for the genSpace component.
 * 
 * @author sheths
 */
public class GenSpaceMenu implements MenuListener {
	public static ClassLoader loader;
	@Override
	public ActionListener getActionListener(String var) {
		if (var.equalsIgnoreCase("Tools.genSpace")) {
			return new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					@SuppressWarnings("unused")
					GenSpace genspace = new GenSpace();
				}
			};
		}
		return null;
	}
	
}
