package org.geworkbench.components.selectors;

import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.engine.management.*;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.util.Util;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Set;
import java.util.HashSet;

import com.Ostermiller.util.CSVPrinter;
import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.ExcelCSVParser;

/**
 * A panel that handles the creation and management of gene panels, as well as individual gene selection.
 *
 * @author John Watkinson
 */
public class GenePanel extends SelectorPanel<DSGeneMarker> {

    /**
     * <code>FileFilter</code> that is used by the <code>JFileChoose</code> to
     * show just panel set files on the filesystem
     */
    protected static class MarkerPanelSetFileFilter extends javax.swing.filechooser.FileFilter {
        private String fileExt;

        MarkerPanelSetFileFilter() {
            fileExt = ".csv";
        }

        public String getExtension() {
            return fileExt;
        }

        public String getDescription() {
            return "Comma Separated Values Files";
        }

        public boolean accept(File f) {
            boolean returnVal = false;
            if (f.isDirectory() || f.getName().endsWith(fileExt)) {
                return true;
            }
            return returnVal;
        }
    }

    public GenePanel() {
        super(DSGeneMarker.class, "Gene");
        // Add gene panel specific menu items.
        treePopup.add(savePanelItem);
        rootPopup.add(loadPanelItem);
        savePanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveButtonPressed(rightClickedPath);
            }
        });
        exportPanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportPanelPressed();
            }
        });
        loadPanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadButtonPressed();
            }
        });
        // Load button at bottom of component
        JPanel loadPanel = new JPanel();
        loadPanel.setLayout(new BoxLayout(loadPanel, BoxLayout.X_AXIS));
        JButton loadButton = new JButton("Load Panel");
        loadPanel.add(loadButton);
        loadPanel.add(Box.createHorizontalGlue());
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadButtonPressed();
            }
        });
        lowerPanel.add(loadPanel);
    }

    private JMenuItem savePanelItem = new JMenuItem("Save");
    private JMenuItem loadPanelItem = new JMenuItem("Load Panel");
    private JMenuItem exportPanelItem = new JMenuItem("Export");

    private void saveButtonPressed(TreePath path) {
        String label = getLabelForPath(path);
        if (label != null) {
            JFileChooser fc = new JFileChooser(".");
            FileFilter filter = new MarkerPanelSetFileFilter();
            fc.setFileFilter(filter);
            fc.setDialogTitle("Save Marker Panel");
            String extension = ((MarkerPanelSetFileFilter) filter).getExtension();
            int choice = fc.showSaveDialog(mainPanel.getParent());
            if (choice == JFileChooser.APPROVE_OPTION) {
                String filename = fc.getSelectedFile().getAbsolutePath();
                if (!filename.endsWith(extension)) {
                    filename += extension;
                }
                boolean confirmed = true;
                if (new File(filename).exists()) {
                    int confirm = JOptionPane.showConfirmDialog(getComponent(), "Replace existing file?");
                    if (confirm != JOptionPane.YES_OPTION) {
                        confirmed = false;
                    }
                }
                if (confirmed) {
                    DSPanel<DSGeneMarker> panel = context.getItemsWithLabel(label);
                    serializePanel(filename, panel);
                }
            }
        }
    }

    private void loadButtonPressed() {
        JFileChooser fc = new JFileChooser(".");
        javax.swing.filechooser.FileFilter filter = new MarkerPanelSetFileFilter();
        fc.setFileFilter(filter);
        fc.setDialogTitle("Open Marker Panel");
        int choice = fc.showOpenDialog(mainPanel.getParent());
        if (choice == JFileChooser.APPROVE_OPTION) {
            DSPanel<DSGeneMarker> panel = deserializePanel(fc.getSelectedFile());
            addPanel(panel);
            throwLabelEvent();
        }
    }

    private void exportPanelPressed() {
        JOptionPane.showMessageDialog(getComponent(), "To be implemented...");
        // todo
    }

    protected void throwLabelEvent() {
        GeneSelectorEvent event = null;
        event = new GeneSelectorEvent(context.getLabelTree());
        if (event != null) {
            publishGeneSelectorEvent(event);
        }
    }

    /**
     * Utility to save a panel to the filesystem as CSV.
     * <p>
     * Format:
     * <p>
     * File name (without .CSV extension) is the name of the panel.
     * <p>
     * Rows of the file contains the label of markers, in order. Only the first column is used.
     *
     * @param filename filename to which the current panel is to be saved.
     */
    private void serializePanel(String filename, DSPanel<DSGeneMarker> panel) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(filename);
            CSVPrinter out = new CSVPrinter(fileWriter);
            if (panel != null && panel.size() > 0) {
                for (int i = 0; i < panel.size(); i++) {
                    DSGeneMarker marker = panel.get(i);
                    out.println(marker.getLabel());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    // Lost cause-- ignore
                }
            }
        }
    }

    /**
     * Utility to obtain the stored panel sets from the filesystem
     *
     * @param file file which contains the stored panel set
     */
    private DSPanel<DSGeneMarker> deserializePanel(final File file) {
        FileInputStream inputStream = null;
        String filename = file.getName();
        if (filename.toLowerCase().endsWith(".csv")) {
            filename = filename.substring(0, filename.length() - 4);
        }
        // Ensure loaded file has unique name
        Set<String> nameSet = new HashSet<String>();
        int n = context.getNumberOfLabels();
        for (int i = 0; i < n; i++) {
            nameSet.add(context.getLabel(i));
        }
        filename = Util.getUniqueName(filename, nameSet);
        DSPanel<DSGeneMarker> panel = new CSPanel<DSGeneMarker>(filename);
        try {
            inputStream = new FileInputStream(file);
            ExcelCSVParser parser = new ExcelCSVParser(inputStream);
            String[][] data = parser.getAllValues();
            for (int i = 0; i < data.length; i++) {
                String[] line = data[i];
                if (line.length > 0) {
                    String label = line[0];
                    DSGeneMarker marker = itemList.get(label);
                    if (marker != null) {
                        panel.add(marker);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Lost cause
                }
            }
        }
        return panel;
    }

    protected void dataSetChanged(DSDataSet dataSet) {
        DSItemList items;
        if (dataSet instanceof DSMicroarraySet) {
            DSMicroarraySet maSet = (DSMicroarraySet) dataSet;
            items = maSet.getMarkers();
            setItemList(items);
        } else if (dataSet instanceof SequenceDB) {
            items = (DSItemList) ((SequenceDB) dataSet).getMarkerList();
            setItemList(items);
        }
    }

    /**
     * Called when a single marker is selected by a component.
     */
    @Subscribe public void receive(MarkerSelectedEvent event, Object source) {
        JList list = itemAutoList.getList();
        int index = itemList.indexOf(event.getMarker());
        list.setSelectedIndex(index);
        list.scrollRectToVisible(list.getCellBounds(index, index));
    }

    /**
     * Called when a component wishes to add, change or remove a panel.
     */
    @Subscribe(Overflow.class) public void receive(org.geworkbench.events.SubpanelChangedEvent spe, Object source) {
        DSPanel<DSGeneMarker> receivedPanel = spe.getPanel();
        String panelName = receivedPanel.getLabel();
        switch (spe.getMode()) {
            case SubpanelChangedEvent.NEW: {
                if (context.indexOfLabel(panelName) != -1) {
                    int number = 1;
                    String newName = panelName + " (" + number + ")";
                    receivedPanel.setLabel(newName);
                    while (context.indexOfLabel(newName) != -1) {
                        number++;
                        newName = panelName + " (" + number + ")";
                        receivedPanel.setLabel(newName);
                    }
                }
                addPanel(receivedPanel);
                break;
            }
            case SubpanelChangedEvent.SET_CONTENTS: {
                boolean foundPanel = false;
                if (context.indexOfLabel(panelName) != -1) {
                    foundPanel = true;
                    // Delete everything from the panel and re-add
                    context.clearItemsFromLabel(panelName);
                    for (DSGeneMarker marker : receivedPanel) {
                        context.labelItem(marker, panelName);
                    }
                    synchronized (treeModel) {
                        treeModel.fireLabelItemsChanged(panelName);
                    }
                    throwLabelEvent();
                }
                if (!foundPanel) {
                    // Add it as a new panel
                    addPanel(receivedPanel);
                }
                break;
            }
            case SubpanelChangedEvent.DELETE: {
                if (context.indexOfLabel(panelName) != -1) {
                    int index = context.indexOfLabel(panelName);
                    context.removeLabel(panelName);
                    treeModel.fireLabelRemoved(panelName, index);
                }
                break;
            }
            default:
                throw new RuntimeException("Unknown subpanel changed event mode: " + spe.getMode());
        }
    }

    @Publish public SubpanelChangedEvent publishSubpanelChangedEvent(SubpanelChangedEvent event) {
        return event;
    }

    @Publish public GeneSelectorEvent publishGeneSelectorEvent(GeneSelectorEvent event) {
        return event;
    }

    protected void publishSingleSelectionEvent(DSGeneMarker item) {
        publishGeneSelectorEvent(new GeneSelectorEvent(item));
    }

}
