package org.geworkbench.components.ttest;

import java.util.HashSet;
import java.util.Vector;

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
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.Combinations;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.QSort;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.TDistribution;
import org.apache.commons.math.distribution.TDistributionImpl;

/**
 * <p>geWorkbench</p>
 * <p>Description: Modular Application Framework for Gene Expression, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
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
 * 
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id$
 */
public class TtestAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 1302806024752128407L;
	
	private static final int GROUP_A = 1;
	private static final int GROUP_B = 2;
	private static final int NEITHER_GROUP = 3;

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
		setDefaultPanel(new TtestAnalysisPanel());
	}

	/* This constructor is necessary for MasterRegulatorAnalysis. */
	public TtestAnalysis(TtestAnalysisPanel panel) {
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
		return AbstractAnalysis.TTEST_TYPE;
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

		String histMarkerString = GenerateMarkerString(data);

		groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
				+ groupAndChipsString;

		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
				maSet, "T-Test", caseSet.toArray(new String[0]), controlSet
						.toArray(new String[0]), criticalPValue

		);

		if (significanceMethod == TtestAnalysisPanel.MIN_P
				|| significanceMethod == TtestAnalysisPanel.MAX_T) {
			TTestUtil util = new TTestUtil(numGenes, numCombs, stopAlgorithm, expMatrix, numExps, numberGroupA, numberGroupB, groupAssignments, useAllCombs);
			TTestUtil.ValueResult r = null;
			if (significanceMethod == TtestAnalysisPanel.MIN_P) {
				 r = util.executeMinP(pbTtest);
			} else if (significanceMethod == TtestAnalysisPanel.MAX_T) {
				r = util.executeMaxT(pbTtest);
			}
			if (null == r) {
				pbTtest.dispose();
				return null;
			}
			tValuesVector = r.tValuesVector;
			pValuesVector = r.pValuesVector;

			for (int i = 0; i < pValuesVector.size(); i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				sigSet.setSignificance(data.markers().get(i),
						pValuesVector.get(i));
				sigSet.setTValue(data.markers().get(i), tValuesVector.get(i));
			}

		} else { // other significanceMethod choices except MIN_P or MAX_T

			Vector<Integer> clusterVector = sortGenesBySignificance(pbTtest);
			
			if (null == clusterVector) {
				pbTtest.dispose();
				return null;
			}

			for (int i = 0; i < pValuesVector.size(); i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
	
				DSGeneMarker m = data.markers().get(i);
				sigSet.setMarker(m, pValuesVector.get(i));
				sigSet.setTValue(m, tValuesVector.get(i));
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
		}

		sigSet.sortMarkersBySignificance();
		setFoldChnage(maSet, sigSet);
		
		// add data set history.
		if (!calledFromOtherComponent) {
			ProjectPanel.addToHistory(sigSet, GenerateHistoryHeader() + groupAndChipsString
				+ histMarkerString);
		}
		pbTtest.dispose();
		if (this.stopAlgorithm) {
			return null;
		}

		return new AlgorithmExecutionResults(true, "Ttest", sigSet);
	} // end of method calculate

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event) {
		return event;
	}
	
	private Vector<Integer> getGenesUsingAdjustedBonferroni(ProgressBar pbTtest) {
		Vector<Integer> sigGenes = new Vector<Integer>();

		float[] tValues = new float[numGenes];
		for (int i = 0; i < numGenes; i++) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			tValues[i] = Math.abs(calculateTValue(i));
		}

		QSort sortTValues = new QSort(tValues);
		float[] sortedTValues = sortTValues.getSorted();
		int[] sortedUniqueIDs = sortTValues.getOrigIndx();

		double adjAlpha = alpha;
		int denomAlpha = numGenes;
		double prob = Double.POSITIVE_INFINITY;

		double[] tValuesArray = new double[numGenes];
		double[] pValuesArray = new double[numGenes];

		for (int i = (sortedTValues.length - 1); i >= 0; i--) {
			if (this.stopAlgorithm) {
				pbTtest.dispose();
				return null;
			}
			float dF = 0;
			boolean dfCondition = true;
			if(!isPermut) {
				dF = getDF(sortedUniqueIDs[i]);
				dfCondition = (Float.isNaN((new Float(dF)).floatValue()))	|| (dF <= 0);
			}
			if ( Float.isNaN(sortedTValues[i]) || dfCondition ) {
				tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
				pValuesArray[sortedUniqueIDs[i]] = Float.NaN;
			} else {
				if(!isPermut) {
					TDistribution tDist = new TDistributionImpl(dF);
					try{
						double cumulP = tDist.cumulativeProbability(-sortedTValues[i]);
						prob = 2 * cumulP;
					}catch(MathException e){
						e.printStackTrace();
					}
					if (prob > 1) {
						prob = 1;
					}
				} else {
					prob = getPermutedProb(sortedUniqueIDs[i]);
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
				if (i>0 && sortedTValues[i] > sortedTValues[i - 1]) {
					denomAlpha--;
					if (denomAlpha < 1) {
						log.warn("Warning: denomAlpha = " + denomAlpha);
					}
				}
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
		
		return sigGenes;
	}

	private Vector<Integer> sortGenesBySignificance(ProgressBar pbTtest) {

		if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
				|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
			Vector<Integer> sigGenes = new Vector<Integer>();
			for (int i = 0; i < numGenes; i++) {
				if (this.stopAlgorithm) {
					pbTtest.dispose();
					return null;
				}
				if (!isPermut) {
					if(isSignificant(i))
						sigGenes.add(new Integer(i));
				} else {
					if(isSignificantByPermutation(i))
						sigGenes.add(new Integer(i));
				}
				tValuesVector.add(new Float(currentT));
				pValuesVector.add(new Float(currentP));
			}
			return sigGenes;
		} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
			return getGenesUsingAdjustedBonferroni(pbTtest);
		} else {
			log.error("unknown significant method "+significanceMethod);
			return null;
		}
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

		float tValue = calculateTValue(gene);
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
					if (!TTestUtil.belongsInArray(i, combArray)) {
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
				float resampTValue = TTestUtil.calculateTValue(resampGroupA, resampGroupB);
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
				float[][] randomGroups = TTestUtil.randomlyPermute(geneValues,
						groupedExpts, numberGroupA, numberGroupB);
				float randomizedTValue = TTestUtil.calculateTValue(randomGroups[0],
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

		float tValue = calculateTValue(gene);
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
					if (!TTestUtil.belongsInArray(i, combArray)) {
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
				float resampTValue = TTestUtil.calculateTValue(resampGroupA, resampGroupB);
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
				float[][] randomGroups = TTestUtil.randomlyPermute(geneValues,
						groupedExpts, numberGroupA, numberGroupB);
				float randomizedTValue = TTestUtil.calculateTValue(randomGroups[0],
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

	private float getDF(int gene) {
		getGroupValues(gene);
		return TTestUtil.calculateDf(groupAValues, groupBValues, useWelchDf);
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

		getGroupValues(gene);
		float tValue = TTestUtil.calculateTValue(groupAValues, groupBValues);
		currentT = tValue;
		float df = TTestUtil.calculateDf(groupAValues, groupBValues, useWelchDf);
		double prob = 1;

		if ((Float.isNaN(tValue))
				|| (Float.isNaN((new Float(df)).floatValue())) || (df <= 0)) {
			sig = false;
			currentP = Float.NaN;
		} else {
			TDistribution tDist = new TDistributionImpl(df);
			try{
				double cumulP = tDist.cumulativeProbability(-Math.abs(tValue));
				prob = 2 * cumulP;
			}catch(MathException e){
				e.printStackTrace();
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

		return sig;
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
			histStr += "Alpha Corrections: Adjusted Bonferroni" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.MIN_P)
			histStr += "Alpha Corrections: minP" + "\n";
		else if (significanceMethod == TtestAnalysisPanel.MAX_T)
			histStr += "Alpha Corrections: maxT" + "\n";

		return histStr;
	}

	private String GenerateGroupAndChipsString(DSPanel<DSMicroarray> panel) {
		StringBuffer histStr =  new StringBuffer( "\t     " + panel.getLabel() + " (" + panel.size()
				+ " chips)" + ":\n" );

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr.append(  "\t\t" ).append( panel.get(aIndex) ).append( "\n" );

		return histStr.toString();
	}

	String GenerateMarkerString(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> view) {
		StringBuffer histStr = new StringBuffer( view.markers().size()+" markers analyzed:\n" );
		for (DSGeneMarker marker : view.markers()) {
			histStr.append( "\t" ).append( marker.getLabel() ).append( "\n" );
		}

		return histStr.toString();

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

	private transient float[] groupAValues = null;
	private transient float[] groupBValues = null;;
	private void getGroupValues(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
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
	
	private float calculateTValue(int gene) {
		getGroupValues(gene);
		return TTestUtil.calculateTValue(groupAValues, groupBValues);
	}
}
