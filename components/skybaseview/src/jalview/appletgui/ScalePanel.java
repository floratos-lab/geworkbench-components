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

import jalview.datamodel.*;

public class ScalePanel
    extends Panel implements MouseMotionListener, MouseListener
{

  protected int offy = 4;
  public int width;

  protected AlignViewport av;
  AlignmentPanel ap;

  boolean stretchingGroup = false;
  int min; //used by mouseDragged to see if user
  int max; //used by mouseDragged to see if user
  boolean mouseDragging = false;
  int[] reveal;

  public ScalePanel(AlignViewport av, AlignmentPanel ap)
  {
    setLayout(null);
    this.av = av;
    this.ap = ap;

    addMouseListener(this);
    addMouseMotionListener(this);

  }

  public void mousePressed(MouseEvent evt)
  {
    int x = (evt.getX() / av.getCharWidth()) + av.getStartRes();
    final int res;

    if (av.hasHiddenColumns)
    {
      res = av.getColumnSelection().adjustForHiddenColumns(x);
    }
    else
    {
      res = x;
    }

    min = res;
    max = res;
    if ( (evt.getModifiers() & InputEvent.BUTTON3_MASK)
        == InputEvent.BUTTON3_MASK)
    {
      PopupMenu pop = new PopupMenu();
      if (reveal != null)
      {
        MenuItem item = new MenuItem("Reveal");
        item.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            av.showColumn(reveal[0]);
            reveal = null;
            ap.paintAlignment(true);
            if (ap.overviewPanel != null)
            {
              ap.overviewPanel.updateOverviewImage();
            }
          }
        });
        pop.add(item);

        if (av.getColumnSelection().getHiddenColumns().size() > 1)
        {
          item = new MenuItem("Reveal All");
          item.addActionListener(new ActionListener()
          {
            public void actionPerformed(ActionEvent e)
            {
              av.showAllHiddenColumns();
              reveal = null;
              ap.paintAlignment(true);
              if (ap.overviewPanel != null)
              {
                ap.overviewPanel.updateOverviewImage();
              }
            }
          });
          pop.add(item);
        }
        this.add(pop);
        pop.show(this, evt.getX(), evt.getY());
      }
      else if (av.getColumnSelection().contains(res))
      {
        MenuItem item = new MenuItem("Hide Columns");
        item.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            av.hideColumns(res, res);
            if (av.getSelectionGroup() != null
                &&
                av.getSelectionGroup().getSize() == av.alignment.getHeight())
            {
              av.setSelectionGroup(null);
            }

            ap.paintAlignment(true);
            if (ap.overviewPanel != null)
            {
              ap.overviewPanel.updateOverviewImage();
            }
          }
        });
        pop.add(item);
        this.add(pop);
        pop.show(this, evt.getX(), evt.getY());
      }
    }
    else // LEFT MOUSE TO SELECT
    {
      if (!evt.isControlDown() && !evt.isShiftDown())
      {
        av.getColumnSelection().clear();
      }

      av.getColumnSelection().addElement(res);
      SequenceGroup sg = new SequenceGroup();
      for (int i = 0; i < av.alignment.getSequences().size(); i++)
      {
        sg.addSequence(av.alignment.getSequenceAt(i), false);
      }

      sg.setStartRes(res);
      sg.setEndRes(res);
      av.setSelectionGroup(sg);

      if (evt.isShiftDown())
      {
        int min = Math.min(av.getColumnSelection().getMin(), res);
        int max = Math.max(av.getColumnSelection().getMax(), res);
        for (int i = min; i < max; i++)
        {
          av.getColumnSelection().addElement(i);
        }
        sg.setStartRes(min);
        sg.setEndRes(max);
      }
    }

    ap.paintAlignment(true);
  }

  public void mouseReleased(MouseEvent evt)
  {
    mouseDragging = false;

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    if (res > av.alignment.getWidth())
    {
      res = av.alignment.getWidth() - 1;
    }

    if (av.hasHiddenColumns)
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (!stretchingGroup)
    {
      ap.paintAlignment(false);

      return;
    }

    SequenceGroup sg = av.getSelectionGroup();

    if (res > sg.getStartRes())
    {
      sg.setEndRes(res);
    }
    else if (res < sg.getStartRes())
    {
      sg.setStartRes(res);
    }

    stretchingGroup = false;
    ap.paintAlignment(false);
  }

  public void mouseDragged(MouseEvent evt)
  {
    mouseDragging = true;

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();
    if (res < 0)
    {
      res = 0;
    }

    if (av.hasHiddenColumns)
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (res > av.alignment.getWidth())
    {
      res = av.alignment.getWidth() - 1;
    }

    if (res < min)
    {
      min = res;
    }

    if (res > max)
    {
      max = res;
    }

    SequenceGroup sg = av.getSelectionGroup();

    if (sg != null)
    {
      stretchingGroup = true;

      if (!av.getColumnSelection().contains(res))
      {
        av.getColumnSelection().addElement(res);
      }

      if (res > sg.getStartRes())
      {
        sg.setEndRes(res);
      }
      if (res < sg.getStartRes())
      {
        sg.setStartRes(res);
      }

      int col;
      for (int i = min; i <= max; i++)
      {
        col = av.getColumnSelection().adjustForHiddenColumns(i);

        if ( (col < sg.getStartRes()) || (col > sg.getEndRes()))
        {
          av.getColumnSelection().removeElement(col);
        }
        else
        {
          av.getColumnSelection().addElement(col);
        }
      }

      ap.paintAlignment(false);
    }
  }

  public void mouseEntered(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.seqPanel.scrollCanvas(null);
    }
  }

  public void mouseExited(MouseEvent evt)
  {
    if (mouseDragging)
    {
      ap.seqPanel.scrollCanvas(evt);
    }
  }

  public void mouseClicked(MouseEvent evt)
  {

  }

  public void mouseMoved(MouseEvent evt)
  {
    if (!av.hasHiddenColumns)
    {
      return;
    }

    int res = (evt.getX() / av.getCharWidth()) + av.getStartRes();

    res = av.getColumnSelection().adjustForHiddenColumns(res);

    reveal = null;
    for (int i = 0; i < av.getColumnSelection().getHiddenColumns().size(); i++)
    {
      int[] region = (int[]) av.getColumnSelection().getHiddenColumns().
          elementAt(i);
      if (res + 1 == region[0] || res - 1 == region[1])
      {
        reveal = region;
        break;
      }
    }

    repaint();
  }

  public void update(Graphics g)
  {
    paint(g);
  }

  public void paint(Graphics g)
  {
    drawScale(g, av.getStartRes(), av.getEndRes(), getSize().width,
              getSize().height);
  }

