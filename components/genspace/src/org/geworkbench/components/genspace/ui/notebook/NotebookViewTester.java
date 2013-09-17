package org.geworkbench.components.genspace.ui.notebook;

import java.awt.Color;

import javax.swing.JFrame;

import org.geworkbench.components.genspace.GenSpaceServerFactory;

public class NotebookViewTester {
	public static void main(String[] args) {
		NotebookPanel view = new NotebookPanel();
		JFrame f = new JFrame();
		f.setBackground(Color.green);
		GenSpaceServerFactory.userLogin("jon", "test123");
		f.setSize(500, 500);
		f.add(view);
		view.init();
		view.updateFormFields();
		f.pack();
		f.setVisible(true);
//			view.viewNotebook();
	}

}
