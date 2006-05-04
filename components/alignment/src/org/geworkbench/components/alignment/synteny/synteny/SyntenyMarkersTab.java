package org.geworkbench.components.alignment.synteny;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import org.geworkbench.events.listeners.GeneSelectorListener;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class SyntenyMarkersTab
    extends JPanel /* implements GeneSelectorListener */ {
    /**
     * Returns the component name that gets displayed in the Tabbed interface
     * @return a string with the component name
     */
    private JPanel mainPanel = new JPanel();
//    private IMarkerPanel markers = null;
    private DefaultListModel ls1 = new DefaultListModel();
    private DefaultListModel ls2 = new DefaultListModel();
    private JList jSelectedList = new JList();
    BorderLayout borderLayout1 = new BorderLayout();

    public Component getComponent() {
        return mainPanel;
    }

//        public void geneSelectorAction(GeneSelectorEvent e) {
//            markers = e.getPanel();
//            if (markers != null) {
//                ls2.clear();
//                for (int j = 0; j < markers.getSubPanelNo(); j++) {
//                    IMarkerSimplePanel mrk = markers.getSubPanel(j);
//                    for (int i = 0; i < mrk.getGenericMarkerNo(); i++) {
//                        ls2.addElement(mrk.getGenericMarker(i));
//                    }
//                }
//            }
//    }

    public SyntenyMarkersTab() {
        try {
            jbInit();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        LayoutManager borderLayout1 = null;
        mainPanel.setLayout(borderLayout1);
        jSelectedList.setBorder(BorderFactory.createEtchedBorder());

        jSelectedList.setModel(ls2);

        ActionListener listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        };
        Object listeners = null;

        mainPanel.add(jSelectedList, java.awt.BorderLayout.CENTER);
    }

}
