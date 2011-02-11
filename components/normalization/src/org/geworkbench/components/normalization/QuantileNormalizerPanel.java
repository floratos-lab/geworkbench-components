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
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia University</p>
 * @author non attributable
 */

/**
 * Parameters panels used by the <code>QuantileNormalizer</code>.
 * @author unknown, yc2480
 * @version $Id$
 */
public class QuantileNormalizerPanel extends AbstractSaveableParameterPanel {
    /**
	 *
	 */
	private static final long serialVersionUID = 4689662471445840601L;

	final String MARKER_OPTION = "Mean profile marker";
    final String MICROARRAY_OPTION = "Mean microarray value";
    private JComboBox averagingTypeSelection = new JComboBox(new String[]{MARKER_OPTION, MICROARRAY_OPTION});

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
			if (key.equals("averagingTypeSelection")){
				this.averagingTypeSelection.setSelectedIndex((Integer)value);
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
		parameters.put("averagingTypeSelection", averagingTypeSelection.getSelectedIndex());
		return parameters;
	}

    /**
     *	for dataset history
     */
	public String getParamDetail() {
		String MissingValuesAveragingMethodSelectionLine = "Missing values averaging method:  "
				+ (String) averagingTypeSelection.getSelectedItem() + "\n";
		String paramDetail = "Quantile Parameters:\n"
				+ MissingValuesAveragingMethodSelectionLine;

		return paramDetail;
	}

    public QuantileNormalizerPanel() {
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

        builder.appendSeparator("Quantile Parameters");

        builder.append("Missing values averaging method", averagingTypeSelection);
        this.add(builder.getPanel(), BorderLayout.CENTER);
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        averagingTypeSelection.addActionListener(parameterActionListener);
    }

    /**
     * Return the user-specified parameter that designates if a missing value for
     * a marker X within a microarray Y will be replaced by the mean value of the
     * marker X across all micorarrays in the set, or with the mean value of all
     * markers within Y.
     *
     * @return <code>MARKER_PROFILE_MEAN</code> or <code>MICROARRAY_MEAN</code>.
     */
    public int getAveragingType() {
        if (averagingTypeSelection.getSelectedItem().equals(MARKER_OPTION))
            return MissingValueNormalizer.MARKER_PROFILE_MEAN;
        else
            return MissingValueNormalizer.MICROARRAY_MEAN;
    }

    public void setAveragingType(String type){
        if(type==null){
            return;
        }
        if(type.equalsIgnoreCase(MARKER_OPTION)){
         averagingTypeSelection.setSelectedItem(MARKER_OPTION);
        }
        if(type.equalsIgnoreCase(MICROARRAY_OPTION)){
         averagingTypeSelection.setSelectedItem(MICROARRAY_OPTION);
        }
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	};

}

