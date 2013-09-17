package org.geworkbench.components.ei;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.engine.properties.PropertiesManager;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * @author mhall
 * @version $Id$
 */
public class EvidenceIntegrationParamPanel extends AbstractSaveableParameterPanel {

	private static final long serialVersionUID = 2596959937445171756L;

	static Log log = LogFactory.getLog(EvidenceIntegrationParamPanel.class);

    private ArrayList<Evidence> evidenceSet;
    private EvidenceTableModel evidenceTableModel;
    private JXTable evidenceTable;
    private ArrayList<Evidence> predefinedPriors;
    String LASTDIR = "lastdir";
    private PriorTableModel predefinedModel;
    private JXTable predefTable;
    private ArrayList<Evidence> loadedPriors;
    private PriorTableModel loadedModel;
    private JXTable loadedTable;

    private ArrayList<JCheckBox> arrayList = new ArrayList<JCheckBox>();
    private ArrayList<Integer> evidenceCandidates = new ArrayList<Integer>();

    public EvidenceIntegrationParamPanel(HashMap<Integer, String> goldStandardSources) {

        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(600, 400));


        final JPanel mainDialogPanel = new JPanel();

        final JToolBar jCenterPanel = new JToolBar(JToolBar.VERTICAL);
        final JPanel jSouthPanel = new JPanel();

        JButton removeButton = new JButton();
        JButton cancelButton = new JButton("Cancel");

        Frame frame = JOptionPane.getFrameForComponent(this);

        final JDialog dialog = new JDialog(frame, "Please select which envidence to remove.", false);

