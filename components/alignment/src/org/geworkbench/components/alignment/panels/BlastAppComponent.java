package org.geworkbench.components.alignment.panels;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;
import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.client.BlastAlgorithm;
import org.geworkbench.components.alignment.client.BlatAlgorithm;
import org.geworkbench.components.alignment.client.HMMDataSet;
import org.geworkbench.components.alignment.grid.CreateGridServiceDialog;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.MicroarraySetViewEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
@AcceptTypes( {CSSequenceSet.class})public class BlastAppComponent extends
        CSSequenceSetViewEventBase {

    JCheckBox pfpFilterBox = new JCheckBox();
    JPanel jBasicPane = new JPanel();
    JLabel DatabaseLabel = new JLabel();
    JTabbedPane jTabbedPane1 = new JTabbedPane();
    JTabbedPane jTabbedBlastPane = new JTabbedPane();
    
    /*********Code Implement By Anh Vu***************
     *********Contact: vietanh.vu@m4x.org************
     *********Declaration of Blat's component********/
    JTabbedPane jTabbedBlatPane = new JTabbedPane();
    /*Blat Web Server Tab
     */
    JLabel jLabelGenome = new JLabel();
    JLabel jLabelAssembly = new JLabel();
    JLabel jLabelQueryType = new JLabel();
    JLabel jLabelSortOutput = new JLabel();
    JLabel jLabelOutputType = new JLabel();
    JComboBox jComboBoxGenome = new JComboBox();
    JComboBox jComboBoxAssembly = new JComboBox();
    JComboBox jComboBoxAssemblyValue = new JComboBox();
    JComboBox jComboBoxQueryType = new JComboBox();
    JComboBox jComboBoxSortOutput = new JComboBox();
    JComboBox jComboBoxOutputType = new JComboBox();
    JCheckBox jCheckBoxLucky = new JCheckBox();
    JCheckBox jCheckBoxOpenInBrowser = new JCheckBox();
    JPanel jPanelMain = new JPanel();
    JPanel jPanelSubBlat = new JPanel();
    JPanel jPanelUpBlat = new JPanel();
    JScrollPane jScrollPaneBlat = new JScrollPane(); 
    Border borderBlat1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    Border borderBlat2 = new TitledBorder(borderBlat1,
                                      "Please select the information from the lists below");
    BorderLayout borderLayoutBlat1 = new BorderLayout();
    BorderLayout borderLayoutBlat2 = new BorderLayout();
    BorderLayout borderLayoutBlat3 = new BorderLayout();
    JToolBar jToolBarBlat = new JToolBar();
    JProgressBar jProgressBarBlat = new JProgressBar();
    JButton jButtonUpdate = new JButton(); 
    
    /*Blat Stand Alone Tab
     */
    JLabel jLabelOoc = new JLabel();
    JLabel jLabelTileSize = new JLabel();
    JLabel jLabelOneOff = new JLabel();
    JLabel jLabelMinMatch = new JLabel();
    JLabel jLabelMinScore = new JLabel();
    JLabel jLabelMinIdentity = new JLabel();
    JLabel jLabelMaxGap = new JLabel();
    JLabel jLabelRepMatch = new JLabel();
    JLabel jLabelMinDivergence = new JLabel();
    JLabel jLabelDots = new JLabel();
    JLabel jLabelSelectDatabase = new JLabel();
    JLabel jLabelOutputFile = new JLabel();
    JLabel jLabelNoHead = new JLabel();
    JLabel jLabelMakeOoc = new JLabel();
    JLabel jLabelTrimT = new JLabel();
    JLabel jLabelNoTrimA = new JLabel();
    JLabel jLabelTrimHardA = new JLabel();
    JLabel jLabelFine = new JLabel();
    JLabel jLabelProt = new JLabel();
    JLabel jLabelDatabaseType = new JLabel();
    JLabel jLabelQType = new JLabel();
    JLabel jLabelMask = new JLabel();
    JLabel jLabelQMask = new JLabel();
    JLabel jLabelOut = new JLabel();
            
    JPanel jPanelStandAloneBlat = new JPanel();
    JTextField jTextFieldOoc = new JTextField();
    JTextField jTextFieldTileSize = new JTextField();
    JTextField jTextFieldOneOff = new JTextField();
    JTextField jTextFieldMinMatch = new JTextField();
    JTextField jTextFieldMinScore = new JTextField();
    JTextField jTextFieldMinIdentity = new JTextField();
    JTextField jTextFieldMaxGap = new JTextField();
    JTextField jTextFieldRepMatch = new JTextField();
    JTextField jTextFieldMinDivergence = new JTextField();
    JTextField jTextFieldDots = new JTextField();
    JTextField jTextFieldSelectDatabase = new JTextField();
    JTextField jTextFieldOutputFile = new JTextField();
    
    JCheckBox jCheckBoxNoHead = new JCheckBox();
    JCheckBox jCheckBoxMakeOoc = new JCheckBox();
    JCheckBox jCheckBoxTrimT = new JCheckBox();
    JCheckBox jCheckBoxNoTrimA = new JCheckBox();
    JCheckBox jCheckBoxTrimHardA = new JCheckBox();
    JCheckBox jCheckBoxFine = new JCheckBox();
    JCheckBox jCheckBoxProt = new JCheckBox();
    
    JComboBox jComboBoxDatabaseType = new JComboBox();
    JComboBox jComboBoxQType = new JComboBox();
    JComboBox jComboBoxMask = new JComboBox();
    JComboBox jComboBoxQMask = new JComboBox();
    JComboBox jComboBoxOut = new JComboBox();
    
    JPanel jPanelSubSBlat = new JPanel();
    JPanel jPanelUpSBlat = new JPanel();
    JScrollPane jScrollPaneSBlat = new JScrollPane(); 
    Border borderSBlat1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    Border borderSBlat2 = new TitledBorder(borderBlat1,
                                      "Please select the information from the lists below");
    BorderLayout borderLayoutSBlat1 = new BorderLayout();
    BorderLayout borderLayoutSBlat2 = new BorderLayout();
    BorderLayout borderLayoutSBlat3 = new BorderLayout();
    JToolBar jToolBarSBlat = new JToolBar();
    JProgressBar jProgressBarSBlat = new JProgressBar();
    
    JButton jButtonSetDefault = new JButton();
    JButton jButtonBrowse = new JButton();
    JButton jButtonOpen = new JButton();
           
    /*********End Code*******************************/
    
    JTabbedPane jTabbedHmmPane = new JTabbedPane();
    JTabbedPane jTabbedSmPane = new JTabbedPane();
    ServerInfoPanel jServerInfoPane = new ServerInfoPanel();
//     BlastGridServiceDataPanel sgePanel = new
//           BlastGridServiceDataPanel();
    CreateGridServicePanel sgePanel = new CreateGridServicePanel();
    JComboBox jMatrixBox = new JComboBox();
    JCheckBox lowComplexFilterBox = new JCheckBox();
    JPanel jAdvancedPane = new JPanel();
    JFileChooser jFileChooser1 = new JFileChooser();
    /*********Code Implement By Anh Vu***************
     *********Contact: vietanh.vu@m4x.org************
     **Modify the Macros accordingly with new code***/
    static final int BLAST = 0;//keep unchange
    //static final int SW = 1; old code
    //static final int HMM = 2; old code
    static final int BLAT = 1;//new code
    static final int HMM = 2;//new code
    static final int SW = 3;//new code
    
    static final int BLAT_SERVER = 0;
    static final int SBLAT = 1;
    /*********End Code*******************************/
    public static final String NCBILABEL = "NCBI BLAST Result";
    public static final String ERROR1 = "Interrupted";
    public static final String ERROR2 = "The connection to the Columbia Blast Server cannot be established, please try NCBI Blast Server.";
    String[] databaseParameter = {
                                 "ncbi/nr                      Peptides of all non-redundant sequences.",
                                 "ncbi/pdbaa               Peptides Sequences derived from the PDB.",
                                 "ncbi/swissprot      SWISS-PROT protein sequence database.",
                                 "ncbi/yeast.aa            Yeast  genomic CDS translations.",
                                 "ncbi/nt                    All Non-redundant  DNA equences.",
                                 "ncbi/pdbnt                Nucleotide sequences derived from the PDB.",
                                 "ncbi/yeast.nt           Yeast genomic nucleotide sequences.",
                                 "/genomes/mouse/goldenPath_Aug2005/100/*",
                                 "/genomes/rat/goldenPath_June2003/100/*",
                                 "/genomes/chimpanzee/goldenPath_Feb2004/100/*",
                                 "/genomes/dog/goldenPath/2005_May/100/*"};
    
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
     /*********Code Implement By Anh Vu***************
     *********Contact: vietanh.vu@m4x.org*************
     ***Setup the default parameter for blat stand 
     ***alone application*****************************/
     public final String DEFAULT_DATABASE_TYPE = "dna";
     public final String DEFAULT_QTYPE = "dna";
     public final String DEFAULT_MASK = "none";
     public final String DEFAULT_QMASK = "none";
     public final String DEFAULT_OUT = "psl";
     public final String DEFAULT_Ooc = "11";
     public final String DEFAULT_TILE_SIZE_DNA = "11";
     public final String DEFAULT_TILE_SIZE_PROT = "5";
     public final String DEFAULT_ONE_OFF = "0";
     public final String DEFAULT_MIN_MATCH_NUCLEOTIDE = "2";
     public final String DEFAULT_MIN_MATCH_PROTEIN = "1";
     public final String DEFAULT_MIN_SCORE = "30";
     public final String DEFAULT_MIN_IDENTITY_NUCLEOTIDE = "90";
     public final String DEFAULT_MIN_IDENTITY_PROTEIN = "25";
     public final String DEFAULT_MAX_GAP = "2";
     public final String DEFAULT_REP_MATCH = "1024"; 
     public final String DEFAULT_MIN_DIVERGENCE = "15";
     public final String DEFAULT_DOTS = "0";
     public final String DEFAULT_DATABASE_DIRECTORY = "C:\\AnhVu\\Fasta";
     public final String DEFAULT_OUTPUT_RESULT = "C:\\AnhVu\\SBlatResults";
     public final String SCRIPT_DIRECTORY = "C:\\AnhVu";
     /*********End Code*******************************/
     
    //JList jDBList = new JList(databaseParameter);
    JPanel checkboxPanel = new JPanel();
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
    JComboBox jGapcostsBox = new JComboBox();
    JLabel jWordsizeLabel = new JLabel();
    JComboBox jWordsizeBox = new JComboBox();
    ParameterSetter parameterSetter = new ParameterSetter();
    CSSequenceSet fastaFile;
    private BlastAppComponent blastAppComponent = null;
    JPanel subSeqPanel;
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JTextField jstartPointField = new JTextField();
    JTextField jendPointField = new JTextField();
    CardLayout cardLayout1 = new CardLayout();
    JProgressBar serviceProgressBar = new JProgressBar();
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
    JLabel jGapcostsLabel = new JLabel();
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
    FlowLayout flowLayout5 = new FlowLayout();
    JButton jButton2 = new JButton();
    JScrollPane jScrollPane3 = new JScrollPane();
    BorderLayout borderLayout6 = new BorderLayout();
    GridBagLayout gridBagLayout4 = new GridBagLayout();
    JCheckBox allArraysCheckBox;
    JToolBar jToolBar2 = new JToolBar();
    TitledBorder titledBorder1 = new TitledBorder("");
    Border border1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    Border border2 = new TitledBorder(border1,
                                      "Please specify subsequence, program and database");
    JToolBar jToolBar1 = new JToolBar();
    JLabel jLabel4;
    JLabel jLabel9 = new JLabel();
    XYLayout xYLayout1 = new XYLayout();
    ImageIcon startButtonIcon = new ImageIcon(this.getClass().getResource(
            "start.gif"));
    ImageIcon stopButtonIcon = new ImageIcon(this.getClass().getResource(
            "stop.gif"));
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    XYLayout xYLayout2 = new XYLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JScrollPane jScrollPane4 = new JScrollPane();
    public static final int MAIN = 0;
    public static final int SERVER = 2;
    JCheckBox humanRepeatFilter = new JCheckBox();
    JPanel jPanel1 = new JPanel();
    private JCheckBox maskLowCaseBox = new JCheckBox();
    private boolean stopButtonPushed;
    public BlastAppComponent() {
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
    public void jbInit() throws Exception {
        super.jbInit();
        pfpFilterBox = new JCheckBox();
        jBasicPane = new JPanel();
        DatabaseLabel = new JLabel();
        jTabbedPane1 = new JTabbedPane();
        jTabbedBlastPane = new JTabbedPane();
        
        /*********Code Implement By Anh Vu***************
        *********Contact: vietanh.vu@m4x.org************/
        jTabbedBlatPane = new JTabbedPane();
        jPanelMain = new JPanel();
        borderLayoutBlat1 = new BorderLayout();
        borderLayoutBlat2 = new BorderLayout();
        borderLayoutBlat3 = new BorderLayout();
        jPanelMain.setPreferredSize(new Dimension(364, 250));
        jPanelMain.setLayout(borderLayoutBlat1);
        jPanelMain.setMinimumSize(new Dimension(10, 100));
        jPanelSubBlat = new JPanel();
        jPanelSubBlat.setLayout(new XYLayout());
        jPanelUpBlat = new JPanel();
        jPanelUpBlat.setLayout(borderLayoutBlat2);
        jScrollPaneBlat = new JScrollPane();
        jLabelGenome = new JLabel();
        jLabelGenome.setText("Genome");
        jLabelAssembly = new JLabel();
        jLabelAssembly.setText("Assembly");
        jLabelQueryType = new JLabel();
        jLabelQueryType.setText("Query Type");
        jLabelSortOutput = new JLabel();
        jLabelSortOutput.setText("Sort Output");
        jLabelOutputType = new JLabel();
        jLabelOutputType.setText("Output Type");
        jToolBarBlat = new JToolBar();
        jProgressBarBlat = new JProgressBar();
        jProgressBarBlat.setMinimumSize(new Dimension(10, 26));
        jProgressBarBlat.setPreferredSize(new Dimension(104, 26));
        jProgressBarBlat.setOrientation(JProgressBar.HORIZONTAL);
        jProgressBarBlat.setBorder(BorderFactory.createEtchedBorder());
        jProgressBarBlat.setStringPainted(true);
        jButtonUpdate = new JButton();
        jButtonUpdate.setText("Update");
        jButtonUpdate.addActionListener(new
                                      BlastAppComponent_jButtonUpdate_actionAdapter(this));

        /*setting up the parameters like on Blat web search page*/
        String[] strGenome = {"Human", "Chimp", "Rhesus", "Dog", "Cow", "Mouse", "Rat", 
                            "Opossum", "Chicken", "X. tropicalis", "Zebrafish",
                            "Tetraodon", "Fugu", "C. intestinalis", "D. melanogaster", 
                            "D. simulans", "D. sechellia", "D. yakuba", "D. erecta", 
                            "D. ananassae", "D. persimilis", "D. pseudoobscura", "D. virilis", 
                            "D. mojavensis", "D. grimshawi", "A. mellifera", "A. gambiae", 
                            "C. elegans", "C. briggsae", "S. purpuratus", "S. cerevisiae", "SARS"};
        String[] strAssembly = {"Mar. 2006", "May 2004", "July 2003", "Apr. 2003"};
        String[] strAssemblyValue = {"hg18", "hg17", "hg16", "hg15"};
        String[] strQueryType = {"BLAT's guess", "DNA", "protein", "translated RNA", "translated DNA"};
        String[] strSortOutput = {"query,score", "query,start", "chrom,score", "chrom,start", "score"};
        String[] strOutputType = {"hyperlink", "psl", "psl no header"};
      
        jComboBoxGenome = new JComboBox(strGenome);
        jComboBoxAssembly = new JComboBox(strAssembly);
        jComboBoxAssembly.addActionListener(new BlastAppComponent_jComboBoxAssembly_actionAdapter(this));
        jComboBoxAssemblyValue = new JComboBox(strAssemblyValue);
        jComboBoxAssemblyValue.setVisible(false);
        jComboBoxQueryType = new JComboBox(strQueryType);
        jComboBoxSortOutput = new JComboBox(strSortOutput);
        jComboBoxOutputType = new JComboBox(strOutputType);
        jCheckBoxLucky = new JCheckBox();
        jCheckBoxLucky.setText("I'm feeling lucky");
        jCheckBoxOpenInBrowser = new JCheckBox();
        jCheckBoxOpenInBrowser.setText("Open Results In Web Browser");
        jCheckBoxOpenInBrowser.setSelected(true);
        Border borderBlat1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
        Border borderBlat2 = new TitledBorder(borderBlat1,
                                      "Please select the information from the lists below");
        jPanelSubBlat.setBorder(borderBlat2);
        jPanelSubBlat.add(jLabelGenome, new XYConstraints(0, 10, 60, 20));
        jPanelSubBlat.add(jLabelAssembly, new XYConstraints(0, 40, 60, 20));
        jPanelSubBlat.add(jLabelQueryType, new XYConstraints(0, 70, 60, 20));
        jPanelSubBlat.add(jLabelSortOutput, new XYConstraints(0, 100, 60, 20));
        jPanelSubBlat.add(jLabelOutputType, new XYConstraints(0, 130, 100, 20));
        jPanelSubBlat.add(jCheckBoxLucky, new XYConstraints(0, 180, 100, 20));
        jPanelSubBlat.add(jCheckBoxOpenInBrowser, new XYConstraints(0, 230, 200, 20));
        jPanelSubBlat.add(jComboBoxGenome, new XYConstraints(100, 10, 200, 20));
        jPanelSubBlat.add(jComboBoxAssembly, new XYConstraints(100, 40, 200, 20));
        jPanelSubBlat.add(jComboBoxAssemblyValue, new XYConstraints(350, 40, 100, 20));
        jPanelSubBlat.add(jComboBoxQueryType, new XYConstraints(100, 70, 200, 20));
        jPanelSubBlat.add(jComboBoxSortOutput, new XYConstraints(100, 100, 200, 20));
        jPanelSubBlat.add(jComboBoxOutputType, new XYConstraints(100, 130, 200, 20));
        jPanelSubBlat.add(jButtonUpdate, new XYConstraints(400, 70, 100, 20));
        jPanelUpBlat.add(jPanelSubBlat, java.awt.BorderLayout.CENTER);
        jScrollPaneBlat.getViewport().add(jPanelUpBlat);
        jPanelMain.add(jScrollPaneBlat, java.awt.BorderLayout.CENTER);
        jToolBarBlat.add(jProgressBarBlat);
        jPanelMain.add(jToolBarBlat, java.awt.BorderLayout.NORTH); 
        jTabbedBlatPane.add(jPanelMain, "BlatServer");
        
        /*Stand Alone Blat
         */
        jLabelOoc = new JLabel("Ooc");
        jLabelTileSize = new JLabel("Tile Size");
        jLabelOneOff = new JLabel("One Off");
        jLabelMinMatch = new JLabel("Min Match");
        jLabelMinScore = new JLabel("Min Score");
        jLabelMinIdentity = new JLabel("Min Identity");
        jLabelMaxGap = new JLabel("Max Gap");
        jLabelRepMatch = new JLabel("Rep Match");
        jLabelMinDivergence = new JLabel("Min Divergence");
        jLabelDots = new JLabel("Dots");
        jLabelSelectDatabase = new JLabel("Select Database");
        jLabelOutputFile = new JLabel("Output File");
        jLabelNoHead = new JLabel("No Head");
        jLabelMakeOoc = new JLabel("Make Ooc");
        jLabelTrimT = new JLabel("Trim T");
        jLabelNoTrimA = new JLabel("No Trim A");
        jLabelTrimHardA = new JLabel("Trim Hard A");
        jLabelFine = new JLabel("Fine");
        jLabelProt = new JLabel("Prot");
        jLabelDatabaseType = new JLabel("Database Type");
        jLabelQType = new JLabel("Query Type");
        jLabelMask = new JLabel("Mask");
        jLabelQMask = new JLabel("QMask");
        jLabelOut = new JLabel("Out");
        
        jTextFieldOoc = new JTextField();
        jTextFieldTileSize = new JTextField();
        jTextFieldOneOff = new JTextField();
        jTextFieldMinMatch = new JTextField();
        jTextFieldMinScore = new JTextField();
        jTextFieldMinIdentity = new JTextField();
        jTextFieldMaxGap = new JTextField();
        jTextFieldRepMatch = new JTextField();
        jTextFieldMinDivergence = new JTextField();
        jTextFieldDots = new JTextField();
        jTextFieldSelectDatabase = new JTextField();
        jTextFieldOutputFile = new JTextField();
    
        jCheckBoxNoHead = new JCheckBox();
        jCheckBoxMakeOoc = new JCheckBox();
        jCheckBoxTrimT = new JCheckBox();
        jCheckBoxNoTrimA = new JCheckBox();
        jCheckBoxTrimHardA = new JCheckBox();
        jCheckBoxFine = new JCheckBox();
        jCheckBoxProt = new JCheckBox();
        
        String[] sDatabaseType = {"dna", "prot", "dnax"};
        String[] sQType = {"dna", "rna", "prot", "dnax", "rnax"};
        String[] sMask = {"none","lower", "upper", "out", "file.out"}; 
        String[] sQMask = {"none","lower", "upper", "out", "file.out"}; 
        String[] sOut ={"psl", "pslx", "axt", "maf", "wublast", "blast"};
        jComboBoxDatabaseType = new JComboBox(sDatabaseType);
        jComboBoxQType = new JComboBox(sQType);
        jComboBoxQType.addActionListener(new BlastAppComponent_jComboBoxQType_actionAdapter(this));
        jComboBoxMask = new JComboBox(sMask);
        jComboBoxQMask = new JComboBox(sQMask);
        jComboBoxOut = new JComboBox(sOut);
        
        borderLayoutSBlat1 = new BorderLayout();
        borderLayoutSBlat2 = new BorderLayout();
        borderLayoutSBlat3 = new BorderLayout();
        
        jPanelStandAloneBlat = new JPanel();
        jPanelStandAloneBlat.setPreferredSize(new Dimension(364, 250));
        jPanelStandAloneBlat.setLayout(borderLayoutSBlat1);
        jPanelStandAloneBlat.setMinimumSize(new Dimension(10, 100));
        jPanelSubSBlat = new JPanel();
        jPanelSubSBlat.setLayout(new XYLayout());
        jPanelUpSBlat = new JPanel();
        jPanelUpSBlat.setLayout(borderLayoutSBlat2);
        
        jScrollPaneSBlat = new JScrollPane(); 
        borderSBlat1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
        borderSBlat2 = new TitledBorder(borderBlat1,
                                      "Please select the options below, check blat documents for more infor");
        
        jToolBarSBlat = new JToolBar();
        jProgressBarSBlat = new JProgressBar();
        jProgressBarSBlat.setMinimumSize(new Dimension(10, 26));
        jProgressBarSBlat.setPreferredSize(new Dimension(104, 26));
        jProgressBarSBlat.setOrientation(JProgressBar.HORIZONTAL);
        jProgressBarSBlat.setBorder(BorderFactory.createEtchedBorder());
        jProgressBarSBlat.setStringPainted(true);
    
        jButtonSetDefault = new JButton();
        jButtonSetDefault.setText("Reset Default");
        jButtonSetDefault.addActionListener(new BlastAppComponent_jButtonSetDefault_actionAdapter(this));
        jButtonBrowse = new JButton();
        jButtonBrowse.addActionListener(new BlastAppComponent_jButtonBrowse_actionAdapter(this));
        jButtonBrowse.setText("Browse");
        jButtonOpen = new JButton();
        jButtonOpen.addActionListener(new BlastAppComponent_jButtonOpen_actionAdapter(this));
        jButtonOpen.setText("Open File");
        
        jPanelSubSBlat.setBorder(borderSBlat2);
        jPanelSubSBlat.add(jLabelSelectDatabase, new XYConstraints(100, 10, 100, 20));
        jPanelSubSBlat.add(jTextFieldSelectDatabase, new XYConstraints(200, 10, 200, 20));
        jPanelSubSBlat.add(jButtonBrowse, new XYConstraints(400, 10, 100, 20));
        
        jPanelSubSBlat.add(jLabelDatabaseType, new XYConstraints(15, 100, 80, 20));
        jPanelSubSBlat.add(jLabelQType, new XYConstraints(130, 100, 80, 20));
        jPanelSubSBlat.add(jLabelMask, new XYConstraints(260, 100, 80, 20));
        jPanelSubSBlat.add(jLabelQMask, new XYConstraints(365, 100, 80, 20));
        jPanelSubSBlat.add(jLabelOut, new XYConstraints(480, 100, 80, 20));
        
        jPanelSubSBlat.add(jComboBoxDatabaseType, new XYConstraints(0, 130, 100, 20));
        jPanelSubSBlat.add(jComboBoxQType, new XYConstraints(110, 130, 100, 20));
        jPanelSubSBlat.add(jComboBoxMask, new XYConstraints(220, 130, 100, 20));
        jPanelSubSBlat.add(jComboBoxQMask, new XYConstraints(330, 130, 100, 20));
        jPanelSubSBlat.add(jComboBoxOut, new XYConstraints(440, 130, 100, 20));
        
        jPanelSubSBlat.add(jLabelOoc, new XYConstraints(0, 160, 80, 20));
        jPanelSubSBlat.add(jLabelTileSize, new XYConstraints(0, 190, 80, 20));
        jPanelSubSBlat.add(jLabelOneOff, new XYConstraints(0, 220, 80, 20));
        jPanelSubSBlat.add(jLabelMinMatch, new XYConstraints(0, 250, 80, 20));
        jPanelSubSBlat.add(jLabelMinScore, new XYConstraints(0, 280, 80, 20));
        jPanelSubSBlat.add(jLabelMinIdentity, new XYConstraints(0, 310, 80, 20));
        jPanelSubSBlat.add(jLabelMaxGap, new XYConstraints(0, 340, 80, 20));
        jPanelSubSBlat.add(jLabelRepMatch, new XYConstraints(0, 370, 80, 20));
        jPanelSubSBlat.add(jLabelMinDivergence, new XYConstraints(0, 400, 80, 20));
        jPanelSubSBlat.add(jLabelDots, new XYConstraints(0, 430, 80, 20));
        
        jPanelSubSBlat.add(jTextFieldOoc, new XYConstraints(90, 160, 30, 20));
        jPanelSubSBlat.add(jTextFieldTileSize, new XYConstraints(90, 190, 30, 20));
        jPanelSubSBlat.add(jTextFieldOneOff, new XYConstraints(90, 220, 30, 20));
        jPanelSubSBlat.add(jTextFieldMinMatch, new XYConstraints(90, 250, 30, 20));
        jPanelSubSBlat.add(jTextFieldMinScore, new XYConstraints(90, 280, 30, 20));
        jPanelSubSBlat.add(jTextFieldMinIdentity, new XYConstraints(90, 310, 30, 20));
        jPanelSubSBlat.add(jTextFieldMaxGap, new XYConstraints(90, 340, 30, 20));
        jPanelSubSBlat.add(jTextFieldRepMatch, new XYConstraints(90, 370, 30, 20));
        jPanelSubSBlat.add(jTextFieldMinDivergence, new XYConstraints(90, 400, 30, 20));
        jPanelSubSBlat.add(jTextFieldDots, new XYConstraints(90, 430, 30, 20));
        
        jPanelSubSBlat.add(jLabelNoHead, new XYConstraints(150, 250, 70, 20));
        jPanelSubSBlat.add(jLabelMakeOoc, new XYConstraints(150, 280, 70, 20));
        jPanelSubSBlat.add(jLabelTrimT, new XYConstraints(150, 310, 70, 20));
        jPanelSubSBlat.add(jLabelTrimHardA, new XYConstraints(150, 340, 70, 20));
        jPanelSubSBlat.add(jLabelFine, new XYConstraints(150, 370, 70, 20));
        jPanelSubSBlat.add(jLabelProt, new XYConstraints(150, 400, 70, 20));
        
        jPanelSubSBlat.add(jCheckBoxNoHead, new XYConstraints(225, 250, 30, 20));
        jPanelSubSBlat.add(jCheckBoxMakeOoc, new XYConstraints(225, 280, 30, 20));
        jPanelSubSBlat.add(jCheckBoxTrimT, new XYConstraints(225, 310, 30, 20));
        jPanelSubSBlat.add(jCheckBoxTrimHardA, new XYConstraints(225, 340, 30, 20));
        jPanelSubSBlat.add(jCheckBoxFine, new XYConstraints(225, 370, 30, 30));
        jPanelSubSBlat.add(jCheckBoxProt, new XYConstraints(225, 400, 30, 30));
        
        jPanelSubSBlat.add(jButtonSetDefault, new XYConstraints(400, 300, 120, 30));
        
        jPanelSubSBlat.add(jLabelOutputFile, new XYConstraints(100, 500, 100, 20));
        jPanelSubSBlat.add(jTextFieldOutputFile, new XYConstraints(200, 500, 200, 20));
        jPanelSubSBlat.add(jButtonOpen, new XYConstraints(400, 500, 100, 20));
        
        jPanelUpSBlat.add(jPanelSubSBlat, java.awt.BorderLayout.CENTER);
        jScrollPaneSBlat.getViewport().add(jPanelUpSBlat);
        jPanelStandAloneBlat.add(jScrollPaneSBlat, java.awt.BorderLayout.CENTER);
        jToolBarSBlat.add(jProgressBarSBlat);
        jPanelStandAloneBlat.add(jToolBarSBlat, java.awt.BorderLayout.NORTH); 
        jTabbedBlatPane.add(jPanelStandAloneBlat, "StandAloneBlat");
        standAloneBlatDefault();
        
        /*********End Code*******************************/
        
        jTabbedHmmPane = new JTabbedPane();
        jTabbedSmPane = new JTabbedPane();
        jServerInfoPane = new ServerInfoPanel();
        jServerInfoPane.setBlastAppComponent(this);
        jScrollPane4 = new JScrollPane();
                //JComboBox jMatrixBox = new JComboBox();
        lowComplexFilterBox = new JCheckBox();
        jAdvancedPane = new JPanel();
        JFileChooser jFileChooser1 = new JFileChooser();

        checkboxPanel = new JPanel();
        jDBList = new JList();
        blastButton = new JButton();
        jScrollPane1 = new JScrollPane();
        jProgramBox = new JComboBox();
        filterPanel = new JPanel();
        maskLookupOnlyBox = new JCheckBox();
        expectLabel = new JLabel();
        jExpectBox = new JComboBox();
        matrixLabel = new JLabel();
        blastxSettingPanel = new JPanel();
        jGapcostsBox = new JComboBox();
        jWordsizeLabel = new JLabel();
        jWordsizeBox = new JComboBox();
        parameterSetter = new ParameterSetter();

        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jstartPointField = new JTextField();
        jendPointField = new JTextField();
        cardLayout1 = new CardLayout();
        serviceProgressBar = new JProgressBar();
        progressBarPanel1 = new JPanel();
        jLabel5 = new JLabel();
        subSeqPanel1 = new JPanel();
        jPanel6 = new JPanel();
        jLabel6 = new JLabel();
        jProgramBox1 = new JComboBox();
        jLabel7 = new JLabel();
        databaseLabel1 = new JLabel();
        jPanel7 = new JPanel();
        jLabel8 = new JLabel();
        gridBagLayout7 = new GridBagLayout();
        jAlgorithmLabel = new JLabel();
        jendPointField1 = new JTextField();
        jPanel8 = new JPanel();
        jOtherAlgorithemPane = new JPanel();
        gridBagLayout8 = new GridBagLayout();
        flowLayout2 = new FlowLayout();
        jstartPointField1 = new JTextField();
        borderLayout5 = new BorderLayout();
        jPanel9 = new JPanel();
        progressBar1 = new JProgressBar();
        gridBagLayout9 = new GridBagLayout();
        jScrollPane2 = new JScrollPane();
        jList2 = new JList(algorithmParameter);
        gridBagLayout13 = new GridBagLayout();
        borderLayout4 = new BorderLayout();
        jDisplayInWebBox = new JCheckBox();
        borderLayout1 = new BorderLayout();
        gridBagLayout3 = new GridBagLayout();
        jGapcostsLabel = new JLabel();
        gridBagLayout2 = new GridBagLayout();
        jButton1 = new JButton();
        blastStopButton = new JButton();
        algorithmSearch = new JButton();
        jLabel13 = new JLabel();
        progressBar3 = new JProgressBar();
        jendPointField3 = new JTextField();
        progressBarPanel3 = new JPanel();
        flowLayout4 = new FlowLayout();
        gridBagLayout14 = new GridBagLayout();
        borderLayout8 = new BorderLayout();
        jHMMPane = new JPanel();
        subSeqPanel3 = new JPanel();
        jPanel11 = new JPanel();
        jLabel14 = new JLabel();
        jPanel14 = new JPanel();
        jPanel15 = new JPanel();
        borderLayout9 = new BorderLayout();
        jLabel15 = new JLabel();
        jstartPointField3 = new JTextField();
        jList4 = new JList(hmmParameter);
        jPanel16 = new JPanel();
        jAlgorithmLabel2 = new JLabel();
        jLabel16 = new JLabel();
        flowLayout5 = new FlowLayout();
        jButton2 = new JButton();
        jScrollPane3 = new JScrollPane();
        borderLayout6 = new BorderLayout();
        gridBagLayout4 = new GridBagLayout();

        jToolBar2 = new JToolBar();
        titledBorder1 = new TitledBorder("");
        border1 = BorderFactory.createEtchedBorder(Color.white,
                new Color(165, 163, 151));
        border2 = new TitledBorder(border1,
                                   "Please specify subsequence, program and database");
        jToolBar1 = new JToolBar();

        jLabel9 = new JLabel();
        xYLayout1 = new XYLayout();
        startButtonIcon = new ImageIcon(this.getClass().getResource(
                "start.gif"));
        stopButtonIcon = new ImageIcon(this.getClass().getResource(
                "stop.gif"));
        jPanel2 = new JPanel();
        jPanel3 = new JPanel();
        borderLayout2 = new BorderLayout();
        xYLayout2 = new XYLayout();
        borderLayout3 = new BorderLayout();

//above is part of code to get rid of npe.
        //sgePanel.setPv(this);
        allArraysCheckBox = new JCheckBox("Activated Sequences", true);
        subSeqPanel = new JPanel();
        subSeqPanel.setBorder(border2);
        jLabel4 = new JLabel();
        jLabel4.setText("jLabel4");
        jLabel9.setText("Program: ");
        jToolBar1.setBorder(null);
        serviceProgressBar.setMinimumSize(new Dimension(10, 26));
        serviceProgressBar.setPreferredSize(new Dimension(104, 26));

        checkboxPanel.setLayout(xYLayout2);
        jBasicPane.setPreferredSize(new Dimension(364, 250));
        jPanel3.setLayout(borderLayout3);
        //this.add(jLabel4, java.awt.BorderLayout.NORTH);
        pfpFilterBox.setToolTipText("Paracel Filtering Package");
        pfpFilterBox.setSelected(false);
        pfpFilterBox.setText("PFP Filter");
        //   jEntThreshBox.addActionListener(new
        //                                  ParameterPanel_jEntThreshBox_actionAdapter(this));
        jBasicPane.setLayout(borderLayout2);
        //jBasicPane.setPreferredSize(new Dimension(10, 100));
        jBasicPane.setMinimumSize(new Dimension(10, 100));
        //jDecreaseDensitySupportBox.setSelectedIndex(0);
        DatabaseLabel.setText("Database:");
        // jServerInfoPane.setPv(this);
        jServerInfoPane.setLayout(cardLayout1);
        //jMatrixBox.setSelectedIndex(0);
        lowComplexFilterBox.setMinimumSize(new Dimension(10, 23));
        lowComplexFilterBox.setMnemonic('0');
        lowComplexFilterBox.setSelected(true);
        lowComplexFilterBox.setText("Low Complexity");
        lowComplexFilterBox.addActionListener(new
                                              BlastAppComponent_lowComplexFilterBox_actionAdapter(this));
        jAdvancedPane.setLayout(gridBagLayout3);
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
        //blastButton.setText("BLAST");
        blastButton.setIcon(startButtonIcon);

        blastButton.setToolTipText("Start BLAST");

        blastButton.addActionListener(new
                                      BlastAppComponent_blastButton_actionAdapter(this));
        jTabbedPane1.setDebugGraphicsOptions(0);
        jTabbedPane1.setMinimumSize(new Dimension(5, 5));
        jProgramBox.addActionListener(new
                                      BlastAppComponent_jProgramBox_actionAdapter(this));
        jMatrixBox = new JComboBox();
        jMatrixBox.addActionListener(new
                                     BlastAppComponent_jMatrixBox_actionAdapter(this));
        // this.setLayout(borderLayout1);
        maskLookupOnlyBox.setText("Mask for lookup table only");
        maskLookupOnlyBox.setMinimumSize(new Dimension(5, 23));
        maskLookupOnlyBox.setMnemonic('0');
        maskLookupOnlyBox.setSelected(false);
        expectLabel.setText("Matrix:");
        jMatrixBox.addItem("dna.mat");
        jMatrixBox.setToolTipText("Select the Matrix.");
        jMatrixBox.setVerifyInputWhenFocusTarget(true);
        jMatrixBox.setSelectedIndex(0);

        jExpectBox.setSelectedIndex( -1);
        jExpectBox.setVerifyInputWhenFocusTarget(true);
        jExpectBox.setToolTipText("Select the expect value here.");
        matrixLabel.setText("Expect:");
        jGapcostsBox.setVerifyInputWhenFocusTarget(true);
        jGapcostsBox.setToolTipText("Select gap cost:");
        jWordsizeBox.setToolTipText(
                "Select the word size, default is 3 for blastp, 11 for blastn.");
        jWordsizeBox.addActionListener(new
                                       BlastAppComponent_jFrameShiftPaneltyBox_actionAdapter(this));
        blastxSettingPanel.setLayout(gridBagLayout2);
        jServerInfoPane.setMinimumSize(new Dimension(0, 0));
        //jServerInfoPane.setPreferredSize(new Dimension(0, 0));
        jServerInfoPane.setToolTipText("Blast server Info");
        jProgramBox.setAutoscrolls(false);
        jProgramBox.setMinimumSize(new Dimension(26, 21));
        subSeqPanel.setLayout(xYLayout1);
        jLabel1.setText("to ");
        jLabel2.setText("Subsequence: From");
        jLabel3.setText("Subsequence: ");
        jstartPointField.setText("1");
        jendPointField.setText("");
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
        //                                 BlastAppComponent_blastButton2_actionAdapter(this));
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
        jGapcostsLabel.setToolTipText("");
        jGapcostsLabel.setText("Gap costs:");
        jButton1.setFont(new java.awt.Font("Arial Black", 0, 11));
        jButton1.setIcon(stopButtonIcon);
        //jButton1.setText("STOP");

        jButton1.addActionListener(new
                                   BlastAppComponent_jButton1_actionAdapter(this));
        blastStopButton.setFont(new java.awt.Font("Arial Black", 0, 11));
        blastStopButton.setVerifyInputWhenFocusTarget(true);
        //blastStopButton.setText("STOP");
        blastStopButton.setIcon(stopButtonIcon);
        blastStopButton.setToolTipText("Stop the Query");

        blastStopButton.addActionListener(new
                                          BlastAppComponent_blastStopButton_actionAdapter(this));
        algorithmSearch.setFont(new java.awt.Font("Arial Black", 0, 11));
        //algorithmSearch.setText("SEARCH");
        algorithmSearch.setIcon(startButtonIcon);
        algorithmSearch.addActionListener(new
                                          BlastAppComponent_algorithmSearch_actionAdapter(this));
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
        jButton2.setFont(new java.awt.Font("Arial Black", 0, 11));
        //jButton2.setText("HMM SEARCH");
        jButton2.setIcon(startButtonIcon);
        jButton2.addActionListener(new
                                   BlastAppComponent_jButton2_actionAdapter(this));
        humanRepeatFilter = new JCheckBox();
        humanRepeatFilter.setToolTipText("Human Repeat Filter");
        humanRepeatFilter.setSelected(false);
        humanRepeatFilter.setText("Human Repeats Filter");
        maskLowCaseBox = new JCheckBox();
        maskLowCaseBox.setToolTipText("Filterl lower case sequences.");
        maskLowCaseBox.setText("Mask lower case");
        jWordsizeLabel.setText("Word size:");
        jToolBar2.add(serviceProgressBar);
        serviceProgressBar.setOrientation(JProgressBar.HORIZONTAL);
        serviceProgressBar.setBorder(BorderFactory.createEtchedBorder());
        serviceProgressBar.setStringPainted(true);
        //jPanel6.add(algorithmSearch, null);
        //jPanel6.add(jButton1, null);
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
        jTabbedPane1.add(jTabbedBlastPane, "BLAST");
        
        /*********Code Implement By Anh Vu***************
        *********Contact: vietanh.vu@m4x.org************/
       // jTabbedPane1.add(jTabbedBlatPane, "BLAT");
        /*********End Code*******************************/
        
        //jTabbedPane1.add(jHMMPane, "HMM");
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
        //jTabbedPane1.add(jOtherAlgorithemPane, "Other Algorithms");
        jHMMPane.add(progressBarPanel3,
                     new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                            , GridBagConstraints.CENTER,
                                            GridBagConstraints.BOTH,
                                            new Insets(0, 0, 0, 0), 291, -2));
        progressBarPanel3.add(progressBar3, BorderLayout.SOUTH);
        jHMMPane.add(jPanel14, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 390, -18));
        jHMMPane.add(jPanel16, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 386, 1));
        jPanel16.add(jScrollPane3, BorderLayout.CENTER);
        jPanel16.add(jAlgorithmLabel2, BorderLayout.NORTH);
        jHMMPane.add(jPanel15, new GridBagConstraints(0, 5, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(7, 0, 3, 0), 192, 3));
        //HMM button removed.
        //jPanel15.add(jButton2, null);
        jHMMPane.add(jPanel11, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 183, 26));
        jPanel11.add(jLabel16, BorderLayout.CENTER);
