package org.geworkbench.components.alignment.panels;

import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


import javax.swing.border.Border;
import java.net.*;
import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.util.session.SoapClient;
import org.geworkbench.components.alignment.synteny.DAS_Retriver;

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

public class SyntenyDotMatrixParameters extends JPanel {

    SequenceAnnotation AnnoX = null;
    SequenceAnnotation AnnoY = null;
    SyntenyAnnotationParameters SAnnoPar=null;
    public SyntenyPresentationsList SPL = null;
    public String CurrentProgram=new String();
    private JComboBox ProgramBox = null;

    JPanel jDotMatrixPane = new JPanel();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel6 = new JLabel();
    JButton runButton = new JButton();
    String FileX;
    String FileY;
    String tempDir = System.getProperty("temporary.files.directory");

    String genomex = null, genomey = null;

    boolean XSourceDAS = false;
    boolean YSourceDAS = false;
    String[] AnnoKeys = {
        "PFP", "affyU133", "affyU95", "affyGnf1h", "ECgene",
        "ensGene", "genscan", "softberryGene", "geneid", "cytoBand",
        "cytoBandIdeo", "fosEndPairs", "gc5Base", "vegaGene", "HInvGeneMrna",
        "est", "intronEst", "mrna", "mzPt1Mm3Rn3Gg2_pHMM", "genomicSuperDups",
        "recombRate", "regPotential2X", "regPotential3X", "rnaCluster",
        "sgpGene",
        "snpMap", "tfbsCons", "vegaPseudoGene", "xenoEst", "xenoMrna",
        "celeraCoverage", "celeraDupPositive", "celeraOverlay", "bacEndPairs",
        "acembly"
    };

    int[] ActiveAnnotaton = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    int NumAnnoKeys = 35;

    BorderLayout borderLayout1 = new BorderLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    GridBagLayout gridBagLayout2 = new GridBagLayout();

    Border border1 = BorderFactory.createEmptyBorder();
    Border border2 = BorderFactory.createEmptyBorder();
    GridBagLayout gridBagLayout3 = new GridBagLayout();
    JLabel ProcessStatus = new JLabel();
    GenomePositionSubPanel GPosX;
    GenomePositionSubPanel GPosY;
    CardLayout cardLayout1 = new CardLayout();
    CardLayout cardLayout2 = new CardLayout();

