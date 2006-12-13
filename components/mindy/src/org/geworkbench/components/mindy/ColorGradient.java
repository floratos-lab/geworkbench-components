package org.geworkbench.components.mindy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author John Watkinson
 */
public class ColorGradient {

    static Log log = LogFactory.getLog(ColorGradient.class);

    public static class ColorPoint implements Comparable<ColorPoint> {

        public ColorPoint(Color color, float point) {
            this.color = color;
            this.point = point;
        }

        private Color color;
        private float point;

        public Color getColor() {
            return color;
        }

        public float getPoint() {
            return point;
        }

        public int compareTo(ColorPoint o) {
            return Float.compare(point, o.point);
        }
    }

    private ArrayList<ColorPoint> points;

    public ColorGradient(Color negOneColor, Color posOneColor) {
        points = new ArrayList<ColorPoint>();
        points.add(new ColorPoint(negOneColor, -1f));
        points.add(new ColorPoint(posOneColor, 1f));
    }

    public void addColorPoint(Color color, float point) {
        points.add(new ColorPoint(color, point));
        Collections.sort(points);
    }

    public int getNumberOfColorPoints() {
        return points.size();
    }

    public ColorPoint getColorPoint(int index) {
        return points.get(index);
    }

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
