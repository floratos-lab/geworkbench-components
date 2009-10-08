package org.geworkbench.components.annotations;

public class RoleData implements Comparable {

    public String role;

    public RoleData(String role) {
        this.role = role;
    }

    public int compareTo(Object o) {
        if (o instanceof RoleData) {
            return role.compareTo(((RoleData) o).role);
        }
        return -1;
    }
}
