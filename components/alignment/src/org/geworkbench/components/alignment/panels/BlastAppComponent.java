package org.geworkbench.components.alignment.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
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
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.alignment.blast.BlastAlgorithm;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Asynchronous;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;

/**
 *
 * @author not attributable
 * @version $Id$
 */
@SuppressWarnings("unchecked")
@AcceptTypes( {CSSequenceSet.class})
public class BlastAppComponent implements VisualPlugin {
	private static final String SELECT_A_PROGRAM_PROMPT = "Select a program";

	private static final int DATABASE_NAME_INDEX = 1;

	Log log = LogFactory.getLog(BlastAppComponent.class);
	
	private static AlgorithmMatcher algorithmMatcher = AlgorithmMatcher.getInstance();

	// members from the base class in the previous version
	private DSSequenceSet sequenceDB = null;
	private CSSequenceSet activeSequenceDB = null;
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

    private JTable databaseTable = null;
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
    
    private CSSequenceSet fastaFile;
    private BlastAppComponent blastAppComponent = null;
    private JPanel subSeqPanel;
    private JPanel subSeqPanel2;

    private JTextField jstartPointField = new JTextField();
    private JTextField jendPointField = new JTextField();
    private JProgressBar serviceProgressBar = new JProgressBar();

    private JComboBox jProgramBox1 = new JComboBox();

    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();

    private JButton blastStopButton = new JButton();

    private JToolBar jToolBar2 = new JToolBar();

    private Border border1 = BorderFactory.createEtchedBorder(Color.white,
            new Color(165, 163, 151));
    private Border border2 = new TitledBorder(border1,
                                      "Please specify Program and Database");
    private Border border3 = new TitledBorder(border1, "Database Details");
    private JTextArea textArea = new JTextArea();

    private JLabel jLabel9 = new JLabel();
    private XYLayout xYLayout1 = new XYLayout();
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
    
    private static JLabel DatabaseLabel = new JLabel("Database:");
    
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

        jAdvancedPane = new JPanel();

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

        blastxSettingPanel = new JPanel();

        jstartPointField = new JTextField();
        jendPointField = new JTextField();
        serviceProgressBar = new JProgressBar();

        jProgramBox1 = new JComboBox();

        gridBagLayout3 = new GridBagLayout();
        gridBagLayout2 = new GridBagLayout();

        blastStopButton = new JButton();

        jToolBar2 = new JToolBar();

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

        jPanel3 = new JPanel();
        borderLayout2 = new BorderLayout();

        borderLayout3 = new BorderLayout();

        //above is part of code to get rid of npe.
        //sgePanel.setPv(this);
        subSeqPanel = new JPanel();
        subSeqPanel.setBorder(border2);
        subSeqPanel2 = new JPanel();
        subSeqPanel2.setBorder(border3);

        jLabel9.setText("Program: ");

        jBasicPane.setPreferredSize(new Dimension(364, 250));
        jBasicPane.setLayout(borderLayout2);
        
        jPanel3.setLayout(borderLayout3);

        jAdvancedPane.setLayout(gridBagLayout3);
        databaseTable.setToolTipText("Select a database");
        databaseTable.setVerifyInputWhenFocusTarget(true);
        databaseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jProgramBox.addItem(SELECT_A_PROGRAM_PROMPT);
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
        databaseTable.getSelectionModel().addListSelectionListener(new BlastAppComponent_jDBList_listSelectionListener() );

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
    	String defaulMatrixName = "BLOSUM62"; 
        String[] model = AlgorithmMatcher.translateToGapcosts(defaulMatrixName
                );
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

        jProgramBox1.setAutoscrolls(false);
        jProgramBox1.setMinimumSize(new Dimension(26, 21));
        //jProgramBox1.setPreferredSize(new Dimension(26, 21));

        jAdvancedPane.setMinimumSize(new Dimension(5, 25));
        blastxSettingPanel.setMinimumSize(new Dimension(5, 115));

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
        subSeqPanel2.add(textArea,new XYConstraints(0, 84, 352, 150));
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

    private void jProgramBox_actionPerformed(ActionEvent e) {

        JComboBox cb = (JComboBox) e.getSource();

		// Get the new item
		String selectedProgramName = (String) cb.getSelectedItem();
		if (selectedProgramName == null
				|| selectedProgramName
						.equalsIgnoreCase(SELECT_A_PROGRAM_PROMPT)) {
			jScrollPane1.getViewport().removeAll();
			textArea.setText( "" );			
			return;
		}

		String[][] array = algorithmMatcher
				.translateToArray((String) selectedProgramName);
		if (array == null)
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
		String[] model = AlgorithmMatcher
				.translateToMatrices(selectedProgramName);
		jMatrixBox.setModel(new DefaultComboBoxModel(model));		
		String[] model2 = AlgorithmMatcher
				.translateToWordSize(selectedProgramName);
		jWordsizeBox.setModel(new DefaultComboBoxModel(model2));
		if (selectedProgramName.equalsIgnoreCase("blastn")) {
			humanRepeatFilter.setEnabled(true);
			jGapcostsBox.setEditable(false);
			jGapcostsBox.setVisible(false);
			jGapcostsLabel.setVisible(false);
		} else {
			jMatrixBox.setSelectedIndex(3);//zheng
			humanRepeatFilter.setEnabled(false);
			jGapcostsBox.setEditable(false);
			jGapcostsBox.setVisible(true);
			jGapcostsLabel.setVisible(true);
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

    public CSSequenceSet getFastaFile() {
        return fastaFile;
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
                    if (fastaFile == null && activeSequenceDB != null) {
                        fastaFile = (CSSequenceSet) activeSequenceDB;
                    }

                    blastAlgo = new BlastAlgorithm();
                    blastAlgo.setParameterSetting(parameterSetting);

                    blastAlgo.setBlastAppComponent(this);

                    blastAlgo.setSequenceDB(activeSequenceDB);
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
        ParameterSetting ps = new ParameterSetting(dbName, programName,
				jDisplayInWebBox.isSelected(), expectValue, lowComplexFilterOn,
				humanRepeatFilterOn, maskLowCaseOn, (String) jMatrixBox
						.getSelectedItem(), maskLookupOnlyBox.isSelected());
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
	
	private void refreshMaSetView() {
		getDataSetView();
		fastaFile = activeSequenceDB;
	}
	
	private void getDataSetView() {
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
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
			fastaFile = activeSequenceDB;
		} else {
			DSDataSet dataSet = e.getDataSet();
			if (dataSet instanceof DSSequenceSet) {
				if (sequenceDB != dataSet) {
					this.sequenceDB = (DSSequenceSet) dataSet;

					activatedMarkers = null;
				}
			}
		}
		refreshMaSetView();
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
	        }
	        
	        textArea.setText( dbDetails );
		}
	}

}
