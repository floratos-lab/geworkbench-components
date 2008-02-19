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
import java.awt.event.*;

public class OverviewPanel
    extends Panel implements Runnable, MouseMotionListener, MouseListener
{
  Image miniMe;
  Image offscreen;
  AlignViewport av;
  AlignmentPanel ap;
  float scalew = 1f;
  float scaleh = 1f;

  public int width, sequencesHeight;
  int graphHeight = 20;
  int boxX = -1, boxY = -1, boxWidth = -1, boxHeight = -1;

  boolean resizing = false;

  // Can set different properties in this seqCanvas than
  // main visible SeqCanvas
  SequenceRenderer sr;
  FeatureRenderer fr;

  Frame nullFrame;

  public OverviewPanel(AlignmentPanel ap)
  {
    this.av = ap.av;
    this.ap = ap;
    setLayout(null);
    nullFrame = new Frame();
    nullFrame.addNotify();

    sr = new SequenceRenderer(av);
    sr.graphics = nullFrame.getGraphics();
    sr.renderGaps = false;
    sr.forOverview = true;
    fr = new FeatureRenderer(av);
    fr.overview = true;

    // scale the initial size of overviewpanel to shape of alignment
    float initialScale = (float) av.alignment.getWidth() /
        (float) av.alignment.getHeight();

    if (av.hconsensus == null)
    {
      graphHeight = 0;
    }

    if (av.alignment.getWidth() > av.alignment.getHeight())
    {
      // wider
      width = 400;
      sequencesHeight = (int) (400f / initialScale);
      if (sequencesHeight < 40)
      {
        sequencesHeight = 40;
      }
    }
    else
    {
      // taller
      width = (int) (400f * initialScale);
      sequencesHeight = 300;
      if (width < 120)
      {
        width = 120;
      }
    }

    setSize(new Dimension(width, sequencesHeight + graphHeight));
    addComponentListener(new ComponentAdapter()
    {

      public void componentResized(ComponentEvent evt)
      {
        if (getSize().width != width ||
            getSize().height != sequencesHeight + graphHeight)
        {
          updateOverviewImage();
        }
      }
    });

    addMouseMotionListener(this);

    addMouseListener(this);

    updateOverviewImage();

  }

  public void mouseEntered(MouseEvent evt)
  {}

  public void mouseExited(MouseEvent evt)
  {}

  public void mouseClicked(MouseEvent evt)
  {}

  public void mouseMoved(MouseEvent evt)
  {}

  public void mousePressed(MouseEvent evt)
  {
    boxX = evt.getX();
    boxY = evt.getY();
    checkValid();
  }

  public void mouseReleased(MouseEvent evt)
  {
    boxX = evt.getX();
    boxY = evt.getY();
    checkValid();
  }

  public void mouseDragged(MouseEvent evt)
  {
    boxX = evt.getX();
    boxY = evt.getY();
    checkValid();
  }

  void checkValid()
  {
    if (boxY < 0)
    {
      boxY = 0;
    }

    if (boxY > (sequencesHeight - boxHeight))
    {
      boxY = sequencesHeight - boxHeight + 1;
    }

    if (boxX < 0)
    {
      boxX = 0;
    }

    if (boxX > (width - boxWidth))
    {
      if (av.hasHiddenColumns)
      {
        //Try smallest possible box
        boxWidth = (int) ( (av.endRes - av.startRes + 1) *
                          av.getCharWidth() * scalew);
      }
      boxX = width - boxWidth;
    }

    int col = (int) (boxX / scalew / av.getCharWidth());
    int row = (int) (boxY / scaleh / av.getCharHeight());

    if (av.hasHiddenColumns)
    {
      if (!av.getColumnSelection().isVisible(col))
      {
        return;
      }

      col = av.getColumnSelection().findColumnPosition(col);
    }

    if (av.hasHiddenRows)
    {
      row = av.alignment.getHiddenSequences().findIndexWithoutHiddenSeqs(row);
    }

    ap.setScrollValues(col, row);
    ap.paintAlignment(false);
  }

  /**
   * DOCUMENT ME!
   */
  public void updateOverviewImage()
  {
    if (resizing)
    {
      resizeAgain = true;
      return;
    }

    if (av.showSequenceFeatures)
    {
      fr.featureGroups = ap.seqPanel.seqCanvas.getFeatureRenderer().
          featureGroups;
      fr.featureColours = ap.seqPanel.seqCanvas.getFeatureRenderer().
          featureColours;
    }

    resizing = true;

    if ( (getSize().width > 0) && (getSize().height > 0))
    {
      width = getSize().width;
      sequencesHeight = getSize().height - graphHeight;
    }
    setSize(new Dimension(width, sequencesHeight + graphHeight));

    Thread thread = new Thread(this);
    thread.start();
    repaint();
  }

  // This is set true if the user resizes whilst
  // the overview is being calculated
  boolean resizeAgain = false;

  public void run()
  {
    miniMe = null;
    int alwidth = av.alignment.getWidth();
    int alheight = av.alignment.getHeight();

    if (av.showSequenceFeatures)
    {
      fr.transferSettings(ap.seqPanel.seqCanvas.getFeatureRenderer());
    }

    if (getSize().width > 0 && getSize().height > 0)
    {
      width = getSize().width;
      sequencesHeight = getSize().height - graphHeight;
    }

    setSize(new Dimension(width, sequencesHeight + graphHeight));

    int fullsizeWidth = alwidth * av.getCharWidth();
    int fullsizeHeight = alheight * av.getCharHeight();

    scalew = (float) width / (float) fullsizeWidth;
    scaleh = (float) sequencesHeight / (float) fullsizeHeight;

    miniMe = nullFrame.createImage(width, sequencesHeight + graphHeight);
    offscreen = nullFrame.createImage(width, sequencesHeight + graphHeight);

    Graphics mg = miniMe.getGraphics();
    float sampleCol = (float) alwidth / (float) width;
    float sampleRow = (float) alheight / (float) sequencesHeight;

    int lastcol = 0, lastrow = 0;
    int xstart = 0, ystart = 0;
    Color color = Color.yellow;
    int row, col, sameRow = 0, sameCol = 0;
    jalview.datamodel.SequenceI seq;
    boolean hiddenRow = false;
    for (row = 0; row <= sequencesHeight; row++)
    {
      if ( (int) (row * sampleRow) == lastrow)
      {
        sameRow++;
        continue;
      }

      hiddenRow = false;
      if (av.hasHiddenRows)
      {
        seq = av.alignment.getHiddenSequences().getHiddenSequence(lastrow);
        if (seq == null)
        {
          int index =
              av.alignment.getHiddenSequences().findIndexWithoutHiddenSeqs(
              lastrow);

          seq = av.alignment.getSequenceAt(index);
        }
        else
        {
          hiddenRow = true;
        }
      }
      else
      {
        seq = av.alignment.getSequenceAt(lastrow);
      }

      for (col = 0; col < width; col++)
      {
        if ( (int) (col * sampleCol) == lastcol &&
            (int) (row * sampleRow) == lastrow)
        {
          sameCol++;
          continue;
        }

        lastcol = (int) (col * sampleCol);

        if (seq.getLength() > lastcol)
        {
          color = sr.getResidueBoxColour(
              seq, lastcol);

          if (av.showSequenceFeatures)
          {
            color = fr.findFeatureColour(color, seq, lastcol);
          }
        }
        else
        {
          color = Color.white; //White
        }

        if (hiddenRow ||
            (av.hasHiddenColumns && !av.getColumnSelection().isVisible(lastcol)))
        {
          color = color.darker().darker();
        }

        mg.setColor(color);
        if (sameCol == 1 && sameRow == 1)
        {
          mg.drawLine(xstart, ystart, xstart, ystart);
        }
        else
        {
          mg.fillRect(xstart, ystart, sameCol, sameRow);
        }

        xstart = col;
        sameCol = 1;
      }
      lastrow = (int) (row * sampleRow);
      ystart = row;
      sameRow = 1;
    }

    if (av.conservation != null)
    {
      for (col = 0; col < width; col++)
      {
        lastcol = (int) (col * sampleCol);
        {
          mg.translate(col, sequencesHeight);
          ap.annotationPanel.drawGraph(mg, av.conservation,
                                       (int) (sampleCol) + 1,
                                       graphHeight,
                                       (int) (col * sampleCol),
                                       (int) (col * sampleCol) + 1);
          mg.translate( -col, -sequencesHeight);
        }
      }
    }
    System.gc();

    resizing = false;

    setBoxPosition();

    if (resizeAgain)
    {
      resizeAgain = false;
      updateOverviewImage();
    }
  }

  public void setBoxPosition()
  {
    int fullsizeWidth = av.alignment.getWidth() * av.getCharWidth();
    int fullsizeHeight = (av.alignment.getHeight()
                          + av.alignment.getHiddenSequences().getSize()) *
        av.getCharHeight();

    int startRes = av.getStartRes();
    int endRes = av.getEndRes();

    if (av.hasHiddenColumns)
    {
      startRes = av.getColumnSelection().adjustForHiddenColumns(startRes);
      endRes = av.getColumnSelection().adjustForHiddenColumns(endRes);
    }

    int startSeq = av.startSeq;
    int endSeq = av.endSeq;

    if (av.hasHiddenRows)
    {
      startSeq =
          av.alignment.getHiddenSequences().adjustForHiddenSeqs(startSeq);

      endSeq =
          av.alignment.getHiddenSequences().adjustForHiddenSeqs(endSeq);

    }

    scalew = (float) width / (float) fullsizeWidth;
    scaleh = (float) sequencesHeight / (float) fullsizeHeight;

    boxX = (int) (startRes * av.getCharWidth() * scalew);
    boxY = (int) (startSeq * av.getCharHeight() * scaleh);

    if (av.hasHiddenColumns)
    {
      boxWidth = (int) ( (endRes - startRes + 1) * av.getCharWidth() * scalew);
    }
    else
    {
      boxWidth = (int) ( (endRes - startRes + 1) * av.getCharWidth() * scalew);
    }

    boxHeight = (int) ( (endSeq - startSeq) * av.getCharHeight() * scaleh);

    repaint();
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {
    Graphics og = offscreen.getGraphics();
    if (miniMe != null)
    {
      og.drawImage(miniMe, 0, 0, this);
      og.setColor(Color.red);
      og.drawRect(boxX, boxY, boxWidth, boxHeight);
      og.drawRect(boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2);
      g.drawImage(offscreen, 0, 0, this);
    }
  }

}
