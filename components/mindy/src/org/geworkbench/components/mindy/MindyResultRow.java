package org.geworkbench.components.mindy;

import java.io.Serializable;
import java.text.CollationKey;

import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * Represents a row in the MINDY result data.
 *
 * @author mhall
 * @version $Id: MindyResultRow.java,v 1.1 2009-03-02 16:30:45 oshteynb Exp $
 */
public class MindyResultRow implements Serializable{
    private DSGeneMarker modulator;
    private DSGeneMarker target;

    private float score;
    
    /**
     * Constructor.
     *
     * @param modulator
     * @param transcriptionFactor
     * @param target
     * @param score
     * @param pvalue
     */
    public MindyResultRow(DSGeneMarker modulator, DSGeneMarker target, float score) {
        this.modulator = modulator;
        this.target = target;
        this.score = score;
    }

    /**
     * Get the modulator in this MINDY result row.
     *
     * @return a modulator gene marker
     */
    public DSGeneMarker getModulator() {
        return modulator;
    }

    /**
     * Set the modulator in this MINDY result row.
     *
     * @param modulator
     */
    public void setModulator(DSGeneMarker modulator) {
        this.modulator = modulator;
    }

    /**
     * Get the target in this MINDY result row.
     *
     * @return a target gene marker
     */
    public DSGeneMarker getTarget() {
        return target;
    }

    /**
     * Set the target for this MINDY result row.
     *
     * @param target
     */
    public void setTarget(DSGeneMarker target) {
        this.target = target;
    }

    /**
     * Get the score in this MINDY result row.
     *
     * @return the score
     */
    public float getScore() {
        return score;
    }

    /**
     * Set the score for this MINDY result row.
     *
     * @param score
     */
    public void setScore(float score) {
        this.score = score;
    }
    
}