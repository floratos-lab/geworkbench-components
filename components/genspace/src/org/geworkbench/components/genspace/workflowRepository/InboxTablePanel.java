package org.geworkbench.components.genspace.workflowRepository;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * TableSelectionDemo.java requires no other files.
 */

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.IncomingWorkflow;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.wrapper.WorkflowWrapper;
import org.geworkbench.engine.config.VisualPlugin;

public class InboxTablePanel extends JPanel implements ActionListener,
VisualPlugin {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2445067698516787918L;
	public JTable table;
	public WorkflowRepository workflowRepository;
	final public JButton addButton = new JButton("Add");
	final public JButton deleteButton = new JButton("Delete");

	public InboxTablePanel(WorkflowRepository wr) {
		super(new BorderLayout());
		// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Workflow Inbox"));
		workflowRepository = wr;

		table = new JTable(new MyTableModel()) {

			private static final long serialVersionUID = -5336081423299910192L;

			@Override
			public String getToolTipText(MouseEvent e) {
				java.awt.Point p = e.getPoint();
				int rowIndex = rowAtPoint(p);
				int colIndex = columnAtPoint(p);
				int realColumnIndex = convertColumnIndexToModel(colIndex);
				Object tip = getValueAt(rowIndex, realColumnIndex);
				if (tip != null && tip instanceof String && !tip.equals(""))
					return (String) tip;
				else
					return super.getToolTipText();
			}
		};

		table.setPreferredScrollableViewportSize(new Dimension(200, 70));
		table.setFillsViewportHeight(true);
		table.getSelectionModel().addListSelectionListener(new RowListener());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumnModel().getSelectionModel()
		.addListSelectionListener(new ColumnListener());
		add(new JScrollPane(table), BorderLayout.CENTER);

		addButton.addActionListener(this);
		deleteButton.addActionListener(this);
		JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
		add(buttonPanel, BorderLayout.SOUTH);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	public void setData(List<IncomingWorkflow> list) {
		MyTableModel model = (MyTableModel) table.getModel();
		if (list != null)
			model.setData(list);
		else
			clearData();

	}

	public void clearData() {
		MyTableModel model = (MyTableModel) table.getModel();
		model.setData(new ArrayList<IncomingWorkflow>());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		final Object source = event.getSource();
		if (GenSpaceServerFactory.isLoggedIn()) {
			int i = table.getSelectedRow();
			if (i != -1) {
				MyTableModel model = (MyTableModel) table
				.getModel();
				IncomingWorkflow wi = model.getWorkflowAtRow(i);
				if (source.equals(addButton)) {
					addToRepository(wi, model);
				} else if (source.equals(deleteButton)) {
					removeFromInbox(wi, model);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Please select an incoming workflow from your inbox\n" +
																			"before you attempt to add it to your repository or\n" +
																			"remove it from your inbox","Error",JOptionPane.WARNING_MESSAGE);
			}
		}

	}

	private void removeFromInbox(final IncomingWorkflow wi,
			final MyTableModel model) {
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
			protected Boolean doInBackground() throws Exception {
				Boolean ret =  GenSpaceServerFactory.getWorkflowOps()
						.deleteFromInbox(wi.getId());
				workflowRepository.updateFormFieldsBG();
				return ret;
			};

			protected void done() {
				try {
					if (get()) {
						model.delIncomingWorkflow(wi);
					}
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server", e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
			};
		};
		worker.execute();
	}

	private void addToRepository(final IncomingWorkflow wi,
			final MyTableModel model) {
		
		SwingWorker<UserWorkflow, Void> worker = new SwingWorker<UserWorkflow, Void>() {
			protected UserWorkflow doInBackground() throws Exception {
				UserWorkflow ret = GenSpaceServerFactory.getWorkflowOps()
						.addToRepository(wi.getId());
				workflowRepository.updateFormFieldsBG();
				return ret;
			};

			protected void done() {
				try {
					UserWorkflow ret = get();
					if (ret != null) {
						workflowRepository.repositoryPanel.tree
						.recalculateAndReload();
						// delete
						removeFromInbox(wi, model);
					}
				} catch (InterruptedException e) {
					GenSpace.logger.warn("Unable to talk to server", e);
				} catch (ExecutionException e) {
					GenSpaceServerFactory.handleExecutionException(e);
					return;
				}
			};
		};
		worker.execute();

	}



private class RowListener implements ListSelectionListener {
	@Override
	public void valueChanged(ListSelectionEvent event) {
		int i = table.getSelectedRow();
		if (i != -1) {
			MyTableModel model = (MyTableModel) table.getModel();
			IncomingWorkflow wi = model.getWorkflowAtRow(i);
			workflowRepository.graphPanel.render(new WorkflowWrapper(wi.getWorkflow()));
			workflowRepository.workflowDetailsPanel
			.setAndPrintWorkflow(wi.getWorkflow());
			workflowRepository.workflowCommentsPanel.setData(wi.getWorkflow());
		}
		if (event.getValueIsAdjusting()) {
			return;
		}
	}
}

private class ColumnListener implements ListSelectionListener {
	@Override
	public void valueChanged(ListSelectionEvent event) {
		if (event.getValueIsAdjusting()) {
			return;
		}
	}
}

class MyTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1827659420978777265L;
	private String[] columnNames = { "Name", "User", "Date" };
	public List<IncomingWorkflow> data;

	public MyTableModel() { 
		super();
		data = new ArrayList<IncomingWorkflow>();
	}

	public MyTableModel(List<IncomingWorkflow> ws) {
		data = ws;
	}

	public void setData(List<IncomingWorkflow> list) {
		data = list;
		this.fireTableDataChanged();
	}

	public void addIncomingWorkflow(IncomingWorkflow wi) {
		int index = data.size();
		data.add(wi);
		this.fireTableRowsInserted(index, index);
	}

	public void delIncomingWorkflow(IncomingWorkflow wi) {
		for (int i = 0; i < data.size(); i++) {
			IncomingWorkflow w = data.get(i);
			if (w.getName().equals(wi.getName()) && w.getSender().equals(wi.getSender())
					&& w.getCreatedAt().equals(wi.getCreatedAt())) {
				data.remove(i);
				this.fireTableRowsDeleted(i, i);
				break;
			}
		}
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (row >= 0 && row < data.size()) {
			IncomingWorkflow wi = data.get(row);
			if (col == 0)
				return wi.getName();
			else if (col == 1)
				return wi.getSender().getUsername();
			else
				return wi.getCreatedAt().toString();
		}
		return null;
	}

	public IncomingWorkflow getWorkflowAtRow(int row) {
		if (row < data.size())
			return data.get(row);
		else
			return null;
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column
	 * would contain text ("true"/"false"), rather than a check box.
	 */
	@Override
	public Class<? extends Object> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	@Override
	public boolean isCellEditable(int row, int col) {
		// Note that the data/cell address is constant,
		// no matter where the cell appears onscreen.
		return false;
	}

	/*
	 * Don't need to implement this method unless your table's data can
	 * change.
	 */
	@Override
	public void setValueAt(Object value, int row, int col) {
		// not editable
	}

}

/**
 * Create the GUI and show it. For thread safety, this method should be
 * invoked from the event-dispatching thread.
 */
private static void createAndShowGUI() {
	// Disable boldface controls.
	UIManager.put("swing.boldMetal", Boolean.FALSE);

	// Create and set up the window.
	JFrame frame = new JFrame("TableSelectionDemo");
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	// Create and set up the content pane.
	InboxTablePanel newContentPane = new InboxTablePanel(null);
	newContentPane.setOpaque(true); // content panes must be opaque
	frame.setContentPane(newContentPane);

	// Display the window.
	frame.pack();
	frame.setVisible(true);
}

public static void main(String[] args) {
	// Schedule a job for the event-dispatching thread:
	// creating and showing this application's GUI.
	javax.swing.SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
			createAndShowGUI();
		}
	});
}
}
