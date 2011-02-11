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
 * Parameters panels used by the <code>MissingValueNormalizer</code>.
 * @author unknown, yc2480
 * @version $Id$
 */
public class MissingValueNormalizerPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 3819922726353717412L;
	
	final String MARKER_OPTION = "Mean profile marker";
    final String MICROARRAY_OPTION = "Mean microarray value";
    private JComboBox averagingTypeSelection = new JComboBox(new String[]{MARKER_OPTION, MICROARRAY_OPTION});

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
			if (key.equals("averaging")){
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
		parameters.put("averaging", averagingTypeSelection.getSelectedIndex());
		return parameters;
	}


    /**
     *	for dataset history
     */
	public String getParamDetail() {
		String AveragingMethodSelectionLine = "Averaging method:  "
				+ (String) averagingTypeSelection.getSelectedItem() + "\n";
		String paramDetail = "Missing Value Parameters:\n"
				+ AveragingMethodSelectionLine;

		return paramDetail;
	}

    public MissingValueNormalizerPanel() {
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

        builder.appendSeparator("Missing Value Parameters");

        builder.append("Averaging method", averagingTypeSelection);
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

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}

}

