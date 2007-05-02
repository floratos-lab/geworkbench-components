package org.geworkbench.components.ei;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;

import javax.swing.*;
import java.io.Serializable;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author mhall
 */
public class EvidenceIntegrationParamPanel extends AbstractSaveableParameterPanel implements Serializable {
    private ArrayList<Evidence> evidenceSet;
    private EvidenceTableModel evidenceTableModel;
    private JXTable evidenceTable;
    private ArrayList<Evidence> predefinedPriors;
    private PriorTableModel predefinedModel;
    private JXTable predefTable;
    private ArrayList<Evidence> loadedPriors;
    private PriorTableModel loadedModel;
    private JXTable loadedTable;

    public EvidenceIntegrationParamPanel() {
        this.setLayout(new BorderLayout());

        JPanel tablesContainer = new JPanel(new GridLayout(1, 2));

        {
            JPanel tablePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Clues");
            tablePanel.add(label, BorderLayout.NORTH);

            evidenceSet = new ArrayList<Evidence>();
            evidenceSet.add(new Evidence("Test Set 1", null));
            evidenceTableModel = new EvidenceTableModel(evidenceSet);
            evidenceTable = new JXTable(evidenceTableModel);
            evidenceTable.setHorizontalScrollEnabled(true);
            evidenceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            evidenceTable.setHighlighters(new HighlighterPipeline(new Highlighter[]{AlternateRowHighlighter.genericGrey}));
            evidenceTable.packTable(3);
            JScrollPane evidenceScrollPane = new JScrollPane(evidenceTable);
            tablePanel.add(evidenceScrollPane, BorderLayout.CENTER);

            JButton loadPrior = new JButton("Load");
            tablePanel.add(loadPrior, BorderLayout.SOUTH);

            tablesContainer.add(tablePanel);
        }

        JPanel priorContainer = new JPanel(new GridLayout(2, 1));

        {
            JPanel tablePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Predefined Priors");
            tablePanel.add(label, BorderLayout.NORTH);

            predefinedPriors = new ArrayList<Evidence>();
            predefinedPriors.add(new Evidence("Predefined 1", null));
            predefinedModel = new PriorTableModel(predefinedPriors);
            predefTable = new JXTable(predefinedModel);
            JScrollPane predefScrollPane = new JScrollPane(predefTable);
            tablePanel.add(predefScrollPane, BorderLayout.CENTER);
            priorContainer.add(tablePanel);
        }

        {
            JPanel tablePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Loaded Priors");
            tablePanel.add(label, BorderLayout.NORTH);

            loadedPriors = new ArrayList<Evidence>();
            loadedPriors.add(new Evidence("Loaded 1", null));
            loadedModel = new PriorTableModel(loadedPriors);
            loadedTable = new JXTable(loadedModel);
            JScrollPane loadedScrollPane = new JScrollPane(loadedTable);
            tablePanel.add(loadedScrollPane, BorderLayout.CENTER);

            JButton loadPrior = new JButton("Load");
            tablePanel.add(loadPrior, BorderLayout.SOUTH);
            priorContainer.add(tablePanel);
        }

        tablesContainer.add(priorContainer);

        this.add(tablesContainer, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        EvidenceIntegrationParamPanel params = new EvidenceIntegrationParamPanel();
        frame.setSize(800, 400);
        frame.getContentPane().add(params, BorderLayout.CENTER);
        frame.setVisible(true);
    }

}
