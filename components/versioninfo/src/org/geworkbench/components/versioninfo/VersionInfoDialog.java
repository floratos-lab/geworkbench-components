package org.geworkbench.components.versioninfo;

/**
 * <p>Title: caWorkbench</p>
 *
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 *
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 *
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 3.0
 */

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.URL;

public class VersionInfoDialog
    extends JDialog implements ActionListener {
    /**
     * VersionInfoDialog
     */
    public VersionInfoDialog() {
        this(null);
    }

    private static Properties properties = new Properties();
    JPanel panel1 = new JPanel();
    JPanel panel2 = new JPanel();
    JPanel insetsPanel1 = new JPanel();
    JPanel insetsPanel2 = new JPanel();
    JPanel insetsPanel3 = new JPanel();
    JButton button1 = new JButton();
    JLabel imageControl1 = new JLabel();
    ImageIcon imageIcon;
    JLabel label1 = new JLabel();
    JLabel label2 = new JLabel();
    JLabel label3 = new JLabel();
    JLabel label4 = new JLabel();
    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    FlowLayout flowLayout2 = new FlowLayout();
    GridLayout gridLayout1 = new GridLayout();
    static String product = "caWorkbench";
    static String version = "Version 3.0";
    static String buildTime = (new Date()).toString();

    static String build = "";

    public String comments = "A Open Platform for BioMedical Informatics.";
    public static final String DEFAULTVERSIONFILE = "version.txt";
    public static final String VERSIONFILEPATH = "temp";
    private static String fullPath = VERSIONFILEPATH + File.separator +
        DEFAULTVERSIONFILE;

    public VersionInfoDialog(Frame parent) {
        super(parent);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            readProperties(VersionInfoDialog.class.getResource("version.txt").
                           openStream());
        }
        catch (IOException ex) {
        }
        //readProperties(fullPath);
        version = properties.getProperty("version");
        build = properties.getProperty("build");
        buildTime = properties.getProperty("buildTime");

        try {
            jbInit();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //imageControl1.setIcon(imageIcon);
        pack();

    }

    /*
     * Reads the version file into a properties object.
     */
    protected static void readProperties(String versionfileName) {

        try {
            File initFile = new File(versionfileName);
            if (initFile.canRead()) {
                FileInputStream input = new FileInputStream(initFile);
                properties.load(input);
            }
            else {
            }
        }
        catch (IOException ex1) {
            System.out.println("VersionInfo Error" + ex1.toString());
        }
    }

    /**
     *  Write to a version.txt file with the current properties.
     */
    public synchronized static void writeProperties(String versionfileName) {
        try {
            FileOutputStream output = new FileOutputStream(new File(
                versionfileName));
            properties.store(output, "Version Information:");
        }
        catch (IOException ex2) {
            System.out.println("Error: " + ex2);
        }
    }

    /**
     *  Write to a version.txt file with the current properties.
     */
    public synchronized static void writeProperties(OutputStream output) {
        try {
            //FileOutputStream output = new FileOutputStream(new File(versionfileName));
            properties.store(output, "Version Information:");
        }
        catch (IOException ex2) {
            System.out.println("Error: " + ex2);
        }
    }

    /**
     * update the build number by 1.
     */
    private static void update() {
        build = properties.getProperty("build");
        try {

            if (build != null) {
                String[] items = build.split("_");
                int newIntValue = new Integer(items[items.length - 1]).intValue() +
                    1;
                items[items.length - 1] = "" + newIntValue;

                String newValue = "Build";
                for (int i = 1; i < items.length; i++) {
                    newValue = newValue + "_" + items[i];
                }
                properties.put("build", newValue);
                properties.put("buildTime", buildTime);
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {

        this.setTitle("About caWorkbench");
        setResizable(false);
        panel1.setLayout(borderLayout1);
        panel2.setLayout(borderLayout2);
        insetsPanel1.setLayout(flowLayout1);
        insetsPanel2.setLayout(flowLayout1);
        insetsPanel2.setBorder(new EmptyBorder(10, 10, 10, 10));
        gridLayout1.setRows(4);
        gridLayout1.setColumns(1);
        label1.setText(product);
        label2.setText(version + " " + build);
        label3.setText("Updated on " + buildTime);
        label4.setText(comments);
        insetsPanel3.setLayout(gridLayout1);
        insetsPanel3.setBorder(new EmptyBorder(10, 60, 10, 10));
        button1.setText("Ok");
        button1.addActionListener(this);
        insetsPanel2.add(imageControl1, null);
        panel2.add(insetsPanel2, BorderLayout.WEST);
        this.getContentPane().add(panel1, null);
        insetsPanel3.add(label1, null);
        insetsPanel3.add(label2, null);
        insetsPanel3.add(label3, null);
        insetsPanel3.add(label4, null);
        panel2.add(insetsPanel3, BorderLayout.CENTER);
        insetsPanel1.add(button1, null);
        panel1.add(insetsPanel1, BorderLayout.SOUTH);
        panel1.add(panel2, BorderLayout.NORTH);
    }

    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            cancel();
        }
        super.processWindowEvent(e);
    }

    void cancel() {
        dispose();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == button1) {
            cancel();
        }
    }

    /**
     * main
     *
     * @param strings String[]
     */
    public static void main(String[] strings) throws IOException {
        // VersionInfoDialog vid = new VersionInfoDialog();
//
//        URL url = VersionInfoDialog.class.getResource("version.txt");
//        String s = url.toString();
//        InputStream in = null;
//        in = url.openStream();
//        VersionInfoDialog.readProperties(in);
        VersionInfoDialog.readProperties(fullPath);
        VersionInfoDialog.update();
        VersionInfoDialog.writeProperties(fullPath);
        // VersionInfoDialog.writeProperties(new FileOutputStream(s));
    }

    /**
     * readProperties
     *
     * @param in InputStream
     */
    public static void readProperties(InputStream in) {
        try {

            properties.load(in);

        }
        catch (IOException ex1) {
            System.out.println("VersionInfo Error" + ex1.toString());
        }

    }

}
