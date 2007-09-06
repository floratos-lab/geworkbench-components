package org.geworkbench.components.alignment.panels;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.AncestorEvent;

import org.geworkbench.bison.datastructure.bioobjects.sequence.
        CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.
        DSAlignmentResultSet;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;
import org.globus.progtutorial.clients.BlastService.Client;


/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: AMDeC_Califano lab</p>
 *
 * @author XZ
 * @version 1.0
 */

public class ServerInfoPanel extends JPanel {
    JPanel jPanel2 = new JPanel();
    JPanel jPanel3 = new JPanel();
    JLabel jTotalProcessorLabel = new JLabel();
    JTextField jServerVersionTextField = new JTextField();
    JLabel jServerVersionLabel = new JLabel();
    JLabel jCurrentAvailProcessorLabel = new JLabel();
    JLabel jQueueJobLabel = new JLabel();
    JLabel jServerUptimeLabel = new JLabel();
    JTextField jTotalProcessor = new JTextField();
    JTextField jAvailProcessorTextField = new JTextField();
    JTextField jqueuedTextField = new JTextField();
    JTextField jServerUptimeTextField = new JTextField();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    private boolean serverOK;
    private BlastAppComponent pv;
    JButton Refresh = new JButton();
    BorderLayout borderLayout2 = new BorderLayout();
    JTextField jTextField1 = new JTextField();
    JPanel jToolBar1 = new JPanel();
    ButtonGroup bottonGroup = new ButtonGroup();
    JRadioButton jRadioButton1 = new JRadioButton();
    JRadioButton jRadioButton2 = new JRadioButton();
    JRadioButton jRadioButton3 = new JRadioButton();
    BorderLayout borderLayout1 = new BorderLayout();
    CreateGridServicePanel sgePanel = new CreateGridServicePanel();
    public static final String GRID = "grid";
    public static final String NCBI = "ncbi";
    public static final String COLUMBIA = "columbia";
    static String DEFAULTSERVERTYPE = NCBI;
    String serverType = DEFAULTSERVERTYPE;


