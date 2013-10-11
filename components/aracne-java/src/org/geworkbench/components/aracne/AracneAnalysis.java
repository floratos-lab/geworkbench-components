package org.geworkbench.components.aracne;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractGridAnalysis;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrix.NodeType;
import org.geworkbench.bison.datastructure.biocollections.AdjacencyMatrixDataSet;
import org.geworkbench.bison.datastructure.biocollections.AracneResult;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
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
import org.geworkbench.components.aracne.data.AracneGraphEdge;
import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.GeneSelectorEvent;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.ProgressBar;

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
	 * @see org.geworkbench.bison.model.analysis.Analysis#execute(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public AlgorithmExecutionResults execute(Object input) {
		if (!(input instanceof DSMicroarraySetView)) {
			log.error("Invalid type passed to Aracne analysis: "+input.getClass().getName());
			return null;
		}
		log.debug("input: " + input);
		AracneParamPanel params = (AracneParamPanel) aspp;
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> mSetView = (DSMicroarraySetView<DSGeneMarker, DSMicroarray>) input;

		final AracneInput aracneInput = new AracneInput();
		if (params.isHubListSpecified()) {			 
			ArrayList<String> hubGeneList = params.getHubGeneList();
			aracneInput.setHubGeneList(hubGeneList.toArray(new String[0]));
		} else {
			int n = JOptionPane.showConfirmDialog(
				    null,
				    "You have chosen 'All vs All' for hub markers. This could take a LONG time. Do you want to continue?",
				    "'All vs All' confirmation",
				    JOptionPane.YES_NO_OPTION);
			if(n==JOptionPane.NO_OPTION)
				return null;

		}
		
		aracneInput.setIsThresholdMI(params.isThresholdMI());
		aracneInput.setThreshold(params.getThreshold());
		aracneInput.setNoCorrection(params.noCorrection());
		aracneInput.setIsKernelWidthSpecified(params.isKernelWidthSpecified());
		aracneInput.setKernelWidth(params.getKernelWidth());
		aracneInput.setIsDPIToleranceSpecified(params.isDPIToleranceSpecified());
		aracneInput.setDPITolerance(params.getDPITolerance());
		
		if (params.isTargetListSpecified()) {			 
			aracneInput.setTargetGeneList(params.getTargetGenes().toArray(new String[0]));
		}
		 
		aracneInput.setAlgorithm(params.getAlgorithmAsString());		 
		aracneInput.setMode(params.getModeAsString());
		String dataSetName = mSetView.getDataSet().getDataSetName();
		aracneInput.setDataSetName(dataSetName);
		aracneInput.setDataSetIdentifier(mSetView.getDataSet().getID());
	 
		int bs = params.getBootstrapNumber();
		float pt = (float)params.getConsensusThreshold();
	 
		aracneInput.setBootstrapNumber(bs);
		aracneInput.setConsensusThreshold(pt);
		
		String[] markers = new String[mSetView.markers().size()];		
		int i =0, j=0;
		for (DSGeneMarker marker : mSetView.markers()) {
			markers[i++] = marker.getLabel();
		}
		aracneInput.setMarkers(markers);
		
		DSItemList<DSMicroarray> arrays = mSetView.items();
		String[] arrayNames = new String[arrays.size()];
		float[][] markerValues = new float[arrayNames.length][markers.length];
		i = 0;	 
		for (DSMicroarray microarray : arrays) {	
			j = 0;
			for (DSGeneMarker marker : mSetView.markers()) {
				markerValues[i][j++] = (float) microarray.getMarkerValue(marker)
						.getValue();
			}
			arrayNames[i++] = microarray.getLabel();
		}
		
		aracneInput.setMicroarrayNames(arrayNames);
		aracneInput.setMarkerValues(markerValues);
		
		//System.out.println(aracneInput.toString());
		
		AracneComputation aracneComputation = new AracneComputation(aracneInput);
		
        ProgressBar progressBar = ProgressBar.create(ProgressBar.INDETERMINATE_TYPE);
		progressBar.addObserver(new CancelObserver(aracneComputation));
		progressBar.setTitle("ARACNE");
		progressBar.setMessage("ARACNE Process Running");
		progressBar.start();
		AracneOutput aracneOutput;
		try
        {
		    aracneOutput = aracneComputation.execute();
        }
        catch (AracneException e) {
			if (progressBar != null) {
				progressBar.dispose();
			}
			e.printStackTrace();
			return new AlgorithmExecutionResults(false,
					"Exception happened in aracne computaiton: " + e, null);
		}
        
		progressBar.stop();
		if(aracneOutput==null) { // likely cancelled
			return null;
		}
		
		if (aracneOutput.getGraphEdges().length > 0) {
			boolean prune = params.isPrune();
			AracneResult dataSet = new AracneResult(
					convert(aracneOutput, params.getHubGeneList(), mSetView.getMicroarraySet(), prune),
					0, "Adjacency Matrix", "ARACNE Set", mSetView
							.getMicroarraySet(), params.getHubGeneList());
			StringBuilder paramDescB = new StringBuilder(
					"Generated with ARACNE run with data:\n");
			paramDescB.append(this.generateHistoryForMaSetView(mSetView));
			String s=prune?"yes":"no";
			String hubSetting = params.getHubSetting();
			String historyStr = "Generated with ARACNE run with paramters:\n"
				+ aracneOutput.getParamterDescription()	
				+ "[PARA] Bootstrapping: " + aracneInput.getBootstrapNumber() + "\n";
			if (aracneInput.getBootstrapNumber() > 1)
				historyStr += "[PARA] Consensus Threshold: " + aracneInput.getConsensusThreshold() + "\n";
			historyStr += dpiTargetListDescription()+"\n"
			           + "[PARA] Merge multiple probesets: "+ s+"\n"
			           + "[PARA] Setting for Hub Markers: " + hubSetting +"\n"
			           + params.hubMarkersDescription()
			           + paramDescB.toString();
			
			HistoryPanel.addToHistory(dataSet, historyStr);
					 
			return new AlgorithmExecutionResults(true, "ARACNE Done.", dataSet);

		} else {
			this.tellUserToRelaxThresholds();
			return null;
		}

	}
	
	static private class CancelObserver implements Observer {
		
		private final AracneComputation aracneComputation;

		CancelObserver(final AracneComputation aracneComputation) {
			super();
			this.aracneComputation = aracneComputation;
		}
		
		@Override
		public void update(Observable o, Object arg) {
			aracneComputation.cancel();
		}
		
	}

	private String dpiTargetListDescription(){
		String listString="[PARA] DPI Target List: ";
		AracneParamPanel params = (AracneParamPanel) aspp;
		listString+=params.getTargetGeneString();		
		return listString;
	}

	

	/**
	 * Convert the result from aracne-java to an AdjacencyMatrix object.
	 * @param graph
	 * @param p 
	 * @param mSet
	 * @return
	 */
	private static AdjacencyMatrix convert(AracneOutput aracneOutput, ArrayList<String> hubGeneList,
			DSMicroarraySet mSet, boolean prune) {
		AdjacencyMatrix matrix = new AdjacencyMatrix(null);
		AracneGraphEdge[] aracneGraphEdges= aracneOutput.getGraphEdges();
	    if (aracneGraphEdges == null || aracneGraphEdges.length == 0)
	    	return matrix;
	    
		int nEdge = 0;
		for (int i=0; i<aracneGraphEdges.length; i++) {
			DSGeneMarker marker1 = mSet.getMarkers().get(aracneGraphEdges[i].getNode1());
			DSGeneMarker marker2 = mSet.getMarkers().get(aracneGraphEdges[i].getNode2());
			
			if (!hubGeneList.contains(marker1.getLabel())) {
				DSGeneMarker m = marker1;
				marker1 = marker2;
				marker2 = m;
			}

			AdjacencyMatrix.Node node1, node2;
			if (!prune) {
				node1 = new AdjacencyMatrix.Node(marker1);
				node2 = new AdjacencyMatrix.Node(marker2);
				matrix.add(node1, node2, aracneGraphEdges[i].getWeight(), null);
			} else {
				node1 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
						marker1.getGeneName());
				node2 = new AdjacencyMatrix.Node(NodeType.GENE_SYMBOL,
						marker2.getGeneName());
				matrix.add(node1, node2, aracneGraphEdges[i].getWeight());
			}
			nEdge++;
		}
		log.debug("edge count " + nEdge);
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
				if ( (0 <= params.getDPITolerance())
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
		bisonParameters.put("isTargetListSpecified", paramPanel.isTargetListSpecified());
		bisonParameters.put("target", targetGene);
		bisonParameters.put("prune", paramPanel.isPrune());
		bisonParameters.put("isMI", paramPanel.isThresholdMI());

		bisonParameters.put("noCorrection", paramPanel.noCorrection());
		bisonParameters.put("threshold", paramPanel.getThreshold());

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
	public Class<AracneResult> getBisonReturnType() {
		return AracneResult.class;
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

		if(maSetView.size()<MINIMUM_ARRAY_NUMBER) {
			int n = JOptionPane.showConfirmDialog(
				    null,
				    "ARACNe should not in general be run on less than "+MINIMUM_ARRAY_NUMBER+" arrays. Do you want to continue?",
				    "Too few arrays",
				    JOptionPane.YES_NO_OPTION);
			if(n!=JOptionPane.YES_OPTION)
				return new ParamValidationResults(true, "QUIT");
		}

		return new ParamValidationResults(true, "No Error");
	}

	/**
	 *
	 * @param e
	 * @param source
	 */
	@Subscribe
	public void receive(org.geworkbench.events.ProjectEvent e, Object source) {
		DSDataSet<?> dataSet = e.getDataSet();
		if (dataSet instanceof AdjacencyMatrixDataSet) {
			AracneParamPanel params = (AracneParamPanel) aspp;
			params.adjMode((AdjacencyMatrixDataSet) dataSet);
		} else if (dataSet instanceof DSMicroarraySet) {
			AracneParamPanel params = (AracneParamPanel) aspp;
			/* This following line is added only at the point when the mark set info is needed for parameter panel. */
			params.setMicroarraySet((DSMicroarraySet)dataSet);
			params.maMode();
		}
	}

	@Subscribe
	public void receive(GeneSelectorEvent e, Object source) {
		if (e.getPanel() != null) {
			final DSPanel<DSGeneMarker> selectorPanel = e.getPanel();
			if (SwingUtilities.isEventDispatchThread()) {
				((AracneParamPanel) aspp).setSelectorPanel(selectorPanel);
			} else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							((AracneParamPanel) aspp)
									.setSelectorPanel(selectorPanel);
						}

					});
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}
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

}
