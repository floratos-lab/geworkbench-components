package org.geworkbench.components.annotations;

public class EvsIdData implements Comparable {

    public String evsId;

    public EvsIdData(String evsId) {
        this.evsId = evsId;
    }

    public int compareTo(Object o) {
        if (o instanceof AgentData) {
            return evsId.compareTo(((EvsIdData) o).evsId);
        }
        return -1;
    }
}