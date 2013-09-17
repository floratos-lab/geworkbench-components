package org.geworkbench.components.filtering;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

/**
 * Parameters panel for the <code>MultipleProbesetFilter</code>. Prompts the
 * user to handle genes with multiple probesets
 * 
 * @author tg2321
 * @version $Id$
 */

public class MultipleProbesetFilterPanel extends AbstractSaveableParameterPanel {

	private static final long serialVersionUID = -259414268047125408L;

	public enum Action {RETAIN_HIGH_COV, RETAIN_HIGH_MEAN, RETAIN_HIGH_MEDIAN};

	private static final String HIGHEST_COEFFICIENT_OF_VARIATION_OPTION = "retain marker with highest coefficient of variation";
	private static final String HIGHEST_MEAN_EXPRESSION_OPTION = "retain marker with highest mean expression";
	private static final String HIGHEST_MEDIAN_EXPRESSION_OPTION = "retain marker with highest median expression";

	private static final Map<String, Action> actionMap = new HashMap<String, Action>();
	static {
		actionMap.put(HIGHEST_COEFFICIENT_OF_VARIATION_OPTION, Action.RETAIN_HIGH_COV);
		actionMap.put(HIGHEST_MEAN_EXPRESSION_OPTION, Action.RETAIN_HIGH_MEAN);
		actionMap.put(HIGHEST_MEDIAN_EXPRESSION_OPTION, Action.RETAIN_HIGH_MEDIAN);
	}

	private JComboBox filterActionSelectionComboBox = new JComboBox(actionMap.keySet().toArray());

	public MultipleProbesetFilterPanel() {
		setLayout(new FlowLayout());
		JPanel dropDownContainer = new JPanel();
		JPanel container = new JPanel();
		dropDownContainer.setLayout(new GridLayout(0, 1));
		JLabel methodSelectionLabel = new JLabel("<html><p>Method - for each gene with multiple probesets (markers),</p></html>");
		dropDownContainer.add(methodSelectionLabel);
		dropDownContainer.add(filterActionSelectionComboBox);

		ParameterActionListener parameterActionListener = new ParameterActionListener(
				this);
		filterActionSelectionComboBox.addActionListener(parameterActionListener);

		container.setPreferredSize(new Dimension(250, 80));
		JPanel jPanel = new JPanel(new FlowLayout());
		jPanel.add(dropDownContainer);
		jPanel.setAlignmentX(LEFT_ALIGNMENT);
		jPanel.setMaximumSize(new Dimension(250, 80));
		this.add(jPanel);
	}

	public Action getFilterAction() {
		return actionMap.get( filterActionSelectionComboBox.getSelectedItem() );
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

		histStr += "Filter Action: ";
		histStr += parameters.get("filterActionSelection");
		histStr += "\n";

		return histStr;
	}

}