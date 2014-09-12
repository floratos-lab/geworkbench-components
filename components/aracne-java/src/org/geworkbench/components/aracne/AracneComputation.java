package org.geworkbench.components.aracne;

import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wb.data.Marker;
import wb.data.MarkerSet;
import wb.data.Microarray;
import wb.data.MicroarraySet;
import wb.plugins.aracne.GraphEdge;
import wb.plugins.aracne.WeightedGraph;
import edu.columbia.c2b2.aracne.Parameter;

import org.geworkbench.components.aracne.AracneException;
import org.geworkbench.components.aracne.data.AracneGraphEdge;
import org.geworkbench.components.aracne.data.AracneInput;
import org.geworkbench.components.aracne.data.AracneOutput;

/**
 * ARACNe computation.
 */
public class AracneComputation {

	private static Log log = LogFactory.getLog(AracneComputation.class);

	public static final String FIXED_BANDWIDTH = "Fixed Bandwidth";
	public static final String ADAPTIVE_PARTITIONING = "Adaptive Partitioning";

	public static final String FIXED = "ARACNe_FBW";
	public static final String ADAPTIVE = "ARACNe_AP";

	public static final String COMPLETE = "Complete";
	public static final String PREPROCESSING = "Preprocessing";
	public static final String DISCOVERY = "Discovery";

	private AracneInput aracneInput;
	 
	private HardenedAracne hardenedAracne = new HardenedAracne();

	public AracneComputation(final AracneInput aracneInput) {
		this.aracneInput = aracneInput;	 
	}

	void cancel() {
		hardenedAracne.cancelled = true;
	}

	public AracneOutput execute() throws AracneException {

		try {
			
			
			validateAracneInput();
			MicroarraySet microarraySet = getMicroarraySet();
			final Parameter p = getParamter(); 	
			int bootstrapNumber = aracneInput.getBootstrapNumber();
			float pThreshold = aracneInput.getConsensusThreshold();		 
			switch (p.getMode()) {
			 
			case PREPROCESSING: // ignore return
				hardenedAracne.run(microarraySet, p, 1, pThreshold);
				return null;
			case DISCOVERY:				 
				return convert(hardenedAracne.run(microarraySet, p, bootstrapNumber,			 
						pThreshold), p.getParamterDescription());				 
			case COMPLETE: // do not use this mode, re-create the process
				p.setMode(Parameter.MODE.PREPROCESSING);
				hardenedAracne.run(microarraySet, p, 1, pThreshold);
				if (hardenedAracne.cancelled)
					return null;
				p.setMode(Parameter.MODE.DISCOVERY);			 
				return convert(hardenedAracne.run(microarraySet, p, bootstrapNumber,			 
						pThreshold), p.getParamterDescription());
			}
		}catch (AracneException e) {			 
			throw new AracneException(e.getMessage());
		}catch (Exception e) {
		    log.warn("Exception caught in ARACNe run: " + e.getMessage());
		    throw new AracneException(e.getMessage());
	    }
		return null;
	}

	private MicroarraySet getMicroarraySet() {
		String[] markers = aracneInput.getMarkers();
		String[] microarrayNames = aracneInput.getMicroarrayNames();
		float[][] markerValues = aracneInput.getMarkerValues();

		MarkerSet markerSet = new MarkerSet();

		for (int i = 0; i < markers.length; i++)
			markerSet.addMarker(new Marker(markers[i]));

		MicroarraySet returnSet = new MicroarraySet(
				aracneInput.getDataSetName(), aracneInput.getDataSetIdentifier(),
				"Unknown", markerSet);

		for (int i = 0; i < microarrayNames.length; i++) {
			float[] markerData = markerValues[i];
			returnSet.addMicroarray(new Microarray(microarrayNames[i],
					markerData));
		}
		return returnSet;
	}

	private Parameter getParamter() {
		final Parameter p = new Parameter();
		String[] hubGenes = aracneInput.gethubGeneList();
		int hubGeneCnt = 1; //avoid divide by zero if none set.
		if (hubGenes != null && hubGenes.length > 0) {
		    hubGeneCnt = hubGenes.length;
			Vector<String> hubGeneList = new Vector<String>();
			for (int i = 0; i < hubGenes.length; i++)
				hubGeneList.add(hubGenes[i]);
			p.setSubnet(new Vector<String>(hubGeneList));
		}

		if (aracneInput.getIsThresholdMI()) {
			p.setThreshold(aracneInput.getThreshold());
		} else {
			if (!aracneInput.getNoCorrection())
				p.setPvalue(aracneInput.getThreshold()
						/ (aracneInput.getMarkers().length * hubGeneCnt));
			else
				p.setPvalue(aracneInput.getThreshold());
		}

		if (aracneInput.getIsKernelWidthSpecified()) {
			p.setSigma(aracneInput.getKernelWidth());
		}
		if (aracneInput.getIsDPIToleranceSpecified()) {
			p.setEps(aracneInput.getDPITolerance());
		}
		String[] targetGenes = aracneInput.getTargetGeneList();
		if (targetGenes != null && targetGenes.length > 0) {
			Vector<String> targetGeneList = new Vector<String>();
			for (int i = 0; i < targetGenes.length; i++)
				targetGeneList.add(targetGenes[i]);
			p.setTf_list(new Vector<String>(targetGeneList));
		}

		p.setAlgorithm(getAlgorithm());
		p.setMode(getMode());

		String DATASETNAME_ALGORITHM_kernel_file = aracneInput.getDataSetName()
				+ "_" + getAlgorithmForFileName() + "_" + "kernel.txt";
		String DATASETNAME_ALGORITHM_threshold_file = aracneInput
				.getDataSetName()
				+ "_"
				+ getAlgorithmForFileName()
				+ "_"
				+ "threshold.txt";

		p.setKernelFile(DATASETNAME_ALGORITHM_kernel_file);
		p.setThresholdFile(DATASETNAME_ALGORITHM_threshold_file);
		p.setSuppressFileWriting(true);

		return p;
	}

