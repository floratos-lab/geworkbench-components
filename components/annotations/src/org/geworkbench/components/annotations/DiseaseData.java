package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.DiseaseOntology;

public class DiseaseData implements Comparable {

    public String name;
    public DiseaseOntology diseaseOntology;
    public String evsId;
    public DiseaseData(String name, DiseaseOntology diseaseOntology, String evsId) {
        this.name = name;
        this.diseaseOntology = diseaseOntology;
        this.evsId = evsId;
    }

    public int compareTo(Object o) {
        if (o instanceof DiseaseData) {
            return name.compareTo(((DiseaseData) o).name);
        }
        return -1;
    }
}