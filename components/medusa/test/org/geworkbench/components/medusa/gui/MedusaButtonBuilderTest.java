package org.geworkbench.components.medusa.gui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import junit.framework.TestCase;

/**
 * Tests the layout of the buttons in the gui area.
 * 
 * @author keshav
 * @version $Id: MedusaButtonBuilderTest.java,v 1.1 2007-06-25 19:15:28 keshav Exp $
 */
public class MedusaButtonBuilderTest extends TestCase {

	public void testButtonPanel() {
		JDialog dialog = new JDialog();

		JButton exportMotifsButton = new JButton();
		exportMotifsButton.setText("Export Motifs");

		JButton addToSetButton = new JButton();
		addToSetButton.setText("Add to set");

		JPanel buttonPanel = new JPanel();

		buttonPanel.add(exportMotifsButton);
		buttonPanel.add(addToSetButton);

		dialog.add(buttonPanel);
		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);

	}

}
