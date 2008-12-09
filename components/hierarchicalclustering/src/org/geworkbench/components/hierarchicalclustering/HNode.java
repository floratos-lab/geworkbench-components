package org.geworkbench.components.hierarchicalclustering;

import java.util.HashSet;
import java.util.Set;
import java.io.PrintStream;

/**
 * @author John Watkinson
 */
public class HNode {

    private Set<String> items;
    private int id;
    private HNode left;
    private HNode right;
    private int depth;
    private double height;

    public HNode(int id, Set<String> items, HNode left, HNode right, double height) {
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

    public void printCluster(PrintStream out) {
        if (left == null) {
            out.println(this);
        } else {
            left.printCluster(out);
            for (int i = 0; i < depth * 4; i++) {
                out.print(' ');
            }
            out.println(this);
            right.printCluster(out);
        }
    }

}
