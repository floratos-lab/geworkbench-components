package org.geworkbench.components.mindy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;
import java.util.*;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMutableMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.bison.util.colorcontext.*;
import org.apache.commons.math.stat.regression.*;

/**
 * Creates a heat map of selected modulator, transcription factor, and targets.
 * 
 * @author mhall
 * @author ch2514
 * @version $ID$
 */
@SuppressWarnings("serial")
public class ModulatorHeatMap extends JPanel {

    private static Log log = LogFactory.getLog(ModulatorHeatMap.class);
    
    private static final String LEFT_LABEL = "Lowest ";
    private static final String RIGHT_LABEL = "Highest ";
    private static final String PERCENT = "%";

    private static final int SPACER_TOP = 20;
    private static final int SPACER_SIDE = 20;
    private static final int BAR_HEIGHT = 12;
    private static final int PREFERRED_CELL_WIDTH = 3;

    public static final int MAX_MARKER_NAME_CHARS = 30;

    public static final Font BASE_FONT = new Font("SansSerif", Font.BOLD, 12);
    public static final Color COLOR_TEXT = new Color(0.2f, 0.2f, 0.2f);

    private DSMicroarraySet maSet;
    private DSGeneMarker modulator;
    private DSGeneMarker transcriptionFactor;
    private float setFractionPercent;
    private MindyData mindyData;

    private int maxGeneNameWidth = -1;
    private java.util.List<MindyData.MindyResultRow> targetRows;
    private List<DSGeneMarker> targetLimits;
    
    private ColorContext colorContext = null;
    private ArrayList<DSMicroarray> sortedPerMod = null;
    private ArrayList<DSMicroarray> half1 = null;
    private ArrayList<DSMicroarray> half2 = null;
    
    private boolean showProbeName = false;

    /**
     * Constructor.
     * 
     * @param modulator - MINDY modulator
     * @param transcriptionFactor - MINDY transcription factor
     * @param mindyData - MINDY data
     * @param targetLimits - list of targets
     */
    @SuppressWarnings("unchecked")
    public ModulatorHeatMap(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, MindyData mindyData, List<DSGeneMarker> targetLimits) {
    	this.showProbeName = !mindyData.isAnnotated();
    	log.debug("\tHeatMap::constructor::start::" + System.currentTimeMillis());
    	this.maSet = mindyData.getArraySet();
        List<DSGeneMarker> markers = mindyData.getTargets(modulator, transcriptionFactor);
        this.colorContext = (ColorContext) maSet.getObject(ColorContext.class);
        
        this.modulator = modulator;
        this.transcriptionFactor = transcriptionFactor;
        this.mindyData = mindyData;
        this.setFractionPercent = mindyData.getSetFraction() * 100;
        
        // Extract and sort set based on modulator        
        sortedPerMod = mindyData.getArraySetAsList();
        Collections.sort(sortedPerMod, new MicroarrayMarkerPositionComparator(modulator.getSerial()
        		, MicroarrayMarkerPositionComparator.EXPRESSION_VALUE
        		,  true));
        
        // Sort half sets based on trans factor
        int size = sortedPerMod.size()/2;
        // For odd number of arrays, cut out the array in the middle (i.e. the overlapping array)
        // -1 means even number of arrays
        int oddNumberCutout = -1;
        if((sortedPerMod.size() % 2) != 0){
        	oddNumberCutout = (int) sortedPerMod.size()/2;
        }
        // stop index for the L- array
        int stopIndex = (int) Math.round(size * this.mindyData.getSetFraction() * 2);
        if(stopIndex > size) stopIndex = size;
        // start index for the L+ array 
        int startIndex = sortedPerMod.size() - ((int) Math.round(size * this.mindyData.getSetFraction() * 2));
        if(startIndex < 0) startIndex = 0;
        half1 = new ArrayList<DSMicroarray>(stopIndex);
        half2 = new ArrayList<DSMicroarray>(stopIndex); 
        int count = 0;
        for(DSMicroarray ma : sortedPerMod){
        	if(count < size){
        		if((count != oddNumberCutout) && (count < stopIndex)){
        			half1.add(ma);
        		}
        	} else {
        		if((count != oddNumberCutout) && (count >= startIndex)){
        			half2.add(ma);
        		}
        	}
        	count++;
        }
        half1.trimToSize();
        half2.trimToSize();
        Collections.sort(half1, new MicroarrayMarkerPositionComparator(transcriptionFactor.getSerial()
        		, MicroarrayMarkerPositionComparator.EXPRESSION_VALUE
        		, true));
        Collections.sort(half2, new MicroarrayMarkerPositionComparator(transcriptionFactor.getSerial()
        		, MicroarrayMarkerPositionComparator.EXPRESSION_VALUE
        		, true));
        limitTargets(targetLimits);        
        Collections.sort(targetRows, new MindyRowComparator(MindyRowComparator.PEARSON_CORRELATION, true));  

        FontRenderContext context = new FontRenderContext(null, true, false);
        for (DSGeneMarker marker : markers) {
        	String shortName = this.getMarkerDisplayName(marker);
            Rectangle2D bounds = BASE_FONT.getStringBounds(shortName, context);
            if (bounds.getWidth() > maxGeneNameWidth) {
                maxGeneNameWidth = (int) bounds.getWidth() + 1;
            }
        }
        log.debug("Max gene name width: " + maxGeneNameWidth);

        this.setBackground(Color.white);
        this.setOpaque(true);
        log.debug("\tHeatMap::constructor::end::" + System.currentTimeMillis());
    }

