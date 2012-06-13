package org.geworkbench.components.microarrays;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

/**
 * <p>Title: Plug And Play Framework</p>
 * <p>Description: Architecture for enGenious Plug&Play</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: First Genetic Trust</p>
 *
 * @author Andrea Califano
 * @version $Id$
 */
public class MicroarrayDisplay extends JPanel {
	private static final long serialVersionUID = 8362707074970193407L;
	
	DSMicroarray microarray = null;
    
	private int dx = 0;
	private int dy = 0;
	private int wx = 0;
	private int wy = 0;
	private double scaleX = 1;
	private double scaleY = 1;
	private int cols = 0;
	private int rows = 0;
	private int selRow = -1;
	private int selCol = -1;
     
    private DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView;
    private int[] patternGenes = null;
    private char[] maskedGenes = null;
    private char[] graphedGenes = null;
    private float intensity = 1f;

    private DSItemList<DSGeneMarker> uniqueMarkers = null;
    
    public MicroarrayDisplay(final DSMicroarraySetView<DSGeneMarker, DSMicroarray> maSetView) {
    	this.maSetView = maSetView;
        this.setBorder(BorderFactory.createLoweredBevelBorder());
        this.setLayout(new BorderLayout());
        
        uniqueMarkers = maSetView.getUniqueMarkers();
    }

    void setMicroarray(DSMicroarray microarray) {

        this.microarray = microarray;
        if (microarray != null) {
            int maxMarkerNo = microarray.getMarkerNo();
            if (maxMarkerNo > 0) {
                patternGenes = new int[maxMarkerNo];
                maskedGenes = new char[maxMarkerNo];
                graphedGenes = new char[maxMarkerNo];
            }
        } else {
            patternGenes = null;
            maskedGenes = graphedGenes = null;
        }
    }

    void setMicroarraySetView(DSMicroarraySetView<DSGeneMarker, DSMicroarray> microarraySetView) {
        rows = 0;
        cols = 0;
    	maSetView = microarraySetView;
    	uniqueMarkers = maSetView.getUniqueMarkers();
        
    	DSMicroarraySet microarrays = microarraySetView.getMicroarraySet();
        if (microarrays != null) {
            int maxMarkerNo = microarrays.size();
            if (maxMarkerNo > 0) {
                rows = (int) Math.ceil(Math.sqrt((double) uniqueMarkers.size()));
                cols = rows;
                patternGenes = new int[maxMarkerNo];
                maskedGenes = new char[maxMarkerNo];
                graphedGenes = new char[maxMarkerNo];
            }
        } else {
            patternGenes = null;
            maskedGenes = graphedGenes = null;
        }
        if ((rows == 0) || (cols == 0)) {
            patternGenes = null;
            maskedGenes = null;
            graphedGenes = null;
        }
    }

    private void computeScale() {
        Rectangle r = this.getBounds();
        Insets i = this.getInsets();
        r.grow(-i.left, -i.top);
        scaleX = (double) r.width / (double) cols;
        scaleY = (double) r.height / (double) rows;
        dx = r.x;
        dy = r.y;
        wx = r.width;
        wy = r.height;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (microarray != null) {
            rows = (int) Math.ceil(Math.sqrt((double) uniqueMarkers.size()));
            cols = rows;
            computeScale();
            drawImage((Graphics2D) g);
            g.setColor(Color.black);
        }
    }

    public Image getCurrentImage() {
        Image image = new BufferedImage(wx - dx, wy - dy, BufferedImage.TYPE_INT_RGB);
        Graphics2D ig = (Graphics2D) image.getGraphics();
        ig.setColor(Color.white);
        ig.fillRect(0,0,wx-dx, wy-dy);

        drawImage(ig);

        return image;
    }

