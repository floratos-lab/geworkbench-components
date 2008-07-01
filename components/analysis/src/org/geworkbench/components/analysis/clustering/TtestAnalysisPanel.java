package org.geworkbench.components.analysis.clustering;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 1.0
 */

public class TtestAnalysisPanel extends AbstractSaveableParameterPanel implements Serializable {

    public static final int GROUP_A = 1;
    public static final int GROUP_B = 2;
    public static final int NEITHER_GROUP = 3;
    public static final int JUST_ALPHA = 4;
    public static final int STD_BONFERRONI = 5;
    public static final int ADJ_BONFERRONI = 6;
    public static final int BETWEEN_SUBJECTS = 7;
    public static final int ONE_CLASS = 8;
    public static final int MAX_T = 9;
    public static final int MIN_P = 10;
    ButtonGroup group1 = new ButtonGroup();
    ButtonGroup group2 = new ButtonGroup();
    ButtonGroup group3 = new ButtonGroup();
    ButtonGroup group4 = new ButtonGroup();
    ButtonGroup group5 = new ButtonGroup();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    JPanel jPanel1 = new JPanel();
    JPanel jPanel4 = new JPanel();
    JRadioButton welch = new JRadioButton();
    GridLayout gridLayout2 = new GridLayout();
    JRadioButton equalVariances = new JRadioButton();
    BorderLayout borderLayout1 = new BorderLayout();
    JRadioButton randomlyGroup = new JRadioButton();
    FlowLayout flowLayout1 = new FlowLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    JLabel jLabel3 = new JLabel();
    JPanel jPanel7 = new JPanel();
    JRadioButton pvaluesByPerm = new JRadioButton();
    JFormattedTextField alpha = new JFormattedTextField(new DecimalFormat("0.###E00"));
    //JFormattedTextField alpha = new JFormattedTextField(NumberFormat.getNumberInstance());
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JRadioButton allPerms = new JRadioButton();
    JPanel jPanel6 = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JLabel jLabel6 = new JLabel();
    JPanel jPanel2 = new JPanel();
    JRadioButton pvaluesByTDistribution = new JRadioButton();
    JFormattedTextField numCombs = new JFormattedTextField(new DecimalFormat());
    JPanel jPanel5 = new JPanel();
    JPanel jPanel11 = new JPanel();
    JPanel jPanel12 = new JPanel();
    GridLayout gridLayout3 = new GridLayout();
    JRadioButton stepdownMaxT = new JRadioButton();
    JPanel jPanel8 = new JPanel();
    JRadioButton adjustedBonferroni = new JRadioButton();
    JPanel jPanel3 = new JPanel();
    JPanel jPanel10 = new JPanel();
    JRadioButton noCorrection = new JRadioButton();
    JRadioButton bonferroni = new JRadioButton();
    JPanel jPanel9 = new JPanel();
    BorderLayout borderLayout6 = new BorderLayout();
    JRadioButton stepdownMinP = new JRadioButton();
    JLabel jLabel5 = new JLabel();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    
    JCheckBox logCheckbox;
    
    private boolean useroverride = false;
    

    private static class SerialInstance implements Serializable {
        private boolean welch;
        private boolean equalGroup;
        private boolean pvaluesByT;
        private boolean pvaluesByPerm;
        private boolean randomlyGroup;
        private Number groupTimes;
        private boolean useAllPerms;
        private Number alpha;
        private boolean justAlpha;
        private boolean bonferroni;
        private boolean adjustedBonferroni;
        private boolean stepdownMinP;
        private boolean stepdownMaxT;
      

        public SerialInstance(boolean welch, boolean equalGroup, boolean pvaluesByT, boolean pvaluesByPerm, boolean randomlyGroup, Number groupTimes, boolean useAllPerms, Number alpha, boolean justAlpha, boolean bonferroni, boolean adjustedBonferroni, boolean stepdownMinP, boolean stepdownMaxT) {
            this.welch = welch;
            this.equalGroup = equalGroup;
            this.pvaluesByT = pvaluesByT;
            this.pvaluesByPerm = pvaluesByPerm;
            this.randomlyGroup = randomlyGroup;
            this.groupTimes = groupTimes;
            this.useAllPerms = useAllPerms;
            this.alpha = alpha;
            this.justAlpha = justAlpha;
            this.bonferroni = bonferroni;
            this.adjustedBonferroni = adjustedBonferroni;
            this.stepdownMinP = stepdownMinP;
            this.stepdownMaxT = stepdownMaxT;
        }

