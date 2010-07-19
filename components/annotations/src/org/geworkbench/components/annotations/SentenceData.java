package org.geworkbench.components.annotations;

public class SentenceData implements Comparable<SentenceData> {

    public String sentence;

    public SentenceData(String sentence) {
        this.sentence = sentence;
    }

    @Override
    public int compareTo(SentenceData sentenceData) {
    	return sentence.compareTo(sentenceData.sentence);
    }
}