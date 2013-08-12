package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.common.domain.DatabaseCrossReference;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    private final Pathway[] pathways;

	/**
     * Organism abbreviation
     */
    private final String organism;
    
    private final Gene gene;
    /**
     * Constructor
     *
     * @param gene <code>Gene</code>
     */
    public GeneAnnotationImpl(final Gene gene) {
    	this.gene = gene;

        Gene g = new Gene();
        g.setId(gene.getId());

        gov.nih.nci.cabio.domain.Pathway pathway = new gov.nih.nci.cabio.domain.Pathway();
        Set<Gene> genes = new HashSet<Gene>();
        genes.add(g);
        pathway.setGeneCollection(genes);
        
        Pathway[] tmp = null;
        try {
        	ApplicationService appService = ApplicationServiceProvider.getApplicationService();
            List<gov.nih.nci.cabio.domain.Pathway> pways = appService.search(
                    "gov.nih.nci.cabio.domain.Pathway", pathway);
            tmp = new PathwayImpl[pways.size()];
            for (int i = 0; i < pways.size(); i++) {
            	gov.nih.nci.cabio.domain.Pathway p = pways.get(i);
                tmp[i] = new PathwayImpl(p.getName(), p.getDiagram());
            }
        } catch (ApplicationException e) {
            log.error(e);
        }  catch (Exception e) { // TODO why do we do this?
            log.error(e);
        }
        pathways = tmp;

		organism = gene.getTaxon().getAbbreviation();
    }

    /**
     * Gets the Gene Name
     *
     * @return Gene Name
     */
    @Override
    public String getGeneName() {
        return gene.getFullName();
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
    public org.geworkbench.components.annotations.Pathway[] getPathways() {
        return pathways;
    }

    @Override
    public String getGeneSymbol() {
        return gene.getSymbol();
    }

    /**
     * Creates <code>GeneAnnotation[]</code> object from <code>Gene[]</code>
     * obtained from caBIO queries
     *
     * @param geneList query result from caBIO
     * @return <code>GeneAnnotation[]</code> object
     */
    public static GeneAnnotation[] toUniqueArray(List<gov.nih.nci.cabio.domain.Gene> geneList) {
        Set<GeneAnnotation> uniqueGenes = new HashSet<GeneAnnotation>();
        for (int i = 0; i < geneList.size(); i++) {
        	Gene g = geneList.get(i);
        	if((g != null) && (g.getSymbol() != null)){
	            uniqueGenes.add(new GeneAnnotationImpl(g));
        	}
        }
        return uniqueGenes.toArray(new GeneAnnotation[]{});
    }

    public static GeneAnnotation[] toUniqueArray(List<gov.nih.nci.cabio.domain.Gene> geneList, String organism) {
        Set<GeneAnnotation> uniqueGenes = new HashSet<GeneAnnotation>();
        for (int i = 0; i < geneList.size(); i++) {
        	Gene g = geneList.get(i);

        	if((g != null) && (g.getSymbol() != null) && (g.getTaxon().getAbbreviation().equals(organism))){
	            uniqueGenes.add(new GeneAnnotationImpl(g));
        	}
        }
        return uniqueGenes.toArray(new GeneAnnotation[]{});
    }

    @Override
    public boolean equals(Object object) {
        GeneAnnotation other = (GeneAnnotation) object;
        String symbol = gene.getSymbol();
		return symbol.equals(other.getGeneSymbol());
    }

    @Override
    public int hashCode() {
        String symbol = gene.getSymbol();
        return symbol.hashCode();
    }

    @Override
	public Gene getGene() {
		return gene;
	}

	public static String getEntrezId(Gene gene) {
        String entrezId = "";
        Collection<DatabaseCrossReference> crossReferences = gene.getDatabaseCrossReferenceCollection();
        for (Iterator<DatabaseCrossReference> iterator = crossReferences.iterator(); iterator.hasNext();) {
			DatabaseCrossReference crossReference = (DatabaseCrossReference) iterator.next();
			if (crossReference.getDataSourceName().equals("LOCUS_LINK_ID")){
				entrezId = crossReference.getCrossReferenceId();
				break;
			}
		}
        return entrezId;
	}

	@Override
	public int compareTo(GeneAnnotation other) {
		String name = gene.getSymbol();
		if(name!=null && other.getGene()!=null) {
			return name.compareTo(other.getGene().getSymbol());
		} else {
			return -1;
		}
	}
}
