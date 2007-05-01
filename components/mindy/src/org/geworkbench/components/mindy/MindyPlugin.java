package org.geworkbench.components.mindy;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.*;
import java.util.*;
import java.util.List;
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
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.descriptive.SummaryStatisticsImpl;
import org.apache.commons.math.stat.descriptive.StatisticalSummary;
import com.solarmetric.ide.ui.CheckboxCellRenderer;

/**
 * @author mhall
 */
public class MindyPlugin extends JPanel {

    static Log log = LogFactory.getLog(MindyPlugin.class);

    private enum ModulatorSort {Aggregate, Enhancing, Negative;}

    private static final int DEFAULT_MODULATOR_LIMIT = 10;

    private AggregateTableModel aggregateModel;
    private ModulatorTargetModel modTargetModel;
    private ModulatorModel modulatorModel;

    private JCheckBox selectionEnabledCheckBox;
    private JList heatMapModNameList;

    private JScrollPane heatMapScrollPane;
    private List<DSGeneMarker> modulators;
    private List<DSGeneMarker> transFactors;
    private MindyData mindyData;
    private JXTable listTable;
    private JCheckBox selectAllModsCheckBox;
    private JCheckBox selectAllTargetsCheckBox;
    private JButton addToSetButton;

    // Contains the state of selections passed in from the marker panel and overrides via All Markers checkboxes
    private MarkerLimitState globalSelectionState = new MarkerLimitState();

    public MindyPlugin(MindyData data) {
        this.mindyData = data;
//        log.debug("Doing mean variance...");
//        logNormalize(data.getArraySet());
//        markerMeanVariance(data.getArraySet());
//        log.debug("Done.");
        modulators = mindyData.getModulators();


        JTabbedPane tabs = new JTabbedPane();
        {
            // Modulator Table

            JPanel panel = new JPanel(new BorderLayout());

            modulatorModel = new ModulatorModel(mindyData);
            final JXTable table = new JXTable(modulatorModel);

            JScrollPane scrollPane = new JScrollPane(table);
            table.setHorizontalScrollEnabled(true);
            restoreBooleanRenderers(table);
//            table.setPreferredScrollableViewportSize(new Dimension(500, 70));
//            table.setColumnControlVisible(true);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setHighlighters(new HighlighterPipeline(new Highlighter[]{AlternateRowHighlighter.genericGrey}));
            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
            renderer.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            table.getColumnModel().getColumn(5).setCellRenderer(renderer);
//            table.getColumnModel().getColumn(5).setHeaderRenderer(renderer);
            table.packTable(DEFAULT_MODULATOR_LIMIT);


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
            final JCheckBox allMarkersCheckBox = makeAllMarkersCheckBox();
            markerPanelOverride.add(allMarkersCheckBox);

            JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
            taskContainer.add(markerSetPane);
            taskContainer.add(markerPanelOverride);
            panel.add(taskContainer, BorderLayout.WEST);

            tabs.add("Modulator", panel);
        }

        {
            // Modulator / Target Table
            Panel panel = new Panel(new BorderLayout());

            JXTaskPane sortPane = new JXTaskPane();
            sortPane.setTitle("Sorting");
            JComboBox sortOptions = new JComboBox();
            sortOptions.addItem(ModulatorSort.Aggregate);
            sortOptions.addItem(ModulatorSort.Enhancing);
            sortOptions.addItem(ModulatorSort.Negative);
            sortOptions.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    JComboBox cb = (JComboBox) actionEvent.getSource();
                    ModulatorSort selected = (ModulatorSort) cb.getSelectedItem();
                    if (selected == ModulatorSort.Aggregate) {
                        log.debug("Setting sort to Aggregate");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Aggregate);
                    } else if (selected == ModulatorSort.Enhancing) {
                        log.debug("Setting sort to Enhacing");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Enhancing);
                    } else {
                        log.debug("Setting sort to Negative");
                        aggregateModel.setModulatorSortMethod(ModulatorSort.Negative);
                    }
                }
            });
            sortPane.add(sortOptions);

            JXTaskPane limitPane = new JXTaskPane();
            Panel limitControls = new Panel(new BorderLayout());
            limitPane.setTitle("Modulator Limits");
            final JCheckBox modulatorLimits = new JCheckBox("Limit To Top");
            modulatorLimits.setSelected(true);
            limitControls.add(modulatorLimits, BorderLayout.WEST);
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(DEFAULT_MODULATOR_LIMIT, 1, 1000, 1);
            final JSpinner modLimitValue = new JSpinner(spinnerModel);