    public ServerInfoPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {

        this.setDebugGraphicsOptions(0);
        this.setMinimumSize(new Dimension(120, 100));
        this.setOpaque(true);
        this.addAncestorListener(new ServerInfoPanel_this_ancestorAdapter(this));
        this.setLayout(borderLayout2);
        jRadioButton3.addActionListener(new
                ServerInfoPanel_jRadioButton3_actionAdapter(this));
        jRadioButton2.addActionListener(new
                ServerInfoPanel_jRadioButton2_actionAdapter(this));

        if (DEFAULTSERVERTYPE.equals(NCBI)) {
            jRadioButton2.setSelected(true);
        } else {
            jRadioButton1.setSelected(true);
        }
        bottonGroup.add(jRadioButton1);
        bottonGroup.add(jRadioButton2);
        //remove grid button.
        //bottonGroup.add(jRadioButton3);
        jPanel2.setLayout(borderLayout1);
        jPanel3.setLayout(gridBagLayout1);
        jTotalProcessorLabel.setIconTextGap(3);
        jTotalProcessorLabel.setText("Total Processors:");
        jServerVersionTextField.addActionListener(new
                ServerInfoPanel_jServerVersionTextField_actionAdapter(this));
        jServerVersionLabel.setIconTextGap(3);
        jServerVersionLabel.setText("Server Up Time:");
        jCurrentAvailProcessorLabel.setIconTextGap(3);
        jCurrentAvailProcessorLabel.setText("Idle Processors:");
        jQueueJobLabel.setIconTextGap(3);
        jQueueJobLabel.setText("Queued jobs:");
        jServerUptimeLabel.setIconTextGap(3);
        jServerUptimeLabel.setText("Server Version:");
        Refresh.setText("Refresh");
        Refresh.addActionListener(new ServerInfoPanel_Refresh_actionAdapter(this));
        jServerUptimeTextField.setFont(new java.awt.Font("Dialog", 0, 12));
        jServerUptimeTextField.setText("");
        jTotalProcessor.setFont(new java.awt.Font("Dialog", 0, 12));
        jTotalProcessor.setText("");
        jServerVersionTextField.setFont(new java.awt.Font("Dialog", 0, 12));
        jServerVersionTextField.setToolTipText("");
        jServerVersionTextField.setText("");
        jAvailProcessorTextField.setFont(new java.awt.Font("Dialog", 0, 12));
        jqueuedTextField.setFont(new java.awt.Font("Dialog", 0, 12));
        jPanel2.setDebugGraphicsOptions(0);
        jPanel2.setMinimumSize(new Dimension(120, 100));
        jRadioButton1.setText("Columbia Server");
        jRadioButton1.addActionListener(new
                ServerInfoPanel_jRadioButton1_actionAdapter(this));
        jRadioButton2.setText("NCBI");
        jRadioButton3.setText("Grid");

        this.add(jPanel2, BorderLayout.CENTER);
        jPanel3.add(jQueueJobLabel,
                new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jCurrentAvailProcessorLabel,
                new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jAvailProcessorTextField,
                new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jTotalProcessorLabel,
                new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jTotalProcessor,
                new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerVersionLabel,
                new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerUptimeTextField,
                new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerUptimeLabel,
                new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.NONE,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerVersionTextField,
                new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jqueuedTextField,
                new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0,
                        GridBagConstraints.WEST,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(2, 2, 2, 2), 0, 0));
        jToolBar1.add(jRadioButton1);
        jToolBar1.add(jRadioButton2);
       // jToolBar1.add(jRadioButton3);
        jToolBar1.add(Refresh);
        jPanel2.add(jToolBar1, java.awt.BorderLayout.NORTH);
        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);
    }

    void Connect_actionPerformed(ActionEvent e) {
        retriveServerInfo();

    }

    public void reportServerError() {

        JOptionPane.showMessageDialog(null,
                "The connection to Columbia Server is refused.",
                "Connection Error",
                JOptionPane.ERROR_MESSAGE);

    }

    public void retriveServerInfo() {

        Runnable getServerInfo = new Runnable() {
            public void run() {
                SoapClient sc = new org.geworkbench.util.session.SoapClient();
                String serverInfo = sc.getBlastServerInfo();
                if (serverInfo.endsWith("ERROR")) {
                    reportServerError();
                    serverOK = false;
                    return;
                }

                serverOK = true;
                StringTokenizer st = new StringTokenizer(serverInfo, "\n");
                boolean realOK = false;
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    if (token.startsWith("Server uptime:")) {

                        jServerUptimeTextField.setText(token.substring(14));
                    }
                    if (token.startsWith("Server version:")) {
                        jServerVersionTextField.setText(token.substring(15));
                        //System.out.println ( token.substring(15));
                    }
                    if (token.startsWith("Jobs queued: ")) {
                        jqueuedTextField.setText(token.substring(13));
                        // System.out.println ( new Integer(token.substring(12).trim()));
                    }
                    if (token.startsWith("Idle worker processors:")) {
                        String idleWorker = token.substring(24);
                        //idleWorker = idleWorker.replace('.', ' ');
                        //int availableWorker = 42 - new Integer(idleWorker.trim()).intValue();
                        //jAvailProcessorTextField.setText(new Integer(availableWorker).toString());
                        jAvailProcessorTextField.setText(idleWorker);
                        realOK = true;
                    }

                    serverType = COLUMBIA;
                }
                if (realOK) {
                    //temp set up.
                    jTotalProcessor.setText("36.");
                } else {

                    reportServerError();
                }
            }

        };
        SwingUtilities.invokeLater(getServerInfo);

    }

    void this_ancestorAdded(AncestorEvent e) {

    }

    void Refresh_actionPerformed(ActionEvent e) {
        if (jRadioButton1.isSelected()) {
            retriveServerInfo();
        }else{
            jRadioButton2_actionPerformed(e); 
        }
    }

    void jServerVersionTextField_actionPerformed(ActionEvent e) {

    }

    public boolean isServerOK() {
        return serverOK;
    }

    public void setServerOK(boolean serverOK) {
        this.serverOK = serverOK;
    }


    void jButton2_actionPerformed(ActionEvent e) {
        //added by xq
        pv.createGridDialog();
        //Temp code for demo.
        try {
            Client client = new Client();
            String input = "C:/test.txt";
            String output = client.submitRequest(input);

            BrowserLauncher.openURL(output);
            URL url = new URL(output);
            String tempFolder = System.getProperties().getProperty(
                    "temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";

            }

            String filename = "C:\\" + url.getFile();
            PrintWriter bw = new PrintWriter(new FileOutputStream(filename));
            URLConnection urlCon = url.openConnection();

            StringBuffer sb = new StringBuffer("");
            String line = "";

            BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.
                    getInputStream()));
            while ((line = br.readLine()) != null) {
                bw.println(line);
            }
            br.close();
            bw.close();

            DSAlignmentResultSet blastResult = new CSAlignmentResultSet(input,
                    filename, null);

            org.geworkbench.events.ProjectNodeAddedEvent event = new
                    ProjectNodeAddedEvent("message", null, blastResult);
            BlastAppComponent blastAppComponent = pv;
            blastAppComponent.publishProjectNodeAddedEvent(event);
        } catch (Exception f) {
            f.printStackTrace();
        }

    }

    /*
        void  jButton2_actionPerformed(ActionEvent e) {
             pv.createGridDialog();
         }

        void  jButton2_actionPerformed(ActionEvent e) {
           // pv.retriveParameters();
            pv.createGridDialog();


           //Temp code for demo.
           try{
           Client client = new Client();
           String input = "C:/test.txt";
           String output = client.submitRequest(input);

           BrowserLauncher.openURL(output);
           URL url = new URL(output);
           String tempFolder = System.getProperties().getProperty(
                "temporary.files.directory");
            if (tempFolder == null) {
              tempFolder = ".";

            }

           String filename = "C:\\"+ url.getFile();
           PrintWriter bw = new PrintWriter( new FileOutputStream(filename));
           URLConnection urlCon = url.openConnection();

               StringBuffer sb = new StringBuffer("");
               String line = "";

               BufferedReader br = new BufferedReader(
                       new InputStreamReader(urlCon.getInputStream()) );
               while ((line = br.readLine()) != null)
               {
                   bw.println(line);
               }
               br.close();
               bw.close();

     CSAlignmentResultSet blastResult = new CSAlignmentResultSet( input, filename);

           ProjectNodeAddedEvent event =
                       new ProjectNodeAddedEvent(null, "message", null,
                                                 blastResult);
      BlastAppComponent blastAppComponent = pv.getBlastAppComponent();
                   blastAppComponent.throwEvent(ProjectNodeAddedListener.class,
                                                "projectNodeAdded", event);
           }catch (Exception f){f.printStackTrace();}
        }

     */


    public BlastAppComponent getPv() {
        return pv;
    }

    public void setPv(BlastAppComponent pv) {
        this.pv = pv;
    }

    public void jRadioButton1_actionPerformed(ActionEvent e) {
        jPanel2.remove(sgePanel);
        jPanel2.add(jPanel3, BorderLayout.CENTER);
        retriveServerInfo();
        this.validate();
        this.repaint();
    }

    public void jRadioButton3_actionPerformed(ActionEvent e) {
        jPanel2.remove(jPanel3);
        //  this.getContentPane().remove(jPanel6);
        jPanel2.add(sgePanel, BorderLayout.CENTER);
        sgePanel.setPv(pv);
        serverType = this.GRID;
        this.validate();
        this.repaint();
    }

    public void jRadioButton2_actionPerformed(ActionEvent e) {

        jServerUptimeTextField.setText("August 26, 2007");
        jServerVersionTextField.setText("Blast 2.2.17");
        jqueuedTextField.setText("N/A");
        jAvailProcessorTextField.setText("N/A");
        jTotalProcessor.setText("N/A");
        jPanel2.remove(sgePanel);
        jPanel2.add(jPanel3, BorderLayout.CENTER);
        serverType = this.NCBI;
        this.validate();
        this.repaint();

    }

    /**
     * getServerType
     */
    public String getServerType() {
        if (serverType != null) {
            return serverType;
        }
        return DEFAULTSERVERTYPE;
    }

    /**
     * setBlastAppComponent
     *
     * @param blastAppComponent BlastAppComponent
     */
    public void setBlastAppComponent(BlastAppComponent blastAppComponent) {
        this.pv = blastAppComponent;
    }


}


