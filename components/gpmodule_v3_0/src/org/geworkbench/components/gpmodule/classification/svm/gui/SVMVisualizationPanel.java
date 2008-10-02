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
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContext;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.SortOrder;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.Position;
import javax.swing.tree.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.text.DecimalFormat;

/**
 * @author Marc-Danie Nazaire
 */
public class SVMVisualizationPanel extends JPanel
{
    private SVMClassifier svmClassifier;
    private JPanel trainResultPanel;
    private JPanel testResultPanel;
    private JPanel testDataPanel;
    private JToolBar createMaSetToolBar;
    private JComboBox maSetNodeComboBox;
    private JComboBox maSetComboBox;
    private JTree maSetGroupTree;
    private JPanel graphPanel;
    private JFreeChart rocCurveChart;
    private JSplitPane testMainPanel;
    private static Map <String, List> testLabels = new HashMap();
    private SVMTreeModel treeModel;
    private JXTable testResultsTable;
    private JRadioButton caseRadioButton;
    private JRadioButton controlRadioButton;
    private JFormattedTextField confidenceThreshold = new JFormattedTextField(new DecimalFormat("0.00"));


    private SVMVisualComponent svmVisComp;

    public SVMVisualizationPanel(SVMClassifier svmClassifier, SVMVisualComponent svmVisComp)
    {
        this.svmClassifier = svmClassifier;
        this.svmVisComp = svmVisComp;

        jbInit();
    }

