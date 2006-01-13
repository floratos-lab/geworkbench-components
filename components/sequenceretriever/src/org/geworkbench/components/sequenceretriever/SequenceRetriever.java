package org.geworkbench.components.sequenceretriever;

import gov.nih.nci.caBIO.bean.Gene;
import gov.nih.nci.common.exception.ManagerException;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.annotationparser.AnnotationParser;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.promoter.SequencePatternDisplayPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 * <p>Widget to retrieve Promoter sequence from UCSC's DAS sequence server</p>
 * <p>Copyright: Copyright (c) 2003 - 2005</p>
 *
 * @author Xuegong Wang
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 3.0
 */

@AcceptTypes( {DSMicroarraySet.class})public class SequenceRetriever implements
        VisualPlugin {

    DSPanel<DSGeneMarker> markers = null;
    DSPanel<DSGeneMarker> activeMarkers = null;
    private CSSequenceSet sequenceDB = new CSSequenceSet();

    boolean selectedRegionChanged = false;

    //  selected results
    Vector results = new Vector();
    private JPanel main = new JPanel();

    //Layouts

    private BorderLayout borderLayout2 = new BorderLayout();

    //Panels and Panes
    private JToolBar jToolbar2 = new JToolBar();
    private JScrollPane seqScrollPane = new JScrollPane();

    private SequencePatternDisplayPanel seqDisPanel = new
            SequencePatternDisplayPanel();

    JPanel jPanel2 = new JPanel();

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

    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel jPanel3 = new JPanel();
    GridLayout gridLayout1 = new GridLayout();
    JButton jActivateBttn = new JButton();
    JButton jButton2 = new JButton();
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
    JPanel jPanel6 = new JPanel();
    JProgressBar jProgressBar1 = new JProgressBar();
    GridLayout gridLayout2 = new GridLayout();
    JLabel jLabel6 = new JLabel();
    FlowLayout flowLayout1 = new FlowLayout();

    public SequenceRetriever() {
        try {
            jbInit();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    class ResultCellRenderer extends JPanel implements ListCellRenderer {
        JCheckBox box = new JCheckBox();
        JLabel label = new JLabel();

        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
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

        jPanel1.setLayout(flowLayout1);
//        jComboCategory.setPreferredSize(new Dimension(46, 21));
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

        jPanel4.setLayout(borderLayout5);
        jLabel4.setText("Selected Microarray Markers");
        jComboCategory.setSelectedItem("DNA");
        jComboCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {

                String cmd = (String) jComboCategory.getSelectedItem();
                if (cmd.equalsIgnoreCase("Protein")) {
                    beforeText.setEnabled(false);
                    afterText.setEnabled(false);
                } else {
                    beforeText.setEnabled(true);
                    afterText.setEnabled(true);
                }

            }
        });
        main.add(jToolbar2, BorderLayout.SOUTH);
        jToolbar2.add(jLabel2, null);
        jToolbar2.add(beforeText, null);
        jToolbar2.add(jLabel1, null);
        jToolbar2.add(afterText, null);
        jToolbar2.add(jButton2, null);
        jToolbar2.add(jActivateBttn, null);
        jPanel2.add(seqScrollPane, BorderLayout.CENTER);
        jPanel2.add(jPanel3, BorderLayout.WEST);

        jPanel3.add(jPanel4, null);
        jPanel4.add(jScrollPane2, BorderLayout.CENTER);

        jSelectedList.setModel(ls2);
        jScrollPane2.getViewport().add(jSelectedList, null);
        jPanel4.add(jLabel4, BorderLayout.NORTH);
        jPanel2.add(jPanel1, BorderLayout.NORTH);
        jPanel1.add(jLabel6, null);
        jPanel1.add(jComboCategory, null);
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

    void jButton2_actionPerformed(ActionEvent e) {
        if (ls2.getSize() > 0) {
            seqDisPanel.initialize();
            jProgressBar1.setIndeterminate(true);
            sequenceDB.clear();
            Thread t = new Thread() {

                public void run() {

                    getSequences();
                    jProgressBar1.setIndeterminate(false);

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
                sequenceDB.setLabel(label);
                ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(
                        "message", sequenceDB, null);
                publishProjectNodeAddedEvent(event);
            }
        }
    }

    void text_actionPerformed(ChangeEvent e) {
        this.selectedRegionChanged = true;
    }

    private void getSequences(DSGeneMarker marker) {
        CSSequence seqs = PromoterSequenceFetcher.getCachedPromoterSequence(
                marker, ((Integer) model.getNumber()).intValue(),
                ((Integer) model1.getNumber()).intValue());

        if (seqs != null) {
            sequenceDB.addASequence(seqs);
            sequenceDB.parseMarkers();
        }
    }

    void getSequences() {
        if (markers != null) {
            sequenceDB.clear();
            // sequenceDB = new CSSequenceSet();
            String fileName = this.getRandomFileName();
            if (((String) jComboCategory.getSelectedItem()).equalsIgnoreCase(
                    "DNA")) {

                for (int i = 0; i < ls2.size(); i++) {
                    DSGeneMarker marker = (DSGeneMarker) ls2.get(i);
                    getSequences(marker);
                }

                if (sequenceDB.getSequenceNo() == 0) {
                    JOptionPane.showMessageDialog(getComponent(),
                            "No sequences retrieved for selected markers");
                } else {
                    sequenceDB.writeToFile(fileName);
                }
            } else {
                BufferedWriter br = null;
                try {
                    br = new BufferedWriter(new FileWriter(fileName));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (markers != null && markers.panels() != null) {
//                    for (int j = 0; j < markers.panels().size(); j++) {
//                         DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
//                        //DSPanel<DSGeneMarker> mrk = markers.get(j);
//                        System.out.println(mrk.size() + " " + markers.size() + " " + markers.panels().size());
//                        for (int i = 0; i < mrk.size(); i++) {
//
//                            String affyid = mrk.get(i).getLabel();
//                            if (affyid.endsWith("_at")) { // if this is affyid
//
//                                getAffyProteinSequences(affyid, br);
//                            }
//                        }
//                    }
                    for (DSGeneMarker geneMarker : markers) {
                        String affyid = geneMarker.getLabel();
                        if (affyid.endsWith("_at")) { // if this is affyid

                            getAffyProteinSequences(affyid, br);
                        }

                    }
                }
                try {
                    br.close();
                } catch (IOException ex1) {
                    ex1.printStackTrace();
                }
                sequenceDB.readFASTAfile(new File(fileName));
                if (sequenceDB.getSequenceNo() == 0) {
                    JOptionPane.showMessageDialog(getComponent(),
                            "No sequences retrieved for selected markers");
                }
            }
            seqDisPanel.initialize(sequenceDB);
        }
    }


    private void getAffyProteinSequences(String affyid, BufferedWriter br) {
        try {

            Call call = (Call)new Service().createCall();
            call.setTargetEndpointAddress(new java.net.URL(
                    "http://www.ebi.ac.uk/ws/services/Dbfetch"));
            call.setOperationName(new QName("urn:Dbfetch", "fetchData"));
            call.addParameter("query", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("format", XMLType.XSD_STRING, ParameterMode.IN);
            call.addParameter("style", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.SOAP_ARRAY);
            String[] uniprotids = AnnotationParser.getInfo(affyid,
                    AnnotationParser.SWISSPROT);
            if (uniprotids != null) {
                for (int i = 0; i < uniprotids.length; i++) {
                    if (uniprotids[i] != null &&
                        !uniprotids[i].trim().equals("")) {
                        String[] result = (String[]) call.invoke(new Object[] {
                                "uniprot:" + uniprotids[i], "fasta", "raw"});

                        if (result.length == 0) {
                            System.out.println("hmm...something wrong :-(\n");
                        } else {

                            for (int count = 0; count < result.length; count++) {
                                result[count] = result[count].replaceAll(">",
                                        ">" + affyid + "|");
                                br.write(result[count]); //need to write to a sequenceDB.
                                br.newLine();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Publish public ProjectNodeAddedEvent publishProjectNodeAddedEvent(org.
            geworkbench.events.ProjectNodeAddedEvent event) {
        return event;
    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */
    @Subscribe public void receive(GeneSelectorEvent e, Object publisher) {
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
//          activeMarkers = markers.activeSubset();
            System.out.println(activeMarkers.size() + " " + markers.size());
            markers = activeMarkers;
//
            System.out.println("afet" + activeMarkers.size() + " " +
                               markers.size());
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
}
