package org.geworkbench.components.sequenceretriever;

import gov.nih.nci.caBIO.bean.Gene;
import gov.nih.nci.common.exception.ManagerException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.
        DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        DSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.
        AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.geworkbench.components.sequenceretriever.SequenceFetcher;
import org.geworkbench.util.sequences.GeneChromosomeMatcher;

import java.util.*;

/**
 * <p/>
 * Widget to retrieve Promoter sequence from UCSC's DAS sequence server
 * </p>
 * <p/>
 * Copyright: Copyright (c) 2003 - 2005
 * </p>
 *
 * @author Xuegong Wang
 * @author manjunath at genomecenter dot columbia dot edu
 * @author xiaoqing at genomecenter dot columbia dot edu
 * @version 3.0
 */

@AcceptTypes({DSMicroarraySet.class})
public class SequenceRetriever implements VisualPlugin {

    private Log log = LogFactory.getLog(SequenceRetriever.class);

    DSPanel<DSGeneMarker> markers = null;
    DSPanel<DSGeneMarker> activeMarkers = null;
    private CSSequenceSet displaySequenceDB = new CSSequenceSet();
    private CSSequenceSet sequenceDB = new CSSequenceSet<DSSequence>();
    private CSSequenceSet selectedSequences = new CSSequenceSet<DSSequence>();
    private DSItemList markerList;
    public static final String NOANNOTATION = "---";
    boolean selectedRegionChanged = false;
    protected DSMicroarraySet<DSMicroarray> refMASet = null;
    private final static String NORMAL = "normal";
    private final static String STOP = "stop";
    private final static String CLEAR = "clear";
    private final static String RUNNING = "running";
    private String status = NORMAL;
    // selected results
    Vector results = new Vector();
    Vector tfNameSet;
    private JPanel main = new JPanel();

    // Layouts

    private BorderLayout borderLayout2 = new BorderLayout();

    // Panels and Panes
    private JToolBar jToolbar2 = new JToolBar();
    private JScrollPane seqScrollPane = new JScrollPane();

    private RetrievedSequencesPanel seqDisPanel = new
            RetrievedSequencesPanel();
    private HashMap<String, RetrievedSequenceView> retrievedMap = new HashMap<String, RetrievedSequenceView>();
    private TreeMap<String, ArrayList<String>> retrievedSequences = new TreeMap<String, ArrayList<String>>();

    JPanel jPanel2 = new JPanel();
    public static String newline = System.getProperty("line.separator");
    SpinnerNumberModel model = new SpinnerNumberModel(1999, 1, 1999, 1);
    JSpinner beforeText = new JSpinner();
    SpinnerNumberModel model1 = new SpinnerNumberModel(2000, 1, 2000, 1);
    JSpinner afterText = new JSpinner();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();

    JPopupMenu jpopMenu = new JPopupMenu();
    JMenuItem jActivateItem = new JMenuItem();
    JMenuItem jDeactivateItem = new JMenuItem();
    JMenuItem jDeleteItem = new JMenuItem();
    JMenuItem jClearUnselectedItem = new JMenuItem();
    JMenuItem jClearAllItem = new JMenuItem();
    JSplitPane rightPanel = new JSplitPane();

    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel jPanel3 = new JPanel();
    GridLayout gridLayout1 = new GridLayout();
    JButton jActivateBttn = new JButton();
    JButton jButton2 = new JButton();
    JButton stopButton = new JButton();
    JButton clearButton = new JButton();
    ButtonGroup sourceGroup = new ButtonGroup();
    JPanel jPanel1 = new JPanel();
    JScrollPane jScrollPane2 = new JScrollPane();
    JPanel jPanel4 = new JPanel();
    BorderLayout borderLayout5 = new BorderLayout();
    JLabel jLabel4 = new JLabel();
    DefaultListModel ls1 = new DefaultListModel();
    DefaultListModel ls2 = new DefaultListModel();
    JList jSelectedList = new JList();
    JComboBox jComboCategory = new JComboBox();
    JComboBox jSourceCategory = new JComboBox();
    JPanel jPanel6 = new JPanel();
    JProgressBar jProgressBar1 = new JProgressBar();
    GridLayout gridLayout2 = new GridLayout();
    JLabel jLabel6 = new JLabel();
    JTabbedPane tabPane = new JTabbedPane();
    JPanel markerPanel = new JPanel();
    FlowLayout flowLayout1 = new FlowLayout();
    public static final String LOCAL = "Local";
    public static final String UCSC = "UCSC";
    public static final String CABIO = "CABIO";
    public static final String EBI = "EBI";

