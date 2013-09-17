package org.geworkbench.components.normalization;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Parameters panel used by the <code>ThresholdNormalizer</code>.
 * @author unknown, yc2480
 * @version $Id$
 */
public class ThresholdNormalizerPanel extends AbstractSaveableParameterPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1914307168083336201L;
	final String MIN_OPTION = "Minimum";
    final String MAX_OPTION = "Maximum";
    final String IGNORE_OPTION = "Ignore";
    final String REPLACE_OPTION = "Replace";

    private JFormattedTextField cutoffEdit = new JFormattedTextField();
    private JComboBox cutoffTypeSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION});
    private JComboBox missingValuesSelection = new JComboBox(new String[]{IGNORE_OPTION, REPLACE_OPTION});

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
        if (parameters==null){
        	return;
        }
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("cutoffEdit")){
	            this.cutoffEdit.setValue((Number)value);
			}
			if (key.equals("cutoffTypeSelection")){
				this.cutoffTypeSelection.setSelectedIndex((Integer)value);
			}
			if (key.equals("missingValuesSelection")){
				this.missingValuesSelection.setSelectedIndex((Integer)value);
			}
		}
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("cutoffEdit", (Number)cutoffEdit.getValue());
		parameters.put("cutoffTypeSelection", cutoffTypeSelection.getSelectedIndex());
		parameters.put("missingValuesSelection", missingValuesSelection.getSelectedIndex());
		return parameters;
	}

    /**
     *	for dataset history
     */
	public String getParamDetail() {
		String CutOffValueLine = "Cut-off value:  " + cutoffEdit.getValue()
				+ "\n";
		String CutOffTypeLine = "Cut-off type:  "
				+ (String) cutoffTypeSelection.getSelectedItem() + "\n";
		String MissingValuesSelectionLine = "Missing values:  "
				+ (String) missingValuesSelection.getSelectedItem() + "\n";
		String paramDetail = "Threshold Parameters:\n" + CutOffValueLine
				+ CutOffTypeLine + MissingValuesSelectionLine;

		return paramDetail;
	}

    public ThresholdNormalizerPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 8dlu, max(60dlu;pref)",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Threshold Parameters");

        builder.append("Cut-off value", cutoffEdit);
        builder.append("Cut-off type", cutoffTypeSelection);
        builder.append("Missing values", missingValuesSelection);
        this.add(builder.getPanel(), BorderLayout.CENTER);

        cutoffEdit.setValue(new Double(0.0));
        cutoffEdit.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        cutoffEdit.addPropertyChangeListener(parameterActionListener);
		cutoffTypeSelection.addActionListener(parameterActionListener);
		missingValuesSelection.addActionListener(parameterActionListener);
    }

    /**
     * Get the cutoff threashold that will be used to bound the array values.
     *
     * @return The cutoff value.
     */
    public double getCutoffValue() {
        return ((Number) cutoffEdit.getValue()).doubleValue();
    }

    /**
     * The user-specified parameter that designates if the cutoff value is
     * expected to be the minimum or the maximum value for the microarray set
     * values.
     *
     * @return <code>MINIMUM</code> or <code>MAXIMUM</code>.
     */
    public int getCutoffType() {
        if (cutoffTypeSelection.getSelectedItem().equals(MIN_OPTION))
            return ThresholdNormalizer.MINIMUM;
        else
            return ThresholdNormalizer.MAXIMUM;
    }

    /**
     * The user-specified parameter that designates how the missing values
     * should be treated by this normalizer (should they be ignored or set to
     * the prescribed cutoff).
     *
     * @return <code>IGNORE</code> or <code>REPLACE</code>.
     */
    public int getMissingValueTreatment() {
        if (missingValuesSelection.getSelectedItem().equals(IGNORE_OPTION))
            return ThresholdNormalizer.IGNORE;
        else
            return ThresholdNormalizer.REPLACE;
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

}

