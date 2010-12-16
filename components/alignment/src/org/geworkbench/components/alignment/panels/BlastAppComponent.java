package org.geworkbench.components.alignment.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.alignment.blast.BlastAlgorithm;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;

/**
 *
 * @author not attributable
 * @version $Id$
 */
//test
@SuppressWarnings("unchecked")
@AcceptTypes( {CSSequenceSet.class})
public class BlastAppComponent implements VisualPlugin {
	private static final String SELECT_A_PROGRAM_PROMPT = "Select a program";

	private static final int DATABASE_NAME_INDEX = 1;

	Log log = LogFactory.getLog(BlastAppComponent.class);
	
	private static AlgorithmMatcher algorithmMatcher = AlgorithmMatcher.getInstance();

	// members from the base class in the previous version
	private DSSequenceSet<DSSequence> sequenceDB = null;
	private CSSequenceSet<? extends DSSequence> activeSequenceDB = null;
	private boolean activateMarkers = true;
	private DSPanel<? extends DSGeneMarker> activatedMarkers = null;
	
	private JPanel mainPanel;
	private JToolBar displayToolBar;
	private JCheckBox chkAllMarkers = new JCheckBox("All Markers");
	private JTextField sequenceNumberField;

	// members from originally in this class
	private JPanel jBasicPane = new JPanel();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private JTabbedPane jTabbedBlastPane = new JTabbedPane();

    //boxes for bug2019
    private JCheckBox excludeModels = null;
    private JCheckBox excludeUnculture = null;
    private JLabel jexcludeLabel = new JLabel("Exclude: ");
    private JLabel jEntrezQueryLabel=new JLabel("Entrez Query: ");
    private JTextField jEntrezQueryText=new JTextField(10);
    private JTextField jQueryFromText=new JTextField(5); 
    private JTextField jQueryToText=new JTextField(5); 
    
    private JRadioButton megablastBtn=null;
    private JLabel jmegablastLabel = new JLabel("Optimize for: ");
    private JRadioButton discontiguousBtn=null;
    private JRadioButton blastnBtn=null;
    private ButtonGroup programBtnGroup=new ButtonGroup();
    
    private JLabel jgeneticCodeLabel = new JLabel("Genetic Code: ");
    private JComboBox jgeneticCodeBox=new JComboBox();
    
    // five check boxes in filter panel+...
    private JCheckBox shortQueriesBox =null;
    private JCheckBox lowComplexFilterBox = null;
    private JCheckBox speciesRepeatFilter = null; 
    private JLabel jMaskLabel =new JLabel("Mask:");    
    private JCheckBox maskLowCaseBox = null;
    private JCheckBox maskLookupOnlyBox = null;
    private JLabel jDisplayLabel =new JLabel("Browser:");
    private JCheckBox jDisplayInWebBox = null;
    
    JPanel jAdvancedPane = new JPanel();    
    JFileChooser jFileChooser1 = new JFileChooser();

    static final int BLAST = 0;//keep unchanged

    public static final String NCBILABEL = "NCBI BLAST Result";
    public static final String ERROR1 = "Interrupted";
    public static final String ERROR2 = "The connection to the Columbia Blast Server cannot be established, please try NCBI Blast Server.";

    private JTable databaseTable = null;
    private JButton blastButton = new JButton();
    private JScrollPane jScrollPane1 = new JScrollPane();

    private JComboBox jProgramBox = new JComboBox();
   
    private JPanel speciesRepeatPanel = new JPanel();
    private JButton defaultButton = new JButton();
    
    // pairs of label and combo box on blastxSettingPanel
    private JLabel jMatrixLabel= new JLabel("Matrix:");
    private JComboBox jMatrixBox = new JComboBox();
    private JLabel maxTargetLabel= new JLabel("Max target sequences:");
    private JComboBox jMaxTargetBox=new JComboBox();

    private JComboBox jExpectBox = new JComboBox();
    private JComboBox jWordsizeBox = new JComboBox();
    private JTextField jHspRangeBox=new JTextField(5);

    private JLabel jScoresLabel= new JLabel("Match/mismatch Scores:");
    private JComboBox jScoresBox=new JComboBox();
    private JLabel jGapcostsLabel = new JLabel("Gap costs:");
    private JComboBox jGapcostsBox = new JComboBox();
    private JLabel jCompositionalLabel= new JLabel("Compositional Adjustments:");
    private JComboBox jCompositionalBox=new JComboBox();
    private JComboBox jSpeciesBox=new JComboBox();
    private JLabel jTemplateLengthLabel = new JLabel("Template Length:");
    private JComboBox jTemplateLengthBox=new JComboBox();
    private JLabel jTemplateTypeLabel = new JLabel("Template Type:");
    private JComboBox jTemplateTypeBox=new JComboBox();
    
    private BlastAppComponent blastAppComponent = null;
    private JPanel subSeqPanel;
    private JPanel subSeqPanel2;

    private JTextField jstartPointField = new JTextField();
    private JTextField jendPointField = new JTextField();
    private JProgressBar serviceProgressBar = new JProgressBar();
    
    private JButton blastStopButton = new JButton();

    private JToolBar jToolBar2 = new JToolBar();

    private Border border1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    private Border border2 = new TitledBorder(border1,
                                      "Please specify Program and Database");
    private Border border3 = new TitledBorder(border1, "Database Details");
    private JTextArea textArea = new JTextArea();

    private ImageIcon startButtonIcon = new ImageIcon(this.getClass().getResource(
            "start.gif"));
    private ImageIcon stopButtonIcon = new ImageIcon(this.getClass().getResource(
            "stop.gif"));

    private JPanel jPanel3 = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();

    private BorderLayout borderLayout3 = new BorderLayout();
    private JScrollPane jScrollPane4 = new JScrollPane();
    private static final int MAIN = 0;
    private static final int SERVER = 2;

    private boolean stopButtonPushed;

	private JPanel discontiguousWordOptionsPanel;

	private JPanel geneticCodePanel;
    
    private static JLabel DatabaseLabel = new JLabel("Database:");
    
    private String programForDefaultButton="blastn";
    
    private JPanel chooseSearchPanel=new JPanel();
    