    public SequenceRetriever() {
        try {
            jbInit();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class SequenceListRenderer extends JLabel implements ListCellRenderer {

        public Component getListCellRendererComponent(
                JList list,
                Object value,            // value to display
                int index,               // cell index
                boolean isSelected,      // is the cell selected
                boolean cellHasFocus)    // the list and the cell have the focus
        {
            String s = value.toString();
            if (retrievedSequences.containsKey(s)) {
                setText("<html><font color=blue>" + s + "</font></html>");
            } else {
                setText(s);
            }

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());
            return this;
        }
    }

    class ResultCellRenderer extends JPanel implements ListCellRenderer {
        JCheckBox box = new JCheckBox();
        JLabel label = new JLabel();

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            BorderLayout bd = new BorderLayout();
            this.setLayout(bd);
            this.add(box, BorderLayout.WEST);
            this.add(label, BorderLayout.CENTER);

            Gene gene = (Gene) value;
            box.setSelected(results.contains(gene));
            String s = null;
            try {
                s = gene.getOrganismAbbreviation() + gene.getClusterId() + "(" +
                        gene.getName() + ")";
            } catch (ManagerException ex) {
            }

            label.setText(s);
            if (isSelected) {
                box.setBackground(list.getSelectionBackground());
                box.setForeground(list.getSelectionForeground());
                label.setBackground(list.getSelectionBackground());
                label.setForeground(list.getSelectionForeground());
            } else {
                label.setBackground(list.getBackground());
                label.setForeground(list.getForeground());
            }
            setEnabled(list.isEnabled());
            setFont(list.getFont());

            return this;
        }

        public JCheckBox getCheckBox() {
            return box;
        }
    }