    private void limitTargets(List<DSGeneMarker> targetLimits) {
        if (targetLimits != null) {
        	this.targetLimits = targetLimits;
            targetRows = mindyData.getRows(modulator, transcriptionFactor, targetLimits);
        } else {
        	this.targetLimits = null;
            targetRows = mindyData.getRows(modulator, transcriptionFactor);
        }
        invalidate();
    }

    /**
     * Paint the heat map graphics object.
     * @param graphics - the graphics object representing the heat map.
     */
    public void paint(Graphics graphics) {
        doPaint(graphics, false);
    }

    /**
     * Print the heat map graphics object.
     * @param graphics - the graphics object representing the heat map.
     */
    public void print(Graphics graphics) {
        doPaint(graphics, true);
    }

    /**
     * Paint the heat map graphics object.
     * @param graphics - the graphics object representing the heat map.
     * @param print - true if this graphics object is to be printed.
     */
    public void doPaint(Graphics graphics, boolean print) {
        super.paint(graphics);

        Graphics2D g = (Graphics2D) graphics;
        Rectangle clip = g.getClipBounds();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(BASE_FONT);

        g.setBackground(Color.WHITE);

        // Draw the modulator expression line
        g.setColor(COLOR_TEXT);
        FontMetrics metrics = g.getFontMetrics();
        String modulatorName = this.getMarkerDisplayName(modulator);
        int modNameWidth = metrics.stringWidth(modulatorName);
        g.drawString(modulatorName, (getWidth() / 2) - (modNameWidth / 2), SPACER_TOP);
        int modBarTopY = SPACER_TOP + metrics.getDescent() + 1;
        int modBarStartX = SPACER_TOP;
        int modBarEndX = getWidth() - SPACER_TOP;
        int barWidth = modBarEndX - modBarStartX;

        // Some variables useful for the next two sections of painting
        float expressionBarWidth = (getWidth() - (2 * SPACER_SIDE) - (2 * SPACER_SIDE + maxGeneNameWidth)) / 2f;
        float cellWidth = expressionBarWidth / ((this.half1.size() + this.half2.size()) / 2f);
        int transFacStartY = modBarTopY + BAR_HEIGHT + SPACER_TOP;

        // Draw the two transcription factor gradients
        // TransFac headers
        g.setColor(COLOR_TEXT);
        String transFacName = this.getMarkerDisplayName(transcriptionFactor).trim();
        int transFacNameWidth = metrics.stringWidth(transFacName);
        // Left Side (marker name)
        g.drawString(transFacName, SPACER_SIDE + (expressionBarWidth - transFacNameWidth)/2 + 1, transFacStartY);        
        // Right Side (marker name)
        g.drawString(transFacName
        		, Math.round((getWidth() - SPACER_SIDE - expressionBarWidth - 1) 
                		+ (expressionBarWidth + 1)/2 - transFacNameWidth/2)        		
        		, transFacStartY);
        
        int transFacBarY = transFacStartY + metrics.getDescent() + 1;

        // Drawing line under modulator name
        int x1 = Math.round(SPACER_SIDE + (expressionBarWidth - transFacNameWidth)/2 + 1 + transFacNameWidth/2);
        int y1 = modBarTopY;
        int x2 = Math.round(
        		(getWidth() - SPACER_SIDE - expressionBarWidth - 1) 
        		+ (expressionBarWidth + 1)/2
        		);
        int y2 = y1 + transFacBarY/4;
        g.setColor(COLOR_TEXT);
        g.drawLine(x1, y1, x2, y1);		// top line
        g.drawLine(x1, y1, x1, y2);		// left vertical line
        g.drawLine(x2, y1, x2, y2);		// right vertical line
        
        // Label two ends of the line
        g.drawString(LEFT_LABEL + this.setFractionPercent + PERCENT, x1, SPACER_TOP);
        String s = RIGHT_LABEL + this.setFractionPercent+ PERCENT;
        g.drawString(s, x2 - metrics.stringWidth(s), SPACER_TOP);
        
        // Outlines for trans fac gradients (the triangles)
        g.setColor(COLOR_TEXT);
        int xx1 = SPACER_SIDE;
        int yy1 = transFacBarY;
        int xx2 = xx1 + (int) expressionBarWidth;
        int yy2 = yy1 + BAR_HEIGHT;
        g.drawLine(xx1, yy2, xx2, yy1);				// left triangle:	diagonal
        g.drawLine(xx2, yy1, xx2, yy2);				//					vertical
        g.drawLine(xx1, yy2, xx2, yy2);				//					horizontal
        xx1 = (int) (getWidth() - SPACER_SIDE - expressionBarWidth - 1);
        xx2 = xx1 + (int) (expressionBarWidth + 1);
        g.drawLine(xx1, yy2, xx2, yy1);				// right triangle:	diagonal
        g.drawLine(xx2, yy1, xx2, yy2);				//					vertical
        g.drawLine(xx1, yy2, xx2, yy2);				//					horizontal
        
        // Draw the target's expression values
        int targetStartY = transFacBarY + BAR_HEIGHT + 5;
        int targetCurrY = targetStartY;

        for (int i = 0; i < targetRows.size(); i++) {
            // Only paint clipping area unless this is a print request
            if (print || (targetCurrY > clip.y - 10 && targetCurrY < clip.y + clip.height + 10)) {
                MindyData.MindyResultRow mindyRow = targetRows.get(i);
                DSGeneMarker target = mindyRow.getTarget();
                paintExpressionBar(cellWidth, expressionBarWidth, g, targetCurrY, target);
                String targetName = this.getMarkerDisplayName(target);

                int targetNameWidth = metrics.stringWidth(targetName);
                g.setColor(COLOR_TEXT);

                g.drawString(targetName, (getWidth() / 2) - (targetNameWidth / 2), targetCurrY + BAR_HEIGHT - 1);
            }
            targetCurrY += BAR_HEIGHT;
        }
        // Outlines for target gradients
        g.setColor(Color.GRAY);
        g.drawRect(SPACER_SIDE, targetStartY, (int) expressionBarWidth, targetCurrY - targetStartY);
        g.drawRect((int) (getWidth() - SPACER_SIDE - expressionBarWidth - 1), targetStartY, (int) (expressionBarWidth + 1), targetCurrY - targetStartY);
        
    }

