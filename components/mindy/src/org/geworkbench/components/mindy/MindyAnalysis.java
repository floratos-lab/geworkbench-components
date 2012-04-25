package org.geworkbench.components.mindy;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.history.HistoryPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;
import org.geworkbench.util.Util;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyResultRow;
import org.geworkbench.util.pathwaydecoder.mutualinformation.ModulatorInfo;

import wb.data.Marker;
import wb.data.MarkerSet;
import wb.data.Microarray;
import wb.data.MicroarraySet;
import edu.columbia.c2b2.mindy.Mindy;
import edu.columbia.c2b2.mindy.MindyResults;

/**
 * @author Matt Hall
 * @author ch2514
 * @author zji
 * @author oshteynb
 * @version $Id$
 */
public class MindyAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private static final long serialVersionUID = -3116424364457413572L;

	private Log log = LogFactory.getLog(MindyAnalysis.class);

	private MindyParamPanel paramPanel;

	private static final String analysisName = "Mindy";

	private ProgressBar progressBar = null;

	/* to test decoupling from dataset */
	/*
	 * assuming that there will be only one parameter panel, like a singleton,
	 * selector panel or selections in a well known location will be a better
	 * solution
	 */
	static MindyParamPanel paramsPanel;

	public static MindyParamPanel getParamsPanel() {
		return paramsPanel;
	}

	/**
	 * Constructor. Creates MINDY parameter panel.
	 */
	public MindyAnalysis() {
		paramPanel = new MindyParamPanel();
		setDefaultPanel(paramPanel);
	}

	/**
	 * The execute method the framework calls to analyze parameters and create
	 * MINDY results.
	 * 
	 * @param input
	 *            - microarray set data coming from the framework
	 * @return analysis algorithm results
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		if (input == null) {
			return new AlgorithmExecutionResults(false, "Invalid input.", null);
		}
		log.debug("input: " + input);
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> inputSetView = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		DSPanel<DSMicroarray> arraySet = null;
		if (inputSetView.useItemPanel())
			arraySet = inputSetView.getItemPanel();

		// Mindy parameter validation always returns true
		// (the method is not overrode from AbstractAnalysis)
		// so we can enter the execute() method and capture
		// both parameter and input errors.
		// The eventual error message dialog (if there are errors)
		// would look the same as the one created by the analysis panel

		stopAlgorithm = false;
		progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		progressBar.addObserver(this);
		progressBar.setTitle("MINDY");
		progressBar.setMessage("Processing Parameters");
		progressBar.start();

		// Use this to get params
		MindyParamPanel params = (MindyParamPanel) aspp;

		/* to test decoupling from dataset */
		paramsPanel = params;

		DSMicroarraySet mSet = inputSetView.getMicroarraySet();

		StringBuilder errMsgB = new StringBuilder();

		int numMAs = mSet.size();

		if (numMAs < 4) {
			errMsgB.append("Not enough microarrays in the set.  MINDY requires at least 4 microarrays.\n");
		}

		int numMarkers = mSet.getMarkers().size();
		ArrayList<Marker> targets = new ArrayList<Marker>();
		ArrayList<String> targetGeneList = params.getTargetGeneList();

		if (numMarkers < 2) {
			errMsgB.append("Not enough markers in the microarrays. (Need at least 2)\n");
		}

		ArrayList<Marker> modulators = new ArrayList<Marker>();
		ArrayList<String> modulatorGeneList = params.getModulatorGeneList();
		if ((modulatorGeneList != null) && (modulatorGeneList.size() > 0)) {
			for (String modGene : modulatorGeneList) {
				DSGeneMarker marker = mSet.getMarkers().get(modGene);
				if (marker == null) {
					errMsgB.append("Couldn't find marker ");
					errMsgB.append(modGene);
					errMsgB.append(" from modulator file in microarray set.\n");
				} else {
					modulators.add(new Marker(modGene));
				}
			}

		} else {
			errMsgB.append("No modulator specified.\n");
		}

		if ((targetGeneList != null) && (targetGeneList.size() > 0)) {
			for (String modGene : targetGeneList) {
				DSGeneMarker marker = mSet.getMarkers().get(modGene);
				if (marker == null) {
					errMsgB.append("Couldn't find marker ");
					errMsgB.append(modGene);
					errMsgB.append(" from target file in microarray set.\n");
				} else {
					targets.add(new Marker(modGene));
				}
			}
		}

		ArrayList<Marker> dpiAnnots = new ArrayList<Marker>();
		ArrayList<String> dpiAnnotList = params.getDPIAnnotatedGeneList();
		for (String modGene : dpiAnnotList) {
			DSGeneMarker marker = mSet.getMarkers().get(modGene);
			if (marker == null) {
				errMsgB.append("Couldn't find marker ");
				errMsgB.append(modGene);
				errMsgB.append(" from DPI annotation file in microarray set.\n");
			} else {
				dpiAnnots.add(new Marker(modGene));
			}
		}

		String transcriptionFactor = params.getTranscriptionFactor();
		DSGeneMarker transFac = mSet.getMarkers().get(transcriptionFactor);
		if (!transcriptionFactor.trim().equals("")) {
			if (transFac == null) {
				errMsgB.append("Specified hub marker (");
				errMsgB.append(transcriptionFactor);
				errMsgB.append(") not found in loadad microarray set.\n");
			}
		} else {
			errMsgB.append("No hub marker specified.\n");
		}

		boolean fullSetMI = false;

		float fullSetThreshold = 0;
		boolean subsetMI = false;
		if (params.getConditional().trim().equals(MindyParamPanel.MI)) {
			subsetMI = true;
		}
		float subsetThreshold = params.getConditionalValue();
		if ((!subsetMI)
				&& (params.getConditionalCorrection()
						.equals(MindyParamPanel.BONFERRONI))) {
			int num = targetGeneList.size();
			if (num <= 0) { // this is always interpreted as "All Markers"
				num = numMarkers;
			}
			subsetThreshold = subsetThreshold / num;
		}

		float setFraction = params.getSetFraction() / 100f;

		if (Math.round(setFraction * 2 * numMarkers) < 2) {
			errMsgB.append("Not enough markers in the specified % sample.  MINDY requires at least 2 markers in the sample.\n");
		}

		// If parameters or inputs have errors, alert the user and return from
		// execute()
		errMsgB.trimToSize();
		String s = errMsgB.toString();
		if (!s.equals("")) {
			log.info(errMsgB.toString());
			progressBar.stop();
			JOptionPane.showMessageDialog(null, s,
					"Parameter and Input Validation Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}

		if (stopAlgorithm) {
			stopAlgorithm = false;
			progressBar.stop();
			log.warn("Cancelling Mindy Analysis.");
			return null;
		}
		progressBar.setMessage("Running MINDY Algorithm");

		String history = params.getDataSetHistory()
				+ generateHistoryForMaSetView(inputSetView,
						useMarkersFromSelector());

		MindyThread mt = new MindyThread(mSet, arraySet,
				params.getTargetGeneList(), transFac, new Marker(
						params.getTranscriptionFactor()), modulators,
				dpiAnnots, fullSetMI, fullSetThreshold, subsetMI,
				subsetThreshold, setFraction, params.getDPITolerance(),
				history, params.getCandidateModulatorsFile());
		progressBar.addObserver(mt);
		mt.start();

		return new AlgorithmExecutionResults(true, "MINDY in progress.", null);
	}

	/**
	 * Receives GeneSelectorEvents from the framework (i.e. the Selector Panel)
	 * 
	 * @param e
	 * @param source
	 */
	@Subscribe
	public void receive(final GeneSelectorEvent e, Object source) {
		if (SwingUtilities.isEventDispatchThread()) {
			processGeneSelectorEvent(e);
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					processGeneSelectorEvent(e);
				}

			});
		}
	}

	// invoke only from EDT
	private void processGeneSelectorEvent(GeneSelectorEvent e) {
		DSGeneMarker marker = e.getGenericMarker(); // GeneselectorEvent can be
		// a panel event therefore
		// won't work here,
		if (marker != null) { // so added this check point--xuegong
			paramPanel.setTranscriptionFactor(marker.getLabel());
		}

		if (e.getPanel() != null) {
			DSPanel<DSGeneMarker> selectorPanel = e.getPanel();
			((MindyParamPanel) aspp).setSelectorPanel(((MindyParamPanel) aspp),
					selectorPanel);
		} else {
			log.debug("Received Gene Selector Event: Selection panel sent was null");
		}
	}

	@Subscribe
	public void receive(ProjectEvent e, Object source) {
		DSDataSet<?> data = e.getDataSet();
		if ((data != null) && (data instanceof DSMicroarraySet)) {
			((MindyParamPanel) aspp).setDataSet(data);
		}
	}

	/**
	 * Publish MINDY data to the framework.
	 * 
	 * @param data
	 * @return
	 */
	@Publish
	public MindyDataSet publishMatrixReduceSet(MindyDataSet data) {
		return data;
	}

	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
	}

	private class MindyThread extends Thread implements Observer {
		DSMicroarraySet mSet;

		DSPanel<DSMicroarray> arraySet;

		List<String> chosenTargets;

		DSGeneMarker transFac;

		Marker tf;

		ArrayList<Marker> modulators;

		ArrayList<Marker> dpiAnnots;

		boolean fullSetMI;

		float fullSetThreshold;

		boolean subsetMI;

		float subsetThreshold;

		float setFraction;

		float dpiTolerance;

		String paramDesc;

		String candidateModFile;

		public MindyThread(DSMicroarraySet mSet,
				DSPanel<DSMicroarray> arraySet, List<String> chosenTargets,
				DSGeneMarker transFac, Marker tf, ArrayList<Marker> modulators,
				ArrayList<Marker> dpiAnnots, boolean fullSetMI,
				float fullSetThreshold, boolean subsetMI,
				float subsetThreshold, float setFraction, float dpiTolerance,
				String paramDesc, String candidateModFile) {
			this.mSet = mSet;
			this.arraySet = arraySet;
			this.chosenTargets = chosenTargets;
			this.transFac = transFac;
			this.tf = tf;
			this.modulators = modulators;
			this.dpiAnnots = dpiAnnots;
			this.fullSetMI = fullSetMI;
			this.fullSetThreshold = fullSetThreshold;
			this.subsetMI = subsetMI;
			this.subsetThreshold = subsetThreshold;
			this.setFraction = setFraction;
			this.dpiTolerance = dpiTolerance;
			this.paramDesc = paramDesc;
			this.candidateModFile = candidateModFile;
		}

		private MindyData createMindyData(MindyResults results,
				CSMicroarraySet arraySet,
				ArrayList<DSMicroarray> arrayForMindyRun, float setFraction,
				DSGeneMarker transFac) {
			MindyData mindyData = new MindyData(arraySet, arrayForMindyRun,
					setFraction, transFac);

			processResults(mindyData, results, arraySet);

			return mindyData;

		}

		/**
		 * @param results
		 * @param arraySet
		 * @param mindyData
		 */
		private void processResults(MindyData mindyData, MindyResults results,
				CSMicroarraySet arraySet) {
			int numWithSymbols = 0;
			List<MindyResultRow> dataRows = new ArrayList<MindyResultRow>();

			// process mindy run results
			// consider iterating over modulators first
			Collator myCollator = Collator.getInstance();
			for (MindyResults.MindyResultForTarget result : results) {
				DSItemList<DSGeneMarker> markers = arraySet.getMarkers();
				DSGeneMarker target = markers.get(result.getTarget().getName());

				// used to find out if annotations file was loaded
				// reminder: look at the class that does file processing, in
				// should process annotation file too.
				if (!StringUtils.isEmpty(target.getGeneName()))
					numWithSymbols++;

				for (MindyResults.MindyResultForTarget.ModulatorSpecificResult specificResult : result) {

					// process results with nonzero scores
					float score = specificResult.getScore();
					if (score != 0.0) {
						mindyData.addToSortkeyMap(myCollator, target);

						DSGeneMarker mod = markers.get(specificResult
								.getModulator().getName());

						// used to find out if annotations file was loaded
						if (!StringUtils.isEmpty(mod.getGeneName()))
							numWithSymbols++;

						mindyData.addToSortkeyMap(myCollator, mod);

						double correlation = mindyData
								.calcPearsonCorrelation(target);
						mindyData.addToTargetInfoMap(correlation, target);

						// load data
						MindyResultRow row = new MindyResultRow(mod, target,
								score);

						HashMap<DSGeneMarker, ModulatorInfo> modInfoMap = mindyData
								.getModulatorInfoMap();
						ModulatorInfo modInfo = modInfoMap.get(mod);
						if (modInfo == null) {
							modInfo = new ModulatorInfo();
							modInfoMap.put(mod, modInfo);
						}
						modInfo.insertRow(row);

						dataRows.add(row);
					}
				}

				mindyData.initFilteredModulatorInfoMap();
			}

			if (numWithSymbols > 0)
				mindyData.setAnnotated(true);
		}

		public void run() {
			log.debug("Running MINDY algorithm...");

			Date startDate = new Date();
			Long startTime = startDate.getTime();

			Mindy mindy = new Mindy();

			ArrayList<DSMicroarray> arrayForMindyRun = MindyData
					.createArrayForMindyRun(mSet, arraySet);

			// bug 1992, it was about NPE, but just in case will catch any
			// exception
			MindyResults results = null;
			try {
				results = mindy.runMindy(
						convert(mSet, arrayForMindyRun, chosenTargets), tf,
						modulators, dpiAnnots, fullSetMI, fullSetThreshold,
						subsetMI, subsetThreshold, setFraction, dpiTolerance);
			} catch (Exception e) {
				log.error(e.getCause());
				results = null;
			}
			if (results == null) {
				progressBar.stop();
				log.warn("MINDY obtained no results.");
				JOptionPane.showMessageDialog(paramPanel.getParent(),
						"MINDY obtained no results.", "see errors, logs",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			log.debug("Finished running MINDY algorithm.");

			progressBar.setMessage("Processing MINDY Results");

			MindyData loadedData = createMindyData(results,
					(CSMicroarraySet) mSet, arrayForMindyRun, setFraction,
					transFac);

			MindyDataSet mindyDataSet = new MindyDataSet(mSet, "MINDY Results", loadedData,
					candidateModFile);

			log.info("Done converting MINDY results.");

			if (loadedData.isEmpty()) {
				progressBar.stop();
				log.warn("MINDY obtained no results.");
				JOptionPane.showMessageDialog(paramPanel.getParent(),
						"MINDY obtained no results.", "MINDY Analyze Error",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			log.info(paramDesc);
			// add start/end time to history
			String history = "Analysis started at: "
					+ Util.formatDateStandard(startDate) + "\n";
			history += "\n" + paramDesc + "\n";

			Date endDate = new Date();
			long endTime = endDate.getTime();
			history += "\nAnalysis finished at: "
					+ Util.formatDateStandard(endDate) + "\n";
			long elapsedTime = endTime - startTime;
			history += "\nTotal elapsed time: "
					+ DurationFormatUtils.formatDurationHMS(elapsedTime);

			HistoryPanel.addToHistory(mindyDataSet, history);
			progressBar.stop();
			publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
					"Mindy Result Added", null, mindyDataSet));

			progressBar.stop();
		}

		@SuppressWarnings("deprecation")
		public void update(java.util.Observable ob, Object o) {
			log.debug("initiated close");
			log.warn("Cancelling Mindy Analysis.");
			this.stop();
		}

		private MicroarraySet convert(DSMicroarraySet inSet,
				ArrayList<DSMicroarray> arrayForMindyRun,
				List<String> chosenTargets) {
			MarkerSet markers = new MarkerSet();
			if ((chosenTargets != null) && (chosenTargets.size() > 0)) {
				log.debug("Processing chosen targets: size="
						+ chosenTargets.size());
				int size = chosenTargets.size();
				List<String> alreadyIn = markers.getAllMarkerNames();
				for (int i = 0; i < size; i++) {
					String chosenName = chosenTargets.get(i);
					if (chosenName != null) {
						chosenName = chosenName.trim();
						if (!alreadyIn.contains(chosenName)) {
							markers.addMarker(new Marker(chosenName));
						}
					}
				}
			}
			log.debug("markers size (post chosen)=" + markers.size());
			if (markers.size() <= 0) {
				log.debug("adding all markers.");
				for (DSGeneMarker marker : inSet.getMarkers()) {
					markers.addMarker(new Marker(marker.getLabel()));
				}
			}

			MicroarraySet returnSet = new MicroarraySet(inSet.getDataSetName(),
					"ID", "ChipType", markers);

			for (DSMicroarray microarray : arrayForMindyRun) {
				returnSet.addMicroarray(new Microarray(microarray.getLabel(),
						microarray.getRawMarkerData()));
			}

			// debug only
			if (log.isDebugEnabled()) {
				MarkerSet ms = returnSet.getMarkers();
				log.debug("Markers in converted set:");
				for (int i = 0; i < ms.size(); i++) {
					log.debug("\t" + ms.getMarker(i).getName());
				}
			}

			return returnSet;
		}

	}

	// the following methods implemented for AbstractGridAnalysis
	@Override
	public String getAnalysisName() {
		return analysisName;
	}

	/**
	 * 
	 * @param maSetView
	 * @return
	 */
	@Override
	public String generateHistoryForMaSetView(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			boolean useMarkersFromSelector) {
		useMarkersFromSelector(true);
		MindyParamPanel params = (MindyParamPanel) aspp;
		if (params.getTargetsFrom().getSelectedItem().toString()
				.equals(MindyParamPanel.FROM_ALL)
				|| params.getTargetGeneList() == null
				|| params.getTargetGeneList().size() == 0)
			maSetView.useMarkerPanel(false);
		else
			useMarkersFromSelector(false);
		return super.generateHistoryForMaSetView(maSetView,
				useMarkersFromSelector());
	}

	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		// protected AbstractSaveableParameterPanel aspp is defined in
		// AbstractAnalysis
		MindyParamPanel paramPanel = (MindyParamPanel) this.aspp;

		ArrayList<String> modulatorGeneList = paramPanel.getModulatorGeneList();
		bisonParameters.put("modulatorGeneList", modulatorGeneList);
		ArrayList<String> targetGeneList = paramPanel.getTargetGeneList();
		bisonParameters.put("targetGeneList", targetGeneList);
		String transcriptionFactor = paramPanel.getTranscriptionFactor(); // this
		// is
		// labeled
		// "Hub
		// marker"
		// on
		// GUI
		bisonParameters.put("transcriptionFactor", transcriptionFactor);
		int setFraction = paramPanel.getSetFraction();
		bisonParameters.put("setFraction", setFraction);

		String conditional = paramPanel.getConditional().trim();
		bisonParameters.put("conditional", conditional);
		float conditionalValue = paramPanel.getConditionalValue();
		bisonParameters.put("conditionalValue", conditionalValue);
		String conditionalCorrection = paramPanel.getConditionalCorrection();
		bisonParameters.put("conditionalCorrection", conditionalCorrection);

		ArrayList<String> dpiAnnotList = paramPanel.getDPIAnnotatedGeneList();
		bisonParameters.put("dpiAnnotList", dpiAnnotList);
		float dpiTolerance = paramPanel.getDPITolerance();
		bisonParameters.put("dpiTolerance", dpiTolerance);

		bisonParameters.put("candidateModulatorsFile",
				paramPanel.getCandidateModulatorsFile());

		return bisonParameters;
	}

	@Override
	public Class<?> getBisonReturnType() {
		return MindyDataSet.class;
	}

	@Override
	protected boolean useMicroarraySetView() {
		return true;
	}

	@Override
	protected boolean useOtherDataSet() {
		return false;
	}

	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {

		MindyParamPanel params = (MindyParamPanel) aspp;
		DSMicroarraySet mSet = null;
		mSet = maSetView.getMicroarraySet();
		int numMAs = mSet.size();
		if (maSetView.useItemPanel()) {
			// if no selection get all arrays
			int numMAsSelect = maSetView.getItemPanel().size();
			if (numMAsSelect != 0) {
				numMAs = numMAsSelect;
			}
		}

		if (numMAs < 4) {
			return new ParamValidationResults(false,
					"Not enough microarrays in the set.  MINDY requires at least 4 microarrays.\n");
		}
		int numMarkers = mSet.getMarkers().size();
		if (numMarkers < 2) {
			return new ParamValidationResults(false,
					"Not enough markers in the microarrays. (Need at least 2)\n");
		}

		ArrayList<String> modulatorGeneList = params.getModulatorGeneList();
		if ((modulatorGeneList != null) && (modulatorGeneList.size() > 0)) {
			for (String modGene : modulatorGeneList) {
				DSGeneMarker marker = mSet.getMarkers().get(modGene);
				if (marker == null) {
					return new ParamValidationResults(
							false,
							"Couldn't find marker "
									+ modGene
									+ " from modulator file in microarray set.\n");
				}
			}
		} else {
			return new ParamValidationResults(false,
					"No modulator specified.\n");
		}

		ArrayList<String> targetGeneList = params.getTargetGeneList();
		if ((targetGeneList != null) && (targetGeneList.size() > 0)) {
			for (String modGene : targetGeneList) {
				DSGeneMarker marker = mSet.getMarkers().get(modGene);
				if (marker == null) {
					return new ParamValidationResults(false,
							"Couldn't find marker " + modGene
									+ " from target file in microarray set.\n");
				}
			}
		}

		ArrayList<String> dpiAnnotList = params.getDPIAnnotatedGeneList();
		for (String modGene : dpiAnnotList) {
			DSGeneMarker marker = mSet.getMarkers().get(modGene);
			if (marker == null) {
				return new ParamValidationResults(
						false,
						"Couldn't find marker "
								+ modGene
								+ " from DPI annotation file in microarray set.\n");
			}
		}
		String transcriptionFactor = params.getTranscriptionFactor();
		DSGeneMarker transFac = mSet.getMarkers().get(transcriptionFactor);
		if (!transcriptionFactor.trim().equals("")) {
			if (transFac == null) {
				return new ParamValidationResults(false,
						"Specified hub marker (" + transcriptionFactor
								+ ") not found in loadad microarray set.\n");
			}
		} else {
			return new ParamValidationResults(false,
					"No hub marker specified.\n");
		}
		float setFraction = params.getSetFraction() / 100f;
		if (Math.round(setFraction * 2 * numMarkers) < 2) {
			return new ParamValidationResults(
					false,
					"Not enough markers in the specified % sample.  MINDY requires at least 2 markers in the sample.\n");
		}

		return new ParamValidationResults(true, "No Error");
	}

}
