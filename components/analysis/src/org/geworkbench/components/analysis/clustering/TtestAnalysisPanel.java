package org.geworkbench.components.analysis.clustering;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

import javax.swing.*;
import java.awt.*;
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

public class TtestAnalysisPanel extends AbstractSaveableParameterPanel {

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
    JRadioButton jRadioButton1 = new JRadioButton();
    GridLayout gridLayout2 = new GridLayout();
    JRadioButton jRadioButton2 = new JRadioButton();
    BorderLayout borderLayout1 = new BorderLayout();
    JRadioButton jRadioButton4 = new JRadioButton();
    FlowLayout flowLayout1 = new FlowLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    JLabel jLabel3 = new JLabel();
    JPanel jPanel7 = new JPanel();
    JRadioButton jRadioButton6 = new JRadioButton();
    JFormattedTextField jTextField2 = new JFormattedTextField(new DecimalFormat("0.###E00"));
    //JFormattedTextField jTextField2 = new JFormattedTextField(NumberFormat.getNumberInstance());
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JRadioButton jRadioButton3 = new JRadioButton();
    JPanel jPanel6 = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JLabel jLabel6 = new JLabel();
    JPanel jPanel2 = new JPanel();
    JRadioButton jRadioButton5 = new JRadioButton();
    JFormattedTextField jTextField1 = new JFormattedTextField(new DecimalFormat());
    JPanel jPanel5 = new JPanel();
    JPanel jPanel11 = new JPanel();
    JPanel jPanel12 = new JPanel();
    GridLayout gridLayout3 = new GridLayout();
    JRadioButton jRadioButton10 = new JRadioButton();
    JPanel jPanel8 = new JPanel();
    JRadioButton jRadioButton7 = new JRadioButton();
    JPanel jPanel3 = new JPanel();
    JPanel jPanel10 = new JPanel();
    JRadioButton jRadioButton9 = new JRadioButton();
    JRadioButton jRadioButton8 = new JRadioButton();
    JPanel jPanel9 = new JPanel();
    BorderLayout borderLayout6 = new BorderLayout();
    JRadioButton jRadioButton11 = new JRadioButton();
    JLabel jLabel5 = new JLabel();
    BorderLayout borderLayout3 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();

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
        if (jTextField2.getValue() instanceof Double)
            return ((Double) jTextField2.getValue()).doubleValue();
        else if (jTextField2.getValue() instanceof Long)
            return ((Long) jTextField2.getValue()).doubleValue();
        else
            return Double.parseDouble(jTextField2.getText());
    }

    public int getSignificanceMethod() {
        if (jRadioButton9.isSelected())
            return JUST_ALPHA;
        else if (jRadioButton8.isSelected())
            return this.STD_BONFERRONI;
        else if (jRadioButton7.isSelected())
            return this.ADJ_BONFERRONI;
        else if (jRadioButton10.isSelected())
            return MAX_T;
        else
            return MIN_P;
    }

    public boolean isPermut() {
        return jRadioButton6.isSelected();
    }

    public boolean useWelchDf() {
        return jRadioButton1.isSelected();
    }

    public int getNumCombs() {
        if (jTextField1.getValue() instanceof Double)
            return ((Double) jTextField1.getValue()).intValue();
        else if (jTextField1.getValue() instanceof Long)
            return ((Long) jTextField1.getValue()).intValue();
        else
            return Integer.parseInt(jTextField1.getText());
    }

    public boolean useAllCombs() {
        return jRadioButton3.isSelected();
    }

    public int[] getGroupAssignments() {
        return new int[1];
    }

    private void configure() {
        group1.add(jRadioButton1);
        group1.add(jRadioButton2);
        jRadioButton1.setSelected(true);

        group2.add(jRadioButton5);
        group2.add(jRadioButton6);
        jRadioButton5.setSelected(true);

        group3.add(jRadioButton3);
        group3.add(jRadioButton4);
        jRadioButton4.setSelected(true);

        group4.add(jRadioButton7);
        group4.add(jRadioButton8);
        group4.add(jRadioButton9);
        group4.add(jRadioButton10);
        group4.add(jRadioButton11);
        jRadioButton9.setSelected(true);
    }

    private void jbInit() throws Exception {
        jLabel5.setText("Step down Westfall and Young Methods (for permutation only)");
        jRadioButton11.setSelected(true);
        jRadioButton11.setText("minP");
        jPanel9.setBorder(BorderFactory.createEtchedBorder());
        jPanel9.setLayout(borderLayout5);
        jRadioButton8.setText("Standard Bonferroni Correction");
        jRadioButton9.setSelected(true);
        jRadioButton9.setText("just alpha (no correction)");
        jPanel3.setLayout(borderLayout6);
        jPanel3.setBorder(BorderFactory.createLineBorder(Color.black));
        jRadioButton7.setText("Adjusted Bonferroni Correction");
        jPanel8.setLayout(gridLayout3);
        jRadioButton10.setText("maxT");
        gridLayout3.setRows(3);
        gridLayout3.setHgap(0);
        gridLayout3.setColumns(1);
        jPanel5.setLayout(borderLayout4);
        jTextField1.setValue(new Long(100));
        jTextField1.setPreferredSize(new Dimension(35, 20));
        jTextField1.setOpaque(true);
        jTextField1.setMinimumSize(new Dimension(35, 20));
        jRadioButton5.setSelected(true);
        jRadioButton5.setText("p-values based on t-distribution");
        jPanel2.setLayout(borderLayout2);
        jPanel2.setBorder(BorderFactory.createLineBorder(Color.black));
        jLabel6.setText("times");
        jPanel6.setLayout(gridBagLayout1);
        jRadioButton3.setText("Use all permutations");
        jTextField2.setValue(new Double(0.01));
        jTextField2.setPreferredSize(new Dimension(35, 20));
        jTextField2.setMinimumSize(new Dimension(35, 20));
        jRadioButton6.setText("p-values based on permutation:");
        jPanel7.setBorder(BorderFactory.createEtchedBorder());
        jPanel7.setLayout(flowLayout1);
        jLabel3.setText("Overall alpha (critical p-value):");
        jLabel3.setHorizontalTextPosition(SwingConstants.LEFT);
        jLabel3.setHorizontalAlignment(SwingConstants.LEFT);
        jRadioButton4.setSelected(true);
        jRadioButton4.setText("Randomly group experiments");
        jRadioButton2.setText("Equal group variances");
        gridLayout2.setRows(2);
        gridLayout2.setHgap(0);
        gridLayout2.setColumns(1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Welch approximation - unequal group variances");
        jPanel4.setLayout(gridLayout2);
        jPanel1.setBorder(BorderFactory.createLineBorder(Color.black));
        jPanel1.setLayout(borderLayout1);
        this.setLayout(borderLayout3);
        this.setMinimumSize(new Dimension(451, 154));
        this.setPreferredSize(new Dimension(451, 154));
        this.add(jTabbedPane1, BorderLayout.CENTER);
        jTabbedPane1.add(jPanel1, "Degree of freedom");
        jPanel1.add(jPanel4, BorderLayout.CENTER);
        jPanel4.add(jRadioButton1, null);
        jPanel4.add(jRadioButton2, null);
        jTabbedPane1.add(jPanel2, "P-Value Parameters");
        jPanel5.add(jRadioButton5, BorderLayout.NORTH);
        jPanel5.add(jRadioButton6, BorderLayout.CENTER);
        jPanel5.add(jPanel11, BorderLayout.SOUTH);
        jPanel11.add(jPanel7, null);
        jPanel7.add(jRadioButton4, null);
        jPanel7.add(jTextField1, null);
        jPanel7.add(jLabel6, null);
        jPanel7.add(jRadioButton3, null);
        jPanel2.add(jPanel6, BorderLayout.SOUTH);
        jPanel2.add(jPanel5, BorderLayout.CENTER);
        jPanel6.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 1, 5, 0), 0, 0));
        jPanel6.add(jTextField2, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 16, 5, 91), 0, 0));
        jTabbedPane1.add(jPanel3, "Alpha Corrections");
        jPanel8.add(jRadioButton9, null);
        jPanel8.add(jRadioButton8, null);
        jPanel8.add(jRadioButton7, null);
        jPanel3.add(jPanel12, BorderLayout.SOUTH);
        jPanel12.add(jPanel9, null);
        jPanel3.add(jPanel8, BorderLayout.CENTER);
        jPanel9.add(jPanel10, BorderLayout.SOUTH);
        jPanel10.add(jRadioButton11, null);
        jPanel10.add(jRadioButton10, null);
        jPanel9.add(jLabel5, BorderLayout.CENTER);
    }
}
