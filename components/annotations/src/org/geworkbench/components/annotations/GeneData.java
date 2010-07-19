package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Gene;


public class GeneData implements Comparable<GeneData> {
    /**
     * Web URL prefix for obtaining CGAP annotation
     */
    private static final String GENE_FINDER_PREFIX = "http://cgap.nci.nih.gov/Genes/GeneInfo?";
    /**
     * Web URL prefix currently being used
     */
    public static final String PREFIX_USED = GENE_FINDER_PREFIX;

    public String name;
    public Gene gene;
    public String organism;

    public GeneData(String name, Gene gene) {
        this.name = name;
        this.gene = gene;
        this.organism = "";
    }

    public GeneData(GeneAnnotation annotation) {
        this.name = annotation.getGeneSymbol();
        this.gene = annotation.getGene();
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