        removeButton.setText("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (arrayList != null && arrayList.size() > 0) {

                    int size = arrayList.size() - 1;
                    for (int i = size; i >= 0; i--) {
                        Evidence evidence = evidenceSet.get(i);
                        if (arrayList.get(i).isSelected() && evidence.getName().equalsIgnoreCase(arrayList.get(i).getText())) {
                            evidenceSet.remove(evidence);
                        }
                    }

                }
                dialog.setVisible(false);
                evidenceTableModel.fireTableDataChanged();
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
            }
        });
        jSouthPanel.add(removeButton);
        jSouthPanel.add(cancelButton);
        JPanel tablesContainer = new JPanel(new GridLayout(1, 2));

        {
            JPanel tablePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Evidence");
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

            JButton loadEvidenceButton = new JButton("Load");
            JButton removeEvidenceButton = new JButton("Remove");
            removeEvidenceButton.setToolTipText("Click to Remove Evidences.");
            JPanel lowPanel = new JPanel();
            lowPanel.add(loadEvidenceButton);
            lowPanel.add(removeEvidenceButton);
            tablePanel.add(lowPanel, BorderLayout.SOUTH);
            loadEvidenceButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        Evidence evidence = loadEvidenceFromFile();
                        if (evidence != null) {
                            addEvidence(evidence);
                        }
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            });
            removeEvidenceButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    int currentEvidenceNumber = 0;
                    arrayList = new ArrayList<JCheckBox>();
                    for (Evidence evidence : evidenceSet) {

                        if (evidence.isLoadedFromFile()) {
                            arrayList.add(new JCheckBox(evidence.getName()));
                            evidenceCandidates.add(currentEvidenceNumber);
                        }
                        currentEvidenceNumber++;
                    }

                    jCenterPanel.removeAll();

                    for (JCheckBox jcheckbox : arrayList) {
                        jcheckbox.setPreferredSize(new Dimension(60, 22));
                        jCenterPanel.add(jcheckbox);

                    }
                    JPanel labelPanel = new JPanel();
                    JLabel jLabel = new JLabel("<html><b>Please Select the Evidence(s) to remove</b></html>");
                    labelPanel.add(jLabel);
                    mainDialogPanel.removeAll();
                    mainDialogPanel.setLayout(new BorderLayout());
                    mainDialogPanel.add(labelPanel, BorderLayout.NORTH);
                    mainDialogPanel.add(jCenterPanel, java.awt.BorderLayout.CENTER);
                    mainDialogPanel.add(jSouthPanel, java.awt.BorderLayout.SOUTH);
                    dialog.getContentPane().add(mainDialogPanel);
                    dialog.setMinimumSize(new Dimension(100, 100));
                    dialog.setPreferredSize(new Dimension(250, 200));
                    dialog.pack();
                    dialog.setLocationRelativeTo(evidenceTable);
                    dialog.setVisible(true);

                    evidenceTableModel.fireTableDataChanged();
                }
            });
            tablesContainer.add(tablePanel);
        }

        JPanel priorContainer = new JPanel(new GridLayout(2, 1));
        final int COLUMN0_MAX_WIDTH=25;
        TableColumn column = null;
        {
            JPanel tablePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Predefined Gold Standards");
            //label.setText("<html><font color=blue><B>Predefined Gold Standards </b></font></html>");
            tablePanel.add(label, BorderLayout.NORTH);

            predefinedPriors = new ArrayList<Evidence>();
            for (Map.Entry<Integer, String> gsSource : goldStandardSources.entrySet()) {
                Evidence source = new Evidence(gsSource.getValue());
                source.setSourceID(gsSource.getKey());
                predefinedPriors.add(source);
            }
            predefinedModel = new PriorTableModel(predefinedPriors);
            predefTable = new JXTable(predefinedModel);            
            
            column = predefTable.getColumnModel().getColumn(0);
            column.setMaxWidth(COLUMN0_MAX_WIDTH); 
            setBooleanRenderers(predefTable);
            
            JScrollPane predefScrollPane = new JScrollPane(predefTable);
            tablePanel.add(predefScrollPane, BorderLayout.CENTER);
            
            priorContainer.add(tablePanel);
        }

        {
            JPanel tablePanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Loaded Gold Standards");
            tablePanel.add(label, BorderLayout.NORTH);

            loadedPriors = new ArrayList<Evidence>();
            //loadedPriors.add(new Evidence("Loaded 1", null));
            loadedModel = new PriorTableModel(loadedPriors);
            loadedTable = new JXTable(loadedModel);
            column = loadedTable.getColumnModel().getColumn(0);
            column.setMaxWidth(COLUMN0_MAX_WIDTH); 
            setBooleanRenderers(loadedTable);
            JScrollPane loadedScrollPane = new JScrollPane(loadedTable);
            tablePanel.add(loadedScrollPane, BorderLayout.CENTER);
            JButton removePrior = new JButton("Remove");
            JButton loadPrior = new JButton("Load");
            JPanel lowPanel = new JPanel();
            lowPanel.add(loadPrior);
            lowPanel.add(removePrior);
            loadPrior.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    try {
                        Evidence evidence = loadGoldStrandardsFromFile();
                        if (evidence != null) {
                            loadedPriors.add(evidence);
                            setBooleanRenderers(loadedTable);
                        }
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            });
            removePrior.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {

                    int[] seleInts = loadedTable.getSelectedRows();
                    if (seleInts == null || seleInts.length <= 0) {
                        JOptionPane.showConfirmDialog(null, "Error", "Please select at least one evidence to remove.", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    for (int i = seleInts.length; i >= 1; i--) {
                        Evidence evidence = loadedPriors.get(seleInts[i - 1]);
                        removeEvidence(evidence);
                    }
                    loadedModel.fireTableDataChanged();

                }
            });

            tablePanel.add(lowPanel, BorderLayout.SOUTH);

            priorContainer.add(tablePanel);
        }

        tablesContainer.add(priorContainer);

        this.add(tablesContainer, BorderLayout.CENTER);
    }

    private void setBooleanRenderers(JXTable table) {
        // Something in workbench is overriding these renderers
        table.setDefaultEditor(Boolean.class, new DefaultCellEditor(new JCheckBox()));
        table.setDefaultEditor(Integer.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultRenderer(Integer.class, new JXTable.NumberRenderer());
    }

    private Evidence loadEvidenceFromFile() throws IOException {

        try {
            String lastdir;

            lastdir = PropertiesManager.getInstance().getProperty(getClass(), LASTDIR, "Default Value");
            if (lastdir == null) {
                lastdir = ".";
            }
            JFileChooser fc = new JFileChooser(lastdir);
            int choice = fc.showOpenDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                lastdir = fc.getSelectedFile().getParentFile().getAbsolutePath();
                PropertiesManager.getInstance().setProperty(getClass(), LASTDIR, lastdir);
            }
            File file = fc.getSelectedFile();
            if (file != null) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                Evidence evidence = new Evidence(file.getName());
                evidence.setLoadedFromFile(true);
                String line = reader.readLine();
                while (line != null) {
                    if (!line.startsWith(">")) {
                        String[] splits = line.split("\t");

                        try {
                            evidence.addEdge(Integer.parseInt(splits[0].trim()), Integer.parseInt(splits[1].trim()), Float.parseFloat(splits[2].trim()));
                        } catch (NumberFormatException e) {
                            JOptionPane.showConfirmDialog(null, "There is a problem to parse the file, the line cannot be parsed correctly: " + line);
                            reader.close();
                            return null;
                        }
                    }
                    line = reader.readLine();
                }
                reader.close();
                return evidence;
            }

        } catch (Exception er) {
        }


        return null;
    }

    private Evidence loadGoldStrandardsFromFile() throws IOException {
        JFileChooser chooser = new JFileChooser(".");
//        chooser.setCurrentDirectory(new File(lwd));
        chooser.showOpenDialog(this);
        File file = chooser.getSelectedFile();
        if (file != null) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Evidence evidence = new Evidence(file.getName());
            evidence.setLoadedFromFile(true);
            String line = reader.readLine();
            while (line != null) {
                if (!line.startsWith(">")) {
                    String[] splits = line.split("\t");
                    evidence.addEdge(Integer.parseInt(splits[0]), Integer.parseInt(splits[1]), Float.parseFloat(splits[2]));
                }
                line = reader.readLine();
            }
            reader.close();
            return evidence;
        }
        return null;
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
        ArrayList<Evidence> toRemove = new ArrayList<Evidence>();
        for (Evidence evidence : evidenceSet) {
            if (!evidence.isLoadedFromFile()) {
                toRemove.add(evidence);
            }
        }
        for (Evidence evidence : toRemove) {
            evidenceSet.remove(evidence);
        }
