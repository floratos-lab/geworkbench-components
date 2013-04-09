package org.geworkbench.components.viper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JComboBox;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class ViperPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 8385765241218944314L;
	private static final String NEWLINE = "\n";
	
	private static final String REGULON_HR = "Viper Regulon: ";
	private static final String METHOD_HR = "Viper Method: ";
	private static final String SERVICE_HR = "Viper Service: ";

	private static final String[] REGULONS  = { "hl60_cmap2_tf_regulon", "mcf7_cmap2_tf_regulon", "pc3_cmap2_tf_regulon" };
	private static final String[] REG_TYPES = { "hl60regul", "mcf7regul", "pc3regul" };
	private static final String[] METHODS   = { "none", "scale", "rank", "mad", "ttest" };
    private static final String[] SERVICES	= { "local service", "web service" };

	private JComboBox regulon = new JComboBox(REGULONS);
    private JComboBox method = new JComboBox(METHODS);
    private JComboBox service = new JComboBox(SERVICES);

    /**
     * Default Constructor
     */
    public ViperPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {

        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 100dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("Viper Parameters");

        builder.append("Select service", service);
        builder.append("Select Regulon", regulon);
        builder.append("Select Method", method);
        this.add(builder.getPanel());

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        
        regulon.addActionListener(parameterActionListener);
        method.addActionListener(parameterActionListener);
        service.addActionListener(parameterActionListener);
    }
    
    /**
     * Gets the currently selected viper regulon
     *
     * @return the currently selected viper regulon
     */
    public String getRegulon() {
        return (String)regulon.getSelectedItem();
    }
    
    public String getRegType(){
    	return REG_TYPES[regulon.getSelectedIndex()];
    }

    /**
     * Gets the currently selected viper method
     *
     * @return currently selected viper method
     */
    public String getMethod() {
        return (String)method.getSelectedItem();
    }
    
    public String getService() {
        return (String)service.getSelectedItem();
    }

    /**
     * Validates if the parameters to be passed to the analysis routine are indeed
     * valid
     *
     * @return <code>ParamValidationResults</code> containing results of
     *         validation
     */
    public ParamValidationResults validateParameters() {
        return new ParamValidationResults(true, "Viper Parameter validations passed");
    }

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if (getStopNotifyAnalysisPanelTemporaryFlag() == true || parameters == null)
    		return;
    	stopNotifyAnalysisPanelTemporary(true);
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			String value = (String)parameter.getValue();
			if (key.equals(REGULON_HR)){
				regulon.setSelectedItem(value);
			}
			else if (key.equals(METHOD_HR)){
				method.setSelectedItem(value);
			}
			else if (key.equals(SERVICE_HR)){
				service.setSelectedItem(value);
			}
		}
        stopNotifyAnalysisPanelTemporary(false);
    }
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getDataSetHistory()
	 */
	@Override
	public String getDataSetHistory() {
		/* translate between machine index to human readable text. */
		String histStr = "";
		Map<Serializable, Serializable> pMap = getParameters();
		// Header, could be moved to AbstractAnalysis.java
		histStr += "Viper Analysis parameters:"
				+ NEWLINE;
		histStr += "----------------------------------------"
				+ NEWLINE;
		for (Iterator<Map.Entry<Serializable, Serializable>> iterator = pMap
				.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			histStr += key.toString() + value.toString() + NEWLINE;
		}
		return histStr;
	}
  
	public Map<Serializable, Serializable> getParameters() {
		
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();

		parameters.put(SERVICE_HR, getService());
		parameters.put(REGULON_HR, getRegulon());
		parameters.put(METHOD_HR, getMethod());

		return parameters;
	}
	
	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
}
