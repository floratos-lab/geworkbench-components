package org.geworkbench.components.genspace.ui;

import javax.swing.JPanel;

public abstract class SocialTab implements UpdateablePanel {
	protected JPanel panel1;
	SocialNetworksHome parentFrame;

	public JPanel getPanel() {
		return panel1;
	}

	public abstract String getName();

	public void updateFormFields() {
		// do nothing is OK
	}
}
