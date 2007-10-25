package org.geworkbench.components.promoter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.pattern.*;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.
        CSSeqRegistration;
import org.geworkbench.components.promoter.modulediscovery.*;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.SequenceDiscoveryTableEvent;
import org.geworkbench.util.JAutoList;
import org.geworkbench.util.RandomSequenceGenerator;
import org.geworkbench.util.associationdiscovery.statistics.ClusterStatistics;
import org.geworkbench.util.patterns.*;
import org.geworkbench.util.promoter.SequencePatternDisplayPanel;
import org.geworkbench.util.promoter.pattern.Display;
import org.geworkbench.util.promoter.pattern.PatternDisplay;
import java.awt.image.BufferedImage;
import com.larvalabs.chart.PSAMPlot;


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


    private PromoterView promoterView;
    DSPanel<DSGeneMarker> markers = null;
    private DSSequenceSet sequenceDB = null;
    private JFileChooser fc = null;
    private JFileChooser fc2 = null;
    boolean selectedRegionChanged = false;
    //JTextPane jInfoPanel = new JTextPane();
    JPanel jInfoPanel = new ImagePanel();
    DSSequenceSet background = null;
    private TreeMap<String,
            TranscriptionFactor> tfMap = new TreeMap<String,
                                         TranscriptionFactor>();
    //Layouts

    private BorderLayout borderLayout2 = new BorderLayout();
    //Panels and Panes
    private JScrollPane seqScrollPane = new JScrollPane();
    private SequencePatternDisplayPanel seqDisPanel = new
            SequencePatternDisplayPanel();
    JPanel jPanel2 = new JPanel();
    JPanel logoPanel = new JPanel();
    TFListModel tfListModel = new TFListModel();
    Vector tfNameSet;
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
    JButton reverseButton = new JButton("Reverse complement");
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
    JLabel noTFLabel = new JLabel("No TF is selected.");
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
    JPopupMenu itemListPopup = new JPopupMenu();
    JPopupMenu selectedItemListPopup = new JPopupMenu();
    protected JMenuItem addToPanelItem = new JMenuItem("Add");
    protected JMenuItem clearSelectionItem = new JMenuItem(
            "Remove from Selected List.");
    protected JMenuItem saveItem = new JMenuItem(
            "Save");
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
    Hashtable<DSPattern<DSSequence, CSSeqRegistration>, List<DSPatternMatch<DSSequence, CSSeqRegistration>>>
            seqPatternMatches = new Hashtable<DSPattern<DSSequence,
                                CSSeqRegistration>,
                                List<DSPatternMatch<DSSequence,
                                CSSeqRegistration>>>();
    HashMap<DSPattern,
            Display> promoterPatternDisplay = new HashMap<DSPattern, Display>();
    Hashtable<DSPattern<DSSequence, CSSeqRegistration>, List<DSPatternMatch<DSSequence, CSSeqRegistration>>>
            promoterPatternMatches = new Hashtable<DSPattern<DSSequence,
                                     CSSeqRegistration>,
                                     List<DSPatternMatch<DSSequence,
                                     CSSeqRegistration>>>();
    private final static int SEQUENCE = 2;
    private final static int LOGO = 0;
    private final static int PARAMETERS = 1;
    protected JMenuItem imageSnapShotItem = new JMenuItem("Image SnapShot");
    private HashMap primerToMotif = new HashMap();
    private boolean fivePrimerDirection = true;
    private TranscriptionFactor currentTF;

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
        jInfoPanel.setMinimumSize(new Dimension(200, 100));
        jInfoPanel.setOpaque(true);
        jInfoPanel.setPreferredSize(new Dimension(20, 60));
        //jInfoPanel.addHyperlinkListener(this);
        seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
        seqScrollPane.setMaximumSize(new Dimension(32767, 32767));
        seqScrollPane.setMinimumSize(new Dimension(24, 24));
        seqScrollPane.setPreferredSize(new Dimension(250, 250));
        //seqDisPanel.setInfoPanel(jInfoPanel);
        jSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jSplitPane1.setDividerSize(4);
        jSplitPane1.setLastDividerLocation(245);
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
        matrixDetailButton.setText("Matrix Details");
        matrixDetailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                matrixDetailButton_actionPerformed(e);
            }
        });
        matrixPane.setVerticalScrollBarPolicy(JScrollPane.
                                              VERTICAL_SCROLLBAR_ALWAYS);
        matrixPane.setViewportBorder(BorderFactory.createRaisedBevelBorder());
        matrixPane.setMinimumSize(new Dimension(50, 100));
        matrixPane.setPreferredSize(new Dimension(100, 100));
        matrixPane.setToolTipText("Detaill");
        reverseButton = new JButton("Reverse complement");
        reverseButton.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {

                reverseButton_actionPerformed(e);
            }
        });

        //jEditorPane1.add(matrixTable);
        itemListPopup.add(addToPanelItem);
        selectedItemListPopup.add(clearSelectionItem);
        clearSelectionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearSelectionPressed();
            }

        });
        addToPanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addSelectionPressed();
            }

        });
        saveItem = new JMenuItem(
                "Save");
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveResultToAFile();
            }

        });

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
                saveResultToAFile();
            }
        });
        jaddNewTFB.setMaximumSize(new Dimension(100, 27));
        jaddNewTFB.setMinimumSize(new Dimension(73, 27));
        jaddNewTFB.setToolTipText("Add a new transcription factor.");
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
                int index = jSelectedTFList.locationToIndex(e.getPoint());

                if (e.isMetaDown()) {
                    selectedItemRightClicked(index, e);

                } else {

                    jSelectedTFList_mouseClicked(e);
                }
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
        gridLayout2.setColumns(3);
        gridLayout2.setRows(2);
        parmsPanel.setLayout(gridBagLayout1);
        pValueLabel.setText("PValue / 1K:");
        pValueField.setMinimumSize(new Dimension(70, 18));
        pValueField.setPreferredSize(new Dimension(70, 18));
        pValueField.setText("0.05");
        //expectedLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
        expectedLabel1.setHorizontalTextPosition(SwingConstants.LEADING);
        expectedLabel1.setText("Expected:");
        //actualLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
        actualLabel1.setText("Actual:");
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
        parmsPanel.setMinimumSize(new Dimension(130, 100));
        parmsPanel.setPreferredSize(new Dimension(130, 100));
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
        totalLabel1.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        totalLabel1.setText("Total hits");
        sequenceLabel1.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        sequenceLabel1.setHorizontalTextPosition(SwingConstants.LEADING);
        sequenceLabel1.setText("Sequences with hits");
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
        iterationBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                iterationBox_actionPerformed(e);
            }
        });
        //iterationLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        iterationLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        iterationLabel.setText("Iterations:");
         primer5Label.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
          primer3Label.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        primer5Label.setText("5\' hits");
        primer3Label.setText("3\' hits");
        //expect2Label.setHorizontalAlignment(SwingConstants.TRAILING);
        expect2Label.setText("Expected:");
        match5PrimeExpectBox.setEditable(false);
        match5PrimeExpectBox.setText("0");
        match3PrimeExpectBox.setEditable(false);
        match3PrimeExpectBox.setText("0");
