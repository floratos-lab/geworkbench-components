package org.geworkbench.components.idea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;

import weka.estimators.KernelEstimator;

/**
 * 
 * Null distribution procedure
 * 
 * @author Zheng
 * @version $Id$
 * 
 */
public class NullDistribution {

	final private static int BIN_COUNT = 100; 
	final private static int BIN_SIZE = 100; 
	final private static int NULL_DATA_COUNT = 100; 
	
	private static final double K_WEIGHT = 1;
	private static final double K_PRECISION = 0.0072;// the precision to which
														// numeric
	// values are given.
	// For example, if the precision is stated to be 0.1, the values in the
	// interval (0.25,0.35] are all treated as 0.3.

	private double incre = -1; // set in prepareBins()
	private double[] sortedCorr; // sorted mutual information. set in prepareBins()
	
	// a bin has MinP and MaxP of sortedCorr[]. set in prepareBins()
	private Bin[] bins = new Bin[BIN_COUNT]; 

	// FIXME this is only to support cancellation in a messy way
	private final IDEAAnalysis analysis; 

	public NullDistribution(final IDEAAnalysis analysis) {
		this.analysis = analysis;
	}

	/* the effect of this method is to modify the element of edgeIndex */
	/**
	 * 
	 * @return true if finished; false if cancelled.
	 * @throws MathException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public boolean calcNullDist(final DSMicroarraySet maSet,
			final HashSet<IdeaEdge> edgeIndex, double pvalue,
			final Phenotype phenotype) throws MathException, IOException,
			ClassNotFoundException {

		int columnCountOverall = maSet.size();
		int columnCountWithoutExclude = columnCountOverall
				- phenotype.getExcludedCount();
		Set<Integer> includePheno = new HashSet<Integer>();
		for (int i = 0; i < columnCountOverall; i++)
			includePheno.add(i);
		Set<Integer> excludePheno = phenotype.getExcludeList();
		includePheno.removeAll(excludePheno);

		// calculate MI for each edge in edgeIndex
		for (IdeaEdge ideaEdge : edgeIndex) {

			double[] x = maSet.getRow(ideaEdge.getMarker1());
			double[] y = maSet.getRow(ideaEdge.getMarker2());

			MutualInfo mutual = MutualInfo.getInstance(x.length);
			// save MI value to the edge
			ideaEdge.setMI(mutual.cacuMutualInfo(x, y));
			double deltaCorr = getDeltaCorr(ideaEdge, phenotype,
					columnCountWithoutExclude, maSet);
			ideaEdge.setDeltaCorr(deltaCorr);// save deltaCorr value to the edge
			if (analysis.stopAlgorithm) {
				analysis.stop();
				return false;
			}
		}

		Map<Double, IdeaEdge> MI_Edge = prepareBins(maSet, edgeIndex);
		if (MI_Edge==null) { // prepareBins cancelled
			return false;
		}

		IdeaEdge currentEdge = null;
		// compute 100X100X100 null delta Corrs
		for (int i = 0; i < BIN_COUNT; i++) {
			// set 100 bins, get MI positions in sortedCorr for each bin

			for (int j = 0; j < BIN_SIZE; j++) { 
				// each bin has 100 points of edge

				int t = bins[i].getMinP() + j;

				// FIXME when t is out of range, the previous currentEdge will be used
				// I doubt that is intentional
				if ((t < sortedCorr.length) && (t >= 0)) {
					currentEdge = MI_Edge.get(sortedCorr[t]);
				}

				if (currentEdge.getNullData() == null) {
					// the 100 null delta data for each edge
					double[] nullData = new double[NULL_DATA_COUNT];

					for (int k = 0; k < NULL_DATA_COUNT; k++) {
						Set<Integer> nullPhenoCols = getRandomNullPhenoNos(
								phenotype.getIncludedCount(),
								columnCountWithoutExclude);
						nullData[k] = getDeltaCorr(currentEdge, new Phenotype(
								nullPhenoCols), columnCountWithoutExclude,
								maSet);
					}
					// save null data to edge
					currentEdge.setNullData(nullData);
				}

				if (analysis.stopAlgorithm) {
					analysis.stop();
					return false;
				}

			} // end of the loop of all points in a bin
			System.out.println("bin " + i);

		} // end of the loop of all bins

		int binMin = 0;
		int binMax = BIN_COUNT - 1;
		int halfWindow = (int) (0.025 / incre) + 1;

		for (IdeaEdge anEdge : edgeIndex) {
			double t = anEdge.getMI() - sortedCorr[0];
			int baseBin = (int) (t / incre);
			if ((baseBin - halfWindow) <= 0) {
				binMin = 0;
				binMax = 2 * halfWindow;
			} else if (baseBin + halfWindow >= BIN_COUNT - 1) {
				binMax = BIN_COUNT - 1;
				binMin = binMax - 2 * halfWindow;
			} else {
				binMin = baseBin - halfWindow;
				binMax = baseBin + halfWindow;
			}
			if (binMin < 0)
				binMin = 0;
			if (binMax > BIN_COUNT)
				binMax = BIN_COUNT;

			// binsPoints have the points in sortedCorr for evaluating
			// significance of the anEdge
			Set<Integer> binsPoints = new TreeSet<Integer>();
			for (int i = binMin; i < binMax; i++) {// i < binMax + 1;
				int minP = bins[i].getMinP();
				int maxP = bins[i].getMaxP();
				for (int j = minP; j < maxP + 1; j++) {
					binsPoints.add(j);
				}

				if (analysis.stopAlgorithm) {
					analysis.stop();
					return false;
				}
			}

			List<Double> zNullI = new ArrayList<Double>();
			KernelEstimator kernel = new KernelEstimator(K_PRECISION);
			/* 0.0072 may be reasonable, not sure, the parameter impact a lot */
			for (Integer a : binsPoints) {
				if ((a < sortedCorr.length) && (a >= 0)) {
					double[] nullDeltaI = MI_Edge.get(sortedCorr[a])
							.getNullData();
					for (int i = 0; i < nullDeltaI.length; i++) {
						kernel.addValue(nullDeltaI[i], K_WEIGHT);
						if (anEdge.getDeltaCorr() * nullDeltaI[i] > 0) {
							zNullI.add(nullDeltaI[i]);
						}
						if (analysis.stopAlgorithm) {
							analysis.stop();
							return false;
						}
					}
				}
			}

