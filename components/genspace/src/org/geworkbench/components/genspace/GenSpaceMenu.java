package org.geworkbench.components.genspace;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.components.genspace.GenSpace;

/**
 * A menu for the genSpace component.
 * @author sheths
 */
public class GenSpaceMenu implements MenuListener {

	public ActionListener getActionListener(String var) {
		if (var.equalsIgnoreCase("Tools.genSpace")) {
			return new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					GenSpace genspace = new GenSpace(); 
				}
			};
		}
		return null;
	}
}
