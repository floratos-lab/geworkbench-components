package org.geworkbench.components.mindy;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.awt.*;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author mhall
 */
public class MindyPlugin extends JPanel {

    static Log log = LogFactory.getLog(MindyPlugin.class);

    private JScrollPane heatMapScrollPane;
    private List<DSGeneMarker> modulators;
    private List<DSGeneMarker> transFactors;
    private MindyData mindyData;

    public MindyPlugin(MindyData data) {
        this.mindyData = data;
        modulators = mindyData.getModulators();

        Model model = new Model(mindyData);

        JTabbedPane tabs = new JTabbedPane();

        {
            // Modulator Table

            JXTable jxTable = new JXTable(model);
            JScrollPane scrollPane = new JScrollPane(jxTable);
            jxTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
//        jxTable.setColumnControlVisible(true);
            jxTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            jxTable.packAll();
            tabs.add("Modulator", scrollPane);
        }

        {
            // Heat Maps
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
                    return transFactors.get(i).getShortName();
                }

                public void addListDataListener(ListDataListener listDataListener) {
                }

                public void removeListDataListener(ListDataListener listDataListener) {
                }
            });

            JXTaskPane modulatorPane = new JXTaskPane();
            modulatorPane.setExpanded(false);
            modulatorPane.setTitle("Modulators");
            String[] modNames = new String[modulators.size()];
            int i = 0;
            for (DSGeneMarker dsGeneMarker : modulators) {
                modNames[i] = dsGeneMarker.getShortName();
                i++;
            }
            final JList modNameList = new JList();
            modNameList.setModel(new ListModel() {
                public int getSize() {
                    return modulators.size();
                }

                public Object getElementAt(int i) {
                    return modulators.get(i).getShortName();
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
            modulatorPane.add(modNameList);

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

    private void setHeatMap(ModulatorHeatMap heatMap) {
        heatMapScrollPane.getViewport().setView(heatMap);
        heatMapScrollPane.repaint();
    }

    private class Model extends DefaultTableModel {

        private boolean[] enabled;
        private List<DSGeneMarker> modulators;
        private MindyData mindyData;
        private String[] columnNames = new String[]{"", "Modulator", "M#", "M+", "M-", "Mode", "Modulator Description"};

        public Model(MindyData mindyData) {
            modulators = new ArrayList<DSGeneMarker>();
            for (Map.Entry<DSGeneMarker, MindyData.ModulatorStatistics> entry : mindyData.getAllModulatorStatistics().entrySet())
            {
                modulators.add(entry.getKey());
            }
            this.enabled = new boolean[modulators.size()];
            this.mindyData = mindyData;
        }

        public int getColumnCount() {
            return 7;
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
                return modulators.get(rowIndex).getShortName();
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

    }


    static class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckboxRenderer() {
            super();
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            if (((JCheckBox) value).isSelected()) {
                setSelected(true);
            } else {
                setSelected(false);
            }
            setHorizontalAlignment(SwingConstants.CENTER);
            if (table.getSelectedRow() == row) {
                setBackground(Color.blue);
            } else {
                setBackground(Color.white);
            }
            return this;
        }
    }
}
