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
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyData;
import org.geworkbench.bison.util.colorcontext.*;

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
    private MindyData mindyData;

    private ColorGradient gradient;
    private float maxValue;
    private float minValue;
    private float valueRange;
    private int maxGeneNameWidth = -1;
    private java.util.List<MindyData.MindyResultRow> targetRows;
    private List<DSGeneMarker> targetLimits;
    
    private ColorContext colorContext = null;
    private ArrayList<DSMicroarray> sortedPerMod = null;
    private ArrayList<DSMicroarray> sortedPerTF = null;
    
    private boolean showProbeName = true;

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
        this.maSet = mindyData.getArraySet();
        List<DSGeneMarker> markers = mindyData.getTargets(modulator, transcriptionFactor);
        this.colorContext = (ColorContext) maSet.getObject(ColorContext.class);
        
        this.modulator = modulator;
        this.transcriptionFactor = transcriptionFactor;
        this.mindyData = mindyData;
        
        // Extract and sort set based on modulator
        sortedPerMod = new ArrayList<DSMicroarray>(maSet.size());
        for(int i = 0; i < maSet.size(); i++){
        	sortedPerMod.add((DSMicroarray) maSet.get(i)); 
        }
        sortedPerMod.trimToSize();
        Collections.sort(sortedPerMod, new MicroarrayMarkerPositionComparator(modulator.getSerial(), true));
        
        
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
        ArrayList<DSMicroarray> half1 = new ArrayList<DSMicroarray>(stopIndex);
        ArrayList<DSMicroarray> half2 = new ArrayList<DSMicroarray>(stopIndex); 
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
        Collections.sort(half1, new MicroarrayMarkerPositionComparator(transcriptionFactor.getSerial(), true));
        Collections.sort(half2, new MicroarrayMarkerPositionComparator(transcriptionFactor.getSerial(), true));
        
        sortedPerTF = new ArrayList<DSMicroarray>(half1.size() + half2.size());
        for(int i = 0; i < half1.size(); i++){
        	this.sortedPerTF.add((DSMicroarray) half1.get(i));
        }
        for(int i = 0; i < half2.size(); i++){
        	this.sortedPerTF.add((DSMicroarray) half2.get(i));
        }
        this.sortedPerTF.trimToSize();

        limitTargets(targetLimits);

        gradient = new ColorGradient(Color.blue, Color.red);
        gradient.addColorPoint(Color.white, 0f);

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
    }

    private void limitTargets(List<DSGeneMarker> targetLimits) {
        if (targetLimits != null) {
        	this.targetLimits = targetLimits;
            targetRows = mindyData.getRows(modulator, transcriptionFactor, targetLimits);
        } else {
        	this.targetLimits = null;
            targetRows = mindyData.getRows(modulator, transcriptionFactor);
        }
        findMaxValues();
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

        // Paint the modulator expression bar
        g.setColor(COLOR_TEXT);
        FontMetrics metrics = g.getFontMetrics();
        String modulatorName = this.getMarkerDisplayName(modulator);
        int modNameWidth = metrics.stringWidth(modulatorName + "+");
        g.drawString(modulatorName.trim()+"-", SPACER_SIDE, SPACER_TOP);
        g.drawString(modulatorName.trim()+"+", (int) (getWidth() - modNameWidth - SPACER_SIDE + 1), SPACER_TOP);
        int modBarTopY = SPACER_TOP + metrics.getDescent() + 1;
        int modBarStartX = SPACER_TOP;
        int modBarEndX = getWidth() - SPACER_TOP;
        int barWidth = modBarEndX - modBarStartX;
        int numArrays = this.sortedPerMod.size();
        float modCellWidth = barWidth / (float) numArrays;
        for (int i = 0; i < numArrays; i++) {
            int x = (int) (modBarStartX + (i * modCellWidth));
            Color cellColor = colorContext.getMarkerValueColor(((DSMicroarray) this.sortedPerMod.get(i)).getMarkerValue(modulator), modulator, 1.0f);         
            g.setColor(cellColor);
            g.fillRect(x, modBarTopY, (int) (modCellWidth + 1), BAR_HEIGHT);
        }

        g.setColor(Color.GRAY);
        g.drawRect(modBarStartX, modBarTopY, barWidth, BAR_HEIGHT);


        // Some variables useful for the next two sections of painting
        float expressionBarWidth = (getWidth() - (2 * SPACER_SIDE) - (2 * SPACER_SIDE + maxGeneNameWidth)) / 2f;
        float cellWidth = expressionBarWidth / (this.sortedPerTF.size() / 2f);
        int transFacStartY = modBarTopY + BAR_HEIGHT + SPACER_TOP;

        // Paint the two transcription factor gradients

        // TransFac headers
        g.setColor(COLOR_TEXT);
        String transFacName = this.getMarkerDisplayName(transcriptionFactor).trim();
        int transFacNameWidth = metrics.stringWidth(transFacName + "+");
        // Left Side
        g.drawString(transFacName + "-", SPACER_SIDE, transFacStartY);
        g.drawString(transFacName + "+", SPACER_SIDE + expressionBarWidth - transFacNameWidth + 1, transFacStartY);
        // Right Side
        g.drawString(transFacName + "-", getWidth() - SPACER_SIDE - expressionBarWidth, transFacStartY);
        g.drawString(transFacName + "+", getWidth() - SPACER_SIDE - transFacNameWidth + 1, transFacStartY);
        int transFacBarY = transFacStartY + metrics.getDescent() + 1;

        paintExpressionBar(cellWidth, expressionBarWidth, g, transFacBarY, transcriptionFactor);
        // Outlines for trans fac gradients
        g.setColor(Color.GRAY);
        g.drawRect(SPACER_SIDE, transFacBarY, (int) expressionBarWidth, BAR_HEIGHT);
        g.drawRect((int) (getWidth() - SPACER_SIDE - expressionBarWidth - 1), transFacBarY, (int) (expressionBarWidth + 1), BAR_HEIGHT);

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
        int halfArrays = this.sortedPerTF.size() / 2;
        for (int i = 0; i < this.sortedPerTF.size(); i++) {
            int startX;
            if (i < halfArrays) {
                startX = SPACER_SIDE + (int) (i * cellWidth);
            } else {
                startX = (int) (getWidth() - SPACER_SIDE - expressionBarWidth + ((i - halfArrays) * cellWidth));
            }
            Color expressionColor = colorContext.getMarkerValueColor(((DSMicroarray) this.sortedPerTF.get(i)).getMarkerValue(markerToPaint), markerToPaint, 1.0f);
            g.setColor(expressionColor);
            g.fillRect(startX, y, (int) (cellWidth + 1), BAR_HEIGHT);
        }
    }

    /*
     * Find's the min and max expression values for the modulator, trans factor and targets
     */
    private void findMaxValues() {
        maxValue = Float.NEGATIVE_INFINITY;
        minValue = Float.POSITIVE_INFINITY;
        for (MindyData.MindyResultRow mindyResultRow : targetRows) {
            findMinMaxForMarker(mindyResultRow.getTarget().getSerial());
        }
        findMinMaxForMarker(modulator.getSerial());
        findMinMaxForMarker(transcriptionFactor.getSerial());
        if (maxValue == minValue) {
            // Avoid div by zero in this degenerate case
            maxValue += 1e-9f;
        }
        valueRange = maxValue - minValue;
    }

    private void findMinMaxForMarker(int index) {
    	for (int i = 0; i < this.sortedPerMod.size(); i++) {
    		float value = ((DSMicroarray) this.sortedPerMod.get(i)).getRawMarkerData()[index];
            if (value > maxValue) {
                maxValue = value;
            }
            if (value < minValue) {
                minValue = value;
            }
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
