package org.geworkbench.components.ei;

import edu.columbia.c2b2.evidenceinegration.Edge;

import java.util.List;

/**
 * @author mhall
 */
public class Evidence {

    private String name = "Default Name";
    private List<Edge> edges;
    private int bins = 30;
    private boolean enabled = true;

    public Evidence(String name, List<Edge> edges) {
        this.name = name;
        this.edges = edges;
    }

    public Evidence(String name, List<Edge> edges, int bins) {
        this.name = name;
        this.edges = edges;
        this.bins = bins;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }

    public int getBins() {
        return bins;
    }

    public void setBins(int bins) {
        this.bins = bins;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
