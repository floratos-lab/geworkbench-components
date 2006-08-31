package org.geworkbench.components.discovery.view;

import org.geworkbench.components.discovery.SequenceDiscoveryViewWidget;
import org.geworkbench.components.parsers.patterns.PatternFileFormat;
import org.geworkbench.util.AlgorithmSelectionPanel;
import org.geworkbench.util.PropertiesMonitor;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

/**
 * The class is used to display the result of hierarchical pattern algorithm.
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author $AUTHOR$
 * @version 1.0
 */
public class PatternTreeView extends JPanel implements TreeModelListener {
    static final public String TREESELECTION = "treeNodeSelection";
    JScrollPane treeScrollPane = new JScrollPane();
    BorderLayout borderLayout1 = new BorderLayout();
    JTree patternTree = new JTree();
    JPanel controller = new JPanel();
    JCheckBox nodeCheckBox = new JCheckBox();
    FlowLayout flowLayout1 = new FlowLayout(SwingConstants.LEFT);
    private JPopupMenu patternMenu = new JPopupMenu();
    private JMenuItem jSavePatternsItem = new JMenuItem();

    private SequenceDiscoveryViewWidget widget = null;

    public PatternTreeView(DefaultTreeModel model, SequenceDiscoveryViewWidget widget1) {
        this.patternTree.setModel(model);
        this.widget = widget1;
        try {
            jbInit();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setDebugGraphicsOptions(0);
        this.setLayout(borderLayout1);
        nodeCheckBox.setText("Expand nodes");
        controller.setLayout(flowLayout1);
        controller.setMinimumSize(new Dimension(212, 30));
        controller.setPreferredSize(new Dimension(212, 30));
        flowLayout1.setAlignment(FlowLayout.LEFT);
        this.add(treeScrollPane, BorderLayout.CENTER);
        treeScrollPane.getViewport().add(patternTree, null);
        this.add(controller, BorderLayout.NORTH);
        controller.add(nodeCheckBox, null);
        addMouseListener();
        initPopupMenuItem();
    }


    private void initPopupMenuItem() {

        jSavePatternsItem.setText("Save Patterns");
        jSavePatternsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                savePatternsItem_actionPerformed(e);
            }
        });
        patternMenu.add(jSavePatternsItem);
    }

    private void savePatternsItem_actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser(PropertiesMonitor.getPropertiesMonitor().getDefPath());
        PatternFileFormat format = new PatternFileFormat();
        FileFilter filter = format.getFileFilter();
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(chooser.getSelectedFile().getAbsoluteFile()));

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) patternTree.getModel().getRoot();
                writer.write(AlgorithmSelectionPanel.HIERARCHICAL + "\n");
                String all = this.widget.getSequenceDB().getFile().getName() + "\n";
                writer.write(all);

                writer.newLine();
                int i = 0;

                saveNode(i, node, "", writer);
                writer.flush();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void saveNode(int i, DefaultMutableTreeNode node, String path, BufferedWriter writer) {

        saveNodeString(i, node, path, writer);
        for (Enumeration enu = node.children(); enu.hasMoreElements();) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) enu.nextElement();
            String childpath = getNodePath(child, path);
            saveNode(i++, child, childpath, writer);
        }
    }

    /**
     * getNodePath
     *
     * @param child DefaultMutableTreeNode
     * @param path  String
     * @return String
     */
    private String getNodePath(DefaultMutableTreeNode node, String parentPath) {

        String childPath = "";
        PatternNode pn = (PatternNode) node.getUserObject();
        if (pn.pattern != null) {
            childPath = parentPath + '0';

        } else {
            childPath = parentPath + '1';

        }

        return childPath;

    }

    private void saveNodeString(int i, DefaultMutableTreeNode node, String path, BufferedWriter writer) {
        String result = "";
        if (node.isRoot()) {

            try {
                writer.write(node.toString() + '\n');
                writer.newLine();
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }

        } else {
            PatternNode pn = (PatternNode) node.getUserObject();

            if (pn.pattern != null) {

                if (pn.pattern.ascii != null) {

                    result = path + "\t" + "+" + "\t" + pn.pattern.seqNo.value;
                } else {
                    result = path + "\t" + "+" + "\t" + pn.pattern.seqNo.value;
                }
            } else {

                result = path + "\t" + "-" + "\t" + pn.sequenceExcluded;
            }
            try {
                writer.write(result + '\n');
                if (pn.pattern == null) {
                    writer.newLine();
                } else {
                    writer.write("[" + i + "]\t");
                    pn.pattern.write(writer);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }


    private void addMouseListener() {
        patternTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {

                firePropertyChange(TREESELECTION, null, patternTree);
            }
        });

        patternTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.isMetaDown()) {
                    patternMenu.show(patternTree, e.getX(), e.getY());
                }
            }
        });
    }

    public void treeNodesChanged(TreeModelEvent evt) {
        //does not get called...
    }

    /**
     * When a node gets inserted we exapnd the tree to show the new node.
     *
     * @param evt TreeModelEvent node inserts event
     */
    public void treeNodesInserted(TreeModelEvent evt) {
        if (nodeCheckBox.isSelected()) {
            patternTree.scrollPathToVisible(evt.getTreePath());
            patternTree.setSelectionPath(evt.getTreePath());
        }
    }

    public void treeNodesRemoved(TreeModelEvent evt) {
        //does not get called
    }

    public void treeStructureChanged(TreeModelEvent evt) {
        //does not get called
    }
}
