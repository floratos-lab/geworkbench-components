package org.geworkbench.components.hierarchicalclustering;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.Cluster;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * <code>Canvas</code> on which dendrograms representing Hierarchical clustering
 * results are painted on. This class draws both Marker and Microarray Dendrograms
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version 3.0
 */
public class HierClusterTree extends JPanel {

    /**
     * HORIZONTAL orientation
     */
    public static final int HORIZONTAL = 0;

    /**
     * VERTICAL orientation
     */
    public static final int VERTICAL = 1;

    /**
     * Tree Orientation: either HORIZONTAL or VERTICAL
     */
    protected int orientation = HORIZONTAL;

    /**
     * Dataset on which clustering was performed
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = null;

    /**
     * Result of Hierarchical Clustering
     */
    private HierCluster clusterRoot = null;

    /**
     * Maximum depth of the tree in terms of the nodes between the root and
     * a leaf, both inclusive
     */
    private int maxDepth;

    /**
     * Maximum height in pixels of the tree given the number of leaves,
     * computed based on the visual height of each leaf. This would be attained
     * only if the Heirarchical tree is a strict binary tree
     */
    private int maxHeight;

    /**
     * Offset from top left corner of the canvas in canvas for drawing the tree
     */
    private int offSet = 5;

    /**
     * Width of the dendrogram
     */
    private int width = 50;

    /**
     * Number of pixels used for drawing a step from a node to one of it's
     * children, recomputed again based on canvas size and
     * <code>maxDepth</code>
     */
    private double branchWidth = 3d;

    /**
     * Container holding this dendrogram tree
     */
    private JPanel parent = null;

    /**
     * Vector containing the "brackets" used for rendering the dendrogram
     */
    private Vector brackets = new Vector();

    /**
     * Check to call setSizes in paint only if the canvas has been resized
     */
    protected boolean resizingMarker = false;

    /**
     * To be set for VERTICAL dendrogram
     */
    int leftOffset = 0;

    /**
     * <code>Image</code> painted on synchronously with this panel
     */
    BufferedImage image = null;

    /**
     * Bit to paint the offline image
     */
    boolean imageSnapshot = false;

    /**
     * Image obtained before scaling
     */
    private Graphics2D ig = null;

    /**
     * Constructor
     *
     * @param parent Widget containing this dendrogram
     */
    public HierClusterTree(JPanel parent) {
        super();
        this.parent = parent;
    }

    /**
     * Constructs a <code>HierClusterTree</code> for passed result and
     * with specified orientation.
     *
     * @param parent      <code>JPanel</code>
     * @param treeData    the result of a hcl calculation.
     * @param orientation the tree orientation.
     */
    public HierClusterTree(JPanel parent, HierCluster treeData, int orientation) {
        super();
        this.parent = parent;
        setBackground(Color.white);
        this.clusterRoot = treeData;
        this.orientation = orientation;
        setTreeData(treeData);
    }

    /**
     * Tests to see if a given point lies on the branches of the Dendrogram and
     * hence if it is clickable
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return if the point is clickable or not
     */
    boolean isPointClickable(int x, int y) {
        for (Enumeration e = brackets.elements(); e.hasMoreElements();) {
            Bracket b = (Bracket) e.nextElement();
            if (orientation == VERTICAL) {
                if (b.contains(y, x))
                    return true;
            } else if (orientation == HORIZONTAL) {
                if (b.contains(x, y))
                    return true;
            }
        }
        return false;
    }

    /**
     * Gets the <code>HierCluster</code> representing the root of the subtree that
     * was clicked
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return <code>HierCluster</code> representing the root of the subtree that
     *         was clicked
     */
    HierCluster getNodeClicked(int x, int y) {
        for (Enumeration e = brackets.elements(); e.hasMoreElements();) {
            Bracket b = (Bracket) e.nextElement();
            if (orientation == VERTICAL) {
                if (b.contains(y, x))
                    return b.getRoot();
            } else if (orientation == HORIZONTAL) {
                if (b.contains(x, y))
                    return b.getRoot();
            }
        }
        return null;
    }

    /**
     * Sets the <code>MicroarraySet</code> on which the Hierachical Clustering
     * Analysis was performed
     *
     * @param chips <code>MicroarraySet</code> on which the Hierrachical Clustering
     *              Analysis was performed
     */
    void setChips(DSMicroarraySetView<DSGeneMarker, DSMicroarray> chips) {
        microarraySet = chips;
        resizingMarker = true;
    }

    /**
     * The <code>HierCluster<code> representing the entire
     * Hierarchical clustering tree obtained from the Analysis
     *
     * @param treeData <code>HierCluster<code> representing the entire
     *                 Hierarchical clustering tree
     */
    void setTreeData(HierCluster treeData) {
        clusterRoot = treeData;
        setSizes(clusterRoot);
        brackets.clear();
        resizingMarker = true;
        //paintImmediately(0, 0, this.getWidth(), this.getHeight());
    }

