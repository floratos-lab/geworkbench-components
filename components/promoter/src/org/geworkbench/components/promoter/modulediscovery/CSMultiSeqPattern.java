/*
 Written by (C) Kai Wang (kw2110@columbia.edu) Columbia University

 This code was written using Borland Java Builder X and may be subject to
 certain additional restrictions as a result.
 */

package org.geworkbench.components.promoter.modulediscovery;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.*;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqCmplxRegistration;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;
import org.geworkbench.util.RandomSequenceGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CSMultiSeqPattern extends CSMatchedPattern<DSSequence, CSSeqRegistration> implements DSMatchedPattern<DSSequence, CSSeqRegistration>, DSPattern<DSSequence, CSSeqRegistration> {
    static HashMap primeNumberPattern = new HashMap();
    DSSequenceSet seqDB = null;
    PatternKey patternKey; // each pattern is represented as a int array
    boolean isMaximal;

    public PatternKey getPatternKey() {
        return patternKey;
    }

    public void setSupport_set(ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>> matches) {
        this.matches = matches;
    }

    public void setPatternKey(PatternKey patternKey) {
        this.patternKey = patternKey;
    }

    public CSMultiSeqPattern(PatternKey patternKey) {
        this.pattern = this;
        this.patternKey = patternKey;
        this.isMaximal = true;
    }

    public CSMultiSeqPattern(PatternKey patternKey, List<DSPatternMatch<DSSequence, CSSeqRegistration>> newMatches) {
        this.pattern = this;
        this.patternKey = patternKey;
        this.isMaximal = true;
        this.matches.addAll(newMatches);
    }

    //    public void addSupport( suppport) {
    //        support_set.add(suppport);
    //    }

    public String asString() {
        String supports = "";
        for (int i = 0; i < matches.size(); i++) {
            DSSequence seq = matches.get(i).getObject();
            ArrayList<Integer> off = ((CSSeqCmplxRegistration) matches.get(i).getRegistration()).offsets;
            supports += "[" + seq.getSerial() + ": ";
            for (int j = 0; j < off.size(); j++) {
                supports += off.get(j) + " ";
            }
            supports += "] ";
        }
        return patternKey + " -> " + supports;
    }

    public CSMultiSeqPattern merge(CSMultiSeqPattern other_pattern, int window_length, int J0) {
        List<DSPatternMatch<DSSequence, CSSeqRegistration>> new_support_set = combineSupportSets(other_pattern.matches, window_length);
        // new pattern must have a support larger thatn J0 to be added into the hashtable
        if (new_support_set.size() >= J0) {
            CSMultiSeqPattern new_pattern = new CSMultiSeqPattern(patternKey.mergePattern(other_pattern.patternKey), new_support_set);

            if (matches.size() <= new_support_set.size()) {
                this.isMaximal = false;
            }

            if (other_pattern.matches.size() <= new_support_set.size()) {
                other_pattern.isMaximal = false;

            }
            return new_pattern;
        }
        return null;
    }

    // combine two support sets, all supports have same length
    private List<DSPatternMatch<DSSequence, CSSeqRegistration>> combineSupportSets(List<DSPatternMatch<DSSequence, CSSeqRegistration>> other_support_set, int window_length) {
        List<DSPatternMatch<DSSequence, CSSeqRegistration>> new_support_set = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
        for (int i = 0; i < matches.size(); i++) {
            DSPatternMatch<DSSequence, CSSeqRegistration> match1 = matches.get(i);
            DSSequence seq1 = match1.getObject();
            ArrayList<Integer> off1 = ((CSSeqCmplxRegistration) matches.get(i).getRegistration()).offsets;
            for (int j = 0; j < other_support_set.size(); j++) {
                DSPatternMatch<DSSequence, CSSeqRegistration> match2 = other_support_set.get(j);
                DSSequence seq2 = match2.getObject();
                ArrayList<Integer> off2 = ((CSSeqCmplxRegistration) match2.getRegistration()).offsets;
                // check whether the support is still in range
                if (isInRange(match1, match2, window_length)) {
                    ArrayList<Integer> new_support = mergeSupports(off1, off2);
                    if (new_support != null) {
                        boolean found = false;
                        // check whether the new_support already exists in the new_support_set
                        for (int k = 0; k < new_support_set.size(); k++) {
                            CSSeqCmplxRegistration registration = (CSSeqCmplxRegistration) new_support_set.get(k).getRegistration();
                            if (registration.offsets.equals(new_support)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            CSPatternMatch<DSSequence, CSSeqRegistration> match = new CSPatternMatch<DSSequence, CSSeqRegistration>(seq1);
                            CSSeqCmplxRegistration reg = new CSSeqCmplxRegistration();
                            reg.offsets = new_support;
                            match.setRegistration(reg);
                            new_support_set.add(match);
                        }
                    }
                } else if (seq1.getSerial() < seq2.getSerial() || (seq1.getSerial() == seq2.getSerial() && off1.get(off1.size() - 1) < off2.get(off2.size() - 1))) {
                    break;
                } else {
                    ;
                }
            }
        }
        return new_support_set;
    }

    public boolean isSelfCombinable(int window_length) {
        for (int i = 0; i < matches.size() - 1; i++) {
            DSPatternMatch<DSSequence, CSSeqRegistration> match1 = matches.get(i);
            DSPatternMatch<DSSequence, CSSeqRegistration> match2 = matches.get(i + 1);

            ArrayList<Integer> off1 = ((CSSeqCmplxRegistration) match1.getRegistration()).offsets;
            ArrayList<Integer> off2 = ((CSSeqCmplxRegistration) match2.getRegistration()).offsets;

            if (isInRange(match1, match2, window_length)) {
                ArrayList<Integer> new_support = mergeSupports(off1, off2);
                if (new_support != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isInRange(DSPatternMatch<DSSequence, CSSeqRegistration> s1, DSPatternMatch<DSSequence, CSSeqRegistration> s2, int window_length) {
        if (s1.getObject() != s2.getObject()) {
            return false; // must be on the same sequence
        }
        ArrayList<Integer> off1 = ((CSSeqCmplxRegistration) s1.getRegistration()).offsets;
        ArrayList<Integer> off2 = ((CSSeqCmplxRegistration) s2.getRegistration()).offsets;
        int len = off1.size();
        if (len != off2.size()) {
            return false; // must be same length
        }
        int max = (off1.get(len - 1) > off2.get(len - 1)) ? off1.get(len - 1) : off2.get(len - 1);
        int min = (off1.get(0) < off2.get(0)) ? off1.get(0) : off2.get(0);
        if ((max - min) < window_length) {
            return true;
        } else {
            return false;
        }
    }

    private static ArrayList<Integer> mergeSupports(ArrayList<Integer> s1, ArrayList<Integer> s2) {
        ArrayList<Integer> new_support = new ArrayList<Integer>();
        int i = 0, j = 0, pos = 0;
        while (i < s1.size() && j < s2.size()) {
            if (s1.get(i) == s2.get(j)) {
                new_support.add(s1.get(i));
                i++;
                j++;
            } else if (s1.get(i) < s2.get(j)) {
                new_support.add(s1.get(i));
                i++;
            } else {
                new_support.add(s2.get(j));
                j++;
            }
        }

        while (i < s1.size()) {
            new_support.add(s1.get(i++));

        }
        while (j < s2.size()) {
            new_support.add(s2.get(j++));

        }
        return new_support;
    }

    public List<DSPatternMatch<DSSequence, CSSeqRegistration>> match(DSSequence seqDB, double p) {
        /** @todo to be implemented */
        return null;
    }

    /**
     * @todo to be implemented
     */
    public CSSeqRegistration match(DSSequence seqDB) {
        return null;
    }

    /**
     * match
     *
     * @param object Object
     * @return IGetPatternMatchCollection
     */
    public List<DSPatternMatch<DSSequence, CSSeqRegistration>> match(List<DSSequence> seqDB) {
        List<DSPatternMatch<DSSequence, CSSeqRegistration>> col = new ArrayList<DSPatternMatch<DSSequence, CSSeqRegistration>>();
        for (int locusId = 0; locusId < matches.size(); locusId++) {
            DSPatternMatch<DSSequence, CSSeqRegistration> match1 = matches.get(locusId);
            ArrayList<Integer> off = ((CSSeqCmplxRegistration) match1.getRegistration()).offsets;
            int offset = off.get(0);
            int ty = Math.abs(off.get(off.size() - 1) - off.get(1)); //this still got problem. we need to know the length of the last module.
            DSSequence seq = match1.getObject();
            DSPatternMatch<DSSequence, CSSeqRegistration> sm = new CSPatternMatch<DSSequence, CSSeqRegistration>(seq);
            CSSeqCmplxRegistration reg = new CSSeqCmplxRegistration();
            reg.x1 = offset;
            reg.x2 = offset + ty;
            sm.setRegistration(reg);
            col.add(sm);
        }
        return col;
    }

    /**
     * asString
     *
     * @return String
     */
    public String toString() {
        return patternKey.toString();
    }

    public String toString(DSSequence seq, CSSeqRegistration r) {
        return toString();
    }

    /**
     * setThreshold
     *
     * @param rs RandomSequenceGenerator
     */
    public void setThreshold(RandomSequenceGenerator rs) {
    }
}
