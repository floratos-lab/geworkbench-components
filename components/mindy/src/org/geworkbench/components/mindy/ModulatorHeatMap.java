package org.geworkbench.components.mindy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;

/**
 * @author mhall
 */
public class ModulatorHeatMap extends JPanel {

    private static Log log = LogFactory.getLog(ModulatorHeatMap.class);

    private static final int SPACER_TOP = 20;
    private static final int SPACER_SIDE = 20;
    private static final int BAR_HEIGHT = 12;
    private static final int PREFERRED_CELL_WIDTH = 6;

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
    private float[][] sortedValues;

    public ModulatorHeatMap(DSGeneMarker modulator, DSGeneMarker transcriptionFactor, MindyData mindyData) {
        this.maSet = mindyData.getArraySet();
        DSItemList<DSGeneMarker> markers = maSet.getMarkers();

        // Extract and sort set based on modulator
        sortedValues = new float[maSet.size()][];
        for (int i = 0; i < maSet.size(); i++) {
            float[] array = ((DSMicroarray) maSet.get(i)).getRawMarkerData();
            sortedValues[i] = new float[array.length];
            System.arraycopy(array, 0, sortedValues[i], 0, array.length);
        }

        Arrays.sort(sortedValues, new ArrayIndexComparator(modulator.getSerial(), true));
//        sortArrays(new ArrayIndexComparator(modulator.getSerial(), true));
        // Sort half sets based on trans factor
        ArrayList<float[]> firstHalf = new ArrayList<float[]>();
        ArrayList<float[]> secondHalf = new ArrayList<float[]>();
//        MicroarraySet firstHalf = new MicroarraySet("Half 1", "Half 1", "", markers);
//        MicroarraySet secondHalf = new MicroarraySet("Half 2", "Half 2", "", markers);
        int count = 0;
        for (float[] values : sortedValues) {
            if (count < sortedValues.length / 2) {
                firstHalf.add(values);
            } else {
                secondHalf.add(values);
            }
            count++;
        }
        Collections.sort(firstHalf, new ArrayIndexComparator(transcriptionFactor.getSerial(), true));
        Collections.sort(secondHalf, new ArrayIndexComparator(transcriptionFactor.getSerial(), true));

        count = 0;
        for (float[] values : firstHalf) {
            sortedValues[count] = values;
            count++;
        }
        for (float[] values : secondHalf) {
            sortedValues[count] = values;
            count++;
        }

        this.modulator = modulator;
        this.transcriptionFactor = transcriptionFactor;
        this.mindyData = mindyData;
        findMaxValues();

        gradient = new ColorGradient(Color.black, Color.yellow);
        gradient.addColorPoint(Color.red, 0f);

        FontRenderContext context = new FontRenderContext(null, true, false);
        for (DSGeneMarker marker : markers) {
            String shortName = marker.getShortName().trim();
            if (shortName.length() > MAX_MARKER_NAME_CHARS) {
                shortName = shortName.substring(0, MAX_MARKER_NAME_CHARS) + " ... ";
            }
            Rectangle2D bounds = BASE_FONT.getStringBounds(shortName, context);
            if (bounds.getWidth() > maxGeneNameWidth) {
                maxGeneNameWidth = (int) bounds.getWidth() + 1;
            }
        }
        log.debug("Max gene name width: " + maxGeneNameWidth);

        this.setBackground(Color.white);
        this.setOpaque(true);
    }

    public void paint(Graphics graphics) {
        doPaint(graphics, false);
    }

    public void print(Graphics graphics) {
        doPaint(graphics, true);
    }

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
        String modulatorName = modulator.getShortName();
        int modNameWidth = metrics.stringWidth(modulatorName + "+");
        g.drawString(modulatorName.trim()+"-", SPACER_SIDE, SPACER_TOP);
        g.drawString(modulatorName.trim()+"+", (int) (getWidth() - modNameWidth - SPACER_SIDE + 1), SPACER_TOP);
        int modBarTopY = SPACER_TOP + metrics.getDescent() + 1;
        int modBarStartX = SPACER_TOP;
        int modBarEndX = getWidth() - SPACER_TOP;
        int n = gradient.getNumberOfColorPoints();
        int barWidth = modBarEndX - modBarStartX;
        float px = modBarStartX;
        float lastX = modBarStartX;
        ColorGradient.ColorPoint lastPoint = null;
        for (int i = 0; i < n; i++) {
            ColorGradient.ColorPoint cPoint = gradient.getColorPoint(i);
            if (lastPoint != null) {
                float width = barWidth * (cPoint.getPoint() - lastPoint.getPoint()) / 2;
                px += width;
                GradientPaint paint = new GradientPaint(lastX, modBarTopY, lastPoint.getColor(), px, modBarTopY, cPoint.getColor());
                g.setPaint(paint);
//                g.setColor(Color.RED);
                g.fill(new Rectangle2D.Double(lastX, modBarTopY, width, BAR_HEIGHT));
            }
            lastPoint = cPoint;
            lastX = px;
        }
        g.setColor(Color.GRAY);
        g.drawRect(modBarStartX, modBarTopY, barWidth, BAR_HEIGHT);


