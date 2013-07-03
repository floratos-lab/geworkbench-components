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
    private String organism;

    public GeneData(final String name, final Gene gene) {
        this.name = name;
        this.gene = gene;
        this.organism = "";
    }

    public void setOrganism(String organism){
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
