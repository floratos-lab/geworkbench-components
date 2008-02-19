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

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jalview.datamodel.*;
import jalview.math.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class RotatableCanvas
    extends JPanel implements MouseListener,
    MouseMotionListener, KeyListener
{
  RotatableMatrix idmat = new RotatableMatrix(3, 3);
  RotatableMatrix objmat = new RotatableMatrix(3, 3);
  RotatableMatrix rotmat = new RotatableMatrix(3, 3);

  //RubberbandRectangle rubberband;
  boolean drawAxes = true;
  int omx = 0;
  int mx = 0;
  int omy = 0;
  int my = 0;
  Image img;
  Graphics ig;
  Dimension prefsize;
  float[] centre = new float[3];
  float[] width = new float[3];
  float[] max = new float[3];
  float[] min = new float[3];
  float maxwidth;
  float scale;
  int npoint;
  Vector points;
  float[][] orig;
  float[][] axes;
  int startx;
  int starty;
  int lastx;
  int lasty;
  int rectx1;
  int recty1;
  int rectx2;
  int recty2;
  float scalefactor = 1;
  AlignViewport av;
  AlignmentPanel ap;
  boolean showLabels = false;
  Color bgColour = Color.black;
  boolean applyToAllViews = false;

  //  Controller    controller;
  public RotatableCanvas(AlignmentPanel ap)
  {
    this.av = ap.av;
    this.ap = ap;

    addMouseWheelListener(new MouseWheelListener()
    {
      public void mouseWheelMoved(MouseWheelEvent e)
      {
        if (e.getWheelRotation() > 0)
        {
          scale = (float) (scale * 1.1);
          repaint();
        }

        else
        {
          scale = (float) (scale * 0.9);
          repaint();
        }
      }
    });

  }

  public void showLabels(boolean b)
  {
    showLabels = b;
    repaint();
  }

  public void setPoints(Vector points, int npoint)
  {
    this.points = points;
    this.npoint = npoint;
    ToolTipManager.sharedInstance().registerComponent(this);
    ToolTipManager.sharedInstance().setInitialDelay(0);
    ToolTipManager.sharedInstance().setDismissDelay(10000);

    prefsize = getPreferredSize();
    orig = new float[npoint][3];

    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = (SequencePoint) points.elementAt(i);

      for (int j = 0; j < 3; j++)
      {
        orig[i][j] = sp.coord[j];
      }
    }

    //Initialize the matrices to identity
    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < 3; j++)
      {
        if (i != j)
        {
          idmat.addElement(i, j, 0);
          objmat.addElement(i, j, 0);
          rotmat.addElement(i, j, 0);
        }
        else
        {
          idmat.addElement(i, j, 0);
          objmat.addElement(i, j, 0);
          rotmat.addElement(i, j, 0);
        }
      }
    }

    axes = new float[3][3];
    initAxes();

    findCentre();
    findWidth();

    scale = findScale();

    addMouseListener(this);

    addMouseMotionListener(this);

  }

  public void initAxes()
  {
    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < 3; j++)
      {
        if (i != j)
        {
          axes[i][j] = 0;
        }
        else
        {
          axes[i][j] = 1;
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void findWidth()
  {
    max = new float[3];
    min = new float[3];

    max[0] = (float) - 1e30;
    max[1] = (float) - 1e30;
    max[2] = (float) - 1e30;

    min[0] = (float) 1e30;
    min[1] = (float) 1e30;
    min[2] = (float) 1e30;

    for (int i = 0; i < 3; i++)
    {
      for (int j = 0; j < npoint; j++)
      {
        SequencePoint sp = (SequencePoint) points.elementAt(j);

        if (sp.coord[i] >= max[i])
        {
          max[i] = sp.coord[i];
        }

        if (sp.coord[i] <= min[i])
        {
          min[i] = sp.coord[i];
        }
      }
    }

    //    System.out.println("xmax " + max[0] + " min " + min[0]);
    //System.out.println("ymax " + max[1] + " min " + min[1]);
    //System.out.println("zmax " + max[2] + " min " + min[2]);
    width[0] = Math.abs(max[0] - min[0]);
    width[1] = Math.abs(max[1] - min[1]);
    width[2] = Math.abs(max[2] - min[2]);

    maxwidth = width[0];

    if (width[1] > width[0])
    {
      maxwidth = width[1];
    }

    if (width[2] > width[1])
    {
      maxwidth = width[2];
    }

    //System.out.println("Maxwidth = " + maxwidth);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float findScale()
  {
    int dim;
    int width;
    int height;

    if (getWidth() != 0)
    {
      width = getWidth();
      height = getHeight();
    }
    else
    {
      width = prefsize.width;
      height = prefsize.height;
    }

    if (width < height)
    {
      dim = width;
    }
    else
    {
      dim = height;
    }

    return (float) ( (dim * scalefactor) / (2 * maxwidth));
  }

  /**
   * DOCUMENT ME!
   */
  public void findCentre()
  {
    //Find centre coordinate
    findWidth();

    centre[0] = (max[0] + min[0]) / 2;
    centre[1] = (max[1] + min[1]) / 2;
    centre[2] = (max[2] + min[2]) / 2;

    //    System.out.println("Centre x " + centre[0]);
    //System.out.println("Centre y " + centre[1]);
    //System.out.println("Centre z " + centre[2]);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Dimension getPreferredSize()
  {
    if (prefsize != null)
    {
      return prefsize;
    }
    else
    {
      return new Dimension(400, 400);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Dimension getMinimumSize()
  {
    return getPreferredSize();
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void paintComponent(Graphics g1)
  {

    Graphics2D g = (Graphics2D) g1;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);
    if (points == null)
    {
      g.setFont(new Font("Verdana", Font.PLAIN, 18));
      g.drawString("Calculating PCA....", 20, getHeight() / 2);
    }
    else
    {
      //Only create the image at the beginning -
      if ( (img == null) || (prefsize.width != getWidth()) ||
          (prefsize.height != getHeight()))
      {
        prefsize.width = getWidth();
        prefsize.height = getHeight();

        scale = findScale();

        //      System.out.println("New scale = " + scale);
        img = createImage(getWidth(), getHeight());
        ig = img.getGraphics();
      }

      drawBackground(ig, bgColour);
      drawScene(ig);

      if (drawAxes == true)
      {
        drawAxes(ig);
      }

      g.drawImage(img, 0, 0, this);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void drawAxes(Graphics g)
  {

    g.setColor(Color.yellow);

    for (int i = 0; i < 3; i++)
    {
      g.drawLine(getWidth() / 2, getHeight() / 2,
                 (int) ( (axes[i][0] * scale * max[0]) + (getWidth() / 2)),
                 (int) ( (axes[i][1] * scale * max[1]) + (getHeight() / 2)));
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param col DOCUMENT ME!
   */
  public void drawBackground(Graphics g, Color col)
  {
    g.setColor(col);
    g.fillRect(0, 0, prefsize.width, prefsize.height);
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   */
  public void drawScene(Graphics g1)
  {

    Graphics2D g = (Graphics2D) g1;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                       RenderingHints.VALUE_ANTIALIAS_ON);

    int halfwidth = getWidth() / 2;
    int halfheight = getHeight() / 2;

    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = (SequencePoint) points.elementAt(i);
      int x = (int) ( (float) (sp.coord[0] - centre[0]) * scale) +
          halfwidth;
      int y = (int) ( (float) (sp.coord[1] - centre[1]) * scale) +
          halfheight;
      float z = sp.coord[1] - centre[2];

      if (av.getSequenceColour(sp.sequence) == Color.black)
      {
        g.setColor(Color.white);
      }
      else
      {
        g.setColor(av.getSequenceColour(sp.sequence));
      }

      if (av.getSelectionGroup() != null)
      {
        if (av.getSelectionGroup().getSequences(null).contains(
            ( (SequencePoint) points.elementAt(i)).sequence))
        {
          g.setColor(Color.gray);
        }
      }

      if (z < 0)
      {
        g.setColor(g.getColor().darker());
      }

      g.fillRect(x - 3, y - 3, 6, 6);
      if (showLabels)
      {
        g.setColor(Color.red);
        g.drawString( ( (SequencePoint) points.elementAt(i)).sequence.
                     getName(),
                     x - 3, y - 4);
      }
    }

    //    //Now the rectangle
    //    if (rectx2 != -1 && recty2 != -1) {
    //      g.setColor(Color.white);
    //
    //      g.drawRect(rectx1,recty1,rectx2-rectx1,recty2-recty1);
    //    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Dimension minimumsize()
  {
    return prefsize;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Dimension preferredsize()
  {
    return prefsize;
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void keyTyped(KeyEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void keyReleased(KeyEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void keyPressed(KeyEvent evt)
  {
    if (evt.getKeyCode() == KeyEvent.VK_UP)
    {
      scalefactor = (float) (scalefactor * 1.1);
      scale = findScale();
    }
    else if (evt.getKeyCode() == KeyEvent.VK_DOWN)
    {
      scalefactor = (float) (scalefactor * 0.9);
      scale = findScale();
    }
    else if (evt.getKeyChar() == 's')
    {
      System.err.println("DEBUG: Rectangle selection"); // log.debug

      if ( (rectx2 != -1) && (recty2 != -1))
      {
        rectSelect(rectx1, recty1, rectx2, recty2);
      }
    }

    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseClicked(MouseEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseExited(MouseEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mousePressed(MouseEvent evt)
  {
    int x = evt.getX();
    int y = evt.getY();

    mx = x;
    my = y;

    omx = mx;
    omy = my;

    startx = x;
    starty = y;

    rectx1 = x;
    recty1 = y;

    rectx2 = -1;
    recty2 = -1;

    SequenceI found = findPoint(x, y);

    if (found != null)
    {
      AlignmentPanel[] aps = getAssociatedPanels();

      for (int a = 0; a < aps.length; a++)
      {
        if (aps[a].av.getSelectionGroup() != null)
        {
          aps[a].av.getSelectionGroup().addOrRemove(found, true);
        }
        else
        {
          aps[a].av.setSelectionGroup(new SequenceGroup());
          aps[a].av.getSelectionGroup().addOrRemove(found, true);
          aps[a].av.getSelectionGroup().setEndRes(
              aps[a].av.alignment.getWidth() - 1);
        }
      }

      PaintRefresher.Refresh(this, av.getSequenceSetId());
    }

    repaint();
  }

  // private void fireSequenceSelectionEvent(Selection sel) {
  //   controller.handleSequenceSelectionEvent(new SequenceSelectionEvent(this,sel));
  //}
  public void mouseMoved(MouseEvent evt)
  {
    SequenceI found = findPoint(evt.getX(), evt.getY());

    if (found != null)
    {
      this.setToolTipText(found.getName());
    }
    else
    {
      this.setToolTipText(null);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent evt)
  {
    mx = evt.getX();
    my = evt.getY();

    //Check if this is a rectangle drawing drag
    if ( (evt.getModifiers() & InputEvent.BUTTON2_MASK) != 0)
    {
      //      rectx2 = evt.getX();
      //      recty2 = evt.getY();
    }
    else
    {
      rotmat.setIdentity();

      rotmat.rotate( (float) (my - omy), 'x');
      rotmat.rotate( (float) (mx - omx), 'y');

      for (int i = 0; i < npoint; i++)
      {
        SequencePoint sp = (SequencePoint) points.elementAt(i);
        sp.coord[0] -= centre[0];
        sp.coord[1] -= centre[1];
        sp.coord[2] -= centre[2];

        //Now apply the rotation matrix
        sp.coord = rotmat.vectorMultiply(sp.coord);

        //Now translate back again
        sp.coord[0] += centre[0];
        sp.coord[1] += centre[1];
        sp.coord[2] += centre[2];
      }

      for (int i = 0; i < 3; i++)
      {
        axes[i] = rotmat.vectorMultiply(axes[i]);
      }

      omx = mx;
      omy = my;

      paint(this.getGraphics());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param x1 DOCUMENT ME!
   * @param y1 DOCUMENT ME!
   * @param x2 DOCUMENT ME!
   * @param y2 DOCUMENT ME!
   */
  public void rectSelect(int x1, int y1, int x2, int y2)
  {
    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = (SequencePoint) points.elementAt(i);
      int tmp1 = (int) ( ( (sp.coord[0] - centre[0]) * scale) +
                        ( (float) getWidth() / 2.0));
      int tmp2 = (int) ( ( (sp.coord[1] - centre[1]) * scale) +
                        ( (float) getHeight() / 2.0));

      if ( (tmp1 > x1) && (tmp1 < x2) && (tmp2 > y1) && (tmp2 < y2))
      {
        if (av != null)
        {
          if (!av.getSelectionGroup().getSequences(null).contains(sp.sequence))
          {
            av.getSelectionGroup().addSequence(sp.sequence, true);
          }
        }
      }
    }

    // if (changedSel) {
    //    fireSequenceSelectionEvent(av.getSelection());
    // }
  }

  /**
   * DOCUMENT ME!
   *
   * @param x DOCUMENT ME!
   * @param y DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceI findPoint(int x, int y)
  {
    int halfwidth = getWidth() / 2;
    int halfheight = getHeight() / 2;

    int found = -1;

    for (int i = 0; i < npoint; i++)
    {
      SequencePoint sp = (SequencePoint) points.elementAt(i);
      int px = (int) ( (float) (sp.coord[0] - centre[0]) * scale) +
          halfwidth;
      int py = (int) ( (float) (sp.coord[1] - centre[1]) * scale) +
          halfheight;

      if ( (Math.abs(px - x) < 3) && (Math.abs(py - y) < 3))
      {
        found = i;
      }
    }

    if (found != -1)
    {
      return ( (SequencePoint) points.elementAt(found)).sequence;
    }
    else
    {
      return null;
    }
  }

  AlignmentPanel[] getAssociatedPanels()
  {
    if (applyToAllViews)
    {
      return PaintRefresher.getAssociatedPanels(av.getSequenceSetId());
    }
    else
    {
      return new AlignmentPanel[]
          {
          ap};
    }
  }
}
