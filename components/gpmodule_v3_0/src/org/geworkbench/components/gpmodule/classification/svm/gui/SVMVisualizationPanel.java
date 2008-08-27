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

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.SortOrder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

/**
 * @author Marc-Danie Nazaire
 */
public class SVMVisualizationPanel extends JPanel
{
    private SVMClassifier svmClassifier;
    private JSplitPane mainSplitPanel;
    private JPanel trainPanel;
    private JPanel testPanel;

    public SVMVisualizationPanel(SVMClassifier svmClassifier)
    {
        this.svmClassifier = svmClassifier;
        jbInit();
    }

    public void jbInit()
    {
        JTabbedPane tabPanel = new JTabbedPane();

        trainPanel = new JPanel(new BorderLayout());
        tabPanel.addTab("Train", trainPanel);
        buildTrainResultTable();

        testPanel = new JPanel(new BorderLayout());
        tabPanel.addTab("Test", testPanel);

        if(svmClassifier.getTestPredResult() != null)
            buildTestResultTable();
        
        mainSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        mainSplitPanel.setTopComponent(tabPanel);

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
        testPanel.add(scrollPane);
    }
}
