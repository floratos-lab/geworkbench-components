package org.geworkbench.components.cagrid.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author keshav
 * @version $Id: GridSelectionButtonListener.java,v 1.1 2007/03/15 16:21:39
 *          keshav Exp $
 */
public class GridSelectionButtonListener implements ActionListener {
	private static final String LOCAL = "Local";

	private Log log = LogFactory.getLog(GridSelectionButtonListener.class);

	private static final String GRID = "Grid";

	boolean gridAnalysis = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		log.debug("event object: " + e.getSource());
		String actionCommand = e.getActionCommand();

		if (StringUtils.equals(actionCommand, GRID)) {
			gridAnalysis = true;
		}

		if (StringUtils.equals(actionCommand, LOCAL)) {
			gridAnalysis = false;
		}
	}

	/**
	 * 
	 * @return
	 */
	public boolean isGridVersion() {
		return gridAnalysis;
	}
}
