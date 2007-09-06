package org.geworkbench.components.alignment.panels;

import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.client.BlastAlgorithm;
import org.geworkbench.components.alignment.client.HMMDataSet;
import org.geworkbench.components.alignment.grid.CreateGridServiceDialog;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ParameterViewWidget extends JPanel {
    JCheckBox pfpFilterBox = new JCheckBox();
    JPanel jBasicPane = new JPanel();
    JLabel DatabaseLabel = new JLabel();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    ServerInfoPanel jServerInfoPane = new ServerInfoPanel();
//    BlastGridServiceDataPanel sgePanel = new
//          BlastGridServiceDataPanel();
    CreateGridServicePanel sgePanel = new CreateGridServicePanel();
    JComboBox jMatrixBox = new JComboBox();
    JCheckBox lowComplexFilterBox = new JCheckBox();
    JLabel programLabel = new JLabel();
    JPanel jAdvancedPane = new JPanel();
    JFileChooser jFileChooser1 = new JFileChooser();

    /*String[] databaseParameter = {
        "ncbi/nr",
        "ncbi/pdbaa",
        "ncbi/swissprot",
        "ncbi/yeast.aa",
        "ncbi/nt1",
        "ncbi/pdbnt",
        "ncbi/yeast.nt"};
     */
    String[] databaseParameter = {
                                 "ncbi/nr                      Peptides of all non-redundant sequences.",
                                 "ncbi/pdbaa               Peptides Sequences derived from the PDB.",
                                 "ncbi/swissprot      SWISS-PROT protein sequence database.",
                                 "ncbi/yeast.aa            Yeast  genomic CDS translations.",
                                 "ncbi/nt                    All Non-redundant  DNA equences.",
                                 "ncbi/pdbnt                Nucleotide sequences derived from the PDB.",
                                 "ncbi/yeast.nt           Yeast genomic nucleotide sequences."};

    String[] programParameter = {
                                "blastp", "blastn", "blastx", "tblastn",
                                "tblastx"};
    String[] algorithmParameter = {
                                  "Smith-Waterman DNA",
                                  "Smith-Waterman Protein",
                                  "Frame (for DNA query to protein DB)",
                                  "Frame (for protein query to DNA DB)",
                                  //   "Double Frame (for DNA sequence to DNA DB)"
    };
    String[] hmmParameter = {
                            "Pfam global alignment only",
                            "Pfam local alignment only",
                            "Pfam global and local alignments"
    };

    //JList jDBList = new JList(databaseParameter);
    JList jDBList = new JList();
    JButton blastButton = new JButton();
    JScrollPane jScrollPane1 = new JScrollPane();
    JComboBox jProgramBox = new JComboBox();
    JPanel filterPanel = new JPanel();
    JCheckBox maskLookupOnlyBox = new JCheckBox();
    JLabel expectLabel = new JLabel();
    JComboBox jExpectBox = new JComboBox();
    JLabel matrixLabel = new JLabel();
    JPanel blastxSettingPanel = new JPanel();
    JComboBox jqueryGenericCodeBox = new JComboBox();
    JLabel jFrameShiftLabel = new JLabel();
    JComboBox jFrameShiftPaneltyBox = new JComboBox();
    ParameterSetter parameterSetter = new ParameterSetter();
    CSSequenceSet fastaFile;
    private BlastAppComponent blastAppComponent = null;
    JPanel jPanel3 = new JPanel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel progressBarPanel = new JPanel();
    JPanel subSeqPanel = new JPanel();
    JPanel jPanel4 = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JTextField jstartPointField = new JTextField();
    JTextField jendPointField = new JTextField();
    GridBagLayout gridBagLayout5 = new GridBagLayout();
    CardLayout cardLayout1 = new CardLayout();
    JProgressBar progressBar = new JProgressBar();
    JPanel jPanel5 = new JPanel();
    GridBagLayout gridBagLayout6 = new GridBagLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JLabel jLabel4 = new JLabel();
    JPanel progressBarPanel1 = new JPanel();
    JLabel jLabel5 = new JLabel();
    JPanel subSeqPanel1 = new JPanel();
    JPanel jPanel6 = new JPanel();
    JLabel jLabel6 = new JLabel();
    JComboBox jProgramBox1 = new JComboBox();
    JLabel jLabel7 = new JLabel();
    JLabel databaseLabel1 = new JLabel();
    JPanel jPanel7 = new JPanel();
    JLabel jLabel8 = new JLabel();
    GridBagLayout gridBagLayout7 = new GridBagLayout();
    JLabel jAlgorithmLabel = new JLabel();
    JTextField jendPointField1 = new JTextField();
    JPanel jPanel8 = new JPanel();
    JPanel jOtherAlgorithemPane = new JPanel();
    GridBagLayout gridBagLayout8 = new GridBagLayout();
    FlowLayout flowLayout2 = new FlowLayout();
    JTextField jstartPointField1 = new JTextField();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel jPanel9 = new JPanel();
    JProgressBar progressBar1 = new JProgressBar();
    GridBagLayout gridBagLayout9 = new GridBagLayout();
    JScrollPane jScrollPane2 = new JScrollPane();
    JList jList2 = new JList(algorithmParameter);
    GridBagLayout gridBagLayout13 = new GridBagLayout();
    BorderLayout borderLayout4 = new BorderLayout();
    JCheckBox jDisplayInWebBox = new JCheckBox();
    BorderLayout borderLayout1 = new BorderLayout();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JLabel jGenericCodeLabel = new JLabel();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JButton jButton1 = new JButton();
    JButton blastStopButton = new JButton();
    JButton algorithmSearch = new JButton();
    JLabel jLabel13 = new JLabel();
    JProgressBar progressBar3 = new JProgressBar();
    JTextField jendPointField3 = new JTextField();
    JPanel progressBarPanel3 = new JPanel();
    FlowLayout flowLayout4 = new FlowLayout();
    GridBagLayout gridBagLayout14 = new GridBagLayout();
    BorderLayout borderLayout8 = new BorderLayout();
    JPanel jHMMPane = new JPanel();
    JPanel subSeqPanel3 = new JPanel();
    JPanel jPanel11 = new JPanel();
    JLabel jLabel14 = new JLabel();
    JPanel jPanel14 = new JPanel();
    JPanel jPanel15 = new JPanel();
    BorderLayout borderLayout9 = new BorderLayout();
    JLabel jLabel15 = new JLabel();
    JTextField jstartPointField3 = new JTextField();
    JList jList4 = new JList(hmmParameter);
    JPanel jPanel16 = new JPanel();
    JLabel jAlgorithmLabel2 = new JLabel();
    JLabel jLabel16 = new JLabel();
    JButton jButton6 = new JButton();
    JButton jButton7 = new JButton();
    FlowLayout flowLayout5 = new FlowLayout();
    JButton jButton2 = new JButton();
    JScrollPane jScrollPane3 = new JScrollPane();
    BorderLayout borderLayout6 = new BorderLayout();
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    public ParameterViewWidget() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * todo Jbuilder set up error, need change permission to private
     * @throws Exception
     */
    private void jbInit() throws Exception {

        pfpFilterBox.setToolTipText("Paracel Filtering Package");
        pfpFilterBox.setSelected(true);
        pfpFilterBox.setText("PFP Filter");
        jBasicPane.setLayout(gridBagLayout6);
        jBasicPane.setMinimumSize(new Dimension(10, 100));
        DatabaseLabel.setText("Database:");
        jServerInfoPane.setLayout(cardLayout1);
        lowComplexFilterBox.setMinimumSize(new Dimension(10, 23));
        lowComplexFilterBox.setMnemonic('0');
        lowComplexFilterBox.setSelected(false);
        lowComplexFilterBox.setText("Low Complexity");
        lowComplexFilterBox.addActionListener(new
                                              ParameterViewWidget_lowComplexFilterBox_actionAdapter(this));
        programLabel.setText("Program:");
        jAdvancedPane.setLayout(gridBagLayout3);
        jMatrixBox.addItem("dna.mat");
        jMatrixBox.addItem("blosum50");
        jMatrixBox.addItem("blosum62");
        jMatrixBox.addItem("blosum100");
     
        jDBList.setToolTipText("Select a database");
        jDBList.setVerifyInputWhenFocusTarget(true);
        jDBList.setVisibleRowCount(1);
        jProgramBox.addItem("Select a program");
        jProgramBox.addItem("blastn");
        jProgramBox.addItem("blastp");
        jProgramBox.addItem("blastx");
        jProgramBox.addItem("tblastn");
        jProgramBox.addItem("tblastx");
        blastButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastButton.setHorizontalAlignment(SwingConstants.LEFT);
        blastButton.setHorizontalTextPosition(SwingConstants.CENTER);
        blastButton.setText("BLAST");
        blastButton.addActionListener(new
                                      ParameterViewWidget_blastButton_actionAdapter(this));
        jTabbedPane1.setDebugGraphicsOptions(0);
        jTabbedPane1.setMinimumSize(new Dimension(5, 5));
        jProgramBox.addActionListener(new
                                      ParameterViewWidget_jProgramBox_actionAdapter(this));
        jMatrixBox.addActionListener(new
                                     ParameterViewWidget_jMatrixBox_actionAdapter(this));
        this.setLayout(borderLayout1);
        maskLookupOnlyBox.setText("Mask the lookup table only");
        maskLookupOnlyBox.setMinimumSize(new Dimension(5, 23));
        maskLookupOnlyBox.setMnemonic('0');
        maskLookupOnlyBox.setSelected(false);
        expectLabel.setText("Matrix:");
        jMatrixBox.setToolTipText("Select the expect value here.");
        jMatrixBox.setVerifyInputWhenFocusTarget(true);
        jMatrixBox.setSelectedIndex(0);

        jExpectBox.setSelectedIndex( -1);
        jExpectBox.setVerifyInputWhenFocusTarget(true);
        jExpectBox.setEditable(true);
        jExpectBox.setToolTipText("Select the expect value here.");
        // jExpectBox.addActionListener(new ParameterViewWidget_jExpectBox_actionAdapter(this));
        matrixLabel.setText("Expect:");
        jqueryGenericCodeBox.setSelectedItem("10");

        jqueryGenericCodeBox.setSelectedIndex( -1);
        jqueryGenericCodeBox.setVerifyInputWhenFocusTarget(true);
        jqueryGenericCodeBox.setToolTipText("Select the expect value here.");
        //jqueryGenericCodeBox.addActionListener(new ParameterViewWidget_jqueryGenericCodeBox_actionAdapter(this));
        jFrameShiftLabel.setText("Frame shift penalty:");
        jFrameShiftPaneltyBox.setToolTipText("Select the expect value here.");
        jFrameShiftPaneltyBox.setVerifyInputWhenFocusTarget(true);
        jFrameShiftPaneltyBox.setSelectedIndex( -1);
        jFrameShiftPaneltyBox.setSelectedIndex( -1);
        jFrameShiftPaneltyBox.setSelectedItem(null);
        jFrameShiftPaneltyBox.addActionListener(new
                                                ParameterViewWidget_jFrameShiftPaneltyBox_actionAdapter(this));
        blastxSettingPanel.setLayout(gridBagLayout2);
        jServerInfoPane.setMinimumSize(new Dimension(0, 0));
        //jServerInfoPane.setPreferredSize(new Dimension(0, 0));
        jServerInfoPane.setToolTipText("Blast server Info");
        jPanel3.setLayout(gridBagLayout1);
        jPanel1.setLayout(borderLayout2);
        jProgramBox.setAutoscrolls(false);
        jProgramBox.setMinimumSize(new Dimension(26, 21));
        //jProgramBox.setPreferredSize(new Dimension(26, 21));
        subSeqPanel.setLayout(gridBagLayout5);
        jPanel4.setLayout(flowLayout1);
        jLabel1.setText("to ");
        jLabel2.setText("From");
        jLabel3.setText("Subsequence: ");
        jstartPointField.setText("1");
        jendPointField.setText("end");
        progressBarPanel.setLayout(borderLayout3);
        progressBarPanel.setMinimumSize(new Dimension(10, 16));
        jLabel4.setAlignmentY((float) 0.5);
        jLabel4.setHorizontalTextPosition(SwingConstants.TRAILING);
        jLabel4.setText("Please specify subsequence, program and database.");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        progressBarPanel1.setMinimumSize(new Dimension(10, 16));
        progressBarPanel1.setLayout(borderLayout5);
        jLabel5.setAlignmentY((float) 0.5);
        jLabel5.setMinimumSize(new Dimension(5, 15));
        jLabel5.setHorizontalTextPosition(SwingConstants.TRAILING);
        jLabel5.setText("Please specify subsequence, database and program .");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        subSeqPanel1.setLayout(gridBagLayout9);
        jPanel6.setLayout(flowLayout2);
        jLabel6.setText("to ");
//    jProgramBox1.addActionListener(new
        //                                ParameterViewWidget_jProgramBox1_actionAdapter(this));
        jProgramBox1.setAutoscrolls(false);
        jProgramBox1.setMinimumSize(new Dimension(26, 21));
        //jProgramBox1.setPreferredSize(new Dimension(26, 21));
        jLabel7.setText("Subsequence: ");
        databaseLabel1.setToolTipText("");
        databaseLabel1.setText("Database:");
        jPanel7.setLayout(gridBagLayout7);
        jLabel8.setText("From");
        jAlgorithmLabel.setText("Algorithms:");
        jendPointField1.setText("end");
        jOtherAlgorithemPane.setLayout(gridBagLayout8);
//    jOtherAlgorithemPane.setPreferredSize(new Dimension(10, 100));
        jOtherAlgorithemPane.setMinimumSize(new Dimension(10, 100));
        jstartPointField1.setText("1");
        jPanel9.setLayout(gridBagLayout13);
        progressBar1.setOrientation(JProgressBar.HORIZONTAL);
        progressBar1.setBorder(BorderFactory.createEtchedBorder());
        progressBar1.setStringPainted(true);
        jPanel8.setLayout(borderLayout4);

//    jProgramBox2.setPreferredSize(new Dimension(26, 21));
//    jBasicPane1.setPreferredSize(new Dimension(10, 100));
        //   blastButton2.addActionListener(new
        //                                 ParameterViewWidget_blastButton2_actionAdapter(this));
        jAdvancedPane.setMinimumSize(new Dimension(5, 25));
        filterPanel.setMinimumSize(new Dimension(5, 10));
        blastxSettingPanel.setMinimumSize(new Dimension(5, 115));
        subSeqPanel1.setMinimumSize(new Dimension(10, 30));
        jPanel8.setMinimumSize(new Dimension(5, 15));
        jPanel7.setPreferredSize(new Dimension(5, 46));
        jPanel9.setPreferredSize(new Dimension(5, 93));
        jList2.setMaximumSize(new Dimension(209, 68));
        jList2.setMinimumSize(new Dimension(100, 68));
        jDisplayInWebBox.setMinimumSize(new Dimension(10, 23));
        jDisplayInWebBox.setSelected(true);
        jDisplayInWebBox.setText("Display result in your web browser");
        jGenericCodeLabel.setText("Query genetic code:");
        jButton1.setFont(new java.awt.Font("Arial Black", 0, 11));
        jButton1.setText("STOP");
        jButton1.addActionListener(new
                                   ParameterViewWidget_jButton1_actionAdapter(this));
        blastStopButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastStopButton.setVerifyInputWhenFocusTarget(true);
        blastStopButton.setText("STOP");
        blastStopButton.addActionListener(new
                                          ParameterViewWidget_blastStopButton_actionAdapter(this));
        algorithmSearch.setFont(new java.awt.Font("Arial Black", 0, 11));
        algorithmSearch.setText("SEARCH");
        algorithmSearch.addActionListener(new
                                          ParameterViewWidget_algorithmSearch_actionAdapter(this));
        jLabel13.setText("From");
        progressBar3.setStringPainted(true);
        progressBar3.setBorder(BorderFactory.createEtchedBorder());
        progressBar3.setOrientation(JProgressBar.HORIZONTAL);
        jendPointField3.setText("end");
        progressBarPanel3.setLayout(borderLayout9);
        progressBarPanel3.setMinimumSize(new Dimension(10, 16));
        jHMMPane.setLayout(gridBagLayout4);
        jHMMPane.setDebugGraphicsOptions(0);
        jHMMPane.setMinimumSize(new Dimension(10, 100));
        subSeqPanel3.setLayout(gridBagLayout14);
        subSeqPanel3.setMinimumSize(new Dimension(10, 30));
        jPanel11.setLayout(borderLayout8);
        jPanel11.setMinimumSize(new Dimension(5, 15));
        jLabel14.setText("Subsequence: ");
        jPanel14.setLayout(flowLayout5);
        jPanel14.setPreferredSize(new Dimension(5, 46));
        jPanel15.setLayout(flowLayout4);
        jLabel15.setText("to ");
        jstartPointField3.setText("1");
        jList4.setMaximumSize(new Dimension(209, 68));
        jList4.setMinimumSize(new Dimension(100, 68));
        jPanel16.setLayout(borderLayout6);
        jPanel16.setPreferredSize(new Dimension(5, 93));
        jAlgorithmLabel2.setText("Search Mode:");
        jLabel16.setAlignmentY((float) 0.5);
        jLabel16.setMinimumSize(new Dimension(5, 15));
        jLabel16.setHorizontalTextPosition(SwingConstants.TRAILING);
        jLabel16.setText("Please specify subsequence, search mode.");
        jLabel16.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel16.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton6.setText("Load your own Model");
        jButton6.addActionListener(new
                                   ParameterViewWidget_jButton6_actionAdapter(this));
        jButton7.setText("Browse Pfam Model");
        jButton7.addActionListener(new
                                   ParameterViewWidget_jButton7_actionAdapter(this));
        jButton2.setFont(new java.awt.Font("Arial Black", 0, 11));
        jButton2.setText("HMM SEARCH");
        jButton2.addActionListener(new
                                   ParameterViewWidget_jButton2_actionAdapter(this));
        blastxSettingPanel.add(jExpectBox,
                               new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jFrameShiftPaneltyBox,
                               new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jqueryGenericCodeBox,
                               new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jFrameShiftLabel,
                               new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(matrixLabel,
                               new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jMatrixBox,
                               new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(expectLabel,
                               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jGenericCodeLabel,
                               new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        jTabbedPane1.add(jHMMPane, "HMM");
        jPanel3.add(programLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(13, 8, 13, 0), 23, 5));
        jPanel3.add(jProgramBox, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(13, 20, 13, 0), 131, -1));
        jBasicPane.add(jPanel3, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 146, -13));
        jPanel4.add(blastButton, null);
        jPanel4.add(blastStopButton, null);
        jBasicPane.add(subSeqPanel, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 14), 80, -1));
        jPanel1.add(jScrollPane1, BorderLayout.CENTER);
        jPanel1.add(DatabaseLabel, BorderLayout.NORTH);
        jTabbedPane1.add(jAdvancedPane, "Advanced_Options");
        jScrollPane1.getViewport().add(jDBList, null);
        jBasicPane.add(jPanel4, new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(7, -3, 0, 0), 312, -5)); //progress bar init
        progressBar.setOrientation(JProgressBar.HORIZONTAL);
        progressBar.setBorder(BorderFactory.createEtchedBorder());
        progressBar.setStringPainted(true);
        subSeqPanel.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        subSeqPanel.add(jLabel2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 17, 0, 0), 0, 10));
        subSeqPanel.add(jstartPointField,
                        new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                                               , GridBagConstraints.WEST,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 18, 0, 0), 14, 4));
        subSeqPanel.add(jLabel1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 21, 0, 0), 0, 10));
        subSeqPanel.add(jendPointField,
                        new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0
                                               , GridBagConstraints.WEST,
                                               GridBagConstraints.HORIZONTAL,
                                               new Insets(5, 36, 0, 49), 6, 4));
        jBasicPane.add(progressBarPanel,
                       new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(1, -3, 0, 0), 247, 2));
        progressBarPanel.add(progressBar, BorderLayout.SOUTH);
        jBasicPane.add(jPanel5, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 390, 18));
        jPanel5.add(jLabel4, null);
        jBasicPane.add(jPanel1, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 329, 81));
        jTabbedPane1.add(jBasicPane, "BLAST");
        jPanel6.add(algorithmSearch, null);
        jPanel6.add(jButton1, null);
        jOtherAlgorithemPane.add(progressBarPanel1,
                                 new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(1, -3, 0, 0), 247, 2));
        progressBarPanel1.add(progressBar1, BorderLayout.SOUTH);
        jOtherAlgorithemPane.add(jPanel8,
                                 new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 390, 18));
        jPanel8.add(jLabel5, BorderLayout.CENTER);
        jOtherAlgorithemPane.add(jPanel7,
                                 new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 146, -13));
        jOtherAlgorithemPane.add(subSeqPanel1,
                                 new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 14), 80, -1));
        subSeqPanel1.add(jLabel8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 17, 0, 0), 0, 10));
        subSeqPanel1.add(jstartPointField1,
                         new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 18, 0, 0), 14, 4));
        subSeqPanel1.add(jLabel6, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        subSeqPanel1.add(jendPointField1,
                         new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 36, 0, 49), 6, 4));
        subSeqPanel1.add(jLabel7, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        jOtherAlgorithemPane.add(jPanel6,
                                 new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(7, -3, 0, 0), 312, -5));
        jPanel7.add(databaseLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(13, 8, 13, 0), 23, 5));
        jPanel7.add(jProgramBox1, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(13, 0, 13, 0), 131, -1));
        jOtherAlgorithemPane.add(jPanel9,
                                 new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, -3, 0, 0), 329, 81));
        jPanel9.add(jAlgorithmLabel,
                    new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                           , GridBagConstraints.WEST,
                                           GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 347, 0));
        jPanel9.add(jScrollPane2, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 1, 59), 78, -61));
        jTabbedPane1.add(jServerInfoPane, "Server_Info");
        jTabbedPane1.add(sgePanel, "Grid_Services");
        jScrollPane2.getViewport().add(jList2, null);

        subSeqPanel3.add(jLabel13, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 17, 0, 0), 0, 10));
        subSeqPanel3.add(jstartPointField3,
                         new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 18, 0, 0), 14, 4));
        subSeqPanel3.add(jLabel15, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        subSeqPanel3.add(jendPointField3,
                         new GridBagConstraints(4, 0, 1, 1, 1.0, 0.0
                                                , GridBagConstraints.WEST,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5, 36, 0, 49), 6, 4));
        subSeqPanel3.add(jLabel14, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(5, 0, 0, 0), 0, 10));
        jTabbedPane1.add(jOtherAlgorithemPane, "Other_Algorithms");
        jHMMPane.add(progressBarPanel3,
                     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                            , GridBagConstraints.CENTER,
                                            GridBagConstraints.BOTH,
                                            new Insets(0, 0, 0, 0), 291, -2));
        progressBarPanel3.add(progressBar3, BorderLayout.SOUTH);
        jHMMPane.add(jPanel14, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 390, -18));
        jPanel14.add(jButton6, null);
        jPanel14.add(jButton7, null);
        jHMMPane.add(jPanel16, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 386, 1));
        jPanel16.add(jScrollPane3, BorderLayout.CENTER);
        jPanel16.add(jAlgorithmLabel2, BorderLayout.NORTH);
        jHMMPane.add(jPanel15, new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(7, 0, 3, 0), 192, 3));
        jPanel15.add(jButton2, null);
        jHMMPane.add(jPanel11, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 183, 26));
        jPanel11.add(jLabel16, BorderLayout.CENTER);
        jHMMPane.add(subSeqPanel3, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 98, 7));
        jScrollPane3.getViewport().add(jList4, null);
        filterPanel.add(pfpFilterBox, null);
        filterPanel.add(lowComplexFilterBox, null);
        filterPanel.add(maskLookupOnlyBox, null);
        filterPanel.add(jDisplayInWebBox, null);
        jAdvancedPane.add(blastxSettingPanel,
                          new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(10, 10, 0, 0), 0, 7));
        jAdvancedPane.add(filterPanel,
                          new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), -150,
                                                 82));
        this.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        jExpectBox.addItem("10");
        jExpectBox.addItem("1");

        jExpectBox.addItem("0.1");
        jExpectBox.addItem("0.01");
        jExpectBox.addItem("0.000000001");
        jExpectBox.addItem("100");

        jExpectBox.addItem("1000");
        jqueryGenericCodeBox.addItem("Standard");
        jqueryGenericCodeBox.addItem("Vertebrate Mitochondrial");
        jqueryGenericCodeBox.addItem("Yeast Mitochondrial ");
        jqueryGenericCodeBox.addItem("Invertebrate Mitochondrial ");
        jqueryGenericCodeBox.addItem("Echinoderm Mitochondrial ");
        jqueryGenericCodeBox.addItem("Euplotid Nuclear ");
        jFrameShiftPaneltyBox.addItem("NO OOF");
        jFrameShiftPaneltyBox.addItem("6");
        jFrameShiftPaneltyBox.addItem("7");
        jFrameShiftPaneltyBox.addItem("8");
        jFrameShiftPaneltyBox.addItem("9");
        jFrameShiftPaneltyBox.addItem("10");
        jFrameShiftPaneltyBox.addItem("11");
        jFrameShiftPaneltyBox.addItem("12");
        jFrameShiftPaneltyBox.addItem("13");
        jFrameShiftPaneltyBox.addItem("14");
        jFrameShiftPaneltyBox.addItem("15");

        jFrameShiftPaneltyBox.addItem("16");
        jFrameShiftPaneltyBox.addItem("17");
        jFrameShiftPaneltyBox.addItem("18");
        jFrameShiftPaneltyBox.addItem("19");
        jFrameShiftPaneltyBox.addItem("20");
        jFrameShiftPaneltyBox.addItem("25");
        jFrameShiftPaneltyBox.addItem("50");
        jFrameShiftPaneltyBox.addItem("100");

        jFrameShiftPaneltyBox.addItem("1000");
        jTabbedPane1.setSelectedComponent(jBasicPane);
        /*
             jProgramBox1.addItem("Smith-Waterman DNA");
             jProgramBox1.addItem("Smith-Waterman Protein");
             jProgramBox1.addItem("Frame (for DNA sequece to protein DB)");
         jProgramBox1.addItem("Reverse Frame (for protein sequecne to protein DB)");
             jProgramBox1.addItem("Double Frame (for DNA sequence to DNA DB)");
         */

        jProgramBox1.addItem("ecoli.nt");
        jProgramBox1.addItem("pdb.nt");
        jProgramBox1.addItem("pdbaa");
        jProgramBox1.addItem("yeast.aa");
        jProgramBox1.addItem("nr");

    }

    void jProgramBox_actionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();

        // Get the new item
        Object newItem = cb.getSelectedItem();

        //System.out.println(newItem + "selected the program" + e.getActionCommand());
        jDBList = new JList(AlgorithmMatcher.translateToArray((String) newItem));
        (jScrollPane1.getViewport()).add(jDBList, null);
        //jScrollPanel = new JScrollPanel(jDBList);
        // repaint();
    }

    void jMatrixBox_actionPerformed(ActionEvent e) {

    }

    void jFrameShiftPaneltyBox_actionPerformed(ActionEvent e) {

    }

    void lowComplexFilterBox_actionPerformed(ActionEvent e) {

    }

    public CSSequenceSet getFastaFile() {
        return fastaFile;
    }

    public void setBlastAppComponent(BlastAppComponent appComponent) {
        blastAppComponent = appComponent;

    }

    public void setFastaFile(CSSequenceSet sd) {
        fastaFile = sd;
        DSSequence s1 = sd.getSequence(0);
        if (s1 != null) {
            int end = s1.length();
            jendPointField.setText(new Integer(end).toString());
            jendPointField1.setText(new Integer(end).toString());
            jendPointField3.setText(new Integer(end).toString());
        }
    }

    public void reportError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    public ParameterSetter retriveParameters() {

        String dbName = (String) jDBList.getSelectedValue();

        String programName = (String) jProgramBox.getSelectedItem();

        if (dbName == null) {
            reportError("Please select a DATABASE to search!",
                        "Parameter Error");
            return null;

        } else {
            StringTokenizer st = new StringTokenizer(dbName);
            dbName = st.nextToken();

        }
        if (programName == null) {
            reportError("Please select a PROGRAM to search!", "Parameter Error");
            return null;
        }

        if (fastaFile == null) {
            reportError("Please select a sequence file first!",
                        "Parameter Error");
            return null;
        } else {
            jServerInfoPane.retriveServerInfo();
            //above method will be run by a thread, so the status of server reported is not reliable.
            /*Thread.sleep(5);
                   if (!jServerInfoPane.isServerOK()){
              System.out.println("OK" + jServerInfoPane.isServerOK() );
              return null;
                   }*/

            try {
                String tempFolder = System.getProperties().getProperty(
                        "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";

                }

                String outputFile = tempFolder + "Blast" +
                                    RandomNumberGenerator.getID() +
                                    ".html";
                //progressBar = new JProgressBar(0, 100);

                progressBar.setForeground(Color.ORANGE);
                progressBar.setBackground(Color.WHITE);

                progressBar.setIndeterminate(true);
                progressBar.setString("Blast is running.");

                SoapClient sc = new SoapClient(programName, dbName,
                                               outputFile);
                BlastAlgorithm blastAlgo = new BlastAlgorithm();
                blastAlgo.setBlastAppComponent(blastAppComponent);
                blastAlgo.setSoapClient(sc);
                blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
                blastAlgo.start();
                Thread.sleep(5);
                //System.out.println("WRONG at PVW: " + parameterSetter + "algo" + blastAlgo);
                if (blastAlgo != null) {
                    parameterSetter.setAlgo(blastAlgo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return parameterSetter;

    }

    public void retriveAlgoParameters() {
        //String[] dbName = (String[]) jDBList.getSelectedValues();

        if (fastaFile == null) {
            reportError("Please load a sequence file first!",
                        "No File Error");
            return;

        }
        String algoTitle = (String) jList2.getSelectedValue();
        if (algoTitle == null) {
            reportError("Please select a algorithm to search!",
                        "Parameter Error");
            return;

        }

        String algoName = AlgorithmMatcher.translate(algoTitle);

        String dbName = (String) jProgramBox1.getSelectedItem();

        if (dbName == null) {
            reportError("Please select a database name first!",
                        "No Database Error");
            return;

        }

        String matrix = (String) jMatrixBox.getSelectedItem();
        if (matrix == null) {
            reportError("Please select a matrix name first!",
                        "No Matrix Error");
            return;

        }

        //System.out.println("fasta file path: " + fastaFile);

        try {

            String tempFolder = System.getProperties().getProperty(
                    "temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";

            }
            String outputFile = tempFolder + "Algo" +
                                RandomNumberGenerator.getID() +
                                ".html";
            // System.out.println(outputFile + " outputfile");
            //progressBar = new JProgressBar(0, 100);

            progressBar1.setForeground(Color.ORANGE);
            progressBar1.setBackground(Color.WHITE);

            progressBar1.setIndeterminate(true);
            progressBar1.setString(algoTitle + " is running.");

            SoapClient sc = new SoapClient(algoName, dbName, matrix,
                                           fastaFile.getFASTAFileName().
                                           trim(),
                                           outputFile);
            BlastAlgorithm blastAlgo = new BlastAlgorithm();
            blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
            blastAlgo.setBlastAppComponent(blastAppComponent);
            blastAlgo.setSoapClient(sc);
            blastAlgo.start();
            Thread.sleep(5);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * blastFinished
     * Take care of the state of finished blast.
     */
    public void blastFinished(String cmd) {
        Date finished_Date = new Date();

        if (cmd.startsWith("Interrupted")) {
            progressBar.setIndeterminate(false);

            progressBar.setForeground(Color.ORANGE);
            progressBar.setBackground(Color.ORANGE);
            progressBar.setString("Stopped on " + finished_Date);

        } else if (cmd.startsWith("OTHERS_Interrupted")) {
            progressBar1.setIndeterminate(false);

            progressBar1.setForeground(Color.ORANGE);
            progressBar1.setBackground(Color.ORANGE);
            progressBar1.setString("Stopped on " + finished_Date);

        } else {

            if (cmd.startsWith("pb")) {

                progressBar.setIndeterminate(false);

                progressBar.setForeground(Color.ORANGE);
                progressBar.setBackground(Color.ORANGE);
                progressBar.setString("Finished on " + finished_Date);
            } else if (cmd.startsWith("btk search")) {
                progressBar1.setIndeterminate(false);

                progressBar1.setForeground(Color.ORANGE);
                progressBar1.setBackground(Color.ORANGE);
                progressBar1.setString("Finished on " + finished_Date);

            } else if (cmd.startsWith("btk hmm")) {
                progressBar3.setIndeterminate(false);

                progressBar3.setForeground(Color.ORANGE);
                progressBar3.setBackground(Color.ORANGE);
                progressBar3.setString("Finished on " + finished_Date);

            }

        }
    }

    void blastButton_actionPerformed(ActionEvent e) {

        parameterSetter = retriveParameters();
        //Session session = createSession(parameter);
        //session.start();

        /* try{
           BrowserLauncher.openURL("c:/data/status.html");
         }catch (IOException ex){ex.printStackTrace();}
         */
    }

    void blastButton1_actionPerformed(ActionEvent e) {
        //System.out.println("run");
        //retriveParameters();
        //retriveAlgoParameters();

    }

    void blastButton2_actionPerformed(ActionEvent e) {
        //System.out.println("stop");
        //stopBlastAction();

    }

    void stopBlastAction() {
        blastFinished("Interrupted");
        BWAbstractAlgorithm algo = parameterSetter.getAlgo();
        if (algo != null) {
            algo.stop();
        }
    };

    void jButton1_actionPerformed(ActionEvent e) {
        //System.out.println("jbutton1");
        // retriveAlgoParameters();
        blastFinished("OTHERS_Interrupted");

    }

    void blastStopButton_actionPerformed(ActionEvent e) {
        stopBlastAction();
    }

    void algorithmSearch_actionPerformed(ActionEvent e) {
        try {
            retriveAlgoParameters();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jButton7_actionPerformed(ActionEvent e) {
        try {
            BrowserLauncher.openURL("http://pfam.wustl.edu/browse.shtml");
        } catch (IOException ex) {
            reportError(ex.getMessage(), "Connection Error");

        }
    }

    void jButton2_actionPerformed(ActionEvent e) {
        if (fastaFile == null || fastaFile.isDNA()) {
            reportError("Please select a PROTEIN sequence file first.",
                        "MisMatch Error");
            return;
        }
        String algoTitle = (String) jList4.getSelectedValue();
        if (algoTitle == null) {
            reportError("Please select a Pfam model first.", "Null Parameter.");
            return;
        }
        String query = AlgorithmMatcher.translate(algoTitle);

        try {

            String tempFolder = System.getProperties().getProperty(
                    "temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = "./";

            }
            String outputFile = tempFolder + "Hmm" +
                                RandomNumberGenerator.getID() +
                                ".txt";
            //System.out.println(outputFile + " outputfile");
            //progressBar = new JProgressBar(0, 100);

            progressBar3.setForeground(Color.ORANGE);
            progressBar3.setBackground(Color.WHITE);

            progressBar3.setIndeterminate(true);
            progressBar3.setString(algoTitle + " is running.");

            SoapClient sc = new SoapClient(query, null, null,
                                           fastaFile.getFASTAFileName().trim(),
                                           outputFile);
            BlastAlgorithm blastAlgo = new BlastAlgorithm();
            blastAlgo.setStartBrowser(jDisplayInWebBox.isSelected());
            blastAlgo.setBlastAppComponent(blastAppComponent);
            blastAlgo.setSoapClient(sc);
            blastAlgo.start();
            Thread.sleep(5);

        } catch (Exception ex) {
            ex.printStackTrace();

        }

    }

    /**
     * createGridDialog
     */
    public void createGridDialog() {

        CreateGridServiceDialog csd = new CreateGridServiceDialog(null,
                "grid service");

    }

    void jButton6_actionPerformed(ActionEvent e) {
        //loadFile();
        String textFile =
                "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Hmm89547134.txt";
        String inputfile =
                "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Hmm89547134.txt";
        HMMDataSet blastResult = new HMMDataSet(textFile,
                                                inputfile, null);
        try {

//add twice blastDataSet. change!@ ???
            ProjectNodeAddedEvent event = new ProjectNodeAddedEvent("message", null,
                    blastResult);
            blastAppComponent.publishProjectNodeAddedEvent(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * loadFile
     */
    public void loadFile() {
        if (JFileChooser.APPROVE_OPTION == jFileChooser1.showOpenDialog(this)) {
            // Call openFile to attempt to load the text from file into TextArea
            openFile(jFileChooser1.getSelectedFile().getPath());
        }
        this.repaint();

    }

    void openFile(String fileName) {
        try {
            // Open a file of the given name.
            File file = new File(fileName);

            // Get the size of the opened file.
            int size = (int) file.length();

            // Set to zero a counter for counting the number of
            // characters that have been read from the file.
            int chars_read = 0;

            // Create an input reader based on the file, so we can read its data.
            // FileReader handles international character encoding conversions.
            FileReader in = new FileReader(file);

            // Create a character array of the size of the file,
            // to use as a data buffer, into which we will read
            // the text data.
            char[] data = new char[size];

            // Read all available characters into the buffer.
            while (in.ready()) {
                // Increment the count for each character read,
                // and accumulate them in the data buffer.
                chars_read += in.read(data, chars_read, size - chars_read);
            }
            in.close();

            // jTextArea1.setText(new String(data, 0, chars_read));

//   jList4.add("your own model", null);
            // Display the name of the opened directory+file in the statusBar.
            //  statusBar.setText("Opened " + fileName);
            //  updateCaption();
        } catch (IOException e) {
            //statusBar.setText("Error opening " + fileName);
        }
    }

    public BlastAppComponent getBlastAppComponent() {
        return blastAppComponent;
    }

}


class ParameterViewWidget_jProgramBox_actionAdapter implements java.awt.event.
        ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_jProgramBox_actionAdapter(ParameterViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jProgramBox_actionPerformed(e);
    }
}


class ParameterViewWidget_jMatrixBox_actionAdapter implements java.awt.event.
        ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_jMatrixBox_actionAdapter(ParameterViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMatrixBox_actionPerformed(e);
    }
}


class ParameterViewWidget_jFrameShiftPaneltyBox_actionAdapter implements java.
        awt.event.ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_jFrameShiftPaneltyBox_actionAdapter(ParameterViewWidget
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jFrameShiftPaneltyBox_actionPerformed(e);
    }
}


class ParameterViewWidget_lowComplexFilterBox_actionAdapter implements java.awt.
        event.ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_lowComplexFilterBox_actionAdapter(ParameterViewWidget
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.lowComplexFilterBox_actionPerformed(e);
    }
}


class ParameterViewWidget_blastButton_actionAdapter implements java.awt.event.
        ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_blastButton_actionAdapter(ParameterViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.blastButton_actionPerformed(e);
    }
}


/*
 class ParameterViewWidget_blastButton1_actionAdapter implements java.awt.event.ActionListener {
  ParameterViewWidget adaptee;

  ParameterViewWidget_blastButton1_actionAdapter(ParameterViewWidget adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.blastButton1_actionPerformed(e);
  }
 }
 */

class ParameterViewWidget_jButton1_actionAdapter implements java.awt.event.
        ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_jButton1_actionAdapter(ParameterViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton1_actionPerformed(e);
    }
}


class ParameterViewWidget_blastStopButton_actionAdapter implements java.awt.
        event.ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_blastStopButton_actionAdapter(ParameterViewWidget
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.blastStopButton_actionPerformed(e);
    }
}


class ParameterViewWidget_algorithmSearch_actionAdapter implements java.awt.
        event.ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_algorithmSearch_actionAdapter(ParameterViewWidget
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.algorithmSearch_actionPerformed(e);
    }
}


class ParameterViewWidget_jButton7_actionAdapter implements java.awt.event.
        ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_jButton7_actionAdapter(ParameterViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton7_actionPerformed(e);
    }
}


class ParameterViewWidget_jButton2_actionAdapter implements java.awt.event.
        ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_jButton2_actionAdapter(ParameterViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton2_actionPerformed(e);
    }
}


class ParameterViewWidget_jButton6_actionAdapter implements java.awt.event.
        ActionListener {
    ParameterViewWidget adaptee;

    ParameterViewWidget_jButton6_actionAdapter(ParameterViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton6_actionPerformed(e);
    }
}