    private void drawImage(Graphics2D g) {
        int geneNo = uniqueMarkers.size();
        int geneId = 0;
        DSDataSet<DSMicroarray> maSet = maSetView.getDataSet();
        if (maSet != null) {
            org.geworkbench.bison.util.colorcontext.ColorContext colorContext = (org.geworkbench.bison.util.colorcontext.ColorContext) maSet.getObject(org.geworkbench.bison.util.colorcontext.ColorContext.class);
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if (geneId < geneNo) {
                        DSGeneMarker stats = uniqueMarkers.get(geneId);
                        //this place should get the right marker with the selecte markers or all markers


                        //this is the markerid of the whole microarryset
                        int markerId = stats.getSerial();
                        if (patternGenes != null  && markerId >= 0 && markerId < patternGenes.length) {
                            DSMutableMarkerValue spot = (DSMutableMarkerValue) microarray.getMarkerValue(stats.getSerial());

                            if ((spot == null) || (spot.isMissing())) {
                                g.setColor(Color.yellow);
                            } else if (spot.isMasked()) {
                                g.setColor(Color.white);
                            } else {
                                g.setColor(colorContext.getMarkerValueColor(spot, stats, intensity));
                            }
                            int x0 = dx + (int) ((double) col * scaleX);
                            int x1 = dx + (int) ((double) (col + 1) * scaleX);
                            int y0 = dy + (int) ((double) row * scaleY);
                            int y1 = dy + (int) ((double) (row + 1) * scaleY);

                            g.fillRect(x0, y0, x1 - x0, y1 - y0);

                            boolean needOutline = true;
                            if (patternGenes[markerId] > 0) {
                                g.setColor(Color.blue);
                            } else if (maskedGenes[markerId] > 0) {
                                g.setColor(Color.orange);
                            } else if (graphedGenes[geneId] == '1') {
                                g.setColor(Color.green);
                            } else {
                                needOutline = false;
                            }
                            if (needOutline) {
                                g.setStroke(new BasicStroke(3));
                                g.drawRect(x0, y0, x1 - x0 - 1, y1 - y0 - 1);
                                g.setStroke(new BasicStroke(1));
                            }

                            geneId++;
                        }
                    }
                }
            }
        }
    }

    int getGeneIdAndRubberBand(int x, int y) {
        int geneId = -1;
        if ((scaleX > 0) && (scaleY > 0)) {
            int row = (int) (y / scaleY);
            int col = (int) (x / scaleX);
            rubberBandBox(row, col);
            geneId = row * cols + col;
            if ((geneId < 0) || (geneId >= uniqueMarkers.size())) {
                return -1;
            }
        }
        return uniqueMarkers.get(geneId).getSerial();
    }

    void rubberBandBox(int row, int col) {
        int x0, x1, y0, y1;
        if ((col != selCol) || (row != selRow)) {
            Graphics g = getGraphics();
            if ((selCol != -1) && (selRow != -1)) {
                x0 = dx + (int) ((double) selCol * scaleX);
                x1 = dx + (int) ((double) (selCol + 1) * scaleX);
                y0 = dy + (int) ((double) selRow * scaleY);
                y1 = dy + (int) ((double) (selRow + 1) * scaleY);
                g.setXORMode(Color.black);
                g.setColor(Color.white);
                g.drawRect(x0, y0, x1 - x0, y1 - y0);
            }
            if ((col != -1) && (row != -1)) {
                x0 = dx + (int) ((double) col * scaleX);
                x1 = dx + (int) ((double) (col + 1) * scaleX);
                y0 = dy + (int) ((double) row * scaleY);
                y1 = dy + (int) ((double) (row + 1) * scaleY);
                g.setXORMode(Color.black);
                g.setColor(Color.white);
                g.drawRect(x0, y0, x1 - x0, y1 - y0);
            }
            selCol = col;
            selRow = row;
        }
    }

    public void graphGene(int geneId) {
        if (graphedGenes != null)
            graphedGenes[geneId] = '1';
    }

    public void ungraphGene(int geneId) {
        if (graphedGenes != null)
            graphedGenes[geneId] = '0';
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }
}
