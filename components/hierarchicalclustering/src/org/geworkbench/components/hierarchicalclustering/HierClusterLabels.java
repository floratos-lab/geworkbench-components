package org.geworkbench.components.hierarchicalclustering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.text.AttributedString;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.HierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;


/**
 * <p>Title: Bioworks</p>
 * <p/>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence
 * and Genotype Analysis</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p/>
 * <p>Company: Columbia University</p>
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id$
 */

public class HierClusterLabels extends JPanel {
	private static final long serialVersionUID = -7687279206082169906L;

	static Log log = LogFactory.getLog(HierClusterLabels.class);

    /**
     * Value for width of marker in pixels
     */
    private static int geneWidth = 20;

    /**
     * The underlying micorarray set used in the hierarchical clustering
     * analysis.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = null;

    /**
     * The current array cluster being rendered in the marker Dendrogram
     */
    private MicroarrayHierCluster currentArrayCluster = null;

    /**
     * The leaf microarrays clusters in <code>currentArrayCluster</code>.
     */
    private Cluster[] leafArrays = null;

    /**
     * <code>Image</code> painted on synchronously with this panel
     */
    BufferedImage image = null;

    /**
     * Bit to paint the offline image
     */
    boolean imageSnapshot = false;

    /**
     * Font used with text labels
     */
    private Font labelFont = null;

    /**
     * Placeholder for font size
     */
    private int fontSize = 5;

    /**
     * The maximum font height in pixels
     */
    private final int maxFontSize = 10;

    /**
     * Default resolution for text display
     */
    private final int defaultResolution = 120;

    /**
     * The current resolution
     */
    private int resolution = defaultResolution;

    /**
     * To be set for labels
     */
    int leftOffset = 0;

    /**
     * Width of the dendrogram
     */
    private int width = 50;

    /**
     * Check to call setSizes in paint only if the canvas has been resized
     */
    boolean resizingMarker = false;

    public HierClusterLabels(JPanel parent) {
        super();
        // TODO parent is not used
        //this.parent = parent;
    }

    /**
     * Receives the reference microarray set on which the hierarchical
     * clustering analysis is based
     *
     * @param chips the reference microarray set
     */
    public void setChips(DSMicroarraySetView<DSGeneMarker, DSMicroarray> chips) {
        microarraySet = chips;
    }

    /**
     * Receives the <code>MicroarrayHierCluster</code> from the
     * <code>HierClusterViewWidget</code>
     *
     * @param mhc the marker cluster
     */
    public void setMicroarrayHierCluster(MicroarrayHierCluster mhc) {
        currentArrayCluster = mhc;

        if (currentArrayCluster != null) {
            java.util.List<Cluster> leaves = currentArrayCluster.getLeafChildren();
            leafArrays = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
            leaves.toArray(leafArrays);
        }
    }

    
    
    /**
     * <code>JComponent</code> method used to render this component
     *
     * @param g Graphics used for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
       
        //default value
        width = 100;
        
        try {
            if (microarraySet != null) {
                Graphics2D ig = null;

                if (imageSnapshot) {
                    image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
                    ig = image.createGraphics();
                    ig.setColor(Color.white);
                    ig.fillRect(0, 0, this.getWidth(), this.getHeight());
                }

                g.setColor(Color.white);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
                g.setColor(Color.black);

                int chipNo = 0;

                if (currentArrayCluster == null) {
                    chipNo = microarraySet.items().size();
                } else {
                    chipNo = leafArrays.length;
                }

                setFont();
                g.setFont(labelFont);

                AffineTransform at = new AffineTransform();
                at.rotate(-Math.PI / 2);

                AffineTransform saveAt = ((Graphics2D) g).getTransform();
                ((Graphics2D) g).transform(at);

                //calculate the max length for width
                for (int j = 0; j < chipNo; j++) {
                    DSMicroarray mArray = null;
                    if ((leafArrays != null) && (currentArrayCluster!=null)) {
                        mArray = ((MicroarrayHierCluster) leafArrays[j]).getMicroarray();
                    } else {
                        mArray = microarraySet.get(j);
                    }                  
                    String name = mArray.getLabel();
                    if (name != null) {                   
                       if( (name.length()/10 + 1)*50 > width)
                    	  width = (name.length()/10 + 1)*50;
                    }
                     
                }

                for (int j = 0; j < chipNo; j++) {
                    DSMicroarray mArray = null;

                    if ((leafArrays != null) && (currentArrayCluster!=null)) {
                        mArray = ((MicroarrayHierCluster) leafArrays[j]).getMicroarray();
                    } else {
                        mArray = microarraySet.get(j);
                    }

                    int yRatio = (int) ((j + 0.3) * geneWidth);
                    String name = mArray.getLabel();

                    if (name == null) {
                        name = "Undefined";
                    }                     
                    
    				AttributedString as = new AttributedString(name);
    				as.addAttribute(TextAttribute.FONT, labelFont);
    				as.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_LTR);
                    g.drawString(as.getIterator(), -width, leftOffset + yRatio);
                    
                }

                ((Graphics2D) g).setTransform(saveAt);

                if (imageSnapshot) {
                    ig.setColor(Color.black);
                    setFont();
                    ig.setFont(labelFont);

                    AffineTransform saveAt1 = ((Graphics2D) ig).getTransform();
                    ((Graphics2D) ig).transform(at);

                    for (int j = 0; j < chipNo; j++) {
                        DSMicroarray mArray = null;

                        if (leafArrays != null) {
                            mArray = ((MicroarrayHierCluster) leafArrays[j]).getMicroarray();
                        } else {
                            mArray = microarraySet.get(j);
                        }

                        int yRatio = (int) ((j + 0.3) * geneWidth);
                        String name = mArray.getLabel();

                        if (name == null) {
                            name = "Undefined";
                        }
                                                
                        ig.drawString(name, -width, leftOffset + yRatio);
                    }

                    ((Graphics2D) ig).setTransform(saveAt1);
                }
            }
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            log.error(npe);
        }
        
        if (resizingMarker) {
            setSizes();
        }
    }

    /**
     * Sets the <code>Font</code> used for drawing text
     */
    private void setFont() {
        int fontSize = Math.min(getFontSize(), (int) ((double) maxFontSize / (double) defaultResolution * (double) resolution));

        if ((fontSize != this.fontSize) || (labelFont == null)) {
            this.fontSize = fontSize;
            labelFont = new Font("Times New Roman", Font.PLAIN, this.fontSize);
        }
    }

    /**
     * Gets the <code>Font</code> size
     *
     * @return <code>Font</code> size
     */
    private int getFontSize() {
        return Math.max(HierClusterDisplay.geneWidth, 5);
    }

    /**
     * The <code>HierCluster<code> representing the entire
     * Hierarchical clustering tree obtained from the Analysis
     *
     * @param treeData <code>HierCluster<code> representing the entire
     *                 Hierarchical clustering tree
     */
    void setTreeData(HierCluster treeData) {
    	// TODO treeData is not used
        setSizes();
        resizingMarker = true;
    }

    /**
     * Sets the display sizes based on the root node obtained from the clustering
     * analysis
     *
     * @param hc root node used for setting sizes
     */
    private void setSizes() {

        if (microarraySet != null) {
            geneWidth = HierClusterDisplay.geneWidth;
            setPreferredSize(new Dimension(this.getParent().getWidth(), width));
            setSize(new Dimension(this.getParent().getWidth(), width));
        }
    }
}