    void jbInit() throws Exception {
        jProgressBar1.setForeground(Color.green);
        jProgressBar1.setMinimumSize(new Dimension(10, 16));
        jProgressBar1.setBorderPainted(true);
        jPanel6.setLayout(gridLayout2);
        jLabel6.setHorizontalAlignment(SwingConstants.LEFT);
        jLabel6.setText("Type");
        rightPanel = new JSplitPane();
        rightPanel.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        jPanel1.setLayout(flowLayout1);
        // jComboCategory.setPreferredSize(new Dimension(46, 21));
        jpopMenu.add(jClearUnselectedItem);
        jpopMenu.add(jClearAllItem);

        seqDisPanel.setBorder(null);
        seqDisPanel.setMinimumSize(new Dimension(10, 10));
        jPanel3.setBorder(null);

        main.setLayout(borderLayout2);
        jToolbar2.setBorder(BorderFactory.createEtchedBorder());
        jToolbar2.setMinimumSize(new Dimension(20, 25));
        jToolbar2.setPreferredSize(new Dimension(20, 25));

        seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
        seqScrollPane.setMaximumSize(new Dimension(32767, 32767));
        seqScrollPane.setMinimumSize(new Dimension(24, 24));
        seqScrollPane.setPreferredSize(new Dimension(250, 250));

        jPanel2.setLayout(borderLayout1);
        beforeText.setModel(model);
        beforeText.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                text_actionPerformed(e);
            }
        });
        beforeText.setSize(new Dimension(15, 10));
        beforeText.setPreferredSize(new Dimension(15, 10));
        afterText.setModel(model1);
        afterText.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                text_actionPerformed(e);
            }
        });
        afterText.setSize(new Dimension(15, 10));
        afterText.setPreferredSize(new Dimension(15, 10));
        jLabel1.setToolTipText("downstream");
        jLabel1.setText("+");
        jLabel2.setToolTipText("Upstream");
        jLabel2.setText("-");
        seqDisPanel.setMaximumSize(new Dimension(32767, 32767));
        seqDisPanel.setPreferredSize(new Dimension(216, 40));

        jPanel3.setLayout(gridLayout1);
        jPanel3.setPreferredSize(new Dimension(160, 240));
        gridLayout1.setColumns(1);
        jActivateBttn.setMaximumSize(new Dimension(100, 27));
        jActivateBttn.setMinimumSize(new Dimension(100, 27));
        jActivateBttn.setPreferredSize(new Dimension(100, 27));
        jActivateBttn.setToolTipText("Add a data node to current project");
        jActivateBttn.setText("Add To Project");
        jActivateBttn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jActivateBttn_actionPerformed(e);
            }
        });
        jButton2.setToolTipText("Get sequence of selected markers");
        jButton2.setHorizontalTextPosition(SwingConstants.TRAILING);
        jButton2.setText("Get Sequence");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
            }
        });
        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopButton_actionPerformed(e);
            }
        });
        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearButton_actionPerformed(e);
            }
        });
        jPanel4.setLayout(borderLayout5);
        jLabel4.setText("Selected Microarray Markers");
        jComboCategory.setSelectedItem("DNA");
        jComboCategory.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String cmd = (String) jComboCategory.getSelectedItem();
                if (cmd.equalsIgnoreCase("Protein")) {
                    beforeText.setEnabled(false);
                    afterText.setEnabled(false);
                    jSourceCategory.removeAllItems();
                    jSourceCategory.addItem(EBI);
                } else {
                    beforeText.setEnabled(true);
                    afterText.setEnabled(true);
                    jSourceCategory.removeAllItems();
                    jSourceCategory.addItem(UCSC);
                    jSourceCategory.addItem(LOCAL);
                    //jSourceCategory.addItem(CABIO);
                    jSourceCategory.setSelectedItem(UCSC);
                }

            }
        });
        jSourceCategory.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (jSourceCategory != null) {
                    String cmd = (String) jSourceCategory.getSelectedItem();
                    if (cmd != null && cmd.equalsIgnoreCase(LOCAL)) {
                        model = new SpinnerNumberModel(1999, 1, 1999, 1);
                        model1 = new SpinnerNumberModel(2000, 1, 2000, 1);
                    } else if (cmd != null && cmd.equalsIgnoreCase(UCSC)) {
                        model = new SpinnerNumberModel(10000, 1, 98000, 1);
                        model1 = new SpinnerNumberModel(10000, 1, 10000, 1);
                    }
                    beforeText.setModel(model);
                    afterText.setModel(model1);
                    beforeText.revalidate();
                    beforeText.repaint();
                    main.repaint();
                }
            }
        });

        main.add(jToolbar2, BorderLayout.SOUTH);
        jToolbar2.add(jLabel2, null);
        jToolbar2.add(beforeText, null);
        jToolbar2.add(jLabel1, null);
        jToolbar2.add(afterText, null);
        jToolbar2.add(stopButton, null);
        jToolbar2.add(clearButton, null);
        jToolbar2.add(jButton2, null);
        jToolbar2.add(jActivateBttn, null);
