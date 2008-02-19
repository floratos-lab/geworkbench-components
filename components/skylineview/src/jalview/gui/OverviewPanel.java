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
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class OverviewPanel
    extends JPanel implements Runnable
{
  BufferedImage miniMe;
  AlignViewport av;
  AlignmentPanel ap;
  float scalew = 1f;
  float scaleh = 1f;
  int width;
  int sequencesHeight;
  int graphHeight = 20;
  int boxX = -1;
  int boxY = -1;
  int boxWidth = -1;
  int boxHeight = -1;
  boolean resizing = false;

  // Can set different properties in this seqCanvas than
  // main visible SeqCanvas
  SequenceRenderer sr;
  FeatureRenderer fr;

  /**
   * Creates a new OverviewPanel object.
   *
   * @param ap DOCUMENT ME!
   */
  public OverviewPanel(AlignmentPanel ap)
  {
    this.av = ap.av;
    this.ap = ap;
    setLayout(null);

    sr = new SequenceRenderer(av);
    sr.renderGaps = false;
    sr.forOverview = true;
    fr = new FeatureRenderer(ap);

    // scale the initial size of overviewpanel to shape of alignment
    float initialScale = (float) av.alignment.getWidth() /
        (float) av.alignment.getHeight();

    if (av.conservation == null)
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

    addComponentListener(new ComponentAdapter()
    {
      public void componentResized(ComponentEvent evt)
      {
        if ( (getWidth() != width) ||
            (getHeight() != (sequencesHeight + graphHeight)))
        {
          updateOverviewImage();
        }
      }
    });

    addMouseMotionListener(new MouseMotionAdapter()
    {
      public void mouseDragged(MouseEvent evt)
      {
        if (!av.wrapAlignment)
        {
          boxX = evt.getX();
          boxY = evt.getY();
          checkValid();
        }
      }
    });

    addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent evt)
      {
        if (!av.wrapAlignment)
        {
          boxX = evt.getX();
          boxY = evt.getY();
          checkValid();
        }
      }
    });

    updateOverviewImage();
  }

  /**
   * DOCUMENT ME!
   */
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

    resizing = true;

    if ( (getWidth() > 0) && (getHeight() > 0))
    {
      width = getWidth();
      sequencesHeight = getHeight() - graphHeight;
    }

    setPreferredSize(new Dimension(width, sequencesHeight + graphHeight));

    Thread thread = new Thread(this);
    thread.start();
    repaint();
  }

  // This is set true if the user resizes whilst
  // the overview is being calculated
  boolean resizeAgain = false;

  /**
   * DOCUMENT ME!
   */
  public void run()
  {
    miniMe = null;

    if (av.showSequenceFeatures)
    {
      fr.transferSettings(ap.seqPanel.seqCanvas.getFeatureRenderer());
    }

    int alwidth = av.alignment.getWidth();
    int alheight = av.alignment.getHeight()
        + av.alignment.getHiddenSequences().getSize();

    setPreferredSize(new Dimension(width, sequencesHeight + graphHeight));

    int fullsizeWidth = alwidth * av.getCharWidth();
    int fullsizeHeight = alheight * av.getCharHeight();

    scalew = (float) width / (float) fullsizeWidth;
    scaleh = (float) sequencesHeight / (float) fullsizeHeight;

    miniMe = new BufferedImage(width, sequencesHeight + graphHeight,
                               BufferedImage.TYPE_INT_RGB);

    Graphics mg = miniMe.getGraphics();
    mg.setColor(Color.orange);
    mg.fillRect(0, 0, width, miniMe.getHeight());

    float sampleCol = (float) alwidth / (float) width;
    float sampleRow = (float) alheight / (float) sequencesHeight;

    int lastcol = -1, lastrow = -1;
    int color = Color.white.getRGB();
    int row, col;
    jalview.datamodel.SequenceI seq;
    boolean hiddenRow = false;
    for (row = 0; row < sequencesHeight; row++)
    {
      if ( (int) (row * sampleRow) == lastrow)
      {
        //No need to recalculate the colours,
        //Just copy from the row above
        for (col = 0; col < width; col++)
        {
          miniMe.setRGB(col, row, miniMe.getRGB(col, row - 1));
        }
        continue;
      }

      lastrow = (int) (row * sampleRow);

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

      if (seq == null)
      {
        System.out.println(lastrow + " null");
        continue;
      }

      for (col = 0; col < width; col++)
      {
        if ( (int) (col * sampleCol) == lastcol &&
            (int) (row * sampleRow) == lastrow)
        {
          miniMe.setRGB(col, row, color);
          continue;
        }

        lastcol = (int) (col * sampleCol);

        if (seq.getLength() > lastcol)
        {
          color = sr.getResidueBoxColour(
              seq, lastcol).getRGB();

          if (av.showSequenceFeatures)
          {
            color = fr.findFeatureColour(color, seq, lastcol);
          }
        }
        else
        {
          color = -1; //White
        }

        if (hiddenRow ||
            (av.hasHiddenColumns && !av.getColumnSelection().isVisible(lastcol)))
        {
          color = new Color(color).darker().darker().getRGB();
        }

        miniMe.setRGB(col, row, color);

      }
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

  /**
   * DOCUMENT ME!
   */
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

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void paintComponent(Graphics g)
  {
    if(resizing)
    {
      g.setColor(Color.white);
      g.fillRect(0,0,getWidth(),getHeight());
    }
    else if (miniMe != null)
    {
      g.drawImage(miniMe, 0, 0, this);
    }

    g.setColor(Color.red);
    g.drawRect(boxX, boxY, boxWidth, boxHeight);
    g.drawRect(boxX + 1, boxY + 1, boxWidth - 2, boxHeight - 2);

  }
}
