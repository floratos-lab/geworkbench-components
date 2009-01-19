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

public class IdCanvas
    extends Panel
{
  protected AlignViewport av;

  protected boolean showScores = true;

  protected int maxIdLength = -1;
  protected String maxIdStr = null;
  Image image;
  Graphics gg;
  int imgHeight = 0;
  boolean fastPaint = false;

  java.util.Vector searchResults;

  public IdCanvas(AlignViewport av)
  {
    setLayout(null);
    this.av = av;
    PaintRefresher.Register(this, av.getSequenceSetId());
  }

  public void drawIdString(Graphics gg, SequenceI s, int i, int starty,
                           int ypos)
  {
    int charHeight = av.getCharHeight();

    if (searchResults != null && searchResults.contains(s))
    {
      gg.setColor(Color.black);
      gg.fillRect(0, ( (i - starty) * charHeight) + ypos,
                  getSize().width, charHeight);
      gg.setColor(Color.white);
    }
    else if (av.getSelectionGroup() != null &&
             av.getSelectionGroup().getSequences(null).contains(s))
    {
      gg.setColor(Color.lightGray);
      gg.fillRect(0, ( (i - starty) * charHeight) + ypos,
                  getSize().width, charHeight);
      gg.setColor(Color.white);
    }
    else
    {
      gg.setColor(av.getSequenceColour(s));
      gg.fillRect(0, ( (i - starty) * charHeight) + ypos,
                  getSize().width, charHeight);
      gg.setColor(Color.black);
    }

    gg.drawString(s.getDisplayId(av.getShowJVSuffix()), 0,
                  ( (i - starty) * charHeight) + ypos +
                  charHeight - (charHeight / 5));

    if (av.hasHiddenRows && av.showHiddenMarkers)
    {
      drawMarker(i, starty, ypos);
    }

  }

  public void fastPaint(int vertical)
  {
    if (gg == null)
    {
      repaint();
      return;
    }

    gg.copyArea(0, 0, getSize().width, imgHeight, 0, -vertical * av.charHeight);

    int ss = av.startSeq, es = av.endSeq, transY = 0;
    if (vertical > 0) // scroll down
    {
      ss = es - vertical;
      if (ss < av.startSeq) // ie scrolling too fast, more than a page at a time
      {
        ss = av.startSeq;
      }
      else
      {
        transY = imgHeight - vertical * av.charHeight;
      }
    }
    else if (vertical < 0)
    {
      es = ss - vertical;
      if (es > av.endSeq)
      {
        es = av.endSeq;
      }
    }

    gg.translate(0, transY);

    drawIds(ss, es);

    gg.translate(0, -transY);

    fastPaint = true;
    repaint();
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {
    if (getSize().height < 0 || getSize().width < 0)
    {
      return;
    }
    if (fastPaint)
    {
      fastPaint = false;
      g.drawImage(image, 0, 0, this);
      return;
    }

    imgHeight = getSize().height;
    imgHeight -= imgHeight % av.charHeight;

    if (imgHeight < 1)
    {
      return;
    }

    if (image == null || imgHeight != image.getHeight(this))
    {
      image = createImage(getSize().width, imgHeight);
      gg = image.getGraphics();
      gg.setFont(av.getFont());
    }

    //Fill in the background
    gg.setColor(Color.white);
    Font italic = new Font(av.getFont().getName(), Font.ITALIC,
                           av.getFont().getSize());
    gg.setFont(italic);

    gg.fillRect(0, 0, getSize().width, getSize().height);
    drawIds(av.startSeq, av.endSeq);
    g.drawImage(image, 0, 0, this);
  }

  void drawIds(int starty, int endy)
  {
    Font italic = new Font(av.getFont().getName(), Font.ITALIC,
                           av.getFont().getSize());

    gg.setFont(italic);

    Color currentColor = Color.white;
    Color currentTextColor = Color.black;

    if (av.getWrapAlignment())
    {
      int maxwidth = av.alignment.getWidth();
      int alheight = av.alignment.getHeight();

      if (av.hasHiddenColumns)
      {
        maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
      }

      int annotationHeight = 0;
      AnnotationLabels labels = null;

      if (av.showAnnotation)
      {
        AnnotationPanel ap = new AnnotationPanel(av);
        annotationHeight = ap.adjustPanelHeight();
        labels = new AnnotationLabels(av);
      }

      int hgap = av.charHeight;
      if (av.scaleAboveWrapped)
      {
        hgap += av.charHeight;
      }

      int cHeight = alheight * av.charHeight
          + hgap
          + annotationHeight;

      int rowSize = av.getEndRes() - av.getStartRes();

      // Draw the rest of the panels
      for (int ypos = hgap, row = av.startRes;
           (ypos <= getSize().height) && (row < maxwidth);
           ypos += cHeight, row += rowSize)
      {
        for (int i = starty; i < alheight; i++)
        {
          if (av.hasHiddenRows)
          {
            setHiddenFont(i);
          }
          else
          {
            gg.setFont(italic);
          }

          SequenceI s = av.alignment.getSequenceAt(i);
          drawIdString(gg, s, i, 0, ypos);
        }

        if (labels != null)
        {
          gg.translate(0, ypos + (alheight * av.charHeight));
          labels.drawComponent(gg, getSize().width);
          gg.translate(0, -ypos - (alheight * av.charHeight));
        }

      }
    }
    else
    {
      //Now draw the id strings

      //Now draw the id strings
      SequenceI seq;
      for (int i = starty; i < endy; i++)
      {
        if (av.hasHiddenRows)
        {
          setHiddenFont(i);
        }

        seq = av.alignment.getSequenceAt(i);
        if (seq == null)
        {
          continue;
        }

        // Selected sequence colours
        if ( (searchResults != null) &&
            searchResults.contains(seq))
        {
          currentColor = Color.black;
          currentTextColor = Color.white;
        }
        else if ( (av.getSelectionGroup() != null) &&
                 av.getSelectionGroup().getSequences(null).contains(seq))
        {
          currentColor = Color.lightGray;
          currentTextColor = Color.black;
        }
        else
        {
          currentColor = av.getSequenceColour(seq);
          currentTextColor = Color.black;
        }

        gg.setColor(currentColor);

        gg.fillRect(0, (i - starty) * av.charHeight, getSize().width,
                    av.charHeight);

        gg.setColor(currentTextColor);

        gg.drawString(seq.getDisplayId(av.getShowJVSuffix()),
                      0,
                      ( ( (i - starty) * av.charHeight) + av.charHeight) -
                      (av.charHeight / 5));

        if (av.hasHiddenRows && av.showHiddenMarkers)
        {
          drawMarker(i, starty, 0);
        }
      }
    }
  }

  public void setHighlighted(java.util.Vector found)
  {
    searchResults = found;
    repaint();
  }

  void drawMarker(int i, int starty, int yoffset)
  {
    SequenceI[] hseqs = av.alignment.getHiddenSequences().hiddenSequences;
    //Use this method here instead of calling hiddenSeq adjust
    //3 times.
    int hSize = hseqs.length;

    int hiddenIndex = i;
    int lastIndex = i - 1;
    int nextIndex = i + 1;

    boolean below = (hiddenIndex > lastIndex + 1);
    boolean above = (nextIndex > hiddenIndex + 1);

    for (int j = 0; j < hSize; j++)
    {
      if (hseqs[j] != null)
      {
        if (j - 1 < hiddenIndex)
        {
          hiddenIndex++;
        }
        if (j - 1 < lastIndex)
        {
          lastIndex++;
        }
        if (j - 1 < nextIndex)
        {
          nextIndex++;
        }
      }
    }

    gg.setColor(Color.blue);
    if (below)
    {
      gg.fillPolygon(new int[]
                     {getSize().width - av.charHeight,
                     getSize().width - av.charHeight,
                     getSize().width},
                     new int[]
                     {
                     (i - starty) * av.charHeight + yoffset,
                     (i - starty) * av.charHeight + yoffset + av.charHeight / 4,
                     (i - starty) * av.charHeight + yoffset
      }, 3);
    }
    if (above)
    {
      gg.fillPolygon(new int[]
                     {getSize().width - av.charHeight,
                     getSize().width - av.charHeight,
                     getSize().width},
                     new int[]
                     {
                     (i - starty + 1) * av.charHeight + yoffset,
                     (i - starty + 1) * av.charHeight + yoffset -
                     av.charHeight / 4,
                     (i - starty + 1) * av.charHeight + yoffset
      }, 3);

    }
  }

  void setHiddenFont(int i)
  {
    /*  System.out.println(i+" "+av.alignment.getHeight());
      if (av.alignment.getSequenceAt(i).getHiddenSequences() != null)
        gg.setFont(new Font(av.getFont().getName(), Font.BOLD,
                            av.getFont().getSize()));
      else
        gg.setFont(new Font(av.getFont().getName(), Font.ITALIC,
                            av.getFont().getSize()));*/
  }
}