//        totalLabel.setText("Total");
//        sequenceLabel.setText("Sequence");
        match5PrimeActualBox.setEditable(false);
        match5PrimeActualBox.setText("0");
        //actualLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        actualLabel.setText("Actual:");
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
        parametersLabel.setFont(new java.awt.Font("Times New Roman", Font.BOLD,
                                                  12));
        parametersLabel.setText("Paramters:");
        expectedCountBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                expectedCountBox_actionPerformed(e);
            }
        });
        pValueField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pValueField_actionPerformed(e);
            }
        });
        resultLabel.setFont(new java.awt.Font("Times New Roman", Font.BOLD, 12));
        resultLabel.setToolTipText("");
        resultLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        resultLabel.setText("Results:");
        jLabel1.setText("% with hits:");
        jPanel12.setLayout(borderLayout12);
        jPanel4.add(jScrollPane1, BorderLayout.CENTER);
        jPanel4.add(jLabel4, BorderLayout.NORTH);

        jScrollPane1.getViewport().add(jTranscriptionFactorList, null);
        jPanel1.add(jScrollPane2, BorderLayout.CENTER);
        jPanel1.add(jLabel3, BorderLayout.NORTH);
        //replace The old TF list Panel with JAutoList
        //jPanel3.add(jPanel4, null);
        tfListModel = new TFListModel();
        tfListModel.refresh();
        tfPanel = new TFListPanel();
        jPanel3.add(tfPanel, null);

        jScrollPane2.getViewport().add(jSelectedTFList, null);
        jPanel11.add(displayBtn, null);
        jPanel11.add(jaddNewTFB, null);
        jPanel11.add(jSaveButton, null);
        jPanel11.add(jButton2, null);
        jPanel11.add(stopButton, null);
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
        //jTabbedPane1.add(jPanel9, "ModuleDiscovery");

        seqDisPanel.addToolBarButton(showSeqPattern);
        seqDisPanel.addToolBarButton(showTF);
        seqDisPanel.addToolBarButton(clearButton);
        logoPanel = new JPanel();
        logoPanel.setLayout(new BorderLayout());
        logoPanel.add(jInfoPanel, BorderLayout.NORTH);
        matrixDisplayPanel = new JPanel();
        logoPanel.add(matrixDisplayPanel, BorderLayout.CENTER);
        matrixDisplayPanel.add(noTFLabel);
        //matrixDisplayPanel.add(matrixPane);
        jTabbedPane2.add(logoPanel, "Logo");
        jTabbedPane2.add(jPanel12, "Parameters");
        northPanel.setLayout(new BorderLayout());
        northPanel.add(seqScrollPane, BorderLayout.CENTER);
        // northPanel.add(jToolBar, BorderLayout.SOUTH);
        seqScrollPane.getViewport().add(seqDisPanel, null);
        jSplitPane1.add(jTabbedPane1, JSplitPane.LEFT);
        jPanel2.add(jSplitPane1, BorderLayout.CENTER);
        this.add(jPanel2, BorderLayout.CENTER);
        jPanel2.add(jProgressBar1, java.awt.BorderLayout.NORTH);
        jSplitPane1.add(jTabbedPane2, JSplitPane.RIGHT);
        jTabbedPane2.add(northPanel, "Sequence");
        jPanel12.add(parmsPanel, java.awt.BorderLayout.CENTER);
        parmsPanel.add(actualLabel1,
                       new GridBagConstraints(1, 8, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(totalLabel1, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(sequenceLabel1,
                       new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(expectedLabel1,
                       new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(expectedCountBox,
                       new GridBagConstraints(2, 7, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(expectedSeqCountBox,
                       new GridBagConstraints(3, 7, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(countBox, new GridBagConstraints(2, 8, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(1, 2, 0, 2), 0, 0));
        parmsPanel.add(seqCountBox, new GridBagConstraints(3, 8, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(1, 2, 0, 2), 0, 0));

          parmsPanel.add(enrichmentBox,
                       new GridBagConstraints(2, 9, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(seqEnrichmentBox,
                       new GridBagConstraints(3, 9, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(enrichmentLabel,
                       new GridBagConstraints(1, 9, 2, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

          parmsPanel.add(percentSeqMatchBox,
                       new GridBagConstraints(3, 10, 2, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

          parmsPanel.add(jLabel1, new GridBagConstraints(1, 10, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));

        parmsPanel.add(primer5Label,
                       new GridBagConstraints(2, 11, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(primer3Label,
                       new GridBagConstraints(3, 11, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(match5PrimeExpectBox,
                       new GridBagConstraints(2, 12, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match3PrimeExpectBox,
                       new GridBagConstraints(3, 12, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

                parmsPanel.add(expect2Label,
                       new GridBagConstraints(1, 12, 2, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

        parmsPanel.add(actualLabel,
                       new GridBagConstraints(1, 13, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match5PrimeActualBox,
                       new GridBagConstraints(2, 13, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match3PrimeActualBox,
                       new GridBagConstraints(3, 13, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(thresholdBox,
                       new GridBagConstraints(2, 2, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(useThresholdCheck,
                       new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(0, 2, 0, 2), 0, 0));
        parmsPanel.add(Set13KCheck, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(0, 2, 0, 2), 0, 0));
        parmsPanel.add(iterationBox,
                       new GridBagConstraints(2, 3, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(iterationLabel,
                       new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(pValueLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(pValueField, new GridBagConstraints(2, 1, 1, 1, 0.5, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(1, 2, 1, 2), 0, 0));


        parmsPanel.add(jLabel5, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(jLabel6, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(resultLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(resultLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
                , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(20, 0, 10, 0), 0, 0));
        parmsPanel.add(parametersLabel,
                       new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.NORTHWEST,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 10, 0), 0, 0));
        jSplitPane1.setDividerLocation(245);
        jTabbedPane2.setSelectedIndex(SEQUENCE);
        imageSnapShotItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createImageSnapshot();
            }
        });

        seqDisPanel.addMenuItem(imageSnapShotItem);
        seqDisPanel.addMenuItem(saveItem);
    }

    public org.geworkbench.events.ImageSnapshotEvent
            createImageSnapshot() {
        JPanel graph = seqDisPanel.getSeqViewWPanel();
        Dimension panelSize = graph.getSize();
        BufferedImage image = new BufferedImage(panelSize.width,
                                                panelSize.height,
                                                BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        graph.paint(g);
        ImageIcon icon = new ImageIcon(image, "Promoter");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.
                events.ImageSnapshotEvent("Promoter Snapshot", icon,
                                          org.geworkbench.events.
                                          ImageSnapshotEvent.Action.SAVE);
        promoterView.createImageSnapshot(event);
        return event;
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
    JPanel parmsPanel = new JPanel();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JTextField pValueField = new JTextField();
    JLabel pValueLabel = new JLabel();
    JLabel expectedLabel1 = new JLabel();
    JTextField expectedCountBox = new JTextField();
    JLabel actualLabel1 = new JLabel();
    JTextField enrichmentBox = new JTextField();
    JLabel enrichmentLabel = new JLabel();
    JTextField countBox = new JTextField();
    JCheckBox useThresholdCheck = new JCheckBox();
    JTextField thresholdBox = new JTextField();
    JCheckBox Set13KCheck = new JCheckBox();
    JTextField percentSeqMatchBox = new JTextField();
    JLabel totalLabel1 = new JLabel();
    JLabel sequenceLabel1 = new JLabel();
    JTextField expectedSeqCountBox = new JTextField();
    JTextField seqCountBox = new JTextField();
    JTextField seqEnrichmentBox = new JTextField();
    JButton stopButton = new JButton();
    JTextField iterationBox = new JTextField();
    JLabel iterationLabel = new JLabel();
    JLabel primer5Label = new JLabel();
    JLabel primer3Label = new JLabel();
    JTextField match5PrimeExpectBox = new JTextField();
    JTextField match3PrimeExpectBox = new JTextField();
    JLabel expect2Label = new JLabel();
    JLabel totalLabel = new JLabel();
    JLabel sequenceLabel = new JLabel();
    JTextField match5PrimeActualBox = new JTextField();
    JTextField match3PrimeActualBox = new JTextField();
    JLabel actualLabel = new JLabel();
    PromoterPatternDB pdb; //= new PromoterPatternDB();
    HashMap<String,
            PromoterPatternDB> map = new HashMap<String, PromoterPatternDB>();
    private JTabbedPane jTabbedPane2 = new JTabbedPane();
    private JLabel parametersLabel = new JLabel();
    private JLabel resultLabel = new JLabel();
    private JLabel jLabel1 = new JLabel();
    private BorderLayout borderLayout12 = new BorderLayout();
    private JLabel jLabel5 = new JLabel();
    private JLabel jLabel6 = new JLabel();
    private TFListPanel tfPanel;
    private JPanel matrixDisplayPanel = new JPanel();
    private JButton matrixDetailButton = new JButton();
    private MatrixScrollPane matrixPane = new MatrixScrollPane();
    private TitledBorder titledBorder6 = new TitledBorder("");
    // private JTable matrixTable = new MatrixTable();
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
            } else {
                clear();
                sequenceDB = db2;
                seqDisPanel.initialize(sequenceDB);

            }
        } else if (db2 != null) {
            clear();
            sequenceDB = db2;
            seqDisPanel.initialize(sequenceDB);

        }

    }

    public void setPromterView(PromoterView promterView) {
        this.promoterView = promterView;
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
//                                     CSSeqRegistration>,
//                                     List<DSPatternMatch<DSSequence,
//                                     CSSeqRegistration>>>();



    }

    public DSSequenceSet getSequenceDB() {
        return sequenceDB;
    }

    public PromoterView getPromterView() {
        return promoterView;
    }

    void fillupTranscriptionFactorList() throws FileNotFoundException,
            IOException {
        if (tfMap == null) {
            tfMap = new TreeMap();
        }
        //DefaultListModel ls = new DefaultListModel();

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

        tfNameSet = new Vector<String>();
        //tfNameSet.add(); = "";
        int i = 0;
        for (Iterator it = factors.keySet().iterator(); it.hasNext(); ) {
            Object id = it.next();
            TranscriptionFactor tf = new TranscriptionFactor();
            HashMap hash = (HashMap) factors.get(id);
            String name = (String) hash.get("name") + ":" +
                          (String) hash.get("class") + ":" + id;
            tf.setName(name);
            tf.setJasparID(id.toString());
            Matrix mx = (Matrix) mxs.get(id);
            mx.normalize();
            tf.setMatrix(mx);
            //
            tfNameSet.add(name);
            i++;
            tfMap.put(name, tf);
        }
        Collections.sort(tfNameSet);
//        Arrays.sort(names);
//        for (int k = 0; k < names.length; k++) {
//            Object tf = map.get(names[k]);
//            tfListModel.addElement(tf);
//        }
//        jTranscriptionFactorList.setToolTipText("Transcription factor list");
//        jTranscriptionFactorList.setModel(tfListModel);
//        jTranscriptionFactorList.addMouseListener(new java.awt.event.
//                                                  MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                jTranscriptionFactorList_mouseClicked(e);
//            }
//
//        });

    }

    void displayBtn_actionPerformed(ActionEvent e) {

        DefaultListModel ls = (DefaultListModel)
                              jSelectedTFList.
                              getModel();

        if (sequenceDB == null) {
            return;
        }
        if (!sequenceDB.isDNA()) {
            JOptionPane.showMessageDialog(null,
                                          "It looks like you are trying to find TF from protein seqeunces. Please load a DNA sequence first.",
                                          "Please check",
                                          JOptionPane.ERROR_MESSAGE);
            return;

        }

        if (ls.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                                          "No Transcription Factor is Selected.",
                                          "Please check",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.mappingPatterns();
        jTabbedPane2.setSelectedIndex(SEQUENCE);
    }

    void saveResultToAFile() {
        //this will save the results into a file.
        if (sequenceDB == null) {
            return;
        }
        final HashMap<CSSequence,
                PatternSequenceDisplayUtil>
                tfPatterns = seqDisPanel.getPatternTFMatches();
        final Set<CSSequence> keySet = tfPatterns.keySet();
        if (tfPatterns == null || keySet == null || keySet.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No pattern is detected.",
                                          "Please check",
                                          JOptionPane.ERROR_MESSAGE);
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

                        for (CSSequence sequence : keySet) {
                            bw.write(sequence.getLabel());
                            bw.write(tab);
                            PatternSequenceDisplayUtil pu = tfPatterns.get(
                                    sequence);
                            TreeSet<PatternLocations>
                                    patternsPerSequence = pu.getTreeSet();
                            if (patternsPerSequence != null &&
                                patternsPerSequence.size() > 0) {
                                for (PatternLocations pl : patternsPerSequence) {
                                    CSSeqRegistration registration = pl.
                                            getRegistration();
                                    if (registration != null) {

                                        CSSeqRegistration seqReg = (
                                                CSSeqRegistration) registration;
                                        bw.write(pl.getAscii() + tab);
                                        if (seqReg.strand == 0){
                                            bw.write(seqReg.x1 + 1 + tab);
                                            bw.write(seqReg.x2 + 1 + tab);
                                        } else if (seqReg.strand == 1){
                                            bw.write(seqReg.x2 + 1 + tab);
                                            bw.write(seqReg.x1 + 1 + tab);
                                        }
                                    }
                                }
                            }

                            bw.newLine();
                        }

                        bw.flush();
                        bw.close();
//
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

            // mx.train(sec);
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

    void jTranscriptionFactorList_mouseClicked(int index, MouseEvent e) {

    }

    private void drawLogo(TranscriptionFactor pattern) throws Exception {

        try {
            jInfoPanel.removeAll();
            logoPanel.removeAll();
            String id = pattern.getJasparID();

//            String iniURL = "http://jaspar.cgb.ki.se//TEMP/" +
//                            pattern.getJasparID().trim() + "_BIG.png";
            jInfoPanel = new ImagePanel();
            matrixDisplayPanel.removeAll();
            matrixDisplayPanel.setLayout(new BorderLayout());
            ((ImagePanel) jInfoPanel).setImage(pattern.getMatrix().
                                               getScores());
            JTable table = matrixPane.createMatrixTable(pattern.getMatrix());
            // setting the size of the table and its columns
            table.setPreferredScrollableViewportSize(new Dimension(800, 100));
            //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            matrixDisplayPanel.add(new JScrollPane(table),
                                   BorderLayout.CENTER);
            logoPanel.add(jInfoPanel, BorderLayout.NORTH);
            logoPanel.add(matrixDisplayPanel, BorderLayout.CENTER);

            this.repaint();
            //jTabbedPane2.setSelectedIndex(LOGO);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
//        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//            try {
//                BrowserLauncher.openURL(event.getURL().toString());
//                //singleAlignmentArea.setPage(event.getURL());
//                //urlField.setText(event.getURL().toExternalForm());
//            } catch (IOException ioe) {
//
//            }
//        }
//
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
//            try {
//               // jInfoPanel.setPage(event.getURL());
//            } catch (IOException ioe) {
//                // Some warning to user
//            }
        }

    }

    void jSelectedTFList_mouseClicked(MouseEvent e) {

        int index = jSelectedTFList.locationToIndex(e.getPoint());
        if (index < 0) {
            return;
        }
        TranscriptionFactor pattern = (TranscriptionFactor)
                                      jSelectedTFList.
                                      getModel().getElementAt(index);
        currentTF = pattern;
        if (e.getClickCount() == 2) {
            ((DefaultListModel) jSelectedTFList.getModel()).removeElementAt(
                    index);
            tfListModel.addElement(pattern);
//            mappingPatterns();

        } else {

            try {
                drawLogo(pattern);
//                jInfoPanel.removeAll();
//                RenderingHints hints = new RenderingHints(RenderingHints.
//                        KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//                DistributionLogo[] logos = ((TranscriptionFactor) pattern).
//                                           getMatrix().getLogo();
//                for (int k = 0; k < logos.length; k++) {
//
//                    jInfoPanel.setLayout(new GridLayout(1, logos.length));
//                    logos[k].setPreferredSize(new Dimension(40, 50));
//                    logos[k].setRenderingHints(hints);
//
//                    jInfoPanel.add(logos[k]);
//
//                }
//                jInfoPanel.revalidate();
//                jInfoPanel.repaint();
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }

    }

    /**
     * Begin to scan the sequences.
     */
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
                    DefaultListModel ls = (DefaultListModel)
                                          jSelectedTFList.
                                          getModel();
                    double threshold = 0;

                    if (pValue < 0.005) {
                        pValue = 0.05;
                        pValueField.setText(formatPV.format(pValue));
                    }
                    RandomSequenceGenerator rs = new
                                                 RandomSequenceGenerator(
                            sequenceDB, pValue);

                    int i = 0;
                    MatchStats msActual = new MatchStats();
                    MatchStats msExpect = new MatchStats();
                    int seqNo = 0;
                    int totalLength = 0;
                    for (Enumeration en = ls.elements(); en.hasMoreElements(); ) {

                        TranscriptionFactor pattern = (TranscriptionFactor)
                                en.
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
                                    stats = getThreshold(pattern,
                                            background,
                                            pValue);
                                } else {
                                    // compute the threshold from the required pValue
                                    // using a random generative model
                                    stats = getThreshold(pattern, rs,
                                            pValue);
                                }
                                // assign the new pValue based on what we could find
                                if (stats != null) {
                                    pValue = stats.pValue;
                                    pValueField.setText(formatPV.format(
                                            pValue));
                                    threshold = stats.score * 0.99;
                                } else {
                                    //stopped.
                                    updateProgressBar(1,
                                            "Stopped on " + new Date());
                                    return;
                                }
                            }
                            pattern.setThreshold(threshold);
                            // Lengths are in base pairs (BP) and do not include the reverse
                            // strand. Analysis is the done on both the normal and reverse
                            // strand.
                            int partialLength = 0;
                            List<DSPatternMatch<DSSequence,
                                    CSSeqRegistration>>
                                    matches = new ArrayList<DSPatternMatch<
                                              DSSequence, CSSeqRegistration>>();
                            for (int seqId = 0; seqId < sequenceDB.size();
                                             seqId++) {
                                double progress = (double) seqId /
                                                  (double) sequenceDB.size();
                                updateProgressBar(progress,
                                                  "Discovery: " +
                                                  pattern.getName());
                                DSSequence seq = sequenceDB.getSequence(
                                        seqId);
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
                                        int oldMatch = (int) msExpect.
                                                matchNo;
                                        msExpect.matchNo += pValue *
                                                (double) (positions) /
                                                1000.0;
                                        msExpect.match5primeNo += pValue *
                                                (double) (positions) /
                                                1000.0 /
                                                2.0;
                                        msExpect.match3primeNo += pValue *
                                                (double) (positions) /
                                                1000.0 /
                                                2.0;
                                        if (msExpect.matchNo - oldMatch >=
                                            1) {
                                            msExpect.matchSeq++;
                                        }
                                    }
                                    List<DSPatternMatch<DSSequence,
                                            CSSeqRegistration>>
                                            seqMatches = pattern.match(seq,
                                            1.0);
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
                                              "Discovery: " +
                                              pattern.getName());
                            if (matches != null) {
                                for (DSPatternMatch<DSSequence, CSSeqRegistration> match : matches) {
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
                                dis.setColor(PatternOperations.
                                             getPatternColor(
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
                                    getMatchesPerLength(pattern,
                                            partialLength,
                                            threshold, background, null,
                                            msExpect);
                                } else {
                                    // using the length of the current sequences as background, determine an appropriate pvalue
                                    // from random data
                                    getMatchesPerLength(pattern,
                                            partialLength,
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
                    percentSeqMatchBox.setText(Integer.toString(percent) +
                                               "%");
                    int matchCount = (int) msActual.matchNo;
                    double enrichmentPValue = Math.exp(ClusterStatistics.
                            logBinomialDistribution(totalLength, matchCount,
                            p));
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
                    expectedCountBox.setText(formatPV.format(msExpect.
                            matchNo));
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

    public void sequenceDiscoveryTableRowSelected(
            SequenceDiscoveryTableEvent e) {
        updateParameters();
        seqDisPanel.patternSelectionHasChanged(e);
        /** @todo Fix patterns */
        //clear previously selected discovered patterns
        Vector<DSPattern> tobedeleted = new Vector<DSPattern>(seqPatterns);
//        for (DSPattern<DSSequence, CSSeqRegistration> pattern : seqDisPanel.getPatternMatches().keySet()) {
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
                CSSeqRegistration>> patterns = e.getPatternMatchCollection();
        seqPatterns = new ArrayList(e.getPatternMatchCollection());
        for (DSMatchedPattern<DSSequence, CSSeqRegistration> pattern : patterns) {
            //IGetPatternMatchCollection im = gp.match(sequenceDB);
            Display dis = new Display();
            dis.setColor(PatternOperations.getPatternColor(pattern.hashCode()));
            dis.setHeight(0.9);
            dis.setShape(Display.OVAL);
            DSPattern<DSSequence, CSSeqRegistration> p = pattern.getPattern();
            //set the associated sequenceDB to current sequenceDB.
            if (pattern instanceof CSMatchedSeqPattern) {
                ((CSMatchedSeqPattern) pattern).setSeqDB(sequenceDB);
            }

            List<DSPatternMatch<DSSequence,
                    CSSeqRegistration>> matches = pattern.matches();
            seqPatternDisplay.put(p, dis);
            seqPatternMatches.put(p, matches);

//            if (showSeqPattern.isSelected()) {
//                seqDisPanel.addAPattern(p, dis, matches);
//            }
        }
    }

    private void updateParameters() {
        seqDisPanel.setDisplaySeqPattern(showSeqPattern.isSelected());
        seqDisPanel.setDisplayTF(showTF.isSelected());
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
                Hashtable<DSPattern<DSSequence, CSSeqRegistration>,
                        List<DSPatternMatch<DSSequence,
                        CSSeqRegistration>>>
                        matchesTable = seqDisPanel.getPatternMatches();
                Hashtable patternPrime = new Hashtable();
                Integer n = new Integer(1);
                for (Iterator it = matchesTable.keySet().iterator();
                                   it.hasNext(); ) {
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
                    dis.setColor(PatternOperations.getPatternColor(pt.
                            hashCode()));
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

    protected void clearSelectionPressed() {
        int[] indices = jSelectedTFList.getSelectedIndices();
        Object[] selectedTFs = jSelectedTFList.getSelectedValues();
        if (indices.length > 0) {
            for (int i = indices.length - 1; i >= 0; i--) {
                ((DefaultListModel) jSelectedTFList.getModel()).
                        removeElementAt(
                                indices[i]);
            }
            for (Object ob : selectedTFs) {
                tfListModel.addElement(ob);
            }

        }
    }

    protected void addSelectionPressed() {
        int[] indices = tfPanel.getList().getSelectedIndices();
        if (indices.length > 0) {

            for (int i = indices.length - 1; i >= 0; i--) {
                addSelectedTF(indices[i]);
            }

        } else {
            JOptionPane.showMessageDialog(this,
                                          "Please select at least one TF first.");
        }
    }

    protected void addSelectedTF(int index) {
        String tfName = (String) tfListModel.getElementAt(index);
        tfListModel.remove(index);
        TranscriptionFactor value = tfMap.get(tfName);
        DefaultListModel ls = (DefaultListModel) jSelectedTFList.
                              getModel();
        if (!ls.contains(value)) {
            ls.addElement(value);
        }
    }

    void moduleListSelection_action(ListSelectionEvent e) {
        //clear existing composite patterns
        Vector<DSPattern<DSSequence,
                CSSeqRegistration>>
                tobedeleted = new Vector<DSPattern<DSSequence,
                              CSSeqRegistration>>();
        Hashtable<DSPattern<DSSequence, CSSeqRegistration>,
                List<DSPatternMatch<DSSequence,
                CSSeqRegistration>>>
                matches = seqDisPanel.getPatternMatches();
        for (DSPattern<DSSequence, CSSeqRegistration> pattern : matches.keySet()) {
            if (pattern.getClass().isAssignableFrom(CSMultiSeqPattern.class)) {
                tobedeleted.add(pattern);
            }
        }
        for (DSPattern<DSSequence, CSSeqRegistration> pattern : tobedeleted) {
            seqDisPanel.removePattern(pattern);
        }

        //add selected ones
        Object[] modules = moduleList.getSelectedValues();
        for (int i = 0; i < modules.length; i++) {
            PatternDisplay<DSSequence,
                    CSSeqRegistration> pd = (PatternDisplay) modules[i];
            Object pdPattern = pd.getPt();
            if (pdPattern instanceof CSMultiSeqPattern) {
                CSMultiSeqPattern compPatt = (CSMultiSeqPattern) pdPattern;
                List<DSPatternMatch<DSSequence,
                        CSSeqRegistration>> pc = compPatt.match(sequenceDB);
                for (int k = 0; k < pc.size(); k++) {
                    DSPatternMatch<DSSequence,
                            CSSeqRegistration> pm = pc.get(k);
                    int lastsub = pm.getRegistration().x2;
                    DSPattern[] patterns = ((CSMultiSeqPattern) pd.getPt()).
                                           getPatternKey().subpatterns;
                    for (int m = 0; m < patterns.length; m++) {
                        List<DSPatternMatch<DSSequence,
                                CSSeqRegistration>>
                                p = seqDisPanel.
                                    getPatternMatches().get(patterns[m]);
                        for (int n = 0; n < p.size(); n++) {
                            DSPatternMatch<DSSequence,
                                    CSSeqRegistration> ma = p.get(n);
                            if (ma.getRegistration().x2 == lastsub) {
                                pm.getRegistration().x2 = ma.
                                        getRegistration().
                                        x2;
                                break;
                            }
                        }
                    }
                }
                DSPattern<DSSequence,
                        CSSeqRegistration> patternMatch = pd.getPt();
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
                URL set13K = new URL(System.getProperty(
                        "data.download.site") +
                                     "13K.fa");
                File set13KFile = new File(System.getProperty(
                        "temporary.files.directory") + "13K.fa");
                if (!set13KFile.exists()) {
                    BufferedReader br = new BufferedReader(new
                            InputStreamReader(set13K.openStream()));
                    BufferedWriter bw = new BufferedWriter(new FileWriter(
                            set13KFile));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        bw.write(line + "\n");
                    }
                    bw.flush();
                    bw.close();
                    br.close();
                }
                background = CSSequenceSet.getSequenceDB(set13KFile);
            } catch (MalformedURLException mfe) {
                mfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void useThresholdCheck_actionPerformed(ActionEvent e) {

    }

    void updateProgressBar(final double percent, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    jProgressBar1.setForeground(Color.GREEN);
                    jProgressBar1.setString(text);
                    jProgressBar1.setValue((int) (percent * 100));
                    if(text.startsWith("Stop")){
                        jProgressBar1.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public ScoreStats getThreshold(TranscriptionFactor pattern,
                                   RandomSequenceGenerator rg,
                                   double pValue) {
        // computes the score based on a probability of a match of pValue
        // To get goo statistics, we expect at least 100 matches to exceed
        // the threshold in the null hypothesis. Hence, this is the number
        // of 1KB sequences we must test.
        int overT = 0;
        int seqLen = 1000;
        int seqNo = (int) (100 / pValue);
        double scores[] = new double[seqLen * 3];

        for (int i = 0; i < seqNo; i++) {
            if (stop) {
                return null;
            }
            double progress = (double) i / (double) seqNo;
            updateProgressBar(progress, "Computing Null Hypothesis");
            DSSequence sequence = rg.getRandomSequence(seqLen +
                    pattern.getLength());
            overT +=
                    pattern.getMatrix().collectSequenceScores(sequence, scores);
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
            if (stop) {
                return null;
            }
            int i = (int) (Math.random() * seqDB.size());
            DSSequence sequence = seqDB.getSequence(i);
            double progress = (double) partialLength / (double) totalLength;
            updateProgressBar(progress, "Computing Null Hypothesis");
            pattern.getMatrix().collectSequenceScores(sequence, scores);
            partialLength +=
                    Math.min(countValid(pattern, sequence), maxSeqLen);
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
                                    RandomSequenceGenerator rg,
                                    MatchStats ms) {
        // Determine the number of iterations so that the statistics are good
        int partialLength = 0;
        int totalLength = length * averageNo;
        while (partialLength < totalLength) {
            int invalidNo = 0;
            double progress = (double) partialLength / (double) totalLength;
            updateProgressBar(progress, "Computing Null Hypothesis");
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
        showTF.setSelected(false);
        showSeqPattern.setSelected(false);
        updateParameters();
        cleanAllPatterns();
        seqDisPanel.initialize(sequenceDB);
        seqDisPanel.initPanelView();
    }

    /**
     * cleanAllPatterns
     */
    public void cleanAllPatterns() {
        clear();
    }

    public void showTF_actionPerformed(ActionEvent e) {
        updateParameters();
        if (showTF.isSelected() && promoterPatterns.size() > 0) {
            for (DSPattern pattern : promoterPatterns) {
                seqDisPanel.addAPattern(pattern,
                                        promoterPatternDisplay.get(pattern),
                                        promoterPatternMatches.get(pattern));
            }

        } else {
//            for (DSPattern pattern : promoterPatterns) {
//                seqDisPanel.removePattern(pattern);
//            }
            seqDisPanel.setPatternTFMatches(null);

        }
        seqDisPanel.initPanelView();
        repaint();
    }

    public void showSeqPattern_actionPerformed(ActionEvent e) {
        if (seqPatterns != null) {
            //        seqDisPanel.setPatternTFMatches(null);
        }
        updateParameters();
        seqDisPanel.initPanelView();
        repaint();
//        if (showSeqPattern.isSelected()) {
//            for (DSPattern pattern : seqPatterns) {
//                seqDisPanel.addAPattern(pattern,
//                                        seqPatternDisplay.get(pattern),
//                                        seqPatternMatches.get(pattern));
//
//            }
//        } else {
//            for (DSPattern pattern : seqPatterns) {
//                seqDisPanel.removePattern(pattern);
//            }
//
//        }
    }

    public void iterationBox_actionPerformed(ActionEvent e) {

    }

    /**
     * receive
     *
     * @param e GeneSelectorEvent
     */
    public void receive(GeneSelectorEvent e) {
        seqDisPanel.sequenceDBUpdate(e);
    }

    public void pValueField_actionPerformed(ActionEvent e) {

    }

    /**
     * The marker JAutoList type.
     */
    private class TFListPanel extends JAutoList {

        public TFListPanel() {
            super(tfListModel);
        }

        public boolean setHighlightedIndex(int theIndex) {
            super.setHighlightedIndex(theIndex);
            elementClicked(theIndex, null);
            return true;
        }

        @Override protected void elementDoubleClicked(int index,
                MouseEvent e) {
            addSelectedTF(index);
        }


        @Override protected void elementClicked(int index, MouseEvent e) {

            String tfName = (String) tfListModel.getElementAt(index);

            TranscriptionFactor pattern = tfMap.get(tfName);
            currentTF = pattern;
            try {
                drawLogo(pattern);
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }

        @Override protected void elementRightClicked(int index,
                MouseEvent e) {
            itemRightClicked(index, e);
        }
    }


    private void itemRightClicked(int index, final MouseEvent e) {
        //ensureItemIsSelected(index);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                itemListPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void selectedItemRightClicked(int index, final MouseEvent e) {
        //ensureItemIsSelected(index);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectedItemListPopup.show(e.getComponent(), e.getX(),
                                           e.getY());
            }
        });
    }

    public void matrixDetailButton_actionPerformed(ActionEvent e) {

    }

    public void reverseButton_actionPerformed(ActionEvent e) {
        fivePrimerDirection = !fivePrimerDirection;
        if (currentTF != null) {
            try {
                drawLogo(currentTF);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * ListModel for the marker list.
     */
    private class TFListModel extends AbstractListModel {

        public int getSize() {
            if (tfNameSet == null) {
                return 0;
            }
            return tfNameSet.size();
        }

        public Object getElementAt(int index) {
            if ((tfNameSet == null) || tfNameSet.size() <= index) {
                return null;
            } else {
                return tfNameSet.get(index);
            }
        }

        public void addElement(Object obj) {
            if (!tfNameSet.contains(obj.toString())) {
                tfNameSet.add(obj.toString());
                Collections.sort(tfNameSet);
                int index = tfNameSet.size();
                fireIntervalAdded(this, index, index);

            }

        }

        public Object remove(int index) {
            Object rv = tfNameSet.get(index);
            tfNameSet.remove(index);
            fireIntervalRemoved(this, index, index);
            return rv;
        }


        public Object getItem(int index) {
            if ((tfNameSet == null) || tfNameSet.size() <= index) {
                return null;
            } else {
                return tfNameSet.get(index);
            }

        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {
            if (tfNameSet == null) {
                fireContentsChanged(this, 0, 0);
            } else {
                fireContentsChanged(this, 0, tfNameSet.size());
            }
        }

        public void refreshItem(int index) {
            fireContentsChanged(this, index, index);
        }

    }


    class ImagePanel extends JPanel {

        private Image img;
        private int WIDTH = 400;
        private int HEIGHT = 200;

        public ImagePanel() {
            this.setBackground(Color.white);
            //  setSize(new Dimension(100, 200));
        }

        //painting Methods
        public void paintComponent(Graphics g) {
            update(g);
        }

        public void update(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(this.getBackground());
            g2.fillRect(0, 0, this.getWidth(), this.getHeight());
            if (img != null) {
                g2.drawImage(img, 0, 0, this);
            } else {
                System.out.println(img == null);
            }

        }

        public void setImage(Image src) {
            this.img = null;
            this.img = src;
            repaint();
        }

        public void setImage(String url) {
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Image im = toolkit.getImage(new URL(url));
                MediaTracker t = new MediaTracker(this);
                t.addImage(im, 0);
                try {
                    t.waitForID(0);
                } catch (InterruptedException e) {
                }
                int width = im.getWidth(null);
                if (width > 0) {
                    this.img = im;
                    repaint();
                }
            } catch (Exception ex) {

            }

        }

        public void setImage(double scores[][]) {
            PSAMPlot psamPlot = new PSAMPlot(scores);
            psamPlot.setMaintainProportions(false);
            psamPlot.setAxisDensityScale(1);
            psamPlot.setYTitle("bits");
            psamPlot.setAxisLabelScale(3);
            psamPlot.setAllowXLabelRotation(false);
            BufferedImage image = new BufferedImage(WIDTH,
                    HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            psamPlot.layoutChart(WIDTH,
                                 HEIGHT,
                                 graphics.getFontRenderContext());

            psamPlot.paint(graphics);
            //ImageIcon psamImage = new ImageIcon(image);
            // Load logo - no longer used.
            // File logoFile = new File(file.getParentFile(), file.getName().replace(".out", ".png"));
            // ImageIcon psamImage = new ImageIcon(logoFile.getAbsolutePath());
            //psam.setPsamImage(psamImage);
            setImage(image);
        }

        public Dimension getPreferredSize() {
            if (img != null) {
                return new Dimension(img.getWidth(this), img.getHeight(this));
            }
            return new Dimension(50, 50);
        }

        public Dimension getMinimumSize() {
            if (img != null) {
                return new Dimension(img.getWidth(this), img.getHeight(this));
            }
            return new Dimension(40, 40);
        }
    }

}
