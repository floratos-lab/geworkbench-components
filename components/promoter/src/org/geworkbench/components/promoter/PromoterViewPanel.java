package org.geworkbench.components.promoter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.RandomSequenceGenerator;
import org.geworkbench.util.associationdiscovery.statistics.ClusterStatistics;
import org.geworkbench.util.patterns.PatternLocations;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;



/**
 * <p>Widget provides all GUI services for sequence panel displays.</p>
 * <p>Widget is controlled by its associated component, SequenceViewAppComponent</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Califano Lab</p>
 *
 * @author Xuegong Wang, manjunath at genomecenter dot columbia dot edu, Xiaoqing Zhang
 * @version $Id$
 */
public final class PromoterViewPanel extends JPanel {
	private static final long serialVersionUID = -6523545577029722522L;

	private class ScoreStats {
        public double score;
        public double pValue;
        
        public ScoreStats(double score, double pValue) {
        	this.score = score;
        	this.pValue = pValue;
        }
    }

    private PromoterView promoterView;
    private DSSequenceSet<DSSequence> sequenceDB = null;
    private JFileChooser fc = null;
    private JFileChooser fc2 = null;
    private ImagePanel jInfoPanel = new ImagePanel();
    private DSSequenceSet<DSSequence> background = null;
    private TreeMap<String,
            TranscriptionFactor> tfMap = new TreeMap<String,
                                         TranscriptionFactor>();
    /* invoked from TFListPanel */
    TreeMap<String, TranscriptionFactor> getTfMap() {
		return tfMap;
	}

	private BorderLayout borderLayout2 = new BorderLayout();
    private JScrollPane seqScrollPane = new JScrollPane();
    private SequencePatternDisplayPanel seqDisPanel = new
            SequencePatternDisplayPanel();
    private JPanel jPanel2 = new JPanel();
    private JPanel logoPanel = new JPanel();
    
    private Vector<String> uniqueTaxGroupVector = new Vector<String>();
    
    /* invoked from TFListPanel */
	Vector<String> getUniqueTaxGroupVector() {
		return uniqueTaxGroupVector;
	}

	private HashMap<String, String> fullNameTaxGroupMap = new HashMap<String, String>();
	
	private BorderLayout borderLayout1 = new BorderLayout();
	private JButton displayBtn = new JButton();

    private JButton jSaveButton = new JButton();
    private JButton jaddNewTFB = new JButton();
    private JPanel jPanel3 = new JPanel();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JList jTranscriptionFactorList = new JList();
    private GridLayout gridLayout1 = new GridLayout();
    private JButton jButton2 = new JButton();
    private JButton reverseButton = new JButton("Reverse complement");
    private JProgressBar jProgressBar1 = new JProgressBar();

    private JPanel jPanel4 = new JPanel();
    private BorderLayout borderLayout4 = new BorderLayout();
    private JSplitPane jSplitPane1 = new JSplitPane();
    private GridLayout gridLayout2 = new GridLayout();
    private JLabel jLabel4 = new JLabel();
    private JLabel noTFLabel = new JLabel("No TF is selected.");
    private JLabel displayOptionLabel = new JLabel ("Display: ");
    private ButtonGroup countFreqGroup = new ButtonGroup();
    private JRadioButton countsRadioButton = new JRadioButton("Counts", true);
    private JRadioButton frequenciesRadioButton = new JRadioButton("Frequencies", false);

    private JTabbedPane jTabbedPane1 = new JTabbedPane();

    private JPopupMenu itemListPopup = new JPopupMenu();
    private JPopupMenu selectedItemListPopup = new JPopupMenu();
    private JMenuItem addToPanelItem = new JMenuItem("Add");
    private JMenuItem clearSelectionItem = new JMenuItem("Remove from Selected List.");
    private JMenuItem saveItem = new JMenuItem("Save");

    private JPanel jPanel10 = new JPanel();
    private JPanel jPanel11 = new JPanel();
    private BorderLayout borderLayout11 = new BorderLayout();
    private DefaultListModel selectedTFModel = new DefaultListModel();
    private JList jSelectedTFList = new JList(selectedTFModel);
    private JLabel jLabel3 = new JLabel();
    private JPanel jPanel1 = new JPanel();
    private JPanel northPanel = new JPanel();
    private JCheckBox showTF = new JCheckBox("Show TFs");
    private JCheckBox showSeqPattern = new JCheckBox("Show Patterns");
    private JButton clearButton = new JButton("Clear All");

    private BorderLayout borderLayout5 = new BorderLayout();
    private JScrollPane jScrollPane2 = new JScrollPane();
    private boolean isRunning = false;
    private boolean stop = false;
    private int averageNo = 10;
    private double pValue = 0.05;

    private ArrayList<TranscriptionFactor> promoterPatterns = new ArrayList<TranscriptionFactor>();

    private Hashtable<TranscriptionFactor, List<DSPatternMatch<DSSequence, CSSeqRegistration>>>
            promoterPatternMatches = new Hashtable<TranscriptionFactor,
                                     List<DSPatternMatch<DSSequence,
                                     CSSeqRegistration>>>();

    private final static int SEQUENCE = 2;
    private JMenuItem imageSnapShotItem = new JMenuItem("Image SnapShot");
    private boolean fivePrimerDirection = true;
    private TranscriptionFactor currentTF;
    
    /* invoked from TFListPanel */
    void setCurrentTF(TranscriptionFactor currentTF) {
		this.currentTF = currentTF;
	}

    private TFListModel tfListModel = new TFListModel(selectedTFModel);
    
    /* invoked from TFListPanel */
    TFListModel getTfListModel() {
		return tfListModel;
	}

	private static final String JASPAR_DATA_FOLDER = "jaspar_CORE";
    private static final String MATRIX_IDNAME_FILE = "MATRIX.txt";
    private static final String MATRIX_ANNOTATION_FILE = "MATRIX_ANNOTATION.txt";
    private static final String MATRIX_DATA_FILE = "MATRIX_DATA.txt";

