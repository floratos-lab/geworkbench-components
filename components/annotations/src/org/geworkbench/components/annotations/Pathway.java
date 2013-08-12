package org.geworkbench.components.annotations;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Defines a contract to obtain Pathway information
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public interface Pathway extends Comparable<Pathway>{
    /**
     * Gets the name of the Pathway contained in this instance
     *
     * @return Pathway name
     */
    String getPathwayName();

    /**
     * Gets the <code>PathwayDiagram</code> contained in the <code>Pathway</code>
     * instance
     *
     * @return Pathway diagram
     */
    String getPathwayDiagram();

}