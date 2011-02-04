/**
 * 
 */
package org.geworkbench.components.ttest;

import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.geworkbench.engine.skin.Skin;
import org.geworkbench.util.Combinations;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.QSort;

/**
 * Collection of computational methods, mostly static, used in t-test analysis.
 * 
 * @author zji
 *
 */
public class TTestUtil {
	private static final int GROUP_A = 1;
	private static final int GROUP_B = 2;
	private static final int NEITHER_GROUP = 3;

	private transient int iterationCounter;
	private transient int[] usedExptsArray;
	private transient int[] combArray;
	private transient double[][] uMatrix;

	static class ValueResult {
		Vector<Float> tValuesVector = null;
		Vector<Float> pValuesVector = null;
		
		ValueResult(Vector<Float> tValue, Vector<Float> pValue) {
			this.tValuesVector = tValue;
			this.pValuesVector = pValue;
		}
		
	}
	
	private int numGenes, numCombs, numExps, numberGroupA, numberGroupB;
	private boolean stopAlgorithm;
	private float[][] expMatrix;
	private int[] groupAssignments;
	private boolean useAllCombs;
	
	TTestUtil(int numGenes, int numCombs, boolean stopAlgorithm, float[][] expMatrix, int numExps, int numberGroupA, int numberGroupB,
			int[] groupAssignments, boolean useAllCombs) {
		this.numGenes = numGenes;
		this.numCombs = numCombs;
		this.stopAlgorithm = stopAlgorithm;
		this.expMatrix = expMatrix;
		this.numExps = numExps;
		this.numberGroupA = numberGroupA;
		this.numberGroupB = numberGroupB;
		this.groupAssignments = groupAssignments;
		this.useAllCombs = useAllCombs;
	}

