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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class ScoreColourScheme
    extends ResidueColourScheme
{
  /** DOCUMENT ME!! */
  public double min;

  /** DOCUMENT ME!! */
  public double max;

  /** DOCUMENT ME!! */
  public double[] scores;

  /**
   * Creates a new ScoreColourScheme object.
   *
   * @param scores DOCUMENT ME!
   * @param min DOCUMENT ME!
   * @param max DOCUMENT ME!
   */
  public ScoreColourScheme(double[] scores, double min, double max)
  {
    super();

    this.scores = scores;
    this.min = min;
    this.max = max;

    // Make colours in constructor
    // Why wasn't this done earlier?
    int i, iSize = scores.length;
    colors = new Color[scores.length];
    for (i = 0; i < iSize; i++)
    {
      float red = (float) (scores[i] - (float) min) / (float) (max - min);

      if (red > 1.0f)
      {
        red = 1.0f;
      }

      if (red < 0.0f)
      {
        red = 0.0f;
      }
      colors[i] = makeColour(red);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param j DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Color findColour(char c, int j)
  {
    if (threshold > 0)
    {
      if (!aboveThreshold(c, j))
      {
        return Color.white;
      }
    }

    if (jalview.util.Comparison.isGap(c))
    {
      return Color.white;
    }

    Color currentColour = colors[ResidueProperties.aaIndex[c]];

    if (conservationColouring)
    {
      currentColour = applyConservation(currentColour, j);
    }

    return currentColour;
  }

  /**
   * DOCUMENT ME!
   *
   * @param c DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Color makeColour(float c)
  {
    return new Color(c, (float) 0.0, (float) 1.0 - c);
  }
}
