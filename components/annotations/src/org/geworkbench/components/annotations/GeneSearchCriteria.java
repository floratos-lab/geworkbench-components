package org.geworkbench.components.annotations;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Defines a contract for a <code>SearchCriteria</code> that can be used
 * to make caBIO queries
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public interface GeneSearchCriteria {
    /**
     * Sets a <code>String</code> to be a Search criterion. This typically would
     * be an Accession identifier
     *
     * @param name accession
     */
    GeneAnnotation[] searchByName(String name);
    GeneAnnotation[] searchByName(String name, String organism);

    /**
     * Sets a BioCarta identifier to be a Search criterion
     *
     * @param bcid BioCarta ID
     */
    GeneAnnotation[] searchByBCID(String bcid);

    GeneAnnotation[] searchByProbeId(String probeId);

    GeneAnnotation[] getGenesInPathway(org.geworkbench.util.annotation.Pathway pathway);
}