package org.geworkbench.components.alignment.blast;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * BlastInfo is a class that contains the "Blast" panel which contains
 * functionality for submission of a protein sequence to NCBI's Blast server.
 * The sequence used is either the primary protein sequence, the subsequence
 * created from the primary protein sequence, or some new sequence pasted into
 * the text area provided.  The user can indicate which to Blast by selecting
 * the appropriate radiobutton.
 * <br><br.
 * Submission of the query to Blast is a separate user-initiated step from the
 * display of Blast results.  This is modeled in a similar fashion to the NCBI
 * Blast search interface.  After a query is submitted to Blast, the user will
 * be notified via a text area message when Blast is done.  They can then click
 * the button for displaying their results which is in the format of a table.
 * Currently the user must click on the lower panel to refresh the panel and
 * visualize the table.  They can then select the hits they are interested in
 * creating a multiple sequence alignment (MSA).
 */
public class BlastInfo extends JPanel {

    /**
     * Reference to parent JTabbedPane.
     */
    private JTabbedPane tabs;
    /**
     * The "primary sequence" object, or current protein sequence in memory.
     */
    private ProtSeq primarySeq;
    /**
     * Button for selecting current primary sequence as Blast query.
     */
    private JRadioButton currentseq;
    /**
     * Button for selecting current primary sequence's subsequence as Blast
     * query.
     */
    private JRadioButton subseq;
    /**
     * Button for selecting a new user-inputted sequence as BLast query.
     */
    private JRadioButton newseq;
    /**
     * JPanel for displaying Blast query options.
     */
    private JPanel optionsPanel;
    /**
     * JPanel for displaying Blast results.
     */
    private JPanel resultsPanel;
    /**
     * Text area in Blast options panel for display of query sequence and
     * messages.
     */
    protected JTextArea textArea;
    /**
     * Text area for showing alignment of a hit to query sequence
     */
    private JTextArea align;
    /**
     * Default file name to write Blast results to.
     */
    private String BLASTFILE = "BLAST_results.txt";
    /**
     * Vector of type String representing each sequence hit by their Accession number.
     */
    private Vector hits;
    /**
     * The RemoteBlast object used for Blast submission and results retrieval.
     */
    private RemoteBlast blast;

    /**
     * The JSplitPane used to enable user to resize the two main display
     * components relative to each other.
     */
    private JSplitPane splitPane;

    /**
     * Creates a new BlastInfo with tabs set to specified JTabbedPane.
     * Also sets formatting and layout of this component as well as create
     * subcomponents contained within.
     *
     * @param tabs - the parent JTabbedPane.
     */
    public BlastInfo(JTabbedPane tabs) {

        this.tabs = tabs;
        primarySeq = null;

        /* format main BlastInfo JPanel */
        this.setPreferredSize(new Dimension(900, 600));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        /* format and create components for optionsPanel */
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BorderLayout());
        optionsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Blast Options"));