    public SyntenyDotMatrixParameters() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public SyntenyDotMatrixParameters(SyntenyAnnotationParameters ap) {
        SAnnoPar = ap;
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Component getComponent() {
                return this;
    }

    void jbInit() throws Exception {
        SyntenyMarkersTab jMarkersPane = new SyntenyMarkersTab();
        GPosX=new GenomePositionSubPanel();
        GPosY=new GenomePositionSubPanel();
        jDotMatrixPane.setLayout(gridBagLayout1);
        jLabel1.setFont(new java.awt.Font("Dialog", 1, 11));
        jLabel1.setForeground(Color.white);
        jLabel1.setText("  Sequence along X axis");

        jLabel6.setEnabled(true);
        jLabel6.setFont(new java.awt.Font("Dialog", 1, 11));
        jLabel6.setForeground(Color.white);
        jLabel6.setText("  Sequence along Y axis");

        runButton.setBackground(Color.white);
        runButton.setBorder(BorderFactory.createLineBorder(Color.black));
        runButton.setMaximumSize(new Dimension(33, 26));
        runButton.setMinimumSize(new Dimension(33, 26));
        runButton.setPreferredSize(new Dimension(33, 26));
        runButton.setFocusPainted(true);
        runButton.setMargin(new Insets(2, 14, 2, 14));
        runButton.setText("R U N");
        runButton.addActionListener(new
                                    SyntenyDotMatrixParameters_runButton_actionAdapter(this)); // Forming select boxes

        GPosY.setPosFrom(97750000);
        GPosY.setPosTo(97754800);
        GPosX.setGenome(2);
        GPosX.setChromosome(21);
        GPosY.setChromosome(2);
        GPosX.setPosFrom(15531500);
        GPosX.setPosTo(15536000);
        GPosY.setBorder(BorderFactory.createLineBorder(Color.black));
        GPosY.setMaximumSize(new Dimension(390, 3647));
        GPosY.setGenome(2);

        GPosY.setMinimumSize(new Dimension(140, 51));
        GPosY.setPreferredSize(new Dimension(145, 51));
        GPosY.setLayout(cardLayout2);

        jDotMatrixPane.setBackground(SystemColor.desktop);
        jDotMatrixPane.setForeground(Color.black);
        jDotMatrixPane.setAlignmentX( (float) 0.5);
        jDotMatrixPane.setMinimumSize(new Dimension(330, 363));
        jDotMatrixPane.setPreferredSize(new Dimension(329, 265));
        jDotMatrixPane.setToolTipText("");
        ProcessStatus.setText(" ");
        ProcessStatus.setOpaque(true);
        ProcessStatus.setBackground(Color.white);
        ProcessStatus.setForeground(Color.blue);
        ProcessStatus.setToolTipText("Process status");
        ProcessStatus.setHorizontalAlignment(SwingConstants.CENTER);
        ProcessStatus.setHorizontalTextPosition(SwingConstants.CENTER);
        this.setPreferredSize(new Dimension(350, 380));
        this.setToolTipText("");
        GPosX.setLayout(cardLayout1);
        GPosX.setBorder(BorderFactory.createLineBorder(Color.black));
        GPosX.setMaximumSize(new Dimension(390, 3647));
        GPosX.setMinimumSize(new Dimension(140, 51));
        GPosX.setPreferredSize(new Dimension(145, 51));
        GPosX.setToolTipText("");
        this.add(jDotMatrixPane, "Genome Segments");
        jDotMatrixPane.add(jLabel1, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        jDotMatrixPane.add(GPosX, new GridBagConstraints(0, 2, 2, 2, 2.0, 2.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        jDotMatrixPane.add(GPosY, new GridBagConstraints(0, 5, 2, 2, 2.0, 2.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        jDotMatrixPane.add(runButton,
                           new GridBagConstraints(0, 7, 1, 1, 1.0, 1.0
                                                  , GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(8, 1, 1, 1), 0, 0));
        jDotMatrixPane.add(ProcessStatus,
                           new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                                  , GridBagConstraints.CENTER,
                                                  GridBagConstraints.BOTH,
                                                  new Insets(0, 0, 0, 0), 0, 0));
        jDotMatrixPane.add(jLabel6, new GridBagConstraints(0, 4, 2, 1, 1.0, 1.0
                , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
    }

    public void setCurrentProgram(String cp){
        CurrentProgram=new String(cp);
    }

    /**********************************************************/
    public void setSyntenyPresentationsList(SyntenyPresentationsList sl){
        SPL=sl;
    }

    /*********************************************************/
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
            int to = 0;
            int from = 0;
            from = datastr.indexOf('>', 0);
            to = datastr.indexOf('>', from + 1);

            if (to != -1) {
                datastr = new String(datastr.substring(from, to));
            }
            if (to == -1) {
                datastr = new String(datastr.substring(from));
            }

            dis.close();
            out.write(datastr.getBytes());
        }
        catch (IOException iox) {
            System.out.println("File read error...");
            iox.printStackTrace();
            return;
        }
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

    /*********************************************************/
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
        if ( GPosX.isRemote() ) {
            if (datastr.indexOf("/# Start of Annotation 1 #/") == -1) {
                return false;
            }
            if (datastr.indexOf("/# End of Annotation 1 #/") == -1) {
                return false;
            }
        }
        if (GPosY.isRemote()) {
            if (datastr.indexOf("/# Start of Annotation 2 #/") == -1) {
                return false;
            }
            if (datastr.indexOf("/# End of Annotation 2 #/") == -1) {
                return false;
            }
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
    void AdjustActiveAnnoTracks(SequenceAnnotation Anno) {
        int i, j;
        int real_an = Anno.getAnnotationTrackNum();

        for (i = 0; i < real_an; i++) {
            for (j = 0; j < NumAnnoKeys; j++) {
                if (AnnoKeys[j].compareTo(Anno.getAnnotationTrack(i).
                                          getAnnotationName()) == 0) {
                    if (ActiveAnnotaton[j] == 1) {
                        Anno.setAnnoTrackActive(i, true);
                    }
                    else {
                        Anno.setAnnoTrackActive(i, false);
                    }
                }
            }
        }
    }

 /************************************************************/
 private void getAnnoTest() {
String str=null;
     try
     {
         URL url = new URL("http://genome.cse.ucsc.edu/cgi-bin/das/hg16/features?segment=chr17:15531500,15536000;");
         BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
         in.close();
     }
     catch (MalformedURLException e) {}
     catch (IOException e) {}
}




    /*********************************************************/
    void runButton_actionPerformed(ActionEvent e) {
        int fx, tx, fy, ty, i, j;

        FileOutputStream fout;
        String out_name = null;
        String job_id = null;
        String res_name;
        final boolean debuging = true;

        fx = GPosX.getValueFrom();
        tx = GPosX.getValueTo();
        if(fx>=tx){
            ProcessStatus.setText("Wrong X positions");
            return;
        }

        fy = GPosY.getValueFrom();
        ty = GPosY.getValueTo();
        if(fy>=ty){
            ProcessStatus.setText("Wrong Y positions");
            return;
        }

        job_id = new String("Synteny_" + Math.rint(Math.random() * 1000000));
        out_name = new String(tempDir + job_id + ".sub");
        res_name = new String(tempDir + job_id + ".res");

        try {
            fout = new FileOutputStream(out_name);
            String tmp;
            tmp = new String("JOB_ID: " + job_id + "\n");
            fout.write(tmp.getBytes());

            tmp = new String("PROGRAM: " + (String)ProgramBox.getSelectedItem() + "\n");
            fout.write(tmp.getBytes());

            tmp = new String("REQUEST_TYPE: DOTMATRIX\n");
            fout.write(tmp.getBytes());

            String genome=new String(GPosX.getGenome());
            if(genome == null){
                ProcessStatus.setText("Wrong X genome");
                return;
            }

            tmp = new String("GENOME1: " + genome + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("CHR1: " + GPosX.getChromosome() +
                             "\n");
            fout.write(tmp.getBytes());
            tmp = new String("FROM1: " + fx + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("TO1: " + tx + "\n");
            fout.write(tmp.getBytes());
            genome=new String(GPosY.getGenome());
            if(genome==null){
                ProcessStatus.setText("Wrong Y genome");
                return;
            }
            tmp = new String("GENOME2: " + genome + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("CHR2: " + GPosY.getChromosome() +
                             "\n");
            fout.write(tmp.getBytes());
            tmp = new String("FROM2: " + fy + "\n");
            fout.write(tmp.getBytes());
            tmp = new String("TO2: " + ty + "\n");
            fout.write(tmp.getBytes());

            if (GPosX.isLocal()) {
                tmp = new String("SEQ1:\n");
                fout.write(tmp.getBytes());
                writeSequenceToStream(GPosX.getChromosome(), fout);
                tmp = new String("///\n");
                fout.write(tmp.getBytes());
            }
            if (GPosY.isLocal()) {
                tmp = new String("SEQ2:\n");
                fout.write(tmp.getBytes());
                writeSequenceToStream(GPosY.getChromosome(), fout);
                tmp = new String("///\n");
                fout.write(tmp.getBytes());
            }
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
                runButton.setBackground(Color.gray);
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
                    // Read dot matrix
                    SPL.addAndDisplay(resn,f_x,t_x,f_y,t_y);
                    ProcessStatus.setText("Done");
                }
                runButton.setBackground(Color.white);
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    public void setAnnottionParameters(SyntenyAnnotationParameters SAP){
        SAnnoPar=SAP;
    }

    public void setProgramBox(JComboBox pb){
        ProgramBox = pb;
    }

}

/**
 * <p>Run Button
 */
class SyntenyDotMatrixParameters_runButton_actionAdapter
    implements java.awt.event.ActionListener {
    SyntenyDotMatrixParameters adaptee;

    SyntenyDotMatrixParameters_runButton_actionAdapter(SyntenyDotMatrixParameters adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.runButton_actionPerformed(e);
    }
}

