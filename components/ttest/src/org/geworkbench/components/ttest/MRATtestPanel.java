package org.geworkbench.components.ttest;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JRadioButton;

import org.geworkbench.events.listeners.ParameterActionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>Description: T-Test panel for MRA analysis</p>
 * <p>Company: Columbia University</p>
 *
 * @author yc2480
 * @version $Id$
 */

public class MRATtestPanel extends TtestAnalysisPanel {
	private static final long serialVersionUID = 6004988712369589946L;
	
	private static final int JUST_ALPHA = 4;
	private static final int STD_BONFERRONI = 5;
	private static final int ADJ_BONFERRONI = 6;

	private static final int MAX_T = 9;
	private static final int MIN_P = 10;
	private ButtonGroup group1 = new ButtonGroup();
	private ButtonGroup group2 = new ButtonGroup();
	private ButtonGroup group3 = new ButtonGroup();
	private ButtonGroup group4 = new ButtonGroup();
	private JRadioButton welch = new JRadioButton("Unequal (Welch approximation)");
	private JRadioButton equalVariances = new JRadioButton("Equal");
	private JRadioButton randomlyGroup = new JRadioButton("Randomly group experiments");
	private JRadioButton pvaluesByPerm = new JRadioButton("permutation");
	private JFormattedTextField alpha = new JFormattedTextField(new DecimalFormat("0.###E00"));
	private JRadioButton allPerms = new JRadioButton("All permutations");
	private JRadioButton pvaluesByTDistribution = new JRadioButton("t-distribution");
	private JFormattedTextField numCombs = new JFormattedTextField(new DecimalFormat());
	private JRadioButton stepdownMaxT = new JRadioButton("maxT");
	private JRadioButton adjustedBonferroni = new JRadioButton("Adjusted Bonferroni Correction");
	private JRadioButton noCorrection = new JRadioButton("Just alpha (no correction)");
	private JRadioButton bonferroni = new JRadioButton("Standard Bonferroni Correction");
	private JRadioButton stepdownMinP = new JRadioButton("minP");
    
    private ParameterActionListener parameterActionListener = null;
    final static int NUM_COMBS_DEFAULT=100;
    
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

