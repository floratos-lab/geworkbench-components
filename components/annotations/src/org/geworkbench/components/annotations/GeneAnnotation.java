package org.geworkbench.components.annotations;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Defines a contract to obtain Gene Information
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public interface GeneAnnotation extends GeneBase, Comparable<GeneAnnotation> {

    String getEntrezId();
    Long getClusterId();

    /**
     * Gets Organism abbreviation
     *
     * @return Organism abbreviation
     */
    String getOrganismAbbreviation();

    /**
     * Gets associated pathways
     *
     * @return associated pathways
     */
    org.geworkbench.components.annotations.Pathway[] getPathways();

}