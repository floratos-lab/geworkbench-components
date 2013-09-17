package org.geworkbench.components.genspace.ui.notebook;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;


public class NoteListModel extends AbstractTableModel implements TableModel{

	private static final long serialVersionUID = 4162759562607434407L;
	
	private List <AnalysisEvent>myList;

	

	public void setMyList(List<AnalysisEvent> myList) {
		this.myList = myList;
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return AnalysisEvent.class;
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		return "Analysis";
	}

	@Override
	public int getRowCount() {
		if(myList == null)
			return 0;
		return myList.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return myList.get(rowIndex);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}


}
