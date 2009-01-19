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

public class UserColourScheme
    extends ResidueColourScheme
{
  Color[] lowerCaseColours;

  protected String schemeName;

  public UserColourScheme()
  {}

  public UserColourScheme(Color[] newColors)
  {
    colors = newColors;
  }

  public UserColourScheme(String colour)
  {
    Color col = getColourFromString(colour);

    if (col == null)
    {
      System.out.println("Unknown colour!! " + colour);
      col = createColourFromName(colour);
    }

    colors = new Color[24];
    for (int i = 0; i < 24; i++)
    {
      colors[i] = col;
    }
  }

  public Color[] getColours()
  {
    return colors;
  }

  public Color[] getLowerCaseColours()
  {
    return lowerCaseColours;
  }

  public void setName(String name)
  {
    schemeName = name;
  }

  public String getName()
  {
    return schemeName;
  }

  public Color getColourFromString(String colour)
  {
    colour = colour.trim();

    Color col = null;
    try
    {
      int value = Integer.parseInt(colour, 16);
      col = new Color(value);
    }
    catch (NumberFormatException ex)
    {}

    if (col == null)
    {
      col = ColourSchemeProperty.getAWTColorFromName(colour);
    }

    if (col == null)
    {
      try
      {
        java.util.StringTokenizer st = new java.util.StringTokenizer(colour,
            ",");
        int r = Integer.parseInt(st.nextToken());
        int g = Integer.parseInt(st.nextToken());
        int b = Integer.parseInt(st.nextToken());
        col = new Color(r, g, b);
      }
      catch (Exception ex)
      {}
    }

    return col;

  }

  public Color createColourFromName(String name)
  {
    int r, g, b;

    int lsize = name.length();
    int start = 0, end = lsize / 3;

    int rgbOffset = Math.abs(name.hashCode() % 10) * 15;

    r = Math.abs(name.substring(start, end).hashCode() + rgbOffset) % 210 + 20;
    start = end;
    end += lsize / 3;
    if (end > lsize)
    {
      end = lsize;
    }

    g = Math.abs(name.substring(start, end).hashCode() + rgbOffset) % 210 + 20;

    b = Math.abs(name.substring(end).hashCode() + rgbOffset) % 210 + 20;

    Color color = new Color(r, g, b);

    return color;
  }

  public void parseAppletParameter(String paramValue)
  {
    StringTokenizer st = new StringTokenizer(paramValue, ";");
    StringTokenizer st2;
    String token = null, colour, residues;
    try
    {
      while (st.hasMoreElements())
      {
        token = st.nextToken().trim();
        residues = token.substring(0, token.indexOf("="));
        colour = token.substring(token.indexOf("=") + 1);

        st2 = new StringTokenizer(residues, " ,");
        while (st2.hasMoreTokens())
        {
          token = st2.nextToken();

          if (ResidueProperties.aaIndex[token.charAt(0)] == -1)
          {
            continue;
          }

          int colIndex = ResidueProperties.aaIndex[token.charAt(0)];

          if (token.equalsIgnoreCase("lowerCase"))
          {
            if (lowerCaseColours == null)
            {
              lowerCaseColours = new Color[23];
            }
            for (int i = 0; i < 23; i++)
            {
              if (lowerCaseColours[i] == null)
              {
                lowerCaseColours[i] = getColourFromString(colour);
              }
            }

            continue;
          }

          if (token.equals(token.toLowerCase()))
          {
            if (lowerCaseColours == null)
            {
              lowerCaseColours = new Color[23];
            }
            lowerCaseColours[colIndex] = getColourFromString(colour);
          }
          else
          {
            colors[colIndex] = getColourFromString(colour);
          }
        }
      }
    }
    catch (Exception ex)
    {
      System.out.println("Error parsing userDefinedColours:\n"
                         + token + "\n" + ex);
    }

  }

  public Color findColour(char c, int j)
  {
    Color currentColour;
    int index = ResidueProperties.aaIndex[c];

    if ( (threshold == 0) || aboveThreshold(c, j))
    {
      if (lowerCaseColours != null && 'a' <= c && c <= 'z')
      {
        currentColour = lowerCaseColours[index];
      }
      else
      {
        currentColour = colors[index];
      }
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

  public void setLowerCaseColours(Color[] lcolours)
  {
    lowerCaseColours = lcolours;
  }

}
