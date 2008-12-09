package org.geworkbench.components.hierarchicalclustering;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis; 
import org.geworkbench.util.Distance;
import org.geworkbench.util.EuclideanDistance;
import org.geworkbench.util.ProgressBar;

/**
 * @author John Watkinson
 * @version $Id: HClustering.java,v 1.1 2008-12-09 16:53:45 chiangy Exp $
 */
public class HClustering {

    static Log log = LogFactory.getLog(HClustering.class);  
    private AbstractAnalysis analysis = null;
    
    
    public enum Linkage {
        SINGLE,
        AVERAGE,
        COMPLETE
    }

    private static class SortStruct implements Comparable {
        public double distance;
        private HNode a, b;

        /**
         * 
         * @param distance
         * @param a
         * @param b
         */
        public SortStruct(double distance, HNode a, HNode b) {
            this.distance = distance;
            if (a.getId() < b.getId()) {
                this.a = a;
                this.b = b;
            } else {
                this.a = b;
                this.b = a;
            }
        }
        
        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final SortStruct that = (SortStruct) o;

            if (!a.equals(that.a)) return false;
            if (!b.equals(that.b)) return false;

            return true;
        }
        
        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            int result;
            result = a.hashCode();
            result = 29 * result + b.hashCode();
            return result;
        }
       
        /*
         * (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            SortStruct s = (SortStruct) o;
            if (this.equals(s)) {
                return 0;
            } else {
                int compare = Double.compare(distance, s.distance);
                if (compare == 0) {
                    compare = Double.compare(a.getId(), s.a.getId());
                    if (compare == 0) {
                        compare = Double.compare(b.getId(), s.b.getId());
                    }
                }
                return compare;
            }
        }
      
        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "(" + a.getId() + ", " + b.getId() + "): " + distance;
        }
    }

   
    private Distance distance;
    private Linkage linkage;
    private ArrayList<HNode> nodes;
    private double[][] values;
    private HashMap<Integer, Double> distanceMap;    
    private ArrayList<float[]> distances;

    // todo - this can be made more efficient with some manual unrolling of treeset's behavior
    private TreeSet<SortStruct> distanceSet;

    private ProgressBar pb;

    /**
     * 
     * @param analysis
     * @param items
     * @param values
     * @param distance
     * @param linkage
     * @param pb
     */
    public HClustering(AbstractAnalysis analysis, String[] items, double[][] values, Distance distance, Linkage linkage, ProgressBar pb) {
        this.analysis = analysis;
    	this.distance = distance;
        this.linkage = linkage;
        this.values = values;
        this.pb = pb;      
        int n = items.length;
        nodes = new ArrayList<HNode>(n);
        for (int i = 0; i < n && !analysis.stopAlgorithm; i++) {
            nodes.add(createLeafNode(i, items[i]));
        }
        id = n;
        distanceMap = new HashMap<Integer, Double>();
        distanceSet = new TreeSet<SortStruct>();
        if (linkage != Linkage.AVERAGE) {  
            distances = new ArrayList<float[]>();
        }

    }
    
    //for testing purpose
    public HClustering(String[] items, double[][] values, Distance distance, Linkage linkage, ProgressBar pb) {
        
    	this.distance = distance;
        this.linkage = linkage;
        this.values = values;
        this.pb = pb;
        int n = items.length;
        nodes = new ArrayList<HNode>(n);
        for (int i = 0; i < n && !analysis.stopAlgorithm; i++) {
            nodes.add(createLeafNode(i, items[i]));
        }
        id = n;
        distanceMap = new HashMap<Integer, Double>();
        distanceSet = new TreeSet<SortStruct>();
        if (linkage != Linkage.AVERAGE) {           
        	distances = new ArrayList<float[]>();
        	
        }

    }

    private int id = 0;

    private int getNextID() {
        return id++;
    }

    private HNode createLeafNode(int id, String item) {
        return new HNode(id, Collections.singleton(item), null, null, 0);
    }
     
    private HNode createBranchNode(HNode left, HNode right, double d) {
        HashSet<String> merged = new HashSet<String>(left.getItems());
        merged.addAll(right.getItems());
        return new HNode(getNextID(), merged, left, right, d);
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

//    private double getDistance(HNode a, HNode b) {
//        int key = getKey(a.getId(), b.getId());
//        return distanceMap.get(key);
//    }
//
     
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
        	
        	if (analysis != null && analysis.stopAlgorithm)             
            	break;
        	
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
        if (pb!=null)
        pb.setMessage("Computing intial distance map...");
        // Compute n^2/2 initial distances
        for (int i = 0; i < n && !analysis.stopAlgorithm; i++) {
        	if (pb!=null)
            pb.updateTo(i);
            if (linkage != Linkage.AVERAGE)  
            {
            	float[] distanceList = new float[n]; 
                distances.add(i,distanceList);
            }
            for (int j = i + 1; j < n && !analysis.stopAlgorithm; j++) {
                computeDistance(i, j);
            }
        }
        // Now, iteratively construct cluster
        log.debug("Constructing cluster...");
        if (pb!=null)
        pb.setMessage("Clustering...");
        int step = 0;
        while (nodes.size() > 1 && !analysis.stopAlgorithm) {
        	if (pb!=null)
            pb.updateTo(step);
            SortStruct closest = distanceSet.first();
            HNode left = closest.a;
            HNode right = closest.b;
            double d = getAndRemoveDistance(left, right);
            HNode newNode = createBranchNode(left, right, d);
            updateDistanceMap(newNode);
            
            if (analysis != null && analysis.stopAlgorithm)
            	return stopAlgorithm(analysis, pb);             
            
            nodes.remove(left);
            nodes.remove(right);
            nodes.add(newNode);
            step++;
        }
        
        if (analysis != null && analysis.stopAlgorithm)
        	return stopAlgorithm(analysis, pb);
        
        log.debug("Done in " + step + " steps.");
        return nodes.get(0);
    }
    
    
    /**
     * 
     * @param root
     */
     
    public static void printCluster(HNode root) {
        root.printCluster(System.out);
    }

    public static void main(String[] args) {
        // General example
//        float[][] data =
//                {
//                        {0, 0},
//                        {1, 1},
//                        {-1, 3},
//                        {-2, 3},
//                        {0, -3},
//                        {-2, 1}
//                };
//        String[] items = {"A", "B", "C", "D", "E", "F"};
        // Different results between single and complete
        double[][] data =
                {
                        {0, 1},
                        {0, 4},
                        {0, 5},
                        {0, 9},
                        {0, 10},
                };
        String[] items = {"A", "B", "C", "D", "E"};
        {
            System.out.println("--- AVERAGE ---");
            HClustering clustering = new HClustering(items, data, EuclideanDistance.instance, Linkage.AVERAGE, null);
            HNode root = clustering.doClustering();
            printCluster(root);
            System.out.println();
        }
        {
            System.out.println("--- SINGLE ---");
            HClustering clustering = new HClustering(items, data, EuclideanDistance.instance, Linkage.SINGLE, null);
            HNode root = clustering.doClustering();
            printCluster(root);
            System.out.println();
        }
        {
            System.out.println("--- COMPLETE ---");
            HClustering clustering = new HClustering(items, data, EuclideanDistance.instance, Linkage.COMPLETE, null);
            HNode root = clustering.doClustering();
            printCluster(root);
            System.out.println();
        }
    }

//    public static HierarchicalClusterNode convertCluster(HNode node) {
//        if (node.isLeafNode()) {
//            return new HierarchicalClusterNode(node.getLeafItem(), node.getId());
//        } else {
//            HierarchicalClusterNode left = convertCluster(node.getLeft());
//            HierarchicalClusterNode right = convertCluster(node.getRight());
//            return new HierarchicalClusterNode(left, right, (float) node.getHeight());
//        }
//    }
    
    /**
     * Terminates the algorithm and stops the progress bar.
     * @param progressBar
     * @return HierCluster
     */
	public HNode stopAlgorithm(AbstractAnalysis analysis, ProgressBar progressBar) {
		analysis.stopAlgorithm = false;
        progressBar.stop();
        return null;	
	}
	
	 

}