			if (key.equals("welch")){
				welch.setSelected((Boolean)value);
			}
			if (key.equals("equalVariances")){
				equalVariances.setSelected((Boolean)value);
			}
			if (key.equals("pvaluesByTDistribution")){
				pvaluesByTDistribution.setSelected((Boolean)value);
			}
			if (key.equals("pvaluesByPerm")){
				pvaluesByPerm.setSelected((Boolean)value);
			}
			if (key.equals("randomlyGroup")){
				randomlyGroup.setSelected((Boolean)value);
			}
			if (key.equals("numCombs")){
				numCombs.setValue((Number)value);
			}
			if (key.equals("allPerms")){
				allPerms.setSelected((Boolean)value);
			}
			if (key.equals("alpha")){
				alpha.setValue((Number)value);
			}
			if (key.equals("noCorrection")){
				noCorrection.setSelected((Boolean)value);
			}
			if (key.equals("bonferroni")){
				bonferroni.setSelected((Boolean)value);
			}
			if (key.equals("adjustedBonferroni")){
				adjustedBonferroni.setSelected((Boolean)value);
			}
			if (key.equals("stepdownMinP")){
				stepdownMinP.setSelected((Boolean)value);
			}
			if (key.equals("stepdownMaxT")){
				stepdownMaxT.setSelected((Boolean)value);
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

		parameters.put("welch", welch.isSelected());
		parameters.put("equalVariances", equalVariances.isSelected());
		parameters.put("pvaluesByTDistribution", pvaluesByTDistribution.isSelected());
		parameters.put("pvaluesByPerm", pvaluesByPerm.isSelected());
		parameters.put("randomlyGroup", randomlyGroup.isSelected());
		parameters.put("numCombs", (Number) numCombs.getValue());
		parameters.put("allPerms", allPerms.isSelected());
		parameters.put("alpha", (Number) alpha.getValue());
		parameters.put("noCorrection", noCorrection.isSelected());
		parameters.put("bonferroni", bonferroni.isSelected());
		parameters.put("adjustedBonferroni", adjustedBonferroni.isSelected());
		parameters.put("stepdownMinP", stepdownMinP.isSelected());
		parameters.put("stepdownMaxT", stepdownMaxT.isSelected());
		return parameters;
	}

    
    public MRATtestPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        configure();
    }

    @Override
    public double getAlpha() {    	 
        return 1;
    }

    @Override
    public int getSignificanceMethod() {
        if (noCorrection.isSelected())
            return JUST_ALPHA;
        else if (bonferroni.isSelected())
            return STD_BONFERRONI;
        else if (adjustedBonferroni.isSelected())
            return ADJ_BONFERRONI;
        else if (stepdownMaxT.isSelected())
            return MAX_T;
        else
            return MIN_P;
    }

    @Override
    public boolean isPermut() {
        return pvaluesByPerm.isSelected();
    }

    @Override
    public boolean useWelchDf() {
        return welch.isSelected();
    }

    @Override
    public int getNumCombs() {
        if (numCombs.getValue() instanceof Double)
            return ((Double) numCombs.getValue()).intValue();
        else if (numCombs.getValue() instanceof Long)
            return ((Long) numCombs.getValue()).intValue();
        else
            return NUM_COMBS_DEFAULT;
    }

    @Override
    public boolean useAllCombs() {
        return allPerms.isSelected();
    }

    @Override
    public boolean isLogNormalized() {         
    	return true;
    }
    
    private void configure() {
        group1.add(welch);
        group1.add(equalVariances);
        welch.setSelected(true);

        group2.add(pvaluesByTDistribution);
        group2.add(pvaluesByPerm);
        pvaluesByTDistribution.setSelected(true);

        group3.add(allPerms);
        group3.add(randomlyGroup);
        randomlyGroup.setSelected(true);

        group4.add(adjustedBonferroni);
        group4.add(bonferroni);
        group4.add(noCorrection);
        group4.add(stepdownMaxT);
        group4.add(stepdownMinP);
        //stepdownMinP.setSelected(true);
        noCorrection.setSelected(true);
    }

    private void jbInit() throws Exception {
    	FormLayout layout = new FormLayout(
                "left:max(100dlu;pref), 10dlu, 120dlu, 10dlu, "
                + "right:120dlu, 10dlu, 100dlu, 10dlu, 100dlu",
                  "");
    			
    	DefaultFormBuilder builder = new DefaultFormBuilder(layout);
    	builder.setDefaultDialogBorder();
        builder.appendSeparator("P-values based on");
        builder.append(pvaluesByTDistribution);
        builder.nextLine();
        builder.append(pvaluesByPerm);
        builder.append(randomlyGroup);
        numCombs.setText(""+NUM_COMBS_DEFAULT);
        builder.append("(# times)", numCombs);
        builder.nextLine();
        builder.append("",allPerms);
        builder.nextLine();

        builder.appendSeparator("Correction Method");
        builder.append(noCorrection, bonferroni, adjustedBonferroni);
        builder.nextLine();
        
        builder.appendSeparator("Step-down Westfall and Young methods (permutations only)");
        builder.append(stepdownMinP, stepdownMaxT);        
        builder.nextLine();
        
        randomlyGroup.setEnabled(false);
        numCombs.setEnabled(false);
        allPerms.setEnabled(false);
        stepdownMinP.setSelected(true);
        stepdownMinP.setEnabled(false);
        stepdownMaxT.setEnabled(false);
        
        builder.appendSeparator("Group Variances");
        welch.setSelected(true);
        builder.append(welch, equalVariances);
    	this.add(builder.getPanel());
    }
    
    protected void setParamActionListener(ParameterActionListener pal) {
		if (pal != null)
			parameterActionListener = pal;
		else
			parameterActionListener = new ParameterActionListener(this);

		//pass parent ASPP (MasterRegulatorPanel) into ParameterActionListener (instead of MRATtestPanel).
		welch.addActionListener(parameterActionListener);
		equalVariances.addActionListener(parameterActionListener);		
		pvaluesByTDistribution.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				randomlyGroup.setSelected(true);
				randomlyGroup.setEnabled(false);				
		        numCombs.setEnabled(false);
		        allPerms.setSelected(false);
		        allPerms.setEnabled(false);		        
		        stepdownMinP.setSelected(true);
		        stepdownMinP.setEnabled(false);		        
		        stepdownMaxT.setSelected(false);
		        stepdownMaxT.setEnabled(false);		        
			}
		});		
		pvaluesByPerm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				randomlyGroup.setEnabled(true);
		        numCombs.setEnabled(true);
		        numCombs.setText(""+NUM_COMBS_DEFAULT);
		        allPerms.setEnabled(true);
		        stepdownMinP.setEnabled(true);
		        stepdownMaxT.setEnabled(true);
			}
		});
		
		
		randomlyGroup.addActionListener(parameterActionListener);
		numCombs.addActionListener(parameterActionListener);
		allPerms.addActionListener(parameterActionListener);
		alpha.addActionListener(parameterActionListener);
		noCorrection.addActionListener(parameterActionListener);
		bonferroni.addActionListener(parameterActionListener);
		adjustedBonferroni.addActionListener(parameterActionListener);
		stepdownMinP.addActionListener(parameterActionListener);
		stepdownMaxT.addActionListener(parameterActionListener);
    }
}
