package org.geworkbench.components.selectors;

import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.bison.components.events.PanelSelectorEvent;
import org.geworkbench.bison.datastructure.complex.panels.CSAssayPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.LabelledObject;
import org.geworkbench.bison.datastructure.properties.DSNamed;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;

/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype
 * Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author Adam Margolin
 * @version 3.0
 */

public class PanelsEnabledSelectorPanel <T extends DSNamed> implements VisualPlugin {
    private HashMap listeners = new HashMap();
    private DSPanel<T> panel = new CSPanel<T>("Top Panel");

    private JTree panelTree = new JTree();
    private TreeSelectionModel panelTreeSelection = null;
    private PanelsEnabledTreeModel<T> panelTreeModel;

    private JPanel mainPanel = new JPanel();
    private JScrollPane jScrollPane2 = new JScrollPane();
    MouseListener panelTreeListener = null;

    JButton btnTest = new JButton();
    JPopupMenu jPanelMenu = new JPopupMenu();
    JMenuItem jActivateItem = new JMenuItem();
    JMenuItem jDeactivateItem = new JMenuItem();
    JMenuItem jDeleteItem = new JMenuItem();
    JMenuItem jRenameItem = new JMenuItem();
    JMenu mnuClassification = new JMenu();
    JCheckBoxMenuItem chkCase = new JCheckBoxMenuItem();
    JCheckBoxMenuItem chkControl = new JCheckBoxMenuItem();
    JCheckBoxMenuItem chkIgnore = new JCheckBoxMenuItem();

    /**
     * Standard constructor
     */
    public PanelsEnabledSelectorPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Default initialization method
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        panelTreeSelection = panelTree.getSelectionModel();
        panelTreeModel = new PanelsEnabledTreeModel<T>(panel);
        panelTree.setModel(panelTreeModel);

        mainPanel.setLayout(new BorderLayout());

