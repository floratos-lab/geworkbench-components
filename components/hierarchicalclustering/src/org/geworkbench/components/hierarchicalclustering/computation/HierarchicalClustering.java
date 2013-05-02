package org.geworkbench.components.hierarchicalclustering.computation;

import org.geworkbench.components.hierarchicalclustering.data.*;

/**
 * Straightforward implementations of Hierarchical Clustering.
 * 
 * @author John Watkinson
 * @version $Id$
 */
public class HierarchicalClustering {

	private Linkage linkage;
	private Distance[] distances = { EuclideanDistance.instance,
			CorrelationDistance.instance, SpearmanRankDistance.instance };
	private Distance distance;
	private double[][] matrix;

	/**
	 * 
	 * @param linkage
	 */
	public HierarchicalClustering(HierClusterInput input) {
		linkage = Linkage.valueOf(input.getLinkageType());
		distance = distances[DistanceType.valueOf(input.getDistanceType())
				.ordinal()];
		matrix = input.getMatrix();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.geworkbench.components.clustering.HierarchicalClusterAlgorithm#compute
	 *  
	 */
	public HNode compute() {

		int n = matrix.length;
		String[] items = new String[n];
		for (int i = 0; i < n; i++) {
			items[i] = "" + i;
		}

		ClusteringAlgorithm clustering = new ClusteringAlgorithm(items, matrix,
				distance, linkage, this);

		return clustering.doClustering();

	}

	public void setMatrix(double[][] matrix) {
		this.matrix = matrix;
	}

	public volatile boolean cancelled = false;

}
