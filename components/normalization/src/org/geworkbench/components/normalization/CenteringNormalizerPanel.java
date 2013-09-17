package org.geworkbench.components.normalization;

import java.awt.BorderLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Parameters panel for the <code>MarkerCenteringNormalizer</code> and <code>MicroarrayCenteringNormalizer</code>.
 * 
 * @version $Id$
 */
public class CenteringNormalizerPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 6613986306658012899L;

	// Static fields used to designate the available user option within the
    // normalizer's parameters panel.
    public static final int MEAN = 0;
    public static final int MEDIAN = 1;
    public static final int MINIMUM = 0;
    public static final int MAXIMUM = 1;
    public static final int ZERO = 2;
    public static final int IGNORE = 3;

	final String MEAN_OPTION = "Mean";
    final String MEDIAN_OPTION = "Median";
    final String MIN_OPTION = "Min profile";
    final String MAX_OPTION = "Max profile";
    final String ZERO_OPTION = "Zero";
    final String IGNORE_OPTION = "Ignore";

    /**
     * Available options for computing the center to be
     */
    private JComboBox averagingSelection = new JComboBox(new String[]{MEAN_OPTION, MEDIAN_OPTION});
    private JComboBox missingValuesSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION, ZERO_OPTION, IGNORE_OPTION});

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
     */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("averaging", averagingSelection.getSelectedIndex());
		parameters.put("missing", missingValuesSelection.getSelectedIndex());
		return parameters;
	}

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
     */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if (parameters == null) return;
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("averaging")){
				this.averagingSelection.setSelectedIndex((Integer)value);
			}
			if (key.equals("missing")){
				this.missingValuesSelection.setSelectedIndex((Integer)value);
			}
		}
    }

    public CenteringNormalizerPanel() {
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

        builder.appendSeparator("Centering Parameters");

        builder.append("Averaging method", averagingSelection);
        builder.append("Missing values", missingValuesSelection);

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        averagingSelection.addActionListener(parameterActionListener);
        missingValuesSelection.addActionListener(parameterActionListener);

        this.add(builder.getPanel(), BorderLayout.CENTER);
    }

    /**
     * Gets the user-specified preference for the averaging method to use (mean
     * or median).
     *
     * @return <code>CenteringNormalizer.MEAN</code> or
     *         <code>CenteringNormalizer.MEDIAN</code>.
     */
    public int getAveragingSelection() {
        if (averagingSelection.getSelectedItem().equals(MEAN_OPTION))
            return MEAN;
        else
            return MEDIAN;
    }

    /**
     * The user-specified parameter that designates how the missing values
     * should be treated by this normalizer (should they be ignored or set to
     * the prescribed value).
     *
     * @return <code>MarkerCenteringNormalizer.MINIMUM</code>,
     *         <code>MarkerCenteringNormalizer.MAXIMUM</code>,
     *         <code>MarkerCenteringNormalizer.ZERO</code> or
     *         <code>MarkerCenteringNormalizer.IGNORE</code>.
     */
    public int getMissingValueTreatment() {
        if (missingValuesSelection.getSelectedItem().equals(MIN_OPTION))
            return MINIMUM;
        else if (missingValuesSelection.getSelectedItem().equals(MAX_OPTION))
            return MAXIMUM;
        else if (missingValuesSelection.getSelectedItem().equals(ZERO_OPTION))
            return ZERO;
        else
            return IGNORE;
    }

    /**
     *	used for history
     */
    public String getParamDetail() {
    	String averagingSelectionLine = "Averaging method:  " + (String) averagingSelection.getSelectedItem() +"\n";
    	String missingValuesSelectionLine = "Missing values:  " + (String) missingValuesSelection.getSelectedItem() +"\n";
        String paramDetail = "Centering Parameters:\n" + averagingSelectionLine + missingValuesSelectionLine;

        return paramDetail ;
     }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

}

