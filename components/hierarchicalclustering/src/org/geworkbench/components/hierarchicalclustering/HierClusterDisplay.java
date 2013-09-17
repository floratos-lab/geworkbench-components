package org.geworkbench.components.hierarchicalclustering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
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
	static private Log log = LogFactory.getLog(HierClusterDisplay.class);
	
	/**
     * Value for height of marker in pixels
     */
    static int geneHeight = 5;

    /**
     * Value for width of marker in pixels
     */
    static int geneWidth = 20;

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
     * Insensity of the markers painted set from the intensity slider
     */
    private double intensity = 1.0;

    /**
     * Keeps track of the positions where the markers are drawn
     */
    private Map<Rectangle, DSGeneMarker> markerPositions = new HashMap<Rectangle, DSGeneMarker>();

    /**
     * Default Constructor
     */
    public HierClusterDisplay() {
        setBackground(Color.white);
        setLayout(new BorderLayout());
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
	 * @param g
	 *            Graphics used for painting
	 */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (microarraySet == null) {
			log.info("null microarraySet");
			return;
		}
		markerPositions.clear();

		int fontGutter = (int) ((double) geneHeight * .22);
		int geneNo = 0;

		if (currentMarkerCluster == null) {
			geneNo = microarraySet.markers().size();
		} else {
			geneNo = leafMarkers.length;
		}

		int chipNo = 0;

		if (currentArrayCluster == null) {
			chipNo = microarraySet.items().size();
		} else {
			chipNo = leafArrays.length;
		}

		ColorContext colorCtx = null;

		if (microarraySet.getDataSet() instanceof DSMicroarraySet) {
			colorCtx = (org.geworkbench.bison.util.colorcontext.ColorContext) ((DSMicroarraySet) microarraySet
					.getDataSet())
					.getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
		} else {
			colorCtx = new org.geworkbench.bison.util.colorcontext.DefaultColorContext();
		}

		for (int i = 0; i < geneNo; i++) {
			DSGeneMarker stats = null;

			if (leafMarkers != null) {
				stats = ((MarkerHierCluster) leafMarkers[i]).getMarkerInfo();
			} else {
				stats = microarraySet.markers().get(i);
			}

			int y = i * geneHeight;

			for (int j = 0; j < chipNo; j++) {
				DSMicroarray mArray = null;

				if (leafArrays != null) {
					mArray = ((MicroarrayHierCluster) leafArrays[j])
							.getMicroarray();
				} else {
					mArray = microarraySet.get(j);
				}

				int x = (j * geneWidth);
				int width = ((j + 1) * geneWidth) - x;
				DSMarkerValue marker = mArray.getMarkerValue(stats);

				Color color = colorCtx.getMarkerValueColor(marker, stats,
						(float) intensity);
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
			Rectangle rectangle = new Rectangle(0, y, microarraySet.items().size() * geneWidth, geneHeight);
			markerPositions.put(rectangle, stats);
			if (accession == null) {
				accession = "Undefined";
			}
			String drawMe = accession
					+ (geneName != null && accession.compareTo(geneName) != 0 ? " ("
							+ geneName + ")"
							: "");
			g.drawString(drawMe, xRatio, yRatio);
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
    	return markerPositions.get(new Rectangle(x, y));
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
        final int maxFontSize = 10;
        final int minFontSize = 5;
        
    	int fontSize = geneHeight;
    	if(fontSize<minFontSize) fontSize = minFontSize;
        if(fontSize>maxFontSize) fontSize = maxFontSize;

        if ((fontSize != this.fontSize) || (labelFont == null)) {
            this.fontSize = fontSize;
            labelFont = new Font("Times New Roman", Font.PLAIN, this.fontSize);
        }
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
	 * 
	 * Clear old variables. 
	 * This method will be called in hierClusterModelChange() in HierClusterViewWidget.java 
	 * when new data arrives.
	 */
	public void resetVariables(DSMicroarraySetView<DSGeneMarker, DSMicroarray> microrraySetView) {
		currentArrayCluster = null;
		currentMarkerCluster = null;
		leafArrays = null;
		leafMarkers = null;

        microarraySet = microrraySetView;
	}    
}
