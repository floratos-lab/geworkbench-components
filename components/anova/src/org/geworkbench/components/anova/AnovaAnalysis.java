package org.geworkbench.components.anova;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSSignificanceResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.components.anova.gui.AnovaAnalysisPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.impl.OneWayANOVA;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
import org.tigr.util.FloatMatrix;

import edu.columbia.geworkbench.cagrid.anova.AnovaResult;
import edu.columbia.geworkbench.cagrid.anova.FalseDiscoveryRateControl;
import edu.columbia.geworkbench.cagrid.anova.PValueEstimation;

/**
 * @author yc2480
 * @version $Id: AnovaAnalysis.java,v 1.26 2009-09-10 16:40:26 chiangy Exp $
 */
public class AnovaAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 6660785761134949795L;
	
	private Log log = LogFactory.getLog(AnovaAnalysis.class);
	private final String analysisName = "Anova";
	private int localAnalysisType;

	/*
	 * store text output used in dataset history. Will be refreshed each time
	 * execute() been called.
	 */
	private String GroupAndChipsString;

	private AnovaAnalysisPanel anovaAnalysisPanel = new AnovaAnalysisPanel();

	public AnovaAnalysis() {
		localAnalysisType = AbstractAnalysis.TTEST_TYPE;
		setDefaultPanel(anovaAnalysisPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	public int getAnalysisType() {
		return localAnalysisType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		assert (input instanceof DSMicroarraySetView);
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();

		/* Get params */
		double pvalueth = anovaAnalysisPanel.anovaParameter.getPValueThreshold();
		if ((pvalueth < 0) || (pvalueth > 1)) {
			JOptionPane
					.showMessageDialog(
							null,
							"P-Value threshold should be a float number between 0.0 and 1.0.",
							"Please try again.",
							JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		ArrayList<String> labelSet = new ArrayList<String>();

		DSAnnotationContextManager manager = CSAnnotationContextManager
				.getInstance();
		DSAnnotationContext<DSMicroarray> context = manager
				.getCurrentContext(maSet);

		int numGenes = 0;
		int numSelectedGroups = 0;

		GroupAndChipsString = "";

		DSItemList<DSPanel<DSMicroarray>> panels = view.getItemPanel().panels();
		Iterator<DSPanel<DSMicroarray>> groups = panels.iterator();
		while (groups.hasNext()) {
			DSPanel<DSMicroarray> panelA = groups.next();
			if (panelA.isActive()) {
				numSelectedGroups++;
				labelSet.add(panelA.getLabel());
			}
		}

		numSelectedGroups = labelSet.size();
		if (numSelectedGroups < 3) {
			return new AlgorithmExecutionResults(false,
					"A minimum of 3 array groups must be activated.", null);
		}
		String[] labels = labelSet.toArray(new String[numSelectedGroups]);
		String[] labels1 = new String[0];
		numGenes = view.markers().size();
		log.debug("NumGenes:" + numGenes);
		/* Create panels and significant result sets to store results */
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSSignificanceResultSet<DSGeneMarker>(
				maSet, "Anova Analysis", labels1, labels, pvalueth);

		AlgorithmData data = new AlgorithmData();

		data.addParam("alpha", String.valueOf(pvalueth));

		/*
		 * use as an index points to all microarrays put in array A
		 */
		int globleArrayIndex = 0;

		ArrayList<DSMicroarray> markerList = new ArrayList<DSMicroarray>();
		/*
		 * calculating how many groups selected and arrays inside selected
		 * groups
		 */
		GroupAndChipsString += numSelectedGroups + " groups analyzed:\n";
		groups = panels.iterator();
		/* for each group */
		for (int i = 0; i < numSelectedGroups; i++) {
			String labelA = labels[i];
			DSPanel<DSMicroarray> panelA = groups.next();
			/* put group label into history */
			GroupAndChipsString += "\tGroup " + labelA + " (" + panelA.size()
					+ " chips)" + ":\n";
			;

			if (panelA.isActive()) {
				int aSize = panelA.size();
				/*
				 * for each array in this group
				 */
				for (int aIndex = 0; aIndex < aSize; aIndex++) {
					/*
					 * put member of each group into history
					 */
					GroupAndChipsString += "\t\t" + panelA.get(aIndex) + "\n";
					if (markerList.contains(panelA.get(aIndex)))
						return new AlgorithmExecutionResults(false,
								"Same marker (" + panelA.get(aIndex)
										+ ") exists in multiple groups.", null);
					else
						markerList.add(panelA.get(aIndex));
					/*
					 * count total arrays in selected groups.
					 */
					globleArrayIndex++;
				}
			}
		}
		/* fill microarray view data into array A, and assign groups */
		int[] groupAssignments = new int[globleArrayIndex];
		float[][] A = new float[numGenes][globleArrayIndex];
		globleArrayIndex = 0;
		/* for each groups */
		for (int i = 0; i < numSelectedGroups; i++) {
			String labelA = labels[i];
			DSPanel<DSMicroarray> panelA = context.getItemsWithLabel(labelA);
			int aSize = panelA.size();
			/*
			 * for each array in this group
			 */
			for (int aIndex = 0; aIndex < aSize; aIndex++) {
				/* for each marker in this array */
				for (int k = 0; k < numGenes; k++) {
					A[k][globleArrayIndex] = (float) panelA.get(aIndex)
							.getMarkerValue(k).getValue();
				}
				groupAssignments[globleArrayIndex] = i + 1;
				globleArrayIndex++;
			}
		}

		/* call MeV's interface using their protocols */
		FloatMatrix FM = new FloatMatrix(A);

		data.addMatrix("experiment", FM);
		data.addIntArray("group-assignments", groupAssignments);
		data.addParam("numGroups", String.valueOf(numSelectedGroups));

		data
				.addParam(
						"usePerms",
						String
								.valueOf(anovaAnalysisPanel.anovaParameter
										.getPValueEstimation() == PValueEstimation.permutation));

		if (anovaAnalysisPanel.anovaParameter.getPValueEstimation() == PValueEstimation.fdistribution) {

		} else if (anovaAnalysisPanel.anovaParameter.getPValueEstimation() == PValueEstimation.permutation) {
			data.addParam("numPerms", String
					.valueOf(anovaAnalysisPanel.anovaParameter
							.getPermutationsNumber()));
			log.debug("numPerms:"
					+ String.valueOf(anovaAnalysisPanel.anovaParameter
							.getPermutationsNumber()));
			if (anovaAnalysisPanel.anovaParameter
					.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.number) {
				data.addParam("falseNum", String.valueOf((new Float(
						anovaAnalysisPanel.anovaParameter
								.getFalseSignificantGenesLimit())).intValue()));
			} else if (anovaAnalysisPanel.anovaParameter
					.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.proportion) {
				data.addParam("falseProp", String
						.valueOf(anovaAnalysisPanel.anovaParameter
								.getFalseSignificantGenesLimit()));
			} else {
				/*
				 * user didn't select these two (which need to pass extra
				 * parameters), so we don't need to do a thing.
				 */
			}
		} else {
			log
					.error("This shouldn't happen! I don't understand that PValueEstimation");
		}

		log.debug(anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl());
		if (anovaAnalysisPanel.anovaParameter.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.adjbonferroni) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.ADJ_BONFERRONI));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.bonferroni) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.STD_BONFERRONI));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.alpha) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.JUST_ALPHA));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.westfallyoung) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.MAX_T));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.number) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_NUM));
		} else if (anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl() == FalseDiscoveryRateControl.proportion) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_PROP));
		} else {
			log
					.error("This shouldn't happen! I don't understand that selection. It should be one of following: Alpha, Boferroni, Adj-Bonferroni, WestfallYoung, FalseNum, FalseProp.");
		}

		/*
		 * TODO: I use AnovaResult for now, which was designed for grid, not
		 * local version, I think we'll also need a local version data type.
		 */
		AnovaResult anovaResult = new AnovaResult();

		OneWayANOVA OWA = new OneWayANOVA();

		final ProgressBar pb = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);

		pb.setTitle("Anova Analysis");
		pb.setMessage("Calculating Anova, please wait...");
		pb.start();

		try {
			/* when user close the ProgressBar, call OWA.abort(); */
			class AbortObserver implements Observer {
				OneWayANOVA OWA = null;;

				public AbortObserver(OneWayANOVA OWA) {
					this.OWA = OWA;
				}

				public void update(Observable o, Object arg) {
					OWA.abort();
				}
			}
			;
			AbortObserver abortObserver = new AbortObserver(OWA);

			pb.addObserver(abortObserver);

			/* execute the OneWayAnova algorithm */
			AlgorithmData result = OWA.execute(data);
			/* get p-values in result */
			FloatMatrix apFM = result.getMatrix("adjPValues");
			FloatMatrix fFM = result.getMatrix("fValues");
			FloatMatrix mFM = result.getMatrix("geneGroupMeansMatrix");
			FloatMatrix sFM = result.getMatrix("geneGroupSDsMatrix");

			/*
			 * I need to know how many will pass the threshold to initialize the
			 * array
			 */
			int significantMarkerIndex = 0;
			for (int i = 0; i < apFM.getRowDimension(); i++) {
				if (apFM.A[i][0] < pvalueth) {
					significantMarkerIndex++;
				}
			}

			int totalSignificantMarkerNum = significantMarkerIndex;
			totalSignificantMarkerNum = result.getCluster("cluster")
					.getNodeList().getNode(0).getFeaturesIndexes().length;
			log
					.debug("totalSignificantMarkerNum: "
							+ totalSignificantMarkerNum);
			String[] significantMarkerNames = new String[totalSignificantMarkerNum];
			significantMarkerIndex = 0;
			for (int cx = 0; cx < totalSignificantMarkerNum; cx++) {
				int i = result.getCluster("cluster").getNodeList().getNode(0)
						.getFeaturesIndexes()[cx];
				significantMarkerNames[significantMarkerIndex] = view.markers()
						.get(i).getLabel();
				significantMarkerIndex++;
			}
			/* output f-value, p-value, adj-p-value, mean, std */
			anovaResult
					.setSignificantMarkerNameCollection(significantMarkerNames);
			anovaResult
					.setPValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult
					.setGroupMeanCollectionForAllMarkers(new float[totalSignificantMarkerNum
							* mFM.getColumnDimension()]);
			anovaResult
					.setGroupStandardDiviationCollectionForAllMarkers(new float[totalSignificantMarkerNum
							* mFM.getColumnDimension()]);
			anovaResult
					.setPValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult
					.setAdjustedPValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult
					.setFValueCollection(new float[totalSignificantMarkerNum]);
			anovaResult.setGroupNameCollection(labels);
			DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
					"Significant Genes");

			significantMarkerIndex = 0;
			for (int i = 0; i < apFM.getRowDimension(); i++) {
				/* check if this marker exist in the significant cluster. */
				int[] aList = result.getCluster("cluster").getNodeList()
						.getNode(0).getFeaturesIndexes();
				boolean inTheList = false;
				for (int cx = 0; cx < aList.length; cx++) {
					if (aList[cx] == i) {
						inTheList = true;
					}
				}
				/*
				 * if this marker exist in the significant cluster, then it's
				 * significant.
				 */
				if (inTheList) {
					DSGeneMarker item = view.markers().get(i);
					log.debug("SignificantMarker: "
							+ view.markers().get(i).getLabel()
							+ ", with apFM: " + apFM.A[i][0]);
					panelSignificant.add(item, new Float(apFM.A[i][0]));
					double doubleSignificance = 0;
					/*
					 * we'll have float and double compare issue in
					 * CSSifnificanceResultSet.setSignificance()
					 */
					if (apFM.A[i][0] == (float) pvalueth) {
						/*
						 * Manually set to pvalueth in double to fix bug 0001239
						 * on Mantis. Then, minus a number which is less then
						 * float can store to let it unequals to pvalue
						 * threshold. (so we don't need to change
						 * CSSignificanceResultSet.setSignificance() to
						 * inclusive.)
						 */
						doubleSignificance = pvalueth - 0.000000001;
					} else {
						doubleSignificance = (double) apFM.A[i][0];
					}
					sigSet.setSignificance(item, doubleSignificance);
					anovaResult.setPValueCollection(significantMarkerIndex,
							apFM.A[i][0]);
					anovaResult.setAdjustedPValueCollection(
							significantMarkerIndex, apFM.A[i][0]);
					anovaResult.setFValueCollection(significantMarkerIndex,
							fFM.A[i][0]);
					for (int j = 0; j < mFM.getColumnDimension(); j++) {
						anovaResult.setGroupMeanCollectionForAllMarkers(j
								* totalSignificantMarkerNum
								+ significantMarkerIndex, mFM.A[i][j]);
						anovaResult
								.setGroupStandardDiviationCollectionForAllMarkers(
										j * totalSignificantMarkerNum
												+ significantMarkerIndex,
										sFM.A[i][j]);
					}
					significantMarkerIndex++;
				}
			}
			publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
					DSGeneMarker.class, panelSignificant,
					SubpanelChangedEvent.NEW));
		} catch (AbortException AE) {
			return new AlgorithmExecutionResults(false, "Analysis Aborted.",
					null);
		} catch (AlgorithmException AE) {
			AE.printStackTrace();
		}

		pb.stop();
		/* add to Dataset History */
		ProjectPanel.addToHistory(sigSet, generateHistoryString(view));

		CSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(view,
				"Anova Analysis Result Set", labels, anovaResult
						.getSignificantMarkerNameCollection(),
				anovaResult2result2DArray(anovaResult));
		log.debug(anovaResult.getSignificantMarkerNameCollection().length
				+ "Markers added to anovaResultSet.");
		anovaResultSet.getSignificantMarkers().addAll(
				sigSet.getSignificantMarkers());
		log.debug(sigSet.getSignificantMarkers().size()
				+ "Markers added to anovaResultSet.getSignificantMarkers().");
		anovaResultSet.sortMarkersBySignificance();

		/* add to Dataset History */
		ProjectPanel.addToHistory(anovaResultSet, generateHistoryString(view));

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Anova Analysis", anovaResultSet);
		return results;
	}

	/**
	 * 
	 * @param anovaResult
	 * @return
	 */
	private double[][] anovaResult2result2DArray(AnovaResult anovaResult) {
		int arrayHeight = anovaResult.getPValueCollection().length;

		/*
		 * each group needs two columns, plus pval, adjpval and fval.
		 */
		int arrayWidth = anovaResult.getGroupNameCollection().length * 2 + 3;

		log.debug("result2DArray:" + arrayWidth + "*" + arrayHeight);

		double[][] result2DArray = new double[arrayWidth][arrayHeight];

		/* fill p-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[0][cx] = anovaResult.getPValueCollection()[cx];
		}

		/* fill adj-p-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[1][cx] = anovaResult.getAdjustedPValueCollection()[cx];
		}
		/* fill f-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[2][cx] = anovaResult.getFValueCollection()[cx];
		}
		/* fill means */
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < anovaResult.getGroupNameCollection().length; cy++) {
				result2DArray[3 + cy * 2][cx] = anovaResult
						.getGroupMeanCollectionForAllMarkers()[cy * arrayHeight
						+ cx];
			}
		}
		/* fill stds */
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < anovaResult.getGroupNameCollection().length; cy++) {
				result2DArray[4 + cy * 2][cx] = anovaResult
						.getGroupStandardDiviationCollectionForAllMarkers()[cy
						* arrayHeight + cx];
			}
		}

		return result2DArray;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public org.geworkbench.events.SubpanelChangedEvent<?> publishSubpanelChangedEvent(
			org.geworkbench.events.SubpanelChangedEvent<?> event) {
		return event;
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	private String generateHistoryString(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> view) {
		String histStr = "";
		/* Header */
		histStr += "Generated with ANOVA run with parameters:\n";
		histStr += "----------------------------------------\n";
		/* P Value Estimation */
		histStr += "P Value estimation: ";
		if (anovaAnalysisPanel.anovaParameter.getPValueEstimation() == PValueEstimation.permutation) {
			histStr += "Permutation\n";
			histStr += "Permutation#: "
					+ anovaAnalysisPanel.anovaParameter.getPermutationsNumber()
					+ "\n";
		} else {
			histStr += "F-Distribution\n";
		}
		/* P Value threshold */
		histStr += "P Value threshold: ";
		histStr += anovaAnalysisPanel.anovaParameter.getPValueThreshold()
				+ "\n";

		/* Correction type */
		histStr += "correction-method: ";
		histStr += anovaAnalysisPanel.anovaParameter
				.getFalseDiscoveryRateControl().toString()
				+ "\n";

		/* group names and markers */

		histStr += GroupAndChipsString;

		histStr += view.markers().size() + " markers analyzed:\n";
		for (DSGeneMarker marker : view.markers()) {
			histStr += "\t" + marker.getLabel() + "\n";
		}

		return histStr;
	}

	/**
	 * 
	 * @param set
	 * @return
	 */
	private boolean isLogNormalized(DSMicroarraySet<DSMicroarray> set) {
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
		/*
		 * if the range of the values is small enough, we guess it's
		 * lognormalized.
		 */
		return ((maxValue - minValue) < 100);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getAnalysisName()
	 */
	@Override
	public String getAnalysisName() {
		return analysisName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> parameterMap = new HashMap<Serializable, Serializable>();
		/*
		 * put every parameters you need for execute(String encodedInput) in
		 * AnovaClient.java. microarray data already been added before these
		 * parameters in AnalysisPanel.java. I'll need to put group information
		 * and anovaParameter.
		 */
		parameterMap.put("anovaParameter", anovaAnalysisPanel.anovaParameter);
		return parameterMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	@Override
	public Class<?> getBisonReturnType() {
		return CSAnovaResultSet.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useMicroarraySetView()
	 */
	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#useOtherDataSet()
	 */
	@Override
	protected boolean useOtherDataSet() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#validInputData(org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView,
	 *      org.geworkbench.bison.datastructure.biocollections.DSDataSet)
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet refMASet) {
		/* check for minimum number of activated groups */
		DSItemList<DSPanel<DSMicroarray>> panels = maSetView.getItemPanel()
				.panels();
		Iterator<DSPanel<DSMicroarray>> groups = panels.iterator();
		int numSelectedGroups = 0;
		/* check each group, to see if they have minimum microarrays. */
		while (groups.hasNext()) {
			DSPanel<DSMicroarray> panelA = groups.next();
			if (panelA.isActive()) {
				numSelectedGroups++;
				int aSize = panelA.size();
				if (aSize < 2)
					return new ParamValidationResults(false,
							"Each microarray group must contains at least 2 arrays.");
			}
		}
		if (numSelectedGroups < 3) {
			return new ParamValidationResults(false,
					"A minimum of 3 array groups must be activated.");
		}
		/* check for log normalization */
		if (!isLogNormalized(maSetView.getMicroarraySet())) {
			Object[] options = { "Proceed", "Cancel" };
			int n = JOptionPane
					.showOptionDialog(
							/*
							 * this make it shown in the center of our software
							 */
							anovaAnalysisPanel.getTopLevelAncestor(),
							"The input dataset should be log-transformed (to approximate a standard distribution); \n\nClick Proceed to override and continue the analysis with the input dataset selected.",
							"Log Transformation", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							/* do not use a custom Icon */
							null,
							/* the titles of buttons */
							options,
							/* default button title */
							options[0]);
			if (n == 1) { /* n==1 means canceled */
				return new ParamValidationResults(false,
						"Analysis canceled by user.");
			}
		}
		return new ParamValidationResults(true, "No Error");
	}

}