   public PromoterViewPanel() {
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

        jPanel4.setLayout(borderLayout4);
        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel4.setText("TF List");

        try {
			fillupTranscriptionFactorList();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
        tfListPanel = new TFListPanel(this);
        tfListModel.setFullNameTaxGroupMap(fullNameTaxGroupMap); 

        reverseButton = new JButton("Reverse complement");
        reverseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reverseButton_actionPerformed(e);
            }
        });

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
        saveItem = new JMenuItem("Save");
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveResultToAFile();
            }
        });

        displayBtn.setToolTipText("Scans the selected sequences with the TF matrices");
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
        
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
        jButton2.setEnabled(false); // disable for release 2.0 see bug 1134

        jSelectedTFList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = jSelectedTFList.locationToIndex(e.getPoint());

                if (e.getButton() == MouseEvent.BUTTON3) {
                    selectedItemRightClicked(index, e);

                } else {

                    jSelectedTFList_mouseClicked(e);
                }
            }
        });
        jSelectedTFList.setToolTipText("Selected transcription factors");
		KeyAdapter tfListKeyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isActionKey()) {
					int index = jSelectedTFList.getSelectedIndex();
					if (index < 0) {
						return;
					}
					TranscriptionFactor pattern = (TranscriptionFactor) jSelectedTFList
							.getModel().getElementAt(index);
					currentTF = pattern;
					try {
						drawLogo(pattern);
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		};
		jSelectedTFList.addKeyListener(tfListKeyAdapter);
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
        pValueLabel.setText("P-Value / 1K:");
        pValueField.setText("0.05");
        //expectedLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
        expectedLabel1.setHorizontalTextPosition(SwingConstants.LEADING);
        expectedLabel1.setText("Expected:");
        //actualLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
        actualLabel1.setText("Actual:");
        expectedCountBox.setEditable(false);
        expectedCountBox.setText("0");

        enrichmentBox.setEditable(false);
        enrichmentBox.setText("1.0");
        parmsPanel.setBorder(BorderFactory.createEtchedBorder());
        parmsPanel.setMinimumSize(new Dimension(130, 100));
        parmsPanel.setPreferredSize(new Dimension(130, 100));
        enrichmentLabel.setText("Enrich. p-value:");
        useThresholdCheck.setText("Use Threshold:");
        useThresholdCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                useThresholdCheck_actionPerformed(e);
            }
        });
        countBox.setEditable(false);
        countBox.setText("0");
        thresholdBox.setEditable(false);
        set13KCheck.setText("13K Set");
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
        pValueFieldResult.setEditable(false);
        thresholdResult.setEditable(false);
        stopButton.setText("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	stop = true;
            }
        });
        iterationBox.setText("10");

        iterationLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        iterationLabel.setText("Iterations:");

        countsRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	drawLogo(currentTF);;
            }
        });

        frequenciesRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	drawLogo(currentTF);;
            }
        });

        pseudocountBox.setText("1.0");

        pseudocountLabel.setHorizontalTextPosition(SwingConstants.LEADING);
        pseudocountLabel.setText("Pseudocount:");

        sqrtNCheckBox.setText("sqrt(n)");
        sqrtNCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	sqrtNCheckBox_actionPerformed(e);
            }
        });

        primer5Label.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        primer3Label.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        primer5Label.setText("5\' hits");
        primer3Label.setText("3\' hits");

        expect2Label.setText("Expected:");
        match5PrimeExpectBox.setEditable(false);
        match5PrimeExpectBox.setText("0");
        match3PrimeExpectBox.setEditable(false);
        match3PrimeExpectBox.setText("0");
        match5PrimeActualBox.setEditable(false);
        match5PrimeActualBox.setText("0");

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
        parametersLabel.setText("Parameters:");

        resultLabel.setFont(new java.awt.Font("Times New Roman", Font.BOLD, 12));
        resultLabel.setHorizontalTextPosition(SwingConstants.LEFT);
        resultLabel.setText("Results:");
        jLabel1.setText("% with hits:");
        jPanel12.setLayout(borderLayout12);
        jPanel4.add(jScrollPane1, BorderLayout.CENTER);
        jPanel4.add(jLabel4, BorderLayout.NORTH);

        jScrollPane1.getViewport().add(jTranscriptionFactorList, null);
        jPanel1.add(jScrollPane2, BorderLayout.CENTER);
        jPanel1.add(jLabel3, BorderLayout.NORTH);
        jPanel3.add(tfListPanel, null);
        jScrollPane2.getViewport().add(jSelectedTFList, null);
        jPanel11.add(displayBtn, null);
        jPanel11.add(jaddNewTFB, null);
        jPanel11.add(jSaveButton, null);
        jPanel11.add(jButton2, null);
        jPanel11.add(stopButton, null);

        jPanel3.add(jPanel1, null);

        jPanel10.add(jPanel3, BorderLayout.CENTER);
        jPanel10.add(jPanel11, BorderLayout.SOUTH);

        jTabbedPane1.add(jPanel10, "TF Mapping");
        //jTabbedPane1.add(jPanel9, "ModuleDiscovery");

        seqDisPanel.addToolBarButton(showSeqPattern);
        seqDisPanel.jToolBar1.add(Box.createRigidArea(new Dimension(15, 0)));
        seqDisPanel.addToolBarButton(showTF);
        seqDisPanel.jToolBar1.add(Box.createRigidArea(new Dimension(15, 0)));
        seqDisPanel.addToolBarButton(clearButton);
        logoPanel = new JPanel();
        logoPanel.setLayout(new BorderLayout());
        logoPanel.add(jInfoPanel, BorderLayout.NORTH);
        matrixDisplayPanel = new JPanel();
        logoPanel.add(matrixDisplayPanel, BorderLayout.CENTER);
        matrixDisplayPanel.add(noTFLabel);
        displayOptionsPanel.add(displayOptionLabel);
        displayOptionsPanel.add(countsRadioButton);
        displayOptionsPanel.add(frequenciesRadioButton);
        logoPanel.add(displayOptionsPanel, BorderLayout.CENTER);
        countFreqGroup.add(countsRadioButton);
        countFreqGroup.add(frequenciesRadioButton);
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
                       new GridBagConstraints(1, 11, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(totalLabel1, new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(sequenceLabel1,
                       new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(expectedLabel1,
                       new GridBagConstraints(1, 10, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(expectedCountBox,
                       new GridBagConstraints(2, 10, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(expectedSeqCountBox,
                       new GridBagConstraints(3, 10, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(countBox, 
                	  new GridBagConstraints(2, 11, 1, 1, 1.0, 0.0
                            				  , GridBagConstraints.CENTER, 
                            				  GridBagConstraints.HORIZONTAL,
                            				  new Insets(1, 2, 0, 2), 0, 0));
        parmsPanel.add(seqCountBox, 
        			  new GridBagConstraints(3, 11, 1, 1, 1.0, 0.0
        					  				  , GridBagConstraints.CENTER, 
        					  				  GridBagConstraints.HORIZONTAL,
        					  				  new Insets(1, 2, 0, 2), 0, 0));
        parmsPanel.add(enrichmentBox,
                       new GridBagConstraints(2, 12, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(seqEnrichmentBox,
                       new GridBagConstraints(3, 12, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(enrichmentLabel,
                       new GridBagConstraints(1, 12, 2, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

          parmsPanel.add(percentSeqMatchBox,
                       new GridBagConstraints(3, 13, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

          parmsPanel.add(jLabel1, 
        		  	   new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0
        		  			   				  , GridBagConstraints.CENTER, 
        		  			   				  GridBagConstraints.HORIZONTAL,
        		  			   				  new Insets(0, 0, 0, 0), 0, 0));

        parmsPanel.add(primer5Label,
                       new GridBagConstraints(2, 14, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(primer3Label,
                       new GridBagConstraints(3, 14, 1, 1, 0.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.NONE,
                                              new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(match5PrimeExpectBox,
                       new GridBagConstraints(2, 15, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match3PrimeExpectBox,
                       new GridBagConstraints(3, 15, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

        parmsPanel.add(expect2Label,
                       new GridBagConstraints(1, 15, 2, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));

        parmsPanel.add(actualLabel,
                       new GridBagConstraints(1, 16, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(pValueFieldResultLabel,
                       new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0
		                                      , GridBagConstraints.CENTER,
		                                      GridBagConstraints.HORIZONTAL,
		                                      new Insets(1, 2, 1, 2), 0, 0));

        parmsPanel.add(pValueFieldResult,
                      new GridBagConstraints(2, 6, 1, 1, 1.0, 0.0
	                                         , GridBagConstraints.CENTER,
	                                         GridBagConstraints.HORIZONTAL,
	                                         new Insets(1, 2, 1, 2), 0, 0));
        
        parmsPanel.add(thresholdResultLabel,
                       new GridBagConstraints(1, 7, 1, 1, 1.0, 0.0
	                                          , GridBagConstraints.CENTER,
	                                          GridBagConstraints.HORIZONTAL,
	                                          new Insets(1, 2, 1, 2), 0, 0));

        parmsPanel.add(thresholdResult,
                       new GridBagConstraints(2, 7, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match5PrimeActualBox,
                       new GridBagConstraints(2, 16, 1, 1, 1.0, 0.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.HORIZONTAL,
                                              new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(match3PrimeActualBox, 
        			   new GridBagConstraints(3, 16, 1, 1, 1.0, 0.0
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
        parmsPanel.add(set13KCheck, 
        			   new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0
        					   				  , GridBagConstraints.CENTER, 
        					   				  GridBagConstraints.HORIZONTAL,
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

		parmsPanel.add(pseudocountBox,
						new GridBagConstraints(2, 4, 1, 1, 0.5,0.0
								               , GridBagConstraints.CENTER,
								               GridBagConstraints.HORIZONTAL,
								               new Insets(1, 2, 1, 2), 0, 0));


		parmsPanel.add(pseudocountLabel,
					    new GridBagConstraints(1, 4, 1, 1,	0.5, 0.0
					    					   , GridBagConstraints.CENTER,
					    					   GridBagConstraints.HORIZONTAL,
					    					   new Insets(1, 2, 1, 2), 0, 0));

        parmsPanel.add(sqrtNCheckBox,
        				new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0
							                   , GridBagConstraints.CENTER,
							                   GridBagConstraints.HORIZONTAL,
								               new Insets(0, 2, 0, 2), 0, 0));

        parmsPanel.add(pValueLabel, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(1, 2, 1, 2), 0, 0));
        parmsPanel.add(pValueField, new GridBagConstraints(2, 1, 1, 1, 0.5, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                new Insets(1, 2, 1, 2), 0, 0));


        parmsPanel.add(jLabel5, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(jLabel6, new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(resultLabel, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0
                , GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0));
        parmsPanel.add(resultLabel, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
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

   	private org.geworkbench.events.ImageSnapshotEvent createImageSnapshot() {
    
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

   	private JPanel jPanel12 = new JPanel();
    private JPanel parmsPanel = new JPanel();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private JTextField pValueField = new JTextField();
    private JLabel pValueFieldResultLabel = new JLabel("Calculated PValue / 1K: ");
    private JTextField pValueFieldResult = new JTextField();
    private JLabel pValueLabel = new JLabel();
    private JLabel expectedLabel1 = new JLabel();
    private JTextField expectedCountBox = new JTextField();
    private JLabel actualLabel1 = new JLabel();
    private JTextField enrichmentBox = new JTextField();
    private JLabel enrichmentLabel = new JLabel();
    private JTextField countBox = new JTextField();
    private JCheckBox useThresholdCheck = new JCheckBox();
    private JTextField thresholdBox = new JTextField();
    private JLabel thresholdResultLabel = new JLabel("Calculated Threshold: ");
    private JTextField thresholdResult = new JTextField();
    private JCheckBox set13KCheck = new JCheckBox();
    private JTextField percentSeqMatchBox = new JTextField();
    private JLabel totalLabel1 = new JLabel();
    private JLabel sequenceLabel1 = new JLabel();
    private JTextField expectedSeqCountBox = new JTextField();
    private JTextField seqCountBox = new JTextField();
    private JTextField seqEnrichmentBox = new JTextField();
    private JButton stopButton = new JButton();
    private JTextField iterationBox = new JTextField();
    private JLabel iterationLabel = new JLabel();
    private JTextField pseudocountBox = new JTextField();
    private JLabel pseudocountLabel = new JLabel();
    private JCheckBox sqrtNCheckBox = new JCheckBox();
    private JLabel primer5Label = new JLabel();
    private JLabel primer3Label = new JLabel();
    private JTextField match5PrimeExpectBox = new JTextField();
    private JTextField match3PrimeExpectBox = new JTextField();
    private JLabel expect2Label = new JLabel();

    private JTextField match5PrimeActualBox = new JTextField();
    private JTextField match3PrimeActualBox = new JTextField();
    private JLabel actualLabel = new JLabel();

    private JTabbedPane jTabbedPane2 = new JTabbedPane();
    private JLabel parametersLabel = new JLabel();
    private JLabel resultLabel = new JLabel();
    private JLabel jLabel1 = new JLabel();
    private BorderLayout borderLayout12 = new BorderLayout();
    private JLabel jLabel5 = new JLabel();
    private JLabel jLabel6 = new JLabel();
    private TFListPanel tfListPanel = null;
	private JPanel matrixDisplayPanel = new JPanel();
    private JPanel displayOptionsPanel = new JPanel();

    /* invoked from PromoterView */
    void setSequenceDB(DSSequenceSet<DSSequence> db2) {
        String id = null;
        if (db2 != null && sequenceDB != null) {
            id = db2.getID();
            if (sequenceDB.getID().equals(id)) {
                return;
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

    /* invoked from PromoterView */
    void setPromterView(PromoterView promterView) {
        this.promoterView = promterView;
    }


    private void clear() {
        promoterPatterns.clear();

        promoterPatternMatches.clear();
    }

    private void fillupTranscriptionFactorList() throws FileNotFoundException, IOException {
        if (tfMap == null) {
            tfMap = new TreeMap<String, TranscriptionFactor>();
        }

		/* Load MATRIX.TXT */
		final int IDNAME_KEY = 0;
		final int IDNAME_ID = 2;
		final int IDNAME_NAME = 4;
		final int IDNAME_COLUMN_NUM = 5;
		String idnameFileName = FilePathnameUtils.getDataFilesDirPath()
				+ JASPAR_DATA_FOLDER + FilePathnameUtils.FILE_SEPARATOR + MATRIX_IDNAME_FILE;
		File idnameFile = new File(idnameFileName);
		InputStream idNameStream = new FileInputStream(idnameFile);
		BufferedReader idNameReader = new BufferedReader(new InputStreamReader(idNameStream));
		HashMap<String, String[]> idNameMap = new HashMap<String, String[]>();
		String idNameRow = idNameReader.readLine();
		while (idNameRow != null) {
			String[] cols = idNameRow.split("\t");
			if (cols.length >= IDNAME_COLUMN_NUM) {
				idNameMap.put(cols[IDNAME_KEY], cols);
			}
			idNameRow = idNameReader.readLine();
		}
		idNameReader.close();
        
		/* Load MATRIX_ANNOTATION.TXT */
		final int ANNOTATIONS_KEY = 0;
		final int ANNOTATIONS_TYPE = 1;
		final int ANNOTATIONS_VALUE = 2;
		final int ANNOTATIONS_COLUMN_NUM = 3;
		String annotationFileName = FilePathnameUtils.getDataFilesDirPath()
				+ JASPAR_DATA_FOLDER + FilePathnameUtils.FILE_SEPARATOR + MATRIX_ANNOTATION_FILE;
		File annotationFile = new File(annotationFileName);
		InputStream annotationStream = new FileInputStream(annotationFile);
		BufferedReader annotationReader = new BufferedReader(new InputStreamReader(annotationStream));
		HashMap<String, HashMap<String, String[]>> annotationMaps = new HashMap<String, HashMap<String, String[]>>();
		String annotationRow = annotationReader.readLine();
		while (annotationRow != null) {
			String[] cols = annotationRow.split("\t");
			HashMap<String, String[]> annotationMap = (HashMap<String, String[]>) annotationMaps.get(cols[ANNOTATIONS_KEY]);
			if (annotationMap == null) {
				annotationMap = new HashMap<String, String[]>();
				annotationMaps.put(cols[ANNOTATIONS_KEY], annotationMap);
			}

			if (cols.length >= ANNOTATIONS_COLUMN_NUM && (cols[ANNOTATIONS_TYPE].equals("family") ||  (cols[ANNOTATIONS_TYPE].equals("tax_group")))) {
				annotationMap.put(cols[ANNOTATIONS_TYPE], cols);
			}
			annotationRow = annotationReader.readLine();
		}
		annotationReader.close();
 
		/* Load MATRIX_DATA.TXT */
		final int DATA_KEY = 0;
		final int DATA_SYMBOL = 1;
		final int DATA_INDEX = 2;
		final int DATA_COUNT = 3;
		final int DATA_COLUMN_NUM = 4;
		String dataFileName = FilePathnameUtils.getDataFilesDirPath()
				+ JASPAR_DATA_FOLDER + FilePathnameUtils.FILE_SEPARATOR + MATRIX_DATA_FILE;
		File dataFile = new File(dataFileName);
		InputStream dataStream = new FileInputStream(dataFile);
		BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataStream));
		HashMap<String, HashMap<String, String[]>> dataMaps = new HashMap<String, HashMap<String, String[]>>();
		String dataRow = dataReader.readLine();
		while (dataRow != null) {
			String[] cols = dataRow.split("\t");
			HashMap<String, String[]> dataMap = (HashMap<String, String[]>) dataMaps.get(cols[DATA_KEY]);
			if (dataMap == null) {
				dataMap = new HashMap<String, String[]>();
				dataMaps.put(cols[DATA_KEY], dataMap);
			}
			if (cols.length >= DATA_COLUMN_NUM) {
				dataMap.put(cols[DATA_SYMBOL]+"~"+cols[DATA_INDEX], cols); // [A,C,G,T]~[1-n] indexes 
			}
			dataRow = dataReader.readLine();
		}
		dataReader.close();

		
		
		HashMap<String, Matrix> matrixMap = new HashMap<String, Matrix>();
		for (Iterator<String> it = dataMaps.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			String[] idNameResult =  (String[])idNameMap.get(key);
			String id = idNameResult[IDNAME_ID]; 
			Matrix matrix = (Matrix) matrixMap.get(key);
			if (matrix == null) {
				char[] sym = { 'A', 'C', 'G', 'T' };
				matrix = new Matrix(sym);
				matrixMap.put(id, matrix);
			}

			HashMap<String, String[]> dataResults = dataMaps.get(key);
			for (Iterator<String> it2 = dataResults.keySet().iterator(); it2.hasNext();) {
				String symbolIndexKey = (String) it2.next();
				String[] row = dataResults.get(symbolIndexKey);

				matrix.setCounts(row[DATA_SYMBOL].charAt(0),
								Integer.parseInt(row[DATA_INDEX]) - 1, 
								Double.parseDouble(row[DATA_COUNT]));
			}
		}	

		uniqueTaxGroupVector.clear();
		
		Set<String> uniqueTaxGroupSet = new HashSet<String>();
		for (Iterator<String> it = dataMaps.keySet().iterator(); it.hasNext();) {
			String key = (String)it.next();
			
			String[] idNameResult = idNameMap.get(key);
			String id = idNameResult[IDNAME_ID];
			String name = idNameResult[IDNAME_NAME];
			
			HashMap<String, String[]> annotationsResults = annotationMaps.get(key);
			String[] familyArray = annotationsResults.get("family");
			String family = familyArray[ANNOTATIONS_VALUE]; 
			
			String fullName = name + ":" + family + ":" + id;

			boolean sqrtNSelected = false;
			double pseduocount = 0;
			Matrix mx = (Matrix) matrixMap.get(id);
			mx.initialize(sqrtNSelected, pseduocount);
			
			TranscriptionFactor tf = new TranscriptionFactor(fullName, mx);
			tfMap.put(fullName, tf);
			tfListModel.addElement(fullName);
			
			
			String[] tax_groupArray = annotationsResults.get("tax_group");
			String tax_group = CapitalizeFirstLetter(tax_groupArray[ANNOTATIONS_VALUE]); 
			uniqueTaxGroupSet.add(tax_group);
	
			fullNameTaxGroupMap.put(fullName, tax_group);
		}
		tfListModel.setFullNameTaxGroupMap(fullNameTaxGroupMap);
		
        uniqueTaxGroupVector.add("All Taxa");
        uniqueTaxGroupVector.addAll(uniqueTaxGroupSet);
        Collections.sort(uniqueTaxGroupVector);
    }

    private static String CapitalizeFirstLetter(String str) {
    	return str.substring(0, 1).toUpperCase()+str.substring(1);
    }
    
    private void refreshTranscriptionFactorList(){

    	Iterator<TranscriptionFactor> it = tfMap.values().iterator();
    	while (it.hasNext()) {
        	TranscriptionFactor transcriptionFactor = it.next();;
        	Matrix matrix = transcriptionFactor.getMatrix();
            boolean sqrtNSelected = sqrtNCheckBox.isSelected();
            double pseduocount = Double.parseDouble(pseudocountBox.getText());
        	matrix.refresh(sqrtNSelected, pseduocount);
        }
    }


    private void displayBtn_actionPerformed(ActionEvent e) {

    	refreshTranscriptionFactorList();

        if (sequenceDB == null) {
            return;
        }
        if (!sequenceDB.isDNA()) {
            JOptionPane.showMessageDialog(null,
                                          "It looks like you are trying to find TF from protein sequences. Please load a DNA sequence first.",
                                          "Please check",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedTFModel.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                                          "No Transcription Factor is Selected.",
                                          "Please check",
                                          JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.mappingPatterns();
        jTabbedPane2.setSelectedIndex(SEQUENCE);
    }

    
    private void saveResultToAFile() {
        //this will save the results into a file.
        if (sequenceDB == null) {
            return;
        }
        final HashMap<CSSequence,
                PatternSequenceDisplayUtil>
                tfPatterns = seqDisPanel.getPatternTFMatches();
        Set<CSSequence> keySet_try = null;
        if (tfPatterns != null) {
        	keySet_try = tfPatterns.keySet();
        }
        if (tfPatterns == null || keySet_try == null || keySet_try.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No pattern is detected.",
                                          "Please check",
                                          JOptionPane.ERROR_MESSAGE);
            return;

        }
        final Set<CSSequence> keySet = keySet_try;
        fc2 = new JFileChooser();
        fc2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand() == JFileChooser.APPROVE_SELECTION) {
                    File file = fc2.getSelectedFile();
                    try {
                        String tab = "\t";
                        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

                        for (CSSequence sequence : keySet) {
                            bw.write(sequence.getLabel());
                            bw.newLine();
                            PatternSequenceDisplayUtil pu = tfPatterns.get(sequence);
                            TreeSet<PatternLocations> patternsPerSequence = pu.getTreeSet();
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
                                            bw.write(seqReg.x2 + tab);
                                        } else if (seqReg.strand == 1){
                                            bw.write(seqReg.x2 + tab);
                                            bw.write(seqReg.x1 + 1 + tab);
                                        }
                                        bw.newLine();
                                    }
                                }
                            }
                            bw.newLine();
                        }
                        bw.flush();
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
    private void jaddNewTFB_actionPerformed(ActionEvent e) {
        fc = new JFileChooser();
        fc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fc_actionPerformed(e);
            }
        });
        fc.setDialogTitle("choose the matrix files");

		String dir = FilePathnameUtils.getTemporaryFilesDirectoryPath();
        // This is where we store user data information
        String filename = "promoterPanelSettings";
        try {
            File file = new File(dir + filename);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));
                dir = br.readLine();
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (dir == null || dir.equals(".")) {
			dir = FilePathnameUtils.getTemporaryFilesDirectoryPath();
        }
        fc.setCurrentDirectory(new File(dir));

        fc.showOpenDialog(this);
    }

    private void fc_actionPerformed(ActionEvent e) {

		String dir = FilePathnameUtils.getTemporaryFilesDirectoryPath();
        // This is where we store user data information
        String filename = "promoterPanelSettings";

        if (e.getActionCommand() == JFileChooser.APPROVE_SELECTION) {
            File file = fc.getSelectedFile();
            String directory = file.getParent();
            char[] sybol = {
                           'A', 'C', 'G', 'T'};
            Matrix mx = new Matrix(sybol);
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(file));
                String line = br.readLine();
                if(line==null) throw new IOException("Unexpected end of file "+file);
                String[] a = line.trim().split("\t");
                line = br.readLine();
                if(line==null) throw new IOException("Unexpected end of file "+file);
                String[] c = line.trim().split("\t");
                line = br.readLine();
                if(line==null) throw new IOException("Unexpected end of file "+file);
                String[] g = line.trim().split("\t");
                line = br.readLine();
                if(line==null) throw new IOException("Unexpected end of file "+file);
                String[] t = line.trim().split("\t");
                br.close();

                for (int indx = 0; indx < a.length; indx++) {
                    mx.setCounts('A', indx, Double.parseDouble(a[indx]));
                    mx.setCounts('C', indx, Double.parseDouble(c[indx]));
                    mx.setCounts('G', indx, Double.parseDouble(g[indx]));
                    mx.setCounts('T', indx, Double.parseDouble(t[indx]));
                }

                try { //save current settings.
                    BufferedWriter bw = new BufferedWriter(new FileWriter(dir + filename));
                    bw.write(directory);
                    bw.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // mx.train(sec);
            boolean sqrtNSelected = sqrtNCheckBox.isSelected();
            double pseduocount = Double.parseDouble(pseudocountBox.getText());
			mx.initialize(sqrtNSelected, pseduocount);
            TranscriptionFactor tf = new TranscriptionFactor(file.getName(), mx);

            /* TF is in the map */
            if ( tfMap.containsValue(tf)){
    			int o = JOptionPane.showConfirmDialog(null,
    					"Replace Transcription Factor: " + file.getName(), "Replace the existing Transcription Factor?",
    					JOptionPane.YES_NO_CANCEL_OPTION);
    			if (o != JOptionPane.YES_OPTION) {
    				return;
    			}

    			/* TF is in sequence panel */
                if ( !selectedTFModel.contains(tf)){
                    tfMap.put(file.getName(), tf);

                    // Highlight
                    int index = tfListModel.indexOf(file.getName());
                    tfListPanel.setHighlightedIndex(index);
					currentTF = tf;
		            try {
		                drawLogo(currentTF);
		            } catch (Exception ex) {
		                ex.printStackTrace();
		            }
                }else{ /* TF is in selected panel */
                    tfMap.put(file.getName(), tf);

	                currentTF = tf;
	                selectedTFModel.removeElement(tf);
	                selectedTFModel.addElement(tf);
	                jSelectedTFList.clearSelection();

                }
            }else{ /* TF is not in the map */

            	currentTF = tf;
            	tfMap.put(file.getName(), tf);
            	tfListModel.addElement(file.getName());

            	tfListModel.refresh();
            	// Highlight
                int index = tfListModel.indexOf(file.getName());
                tfListPanel.setHighlightedIndex(index);
            }
        }
    }

    /* this method is invoked within this class or from TFListPanel */
    void drawLogo(TranscriptionFactor pattern) {

        try {
            jInfoPanel.removeAll();
            logoPanel.removeAll();

            jInfoPanel = new ImagePanel();
            matrixDisplayPanel.removeAll();
            matrixDisplayPanel.setLayout(new BorderLayout());
            jInfoPanel.setImage(pattern.getMatrix().getSmallSampleScores());
            boolean showCounts = countsRadioButton.isSelected();
            JTable table = MatrixScrollPane.createMatrixTable(showCounts, pattern.getMatrix());
            // setting the size of the table and its columns
            table.setPreferredScrollableViewportSize(new Dimension(800, 100));
            //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            matrixDisplayPanel.add(new JScrollPane(table),
                                   BorderLayout.CENTER);
            jInfoPanel.add(new JLabel(pattern.getName()));
            logoPanel.add(jInfoPanel, BorderLayout.NORTH);
            logoPanel.add(matrixDisplayPanel, BorderLayout.CENTER);
            logoPanel.add(displayOptionsPanel, BorderLayout.AFTER_LAST_LINE);

            this.repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void jSelectedTFList_mouseClicked(MouseEvent e) {

        int index = jSelectedTFList.locationToIndex(e.getPoint());
        if (index < 0) {
            return;
        }
        TranscriptionFactor pattern = (TranscriptionFactor)
                                      jSelectedTFList.
                                      getModel().getElementAt(index);
        currentTF = pattern;
        if (e.getClickCount() == 2) {
            ((DefaultListModel) jSelectedTFList.getModel()).removeElementAt(index);
            tfListModel.addElement(pattern);
            tfListModel.refresh();
        } else {

            try {
                drawLogo(pattern);
            } catch (Exception e2) {
                e2.printStackTrace();
            }

        }
    }

    /**
     * Begin to scan the sequences.
     */
    private void mappingPatterns() {
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
                    ArrayList<TranscriptionFactor> ar = new ArrayList<TranscriptionFactor>();
                    double threshold = 0;

                    if (pValue < 0.05) {
                        pValue = 0.05;
                        pValueField.setText(formatPV.format(pValue));
                    }
                    RandomSequenceGenerator rs = new
                                                 RandomSequenceGenerator(
                            sequenceDB, pValue);

                    MatchStats msActual = new MatchStats();
                    MatchStats msExpect = new MatchStats();
                    int seqNo = 0;
                    int totalLength = 0;
                    for (Enumeration<?> en = selectedTFModel.elements(); en.hasMoreElements(); ) {
                        TranscriptionFactor pattern = (TranscriptionFactor)en.nextElement();
                        if (pattern == null) continue;
                        
                        jProgressBar1.setString("Processing :" + pattern.getName());

						ar.add(pattern);
						ScoreStats stats = null;
						// Load the 13K set if needed
						if (set13KCheck.isSelected()) {
							load13KBSet();
						} else {
							background = null;
						}
						if (useThresholdCheck.isSelected()) {
							pValue = 0.05;
							threshold = Double.parseDouble(thresholdBox
									.getText());
						} else {
							if (background != null) {
								// compute the threshold from the required
								// pValue
								// using the predefiedn background database
								stats = getThreshold(pattern, background,
										pValue);
							} else {
								// compute the threshold from the required
								// pValue
								// using a random generative model
								stats = getThreshold(pattern, rs, pValue);
							}
							// assign the new pValue based on what we could find
							if (stats != null) {
								pValue = stats.pValue;
								pValueFieldResult.setText(formatPV
										.format(pValue));
								threshold = stats.score * 0.99;
							} else {
								// stopped.
								updateProgressBar(1, "Stopped on " + new Date());
								return;
							}
						}
						pattern.setThreshold(threshold);
						// Lengths are in base pairs (BP) and do not include the
						// reverse
						// strand. Analysis is the done on both the normal and
						// reverse
						// strand.
						int partialLength = 0;
						List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
						for (int seqId = 0; seqId < sequenceDB.size(); seqId++) {
							double progress = (double) seqId
									/ (double) sequenceDB.size();
							updateProgressBar(progress,
									"Discovery: " + pattern.getName());
							DSSequence seq = sequenceDB.getSequence(seqId);
							// Count the valid positions so that we can compute
							// the background matches
							// in a meaningful way. E.g. don't count # or
							// stretches that do not contain
							// valid sequence data
							int positions = countValid(pattern, seq);
							if (positions > 10) {
								seqNo++;
								partialLength += positions;
								totalLength += positions;

								if (!useThresholdCheck.isSelected()) {
									// This assumes that the pvalue has been
									// correctly estimated
									// the compute the expected matches from the
									// p-value
									int oldMatch = (int) msExpect.matchNo;
									msExpect.matchNo += pValue
											* (double) (positions) / 1000.0;
									msExpect.match5primeNo += pValue
											* (double) (positions) / 1000.0
											/ 2.0;
									msExpect.match3primeNo += pValue
											* (double) (positions) / 1000.0
											/ 2.0;
									if (msExpect.matchNo - oldMatch >= 1) {
										msExpect.matchSeq++;
									}
								}
								List<DSPatternMatch<DSSequence, CSSeqRegistration>> seqMatches = pattern
										.match(seq);
								if (seqMatches.size() > 0) {
									msActual.matchSeq++;
								}
								matches.addAll(seqMatches);
							}
							if (stop) {
								return;
							}
						}
						updateProgressBar(1, "Discovery: " + pattern.getName());
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

							PatternOperations.getPatternColor(pattern
									.hashCode());
							if (showTF.isSelected()) {
								seqDisPanel.addAPattern(pattern, matches);
							}

							promoterPatternMatches.put(pattern, matches);
							promoterPatterns.add(pattern);
						}

						if (useThresholdCheck.isSelected()) {
							if (set13KCheck.isSelected()) { // set13KCheck
								// using the length of the current sequences as
								// background, determine an appropriate pvalue
								// from the 13K Set
								getMatchesPerLength(pattern, partialLength,
										threshold, background, null, msExpect);
							} else {
								// using the length of the current sequences as
								// background, determine an appropriate pvalue
								// from random data
								getMatchesPerLength(pattern, partialLength,
										threshold, null, rs, msExpect);
							}
						}
                    }

                    jProgressBar1.setIndeterminate(false);
                    double p = 1.0;
                    if (msExpect.matchNo > 0) {
                        p = msExpect.matchNo / (double) totalLength;
                    }
                    if (useThresholdCheck.isSelected()) {
                    	pValueFieldResult.setText(formatPV.format(p * 1000));      
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
                    thresholdResult.setText(format.format(threshold));
                    countBox.setText(formatInt.format(matchCount));
                    updateProgressBar(0, "Done");

                }

            };
            t.setPriority(Thread.MIN_PRIORITY);
            stop = false;
            isRunning = false;
            t.start();
        }
    }

    /* show the patterns over sequence viewer */
	void setPatterns(List<DSMatchedPattern<DSSequence, CSSeqRegistration>> patterns) {
        updateParameters();
        seqDisPanel.patternSelectionHasChanged(patterns);

        for (DSMatchedPattern<DSSequence, CSSeqRegistration> pattern : patterns) {
            PatternOperations.getPatternColor(pattern.hashCode());
        }
        /* There used to be more code in this method. It seems that they don't have any effect. */
    }

    private void updateParameters() {
        seqDisPanel.setDisplaySeqPattern(showSeqPattern.isSelected());
        seqDisPanel.setDisplayTF(showTF.isSelected());
    }

    private void clearSelectionPressed() {
        int[] indices = jSelectedTFList.getSelectedIndices();
        Object[] selectedTFs = jSelectedTFList.getSelectedValues();
        if (indices.length > 0) {
            for (int i = indices.length - 1; i >= 0; i--) {
            	selectedTFModel.removeElementAt(indices[i]);
            }
            for (Object ob : selectedTFs) {
                tfListModel.addElement(ob);
            }
            
			tfListModel.refresh();
        }
    }

    private void addSelectionPressed() {
        int[] indices = tfListPanel.getTFList().getSelectedIndices();
        if (indices.length > 0) {
            for (int i = indices.length - 1; i >= 0; i--) {
                addSelectedTF(indices[i]);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select at least one TF first.");
        }
    }

    /* this methos is invoked from within this class or TFListPanel */
    void addSelectedTF(int index) {
        String tfName = (String) tfListModel.getElementAt(index);
        tfListModel.remove(index);
		tfListModel.refresh();

        TranscriptionFactor value = tfMap.get(tfName);
		if (!selectedTFModel.contains(value)) {
			selectedTFModel.addElement(value);
		}
    }

    @SuppressWarnings("unchecked")
	private void load13KBSet() {
        if (background == null) {
            try {
                URL set13K = new URL(System.getProperty(
                        "data.download.site") +
                                     "13K.fa");
                File set13KFile = new File(FilePathnameUtils
						.getTemporaryFilesDirectoryPath() + "13K.fa");
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


    private void useThresholdCheck_actionPerformed(ActionEvent e) {
    	boolean useThresholdSelected = useThresholdCheck.isSelected();
    	if(useThresholdSelected){
    		pValueField.setEditable(false);
    		thresholdBox.setEditable(true);
    	}else{
    		pValueField.setEditable(true);
    		thresholdBox.setEditable(false);
    	}
	}


    private void updateProgressBar(final double percent, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                    jProgressBar1.setForeground(Color.GREEN);
                    jProgressBar1.setString(text);
                    jProgressBar1.setValue((int) (percent * 100));
                    if(text.startsWith("Stop")){
                        jProgressBar1.setForeground(Color.RED);
                    }
            }
        };
        SwingUtilities.invokeLater(r);
    }


    private ScoreStats getThreshold(TranscriptionFactor pattern,
                                   RandomSequenceGenerator rg,
                                   double pValue) {
        // computes the score based on a probability of a match of pValue
        // To get goo statistics, we expect at least 100 matches to exceed
        // the threshold in the null hypothesis. Hence, this is the number
        // of 1KB sequences we must test.
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
        return new ScoreStats(scores[x],
        		(double) (scores.length - x - 1) / (double) seqNo);
    }


    private ScoreStats getThreshold(TranscriptionFactor pattern,
                                   DSSequenceSet<DSSequence> seqDB, double pValue) {
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
        return new ScoreStats(scores[x],
        		(double) (scores.length - x - 1) / (double) partialLength * 1000);
    }


    private void getMatchesPerLength(TranscriptionFactor pattern, int length,
                                    double threshold, DSSequenceSet<DSSequence> seqDB,
                                    RandomSequenceGenerator rg,
                                    MatchStats ms) {
        // Determine the number of iterations so that the statistics are good
        int partialLength = 0;
        int totalLength = length * averageNo;
        while (partialLength < totalLength) {
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
                } 
                if (isBasePair(c2)) {
                    valid--;
                } 
                if (valid >= tfLen) {
                    validPositions++;
                }
            }
        }
        return validPositions;
    }

    private void clearButton_actionPerformed(ActionEvent e) {
        showTF.setSelected(false);
        showSeqPattern.setSelected(false);
        updateParameters();
        clear(); // cleanAllPatterns
        seqDisPanel.initialize(sequenceDB);
        seqDisPanel.initPanelView();
    }

    private void showTF_actionPerformed(ActionEvent e) {
        updateParameters();
        if (showTF.isSelected() && promoterPatterns.size() > 0) {
            for (TranscriptionFactor pattern : promoterPatterns) {
                seqDisPanel.addAPattern(pattern,
                                        promoterPatternMatches.get(pattern));
            }
        } else {
            seqDisPanel.setPatternTFMatches(null);
        }
        seqDisPanel.initPanelView();
        repaint();
    }

    private void showSeqPattern_actionPerformed(ActionEvent e) {
        updateParameters();
        seqDisPanel.initPanelView();
        repaint();
    }

    private void sqrtNCheckBox_actionPerformed(ActionEvent e) {

    	boolean sqrtNSelected = sqrtNCheckBox.isSelected();
    	if(sqrtNSelected){
    		pseudocountBox.setEditable(false);
    	}else{
    		pseudocountBox.setEditable(true);
    	}
    }

    /**
     * receive invoked from PromoterView.
     *
     * @param e GeneSelectorEvent
     */
    void receive(GeneSelectorEvent e) {
        seqDisPanel.sequenceDBUpdate(e);
    }

    /* invoked from TFLIstPanel. always from EDT. */
    void itemRightClicked(int index, final MouseEvent e) {
    	itemListPopup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void selectedItemRightClicked(int index, final MouseEvent e) {
        //ensureItemIsSelected(index);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                selectedItemListPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void reverseButton_actionPerformed(ActionEvent e) {
        fivePrimerDirection = !fivePrimerDirection;
        if (currentTF != null) {
            try {
                drawLogo(currentTF);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}