//        jHMMPane.add(subSeqPanel3, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
//                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
//                new Insets(0, 0, 0, 0), 98, 7));
        jScrollPane3.getViewport().add(jList4, null);
        filterPanel.add(lowComplexFilterBox, null);
        filterPanel.add(maskLowCaseBox);
        filterPanel.add(maskLookupOnlyBox, null);
        filterPanel.add(humanRepeatFilter);
        filterPanel.add(pfpFilterBox, null);
        jPanel1 = new JPanel();
        filterPanel.add(jPanel1);
        jPanel1.add(jDisplayInWebBox);
        jTabbedBlastPane.add(jBasicPane, "Main");
        jTabbedBlastPane.add(jAdvancedPane, "Advanced Options");
        jTabbedBlastPane.add(jServerInfoPane, "Service");
        mainPanel.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        // mainPanel.add(checkboxPanel, BorderLayout.SOUTH);
        jToolBar1.add(jLabel2);
        jToolBar1.add(jstartPointField);
        jToolBar1.add(jLabel1);
        jToolBar1.add(jendPointField);
        subSeqPanel.add(jScrollPane1, new XYConstraints(0, 89, 352, 97));
        subSeqPanel.add(jLabel9, new XYConstraints(0, 36, 60, 23));
        subSeqPanel.add(DatabaseLabel, new XYConstraints(0, 59, 61, 23));
        subSeqPanel.add(jProgramBox, new XYConstraints(84, 36, 267, 25)); //edit for new class.
       // subSeqPanel.add(jToolBar1, new XYConstraints( -1, 0, 353, 27));
        displayToolBar.add(Box.createHorizontalStrut(10), null);
        displayToolBar.add(blastButton);
        displayToolBar.add(Box.createHorizontalStrut(5), null);
        displayToolBar.add(blastStopButton);
        jScrollPane4.getViewport().add(jPanel3);
        jPanel3.add(subSeqPanel, java.awt.BorderLayout.CENTER);
        jBasicPane.add(jScrollPane4, java.awt.BorderLayout.CENTER);
        jBasicPane.add(jToolBar2, java.awt.BorderLayout.NORTH);
        blastxSettingPanel.add(jMatrixBox,
                               new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(expectLabel,
                               new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jWordsizeLabel,
                               new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jWordsizeBox,
                               new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(matrixLabel,
                               new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 2), 0, 0));
        blastxSettingPanel.add(jExpectBox,
                               new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        jAdvancedPane.add(filterPanel,
                          new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), -150,
                                                 82));
        blastxSettingPanel.add(jGapcostsLabel,
                               new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.NONE,
                new Insets(2, 2, 2, 6), 1, -2));
        blastxSettingPanel.add(jGapcostsBox,
                               new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
                , GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                new Insets(2, 2, 2, 2), 0, 0));
        jAdvancedPane.add(blastxSettingPanel,
                          new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(0, 2, 1, 3), 0, 23));
        jExpectBox.addItem("10");
        jExpectBox.addItem("1");

        jExpectBox.addItem("0.1");
        jExpectBox.addItem("0.01");
        jExpectBox.addItem("0.000000001");
        jExpectBox.addItem("100");

        jExpectBox.addItem("1000");
        jExpectBox.setEditable(true);
        jGapcostsBox.addItem("Existence: 11 Extension: 1");
        jGapcostsBox.addItem("Existence:  9 Extension: 2");
        jGapcostsBox.addItem("Existence:  8 Extension: 2");
        jGapcostsBox.addItem("Existence:  7 Extension: 2");
        jGapcostsBox.addItem("Existence: 12 Extension: 1");
        jGapcostsBox.addItem("Existence: 10 Extension: 1");

        jWordsizeBox.addItem("3");
        jWordsizeBox.addItem("2");

        jWordsizeBox.addItem("7");

        jWordsizeBox.addItem("11");

        jWordsizeBox.addItem("15");

