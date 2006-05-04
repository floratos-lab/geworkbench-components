package org.geworkbench.components.alignment.synteny;

import javax.swing.*;
import java.awt.*;
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

public class PositionSubPanel
    extends JPanel {

    SpinnerModel modelF = new SpinnerNumberModel();

    JSpinner From = new JSpinner(modelF);
    JLabel jLabelFrom = new JLabel();
    JPanel CoordPanel=new JPanel(null);

    JButton left2Button = new JButton();
    JButton leftButton = new JButton();
    JButton rightButton = new JButton();
    JButton right2Button = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    String name=null;
    CardLayout cardLayout1 = new CardLayout();
    int increment=1000;
    int max=500000000;
    int min=1;

    void jbInit() throws Exception {

        CoordPanel.setBackground(Color.gray);
        CoordPanel.setMaximumSize(new Dimension(4000, 4000));
        CoordPanel.setMinimumSize(new Dimension(140, 10));
        CoordPanel.setPreferredSize(new Dimension(151, 30));
        CoordPanel.setRequestFocusEnabled(false);
        CoordPanel.setToolTipText("");
        CoordPanel.setLayout(gridBagLayout1);
        left2Button.setMaximumSize(new Dimension(10, 30));
        left2Button.setMinimumSize(new Dimension(10, 10));
        left2Button.setPreferredSize(new Dimension(10, 10));
        left2Button.setMargin(new Insets(0, 0, 0, 0));
        left2Button.addActionListener(new
                                    PositionSubPanel_Button2Left_actionAdapter(this));
        leftButton.setAlignmentX( (float) 0.5);
        leftButton.setMaximumSize(new Dimension(10, 30));
        leftButton.setMinimumSize(new Dimension(10, 10));
        leftButton.setPreferredSize(new Dimension(10, 10));
        leftButton.setMargin(new Insets(0, 0, 0, 0));
        leftButton.addActionListener(new
                                    PositionSubPanel_ButtonLeft_actionAdapter(this));
        right2Button.setMaximumSize(new Dimension(10, 30));
        right2Button.setMinimumSize(new Dimension(10, 10));
        right2Button.setPreferredSize(new Dimension(10, 10));
        right2Button.setMargin(new Insets(0, 0, 0, 0));
        right2Button.addActionListener(new
                                    PositionSubPanel_Button2Right_actionAdapter(this));
        rightButton.setMaximumSize(new Dimension(10, 30));
        rightButton.setMinimumSize(new Dimension(10, 10));
        rightButton.setPreferredSize(new Dimension(10, 10));
        rightButton.setMargin(new Insets(0, 0, 0, 0));
        rightButton.addActionListener(new
                                    PositionSubPanel_ButtonRight_actionAdapter(this));
        jLabelFrom.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelFrom.setHorizontalTextPosition(SwingConstants.CENTER);
        this.setLayout(cardLayout1);
        jLabelFrom.setFont(new java.awt.Font("Dialog", 1, 11));
        jLabelFrom.setForeground(Color.white);
        jLabelFrom.setText(name);

        jLabelFrom.setFont(new java.awt.Font("Dialog", 1, 11));
        jLabelFrom.setForeground(Color.white);
        jLabelFrom.setMaximumSize(new Dimension(100, 100));
        jLabelFrom.setMinimumSize(new Dimension(40, 15));
        jLabelFrom.setPreferredSize(new Dimension(40, 15));

        /* Setting specific values for demonstration */
        /*~~~~~~~~~~~~~~~~~~~~~*/
        From.setValue(new Integer(1));
        From.setMinimumSize(new Dimension(80, 10));
        From.setPreferredSize(new Dimension(80, 15));
        From.setToolTipText("Select position");
        left2Button.setText("<<");
        leftButton.setText("<");
        right2Button.setText(">>");
        rightButton.setText(">");
        CoordPanel.add(jLabelFrom, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.WEST, GridBagConstraints.NONE,
            new Insets(67, 16, 68, 16), 0, 0));
        CoordPanel.add(jLabelFrom, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        this.add(CoordPanel, "CoordPanel");
        CoordPanel.add(jLabelFrom, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            , GridBagConstraints.CENTER, GridBagConstraints.NONE,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(leftButton, new GridBagConstraints(2, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(From, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(left2Button, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(rightButton, new GridBagConstraints(4, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(right2Button, new GridBagConstraints(5, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        CoordPanel.add(jLabelFrom, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            , GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
    }

    public PositionSubPanel(String nm) {
        name=new String(nm);

        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setValue(int fr){
        From.setValue(new Integer(fr));
    }

    public int getValue(){
        return ( (Integer) From.getValue()).intValue();
    }

    public void setMax(int m){
        max=m;
    }

    public void setMin(int m){
        min=m;
    }

    public void setIncrement(int im){
        increment=im;
    }

    public void leftButton_actionPerformed(ActionEvent e){
        int cv= ( (Integer) From.getValue()).intValue();
        cv=cv-increment/2;
        if(cv<min)cv=min;
        From.setValue(new Integer(cv));
    }

    public void left2Button_actionPerformed(ActionEvent e){
        int cv= ( (Integer) From.getValue()).intValue();
        cv=cv-increment;
        if(cv<min)cv=min;
        From.setValue(new Integer(cv));
    }

    public void rightButton_actionPerformed(ActionEvent e){
        int cv= ( (Integer) From.getValue()).intValue();
        cv=cv+increment/2;
        if(cv<min)cv=min;
        From.setValue(new Integer(cv));
    }
    public void right2Button_actionPerformed(ActionEvent e){
        int cv= ( (Integer) From.getValue()).intValue();
        cv=cv+increment;
        if(cv<min)cv=min;
        From.setValue(new Integer(cv));
    }
}

class PositionSubPanel_ButtonLeft_actionAdapter
    implements java.awt.event.ActionListener {
    PositionSubPanel adaptee;

    PositionSubPanel_ButtonLeft_actionAdapter(PositionSubPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.leftButton_actionPerformed(e);
    }
}

class PositionSubPanel_Button2Left_actionAdapter
    implements java.awt.event.ActionListener {
    PositionSubPanel adaptee;

    PositionSubPanel_Button2Left_actionAdapter(PositionSubPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.left2Button_actionPerformed(e);
    }
}

class PositionSubPanel_ButtonRight_actionAdapter
    implements java.awt.event.ActionListener {
    PositionSubPanel adaptee;

    PositionSubPanel_ButtonRight_actionAdapter(PositionSubPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.rightButton_actionPerformed(e);
    }
}

class PositionSubPanel_Button2Right_actionAdapter
    implements java.awt.event.ActionListener {
    PositionSubPanel adaptee;

    PositionSubPanel_Button2Right_actionAdapter(PositionSubPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.right2Button_actionPerformed(e);
    }
}
