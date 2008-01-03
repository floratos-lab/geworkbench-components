package org.geworkbench.components.netboost;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.awt.*;

import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class NetBoostParamPanel extends AbstractSaveableParameterPanel
		implements Serializable {

	static Log log = LogFactory.getLog(NetBoostParamPanel.class);
	private static final String[] methods = {"8 Step Walk", "7 Edges"};
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
	
	private static class SerialInstance implements Serializable {
		private Object trainingEx;
		private Object boostingIter;
		private Object subgraphCounting;
		private Object crossValid;
		private boolean[] modelSelections;
		
		public SerialInstance(Object trainingEx
				, Object boostingIter
				, Object subgraphCounting
				, Object crossValid
				, boolean[] modelSelections
				){
			this.trainingEx = trainingEx;
			this.boostingIter = boostingIter;
			this.crossValid = crossValid;
			this.subgraphCounting = subgraphCounting;
			this.modelSelections = modelSelections;
		}
		
		Object readResolve() throws ObjectStreamException {
			NetBoostParamPanel result = new NetBoostParamPanel();
			result.traininExSpinner.setValue(this.trainingEx);
			result.boostingIterSpinner.setValue(this.boostingIter);
			result.crossValidSpinner.setValue(this.crossValid);
			result.subgraphCountingCombo.setSelectedItem(this.subgraphCounting);
			
			int numBooleans = this.modelSelections.length;
			int numBoxes = result.modelBoxes.length;
			
			if(numBooleans >= numBoxes){
				if(numBooleans > numBoxes)
					log.warn("Number of saved model selections is greater than number of model choices.");
				for(int i = 0; i < numBoxes; i++){
					result.modelBoxes[i].setSelected(this.modelSelections[i]);
				}
			} else if(numBooleans < numBoxes){
				log.warn("Number of saved model selections less than number of model choices.");
			} else {
				log.warn("Number of saved model selections does not match number of model choices.");
				for(int i = 0; i < numBoxes; i++){
					result.modelBoxes[i].setSelected(true);
				}
			}
			return result;
		}
	}
	
	Object writeReplace() throws ObjectStreamException {
		return new SerialInstance(this.traininExSpinner.getValue()
			, this.boostingIterSpinner.getValue()
			, this.subgraphCountingCombo.getSelectedItem()
			, this.crossValidSpinner.getValue()
			, this.getSelectedModels()
			);
	}
	
	public NetBoostParamPanel(){		
		super();
        try {
        	this.modelNames = NetBoostAnalysis.getModelNames();
        	this.modelDescs = NetBoostAnalysis.getModelDescriptions();
            init();
        } catch (Exception e) {
        	e.printStackTrace();
            log.debug("Cannot initialize NetBoost param panel.", e);
        }		
	}
	
	private void init(){        	
        tabs = new JTabbedPane();
		tabs.addTab("Main", initMainPanel());
		tabs.addTab("Secondary", initSecondaryPanel());
		this.setLayout(new BorderLayout());
		this.add(tabs, BorderLayout.PAGE_START);
	}
	
	private JPanel initMainPanel(){
		JPanel result = new JPanel(new BorderLayout());
		traininExSpinner = new JSpinner(new SpinnerNumberModel(100, 1, 500, 1));
		boostingIterSpinner = new JSpinner(new SpinnerNumberModel(120, 1, 500, 1));
		subgraphCountingCombo = new JComboBox(methods);
		crossValidSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
		
		FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 4dlu, 70dlu, 7dlu",
                "");
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
	
	private JPanel initSecondaryPanel(){
		JPanel result = new JPanel(new BorderLayout());
		JLabel l = new JLabel("Select Models:");
		l.setFont(new Font("Arial", Font.PLAIN, 12));
		l.setForeground(Color.BLUE);
		JLabel m = new JLabel("Model");
		m.setFont(new Font("Arial", Font.BOLD, 12));
		JLabel d = new JLabel("Description");
		d.setFont(new Font("Arial", Font.BOLD, 12));
		
		// assumes the number of model names == number of model descriptions
		// should already be checked at the analysis panel level when it reads in the properties file
		modelBoxes = new JCheckBox[modelNames.length];
		modelLabels = new JLabel[modelNames.length];
		int numDescs = 0;
		if(modelDescs != null)
			numDescs = modelDescs.length;
		for(int i = 0; i < modelNames.length; i++){
			modelBoxes[i] = new JCheckBox(modelNames[i]);
			modelBoxes[i].setSelected(true);
			if(i < numDescs)
				modelLabels[i] = new JLabel(modelDescs[i]);
			else
				modelLabels[i] = new JLabel(" ");
		}
		
		
		FormLayout layout = new FormLayout(
                "left:max(100dlu;pref), 4dlu, 150dlu, 150dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        builder.appendSeparator("NetBoost Analysis Secondary Parameters");

        builder.append(l);
        builder.nextLine();
        builder.append(m, d);
        builder.nextLine();
        
        // assumes the number of model names == number of model descriptions
		// should already be checked at the analysis panel level when it reads in the properties file
        for(int i = 0; i < modelBoxes.length; i++){
	        builder.append(modelBoxes[i], modelLabels[i]);
	        builder.nextLine();
        }
        
        result.add(builder.getPanel());
		return result;
	}
	
	public int getTrainingExamples(){
		return new Integer(this.traininExSpinner.getValue().toString()).intValue();
	}
	
	public int getBoostingIterations(){
		return new Integer(this.boostingIterSpinner.getValue().toString()).intValue();
	}
	
	public String getSubgraphCountingMethods(){
		return this.subgraphCountingCombo.getSelectedItem().toString();
	}
	
	public int getCrossValidationFolds(){
		return new Integer(this.crossValidSpinner.getValue().toString()).intValue();
	}
	
	boolean[] getSelectedModels(){
		boolean[] result = new boolean[modelNames.length];
		for(int i = 0; i < modelBoxes.length; i++){
			if(modelBoxes[i].isSelected())
				result[i] = true;
			else 
				result[i] = false;
		}
		return result;
	}
	
	/**
     * Validates if the parameters to be passed to the analysis routine are indeed
     * valid
     *
     * @return <code>ParamValidationResults</code> containing results of
     *         validation
     */
    public ParamValidationResults validateParameters() {
    	if(this.getTrainingExamples() <= 0)
    		return new ParamValidationResults(false, "Number of training examples has to be greater than 0");
    	else if(this.getBoostingIterations() <= 0)
    		return new ParamValidationResults(false, "Number of boosting interations has to be greater than 0");
    	else if(this.getCrossValidationFolds() <= 0)
    		return new ParamValidationResults(false, "Number of cross validation folds has to be greater than 0");
    	
    	return new ParamValidationResults(true, "NetBoost Parameter validations passed");
    }
	
	/**
     * {@link java.io.Serializable} method
     *
     * @param out <code>ObjectOutputStream</code>
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    /**
     * {@link java.io.Serializable} method
     *
     * @param in <code>ObjectInputStream</code>
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    	in.defaultReadObject();
    	revalidate();
    }
	

}
