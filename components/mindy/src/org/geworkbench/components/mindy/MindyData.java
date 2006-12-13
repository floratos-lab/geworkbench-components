package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

import java.util.List;
import java.util.ArrayList;

/**
 * Class containing MINDY run results.
 * @author mhall
 */
public class MindyData {

    private CSMicroarraySet arraySet;
    private List<MindyResultRow> data;

    public MindyData(CSMicroarraySet arraySet, List<MindyResultRow> data) {
        this.arraySet = arraySet;
        this.data = data;
    }

    public CSMicroarraySet getArraySet() {
        return arraySet;
    }

    public void setArraySet(CSMicroarraySet arraySet) {
        this.arraySet = arraySet;
    }

    public List<MindyResultRow> getData() {
        return data;
    }

    public void setData(List<MindyResultRow> data) {
        this.data = data;
    }

    public List<DSGeneMarker> getModulators() {
        ArrayList<DSGeneMarker> modulators = new ArrayList<DSGeneMarker>();
        for (MindyResultRow mindyResultRow : data) {
            modulators.add(mindyResultRow.getModulator());
        }
        return modulators;
    }

    public List<DSGeneMarker> getTranscriptionFactors() {
        ArrayList<DSGeneMarker> modulators = new ArrayList<DSGeneMarker>();
        for (MindyResultRow mindyResultRow : data) {
            modulators.add(mindyResultRow.getTranscriptionFactor());
        }
        return modulators;
    }

    public static class MindyResultRow {
        private DSGeneMarker modulator;
        private DSGeneMarker transcriptionFactor;
        private DSGeneMarker target;

        private float score;
        private float pvalue;

        public MindyResultRow(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, DSGeneMarker target, float score, float pvalue) {
            this.modulator = modulator;
            this.transcriptionFactor = transcriptionFactor;
            this.target = target;
            this.score = score;
            this.pvalue = pvalue;
        }

        public DSGeneMarker getModulator() {
            return modulator;
        }

        public void setModulator(DSGeneMarker modulator) {
            this.modulator = modulator;
        }

        public DSGeneMarker getTranscriptionFactor() {
            return transcriptionFactor;
        }

        public void setTranscriptionFactor(DSGeneMarker transcriptionFactor) {
            this.transcriptionFactor = transcriptionFactor;
        }

        public DSGeneMarker getTarget() {
            return target;
        }

        public void setTarget(DSGeneMarker target) {
            this.target = target;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public float getPvalue() {
            return pvalue;
        }

        public void setPvalue(float pvalue) {
            this.pvalue = pvalue;
        }

    }

}
