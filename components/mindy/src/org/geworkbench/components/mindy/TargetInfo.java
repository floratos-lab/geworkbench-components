package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

public class TargetInfo {
    public TargetInfo(DSGeneMarker target, double correlation) {
		this.target = target;
		this.correlation = correlation;
	}

	private DSGeneMarker target;
	private double correlation;  // pearson correlation (TF, target)

	/**
     * Pearson correlation between the transcription factor and the target gene.
     * Used primarily for the heat map.
     * @return result of Pearson correlation
     */
    public double getCorrelation(){
    	return this.correlation;
    }

    /**
     * Pearson correlation between the transcription factor and the target gene.
     * Used primarily for the heat map.
     *
     * @param Pearson correlation
     */
    public void setCorrelation(double correlation){
    	this.correlation = correlation;
    }

}
