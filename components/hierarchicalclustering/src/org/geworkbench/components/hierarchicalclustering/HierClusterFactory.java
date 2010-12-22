package org.geworkbench.components.hierarchicalclustering;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.bison.model.clusters.*;

/**
 * <p>Title: caWorkbench</p>
 * <p/>
 * <p>Description: Create different <code>HierCluster</code>:
 * <code>MarkerHierCluster</code>, <code>MicroarrayHierCluster</code></p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author Frank Wei Guo
 * @version 3.0
 */
public abstract class HierClusterFactory {
    /**
     * Prevent from creating a concrete object
     */
    protected HierClusterFactory() {
    }

    public abstract HierCluster newCluster();

    public abstract HierCluster newLeaf(int i);

    public static class Gene extends HierClusterFactory {
        DSItemList<DSGeneMarker> markers;

        /**
         * Marker
         *
         * @param markers DSItemList<DSGeneMarker>
         */
        public Gene(DSItemList<DSGeneMarker> markers) {
            this.markers = markers;
        }

        public HierCluster newCluster() {
            return new MarkerHierCluster();
        }

        public HierCluster newLeaf(int i) {
            MarkerHierCluster hc = new MarkerHierCluster();
            hc.setMarkerInfo(markers.get(i));
            hc.setDepth(0);
            return hc;
        }
    }

    public static class Microarray extends HierClusterFactory {
        DSItemList<DSMicroarray> microarrays;

        /**
         * Marker
         *
         * @param microarrays DSItemList<DSMicroarray>
         */
        public Microarray(DSItemList<DSMicroarray> microarrays) {
            this.microarrays = microarrays;
        }

        public HierCluster newCluster() {
            return new MicroarrayHierCluster();
        }

        public HierCluster newLeaf(int i) {
            MicroarrayHierCluster hc = new MicroarrayHierCluster();
            hc.setMicroarray(microarrays.get(i));
            hc.setDepth(0);
            return hc;
        }
    }

}
