package org.geworkbench.components.promoter;

import org.biojava.bio.gui.DistributionLogo;
import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSSeqRegistration;
import org.geworkbench.components.promoter.modulediscovery.CSMultiSeqPattern;
import org.geworkbench.components.promoter.modulediscovery.Discovery;
import org.geworkbench.components.promoter.modulediscovery.SequenceFileReader;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.util.RandomSequenceGenerator;
import org.geworkbench.util.associationdiscovery.statistics.ClusterStatistics;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.promoter.SequencePatternDisplayPanel;
import org.geworkbench.util.promoter.pattern.Display;
import org.geworkbench.util.promoter.pattern.PatternDisplay;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * <p>Widget provides all GUI services for sequence panel displays.</p>
 * <p>Widget is controlled by its associated component, SequenceViewAppComponent</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab</p>
 *
 * @author Xuegong Wang, manjunath at genomecenter dot columbia dot edu, Xiaoqing Zhang
 * @version 1.0
 */

public class PromoterViewPanel extends JPanel {
    public class ScoreStats {
        public double score;
        public double pValue;
        public ScoreStats() {}
    }


    DSPanel<DSGeneMarker> markers = null;

    private DSSequenceSet sequenceDB = null;

    private JFileChooser fc = null;
    private JFileChooser fc2 = null;
    boolean selectedRegionChanged = false;
    JPanel jInfoPanel = new JPanel();
    DSSequenceSet background = null;

    //Layouts

    private BorderLayout borderLayout2 = new BorderLayout();

    //Panels and Panes
    private JScrollPane seqScrollPane = new JScrollPane();

    private SequencePatternDisplayPanel seqDisPanel = new
            SequencePatternDisplayPanel();

    JPanel jPanel2 = new JPanel();

    //stores transcriptionfactors

    BorderLayout borderLayout1 = new BorderLayout();
    JButton displayBtn = new JButton();
    BorderLayout borderLayout3 = new BorderLayout();
    JButton jSaveButton = new JButton();
    JButton jaddNewTFB = new JButton();
    JPanel jPanel3 = new JPanel();
    JScrollPane jScrollPane1 = new JScrollPane();
    JList jTranscriptionFactorList = new JList();
    GridLayout gridLayout1 = new GridLayout();
    JButton jButton2 = new JButton();
    JProgressBar jProgressBar1 = new JProgressBar();
    ButtonGroup sourceGroup = new ButtonGroup();

    File resultFile = null;
    SequenceFileReader sfr = null;
    DefaultListModel modulelistmodel = new DefaultListModel();
    JPanel jPanel4 = new JPanel();
    BorderLayout borderLayout4 = new BorderLayout();
    JSplitPane jSplitPane1 = new JSplitPane();
    GridLayout gridLayout2 = new GridLayout();
    JLabel jLabel4 = new JLabel();
    TitledBorder titledBorder1;
    TitledBorder titledBorder2;
    TitledBorder titledBorder3;
    TitledBorder titledBorder4;
    TitledBorder titledBorder5;
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    JList moduleList = new JList();
    JPanel jPanel8 = new JPanel();
    JScrollPane jScrollPane3 = new JScrollPane();
    JPanel jPanel5 = new JPanel();
    BorderLayout borderLayout6 = new BorderLayout();
    JTextField minOccur = new JTextField("50");
    JPanel jPanel7 = new JPanel();
    JPanel jPanel6 = new JPanel();
    JPanel jPanel9 = new JPanel();
    JLabel jLabel2 = new JLabel();
    JLabel winSizeL = new JLabel();
    BorderLayout borderLayout10 = new BorderLayout();
    JLabel minOccurL = new JLabel();
    BorderLayout borderLayout8 = new BorderLayout();
    JButton jDiscoverModuleBttn = new JButton();
    BorderLayout borderLayout9 = new BorderLayout();
    BorderLayout borderLayout7 = new BorderLayout();
    JTextField winSize = new JTextField("100");
    JPanel jPanel10 = new JPanel();
    JPanel jPanel11 = new JPanel();
    BorderLayout borderLayout11 = new BorderLayout();
    JList jSelectedTFList = new JList();
    JLabel jLabel3 = new JLabel();
    JPanel jPanel1 = new JPanel();
    JPanel northPanel = new JPanel();
    JCheckBox showTF = new JCheckBox("Show TFs");
    JCheckBox showSeqPattern = new JCheckBox("Show Patterns     ");
    JButton clearButton = new JButton("Clear All");
    JToolBar jToolBar = new JToolBar();
    BorderLayout borderLayout5 = new BorderLayout();
    JScrollPane jScrollPane2 = new JScrollPane();
    boolean isRunning = false;
    boolean stop = false;
    int averageNo = 10;
    double pValue = 0.05;
    private ArrayList<DSPattern> seqPatterns = new ArrayList<DSPattern>();
    private ArrayList<DSPattern> promoterPatterns = new ArrayList<DSPattern>();
    HashMap<DSPattern,
            Display> seqPatternDisplay = new HashMap<DSPattern, Display>();
    Hashtable<DSPattern<DSSequence, DSSeqRegistration>,
            List<DSPatternMatch<DSSequence,
            DSSeqRegistration>>>
            seqPatternMatches = new Hashtable<DSPattern<DSSequence,
                                DSSeqRegistration>,
                                List<DSPatternMatch<DSSequence,
                                DSSeqRegistration>>>();
    HashMap<DSPattern,
            Display> promoterPatternDisplay = new HashMap<DSPattern, Display>();
    Hashtable<DSPattern<DSSequence, DSSeqRegistration>,
            List<DSPatternMatch<DSSequence,
            DSSeqRegistration>>>
            promoterPatternMatches = new Hashtable<DSPattern<DSSequence,
                                     DSSeqRegistration>,
                                     List<DSPatternMatch<DSSequence,
                                     DSSeqRegistration>>>();


    private HashMap primerToMotif = new HashMap();