    public BlastAppComponent() {
        try {
        	init1();
        	init2();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init2() throws Exception {

        jBasicPane = new JPanel();
        jTabbedPane1 = new JTabbedPane();
        jTabbedBlastPane = new JTabbedPane();

        jScrollPane4 = new JScrollPane();

        databaseTable = new JTable() // customized only to hide the header
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
      
        jstartPointField = new JTextField();
        jendPointField = new JTextField();
        serviceProgressBar = new JProgressBar();
        
        blastStopButton = new JButton();

        jToolBar2 = new JToolBar();

        border1 = BorderFactory.createEtchedBorder(Color.white,
                new Color(165, 163, 151));
        border2 = new TitledBorder(border1,
                                   "Please specify Program and Database");

        startButtonIcon = new ImageIcon(this.getClass().getResource(
                "start.gif"));
        stopButtonIcon = new ImageIcon(this.getClass().getResource(
                "stop.gif"));

        jPanel3 = new JPanel();
        borderLayout2 = new BorderLayout();

        borderLayout3 = new BorderLayout();

        geneticCodePanel = new JPanel();

        //above is part of code to get rid of npe.
        //sgePanel.setPv(this);
        subSeqPanel = new JPanel();
        subSeqPanel.setBorder(border2);
        subSeqPanel2 = new JPanel();
        subSeqPanel2.setBorder(border3);

        jBasicPane.setPreferredSize(new Dimension(364, 250));
        jBasicPane.setLayout(borderLayout2);
        
        jPanel3.setLayout(borderLayout3);

        databaseTable.setToolTipText("Select a database");
        databaseTable.setVerifyInputWhenFocusTarget(true);
        databaseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jProgramBox.addItem(SELECT_A_PROGRAM_PROMPT);
        jProgramBox.addItem("blastn");
        jProgramBox.addItem("blastp");
        jProgramBox.addItem("blastx");
        jProgramBox.addItem("tblastn");
        jProgramBox.addItem("tblastx");
        
        defaultButton.setText("Restore Defaults");
        defaultButton.addActionListener(new
                BlastAppComponent_default_actionAdapter());
        
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
        databaseTable.getSelectionModel().addListSelectionListener(new BlastAppComponent_jDBList_listSelectionListener() );

        // details of four combo boxes on plastx panel
        // (1)
        jMatrixBox
				.addActionListener(new BlastAppComponent_jMatrixBox_actionAdapter());
        jMatrixBox.addItem("dna.mat");
        jMatrixBox.setVerifyInputWhenFocusTarget(true);
        jMatrixBox.setSelectedIndex(0);
        // (2)
        
        jExpectBox.setVerifyInputWhenFocusTarget(true);        
        jExpectBox.addItem("0.000000001");
        jExpectBox.addItem("0.01");
        jExpectBox.addItem("0.1");        
        jExpectBox.addItem("1");
        jExpectBox.addItem("10");
        jExpectBox.addItem("100");
        jExpectBox.addItem("1000");
        jExpectBox.setEditable(true);
       
        // (3)
        jGapcostsBox.setVerifyInputWhenFocusTarget(true);
    	String defaulMatrixName = "BLOSUM62"; 
        String[] model = AlgorithmMatcher.translateToGapcosts(defaulMatrixName);
       	jGapcostsBox.setModel(new DefaultComboBoxModel(model));
       	Integer index = AlgorithmMatcher.defaultGapcostIndex.get(defaulMatrixName);
       	if(index==null) index = 0;
       	jGapcostsBox.setSelectedIndex(index);

        // (4) 
        jWordsizeBox.addItem("3");
        jWordsizeBox.addItem("2");
        jWordsizeBox.addItem("7");
        jWordsizeBox.addItem("11");
        jWordsizeBox.addItem("15");
        jWordsizeBox.setMaximumRowCount(10);
        
        //(5)        
        jScoresBox.addItem("1,-2");
        jScoresBox.addItem("1,-3");
        jScoresBox.addItem("1,-4");
        jScoresBox.addItem("2,-3");
        jScoresBox.addItem("4,-5");
        jScoresBox.addItem("1,-1");
        
        jCompositionalBox.addItem("No adjustment");
        jCompositionalBox.addItem("Composition-based statistics");
        jCompositionalBox.addItem("Conditional compositional score matrix adjustment");
        jCompositionalBox.addItem("Universal compositional score matrix adjustment");
       
        jSpeciesBox.addItem("Human");
        jSpeciesBox.addItem("Rodents");
        jSpeciesBox.addItem("Arabidopsis");
        jSpeciesBox.addItem("Rice");
        jSpeciesBox.addItem("Mammals");
        jSpeciesBox.addItem("Fungi");   
        jSpeciesBox.addItem("C.elegans");
        jSpeciesBox.addItem("A.gambiae");
        jSpeciesBox.addItem("Zebrafish");
        jSpeciesBox.addItem("Fruit fly"); 
        jSpeciesBox.setMaximumRowCount(10);
     
        jTemplateLengthBox.addItem("None");
        jTemplateLengthBox.addItem("16");
        jTemplateLengthBox.addItem("18");
        jTemplateLengthBox.addItem("21");        
        
        jTemplateTypeBox.addItem("Coding");
        jTemplateTypeBox.addItem("Maximal");
        jTemplateTypeBox.addItem("Two template");        
        
        jgeneticCodeBox.addItem("Standard (1)");
        jgeneticCodeBox.addItem("Vertebrate Mitochondrial (2)");
        jgeneticCodeBox.addItem("Yeast Mitochondrial (3)");
        jgeneticCodeBox.addItem("Mold Mitochondrial (4)");
        jgeneticCodeBox.addItem("Invertebrate Mitochondrial (5)");
        jgeneticCodeBox.addItem("Ciliate Nuclear(6)");
        jgeneticCodeBox.addItem("Echinoderm Mitochondrial (9)");
        jgeneticCodeBox.addItem("Euplotid Nuclear (10)");
        jgeneticCodeBox.addItem("Bacteria and Archaea (11)");
        jgeneticCodeBox.addItem("Alternative Yeast Nuclear (12)");
        jgeneticCodeBox.addItem("Ascidian Mitochondrial (13)");
        jgeneticCodeBox.addItem("Flatworm Mitochondrial (14)");
        jgeneticCodeBox.addItem("Blepharisma Macronuclear (15)");
        jgeneticCodeBox.setMaximumRowCount(13);
        
        jMaxTargetBox.addItem("10");
        jMaxTargetBox.addItem("50");
        jMaxTargetBox.addItem("100");
        jMaxTargetBox.addItem("250");
        jMaxTargetBox.addItem("500");
        jMaxTargetBox.addItem("1000");
        jMaxTargetBox.addItem("5000");
        jMaxTargetBox.addItem("10000");
        jMaxTargetBox.addItem("20000");
        jMaxTargetBox.setMaximumRowCount(10);
                
        jAdvancedPane.setLayout(new GridBagLayout());       

        jProgramBox.setAutoscrolls(false);
        subSeqPanel.setLayout(new BoxLayout(subSeqPanel, BoxLayout.Y_AXIS));
        subSeqPanel2.setLayout(new BoxLayout(subSeqPanel2, BoxLayout.Y_AXIS));

        jstartPointField.setText("1");
        jendPointField.setText("");

        blastStopButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastStopButton.setVerifyInputWhenFocusTarget(true);
        //blastStopButton.setText("STOP");
        blastStopButton.setIcon(stopButtonIcon);
        blastStopButton.setToolTipText("Stop the Query");

        blastStopButton.addActionListener(new
                                          BlastAppComponent_blastStopButton_actionAdapter());

        jToolBar2.setLayout(new BoxLayout(jToolBar2, BoxLayout.LINE_AXIS));
        jToolBar2.add(serviceProgressBar);
        serviceProgressBar.setOrientation(JProgressBar.HORIZONTAL);
        serviceProgressBar.setBorder(BorderFactory.createEtchedBorder());
        serviceProgressBar.setStringPainted(true);

        jTabbedPane1.add(jTabbedBlastPane, "BLAST");
        
        excludeModels = new JCheckBox();
        excludeModels.setMinimumSize(new Dimension(10, 23));
        excludeModels.setMnemonic(0);       
        excludeModels.setSelected(false);
        excludeModels.setText("Models(XM/XP)");
        
        excludeUnculture = new JCheckBox();
        excludeUnculture.setMinimumSize(new Dimension(10, 23));
        excludeUnculture.setMnemonic(0);
        excludeUnculture.setSelected(false);
        excludeUnculture.setText("Uncultured/environmental sequences");        
       
        megablastBtn=new JRadioButton();
        megablastBtn.setMinimumSize(new Dimension(10,23));
        megablastBtn.setMnemonic(0);        
        megablastBtn.setSelected(true);
        megablastBtn.setText("Highly similar sequences (megablast)");
        megablastBtn.addActionListener(new BlastAppComponent_megablastBtn_actionAdapter());        
        
        discontiguousBtn=new JRadioButton();
        discontiguousBtn.setMinimumSize(new Dimension(10,23));
        discontiguousBtn.setMnemonic(0);       
        discontiguousBtn.setText("More dissimilar sequences (discontiguous megablast)");
        discontiguousBtn.addActionListener(new BlastAppComponent_discontiguousBtn_actionAdapter());
        
        blastnBtn=new JRadioButton();
        blastnBtn.setMinimumSize(new Dimension(10,23));
        blastnBtn.setMnemonic(0);       
        blastnBtn.setText("Somewhat similar sequences (blastn)");
        blastnBtn.addActionListener(new BlastAppComponent_discontiguousBtn_actionAdapter());
        
        programBtnGroup.add(megablastBtn);
        programBtnGroup.add(discontiguousBtn);
        programBtnGroup.add(blastnBtn);
        
        // 5 check boxes on filter panel+...
        shortQueriesBox=new JCheckBox();
        shortQueriesBox.setMnemonic('0');
        shortQueriesBox.setSelected(true);
        shortQueriesBox.setText("Automatically adjust parameters for short input sequences");
        
        lowComplexFilterBox = new JCheckBox();
        lowComplexFilterBox.setMnemonic('0');
        lowComplexFilterBox.setSelected(false);
        lowComplexFilterBox.setText("Low Complexity");
        
        maskLowCaseBox = new JCheckBox();
        maskLowCaseBox.setToolTipText("Filterl lower case sequences.");
        maskLowCaseBox.setText("Mask lower case letter");

        maskLookupOnlyBox = new JCheckBox();
        maskLookupOnlyBox.setText("Mask for lookup table only");
        maskLookupOnlyBox.setMnemonic('0');
        maskLookupOnlyBox.setSelected(false);
        
        speciesRepeatFilter = new JCheckBox();
        speciesRepeatFilter.setToolTipText("Species_specific Repeat Filter");
        speciesRepeatFilter.setSelected(false);
        speciesRepeatFilter.setText("Species-specific repeats for");              
       
        jDisplayInWebBox = new JCheckBox();
        jDisplayInWebBox.setSelected(true);
        jDisplayInWebBox.setText("Display result in your web browser");
 
        // speciesRepeatPanel: including 2 items
        speciesRepeatPanel = new JPanel();
        speciesRepeatPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        speciesRepeatPanel.add(new JLabel("          "));
        speciesRepeatPanel.add(speciesRepeatFilter);
        speciesRepeatPanel.add(jSpeciesBox);
        
        // update tooltip text
        // based on information from http://blast.ncbi.nlm.nih.gov/Blast.cgi and http://www.ncbi.nlm.nih.gov/BLAST/matrix_info.html
        jMaxTargetBox.setToolTipText("Select the maximum number of aligned sequences to display");
        jExpectBox.setToolTipText("Random background noise");
        jWordsizeBox.setToolTipText("The length of the words governing the sensitivity");
        jHspRangeBox.setToolTipText("Limit the number of matches to a query range");
        jMatrixBox.setToolTipText("Assigns a score for aligning any possible pair of residues");
        jGapcostsBox.setToolTipText("Score subtracted due to the gaps");
        excludeModels.setToolTipText("Models(XM/XP)");
        excludeUnculture.setToolTipText("Uncultured/environmental sample sequences");
        jEntrezQueryText.setToolTipText("Enter an Entrez query to limit search ");
        jQueryFromText.setToolTipText("Sequence coordinates are from 1 to the sequence length");
        jQueryToText.setToolTipText("Sequence coordinates are from 1 to the sequence length");
        megablastBtn.setToolTipText("Highly similar sequences (megablast)");
        discontiguousBtn.setToolTipText("More dissimilar sequences (discontiguous megablast)");
        blastnBtn.setToolTipText("Somewhat similar sequences (blastn)");        
        
		// jTabbedBlastPane contains two panels
		jTabbedBlastPane.add(jBasicPane, "Main");
        jTabbedBlastPane.add(new JScrollPane(jAdvancedPane), "Algorithm Parameters");
        jAdvancedPane.setEnabled(false);

        // mainPanel contains one tabbed pane
        mainPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        JPanel programPanel = new JPanel();
        programPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        programPanel.add(new JLabel("Program: "));
        programPanel.add(jProgramBox);
        programPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subSeqPanel.add(programPanel);
        
        JPanel discontinguousPanel = new JPanel();
        discontinguousPanel.setLayout(new BoxLayout(discontinguousPanel, BoxLayout.X_AXIS) );
        discontinguousPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        jmegablastLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        discontinguousPanel.add(jmegablastLabel);
        JPanel discontinguousOptionsPanel = new JPanel();
        discontinguousOptionsPanel.setLayout(new BoxLayout(discontinguousOptionsPanel, BoxLayout.Y_AXIS));
        discontinguousOptionsPanel.add(megablastBtn);
        discontinguousOptionsPanel.add(discontiguousBtn);
        discontinguousOptionsPanel.add(blastnBtn);
        discontinguousOptionsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        discontinguousPanel.add(discontinguousOptionsPanel);
        discontinguousPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subSeqPanel.add(discontinguousPanel);
        enableBlastnRelateOptions(false); 
        
        geneticCodePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        geneticCodePanel.add(jgeneticCodeLabel);
        geneticCodePanel.add(jgeneticCodeBox);
        geneticCodePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subSeqPanel.add(geneticCodePanel);
        geneticCodePanel.setVisible(false);      
        
        DatabaseLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subSeqPanel.add(DatabaseLabel);
        jScrollPane1.setPreferredSize(new Dimension(300, 100));
        jScrollPane1.setAlignmentX(Component.LEFT_ALIGNMENT);
        subSeqPanel.add(jScrollPane1);
        
        textArea.setBackground(subSeqPanel2.getBackground());
        textArea.setEditable(false);
        textArea.setLineWrap(true); //wrap text around
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setPreferredSize(new Dimension(350, 100));
        subSeqPanel2.add(textArea);
        
        chooseSearchPanel.setLayout(new GridBagLayout());       
        
        JPanel excludePanel = new JPanel();
        excludePanel.setLayout(new BoxLayout(excludePanel, BoxLayout.Y_AXIS) );
        excludePanel.setBorder(new TitledBorder(border1, "Search Choice (optional)"));
       
        JPanel excludeOptions1Panel = new JPanel();
        excludeOptions1Panel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        excludeOptions1Panel.add(jexcludeLabel);        
        excludeOptions1Panel.add(excludeModels);
        excludePanel.add(excludeOptions1Panel);
        JPanel excludeOptions2Panel = new JPanel();
        excludeOptions2Panel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        excludeOptions2Panel.add(new JLabel("                "));        
        excludeOptions2Panel.add(excludeUnculture);
        excludePanel.add(excludeOptions2Panel);              
        
        JPanel entrezQueryPanel = new JPanel();
        entrezQueryPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        entrezQueryPanel.add(jEntrezQueryLabel);
        entrezQueryPanel.add(jEntrezQueryText);
        
        jEntrezQueryText.setText("");
        jEntrezQueryLabel.setVisible(false);
		jEntrezQueryText.setVisible(false);
        excludePanel.add(entrezQueryPanel);
        
        GridBagConstraints b = new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0);
        b.gridx = 0;
        b.gridy = 0;
        chooseSearchPanel.add(excludePanel,b);		
	
		JPanel querySubrangePanel=new JPanel();
		querySubrangePanel.setBorder(new TitledBorder(border1, "Query Subrange"));
		querySubrangePanel.setLayout(new BoxLayout(querySubrangePanel, BoxLayout.Y_AXIS) );		
		//querySubrangePanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		
		JPanel queryFromPanel = new JPanel();
		queryFromPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		queryFromPanel.add(new JLabel("From:"));
		queryFromPanel.add(jQueryFromText);		
		querySubrangePanel.add(queryFromPanel);
		
		JPanel queryToPanel = new JPanel();
		queryToPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		queryToPanel.add(new JLabel("To:     "));
		queryToPanel.add(jQueryToText);
		querySubrangePanel.add(queryToPanel);		
        b.gridx = 1;
        b.gridy = 0;
        chooseSearchPanel.add(querySubrangePanel,b); 
        chooseSearchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);		
		subSeqPanel.add(chooseSearchPanel);
		chooseSearchPanel.setVisible(false);
        
        displayToolBar.add(Box.createHorizontalStrut(10), null);
        displayToolBar.add(blastButton);
        displayToolBar.add(Box.createHorizontalStrut(5), null);
        displayToolBar.add(blastStopButton);
        jScrollPane4.getViewport().add(jPanel3);
        jPanel3.add(subSeqPanel, BorderLayout.CENTER);
        jPanel3.add(subSeqPanel2, BorderLayout.EAST);
        jBasicPane.add(jScrollPane4, java.awt.BorderLayout.CENTER);
        jBasicPane.add(jToolBar2, java.awt.BorderLayout.NORTH);

        jTabbedPane1.setSelectedComponent(jTabbedBlastPane);
        jTabbedBlastPane.setSelectedComponent(jBasicPane);
    }
    
