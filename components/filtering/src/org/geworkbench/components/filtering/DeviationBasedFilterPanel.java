package org.geworkbench.components.filtering;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version 1.0
 */

/**
 * Parameters panel for the <code>DeviationBasedFilter</code>. Prompts the
 * user enter a deviation value. All markers whose signal deviation across
 * all arrays is less that this value will be filtered.
 */
public class DeviationBasedFilterPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = -4547048958101290068L;
	private static Log log = LogFactory.getLog(DeviationBasedFilterPanel.class);
	
	final String MARKER_OPTION = "Marker average";
    final String MICROARRAY_OPTION = "Microarray average";
    final String IGNORE_OPTION = "Ignore";

    private GridLayout gridLayout1 = new GridLayout();
    private JLabel deviationLabel = new JLabel(" Std. Deviation bound");
    private JLabel missingValuesLabel = new JLabel("Missing values");
    private JTextField deviationCutoff = new JTextField();
    private JComboBox missingValuesSelection = new JComboBox(new String[]{MARKER_OPTION, MICROARRAY_OPTION, IGNORE_OPTION});
    private ParameterActionListener parameterActionListener = null;

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
     */
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
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if ((getStopNotifyAnalysisPanelTemporaryFlag()==true)&&(parameterActionListener.getCalledFromProgramFlag()==true)) return;
    	stopNotifyAnalysisPanelTemporary(true);
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
        stopNotifyAnalysisPanelTemporary(false);
    }

    public DeviationBasedFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
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
        deviationCutoff.setText("0.0");       
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        missingValuesSelection.addActionListener(parameterActionListener);
        deviationCutoff.addActionListener(parameterActionListener);
    }

    /**
     * Get the deviation cutoff threshold that will be used for deciding which
     * markers to prune.
     *
     * @return The cutoff value.
     */
    public double getDeviationCutoff() {
    	try {
			return (new Double(deviationCutoff.getText()));
		} catch (Exception ex) {
			 
			return new Double(-1);
		}
    
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
        else if (missingValuesSelection.getSelectedItem().equals(IGNORE_OPTION))
            return DeviationBasedFilter.IGNORE;
        else {
        	// also return ignore option, but not intended
        	log.error("unexcepted option of missing value treatment");
        	return DeviationBasedFilter.IGNORE;
        }
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
            return new ParamValidationResults(false, "The deviation is supposed to be 0 or a positive number");
        else
            return new ParamValidationResults(true, "No Error");
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDataSetHistory() {
		String histStr = "Deviation Based Filter parameters:\n";
		Map<Serializable, Serializable>parameters = null;
		parameters = getParameters();		
		histStr += "Missing Values: ";
		histStr += parameters.get("missingValues");
		histStr += "\nStd. Deviation Bound: ";
		histStr += parameters.get("bound");
		histStr += "\n";
		return histStr;
	}
	
}
