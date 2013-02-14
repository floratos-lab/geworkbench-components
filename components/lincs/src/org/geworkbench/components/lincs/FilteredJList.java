package org.geworkbench.components.lincs;
 
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import javax.swing.JList; 
import javax.swing.JTextField; 
import javax.swing.ListModel;
import javax.swing.AbstractListModel;

import java.util.ArrayList;
import java.util.List;

public class FilteredJList extends JList {
	private static final long serialVersionUID = -5781097715334727519L;
	private FilterField filterField;
	 
	public FilteredJList() {
		super();
		setModel(new FilterModel());
		filterField = new FilterField();
		filterField.setText("search");
		final Font f = filterField.getFont();
		filterField.addMouseListener(new MouseAdapter(){
	            @Override
	            public void mouseClicked(MouseEvent e){
	            	if (filterField.getText().equals("search"))
	            		filterField.setText("");
	            	filterField.setFont(f);
	            }
	        });
		filterField.setPreferredSize(new Dimension(50, 25));
		filterField.setFont(new Font("Courier", Font.ITALIC, 12));

	}

	public void setModel(ListModel m) {
		if (!(m instanceof FilterModel))
			throw new IllegalArgumentException();
		super.setModel(m);
	}

	public void addItem(String o) {
		((FilterModel) getModel()).addElement(o);
	}

	public JTextField getFilterField() {
		return filterField;
	}

	class FilterModel extends AbstractListModel {

		private static final long serialVersionUID = 718002238381710120L;
		List<String> items;
		List<String> filterItems;

		public FilterModel() {
			super();
			items = new ArrayList<String>();
			filterItems = new ArrayList<String>();
		}
		
		public FilterModel(List<String> items) {
			super();
			this.items = items;
			filterItems = new ArrayList<String>();
		}

		public Object getElementAt(int index) {
			if (index < filterItems.size())
				return filterItems.get(index);
			else
				return null;
		}

		public int getSize() {
			return filterItems.size();
		}

		public void addElement(String o) {
			items.add(o);
			refilter();

		}

		private void refilter() {
			filterItems.clear();
			String term = getFilterField().getText().toLowerCase();
			if (term.equals("search"))
				filterItems.addAll(items);
			else {
				for (int i = 0; i < items.size(); i++)
					if (items.get(i).toString().toLowerCase().indexOf(term, 0) != -1)
						filterItems.add(items.get(i));
			}
			fireContentsChanged(this, 0, getSize());
		}
	}

	// FilterField inner class listed below

	// inner class provides filter-by-keystroke field
	class FilterField extends JTextField implements DocumentListener {

		private static final long serialVersionUID = -4467945272051575635L;

		public FilterField() {
			super();
			getDocument().addDocumentListener(this);
		}

		public void changedUpdate(DocumentEvent e) {
			((FilterModel) getModel()).refilter();
		}

		public void insertUpdate(DocumentEvent e) {
			((FilterModel) getModel()).refilter();
		}

		public void removeUpdate(DocumentEvent e) {
			((FilterModel) getModel()).refilter();
		}
	}

}
