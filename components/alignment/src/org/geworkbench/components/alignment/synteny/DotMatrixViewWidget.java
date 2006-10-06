package org.geworkbench.components.alignment.synteny;


import org.geworkbench.util.sequences.SequenceAnnotation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.MouseEvent;

/**
 * <p>Widget provides all GUI services for dot matrix panel displays.</p>
 * <p>Widget is controlled by its associated component, DotMatrixViewAppComponent</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Columbia Genome Center/Califano Lab</p>
 * @author
 * @version 1.0
 */

public class DotMatrixViewWidget
    extends JPanel {

    private BorderLayout borderLayout2 = new BorderLayout();
    private JScrollPane dotScrollPane = new JScrollPane();

    public static DotMatrixViewWidgetPanel dotViewWPanel=null;
    public static DotMatrixInfoPanel DMInfoPanel=null;
    public static AnnotationControlPanel ACPanel=null;

    private JToolBar jToolBar1 = new JToolBar();
    private JRadioButton showDirBtn = new JRadioButton();
    private JRadioButton showInvBtn = new JRadioButton();
    private JRadioButton showAnnoX = new JRadioButton();
    private JRadioButton showAnnoY = new JRadioButton();
    JPanel jPanel1 = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JButton saveButton = new JButton();

    public DotMatrixViewWidget() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void dmrepaint() {
        dotViewWPanel.repaint();
    }

    public static void showinfo(String inf) {
        DMInfoPanel.showInfo(inf);
    }

    public static void drawNewDotMatrix(DotMatrixObj dmo, SequenceAnnotation sax, SequenceAnnotation say) {
        dotViewWPanel.DMViewWidgetPaneladd(dmo, sax, say, DMInfoPanel);
        dotViewWPanel.repaint();
        dotViewWPanel.setPreferredSize(new Dimension(500, 800));
        DMInfoPanel.setTitle("X: " + " " + dmo.getDescriptionX() + "   Y: " + " " + dmo.getDescriptionY());
        ACPanel.setAnnotations(sax,say);
        ACPanel.setDotViewPanel(dotViewWPanel);
    }

    void jbInit() throws Exception {
        dotViewWPanel = new DotMatrixViewWidgetPanel();
        DMInfoPanel = new DotMatrixInfoPanel();
        ACPanel = new AnnotationControlPanel();

        this.setLayout(borderLayout2);
        dotScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                                 VERTICAL_SCROLLBAR_ALWAYS);
        dotScrollPane.setAutoscrolls(true);
        dotScrollPane.setOpaque(false);
        dotScrollPane.setPreferredSize(new Dimension(300, 300));

        dotViewWPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jDisplayPanel_mouseClicked(e);
            }
        });

        showDirBtn.setText("Direct");
        showDirBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonD_actionPerformed(e);
            }
        });
        showDirBtn.setSelected(true);

        showInvBtn.setText("Invert");
        showInvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonI_actionPerformed(e);
            }
        });
        showInvBtn.setSelected(true);

        showAnnoX.setText("Annotation X");
        showAnnoX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonAX_actionPerformed(e);
            }
        });
        showAnnoX.setSelected(true);

        showAnnoY.setText("Annotation Y");
        showAnnoY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonAY_actionPerformed(e);
            }
        });
        showAnnoY.setSelected(true);
        jPanel1.setLayout(borderLayout1);
        saveButton.setText("Save image");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jSaveButton_actionPerformed(e);
            }
        });

        jToolBar1.add(showDirBtn, null);
        jToolBar1.add(showInvBtn, null);
        jToolBar1.add(showAnnoX, null);
        jToolBar1.add(showAnnoY, null);
        jToolBar1.add(saveButton);
        this.add(dotScrollPane, java.awt.BorderLayout.CENTER);
        dotScrollPane.getViewport().add(dotViewWPanel);
//        dotScrollPane.getViewport().add(dotViewWPanel);
        this.add(jPanel1, java.awt.BorderLayout.NORTH);

        this.add(ACPanel, java.awt.BorderLayout.WEST);

        jPanel1.add(DMInfoPanel, java.awt.BorderLayout.CENTER);
        jPanel1.add(jToolBar1, java.awt.BorderLayout.NORTH);

        dotViewWPanel.setShowDirect(showDirBtn.isSelected());
        dotViewWPanel.setShowInverted(showInvBtn.isSelected());
    }

// --------------- sets all required session objects
    void jDisplayPanel_mouseClicked(MouseEvent e) {
        // return just x and y
        final Font font = new Font("Courier", Font.BOLD, 10);
        int x = e.getX();
        int y = e.getY();
    }

    void this_caretPositionChanged(InputMethodEvent e) {
    }

    public void deserialize(String filename) {
    }

// ----------------- Button actions
    void jToggleButtonD_actionPerformed(ActionEvent e) {
        if(dotViewWPanel.isInitiated()){
            dotViewWPanel.setShowDirect(showDirBtn.isSelected());
            dotViewWPanel.repaint();
        }
    }

    void jToggleButtonI_actionPerformed(ActionEvent e) {
        if(dotViewWPanel.isInitiated()){
            dotViewWPanel.setShowInverted(showInvBtn.isSelected());
            dotViewWPanel.repaint();
        }
    }

    void jToggleButtonAX_actionPerformed(ActionEvent e) {
        if(dotViewWPanel.isInitiated()){
            dotViewWPanel.setShowAnnoX(showAnnoX.isSelected());
            dotViewWPanel.repaint();
        }
    }

    void jToggleButtonAY_actionPerformed(ActionEvent e) {
        if(dotViewWPanel.isInitiated()){
            dotViewWPanel.setShowAnnoY(showAnnoY.isSelected());
            dotViewWPanel.repaint();
        }
    }

    void jSaveButton_actionPerformed(ActionEvent e){
        dotViewWPanel.saveToJpeg();
    }

}
