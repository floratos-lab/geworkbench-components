package org.geworkbench.components.filtering;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.geworkbench.analysis.AbstractSaveableParameterPanel;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.events.listeners.ParameterActionListener;
 

/**
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Columbia.</p>
 * @author Xiaoqing Zhang
 * @version $Id$
 */

/**
 * The parameters panel for the <code>GenepixFlagsFilter</code>
 * filter. The measures will be removed based on their flags.
 */
public class GenepixFlagsFilterPanel extends AbstractSaveableParameterPanel {
	private static final long serialVersionUID = -8138872216028249538L;

	/**
     * Inner class to represent FlagDetail.
     */
    private static class FlagDetail implements Serializable {
		private static final long serialVersionUID = 6505401364167035460L;
		
		private String label;
		private boolean isFiltered;

        FlagDetail(String theLabel) {
            label = theLabel;
            isFiltered = false;
        }

        public String getLabel() {
            return label;
        }

        public void setIsFiltered(boolean filtered) {
            isFiltered = filtered;
        }
    }


    String nonApplicableReminder =
            "No data is loaded.";
    String nonAppReminder =
            "Cannot filter dataset using the Genepix Flags filter: ";
    String noFlagDetectedReminder =
            "The Genepix file has no flags.";
    int unflaggedProbeNum = 0;
    Map<String, Integer> flaggedProbeNum;
    final Hashtable<String,
            String> flagExplanationTable = new Hashtable<String, String>();
    static {

    }

    private static final String NOT_FILTERED = "not filtered";
	private static final String FILTERED = "filtered";

    JScrollPane flagInfoPane = new JScrollPane();
    private ArrayList<FlagDetail> hits = new ArrayList<FlagDetail>();

    private JLabel infoLabel;
    JPanel container = new JPanel();
    BoxLayout boxlayout;
    JTable table1;

    private static GenepixFlagsFilterPanel heldPanel;

