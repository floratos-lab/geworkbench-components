package org.geworkbench.components.medusa;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * 
 * @author keshav
 * @version $Id: MedusaPlugin.java,v 1.1 2007-05-15 18:27:38 keshav Exp $
 */
public class MedusaPlugin extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private MedusaData medusaData = null;

	public MedusaPlugin(MedusaData medusaData) {
		super();
		this.medusaData = medusaData;

		JTabbedPane tabbedPane = new JTabbedPane();

		JPanel motifPanel = new JPanel();
		tabbedPane.add("Motif", motifPanel);

		JPanel pssmPanel = new JPanel();
		tabbedPane.add("PSSM", pssmPanel);

		setLayout(new BorderLayout());
		add(tabbedPane, BorderLayout.CENTER);
	}

}
