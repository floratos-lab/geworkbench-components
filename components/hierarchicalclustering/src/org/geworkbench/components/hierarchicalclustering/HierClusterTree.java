package org.geworkbench.components.hierarchicalclustering;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * <code>Canvas</code> on which dendrograms representing Hierarchical clustering
 * results are painted on. This class draws both Marker and Microarray Dendrograms
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id$
 */
public class HierClusterTree extends JPanel {
	private static final long serialVersionUID = 948216707369486985L;

	static Log log = LogFactory.getLog(HierClusterTree.class);

    public static enum Orientation { HORIZONTAL, VERTICAL } ;

    /**
     * Tree Orientation: either HORIZONTAL or VERTICAL
     */
    private Orientation orientation = Orientation.HORIZONTAL;

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
     * only if the Hierarchical tree is a strict binary tree
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
     * Check to call setSizes in paint only if the canvas has been resized
     */
    boolean resizingMarker = false;

    /**
     * To be set for VERTICAL dendrogram
     */
    int leftOffset = 0;

    /**
     * An array that represents the parts of the display that are under the specified node, used for selecting subtrees
     */
    private HierCluster currentHighlight;

    /**
     * Used to store where the mouse was so we can draw the highlight on repaint.
     */
    private int lastMouseX = -1, lastMouseY = -1;

