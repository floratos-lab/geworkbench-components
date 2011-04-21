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

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

/**
 * 
 * @author zji
 * @version $Id$
 *
 */
public class MultipleEntrezGeneIDFilterPanel extends
		AbstractSaveableParameterPanel implements ItemListener {

	private static final long serialVersionUID = -6023380290219889724L;

	private static final String NOT_FILTERED = "not filtered";
	private static final String FILTERED = "filtered";

	public enum Action {REMOVE, CREATE_FROM_MATCHING, CREATE_FROM_EXCLUDING};

	private static final String REMOVE_MARKERS_OPTION = "Remove markers";
	private static final String NEW_MARKERS_FROM_MATCHING_FILTER_OPTION = "Create new marker set(s) from markers matching filter (_GID_none, _GID_multiple)";
	private static final String NEW_MARKERS_EXCLUDING_MATCHING_FILTER_OPTION = "Create new marker set excluding markers matching filter ( GID filtered)";

	private static final Map<String, Action> actionMap = new HashMap<String, Action>();
	static {
		actionMap.put(REMOVE_MARKERS_OPTION, Action.REMOVE);
		//the following settings are left for the future use, there is only option of REMOVE for now
		actionMap.put(NEW_MARKERS_FROM_MATCHING_FILTER_OPTION, Action.CREATE_FROM_MATCHING);
		actionMap.put(NEW_MARKERS_EXCLUDING_MATCHING_FILTER_OPTION, Action.CREATE_FROM_EXCLUDING);
	}

	private static final String NO_ENTREZ_IDS = "No Entrez ID";
	private static final String MULTIPLE_ENTREZ_IDS_OPTION = "Multiple Entrez IDs";
	private static final String FILTER_ACTION_LABEL_SELECTION = "Filter Action Label";

	private JComboBox filterActionSelectionComboBox = new JComboBox(actionMap.keySet().toArray());

	private JCheckBox noEntrezIDsCheckBox = new JCheckBox(NO_ENTREZ_IDS);
	private boolean noEntrezIDsStatus;

	private JCheckBox multipleEntrezIDsCheckBox = new JCheckBox(
			MULTIPLE_ENTREZ_IDS_OPTION);
	private boolean multipleEntrezIDsStatus;

	public MultipleEntrezGeneIDFilterPanel() {

		noEntrezIDsCheckBox.setSelected(false);
		noEntrezIDsStatus = false;

		multipleEntrezIDsCheckBox.setSelected(false);
		multipleEntrezIDsStatus = false;

		JPanel container = new JPanel(new GridLayout(0, 1));
		container.setPreferredSize(new Dimension(500, 140));

		JLabel filterActionLabel = new JLabel("<html><p>Filter out markers with: </p><p></p></html>");
		container.add(filterActionLabel);
		container.add(noEntrezIDsCheckBox);
		container.add(multipleEntrezIDsCheckBox);
		noEntrezIDsCheckBox.addItemListener(this);
		multipleEntrezIDsCheckBox.addItemListener(this);

		container.setAlignmentX(LEFT_ALIGNMENT);

		JLabel filterOutMarkersLabel = new JLabel("<html><p> </p><p>Filter action: </p></html>");
		container.add(filterOutMarkersLabel);
		filterActionSelectionComboBox.setSelectedIndex(0);	//default is remove
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