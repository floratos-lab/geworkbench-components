package org.geworkbench.components.annotations;

import org.geworkbench.util.annotation.Pathway;

public class PathwayData implements Comparable {

    public String name;
    public Pathway pathway;

    public PathwayData(String name, Pathway pathway) {
        this.name = name;
        this.pathway = pathway;
    }

    public int compareTo(Object o) {
        if (o instanceof PathwayData) {
            return name.compareTo(((PathwayData) o).name);
        }
        return -1;
    }
}