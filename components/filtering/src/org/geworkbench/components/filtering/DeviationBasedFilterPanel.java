package org.geworkbench.components.filtering;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Paremeters panel for the <code>DeviationBasedFilter</code>. Promts the
 * user enter a deviation value. All markers whose signal deviation across
 * all arrays is less that this value will be filtered.
 */
public class DeviationBasedFilterPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String MARKER_OPTION = "Marker average";
    final String MICROARRAY_OPTION = "Microarray average";
    final String IGNORE_OPTION = "Ignore";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel deviationLabel = new JLabel("Deviation bound");
    private JLabel missingValuesLabel = new JLabel("Missing values");
    private JFormattedTextField deviationCutoff = new JFormattedTextField();
    private JComboBox missingValuesSelection = new JComboBox(new String[]{MARKER_OPTION, MICROARRAY_OPTION, IGNORE_OPTION});

    private static class SerializedInstance implements Serializable {

        String missingValues;
        Double bound;

        public SerializedInstance(String missingValues, Double bound) {
            this.missingValues = missingValues;
            this.bound = bound;
        }

        Object readResolve() throws ObjectStreamException {
            DeviationBasedFilterPanel panel = new DeviationBasedFilterPanel();
            panel.deviationCutoff.setValue(bound);
            panel.missingValuesSelection.setSelectedItem(missingValues);
            return panel;
        }
    }

    public Object writeReplace()  throws ObjectStreamException {
        return new SerializedInstance((String)missingValuesSelection.getSelectedItem(), (Double) deviationCutoff.getValue());
    }

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
     */
    @Override
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("missingValues", (String)missingValuesSelection.getSelectedItem());
		parameters.put("bound", deviationCutoff.getText());
		return parameters;
	}
    
    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
     */
    @Override
    public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("missingValues")){
				missingValuesSelection.setSelectedItem((String)value);
			}
			if (key.equals("bound")){
				deviationCutoff.setText((String)value);
			}
		}
    }

    public DeviationBasedFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        this.setLayout(new FlowLayout());
        JPanel container = new JPanel();
        gridLayout1.setColumns(2);
        gridLayout1.setHgap(10);
        gridLayout1.setRows(2);
        gridLayout1.setVgap(10);
        container.setLayout(gridLayout1);
        container.add(deviationLabel);
        container.add(deviationCutoff);
        container.add(missingValuesLabel);
        container.add(missingValuesSelection);
        container.setPreferredSize(new Dimension(250, 55));
        this.add(container);
        deviationCutoff.setValue(new Double(0.0));
        deviationCutoff.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        missingValuesSelection.addActionListener(parameterActionListener);
        deviationCutoff.addActionListener(parameterActionListener);
    }

    /**
     * Get the deviation cutoff threashold that will be used for deciding which
     * markers to prune.
     *
     * @return The cutoff value.
     */
    public double getDeviationCutoff() {
        return ((Number) deviationCutoff.getValue()).doubleValue();
    }

    /**
     * The user-specified parameter that designates how the missing values
     * should be treated by this filter (should they be ignored or set to
     * an average value before computing the marker deviations).
     *
     * @return <code>DeviationBasedFilter.MARKER</code>,
     *         <code>DeviationBasedFilter.MICROARRAY</code>, or
     *         <code>DeviationBasedFilter.IGNORE</code>.
     */
    public int getMissingValueTreatment() {
        if (missingValuesSelection.getSelectedItem().equals(MARKER_OPTION))
            return DeviationBasedFilter.MARKER;
        else if (missingValuesSelection.getSelectedItem().equals(MICROARRAY_OPTION))
            return DeviationBasedFilter.MICROARRAY;
        else
            return DeviationBasedFilter.IGNORE;
    }

    /**
     * Overrides the method from <code>AbstractSaveableParameterPanel</code>.
     * Provides an error message if the user-designated deviation bound is a
     * negative number.
     *
     * @return
     */
    public ParamValidationResults validateParameters() {
        if (getDeviationCutoff() < 0)
            return new ParamValidationResults(false, "The deviation cannot be negative.");
        else
            return new ParamValidationResults(true, "No Error");
    }

    public void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}
