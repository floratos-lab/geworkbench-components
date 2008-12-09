package org.geworkbench.components.hierarchicalclustering;

import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.util.IndexSortComparator;
import org.geworkbench.util.ProgressBar;

import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Convert between different hierarchical cluster representations,
 * including <code>HierCluster</code> representation, pointer representation (See Sibson, R.,
 * SLINK: an optimally efficient algorithm for the single link cluster method,
 * The Computer Journal, Vol 16, pp.30--45, 1973.), two-integer-array representation.</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author Frank Wei Guo
 * @version 3.0
 */
public class ClusterConverter implements Observer {
    HierClusterFactory clusterFactory;
    org.geworkbench.util.ProgressBar pb;

    /**
     * ClusterConverter
     *
     * @param clusterFactory HierClusterFactory
     */
    public ClusterConverter(HierClusterFactory clusterFactory) {
        this.clusterFactory = clusterFactory;
    }


    public int[][] ptr2Array(int pi[], double[] lambda) {
        assert(pi.length == lambda.length);
        int N = pi.length;


        pb = org.geworkbench.util.ProgressBar.create(org.geworkbench.util.ProgressBar.BOUNDED_TYPE);
        pb.addObserver(this);
        pb.setTitle("Convert Cluster Representations");
        pb.setMessage("Pointer Rep to Array Rep ... " + (N - 1) + " variables");
        pb.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0, N - 1, 0, N - 1, 1));
        pb.start();

        int[][] array = new int[N - 1][2];
        int[] c = new int[N];
        Integer[] s = new Integer[N]; // to use the customized sort has to be class
        int i, lt, rt;
        for (i = 0; i < N; i++) c[i] = s[i] = i;
        Arrays.sort(s, new IndexSortComparator(lambda));
        for (i = 0; i < N - 1; i++) {
            lt = s[i];
            rt = pi[lt];
            array[i][0] = c[lt];
            array[i][1] = c[rt];
            c[rt] = -i - 1;
            pb.updateTo(i);
        }
        pb.dispose();
        /*
        for (i = 0; i < array.length; ++i)
        {
            System.out.print("(" + array[i][0] + ", " + array[i][1] + ") ");
        }
        */
        return array;
    }

    public HierCluster ptr2HierCluster(int[] pi, double[] lambda) {
        return array2HierCluster(ptr2Array(pi, lambda));
    }


    public HierCluster array2HierCluster(int[][] array) {
        int N = array.length;
        if (N == 0) {
            return clusterFactory.newLeaf(0);
        }
        pb = ProgressBar.create(ProgressBar.BOUNDED_TYPE);
        pb.addObserver(this);
        pb.setTitle("Convert Cluster Representations");
        pb.setMessage("Array Rep to HierCluster Rep ... " + N + " variables");
        pb.setBounds(new org.geworkbench.util.ProgressBar.IncrementModel(0, N, 0, N, 1));
        pb.start();

        HierCluster[] hc = new HierCluster[N];


        try {
            for (int i = 0; i < N; ++i) {
                hc[i] = clusterFactory.newCluster();
                if (array[i][0] >= 0) {
                    hc[i].addNode(clusterFactory.newLeaf(array[i][0]), 0);
                } else {
                    assert(hc[-array[i][0] - 1] != null);
                    hc[i].addNode(hc[-array[i][0] - 1], 0);
                }
                if (array[i][1] >= 0) {
                    hc[i].addNode(clusterFactory.newLeaf(array[i][1]), 1);
                } else {
                    assert(hc[-array[i][1] - 1] != null);
                    hc[i].addNode(hc[-array[i][1] - 1], 1);
                }
                hc[i].setDepth(1 + Math.max(hc[i].getNode(0).getDepth(), hc[i].getNode(1).getDepth()));
                pb.updateTo(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        pb.dispose();
        return hc[N - 1];
    }

    public void update(Observable o, Object arg) {
    }
}
