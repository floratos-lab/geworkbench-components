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
 * @version $Id: MedusaParamPanel.java,v 1.2 2007-04-24 15:43:36 keshav Exp $
 */
public class MedusaParamPanel extends AbstractSaveableParameterPanel implements
		Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JTabbedPane parametersTabbedPane = new JTabbedPane();

	private JPanel mainPanel = new JPanel();

	private JPanel secondaryPanel = new JPanel();

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
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.appendSeparator("MEDUSA Paramaters");

		/* features */
		builder.append("Features File", loadFeaturesButton);
		builder.nextRow();

		/* regulators */
		builder.append("Regulators", regulatorCombo);
		builder.append(regulatorTextField, loadRegulatorsButton);
		// builder.nextRow();

		/* targets */
		builder.append("Targets", targetCombo);
		builder.append(targetTextField, loadTargetsButton);
		// builder.nextRow();

		/* intervals */
		builder.append("Interval Base", intervalBaseTextField);
		builder.nextRow();
		builder.append("Interval Bound", intervalBoundTextField);
		builder.nextRow();

		/* iterations */
		builder.append("Boosting Iterations", boostingIterationsTextField);
		builder.nextRow();

		/* all arrays */
		builder.append("All Arrays", allArraysCheckBox);

		mainPanel.add(builder.getPanel());

		parametersTabbedPane.add("Main", mainPanel);
		parametersTabbedPane.add("Secondary", secondaryPanel);

		this.add(parametersTabbedPane);
		// this.add(builder.getPanel());

	}

}
