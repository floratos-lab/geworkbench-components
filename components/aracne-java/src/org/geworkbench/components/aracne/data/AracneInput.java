package org.geworkbench.components.aracne.data;

public class AracneInput {

	private String dataSetName;
	private String dataSetIdentifier;
	private String[] hubGeneList;
	private String mode;
	private String algorithm;
	private boolean isKernelWidthSpecified;
	private float kernelWidth;
	private boolean isThresholdMI;
	private boolean noCorrection;
	private float threshold;
	private boolean isDPIToleranceSpecified;
	private float dPITolerance;	 
	private String[] targetGeneList;
	private int bootstrapNumber;
	private float consensusThreshold;
	
	private String[] markers;
	private String[] microarrayNames;
	private float[][] markerValues;
 
	public AracneInput() {
	};

	public AracneInput(String dataSetName, String dataSetIdentifier, String[] hubGeneList, String mode,
			String algorithm, boolean isKernelWidthSpecified,
			float kernelWidth, boolean isThresholdMI, boolean noCorrection,
			float threshold, boolean isDPIToleranceSpecified,
			float dPITolerance, String[] targetGeneList,
			int bootstrapNumber, float consensusThreshold, String[] markers,
			String[] microarrayNames,
			float[][] markerValues) {		
		this.dataSetName = dataSetName;
		this.dataSetIdentifier = dataSetIdentifier;
		this.hubGeneList = hubGeneList;
		this.mode = mode;
		this.algorithm = algorithm;
		this.isKernelWidthSpecified = isKernelWidthSpecified;
		this.kernelWidth = kernelWidth;
		this.isThresholdMI = isThresholdMI;
		this.noCorrection = noCorrection;
		this.threshold = threshold;
		this.isDPIToleranceSpecified = isDPIToleranceSpecified;
		this.dPITolerance = dPITolerance;
		this.targetGeneList = targetGeneList;
		this.bootstrapNumber = bootstrapNumber;
		this.consensusThreshold = consensusThreshold;
		this.markers = markers;
		this.microarrayNames = microarrayNames;
		this.markerValues = markerValues;
		 
	}

	public void setDataSetName(String dataSetName) {
		this.dataSetName = dataSetName;
	}

	public String getDataSetName() {
		return this.dataSetName;
	}
	
	public void setDataSetIdentifier(String dataSetIdentifier) {
		this.dataSetIdentifier = dataSetIdentifier;
	}

	public String getDataSetIdentifier() {
		return this.dataSetIdentifier;
	}

	public void setHubGeneList(String[] hubGeneList) {
		this.hubGeneList = hubGeneList;
	}