    public void jbInit()
    {
        JTabbedPane tabPanel = new JTabbedPane();

        trainResultPanel = new JPanel(new BorderLayout());
        buildTrainResultTable();
        tabPanel.addTab("Train", trainResultPanel);

        testMainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        testMainPanel.setDividerSize(4);
        testMainPanel.setOneTouchExpandable(true);
        testMainPanel.setDividerLocation(0.25);
        testMainPanel.setResizeWeight(0.25);

        tabPanel.addTab("Test", testMainPanel);

        testResultPanel = new JPanel(new BorderLayout());
        testMainPanel.setRightComponent(testResultPanel);

        if(svmClassifier.getTestPredResult() != null)
        {
            testResultsTable = new JXTable();
            buildTestResultTable();

            JScrollPane scrollPane = new JScrollPane(testResultsTable);
            testResultPanel.add(scrollPane, BorderLayout.CENTER);
        }

        testDataPanel = new JPanel();
        testDataPanel.setLayout(new BoxLayout(testDataPanel, BoxLayout.PAGE_AXIS));
        testDataPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 20, 6));
        buildTestDataPanel();

        createMaSetToolBar = new JToolBar();
        createMaSetToolBar.setLayout(new BoxLayout(createMaSetToolBar, BoxLayout.LINE_AXIS));
        buildCreateMaSetToolBar();
        testResultPanel.add(createMaSetToolBar, BorderLayout.PAGE_END);

        testMainPanel.setLeftComponent(new JScrollPane(testDataPanel));

        JSplitPane mainSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPanel.setDividerLocation(0.56);
        mainSplitPanel.setResizeWeight(0.56);

        mainSplitPanel.setTopComponent(tabPanel);

        graphPanel = new JPanel(new BorderLayout());
        buildGraphPanel();
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
        trainResultPanel.add(scrollPane);
    }

    private void buildTestResultTable()
    {
        testResultsTable.addHighlighter(HighlighterFactory.createAlternateStriping());
        testResultsTable.setSortable(true);
        testResultsTable.setEditable(false);
        testResultsTable.setShowGrid(true);
        testResultsTable.setGridColor(Color.LIGHT_GRAY);

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

        testResultsTable.setModel(tableModel);
        testResultsTable.setSortOrder("Confidence", SortOrder.DESCENDING);

        testMainPanel.setRightComponent(testResultPanel);
    }

    private void buildCreateMaSetToolBar()
    {
        caseRadioButton = new JRadioButton("Case");
        caseRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);
        caseRadioButton.setSelected(true);

        controlRadioButton = new JRadioButton("Control");
        caseRadioButton.setAlignmentX(JRadioButton.LEFT_ALIGNMENT);

        ButtonGroup group = new ButtonGroup();
        group.add(caseRadioButton);
        group.add(controlRadioButton);

        createMaSetToolBar.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 3));
        createMaSetToolBar.add(Box.createHorizontalGlue());
        createMaSetToolBar.add(caseRadioButton);
        createMaSetToolBar.add(controlRadioButton);

        createMaSetToolBar.add(Box.createRigidArea(new Dimension(9, 0)));

        JButton createMaSetButton = new JButton("Create MicroarraySet");
        createMaSetButton.setMinimumSize(new Dimension(128, 23));
        createMaSetButton.setPreferredSize(new Dimension(128, 23));
        createMaSetButton.setMaximumSize(new Dimension(128, 23));
        createMaSetButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                DSPanel<DSMicroarray> predictedResultPanel = null;
                String testClass;

                if(caseRadioButton.isSelected())
                {
                    testClass = caseRadioButton.getText();
                    predictedResultPanel = new CSPanel<DSMicroarray>("Predicted Cases");

                    CSMicroarraySet dataset = (CSMicroarraySet)svmClassifier.getParentDataSet();
                    for(int i = 0; i < testResultsTable.getRowCount(); i++)
                    {
                        if(!((String)testResultsTable.getModel().getValueAt(i, 1)).equalsIgnoreCase("Case"))
                            continue;

                        String microarrayName = (String)testResultsTable.getModel().getValueAt(i, 0);

                        DSMicroarray microarray = dataset.getMicroarrayWithId(microarrayName);

                        if(microarray != null)
                        {
                            predictedResultPanel.add(microarray);
                        }
                    }
                }
                else
                {
                    testClass = controlRadioButton.getText();
                    predictedResultPanel = new CSPanel<DSMicroarray>("Predicted Controls");

                    CSMicroarraySet dataset = (CSMicroarraySet)svmClassifier.getParentDataSet();
                    for(int i = 0; i < testResultsTable.getRowCount(); i++)
                    {
                        if(!((String)testResultsTable.getModel().getValueAt(i, 1)).equalsIgnoreCase("Control"))
                            continue;

                        String microarrayName = (String)testResultsTable.getModel().getValueAt(i, 0);

                        DSMicroarray microarray = dataset.getMicroarrayWithId(microarrayName);

                        if(microarray != null)
                        {
                                predictedResultPanel.add(microarray);
                        }
                    }
                }

                if(predictedResultPanel.size() == 0)
                {
                    JOptionPane.showMessageDialog(null, "No microarrays predicted as " + testClass + " ");
                }
                else
                    svmVisComp.publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent(DSMicroarray.class, predictedResultPanel, org.geworkbench.events.SubpanelChangedEvent.NEW));
            }
        });

        createMaSetToolBar.add(createMaSetButton);
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
        maSetNodeComboBox.setMaximumSize(new Dimension(1000, maSetNodeComboBox.getMinimumSize().height));
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
        testDataPanel.add(Box.createRigidArea(new Dimension(0, 8)));

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
        testDataPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        testDataPanel.add(Box.createVerticalGlue());

        JLabel maSetGroupLabel = new JLabel("Select Microarray Set:");
        maSetGroupLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
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
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));
        jPanel.setMinimumSize(new Dimension(jPanel.getWidth(), 110));
        jPanel.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        jPanel.add(scrollPane);

        JButton testButton = new JButton("Test");
        testButton.setMinimumSize(new Dimension(78, 35));
        testButton.setPreferredSize(new Dimension(78, 35));
        testButton.setMaximumSize(new Dimension(78, 35));
        testButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        testButton.addActionListener(new TestDataPanelActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                DSMicroarraySet maSet = (DSMicroarraySet)SVMVisualComponent.microarraySets.get(maSetNodeComboBox.getSelectedItem());
                CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();

                String context = (String)maSetComboBox.getSelectedItem();
                DSAnnotationContext selectedContext = manager.getContext(maSet, context);

                testLabels.clear();
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


        jPanel.add(testButton);
        jPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        testDataPanel.add(jPanel);

        testDataPanel.add(Box.createVerticalGlue());

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
    }

    private void buildGraphPanel()
    {
        graphPanel.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 12));
        rocCurveChart = ChartFactory.createXYLineChart("ROC Curve", "confidence value",
				"true positives/negatives ratio", null, PlotOrientation.VERTICAL, true, true, true);

        TextTitle title = rocCurveChart.getTitle();
        title.setFont(rocCurveChart.getTitle().getFont().deriveFont(4));
        rocCurveChart.setTitle(title);

        rocCurveChart.getXYPlot().getDomainAxis().setLabelFont(rocCurveChart.getXYPlot().getDomainAxis().getLabelFont().deriveFont(7));
        rocCurveChart.getXYPlot().getRangeAxis().setLabelFont(rocCurveChart.getXYPlot().getRangeAxis().getLabelFont().deriveFont(7));

        // Set the range of confidence values axis to 0-1
        rocCurveChart.getXYPlot().getDomainAxis().setRange(0, 1);
        rocCurveChart.getXYPlot().setDomainCrosshairVisible(true);

        ChartPanel chartPanel = new ChartPanel(rocCurveChart, true);
        graphPanel.add(chartPanel, BorderLayout.CENTER);

        JToolBar sliderToolBar = new JToolBar();

        confidenceThreshold.setValue(0);
        confidenceThreshold.setMinimumSize(new Dimension(70, 24));
        confidenceThreshold.setPreferredSize(new Dimension(70, 24));
        confidenceThreshold.setMaximumSize(new Dimension(70, 24));

        sliderToolBar.add(Box.createRigidArea(new Dimension(3, 0)));
        sliderToolBar.add(confidenceThreshold);
        sliderToolBar.add(Box.createRigidArea(new Dimension(7, 0)));

        JSlider slider = new JSlider(0, 100, 1);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(10);
        slider.setMinorTickSpacing(1);
        slider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                JSlider slider = (JSlider)event.getSource();

                confidenceThreshold.setValue(0.01 * slider.getValue());

                rocCurveChart.getXYPlot().setDomainCrosshairValue(Double.valueOf(confidenceThreshold.getText()));
                rocCurveChart.fireChartChanged();

                testResultsTable.setFilters(new FilterPipeline());
            }
        });

        sliderToolBar.add(slider);


        graphPanel.add(sliderToolBar, BorderLayout.PAGE_END);
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
