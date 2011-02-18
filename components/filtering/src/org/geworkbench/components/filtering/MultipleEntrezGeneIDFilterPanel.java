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

public class MultipleEntrezGeneIDFilterPanel extends
		AbstractSaveableParameterPanel implements ItemListener {

	private static final long serialVersionUID = -6023380290219889724L;

	private static final String NOT_FILTERED = "not filtered";
	private static final String FILTERED = "filtered";

	private static Log log = LogFactory.getLog(MultipleEntrezGeneIDFilterPanel.class);

	private JLabel filterOutMarkersLabel = new JLabel("<html><p> </p><p>Filter action: </p></html>");
	final String NO_ENTREZ_IDS = "No Entrez ID";
	final String MULTIPLE_ENTREZ_IDS_OPTION = "Multiple Entrez IDs";
	final String FILTER_ACTION_LABEL_SELECTION = "Filter Action Label";

	final String REMOVE_MARKERS_OPTION = "Remove markers";
	final String NEW_MARKERS_FROM_MATCHING_FILTER_OPTION = "Create new marker set(s) from markers matching filter (_GID_none, _GID_multiple)";
	final String NEW_MARKERS_EXCLUDING_MATCHING_FILTER_OPTION = "Create new marker set excluding markers matching filter ( GID filtered)";

	private JComboBox filterActionSelectionComboBox = new JComboBox(
			new String[] { REMOVE_MARKERS_OPTION,
					NEW_MARKERS_FROM_MATCHING_FILTER_OPTION,
					NEW_MARKERS_EXCLUDING_MATCHING_FILTER_OPTION });

	private JCheckBox noEntrezIDsCheckBox = new JCheckBox(NO_ENTREZ_IDS);
	private boolean noEntrezIDsStatus;

	private JCheckBox multipleEntrezIDsCheckBox = new JCheckBox(
			MULTIPLE_ENTREZ_IDS_OPTION);
	private boolean multipleEntrezIDsStatus;

	private GridLayout gridLayout = new GridLayout(0, 1);
	private JLabel filterActionLabel = new JLabel(
			"<html><p>Filter out markers with: </p><p></p></html>");

	public MultipleEntrezGeneIDFilterPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {

		noEntrezIDsCheckBox.setSelected(false);
		noEntrezIDsStatus = false;

		multipleEntrezIDsCheckBox.setSelected(false);
		multipleEntrezIDsStatus = false;

		JPanel container = new JPanel(gridLayout);
		container.setPreferredSize(new Dimension(500, 140));

		container.add(filterActionLabel);
		container.add(noEntrezIDsCheckBox);
		container.add(multipleEntrezIDsCheckBox);
		noEntrezIDsCheckBox.addItemListener(this);
		multipleEntrezIDsCheckBox.addItemListener(this);

		container.setAlignmentX(LEFT_ALIGNMENT);

		container.add(filterOutMarkersLabel);
		container.add(filterActionSelectionComboBox);

		ParameterActionListener parameterActionListener = new ParameterActionListener(this);
		filterActionSelectionComboBox.addActionListener(parameterActionListener);
		noEntrezIDsCheckBox.addActionListener(parameterActionListener);
		multipleEntrezIDsCheckBox.addActionListener(parameterActionListener);

		JPanel panel = new JPanel(new FlowLayout());
		panel.add(container);
		panel.setAlignmentX(LEFT_ALIGNMENT);

		this.add(panel);
	}

	@Override
	public void itemStateChanged(ItemEvent paramItemEvent) {
		Object source = paramItemEvent.getItemSelectable();

		if (source == noEntrezIDsCheckBox) {
			noEntrezIDsStatus = !noEntrezIDsStatus;
		} else if (source == multipleEntrezIDsCheckBox) {
			multipleEntrezIDsStatus = !multipleEntrezIDsStatus;
		}
	}

	public int getFilterAction() {
		if (filterActionSelectionComboBox.getSelectedItem().equals(
				REMOVE_MARKERS_OPTION))
			return MultipleEntrezGeneIDFilter.REMOVE;
		else if (filterActionSelectionComboBox.getSelectedItem().equals(
				NEW_MARKERS_FROM_MATCHING_FILTER_OPTION))
			return MultipleEntrezGeneIDFilter.CREATE_FROM_MATCHING;
		else if (filterActionSelectionComboBox.getSelectedItem().equals(
				NEW_MARKERS_EXCLUDING_MATCHING_FILTER_OPTION))
			return MultipleEntrezGeneIDFilter.CREATE_FROM_EXCLUDING;
		else {
			// also return ignore option, but not intended
			log.error("unexcepted option of missing value treatment");
			return MultipleEntrezGeneIDFilter.REMOVE;
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

			if (key.equals("noEntrezIDs")) {
				this.noEntrezIDsCheckBox.setSelected((Boolean) value);
			} else if (key.equals("multipleEntrezIDs")) {
				this.multipleEntrezIDsCheckBox.setSelected((Boolean) value);
			}

			if (key.equals("filterActionSelection")) {
				filterActionSelectionComboBox.setSelectedItem((String) value);
			}
		}
	}

	@Override
	public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("noEntrezIDs", noEntrezIDsCheckBox.isSelected());
		parameters.put("multipleEntrezIDs", multipleEntrezIDsCheckBox.isSelected());
		parameters.put("filterActionSelection", (String) filterActionSelectionComboBox.getSelectedItem());
		return parameters;
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
	}

	/**
	 * Check if the "noEntrezIDsStatus" option is selected.
	 */
	public boolean isNoEntrezIDsStatusSelected() {
		return noEntrezIDsStatus;
	}

	/**
	 * Check if the "multipleEntrezIDsStatus" option is selected.
	 */
	public boolean isMultipleEntrezIDsStatusSelected() {
		return multipleEntrezIDsStatus;
	}

	@Override
	public String getDataSetHistory() {
		String histStr = "";
		Map<Serializable, Serializable> parameters = null;
		parameters = getParameters();

		histStr += "Multiple Entrez Gene ID Filter parameters:\n";

		histStr += NO_ENTREZ_IDS + ": ";
		histStr += isNoEntrezIDsStatusSelected() ? FILTERED : NOT_FILTERED;
		histStr += "\n";

		histStr += MULTIPLE_ENTREZ_IDS_OPTION + ": ";
		histStr += isMultipleEntrezIDsStatusSelected() ? FILTERED : NOT_FILTERED;
		histStr += "\n";

		histStr += FILTER_ACTION_LABEL_SELECTION + ": ";
		histStr += parameters.get("filterActionSelection");
		histStr += "\n";

		return histStr;
	}

}