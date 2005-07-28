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
    void setSearchName(String name);

    /**
     * Sets a BioCarta identifier to be a Search criterion
     *
     * @param bcid BioCarta ID
     */
    void setSearchByBCID(String bcid);

    /**
     * Invokes the query for annotations to caBIO
     */
    void search();

    /**
     * Gets the <code>GeneAnnotation[]</code> returned by a serach to caBIO
     *
     * @return gene annotation search result
     */
    GeneAnnotation[] getGeneAnnotations();

    /**
     * Gets the number of <code>GeneAnnotation[]</code> obtained by a serach
     *
     * @return number of Genes retrieved
     */
    int getResultsSize();

    /**
     * Utility method to retrieve individual <code>GeneAnnotation</code> objects
     *
     * @param index index of Annotation to be retrieved
     * @return Annotation
     */
    GeneAnnotation getGeneAnnotationAtIndex(int index);
}