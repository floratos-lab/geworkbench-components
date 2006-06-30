package org.geworkbench.components.matrixreduce;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSPositionSpecificAffintyMatrix;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

/**
 * @author John Watkinson
 */
@AcceptTypes(DSMatrixReduceSet.class)
public class MatrixReduceViewer implements VisualPlugin {

    private static final int TAB_PSAM = 0;
    private static final int TAB_SEQUENCE = 1;

    private JTabbedPane tabPane;
    private JPanel psamPanel, sequencePanel;
    private DSMatrixReduceSet dataSet = null;
    private boolean imageMode = true;
    private TableModel model;
    private static final int IMAGE_HEIGHT = 284;
    private JTable table;
    private int defaultTableRowHeight;
    private int selectedPSAM = 0;
    private ListOrderedMap<String, String> sequences;
    private ListModel sequenceModel;
    private int maxSequenceLength = 1;
    private SequenceGraph[] graphs = null;

    private class ListModel extends AbstractListModel {
        public int getSize() {
            if (sequences == null) {
                return 0;
            } else {
                return sequences.size();
            }
        }

        public String getElementAt(int index) {
            return sequences.get(sequences.get(index));
        }

        public void fireContentsChanged() {
            if (sequences != null) {
                super.fireContentsChanged(this, 0, sequences.size() - 1);
            } else {
                super.fireContentsChanged(this, 0, 0);
            }
        }
    }

    private class TableModel extends AbstractTableModel {

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Consensus Sequence";
                case 1:
                    return "Experiment Name";
                case 2:
                    return "Seed Sequence";
                default:
                    return "P-Value";
            }
        }

        public int getRowCount() {
            if (dataSet == null) {
                return 0;
            } else {
                return dataSet.size();
            }
        }

        public int getColumnCount() {
            if (dataSet == null) {
                return 0;
            } else {
                return 4;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            DSPositionSpecificAffintyMatrix psam = dataSet.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    if (imageMode) {
                        return psam.getPsamImage();
                    } else {
                        return psam.getConsensusSequence();
                    }
                case 1:
                    return psam.getExperiment();
                case 2:
                    return psam.getSeedSequence();
                default:
                    return psam.getPValue();
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    if (imageMode) {
                        return ImageIcon.class;
                    } else {
                        return String.class;
                    }
                case 3:
                    return Double.class;
                default:
                    return String.class;
            }
        }
    }

    public Component getComponent() {
        return tabPane;
    }

    public MatrixReduceViewer() {
        tabPane = new JTabbedPane();
        psamPanel = new JPanel(new BorderLayout());
        sequencePanel = new JPanel(new BorderLayout());
        tabPane.add("PSAM Detail", psamPanel);
        tabPane.add("Sequence", sequencePanel);
        JRadioButton nameViewButton = new JRadioButton("Name View");
        JRadioButton imageViewButton = new JRadioButton("Image View");
        nameViewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageMode = false;
                table.setRowHeight(defaultTableRowHeight);
                model.fireTableDataChanged();
            }
        });
        imageViewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                imageMode = true;
                table.setRowHeight(IMAGE_HEIGHT);
                model.fireTableDataChanged();
            }
        });
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(nameViewButton);
        buttonGroup.add(imageViewButton);
        imageViewButton.setSelected(true);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(nameViewButton);
        buttonPanel.add(imageViewButton);
        buttonPanel.add(Box.createHorizontalGlue());
        psamPanel.add(buttonPanel, BorderLayout.NORTH);
        model = new TableModel();
        table = new JTable(model);
        final JLabel imageLabel = new JLabel() {
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        table.setDefaultRenderer(ImageIcon.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                imageLabel.setIcon((Icon) value);
                return imageLabel;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row != -1) {
                        selectedPSAM = row;
                        updateGraphs();
                        sequenceModel.fireContentsChanged();
                        tabPane.setSelectedIndex(TAB_SEQUENCE);
                    }
                }
            }
        });
        defaultTableRowHeight = table.getRowHeight();
        table.setRowHeight(IMAGE_HEIGHT);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setColumnHeaderView(table.getTableHeader());
        psamPanel.add(scrollPane, BorderLayout.CENTER);
        //// Sequence Tab
        sequenceModel = new ListModel();
        JList sequenceList = new JList(sequenceModel);
        sequenceList.getInsets().set(4, 4, 4, 4);
        sequenceList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//                label.setText(value.toString());
//                return label;
                return graphs[index];
            }
        });
        sequencePanel.add(new JScrollPane(sequenceList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
    }

    private void updateGraphs() {
        if ((dataSet != null) && (selectedPSAM < dataSet.size())) {
            for (SequenceGraph graph : graphs) {
                graph.createScores(dataSet.get(selectedPSAM), true);
                graph.createScores(dataSet.get(selectedPSAM), false);
            }
        }
    }

    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        DSDataSet data = projectEvent.getDataSet();
        if ((data != null) && (data instanceof DSMatrixReduceSet)) {
            dataSet = ((DSMatrixReduceSet) data);
            model.fireTableStructureChanged();
            sequences = dataSet.getSequences();
            int n = sequences.size();
            graphs = new SequenceGraph[n];
            maxSequenceLength = 1;
            for (String s : ((Collection<String>) sequences.values())) {
                if (s.length() > maxSequenceLength) {
                    maxSequenceLength = s.length();
                }
            }
            // Unregister tooltips
            if (graphs != null) {
                for (SequenceGraph graph : graphs) {
                    if (graph != null) {
                        ToolTipManager.sharedInstance().unregisterComponent(graph);
                    }
                }
            }
            for (int i = 0; i < n; i++) {
                String label = sequences.get(i);
                String sequence = sequences.get(label);
                graphs[i] = new SequenceGraph(sequence, label, maxSequenceLength);
                ToolTipManager.sharedInstance().registerComponent(graphs[i]);
            }
            selectedPSAM = 0;
            updateGraphs();
            sequenceModel.fireContentsChanged();
        }
    }

}
