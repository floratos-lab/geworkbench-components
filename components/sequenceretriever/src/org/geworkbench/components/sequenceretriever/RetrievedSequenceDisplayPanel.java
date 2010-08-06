package org.geworkbench.components.sequenceretriever;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.util.BrowserLauncher;
import org.geworkbench.util.patterns.PatternLocations;
import org.geworkbench.util.patterns.PatternSequenceDisplayUtil;

/**
 * Created by IntelliJ IDEA.
 * User: xiaoqing
 * Date: Sep 2, 2006
 * Time: 12:10:12 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * A new display specifically for all retrieved Sequences.
 * 
 * @version $Id$
 */
public final class RetrievedSequenceDisplayPanel extends JPanel {
	private static final long serialVersionUID = 2953271321774394841L;

	// constant
	private final int xOff = 60;
	private final int yOff = 20;
	private final int yStep = 14;
	private static final int ROW_HEIGHT = 60;
	
	private double scale = 1.0;

	private JTable table;
	private int selected = 0;

	private String displayInfo = "";

	private DSSequenceSet<DSSequence> sequenceDB = null;
	private HashMap<CSSequence, PatternSequenceDisplayUtil> sequencePatternmatches;
	private HashMap<String, RetrievedSequenceView> retrievedMap = new HashMap<String, RetrievedSequenceView>();
	private boolean lineView;
	private boolean singleSequenceView;

	private double yBasescale;
	private int xBaseCols;
	private int[] eachSeqStartRowNum;
	private double xBasescale;
	private int seqXclickPoint = 0;
	private DSSequence selectedSequence;
	private RetrievedSequencesPanel retrievedSequencesPanel;

	public RetrievedSequenceDisplayPanel() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		table = new JTable(new RetrievedSequenceTableModel());