        /* create and group radiobuttons and Blast controls*/
        ButtonGroup options = new ButtonGroup();
        currentseq = new JRadioButton("Current Protein Sequence");
        currentseq.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText(primarySeq.getSequence());
                textArea.setEditable(false);
            }
        });
        subseq = new JRadioButton("Subsequence");
        subseq.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText(primarySeq.getSubseq());
                textArea.setEditable(false);
            }
        });
        newseq = new JRadioButton("New Sequence");
        newseq.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
                textArea.setEditable(true);
            }
        });
        currentseq.setSelected(true);
        JLabel label = new JLabel("  Blast with:");
        options.add(currentseq);
        options.add(subseq);
        options.add(newseq);

        JButton blast = new JButton("BLAST!");
        blast.addActionListener(new Blastlistener());
        JButton displayBlast = new JButton("Display BLAST results (might have to click panel to refresh)");
        displayBlast.addActionListener(new DisplayBlastlistener());
        Box controls = new Box(BoxLayout.X_AXIS);
        controls.add(Box.createGlue());
        controls.add(blast);
        controls.add(displayBlast);
        controls.add(Box.createGlue());

        Box box = new Box(BoxLayout.Y_AXIS);
        box.setPreferredSize(new Dimension(200, 200));
        box.setAlignmentY(TOP_ALIGNMENT);
        box.add(label);
        box.add(currentseq);
        box.add(subseq);
        box.add(newseq);

        /* set format and scrolling capabilities of textArea */
        textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setLineWrap(true); //wrap text around
        textArea.setEditable(false); //text is non-editable
        textArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(700, 200));

        optionsPanel.add(box, BorderLayout.WEST);
        optionsPanel.add(areaScrollPane, BorderLayout.CENTER);
        optionsPanel.add(controls, BorderLayout.SOUTH);
        optionsPanel.setPreferredSize(new Dimension(900, 400));

        /* JPanel for holding Blast results table */
        resultsPanel = new JPanel();
        resultsPanel.setPreferredSize(new Dimension(900, 200));
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        /* format and add components to splitPane */
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, optionsPanel, resultsPanel);
        splitPane.resetToPreferredSizes();
        splitPane.setAlignmentX(CENTER_ALIGNMENT);
        splitPane.setPreferredSize(new Dimension(900, 550));

        /* add panes to main BlastInfo pane */
        add(optionsPanel);
        //add(controls);
        add(splitPane);

    }

    /**
     * Sets the primary sequence to the specified ProtSeq object.
     *
     * @param primarySeq - the new primay sequence to work with.
     */
    public void setPrimarySeq(ProtSeq primarySeq) {
        this.primarySeq = primarySeq;
    }

    /**
     * Updates the text area with the sequence of the current primary sequence.
     */
    public void update() {
        textArea.setText(primarySeq.getSequence());
    }

    /**
     * Creates and returns a Vector containing user-selected Blast hits for
     * multiple sequence alignment purposes.
     *
     * @return a Vector of type String containing Accession #s of
     *         selected Blast hits.
     */
    public Vector getHitsForAlign() {
        int num = hits.size();
        Vector included = new Vector();

        for (int i = 0; i < num; i++) {
            BlastObj hit = (BlastObj) hits.get(i);
            if (hit.getInclude()) {
                String name = hit.getName();
                //int dotPlace = name.indexOf(".");
                //if (dotPlace > 1) {
                //	name = name.substring(0, dotPlace - 1);
                //}
                included.add(name);
            }
        }
        return included;
    }

    /**
     * Displays Blast results as a table of hits.  Also passes the Vector of
     * Blast hits to the ClustalW panel for multiple sequence alignment
     * purposes.
     *
     * @param		hits - the Vector of Blast results hits to display.
     */
    public void display(Vector hits) {
        this.hits = hits;
        //resultsPanel.add(graphicalAlign());
        resultsPanel.removeAll();
        resultsPanel.add(hitsList());
        JButton ready = new JButton("Select checked for ClustalW analysis");
        ready.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //    ((ClustalW) tabs.getComponentAt(4)).setBlastHits(
                //          getHitsForAlign());
                //tabs.setSelectedComponent(tabs.getComponentAt(4));
            }
        });
        resultsPanel.add(ready);

        /*set up textArea for displaying hit/query alignment*/
        align = new JTextArea();
        align.setFont(new Font("Monospaced", Font.PLAIN, 12));
        align.setLineWrap(false); //wrap text around
        align.setEditable(false); //text is non-editable
        align.setMargin(new Insets(10, 10, 10, 10));
        //align.setRows(5);
        JScrollPane alignPane = new JScrollPane(align);
        alignPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        alignPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        alignPane.setPreferredSize(new Dimension(800, 100));

        resultsPanel.add(alignPane);
        resultsPanel.repaint();
        this.repaint();
    }

    private void showAlignment(BlastObj hit) {
        String text;

        text = "query:\t" + Integer.toString(hit.query_align[0]) + "\t" + hit.getQuery() + "\n" + "align:\t\t" + hit.getAlign() + "\n" + "hit:\t" + Integer.toString(hit.subject_align[0]) + "\t" + hit.getSubject();
        align.setText(text);

    }

    /**
     * Returns a JScrollpane containing Blast results in table format.
     *
     * @return a JScrollpane containing table of Blast results.
     */
    private JScrollPane hitsList() {

        /*customized table Model*/
        HitsTableModel myModel = new HitsTableModel();
        /* table based on myModel*/
        JTable table = new JTable(myModel);

        /* setting the size of the table and its columns*/
        table.setPreferredScrollableViewportSize(new Dimension(800, 100));
        table.getColumnModel().getColumn(0).setPreferredWidth(15);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(20);
        table.getColumnModel().getColumn(4).setPreferredWidth(20);
        table.getColumnModel().getColumn(5).setPreferredWidth(20);
        table.getColumnModel().getColumn(5).setPreferredWidth(20);

        /*set up Listener for row selection on table*/
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new listSelectionListener());
        table.setSelectionModel(rowSM);
        JScrollPane hitsPane = new JScrollPane(table);
        return hitsPane;
    }

    /**
     * class that implements a ListSelectionListener; listens to the row selection in table.
     * displays query alignment for hit in that row.
     */
    private class listSelectionListener implements ListSelectionListener {
        int selectedRow;
        BlastObj selectedHit;

        public void valueChanged(ListSelectionEvent e) {
            //Ignore extra messages.
            if (e.getValueIsAdjusting())
                return;
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
            } else {
                selectedRow = lsm.getMinSelectionIndex();
                selectedHit = (BlastObj) hits.get(selectedRow);
                showAlignment(selectedHit);
            }
        }
    }

    /**
     * This class extends AbstractTableModel and creates a table view of Blast
     * results.
     */
    private class HitsTableModel extends AbstractTableModel {
        /* array of the column names in order from left to right*/
        final String[] columnNames = {"db", "Name", "Description", "e-value", "align length", "%identity", "Include"};
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
                    return new Integer(hit.getLength());
                    //length of hit protein
                case 5:
                    //percent of sequence aligned to hit sequence
                    return new Integer(hit.getPercentAligned());
                case 6:
                    return new Boolean(hit.getInclude());
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

    /**
     * This is a class that implements ActionListener and listens for a user-
     * initated button click.  It then attempts to create a new RemoteBlast
     * using the user-designated sequence source as a query.
     */
    private class Blastlistener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String query = textArea.getText();
            if (query.length() < 1 || query == null) {
                textArea.setText("No protein sequence!");
            } else {
                if (currentseq.isSelected()) {
                    blast = new RemoteBlast(primarySeq.getSequence(), textArea);
                } else if (subseq.isSelected()) {
                    blast = new RemoteBlast(primarySeq.getSubseq(), textArea);
                } else if (newseq.isSelected()) {
                    blast = new RemoteBlast(textArea.getText(), textArea);
                }
                String BLAST_rid = blast.submitBlast();
                blast.getBlast(BLAST_rid);
            }
        }
    }

    /**
     * This is a class that implements ActionListener and listens for a user-
     * initated button click.  It then checks if Blast is done and informs the
     * user if not so.  Otherwise it creates a new BlastParser to parse out
     * Blast results and generates a table formatted view of the Blast hits for
     * display.
     */
    private class DisplayBlastlistener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (blast.getBlastDone() == false) {
                textArea.append("\nBLAST is not done yet! Please wait...\n");
            } else {
                BlastParser blastParse = new BlastParser(primarySeq, BLASTFILE);
                blastParse.parseResults();
                hits = blastParse.getHits();
                display(hits);
            }
        }
    }
}
