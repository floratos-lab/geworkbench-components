/*
* The geworkbench project
* 
* Copyright (c) 2006 Columbia University
* 
*/
package org.geworkbench.components.hierarchicalclustering;

import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.util.Distance;
import org.geworkbench.util.MatrixModel;
import org.geworkbench.util.ProgressBar;

/**
 * 
 * @author unattributable
 *
 */
public interface HierarchicalClusterAlgorithm {
	
	/**
	 * 
	 * @param analysis
	 * @param mm
	 * @param clusterFactory
	 * @param dist
	 * @return
	 */
    public HierCluster compute(AbstractAnalysis analysis, MatrixModel mm, HierClusterFactory clusterFactory, Distance dist);
    
    /**
     * 
     * @param analysis
     * @param mm
     * @param factory
     * @param distance
     * @return
     */
	public HierCluster compute(AbstractAnalysis analysis, double[][] mm, HierClusterFactory factory, Distance distance);
	
	/**
	 * 
	 * @param analysis
	 * @param progressBar
	 * @return
	 */
	public HierCluster stopAlgorithm(AbstractAnalysis analysis, ProgressBar progressBar);
}