			// compute norm delta correlation
			double normCorr = kernel.getProbability(anEdge.getDeltaCorr());
			anEdge.setFlags(normCorr, pvalue / edgeIndex.size());

			double[] values = new double[zNullI.size() * 2];
			StandardDeviation std = new StandardDeviation();
			for (int i = 0; i < zNullI.size(); i++) {
				values[i] = zNullI.get(i);
				values[i + zNullI.size()] = -values[i];
				if (analysis.stopAlgorithm) {
					analysis.stop();
					return false;
				}
			}
			double zDeltaCorr = anEdge.getDeltaCorr() / std.evaluate(values);
			anEdge.setzDeltaCorr(zDeltaCorr);

		}// end of edges loop

		return true;
	}

	private Map<Double, IdeaEdge> prepareBins(final DSMicroarraySet maSet,
			final HashSet<IdeaEdge> edgeIndex) {
		// setup MI value-->edge map
		Map<Double, IdeaEdge> MI_Edge = new HashMap<Double, IdeaEdge>();
		
		sortedCorr = new double[edgeIndex.size()];
		int index = 0;
		for (IdeaEdge ideaEdge : edgeIndex) {
			sortedCorr[index++] = ideaEdge.getMI();
			MI_Edge.put(ideaEdge.getMI(), ideaEdge);
		}
		Arrays.sort(sortedCorr); // check if edgeIndex is changed!
		incre = (sortedCorr[sortedCorr.length - 1] - sortedCorr[0]) / BIN_COUNT;
		
		// evenly separated points from min MI to max MI
		double[] samplePoints = new double[BIN_COUNT];
		double[] binPoints = new double[BIN_COUNT]; // the MI close to the samplePoint
		int[] binPositions = new int[BIN_COUNT]; // binPoint position in sortedCorr[]

		samplePoints[0] = sortedCorr[0];
		for (int i = 0; i < BIN_COUNT; i++) {
			samplePoints[i] = samplePoints[0] + i * incre;
		}

		int edgeSize = edgeIndex.size();
		for (int i = 0; i < BIN_COUNT; i++) {// prepare binPoint,which correspond to
										// sortedCorr closely near samplePoint
			binPoints[i] = samplePoints[i];
			boolean find = false;
			int j = 0;
			while (!find && j < edgeSize) {
				j++;
				if (j < sortedCorr.length) {
					if (sortedCorr[j - 1] <= binPoints[i]
							&& sortedCorr[j] > binPoints[i]) {
						binPoints[i] = sortedCorr[j - 1];
						binPositions[i] = j - 1;
						find = true;
					} else if (sortedCorr[j - 1] < binPoints[i]
							&& sortedCorr[j] >= binPoints[i]) {
						binPoints[i] = sortedCorr[j];
						binPositions[i] = j;
						find = true;
					}
				}
				if (analysis.stopAlgorithm) {
					analysis.stop();
					return null;
				}
			}// end of while

		}// end of for 100 loops

		final int HALF_SIZE = BIN_SIZE / 2;
		for (int i = 0; i < BIN_COUNT; i++) {
			int center = binPositions[i];
			int minP = center - HALF_SIZE + 1;
			int maxP = center + HALF_SIZE;
			// minP and maxP should be in the correct range
			if (minP < 0) {
				minP = 0;
			} else if (maxP > edgeSize - 1) {
				maxP = edgeSize - 1;
			}
			bins[i] = new Bin(minP, maxP);
			// each bin knows its start and end positions in sortedCorr array.
		}

		return MI_Edge;
	}

	private static class Bin {
		final int minP;
		final int maxP;

		public Bin(int minP, int maxP) {
			this.minP = minP;
			this.maxP = maxP;
		}

		public int getMinP() {
			return minP;
		}

		public int getMaxP() {
			return maxP;
		}
	}

	private static Random randomGenerator = new Random();

	private static Set<Integer> getRandomNullPhenoNos(int phenoSize,
			int columnCountWithoutExclude) {
		// the random columns generated may have columns of phenotype
		Set<Integer> nullPhenoNos = new HashSet<Integer>();
		while (nullPhenoNos.size() < phenoSize) {
			nullPhenoNos
					.add(randomGenerator.nextInt(columnCountWithoutExclude));
		}

		return nullPhenoNos;
	}

	private static double getDeltaCorr(final IdeaEdge anEdge,
			final Phenotype phenotype, int columnCountWithoutExclude,
			final DSMicroarraySet maSet) throws MathException {

		int smallerColumnCount = columnCountWithoutExclude
				- phenotype.getIncludedCount();
		int[] exceptPhenoCols = new int[smallerColumnCount];

		int j = 0;
		for (int i = 0; i < exceptPhenoCols.length; i++) {
			while ((phenotype.isIncluded(j) || (phenotype.isExcluded(j)))
					&& (j < columnCountWithoutExclude))
				j++;
			exceptPhenoCols[i] = j;
			j++;
		}
		double[] x = maSet.getRow(anEdge.getMarker1());
		double[] y = maSet.getRow(anEdge.getMarker2());
		double[] excPhenoG1 = new double[smallerColumnCount];
		double[] excPhenoG2 = new double[smallerColumnCount];
		for (int i = 0; i < smallerColumnCount; i++) {
			int col = exceptPhenoCols[i];

			excPhenoG1[i] = x[col];// expData[row1][col]
			excPhenoG2[i] = y[col];
		}
		MutualInfo removalMI = MutualInfo.getInstance(smallerColumnCount);
		double d = removalMI.cacuMutualInfo(excPhenoG1, excPhenoG2);
		return anEdge.getMI() - d;
	}

}
