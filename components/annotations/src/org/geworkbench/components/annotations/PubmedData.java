package org.geworkbench.components.annotations;

public class PubmedData implements Comparable<PubmedData> {

    public String id;

    public PubmedData(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(PubmedData pubmedData) {
    	return id.compareTo(pubmedData.id);
    }
}