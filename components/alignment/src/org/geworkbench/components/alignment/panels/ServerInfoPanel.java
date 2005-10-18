package org.geworkbench.components.alignment.panels;


import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSAlignmentResultSet;
import org.geworkbench.util.session.SoapClient;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.globus.progtutorial.clients.BlastService.Client;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;


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
    private ParameterViewWidget pv;
    // String imageName = "c:/data/pbidle.png";
    JPanel jPanel1 = new JPanel();
    JButton Stop = new JButton();
    JButton Connect = new JButton();
    JButton Refresh = new JButton();
    BorderLayout borderLayout2 = new BorderLayout();
    JTextField jTextField1 = new JTextField();
    BoxLayout boxLayout21;

    public ServerInfoPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {

        this.setDebugGraphicsOptions(0);
        this.setOpaque(true);
        this.addAncestorListener(new ServerInfoPanel_this_ancestorAdapter(this));
        this.setLayout(borderLayout2);
        boxLayout21 = new BoxLayout(jPanel2, BoxLayout.Y_AXIS);
        jPanel2.setLayout(boxLayout21);
        jPanel3.setLayout(gridBagLayout1);
        jTotalProcessorLabel.setIconTextGap(3);
        jTotalProcessorLabel.setText("Total Processors:");
        jServerVersionTextField.addActionListener(new ServerInfoPanel_jServerVersionTextField_actionAdapter(this));
        jServerVersionLabel.setIconTextGap(3);
        jServerVersionLabel.setText("Server Up Time:");
        jCurrentAvailProcessorLabel.setIconTextGap(3);
        jCurrentAvailProcessorLabel.setText("Idle Processors:");
        jQueueJobLabel.setIconTextGap(3);
        jQueueJobLabel.setText("Queued jobs:");
        jServerUptimeLabel.setIconTextGap(3);
        jServerUptimeLabel.setText("Server Version:");
        Stop.setText("Stop");
        Connect.setRolloverEnabled(true);
        Connect.setText("Connect");
        Connect.addActionListener(new ServerInfoPanel_Connect_actionAdapter(this));
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

        this.add(jPanel2, BorderLayout.CENTER);
        jPanel3.add(jQueueJobLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jCurrentAvailProcessorLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jAvailProcessorTextField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jTotalProcessorLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jTotalProcessor, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerVersionLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerUptimeTextField, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerUptimeLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jServerVersionTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jPanel3.add(jqueuedTextField, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jPanel1.add(Connect, null);
        jPanel1.add(Refresh, null);
        jPanel1.add(Stop, null);
        jPanel2.add(jPanel3, null);
        jPanel2.add(jPanel1, null);
    }

    void Connect_actionPerformed(ActionEvent e) {
        retriveServerInfo();

    }

    public void reportServerError() {

        JOptionPane.showMessageDialog(null, "The connection to Columbia Soap Server is refused.", "Connection Error", JOptionPane.ERROR_MESSAGE);

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
                    }
                    jTotalProcessor.setText("40.");
                }

            }

        };
        SwingUtilities.invokeLater(getServerInfo);

    }

    void this_ancestorAdded(AncestorEvent e) {

    }

    void Refresh_actionPerformed(ActionEvent e) {
        retriveServerInfo();

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
            String tempFolder = System.getProperties().getProperty("temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";

            }

            String filename = "C:\\" + url.getFile();
            PrintWriter bw = new PrintWriter(new FileOutputStream(filename));
            URLConnection urlCon = url.openConnection();

            StringBuffer sb = new StringBuffer("");
            String line = "";

            BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            while ((line = br.readLine()) != null) {
                bw.println(line);
            }
            br.close();
            bw.close();

            DSAlignmentResultSet blastResult = new CSAlignmentResultSet(input, filename);

            org.geworkbench.events.ProjectNodeAddedEvent event = new ProjectNodeAddedEvent("message", null, blastResult);
            BlastAppComponent blastAppComponent = pv.getBlastAppComponent();
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



    public ParameterViewWidget getPv() {
        return pv;
    }

    public void setPv(ParameterViewWidget pv) {
        this.pv = pv;
    }

}

class ServerInfoPanel_Connect_actionAdapter implements java.awt.event.ActionListener {
    ServerInfoPanel adaptee;

    ServerInfoPanel_Connect_actionAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.Connect_actionPerformed(e);
    }
}

class ServerInfoPanel_this_ancestorAdapter implements javax.swing.event.AncestorListener {
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

class ServerInfoPanel_Refresh_actionAdapter implements java.awt.event.ActionListener {
    ServerInfoPanel adaptee;

    ServerInfoPanel_Refresh_actionAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.Refresh_actionPerformed(e);
    }
}

class ServerInfoPanel_jServerVersionTextField_actionAdapter implements java.awt.event.ActionListener {
    ServerInfoPanel adaptee;

    ServerInfoPanel_jServerVersionTextField_actionAdapter(ServerInfoPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jServerVersionTextField_actionPerformed(e);
    }
}
