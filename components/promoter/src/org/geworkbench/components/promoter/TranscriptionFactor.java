package org.geworkbench.components.promoter;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.CSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.bison.util.SequenceUtils;

public class TranscriptionFactor {
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

    public TranscriptionFactor(String name, Matrix matrix) {
    	this.name = name;
    	this.matrix = matrix;
    }

    public List<DSPatternMatch<DSSequence, CSSeqRegistration>> match(DSSequence sequence) {
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

    @Override
    public int hashCode() {
    	if(name==null) return 42; // an arbitrary constant
    	else return name.hashCode();
    }

}
