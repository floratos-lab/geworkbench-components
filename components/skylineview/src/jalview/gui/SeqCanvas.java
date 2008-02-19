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
package jalview.gui;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SeqCanvas
    extends JComponent
{
  final FeatureRenderer fr;
  final SequenceRenderer sr;
  BufferedImage img;
  Graphics2D gg;
  int imgWidth;
  int imgHeight;
  AlignViewport av;
  SearchResults searchResults = null;
  boolean fastPaint = false;
  int LABEL_WEST;
  int LABEL_EAST;

  int cursorX = 0;
  int cursorY = 0;

  /**
   * Creates a new SeqCanvas object.
   *
   * @param av DOCUMENT ME!
   */
  public SeqCanvas(AlignmentPanel ap)
  {
    this.av = ap.av;
    fr = new FeatureRenderer(ap);
    sr = new SequenceRenderer(av);
    setLayout(new BorderLayout());
    PaintRefresher.Register(this, av.getSequenceSetId());
    setBackground(Color.white);
  }

  public SequenceRenderer getSequenceRenderer()
  {
    return sr;
  }

  public FeatureRenderer getFeatureRenderer()
  {
    return fr;
  }


  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param startx DOCUMENT ME!
   * @param endx DOCUMENT ME!
   * @param ypos DOCUMENT ME!
   */
  void drawNorthScale(Graphics g, int startx, int endx, int ypos)
  {
    int scalestartx = startx - (startx % 10) + 10;

    g.setColor(Color.black);

    // NORTH SCALE
    for (int i = scalestartx; i < endx; i += 10)
    {
      int value = i;
      if (av.hasHiddenColumns)
      {
        value = av.getColumnSelection().adjustForHiddenColumns(value);
      }

      g.drawString(String.valueOf(value), (i - startx - 1) * av.charWidth,
                   ypos - (av.charHeight / 2));

      g.drawLine( ( (i - startx - 1) * av.charWidth) + (av.charWidth / 2),
                 (ypos + 2) - (av.charHeight / 2),
                 ( (i - startx - 1) * av.charWidth) + (av.charWidth / 2), ypos -
                 2);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param startx DOCUMENT ME!
   * @param endx DOCUMENT ME!
   * @param ypos DOCUMENT ME!
   */
  void drawWestScale(Graphics g, int startx, int endx, int ypos)
  {
    FontMetrics fm = getFontMetrics(av.getFont());
    ypos += av.charHeight;

    if (av.hasHiddenColumns)
    {
      startx = av.getColumnSelection().adjustForHiddenColumns(startx);
      endx = av.getColumnSelection().adjustForHiddenColumns(endx);
    }

    int maxwidth = av.alignment.getWidth();
    if (av.hasHiddenColumns)
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    // WEST SCALE
    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      SequenceI seq = av.alignment.getSequenceAt(i);
      int index = startx;
      int value = -1;

      while (index < endx)
      {
        if (jalview.util.Comparison.isGap(seq.getCharAt(index)))
        {
          index++;

          continue;
        }

        value = av.alignment.getSequenceAt(i).findPosition(index);

        break;
      }

      if (value != -1)
      {
        int x = LABEL_WEST - fm.stringWidth(String.valueOf(value)) -
            av.charWidth / 2;
        g.drawString(value + "", x,
                     (ypos + (i * av.charHeight)) - (av.charHeight / 5));
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param startx DOCUMENT ME!
   * @param endx DOCUMENT ME!
   * @param ypos DOCUMENT ME!
   */
  void drawEastScale(Graphics g, int startx, int endx, int ypos)
  {
    ypos += av.charHeight;

    if (av.hasHiddenColumns)
    {
      endx = av.getColumnSelection().adjustForHiddenColumns(endx);
    }

    SequenceI seq;
    // EAST SCALE
    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      seq = av.alignment.getSequenceAt(i);
      int index = endx;
      int value = -1;

      while (index > startx)
      {
        if (jalview.util.Comparison.isGap(seq.getCharAt(index)))
        {
          index--;

          continue;
        }

        value = seq.findPosition(index);

        break;
      }

      if (value != -1)
      {
        g.drawString(String.valueOf(value), 0,
                     (ypos + (i * av.charHeight)) - (av.charHeight / 5));
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param horizontal DOCUMENT ME!
   * @param vertical DOCUMENT ME!
   */
  public void fastPaint(int horizontal, int vertical)
  {
    if (gg == null)
    {
      return;
    }

    fastPaint = true;

    gg.copyArea(horizontal * av.charWidth,
                vertical * av.charHeight,
                imgWidth,
                imgHeight,
                -horizontal * av.charWidth,
                -vertical * av.charHeight);

    int sr = av.startRes;
    int er = av.endRes;
    int ss = av.startSeq;
    int es = av.endSeq;
    int transX = 0;
    int transY = 0;

    if (horizontal > 0) // scrollbar pulled right, image to the left
    {
      er++;
      transX = (er - sr - horizontal) * av.charWidth;
      sr = er - horizontal;
    }
    else if (horizontal < 0)
    {
      er = sr - horizontal - 1;
    }
    else if (vertical > 0) // scroll down
    {
      ss = es - vertical;

      if (ss < av.startSeq)
      { // ie scrolling too fast, more than a page at a time
        ss = av.startSeq;
      }
      else
      {
        transY = imgHeight - (vertical * av.charHeight);
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

    gg.translate(transX, transY);
    drawPanel(gg, sr, er, ss, es, 0);
    gg.translate( -transX, -transY);

    repaint();
  }

  /**
   * Definitions of startx and endx (hopefully):
   * SMJS This is what I'm working towards!
   *   startx is the first residue (starting at 0) to display.
   *   endx   is the last residue to display (starting at 0).
   *   starty is the first sequence to display (starting at 0).
   *   endy   is the last sequence to display (starting at 0).
   * NOTE 1: The av limits are set in setFont in this class and
   * in the adjustment listener in SeqPanel when the scrollbars move.
   */

  // Set this to false to force a full panel paint
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);

    if (img != null && (fastPaint
                        || (getVisibleRect().width != g.getClipBounds().width)
                        || (getVisibleRect().height != g.getClipBounds().height)))
    {
      g.drawImage(img, 0, 0, this);
      fastPaint = false;
      return;
    }

    // this draws the whole of the alignment
    imgWidth = getWidth();
    imgHeight = getHeight();

    imgWidth -= (imgWidth % av.charWidth);
    imgHeight -= (imgHeight % av.charHeight);

    if ( (imgWidth < 1) || (imgHeight < 1))
    {
      return;
    }

    if (img == null || imgWidth != img.getWidth() || imgHeight != img.getHeight())
    {
      try
      {
        img = new BufferedImage(imgWidth, imgHeight,
                                BufferedImage.TYPE_INT_RGB);
        gg = (Graphics2D) img.getGraphics();
        gg.setFont(av.getFont());
      }
      catch (OutOfMemoryError er)
      {
        System.gc();
        System.out.println(er + " making image, SeqCanvas");
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          public void run()
          {
            javax.swing.JOptionPane.showInternalMessageDialog(Desktop.
                desktop,
                "Out of memory creating alignment image!!"
                +
                "\nSee help files for increasing Java Virtual Machine memory."
                , "Out of memory",
                javax.swing.JOptionPane.WARNING_MESSAGE);
          }
        });

        return;
      }
    }

    if (av.antiAlias)
    {
      gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
    }

    gg.setColor(Color.white);
    gg.fillRect(0, 0, imgWidth, imgHeight);

    if (av.getWrapAlignment())
    {
      drawWrappedPanel(gg, getWidth(), getHeight(), av.startRes);
    }
    else
    {
      drawPanel(gg, av.startRes, av.endRes, av.startSeq, av.endSeq, 0);
    }

    g.drawImage(img, 0, 0, this);


  }

  /**
   * DOCUMENT ME!
   *
   * @param cwidth DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getWrappedCanvasWidth(int cwidth)
  {
    FontMetrics fm = getFontMetrics(av.getFont());

    LABEL_EAST = 0;
    LABEL_WEST = 0;

    if (av.scaleRightWrapped)
    {
      LABEL_EAST = fm.stringWidth(getMask());
    }

    if (av.scaleLeftWrapped)
    {
      LABEL_WEST = fm.stringWidth(getMask());
    }

    return (cwidth - LABEL_EAST - LABEL_WEST) / av.charWidth;
  }

  /**
   * Generates a string of zeroes.
   * @return String
   */
  String getMask()
  {
    String mask = "00";
    int maxWidth = 0;
    int tmp;
    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      tmp = av.alignment.getSequenceAt(i).getEnd();
      if (tmp > maxWidth)
      {
        maxWidth = tmp;
      }
    }

    for (int i = maxWidth; i > 0; i /= 10)
    {
      mask += "0";
    }
    return mask;
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param canvasWidth DOCUMENT ME!
   * @param canvasHeight DOCUMENT ME!
   * @param startRes DOCUMENT ME!
   */
  public void drawWrappedPanel(Graphics g, int canvasWidth, int canvasHeight,
                               int startRes)
  {
    AlignmentI al = av.getAlignment();

    FontMetrics fm = getFontMetrics(av.getFont());

    if (av.scaleRightWrapped)
    {
      LABEL_EAST = fm.stringWidth(getMask());
    }

    if (av.scaleLeftWrapped)
    {
      LABEL_WEST = fm.stringWidth(getMask());
    }

    int hgap = av.charHeight;
    if (av.scaleAboveWrapped)
    {
      hgap += av.charHeight;
    }

    int cWidth = (canvasWidth - LABEL_EAST - LABEL_WEST) / av.charWidth;
    int cHeight = av.getAlignment().getHeight() * av.charHeight;

    av.setWrappedWidth(cWidth);

    av.endRes = av.startRes + cWidth;

    int endx;
    int ypos = hgap;
    int maxwidth = av.alignment.getWidth() - 1;

    if (av.hasHiddenColumns)
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    while ( (ypos <= canvasHeight) && (startRes < maxwidth))
    {
      endx = startRes + cWidth - 1;

      if (endx > maxwidth)
      {
        endx = maxwidth;
      }

      g.setFont(av.getFont());
      g.setColor(Color.black);

      if (av.scaleLeftWrapped)
      {
        drawWestScale(g, startRes, endx, ypos);
      }

      if (av.scaleRightWrapped)
      {
        g.translate(canvasWidth - LABEL_EAST, 0);
        drawEastScale(g, startRes, endx, ypos);
        g.translate( - (canvasWidth - LABEL_EAST), 0);
      }

      g.translate(LABEL_WEST, 0);

      if (av.scaleAboveWrapped)
      {
        drawNorthScale(g, startRes, endx, ypos);
      }

      if (av.hasHiddenColumns && av.showHiddenMarkers)
      {
        g.setColor(Color.blue);
        int res;
        for (int i = 0; i < av.getColumnSelection().getHiddenColumns().size();
             i++)
        {
          res = av.getColumnSelection().findHiddenRegionPosition(i) -
              startRes;

          if (res < 0 || res > endx - startRes)
          {
            continue;
          }

          gg.fillPolygon(new int[]
                         {res * av.charWidth - av.charHeight / 4,
                         res * av.charWidth + av.charHeight / 4,
                         res * av.charWidth},
                         new int[]
                         {
                         ypos - (av.charHeight / 2),
                         ypos - (av.charHeight / 2),
                         ypos - (av.charHeight / 2) + 8
          }, 3);

        }
      }

      // When printing we have an extra clipped region,
      // the Printable page which we need to account for here
      Shape clip = g.getClip();

      if (clip == null)
      {
        g.setClip(0, 0, cWidth * av.charWidth, canvasHeight);
      }
      else
      {
        g.setClip(0, (int) clip.getBounds().getY(),
                  cWidth * av.charWidth, (int) clip.getBounds().getHeight());
      }

      drawPanel(g, startRes, endx, 0, al.getHeight(), ypos);

      if (av.showAnnotation)
      {
        g.translate(0, cHeight + ypos + 3);
        if (annotations == null)
        {
          annotations = new AnnotationPanel(av);
        }

        annotations.drawComponent( (Graphics2D) g, startRes, endx + 1);
        g.translate(0, -cHeight - ypos - 3);
      }
      g.setClip(clip);
      g.translate( -LABEL_WEST, 0);

      ypos += cHeight + getAnnotationHeight() + hgap;

      startRes += cWidth;
    }
  }

  AnnotationPanel annotations;
  int getAnnotationHeight()
  {
    if (!av.showAnnotation)
    {
      return 0;
    }

    if (annotations == null)
    {
      annotations = new AnnotationPanel(av);
    }

    return annotations.adjustPanelHeight();
  }

  /**
   * DOCUMENT ME!
   *
   * @param g1 DOCUMENT ME!
   * @param startRes DOCUMENT ME!
   * @param endRes DOCUMENT ME!
   * @param startSeq DOCUMENT ME!
   * @param endSeq DOCUMENT ME!
   * @param offset DOCUMENT ME!
   */
  void drawPanel(Graphics g1, int startRes, int endRes,
                 int startSeq, int endSeq, int offset)
  {
    if (!av.hasHiddenColumns)
    {
      draw(g1, startRes, endRes, startSeq, endSeq, offset);
    }
    else
    {
      java.util.Vector regions = av.getColumnSelection().getHiddenColumns();

      int screenY = 0;
      int blockStart = startRes;
      int blockEnd = endRes;

      for (int i = 0; i < regions.size(); i++)
      {
        int[] region = (int[]) regions.elementAt(i);
        int hideStart = region[0];
        int hideEnd = region[1];

        if (hideStart <= blockStart)
        {
          blockStart += (hideEnd - hideStart) + 1;
          continue;
        }

        blockEnd = hideStart - 1;

        g1.translate(screenY * av.charWidth, 0);

        draw(g1, blockStart, blockEnd, startSeq, endSeq, offset);

        if (av.getShowHiddenMarkers())
        {
          g1.setColor(Color.blue);

          g1.drawLine( (blockEnd - blockStart + 1) * av.charWidth - 1,
                      0 + offset,
                      (blockEnd - blockStart + 1) * av.charWidth - 1,
                      (endSeq - startSeq) * av.charHeight + offset);
        }

        g1.translate( -screenY * av.charWidth, 0);
        screenY += blockEnd - blockStart + 1;
        blockStart = hideEnd + 1;
      }

      if (screenY <= (endRes - startRes))
      {
        blockEnd = blockStart + (endRes - startRes) - screenY;
        g1.translate(screenY * av.charWidth, 0);
        draw(g1, blockStart, blockEnd, startSeq, endSeq, offset);

        g1.translate( -screenY * av.charWidth, 0);
      }
    }

  }

  //int startRes, int endRes, int startSeq, int endSeq, int x, int y,
  // int x1, int x2, int y1, int y2, int startx, int starty,
  void draw(Graphics g,
            int startRes, int endRes,
            int startSeq, int endSeq,
            int offset)
  {
    g.setFont(av.getFont());
    sr.prepare(g, av.renderGaps);

    SequenceI nextSeq;

    /// First draw the sequences
    /////////////////////////////
    for (int i = startSeq; i < endSeq; i++)
    {
      nextSeq = av.alignment.getSequenceAt(i);

      sr.drawSequence(nextSeq, av.alignment.findAllGroups(nextSeq),
                      startRes, endRes,
                      offset + ( (i - startSeq) * av.charHeight));

      if (av.showSequenceFeatures)
      {
        fr.drawSequence(g, nextSeq, startRes, endRes,
                        offset + ( (i - startSeq) * av.charHeight));
      }

      /// Highlight search Results once all sequences have been drawn
      //////////////////////////////////////////////////////////
      if (searchResults != null)
      {
        int[] visibleResults = searchResults.getResults(nextSeq, startRes,
            endRes);
        if (visibleResults != null)
        {
          for (int r = 0; r < visibleResults.length; r += 2)
          {
            sr.drawHighlightedText(nextSeq, visibleResults[r],
                                   visibleResults[r + 1],
                                   (visibleResults[r] - startRes) *
                                   av.charWidth,
                                   offset + ( (i - startSeq) * av.charHeight));
          }
        }
      }

      if (av.cursorMode && cursorY == i
          && cursorX >= startRes && cursorX <= endRes)
      {
        sr.drawCursor(nextSeq, cursorX, (cursorX - startRes) * av.charWidth,
                      offset + ( (i - startSeq) * av.charHeight));
      }
    }

    if (av.getSelectionGroup() != null || av.alignment.getGroups().size() > 0)
    {
      drawGroupsBoundaries(g, startRes, endRes, startSeq, endSeq, offset);
    }

  }

  void drawGroupsBoundaries(Graphics g1,
                            int startRes, int endRes,
                            int startSeq, int endSeq,
                            int offset)
  {
    Graphics2D g = (Graphics2D) g1;
    //
    /////////////////////////////////////
    // Now outline any areas if necessary
    /////////////////////////////////////
    SequenceGroup group = av.getSelectionGroup();

    int sx = -1;
    int sy = -1;
    int ex = -1;
    int groupIndex = -1;
    int visWidth = (endRes - startRes + 1) * av.charWidth;

    if ( (group == null) && (av.alignment.getGroups().size() > 0))
    {
      group = (SequenceGroup) av.alignment.getGroups().elementAt(0);
      groupIndex = 0;
    }

    if (group != null)
    {
      do
      {
        int oldY = -1;
        int i = 0;
        boolean inGroup = false;
        int top = -1;
        int bottom = -1;

        for (i = startSeq; i < endSeq; i++)
        {
          sx = (group.getStartRes() - startRes) * av.charWidth;
          sy = offset + ( (i - startSeq) * av.charHeight);
          ex = ( ( (group.getEndRes() + 1) - group.getStartRes()) *
                av.charWidth) -
              1;

          if (sx + ex < 0 || sx > visWidth)
          {
            continue;
          }

          if ( (sx <= (endRes - startRes) * av.charWidth) &&
              group.getSequences(null).
              contains(av.alignment.getSequenceAt(i)))
          {
            if ( (bottom == -1) &&
                !group.getSequences(null).contains(
                    av.alignment.getSequenceAt(i + 1)))
            {
              bottom = sy + av.charHeight;
            }

            if (!inGroup)
            {
              if ( ( (top == -1) && (i == 0)) ||
                  !group.getSequences(null).contains(
                      av.alignment.getSequenceAt(i - 1)))
              {
                top = sy;
              }

              oldY = sy;
              inGroup = true;

              if (group == av.getSelectionGroup())
              {
                g.setStroke(new BasicStroke(1,
                                            BasicStroke.CAP_BUTT,
                                            BasicStroke.JOIN_ROUND, 3f,
                                            new float[]
                                            {5f, 3f}, 0f));
                g.setColor(Color.RED);
              }
              else
              {
                g.setStroke(new BasicStroke());
                g.setColor(group.getOutlineColour());
              }
            }
          }
          else
          {
            if (inGroup)
            {
              if (sx >= 0 && sx < visWidth)
              {
                g.drawLine(sx, oldY, sx, sy);
              }

              if (sx + ex < visWidth)
              {
                g.drawLine(sx + ex, oldY, sx + ex, sy);
              }

              if (sx < 0)
              {
                ex += sx;
                sx = 0;
              }

              if (sx + ex > visWidth)
              {
                ex = visWidth;
              }

              else if (sx + ex >= (endRes - startRes + 1) * av.charWidth)
              {
                ex = (endRes - startRes + 1) * av.charWidth;
              }

              if (top != -1)
              {
                g.drawLine(sx, top, sx + ex, top);
                top = -1;
              }

              if (bottom != -1)
              {
                g.drawLine(sx, bottom, sx + ex, bottom);
                bottom = -1;
              }

              inGroup = false;
            }
          }
        }

        if (inGroup)
        {
          sy = offset + ( (i - startSeq) * av.charHeight);
          if (sx >= 0 && sx < visWidth)
          {
            g.drawLine(sx, oldY, sx, sy);
          }

          if (sx + ex < visWidth)
          {
            g.drawLine(sx + ex, oldY, sx + ex, sy);
          }

          if (sx < 0)
          {
            ex += sx;
            sx = 0;
          }

          if (sx + ex > visWidth)
          {
            ex = visWidth;
          }
          else if (sx + ex >= (endRes - startRes + 1) * av.charWidth)
          {
            ex = (endRes - startRes + 1) * av.charWidth;
          }

          if (top != -1)
          {
            g.drawLine(sx, top, sx + ex, top);
            top = -1;
          }

          if (bottom != -1)
          {
            g.drawLine(sx, bottom - 1, sx + ex, bottom - 1);
            bottom = -1;
          }

          inGroup = false;
        }

        groupIndex++;

        g.setStroke(new BasicStroke());

        if (groupIndex >= av.alignment.getGroups().size())
        {
          break;
        }

        group = (SequenceGroup) av.alignment.getGroups().elementAt(groupIndex);

      }
      while (groupIndex < av.alignment.getGroups().size());

    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param results DOCUMENT ME!
   */
  public void highlightSearchResults(SearchResults results)
  {
    img = null;

    searchResults = results;

    repaint();
  }
}
