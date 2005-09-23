package org.geworkbench.components.alignment.panels;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.JPopupMenu;
import java.util.HashMap;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.Calendar;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import org.geworkbench.components.alignment.synteny.SyntenyMapFragment;
import org.geworkbench.engine.config.events.EventSource;
import org.geworkbench.engine.config.VisualPlugin;

import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.util.session.SoapClient;
import org.geworkbench.components.alignment.synteny.DAS_Retriver;
import org.geworkbench.components.alignment.synteny.SyntenyMapObject;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import com.borland.jbcl.layout.VerticalFlowLayout;

/**
 * <p>Title: Bioworks</p>
 *
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class SyntenyParameters
    extends EventSource implements VisualPlugin {

    private HashMap listeners = new HashMap();

    boolean selectedRegionChanged = false;
    private SyntenyAnnotationParameters SAP = new SyntenyAnnotationParameters();
    private SyntenyDotMatrixParameters SDPM = new SyntenyDotMatrixParameters(
        SAP);
    private String single_marker = null;

    //Layouts
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout1 = new BorderLayout();
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private BorderLayout borderLayout5 = new BorderLayout();

    //Panels and Panes
    private JPanel main = new JPanel();
    private JTabbedPane jTabbedPane1 = new JTabbedPane();
    private JPanel jPanelMarkers = new JPanel();
    private JPanel jPanel1 = new JPanel();
    private JPanel jPanel2 = new JPanel();
    private JScrollPane jMarkerScrollPane = new JScrollPane();
    private JToolBar jToolbar2 = new JToolBar(); //    private SequencePatternDisplayPanel seqDisPanel = new
    private JPopupMenu SelectionMenu = new JPopupMenu();
    private JPopupMenu TreeSelectionMenu = new JPopupMenu();
    private JPopupMenu MarkerSelectionMenu = new JPopupMenu();

    private JTextField beforeText = new JTextField();
    private JTextField afterText = new JTextField();
    private JLabel jLabel1 = new JLabel();
    private JLabel jLabel2 = new JLabel();
    private ButtonGroup sourceGroup = new ButtonGroup();
    private JLabel jLabel4 = new JLabel();
    private DefaultListModel SelectedListModel = new DefaultListModel();
    private DefaultListModel ls2 = new DefaultListModel();
    private JList jInitialList = new JList();
    private JButton jButtonRun = new JButton();
    private JMenuItem jAddToX = new JMenuItem();
    private JMenuItem treeToX = new JMenuItem();
    private JMenuItem treeToY = new JMenuItem();
    private JMenuItem synMap = new JMenuItem("Build Synteny Map for this marker");
    private JLabel ProcessStatus = new JLabel();
    public SyntenyPresentationsList SPList = null;
    private JPanel jPanel3 = new JPanel();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private JLabel jLabelX = new JLabel();
    private JLabel jLabelY = new JLabel();
    private String tempDir = System.getProperty("temporary.files.directory");
    private SequenceAnnotation AnnoX = null;
    private SequenceAnnotation AnnoY = null;
    public JComboBox ProgramBox = new JComboBox();
    private boolean XSourceDAS = true;
    private boolean YSourceDAS = true;
    private GridBagLayout gridBagLayout3 = new GridBagLayout();
    private JScrollPane jScrollPane1 = new JScrollPane();

    private DefaultMutableTreeNode root = new ProjectTreeNode(
        "Select regions from");
    private DefaultTreeModel mTreeModel = new DefaultTreeModel(root);
    private JTree jTree1 = null;
    private DefaultMutableTreeNode category = null;
    JPanel jPanelProgram = new JPanel();
    JLabel jLabel3 = new JLabel();
    public SyntenyParameters() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        ActionListener treeListener = null;
        ActionListener listener = null;
        jAddToX.setText("Add marker");
        treeToX.setText("Select as X");
        treeToY.setText("Select as Y");
        SPList = new SyntenyPresentationsList();
        SDPM.setSyntenyPresentationsList(SPList);
        SAP.setSyntenyPresentationsList(SPList);
        SPList.setSyntenyAnnotationParameters(SAP);
        listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Menu_actionPerformed(e);
            }
        };

        treeListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Ttree_actionPerformed(e);
            }
        };

        mTreeModel.addTreeModelListener(new MyTreeModelListener());
        jTree1 = new JTree(mTreeModel);
        jTree1.setBackground(Color.white);
        jTree1.setEditable(true);
        jTree1.getSelectionModel().setSelectionMode
            (TreeSelectionModel.SINGLE_TREE_SELECTION);
        jTree1.setShowsRootHandles(true);

        listeners.put("Commands.Panels.Add to Panel", listener);
        jAddToX.addActionListener(listener);
        treeToX.addActionListener(treeListener);
        treeToY.addActionListener(treeListener);
        synMap.addActionListener(treeListener);

        ProgramBox.addItem("MUMmer");
        ProgramBox.addItem("Dots");
        ProgramBox.addItem("SyntenyMap");
        ProgramBox.setMaximumSize(new Dimension(300, 50));
        ProgramBox.setToolTipText("Select program to compare sequence regions");
        ProgramBox.addActionListener(new
                             SyntenyParameters_ProgramBox_actionAdapter(this));

        jPanel3.setLayout(gridBagLayout2);
        jLabelX.setText("X : ");
        jLabelY.setText("Y :");
        ProcessStatus.setForeground(Color.blue);
        ProcessStatus.setBorder(BorderFactory.createEtchedBorder());
        ProcessStatus.setOpaque(true);
        ProcessStatus.setText(" ");
        jLabel3.setAlignmentX((float) 5.5);
        jLabel3.setToolTipText("");
        jLabel3.setText("Select Program");
        jLabel3.setVerticalAlignment(SwingConstants.BOTTOM);
        jPanelProgram.setLayout(verticalFlowLayout1);

        SelectionMenu.add(jAddToX);
        TreeSelectionMenu.add(treeToX);
        TreeSelectionMenu.add(treeToY);
        MarkerSelectionMenu.add(synMap);

        jPanelMarkers.setPreferredSize(new Dimension(256, 310));
        jPanelMarkers.setToolTipText("");
        jPanelMarkers.setLayout(gridBagLayout3);

        main.setLayout(borderLayout2);

        jToolbar2.setBorder(BorderFactory.createEtchedBorder());
        jToolbar2.setMinimumSize(new Dimension(20, 25));
        jToolbar2.setPreferredSize(new Dimension(20, 25));

        beforeText.setText("10000");
        beforeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text_actionPerformed(e);
            }
        });
        afterText.setText("10000");
        afterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                text_actionPerformed(e);
            }
        });
        jLabel1.setToolTipText("downstream");
        jLabel1.setText("+");
        jLabel2.setToolTipText("Upstream");
        jLabel2.setText("-");
        jLabel4.setText("Selected Microarray Markers");
        jButtonRun.setText("Run");
        jButtonRun.addActionListener(new
                                     SyntenyParameters_ButtonRun_actionAdapter(this));
        jMarkerScrollPane.getViewport().setBackground(Color.lightGray);
        jMarkerScrollPane.setForeground(Color.lightGray);
        jMarkerScrollPane.setPreferredSize(new Dimension(150, 280));
        jMarkerScrollPane.setToolTipText("");
        jPanel1.setLayout(gridBagLayout1);
        jPanel2.setLayout(borderLayout5);
        jToolbar2.add(jLabel2, null);
        jToolbar2.add(beforeText, null);
        jToolbar2.add(jLabel1, null);
        jToolbar2.add(afterText, null);
        jInitialList.setModel(ls2);
        jInitialList.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                jInitialList_mouseReleased(e);
                super.mouseReleased(e);
            }
        });

        jInitialList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                int firstIndex = e.getFirstIndex();
                int lastIndex = e.getLastIndex();
                System.out.println("Event for indexes \n");
            }
        });

        jTree1.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                jTree_mouseReleased(e);
                super.mouseReleased(e);
            }
        });

        jTabbedPane1.add(jPanelMarkers, "Markers");
        jTabbedPane1.add(SDPM, "Genome");
        SDPM.setProgramBox(ProgramBox);
        jTabbedPane1.add(jPanelProgram, "Program");
        jTabbedPane1.add(SAP, "Annotation");
        main.add(jTabbedPane1, java.awt.BorderLayout.CENTER);
        jPanel2.add(jToolbar2, java.awt.BorderLayout.CENTER);
        jPanel2.add(jButtonRun, java.awt.BorderLayout.SOUTH);
        jPanel3.add(jLabelX, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 1, 1));
        jPanel3.add(jLabelY, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 1, 1));
        jPanelMarkers.add(jLabel4, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 1, 1));
        jPanelMarkers.add(jPanel1, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 1, 1));
        jPanelMarkers.add(jPanel2, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 1, 1));
        jPanel1.add(jMarkerScrollPane,
                    new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                           , GridBagConstraints.CENTER,
                                           GridBagConstraints.BOTH,
                                           new Insets(0, 0, 0, 0), 1, 1));
        jMarkerScrollPane.getViewport().add(jInitialList);
        jPanel1.add(jPanel3, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 1, 1));
        jPanel1.add(jScrollPane1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.SOUTHEAST, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        jScrollPane1.getViewport().add(jTree1);
        jPanelMarkers.add(ProcessStatus,
                          new GridBagConstraints(0, 0, 2, 1, 1.0, 0.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), 1, 1));

        jPanelProgram.add(jLabel3, null);
        jPanelProgram.add(ProgramBox, null);
    }

    /**************************/
    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {

        // add checking for the object ot be unique

        DefaultMutableTreeNode childNode =
            new DefaultMutableTreeNode(child);

        String[] mk_cnt = check_marker(child.toString());
        if (mk_cnt.length > 0) {
            mTreeModel.insertNodeInto(childNode, parent,
                                      parent.getChildCount());
            // now check for this marke content

            // add marker content
            for (int i = 0; i < mk_cnt.length; i++) {
                DefaultMutableTreeNode child2 =
                    new DefaultMutableTreeNode(mk_cnt[i]);
                mTreeModel.insertNodeInto(child2, childNode,
                                          childNode.getChildCount());
            }

            return childNode;
        }
        else {
            ProcessStatus.setText(
                "No sequences for this marker.");
            return null;
        }
    }

    /**************************/
    public String getProgram(){
    return ProgramBox.toString();
    }

    /**************************/
    private String[] check_marker(String mkstr) {

        FileOutputStream fout;
        String out_name = null;
        String job_id = null;
        String res_name;
        String tmp = null;
        String[] to_return = null;
        String infile = null;
        String tURL = null;
        final boolean debuging = true;

        ProcessStatus.setText(
            "Requesting marker information");

        // Forming request
        job_id = new String("Synteny_short_" +
                            Math.rint(Math.random() * 1000000));
        out_name = new String(tempDir + job_id + ".sub");
        res_name = new String(tempDir + job_id + ".res");

        String tmp1 = null;
        try {
            fout = new FileOutputStream(out_name);
            tmp = new String("JOB_ID: " + job_id + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("REQUEST_TYPE: ASK_MKR_INFO\n");
            fout.write(tmp.getBytes());

            tmp1 = new String(mkstr);
            int pr = tmp1.lastIndexOf(":");
            if (pr != -1) {
                tmp1 = new String(tmp1.substring(0, pr));
            }
            tmp = new String("MARKER: " + tmp1 + "\n");

            fout.write(tmp.getBytes());
            fout.flush();
            fout.close();
        }
        catch (IOException ioe) {
            return null;
        }

        // running request
        final String jid = new String(job_id);
        final String resn = new String(res_name);
        final String result = null;

        boolean error_flag = false;

        try {
            SoapClient sp = new SoapClient();
            infile = new String(sp.submitFile(out_name));

            String result_file = new String(
                "/users/amdecweb/jakarta-tomcat-4.1.30/bin/outputFolder/" +
                jid + ".res");
            String job_string = new String(sp.submitJob(
                "java -cp /adtera/users/pavel/synteny_remote SyntenyServerSide",
                infile, result_file));

            tURL = new String(
                "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
                ".info");
        }
        catch (IOException ioe) {
            return null;
        }
        catch (Exception ex) {
            /** @todo Handle this exception */
        }

        String ServerAnswer = null;
        while (true) {
            Delay(100);
            ServerAnswer = DAS_Retriver.GetIt(tURL);
            if (ServerAnswer != null) {
                if (ServerAnswer.indexOf("Server job done") != -1) {
                    break;
                }
            }
            else {
                ProcessStatus.setText(
                    "Waiting for reply from server");
            }
        }

        ProcessStatus.setText(
            "Done");
        // parsing the answer
        tURL = new String(
            "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
            ".res");
        tmp = DAS_Retriver.GetIt(tURL);
        int ii = tmp.indexOf("\n");
        String toparse = new String(tmp.substring(0, ii));
        int jj = Integer.parseInt(toparse);
        to_return = new String[jj];

        int kk = ii + 1;
        for (int k = 0; k < jj; k++) {
            int ll = tmp.indexOf("\n", kk);
            to_return[k] = new String(tmp.substring(kk, ll - 1));
            kk = ll + 1;
        }

        return to_return;
    }

    /**************************/
    private void Ttree_actionPerformed(ActionEvent e) {
        String str = e.getActionCommand();

        Object[] selectedMarkers = jTree1.getSelectionPaths();
        String sm = new String(selectedMarkers[0].toString());

        int ii = sm.indexOf(">");
        if (ii != -1) {
            sm = sm.substring(ii);
        } else {
            ii = sm.indexOf(",");
            int jj = sm.indexOf(":");
            sm = sm.substring(ii+1, jj);
        }

        if (selectedMarkers == null || selectedMarkers.length == 0) {
            return;
        }

        if (str.compareTo("Select as X") == 0) {
            jLabelX.setText("X: " + sm);
        }
        if (str.compareTo("Select as Y") == 0) {
            jLabelY.setText("Y: " + sm);
        }
        if (str.indexOf("Synteny Map") != -1) {
            single_marker=new String(sm);
            SyntenyMap_action(1);
        }
    }

    /**************************/
    private void Menu_actionPerformed(ActionEvent e) {
        int i, j, nc;
        String left = null, right = null;
        boolean flag = true;

        String str = e.getActionCommand();

        Object[] selectedMarkers = jInitialList.getSelectedValues();
        if (selectedMarkers == null || selectedMarkers.length == 0) {
            return;
        }

        if (str.compareTo("Add marker") == 0) {
            this.addObject(root, selectedMarkers[0]);
        }
    }

    private void jInitialList_mouseReleased(MouseEvent e) {
        if (e.isMetaDown()) {
            SelectionMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private void jTree_mouseReleased(MouseEvent e) {
        if (e.isMetaDown()) {

            Object selectedMarkers = jTree1.getSelectionPath();
            if(selectedMarkers!=null){
                String sm = new String(selectedMarkers.toString());
                int ii = sm.indexOf(">");
                if (ii != -1) {
                    TreeSelectionMenu.show(e.getComponent(), e.getX(), e.getY());
                } else {
                    /* probably the whole marker selected */
                    MarkerSelectionMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }

    void text_actionPerformed(ActionEvent e) {
        this.selectedRegionChanged = true;
    }

    /**
     * geneSelectorAction
     *
     * @param e GeneSelectorEvent
     */

    DSPanel<DSGeneMarker> markers;
    VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
    @Subscribe public void geneSelectorAction(GeneSelectorEvent e, Object publisher) {
        markers = e.getPanel();
        if (markers != null) {
            ls2.clear();
            for (int j = 0; j < markers.panels().size(); j++) {
                DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
                if (mrk.isActive())
                    for (int i = 0; i < mrk.size(); i++) {
                        if (!ls2.contains(mrk.get(i)))
                            ls2.addElement(mrk.get(i));
                    }
            }
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
     * @param e ActionEvent
     */
    void jClearAllItem_actionPerformed(ActionEvent e) {
        ls2.removeAllElements();
    }

    /**
     * Just populate SyntenyMapObject for testing purposes
     * @return SyntenyMapObject
     */
    public SyntenyMapObject PopulateSyntenyMap(){
         SyntenyMapObject smo = new SyntenyMapObject();

         SyntenyMapFragment smf1 = new SyntenyMapFragment(3, 4, 5);

         String[] nms={"name1","name2","name3","name4"};

         smf1.setUpperNames(nms);
         smf1.setLowerNames(nms);

         int[] strts={1000,9000,13000,19000};

         smf1.setUpperStarts(strts);
         smf1.setLowerStarts(strts);

         int[] ens={1500,9800,14500,20500};

         smf1.setUpperEnds(ens);
         smf1.setLowerEnds(ens);

         int[] fp={0,0,1,2,2};
         int[] sp={1,2,3,1,1};
         int[] w={1,2,1,1,1};

         smf1.setPairs(fp, sp, w);
         smf1.setUpperName("Upper");
         smf1.setLowerName("Lower");
         smf1.setLowerChromosome("chr1");
         smf1.setUpperChromosome("chr2");
         smf1.setLowerGenome("hg16");
         smf1.setUpperGenome("hg16");
         smf1.setUpperCoordinates(12345,54321);
         smf1.setLowerCoordinates(12345,54321);

         smo.addSyntenyFragment(smf1);
         smo.addSyntenyFragment(smf1);

         return smo;
    }

    void SyntenyMap_action(int type){
        // It is the test for Synteny Map !
        // SyntenyMapObject smObj = PopulateSyntenyMap();

        String job_id = new String("Synteny_" + Math.rint(Math.random() * 1000000));
        String out_name = new String(tempDir + job_id + ".sub");
        String res_name = new String(tempDir + job_id + ".res");

        // Forming the request
        try {
            // Parsing input parameters
            FileOutputStream fout = new FileOutputStream(out_name);
            String tmp;
            tmp = new String("JOB_ID: " + job_id + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("REQUEST_TYPE: M_SMAP\n");
            fout.write(tmp.getBytes());

            tmp = new String("PROGRAM: " + (String)ProgramBox.getSelectedItem() + "\n");
            fout.write(tmp.getBytes());

            if(type == 2){
                String infstr = new String(jLabelX.getText());
                int ii = infstr.indexOf(">");
                int jj = infstr.indexOf(":", ii + 1);
                String x_marker = new String(infstr.substring(ii+1, jj));

                tmp = new String("MARKER1: " + x_marker + "\n");
                fout.write(tmp.getBytes());

                infstr = new String(jLabelY.getText());
                ii = infstr.indexOf(">");
                jj = infstr.indexOf(":", ii + 1);
                String y_marker = new String(infstr.substring(ii+1, jj));

                tmp = new String("MARKER2: " + y_marker + "\n");
                fout.write(tmp.getBytes());
            }
            if(type == 1){
                tmp = new String("MARKER1:" + single_marker + "\n");
                fout.write(tmp.getBytes());
                tmp = new String("MARKER2: " + "NONE\n");
                fout.write(tmp.getBytes());
            }

            tmp = new String("ZONE: " + Integer.parseInt(beforeText.getText()) + "\n");
            fout.write(tmp.getBytes());

            fout.flush();
            fout.close();
        }
        catch (IOException ioe) {
            return;
        }
        // here goes request to the server
        final String outf = new String(out_name);
        final String jid = new String(job_id);
        final String resn = new String(res_name);

        Thread t = new Thread() {
            public void run() {
                jButtonRun.setBackground(Color.gray);
                ProcessStatus.setText("Submitting job to remote server");
                boolean error_flag = false;

                try {
                    SoapClient sp = new SoapClient();
                    String infile = new String(sp.submitFile(outf));

                    String result_file = new String(
                        "/users/amdecweb/jakarta-tomcat-4.1.30/bin/outputFolder/" +
                        jid + ".res");
                    String job_string = new String(sp.submitJob(
                        "java -cp /adtera/users/pavel/synteny_remote/ SyntenyServerSide",
                        infile, result_file));

                    String tURL = new String(
                        "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
                        ".info");

                    ProcessStatus.setText(
                        "Waiting for reply from remote server");
                    String ServerAnswer = null;
                    while (true) {
                        Delay(100);
                        ServerAnswer = DAS_Retriver.GetIt(tURL);
                        if (ServerAnswer != null) {
                            ProcessStatus.setText(ServerAnswer);
                            if (ServerAnswer.indexOf("Server job done") != -1) {
                                break;
                            }
                        }
                        else {
                            ProcessStatus.setText(
                                "Waiting for reply from server");
                        }
                    }

                    tURL = new String(
                        "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
                        ".res");

                    ProcessStatus.setText("Retriving results from server");
                }
                catch (Exception ee) {
                    System.err.println(ee);
                    return;
                }
                ProcessStatus.setText("Parsing");
            }

        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

        // here goes drawing
        //SyntenyMapViewWidget.smrepaint(smObj);
        //SyntenyMapViewWidget smw=new SyntenyMapViewWidget();
    return;
    }

    /*********************************************************/
    void ButtonRun_actionPerformed(ActionEvent e) {
        int fx, tx, fy, ty, i, j;


        FileOutputStream fout;
        String out_name = null;
        String job_id = null;
        String res_name;
        final boolean debuging = true;

        job_id = new String("Synteny_" + Math.rint(Math.random() * 1000000));
        out_name = new String(tempDir + job_id + ".sub");
        res_name = new String(tempDir + job_id + ".res");

        if(((String)ProgramBox.getSelectedItem()).indexOf("SyntenyMap") != -1){
            SyntenyMap_action(2);
        return;
        }

        // Parsing X information
        String infstr = new String(jLabelX.getText());
        int ii = infstr.indexOf(":");
        int jj = infstr.indexOf(":", ii + 1);
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        String genomex = new String(infstr.substring(ii + 1, jj));
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        String chromX = new String(infstr.substring(ii + 1, jj));
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        String tmp1 = new String(infstr.substring(ii + 1, jj));
        String tmp2 = new String(beforeText.getText());

        fx = Integer.parseInt(infstr.substring(ii + 1, jj)) -
            Integer.parseInt(beforeText.getText());
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        tx = Integer.parseInt(infstr.substring(ii + 1, jj)) +
            Integer.parseInt(afterText.getText()); ;

        // Parsing Y information
        infstr = new String(jLabelY.getText());
        ii = infstr.indexOf(":");
        jj = infstr.indexOf(":", ii + 1);
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        String genomey = new String(infstr.substring(ii + 1, jj));
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        String chromY = new String(infstr.substring(ii + 1, jj));
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        fy = Integer.parseInt(infstr.substring(ii + 1, jj)) -
            Integer.parseInt(beforeText.getText());
        ii = jj;
        jj = infstr.indexOf(":", ii + 1);
        ty = Integer.parseInt(infstr.substring(ii + 1, jj)) +
            Integer.parseInt(afterText.getText()); ;

        try {
            // Parsing input parameters
            fout = new FileOutputStream(out_name);
            String tmp;
            tmp = new String("JOB_ID: " + job_id + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("REQUEST_TYPE: M_DOTMATRIX\n");
            fout.write(tmp.getBytes());

            tmp = new String("PROGRAM: "+(String)ProgramBox.getSelectedItem()+"\n");
            fout.write(tmp.getBytes());

            tmp = new String("GENOME1: " + genomex + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("CHR1: " + chromX +
                             "\n");
            fout.write(tmp.getBytes());
            tmp = new String("FROM1: " + fx + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("TO1: " + tx + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("GENOME2: " + genomey + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("CHR2: " + chromY +
                             "\n");
            fout.write(tmp.getBytes());
            tmp = new String("FROM2: " + fy + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("TO2: " + ty + "\n");
            fout.write(tmp.getBytes());

            fout.flush();
            fout.close();
        }
        catch (IOException ioe) {
            return;
        }

        final String outf = new String(out_name);
        final String jid = new String(job_id);
        final String resn = new String(res_name);
        final int f_x = fx;
        final int f_y = fy;
        final int t_x = tx;
        final int t_y = ty;

        Thread t = new Thread() {
            public void run() {
                jButtonRun.setBackground(Color.gray);
                ProcessStatus.setText("Submitting job to remote server");
                boolean error_flag = false;

                try {
                    SoapClient sp = new SoapClient();
                    String infile = new String(sp.submitFile(outf));

                    String result_file = new String(
                        "/users/amdecweb/jakarta-tomcat-4.1.30/bin/outputFolder/" +
                        jid + ".res");
                    String job_string = new String(sp.submitJob(
                        "java -cp /adtera/users/pavel/synteny_remote/ SyntenyServerSide",
                        infile, result_file));

                    String tURL = new String(
                        "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
                        ".info");

                    ProcessStatus.setText(
                        "Waiting for reply from remote server");
                    String ServerAnswer = null;
                    while (true) {
                        Delay(100);
                        ServerAnswer = DAS_Retriver.GetIt(tURL);
                        if (ServerAnswer != null) {
                            ProcessStatus.setText(ServerAnswer);
                            if (ServerAnswer.indexOf("Server job done") != -1) {
                                break;
                            }
                        }
                        else {
                            ProcessStatus.setText(
                                "Waiting for reply from server");
                        }
                    }

                    tURL = new String(
                        "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
                        ".res");

                    ProcessStatus.setText("Retriving results from server");
                    if (DAS_Retriver.GetItToFile(tURL, resn) == false) {
                        error_flag = true;
                    }
                }
                catch (Exception ee) {
                    System.err.println(ee);
                    return;
                }

                ProcessStatus.setText("Parsing");
                if (CheckFileIntegrity(resn) == false) {
                    error_flag = true;
                }

                if (error_flag) {
                    ProcessStatus.setText("Server error! Please try again.");
                }
                else {
                    SPList.addAndDisplay(resn, f_x, t_x, f_y, t_y);
                    jButtonRun.setBackground(Color.white);
                }
            }

        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    /*****************************************************/
    private boolean CheckFileIntegrity(String Fil) {

        File f = new File(Fil);
        int size = (int) f.length();
        int len = 0;
        String datastr = null;

        if (size < 20) {
            return false;
        }

        byte[] data = new byte[size];

        try {
            FileInputStream fis = new FileInputStream(Fil);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            dis.read(data);
            datastr = new String(data);
            dis.close();
        }
        catch (IOException iox) {
            System.out.println("File read error...");
            iox.printStackTrace();
            return false;
        }

        if (datastr.indexOf("/# Start of DOTS output #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# End of DOTS output #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# Start of Annotation 1 #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# End of Annotation 1 #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# Start of Annotation 2 #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# End of Annotation 2 #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# Start of PFP output 1 #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# End PFP output 1 #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# Start of PFP output 2 #/") == -1) {
            return false;
        }
        if (datastr.indexOf("/# End PFP output 2 #/") == -1) {
            return false;
        }

        return true;
    }

    /*********************************************************/
    public static void Delay(int n) {
        long tm;
        long tm1;
        Calendar cal = Calendar.getInstance();

        tm = cal.getTimeInMillis();

        tm1 = tm + n;
        while (tm < tm1) {
            Calendar cal1 = Calendar.getInstance();
            tm = cal1.getTimeInMillis();
        }
    }

    void ProgramBox_actionPerformed(ActionEvent e) {
        SDPM.setCurrentProgram((String)ProgramBox.getSelectedItem());
    }
}

class SyntenyParameters_ButtonRun_actionAdapter
    implements java.awt.event.ActionListener {
    SyntenyParameters adaptee;

    SyntenyParameters_ButtonRun_actionAdapter(SyntenyParameters adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ButtonRun_actionPerformed(e);
    }
}

class MyTreeModelListener
    implements TreeModelListener {
    public void treeNodesChanged(TreeModelEvent e) {
        DefaultMutableTreeNode node;
        node = (DefaultMutableTreeNode)
            (e.getTreePath().getLastPathComponent());
        /*
         * If the event lists children, then the changed
         * node is the child of the node we've already
         * gotten.  Otherwise, the changed node and the
         * specified node are the same.
         */
        try {
            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode)
                (node.getChildAt(index));
        }
        catch (NullPointerException exc) {}
    }

    public void treeNodesInserted(TreeModelEvent e) {
    }

    public void treeNodesRemoved(TreeModelEvent e) {
    }

    public void treeStructureChanged(TreeModelEvent e) {
    }
}

class SyntenyParameters_ProgramBox_actionAdapter
    implements java.awt.event.ActionListener {
    SyntenyParameters adaptee;

    SyntenyParameters_ProgramBox_actionAdapter(SyntenyParameters adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ProgramBox_actionPerformed(e);
    }
}
