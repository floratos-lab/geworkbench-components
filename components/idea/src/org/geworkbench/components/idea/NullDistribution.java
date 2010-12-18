package org.geworkbench.components.idea;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.geworkbench.bison.datastructure.bioobjects.IdeaEdge;

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

	private final int columnCount;
	private final double[][] expData;
	private ArrayList<IdeaEdge> edgeIndex = null;

	private boolean useExistNull;
	private String nullFileName;
	private double incre;
	private static final double K_WEIGHT = 1;
	private static final double K_PRECISION = 0.0072;// the precision to which numeric
												// values are given.
	// For example, if the precision is stated to be 0.1, the values in the
	// interval (0.25,0.35] are all treated as 0.3.

	private Map<Double, IdeaEdge> MI_Edge;
	private double[] sortedCorr;
	private double[] samplePoints; // evenly seperated points from min MI to max
									// MI
	private double[] binPoints; // the MI close to the samplePoint
	private int[] binPositions; // binPoint position in sortedCorr[]
	private ArrayList<Bin> bins; // a bin has MinP and MaxP of sortedCorr[],
									// there are 100 bins totally
	private final Phenotype phenotype;

	public NullDistribution(ArrayList<IdeaEdge> edgeIndex, final double[][] expData,
			Boolean useExistNull, String nullFileName,
			int columnCount, final Phenotype phenotype) {
		
		this.edgeIndex = edgeIndex;
		this.expData = expData;
		this.useExistNull = useExistNull;
		this.nullFileName = nullFileName;

		this.columnCount = columnCount;
		this.phenotype = phenotype;		
	}

	@SuppressWarnings("unchecked")
	public int calcNullDist() throws MathException, IOException,
			ClassNotFoundException {

		MI_Edge = new HashMap<Double, IdeaEdge>();
		sortedCorr = new double[edgeIndex.size()];
		samplePoints = new double[100]; // evenly seperated points from min MI
										// to max MI
		binPoints = new double[100]; // correspond to sortedCorr closely near
										// samplePoint
		binPositions = new int[100];
		bins = new ArrayList<Bin>();

		String dir = System.getProperty("user.dir");
		String nullOutputFile = dir + "\\data\\null.dat";
		if (useExistNull) {

			FileInputStream fileIn = new FileInputStream(nullFileName);
			ObjectInputStream in1 = new ObjectInputStream(fileIn);			
			
			edgeIndex = (ArrayList<IdeaEdge>) in1.readObject();
			System.out.println("nullFile exist.");		

			//prepareBins();
			in1.close();
			fileIn.close();

		}// end of read null data
		else {
			// calcu MI for each edge in edgeIndex
			for (IdeaEdge ideaEdge : edgeIndex) {
				int rowx = ideaEdge.getExpRowNoG1();
				int rowy = ideaEdge.getExpRowNoG2();
				double[] x = new double[columnCount];
				double[] y = new double[columnCount];

				for (int j = 0; j < columnCount; j++) {
					x[j] = expData[rowx][j];
					y[j] = expData[rowy][j];
				}
				MutualInfo mutual = MutualInfo.getInstance(x.length);
				// save MI value to the edge
				ideaEdge.setMI(mutual.cacuMutualInfo(x, y)); 
				double deltaCorr = getDeltaCorr(ideaEdge, expData, phenotype, columnCount);
				ideaEdge.setDeltaCorr(deltaCorr);// save
													// deltaCorr
													// value to
													// the edge
				// deltaCorr[i]=delta.getDeltaCorr();//deltaCorr[] can be
				// removed after test.
				//System.out.println("deltaMI of edge's delta corr is "
				//		+ ideaEdge.getDeltaCorr());
			}

			prepareBins();

			// compute 100X100X100 null delta Corrs
			for (int i = 0; i < 100; i++) {// set 100 bins, get MI positions in
											// sortedCorr for each bin

				for (int j = 0; j < 100; j++) { // each bin has 100 points of
												// edge
					IdeaEdge currentEdge = MI_Edge.get(sortedCorr[bins.get(i)
							.getMinP() + j]);
					if(currentEdge.getNullData()==null) {
						// the 100 null delta data for each edge
						double[] nullData = new double[100]; 

						for (int k = 0; k < 100; k++) {
							Set<Integer> nullPhenoCols = getRandomNullPhenoNos(
									phenotype.getIncludedCount(), columnCount);
							nullData[k] = getDeltaCorr(currentEdge, expData,
									new Phenotype(nullPhenoCols), columnCount);
						}
						// save null data to edge
						currentEdge.setNullData(nullData); 
					}
				}// end of each bin
				System.out.println("bin " + i);

			}// end of 100 bins			
		
			int binMin = 0;
			int binMax = 100 - 1;
			int halfWindow = (int) (0.025 / incre) + 1;
	
			for (IdeaEdge anEdge : edgeIndex) {
				int baseBin = (int) ((anEdge.getMI() - sortedCorr[0]) / incre);
				if ((baseBin - halfWindow) <= 0) {
					binMin = 0;
					binMax = 2 * halfWindow;
				} else if (baseBin + halfWindow >= 100 - 1) {
					binMax = 100 - 1;
					binMin = binMax - 2 * halfWindow;
				} else {
					binMin = baseBin - halfWindow;
					binMax = baseBin + halfWindow;
				}
	
				Set<Integer> binsPoints = new TreeSet<Integer>(); // binsPoints have
																	// the points in
																	// sortedCorr
																	// for
																	// evaluating
																	// significance
																	// of the anEdge
				for (int i = binMin; i < binMax + 1; i++) {
					for (int j = bins.get(i).getMinP(); j < bins.get(i).getMaxP() + 1; j++) {
						binsPoints.add(j);
					}
				}
	
				ArrayList<Double> zNullI = new ArrayList<Double>();
				KernelEstimator kernel = new KernelEstimator(K_PRECISION);
				/* 0.0072 may be reasonable, not sure, the parameter impact a lot */
				for (Integer a : binsPoints) {
					double[] nullDeltaI = MI_Edge.get(sortedCorr[a]).getNullData();
					for (int i = 0; i < nullDeltaI.length; i++) {
						kernel.addValue(nullDeltaI[i], K_WEIGHT);
						if (anEdge.getDeltaCorr() > 0 && nullDeltaI[i] > 0)
							zNullI.add(nullDeltaI[i]);
						else if (anEdge.getDeltaCorr() < 0 && nullDeltaI[i] < 0) {
							zNullI.add(nullDeltaI[i]);
						}
					}
				}
	
				double normCorr = kernel.getProbability(anEdge.getDeltaCorr());// compute
																				// norm
																				// delta
																				// correlation
				anEdge.setNormCorr(normCorr); // save it in edgeIndex
	
				double[] values = new double[zNullI.size() * 2];
				StandardDeviation std = new StandardDeviation();
				for (int i = 0; i < zNullI.size(); i++) {
					values[i] = zNullI.get(i);
					values[i + zNullI.size()] = -values[i];
				}
				double zDeltaCorr = anEdge.getDeltaCorr() / std.evaluate(values);
				anEdge.setzDeltaCorr(zDeltaCorr);
	
			}// end of edges loop
	
			
			
			FileOutputStream fileOut = new FileOutputStream(nullOutputFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
	
			out.writeObject(edgeIndex);
			out.close();
			fileOut.close();
		}// end of else, there is no pre null dat, so compute it
		
		return 1;
	}

	private void prepareBins() {
		// setup MI value-->edge map
		int index = 0;
		for (IdeaEdge ideaEdge : edgeIndex) {
			sortedCorr[index++] = ideaEdge.getMI();
			MI_Edge.put(ideaEdge.getMI(), ideaEdge);
		}
		Arrays.sort(sortedCorr); // check if edgeIndex is changed!
		// for(int i=0;i<edgeSize;i++)
		// System.out.println("get MI "+sortedCorr[i]+"-->"+MI_Edge.get(sortedCorr[i]).getGeneNo1()+" "+MI_Edge.get(sortedCorr[i]).getGeneNo2()+" "+MI_Edge.get(sortedCorr[i]).getExpRowNoG1()+" "+MI_Edge.get(sortedCorr[i]).getExpRowNoG2());
		incre = (sortedCorr[sortedCorr.length - 1] - sortedCorr[0]) / 100;
		samplePoints[0] = sortedCorr[0];
		for (int i = 0; i < 100; i++) {
			samplePoints[i] = samplePoints[0] + i * incre;
		}

		int edgeSize = edgeIndex.size();
		for (int i = 0; i < 100; i++) {// prepare binPoint,which correspond to
										// sortedCorr closely near samplePoint
			binPoints[i] = samplePoints[i];
			boolean find = false;
			int j = 0;
			while (!find && j < edgeSize) {
				j++;
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
			}// end of while

		}// end of for 100 loops

		for (int i = 0; i < 100; i++) {
			int center = binPositions[i];
			if ((binPositions[i] - 50 + 1) < 0) { // minP and maxP should be in
													// the correct range
				center = 50 - 1;
			} else if ((binPositions[i] + 50) > edgeSize - 1) {
				center = edgeSize - 50 - 1;
			}
			int minP = center - 50 + 1;
			int maxP = center + 50;
			Bin aBin = new Bin(minP, maxP);
			bins.add(aBin); // each bin knows its start and end positions in
							// sortedCorr array.
		}

	}

	public ArrayList<IdeaEdge> getEdgeIndex() {
		return edgeIndex;
	}

	public class Bin {
		int minP;
		int maxP;

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
	
	private static Set<Integer> getRandomNullPhenoNos(int phenoSize, int columnCount) {
		// the random columns generated may have columns of phenotype
		Set<Integer> nullPhenoNos = new HashSet<Integer>();
		while (nullPhenoNos.size() < phenoSize) {
			nullPhenoNos.add( randomGenerator.nextInt(columnCount) );
		}

		return nullPhenoNos;
	}

	// deltaCorr=anEdge.getMI()-removalMI.getMI()
	private static double getDeltaCorr(final IdeaEdge anEdge,
			final double[][] expData, final Phenotype phenotype, int columnCount) throws MathException {

		int smallerColumnCount = columnCount - phenotype.getIncludedCount();
		int[] exceptPhenoCols = new int[smallerColumnCount];

		int j = 0;
		for (int i = 0; i < exceptPhenoCols.length; i++) {
			while ( phenotype.isIncluded(j)
					&& (j < columnCount))
				j++;
			exceptPhenoCols[i] = j;
			j++;
		}
		double[] excPhenoG1 = new double[smallerColumnCount];
		double[] excPhenoG2 = new double[smallerColumnCount];
		int row1 = anEdge.getExpRowNoG1();
		int row2 = anEdge.getExpRowNoG2();
		for (int i = 0; i < smallerColumnCount; i++) {
			int col = exceptPhenoCols[i];
			excPhenoG1[i] = expData[row1][col];
			excPhenoG2[i] = expData[row2][col];
		}
		MutualInfo removalMI = MutualInfo.getInstance(excPhenoG1.length);
		double d = removalMI.cacuMutualInfo(excPhenoG1, excPhenoG2);
		return anEdge.getMI() - d;
	}

}// end of class

