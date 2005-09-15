package org.geworkbench.components.selectors;

import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.MarkerSelectedEvent;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.management.*;
import org.geworkbench.util.JAutoList;
import org.geworkbench.util.sequences.SequenceDB;
import org.geworkbench.util.visualproperties.PanelVisualProperties;
import org.geworkbench.util.visualproperties.PanelVisualPropertiesManager;
import org.geworkbench.util.visualproperties.VisualPropertiesDialog;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSItemList;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.util.CSMarkerManager;
import org.geworkbench.engine.config.VisualPlugin;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A panel that handles the creation and management of gene panels, as well as individual gene selection.
 *
 * @author John Watkinson
 */
public class GenePanel implements VisualPlugin {

    //// Classes

    /**
     * Printable that is responsible for printing the contents of a panel.
     */
    private static class PrintListingPainter implements Printable {
        private Font fnt = new Font("Helvetica", Font.PLAIN, 8);
        private int rememberedPageIndex = -1;
        private long rememberedFilePointer = -1;
        private boolean rememberedEOF = false;
        private int index = 0;
        private int lastIndex = 0;
        DSGeneMarker gm = null;
        DSPanel<DSGeneMarker> panel;

        public PrintListingPainter(DSPanel<DSGeneMarker> panel) {
            this.panel = panel;
        }

