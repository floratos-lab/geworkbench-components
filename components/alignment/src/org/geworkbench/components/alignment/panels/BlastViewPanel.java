package org.geworkbench.components.alignment.panels;

import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.blast.BlastObj;
import org.geworkbench.components.alignment.blast.BlastParser;
import org.geworkbench.components.alignment.blast.HmmObj;
import org.geworkbench.components.alignment.blast.HmmResultParser;
import org.geworkbench.util.PropertiesMonitor;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import org.geworkbench.components.alignment.blast.BlastDataOutOfBoundException;

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
    JPanel blastResult = new JPanel();
    JPanel detailedInfo = new JPanel();
    JPanel furtherProcess = new JPanel();
    JButton HMMButton = new JButton();
    JButton resetButton = new JButton();
    JEditorPane singleAlignmentArea = new JEditorPane();
    private Vector hits;
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

    GridBagLayout gridBagLayout3 = new GridBagLayout();

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

    @Subscribe public void receive(org.geworkbench.events.ProjectNodeAddedEvent pnae, Object source) {
        /**@todo Implement this medusa.components.listeners.ProjectNodeAddedListener method*/
        throw new java.lang.UnsupportedOperationException("Method projectNodeAdded() not yet implemented.");
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
        this.setLayout(gridBagLayout3);
        blastResult.setLayout(borderLayout1);
        HMMButton.setMinimumSize(new Dimension(100, 23));
        HMMButton.setText("Add Selected Sequences to Project ");
        HMMButton.addActionListener(new BlastViewPanel_HMMButton_actionAdapter(this));
        resetButton.setText("Reset");
        resetButton.addActionListener(new BlastViewPanel_resetButton_actionAdapter(this));

        blastResult.setBorder(BorderFactory.createLoweredBevelBorder());
        blastResult.setPreferredSize(new Dimension(145, 200));
        furtherProcess.setBorder(BorderFactory.createLoweredBevelBorder());
        furtherProcess.setMinimumSize(new Dimension(10, 37));
        detailedInfo.setLayout(borderLayout2);
        jScrollPane1.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        this.setMinimumSize(new Dimension(10, 386));
        loadButton.setText("Load");
        loadButton.addActionListener(new BlastViewPanel_loadButton_actionAdapter(this));
        singleAlignmentArea.setContentType("text/html");
        //singleAlignmentArea.setText("Single Alignment panel");

        String iniURL = "C:/Blast-1992809694.html";
        File f = new File(iniURL);
        String s1 = "http://amdec-bioinfo.cu-genome.org/html/Blast-798903635.html";
        // s1 =  "http://amdec-bioinfo.cu-genome.org/html/hmmOut.txt";
        iniURL = "http://amdec-bioinfo.cu-genome.org/html/index.html";
        //singleAlignmentArea.setPage(s1);

        singleAlignmentArea.setEditable(false);
        singleAlignmentArea.addHyperlinkListener(this);
        jButton1.setText("jButton1");
        addAlignedButton.setMinimumSize(new Dimension(100, 23));
        addAlignedButton.setText("Only Added Aligned Parts");
        addAlignedButton.addActionListener(new BlastViewPanel_addAlignedButton_actionAdapter(this));
        allButton.setText("Select All");
        allButton.addActionListener(new BlastViewPanel_allButton_actionAdapter(this));
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        furtherProcess.add(loadButton, null);
        furtherProcess.add(resetButton, null);
        furtherProcess.add(allButton);
        furtherProcess.add(HMMButton, null);
        furtherProcess.add(addAlignedButton);
        jSplitPane1.add(blastResult, JSplitPane.TOP);
        jSplitPane1.add(detailedInfo, JSplitPane.BOTTOM);
        detailedInfo.add(jScrollPane1, BorderLayout.CENTER);
        jScrollPane1.getViewport().add(singleAlignmentArea, null);
        this.add(furtherProcess, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 3, 7), -195, 18));
        this.add(jSplitPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 398, 223));
        currentError = "No alignment result is loaded, please check again.";
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
    }

    public void displayResults(String s) {

        blastResult.removeAll();
        blastResult.add(getBlastListPanel());
        singleAlignmentArea.setText(s);

        revalidate();

    }

    public void resetToWhite() {

        blastResult.removeAll();

        singleAlignmentArea.setText("Alignment Detail panel");
        revalidate();

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
        rowSM.addListSelectionListener(new listSelectionListener());
        table.setSelectionModel(rowSM);
        JScrollPane hitsPane = new JScrollPane(table);

        return hitsPane;
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
        rowSM.addListSelectionListener(new listSelectionListener());
        table.setSelectionModel(rowSM);
        JScrollPane hitsPane = new JScrollPane(table);

        return hitsPane;
    }

    private class listSelectionListener implements ListSelectionListener {
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
                selectedHit = (BlastObj) hits.get(selectedRow);

                showAlignment(selectedHit);
                if (foundAtLeastOneSelected()) {
                    HMMButton.setBackground(Color.orange);
                } else {
                    HMMButton.setBackground(Color.white);
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

            "Name", "ID", "Description", "e-value", "align length", "%identity", "Include"};
        HmmObj hit;

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
            if (col < 6) {
                return false;
            } else {
                return true;
            }
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
        final String[] columnNames = {"db", "Name", "Description", "e-value", "start point", "align length", "%identity", "Include"};
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
        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.INFORMATION_MESSAGE);
    }

    void HMMButton_actionPerformed(ActionEvent e) {
        SequenceDB db = new SequenceDB();
        if (!verify()) {
            reportError(currentError);
            return;
        }
        submitNewSequences(e, true);

    }

    void submitNewSequences(ActionEvent e, boolean isFullLength) {

        SequenceDB db = new SequenceDB();

        /**todo
         * Old SoapClient need fastaFile name, so just create a temp fasta file here.
         * Consider change SoapClient to Dataset directly for blast.
         */
        try {
            String tempString = "temp" + RandomNumberGenerator.getID() + ".fasta";
            String tempFolder = System.getProperties().getProperty("temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";
            }
            File tempFile = new File(tempFolder + tempString);
            PrintWriter out = new PrintWriter(new FileOutputStream(tempFile));
            for (int i = 0; i < hits.size(); i++) {
                BlastObj hit = (BlastObj) hits.get(i);
                if (hit.getInclude()) {

                    CSSequence seq = hit.getWholeSeq();
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
            db.setFASTAFile(tempFile);

            // AncillaryDataSet blastResult = new CSAlignmentResultSet (htmlFile, soapClient.getInputFileName());

            org.geworkbench.events.ProjectNodeAddedEvent event = new org.geworkbench.events.ProjectNodeAddedEvent("message", db, null);
            blastViewComponent.publishProjectNodeAddedEvent(event);
        }catch (BlastDataOutOfBoundException be){
            //be.printStackTrace();
            String errorMessage = be.getMessage();
            JOptionPane.showMessageDialog(null,
                                errorMessage,
                                "Error",
                                JOptionPane.WARNING_MESSAGE);


        }
        catch (Exception ex) {
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
        HMMButton.setBackground(Color.white);

        displayResults("<h4>No alignment hit is selected.");

    }


    /**
     * setResults
     *
     * @param string String
     */
    public void setResults(String string) {
        // try {
        //  singleAlignmentArea.setPage(string);

        HmmResultParser hmmParser = new HmmResultParser(string);
        hmmParser.parseResults();
        this.hits = hmmParser.getHits();
        blastResult.removeAll();
        blastResult.add(getHmmListPanel());
        revalidate();

        //  }catch (IOException e){e.printStackTrace();
        //System.out.println(string + " cannot be presented at visual area");
    }

    public void loadButton_actionPerformed(ActionEvent actionEvent) {

        JFileChooser chooser = new JFileChooser(PropertiesMonitor.getPropertiesMonitor().getDefPath());
        org.geworkbench.engine.parsers.ExampleFileFilter filter = new org.geworkbench.engine.parsers.ExampleFileFilter();
        filter.setDescription("Alignment file (*.html)");
        filter.addExtension("html");
        chooser.addChoosableFileFilter(filter);

        int returnVal = chooser.showOpenDialog(this);
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        //      updateFileProperty(chooser.getSelectedFile().getAbsolutePath());
        org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor().setDefPath(chooser.getCurrentDirectory().getAbsolutePath());
        File patternfile = chooser.getSelectedFile();
        try {

            BlastParser bp = new BlastParser(patternfile.getAbsolutePath());

            if (bp.parseResults()) {

                hits = bp.getHits();

                setResults(hits);
            } else {
                JOptionPane.showMessageDialog(null, "The file is not in a supported format.", "Format Error", JOptionPane.ERROR_MESSAGE);

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
            HMMButton.setBackground(Color.orange);

        }
        displayResults("<h4>All are selected.");

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


class BlastViewPanel_HMMButton_actionAdapter implements java.awt.event.ActionListener {
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


class BlastViewPanel_resetButton_actionAdapter implements java.awt.event.ActionListener {
    BlastViewPanel adaptee;

    BlastViewPanel_resetButton_actionAdapter(BlastViewPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.resetButton_actionPerformed(e);
    }
}
