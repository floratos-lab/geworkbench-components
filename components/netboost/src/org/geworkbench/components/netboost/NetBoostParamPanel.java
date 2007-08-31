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
	
	private JTabbedPane tabs;
	
	// main panel
	private JSpinner traininExSpinner;
	private JSpinner boostingIterSpinner;
	private JComboBox subgraphCountingCombo;
	private JSpinner crossValidSpinner;
	
	// secondary panel
	private JCheckBox lpaBox;
	private JCheckBox rdgBox;
	private JCheckBox rdsBox;
	private JCheckBox dmcBox;
	private JCheckBox agvBox;
	private JCheckBox smwBox;
	private JCheckBox dmrBox;
	
	private static class SerialInstance implements Serializable {
		private Object trainingEx;
		private Object boostingIter;
		private Object subgraphCounting;
		private Object crossValid;
		private Boolean lpa;
		private Boolean rdg;
		private Boolean rds;
		private Boolean dmc;
		private Boolean agv;
		private Boolean smw;
		private Boolean dmr;
		
		public SerialInstance(Object trainingEx
				, Object boostingIter
				, Object subgraphCounting
				, Object crossValid
				, Boolean lpa
				, Boolean rdg
				, Boolean rds
				, Boolean dmc
				, Boolean agv
				, Boolean smw
				, Boolean dmr
				){
			this.trainingEx = trainingEx;
			this.boostingIter = boostingIter;
			this.crossValid = crossValid;
			this.subgraphCounting = subgraphCounting;
			this.lpa = lpa;
			this.rdg = rdg;
			this.rds = rds;
			this.dmc = dmc;
			this.agv = agv;
			this.smw = smw;
			this.dmr = dmr;
		}
		
		Object readResolve() throws ObjectStreamException {
			NetBoostParamPanel result = new NetBoostParamPanel();
			result.traininExSpinner.setValue(this.trainingEx);
			result.boostingIterSpinner.setValue(this.boostingIter);
			result.crossValidSpinner.setValue(this.crossValid);
			result.subgraphCountingCombo.setSelectedItem(this.subgraphCounting);
			result.lpaBox.setSelected(this.lpa.booleanValue());
			result.rdgBox.setSelected(this.rdg.booleanValue());
			result.rdsBox.setSelected(this.rds.booleanValue());
			result.dmcBox.setSelected(this.dmc.booleanValue());
			result.agvBox.setSelected(this.agv.booleanValue());
			result.smwBox.setSelected(this.smw.booleanValue());
			result.dmrBox.setSelected(this.dmr.booleanValue());
			return result;
		}
	}
	
	Object writeReplace() throws ObjectStreamException {
		return new SerialInstance(this.traininExSpinner.getValue()
			, this.boostingIterSpinner.getValue()
			, this.subgraphCountingCombo.getSelectedItem()
			, this.crossValidSpinner.getValue()
			, new Boolean(this.lpaBox.isSelected())
			, new Boolean(this.rdgBox.isSelected())
			, new Boolean(this.rdsBox.isSelected())
			, new Boolean(this.dmcBox.isSelected())
			, new Boolean(this.agvBox.isSelected())
			, new Boolean(this.smwBox.isSelected())
			, new Boolean(this.dmrBox.isSelected())
			);
	}
	
	public NetBoostParamPanel(){		
		super();
        try {
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
		lpaBox = new JCheckBox("LPA");
		rdgBox = new JCheckBox("RDG");
		rdsBox = new JCheckBox("RDS");
		dmcBox = new JCheckBox("DMC");
		agvBox = new JCheckBox("AGV");
		smwBox = new JCheckBox("SMW");
		dmrBox = new JCheckBox("DMR");
		
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
        
        builder.append(lpaBox, new JLabel("Linear Preferential Attachment"));
        builder.nextLine();
        builder.append(rdgBox, new JLabel("Random Growing Networks"));
        builder.nextLine();
        builder.append(rdsBox, new JLabel("Random Static Network"));
        builder.nextLine();
        builder.append(dmcBox, new JLabel("Random Growing Networks"));
        builder.nextLine();
        builder.append(agvBox, new JLabel("Aging Vertex"));
        builder.nextLine();
        builder.append(smwBox, new JLabel("Small World"));
        builder.nextLine();
        builder.append(dmrBox, new JLabel("Random Mutations"));
        builder.nextLine();
        
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
	
	public boolean getLPA(){
		return this.lpaBox.isSelected();
	}
	
	public boolean getRDG(){
		return this.rdgBox.isSelected();
	}
	
	public boolean getRDS(){
		return this.rdsBox.isSelected();
	}
	
	public boolean getDMC(){
		return this.dmcBox.isSelected();
	}
	
	public boolean getAGV(){
		return this.agvBox.isSelected();
	}
	
	public boolean getSMW(){
		return this.smwBox.isSelected();
	}
	
	public boolean getDMR(){
		return this.dmrBox.isSelected();
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
