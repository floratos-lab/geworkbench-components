package org.geworkbench.components.selectors;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.SingleMicroarrayEvent;
import org.geworkbench.events.PhenotypeSelectedEvent;
import org.geworkbench.events.PhenotypeSelectorEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.bison.datastructure.biocollections.classification.phenotype.CSClassCriteria;
import org.geworkbench.bison.datastructure.biocollections.classification.phenotype.DSClassCriteria;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.geworkbench.util.visualproperties.VisualPropertiesDialog;
import org.geworkbench.bison.annotation.DSCriteria;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.properties.DSNamed;
import org.geworkbench.bison.util.*;
import org.geworkbench.engine.config.MenuListener;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;

/**
 * <p>Title: Plug And Play</p>
 * <p>Description: Dynamic Proxy Implementation of enGenious</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust Inc.</p>
 *
 * @author Manjunath Kustagi
 * @version 1.0
 *          This class is a template to illustrate the requirements to build a component
 *          that subscribes to a given Interface. The interface MUST end in "Subscriber"
 *          to be automatically handled by the Plug&Play framework. For instance, in this
 *          case, the interface is IMarkerIdChangeSubscriber.
 */

public class PropertyTagSelectorPanel implements VisualPlugin, MenuListener {
    private HashMap listeners = new HashMap();
    private JPanel mainPanel = new JPanel();
    private JPanel listPanel = new JPanel();
    private JList itemList = new JList();
    private DSDataSet<? extends DSNamed> dataSet = null;
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode();
    private DefaultTreeModel treeModel = new DefaultTreeModel(root);
    private TreeSelectionModel treeSelectionModel = null;
    private DSCriteria<DSBioObject> criteria = null;
    protected DSClassCriteria classCriteria = null;
    private DSPanel<DSBioObject> selectedCriterion = null;
    private DSPanel<DSBioObject> selectedPanel = null;

    private TreePath selectedPath = null;
    private DefaultMutableTreeNode selectedNode = null;

    private JScrollPane jScrollPane1 = new JScrollPane();
    private JScrollPane jScrollPane2 = new JScrollPane();
    private JTree jCriterionTree = new JTree(treeModel);
    private JPopupMenu jItemListPopupMenu = new JPopupMenu();
    private JPopupMenu jCriterionPanelMenu = new JPopupMenu();
    private JMenuItem jDeleteItem = new JMenuItem();
    private JMenuItem jMergeItem = new JMenuItem();
    private JMenuItem jRenameItem = new JMenuItem();
    private JMenuItem jClearItem = new JMenuItem();
    private JMenuItem jVisualPropertiesItem = new JMenuItem();
    private JPanel jPanel1 = new JPanel();
    private JComboBox jCriterionSelectionBox = new JComboBox();
    private BorderLayout jBorderLayout = new BorderLayout();
    private JLabel jLabel1 = new JLabel();
    private JMenuItem jAddItem = new JMenuItem();
    private JMenuItem jActivatePanelItem = new JMenuItem();
    private JMenuItem jDeactivatePanelItem = new JMenuItem();
    private JMenu jMenu1 = new JMenu();
    private JRadioButtonMenuItem jCaseRadioBtn = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem jControlRadioBtn = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem jTestRadioBtn = new JRadioButtonMenuItem();
    private JRadioButtonMenuItem jIgnoreRadioBtn = new JRadioButtonMenuItem();
    private ButtonGroup jCriterionGroup = new ButtonGroup();
    private BorderLayout iBorderLayout2 = new BorderLayout();
    private BorderLayout borderLayout3 = new BorderLayout();
    private JSplitPane jSplitPane1 = new JSplitPane();
    private JPanel jPanel2 = new JPanel();
    private BorderLayout borderLayout4 = new BorderLayout();
    private JPanel jPanel3 = new JPanel();
    private BorderLayout borderLayout5 = new BorderLayout();

    // This list allows displaying all items in a data set
    // without having to allocate space for them
    private ListModel itemListModel = new AbstractListModel() {
        public int getSize() {
            if (dataSet == null) {
                return 0;
            }
            int n = dataSet.size();
            return n;
        }

        public Object getElementAt(int index) {
            if ((dataSet == null) || (index < 0)) {
                return null;
            }
            return dataSet.get(index);
        }
    };

