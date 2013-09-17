package org.geworkbench.components.genspace.ui.notebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.XMLGregorianCalendar;

import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.AnalysisEvent;
import org.geworkbench.components.genspace.server.stubs.AnalysisEventParameter;


public class NotebookCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

	private static final long serialVersionUID = -8030893125935988646L;
	
	JPanel secondPanel;
	JPanel panel;
	JPanel buttonPanel;
	JLabel noteInfo;
	JLabel dataSetInfo;
	JTextArea noteText;
	SimpleDateFormat format = new SimpleDateFormat("M/d/yy h:mm a");
	JScrollPane scroll;
	AnalysisEvent currentEvent;
	static int lines = 0;
	private JTable parent;
	
	public NotebookCellEditor(JTable owner)
	{
		this.parent = owner;
		panel = new JPanel();
		secondPanel = new JPanel();
		buttonPanel = new JPanel();
		noteInfo = new JLabel();
		noteInfo.setFont(NotebookCellRenderer.headerFont);
		dataSetInfo = new JLabel();
		dataSetInfo.setFont(NotebookCellRenderer.dataSetFont);
		secondPanel.setLayout(new BoxLayout(secondPanel,BoxLayout.Y_AXIS));
		panel.setLayout(new BorderLayout());
		buttonPanel.setLayout(new FlowLayout());
		noteText = new JTextArea();
		noteText.setEditable(true);
		noteText.setLineWrap(true);
		noteText.setWrapStyleWord(true);
		scroll = new JScrollPane(noteText);
		scroll.setSize(new Dimension(noteText.getPreferredSize()));	
		
		JButton viewParamsButton = new JButton("View Parameters");
		viewParamsButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingWorker<List<AnalysisEventParameter>, Void> worker = new SwingWorker<List<AnalysisEventParameter>, Void>(){

					@Override
					protected List<AnalysisEventParameter> doInBackground() throws Exception {
						return GenSpaceServerFactory.getPrivUsageFacade().getAnalysisParameters(currentEvent.getId());
					}
					@Override
					protected void done() {
						super.done();
						try{
							String message = "";
							for(AnalysisEventParameter p : get())
							{
								message += p.getParameterKey() + " \""+p.getParameterValue()+"\"\n";
							}
							JOptionPane.showMessageDialog(panel, message, "Parameters for " + currentEvent.getToolname(), JOptionPane.INFORMATION_MESSAGE);
						}
						catch(Exception ex)
						{
							ex.printStackTrace();
						}
					}
				};
				worker.execute();
			}
		});
		buttonPanel.add(viewParamsButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				parent.clearSelection();
				parent.removeEditor();
			}
		});
		buttonPanel.add(cancelButton);
		
		JButton saveButton = new JButton("Save");
		;
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCellEditing();
			}
		});
		buttonPanel.add(saveButton);

		secondPanel.add(noteInfo);
		secondPanel.add(dataSetInfo);
		secondPanel.add(scroll);
		panel.add(secondPanel, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		panel.setOpaque(true);
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Now editing (Esc to cancel)"));
//		panel.setBackground(new Color(253,252,132));
		noteText.setBackground(new Color(0,0,0,0));
		noteText.setOpaque(false);
//		noteInfo.setBackground(new Color(253,252,132));
	}
	public JTextArea getNoteText()
	{
		return noteText;
	}
	@Override
	public void addCellEditorListener(CellEditorListener listener) {
		
	}

	@Override
	public void cancelCellEditing() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getCellEditorValue()
	{
		return currentEvent;
	}

	@Override
	public boolean isCellEditable(EventObject arg0) {
		return true;
	}

	@Override
	public void removeCellEditorListener(CellEditorListener arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean shouldSelectCell(EventObject arg0) {
		return false;
	}

	@Override
	public boolean stopCellEditing() {
		currentEvent.setNote(noteText.getText());
		GenSpaceServerFactory.getPrivUsageFacade().saveNote(currentEvent);
		parent.clearSelection();
		parent.removeEditor();
		return true;
	}
	private Date convertToDate(XMLGregorianCalendar cal) {
		return DatatypeConverter.parseDateTime(cal.toXMLFormat()).getTime();
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		SimpleDateFormat format = new SimpleDateFormat("F/M/yy h:mm a");
		TableModel lm = table.getModel();
		AnalysisEvent e = (AnalysisEvent) lm.getValueAt(row, 0); 
		currentEvent = e;
		noteInfo.setText(e.getTool().getName() + " at " + format.format(convertToDate(e.getCreatedAt())));
		noteText.setText(e.getNote());
		noteText.setSize(table.getWidth(), noteText.getHeight());

		dataSetInfo.setText("Dataset: " + e.getTransaction().getDataSetName());
		table.setRowHeight(row, 150 +  (noteText.getFontMetrics(noteText.getFont()).getHeight() * (NotebookPanel.countLines(noteText) +2)));
		
//		panel.setBackground(new Color(255,255,0,255));

		return panel;
	}
	
	

	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}


	
}