//        evidenceSet.clear();
        setBooleanRenderers(evidenceTable);
//        evidenceTableModel.fireTableDataChanged();
    }

    public void addEvidence(Evidence evidence) {
        evidenceSet.add(evidence);
        setBooleanRenderers(evidenceTable);
//        evidenceTableModel.fireTableDataChanged();
    }

    public void removeEvidence(Evidence evidence) {
        if (evidence != null) {
            loadedPriors.remove(evidence);
            //setBooleanRenderers(evidenceTable);
            loadedModel.fireTableDataChanged();
        }
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
        ArrayList<Integer> enabledGS = new ArrayList<Integer>();
        for (Evidence goldStandard : predefinedPriors) {
            if (goldStandard.isEnabled()) {
                enabledGS.add(goldStandard.getSourceID());
            }
        }
        return enabledGS;
    }
     public List<Evidence> getSelectedUserDefinedGoldStandards() {
        ArrayList<Evidence> enabledGS = new ArrayList<Evidence>();
        for (Evidence goldStandard : loadedPriors) {
            if (goldStandard.isEnabled()) {
               enabledGS.add(goldStandard);
            }
        }
        return enabledGS;
    }

     /*
      * (non-Javadoc)
      * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
      */
     public Map<Serializable, Serializable> getParameters() {
    	 return new HashMap<Serializable, Serializable>();
     }

     /*
      * (non-Javadoc)
      * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
      */
     public void setParameters(Map<Serializable, Serializable> parameter) {
    	 return;
     }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub
		
	}
}
