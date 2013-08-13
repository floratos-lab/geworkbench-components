package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.common.domain.DatabaseCrossReference;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * <p>
 * Company: Columbia University
 * </p>
 * <p/> Implementation of the <code>GeneSearchCriteria</code> contract
 * 
 * @author First Genetic Trust
 * @version $Id$
 */
public class GeneSearchCriteriaImpl implements GeneSearchCriteria {
	static Log log = LogFactory.getLog(GeneSearchCriteriaImpl.class);

	final private ApplicationService appService;
	
	/**
     * Default Constructor
     */
    public GeneSearchCriteriaImpl() {
    	
		ApplicationService tmp = null;
		try {
			tmp = ApplicationServiceProvider.getApplicationService();
		} catch (Exception e) {
			log.error(e);
		}
		appService = tmp;
    }

    @Override
	public GeneAnnotation[] searchByName(String name, String organism) {
		Gene gene = new Gene();
		gene.setSymbol(name);

		try {
		 
			List<Gene> results = appService.search(Gene.class, gene);			
			return GeneSearchCriteriaImpl.toUniqueArray(results, organism);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
					"geWorkbench cannot retrieve gene annotations from the caBIO server.\nThere may be a connection error. Please check your network connection or try again later.",
					"Data processing/connection error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

    @Override
	public GeneBase[] getGenesInPathway(String pathwayName) {
    	gov.nih.nci.cabio.domain.Pathway searchPathway = new gov.nih.nci.cabio.domain.Pathway();
		searchPathway.setName(pathwayName);

		try {
		 
			List<gov.nih.nci.cabio.domain.Pathway> results = appService.search(gov.nih.nci.cabio.domain.Pathway.class, searchPathway);			 
			if (results.size() > 1) {
				log.warn("Found more than 1 pathway for "
						+ pathwayName);
			}
			gov.nih.nci.cabio.domain.Pathway resultPathway = (gov.nih.nci.cabio.domain.Pathway)results.get(0);

			return GeneSearchCriteriaImpl.toUniqueArray(new ArrayList<Gene>(resultPathway
					.getGeneCollection()));
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

    private static Pathway[] getPathways(Long geneId) {
        Gene g = new Gene();
        g.setId(geneId);

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
        
        return tmp;
    }
    
	private static GeneBase[] toUniqueArray(
			List<gov.nih.nci.cabio.domain.Gene> geneList) {
		Set<GeneBase> uniqueGenes = new HashSet<GeneBase>();
		for (int i = 0; i < geneList.size(); i++) {
			Gene g = geneList.get(i);

			if (g == null || g.getSymbol() == null) {
				continue;
			}
			uniqueGenes.add(new GeneImpl(g.getSymbol(), g.getFullName()));
		}
		return uniqueGenes.toArray(new GeneBase[] {});
	}
    
    private static GeneAnnotation[] toUniqueArray(List<gov.nih.nci.cabio.domain.Gene> geneList, String organism) {
        Set<GeneAnnotation> uniqueGenes = new HashSet<GeneAnnotation>();
        for (int i = 0; i < geneList.size(); i++) {
        	Gene g = geneList.get(i);

        	if(g==null || g.getSymbol()==null) {
        		continue;
        	}
        	
        	if(organism == null || (g.getTaxon().getAbbreviation().equals(organism))){
        		// find entrez ID
                String entrezId = "";
                Collection<DatabaseCrossReference> crossReferences = g.getDatabaseCrossReferenceCollection();
                for (Iterator<DatabaseCrossReference> iterator = crossReferences.iterator(); iterator.hasNext();) {
        			DatabaseCrossReference crossReference = (DatabaseCrossReference) iterator.next();
        			if (crossReference.getDataSourceName().equals("LOCUS_LINK_ID")){
        				entrezId = crossReference.getCrossReferenceId();
        				break;
        			}
        		}
                
				Pathway[] pathways = getPathways(g.getId());
				uniqueGenes.add(new GeneAnnotationImpl(g.getSymbol(), g
						.getFullName(), entrezId, g.getClusterId(), g
						.getTaxon().getAbbreviation(), pathways));
        	}
        }
        return uniqueGenes.toArray(new GeneAnnotation[]{});
    }
}