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
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Parameters panel for the <code>MarkerMeanVarianceNormalizer</code>..
 * @author unknown, yc2480
 * @version $Id$
 */
public class MarkerMeanVarianceNormalizerPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 2566041410176133609L;
	
	final String MIN_OPTION = "Min profile";
    final String MAX_OPTION = "Max profile";
    final String ZERO_OPTION = "Zero";
    final String IGNORE_OPTION = "Ignore";
    /**
     * Available options for computing the center to be
     */
    private JComboBox missingValuesSelection = new JComboBox(new String[]{MIN_OPTION, MAX_OPTION, ZERO_OPTION, IGNORE_OPTION});

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("missing")){
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
		parameters.put("missing", missingValuesSelection.getSelectedIndex());
		return parameters;
	}

    /**
     *	used for history
     */
	public String getParamDetail() {
		String missingValuesSelectionLine = "Missing values:  "
				+ (String) missingValuesSelection.getSelectedItem() + "\n";
		String paramDetail = "Mean-Variance Parameters:\n"
				+ missingValuesSelectionLine;

		return paramDetail;
	}

    public MarkerMeanVarianceNormalizerPanel() {
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

        builder.appendSeparator("Mean-Variance Parameters");

        builder.append("Missing values", missingValuesSelection);
        this.add(builder.getPanel(), BorderLayout.CENTER);
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        missingValuesSelection.addActionListener(parameterActionListener);
    }

    /**
     * The user-specified parameter that designates how the missing values
     * should be treated by this normalizer (should they be ignored or set to
     * the prescribed value).
     *
     * @return <code>MarkerMeanVarianceNormalizer.MINIMUM</code>,
     *         <code>MarkerMeanVarianceNormalizer.MAXIMUM</code>,
     *         <code>MarkerMeanVarianceNormalizer.ZERO</code> or
     *         <code>MarkerMeanVarianceNormalizer.IGNORE</code>.
     */
    public int getMissingValueTreatment() {
        if (missingValuesSelection.getSelectedItem().equals(MIN_OPTION))
            return MarkerMeanVarianceNormalizer.MINIMUM;
        else if (missingValuesSelection.getSelectedItem().equals(MAX_OPTION))
            return MarkerMeanVarianceNormalizer.MAXIMUM;
        else if (missingValuesSelection.getSelectedItem().equals(ZERO_OPTION))
            return MarkerMeanVarianceNormalizer.ZERO;
        else
            return MarkerMeanVarianceNormalizer.IGNORE;
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

}

