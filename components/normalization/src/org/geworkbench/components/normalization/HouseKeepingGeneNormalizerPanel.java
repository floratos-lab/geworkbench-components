package org.geworkbench.components.normalization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.events.listeners.ParameterActionListener;
import org.geworkbench.util.FilePathnameUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
 * @version $Id$
 */

/**
 * Parameters panel for the <code>HouseKeepingGeneNormalizer</code>..
 */
//AbstractSaveableParameterPanel
public class HouseKeepingGeneNormalizerPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = 8632072605758980427L;
	
	private JScrollPane jScrollPane1 = new JScrollPane();
    private JScrollPane jScrollPane2 = new JScrollPane();

    private JPanel jPanel1 = new JPanel();
    private JButton addButton = new JButton();
    private JButton removeButton = new JButton();
    private JPanel jPanel3 = new JPanel();
    private JPanel jPanel4 = new JPanel();
    private JLabel selectedGenesLabel = new JLabel();
    private JLabel excludedGenesLabel = new JLabel();
    private JButton clearButton = new JButton();

    private DSPanel<DSGeneMarker> panel;
    private DefaultListModel selectedModel = new DefaultListModel();
    private DefaultListModel markerModel = new DefaultListModel();
    private JPanel mainPanel = new JPanel();
    private JButton loadButton = new JButton();
    private JList jList2 = new JList(selectedModel);
    private JList jList1 = new JList(markerModel); //(DefaultListModel) jList1.getListModel();

    private JButton saveButton = new JButton();

    private JLabel missingValuesLabel = new JLabel();
    private JPopupMenu listPopup = new JPopupMenu();
    private JMenuItem removeItem = new JMenuItem("Delete");
    private JMenuItem editItem = new JMenuItem("Rename");
    private JMenuItem excludeItem = new JMenuItem("Move to Excluded List");

    private JMenuItem clearHightlights = new JMenuItem(
            "Clear Highlights");
    private JMenuItem removeAllHighlightsItem = new JMenuItem(
            "Delete Highlighted");
    private JMenuItem moveAllHighlightsItem = new JMenuItem(
            "Exclude Hightlighted");
    private TreeSet<String> highlightedMarkers = new TreeSet<String>();
    
    private final String AVG_OPTION = "Microarray Average";
    private final String IGNORE_OPTION = "Ignore";

    private int currentHighlightedIndex = -1;
    private boolean isMissingValueIgnored = true;
    private JComboBox missingValuesCombo = new JComboBox(new String[]{IGNORE_OPTION,
            AVG_OPTION});
    private JPanel jPanel5 = new JPanel();
    private JButton moveToAboveButton = new JButton();
    private JButton moveNextButton = new JButton();
    private JPanel jPanel6 = new JPanel();

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
     */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		int m = markerModel.size();
		ArrayList<String> exclude = new ArrayList<String>(m);
		for (int i = 0; i < m; i++) {
			exclude.add((String)markerModel.get(i));
		}
		int n = selectedModel.size();
		ArrayList<String> select = new ArrayList<String>(n);
		for (int i = 0; i < n; i++) {
			select.add((String)selectedModel.get(i));
		}
		 
		parameters.put("excluded", exclude);
		parameters.put("selected", select);
		parameters.put("missingvalues", missingValuesCombo.getSelectedIndex());
		return parameters;
	}

    /*
     * (non-Javadoc)
     * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
     */
    public void setParameters(Map<Serializable, Serializable> parameters){
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();
			if (key.equals("excluded")){
				this.markerModel.clear();
				ArrayList<?> exclude = (ArrayList<?>)value;
	            for (int i = 0; i < exclude.size(); i++) {
	                this.markerModel.add(i, exclude.get(i));
	            }
			}
			if (key.equals("selected")){
				this.selectedModel.clear();
				ArrayList<?>  select = (ArrayList<?>)value;
	            for (int i = 0; i < select.size(); i++) {
	                this.selectedModel.add(i, select.get(i));
	            }
			}
			if (key.equals("missingvalues")){
				this.missingValuesCombo.setSelectedIndex((Integer)value);
			}
		}
    }

    public HouseKeepingGeneNormalizerPanel() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public boolean isMissingValueIgnored() {
        isMissingValueIgnored = missingValuesCombo.getSelectedItem().toString().equalsIgnoreCase(IGNORE_OPTION);
        return isMissingValueIgnored;
    }

    public void setMissingValueIgnored(boolean missingValueIgnored) {
        isMissingValueIgnored = missingValueIgnored;
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
    private void loadButtonPressed() {
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
     *
     * @param index int
     * @param e     MouseEvent
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


    /**
     * reportError, Error report wrapper.
     *
     * @param message String
     * @param title   String
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

        String line = null;
        Set<String> treeSet = new TreeSet<String>();

        while ((line = br.readLine()) != null) {
            String[] cols = line.split(",");
            for (String s : cols) {
                treeSet.add(s);
            }
        }
        br.close();
        for (String s : treeSet) {
            if (!selectedModel.contains(s)) {
                selectedModel.addElement(s.trim());
            }
        }

    }

    /**
     * marker_mouseClicked, add the double clicked marker to the
     * selected_marker list.
     *
     * @param e MouseEvent
     */

    private void markerList_mouseClicked(MouseEvent e) {
        int index = jList1.locationToIndex(e.getPoint());
        if (index > -1 && e.getClickCount() == 2) {
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
            moveMarker(value);
        }
        updateLabel();
    }

    /**
     * moveMarkers based on the label.
     *
     * @param value String
     */
    private void moveMarker(String value) {
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
     *
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
     *
     * @param markerName String
     */
    private void removeMarker(String markerName) {
        if (highlightedMarkers != null) {
            highlightedMarkers.remove(markerName);
        }
        selectedModel.removeElement(markerName);
        updateLabel();
    }


    void jbInit() throws Exception {
        //        BoxLayout boxLayout2 = new BoxLayout(this, BoxLayout.Y_AXIS);
        // this.setLayout(xYLayout1);
        this.setLayout(new BorderLayout());
        panel = new CSPanel<DSGeneMarker>();
        addButton.setText(">");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton1_actionPerformed(e);
            }
        });
        removeButton.setText("<");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton2_actionPerformed(e);
            }
        });


        BoxLayout boxLayout21 = new BoxLayout(jPanel3, BoxLayout.X_AXIS);
        jPanel3.setLayout(boxLayout21);
        selectedGenesLabel.setText("Current Selected Genes");
        excludedGenesLabel.setText("Excluded HouseKeeping Genes");
        clearButton.setText("Clear All");
        clearButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						clearAllActionPerformed();
					}
        	
        });
        jPanel4.setLayout(new BorderLayout());
        loadButton.setToolTipText("Load housekeeping genes from a file.");
        loadButton.setText("Load");
        loadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadButtonPressed();
			}
        	
        });
        saveButton.setToolTipText("Save the housekeeping genes into a file.");
        saveButton.setText("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jButton3_actionPerformed(e);
            }
        });
        missingValuesLabel.setText("Missing Values");
        moveToAboveButton.setToolTipText("Previous highlighted marker");
        moveToAboveButton.setText("^");
        moveToAboveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveToAboveButton_actionPerformed(e);
            }
        });

        moveNextButton.setToolTipText("Next highlighted marker");
        moveNextButton.setText("v");
        moveNextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                moveNextButton_actionPerformed(e);
            }
        });
        jPanel6.setPreferredSize(new Dimension(10, 70));
        BoxLayout bl = new BoxLayout(jPanel1, BoxLayout.PAGE_AXIS);
        jPanel1.setLayout(bl);
        jPanel1.setMaximumSize(new Dimension(400, 200));
        jPanel1.setMinimumSize(new Dimension(100, 128));
        jPanel1.setPreferredSize(new Dimension(100, 128));

        jScrollPane1.setMinimumSize(new Dimension(100, 100));
        jScrollPane1.setPreferredSize(new Dimension(100, 100));
        jScrollPane2.setMinimumSize(new Dimension(100, 100));
        jScrollPane2.setPreferredSize(new Dimension(100, 100));
        jPanel5.setMinimumSize(new Dimension(60, 100));
        jPanel5.setPreferredSize(new Dimension(60, 100));
        jPanel1.add(Box.createRigidArea(new Dimension(0, 5)));
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        jPanel1.add(addButton);
        jPanel1.add(removeButton);
        jPanel1.add(clearButton);
        jPanel1.add(loadButton);
        jPanel1.add(saveButton);
        jList1.setToolTipText("HouseKeeping genes list");

        jList1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                markerList_mouseClicked(e);
            }

        });

        jList2.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
            }
        });

        /*
		 * In this case, it use JList, which we'll not monitor it directly,
		 * we'll moniter the model instead.
		 */
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        selectedModel.addListDataListener(parameterActionListener);
        markerModel.addListDataListener(parameterActionListener);
        missingValuesCombo.addActionListener(parameterActionListener);

        jList2.setCellRenderer(new ListCellRenderer());
        jList1.setCellRenderer(new DefaultListCellRenderer());
        listPopup.add(editItem);
        listPopup.add(removeItem);
        listPopup.addSeparator();
        listPopup.add(moveAllHighlightsItem);
        listPopup.add(removeAllHighlightsItem);
        listPopup.add(clearHightlights);
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

        clearHightlights.addActionListener(new ActionListener() {
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

        FormLayout layout = new FormLayout("150dlu,5dlu,60dlu,5dlu,150dlu", "p,8dlu,p,p,p,p,p,p,8dlu,p,8dlu,p");
        layout.setColumnGroups(new int[][]{{1, 5}});
        PanelBuilder builder = new PanelBuilder(layout);

        builder.addSeparator("HouseKeeping Parameters");

        int row = 3;

        CellConstraints cc = new CellConstraints();
        builder.add(excludedGenesLabel, cc.xy(1, row));
        builder.add(selectedGenesLabel, cc.xy(5, row));
        row++;
        builder.add(jScrollPane1, cc.xywh(1, row, 1, 5));
        builder.add(jScrollPane2, cc.xywh(5, row, 1, 5));

        // Add buttons
        builder.add(addButton, cc.xy(3, row++));
        builder.add(removeButton, cc.xy(3, row++));
        builder.add(clearButton, cc.xy(3, row++));
        builder.add(loadButton, cc.xy(3, row++));
        builder.add(saveButton, cc.xy(3, row++));

        // Add missing values controls
        row++;
        builder.addSeparator("Missing Values Options for Housekeeping Genes", cc.xyw(1, row++, 5));
        row++;
        builder.add(missingValuesLabel, cc.xy(3, row));
        builder.add(missingValuesCombo, cc.xy(5, row));


        jScrollPane2.getViewport().add(jList2);
        jScrollPane1.getViewport().add(jList1);

        this.add(builder.getPanel(), BorderLayout.CENTER);

        updateLabel();
    }

    /**
     * editItemPressed
     */
    private void editItemPressed() {
        String oldLabel = (String) jList2.getSelectedValue();
        int selectedIndex = jList2.getSelectedIndex();

        if (oldLabel != null) {
            String label = JOptionPane.showInputDialog("New Marker Name:",
                    oldLabel);
            if (label != null) {
                selectedModel.set(selectedIndex, label);
            }
        }

    }

    /**
     * removeItemPressed
     */
    private void removeItemPressed() {
        Object[] oldLabels = jList2.getSelectedValues();
        for (Object oldLabel : oldLabels) {
            if (oldLabel != null) {
                removeMarker((String) oldLabel);
            }
        }
    }

    /**
     * excludeItemPressed
     */
    private void excludeItemPressed() {
        Object[] oldLabels = jList2.getSelectedValues();
        for (Object oldLabel : oldLabels) {
            if (oldLabel != null) {
                moveMarker((String) oldLabel);
            }
        }
    }

    private void clearAllHightlightsItemPressed() {
        if (highlightedMarkers != null) {
            for (Object ob : highlightedMarkers) {
                selectedModel.removeElement(ob);
            }
        }
        highlightedMarkers.clear();
        updateLabel();
    }

 public  void clearAllHightlightsPressed() {
        if (highlightedMarkers != null) {
            highlightedMarkers.clear();
            updateLabel();

        }

    }

    private void moveAllHightlightsItemPressed() {
        if (highlightedMarkers != null) {
            for (Object ob : highlightedMarkers) {
                selectedModel.removeElement(ob);
                markerModel.addElement(ob);
            }
        }
        highlightedMarkers.clear();

        updateLabel();

    }


    /**
     * Handle mouse event.
     *
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
                }
            }
        }
    }


    public void jButton3_actionPerformed(ActionEvent e) {
        saveButtonPressed(null);
    }

    public String getParamDetail() {
       StringBuffer paramDetail = new StringBuffer("Excluded Genes:\n");
       for (Enumeration<?> en = markerModel.elements(); en.hasMoreElements();) {

           paramDetail .append(  en.nextElement().toString() ).append( "\n" );
       }
       paramDetail .append( "Included Genes:\n" );

       for (Enumeration<?> en = selectedModel.elements(); en.hasMoreElements();) {

    	   paramDetail .append(  en.nextElement().toString() ).append( "\n" );
       }

       paramDetail .append( "How to handle missing value: " ).append( missingValuesCombo.getSelectedItem().toString() ).append( "\n" );

       return paramDetail.toString() ;
    }


    public DSPanel<DSGeneMarker> getPanel() {
        updatePanel();

        return panel;
    }

    /**
     * updateLabel, display the number of Genes listed in each list box.
     */
    public void updateLabel() {
        selectedGenesLabel.setText("Current Selected Genes [" + selectedModel.size() + "]");
        excludedGenesLabel.setText("Excluded Genes [" + markerModel.size() + "]    ");
        if (highlightedMarkers.isEmpty()) {
            jPanel5.setVisible(false);
            removeAllHighlightsItem.setEnabled(false);
            clearHightlights.setEnabled(false);
            moveAllHighlightsItem.setEnabled(false);

        } else {
            removeAllHighlightsItem.setEnabled(true);
            clearHightlights.setEnabled(true);
            moveAllHighlightsItem.setEnabled(true);

        }
    }

    /**
     * updatePanel, set up the selected marker list.
     */
    public void updatePanel() {
        panel = new CSPanel<DSGeneMarker>();
        for (Enumeration<?> en = selectedModel.elements(); en.hasMoreElements();) {
            CSGeneMarker csg = new CSGeneMarker((String) en.nextElement());
            panel.add(csg);
        }
    }

    private void clearAllActionPerformed() {
        moveAllHightlightsItemPressed();
        selectedModel.clear();
        markerModel.clear();
        updateLabel();
    }

    public void jButton1_actionPerformed(ActionEvent e) {
        Object[] selectedGenes = jList1.getSelectedValues();
        if (selectedGenes != null) {
            for (Object selected : selectedGenes) {
                markerModel.removeElement(selected);
                selectedModel.addElement(selected);
            }
            updateLabel();
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
            updateLabel();
        } else {
            reportError("No gene is selected", "No selection");
        }

    }

    public String getLastDirectory() {
        String dir = ".";
        try {
            String filename = FilePathnameUtils.getHousekeepingnormalizerSettingsPath();

            File file = new File(filename);
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


    public void setLastDirectory(String dir) {
        try { //save current settings.
            String outputfile = FilePathnameUtils.getHousekeepingnormalizerSettingsPath();
            BufferedWriter br = new BufferedWriter(new FileWriter(
                    outputfile));
            br.write(dir);
            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * highlightMarkers
     *
     * @param nonFoundGenes ArrayList
     */
    public void setHighlightedMarkers(TreeSet<String> nonFoundGenes) {
        if (nonFoundGenes != null) {
            highlightedMarkers = nonFoundGenes;
            updateLabel();

        }
        if (nonFoundGenes.size() > 1) {

            addnewMovePanel();
        } else {
            jPanel5.setVisible(false);
            repaint();
        }

        if (highlightedMarkers.size() > 1) {
            addnewMovePanel();
        } else {
            //this.getContentPane().remove(jPanel5);

        }
    }

    /**
     * Add two buttons for browsing highlighted markers.
     * addnewMovePanel
     */
    private void addnewMovePanel() {
        //jPanel3.add(jPanel5, new XYConstraints(313, 7, 41, 132));
        jPanel5.setVisible(true);
        String currentMarker = (String) highlightedMarkers.first();
        jList2.setSelectedValue(currentMarker, true);
        currentHighlightedIndex = selectedModel.indexOf(currentMarker);
        revalidate();
        repaint();
    }

    public void moveToAboveButton_actionPerformed(ActionEvent e) {
        for (int i = currentHighlightedIndex - 1; i >= 0; i--) {
            if (highlightedMarkers.contains(selectedModel.elementAt(i))) {
                jList2.setSelectedValue(selectedModel.elementAt(i), true);
                currentHighlightedIndex = i;
                break;
            }
        }

    }

    public void moveNextButton_actionPerformed(ActionEvent e) {
        for (int i = currentHighlightedIndex + 1; i < selectedModel.size(); i++) {
            if (highlightedMarkers.contains(selectedModel.elementAt(i))) {
                jList2.setSelectedValue(selectedModel.elementAt(i), true);
                currentHighlightedIndex = i;
                this.repaint();
                break;
            }
        }
    }


    private class ListCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = -3440814217180031900L;

		@Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            Component component = super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);

            if (highlightedMarkers.contains(selectedModel.get(index))) {
                if (!isSelected) {
                    component.setBackground(Color.YELLOW);
                } else {
                    component.setBackground(Color.ORANGE);
                }
            }
            return component;
        }
    }


	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}
}
