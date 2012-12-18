package org.geworkbench.components.ttest.data;

import org.geworkbench.components.ttest.SignificanceMethod;

/* Fields complete. */
public class TTestInput {
	public double[][] caseArray; // [rowCount][caseCount]
	public double[][] controlArray; // [rowCount][controlCount]

	public int rowCount;
	public int caseCount;
	public int controlCount;

	// options. please some of them are supposed to be ignored depending on
	// other ones
	// approximately in the order that affects the algorithm flow more
	public SignificanceMethod significanceMethod;
	public boolean byPermutation;
	public double alpha;
	public boolean useWelch; // only affect the calculation of DF. thus orthogonal to other parameters
	public boolean useAllCombinations;
	public int numberCombinations;

	public boolean isLogNormalized; // only affect folder change
	
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