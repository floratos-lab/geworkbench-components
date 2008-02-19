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
import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class IdwidthAdjuster
    extends JPanel implements MouseListener,
    MouseMotionListener
{
  boolean active = false;
  int oldX = 0;
  Image image;
  AlignmentPanel ap;

  /**
   * Creates a new IdwidthAdjuster object.
   *
   * @param ap DOCUMENT ME!
   */
  public IdwidthAdjuster(AlignmentPanel ap)
  {
    this.ap = ap;

    java.net.URL url = getClass().getResource("/images/idwidth.gif");

    if (url != null)
    {
      image = java.awt.Toolkit.getDefaultToolkit().createImage(url);
    }

    addMouseListener(this);
    addMouseMotionListener(this);
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mousePressed(MouseEvent evt)
  {
    oldX = evt.getX();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent evt)
  {
    active = false;
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent evt)
  {
    active = true;
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseExited(MouseEvent evt)
  {
    active = false;
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent evt)
  {
    active = true;

    Dimension d = ap.idPanel.idCanvas.getPreferredSize();
    int dif = evt.getX() - oldX;

    if ( ( (d.width + dif) > 20) || (dif > 0))
    {
      ap.idPanel.idCanvas.setPreferredSize(new Dimension(d.width + dif,
          d.height));
      ap.paintAlignment(true);
    }

    oldX = evt.getX();
  }

  /**
   * DOCUMENT ME!
   *
   * @param evt DOCUMENT ME!
   */
  public void mouseMoved(MouseEvent evt)
  {
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
   * @param g DOCUMENT ME!
   */
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    if (active)
    {
      if (image != null)
      {
        g.drawImage(image, getWidth() - 20, 2, this);
      }
    }
  }
}
