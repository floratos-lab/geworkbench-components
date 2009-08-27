package org.geworkbench.components.filtering;


import java.awt.Component;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia.</p>
 * @author Xiaoqing Zhang
 * @version 1.0
 */

/**
 * The parameters panel for the <code>GenepixFlagsFilter</code>
 * filter. The measures will be removed based on their flags.
 */
public class GenepixFlagsFilterPanel extends AbstractSaveableParameterPanel {
    /**
     * Inner class to represent FlagDetail.
     */
    private static class FlagDetail implements Serializable {
        String label;
        String number;
        boolean isFiltered;
        String description;
        HashMap knownDescriptions = new HashMap() {};

        FlagDetail(String theLabel, String theNumber) {
            label = theLabel;
            number = theNumber;
            isFiltered = false;
        }

        FlagDetail(String theLabel) {
            label = theLabel;
            isFiltered = false;
            number = "0";
        }

        public String getLabel() {
            return label;
        }

        public String getNumber() {
            return number;
        }

        public void setIsFiltered(boolean filtered) {
            isFiltered = filtered;
        }
    }


    String nonApplicableReminder =
            "No data is loaded.";
    String nonAppReminder =
            "The dataset is not ammenable to filtering by the Genepix Flags filter: ";
    String noFlagDetectedReminder =
            "The Genepix file has no flags.";
    int unflaggedProbeNum = 0;
    Map flaggedProbeNum;
    final Hashtable<String,
            String> flagExplanationTable = new Hashtable<String, String>();
    static {

    }

    private static final String NOT_FILTERED = "not filtered";
	private static final String FILTERED = "filtered";

    JScrollPane flagInfoPane = new JScrollPane();
    private ArrayList<FlagDetail> hits = new ArrayList<FlagDetail>();

    private JLabel infoLabel;
    private JLabel noFlagLabel;
    JPanel container = new JPanel();
    BoxLayout boxlayout;
    JTable table1;

    private static GenepixFlagsFilterPanel heldPanel;

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    public void setParameters(Map<Serializable, Serializable> parameters){
    	if(parameters==null){
    		return;
    	}
        Set<Map.Entry<Serializable, Serializable>> set = parameters.entrySet();
        for (Iterator<Map.Entry<Serializable, Serializable>> iterator = set.iterator(); iterator.hasNext();) {
        	Map.Entry<Serializable, Serializable> parameter = iterator.next();
			Object key = parameter.getKey();
			Object value = parameter.getValue();

			if (key.equals("SelectedFlags")){
				GenepixFlagsFilterPanel panel = heldPanel;
	            ArrayList selectedFlags = (ArrayList)value;
	            for (Object fd : panel.hits) {
	                FlagDetail detail = (FlagDetail) fd;
	                if (selectedFlags.contains(detail.getLabel())) {
	                    detail.setIsFiltered(true);
	                } else {
	                    detail.setIsFiltered(false);
	                }
	            }
			}
		}
        revalidate();
        repaint();
    }

    /*
	 * (non-Javadoc)
	 *
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#getParameters()
	 */
    public Map<Serializable, Serializable> getParameters() {
		Map<Serializable, Serializable> parameters = new HashMap<Serializable, Serializable>();
		parameters.put("SelectedFlags", getSelectedFlags());
		return parameters;
	}


