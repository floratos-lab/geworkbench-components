package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.GenericReporter;
import gov.nih.nci.cabio.domain.Pathway;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService; 
import gov.nih.nci.system.client.ApplicationServiceProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: First Genetic Trust Inc.
 * </p>
 * <p/> Implementation of the <code>GeneSearchCriteria</code> contract
 * 
 * @author First Genetic Trust
 * @version 1.0
 */
public class GeneSearchCriteriaImpl implements GeneSearchCriteria {
	static Log log = LogFactory.getLog(GeneSearchCriteriaImpl.class);

		 
	ApplicationService appService;
	
	
	/**
     * Default Constructor
     */
    public GeneSearchCriteriaImpl() {
    	
       try{
    	   appService = ApplicationServiceProvider.getApplicationService();
       }catch( Exception e) {
           log.error(e);
       }
    
    }

	/**
	 * Sets a BioCarta identifier to be a Search criterion
	 * 
	 * @param bcid
	 *            BioCarta ID
	 */
	public GeneAnnotation[] searchByBCID(String bcid) {
		Gene gene = new Gene();
		gene.setSymbol(bcid);
		// gene.setClusterId(Long.parseLong(bcid));
		try {
			
			List results = appService.search(Gene.class, gene); 		
			return GeneAnnotationImpl.toUniqueArray(results);
			
		} catch (ApplicationException e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * Sets a <code>String</code> to be a Search criterion. This typically
	 * would be an Accession identifier
	 * 
	 * @param name
	 *            accession
	 */
	public GeneAnnotation[] searchByName(String name) {
		Gene gene = new Gene();
		gene.setSymbol(name);

		try {
		 
			List results = appService.search(Gene.class, gene);			
			return GeneAnnotationImpl.toUniqueArray(results);
			
		} catch (ApplicationException e) {
			log.error(e);
			return null;
		}
	}

	public GeneAnnotation[] searchByProbeId(String probeId) {
		GenericReporter reporter = new GenericReporter();
		reporter.setName(probeId);

		try {
			 
			List results = appService.search(Gene.class, reporter);			 
			return GeneAnnotationImpl.toUniqueArray(results);
			
		} catch (ApplicationException e) {
			log.error(e);
			return null;
		}
	}

	public GeneAnnotation[] getGenesInPathway(
			org.geworkbench.util.annotation.Pathway pathway) {
		Pathway searchPathway = new Pathway();
		searchPathway.setName(pathway.getPathwayName());

		try {
		 
			List results = appService.search(Pathway.class, searchPathway);			 
			if (results.size() > 1) {
				log.warn("Found more than 1 pathway for "
						+ pathway.getPathwayName());
			}
			Pathway resultPathway = (Pathway)results.get(0);

			return GeneAnnotationImpl.toUniqueArray(new ArrayList(resultPathway
					.getGeneCollection()));
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

	public static void main(String[] args) {
		GeneSearchCriteriaImpl searcher = new GeneSearchCriteriaImpl();
		GeneAnnotation[] geneAnnotations = searcher.searchByName("IL1A");
		for (int i = 0; i < geneAnnotations.length; i++) {
			GeneAnnotation geneAnnotation = geneAnnotations[i];
			System.out.println(geneAnnotation.getGeneName());
			org.geworkbench.util.annotation.Pathway[] pathways = geneAnnotation
					.getPathways();
			for (int j = 0; j < pathways.length; j++) {
				org.geworkbench.util.annotation.Pathway pathway = pathways[j];
				System.out.println(" - " + pathway.getPathwayName());
				GeneAnnotation[] genesInPathway = searcher
						.getGenesInPathway(pathway);
				for (int k = 0; k < genesInPathway.length; k++) {
					GeneAnnotation annotation = genesInPathway[k];
					System.out.println("   - " + annotation.getGeneSymbol()
							+ ":" + annotation.getGeneName());
				}
			}
		}
	}
}