package org.geworkbench.components.matrixreduce;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.commons.collections15.map.ListOrderedMap;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceExperiment;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSMatrixReduceSet;
import org.geworkbench.bison.datastructure.complex.pattern.matrix.DSPositionSpecificAffintyMatrix;
import org.geworkbench.builtin.projects.LoadDataDialog;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.ProjectEvent;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import com.larvalabs.chart.PSAMPlot;

/**
 * @author John Watkinson
 * @author ch2514
 * @version $Id$
 */
@AcceptTypes(DSMatrixReduceSet.class)
public class MatrixReduceViewer implements VisualPlugin {

	private enum Direction {
		FORWARD, BACKWARD, BOTH
	}

	private static final int IMAGE_HEIGHT = 100;

	private static final int IMAGE_WIDTH = 200;

	private static final int TAB_SEQUENCE = 1;

	private JTabbedPane tabPane;

	private JPanel sequencePanel;

	private DSMatrixReduceSet dataSet = null;

	private ListOrderedMap<String, List<DSMatrixReduceExperiment>> exps = null;

	private boolean imageMode = true;

	private final TableModel model;

	private final ExpTableModel expModel;

	private JTable table, expsTable;

	private int defaultTableRowHeight;

	private int selectedPSAM = 0;

	private int currentPSAM = 0;

	private HashSet<DSPositionSpecificAffintyMatrix> selectedPSAMs = new HashSet<DSPositionSpecificAffintyMatrix>();

	private ListOrderedMap<String, String> sequences;

	private ArrayList<String> selectedSequences, consensusSequences;

	private final ListModel sequenceModel;

	private final ConsensusModel consensusModel;

	private HashMap<String, SequenceGraph> graphs = null;

	private boolean showForward = true;

	private boolean showBackward = true;

	private String filterSequence = null;

	private double threshold = 0.0;

	private JLabel psamLabel;

	private JList sequenceList;

	private final JComboBox psamList;

	private class ListModel extends AbstractListModel {
		private static final long serialVersionUID = -2759392322276667038L;

		public int getSize() {
			if (selectedSequences == null) {
				return 0;
			} else {
				return selectedSequences.size();
			}
		}

		public String getElementAt(int index) {
			return selectedSequences.get(index);
		}

		public void fireContentsChanged() {
			if (selectedSequences != null) {
				super
						.fireContentsChanged(this, 0,
								selectedSequences.size() - 1);
			} else {
				super.fireContentsChanged(this, 0, 0);
			}
		}
	}

	private class ConsensusModel extends AbstractListModel implements
			ComboBoxModel {
		private static final long serialVersionUID = -8877089706329828557L;
		
		String selection = null;

		public int getSize() {
			if (consensusSequences == null) {
				return 0;
			} else {
				return consensusSequences.size();
			}
		}

		public String getElementAt(int index) {
			return consensusSequences.get(index);
		}

		public void fireContentsChanged() {
			if (consensusSequences != null) {
				super.fireContentsChanged(this, 0,
						consensusSequences.size() - 1);
			} else {
				super.fireContentsChanged(this, 0, 0);
			}
		}

		public void setSelectedItem(Object item) {
			selection = (String) item;
		}

		public Object getSelectedItem() {
			return selection;
		}
	}

	private class TableModel extends AbstractTableModel {

