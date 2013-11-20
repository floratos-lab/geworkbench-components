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
		if (input.getDimensionType().equals(DimensionType.ARRAY.name()))
			matrix = getTranspose(input.getMatrix());
		else //default to MARKER
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
	
	
	private static double[][] getTranspose(final double[][] input) {
		double d[][] = new double[input[0].length][input.length];
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				d[i][j] = input[j][i];
			}
		}
		return d;
	}

	public volatile boolean cancelled = false;

}
