package org.geworkbench.components.hierarchicalclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.Vector;

import javax.swing.JPanel;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.model.clusters.Cluster;
import org.geworkbench.bison.model.clusters.MarkerHierCluster;
import org.geworkbench.bison.model.clusters.MicroarrayHierCluster;
import org.geworkbench.bison.util.colorcontext.ColorContext;


/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * <p/>
 * <code>Canvas</code> on which an Eisen Plot representing Markers and
 * Microarrays used for Hierarchical clustering are painted on
 *
 * @author manjunath at genomecenter dot columbia dot edu
 * @version $Id$
 */
public class HierClusterDisplay extends JPanel {
	private static final long serialVersionUID = -1551868794872426240L;

	/**
     * Value for height of marker in pixels
     */
    protected static int geneHeight = 5;

    /**
     * Value for width of marker in pixels
     */
    protected static int geneWidth = 20;

    /**
     * The underlying micorarray set used in the hierarchical clustering
     * analysis.
     */
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySet = null;

    /**
     * The current marker cluster being rendered in the marker Dendrogram
     */
    private MarkerHierCluster currentMarkerCluster = null;

    /**
     * The leaf marker clusters in <code>currentMarkerCluster</code>.
     */
    private Cluster[] leafMarkers = null;

    /**
     * The current array cluster being rendered in the marker Dendrogram
     */
    private MicroarrayHierCluster currentArrayCluster = null;

    /**
     * The leaf microarrays clusters in <code>currentArrayCluster</code>.
     */
    private Cluster[] leafArrays = null;

    /**
     * Space from eisenplot where the accession is printed
     */
    private int labelGutter = 5;

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
     * Insensity of the markers painted set from the intensity slider
     */
    private double intensity = 1.0;

    /**
     * Swing layout manager for this <code>JPanel</code>
     */
    private BorderLayout borderLayout1 = new BorderLayout();

    /**
     * <code>Image</code> painted on synchronously with this panel
     */
    BufferedImage image = null;

    /**
     * Bit to paint the offline image
     */
    boolean imageSnapshot = false;

    /**
     * Keeps track of the positions where the markers are drawn
     */
    private Vector<MarkerInfoPosition> markerPositions = new Vector<MarkerInfoPosition>();

