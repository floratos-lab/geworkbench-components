package org.geworkbench.components.analysis.clustering;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContext;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSTTestResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSTTestResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Script;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.Combinations;
import org.geworkbench.util.ProgressBarT;
import org.geworkbench.util.QSort;

import JSci.maths.statistics.TDistribution;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 1.0
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

@SuppressWarnings("unchecked")
public class TtestAnalysis extends AbstractAnalysis implements
		ClusteringAnalysis {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int localAnalysisType;

	private boolean stop = false;
	// private ProgressBar pb = null;
	private int function;
	private float factor;
	private boolean absolute;
	private float[][] expMatrix;
	// THe following result is used for caSCRIPT only.
	DSSignificanceResultSet<DSGeneMarker> the_sigSet;
	boolean hierarchical_tree;
	int method_linkage;
	boolean calculate_genes;
	boolean calculate_experiments;

	private Vector[] clusters;
	private int k;

	private int numGenes, numExps;
	private double alpha, criticalPValue;
	private int significanceMethod;
	private boolean isPermut, useWelchDf;
	int[] groupAssignments;
	private int numCombs;
	boolean useAllCombs;
	int tTestDesign;
	float oneClassMean = 0.0f;
	
	boolean useroverride = false;
    boolean isLogNormalized = false;    

	double currentP = 0.0f;
	double currentT = 0.0f;
	int currentIndex = 0;
	Vector sigTValues = new Vector();
	Vector sigPValues = new Vector();
	Vector nonSigTValues = new Vector();
	Vector nonSigPValues = new Vector();
	Vector tValuesVector = new Vector();
	Vector pValuesVector = new Vector();

	public TtestAnalysis() {
		localAnalysisType = AbstractAnalysis.TTEST_TYPE;
		setLabel("T Test Analysis");
		setDefaultPanel(new TtestAnalysisPanel());
	}

	private void reset() {
		sigTValues.clear();
		sigPValues.clear();
		nonSigTValues.clear();
		nonSigPValues.clear();
		tValuesVector.clear();
		pValuesVector.clear();
		clusters = null;
		expMatrix = null;
		groupAssignments = null;
		currentP = currentT = 0.0d;
		oneClassMean = 0f;
		alpha = 0d;
		criticalPValue = 0d;
		currentIndex = numGenes = numExps = tTestDesign = significanceMethod = 0;
		useroverride = false;
        isLogNormalized = false;
	}

	public int getAnalysisType() {
		return localAnalysisType;
	}

	@Script
	public DSSignificanceResultSet runTtest(Object input) {

		if (input instanceof DSMicroarraySet) {
			CSMicroarraySetView csMicroarraySetView = new CSMicroarraySetView(
					(DSMicroarraySet) input);
			execute(csMicroarraySetView);
		}

		return the_sigSet;
	}

	@Script
	public DSSignificanceResultSet runTtest(Object input, double alpha,
			String variancesLevel, String pvalueBase, String significanceMethod) {
		((TtestAnalysisPanel) aspp).setAlpha(alpha);
		((TtestAnalysisPanel) aspp).setSignificanceMethod(significanceMethod);
		((TtestAnalysisPanel) aspp).setUseWalch(variancesLevel);
		((TtestAnalysisPanel) aspp).setPValuesDistribution(pvalueBase);
		if (input instanceof DSMicroarraySet) {
			CSMicroarraySetView csMicroarraySetView = new CSMicroarraySetView(
					(DSMicroarraySet) input);
			execute(csMicroarraySetView);
		}

		return the_sigSet;
	}

	public AlgorithmExecutionResults execute(Object input) {
		ProgressBarT pbTtest = null;
		reset();
		if (input == null) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}

		assert input instanceof DSMicroarraySetView;
		// DSDataSetView<DSMarker, DSMicroarray> data = (DSDataSetView<DSMarker,
		// DSMicroarray>)input;
		DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> data = (DSMicroarraySetView) input;
		boolean allArrays = !data.useItemPanel();
		System.out.println("All arrays: " + allArrays);
		// data.useItemPanel(true);
		// data.useMarkerPanel(true);

		int markers = data.markers().size();
		int arrays = data.items().size();

		pbTtest = ProgressBarT.create(ProgressBarT.BOUNDED_TYPE);
		pbTtest.addObserver(this);
		pbTtest.setTitle("T Test Analysis");
		pbTtest.setBounds(new ProgressBarT.IncrementModel(0, markers, 0,
				markers, 1));
		pbTtest.setMessage("Constructing ... " + markers + " variables");
		/*
		 * pbTtest.start(); this.stopAlgorithm = false; expMatrix = new
		 * float[markers][arrays]; pbTtest.setType(ProgressBarT.BOUNDED_TYPE);
		 * for (int i = 0; i < markers; i++) { if (!this.stopAlgorithm) { for
		 * (int j = 0; j < arrays; j++) { // expMatrix[i][j] = //
		 * (float)data.items().get(j).getMarkerValue(i).getValue();
		 * expMatrix[i][j] = (float) data.getValue(i, j); } pbTtest.update(); }
		 * else { pbTtest.dispose(); return null; } }
		 */
		pbTtest.setType(ProgressBarT.INDETERMINATE_TYPE);
		groupAssignments = new int[arrays];
		// .getObject();
		DSDataSet set = data.getDataSet();

		if (set instanceof DSMicroarraySet) {
			DSMicroarraySet maSet = (DSMicroarraySet) set;
			DSAnnotationContextManager manager = CSAnnotationContextManager
					.getInstance();
			DSAnnotationContext<DSMicroarray> context = manager
					.getCurrentContext(maSet);
			tTestDesign = TtestAnalysisPanel.ONE_CLASS;
			boolean hasGroupA = false;
			boolean hasGroupB = false;
			for (int i = 0; i < arrays; i++) {
				DSMicroarray ma = data.items().get(i);
				if (ma instanceof DSMicroarray) {
					// DSPanel panel = selCriterion.panels().get(ma);
					String[] labels = context.getLabelsForItem(ma);
					if ((labels.length == 0) && allArrays) {
						groupAssignments[i] = TtestAnalysisPanel.GROUP_B;
						tTestDesign = TtestAnalysisPanel.BETWEEN_SUBJECTS;
						hasGroupB = true;
					}
					for (String label : labels) {
						if (context.isLabelActive(label) || allArrays) {
							String v = context.getClassForLabel(label);
							if (v.equals(CSAnnotationContext.CLASS_CASE)) {
								groupAssignments[i] = TtestAnalysisPanel.GROUP_A;
								hasGroupA = true;
							} else if (v
									.equals(CSAnnotationContext.CLASS_CONTROL)) {
								groupAssignments[i] = TtestAnalysisPanel.GROUP_B;
								tTestDesign = TtestAnalysisPanel.BETWEEN_SUBJECTS;
								hasGroupB = true;
							} else {
								groupAssignments[i] = TtestAnalysisPanel.NEITHER_GROUP;
							}
						}
					}
				} else {
					groupAssignments[i] = TtestAnalysisPanel.NEITHER_GROUP;
				}
			}
			if (!(hasGroupA || hasGroupB)) {
				pbTtest.dispose();
				/*
				 * return new AlgorithmExecutionResults( false, "Please specify
				 * at least one \"case\" microarray and one \"control\"
				 * microarray.", null);
				 */
				return new AlgorithmExecutionResults(
						false,
						"Please activate at least one set of arrays for \"case\", and one set of arrays for \"control\".",
						null);
			}
			if (!hasGroupA) {
				pbTtest.dispose();
				/*
				 * return new AlgorithmExecutionResults(false, "Please specify
				 * at least one \"case\" microarray.", null);
				 */
				return new AlgorithmExecutionResults(false,
						"Please activate at least one set of arrays for \"case\".",
						null);
			}
			if (!hasGroupB) {
				pbTtest.dispose();
				/*
				 * return new AlgorithmExecutionResults(false, "Please specify
				 * at least one \"control\" microarray.", null);
				 */
				return new AlgorithmExecutionResults(false,
						"Please activate at least one set of arrays for \"control\".",
						null);
			}

			/*
			 * the calculations moved below checking input arguments.
			 */
/*
			pbTtest = ProgressBarT.create(ProgressBarT.BOUNDED_TYPE);
			pbTtest.addObserver(this);
			pbTtest.setTitle("T Test Analysis");
			pbTtest.setBounds(new ProgressBarT.IncrementModel(0, markers, 0,
					markers, 1));
			pbTtest.setMessage("Constructing ... " + markers + " variables");
*/
			pbTtest.start();
			this.stopAlgorithm = false;
			expMatrix = new float[markers][arrays];
			pbTtest.setType(ProgressBarT.BOUNDED_TYPE);
			for (int i = 0; i < markers; i++) {
				if (!this.stopAlgorithm) {
					for (int j = 0; j < arrays; j++) {
						// expMatrix[i][j] =
						// (float)data.items().get(j).getMarkerValue(i).getValue();
						expMatrix[i][j] = (float) data.getValue(i, j);
					}
					pbTtest.update();
				} else {
					pbTtest.dispose();
					return null;
				}
			}
			pbTtest.setType(ProgressBarT.INDETERMINATE_TYPE);

			// ///////////////////////////////////////////////////////

			function = ((TtestAnalysisPanel) aspp).getDistanceFunction();
			factor = ((TtestAnalysisPanel) aspp).getDistanceFactor();
			absolute = ((TtestAnalysisPanel) aspp).isDistanceAbsolute();

			hierarchical_tree = ((TtestAnalysisPanel) aspp)
					.computeHierarchicalTree();
			method_linkage = ((TtestAnalysisPanel) aspp).getLinkageMethod();
			calculate_genes = ((TtestAnalysisPanel) aspp).calculateGenes();
			calculate_experiments = ((TtestAnalysisPanel) aspp)
					.calculateExperiments();

			useroverride = ((TtestAnalysisPanel) aspp).isUseroverride();
	        isLogNormalized = ((TtestAnalysisPanel) aspp).isLogNormalized();
			
	        numGenes = data.markers().size();
			numExps = data.items().size();

			if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
				oneClassMean = ((TtestAnalysisPanel) aspp).getOneClassMean();
			}
			alpha = ((TtestAnalysisPanel) aspp).getAlpha();
			criticalPValue = ((TtestAnalysisPanel) aspp).getAlpha();
			significanceMethod = ((TtestAnalysisPanel) aspp)
					.getSignificanceMethod();
			isPermut = ((TtestAnalysisPanel) aspp).isPermut();
			useWelchDf = ((TtestAnalysisPanel) aspp).useWelchDf();
			numCombs = ((TtestAnalysisPanel) aspp).getNumCombs();
			useAllCombs = ((TtestAnalysisPanel) aspp).useAllCombs();

			String[][] labels = new String[2][];
			labels[0] = context
					.getLabelsForClass(CSAnnotationContext.CLASS_CASE);
			labels[1] = context
					.getLabelsForClass(CSAnnotationContext.CLASS_CONTROL);
			HashSet<String>[] classSets = new HashSet[2];

			String groupAndChipsString = "";
			for (int j = 0; j < 2; j++) {
				String[] classLabels = labels[j];
				classSets[j] = new HashSet<String>();

				if (j == 0)
					groupAndChipsString += "\t case group(s): \n";
				else
					groupAndChipsString += "\t control group(s): \n";

				for (int i = 0; i < classLabels.length; i++) {
					String label = classLabels[i];
					if (context.isLabelActive(label) || !data.useItemPanel()) {
						// if (context.isLabelActive(label)) {
						classSets[j].add(label);
						groupAndChipsString += GenerateGroupAndChipsString(context
								.getItemsWithLabel(label));
					}
				}
			}

			int totalSelectedGroup = classSets[0].size() + classSets[1].size();
			String histHeader = null;
			String histMarkerString = GenerateMarkerString(data);

			groupAndChipsString = totalSelectedGroup + " groups analyzed:\n"
					+ groupAndChipsString;

			if (significanceMethod == TtestAnalysisPanel.MIN_P
					|| significanceMethod == TtestAnalysisPanel.MAX_T) {
				AlgorithmExecutionResults results = null;
				if (significanceMethod == TtestAnalysisPanel.MIN_P) {
					results = executeMinP();
				} else if (significanceMethod == TtestAnalysisPanel.MAX_T) {
					results = executeMaxT();
				}
				DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
						maSet, "T-Test", classSets[0].toArray(new String[0]),
						classSets[1].toArray(new String[0]), criticalPValue

				);
				the_sigSet = sigSet;
				Hashtable result = (Hashtable) results.getResults();
				float[][] pValuesMatrix = (float[][]) result.get("pValues");
				for (int i = 0; i < pValuesMatrix.length; i++) {
					sigSet.setSignificance(data.markers().get(i),
							pValuesMatrix[i][0]);
				}
				sigSet.sortMarkersBySignificance();
				
				
				setFoldChnage (maSet, sigSet);
				// add data set history.
				histHeader = GenerateHistoryHeader();
				ProjectPanel.addToHistory(sigSet, histHeader
						+ groupAndChipsString + histMarkerString);
				pbTtest.dispose();     
				return new AlgorithmExecutionResults(true, "Ttest", sigSet);
			}

			Vector clusterVector = new Vector();
			if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
				if (isPermut) {
					clusterVector = sortGenesByPermutationSignificance();
				} else {
					clusterVector = sortGenesBySignificance();
				}
			} else if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
				clusterVector = sortGenesForOneClassDesign();
			}

			k = clusterVector.size();

			float[][] isSigMatrix = new float[numGenes][1];

			for (int i = 0; i < isSigMatrix.length; i++) {
				isSigMatrix[i][0] = 0.0f;
			}

			Vector sigGenes = (Vector) (clusterVector.get(0));

			for (int i = 0; i < sigGenes.size(); i++) {
				int currentGene = ((Integer) (sigGenes.get(i))).intValue();
				isSigMatrix[currentGene][0] = 1.0f;
			}

			Vector oneClassDFVector = new Vector();
			Vector oneClassGeneMeansVector = new Vector();
			Vector oneClassGeneSDsVector = new Vector();

			if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
				tValuesVector = new Vector();
				oneClassDFVector = new Vector();

				for (int i = 0; i < numGenes; i++) {
					float[] currentGeneValues = getOneClassGeneValues(i);
					float currentOneClassT = (float) getOneClassTValue(currentGeneValues);
					tValuesVector.add(new Float(currentOneClassT));
					float currentOneClassDF = (float) getOneClassDFValue(currentGeneValues);
					oneClassDFVector.add(new Float(currentOneClassDF));
					float currentOneClassMean = getMean(currentGeneValues);
					oneClassGeneMeansVector.add(new Float(currentOneClassMean));
					float currentOneClassSD = (float) (Math
							.sqrt(getVar(currentGeneValues)));
					oneClassGeneSDsVector.add(new Float(currentOneClassSD));
				}
			}

			float[][] tValuesMatrix = new float[tValuesVector.size()][1];
			float[][] pValuesMatrix = new float[pValuesVector.size()][1];
			float[][] dfMatrix = new float[numGenes][1];
			float[][] oneClassMeansMatrix = new float[numGenes][1];
			float[][] oneClassSDsMatrix = new float[numGenes][1];

			if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
				for (int i = 0; i < tValuesVector.size(); i++) {
					tValuesMatrix[i][0] = Math.abs(((Float) (tValuesVector
							.get(i))).floatValue());
				}
			} else if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
				for (int i = 0; i < tValuesVector.size(); i++) {
					tValuesMatrix[i][0] = ((Float) (tValuesVector.get(i)))
							.floatValue();
				}
			}

			DSSignificanceResultSet<DSGeneMarker> sigSet = new CSTTestResultSet<DSGeneMarker>(
					maSet, "T-Test", classSets[0].toArray(new String[0]),
					classSets[1].toArray(new String[0]), criticalPValue

			);
			the_sigSet = sigSet;
			for (int i = 0; i < pValuesVector.size(); i++) {
				pValuesMatrix[i][0] = ((Float) (pValuesVector.get(i)))
						.floatValue();
				sigSet.setMarker(data.markers().get(i), pValuesMatrix[i][0]);
			}

			if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
				for (int i = 0; i < numGenes; i++) {
					dfMatrix[i][0] = (float) (getDF(i));
				}
			} else if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
				for (int i = 0; i < numGenes; i++) {
					dfMatrix[i][0] = ((Float) (oneClassDFVector.get(i)))
							.floatValue();
					oneClassMeansMatrix[i][0] = ((Float) (oneClassGeneMeansVector
							.get(i))).floatValue();
					oneClassSDsMatrix[i][0] = ((Float) (oneClassGeneSDsVector
							.get(i))).floatValue();
				}
			}

			float[][] meansAMatrix = new float[numGenes][1];
			float[][] meansBMatrix = new float[numGenes][1];
			float[][] sdAMatrix = new float[numGenes][1];
			float[][] sdBMatrix = new float[numGenes][1];

			if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
				Vector meansAndSDs = getMeansAndSDs();
				float[] meansA = (float[]) (meansAndSDs.get(0));
				float[] meansB = (float[]) (meansAndSDs.get(1));
				float[] sdA = (float[]) (meansAndSDs.get(2));
				float[] sdB = (float[]) (meansAndSDs.get(3));

				for (int i = 0; i < numGenes; i++) {
					meansAMatrix[i][0] = meansA[i];
					meansBMatrix[i][0] = meansB[i];
					sdAMatrix[i][0] = sdA[i];
					sdBMatrix[i][0] = sdB[i];
				}
			}

			clusters = new Vector[k];

			for (int i = 0; i < k; i++) {
				clusters[i] = (Vector) (clusterVector.get(i));
			}

			float[][] means = getMeans(clusters);
			float[][] variances = getVariances(clusters, means);

			// Hashtable result = new Hashtable();

			DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
					"Significant Genes");

			if (clusters.length == 2) {
				for (int i = 0; i < clusters[0].size(); i++) {
					int index = ((Integer) clusters[0].get(i)).intValue();
					DSGeneMarker item = data.markers().get(index);
					panelSignificant.add(item, new Float(
							pValuesMatrix[index][0]));
					sigSet.addSigGenToPanel(item);
				}
			}
			publishSubpanelChangedEvent(new SubpanelChangedEvent(
					DSGeneMarker.class, panelSignificant,
					SubpanelChangedEvent.NEW));
			// result.put("Significant Genes", panelSignificant);
			//
			// result.put("number-of-clusters",
			// String.valueOf(clusters.length));
			// result.put("clusters_means", means);
			// result.put("clusters_variances", variances);
			// result.put("pValues", pValuesMatrix);
			// result.put("tValues", tValuesMatrix);
			// result.put("dfValues", dfMatrix);
			// result.put("meansAMatrix", meansAMatrix);
			// result.put("meansBMatrix", meansBMatrix);
			// result.put("sdAMatrix", sdAMatrix);
			// result.put("sdBMatrix", sdBMatrix);
			// result.put("isSigMatrix", isSigMatrix);
			// result.put("oneClassMeansMatrix", oneClassMeansMatrix);
			// result.put("oneClassSDsMatrix", oneClassSDsMatrix);

			sigSet.sortMarkersBySignificance();
			AlgorithmExecutionResults results = new AlgorithmExecutionResults(
					true, "Ttest", sigSet);
			setFoldChnage (maSet, sigSet); 
			// add data set history.
			histHeader = GenerateHistoryHeader();
			ProjectPanel.addToHistory(sigSet, histHeader + groupAndChipsString
					+ histMarkerString);
			 

			pbTtest.dispose();
			if (this.stopAlgorithm) {
				return null;
			}

			return results;
		}
		pbTtest.dispose();
		return null;
	}

	@Publish
	public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent event) {
		return event;
	}

	public AlgorithmExecutionResults executeMaxT() {
		double[] origTValues = new double[numGenes];
		double[] descTValues = new double[numGenes];
		int[] descGeneIndices = new int[numGenes];
		double[] adjPValues = new double[numGenes];
		double[][] permutedRankedTValues = new double[numCombs][numGenes];
		double[][] uMatrix = new double[numGenes][numCombs];
		if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
			for (int i = 0; i < numGenes; i++) {
				origTValues[i] = Math.abs(getTValue(i));
			}

			org.geworkbench.util.QSort sortDescTValues = new QSort(origTValues,
					org.geworkbench.util.QSort.DESCENDING);
			descTValues = sortDescTValues.getSortedDouble();
			descGeneIndices = sortDescTValues.getOrigIndx();

			if (!useAllCombs) {
				for (int i = 0; i < numCombs; i++) {
					int[] permutedExpts = new int[1];
					Vector validExpts = new Vector();

					for (int j = 0; j < groupAssignments.length; j++) {
						if (groupAssignments[j] != TtestAnalysisPanel.NEITHER_GROUP) {
							validExpts.add(new Integer(j));
						}
					}

					int[] validArray = new int[validExpts.size()];
					for (int j = 0; j < validArray.length; j++) {
						validArray[j] = ((Integer) (validExpts.get(j)))
								.intValue();
					}

					permutedExpts = getPermutedValues(numExps, validArray);
					float[][] permutedMatrix = getPermutedMatrix(expMatrix,
							permutedExpts);
					double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);

					if (Double
							.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
						uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
					} else {
						uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
					}

					for (int j = numGenes - 2; j >= 0; j--) {
						if (Double
								.isNaN(currentPermTValues[descGeneIndices[j]])) {
							uMatrix[j][i] = uMatrix[j + 1][i];
						} else {
							uMatrix[j][i] = Math.max(uMatrix[j + 1][i],
									currentPermTValues[descGeneIndices[j]]);
						}
					}

				}
			} else {
				int[] permutedExpts = new int[numExps];

				for (int i = 0; i < numExps; i++) {
					permutedExpts[i] = i;
				}
				Vector usedExptsVector = new Vector();
				int numGroupAValues = 0;
				for (int i = 0; i < groupAssignments.length; i++) {
					if (groupAssignments[i] != TtestAnalysisPanel.NEITHER_GROUP) {
						usedExptsVector.add(new Integer(i));
					}
					if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
						numGroupAValues++;
					}
				}
				int[] usedExptsArray = new int[usedExptsVector.size()];

				for (int i = 0; i < usedExptsArray.length; i++) {
					usedExptsArray[i] = ((Integer) (usedExptsVector.get(i)))
							.intValue();
				}

				int[] combArray = new int[numGroupAValues];
				for (int i = 0; i < combArray.length; i++) {
					combArray[i] = -1;
				}

				int numGroupBValues = usedExptsArray.length - numGroupAValues;

				int permCounter = 0;

				while (org.geworkbench.util.Combinations.enumerateCombinations(
						usedExptsArray.length, numGroupAValues, combArray)) {

					int[] notInCombArray = new int[numGroupBValues];
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

					if (Double
							.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
						uMatrix[numGenes - 1][permCounter] = Double.NEGATIVE_INFINITY;
					} else {
						uMatrix[numGenes - 1][permCounter] = currentPermTValues[descGeneIndices[numGenes - 1]];
					}

					for (int j = numGenes - 2; j >= 0; j--) {
						if (Double
								.isNaN(currentPermTValues[descGeneIndices[j]])) {
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
		} else if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
			for (int i = 0; i < numGenes; i++) {
				origTValues[i] = Math.abs(getOneClassTValue(i));
			}

			org.geworkbench.util.QSort sortDescTValues = new org.geworkbench.util.QSort(
					origTValues, QSort.DESCENDING);
			descTValues = sortDescTValues.getSortedDouble();
			descGeneIndices = sortDescTValues.getOrigIndx();

			if (!useAllCombs) {
				boolean[] changeSign = new boolean[1];

				Random rand = new Random();
				long[] randomSeeds = new long[numCombs];
				for (int i = 0; i < numCombs; i++) {
					randomSeeds[i] = rand.nextLong();
				}

				for (int i = 0; i < numCombs; i++) {

					Vector validExpts = new Vector();

					for (int j = 0; j < groupAssignments.length; j++) {
						if (groupAssignments[j] == 1) {
							validExpts.add(new Integer(j));
						}
					}

					int[] validArray = new int[validExpts.size()];
					for (int j = 0; j < validArray.length; j++) {
						validArray[j] = ((Integer) (validExpts.get(j)))
								.intValue();
					}

					changeSign = getOneClassChangeSignArray(randomSeeds[i],
							validArray);
					float[][] permutedMatrix = getOneClassPermMatrix(expMatrix,
							changeSign);

					double[] currentPermTValues = getOneClassTValues(permutedMatrix);

					if (Double
							.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
						uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
					} else {
						uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
					}

					for (int j = numGenes - 2; j >= 0; j--) {
						if (Double
								.isNaN(currentPermTValues[descGeneIndices[j]])) {
							uMatrix[j][i] = uMatrix[j + 1][i];
						} else {
							uMatrix[j][i] = Math.max(uMatrix[j + 1][i],
									currentPermTValues[descGeneIndices[j]]);
						}
					}
				}
			} else {
				for (int i = 0; i < numCombs; i++) {

					Vector validExpts = new Vector();
					for (int j = 0; j < groupAssignments.length; j++) {
						if (groupAssignments[j] == 1) {
							validExpts.add(new Integer(j));
						}
					}

					int[] validArray = new int[validExpts.size()];
					for (int j = 0; j < validArray.length; j++) {
						validArray[j] = ((Integer) (validExpts.get(j)))
								.intValue();
					}

					boolean[] changeSign = getOneClassChangeSignArrayAllUniquePerms(
							i, validArray);
					float[][] permutedMatrix = getOneClassPermMatrix(expMatrix,
							changeSign);

					double[] currentPermTValues = getOneClassTValues(permutedMatrix);

					if (Double
							.isNaN(currentPermTValues[descGeneIndices[numGenes - 1]])) {
						uMatrix[numGenes - 1][i] = Double.NEGATIVE_INFINITY;
					} else {
						uMatrix[numGenes - 1][i] = currentPermTValues[descGeneIndices[numGenes - 1]];
					}

					for (int j = numGenes - 2; j >= 0; j--) {
						if (Double
								.isNaN(currentPermTValues[descGeneIndices[j]])) {
							uMatrix[j][i] = uMatrix[j + 1][i];
						} else {
							uMatrix[j][i] = Math.max(uMatrix[j + 1][i],
									currentPermTValues[descGeneIndices[j]]);
						}
					}
				}
			}
		}

		adjPValues = new double[numGenes];

		for (int i = 0; i < numGenes; i++) {
			int pCounter = 0;
			for (int j = 0; j < numCombs; j++) {
				if (uMatrix[i][j] >= descTValues[i]) {
					pCounter++;
				}
			}
			adjPValues[descGeneIndices[i]] = (double) pCounter
					/ (double) numCombs;
		}

		int NaNPCounter = 0;
		for (int i = 0; i < numGenes; i++) {
			if (Double.isNaN(origTValues[i])) {
				adjPValues[i] = Double.NaN;
				NaNPCounter++;
			}
		}
		for (int i = 1; i < numGenes - NaNPCounter; i++) {
			adjPValues[descGeneIndices[i]] = Math.max(
					adjPValues[descGeneIndices[i]],
					adjPValues[descGeneIndices[i - 1]]);
		}

		Vector clusterVector = new Vector();
		Vector sigGenes = new Vector();
		Vector nonSigGenes = new Vector();
		for (int i = 0; i < numGenes; i++) {
			if (Double.isNaN(adjPValues[i])) {
				nonSigGenes.add(new Integer(i));
			} else if ((float) adjPValues[i] <= alpha) {
				sigGenes.add(new Integer(i));
			} else {
				nonSigGenes.add(new Integer(i));
			}
		}

		clusterVector.add(sigGenes);
		clusterVector.add(nonSigGenes);

		k = clusterVector.size();

		float[][] isSigMatrix = new float[numGenes][1];

		for (int i = 0; i < isSigMatrix.length; i++) {
			isSigMatrix[i][0] = 0.0f;
		}

		// Vector sigGenes = (Vector)(clusterVector.get(0));

		for (int i = 0; i < sigGenes.size(); i++) {
			int currentGene = ((Integer) (sigGenes.get(i))).intValue();
			isSigMatrix[currentGene][0] = 1.0f;
		}

		float[][] tValuesMatrix = new float[numGenes][1];
		float[][] pValuesMatrix = new float[numGenes][1];

		float[][] dfMatrix = new float[numGenes][1];
		float[][] meansAMatrix = new float[numGenes][1];
		float[][] meansBMatrix = new float[numGenes][1];
		float[][] sdAMatrix = new float[numGenes][1];
		float[][] sdBMatrix = new float[numGenes][1];
		float[][] oneClassMeansMatrix = new float[numGenes][1];
		float[][] oneClassSDsMatrix = new float[numGenes][1];

		if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
			for (int i = 0; i < numGenes; i++) {
				tValuesMatrix[i][0] = (float) (origTValues[i]);
				pValuesMatrix[i][0] = (float) (adjPValues[i]);
				dfMatrix[i][0] = (float) (getDF(i));
			}
			Vector meansAndSDs = getMeansAndSDs();
			float[] meansA = (float[]) (meansAndSDs.get(0));
			float[] meansB = (float[]) (meansAndSDs.get(1));
			float[] sdA = (float[]) (meansAndSDs.get(2));
			float[] sdB = (float[]) (meansAndSDs.get(3));

			for (int i = 0; i < numGenes; i++) {
				meansAMatrix[i][0] = meansA[i];
				meansBMatrix[i][0] = meansB[i];
				sdAMatrix[i][0] = sdA[i];
				sdBMatrix[i][0] = sdB[i];
			}
		} else if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
			for (int i = 0; i < numGenes; i++) {
				float[] currentGeneValues = getOneClassGeneValues(i);
				tValuesMatrix[i][0] = (float) (origTValues[i]);
				pValuesMatrix[i][0] = (float) (adjPValues[i]);
				dfMatrix[i][0] = (float) (getOneClassDFValue(currentGeneValues));
				oneClassMeansMatrix[i][0] = getMean(currentGeneValues);
				oneClassSDsMatrix[i][0] = (float) (Math
						.sqrt(getVar(currentGeneValues)));
			}
		}

		clusters = new Vector[k];

		for (int i = 0; i < k; i++) {
			clusters[i] = (Vector) (clusterVector.get(i));
		}

		float[][] means = getMeans(clusters);
		float[][] variances = getVariances(clusters, means);

		Hashtable result = new Hashtable();
		result.put("number-of-clusters", String.valueOf(clusters.length));
		result.put("clusters_means", means);
		result.put("clusters_variances", variances);
		result.put("pValues", pValuesMatrix);
		result.put("tValues", tValuesMatrix);
		result.put("dfValues", dfMatrix);
		result.put("meansAMatrix", meansAMatrix);
		result.put("meansBMatrix", meansBMatrix);
		result.put("sdAMatrix", sdAMatrix);
		result.put("sdBMatrix", sdBMatrix);
		result.put("isSigMatrix", isSigMatrix);
		result.put("oneClassMeansMatrix", oneClassMeansMatrix);
		result.put("oneClassSDsMatrix", oneClassSDsMatrix);

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Ttest", result);
		return results;
	}

	public AlgorithmExecutionResults executeMinP() {
		double[] origTValues = new double[numGenes];
		double[] rawPValues = new double[numGenes];
		double[] adjPValues = new double[numGenes];
		double[] sortedRawPValues = new double[1];
		double[][] origTMatrix = new double[numGenes][numCombs];
		double[][] qMatrix = new double[numGenes + 1][numCombs];
		double[][] sortedTMatrix = new double[numGenes][numCombs];
		double[][] pMatrix = new double[numGenes][numCombs];
		int[] sortedRawPValueIndices = new int[1];

		for (int i = 0; i < numCombs; i++) {
			qMatrix[numGenes][i] = 1.0d;
		}

		if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
			for (int i = 0; i < numGenes; i++) {
				origTValues[i] = Math.abs(getTValue(i));
			}
			if (!useAllCombs) {
				for (int i = 0; i < numCombs; i++) {

					int[] permutedExpts = new int[1];
					Vector validExpts = new Vector();

					for (int j = 0; j < groupAssignments.length; j++) {
						if (groupAssignments[j] != TtestAnalysisPanel.NEITHER_GROUP) {
							validExpts.add(new Integer(j));
						}
					}

					int[] validArray = new int[validExpts.size()];
					for (int j = 0; j < validArray.length; j++) {
						validArray[j] = ((Integer) (validExpts.get(j)))
								.intValue();
					}

					permutedExpts = getPermutedValues(numExps, validArray); // returns
					// an
					// int
					// array
					// of
					// size
					// "numExps",
					// with
					// the
					// valid
					// values
					// permuted

					float[][] permutedMatrix = getPermutedMatrix(expMatrix,
							permutedExpts);
					double[] currentPermTValues = getTwoClassUnpairedTValues(permutedMatrix);
					for (int j = 0; j < numGenes; j++) {
						origTMatrix[j][i] = currentPermTValues[j];
					}
				}
			} else {
				int[] permutedExpts = new int[numExps];

				for (int i = 0; i < numExps; i++) {
					permutedExpts[i] = i;
				}

				Vector usedExptsVector = new Vector();
				int numGroupAValues = 0;
				for (int i = 0; i < groupAssignments.length; i++) {
					if (groupAssignments[i] != TtestAnalysisPanel.NEITHER_GROUP) {
						usedExptsVector.add(new Integer(i));
					}
					if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
						numGroupAValues++;
					}
				}
				int[] usedExptsArray = new int[usedExptsVector.size()];

				for (int i = 0; i < usedExptsArray.length; i++) {
					usedExptsArray[i] = ((Integer) (usedExptsVector.get(i)))
							.intValue();
				}

				int[] combArray = new int[numGroupAValues];
				for (int i = 0; i < combArray.length; i++) {
					combArray[i] = -1;
				}

				int numGroupBValues = usedExptsArray.length - numGroupAValues;

				int permCounter = 0;

				while (Combinations.enumerateCombinations(
						usedExptsArray.length, numGroupAValues, combArray)) {

					int[] notInCombArray = new int[numGroupBValues];
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
				double currentTValue = (double) getTValue(i);
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
				for (int j = 0; j < numCombs; j++) {
					sortedTMatrix[i][j] = origTMatrix[sortedRawPValueIndices[i]][j];
				}
			}
		} else if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
			for (int i = 0; i < numGenes; i++) {
				origTValues[i] = Math.abs(getOneClassTValue(i));
			}
			if (!useAllCombs) {
				boolean[] changeSign = new boolean[1];

				Random rand = new Random();
				long[] randomSeeds = new long[numCombs];
				for (int i = 0; i < numCombs; i++) {
					randomSeeds[i] = rand.nextLong();
				}

				for (int i = 0; i < numCombs; i++) {

					Vector validExpts = new Vector();

					for (int j = 0; j < groupAssignments.length; j++) {
						if (groupAssignments[j] == 1) {
							validExpts.add(new Integer(j));
						}
					}

					int[] validArray = new int[validExpts.size()];
					for (int j = 0; j < validArray.length; j++) {
						validArray[j] = ((Integer) (validExpts.get(j)))
								.intValue();
					}

					changeSign = getOneClassChangeSignArray(randomSeeds[i],
							validArray);
					float[][] permutedMatrix = getOneClassPermMatrix(expMatrix,
							changeSign);

					double[] currentPermTValues = getOneClassTValues(permutedMatrix);
					for (int j = 0; j < numGenes; j++) {
						origTMatrix[j][i] = currentPermTValues[j];
					}
				}
			} else {
				for (int i = 0; i < numCombs; i++) {

					Vector validExpts = new Vector();
					for (int j = 0; j < groupAssignments.length; j++) {
						if (groupAssignments[j] == 1) {
							validExpts.add(new Integer(j));
						}
					}

					int[] validArray = new int[validExpts.size()];
					for (int j = 0; j < validArray.length; j++) {
						validArray[j] = ((Integer) (validExpts.get(j)))
								.intValue();
					}

					boolean[] changeSign = getOneClassChangeSignArrayAllUniquePerms(
							i, validArray);
					float[][] permutedMatrix = getOneClassPermMatrix(expMatrix,
							changeSign);

					double[] currentPermTValues = getOneClassTValues(permutedMatrix);
					for (int j = 0; j < numGenes; j++) {
						origTMatrix[j][i] = currentPermTValues[j];
					}
				}
			}

			for (int i = 0; i < numGenes; i++) {
				double currentTValue = (double) getOneClassTValue(i);
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

			org.geworkbench.util.QSort sortRawPValues = new org.geworkbench.util.QSort(
					rawPValues, QSort.ASCENDING);
			sortedRawPValues = sortRawPValues.getSortedDouble();
			sortedRawPValueIndices = sortRawPValues.getOrigIndx();

			for (int i = 0; i < numGenes; i++) {
				for (int j = 0; j < numCombs; j++) {
					sortedTMatrix[i][j] = origTMatrix[sortedRawPValueIndices[i]][j];
				}
			}
		}

		double[] sortedAdjPValues = new double[numGenes];

		int currentGeneCounter = 0;
		for (int i = numGenes - 1; i >= 0; i--) {
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
			double[] currentGeneSortedPVals = getPValsFromOrderStats(sortedCurrentGeneTVals);
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
			sortedAdjPValues[i] = Math.max(sortedAdjPValues[i - 1],
					sortedAdjPValues[i]);
		}

		for (int i = 0; i < sortedAdjPValues.length; i++) {
			adjPValues[i] = sortedAdjPValues[sortedRawPValueIndices[i]];
			if (Double.isNaN(rawPValues[i])) {
				adjPValues[i] = Double.NaN;
			}
		}

		Vector clusterVector = new Vector();
		Vector sigGenes = new Vector();
		Vector nonSigGenes = new Vector();
		for (int i = 0; i < numGenes; i++) {
			if (Double.isNaN(adjPValues[i])) {
				nonSigGenes.add(new Integer(i));
			} else if ((float) adjPValues[i] <= alpha) {
				sigGenes.add(new Integer(i));
			} else {
				nonSigGenes.add(new Integer(i));
			}
		}

		clusterVector.add(sigGenes);
		clusterVector.add(nonSigGenes);

		k = clusterVector.size();

		float[][] isSigMatrix = new float[numGenes][1];

		for (int i = 0; i < isSigMatrix.length; i++) {
			isSigMatrix[i][0] = 0.0f;
		}

		for (int i = 0; i < sigGenes.size(); i++) {
			int currentGene = ((Integer) (sigGenes.get(i))).intValue();
			isSigMatrix[currentGene][0] = 1.0f;
		}

		float[][] tValuesMatrix = new float[numGenes][1];
		float[][] pValuesMatrix = new float[numGenes][1];
		float[][] dfMatrix = new float[numGenes][1];
		float[][] meansAMatrix = new float[numGenes][1];
		float[][] meansBMatrix = new float[numGenes][1];
		float[][] sdAMatrix = new float[numGenes][1];
		float[][] sdBMatrix = new float[numGenes][1];
		float[][] oneClassMeansMatrix = new float[numGenes][1];
		float[][] oneClassSDsMatrix = new float[numGenes][1];

		if (tTestDesign == TtestAnalysisPanel.BETWEEN_SUBJECTS) {
			for (int i = 0; i < numGenes; i++) {
				tValuesMatrix[i][0] = (float) (origTValues[i]);
				pValuesMatrix[i][0] = (float) (adjPValues[i]);
				dfMatrix[i][0] = (float) (getDF(i));
			}
			Vector meansAndSDs = getMeansAndSDs();
			float[] meansA = (float[]) (meansAndSDs.get(0));
			float[] meansB = (float[]) (meansAndSDs.get(1));
			float[] sdA = (float[]) (meansAndSDs.get(2));
			float[] sdB = (float[]) (meansAndSDs.get(3));

			for (int i = 0; i < numGenes; i++) {
				meansAMatrix[i][0] = meansA[i];
				meansBMatrix[i][0] = meansB[i];
				sdAMatrix[i][0] = sdA[i];
				sdBMatrix[i][0] = sdB[i];
			}
		} else if (tTestDesign == TtestAnalysisPanel.ONE_CLASS) {
			for (int i = 0; i < numGenes; i++) {
				float[] currentGeneValues = getOneClassGeneValues(i);
				tValuesMatrix[i][0] = (float) (origTValues[i]);
				pValuesMatrix[i][0] = (float) (adjPValues[i]);
				dfMatrix[i][0] = (float) (getOneClassDFValue(currentGeneValues));
				oneClassMeansMatrix[i][0] = getMean(currentGeneValues);
				oneClassSDsMatrix[i][0] = (float) (Math
						.sqrt(getVar(currentGeneValues)));
			}
		}

		clusters = new Vector[k];

		for (int i = 0; i < k; i++) {
			clusters[i] = (Vector) (clusterVector.get(i));
		}

		float[][] means = getMeans(clusters);
		float[][] variances = getVariances(clusters, means);

		Hashtable result = new Hashtable();
		result.put("number-of-clusters", String.valueOf(clusters.length));
		result.put("clusters_means", means);
		result.put("clusters_variances", variances);
		result.put("pValues", pValuesMatrix);
		result.put("tValues", tValuesMatrix);
		result.put("dfValues", dfMatrix);
		result.put("meansAMatrix", meansAMatrix);
		result.put("meansBMatrix", meansBMatrix);
		result.put("sdAMatrix", sdAMatrix);
		result.put("sdBMatrix", sdBMatrix);
		result.put("isSigMatrix", isSigMatrix);
		result.put("oneClassMeansMatrix", oneClassMeansMatrix);
		result.put("oneClassSDsMatrix", oneClassSDsMatrix);

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Ttest", result);
		return results;
	}

	public void abort() {
		stop = true;
	}

	private double[] getPValsFromOrderStats(double[] sortedTVals) {
		double[] pVals = new double[sortedTVals.length];
		int[] ranksArray = new int[sortedTVals.length];

		if (Double.isNaN(sortedTVals[0])) {
			for (int i = 0; i < pVals.length; i++) {
				pVals[i] = Double.NaN;
			}
			return pVals;
		}

		Vector ranksVector = new Vector();
		Vector ranksCounterVector = new Vector();
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

	private double[] getTwoClassUnpairedTValues(float[][] inputMatrix) {
		double[] tValsFromMatrix = new double[numGenes];
		for (int i = 0; i < numGenes; i++) {
			tValsFromMatrix[i] = Math.abs(getTValue(i, inputMatrix));
		}

		return tValsFromMatrix;
	}

	private float[][] getPermutedMatrix(float[][] inputMatrix, int[] permExpts) {
		float[][] permutedMatrix = new float[inputMatrix.length][inputMatrix[0].length];
		for (int i = 0; i < inputMatrix.length; i++) {
			for (int j = 0; j < inputMatrix[0].length; j++) {
				permutedMatrix[i][j] = inputMatrix[i][permExpts[j]];
			}
		}
		return permutedMatrix;
	}

	private int[] getPermutedValues(int arrayLength, int[] validArray) {
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

	private float[][] getMeans(Vector[] clusters) {
		float[][] means = new float[clusters.length][numExps];
		float[][] mean;
		for (int i = 0; i < clusters.length; i++) {
			mean = getMean(clusters[i]);
			means[i] = mean[0];
		}
		return means;
	}

	private float[][] getMean(Vector cluster) {
		float[][] mean = new float[1][numExps];
		float currentMean;
		int n = cluster.size();
		int denom = 0;
		float value;
		for (int i = 0; i < numExps; i++) {
			currentMean = 0f;
			denom = 0;
			for (int j = 0; j < n; j++) {
				value = expMatrix[((Integer) cluster.get(j)).intValue()][i];
				if (!Float.isNaN(value)) {
					currentMean += value;
					denom++;
				}
			}
			mean[0][i] = currentMean / (float) denom;
		}

		return mean;
	}

	private float[][] getVariances(Vector[] clusters, float[][] means) {
		final int rows = means.length;
		final int columns = means[0].length;
		float[][] variances = new float[rows][columns];
		for (int row = 0; row < rows; row++) {
			for (int column = 0; column < columns; column++) {
				variances[row][column] = getSampleVariance(clusters[row],
						column, means[row][column]);
			}
		}
		return variances;
	}

	private int validN;

	private float getSampleNormalizedSum(Vector cluster, int column, float mean) {
		final int size = cluster.size();
		float sum = 0f;
		float value;
		validN = 0;
		for (int i = 0; i < size; i++) {
			value = expMatrix[((Integer) cluster.get(i)).intValue()][column];
			if (!Float.isNaN(value)) {
				sum += Math.pow(value - mean, 2);
				validN++;
			}
		}
		return sum;
	}

	private float getSampleVariance(Vector cluster, int column, float mean) {
		return (float) Math.sqrt(getSampleNormalizedSum(cluster, column, mean)
				/ (float) (validN - 1));
	}

	private Vector sortGenesForOneClassDesign() {
		Vector sigGenes = new Vector();
		Vector nonSigGenes = new Vector();
		pValuesVector = new Vector();
		if (!isPermut) {
			if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
					|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
				for (int i = 0; i < numGenes; i++) {
					if (isSigOneClass(i)) {
						sigGenes.add(new Integer(i));
					} else {
						nonSigGenes.add(new Integer(i));
					}
				}
			} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
				float[] pValues = new float[numGenes];
				for (int i = 0; i < numGenes; i++) {
					float[] currentGeneValues = getOneClassGeneValues(i);
					float currentOneClassT = (float) getOneClassTValue(currentGeneValues);
					float currentOneClassDF = (float) getOneClassDFValue(currentGeneValues);
					float currentOneClassProb = getProb(currentOneClassT,
							(int) currentOneClassDF);
					pValues[i] = currentOneClassProb;
				}

				for (int i = 0; i < pValues.length; i++) {
					pValuesVector.add(new Float(pValues[i]));
				}
				int denomAlpha = numGenes;
				double adjAlpha = alpha / (double) denomAlpha;

				QSort sortPVals = new QSort(pValues);
				float[] sortedPValues = sortPVals.getSorted();
				int[] sortedIndices = sortPVals.getOrigIndx();

				for (int i = (sortedPValues.length - 1); i >= 0; i--) {
					if (sortedPValues[i] <= adjAlpha) {
						sigGenes.add(new Integer(sortedIndices[i]));
					} else {
						nonSigGenes.add(new Integer(sortedIndices[i]));
					}
					if (i < sortedPValues.length - 1) {
						if (sortedPValues[i] < sortedPValues[i + 1]) {
							denomAlpha--;
							if (denomAlpha < 1) {
								System.out.println("Warning: denomAlpha = "
										+ denomAlpha);
							}
						} else {
						}
					} else {
						if (denomAlpha < 1) {
							System.out.println("Warning: denomAlpha = "
									+ denomAlpha);
						}
					}
					adjAlpha = alpha / denomAlpha;
				}
			}
		} else {
			if (useAllCombs) {
				if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
						|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
					for (int i = 0; i < numGenes; i++) {
						if (significanceMethod == TtestAnalysisPanel.JUST_ALPHA) {
							float currentProb = getAllCombsOneClassProb(i);
							pValuesVector.add(new Float(currentProb));
							if (currentProb <= alpha) {
								sigGenes.add(new Integer(i));
							} else {
								nonSigGenes.add(new Integer(i));
							}
						} else if (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI) {
							float currentProb = getAllCombsOneClassProb(i);
							pValuesVector.add(new Float(currentProb));
							float thresh = (float) (alpha / (double) numGenes);
							if (currentProb <= thresh) {
								sigGenes.add(new Integer(i));
							} else {
								nonSigGenes.add(new Integer(i));
							}
						}
					}
				} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
					float[] pValues = new float[numGenes];
					for (int i = 0; i < numGenes; i++) {
						pValues[i] = getAllCombsOneClassProb(i);
					}
					for (int i = 0; i < pValues.length; i++) {
						pValuesVector.add(new Float(pValues[i]));
					}
					int denomAlpha = numGenes;
					double adjAlpha = alpha / (double) denomAlpha;

					QSort sortPVals = new QSort(pValues);
					float[] sortedPValues = sortPVals.getSorted();
					int[] sortedIndices = sortPVals.getOrigIndx();

					for (int i = (sortedPValues.length - 1); i >= 0; i--) {
						if (sortedPValues[i] <= adjAlpha) {
							sigGenes.add(new Integer(sortedIndices[i]));
						} else {
							nonSigGenes.add(new Integer(sortedIndices[i]));
						}

						if (i < sortedPValues.length - 1) {
							if (sortedPValues[i] < sortedPValues[i + 1]) {
								denomAlpha--;
								if (denomAlpha < 1) {
									System.out.println("Warning: denomAlpha = "
											+ denomAlpha);
								}
							} else {
							}
						} else {
							if (denomAlpha < 1) {
								System.out.println("Warning: denomAlpha = "
										+ denomAlpha);
							}
						}
						adjAlpha = alpha / denomAlpha;
					}
				}
			} else {
				if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
						|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
					for (int i = 0; i < numGenes; i++) {
						float currentProb = getSomeCombsOneClassProb(i);
						pValuesVector.add(new Float(currentProb));

						if (significanceMethod == TtestAnalysisPanel.JUST_ALPHA) {
							if (currentProb <= alpha) {
								sigGenes.add(new Integer(i));
							} else {
								nonSigGenes.add(new Integer(i));
							}
						} else if (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI) {
							float thresh = (float) (alpha / (double) numGenes);
							if (currentProb <= thresh) {
								sigGenes.add(new Integer(i));
							} else {
								nonSigGenes.add(new Integer(i));
							}
						}
					}
				} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
					float[] pValues = new float[numGenes];
					for (int i = 0; i < numGenes; i++) {
						pValues[i] = getSomeCombsOneClassProb(i);
					}
					for (int i = 0; i < pValues.length; i++) {
						pValuesVector.add(new Float(pValues[i]));
					}
					int denomAlpha = numGenes;
					double adjAlpha = alpha / (double) denomAlpha;

					QSort sortPVals = new QSort(pValues);
					float[] sortedPValues = sortPVals.getSorted();
					int[] sortedIndices = sortPVals.getOrigIndx();

					for (int i = (sortedPValues.length - 1); i >= 0; i--) {
						if (sortedPValues[i] <= adjAlpha) {
							sigGenes.add(new Integer(sortedIndices[i]));
						} else {
							nonSigGenes.add(new Integer(sortedIndices[i]));
						}

						if (i < sortedPValues.length - 1) {
							if (sortedPValues[i] < sortedPValues[i + 1]) {
								denomAlpha--;
								if (denomAlpha < 1) {
									System.out.println("Warning: denomAlpha = "
											+ denomAlpha);
								}
							} else {
							}
						} else {
							if (denomAlpha < 1) {
								System.out.println("Warning: denomAlpha = "
										+ denomAlpha);
							}
						}
						adjAlpha = alpha / denomAlpha;
					}
				}
			}
		}

		Vector sortedGenes = new Vector();
		sortedGenes.add(sigGenes);
		sortedGenes.add(nonSigGenes);

		return sortedGenes;
	}

	private float getSomeCombsOneClassProb(int gene) {
		float[] origGeneValues = getOneClassGeneValues(gene);
		float origOneClassT = (float) Math
				.abs(getOneClassTValue(origGeneValues));
		if (Float.isNaN(origOneClassT)) {
			return Float.NaN;
		}

		Random rand = new Random();
		long[] randomSeeds = new long[numCombs];
		for (int i = 0; i < numCombs; i++) {
			randomSeeds[i] = rand.nextLong();
		}

		int exceedCount = 0;
		for (int i = 0; i < numCombs; i++) {
			boolean[] changeSign = getSomeCombsPermutArray(randomSeeds[i]);
			float[] randomizedGene = new float[origGeneValues.length];

			for (int l = 0; l < changeSign.length; l++) {
				if (changeSign[l]) {
					randomizedGene[l] = (float) (origGeneValues[l] - 2.0f * (origGeneValues[l] - oneClassMean));
				} else {
					randomizedGene[l] = origGeneValues[l];
				}
			}
			double randTValue = Math.abs(getOneClassTValue(randomizedGene));
			if (randTValue > origOneClassT) {
				exceedCount++;
			}
		}

		double prob = (double) exceedCount / (double) numCombs;

		return (float) prob;
	}

	private boolean[] getSomeCombsPermutArray(long seed) {
		boolean[] boolArray = new boolean[getNumValidOneClassExpts()];
		for (int i = 0; i < boolArray.length; i++) {
			boolArray[i] = false;
		}

		Random generator2 = new Random(seed);
		for (int i = 0; i < boolArray.length; i++) {
			boolArray[i] = generator2.nextBoolean();
		}
		return boolArray;
	}

	private float getAllCombsOneClassProb(int gene) {
		int validNumExps = getNumValidOneClassExpts();
		int numAllPossOneClassPerms = (int) (Math.pow(2, validNumExps));
		float[] currentGene = expMatrix[gene];
		float[] origGeneValues = getOneClassGeneValues(gene);
		float origOneClassT = (float) Math
				.abs(getOneClassTValue(origGeneValues));
		if (Float.isNaN(origOneClassT)) {
			return Float.NaN;
		}
		int exceedCount = 0;

		for (int j = 0; j < numAllPossOneClassPerms; j++) {
			boolean[] changeSign = getOneClassPermutArray(j);
			float[] randomizedGene = new float[currentGene.length];

			for (int l = 0; l < changeSign.length; l++) {
				if (changeSign[l]) {
					randomizedGene[l] = (float) (currentGene[l] - 2.0f * (currentGene[l] - oneClassMean));
				} else {
					randomizedGene[l] = currentGene[l];
				}
			}

			float[] reducedRandGene = new float[validNumExps];
			int count = 0;
			for (int l = 0; l < groupAssignments.length; l++) {
				if (groupAssignments[l] == 1) {
					reducedRandGene[count] = randomizedGene[l];
					count++;
				}
			}

			double randTValue = Math.abs(getOneClassTValue(reducedRandGene));
			if (randTValue > origOneClassT) {
				exceedCount++;
			}

		}

		double prob = (double) exceedCount / (double) numAllPossOneClassPerms;
		return (float) prob;
	}

	private boolean[] getOneClassChangeSignArray(long seed, int[] validExpts) {
		boolean[] changeSignArray = new boolean[numExps];
		for (int i = 0; i < changeSignArray.length; i++) {
			changeSignArray[i] = false;
		}
		Random generator2 = new Random(seed);
		for (int i = 0; i < validExpts.length; i++) {
			changeSignArray[validExpts[i]] = generator2.nextBoolean();
		}
		return changeSignArray;
	}

	private float[][] getOneClassPermMatrix(float[][] inputMatrix,
			boolean[] changeSign) {
		float[][] permutedMatrix = new float[inputMatrix.length][inputMatrix[0].length];
		for (int i = 0; i < inputMatrix.length; i++) {
			for (int j = 0; j < inputMatrix[0].length; j++) {
				if (changeSign[j]) {
					permutedMatrix[i][j] = (float) (inputMatrix[i][j] - 2.0f * (inputMatrix[i][j] - oneClassMean));
				} else {
					permutedMatrix[i][j] = inputMatrix[i][j];
				}
			}
		}

		return permutedMatrix;
	}

	private boolean[] getOneClassChangeSignArrayAllUniquePerms(int num,
			int[] validExpts) {
		boolean[] changeSignArray = new boolean[numExps];
		for (int i = 0; i < changeSignArray.length; i++) {
			changeSignArray[i] = false;
		}

		int numValidExps = validExpts.length;

		String binaryString = Integer.toBinaryString(num);
		char[] binArray = binaryString.toCharArray();
		if (binArray.length < numValidExps) {
			Vector binVector = new Vector();
			for (int i = 0; i < (numValidExps - binArray.length); i++) {
				binVector.add(new Character('0'));
			}

			for (int i = 0; i < binArray.length; i++) {
				binVector.add(new Character(binArray[i]));
			}
			binArray = new char[binVector.size()];

			for (int i = 0; i < binArray.length; i++) {
				binArray[i] = ((Character) (binVector.get(i))).charValue();
			}
		}

		for (int i = 0; i < validExpts.length; i++) {
			if (binArray[i] == '1') {
				changeSignArray[validExpts[i]] = true;
			} else {
				changeSignArray[validExpts[i]] = false;
			}
		}

		return changeSignArray;
	}

	boolean[] getOneClassPermutArray(int num) {
		boolean[] oneClassPermutArray = new boolean[numExps];

		for (int i = 0; i < oneClassPermutArray.length; i++) {
			oneClassPermutArray[i] = false;
		}

		int validNumExps = getNumValidOneClassExpts();

		String binaryString = Integer.toBinaryString(num);
		char[] binArray = binaryString.toCharArray();
		if (binArray.length < validNumExps) {
			Vector binVector = new Vector();
			for (int i = 0; i < (validNumExps - binArray.length); i++) {
				binVector.add(new Character('0'));
			}

			for (int i = 0; i < binArray.length; i++) {
				binVector.add(new Character(binArray[i]));
			}
			binArray = new char[binVector.size()];

			for (int i = 0; i < binArray.length; i++) {
				binArray[i] = ((Character) (binVector.get(i))).charValue();
			}
		}
		int counter = 0;

		for (int i = 0; i < oneClassPermutArray.length; i++) {
			if (groupAssignments[i] == 1) {
				if (binArray[counter] == '1') {
					oneClassPermutArray[i] = true;
				} else {
					oneClassPermutArray[i] = false;
				}
				counter++;
			}
		}
		return oneClassPermutArray;
	}

	public int getNumValidOneClassExpts() {
		int validNum = 0;
		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == 1) {
				validNum++;
			}
		}
		return validNum;
	}

	private float[] getOneClassGeneValues(int gene) {
		Vector currentGene = new Vector();
		for (int i = 0; i < numExps; i++) {
			if (groupAssignments[i] == 1) {
				currentGene.add(new Float(expMatrix[gene][i]));
			}
		}
		float[] currGeneArray = new float[currentGene.size()];
		for (int i = 0; i < currGeneArray.length; i++) {
			currGeneArray[i] = ((Float) (currentGene.get(i))).floatValue();
		}
		return currGeneArray;
	}

	private boolean isSigOneClass(int gene) {
		boolean isSig = false;
		Vector currentGene = new Vector();
		for (int i = 0; i < numExps; i++) {
			if (groupAssignments[i] == 1) {
				currentGene.add(new Float(expMatrix[gene][i]));
			}
		}

		float[] currGeneArray = new float[currentGene.size()];
		for (int i = 0; i < currGeneArray.length; i++) {
			currGeneArray[i] = ((Float) (currentGene.get(i))).floatValue();
		}

		double tValue = getOneClassTValue(currGeneArray);

		if (Double.isNaN(tValue)) {
			pValuesVector.add(new Float(Float.NaN));
			return false;
		}

		int validNum = 0;
		for (int i = 0; i < currGeneArray.length; i++) {
			if (!Float.isNaN(currGeneArray[i])) {
				validNum++;
			}
		}

		int df = validNum - 1;
		double prob;
		TDistribution tDist = new TDistribution(df);
		double cumulP = tDist.cumulative(Math.abs(tValue));
		prob = 2 * (1 - cumulP); // two-tailed test
		if (prob > 1) {
			prob = 1;
		}

		pValuesVector.add(new Float((float) prob));

		if (significanceMethod == TtestAnalysisPanel.JUST_ALPHA) {
			if (prob <= alpha) {
				isSig = true;
			} else {
				isSig = false;
			}
		} else if (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI) {
			double thresh = alpha / (double) numGenes;
			if (prob <= thresh) {
				isSig = true;
			} else {
				isSig = false;
			}
		}
		return isSig;
	}

	private double getOneClassTValue(int gene, float[][] inputMatrix) {
		Vector currentGene = new Vector();

		for (int i = 0; i < numExps; i++) {
			if (groupAssignments[i] == 1) {
				currentGene.add(new Float(inputMatrix[gene][i]));
			}
		}

		float[] currGeneArray = new float[currentGene.size()];

		for (int i = 0; i < currGeneArray.length; i++) {
			currGeneArray[i] = ((Float) (currentGene.get(i))).floatValue();
		}

		return getOneClassTValue(currGeneArray);
	}

	private double getOneClassTValue(int gene) {
		float[] currentGene = getOneClassGeneValues(gene);
		return getOneClassTValue(currentGene);
	}

	private double getOneClassTValue(float[] geneArray) {
		double tValue;
		float mean = getMean(geneArray);
		double stdDev = Math.sqrt((double) (getVar(geneArray)));
		int validNum = 0;
		for (int i = 0; i < geneArray.length; i++) {
			if (!Float.isNaN(geneArray[i])) {
				validNum++;
			}
		}
		double stdErr = stdDev / (Math.sqrt(validNum));
		tValue = ((double) (mean - oneClassMean)) / stdErr;
		return Math.abs(tValue);
	}

	double[] getOneClassTValues(float[][] inputMatrix) {
		double[] tValsFromMatrix = new double[numGenes];
		for (int i = 0; i < numGenes; i++) {
			tValsFromMatrix[i] = Math.abs(getOneClassTValue(i, inputMatrix));
		}

		return tValsFromMatrix;
	}

	private int getOneClassDFValue(float[] geneArray) {
		int validNum = 0;
		for (int i = 0; i < geneArray.length; i++) {
			if (!Float.isNaN(geneArray[i])) {
				validNum++;
			}
		}
		int df = validNum - 1;
		return df;
	}

	private float getProb(float tValue, int df) {
		TDistribution tDist = new TDistribution(df);
		double cumulP = tDist.cumulative(Math.abs((double) tValue));
		double prob = 2 * (1 - cumulP);
		if (prob > 1) {
			prob = 1;
		}

		return (float) prob;
	}

	private Vector sortGenesBySignificance() {
		Vector sigGenes = new Vector();
		Vector nonSigGenes = new Vector();

		if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
				|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
			sigGenes = new Vector();
			nonSigGenes = new Vector();
			for (int i = 0; i < numGenes; i++) {
				if (isSignificant(i)) {
					sigGenes.add(new Integer(i));
					sigTValues.add(new Float(currentT));
					sigPValues.add(new Float(currentP));
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				} else {
					nonSigGenes.add(new Integer(i));
					nonSigTValues.add(new Float(currentT));
					nonSigPValues.add(new Float(currentP));
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				}
			}

		} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
			sigGenes = new Vector();
			nonSigGenes = new Vector();
			float[] tValues = new float[numGenes];
			for (int i = 0; i < numGenes; i++) {
				tValues[i] = Math.abs(getTValue(i));
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
				dF = getDF(sortedUniqueIDs[i]);
				if ((Float.isNaN(sortedTValues[i]))
						|| (Float.isNaN((new Integer(dF)).floatValue()))
						|| (dF <= 0)) {
					nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
					nonSigTValues.add(new Float(sortedTValues[i]));
					nonSigPValues.add(new Float(Float.NaN));
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
						sigTValues.add(new Float(sortedTValues[i]));
						sigPValues.add(new Float(prob));
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					} else {
						nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
						nonSigTValues.add(new Float(sortedTValues[i]));
						nonSigPValues.add(new Float(prob));
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					}
					if (sortedTValues[i] > sortedTValues[i - 1]) {
						denomAlpha--;
						if (denomAlpha < 1) {
							System.out.println("Warning: denomAlpha = "
									+ denomAlpha);
						}
					}
				}
			}
			dF = getDF(sortedUniqueIDs[0]);
			if ((Float.isNaN(sortedTValues[0]))
					|| (Float.isNaN((new Integer(dF)).floatValue()))
					|| (dF <= 0)) {
				nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
				nonSigTValues.add(new Float(sortedTValues[0]));
				nonSigPValues.add(new Float(Float.NaN));
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
					sigTValues.add(new Float(sortedTValues[0]));
					sigPValues.add(new Float(prob));
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				} else {
					nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
					nonSigTValues.add(new Float(sortedTValues[0]));
					nonSigPValues.add(new Float(prob));
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				}
			}
			tValuesVector = new Vector();
			pValuesVector = new Vector();

			for (int i = 0; i < tValuesArray.length; i++) {
				tValuesVector.add(new Float(tValuesArray[i]));
				pValuesVector.add(new Float(pValuesArray[i]));
			}

			criticalPValue = adjAlpha;
		}

		Vector sortedGenes = new Vector();
		sortedGenes.add(sigGenes);
		sortedGenes.add(nonSigGenes);

		return sortedGenes;
	}

	private Vector sortGenesByPermutationSignificance() {
		Vector sigGenes = new Vector();
		Vector nonSigGenes = new Vector();

		if ((significanceMethod == TtestAnalysisPanel.JUST_ALPHA)
				|| (significanceMethod == TtestAnalysisPanel.STD_BONFERRONI)) {
			sigGenes = new Vector();
			nonSigGenes = new Vector();
			for (int i = 0; i < numGenes; i++) {
				if (isSignificantByPermutation(i)) {
					sigGenes.add(new Integer(i));
					sigTValues.add(new Float(currentT));
					sigPValues.add(new Float(currentP));
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				} else {
					nonSigGenes.add(new Integer(i));
					nonSigTValues.add(new Float(currentT));
					nonSigPValues.add(new Float(currentP));
					tValuesVector.add(new Float(currentT));
					pValuesVector.add(new Float(currentP));
				}
			}
		} else if (significanceMethod == TtestAnalysisPanel.ADJ_BONFERRONI) {
			sigGenes = new Vector();
			nonSigGenes = new Vector();
			float[] tValues = new float[numGenes];
			for (int i = 0; i < numGenes; i++) {
				tValues[i] = Math.abs(getTValue(i));
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
				if (Float.isNaN(sortedTValues[i])) {
					nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
					nonSigTValues.add(new Float(sortedTValues[i]));
					nonSigPValues.add(new Float(Float.NaN));
					tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
					pValuesArray[sortedUniqueIDs[i]] = Float.NaN;
				} else {
					prob = getPermutedProb(sortedUniqueIDs[i]);
					adjAlpha = alpha / (double) denomAlpha;
					if (prob <= adjAlpha) {
						sigGenes.add(new Integer(sortedUniqueIDs[i]));
						sigTValues.add(new Float(currentT));
						sigPValues.add(new Float(prob));
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					} else {
						nonSigGenes.add(new Integer(sortedUniqueIDs[i]));
						nonSigTValues.add(new Float(currentT));
						nonSigPValues.add(new Float(prob));
						tValuesArray[sortedUniqueIDs[i]] = sortedTValues[i];
						pValuesArray[sortedUniqueIDs[i]] = prob;
					}

					if (sortedTValues[i] > sortedTValues[i - 1]) {
						denomAlpha--;
						if (denomAlpha < 1) {
							System.out.println("Warning: denomAlpha = "
									+ denomAlpha);
						}
					}
				}
			}

			if (Float.isNaN(sortedTValues[0])) {
				nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
				nonSigTValues.add(new Float(sortedTValues[0]));
				nonSigPValues.add(new Float(Float.NaN));
				tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
				pValuesArray[sortedUniqueIDs[0]] = Float.NaN;
			} else {
				prob = getPermutedProb(sortedUniqueIDs[0]);
				adjAlpha = alpha / (double) denomAlpha;
				if (prob <= adjAlpha) {
					sigGenes.add(new Integer(sortedUniqueIDs[0]));
					sigTValues.add(new Float(currentT));
					sigPValues.add(new Float(prob));
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				} else {
					nonSigGenes.add(new Integer(sortedUniqueIDs[0]));
					nonSigTValues.add(new Float(currentT));
					nonSigPValues.add(new Float(prob));
					tValuesArray[sortedUniqueIDs[0]] = sortedTValues[0];
					pValuesArray[sortedUniqueIDs[0]] = prob;
				}
			}
			tValuesVector = new Vector();
			pValuesVector = new Vector();

			for (int i = 0; i < tValuesArray.length; i++) {
				tValuesVector.add(new Float(tValuesArray[i]));
				pValuesVector.add(new Float(pValuesArray[i]));
			}
		}

		Vector sortedGenes = new Vector();
		sortedGenes.add(sigGenes);
		sortedGenes.add(nonSigGenes);

		return sortedGenes;
	}

	private double getPermutedProb(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBCounter++;
			}
		}

		float[] groupAValues = new float[groupACounter];
		float[] groupBValues = new float[groupBCounter];
		int[] groupedExpts = new int[(groupACounter + groupBCounter)];

		groupACounter = 0;
		groupBCounter = 0;
		int groupedExptsCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				groupACounter++;
				groupedExpts[groupedExptsCounter] = i;
				groupedExptsCounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
				groupBCounter++;
				groupedExpts[groupedExptsCounter] = i;
				groupedExptsCounter++;
			}
		}

		float tValue = Math.abs(calculateTValue(groupAValues, groupBValues));
		currentT = tValue;
		double permutProb;
		permutProb = 0;
		if (useAllCombs) {
			int numCombsCounter = 0;
			int[] combArray = new int[groupAValues.length];
			for (int i = 0; i < combArray.length; i++) {
				combArray[i] = -1;
			}
			while (Combinations.enumerateCombinations(groupedExpts.length,
					groupAValues.length, combArray)) {
				float[] resampGroupA = new float[groupAValues.length];
				float[] resampGroupB = new float[groupBValues.length];
				int[] notInCombArray = new int[groupBValues.length];
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
				float resampTValue = Math.abs(calculateTValue(resampGroupA,
						resampGroupB));
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
						groupedExpts, groupAValues.length, groupBValues.length);
				float randomizedTValue = Math.abs(calculateTValue(
						randomGroups[0], randomGroups[1]));
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

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBCounter++;
			}
		}

		float[] groupAValues = new float[groupACounter];
		float[] groupBValues = new float[groupBCounter];
		int[] groupedExpts = new int[(groupACounter + groupBCounter)];
		int numbValidValuesA = 0;
		int numbValidValuesB = 0;

		groupACounter = 0;
		groupBCounter = 0;
		int groupedExptsCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesA++;
				}
				groupACounter++;
				groupedExpts[groupedExptsCounter] = i;
				groupedExptsCounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
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

		float tValue = Math.abs(calculateTValue(groupAValues, groupBValues));
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
			int[] combArray = new int[groupAValues.length];
			for (int i = 0; i < combArray.length; i++) {
				combArray[i] = -1;
			}
			while (Combinations.enumerateCombinations(groupedExpts.length,
					groupAValues.length, combArray)) {
				float[] resampGroupA = new float[groupAValues.length];
				float[] resampGroupB = new float[groupBValues.length];
				int[] notInCombArray = new int[groupBValues.length];
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
				float resampTValue = Math.abs(calculateTValue(resampGroupA,
						resampGroupB));
				if (tValue < resampTValue) {
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
						groupedExpts, groupAValues.length, groupBValues.length);
				float randomizedTValue = Math.abs(calculateTValue(
						randomGroups[0], randomGroups[1]));
				if (tValue < randomizedTValue) {
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

	private float[][] randomlyPermute(float[] gene, int[] groupedExpts,
			int groupALength, int groupBLength) {
		float[][] groupedValues = new float[2][];
		groupedValues[0] = new float[groupALength];
		groupedValues[1] = new float[groupBLength];
		if (groupALength > groupBLength) {
			groupedValues[0] = new float[groupBLength];
			groupedValues[1] = new float[groupALength];
		}

		Vector groupedExptsVector = new Vector();
		for (int i = 0; i < groupedExpts.length; i++) {
			groupedExptsVector.add(new Integer(groupedExpts[i]));
		}

		for (int i = 0; i < groupedValues[0].length; i++) {
			int randInt = (int) Math.round(Math.random()
					* (groupedExptsVector.size() - 1));
			int randIndex = ((Integer) groupedExptsVector.remove(randInt))
					.intValue();
			groupedValues[0][i] = gene[randIndex];
		}

		for (int i = 0; i < groupedValues[1].length; i++) {
			int index = ((Integer) groupedExptsVector.get(i)).intValue();
			groupedValues[1][i] = gene[index];
		}

		return groupedValues;
	}

	private boolean belongsInArray(int i, int[] arr) {
		boolean belongs = false;

		for (int j = 0; j < arr.length; j++) {
			if (i == arr[j]) {
				belongs = true;
				break;
			}
		}
		return belongs;
	}

	private float getTValue(int gene, float[][] inputMatrix) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = inputMatrix[gene][i];
		}

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBCounter++;
			}
		}

		float[] groupAValues = new float[groupACounter];
		float[] groupBValues = new float[groupBCounter];

		groupACounter = 0;
		groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
				groupBCounter++;
			}
		}

		float tValue = calculateTValue(groupAValues, groupBValues);
		return tValue;
	}

	private float getTValue(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBCounter++;
			}
		}

		float[] groupAValues = new float[groupACounter];
		float[] groupBValues = new float[groupBCounter];

		groupACounter = 0;
		groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
				groupBCounter++;
			}
		}

		float tValue = calculateTValue(groupAValues, groupBValues);
		return tValue;
	}

	private int getDF(int gene) {
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBCounter++;
			}
		}

		float[] groupAValues = new float[groupACounter];
		float[] groupBValues = new float[groupBCounter];

		groupACounter = 0;
		groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
				groupBCounter++;
			}
		}

		int df = calculateDf(groupAValues, groupBValues);

		return df;
	}

	private Vector getMeansAndSDs() {
		float[] meansA = new float[numGenes];
		float[] meansB = new float[numGenes];
		float[] sdA = new float[numGenes];
		float[] sdB = new float[numGenes];
		for (int i = 0; i < numGenes; i++) {
			float[] geneValues = new float[numExps];
			for (int j = 0; j < numExps; j++) {
				geneValues[j] = expMatrix[i][j];
			}

			int groupACounter = 0;
			int groupBCounter = 0;

			for (int j = 0; j < groupAssignments.length; j++) {
				if (groupAssignments[j] == TtestAnalysisPanel.GROUP_A) {
					groupACounter++;
				} else if (groupAssignments[j] == TtestAnalysisPanel.GROUP_B) {
					groupBCounter++;
				}
			}

			float[] groupAValues = new float[groupACounter];
			float[] groupBValues = new float[groupBCounter];

			groupACounter = 0;
			groupBCounter = 0;

			for (int j = 0; j < groupAssignments.length; j++) {
				if (groupAssignments[j] == TtestAnalysisPanel.GROUP_A) {
					groupAValues[groupACounter] = geneValues[j];
					groupACounter++;
				} else if (groupAssignments[j] == TtestAnalysisPanel.GROUP_B) {
					groupBValues[groupBCounter] = geneValues[j];
					groupBCounter++;
				}
			}

			meansA[i] = getMean(groupAValues);
			meansB[i] = getMean(groupBValues);
			sdA[i] = (float) (Math.sqrt(getVar(groupAValues)));
			sdB[i] = (float) (Math.sqrt(getVar(groupBValues)));
		}

		Vector meansAndSDs = new Vector();
		meansAndSDs.add(meansA);
		meansAndSDs.add(meansB);
		meansAndSDs.add(sdA);
		meansAndSDs.add(sdB);

		return meansAndSDs;
	}

	private boolean isSignificant(int gene) {
		boolean sig = false;
		float[] geneValues = new float[numExps];
		for (int i = 0; i < numExps; i++) {
			geneValues[i] = expMatrix[gene][i];
		}

		int groupACounter = 0;
		int groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBCounter++;
			}
		}

		float[] groupAValues = new float[groupACounter];
		float[] groupBValues = new float[groupBCounter];

		int numbValidValuesA = 0;
		int numbValidValuesB = 0;

		groupACounter = 0;
		groupBCounter = 0;

		for (int i = 0; i < groupAssignments.length; i++) {
			if (groupAssignments[i] == TtestAnalysisPanel.GROUP_A) {
				groupAValues[groupACounter] = geneValues[i];
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesA++;
				}
				groupACounter++;
			} else if (groupAssignments[i] == TtestAnalysisPanel.GROUP_B) {
				groupBValues[groupBCounter] = geneValues[i];
				if (!Float.isNaN(geneValues[i])) {
					numbValidValuesB++;
				}
				groupBCounter++;
			}
		}

		if ((numbValidValuesA < 2) || (numbValidValuesB < 2)) {
			currentP = Float.NaN;
			currentT = Float.NaN;
			return false;
		}

		float tValue = calculateTValue(groupAValues, groupBValues);
		currentT = tValue;
		int df = calculateDf(groupAValues, groupBValues);
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
					prob = 1;
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

	private float calculateTValue(float[] groupA, float[] groupB) {
		int kA = groupA.length;
		int kB = groupB.length;
		float meanA = getMean(groupA);
		float meanB = getMean(groupB);
		float varA = getVar(groupA);
		float varB = getVar(groupB);

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

		return Math.abs(tValue);
	}

	private int calculateDf(float[] groupA, float[] groupB) {
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

	private float getMean(float[] group) {
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

	private float getVar(float[] group) {
		float mean = getMean(group);
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

	private String GenerateHistoryHeader() {

		String histStr = "";
		// Header
		histStr += "T Test run with the following parameters:\n";
		histStr += "----------------------------------------\n";

		if (useWelchDf)
			histStr += "Group Variances: Unequal(Welch approximation)" + "\n";
		else
			histStr += "Group Variances: Equal" + "\n";

		histStr += "P-Values Parameters:" + "\n";
		if (isPermut)
			histStr += "\t" + "permutation is selected" + "\n";
		else
			histStr += "\t" + "t-distribution is selected" + "\n";
		if (useAllCombs)
			histStr += "\t" + "Use all permutations is selected" + "\n";
		else {
			histStr += "\t" + "Randomly group experiments is selected" + "\n";
			histStr += "\t" + "#times: " + numCombs + "\n";
		}
		histStr += "\t" + "critical p-Value: " + alpha + "\n";
		
		if ( useroverride == true)
			histStr += "\t" + "user override: true \n";
		else
			histStr += "\t" + "user override: false \n";
		
		if ( isLogNormalized == true)
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
		;

		int aSize = panel.size();
		for (int aIndex = 0; aIndex < aSize; aIndex++)
			histStr += "\t\t" + panel.get(aIndex) + "\n";

		return histStr;
	}

	private String GenerateMarkerString(
			DSMicroarraySetView<? extends DSGeneMarker, ? extends DSMicroarray> view) {
		String histStr = null;

		histStr = view.markers().size() + " markers analyzed:\n";
		for (DSGeneMarker marker : view.markers()) {
			histStr += "\t" + marker.getLabel() + "\n";
		}

		return histStr;

	}
	
	private void setFoldChnage(DSMicroarraySet<DSMicroarray> set, DSSignificanceResultSet<DSGeneMarker> resultSet)
	{
		            
             if (useroverride == false)             
                 guessLogNormalized(set);
            
             
             String[] caseLabels = resultSet.getLabels(DSTTestResultSet.CASE);
             String[] controlLabels = resultSet.getLabels(DSTTestResultSet.CONTROL);
             DSAnnotationContext<DSMicroarray> context = CSAnnotationContextManager.getInstance().getCurrentContext(set);
             DSPanel<DSMicroarray> casePanel = new CSPanel<DSMicroarray>("Case");
             for (int i = 0; i < caseLabels.length; i++) {
                 String label = caseLabels[i];
                 casePanel.addAll(context.getItemsWithLabel(label));
             }
             casePanel.setActive(true);
             DSPanel<DSMicroarray> controlPanel = new CSPanel<DSMicroarray>("Control");
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

                     double sigValue = resultSet.getSignificance(marker);
                     
                     String isLogNormalizedStr ="";
                     double fold_change = 0;
                     double ratio =0;
                     if (!isLogNormalized) {
                         ratio = caseMean / controlMean;
                         if (ratio < 0) {
                             
                        	 fold_change = -Math.log(-ratio) / Math.log(2.0);
                         } else {
                        	 fold_change = Math.log(ratio) / Math.log(2.0);
                         }
                         isLogNormalizedStr = "false";
                     } else {;
                    	 fold_change = caseMean - controlMean;
                    	 isLogNormalizedStr = "true";
                     }
                                          
                     
                     resultSet.setFoldChange(marker, fold_change);
                     
                 }
		  
	}
	 
	private void guessLogNormalized(DSMicroarraySet<DSMicroarray> set) {
	        double minValue = Double.POSITIVE_INFINITY;
	        double maxValue = Double.NEGATIVE_INFINITY;
	        for (DSMicroarray microarray : set) {
	            DSMutableMarkerValue[] values = microarray.getMarkerValues();
	            double v;
	            for (DSMutableMarkerValue value : values) {
	                v = value.getValue();
	                if (v < minValue) {
	                    minValue = v;
	                }
	                if (v > maxValue) {
	                    maxValue = v;
	                }
	            }
	        }
	        if (maxValue - minValue < 100) {
	            isLogNormalized = true;
	        } else {
	            isLogNormalized = false;
	        }
	         
	    }

}