		table.setDefaultRenderer(RetrievedSequenceView.class,
				new DefaultTableCellRenderer() {
					private static final long serialVersionUID = -902870291577015527L;

					public Component getTableCellRendererComponent(
							JTable table, Object value, boolean isSelected,
							boolean hasFocus, int row, int column) {
						if (value instanceof RetrievedSequenceView) {
							return (RetrievedSequenceView) value;
						}
						return new RetrievedSequenceView(null);
					}
				});
		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());
				if (row != -1) {
					selectedSequence = (DSSequence) sequenceDB.get(row);
					int column = table.columnAtPoint(e.getPoint());
					if (column == 2) {
						Object sequence = sequenceDB.get(row);

						double xT = RetrievedSequenceView.getCurrentLocation();
						if (xT < ((CSSequence) sequence).length()) {
							seqXclickPoint = new Integer((int) xT);
							retrievedSequencesPanel
									.setSelectedSequence(selectedSequence);
							retrievedSequencesPanel
									.updateDetailPanel(seqXclickPoint);
						}
					}
				}
				if (e.getClickCount() == 2) {
					row = table.rowAtPoint(e.getPoint());
					if (row != -1) {
						int column = table.columnAtPoint(e.getPoint());
						if (column == 1 && !sequenceDB.isDNA()) {
							Object sequence = sequenceDB.get(row);
							RetrievedSequenceView retrievedSequenceView = retrievedMap
									.get(sequence.toString());
							try {
								if (retrievedSequenceView.getUrl() != null) {
									BrowserLauncher
											.openURL(retrievedSequenceView
													.getUrl());
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						} else if (column == 2) {
							if (retrievedSequencesPanel != null) {
								retrievedSequencesPanel.flapToNewView(false);
							}
						}
					}
				}
			}
		});
		table.setRowHeight(ROW_HEIGHT);
		// setting the size of the table and its columns
		if (sequenceDB != null) {
			table.setPreferredScrollableViewportSize(new Dimension(600, 300));
			table.getColumnModel().getColumn(0).setPreferredWidth(15);
			table.getColumnModel().getColumn(1).setPreferredWidth(100);
			table.getColumnModel().getColumn(2).setPreferredWidth(400);
		}

		table.setRowHeight(RetrievedSequenceView.HEIGHT);
		JScrollPane scrollPane = new JScrollPane(table);
		this.setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

	}

	// called only from RetreivedSequencesPanel
	void setRetrievedMap(HashMap<String, RetrievedSequenceView> retrievedMap) {
		this.retrievedMap = retrievedMap;
	}

	// called only from RetreivedSequencesPanel.initPanelView
	void initialize(DSSequenceSet<DSSequence> seqDB, boolean isLineView) {
		sequenceDB = seqDB;
		lineView = isLineView;
		try {
			this.removeAll();
			this.jbInit();
			repaint();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// called only from RetreivedSequencesPanel
	void setMaxSeqLen(int maxSeqLen) {
		RetrievedSequenceView.setMaxSeqLen(maxSeqLen);
	}

	// called only from RetreivedSequencesPanel.initialize
	void setSelectedSequence(DSSequence selectedSequence) {
		this.selectedSequence = selectedSequence;
	}

	// called only from RetreivedSequencesPanel
	DSSequence getSelectedSequence() {
		return selectedSequence;
	}

	private int getSeqDx(int x) {
		return (int) ((double) (x - xOff) / scale);
	}

	/**
	 * Set up the coresponding parameters when mouse moves.
	 * 
	 * @param e
	 *            MouseEvent
	 */
	private void setMouseMoveParameters(MouseEvent e) {
		int y = e.getY();
		int x = e.getX();
		int mouseSelected = -1;
		int mouseMovePoint = -1;
		DSSequence mouseSelectedSequence;
		if (!lineView) {
			mouseSelected = getSeqIdInFullView(y);
			if (eachSeqStartRowNum != null
					&& mouseSelected < eachSeqStartRowNum.length) {
				mouseMovePoint = (int) ((int) ((y - yOff - 1 - ((double) eachSeqStartRowNum[mouseSelected])
						* yBasescale) / yBasescale)
						* xBaseCols + x / xBasescale - 5);
			}
		} else {
			if (!singleSequenceView) {
				mouseSelected = getSeqId(y);
				mouseMovePoint = getSeqDx(x);

			} else {

				mouseMovePoint = (int) ((int) ((y - yOff - 1) / yBasescale)
						* xBaseCols + x / xBasescale - 5);
			}
		}
		if (sequenceDB != null && selected < sequenceDB.size()) {
			mouseSelectedSequence = sequenceDB.getSequence(mouseSelected);
		} else {
			mouseSelectedSequence = null;
		}
		if (mouseSelectedSequence != null) {
			displayInfo = "For sequence " + mouseSelectedSequence.getLabel()
					+ ", total length: " + mouseSelectedSequence.length();
			if (sequencePatternmatches != null) {
				PatternSequenceDisplayUtil psu = sequencePatternmatches
						.get(mouseSelectedSequence);
				if (psu != null && psu.getTreeSet() != null) {
					displayInfo += ", pattern number: "
							+ psu.getTreeSet().size();
				}
			}
			if ((mouseMovePoint <= mouseSelectedSequence.length())
					&& (mouseMovePoint > 0)) {
				this.setToolTipText("" + mouseMovePoint);
				displayInfo += ". Current location: " + mouseMovePoint;
			}
		}
		{
			this.setToolTipText(null);
		}

	}

	/**
	 * getSeqIdInFullView
	 * 
	 * @param y
	 *            int
	 * @return int
	 */
	private int getSeqIdInFullView(int y) {
		double yBase = (y - yOff - 3) / yBasescale + 1;
		if (eachSeqStartRowNum != null) {
			for (int i = 0; i < eachSeqStartRowNum.length; i++) {
				if (eachSeqStartRowNum[i] > yBase) {
					return Math.max(0, i - 1);
				}

			}
			return Math.max(0, eachSeqStartRowNum.length - 1);
		}
		return 0;
	}

	private int getSeqId(int y) {
		return (y - yOff + 5) / yStep;
	}

	// called only from RetreivedSequencesPanel
	void this_mouseMoved(MouseEvent e) {
		setMouseMoveParameters(e);
		if (!lineView) {
			mouseOverFullView(e);
		} else {
			mouseOverLineView(e);

		}

	}

	// called only from RetreivedSequencesPanel.jbInit
	void setRetrievedSequencesPanel(
			RetrievedSequencesPanel retrievedSequencesPanel) {
		this.retrievedSequencesPanel = retrievedSequencesPanel;
	}

	private void mouseOverFullView(MouseEvent e)
			throws ArrayIndexOutOfBoundsException {
		if (sequenceDB == null) {
			return;
		}
		int x1 = e.getX();
		int y1 = e.getY();

		Font f = new Font("Courier New", Font.PLAIN, 11);
		Graphics g = this.getGraphics();
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		FontMetrics fm = g.getFontMetrics(f);
		if (sequenceDB.getSequence(selected) == null
				|| sequenceDB.getSequence(selected).getSequence() == null) {
			return;
		}
		String asc = sequenceDB.getSequence(selected).getSequence();
		Rectangle2D r2d = fm.getStringBounds(asc, g);
		double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
		double yscale = 1.3 * r2d.getHeight();
		int width = this.getWidth();
		int cols = (int) (width / xscale) - 8;
		int dis = (int) ((int) ((y1 - yOff - 1) / yscale) * cols + x1 / xscale - 5);
		if (sequenceDB.getSequence(selected) != null) {
			if (((y1 - yOff - 1) / yscale > 0) && (dis > 0)
					&& (dis <= sequenceDB.getSequence(selected).length())) {
				this.setToolTipText("" + dis);
			}
		}
	}

	private void mouseOverLineView(MouseEvent e)
			throws ArrayIndexOutOfBoundsException {
		int y = e.getY();
		int x = e.getX();
		// displayInfo = "";
		if (!singleSequenceView) {
			int seqid = getSeqId(y);

			if (sequenceDB == null) {
				return;
			}
			int off = this.getSeqDx(x);
			DSSequence sequence = sequenceDB.getSequence(seqid);
			if (sequence != null) {
				if ((off <= sequenceDB.getSequence(seqid).length())
						&& (off > 0)) {
					String texttip = getTipInfo(sequence, off);
					this.setToolTipText(texttip);
				}
			}
		} else {

			Font f = new Font("Courier New", Font.PLAIN, 11);
			Graphics g = this.getGraphics();
			((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			FontMetrics fm = g.getFontMetrics(f);
			if (sequenceDB.getSequence(selected) == null
					|| sequenceDB.getSequence(selected).getSequence() == null) {
				return;
			}
			DSSequence sequence = sequenceDB.getSequence(selected);
			String asc = sequence.getSequence();
			displayInfo = "Length of " + sequence.getLabel() + ": "
					+ sequence.length();
			Rectangle2D r2d = fm.getStringBounds(asc, g);
			double xscale = (r2d.getWidth() + 3) / (double) (asc.length());
			double yscale = 1.3 * r2d.getHeight();
			int width = this.getWidth();
			int cols = (int) (width / xscale) - 8;
			int dis = (int) ((int) ((y - yOff - 1) / yscale) * cols + x
					/ xscale - 5);
			if (sequenceDB.getSequence(selected) != null) {
				if (x >= 6 * xscale && x <= (cols + 6) * xscale
						&& ((y - yOff - 1) / yscale > 0) && (dis > 0)
						&& (dis <= sequenceDB.getSequence(selected).length())) {

					String texttip = getTipInfo(sequence, dis);
					this.setToolTipText(texttip);

					// this.setToolTipText("" + dis);
				}
				displayInfo += ". Current location: " + dis;
			}
		}

	}

	/**
	 * getTipInfo
	 * 
	 * @param sequence
	 *            DSSequence
	 * @param off
	 *            int
	 * @return String
	 */
	private String getTipInfo(DSSequence sequence, int off) {
		String tip = "" + off;
		if (sequencePatternmatches != null) {
			PatternSequenceDisplayUtil psd = sequencePatternmatches
					.get(sequence);
			if (psd != null) {
				TreeSet<PatternLocations> patternsPerSequence = psd
						.getTreeSet();
				if (patternsPerSequence != null
						&& patternsPerSequence.size() > 0) {
					for (PatternLocations pl : patternsPerSequence) {
						CSSeqRegistration reg = pl.getRegistration();
						if (reg != null && reg.x1 <= off && reg.x2 >= off) {
							int x1 = reg.x1 + 1;
							int x2 = reg.x2 + 1;
							if (pl.getPatternType().equals(
									PatternLocations.DEFAULTTYPE)) {
								tip = tip + " " + pl.getAscii() + "<" + x1
										+ "," + x2 + "> ";
							} else if (pl.getPatternType().equals(
									PatternLocations.TFTYPE)) {
								tip = tip + " " + pl.getAscii() + "<" + x1
										+ "," + x2 + "> ";
							}
						}
					}
				}
			}
		}

		return tip;
	}

	private class RetrievedSequenceTableModel extends AbstractTableModel {
		private static final long serialVersionUID = -6009654606398029902L;

		@Override
		public String getColumnName(int column) {
			switch (column) {
			case 0:
				return "Include";
			case 1:
				return "Name";
			case 2:
				return "Sequence Detail";
			default:
				return "Sequence Detail";
			}
		}

		@Override
		public int getRowCount() {
			if (sequenceDB == null) {
				return 0;
			} else {
				return sequenceDB.size();
			}
		}

		@Override
		public int getColumnCount() {
			if (sequenceDB == null) {
				return 0;
			} else {
				return 3;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object sequence = sequenceDB.get(rowIndex);
			RetrievedSequenceView retrievedSequenceView = retrievedMap
					.get(sequence.toString());
			if (retrievedSequenceView == null) {
				return null;
			}
			switch (columnIndex) {
			case 0:
				return retrievedSequenceView.isIncluded();
			case 1:
				if (sequenceDB.isDNA()) {
					return sequence.toString();
				} else {
					return "<html><<font  color=\"#0000FF\"><u>"
							+ sequence.toString() + "</u></font>";
				}
			case 2:
				return retrievedSequenceView;
			default:
				return sequence.toString();
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 2:
				return RetrievedSequenceView.class;
			default:
				return String.class;
			}
		}

		/*
		 * returns if the cell is editable; returns false for all cells in
		 * columns except the first column (check box)
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 0;
		}

		/*
		 * detect change in cell at (row, col); set cell to value; update the
		 * table
		 */
		@Override
		public void setValueAt(Object value, int row, int col) {
			Object sequence = sequenceDB.get(row);
			if (sequence != null) {
				RetrievedSequenceView retrievedSequenceView = retrievedMap
						.get(sequence.toString());
				if (value != null) {
					retrievedSequenceView.setIncluded(((Boolean) value)
							.booleanValue());
				}
			}
			fireTableCellUpdated(row, col);
		}

	}
}