	private float calculateTValue(int gene, float[][] inputMatrix) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = inputMatrix[gene][i];
		}

		float[] groupAValues = new float[numberGroupA];
		float[] groupBValues = new float[numberGroupB];

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < numExps; i++) {
			if (groupAssignments[i] == GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				groupACounter++;
			} else if (groupAssignments[i] == GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
				groupBCounter++;
			}
		}
		return TTestUtil.calculateTValue(groupAValues, groupBValues);
	}

	/* used in case of useAllCombs */
	private double[] getCurrentPermTValueForAllCombs(int permCounter,
			int iterationCounter, int numGroupBValues, int[] usedExptsArray,
			int[] combArray, ProgressBar pbTtest) {
		if (this.stopAlgorithm) {
			pbTtest.dispose();
			return null;
		}

		if (permCounter == iterationCounter)
			return null;

		int[] notInCombArray = new int[numGroupBValues];
		int notCombCounter = 0;

		for (int i = 0; i < usedExptsArray.length; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			if (!TTestUtil.belongsInArray(i, combArray)) {
				notInCombArray[notCombCounter] = i;
				notCombCounter++;
			}
		}

		int[] permutedExpts = new int[numExps];

		for (int i = 0; i < combArray.length; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			permutedExpts[usedExptsArray[i]] = usedExptsArray[combArray[i]];
		}
		for (int i = 0; i < notInCombArray.length; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			permutedExpts[usedExptsArray[combArray.length + i]] = usedExptsArray[notInCombArray[i]];
		}

		float[][] permutedMatrix = TTestUtil.getPermutedMatrix(expMatrix, permutedExpts);
		return getTwoClassUnpairedTValues(permutedMatrix);
	}

	private double[] getTwoClassUnpairedTValues(float[][] inputMatrix) {
		double[] tValsFromMatrix = new double[numGenes];
		for (int i = 0; i < numGenes; i++) {
			tValsFromMatrix[i] = calculateTValue(i, inputMatrix);
		}

		return tValsFromMatrix;
	}
	
	/* used if NOT useAllCombs */
	private double[] getCurrentPermTValueNotUseAllCombs(ProgressBar pbTtest) {
		if (this.stopAlgorithm) {
			pbTtest.dispose();
			return null;
		}
		int[] permutedExpts = new int[1];
		Vector<Integer> validExpts = new Vector<Integer>();

		for (int j = 0; j < groupAssignments.length; j++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			if (groupAssignments[j] != NEITHER_GROUP) {
				validExpts.add(new Integer(j));
			}
		}

		int[] validArray = new int[validExpts.size()];
		for (int j = 0; j < validArray.length; j++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			validArray[j] = ((Integer) (validExpts.get(j))).intValue();
		}

		permutedExpts = TTestUtil.getPermutedValues(numExps, validArray);
		float[][] permutedMatrix = TTestUtil.getPermutedMatrix(expMatrix, permutedExpts);

		return getTwoClassUnpairedTValues(permutedMatrix);
	}
	
	private boolean prepareForAllCombs(ProgressBar pbTtest) {
		uMatrix = null;
		Vector<Integer> usedExptsVector = new Vector<Integer>();

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] != NEITHER_GROUP) {
				usedExptsVector.add(new Integer(i));
			}
		}
		usedExptsArray = new int[usedExptsVector.size()];

		for (int i = 0; i < usedExptsArray.length; i++) {
			usedExptsArray[i] = usedExptsVector.get(i);
		}

		combArray = new int[numberGroupA];
		for (int i = 0; i < combArray.length; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return false;
			}
			combArray[i] = -1;
		}

		iterationCounter = 0;

		while (org.geworkbench.util.Combinations.enumerateCombinations(
				usedExptsArray.length, numberGroupA, combArray)) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return false;
			}
			iterationCounter++;
		}

		if (iterationCounter > numCombs) {

			String message ="Note - the \"all permutations\" option will result in "
				+iterationCounter
				+" permutations being run, which may take a long time. Do you wish to proceed?"; 
			Object[] options = { "Proceed", "Cancel" };
			int n = JOptionPane.showOptionDialog(Skin.getFrame(), message,
					"Log Transformation", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, // do not use a
					// custom Icon
					options, // the titles of buttons
					options[0]); // default button title
			if (n == 1) { // n==1 means canceled
				pbTtest.dispose();
				return false;
			}

			uMatrix = new double[numGenes][iterationCounter];
		}
		for (int i = 0; i < combArray.length; i++) {
			combArray[i] = -1;
		}
		return true;
	}

	ValueResult executeMaxT(ProgressBar pbTtest) {
		Vector<Float> tValuesVector = null;
		Vector<Float> pValuesVector = null;

		double[] origTValues = new double[numGenes];
		double[] absTValues = new double[numGenes];
		double[] descTValues = new double[numGenes];
		int[] descGeneIndices = new int[numGenes];
		double[] adjPValues = new double[numGenes];

		double[][] uMatrix = new double[numGenes][numCombs];

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			origTValues[i] = calculateTValue(i, expMatrix);
			absTValues[i] = Math.abs(origTValues[i]);
		}

		org.geworkbench.util.QSort sortDescTValues = new QSort(absTValues,
				org.geworkbench.util.QSort.DESCENDING);
		descTValues = sortDescTValues.getSortedDouble();
		descGeneIndices = sortDescTValues.getOrigIndx();

		if (!useAllCombs) {
			for (int i = 0; i < numCombs; i++) {
				double[] currentPermTValues = getCurrentPermTValueNotUseAllCombs(pbTtest);

				if (Double
						.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
					uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
				} else {
					uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
				}

				for (int j = numGenes - 2; j >= 0; j--) {
					if (this.stopAlgorithm) {
						pbTtest.dispose();
						return null;
					}
					if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
						uMatrix[j][i] = uMatrix[j + 1][i];
					} else {
						uMatrix[j][i] = Math.max(uMatrix[j + 1][i],
								currentPermTValues[descGeneIndices[j]]);
					}
				}

			}
		} else {
			if(!prepareForAllCombs(pbTtest))
				return null;
			
			if (uMatrix != null)
				uMatrix = this.uMatrix;

			int permCounter = 0;

			while (org.geworkbench.util.Combinations.enumerateCombinations(
					usedExptsArray.length, numberGroupA, combArray)) {

				double[] currentPermTValues = getCurrentPermTValueForAllCombs(
						permCounter, iterationCounter,
						numberGroupB, usedExptsArray, combArray, pbTtest);

				if (currentPermTValues == null)
					break;

				if (Double
						.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
					uMatrix[numGenes - 1][permCounter] = Double.NEGATIVE_INFINITY;
				} else {
					uMatrix[numGenes - 1][permCounter] = currentPermTValues[descGeneIndices[numGenes - 1]];
				}

				for (int j = numGenes - 2; j >= 0; j--) {
					if (this.stopAlgorithm) {
						pbTtest.dispose();
						return null;
					}
					if (Double.isNaN(currentPermTValues[descGeneIndices[j]])) {
						uMatrix[j][permCounter] = uMatrix[j + 1][permCounter];
					} else {
						uMatrix[j][permCounter] = Math.max(
								uMatrix[j + 1][permCounter],
								currentPermTValues[descGeneIndices[j]]);
					}
				}

				permCounter++;
			}
		}

		adjPValues = new double[numGenes];

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			int pCounter = 0;
			for (int j = 0; j < numCombs; j++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				if (Math.abs(uMatrix[i][j]) >= Math.abs(descTValues[i])) {
					pCounter++;
				}
			}
			adjPValues[descGeneIndices[i]] = (double) pCounter
					/ (double) numCombs;
		}

		int NaNPCounter = 0;
		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			if (Double.isNaN(origTValues[i])) {
				adjPValues[i] = Double.NaN;
				NaNPCounter++;
			}
		}
		for (int i = 1; i < numGenes - NaNPCounter; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			adjPValues[descGeneIndices[i]] = Math.max(
					adjPValues[descGeneIndices[i]],
					adjPValues[descGeneIndices[i - 1]]);
		}

		tValuesVector = new Vector<Float>();
		pValuesVector = new Vector<Float>();
		for(double t: origTValues) {
			tValuesVector.add((float)t);
		}
		for(double p: adjPValues) {
			pValuesVector.add((float)p);
		}
		
		return new ValueResult(tValuesVector, pValuesVector);
	}
	
	ValueResult executeMinP(ProgressBar pbTtest) {
		Vector<Float> tValuesVector = null;
		Vector<Float> pValuesVector = null;

		double[] origTValues = new double[numGenes];
		double[] rawPValues = new double[numGenes];
		double[] adjPValues = new double[numGenes];
		double[][] origTMatrix = new double[numGenes][numCombs];
		double[][] qMatrix = new double[numGenes + 1][numCombs];
		double[][] sortedTMatrix = new double[numGenes][numCombs];
		int[] sortedRawPValueIndices = new int[1];
		double[] sortedRawPValues = new double[1];

		for (int i = 0; i < numCombs; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			qMatrix[numGenes][i] = 1.0d;
		}

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			origTValues[i] = calculateTValue(i, expMatrix);
		}
		if (!useAllCombs) {
			for (int i = 0; i < numCombs; i++) {
				double[] currentPermTValues = getCurrentPermTValueNotUseAllCombs(pbTtest);

				for (int j = 0; j < numGenes; j++) {
					if (this.stopAlgorithm) {
						pbTtest.dispose();
						return null;
					}
					origTMatrix[j][i] = currentPermTValues[j];
				}
			}
		} else {
			int[] permutedExpts = new int[numExps];

			for (int i = 0; i < numExps; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				permutedExpts[i] = i;
			}

			if(!prepareForAllCombs(pbTtest))
				return null;
			
			if (uMatrix != null)
				origTMatrix = this.uMatrix;

			int permCounter = 0;

			while (Combinations.enumerateCombinations(usedExptsArray.length,
					numberGroupA, combArray)) {

				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				if (permCounter == iterationCounter)
					break;

				int[] notInCombArray = new int[numberGroupB];
				int notCombCounter = 0;

				for (int i = 0; i < usedExptsArray.length; i++) {
					if (!TTestUtil.belongsInArray(i, combArray)) {
						notInCombArray[notCombCounter] = i;
						notCombCounter++;
					}
				}

				for (int i = 0; i < combArray.length; i++) {
					permutedExpts[usedExptsArray[i]] = usedExptsArray[combArray[i]];
				}
				for (int i = 0; i < notInCombArray.length; i++) {
					permutedExpts[usedExptsArray[combArray.length + i]] = usedExptsArray[notInCombArray[i]];
				}

				float[][] permutedMatrix = TTestUtil.getPermutedMatrix(expMatrix,
						permutedExpts);
				double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
				for (int j = 0; j < numGenes; j++) {
					origTMatrix[j][permCounter] = currentPermTValues[j];
				}

				permCounter++;
			}
		}

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			double currentTValue = (double) calculateTValue(i, expMatrix);
			if (Double.isNaN(currentTValue)) {
				rawPValues[i] = Double.NaN;
			} else {
				int pCounter = 0;
				for (int j = 0; j < numCombs; j++) {
					if (origTMatrix[i][j] >= currentTValue) {
						pCounter++;
					}
				}
				rawPValues[i] = (double) pCounter / (double) numCombs;
			}
		}

		QSort sortRawPValues = new QSort(rawPValues, QSort.ASCENDING);
		sortedRawPValues = sortRawPValues.getSortedDouble();
		sortedRawPValueIndices = sortRawPValues.getOrigIndx();

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			for (int j = 0; j < numCombs; j++) {
				sortedTMatrix[i][j] = origTMatrix[sortedRawPValueIndices[i]][j];
			}
		}

		double[] sortedAdjPValues = new double[numGenes];
		double[][] pMatrix = new double[numGenes][numCombs];

		int currentGeneCounter = 0;
		for (int i = numGenes - 1; i >= 0; i--) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			double[] currentGeneTVals = new double[numCombs];
			for (int j = 0; j < numCombs; j++) {
				currentGeneTVals[j] = sortedTMatrix[i][j];
			}
			QSort sortCurrentGeneTVals = new org.geworkbench.util.QSort(
					currentGeneTVals, QSort.DESCENDING);
			double[] sortedCurrentGeneTVals = sortCurrentGeneTVals
					.getSortedDouble();
			int[] currentGeneTValsSortedIndices = sortCurrentGeneTVals
					.getOrigIndx();
			double[] currentGeneSortedPVals = TTestUtil.getPValsFromOrderStats(
					sortedCurrentGeneTVals, numCombs);
			for (int j = 0; j < pMatrix[i].length; j++) {
				pMatrix[i][j] = currentGeneSortedPVals[currentGeneTValsSortedIndices[j]];
			}
			for (int j = 0; j < qMatrix[i].length; j++) {
				qMatrix[i][j] = Math.min(qMatrix[i + 1][j], pMatrix[i][j]);
			}

			int adjPCounter = 0;

			for (int j = 0; j < qMatrix[i].length; j++) {
				if (qMatrix[i][j] <= sortedRawPValues[i]) {
					adjPCounter++;
				}
			}

			sortedAdjPValues[i] = (double) adjPCounter / (double) numCombs;
			currentGeneCounter++;
		}

		for (int i = 1; i < sortedAdjPValues.length; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			sortedAdjPValues[i] = Math.max(sortedAdjPValues[i - 1],
					sortedAdjPValues[i]);
		}

		for (int i = 0; i < sortedAdjPValues.length; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			adjPValues[i] = sortedAdjPValues[sortedRawPValueIndices[i]];
			if (Double.isNaN(rawPValues[i])) {
				adjPValues[i] = Double.NaN;
			}
		}

		tValuesVector = new Vector<Float>();
		pValuesVector = new Vector<Float>();
		for(double t: origTValues) {
			tValuesVector.add((float)t);
		}
		for(double p: adjPValues) {
			pValuesVector.add((float)p);
		}

		return new ValueResult(tValuesVector, pValuesVector);
	}
	
	static float[][] randomlyPermute(float[] gene, int[] groupedExpts,
			int groupALength, int groupBLength) {
		float[][] groupedValues = new float[2][];
		groupedValues[0] = new float[groupALength];
		groupedValues[1] = new float[groupBLength];
		if (groupALength > groupBLength) {
			groupedValues[0] = new float[groupBLength];
			groupedValues[1] = new float[groupALength];
		}

		Vector<Integer> groupedExptsVector = new Vector<Integer>();
		for (int i = 0; i < groupedExpts.length; i++) {
			groupedExptsVector.add(new Integer(groupedExpts[i]));
		}

		for (int i = 0; i < groupedValues[0].length; i++) {
			int randInt = (int) Math.round(Math.random()
					* (groupedExptsVector.size() - 1));
			int randIndex = (groupedExptsVector.remove(randInt)).intValue();
			groupedValues[0][i] = gene[randIndex];
		}

		for (int i = 0; i < groupedValues[1].length; i++) {
			int index = (groupedExptsVector.get(i)).intValue();
			groupedValues[1][i] = gene[index];
		}

		return groupedValues;
	}

	static int calculateDf(float[] groupA, float[] groupB,
			boolean useWelchDf) {
		int kA = 0;
		int kB = 0;
		for (int i = 0; i < groupA.length; i++) {
			if (!Float.isNaN(groupA[i])) {
				kA++;
			}
		}

		for (int i = 0; i < groupB.length; i++) {
			if (!Float.isNaN(groupB[i])) {
				kB++;
			}
		}

		if (!useWelchDf) {
			int df = kA + kB - 2;
			if (df < 0) {
				df = 0;
			}
			return df;
		}

		float varA = TTestUtil.getVar(groupA);
		float varB = TTestUtil.getVar(groupB);
		float numerator = (float) (Math.pow(((varA / kA) + (varB / kB)), 2));
		float denom = (float) ((Math.pow((varA / kA), 2) / (kA - 1)) + (Math
				.pow((varB / kB), 2) / (kB - 1)));
		int df = (int) Math.floor(numerator / denom);

		return df;
	}

	static double[] getPValsFromOrderStats(double[] sortedTVals,
			int numCombs) {
		double[] pVals = new double[sortedTVals.length];
		int[] ranksArray = new int[sortedTVals.length];

		if (Double.isNaN(sortedTVals[0])) {
			for (int i = 0; i < pVals.length; i++) {
				pVals[i] = Double.NaN;
			}
			return pVals;
		}

		Vector<Integer> ranksVector = new Vector<Integer>();
		Vector<Integer> ranksCounterVector = new Vector<Integer>();
		ranksVector.add(new Integer(1));
		ranksArray[0] = 1;

		for (int i = 1; i < sortedTVals.length; i++) {
			if (Double.isNaN(sortedTVals[i])) {
				ranksArray[i] = -1;
			} else {
				if (sortedTVals[i - 1] > sortedTVals[i]) {
					ranksArray[i] = ranksArray[i - 1] + 1;
					ranksVector.add(new Integer(ranksArray[i - 1] + 1));
				} else {
					ranksArray[i] = ranksArray[i - 1];
				}
			}
		}

		int currCounter = 0;

		for (int i = 0; i < ranksVector.size(); i++) {
			int currRank = ((Integer) (ranksVector.get(i))).intValue();
			int currRankCounter = 0;
			for (int j = currCounter; j < ranksArray.length; j++) {
				if (currRank == ranksArray[j]) {
					currRankCounter++;
					currCounter++;
				} else {
					ranksCounterVector.add(new Integer(currRankCounter));
					break;
				}
			}

			if (i == ranksVector.size() - 1) {
				ranksCounterVector.add(new Integer(currRankCounter));
			}
		}

		int[] numerators = new int[ranksArray.length];

		int currentNumerator = 0;
		int currentIndex = 0;
		for (int i = 0; i < ranksVector.size(); i++) {
			currentNumerator = currentNumerator
					+ ((Integer) (ranksCounterVector.get(i))).intValue();

			for (int j = currentIndex; j < currentNumerator; j++) {
				numerators[j] = currentNumerator;
				currentIndex++;
			}
		}
		for (int i = 0; i < numerators.length; i++) {
			if (Double.isNaN(sortedTVals[i])) {
				pVals[i] = Double.NaN;
			} else {
				pVals[i] = (double) numerators[i] / (double) numCombs;
			}
		}

		return pVals;
	}

	static float calculateTValue(float[] groupA, float[] groupB) {
		int kA = groupA.length;
		int kB = groupB.length;
		float meanA = getMean(groupA);
		float meanB = getMean(groupB);
		float varA = getVar(groupA, meanA);
		float varB = getVar(groupB, meanB);

		int numbValidGroupAValues = 0;
		int numbValidGroupBValues = 0;

		for (int i = 0; i < groupA.length; i++) {
			if (!Float.isNaN(groupA[i])) {
				numbValidGroupAValues++;
			}
		}

		for (int i = 0; i < groupB.length; i++) {
			if (!Float.isNaN(groupB[i])) {
				numbValidGroupBValues++;
			}
		}

		if ((numbValidGroupAValues < 2) || (numbValidGroupBValues < 2)) {
			return Float.NaN;
		}

		float tValue = (float) ((meanA - meanB) / Math.sqrt((varA / kA)
				+ (varB / kB)));

		return tValue;
	}

	static private float getMean(float[] group) {
		float sum = 0;
		int n = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sum = sum + group[i];
				n++;
			}
		}
		if (n == 0) {
			return Float.NaN;
		}
		float mean = sum / (float) n;

		if (Float.isInfinite(mean)) {
			return Float.NaN;
		}
		return mean;
	}

	static private float getVar(float[] group, float mean) {
		int n = 0;

		float sumSquares = 0;

		for (int i = 0; i < group.length; i++) {
			if (!Float.isNaN(group[i])) {
				sumSquares = (float) (sumSquares + Math.pow((group[i] - mean),
						2));
				n++;
			}
		}

		if (n < 2) {
			return Float.NaN;
		}

		float var = sumSquares / (float) (n - 1);
		if (Float.isInfinite(var)) {
			return Float.NaN;
		}
		return var;
	}

	// a version when the mean is not kept
	static float getVar(float[] group) {
		return getVar(group, getMean(group));
	}

	static int[] getPermutedValues(int arrayLength, int[] validArray) {
		int[] permutedValues = new int[arrayLength];
		for (int i = 0; i < permutedValues.length; i++) {
			permutedValues[i] = i;
		}

		int[] permutedValidArray = new int[validArray.length];
		for (int i = 0; i < validArray.length; i++) {
			permutedValidArray[i] = validArray[i];
		}

		for (int i = permutedValidArray.length; i > 1; i--) {
			Random generator2 = new Random();
			int randVal = generator2.nextInt(i - 1);
			int temp = permutedValidArray[randVal];
			permutedValidArray[randVal] = permutedValidArray[i - 1];
			permutedValidArray[i - 1] = temp;
		}

		for (int i = 0; i < validArray.length; i++) {
			permutedValues[validArray[i]] = permutedValidArray[i];
		}

		try {
			Thread.sleep(10);
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		return permutedValues;

	}

	static float[][] getPermutedMatrix(float[][] inputMatrix,
			int[] permExpts) {
		float[][] permutedMatrix = new float[inputMatrix.length][inputMatrix[0].length];
		for (int i = 0; i < inputMatrix.length; i++) {
			for (int j = 0; j < inputMatrix[0].length; j++) {
				permutedMatrix[i][j] = inputMatrix[i][permExpts[j]];
			}
		}
		return permutedMatrix;
	}

	static boolean belongsInArray(int i, int[] arr) {
		for (int j = 0; j < arr.length; j++) {
			if (i == arr[j]) {
				return true;
			}
		}
		return false;
	}

}