	private void validateAracneInput() throws AracneException{
		if (aracneInput.getIsThresholdMI()) {
			 
			if (aracneInput.getThreshold() < 0 )  				 
			    throw new AracneException("Threshold Mutual Info. should be larger than or equal to zero.");
				 
		} else {
			 
			if (aracneInput.getThreshold()<0 || aracneInput.getThreshold() > 1)				 
			   throw new AracneException("Threshold P-Value should be between 0.0 and 1.0");
			 
		}
		if (aracneInput.getIsKernelWidthSpecified()) {		 
			if (aracneInput.getKernelWidth() < 0 ||
						 aracneInput.getKernelWidth() > 1) 				 
			   throw new AracneException("Kernel Width should between 0.0 and 1.0");
			 
		}
		if (aracneInput.getIsDPIToleranceSpecified()) {
			 
				if (aracneInput.getDPITolerance() < 0 || aracneInput.getDPITolerance()> 1)				 
						throw new AracneException("DPI Tolerance should between 0.0 and 1.0");
		 
		}
		
		if (aracneInput.getBootstrapNumber() <= 0) {
			throw new AracneException("Bootstrap number is not valid.");
		}
		
		if (aracneInput.getConsensusThreshold() <= 0
				|| aracneInput.getConsensusThreshold() > 1)  
			throw new AracneException("Consensus threshold is not valid.");
		
		if (aracneInput.getMicroarrayNames().length <= 0)
			throw new AracneException("There is no microarray name provided.");
		
		if (aracneInput.getMarkers().length <= 0)
			throw new AracneException("There is no marker name provided.");
		
		if (aracneInput.getMarkerValues().length <= 0)
			throw new AracneException("There is no marker values provided.");
		 
	}

	public String getAlgorithmForFileName() {
		String algor = FIXED;

		if (aracneInput.getAlgorithm().equals(FIXED_BANDWIDTH)) {
			algor = FIXED;
		}

		if (aracneInput.getAlgorithm().equals(ADAPTIVE_PARTITIONING)) {
			algor = ADAPTIVE;
		}

		return algor;
	}

	public Parameter.MODE getMode() {
		Parameter.MODE mode = Parameter.MODE.COMPLETE;

		if (aracneInput.getMode().equals(PREPROCESSING)) {
			mode = Parameter.MODE.PREPROCESSING;
		}

		if (aracneInput.getMode().equals(DISCOVERY)) {
			mode = Parameter.MODE.DISCOVERY;
		}

		return mode;
	}

	public Parameter.ALGORITHM getAlgorithm() {
		Parameter.ALGORITHM algor = Parameter.ALGORITHM.FIXED_BANDWIDTH;

		if (aracneInput.getAlgorithm().equals(FIXED_BANDWIDTH)) {
			algor = Parameter.ALGORITHM.FIXED_BANDWIDTH;
		}

		if (aracneInput.getAlgorithm().equals(ADAPTIVE_PARTITIONING)) {
			algor = Parameter.ALGORITHM.ADAPTIVE_PARTITIONING;
		}

		return algor;
	}

	private static AracneOutput convert(WeightedGraph graph, String paramterDescription) {
		if(graph==null) {
			log.info("null graph");
			return null;
		}
		
		AracneGraphEdge[] aracneGraphEdges = new AracneGraphEdge[graph.getEdges().size()];
		int nEdge = 0;
		for (GraphEdge graphEdge : graph.getEdges()) {				 
			AracneGraphEdge aracneGraphEdge = new AracneGraphEdge(graphEdge.getNode1(),graphEdge.getNode2(),  graphEdge.getWeight());
			aracneGraphEdges[nEdge] = aracneGraphEdge;
			nEdge++;
		} 		
		log.debug("edge count " + nEdge);
		return new AracneOutput(aracneGraphEdges, paramterDescription);
	}

}