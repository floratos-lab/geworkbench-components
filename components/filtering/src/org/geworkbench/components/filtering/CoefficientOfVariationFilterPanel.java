package org.geworkbench.components.filtering;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

public class CoefficientOfVariationFilterPanel extends AbstractSaveableParameterPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7800678939373009896L;

	private static Log log = LogFactory.getLog(CoefficientOfVariationFilterPanel.class);
	
	final String MARKER_OPTION = "Marker average";
    final String MICROARRAY_OPTION = "Microarray average";
    final String IGNORE_OPTION = "Ignore";

    private JLabel deviationLabel = new JLabel("Remove markers for which the Coefficient of Variation is less than");
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

    public CoefficientOfVariationFilterPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        JPanel canvas = new JPanel();
        canvas.setLayout(new BoxLayout(canvas, BoxLayout.LINE_AXIS));
        
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        left.setPreferredSize(new Dimension(375,55));
        
        JPanel middle = new JPanel();
        middle.setLayout(new BoxLayout(middle, BoxLayout.PAGE_AXIS));
        middle.setPreferredSize(new Dimension(200,55));
        
        left.add(deviationLabel);
        left.add(new JLabel("\n"));
        left.add(missingValuesLabel);
        middle.add(deviationCutoff);
        middle.add(missingValuesSelection);
        
        canvas.add(left);
        canvas.add(middle);
        this.add(canvas);
        
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

    public ParamValidationResults validateParameters() {
        if (getDeviationCutoff() < 0)
            return new ParamValidationResults(false, "The coefficient of variation is supposed to be 0 or a positive number.");
        else
            return new ParamValidationResults(true, "No Error");
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDataSetHistory() {
		String histStr = "Coefficient of Variation Filter parameters:\n";
		Map<Serializable, Serializable>parameters = null;
		parameters = getParameters();		
		histStr += "Missing Values: ";
		histStr += parameters.get("missingValues");
		histStr += "\nCoefficient of Variation Bound: ";
		histStr += parameters.get("bound");
		histStr += "\n";
		return histStr;
	}
	

}
