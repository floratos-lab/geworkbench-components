package org.geworkbench.components.alignment.synteny;

import javax.swing.*;

import org.geworkbench.util.FilePathnameUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.IOException;

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
public class MarkerPane
    extends JPanel {

    int max_selected=7;
    int nmeasures = 2;
    String[] Measures = {
        "BLAST p-value", "BLAST homology"};

    int nmarkers = 30;
    String[] Markers = {
        "1000_at", "1001_at", "1002_f_at", "1003_s_at", "1004_at",
        "1005_at", "1006_at", "1007_s_at", "1008_f_at", "1009_at",
        "100_g_at", "1010_at", "1011_s_at", "1012_at", "1013_at",
        "1014_at", "1015_s_at", "1016_s_at", "1017_at", "1018_at",
        "1019_g_at", "101_at", "1020_s_at", "1021_at", "1022_f_at",
        "1023_at", "1024_at", "1025_g_at", "1026_s_at", "1027_at"
    };

    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel1 = new JLabel();
    JScrollPane jScrollPane1 = new JScrollPane();
    JLabel jLabel2 = new JLabel();
    JScrollPane jScrollPane2 = new JScrollPane();
    JLabel jLabel3 = new JLabel();
    JScrollPane jScrollPane3 = new JScrollPane();
    JButton jButton1 = new JButton();

    JList jListMarkers = new JList();
    JList jListSelectedMarkers = new JList();
    JList jListMeasures = new JList();

    DefaultListModel listModel1 = new DefaultListModel();
    DefaultListModel listModel2 = new DefaultListModel();
    DefaultListModel listModel3 = new DefaultListModel();

    public MarkerPane() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        jLabel1.setText("Current markers");
        jLabel2.setText("Selected Markers");
        jLabel3.setText("Measure");
        jButton1.setText("View with selected markers");
        jButton1.addActionListener(new viewButton_actionAdapter(this));


        for (int ii = 0; ii < nmarkers; ii++) {
            listModel1.addElement(Markers[ii]);
        }
        for (int ii = 0; ii < nmeasures; ii++) {
            listModel3.addElement(Measures[ii]);
        }

        jListMarkers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jMarkerList_mouseClicked(e);
            }
        });

        jListSelectedMarkers.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jSelectedMarkerList_mouseClicked(e);
            }
        });

        jScrollPane1.setVerticalScrollBarPolicy(JScrollPane.
                                                VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane2.setVerticalScrollBarPolicy(JScrollPane.
                                                VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane3.setVerticalScrollBarPolicy(JScrollPane.
                                                VERTICAL_SCROLLBAR_ALWAYS);
        jListMeasures.setModel(listModel3);

        jListMeasures.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jListMarkers.setModel(listModel1);
        jListSelectedMarkers.setModel(listModel2);
        this.add(jLabel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), 0, 0));
        this.add(jScrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 1.0
            , GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel2, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), 0, 0));
        this.add(jScrollPane2, new GridBagConstraints(1, 3, 2, 1, 0.0, 1.0
            , GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        this.add(jLabel3, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
                                                 , GridBagConstraints.NORTHWEST,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(0, 0, 0, 0), 0, 0));
        this.add(jScrollPane3, new GridBagConstraints(1, 5, 1, 2, 0.0, 1.0
            , GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        this.add(jButton1, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0
                                                  ,
                                                  GridBagConstraints.NORTHWEST,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(0, 0, 0, 0), 0, 0));

        jScrollPane1.getViewport().add(jListMarkers);
        jScrollPane2.getViewport().add(jListSelectedMarkers);
        jScrollPane3.getViewport().add(jListMeasures);
    }

    void jMarkerList_mouseClicked(MouseEvent e) {
        int index = jListMarkers.locationToIndex(e.getPoint());
        int n = listModel2.getSize();

        if (e.getClickCount() == 2) {
            if(n<max_selected){
                for(int i=0;i<n;i++)
                    if(((String)listModel2.getElementAt(i)).compareTo(Markers[index]) == 0){return;}
                listModel2.addElement(Markers[index]);
            }
        }
    }
    void jSelectedMarkerList_mouseClicked(MouseEvent e) {
        int index = jListSelectedMarkers.locationToIndex(e.getPoint());
        if (e.getClickCount() == 2) {
            if(listModel2.getSize()>0){
                listModel2.remove(index);
            }
        }
    }

    void viewButton_actionPerformed(ActionEvent e){
        int fx, tx, fy, ty, i, j;

        if(listModel2.getSize()==0)return;

		String tempDir = FilePathnameUtils.getTemporaryFilesDirectoryPath();

              FileOutputStream fout;
              String out_name = null;
              String job_id = null;

              job_id = new String("Synteny_" + Math.rint(Math.random() * 1000000));
              out_name = new String(tempDir + job_id + ".sub");

              try {
                  fout = new FileOutputStream(out_name);
                  String tmp;
                  tmp = new String("JOB_ID: " + job_id + "\n");
                  fout.write(tmp.getBytes());
                  tmp = new String("REQUEST_TYPE: MARKER\n");
                  fout.write(tmp.getBytes());
                  tmp = new String("RADIUS: 20\n");
                  fout.write(tmp.getBytes());
                  tmp = new String("N_MARKERS: "+listModel2.getSize()+"\n");
                  fout.write(tmp.getBytes());
                  for(i=0; i < listModel2.getSize();i++){
                      tmp = new String("MARKER: " + listModel2.get(i) + "\n");
                      fout.write(tmp.getBytes());
                  }
                  fout.flush();
                  fout.close();
              }
              catch (IOException ioe) {
                  return;
              }

/*              try {
                  SoapClient sp = new SoapClient();
                  String infile = new String(sp.submitFile(out_name));

                  String result_file = new String(
                      "/users/amdecweb/jakarta-tomcat-4.1.30/bin/outputFolder/" +
                      job_id + ".res");
                  String job_string = new String(sp.submitJob(
                      "java -cp /adtera/users/pavel/synteny_remote/ SyntenyServerSide",
                      infile, result_file));

                  i = 0;
                  while (sp.isJobFinished(job_id + ".res") == false) {
                      Thread.sleep(1000);
                      if (i++ > 1000) {
                          break;
                      }
                  }
                  String tURL = new String(
                      "http://amdec-bioinfo.cu-genome.org/html/temp/" + job_id +
                      ".res");
              }
              catch (Exception ee) {
                  return;
              }
*/              // Third - parsing the results

    }
    //-----------------------------
}

class viewButton_actionAdapter
    implements java.awt.event.ActionListener {
    MarkerPane adaptee;

    viewButton_actionAdapter(MarkerPane adaptee) {
        this.adaptee = adaptee;
    }
    public void actionPerformed(ActionEvent e) {
        adaptee.viewButton_actionPerformed(e);
    }
}


