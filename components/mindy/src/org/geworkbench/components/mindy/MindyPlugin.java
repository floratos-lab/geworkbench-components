package org.geworkbench.components.mindy;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.decorator.*;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;

/**
 * @author mhall
 */
public class MindyPlugin extends JPanel {

    static Log log = LogFactory.getLog(MindyPlugin.class);

    private JScrollPane heatMapScrollPane;
    private List<DSGeneMarker> modulators;
    private List<DSGeneMarker> transFactors;
    private MindyData mindyData;
    private JXTable listTable;
    private JCheckBox selectAllModsCheckBox;
    private JCheckBox selectAllTargetsCheckBox;
    private JButton addToSetButton;

    public MindyPlugin(MindyData data) {
        this.mindyData = data;
        log.debug("Doing mean variance...");
        logNormalize(data.getArraySet());
        markerMeanVariance(data.getArraySet());
        log.debug("Done.");
        modulators = mindyData.getModulators();


        JTabbedPane tabs = new JTabbedPane();
        {
            // Modulator Table

            JPanel panel = new JPanel(new BorderLayout());

            ModulatorModel modulatorModel = new ModulatorModel(mindyData);
            final JXTable table = new JXTable(modulatorModel);
            JScrollPane scrollPane = new JScrollPane(table);
            table.setHorizontalScrollEnabled(true);
//            table.setPreferredScrollableViewportSize(new Dimension(500, 70));
//            table.setColumnControlVisible(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setHighlighters(new HighlighterPipeline(new Highlighter[]{AlternateRowHighlighter.genericGrey}));
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            table.getColumnModel().getColumn(5).setCellRenderer(renderer);
//            table.getColumnModel().getColumn(5).setHeaderRenderer(renderer);
            table.packTable(10);
            panel.add(scrollPane, BorderLayout.CENTER);

            JXTaskPane markerSetPane = new JXTaskPane();
            markerSetPane.setTitle("List Selections");
            final JCheckBox selectAll = new JCheckBox("Select All");
            selectAll.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ModulatorModel model = (ModulatorModel) table.getModel();
                    model.selectAllModulators(selectAll.isSelected());
                    table.repaint();
                }
            });

            markerSetPane.add(selectAll);

            JXTaskPane markerPanelOverride = new JXTaskPane();
            markerPanelOverride.setTitle("Marker Panel Override");
            JCheckBox allMarkers = new JCheckBox("All Markers");
            markerPanelOverride.add(allMarkers);

            JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
            taskContainer.add(markerSetPane);
            taskContainer.add(markerPanelOverride);
            panel.add(taskContainer, BorderLayout.WEST);

            tabs.add("Modulator", panel);
        }

        {
            // Modulator / Target list

            JPanel panel = new JPanel(new BorderLayout());

            JXTaskPane markerSetPane = new JXTaskPane();
            markerSetPane.setTitle("List Selections");

            ModulatorTargetModel model = new ModulatorTargetModel(mindyData);
            listTable = new JXTable(model);
            listTable.setBackground(new Color(245, 245, 245));
//            ModulatorHighlighter modulatorHighlighter = new ModulatorHighlighter();
//            modulatorHighlighter.setBackground(new Color(233, 233, 233));
//            Highlighter[] highlighters = new Highlighter[]{modulatorHighlighter};
//            listTable.setHighlighters(new HighlighterPipeline(highlighters));
            listTable.setHighlighters(new HighlighterPipeline(new Highlighter[]{AlternateRowHighlighter.genericGrey}));
            JScrollPane scrollPane = new JScrollPane(listTable);
            listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listTable.getColumnExt(0).setMaxWidth(20);
            listTable.getColumnExt(2).setMaxWidth(20);
            setListCheckboxesVisibility(false);
            listTable.packAll();

            final JCheckBox selectionCheck = new JCheckBox("Enable Selection");
            markerSetPane.add(selectionCheck);
            selectionCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean selected = selectionCheck.isSelected();
                    log.debug("Setting list box visibility to " + selected);
                    setListCheckboxesVisibility(selected);
                    setListControlVisibility(selected);
                }
            });
            selectAllModsCheckBox = new JCheckBox("Select All Modulators");
            markerSetPane.add(selectAllModsCheckBox);
            selectAllModsCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ModulatorTargetModel model = (ModulatorTargetModel) listTable.getModel();
                    model.selectAllModulators(selectAllModsCheckBox.isSelected());
                }
            });
            selectAllTargetsCheckBox = new JCheckBox("Select All Targets");
            markerSetPane.add(selectAllTargetsCheckBox);
            selectAllTargetsCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    ModulatorTargetModel model = (ModulatorTargetModel) listTable.getModel();
                    model.selectAllTargets(selectAllTargetsCheckBox.isSelected());
                }
            });

            addToSetButton = new JButton("Add To Set");
            markerSetPane.add(addToSetButton);
            setListControlVisibility(false);

            JXTaskPane markerPanelOverride = new JXTaskPane();
            markerPanelOverride.setTitle("Marker Panel Override");
            JCheckBox allMarkers = new JCheckBox("All Markers");
            markerPanelOverride.add(allMarkers);

            JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
            taskContainer.add(markerSetPane);
            taskContainer.add(markerPanelOverride);
            panel.add(taskContainer, BorderLayout.WEST);

            panel.add(scrollPane, BorderLayout.CENTER);

            tabs.add("List", panel);
        }

        {
            // Heat Map

            JPanel panel = new JPanel(new BorderLayout());
            // This is modulator just to give us something to generate the heat map with upon first running
            DSGeneMarker modulator = modulators.iterator().next();
            transFactors = mindyData.getTranscriptionFactors(modulator);
            ModulatorHeatMap heatmap = new ModulatorHeatMap(modulator, transFactors.iterator().next(), mindyData);
            heatMapScrollPane = new JScrollPane(heatmap);
            panel.add(heatMapScrollPane, BorderLayout.CENTER);

            JXTaskPane transFacPane = new JXTaskPane();
            transFacPane.setTitle("Trans. Factors");
            final JList transNameList = new JList();
            transNameList.setModel(new ListModel() {
                public int getSize() {
                    return transFactors.size();
                }

                public Object getElementAt(int i) {
                    return transFactors.get(i).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
                }

                public void addListDataListener(ListDataListener listDataListener) {
                }

                public void removeListDataListener(ListDataListener listDataListener) {
                }
            });

            JXTaskPane modulatorPane = new JXTaskPane();
            JTextField modFilterField = new JTextField();
//            modulatorPane.setExpanded(false);
            modulatorPane.setTitle("Modulators");
            final JList modNameList = new JList();
            JScrollPane modListScrollPane = new JScrollPane(modNameList);
            modNameList.setModel(new ListModel() {
                public int getSize() {
                    return modulators.size();
                }

                public Object getElementAt(int i) {
                    return modulators.get(i).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
                }

                public void addListDataListener(ListDataListener listDataListener) {
                }

                public void removeListDataListener(ListDataListener listDataListener) {
                }
            });
            modNameList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    DSGeneMarker modMarker = modulators.get(modNameList.getSelectedIndex());
                    log.debug("Selected value: " + modMarker.getShortName());
                    transFactors = mindyData.getTranscriptionFactors(modMarker);
                    transNameList.clearSelection();
                    transNameList.invalidate();
                }
            });
            AutoCompleteDecorator.decorate(modNameList, modFilterField);
            modulatorPane.add(modFilterField);
            modulatorPane.add(modListScrollPane);

            transNameList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent listSelectionEvent) {

                    if (!listSelectionEvent.getValueIsAdjusting() && transNameList.getSelectedIndex() > -1) {
                        DSGeneMarker modMarker = modulators.get(modNameList.getSelectedIndex());
                        DSGeneMarker transMarker = transFactors.get(transNameList.getSelectedIndex());

                        log.debug("Rebuilding heat map.");
                        setHeatMap(new ModulatorHeatMap(modMarker, transMarker, mindyData));
                    }
                }
            });

            transFacPane.add(transNameList);

            JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
            taskContainer.add(modulatorPane);
            taskContainer.add(transFacPane);
            JScrollPane modTransScroll = new JScrollPane(taskContainer);
            panel.add(modTransScroll, BorderLayout.WEST);
            tabs.add("Heat Map", panel);
        }

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private void markerMeanVariance(DSMicroarraySet microarraySet) {
        int n = microarraySet.size();
        int m = microarraySet.getMarkers().size();
        for (int i = 0; i < m; i++) {
            SummaryStatistics stats = new SummaryStatisticsImpl();
            for (int j = 0; j < n; j++) {
                stats.addValue(microarraySet.getValue(i, j));
            }
            StatisticalSummary summary = stats.getSummary();
            double mean = summary.getMean();
            double sd = summary.getStandardDeviation();
            if (sd > 0) {
                for (int j = 0; j < n; j++) {
                    DSMicroarray array = (DSMicroarray) microarraySet.get(j);
                    DSMutableMarkerValue markerValue = array.getMarkerValue(i);
                    markerValue.setValue((markerValue.getValue() - mean) / sd);
//                    microarraySet.getMicroarray(j).getValues()[i] = (float) ((microarraySet.getMicroarray(j).getValues()[i] - mean) / sd);
                }
            } else {
                for (int j = 0; j < n; j++) {
                    DSMicroarray array = (DSMicroarray) microarraySet.get(j);
                    DSMutableMarkerValue markerValue = array.getMarkerValue(i);
                    markerValue.setValue(0);

//                    microarraySet.getMicroarray(j).getValues()[i] = 0f;
                }
            }
        }
    }

    private void logNormalize(DSMicroarraySet microarraySet) {
        double log2 = Math.log10(2);
        int n = microarraySet.size();
        int m = microarraySet.getMarkers().size();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                DSMicroarray array = (DSMicroarray) microarraySet.get(j);
                DSMutableMarkerValue markerValue = array.getMarkerValue(i);
                markerValue.setValue(Math.log10(markerValue.getValue()) / log2);
            }
        }

    }

    private void setListCheckboxesVisibility(boolean show) {
        listTable.getColumnExt(" ").setVisible(show);
        listTable.getColumnExt("  ").setVisible(show);
        listTable.packAll();
        listTable.repaint();
    }

    private void setListControlVisibility(boolean show) {
        selectAllModsCheckBox.setEnabled(show);
        selectAllTargetsCheckBox.setEnabled(show);
        addToSetButton.setEnabled(show);
    }

    private void setHeatMap(ModulatorHeatMap heatMap) {
        heatMapScrollPane.getViewport().setView(heatMap);
        heatMapScrollPane.repaint();
    }

    private class ModulatorModel extends DefaultTableModel {

        private boolean[] enabled;
        private List<DSGeneMarker> modulators;
        private MindyData mindyData;
        private String[] columnNames = new String[]{"", "Modulator", " M# ", " M+ ", " M- ", " Mode ", "Modulator Description"};

        public ModulatorModel(MindyData mindyData) {
            modulators = new ArrayList<DSGeneMarker>();
            for (Map.Entry<DSGeneMarker, MindyData.ModulatorStatistics> entry : mindyData.getAllModulatorStatistics().entrySet())
            {
                modulators.add(entry.getKey());
            }
            this.enabled = new boolean[modulators.size()];
            this.mindyData = mindyData;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (enabled == null) {
                return 0;
            } else {
                return modulators.size();
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return true;
            } else {
                return false;
            }
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else if (columnIndex == 1) {
                return String.class;
            } else if (columnIndex == getColumnCount() - 1 || columnIndex == getColumnCount() - 2) {
                return String.class;
            } else {
                return Integer.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return enabled[rowIndex];
            } else if (columnIndex == 1) {
                return modulators.get(rowIndex).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 2) {
                return mindyData.getStatistics(modulators.get(rowIndex)).getCount();
            } else if (columnIndex == 3) {
                return mindyData.getStatistics(modulators.get(rowIndex)).getMover();
            } else if (columnIndex == 4) {
                return mindyData.getStatistics(modulators.get(rowIndex)).getMunder();
            } else if (columnIndex == 5) {
                int mover = mindyData.getStatistics(modulators.get(rowIndex)).getMover();
                int munder = mindyData.getStatistics(modulators.get(rowIndex)).getMunder();
                if (mover > munder) {
                    return "+";
                } else if (mover < munder) {
                    return "-";
                } else {
                    return "=";
                }
            } else {
                return modulators.get(rowIndex).getDescription();
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                enabled[rowIndex] = (Boolean) aValue;
            }
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        public void selectAllModulators(boolean selected) {
            for (int i = 0; i < enabled.length; i++) {
                enabled[i] = selected;
            }
        }
    }

    private class ModulatorTargetModel extends DefaultTableModel {

        private boolean[] enabledMods;
        private boolean[] enabledTrans;
        private MindyData mindyData;
        private String[] columnNames = new String[]{" ", "Modulator", "  ", "Target", "Score"};

        public ModulatorTargetModel(MindyData mindyData) {
            this.enabledMods = new boolean[mindyData.getData().size()];
            this.enabledTrans = new boolean[mindyData.getData().size()];
            this.mindyData = mindyData;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (enabledMods == null) {
                return 0;
            } else {
                return mindyData.getData().size();
            }
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return true;
            } else {
                return false;
            }
        }

        public Class getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 2) {
                return Boolean.class;
            } else if (columnIndex == columnNames.length - 1) {
                return Float.class;
            } else {
                return String.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return enabledMods[rowIndex];
            } else if (columnIndex == 1) {
                return mindyData.getData().get(rowIndex).getModulator().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 2) {
                return enabledTrans[rowIndex];
            } else if (columnIndex == 3) {
                return mindyData.getData().get(rowIndex).getTarget().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 4) {
                return mindyData.getData().get(rowIndex).getScore();
            } else {
                log.error("Requested unknown column");
                return null;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                enabledMods[rowIndex] = (Boolean) aValue;
            } else if (columnIndex == 2) {
                enabledTrans[rowIndex] = (Boolean) aValue;
            }
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        private void selectAllModulators(boolean select) {
            for (int i = 0; i < enabledMods.length; i++) {
                enabledMods[i] = select;
            }
            listTable.repaint();
        }

        private void selectAllTargets(boolean select) {
            for (int i = 0; i < enabledTrans.length; i++) {
                enabledTrans[i] = select;
            }
            listTable.repaint();
        }

    }

    private class ModulatorHighlighter extends ConditionalHighlighter {

        private boolean useBackground = false;
        ArrayList<Boolean> rowBackgrounds = new ArrayList<Boolean>();
        private int lastRowChecked = -1;
        private int maxRowChecked = -1;

        protected boolean test(ComponentAdapter adapter) {
//            if (!adapter.isTestable(0)) {
//                return false;
//            }

            if (adapter.row < rowBackgrounds.size()) {
                return rowBackgrounds.get(adapter.row);
            }

            if (adapter.row == lastRowChecked) {
                return useBackground;
            }

            if (adapter.row == 0) {
                lastRowChecked = 0;
                maxRowChecked = 0;
                rowBackgrounds.add(useBackground);
                return useBackground;
            }

            Object value = adapter.getFilteredValueAt(adapter.row, 0);
            Object compareValue = adapter.getFilteredValueAt(adapter.row - 1, 0);
            if (!(value instanceof String)) {
                value = adapter.getFilteredValueAt(adapter.row, 1);
                compareValue = adapter.getFilteredValueAt(adapter.row - 1, 1);
            }

            lastRowChecked = adapter.row;

            if (value == null || compareValue == null) {
                rowBackgrounds.add(useBackground);
                return useBackground;
            } else {
                boolean matches = value.toString().trim().equals(compareValue.toString().trim());
                if (!matches) {
                    useBackground = !useBackground;
                }
            }
            rowBackgrounds.add(useBackground);
            return useBackground;
        }
    }
}
