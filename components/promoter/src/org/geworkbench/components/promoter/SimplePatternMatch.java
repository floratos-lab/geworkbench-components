package org.geworkbench.components.promoter;

import org.geworkbench.bison.datastructure.pattern.IGetPattern;
import org.geworkbench.bison.datastructure.pattern.IPatternMatch;

public class SimplePatternMatch implements IPatternMatch {
    int seqID = -1;
    double pvalue = 0;
    int offset = 0;
    int length = 0;
    IGetPattern pattern = null;

    public void setPValue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getPValue() {
        return pvalue;
    }

    public SimplePatternMatch() {

    }

    public void setPattern(IGetPattern pattern) {
        this.pattern = pattern;
    }

    public void setAlignment(int seqID, int off, int length) {
        this.seqID = seqID;
        offset = off;
        this.length = length;

    }

    /**
     * getLength
     *
     * @return int
     */
    public int getLength() {
        return length;
    }

    /**
     * getOffset
     *
     * @return int
     */
    public int getOffset() {
        return offset;
    }

    /**
     * getPattern
     *
     * @return IGetPattern
     */
    public IGetPattern getPattern() {
        return pattern;
    }

    /**
     * getScore
     *
     * @return double
     */
    public double getScore() {
        return pvalue;
    }

    /**
     * getSeqID
     *
     * @return int
     */
    public int getSeqID() {
        return seqID;
    }

    /**
     * setLength
     *
     * @param length int
     */
    public void setLength(int length) {
        this.length = length;
    }
}
