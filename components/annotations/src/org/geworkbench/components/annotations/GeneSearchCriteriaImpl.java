package org.geworkbench.components.annotations;

import gov.nih.nci.caBIO.bean.ExpressionMeasurementSearchCriteria;
import gov.nih.nci.caBIO.bean.Gene;
import gov.nih.nci.caBIO.bean.SearchResult;
import gov.nih.nci.caBIO.util.CriteriaElement;
import gov.nih.nci.caBIO.util.ManagerException;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Implementation of the <code>GeneSearchCriteria</code> contract
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public class GeneSearchCriteriaImpl implements GeneSearchCriteria {
    private ExpressionMeasurementSearchCriteria exMeasurementSC = null;
    private gov.nih.nci.caBIO.bean.GeneSearchCriteria geneSearchCriteria = null;
    private Gene gene = null;
    private GeneAnnotation[] geneAnnotations = null;
    private SearchResult result = null;

    /**
     * Default Constructor
     */
    public GeneSearchCriteriaImpl() {
        gene = new Gene();
    }

    /**
     * Sets a BioCarta identifier to be a Search criterion
     *
     * @param bcid BioCarta ID
     */
    public void setSearchByBCID(String bcid) {
        geneSearchCriteria = new gov.nih.nci.caBIO.bean.GeneSearchCriteria();
        geneSearchCriteria.setBCId(bcid.toUpperCase());
    }

    /**
     * Sets a <code>String</code> to be a Search criterion. This typically would
     * be an Accession identifier
     *
     * @param name accession
     */
    public void setSearchName(String name) {
        exMeasurementSC = new ExpressionMeasurementSearchCriteria();
        geneSearchCriteria = new gov.nih.nci.caBIO.bean.GeneSearchCriteria();
        exMeasurementSC.setName(name);
        geneSearchCriteria.putSearchCriteria(exMeasurementSC, CriteriaElement.AND);
    }

    /**
     * Invokes the query for annotations to caBIO
     */
    public void search() {
        try {
            result = gene.search(geneSearchCriteria);
        } catch (ManagerException me) {
            me.printStackTrace();
        }

        assert result != null : "Result null";
        geneAnnotations = GeneAnnotationImpl.toArray((Gene[]) result.getResultSet());
    }

    /**
     * Gets the <code>GeneAnnotation[]</code> returned by a serach to caBIO
     *
     * @return gene annotation search result
     */
    public GeneAnnotation[] getGeneAnnotations() {
        return geneAnnotations;
    }

    /**
     * Gets the number of <code>GeneAnnotation[]</code> obtained by a serach
     *
     * @return number of Genes retrieved
     */
    public int getResultsSize() {
        return geneAnnotations.length;
    }

    /**
     * Utility method to retrieve individual <code>GeneAnnotation</code> objects
     *
     * @param index index of Annotation to be retrieved
     * @return Annotation
     */
    public GeneAnnotation getGeneAnnotationAtIndex(int index) {
        if (index < geneAnnotations.length)
            return geneAnnotations[index];
        throw new ArrayIndexOutOfBoundsException(index);
    }

}