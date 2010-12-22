package org.geworkbench.components.hierarchicalclustering;

import java.util.Observable;
import java.util.Observer;

import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.util.MatrixModel;
import org.geworkbench.util.Distance;
import org.geworkbench.util.ProgressBar;

/**
 * Straightforward implementations of Hierarchical Clustering.
 *
 * @author John Watkinson
 * @version $Id$
 */
public class HierarchicalClustering implements Observer {

    ClusteringAlgorithm.Linkage linkage;     
    private AbstractAnalysis analysis = null;
    
    /**
     * 
     * @param linkage
     */
    public HierarchicalClustering(ClusteringAlgorithm.Linkage linkage) {
        this.linkage = linkage;
    }
    
    /*
     * (non-Javadoc)
     * @see org.geworkbench.components.clustering.HierarchicalClusterAlgorithm#compute(org.geworkbench.analysis.AbstractAnalysis, org.geworkbench.util.MatrixModel, org.geworkbench.components.clustering.HierClusterFactory, org.geworkbench.util.Distance)
     */
    public HierCluster compute(AbstractAnalysis analysis, MatrixModel mm, HierClusterFactory clusterFactory, Distance dist) {
        // Not used
        return null;	
    }
    
    /*
     * (non-Javadoc)
     * @see org.geworkbench.components.clustering.HierarchicalClusterAlgorithm#compute(org.geworkbench.analysis.AbstractAnalysis, double[][], org.geworkbench.components.clustering.HierClusterFactory, org.geworkbench.util.Distance)
     */
    public HierCluster compute(AbstractAnalysis analysis, double[][] mm, HierClusterFactory factory, Distance distance) {
        
    	int n = mm.length;
        String[] items = new String[n];
        for (int i = 0; i < n; i++) {
            items[i] = "" + i;
        }
         
        if ( analysis != null)
        	 analysis.stopAlgorithm = false;
        this.analysis = analysis;
         
        org.geworkbench.util.ProgressBar pb = org.geworkbench.util.ProgressBar.create(org.geworkbench.util.ProgressBar.BOUNDED_TYPE);
        pb.addObserver(this);
        pb.setTitle("Hierarchical Clustering");
        pb.setMessage("Computing distance matrix...");
        pb.setBounds(new ProgressBar.IncrementModel(0, n, 0, n, 1));
        pb.start();
        
        ClusteringAlgorithm clustering = new ClusteringAlgorithm(this.analysis, items, mm, distance, linkage, pb);
        HNode root = clustering.doClustering();
        if ( root == null )
        	return null;
        HierCluster result = convertCluster(this.analysis, factory, root, pb);
        
        if (analysis != null && analysis.stopAlgorithm)
        	return stopAlgorithm(analysis, pb);         
        
        pb.dispose();
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
    private static HierCluster convertCluster(AbstractAnalysis analysis, HierClusterFactory factory, HNode node, ProgressBar pb) {
        if (node.isLeafNode()) {
            return factory.newLeaf(Integer.parseInt(node.getLeafItem()));
        } else {
        	
        	if (analysis != null && analysis.stopAlgorithm)
              	   return null;        
        	HierCluster left = convertCluster(analysis, factory, node.getLeft(), pb);
            HierCluster right = convertCluster(analysis, factory, node.getRight(), pb);
            HierCluster cluster = factory.newCluster();
            cluster.setDepth(Math.max(left.getDepth(), right.getDepth()) + 1);
            cluster.setHeight(node.getHeight());
            cluster.addNode(left, 0);
            cluster.addNode(right, 0);
            return cluster;
        }
    }
    
    /**
     * Terminates the algorithm and stops the progress bar.
     * @param progressBar
     * @return HierCluster
     */
	public HierCluster stopAlgorithm(AbstractAnalysis analysis, ProgressBar progressBar) {
		analysis.stopAlgorithm = false;
        progressBar.stop();
        return null;	
	}
	
	/**
	 * @param o
	 * @param arg
	 */
	public void update(Observable o, Object arg) {
		this.analysis.stopAlgorithm = true;		 
    }
    
}
