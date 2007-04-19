package org.geworkbench.components.medusa;

import java.awt.BorderLayout;
import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author keshav
 * @version $Id: MedusaParamPanel.java,v 1.1 2007-04-19 20:15:29 keshav Exp $
 */
public class MedusaParamPanel extends AbstractSaveableParameterPanel implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* regulators */
	private String REGULATOR_LIST = "Specify";

	private String REGULATOR_ACTIVATED = "Activated Markers";

	private JComboBox regulatorCombo = new JComboBox(new String[] {
			REGULATOR_ACTIVATED, REGULATOR_LIST });

	private String DEFAULT_REGULATOR_LIST = null;

	private JTextField regulatorTextField = new JTextField(
			DEFAULT_REGULATOR_LIST);

	/* targets */
	private String TARGET_LIST = "Specify";

	private String TARGET_ALL = "Activated Markers";

	private JComboBox targetCombo = new JComboBox(new String[] { TARGET_ALL,
			TARGET_LIST });

	private String DEFAULT_TARGET_LIST = null;

	private JTextField targetTextField = new JTextField(DEFAULT_TARGET_LIST);

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
		this.targetTextField.setEnabled(false);
		this.intervalBaseTextField.setEnabled(false);
		this.intervalBoundTextField.setEnabled(false);

		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 3dlu, 60dlu, 3dlu, 90dlu, 3dlu, 40dlu, 7dlu",
				"");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("MEDUSA Paramaters");

		builder.append("Regulators", regulatorCombo);
		builder.append(regulatorTextField);
		builder.nextRow();

		builder.append("Targets", targetCombo);
		builder.append(targetTextField);
		builder.nextRow();

		builder.append("Interval Base", intervalBaseTextField);
		builder.nextRow();
		builder.append("Interval Bound", intervalBoundTextField);
		builder.nextRow();

		builder.append("Boosting Iterations", boostingIterationsTextField);
		builder.nextRow();

		builder.append("All Arrays", allArraysCheckBox);

		this.add(builder.getPanel());

	}

}
