package org.geworkbench.components.alignment.synteny;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.*;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import org.geworkbench.components.alignment.panels.SynMapPresentationList;

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
public class SyntenyMapViewWidget extends JPanel {

    public SynMapPresentationList SMPList = null;
    private static SyntenyMapObject smObj = null;
    public static SyntenyMapViewWidgetPanel smvwp = null;
    public static JComboBox jDistanceBox = null;
    JButton savePictButton = new JButton();
    JScrollPane smScrollPane = new JScrollPane();
    BorderLayout borderLayout2 = new BorderLayout();
    JRadioButton jRadioButtonLegends = new JRadioButton();
    JRadioButton jRadioButtonScale = new JRadioButton();
    TitledBorder titledBorder1 = new TitledBorder("");
    JToolBar jToolBar1 = new JToolBar();
    Border border1 = BorderFactory.createEmptyBorder();
    JLabel DistanceLabel = new JLabel();

    public SyntenyMapViewWidget() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        jDistanceBox = new JComboBox();
        SMPList = new SynMapPresentationList();
        smvwp = new SyntenyMapViewWidgetPanel();
        smvwp.setPreferredSize(new Dimension(200, 200));
        smvwp.setWidgetAddress(this);
        smScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                                VERTICAL_SCROLLBAR_ALWAYS);
        smScrollPane.setAutoscrolls(true);
        smScrollPane.setPreferredSize(new Dimension(600, 600));

        this.setLayout(borderLayout2);
        jRadioButtonLegends.setSelected(false);
        jRadioButtonLegends.setText("Legends");
        jRadioButtonScale.setSelected(true);
        jRadioButtonScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                smvwp.scaledPicture = !smvwp.scaledPicture;
                if (smObj != null) {
                    smvwp.repaint();
                }
            }
        });
        jRadioButtonLegends.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                smvwp.showLegends = !smvwp.showLegends;
                if (smObj != null) {
                    smvwp.repaint();
                }
            }
        });

        jRadioButtonScale.setText("Scale");
        savePictButton.setBorder(BorderFactory.createEtchedBorder());
        DistanceLabel.setText("  Distance: ");
        jDistanceBox.setPreferredSize(new Dimension(30, 10));
        jDistanceBox.setMaximumSize(new Dimension(130, 20));
        this.add(smScrollPane, java.awt.BorderLayout.CENTER);
        savePictButton.setText("Save Image");
        savePictButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jSaveButton_actionPerformed(e);
            }
        });
        jDistanceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jDistanceBox_actionPerformed(e);
            }
        });

        smScrollPane.getViewport().add(smvwp);
        this.add(jToolBar1, java.awt.BorderLayout.NORTH);
        jToolBar1.add(jRadioButtonScale);
        jToolBar1.add(jRadioButtonLegends);
        jToolBar1.add(savePictButton);
        jToolBar1.add(DistanceLabel);
        jToolBar1.add(jDistanceBox);
    }

    public static void smrepaint(SyntenyMapObject smo) {
        smObj=smo;
        populateDistanceBox(smo);
        smvwp.setNewData(smo);
        smvwp.repaint();
        smvwp.setPreferredSize(new Dimension(500, 800));
    }

    void jSaveButton_actionPerformed(ActionEvent e) {
        smvwp.saveToJpeg();
    }

    void jDistanceBox_actionPerformed(ActionEvent e){
        if(smObj != null){
            smObj.getFragment(smObj.getActiveFragmentNum()).setActiveWeightType(jDistanceBox.getSelectedIndex());
            System.out.println("jDistanceBox selection"+jDistanceBox.getSelectedIndex()+"Active fragment: "+smObj.getActiveFragmentNum()+"Active weight: " + smObj.getFragment(smObj.getActiveFragmentNum()).ActiveWeightType);
            smvwp.repaint();
        }
    }

    public static void drawNewSyntenyMap(SyntenyMapObject smo) {
        populateDistanceBox(smo);
        smObj=smo;
        smvwp.setNewData(smo);
        smvwp.repaint();
        smvwp.setPreferredSize(new Dimension(500, 800));
    }

    public static void populateDistanceBox(SyntenyMapObject smo){
        int n=smo.getFragment(smo.getActiveFragmentNum()).getWeightTypesNum();
        if(n!=0){
            jDistanceBox.removeAllItems();
            for (int i = 0; i < n; i++) {
                jDistanceBox.addItem(smo.getFragment(smo.getActiveFragmentNum()).
                                     getWeightName(i));
            }
        }
    }


}