    /**
     * Constructs a <code>HierClusterTree</code> for passed result and
     * with specified orientation.
     *
     * @param parent      <code>JPanel</code>
     * @param treeData    the result of a hcl calculation.
     * @param orientation the tree orientation.
     */
    public HierClusterTree(JPanel parent, HierCluster treeData, Orientation orientation) {
        super();
        this.parent = parent;
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
     * @param source
     * @return if the point is clickable or not
     */
    boolean isPointClickable(int x, int y, boolean markerSource) {
        if (!markerSource) {
            if (x >= leftOffset && x <= getWidth()) {
                return true;
            }
        } else {
            return true;
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
		return currentHighlight;
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
        resizingMarker = true;
    }

    /**
     * Utility method to get the height of the rendered dendrogram
     */
    int getMaxHeight() {
        if (orientation == Orientation.HORIZONTAL && microarraySet != null) {
            if (clusterRoot != null)
                maxHeight = clusterRoot.getLeafChildrenCount() * HierClusterDisplay.geneHeight;
            else
                maxHeight = microarraySet.markers().size() * HierClusterDisplay.geneHeight;
        } else if (orientation == Orientation.VERTICAL && microarraySet != null) {
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
        if (hc != null) {
            maxDepth = hc.getDepth();
        } else {
            maxDepth = 1;
        }
        
        if(microarraySet==null) return;
        
        if (orientation == Orientation.HORIZONTAL) {
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
        } else if (orientation == Orientation.VERTICAL) {
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
        }

    }

    /**
     * Node to highlight in display, usually the one that would be selected given a mouse click
     * @return
     */
    public void setCurrentHighlight(HierCluster currentHighlight) {
        if (this.currentHighlight != currentHighlight) {
            this.currentHighlight = currentHighlight;
            if (currentHighlight == null) {
                // Mark as out of region
                lastMouseX = -1;
                lastMouseY = -1;
            } else {
                log.debug("Selecting highlight "+currentHighlight.getDepth());
            }
            repaint();
        }
    }

    public void setCurrentHighlightForMouseLocation(int x, int y) {
        if (orientation == Orientation.HORIZONTAL) {
            repaintHighlightImage(x, y);
        } else if (orientation == Orientation.VERTICAL) {
            if (x > leftOffset) {
                repaintHighlightImage(x, y);
            }
        }
    }

    private Map<HierCluster,  RenderedNodeInfo> renderInfo = new HashMap<HierCluster, RenderedNodeInfo>();

    /**
     * Paints the branches and trunk from a particular node
     *
     * @param nodeParam   the Hierarchical clustering node currently being painted
     * @param startYparam vertical start coordinate for painting tree beneath this node
     * @param endYparam   vertical end coordinate for painting tree beneath this node
     */
    private void paintNode(Graphics ig, HierCluster nodeParam, int startYparam, int endYparam) {
        ArrayList<NodePaintingInstructions> nodesToPaint = new ArrayList<NodePaintingInstructions>();
        Map<Cluster, Integer> childCounts = nodeParam.getLeafChildrenCountMap();
        nodesToPaint.add(new NodePaintingInstructions(nodeParam, startYparam, endYparam));
		while (!nodesToPaint.isEmpty()) {
			NodePaintingInstructions instr = nodesToPaint.remove(0);
			HierCluster node = instr.node;

			if (node.isLeaf())
				continue;

			int startY = instr.startY;
			int endY = instr.endY;

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
			int x1 = offSet
					+ (int) Math.ceil((maxDepth - depth1) * branchWidth);
			int x2 = offSet
					+ (int) Math.ceil((maxDepth - depth2) * branchWidth);
			if (orientation == Orientation.HORIZONTAL) {
				ig.drawLine(x0, y0, x0, y1);
				ig.drawLine(x0, y0, x1, y0);
				ig.drawLine(x0, y1, x2, y1);
				renderInfo.put(node, new RenderedNodeInfo(startY, endY, x0,
						new Dimension(getWidth() - x0, endY - startY)));
			} else if (orientation == Orientation.VERTICAL) {
				ig.drawLine(leftOffset + y0, x0, leftOffset + y1, x0);
				ig.drawLine(leftOffset + y0, x0, leftOffset + y0, x1);
				ig.drawLine(leftOffset + y1, x0, leftOffset + y1, x2);
				renderInfo.put(node, new RenderedNodeInfo(leftOffset + startY,
						leftOffset + endY, x0, new Dimension(endY - startY,
								getHeight() - x0)));
			}
			ig.setColor(Color.black);

			int sY = startY, eY = startY + yTemp;
			nodesToPaint
					.add(new NodePaintingInstructions(child1, sY, eY));
			sY = eY;
			eY = endY;
			nodesToPaint
					.add(new NodePaintingInstructions(child2, sY, eY));
		}
	}

    private void paintHighlight(Graphics g, HierCluster startNode, int mouseX, int mouseY) {
        if (renderInfo.size() == 0) {
            log.debug("No rendering info yet to calculate highlight.");
            return;
        }
        if (mouseX < 0 || mouseY < 0) {
            return;
        }
        ArrayList<HierCluster> nodesToCheck = new ArrayList<HierCluster>();
        nodesToCheck.add(startNode);
        while (!nodesToCheck.isEmpty()) {
            HierCluster node = nodesToCheck.remove(0);
            RenderedNodeInfo thisInfo = renderInfo.get(node);
            RenderedNodeInfo child1Info = renderInfo.get(node.getNode(0));
            RenderedNodeInfo child2Info = renderInfo.get(node.getNode(1));
            if (mouseY > thisInfo.y) {
                if (child1Info != null && mouseX >= child1Info.startX && mouseX <= child1Info.endX) {
                    nodesToCheck.add(node.getNode(0));
                } else if (child2Info != null && mouseX >= child2Info.startX && mouseX <= child2Info.endX) {
                    nodesToCheck.add(node.getNode(1));
                }
            }
            if (nodesToCheck.isEmpty()) {
                HierCluster nodeToPaint = null;
                if (mouseY < thisInfo.y) {
                    nodeToPaint = (HierCluster) node.getParent();
                    if (nodeToPaint == null) {
                        nodeToPaint = node;
                    }
                } else {
                    nodeToPaint = node;
                }
                if (nodeToPaint != currentHighlight) {
                    this.currentHighlight = nodeToPaint;
                }
                if (this.currentHighlight != null) {
                    RenderedNodeInfo paintInfo = renderInfo.get(this.currentHighlight);
                    g.setColor(Color.blue);
                    if (orientation == Orientation.HORIZONTAL) {
                        g.fillRect(paintInfo.y, paintInfo.startX, (int) paintInfo.highlightDim.getWidth(), (int) paintInfo.highlightDim.getHeight());
                    } else {
                        g.fillRect(paintInfo.startX, paintInfo.y, (int) paintInfo.highlightDim.getWidth(), (int) paintInfo.highlightDim.getHeight());
                    }
                }
                return;
            }
        }
    }

    private static class NodePaintingInstructions {
        public HierCluster node;
        public int startY, endY;

        public NodePaintingInstructions(HierCluster node, int startY, int endY) {
            this.node = node;
            this.startY = startY;
            this.endY = endY;
        }
    }

    private static class RenderedNodeInfo {
        public int startX, endX;
        public int y;
        public Dimension highlightDim;

        public RenderedNodeInfo(int startX, int endX, int y, Dimension dimension) {
            this.startX = startX;
            this.endX = endX;
            this.y = y;
            this.highlightDim = dimension;
        }
    }

    /**
     * Paints the tree into specified graphics.
     *
     * @param g the Graphics context used for painting
     */
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (resizingMarker) {
            setSizes(clusterRoot);
        }

        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(Color.black);

        if (clusterRoot != null
            && clusterRoot.getLeafChildrenCount() > 1
            && !clusterRoot.isLeaf() ) {
        		paintNode(g, clusterRoot, 0, maxHeight);
        }

        Graphics2D g2d = (Graphics2D) g;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2d.setColor(Color.blue);
        if (clusterRoot != null){
        	paintHighlight(g2d, clusterRoot, lastMouseX, lastMouseY);
        }

        resizingMarker = false;
    }

    private void repaintHighlightImage(int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        repaint();
    }
}
