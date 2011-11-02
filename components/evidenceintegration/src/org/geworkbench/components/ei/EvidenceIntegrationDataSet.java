package org.geworkbench.components.ei;

import java.util.List;
import java.util.Map;

import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.DSBioObject;

import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * @author mhall
 */
public class EvidenceIntegrationDataSet extends CSAncillaryDataSet<DSBioObject> {
    private static final long serialVersionUID = -6835973287728524201L;
    private List<Evidence> evidence;
    private String filename;
    private Map<Integer, String> goldStandardSources;

    @SuppressWarnings("unchecked")
	public EvidenceIntegrationDataSet(DSDataSet<? extends DSBioObject> parent, String label, List<Evidence> evidence, String filename, Map<Integer, String> goldStandardSources) {
        super((DSDataSet<DSBioObject>) parent, label);
        this.evidence = evidence;
        this.filename = filename;
        this.goldStandardSources = goldStandardSources;
    }

    public List<Evidence> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<Evidence> evidence) {
        this.evidence = evidence;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Map<Integer, String> getGoldStandardSources() {
        return goldStandardSources;
    }

    public void setGoldStandardSources(Map<Integer, String> goldStandardSources) {
        this.goldStandardSources = goldStandardSources;
    }
}