        panelTreeListener = new java.awt.event.MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                panelTree_mouseReleased(e);
            }
        };

        panelTree.addMouseListener(panelTreeListener);

        jActivateItem.setText("Activate");
        ActionListener listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jActivateItem_actionPerformed(e);
            }
        };
        listeners.put("Commands.Panels.Activate", listener);
        jActivateItem.addActionListener(listener);

        jDeactivateItem.setText("Deactivate");
        jDeleteItem.setText("Delete");
        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jDeleteItem_actionPerformed(e);
            }
        };
        listeners.put("Commands.Panels.Delete", listener);
        jDeleteItem.addActionListener(listener);

        panelTree.setEditable(false);
        jRenameItem.setText("Rename");

        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jRenameItem_actionPerformed(e);
            }
        };
        listeners.put("Commands.Panels.Rename", listener);
        jRenameItem.addActionListener(listener);
        mnuClassification.setText("Classification");
        chkCase.setText("Case");
        chkCase.addActionListener(new PanelsEnabledSelectorPanel2_jCheckBoxMenuItem1_actionAdapter(this));
        chkControl.setSelected(true);
        chkControl.setText("Control");
        chkControl.addActionListener(new PanelsEnabledSelectorPanel2_chkControl_actionAdapter(this));
        chkIgnore.setText("Ignore");
        chkIgnore.addActionListener(new PanelsEnabledSelectorPanel2_chkIgnore_actionAdapter(this));
        jPanelMenu.add(jRenameItem);
        jPanelMenu.add(jActivateItem);
        jPanelMenu.add(jDeactivateItem);
        jPanelMenu.add(jDeleteItem);
        jPanelMenu.add(mnuClassification);
        jScrollPane2.getViewport().add(panelTree, null);
        mnuClassification.add(chkCase);
        mnuClassification.add(chkControl);
        mnuClassification.add(chkIgnore);
        panelTreeSelection.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jDeactivateItem_actionPerformed(e);
            }
        };
        listeners.put("Commands.Panels.Deactivate", listener);
        jDeactivateItem.addActionListener(listener);

        panelTree.setCellRenderer(new SelectionTreeRenderer());

        btnTest.setText("Test");
        btnTest.addActionListener(new PanelsEnabledSelectorPanel2_btnTest_actionAdapter(this));
        mainPanel.add(btnTest, java.awt.BorderLayout.NORTH);
        mainPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);
    }

    void panelModified() {
        //    throwEvent(GeneSelectorEvent.PANEL_SELECTION);

        if (panel == null) {
            return;
        }

        //        DSPanel<T> chipSetPanel = panel.panels().get(1);

        //        T object = chipSetPanel.get(0);
        //        PanelSelectorEvent event = new PanelSelectorEvent(object);
        PanelSelectorEvent event = new PanelSelectorEvent(panel);
        publishPanelSelectorEvent(event);
    }

    @Publish public PanelSelectorEvent publishPanelSelectorEvent(PanelSelectorEvent event) {
        return event;
    }

    public Component getComponent() {
        return mainPanel;
    }

    /**
     * implements the method in subpaneladdedlistenser
     *
     * @param spe
     */
    @Subscribe public void receive(SubpanelChangedEvent spe, Object source) {
        DSPanel<T> pan = spe.getPanel();
        if (spe.getMode() == SubpanelChangedEvent.NEW) {
            panel.panels().add(pan);
        }
        panelTreeModel.fireTreeStructureChanged();
    }

    void panelTree_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            int x = e.getX();
            int y = e.getY();
            TreePath[] paths = panelTree.getSelectionPaths();
            TreePath selectedPath = panelTree.getPathForLocation(x, y);
            if (paths != null && selectedPath != null && selectedPath.getLastPathComponent() != null) {
                Object obj = selectedPath.getLastPathComponent();
                if (obj != null) {
                    //                if (IMarkerSimplePanel.class.isAssignableFrom(obj.getClass()))
                    jPanelMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }


    void jActivateItem_actionPerformed(ActionEvent e) {
        activateSelectedNodes(true);
    }

    void jDeactivateItem_actionPerformed(ActionEvent e) {
        activateSelectedNodes(false);
    }

    void activateSelectedNodes(boolean isActive) {
        TreePath[] paths = panelTree.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            //            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].
            //                getLastPathComponent();

            //            if (node != null) {
            //                if (node.getUserObject() instanceof DSPanel) {
            //                    ( (DSPanel) node.getUserObject()).setActive(isActive);
            //                }
            //            }


            Object node = paths[i].getLastPathComponent();
            if (node != null && node instanceof DSPanel) {
                ((DSPanel) node).setActive(isActive);
            }

        }
        panelModified();
    }

    public void chkControl_actionPerformed(ActionEvent e) {
        setSelectedNodeClassification("Control");
    }

    public void chkCase_actionPerformed(ActionEvent e) {
        setSelectedNodeClassification("Case");
    }

    public void chkIgnore_actionPerformed(ActionEvent e) {
        setSelectedNodeClassification("Ignore");
    }

    void setSelectedNodeClassification(String classification) {
        TreePath[] paths = panelTree.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            //                getPathComponent(1);
            if (node != null) {
                if (node.getUserObject() instanceof CSAssayPanel) {
                    CSAssayPanel selectedPanel = (CSAssayPanel) node.getUserObject();
                    selectedPanel.getAnnotator().setAnnotation("Classification", classification);
                }
            }
        }
        panelModified();
    }

    void jRenameItem_actionPerformed(ActionEvent e) {
        LabelledObject node = (LabelledObject) panelTree.getSelectionPath().getLastPathComponent();
        //        LabelledObject node = (LabelledObject)panelTreeModel.getChild(panel, 0);
        String inputValue = JOptionPane.showInputDialog("Please input a value", node.toString());
        if (inputValue != null) {
            node.setLabel(inputValue);
            DefaultTreeModel tr = new DefaultTreeModel(null);
            tr.nodeChanged(null);
            panelTreeModel.fireTreeStructureChanged();
            //        panelModified();
        }
    }

    void jDeleteItem_actionPerformed(ActionEvent e) {
        //        panelTreeModel.fireTreeStructureChanged();
        TreePath[] paths = panelTree.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            Object node = paths[i].getLastPathComponent();
            if (node != null) {
                panel.panels().remove(node);
                //                panel.panels().remove(2);
                //                panelTreeModel.getPanel().panels().remove(2);
                //                panelTreeModel.getPanel().remove(2);
                //                panelTreeModel.getPanel().remove(node);
                //                if (node.getParent() != null) {
                //                    //                    panelTreeModel.removeNodeFromParent(node);
                //                }
                //                //                }
                //                else {
                //                    panel.clear();
                //                    //                    root.removeAllChildren();
                //                }
            }
        }
        panelTreeModel.fireTreeStructureChanged();
        panelModified();
    }


    /**
     * getActionListener
     *
     * @param var String
     * @return ActionListener
     */
    public ActionListener getActionListener(String var) {
        return (ActionListener) listeners.get(var);
    }

    public void btnTest_actionPerformed(ActionEvent e) {
        panelTreeModel.printPanelInfo();
    }

}

class PanelsEnabledSelectorPanel2_btnTest_actionAdapter <T extends DSNamed> implements ActionListener {
    private PanelsEnabledSelectorPanel<T> adaptee;

    PanelsEnabledSelectorPanel2_btnTest_actionAdapter(PanelsEnabledSelectorPanel<T> adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.btnTest_actionPerformed(e);
    }
}

class PanelsEnabledSelectorPanel2_chkIgnore_actionAdapter <T extends DSNamed> implements ActionListener {
    private PanelsEnabledSelectorPanel<T> adaptee;

    PanelsEnabledSelectorPanel2_chkIgnore_actionAdapter(PanelsEnabledSelectorPanel<T> adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.chkIgnore_actionPerformed(e);
    }
}

class PanelsEnabledSelectorPanel2_jCheckBoxMenuItem1_actionAdapter <T extends DSNamed> implements ActionListener {
    private PanelsEnabledSelectorPanel<T> adaptee;

    PanelsEnabledSelectorPanel2_jCheckBoxMenuItem1_actionAdapter(PanelsEnabledSelectorPanel<T> adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.chkCase_actionPerformed(e);
    }
}

class PanelsEnabledSelectorPanel2_chkControl_actionAdapter <T extends DSNamed> implements ActionListener {
    private PanelsEnabledSelectorPanel<T> adaptee;

    PanelsEnabledSelectorPanel2_chkControl_actionAdapter(PanelsEnabledSelectorPanel<T> adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.chkControl_actionPerformed(e);
    }
}
