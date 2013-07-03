package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Defines a contract to obtain Gene Information
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public interface GeneAnnotation {
    /**
     * Gets Gene name
     *
     * @return Gene name
     */
    String getGeneName();
    Gene getGene();

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
    org.geworkbench.util.annotation.Pathway[] getPathways();

    String getGeneSymbol();
}