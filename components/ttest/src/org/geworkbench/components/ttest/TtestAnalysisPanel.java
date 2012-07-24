package org.geworkbench.components.ttest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.bison.model.analysis.ParameterPanelIncludingNormalized;
import org.geworkbench.events.listeners.ParameterActionListener;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @author yc2480
 * @version $Id$
 */

public class TtestAnalysisPanel extends AbstractSaveableParameterPanel implements ParameterPanelIncludingNormalized {
	private static final long serialVersionUID = 2626991982723835444L;
	
	// significance method
	static final int JUST_ALPHA = 4;
	static final int STD_BONFERRONI = 5;
	static final int ADJ_BONFERRONI = 6;
	static final int MAX_T = 9;
	static final int MIN_P = 10;
	
    private ButtonGroup group1 = new ButtonGroup();
    private ButtonGroup group2 = new ButtonGroup();
    private ButtonGroup group3 = new ButtonGroup();
    private ButtonGroup group4 = new ButtonGroup();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel4 = new JPanel();
    private JRadioButton welch = new JRadioButton();
    private GridLayout gridLayout2 = new GridLayout();
    private JRadioButton equalVariances = new JRadioButton();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JRadioButton randomlyGroup = new JRadioButton();
    private FlowLayout flowLayout1 = new FlowLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private JLabel jLabel3 = new JLabel();
    private JPanel jPanel7 = new JPanel();
    private JRadioButton pvaluesByPerm = new JRadioButton();
    private JFormattedTextField alpha = new JFormattedTextField(new DecimalFormat("0.###E00"));
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JRadioButton allPerms = new JRadioButton();
    private JPanel jPanel6 = new JPanel();
    private BorderLayout borderLayout4 = new BorderLayout();
    private JLabel jLabel6 = new JLabel();
    private JPanel jPanel2 = new JPanel();
    private JRadioButton pvaluesByTDistribution = new JRadioButton();
    private JFormattedTextField numCombs = new JFormattedTextField(new DecimalFormat());
    private JPanel jPanel5 = new JPanel();
    private GridLayout gridLayout3 = new GridLayout();
    private JRadioButton stepdownMaxT = new JRadioButton();
    private JPanel jPanel8 = new JPanel();
    private JRadioButton adjustedBonferroni = new JRadioButton();
    private JPanel jPanel3 = new JPanel();
    private JRadioButton noCorrection = new JRadioButton();
    private JRadioButton bonferroni = new JRadioButton();
    private JPanel jPanel9 = new JPanel();
    private BorderLayout borderLayout6 = new BorderLayout();
    private JRadioButton stepdownMinP = new JRadioButton();
    private JLabel jLabel5 = new JLabel();
    private BorderLayout borderLayout3 = new BorderLayout();
    private BorderLayout borderLayout5 = new BorderLayout();
    private JLabel numCombsLabel = new JLabel();
    
