package org.geworkbench.components.hierarchicalclustering.computation;

import java.io.Serializable;
import java.util.Set;

/**
 * @author John Watkinson
 * @version $Id$
 */
public class HNode implements  Serializable {
 
	private static final long serialVersionUID = -5341700067900832231L;
	
	private Set<String> items;
    private int id;
    private HNode left;
    private HNode right;
    private int depth;
    private double height;

    public HNode(){};
    
    public HNode(final int id, final Set<String> items, final HNode left, final HNode right, final double height) {
        this.id = id;
        this.items = items;
        this.left = left;
        this.right = right;
        this.height = height;
        if (left == null) {
            depth = 0;
        } else {
            depth = Math.max(left.depth, right.depth) + 1;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final HNode hNode = (HNode) o;

        if (id != hNode.id) return false;

        return true;
    }

    public Set<String> getItems() {
        return items;
    }

    public boolean isLeafNode() {
        return (left == null);
    }

    public HNode getLeft() {
        return left;
    }

    public HNode getRight() {
        return right;
    }

    public int getId() {
        return id;
    }

    public int size() {
        return items.size();
    }

    public double getHeight() {
        // Currently depth
        return depth;
    }

    public String getLeafItem() {
        return items.iterator().next();
    }

    public int hashCode() {
        return id;
    }

    public String toString() {
        if (left == null) {
            return items.iterator().next();
        } else {
            return "" + height;
        }
    }

}
