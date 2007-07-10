package org.geworkbench.components.mindy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Calculates the color gradient to show on the MINDY GUI.
 * @author John Watkinson
 * @version $ID$
 */
public class ColorGradient {

    static Log log = LogFactory.getLog(ColorGradient.class);

    /**
     * Represents a drawn color point.  
     * Includes both color and the float value to which the color is associated.
     * @author John Watkinson
     * @version $Id: ColorGradient.java,v 1.2 2007-07-10 20:02:54 hungc Exp $
     */
    public static class ColorPoint implements Comparable<ColorPoint> {

    	/** 
    	 * @param color - color of the point
    	 * @param point - float value of the point
    	 */
        public ColorPoint(Color color, float point) {
            this.color = color;
            this.point = point;
        }

        private Color color;
        private float point;

        /** 
         * @return A Color object representing the color of the color point.
         */
        public Color getColor() {
            return color;
        }

        /**
         * @return The float value of the color point.
         */
        public float getPoint() {
            return point;
        }

        /**
         * @param - A color point with which to compare
         * @return A negative integer if the color point precedes the one specified by the parameter.  
         * Zero if the two color points have the same float value.
         * A positive integer if the color point specified by the parameter takes precedence.
         */
        public int compareTo(ColorPoint o) {
            return Float.compare(point, o.point);
        }
    }

    private ArrayList<ColorPoint> points;

    /**
     * @param negOneColor - negative score color
     * @param posOneColor - positive score color
     */
    public ColorGradient(Color negOneColor, Color posOneColor) {
        points = new ArrayList<ColorPoint>();
        points.add(new ColorPoint(negOneColor, -1f));
        points.add(new ColorPoint(posOneColor, 1f));
    }

    /**
     * Add to the list of color points.
     * @param color
     * @param point
     */
    public void addColorPoint(Color color, float point) {
        points.add(new ColorPoint(color, point));
        Collections.sort(points);
    }

    /**
     * @return number of color points.
     */
    public int getNumberOfColorPoints() {
        return points.size();
    }

    /**
     * @param index
     * @return color point from the list of color points
     */
    public ColorPoint getColorPoint(int index) {
        return points.get(index);
    }

    /**
     * Calculates the color gradient of the float value.
     * @param point - float value data point
     * @return the color associated with the float data point
     */
    public Color getColor(float point) {
        ColorPoint cp = new ColorPoint(null, point);
        int index = Collections.binarySearch(points, cp);
        if (index >= 0) {
            return points.get(index).color;
        } else {
            index = (-index) - 1;
            ColorPoint right = null;
            ColorPoint left = null;
            try {
                right = points.get(index);
                left = points.get(index - 1);
            } catch (IndexOutOfBoundsException e) {
                // There appears to be a bug in Collections.binarySearch() as it can return collection.size() + 1 even
                // though the javadoc claims otherwise
                // log.info("Collections.binarySearch() returned size()+1, using last elements");
                left = points.get(points.size() - 2);
                right = points.get(points.size() - 1);
            }
            float rightWeight = point - left.point;
            float leftWeight = right.point - point;
            float totalWeight = leftWeight + rightWeight;
            leftWeight /= totalWeight;
            rightWeight /= totalWeight;
            int red = (int) (left.color.getRed() * leftWeight + right.color.getRed() * rightWeight);
            int green = (int) (left.color.getGreen() * leftWeight + right.color.getGreen() * rightWeight);
            int blue = (int) (left.color.getBlue() * leftWeight + right.color.getBlue() * rightWeight);
            return new Color(red, green, blue);
        }
    }
}
