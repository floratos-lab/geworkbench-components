package org.geworkbench.components.medusa;

import javax.swing.JDialog;

import junit.framework.TestCase;

/**
 * GUI tests for the {@link MedusaPlugin}.
 * 
 * @author keshav
 * @version $Id: MedusaPluginTest.java,v 1.1 2007-05-15 18:27:38 keshav Exp $
 */
public class MedusaPluginTest extends TestCase {

	MedusaData medusaData = null;

	MedusaPlugin medusaPlugin = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() {
		medusaData = new MedusaData();
		medusaPlugin = new MedusaPlugin(medusaData);
	}

	/**
	 * Tests setting up the MedusuaPlugin layout.
	 * 
	 */
	public void testMedusaPluginLayout() {

		JDialog dialog = new JDialog();
		dialog.add(medusaPlugin);
		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);

	}

}
