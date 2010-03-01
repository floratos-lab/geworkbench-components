package org.geworkbench.components.alignment.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.client.BlastAlgorithm;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.session.SoapClient;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

/**
 *
 * @author not attributable
 * @version $Id$
 */
@SuppressWarnings("unchecked")
@AcceptTypes( {CSSequenceSet.class})
public class BlastAppComponent extends
        CSSequenceSetViewEventBase {
	Log log = LogFactory.getLog(BlastAppComponent.class);

    private JPanel jBasicPane = new JPanel();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private JTabbedPane jTabbedBlastPane = new JTabbedPane();

    // five check boxes in filter panel
    private JCheckBox lowComplexFilterBox = null;
    private JCheckBox maskLowCaseBox = null;
    private JCheckBox maskLookupOnlyBox = null;
    private JCheckBox humanRepeatFilter = null;
    private JCheckBox jDisplayInWebBox = null;
    
    JPanel jAdvancedPane = new JPanel();
    JFileChooser jFileChooser1 = new JFileChooser();

    static final int BLAST = 0;//keep unchanged

    public static final String NCBILABEL = "NCBI BLAST Result";
    public static final String ERROR1 = "Interrupted";
    public static final String ERROR2 = "The connection to the Columbia Blast Server cannot be established, please try NCBI Blast Server.";

    String[] algorithmParameter = {
                                  "Smith-Waterman DNA",
                                  "Smith-Waterman Protein",
                                  "Frame (for DNA query to protein DB)",
                                  "Frame (for protein query to DNA DB)",
                                  //   "Double Frame (for DNA sequence to DNA DB)"
    };

    private JTable jDBList = null;
    private JButton blastButton = new JButton();
    private JScrollPane jScrollPane1 = new JScrollPane();

    private JComboBox jProgramBox = new JComboBox();

    private JPanel filterPanel = new JPanel();
    private JPanel blastxSettingPanel = new JPanel();
    
    // 4 pairs of label and combo box on blastxSettingPanel
    private JLabel matrixLabel = new JLabel("Expect:");
    private JComboBox jMatrixBox = new JComboBox();
    private JLabel expectLabel = new JLabel("Matrix:");
    private JComboBox jExpectBox = new JComboBox();
    private JLabel jWordsizeLabel = new JLabel("Word size:");
    private JComboBox jWordsizeBox = new JComboBox();
    private JLabel jGapcostsLabel = new JLabel("Gap costs:");
    private JComboBox jGapcostsBox = new JComboBox();
    
    ParameterSetter parameterSetter = new ParameterSetter();
	CSSequenceSet fastaFile;
    private BlastAppComponent blastAppComponent = null;
    JPanel subSeqPanel;
    JPanel subSeqPanel2;

    JTextField jstartPointField = new JTextField();
    JTextField jendPointField = new JTextField();
    JProgressBar serviceProgressBar = new JProgressBar();

    JLabel jLabel5 = new JLabel();
    JPanel subSeqPanel1 = new JPanel();

    JLabel jLabel6 = new JLabel();
    JComboBox jProgramBox1 = new JComboBox();
    JLabel jLabel7 = new JLabel();
    JLabel databaseLabel1 = new JLabel();

    JLabel jLabel8 = new JLabel();
    GridBagLayout gridBagLayout7 = new GridBagLayout();
    JLabel jAlgorithmLabel = new JLabel();
    JTextField jendPointField1 = new JTextField();

    GridBagLayout gridBagLayout8 = new GridBagLayout();
    FlowLayout flowLayout2 = new FlowLayout();
    JTextField jstartPointField1 = new JTextField();
    BorderLayout borderLayout5 = new BorderLayout();

    JProgressBar progressBar1 = new JProgressBar();
    GridBagLayout gridBagLayout9 = new GridBagLayout();

    JList jList2 = new JList(algorithmParameter);

    BorderLayout borderLayout1 = new BorderLayout();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    JButton blastStopButton = new JButton();

    FlowLayout flowLayout4 = new FlowLayout();
    GridBagLayout gridBagLayout14 = new GridBagLayout();
    BorderLayout borderLayout8 = new BorderLayout();

    BorderLayout borderLayout9 = new BorderLayout();

    BorderLayout borderLayout6 = new BorderLayout();
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    JCheckBox allArraysCheckBox;
    JToolBar jToolBar2 = new JToolBar();
    TitledBorder titledBorder1 = new TitledBorder("");
    Border border1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    Border border2 = new TitledBorder(border1,
                                      "Please specify Program and Database");
    Border border3 = new TitledBorder(border1, "Database Details");
    JTextArea textArea = new JTextArea();

    JLabel jLabel9 = new JLabel();
    XYLayout xYLayout1 = new XYLayout();
    ImageIcon startButtonIcon = new ImageIcon(this.getClass().getResource(
            "start.gif"));
    ImageIcon stopButtonIcon = new ImageIcon(this.getClass().getResource(
            "stop.gif"));
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();

    BorderLayout borderLayout3 = new BorderLayout();
    JScrollPane jScrollPane4 = new JScrollPane();
    private static final int MAIN = 0;
    public static final int SERVER = 2;

    private boolean stopButtonPushed;
    
    private static JLabel DatabaseLabel = new JLabel("Database:");
    
    public BlastAppComponent() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {

        jBasicPane = new JPanel();
        jTabbedPane1 = new JTabbedPane();
        jTabbedBlastPane = new JTabbedPane();

        jScrollPane4 = new JScrollPane();

        jAdvancedPane = new JPanel();

        jDBList = new JTable() // customized only to hide the header
        {
			private static final long serialVersionUID = -7546361375519248646L;

			protected void configureEnclosingScrollPane() {
                Container p = getParent();
                if (p instanceof JViewport) {
                    Container gp = p.getParent();
                    if (gp instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane)gp;
                        // Make certain we are the viewPort's view and not, for
                        // example, the rowHeaderView of the scrollPane -
                        // an implementor of fixed columns might do this.
                        JViewport viewport = scrollPane.getViewport();
                        if (viewport == null || viewport.getView() != this) {
                            return;
                        }
//                        scrollPane.setColumnHeaderView(getTableHeader());
                        scrollPane.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE );
                        scrollPane.setBorder(UIManager.getBorder("Table.scrollPaneBorder"));
                    }
                }
            }
        };

        blastButton = new JButton();
        jScrollPane1 = new JScrollPane();
        jProgramBox = new JComboBox();

        blastxSettingPanel = new JPanel();
        parameterSetter = new ParameterSetter();

        jstartPointField = new JTextField();
        jendPointField = new JTextField();
        serviceProgressBar = new JProgressBar();

        jLabel5 = new JLabel();
        subSeqPanel1 = new JPanel();

        jLabel6 = new JLabel();
        jProgramBox1 = new JComboBox();
        jLabel7 = new JLabel();
        databaseLabel1 = new JLabel();

        jLabel8 = new JLabel();
        gridBagLayout7 = new GridBagLayout();
        jAlgorithmLabel = new JLabel();
        jendPointField1 = new JTextField();

        gridBagLayout8 = new GridBagLayout();
        flowLayout2 = new FlowLayout();
        jstartPointField1 = new JTextField();
        borderLayout5 = new BorderLayout();

        progressBar1 = new JProgressBar();
        gridBagLayout9 = new GridBagLayout();

        jList2 = new JList(algorithmParameter);

        borderLayout1 = new BorderLayout();
        gridBagLayout3 = new GridBagLayout();
        gridBagLayout2 = new GridBagLayout();

        blastStopButton = new JButton();

        flowLayout4 = new FlowLayout();
        gridBagLayout14 = new GridBagLayout();
        borderLayout8 = new BorderLayout();

        borderLayout9 = new BorderLayout();

        borderLayout6 = new BorderLayout();
        gridBagLayout4 = new GridBagLayout();

        jToolBar2 = new JToolBar();
        titledBorder1 = new TitledBorder("");
        border1 = BorderFactory.createEtchedBorder(Color.white,
                new Color(165, 163, 151));
        border2 = new TitledBorder(border1,
                                   "Please specify Program and Database");

        jLabel9 = new JLabel();
        xYLayout1 = new XYLayout();
        startButtonIcon = new ImageIcon(this.getClass().getResource(
                "start.gif"));
        stopButtonIcon = new ImageIcon(this.getClass().getResource(
                "stop.gif"));
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        borderLayout2 = new BorderLayout();

        borderLayout3 = new BorderLayout();

        //above is part of code to get rid of npe.
        //sgePanel.setPv(this);
        allArraysCheckBox = new JCheckBox("Activated Sequences", true);
        subSeqPanel = new JPanel();
        subSeqPanel.setBorder(border2);
        subSeqPanel2 = new JPanel();
        subSeqPanel2.setBorder(border3);

        jLabel9.setText("Program: ");

        serviceProgressBar.setMinimumSize(new Dimension(10, 26));
        serviceProgressBar.setPreferredSize(new Dimension(104, 26));

        jBasicPane.setPreferredSize(new Dimension(364, 250));
        jPanel3.setLayout(borderLayout3);

        jBasicPane.setLayout(borderLayout2);
        //jBasicPane.setPreferredSize(new Dimension(10, 100));
        jBasicPane.setMinimumSize(new Dimension(10, 100));

        jAdvancedPane.setLayout(gridBagLayout3);
        jDBList.setToolTipText("Select a database");
        jDBList.setVerifyInputWhenFocusTarget(true);
        jDBList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jProgramBox.addItem("Select a program");
        jProgramBox.addItem("blastn");
        jProgramBox.addItem("blastp");
        jProgramBox.addItem("blastx");
        jProgramBox.addItem("tblastn");
        jProgramBox.addItem("tblastx");
        blastButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastButton.setHorizontalAlignment(SwingConstants.LEFT);
        blastButton.setHorizontalTextPosition(SwingConstants.CENTER);
        //blastButton.setText("BLAST");
        blastButton.setIcon(startButtonIcon);

        blastButton.setToolTipText("Start BLAST");

        blastButton.addActionListener(new
                                      BlastAppComponent_blastButton_actionAdapter());
        jTabbedPane1.setDebugGraphicsOptions(0);
        jTabbedPane1.setMinimumSize(new Dimension(5, 5));
        jProgramBox.addActionListener(new
                                      BlastAppComponent_jProgramBox_actionAdapter());
        jDBList.getSelectionModel().addListSelectionListener(new BlastAppComponent_jDBList_listSelectionListener() );

        // details of four combo boxes on plastx panel
        // (1)
        jMatrixBox
				.addActionListener(new BlastAppComponent_jMatrixBox_actionAdapter());
        jMatrixBox.addItem("dna.mat");
        jMatrixBox.setVerifyInputWhenFocusTarget(true);
        jMatrixBox.setSelectedIndex(0);
        // (2)
        jExpectBox.setSelectedIndex( -1);
        jExpectBox.setVerifyInputWhenFocusTarget(true);
        jExpectBox.addItem("10");
        jExpectBox.addItem("1");
        jExpectBox.addItem("0.1");
        jExpectBox.addItem("0.01");
        jExpectBox.addItem("0.000000001");
        jExpectBox.addItem("100");
        jExpectBox.addItem("1000");
        jExpectBox.setEditable(true);
        // (3)
        jGapcostsBox.setVerifyInputWhenFocusTarget(true);
        jGapcostsBox.addItem("Existence: 11 Extension: 1");
        jGapcostsBox.addItem("Existence:  9 Extension: 2");
        jGapcostsBox.addItem("Existence:  8 Extension: 2");
        jGapcostsBox.addItem("Existence:  7 Extension: 2");
        jGapcostsBox.addItem("Existence: 12 Extension: 1");
        jGapcostsBox.addItem("Existence: 10 Extension: 1");
        // (4)
        jWordsizeBox.addItem("3");
        jWordsizeBox.addItem("2");
        jWordsizeBox.addItem("7");
        jWordsizeBox.addItem("11");
        jWordsizeBox.addItem("15");
        
        // update tooltip text
        // based on information from http://blast.ncbi.nlm.nih.gov/Blast.cgi and http://www.ncbi.nlm.nih.gov/BLAST/matrix_info.html
        jExpectBox.setToolTipText("Random background noise");
        jWordsizeBox.setToolTipText("The length of the words governing the sensitivity");
        jMatrixBox.setToolTipText("Assigns a score for aligning any possible pair of residues");
        jGapcostsBox.setToolTipText("Score subtracted due to the gaps");

        blastxSettingPanel.setLayout(gridBagLayout2);

        jProgramBox.setAutoscrolls(false);
        jProgramBox.setMinimumSize(new Dimension(26, 21));
        subSeqPanel.setLayout(xYLayout1);
        subSeqPanel2.setLayout(xYLayout1);

        jstartPointField.setText("1");
        jendPointField.setText("");

        jLabel5.setAlignmentY((float) 0.5);
        jLabel5.setMinimumSize(new Dimension(5, 15));
        jLabel5.setHorizontalTextPosition(SwingConstants.TRAILING);
        jLabel5.setText("Please specify subsequence, database and program .");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        jLabel5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        subSeqPanel1.setLayout(gridBagLayout9);

        jLabel6.setText("to ");
        jProgramBox1.setAutoscrolls(false);
        jProgramBox1.setMinimumSize(new Dimension(26, 21));
        //jProgramBox1.setPreferredSize(new Dimension(26, 21));
        jLabel7.setText("Subsequence: ");
        databaseLabel1.setToolTipText("");
        databaseLabel1.setText("Database:");

        jLabel8.setText("From");
        jAlgorithmLabel.setText("Algorithms:");
        jendPointField1.setText("end");

        jstartPointField1.setText("1");

        progressBar1.setOrientation(JProgressBar.HORIZONTAL);
        progressBar1.setBorder(BorderFactory.createEtchedBorder());
        progressBar1.setStringPainted(true);

        jAdvancedPane.setMinimumSize(new Dimension(5, 25));
        blastxSettingPanel.setMinimumSize(new Dimension(5, 115));
        subSeqPanel1.setMinimumSize(new Dimension(10, 30));

        jList2.setMaximumSize(new Dimension(209, 68));
        jList2.setMinimumSize(new Dimension(100, 68));

        blastStopButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastStopButton.setVerifyInputWhenFocusTarget(true);
        //blastStopButton.setText("STOP");
        blastStopButton.setIcon(stopButtonIcon);
        blastStopButton.setToolTipText("Stop the Query");

        blastStopButton.addActionListener(new
                                          BlastAppComponent_blastStopButton_actionAdapter());

        jToolBar2.add(serviceProgressBar);
        serviceProgressBar.setOrientation(JProgressBar.HORIZONTAL);
        serviceProgressBar.setBorder(BorderFactory.createEtchedBorder());
        serviceProgressBar.setStringPainted(true);

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

        jTabbedPane1.add(jTabbedBlastPane, "BLAST");
        
        // 5 check boxes on filter panel
        lowComplexFilterBox = new JCheckBox();
        lowComplexFilterBox.setMinimumSize(new Dimension(10, 23));
        lowComplexFilterBox.setMnemonic('0');
        lowComplexFilterBox.setSelected(true);
        lowComplexFilterBox.setText("Low Complexity");
        
        maskLowCaseBox = new JCheckBox();
        maskLowCaseBox.setToolTipText("Filterl lower case sequences.");
        maskLowCaseBox.setText("Mask lower case");

        maskLookupOnlyBox = new JCheckBox();
        maskLookupOnlyBox.setText("Mask for lookup table only");
        maskLookupOnlyBox.setMinimumSize(new Dimension(5, 23));
        maskLookupOnlyBox.setMnemonic('0');
        maskLookupOnlyBox.setSelected(false);
        
        humanRepeatFilter = new JCheckBox();
        humanRepeatFilter.setToolTipText("Human Repeat Filter");
        humanRepeatFilter.setSelected(false);
        humanRepeatFilter.setText("Human Repeats Filter");
        
        jDisplayInWebBox = new JCheckBox();
        jDisplayInWebBox.setMinimumSize(new Dimension(10, 23));
        jDisplayInWebBox.setSelected(true);
        jDisplayInWebBox.setText("Display result in your web browser");

        // filter Panel: including five check boxes
        filterPanel = new JPanel();
        filterPanel.setMinimumSize(new Dimension(5, 10));
        filterPanel.add(lowComplexFilterBox);
        filterPanel.add(maskLowCaseBox);
        filterPanel.add(maskLookupOnlyBox);
        filterPanel.add(humanRepeatFilter);
        filterPanel.add(jDisplayInWebBox);
        
        // blastxSettingPanel includes four pairs of label and combo box
		blastxSettingPanel.add(matrixLabel, new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jMatrixBox, new GridBagConstraints(1, 2, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		blastxSettingPanel.add(jWordsizeLabel, new GridBagConstraints(0, 1, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		blastxSettingPanel.add(jWordsizeBox, new GridBagConstraints(1, 1, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		blastxSettingPanel.add(expectLabel, new GridBagConstraints(0, 2, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 2), 0, 0));
		blastxSettingPanel.add(jExpectBox, new GridBagConstraints(1, 0, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jGapcostsLabel, new GridBagConstraints(0, 3, 1,
				1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(2, 2, 2, 6), 1, -2));
		blastxSettingPanel.add(jGapcostsBox, new GridBagConstraints(1, 3, 1, 1,
				1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        // Advanced Pane contains two panels
		jAdvancedPane.add(filterPanel, new GridBagConstraints(0, 1, 1, 1, 1.0,
				1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), -150, 82));
		jAdvancedPane.add(blastxSettingPanel, new GridBagConstraints(0, 0, 1,
				1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 2, 1, 3), 0, 23));

		// jTabbedBlastPane contains two panels
		jTabbedBlastPane.add(jBasicPane, "Main");
        jTabbedBlastPane.add(jAdvancedPane, "Advanced Options");

        // mainPanel contains one tabbed pane
        mainPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        subSeqPanel.add(jScrollPane1, new XYConstraints(0, 89, 352, 97));
        subSeqPanel.add(jLabel9, new XYConstraints(0, 36, 60, 23));
        textArea.setBackground(subSeqPanel2.getBackground());
        textArea.setEditable(false);
        textArea.setLineWrap(true); //wrap text around
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        subSeqPanel2.add(textArea,new XYConstraints(0, 84, 352, 55));
        subSeqPanel.add(DatabaseLabel, new XYConstraints(0, 59, 61, 23));
        subSeqPanel.add(jProgramBox, new XYConstraints(84, 36, 267, 25)); //edit for new class.
       // subSeqPanel.add(jToolBar1, new XYConstraints( -1, 0, 353, 27));
        displayToolBar.add(Box.createHorizontalStrut(10), null);
        displayToolBar.add(blastButton);
        displayToolBar.add(Box.createHorizontalStrut(5), null);
        displayToolBar.add(blastStopButton);
        jScrollPane4.getViewport().add(jPanel3);
        jPanel3.add(subSeqPanel, java.awt.BorderLayout.WEST);
        jPanel3.add(subSeqPanel2, java.awt.BorderLayout.CENTER);
        jBasicPane.add(jScrollPane4, java.awt.BorderLayout.CENTER);
        jBasicPane.add(jToolBar2, java.awt.BorderLayout.NORTH);

        jTabbedPane1.setSelectedComponent(jTabbedBlastPane);
        jTabbedBlastPane.setSelectedComponent(jBasicPane);

        jProgramBox1.addItem("ecoli.nt");
        jProgramBox1.addItem("pdb.nt");
        jProgramBox1.addItem("pdbaa");
        jProgramBox1.addItem("yeast.aa");
        jProgramBox1.addItem("nr");

    }

    private void jProgramBox_actionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();

        // Get the new item

        String selectedProgramName = (String) cb.getSelectedItem();
        if (selectedProgramName != null) {
        	//jDBList.setRowSelectionInterval(0, 0); // TODO is this necessary?
        	final String[] columnNames = {"abbreviate", "description"};
            TableModel listModel = new DefaultTableModel(AlgorithmMatcher.translateToArray((String)selectedProgramName), columnNames);
        	jDBList.setModel(listModel);
            (jScrollPane1.getViewport()).add(jDBList, null);
            String[] model = AlgorithmMatcher.translateToMatrices(
                    selectedProgramName);
            jMatrixBox.setModel(new DefaultComboBoxModel(model));
            String[] model2 = AlgorithmMatcher.translateToWordSize(
                    selectedProgramName);
            jWordsizeBox.setModel(new DefaultComboBoxModel(model2));
            if (selectedProgramName.equalsIgnoreCase("blastn")) {
                humanRepeatFilter.setEnabled(true);
                jGapcostsBox.setEditable(false);
                jGapcostsBox.setVisible(false);
                jGapcostsLabel.setVisible(false);
            } else {
                humanRepeatFilter.setEnabled(false);
                jGapcostsBox.setEditable(false);
                jGapcostsBox.setVisible(true);
                jGapcostsLabel.setVisible(true);
            }

        }
    }

    private void jDBList_actionPerformed(ListSelectionEvent e) {
    	DefaultListSelectionModel jDBList = (DefaultListSelectionModel)e.getSource();
    	int dbSelection = jDBList.getMinSelectionIndex();
    	
        String program = (String) jProgramBox.getSelectedItem();
        String dbDetails = AlgorithmMatcher.translateToDBdetails(program, dbSelection); 
        
        textArea.setText( dbDetails );
    }
    
    private void jMatrixBox_actionPerformed(ActionEvent e) {
        String[] model = AlgorithmMatcher.translateToGapcosts(
                jMatrixBox.getSelectedItem().toString());
       	jGapcostsBox.setModel(new DefaultComboBoxModel(model));
    }

    public CSSequenceSet getFastaFile() {
        return fastaFile;
    }

    public void reportError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    private ParameterSetter processNCBIParameters() {
        ParameterSetting parameterSetting = collectParameters();
        if (parameterSetting == null) {
            return null;
        }
        parameterSetting.setUseNCBI(true);
        if (activeSequenceDB != null) {
            if (sequenceDB == null) {
                reportError("Please select a sequence file first!",
                            "Parameter Error");
                return null;
            } else { //to handle new sequenceDB.

                try {
                    String tempFolder = FilePathnameUtils.getTemporaryFilesDirectoryPath();

                    String outputFile = tempFolder + "Blast" +
                                        RandomNumberGenerator.getID() +
                                        ".html";
                    //progressBar = new JProgressBar(0, 100);

                    serviceProgressBar.setForeground(Color.GREEN);
                    serviceProgressBar.setBackground(Color.WHITE);

                    updateProgressBar(10, "Wait...");
                    if (fastaFile == null && activeSequenceDB != null) {
                        fastaFile = (CSSequenceSet) activeSequenceDB;
                    }
                    SoapClient sc = new SoapClient(parameterSetting.
                            getProgramName(),
                            parameterSetting.getDbName(),
                            outputFile);

                    sc.setSequenceDB(activeSequenceDB);
                    sc.setParentSequenceDB(sequenceDB);
                    BlastAlgorithm blastAlgo = new BlastAlgorithm();
                    blastAlgo.setUseNCBI(true);
                    blastAlgo.setParameterSetting(parameterSetting);

                    blastAlgo.setBlastAppComponent(this);
                    blastAlgo.setSoapClient(sc);
                    blastAlgo.start();
                    Thread.sleep(5);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return parameterSetter;

    }

    /**
     * Collect all selected parameters and save it to a ParameterSetting object.
     * @return ParameterSetting
     */
    private ParameterSetting collectParameters() {
        ParameterSetting ps = new ParameterSetting();
        int selectedRow = jDBList.getSelectedRow();
        String dbName = (String)jDBList.getModel().getValueAt(selectedRow, 0);

        String programName = (String) jProgramBox.getSelectedItem();
        if (programName == null ||
            programName.equalsIgnoreCase(AlgorithmMatcher.BLASTPROGRAM0)) {
            reportError("Please select a PROGRAM to search!", "Parameter Error");
            return null;
        }
        if (dbName == null) {
            reportError("Please select a DATABASE to search!",
                        "Parameter Error");
            return null;
        }

        boolean lowComplexFilterOn = lowComplexFilterBox.isSelected();
        boolean humanRepeatFilterOn = humanRepeatFilter.isSelected();
        boolean maskLowCaseOn = maskLowCaseBox.isSelected();
        String expectString = (String) jExpectBox.getSelectedItem();
        double expectValue = 10;
        if (expectString != null) {
            expectValue = Double.parseDouble(expectString.trim());
        }
        String endPoint = jendPointField.getSelectedText();
        String startPoint = jstartPointField.getSelectedText();
        if (fastaFile == null && activeSequenceDB != null) {
            fastaFile = (CSSequenceSet) activeSequenceDB;
        } else if (fastaFile == null && sequenceDB != null) {
            fastaFile = (CSSequenceSet) sequenceDB;
        }

        int endValue = -1;
        int startValue = 1;
        if (endPoint != null) {
            try {
                endValue = Integer.parseInt(endPoint.trim());
                startValue = Integer.parseInt(startPoint.trim());
            } catch (NumberFormatException e) {

            }
        }
        String wordsize = (String) jWordsizeBox.getSelectedItem();
        ps.setDbName(dbName);
        ps.setProgramName(programName);
        ps.setViewInBrowser(jDisplayInWebBox.isSelected());
        ps.setExpect(expectValue);
        ps.setLowComplexityFilterOn(lowComplexFilterOn);
        ps.setHumanRepeatFilterOn(humanRepeatFilterOn);
        ps.setMaskLowCase(maskLowCaseOn);
        ps.setMatrix((String) jMatrixBox.getSelectedItem());
        ps.setMaskLookupTable(maskLookupOnlyBox.isSelected());
        if (startValue <= 1 && endValue >= fastaFile.getMaxLength()) {
            //just use whole sequence. No end to reset.

        } else {
            ps.setStartPoint(startValue);
            ps.setEndPoint(endValue);
        }
        if (wordsize != null) {
            ps.setWordsize(wordsize);
        }
        String gapCost = (String) jGapcostsBox.getSelectedItem();
        if (gapCost != null) {
            ps.setGapCost(gapCost);
        }

        return ps;
    }

    /**
     * blastFinished
     * Take care of the state of finished blast.
     */
    public void blastFinished(String cmd) {
        Date finished_Date = new Date();
        if (cmd.startsWith("Interrupted")) {



            serviceProgressBar.setForeground(Color.ORANGE);
            serviceProgressBar.setBackground(Color.ORANGE);
           updateProgressBar(false, "Stopped on " + finished_Date);

        } else if (cmd.startsWith("OTHERS_Interrupted")) {

            progressBar1.setForeground(Color.ORANGE);
            progressBar1.setBackground(Color.ORANGE);
            updateProgressBar(false, "Stopped on " + finished_Date);

        } else {

            if (cmd.startsWith("pb")) {

                serviceProgressBar.setIndeterminate(false);

                serviceProgressBar.setForeground(Color.ORANGE);
                serviceProgressBar.setBackground(Color.ORANGE);
                serviceProgressBar.setString("Finished on " + finished_Date);
            } else if (cmd.startsWith("btk search")) {
                progressBar1.setIndeterminate(false);

                progressBar1.setForeground(Color.ORANGE);
                progressBar1.setBackground(Color.ORANGE);
                progressBar1.setString("Finished on " + finished_Date);
            }

        }
    }

    private void blastButton_actionPerformed(ActionEvent e) {
        stopButtonPushed = false;
        if (jTabbedPane1.getSelectedIndex() == BlastAppComponent.BLAST) {
            jTabbedBlastPane.setSelectedIndex(BlastAppComponent.MAIN);

			// only support NCBI server now
			parameterSetter = processNCBIParameters();
        } else {
            log.warn("unexpected selectedIndex of jTabbedPane1 "+jTabbedPane1.getSelectedIndex());
        }

    }

    public void setBlastDisplayPanel(int selectedPanel) {
        if (selectedPanel == BlastAppComponent.SERVER) {
            jTabbedBlastPane.setSelectedIndex(BlastAppComponent.SERVER);

        } else {
            jTabbedBlastPane.setSelectedIndex(BlastAppComponent.MAIN);
        }

    }

    protected void fireModelChangedEvent(MicroarraySetViewEvent event) {
    	fastaFile = activeSequenceDB;
    }

    void stopBlastAction() {
        stopButtonPushed = true;
        if (this.jTabbedPane1.getSelectedIndex() == BlastAppComponent.BLAST) {
            blastFinished("Interrupted");
        }

        if (parameterSetter != null) {

            BWAbstractAlgorithm algo = parameterSetter.getAlgo();
            if (algo != null) {
                algo.stop();
            }
        }
    };

    void jButton1_actionPerformed(ActionEvent e) {

        blastFinished("OTHERS_Interrupted");

    }

    private void blastStopButton_actionPerformed(ActionEvent e) {
        stopBlastAction();
    }

    void jButton7_actionPerformed(ActionEvent e) {
        try {
            BrowserLauncher.openURL("http://pfam.wustl.edu/browse.shtml");
        } catch (IOException ex) {
            reportError(ex.getMessage(), "Connection Error");

        }
    }

    public void updateProgressBar(final double percent, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    serviceProgressBar.setString(text);
                    serviceProgressBar.setValue((int) (percent * 100));
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void updateProgressBar(final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    serviceProgressBar.setString(text);
                    serviceProgressBar.setIndeterminate(true);
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void updateProgressBar(final boolean boo, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    serviceProgressBar.setString(text);
                    serviceProgressBar.setIndeterminate(boo);
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public BlastAppComponent getBlastAppComponent() {
        return blastAppComponent;
    }

    public boolean isStopButtonPushed() {
        return stopButtonPushed;
    }

    /**
     * publishProjectNodeAddedEvent
     *
     * @param event ProjectNodeAddedEvent
     */
    @Publish public org.geworkbench.events.ProjectNodeAddedEvent
            publishProjectNodeAddedEvent(org.geworkbench.events.
                                         ProjectNodeAddedEvent event) {
        return event;
    }
    
    private class BlastAppComponent_jMatrixBox_actionAdapter implements
			java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {
			jMatrixBox_actionPerformed(e);
		}
	}
    
    private class BlastAppComponent_jProgramBox_actionAdapter implements
			java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {
			jProgramBox_actionPerformed(e);
		}
	}
    
    private class BlastAppComponent_blastButton_actionAdapter implements
			java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {
			blastButton_actionPerformed(e);
		}
	}

    private class BlastAppComponent_blastStopButton_actionAdapter implements
			java.awt.event.ActionListener {
		public void actionPerformed(ActionEvent e) {
			blastStopButton_actionPerformed(e);
		}
	}

    private class BlastAppComponent_jDBList_listSelectionListener implements
			ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting()) {
				return;
			}

			jDBList_actionPerformed(e);
		}
	}

}