//        jPanel2.add(seqScrollPane, BorderLayout.CENTER);
//        jPanel2.add(jPanel3, BorderLayout.WEST);

        jPanel2.add(rightPanel, BorderLayout.CENTER);
        tabPane = new JTabbedPane();
        markerPanel = new TFListPanel();
        tabPane.add("Marker", jPanel3);
        tabPane.add("Find a Marker", markerPanel);
        rightPanel.add(seqScrollPane, JSplitPane.RIGHT);
        rightPanel.add(tabPane, JSplitPane.LEFT);
        jPanel3.add(jPanel4, null);
        jPanel4.add(jScrollPane2, BorderLayout.CENTER);
        jSelectedList.setModel(ls2);
        jSelectedList.setCellRenderer(new SequenceListRenderer());
        jScrollPane2.getViewport().add(jSelectedList, null);
        //jPanel4.add(jLabel4, BorderLayout.NORTH);
        jPanel2.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jLabel6, null);
        jPanel1.add(jComboCategory, null);
        jPanel1.add(jSourceCategory, null);
        main.add(jPanel6, BorderLayout.NORTH);
        jPanel6.add(jProgressBar1, null);
        seqScrollPane.getViewport().add(seqDisPanel, null);
        main.add(jPanel2, BorderLayout.CENTER);
        jComboCategory.addItem("DNA");
        jComboCategory.addItem("Protein");
    }

    public void setSequenceDB(CSSequenceSet db2) {
        sequenceDB = db2;

    }

    public DSSequenceSet getSequenceDB() {
        return sequenceDB;
    }

    void updateProgressBar(final double percent, final String text) {
        Runnable r = new Runnable() {
            public void run() {
                try {
                    jProgressBar1.setForeground(Color.GREEN);
                    jProgressBar1.setString(text);
                    jProgressBar1.setValue((int) (percent * 100));
                    if (text.startsWith("Stop")) {
                        jProgressBar1.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                }
            }
        };
        SwingUtilities.invokeLater(r);
    }


    void stopButton_actionPerformed(ActionEvent e) {
        status = STOP;
        stopButton.setEnabled(false);
    }

    void clearButton_actionPerformed(ActionEvent e) {
        status = CLEAR;
        cleanUp();
    }

    void cleanUp() {
        retrievedMap = new HashMap<String, RetrievedSequenceView>();
        retrievedSequences = new TreeMap<String, ArrayList<String>>();
        jSelectedList.repaint();
        seqDisPanel.setRetrievedMap(retrievedMap);
        seqDisPanel.initialize();
    }

    void jButton2_actionPerformed(ActionEvent e) {
        cleanUp();

        status = RUNNING;
        stopButton.setEnabled(true);
        if (ls2.getSize() > 0) {
            seqDisPanel.initialize();
            jProgressBar1.setIndeterminate(false);
            jProgressBar1.setMinimum(0);
            jProgressBar1.setMaximum(100);
            jProgressBar1.setStringPainted(true);
            jProgressBar1.setValue(0);
            if (sequenceDB != null) {
                sequenceDB = new CSSequenceSet();
            }
            Thread t = new Thread() {

                public void run() {
                    getSequences(ls2);
                    if (status.equalsIgnoreCase(STOP)) {
                        sequenceDB = new CSSequenceSet();
                        updateProgressBar(100, "Stopped on " + new Date());
                    } else {
                        updateProgressBar(100, "Finished on " + new Date());
                        jSelectedList.updateUI();
                        //jSelectedList.repaint();
                        //main.revalidate();
                        seqDisPanel.setRetrievedMap(retrievedMap);
                    }
                    stopButton.setEnabled(false);
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        } else {
            JOptionPane.showMessageDialog(null,
                    "Please select gene(s) or marker(s).");
        }
    }

    void jActivateBttn_actionPerformed(ActionEvent e) {
        if ((sequenceDB != null) && (sequenceDB.getSequenceNo() >= 1)) {
            String label = JOptionPane.showInputDialog(
                    "Please enter a name for the dataset");
            if (label != null) {
                CSSequenceSet selectedSequenceDB = new CSSequenceSet();
                for (Object sequence : sequenceDB) {
                    if (sequence != null) {
                        RetrievedSequenceView retrievedSequenceView = retrievedMap.get(sequence.toString());
                        if (retrievedSequenceView.isIncluded()) {
                            selectedSequenceDB.add(sequence);
                        }
                    }
                }
                selectedSequenceDB.setLabel(label);
                ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
                        "message", selectedSequenceDB, null);
                publishProjectNodeAddedEvent(event);
            }
        }
    }

    void text_actionPerformed(ChangeEvent e) {
        this.selectedRegionChanged = true;
    }

    private void getSequences(DSGeneMarker marker) {
        CSSequence seqs = PromoterSequenceFetcher.getCachedPromoterSequence(
                marker, ((Integer) model.getNumber())
                .intValue(), ((Integer) model1.getNumber()).intValue());

        if (seqs != null) {
            sequenceDB.addASequence(seqs);
            RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView(seqs);
            retrievedMap.put(seqs.toString(), retrievedSequenceView);
            sequenceDB.parseMarkers();
            if (retrievedSequences.containsKey(marker.toString())) {
                ArrayList<String> values = retrievedSequences.get(marker.toString());
                values.add(seqs.toString());
            } else {
                ArrayList<String> values = new ArrayList<String>();
                values.add(seqs.toString());
                retrievedSequences.put(marker.toString(), values);
            }
        }
    }

    void getSequences(DefaultListModel selectedList) {

        if (markers != null && selectedList != null) {
            // sequenceDB.clear();
            sequenceDB = new CSSequenceSet();
            // sequenceDB = new CSSequenceSet();
            String fileName = this.getRandomFileName();
            if (((String) jComboCategory.getSelectedItem()).equalsIgnoreCase(
                    "DNA")) {
                if (jSourceCategory.getSelectedItem().equals(LOCAL)) {
                    for (int i = 0; i < selectedList.size(); i++) {
                        DSGeneMarker marker = (DSGeneMarker) selectedList.get(i);
                        double progress = (double) i / (double) (selectedList.size());
                        updateProgressBar(progress,
                                "Retrieving " + marker.getLabel());
                        if (status.equalsIgnoreCase(STOP)) {
                            return;
                        }
                        getSequences(marker);
                    }

                } else if (jSourceCategory.getSelectedItem().equals(UCSC)) {
                    int startPoint = ((Integer) model.getNumber())
                            .intValue();
                    int endPoint = ((Integer) model1.getNumber()).
                            intValue();
                    String database = SequenceFetcher.matchChipType(
                            AnnotationParser.getCurrentChipType());
                    for (int i = 0; i < selectedList.size(); i++) {
                        DSGeneMarker marker = (DSGeneMarker) selectedList.get(i);
                        double progress = (double) (i + 1) / (double) (selectedList.size());
                        if (status.equalsIgnoreCase(STOP)) {
                            return;
                        }
                        updateProgressBar(progress,
                                "Retrieving " + marker.getLabel());
                        String[] knownGeneName = AnnotationParser.getInfo(
                                marker.getLabel(), AnnotationParser.REFSEQ);
                        if (knownGeneName != null && knownGeneName.length > 0) {

                            for (String geneName : knownGeneName) {
                                if (geneName == null ||
                                        geneName.equals(NOANNOTATION)) {
                                    continue;
                                }
                                Vector geneChromosomeMatchers =
                                        SequenceFetcher.
                                                getGeneChromosomeMatchers(geneName,
                                                        database);
                                if (geneChromosomeMatchers != null) {
                                    for (int j = 0;
                                         j < geneChromosomeMatchers.
                                                 size();
                                         j++) {
                                        GeneChromosomeMatcher o = (
                                                GeneChromosomeMatcher)
                                                geneChromosomeMatchers.get(j);
                                        CSSequence seqs = SequenceFetcher.
                                                getSequenceFetcher().
                                                getSequences(o, startPoint,
                                                        endPoint);
                                        if (seqs != null) {
                                            seqs.setLabel(marker.getLabel() +
                                                    "_" + o.getChr() + "_" + o.getStartPoint() + "_" + o.getEndPoint());
                                            sequenceDB.addASequence(seqs);
                                            RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView(seqs);
                                            retrievedSequenceView.setGeneChromosomeMatcher(o);
                                            retrievedMap.put(seqs.toString(), retrievedSequenceView);
                                            if (retrievedSequences.containsKey(marker.toString())) {
                                                ArrayList<String> values = retrievedSequences.get(marker.toString());
                                                values.add(seqs.toString());
                                            } else {
                                                ArrayList<String> values = new ArrayList<String>();
                                                values.add(seqs.toString());
                                                retrievedSequences.put(marker.toString(), values);
                                            }
                                            //sequenceDB.parseMarkers();
                                        }

                                    }
                                }
                            }
                        }
                    }

                }
                //get the object sequenceDB from local or UCSC, save it to a local file.
                if (sequenceDB.getSequenceNo() != 0) {
                    sequenceDB.writeToFile(fileName);
                    sequenceDB = new CSSequenceSet();
                    sequenceDB.readFASTAFile(new File(fileName));
                }
            } else {
                sequenceDB = new CSSequenceSet();
                BufferedWriter br = null;
                try {
                    br = new BufferedWriter(new FileWriter(fileName));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (markers != null && markers.panels() != null) {

                    for (DSGeneMarker geneMarker : markers) {
                        String affyid = geneMarker.getLabel();
                        if (status.equalsIgnoreCase(STOP)) {
                            return;
                        }
                        if (affyid.endsWith("_at")) { // if this is affyid
                            CSSequenceSet sequenceSet = SequenceFetcher.getAffyProteinSequences(affyid);

                            String[] uniprotids = AnnotationParser.getInfo(affyid,
                                    AnnotationParser.SWISSPROT);
                            if (sequenceSet.size() > 0) {
                                ArrayList<String> values = new ArrayList<String>();
                                int i = 0;
                                for (Object o : sequenceSet) {
                                    retrievedSequences.put(geneMarker.toString(), values);
                                    sequenceDB.addASequence((CSSequence) o);
                                    RetrievedSequenceView retrievedSequenceView = new RetrievedSequenceView((CSSequence) o);
                                    retrievedSequenceView.setUrl(uniprotids[i++]);
                                    retrievedMap.put(o.toString(), retrievedSequenceView);
                                    sequenceDB.parseMarkers();
                                    values.add(o.toString());
                                    retrievedSequences.put(geneMarker.toString(), values);

                                }
                            }

                        }
                    }

                }
                try {
                    br.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
                //Need to remove all previous result.
                // sequenceDB = new CSSequenceSet();
                //  sequenceDB.readFASTAFile(new File(fileName));
                /**
                 * todo Don't know why we need save it in temp file. Maybe for the editor to read it?
                 */
                if (sequenceDB.getSequenceNo() != 0) {
                    sequenceDB.writeToFile(fileName);
                    sequenceDB = new CSSequenceSet();
                    sequenceDB.readFASTAFile(new File(fileName));
                }

            }
            if (sequenceDB.getSequenceNo() == 0) {
                JOptionPane.showMessageDialog(getComponent(),
                        "No sequences retrieved for selected markers");
            }

            seqDisPanel.initialize(sequenceDB);
            main.revalidate();
            main.repaint();

        }
    }

    @Publish
    public ProjectNodeAddedEvent publishProjectNodeAddedEvent(org.
            geworkbench.events.ProjectNodeAddedEvent event) {
        return event;
    }

    /**
     * receiveProjectSelection
     *
     * @param e ProjectEvent
     */
    @Subscribe
    @SuppressWarnings("unchecked")
    public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

        log.debug("Source object " + source);

        if (e.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
            refMASet = null;
        } else {
            DSDataSet dataSet = e.getDataSet();
            if (dataSet instanceof DSMicroarraySet) {
                if (refMASet != dataSet) {
                    this.refMASet = (DSMicroarraySet) dataSet;
                    sequenceDB = new CSSequenceSet();
                    seqDisPanel.initialize();
                    seqDisPanel.initPanelView();
                    markerList = refMASet.getMarkers();
                }
            }

            //remove below part for production.
            if (dataSet instanceof DSSequenceSet) {
                seqDisPanel.setSequenceDB((DSSequenceSet) dataSet);
            }
            // refreshMaSetView();
        }
    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe
    public void receive(GeneSelectorEvent e, Object publisher) {
        markers = e.getPanel();
        activeMarkers = new CSPanel();
        if (markers != null) {
            ls2.clear();
            for (int j = 0; j < markers.panels().size(); j++) {
                DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
                if (mrk.isActive()) {
                    for (int i = 0; i < mrk.size(); i++) {
                        if (!ls2.contains(mrk.get(i))) {
                            ls2.addElement(mrk.get(i));
                        }
                        activeMarkers.add(mrk.get(i));

                    }

                }
            }
            // activeMarkers = markers.activeSubset();
            markers = activeMarkers;
            //
            log.debug("Active markers / markers: " + activeMarkers.size() +
                    " / " + markers.size());
        }
    }

    /**
     * getComponent
     *
     * @return Component
     */
    public Component getComponent() {
        return main;
    }

    void jResultList_mouseClicked(MouseEvent e) {
    }

    /**
     * todo sdfsd
     *
     * @param e ActionEvent
     */

    void jClearAllItem_actionPerformed(ActionEvent e) {
        ls1.removeAllElements();
    }

    private String getRandomFileName() {
        String tempString = "temp" + RandomNumberGenerator.getID() + ".fasta";
        String tempFolder = System.getProperties().getProperty(
                "temporary.files.directory");

        if (tempFolder == null) {
            tempFolder = ".";
        }
        String fileName = tempFolder + tempString;
        return fileName;
    }


    private class TFListPanel extends JPanel {


        public static final String NEXT_BUTTON_TEXT = "Find";
        public static final String SEARCH_LABEL_TEXT = "Search:";

        private JList list;
        private JButton nextButton;
        private JTextField searchField;
        private DefaultListModel model;
        private JScrollPane scrollPane;

        private boolean lastSearchFailed = false;
        private boolean lastSearchWasAscending = true;

        private boolean prefixMode = false;

        public TFListPanel() {
            this(null);
        }

        public TFListPanel(ListModel themodel) {
            super();
            //this.model = model;
            // Create and lay out components
            model = new DefaultListModel();

            setLayout(new BorderLayout());
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
            JLabel searchLabel = new JLabel(SEARCH_LABEL_TEXT);
            nextButton = new JButton(NEXT_BUTTON_TEXT);
            searchField = new JTextField();
            list = new JList(model);
            scrollPane = new JScrollPane();
            // Compose components
            topPanel.add(searchLabel);
            topPanel.add(searchField);
            topPanel.add(nextButton);
            add(topPanel, BorderLayout.NORTH);
            scrollPane.getViewport().setView(list);
            add(scrollPane, BorderLayout.CENTER);
            // Add appropriate listeners
            nextButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    findNext(true);
                }
            });
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    handleMouseEvent(e);
                }
            });
            searchField.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    searchFieldChanged();
                }

                public void removeUpdate(DocumentEvent e) {
                    searchFieldChanged();
                }

                public void changedUpdate(DocumentEvent e) {
                    searchFieldChanged();
                }
            });

        }

        private void handleMouseEvent(MouseEvent event) {
            int index = list.locationToIndex(event.getPoint());
            if (index != -1) {
                if (event.isMetaDown()) {
                    elementRightClicked(index, event);
                } else if (event.getButton() == MouseEvent.BUTTON1) {
                    if (event.getClickCount() > 1) {
                        elementDoubleClicked(index, event);
                    } else {
                        elementClicked(index, event);
                    }
                }
            }
        }


        private void handlePostSearch() {
            if (lastSearchFailed) {
                searchField.setForeground(Color.red);
            }
        }

        private void handlePreSearch() {
            searchField.setForeground(Color.black);
        }

        /**
         * Override to customize the result of the 'next' button being clicked (or ENTER being pressed in text field).
         */
        protected boolean findNext(boolean ascending) {
            handlePreSearch();

            String text = searchField.getText();
            if (findNext(text) == null) {
                boolean confirmed = true;
                JOptionPane.showMessageDialog(this, "No marker can be found.");
            } else {
                boolean confirmed = false;
                int confirm = JOptionPane.showConfirmDialog(this, "Use the marker to search sequences?");
                if (confirm != JOptionPane.YES_OPTION) {
                    confirmed = true;
                }
                if (confirmed) {
                    if (model.getSize() > 0) {
                        seqDisPanel.initialize();
                        jProgressBar1.setIndeterminate(false);
                        jProgressBar1.setMinimum(0);
                        jProgressBar1.setMaximum(100);
                        jProgressBar1.setStringPainted(true);

                        if (sequenceDB != null) {
                            sequenceDB = new CSSequenceSet();
                        }
                        Thread t = new Thread() {

                            public void run() {
                                getSequences(model);
                                if (status.equalsIgnoreCase(STOP)) {
                                    sequenceDB = new CSSequenceSet();
                                    updateProgressBar(100, "Stopped on " + new Date());
                                } else {
                                    updateProgressBar(100, "Finished on " + new Date());
                                    jSelectedList.updateUI();
                                    seqDisPanel.setRetrievedMap(retrievedMap);
                                }
                                stopButton.setEnabled(false);
                            }
                        };
                        t.setPriority(Thread.MIN_PRIORITY);
                        t.start();
                    }

                }
            }
            handlePostSearch();
            return !lastSearchFailed;
        }

        /**
         * Search the markerList to get the matched markers.
         */
        protected DSItemList findNext(String query) {
            model.removeAllElements();
            if (markerList != null) {
                Object theOne = markerList.get(query);
                if (theOne != null) {
                    model.addElement(theOne);

                }
                for (Object o : markerList) {

                }
            }
            return null;
        }


        protected void searchFieldChanged() {
            handlePreSearch();

            handlePostSearch();
        }

        /**
         * Does nothing by default. Override to handle a list element being clicked.
         *
         * @param index the list element that was clicked.
         */
        protected void elementClicked(int index, MouseEvent e) {
        }

        /**
         * Does nothing by default. Override to handle a list element being double-clicked.
         *
         * @param index the list element that was clicked.
         */
        protected void elementDoubleClicked(int index, MouseEvent e) {
        }

        /**
         * Does nothing by default. Override to handle a list element being right-clicked.
         *
         * @param index the list element that was clicked.
         */
        protected void elementRightClicked(int index, MouseEvent e) {
        }

        public JList getList() {
            return list;
        }

        public ListModel getModel() {
            return model;
        }

        public int getHighlightedIndex() {
            return list.getSelectedIndex();
        }

        /**
         * Set the highlightedIndex automatically.
         *
         * @param theIndex int
         * @return boolean
         */
        public boolean setHighlightedIndex(int theIndex) {
            if (model != null && model.getSize() > theIndex) {
                list.setSelectedIndex(theIndex);
                list.scrollRectToVisible(list.getCellBounds(theIndex, theIndex));

                return true;
            }
            return false;
        }

        public boolean isPrefixMode() {
            return prefixMode;
        }

        public void setPrefixMode(boolean prefixMode) {
            this.prefixMode = prefixMode;
        }


    }


    private void itemRightClicked(int index, final MouseEvent e) {
        //ensureItemIsSelected(index);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //  itemListPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void selectedItemRightClicked(int index, final MouseEvent e) {
        //ensureItemIsSelected(index);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //  selectedItemListPopup.show(e.getComponent(), e.getX(),
                //         e.getY());
            }
        });
    }

    public void matrixDetailButton_actionPerformed(ActionEvent e) {

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


}
