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
public class NucleotideColourScheme
    extends ResidueColourScheme
{
  /**
   * Creates a new NucleotideColourScheme object.
   */
  public NucleotideColourScheme()
  {
    super(ResidueProperties.nucleotide, 0);
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
    // System.out.println("called"); log.debug
    return colors[ResidueProperties.nucleotideIndex[c]];
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
    Color currentColour;
    if ( (threshold == 0) || aboveThreshold(c, j))
    {
      try
      {
        currentColour = colors[ResidueProperties.nucleotideIndex[c]];
      }
      catch (Exception ex)
      {
        return Color.white;
      }
    }
    else
    {
      return Color.white;
    }

    if (conservationColouring)
    {
      currentColour = applyConservation(currentColour, j);
    }

    return currentColour;
  }
}
