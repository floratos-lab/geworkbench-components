package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.Pathway;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.client.ApplicationServiceProvider;

import java.util.ArrayList;
import java.util.List;

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
			return GeneAnnotationImpl.toUniqueArray(results, organism);
			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, 
					"geWorkbench cannot retrieve gene annotations from the caBIO server.\nThere may be a connection error. Please check your network connection or try again later.",
					"Data processing/connection error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

    @Override
	public GeneAnnotation[] getGenesInPathway(
			org.geworkbench.components.annotations.Pathway pathway) {
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