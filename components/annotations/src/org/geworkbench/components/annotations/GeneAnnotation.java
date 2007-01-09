package org.geworkbench.components.annotations;

import org.geworkbench.util.annotation.Pathway;

import java.net.URL;
import java.util.List;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Defines a contract to obtain Gene Information
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public interface GeneAnnotation {
    /**
     * Gets Gene name
     *
     * @return Gene name
     */
    String getGeneName();

    /**
     * Gets Gene Description
     *
     * @return Gene Description
     */
    String getGeneDescription();

    List<GeneAnnotationImpl.CGAPUrl> getCGAPGeneURLs();

    /**
     * Gets Gene Locus Link ID
     *
     * @return Gene Locus Link ID
     */
    String getLocusLinkId();

    /**
     * Gets Gene Unigene Cluster ID
     *
     * @return Gene Unigene Cluster ID
     */
    Long getUnigeneClusterId();

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

//    boolean equals(Object object);
}