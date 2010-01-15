package org.geworkbench.components.versioninfo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * The component to support the version dialog.
 * 
 * @author not attributable
 * @version $Id$
 */
public class VersionInfoComponent implements
		org.geworkbench.engine.config.MenuListener {
	// Holds references to listeners of menu items for this component.
	private Map<String, ActionListener> listeners = new HashMap<String, ActionListener>();

	public VersionInfoComponent() {
		// Register menu items listener - sessions dialog
		ActionListener allSession = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VersionInfoDialog dlg = new VersionInfoDialog();
				dlg.setLocationRelativeTo(null);
				dlg.setModal(true);
				dlg.setVisible(true);
			}
		};
		listeners.put("Help.Version", allSession);
	}

	/**
	 * Return a listener which registered with the var string.
	 * 
	 * @param var
	 *            - the name of the listener
	 * @return - the listener
	 */
	public ActionListener getActionListener(String var) {
		// in fact, this component only handles version dialog, so the map are not necessary 
		// and it should always return an action listener that shows version dialog. 
		return (ActionListener) listeners.get(var);
	}

}
