package org.geworkbench.components.matrixreduce;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.apache.commons.collections15.map.ListOrderedMap;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSPositionSpecificAffintyMatrix;
import org.geworkbench.bison.util.StringUtils;
import org.geworkbench.builtin.projects.LoadData;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author John Watkinson
 */
@AcceptTypes(DSMatrixReduceSet.class)
public class MatrixReduceViewer implements VisualPlugin {

    private enum Direction {
        FORWARD, BACKWARD, BOTH
    }

    public static final int IMAGE_HEIGHT = 100;
    public static final int IMAGE_WIDTH = 200;

    private static final int TAB_PSAM = 0;
    private static final int TAB_SEQUENCE = 1;

    private JTabbedPane tabPane;
    private JPanel psamPanel, sequencePanel;
    private DSMatrixReduceSet dataSet = null;
    private boolean imageMode = true;
    private TableModel model;
    private JTable table;
    private int defaultTableRowHeight;
    private int selectedPSAM = 0;
    private HashSet<DSPositionSpecificAffintyMatrix> selectedPSAMs = new HashSet<DSPositionSpecificAffintyMatrix>();
    private ListOrderedMap<String, String> sequences;
    private ArrayList<String> selectedSequences;
    private ListModel sequenceModel;
    private int maxSequenceLength = 1;
    // todo - map from string -> graph
    private HashMap<String, SequenceGraph> graphs = null;
    private boolean showForward = true;
    private boolean showBackward = true;
    private String filterSequence = null;
    private double threshold = 0.0;
    private JLabel psamLabel;
    private JList sequenceList;
    
    private class ListModel extends AbstractListModel {
        public int getSize() {
            if (selectedSequences == null) {
                return 0;
            } else {
                return selectedSequences.size();
            }
        }

        public String getElementAt(int index) {
            return selectedSequences.get(index);
        }

        public void fireContentsChanged() {
            if (selectedSequences != null) {
                super.fireContentsChanged(this, 0, selectedSequences.size() - 1);
            } else {
                super.fireContentsChanged(this, 0, 0);
            }
        }
    }

    private class TableModel extends AbstractTableModel {

        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return "Select"; 
                case 1:
                    return "Consensus Sequence";
                case 2:
                    return "Experiment Name";
                case 3:
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
                return 5;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            DSPositionSpecificAffintyMatrix psam = dataSet.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return selectedPSAMs.contains(psam);
                case 1:
                    if (imageMode) {
                        return psam.getPsamImage();
                    } else {
                        return psam.getConsensusSequence();
                    }
                case 2:
                    return psam.getExperiment();
                case 3:
                    return psam.getSeedSequence();
                default:
                    return psam.getPValue();
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return true;
            } else {
                return false;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert (rowIndex == 0);
            DSPositionSpecificAffintyMatrix psam = dataSet.get(rowIndex);
            if (selectedPSAMs.contains(psam)) {
                selectedPSAMs.remove(psam);
            } else {
                selectedPSAMs.add(psam);
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                    if (imageMode) {
                        return ImageIcon.class;
                    } else {
                        return String.class;
                    }
                case 4:
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
        // Do export button
        JButton exportAllButton = new JButton("Export All");
        exportAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportPSAMs(new HashSet(dataSet));
            }
        });
        JButton exportSelectedButton = new JButton("Export Selected");
        exportSelectedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedPSAMs.isEmpty()) {
                    JOptionPane.showMessageDialog(getComponent(), "Please select some PSAMs.");
                } else {
                    exportPSAMs(selectedPSAMs);
                }
            }
        });
        JPanel lowerPanel = new JPanel(new FlowLayout());
        lowerPanel.add(Box.createHorizontalGlue());
        lowerPanel.add(exportAllButton);
        lowerPanel.add(exportSelectedButton);
        psamPanel.add(lowerPanel, BorderLayout.SOUTH);
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
                        doFilter();
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
        sequenceList = new JList(sequenceModel);
        sequenceList.getInsets().set(4, 4, 4, 4);
        sequenceList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//                label.setText(value.toString());
//                return label;
                return graphs.get((String) value);
            }
        });
        sequencePanel.add(new JScrollPane(sequenceList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        JPanel controlPanel = new JPanel(new BorderLayout());
        // controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        FormLayout layout = new FormLayout(
                "right:max(40dlu;pref), 3dlu, 70dlu, 7dlu",
                "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        ButtonGroup directionGroup = new ButtonGroup();
        JRadioButton forwardButton = new JRadioButton("Forward");
        forwardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDirection(Direction.FORWARD);
            }
        });
        JRadioButton backwardsButton = new JRadioButton("Backward");
        backwardsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDirection(Direction.BACKWARD);
            }
        });
        JRadioButton bothButton = new JRadioButton("Both", true);
        bothButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setDirection(Direction.BOTH);
            }
        });
        directionGroup.add(forwardButton);
        directionGroup.add(backwardsButton);
        directionGroup.add(bothButton);
        final JFormattedTextField thresholdField = new JFormattedTextField(0.0);
        final JTextField searchField = new JTextField();
        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = searchField.getText();
                if ((text == null) || (text.trim().length() == 0)) {
                    filterSequence = null;
                } else {
                    filterSequence = text.trim();
                }
                threshold = 0;
                try {
                    threshold = Double.parseDouble(thresholdField.getText());
                } catch (NumberFormatException nfe) {
                    // Ignore, use 0.
                }
                doFilter();
                sequenceModel.fireContentsChanged();
            }
        });
        JButton imageSnapshotButton = new JButton("Take Snapshot");
        imageSnapshotButton.addActionListener(new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		createImageSnapshot();
        	}
        });
        builder.appendSeparator("Direction");
        builder.append("", forwardButton);
        builder.append("", backwardsButton);
        builder.append("", bothButton);
        builder.appendSeparator("Filtering");
        builder.append("Threshold", thresholdField);
        builder.append("Sequence Search", searchField);
        builder.append("", filterButton);
        builder.appendSeparator("Image Snapshot");
        builder.append("", imageSnapshotButton);
        builder.append(Box.createVerticalGlue());
        psamLabel = new JLabel("");
        psamLabel.setBorder(new LineBorder(Color.black, 1));
        JPanel flowPanel = new JPanel(new FlowLayout());
        flowPanel.add(psamLabel);
        controlPanel.add(flowPanel, BorderLayout.NORTH);
        controlPanel.add(builder.getPanel(), BorderLayout.CENTER);
        sequencePanel.add(controlPanel, BorderLayout.WEST);

    }

