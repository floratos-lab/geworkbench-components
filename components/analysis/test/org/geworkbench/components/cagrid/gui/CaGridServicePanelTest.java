package org.geworkbench.components.cagrid.gui;

import javax.swing.JDialog;

import junit.framework.TestCase;

import org.geworkbench.components.cagrid.gui.GridServicePanel;
import org.geworkbench.util.Util;

/**
 * 
 * @author keshav
 * @version $Id: CaGridServicePanelTest.java,v 1.1 2007-03-14 20:33:47 keshav Exp $
 */
public class CaGridServicePanelTest extends TestCase {

	/**
	 * Tests the JGridServicePanel
	 * 
	 */
	public void testCreateGridServicePanel() {
		JDialog dialogue = new JDialog();
		GridServicePanel jGridServicePanel = new GridServicePanel(
				"Test Panel");
		dialogue.add(jGridServicePanel);
		dialogue.setModal(true);
		dialogue.pack();
		Util.centerWindow(dialogue);
		dialogue.setVisible(true);
	}

}
