package org.geworkbench.components.alignment.panels;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

import org.geworkbench.bison.datastructure.biocollections.views.
        CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.
        DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.blast.*;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.JAutoList;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import com.borland.jbcl.layout.VerticalFlowLayout;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author XZ
 * @version 1.0
 */

public class BlastViewPanel extends JPanel implements HyperlinkListener {
    JPanel westPanel = new JPanel(new BorderLayout());
    private JAutoList markerList;

    JPanel blastResult = new JPanel();
    JPanel detailedInfo = new JPanel();
    JPanel furtherProcess = new JPanel();
    JButton AddSequenceToProjectButton = new JButton();
    JLabel summaryLabel = new JLabel();
    JButton resetButton = new JButton();
    JEditorPane singleAlignmentArea = new JEditorPane();
    private Vector hits;
    String summaryStr;
    BorderLayout borderLayout1 = new BorderLayout();
    JScrollPane jScrollPane1 = new JScrollPane();
    BorderLayout borderLayout2 = new BorderLayout();
    BlastViewComponent blastViewComponent;
    JButton loadButton = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    private String currentError;
    JButton jButton1 = new JButton();
    JButton addAlignedButton = new JButton();
    GridBagLayout gridBagLayout2 = new GridBagLayout();
    JButton allButton = new JButton();
    JSplitPane jSplitPane1 = new JSplitPane();
    JPanel summaryPanel = new JPanel();
    private JSplitPane mainPanel = new JSplitPane();
    GeneListModel geneListModel = new GeneListModel();
    JSplitPane rightPanel = new JSplitPane();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    private CSSequenceSet sequenceDB;
    private ArrayList blastDataSet = new ArrayList();

    public BlastViewPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBlastViewComponent(BlastViewComponent bc) {
        blastViewComponent = bc;
    }