//            modLimitValue.setValue(DEFAULT_MODULATOR_LIMIT);
            limitControls.add(modLimitValue, BorderLayout.EAST);
            limitPane.add(limitControls);

            JXTaskPane tableOptionsPane = new JXTaskPane();
            tableOptionsPane.setTitle("Display Options");
            final JCheckBox colorCheck = new JCheckBox("Color View");
            tableOptionsPane.add(colorCheck);
            final JCheckBox scoreCheck = new JCheckBox("Score View");
            tableOptionsPane.add(scoreCheck);

            final ColorGradient gradient = new ColorGradient(Color.black, Color.yellow);
            gradient.addColorPoint(Color.red, 0f);

            aggregateModel = new AggregateTableModel(mindyData);
            aggregateModel.setModLimit(DEFAULT_MODULATOR_LIMIT);
            aggregateModel.setModulatorsLimited(modulatorLimits.isSelected());
            final JXTable table = new JXTable(aggregateModel) {
                public Component prepareRenderer(TableCellRenderer tableCellRenderer, int row, int col) {
                    Component component = super.prepareRenderer(tableCellRenderer, row, col);
                    if (colorCheck.isSelected() && col > 1) {
                        float score = aggregateModel.getScoreAt(row, col);
                        component.setBackground(gradient.getColor(score));
                    }
                    return component;
                }
            };
            table.setAutoCreateColumnsFromModel(true);
            JScrollPane scrollPane = new JScrollPane(table);
            table.setHorizontalScrollEnabled(true);
            restoreBooleanRenderers(table);

            colorCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    table.invalidate();
                    table.repaint();
                }
            });

            scoreCheck.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    aggregateModel.setScoreView(scoreCheck.isSelected());
                    table.invalidate();
                    table.repaint();
                }
            });

            modulatorLimits.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    Integer modLimit = (Integer) modLimitValue.getValue();
                    log.debug("Limiting modulators displayed to top " + modLimit);
                    boolean selected = modulatorLimits.isSelected();
                    limitModulators(modLimit, selected, table);
                }
            });

            modLimitValue.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent changeEvent) {
                    if (modulatorLimits.isSelected()) {
                        limitModulators((Integer) modLimitValue.getValue(), true, table);
                    }
                }
            });

//            TableColumnModel columnModel = table.getColumnModel();
//            TableColumn column = columnModel.getColumn(2);
//            column.setCellRenderer(new ScoreColorRenderer(mindyData));

            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setHighlighters(new HighlighterPipeline(new Highlighter[]{AlternateRowHighlighter.genericGrey}));

            table.packAll();
            panel.add(scrollPane, BorderLayout.CENTER);

            JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
            taskContainer.add(sortPane);
            taskContainer.add(limitPane);
            taskContainer.add(tableOptionsPane);
            panel.add(taskContainer, BorderLayout.WEST);

            tabs.add("Table", panel);
        }

        {
            // Modulator / Target list

            JPanel panel = new JPanel(new BorderLayout());

            JXTaskPane markerSetPane = new JXTaskPane();
            markerSetPane.setTitle("List Selections");

            modTargetModel = new ModulatorTargetModel(mindyData);
            listTable = new JXTable(modTargetModel);
            listTable.setBackground(new Color(245, 245, 245));
//            ModulatorHighlighter modulatorHighlighter = new ModulatorHighlighter();
//            modulatorHighlighter.setBackground(new Color(233, 233, 233));
//            Highlighter[] highlighters = new Highlighter[]{modulatorHighlighter};
//            listTable.setHighlighters(new HighlighterPipeline(highlighters));
            listTable.setHighlighters(new HighlighterPipeline(new Highlighter[]{AlternateRowHighlighter.genericGrey}));
            JScrollPane scrollPane = new JScrollPane(listTable);
            listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            restoreBooleanRenderers(listTable);

            selectionEnabledCheckBox = new JCheckBox("Enable Selection");
            markerSetPane.add(selectionEnabledCheckBox);
            selectionEnabledCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    boolean selected = selectionEnabledCheckBox.isSelected();
                    log.debug("Setting list box visibility to " + selected);
                    setListCheckboxesVisibility(selected);
                    setListControlVisibility(selected);
                    setListTableViewOptions();
                }
            });

            setListTableViewOptions();

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
            final JCheckBox allMarkers = makeAllMarkersCheckBox();
            markerPanelOverride.add(allMarkers);

            JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
            taskContainer.add(markerSetPane);
