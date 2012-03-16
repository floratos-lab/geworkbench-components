package org.geworkbench.components.hierarchicalclustering;

import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.util.Distance;

/**
 * Straightforward implementations of Hierarchical Clustering.
 *
 * @author John Watkinson
 * @version $Id$
 */
public class HierarchicalClustering {

    ClusteringAlgorithm.Linkage linkage;
    
    /**
     * 
     * @param linkage
     */
    public HierarchicalClustering(ClusteringAlgorithm.Linkage linkage) {
        this.linkage = linkage;
    }
    
    /*
     * (non-Javadoc)
     * @see org.geworkbench.components.clustering.HierarchicalClusterAlgorithm#compute(org.geworkbench.analysis.AbstractAnalysis, double[][], org.geworkbench.components.clustering.HierClusterFactory, org.geworkbench.util.Distance)
     */
    public HierCluster compute(final double[][] mm, final HierClusterFactory factory, final Distance distance) {
        
    	int n = mm.length;
        String[] items = new String[n];
        for (int i = 0; i < n; i++) {
            items[i] = "" + i;
        }
         
        ClusteringAlgorithm clustering = new ClusteringAlgorithm(items, mm, distance, linkage, this);
        HNode root = clustering.doClustering();
        if ( root == null )
        	return null;
        HierCluster result = convertCluster(factory, root);
        
        return result;
    }

   
    /**
     * 
     * @param analysis
     * @param factory
     * @param node
     * @param pb
     * @return
     */
    private static HierCluster convertCluster(HierClusterFactory factory, HNode node) {
        if (node.isLeafNode()) {
            return factory.newLeaf(Integer.parseInt(node.getLeafItem()));
        } else {
        	
        	HierCluster left = convertCluster(factory, node.getLeft());
            HierCluster right = convertCluster(factory, node.getRight());
            HierCluster cluster = factory.newCluster();
            cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
            cluster.setHeight(node.getHeight());
            cluster.addNode(left, 0);
            cluster.addNode(right, 0);
            return cluster;
        }
    }

    public volatile boolean cancelled = false;
    
}
