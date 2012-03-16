package org.geworkbench.components.hierarchicalclustering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.util.Distance;

/**
 * @author John Watkinson
 * @version $Id$
 */
public class ClusteringAlgorithm {

    static Log log = LogFactory.getLog(ClusteringAlgorithm.class); 
    
    public enum Linkage {
        SINGLE,
        AVERAGE,
        COMPLETE
    }
   
    private Distance distance;
    private Linkage linkage;
    private ArrayList<HNode> nodes;
    private final double[][] values;
    private HashMap<Integer, Double> distanceMap;    
    private ArrayList<float[]> distances;

    // TODO - this can be made more efficient with some manual unrolling of treeset's behavior
    private TreeSet<SortStruct> distanceSet;
	
    final private HierarchicalClustering clusteringTask;

    /**
     * 
     * @param analysis
     * @param items
     * @param values
     * @param distance
     * @param linkage
     * @param pb
     */
    public ClusteringAlgorithm(final String[] items, final double[][] values, final Distance distance, final Linkage linkage,
    		final HierarchicalClustering clusteringTask) {
    	this.distance = distance;
        this.linkage = linkage;
        this.values = values;

        int n = items.length;
        nodes = new ArrayList<HNode>(n);
        for (int i = 0; i < n; i++) {
            nodes.add(createLeafNode(i, items[i]));
        }
        id = n;
        distanceMap = new HashMap<Integer, Double>();
        distanceSet = new TreeSet<SortStruct>();
        if (linkage != Linkage.AVERAGE) {  
            distances = new ArrayList<float[]>();
        }

        this.clusteringTask = clusteringTask;
    }
    
    private int id = 0;

    private HNode createLeafNode(int id, String item) {
        return new HNode(id, Collections.singleton(item), null, null, 0);
    }
     
    private HNode createBranchNode(HNode left, HNode right, double d) {
        HashSet<String> merged = new HashSet<String>(left.getItems());
        merged.addAll(right.getItems());
        return new HNode(++id, merged, left, right, d);
    }

    private static int getKey(int keyA, int keyB) {
        int key;
        if (keyA < keyB) {
            key = (keyA << 16) + keyB;
        } else {
            key = (keyB << 16) + keyA;
        }
        return key;
    }

    private void computeDistance(int i, int j) {
        double d = distance.compute(values[i], values[j]);
        int key = (i << 16) + j;
        distanceMap.put(key, d);
        distanceSet.add(new SortStruct(d, nodes.get(i), nodes.get(j)));
        if (linkage != Linkage.AVERAGE) {                	 
        	distances.get(i)[j] = (float) d ;
        }
    }

    private double getAndRemoveDistance(HNode a, HNode b) {
        int key = getKey(a.getId(), b.getId());
        double d = distanceMap.remove(key);
        distanceSet.remove(new SortStruct(d, a, b));
        return d;
    }

    /**
     * 
     * @param a
     * @param b
     * @param d
     */
    private void putDistance(HNode a, HNode b, double d) {
        int key = getKey(a.getId(), b.getId());
        distanceMap.put(key, d);
        distanceSet.add(new SortStruct(d, a, b));
    }

    /**
     * 
     * @param newNode
     */
    private void updateDistanceMap(HNode newNode) {
        // We will remove a and b from the distance map, then recompute distances for the combined node
        HNode left = newNode.getLeft();
        HNode right = newNode.getRight();
        int leftN = left.size();
        int rightN = right.size();
        int newN = leftN + rightN;
        for (HNode node : nodes ) {
        	
            if (node.equals(left) || node.equals(right)) {
                continue;
            }
            double leftD = getAndRemoveDistance(left, node);
            double rightD = getAndRemoveDistance(right, node);
            double newDistance;
            if (linkage == Linkage.AVERAGE) {
                newDistance = (leftN * leftD + rightN * rightD) / (newN);
            } else if (linkage == Linkage.SINGLE) {
                newDistance = Math.min(leftD, rightD);
            } else { // COMPLETE
                newDistance = Math.max(leftD, rightD);
            }
            putDistance(node, newNode, newDistance);
        }
    }

    /**
     * 
     * @return
     */
    public HNode doClustering() {
        int n = nodes.size();
        
        log.debug("Computing intial distance map...");

        // Compute n^2/2 initial distances
        for (int i = 0; i < n; i++) {

            if (clusteringTask.cancelled) {
            	return null; // cancelled
            }
            
            if (linkage != Linkage.AVERAGE)  
            {
            	float[] distanceList = new float[n]; 
                distances.add(i,distanceList);
            }
            for (int j = i + 1; j < n; j++) {
                computeDistance(i, j);
            }
        }
        // Now, iteratively construct cluster
        log.debug("Constructing cluster...");

        int step = 0;
        while (nodes.size() > 1) {

            SortStruct closest = distanceSet.first();
            HNode left = closest.getLeftNode();
            HNode right = closest.getRightNode();
            double d = getAndRemoveDistance(left, right);
            HNode newNode = createBranchNode(left, right, d);
            updateDistanceMap(newNode);

            if (clusteringTask.cancelled) {
            	return null; // cancelled
            }
            
            nodes.remove(left);
            nodes.remove(right);
            nodes.add(newNode);
            step++;
        }
        
        log.debug("Done in " + step + " steps.");
        return nodes.get(0);
    }
    
}