        Object readResolve() throws ObjectStreamException {
            TtestAnalysisPanel panel = new TtestAnalysisPanel();
            panel.welch.setSelected(welch);
            panel.equalVariances.setSelected(equalGroup);
            panel.pvaluesByTDistribution.setSelected(pvaluesByT);
            panel.pvaluesByPerm.setSelected(pvaluesByPerm);
            panel.randomlyGroup.setSelected(randomlyGroup);
            panel.numCombs.setValue(groupTimes);
            panel.allPerms.setSelected(useAllPerms);
            panel.alpha.setValue(alpha);
            panel.noCorrection.setSelected(justAlpha);
            panel.bonferroni.setSelected(bonferroni);
            panel.adjustedBonferroni.setSelected(adjustedBonferroni);
            panel.stepdownMinP.setSelected(stepdownMinP);
            panel.stepdownMaxT.setSelected(stepdownMaxT);
            return panel;
        }
    }

    Object writeReplace() throws ObjectStreamException {
        return new SerialInstance(
                welch.isSelected(),
                equalVariances.isSelected(),
                pvaluesByTDistribution.isSelected(),
                pvaluesByPerm.isSelected(),
                randomlyGroup.isSelected(),
                (Number) numCombs.getValue(),
                allPerms.isSelected(),
                (Number) alpha.getValue(),
                noCorrection.isSelected(),
                bonferroni.isSelected(),
                adjustedBonferroni.isSelected(),
                stepdownMinP.isSelected(),
                stepdownMaxT.isSelected()
        );
    }

