package org.geworkbench.components.alignment.panels;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;
import org.geworkbench.util.session.SoapClient;
import org.geworkbench.components.alignment.synteny.DotsParser;
import org.geworkbench.components.alignment.synteny.GPAnnoParser;
import org.geworkbench.components.alignment.synteny.PFPParser;
import org.geworkbench.components.alignment.panels.das.DAS_Retriver;
import org.geworkbench.components.alignment.synteny.DotMatrixViewWidget;
import org.geworkbench.components.alignment.synteny.DotMatrixViewWidgetPanel;
import org.geworkbench.util.sequences.Genome;
import org.geworkbench.util.sequences.SequenceAnnotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Calendar;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class SyntenyViewWidget extends JPanel {
    JPanel jBasicPane = new JPanel();
    JPanel jAdvancedPane = new JPanel();
    JTabbedPane jTabbedPane1 = new JTabbedPane();

    JTextArea jTextAreaX = new JTextArea();
    JLabel jLabel1 = new JLabel();
    JComboBox SourceBoxX = new JComboBox();
    JComboBox ChrBoxX = new JComboBox();
    JSpinner FromX = new JSpinner();
    JSpinner ToX = new JSpinner();
    JLabel jLabel4 = new JLabel();
    JLabel jLabel5 = new JLabel();
    JButton ButtonBrowseX = new JButton();
    JLabel jLabel6 = new JLabel();
    JTextArea jTextAreaY = new JTextArea();
    JComboBox SourceBoxY = new JComboBox();
    JComboBox ChrBoxY = new JComboBox();
    JSpinner ToY = new JSpinner();
    JSpinner FromY = new JSpinner();
    JLabel jLabel8 = new JLabel();
    JButton ButtonBrowseY = new JButton();
    JLabel jLabel10 = new JLabel();
    JButton runButton = new JButton();
    JComboBox ProgramBox = new JComboBox();
    XYLayout xYLayout2 = new XYLayout();

    String FileX;
    String FileY;
    String tempDir = System.getProperty("temporary.files.directory");

    String genomex = null, genomey = null;

    boolean XSourceDAS = false;
    boolean YSourceDAS = false;
    JRadioButton jRadioButton1 = new JRadioButton();
    JButton jButton1 = new JButton();
    String[] AnnoSources = {"goldenPath ECgene", "goldenPath affyU133", "goldenPath affyU95", "goldenPath affyGnf1h", "PFP (Paracell Filtering Package)", "(N/A) Custom"};
    public String[] AnnoKeys = {"ECgene", "affyU133", "affyU95", "affyGnf1h", "PFP", "Custom"};
    public int NumAnnoKeys = 6;

    JList jListAnnotation = new JList(AnnoSources);
    int[] iniAnnoSelect = {0, 2, 3, 4};
    int knownAnnoNum = 6;
    BorderLayout borderLayout1 = new BorderLayout();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JLabel jLabel2 = new JLabel();

    public SyntenyViewWidget() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        jTextAreaX.setBackground(Color.white);
        jTextAreaX.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaX.setText("No X query selected");
        jTextAreaX.setEditable(false);
        jBasicPane.setLayout(gridBagLayout1);
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 11));
        jLabel1.setForeground(Color.white);
        jLabel1.setText("  Sequence along X axis");
        jLabel4.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel4.setText("From");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabel5.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel5.setText("  To ");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        jListAnnotation.setSelectedIndices(iniAnnoSelect);

        ButtonBrowseX.setText("Browse...");
        ButtonBrowseX.addActionListener(new SyntenyViewWidget_BrowseButtonX_actionAdapter(this));

        jLabel6.setEnabled(true);
        jLabel6.setFont(new java.awt.Font("Dialog", 1, 11));
        jLabel6.setForeground(Color.white);
        jLabel6.setText("  Sequence along Y axis");
        jTextAreaY.setBackground(Color.white);
        jTextAreaY.setBorder(BorderFactory.createEtchedBorder());
        jTextAreaY.setText("No Y query selected");
        jTextAreaY.setEditable(false);
        jLabel8.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel8.setText("From");
        jLabel8.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
        ButtonBrowseY.setText("Browse...");
        ButtonBrowseY.addActionListener(new SyntenyViewWidget_BrowseButtonY_actionAdapter(this));

        jLabel10.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel10.setText("  To ");
        jLabel10.setVerticalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);

        runButton.setBackground(Color.white);
        runButton.setBorder(BorderFactory.createLineBorder(Color.black));
        runButton.setMaximumSize(new Dimension(33, 26));
        runButton.setMinimumSize(new Dimension(33, 26));
        runButton.setPreferredSize(new Dimension(33, 26));
        runButton.setFocusPainted(true);
        runButton.setMargin(new Insets(2, 14, 2, 14));
        runButton.setText("R U N");
        runButton.addActionListener(new SyntenyViewWidget_runButton_actionAdapter(this));


        // Forming select boxes
        SourceBoxX.addItem("Select Source");
        SourceBoxX.addItem("File");
        SourceBoxX.addItem("human (GoldenPath)");
        SourceBoxX.addItem("mouse (GoldenPath)");
        SourceBoxX.addItem("rat (GoldenPath)");
        SourceBoxX.addItem("chimpanzee (GoldenPath)");
        SourceBoxX.addActionListener(new SyntenyViewWidget_selectBoxX_actionAdapter(this));

        SourceBoxY.addItem("Select Source");
        SourceBoxY.addItem("File");
        SourceBoxY.addItem("human (GoldenPath)");
        SourceBoxY.addItem("mouse (GoldenPath)");
        SourceBoxY.addItem("rat (GoldenPath)");
        SourceBoxY.addItem("chimpanzee (GoldenPath)");
        SourceBoxY.addActionListener(new SyntenyViewWidget_selectBoxY_actionAdapter(this));

        initSourceSelection("hg16", ChrBoxX);
        initSourceSelection("hg16", ChrBoxY);


        ProgramBox.addItem("Select program");
        ProgramBox.addItem("Dots");
        ProgramBox.addItem("Mummer");

        /* Setting specific values for demonstration */
        /*~~~~~~~~~~~~~~~~~~~~~*/
        SourceBoxX.setSelectedIndex(2);
        SourceBoxY.setSelectedIndex(2);
        ChrBoxX.setSelectedIndex(21);
        ChrBoxY.setSelectedIndex(2);
        FromX.setValue(new Integer(15531500));
        ToX.setValue(new Integer(15536000));
        FromY.setValue(new Integer(97750000));
        ToY.setValue(new Integer(97754800));
        ProgramBox.setSelectedIndex(1);
        ProgramBox.addActionListener(new SyntenyViewWidget_ProgramBox_actionAdapter(this));
        jTextAreaX.setText("Human " + "chr22" + ":" + Integer.toString(15531500) + "-" + Integer.toString(15536000));
        jTextAreaY.setText("Human " + "chr2" + ":" + Integer.toString(97750000) + "-" + Integer.toString(97754800));
        /*^^^^^^^^^^^^^^^^*/

        jBasicPane.setBackground(SystemColor.desktop);
        jBasicPane.setForeground(Color.black);
        jBasicPane.setAlignmentX((float) 0.5);
        jBasicPane.setMinimumSize(new Dimension(330, 363));
        jBasicPane.setPreferredSize(new Dimension(265, 370));
        jAdvancedPane.setLayout(xYLayout2);
        jAdvancedPane.setBackground(SystemColor.desktop);
        jRadioButton1.setText("jRadioButton1");
        jButton1.setText("jButton1");
        this.setLayout(borderLayout1);
        jLabel2.setFont(new java.awt.Font("Dialog", 1, 11));
        jLabel2.setForeground(Color.white);
        jLabel2.setToolTipText("");
        jLabel2.setText("Program:");


        jBasicPane.add(jTextAreaX, new GridBagConstraints(0, 2, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 0, 2), 0, 0));
        jBasicPane.add(FromY, new GridBagConstraints(1, 11, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        ButtonBrowseY.setVisible(false);
        jBasicPane.add(ButtonBrowseY, new GridBagConstraints(0, 10, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        jBasicPane.add(SourceBoxY, new GridBagConstraints(0, 9, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jBasicPane.add(jTextAreaY, new GridBagConstraints(0, 8, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 2, 2, 2), 0, 0));

        ButtonBrowseX.setVisible(false);
        jBasicPane.add(ButtonBrowseX, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

        jBasicPane.add(jLabel4, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 20, 0));
        jBasicPane.add(jLabel1, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(8, 2, 2, 2), 0, 0));
        jBasicPane.add(ProgramBox, new GridBagConstraints(1, 13, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(12, 2, 2, 2), 0, 0));
        jBasicPane.add(SourceBoxX, new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 2, 2, 2), 0, 0));
        jBasicPane.add(ChrBoxX, new GridBagConstraints(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jBasicPane.add(jLabel5, new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 20, 0));
        jBasicPane.add(jLabel2, new GridBagConstraints(0, 13, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(12, 5, 2, 5), 0, 0));
        jBasicPane.add(ChrBoxY, new GridBagConstraints(0, 10, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jBasicPane.add(runButton, new GridBagConstraints(0, 14, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(8, 0, 0, 0), 0, 0));
        jBasicPane.add(FromX, new GridBagConstraints(1, 5, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jBasicPane.add(ToX, new GridBagConstraints(1, 6, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
        jBasicPane.add(jLabel6, new GridBagConstraints(0, 7, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(8, 2, 2, 2), 0, 0));
        jBasicPane.add(jLabel10, new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 20, 0));
        jBasicPane.add(jLabel8, new GridBagConstraints(0, 11, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 20, 0));
        jBasicPane.add(ToY, new GridBagConstraints(1, 12, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 0, 2), 0, 0));

        jAdvancedPane.add(jListAnnotation, new XYConstraints(9, 39, 247, 321));

        jTabbedPane1.add(jBasicPane, "Input");
        jTabbedPane1.add(jAdvancedPane, "Annotation");

        this.add(jTabbedPane1, BorderLayout.CENTER);
    }

    /**
     * ******************************************************
     */
    void initSourceSelection(String genome_name, JComboBox CBox) {
        int i, n;

        // first clear the selection box
        n = CBox.getItemCount();
        for (i = n - 1; i >= 0; i--) {
            CBox.removeItemAt(i);
        }

        if (genome_name == null) {
            CBox.addItem("Source not selected");
        } else {
            for (i = 0; i < Genome.getChrNum(genome_name); i++) {
                CBox.addItem(Genome.getChrName(genome_name, i));
            }
        }
    }

    /**
     * ******************************************************
     */
    void browserButtonX_actionPerformed(ActionEvent e) {

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open FASTA file");
        int returnVal = fc.showOpenDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileX = fc.getSelectedFile().getAbsolutePath();
            FromX.setValue(new Integer(1));
            ToX.setValue(new Integer(checkLocalFile(FileX)));
            XSourceDAS = false;

            // checking the file and writting the information into the project and windows
            jTextAreaX.setText("File :\n" + FileX);
        }
    }

    void browserButtonY_actionPerformed(ActionEvent e) {

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open FASTA file");
        int returnVal = fc.showOpenDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileY = fc.getSelectedFile().getAbsolutePath();
            FromY.setValue(new Integer(1));
            ToY.setValue(new Integer(checkLocalFile(FileY)));
            YSourceDAS = false;

            // checking the file and writting the information into the project and windows
            jTextAreaY.setText("File :\n" + FileY);
        }
    }

    /**
     * ************************************
     */
    void selectBoxX_actionPerformed(ActionEvent e) {
        int box_sel = SourceBoxX.getSelectedIndex();
        if (box_sel == 1) {
            // Disabling genome related
            ChrBoxX.setVisible(false);
            ButtonBrowseX.setVisible(true);
            XSourceDAS = false;
            genomex = "file";
        } else {
            // Unabling disabled if they are
            ButtonBrowseX.setVisible(false);
            ChrBoxX.setVisible(true);
            XSourceDAS = true;
            switch (box_sel) {
                case 2: /* human */
                    genomex = "hg16";
                    break;
                case 3: /* mouse */
                    genomex = "mm4";
                    break;
                case 4: /* fugu */
                    genomex = "rn3";
                    break;
                case 5: /* Chimpanzee */
                    genomex = "panTro1";
                    break;
                default:
                    genomex = null;
                    return;
            }
        }
        initSourceSelection(genomex, ChrBoxX);
    }

    void selectBoxY_actionPerformed(ActionEvent e) {

        int box_sel = SourceBoxY.getSelectedIndex();
        if (box_sel == 1) {
            // Disabling genome related
            ChrBoxY.setVisible(false);
            ButtonBrowseY.setVisible(true);
            YSourceDAS = false;
            genomey = "file";
        } else {
            // Unabling disabled if they are
            ButtonBrowseY.setVisible(false);
            ChrBoxY.setVisible(true);
            YSourceDAS = true;
            switch (box_sel) {
                case 2: /* human */
                    genomey = "hg16";
                    break;
                case 3: /* mouse */
                    genomey = "mm4";
                    break;
                case 4: /* fugu */
                    genomey = "rn3";
                    break;
                case 5: /* Chimpanzee */
                    genomey = "panTro1";
                    break;
                default:
                    genomey = null;
                    return;
            }
        }
        initSourceSelection(genomey, ChrBoxY);
    }

    /**
     * *****************************************************
     */
    private void writeSequenceToStream(String Fil, FileOutputStream out) {
        File f = new File(Fil);
        int size = (int) f.length();
        byte[] data = new byte[size];

        try {
            FileInputStream fis = new FileInputStream(Fil);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);
            dis.read(data);
            String datastr = new String(data);
            dis.close();
            out.write(datastr.getBytes());
        } catch (IOException iox) {
            System.out.println("File read error...");
            iox.printStackTrace();
            return;
        }
    }

    /**
     * *****************************************************
     */
    private int checkLocalFile(String Fil) {
        File f = new File(Fil);
        int size = (int) f.length();
        int len = 0;
        String datastr = null;
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
            return -1;
        }
        int i = datastr.indexOf(">", 0);
        i = datastr.indexOf("\n", i);

        for (len = 0; i < size; i++) {
            if (datastr.charAt(i) > ' ') {
                len++;

            }
        }
        return len;
    }

    /**
     * *****************************************************
     */
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

    /**
     * *****************************************************
     */
    void runButton_actionPerformed(ActionEvent e) {
        int fx, tx, fy, ty, i, j;
        FileOutputStream fout;
        String out_name = null;
        String job_id = null;
        String res_name;

        runButton.setBackground(Color.gray);

        fx = ((Integer) FromX.getValue()).intValue();
        tx = ((Integer) ToX.getValue()).intValue();
        fy = ((Integer) FromY.getValue()).intValue();
        ty = ((Integer) ToY.getValue()).intValue();

        // Checking for correct start/end here !!!!


        // Here should be choosing of the program  to run
        // SOAP part

        job_id = new String("Synteny_" + Math.rint(Math.random() * 1000000));
        out_name = new String(tempDir + job_id + ".sub");
        res_name = new String(tempDir + job_id + ".res");

        try {
            fout = new FileOutputStream(out_name);
            String tmp;
            tmp = new String("JOB_ID: " + job_id + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("GENOME1: " + genomex + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("CHR1: " + (String) ChrBoxX.getSelectedItem() + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("FROM1: " + fx + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("TO1: " + tx + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("GENOME2: " + genomey + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("CHR2: " + (String) ChrBoxY.getSelectedItem() + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("FROM2: " + fy + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("TO2: " + ty + "\n");
            fout.write(tmp.getBytes());

            if (genomex.compareTo("file") == 0) {
                tmp = new String("SEQ1:\n");
                fout.write(tmp.getBytes());
                writeSequenceToStream(FileX, fout);
                tmp = new String("///\n");
                fout.write(tmp.getBytes());
            }
            if (genomey.compareTo("file") == 0) {
                tmp = new String("SEQ2:\n");
                fout.write(tmp.getBytes());
                writeSequenceToStream(FileY, fout);
                tmp = new String("///\n");
                fout.write(tmp.getBytes());
            }
            fout.flush();
            fout.close();
        } catch (IOException ioe) {
            return;
        }

        try {
            SoapClient sp = new SoapClient();
            String infile = new String(sp.submitFile(out_name));

            String result_file = new String("/users/amdecweb/jakarta-tomcat-4.1.30/bin/outputFolder/" + job_id + ".res");
            String job_string = new String(sp.submitJob("java -cp /adtera/users/pavel/synteny_remote/ SyntenyServerSide", infile, result_file));

            i = 0;
            while (sp.isJobFinished(job_id + ".res") == false) {
                Delay(1000);
                if (i++ > 100) {
                    break;
                }
            }
            String tURL = new String("http://amdec-bioinfo.cu-genome.org/html/temp/" + job_id + ".res");

            DAS_Retriver.GetItToFile(tURL, res_name);
        } catch (Exception ee) {
            return;
        }


        /**/

        // DAS_Retriver.GetItToFile("http://amdec-bioinfo.cu-genome.org/html/temp/Synteny_706981.0.res", res_name);

        // Third - parsing the results
        SequenceAnnotation AnnoX = new SequenceAnnotation();
        SequenceAnnotation AnnoY = new SequenceAnnotation();

        AnnoX.setSeqSegmentStart(fx);
        AnnoX.setSeqSegmentEnd(tx);
        AnnoY.setSeqSegmentStart(fy);
        AnnoY.setSeqSegmentEnd(ty);

        if (XSourceDAS) {
            GPAnnoParser.runGPAnnoParser(AnnoX, res_name, 1);
            PFPParser.runPFPParser(AnnoX, res_name, 1);
            AnnoX.SetColors(AnnoKeys, NumAnnoKeys);
        }
        if (YSourceDAS) {
            GPAnnoParser.runGPAnnoParser(AnnoY, res_name, 2);
            PFPParser.runPFPParser(AnnoY, res_name, 2);
            AnnoY.SetColors(AnnoKeys, NumAnnoKeys);
        }

        // Activating the annotations
        for (i = 0; i < AnnoX.getAnnotationTrackNum() && i < 31; i++) {
            for (j = 0; j < knownAnnoNum; j++) {
                if (AnnoKeys[j].compareTo(AnnoX.getAnnotationTrack(i).getAnnotationName()) == 0) {
                    AnnoX.setAnnoTrackActive(i, true);
                }
            }
        }
        for (i = 0; i < AnnoY.getAnnotationTrackNum() && i < 31; i++) {
            for (j = 0; j < knownAnnoNum; j++) {
                if (AnnoKeys[j].compareTo(AnnoY.getAnnotationTrack(i).getAnnotationName()) == 0) {
                    AnnoY.setAnnoTrackActive(i, true);
                }
            }
        }

        // Read dot matrix
        DotsParser dp = null;
        if (ProgramBox.getSelectedIndex() == 1) {
            dp = new DotsParser(res_name);
        }

        dp.dm.setStartX(fx);
        dp.dm.setStartY(fy);
        dp.dm.setEndX(tx);
        dp.dm.setEndY(ty);

        DotMatrixViewWidgetPanel dmView = new DotMatrixViewWidgetPanel(dp.dm, AnnoX, AnnoY);
        //            dmView.repaint();

        runButton.setBackground(Color.white);
        DotMatrixViewWidget.dmrepaint();
    }

    void ProgramBox_actionPerformed(ActionEvent e) {

    }
}

/**
 * <p>select Box X
 */
class SyntenyViewWidget_selectBoxX_actionAdapter implements java.awt.event.ActionListener {
    SyntenyViewWidget adaptee;

    SyntenyViewWidget_selectBoxX_actionAdapter(SyntenyViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.selectBoxX_actionPerformed(e);
    }
}

/**
 * <p>select Box Y
 */
class SyntenyViewWidget_selectBoxY_actionAdapter implements java.awt.event.ActionListener {
    SyntenyViewWidget adaptee;

    SyntenyViewWidget_selectBoxY_actionAdapter(SyntenyViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.selectBoxY_actionPerformed(e);
    }
}

/**
 * <p>Run Button
 */
class SyntenyViewWidget_runButton_actionAdapter implements java.awt.event.ActionListener {
    SyntenyViewWidget adaptee;

    SyntenyViewWidget_runButton_actionAdapter(SyntenyViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.runButton_actionPerformed(e);
    }
}

/**
 * <p>Browse Button X
 */
class SyntenyViewWidget_BrowseButtonX_actionAdapter implements java.awt.event.ActionListener {
    SyntenyViewWidget adaptee;

    SyntenyViewWidget_BrowseButtonX_actionAdapter(SyntenyViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.browserButtonX_actionPerformed(e);
    }
}

/**
 * <p>Browse Button Y
 */
class SyntenyViewWidget_BrowseButtonY_actionAdapter implements java.awt.event.ActionListener {
    SyntenyViewWidget adaptee;

    SyntenyViewWidget_BrowseButtonY_actionAdapter(SyntenyViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.browserButtonY_actionPerformed(e);
    }
}

class SyntenyViewWidget_ProgramBox_actionAdapter implements java.awt.event.ActionListener {
    SyntenyViewWidget adaptee;

    SyntenyViewWidget_ProgramBox_actionAdapter(SyntenyViewWidget adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.ProgramBox_actionPerformed(e);
    }
}