//        jWordsizeBox.addItem("16");
//        jWordsizeBox.addItem("17");
//        jWordsizeBox.addItem("18");
//        jWordsizeBox.addItem("19");
//        jWordsizeBox.addItem("20");
//        jWordsizeBox.addItem("25");
//        jWordsizeBox.addItem("50");
//        jWordsizeBox.addItem("100");
//
//        jWordsizeBox.addItem("1000");
        jTabbedPane1.setSelectedComponent(jTabbedBlastPane);
        jTabbedBlastPane.setSelectedComponent(jBasicPane);
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

        //System.out.println(newItem + "selected the program" + e.getActionCommand());
        String selectedProgramName = (String) cb.getSelectedItem();
        if (selectedProgramName != null) {
            jDBList = new JList(AlgorithmMatcher.translateToArray((String)
                    selectedProgramName));
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
        //jScrollPanel = new JScrollPanel(jDBList);
        // repaint();
    }

    void jMatrixBox_actionPerformed(ActionEvent e) {
        String[] model = AlgorithmMatcher.translateToGapcosts(
                jMatrixBox.getSelectedItem().toString());
        jGapcostsBox.setModel(new DefaultComboBoxModel(model));
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
        int endPoint = 0;
        if (sd != null) {

            endPoint = sd.getMaxLength();
            jendPointField.setText(new Integer(endPoint).toString());
            jendPointField1.setText(new Integer(endPoint).toString());
            jendPointField3.setText(new Integer(endPoint).toString());

            String[] model = AlgorithmMatcher.translateToPrograms(
                    sd.isDNA());
            jProgramBox.setModel(new DefaultComboBoxModel(model));

        }

    }


    public void setStopButtonPushed(boolean stopButtonPushed) {
        this.stopButtonPushed = stopButtonPushed;
    }

    public void reportError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    public ParameterSetter processNCBIParameters() {
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
                    String tempFolder = System.getProperties().getProperty(
                            "temporary.files.directory");
                    if (tempFolder == null) {
                        tempFolder = ".";

                    }

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
    public ParameterSetting collectParameters() {
        ParameterSetting ps = new ParameterSetting();
        String dbName = (String) jDBList.getSelectedValue();
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

        } else {
            StringTokenizer st = new StringTokenizer(dbName);
            dbName = st.nextToken();

        }

        boolean lowComplexFilterOn = lowComplexFilterBox.isSelected();
        boolean humanRepeatFilterOn = humanRepeatFilter.isSelected();
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
        ps.setMatrix((String) jMatrixBox.getSelectedItem());
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
     * Collect selected parameters.
     * @return ParameterSetter
     */

    public ParameterSetter processParameters() {
        ParameterSetting parameterSetting = collectParameters();
        if (parameterSetting == null) {
            return null;
        }
        parameterSetting.setUseNCBI(false);
        //jServerInfoPane.retriveServerInfo();
        if (fastaFile == null) {
            if (sequenceDB == null) {
                reportError("Please select a sequence file first!",
                            "Parameter Error");
                return null;
            }

        } else {

            try {

                String tempFolder = System.getProperties().getProperty(
                        "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";

                }

                String outputFile = tempFolder + "Blast" +
                                    RandomNumberGenerator.getID() +
                                    ".html";

                if (fastaFile == null) {
                    fastaFile = activeSequenceDB;
                }
                SoapClient sc = new SoapClient(parameterSetting.getProgramName(),
                                               parameterSetting.getDbName(),
                                               outputFile);
                serviceProgressBar.setForeground(Color.ORANGE);
                serviceProgressBar.setBackground(Color.WHITE);
                serviceProgressBar.setIndeterminate(true);
                serviceProgressBar.setString(
                        "Blast is running on the Columbia Blast Server.");

                BlastAlgorithm blastAlgo = new BlastAlgorithm();
                sc.setSequenceDB(activeSequenceDB);
                sc.setParentSequenceDB(sequenceDB);
                sc.setCmd(AlgorithmMatcher.translateToCommandline(
                        parameterSetting));
                blastAlgo.setBlastAppComponent(this);
                blastAlgo.setSoapClient(sc);
                blastAlgo.setStartBrowser(parameterSetting.isViewInBrowser());
                blastAlgo.start();
                Thread.sleep(2);
                if (blastAlgo != null && parameterSetter != null) {
                    parameterSetter.setAlgo(blastAlgo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return parameterSetter;

    }
    /*********Code Implement By Anh Vu***************
     *********Contact: vietanh.vu@m4x.org************
     *Collect all selected parameters and save it to*
     * a ParameterBlatSetting object. @return this*** 
     * ParameterBlatSetting*************************/
    public ParameterBlatSetting collectBlatParameters(){
        ParameterBlatSetting ps = new ParameterBlatSetting();
        ps.setStringGenome((String) jComboBoxGenome.getSelectedItem());
        ps.setStringAssembly((String) jComboBoxAssemblyValue.getSelectedItem());
        ps.setStringOutputType((String) jComboBoxOutputType.getSelectedItem());
        ps.setStringQueryType((String) jComboBoxQueryType.getSelectedItem());
        ps.setStringSortOutput((String) jComboBoxSortOutput.getSelectedItem());
        ps.setBooleanFeelLucky(jCheckBoxLucky.isSelected()); 
        ps.setBooleanOpenInBrowser(jCheckBoxOpenInBrowser.isSelected());
        return ps;
    }
    
    public ParameterSetter processBlatParameters(){
        ParameterBlatSetting parameterBlatSetting = collectBlatParameters();
        if (parameterBlatSetting == null) {
            return null;
        }
        if (fastaFile == null) {
            if (sequenceDB == null) {
                reportError("Unknown input fasta file", "Parameter Error");
                return null;
            }
        } else {

            try {
                String tempFolder = System.getProperties().getProperty(
                        "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";
                }
                String outputFile = tempFolder + "Blat" +
                                    RandomNumberGenerator.getID() +
                                    ".html";

                if (fastaFile == null) {
                    fastaFile = activeSequenceDB;
                }
                jProgressBarBlat.setForeground(Color.ORANGE);
                jProgressBarBlat.setBackground(Color.WHITE);
                jProgressBarBlat.setIndeterminate(true);
                jProgressBarBlat.setString(
                        "Blat is running on the Web Blat Server.");
                SoapClient sc = new SoapClient();
                sc.setSequenceDB(activeSequenceDB);
                BlatAlgorithm blatAlgo = new BlatAlgorithm();
                blatAlgo.setBlastAppComponent(this);
                blatAlgo.setParameterBlatSetting(parameterBlatSetting);
                blatAlgo.setSoapClient(sc);
                blatAlgo.start();
                Thread.sleep(2);
                if (blatAlgo != null && parameterSetter != null) {
                    parameterSetter.setAlgo(blatAlgo);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return parameterSetter;
    } 
    /*********End Code*******************************/   
    
    /*********Code Implement By Anh Vu***************
    *********Contact: vietanh.vu@m4x.org************
    ****Set up the command Line for Stand Alone Blat*/
    public String sBlatCommandLine(String sDatabase, String sQuery, String sOutput){
        String sCommandLine = "";
        sCommandLine += SCRIPT_DIRECTORY;
        sCommandLine += "\\blat ";
        sCommandLine += sDatabase;
        sCommandLine += " ";
        sCommandLine += sQuery;
        sCommandLine += " -t=";
        sCommandLine += jComboBoxDatabaseType.getSelectedItem();
        sCommandLine += " -q=";
        sCommandLine += jComboBoxQType.getSelectedItem();
        sCommandLine += " ";
        if(jCheckBoxProt.isSelected())
            sCommandLine += "-prot ";
        //not implemented yet
        /*sCommandLine += "-ooc=";
        sCommandLine += jTextFieldOoc.getText();
        sCommandLine += ".ooc ";*/
        sCommandLine += "-tileSize=";
        sCommandLine += jTextFieldTileSize.getText();
        sCommandLine += " -oneOff=";
        sCommandLine += jTextFieldOneOff.getText();
        sCommandLine += " -minMatch=";
        sCommandLine += jTextFieldMinMatch.getText();
        sCommandLine += " -minScore=";
        sCommandLine += jTextFieldMinScore.getText();
        sCommandLine += " -minIdentity=";
        sCommandLine += jTextFieldMinIdentity.getText();
        sCommandLine += " maxGap=";
        sCommandLine += jTextFieldMaxGap.getText();
        sCommandLine += " ";
        if(jCheckBoxNoHead.isSelected())
            sCommandLine += "-noHead ";
        //option not implement yet
        /*sCommandLine += "-makeOoc";
        sCommandLine += jTextFieldMakeOoc.getText();*/
        sCommandLine += " -repMatch=";
        sCommandLine += jTextFieldRepMatch.getText();
        sCommandLine += " ";
        if(!jComboBoxMask.getSelectedItem().equals(DEFAULT_MASK)){
            sCommandLine += "-mask=";
            sCommandLine += jComboBoxMask.getSelectedItem();
            sCommandLine += " ";
        }
        
        if(!jComboBoxQMask.getSelectedItem().equals(DEFAULT_QMASK)){
            sCommandLine += "-qMask=";
            sCommandLine += jComboBoxQMask.getSelectedItem();
            sCommandLine += " ";
        }
        sCommandLine += "-minRepDivergence="; 
        sCommandLine += jTextFieldMinDivergence.getText();
        sCommandLine += " dots=";
        sCommandLine += jTextFieldDots.getText();
        if(jCheckBoxTrimT.isSelected())
            sCommandLine += " -trimT";
        if(jCheckBoxNoTrimA.isSelected())
            sCommandLine += " -noTrimA";
        if(jCheckBoxTrimHardA.isSelected())
            sCommandLine += " -trimHardA";
        sCommandLine += " -out=";
        sCommandLine += jComboBoxOut.getSelectedItem();
        if(jCheckBoxFine.isSelected())
            sCommandLine += " -fine";
        sCommandLine += " ";
        sCommandLine += sOutput;
        return sCommandLine;
    }
    /*********End Code*******************************/   
    
    public void retriveAlgoParameters() {

        if (jTabbedPane1.getSelectedIndex() == this.SW) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
            return;

        }
        if (jTabbedPane1.getSelectedIndex() == this.HMM) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
            return;

        }
        if (fastaFile == null && sequenceDB == null) {
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
//        if (matrix == null) {
//            reportError("Please select a matrix name first!",
//                        "No Matrix Error");
//            return;
//
//        }

        if (jTabbedPane1.getSelectedIndex() == this.SW) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
            return;

        }
        if (jTabbedPane1.getSelectedIndex() == this.HMM) {
            reportError("Sorry, the backend server is unreachable now!",
                        "No Available Server Error");
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

            } else if (cmd.startsWith("btk hmm")) {
                progressBar3.setIndeterminate(false);

                progressBar3.setForeground(Color.ORANGE);
                progressBar3.setBackground(Color.ORANGE);
                progressBar3.setString("Finished on " + finished_Date);

            }

        }
    }

    void blastButton_actionPerformed(ActionEvent e) {
//        System.out.println("thenumber=" + jTabbedPane1.getSelectedIndex());
        stopButtonPushed = false;
        if (jTabbedPane1.getSelectedIndex() == this.BLAST) {
            jTabbedBlastPane.setSelectedIndex(this.MAIN);
            if (jServerInfoPane.getServerType() ==
                ServerInfoPanel.COLUMBIA) {
                parameterSetter = processParameters();
            } else if (jServerInfoPane.getServerType() == ServerInfoPanel.NCBI) {
                parameterSetter = processNCBIParameters();

            }
        } 
        else if (jTabbedPane1.getSelectedIndex() == this.BLAT){
            if(jTabbedBlatPane.getSelectedIndex() == BLAT_SERVER)
                parameterSetter = processBlatParameters();
            else{//Stand Alone Blat
                processSBlat();
            }
        }
        else {
            retriveAlgoParameters();
        }

    }
    /*********Code Implement By Anh Vu***************
     *********Contact: vietanh.vu@m4x.org************/
    public void blatFinished(String cmd) {
        Date finished_Date = new Date();
        if (cmd.startsWith("Interrupted")) {
            jProgressBarBlat.setIndeterminate(false);
            jProgressBarBlat.setForeground(Color.ORANGE);
            jProgressBarBlat.setBackground(Color.ORANGE);
            jProgressBarBlat.setString("Blat Search Stopped On " + finished_Date);
        }
    }
    /*********End Code*******************************/ 
    
    public void setBlastDisplayPanel(int selectedPanel) {
        if (selectedPanel == this.SERVER) {
            jTabbedBlastPane.setSelectedIndex(this.SERVER);

        } else {
            jTabbedBlastPane.setSelectedIndex(this.MAIN);
        }

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

    protected void fireModelChangedEvent(MicroarraySetViewEvent event) {
        setFastaFile(activeSequenceDB);
    }

    void stopBlastAction() {
        stopButtonPushed = true;
        if (this.jTabbedPane1.getSelectedIndex() == this.BLAST)
            blastFinished("Interrupted");
        else if (this.jTabbedPane1.getSelectedIndex() == this.BLAT)
            blatFinished("Interrupted");
        if (parameterSetter != null) {

            BWAbstractAlgorithm algo = parameterSetter.getAlgo();
            if (algo != null) {
                algo.stop();
            }
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

     /*********Code Implement By Anh Vu***************
     *********Contact: vietanh.vu@m4x.org************/
    public void updateProgressBarBlat(final double percent, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    jProgressBarBlat.setString(text);
                    jProgressBarBlat.setValue((int) (percent * 100));
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void updateProgressBarBlat(final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    jProgressBarBlat.setString(text);
                    jProgressBarBlat.setIndeterminate(true);
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }

    public void updateProgressBarBlat(final boolean boo, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    jProgressBarBlat.setString(text);
                    jProgressBarBlat.setIndeterminate(boo);
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }
    
     /*********End Code*******************************/   
    
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
        if (JFileChooser.APPROVE_OPTION ==
            jFileChooser1.showOpenDialog(mainPanel)) {
            // Call openFile to attempt to load the text from file into TextArea
            openFile(jFileChooser1.getSelectedFile().getPath());
        }
        mainPanel.repaint();

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
    /*********Code Implement By Anh Vu***************
     *********Contact: vietanh.vu@m4x.org************
     *Setting up the default parameters for Stand****
     *Alone Blat Application*************************/
    
    public void standAloneBlatDefault(){
        jTextFieldOoc.setText(DEFAULT_Ooc);
        jTextFieldTileSize.setText(DEFAULT_TILE_SIZE_DNA);
        jTextFieldOneOff.setText(DEFAULT_ONE_OFF);
        jTextFieldMinMatch.setText(DEFAULT_MIN_MATCH_NUCLEOTIDE);
        jTextFieldMinScore.setText(DEFAULT_MIN_SCORE);
        jTextFieldMinIdentity.setText(DEFAULT_MIN_IDENTITY_NUCLEOTIDE);
        jTextFieldMaxGap.setText(DEFAULT_MAX_GAP);
        jTextFieldRepMatch.setText(DEFAULT_REP_MATCH);
        jTextFieldMinDivergence.setText(DEFAULT_MIN_DIVERGENCE);
        jTextFieldDots.setText(DEFAULT_DOTS);
        jComboBoxDatabaseType.setSelectedIndex(0);
        jComboBoxQType.setSelectedIndex(0);
        jComboBoxMask.setSelectedIndex(0);
        jComboBoxQMask.setSelectedIndex(0);
        jComboBoxOut.setSelectedIndex(0);
        jTextFieldSelectDatabase.setText(DEFAULT_DATABASE_DIRECTORY);
    }
    
    public void processSBlat(){
        String sCommandLine;
        File f = new File(jTextFieldSelectDatabase.getText());
        File files[] = f.listFiles();
        String sOutput = DEFAULT_OUTPUT_RESULT + "\\" + RandomNumberGenerator.getID() + ".sql";
        String sQuery = "\"" + activeSequenceDB.getFile().getAbsolutePath() + "\"";
        for(int i=0;i<files.length;i++){
            if(files[i].isFile()){
                String sDatabase = "\"" + files[i].getAbsolutePath() + "\"";
                sCommandLine = sBlatCommandLine(sDatabase, sQuery, sOutput);
                RunCommand runCommand = new RunCommand(sCommandLine);
                try{            
                    Runtime rt = Runtime.getRuntime();
                    Process proc = rt.exec(sCommandLine);
                } catch (Throwable t){
                t.printStackTrace();
                }
            }
        }
        jTextFieldOutputFile.setText(sOutput);
    }
}


class BlastAppComponent_jProgramBox_actionAdapter implements java.awt.
        event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jProgramBox_actionAdapter(BlastAppComponent
                                                adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jProgramBox_actionPerformed(e);
    }
}


class BlastAppComponent_jMatrixBox_actionAdapter implements java.awt.
        event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jMatrixBox_actionAdapter(BlastAppComponent
                                               adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jMatrixBox_actionPerformed(e);
    }
}


class BlastAppComponent_jFrameShiftPaneltyBox_actionAdapter implements
        java.awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jFrameShiftPaneltyBox_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jFrameShiftPaneltyBox_actionPerformed(e);
    }
}


class BlastAppComponent_lowComplexFilterBox_actionAdapter implements
        java.awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_lowComplexFilterBox_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.lowComplexFilterBox_actionPerformed(e);
    }
}


class BlastAppComponent_blastButton_actionAdapter implements java.awt.
        event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_blastButton_actionAdapter(BlastAppComponent
                                                adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.blastButton_actionPerformed(e);
    }
}


/*
 class BlastAppComponent_blastButton1_actionAdapter implements java.awt.event.ActionListener {
  BlastAppComponent adaptee;

  BlastAppComponent_blastButton1_actionAdapter(BlastAppComponent adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.blastButton1_actionPerformed(e);
  }
 }
 */

class BlastAppComponent_jButton1_actionAdapter implements java.awt.event.
        ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_jButton1_actionAdapter(BlastAppComponent
                                             adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton1_actionPerformed(e);
    }
}


