package org.geworkbench.components.medusa;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: MedusaParamPanel.java,v 1.3 2007-04-24 16:24:12 keshav Exp $
 */
public class MedusaParamPanel extends AbstractSaveableParameterPanel implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane parametersTabbedPane = new JTabbedPane();

	/* features */
	private JButton loadFeaturesButton = new JButton("Load Features");

	/* regulators */
	private String REGULATOR_LIST = "Specify";

	private String REGULATOR_ACTIVATED = "Activated Markers";

	private JComboBox regulatorCombo = new JComboBox(new String[] {
			REGULATOR_ACTIVATED, REGULATOR_LIST });

	private String DEFAULT_REGULATOR_LIST = null;

	private JTextField regulatorTextField = new JTextField(
			DEFAULT_REGULATOR_LIST);

	private JButton loadRegulatorsButton = new JButton("Load Regulators");

	/* targets */
	private String TARGET_LIST = "Specify";

	private String TARGET_ALL = "Activated Markers";

	private JComboBox targetCombo = new JComboBox(new String[] { TARGET_ALL,
			TARGET_LIST });

	private String DEFAULT_TARGET_LIST = null;

	private JTextField targetTextField = new JTextField(DEFAULT_TARGET_LIST);

	private JButton loadTargetsButton = new JButton("Load Targets");

	/* discretization interval */

	private double intervalBase = 0;

	private JTextField intervalBaseTextField = new JTextField(String
			.valueOf(intervalBase));

	private double intervalBound = 0;

	private JTextField intervalBoundTextField = new JTextField(String
			.valueOf(intervalBound));

	/* boosting iterations */
	private int boostingIterations = 0;

	private JTextField boostingIterationsTextField = new JTextField(String
			.valueOf(boostingIterations));

	/* all arrays */
	private JCheckBox allArraysCheckBox = new JCheckBox("", true);

	/**
	 * 
	 * 
	 */
	public MedusaParamPanel() {

		this.setLayout(new BorderLayout());

		this.regulatorTextField.setEnabled(false);
		this.loadRegulatorsButton.setEnabled(false);
		this.targetTextField.setEnabled(false);
		this.loadTargetsButton.setEnabled(false);
		this.intervalBaseTextField.setEnabled(false);
		this.intervalBoundTextField.setEnabled(false);

		/* form layout */
		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 3dlu, 60dlu, 3dlu, 90dlu, 3dlu, 40dlu, 7dlu",
				"");

		/* build the main panel */
		DefaultFormBuilder mainBuilder = new DefaultFormBuilder(layout);
		mainBuilder.setDefaultDialogBorder();
		mainBuilder.appendSeparator("MEDUSA Main Paramaters");

		/* features */
		mainBuilder.append("Features File", loadFeaturesButton);
		mainBuilder.nextRow();

		/* regulators */
		mainBuilder.append("Regulators", regulatorCombo);
		mainBuilder.append(regulatorTextField, loadRegulatorsButton);
		// builder.nextRow();

		/* targets */
		mainBuilder.append("Targets", targetCombo);
		mainBuilder.append(targetTextField, loadTargetsButton);
		// builder.nextRow();

		/* intervals */
		mainBuilder.append("Interval Base", intervalBaseTextField);
		mainBuilder.nextRow();
		mainBuilder.append("Interval Bound", intervalBoundTextField);
		mainBuilder.nextRow();

		/* iterations */
		mainBuilder.append("Boosting Iterations", boostingIterationsTextField);
		mainBuilder.nextRow();

		/* all arrays */
		mainBuilder.append("All Arrays", allArraysCheckBox);

		/* build the secondary panel */
		DefaultFormBuilder secondaryBuilder = new DefaultFormBuilder(layout);
		secondaryBuilder.setDefaultDialogBorder();
		secondaryBuilder.addSeparator("MEDUSA Secondary Parameters");

		/* add tabs */
		parametersTabbedPane.add("Main", mainBuilder.getPanel());
		parametersTabbedPane.add("Secondary", secondaryBuilder.getPanel());

		this.add(parametersTabbedPane);

	}

}