	public String[] gethubGeneList() {
		return this.hubGeneList;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getMode() {
		return this.mode;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return this.algorithm;
	}

	public void setIsKernelWidthSpecified(boolean isKernelWidthSpecified) {
		this.isKernelWidthSpecified = isKernelWidthSpecified;
	}

	public boolean getIsKernelWidthSpecified() {
		return this.isKernelWidthSpecified;
	}

	public void setKernelWidth(float kernelWidth) {
		this.kernelWidth = kernelWidth;
	}

	public double getKernelWidth() {
		return this.kernelWidth;
	}

	public void setIsThresholdMI(boolean isThresholdMI) {
		this.isThresholdMI = isThresholdMI;
	}

	public boolean getIsThresholdMI() {
		return this.isThresholdMI;
	}

	public void setNoCorrection(boolean noCorrection) {
		this.noCorrection = noCorrection;
	}

	public boolean getNoCorrection() {
		return this.noCorrection;
	}

	public void setThreshold(float threshold) {
		this.threshold = threshold;
	}

	public float getThreshold() {
		return this.threshold;
	}

	public void setIsDPIToleranceSpecified(boolean isDPIToleranceSpecified) {
		this.isDPIToleranceSpecified = isDPIToleranceSpecified;
	}

	public boolean getIsDPIToleranceSpecified() {
		return this.isDPIToleranceSpecified;
	}

	public void setDPITolerance(float dPITolerance) {
		this.dPITolerance = dPITolerance;
	}

	public double getDPITolerance() {
		return this.dPITolerance;
	}

	public void setTargetGeneList(String[] targetGeneList) {
		this.targetGeneList = targetGeneList;
	}

	public String[] getTargetGeneList() {
		return this.targetGeneList;
	}

	public void setBootstrapNumber(int bootstrapNumber) {
		this.bootstrapNumber = bootstrapNumber;
	}

	public int getBootstrapNumber() {
		return this.bootstrapNumber;
	}

	public void setConsensusThreshold(float consensusThreshold) {
		this.consensusThreshold = consensusThreshold;
	}

	public double getConsensusThreshold() {
		return this.consensusThreshold;
	}
	 
	public void setMarkers(String[] markers) {
		this.markers = markers;
	}

	public String[] getMarkers() {
		return this.markers;
	}
	
	public void setMicroarrayNames(String[] microarrayNames) {
		this.microarrayNames = microarrayNames;
	}

	public String[] getMicroarrayNames() {
		return this.microarrayNames;
	}
	
	public void setMarkerValues(float[][] markerValues) {
		this.markerValues = markerValues;
	}

	public float[][] getMarkerValues() {
		return this.markerValues;
	}
	

	 
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Aracne Input\n");
		sb.append("dataSetName:" + dataSetName).append("\n");
		sb.append("dataSetIdentifier:" + dataSetIdentifier).append("\n");
		sb.append("hubGeneList:").append(" \n");
		if (hubGeneList != null)
		{sb.append("{");		
		for(int i=0; i<hubGeneList.length; i++)
		{	if (i== hubGeneList.length - 1)			
			sb.append("\"" +hubGeneList[i]+ "\"").append("}");
			else
				sb.append("\"" +hubGeneList[i]+ "\"").append(",");
	    }
		sb.append("\n");
		}
		
		sb.append("mode:" + mode);
		sb.append("\n");
		sb.append("algorithm:" + algorithm);
		sb.append("\n");
		sb.append("isKernelWidthSpecified:" + isKernelWidthSpecified);
		sb.append("\n");
		sb.append("kernelWidth:" + kernelWidth);
		sb.append("\n");
		sb.append("isThresholdMI:" + isThresholdMI);
		sb.append("noCorrection:" + noCorrection);
		sb.append("\n");
		sb.append("threshold:" + threshold);
		sb.append("\n");
		sb.append("isDPIToleranceSpecified:" + isDPIToleranceSpecified);
		sb.append("\n");
		
		sb.append("targetGeneList:").append(" \n");
		if (targetGeneList != null)
		{sb.append("{");
		for(int i=0; i<targetGeneList.length; i++)
		{	if (i== targetGeneList.length - 1)
			sb.append("\"" +targetGeneList[i]+"\"").append("}");
			else
				sb.append("\"" +targetGeneList[i]+"\"").append(",");
	    }
		sb.append("\n");
		}
		
		sb.append("bootstrapNumber:" + bootstrapNumber);
		sb.append("\n");
		sb.append("consensusThreshold:" + consensusThreshold);
		sb.append("\n");
		 
		sb.append("markers:").append(" \n");
		if (markers != null)
		{sb.append("{");
		for(int i=0; i<markers.length; i++)
		{	if (i== markers.length - 1)
			sb.append("\"" +markers[i]+"\"").append("}");
			else
				sb.append("\"" +markers[i]+"\"").append(",");
	    }
		sb.append("\n");
		}
		
		sb.append("microarrayNames:").append(" \n");
		if (microarrayNames != null)
		{sb.append("{");
		for(int i=0; i<microarrayNames.length; i++)
		{	if (i== microarrayNames.length - 1)
			sb.append("\"" +microarrayNames[i]+"\"").append("}");
			else
				sb.append("\"" +microarrayNames[i]+"\"").append(",");
	    }
		sb.append("\n");
		}
		
		sb.append("markerValues:").append("\n");
		sb.append("{");
		for (int i=0; i<markerValues.length; i++)
		{	sb.append("{");
			for(int j=0; j<markerValues[i].length; j++)
			{	if (j == markerValues[i].length - 1)
				sb.append(markerValues[i][j] + "f").append("}");
				else
					sb.append(markerValues[i][j] + "f").append(",");
		    }
			if (i == markerValues.length-1)
				sb.append("}");
			else
				sb.append(",\n");
		}
		
		return sb.toString();
	}
	

}