    public GenepixFlagsFilterPanel() {
        try {
            jbInit();
            heldPanel = this;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void jbInit() throws Exception {
        flagExplanationTable.put("-100", "Bad");
        flagExplanationTable.put("100", "Good");
        flagExplanationTable.put("-50", "Not Found");
        flagExplanationTable.put("-75", "Absent");
        flagExplanationTable.put("0", "Unflagged");
       boxlayout = new BoxLayout(container, BoxLayout.Y_AXIS);
        container.setLayout(boxlayout);
        infoLabel = new JLabel(nonApplicableReminder);
        container.add(infoLabel);
        container.add(Box.createVerticalGlue());
        container.setPreferredSize(new Dimension(380, 150));
        this.add(container);

    }

    /**
     * setFlagInfoPanel
     */
    public void setFlagInfoPanel() {
         container.removeAll();
        container.add(new JLabel(nonApplicableReminder));
        //container.add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }

    /**
     * setFlagInfoPanel
     *
     * @param aString String
     */
    public void setFlagInfoPanel(String aString) {
         container.removeAll();

        container.add(new JLabel(nonAppReminder));
        if(aString!=null){
            container.add(new JLabel(aString));
        }
        //container.add(Box.createVerticalGlue());
        revalidate();
        repaint();
    }


    /**
     * Returns a JScrollpane containing Flag results in table format.
     *
     * @return a JScrollpane containing table of Flags.
     */
    public void setFlagInfoPanel(Set values) {
        hits.clear();

        for (Object ob : values) {
            FlagDetail newFlag = new FlagDetail((String) ob);
            hits.add(newFlag);
        }
        container.removeAll();

        FlagsTableModel myModel = new FlagsTableModel();
        JTable table1 = new JTable(myModel);
        table1.setPreferredScrollableViewportSize(new Dimension(200, 90));
        table1.getColumnModel().getColumn(0).setPreferredWidth(15);
        table1.getColumnModel().getColumn(1).setPreferredWidth(30);
        table1.getColumnModel().getColumn(2).setPreferredWidth(30);
        table1.getColumnModel().getColumn(3).setPreferredWidth(30);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = table1.getSelectionModel();
        rowSM.addListSelectionListener(new listSelectionListener());
        table1.setSelectionModel(rowSM);

        JScrollPane hitsPane = new JScrollPane(table1);
        container.add(
                new JLabel("Please select flags to filter:"));
        container.add(hitsPane);
        container.add(Box.createVerticalGlue());
        revalidate();
        repaint();

    }

    /**
     * getSelectedFlags
     */
    public ArrayList getSelectedFlags() {
        ArrayList selectedFlags = new ArrayList();
        for (Object fd : hits) {
            if (((FlagDetail) fd).isFiltered) {
                selectedFlags.add(((FlagDetail) fd).getLabel());
            }
        }
        return selectedFlags;
    }


    /**
     * For the FlagsTable.
     */
    private class listSelectionListener implements ListSelectionListener {
        int selectedRow;

        public void valueChanged(ListSelectionEvent e) {
            //Ignore extra messages.
            if (e.getValueIsAdjusting()) {
                return;
            }
            ListSelectionModel lsm = (ListSelectionModel) e.getSource();
            if (lsm.isSelectionEmpty()) {
            } else {
                selectedRow = lsm.getMinSelectionIndex();

            }
        }
    }


    /**
     * This class extends AbstractTableModel and creates a table view of Flags.
     */
    private class FlagsTableModel extends AbstractTableModel {

        /* array of the column names in order from left to right*/
        final String[] columnNames = {"Filter", "Flags Name", "Description",
                                     "# of probes",
        };
        FlagDetail hit;

        /* returns the number of columns in table*/
        public int getColumnCount() {
            return columnNames.length;
        }

        /* returns the number of rows in table*/
        public int getRowCount() {
            return (hits.size());
        }

        /* return the header for the column number */
        public String getColumnName(int col) {
            return columnNames[col];
        }

        /* get the Object data to be displayed at (row, col) in table*/
        public Object getValueAt(int row, int col) {
            /*get specific BlastObj based on row number*/
            hit = (FlagDetail) hits.get(row);
            /*display data depending on which column is chosen*/
            if (hit != null) {
                switch (col) {
                case 0:
                    return hit.isFiltered;
                case 1:
                    return hit.getLabel(); //accesion number
                case 2:
                    if (flagExplanationTable.get(hit.getLabel()) != null) {
                        return flagExplanationTable.get(hit.getLabel());
                    }
                    return "N/A";
                case 3:
                    if (flaggedProbeNum!=null && flaggedProbeNum.get(hit.getLabel()) != null) {
                        return flaggedProbeNum.get(hit.getLabel());
                    }
                    return "1"; //description
                }

            }

            return null;
        }

        /*returns the Class type of the column c*/
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*returns if the cell is editable; returns false for all cells in columns except column 6*/
        public boolean isCellEditable(int row, int col) {

            if (col == 0) {
                return true;
            } else {
                return false;
            }
        }

        /*detect change in cell at (row, col); set cell to value; update the table */
        public void setValueAt(Object value, int row, int col) {
            hit = (FlagDetail) hits.get(row);
            hit.isFiltered = (((Boolean) value).booleanValue());
            fireTableCellUpdated(row, col);
        }

    }


    public Component getComponent() {
        return this;
    };

    public void setFlagInfoPanel(JScrollPane FlagInfoPanel) {
        this.flagInfoPane = FlagInfoPanel;

        revalidate();
    }

    public JLabel getInfoLabel() {
        return infoLabel;
    }

    public void setInfoLabel(String infoLabelString) {
        this.infoLabel = new JLabel(infoLabelString);
        revalidate();
    }

    /**
     * setflaggedProbeNum
     *
     * @param flagsProbeNum Map
     */
    public void setflaggedProbeNum(Map flagsProbeNum) {
        this.flaggedProbeNum = flagsProbeNum;
    }

    /**
     * setUnflaggedProbeNum
     *
     * @param unflaggedProbeNum int
     */
    public void setUnflaggedProbeNum(int unflaggedProbeNum) {
        this.unflaggedProbeNum = unflaggedProbeNum;
    }

	@Override
	public void fillDefaultValues(Map<Serializable, Serializable> parameters) {
		// TODO Auto-generated method stub

	}
	@Override
	public String getDataSetHistory() {
		String histStr = "";

		// Header
		histStr += "GenepixFlagsFilter parameters:\n";
		histStr += "----------------------------------------\n";

		/*   flags*/
        for (Object fd : heldPanel.hits) {
            FlagDetail detail = (FlagDetail) fd;
    		histStr += detail.getLabel() + " (" + flagExplanationTable.get(detail.getLabel()).toString() + ") : ";
    		histStr += detail.isFiltered? FILTERED : NOT_FILTERED;
    		histStr += "\n";

        }

		return histStr;
	}


}
