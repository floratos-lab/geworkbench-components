package org.geworkbench.components.discovery.view;

import org.geworkbench.util.patterns.CSMatchedHMMOriginSeqPattern;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.CSMatchedHMMSeqPattern;

/**
 * The visual representation of a Pattern node.
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author $AUTHOR$
 * @version 1.0
 */
public class PatternNode {
    public PatternNode() {
    }

    public CSMatchedSeqPattern pattern = null;
    //The number of sequence were the pattern was NOT found
    int sequenceExcluded;
    String path = null;

    public String toString() {
        if (pattern != null) {
            if (pattern instanceof CSMatchedHMMOriginSeqPattern) {
                CSMatchedHMMSeqPattern hmmPatOrigin = (CSMatchedHMMSeqPattern) pattern;
                return "HMM [" + hmmPatOrigin.ascii + "] SeqNo: " + hmmPatOrigin.getUniqueSupport() + " Supp: " + hmmPatOrigin.getSupport();

            } else {
                org.geworkbench.util.patterns.CSMatchedSeqPattern pat = (CSMatchedSeqPattern) pattern;
                if (pat.ascii != null) {
                    return "[" + pat.ascii + "] " + pat.seqNo.value;
                } else {
                    return "[] " + pat.seqNo.value;
                }
            }
        } else {
            return "[] " + sequenceExcluded;
        }
    }

    public org.geworkbench.util.patterns.CSMatchedSeqPattern getPattern() {
        return pattern;
    }

    public String getPath() {
        return path;
    }

    public void setPattern(CSMatchedSeqPattern p) {
        pattern = p;
    }

    /**
     * Sets the number of sequences were the pattern was not found in.
     *
     * @param excluded int
     */
    public void setSequenceExcluded(int excluded) {
        sequenceExcluded = excluded;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