	/*
	 * (non-Javadoc)
	 * @see org.geworkbench.analysis.AbstractSaveableParameterPanel#setParameters(java.util.Map)
	 * Set inputed parameters to GUI.
	 */
    @SuppressWarnings("unchecked")
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
	            List<String> selectedFlags = (ArrayList<String>)value;
	            for (Object fd : panel.hits) {
	                FlagDetail detail = (FlagDetail) fd;
	                if (selectedFlags.contains(detail.getLabel())) {
	                    detail.setIsFiltered(true);
	                } else {
	                    detail.setIsFiltered(false);
	                }
	            }
			} else if (key.equals("numberThreshold")){
	            this.filterOptionPanel.numberField.setText(value.toString());	           
	            this.filterOptionPanel.numberRemovalButton.setSelected(true);
	            this.revalidate();
			} else if (key.equals("percentThreshold")){
	            this.filterOptionPanel.percentField.setText( value.toString());	            
	            this.filterOptionPanel.percentRemovalButton.setSelected(true);
	            this.revalidate();
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
		if (this.filterOptionPanel.numberRemovalButton.isSelected())
			parameters.put("numberThreshold", (Integer) this.filterOptionPanel.getNumberThreshold());
		else
			parameters.put("percentThreshold", (Double) this.filterOptionPanel.getPercentThreshold()*100);
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
   

    private FilterOptionPanel filterOptionPanel = new FilterOptionPanel();

    private void jbInit() throws Exception {
        flagExplanationTable.put("-100", "Bad");
        flagExplanationTable.put("100", "Good");
        flagExplanationTable.put("-50", "Not Found");
        flagExplanationTable.put("-75", "Absent");
        flagExplanationTable.put("0", "Unflagged");
        flagExplanationTable.put("-99", "Flagged bad by user");
        flagExplanationTable.put("99", "Flagged good by user");
        
        boxlayout = new BoxLayout(container, BoxLayout.Y_AXIS);
        container.setLayout(boxlayout);
        infoLabel = new JLabel(nonApplicableReminder);
        container.add(infoLabel);
        container.add(Box.createVerticalGlue());
        container.setPreferredSize(new Dimension(380, 150));

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(filterOptionPanel);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(container);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setMaximumSize(new Dimension(380, 150));

        JPanel wrapperPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(wrapperPanel, BoxLayout.PAGE_AXIS);
        wrapperPanel.setLayout(boxLayout);
        wrapperPanel.add(topPanel);
        wrapperPanel.add(bottomPanel);
        this.add(wrapperPanel);

        ParameterActionListener parameterActionListener = new ParameterActionListener(this);
        filterOptionPanel.numberField.addActionListener(parameterActionListener);
        filterOptionPanel.percentField.addActionListener(parameterActionListener);
        filterOptionPanel.numberRemovalButton.addActionListener(parameterActionListener);
        filterOptionPanel.percentRemovalButton.addActionListener(parameterActionListener);
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
    public void setFlagInfoPanel(Set<String> values) {
        hits.clear();

        for (String str : values) {
            FlagDetail newFlag = new FlagDetail(str);
            hits.add(newFlag);
        }
        container.removeAll();

        FlagsTableModel myModel = new FlagsTableModel();
        JTable table1 = new JTable(myModel);
        table1.setPreferredScrollableViewportSize(new Dimension(200, 90));
        table1.getColumnModel().getColumn(0).setPreferredWidth(15);
        table1.getColumnModel().getColumn(1).setPreferredWidth(30);
        table1.getColumnModel().getColumn(2).setPreferredWidth(30);
        table1.getColumnModel().getColumn(3).setPreferredWidth(35);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ListSelectionModel rowSM = table1.getSelectionModel();
        table1.setSelectionModel(rowSM);
        
        ParameterActionListener parameterActionListener = new ParameterActionListener(this);        
        table1.addMouseListener(parameterActionListener);
       

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
    // return type should be changed to List
    public ArrayList<String> getSelectedFlags() {
        ArrayList<String> selectedFlags = new ArrayList<String>();
        for (Object fd : hits) {
            if (((FlagDetail) fd).isFiltered) {
                selectedFlags.add(((FlagDetail) fd).getLabel());
            }
        }
        return selectedFlags;
    }

    /**
     * This class extends AbstractTableModel and creates a table view of Flags.
     */
    private class FlagsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 8266464868252493663L;
		
		/* array of the column names in order from left to right*/
        final String[] columnNames = {"Filter", "Flags Name", "Description",
                                     "# of occurrences",
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
        public Class<?> getColumnClass(int c) {
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
    public void setflaggedProbeNum(Map<String, Integer> flagsProbeNum) {
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
		Map<Serializable, Serializable>parameters = null;
		parameters = getParameters();		
		String histStr = "";

		// Header
		histStr += "GenepixFlagsFilter parameters:\n";		
		if(this.filterOptionPanel.numberRemovalButton.isSelected()){
			histStr += "number threshold of missing arrays: ";
			histStr += parameters.get("numberThreshold");			
		}else{
			histStr += "percentage threshold of missing arrays: ";
			histStr += parameters.get("percentThreshold");			
		}		
		histStr += "\n";
		/*   flags*/
        for (Object fd : heldPanel.hits) {
            FlagDetail detail = (FlagDetail) fd;
    		String flagExplanation = "unknown";
    		if (flagExplanationTable.get(detail.getLabel()) != null )
    			flagExplanation = flagExplanationTable.get(detail.getLabel()).toString();
            histStr += detail.getLabel() + " (" + flagExplanation + ") : ";
    		histStr += detail.isFiltered? FILTERED : NOT_FILTERED;
    		histStr += "\n";

        }

		return histStr;
	}
	
	@Override
    public ParamValidationResults validateParameters() {
    	 String error = filterOptionPanel.validateParameters();
    	 if ( error == null)
            return new ParamValidationResults(true, "No Error");
    	 else   	  	
    		return new ParamValidationResults(false, error);
    	 
    }
	

	FilterOptionPanel getFilterOptionPanel() {
		return filterOptionPanel;
	}

}
