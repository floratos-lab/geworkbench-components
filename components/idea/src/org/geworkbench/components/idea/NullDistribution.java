package org.geworkbench.components.idea;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
	static final int KERNEL_N = 100;
	private int[] tExp;
	private double[][] expData = null;
	private ArrayList<IdeaEdge> edgeIndex = null;
	private Phenotype pheno = null;
	private int edgeSize;
	private int headCol;
	private boolean useExistNull;
	private String nullFileName;
	private double incre;
	static final double K_WEIGHT = 1;
	static final double K_PRECISION = 0.0072;// the precision to which numeric
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

	public NullDistribution(ArrayList<IdeaEdge> edgeIndex,
			double[][] expData, Phenotype pheno, int headCol, Boolean useExistNull, String nullFileName) {// headCol is the
																// extra
																// column,i.e,head
																// columns of
																// exp file
		this.edgeIndex = edgeIndex;
		this.expData = expData;
		this.pheno = pheno;
		this.headCol = headCol;
		this.useExistNull=useExistNull;
		this.nullFileName=nullFileName;
		edgeSize = edgeIndex.size();
		tExp = pheno.getAllExpCols();
	}

	@SuppressWarnings("unchecked")
	public int calcNullDist() throws MathException, IOException, ClassNotFoundException {

		MI_Edge = new HashMap<Double, IdeaEdge>();
		sortedCorr = new double[edgeSize];
		samplePoints = new double[100]; // evenly seperated points from min MI
										// to max MI
		binPoints = new double[100]; // correspond to sortedCorr closely near
										// samplePoint
		binPositions = new int[100];
		bins = new ArrayList<Bin>();

		String dir = System.getProperty("user.dir");
		String nullString = dir+"\\data\\null.dat";
		if (useExistNull) {
		
				FileInputStream fileIn = new FileInputStream(nullFileName);
				ObjectInputStream in1 = new ObjectInputStream(fileIn);
				// System.out.println("before reading2...");
			
					ArrayList<IdeaEdge> readObject = (ArrayList<IdeaEdge>) in1
							.readObject();
					edgeIndex = readObject;
					System.out.println("nullFile exist.");

					prepareBins();

					in1.close();
					fileIn.close();

			
			
		}// end of read null data
		else {
			for (int i = 0; i < edgeSize; i++) { // calcu MI for each edge in
													// edgeIndex
				int rowx = edgeIndex.get(i).getExpRowNoG1();
				int rowy = edgeIndex.get(i).getExpRowNoG2();
				double[] x = new double[pheno.getAllExpCols().length];
				double[] y = new double[pheno.getAllExpCols().length];

				for (int j = 0; j < pheno.getAllExpCols().length; j++) {
					x[j] = expData[rowx][tExp[j] + headCol]; // the first 2
																// columns are
																// not real
																// data, so get
																// column 2 and
																// above
					y[j] = expData[rowy][tExp[j] + headCol];
				}
				MutualInfo mutual = MutualInfo.getInstance(x.length);
				edgeIndex.get(i).setMI(mutual.cacuMutualInfo(x, y)); // save MI value to the
														// edge
				double deltaCorr = getDeltaCorr(pheno, edgeIndex.get(i),
						expData);
				edgeIndex.get(i).setDeltaCorr(deltaCorr);// save
																	// deltaCorr
																	// value to
																	// the edge
				// deltaCorr[i]=delta.getDeltaCorr();//deltaCorr[] can be
				// removed after test.
				System.out.println("deltaMI of edge" + i + " is "
						+ edgeIndex.get(i).getDeltaCorr());
			}

			prepareBins();

			// compute 100X100X100 null delta Corrs
			for (int i = 0; i < 100; i++) {// set 100 bins, get MI positions in
											// sortedCorr for each bin

				for (int j = 0; j < 100; j++) { // each bin has 100 points of
												// edge
					IdeaEdge currentEdge = MI_Edge.get(sortedCorr[bins.get(i)
							.getMinP()+ j]);
					double[] nullData = new double[100]; // the 100 null delta
															// data for each
															// edge

					for (int k = 0; k < 100; k++) {// each edge has 100 random
													// null data
						int[] nullPhenoCols = getRandomNullPhenoNos(pheno, tExp);
						Phenotype nullPheno = new Phenotype(nullPhenoCols);
						nullData[k] = getDeltaCorr(nullPheno, currentEdge,
								expData);
					}

					currentEdge.setNullData(nullData); // save null data to edge
				}// end of each bin
				System.out.println("bin " + i);

			}// end of 100 bins

			try { // save the null date
				FileOutputStream fileOut = new FileOutputStream(nullString);
				ObjectOutputStream out = new ObjectOutputStream(fileOut);

				out.writeObject(edgeIndex);
				fileOut.close();

			} catch (IOException e) {
				System.out.println("error:" + e.getMessage());
			}
		}// end of else, there is no pre null dat, so compute it

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
			KernelEstimator kernel = new KernelEstimator(K_PRECISION);// 0.0072
																		// may
																		// be
																		// reasonable,
																		// not
																		// sure,
																		// the
																		// parameter
																		// impact
																		// a lot
			for (Integer a : binsPoints) {
				// System.out.println(a);
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

		for (IdeaEdge anEdge : edgeIndex) {

			if (anEdge.getNormCorr() < 0.05 / edgeSize) { // show significant
															// edges
				if (anEdge.getDeltaCorr() < 0)
					anEdge.setLoc(true);// save the flag for significant edge
				else if (anEdge.getDeltaCorr() > 0)
					anEdge.setGoc(true);
				// System.out.println("test in nullDistribution object\n");
				System.out.println(anEdge.getGeneNo1() + "\t"
						+ anEdge.getGeneNo2() + "\t"
						+ (anEdge.getExpRowNoG1() - 6) + "\t"
						+ (anEdge.getExpRowNoG2() - 6) + "\tDeltaCorr:\t"
						+ anEdge.getDeltaCorr() + "\tNormCorr:\t"
						+ anEdge.getNormCorr() + "\tzDeltaCorr:\t"
						+ anEdge.getzDeltaCorr() + "\tLOC:" + anEdge.isLoc()
						+ "\tGOC:" + anEdge.isGoc());
			}
		}

		return 1;
	}

	private void prepareBins() {
		for (int i = 0; i < edgeSize; i++) {
			sortedCorr[i] = edgeIndex.get(i).getMI();
			MI_Edge.put(edgeIndex.get(i).getMI(), edgeIndex.get(i)); // setup MI
																		// value-->edge
																		// map
		}
		Arrays.sort(sortedCorr); // zheng:check if edgeIndex is changed!
		// for(int i=0;i<edgeSize;i++)
		// System.out.println("get MI "+sortedCorr[i]+"-->"+MI_Edge.get(sortedCorr[i]).getGeneNo1()+" "+MI_Edge.get(sortedCorr[i]).getGeneNo2()+" "+MI_Edge.get(sortedCorr[i]).getExpRowNoG1()+" "+MI_Edge.get(sortedCorr[i]).getExpRowNoG2());
		incre = (sortedCorr[sortedCorr.length - 1] - sortedCorr[0]) / 100;
		samplePoints[0] = sortedCorr[0];
		for (int i = 0; i < 100; i++) {
			samplePoints[i] = samplePoints[0] + i * incre;
		}

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

	private	static int[] getRandomNullPhenoNos(Phenotype p, int[] t) {
		// the random columns generated may have columns of phenotype
			int phenoSize = p.getExpCols().length;
			int [] nullPhenoNos = new int[phenoSize];
			for (int i = 0; i < phenoSize; i++)
				nullPhenoNos[i] = t[t.length - 1]; // init nullPhenoNos to max
													// number, so the new random
													// data can bubble to the
													// top
			Random randomGenerator = new Random();
			int howManyNos = 0;
			while (howManyNos < phenoSize) {
				int randomInt = t[randomGenerator.nextInt(t.length)];
				Arrays.sort(nullPhenoNos);
				if (Arrays.binarySearch(nullPhenoNos, randomInt) < 0) { 
					/* found one, means randomInt is a new one in nullPhenoNos[] */
					nullPhenoNos[nullPhenoNos.length - 1] = randomInt;
					howManyNos++;
				}
			}
			Arrays.sort(nullPhenoNos);

			return nullPhenoNos;
	}

	// deltaCorr=anEdge.getMI()-removalMI.getMI()
	private double getDeltaCorr(Phenotype p, IdeaEdge anEdge, double[][] expData)
				throws MathException {

			int[] phenoCols = new int[p.getExpCols().length];
			for (int i = 0; i < phenoCols.length; i++)
				phenoCols[i] = p.getExpCols()[i];
			Arrays.sort(phenoCols);
			int[] exceptPhenoCols = new int[pheno.getAllExpCols().length
					- phenoCols.length];
			int[] t = pheno.getAllExpCols();

			int j = 0;
			for (int i = 0; i < exceptPhenoCols.length; i++) {
				while ((Arrays.binarySearch(phenoCols, t[j]) >= 0)
						&& (j < pheno.getAllExpCols().length))
					j++;
				exceptPhenoCols[i] = t[j];
				j++;
			}
			double[] excPhenoG1 = new double[exceptPhenoCols.length];
			double[] excPhenoG2 = new double[exceptPhenoCols.length];
			double[] allG1 = expData[anEdge.getExpRowNoG1()];
			double[] allG2 = expData[anEdge.getExpRowNoG2()];
			for (int i = 0; i < exceptPhenoCols.length; i++) {
				excPhenoG1[i] = allG1[exceptPhenoCols[i] + headCol];
				excPhenoG2[i] = allG2[exceptPhenoCols[i] + headCol];
			}
			MutualInfo removalMI = MutualInfo.getInstance(excPhenoG1.length);
			double d = removalMI.cacuMutualInfo(excPhenoG1, excPhenoG2);
			// anEdge.setRemovalCorr(d); //save removalCorr to the edge
			return anEdge.getMI() - d;
		}

}// end of class

