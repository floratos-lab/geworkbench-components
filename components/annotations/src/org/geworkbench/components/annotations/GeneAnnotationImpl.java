package org.geworkbench.components.annotations;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * Implementation of the <code>GeneAnnotation</code> contract
 *
 * @author First Genetic Trust
 * @version $Id$
 */
public class GeneAnnotationImpl implements GeneAnnotation {

    static Log log = LogFactory.getLog(GeneAnnotationImpl.class);

    /**
     * Associated pathways
     */
    private final String[] pathways;

	/**
     * Organism abbreviation
     */
    private final String organism;
    
    private final String geneSymbol;
    private final String geneFullName;
    private final String entrezId;
    private final Long clusterId;
    
	public GeneAnnotationImpl(String symbol, String fullName, String entrezId,
			Long clusterId, String organism, String[] pathways) {
    	geneSymbol = symbol;
    	geneFullName = fullName;
    	this.entrezId = entrezId;
    	this.clusterId = clusterId;
    	this.organism = organism;
        this.pathways = pathways;
    }

    /**
     * Gets the Gene Name
     *
     * @return Gene Name
     */
    @Override
    public String getGeneName() {
        return geneFullName;
    }

    /**
     * Gets the short form of 'Genus species' nomenclature of the organism
     * in question
     *
     * @return short name
     */
    @Override
    public String getOrganismAbbreviation() {
        return organism;
    }

    /**
     * Gets Scalable Vector Graphic images of Pathways contained in a
     * <code>Pathway[]</code> data structure
     *
     * @return pathways
     */
    @Override
    public String[] getPathways() {
        return pathways;
    }

    @Override
    public String getGeneSymbol() {
        return geneSymbol;
    }

    @Override
    public boolean equals(Object object) {
    	if(object==null) return false;
    	
        GeneAnnotation other = (GeneAnnotation) object;
		return geneSymbol.equals(other.getGeneSymbol());
    }

    @Override
    public int hashCode() {
        return geneSymbol.hashCode();
    }

    @Override
	public String getEntrezId() {
		return entrezId;
	}

    @Override
	public Long getClusterId() {
		return clusterId;
	}

	@Override
	public int compareTo(GeneAnnotation other) {
		if(geneSymbol!=null) {
			return geneSymbol.compareTo(other.getGeneSymbol());
		} else {
			return -1;
		}
	}
}
