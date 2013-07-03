package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;

/**
 * 
 * @author zji
 * @version $Id$
 *
 */
public class GeneData implements Comparable<GeneData> {

    public final String name;
    public final Gene gene;
    private final String organism;

    public GeneData(final Gene gene, final String organism) {
        this.name = gene.getSymbol();
        this.gene = gene;
    	this.organism = organism;
    }

    public String getOrganism(){
    	return this.organism;
    }

    @Override
	public int compareTo(GeneData geneData) {
		return name.compareTo(geneData.name);
	}

}
