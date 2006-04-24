package org.geworkbench.components.example;

import java.util.Set;

/**
 * Stores a row of T-Profiler data.
 */
public class ProfileGroup {

    private String groupName;
    private double tValue;
    private Set<String> markerIDs;

    /**
     * Constructs a new ProfileGroup.
     * @param groupName the name of this group (for example, GO Term or Pathway)
     * @param tValue the t value of the group.
     * @param markerIDs the set of marker IDs that belong to this group.
     */
    public ProfileGroup(String groupName, double tValue, Set<String> markerIDs) {
        this.groupName = groupName;
        this.tValue = tValue;
        this.markerIDs = markerIDs;
    }

    public String getGroupName() {
        return groupName;
    }

    public double getTValue() {
        return tValue;
    }

    public Set<String> getMarkerIDs() {
        return markerIDs;
    }

}
