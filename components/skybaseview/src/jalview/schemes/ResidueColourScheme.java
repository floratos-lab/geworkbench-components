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

import java.util.*;

import java.awt.*;

import jalview.analysis.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class ResidueColourScheme
    implements ColourSchemeI
{

  boolean conservationColouring = false;

  Color[] colors;
  int threshold = 0;

  /* Set when threshold colouring to either pid_gaps or pid_nogaps*/
  protected String ignoreGaps = AAFrequency.PID_GAPS;

  /** Consenus as a hashtable array */
  Hashtable[] consensus;

  /** Conservation string as a char array */
  char[] conservation;
  int conservationLength=0;

  /** DOCUMENT ME!! */
  int inc = 30;

  /**
   * Creates a new ResidueColourScheme object.
   *
   * @param colors DOCUMENT ME!
   * @param threshold DOCUMENT ME!
   */
  public ResidueColourScheme(Color[] colours, int threshold)
  {
    this.colors = colours;
    this.threshold = threshold;
  }

  /**
   * Creates a new ResidueColourScheme object.
   */
  public ResidueColourScheme()
  {
  }

  /**
   * Find a colour without an index in a sequence
   */
  public Color findColour(char c)
  {
    return colors[ResidueProperties.aaIndex[c]];
  }

  public Color findColour(char c, int j)
  {
    Color currentColour;

    if ( (threshold == 0) || aboveThreshold(c, j))
    {
      currentColour = colors[ResidueProperties.aaIndex[c]];
    }
    else
    {
      currentColour = Color.white;
    }

    if (conservationColouring)
    {
      currentColour = applyConservation(currentColour, j);
    }

    return currentColour;
  }

  /**
   * Get the percentage threshold for this colour scheme
   *
   * @return Returns the percentage threshold
   */
  public int getThreshold()
  {
    return threshold;
  }

  /**
   * DOCUMENT ME!
   *
   * @param ct DOCUMENT ME!
   */
  public void setThreshold(int ct, boolean ignoreGaps)
  {
    threshold = ct;
    if (ignoreGaps)
    {
      this.ignoreGaps = AAFrequency.PID_NOGAPS;
    }
    else
    {
      this.ignoreGaps = AAFrequency.PID_GAPS;
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
  public boolean aboveThreshold(char c, int j)
  {
    if ('a' <= c && c <= 'z')
    {
      // TO UPPERCASE !!!
      //Faster than toUpperCase
      c -= ('a' - 'A');
    }

    if (consensus == null || consensus.length<j || consensus[j] == null)
    {
      return false;
    }

    if ( ( ( (Integer) consensus[j].get(AAFrequency.MAXCOUNT)).intValue() != -1) &&
        consensus[j].contains(String.valueOf(c)))
    {
      if ( ( (Float) consensus[j].get(ignoreGaps)).floatValue() >= threshold)
      {
        return true;
      }
    }

    return false;
  }

  public boolean conservationApplied()
  {
    return conservationColouring;
  }

  public void setConservationInc(int i)
  {
    inc = i;
  }

  public int getConservationInc()
  {
    return inc;
  }

  /**
   * DOCUMENT ME!
   *
   * @param consensus DOCUMENT ME!
   */
  public void setConsensus(Hashtable[] consensus)
  {
    if (consensus == null)
    {
      return;
    }

    this.consensus = consensus;
  }

  public void setConservation(Conservation cons)
  {
    if (cons == null)
    {
      conservationColouring = false;
      conservation = null;
    }
    else
    {
      conservationColouring = true;
      int i, iSize = cons.getConsSequence().getLength();
      conservation = new char[iSize];
      for (i = 0; i < iSize; i++)
      {
        conservation[i] = cons.getConsSequence().getCharAt(i);
      }
      conservationLength = conservation.length;
    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param i DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */

  Color applyConservation(Color currentColour, int i)
  {

    if ((conservationLength>i) && (conservation[i] != '*') && (conservation[i] != '+'))
    {
      if ( jalview.util.Comparison.isGap(conservation[i]))
      {
        currentColour = Color.white;
      }
      else
      {
        float t = 11 - (conservation[i] - '0');
        if (t == 0)
        {
          return Color.white;
        }

        int red = currentColour.getRed();
        int green = currentColour.getGreen();
        int blue = currentColour.getBlue();

        int dr = 255 - red;
        int dg = 255 - green;
        int db = 255 - blue;

        dr *= t / 10f;
        dg *= t / 10f;
        db *= t / 10f;

        red += (inc / 20f) * dr;
        green += (inc / 20f) * dg;
        blue += (inc / 20f) * db;

        if (red > 255 || green > 255 || blue > 255)
        {
          currentColour = Color.white;
        }
        else
        {
          currentColour = new Color(red, green, blue);
        }
      }
    }
    return currentColour;
  }

}
