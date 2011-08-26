package org.geworkbench.components.cupid;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CupidTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5506400618680097318L;
	private Log log = LogFactory.getLog(CupidTableModel.class);

	final int COLUMN_COUNT = 10;

	public void setValues(Vector<String[]> data) {
		rowCount = data.size();
		refSeq = new String[rowCount];
		miRNA = new String[rowCount];
		siteStart = new int[rowCount];
		targetScanScore = new float[rowCount];
		mirandaScore = new float[rowCount];
		pitaScore = new float[rowCount];
		conservationScore = new float[rowCount];
		goldStandardClass = new int[rowCount];
		interactionProb = new float[rowCount];
		int i = 0;
		for (String[] row : data) {
			refSeq[i] = row[0];
			miRNA[i] = row[1];
			try {
				siteStart[i] = Integer.parseInt(row[2]);
				targetScanScore[i] = Float.parseFloat(row[3]);
				mirandaScore[i] = Float.parseFloat(row[4]);
				pitaScore[i] = Float.parseFloat(row[5]);
				conservationScore[i] = Float.parseFloat(row[6]);
				goldStandardClass[i] = Integer.parseInt(row[7]);
				interactionProb[i] = Float.parseFloat(row[8]);
			} catch (NumberFormatException e) {
				// let's just keep going if things go wrong
				e.printStackTrace();
			}
			i++;
		}
	}

	int rowCount = 0;
	String[] refSeq;
	String[] miRNA;
	int[] siteStart;
	float[] targetScanScore;
	float[] mirandaScore;
	float[] pitaScore;
	float[] conservationScore;
	int[] goldStandardClass;
	float[] interactionProb;

	@Override
	public int getRowCount() {
		return rowCount;
	}

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return refSeq[rowIndex];
		case 1:
			return miRNA[rowIndex];
		case 2:
			return siteStart[rowIndex];
		case 3:
			return targetScanScore[rowIndex];
		case 4:
			return mirandaScore[rowIndex];
		case 5:
			return pitaScore[rowIndex];
		case 6:
			return conservationScore[rowIndex];
		case 7:
			return goldStandardClass[rowIndex];
		case 8:
			return interactionProb[rowIndex];
		case 9:
			return 0;
		default:
			log.error("Wrong column number");
			return null;
		}
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	private static String[] columnNames = { "refSeq", "miRNA", "Site Start",
			"TargetScan Score", "MIRANDA Score", "PITA Score",
			"Conservation Score", "Gold Standard Class", "Overall Probablity",
			"NUMBER10" };

}
