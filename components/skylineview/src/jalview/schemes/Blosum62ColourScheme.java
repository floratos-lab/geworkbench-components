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

import jalview.analysis.*;

public class Blosum62ColourScheme
    extends ResidueColourScheme
{
  public Blosum62ColourScheme()
  {
    super();
  }

  public Color findColour(char res, int j)
  {
    if ('a' <= res && res <= 'z')
    {
      // TO UPPERCASE !!!
      res -= ('a' - 'A');
    }

    if (consensus == null ||
        j >= consensus.length ||
        consensus[j] == null ||
        (threshold != 0 && !aboveThreshold(res, j)))
    {
      return Color.white;
    }

    Color currentColour;

    if (!jalview.util.Comparison.isGap(res))
    {
      String max = (String) consensus[j].get(AAFrequency.MAXRESIDUE);

      if (max.indexOf(res) > -1)
      {
        currentColour = new Color(154, 154, 255);
      }
      else
      {
        int c = 0;
        int max_aa = 0;
        int n = max.length();

        do
        {
          c += ResidueProperties.getBLOSUM62(
              max.charAt(max_aa), res);
        }
        while (++max_aa < n);

        if (c > 0)
        {
          currentColour = new Color(204, 204, 255);
        }
        else
        {
          currentColour = Color.white;
        }
      }

      if (conservationColouring)
      {
        currentColour = applyConservation(currentColour, j);
      }
    }
    else
    {
      return Color.white;
    }

    return currentColour;
  }
}
