package org.geworkbench.components.cagrid.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author keshav
 * @version $Id: GridSelectionButtonListener.java,v 1.1 2007-03-15 16:21:39 keshav Exp $
 */
public class GridSelectionButtonListener implements ActionListener {
	private Log log = LogFactory.getLog(GridSelectionButtonListener.class);

	private static final String GRID = "Grid";
	boolean gridVersion = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		log.debug("event object: " + e.getSource());
		String actionCommand = e.getActionCommand();

		if (StringUtils.equals(actionCommand, GRID)) {
			gridVersion = true;
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean isGridVersion() {
		return gridVersion;
	}
}
