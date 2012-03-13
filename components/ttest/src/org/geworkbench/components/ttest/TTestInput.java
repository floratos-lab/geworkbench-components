package org.geworkbench.components.ttest;

/* Fields complete. */
public class TTestInput {
	double[][] caseArray; // [rowCount][caseCount]
	double[][] controlArray; // [rowCount][controlCount]

	int rowCount;
	int caseCount;
	int controlCount;

	// options. please some of them are supposed to be ignored depending on
	// other ones
	// approximately in the order that affects the algorithm flow more
	SignificanceMethod significanceMethod;
	boolean byPermutation;
	double alpha;
	boolean useWelch; // only affect the calculation of DF. thus orthogonal to other parameters
	boolean useAllCombinations;
	int numberCombinations;

	boolean isLogNormalized; // only affect folder change
	
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
}