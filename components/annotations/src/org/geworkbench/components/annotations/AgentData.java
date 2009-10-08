package org.geworkbench.components.annotations;

import gov.nih.nci.cabio.domain.Agent;


public class AgentData implements Comparable {

    public String name;
    public Agent agent;
    public String evsId;
    public AgentData(String name, Agent agent, String evsId) {
        this.name = name;
        this.agent = agent;
        this.evsId = evsId;
    }

    public int compareTo(Object o) {
        if (o instanceof AgentData) {
            return name.compareTo(((AgentData) o).name);
        }
        return -1;
    }
}
