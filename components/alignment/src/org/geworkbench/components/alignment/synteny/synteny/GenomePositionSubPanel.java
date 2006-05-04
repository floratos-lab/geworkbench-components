package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.Genome;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;


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


public class GenomePositionSubPanel
    extends JPanel {

    JPanel CoordPanel=new JPanel(null);
    CardLayout cardLayout1 = new CardLayout();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    PositionSubPanel fpsp = new PositionSubPanel("From:");
    PositionSubPanel tpsp = new PositionSubPanel("To:");
    JLabel jLabel6 = new JLabel();
    JComboBox SourceBox = new JComboBox();
    JComboBox ChrBox = new JComboBox();
    JButton ButtonBrowse = new JButton();
    String File;
    String genome = null;
    boolean SourceDAS = true;

    void jbInit() throws Exception {

        CoordPanel.setBackground(Color.gray);
        CoordPanel.setMaximumSize(new Dimension(4000, 4000));
        CoordPanel.setMinimumSize(new Dimension(140, 20));
        CoordPanel.setPreferredSize(new Dimension(151, 30));
        CoordPanel.setLayout(gridBagLayout1);

        ButtonBrowse.setText("Browse...");
        ButtonBrowse.addActionListener(new
                                        GenomePosSubPanel_BrowseButton_actionAdapter(this));

        SourceBox.addItem("Select Source:");
        SourceBox.addItem("File");
        SourceBox.addItem("human hg18 (GoldenPath)");
        SourceBox.addItem("mouse mm7 (GoldenPath)");
        SourceBox.addItem("rat rn3 (GoldenPath)");
        SourceBox.addItem("chimpanzee panTro1 (GoldenPath)");
        SourceBox.addActionListener(new
                                     GenomePosSubPanel_sourceBox_actionAdapter(this));
        initSourceSelection("hg18", ChrBox);
        SourceBox.setToolTipText("Select genome");
        ChrBox.setToolTipText("Select chromosome");
        ButtonBrowse.setVisible(false);
        ButtonBrowse.setMaximumSize(new Dimension(503, 190));
        ButtonBrowse.setPreferredSize(new Dimension(150, 19));
        ButtonBrowse.setMargin(new Insets(0, 0, 0, 0));
        this.setLayout(cardLayout1);
        fpsp.setMinimumSize(new Dimension(140, 15));
        fpsp.setPreferredSize(new Dimension(150, 20));
        tpsp.setMinimumSize(new Dimension(140, 15));
        tpsp.setPreferredSize(new Dimension(150, 20));
        tpsp.setToolTipText("");
        this.add(CoordPanel, "CoordPanel");
        ButtonBrowse.setVisible(false);

        CoordPanel.add(ChrBox, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(fpsp, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(tpsp, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(SourceBox, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(ButtonBrowse,
                       new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0
                                              , GridBagConstraints.CENTER,
                                              GridBagConstraints.BOTH,
                                              new Insets(0, 0, 0, 0), 0, 0));
    }

    /****************************************/
    public GenomePositionSubPanel() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * <p>Browse Button X
     */
    class GenomePosSubPanel_sourceBox_actionAdapter
        implements java.awt.event.ActionListener {
        GenomePositionSubPanel adaptee;

        GenomePosSubPanel_sourceBox_actionAdapter(GenomePositionSubPanel adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.sourceBox_actionPerformed(e);
        }
    }
    /****************************************/
    void sourceBox_actionPerformed(ActionEvent e) {
        int box_sel = SourceBox.getSelectedIndex();

        if (box_sel == 1) {
            ButtonBrowse.setVisible(true);
            ChrBox.setVisible(false);
            SourceDAS = false;
            genome = "file";
        }
        else {
            // Unabling disabled if they are
            ButtonBrowse.setVisible(false);
            ChrBox.setVisible(true);
            SourceDAS = true;
            switch (box_sel) {
                case 2: /* human */
                    genome = "hg18";
                    break;
                case 3: /* mouse */
                    genome = "mm7";
                    break;
                case 4: /* fugu */
                    genome = "rn3";
                    break;
                case 5: /* Chimpanzee */
                    genome = "panTro1";
                    break;
                default:
                    genome = null;
                    return;
            }
        }
        initSourceSelection(genome, ChrBox);
    }

    /**
     * <p>Browse Button X
     */
    class GenomePosSubPanel_BrowseButton_actionAdapter
        implements java.awt.event.ActionListener {
        GenomePositionSubPanel adaptee;

        GenomePosSubPanel_BrowseButton_actionAdapter(GenomePositionSubPanel adaptee) {
            this.adaptee = adaptee;
        }

        public void actionPerformed(ActionEvent e) {
            adaptee.browserButton_actionPerformed(e);
        }
    }
    /*********************************************************/
    void browserButton_actionPerformed(ActionEvent e) {

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Open FASTA file");
        int returnVal = fc.showOpenDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File = fc.getSelectedFile().getAbsolutePath();
            int sl = checkLocalFile(File);
            if (sl > 10) {
                ButtonBrowse.setText("Selected: " +
                                     fc.getSelectedFile().getName());
                fpsp.setValue(1);
                tpsp.setValue(sl);
            }
            else {
                System.out.println("Wrong input file...");
            }
            // checking the file and writting the information into the project and windows
        }
    }


    /**********************************************************/
    void initSourceSelection(String genome_name, JComboBox CBox) {
        int i, n;

        // first clear the selection box
        n = CBox.getItemCount();
        for (i = n - 1; i >= 0; i--) {
            CBox.removeItemAt(i);
        }

        if (genome_name == null) {
            CBox.addItem("Source not selected");
        }
        else {
            for (i = 0; i < Genome.getChrNum(genome_name); i++) {
                CBox.addItem(Genome.getChrName(genome_name, i));
            }
        }
    }

    /*********************************************************/
    private int checkLocalFile(String Fil) {

        File f = new File(Fil);
        int size = (int) f.length();
        int len;
        String datastr;
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
            return -1;
        }

        int i = datastr.indexOf(">", 0);
        i = datastr.indexOf("\n", i);

        for (len = 0; i < size; i++) {
            if (datastr.charAt(i) == '>' && datastr.charAt(i - 1) < ' ') {
                break;
            }
            if (datastr.charAt(i) > ' ') {
                len++;
            }
        }
        return len;
    }


    public String getChromosome(){
        if(SourceBox.getSelectedIndex()==1)
            return File;
        else
            return (String)ChrBox.getSelectedItem();
    }

    public String getGenome(){
        switch (SourceBox.getSelectedIndex()) {
            case 1: /* file */
                return "file";
            case 2: /* human */
                return "hg18";
            case 3: /* mouse */
                return "mm7";
            case 4: /* fugu */
                return "rn3";
            case 5: /* Chimpanzee */
                return "panTro1";
            case 0:
            default:
                return null;
        }
    }

    public void setGenome(int n){
        SourceBox.setSelectedIndex(n);
    }

    public void setChromosome(int n){
        ChrBox.setSelectedIndex(n);
    }

    public boolean isRemote(){
        if(SourceBox.getSelectedIndex() == 1)
            return false;
        return true;
    }

    public boolean isLocal(){
        if(SourceBox.getSelectedIndex() == 1)
            return true;
        return false;
    }
    public int getValueFrom(){
        return fpsp.getValue();
    }

    public int getValueTo(){
        return tpsp.getValue();
    }

    public void setPosFrom(int n){
        fpsp.setValue(n);
    }

    public void setPosTo(int n){
        tpsp.setValue(n);
    }

    public int askIncrement(){
        return 1000;
    }
}
