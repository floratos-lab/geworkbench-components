package org.geworkbench.components.cagrid.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 * @author keshav
 * @version $Id: IndexServiceSelectionButtonListener.java,v 1.1 2007-03-15 15:41:35 keshav Exp $
 */
public class IndexServiceSelectionButtonListener implements ActionListener {
	String url = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		url = e.getActionCommand();
		System.out.println("action command: " + url);
	}

	/**
	 * 
	 * @return
	 */
	public String getUrl() {
		return url;
	}

}
