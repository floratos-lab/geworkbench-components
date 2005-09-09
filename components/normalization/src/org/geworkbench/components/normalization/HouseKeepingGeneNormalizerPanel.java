package org.geworkbench.components.normalization;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;

import com.borland.jbcl.layout.XYConstraints;
import com.borland.jbcl.layout.XYLayout;
import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.*;
import java.awt.Component;
import java.awt.Color;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author Xiaoqing Zhang
 * @version 1.0
 */

/**
 * Parameters panel for the <code>HouseKeepingGeneNormalizer</code>..
 */

public class HouseKeepingGeneNormalizerPanel extends
        AbstractSaveableParameterPanel implements Serializable {

    JScrollPane jScrollPane1 = new JScrollPane();
    JScrollPane jScrollPane2 = new JScrollPane();
    JList jList1;
    JPanel jPanel1 = new JPanel();
    JButton jButton1 = new JButton();
    JButton jButton2 = new JButton();
    JPanel jPanel3 = new JPanel();
    XYLayout xYLayout2 = new XYLayout();
    JPanel jPanel4 = new JPanel();
    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JButton jButton5 = new JButton();
    BorderLayout borderLayout1 = new BorderLayout();
    // Data models
    DSItemList<DSGeneMarker> markerList;
    DSPanel<DSGeneMarker> panel;
    DSPanel<DSGeneMarker> markerPanel;
    DefaultListModel selectedModel = new DefaultListModel();
    DefaultListModel markerModel = new DefaultListModel();
    JPanel mainPanel = new JPanel();
    JButton loadButton = new JButton();
    JList jList2 = new JList(selectedModel);
    JButton jButton3 = new JButton();
    XYLayout xYLayout1 = new XYLayout();
    JPanel jPanel2 = new JPanel();
    JLabel jLabel3 = new JLabel();
    private JPopupMenu listPopup = new JPopupMenu();
    private JMenuItem removeItem = new JMenuItem("Delete");
    private JMenuItem editItem = new JMenuItem("Edit");
    private JMenuItem excludeItem = new JMenuItem("Move to Excluded List");

    private JMenuItem clearAllHighlightsItem = new JMenuItem(
            "Clear All Highlights");
    private JMenuItem removeAllHighlightsItem = new JMenuItem(
            "Delete All Highlighted");
    private JMenuItem moveAllHighlightsItem = new JMenuItem(
            "Move All Hightlighted to Excluded List");
    private Set<String> highlightedMarkers = new TreeSet<String>();
    final String AVG_OPTION = "Microarray Average";
    final String IGNORE_OPTION = "Ignore";
    static String lastDirString = ".";
    static final String USERSETTINGSFILE = "userSettings.txt";

    JComboBox jComboBox1 = new JComboBox(new String[] {IGNORE_OPTION,
                                         AVG_OPTION});

    public HouseKeepingGeneNormalizerPanel() {
       try {
           jbInit();
       } catch (Exception ex) {
           ex.printStackTrace();
       }

   }

    /**
     * saveButtonPressed save the markers in selected marker list to a file.
     *
     * @param path TreePath
     */
    public void saveButtonPressed(TreePath path) {

        if (panel != null) {
            JFileChooser fc = new JFileChooser(this.getLastDirectory());
            FileFilter filter = new MarkerPanelSetFileFilter();
            fc.setFileFilter(filter);
            fc.setDialogTitle("Save Housekeeping Genes Panel");
            String extension = ((MarkerPanelSetFileFilter) filter).getExtension();
            int choice = fc.showSaveDialog(null);
            if (choice == JFileChooser.APPROVE_OPTION) {
                String filename = fc.getSelectedFile().getAbsolutePath();
                if (!filename.endsWith(extension)) {
                    filename += extension;
                }
                boolean confirmed = true;
                if (new File(filename).exists()) {
                    int confirm = JOptionPane.showConfirmDialog(null,
                            "Replace existing file?");
                    if (confirm != JOptionPane.YES_OPTION) {
                        confirmed = false;
                    }
                }
                if (confirmed) {
                    saveToText(filename);
                }
            }
        }
    }

    /**
     * saveToText
     *
     * @param filename String
     */
    public void saveToText(String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
                    filename)));
            String line = null;
            if (selectedModel.size() == 0) {
                reportError("No gene is selected", null);
                return;
            }

            for (int i = 0; i < selectedModel.size(); i++) {
                line = (String) selectedModel.getElementAt(i);
                writer.write(line);
                writer.newLine();
            }

            writer.flush();
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }


    /**
     * loadButtonPressed to load markers from a csv format file.
     */
    public void loadButtonPressed() {
        JFileChooser fc = new JFileChooser(this.getLastDirectory());
        javax.swing.filechooser.FileFilter filter = new
                MarkerPanelSetFileFilter();
        fc.setFileFilter(filter);
        fc.setDialogTitle("Load new housekeeping Genes");
        int choice = fc.showOpenDialog(mainPanel.getParent());
        if (choice == JFileChooser.APPROVE_OPTION) {

            try {
                String filename = fc.getSelectedFile().getAbsolutePath();
                String filepath = fc.getCurrentDirectory().getCanonicalPath();
                setLastDirectory(filepath);

                InputStream input2 = new FileInputStream(filename);
                populateList(input2);
                updateLabel();

            } catch (Exception ex) {
                ex.printStackTrace();
                reportError(ex.toString(), null);
            }

        }

    }

    /**
     * Add a pop menu for mouse right click.
     * @param index int
     * @param e MouseEvent
     */
    private void markerRightClicked(int index, final MouseEvent e) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                listPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }


    /**
     * <code>FileFilter</code> that is used by the <code>JFileChoose</code> to
     * show just panel set files on the filesystem
     */
    private class MarkerPanelSetFileFilter extends javax.swing.
            filechooser.FileFilter {
        String fileExt;

        MarkerPanelSetFileFilter() {
            fileExt = ".csv";
        }

        public String getExtension() {
            return fileExt;
        }

        public String getDescription() {
            return "csv";
        }

        public boolean accept(File f) {
            boolean returnVal = false;
            if (f.isDirectory() || f.getName().endsWith(fileExt)) {
                return true;
            }
            return returnVal;
        }
    }


