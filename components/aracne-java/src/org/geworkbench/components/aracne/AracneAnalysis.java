package org.geworkbench.components.aracne;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSExpressionMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ClusteringAnalysis;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.AdjacencyMatrixEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrix;
import org.geworkbench.util.pathwaydecoder.mutualinformation.AdjacencyMatrixDataSet;

import wb.data.Marker;
import wb.data.MarkerSet;
import wb.data.Microarray;
import wb.data.MicroarraySet;
import wb.plugins.aracne.GraphEdge;
import wb.plugins.aracne.WeightedGraph;
import edu.columbia.c2b2.aracne.Parameter;

/**
 * @author Matt Hall
 * @version $id$
 */
public class AracneAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Log log = LogFactory.getLog(AracneAnalysis.class);

	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;
	private AdjacencyMatrixDataSet adjMatrix;
	private final String analysisName = "Aracne";

	/**
	 * 
	 */
	public AracneAnalysis() {
		setLabel("ARACNE");
		setDefaultPanel(new AracneParamPanel());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractAnalysis#getAnalysisType()
	 */
	public int getAnalysisType() {
		return AbstractAnalysis.ZERO_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		log.debug("input: " + input);
		AracneParamPanel params = (AracneParamPanel) aspp;
		if (input instanceof DSMicroarraySetView) {
			log.debug("Input dataset is microarray type.");
			mSetView = (DSMicroarraySetView) input;
		} else if (input instanceof AdjacencyMatrixDataSet) {
			log
					.debug("Input dataset is adjacency matrix, will only perform DPI.");
			adjMatrix = (AdjacencyMatrixDataSet) input;
		}

		final Parameter p = new Parameter();
		if (params.isHubListSpecified()) {
			if (params.getHubGeneList() == null
					|| params.getHubGeneList().size() == 0) {
				JOptionPane.showMessageDialog(null,
						"You did not load any genes as hub markers.");
				return null;
			}

			ArrayList<String> hubGeneList = params.getHubGeneList();
			for (String modGene : hubGeneList) {
				DSGeneMarker marker = mSetView.markers().get(modGene);
				if (marker == null) {
					log.info("Couldn't find marker " + modGene
							+ " specified as hub gene in microarray set.");
					JOptionPane.showMessageDialog(null, "Couldn't find marker "
							+ modGene
							+ " specified as hub gene in microarray set.");
					return null;
				}
			}

			p.setSubnet(new Vector<String>(hubGeneList));
		} else {
			int n = JOptionPane.showConfirmDialog(
				    null,
				    "You choose 'All vs All' for hub markers. This could take a LONG time. Do you want to continue?",
				    "'All vs All' confirmation",
				    JOptionPane.YES_NO_OPTION);
			if(n==JOptionPane.NO_OPTION)
				return null;

		}
		if (params.isThresholdMI()) {
			p.setThreshold(params.getThreshold());
		} else {
			p.setPvalue(params.getThreshold());
		}
		if (params.isKernelWidthSpecified()) {
			p.setSigma(params.getKernelWidth());
		}
		if (params.isDPIToleranceSpecified()) {
			p.setEps(params.getDPITolerance());
		}
		if (params.isTargetListSpecified()) {
			if (params.getTargetGenes() == null
					|| params.getTargetGenes().size() == 0) {
				JOptionPane.showMessageDialog(null,
						"You did not load any target genes.");
				return null;
			}
			p.setTf_list(new Vector<String>(params.getTargetGenes()));
		}
		if (adjMatrix != null) {
			p.setPrecomputedAdjacencies(convert(adjMatrix, mSetView));
			adjMatrix = null;
		}

		int bs = params.getBootstrapNumber();
		double pt = params.getConsensusThreshold();
		if (bs <= 0 || pt <= 0 || pt > 1)
			return null;

		AracneThread aracneThread = new AracneThread(mSetView, p, bs, pt);

		AracneProgress progress = new AracneProgress(aracneThread);
		aracneThread.setProgressWindow(progress);
		progress.startProgress();

		return new AlgorithmExecutionResults(true, "ARACNE in progress.", null);

	}

	/**
	 * 
	 * @param adjMatrix
	 * @param mSet
	 * @return
	 */
	private WeightedGraph convert(AdjacencyMatrixDataSet adjMatrix,
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet) {
		WeightedGraph graph = new WeightedGraph(adjMatrix.getNetworkName());
		AdjacencyMatrix matrix = adjMatrix.getMatrix();
		HashMap<Integer, HashMap<Integer, Float>> geneRows = matrix
				.getGeneRows();
		DSItemList<DSGeneMarker> markers = mSet.markers();
		for (DSGeneMarker marker : markers) {
			log.debug(marker.getLabel() + "added");
			graph.addEdge(marker.getLabel(), marker.getLabel(), 0);
		}
		for (Map.Entry<Integer, HashMap<Integer, Float>> entry : geneRows
				.entrySet()) {
			DSGeneMarker gene1 = markers.get(entry.getKey());
			if (gene1 != null) {
				HashMap<Integer, Float> destGenes = entry.getValue();
				for (Map.Entry<Integer, Float> destEntry : destGenes.entrySet()) {
					DSGeneMarker destGene = markers.get(destEntry.getKey());
					if (destGene != null) {
						graph.addEdge(gene1.getLabel(), destGene.getLabel(),
								destEntry.getValue());
					} else {
						log.debug("Gene with index " + destEntry.getKey()
								+ " not found in selected genes, skipping.");
					}
				}
			} else {
				log.debug("Gene with index " + entry.getKey()
						+ " not found in selected genes, skipping.");
			}
		}
		return graph;
	}

	/**
	 * 
	 * FIXME: This convert() has bug in it !!!, Since Microarray.getValues() in
	 * workbook.jar will return all the marker values, we should filter out
	 * those inactive markers in DSMicroarraySetView before put into
	 * MicroarraySet
	 * 
	 * @param inSet
	 * @return
	 */
	private MicroarraySet convert(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> inSet) {
		MarkerSet markers = new MarkerSet();
		for (DSGeneMarker marker : inSet.markers()) {
			markers.addMarker(new Marker(marker.getLabel()));
		}
		MicroarraySet returnSet = new MicroarraySet(inSet.getDataSet()
				.getDataSetName(), inSet.getDataSet().getID(), "Unknown",
				markers);
		DSItemList<DSMicroarray> arrays = inSet.items();
		for (DSMicroarray microarray : arrays) {
			float[] markerData=new float[markers.size()];
			int i=0;
			for (DSGeneMarker marker : inSet.markers()){
				markerData[i++]=(float)microarray.getMarkerValue(marker).getValue();
			}
			returnSet.addMicroarray(new Microarray(microarray.getLabel(),
					markerData));
		}
		return returnSet;
	}

	/**
	 * 
	 * @param graph
	 * @param mSet
	 * @return
	 */
	private AdjacencyMatrix convert(WeightedGraph graph,
			DSMicroarraySet<DSMicroarray> mSet) {
		AdjacencyMatrix matrix = new AdjacencyMatrix();
		matrix.setMicroarraySet(mSet);
		for (String node : graph.getNodes()) {
			DSGeneMarker marker = mSet.getMarkers().get(node);
			matrix.addGeneRow(marker.getSerial());
		}
		for (GraphEdge graphEdge : graph.getEdges()) {
			DSGeneMarker marker1 = mSet.getMarkers().get(graphEdge.getNode1());
			DSGeneMarker marker2 = mSet.getMarkers().get(graphEdge.getNode2());
			matrix.add(marker1.getSerial(), marker2.getSerial(), graphEdge
					.getWeight());
		}
		return matrix;
	}

	/**
	 * 
	 * @param ae
	 * @return
	 */
	@Publish
	public AdjacencyMatrixEvent publishAdjacencyMatrixEvent(
			AdjacencyMatrixEvent ae) {
		return ae;
	}

	/**
	 * 
	 * @param event
	 * @return
	 */
	@Publish
	public ProjectNodeAddedEvent publishProjectNodeAddedEvent(
			ProjectNodeAddedEvent event) {
		return event;
	}

	/**
	 * 
	 */
	class AracneThread extends Thread {
		private WeightedGraph weightedGraph;
		private AracneProgress progressWindow;
		private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;
		private Parameter p;

		private int bootstrapNumber;
		private double pThreshold;

		public AracneThread(
				DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet,
				Parameter p, int bootstrapNumber, double pThreshold) {
			this.mSetView = mSet;
			this.p = p;

			this.bootstrapNumber = bootstrapNumber;
			this.pThreshold = pThreshold;
		}

		public void run() {
			log.debug("Running ARACNE in worker thread.");
			p.setSuppressFileWriting(true);
			weightedGraph = HardenedAracne.run(convert(mSetView), p,
					bootstrapNumber, pThreshold);
			log.debug("Done running ARACNE in worker thread.");
			progressWindow.stopProgress();

			if (weightedGraph.getEdges().size() > 0) {
				AdjacencyMatrixDataSet dataSet = new AdjacencyMatrixDataSet(
						convert(weightedGraph, mSetView.getMicroarraySet()),
						-1, 0, 1000, "Adjacency Matrix", "ARACNE Set", mSetView
								.getMicroarraySet());
				StringBuilder paramDescB = new StringBuilder(
						"Generated with ARACNE run with data:\n");
				paramDescB.append(generateHistoryString(this.mSetView));
				ProjectPanel.addToHistory(dataSet,
						"Generated with ARACNE run with paramters:\n"
								+ p.getParamterDescription()
								+ hubMarkersDescription(p)
								+ paramDescB.toString());
				publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
						"Adjacency Matrix Added", null, dataSet));

				publishAdjacencyMatrixEvent(new AdjacencyMatrixEvent(convert(
						weightedGraph, mSetView.getMicroarraySet()),
						"ARACNE Set", -1, 2, 0.5f,
						AdjacencyMatrixEvent.Action.DRAW_NETWORK));
			} else {
				tellUserToRelaxThresholds();
			}

		}

		/*
		 * this is not included in Parameter's implement, which is outside this
		 * package
		 */
		private final String hubMarkersDescription(Parameter p) {
			StringBuilder builder = new StringBuilder();
			Vector<String> subnet = p.getSubnet();
			if (subnet.size() == 0)
				return "";
			builder.append("[PARA] Hub markers: " + subnet.get(0));
			for (int i = 1; i < subnet.size(); i++)
				builder.append(", " + subnet.get(i));
			builder.append("\n");
			return builder.toString();
		}

		/**
		 * 
		 * @return
		 */
		public AracneProgress getProgressWindow() {
			return progressWindow;
		}

		/**
		 * 
		 * @param progressWindow
		 */
		public void setProgressWindow(AracneProgress progressWindow) {
			this.progressWindow = progressWindow;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractAnalysis#validateParameters()
	 *      Validates the user-entered parameter values.
	 */
	@Override
	public ParamValidationResults validateParameters() {
		// Delegates the validation to the panel.

		if (aspp == null)
			return new ParamValidationResults(true, null);

		// Use this to get params
		AracneParamPanel params = (AracneParamPanel) aspp;

		if (params.isHubListSpecified()) {
			if (params.getHubGeneList() == null
					|| params.getHubGeneList().size() == 0) {
				return new ParamValidationResults(false,
						"You did not load any genes as hub markers.");
			}
		}
		if (params.isThresholdMI()) {
			try {
				if ((0 <= params.getThreshold())
						&& (params.getThreshold() <= 1)) {
				} else
					return new ParamValidationResults(false,
							"Threshold Mutual Info. should between 0.0 and 1.0");
			} catch (NumberFormatException nfe) {
				return new ParamValidationResults(false,
						"Threshold Mutual Info. should be a float number between 0.0 and 1.0.");
			}
			;
		} else {
			try {
				if ((0 <= params.getThreshold())
						&& (params.getThreshold() <= 1)) {
				} else
					return new ParamValidationResults(false,
							"Threshold P-Value should between 0.0 and 1.0");
			} catch (NumberFormatException nfe) {
				return new ParamValidationResults(false,
						"Threshold P-Value should be a float number between 0.0 and 1.0.");
			}
			;
		}
		if (params.isKernelWidthSpecified()) {
			try {
				if ((0 <= params.getKernelWidth())
						&& (params.getKernelWidth() <= 1)) {
				} else
					return new ParamValidationResults(false,
							"Kernel Width should between 0.0 and 1.0");
			} catch (NumberFormatException nfe) {
				return new ParamValidationResults(false,
						"Kernel Width should be a float number between 0.0 and 1.0.");
			}
			;
		}
		if (params.isDPIToleranceSpecified()) {
			try {
				if ((params.getDPITolerance() != Float.NaN)
						&& (0 <= params.getDPITolerance())
						&& (params.getDPITolerance() <= 1)) {
				} else
					return new ParamValidationResults(false,
							"DPI Tolerance should between 0.0 and 1.0");
			} catch (NumberFormatException nfe) {
				return new ParamValidationResults(false,
						"DPI Tolerance should be a float number between 0.0 and 1.0.");
			}
			;
		}
		if (params.isTargetListSpecified()) {
			if (params.getTargetGenes() == null
					|| params.getTargetGenes().size() == 0) {
				return new ParamValidationResults(false,
						"You did not load any target genes.");
			}
		}
		if (params.getBootstrapNumber() <= 0) {
			return new ParamValidationResults(false,
					"Bootstrap number is not valid.");
		}
		if (params.getConsensusThreshold() <= 0
				|| params.getConsensusThreshold() > 1) {
			return new ParamValidationResults(false,
					"Consensus threshold is not valid.");
		}
		return new ParamValidationResults(true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonParameters()
	 */
	@Override
	protected Map<Serializable, Serializable> getBisonParameters() {
		log.debug("Reading bison parameters");

		Map<Serializable, Serializable> bisonParameters = new HashMap<Serializable, Serializable>();
		AracneParamPanel paramPanel = (AracneParamPanel) this.aspp;

		bisonParameters.put("isDPISpecified", paramPanel
				.isDPIToleranceSpecified());
		if (paramPanel.isDPIToleranceSpecified()) {
			float dpiTolerence = paramPanel.getDPITolerance();
			bisonParameters.put("dpi", dpiTolerence);
		}
		bisonParameters.put("isKernelWidthSpecified", paramPanel
				.isKernelWidthSpecified());
		if (paramPanel.isKernelWidthSpecified()) {
			float kernelWidth = paramPanel.getKernelWidth();
			bisonParameters.put("kernel", kernelWidth);
		}

		/* TODO allow user to enter many markers or a file of markers */
		String hubGene = paramPanel.getHubGeneString();
		bisonParameters.put("isHubListSpecified", paramPanel
				.isHubListSpecified());
		if (paramPanel.isHubListSpecified())
			bisonParameters.put("hub", hubGene);
		else
			bisonParameters.put("hub", "");

		ArrayList<String> targetGeneList = paramPanel.getTargetGenes();
		String targetGene = "";
		boolean isFirst = true;
		for (java.util.Iterator<String> iterator = targetGeneList.iterator(); iterator
				.hasNext();) {
			if (isFirst)
				isFirst = false;
			else
				targetGene += ",";
			String gene = (String) iterator.next();
			targetGene += gene;
		}
		bisonParameters.put("isTargetListSpecified", paramPanel
				.isTargetListSpecified());
		if (paramPanel.isTargetListSpecified())
			bisonParameters.put("target", targetGene);
		else
			bisonParameters.put("target", "");

		bisonParameters.put("isMI", paramPanel.isThresholdMI());

		float threshold = paramPanel.getThreshold();
		bisonParameters.put("threshold", threshold);

		int bootstrapNumber = paramPanel.getBootstrapNumber();
		bisonParameters.put("bootstrapNumber", bootstrapNumber);
		double consensusThreshold = paramPanel.getConsensusThreshold();
		bisonParameters.put("consensusThreshold", consensusThreshold);

		return bisonParameters;
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
	 * @see org.geworkbench.analysis.AbstractGridAnalysis#getBisonReturnType()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class getBisonReturnType() {
		return AdjacencyMatrix.class;
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
	@SuppressWarnings("unchecked")
	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet refMASet) {
		AracneParamPanel params = (AracneParamPanel) aspp;
		if (params.isHubListSpecified()) {
			ArrayList<String> hubGeneList = params.getHubGeneList();
			for (String modGene : hubGeneList) {
				DSGeneMarker marker = maSetView.markers().get(modGene);
				if (marker == null) {
					log.info("Couldn't find marker " + modGene
							+ " specified as hub gene in microarray set.");
					return new ParamValidationResults(
							false,
							"Couldn't find marker "
									+ modGene
									+ " specified as hub gene in microarray set.");
				}
			}
		}

		return new ParamValidationResults(true, "No Error");
	}

	/**
	 * 
	 * @param e
	 * @param source
	 */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		DSDataSet dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			AracneParamPanel params = (AracneParamPanel) aspp;
			params.adjMode((AdjacencyMatrixDataSet) dataSet);
		} else if (dataSet instanceof DSMicroarraySet) {
			AracneParamPanel params = (AracneParamPanel) aspp;
			params.maMode();
		}
	}

	/**
	 * 
	 * @param e
	 * @param source
	 */
	@SuppressWarnings("unchecked")
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodePostCompletedEvent e,
			Object source) {
		DSDataSet dataSet = e.getAncillaryDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			AdjacencyMatrixDataSet adjMatrixDataSet = (AdjacencyMatrixDataSet) dataSet;
			if (adjMatrixDataSet.getMatrix().getConnectionNo() == 0)
				tellUserToRelaxThresholds();
		}
	}

	/**
	 * Show user there's no result, please relax their thresholds. TODO: we
	 * probably can disable the GUI interaction if in head-less unit test or
	 * batch script mode.
	 */
	private void tellUserToRelaxThresholds() {
		JOptionPane.showMessageDialog(null,
				"The ARACNE run resulted in no adjacent genes, "
						+ "consider relaxing your thresholds.");
	}

	@SuppressWarnings("unchecked")
	public String generateHistoryString(DSMicroarraySetView maSetView) {
		String ans = "";

		//TODO: this probably should get from DSMicroarraySetView.toString()
		
		// generate text for microarrays/groups
		ans += "=The MicroarraySetView used for analysis contains following data=\n";
		try {
			log.debug("We got a "+maSetView.items().getClass().toString());
			if (maSetView.items().getClass() == CSPanel.class){
				log.debug("situation 1: microarraySets selected");
				DSItemList paneltest = ((DSPanel) maSetView.items()).panels();
				Iterator groups2 = paneltest.iterator(); // groups
				ans += "==Microarray Sets [" + paneltest.size() + "]==\n";
				while (groups2.hasNext()) {
					DSPanel temp = (DSPanel) groups2.next();
					ans += "\t" + temp.toString() + "\n";
					Iterator groups3 = temp.iterator(); // microarrays in the group
					while (groups3.hasNext()) {
						Object temp2 = groups3.next();
						ans += "\t\t" + temp2.toString() + "\n";
					}
				}
			}else if (maSetView.items().getClass() == CSExprMicroarraySet.class){
				log.debug("situation 2: microarraySets not selected");
				CSExprMicroarraySet exprSet = (CSExprMicroarraySet)maSetView.items();
				ans += "==Used Microarrays [" + exprSet.size() + "]==\n";
				for (Iterator<DSMicroarray> iterator = exprSet.iterator(); iterator.hasNext();) {
					DSMicroarray array = iterator.next();
					ans += "\t"+ array.getLabel()+"\n";
				}
			}
			ans += "==End of Microarray Sets==\n";
			// generate text for markers
			DSItemList paneltest = maSetView.getMarkerPanel();
			if ((paneltest!=null) && (paneltest.size()>0)){
				log.debug("situation 3: markers selected");
				Iterator groups2 = paneltest.iterator(); // groups
				ans += "==Used Markers [" + paneltest.size() + "]==\n";
				while (groups2.hasNext()) {
					CSExpressionMarker temp = (CSExpressionMarker) groups2.next();
					ans += "\t" + temp.getLabel() + "\n";
				}
			}else{
				log.debug("situation 4: no markers selected.");
				DSItemList<DSGeneMarker> markers = maSetView.markers();
				ans += "==Used Markers [" + markers.size() + "]==\n";
				for (Iterator iterator = markers.iterator(); iterator.hasNext();) {
					DSGeneMarker marker = (DSGeneMarker) iterator.next();
					ans += "\t" + marker.getLabel() + "\n";
				}
			}
			ans += "==End of Used Markers==\n";
		} catch (ClassCastException cce) {
			// it's not a DSPanel, we generate nothing for panel part
			log.error(cce);
		}
		ans += "=End of MicroarraySetView data=";
		return ans;
	}
	
}
