package org.geworkbench.components.promoter;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ProgressMonitor;

import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.*;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;

public class TranscriptionFactor implements DSPattern<DSSequence, CSSeqRegistration> {
    //need to contain binding site matrix and realted  Generic Marker
    private Matrix matrix = null;
    private String name;
    private String jasparID;
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

    public String getJasparID() {
        return jasparID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public void setJasparID(String jasparID) {
        this.jasparID = jasparID;
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

    public List<DSPatternMatch<DSSequence, CSSeqRegistration>> match(DSSequence sequence, double pValue) {
        //The pvalue is ignored here
        List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
        if (sequence.isDNA()) {
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
    public CSSeqRegistration match(DSSequence seqDB) {
        return null;
    }


    /**
     * this should create a tf from a marker that selected by user.
     *
     * @param mk IGenericMarker
     * @todo establish the relationship between marker and tfs
     */
    public TranscriptionFactor(DSGeneMarker mk) {
        String accession = mk.getLabel();
        String label = mk.getDescription();
    }

    /**
     * @param object    Object
     * @param threshold double
     * @return IGetPatternMatchCollection
     * @todo add something that tells the direction of a match
     * match
     */
    public List<DSPatternMatch<DSSequence, CSSeqRegistration>> match(DSCollection<DSSequence> seqDB) {
        //Pattern matches = new  PatternImpl();
        List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
        for (int k = 0; k < seqDB.size(); k++) {
            DSSequence seq = seqDB.get(k);
            if (seq.isDNA()) {
                for (int offset = 1; offset < seq.length() - matrix.getLength() + 1; offset++) {
                    double score = matrix.score(seq, offset);
                    double q = Math.exp(score);
                    double rscore = matrix.scoreReverse(seq, offset);
                    double q2 = Math.exp(rscore);
                    evaluate(matches, offset, q, seq, 0);
                    evaluate(matches, offset, q2, seq, 1);
                }
            }
        }
        return matches;
    }

    public List<DSPatternMatch<DSSequence, CSSeqRegistration>> progressiveMatch(Object object, ProgressMonitor progressMonitor, int start) {
        DSSequenceSet seqdb = (DSSequenceSet) object;
        List<DSPatternMatch<DSSequence, CSSeqRegistration>> matches = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
        //Pattern matches = new PatternImpl();

        for (int k = 0; k < seqdb.getSequenceNo(); k++) {

            progressMonitor.setProgress(start++);
            DSSequence seq = seqdb.getSequence(k);
            if (seq.isDNA()) {
                for (int offset = 1; offset < seq.length() - matrix.getLength() + 1; offset++) {

                    if (progressMonitor.isCanceled()) { //when user cancel this
                        progressMonitor.close();
                        return null;
                    }

                    double score = matrix.score(seq, offset);
                    double q = Math.exp(score);
                    double rscore = matrix.scoreReverse(seq, offset);
                    double q2 = Math.exp(rscore);
                    evaluate(matches, offset, q, seq, 0);
                    evaluate(matches, offset, q2, seq, 1);
                }
            }
        }

        return matches;
    }

    //  public IGetPatternMatchCollection match(Object object,JProgressBar jp) {
    //    Sequence seq = (Sequence) object;
    //    threshold=matrix.getThreshold(seq,0.05);
    //    SimplePatternMatchCollection matches = new SimplePatternMatchCollection();
    //    for (int offset = 1;
    //         offset < seq.length() - matrix.getLength() + 1;
    //         offset++) {
    //      double score = matrix.score(seq, offset);
    //      double q = Math.exp(score);
    //      double rscore = matrix.scoreReverse(seq, offset);
    //      double q2 = Math.exp(rscore);
    //      q2=0-q2;
    //      evaluate(matches, offset, q,k);
    //      evaluate(matches, offset, q2,k);
    //    }
    //    return matches;
    //  }

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

    /**
     * asString
     *
     * @return String
     */
    public String asString() {
        return name;
    }

    public String toString(DSSequence sequence, CSSeqRegistration reg) {
        return "TF" + sequence.getLabel() + ": " + reg.toString();
    }
}
