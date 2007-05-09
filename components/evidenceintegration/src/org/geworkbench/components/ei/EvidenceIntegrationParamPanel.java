package org.geworkbench.components.ei;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.io.Serializable;
import java.awt.*;
import java.util.*;
import java.util.List;

import com.solarmetric.ide.ui.CheckboxCellRenderer;
import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * @author mhall
 */
public class EvidenceIntegrationParamPanel extends AbstractSaveableParameterPanel implements Serializable {

    static Log log = LogFactory.getLog(EvidenceIntegrationParamPanel.class);

    private ArrayList<Evidence> evidenceSet;
    private EvidenceTableModel evidenceTableModel;
    private JXTable evidenceTable;
    private ArrayList<Evidence> predefinedPriors;
    private PriorTableModel predefinedModel;
    private JXTable predefTable;
    private ArrayList<Evidence> loadedPriors;
    private PriorTableModel loadedModel;
    private JXTable loadedTable;

    public EvidenceIntegrationParamPanel(HashMap<Integer, String> goldStandardSources) {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(0, 100));

        JPanel tablesContainer = new JPanel(new GridLayout(1, 2));

        {
            JPanel tablePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Clues");
            tablePanel.add(label, BorderLayout.NORTH);

            evidenceSet = new ArrayList<Evidence>();
//            evidenceSet.add(new Evidence("Test Set 1", null));
            evidenceTableModel = new EvidenceTableModel(evidenceSet);
            evidenceTable = new JXTable(evidenceTableModel);
            evidenceTable.setHorizontalScrollEnabled(true);
            evidenceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            evidenceTable.setHighlighters(new HighlighterPipeline(new Highlighter[]{AlternateRowHighlighter.genericGrey}));
            evidenceTable.packTable(3);
            setBooleanRenderers(evidenceTable);
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
            setBooleanRenderers(predefTable);
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
            setBooleanRenderers(loadedTable);
            JScrollPane loadedScrollPane = new JScrollPane(loadedTable);
            tablePanel.add(loadedScrollPane, BorderLayout.CENTER);

            JButton loadPrior = new JButton("Load");
            tablePanel.add(loadPrior, BorderLayout.SOUTH);
            priorContainer.add(tablePanel);
        }

        tablesContainer.add(priorContainer);

        this.add(tablesContainer, BorderLayout.CENTER);
    }

    private void setBooleanRenderers(JXTable table) {
        // Something in workbench is overriding these renderers
        table.setDefaultEditor(Boolean.class, new DefaultCellEditor(new JCheckBox()));
        table.setDefaultRenderer(Boolean.class, new CheckboxCellRenderer());
    }


/*
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        EvidenceIntegrationParamPanel params = new EvidenceIntegrationParamPanel(eiEngine.getGoldStandardSources());
        frame.setSize(800, 400);
        frame.getContentPane().add(params, BorderLayout.CENTER);
        frame.setVisible(true);
    }
*/

    public void clearEvidence() {
        evidenceSet.clear();
        setBooleanRenderers(evidenceTable);
//        evidenceTableModel.fireTableDataChanged();
    }

    public void addEvidence(Evidence evidence) {
        evidenceSet.add(evidence);
        setBooleanRenderers(evidenceTable);
//        evidenceTableModel.fireTableDataChanged();
    }

    public List<Evidence> getSelectedEvidence() {
        List<Evidence> selected = new ArrayList<Evidence>();
        for (Evidence evidence : evidenceSet) {
            if (evidence.isEnabled()) {
                selected.add(evidence);
            }
        }
        return selected;
    }

    public List<Integer> getSelectedGoldStandards() {
        //todo
        return null;  //To change body of created methods use File | Settings | File Templates.
    }
}
