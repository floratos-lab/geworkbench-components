package org.geworkbench.components.alignment.synteny;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * SyntenyInfo is a class that contains the "Synteny" panel which contains
 * functionality for submission of a request to remote server server.
 * The request used is either two nucleotide sequences of two URL's.
 * If one query is submitted it will be used as both used as X and Y query.
 * <br><br.
 *
 */

public class SyntenyInfo extends JPanel {

  /* Reference to parent JTabbedPane. */
  private JTabbedPane tabs;
  SyntenyQueryObj queryX;
  SyntenyQueryObj queryY;
  /* The "primary sequence" object, or current protein sequence in memory. */
  private String seqNameX;
  /* Button for selecting primary sequence as query. */
  private JRadioButton currentseq;
  /* Button for selecting current primary sequence's subsequence as Blast query. */
  private JRadioButton subseq;
  /* Button for selecting a new user-inputted sequence as BLast query. */
  private JRadioButton newseq;
  /* JPanel for displaying query options. */
  private JPanel optionsPanel;
  /* JPanel for displaying results. */
  private JPanel resultsPanel;
  /* Text area in options panel for display of query sequence and messages. */
  protected JTextArea textArea;
  /* Text area for showing alignment of a hit to query sequence */
  private JTextArea align;
  /* Default file name to write Blast results to. */
  private String BLASTFILE = "BLAST_results.txt";
  /* Vector of type String representing each sequence hit by their Accession number. */
  private Vector hits;
  /* The RemoteBlast object used for Blast submission and results retrieval. */
  private RemoteDots rdots;
  /* The JSplitPane used to enable user to resize the two main display components relative to each other. */
  private JSplitPane splitPane;

