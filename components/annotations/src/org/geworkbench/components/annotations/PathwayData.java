package org.geworkbench.components.annotations;

import org.geworkbench.util.annotation.Pathway;

public class PathwayData implements Comparable<PathwayData> {

    public String name;
    public Pathway pathway;

    public PathwayData(String name, Pathway pathway) {
        this.name = name;
        this.pathway = pathway;
    }

    @Override
    public int compareTo(PathwayData pathwayData) {
    	return name.compareTo(pathwayData.name);
    }
}