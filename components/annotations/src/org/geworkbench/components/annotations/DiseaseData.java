package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.DiseaseOntology;

public class DiseaseData implements Comparable<DiseaseData> {

    public String name;
    public DiseaseOntology diseaseOntology;
    public String evsId;
    public DiseaseData(String name, DiseaseOntology diseaseOntology, String evsId) {
        this.name = name;
        this.diseaseOntology = diseaseOntology;
        this.evsId = evsId;
    }

    @Override
    public int compareTo(DiseaseData diseaseData) {
    	return name.compareTo(diseaseData.name);
    }
}