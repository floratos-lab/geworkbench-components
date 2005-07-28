package org.geworkbench.components.alignment.synteny;

import org.geworkbench.components.alignment.synteny.*;
import org.geworkbench.components.alignment.synteny.DotMatrixViewWidgetPanel;
import org.geworkbench.util.PropertiesMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

/**
 * <p>Widget provides all GUI services for dot matrix panel displays.</p>
 * <p>Widget is controlled by its associated component, DotMatrixViewAppComponent</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Columbia Genome Center/Califano Lab</p>
 *
 * @author
 * @version 1.0
 */

public class DotMatrixViewWidget extends JPanel {
    private ActionListener listener = null;
    private final int xOff = 60;
    private final int yOff = 20;
    private final int xStep = 5;
    private final int yStep = 12;

    //Layouts
    private GridBagLayout gridBagLayout1 = new GridBagLayout();
    private GridBagLayout gridBagLayout2 = new GridBagLayout();
    private BorderLayout borderLayout1 = new BorderLayout();
    private BorderLayout borderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    //Panels and Panes
    private JPanel jPanel1 = new JPanel();
    private JScrollPane seqScrollPane = new JScrollPane();

    public static DotMatrixViewWidgetPanel dotViewWPanel;

    //  dotViewWPanel = new DotMatrixViewWidgetPanel();

    //Models
    private DotMatrixObj selectedPatterns = new DotMatrixObj();
    private org.geworkbench.util.PropertiesMonitor propertiesMonitor = null; //debug
    private JToolBar jToolBar1 = new JToolBar();
    private JToggleButton showDirBtn = new JToggleButton();
    private JToggleButton showInvBtn = new JToggleButton();
    private JToggleButton showAnnoX = new JToggleButton();
    private JToggleButton showAnnoY = new JToggleButton();

    public DotMatrixViewWidget() {

        try {
            jbInit();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void dmrepaint() {
        dotViewWPanel.repaint();
    }

    void jbInit() throws Exception {

        dotViewWPanel = new org.geworkbench.components.alignment.synteny.DotMatrixViewWidgetPanel();
        propertiesMonitor = org.geworkbench.util.PropertiesMonitor.getPropertiesMonitor();

        this.setLayout(borderLayout2);

        jPanel1.setBorder(BorderFactory.createEtchedBorder());
        jPanel1.setMinimumSize(new Dimension(14, 25));
        jPanel1.setPreferredSize(new Dimension(14, 25));

        seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
        dotViewWPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                jDisplayPanel_mouseClicked(e);
            }
        });
        this.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void inputMethodTextChanged(InputMethodEvent e) {
            }

            public void caretPositionChanged(InputMethodEvent e) {
                this_caretPositionChanged(e);
            }
        });
        this.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                this_propertyChange(e);
            }
        });

        showDirBtn.setText("Direct On/Off");
        showDirBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonD_actionPerformed(e);
            }
        });
        showDirBtn.setSelected(true);

        showInvBtn.setText("Invert On/Off");
        showInvBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonI_actionPerformed(e);
            }
        });
        showInvBtn.setSelected(true);

        showAnnoX.setText("Annotation X On/Off");
        showAnnoX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonAX_actionPerformed(e);
            }
        });
        showAnnoX.setSelected(true);

        showAnnoY.setText("Annotation Y On/Off");
        showAnnoY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonAY_actionPerformed(e);
            }
        });
        showAnnoY.setSelected(true);


        this.add(jPanel1, BorderLayout.SOUTH);
        this.add(seqScrollPane, BorderLayout.CENTER);
        this.add(jToolBar1, BorderLayout.NORTH);
        jToolBar1.add(showDirBtn, null);
        jToolBar1.add(showInvBtn, null);
        jToolBar1.add(showAnnoX, null);
        jToolBar1.add(showAnnoY, null);
        seqScrollPane.getViewport().add(dotViewWPanel, null);
        dotViewWPanel.setShowDirect(showDirBtn.isSelected());
        dotViewWPanel.setShowInverted(showInvBtn.isSelected());
    }

    //sets all required session objects
    void jDisplayPanel_mouseClicked(MouseEvent e) {
        // return just x and y
        final Font font = new Font("Courier", Font.BOLD, 10);
        int x = e.getX();
        int y = e.getY();
    }

    void this_caretPositionChanged(InputMethodEvent e) {
    }

    void this_propertyChange(PropertyChangeEvent e) {
    }

    public void deserialize(String filename) {
    }

    void jToggleButtonD_actionPerformed(ActionEvent e) {
        dotViewWPanel.setShowDirect(showDirBtn.isSelected());
        dotViewWPanel.repaint();
    }

    void jToggleButtonI_actionPerformed(ActionEvent e) {
        dotViewWPanel.setShowInverted(showInvBtn.isSelected());
        dotViewWPanel.repaint();
    }

    void jToggleButtonAX_actionPerformed(ActionEvent e) {
        dotViewWPanel.setShowAnnoX(showAnnoX.isSelected());
        dotViewWPanel.repaint();
    }

    void jToggleButtonAY_actionPerformed(ActionEvent e) {
        dotViewWPanel.setShowAnnoY(showAnnoY.isSelected());
        dotViewWPanel.repaint();
    }


}


