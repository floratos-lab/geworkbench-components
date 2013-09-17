package org.geworkbench.components.netboost;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * 
 * @author ch2514
 * @author yc2480
 * @version $Id$
 */
public class NetBoostParamPanel extends AbstractSaveableParameterPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Log log = LogFactory.getLog(NetBoostParamPanel.class);

	private static final String[] methods = { "8 Step Walk", "7 Edges" };

	private static String[] modelNames = null;

	private static String[] modelDescs = null;

	private JTabbedPane tabs;

	// main panel
	private JSpinner traininExSpinner;

	private JSpinner boostingIterSpinner;

	private JComboBox subgraphCountingCombo;

	private JSpinner crossValidSpinner;

	// secondary panel
	private JCheckBox[] modelBoxes;

	private JLabel[] modelLabels;

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    @SuppressWarnings("unchecked")
	public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("trainingEx")){
				this.traininExSpinner.setValue(value);
			}
			if (key.equals("boostingIter")){
				this.boostingIterSpinner.setValue(value);
			}
			if (key.equals("subgraphCounting")){
				this.subgraphCountingCombo.setSelectedItem((String)value);
			}
			if (key.equals("crossValid")){
				this.crossValidSpinner.setValue(value);
			}
			if (key.equals("modelSelections")){
				setSelectedModels(booleanArray2ArrayList((ArrayList<Boolean>)value));
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
		parameters.put("trainingEx", (Number)this.traininExSpinner.getValue());
		parameters.put("boostingIter", (Number)this.boostingIterSpinner.getValue());
		parameters.put("subgraphCounting", (String)this.subgraphCountingCombo.getSelectedItem());
		parameters.put("crossValid", (Number)this.crossValidSpinner.getValue());
		parameters.put("modelSelections", booleanArray2ArrayList(this.getSelectedModels()));
		return parameters;
	}

    private ArrayList<Boolean> booleanArray2ArrayList(boolean[] bArray){
    	ArrayList<Boolean> result = new ArrayList<Boolean>();
    	for (int i = 0; i < bArray.length; i++) {
			result.add(new Boolean(bArray[i]));
		}
    	return result;
    }

    private boolean[] booleanArray2ArrayList(ArrayList<Boolean> bArrayList){
    	boolean[] result = new boolean[bArrayList.size()];
    	for (int i = 0; i < result.length; i++) {
    		result[i]=bArrayList.get(i);
		}
    	return result;
    }

	/**
	 * 
	 * 
	 */
	public NetBoostParamPanel() {
		super();
		try {
			NetBoostParamPanel.modelNames = NetBoostAnalysis.getModelNames();
			NetBoostParamPanel.modelDescs = NetBoostAnalysis.getModelDescriptions();
			init();
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("Cannot initialize NetBoost param panel.", e);
		}
	}

	/**
	 * 
	 * 
	 */
	private void init() {
		tabs = new JTabbedPane();
		tabs.addTab("Main", initMainPanel());
		tabs.addTab("Secondary", initSecondaryPanel());
		this.setLayout(new BorderLayout());
		this.add(tabs, BorderLayout.PAGE_START);
	}

	/**
	 * 
	 * @return
	 */
	private JPanel initMainPanel() {
		JPanel result = new JPanel(new BorderLayout());
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        
		traininExSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 500, 1));
		boostingIterSpinner = new JSpinner(new SpinnerNumberModel(120, 1, 500,
				1));
		subgraphCountingCombo = new JComboBox(methods);
		crossValidSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));

		traininExSpinner.addChangeListener(parameterActionListener);
		boostingIterSpinner.addChangeListener(parameterActionListener);
		subgraphCountingCombo.addActionListener(parameterActionListener);
		crossValidSpinner.addChangeListener(parameterActionListener);

		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 4dlu, 70dlu, 7dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("NetBoost Analysis Main Parameters");

		builder.append("Training Examples", traininExSpinner);
		builder.nextLine();
		builder.append("Boosting Iterations", boostingIterSpinner);
		builder.nextLine();
		builder.append("Subgraph Counting Methods", subgraphCountingCombo);
		builder.nextLine();
		builder.append("Cross-validation Folds", crossValidSpinner);
		builder.nextLine();
		result.add(builder.getPanel());
		return result;
	}

	/**
	 * 
	 * @return
	 */
	private JPanel initSecondaryPanel() {
		JPanel result = new JPanel(new BorderLayout());
		JLabel l = new JLabel("Select Models:");
		l.setFont(new Font("Arial", Font.PLAIN, 12));
		l.setForeground(Color.BLUE);
		JLabel m = new JLabel("Model");
		m.setFont(new Font("Arial", Font.BOLD, 12));
		JLabel d = new JLabel("Description");
		d.setFont(new Font("Arial", Font.BOLD, 12));
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);

		// assumes the number of model names == number of model descriptions
		// should already be checked at the analysis panel level when it reads
		// in the properties file
		modelBoxes = new JCheckBox[modelNames.length];
		modelLabels = new JLabel[modelNames.length];
		int numDescs = 0;
		if (modelDescs != null)
			numDescs = modelDescs.length;
		for (int i = 0; i < modelNames.length; i++) {
			modelBoxes[i] = new JCheckBox(modelNames[i]);
			modelBoxes[i].setSelected(true);
			modelBoxes[i].addActionListener(parameterActionListener);
			
			if (i < numDescs)
				modelLabels[i] = new JLabel(modelDescs[i]);
			else
				modelLabels[i] = new JLabel(" ");
		}

		FormLayout layout = new FormLayout(
				"left:max(100dlu;pref), 4dlu, 150dlu, 150dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		builder.appendSeparator("NetBoost Analysis Secondary Parameters");

		builder.append(l);
		builder.nextLine();
		builder.append(m, d);
		builder.nextLine();

		// assumes the number of model names == number of model descriptions
		// should already be checked at the analysis panel level when it reads
		// in the properties file
		for (int i = 0; i < modelBoxes.length; i++) {
			builder.append(modelBoxes[i], modelLabels[i]);
			builder.nextLine();
		}

		result.add(builder.getPanel());
		return result;
	}

	/**
	 * 
	 * @return
	 */
	public int getTrainingExamples() {
		return new Integer(this.traininExSpinner.getValue().toString())
				.intValue();
	}

	/**
	 * 
	 * @return
	 */
	public int getBoostingIterations() {
		return new Integer(this.boostingIterSpinner.getValue().toString())
				.intValue();
	}

	/**
	 * 
	 * @return
	 */
	public String getSubgraphCountingMethods() {
		return this.subgraphCountingCombo.getSelectedItem().toString();
	}

	/**
	 * 
	 * @return
	 */
	public int getCrossValidationFolds() {
		return new Integer(this.crossValidSpinner.getValue().toString())
				.intValue();
	}

	/**
	 * 
	 * @return
	 */
	boolean[] getSelectedModels() {
		boolean[] result = new boolean[modelNames.length];
		for (int i = 0; i < modelBoxes.length; i++) {
			if (modelBoxes[i].isSelected())
				result[i] = true;
			else
				result[i] = false;
		}
		return result;
	}

	/**
	 * 
	 * @return
	 */
	void setSelectedModels(boolean[] booleans) {
		if (modelBoxes.length == booleans.length) //else, probably version conflict.
		for (int i = 0; i < modelBoxes.length; i++) {
			modelBoxes[i].setSelected(booleans[i]);
		}
	}

	/**
	 * Validates if the parameters to be passed to the analysis routine are
	 * indeed valid
	 * 
	 * @return <code>ParamValidationResults</code> containing results of
	 *         validation
	 */
	public ParamValidationResults validateParameters() {
		if (this.getTrainingExamples() <= 0)
			return new ParamValidationResults(false,
					"Number of training examples has to be greater than 0");
		else if (this.getBoostingIterations() <= 0)
			return new ParamValidationResults(false,
					"Number of boosting interations has to be greater than 0");
		else if (this.getCrossValidationFolds() <= 0)
			return new ParamValidationResults(false,
					"Number of cross validation folds has to be greater than 0");

		return new ParamValidationResults(true,
				"NetBoost Parameter validations passed");
	}

	@Override
	public String getDataSetHistory() {
		StringBuilder histStr = new StringBuilder("");
		histStr.append("NetBoost parameters:\n");
		histStr.append("----------------------------------------\n");
		histStr.append("Training Examples: ");
		histStr.append(getTrainingExamples());
		histStr.append("\nBoosting Iterations: ");
		histStr.append(getBoostingIterations());
		histStr.append("\nSubgraph Counting Methods: ");
		histStr.append(getSubgraphCountingMethods());
		histStr.append("\nCross-Validation Folds: ");
		histStr.append(getCrossValidationFolds());
		histStr.append("\nSelected Models:\n");

		boolean[] selectedModels = getSelectedModels();
		
		if ((modelNames != null) && (selectedModels != null)
				&& (modelNames.length == selectedModels.length)) {
			if ((modelDescs != null)
					&& (modelDescs.length == modelNames.length)) {
				for (int i = 0; i < selectedModels.length; i++) {
					if (selectedModels[i]) {
						histStr.append("\t");
						histStr.append(modelNames[i]);
						histStr.append(" (");
						histStr.append(modelDescs[i]);
						histStr.append(")\n");
					}
				}
			} else {
				for (int i = 0; i < selectedModels.length; i++) {
					if (selectedModels[i]) {
						histStr.append("\t");
						histStr.append(modelNames[i]);
						histStr.append("\n");
					}
				}
			}
			histStr.append("\n");
		} else {
			histStr.append("No model information.");
		}

		return histStr.toString();
	}

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}

}
