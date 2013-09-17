package org.geworkbench.components.filtering;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

/**
 * PreviewSearchTable
 * 
 * @author zm2165
 * @version $Id$
 */

public class PreviewSearchTable extends JPanel {

	private static final long serialVersionUID = 395551495657406162L;

	public static final String SEARCH_LABEL_TEXT = "Search:";
	//private static final String MARKERTABLE_DELITMETER= "\t";
    private JList list;   
    private JTextField searchField;
    private ListModel model;
    private JScrollPane scrollPane;
    private JPanel topPanel;
    private JTable markers;
    private DefaultTableModel markerTableModel;

    private boolean lastSearchFailed = false;
    private boolean lastSearchWasAscending = true;

    private boolean prefixMode = false;
    
    public PreviewSearchTable(ListModel model, DefaultTableModel markerTableModel) {
        super();
        this.model = model;
        this.markerTableModel=markerTableModel;
        // Create and lay out components
        setLayout(new BorderLayout());
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        JLabel searchLabel = new JLabel(SEARCH_LABEL_TEXT);        
        searchField = new JTextField();
        list = new JList(model);
        markers= new JTable(markerTableModel);
        markers.setAutoCreateRowSorter(true);
        scrollPane = new JScrollPane(markers);
        scrollPane.setPreferredSize(new Dimension(250, 180));	
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);
		        
        topPanel.add(searchLabel);
        topPanel.add(searchField);        
        add(topPanel, BorderLayout.NORTH);        
        add(scrollPane, BorderLayout.CENTER);
        // Add appropriate listeners
        
         searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                searchFieldChanged();
            }

            public void removeUpdate(DocumentEvent e) {
                searchFieldChanged();
            }

            public void changedUpdate(DocumentEvent e) {
                searchFieldChanged();
            }
        });
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
            	PreviewSearchTable.this.keyPressed(e);
            }
        });
    }

    /**
     * Finds the previous match for the text string and selects it in the list.
     *
     * @param text  the text to match.
     * @param index the index from which to begin the search.
     * @return true if a match was found, false otherwise.
     */
    private boolean findPreviousFrom(String text, int index) {
        if (text.trim().length() > 0) {
            text = text.toLowerCase();
            boolean found = false;
            for (int i = index; i >= 0; i--) {
                if (match(i, text)) {
                    list.setSelectedIndex(i);
                    list.scrollRectToVisible(list.getCellBounds(i, i));
                    int row = markers.convertRowIndexToView(i);
					Rectangle r = markers.getCellRect(row, 0, false);  
					markers.scrollRectToVisible(r);
					markers.setRowSelectionInterval(row, row);
                    found = true;
                    break;
                }
            }
            return found;
        }
        // Degenerate case
        return true;
    }

    private boolean match(int index, String text) {
        String element = model.getElementAt(index).toString().toLowerCase();
        if (!prefixMode) {
            return element.contains(text);
        } else {
            return element.startsWith(text);
        }
    }

    /**
     * Finds the next match for the text string and selects it in the list.
     *
     * @param text  the text to match.
     * @param index the index from which to begin the search.
     * @return true if a match was found, false otherwise.
     */
    private boolean findNextFrom(String text, int index) {
        if (text.trim().length() > 0) {
            text = text.toLowerCase();
            boolean found = false;
            for (int i = index; i < model.getSize(); i++) {
                if (match(i, text)) {
                    list.setSelectedIndex(i);
                    list.scrollRectToVisible(list.getCellBounds(i, i));
                    int row = markers.convertRowIndexToView(i);
					Rectangle r = markers.getCellRect(row, 0, false);  
					markers.scrollRectToVisible(r);
					markers.setRowSelectionInterval(row, row);
                    found = true;
                    break;
                }
            }
            return found;
        }
        // Degenerate case
        return true;
    }
    
    /**
     * Override to customize the result of the 'next' button being clicked (or ENTER being pressed in text field).
     */
    private boolean findNext(boolean ascending) {       
        int index = list.getSelectedIndex();
        if (lastSearchFailed) {
            if (ascending && lastSearchWasAscending) {
                index = -1;
            } else if (!ascending && !lastSearchWasAscending) {
                index = model.getSize();
            }
        }
        lastSearchWasAscending = ascending;
        String text = searchField.getText();
        if (ascending) {
            if (index < (model.getSize() - 1)) {
                index++;
                lastSearchFailed = !findNextFrom(text, index);
            } else {
                lastSearchFailed = true;
            }
        } else {
            if (index > 0) {
                index--;
                lastSearchFailed = !findPreviousFrom(text, index);
            } else {
                lastSearchFailed = true;
            }
        }       
        return!lastSearchFailed;
    }

    /**
     * Override to customize the result of a key being typed in the search field.
     *
     * @param event the key event.
     */
    private void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            // Same effect as next button
            findNext(true);
        } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { // ESC key
            searchField.setText("");
        } else if (event.getKeyCode() == KeyEvent.VK_DOWN) {
            int index = list.getSelectedIndex();
            index++;            
            if (index < markerTableModel.getRowCount()) {
                list.setSelectedIndex(index);
                list.scrollRectToVisible(list.getCellBounds(index, index));
                int row = markers.convertRowIndexToView(index);
				Rectangle r = markers.getCellRect(row, 0, false);  
				markers.scrollRectToVisible(r);
				markers.setRowSelectionInterval(row, row);
            }
        } else if (event.getKeyCode() == KeyEvent.VK_UP) {
            int index = list.getSelectedIndex();
            index--;
            if (index >= 0) {
                list.setSelectedIndex(index);
                list.scrollRectToVisible(list.getCellBounds(index, index));
                int row = markers.convertRowIndexToView(index);
				Rectangle r = markers.getCellRect(row, 0, false);  
				markers.scrollRectToVisible(r);
				markers.setRowSelectionInterval(row, row);
            }
        } else if (event.isControlDown()) {
            if (event.getKeyChar() == '\u000E') {
                findNext(true);
            } else if (event.getKeyChar() == '\u0002') {
                findNext(false);
            }
        }
    }

    private void searchFieldChanged() {
    	
    	markerTableModel.setRowCount(0);
		
		String searchText = searchField.getText().toLowerCase();		

		for (int i = 0; i < model.getSize(); i++){
			if (model.getElementAt(i).toString().toLowerCase().indexOf(searchText, 0) != -1){
				String s=model.getElementAt(i).toString();
				String[] tokens=s.split(PreviewDialog.MARKERTABLE_DELITMETER);
				markerTableModel.addRow(tokens);				
			}
		}
		markerTableModel.newDataAvailable(null);
       
    }

    public JList getList() {
        return list;
    }

  
}