// scalewidth will normally be screenwidth,
  public void drawScale(Graphics gg, int startx, int endx, int width,
                        int height)
  {
    gg.setFont(av.getFont());

    //Fill in the background
    gg.setColor(Color.white);
    gg.fillRect(0, 0, width, height);
    gg.setColor(Color.black);

    //Fill the selected columns
    ColumnSelection cs = av.getColumnSelection();
    gg.setColor(new Color(220, 0, 0));

    for (int i = 0; i < cs.size(); i++)
    {
      int sel = cs.columnAt(i);
      if (av.hasHiddenColumns)
      {
        sel = av.getColumnSelection().findColumnPosition(sel);
      }

      if ( (sel >= startx) && (sel <= endx))
      {
        gg.fillRect( (sel - startx) * av.charWidth, 0, av.charWidth,
                    getSize().height);
      }
    }

    // Draw the scale numbers
    gg.setColor(Color.black);

    int scalestartx = (startx / 10) * 10;

    FontMetrics fm = gg.getFontMetrics(av.getFont());
    int y = av.charHeight - fm.getDescent();

    if ( (scalestartx % 10) == 0)
    {
      scalestartx += 5;
    }

    String string;
    int maxX = 0;

    for (int i = scalestartx; i < endx; i += 5)
    {
      if ( (i % 10) == 0)
      {
        string = String.valueOf(av.getColumnSelection().adjustForHiddenColumns(
            i));
        if ( (i - startx - 1) * av.charWidth > maxX)
        {
          gg.drawString(string,
                        (i - startx - 1) * av.charWidth, y);
          maxX = (i - startx + 1) * av.charWidth + fm.stringWidth(string);
        }

        gg.drawLine( (int) ( ( (i - startx - 1) * av.charWidth) +
                            (av.charWidth / 2)), y + 2,
                    (int) ( ( (i - startx - 1) * av.charWidth) +
                           (av.charWidth / 2)),
                    y + (fm.getDescent() * 2));

      }
      else
      {
        gg.drawLine( (int) ( ( (i - startx - 1) * av.charWidth) +
                            (av.charWidth / 2)), y + fm.getDescent(),
                    (int) ( ( (i - startx - 1) * av.charWidth) +
                           (av.charWidth / 2)), y + (fm.getDescent() * 2));
      }
    }

    if (av.hasHiddenColumns)
    {
      gg.setColor(Color.blue);
      int res;
      if (av.getShowHiddenMarkers())
      {
        for (int i = 0; i < av.getColumnSelection().getHiddenColumns().size();
             i++)
        {

          res = av.getColumnSelection().findHiddenRegionPosition(i) -
              startx;

          if (res < 0 || res > endx - scalestartx)
          {
            continue;
          }

          gg.fillPolygon(new int[]
                         {res * av.charWidth - av.charHeight / 4,
                         res * av.charWidth + av.charHeight / 4,
                         res * av.charWidth},
                         new int[]
                         {
                         y - av.charHeight / 2, y - av.charHeight / 2,
                         y + 8
          }, 3);

        }
      }

      if (reveal != null && reveal[0] > startx && reveal[0] < endx)
      {
        gg.drawString("Reveal Columns", reveal[0] * av.charWidth, 0);
      }
    }

  }

}