    private JCheckBox logCheckbox;
    
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
				if ((Boolean)value)
					pvaluesByTDistribution_actionPerformed();
			}
			if (key.equals("pvaluesByPerm")){
				pvaluesByPerm.setSelected((Boolean)value);
				if ((Boolean)value)
					pvaluesByPerm_actionPerformed();
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
			if (key.equals("isLog2Transformed"))
			{
				logCheckbox.setSelected((Boolean)value);
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
		parameters.put("isLog2Transformed", isLogNormalized());
		parameters.put("alpha", (Number) alpha.getValue());
		parameters.put("noCorrection", noCorrection.isSelected());
		parameters.put("bonferroni", bonferroni.isSelected());
		parameters.put("adjustedBonferroni", adjustedBonferroni.isSelected());
		parameters.put("stepdownMinP", stepdownMinP.isSelected());
		parameters.put("stepdownMaxT", stepdownMaxT.isSelected());
		return parameters;
	}

    
    public TtestAnalysisPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        configure();
    }

    public double getAlpha() {
        if (alpha.getValue() instanceof Double)
            return ((Double) alpha.getValue()).doubleValue();
        else if (alpha.getValue() instanceof Long)
            return ((Long) alpha.getValue()).doubleValue();
        else
            return Double.parseDouble(alpha.getText());
    }

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

    public boolean isPermut() {
        return pvaluesByPerm.isSelected();
    }

    public boolean useWelchDf() {
        return welch.isSelected();
    }

    public int getNumCombs() {
        if (numCombs.getValue() instanceof Double)
            return ((Double) numCombs.getValue()).intValue();
        else if (numCombs.getValue() instanceof Long)
            return ((Long) numCombs.getValue()).intValue();
        else
            return Integer.parseInt(numCombs.getText());
    }

    public boolean useAllCombs() {
        return allPerms.isSelected();
    }
    
    public boolean isLogNormalized() {         
    	return logCheckbox.isSelected();
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
        noCorrection.setSelected(true);
    }

    private void jbInit() throws Exception {
        jLabel5.setText("Step down Westfall and Young Methods (for permutation only)");        
        stepdownMinP.setSelected(true);
        stepdownMinP.setText("minP");
        stepdownMinP.setEnabled(false);
        jPanel9.setBorder(BorderFactory.createEtchedBorder());
        jPanel9.setLayout(borderLayout5);
        bonferroni.setText("Standard Bonferroni Correction");
        noCorrection.setSelected(true);
        noCorrection.setText("Just alpha (no correction)");
        jPanel3.setLayout(borderLayout6);
        adjustedBonferroni.setText("Adjusted Bonferroni Correction");
        jPanel8.setLayout(gridLayout3);
        stepdownMaxT.setText("maxT");
        stepdownMaxT.setEnabled(false);
        gridLayout3.setRows(3);
        gridLayout3.setHgap(0);
        gridLayout3.setColumns(1);
        jPanel5.setLayout(borderLayout4);
        numCombs.setValue(new Long(100));
        numCombs.setOpaque(true);
        numCombs.setMinimumSize(new Dimension(35, 20));
        numCombs.setEnabled(false);
        numCombsLabel.setText("(# times)");
        numCombsLabel.setEnabled(false);
        pvaluesByTDistribution.setSelected(true);
        pvaluesByTDistribution.setText("t-distribution");
        jPanel2.setLayout(borderLayout2);
        jLabel6.setText("times");
        jPanel6.setLayout(gridBagLayout1);
        allPerms.setText("Use all permutations");
        allPerms.setEnabled(false);
        alpha.setValue(new Double(0.01));
        alpha.setMinimumSize(new Dimension(35, 20));
        alpha.setInputVerifier(new InputVerifier() {
            public boolean verify(JComponent input) {
              if (!(input instanceof JFormattedTextField))
                return true;
              return ((JFormattedTextField) input).isEditValid();
            }
          });
        
        pvaluesByPerm.setText("permutation:");
        jPanel7.setBorder(BorderFactory.createEtchedBorder());
        jPanel7.setLayout(flowLayout1);
        jLabel3.setText("Overall alpha (critical p-value):");
        jLabel3.setHorizontalTextPosition(SwingConstants.LEFT);
        jLabel3.setHorizontalAlignment(SwingConstants.LEFT);
        randomlyGroup.setSelected(true);
        randomlyGroup.setText("Randomly group experiments");        
        randomlyGroup.setEnabled(false);
        equalVariances.setText("Equal");
        gridLayout2.setRows(2);
        gridLayout2.setHgap(0);
        gridLayout2.setColumns(1);
        welch.setSelected(true);
        welch.setText("Unequal (Welch approximation)");
        jPanel4.setLayout(gridLayout2);
        jPanel1.setLayout(borderLayout1);
        this.setLayout(borderLayout3);
        this.add(jTabbedPane1, BorderLayout.CENTER);

        logCheckbox = new JCheckBox("Data is log2-transformed", false);

        // P-Value pane
        jTabbedPane1.add(jPanel2, "P-Value Parameters");
        {
            FormLayout layout = new FormLayout(
                    "right:max(10dlu;pref), 3dlu, pref, 7dlu, "
                  + "right:max(10dlu;pref), 3dlu, pref, 7dlu, "
                  + "right:max(10dlu;pref), 3dlu, pref, 7dlu ",
                    "");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();

            builder.appendSeparator("p-Values based on");

            builder.append("", pvaluesByTDistribution);
            builder.nextLine();

            builder.append("", pvaluesByPerm);
            builder.append("", randomlyGroup);
            builder.append(numCombsLabel, numCombs);
            builder.nextLine();

            builder.append("", new JLabel(""));
            builder.append("", allPerms);
            builder.nextLine();
            builder.append("", logCheckbox);
            jPanel2.add(builder.getPanel(), BorderLayout.CENTER);
        }

        {
            FormLayout layout = new FormLayout(
                    "right:max(40dlu;pref), 3dlu, 40dlu, 7dlu, "
                  + "right:max(10dlu;pref), 3dlu, pref, 7dlu, "
                  + "right:max(10dlu;pref), 3dlu, pref, 7dlu ",
                    "");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();

            builder.appendSeparator("Overall alpha (critical p-Value)");

            builder.append("", alpha);

            jPanel2.add(builder.getPanel(), BorderLayout.LINE_END);
        }

        // Alpha corrections pane
        jTabbedPane1.add(jPanel3, "Alpha Corrections");

        {
            FormLayout layout = new FormLayout(
                    "right:max(40dlu;pref), 3dlu, pref, 7dlu, "
                  + "right:1dlu, 3dlu, pref, 7dlu, "
                  + "right:1dlu, 3dlu, pref, 7dlu ",
                    "");
            layout.setColumnGroups(new int[][]{ {3, 7, 11} });
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();

            builder.appendSeparator("Correction Method");

            builder.append("", noCorrection);
            builder.append("", bonferroni);
            builder.append("", adjustedBonferroni);

            builder.appendSeparator("Step down Westfall and Young Methods (for permutation only)");

            builder.append("", stepdownMinP);
            builder.appendGlueColumn();
            builder.append("", stepdownMaxT);

            jPanel3.add(builder.getPanel(), BorderLayout.CENTER);

        }
        
        
        // Degree of freedom pane
        jTabbedPane1.add(jPanel1, "Degree of freedom");
        {
            FormLayout layout = new FormLayout(
                    "right:max(40dlu;pref), 3dlu, pref",
                    "");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();

            builder.appendSeparator("Group Variances");

            builder.append("", welch);
            builder.append("", equalVariances);
            jPanel1.add(builder.getPanel(), BorderLayout.CENTER);

        }
        
       ParameterActionListener parameterActionListener = new ParameterActionListener(this);
       welch.addActionListener(parameterActionListener);
       equalVariances.addActionListener(parameterActionListener);
       pvaluesByTDistribution.addActionListener(new pvaluesByTDistribution_actionAdapter());
       pvaluesByPerm.addActionListener(new
               pvaluesByPerm_actionAdapter());
       
       logCheckbox.addActionListener(parameterActionListener);
       
       randomlyGroup.addActionListener(new randomlyGroup_actionAdapter());
       numCombs.addActionListener(parameterActionListener);
       allPerms.addActionListener(new allPerms_actionAdapter());
       alpha.addActionListener(parameterActionListener);
       noCorrection.addActionListener(parameterActionListener);
       bonferroni.addActionListener(parameterActionListener);
       adjustedBonferroni.addActionListener(parameterActionListener);
       stepdownMinP.addActionListener(parameterActionListener);
       stepdownMaxT.addActionListener(parameterActionListener);
    }
    
    private class pvaluesByTDistribution_actionAdapter implements
		java.awt.event.ActionListener {
			public void actionPerformed(ActionEvent e) {
				pvaluesByTDistribution_actionPerformed();
		}
	}
	
	private void pvaluesByTDistribution_actionPerformed() {
			allPerms.setEnabled(false);
			randomlyGroup.setEnabled(false);
			numCombs.setEnabled(false);
			numCombsLabel.setEnabled(false);
			jLabel5.setVisible(false);
			stepdownMinP.setSelected(false);
			stepdownMinP.setEnabled(false);
			stepdownMaxT.setSelected(false);
			stepdownMaxT.setEnabled(false);
	}
    
    private class pvaluesByPerm_actionAdapter implements
		java.awt.event.ActionListener {
			public void actionPerformed(ActionEvent e) {
				pvaluesByPerm_actionPerformed();
			}
    }
    
    private void pvaluesByPerm_actionPerformed() {
    	allPerms.setEnabled(true);
    	randomlyGroup.setEnabled(true);
    	numCombs.setEnabled(true);
    	numCombsLabel.setEnabled(true);    	
    	jLabel5.setVisible(true);
    	stepdownMinP.setEnabled(true);
    	stepdownMaxT.setEnabled(true);
    }
    
    private class allPerms_actionAdapter implements
    	java.awt.event.ActionListener {
			public void actionPerformed(ActionEvent e) {
				allPerms_actionPerformed();
			}
    }
    private void allPerms_actionPerformed() {
    	numCombs.setEditable(false);
    }
    
    private class randomlyGroup_actionAdapter implements
    	java.awt.event.ActionListener {
			public void actionPerformed(ActionEvent e) {
				ramdomlyGroup_actionPerformed();
			}
	}
	private void ramdomlyGroup_actionPerformed() {
		numCombs.setEditable(true);
	}
    
	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public ParamValidationResults validateParameters() {
		
		if(alpha.getInputVerifier().verify(alpha) == false) {
			alpha.requestFocus();
			return new ParamValidationResults(false,
					"Please enter valid Critical P-Value.");
		}
		
		if (alpha.getValue() instanceof Double) {
            if(((Double) alpha.getValue()).doubleValue() < 0 || ((Double) alpha.getValue()).doubleValue() > 1) { 
            		alpha.requestFocus();
					return new ParamValidationResults(false,
							"Please enter valid Critical P-Value.");
			}
		} else if (alpha.getValue() instanceof Long) {
			if(((Long) alpha.getValue()).doubleValue() < 0 || ((Long) alpha.getValue()).doubleValue() > 1) { 
        		alpha.requestFocus();
				return new ParamValidationResults(false,
						"Please enter valid Critical P-Value.");
			}
		} 
		
		return new ParamValidationResults(true, null);
	}
	
}
