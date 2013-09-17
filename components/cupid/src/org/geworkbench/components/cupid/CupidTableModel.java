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
		distanceFromStart = new float[rowCount];
		distanceFromEnd = new float[rowCount];
		targetScanScore = new float[rowCount];
		mirandaScore = new float[rowCount];
		pitaScore = new float[rowCount];
		conservationScore = new float[rowCount];
		goldStandardClass = new int[rowCount];
		interactionProb = new float[rowCount];
		int i = 0;
		for (String[] row : data) {
			refSeq[i] = row[1];
			miRNA[i] = row[2];
			try {
				distanceFromStart[i] = Float.parseFloat(row[3]);
				distanceFromEnd[i] = Float.parseFloat(row[4]);
				pitaScore[i] = Float.parseFloat(row[5]);
				mirandaScore[i] = Float.parseFloat(row[6]);
				targetScanScore[i] = Float.parseFloat(row[7]);
				conservationScore[i] = Float.parseFloat(row[8]);
				goldStandardClass[i] = Integer.parseInt(row[9]);
				interactionProb[i] = Float.parseFloat(row[0]);
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
	float[] distanceFromStart;
	float[] distanceFromEnd;
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
		case 1:
			return refSeq[rowIndex];
		case 2:
			return miRNA[rowIndex];
		case 3:
			return distanceFromStart[rowIndex];
		case 4:
			return distanceFromEnd[rowIndex];
		case 5:
			return pitaScore[rowIndex];
		case 6:
			return mirandaScore[rowIndex];
		case 7:
			return targetScanScore[rowIndex];
		case 8:
			return conservationScore[rowIndex];
		case 9:
			int g = goldStandardClass[rowIndex];
			if(g==1)
				return "Yes";
			else if(g==0)
				return "No";
			else
				return "";
		case 0:
			return interactionProb[rowIndex];
		default:
			log.error("Wrong column number");
			return null;
		}
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	private static String[] columnNames = { "Interaction Probablity", "refSeq", "miRNA", 
		"Distance From Start of UTR", "Distance From End of UTR",
		"PITA Score", "MIRANDA Score", 	"TargetScan Score", 
		"Conservation Score", "Gold Standard Class" 
		};

}