    // Cell renderer for the itemListModel. Displays item selections in different colors
    private DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            DSNamed mArray = dataSet.get(index);
            if (selectedCriterion != null) {
                if (isSelected) {
                    if (selectedCriterion.getSelection().contains(mArray)) {
                        c.setBackground(Color.yellow);
                    }
                } else {
                    if (selectedCriterion.getSelection().contains(mArray)) {
                        c.setBackground(Color.orange);
                    } else {
                        c.setBackground(Color.white);
                    }
                }
            }
            return c;
        }
    };

    private DefaultTreeCellRenderer criterionTreeRenderer = new DefaultTreeCellRenderer() {
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object object = node.getUserObject();
            if (object instanceof DSPanel) {
                // This is one of the criterion values (a final panel)
                DSPanel<DSMicroarray> maVector = (DSPanel<DSMicroarray>) object;
                DSAnnotValue classValue = classCriteria.getValue(maVector);
                ImageIcon icon = classCriteria.getIcon(classValue);
                if (icon != null) {
                    setIcon(icon);
                }
                if (maVector.isActive()) {
                    c.setForeground(Color.red);
                } else {
                    c.setForeground(Color.black);
                }
            }
            return c;
        }
    };

    /**
     * Standard constructor
     */
    public PropertyTagSelectorPanel() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the component name that gets displayed in the Tabbed interface
     *
     * @return a string with the component name
     */
    public String getComponentName() {
        return "Class Selector";
    }

    /**
     * Default initialization method
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        treeSelectionModel = jCriterionTree.getSelectionModel();
        mainPanel.setLayout(borderLayout3);
        jAddItem.setText("Add to Panel");
        ActionListener listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jAddToPanel_actionPerformed(e);
            }
        };

        jAddItem.addActionListener(listener);
        listeners.put("Commands.Panels.Add to Panel", listener);

        itemList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                jList1_mouseReleased(e);
                super.mouseReleased(e);
            }

            public void mouseClicked(MouseEvent e) {
                itemList_mouseClicked(e);
                super.mouseReleased(e);
            }
        });
        jCriterionTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                panelTree_mouseReleased(e);
            }
        });
        jDeleteItem.setText("Delete");

        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jDeleteItem_actionPerformed(e);
            }
        };

        jDeleteItem.addActionListener(listener);
        listeners.put("Commands.Panels.Delete", listener);

        jMergeItem.setText("Merge");
        jCriterionTree.setEditable(false);
        jRenameItem.setText("Rename");
        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jRenameItem_actionPerformed(e);
            }
        };

        jVisualPropertiesItem.setText("Change Visual Properties");
        jVisualPropertiesItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jVisualPropertiesItem_actionPerformed(e);
            }
        });

        jRenameItem.addActionListener(listener);
        listeners.put("Commands.Panels.Rename", listener);

        jClearItem.setText("Clear Selection");
        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jMenuItem1_actionPerformed(e);
            }
        };

        jClearItem.addActionListener(listener);
        listeners.put("View.Clear Selection", listener);

        jPanel1.setLayout(jBorderLayout);
        jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setText("Phenotype");
        jCriterionSelectionBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jPhenotypeSelectionBox_actionPerformed(e);
            }
        });

        jActivatePanelItem.setText("Activate");

        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jActivateMenuItem_actionPerformed(e);
            }
        };
        listeners.put("Commands.Panels.Activate", listener);
        jActivatePanelItem.addActionListener(listener);

        jDeactivatePanelItem.setText("Deactivate");

        listener = new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jDeactivateMenuItem_actionPerformed(e);
            }
        };
        listeners.put("Commands.Panels.Deactivate", listener);
        jDeactivatePanelItem.addActionListener(listener);

        jMenu1.setText("Classification");
        jCaseRadioBtn.setText("Case");
        jCaseRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jRadioButtonMenuItem1_actionPerformed(e);
            }
        });
        jControlRadioBtn.setText("Control");
        jControlRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jControlRadioBtn_actionPerformed(e);
            }
        });
        jTestRadioBtn.setText("Test");
        jTestRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jTestRadioBtn_actionPerformed(e);
            }
        });
        jIgnoreRadioBtn.setText("Ignore");
        jIgnoreRadioBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jIgnoreRadioBtn_actionPerformed(e);
            }
        });
        listPanel.setLayout(iBorderLayout2);
        listPanel.setDebugGraphicsOptions(0);

        jPanel2.setLayout(borderLayout4);
        jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setDividerSize(2);
        jPanel3.setLayout(borderLayout5);
        jPanel1.add(jCriterionSelectionBox, BorderLayout.SOUTH);
        jPanel1.add(jLabel1, BorderLayout.NORTH);
        jPanel2.add(jPanel3, BorderLayout.CENTER);
        jPanel3.add(jScrollPane2, BorderLayout.CENTER);
        jPanel2.add(jPanel1, BorderLayout.NORTH);
        jScrollPane2.getViewport().add(jCriterionTree, null);
        mainPanel.add(jSplitPane1, BorderLayout.CENTER);
        jSplitPane1.add(listPanel, JSplitPane.LEFT);
        listPanel.add(jScrollPane1, BorderLayout.CENTER);
        jSplitPane1.add(jPanel2, JSplitPane.RIGHT);
        jScrollPane1.getViewport().add(itemList, null);
        jItemListPopupMenu.add(jAddItem);
        jItemListPopupMenu.add(jClearItem);
        jCriterionPanelMenu.add(jRenameItem);
        jCriterionPanelMenu.addSeparator();
        jCriterionPanelMenu.add(jActivatePanelItem);
        jCriterionPanelMenu.add(jDeactivatePanelItem);
        jCriterionPanelMenu.add(jMenu1);
        jCriterionPanelMenu.add(jDeleteItem);
        jCriterionPanelMenu.add(jMergeItem);
        jCriterionPanelMenu.add(jVisualPropertiesItem);
        jMenu1.add(jCaseRadioBtn);
        jMenu1.add(jControlRadioBtn);
        jMenu1.add(jTestRadioBtn);
        jMenu1.add(jIgnoreRadioBtn);
        treeSelectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        itemList.setCellRenderer(renderer);
        jCriterionTree.setCellRenderer(criterionTreeRenderer);
        jCriterionGroup.add(jCaseRadioBtn);
        jCriterionGroup.add(jControlRadioBtn);
        jCriterionGroup.add(jTestRadioBtn);
        jCriterionGroup.add(jIgnoreRadioBtn);
        jSplitPane1.setDividerLocation(150);
    }

    public void notifyMAChange(DSDataSet dataSet) {
        if (dataSet != null) {
            this.dataSet = dataSet;
            // Determines if the item set already has an associated set of phenotypes and tags
            criteria = CSCriterionManager.getCriteria(dataSet);
            classCriteria = CSCriterionManager.getClassCriteria(dataSet);
            selectedCriterion = criteria.getSelectedCriterion();

            if (selectedCriterion == null) {
                // Make sure that the Selected Criterion is the first available
                for (DSPanel panel : criteria.values()) {
                    selectedCriterion = panel;
                    break;
                }
            }
            // If there is no criterion at all, create an empty default one
            if (selectedCriterion == null) {
                selectedCriterion = new CSPanel<DSBioObject>("Default");
                criteria.put(new CSAnnotLabel("Default"), selectedCriterion);
            }
            // If the criterion does not have the default "Selected" value, add an empty one
            setSelectedPanel();
            itemList.setModel(new DefaultListModel());
            itemList.setModel(itemListModel);
            // Find all the phenotypic criteria that have been defined for this set
            root = new DefaultMutableTreeNode();
            treeModel.setRoot(root);
            root.removeAllChildren();
            jCriterionSelectionBox.removeAllItems();
            for (DSAnnotLabel property : criteria.keySet()) {
                // For each phenotypic criteria (Default one, "Accession" is ignored)
                if (!property.equals("Accession")) {
                    // Add the criteria to the drop down box for easy selection
                    jCriterionSelectionBox.addItem(property);
                } else {
                    // Add an Unsupervised criteria (MAJOR HACK. Should be done independently of Accession)
                    jCriterionSelectionBox.addItem(new CSAnnotLabel("Unsupervised"));
                }
            }
            jCriterionSelectionBox.setSelectedItem(criteria.getSelectedCriterion());
        } else if (dataSet == null) {
            itemList.setModel(new DefaultListModel());
            root = new DefaultMutableTreeNode();
            treeModel.setRoot(root);
            root.removeAllChildren();
            jCriterionSelectionBox.removeAllItems();
        }
        itemList.repaint();
    }

    private void showPhenotype(DSAnnotLabel property) {
        // Retrieve all the available phenotypes and tags
        // Get the set of tags for the selected phenotype
        DSPanel<DSBioObject> panel = criteria.get(property);
        if (panel == null) {
        } else {
            // Show the criterion in the drop down box
            jCriterionSelectionBox.setSelectedItem(property);
            // Get the criterion panels (values) associated with
            // this criterion (property)
            selectedCriterion = criteria.setSelectedCriterion(property);
            classCriteria.setSelectedCriterion(property);
            // regenerate the tree structure
            regenerateTree();
        }
    }

    private void regenerateTree() {
        // Remove all items from the tree
        root.removeAllChildren();
        treeModel.reload(root);

        if (selectedCriterion != null) {
            // If the criterion does not have the default "Selected" value, add an empty one
            setSelectedPanel();
            for (int i = 0; i < selectedCriterion.panels().size(); i++) {
                DSPanel<DSBioObject> value = selectedCriterion.panels().get(i);
                DefaultMutableTreeNode panelNode = new DefaultMutableTreeNode(value);
                treeModel.insertNodeInto(panelNode, root, root.getChildCount());

                for (int id = 0; id < value.size(); id++) {
                    DSNamed mArray = value.get(id);
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(mArray);
                    treeModel.insertNodeInto(node, panelNode, panelNode.getChildCount());
                }
                jCriterionTree.scrollPathToVisible(new TreePath(panelNode.getPath()));
            }
        }
        jCriterionTree.repaint();
    }

    private void setSelectedPanel() {
    }

    public void notifyMArrayIdChange(DSDataSet dataSet, int id) {
        itemList.setSelectedIndex(id);
        itemList.ensureIndexIsVisible(id);
        itemList.repaint();
    }

    void jAddToPanel_actionPerformed(ActionEvent e) {
        DSAnnotLabel property = null;
        DSAnnotValue value = null;
        Object[] selection = itemList.getSelectedValues();
        DSPanel<DSBioObject> criterion = null;
        if (selection.length > 0) {
            DSPanel<DSBioObject> criterionValue = null;
            TreePath[] selectionPath = jCriterionTree.getSelectionPaths();
            if ((selectionPath != null) && (jCriterionTree.getSelectionPaths().length == 1) && (jCriterionTree.getSelectionPath().getPathCount() > 1)) {
                selectedNode = (DefaultMutableTreeNode) jCriterionTree.getSelectionPath().getPathComponent(1);
                selectedPanel = (DSPanel) selectedNode.getUserObject();
                criterionValue = selectedPanel;
                criterion = selectedCriterion;
            } else {
                // Get the name of the criterion where we should add the data items
                // The default is the currently selected criterion
                String propertyName = JOptionPane.showInputDialog("Criterion:", selectedCriterion.getLabel());
                if (propertyName.trim().length() == 0) {
                    return;
                }
                // Get the panel name (value) we should use to tage the data items
                // Default is the currently selected panel
                property = new CSAnnotLabel(propertyName);
                String valueName = JOptionPane.showInputDialog("Value:", "");
                if (valueName.length() == 0) {
                    return;
                }
                value = new CSAnnotValue(valueName, valueName.hashCode());
                //
                criterion = criteria.get(property);
                if (criterion == null) {
                    // we must add the new criterion to the set of available criteria
                    criterion = new CSPanel<DSBioObject>(property.toString());
                    criteria.put(property, criterion);
                    jCriterionSelectionBox.addItem(property);
                    jCriterionSelectionBox.setSelectedItem(property);
                }
                // Retrieve the required criterion value panel, if it exists
                criterionValue = criterion.panels().get(value.toString());
                if (criterionValue == null) {
                    // Create one if it does not
                    criterionValue = new CSPanel<DSBioObject>(value.toString());
                    criterion.panels().add(criterionValue);
                    if (criterion == selectedCriterion) {
                        // If the criterion is the one currently shown in the tree
                        selectedNode = new DefaultMutableTreeNode(criterionValue);
                        treeModel.insertNodeInto(selectedNode, root, root.getChildCount());
                    }
                }
            }
            // Add all the data items to the new criterion value
            for (int i = 0; i < selection.length; i++) {
                criterionValue.add((DSMicroarray) selection[i]);
                if (criterion == selectedCriterion) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(selection[i]);
                    treeModel.insertNodeInto(node, selectedNode, selectedNode.getChildCount());
                    jCriterionTree.scrollPathToVisible(new TreePath(node.getPath()));
                }
            }
            // If the criterion is the one currently shown in the tree
            // make sure the new node is visible
            if (criterion == selectedCriterion) {
                jCriterionTree.setSelectionPath(new TreePath(selectedNode.getPath()));
                jCriterionTree.scrollPathToVisible(new TreePath(selectedNode.getPath()));
                panelModified();
            }
        }
    }

    void panelModified() {
        itemList.repaint();
        throwEvent();
    }

    private boolean isListItemSelected(int index) {
        int[] selectedIndices = itemList.getSelectedIndices();
        if (selectedIndices == null) {
            return false;
        } else {
            for (int i = 0; i < selectedIndices.length; i++) {
                if (index == selectedIndices[i]) {
                    return true;
                }
            }
            return false;
        }
    }

    void jList1_mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            int index = itemList.locationToIndex(new Point(e.getX(), e.getY()));
            if (!isListItemSelected(index)) {
                itemList.setSelectedIndex(index);
            }
            jItemListPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        } else if (e.getButton() == MouseEvent.BUTTON1) {
            int index = itemList.locationToIndex(new Point(e.getX(), e.getY()));
            if (dataSet instanceof DSMicroarraySet) {
                DSMicroarray array = (DSMicroarray) dataSet.get(index);
                publishSingleMicroarrayEvent(new SingleMicroarrayEvent(array, "Selected"));
            }
        }
    }

    private boolean isPathSelected(TreePath path) {
        TreePath[] selectedPaths = jCriterionTree.getSelectionPaths();
        if (selectedPaths == null) {
            return false;
        }
        for (int i = 0; i < selectedPaths.length; i++) {
            TreePath selectedPath = selectedPaths[i];
            if (path == selectedPath) {
                return true;
            }
        }
        return false;
    }


    void panelTree_mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        selectedPath = jCriterionTree.getPathForLocation(x, y);
        DSMicroarray selectedMicroarray = null;
        if (selectedPath != null) {
            selectedPanel = null;
            selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            Object obj = selectedNode.getUserObject();
            if (obj != null) {
                if (obj instanceof DSPanel) {
                    selectedPanel = (DSPanel) obj;
                } else if (obj instanceof DSMicroarray) {
                    selectedMicroarray = (DSMicroarray) obj;
                }
                if (e.getButton() == MouseEvent.BUTTON3) {
                    if (!isPathSelected(selectedPath)) {
                        jCriterionTree.setSelectionPath(selectedPath);
                    }
                    if (selectedPanel != null) {
                        // The selected node is a panel
                        // @todo - watkin - The use below of hashCode() should be replaced with straight-up equals.
                        DSAnnotValue value = classCriteria.getValue(selectedPanel);
                        if (value != null) {
                            if (value.hashCode() == CSClassCriteria.cases.hashCode()) {
                                jCaseRadioBtn.setSelected(true);
                            } else if (value.hashCode() == CSClassCriteria.controls.hashCode()) {
                                jControlRadioBtn.setSelected(true);
                            } else if (value.hashCode() == CSClassCriteria.test.hashCode()) {
                                jIgnoreRadioBtn.setSelected(true);
                            } else if (value.hashCode() == CSClassCriteria.test.hashCode()) {
                                jTestRadioBtn.setSelected(true);
                            }
                        }
                        jCriterionPanelMenu.show(e.getComponent(), x, y);
                    } else {
                        // Support for additional node types
                    }
                } else if (e.getButton() == MouseEvent.BUTTON1) {
                    if (selectedMicroarray != null) {
                        publishSingleMicroarrayEvent(new SingleMicroarrayEvent(selectedMicroarray, "Selected"));
                    }
                }
            }
        }
    }

    void jVisualPropertiesItem_actionPerformed(ActionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jCriterionTree.getSelectionPath().getLastPathComponent();
        Object obj = node.getUserObject();
        if (obj instanceof DSPanel) {
            DSPanel panel = (DSPanel) obj;
            // Get current active index of panel for default visual properties
            int index = 0;
            if (panel.isActive()) {
                for (int i = 0; i < selectedCriterion.panels().size(); i++) {
                    DSPanel<DSBioObject> subPanel = selectedCriterion.panels().get(i);
                    if (subPanel.isActive()) {
                        index++;
                    }
                    if (subPanel.equals(panel)) {
                        break;
                    }
                }
            }
            VisualPropertiesDialog dialog = new VisualPropertiesDialog(null, "Change Visual Properties", panel, index);
            dialog.pack();
            dialog.setSize(600, 600);
            dialog.setVisible(true);
            if (dialog.isPropertiesChanged()) {
                PanelVisualPropertiesManager manager = PanelVisualPropertiesManager.getInstance();
                PanelVisualProperties visualProperties = dialog.getVisualProperties();
                if (visualProperties == null) {
                    manager.clearVisualProperties(panel);
                } else {
                    manager.setVisualProperties(panel, visualProperties);
                }
                throwEvent();
            }
        }
    }

    void jRenameItem_actionPerformed(ActionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) jCriterionTree.getSelectionPath().getLastPathComponent();
        Object obj = node.getUserObject();
        if (obj instanceof DSPanel) {
            String inputValue = JOptionPane.showInputDialog("Please input a value", ((DSPanel) obj).getLabel());
            if (inputValue != null) {
                ((DSPanel) obj).setLabel(inputValue);
                treeModel.nodeChanged(node);
                panelModified();
            }
        }
    }

    void jDeactivateItem_actionPerformed(ActionEvent e) {
        TreePath[] paths = jCriterionTree.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getPathComponent(1);
            if (node != null) {
                DSPanel<DSBioObject> valuePanel = (DSPanel<DSBioObject>) node.getUserObject();
                valuePanel.setActive(false);
                DSAnnotValue av = classCriteria.getValue(valuePanel);
                classCriteria.removePanel(av, valuePanel);
            }
        }
        panelModified();
    }

    void jDeleteItem_actionPerformed(ActionEvent e) {
        TreePath[] paths = jCriterionTree.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            if (node != null) {
                Object obj = node.getUserObject();
                if (obj instanceof DSPanel) {
                    // we have selected a single value panel
                    DSPanel maVector = (DSPanel) obj;
                    selectedCriterion.panels().remove(maVector);
                    if (node.getParent() != null) {
                        treeModel.removeNodeFromParent(node);
                    }
                } else if (obj instanceof DSMicroarray) {
                    // we are on a data node
                    if (node.getParent() != null) {
                        DSPanel<DSMicroarray> valuePanel = (DSPanel) ((DefaultMutableTreeNode) node.getParent()).getUserObject();
                        valuePanel.remove((DSMicroarray) obj);
                        treeModel.removeNodeFromParent(node);
                    }
                } else {
                    // We have selected the full tree
                    selectedCriterion.clear();
                    root.removeAllChildren();
                }
            }
        }
        panelModified();
    }

    public void notifyMarkerClicked(int markerId, int mArrayId) {
        itemList.setSelectedIndex(mArrayId);
        itemList.ensureIndexIsVisible(mArrayId);
    }

    void itemList_mouseClicked(MouseEvent e) {
        int index = itemList.locationToIndex(e.getPoint());
        if (index != -1) {
            if (e.getClickCount() == 2) {
                DSBioObject mArray = (DSBioObject) dataSet.get(index);
                if (selectedCriterion.getSelection().contains(mArray)) {
                    // remove the microarray from the selection
                    selectedCriterion.getSelection().remove(mArray);
                } else {
                    // add the selected microarray to the selection
                    selectedCriterion.getSelection().add(mArray);
                }
                regenerateTree();
                panelModified();
                itemList.repaint();
            }
        }
    }

    void jMenuItem1_actionPerformed(ActionEvent e) {
        selectedCriterion.getSelection().clear();
        panelModified();
    }

    void jPhenotypeSelectionBox_actionPerformed(ActionEvent e) {
        DSAnnotLabel property = (CSAnnotLabel) jCriterionSelectionBox.getSelectedItem();
        dataSet.setSelectedProperty(property);
        showPhenotype(property);
        panelModified();
    }

    void jActivateMenuItem_actionPerformed(ActionEvent e) {
        TreePath[] paths = jCriterionTree.getSelectionPaths();
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getPathComponent(1);
                if (node != null) {
                    DSPanel maVector = (DSPanel) node.getUserObject();
                    maVector.setActive(true);
                    DSAnnotValue av = classCriteria.getValue(maVector);
                    if (av == CSClassCriteria.controls) {
                        classCriteria.addPanel(CSClassCriteria.controls, maVector);
                    }
                }
            }
            panelModified();
        }
    }

    void jDeactivateMenuItem_actionPerformed(ActionEvent e) {
        TreePath[] paths = jCriterionTree.getSelectionPaths();
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getPathComponent(1);
                if (node != null) {
                    DSPanel maVector = (DSPanel) node.getUserObject();
                    maVector.setActive(false);
                    DSAnnotValue av = classCriteria.getValue(maVector);
                    classCriteria.removePanel(av, maVector);
                }
            }
            panelModified();
        }
    }

    void setClass() {
        TreePath[] paths = jCriterionTree.getSelectionPaths();
        for (int i = 0; i < paths.length; i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i].getPathComponent(1);
            if (node != null) {
                Object obj = node.getUserObject();
                if (obj instanceof DSPanel) {
                    DSPanel maVector = (DSPanel) obj;
                    if (jCaseRadioBtn.isSelected()) {
                        classCriteria.addPanel(CSClassCriteria.cases, maVector);
                    } else if (jControlRadioBtn.isSelected()) {
                        classCriteria.addPanel(CSClassCriteria.controls, maVector);
                    } else if (jTestRadioBtn.isSelected()) {
                        classCriteria.addPanel(CSClassCriteria.test, maVector);
                    } else if (jIgnoreRadioBtn.isSelected()) {
                        classCriteria.addPanel(CSClassCriteria.ignore, maVector);
                    }
                    throwEvent();
                }
            }
        }
        jCriterionTree.repaint();
    }

    void jRadioButtonMenuItem1_actionPerformed(ActionEvent e) {
        setClass();
    }

    void jControlRadioBtn_actionPerformed(ActionEvent e) {
        setClass();
    }

    void jTestRadioBtn_actionPerformed(ActionEvent e) {
        setClass();
    }

    void jIgnoreRadioBtn_actionPerformed(ActionEvent e) {
        setClass();
    }

    public Component getComponent() {
        return mainPanel;
    }

    @Subscribe public void receive(org.geworkbench.events.PhenotypeSelectedEvent event, Object source) {
        DSBioObject object = event.getObject();
        if (dataSet != null) {
            int index = dataSet.indexOf(object);
            if (index != -1) {
                itemList.setSelectedIndex(index);
                itemList.ensureIndexIsVisible(index);
            }
        }
    }

    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        if (projectEvent.getMessage().equals(ProjectEvent.CLEARED)) {
            notifyMAChange(null);
        }
            ProjectSelection selection = ((ProjectPanel) source).getSelection();
            DSDataSet dataFile = selection.getDataSet();
            if (dataFile instanceof DSDataSet) {
                if (selection.getSelectedNode() != selection.getSelectedProjectNode()) {
                    notifyMAChange(dataFile);
                }
            } else {
                notifyMAChange(null);
            }

    }

    @Publish public SingleMicroarrayEvent publishSingleMicroarrayEvent(org.geworkbench.events.SingleMicroarrayEvent event) {
        return event;
    }

    @Publish public PhenotypeSelectorEvent publishPhenotypeSelectorEvent(org.geworkbench.events.PhenotypeSelectorEvent event) {
        return event;
    }

    void throwEvent() {
        PhenotypeSelectorEvent event = new PhenotypeSelectorEvent(selectedCriterion);
        if (dataSet instanceof CSExprMicroarraySet) {
            ((CSExprMicroarraySet) dataSet).computeRange();
        }
        publishPhenotypeSelectorEvent(event);
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
}
