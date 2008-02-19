/*
 * Jalview - A Sequence Alignment Editor and Viewer
 * Copyright (C) 2007 AM Waterhouse, J Procter, G Barton, M Clamp, S Searle
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 */
package jalview.schemes;

import java.awt.*;

import jalview.datamodel.*;

public class AnnotationColourGradient
    extends ResidueColourScheme
{
  public static int NO_THRESHOLD = -1;
  public static int BELOW_THRESHOLD = 0;
  public static int ABOVE_THRESHOLD = 1;

  public AlignmentAnnotation annotation;
  int aboveAnnotationThreshold = -1;
  public boolean thresholdIsMinMax = false;

  GraphLine annotationThreshold;

  float r1, g1, b1, rr, gg, bb, dr, dg, db;

  ColourSchemeI colourScheme;

  public boolean predefinedColours = false;

  /**
   * Creates a new AnnotationColourGradient object.
   */
  public AnnotationColourGradient(AlignmentAnnotation annotation,
                                  ColourSchemeI originalColour,
                                  int aboveThreshold)
  {
    if (originalColour instanceof AnnotationColourGradient)
    {
      colourScheme = ( (AnnotationColourGradient) originalColour).colourScheme;
    }
    else
    {
      colourScheme = originalColour;
    }

    this.annotation = annotation;

    aboveAnnotationThreshold = aboveThreshold;

    if (aboveThreshold != NO_THRESHOLD && annotation.threshold != null)
    {
      annotationThreshold = annotation.threshold;
    }
  }

  /**
   * Creates a new AnnotationColourGradient object.
   */
  public AnnotationColourGradient(AlignmentAnnotation annotation,
                                  Color minColour, Color maxColour,
                                  int aboveThreshold)
  {
    this.annotation = annotation;

    aboveAnnotationThreshold = aboveThreshold;

    if (aboveThreshold != NO_THRESHOLD && annotation.threshold != null)
    {
      annotationThreshold = annotation.threshold;
    }

    r1 = minColour.getRed();
    g1 = minColour.getGreen();
    b1 = minColour.getBlue();

    rr = maxColour.getRed() - r1;
    gg = maxColour.getGreen() - g1;
    bb = maxColour.getBlue() - b1;
  }

  public String getAnnotation()
  {
    return annotation.label;
  }

  public int getAboveThreshold()
  {
    return aboveAnnotationThreshold;
  }

  public float getAnnotationThreshold()
  {
    if (annotationThreshold == null)
    {
      return 0;
    }
    else
    {
      return annotationThreshold.value;
    }
  }

  public ColourSchemeI getBaseColour()
  {
    return colourScheme;
  }

  public Color getMinColour()
  {
    return new Color( (int) r1, (int) g1, (int) b1);
  }

  public Color getMaxColour()
  {
    return new Color( (int) (r1 + rr), (int) (g1 + gg), (int) (b1 + bb));
  }

  /**
   * DOCUMENT ME!
   *
   * @param n DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Color findColour(char c)
  {
    return Color.red;
  }

  /**
   * DOCUMENT ME!
   *
   * @param n DOCUMENT ME!
   * @param j DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Color findColour(char c, int j)
  {
    Color currentColour = Color.white;

    if ( (threshold == 0) || aboveThreshold(c, j))
    {
      if (j < annotation.annotations.length
          && annotation.annotations[j] != null
          && !jalview.util.Comparison.isGap(c))
      {

       if (predefinedColours)
        {
          if(annotation.annotations[j].colour != null)
              return annotation.annotations[j].colour;
          else
            return currentColour;
        }

        if (aboveAnnotationThreshold == NO_THRESHOLD
            ||
            (annotationThreshold != null && aboveAnnotationThreshold == ABOVE_THRESHOLD &&
             annotation.annotations[j].value >= annotationThreshold.value)
            ||
            (annotationThreshold != null && aboveAnnotationThreshold == BELOW_THRESHOLD &&
             annotation.annotations[j].value <= annotationThreshold.value))
        {

          float range=1f;
          if (thresholdIsMinMax
              && annotation.threshold != null
              && aboveAnnotationThreshold == ABOVE_THRESHOLD
              && annotation.annotations[j].value>annotation.threshold.value)
          {
            range =
                (annotation.annotations[j].value - annotation.threshold.value) /
                (annotation.graphMax - annotation.threshold.value);
          }
          else if (thresholdIsMinMax
                   && annotation.threshold != null
                   && aboveAnnotationThreshold == BELOW_THRESHOLD
              &&  annotation.annotations[j].value > annotation.graphMin)
          {
            range =
                ( annotation.annotations[j].value - annotation.graphMin ) /
                (annotation.threshold.value - annotation.graphMin );
          }
          else
          {
            range = (annotation.annotations[j].value -
                     annotation.graphMin) /
                (annotation.graphMax - annotation.graphMin);
          }


          if (colourScheme != null)
          {
            currentColour = colourScheme.findColour(c, j);
          }
          else if (range != 0)
          {
            dr = rr * range   + r1;
            dg = gg * range   + g1;
            db = bb * range   + b1;

            currentColour = new Color( (int) dr, (int) dg, (int) db);
          }
        }
      }
    }

    if (conservationColouring)
    {
      currentColour = applyConservation(currentColour, j);
    }

    return currentColour;
  }
}
