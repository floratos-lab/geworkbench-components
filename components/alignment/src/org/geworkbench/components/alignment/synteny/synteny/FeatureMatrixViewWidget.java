package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.PropertiesMonitor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class FeatureMatrixViewWidget
    extends JPanel {
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

    public static FeatureMatrixViewWidgetPanel featViewWPanel;

    //Models
    private PropertiesMonitor propertiesMonitor = null; //debug
    private JToolBar jToolBar1 = new JToolBar();
    private JToggleButton showDirBtn = new JToggleButton();

    public FeatureMatrixViewWidget() {

        try {
            jbInit();

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void fmrepaint() {
        featViewWPanel.repaint();
    }

    void jbInit() throws Exception {

        featViewWPanel = new FeatureMatrixViewWidgetPanel();
        propertiesMonitor = PropertiesMonitor.getPropertiesMonitor();

        this.setLayout(borderLayout2);

        jPanel1.setBorder(BorderFactory.createEtchedBorder());
        jPanel1.setMinimumSize(new Dimension(14, 25));
        jPanel1.setPreferredSize(new Dimension(14, 25));

        seqScrollPane.setBorder(BorderFactory.createEtchedBorder());
        featViewWPanel.addMouseListener(new java.awt.event.MouseAdapter() {
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

        showDirBtn.setText("Scaled");
        showDirBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jToggleButtonD_actionPerformed(e);
            }
        });
        showDirBtn.setSelected(true);




        this.add(jPanel1, BorderLayout.SOUTH);
        this.add(seqScrollPane, BorderLayout.CENTER);
        this.add(jToolBar1, BorderLayout.NORTH);
        jToolBar1.add(showDirBtn, null);
        seqScrollPane.getViewport().add(featViewWPanel, null);
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
    }
}
