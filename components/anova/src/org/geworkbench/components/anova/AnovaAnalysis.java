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
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSSignificanceResultSet;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.components.anova.gui.AnovaAnalysisPanel;
import org.geworkbench.components.anova.gui.AnovaAnalysisPanel.FalseDiscoveryRateControl;
import org.geworkbench.components.anova.gui.AnovaAnalysisPanel.PValueEstimation;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.impl.OneWayANOVA;
import org.tigr.microarray.mev.cluster.gui.impl.owa.OneWayANOVAInitBox;
import org.tigr.util.FloatMatrix;

/**
 * @author yc2480
 * @version $Id: AnovaAnalysis.java,v 1.26 2009-09-10 16:40:26 chiangy Exp $
 */
public class AnovaAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	private static final long serialVersionUID = 6660785761134949795L;
	
	private Log log = LogFactory.getLog(AnovaAnalysis.class);
	private final String analysisName = "Anova";

	/*
	 * store text output used in dataset history. Will be refreshed each time
	 * execute() been called.
	 */
	private String GroupAndChipsString;

	private AnovaAnalysisPanel anovaAnalysisPanel = new AnovaAnalysisPanel();

	public AnovaAnalysis() {
		setDefaultPanel(anovaAnalysisPanel);
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
		DSMicroarraySet maSet = view.getMicroarraySet();

		/* Get params */
		double pvalueth = anovaAnalysisPanel.pValueThreshold;
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
								.valueOf(anovaAnalysisPanel.pValueEstimation == PValueEstimation.permutation));

		if (anovaAnalysisPanel.pValueEstimation == PValueEstimation.fdistribution) {

		} else if (anovaAnalysisPanel.pValueEstimation == PValueEstimation.permutation) {
			data.addParam("numPerms", String
					.valueOf(anovaAnalysisPanel.permutationsNumber));
			log.debug("numPerms:"
					+ String.valueOf(anovaAnalysisPanel.permutationsNumber));
			if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.number) {
				data.addParam("falseNum", String.valueOf((new Float(
						anovaAnalysisPanel.falseSignificantGenesLimit)).intValue()));
			} else if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.proportion) {
				data.addParam("falseProp", String
						.valueOf(anovaAnalysisPanel.falseSignificantGenesLimit));
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

		log.debug(anovaAnalysisPanel.falseDiscoveryRateControl);
		if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.adjbonferroni) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.ADJ_BONFERRONI));
		} else if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.bonferroni) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.STD_BONFERRONI));
		} else if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.alpha) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.JUST_ALPHA));
		} else if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.westfallyoung) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.MAX_T));
		} else if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.number) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_NUM));
		} else if (anovaAnalysisPanel.falseDiscoveryRateControl == FalseDiscoveryRateControl.proportion) {
			data.addParam("correction-method", String
					.valueOf(OneWayANOVAInitBox.FALSE_PROP));
		} else {
			log
					.error("This shouldn't happen! I don't understand that selection. It should be one of following: Alpha, Boferroni, Adj-Bonferroni, WestfallYoung, FalseNum, FalseProp.");
		}

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
			float[] pValueCollection = new float[totalSignificantMarkerNum];
			float[] adjustedPValueCollection = new float[totalSignificantMarkerNum];
			float[] groupMeanCollectionForAllMarkers = new float[totalSignificantMarkerNum
							* mFM.getColumnDimension()];
			float[] groupStandardDiviationCollectionForAllMarkers = new float[totalSignificantMarkerNum
							* mFM.getColumnDimension()];
			float[] fValueCollection = new float[totalSignificantMarkerNum];
			
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
					pValueCollection[significantMarkerIndex] = 
							apFM.A[i][0];
					adjustedPValueCollection[
							significantMarkerIndex] = apFM.A[i][0];
					fValueCollection[significantMarkerIndex] = 
							fFM.A[i][0];
					for (int j = 0; j < mFM.getColumnDimension(); j++) {
						groupMeanCollectionForAllMarkers[j
								* totalSignificantMarkerNum
								+ significantMarkerIndex] = mFM.A[i][j];
						groupStandardDiviationCollectionForAllMarkers[
										j * totalSignificantMarkerNum
												+ significantMarkerIndex] =
										sFM.A[i][j];
					}
					significantMarkerIndex++;
				}
			}
			publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
					DSGeneMarker.class, panelSignificant,
					SubpanelChangedEvent.NEW));

			pb.stop();
			/* add to Dataset History */
			String history = generateHistoryString(view);
			HistoryPanel.addToHistory(sigSet, history);

			CSAnovaResultSet<DSGeneMarker> anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(view,
					"Anova Analysis Result Set", labels, significantMarkerNames,
					anovaResult2result2DArray(pValueCollection, labels,
							adjustedPValueCollection, fValueCollection, groupMeanCollectionForAllMarkers,
							groupStandardDiviationCollectionForAllMarkers));
			log.debug(significantMarkerNames.length
					+ "Markers added to anovaResultSet.");
			anovaResultSet.getSignificantMarkers().addAll(
					sigSet.getSignificantMarkers());
			log.debug(sigSet.getSignificantMarkers().size()
					+ "Markers added to anovaResultSet.getSignificantMarkers().");
			anovaResultSet.sortMarkersBySignificance();

			/* add to Dataset History */
			HistoryPanel.addToHistory(anovaResultSet, history);

			AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
					"Anova Analysis", anovaResultSet);
			return results;

		} catch (AbortException AE) {
			pb.stop();
			return new AlgorithmExecutionResults(false, "Analysis Aborted.",
					null);
		} catch (AlgorithmException AE) {
			pb.stop();
			AE.printStackTrace();
			return new AlgorithmExecutionResults(false, "Analysis failed for "+AE,
					null);
		}

	}

	/**
	 * 
	 * @param anovaResult
	 * @return
	 */
	private double[][] anovaResult2result2DArray(final float[] pValueCollection,
			final String[] groupNameCollection,
			final float[] adjustedPValueCollection,
			final float[] fValueCollection,
			final float[] groupMeanCollectionForAllMarkers,
			final float[] groupStandardDiviationCollectionForAllMarkers) {
		int arrayHeight = pValueCollection.length;

		/*
		 * each group needs two columns, plus pval, adjpval and fval.
		 */
		int arrayWidth = groupNameCollection.length * 2 + 3;

		log.debug("result2DArray:" + arrayWidth + "*" + arrayHeight);

		double[][] result2DArray = new double[arrayWidth][arrayHeight];

		/* fill p-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[0][cx] = pValueCollection[cx];
		}

		/* fill adj-p-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[1][cx] = adjustedPValueCollection[cx];
		}
		/* fill f-values */
		for (int cx = 0; cx < arrayHeight; cx++) {
			result2DArray[2][cx] = fValueCollection[cx];
		}
		/* fill means */
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < groupNameCollection.length; cy++) {
				result2DArray[3 + cy * 2][cx] = groupMeanCollectionForAllMarkers[cy * arrayHeight
						+ cx];
			}
		}
		/* fill stds */
		for (int cx = 0; cx < arrayHeight; cx++) {
			for (int cy = 0; cy < groupNameCollection.length; cy++) {
				result2DArray[4 + cy * 2][cx] = groupStandardDiviationCollectionForAllMarkers[cy
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
		StringBuilder histStr = new StringBuilder("Generated with ANOVA run with parameters:\n");
		histStr .append( "----------------------------------------\n" );
		/* P Value Estimation */
		histStr .append( "P Value estimation: " );
		if (anovaAnalysisPanel.pValueEstimation == PValueEstimation.permutation) {
			histStr .append( "Permutation\n" );
			histStr .append( "Permutation#: " )
					.append( anovaAnalysisPanel.permutationsNumber )
							.append( "\n" );
		} else {
			histStr .append( "F-Distribution\n" );
		}
		/* P Value threshold */
		histStr .append( "P Value threshold: " );
		histStr .append( anovaAnalysisPanel.pValueThreshold )
				.append( "\n" );

		/* Correction type */
		histStr .append( "correction-method: " );
		histStr .append( anovaAnalysisPanel.falseDiscoveryRateControl.toString() )
				.append( "\n" );

		/* group names and markers */

		histStr .append( GroupAndChipsString );

		histStr .append( view.markers().size() ) .append( " markers analyzed:\n" );
		for (DSGeneMarker marker : view.markers()) {
			histStr .append( "\t" ) .append( marker.getLabel() ) .append( "\n" );
		}

		return histStr.toString();
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
		// FIXME caGrdi service code need to be updated accordingly
		parameterMap.put("permutationsNumber", anovaAnalysisPanel.permutationsNumber);
		parameterMap.put("falseSignificantGenesLimit", anovaAnalysisPanel.falseSignificantGenesLimit);
		parameterMap.put("pValueThreshold", anovaAnalysisPanel.pValueThreshold);
		parameterMap.put("falseDiscoveryRateControl", anovaAnalysisPanel.falseDiscoveryRateControl.toString());
		parameterMap.put("pValueEstimation", anovaAnalysisPanel.pValueEstimation.toString());
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
	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
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

		return new ParamValidationResults(true, "No Error");
	}

}