    /**
     * Default Constructor
     */
    public HierClusterDisplay() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * Receives the <code>MarkerHierCluster</code> from the
     * <code>HierClusterViewWidget</code>
     *
     * @param mhc the marker cluster
     */
    public void setMarkerHierCluster(MarkerHierCluster mhc) {
        currentMarkerCluster = mhc;

        if (currentMarkerCluster != null) {
            java.util.List<Cluster> leaves = currentMarkerCluster.getLeafChildren();
            leafMarkers = (Cluster[]) Array.newInstance(Cluster.class, leaves.size());
            leaves.toArray(leafMarkers);
        }
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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        try {
            if (microarraySet != null) {
                markerPositions.clear();

                Graphics2D ig = null;

                if (imageSnapshot) {
                    image = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
                    ig = image.createGraphics();
                    ig.setColor(Color.white);
                    ig.fillRect(0, 0, this.getWidth(), this.getHeight());
                }

                int fontGutter = (int) ((double) geneHeight * .22);
                int geneNo = 0;

                if (currentMarkerCluster == null) {
                    geneNo = microarraySet.markers().size();
                } else {
                    geneNo = leafMarkers.length;
                }

                //geneNo = currentMarkerCluster.getLeafChildrenCount();
                int chipNo = 0;

                if (currentArrayCluster == null) {
                    chipNo = microarraySet.items().size();
                } else {
                    chipNo = leafArrays.length;
                }

                //chipNo = currentArrayCluster.getLeafChildrenCount();
                ColorContext colorCtx = null;

                if (microarraySet.getDataSet() instanceof DSMicroarraySet) {
                    colorCtx = (org.geworkbench.bison.util.colorcontext.ColorContext) ((DSMicroarraySet<DSMicroarray>) microarraySet.getDataSet()).getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
                } else {
                    colorCtx = new org.geworkbench.bison.util.colorcontext.DefaultColorContext();
                }

                int firstMarker = (int) this.getVisibleRect().getY() / geneHeight;
                int lastMarker = firstMarker + ((int) this.getVisibleRect().getHeight() / geneHeight) + 2;
                lastMarker = (lastMarker > geneNo) ? geneNo : lastMarker;

                for (int i = firstMarker; i < lastMarker; i++) {
                    DSGeneMarker stats = null;

                    if ((leafMarkers != null)&&(i < leafMarkers.length)) {
                        stats = ((MarkerHierCluster) leafMarkers[i]).getMarkerInfo();
                    } else {
                        stats = microarraySet.markers().get(i);
                    }

                    int y = i * geneHeight;

                    //if(i + 1 > 500) {
                    //  return;
                    //}
                    for (int j = 0; j < chipNo; j++) {
                        DSMicroarray mArray = null;

                        if ((leafArrays != null)&&(j < leafArrays.length)) {
                            mArray = ((MicroarrayHierCluster) leafArrays[j]).getMicroarray();
                        } else {
                            mArray = microarraySet.items().get(j);
                        }

                        int x = (j * geneWidth);
                        int width = ((j + 1) * geneWidth) - x;
                        DSMutableMarkerValue marker = mArray.getMarkerValue(stats);

                        //MarkerValue marker = mArray.getMarker(microarraySet.
                        //                                    getMarkerInfoIndex(
                        //stats));
                        Color color = colorCtx.getMarkerValueColor(marker, stats, (float) intensity);
                        g.setColor(color);
                        g.fillRect(x, y, width, geneHeight);
                    }

                    g.setColor(Color.black);
                    setFont();
                    g.setFont(labelFont);

                    int xRatio = (chipNo * geneWidth) + labelGutter;
                    int yRatio = (y + geneHeight) - fontGutter;
                    String accession = stats.getLabel();
                    String geneName = stats.getShortName();
                    markerPositions.add(new MarkerInfoPosition(stats, 0, y + geneHeight));

                    if (accession == null) {
                        accession = "Undefined";
                    }

                    g.drawString(accession + (geneName != null && accession.compareTo(geneName) != 0 ?
                       " (" + geneName + ")" : ""), xRatio, yRatio);
                }

                if (imageSnapshot) {
                    for (int i = 0; i < geneNo; i++) {
                        DSGeneMarker stats = null;

                        if (leafMarkers != null) {
                            stats = ((MarkerHierCluster) leafMarkers[i]).getMarkerInfo();
                        } else {
                            stats = microarraySet.markers().get(i);
                        }

                        int y = i * geneHeight;

                        //if(i + 1 > 500) {
                        //  return;
                        //}
                        for (int j = 0; j < chipNo; j++) {
                            DSMicroarray mArray = null;

                            if (leafArrays != null) {
                                mArray = ((MicroarrayHierCluster) leafArrays[j]).getMicroarray();
                            } else {
                                mArray = microarraySet.get(j);
                            }

                            int x = (j * geneWidth);
                            int width = ((j + 1) * geneWidth) - x;
                            DSMutableMarkerValue marker = mArray.getMarkerValue(stats);

                            //MarkerValue marker = mArray.getMarker(microarraySet.
                            //                                    getMarkerInfoIndex(
                            //stats));
                            Color color = colorCtx.getMarkerValueColor(marker, stats, (float) intensity);
                            ig.setColor(color);
                            ig.fillRect(x, y, width, geneHeight);
                        }

                        ig.setColor(Color.black);
                        setFont();
                        ig.setFont(labelFont);

                        int xRatio = (chipNo * geneWidth) + labelGutter;
                        int yRatio = (y + geneHeight) - fontGutter;
                        String accession = stats.getLabel();
						String geneName = stats.getShortName();
						if (accession == null) {
							accession = "Undefined";
						}
						String drawMe = accession + (geneName != null
										&& accession.compareTo(geneName) != 0 ? " ("
										+ geneName + ")" : "");
						ig.drawString(drawMe, xRatio, yRatio);
					}
                }
            }
        } catch (NullPointerException npe) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
            }