    public PromoterViewPanel() {
        try {
            jbInit();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(borderLayout2);

        jProgressBar1.setForeground(Color.green);
        jProgressBar1.setBorderPainted(true);

        jPanel2.setLayout(borderLayout1);

        seqDisPanel.setMinimumSize(new Dimension(0, 0));
        seqDisPanel.setMaximumSize(new Dimension(32767, 32767));
        seqDisPanel.setPreferredSize(new Dimension(0, 20));
        jInfoPanel.setBorder(BorderFactory.createEtchedBorder());

        jInfoPanel.setMinimumSize(new Dimension(14, 50));
        jInfoPanel.setOpaque(true);
        jInfoPanel.setPreferredSize(new Dimension(20, 60));

        seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
        seqScrollPane.setMaximumSize(new Dimension(32767, 32767));
        seqScrollPane.setMinimumSize(new Dimension(24, 24));
        seqScrollPane.setPreferredSize(new Dimension(250, 250));
        seqDisPanel.setInfoPanel(jInfoPanel);

        jSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane1.setDividerSize(4);
        jSplitPane1.setLastDividerLocation(60);
        jTabbedPane1.setMinimumSize(new Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new Dimension(100, 100));

        jPanel10.setLayout(borderLayout11);

        jPanel9.setBorder(BorderFactory.createRaisedBevelBorder());
        jPanel9.setLayout(borderLayout7);

        jPanel7.setLayout(borderLayout9);

        jDiscoverModuleBttn.setEnabled(true);
        jDiscoverModuleBttn.setPreferredSize(new Dimension(68, 23));
        jDiscoverModuleBttn.setSelected(false);
        jDiscoverModuleBttn.setText("Discover Module");
        jDiscoverModuleBttn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jDiscoverModuleBttn_actionPerformed(e);
            }
        });

        jScrollPane3.setHorizontalScrollBarPolicy(JScrollPane.
                                                  HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.
                                                VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane3.setAutoscrolls(false);
        jScrollPane3.setPreferredSize(new Dimension(64, 59));

        jPanel4.setLayout(borderLayout4);
        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel4.setText("TF List");

        fillupTranscriptionFactorList();
        displayBtn.setToolTipText(
                "Scans the selected sequences with the TF matrices");

        displayBtn.setText("Scan");
        displayBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayBtn_actionPerformed(e);
            }
        });
        jSaveButton.setMaximumSize(new Dimension(100, 27));
        jSaveButton.setMinimumSize(new Dimension(55, 27));
        jSaveButton.setPreferredSize(new Dimension(55, 27));
        jSaveButton.setToolTipText("Save current result");
        jSaveButton.setActionCommand("jSaveButton");
        jSaveButton.setRolloverEnabled(false);
        jSaveButton.setText(" Save");
        jSaveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jSaveButton_actionPerformed(e);
            }
        });
        jaddNewTFB.setMaximumSize(new Dimension(100, 27));
        jaddNewTFB.setMinimumSize(new Dimension(73, 27));
        jaddNewTFB.setToolTipText("Add new transcription factor");
        jaddNewTFB.setText("Add TF");
        jaddNewTFB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jaddNewTFB_actionPerformed(e);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.
                                                  HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setAlignmentX((float) 0.5);
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setBorder(BorderFactory.createEtchedBorder());
        jScrollPane1.setDebugGraphicsOptions(0);
        jScrollPane1.setMaximumSize(new Dimension(32767, 32767));
        jScrollPane1.setMinimumSize(new Dimension(24, 24));
        jScrollPane1.setPreferredSize(new Dimension(100, 140));
        jScrollPane1.setRequestFocusEnabled(true);

        jPanel3.setLayout(gridLayout1);
        jPanel3.setPreferredSize(new Dimension(160, 240));
        gridLayout1.setColumns(1);
        gridLayout1.setRows(2);
        jButton2.setToolTipText("Retrieve promoter sequences");
        jButton2.setHorizontalTextPosition(SwingConstants.TRAILING);
        jButton2.setText("Retrieve");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
            }
        });
        moduleList.setModel(modulelistmodel);
        moduleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                moduleListSelection_action(e);
            }
        }
        );
        jPanel8.setLayout(borderLayout10);
        jPanel5.setLayout(borderLayout6);
        minOccur.setOpaque(true);
        minOccur.setPreferredSize(new Dimension(60, 20));
        minOccur.setText("50");
        jPanel6.setLayout(borderLayout8);
        jLabel2.setText("Discovered Modules:");
        winSizeL.setText("Win Size");
        minOccurL.setText("Min Occur");
        winSize.setPreferredSize(new Dimension(60, 20));
        winSize.setText("200");
        jSelectedTFList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jSelectedTFList_mouseClicked(e);
            }
        });
        jSelectedTFList.setToolTipText("Selected transcription factors");
        DefaultListModel ls1 = new DefaultListModel();
        jSelectedTFList.setModel(ls1);
        jLabel3.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel3.setText("Selected TF");
        jPanel1.setLayout(borderLayout5);
        jPanel1.setForeground(Color.black);

        jScrollPane2.setMinimumSize(new Dimension(24, 24));
        jScrollPane2.setPreferredSize(new Dimension(4, 100));

        jPanel11.setLayout(gridLayout2);
        gridLayout2.setColumns(2);
        gridLayout2.setRows(2);
        jPanel12.setLayout(borderLayout12);
        parmsPanel.setLayout(gridBagLayout1);
        jLabel1.setText("PValue / 1K:");
        pValueField.setMinimumSize(new Dimension(70, 18));
        pValueField.setPreferredSize(new Dimension(70, 18));
        pValueField.setText("0.05");
        jLabel5.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel5.setText("Expected:");
        jLabel6.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel6.setText("Actual:");
        expectedCountBox.setPreferredSize(new Dimension(70, 18));
        expectedCountBox.setEditable(false);
        expectedCountBox.setText("0");
        expectedCountBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expectedCountBox_actionPerformed(e);
            }
        });
        enrichmentBox.setPreferredSize(new Dimension(70, 18));
        enrichmentBox.setEditable(false);
        enrichmentBox.setText("1.0");
        parmsPanel.setBorder(BorderFactory.createEtchedBorder());
        enrichmentLabel.setText("Enrich. p-value:");
        useThresholdCheck.setText("Use Thr.");
        useThresholdCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                useThresholdCheck_actionPerformed(e);
            }
        });
        countBox.setPreferredSize(new Dimension(70, 18));
        countBox.setEditable(false);
        countBox.setText("0");
        thresholdBox.setPreferredSize(new Dimension(70, 18));
        Set13KCheck.setText("13K Set");
        percentSeqMatchBox.setPreferredSize(new Dimension(70, 18));
        percentSeqMatchBox.setEditable(false);
        percentSeqMatchBox.setText("0%");
        jLabel7.setText("Total");
        jLabel9.setText("Sequences");
        expectedSeqCountBox.setEditable(false);
        expectedSeqCountBox.setText("0");
        seqCountBox.setEditable(false);
        seqCountBox.setText("0");
        seqEnrichmentBox.setEditable(false);
        seqEnrichmentBox.setText("1.0");
        stopButton.setText("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });
        iterationBox.setText("10");
        jLabel8.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel8.setText("Iterations:");
        jLabel10.setText("5\'");
        jLabel11.setText("3\'");
        jLabel12.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel12.setText("Expected::");
        match5PrimeExpectBox.setEditable(false);
        match5PrimeExpectBox.setText("0");
        match3PrimeExpectBox.setEditable(false);
        match3PrimeExpectBox.setText("0");
        jLabel13.setText("Total");
        jLabel14.setText("Sequence");
        match5PrimeActualBox.setEditable(false);
        match5PrimeActualBox.setText("0");
        jLabel15.setHorizontalAlignment(SwingConstants.TRAILING);
        jLabel15.setText("Actual:");
        match3PrimeActualBox.setEditable(false);
        match3PrimeActualBox.setText("0");
        clearButton.setToolTipText("Clear all patterns and transc. factors.");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearButton_actionPerformed(e);
            }
        });
        showTF.setToolTipText("Display transc. factors");
        showTF.setSelected(true);
        showTF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showTF_actionPerformed(e);
            }
        });
        showSeqPattern.setToolTipText("Display patterns.");
        showSeqPattern.setSelected(true);
        showSeqPattern.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSeqPattern_actionPerformed(e);
            }
        });
        jPanel4.add(jScrollPane1, BorderLayout.CENTER);
        jPanel4.add(jLabel4, BorderLayout.NORTH);

        jScrollPane1.getViewport().add(jTranscriptionFactorList, null);
        jPanel1.add(jScrollPane2, BorderLayout.CENTER);
        jPanel1.add(jLabel3, BorderLayout.NORTH);
        jPanel3.add(jPanel4, null);
        jScrollPane2.getViewport().add(jSelectedTFList, null);
        jPanel11.add(displayBtn, null);
        jPanel11.add(jaddNewTFB, null);
        jPanel11.add(jSaveButton, null);
        jPanel11.add(jButton2, null);
        jPanel6.add(winSize, BorderLayout.EAST);
        jPanel6.add(winSizeL, BorderLayout.WEST);
        jPanel5.add(jPanel8, BorderLayout.NORTH);
        jPanel8.add(minOccur, BorderLayout.EAST);
        jPanel8.add(minOccurL, BorderLayout.WEST);
        jPanel5.add(jPanel6, BorderLayout.SOUTH);

        jScrollPane3.getViewport().add(moduleList, null);
        jPanel3.add(jPanel1, null);

        jPanel7.add(jDiscoverModuleBttn, BorderLayout.SOUTH);
        jPanel7.add(jScrollPane3, BorderLayout.CENTER);
        jPanel7.add(jLabel2, BorderLayout.NORTH);

        jPanel9.add(jPanel7, BorderLayout.CENTER);
        jPanel9.add(jPanel5, BorderLayout.SOUTH);

        jPanel10.add(jPanel3, BorderLayout.CENTER);
        jPanel10.add(jPanel11, BorderLayout.SOUTH);

        jTabbedPane1.add(jPanel10, "TF Mapping");
        jTabbedPane1.add(jPanel9, "ModuleDiscovery");

        seqScrollPane.getViewport().add(seqDisPanel, null);
        jToolBar.add(showTF);
        jToolBar.add(showSeqPattern);
        jToolBar.add(clearButton);
        northPanel.setLayout(new BorderLayout());
        northPanel.add(seqScrollPane, BorderLayout.CENTER);
        northPanel.add(jToolBar, BorderLayout.SOUTH);
        jSplitPane1.add(jTabbedPane1, JSplitPane.LEFT);
        jSplitPane1.add(northPanel, JSplitPane.RIGHT);

        jPanel2.add(jSplitPane1, BorderLayout.CENTER);
        this.add(jPanel2, BorderLayout.CENTER);
        jPanel2.add(jPanel12, java.awt.BorderLayout.SOUTH);
        jPanel2.add(jProgressBar1, java.awt.BorderLayout.NORTH);
        jPanel12.add(jInfoPanel, java.awt.BorderLayout.CENTER);
        jPanel12.add(parmsPanel, java.awt.BorderLayout.WEST);
        parmsPanel.add(pValueField,
                       new GridBagConstraints(1, 0, 1, 1, 0.5, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(2, 2, 2, 2), 0, 0));
        parmsPanel.add(jLabel1,
                       new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(jLabel6,
                       new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(jLabel7,
                       new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(jLabel9,
                       new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(stopButton,
                       new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(jLabel5,
                       new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(expectedCountBox,
                       new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(expectedSeqCountBox,
                       new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(countBox,
                       new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 0, 2), 0, 0));
        parmsPanel.add(seqCountBox,
                       new GridBagConstraints(2, 3, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 0, 2), 0, 0));
        parmsPanel.add(jLabel10,
                       new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(jLabel11,
                       new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(match5PrimeExpectBox,
                       new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match3PrimeExpectBox,
                       new GridBagConstraints(2, 5, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(jLabel12,
                       new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(enrichmentBox,
                       new GridBagConstraints(1, 8, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(seqEnrichmentBox,
                       new GridBagConstraints(2, 8, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(jLabel13,
                       new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(jLabel14,
                       new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(enrichmentLabel,
                       new GridBagConstraints(0, 8, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(percentSeqMatchBox,
                       new GridBagConstraints(2, 9, 2, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(thresholdBox,
                       new GridBagConstraints(1, 10, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(useThresholdCheck,
                       new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(0, 2, 0, 2), 0, 0));
        parmsPanel.add(Set13KCheck,
                       new GridBagConstraints(2, 10, 1, 1, 0.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(0, 2, 0, 2), 0, 0));
        parmsPanel.add(jLabel8,
                       new GridBagConstraints(0, 11, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(iterationBox,
                       new GridBagConstraints(1, 11, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(jLabel15,
                       new GridBagConstraints(0, 6, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match5PrimeActualBox,
                       new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match3PrimeActualBox,
                       new GridBagConstraints(2, 6, 1, 1, 1.0, 0.0,
                                              GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        jSplitPane1.setDividerLocation(180);
    }

    /**
     * @todo addto use on transcriptionFactorList
     */
    private DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
            // IGenericMarker stats = (IGenericMarker) value;
            if (isSelected) {

                c.setBackground(Color.yellow);

            } else {

                c.setBackground(Color.white);

            }
            return c;
        }
    }; JPanel jPanel12 = new JPanel();
    BorderLayout borderLayout12 = new BorderLayout();
    JPanel parmsPanel = new JPanel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JTextField pValueField = new JTextField();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JTextField expectedCountBox = new JTextField();
    JLabel jLabel6 = new JLabel();
    JTextField enrichmentBox = new JTextField();
    JLabel enrichmentLabel = new JLabel();
    JTextField countBox = new JTextField();
    JCheckBox useThresholdCheck = new JCheckBox();
    JTextField thresholdBox = new JTextField();
    JCheckBox Set13KCheck = new JCheckBox();
    JTextField percentSeqMatchBox = new JTextField();
    JLabel jLabel7 = new JLabel();
    JLabel jLabel9 = new JLabel();
    JTextField expectedSeqCountBox = new JTextField();
    JTextField seqCountBox = new JTextField();
    JTextField seqEnrichmentBox = new JTextField();
    JButton stopButton = new JButton();
    JTextField iterationBox = new JTextField();
    JLabel jLabel8 = new JLabel();
    JLabel jLabel10 = new JLabel();
    JLabel jLabel11 = new JLabel();
    JTextField match5PrimeExpectBox = new JTextField();
    JTextField match3PrimeExpectBox = new JTextField();
    JLabel jLabel12 = new JLabel();
    JLabel jLabel13 = new JLabel();
    JLabel jLabel14 = new JLabel();
    JTextField match5PrimeActualBox = new JTextField();
    JTextField match3PrimeActualBox = new JTextField();
    JLabel jLabel15 = new JLabel();
    PromoterPatternDB pdb; //= new PromoterPatternDB();
    HashMap<String,
            PromoterPatternDB> map = new HashMap<String, PromoterPatternDB>();

    public void setSequenceDB(DSSequenceSet db2) {
        String id = null;
        if (db2 != null && sequenceDB != null) {
            id = db2.getID();
            if (sequenceDB.getID().equals(id)) {
                return;
            } else if (map.containsKey(id)) {
                    pdb = map.get(id);
                    sequenceDB = db2;
                    seqDisPanel.initialize(sequenceDB);
                    seqDisPanel.setPatternDisplay(pdb.getDisplay());
                    seqDisPanel.setPatternMatches(pdb.getMatches());
                }else{
                    clear();
                    sequenceDB = db2;
                    seqDisPanel.initialize(sequenceDB);

                }
        }else if(db2!=null){
            clear();
                    sequenceDB = db2;
                    seqDisPanel.initialize(sequenceDB);

        }

    }

    /**
     * clear
     */
    public void clear() {
        seqPatterns.clear();
        seqPatternDisplay.clear();
        seqPatternMatches.clear();
        promoterPatterns.clear();
        promoterPatternDisplay.clear();
        promoterPatternMatches.clear();

//        promoterPatterns = new ArrayList<DSPattern>();
//
//           promoterPatternDisplay = new HashMap<DSPattern, Display>();
//
//            promoterPatternMatches = new Hashtable<DSPattern<DSSequence,
//                                     DSSeqRegistration>,
//                                     List<DSPatternMatch<DSSequence,
//                                     DSSeqRegistration>>>();



    }

    public DSSequenceSet getSequenceDB() {
        return sequenceDB;
    }

    void fillupTranscriptionFactorList() throws FileNotFoundException,
            IOException {
        DefaultListModel ls = new DefaultListModel();

        BufferedReader br = null;

        InputStream input2 = PromoterViewPanel.class.getResourceAsStream(
                "MATRIX_ANNOTATION.txt");
        br = new BufferedReader(new InputStreamReader(input2));

        HashMap factors = new HashMap();
        String line = br.readLine();
        while (line != null) {

            String[] cols = line.split("\t");

            HashMap hs = (HashMap) factors.get(cols[0]);
            if (hs == null) {
                hs = new HashMap();
                factors.put(cols[0], hs);
            }
            if (cols.length >= 3) {
                hs.put(cols[1], cols[2]);
            }
            line = br.readLine();

        }
        br.close();
        HashMap mxs = new HashMap();
        BufferedReader b = null;

        InputStream input = PromoterViewPanel.class.getResourceAsStream(
                "MATRIX_DATA.txt");
        b = new BufferedReader(new InputStreamReader(input));

        String oneline = b.readLine();

        while (oneline != null) {
            String[] cls = oneline.split("\t");
            Matrix m = (Matrix) mxs.get(cls[0]);
            if (m == null) {
                char[] sym = {
                             'A', 'C', 'G', 'T'};
                m = new Matrix(sym);
                mxs.put(cls[0], m);
            }
            m.setCounts(cls[1].charAt(0), Integer.parseInt(cls[2]) - 1,
                        Double.parseDouble(cls[3]));
            oneline = b.readLine();
        }
        b.close();
        HashMap map = new HashMap();
        String[] names = new String[factors.size()];
        int i = 0;
        for (Iterator it = factors.keySet().iterator(); it.hasNext(); ) {
            Object id = it.next();
            TranscriptionFactor tf = new TranscriptionFactor();
            HashMap hash = (HashMap) factors.get(id);
            String name = (String) hash.get("name") + ":" +
                          (String) hash.get("class");
            tf.setName(name);
            Matrix mx = (Matrix) mxs.get(id);
            mx.normalize();
            tf.setMatrix(mx);
            //
            names[i++] = name;
            map.put(name, tf);
        }
        Arrays.sort(names);
        for (int k = 0; k < names.length; k++) {
            Object tf = map.get(names[k]);
            ls.addElement(tf);
        }
        jTranscriptionFactorList.setToolTipText("Transcription factor list");
        jTranscriptionFactorList.setModel(ls);
        jTranscriptionFactorList.addMouseListener(new java.awt.event.
                                                  MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jTranscriptionFactorList_mouseClicked(e);
            }

        });

    }

    void displayBtn_actionPerformed(ActionEvent e) {
        this.mappingPatterns();
    }

    void jSaveButton_actionPerformed(ActionEvent e) {
        //this will save the results into a file.
        if (sequenceDB == null) {
            return;
        }
        fc2 = new JFileChooser();
        fc2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() == fc.APPROVE_SELECTION) {
                    File file = fc2.getSelectedFile();
                    try {
                        String comma = ", ";
                        String tab = "\t";
                        BufferedWriter bw = new BufferedWriter(new FileWriter(
                                file));
                        for (DSPattern pattern : promoterPatterns) {
                            bw.write(pattern.toString());
                            bw.newLine();
                            Hashtable<Integer,
                                    DSSequence>
                                    order = new Hashtable<Integer, DSSequence>();
                            Hashtable<DSSequence,
                                    Vector<Integer>>
                                    hitsForPrinting = new Hashtable<DSSequence,
                                    Vector<Integer>>();
                            List<DSPatternMatch<DSSequence, DSSeqRegistration>>
                                    matches = promoterPatternMatches.get(
                                            pattern);
                            for (int i = 0; i < sequenceDB.getSequenceNo(); i++) {
                                DSSequence sequence = sequenceDB.getSequence(i);
                                order.put(i, sequence);
                                hitsForPrinting.put(sequence,
                                        new Vector<Integer>());
                            }
                            for (DSPatternMatch<DSSequence,
                                 DSSeqRegistration> match : matches) {
                                Vector<Integer>
                                        v = hitsForPrinting.get(match.getObject());
                                v.add(match.getRegistration().x1 + 1);
                            }
                            for (int i = 0; i < sequenceDB.getSequenceNo(); i++) {
                                DSSequence sequence = order.get(i);
                                Vector<Integer>
                                        val = hitsForPrinting.get(sequence);
                                String label = sequence.getLabel();
                                bw.write(label.split("\\|")[0] + tab);
                                String positions = "";
                                int c = 0;
                                for (Integer integer : val) {
                                    if (c == 0) {
                                        positions += integer;
                                    } else {
                                        positions += comma + integer;
                                    }
                                    c++;
                                }
                                bw.write(positions);
                                bw.newLine();
                            }
                        }
                        bw.flush();
                        bw.close();
//                        if (sequenceDB.getFile() != null) {
//                            String firstLine = sequenceDB.getFile().
//                                               getAbsolutePath();
//                            bw.write(firstLine);
//                            bw.newLine();
//                        }
//                        bw.write(seqDisPanel.asString());
                        bw.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                }

            }
        });
        fc2.setDialogTitle("Save to file");
        fc2.showSaveDialog(this);
    }

//this will add  new TFs from fasta file
    void jaddNewTFB_actionPerformed(ActionEvent e) {
        fc = new JFileChooser();
        fc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fc_actionPerformed(e);
            }

        });
        fc.setDialogTitle("choose the matrix files");

        String dir = System.getProperty("temporary.files.directory");
        // This is where we store user data information
        String filename = "promoterPanelSettings";
        try {
            File file = new File(dir + File.separator + filename);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                dir = br.readLine();
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (dir == null || dir.equals(".")) {
            dir = System.getProperty("user.dir");
        }
        fc.setCurrentDirectory(new File(dir));

        fc.showOpenDialog(this);

    }

    private void fc_actionPerformed(ActionEvent e) {

        String dir = System.getProperty("temporary.files.directory");
        // This is where we store user data information
        String filename = "promoterPanelSettings";

        if (e.getActionCommand() == fc.APPROVE_SELECTION) {
            File file = fc.getSelectedFile();
            String directory = file.getParent();
            char[] sybol = {
                           'A', 'C', 'G', 'T'};
            Matrix mx = new Matrix(sybol);
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String[] a = br.readLine().trim().split("\t");
                String[] c = br.readLine().trim().split("\t");
                String[] g = br.readLine().trim().split("\t");
                String[] t = br.readLine().trim().split("\t");
                br.close();
                for (int indx = 0; indx < a.length; indx++) {
                    mx.setCounts('A', indx, Double.parseDouble(a[indx]));
                    mx.setCounts('C', indx, Double.parseDouble(c[indx]));
                    mx.setCounts('G', indx, Double.parseDouble(g[indx]));
                    mx.setCounts('T', indx, Double.parseDouble(t[indx]));
                }

                try { //save current settings.
                    BufferedWriter bw = new BufferedWriter(new FileWriter(dir +
                            File.separator + filename));
                    bw.write(directory);
                    bw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

//      mx.train(sec);
            TranscriptionFactor tf = new TranscriptionFactor();
            mx.normalize();
            tf.setMatrix(mx);
            tf.setName(file.getName());
            DefaultListModel ls = (DefaultListModel) jSelectedTFList.getModel();
            ls.addElement(tf);
        }
    }

//    public String asString() {
//        return seqDisPanel.asString();

//    }

    void jTranscriptionFactorList_mouseClicked(MouseEvent e) {
        int index = jTranscriptionFactorList.locationToIndex(e.getPoint());
        if (e.getClickCount() == 2) {
            TranscriptionFactor value = (TranscriptionFactor)
                                        jTranscriptionFactorList.getModel().
                                        getElementAt(index);
            DefaultListModel ls = (DefaultListModel) jSelectedTFList.getModel();
            ls.addElement(value);
        } else {

            TranscriptionFactor pattern = (TranscriptionFactor)
                                          jTranscriptionFactorList.getModel().
                                          getElementAt(index);

            try {
                jInfoPanel.removeAll();
                RenderingHints hints = new RenderingHints(RenderingHints.
                        KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                DistributionLogo[] logos = ((TranscriptionFactor) pattern).
                                           getMatrix().getLogo();
                for (int k = 0; k < logos.length; k++) {

                    jInfoPanel.setLayout(new GridLayout(1, logos.length));
                    logos[k].setPreferredSize(new Dimension(40, 50));
                    logos[k].setRenderingHints(hints);

                    jInfoPanel.add(logos[k]);

                }
                jInfoPanel.revalidate();
                jInfoPanel.repaint();
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
    }

    void jSelectedTFList_mouseClicked(MouseEvent e) {
        int index = jSelectedTFList.locationToIndex(e.getPoint());
        if (e.getClickCount() == 2) {
            ((DefaultListModel) jSelectedTFList.getModel()).removeElementAt(
                    index);
//            mappingPatterns();

        } else {

            TranscriptionFactor pattern = (TranscriptionFactor) jSelectedTFList.
                                          getModel().getElementAt(index);

            try {
                jInfoPanel.removeAll();
                RenderingHints hints = new RenderingHints(RenderingHints.
                        KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                DistributionLogo[] logos = ((TranscriptionFactor) pattern).
                                           getMatrix().getLogo();
                for (int k = 0; k < logos.length; k++) {

                    jInfoPanel.setLayout(new GridLayout(1, logos.length));
                    logos[k].setPreferredSize(new Dimension(40, 50));
                    logos[k].setRenderingHints(hints);

                    jInfoPanel.add(logos[k]);

                }
                jInfoPanel.revalidate();
                jInfoPanel.repaint();
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }

    }

    public void mappingPatterns() {
        if (!isRunning) {
            averageNo = 10;
            try {
                averageNo = Integer.parseInt(iterationBox.getText());
            } catch (NumberFormatException ex) {
            }
            try {
                pValue = Double.parseDouble(pValueField.getText());
            } catch (NumberFormatException ex) {
            }
            if (sequenceDB == null) {
                return;
            }
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setMinimum(0);
            jProgressBar1.setMaximum(100);
            jProgressBar1.setStringPainted(true);

            Thread t = new Thread() {
                public void run() {
                    DecimalFormat format = new DecimalFormat("0.###E0");
                    DecimalFormat formatInt = new DecimalFormat("0");
                    DecimalFormat formatPV = new DecimalFormat("0.0000");
                    ArrayList ar = new ArrayList();
                    DefaultListModel ls = (DefaultListModel) jSelectedTFList.
                                          getModel();
                    double threshold = 0;

                    if (pValue < 0.005) {
                        pValue = 0.05;
                        pValueField.setText(formatPV.format(pValue));
                    }
                    RandomSequenceGenerator rs = new RandomSequenceGenerator(
                            sequenceDB, pValue);

                    int i = 0;
                    MatchStats msActual = new MatchStats();
                    MatchStats msExpect = new MatchStats();
                    int seqNo = 0;
                    int totalLength = 0;
                    for (Enumeration en = ls.elements(); en.hasMoreElements(); ) {

                        TranscriptionFactor pattern = (TranscriptionFactor) en.
                                nextElement();
                        jProgressBar1.setString("Processing :" +
                                                pattern.getName());

                        if (pattern != null) {
                            ar.add(pattern);
                            ScoreStats stats = null;
                            // Load the 13K set if needed
                            if (Set13KCheck.isSelected()) {
                                load13KBSet();
                            } else {
                                background = null;
                            }
                            if (useThresholdCheck.isSelected()) {
                                pValue = 0.05;
                                threshold = Double.parseDouble(thresholdBox.
                                        getText());
                            } else {
                                if (background != null) {
                                    // compute the threshold from the required pValue
                                    // using the predefiedn background database
                                    stats = getThreshold(pattern, background,
                                            pValue);
                                } else {
                                    // compute the threshold from the required pValue
                                    // using a random generative model
                                    stats = getThreshold(pattern, rs, pValue);
                                }
                                // assign the new pValue based on what we could find
                                pValue = stats.pValue;
                                pValueField.setText(formatPV.format(pValue));
                                threshold = stats.score * 0.99;
                            }
                            pattern.setThreshold(threshold);
                            // Lengths are in base pairs (BP) and do not include the reverse
                            // strand. Analysis is the done on both the normal and reverse
                            // strand.
                            int partialLength = 0;
                            List<DSPatternMatch<DSSequence,
                                    DSSeqRegistration>>
                                    matches = new ArrayList<DSPatternMatch<
                                              DSSequence, DSSeqRegistration>>();
                            for (int seqId = 0; seqId < sequenceDB.size();
                                 seqId++) {
                                double progress = (double) seqId /
                                                  (double) sequenceDB.size();
                                updateProgressBar(progress,
                                                  "Discovery: " +
                                                  pattern.getName());
                                DSSequence seq = sequenceDB.getSequence(seqId);
                                // Count the valid positions so that we can compute the background matches
                                // in a meaningful way. E.g. don't count # or stretches that do not contain
                                // valid sequence data
                                int positions = countValid(pattern, seq);
                                if (positions > 10) {
                                    seqNo++;
                                    partialLength += positions;
                                    totalLength += positions;

                                    if (!useThresholdCheck.isSelected()) {
                                        // This assumes that the pvalue has been correctly estimated
                                        // the compute the expected matches from the p-value
                                        int oldMatch = (int) msExpect.matchNo;
                                        msExpect.matchNo += pValue *
                                                (double) (positions) / 1000.0;
                                        msExpect.match5primeNo += pValue *
                                                (double) (positions) / 1000.0 /
                                                2.0;
                                        msExpect.match3primeNo += pValue *
                                                (double) (positions) / 1000.0 /
                                                2.0;
                                        if (msExpect.matchNo - oldMatch >= 1) {
                                            msExpect.matchSeq++;
                                        }
                                    }
                                    List<DSPatternMatch<DSSequence,
                                            DSSeqRegistration>>
                                            seqMatches = pattern.match(seq, 1.0);
                                    if (seqMatches.size() > 0) {
                                        msActual.matchSeq++;
                                    }
                                    matches.addAll(seqMatches);
                                }
                                if (stop) {
                                    return;
                                }
                            }
                            updateProgressBar(1,
                                              "Discovery: " + pattern.getName());
                            if (matches != null) {
                                for (DSPatternMatch<DSSequence,
                                     DSSeqRegistration> match : matches) {
                                    if (match.getRegistration().strand == 0) {
                                        msActual.match5primeNo++;
                                    }
                                    if (match.getRegistration().strand == 1) {
                                        msActual.match3primeNo++;
                                    }
                                    msActual.matchNo++;
                                }
//                                matchCount += matches.size();
                                Display dis = new Display();
                                dis.setColor(PatternOperations.getPatternColor(
                                        pattern.hashCode()));
                                i++;
                                dis.setHeight(0.9);
                                dis.setShape(Display.RECTANGLE);
                                if (showTF.isSelected()) {
                                    seqDisPanel.addAPattern(pattern, dis,
                                            matches);
                                }
                                promoterPatternDisplay.put(pattern, dis);
                                promoterPatternMatches.put(pattern, matches);
                                promoterPatterns.add(pattern);
                            }

                            if (useThresholdCheck.isSelected()) {
                                if (Set13KCheck.isSelected()) {
                                    // using the length of the current sequences as background, determine an appropriate pvalue
                                    // from the 13K Set
                                    getMatchesPerLength(pattern, partialLength,
                                            threshold, background, null,
                                            msExpect);
                                } else {
                                    // using the length of the current sequences as background, determine an appropriate pvalue
                                    // from random data
                                    getMatchesPerLength(pattern, partialLength,
                                            threshold, null, rs, msExpect);
                                }
                            }
                        }
                    }

                    jProgressBar1.setIndeterminate(false);
                    double p = 1.0;
                    if (msExpect.matchNo > 0) {
                        p = msExpect.matchNo / (double) totalLength;
                    }
                    if (useThresholdCheck.isSelected()) {
                        pValueField.setText(formatPV.format(p * 1000));
                    }
                    int percent = (int) (100 * (double) msActual.matchSeq /
                                         (double) seqNo);
                    percentSeqMatchBox.setText(Integer.toString(percent) + "%");
                    int matchCount = (int) msActual.matchNo;
                    double enrichmentPValue = Math.exp(ClusterStatistics.
                            logBinomialDistribution(totalLength, matchCount, p));
                    enrichmentPValue +=
                            Math.exp(ClusterStatistics.
                                     logBinomialDistribution(totalLength,
                            matchCount + 1, p));
                    enrichmentPValue +=
                            Math.exp(ClusterStatistics.
                                     logBinomialDistribution(totalLength,
                            matchCount + 2, p));
                    enrichmentPValue +=
                            Math.exp(ClusterStatistics.
                                     logBinomialDistribution(totalLength,
                            matchCount + 3, p));
                    enrichmentPValue +=
                            Math.exp(ClusterStatistics.
                                     logBinomialDistribution(totalLength,
                            matchCount + 4, p));
                    enrichmentPValue +=
                            Math.exp(ClusterStatistics.
                                     logBinomialDistribution(totalLength,
                            matchCount + 5, p));
                    enrichmentBox.setText(format.format(enrichmentPValue));
                    expectedCountBox.setText(formatPV.format(msExpect.matchNo));
                    expectedSeqCountBox.setText(formatInt.format(msExpect.
                            matchSeq));
                    seqCountBox.setText(formatInt.format(msActual.matchSeq));
                    match5PrimeActualBox.setText(formatInt.format(msActual.
                            match5primeNo));
                    match3PrimeActualBox.setText(formatInt.format(msActual.
                            match3primeNo));
                    match5PrimeExpectBox.setText(formatPV.format(msExpect.
                            match5primeNo));
                    match3PrimeExpectBox.setText(formatPV.format(msExpect.
                            match3primeNo));
                    thresholdBox.setText(format.format(threshold));
                    countBox.setText(formatInt.format(matchCount));
                    updateProgressBar(0, "Done");

                    //update the map, associate the seqenceDB with Patterns and save it to the map
//                    pdb = new PromoterPatternDB(sequenceDB);
//                    pdb.setDisplay(promoterPatternDisplay);
//                    pdb.setMatches(promoterPatternMatches);
//                    map.put(sequenceDB.getID(), pdb);

                }

            };
            t.setPriority(Thread.MIN_PRIORITY);
            stop = false;
            isRunning = false;
            t.start();
        }
    }

    void jButton2_actionPerformed(ActionEvent e) {
        fc = new JFileChooser();
        fc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() == fc.APPROVE_SELECTION) {
                    File data = fc.getSelectedFile();
                    digestInputFile(data);

                }

            }

        });
        fc.setDialogTitle("choose the Transfac file");
        fc.showOpenDialog(this);

    }

//    void jActivateBttn_actionPerformed(ActionEvent e) {
//        String label = JOptionPane.showInputDialog(
//            "Please enter a name for the dataset");
//        if (label != null) {
//            sequenceDB.setLabel(label);
//            ProjectNodeAddedEvent event =
//                new ProjectNodeAddedEvent(promoterView, "message", sequenceDB, null);
//
//            try {
//                promoterView.throwEvent(ProjectNodeAddedListener.class,
//                                        "projectNodeAdded", event);
//            }
//            catch (AppEventListenerException ex) {
//                ex.printStackTrace();
//            }
//        }
//
//    }

    void text_actionPerformed(ActionEvent e) {
        this.selectedRegionChanged = true;
    }

    /**
     * sequenceDiscoveryTableRowSelected
     *
     * @param e SequenceDiscoveryTableEvent
     */

    public void sequenceDiscoveryTableRowSelected(SequenceDiscoveryTableEvent e) {
        /** @todo Fix patterns */
        //clear previously selected discovered patterns
        Vector<DSPattern> tobedeleted = new Vector<DSPattern>(seqPatterns);
//        for (DSPattern<DSSequence, DSSeqRegistration> pattern : seqDisPanel.getPatternMatches().keySet()) {
//            tobedeleted.add(pattern);
//        }
        for (int i = 0; i < tobedeleted.size(); i++) {
            seqDisPanel.removePattern(tobedeleted.get(i));
        }
        //add newly selected patterns
        /** @todo Fix patterns */

        //add the new patterns into seqPatterns
        seqPatterns.clear();
        DSCollection<DSMatchedPattern<DSSequence,
                DSSeqRegistration>> patterns = e.getPatternMatchCollection();
        seqPatterns = new ArrayList(e.getPatternMatchCollection());
        for (DSMatchedPattern<DSSequence, DSSeqRegistration> pattern : patterns) {
            //IGetPatternMatchCollection im = gp.match(sequenceDB);
            Display dis = new Display();
            dis.setColor(PatternOperations.getPatternColor(pattern.hashCode()));
            dis.setHeight(0.9);
            dis.setShape(Display.OVAL);
            DSPattern<DSSequence, DSSeqRegistration> p = pattern.getPattern();
            //set the associated sequenceDB to current sequenceDB.
            if (pattern instanceof CSMatchedSeqPattern) {
                ((CSMatchedSeqPattern) pattern).setSeqDB(sequenceDB);
            }

            List<DSPatternMatch<DSSequence,
                    DSSeqRegistration>> matches = pattern.matches();
            seqPatternDisplay.put(p, dis);
            seqPatternMatches.put(p, matches);

            if (showSeqPattern.isSelected()) {
                seqDisPanel.addAPattern(p, dis, matches);
            }
        }
    }

//    void jButton1_actionPerformed(ActionEvent e) {
//        BufferedWriter br = null;
//        try {
//            br = new BufferedWriter(new FileWriter("testtesttest.txt"));
//        }
//        catch (IOException ex) {
//        }
//        String oneline = "";
//        for (Iterator it = seqDisPanel.getPatternMatchs().keySet().iterator();
//             it.hasNext(); ) {
//
//            IGetPattern pattern = (IGetPattern) it.next();
//            IGetPatternMatchCollection matches = (
//                IGetPatternMatchCollection) seqDisPanel.getPatternMatchs().get(
//                pattern);
//            for (int m = 0; m < matches.size(); m++) {
//                int offset = matches.get(m).getOffset();
//                oneline = oneline + "V$" + pattern.asString() + " " +
//                    offset + "(-);";
//            }
//
//        }
//
//        oneline = oneline + ")\n";
//        try {
//            br.write(oneline);
//        }
//        catch (IOException ex1) {
//        }
//
//    }


    void jDiscoverModuleBttn_actionPerformed(ActionEvent e) {
        try {
//            jButton1_actionPerformed(e); // write the stuff into a file
            discovery();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void discovery() {
        if (sequenceDB == null) {
            return;
        }
        Thread t = new Thread() {
            public void run() {
                Hashtable<DSPattern<DSSequence, DSSeqRegistration>,
                        List<DSPatternMatch<DSSequence,
                        DSSeqRegistration>>>
                        matchesTable = seqDisPanel.getPatternMatches();
                Hashtable patternPrime = new Hashtable();
                Integer n = new Integer(1);
                for (Iterator it = matchesTable.keySet().iterator(); it.hasNext(); ) {
                    n = nextPrime(n);
                    Object o = it.next();
                    patternPrime.put(o, n);
                    patternPrime.put(n, o);
                }

                Discovery.discover(sequenceDB, matchesTable,
                                   Integer.parseInt(minOccur.getText()),
                                   Integer.parseInt(winSize.getText()),
                                   patternPrime);

                Vector modules = Discovery.getFinalPattern();
                modulelistmodel.clear();

                for (Iterator it = modules.iterator(); it.hasNext(); ) {
                    CSMultiSeqPattern pt = (CSMultiSeqPattern) it.next();
                    pt.getPatternKey().getMapping(patternPrime);
                    Display dis = new Display();
                    dis.setColor(PatternOperations.getPatternColor(pt.hashCode()));
                    dis.setHeight(1.1);
                    dis.setShape(Display.ROUNDRECT);
                    PatternDisplay pd = new PatternDisplay(pt, dis);
                    modulelistmodel.addElement(pd);

                }
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private void digestInputFile(File data) {
        sfr = new SequenceFileReader(data);
        HashMap pr = sfr.motiftoPrime;

    }

    void moduleListSelection_action(ListSelectionEvent e) {
        //clear existing composite patterns
        Vector<DSPattern<DSSequence,
                DSSeqRegistration>>
                tobedeleted = new Vector<DSPattern<DSSequence,
                              DSSeqRegistration>>();
        Hashtable<DSPattern<DSSequence, DSSeqRegistration>,
                List<DSPatternMatch<DSSequence,
                DSSeqRegistration>>> matches = seqDisPanel.getPatternMatches();
        for (DSPattern<DSSequence, DSSeqRegistration> pattern : matches.keySet()) {
            if (pattern.getClass().isAssignableFrom(CSMultiSeqPattern.class)) {
                tobedeleted.add(pattern);
            }
        }
        for (DSPattern<DSSequence, DSSeqRegistration> pattern : tobedeleted) {
            seqDisPanel.removePattern(pattern);
        }

        //add selected ones
        Object[] modules = moduleList.getSelectedValues();
        for (int i = 0; i < modules.length; i++) {
            PatternDisplay<DSSequence,
                    DSSeqRegistration> pd = (PatternDisplay) modules[i];
            Object pdPattern = pd.getPt();
            if (pdPattern instanceof CSMultiSeqPattern) {
                CSMultiSeqPattern compPatt = (CSMultiSeqPattern) pdPattern;
                List<DSPatternMatch<DSSequence,
                        DSSeqRegistration>> pc = compPatt.match(sequenceDB);
                for (int k = 0; k < pc.size(); k++) {
                    DSPatternMatch<DSSequence, DSSeqRegistration> pm = pc.get(k);
                    int lastsub = pm.getRegistration().x2;
                    DSPattern[] patterns = ((CSMultiSeqPattern) pd.getPt()).
                                           getPatternKey().subpatterns;
                    for (int m = 0; m < patterns.length; m++) {
                        List<DSPatternMatch<DSSequence,
                                DSSeqRegistration>>
                                p = seqDisPanel.
                                    getPatternMatches().get(patterns[m]);
                        for (int n = 0; n < p.size(); n++) {
                            DSPatternMatch<DSSequence,
                                    DSSeqRegistration> ma = p.get(n);
                            if (ma.getRegistration().x2 == lastsub) {
                                pm.getRegistration().x2 = ma.getRegistration().
                                        x2;
                                break;
                            }
                        }
                    }
                }
                DSPattern<DSSequence,
                        DSSeqRegistration> patternMatch = pd.getPt();
                seqDisPanel.addAPattern(patternMatch, pd.getDis(), pc);
            }
        }
    }

    public static Integer nextPrime(Integer m) {
        int n;
        n = m.intValue();
        if (n <= 1) {
            return new Integer(2);
        }
        if ((++n) % 2 == 0) {
            n++;
        }
        for (; !isPrime(n); n += 2) {
            ;
        }
        return new Integer(n);
    }

    private static boolean isPrime(int n) {
        if (n == 2 || n == 3) {
            return true;
        }
        if (n == 1 || n % 2 == 0) {
            return false;
        }
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }

    void load13KBSet() {
        if (background == null) {
            try {
                URL set13K = new URL(System.getProperty("data.download.site") + "13K.fa");
                File set13KFile = new File(System.getProperty("temporary.files.directory") + "13K.fa");
                if (!set13KFile.exists()){
                    BufferedReader br = new BufferedReader(new InputStreamReader(set13K.openStream()));
                    BufferedWriter bw = new BufferedWriter(new FileWriter(set13KFile));
                    String line = null;
                    while ((line = br.readLine()) != null){
                        bw.write(line + "\n");
                    }
                    bw.flush();
                    bw.close();
                    br.close();
                }
                background = CSSequenceSet.getSequenceDB(set13KFile);
            } catch (MalformedURLException mfe){mfe.printStackTrace();
            } catch (IOException ioe) {ioe.printStackTrace();}
        }
    }

    public void useThresholdCheck_actionPerformed(ActionEvent e) {

    }

    void updateProgressBar(final double percent, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    jProgressBar1.setString(text);
                    jProgressBar1.setValue((int) (percent * 100));
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public ScoreStats getThreshold(TranscriptionFactor pattern,
                                   RandomSequenceGenerator rg, double pValue) {
        // computes the score based on a probability of a match of pValue
        // To get goo statistics, we expect at least 100 matches to exceed
        // the threshold in the null hypothesis. Hence, this is the number
        // of 1KB sequences we must test.
        int overT = 0;
        int seqLen = 1000;
        int seqNo = (int) (100 / pValue);
        double scores[] = new double[seqLen * 3];

        for (int i = 0; i < seqNo; i++) {
            double progress = (double) i / (double) seqNo;
            updateProgressBar(progress, "Computing Null Hypothesis");
            DSSequence sequence = rg.getRandomSequence(seqLen +
                    pattern.getLength());
            overT += pattern.getMatrix().collectSequenceScores(sequence, scores);
        }
        int x = scores.length - 101;
        while ((x < scores.length) && scores[x - 1] == scores[x]) {
            x++;
        }
        if (x >= scores.length) {
            x = scores.length - 101;
            while ((x > scores.length - 1000) && scores[x - 1] == scores[x]) {
                x--;
            }
        }
        ScoreStats stats = new ScoreStats();
        stats.pValue = (double) (scores.length - x - 1) / (double) seqNo;
        stats.score = scores[x];
        return stats;
    }

    public ScoreStats getThreshold(TranscriptionFactor pattern,
                                   DSSequenceSet seqDB, double pValue) {
        // computes the score based on a probability of a match of pValue
        // To get goo statistics, we expect at least 100 matches to exceed
        // the threshold in the null hypothesis. Hence, this is the number
        // of 1KB sequences we must test.

        // Total number of tokens required to compute statistics
        int totalLength = (int) (1000 * 100 / pValue);
        int partialLength = 0;
        int maxSeqLen = 2000;
        double scores[] = new double[maxSeqLen * 3];

        while (partialLength < totalLength) {
            int i = (int) (Math.random() * seqDB.size());
            DSSequence sequence = seqDB.getSequence(i);
            double progress = (double) partialLength / (double) totalLength;
            updateProgressBar(progress, "Computing Null Hypothsis");
            pattern.getMatrix().collectSequenceScores(sequence, scores);
            partialLength += Math.min(countValid(pattern, sequence), maxSeqLen);
        }

        int x = scores.length - 101;
        while ((x < scores.length) && scores[x - 1] == scores[x]) {
            x++;
        }
        if (x >= scores.length) {
            x = scores.length - 101;
            while ((x > scores.length - 1000) && scores[x - 1] == scores[x]) {
                x--;
            }
        }
        ScoreStats stats = new ScoreStats();
        stats.pValue = (double) (scores.length - x - 1) /
                       (double) partialLength * 1000;
        stats.score = scores[x];
        return stats;
    }

    public void getMatchesPerLength(TranscriptionFactor pattern, int length,
                                    double threshold, DSSequenceSet seqDB,
                                    RandomSequenceGenerator rg, MatchStats ms) {
        // Determine the number of iterations so that the statistics are good
        int partialLength = 0;
        int totalLength = length * averageNo;
        while (partialLength < totalLength) {
            int invalidNo = 0;
            double progress = (double) partialLength / (double) totalLength;
            updateProgressBar(progress, "Computing Null Hypothsis");
            DSSequence sequence = null;
            if (seqDB != null) {
                int i = (int) (Math.random() * seqDB.size());
                sequence = seqDB.getSequence(i);
            } else if (rg != null) {
                sequence = rg.getRandomSequence(1000 + pattern.getLength());
            } else {
                return;
            }
            pattern.getMatrix().countSequenceMatches(length, threshold,
                    averageNo, partialLength, sequence, ms);
            partialLength += countValid(pattern, sequence);
        }
        ms.match3primeNo = (int) ms.match3primeNo / (double) averageNo;
        ms.match5primeNo = (int) ms.match5primeNo / (double) averageNo;
        ms.matchNo = (int) ms.matchNo / (double) averageNo;
        ms.matchSeq = (int) ms.matchSeq / (double) averageNo;

    }

    private int countInvalid(TranscriptionFactor tf, DSSequence seq) {
        boolean validRegion = true;
        boolean invalidRegion = false;
        int validRegionLen = 0;
        int pLen = tf.getLength();
        int invalidNo = 0;
        String ascii = seq.getSequence();
        for (int i = 0; i < ascii.length(); i++) {
            char c = Character.toUpperCase(ascii.charAt(i));
            switch (c) {
            case 'A':
            case 'C':
            case 'G':
            case 'T':
            case 'U':
                validRegionLen++;
                if (validRegionLen > pLen) {
                    validRegion = true;
                } else {
                    // Not enough characters yet to match the pattern
                    invalidNo++;
                }
                break;
            default:
                validRegion = false;
                invalidNo++;
            }
        }
        return invalidNo;
    }

    private boolean isBasePair(char c) {
        switch (c) {
        case 'A':
        case 'C':
        case 'G':
        case 'T':
        case 'U':
            return true;
        default:
            return false;
        }
    }

    private int countValid(TranscriptionFactor tf, DSSequence seq) {
        int validPositions = 0;
        int valid = 0;
        int tfLen = tf.getLength();
        String ascii = seq.getSequence();

        if (ascii.length() >= tfLen) {
            for (int i = 0; i < tfLen; i++) {
                char c = Character.toUpperCase(ascii.charAt(i));
                if (isBasePair(c)) {
                    valid++;
                }
            }
            for (int i = tfLen; i < ascii.length(); i++) {
                char c1 = Character.toUpperCase(ascii.charAt(i));
                char c2 = Character.toUpperCase(ascii.charAt(i - tfLen));
                if (isBasePair(c1)) {
                    valid++;
                } else {
                    int x = 1;
                }
                if (isBasePair(c2)) {
                    valid--;
                } else {
                    int x = 1;
                }
                if (valid >= tfLen) {
                    validPositions++;
                }
            }
        }
        return validPositions;
    }

    public void jButton1_actionPerformed(ActionEvent e) {
        stop = true;
    }

    public void expectedCountBox_actionPerformed(ActionEvent e) {

    }

    public void clearButton_actionPerformed(ActionEvent e) {
        seqDisPanel.initialize(sequenceDB);
    }

    public void showTF_actionPerformed(ActionEvent e) {
        if (showTF.isSelected()) {
            for (DSPattern pattern : promoterPatterns) {
                seqDisPanel.addAPattern(pattern,
                                        promoterPatternDisplay.get(pattern),
                                        promoterPatternMatches.get(pattern));
            }
        } else {
            for (DSPattern pattern : promoterPatterns) {
                seqDisPanel.removePattern(pattern);
            }

        }
    }

    public void showSeqPattern_actionPerformed(ActionEvent e) {
        if (showSeqPattern.isSelected()) {
            for (DSPattern pattern : seqPatterns) {
                seqDisPanel.addAPattern(pattern,
                                        seqPatternDisplay.get(pattern),
                                        seqPatternMatches.get(pattern));
            }
        } else {
            for (DSPattern pattern : seqPatterns) {
                seqDisPanel.removePattern(pattern);
            }

        }
    }
}