class ServerInfoPanel_jRadioButton2_actionAdapter implements ActionListener {
    private ServerInfoPanel adaptee;

    ServerInfoPanel_jRadioButton2_actionAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jRadioButton2_actionPerformed(e);
    }
}


class ServerInfoPanel_jRadioButton1_actionAdapter implements ActionListener {
    private ServerInfoPanel adaptee;

    ServerInfoPanel_jRadioButton1_actionAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jRadioButton1_actionPerformed(e);
    }
}


class ServerInfoPanel_jRadioButton3_actionAdapter implements ActionListener {
    private ServerInfoPanel adaptee;

    ServerInfoPanel_jRadioButton3_actionAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {

        adaptee.jRadioButton3_actionPerformed(e);
    }
}


class ServerInfoPanel_this_ancestorAdapter implements javax.swing.event.
        AncestorListener {
    ServerInfoPanel adaptee;

    ServerInfoPanel_this_ancestorAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void ancestorAdded(AncestorEvent e) {
        adaptee.this_ancestorAdded(e);
    }

    public void ancestorMoved(AncestorEvent e) {
    }

    public void ancestorRemoved(AncestorEvent e) {
    }
}


class ServerInfoPanel_Refresh_actionAdapter implements java.awt.event.
        ActionListener {
    ServerInfoPanel adaptee;

    ServerInfoPanel_Refresh_actionAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.Refresh_actionPerformed(e);
    }
}


class ServerInfoPanel_jServerVersionTextField_actionAdapter implements java.awt.
        event.ActionListener {
    ServerInfoPanel adaptee;

    ServerInfoPanel_jServerVersionTextField_actionAdapter(ServerInfoPanel
            adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jServerVersionTextField_actionPerformed(e);
    }
}
