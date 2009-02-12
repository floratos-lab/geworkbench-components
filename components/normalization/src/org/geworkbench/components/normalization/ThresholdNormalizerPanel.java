package org.geworkbench.components.normalization;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;

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
 * @version $ID$
 */
public class ThresholdNormalizerPanel extends AbstractSaveableParameterPanel implements Serializable {
    final String MIN_OPTION = "Minimum";
    final String MAX_OPTION = "Maximum";
    final String IGNORE_OPTION = "Ignore";
    final String REPLACE_OPTION = "Replace";
    private GridLayout gridLayout1 = new GridLayout();
    private JLabel cutoffLabel = new JLabel("Cut-off value");
    private JLabel minMaxLabel = new JLabel("Cut-off type");
    private JLabel missingValuesLabel = new JLabel("Missing values");
    private JFormattedTextField cutoffEdit = new JFormattedTextField();
    private JComboBox cutoffTypeSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION});
    private JComboBox missingValuesSelection = new JComboBox(new String[]{IGNORE_OPTION, REPLACE_OPTION});

    private static class SerializedInstance implements Serializable {

        private Number cutoff;
        private int cutoffType;
        private int missing;

        public SerializedInstance(Number cutoff, int cutoffType, int missing) {
            this.cutoff = cutoff;
            this.cutoffType = cutoffType;
            this.missing = missing;
        }

        Object readResolve() throws ObjectStreamException {
            ThresholdNormalizerPanel panel = new ThresholdNormalizerPanel();
            panel.cutoffEdit.setValue(cutoff);
            panel.cutoffTypeSelection.setSelectedIndex(cutoffType);
            panel.missingValuesSelection.setSelectedIndex(missing);
            return panel;
        }
    }

    public Object writeReplace() throws ObjectStreamException {
        return new SerializedInstance((Number)cutoffEdit.getValue(), cutoffTypeSelection.getSelectedIndex(), missingValuesSelection.getSelectedIndex());
    }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    @Override
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
    @Override
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("cutoffEdit", (Number)cutoffEdit.getValue());
		parameters.put("cutoffTypeSelection", cutoffTypeSelection.getSelectedIndex());
		parameters.put("missingValuesSelection", missingValuesSelection.getSelectedIndex());
		return parameters;
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

    public void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    public void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

}