/*    public void createImageSnapshot(){
    	System.out.println("blaa;dlfjk");
    	SequenceGraph sg = graphs.get(selectedSequences.get(0));
    	java.awt.Dimension dim = sg.getMaximumSize();
    	System.out.print( dim.height);
    	for (int i = 0; i < selectedSequences.size(); i++){
    		
    	}
*/
    @Publish 
    public org.geworkbench.events.ImageSnapshotEvent createImageSnapshot(){
    	org.geworkbench.events.ImageSnapshotEvent event = null;
    	try{
    		BufferedImage image = new BufferedImage(sequenceList.getWidth(),sequenceList.getHeight(),
                                                BufferedImage.TYPE_INT_RGB);
    		Graphics g = image.getGraphics();
    		sequenceList.print(g);
        	ImageIcon icon = new ImageIcon(image, "MatrixReduce");
        	event = new org.geworkbench.
                events.ImageSnapshotEvent("MatrixReduce Snapshot", icon,
                                          org.geworkbench.events.
                                          ImageSnapshotEvent.Action.SAVE);
        }catch(Error e){
        	JOptionPane.showMessageDialog(this.getComponent(), "Sorry, out of memory...");
        }
        return event;
    }
    
    private void exportPSAMs(Set<DSPositionSpecificAffintyMatrix> psams) {
        // Pop up a file chooser
        String dir = LoadData.getLastDataDirectory();
        if (dir == null) {
            dir = ".";
        }
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        chooser.showSaveDialog(getComponent());
        File file = chooser.getSelectedFile();
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            for (DSPositionSpecificAffintyMatrix psam : psams) {
                MatrixReduceAnalysis.writePSAM(psam, out);
                out.println();
            }
            out.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void updateGraphs() {
        if ((dataSet != null) && (selectedPSAM < dataSet.size())) {
            for (SequenceGraph graph : graphs.values()) {
                graph.createScores(dataSet.get(selectedPSAM), true);
                graph.createScores(dataSet.get(selectedPSAM), false);
            }
            psamLabel.setIcon(dataSet.get(selectedPSAM).getPsamImage());
        }
    }

    private void setDirection(Direction direction) {
        showForward = true;
        showBackward = true;
        if (direction == Direction.FORWARD) {
            showBackward = false;
        } else if (direction == Direction.BACKWARD) {
            showForward = false;
        }
        doFilter();
        sequencePanel.repaint();
    }

    public boolean isShowForward() {
        return showForward;
    }

    public boolean isShowBackward() {
        return showBackward;
    }

    private void doFilter() {
        HashSet<String> seqFilter = new HashSet<String>();
        // 1) Filter by sequence
        if (filterSequence == null) {
            seqFilter.addAll(sequences.keySet());
        } else {
            filterSequence = filterSequence.toUpperCase();
            String reverseSequence = StringUtils.reverseString(filterSequence);
            for (int i = 0; i < sequences.size(); i++) {
                String s = sequences.get(sequences.get(i));
                if (s.contains(filterSequence) || s.contains(reverseSequence)) {
                    seqFilter.add(sequences.get(i));
                }
            }
        }
        // 2) Filter by threshold
        for (int i = 0; i < sequences.size(); i++) {
            String key = sequences.get(i);
            SequenceGraph graph = graphs.get(key);
            boolean passed = false;
            if (showForward) {
                if (graph.getBestPosScore() > threshold) {
                    passed = true;
                }
            }
            if (showBackward) {
                if (graph.getBestNegScore() > threshold) {
                    passed = true;
                }
            }
            if (!passed) {
                seqFilter.remove(key);
            }
        }
        // 3) Build ordered list of the result
        selectedSequences = new ArrayList<String>();
        for (int i = 0; i < sequences.size(); i++) {
            String key = sequences.get(i);
            if (seqFilter.contains(key)) {
                selectedSequences.add(key);
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
            maxSequenceLength = 1;
            for (String s : ((Collection<String>) sequences.values())) {
                if (s.length() > maxSequenceLength) {
                    maxSequenceLength = s.length();
                }
            }
            // Unregister tooltips
            if (graphs != null) {
                for (SequenceGraph graph : graphs.values()) {
                    if (graph != null) {
                        ToolTipManager.sharedInstance().unregisterComponent(graph);
                    }
                }
            }
            graphs = new HashMap<String, SequenceGraph>();
            for (int i = 0; i < n; i++) {
                String label = sequences.get(i);
                String sequence = sequences.get(label);
                SequenceGraph graph = new SequenceGraph(sequence, label, maxSequenceLength, this);
                ToolTipManager.sharedInstance().registerComponent(graph);
                graphs.put(label, graph);
            }
            selectedPSAM = 0;
            updateGraphs();
            doFilter();
            sequenceModel.fireContentsChanged();
        }
    }

}
