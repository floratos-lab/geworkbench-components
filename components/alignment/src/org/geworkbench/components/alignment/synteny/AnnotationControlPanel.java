package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.util.Util;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.config.events.EventSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


public class AnnotationControlPanel extends JPanel implements VisualPlugin {

    ImageIcon addButtonIcon = Util.createImageIcon("/images/down.gif");
    ImageIcon removeButonIcon = Util.createImageIcon("/images/up.gif");
    private SequenceAnnotation SA_X = null;
    private SequenceAnnotation SA_Y = null;
    private JList list2 = new JList();
    private JList list1 = new JList();
    private DotMatrixViewWidgetPanel dVWPanel=null;

    private DefaultListModel list1_model = new DefaultListModel();
    private DefaultListModel list2_model = new DefaultListModel();

    private JButton button3 = new JButton();
    private JButton button2 = new JButton();
    private JButton button1 = new JButton();
    private JLabel LabelUp = new JLabel();
    private JLabel LabelDown = new JLabel();
    private GridBagLayout gbLayout = new GridBagLayout();

    private JScrollPane UpScrollPane = new JScrollPane();
    private JScrollPane DownScrollPane = new JScrollPane();

    public AnnotationControlPanel() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void jbInit() throws Exception {

        this.setLayout(gbLayout);
        this.setBorder(BorderFactory.createEtchedBorder());
        list1.setModel(list1_model);
        list2.setModel(list2_model);

        LabelUp.setText("Available annotations");
        LabelDown.setText("Active annotations");
        button1.setIcon(addButtonIcon);
        button1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ACP_add_Button_action(e);
            }
        });

        button2.setIcon(removeButonIcon);
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ACP_remove_Button_action(e);
            }
        });

        button3.setText("Load anno track");
        button2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ACP_load_Button_action(e);
            }
        });

        UpScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                VERTICAL_SCROLLBAR_ALWAYS);
        UpScrollPane.setAutoscrolls(true);
        UpScrollPane.setOpaque(false);
        UpScrollPane.setPreferredSize(new Dimension(300, 300));
        UpScrollPane.getViewport().add(list1);

        DownScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                VERTICAL_SCROLLBAR_ALWAYS);
        DownScrollPane.setAutoscrolls(true);
        DownScrollPane.setOpaque(false);
        DownScrollPane.setPreferredSize(new Dimension(300, 300));
        DownScrollPane.getViewport().add(list2);

        this.add(LabelUp, new GridBagConstraints(0, 0, 4, 1, 0.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 1, 1));
        this.add(UpScrollPane, new GridBagConstraints(0, 1, 4, 3, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 1, 1));
        this.add(button1, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 1, 1));
        this.add(button2, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 1, 1));
//        this.add(button3, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0
//                , GridBagConstraints.CENTER,
//                GridBagConstraints.NONE,
//                new Insets(0, 0, 0, 0), 1, 1));

        this.add(LabelDown, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 1, 1));

        this.add(DownScrollPane, new GridBagConstraints(0, 7, 4, 3, 1.0, 1.0
                , GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 1, 1));
    }

    public void setAnnotations(SequenceAnnotation sax, SequenceAnnotation say) {
        SA_X = sax;
        SA_Y = say;

        // ADDING FOR x ANNOTATION
        for (int i = 0; i < SA_X.getAnnotationTrackNum(); i++) {
            if (SA_X.getAnnoTrackActive(i)) {
                list2_model.add(list2_model.getSize(), SA_X.getAnnotationTrack(i).getAnnotationName());
            } else {
                list1_model.add(list1_model.getSize(), SA_X.getAnnotationTrack(i).getAnnotationName());
            }
        }
        // ADDING FOR y ANNOTATION
        for (int i = 0; i < SA_Y.getAnnotationTrackNum(); i++) {
            boolean flag = true;

            String atnm = SA_Y.getAnnotationTrack(i).getAnnotationName();
            if (SA_Y.getAnnoTrackActive(i)) {
                for (int j = 0; j < list2_model.getSize(); j++) {
                    if (atnm.indexOf((String) list2_model.elementAt(j)) == 0) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    list2_model.add(list2_model.getSize(), atnm);
                }
            } else {
                for (int j = 0; j < list1_model.getSize(); j++) {
                    if (atnm.indexOf((String) list1_model.elementAt(j)) == 0) {
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    list1_model.add(list1_model.getSize(), atnm);
                }
            }
        }
        super.repaint();
//        this.repaint();
    }

    void ACP_add_Button_action(ActionEvent e) {

        Object[] selected = list1.getSelectedValues();
        if (selected == null || selected.length == 0) return;

        int[] inds = list1.getSelectedIndices();
        for (int k = inds.length-1; k >=0 ; k--) list1_model.remove(inds[k]);

        for (int k = 0; k < selected.length; k++) {
            String sm = selected[k].toString();
            list2_model.add(list2_model.getSize(), sm);

            /* change active in Annotations */
            for (int i = 0; i < SA_X.getAnnotationTrackNum(); i++) {
                if (sm.indexOf(SA_X.getAnnotationTrack(i).getAnnotationName()) == 0) {
                    SA_X.setAnnoTrackActive(i, true);
                }
            }
            for (int i = 0; i < SA_Y.getAnnotationTrackNum(); i++) {
                if (sm.indexOf(SA_Y.getAnnotationTrack(i).getAnnotationName()) == 0) {
                    SA_Y.setAnnoTrackActive(i, true);
                }
            }
        }
        /* Redraw should be here */
        dVWPanel.repaint();
    }

    void ACP_remove_Button_action(ActionEvent e) {

        Object[] selected = list2.getSelectedValues();
        if (selected == null || selected.length == 0) return;

        int[] inds = list2.getSelectedIndices();
        for (int k = inds.length-1; k >=0 ; k--) list2_model.remove(inds[k]);

        for (int k = 0; k < selected.length; k++) {
            String sm = selected[k].toString();
            list1_model.add(list1_model.getSize(), sm);

            /* change active in Annotations */
            for (int i = 0; i < SA_X.getAnnotationTrackNum(); i++) {
                if (sm.indexOf(SA_X.getAnnotationTrack(i).getAnnotationName()) == 0) {
                    SA_X.setAnnoTrackActive(i, false);
                }
            }
            for (int i = 0; i < SA_Y.getAnnotationTrackNum(); i++) {
                if (sm.indexOf(SA_Y.getAnnotationTrack(i).getAnnotationName()) == 0) {
                    SA_Y.setAnnoTrackActive(i, false);
                }
            }
        }
        /* Redraw should be here */
        dVWPanel.repaint();
    }

    void ACP_load_Button_action(ActionEvent e) {
        /* Select file */
        
        /* Parse file */
        /* add to the tracks */
    }

    void setDotViewPanel(DotMatrixViewWidgetPanel dmvwp){
            dVWPanel=dmvwp;
    }

    /**
     * getComponent
     *
     * @return Component
     */
    public Component getComponent() {
        return this;
    }
}
