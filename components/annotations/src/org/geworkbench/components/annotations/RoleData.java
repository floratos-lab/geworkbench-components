package org.geworkbench.components.annotations;

public class RoleData implements Comparable<RoleData> {

    public String role;

    public RoleData(String role) {
        this.role = role;
    }

    @Override
    public int compareTo(RoleData roleData) {
    	return role.compareTo(roleData.role);
    }
}
