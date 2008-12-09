package org.geworkbench.components.hierarchicalclustering;

import org.geworkbench.bison.datastructure.biocollections.microarrays.CSExprMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.components.parsers.ExpressionFileFormat;
import org.geworkbench.util.Distance;
import org.geworkbench.util.EuclideanDistance;
import org.geworkbench.util.FastMatrixModel;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: fguo
 * Date: Aug 23, 2005
 * Time: 2:45:46 PM
 * Compute the U-statistics for a hierarchical cluster. See A. D. Gordon, Identifying genuine clusters in a
 * classification, Computational Statistics & Data Analysis 18 (1994) 561-581.
 * <p/>
 * The U-statistic is computed for each cluster in the cluster hierarchy, the total is reported.
 */
public class Validation {
    private HierCluster cluster;
    private int UStat;
    private boolean [] mask;
    private int size;
    private int nInCluster;
    private double [][] dmat; // dissimilarity matrix
    private double [][] pmat; // pattern matrix
    private int count;
    public Validation(DSMicroarraySetView data, HierCluster cluster, Distance dist) {
        this.cluster = cluster;
        if (cluster instanceof MarkerHierCluster) {
            size = data.markers().size();
            pmat = FastMatrixModel.getMatrix(data, FastMatrixModel.Metric.GENE);
        }
        else if (cluster instanceof MicroarrayHierCluster) {
            size = data.items().size();
            pmat = FastMatrixModel.getMatrix(data, FastMatrixModel.Metric.MICROARRAY);
        } else {
            size = 0;
        }
        mask = new boolean[size];
        // compute the dissimilarity matrix lower-left triangle
        dmat = new double[size][];
        for (int i=1; i<size; ++i) {
            dmat[i] = new double[i];
            for (int j=0; j<i; ++j) {
                dmat[i][j] = dist.compute(pmat[i], pmat[j]);
            }
        }
    }
    public int computeTotalUStatistic() {
        UStat = 0;
        count = 0;
        // traverse each node of the cluster hierarchy
        traverse(cluster);
        return UStat;
    }
    // traverse each node and compute U-Statistic for that node and add it to UStat
    private void traverse(HierCluster c) {
        if (c == null) return;
        traverse(c.getNode(0));
        if (!c.isLeaf())
            UStat += computeUStatistic(c);
        traverse(c.getNode(1));
    }
    private int computeUStatistic(HierCluster c) {
        int U = 0;
        nInCluster = 0;
        for (int i=0; i<size; ++i)
            mask[i] = false;
        // make those masks true for nodes within the cluster c
        traverseMask(c);
        if (nInCluster < 2 || size - nInCluster < 2)
            return 0;
        System.out.print("Cluster: ");
        for (int i=0; i<size; ++i) {
            if (!mask[i]) continue;
            System.out.print(" " +i);
        }
        System.out.println("");
        // compute for each within-cluster pair dmat[i][j] agains each inter-cluster pair dmat[k][l]
        for (int i=0; i<size; ++i) {
            if (!mask[i]) continue;
            for (int j=i+1; j<size; ++j) {
                if (!mask[j]) continue;
                for (int k=0; k<size; ++k) {
                    if (!mask[k]) continue;
                    for (int l=0; l<size; ++l) {
                        if (mask[l]) continue;
                        double d = dmat[j][i] - distance(k,l);
                        if (d == 0) U += 1;
                        else if (d > 0) {
                            U += 2;
                            System.out.println("d(" + i + ", " + j + ") = " + dmat[j][i] + " > d(" + k + ", " + l + ") = " + distance(k,l));
                        }
                    }
                }
            }
        }
        System.out.println(++count + " " + U);
        return U;
    }
    private double distance(int i, int j) {
        if (i<j) return dmat[j][i];
        else if (i>j) return dmat[i][j];
        else return 0;
    }
    private void traverseMask(HierCluster c) {
        if (c == null) return;
        if (c.isLeaf()) {
            ++nInCluster;
            if (c instanceof MarkerHierCluster) {
                mask[((MarkerHierCluster)c).getMarkerInfo().getSerial()] = true;
            }
            else if (c instanceof MicroarrayHierCluster) {
                mask[((MicroarrayHierCluster)c).getMicroarray().getSerial()] = true;
            }
            return;
        }
        traverseMask(c.getNode(0));
        traverseMask(c.getNode(1));
    }

    public static void main(String[] args) {
        ExpressionFileFormat format = new ExpressionFileFormat();
        CSExprMicroarraySet microarraySet = (CSExprMicroarraySet)format.getMArraySet(new File("c:/web20.exp"));
        DSMicroarraySetView data = new CSMicroarraySetView();
        data.setDataSet(microarraySet);
        // Fast
        FastHierClustAnalysis fast = new FastHierClustAnalysis();
        HierClustPanel fastPanel = ((HierClustPanel)fast.getParameterPanel());
        fastPanel.setDimension(2);
        fastPanel.setMethod(2);      // complete link
        fastPanel.setDistanceMetric(0); // Euclidean
        AlgorithmExecutionResults fastAER = fast.execute(data);
        HierCluster cluster = ((HierCluster[])fastAER.getResults())[0];
        Validation val = new Validation(data, cluster, EuclideanDistance.instance);
        int u = val.computeTotalUStatistic();
        System.out.println("U statistic for hierarchical clustering = " + u);
        return;
    }
}