//    /**
//     * List Model backed by the marker item list.
//     */
//    private class MarkerListModel extends AbstractListModel {
//
//        public int getSize() {
//            if (markerList == null) {
//                return 0;
//            }
//            return markerList.size();
//        }
//
//        public Object getElementAt(int index) {
//            if (markerList == null) {
//                return null;
//            }
//            return markerList.get(index);
//        }
//
//        public DSGeneMarker getMarker(int index) {
//            return markerList.get(index);
//        }
//
//        /**
//         * Indicates to the associated JList that the contents need to be redrawn.
//         */
//        public void refresh() {
//            if (markerList == null) {
//                fireContentsChanged(this, 0, 0);
//            } else {
//                fireContentsChanged(this, 0, markerList.size());
//            }
//        }
//
//    }


    /**
     * reportError, Error report wrapper.
     *
     * @param message String
     * @param title String
     */
    private void reportError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title,
                                      JOptionPane.ERROR_MESSAGE);
    }


    /**
     * populateList, populate the list from an inputStream. The markers are
     * added into selected_marker list.
     *
     * @param input InputStream
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void populateList(InputStream input) throws FileNotFoundException,
            IOException {

        BufferedReader br = null;

        br = new BufferedReader(new InputStreamReader(input));

        HashMap factors = new HashMap();
        String line = null;

        while ((line = br.readLine()) != null) {

            String[] cols = line.split(",");

            for (String s : cols) {
                if (!selectedModel.contains(s)) {
                    selectedModel.addElement(s.trim());
                }
            }

            markerList.add(new CSGeneMarker(cols[0]));
        }
        br.close();
    }

    /**
     * marker_mouseClicked, add the double clicked marker to the
     * selected_marker list.
     *
     * @param e MouseEvent
     */

    private void markerList_mouseClicked(MouseEvent e) {
        int index = jList1.locationToIndex(e.getPoint());
        if (e.getClickCount() == 2) {
            String value = (String) markerModel.getElementAt(index);
            addMarkers(value);
        }
    }

    /**
     * List_mouseClicked, move the double clicked marker from the
     * selected_marker list.
     *
     * @param e MouseEvent
     */
    private void List_mouseClicked(MouseEvent e) {
        int index = jList2.locationToIndex(e.getPoint());

        if (index > -1 && e.getClickCount() == 2) {
            String value = (String) selectedModel.getElementAt(index);
            moveMarkers(value);
        }
        updateLabel();
    }

    /**
     * moveMarkers based on the label.
     *
     * @param value String
     */
    private void moveMarkers(String value) {
        if (!markerModel.contains(value)) {
            markerModel.addElement(value);
        }
        if (highlightedMarkers != null) {
            highlightedMarkers.remove(value);
        }

        selectedModel.removeElement(value);
        updateLabel();

    }

    /**
     * Add one marker into the selected list.
     * @param markerName String
     */
    private void addMarkers(String markerName) {
        if (!selectedModel.contains(markerName)) {
            selectedModel.addElement(markerName);
        }

        markerModel.removeElement(markerName);
        updateLabel();
    }

    /**
     * Remove marker from selected list.
     * @param markerName String
     */
    private void removeMarkers(String markerName) {
        if (highlightedMarkers != null) {
            highlightedMarkers.remove(markerName);
        }
        selectedModel.removeElement(markerName);
        updateLabel();

    }


    void jbInit() throws Exception {
        this.setLayout(xYLayout1);
        markerList = new CSItemList<DSGeneMarker>();
        panel = new CSPanel<DSGeneMarker>();
        jButton1.setText(">");
        jButton1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });
        jButton2.setText("<");
        jButton2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
            }
        });
        jPanel3.setLayout(xYLayout2);
        jLabel1.setText("    Current Selected Genes");
        jLabel2.setText("Excluded HouseKeeping Genes");
        jButton5.setText("Clear all");
        jButton5.addActionListener(new
                                   HouseKeepingGeneNormalizerPanel_jButton5_actionAdapter(this));
        jPanel4.setLayout(borderLayout1);
        loadButton.setToolTipText("Load housekeeping genes from a file.");
        loadButton.setText("Load");
        loadButton.addActionListener(new
                                     HouseKeepingGeneNormalizerPanel_loadButton_actionAdapter(this));
        jButton3.setToolTipText("Save the housekeeping genes into a file.");
        jButton3.setText("Save");
        jButton3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton3_actionPerformed(e);
            }
        });
        jLabel3.setText("Missing Values");
        jComboBox1.setToolTipText("");
        jPanel1.add(jButton1);
        jPanel1.add(jButton2);
        jPanel1.add(jButton5);
        jPanel1.add(loadButton);
        jPanel1.add(jButton3);
        //jList2.setModel(selectedModel);
        jList1 = new JList();
        jList1.setToolTipText("HouseKeeping genes list");
        markerModel = new DefaultListModel();
        jList1 = new JList(markerModel); //(DefaultListModel) jList1.getListModel();

        jList1.addMouseListener(new java.awt.event.
                                MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                markerList_mouseClicked(e);
            }

        });

        jList2.addMouseListener(new MouseAdapter() {
            @Override public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
            }
        });
        jList2.setCellRenderer(new ListCellRenderer());

        listPopup.add(editItem);
        listPopup.add(removeItem);
        listPopup.add(excludeItem);
        listPopup.addSeparator();
        listPopup.add(moveAllHighlightsItem);
        listPopup.add(removeAllHighlightsItem);
        listPopup.add(clearAllHighlightsItem);

        editItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editItemPressed();
            }
        });
        removeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeItemPressed();
            }
        });
        excludeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                excludeItemPressed();
            }
        });

        clearAllHighlightsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllHightlightsPressed();
            }
        });
        removeAllHighlightsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearAllHightlightsItemPressed();
            }
        });
        moveAllHighlightsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveAllHightlightsItemPressed();
            }
        });

        jScrollPane2.getViewport().add(jList2);
        jScrollPane1.getViewport().add(jList1);

        jPanel4.add(jLabel2, java.awt.BorderLayout.WEST);
        jPanel4.add(jLabel1, java.awt.BorderLayout.CENTER);
        jPanel2.add(jLabel3);
        jPanel2.add(jComboBox1);
        jPanel3.add(jPanel1, new XYConstraints(90, 3, 71, 137));
        jPanel3.add(jScrollPane2, new XYConstraints(165, 3, 91, 137));
        jPanel3.add(jScrollPane1, new XYConstraints(2, 3, 86, 137));
        this.add(jPanel4, new XYConstraints(0, 0, 279, 27));
        this.add(jPanel2, new XYConstraints(0, 168, 279, 21));
        this.add(jPanel3, new XYConstraints(0, 28, 279, -1));
        InputStream input = HouseKeepingGeneNormalizer.class.
                            getResourceAsStream(
                                    "DEFAULT_HOUSEKEEPING_GENES.txt");

        populateList(input);
        updateLabel();
    }

    /**
     * editItemPressed
     */
    private void editItemPressed() {
        String oldLabel = (String) jList2.getSelectedValue();
        int selectedIndex = jList2.getSelectedIndex();

        if (oldLabel != null) {

            String label = JOptionPane.showInputDialog("New Label:", oldLabel);
            if (label != null) {
                selectedModel.set(selectedIndex, label);
            }
        }

    }

    /**
     * removeItemPressed
     */
    private void removeItemPressed() {
        String oldLabel = (String) jList2.getSelectedValue();
        if (oldLabel != null) {

            removeMarkers(oldLabel);

        }

    }

    /**
     * excludeItemPressed
     */
    private void excludeItemPressed() {
        String oldLabel = (String) jList2.getSelectedValue();
        if (oldLabel != null) {

            removeMarkers(oldLabel);

        }

    }

    private void clearAllHightlightsItemPressed() {
        if (highlightedMarkers != null) {
            for (Object ob : highlightedMarkers) {
                selectedModel.removeElement(ob);
            }
        }
        highlightedMarkers.clear();
    }

    private void clearAllHightlightsPressed() {
        if (highlightedMarkers != null) {
            highlightedMarkers.clear();
            repaint();
        }

    }

    private void moveAllHightlightsItemPressed() {
        if (highlightedMarkers != null) {
            for (Object ob : highlightedMarkers) {
                removeMarkers((String) ob);
            }
        }
        highlightedMarkers.clear();

    }


    /**
     * Handle mouse event.
     * @param event MouseEvent
     */
    private void handleMouseEvent(MouseEvent event) {
        int index = jList2.locationToIndex(event.getPoint());
        if (index != -1) {
            if (event.getButton() == MouseEvent.BUTTON3) {
                markerRightClicked(index, event);
            } else if (event.getButton() == MouseEvent.BUTTON1) {
                if (event.getClickCount() > 1) {
                    List_mouseClicked(event);
                } else {
                    //elementClicked(index, event);
                }
            }
        }
    }


    public void jButton3_actionPerformed(ActionEvent e) {
        saveButtonPressed(null);
    }

    public DSPanel getMarkerPanel() {
        return markerPanel;
    }

    public DSPanel getPanel() {
        updatePanel();

        return panel;
    }

    /**
     * updateLabel, display the number of Genes listed in each list box.
     */
    public void updateLabel() {
        jLabel1.setText("Current Selected Genes [" + selectedModel.size() + "]");
        jLabel2.setText("Excluded Genes [" + markerModel.size() + "]    ");
    }

    /**
     * updatePanel, set up the selected marker list.
     */
    public void updatePanel() {
        panel = new CSPanel<DSGeneMarker>();
        for (Enumeration en = selectedModel.elements(); en.hasMoreElements(); ) {

            CSGeneMarker csg = new CSGeneMarker((String) en.nextElement());
            panel.add(csg);
        }
    }

    public void setMarkerPanel(DSPanel markerPanel) {
        this.markerPanel = markerPanel;
    }

    public void setPanel(DSPanel panel) {
        this.panel = panel;
    }

    public void loadButton_actionPerformed(ActionEvent e) {
        loadButtonPressed();
    }

    public void jButton5_actionPerformed(ActionEvent e) {
        selectedModel.clear();
        markerModel.clear();
    }

    public void jButton1_actionPerformed(ActionEvent e) {
        Object[] selectedGenes = jList1.getSelectedValues();
        if (selectedGenes != null) {
            for (Object selected : selectedGenes) {
                markerModel.removeElement(selected);
                selectedModel.addElement(selected);
            }
        } else {
            reportError("No gene is selected", "No selection");
        }
    }

    public void jButton2_actionPerformed(ActionEvent e) {
        Object[] selectedGenes = jList2.getSelectedValues();
        if (selectedGenes != null) {
            for (Object selected : selectedGenes) {
                selectedModel.removeElement(selected);
                markerModel.addElement(selected);
            }
        } else {
            reportError("No gene is selected", "No selection");
        }

    }

    static public String getLastDirectory() {
        String dir = ".";
        try {
            File file = new File(USERSETTINGSFILE);
            if (file.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(file));

                dir = br.readLine();
                br.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (dir == null) {
            dir = ".";
        }
        return dir;
    }


    static public void setLastDirectory(String dir) {
        try { //save current settings.
            BufferedWriter br = new BufferedWriter(new FileWriter(
                    USERSETTINGSFILE));
            br.write(dir);
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        revalidate();
    }

    /**
     * highlightMarkers
     *
     * @param nonFoundGenes ArrayList
     */
    public void setHighlightedMarkers(Set<String> nonFoundGenes) {
        highlightedMarkers = nonFoundGenes;
    }

    private class ListCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

                if (highlightedMarkers.contains(selectedModel.get(index))) {
                    if (!isSelected) {
                    component.setBackground(Color.YELLOW);
                }else{
                    component.setBackground(Color.ORANGE);
                }
            }
            return component;
        }
    }

}


class HouseKeepingGeneNormalizerPanel_jButton5_actionAdapter implements
        ActionListener {
    private HouseKeepingGeneNormalizerPanel adaptee;
    HouseKeepingGeneNormalizerPanel_jButton5_actionAdapter(
            HouseKeepingGeneNormalizerPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.jButton5_actionPerformed(e);
    }
}


class HouseKeepingGeneNormalizerPanel_loadButton_actionAdapter implements
        ActionListener {
    private HouseKeepingGeneNormalizerPanel adaptee;
    HouseKeepingGeneNormalizerPanel_loadButton_actionAdapter(
            HouseKeepingGeneNormalizerPanel adaptee) {
        this.adaptee = adaptee;
    }

    public void actionPerformed(ActionEvent e) {
        adaptee.loadButton_actionPerformed(e);
    }
}
