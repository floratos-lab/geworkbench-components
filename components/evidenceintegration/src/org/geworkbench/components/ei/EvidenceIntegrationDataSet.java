package org.geworkbench.components.ei;

import org.geworkbench.bison.datastructure.biocollections.CSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;

import java.io.File;
import java.util.List;
import java.util.Map;

import edu.columbia.c2b2.evidenceinegration.Evidence;

/**
 * @author mhall
 */
public class EvidenceIntegrationDataSet extends CSAncillaryDataSet implements DSAncillaryDataSet {
    private static final long serialVersionUID = -6835973287728524201L;
    private List<Evidence> evidence;
    private String filename;
    private Map<Integer, String> goldStandardSources;

    public EvidenceIntegrationDataSet(DSDataSet parent, String label, List<Evidence> evidence, String filename, Map<Integer, String> goldStandardSources) {
        super(parent, label);
        this.evidence = evidence;
        this.filename = filename;
        this.goldStandardSources = goldStandardSources;
    }

    public File getDataSetFile() {
        // no-op
        return null;
    }

    public void setDataSetFile(File file) {
        // no-op
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