class BlastAppComponent_blastStopButton_actionAdapter implements java.
        awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_blastStopButton_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.blastStopButton_actionPerformed(e);
    }
}


class BlastAppComponent_algorithmSearch_actionAdapter implements java.
        awt.event.ActionListener {
    BlastAppComponent adaptee;

    BlastAppComponent_algorithmSearch_actionAdapter(
            BlastAppComponent
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.algorithmSearch_actionPerformed(e);
    }
}


class BlastAppComponent_jButton2_actionAdapter implements java.awt.event.
        ActionListener {
    BlastAppComponent adaptee;
    
    BlastAppComponent_jButton2_actionAdapter(BlastAppComponent
                                             adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton2_actionPerformed(e);
    }
}

/*********Code Implement By Anh Vu***************
 *********Contact: vietanh.vu@m4x.org************
 **Add an actionlistener to jComboBoxAssembly****
 **so everytime its selected item changes, the***
 **jComboBoxAssemblyValue's selected item *******
 **changes accordingly***************************
 ************************************************
 **Add an actionlistener to jButtonUpdate********
 **so this button will take information from web*
 **server to update current available options****/

class BlastAppComponent_jComboBoxAssembly_actionAdapter implements java.awt.event.ActionListener{
    BlastAppComponent adaptee;
    BlastAppComponent_jComboBoxAssembly_actionAdapter(BlastAppComponent adaptee){
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e){
        this.adaptee.jComboBoxAssemblyValue.setSelectedIndex(this.adaptee.jComboBoxAssembly.getSelectedIndex());
    }
}
 
