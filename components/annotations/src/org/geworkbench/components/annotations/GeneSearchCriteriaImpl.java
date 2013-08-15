package org.geworkbench.components.annotations;

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
	
    @Override
	public GeneAnnotation[] searchByGeneSymbol(String geneSymbol, int taxonId) {
		BioDBnetClient client = new BioDBnetClient();
		client.queryByGeneSymbol(taxonId, geneSymbol);
		if (!client.hasResult()) {
			return null;
		}

		/* let's keep the original design's flexibility of returning an array. For now, we only return one element, though. */
		GeneAnnotation[] r = new GeneAnnotation[1];
		r[0] = client.getGeneAnnotation();
		return r;
	}

    @Override
	public GeneBase[] getGenesInPathway(String pathwayName) {
		BioDBnetClient client = new BioDBnetClient();
		return client.queryGenesForPathway(pathwayName);
	}

}