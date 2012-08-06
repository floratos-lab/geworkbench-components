package org.geworkbench.components.anova;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.SubpanelChangedEvent;
import org.geworkbench.util.ProgressBar; 

import org.geworkbench.components.anova.data.AnovaInput;
import org.geworkbench.components.anova.data.AnovaOutput;

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

	private transient ProgressBar pbAnova;

	private AnovaAnalysisPanel anovaAnalysisPanel = new AnovaAnalysisPanel();

	public AnovaAnalysis() {
		setDefaultPanel(anovaAnalysisPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
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
		DSItemList<DSGeneMarker> selectMarkers = null;
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

		selectMarkers = view.markers();
		numGenes = selectMarkers.size();
		log.debug("NumGenes:" + numGenes);

		/* Create panels and significant result sets to store results */
		DSSignificanceResultSet<DSGeneMarker> sigSet = new CSSignificanceResultSet<DSGeneMarker>(
				maSet, "Anova Analysis", labels1, labels, pvalueth);

		/*
		 * use as an index points to all microarrays put in array A
		 */
		int globleArrayIndex = 0;

		ArrayList<DSMicroarray> microarrayList = new ArrayList<DSMicroarray>();
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
					if (microarrayList.contains(panelA.get(aIndex)))
						return new AlgorithmExecutionResults(false,
								"Same array (" + panelA.get(aIndex)
										+ ") exists in multiple groups.", null);
					else
						microarrayList.add(panelA.get(aIndex));
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
							.getMarkerValue(selectMarkers.get(k)).getValue();

				}
				groupAssignments[globleArrayIndex] = i + 1;
				globleArrayIndex++;
			}
		}

		AnovaInput anovaInput = new AnovaInput(A,groupAssignments, numGenes,
			    numSelectedGroups, pvalueth, anovaAnalysisPanel.pValueEstimation.ordinal(),
			    anovaAnalysisPanel.permutationsNumber, anovaAnalysisPanel.falseDiscoveryRateControl.ordinal(),
			    anovaAnalysisPanel.falseSignificantGenesLimit);
	 
		final Anova anova = new Anova(anovaInput);
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				pbAnova = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
				pbAnova.addObserver(new CancelObserver(anova));
				pbAnova.setTitle("Anova Analysis");
				pbAnova.setMessage("Calculating Anova, please wait...");
				pbAnova.start();
			}

		});

		CSAnovaResultSet<DSGeneMarker> anovaResultSet = null;

		try {
			AnovaOutput output = anova.execute();
			if (output == null) {
				if (pbAnova != null) {
					pbAnova.dispose();
				}
				return null; // cancelled
			}

			int[] featuresIndexes = output.getFeaturesIndexes();
			double[] significances = output.getSignificances();
			String[] significantMarkerNames = new String[featuresIndexes.length];
			DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
					"Significant Genes");

			for (int i = 0; i < featuresIndexes.length; i++) {
				DSGeneMarker item = view.markers().get(featuresIndexes[i]);
				log.debug("SignificantMarker: " + item.getLabel()
						+ ", with apFM: " + significances[i]);
				panelSignificant.add(item, new Float(significances[i]));
				sigSet.setSignificance(item, significances[i]);
				significantMarkerNames[i] = item.getLabel();
			}

			publishSubpanelChangedEvent(new SubpanelChangedEvent<DSGeneMarker>(
					DSGeneMarker.class, panelSignificant,
					SubpanelChangedEvent.NEW));

			/* add to Dataset History */
			String history = generateHistoryString(view);
			HistoryPanel.addToHistory(sigSet, history);

			anovaResultSet = new CSAnovaResultSet<DSGeneMarker>(view,
					"Anova Analysis Result Set", labels,
					significantMarkerNames, output.getResult2DArray());
			log.debug(significantMarkerNames.length
					+ "Markers added to anovaResultSet.");
			anovaResultSet.getSignificantMarkers().addAll(
					sigSet.getSignificantMarkers());
			log.debug(sigSet.getSignificantMarkers().size()
					+ "Markers added to anovaResultSet.getSignificantMarkers().");
			anovaResultSet.sortMarkersBySignificance();

			/* add to Dataset History */
			HistoryPanel.addToHistory(anovaResultSet, history);

		} catch (AnovaException e) {
			if (pbAnova != null) {
				pbAnova.dispose();
			}
			e.printStackTrace();
			return new AlgorithmExecutionResults(false,
					"Exception happened in anova computaiton: " + e, null);
		}

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (pbAnova != null) {
					pbAnova.dispose();
				}
			}

		});

		AlgorithmExecutionResults results = new AlgorithmExecutionResults(true,
				"Anova Analysis", anovaResultSet);
		return results;

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
		StringBuilder histStr = new StringBuilder(
				"Generated with ANOVA run with parameters:\n");
		histStr.append("----------------------------------------\n");
		/* P Value Estimation */
		histStr.append("P Value estimation: ");
		if (anovaAnalysisPanel.pValueEstimation == PValueEstimation.permutation) {
			histStr.append("Permutation\n");
			histStr.append("Permutation#: ")
					.append(anovaAnalysisPanel.permutationsNumber).append("\n");
		} else {
			histStr.append("F-Distribution\n");
		}
		/* P Value threshold */
		histStr.append("P Value threshold: ");
		histStr.append(anovaAnalysisPanel.pValueThreshold).append("\n");

		/* Correction type */
		histStr.append("correction-method: ");
		histStr.append(anovaAnalysisPanel.falseDiscoveryRateControl.toString())
				.append("\n");

		/* group names and markers */

		histStr.append(GroupAndChipsString);

		histStr.append(view.markers().size()).append(" markers analyzed:\n");
		for (DSGeneMarker marker : view.markers()) {
			histStr.append("\t").append(marker.getLabel()).append("\n");
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
		parameterMap.put("permutationsNumber",
				anovaAnalysisPanel.permutationsNumber);
		parameterMap.put("falseSignificantGenesLimit",
				anovaAnalysisPanel.falseSignificantGenesLimit);
		parameterMap.put("pValueThreshold", anovaAnalysisPanel.pValueThreshold);
		parameterMap.put("falseDiscoveryRateControl",
				anovaAnalysisPanel.falseDiscoveryRateControl.toString());
		parameterMap.put("pValueEstimation",
				anovaAnalysisPanel.pValueEstimation.toString());
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
	 * @see
	 * org.geworkbench.analysis.AbstractGridAnalysis#validInputData(org.geworkbench
	 * .bison.datastructure.biocollections.views.DSMicroarraySetView,
	 * org.geworkbench.bison.datastructure.biocollections.DSDataSet)
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

	static private class CancelObserver implements Observer {
		final private Anova anova;

		CancelObserver(final Anova anova) {
			super();
			this.anova = anova;
		}

		@Override
		public void update(Observable o, Object arg) {
			anova.cancelled = true;
			anova.OWA.abort();
		}

	}

}
