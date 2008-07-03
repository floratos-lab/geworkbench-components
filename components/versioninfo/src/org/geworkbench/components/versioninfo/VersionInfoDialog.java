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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.geworkbench.engine.config.PluginDescriptor;
import org.geworkbench.engine.management.ComponentRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

public class VersionInfoDialog
    extends JDialog implements ActionListener {
    /**
     * VersionInfoDialog
     */
    public VersionInfoDialog() {
        this(null);
    }

    private static Properties properties = new Properties();
    JButton button1 = new JButton();
    JLabel imageControl1 = new JLabel();
    ImageIcon imageIcon;
    static String product = "geWorkbench";
    static String version = "Version 1.0.";
    static String buildTime = (new Date()).toString();

    static String build = "1";

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

        this.setTitle("geWorkbench Version Info");
        setResizable(false);

        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));

        button1.setText("Ok");
        button1.addActionListener(this);

        {
            FormLayout layout = new FormLayout("right:270dlu,10dlu", "");
//            FormLayout layout = new FormLayout("right:570dlu,10dlu", "");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();
            builder.appendSeparator("geWorkbench Core");
            builder.nextLine();
            builder.append(new JLabel("geWorkbench"));
            builder.nextLine();
            builder.append(new JLabel(version + "." + build));
            builder.nextLine();
            builder.append(new JLabel("Updated on " + buildTime));

            this.getContentPane().add(builder.getPanel(), null);
        }


//        {
//            // Loop through the components and add their info
//
//            FormLayout layout = new FormLayout("right:160dlu,10dlu,20dlu,5dlu,right:160dlu,10dlu,20dlu,5dlu,right:160dlu,10dlu,20dlu", "");
//            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
//            builder.setDefaultDialogBorder();
//
//            builder.appendSeparator("Plugins / Components");
//            Collection components = ComponentRegistry.getRegistry().getAllPluginDescriptors();
//            int counter = 0;
//            for (Iterator iterator = components.iterator(); iterator.hasNext();) {
//                PluginDescriptor pluginDescriptor = (PluginDescriptor) iterator.next();
//
//                if (counter % 3 == 0) {
//                    builder.nextLine();
//                }
//
//                addPluginInfo(builder, pluginDescriptor);
//                counter++;
//
////                builder.append(pluginDescriptor.getLabel(), new JLabel("v1.0"));
////                builder.append("");
////                builder.append("v1.0");
//            }
//
//            this.getContentPane().add(builder.getPanel(), null);
//        }

        {
            // A bit of overkill for the OK button layout
//            FormLayout layout = new FormLayout("right:570dlu", "");
            FormLayout layout = new FormLayout("right:270dlu", "");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);
            builder.setDefaultDialogBorder();
            builder.append(button1);

            this.getContentPane().add(builder.getPanel(), null);
        }
    }

    private void addPluginInfo(DefaultFormBuilder builder, PluginDescriptor pluginDescriptor) {
        if (pluginDescriptor.isLoadedFromGear()) {
            builder.append(pluginDescriptor.getLabel() + " (from GEAR)", new JLabel("v1.0"));
        } else {
            builder.append(pluginDescriptor.getLabel(), new JLabel("v1.0"));
        }
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