            ;
            repaint();
        }
    }

    /**
     * Gets the <code>MarkerInfo</code> corresponding to a region on the
     * Eisen plot
     *
     * @param x x coordinate of the point to be queried
     * @param y y coordinate of the point to be queried
     * @return <code>MarkerInfo</code> cooresponding to the point queried
     */
    DSGeneMarker getMarkerInfoClicked(int x, int y) {
        DSGeneMarker mInfo = null;

        for (MarkerInfoPosition mip : markerPositions) {
            if (mip.contains(x, y)) {
                return mip.getMarkerInfo();
            }
        }

        return mInfo;
    }

    /**
     * Sets the Color intensity of the Marker spots displayed
     *
     * @param it intensity
     */
    void setIntensity(double it) {
        intensity = it;
        repaint();
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
        return Math.max(geneHeight, 5);
    }

    /**
     * Draws a Rectangular region corresponding to each Marker Spot
     *
     * @param x           x coordinate of the <code>Rectangle</code> to be drawn at
     * @param y           y coordinate of the <code>Rectangle</code> to be drawn at
     * @param showToolTip if <code>ToolTip</code>s are to be shown or not
     */
    void drawCell(int x, int y, boolean showToolTip) {
        if (microarraySet != null) {
            Graphics g = this.getGraphics();
            g.setXORMode(Color.black);
            g.setColor(Color.white);

            int row = y / geneHeight;
            int column = x / geneWidth;
            int R = 0;

            if (currentMarkerCluster != null) {
                R = currentMarkerCluster.getLeafChildrenCount();
            } else {
                R = microarraySet.markers().size();
            }

            int C = 0;

            if (currentArrayCluster != null) {
                C = currentArrayCluster.getLeafChildrenCount();
            } else {
                C = microarraySet.items().size();
            }

            if ((row < R) && (column < C)) {
                int y0 = row * geneHeight;
                int x0 = column * geneWidth;
                int y1 = (row + 1) * geneHeight;
                int x1 = (column + 1) * geneWidth;
                g.drawLine(x0, y0, x0, y1);
                g.drawLine(x0, y0, x1, y0);
                g.setXORMode(Color.white);
                g.setColor(Color.black);
                g.drawLine(x1, y1, x1, y0);
                g.drawLine(x1, y1, x0, y1);

                if (showToolTip) {
                    DSGeneMarker mInfo = null;

                    if (currentMarkerCluster != null) {
                        mInfo = ((MarkerHierCluster) leafMarkers[row]).getMarkerInfo();
                    } else {
                        mInfo = microarraySet.markers().get(row);
                    }

                    DSMicroarray marray = null;

                    if (currentArrayCluster != null) {
                        marray = ((MicroarrayHierCluster) leafArrays[column]).getMicroarray();
                    } else {
                        marray = microarraySet.get(column);
                    }

                    this.setToolTipText("<html>Chip: " + marray.getLabel() + "<br>" + "Marker: " + mInfo.getLabel() + "<br>" + "Signal: " + marray.getMarkerValue(mInfo).getValue() + "</html>");
                }

                this.revalidate();
                this.repaint();
            } else {
                this.setToolTipText(null);
            }
        }
    }

    /**
     * Configures the Graphical User Interface and Listeners
     *
     * @throws Exception
     */
    private void jbInit() throws Exception {
        this.setBackground(Color.white);
        this.setLayout(borderLayout1);
    }

    /**
     * Encapsulates individual <code>Rectangle</code> regions between points
     * drawn as a part of the Eisen plot canvas
     */
    private class MarkerInfoPosition {
        Rectangle rectangle = null;
        DSGeneMarker markerInfo = null;

        public MarkerInfoPosition(DSGeneMarker mInfo, int x, int y) {
            markerInfo = mInfo;
            rectangle = new Rectangle(x, y - geneHeight, microarraySet.items().size() * geneWidth, geneHeight);
        }

        public boolean contains(int x, int y) {
            return rectangle.contains(x, y);
        }

        public DSGeneMarker getMarkerInfo() {
            return markerInfo;
        }
    }
	/**
	 * detail see mantis #1578. 
	 * Clear old variables. 
	 * This method will be called in hierClusterModelChange() in HierClusterViewWidget.java 
	 * when new data arrives.
	 */
	public void resetVariables() {
		this.currentArrayCluster = null;
		this.currentMarkerCluster = null;
		this.leafArrays = null;
		this.leafMarkers = null;
		this.microarraySet = null;
	}    
}
