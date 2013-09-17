package org.geworkbench.components.promoter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;


class TFListPanel extends JPanel {
	private static final long serialVersionUID = 4166305079090877412L;
	
	public static final String SEARCH_LABEL_TEXT = "Search:";
    private JTextField filterField = null;
    JComboBox filterComboBox = null;
    
    JComboBox getFilterComboBox() {
		return filterComboBox;
	}

	String[] comboBoxSelections = null;
    JPanel topPanel = new JPanel();
    
    private PromoterViewPanel promoterViewPanel = null;
    private JList tfList = new JList();
    private TFListModel tfListModel = null;
    
    TFListPanel (PromoterViewPanel pvPanel){
	    super();	    	

	    filterField = new JTextField();
	    
	    promoterViewPanel = pvPanel;
	    tfListModel = promoterViewPanel.getTfListModel();
	    tfList = new JList(tfListModel);
	    
	    Vector<String> uniqueTaxGroupVector = promoterViewPanel.getUniqueTaxGroupVector();     
	    filterComboBox = new JComboBox(uniqueTaxGroupVector);
	    if (filterComboBox.getItemCount() >0){
        	filterComboBox.setSelectedIndex(0);
        }
 	    
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        setLayout(new BorderLayout());
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        JLabel searchLabel = new JLabel(SEARCH_LABEL_TEXT);
        
        JScrollPane scrollPane = new JScrollPane();
        topPanel.add(searchLabel);
        add(topPanel, BorderLayout.NORTH);
        scrollPane.getViewport().setView(tfList);
        add(scrollPane, BorderLayout.CENTER);

        tfList.addMouseListener(new MouseAdapter() {
            @Override 
            public void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
                tfListModel.refresh();
            }
        });
        
		KeyAdapter tfListKeyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.isActionKey()) {
					int index = tfList.getSelectedIndex();
					if (index != -1) {
						String tfName = (String) tfListModel.getElementAt(index);
						TranscriptionFactor pattern = promoterViewPanel
								.getTfMap().get(tfName);
						promoterViewPanel.setCurrentTF(pattern);
						try {
							promoterViewPanel.drawLogo(pattern);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
			}
		};
        
		tfList.addKeyListener(tfListKeyAdapter);
        
        KeyAdapter filterKeyAdapter = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String filter = filterField.getText();
				tfListModel.setFilterText(filter.toLowerCase().trim());
				tfListModel.refresh();
			}
		};
		filterField.addKeyListener(filterKeyAdapter);
		topPanel.add(filterField);
		
		ActionListener filterComboBoxActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				tfListModel.setTaxonomyFilter((String)filterComboBox.getSelectedItem());
				tfListModel.refresh();
			}
		};
		filterComboBox.addActionListener(filterComboBoxActionListener);
		topPanel.add(filterComboBox);		
    }
	
    JList getTFList() {
        return tfList;
    }

    boolean setHighlightedIndex(int theIndex) {
		if (tfListModel != null && tfListModel.getSize() > theIndex) {
			tfList.setSelectedIndex(theIndex);
			tfList.scrollRectToVisible(tfList.getCellBounds(theIndex,theIndex));
		}
		elementClicked(theIndex, null);
		return true;
	}
    
    private void elementDoubleClicked(int index, MouseEvent e) {
    	promoterViewPanel.addSelectedTF(index);
    	
    }

    private void elementClicked(int index, MouseEvent e) {

        String tfName = (String) tfListModel.getElementAt(index);
        TranscriptionFactor pattern = promoterViewPanel.getTfMap().get(tfName);
        promoterViewPanel.setCurrentTF(pattern);
        
        try {
        	promoterViewPanel.drawLogo(pattern);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void elementRightClicked(int index, MouseEvent e) {
    	promoterViewPanel.itemRightClicked(index, e);
    }
    
	
    private void handleMouseEvent(MouseEvent event) {
        int index = tfList.locationToIndex(event.getPoint());
        if (index != -1) {
            if (event.getButton() == MouseEvent.BUTTON3) {
                elementRightClicked(index, event);
            } else if (event.getButton() == MouseEvent.BUTTON1) {
                if (event.getClickCount() > 1) {
                    elementDoubleClicked(index, event);
                } else {
                    elementClicked(index, event);
                }
            }
        }
    }
}