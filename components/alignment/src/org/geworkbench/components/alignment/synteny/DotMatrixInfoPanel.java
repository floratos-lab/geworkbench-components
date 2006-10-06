package org.geworkbench.components.alignment.synteny;

import javax.swing.*;
import java.awt.*;

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
public class DotMatrixInfoPanel extends JPanel {

    private BorderLayout borderLayout2 = new BorderLayout();
    JLabel jLabelDMTitle = new JLabel();
    JTextField jTextField1 = new JTextField();

    public DotMatrixInfoPanel() {

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {
        this.setLayout(borderLayout2);
        jLabelDMTitle.setText("No data to display");
        jTextField1.setBackground(Color.white);
        jTextField1.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        jTextField1.setBorder(BorderFactory.createEtchedBorder());
        jTextField1.setToolTipText(
            "Sequence position or annotation information");
        jTextField1.setEditable(false);
        jTextField1.setText("");
        this.add(jLabelDMTitle, java.awt.BorderLayout.CENTER);
        this.add(jTextField1, java.awt.BorderLayout.SOUTH);
    }

    public void setTitle(String ttl){
        jLabelDMTitle.setText(ttl);
    }

    public void showInfo(String inf){
        jTextField1.setText(inf);
    }

}
