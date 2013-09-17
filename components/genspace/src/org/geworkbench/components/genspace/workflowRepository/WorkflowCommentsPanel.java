package org.geworkbench.components.genspace.workflowRepository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeFactory;

import org.geworkbench.components.genspace.GenSpace;
import org.geworkbench.components.genspace.GenSpaceServerFactory;
import org.geworkbench.components.genspace.server.stubs.User;
import org.geworkbench.components.genspace.server.stubs.UserWorkflow;
import org.geworkbench.components.genspace.server.stubs.Workflow;
import org.geworkbench.components.genspace.server.stubs.WorkflowComment;
import org.geworkbench.engine.config.VisualPlugin;

public class WorkflowCommentsPanel extends JPanel implements VisualPlugin,
ActionListener {


	private static final long serialVersionUID = 4975744972196562645L;
	public JTable table;
//	private WorkflowRepository workflowRepository;
	final public JButton newButton = new JButton("New");
	final public JButton removeButton = new JButton("Remove");
	public Workflow workflow;

	public WorkflowCommentsPanel(WorkflowRepository wr) {
		super(new BorderLayout());
		// setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder(), "Workflow Comments"));
//		workflowRepository = wr;

		table = new JTable(new MyTableModel()) {


			private static final long serialVersionUID = 2666623917953696650L;

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
		JScrollPane jsp = new JScrollPane(table);

		TableColumn columnUser = table.getColumnModel().getColumn(0);
		columnUser.setPreferredWidth(100);
		columnUser.setMaxWidth(100);
		TableColumn columnDate = table.getColumnModel().getColumn(1);
		columnDate.setPreferredWidth(120);
		columnDate.setMaxWidth(120);
		table.setAutoResizeMode(WIDTH);
		add(jsp, BorderLayout.CENTER);

		newButton.addActionListener(this);
		removeButton.addActionListener(this);
		JPanel buttonPanel = new JPanel(new GridLayout(0, 2));
		buttonPanel.add(newButton);
		buttonPanel.add(removeButton);
		add(buttonPanel, BorderLayout.NORTH);
	}

	public void setData(Workflow workflow) {
		this.workflow = workflow;
		MyTableModel model = (MyTableModel) table.getModel();
		if (workflow != null)
			model.setData(GenSpaceServerFactory.getUsageOps().getWFComments(workflow));
		else
			clearData();
	}

	public void clearData() {
		workflow = null;
		MyTableModel model = (MyTableModel) table.getModel();
		model.setData(new ArrayList<WorkflowComment>());
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Object source = e.getSource();
		User u = GenSpaceServerFactory.getUser();
		if (u != null) {
			MyTableModel model = (MyTableModel) table.getModel();
			if (source.equals(newButton)) {
				newComment(model);
			} else if (source.equals(removeButton)) {
				int i = table.getSelectedRow();
				if (i != -1) {
					WorkflowComment wc = model
					.getWorkflowCommentAtRow(i);
					removeComment(wc, model);
				} else
					JOptionPane.showMessageDialog(null, "Please select a comment to delete");
			}
		}
	}


	private void removeComment(final WorkflowComment wc,
			final MyTableModel model) {
		if (wc.getCreator().getId() != GenSpaceServerFactory.getUser().getId()) {
			JOptionPane.showMessageDialog(null, "You can only delete your own comments.");
		}
		
		SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
			protected Boolean doInBackground() throws Exception {
				Boolean ret = GenSpaceServerFactory.getWorkflowOps()
						.removeComment(wc.getId());
				GenSpaceServerFactory.updateCachedUser();
				if(GenSpaceServerFactory.isLoggedIn())
				{
					GenSpace.getInstance().getWorkflowRepository().repositoryPanel.tree.root = (GenSpaceServerFactory.getUserOps().getRootFolder());
					GenSpace.getInstance().getWorkflowRepository().repositoryPanel.tree.recalculateAndReload();
				}
//				GenSpace.getInstance().getWorkflowRepository().updateFormFieldsBG();
				return ret;
			};

			protected void done() {
				try {
					if (get()) {
						model.removeComment(wc);
						for(UserWorkflow w : GenSpace.getInstance().getWorkflowRepository().repositoryPanel.tree.root.getWorkflows())
						{
							if(w.getWorkflow().getId() == workflow.getId())
							{
								GenSpace.getInstance().getWorkflowRepository().workflowCommentsPanel.setData(w.getWorkflow());
								GenSpace.getInstance().getWorkflowRepository().workflowCommentsPanel.repaint();
							}
						}
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

	private void newComment(final MyTableModel model) {
		final String comment = JOptionPane
		.showInputDialog("Input comment text");
		if (comment != null && !comment.trim().equals("")) {
			
			
			SwingWorker<WorkflowComment, Void> worker = new SwingWorker<WorkflowComment, Void>() {
				protected WorkflowComment doInBackground() throws Exception {
					WorkflowComment wc = new WorkflowComment();
					wc.setComment(comment);
					wc.setCreatedAt(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()));
					wc.setCreator(GenSpaceServerFactory.getUser());
					WorkflowComment ret =GenSpaceServerFactory.getWorkflowOps()
							.addCommentToWf(wc, workflow);
					GenSpaceServerFactory.updateCachedUser();
					if(GenSpaceServerFactory.isLoggedIn())
					{
						GenSpace.getInstance().getWorkflowRepository().repositoryPanel.tree.root = (GenSpaceServerFactory.getUserOps().getRootFolder());
						GenSpace.getInstance().getWorkflowRepository().repositoryPanel.tree.recalculateAndReload();
					}
					return ret;
				};

				protected void done() {
					for(UserWorkflow w : GenSpace.getInstance().getWorkflowRepository().repositoryPanel.tree.root.getWorkflows())
					{
						if(w.getWorkflow().getId() == workflow.getId())
						{
							GenSpace.getInstance().getWorkflowRepository().workflowCommentsPanel.setData(w.getWorkflow());
							GenSpace.getInstance().getWorkflowRepository().workflowCommentsPanel.repaint();
						}
					}
				};
			};
			worker.execute();
		} 
	}



private class RowListener implements ListSelectionListener {
	@Override
	public void valueChanged(ListSelectionEvent event) {
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
	private static final long serialVersionUID = -2623963206256666425L;
	private String[] columnNames = { "User", "Date", "Comment" };
	public List<WorkflowComment> data;

	public MyTableModel() {
		super();
		data = new ArrayList<WorkflowComment>();
	}

	public MyTableModel(ArrayList<WorkflowComment> wc) {
		data = wc;
	}
	public void setData(List<WorkflowComment> list) {
		data = list;
		this.fireTableDataChanged();
	}
	public void setData(WorkflowComment[] list) {
		data = Arrays.asList(list);
		this.fireTableDataChanged();
	}

	public void addComment(WorkflowComment wc) {
		int index = data.size();
		data.add(wc);
		this.fireTableRowsInserted(index, index);
	}

	public void removeComment(WorkflowComment wc) {
		for (int i = 0; i < data.size(); i++) {
			WorkflowComment w = data.get(i);
			if (w.getCreator().equals(wc.getCreator())
					&& w.getCreatedAt().equals(wc.getCreatedAt())
					&& w.getComment().equals(wc.getComment())) {
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
SimpleDateFormat fmt = new SimpleDateFormat("F/M/yy h:mm a");
	@Override
	public Object getValueAt(int row, int col) {
		if (row >= 0 && row < data.size()) {
			WorkflowComment wi = data.get(row);
			if (col == 0)
				return wi.getCreator().getUsername();
			else if (col == 1)
				return fmt.format(DatatypeConverter.parseDateTime(wi.getCreatedAt().toString()).getTime());
			else
				return wi.getComment();
		}
		return null;
	}

	public WorkflowComment getWorkflowCommentAtRow(int row) {
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class getColumnClass(int c) {
		if (data.size() > 0)
			return getValueAt(0, c).getClass();
		else
			return WorkflowComment.class;
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

}