    private void paintExpressionBar(float cellWidth, float expressionBarWidth, Graphics2D g, int y, DSGeneMarker markerToPaint) {
    	int size = this.half1.size() + this.half2.size();
        int halfArrays = size / 2;
        for (int i = 0; i < size; i++) {
            int startX;
            DSMutableMarkerValue value = null;
            if (i < halfArrays) {
                startX = SPACER_SIDE + (int) (i * cellWidth);
                value = ((DSMicroarray) this.half1.get(i)).getMarkerValue(markerToPaint);
            } else {
                startX = (int) (getWidth() - SPACER_SIDE - expressionBarWidth + ((i - halfArrays) * cellWidth));
                value = ((DSMicroarray) this.half2.get(i - halfArrays)).getMarkerValue(markerToPaint);
            }
            g.setColor(colorContext.getMarkerValueColor(value, markerToPaint, 1.0f));
            g.fillRect(startX, y, (int) (cellWidth + 1), BAR_HEIGHT);
        }
    }

    /**
     * Get the preferred size of the heat map.
     * @return A Dimension object representing the preferred size of the heat map.
     */
    public Dimension getPreferredSize() {
    	int preferredWidth = (int) ((PREFERRED_CELL_WIDTH * this.sortedPerMod.size()) * 1.5 + (2 * SPACER_SIDE));
        if (preferredWidth < 3 * maxGeneNameWidth) {
            // This means there are probably a small number of arrays, so the calculated width is going to be small
            preferredWidth = 3 * maxGeneNameWidth + (2 * SPACER_SIDE);
        }
        int preferredHeight = (targetRows.size() * BAR_HEIGHT) + (6 * BAR_HEIGHT) + (2 * SPACER_TOP);
        return new Dimension(preferredWidth, preferredHeight);
    }

    /**
     * Get the minimum size of the heat map.
     * @return A Dimension object representing the minimum size of the heat map.
     */
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    /**
     * Check to see if the heat map should display probe names or gene names.
     * @return If true, the heat map displays probe names.  
     * If not, the map displays gene names.
     */
    public boolean isShowProbeName(){
    	return this.showProbeName;
    }
    
    /**
     * Specify whether or not the heat map should display probe names or gene names.
     * @param showProbeName - if true, the heat map displays probe names.  
     * If not, the map displays gene names.
     */
    public void setShowProbeName(boolean showProbeName){
    	this.showProbeName = showProbeName;
    }
    
    /**
     * Specifies the marker name (probe name vs. gene name) to display on the heat map.
     * @param marker - gene marker
     * @return The marker name (probe vs. gene) to display on the heat map.
     */
    public String getMarkerDisplayName(DSGeneMarker marker){
    	String result = marker.getGeneName();
    	if(this.showProbeName){
    		result = marker.getLabel();
    	}
    	return result;
    }
    
    /**
     * Get the list of limited targets currently displaying on this heat map.
     * 
     * @return list of limited targets.  If all markers are shown, return null.
     */
    public List<DSGeneMarker> getTargetLimits(){
    	return this.targetLimits;
    }
    
    public void setTargetLimits(List<DSGeneMarker> targetLimits){
    	this.targetLimits = targetLimits;
    }
}
