/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2010) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/
package org.geworkbench.components.gpmodule.classification.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Position;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.genepattern.filter.IndexFilter;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.components.gpmodule.classification.PredictionResult;
import org.geworkbench.components.gpmodule.classification.VisualGPClassifier;
import org.geworkbench.components.gpmodule.classification.svm.gui.GPTreeModel;
import org.geworkbench.util.ProgressBar;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.SortOrder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @author Marc-Danie Nazaire
 * @version $Id$
 */
public class GPClassificationVisualizationPanel extends JPanel implements ItemListener
{
	private static final long serialVersionUID = 1450204284902216376L;
	
	final private Color colorAccuracy = Color.red;
	final private Color colorSensitivity = Color.blue;
	final private Color colorSpecificity = Color.green;
	
	private GPClassificationVisualComponent gpCVisComp;
    private VisualGPClassifier visualGPClassifier;
    private JTabbedPane tabPane;
    private JPanel trainResultPanel;
    private JPanel testResultPanel;
    private JPanel testDataPanel;
    private JToolBar createMaSetToolBar;
    private JComboBox maSetNodeComboBox;
    private JComboBox maSetComboBox;
    private JTree maSetGroupTree;
    private JPanel graphPanel;
    private ChartPanel chartPanel;
    private JFreeChart curveChart;
    private JSplitPane testMainPanel;
    private static Map <DSMicroarraySet, Map<String, List<String>>> maTestLabels = new HashMap<DSMicroarraySet, Map<String, List<String>>>(); 
    private GPTreeModel treeModel;
    private JXTable trainResultsTable;
    private JXTable testResultsTable;
    private JButton createMaSetButton;
    private JRadioButton caseRadioButton;
    private JRadioButton controlRadioButton;
    private JFormattedTextField confidenceThreshold = new JFormattedTextField(new DecimalFormat("#.#######"));
    private SortedMap<Double, Double> confidenceMap;
    private JCheckBox applyFilter = new JCheckBox("Apply filter");
    private JSlider confSlider;


    public GPClassificationVisualizationPanel(VisualGPClassifier visualGPClassifier, GPClassificationVisualComponent gpCVisComp)
    {
        this.visualGPClassifier = visualGPClassifier;
        this.gpCVisComp = gpCVisComp;

        jbInit();
    }