//            taskContainer.add(markerPanelOverride);
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
            ModulatorHeatMap heatmap = new ModulatorHeatMap(modulator, transFactors.iterator().next(), mindyData, null);
            heatMapScrollPane = new JScrollPane(heatmap);
            panel.add(heatMapScrollPane, BorderLayout.CENTER);

            JXTaskPane transFacPane = new JXTaskPane();
            transFacPane.setTitle("Transcription Factor");
            JLabel transFactorName = new JLabel(mindyData.getTranscriptionFactor().getShortName());
            transFacPane.add(transFactorName);

            JXTaskPane modulatorPane = new JXTaskPane();
            JTextField modFilterField = new JTextField();
//            modulatorPane.setExpanded(false);
            modulatorPane.setTitle("Modulators");
            heatMapModNameList = new JList();
            JScrollPane modListScrollPane = new JScrollPane(heatMapModNameList);
            heatMapModNameList.setModel(new ModulatorListModel());
            heatMapModNameList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent listSelectionEvent) {
                    rebuildHeatMap();
                }
            });
            AutoCompleteDecorator.decorate(heatMapModNameList, modFilterField);
            JButton refreshButton = new JButton("Refresh HeatMap");
            refreshButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    rebuildHeatMap();
                }
            });
            modulatorPane.add(modFilterField);
            modulatorPane.add(modListScrollPane);
            modulatorPane.add(refreshButton);

            JXTaskPaneContainer taskContainer = new JXTaskPaneContainer();
            taskContainer.add(transFacPane);
            taskContainer.add(modulatorPane);
            JScrollPane modTransScroll = new JScrollPane(taskContainer);
            panel.add(modTransScroll, BorderLayout.WEST);
            tabs.add("Heat Map", panel);
        }

        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);
    }

    private void limitModulators(Integer modLimit, boolean selected, JXTable table) {
        aggregateModel.setModLimit(modLimit);
        aggregateModel.setModulatorsLimited(selected);
        aggregateModel.fireTableStructureChanged();
        table.packAll();
        table.repaint();
    }

    private void rebuildHeatMap() {
        int selectedIndex = heatMapModNameList.getSelectedIndex();
        DSGeneMarker modMarker;
        if (selectedIndex > 0) {
            modMarker = modTargetModel.getEnabledModulators().get(selectedIndex);
        } else {
            modMarker = modTargetModel.getEnabledModulators().get(0);
        }

        if (modMarker != null) {
            log.debug("Rebuilding heat map.");
            setHeatMap(new ModulatorHeatMap(modMarker, mindyData.getTranscriptionFactor(), mindyData, aggregateModel.getCheckedTargets()));
        }
    }

    private void restoreBooleanRenderers(JXTable table) {
        // Something in workbench is overriding these renderers
        table.setDefaultEditor(Boolean.class, new DefaultCellEditor(new JCheckBox()));
        table.setDefaultRenderer(Boolean.class, new CheckboxCellRenderer());
    }

    private JCheckBox makeAllMarkersCheckBox() {
        final JCheckBox allMarkers = new JCheckBox("All Markers");
        allMarkers.setSelected(true);
        allMarkers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                globalSelectionState.allMarkerOverride = allMarkers.isSelected();
                setAllMarkersOverride(allMarkers.isSelected());
            }
        });
        return allMarkers;
    }

    private void setListTableViewOptions() {
        boolean selected = selectionEnabledCheckBox.isSelected();
        if (selected) {
            listTable.getColumnExt(0).setMaxWidth(20);
            listTable.getColumnExt(2).setMaxWidth(20);
        }
        setListCheckboxesVisibility(selected);
        listTable.packAll();
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

    public void limitMarkers(List<DSGeneMarker> markers) {
        globalSelectionState.globalUserSelection = markers;
        modulatorModel.limitMarkers(markers);
        if (!globalSelectionState.allMarkerOverride) {
            aggregateModel.fireTableStructureChanged();
            modTargetModel.fireTableDataChanged();
        }
    }

    private void setAllMarkersOverride(boolean allMarkers) {
        globalSelectionState.allMarkerOverride = allMarkers;
        aggregateModel.fireTableStructureChanged();
        modTargetModel.fireTableDataChanged();
        modulatorModel.fireTableDataChanged();
    }

    /**
     * Models and support classes follow
     */

    private class ModulatorModel extends DefaultTableModel {

        private boolean[] enabled;
        private List<DSGeneMarker> modulators;
        private List<DSGeneMarker> limitedModulators = new ArrayList<DSGeneMarker>();
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

        public void limitMarkers(List<DSGeneMarker> limitList) {
            if (limitList == null) {
                limitedModulators = null;
                log.debug("Cleared modulator limits.");
            } else {
                limitedModulators = new ArrayList<DSGeneMarker>();
                for (DSGeneMarker marker : limitList) {
                    if (modulators.contains(marker)) {
                        limitedModulators.add(marker);
                    }
                }
                log.debug("Limited modulators table to " + limitedModulators.size() + " mods.");
                this.enabled = new boolean[modulators.size()];
            }
            if (!globalSelectionState.allMarkerOverride) {
                fireTableDataChanged();
            }
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (globalSelectionState.allMarkerOverride) {
                if (enabled == null) {
                    return 0;
                } else {
                    return modulators.size();
                }
            } else {
                if (limitedModulators == null) {
                    return 0;
                }
                return limitedModulators.size();
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
            DSGeneMarker mod;
            mod = getModulatorForIndex(rowIndex);
            if (columnIndex == 0) {
                return enabled[rowIndex];
            } else if (columnIndex == 1) {
                return mod.getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 2) {
                return mindyData.getStatistics(mod).getCount();
            } else if (columnIndex == 3) {
                return mindyData.getStatistics(mod).getMover();
            } else if (columnIndex == 4) {
                return mindyData.getStatistics(mod).getMunder();
            } else if (columnIndex == 5) {
                int mover = mindyData.getStatistics(mod).getMover();
                int munder = mindyData.getStatistics(mod).getMunder();
                if (mover > munder) {
                    return "+";
                } else if (mover < munder) {
                    return "-";
                } else {
                    return "=";
                }
            } else {
                return mod.getDescription();
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                enableModulator(rowIndex, (Boolean) aValue);
            }
        }

        private DSGeneMarker getModulatorForIndex(int rowIndex) {
            DSGeneMarker mod;
            if (globalSelectionState.allMarkerOverride) {
                mod = modulators.get(rowIndex);
            } else {
                mod = limitedModulators.get(rowIndex);
            }
            return mod;
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        public void selectAllModulators(boolean selected) {
            for (int i = 0; i < enabled.length; i++) {
                enableModulator(i, selected);
            }
        }

        private void enableModulator(int rowIndex, boolean enable) {
            enabled[rowIndex] = enable;
            DSGeneMarker mod = getModulatorForIndex(rowIndex);
            if (enabled[rowIndex]) {
                aggregateModel.enableModulator(mod);
                modTargetModel.enableModulator(mod);
                ModulatorListModel model = (ModulatorListModel) heatMapModNameList.getModel();
                model.refresh();
            } else {
                aggregateModel.disableModulator(mod);
                modTargetModel.disableModulator(mod);
                ModulatorListModel model = (ModulatorListModel) heatMapModNameList.getModel();
                model.refresh();
            }
        }

    }

    private class AggregateTableModel extends DefaultTableModel {

        private static final int EXTRA_COLS = 2;

        private boolean[] checkedTargets;
        private List<DSGeneMarker> allModulators;
        private List<DSGeneMarker> enabledModulators;
        private List<DSGeneMarker> activeTargets;
        private MindyData mindyData;
        private boolean scoreView = false;
        private boolean modulatorsLimited = false;
        private int modLimit = DEFAULT_MODULATOR_LIMIT;
        private ModulatorSort modulatorSortMethod = ModulatorSort.Aggregate;

        public AggregateTableModel(MindyData mindyData) {
            this.checkedTargets = new boolean[mindyData.getData().size()];
            this.mindyData = mindyData;
            allModulators = mindyData.getModulators();
            enabledModulators = new ArrayList<DSGeneMarker>();
            activeTargets = new ArrayList<DSGeneMarker>();
        }

        public boolean isScoreView() {
            return scoreView;
        }

        public void setScoreView(boolean scoreView) {
            this.scoreView = scoreView;
        }

        public ModulatorSort getModulatorSortMethod() {
            return modulatorSortMethod;
        }

        public void setModulatorSortMethod(ModulatorSort modulatorSortMethod) {
            this.modulatorSortMethod = modulatorSortMethod;
            resortModulators();
        }

        public boolean isModulatorsLimited() {
            return modulatorsLimited;
        }

        public void setModulatorsLimited(boolean modulatorsLimited) {
            this.modulatorsLimited = modulatorsLimited;
        }

        public int getModLimit() {
            return modLimit;
        }

        public void setModLimit(int modLimit) {
            this.modLimit = modLimit;
        }

        public List<DSGeneMarker> getEnabledModulators() {
            return enabledModulators;
        }

        public void setEnabledModulators(List<DSGeneMarker> enabledModulators) {
            this.enabledModulators = enabledModulators;
        }

        public List<DSGeneMarker> getCheckedTargets() {
            ArrayList<DSGeneMarker> result = new ArrayList<DSGeneMarker>();
            for (int i = 0; i < checkedTargets.length; i++) {
                if (checkedTargets[i]) {
                    result.add(activeTargets.get(i));
                }
            }
            return result;
        }

        public void enableModulator(DSGeneMarker mod) {
            if (!enabledModulators.contains(mod)) {
                enabledModulators.add(mod);
                recalcActiveTargets();
                resortModulators();     // This also fires structure changed
                repaint();
            }
        }

        public void disableModulator(DSGeneMarker mod) {
            enabledModulators.remove(mod);
            recalcActiveTargets();
            resortModulators();     // This also fires structure changed
            repaint();
        }

        private void recalcActiveTargets() {
            activeTargets.clear();
            for (DSGeneMarker modMarker : enabledModulators) {
                List<MindyData.MindyResultRow> rows = mindyData.getRows(modMarker, mindyData.getTranscriptionFactor());
                for (MindyData.MindyResultRow row : rows) {
                    DSGeneMarker target = row.getTarget();
                    if (!activeTargets.contains(target)) {
                        activeTargets.add(target);
                    }
                }
            }
            fireTableDataChanged();
        }

        public int getColumnCount() {
            // Number of allModulators plus target name and checkbox column
            if (!modulatorsLimited) {
                return enabledModulators.size() + EXTRA_COLS;
            } else {
                return Math.min(modLimit + EXTRA_COLS, enabledModulators.size() + EXTRA_COLS);
            }
        }

        public int getRowCount() {
            if (activeTargets == null) {
                return 0;
            }
            return activeTargets.size();
        }

        public Class<?> getColumnClass(int i) {
            if (i == 0) {
                return String.class;
            } else if (i == 1) {
                return Boolean.class;
            } else {
                return Float.class;
            }
        }

        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return activeTargets.get(row).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (col == 1) {
                return checkedTargets[row];
            } else {
                float score = mindyData.getScore(enabledModulators.get(col - EXTRA_COLS), mindyData.getTranscriptionFactor(), activeTargets.get(row));
                if (score != 0) {
                    if (scoreView) {
                        return score;
                    } else {
                        return Math.signum(score) * 1;
                    }
                } else {
                    return score;
                }
            }
        }

        public float getScoreAt(int row, int col) {
            float score = mindyData.getScore(allModulators.get(col - EXTRA_COLS), mindyData.getTranscriptionFactor(), activeTargets.get(row));
            return score;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                checkedTargets[rowIndex] = (Boolean) aValue;
                if (checkedTargets[rowIndex]) {
                    DSGeneMarker mod = activeTargets.get(rowIndex);
                    modTargetModel.enableTarget(mod);
                } else {
                    DSGeneMarker mod = activeTargets.get(rowIndex);
                    modTargetModel.disableTarget(mod);
                }
            }
        }

        public String getColumnName(int col) {
            if (col == 0) {
                return "Target";
            } else if (col == 1) {
                return " ";
            } else {
                DSGeneMarker mod = enabledModulators.get(col - EXTRA_COLS);
                String colName =  mod.getShortName();
                if (modulatorSortMethod == ModulatorSort.Aggregate) {
                    colName += " (M# " + mindyData.getStatistics(mod).getCount() + ")";
                } else if (modulatorSortMethod == ModulatorSort.Enhancing) {
                    colName += " (M+ " + mindyData.getStatistics(mod).getMover() + ")";
                } else if (modulatorSortMethod == ModulatorSort.Negative) {
                    colName += " (M- " + mindyData.getStatistics(mod).getMunder() + ")";
                }
                return colName;
            }
        }

        public void resortModulators() {
            Collections.sort(enabledModulators, new ModulatorStatComparator(mindyData, modulatorSortMethod));
            fireTableStructureChanged();
        }

    }

    private class ModulatorStatComparator implements Comparator<DSGeneMarker> {

        private MindyData data;
        private ModulatorSort sortType;

        public ModulatorStatComparator(MindyData data, ModulatorSort sortType) {
            this.data = data;
            this.sortType = sortType;
        }

        public int compare(DSGeneMarker dsGeneMarker, DSGeneMarker dsGeneMarker1) {
            if (sortType == ModulatorSort.Aggregate) {
                return data.getStatistics(dsGeneMarker1).getCount() - data.getStatistics(dsGeneMarker).getCount();
            } else if (sortType == ModulatorSort.Enhancing) {
                return data.getStatistics(dsGeneMarker1).getMover() - data.getStatistics(dsGeneMarker).getMover();
            } else {
                return data.getStatistics(dsGeneMarker1).getMunder() - data.getStatistics(dsGeneMarker).getMunder();
            }
        }

    }

    private class ModulatorTargetModel extends DefaultTableModel {

        private boolean[] modChecks;
        private boolean[] targetChecks;
        private ArrayList<DSGeneMarker> enabledModulators = new ArrayList<DSGeneMarker>();
        private ArrayList<DSGeneMarker> enabledTargets = new ArrayList<DSGeneMarker>();
        private MindyData mindyData;
        private String[] columnNames = new String[]{" ", "Modulator", "  ", "Target", "Score"};
        private ArrayList<MindyData.MindyResultRow> rows = new ArrayList<MindyData.MindyResultRow>();

        public ModulatorTargetModel(MindyData mindyData) {
            this.modChecks = new boolean[mindyData.getData().size()];
            this.targetChecks = new boolean[mindyData.getData().size()];
            this.mindyData = mindyData;
        }

        public ArrayList<DSGeneMarker> getEnabledModulators() {
            return enabledModulators;
        }

        public void setEnabledModulators(ArrayList<DSGeneMarker> enabledModulators) {
            this.enabledModulators = enabledModulators;
        }

        public ArrayList<DSGeneMarker> getEnabledTargets() {
            return enabledTargets;
        }

        public void setEnabledTargets(ArrayList<DSGeneMarker> enabledTargets) {
            this.enabledTargets = enabledTargets;
        }

        public void enableModulator(DSGeneMarker mod) {
            if (!enabledModulators.contains(mod)) {
                enabledModulators.add(mod);
                recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }

        public void disableModulator(DSGeneMarker mod) {
            enabledModulators.remove(mod);
            recalculateRows();
            fireTableStructureChanged();
            setListTableViewOptions();
            repaint();
        }

        public void enableTarget(DSGeneMarker target) {
            if (!enabledTargets.contains(target)) {
                enabledTargets.add(target);
                recalculateRows();
                fireTableDataChanged();
                setListTableViewOptions();
                repaint();
            }
        }

        public void disableTarget(DSGeneMarker target) {
            enabledTargets.remove(target);
            recalculateRows();
            fireTableDataChanged();
            setListTableViewOptions();
            repaint();
        }

        private void recalculateRows() {
            rows.clear();
            for (DSGeneMarker modMarker : enabledModulators) {
                List<DSGeneMarker> targets = mindyData.getTargets(modMarker, mindyData.getTranscriptionFactor());
                for (DSGeneMarker target : targets) {
                    if (enabledTargets.contains(target)) {
                        rows.add(mindyData.getRow(modMarker, mindyData.getTranscriptionFactor(), target));
                    }
                }
            }
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (modChecks == null) {
                return 0;
            } else {
                return rows.size();
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
                return modChecks[rowIndex];
            } else if (columnIndex == 1) {
                return rows.get(rowIndex).getModulator().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 2) {
                return targetChecks[rowIndex];
            } else if (columnIndex == 3) {
                return rows.get(rowIndex).getTarget().getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
            } else if (columnIndex == 4) {
                return rows.get(rowIndex).getScore();
            } else {
                log.error("Requested unknown column");
                return null;
            }
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                modChecks[rowIndex] = (Boolean) aValue;
            } else if (columnIndex == 2) {
                targetChecks[rowIndex] = (Boolean) aValue;
            }
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        private void selectAllModulators(boolean select) {
            for (int i = 0; i < modChecks.length; i++) {
                modChecks[i] = select;
            }
            listTable.repaint();
        }

        private void selectAllTargets(boolean select) {
            for (int i = 0; i < targetChecks.length; i++) {
                targetChecks[i] = select;
            }
            listTable.repaint();
        }

    }

    private class ScoreColorRenderer extends DefaultTableCellRenderer {
        MindyData data;

        public ScoreColorRenderer(MindyData data) {
            this.data = data;
        }

        public Component getTableCellRendererComponent(JTable jTable, Object object, boolean b, boolean b1, int i, int i1) {
            setValue(object);
            this.setBackground(Color.red);
            return this;
        }
    }

    private class ModulatorListModel extends AbstractListModel {
        public int getSize() {
            return modTargetModel.getEnabledModulators().size();
        }

        public Object getElementAt(int i) {
            return modTargetModel.getEnabledModulators().get(i).getShortName(ModulatorHeatMap.MAX_MARKER_NAME_CHARS);
        }

        public void refresh() {
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * State of selections and overrides for the entire component
     */
    private class MarkerLimitState {
        public List<DSGeneMarker> globalUserSelection = new ArrayList<DSGeneMarker>();
        public boolean allMarkerOverride = true;
    }


    /**
     * This doesn't quite work, but it's close.
     */
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
