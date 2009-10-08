package org.geworkbench.components.annotations;

public class SentenceData implements Comparable {

    public String sentence;

    public SentenceData(String sentence) {
        this.sentence = sentence;
    }

    public int compareTo(Object o) {
        if (o instanceof SentenceData) {
            return sentence.compareTo(((SentenceData) o).sentence);
        }
        return -1;
    }
}