		private static final long serialVersionUID = 2209495803556269287L;

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Select";
			case 1:
				return "Consensus Sequence";
			case 2:
				return "Experiment Name";
			case 3:
				return "Seed Sequence";
			case 4:
				return "F";
			case 5:
				return "t";
			default:
				return "p";
			}
		}

		public int getRowCount() {
			if (dataSet == null) {
				return 0;
			} else {
				return dataSet.size();
			}
		}

		public int getColumnCount() {
			if (dataSet == null) {
				return 0;
			} else {
				return 7;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			DSPositionSpecificAffintyMatrix psam = dataSet.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return selectedPSAMs.contains(psam);
			case 1:
				if (imageMode) {
					return getPsamImage(psam.getScores());
				} else {
					return psam.getConsensusSequence();
				}
			case 2:
				return psam.getExperiment();
			case 3:
				return psam.getSeedSequence();
			case 4:
				return psam.getCoeff();
			case 5:
				return psam.getTValue();
			default:
				return psam.getPValue();
			}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return true;
			} else {
				return false;
			}
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			assert (rowIndex == 0);
			DSPositionSpecificAffintyMatrix psam = dataSet.get(rowIndex);
			if (selectedPSAMs.contains(psam)) {
				selectedPSAMs.remove(psam);
			} else {
				selectedPSAMs.add(psam);
			}
		}

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 1:
				if (imageMode) {
					return ImageIcon.class;
				} else {
					return String.class;
				}
			case 4:
				return Double.class;
			case 5:
				return Double.class;
			case 6:
				return Double.class;
			default:
				return String.class;
			}
		}
	}

	private class ExpTableModel extends AbstractTableModel {

		private static final long serialVersionUID = -4605355596639274671L;

		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Experiment";
			case 1:
				return "F";
			case 2:
				return "t";
			default:
				return "p";
			}
		}

		public int getRowCount() {
			if ((dataSet == null) || (exps == null) || (exps.size() <= 0)
					|| (exps.get(dataSet.get(currentPSAM).getID()) == null)) {
				return 0;
			} else {
				return exps.get(dataSet.get(currentPSAM).getID()).size();
			}
		}

		public int getColumnCount() {
			if ((dataSet == null) || (exps == null)) {
				return 0;
			} else {
				return 4;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			List<DSMatrixReduceExperiment> list = exps.get(dataSet.get(currentPSAM).getID());
			switch (columnIndex) {
			case 0:
				return ((DSMatrixReduceExperiment) list.get(rowIndex)).getLabel();
			case 1:
				return ((DSMatrixReduceExperiment) list.get(rowIndex)).getCoeff();
			case 2:
				return ((DSMatrixReduceExperiment) list.get(rowIndex)).getTValue();
			default:
				return ((DSMatrixReduceExperiment) list.get(rowIndex)).getPValue();
			}
		}

		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return String.class;
			default:
				return Double.class;
			}
		}
	}

	public Component getComponent() {
		return tabPane;
	}

	public MatrixReduceViewer() {
		tabPane = new JTabbedPane();
		JPanel psamPanel = new JPanel(new BorderLayout());
		sequencePanel = new JPanel(new BorderLayout());
		tabPane.add("PSAM Detail", psamPanel);
		tabPane.add("Sequence", sequencePanel);
		JRadioButton nameViewButton = new JRadioButton("Name View");
		JRadioButton imageViewButton = new JRadioButton("Image View");
		nameViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageMode = false;
				table.setRowHeight(defaultTableRowHeight);
				model.fireTableDataChanged();
			}
		});
		imageViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				imageMode = true;
				table.setRowHeight(IMAGE_HEIGHT);
				model.fireTableDataChanged();
			}
		});
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(nameViewButton);
		buttonGroup.add(imageViewButton);
		imageViewButton.setSelected(true);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.add(nameViewButton);
		buttonPanel.add(imageViewButton);
		buttonPanel.add(Box.createHorizontalGlue());
		psamPanel.add(buttonPanel, BorderLayout.NORTH);
		// Do export button
		JButton exportAllButton = new JButton("Export All");
		exportAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportPSAMs(new HashSet<DSPositionSpecificAffintyMatrix>(
						dataSet));
			}
		});
		JButton exportSelectedButton = new JButton("Export Selected");
		exportSelectedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectedPSAMs.isEmpty()) {
					JOptionPane.showMessageDialog(getComponent(),
							"Please select some PSAMs.");
				} else {
					exportPSAMs(selectedPSAMs);
				}
			}
		});
		JPanel lowerPanel = new JPanel(new FlowLayout());
		lowerPanel.add(Box.createHorizontalGlue());
		lowerPanel.add(exportAllButton);
		lowerPanel.add(exportSelectedButton);
		psamPanel.add(lowerPanel, BorderLayout.SOUTH);

		model = new TableModel();
		table = new JTable(model);
		final JLabel imageLabel = new JLabel() {
			private static final long serialVersionUID = -5166715135998198492L;

			public Dimension getMinimumSize() {
				return getPreferredSize();
			}
		};
		table.setDefaultRenderer(ImageIcon.class,
				new DefaultTableCellRenderer() {
					private static final long serialVersionUID = -2814071994069770065L;

					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						imageLabel.setIcon((Icon) value);
						return imageLabel;
					}
				});
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				if (row != -1) {
					changeSequenceGraphs(row, (e.getClickCount() == 2), true);
				}
			}
		});
		defaultTableRowHeight = table.getRowHeight();
		table.setRowHeight(IMAGE_HEIGHT);

		expModel = new ExpTableModel();
		expsTable = new JTable(expModel) {
			private static final long serialVersionUID = -6583506500821202846L;

			public TableCellRenderer getCellRenderer(int row, int column) {
				DefaultTableCellRenderer tcr = (DefaultTableCellRenderer) super
						.getCellRenderer(row, column);
				if (row != -1) {
					if (((String) this.getValueAt(row, 0)).equals(dataSet.get(
							currentPSAM).getExperiment()))
						tcr.setBackground(Color.green);
					else
						tcr.setBackground(Color.white);
				}
				return tcr;
			}
		};
		expsTable.setRowHeight(defaultTableRowHeight);

		JScrollPane psamScrollPane = new JScrollPane(table);
		psamScrollPane.setColumnHeaderView(table.getTableHeader());
		JScrollPane expsScrollPane = new JScrollPane(expsTable);
		expsScrollPane.setColumnHeaderView(expsTable.getTableHeader());
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				psamScrollPane, expsScrollPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(350);
		psamPanel.add(splitPane, BorderLayout.CENTER);

		// // Sequence Tab
		sequenceModel = new ListModel();
		sequenceList = new JList(sequenceModel);
		sequenceList.getInsets().set(4, 4, 4, 4);
		sequenceList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = -1762705277909440001L;

			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				return graphs.get((String) value);
			}
		});
		sequencePanel.add(new JScrollPane(sequenceList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		JPanel controlPanel = new JPanel(new BorderLayout());
		FormLayout layout = new FormLayout(
				"right:max(40dlu;pref), 3dlu, 70dlu, 7dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		builder.setDefaultDialogBorder();

		consensusModel = new ConsensusModel();
		psamList = new JComboBox(consensusModel);
		psamList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// highlight table in the right row
				currentPSAM = psamList.getSelectedIndex();
				if (currentPSAM >= 0)
					table.setRowSelectionInterval(currentPSAM, currentPSAM);
				else
					currentPSAM = 0;

				// display appropriate psam in sequence tab
				changeSequenceGraphs(currentPSAM, true, false);
			}
		});

		ButtonGroup directionGroup = new ButtonGroup();
		JRadioButton forwardButton = new JRadioButton("Forward");
		forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDirection(Direction.FORWARD);
			}
		});
		JRadioButton backwardsButton = new JRadioButton("Backward");
		backwardsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDirection(Direction.BACKWARD);
			}
		});
		JRadioButton bothButton = new JRadioButton("Both", true);
		bothButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setDirection(Direction.BOTH);
			}
		});
		directionGroup.add(forwardButton);
		directionGroup.add(backwardsButton);
		directionGroup.add(bothButton);
		final JFormattedTextField thresholdField = new JFormattedTextField(0.0);
		final JTextField searchField = new JTextField();
		JButton filterButton = new JButton("Filter");
		filterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = searchField.getText();
				if ((text == null) || (text.trim().length() == 0)) {
					filterSequence = null;
				} else {
					filterSequence = text.trim();
				}
				threshold = 0;
				try {
					threshold = Double.parseDouble(thresholdField.getText());
				} catch (NumberFormatException nfe) {
					// Ignore, use 0.
				}
				doFilter();
				sequenceModel.fireContentsChanged();
			}
		});
		JButton imageSnapshotButton = new JButton("Take Snapshot");
		imageSnapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createImageSnapshot();
			}
		});

		builder.appendSeparator("PSAM");
		builder.append("Choose PSAM", psamList);
		builder.appendSeparator("Direction");
		builder.append("", forwardButton);
		builder.append("", backwardsButton);
		builder.append("", bothButton);
		builder.appendSeparator("Filtering");
		builder.append("Threshold", thresholdField);
		builder.append("Sequence Name", searchField);
		builder.append("", filterButton);
		builder.appendSeparator("Image Snapshot");
		builder.append("", imageSnapshotButton);
		builder.append(Box.createVerticalGlue());
		psamLabel = new JLabel("");
		psamLabel.setBorder(new LineBorder(Color.black, 1));
		JPanel flowPanel = new JPanel(new FlowLayout());
		flowPanel.add(psamLabel);
		controlPanel.add(flowPanel, BorderLayout.NORTH);
		controlPanel.add(builder.getPanel(), BorderLayout.CENTER);
		sequencePanel.add(controlPanel, BorderLayout.WEST);

	}
	
	private static int MAX_PIXEL_MB = 100;

	@Publish
	public org.geworkbench.events.ImageSnapshotEvent createImageSnapshot() {
		org.geworkbench.events.ImageSnapshotEvent event = null;
		int w = sequenceList.getWidth();
		int h = sequenceList.getHeight();
		long size = w*h;
		if(size > MAX_PIXEL_MB*1024*1024) {
			JOptionPane.showMessageDialog(this.getComponent(),
			"Cannot create snapshot.\n"+ 
			"The requested snapshot is "+w+"X"+h+" pixels, or about "+size/1000000+" megapixels.\n"+ 
			"The upper limit is 100 megapixels.");
			return null;
		}
		try {
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			Graphics g = image.getGraphics();
			sequenceList.print(g);
			ImageIcon icon = new ImageIcon(image, "MatrixReduce");
			event = new org.geworkbench.events.ImageSnapshotEvent(
					"MatrixReduce Snapshot", icon,
					org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
		} catch (OutOfMemoryError e) {
			JOptionPane.showMessageDialog(this.getComponent(),
					"OutOfMemoryError when the image's size is "+w+"X"+h);
		}
		return event;
	}

	private void exportPSAMs(Set<DSPositionSpecificAffintyMatrix> psams) {
		// Pop up a file chooser
		String dir = LoadDataDialog.getLastDataDirectory();
		if (dir == null) {
			dir = ".";
		}
		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.showSaveDialog(getComponent());
		File file = chooser.getSelectedFile();
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
			for (DSPositionSpecificAffintyMatrix psam : psams) {
				MatrixReduceAnalysis.writePSAM(psam, out);
				out.println();
			}
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void updateGraphs() {
		if ((dataSet != null) && (selectedPSAM < dataSet.size())) {
			for (SequenceGraph graph : graphs.values()) {
				graph.createScores(dataSet.get(selectedPSAM), true);
				graph.createScores(dataSet.get(selectedPSAM), false);
			}
			psamLabel.setIcon(getPsamImage(dataSet.get(selectedPSAM).getScores()));
		}
	}

	private void setDirection(Direction direction) {
		showForward = true;
		showBackward = true;
		if (direction == Direction.FORWARD) {
			showBackward = false;
		} else if (direction == Direction.BACKWARD) {
			showForward = false;
		}
		doFilter();
		sequencePanel.repaint();
	}

	private void changeSequenceGraphs(int row, boolean changePSAM, boolean switchToSequenceTab) {
		currentPSAM = row;
		expModel.fireTableDataChanged();
		if (changePSAM) {
			selectedPSAM = row;
			updateGraphs();
			doFilter();
			sequenceModel.fireContentsChanged();
			psamList.setSelectedIndex(row);
			if(switchToSequenceTab) tabPane.setSelectedIndex(TAB_SEQUENCE);
		}
	}

	public boolean isShowForward() {
		return showForward;
	}

	public boolean isShowBackward() {
		return showBackward;
	}

	private void doFilter() {
		HashSet<String> seqFilter = new HashSet<String>();
		// 1) Filter by sequence
		if (filterSequence == null) {
			seqFilter.addAll(sequences.keySet());
		} else {
			filterSequence = filterSequence.toUpperCase();
			for (int i = 0; i < sequences.size(); i++) {
				String s = sequences.get(i);
				if (s.toUpperCase().contains(filterSequence)) {
					seqFilter.add(sequences.get(i));
				}
			}
			/*
			 * This code is for searching if a sequence is provided instead of a
			 * name... filterSequence = filterSequence.toUpperCase(); String
			 * reverseSequence = StringUtils.reverseString(filterSequence); for
			 * (int i = 0; i < sequences.size(); i++) { String s =
			 * sequences.get(sequences.get(i)); if (s.contains(filterSequence) ||
			 * s.contains(reverseSequence)) { seqFilter.add(sequences.get(i)); } }
			 */
		}
		// 2) Filter by threshold
		for (int i = 0; i < sequences.size(); i++) {
			String key = sequences.get(i);
			SequenceGraph graph = graphs.get(key);
			boolean passed = false;
			if (showForward) {
				if (graph.getBestPosScore() > threshold) {
					passed = true;
				}
			}
			if (showBackward) {
				if (graph.getBestNegScore() > threshold) {
					passed = true;
				}
			}
			if (!passed) {
				seqFilter.remove(key);
			}
		}
		// 3) Build ordered list of the result
		selectedSequences = new ArrayList<String>();
		for (int i = 0; i < sequences.size(); i++) {
			String key = sequences.get(i);
			if (seqFilter.contains(key)) {
				selectedSequences.add(key);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(ProjectEvent projectEvent, Object source) {
		DSDataSet<?> data = projectEvent.getDataSet();
		if (!(data instanceof DSMatrixReduceSet)) {
			return;
		}

		dataSet = ((DSMatrixReduceSet) data);
		model.fireTableStructureChanged();
		exps = dataSet.getMatrixReduceExperiments();
		if ((dataSet.size() > 0) && (exps != null) && (exps.size() > 0)) {
			currentPSAM = 0;
			table.setRowSelectionInterval(0, 0);
			expModel.fireTableStructureChanged();
		}
		sequences = dataSet.getSequences();
		int n = sequences.size();
		int maxSequenceLength = 1;
		for (String s : ((Collection<String>) sequences.values())) {
			if (s.length() > maxSequenceLength) {
				maxSequenceLength = s.length();
			}
		}
		// Unregister tooltips
		if (graphs != null) {
			for (SequenceGraph graph : graphs.values()) {
				if (graph != null) {
					ToolTipManager.sharedInstance().unregisterComponent(graph);
				}
			}
		} else {
			graphs = new HashMap<String, SequenceGraph>();
		}

		for (int i = 0; i < n; i++) {
			String label = sequences.get(i);
			String sequence = sequences.get(label);
			SequenceGraph graph = graphs.get(label);
			if(graph==null) {
				graph = new SequenceGraph(sequence, label,
						maxSequenceLength, this);
				graphs.put(label, graph);
			} else {
				graph.updateSequence(sequence, label,
						maxSequenceLength);
			}
			ToolTipManager.sharedInstance().registerComponent(graph);
		}
		selectedPSAM = 0;
		updateGraphs();
		doFilter();
		sequenceModel.fireContentsChanged();

		consensusSequences = new ArrayList<String>(dataSet.size());
		for (int i = 0; i < dataSet.size(); i++) {
			consensusSequences.add(dataSet.get(i).getConsensusSequence());
		}
		if (consensusSequences.size() > 0) {
			consensusModel.fireContentsChanged();
			psamList.setSelectedIndex(0);
		}
	}

	private static ImageIcon getPsamImage(double[][] scores){
		PSAMPlot psamPlot = new PSAMPlot(
				convertScoresToWeights(scores));
		psamPlot.setMaintainProportions(false);
		psamPlot.setAxisDensityScale(4);
		psamPlot.setAxisLabelScale(3);
		BufferedImage image = new BufferedImage(
				MatrixReduceViewer.IMAGE_WIDTH,
				MatrixReduceViewer.IMAGE_HEIGHT,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();
		psamPlot.layoutChart(MatrixReduceViewer.IMAGE_WIDTH,
				MatrixReduceViewer.IMAGE_HEIGHT, graphics
						.getFontRenderContext());
		psamPlot.paint(graphics);
		ImageIcon psamImage = new ImageIcon(image);
		return psamImage;
	}
	
	private static double[][] convertScoresToWeights(double[][] psamData) {
		double[][] psamddG = new double[psamData.length][4];
		for (int i = 0; i < psamData.length; i++) {
			double logMean = 0;
			for (int j = 0; j < 4; j++) {
				logMean += Math.log(psamData[i][j]);
			}
			logMean /= 4;
			for (int j = 0; j < 4; j++) {
				psamddG[i][j] = Math.log(psamData[i][j]) - logMean;
			}
		}
		return psamddG;
	}

}
