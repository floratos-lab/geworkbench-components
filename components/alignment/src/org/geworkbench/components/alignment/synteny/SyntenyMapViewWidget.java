package org.geworkbench.components.alignment.synteny;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
    JRadioButton jRadioButtonLegends = new JRadioButton();
    JRadioButton jRadioButtonScale = new JRadioButton();
    TitledBorder titledBorder1 = new TitledBorder("");
    JToolBar jToolBar1 = new JToolBar();
    Border border1 = BorderFactory.createEmptyBorder();

    public SyntenyMapViewWidget() {

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        smvwp = new SyntenyMapViewWidgetPanel();

        smScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                                 VERTICAL_SCROLLBAR_ALWAYS);
        smScrollPane.setAutoscrolls(true);
        smScrollPane.setPreferredSize(new Dimension(600, 600));

        this.setLayout(borderLayout2);
        toScaleSwitch.setText("To Scale");
        jRadioButtonLegends.setSelected(false);
        jRadioButtonLegends.setText("Legends");
        jRadioButtonScale.setSelected(true);
        jRadioButtonScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                smvwp.scaledPicture = ! smvwp.scaledPicture;
                if(smObj!=null)
                    smvwp.repaint();
            }
        });

        jRadioButtonLegends.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                smvwp.showLegends = ! smvwp.showLegends;
                if(smObj!=null)
                    smvwp.repaint();
            }
        });

        jRadioButtonScale.setText("Scale");
        savePictButton.setBorder(BorderFactory.createEtchedBorder());
        this.add(smScrollPane, java.awt.BorderLayout.CENTER);

        savePictButton.setText("Save Image");
        savePictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jSaveButton_actionPerformed(e);
            }
        });

        smScrollPane.getViewport().add(smvwp);
        this.add(jToolBar1, java.awt.BorderLayout.NORTH);
        jToolBar1.add(jRadioButtonScale);
        jToolBar1.add(jRadioButtonLegends);
        jToolBar1.add(savePictButton);
    }


    public static void smrepaint(SyntenyMapObject smo) {
//        smvwp = new SyntenyMapViewWidgetPanel();
        smvwp.setNewData(smo);
        smvwp.repaint();
        smvwp.setPreferredSize(new Dimension(500, 800));
    }

    void jSaveButton_actionPerformed(ActionEvent e){
        smvwp.saveToJpeg();
    }

    public static void drawNewSyntenyMap(SyntenyMapObject smo) {
    smvwp.setNewData(smo);
    smvwp.repaint();
    smvwp.setPreferredSize(new Dimension(500, 800));

//    DMInfoPanel.setTitle("X: "+" "+dmo.getDescriptionX()+"   Y: "+" "+dmo.getDescriptionY());

}

}



