package org.geworkbench.components.aracne;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

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
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.ginkgo.labs.util.FileTools;

import wb.data.Marker;
import wb.data.MarkerSet;
import wb.data.Microarray;
import wb.data.MicroarraySet;
import wb.plugins.aracne.GraphEdge;
import wb.plugins.aracne.WeightedGraph;
import edu.columbia.c2b2.aracne.Parameter;

/**
 * @author Matt Hall
 * @version $Id$
 */
public class AracneAnalysis extends AbstractGridAnalysis implements
		ClusteringAnalysis {

	private static final long serialVersionUID = -4501531893816533232L;

	private static final int MINIMUM_ARRAY_NUMBER = 100;

	static Log log = LogFactory.getLog(AracneAnalysis.class);

	private DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView;
	private AdjacencyMatrixDataSet adjMatrix;
	private final String analysisName = "Aracne";
	/**
	 *
	 */
	public AracneAnalysis() {
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
			mSetView = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;
		} else if (input instanceof AdjacencyMatrixDataSet) {
			log
					.debug("Input dataset is adjacency matrix, will only perform DPI.");
			adjMatrix = (AdjacencyMatrixDataSet) input;
			mSetView.setDataSet(adjMatrix.getParentDataSet());
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
				    "You have chosen 'All vs All' for hub markers. This could take a LONG time. Do you want to continue?",
				    "'All vs All' confirmation",
				    JOptionPane.YES_NO_OPTION);
			if(n==JOptionPane.NO_OPTION)
				return null;

		}
		if (params.isThresholdMI()) {
			p.setThreshold(params.getThreshold());
		} else {
	    	if (!params.noCorrection() && mSetView != null && mSetView.markers().size() > 0)
	    		p.setPvalue(params.getThreshold() / mSetView.markers().size());
	    	else
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

		Parameter.ALGORITHM algor = params.getAlgorithm();
		p.setAlgorithm(algor);

		Parameter.MODE mode = params.getMode();
		p.setMode(mode);

		String dataSetName = mSetView.getDataSet().getDataSetName();
		String DATASETNAME_ALGORITHM_kernel_file = params.getKernelFile(dataSetName);
		String DATASETNAME_ALGORITHM_threshold_file = params.getThresholdFile(dataSetName);

		p.setKernelFile(DATASETNAME_ALGORITHM_kernel_file);
		p.setThresholdFile(DATASETNAME_ALGORITHM_threshold_file);

		int bs = params.getBootstrapNumber();
		double pt = params.getConsensusThreshold();
		if (bs <= 0 || pt <= 0 || pt > 1)
			return null;
		
		if(mSetView.size()<MINIMUM_ARRAY_NUMBER) {
			int n = JOptionPane.showConfirmDialog(
				    null,
				    "ARACNe should not in general be run on less than "+MINIMUM_ARRAY_NUMBER+" arrays. Do you want to continue?",
				    "Too few arrays",
				    JOptionPane.YES_NO_OPTION);
			if(n!=JOptionPane.YES_OPTION)
				return null;
		}

		AracneThread aracneThread = new AracneThread(mSetView, p, bs, pt, params.isPrune());

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

		DSItemList<DSGeneMarker> markers = mSet.markers();
		for (DSGeneMarker marker : markers) {
			log.debug(marker.getLabel() + "added");
			graph.addEdge(marker.getLabel(), marker.getLabel(), 0);
		}
		for (AdjacencyMatrix.Edge edge : matrix.getEdges()) {
			if (edge.node1.type==NodeType.MARKER) {
				DSGeneMarker gene1 = edge.node1.marker;
				if (edge.node2.type==NodeType.MARKER) {
					DSGeneMarker destGene = edge.node2.marker;
					graph.addEdge(gene1.getLabel(), destGene.getLabel(),
							edge.info.value);
				} else {
					log.debug("Gene with index " + edge.node2
							+ " not found in selected genes, skipping.");
				}
			} else {
				log.debug("Gene with index " + edge.node1
						+ " not found in selected genes, skipping.");
			}
		}
		return graph;
	}

	// a very old comment (revision 4310) saying this method has a bug is removed
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
			DSMicroarraySet<DSMicroarray> mSet, boolean prune) {
		AdjacencyMatrix matrix = new AdjacencyMatrix(null, mSet);

		int nNode = 0, nEdge = 0;
		if (!prune) {
			for (String node : graph.getNodes()) {
				DSGeneMarker marker = mSet.getMarkers().get(node);
				matrix.addGeneRow(new AdjacencyMatrix.Node(marker));
				nNode++;
			}
			for (GraphEdge graphEdge : graph.getEdges()) {
				DSGeneMarker marker1 = mSet.getMarkers().get(
						graphEdge.getNode1());
				DSGeneMarker marker2 = mSet.getMarkers().get(
						graphEdge.getNode2());
				matrix.add(new AdjacencyMatrix.Node(marker1),
						new AdjacencyMatrix.Node(marker2),
						graphEdge.getWeight(), null);
				nEdge++;
			}
		} else {
			for (String node : graph.getNodes()) {
				DSGeneMarker marker = mSet.getMarkers().get(node);
				matrix.addGeneRow(new AdjacencyMatrix.Node(NodeType.STRING, marker.getGeneName()));
				nNode++;
			}
			for (GraphEdge graphEdge : graph.getEdges()) {
				DSGeneMarker marker1 = mSet.getMarkers().get(
						graphEdge.getNode1());
				DSGeneMarker marker2 = mSet.getMarkers().get(
						graphEdge.getNode2());
				matrix.add(new AdjacencyMatrix.Node(NodeType.STRING, marker1.getGeneName()),
						new AdjacencyMatrix.Node(NodeType.STRING, marker2.getGeneName()),
						graphEdge.getWeight());
				nEdge++;
			}
		}
		log.debug("node count "+nNode + "; edge count " + nEdge);
		return matrix;
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
		
		private boolean prune;

		public AracneThread(
				DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSet,
				Parameter p, int bootstrapNumber, double pThreshold, boolean prune) {
			this.mSetView = mSet;
			this.p = p;

			this.bootstrapNumber = bootstrapNumber;
			this.pThreshold = pThreshold;
			
			this.prune = prune;
		}

		public void run() {
			log.debug("Running ARACNE in worker thread.");
			p.setSuppressFileWriting(true);
			try {
				weightedGraph = HardenedAracne.run(convert(mSetView), p,
						bootstrapNumber, pThreshold);
			} catch (Exception e) {
				progressWindow.stopProgress();
				showMessage("Exception caught in ARACNe run: "+e.toString());
				return;
			}
			log.debug("Done running ARACNE in worker thread.");
			progressWindow.stopProgress();

			/* done if in PREPROCESSING mode*/
			if (this.p.getMode().equals(Parameter.MODE.PREPROCESSING)) {
				return;
			}

			if (weightedGraph.getEdges().size() > 0) {
				AdjacencyMatrixDataSet dataSet = new AdjacencyMatrixDataSet(
						convert(weightedGraph, mSetView.getMicroarraySet(), prune),
						0, "Adjacency Matrix", "ARACNE Set", mSetView
								.getMicroarraySet());
				StringBuilder paramDescB = new StringBuilder(
						"Generated with ARACNE run with data:\n");
				paramDescB.append(generateHistoryString(this.mSetView));
				ProjectPanel.addToHistory(dataSet,
						"Generated with ARACNE run with paramters:\n"
								+ p.getParamterDescription()
								+ dpiTargetListDescription()+"\n"
								+ hubMarkersDescription(p)
								+ paramDescB.toString());
				publishProjectNodeAddedEvent(new ProjectNodeAddedEvent(
						"Adjacency Matrix Added", null, dataSet));
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
		
		private String dpiTargetListDescription(){
			String listString="[PARA] DPI Target List: ";
			AracneParamPanel params = (AracneParamPanel) aspp;
			listString+=params.getTargetGeneString();		
			return listString;
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

	// this is meant to be called from non-EDT thread
	private static void showMessage(final String message) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				JOptionPane.showMessageDialog(null, message);
			}
			
		};
		try {
			SwingUtilities.invokeAndWait(runnable);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
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
				if ( 0 <= params.getThreshold() ) {
				} else {
					return new ParamValidationResults(false,
							"Threshold Mutual Info. should be larger than or equal to zero.");
				}
			} catch (NumberFormatException nfe) {
				return new ParamValidationResults(false,
						"Threshold Mutual Info. should be a float number larger than or equal to zero.");
			}
			;
		} else {
			try {
				if ((0 <= params.getThreshold())
						&& (params.getThreshold() <= 1)) {
				} else
					return new ParamValidationResults(false,
							"Threshold P-Value should be between 0.0 and 1.0");
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

		bisonParameters.put("isMI", paramPanel.isThresholdMI());

		float threshold = paramPanel.getThreshold();
		if (!paramPanel.isThresholdMI() && !paramPanel.noCorrection() && mSetView!=null && mSetView.markers().size() > 0){
    		threshold = threshold / mSetView.markers().size();
    		paramPanel.pval = threshold;
		}
		bisonParameters.put("threshold", threshold);

		int bootstrapNumber = paramPanel.getBootstrapNumber();
		bisonParameters.put("bootstrapNumber", bootstrapNumber);
		double consensusThreshold = paramPanel.getConsensusThreshold();
		bisonParameters.put("consensusThreshold", consensusThreshold);

		String algorithm = null;
    	if (paramPanel.getAlgorithm().equals(Parameter.ALGORITHM.ADAPTIVE_PARTITIONING)){
    		algorithm = AracneParamPanel.ADAPTIVE_PARTITIONING;
    	} else if (paramPanel.getAlgorithm().equals(Parameter.ALGORITHM.FIXED_BANDWIDTH)){
    		algorithm = AracneParamPanel.FIXED_BANDWIDTH;
    	} else {
    		log.error("wrong algorithm in parameters");
    	}
		bisonParameters.put("algorithm", algorithm);

		String mode = null;
    	if (paramPanel.getMode().equals(Parameter.MODE.COMPLETE)){
    		mode = AracneParamPanel.COMPLETE;
    	} else if (paramPanel.getMode().equals(Parameter.MODE.PREPROCESSING)){
    		mode = AracneParamPanel.PREPROCESSING;
    	} else if (paramPanel.getMode().equals(Parameter.MODE.DISCOVERY)){
    		mode = AracneParamPanel.DISCOVERY;
    	} else {
    		log.error("wrong mode in parameters");
    	}
		bisonParameters.put("mode", mode);

	    // bug #1997
		String dataSetName = paramPanel.getMaSetName();
		String DATASETNAME_ALGORITHM_kernel_file = paramPanel.getKernelFile(dataSetName);
		String DATASETNAME_ALGORITHM_threshold_file = paramPanel.getThresholdFile(dataSetName);

		bisonParameters.put("kernalFile", DATASETNAME_ALGORITHM_kernel_file);
		bisonParameters.put("thresholdFile", DATASETNAME_ALGORITHM_threshold_file);

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
	@Override
	public Class<AdjacencyMatrix> getBisonReturnType() {
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
	@Override
	public ParamValidationResults validInputData(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView,
			DSDataSet<?> refMASet) {
		AracneParamPanel params = (AracneParamPanel) aspp;
		mSetView = maSetView;
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
		DSDataSet<?> dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			AracneParamPanel params = (AracneParamPanel) aspp;
			params.adjMode((AdjacencyMatrixDataSet) dataSet);
		} else if (dataSet instanceof DSMicroarraySet) {
			AracneParamPanel params = (AracneParamPanel) aspp;
			/* This following line is added only at the point when the mark set info is needed for parameter panel. */
			params.setMicroarraySet((DSMicroarraySet<DSMicroarray>)dataSet);
			params.maMode();
		}
	}

	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null) {
			DSPanel<DSGeneMarker> selectorPanel = e.getPanel();
			((AracneParamPanel) aspp).setSelectorPanel(selectorPanel);
		} else
			log.debug("Aracne Received Gene Selector Event: Selection panel sent was null");
	}

	/**
	 *
	 * @param e
	 * @param source
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectNodePostCompletedEvent e,
			Object source) {
		DSDataSet<?> dataSet = e.getAncillaryDataSet();
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
	private String generateHistoryString(DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView) {
		StringBuilder ans = new StringBuilder(
				"=The MicroarraySetView used for analysis contains following data=\n");
		try {
			log.debug("We got a "+maSetView.items().getClass().toString());
			if (maSetView.items().getClass() == CSPanel.class){
				log.debug("situation 1: microarraySets selected");
				DSItemList<DSPanel<DSMicroarray>> paneltest = ((DSPanel<DSMicroarray>) maSetView.items()).panels();

				ans .append( "==Microarray Sets [" ).append( paneltest.size() ).append( "]==\n" );
				for (Object obj : paneltest) {
					DSPanel<DSMicroarray> temp = (DSPanel<DSMicroarray>) obj;
					ans .append( "\t" ).append( temp.toString() ).append( "\n" );
					// microarrays in the group
					for (Object temp2 : temp) {
						ans .append( "\t\t" ).append( temp2.toString() ).append( "\n" );
					}
				}
			}else if (maSetView.items().getClass() == CSExprMicroarraySet.class){
				log.debug("situation 2: microarraySets not selected");
				CSExprMicroarraySet exprSet = (CSExprMicroarraySet)maSetView.items();
				ans .append( "==Used Microarrays [" ).append( exprSet.size() ).append( "]==\n" );
				for (Iterator<DSMicroarray> iterator = exprSet.iterator(); iterator.hasNext();) {
					DSMicroarray array = iterator.next();
					ans .append( "\t"+ array.getLabel()).append("\n");
				}
			}
			ans .append( "==End of Microarray Sets==\n" );
			// generate text for markers; iterations over markers could be refactored into one
			DSPanel<DSGeneMarker> paneltest = maSetView.getMarkerPanel();

			if (maSetView.useMarkerPanel()) {
				if ((paneltest!=null) && (paneltest.size()>0)){
					log.debug("situation 3: markers selected");

					ans .append( "==Used Markers [" ).append( paneltest.size() ).append( "]==\n" );
					for (Object obj : paneltest) {
						CSExpressionMarker temp = (CSExpressionMarker) obj;
						ans .append( "\t" ).append( temp.getLabel() ).append( "\n" );
					}
				}else{
					log.debug("situation 4: no markers selected.");
					DSItemList<DSGeneMarker> markers = maSetView.markers();
					ans .append( "==Used Markers [" ).append( markers.size() ).append( "]==\n" );
					for (DSGeneMarker marker : markers) {
						ans .append( "\t" ).append( marker.getLabel() ).append( "\n" );
					}
				}
			} else {
				log.debug("situation 5: All Markers selected.");
				DSItemList<DSGeneMarker> markers = maSetView.allMarkers();
				ans .append( "==Used Markers [" ).append( markers.size() ).append( "]==" )
						.append( FileTools.NEWLINE );
				for (DSGeneMarker marker : markers) {
					ans .append( FileTools.TAB ).append( marker.getLabel()
					).append( FileTools.NEWLINE );
				}
			}


			ans .append( "==End of Used Markers==\n" );
		} catch (ClassCastException cce) {
			// it's not a DSPanel, we generate nothing for panel part
			log.error(cce);
		}
		ans .append( "=End of MicroarraySetView data=" );
		return ans.toString();
	}

}