        // Some variables useful for the next two sections of painting
        float thirdOfWidth = (getWidth() - (2 * SPACER_SIDE)) / 3f;
        float cellWidth = thirdOfWidth / (maSet.size() / 2f);
        int transFacStartY = modBarTopY + BAR_HEIGHT + SPACER_TOP;

        // Paint the two transcription factor gradients

        // TransFac headers
        g.setColor(COLOR_TEXT);
        String transFacName = transcriptionFactor.getShortName().trim();
        int transFacNameWidth = metrics.stringWidth(transFacName + "+");
        // Left Side
        g.drawString(transFacName + "-", SPACER_SIDE, transFacStartY);
        g.drawString(transFacName + "+", SPACER_SIDE + thirdOfWidth - transFacNameWidth + 1, transFacStartY);
        // Right Side
        g.drawString(transFacName + "-", getWidth() - SPACER_SIDE - thirdOfWidth, transFacStartY);
        g.drawString(transFacName + "+", getWidth() - SPACER_SIDE - transFacNameWidth + 1, transFacStartY);
        int transFacBarY = transFacStartY + metrics.getDescent() + 1;

        paintExpressionBar(cellWidth, thirdOfWidth, g, transFacBarY, transcriptionFactor);
        // Outlines for trans fac gradients
        g.setColor(Color.GRAY);
        g.drawRect(SPACER_SIDE, transFacBarY, (int) thirdOfWidth, BAR_HEIGHT);
        g.drawRect((int) (SPACER_SIDE + (2 * thirdOfWidth)), transFacBarY, (int) thirdOfWidth, BAR_HEIGHT);

        // Draw the target's expression values
        int targetStartY = transFacBarY + BAR_HEIGHT + 5;
        int targetCurrY = targetStartY;
        DSItemList<DSGeneMarker> markers = maSet.getMarkers();
        int numMarkers = markers.size();
        for (int i = 0; i < numMarkers; i++) {
            // Only paint clipping area unless this is a print request
            if (print || (targetCurrY > clip.y - 10 && targetCurrY < clip.y + clip.height + 10)) {
                DSGeneMarker marker = markers.get(i);
                paintExpressionBar(cellWidth, thirdOfWidth, g, targetCurrY, marker);
                String targetName = marker.getShortName().trim();
                if (targetName.length() > MAX_MARKER_NAME_CHARS) {
                    targetName = targetName.substring(0, MAX_MARKER_NAME_CHARS) + " ... ";
                }

                int targetNameWidth = metrics.stringWidth(targetName + "+");
                g.setColor(COLOR_TEXT);

                g.drawString(targetName, getWidth() / 2 - (targetNameWidth / 2), targetCurrY + BAR_HEIGHT - 1);
            }
            targetCurrY += BAR_HEIGHT;
        }
        // Outlines for target gradients
        g.setColor(Color.GRAY);
        g.drawRect(SPACER_SIDE, targetStartY, (int) thirdOfWidth, targetCurrY - targetStartY);
        g.drawRect((int) (SPACER_SIDE + (2 * thirdOfWidth)), targetStartY, (int) thirdOfWidth, targetCurrY - targetStartY);

    }

    private void paintExpressionBar(float cellWidth, float thirdOfWidth, Graphics2D g, int y, DSGeneMarker markerToPaint) {
        int halfArrays = maSet.size() / 2;
        for (int i = 0; i < maSet.size(); i++) {
            float thisValue = (float) sortedValues[i][markerToPaint.getSerial()];
            int startX = SPACER_SIDE;
            if (i < halfArrays) {
                startX += (int) (i * cellWidth);
            } else {
                startX += 2 * thirdOfWidth + ((i - halfArrays) * cellWidth);

            }
            g.setColor(gradient.getColor((thisValue - minValue) / valueRange));
            g.fillRect(startX, y, (int) (cellWidth + 1), BAR_HEIGHT);
        }
    }

    private void findMaxValues() {
        maxValue = Float.NEGATIVE_INFINITY;
        minValue = Float.POSITIVE_INFINITY;
        for (int i = 0; i < maSet.size(); i++) {
            float[] value = sortedValues[i];
            for (int j = 0; j < value.length; j++) {
                if (value[j] > maxValue) {
                    maxValue = value[j];
                }
                if (value[j] < minValue) {
                    minValue = value[j];
                }
            }
        }
        if (maxValue == minValue) {
            // Avoid div by zero in this degenerate case
            maxValue += 1e-9f;
        }
        valueRange = maxValue - minValue;
    }

    public Dimension getPreferredSize() {
        int preferredWidth = (int) ((PREFERRED_CELL_WIDTH * maSet.size()) * 1.5 + (2 * SPACER_SIDE));
        if (preferredWidth < 3 * maxGeneNameWidth) {
            // This means there are probably a small number of arrays, so the calculated width is going to be small
            preferredWidth = 3 * maxGeneNameWidth + (2 * SPACER_SIDE);
        }
        int preferredHeight = (maSet.getMarkers().size() * BAR_HEIGHT) + (6 * BAR_HEIGHT) + (2 * SPACER_TOP);
        return new Dimension(preferredWidth, preferredHeight);
    }

    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
}
