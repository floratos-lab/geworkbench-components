package org.geworkbench.components.alignment;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.BlastObj;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.builtin.projects.ProjectSelection;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.FilePathnameUtils;
import org.geworkbench.util.JAutoList;

/**
 * Blast result view.
 * 
 * @author XZ
 * @version $Id$
 */
@AcceptTypes( { DSAlignmentResultSet.class })
public class BlastViewComponent implements VisualPlugin {
	private static Log log = LogFactory.getLog(BlastViewComponent.class);

	private final JPanel blastViewPanel;

	@Publish
	public org.geworkbench.events.ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			org.geworkbench.events.ProjectNodeAddedEvent event) {
		return event;
	}

	public BlastViewComponent() {
		blastViewPanel = new JPanel();
		try {
			initializeBlastViewPanel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initializeBlastViewPanel() {

		JSplitPane mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainPanel.setOneTouchExpandable(true);

		rightPanel = new JSplitPane();
		rightPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
		markerList = new MarkerAutoList();

		blastResult.setLayout(new BorderLayout());
		AddSequenceToProjectButton.setMinimumSize(new Dimension(100, 23));

		AddSequenceToProjectButton
				.setText("Add Selected Sequences to Project ");
		AddSequenceToProjectButton
				.addActionListener(new AddSelectedActionAdapter(
						));
		resetButton.setToolTipText("Clear all selections.");
		resetButton.setText("Reset");
		resetButton
				.addActionListener(new ResetActionAdapter(
						));

		blastResult.setBorder(BorderFactory.createLoweredBevelBorder());
		blastResult.setPreferredSize(new Dimension(145, 150));
		blastResult.setMinimumSize(new Dimension(145, 100));
		detailedInfo.setPreferredSize(new Dimension(145, 150));
		detailedInfo.setMinimumSize(new Dimension(145, 100));
		rightPanel.setPreferredSize(new Dimension(155, 400));
		rightPanel.setDividerSize(2);

		rightPanel.setMinimumSize(new Dimension(155, 300));

		JPanel summaryPanel = new JPanel();
		summaryLabel = new JLabel();
		summaryPanel.setBorder(BorderFactory.createLoweredBevelBorder());
		summaryPanel.setMinimumSize(new Dimension(100, 40));
		summaryPanel.setPreferredSize(new Dimension(100, 40));
		JPanel furtherProcess = new JPanel();
		furtherProcess.setLayout(new BorderLayout());
		furtherProcess.add(summaryPanel, java.awt.BorderLayout.CENTER);
		furtherProcess.add(summaryLabel, java.awt.BorderLayout.SOUTH);

		furtherProcess.setMinimumSize(new Dimension(50, 40));
		furtherProcess.setPreferredSize(new Dimension(50, 40));
		furtherProcess.setMaximumSize(new Dimension(50, 40));

		detailedInfo.setLayout(new BorderLayout());

		singleAlignmentArea.setContentType("text/html");

		singleAlignmentArea.setEditable(false);
		singleAlignmentArea.addHyperlinkListener(new BlastResultHyperlinkListener());

		addAlignedButton.setMinimumSize(new Dimension(100, 23));
		addAlignedButton.setToolTipText("Add only aligned parts into project.");
		addAlignedButton.setText("Only Add Aligned Parts");
		addAlignedButton
				.addActionListener(new AddAllActionAdapter());
		allButton.setToolTipText("Select all hits.");
		allButton.setText("Select All");
		allButton.addActionListener(new SelectAllActionAdapter(
				));
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.setMinimumSize(new Dimension(100, 100));
		jSplitPane1.setPreferredSize(new Dimension(500, 600));
		jSplitPane1.setDividerSize(1);
		blastViewPanel.setLayout(new BorderLayout());

		summaryPanel.add(resetButton, null);
		summaryPanel.add(allButton);
		summaryPanel.add(AddSequenceToProjectButton, null);
		summaryPanel.add(addAlignedButton);

		jSplitPane1.add(blastResult, JSplitPane.TOP);
		JScrollPane jScrollPane1 = new JScrollPane();
		detailedInfo.add(jScrollPane1, BorderLayout.CENTER);
		jSplitPane1.add(detailedInfo, JSplitPane.BOTTOM);
		jSplitPane1.setDividerLocation(jSplitPane1DividerLocation);
		jScrollPane1.getViewport().add(singleAlignmentArea, null);

		rightPanel.add(jSplitPane1, JSplitPane.TOP);
		// rightPanel.setDividerLocation(0.8);
		rightPanel.add(furtherProcess, JSplitPane.BOTTOM);
		currentError = "No alignment result is loaded, please check again.";
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(markerList, BorderLayout.CENTER);
		// mainPanel.add(markerList);
		mainPanel.add(westPanel);
		mainPanel.add(rightPanel);
		double propLoc = .5D;
		rightPanel.setDividerLocation(propLoc);
		blastViewPanel.add(mainPanel, java.awt.BorderLayout.CENTER);
	}

	private static class BlastResultHyperlinkListener implements HyperlinkListener {

		@Override
		public void hyperlinkUpdate(HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					BrowserLauncher.openURL(event.getURL().toString());
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}

	}
	
	/**
	 * getComponent
	 * 
	 * @return Component
	 */
	public Component getComponent() {
		return blastViewPanel;
	}
	
	transient DSAlignmentResultSet resultSet = null;

	/**
	 * receiveProjectSelection
	 * 
	 * @param e
	 *            ProjectEvent
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {

		ProjectSelection selection = ((ProjectPanel) source).getSelection();
		DSAncillaryDataSet<?> df = selection.getDataSubSet();

		if (df == resultSet || !(df instanceof DSAlignmentResultSet)) {
			return; // do nothing
		}

		log.debug("update blast result view panel");
		resultSet = (DSAlignmentResultSet) df;

		// Get the sequenceDb from DAncillaryDataset, not from project.
		DSSequenceSet<? extends DSSequence> sequenceDB = resultSet
				.getBlastedParentDataSet();
		ArrayList<Vector<BlastObj>> blastDataSet = resultSet.getBlastDataSet();

		// if (sequenceDB instanceof CSSequenceSet) // always true
		this.sequenceDB = sequenceDB;
		geneListModel.refresh();

		this.blastDataSet = blastDataSet;
		if (markerList != null && blastDataSet != null && sequenceDB != null) {
			markerList.setHighlightedIndex(sequenceDB.size() - 1);
		}

		summaryStr = resultSet.getSummary();
		displaySummaryLabel(summaryStr);
		resultSet.setDescription(summaryStr);

		if (resultSet.getHitCount() == 0) {
			resetToWhite("No alignment hit is found.");
			// set SummaryPanel Off
			resetButton.setEnabled(false);
			allButton.setEnabled(false);
			AddSequenceToProjectButton.setEnabled(false);
			addAlignedButton.setEnabled(false);
		} else {
			// set SummaryPanel On
			resetButton.setEnabled(true);
			allButton.setEnabled(true);
			AddSequenceToProjectButton.setEnabled(true);
			addAlignedButton.setEnabled(true);
		}

	}

	private void resetToWhite(String detailString) {

		blastResult.removeAll();
		if (detailString == null) {
			singleAlignmentArea.setText("Alignment Detail panel");
		} else {
			singleAlignmentArea.setText(detailString);
		}

		blastViewPanel.revalidate();
		singleAlignmentArea.setCaretPosition(0);
	}

	private void displaySummaryLabel(String s) {

		summaryLabel.setText(s);
		double propLoc = .8D;
	    rightPanel.setDividerLocation(propLoc);
		rightPanel.setResizeWeight(0.95);
	    jSplitPane1.setDividerLocation(jSplitPane1DividerLocation);
		jSplitPane1.setResizeWeight(0.4);		
		blastViewPanel.revalidate();		 
	}

	/* These fields used to be in BlastViewPanel */
	private JAutoList markerList;

	private JPanel blastResult = new JPanel();
	private JPanel detailedInfo = new JPanel();

	private JButton AddSequenceToProjectButton = new JButton();
	private JLabel summaryLabel = new JLabel();
	private JButton resetButton = new JButton();
	private JEditorPane singleAlignmentArea = new JEditorPane();
	private int selectedInputSequence = 0;
	private String summaryStr;

	private String currentError;

	private JButton addAlignedButton = new JButton();
	private JButton allButton = new JButton();
	private JSplitPane jSplitPane1 = new JSplitPane();

	private GeneListModel geneListModel = new GeneListModel();
	private JSplitPane rightPanel = new JSplitPane();
	private DSSequenceSet<? extends DSSequence> sequenceDB;
	private ArrayList<Vector<BlastObj>> blastDataSet = new ArrayList<Vector<BlastObj>>();
	private final double jSplitPane1DividerLocation = 0.5;
	
	private void selectInputSequence(int selecIndext) {
		selectedInputSequence = selecIndext;

		blastResult.removeAll();
		blastResult.add(getBlastListPanel());
		blastViewPanel.revalidate();
		blastViewPanel.repaint();
	}

	/**
	 * Returns a JScrollpane containing Blast results in table format.
	 *
	 * @return a JScrollpane containing table of Blast results.
	 */
	private JScrollPane getBlastListPanel() {

		/* customized table Model */
		HitsTableModel myModel = new HitsTableModel();
		/* table based on myModel */
		JTable table = new JTable(myModel);

		// setting the size of the table and its columns
		table.setPreferredScrollableViewportSize(new Dimension(800, 100));		 
		table.getColumnModel().getColumn(0).setPreferredWidth(35);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(300);
		table.getColumnModel().getColumn(3).setPreferredWidth(50);
		table.getColumnModel().getColumn(4).setPreferredWidth(80);
		table.getColumnModel().getColumn(5).setPreferredWidth(80);
		table.getColumnModel().getColumn(6).setPreferredWidth(30);

		/* set up Listener for row selection on table */
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new BlastDetaillistSelectionListener());
		table.setSelectionModel(rowSM);
		table.changeSelection(0, 0, false, false);

		return new JScrollPane(table);

	}

	private class BlastDetaillistSelectionListener implements
			ListSelectionListener {
		int selectedRow;
		BlastObj selectedHit;

		public void valueChanged(ListSelectionEvent e) {
			// Ignore extra messages.
			if (e.getValueIsAdjusting()) {
				return;
			}
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			if (lsm.isSelectionEmpty()) {

			} else {
				selectedRow = lsm.getMinSelectionIndex();
				Vector<BlastObj> hits = blastDataSet.get(selectedInputSequence);
				if (hits != null && hits.size() > selectedRow) {
					selectedHit = hits.get(selectedRow);

					String text = selectedHit.getDetailedAlignment();
					singleAlignmentArea.setText(text);
					singleAlignmentArea.setCaretPosition(0);

				}

			}
		}
	}

	/**
	 * This class extends AbstractTableModel and creates a table view of Blast
	 * results.
	 */
	private class HitsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -6186103749946782665L;

		/* array of the column names in order from left to right */
		final String[] columnNames = { "db", "Name", "Description", "e-value",
				"start point", "align length", "%identity", "Include" };

		/* returns the number of columns in table */
		public int getColumnCount() {
			return columnNames.length;
		}

		/* returns the number of rows in table */
		public int getRowCount() {
			Vector<BlastObj> hits = blastDataSet.get(selectedInputSequence);
			return (hits.size());
		}

		/* return the header for the column number */
		public String getColumnName(int col) {
			return columnNames[col];
		}

		/* get the Object data to be displayed at (row, col) in table */
		public Object getValueAt(int row, int col) {
			Vector<BlastObj> hits = blastDataSet.get(selectedInputSequence);
			/* get specific BlastObj based on row number */
			BlastObj hit = (BlastObj) hits.get(row);
			/* display data depending on which column is chosen */
			switch (col) {
			case 0:
				return hit.getDatabaseID(); // database ID
			case 1:
				return hit.getName(); // accesion number
			case 2:
				return hit.getDescription(); // description
			case 3:
				return hit.getEvalue(); // evalue
			case 4:
				return new Integer(hit.getStartPoint());
				// length of hit protein
			case 5:
				return new Integer(hit.getAlignmentLength());
			case 6:
				// percent of sequence aligned to hit sequence
				return new Integer(hit.getPercentAligned());
			case 7:
				return new Boolean(hit.getInclude()); // whether is chosen for
														// MSA
			}
			return null;
		}

		/* returns the Class type of the column c */
		public Class<?> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * returns if the cell is editable; returns false for all cells in
		 * columns except column 6
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 6) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * detect change in cell at (row, col); set cell to value; update the
		 * table
		 */
		public void setValueAt(Object value, int row, int col) {
			Vector<BlastObj> hits = blastDataSet.get(selectedInputSequence);
			BlastObj hit = hits.get(row);
			hit.setInclude(((Boolean) value).booleanValue());
			fireTableCellUpdated(row, col);
		}

	}

	/**
	 * The marker JAutoList type.
	 */
	private class MarkerAutoList extends JAutoList {
		private static final long serialVersionUID = 8058920616446339946L;

		public MarkerAutoList() {
			super(geneListModel);
		}

		public boolean setHighlightedIndex(int theIndex) {
			super.setHighlightedIndex(theIndex);
			elementClicked(theIndex, null);
			return true;
		}

		@Override
		protected void elementClicked(int index, MouseEvent e) {
			if (blastDataSet != null && blastDataSet.size() > index) {
				if (blastDataSet.get(index) != null) {
					selectInputSequence( index );
					displaySummaryLabel(" " + summaryStr + " Sequence "
							+ ((CSSequence) sequenceDB.get(index)).getLabel()
							+ " has "
							+ blastDataSet.get(index).size()
							+ " hits.");

				} else {

					selectInputSequence(-1);
					resetToWhite("No hits found");
					displaySummaryLabel(" " + summaryStr + " Sequence "
							+ ((CSSequence) sequenceDB.get(index)).getLabel()
							+ " has 0 hits.");

				}
			} else if (blastDataSet != null && blastDataSet.size()>0
					&& blastDataSet.get(0) != null) {
				selectInputSequence(0);
			}
		}

		@Override
		protected void elementRightClicked(int index, MouseEvent e) {
		}
	}

	/**
	 * ListModel for the marker list.
	 */
	private class GeneListModel extends AbstractListModel {
		private static final long serialVersionUID = 5222516039991183235L;

		public int getSize() {
			if (sequenceDB == null) {
				return 0;
			}
			return sequenceDB.size();
		}

		public Object getElementAt(int index) {
			if (sequenceDB == null) {
				return null;
			}
			return ((CSSequence) sequenceDB.get(index)).getLabel();
		}

		/**
		 * Indicates to the associated JList that the contents need to be
		 * redrawn.
		 */
		public void refresh() {
			if (sequenceDB == null) {
				fireContentsChanged(this, 0, 0);
			} else {
				fireContentsChanged(this, 0, sequenceDB.size());
			}
		}

	}
	
	/**
	 * Display details of each alignment.
	 *
	 * @param s
	 */
	private void displayResults(String s) {

		blastResult.removeAll();
		blastResult.add(getBlastListPanel());
		singleAlignmentArea.setText(s);

		blastViewPanel.revalidate();
		singleAlignmentArea.setCaretPosition(0);
	}
	
	private void reportError(String errorMessage) {
		JOptionPane.showMessageDialog(null, errorMessage, "Error",
				JOptionPane.INFORMATION_MESSAGE);
	}
	
	private volatile boolean empty = true;

	private void addNewSequence(final boolean isFullLength) {
		int total = 0;
		int countForSelectedSequence = 0;
		for (int index = 0; index < blastDataSet.size(); index++) {
			Vector<BlastObj> hits = blastDataSet.get(index);
			for (BlastObj hit : hits) {
				if (hit.getInclude()) {
					total++;
					if (index == selectedInputSequence)
						countForSelectedSequence++;
				}
			}
		}
		if (total == 0) {
			reportError("No hit is selected. Please choose at least one.");
			return;
		}
		if (countForSelectedSequence < total) {
			int ret = JOptionPane
					.showConfirmDialog(
							null,
							"Some of selected hits belong to the sequences other than the one currently highlighted.\n"
									+ "They will be included in the project too. Do you want to continue?",
							"Include all selected hits?",
							JOptionPane.YES_NO_OPTION);
			if (ret != JOptionPane.YES_OPTION) {
				return;
			}
		}
		final int countToBeAdded = countForSelectedSequence;

		String tempString = "temp-" + RandomNumberGenerator.getID() + ".fasta";
		String tempFolder = FilePathnameUtils.getTemporaryFilesDirectoryPath();
		final File tempFile = new File(tempFolder + tempString);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				PrintWriter out;
				try {
					out = new PrintWriter(new FileOutputStream(tempFile));
					ProgressMonitor progressMonitor = new ProgressMonitor(
							detailedInfo, "Collecting Sequences ...", "", 0,
							countToBeAdded);

					int retrievedSequenceNum = 0;

					progressMonitor.setProgress(retrievedSequenceNum);
					for (int index = 0; index < blastDataSet.size(); index++) {
						Vector<BlastObj> hits = blastDataSet.get(index);
						for (int i = 0; i < hits.size(); i++) {
							BlastObj hit = hits.get(i);
							progressMonitor.setProgress(retrievedSequenceNum);

							if (progressMonitor.isCanceled()) {
								empty = true;
								return null;
							}
							if (hit.getInclude()) {

								retrievedSequenceNum++;

								CSSequence seq = null;
								if (isFullLength) {
									seq = hit.getWholeSeq();
								} else {
									seq = hit.getAlignedSeq();

								}
								if (seq != null) {
									out.println(seq.getLabel());
									out.println(seq.getSequence());
									empty = false;
								}
							}

						}
					}
					out.flush();
					out.close();
					progressMonitor.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {
				if (empty) {
					log.info("cancelled or for other reason no hits returned");
					return;
				}

				CSSequenceSet<DSSequence> db = new CSSequenceSet<DSSequence>();
				db.setLabel("temp_Fasta_File");
				db.readFASTAFile(tempFile);

				org.geworkbench.events.ProjectNodeAddedEvent event = new org.geworkbench.events.ProjectNodeAddedEvent(
						"message", db, null);
				publishProjectNodeAddedEvent(event);
			}

		};
		worker.execute();
	}

	// following are action adapters for the buttons
	private class SelectAllActionAdapter implements
			ActionListener {

		public void actionPerformed(ActionEvent e) {
			Vector<BlastObj> hits =  blastDataSet.get(selectedInputSequence);
			if (hits == null) {
				reportError(currentError);
				return;
			}
			for (int i = 0; i < hits.size(); i++) {
				BlastObj hit = hits.get(i);
				hit.setInclude(true);

			}
			displayResults("<h4>All are selected.");
		}
	}

	private class AddSelectedActionAdapter implements
			java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {
			Vector<BlastObj> hits =  blastDataSet.get(selectedInputSequence);
			if (hits == null) {
				reportError(currentError);
				return;
			}
			addNewSequence(true);
		}
	}

	private class AddAllActionAdapter
			implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			Vector<BlastObj> hits =  blastDataSet.get(selectedInputSequence);
			if (hits == null) {
				reportError(currentError);
				return;
			}
			addNewSequence(false);
		}
	}

	private class ResetActionAdapter implements
			java.awt.event.ActionListener {

		public void actionPerformed(ActionEvent e) {
			Vector<BlastObj> hits =  blastDataSet.get(selectedInputSequence);
			if (hits == null) {
				reportError(currentError);
				return;
			}

			for (int i = 0; i < hits.size(); i++) {
				BlastObj hit = hits.get(i);
				hit.setInclude(false);
			}

			displayResults("<h4>No alignment hit is selected.");
		}
	}
}
