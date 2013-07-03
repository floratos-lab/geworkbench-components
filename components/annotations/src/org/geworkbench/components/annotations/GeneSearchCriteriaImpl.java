package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.Pathway;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;
import javax.swing.JOptionPane;

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
 * @version $Id$
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
			
			List<Gene> results = appService.search(Gene.class, gene); 		
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
		 
			List<Gene> results = appService.search(Gene.class, gene);			
			return GeneAnnotationImpl.toUniqueArray(results);
			
		} catch (ApplicationException e) {
			log.error(e);
			return null;
		}
	}

	public GeneAnnotation[] searchByName(String name, String organism) {
		Gene gene = new Gene();
		gene.setSymbol(name);

		try {
		 
			List<Gene> results = appService.search(Gene.class, gene);			
			return GeneAnnotationImpl.toUniqueArray(results, organism);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
					"geWorkbench cannot retrieve gene annotations from the caBIO server.\nThere may be a connection error. Please check your network connection or try again later.",
					"Data processing/connection error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public GeneAnnotation[] searchByProbeId(String probeId) {
		log.error(new OperationNotSupportedException("No searchByProbeId implemented for caBio 4.2 yet."));
		return null;
	}

	public GeneAnnotation[] getGenesInPathway(
			org.geworkbench.util.annotation.Pathway pathway) {
		Pathway searchPathway = new Pathway();
		searchPathway.setName(pathway.getPathwayName());

		try {
		 
			List<Pathway> results = appService.search(Pathway.class, searchPathway);			 
			if (results.size() > 1) {
				log.warn("Found more than 1 pathway for "
						+ pathway.getPathwayName());
			}
			Pathway resultPathway = (Pathway)results.get(0);

			return GeneAnnotationImpl.toUniqueArray(new ArrayList<Gene>(resultPathway
					.getGeneCollection()));
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}

}