    /**
     * Utility method to get the height of the rendered dendrogram
     */
    int getMaxHeight() {
        if (orientation == HORIZONTAL && microarraySet != null) {
            if (clusterRoot != null)
                maxHeight = clusterRoot.getLeafChildrenCount() * HierClusterDisplay.geneHeight;
            else
                maxHeight = microarraySet.markers().size() * HierClusterDisplay.geneHeight;
        } else if (orientation == VERTICAL && microarraySet != null) {
            if (clusterRoot != null)
                maxHeight = clusterRoot.getLeafChildrenCount() * HierClusterDisplay.geneWidth;
            else
                maxHeight = microarraySet.items().size() * HierClusterDisplay.geneWidth;
        }
        return maxHeight;
    }

    /**
     * Sets the display sizes based on the root node obtained from the clustering
     * analysis
     *
     * @param hc root node used for setting sizes
     */
    private void setSizes(HierCluster hc) {
        brackets.clear();
        if (hc != null)
            maxDepth = hc.getDepth();
        else
            maxDepth = 1;
        if (orientation == HORIZONTAL && microarraySet != null) {
            if (hc != null)
                maxHeight = hc.getLeafChildrenCount() * HierClusterDisplay.geneHeight;
            else
                maxHeight = microarraySet.markers().size() * HierClusterDisplay.geneHeight;
            width = parent.getWidth() / 3;
            branchWidth = Math.ceil((double) width / (double) maxDepth);
            if (branchWidth < 2d)
                branchWidth = 2d;
            width = (int) branchWidth * maxDepth + offSet;
            setPreferredSize(new Dimension((int) width, maxHeight));
            setSize(new Dimension((int) width, maxHeight));
        } else if (orientation == VERTICAL && microarraySet != null) {
            if (hc != null)
                maxHeight = hc.getLeafChildrenCount() * HierClusterDisplay.geneWidth;
            else
                maxHeight = microarraySet.items().size() * HierClusterDisplay.geneWidth;
            width = parent.getHeight() / 6;
            branchWidth = Math.ceil((double) width / (double) maxDepth);
            if (branchWidth < 2d)
                branchWidth = 2d;
            width = (int) branchWidth * maxDepth + offSet;
            setPreferredSize(new Dimension(this.getParent().getWidth(), width));
            setSize(new Dimension(this.getParent().getWidth(), width));
            // branchWidth = (double) width / (double) maxDepth;
        }
    }

    /**
     * Paints the branches and trunk from a particular node
     *
     * @param g      the Graphics context used for painting
     * @param node   the Hierarchical clustering node currently being painted
     * @param startY vertical start coordinate for painting tree beneath this node
     * @param endY   vertical end coordinate for painting tree beneath this node
     */
/*
    private void paintNode(Graphics g, HierCluster node, int startY, int endY) {
        if (!node.isLeaf()) {
            int depth = node.getDepth();
            HierCluster child1 = node.getNode(0);
            HierCluster child2 = node.getNode(1);
            int numChild1 = child1.getLeafChildrenCount();
            int numChild2 = child2.getLeafChildrenCount();
            int totalChildren = node.getLeafChildrenCount();
            int depth1 = child1.getDepth();
            int depth2 = child2.getDepth();
            int yTemp = (numChild1 * (endY - startY) / totalChildren);
            int yTemp2 = (numChild2 * (endY - startY) / totalChildren);
            int y0 = startY + (yTemp / 2);
            int x0 = offSet + (int) Math.ceil((maxDepth - depth) * branchWidth);
            int y1 = startY + yTemp + (yTemp2 / 2);
            int x1 = offSet + (int) Math.ceil((maxDepth - depth1) * branchWidth);
            int x2 = offSet + (int) Math.ceil((maxDepth - depth2) * branchWidth);
            if (orientation == HORIZONTAL) {
                ig.drawLine(x0, y0, x0, y1);
                ig.drawLine(x0, y0, x1, y0);
                ig.drawLine(x0, y1, x2, y1);
                brackets.add(new Bracket(node, x0, x1, x2, y0, y1));
            } else if (orientation == VERTICAL) {
                ig.drawLine(leftOffset + y0, x0, leftOffset + y1, x0);
                ig.drawLine(leftOffset + y0, x0, leftOffset + y0, x1);
                ig.drawLine(leftOffset + y1, x0, leftOffset + y1, x2);
                brackets.add(new Bracket(node, x0, x1, x2, leftOffset + y0, leftOffset + y1));
            }

            int sY = startY, eY = startY + yTemp;
            paintNode(g, child1, sY, eY);
            sY = eY;
            eY = endY;
            paintNode(g, child2, sY, eY);
        }
    }
*/

