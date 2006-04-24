package org.geworkbench.components.example;

import org.geworkbench.bison.datastructure.biocollections.CSDataSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

/**
 * Stores the results of a T-Profiler run.
 */
public class TProfilerResults extends CSDataSet {

    public static final Comparator<ProfileGroup> T_VALUE_COMPARATOR = new Comparator<ProfileGroup>() {
        public int compare(ProfileGroup o1, ProfileGroup o2) {
            if (o1.getTValue() > o2.getTValue()) {
                return 1;
            } else if (o1.getTValue() < o2.getTValue()) {
                return -1;
            } else {
                return 0;
            }
        }
    };

    public static final Comparator<ProfileGroup> NAME_COMPARATOR = new Comparator<ProfileGroup>() {
        public int compare(ProfileGroup o1, ProfileGroup o2) {
            return o1.getGroupName().compareTo(o2.getGroupName());
        }
    };

    private ArrayList<ProfileGroup> groups;

    public TProfilerResults() {
        groups = new ArrayList<ProfileGroup>();
    }

    public void addGroup(ProfileGroup group) {
        groups.add(group);
    }

    public int getNumberOfGroups() {
        return groups.size();
    }

    public ProfileGroup getGroup(int index) {
        return groups.get(index);
    }

    public void sort(Comparator<ProfileGroup> comparator) {
        Collections.sort(groups, comparator);        
    }
}