        /**
         * Called by the print job.
         */
        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            DecimalFormat format = new DecimalFormat("#.####");
            try {
                int itemNo = Math.min(panel.size(), 500);
                // For catching IOException
                if (pageIndex != rememberedPageIndex) {
                    // First time we've visited this page
                    rememberedPageIndex = pageIndex;
                    lastIndex = index;
                    // If encountered EOF on previous page, done
                    if (rememberedEOF) {
                        return Printable.NO_SUCH_PAGE;
                    }
                    // Save current position in input file
                } else {
                    index = lastIndex;
                }
                g.setColor(Color.black);
                g.setFont(fnt);
                int x = (int) pf.getImageableX() + 10;
                int y = (int) pf.getImageableY() + 12;
                y += 36;
                while (y + 12 < pf.getImageableY() + pf.getImageableHeight()) {
                    if (index >= itemNo) {
                        rememberedEOF = true;
                        break;
                    }
                    gm = panel.get(index);
                    String line = "[" + gm.getSerial() + "]";
                    g.drawString(line, x, y);
                    g.drawString(gm.getLabel(), x + 30, y);
                    g.drawString(gm.toString(), x + 160, y);
                    y += 12;
                    index++;
                }
                return Printable.PAGE_EXISTS;
            } catch (Exception e) {
                return Printable.NO_SUCH_PAGE;
            }
        }
    }

    /**
     * <code>FileFilter</code> that is used by the <code>JFileChoose</code> to
     * show just panel set files on the filesystem
     */
    private static class MarkerPanelSetFileFilter extends javax.swing.filechooser.FileFilter {
        String fileExt;

        MarkerPanelSetFileFilter() {
            fileExt = ".mps";
        }

        public String getExtension() {
            return fileExt;
        }

        public String getDescription() {
            return "Marker Panel Files";
        }

        public boolean accept(File f) {
            boolean returnVal = false;
            if (f.isDirectory() || f.getName().endsWith(fileExt)) {
                return true;
            }
            return returnVal;
        }
    }

    /**
     * List Model backed by the marker item list.
     */
    private class MarkerListModel extends AbstractListModel {

        public int getSize() {
            if (markerList == null) {
                return 0;
            }
            return markerList.size();
        }

        public Object getElementAt(int index) {
            if (markerList == null) {
                return null;
            }
            return markerList.get(index);
        }

        public DSGeneMarker getMarker(int index) {
            return markerList.get(index);
        }

        /**
         * Indicates to the associated JList that the contents need to be redrawn.
         */
        public void refresh() {
            if (markerList == null) {
                fireContentsChanged(this, 0, 0);
            } else {
                fireContentsChanged(this, 0, markerList.size());
            }
        }

    }

    /**
     * Auto-scrolling list for markers that customizes the double-click and right-click behavior.
     */
    private class MarkerList extends org.geworkbench.util.JAutoList {

        public MarkerList(ListModel model) {
            super(model);
        }

        @Override protected void elementDoubleClicked(int index, MouseEvent e) {
            markerDoubleClicked(index, e);
        }

        @Override protected void elementRightClicked(int index, MouseEvent e) {
            markerRightClicked(index, e);
        }

        @Override protected void elementClicked(int index, MouseEvent e) {
            markerClicked(index, e);
        }

    }

    private class ListCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (!isSelected) {
                if (markerPanel.getSelection().contains(markerList.get(index))) {
                    component.setBackground(Color.YELLOW);
                }
            }
            return component;
        }
    }

    private class PanelCellRenderer extends DefaultTreeCellRenderer {

        private JCheckBox checkBox;
        private Color selectionBorderColor, selectionForeground, selectionBackground, textForeground, textBackground;

        public PanelCellRenderer() {
            checkBox = new JCheckBox();
            selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
            selectionForeground = UIManager.getColor("Tree.selectionForeground");
            selectionBackground = UIManager.getColor("Tree.selectionBackground");
            textForeground = UIManager.getColor("Tree.textForeground");
            textBackground = UIManager.getColor("Tree.textBackground");
            Font fontValue;
            fontValue = UIManager.getFont("Tree.font");
            if (fontValue != null) {
                checkBox.setFont(fontValue);
            }
            checkBoxWidth = checkBox.getPreferredSize().width;
        }

        @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (value instanceof DSPanel) {
                DSPanel<DSGeneMarker> panel = (DSPanel<DSGeneMarker>) value;
                if (panel != markerPanel) {
                    // Use custom renderer
                    String label = panel.getLabel() + " [" + panel.size() + "]";
                    checkBox.setText(label);
                    checkBox.setSelected(panel.isActive());
                    if (selected) {
                        checkBox.setForeground(selectionForeground);
                        checkBox.setBackground(selectionBackground);
                    } else {
                        checkBox.setForeground(textForeground);
                        checkBox.setBackground(textBackground);
                    }
                    return checkBox;
                } else {
                    return super.getTreeCellRendererComponent(tree, "", selected, expanded, leaf, row, hasFocus);
                }
            }
            return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        }

    }
    //// Members

    // Data models
    private DSItemList<DSGeneMarker> markerList;
    private DSPanel<DSGeneMarker> markerPanel;
    private MarkerListModel listModel;
    private PanelsEnabledTreeModel treeModel;
    private int checkBoxWidth;
    private CSPanel<DSGeneMarker> emptyPanel;
    private CSItemList<DSGeneMarker> emptyList;

    // Components
    private JPanel mainPanel;
    private org.geworkbench.util.JAutoList geneAutoList;
    private JTree panelTree;

    // Menu items
    private JPopupMenu geneListPopup = new JPopupMenu();
    private JMenuItem addToPanelItem = new JMenuItem("Add to Panel");
    private JMenuItem clearSelectionItem = new JMenuItem("Clear Selection");
    private JPopupMenu panelPopup = new JPopupMenu();
    private JMenuItem renamePanelItem = new JMenuItem("Rename");
    private JMenuItem activatePanelItem = new JMenuItem("Activate");
    private JMenuItem deactivatePanelItem = new JMenuItem("Deactivate");
    private JMenuItem deletePanelItem = new JMenuItem("Delete");
    private JMenuItem printPanelItem = new JMenuItem("Print");
    private JMenuItem savePanelItem = new JMenuItem("Save");
    private JMenuItem exportPanelItem = new JMenuItem("Export");
    private JMenuItem visualPropertiesItem = new JMenuItem("Visual Properties");
    private JPopupMenu rootPopup = new JPopupMenu();
    private JMenuItem loadPanelItem = new JMenuItem("Load Panel");
    private JPopupMenu markerPopup = new JPopupMenu();
    private JMenuItem removeFromPanelItem = new JMenuItem("Remove from Panel");

    // Context info for right-click events
    TreePath rightClickedPath = null;

    public GenePanel() {
        // Initialize data models
        emptyList = new CSItemList<DSGeneMarker>();
        emptyPanel = new CSPanel<DSGeneMarker>("");
        markerList = emptyList;
        markerPanel = emptyPanel;
        listModel = new MarkerListModel();
        treeModel = new PanelsEnabledTreeModel<DSGeneMarker>(markerPanel);
        // Initialize components
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        geneAutoList = new MarkerList(listModel);
        geneAutoList.getList().setCellRenderer(new ListCellRenderer());
        panelTree = new JTree(treeModel);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, geneAutoList, new JScrollPane(panelTree));
        splitPane.setDividerSize(3);
        splitPane.setResizeWeight(0.5);
        panelTree.setCellRenderer(new PanelCellRenderer());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        // Initialize popups
        geneListPopup.add(addToPanelItem);
        geneListPopup.add(clearSelectionItem);
        panelPopup.add(renamePanelItem);
        panelPopup.add(activatePanelItem);
        panelPopup.add(deactivatePanelItem);
        panelPopup.add(deletePanelItem);
        panelPopup.add(printPanelItem);
        panelPopup.add(savePanelItem);

        // Removing the "Export" popup item, until we decide what the export
        // functionlity is, if anything (since there is also a "Save" option.
        // panelPopup.add(exportPanelItem);

        panelPopup.add(visualPropertiesItem);
        rootPopup.add(loadPanelItem);
        markerPopup.add(removeFromPanelItem);
        // Add behaviors
        panelTree.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                panelTreeClicked(e);
            }
        });
        addToPanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addToPanelPressed();
            }
        });
        clearSelectionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearSelectionPressed();
            }
        });
        renamePanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renamePanelPressed(rightClickedPath);
            }
        });
        activatePanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activateOrDeactivatePanelPressed(true);
            }
        });
        deactivatePanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                activateOrDeactivatePanelPressed(false);
            }
        });
        deletePanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deletePanelPressed();
            }
        });
        printPanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                printPanelPressed(rightClickedPath);
            }
        });
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
        visualPropertiesItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                visualPropertiesPressed(rightClickedPath);
            }
        });
        loadPanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadButtonPressed();
            }
        });
        removeFromPanelItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeFromPanelPressed();
            }
        });
    }

    private DSPanel<DSGeneMarker> getPanelForPath(TreePath path) {
        Object obj = path.getLastPathComponent();
        if (obj instanceof DSPanel) {
            return (DSPanel<DSGeneMarker>) obj;
        } else {
            return null;
        }
    }

    private DSGeneMarker getGeneMarkerForPath(TreePath path) {
        Object obj = path.getLastPathComponent();
        if (obj instanceof DSGeneMarker) {
            return (DSGeneMarker) obj;
        } else {
            return null;
        }
    }

    private void ensurePathIsSelected(TreePath path) {
        if (path != null) {
            boolean alreadySelected = false;
            TreePath[] paths = panelTree.getSelectionPaths();
            if (paths != null) {
                for (int i = 0; i < paths.length; i++) {
                    if (paths[i].getLastPathComponent().equals(path.getLastPathComponent())) {
                        alreadySelected = true;
                        break;
                    }
                }
            }
            if (!alreadySelected) {
                panelTree.setSelectionPath(path);
            }
        }
    }

    private void ensureItemIsSelected(int index) {
        boolean alreadySelected = false;
        int[] indices = geneAutoList.getList().getSelectedIndices();
        if (indices != null) {
            for (int i = 0; i < indices.length; i++) {
                if (index == indices[i]) {
                    alreadySelected = true;
                    break;
                }
            }
        }
        if (!alreadySelected) {
            geneAutoList.getList().setSelectedIndex(index);
        }
    }

    private void removePanel(DSPanel<DSGeneMarker> panel) {
        int index = treeModel.getIndexOfChild(markerPanel, panel);
        markerPanel.panels().remove(panel);
        treeModel.firePanelRemoved(panel, index);
    }

    private void addPanel(DSPanel<DSGeneMarker> panel) {
        markerPanel.panels().add(panel);
        int index = treeModel.getIndexOfChild(markerPanel, panel);
        treeModel.firePanelAdded(panel, index);
    }

    private void panelTreeClicked(final MouseEvent e) {
        TreePath path = panelTree.getPathForLocation(e.getX(), e.getY());
        if (path != null) {
            DSPanel<DSGeneMarker> panel = getPanelForPath(path);
            DSGeneMarker marker = getGeneMarkerForPath(path);
            if ((e.isMetaDown()) && (e.getClickCount() == 1)) {
                rightClickedPath = path;
                ensurePathIsSelected(rightClickedPath);
                if (panel != null) {
                    if (panel != markerPanel) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                panelPopup.show(e.getComponent(), e.getX(), e.getY());
                            }
                        });
                    } else {
                        // Show root popup
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                rootPopup.show(e.getComponent(), e.getX(), e.getY());
                            }
                        });
                    }
                } else if (marker != null) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            markerPopup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    });
                }
            } else {
                if (panel != null) {
                    if (e.getX() < panelTree.getPathBounds(path).x + checkBoxWidth) {
                        panel.setActive(!panel.isActive());
                        treeModel.valueForPathChanged(path, panel);
                        throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
                    }
                } else if (marker != null) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        publishGeneSelectorEvent(new org.geworkbench.events.GeneSelectorEvent(marker));
                    }
                }
            }
        } else {
            panelTree.clearSelection();
        }
    }

    private void activateOrDeactivatePanelPressed(boolean value) {
        DSPanel<DSGeneMarker>[] panels = getSelectedPanelsFromTree();
        if (panels.length > 0) {
            for (int i = 0; i < panels.length; i++) {
                panels[i].setActive(value);
                // Notify model
                treeModel.firePanelChanged(panels[i]);
            }
            throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
        }
    }

    private void markerClicked(int index, MouseEvent e) {
        throwEvent(GeneSelectorEvent.MARKER_SELECTION);
    }

    private void markerDoubleClicked(int index, MouseEvent e) {
        // Get double-clicked marker
        DSGeneMarker marker = markerList.get(index);
        if (markerPanel.getSelection().contains(marker)) {
            markerPanel.getSelection().remove(marker);
        } else {
            markerPanel.getSelection().add(marker);
        }
        treeModel.firePanelChildrenChanged(markerPanel.getSelection());
        throwEvent(GeneSelectorEvent.PANEL_SELECTION);
    }

    private void markerRightClicked(int index, final MouseEvent e) {
        ensureItemIsSelected(index);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                geneListPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void saveButtonPressed(TreePath path) {
        DSPanel<DSGeneMarker> panel = getPanelForPath(path);
        if (panel != null) {
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
            throwEvent(GeneSelectorEvent.PANEL_SELECTION);
        }
    }

    private void removeFromPanelPressed() {
        TreePath[] paths = panelTree.getSelectionPaths();
        HashSet<DSPanel> affectedPanels = new HashSet<DSPanel>();
        for (int i = 0; i < paths.length; i++) {
            TreePath path = paths[i];
            Object obj = path.getLastPathComponent();
            if (obj instanceof DSGeneMarker) {
                DSGeneMarker marker = (DSGeneMarker) obj;
                // Path must have a panel as the second-to-last component
                DSPanel<DSGeneMarker> panel = (DSPanel<DSGeneMarker>) path.getParentPath().getLastPathComponent();
                panel.remove(marker);
                affectedPanels.add(panel);
            }
        }
        for (Iterator<DSPanel> iterator = affectedPanels.iterator(); iterator.hasNext();) {
            DSPanel<DSGeneMarker> panel = iterator.next();
            treeModel.firePanelChildrenChanged(panel);
        }
        throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
    }

    private void addToPanelPressed() {
        DSGeneMarker[] markers = getSelectedMarkersFromList();
        if (markers.length > 0) {
            // Is there already a selected panel?
            DSPanel<DSGeneMarker> defaultPanel = getSelectedPanelFromTree();
            String defaultLabel;
            if (defaultPanel == null) {
                defaultLabel = "";
            } else {
                defaultLabel = defaultPanel.getLabel();
            }
            String label = JOptionPane.showInputDialog("Panel Label:", defaultLabel);
            if (label == null) {
                return;
            } else {
                // todo: check for existing panel with this name
                for (int i = 0; i < markerPanel.panels().size(); i++) {
                    DSPanel<DSGeneMarker> panel = markerPanel.panels().get(i);
                    if (label.equals(panel.getLabel())) {
                        for (int j = 0; j < markers.length; j++) {
                            panel.add(markers[j]);
                        }
                        // Let the model know that this path changed
                        panelTree.scrollPathToVisible(new TreePath(new Object[]{markerPanel, panel}));
                        treeModel.firePanelChildrenChanged(panel);
                        throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
                        return;
                    }
                }
                // No panel found, so create a new one
                DSPanel<DSGeneMarker> panel = new CSPanel<DSGeneMarker>(label);
                for (int j = 0; j < markers.length; j++) {
                    panel.add(markers[j]);
                }
                addPanel(panel);
                panelTree.scrollPathToVisible(new TreePath(new Object[]{markerPanel, panel}));
                throwEvent(GeneSelectorEvent.PANEL_SELECTION);
            }
        }
    }

    private void clearSelectionPressed() {
        markerPanel.getSelection().clear();
        geneAutoList.getList().repaint();
        treeModel.firePanelChildrenChanged(markerPanel.getSelection());
        throwEvent(GeneSelectorEvent.PANEL_SELECTION);
    }

    /**
     * Only effects the right-clicked path, not the entire selection
     */
    private void renamePanelPressed(TreePath path) {
        DSPanel<DSGeneMarker> panel = getPanelForPath(path);
        if (panel != null) {
            String defaultLabel;
            defaultLabel = panel.getLabel();
            String label = JOptionPane.showInputDialog("New Label:", defaultLabel);
            if (label != null) {
                // todo: check for an existing panel with this name
                markerPanel.renameSubPanel(panel, label);
                treeModel.firePanelChanged(panel);
                throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
            }
        }
    }

    private void deletePanelPressed() {
        DSPanel<DSGeneMarker> panels[] = getSelectedPanelsFromTree();
        if (panels.length > 0) {
            int confirm = JOptionPane.showConfirmDialog(getComponent(), "Delete selected panel" + (panels.length > 1 ? "s" : "") + "?");
            if (confirm == JOptionPane.YES_OPTION) {
                panelTree.clearSelection();
                for (int i = 0; i < panels.length; i++) {
                    DSPanel<DSGeneMarker> panel = panels[i];
                    // Cannot delete root panel or selection panel
                    if ((panel != markerPanel) && (panel != markerPanel.getSelection())) {
                        removePanel(panel);
                    }
                }
                throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
            }
        }
    }

    private void printPanelPressed(TreePath path) {
        DSPanel<DSGeneMarker> panel = getPanelForPath(path);
        if (panel != null) {
            // Get a PrinterJob
            PrinterJob job = PrinterJob.getPrinterJob();
            // Ask user for page format (e.g., portrait/landscape)
            PageFormat pf = job.pageDialog(job.defaultPage());
            // Specify the Printable is an instance of
            // PrintListingPainter; also provide given PageFormat
            job.setPrintable(new PrintListingPainter(panel), pf);
            // Print 1 copy
            job.setCopies(1);
            // Put up the dialog box
            if (job.printDialog()) {
                // Print the job if the user didn't cancel printing
                try {
                    job.print();
                } catch (Exception pe) {
                    pe.printStackTrace();
                }
            }
        }
    }

    private void exportPanelPressed() {
        JOptionPane.showMessageDialog(getComponent(), "To be implemented...");
        // todo
    }

    /**
     * Only effects the right-clicked path, not the entire selection
     */
    private void visualPropertiesPressed(TreePath path) {
        DSPanel<DSGeneMarker> panel = getPanelForPath(path);
        // Cannot change visual properties for root
        if ((panel != null) && (panel != markerPanel)) {
            // Get current active index of panel for default visual properties
            int index = 0;
            if (panel.isActive()) {
                for (int i = 0; i < markerPanel.panels().size(); i++) {
                    DSPanel<DSGeneMarker> subPanel = markerPanel.panels().get(i);
                    if (subPanel.isActive()) {
                        index++;
                    }
                    if (subPanel.equals(panel)) {
                        // index--;
                        break;
                    }
                }
            }
            VisualPropertiesDialog dialog = new VisualPropertiesDialog(null, "Change Visual Properties", panel, index);
            dialog.pack();
            dialog.setSize(600, 600);
            dialog.setVisible(true);
            if (dialog.isPropertiesChanged()) {
                PanelVisualPropertiesManager manager = PanelVisualPropertiesManager.getInstance();
                PanelVisualProperties visualProperties = dialog.getVisualProperties();
                if (visualProperties == null) {
                    manager.clearVisualProperties(panel);
                } else {
                    manager.setVisualProperties(panel, visualProperties);
                }
                throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
            }
        }
    }

    /**
     * Convenience method to get all the selected markers in the gene list.
     */
    private DSGeneMarker[] getSelectedMarkersFromList() {
        int[] indices = geneAutoList.getList().getSelectedIndices();
        int n = indices.length;
        DSGeneMarker[] markers = new DSGeneMarker[n];
        for (int i = 0; i < n; i++) {
            markers[i] = markerList.get(indices[i]);
        }
        return markers;
    }

    /**
     * Convenience method to get all the selected panels in the panel tree.
     */
    private DSPanel<DSGeneMarker>[] getSelectedPanelsFromTree() {
        TreePath[] paths = panelTree.getSelectionPaths();
        int n = paths.length;
        ArrayList<DSPanel> list = new ArrayList<DSPanel>();
        for (int i = 0; i < n; i++) {
            TreePath path = paths[i];
            Object obj = path.getLastPathComponent();
            if (obj instanceof DSPanel) {
                list.add((DSPanel) obj);
            }
        }
        return list.toArray(new DSPanel[]{});
    }

    /**
     * Convenience method to get all the selected panels in the panel tree.
     */
    private DSGeneMarker[] getSelectedMarkersFromTree() {
        TreePath[] paths = panelTree.getSelectionPaths();
        int n = paths.length;
        ArrayList<DSGeneMarker> list = new ArrayList<DSGeneMarker>();
        for (int i = 0; i < n; i++) {
            TreePath path = paths[i];
            Object obj = path.getLastPathComponent();
            if (obj instanceof DSGeneMarker) {
                list.add((DSGeneMarker) obj);
            }
        }
        return list.toArray(new DSGeneMarker[]{});
    }

    /**
     * Gets the single panel selected from the tree if there is one
     */
    private DSPanel<DSGeneMarker> getSelectedPanelFromTree() {
        TreePath path = panelTree.getSelectionPath();
        if (path == null) {
            return null;
        } else {
            Object obj = path.getLastPathComponent();
            if (obj instanceof DSPanel) {
                return (DSPanel) obj;
            } else {
                return null;
            }
        }
    }

    private void dataSetCleared() {
        treeModel.setPanel(emptyPanel);
        markerList = emptyList;
        markerPanel = emptyPanel;
        geneAutoList.getList().repaint();
        treeModel.fireTreeStructureChanged();
        throwEvent(org.geworkbench.events.GeneSelectorEvent.PANEL_SELECTION);
    }

    private void dataSetChanged(DSDataSet dataSet) {
        if (dataSet instanceof DSMicroarraySet) {
            DSMicroarraySet maSet = (DSMicroarraySet) dataSet;
            markerList = maSet.getMarkers();
        } else if (dataSet instanceof SequenceDB) {
            markerList = (DSItemList)((SequenceDB)dataSet).getMarkerList();
        } else {
            markerList = dataSet;
        }
        markerPanel = CSMarkerManager.getMarkerPanel(dataSet);
        if (markerPanel == null) {
            markerPanel = new CSPanel<DSGeneMarker>("Marker Panel");
            markerPanel.getSelection().setActive(true);
            CSMarkerManager.setMarkerPanel(dataSet, markerPanel);
        }
        // Refresh list
        listModel.refresh();
        // Refresh tree
        treeModel.setPanel(markerPanel);
        treeModel.fireTreeStructureChanged();
        throwEvent(GeneSelectorEvent.PANEL_SELECTION);
    }

    private void throwEvent(int type) {
        org.geworkbench.events.GeneSelectorEvent event = null;
        switch (type) {
            case GeneSelectorEvent.PANEL_SELECTION:
                event = new GeneSelectorEvent(markerPanel);
                break;
            case org.geworkbench.events.GeneSelectorEvent.MARKER_SELECTION:
                int index = geneAutoList.getList().getSelectedIndex();
                if (index != -1) {
                    if (markerList != null) {
                        event = new GeneSelectorEvent(markerList.get(index));
                    }
                }
                break;
        }
        if (event != null) {
            publishGeneSelectorEvent(event);
        }
    }

    /**
     * Utility to save a panel to the filesystem.
     *
     * @param filename filename to which the current panel is to be saved.
     */
    private void serializePanel(String filename, DSPanel<DSGeneMarker> panel) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
            String line = null;
            if (panel != null && panel.size() > 0) {
                line = "Label\t" + panel.getLabel();
                writer.write(line);
                writer.newLine();
                line = "MinorLabel\t" + panel.getSubLabel();
                writer.write(line);
                writer.newLine();
                line = "MarkerType\t" + panel.get(0).getClass().getName();
                writer.write(line);
                writer.newLine();
                for (int i = 0; i < panel.size(); i++) {
                    DSGeneMarker marker = (DSGeneMarker) panel.get(i);
                    line = marker.getSerial() + "\t" + marker.getLabel() + "\t" + marker.getDescription();
                    writer.write(line);
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Utility to obtain the stored panel sets from the filesystem
     *
     * @param filename filename which contains the stored panel set
     */
    private DSPanel<DSGeneMarker> deserializePanel(final File file) {
        BufferedReader stream = null;
        try {
            stream = new BufferedReader(new InputStreamReader(new ProgressMonitorInputStream(getComponent(), "Loading probes " + file.getName(), new FileInputStream(file))));
            String line = null;
            DSPanel<DSGeneMarker> panel = new CSPanel<DSGeneMarker>();
            Class type = null;
            int serial = 0;
            while ((line = stream.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens != null && tokens.length == 2) {
                    if (tokens[0].trim().equalsIgnoreCase("Label")) {
                        panel.setLabel(new String(tokens[1].trim()));
                    } else if (tokens[0].trim().equalsIgnoreCase("MinorLabel")) {
                        panel.setSubLabel(new String(tokens[1].trim()));
                    } else if (tokens[0].trim().equalsIgnoreCase("MarkerType")) {
                        type = Class.forName(tokens[1].trim());
                    } else {
                    }
                }
                if (tokens != null && tokens.length == 3) {
                    if (type != null) {
                        DSGeneMarker marker = (DSGeneMarker) type.newInstance();
                        if (marker != null) {
                            marker.setSerial(Integer.parseInt(tokens[0].trim()));
                            marker.setLabel(new String(tokens[1].trim()));
                            marker.setDescription(new String(tokens[2].trim()));
                            panel.add(marker);
                        }
                    }
                }
                if (tokens.length == 1){
                    if (type == null){
                        type = Class.forName(System.getProperty("expression.marker.type"));
                        panel.setLabel(new String(file.getName().split("\\.")[0]));
                    }
                    DSGeneMarker marker = (DSGeneMarker) type.newInstance();
                    if (marker != null){
                        marker.setSerial(serial++);
                        marker.setLabel(new String(tokens[0].trim()));
                        panel.add(marker);
                    }
                }
            }
            return panel;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    public Component getComponent() {
        return mainPanel;
    }

    /**
     * Called when a data set is selected or cleared in the project panel.
     */
    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        if (projectEvent.getMessage().equals(org.geworkbench.events.ProjectEvent.CLEARED)) {
            dataSetCleared();
        }
        ProjectSelection selection = ((ProjectPanel) source).getSelection();
        DSDataSet dataSet = selection.getDataSet();
        if (selection.getSelectedNode() != selection.getSelectedProjectNode()) {
            processDataSet(dataSet);
        }

    }

    private void processDataSet(DSDataSet dataSet) {
        if (dataSet != null) {
            dataSetChanged(dataSet);
        } else {
            dataSetCleared();
        }
    }

    @Script public void setDataSet(DSDataSet dataSet) {
        processDataSet(dataSet);
    }

    @Script
    public void createPanel(int a, int b, boolean c) {
        // todo implement
    }
    /**
     * Called when a single marker is selected by a component.
     */
    @Subscribe public void receive(MarkerSelectedEvent event, Object source) {
        JList list = geneAutoList.getList();
        int index = markerList.indexOf(event.getMarker());
        list.setSelectedIndex(index);
        list.scrollRectToVisible(list.getCellBounds(index, index));
    }

    /**
     * Called when a component wishes to add, change or remove a panel.
     */
    @Subscribe(Overflow.class) public void receive(org.geworkbench.events.SubpanelChangedEvent spe, Object source) {
        DSPanel<DSGeneMarker> receivedPanel = spe.getPanel();
        switch (spe.getMode()) {
            case SubpanelChangedEvent.NEW: {
                String panelName = receivedPanel.getLabel();
                if (markerPanel.panels().contains(receivedPanel)) {
                    int number = 1;
                    String newName = panelName + " (" + number + ")";
                    receivedPanel.setLabel(newName);
                    while (markerPanel.panels().contains(receivedPanel)) {
                        number++;
                        newName = panelName + " (" + number + ")";
                        receivedPanel.setLabel(newName);
                    }
                }
                addPanel(receivedPanel);
                break;
            }
            case SubpanelChangedEvent.SET_CONTENTS:
                {
                    boolean foundPanel = false;
                    for (int i = 0; i < markerPanel.panels().size(); i++) {
                        DSPanel<DSGeneMarker> panel = markerPanel.panels().get(i);
                        if (panel.equals(receivedPanel)) {
                            foundPanel = true;
                            // Delete everything frpm the panel and re-add
                            panel.clear();
                            for (DSGeneMarker marker : receivedPanel) {
                                panel.add(marker);
                            }
                            synchronized (treeModel) {
                                treeModel.firePanelChildrenChanged(panel);
                            }
                            throwEvent(GeneSelectorEvent.PANEL_SELECTION);
                            break;
                        }
                    }
                    if (!foundPanel) {
                        // Add it as a new panel
                        addPanel(receivedPanel);
                    }
                    break;
                }
                // JWAT - exclude now phased out
//            case SubpanelChangedEvent.EXCLUDE:
//                {
//                    for (int i = 0; i < markerPanel.panels().size(); i++) {
//                        DSPanel<DSGeneMarker> panel = markerPanel.panels().get(i);
//                        if (panel.equals(receivedPanel)) {
//                            for (DSGeneMarker marker : receivedPanel) {
//                                panel.remove(marker);
//                            }
//                            synchronized (treeModel) {
//                                treeModel.firePanelChildrenChanged(panel);
//                            }
//                            throwEvent(GeneSelectorEvent.PANEL_SELECTION);
//                            break;
//                        }
//                    }
//                    break;
//                }
            case SubpanelChangedEvent.DELETE:
                {
                    for (int i = 0; i < markerPanel.panels().size(); i++) {
                        DSPanel<DSGeneMarker> panel = markerPanel.panels().get(i);
                        if (panel.equals(receivedPanel)) {
                            markerPanel.panels().remove(panel);
                            treeModel.fireTreeStructureChanged();
                            break;
                        }
                    }
                    break;
                }
            default:
                throw new RuntimeException("Unknown subpanel changed event mode: " + spe.getMode());
        }
    }

    @Publish public org.geworkbench.events.GeneSelectorEvent publishGeneSelectorEvent(GeneSelectorEvent event) {
        return event;
    }

    @Publish public SubpanelChangedEvent publishSubpanelChangedEvent(SubpanelChangedEvent event) {
        return event;
    }

}
