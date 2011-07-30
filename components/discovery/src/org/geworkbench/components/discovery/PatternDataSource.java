package org.geworkbench.components.discovery;

import java.util.Arrays;

import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternSorter;
import org.geworkbench.util.patterns.SequentialPatternSource;

/**
 * An adapter class for showing patterns.
 */
public class PatternDataSource implements SequentialPatternSource {

    private DSMatchedSeqPattern[] pattern = null;

    //Used to sort patterns
    static private PatternSorter sorter = new PatternSorter();

    public PatternDataSource(PatternResult db) {
        pattern = new DSMatchedSeqPattern[db.getPatternNo()];
        for (int i = 0; i < pattern.length; ++i) {
            pattern[i] = db.getPattern(i);
        }
    }

    public int getPatternSourceSize() {
        return pattern.length;
    }

    public DSMatchedSeqPattern getPattern(int i) {
        return pattern[i];
    }

    public void sort(int field) {
        sorter.setMode(field);
        Arrays.sort(pattern, sorter);
    }

    public void mask(int[] index, boolean mask) {
        //does nothing for local patterns
    }
}