    public TtestAnalysisPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        configure();
    }

    public int getDistanceFunction() {
        return 0;
    }

    public float getDistanceFactor() {
        return 1.0f;
    }

    public boolean isDistanceAbsolute() {
        return false;
    }

    public boolean computeHierarchicalTree() {
        return false;
    }

    public int getLinkageMethod() {
        return 0;
    }

    public boolean calculateGenes() {
        return true;
    }

    public boolean calculateExperiments() {
        return true;
    }

    public float getOneClassMean() {
        return 1.0f;
    }

    public int getTtestDesign() {
        return BETWEEN_SUBJECTS;
    }

    public double getAlpha() {
        if (alpha.getValue() instanceof Double)
            return ((Double) alpha.getValue()).doubleValue();
        else if (alpha.getValue() instanceof Long)
            return ((Long) alpha.getValue()).doubleValue();
        else
            return Double.parseDouble(alpha.getText());
    }

    public void setAlpha(double alphaValue){
        alpha.setValue(alphaValue);
    }

     public void setPValuesDistribution(String value){
       if(value.startsWith("t-dist")){
           pvaluesByTDistribution.setSelected(true);
       }else{
           pvaluesByPerm.setSelected(true);
       }
    }
    public void setSignificanceMethod(String value){
        if(value.startsWith("noCorrection")){
            noCorrection.setSelected(true);
        }
         if(value.startsWith("Bonferroni")){
           bonferroni.setSelected(true);
        }
        if(value.startsWith("adjustedBonferroni")){
           adjustedBonferroni.setSelected(true);
        }
    }
    public void setUseWalch(String methods){
        if(methods.startsWith("Welch")){
            welch.setSelected(true);
        }else{
          equalVariances.setSelected(true);   
        }
    }

    public int getSignificanceMethod() {
        if (noCorrection.isSelected())
            return JUST_ALPHA;
        else if (bonferroni.isSelected())
            return this.STD_BONFERRONI;
        else if (adjustedBonferroni.isSelected())
            return this.ADJ_BONFERRONI;
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
    
    public boolean isUseroverride()
    {
    	return this.useroverride;
    }
    
    public boolean isLogNormalized() {         
    	return logCheckbox.isSelected();
    }
    

    public int[] getGroupAssignments() {
        return new int[1];
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
        jPanel9.setBorder(BorderFactory.createEtchedBorder());
        jPanel9.setLayout(borderLayout5);
        bonferroni.setText("Standard Bonferroni Correction");
        noCorrection.setSelected(true);
        noCorrection.setText("Just alpha (no correction)");
        jPanel3.setLayout(borderLayout6);
//        jPanel3.setBorder(BorderFactory.createLineBorder(Color.black));
        adjustedBonferroni.setText("Adjusted Bonferroni Correction");
        jPanel8.setLayout(gridLayout3);
        stepdownMaxT.setText("maxT");
        gridLayout3.setRows(3);
        gridLayout3.setHgap(0);
        gridLayout3.setColumns(1);
        jPanel5.setLayout(borderLayout4);
        numCombs.setValue(new Long(100));
        numCombs.setPreferredSize(new Dimension(35, 20));
        numCombs.setOpaque(true);
        numCombs.setMinimumSize(new Dimension(35, 20));
        pvaluesByTDistribution.setSelected(true);
        pvaluesByTDistribution.setText("t-distribution");
        jPanel2.setLayout(borderLayout2);
//        jPanel2.setBorder(BorderFactory.createLineBorder(Color.black));
        jLabel6.setText("times");
        jPanel6.setLayout(gridBagLayout1);
        allPerms.setText("Use all permutations");
        alpha.setValue(new Double(0.01));
        alpha.setPreferredSize(new Dimension(35, 20));
        alpha.setMinimumSize(new Dimension(35, 20));
        pvaluesByPerm.setText("permutation:");
        jPanel7.setBorder(BorderFactory.createEtchedBorder());
        jPanel7.setLayout(flowLayout1);
        jLabel3.setText("Overall alpha (critical p-value):");
        jLabel3.setHorizontalTextPosition(SwingConstants.LEFT);
        jLabel3.setHorizontalAlignment(SwingConstants.LEFT);
        randomlyGroup.setSelected(true);
        randomlyGroup.setText("Randomly group experiments");
        equalVariances.setText("Equal");
        gridLayout2.setRows(2);
        gridLayout2.setHgap(0);
        gridLayout2.setColumns(1);
        welch.setSelected(true);
        welch.setText("Unequal (Welch approximation)");
        jPanel4.setLayout(gridLayout2);
//        jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
        jPanel1.setLayout(borderLayout1);
        this.setLayout(borderLayout3);
//        this.setMinimumSize(new Dimension(451, 154));
//        this.setPreferredSize(new Dimension(451, 154));
        this.add(jTabbedPane1, BorderLayout.CENTER);

      
//        jPanel4.add(welch, null);
//        jPanel4.add(equalVariances, null);
        
        logCheckbox = logCheckbox = new JCheckBox("Data is log2-transformed", false);
        logCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	useroverride = true;                              
                 
            }
        });

        // P-Value pane
        jTabbedPane1.add(jPanel2, "P-Value Parameters");
        {
            FormLayout layout = new FormLayout(
                    "right:max(10dlu;pref), 3dlu, pref, 7dlu, "
                  + "right:max(10dlu;pref), 3dlu, pref, 7dlu, "
                  + "right:max(10dlu;pref), 3dlu, pref, 7dlu ",
                    "");
//            layout.setColumnGroups(new int[][]{ {3, 7} });
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();

            builder.appendSeparator("p-Values based on");

            builder.append("", pvaluesByTDistribution);
            builder.nextLine();

            builder.append("", pvaluesByPerm);
            builder.append("", randomlyGroup);
            builder.append("(# times)", numCombs);
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
//            layout.setColumnGroups(new int[][]{ {3, 7} });
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();

            builder.appendSeparator("Overall alpha (critical p-Value)");

            builder.append("", alpha);

            jPanel2.add(builder.getPanel(), BorderLayout.LINE_END);
        }

/*
        jPanel5.add(pvaluesByTDistribution, BorderLayout.NORTH);
        jPanel5.add(pvaluesByPerm, BorderLayout.CENTER);
        jPanel5.add(jPanel11, BorderLayout.SOUTH);
        jPanel11.add(jPanel7, null);
        jPanel7.add(randomlyGroup, null);
        jPanel7.add(numCombs, null);
        jPanel7.add(jLabel6, null);
        jPanel7.add(allPerms, null);
        jPanel2.add(jPanel6, BorderLayout.SOUTH);
        jPanel2.add(jPanel5, BorderLayout.CENTER);
        jPanel6.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 1, 5, 0), 0, 0));
        jPanel6.add(alpha, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 16, 5, 91), 0, 0));
*/

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
//        jPanel1.add(jPanel4, BorderLayout.CENTER);
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

        

/*
        jPanel8.add(noCorrection, null);
        jPanel8.add(bonferroni, null);
        jPanel8.add(adjustedBonferroni, null);
        jPanel3.add(jPanel12, BorderLayout.SOUTH);
        jPanel12.add(jPanel9, null);
        jPanel3.add(jPanel8, BorderLayout.CENTER);
        jPanel9.add(jPanel10, BorderLayout.SOUTH);
        jPanel10.add(stepdownMinP, null);
        jPanel10.add(stepdownMaxT, null);
        jPanel9.add(jLabel5, BorderLayout.CENTER);
*/
    }
}