    @Subscribe public void receive(org.geworkbench.events.ProjectNodeAddedEvent
                                   pnae, Object source) {
        /**@todo Implement this medusa.components.listeners.ProjectNodeAddedListener method*/
        throw new java.lang.UnsupportedOperationException(
                "Method projectNodeAdded() not yet implemented.");
    }

    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                BrowserLauncher.openURL(event.getURL().toString());
                //singleAlignmentArea.setPage(event.getURL());
                //urlField.setText(event.getURL().toExternalForm());
            } catch (IOException ioe) {

            }
        }
    }

    /**
     * Creates a new <code>JPanel</code> with <code>FlowLayout</code> and the
     * specified buffering strategy.
     *
     * @param isDoubleBuffered a boolean, true for double-buffering, which uses
     *                         additional memory space to achieve fast, flicker-free updates
     * @todo Implement this javax.swing.JPanel constructor
     */
    public BlastViewPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    /**
     * @param layout the LayoutManager to use
     * @todo Implement this javax.swing.JPanel constructor
     */
    public BlastViewPanel(LayoutManager layout) {
        super(layout);
    }

    /**
     * Creates a new JPanel with the specified layout manager and buffering
     * strategy.
     *
     * @param layout           the LayoutManager to use
     * @param isDoubleBuffered a boolean, true for double-buffering, which uses
     *                         additional memory space to achieve fast, flicker-free updates
     * @todo Implement this javax.swing.JPanel constructor
     */
    public BlastViewPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }

    private void jbInit() throws Exception {
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        // mainPanel.setPreferredSize(new Dimension(500, 500));
        mainPanel.setOneTouchExpandable(true);
//        rightPanel.setLayout(borderLayout3);
        //rightPanel = new JPanel(gridBagLayout3); //this.setLayout(gridBagLayout3);
        rightPanel = new JSplitPane();
        rightPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
        markerList = new MarkerAutoList();

        blastResult.setLayout(borderLayout1);
        AddSequenceToProjectButton.setMinimumSize(new Dimension(100, 23));

        AddSequenceToProjectButton.setText("Add Selected Sequences to Project ");
        AddSequenceToProjectButton.addActionListener(new
                BlastViewPanel_HMMButton_actionAdapter(this));
        resetButton.setToolTipText("Clear all selections.");
        resetButton.setText("Reset");
        resetButton.addActionListener(new
                                      BlastViewPanel_resetButton_actionAdapter(this));

        blastResult.setBorder(BorderFactory.createLoweredBevelBorder());
        blastResult.setPreferredSize(new Dimension(145, 150));
        blastResult.setMinimumSize(new Dimension(145, 100));
        detailedInfo.setPreferredSize(new Dimension(145, 150));
       detailedInfo.setMinimumSize(new Dimension(145, 100));
        rightPanel.setPreferredSize(new Dimension(155, 400));
        rightPanel.setDividerSize(2);

        rightPanel.setMinimumSize(new Dimension(155, 300));

        summaryPanel = new JPanel();
        summaryLabel = new JLabel();
        summaryPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        summaryPanel.setMinimumSize(new Dimension(100, 40));
        summaryPanel.setPreferredSize(new Dimension(100, 40));
         furtherProcess.setLayout(new BorderLayout());
         furtherProcess.add(summaryPanel, java.awt.BorderLayout.CENTER);
         furtherProcess.add(summaryLabel, java.awt.BorderLayout.SOUTH);
        //furtherProcess.add(summaryLabel);
        furtherProcess.setMinimumSize(new Dimension(50, 40));
        furtherProcess.setPreferredSize(new Dimension(50, 40));
        furtherProcess.setMaximumSize(new Dimension(50, 40));
        //displaySummary("HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");
        detailedInfo.setLayout(borderLayout2);
//        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.
//                                                  HORIZONTAL_SCROLLBAR_ALWAYS);
//        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.
//                                                VERTICAL_SCROLLBAR_ALWAYS);
        loadButton.setToolTipText("Load a blast result file.");
        //rightPanel.setMinimumSize(new Dimension(10, 386));

        loadButton.setText("Load");
        loadButton.addActionListener(new
                                     BlastViewPanel_loadButton_actionAdapter(this));
        singleAlignmentArea.setContentType("text/html");
        //singleAlignmentArea.setText("Single Alignment panel");

        String iniURL = "C:/Blast-1992809694.html";
        File f = new File(iniURL);
        String s1 =
                "http://amdec-bioinfo.cu-genome.org/html/Blast-798903635.html";
        // s1 =  "http://amdec-bioinfo.cu-genome.org/html/hmmOut.txt";
        iniURL = "http://amdec-bioinfo.cu-genome.org/html/index.html";
        //singleAlignmentArea.setPage(s1);

        singleAlignmentArea.setEditable(false);
        singleAlignmentArea.addHyperlinkListener(this);
        jButton1.setText("jButton1");
        addAlignedButton.setMinimumSize(new Dimension(100, 23));
        addAlignedButton.setToolTipText("Add only aligned parts into project.");
        addAlignedButton.setText("Only Add Aligned Parts");
        addAlignedButton.addActionListener(new
                                           BlastViewPanel_addAlignedButton_actionAdapter(this));
        allButton.setToolTipText("Select all hits.");
        allButton.setText("Select All");
        allButton.addActionListener(new BlastViewPanel_allButton_actionAdapter(this));
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setMinimumSize(new Dimension(100, 100));
        jSplitPane1.setPreferredSize(new Dimension(500, 600));
        jSplitPane1.setDividerSize(1);
        this.setLayout(borderLayout3);
//        furtherProcess.add(loadButton, null);
//        furtherProcess.add(resetButton, null);
//        furtherProcess.add(allButton);
//        furtherProcess.add(AddSequenceToProjectButton, null);
//        furtherProcess.add(addAlignedButton);
        summaryPanel.add(loadButton, null);
        summaryPanel.add(resetButton, null);
        summaryPanel.add(allButton);
        summaryPanel.add(AddSequenceToProjectButton, null);
        summaryPanel.add(addAlignedButton);

        jSplitPane1.add(blastResult, JSplitPane.TOP);
        jSplitPane1.add(detailedInfo, JSplitPane.BOTTOM);
        detailedInfo.add(jScrollPane1, BorderLayout.CENTER);
        jSplitPane1.setDividerLocation(0.8);
        jScrollPane1.getViewport().add(singleAlignmentArea, null);
//        rightPanel.add(furtherProcess,
//                       new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
//                                              GridBagConstraints.CENTER,
//                                              GridBagConstraints.BOTH,
//                                              new Insets(0, 0, 3, 7), -195, 18));
//        rightPanel.add(jSplitPane1,
//                       new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
//                                              GridBagConstraints.CENTER,
//                                              GridBagConstraints.BOTH,
//                                              new Insets(0, 0, 0, 0), 398, 223));
        rightPanel.add(jSplitPane1, JSplitPane.TOP);
         //rightPanel.setDividerLocation(0.8);
        rightPanel.add(furtherProcess, JSplitPane.BOTTOM);
        currentError = "No alignment result is loaded, please check again.";
        westPanel.add(markerList, BorderLayout.CENTER);
        //mainPanel.add(markerList);
        mainPanel.add(westPanel);
        mainPanel.add(rightPanel);
        double propLoc = .5D;
        rightPanel.setDividerLocation(propLoc);
        this.add(mainPanel, java.awt.BorderLayout.CENTER);
    }


    /**
     * getResults
     *
     * @param blastAppComponent Object
     * @return Object[]
     */
    public void setResults(Vector hits) {
        this.hits = hits;
        displayResults();
    }

    public void displayResults() {
        blastResult.removeAll();
        blastResult.add(getBlastListPanel());
        revalidate();
        repaint();
    }

    public void setSummary(String s) {

        summaryStr = s;
        displaySummaryLabel(s);
    }

    public void displaySummaryLabel(String s) {

        summaryLabel.setText(s);
         double propLoc = .8D;
        rightPanel.setDividerLocation(propLoc);
        rightPanel.setResizeWeight(0.95);
        jSplitPane1.setDividerLocation(1-propLoc);
        jSplitPane1.setResizeWeight(0.4);
        revalidate();
    }

    /**
     * Display details of each alignment.
     * @param s String
     */
    public void displayResults(String s) {

        blastResult.removeAll();
        blastResult.add(getBlastListPanel());
        singleAlignmentArea.setText(s);

        revalidate();
        singleAlignmentArea.setCaretPosition(0);
    }

    public void resetToWhite(String detailString) {

        blastResult.removeAll();
        if (detailString == null) {
            singleAlignmentArea.setText("Alignment Detail panel");
        } else {
            singleAlignmentArea.setText(detailString);
        }

        revalidate();
        singleAlignmentArea.setCaretPosition(0);
    }

    public void resetToWhite() {
        resetToWhite(null);
    }

    private void showAlignment(BlastObj hit) {
        String text;

        /*    text =
                    "query:\t"
                            + Integer.toString(hit.query_align[0])
                            + "\t"
                            + hit.getQuery()
                            + "\n"
                            + "align:\t\t"
                            + hit.getAlign()
                            + "\n"
                            + "hit:\t"
                            + Integer.toString(hit.subject_align[0])
                            + "\t"
                            + hit.getSubject();
         }*/
        text = hit.getDetailedAlignment();
        //System.out.println("(" + text + ")");
        singleAlignmentArea.setText(text);
        singleAlignmentArea.setCaretPosition(0);

    }

    /**
     * Returns a JScrollpane containing Blast results in table format.
     *
     * @return a JScrollpane containing table of Blast results.
     */
    private JScrollPane getHmmListPanel() {

        /*customized table Model*/
        HmmHitsTableModel myModel = new HmmHitsTableModel();
        /* table based on myModel*/
        JTable table = new JTable(myModel);

        // setting the size of the table and its columns
        table.setPreferredScrollableViewportSize(new Dimension(800, 100));
        table.getColumnModel().getColumn(0).setPreferredWidth(15);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(20);
        table.getColumnModel().getColumn(4).setPreferredWidth(20);
        table.getColumnModel().getColumn(5).setPreferredWidth(20);
        table.getColumnModel().getColumn(6).setPreferredWidth(20);
        /* */
        /*set up Listener for row selection on table*/
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new BlastDetaillistSelectionListener());
        table.setSelectionModel(rowSM);
        return new JScrollPane(table);


    }

    /**
     * Returns a JScrollpane containing Blast results in table format.
     *
     * @return a JScrollpane containing table of Blast results.
     */
    private JScrollPane getBlastListPanel() {

        /*customized table Model*/
        HitsTableModel myModel = new HitsTableModel();
        /* table based on myModel*/
        JTable table = new JTable(myModel);

        // setting the size of the table and its columns
        table.setPreferredScrollableViewportSize(new Dimension(800, 100));
        table.getColumnModel().getColumn(0).setPreferredWidth(35);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(80);
        table.getColumnModel().getColumn(6).setPreferredWidth(30);
        /* */
        /*set up Listener for row selection on table*/
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new BlastDetaillistSelectionListener());
        table.setSelectionModel(rowSM);
        table.changeSelection(0, 0, false, false);

          return  new JScrollPane(table);


    }

    private class BlastDetaillistSelectionListener implements
            ListSelectionListener {
        int selectedRow;
        BlastObj selectedHit;

        public void valueChanged(ListSelectionEvent e) {
            //Ignore extra messages.
            if (e.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {

            } else {
                selectedRow = lsm.getMinSelectionIndex();
                if (hits != null && hits.size() > selectedRow) {
                    selectedHit = (BlastObj) hits.get(selectedRow);
                    showAlignment(selectedHit);
//                    if (foundAtLeastOneSelected()) {
//                        //AddSequenceToProjectButton.setBackground(Color.orange);
//                    } else {
//                        //AddSequenceToProjectButton.setBackground(Color.white);
//                    }

                }

            }
        }
    }


    public boolean foundAtLeastOneSelected() {

        for (int i = 0; i < hits.size(); i++) {
            BlastObj hit = (BlastObj) hits.get(i);
            if (hit.getInclude()) {
                return true;
            }

        }
        return false;

    }

    /**
     * This class extends AbstractTableModel and creates a table view of Blast
     * results.
     */
    private class HmmHitsTableModel extends AbstractTableModel {
        /* array of the column names in order from left to right*/
        final String[] columnNames = {

                                     "Name", "ID", "Description", "e-value",
                                     "align length", "%identity", "Include"};
        HmmObj hit;

        /* returns the number of columns in table*/
        public int getColumnCount() {
            return columnNames.length;
        }

        /* returns the number of rows in table*/
        public int getRowCount() {
            if (hits == null) {
                return 0;
            }

            return (hits.size());
        }

        /* return the header for the column number */
        public String getColumnName(int col) {
            return columnNames[col];
        }

        /* get the Object data to be displayed at (row, col) in table*/
        public Object getValueAt(int row, int col) {
            /*get specific BlastObj based on row number*/
            hit = (HmmObj) hits.get(row);
            /*display data depending on which column is chosen*/
            switch (col) {
            case 0:
                return hit.getName(); //name
            case 1:
                return hit.getID();
            case 2:
                return hit.getDescription(); //description
            case 3:
                return "N/A"; //evalue
            case 4:
                return "N/A"; //evalue
                //length of hit protein
            case 5:

                //percent of sequence aligned to hit sequence
                return "N/A"; //evalue
            case 6:
                return "N/A"; //evalue//whether is chosen for MSA
            }
            return null;
        }

        /*returns the Class type of the column c*/
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*returns if the cell is editable; returns false for all cells in columns except column 6*/
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col >= 6;

        }

        /*detect change in cell at (row, col); set cell to value; update the table */
        public void setValueAt(Object value, int row, int col) {
            hit = (HmmObj) hits.get(row);
            // hit.setInclude( ( (Boolean) value).booleanValue());
            fireTableCellUpdated(row, col);
        }

    }


    /**
     * This class extends AbstractTableModel and creates a table view of Blast
     * results.
     */
    private class HitsTableModel extends AbstractTableModel {
        /* array of the column names in order from left to right*/
        final String[] columnNames = {"db", "Name", "Description", "e-value",
                                     "start point", "align length", "%identity",
                                     "Include"};
        BlastObj hit;

        /* returns the number of columns in table*/
        public int getColumnCount() {
            return columnNames.length;
        }

        /* returns the number of rows in table*/
        public int getRowCount() {

            return (hits.size());
        }

        /* return the header for the column number */
        public String getColumnName(int col) {
            return columnNames[col];
        }

        /* get the Object data to be displayed at (row, col) in table*/
        public Object getValueAt(int row, int col) {
            /*get specific BlastObj based on row number*/
            hit = (BlastObj) hits.get(row);
            /*display data depending on which column is chosen*/
            switch (col) {
            case 0:
                return hit.getDatabaseID(); //database ID
            case 1:
                return hit.getName(); //accesion number
            case 2:
                return hit.getDescription(); //description
            case 3:
                return hit.getEvalue(); //evalue
            case 4:
                return new Integer(hit.getStartPoint());
                //length of hit protein
            case 5:
                return new Integer(hit.getAlignmentLength());
            case 6:

                //percent of sequence aligned to hit sequence
                return new Integer(hit.getPercentAligned());
            case 7:
                return new Boolean(hit.getInclude()); //whether is chosen for MSA
            }
            return null;
        }

        /*returns the Class type of the column c*/
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*returns if the cell is editable; returns false for all cells in columns except column 6*/
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 6) {
                return false;
            } else {
                return true;
            }
        }

        /*detect change in cell at (row, col); set cell to value; update the table */
        public void setValueAt(Object value, int row, int col) {
            hit = (BlastObj) hits.get(row);
            hit.setInclude(((Boolean) value).booleanValue());
            fireTableCellUpdated(row, col);
        }

    }


    boolean verify() {
        if (hits == null) {
            return false;
        }
        return true;

    }

    void reportError(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, "Error",
                                      JOptionPane.INFORMATION_MESSAGE);
    }

    void HMMButton_actionPerformed(ActionEvent e) {
        CSSequenceSet db = new CSSequenceSet();
        if (!verify()) {
            reportError(currentError);
            return;
        }
        submitNewSequences(e, true);

    }

    void submitNewSequences(ActionEvent e, boolean isFullLength) {

        CSSequenceSet db = new CSSequenceSet();

        /**todo
         * Old SoapClient need fastaFile name, so just create a temp fasta file here.
         * Consider change SoapClient to Dataset directly for blast.
         */
        try {

            if (!foundAtLeastOneSelected()) {
                reportError("No hit is selected. Please choose at least one.");
                return;
            }
            String tempString = "temp-" + RandomNumberGenerator.getID() +
                                ".fasta";
            String tempFolder = System.getProperties().getProperty(
                    "temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";
            }
            File tempFile = new File(tempFolder + tempString);
            PrintWriter out = new PrintWriter(new FileOutputStream(tempFile));
            for (int i = 0; i < hits.size(); i++) {
                BlastObj hit = (BlastObj) hits.get(i);
                if (hit.getInclude()) {

                    CSSequence seq = null;
                    if (isFullLength) {

                        seq = hit.getWholeSeq();
                    } else {
                        seq = hit.getAlignedSeq();

                    }

                    out.println(seq.getLabel());
                    out.println(seq.getSequence());

                }

            }
            out.flush();
            out.close();
            db.setLabel("temp_Fasta_File");
            db.readFASTAFile(tempFile);

            org.geworkbench.events.ProjectNodeAddedEvent event = new org.
                    geworkbench.events.ProjectNodeAddedEvent("message", db, null);
            blastViewComponent.publishProjectNodeAddedEvent(event);
        } catch (BlastDataOutOfBoundException be) {
            String errorMessage = be.getMessage();
            JOptionPane.showMessageDialog(null,
                                          errorMessage,
                                          "Error",
                                          JOptionPane.WARNING_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    void resetButton_actionPerformed(ActionEvent e) {
        if (!verify()) {
            reportError(currentError);
            return;
        }

        for (int i = 0; i < hits.size(); i++) {
            BlastObj hit = (BlastObj) hits.get(i);
            hit.setInclude(false);
        }
        AddSequenceToProjectButton.setBackground(Color.white);

        displayResults("<h4>No alignment hit is selected.");

    }


    /**
     * setResults
     *
     * @param string String
     */
    public void setResults(String string) {

        HmmResultParser hmmParser = new HmmResultParser(string);
        hmmParser.parseResults();
        this.hits = hmmParser.getHits();
        blastResult.removeAll();
        blastResult.add(getHmmListPanel());
        revalidate();

    }

    public void loadButton_actionPerformed(ActionEvent actionEvent) {

        JFileChooser chooser = new JFileChooser(PropertiesMonitor.
                                                getPropertiesMonitor().
                                                getDefPath());
        org.geworkbench.components.parsers.ExampleFileFilter filter = new org.geworkbench.components.parsers.ExampleFileFilter();
        filter.setDescription("Alignment file (*.html)");
        filter.addExtension("html");
        chooser.addChoosableFileFilter(filter);

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor().
                setDefPath(chooser.getCurrentDirectory().getAbsolutePath());
        File patternfile = chooser.getSelectedFile();
        try {

           //BlastParser bp = new BlastParser(patternfile.getAbsolutePath());
            NCBIBlastParser bp = new NCBIBlastParser(patternfile.getAbsolutePath());


            //bp.setTotalSequenceNum(1);
            if (bp.parseResults()) {

                hits = bp.getHits();

                setResults(hits);
            } else {
                NCBIBlastParser nbp = new NCBIBlastParser(patternfile.
                        getAbsolutePath());
                nbp.setTotalSequenceNum(1);
                if (nbp.parseResults()) {

                    hits = nbp.getHits();

                    setResults(hits);
                } else {

                    JOptionPane.showMessageDialog(null,
                                                  "The file is not in a supported format.",
                                                  "Format Error",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (NullPointerException e1) {

            e1.printStackTrace();

        }

    }

    public void addAlignedButton_actionPerformed(ActionEvent e) {
        if (!verify()) {
            reportError(currentError);
            return;
        }
        submitNewSequences(e, false);
    }

    public void allButton_actionPerformed(ActionEvent e) {
        if (!verify()) {
            reportError(currentError);
            return;
        }
        for (int i = 0; i < hits.size(); i++) {
            BlastObj hit = (BlastObj) hits.get(i);
            hit.setInclude(true);
            AddSequenceToProjectButton.setBackground(Color.orange);

        }
        displayResults("<h4>All are selected.");

    }

    /**
     * The marker JAutoList type.
     */
    private class MarkerAutoList extends JAutoList {

        public MarkerAutoList() {
            super(geneListModel);
        }

       public boolean setHighlightedIndex(int theIndex) {
        super.setHighlightedIndex(theIndex);
        elementClicked(theIndex, null);
        return true;
    }

        @Override protected void elementClicked(int index, MouseEvent e) {
//            itemClicked(PlotType.MARKER, index);
//            markerModel.refresh();
            if (blastDataSet != null && blastDataSet.size() > index) {
                if (blastDataSet.get(index) != null) {
                    setResults((Vector) blastDataSet.get(index));
                    displaySummaryLabel(" " + summaryStr + " Sequence " +
                                        ((CSSequence) sequenceDB.get(index)).
                                        getLabel() + " has " +
                                        ((Vector) blastDataSet.get(index)).size() +
                                        " hits.");

                } else {

                      setResults(new Vector());
                    resetToWhite("No hits found");
                    displaySummaryLabel(" " + summaryStr + " Sequence " +
                                        ((CSSequence) sequenceDB.get(index)).
                                        getLabel() + " has 0 hit.");

                }
            } else if (blastDataSet != null && (Vector) blastDataSet.get(0) != null) {
                setResults((Vector) blastDataSet.get(0));
            }
        }

        @Override protected void elementRightClicked(int index, MouseEvent e) {
            //     itemRightClicked(PlotType.MARKER, e, index);
        }
    }


    /**
     * ListModel for the marker list.
     */
    private class GeneListModel extends AbstractListModel {

        public int getSize() {
            if (sequenceDB == null) {
                return 0;
            }
            return sequenceDB.size();
        }

        public Object getElementAt(int index) {
            if (sequenceDB == null) {
                return null;
            }
            return ((CSSequence) sequenceDB.get(index)).getLabel();
        }

        public DSGeneMarker getMarker(int index) {
            return dataSetView.getMicroarraySet().getMarkers().get(index);
        }

        public Object getItem(int index) {
            return sequenceDB.get(index);
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {
            if (sequenceDB == null) {
                fireContentsChanged(this, 0, 0);
            } else {
                fireContentsChanged(this, 0, sequenceDB.size());
            }
        }

        public void refreshItem(int index) {
            fireContentsChanged(this, index, index);
        }


    }


    /**
     * The dataset that holds the microarrayset and panels.
     */
    private DSMicroarraySetView<DSGeneMarker,
            DSMicroarray> dataSetView = new CSMicroarraySetView<DSGeneMarker,
                                        DSMicroarray>();
    BorderLayout borderLayout3 = new BorderLayout();
    VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
    //private MarkerListModel markerModel = new MarkerListModel();
    // XYLayout xYLayout1 = new XYLayout();
    /**
     * setSequenceDB
     *
     * @param sequenceDB DSDataSet
     */
    public void setSequenceDB(CSSequenceSet sequenceDB) {
        this.sequenceDB = sequenceDB;
        geneListModel.refresh();
    }

    /**
     * setBlastDataSet
     *
     * @param arrayList ArrayList
     */
    public void setBlastDataSet(ArrayList arrayList) {
        blastDataSet = arrayList;
        if(markerList!=null && blastDataSet!=null && sequenceDB != null){
            int index = sequenceDB.size() - 1;
            markerList.setHighlightedIndex(sequenceDB.size() - 1);
            //markerList.elementClicked(index, null);
            //markerList.setH
        }

    }
}


class BlastViewPanel_allButton_actionAdapter implements ActionListener {
    private BlastViewPanel adaptee;

    BlastViewPanel_allButton_actionAdapter(BlastViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.allButton_actionPerformed(e);
    }
}


class BlastViewPanel_loadButton_actionAdapter implements ActionListener {
    private BlastViewPanel adaptee;

    BlastViewPanel_loadButton_actionAdapter(BlastViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        adaptee.loadButton_actionPerformed(actionEvent);
    }
}


class BlastViewPanel_HMMButton_actionAdapter implements java.awt.event.
        ActionListener {
    BlastViewPanel adaptee;

    BlastViewPanel_HMMButton_actionAdapter(BlastViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.HMMButton_actionPerformed(e);
    }
}


class BlastViewPanel_addAlignedButton_actionAdapter implements ActionListener {
    private BlastViewPanel adaptee;

    BlastViewPanel_addAlignedButton_actionAdapter(BlastViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {

        adaptee.addAlignedButton_actionPerformed(e);
    }
}


class BlastViewPanel_resetButton_actionAdapter implements java.awt.event.
        ActionListener {
    BlastViewPanel adaptee;

    BlastViewPanel_resetButton_actionAdapter(BlastViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.resetButton_actionPerformed(e);
    }
}