    static private class UneditableTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1231350431097267149L;
		
		private String[][] data;
    	
    	UneditableTableModel(String[][] data) {
    		this.data = data;
    	}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}
    	
		@Override
		public boolean isCellEditable(int rowIndex,
                int columnIndex) {
			return false;
		}
    }

    private void enableBlastnRelateOptions(Boolean b){
    	geneticCodePanel.setVisible(false);
    	if(((String) jProgramBox.getSelectedItem()).equalsIgnoreCase("blastn")&&b){
    		megablastBtn.setVisible(b);
    		discontiguousBtn.setVisible(b);
    		blastnBtn.setVisible(b);
    		jmegablastLabel.setVisible(b);
    	}
    	else{
    		megablastBtn.setVisible(false);
    		discontiguousBtn.setVisible(false);
    		blastnBtn.setVisible(false);
    		jmegablastLabel.setVisible(false);
    	}    	
    	jexcludeLabel.setVisible(b);		
		excludeModels.setVisible(b);
		excludeUnculture.setVisible(b);		
    }
    
    private void setAdvancedOptions(){
    	String selectedProgramName = programForDefaultButton;
		jAdvancedPane.removeAll();
		jEntrezQueryLabel.setVisible(true);
		jEntrezQueryText.setVisible(true);		
		enableBlastnRelateOptions(true);
		if(selectedProgramName.equalsIgnoreCase("blastx")||selectedProgramName.equalsIgnoreCase("tblastx")){
			geneticCodePanel.setVisible(true);
		}

        JPanel generalParametersPanel = new JPanel();
        generalParametersPanel.setBorder( new TitledBorder(border1, "General Parameters"));
        generalParametersPanel.setLayout(new BoxLayout(generalParametersPanel, BoxLayout.Y_AXIS));
        JPanel maxTargetPanel = new JPanel();
        maxTargetPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        maxTargetPanel.add(maxTargetLabel);
        maxTargetPanel.add(jMaxTargetBox);
        maxTargetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        generalParametersPanel.add(maxTargetPanel);
        shortQueriesBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        generalParametersPanel.add(shortQueriesBox);
        JPanel expectThresholdPanel = new JPanel();
        expectThresholdPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
		expectThresholdPanel.add(new JLabel("Expect threshold:"));
        expectThresholdPanel.add(jExpectBox);
        expectThresholdPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        generalParametersPanel.add(expectThresholdPanel);
        JPanel wordSizePanel = new JPanel();
        wordSizePanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        wordSizePanel.add(new JLabel("Word size:"));
        wordSizePanel.add(jWordsizeBox);        
        wordSizePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        generalParametersPanel.add(wordSizePanel);
        JPanel hspRangePanel = new JPanel();
        hspRangePanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        hspRangePanel.add(new JLabel("Max matches in a query range:"));
        hspRangePanel.add(jHspRangeBox);        
        hspRangePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        generalParametersPanel.add(hspRangePanel);
        jHspRangeBox.setText("0");
        
        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1,
				0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0);
        jAdvancedPane.add(generalParametersPanel, c);
		
        JPanel scoringParametersPanel = new JPanel();
        scoringParametersPanel.setBorder(new TitledBorder(border1, "Scoring Paremeters"));
        scoringParametersPanel.setLayout(new BoxLayout(scoringParametersPanel, BoxLayout.Y_AXIS));
        JPanel maxtrixPanel = new JPanel();
        maxtrixPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        maxtrixPanel.add(jMatrixLabel);
        maxtrixPanel.add(jMatrixBox);
        maxtrixPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoringParametersPanel.add(maxtrixPanel);	
        JPanel scoresPanel = new JPanel();
        scoresPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        scoresPanel.add(jScoresLabel);
        scoresPanel.add(jScoresBox);
        scoresPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoringParametersPanel.add(scoresPanel);
        JPanel gapCostsPanel = new JPanel();
        gapCostsPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        gapCostsPanel.add(jGapcostsLabel);
        gapCostsPanel.add(jGapcostsBox);
        gapCostsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoringParametersPanel.add(gapCostsPanel);
        JPanel compositionalPanel = new JPanel();
        compositionalPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        compositionalPanel.add(jCompositionalLabel);
        compositionalPanel.add(jCompositionalBox);
        compositionalPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        scoringParametersPanel.add(compositionalPanel);
        c.gridy = 1;
        jAdvancedPane.add(scoringParametersPanel, c);
		
        JPanel filterAndMaskPanel = new JPanel();
        filterAndMaskPanel.setBorder(new TitledBorder(border1, "Filters and Masking"));
        filterAndMaskPanel.setLayout(new BoxLayout(filterAndMaskPanel, BoxLayout.Y_AXIS));
        JPanel filter1Panel = new JPanel();
        filter1Panel.setLayout(new FlowLayout(FlowLayout.LEADING));
        filter1Panel.add(new JLabel("Filter:"));
        filter1Panel.add(lowComplexFilterBox);
        filterAndMaskPanel.add(filter1Panel);
        filterAndMaskPanel.add(speciesRepeatPanel);
        JPanel mask1Panel = new JPanel();
        mask1Panel.setLayout(new FlowLayout(FlowLayout.LEADING));
        mask1Panel.add(jMaskLabel);
        mask1Panel.add(maskLookupOnlyBox);
        filterAndMaskPanel.add(mask1Panel);
        JPanel mask2Panel = new JPanel();
        mask2Panel.setLayout(new FlowLayout(FlowLayout.LEADING));
        mask2Panel.add(new JLabel("          "));
        mask2Panel.add(maskLowCaseBox);
        filterAndMaskPanel.add(mask2Panel);
        c.gridx = 1;
        c.gridy = 0;
        jAdvancedPane.add(filterAndMaskPanel, c);
        
        JPanel browserPanel = new JPanel();
        browserPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        browserPanel.add(jDisplayLabel);
        browserPanel.add(jDisplayInWebBox);        
        browserPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.gridx = 0;
        c.gridy = 2;
        jAdvancedPane.add(browserPanel, c);        
        
        JPanel defaultButtonPanel = new JPanel();
        defaultButtonPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        defaultButtonPanel.add(defaultButton);          
        defaultButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.gridx = 1;
        c.gridy = 2;
        jAdvancedPane.add(defaultButtonPanel, c);        
        
        discontiguousWordOptionsPanel = new JPanel();
        discontiguousWordOptionsPanel.setBorder(new TitledBorder(border1, "Discontiguous Word Options"));
        discontiguousWordOptionsPanel.setLayout( new BoxLayout(discontiguousWordOptionsPanel, BoxLayout.Y_AXIS) );
        JPanel templeateLengthPanel = new JPanel();
        templeateLengthPanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        templeateLengthPanel.add(jTemplateLengthLabel);
        templeateLengthPanel.add(jTemplateLengthBox);
        jTemplateLengthBox.setSelectedIndex(2);
        discontiguousWordOptionsPanel.add(templeateLengthPanel);
        JPanel templeateTypePanel = new JPanel();
        templeateTypePanel.setLayout(new FlowLayout(FlowLayout.LEADING) );
        templeateTypePanel.add(jTemplateTypeLabel);
        templeateTypePanel.add(jTemplateTypeBox);
        jTemplateTypeBox.setSelectedIndex(0);        
        discontiguousWordOptionsPanel.add(templeateTypePanel);
        c.gridx = 1;
        c.gridy = 1;
        jAdvancedPane.add(discontiguousWordOptionsPanel, c);
		
        discontiguousWordOptionsPanel.setVisible(false);
		
		jCompositionalLabel.setVisible(false);
		jCompositionalBox.setVisible(false);
		
		shortQueriesBox.setVisible(false);
		
		jAdvancedPane.setEnabled(true);
	
		String[] model = AlgorithmMatcher
				.translateToMatrices(selectedProgramName);
		jMatrixBox.setModel(new DefaultComboBoxModel(model));
		
		jWordsizeBox.setVerifyInputWhenFocusTarget(true);		
       	String defaultOptimizeFor="megablast";       
		String[] model2 = AlgorithmMatcher
				.translateToWordSize(selectedProgramName,defaultOptimizeFor);
		jWordsizeBox.setModel(new DefaultComboBoxModel(model2));
		jMaxTargetBox.setSelectedIndex(2);
		jExpectBox.setSelectedIndex(4);
		jSpeciesBox.setSelectedIndex(0);
		shortQueriesBox.setSelected(true);
		jDisplayInWebBox.setSelected(true);		
	
		if (selectedProgramName.equalsIgnoreCase("blastn")) {
			String[] capModel = AlgorithmMatcher.translateToGapcosts("blastn","megablast");
	       	jGapcostsBox.setModel(new DefaultComboBoxModel(capModel));
	       	Integer index = AlgorithmMatcher.defaultGapcostIndex.get("megablast");
	       	if(index==null) index = 0;
	       	jGapcostsBox.setSelectedIndex(index);
			jWordsizeBox.setSelectedIndex(3);//default selection
			jScoresLabel.setVisible(true);
			jScoresBox.setVisible(true);
			jScoresBox.setSelectedIndex(0);		
			speciesRepeatPanel.setVisible(true);
			jGapcostsBox.setEditable(false);
			jGapcostsBox.setVisible(true);
			jGapcostsLabel.setVisible(true);
			maskLookupOnlyBox.setSelected(true);
			shortQueriesBox.setVisible(true);
			jMatrixLabel.setVisible(false);
			jMatrixBox.setVisible(false);
			if (megablastBtn.isSelected())
				megaBlastSet();
			else
				discontiguosSet();
			
		} else {
			jScoresLabel.setVisible(false);
			jScoresBox.setVisible(false);
			jMatrixBox.setSelectedIndex(3);
			jWordsizeBox.setSelectedIndex(1);
			speciesRepeatPanel.setVisible(false);
			jGapcostsBox.setEditable(false);
			jGapcostsBox.setVisible(true);
			jGapcostsLabel.setVisible(true);			
			maskLookupOnlyBox.setSelected(false);
			maskLowCaseBox.setSelected(false);
			jMatrixLabel.setVisible(true);
			jMatrixBox.setVisible(true);
			if (selectedProgramName.equalsIgnoreCase("blastp")||selectedProgramName.equalsIgnoreCase("tblastn")){
				jCompositionalLabel.setVisible(true);
				jCompositionalBox.setVisible(true);
				jCompositionalBox.setSelectedIndex(2);
				
			}
			if (selectedProgramName.equalsIgnoreCase("blastp")){
				lowComplexFilterBox.setSelected(false);
				shortQueriesBox.setVisible(true);
			}
			else{
				lowComplexFilterBox.setSelected(true);
			}			
		}
    }
    
    private void jDefaultButton_actionPerformed(ActionEvent e) {
    	setAdvancedOptions(); 
    }    
   
    private void jProgramBox_actionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();
		// Get the new item
		String selectedProgramName = (String) cb.getSelectedItem();
		programForDefaultButton=selectedProgramName;
		jAdvancedPane.removeAll();		
		if (selectedProgramName == null
				|| selectedProgramName
						.equalsIgnoreCase(SELECT_A_PROGRAM_PROMPT)) {
			jScrollPane1.getViewport().removeAll();
			textArea.setText( "" );
			jAdvancedPane.setEnabled(false);
			enableBlastnRelateOptions(false);			
			jEntrezQueryLabel.setVisible(false);
			jEntrezQueryText.setVisible(false);
			chooseSearchPanel.setVisible(false);
			return;
		}	
		
		String[][] arrayT = algorithmMatcher
				.translateToArray((String) selectedProgramName);
		String[][] array = null;
		/*though blastn, tblastn, tblastx are all type "nucleotide", however, their database choices are different.
		 * for tblastn, tblastx, there are no database choices:dbindex/9606/rna and dbindex/10090/rna
		 */
		if(arrayT!=null){			
			if(selectedProgramName.equalsIgnoreCase("tblastn")||selectedProgramName.equalsIgnoreCase("tblastx")){
				int num=0;
				for(int i=0;i<arrayT.length;i++){
					if(arrayT[i][1].equalsIgnoreCase("dbindex/9606/allcontig_and_rna")||arrayT[i][1].equalsIgnoreCase("dbindex/10090/allcontig_and_rna")){
						num++;
					}
				}				
				array=new String[arrayT.length-num][2];
				int i=0;
				for(int j=0;j<arrayT.length;j++){			
					if (!(arrayT[j][1].equalsIgnoreCase("dbindex/9606/allcontig_and_rna")||arrayT[j][1].equalsIgnoreCase("dbindex/10090/allcontig_and_rna"))){
						array[i]=arrayT[j];					
						i++;
					}
				}
			}
			else {
				array=arrayT;
			}
		}
		else
			return;

		TableModel listModel = new UneditableTableModel(array);
		databaseTable.setModel(listModel);
		databaseTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		// make the first column large enough
		int vColIndex = 0;
		TableColumn col = databaseTable.getColumnModel().getColumn(vColIndex);
		int width = 250;
		col.setPreferredWidth(width);
		(jScrollPane1.getViewport()).add(databaseTable, null);		
		excludeModels.setSelected(false);
	    excludeUnculture.setSelected(false);
	    jEntrezQueryText.setText("");
	    jQueryFromText.setText("");
		jQueryToText.setText("");
	    megablastBtn.setSelected(true);
	    chooseSearchPanel.setVisible(true);
	    
		setAdvancedOptions();
    }
    
    private void jMegaBlastBtn_actionPerformed(ActionEvent e) {    	  	  
    	megaBlastSet();
    }
    private void megaBlastSet(){
    	String[] model2 = AlgorithmMatcher
		.translateToWordSize("blastn","megablast");
		jWordsizeBox.setModel(new DefaultComboBoxModel(model2));		
		jWordsizeBox.setSelectedIndex(3);
		jScoresBox.setSelectedIndex(0);	
		String[] capModel = AlgorithmMatcher.translateToGapcosts("blastn","megablast");
		jGapcostsBox.setModel(new DefaultComboBoxModel(capModel));
		Integer index = AlgorithmMatcher.defaultGapcostIndex.get("megablast");
		if(index==null) index = 0;
		jGapcostsBox.setSelectedIndex(index);
		lowComplexFilterBox.setSelected(true);
		speciesRepeatFilter.setSelected(false);
		maskLookupOnlyBox.setSelected(true);
		maskLowCaseBox.setSelected(false);
		
		discontiguousWordOptionsPanel.setVisible(false);
    }
    
    private void jDiscontiguousBtn_actionPerformed(ActionEvent e) {    	  	  
    	discontiguosSet();
    }
    
    private void discontiguosSet(){
    	String[] model2 = AlgorithmMatcher
		.translateToWordSize("blastn","discontiguous");
    	jWordsizeBox.setModel(new DefaultComboBoxModel(model2));		
    	jWordsizeBox.setSelectedIndex(0);
    	jScoresBox.setSelectedIndex(3);
    	String[] capModel = AlgorithmMatcher.translateToGapcosts("blastn","discontiguous");
		jGapcostsBox.setModel(new DefaultComboBoxModel(capModel));
		Integer index = AlgorithmMatcher.defaultGapcostIndex.get("discontiguous");
		if(index==null) index = 0;
		jGapcostsBox.setSelectedIndex(index);
		lowComplexFilterBox.setSelected(true);
		speciesRepeatFilter.setSelected(false);
		maskLookupOnlyBox.setSelected(true);
		maskLowCaseBox.setSelected(false);
	
		if (discontiguousBtn.isSelected()){
			discontiguousWordOptionsPanel.setVisible(true);
		}
		else{
			discontiguousWordOptionsPanel.setVisible(false);
		}
    }

    private void jMatrixBox_actionPerformed(ActionEvent e) {
    	String matrixName = jMatrixBox.getSelectedItem().toString(); 
        String[] model = AlgorithmMatcher.translateToGapcosts(matrixName
                );
       	jGapcostsBox.setModel(new DefaultComboBoxModel(model));
       	Integer index = AlgorithmMatcher.defaultGapcostIndex.get(matrixName);
       	if(index==null) index = 0;
       	jGapcostsBox.setSelectedIndex(index);
    }

    public void reportError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    private BlastAlgorithm blastAlgo = null;
    private void runBlastAlgorithm() {
        ParameterSetting parameterSetting = collectParameters();
        if (parameterSetting == null) {
            return;
        }
        parameterSetting.setUseNCBI(true);
        if (activeSequenceDB != null) {
            if (sequenceDB == null) {
                reportError("Please select a sequence file first!",
                            "Parameter Error");
                return;
            } else { //to handle new sequenceDB.

                try {

                    serviceProgressBar.setForeground(Color.BLACK);
                    serviceProgressBar.setBackground(Color.WHITE);

                    updateProgressBar(10, "Wait...");

                    blastAlgo = new BlastAlgorithm();
                    blastAlgo.setParameterSetting(parameterSetting);

                    blastAlgo.setBlastAppComponent(this);

                    blastAlgo.setSequenceDB( (CSSequenceSet<CSSequence>) activeSequenceDB);
                    blastAlgo.setParentSequenceDB(sequenceDB);
                    
                    blastAlgo.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Collect all selected parameters and save it to a ParameterSetting object.
     * @return ParameterSetting
     */
    @SuppressWarnings("rawtypes")
	private ParameterSetting collectParameters() {
        String programName = (String) jProgramBox.getSelectedItem();
        if (programName == null ||
            programName.equalsIgnoreCase(SELECT_A_PROGRAM_PROMPT)) {
            reportError("Please select a PROGRAM to search!", "Parameter Error");
            return null;
        }
        int selectedRow = databaseTable.getSelectedRow();
        String dbName = null;
        if(selectedRow!=-1) {
        	dbName = (String)databaseTable.getModel().getValueAt(selectedRow, DATABASE_NAME_INDEX);
        }
        if (dbName == null) {
            reportError("Please select a DATABASE to search!",
                        "Parameter Error");
            return null;
        }

        boolean lowComplexFilterOn = lowComplexFilterBox.isSelected();       
        boolean maskLowCaseOn = maskLowCaseBox.isSelected();
        String expectString = (String) jExpectBox.getSelectedItem();
        double expectValue = 10;
        if (expectString != null) {
            expectValue = Double.parseDouble(expectString.trim());
        }
        String endPoint = jendPointField.getSelectedText();
        String startPoint = jstartPointField.getSelectedText();
        DSSequenceSet fastaFile = null;
		if (activeSequenceDB != null) {
            fastaFile = activeSequenceDB;
        } else if (fastaFile == null && sequenceDB != null) {
            fastaFile = sequenceDB;
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
        
        boolean humanRepeatFilterOn = speciesRepeatFilter.isSelected();
        
        boolean excludeModelsOn = excludeModels.isSelected();
        boolean excludeUncultureOn = excludeUnculture.isSelected();
        boolean megaBlastOn=megablastBtn.isSelected();
        boolean discontiguousOn=discontiguousBtn.isSelected();
        boolean blastnBtnOn=blastnBtn.isSelected();
        boolean shortQueriesOn=shortQueriesBox.isSelected();        
        String matchScores=(String) jScoresBox.getSelectedItem();
        String compositionalAdjustment=(String) jCompositionalBox.getSelectedItem();
        String speciesRepeat=(String) jSpeciesBox.getSelectedItem();
        String templateLength=(String) jTemplateLengthBox.getSelectedItem();
        String templateType=(String) jTemplateTypeBox.getSelectedItem();
        String geneticCode=(String) jgeneticCodeBox.getSelectedItem();
        String maxTargetNumber=(String) jMaxTargetBox.getSelectedItem();
        String entrezQuery=jEntrezQueryText.getText().trim();
        String fromQuery=jQueryFromText.getText().trim();
        String toQuery=jQueryToText.getText().trim();
        String hspRange=jHspRangeBox.getText().trim();
        
        ParameterSetting ps = new ParameterSetting(dbName, programName,
				jDisplayInWebBox.isSelected(), expectValue, lowComplexFilterOn,
				humanRepeatFilterOn, maskLowCaseOn, (String) jMatrixBox
						.getSelectedItem(), maskLookupOnlyBox.isSelected(),
						excludeModelsOn,excludeUncultureOn, entrezQuery, fromQuery, toQuery, megaBlastOn,
						discontiguousOn,blastnBtnOn,shortQueriesOn,matchScores,compositionalAdjustment,
						speciesRepeat, templateLength, templateType, geneticCode, maxTargetNumber, hspRange);
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

    private void blastButton_actionPerformed(ActionEvent e) {
        stopButtonPushed = false;
        if (jTabbedPane1.getSelectedIndex() == BlastAppComponent.BLAST) {
            jTabbedBlastPane.setSelectedIndex(BlastAppComponent.MAIN);

			// only support NCBI server now
			runBlastAlgorithm();
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

    private void blastStopButton_actionPerformed(ActionEvent e) {
        stopButtonPushed = true;
        if (this.jTabbedPane1.getSelectedIndex() == BlastAppComponent.BLAST) {
			serviceProgressBar.setForeground(Color.ORANGE);
			serviceProgressBar.setBackground(Color.ORANGE);
			updateProgressBar(false, "Stopped on " + new Date());
		}
        
        if(blastAlgo!=null) {
        	if(!blastAlgo.cancel(true)) {
        		log.error("blast job was not able to be cancelled.");
        	}
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
    
	/**
	 * getComponent
	 * 
	 * @return Component
	 */
	public Component getComponent() {
		return mainPanel;
	}

	private void init1() throws Exception {
		mainPanel = new JPanel();

		displayToolBar = new JToolBar();
		displayToolBar
				.setLayout(new BoxLayout(displayToolBar, BoxLayout.X_AXIS));
		chkAllMarkers.setToolTipText("Use All Markers.");
		chkAllMarkers.setSelected(false);
		chkAllMarkers.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				activateMarkers = !((JCheckBox) e.getSource()).isSelected();
				refreshMaSetView();
			}

		});

		BorderLayout borderLayout2 = new BorderLayout();
		mainPanel.setLayout(borderLayout2);
		sequenceNumberField = new JTextField(20);
		sequenceNumberField.setMaximumSize(sequenceNumberField.getPreferredSize());
		sequenceNumberField.setEditable(false);

		sequenceNumberField.setText("Total Sequence Number:");
		displayToolBar.add(chkAllMarkers, null);
		displayToolBar.add(Box.createHorizontalStrut(5), null);
		displayToolBar.add(sequenceNumberField);
		mainPanel.add(displayToolBar, java.awt.BorderLayout.SOUTH);

		activateMarkers = !chkAllMarkers.isSelected();

	}
	
	@SuppressWarnings("rawtypes")
	private void refreshMaSetView() {
		activateMarkers = !chkAllMarkers.isSelected();
		if (activateMarkers) {
			if (activatedMarkers != null && activatedMarkers.size() > 0) {

				if (activateMarkers && (sequenceDB != null)) {
					activeSequenceDB = (CSSequenceSet) ((CSSequenceSet) sequenceDB)
							.getActiveSequenceSet(activatedMarkers);
					sequenceNumberField.setText("Activated Sequence Number: "
							+ activeSequenceDB.size());
				}

			} else if (sequenceDB != null) {
				sequenceNumberField.setText("Total Sequence Number: "
						+ sequenceDB.size());

				activeSequenceDB = (CSSequenceSet) sequenceDB;
			}

		} else if (sequenceDB != null) {
			sequenceNumberField.setText("Total Sequence Number: "
					+ sequenceDB.size());
		}

	}
	
	/**
	 * receiveProjectSelection
	 * 
	 * @param e -
	 *            ProjectEvent
	 */
	@SuppressWarnings("rawtypes")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof DSSequenceSet) {
			if (sequenceDB != dataSet) {
				this.sequenceDB = (DSSequenceSet) dataSet;

				activatedMarkers = null;
			}
			refreshMaSetView();
		}
	}
	
	/**
	 * geneSelectorAction
	 * 
	 * @param e
	 *            GeneSelectorEvent
	 */
	@Subscribe(Asynchronous.class)
	public void receive(GeneSelectorEvent e, Object source) {
		if(e.getGenericMarker()!=null && e.getPanel() == null) // do nothing for single marker selection
			return;
		
		if (e.getPanel() != null && e.getPanel().size() > 0) {
			activatedMarkers = e.getPanel().activeSubset();
		} else {
			activatedMarkers = null;
		}
		refreshMaSetView();
	}

	// following are a group of listener classes
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
    
    private class BlastAppComponent_default_actionAdapter implements	
			java.awt.event.ActionListener {

    	public void actionPerformed(ActionEvent e) {
    		jDefaultButton_actionPerformed(e);
    	}
    }
    
    
    private class BlastAppComponent_megablastBtn_actionAdapter implements
			java.awt.event.ActionListener {
    		public void actionPerformed(ActionEvent e) {
    		jMegaBlastBtn_actionPerformed(e);
    	}
    }
    
    private class BlastAppComponent_discontiguousBtn_actionAdapter implements
	java.awt.event.ActionListener {
	public void actionPerformed(ActionEvent e) {
	jDiscontiguousBtn_actionPerformed(e);
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

	        String program = (String) jProgramBox.getSelectedItem();
	        int selectedRow = databaseTable.getSelectedRow();
	        String dbDetails = "";
	        if(selectedRow!=-1) {
		        String dbName = (String)databaseTable.getModel().getValueAt(selectedRow, DATABASE_NAME_INDEX);
		        dbDetails = algorithmMatcher.getDatabaseDetail(program, dbName);
		       	jSpeciesBox.setSelectedIndex(0);
	        }
	        
	        textArea.setText( dbDetails );
		}
	}

}
