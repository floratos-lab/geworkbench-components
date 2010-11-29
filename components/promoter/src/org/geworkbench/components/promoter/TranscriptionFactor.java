package org.geworkbench.components.promoter;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.CSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.DSPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.bison.util.SequenceUtils;

public class TranscriptionFactor implements DSPattern<DSSequence, CSSeqRegistration> {
    //need to contain binding site matrix and related  Generic Marker
    private Matrix matrix = null;
    private String name;
    private double threshold = 0;

    public String getName() {
        return name;
    }

    public void setThreshold(double th) {
        threshold = th;
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public int getLength() {
        if (matrix == null) {
            return 0;
        } else {
            return matrix.getLength();
        }
    }

    public String toString() {
        return name;

    }

    public TranscriptionFactor() {
    }

    @Override
    public List<DSPatternMatch<DSSequence, CSSeqRegistration>> match(DSSequence sequence, double pValue) {
        //The pvalue is ignored here
        List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
        if (SequenceUtils.isValidDNASeqForBLAST(sequence)) {
            for (int offset = 0; offset < sequence.length() - matrix.getLength() + 1; offset++) {
                double score = matrix.score(sequence, offset);
                double q = Math.exp(score);
                double rscore = matrix.scoreReverse(sequence, offset);
                double q2 = Math.exp(rscore);
                evaluate(matches, offset, q, sequence, 0);
                evaluate(matches, offset, q2, sequence, 1);
            }
        }
        return matches;
    }

    /**
     * @todo to be implemented
     */
    @Override
    public CSSeqRegistration match(DSSequence seqDB) {
        return null;
    }

    private void evaluate(List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches, int offset, double q, DSSequence sequence, int strand) {
        if (q >= threshold) {
            CSPatternMatch<DSSequence, CSSeqRegistration> one = new CSPatternMatch<DSSequence, CSSeqRegistration>(sequence);
            CSSeqRegistration reg = new CSSeqRegistration();
            reg.x1 = offset;
            reg.x2 = offset + matrix.getLength();
            reg.strand = strand;
            one.setPValue(q);
            one.setRegistration(reg);
            matches.add(one);
        }
    }

    // FIXME this is a wrongly designed method from DSPattern
    // nothing from this class is used
    @Override
    public String toString(DSSequence sequence, CSSeqRegistration reg) {
        return "TF" + sequence.getLabel() + ": " + reg.toString();
    }

    @Override
    public boolean equals (Object object){
    	if(!(object instanceof TranscriptionFactor))
    		return false;
    	
    	TranscriptionFactor tf = (TranscriptionFactor )object;
    	if (this.getName().equals( tf.getName())){
    		return true;
    	} else {
    	   	return false;
    	}
    }
}