class BlastAppComponent_jButtonUpdate_actionAdapter implements java.awt.event.ActionListener{
    BlastAppComponent adaptee;
    BlastAppComponent_jButtonUpdate_actionAdapter(BlastAppComponent adaptee){
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e){  
        try {
            Socket s = new Socket(org.geworkbench.components.alignment.blast.RemoteBlat.getBlatServer(),
                                  org.geworkbench.components.alignment.blast.RemoteBlat.getBlatPort());

            //create an output stream for sending message.
            DataOutputStream out = new DataOutputStream(s.getOutputStream());
            
            //create buffered reader stream for reading incoming byte stream.
            InputStreamReader inBytes = new InputStreamReader(s.getInputStream());
            BufferedReader in = new BufferedReader(inBytes);
            
            out.writeBytes("PUT /cgi-bin/hgBlat\r\n");
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("<SELECT NAME");
            while(true){
                String sData = in.readLine();
                if (sData == null)
                    break;
                java.util.regex.Matcher m = p.matcher(sData);
                if (m.find()){
                    String sOption = org.geworkbench.components.alignment.blast.RemoteBlat.getString(sData, '\"');
                    if (sOption.equals("org")){
                        adaptee.jComboBoxGenome.removeAllItems();
                        sData = in.readLine();
                        if (sData == null)
                            break;
                        while (!sData.equals("</SELECT>")){
                            adaptee.jComboBoxGenome.addItem(org.geworkbench.components.alignment.blast.RemoteBlat.getString(sData, '\"'));
                            sData = in.readLine();
                        }
                    }
                    
                    else if (sOption.equals("db")){
                        adaptee.jComboBoxAssembly.removeAllItems();
                        adaptee.jComboBoxAssemblyValue.removeAllItems();
                        sData = in.readLine();
                        if (sData == null)
                            break;
                        while (!sData.equals("</SELECT>")){
                            adaptee.jComboBoxAssemblyValue.addItem(org.geworkbench.components.alignment.blast.RemoteBlat.getString(sData, '\"'));
                            adaptee.jComboBoxAssembly.addItem(org.geworkbench.components.alignment.blast.RemoteBlat.getString(sData, '>', '<'));
                            sData = in.readLine();
                        }
                    }
                    
                    else if (sOption.equals("type")){
                        adaptee.jComboBoxQueryType.removeAllItems();
                        sData = in.readLine();
                        if (sData == null)
                            break;
                        while (!sData.equals("</SELECT>")){
                            adaptee.jComboBoxQueryType.addItem(org.geworkbench.components.alignment.blast.RemoteBlat.getString(sData, '>', '<'));
                            sData = in.readLine();
                        }
                    }
                    
                    else if (sOption.equals("sort")){
                        adaptee.jComboBoxSortOutput.removeAllItems();
                        sData = in.readLine();
                        if (sData == null)
                            break;
                        while (!sData.equals("</SELECT>")){
                            adaptee.jComboBoxSortOutput.addItem(org.geworkbench.components.alignment.blast.RemoteBlat.getString(sData, '>', '<'));
                            sData = in.readLine();
                        }
                    }
                    else if (sOption.equals("output")){
                        adaptee.jComboBoxOutputType.removeAllItems();
                        sData = in.readLine();
                        if (sData == null)
                            break;
                        while (!sData.equals("</SELECT>")){
                            adaptee.jComboBoxOutputType.addItem(org.geworkbench.components.alignment.blast.RemoteBlat.getString(sData, '>', '<'));
                            sData = in.readLine();
                        }
                    }
                    
                    else {
                        
                    }
                }
            }
        }
        catch (UnknownHostException err) {
            adaptee.reportError("Socket:" + err.getMessage(), "Unknown Host Exception Error");
        }
        catch (EOFException err) {
            adaptee.reportError("EOF:" + err.getMessage(), "EOF Exception Error");
        } 
        catch (IOException err) {
            adaptee.reportError(err.getMessage(), "IO Exception Error");
        }
    }
}

