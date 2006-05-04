package org.geworkbench.components.alignment.synteny;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.builtin.projects.ProjectTreeNode;
import org.geworkbench.components.alignment.synteny.DAS_Retriver;
import org.geworkbench.components.alignment.synteny.SyntenyPresentationsList;
import org.geworkbench.components.alignment.synteny.SyntenyAnnotationParameters;
import org.geworkbench.components.alignment.synteny.SynMapPresentationList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.events.EventSource;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.SyntenyEvent;
import org.geworkbench.util.session.SoapClient;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Calendar;
import java.util.HashMap;

// import org.geworkbench.components.alignment.synteny.SynMapParser;


/**
 * <p>Title: Bioworks</p>
 *
 * <p>Description: Synteny Parameters Panel</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

@AcceptTypes({DSMicroarraySet.class}) public class SyntenyParameters extends EventSource implements VisualPlugin {
        private HashMap listeners = new HashMap();
        boolean cancel_flag = false;
        boolean job_active = false;
        boolean selectedRegionChanged = false;
        private SyntenyAnnotationParameters SAP = new SyntenyAnnotationParameters();
        private GenomePositionSubPanel GPos;
        private JList RegionsList = new JList();
        private DefaultListModel RegionsListModel = new DefaultListModel();

        private GridBagLayout gridBagLayout1 = new GridBagLayout();
        private BorderLayout borderLayout5 = new BorderLayout();

        //Panels and Panes
        private JPanel JPanelRunSelected = new JPanel();
        private BorderLayout borderLayout3 = new BorderLayout();
        private JLabel jLabel5 = new JLabel();
        private JPanel jPanel5 = new JPanel();
        private BorderLayout borderLayout4 = new BorderLayout();
        private JPanel JPanelInfo = new JPanel();
        private BorderLayout borderLayout2 = new BorderLayout();
        private JPanel main = new JPanel();
        private JTabbedPane jTabbedPane1 = new JTabbedPane();
        private JPanel jPanelMarkers = new JPanel();
        private JPanel jPanelProgram = new JPanel();
        private JPanel jPanel1 = new JPanel();
        private JPanel jPanel2 = new JPanel();
        private JScrollPane jMarkerScrollPane = new JScrollPane();
        private JToolBar jToolbar2 = new JToolBar(); //    private SequencePatternDisplayPanel seqDisPanel = new
        private JPopupMenu SelectionMenu = new JPopupMenu();
        private JPopupMenu TreeSelectionMenu = new JPopupMenu();
        private JPopupMenu MarkerSelectionMenu = new JPopupMenu();
        private JPopupMenu XYMenu = new JPopupMenu();
        GridBagLayout gridBagLayout4 = new GridBagLayout();
        GridBagLayout gridBagLayout5 = new GridBagLayout();
        JLabel jLabelProgram = new JLabel();
        JScrollPane jRegionsScrollPane = new JScrollPane();
        TitledBorder titledBorder2 = new TitledBorder("");
        JPanel jPanelButtons = new JPanel();
        BorderLayout borderLayout6 = new BorderLayout();
        JButton jButtonStopIt = new JButton();
        JComboBox SynMapParametersComboBox = new JComboBox();
        private JTextField beforeText = new JTextField();
        private JTextField afterText = new JTextField();
        private JLabel jLabel1 = new JLabel();
        private JLabel jLabel2 = new JLabel();
        private JLabel jLabel4 = new JLabel();
        private DefaultListModel ls2 = new DefaultListModel();
        private JList jInitialList = new JList();
        private JButton jButtonRun = new JButton();
        private JMenuItem jAddToX = new JMenuItem();
        private JMenuItem treeToX = new JMenuItem();
        private JMenuItem treeToY = new JMenuItem();
        private JMenuItem treeToSelected = new JMenuItem();
        private JMenuItem ToX = new JMenuItem();
        private JMenuItem ToY = new JMenuItem();
        private JMenuItem ShowAnnot = new JMenuItem();
        private JMenuItem Delete = new JMenuItem();
        private JLabel ProcessStatus = new JLabel();
        public SyntenyPresentationsList SPList = null;
        public SynMapPresentationList SMPList = null;
        private JPanel jPanel3 = new JPanel();
        private GridBagLayout gridBagLayout2 = new GridBagLayout();
        private JLabel jLabelX = new JLabel();
        private JLabel jLabelY = new JLabel();
        private String tempDir = System.getProperty("temporary.files.directory");
        public JComboBox ProgramBox = new JComboBox();
        private GridBagLayout gridBagLayout3 = new GridBagLayout();
        private JScrollPane jScrollPane1 = new JScrollPane();

        private DefaultMutableTreeNode root = new ProjectTreeNode(
                "Select regions from");
        private DefaultTreeModel mTreeModel = new DefaultTreeModel(root);
        private JTree jTree1 = null;
        private JPanel GenomePosPanel = new JPanel();
        private JButton addButton = new JButton();
        public SyntenyParameters() {
            try {
                jbInit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        void jbInit() throws Exception {

            ActionListener treeListener = null;
            ActionListener listener = null;
            ActionListener regionsListListener = null;

            jAddToX.setText("Add marker");
            treeToX.setText("Select as X");
            treeToY.setText("Select as Y");
            treeToSelected.setText("Add to selected");

            regionsListListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    regionsList_actionPerformed(e);
                }
            };

            SynMapParametersComboBox.addItem("Complete search for markers");
            SynMapParametersComboBox.addItem("Search for specifyed regions only");
            SynMapParametersComboBox.hide();

            ToX.setText("Select as X");
            ToY.setText("Select as Y");
            Delete.setText("Remove");
            ShowAnnot.setText("Show Annotation");
            ToX.addActionListener(regionsListListener);
            ToY.addActionListener(regionsListListener);
            ShowAnnot.addActionListener(regionsListListener);
            Delete.addActionListener(regionsListListener);

            jLabelProgram.setBackground(Color.white);
            jLabelProgram.setBorder(BorderFactory.createEtchedBorder());
            jLabelProgram.setOpaque(true);
            jLabelX.setBackground(Color.white);
            jLabelX.setOpaque(true);
            jLabelY.setBackground(Color.white);
            jLabelY.setOpaque(true);
            JPanelInfo.setBorder(titledBorder2);
            jPanelButtons.setLayout(borderLayout6);
            jButtonStopIt.setText("Cancel");
            jPanelProgram.setLayout(borderLayout1);
            jButtonAnnotationRedraw.setToolTipText("");
            jButtonAnnotationRedraw.setText("Redraw annotation");
            jButtonAnnotationRedraw.addActionListener(new
                                                      SyntenyParameters_redrawButton_actionAdapter(this));
            XYMenu.add(ToX);
            XYMenu.add(ToY);
            XYMenu.add(ShowAnnot);
            XYMenu.add(Delete);

            SPList = new SyntenyPresentationsList();
            SMPList = new SynMapPresentationList();
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
            treeToSelected.addActionListener(treeListener);

            ProgramBox.addItem("MUMmer");
            ProgramBox.addItem("Dots");
            ProgramBox.addItem("SyntenyMap");
            ProgramBox.setMaximumSize(new Dimension(300, 50));
            ProgramBox.setToolTipText("Select program to compare sequence regions");
            ProgramBox.addActionListener(new
                                         SyntenyParameters_ProgramBox_actionAdapter(this));

            jLabelProgram.setText("  Program/Method :  " +
                                  (String) ProgramBox.getSelectedItem());

            jPanel3.setLayout(gridBagLayout2);
            jLabelX.setText("  X : ");
            jLabelY.setToolTipText("");
            jLabelY.setText("  Y :");
            ProcessStatus.setForeground(Color.blue);
            ProcessStatus.setBorder(BorderFactory.createEtchedBorder());
            ProcessStatus.setOpaque(true);
            ProcessStatus.setText(" ");
            JPanelRunSelected.setMinimumSize(new Dimension(100, 300));
            JPanelRunSelected.setPreferredSize(new Dimension(200, 400));
            JPanelRunSelected.setLayout(borderLayout3);
            main.setPreferredSize(new Dimension(500, 400));
            jTabbedPane1.setMinimumSize(new Dimension(100, 300));
            jTabbedPane1.setPreferredSize(new Dimension(200, 400));
            jLabel5.setToolTipText("");
            jLabel5.setText(" Selected regions");
            jButtonStopIt.addActionListener(new
                                            SyntenyParameters_cancelButton_actionAdapter(this));
            jButtonStopIt.setForeground(Color.white);

            jButtonRun.setText("R U N");
            jButtonRun.addActionListener(new
                                         SyntenyParameters_runButton_actionAdapter(this));
            jPanel5.setLayout(borderLayout4);

            RegionsList.setBackground(Color.white);
            RegionsList.setBorder(BorderFactory.createLineBorder(Color.black));
            RegionsList.setOpaque(true);
            RegionsList.setModel(RegionsListModel);
            RegionsList.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    RegionsList_mouseReleased(e);
                    super.mouseReleased(e);
                }
            });

            JPanelInfo.setLayout(borderLayout2);
            addButton.setToolTipText("");
            addButton.setText("Add to selected");
            addButton.addActionListener(new
                                        SyntenyParameters_addButton_actionAdapter(this)); // Forming select boxes

            SelectionMenu.add(jAddToX);
            TreeSelectionMenu.add(treeToSelected);

            jPanelMarkers.setPreferredSize(new Dimension(256, 310));
            jPanelMarkers.setToolTipText("");
            jPanelMarkers.setLayout(gridBagLayout3);

            GPos = new GenomePositionSubPanel();
            GPos.setMinimumSize(new Dimension(100, 100));
            GPos.setPreferredSize(new Dimension(90, 70));

            GenomePosPanel.setLayout(gridBagLayout4);

            GPos.setPosFrom(97750000);
            GPos.setPosTo(97754800);
            GPos.setChromosome(2);
            GPos.setGenome(2);

            main.setLayout(gridBagLayout5);

            jToolbar2.setBorder(BorderFactory.createEtchedBorder());
            jToolbar2.setMinimumSize(new Dimension(20, 25));
            jToolbar2.setPreferredSize(new Dimension(20, 25));

            beforeText.setText("1000");
            beforeText.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    text_actionPerformed(e);
                }
            });
            afterText.setText("1000");
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
            jTabbedPane1.add(jPanelProgram, "Program");
            jTabbedPane1.add(SAP, "Annotation");
            SAP.add(jButtonAnnotationRedraw, java.awt.BorderLayout.SOUTH);
            jTabbedPane1.add(GenomePosPanel, "Genome");

            jPanel2.add(jToolbar2, java.awt.BorderLayout.CENTER);
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
            JPanelRunSelected.add(jLabel5, java.awt.BorderLayout.NORTH);
            JPanelRunSelected.add(jPanel5, java.awt.BorderLayout.SOUTH);
            JPanelInfo.add(jLabelX, java.awt.BorderLayout.NORTH);
            GenomePosPanel.add(GPos, new GridBagConstraints(0, 0, 1, 2, 1.0, 2.0
                    , GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                    new Insets(5, 0, 5, 0), 0, 0));
            jPanelMarkers.add(jLabel4, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 1, 1));
            jPanelMarkers.add(jPanel1, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 1, 1));
            jPanelMarkers.add(jPanel2, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 1, 1));
            main.add(jTabbedPane1, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            main.add(JPanelRunSelected, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
                    , GridBagConstraints.CENTER,
                    GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            JPanelInfo.add(jLabelY, java.awt.BorderLayout.CENTER);
            JPanelInfo.add(jLabelProgram, java.awt.BorderLayout.SOUTH);
            JPanelRunSelected.add(jRegionsScrollPane, java.awt.BorderLayout.CENTER);
            jRegionsScrollPane.getViewport().add(RegionsList);
            main.add(ProcessStatus, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
                    , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            jPanel5.add(JPanelInfo, java.awt.BorderLayout.CENTER);
            jPanel5.add(jPanelButtons, java.awt.BorderLayout.SOUTH);
            jPanelButtons.add(jButtonRun, java.awt.BorderLayout.CENTER);
            jPanelButtons.add(jButtonStopIt, java.awt.BorderLayout.EAST);
            GenomePosPanel.add(addButton,
                               new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
                                                      , GridBagConstraints.CENTER,
                                                      GridBagConstraints.HORIZONTAL,
                                                      new Insets(0, 0, 0, 0), 0, 0));
            jPanel4.add(SynMapParametersComboBox);
            jPanelProgram.add(jPanel4, java.awt.BorderLayout.CENTER);
            jPanelProgram.add(ProgramBox, java.awt.BorderLayout.NORTH);
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
            } else {
                ProcessStatus.setText(
                        "No sequences for this marker.");
                return null;
            }
        }

        /**************************/
        public String getProgram() {
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
            job_id = "Synteny_short_" +
                                Math.rint(Math.random() * 1000000);
            out_name = tempDir + job_id + ".sub";
            res_name = tempDir + job_id + ".res";

            String tmp1 = null;
            try {
                fout = new FileOutputStream(out_name);
                tmp = "JOB_ID: " + job_id + "\n";
                fout.write(tmp.getBytes());
                tmp = "REQUEST_TYPE: ASK_MKR_INFO\n";
                fout.write(tmp.getBytes());

                tmp1 = mkstr;
                int pr = tmp1.lastIndexOf(":");
                if (pr != -1) {
                    tmp1 = tmp1.substring(0, pr);
                }
                tmp = "MARKER: " + tmp1 + "\n";

                fout.write(tmp.getBytes());
                fout.flush();
                fout.close();
            } catch (IOException ioe) {
                return null;
            }

            // running request
            final String jid = job_id;
            final String resn = res_name;
            final String result = null;

            boolean error_flag = false;

            try {
                SoapClient sp = new SoapClient();
                infile = sp.submitFile(out_name);

                String result_file =
                        "/users/amdecweb/jakarta-tomcat-4.1.30/bin/outputFolder/" +
                        jid + ".res";
                String job_string = sp.submitJob(
                        "java -cp /adtera/users/pavel/synteny_remote SyntenyServerSide",
                        infile, result_file);

                tURL = "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
                        ".info";
            } catch (IOException ioe) {
                return null;
            } catch (Exception ex) {
                /**
                 *
                 */
            }

            String ServerAnswer = null;
            while (true) {
                Delay(500);
                ServerAnswer = DAS_Retriver.GetItSilent(tURL);
                if (ServerAnswer != null) {
                    if (ServerAnswer.indexOf("Server job done") != -1) {
                        break;
                    }
                } else {
                    ProcessStatus.setText(
                            "Waiting for reply from server");
                }
            }

            ProcessStatus.setText(
                    "Done");
            // parsing the answer
            tURL = "http://amdec-bioinfo.cu-genome.org/html/temp/" + jid +
                    ".res";
            tmp = DAS_Retriver.GetIt(tURL);
            int ii = tmp.indexOf("\n");
            String toparse = tmp.substring(0, ii);
            int jj = Integer.parseInt(toparse);
            to_return = new String[jj];

            int kk = ii + 1;
            for (int k = 0; k < jj; k++) {
                int ll = tmp.indexOf("\n", kk);
                to_return[k] = tmp.substring(kk, ll - 1);
                kk = ll + 1;
            }

            return to_return;
        }

        /**************************/
        private void regionsList_actionPerformed(ActionEvent e) {
            String str = e.getActionCommand();

            Object[] selectedMarkers = RegionsList.getSelectedValues();
            if (selectedMarkers == null || selectedMarkers.length == 0) {
                return;
            }

            String sm = selectedMarkers[0].toString();

            int ii = sm.indexOf(">");
            if (ii != -1) {
                sm = sm.substring(ii);
            } else {
                ii = sm.indexOf(",");
                int jj = sm.indexOf(":");
                sm = sm.substring(ii + 1, jj);
            }

            if (selectedMarkers == null || selectedMarkers.length == 0) {
                return;
            }

            if (str.compareTo("Select as X") == 0) {
                jLabelX.setText("  X: " + sm);
            }
            if (str.compareTo("Select as Y") == 0) {
                jLabelY.setText("  Y: " + sm);
            }
        }

        public void addToRegionsListModel(String sm) {
            for (int i = 0; i < RegionsListModel.getSize(); i++) {
                if (sm.indexOf((String) RegionsListModel.elementAt(i)) == 0) {
                    return;
                }
            }

            RegionsListModel.add(RegionsListModel.getSize(), sm);
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
                sm = sm.substring(ii + 1, jj);
            }

            if (selectedMarkers == null || selectedMarkers.length == 0) {
                return;
            }

            if (str.compareTo("Add to selected") == 0) {
                String[] sm_comps = sm.split(":");
                int fr = (int) (Integer.parseInt(sm_comps[3]));
                fr = fr - (int) (Integer.parseInt(beforeText.getText()));
                int tt = (int) (Integer.parseInt(sm_comps[4])) +
                         (int) (Integer.parseInt(afterText.getText()));
                sm = new String(sm_comps[0] + ":" + sm_comps[1] + ":" + sm_comps[2] +
                                ":" + fr + ":" + tt + ":" + sm_comps[5]);
                addToRegionsListModel(sm);
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

        private void RegionsList_mouseReleased(MouseEvent e) {
            if (e.isMetaDown()) {
                XYMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }

        private void jTree_mouseReleased(MouseEvent e) {
            if (e.isMetaDown()) {

                Object selectedMarkers = jTree1.getSelectionPath();
                if (selectedMarkers != null) {
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

        DSPanel<DSGeneMarker> markers;
        JPanel jPanel4 = new JPanel();
        BorderLayout borderLayout1 = new BorderLayout();
        JButton jButtonAnnotationRedraw = new JButton();

        @Subscribe public void geneSelectorAction(GeneSelectorEvent e,
                                                  Object publisher) {
            markers = e.getPanel();
            if (markers != null) {
                ls2.clear();
                for (int j = 0; j < markers.panels().size(); j++) {
                    DSPanel<DSGeneMarker> mrk = markers.panels().get(j);
                    if (mrk.isActive()) {
                        for (int i = 0; i < mrk.size(); i++) {
                            if (!ls2.contains(mrk.get(i))) {
                                ls2.addElement(mrk.get(i));
                            }
                        }
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

        void CancelButtonAction(String jid) {
            FileOutputStream fout;
            String out_name = new String(tempDir + "cancel_request_" + jid);

            job_active = false;
            // forming file with request
            try {
                fout = new FileOutputStream(out_name);
                String tmp;
                tmp = new String("CANCEL: " + jid + "\n");
                fout.write(tmp.getBytes());
                fout.close();
            } catch (IOException ioe) {
                return;
            }

            // Submitting the request to the server
            final String outf = new String(out_name);
            Thread t = new Thread() {
                public void run() {
                    ProcessStatus.setText("Cancelling...");
                    try {
                        SoapClient sp = new SoapClient();
                        String infile = new String(sp.submitFile(outf));
                        String job_string = new String(sp.submitJob(
                                "java -cp /adtera/users/pavel/synteny_remote/ SyntenyServerSide",
                                infile, "nofile"));
                    } catch (Exception ee) {
                        System.err.println(ee);
                        return;
                    }
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            ProcessStatus.setText("Job cancelled.");
        }

        /**
         * Just populate SyntenyMapObject for testing purposes
         * @return SyntenyMapObject
         */

        /*********************************************************/
        void ButtonRun_actionPerformed(ActionEvent e) {
            int fx, tx, fy, ty, i, j, ux, dx, uy, dy;

            /******************************************************************************
            if (((String) ProgramBox.getSelectedItem()).indexOf("SyntenyMap") != -1) {
//            String rr = tempDir + "Synteny_838728.0.res";
                String rr = tempDir + "Synteny_961301.0.res";
                job_active = false;
                int ff_x = 46224505;
                int tt_x = 46638695;
                int ff_y = 30924275;
                int tt_y = 31342378;

                sendEvent("SM\t" + rr + "\t" + ff_x + "\t" + tt_x + "\t" + ff_y +
                          "\t" + tt_y);
                jButtonRun.setBackground(Color.white);
                return;
            }
            /******************************************************************************/

            if (job_active) {
                JOptionPane.showMessageDialog
                        (null, "Previous job is active!", "Results",
                         JOptionPane.ERROR_MESSAGE);
                return;
            }
            job_active = true;
            if (((jLabelX.getText()).indexOf(">") == -1) ||
                ((jLabelY.getText()).indexOf(">") == -1)) {
                JOptionPane.showMessageDialog
                        (null, "Invalid X or Y genomic region!", "Results",
                         JOptionPane.ERROR_MESSAGE);
                job_active = false;
                return;
            }

            FileOutputStream fout;
            String out_name = null;
            String job_id = null;
            String res_name;

            job_id = "Synteny_" + Math.rint(Math.random() * 1000000);
            out_name = tempDir + job_id + ".sub";
            res_name = tempDir + job_id + ".res";

            String req_type = "M_DOTMATRIX";
            if (((String) ProgramBox.getSelectedItem()).indexOf("SyntenyMap") != -1) {
                req_type = "M_SMAP";
            }

            // Parsing X information
            String[] infstr = ((String) jLabelX.getText()).split(":");
            String sourcex = infstr[1].substring(2);
            if ((((String) ProgramBox.getSelectedItem()).indexOf("SyntenyMap") !=
                 -1) && sourcex.equalsIgnoreCase("genomic")) {
                JOptionPane.showMessageDialog
                        (null, "Currently SyntenyMap program can't accept genomic regions defined by genome positions!",
                         "Results",
                         JOptionPane.ERROR_MESSAGE);
                return;
            }

            String genomex = infstr[2];
            String chromX = infstr[3];
            dx = Integer.parseInt(beforeText.getText());
            ux = Integer.parseInt(afterText.getText());
            fx = Integer.parseInt(infstr[4]);// - dx;
            tx = Integer.parseInt(infstr[5]);// + ux;

            // Parsing Y information
            infstr = (jLabelY.getText()).split(":");
            String sourcey = infstr[1].substring(2);
            if ((((String) ProgramBox.getSelectedItem()).indexOf("SyntenyMap") !=
                 -1) && sourcey.equalsIgnoreCase("genomic")) {
                JOptionPane.showMessageDialog
                        (null, "Currently SyntenyMap program can't accept genomic regions defined by genome positions!",
                         "Results",
                         JOptionPane.ERROR_MESSAGE);
                return;
            }
            String genomey = infstr[2];
            String chromY = infstr[3];

            dy = Integer.parseInt(beforeText.getText());

            uy = Integer.parseInt(afterText.getText());
            fy = Integer.parseInt(infstr[4]);// - dy;
            ty = Integer.parseInt(infstr[5]);// + uy;

            try {
                fout = new FileOutputStream(out_name);
                String tmp;

                tmp = new String("JOB_ID: " + job_id + "\n");
                fout.write(tmp.getBytes());

                tmp = new String("REQUEST_TYPE: " + req_type + "\n");
                fout.write(tmp.getBytes());

                tmp = new String("PROGRAM: " + (String) ProgramBox.getSelectedItem() +
                                 "\n");
                fout.write(tmp.getBytes());

                if (((String) ProgramBox.getSelectedItem()).indexOf("SyntenyMap") !=
                    -1) {
                    tmp = new String("SYN_PARAME: " +
                                     (String) SynMapParametersComboBox.
                                     getSelectedItem() + "\n");
                }
                fout.write(tmp.getBytes());

                tmp = new String("SOURCE1: " + sourcex + "\n");
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
                tmp = new String("UPSTREAM1: " + ux + "\n");
                fout.write(tmp.getBytes());
                tmp = new String("DOWNSTREAM1: " + dx + "\n");
                fout.write(tmp.getBytes());

                tmp = new String("SOURCE2: " + sourcey + "\n");
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
                tmp = new String("UPSTREAM2: " + uy + "\n");
                fout.write(tmp.getBytes());
                tmp = new String("DOWNSTREAM2: " + dy + "\n");
                fout.write(tmp.getBytes());

                fout.flush();
                fout.close();
            } catch (IOException ioe) {
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
                    jButtonStopIt.setForeground(Color.black);
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
//                            "java -cp /adtera/users/pavel/synteny_remote/ SSSTest",
                                infile, result_file));

                        String tURL = new String(
                                "http://amdec-bioinfo.cu-genome.org/html/temp/" +
                                jid +
                                ".info");

                        ProcessStatus.setText(
                                "Waiting for reply from remote server");
                        String ServerAnswer = null;
                        while (true) {
                            Delay(500);
                            if (cancel_flag) {
                                CancelButtonAction(jid);
                                break;
                            }
                            ServerAnswer = DAS_Retriver.GetItSilent(tURL);
                            if (ServerAnswer != null) {
                                ProcessStatus.setText(ServerAnswer);
                                if (ServerAnswer.indexOf("Server job done") != -1) {
                                    break;
                                }
                            } else {
                                ProcessStatus.setText(
                                        "Waiting for reply from server");
                            }
                        }

                        if (cancel_flag == false) {
                            tURL = new String(
                                    "http://amdec-bioinfo.cu-genome.org/html/temp/" +
                                    jid +
                                    ".res");

                            ProcessStatus.setText("Retriving results from server");
                            if (DAS_Retriver.GetItToFile(tURL, resn) == false) {
                                error_flag = true;
                            }
                        }
                    } catch (Exception ee) {
                        System.err.println(ee);
                        return;
                    }

                    if (cancel_flag == false) {
                        ProcessStatus.setText("Parsing");
                        if (((String) ProgramBox.getSelectedItem()).indexOf(
                                "SyntenyMap") != -1) {
                            if (CheckSynMapFileIntegrity(resn) == false) {
                                error_flag = true;
                            }
                            if (error_flag) {
                                job_active = false;
                                ProcessStatus.setText(
                                        "Server error! Please try again.");
                                jButtonRun.setBackground(Color.white);
                            } else {
                                ProcessStatus.setText("Done");
                                job_active = false;
//                            SMPList.addAndDisplay(resn, f_x, t_x, f_y, t_y);
                                sendEvent("SM\t" + resn + "\t" + f_x + "\t" + t_x +
                                          "\t" + f_y + "\t" + t_y);
                                jButtonRun.setBackground(Color.white);
                            }
                        } else {
                            if (CheckFileIntegrity(resn) == false) {
                                error_flag = true;
                            }
                            if (error_flag) {
                                ProcessStatus.setText(
                                        "Server error! Please try again.");
                                job_active = false;
                                jButtonRun.setBackground(Color.white);
                            } else {
                                ProcessStatus.setText("Done");
                                job_active = false;
                                sendEvent("DM\t" + resn + "\t" + f_x + "\t" + t_x +
                                          "\t" + f_y + "\t" + t_y);
                                jButtonRun.setBackground(Color.white);
                            }
                        }
                    } else {
                        ProcessStatus.setText("Process cancelled");
                        jButtonRun.setBackground(Color.white);
                        jButtonStopIt.setForeground(Color.white);
                        cancel_flag = false;
                    }
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }

        @Publish public SyntenyEvent publishSyntenyEvent(SyntenyEvent se) {
            return se;
        }

        void sendEvent(String s) {
            publishSyntenyEvent(new SyntenyEvent(s));

        }

        void redrawButton_actionPerformed(ActionEvent e) {
            publishSyntenyEvent(new SyntenyEvent("AC\t" + SAP.getAnnoString()));
        }

        void addButton_actionPerformed(ActionEvent e) {
            addToRegionsListModel(">genomic:" + GPos.getGenome() + ":" +
                                  GPos.getChromosome() + ":" + GPos.getValueFrom() +
                                  ":" + GPos.getValueTo() + ":");
        }

        private boolean CheckSynMapFileIntegrity(String Fil) {

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
            } catch (IOException iox) {
                System.out.println("File read error...");
                iox.printStackTrace();
                return false;
            }

            if (datastr.indexOf("/# Start of SYN_MAP output #/") == -1) {
                return false;
            }
            if (datastr.indexOf("/# End of SYN_MAP output #/") == -1) {
                return false;
            }
            if (datastr.indexOf("N1: ") == -1) {
                return false;
            }
            if (datastr.indexOf("N2: ") == -1) {
                return false;
            }
            if (datastr.indexOf("ST: ") == -1) {
                return false;
            }
            int c = datastr.indexOf("FN: ");
            int c1 = datastr.indexOf("N2: ", c);
            if (c1 - c < 20) {
                return false;
            }

            return true;
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
            } catch (IOException iox) {
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
            jLabelProgram.setText("  Program/Method :  " +
                                  (String) ProgramBox.getSelectedItem());
            if ((ProgramBox.getSelectedItem()).equals("SyntenyMap")) {
                SynMapParametersComboBox.show();
            } else {
                SynMapParametersComboBox.hide();
            }
        }

        void ButtonCancel_actionPerformed(ActionEvent e) {
            cancel_flag = true;
        }

    }


    /**
     * <p>Tree Model Listener</p>
     */
    class MyTreeModelListener implements TreeModelListener {
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
            } catch (NullPointerException exc) {}
        }

        public void treeNodesInserted(TreeModelEvent e) {
        }

        public void treeNodesRemoved(TreeModelEvent e) {
        }

        public void treeStructureChanged(TreeModelEvent e) {
        }
    }


    /**
     * <p>Program Box</p>
     */
    class SyntenyParameters_ProgramBox_actionAdapter implements java.awt.event.
            ActionListener {
        SyntenyParameters adaptee;

        SyntenyParameters_ProgramBox_actionAdapter(SyntenyParameters adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.ProgramBox_actionPerformed(e);
        }
    }


    /**
     * <p>Run Button
     */
    class SyntenyParameters_addButton_actionAdapter implements java.awt.event.
            ActionListener {
        SyntenyParameters adaptee;

        SyntenyParameters_addButton_actionAdapter(SyntenyParameters adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.addButton_actionPerformed(e);
        }
    }


    /**
     * <p>Run Button
     */
    class SyntenyParameters_runButton_actionAdapter implements java.awt.event.
            ActionListener {
        SyntenyParameters adaptee;

        SyntenyParameters_runButton_actionAdapter(SyntenyParameters adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.ButtonRun_actionPerformed(e);
        }
    }


    /**
     * <p>Run Button
     */
    class SyntenyParameters_cancelButton_actionAdapter implements java.awt.event.
            ActionListener {
        SyntenyParameters adaptee;

        SyntenyParameters_cancelButton_actionAdapter(SyntenyParameters adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.ButtonCancel_actionPerformed(e);
        }
    }


    class SyntenyParameters_redrawButton_actionAdapter implements java.awt.event.
            ActionListener {
        SyntenyParameters adaptee;

        SyntenyParameters_redrawButton_actionAdapter(SyntenyParameters adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.redrawButton_actionPerformed(e);
        }
    }



