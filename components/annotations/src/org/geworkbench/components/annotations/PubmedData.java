package org.geworkbench.components.annotations;

public class PubmedData implements Comparable {

    public String id;

    public PubmedData(String id) {
        this.id = id;
    }

    public int compareTo(Object o) {
        if (o instanceof PubmedData) {
            return id.compareTo(((PubmedData) o).id);
        }
        return -1;
    }
}