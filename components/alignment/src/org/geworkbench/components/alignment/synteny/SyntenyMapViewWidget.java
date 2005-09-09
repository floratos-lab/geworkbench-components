package org.geworkbench.components.alignment.synteny;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.*;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

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
public class SyntenyMapViewWidget extends JPanel{

    SyntenyMapObject smObj = null;
    private static SyntenyMapViewWidgetPanel smvwp = null;
    private JButton savePictButton = new JButton();
    private JScrollPane smScrollPane = new JScrollPane();
    BorderLayout borderLayout2 = new BorderLayout();
    JRadioButton toScaleSwitch = new JRadioButton();
    JPanel jPanel1 = new JPanel();
    JRadioButton jRadioButtonLegends = new JRadioButton();
    JRadioButton jRadioButtonScale = new JRadioButton();

    public SyntenyMapViewWidget() {

//        smObj = PopulateSyntenyMap();
        smvwp = new SyntenyMapViewWidgetPanel(null);

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        smScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                                 VERTICAL_SCROLLBAR_ALWAYS);
        smScrollPane.setAutoscrolls(true);
        smScrollPane.setPreferredSize(new Dimension(600, 600));

        this.setLayout(borderLayout2);
        toScaleSwitch.setText("To Scale");
        jRadioButtonLegends.setSelected(true);
        jRadioButtonLegends.setText("Legends");
        jRadioButtonScale.setSelected(true);
        jRadioButtonScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                smvwp.scaledPicture = ! smvwp.scaledPicture;
                smvwp.repaint();
            }
        });
        jRadioButtonLegends.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                smvwp.showLegends = ! smvwp.showLegends;
                smvwp.repaint();
            }
        });

        jRadioButtonScale.setText("Scale");
        this.add(smScrollPane, java.awt.BorderLayout.CENTER);

        savePictButton.setText("Save Image");
        savePictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jSaveButton_actionPerformed(e);
            }
        });

        smScrollPane.getViewport().add(smvwp);
        this.add(jPanel1, java.awt.BorderLayout.SOUTH);
        jPanel1.add(jRadioButtonScale);
        jPanel1.add(jRadioButtonLegends);
        jPanel1.add(savePictButton);
    }


    public static void smrepaint(SyntenyMapObject smo) {
        smvwp.setNewData(smo);
        smvwp.repaint();
    }

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

    void jSaveButton_actionPerformed(ActionEvent e){
        smvwp.saveToJpeg();
    }
}