    public void jbInit()
    {
        tabPane = new JTabbedPane();

        trainResultPanel = new JPanel(new BorderLayout());
        buildTrainResultTable();
        tabPane.addTab("Train", trainResultPanel);

        testMainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        testMainPanel.setDividerSize(4);
        testMainPanel.setOneTouchExpandable(true);
        testMainPanel.setDividerLocation(0.25);
        testMainPanel.setResizeWeight(0.25);

        tabPane.addTab("Test", testMainPanel);

        testResultPanel = new JPanel(new BorderLayout());
        testMainPanel.setRightComponent(testResultPanel);

        testResultsTable = new JXTable();
        JScrollPane scrollPane = new JScrollPane(testResultsTable);
        testResultPanel.add(scrollPane, BorderLayout.CENTER);
        if(visualGPClassifier.getTestPredResult() != null)
        {
            buildTestResultTable(visualGPClassifier.getTestPredResult());
        }

        testDataPanel = new JPanel();
        testDataPanel.setLayout(new BoxLayout(testDataPanel, BoxLayout.PAGE_AXIS));
        testDataPanel.setBorder(BorderFactory.createEmptyBorder(7, 6, 12, 6));
        buildTestDataPanel();

        createMaSetToolBar = new JToolBar();
        createMaSetToolBar.setLayout(new BoxLayout(createMaSetToolBar, BoxLayout.LINE_AXIS));
        buildCreateMaSetToolBar();
        testResultPanel.add(createMaSetToolBar, BorderLayout.PAGE_END);

        testMainPanel.setLeftComponent(new JScrollPane(testDataPanel));

        JSplitPane mainSplitPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPanel.setDividerLocation(0.50);
        mainSplitPanel.setResizeWeight(0.50);

        mainSplitPanel.setTopComponent(tabPane);

        graphPanel = new JPanel(new BorderLayout());
        buildGraphPanel();
        mainSplitPanel.setBottomComponent(graphPanel);

        setLayout(new BorderLayout());
        add(mainSplitPanel);

        tabPane.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
            	applyFilter_actionPerformed();
            }
        });
    }

    private void buildTrainResultTable()
    {
        trainResultsTable = new JXTable();

        trainResultsTable.addHighlighter(HighlighterFactory.createAlternateStriping());
        trainResultsTable.setSortable(true);
        trainResultsTable.setEditable(false);
        trainResultsTable.setShowGrid(true);
        trainResultsTable.setGridColor(Color.LIGHT_GRAY);

        PredictionResult predResult = visualGPClassifier.getTrainPredResult();

        int sampleIndx = predResult.getColumn("Samples");
        int tClassIndx = predResult.getColumn("True Class");
        int pClassIndx = predResult.getColumn("Predicted Class");
        int confIndx = predResult.getColumn("Confidence");
        int corIndx = predResult.getColumn("Correct?");


        String[] columnNames = {"Array Name", "Real Class", "Predicted Class", "Confidence", "Correct?"};
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnNames);

        java.net.URL imageURL1 = GPClassificationVisualizationPanel.class.getResource("images/green_check_mark.gif");
        ImageIcon correct_image = new ImageIcon(imageURL1);

        java.net.URL imageURL2 = GPClassificationVisualizationPanel.class.getResource("images/xMark.gif");
        ImageIcon incorrect_image = new ImageIcon(imageURL2);


        for(int i =0 ; i < predResult.getNumRows(); i++)
        {
        	// this vector contains String and ImageIcon
            Vector<Object> rowVector = new Vector<Object>();
            rowVector.add(predResult.getValueAt(i, sampleIndx));
            rowVector.add(predResult.getValueAt(i, tClassIndx));
            rowVector.add(predResult.getValueAt(i, pClassIndx));
            rowVector.add(predResult.getValueAt(i, confIndx));

            if(predResult.getValueAt(i, corIndx).equalsIgnoreCase("true"))
            {
                rowVector.add(correct_image);
            }
            else {
                rowVector.add(incorrect_image);
            }

            tableModel.addRow(rowVector);
        }

        trainResultsTable.setModel(tableModel);
        trainResultsTable.getColumn("Correct?").setCellRenderer(new IconRenderer());
        trainResultsTable.setFillsViewportHeight(true);
        trainResultsTable.getColumn("Correct?").sizeWidthToFit();

        trainResultsTable.setSortOrder("Confidence", SortOrder.DESCENDING);

        JScrollPane scrollPane = new JScrollPane(trainResultsTable);
        trainResultPanel.add(scrollPane);
    }

    private void buildTestResultTable(PredictionResult predResult)
    {
        testResultsTable.addHighlighter(HighlighterFactory.createAlternateStriping());
        testResultsTable.setSortable(true);
        testResultsTable.setEditable(false);
        testResultsTable.setShowGrid(true);
        testResultsTable.setGridColor(Color.LIGHT_GRAY);

        int sampleIndx = predResult.getColumn("Samples");
        int pClassIndx = predResult.getColumn("Predicted Class");
        int confIndx = predResult.getColumn("Confidence");

        String[] columnNames = {"Array Name", "Predicted Class", "Confidence"};
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnNames);

        for(int i =0 ; i < predResult.getNumRows(); i++)
        {
            Vector<String> rowVector = new Vector<String>();
            rowVector.add(predResult.getValueAt(i, sampleIndx));
            rowVector.add(predResult.getValueAt(i, pClassIndx));
            rowVector.add(predResult.getValueAt(i, confIndx));

            tableModel.addRow(rowVector);
        }

        testResultsTable.setModel(tableModel);
        testResultsTable.setSortOrder("Confidence", SortOrder.DESCENDING);

        testMainPanel.setRightComponent(testResultPanel);        
    }

    private void applyFilter_actionPerformed(){
    	int selectedIndex = tabPane.getSelectedIndex();
        if(tabPane.getTitleAt(selectedIndex).equalsIgnoreCase("Train") ||
        		applyFilter.isSelected())
        {
            applyConfidenceFilter();
            chartPanel.setEnabled(true);
            confSlider.setEnabled(true);
            confidenceThreshold.setEnabled(true);
        }
        else
        {
            testResultsTable.setFilters(null);
            chartPanel.setEnabled(false);
            confSlider.setEnabled(false);
            confidenceThreshold.setEnabled(false);

            if(testResultsTable.getRowCount() > 0)
            {
                createMaSetButton.setEnabled(true);
                controlRadioButton.setEnabled(true);
                caseRadioButton.setEnabled(true);
            }else{
            	createMaSetButton.setEnabled(false);
                controlRadioButton.setEnabled(false);
                caseRadioButton.setEnabled(false);
            }
        }
    }

    private void buildCreateMaSetToolBar()
    {
        applyFilter.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
            	applyFilter_actionPerformed();
            }
        });

        createMaSetToolBar.add(applyFilter);

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

        createMaSetButton = new JButton("Create array set");
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

					CSMicroarraySet dataset = (CSMicroarraySet) visualGPClassifier.getParentDataSet();
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

					CSMicroarraySet dataset = (CSMicroarraySet) visualGPClassifier.getParentDataSet();
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
                    gpCVisComp.publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent<DSMicroarray>(DSMicroarray.class, predictedResultPanel, org.geworkbench.events.SubpanelChangedEvent.NEW));
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
        maSetNodeComboBox.setMaximumSize(new Dimension(700, maSetNodeComboBox.getMinimumSize().height));
        maSetNodeComboBox.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event)
            {
                if(event.getStateChange() != ItemEvent.SELECTED)
                    return;

                JComboBox comboBox = (JComboBox)event.getSource();
                DSMicroarraySet microarraySet = ((StringSet)comboBox.getSelectedItem()).maSet;

                maSetComboBox.removeAllItems();

                CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();

                int numContexts = manager.getNumberOfContexts(microarraySet);
                DSAnnotationContext<DSMicroarray> currentContext = manager.getCurrentContext(microarraySet);
                for(int i = 0; i < numContexts; i++)
                {
                    DSAnnotationContext<DSMicroarray> aContext = manager.getContext(microarraySet, i);
                    maSetComboBox.addItem(aContext.getName());

                    if(currentContext.getName().equals(aContext.getName()))
                    {
                        maSetComboBox.setSelectedItem(aContext.getName());
                    }
                }

                Dimension pref = maSetNodeComboBox.getPreferredSize();
                maSetNodeComboBox.setMaximumSize(new Dimension(pref.width+3, pref.height));                
            }
        });

        testDataPanel.add(maSetNodeComboBox);
        testDataPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel maSetLabel = new JLabel("Select Array/Phenotype Set List:");
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

                DSMicroarraySet microarraySet = ((StringSet)maSetNodeComboBox.getSelectedItem()).maSet;
                CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();

                DSAnnotationContext<?> context = manager.getContext(microarraySet, contextName);

                Map <String, List<String>> testLabels = null;
                if (!maTestLabels.containsKey(microarraySet)){
                	testLabels = new HashMap<String, List<String>>();
                	maTestLabels.put(microarraySet, testLabels);
                }else testLabels = maTestLabels.get(microarraySet);

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
                Dimension pref = maSetComboBox.getPreferredSize();
                maSetComboBox.setMaximumSize(new Dimension(pref.width+3, pref.height));                                
            }
        });

        testDataPanel.add(maSetComboBox);
        testDataPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel maSetGroupLabel = new JLabel("Select Test Array Set(s):");
        maSetGroupLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        testDataPanel.add(maSetGroupLabel);

        treeModel = new GPTreeModel();

        maSetGroupTree = new JTree(treeModel)
        {
			private static final long serialVersionUID = 3725880146399512736L;

			public void setSelectionPath(TreePath path)
            {
                if(this.isPathSelected(path))
                    super.removeSelectionPath(path);
                else
                    super.addSelectionPath(path);
            }
        };
        maSetGroupTree.setMaximumSize(maSetGroupTree.getPreferredSize());                                


        TreeSelectionModel selectionModel = maSetGroupTree.getSelectionModel();
        selectionModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        maSetGroupTree.setSelectionModel(selectionModel);

        maSetGroupTree.setCellRenderer(new MSetGroupTreeRenderer());

        JScrollPane scrollPane = new JScrollPane();

        scrollPane.setViewportView(maSetGroupTree);
        scrollPane.setPreferredSize(new Dimension(scrollPane.getPreferredSize().width, 100));
        JPanel jPanel = new JPanel();
        jPanel.setAlignmentX(JComboBox.LEFT_ALIGNMENT);
        jPanel.add(scrollPane);
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.PAGE_AXIS));

        JButton testButton = new JButton("Test");
        testButton.setMinimumSize(new Dimension(78, 35));
        testButton.setPreferredSize(new Dimension(78, 35));
        testButton.setMaximumSize(new Dimension(78, 35));
        testButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        testButton.addActionListener(new TestDataPanelActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                Thread t = new Thread()
                {
                    @SuppressWarnings({ "rawtypes", "unchecked" })
					public void run()
                    {
                        DSMicroarraySet maSet = ((StringSet)maSetNodeComboBox.getSelectedItem()).maSet;
                        CSAnnotationContextManager manager = CSAnnotationContextManager.getInstance();

                        String context = (String)maSetComboBox.getSelectedItem();
                        DSAnnotationContext<DSMicroarray> selectedContext = manager.getContext(maSet, context);

                        Map <String, List<String>> testLabels = null;
                        if (!maTestLabels.containsKey(maSet)){
                        	testLabels = new HashMap<String, List<String>>();
                        	maTestLabels.put(maSet, testLabels);
                        }else testLabels = maTestLabels.get(maSet);

                        testLabels.clear();
                        TreePath[] labels = maSetGroupTree.getSelectionPaths();
                        if(labels == null){
                        	JOptionPane.showMessageDialog(null, "Please select test array set(s)",
                        			"Test Error", JOptionPane.ERROR_MESSAGE);
                        	return;
                        }
                        ArrayList<String> labelNames = new ArrayList<String>();
                        DSPanel<DSMicroarray> panel = new CSPanel<DSMicroarray>();
                        for(int i = 0; i < labels.length; i++)
                        {
                            String label = (String)labels[i].getPath()[1];
                            DSPanel<DSMicroarray> selectedPanel = selectedContext.getItemsWithLabel(label);
                            panel.addAll(selectedPanel);
                            labelNames.add((String)labels[i].getLastPathComponent());
                        }

                        //save labels that belong in context that are used to test the classifier
                        testLabels.put(context, labelNames);

                        manager.setCurrentContext(maSet, selectedContext);
                        visualGPClassifier.setParent((DSDataSet)maSet);

                        ProgressBar progressBar;
                        progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
                        progressBar.setTitle("Running classifier on test set");
                        progressBar.setAlwaysOnTop(false);
                        progressBar.showValues(false);

                        progressBar.start();

                        PredictionResult result = null;
                        try
                        {
                            result = visualGPClassifier.classify(panel);

                            buildTestResultTable(result);
                        }
                        catch(Exception e)
                        {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(null, e.getMessage());
                    
                            return;
                        }
                        finally
                        {
                            progressBar.stop();
                        }

                        if(testResultsTable != null && testResultsTable.getRowCount() > 0)
                        {
                            controlRadioButton.setEnabled(true);
                            caseRadioButton.setEnabled(true);
                            createMaSetButton.setEnabled(true);
                        }
                    }
                };
                t.start();
            }
        });


        jPanel.add(testButton);
        testDataPanel.add(jPanel);

        Iterator<DSMicroarraySet> it = GPClassificationVisualComponent.microarraySets.iterator();
        while(it.hasNext())
        {
            DSMicroarraySet microarraySet = it.next();
            StringSet strset = new StringSet(microarraySet);
            maSetNodeComboBox.addItem(strset);

            if(microarraySet.getID().equals(visualGPClassifier.getParentDataSet().getID()))
            {
                maSetNodeComboBox.setSelectedItem(strset);
            }
        }
    }
    
    private class StringSet{
    	DSMicroarraySet maSet;
    	String name;
    	public StringSet(DSMicroarraySet set){
    		maSet = set;
    		name = set.getDataSetName();
    	}
    	public String toString(){ return name; }
    }

    public void itemStateChanged(ItemEvent event)
    {
        if(!(event.getSource() instanceof JCheckBox))
            return;

        JCheckBox checkbox = (JCheckBox) event.getSource();
        int seriesIndex = curveChart.getXYPlot().getDataset().indexOf(checkbox.getText());

        if (event.getStateChange() == ItemEvent.SELECTED)
        {
             curveChart.getXYPlot().getRenderer().setSeriesVisible(seriesIndex, true, true);
        }
        else
        {
            curveChart.getXYPlot().getRenderer().setSeriesVisible(seriesIndex, false, true);
        }
    }

    private void buildGraphPanel()
    {
        curveChart = ChartFactory.createXYLineChart(null, "% unclassified arrays",
				"performance", null, PlotOrientation.VERTICAL, false, true, true);

        curveChart.getXYPlot().getDomainAxis().setLabelFont(curveChart.getXYPlot().getDomainAxis().getLabelFont().deriveFont(7));
        curveChart.getXYPlot().getRangeAxis().setLabelFont(curveChart.getXYPlot().getRangeAxis().getLabelFont().deriveFont(7));

        // Set the range of unclassified arrays ratio axis to 0-1
        curveChart.getXYPlot().getDomainAxis().setRange(0, 1);
        curveChart.getXYPlot().getRangeAxis().setRange(0, 1);

        curveChart.getXYPlot().setDomainCrosshairVisible(true);

        //Add plot data
        curveChart.getXYPlot().setDataset(getPlotData());
        curveChart.getXYPlot().getRenderer().setSeriesPaint(0, colorAccuracy);
        curveChart.getXYPlot().getRenderer().setSeriesPaint(1, colorSensitivity);
        curveChart.getXYPlot().getRenderer().setSeriesPaint(2, colorSpecificity);

        curveChart.getXYPlot().getRenderer().setBaseSeriesVisible(false);

        curveChart.addProgressListener(new ChartProgressListener()
        {
            public void chartProgress(ChartProgressEvent event)
            {
                if(event.getType() == ChartProgressEvent.DRAWING_FINISHED)
                {
                    double domainValue = curveChart.getXYPlot().getDomainCrosshairValue();

                    int sliderValue = (int)Math.round( (domainValue  + 0.01)/0.01 );

                    if(confSlider.getValue() != sliderValue)
                        confSlider.setValue(sliderValue);
                }
            }
        });

       chartPanel = new ChartPanel(curveChart, true);

        chartPanel.setFocusable(true);

        graphPanel.add(chartPanel, BorderLayout.CENTER);

        JToolBar sliderToolBar = new JToolBar();
        sliderToolBar.setFloatable(false);
        sliderToolBar.setBorderPainted(false);

        confSlider = new JSlider(1, 101);
        confSlider.setPaintTicks(true);
        confSlider.setMajorTickSpacing(10);
        confSlider.setMinorTickSpacing(1);
        confSlider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                JSlider slider = (JSlider)event.getSource();

                double sliderValue = (0.01 * slider.getValue()) - 0.01;

                double confidence = 0;
                Set<Double> unclassified = confidenceMap.keySet();
                Iterator<Double> unclIt = unclassified.iterator();
                while(unclIt.hasNext())
                {
                    double value = unclIt.next();
                    if(value > sliderValue)
                    {
                        break;
                    }
                    else
                        confidence = (Double)confidenceMap.get(value);
                }

                confidenceThreshold.setValue(confidence);

                curveChart.getXYPlot().setDomainCrosshairValue(sliderValue);
                curveChart.fireChartChanged();

                // Filter rows from current table whose confidence is less than the confidenceThreshold value
                applyConfidenceFilter();
            }
        });

        sliderToolBar.add(Box.createRigidArea(new Dimension(10, 0)));
        sliderToolBar.add(confSlider);

        sliderToolBar.add(Box.createRigidArea(new Dimension(20, 0)));
        graphPanel.add(sliderToolBar, BorderLayout.PAGE_END);

        confSlider.setValue(1);

        confidenceThreshold.setMinimumSize(new Dimension(77, 24));
        confidenceThreshold.setPreferredSize(new Dimension(77, 24));
        confidenceThreshold.setMaximumSize(new Dimension(77, 24));

        JLabel confidenceLabel = new JLabel("Confidence Threshold:");
        confidenceLabel.setFont(new JCheckBox().getFont().deriveFont(4));
        sliderToolBar.add(confidenceLabel);
        sliderToolBar.add(Box.createRigidArea(new Dimension(4, 0)));

        sliderToolBar.add(confidenceThreshold);
        sliderToolBar.add(Box.createRigidArea(new Dimension(5, 0)));

        JToolBar plotSelectToolBar = new JToolBar();
        plotSelectToolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 4, 5));
        plotSelectToolBar.setLayout(new BoxLayout(plotSelectToolBar, BoxLayout.PAGE_AXIS));
        plotSelectToolBar.setBorderPainted(false);

        JPanel plotSelectionPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(3, 2);
        plotSelectionPanel.setLayout(gridLayout);
        plotSelectionPanel.setMinimumSize(new Dimension(160, 110));
        plotSelectionPanel.setPreferredSize(new Dimension(160, 110));
        plotSelectionPanel.setMaximumSize(new Dimension(160, 110));
        plotSelectToolBar.add(plotSelectionPanel);

        //add accuracy plot color selection
        JCheckBox accuracyCheckBox = new JCheckBox("Accuracy");
        accuracyCheckBox.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
        accuracyCheckBox.addItemListener(this);
        accuracyCheckBox.setSelected(true);

        JButton accColorIcon = new JButton();
        accColorIcon.setAlignmentX(JButton.LEFT_ALIGNMENT);
        accColorIcon.setBorder(BorderFactory.createEmptyBorder());
        accColorIcon.setIcon(new MyIcon(colorAccuracy));

        plotSelectionPanel.add(accuracyCheckBox);
        plotSelectionPanel.add(accColorIcon);

        // Add sensitivity plot selection
        JCheckBox sensitivityCheckBox = new JCheckBox("Sensitivity");
        sensitivityCheckBox.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
        sensitivityCheckBox.addItemListener(this);
        sensitivityCheckBox.setSelected(true);

        JButton sensColorIcon = new JButton();
        sensColorIcon.setAlignmentX(JButton.LEFT_ALIGNMENT);
        sensColorIcon.setBorder(BorderFactory.createEmptyBorder());
        sensColorIcon.setIcon(new MyIcon(colorSensitivity));

        plotSelectionPanel.add(sensitivityCheckBox);
        plotSelectionPanel.add(sensColorIcon);

        // Add specificity plot selection
        JCheckBox specificityCheckBox = new JCheckBox("Specificity");
        specificityCheckBox.setAlignmentX(JCheckBox.LEFT_ALIGNMENT);
        specificityCheckBox.addItemListener(this);
        specificityCheckBox.setSelected(true);

        JButton specColorIcon = new JButton();
        specColorIcon.setAlignmentX(JButton.LEFT_ALIGNMENT);
        specColorIcon.setBorder(BorderFactory.createEmptyBorder());
        specColorIcon.setIcon(new MyIcon(colorSpecificity));

        plotSelectionPanel.add(specificityCheckBox);
        plotSelectionPanel.add(specColorIcon);

        graphPanel.add(plotSelectToolBar, BorderLayout.EAST);
    }

    private void applyConfidenceFilter()
    {
        JXTable currentTable = testResultsTable;
        int confidenceIndex = 2;
        if(tabPane.getTitleAt(tabPane.getSelectedIndex()).equalsIgnoreCase("Train"))
        {
            currentTable = trainResultsTable;
            confidenceIndex = 3;
        }

        if(currentTable == null)
            return;

        currentTable.setFilters(null);
        ArrayList<Integer> list = new ArrayList<Integer>();
        for(int r = 0; r < currentTable.getRowCount(); r++)
        {
            String confidence = (String)currentTable.getModel().getValueAt(r, confidenceIndex);
            if(Double.valueOf(confidence).doubleValue() >= ((Double)confidenceThreshold.getValue()).doubleValue())
            {
                list.add(r);
            }
        }        

        int[] indices = new int[list.size()];
        for(int r = 0; r < indices.length; r++)
        {
            indices[r] = ((Integer)list.get(r)).intValue();
        }

        Filter[] filterArray = { new IndexFilter(indices) };
	    FilterPipeline filters = new FilterPipeline(filterArray);

        currentTable.setFilters(filters);

        if(tabPane.getTitleAt(tabPane.getSelectedIndex()).equalsIgnoreCase("Test"))
        {
            if(testResultsTable.getRowCount() == 0)

            {
                createMaSetButton.setEnabled(false);
                controlRadioButton.setEnabled(false);
                caseRadioButton.setEnabled(false);
            }
            else
            {
                createMaSetButton.setEnabled(true);
                controlRadioButton.setEnabled(true);
                caseRadioButton.setEnabled(true);
            }
        }
    }

    public XYDataset getPlotData()
    {
        confidenceMap = new TreeMap<Double, Double>();
        XYSeriesCollection xySeriesCol = new XYSeriesCollection();

        XYSeries accuracySeries = new XYSeries("Accuracy");
        xySeriesCol.addSeries(accuracySeries);

        XYSeries sensitivitySeries = new XYSeries("Sensitivity");
        xySeriesCol.addSeries(sensitivitySeries);

        XYSeries specificitySeries = new XYSeries("Specificity");
        xySeriesCol.addSeries(specificitySeries);

        int N = trainResultsTable.getRowCount();
        double prevConfidence = -1;
        for(int r = 0; r < N; r++)
        {
            double confidence = Double.valueOf((String)trainResultsTable.getValueAt(r, 3));
            if(r != 0 && prevConfidence <= confidence)
                continue;

            double ppAndtp = 0;
            double pnAndtn = 0;
            double tp = 0;
            double tn = 0;
            double s_z = 0;

            for(int r2 = 0; r2 < N; r2++)
            {
                double rowConfidence = Double.valueOf((String)trainResultsTable.getValueAt(r2, 3));
                if(rowConfidence < confidence)
                {
                    continue;
                }

                s_z++;
                if(trainResultsTable.getValueAt(r2, 1).equals("Case"))
                {
                    tp++;

                    if(trainResultsTable.getValueAt(r2, 2).equals("Case"))
                        ppAndtp++;
                }

                if(trainResultsTable.getValueAt(r2, 1).equals("Control"))
                {
                    tn++;

                    if(trainResultsTable.getValueAt(r2, 2).equals("Control"))
                        pnAndtn++;
                }
            }

            prevConfidence = confidence;

            double uncl = 1.0 - (s_z/N);
            confidenceMap.put(uncl, confidence);

            // Add to accuracy series
            double acc = (ppAndtp + pnAndtn)/(tp+tn);

            accuracySeries.add(uncl, acc);

            // Add to sensitivity series
            if(tp != 0)
            {
                double sens = ppAndtp/tp;
                sensitivitySeries.add(uncl, sens);
            }

            // Add to specificity series
            if(tn != 0)
            {
                double spec = pnAndtn/tn;
                specificitySeries.add(uncl, spec);
            }
        }

        return xySeriesCol;
    }


    private abstract class TestDataPanelActionListener implements ActionListener
    {
        public abstract void actionPerformed(ActionEvent event);
    }

    private class MSetGroupTreeRenderer extends DefaultTreeCellRenderer
    {
		private static final long serialVersionUID = -2375695504163370140L;
		
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

    public static class IconRenderer extends DefaultTableCellRenderer
    {
		private static final long serialVersionUID = 8712382539413893926L;

		public IconRenderer()
        {
            super ();
            setHorizontalAlignment(JLabel.CENTER);
        }

        @Override
        public void setValue(Object value)
        {
            setIcon((value instanceof  Icon) ? (Icon) value : null);
        }
    }

    private class MyIcon implements Icon
    {
        private int width = 30;
        private int height = 3;
        private Color color;

        public MyIcon(Color color)
        {
            this.color = color;
        }

        public void paintIcon(Component comp, Graphics g, int x, int y)
        {
            g.setColor(color);
            g.fillRect(x, y, width, height);
        }

        public int getIconWidth()
        {
            return width;
        }

        public int getIconHeight()
        {
            return height;
        }
    }
}
