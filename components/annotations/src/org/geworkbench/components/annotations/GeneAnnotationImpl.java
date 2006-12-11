package org.geworkbench.components.annotations;

import gov.nih.nci.caBIO.bean.Gene;
import gov.nih.nci.common.exception.ManagerException;
import gov.nih.nci.common.exception.OperationException;
import org.geworkbench.util.annotation.Pathway;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Implementation of the <code>GeneAnnotation</code> contract
 *
 * @author First Genetic Trust
 * @version 1.0
 */
public class GeneAnnotationImpl implements GeneAnnotation {
    /**
     * Web URL prefix for obtaining Locus Link annotation
     */
    private static final String LOCUS_LINK_PREFIX = "http://www.ncbi.nlm.nih.gov/LocusLink/LocRpt.cgi?l=";
    /**
     * Web URL prefix for obtaining CGAP annotation
     */
    private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";
    /**
     * Web URL prefix currently being used
     */
    public static final String PREFIX_USED = GENE_FINDER_PREFIX;
    /**
     * Gene name
     */
    private String name = null;
    /**
     * Gene description
     */
    private String description = null;
    /**
     * Gene annotation URL
     */
    private URL url = null;
    /**
     * Associated pathways
     */
    private Pathway[] pathways = null;
    /**
     * Gene Locus Link ID
     */
    private String locusLinkId = null;
    /**
     * Gene Unigene Cluster ID
     */
    private Long unigeneClusterId = null;
    /**
     * Organism abbreviation
     */
    private String organism = null;

    /**
     * Default Constructor
     */
    public GeneAnnotationImpl() {
    }

    /**
     * Constructor
     *
     * @param gene <code>Gene</code>
     */
    public GeneAnnotationImpl(Gene gene) {
        name = gene.getTitle();
        description = gene.getLocusLinkSummary();
        locusLinkId = gene.getLocusLinkId();
/*
        try {
            // Todo: Change to new objects.
            pathways = PathwayImpl.toArray(gene.getPathways());
        } catch (OperationException oe) {
            oe.printStackTrace();
        }
*/

        url = composeURL(gene);
    }

    private URL composeURL(Gene gene) {
        URL link = null;
        unigeneClusterId = gene.getClusterId();
        try {
            organism = gene.getOrganismAbbreviation().trim();
            link = new URL(GENE_FINDER_PREFIX + "ORG=" + organism + "&CID=" + unigeneClusterId);
            //link = new URL(LOCUS_LINK_PREFIX + locusLinkId);
        } catch (MalformedURLException mu) {
            mu.printStackTrace();
        } catch (ManagerException me) {
            me.printStackTrace();
        }

        return link;
    }

    /**
     * Gets the Gene Name
     *
     * @return Gene Name
     */
    public String getGeneName() {
        return name;
    }

    /**
     * Gets the Gene Description
     *
     * @return Gene Description
     */
    public String getGeneDescription() {
        return description;
    }

    /**
     * Gets the Locus Link Identifier
     *
     * @return Locus Link ID
     */
    public String getLocusLinkId() {
        return locusLinkId;
    }

    /**
     * Gets the Unigene Cluster Identifier
     *
     * @return Unigene Cluster ID
     */
    public Long getUnigeneClusterId() {
        return unigeneClusterId;
    }

    /**
     * Gets the short form of 'Genus species' nomenclature of the organism
     * in question
     *
     * @return short name
     */
    public String getOrganismAbbreviation() {
        return organism;
    }

    /**
     * Gets the Gene Annotation Uniform Resource Locator. This could be either a
     * Locus Link annotation or an CGAP annotation
     *
     * @return annotation URL
     */
    public URL getGeneURL() {
        return url;
    }

    /**
     * Gets Scalable Vector Graphic images of Pathways contained in a
     * <code>Pathway[]</code> data structure
     *
     * @return pathways
     */
    public org.geworkbench.util.annotation.Pathway[] getPathways() {
        return pathways;
    }

    /**
     * Gets the number of Pathways associated with this <code>GeneAnnotation</code>
     * hence <code>Gene</code> instance
     *
     * @return pathway count
     */
    public int getPathwaysCount() {
        return pathways.length;
    }

    /**
     * Utility method to retrieve individual Pathways
     *
     * @param index index of Pathway to be retrieved
     * @return Pathway
     */
    public Pathway getPathwayAtIndex(int index) {
        if (index < pathways.length)
            return pathways[index];
        throw new ArrayIndexOutOfBoundsException(index);
    }

    /**
     * Creates <code>GeneAnnotation[]</code> object from <code>Gene[]</code>
     * obtained from caBIO queries
     *
     * @param array query result from caBIO
     * @return <code>GeneAnnotation[]</code> object
     */
    public static GeneAnnotation[] toArray(Gene[] array) {
        GeneAnnotation[] toBeReturned = new GeneAnnotationImpl[array.length];
        for (int i = 0; i < array.length; i++) {
            toBeReturned[i] = new GeneAnnotationImpl(array[i]);
        }

        return toBeReturned;
    }

}

