package org.geworkbench.components.ttest.data;

/* Fields complete. */
public class TTestOutput {
	public int[] significanceIndex; // [significanceCount]
	public double[] tValue; // [rowCount]
	public double[] pValue; // [rowCount]

	public double[] foldChange; // [rowCount]

	private int rowCount; // same as from input
	private int significanceCount;

	public TTestOutput(){
	}
	
	public TTestOutput(int rowCount, double[] tValue, double[] pValue,
			double[] foldChange, int significanceCount, int[] significanceIndex) {
		this.rowCount = rowCount;
		this.tValue = tValue;
		this.pValue = pValue;
		this.foldChange = foldChange;
		this.significanceCount = significanceCount;
		this.significanceIndex = significanceIndex;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("t-test result\n");
		sb.append(rowCount).append("\n");
		for(int i=0; i<rowCount; i++)sb.append(tValue[i]).append(" ");
		sb.append("\n");
		for(int i=0; i<rowCount; i++)sb.append(pValue[i]).append(" ");
		sb.append("\n");
		for(int i=0; i<rowCount; i++)sb.append(foldChange[i]).append(" ");
		sb.append("\n");
		sb.append(significanceCount).append("\n");
		for(int i=0; i<significanceCount; i++)sb.append(significanceIndex[i]).append(" ");
		return sb.toString();
	}

	public int[] getSignificanceIndex() {
		return significanceIndex;
	}

	public double[] gettValue() {
		return tValue;
	}

	public double[] getpValue() {
		return pValue;
	}

	public double[] getFoldChange() {
		return foldChange;
	}

	public int getRowCount() {
		return rowCount;
	}

	public int getSignificanceCount() {
		return significanceCount;
	}

}