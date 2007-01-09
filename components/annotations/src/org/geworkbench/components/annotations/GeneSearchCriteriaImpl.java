package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;
import gov.nih.nci.cabio.domain.GenericReporter;
import gov.nih.nci.system.applicationservice.ApplicationService;
import gov.nih.nci.system.applicationservice.ApplicationServiceProvider;
import gov.nih.nci.system.applicationservice.ApplicationException;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    static Log log = LogFactory.getLog(GeneSearchCriteriaImpl.class);

    ApplicationService appService = ApplicationServiceProvider.getApplicationService();

    /**
     * Sets a BioCarta identifier to be a Search criterion
     *
     * @param bcid BioCarta ID
     */
    public GeneAnnotation[] searchByBCID(String bcid) {
        Gene gene = new Gene();
        gene.setSymbol(bcid);
//        gene.setClusterId(Long.parseLong(bcid));
        try {
            List<Gene> results = appService.search(Gene.class, gene);
            return GeneAnnotationImpl.toUniqueArray(results);
        } catch (ApplicationException e) {
            log.error(e);
            return null;
        }
    }

    /**
     * Sets a <code>String</code> to be a Search criterion. This typically would
     * be an Accession identifier
     *
     * @param name accession
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

    public GeneAnnotation[] searchByProbeId(String probeId) {
        GenericReporter reporter = new GenericReporter();
        reporter.setName(probeId);

        try {
            List<Gene> results = appService.search(Gene.class, reporter);
            return GeneAnnotationImpl.toUniqueArray(results);
        } catch (ApplicationException e) {
            log.error(e);
            return null;
        }
    }
}