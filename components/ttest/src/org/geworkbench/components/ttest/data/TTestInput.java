package org.geworkbench.components.ttest.data;

import org.geworkbench.components.ttest.SignificanceMethod;

/* Fields complete. */
public class TTestInput {
	private double[][] caseArray; // [rowCount][caseCount]
	private double[][] controlArray; // [rowCount][controlCount]

	private int rowCount;
	private int caseCount;
	private int controlCount;

	// options. please some of them are supposed to be ignored depending on
	// other ones
	// approximately in the order that affects the algorithm flow more
	private SignificanceMethod significanceMethod;
	private boolean byPermutation;
	private double alpha;
	private boolean useWelch; // only affect the calculation of DF. thus orthogonal to other parameters
	private boolean useAllCombinations;
	private int numberCombinations;

	private boolean isLogNormalized; // only affect folder change
	
	public TTestInput() {
	};
	
	public TTestInput(int rowCount, int caseCount, int controlCount, double[][] caseArray, double[][]controlArray,
			SignificanceMethod significanceMethod, double alpha, boolean byPermutation, boolean useWelch, boolean useAllCombinations, int numberCombinations,
			boolean isLogNormalized) {
		// data
		this.rowCount = rowCount;
		this.caseCount = caseCount;
		this.controlCount = controlCount;
		this.caseArray = caseArray;
		this.controlArray = controlArray;
		
		// options
		this.significanceMethod = significanceMethod;
		this.alpha = alpha;
		this.byPermutation = byPermutation;
		this.useWelch = useWelch;
		this.useAllCombinations = useAllCombinations;
		this.numberCombinations = numberCombinations;
		this.isLogNormalized = isLogNormalized;
	}
	
	public double[][] getCaseArray() {
		return caseArray;
	}

	public void setCaseArray(double[][] caseArray) {
		this.caseArray = caseArray;
	}

	public double[][] getControlArray() {
		return controlArray;
	}

	public void setControlArray(double[][] controlArray) {
		this.controlArray = controlArray;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getCaseCount() {
		return caseCount;
	}

	public void setCaseCount(int caseCount) {
		this.caseCount = caseCount;
	}

	public int getControlCount() {
		return controlCount;
	}

	public void setControlCount(int controlCount) {
		this.controlCount = controlCount;
	}

	public SignificanceMethod getSignificanceMethod() {
		return significanceMethod;
	}

	public void setSignificanceMethod(SignificanceMethod significanceMethod) {
		this.significanceMethod = significanceMethod;
	}

	public boolean isByPermutation() {
		return byPermutation;
	}

	public void setByPermutation(boolean byPermutation) {
		this.byPermutation = byPermutation;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public boolean isUseWelch() {
		return useWelch;
	}

	public void setUseWelch(boolean useWelch) {
		this.useWelch = useWelch;
	}

	public boolean isUseAllCombinations() {
		return useAllCombinations;
	}

	public void setUseAllCombinations(boolean useAllCombinations) {
		this.useAllCombinations = useAllCombinations;
	}

	public int getNumberCombinations() {
		return numberCombinations;
	}

	public void setNumberCombinations(int numberCombinations) {
		this.numberCombinations = numberCombinations;
	}

	public boolean isLogNormalized() {
		return isLogNormalized;
	}

	public void setLogNormalized(boolean isLogNormalized) {
		this.isLogNormalized = isLogNormalized;
	}
}