class BlastAppComponent_jButtonSetDefault_actionAdapter implements java.awt.event.ActionListener{
    BlastAppComponent adaptee;
    BlastAppComponent_jButtonSetDefault_actionAdapter(BlastAppComponent adaptee){
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e){
        this.adaptee.standAloneBlatDefault();
    }
}
class BlastAppComponent_jButtonBrowse_actionAdapter implements java.awt.event.ActionListener{
    BlastAppComponent adaptee;
    BlastAppComponent_jButtonBrowse_actionAdapter(BlastAppComponent adaptee){
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e){
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);      
        if (fc.showOpenDialog(null) == fc.APPROVE_OPTION){
            File dir = fc.getSelectedFile();
            this.adaptee.jTextFieldSelectDatabase.setText(dir.getAbsolutePath());
        }
    }
}
class BlastAppComponent_jButtonOpen_actionAdapter implements java.awt.event.ActionListener{
    BlastAppComponent adaptee;
    BlastAppComponent_jButtonOpen_actionAdapter(BlastAppComponent adaptee){
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e){
        try{
        BrowserLauncher.openURL("file:///"+ this.adaptee.jTextFieldOutputFile.getText());
        }
        catch(Exception ex){
            System.out.println("Some problems");
            ex.printStackTrace();
        }
    }
}
class BlastAppComponent_jComboBoxQType_actionAdapter implements java.awt.event.ActionListener{
    BlastAppComponent adaptee;
    BlastAppComponent_jComboBoxQType_actionAdapter(BlastAppComponent adaptee){
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e){
        if(this.adaptee.jComboBoxQType.getSelectedItem().equals("prot")){
            this.adaptee.jTextFieldTileSize.setText(this.adaptee.DEFAULT_TILE_SIZE_PROT);
            this.adaptee.jTextFieldMinMatch.setText(this.adaptee.DEFAULT_MIN_MATCH_PROTEIN);
            this.adaptee.jTextFieldMinIdentity.setText(this.adaptee.DEFAULT_MIN_IDENTITY_PROTEIN);
        }
        else{
            this.adaptee.jTextFieldTileSize.setText(this.adaptee.DEFAULT_TILE_SIZE_DNA);
            this.adaptee.jTextFieldMinMatch.setText(this.adaptee.DEFAULT_MIN_MATCH_NUCLEOTIDE);
            this.adaptee.jTextFieldMinIdentity.setText(this.adaptee.DEFAULT_MIN_IDENTITY_NUCLEOTIDE);
        }
    }
}
class RunCommand extends Thread{
    String sCommand;
    RunCommand(String sCommand){
        this.sCommand = sCommand;
        this.start();
    }
    public void run(){
        try{            
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(sCommand);
        } catch (Throwable t){
            t.printStackTrace();
        }
    }
} 
 /*********End Code*******************************/