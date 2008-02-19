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

package jalview.appletgui;

import java.awt.*;

import jalview.datamodel.*;
import jalview.schemes.*;

public class SequenceRenderer
{
  AlignViewport av;
  FontMetrics fm;
  boolean renderGaps = true;
  SequenceGroup currentSequenceGroup = null;
  SequenceGroup[] allGroups = null;
  Color resBoxColour;
  Graphics graphics;
  boolean forOverview = false;

  public SequenceRenderer(AlignViewport av)
  {
    this.av = av;
  }

  /**
   * DOCUMENT ME!
   *
   * @param b DOCUMENT ME!
   */
  public void prepare(Graphics g, boolean renderGaps)
  {
    graphics = g;
    fm = g.getFontMetrics();

    this.renderGaps = renderGaps;
  }

  public Color getResidueBoxColour(SequenceI seq, int i)
  {
    allGroups = av.alignment.findAllGroups(seq);

    if (inCurrentSequenceGroup(i))
    {
      if (currentSequenceGroup.getDisplayBoxes())
      {
        getBoxColour(currentSequenceGroup.cs, seq, i);
      }
    }
    else if (av.getShowBoxes())
    {
      getBoxColour(av.globalColourScheme, seq, i);
    }

    return resBoxColour;
  }

  void getBoxColour(ColourSchemeI cs, SequenceI seq, int i)
  {
    if (cs != null)
    {
      resBoxColour = cs.findColour(seq.getCharAt(i), i);
    }
    else if (forOverview && !jalview.util.Comparison.isGap(seq.getCharAt(i)))
    {
      resBoxColour = Color.lightGray;
    }
    else
    {
      resBoxColour = Color.white;
    }

  }

  public Color findSequenceColour(SequenceI seq, int i)
  {
    allGroups = av.alignment.findAllGroups(seq);
    drawBoxes(seq, i, i, 0);
    return resBoxColour;
  }

  public void drawSequence(SequenceI seq, SequenceGroup[] sg,
                           int start, int end, int y1)
  {
    if (seq == null)
    {
      return;
    }

    allGroups = sg;

    drawBoxes(seq, start, end, y1);

    if (av.validCharWidth)
    {
      drawText(seq, start, end, y1);
    }
  }

  public void drawBoxes(SequenceI seq, int start, int end, int y1)
  {
    int i = start;
    int length = seq.getLength();

    int curStart = -1;
    int curWidth = av.charWidth;

    Color tempColour = null;
    while (i <= end)
    {
      resBoxColour = Color.white;
      if (i < length)
      {
        if (inCurrentSequenceGroup(i))
        {
          if (currentSequenceGroup.getDisplayBoxes())
          {
            getBoxColour(currentSequenceGroup.cs, seq, i);
          }
        }
        else if (av.getShowBoxes())
        {
          getBoxColour(av.getGlobalColourScheme(), seq, i);
        }
      }

      if (resBoxColour != tempColour)
      {
        if (tempColour != null)
        {
          graphics.fillRect(av.charWidth * (curStart - start), y1, curWidth,
                            av.charHeight);
        }
        graphics.setColor(resBoxColour);

        curStart = i;
        curWidth = av.charWidth;
        tempColour = resBoxColour;

      }
      else
      {
        curWidth += av.charWidth;
      }

      i++;
    }

    graphics.fillRect(av.charWidth * (curStart - start), y1, curWidth,
                      av.charHeight);
  }

  public void drawText(SequenceI seq, int start, int end, int y1)
  {
    Font boldFont = null;
    boolean bold = false;
    if (av.upperCasebold)
    {
      boldFont = new Font(av.getFont().getName(), Font.BOLD, av.charHeight);

      graphics.setFont(av.getFont());
    }

    y1 += av.charHeight - av.charHeight / 5; // height/5 replaces pady

    int charOffset = 0;

    // Need to find the sequence position here.
    if (end + 1 >= seq.getLength())
    {
      end = seq.getLength() - 1;
    }

    char s = ' ';

    for (int i = start; i <= end; i++)
    {
      graphics.setColor(Color.black);

      s = seq.getCharAt(i);
      if (!renderGaps && jalview.util.Comparison.isGap(s))
      {
        continue;
      }

      if (inCurrentSequenceGroup(i))
      {
        if (!currentSequenceGroup.getDisplayText())
        {
          continue;
        }

        if (currentSequenceGroup.getColourText())
        {
          getBoxColour(currentSequenceGroup.cs, seq, i);
          graphics.setColor(resBoxColour.darker());
        }
      }
      else
      {
        if (!av.getShowText())
        {
          continue;
        }

        if (av.getColourText())
        {
          getBoxColour(av.getGlobalColourScheme(), seq, i);
          if (av.getShowBoxes())
          {
            graphics.setColor(resBoxColour.darker());
          }
          else
          {
            graphics.setColor(resBoxColour);
          }
        }
      }

      if (av.upperCasebold)
      {
        fm = graphics.getFontMetrics();
        if ('A' <= s && s <= 'Z')
        {
          if (!bold)
          {

            graphics.setFont(boldFont);
          }
          bold = true;
        }
        else if (bold)
        {
          graphics.setFont(av.font);
          bold = false;
        }

      }

      charOffset = (av.charWidth - fm.charWidth(s)) / 2;
      graphics.drawString(String.valueOf(s),
                          charOffset + av.charWidth * (i - start),
                          y1);
    }

  }

  boolean inCurrentSequenceGroup(int res)
  {
    if (allGroups == null)
    {
      return false;
    }

    for (int i = 0; i < allGroups.length; i++)
    {
      if (allGroups[i].getStartRes() <= res && allGroups[i].getEndRes() >= res)
      {
        currentSequenceGroup = allGroups[i];
        return true;
      }
    }

    return false;
  }

  public void drawHighlightedText(SequenceI seq, int start, int end, int x1,
                                  int y1)
  {
    int pady = av.charHeight / 5;
    int charOffset = 0;
    graphics.setColor(Color.black);
    graphics.fillRect(x1, y1, av.charWidth * (end - start + 1), av.charHeight);
    graphics.setColor(Color.white);

    char s = '~';
    // Need to find the sequence position here.
    if (av.validCharWidth)
    {
      for (int i = start; i <= end; i++)
      {
        if (i < seq.getLength())
        {
          s = seq.getCharAt(i);
        }

        charOffset = (av.charWidth - fm.charWidth(s)) / 2;
        graphics.drawString(String.valueOf(s),
                            charOffset + x1 + av.charWidth * (i - start),
                            y1 + av.charHeight - pady);
      }
    }
  }

  public void drawCursor(SequenceI seq, int res, int x1, int y1)
  {
    int pady = av.charHeight / 5;
    int charOffset = 0;
    graphics.setColor(Color.black);
    graphics.fillRect(x1, y1, av.charWidth, av.charHeight);
    graphics.setColor(Color.white);

    graphics.setColor(Color.white);

    char s = seq.getCharAt(res);
    if (av.validCharWidth)
    {

      charOffset = (av.charWidth - fm.charWidth(s)) / 2;
      graphics.drawString(String.valueOf(s),
                          charOffset + x1,
                          (y1 + av.charHeight) - pady);
    }
  }

}
