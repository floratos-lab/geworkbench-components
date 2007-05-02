package org.geworkbench.components.cytoscape_v2_4;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.CytoscapeInit;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 1.0
 */

public abstract class Cytoscape extends cytoscape.Cytoscape {
    public static cytoscape.view.CytoscapeDesktop getDesktop() {
        if (defaultDesktop == null) {
            // System.out.println( " Defaultdesktop created: "+defaultDesktop );
            defaultDesktop = new cytoscape.view.CytoscapeDesktop(CytoscapeDesktop
                    .parseViewType(CytoscapeInit.getProperties().getProperty(
                            "viewType")));
        }
        return defaultDesktop;
    }
}