  /**
   * Creates a new BlastInfo with tabs set to specified JTabbedPane.
   * Also sets formatting and layout of this component as well as create
   * subcomponents contained within.
   *
   * @param 		tabs - the parent JTabbedPane.
   */
  public SyntenyInfo(JTabbedPane tabs) {

    this.tabs = tabs;
    queryX = null;
    queryY = null;

    /* format main Info JPanel */
    this.setPreferredSize(new Dimension(900, 600));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    /* format and create components for optionsPanel */
    optionsPanel = new JPanel();
    optionsPanel.setLayout(new BorderLayout());
    optionsPanel.setBorder(
        BorderFactory.createTitledBorder(
        BorderFactory.createEtchedBorder(),
        "Synteny Options"));

    /* create and group radiobuttons and Blast controls*/
    ButtonGroup options = new ButtonGroup();
    currentseq = new JRadioButton("Current Query");
    currentseq.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        textArea.setText("First query " + queryX + "Second Query:  " + queryY);
        textArea.setEditable(false);
      }
    });
    subseq = new JRadioButton("Query options");
    subseq.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        textArea.setText("First query " + queryX + "Second Query:  " + queryY);
        textArea.setEditable(false);
      }
    });
    newseq = new JRadioButton("New query");
    newseq.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        textArea.setText("");
        textArea.setEditable(true);
      }
    });
    currentseq.setSelected(true);
    JLabel label = new JLabel(" Use program:");
    options.add(currentseq);
    options.add(subseq);
    options.add(newseq);

    JButton run = new JButton("RUNNNNNNNNNNNNNNNNNNNN");
    run.addActionListener(new SyntenyListener());
    JButton displayBlast =
        new JButton(
        "Display Synteny results (might have to click panel to refresh)");
    displayBlast.addActionListener(new SyntenyListener());
    Box controls = new Box(BoxLayout.X_AXIS);
    controls.add(Box.createGlue());
    controls.add(run);
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
    areaScrollPane.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    areaScrollPane.setHorizontalScrollBarPolicy(
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
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
    splitPane =
        new JSplitPane(
        JSplitPane.VERTICAL_SPLIT,
        optionsPanel,
        resultsPanel);
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
   * @param 		primarySeq - the new primay sequence to work with.
   */
  /*        public void setPrimarySeq(ProtSeq primarySeq) {
                  this.primarySeq = primarySeq;
          }
   */
  /**
   * Updates the text area with the sequence of the current primary sequence.
   */
  /*        public void update() {
                  textArea.setText(primarySeq.getSequence());
          }
   */
  /**
   * Creates and returns a Vector containing user-selected Blast hits for
   * multiple sequence alignment purposes.
   *
   * @return 		a Vector of type String containing Accession #s of
   * 				selected Blast hits.
   */
  /*      public Vector getHitsForAlign() {
                int num = hits.size();
                Vector included = new Vector();

                for (int i = 0; i < num; i++) {
                        BlastObj hit = (BlastObj) hits.get(i);
                        if (hit.getInclude().booleanValue()) {
                                String name = hit.get_name();
                                //int dotPlace = name.indexOf(".");
                                //if (dotPlace > 1) {
                                //	name = name.substring(0, dotPlace - 1);
                                //}
                                included.add(name);
                        }
                }
                return included;
        }
   */
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
    alignPane.setVerticalScrollBarPolicy(
        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    alignPane.setHorizontalScrollBarPolicy(
        JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    alignPane.setPreferredSize(new Dimension(800, 100));

    resultsPanel.add(alignPane);
    resultsPanel.repaint();
    this.repaint();
  }

  private void showAlignment(DotMatrixObj hit) {
    String text;

    text =
        "query:\t";
    align.setText(text);

  }

  /**
   * Returns a JScrollpane containing Blast results in table format.
   *
   * @return 		a JScrollpane containing table of Blast results.
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
  private class listSelectionListener
      implements ListSelectionListener {
    int selectedRow;
    DotMatrixObj selectedHit;

    public void valueChanged(ListSelectionEvent e) {
      //Ignore extra messages.
      if (e.getValueIsAdjusting())
        return;
      ListSelectionModel lsm = (ListSelectionModel) e.getSource();
      if (lsm.isSelectionEmpty()) {
      }
      else {
        selectedRow = lsm.getMinSelectionIndex();
        showAlignment(selectedHit);
      }
    }
  }

  /**
   * This class extends AbstractTableModel and creates a table view of Blast
   * results.
   */
  private class HitsTableModel
      extends AbstractTableModel {
    /* array of the column names in order from left to right*/
    final String[] columnNames = {
        "db",
        "Name",
        "Description",
        "e-value",
        "align length",
        "%identity",
        "Include"};
    DotMatrixObj hit;

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
      }
      else {
        return true;
      }
    }

    /*detect change in cell at (row, col); set cell to value; update the table */
    public void setValueAt(Object value, int row, int col) {
      hit = (DotMatrixObj) hits.get(row);
      fireTableCellUpdated(row, col);
    }

  }

  /**
   * This is a class that implements ActionListener and listens for a user-
   * initated button click.  It then attempts to create a new RemoteBlast
   * using the user-designated sequence source as a query.
   */
  private class SyntenyListener
      implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String query = textArea.getText();
      if (query.length() < 1 || query == null) {
        textArea.setText("No protein sequence!");
      }
      else {
        if (currentseq.isSelected()) {
          rdots = new RemoteDots(queryX, queryY, textArea);
        }
      String DOTS_rid = rdots.submitDots();
      rdots.getDots(DOTS_rid);
      }
    }

    /**
     * This is a class that implements ActionListener and listens for a user-
     * initated button click.  It then checks if Blast is done and informs the
     * user if not so.  Otherwise it creates a new BlastParser to parse out
     * Blast results and generates a table formatted view of the Blast hits for
     * display.
     */
    private class DisplaySyntenylistener
        implements ActionListener {
      public void actionPerformed(ActionEvent e) {
        textArea.append("\nSynteny Analysis in progress, wait please...\n");
      }
    }
  }
}
