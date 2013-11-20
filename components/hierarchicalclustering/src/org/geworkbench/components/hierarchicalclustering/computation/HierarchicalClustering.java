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
<<<<<<< HEAD
		matrix = input.getMatrix();
=======
		if (input.getDimensionType().equals(DimensionType.ARRAY.name()))
			matrix = getTranspose(input.getMatrix());
		else //default to MARKER
		    matrix = input.getMatrix();
>>>>>>> 0ba04f36ba02d9238573496aad71bb60bb642103
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
<<<<<<< HEAD
=======
	
	
	private static double[][] getTranspose(final double[][] input) {
		double d[][] = new double[input[0].length][input.length];
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				d[i][j] = input[j][i];
			}
		}
		return d;
	}
>>>>>>> 0ba04f36ba02d9238573496aad71bb60bb642103

	public volatile boolean cancelled = false;

}
