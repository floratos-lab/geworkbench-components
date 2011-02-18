package org.geworkbench.components.filtering;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Parameters panel for the <code>MultipleProbesetFilter</code>. Prompts the
 * user ho to handle genes with multiple probesets
 * 
 * @author unknown, tg2321
 * @version $ID$
 */

public class MultipleProbesetFilterPanel extends AbstractSaveableParameterPanel
		implements ItemListener {

	private static final long serialVersionUID = -259414268047125408L;

	private static Log log = LogFactory
			.getLog(MultipleProbesetFilterPanel.class);

	private static final String HIGHEST_COEFFICIENT_OF_VARIATION_OPTION = "retain marker with highest coefficient of variation";
	private static final String HIGHEST_MEAN_EXPRESSION_OPTION = "retain marker with highest mean expression";
	private static final String HIGHEST_MEDIAN_EXPRESSION_OPTION = "retain marker with highest median expression";
	// private static final String AVERAGE_EXPRESSION_VALUES_OPTION =
	// "average expression values (create new data file)";

	private static final String FILTER_ACTION_LABEL_SELECTION = "Filter Action Label";

	// private JComboBox filterActionSelectionComboBox = new JComboBox(new
	// String[]{HIGHEST_COEFFICIENT_OF_VARIATION_OPTION,
	// HIGHEST_MEAN_EXPRESSION_OPTION, HIGHEST_MEDIAN_EXPRESSION_OPTION,
	// AVERAGE_EXPRESSION_VALUES_OPTION});
	private JComboBox filterActionSelectionComboBox = new JComboBox(
			new String[] { HIGHEST_COEFFICIENT_OF_VARIATION_OPTION,
						   HIGHEST_MEAN_EXPRESSION_OPTION,
						   HIGHEST_MEDIAN_EXPRESSION_OPTION });

	private GridLayout gridLayout1 = new GridLayout(0, 1);
	private JLabel methodSelectionLabel = new JLabel("<html><p>Method - for each gene with multiple probesets (markers),</p></html>");

	private JCheckBox coefficientOfVariationCheckBox = new JCheckBox(
			"Highest CoV");
	private JCheckBox highestMeanCheckBox = new JCheckBox("Highest mean");
	private JCheckBox highestMedianCheckBox = new JCheckBox("Highest median");
	private JCheckBox averageExpressionValuesCheckBox = new JCheckBox("Average ");

	private boolean coefficientOfVariationStatus;
	private boolean highestMeanStatus;
	private boolean highestMedianStatus;
	private boolean averageExpressionValuesStatus;

	public MultipleProbesetFilterPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new FlowLayout());
		JPanel dropDownContainer = new JPanel();
		JPanel container = new JPanel();
		dropDownContainer.setLayout(gridLayout1);
		dropDownContainer.add(methodSelectionLabel);
		dropDownContainer.add(filterActionSelectionComboBox);

		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		filterActionSelectionComboBox.addActionListener(parameterActionListener);
		coefficientOfVariationCheckBox.setSelected(false);
		highestMeanCheckBox.setSelected(false);
		highestMedianCheckBox.setSelected(false);
		averageExpressionValuesCheckBox.setSelected(false);
		coefficientOfVariationStatus = false;
		highestMeanStatus = false;
		highestMedianStatus = false;
		averageExpressionValuesStatus = false;
		coefficientOfVariationCheckBox.addItemListener(this);
		highestMeanCheckBox.addItemListener(this);
		highestMedianCheckBox.addItemListener(this);
		averageExpressionValuesCheckBox.addItemListener(this);

		container.setPreferredSize(new Dimension(250, 80));
		JPanel jPanel = new JPanel(new FlowLayout());
		jPanel.add(dropDownContainer);
		jPanel.setAlignmentX(LEFT_ALIGNMENT);
		jPanel.setMaximumSize(new Dimension(250, 80));
		this.add(jPanel);
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		Object source = arg0.getItemSelectable();
		if (source == coefficientOfVariationCheckBox) {
			coefficientOfVariationStatus = !coefficientOfVariationStatus;
		} else if (source == highestMeanCheckBox) {
			highestMeanStatus = !highestMeanStatus;
		} else if (source == highestMedianCheckBox) {
			highestMedianStatus = !highestMedianStatus;
		} else if (source == averageExpressionValuesCheckBox) {
			averageExpressionValuesStatus = !averageExpressionValuesStatus;
		}
	}

	public int getFilterAction() {
		if (filterActionSelectionComboBox.getSelectedItem().equals(
				HIGHEST_COEFFICIENT_OF_VARIATION_OPTION))
			return MultipleProbesetFilter.HIGHEST_COEFFICIENT_OF_VARIATION;
		else if (filterActionSelectionComboBox.getSelectedItem().equals(
				HIGHEST_MEAN_EXPRESSION_OPTION))
			return MultipleProbesetFilter.HIGHEST_MEAN_EXPRESSION;
		else if (filterActionSelectionComboBox.getSelectedItem().equals(
				HIGHEST_MEDIAN_EXPRESSION_OPTION))
			return MultipleProbesetFilter.HIGHEST_MEDIAN_EXPRESSION;
		// else if
		// (filterActionSelectionComboBox.getSelectedItem().equals(AVERAGE_EXPRESSION_VALUES_OPTION))
		// return MultipleProbesetFilter.AVERAGE_EXPRESSION_VALUES;
		else {
			// also return ignore option, but not intended
			log.error("unexcepted option of missing value treatment");
			return MultipleProbesetFilter.HIGHEST_COEFFICIENT_OF_VARIATION;
		}
	}

	@Override
	public void setParameters(Map<Serializable, Serializable> parameters) {
		Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set
				.iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			if (key.equals("filterActionSelection")) {
				filterActionSelectionComboBox.setSelectedItem((String) value);
			}

		}
	}

	@Override
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put("filterActionSelection", (String) filterActionSelectionComboBox.getSelectedItem());

		return parameters;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDataSetHistory() {
		String histStr = "";
		Map<Serializable, Serializable> parameters = null;
		parameters = getParameters();
		histStr += "Multiple Probeset Filter: \n";

		histStr += FILTER_ACTION_LABEL_SELECTION + ": ";
		histStr += parameters.get("filterActionSelection");
		histStr += "\n";

		return histStr;
	}

	public boolean isCoefficientOfVariationStatusSelected() {
		return coefficientOfVariationStatus;
	}

	public boolean isHighestMeanStatusSelected() {
		return highestMeanStatus;
	}

	public boolean isHighestMedianStatusSelected() {
		return highestMedianStatus;
	}

	public boolean isAverageExpressionValuesStatusSelected() {
		return averageExpressionValuesStatus;
	}

}