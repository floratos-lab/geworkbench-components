package org.geworkbench.components.ttest;

import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSTTestResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.skin.Skin;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.Combinations;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.QSort;

import JSci.maths.statistics.TDistribution;

/**
 * <p>geWorkbench</p>
 * <p>Description: Modular Application Framework for Gene Expression, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id$
 */

/**
 * Component to perform the T-Test Analysis.
 * <p>
 * <b><u>NOTE:</u></b> The code in this file is based on the T Test analysis
 * algorithm implementation developed by TIGR (The Institute for Genomic
 * Research), in the context of their TMEV project. In particular, we have
 * borrowed and modified for our purposes parts of the source file
 * <code>Ttest.java</code> located in the package:
 * <p>
 * <p>
 * &nbsp;&nbsp;&nbsp; org.tigr.microarray.mev.cluster.algorithm.impl
 */
public class TtestAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 1302806024752128407L;
	
	private static final int GROUP_A = 1;
	private static final int GROUP_B = 2;
	private static final int NEITHER_GROUP = 3;

	private int localAnalysisType;
	private static Log log = LogFactory.getLog(TtestAnalysis.class);

	private float[][] expMatrix;

	private int numGenes, numExps;
	private double alpha, criticalPValue;
	private int significanceMethod;
	private boolean isPermut, useWelchDf;
	private int[] groupAssignments;
	private int numCombs;
	private boolean useAllCombs;

	private boolean isLogNormalized = false;

	private double currentP = 0.0f;
	private double currentT = 0.0f;

	private Vector<Float> tValuesVector = new Vector<Float>();
	private Vector<Float> pValuesVector = new Vector<Float>();

	private int numberGroupA = 0;
	private int numberGroupB = 0;

	public TtestAnalysis() {
		localAnalysisType = AbstractAnalysis.TTEST_TYPE;
		setDefaultPanel(new TtestAnalysisPanel());
	}

	/* This constructor is necessary for MasterRegulatorAnalysis. */
	public TtestAnalysis(TtestAnalysisPanel panel) {
		localAnalysisType = AbstractAnalysis.TTEST_TYPE;
		setDefaultPanel(panel);
	}

	private void reset() {
		if (tValuesVector != null)
		   tValuesVector.clear();
		else
			tValuesVector = new Vector<Float>();
		if (pValuesVector != null)
		   pValuesVector.clear();
		else
			pValuesVector = new Vector<Float>();

		currentP = currentT = 0.0d;
		alpha = 0d;
		criticalPValue = 0d;
	}

	public int getAnalysisType() {
		return localAnalysisType;
	}

	@Override
	public AlgorithmExecutionResults execute(Object input) {
		return calculate(input, false);
	}

	@SuppressWarnings("unchecked")
	AlgorithmExecutionResults calculate(Object input,
			boolean calledFromOtherComponent) {
		reset();
		if (input == null || !(input instanceof DSMicroarraySetView)) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}

		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray>) input;
		boolean allArrays = !data.useItemPanel();
		log.info("All arrays: " + allArrays);

		numGenes = data.markers().size();
		numExps = data.items().size();

		ProgressBar pbTtest = ProgressBar
				.create(ProgressBar.INDETERMINATE_TYPE);
		pbTtest.addObserver(this);
		pbTtest.setTitle("T Test Analysis");
		pbTtest.setBounds(new ProgressBar.IncrementModel(0, numGenes, 0,
				numGenes, 1));

		pbTtest.setMessage("Calculating TTest, please wait...");
		pbTtest.start();
		this.stopAlgorithm = false;

		groupAssignments = new int[numExps];

		DSDataSet<? extends DSBioObject> set = data.getDataSet();

		if (!(set instanceof DSMicroarraySet)) {
			pbTtest.dispose();
			return null;
		}

		DSMicroarraySet<DSMicroarray> maSet = (DSMicroarraySet<DSMicroarray>) set;
		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		numberGroupA = 0;
		numberGroupB = 0;
		for (int i = 0; i < numExps; i++) {
			DSMicroarray ma = data.items().get(i);
			String[] labels = context.getLabelsForItem(ma);
			if ((labels.length == 0) && allArrays) {
				groupAssignments[i] = GROUP_B;
				numberGroupB++;
			}
			for (String label : labels) {
				if (context.isLabelActive(label) || allArrays) {
					String v = context.getClassForLabel(label);
					if (v.equals(CSAnnotationContext.CLASS_CASE)) {
						groupAssignments[i] = GROUP_A;
						numberGroupA++;
					} else if (v.equals(CSAnnotationContext.CLASS_CONTROL)) {
						groupAssignments[i] = GROUP_B;
						numberGroupB++;
					} else {
						groupAssignments[i] = NEITHER_GROUP;
					}
				}
			}
		}
		if (numberGroupA == 0 && numberGroupB == 0) {
			pbTtest.dispose();
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".",
					null);
		}
		if (numberGroupA == 0) {
			pbTtest.dispose();
			return new AlgorithmExecutionResults(false,
					"Please activate at least one set of arrays for \"case\".",
					null);
		}
		if (numberGroupB == 0) {
			pbTtest.dispose();
			return new AlgorithmExecutionResults(
					false,
					"Please activate at least one set of arrays for \"control\".",
					null);
		}

		expMatrix = new float[numGenes][numExps];

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			for (int j = 0; j < numExps; j++) {
				expMatrix[i][j] = (float) data.getValue(i, j);
			}
		}

		// ///////////////////////////////////////////////////////

		isLogNormalized = ((TtestAnalysisPanel) aspp).isLogNormalized();

		alpha = ((TtestAnalysisPanel) aspp).getAlpha();
		criticalPValue = ((TtestAnalysisPanel) aspp).getAlpha();
		significanceMethod = ((TtestAnalysisPanel) aspp)
				.getSignificanceMethod();
		isPermut = ((TtestAnalysisPanel) aspp).isPermut();
		useWelchDf = ((TtestAnalysisPanel) aspp).useWelchDf();
		numCombs = ((TtestAnalysisPanel) aspp).getNumCombs();
		useAllCombs = ((TtestAnalysisPanel) aspp).useAllCombs();

		String[][] labels = new String[2][];
		labels[0] = context.getLabelsForClass(CSAnnotationContext.CLASS_CASE);
		labels[1] = context
				.getLabelsForClass(CSAnnotationContext.CLASS_CONTROL);
		HashSet<String> caseSet = new HashSet<String>();
		HashSet<String> controlSet = new HashSet<String>();

		String groupAndChipsString = "";

		// case
		String[] classLabels = labels[0];
		groupAndChipsString += "\t case group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) || !data.useItemPanel()) {
				caseSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		// control
		classLabels = labels[1];
		groupAndChipsString += "\t control group(s): \n";
		for (int i = 0; i < classLabels.length; i++) {
			String label = classLabels[i];
			if (context.isLabelActive(label) || !data.useItemPanel()) {
				controlSet.add(label);
				groupAndChipsString += GenerateGroupAndChipsString(context
						.getItemsWithLabel(label));
			}
		}

		int totalSelectedGroup = caseSet.size() + controlSet.size();
		String histHeader = null;
		String histMarkerString = GenerateMarkerString(data);

		groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
				+ groupAndChipsString;

		if (significanceMethod == TtestAnalysisPanel.MIN_P
				|| significanceMethod == TtestAnalysisPanel.MAX_T) {
			if (significanceMethod == TtestAnalysisPanel.MIN_P) {
				executeMinP(pbTtest);
				if (null == pValuesVector) {
					pbTtest.dispose();
					return null;
				}
			} else if (significanceMethod == TtestAnalysisPanel.MAX_T) {
				executeMaxT(pbTtest);
				if (null == pValuesVector) {
					pbTtest.dispose();
					return null;
				}
			}
			DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
					maSet, "T-Test", caseSet.toArray(new String[0]), controlSet
							.toArray(new String[0]), criticalPValue

			);

			for (int i = 0; i < pValuesVector.size(); i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				sigSet.setSignificance(data.markers().get(i),
						pValuesVector.get(i));
				sigSet.setTValue(data.markers().get(i), tValuesVector.get(i));
			}
			sigSet.sortMarkersBySignificance();

			setFoldChnage(maSet, sigSet);
			// add data set history.
			histHeader = GenerateHistoryHeader();
			ProjectPanel.addToHistory(sigSet, histHeader + groupAndChipsString
					+ histMarkerString);
			pbTtest.dispose();

			return new AlgorithmExecutionResults(true, "Ttest", sigSet);
		}

		Vector<Integer> clusterVector = new Vector<Integer>();
		if (isPermut) {
			clusterVector = sortGenesByPermutationSignificance(pbTtest);
			if (null == clusterVector) {
				pbTtest.dispose();
				return null;
			}
		} else {
			clusterVector = sortGenesBySignificance(pbTtest);
			if (null == clusterVector) {
				pbTtest.dispose();
				return null;
			}
		}

		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
				maSet, "T-Test", caseSet.toArray(new String[0]), controlSet
						.toArray(new String[0]), criticalPValue

		);

		StringBuilder sb = new StringBuilder(
				"DSSignificanceResultSet:\nmarker\tt-value\t\tp-value\n");
		for (int i = 0; i < pValuesVector.size(); i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}

			DSGeneMarker m = data.markers().get(i);
			sigSet.setMarker(m, pValuesVector.get(i));
			sigSet.setTValue(m, tValuesVector.get(i));

			sb.append(m.getShortName());
			sb.append("\t");
			sb.append(sigSet.getTValue(m));
			sb.append("\t");
			sb.append(sigSet.getSignificance(m));
			sb.append("\n");
		}

		DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
				"Significant Genes");

		for (Integer index : clusterVector) {
			DSGeneMarker item = data.markers().get(index);
			panelSignificant.add(item, new Float(pValuesVector.get(index)));
			sigSet.addSigGenToPanel(item);
		}

		if (!calledFromOtherComponent) {
			publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
					DSGeneMarker.class, panelSignificant,
					SubpanelChangedEvent.NEW));
		}

		sigSet.sortMarkersBySignificance();

		sb.trimToSize();
		log.debug(sb.toString());

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Ttest", sigSet);
		setFoldChnage(maSet, sigSet);
		// add data set history.
		histHeader = GenerateHistoryHeader();
		if (!calledFromOtherComponent) {
			ProjectPanel.addToHistory(sigSet, histHeader + groupAndChipsString
					+ histMarkerString);
		}

		pbTtest.dispose();
		if (this.stopAlgorithm) {
			return null;
		}

		return results;
	} // end of method calculate

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}

	private Vector<Integer> sortGenesBySignificance(ProgressBar pbTtest) {
		Vector<Integer> sigGenes = new Vector<Integer>();

		if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
				|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
			sigGenes = new Vector<Integer>();
			for (int i = 0; i < numGenes; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				if (isSignificant(i)) {
					sigGenes.add(new Integer(i));
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				} else {
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				}
			}

		} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
			sigGenes = new Vector<Integer>();
			float[] tValues = new float[numGenes];
			for (int i = 0; i < numGenes; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				tValues[i] = Math.abs(calculateTValue(i, expMatrix));
			}

			QSort sortTValues = new QSort(tValues);
			float[] sortedTValues = sortTValues.getSorted();
			int[] sortedUniqueIDs = sortTValues.getOrigIndx();

			double adjAlpha = alpha;
			int denomAlpha = numGenes;
			int dF = 0;
			double prob = Double.POSITIVE_INFINITY;

			double[] tValuesArray = new double[numGenes];
			double[] pValuesArray = new double[numGenes];

			for (int i = (sortedTValues.length - 1); i > 0; i--) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				dF = getDF(sortedUniqueIDs[i]);
				if ((Float.isNaN(sortedTValues[i]))
						|| (Float.isNaN((new Integer(dF)).floatValue()))
						|| (dF <= 0)) {
					tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
					pValuesArray[sortedUniqueIDs[i]] = Float.NaN;
				} else {
					TDistribution tDist = new TDistribution(dF);
					double cumulP = tDist.cumulative(sortedTValues[i]);
					prob = 2 * (1 - cumulP);
					if (prob > 1) {
						prob = 1;
					}
					adjAlpha = alpha / (double) denomAlpha;
					if (prob <= adjAlpha) {
						sigGenes.add(new Integer(sortedUniqueIDs[i]));
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					} else {
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					}
					if (sortedTValues[i] > sortedTValues[i - 1]) {
						denomAlpha--;
						if (denomAlpha < 1) {
							log.warn("Warning: denomAlpha = " + denomAlpha);
						}
					}
				}
			}
			dF = getDF(sortedUniqueIDs[0]);
			if ((Float.isNaN(sortedTValues[0]))
					|| (Float.isNaN((new Integer(dF)).floatValue()))
					|| (dF <= 0)) {
				tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
				pValuesArray[sortedUniqueIDs[0]] = Float.NaN;
			} else {
				TDistribution tDist = new TDistribution(dF);
				double cumulP = tDist.cumulative(sortedTValues[0]);
				prob = 2 * (1 - cumulP);
				if (prob > 1) {
					prob = 1;
				}
				adjAlpha = alpha / (double) denomAlpha;
				if (prob <= adjAlpha) {
					sigGenes.add(new Integer(sortedUniqueIDs[0]));
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				} else {
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				}
			}
			tValuesVector = new Vector<Float>();
			pValuesVector = new Vector<Float>();

			for (int i = 0; i < tValuesArray.length; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				tValuesVector.add(new Float(tValuesArray[i]));
				pValuesVector.add(new Float(pValuesArray[i]));
			}

			criticalPValue = adjAlpha;
		}

		return sigGenes;
	}

	private Vector<Integer> sortGenesByPermutationSignificance(
			ProgressBar pbTtest) {
		Vector<Integer> sigGenes = new Vector<Integer>();

		if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
				|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
			sigGenes = new Vector<Integer>();
			for (int i = 0; i < numGenes; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				if (isSignificantByPermutation(i)) {
					sigGenes.add(new Integer(i));
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				} else {
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				}
			}
		} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
			sigGenes = new Vector<Integer>();
			float[] tValues = new float[numGenes];
			for (int i = 0; i < numGenes; i++) {
				tValues[i] = Math.abs(calculateTValue(i, expMatrix));
			}

			QSort sortTValues = new QSort(tValues);
			float[] sortedTValues = sortTValues.getSorted();
			int[] sortedUniqueIDs = sortTValues.getOrigIndx();

			double adjAlpha = alpha;
			int denomAlpha = numGenes;
			double prob = Double.POSITIVE_INFINITY;

			double[] tValuesArray = new double[numGenes];
			double[] pValuesArray = new double[numGenes];

			for (int i = (sortedTValues.length - 1); i > 0; i--) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				if (Float.isNaN(sortedTValues[i])) {
					tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
					pValuesArray[sortedUniqueIDs[i]] = Float.NaN;
				} else {
					prob = getPermutedProb(sortedUniqueIDs[i]);
					adjAlpha = alpha / (double) denomAlpha;
					if (prob <= adjAlpha) {
						sigGenes.add(new Integer(sortedUniqueIDs[i]));
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					} else {
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					}

					if (sortedTValues[i] > sortedTValues[i - 1]) {
						denomAlpha--;
						if (denomAlpha < 1) {
							log.warn("Warning: denomAlpha = " + denomAlpha);
						}
					}
				}
			}

			if (Float.isNaN(sortedTValues[0])) {
				tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
				pValuesArray[sortedUniqueIDs[0]] = Float.NaN;
			} else {
				prob = getPermutedProb(sortedUniqueIDs[0]);
				adjAlpha = alpha / (double) denomAlpha;
				if (prob <= adjAlpha) {
					sigGenes.add(new Integer(sortedUniqueIDs[0]));
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				} else {
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				}
			}
			tValuesVector = new Vector<Float>();
			pValuesVector = new Vector<Float>();

			for (int i = 0; i < tValuesArray.length; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				tValuesVector.add(new Float(tValuesArray[i]));
				pValuesVector.add(new Float(pValuesArray[i]));
			}
		}

		return sigGenes;
	}

	private double getPermutedProb(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int[] groupedExpts = new int[numberGroupA + numberGroupB];

		int groupedExptsCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == GROUP_A) {
				groupedExpts[groupedExptsCounter] = i;
				groupedExptsCounter++;
			} else if (groupAssignments[i] == GROUP_B) {
				groupedExpts[groupedExptsCounter] = i;
				groupedExptsCounter++;
			}
		}

		float tValue = calculateTValue(gene, expMatrix);
		currentT = tValue;
		double permutProb;
		permutProb = 0;
		if (useAllCombs) {
			int numCombsCounter = 0;
			int[] combArray = new int[numberGroupA];
			for (int i = 0; i < combArray.length; i++) {
				combArray[i] = -1;
			}
			while (Combinations.enumerateCombinations(groupedExpts.length,
					numberGroupA, combArray)) {
				float[] resampGroupA = new float[numberGroupA];
				float[] resampGroupB = new float[numberGroupA];
				int[] notInCombArray = new int[numberGroupA];
				int notCombCounter = 0;
				for (int i = 0; i < groupedExpts.length; i++) {
					if (!belongsInArray(i, combArray)) {
						notInCombArray[notCombCounter] = i;
						notCombCounter++;
					}
				}
				for (int i = 0; i < combArray.length; i++) {
					resampGroupA[i] = geneValues[groupedExpts[combArray[i]]];
				}
				for (int i = 0; i < notInCombArray.length; i++) {
					resampGroupB[i] = geneValues[groupedExpts[notInCombArray[i]]];
				}
				float resampTValue = calculateTValue(resampGroupA, resampGroupB);
				if (tValue < resampTValue) {
					permutProb++;
				}
				numCombsCounter++;
			}
			permutProb = permutProb / (double) numCombsCounter;

		} else {
			int randomCounter = 0;
			permutProb = 0;
			for (int i = 0; i < numCombs; i++) {
				float[][] randomGroups = randomlyPermute(geneValues,
						groupedExpts, numberGroupA, numberGroupB);
				float randomizedTValue = calculateTValue(randomGroups[0],
						randomGroups[1]);
				if (tValue < randomizedTValue) {
					permutProb++;
				}
				randomCounter++;
			}
			permutProb = permutProb / (double) randomCounter;
		}
		currentP = permutProb;
		return permutProb;
	}

	private boolean isSignificantByPermutation(int gene) {
		boolean sig = false;
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int[] groupedExpts = new int[numberGroupA + numberGroupB];
		int numbValidValuesA = 0;
		int numbValidValuesB = 0;

		int groupACounter = 0;
		int groupBCounter = 0;
		int groupedExptsCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == GROUP_A) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesA++;
				}
				groupACounter++;
				groupedExpts[groupedExptsCounter] = i;
				groupedExptsCounter++;
			} else if (groupAssignments[i] == GROUP_B) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesB++;
				}
				groupBCounter++;
				groupedExpts[groupedExptsCounter] = i;
				groupedExptsCounter++;
			}
		}

		if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
			currentP = Float.NaN;
			currentT = Float.NaN;
			return false;
		}

		float tValue = calculateTValue(gene, expMatrix);
		currentT = tValue;
		double permutProb, criticalP;
		permutProb = 0;
		criticalP = 0;

		if (significanceMethod == TtestAnalysisPanel.JUST_ALPHA) {
			criticalP = alpha;
		} else if (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI) {
			criticalP = alpha / (double) numGenes;
		}

		if (Float.isNaN(tValue)) {
			sig = false;
			currentP = Float.NaN;
			return sig;
		} else if (useAllCombs) {
			int numCombsCounter = 0;
			int[] combArray = new int[numberGroupA];
			for (int i = 0; i < combArray.length; i++) {
				combArray[i] = -1;
			}
			while (Combinations.enumerateCombinations(groupedExpts.length,
					numberGroupA, combArray)) {
				float[] resampGroupA = new float[numberGroupA];
				float[] resampGroupB = new float[numberGroupB];
				int[] notInCombArray = new int[numberGroupB];
				int notCombCounter = 0;
				for (int i = 0; i < groupedExpts.length; i++) {
					if (!belongsInArray(i, combArray)) {
						notInCombArray[notCombCounter] = i;
						notCombCounter++;
					}
				}
				for (int i = 0; i < combArray.length; i++) {
					resampGroupA[i] = geneValues[groupedExpts[combArray[i]]];
				}
				for (int i = 0; i < notInCombArray.length; i++) {
					resampGroupB[i] = geneValues[groupedExpts[notInCombArray[i]]];
				}
				float resampTValue = calculateTValue(resampGroupA, resampGroupB);
				if (Math.abs(tValue) < Math.abs(resampTValue)) {
					permutProb++;
				}
				numCombsCounter++;
			}

			permutProb = permutProb / (double) numCombsCounter;
			currentP = permutProb;
			if (permutProb <= criticalP) {
				sig = true;
			}
			criticalPValue = criticalP;

			return sig;
		} else {
			int randomCounter = 0;
			permutProb = 0;
			for (int i = 0; i < numCombs; i++) {
				float[][] randomGroups = randomlyPermute(geneValues,
						groupedExpts, numberGroupA, numberGroupB);
				float randomizedTValue = calculateTValue(randomGroups[0],
						randomGroups[1]);
				if (Math.abs(tValue) < Math.abs(randomizedTValue)) {
					permutProb++;
				}
				randomCounter++;
			}

			permutProb = permutProb / (double) randomCounter;
			currentP = permutProb;
			if (permutProb <= criticalP) {
				sig = true;
			}
		}
		return sig;
	}

	static private float[][] randomlyPermute(float[] gene, int[] groupedExpts,
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

	private int getDF(int gene) {
		getGroupValues(gene, expMatrix);
		return calculateDf(groupAValues, groupBValues, useWelchDf);
	}

	private boolean isSignificant(int gene) {
		boolean sig = false;
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int numbValidValuesA = 0;
		int numbValidValuesB = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == GROUP_A) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesA++;
				}
			} else if (groupAssignments[i] == GROUP_B) {
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesB++;
				}
			}
		}

		if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
			currentP = Float.NaN;
			currentT = Float.NaN;
			return false;
		}

		getGroupValues(gene, expMatrix);
		float tValue = calculateTValue(groupAValues, groupBValues);
		currentT = tValue;
		int df = calculateDf(groupAValues, groupBValues, useWelchDf);
		double prob;

		if (!isPermut) {
			if ((Float.isNaN(tValue))
					|| (Float.isNaN((new Integer(df)).floatValue()))
					|| (df <= 0)) {
				sig = false;
				currentP = Float.NaN;
			} else {
				TDistribution tDist = new TDistribution(df);
				double cumulP = tDist.cumulative(tValue);
				prob = 2 * (1 - cumulP);
				if (prob > 1) {
					prob = 2 - prob;
				}
				currentP = prob;

				if (significanceMethod == TtestAnalysisPanel.JUST_ALPHA) {
					if (prob <= alpha) {
						sig = true;
					} else {
						sig = false;
					}
				} else if (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI) {
					double thresh = alpha / (double) numGenes;
					criticalPValue = thresh;
					if (prob <= thresh) {
						sig = true;
					} else {
						sig = false;
					}
				}
			}
		}

		return sig;
	}

	static private int calculateDf(float[] groupA, float[] groupB,
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

		float varA = getVar(groupA);
		float varB = getVar(groupB);
		float numerator = (float) (Math.pow(((varA / kA) + (varB / kB)), 2));
		float denom = (float) ((Math.pow((varA / kA), 2) / (kA - 1)) + (Math
				.pow((varB / kB), 2) / (kB - 1)));
		int df = (int) Math.floor(numerator / denom);

		return df;
	}

	protected String GenerateHistoryHeader() {

		String histStr = "";
		// Header
		histStr += "T Test run with the following parameters:\n";
		histStr += "----------------------------------------\n";

		if (useWelchDf)
			histStr += "Group Variances: Unequal(Welch approximation)" + "\n";
		else
			histStr += "Group Variances: Equal" + "\n";

		histStr += "P-Values Parameters:" + "\n";
		if (isPermut){
			histStr += "\t" + "permutation is selected" + "\n";
			if (useAllCombs)
				histStr += "\t" + "Use all permutations is selected" + "\n";
			else {
				histStr += "\t" + "Randomly group experiments is selected" + "\n";
				histStr += "\t" + "#times: " + numCombs + "\n";
			}
		}
		else
			histStr += "\t" + "t-distribution is selected" + "\n";
		
		histStr += "\t" + "critical p-Value: " + alpha + "\n";

		if (isLogNormalized == true)
			histStr += "\t" + "isLogNormalized: true \n";
		else
			histStr += "\t" + "isLogNormalized: false \n";

		if (significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
			histStr += "Alpha Corrections: Just alpha(no correction)" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)
			histStr += "Alpha Corrections: Standard Bonferroni" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI)
			histStr += "Alpha Corrections: Ajusted Bonferroni" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.MIN_P)
			histStr += "Alpha Corrections: minP" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.MAX_T)
			histStr += "Alpha Corrections: maxT" + "\n";

		return histStr;
	}

	private String GenerateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		String histStr = null;

		histStr = "\t     " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n";

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr += "\t\t" + panel.get(aIndex) + "\n";

		return histStr;
	}

	String GenerateMarkerString(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> view) {
		String histStr = null;

		histStr = view.markers().size() + " markers analyzed:\n";
		for (DSGeneMarker marker : view.markers()) {
			histStr += "\t" + marker.getLabel() + "\n";
		}

		return histStr;

	}

	private void setFoldChnage(DSMicroarraySet<DSMicroarray> set,
			DSSignificanceResultSet<DSGeneMarker> resultSet) {

		String[] caseLabels = resultSet.getLabels(DSTTestResultSet.CASE);
		String[] controlLabels = resultSet.getLabels(DSTTestResultSet.CONTROL);
		DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager
				.getInstance().getCurrentContext(set);
		DSPanel<DSMicroarray> casePanel = new CSPanel<DSMicroarray>("Case");
		for (int i = 0; i < caseLabels.length; i++) {
			String label = caseLabels[i];
			casePanel.addAll(context.getItemsWithLabel(label));
		}
		casePanel.setActive(true);
		DSPanel<DSMicroarray> controlPanel = new CSPanel<DSMicroarray>(
				"Control");
		for (int i = 0; i < controlLabels.length; i++) {
			String label = controlLabels[i];
			controlPanel.addAll(context.getItemsWithLabel(label));
		}
		casePanel.setActive(true);

		int numMarkers = resultSet.getSignificantMarkers().size();

		double minValue = Double.MAX_VALUE;
		for (int i = 0; i < numMarkers; i++) {
			DSGeneMarker marker = resultSet.getSignificantMarkers().get(i);
			for (DSMicroarray microarray : casePanel) {
				if (microarray.getMarkerValue(marker).getValue() < minValue) {
					minValue = microarray.getMarkerValue(marker).getValue();
				}
			}

			for (DSMicroarray microarray : controlPanel) {
				if (microarray.getMarkerValue(marker).getValue() < minValue) {
					minValue = microarray.getMarkerValue(marker).getValue();
				}
			}

		}

		if (minValue < 0) {
			// Minimum value adjust to get us above 0 values
			minValue = Math.abs(minValue) + 1;
		} else {
			minValue = 0;
		}

		for (int i = 0; i < numMarkers; i++) {

			DSGeneMarker marker = resultSet.getSignificantMarkers().get(i);
			// Calculate fold change
			double caseMean = 0;
			for (DSMicroarray microarray : casePanel) {
				caseMean += microarray.getMarkerValue(marker).getValue();
			}
			caseMean = caseMean / casePanel.size() + minValue;

			double controlMean = 0;
			for (DSMicroarray microarray : controlPanel) {
				controlMean += microarray.getMarkerValue(marker).getValue();
			}
			controlMean = controlMean / controlPanel.size() + minValue;

			double fold_change = 0;
			double ratio = 0;
			if (!isLogNormalized) {
				ratio = caseMean / controlMean;
				if (ratio < 0) {

					fold_change = -Math.log(-ratio) / Math.log(2.0);
				} else {
					fold_change = Math.log(ratio) / Math.log(2.0);
				}
			} else {
				;
				fold_change = caseMean - controlMean;
			}

			resultSet.setFoldChange(marker, fold_change);

		}

	}

	private void executeMaxT(ProgressBar pbTtest) {
		 tValuesVector = null;
		 pValuesVector = null;

		double[] origTValues = new double[numGenes];
		double[] absTValues = new double[numGenes];
		double[] descTValues = new double[numGenes];
		int[] descGeneIndices = new int[numGenes];
		double[] adjPValues = new double[numGenes];

		double[][] uMatrix = new double[numGenes][numCombs];

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return;
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
						return;
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
				return;
			
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
						return;
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
				return;
			}
			int pCounter = 0;
			for (int j = 0; j < numCombs; j++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return;
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
				return;
			}
			if (Double.isNaN(origTValues[i])) {
				adjPValues[i] = Double.NaN;
				NaNPCounter++;
			}
		}
		for (int i = 1; i < numGenes - NaNPCounter; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return;
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
			if (!belongsInArray(i, combArray)) {
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

		float[][] permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);
		return getTwoClassUnpairedTValues(permutedMatrix);
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

		permutedExpts = getPermutedValues(numExps, validArray);
		float[][] permutedMatrix = getPermutedMatrix(expMatrix, permutedExpts);

		return getTwoClassUnpairedTValues(permutedMatrix);
	}

	private transient int iterationCounter;
	private transient int[] usedExptsArray;
	private transient int[] combArray;
	private transient double[][] uMatrix;

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

			String message ="the \"all permutations\" option will result in "
				+iterationCounter
				+" permutations being run, which may take a long time. Do you wish to proceed?"; 
			/*
			String message = "The number of specified permutations is "
					+ numCombs
					+ " but based on chosen option of all permutations\n\n"
					+ "\t\tthe real number of permutations will be "
=======
			String message = "Note - the \"all permutations\" option will result in "
 
>>>>>>> .r7051
					+ iterationCounter
					+ " permutations being run, which may take a long time. \n"
					+ "Do you wish to proceed?";
			*/
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

	private void executeMinP(ProgressBar pbTtest) {
		tValuesVector = null;
		pValuesVector = null;

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
				return;
			}
			qMatrix[numGenes][i] = 1.0d;
		}

		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return;
			}
			origTValues[i] = calculateTValue(i, expMatrix);
		}
		if (!useAllCombs) {
			for (int i = 0; i < numCombs; i++) {
				double[] currentPermTValues = getCurrentPermTValueNotUseAllCombs(pbTtest);

				for (int j = 0; j < numGenes; j++) {
					if (this.stopAlgorithm) {
						pbTtest.dispose();
						return;
					}
					origTMatrix[j][i] = currentPermTValues[j];
				}
			}
		} else {
			int[] permutedExpts = new int[numExps];

			for (int i = 0; i < numExps; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return;
				}
				permutedExpts[i] = i;
			}

			if(!prepareForAllCombs(pbTtest))
				return;
			
			if (uMatrix != null)
				origTMatrix = this.uMatrix;

			int permCounter = 0;

			while (Combinations.enumerateCombinations(usedExptsArray.length,
					numberGroupA, combArray)) {

				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return;
				}
				if (permCounter == iterationCounter)
					break;

				int[] notInCombArray = new int[numberGroupB];
				int notCombCounter = 0;

				for (int i = 0; i < usedExptsArray.length; i++) {
					if (!belongsInArray(i, combArray)) {
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

				float[][] permutedMatrix = getPermutedMatrix(expMatrix,
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
				return;
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
				return;
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
				return;
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
			double[] currentGeneSortedPVals = getPValsFromOrderStats(
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
				return;
			}
			sortedAdjPValues[i] = Math.max(sortedAdjPValues[i - 1],
					sortedAdjPValues[i]);
		}

		for (int i = 0; i < sortedAdjPValues.length; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return;
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
	}

	static private double[] getPValsFromOrderStats(double[] sortedTVals,
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

	static private float calculateTValue(float[] groupA, float[] groupB) {
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
	static private float getVar(float[] group) {
		return getVar(group, getMean(group));
	}

	static private int[] getPermutedValues(int arrayLength, int[] validArray) {
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

	static private float[][] getPermutedMatrix(float[][] inputMatrix,
			int[] permExpts) {
		float[][] permutedMatrix = new float[inputMatrix.length][inputMatrix[0].length];
		for (int i = 0; i < inputMatrix.length; i++) {
			for (int j = 0; j < inputMatrix[0].length; j++) {
				permutedMatrix[i][j] = inputMatrix[i][permExpts[j]];
			}
		}
		return permutedMatrix;
	}

	private double[] getTwoClassUnpairedTValues(float[][] inputMatrix) {
		double[] tValsFromMatrix = new double[numGenes];
		for (int i = 0; i < numGenes; i++) {
			tValsFromMatrix[i] = calculateTValue(i, inputMatrix);
		}

		return tValsFromMatrix;
	}

	private transient float[] groupAValues = null;
	private transient float[] groupBValues = null;;
	private void getGroupValues(int gene, float[][] inputMatrix) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = inputMatrix[gene][i];
		}

		groupAValues = new float[numberGroupA];
		groupBValues = new float[numberGroupB];

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
	}
	
	private float calculateTValue(int gene, float[][] inputMatrix) {
		getGroupValues(gene, inputMatrix);
		return calculateTValue(groupAValues, groupBValues);
	}

	static private boolean belongsInArray(int i, int[] arr) {
		for (int j = 0; j < arr.length; j++) {
			if (i == arr[j]) {
				return true;
			}
		}
		return false;
	}

}
