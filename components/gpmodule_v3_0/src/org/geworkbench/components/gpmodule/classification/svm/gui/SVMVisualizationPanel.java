/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2008) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.svm.gui;

import org.geworkbench.components.gpmodule.classification.svm.SVMClassifier;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContext;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.SortOrder;

import javax.swing.*;
import javax.swing.text.Position;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * @author Marc-Danie Nazaire
 */
public class SVMVisualizationPanel extends JPanel
{
    private SVMClassifier svmClassifier;
    private JSplitPane mainSplitPanel;
    private JPanel trainPanel;
    private JPanel testResultPanel;
    private JPanel testDataPanel;
    private JComboBox maSetNodeComboBox;
    private JComboBox maSetComboBox;
    private JTree maSetGroupTree;
    private JPanel graphPanel;
    private JButton testButton;
    private JSplitPane testMainPanel;
    private static Map <String, List> testLabels = new HashMap();
    private SVMTreeModel treeModel;

    public SVMVisualizationPanel(SVMClassifier svmClassifier)
    {
        this.svmClassifier = svmClassifier;
        jbInit();
    }

    public void jbInit()
    {
        JTabbedPane tabPanel = new JTabbedPane();

        trainPanel = new JPanel(new BorderLayout());
        buildTrainResultTable();
        tabPanel.addTab("Train", trainPanel);

        testMainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        testMainPanel.setDividerSize(4);
        testMainPanel.setDividerLocation(0.25);

        tabPanel.addTab("Test", testMainPanel);

        testResultPanel = new JPanel(new BorderLayout());
        testMainPanel.setRightComponent(testResultPanel);

        if(svmClassifier.getTestPredResult() != null)
        {
            buildTestResultTable();
        }

        testDataPanel = new JPanel();
        testDataPanel.setLayout(new BoxLayout(testDataPanel, BoxLayout.PAGE_AXIS));
        testDataPanel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 6));

        buildTestDataPanel();

        mainSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPanel.setDividerLocation(0.40);

        mainSplitPanel.setTopComponent(tabPanel);

        graphPanel = new JPanel(new BorderLayout());
        mainSplitPanel.setBottomComponent(graphPanel);

        setLayout(new BorderLayout());
        add(mainSplitPanel);
    }

    private void buildTrainResultTable()
    {
        JXTable table = new JXTable();

        table.addHighlighter(HighlighterFactory.createAlternateStriping());
        table.setSortable(true);
        table.setEditable(false);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);

        PredictionResult predResult = svmClassifier.getTrainPredResult();

        int sampleIndx = predResult.getColumn("Samples");
        int rClassIndx = predResult.getColumn("True Class");
        int pClassIndx = predResult.getColumn("Predicted Class");
        int confIndx = predResult.getColumn("Confidence");

        String[] columnNames = {"Array Name", "Real Class", "Predicted Class", "Confidence"};
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnNames);

        for(int i =0 ; i < predResult.getNumRows(); i++)
        {
            Vector rowVector = new Vector();
            rowVector.add(predResult.getValueAt(i, sampleIndx));
            rowVector.add(predResult.getValueAt(i, rClassIndx));
            rowVector.add(predResult.getValueAt(i, pClassIndx));
            rowVector.add(predResult.getValueAt(i, confIndx));

            tableModel.addRow(rowVector);
        }

        table.setModel(tableModel);
        table.setSortOrder("Confidence", SortOrder.DESCENDING);

        JScrollPane scrollPane = new JScrollPane(table);
        trainPanel.add(scrollPane);
    }

    private void buildTestResultTable()
    {
        JXTable table = new JXTable();

        table.addHighlighter(HighlighterFactory.createAlternateStriping());
        table.setSortable(true);
        table.setEditable(false);
        table.setShowGrid(true);
        table.setGridColor(Color.LIGHT_GRAY);

        PredictionResult predResult = svmClassifier.getTestPredResult();

        int sampleIndx = predResult.getColumn("Samples");
        int pClassIndx = predResult.getColumn("Predicted Class");
        int confIndx = predResult.getColumn("Confidence");

        String[] columnNames = {"Array Name", "Predicted Class", "Confidence"};
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnNames);

        for(int i =0 ; i < predResult.getNumRows(); i++)
        {
            Vector rowVector = new Vector();
            rowVector.add(predResult.getValueAt(i, sampleIndx));
            rowVector.add(predResult.getValueAt(i, pClassIndx));
            rowVector.add(predResult.getValueAt(i, confIndx));

            tableModel.addRow(rowVector);
        }

        table.setModel(tableModel);
        table.setSortOrder("Confidence", SortOrder.DESCENDING);

        JScrollPane scrollPane = new JScrollPane(table);
        testResultPanel.removeAll();
        testResultPanel.add(scrollPane);

        testMainPanel.setRightComponent(testResultPanel);
        testMainPanel.repaint();
    }

    private void buildTestDataPanel()
    {
        JLabel maSetNodeLabel = new JLabel("Select Microarray Set Node:");
        maSetNodeLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        maSetNodeLabel.setMinimumSize(new Dimension(maSetNodeLabel.getMinimumSize().width, 28));
        maSetNodeLabel.setPreferredSize(new Dimension(maSetNodeLabel.getPreferredSize().width, 28));
        maSetNodeLabel.setMaximumSize(new Dimension(maSetNodeLabel.getMaximumSize().width, 28));
        testDataPanel.add(maSetNodeLabel);

        maSetNodeComboBox = new JComboBox();
        maSetNodeComboBox.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        maSetNodeComboBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event)
            {
                if(event.getStateChange() != ItemEvent.SELECTED)
                    return;

                JComboBox comboBox = (JComboBox)event.getSource();
                String microarraySetName = (String)comboBox.getSelectedItem();
                DSMicroarraySet microarraySet = (DSMicroarraySet)SVMVisualComponent.microarraySets.get(microarraySetName);

                maSetComboBox.removeAllItems();

                CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();

                int numContexts = manager.getNumberOfContexts(microarraySet);
                DSAnnotationContext currentContext = manager.getCurrentContext(microarraySet);
                for(int i = 0; i < numContexts; i++)
                {
                    DSAnnotationContext aContext = manager.getContext(microarraySet, i);
                    maSetComboBox.addItem(aContext.getName());

                    if(currentContext.getName().equals(aContext.getName()))
                    {
                        maSetComboBox.setSelectedItem(aContext.getName());
                    }
                }
            }
        });

        testDataPanel.add(maSetNodeComboBox);

        testDataPanel.add(Box.createVerticalGlue());

        JLabel maSetLabel = new JLabel("Select Array/Phenotype Set Group:");
        maSetLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        maSetLabel.setMinimumSize(new Dimension(maSetLabel.getMinimumSize().width, 28));
        maSetLabel.setPreferredSize(new Dimension(maSetLabel.getPreferredSize().width, 28));
        maSetLabel.setMaximumSize(new Dimension(maSetLabel.getMaximumSize().width, 28));
        testDataPanel.add(maSetLabel);

        maSetComboBox = new JComboBox();
        maSetComboBox.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        maSetComboBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event)
            {
                if(event.getStateChange() != ItemEvent.SELECTED)
                    return;

                JComboBox comboBox = (JComboBox)event.getSource();
                String contextName = (String)comboBox.getSelectedItem();

                String microarraySetName = (String)maSetNodeComboBox.getSelectedItem();
                DSMicroarraySet microarraySet = (DSMicroarraySet)SVMVisualComponent.microarraySets.get(microarraySetName);
                CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();

                DSAnnotationContext context = manager.getContext(microarraySet, contextName);

                List<String> labelItems = testLabels.get(context.getName());
                if(labelItems == null || labelItems.isEmpty())
                {
                    String[] labelsMarkedTest = context.getLabelsForClass(CSAnnotationContext.CLASS_TEST);
                    labelItems = Arrays.asList(labelsMarkedTest);
                    testLabels.put(context.getName(), labelItems);
                }

                treeModel.setContext(context);
                treeModel.fireTreeStructureChanged();
                maSetGroupTree.setModel(treeModel);

                for(int i = 0; i< labelItems.size(); i++)
                {
                    TreePath path = maSetGroupTree.getNextMatch(labelItems.get(i), 0, Position.Bias.Forward);
                    maSetGroupTree.addSelectionPath(path);
                }

                System.out.println("Context: " + context.getName());
            }
        });

        testDataPanel.add(maSetComboBox);

        testDataPanel.add(Box.createVerticalGlue());

        JLabel maSetGroupLabel = new JLabel("Select Microarray Set:");
        maSetGroupLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        maSetGroupLabel.setMinimumSize(new Dimension(maSetGroupLabel.getMinimumSize().width, 28));
        maSetGroupLabel.setPreferredSize(new Dimension(maSetGroupLabel.getPreferredSize().width, 28));
        maSetGroupLabel.setMaximumSize(new Dimension(maSetGroupLabel.getMaximumSize().width, 28));
        testDataPanel.add(maSetGroupLabel);

        treeModel = new SVMTreeModel();

        maSetGroupTree = new JTree(treeModel)
        {
            public void setSelectionPath(TreePath path)
            {
                if(this.isPathSelected(path))
                    super.removeSelectionPath(path);
                else
                    super.addSelectionPath(path);
            }
        };

        TreeSelectionModel selectionModel = maSetGroupTree.getSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        maSetGroupTree.setSelectionModel(selectionModel);

        maSetGroupTree.setCellRenderer(new MSetGroupTreeRenderer());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(maSetGroupTree);
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        jPanel.add(scrollPane);
        testDataPanel.add(jPanel);

        Iterator it = SVMVisualComponent.microarraySets.keySet().iterator();
        while(it.hasNext())
        {
            String key = (String)it.next();
            DSMicroarraySet microarraySet = (DSMicroarraySet)SVMVisualComponent.microarraySets.get(key);
            maSetNodeComboBox.addItem(microarraySet.getDataSetName());

            if(microarraySet.getDataSetName().equals(svmClassifier.getParentDataSet().getDataSetName()))
            {
                maSetNodeComboBox.setSelectedItem(microarraySet.getDataSetName());
            }
        }

        testDataPanel.add(Box.createVerticalGlue());

        testButton = new JButton("Test");
        testButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        testButton.addActionListener(new TestDataPanelActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                DSMicroarraySet maSet = (DSMicroarraySet)SVMVisualComponent.microarraySets.get(maSetNodeComboBox.getSelectedItem());
                CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();

                String context = (String)maSetComboBox.getSelectedItem();
                DSAnnotationContext selectedContext = manager.getContext(maSet, context);


                TreePath[] labels = maSetGroupTree.getSelectionPaths();
                ArrayList labelNames = new ArrayList();
                DSPanel panel = new CSPanel();
                for(int i = 0; i < labels.length; i++)
                {
                    String label = (String)labels[i].getPath()[1];
                    DSPanel selectedPanel = selectedContext.getItemsWithLabel(label);
                    panel.addAll(selectedPanel);
                    labelNames.add(labels[i].getLastPathComponent());
                }

                testLabels.put(context, labelNames);

                manager.setCurrentContext(maSet, selectedContext);
                svmClassifier.setParent(maSet);
                svmClassifier.classify(panel);
                buildTestResultTable();
            }
        });

        JPanel testButtonPanel = new JPanel();
        testButtonPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        testButtonPanel.setMinimumSize(testButton.getMinimumSize());
        testButtonPanel.setPreferredSize(testButton.getPreferredSize());
        testButtonPanel.setMaximumSize(testButton.getMaximumSize());
        testButtonPanel.add(testButton);

        testDataPanel.add(testButtonPanel);

        testDataPanel.add(Box.createVerticalGlue());
        testMainPanel.setLeftComponent(testDataPanel);
    }

    private abstract class TestDataPanelActionListener implements ActionListener
    {
        public abstract void actionPerformed(ActionEvent event);
    }

    private class MSetGroupTreeRenderer extends DefaultTreeCellRenderer
    {
        protected JCheckBox checkBox;
        private JPanel component;
        protected JLabel cellLabel;
        private Color selectedBgColor;

        public MSetGroupTreeRenderer()
        {
            selectedBgColor = UIManager.getColor("Tree.selectionBackground");
            checkBox = new JCheckBox();
            checkBox.setBackground(Color.WHITE);
            component = new JPanel();

            component.setLayout(new BorderLayout());
            component.setBackground(Color.WHITE);
            component.add(checkBox, BorderLayout.WEST);

            cellLabel = new JLabel("");
            cellLabel.setBackground(Color.WHITE);
            cellLabel.setOpaque(true);
            component.add(cellLabel, BorderLayout.CENTER);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            if (value instanceof String)
            {
                String label = (String) value;

                String displayLabel = label + " [" + tree.getModel().getChildCount(label) + "]";
                cellLabel.setText(" " + displayLabel);
                checkBox.setSelected(selected);

                if (selected)
                {
                    cellLabel.setBackground(selectedBgColor);
                }
                else
                {
                    cellLabel.setBackground(Color.WHITE);
                }
                return component;
            }

            // Root
            if (value == tree.getModel().getRoot())
            {
                return new JLabel();
            }

            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }
    }
}