    private void paintNode(Graphics g, HierCluster nodeParam, int startYparam, int endYparam) {
        ArrayList<NodePaintingInstructions> nodesToPaint = new ArrayList<NodePaintingInstructions>();
        Map<Cluster, Integer> childCounts = nodeParam.getLeafChildrenCountMap();
        nodesToPaint.add(new NodePaintingInstructions(nodeParam, startYparam, endYparam));
        while (!nodesToPaint.isEmpty()) {
            NodePaintingInstructions instr = nodesToPaint.remove(0);
            HierCluster node = instr.node;
            int startY = instr.startY;
            int endY = instr.endY;
            if (!node.isLeaf()) {
                int depth = node.getDepth();
                HierCluster child1 = node.getNode(0);
                HierCluster child2 = node.getNode(1);
                int numChild1 = Math.max(childCounts.get(child1), 1);
                int numChild2 = Math.max(childCounts.get(child2), 1);

                int totalChildren = numChild1 + numChild2;
                int depth1 = child1.getDepth();
                int depth2 = child2.getDepth();
                int yTemp = (numChild1 * (endY - startY) / totalChildren);
                int yTemp2 = (numChild2 * (endY - startY) / totalChildren);
                int y0 = startY + (yTemp / 2);
                int x0 = offSet + (int) Math.ceil((maxDepth - depth) * branchWidth);
                int y1 = startY + yTemp + (yTemp2 / 2);
                int x1 = offSet + (int) Math.ceil((maxDepth - depth1) * branchWidth);
                int x2 = offSet + (int) Math.ceil((maxDepth - depth2) * branchWidth);
                if (orientation == HORIZONTAL) {
                    ig.drawLine(x0, y0, x0, y1);
                    ig.drawLine(x0, y0, x1, y0);
                    ig.drawLine(x0, y1, x2, y1);
                    brackets.add(new Bracket(node, x0, x1, x2, y0, y1));
                } else if (orientation == VERTICAL) {
                    ig.drawLine(leftOffset + y0, x0, leftOffset + y1, x0);
                    ig.drawLine(leftOffset + y0, x0, leftOffset + y0, x1);
                    ig.drawLine(leftOffset + y1, x0, leftOffset + y1, x2);
                    brackets.add(new Bracket(node, x0, x1, x2, leftOffset + y0, leftOffset + y1));
                }

                int sY = startY, eY = startY + yTemp;
//                paintNode(g, child1, sY, eY);
                nodesToPaint.add(new NodePaintingInstructions(child1, sY, eY));
                sY = eY;
                eY = endY;
//                paintNode(g, child2, sY, eY);
                nodesToPaint.add(new NodePaintingInstructions(child2, sY, eY));
            }
        }
    }

    private class NodePaintingInstructions {
        public HierCluster node;
        public int startY, endY;

        public NodePaintingInstructions(HierCluster node, int startY, int endY) {
            this.node = node;
            this.startY = startY;
            this.endY = endY;
        }
    }

    /**
     * Paints the tree into specified graphics when this component is not showing.
     *
     * @param g the Graphics context used for painting
     */
    protected void auxiliaryPaintComponent(Graphics g) {
        if (imageSnapshot || resizingMarker) {
            if (resizingMarker) {
                setSizes(clusterRoot);
            }
            image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            ig = image.createGraphics();
            ig.setColor(Color.white);
            ig.fillRect(0, 0, this.getWidth(), this.getHeight());
            ig.setColor(Color.black);
            if (clusterRoot != null) {
                if (clusterRoot.getLeafChildrenCount() > 1) {
                    if (!clusterRoot.isLeaf()) {
                        paintNode(g, clusterRoot, 0, maxHeight);
                    }
                }
            }
        }
        if (image != null)
            ((Graphics2D) g).drawImage(image, null, 0, 0);
        resizingMarker = false;
    }

    /**
     * Paints the tree into specified graphics.
     *
     * @param g the Graphics context used for painting
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (imageSnapshot || resizingMarker) {
            if (resizingMarker) {
                setSizes(clusterRoot);
            }
            image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            ig = image.createGraphics();
            ig.setColor(Color.white);
            ig.fillRect(0, 0, this.getWidth(), this.getHeight());
            ig.setColor(Color.black);
            if (clusterRoot != null) {
                if (clusterRoot.getLeafChildrenCount() > 1) {
                    if (!clusterRoot.isLeaf()) {
                        paintNode(g, clusterRoot, 0, maxHeight);
                    }
                }
            }
        }
        if (image != null)
            ((Graphics2D) g).drawImage(image, null, 0, 0);
        resizingMarker = false;
    }

    /**
     * Encapsulates graphical portions of the Dendrogram covered by a single node
     */
    class Bracket {
        HierCluster root = null;
        int x0, x1, x2, y0, y1;

        public Bracket(HierCluster root, int x0, int x1, int x2, int y0, int y1) {
            this.root = root;
            this.x0 = x0;
            this.x1 = x1;
            this.x2 = x2;
            this.y0 = y0;
            this.y1 = y1;
        }

        public HierCluster getRoot() {
            return root;
        }

        public boolean contains(int x, int y) {
            if (x == x0)
                return (y >= y0) && (y <= y1);
            else if (y == y0)
                return (x >= x0) && (x <= x1);
            else if (y == y1)
                return (x >= x0) && (x <= x2);
            return false;
        }
    }
}
