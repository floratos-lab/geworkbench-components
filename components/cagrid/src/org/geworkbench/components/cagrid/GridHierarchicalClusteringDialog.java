package org.geworkbench.components.cagrid;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.geworkbench.util.Util;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Dim;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Distance;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.HierarchicalClusteringParameter;
import edu.columbia.geworkbench.cagrid.cluster.hierarchical.Method;

/**
 * @author John Watkinson
 * @version $Id: GridHierarchicalClusteringDialog.java,v 1.1 2007/01/09 16:16:49
 *          keshav Exp $
 */
public class GridHierarchicalClusteringDialog extends JDialog {

	private static final String SPEARMAN = "Spearman";
	private static final String PEARSON = "Pearson";
	private static final String EUCLIDEAN = "Euclidean";
	private static final String BOTH = "Both";
	private static final String MICROARRAY = "Microarray";
	private static final String MARKER = "Marker";
	private static final String COMPLETE = "Complete";
	private static final String AVERAGE = "Average";
	private static final String SINGLE = "Single";
	private HierarchicalClusteringParameter parameters = null;

	/**
	 * @throws HeadlessException
	 */
	public GridHierarchicalClusteringDialog() throws HeadlessException {
		FormLayout layout = new FormLayout(
				"right:max(60dlu;pref), 3dlu, 100dlu, 7dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("Hierarchical Clustering Parameters");
		final JComboBox methodBox = new JComboBox(new String[] { SINGLE,
				AVERAGE, COMPLETE });
		final JComboBox dimBox = new JComboBox(new String[] { MARKER,
				MICROARRAY, BOTH });
		final JComboBox distanceBox = new JComboBox(new String[] { EUCLIDEAN,
				PEARSON, SPEARMAN });
		builder.append("Method", methodBox);
		builder.append("Dimension", dimBox);
		builder.append("Distance", distanceBox);
		JPanel dialogPanel = builder.getPanel();
		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Dim dim = null;
				String dimString = (String) dimBox.getSelectedItem();
				if (dimString.equalsIgnoreCase(MARKER))
					dim = Dim.marker;
				else if (dimString.equalsIgnoreCase(MICROARRAY))
					dim = Dim.microarray;
				else
					dim = Dim.both;

				Distance distance = null;
				String distanceString = (String) distanceBox.getSelectedItem();
				if (distanceString.equalsIgnoreCase(EUCLIDEAN))
					distance = Distance.euclidean;
				else if (distanceString.equalsIgnoreCase(PEARSON))
					distance = Distance.pearson;
				else
					distance = Distance.spearman;

				Method method = null;
				String methodString = (String) methodBox.getSelectedItem();
				if (methodString.equalsIgnoreCase(SINGLE))
					method = Method.single;
				else if (methodString.equalsIgnoreCase(AVERAGE))
					method = Method.average;
				else
					method = Method.complete;

				parameters = new HierarchicalClusteringParameter(dim, distance,
						method);
				dispose();
			}
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(dialogPanel, BorderLayout.CENTER);
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);
		getContentPane().add(mainPanel);
		setModal(true);
	}

	/**
	 * @return HierarchicalClusteringParameter
	 */
	public HierarchicalClusteringParameter getParameters() {
		pack();
		setSize(300, 200);
		Util.centerWindow(this);
		setVisible(true);
		return parameters;
	}

}
