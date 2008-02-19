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

import java.applet.*;
import java.util.*;

import java.awt.*;
import java.awt.event.*;

public class Tooltip
    extends Canvas implements MouseListener,
    MouseMotionListener
{
  private String[] tip;
  private String lastTip = "";
  private boolean setPosition = false;
  protected Component owner;

  private Container mainContainer;
  private LayoutManager mainLayout;

  private boolean shown;

  private final int VERTICAL_OFFSET = 20;
  private final int HORIZONTAL_ENLARGE = 10;

  int fontHeight = 0;

  Image linkImage;

  FontMetrics fm;

  public Tooltip(String tip, Component owner)
  {
    this.owner = owner;
    owner.addMouseListener(this);
    owner.addMouseMotionListener(this);
    setBackground(new Color(255, 255, 220));
    setTip(tip);
    java.net.URL url = getClass().getResource("/images/link.gif");
    if (url != null)
    {
      linkImage = java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }
  }

  public void paint(Graphics g)
  {
    int w = getSize().width;
    int h = getSize().height;

    g.drawRect(0, 0, w - 1, h - 1);
    int lindex, x;
    for (int i = 0; i < tip.length; i++)
    {
      x = 3;
      lindex = tip[i].indexOf("%LINK%");
      if (lindex != -1)
      {
        if (lindex > 0)
        {
          g.drawString(tip[i].substring(0, lindex), 3, (i + 1) * fontHeight - 3);
          x += fm.stringWidth(tip[i].substring(0, lindex) + 3);
        }
        g.drawImage(linkImage, x, i * fontHeight + 1, this);
        if (lindex + 6 < tip[i].length())
        {
          g.drawString(tip[i].substring(lindex + 6),
                       x + linkImage.getWidth(this),
                       (i + 1) * fontHeight - 3);
        }
      }
      else
      {
        g.drawString(tip[i], 3, (i + 1) * fontHeight - 3);
      }
    }
  }

  synchronized void setTip(String tip)
  {
    if (tip == null)
    {
      setTip("");
      return;
    }

    if (lastTip.equals(tip))
    {
      return;
    }

    lastTip = tip;
    setPosition = true;

    fm = getFontMetrics(owner.getFont());
    fontHeight = fm.getHeight();

    int longestLine = 0;
    StringTokenizer st = new StringTokenizer(tip, "\n");
    this.tip = new String[st.countTokens()];
    int index = 0;
    while (st.hasMoreElements())
    {
      this.tip[index] = st.nextToken();
      if (fm.stringWidth(this.tip[index]) > longestLine)
      {
        longestLine = fm.stringWidth(this.tip[index]);
      }
      index++;
    }

    setSize(longestLine + HORIZONTAL_ENLARGE,
            fontHeight * this.tip.length);

    repaint();

  }

  void setTipLocation(MouseEvent evt)
  {
    if(mainContainer==null || owner==null)
    {
      return;
    }
    setLocation( (owner.getLocationOnScreen().x
                  - mainContainer.getLocationOnScreen().x) + evt.getX(),
                (owner.getLocationOnScreen().y -
                 mainContainer.getLocationOnScreen().y
                 + VERTICAL_OFFSET) + evt.getY());

    // correction, whole tool tip must be visible
    if (mainContainer.getSize().width < (getLocation().x + getSize().width))
    {
      setLocation(mainContainer.getSize().width - getSize().width,
                  getLocation().y);
    }
  }

  private void removeToolTip()
  {
    if (shown)
    {
      mainContainer.remove(0);
      mainContainer.setLayout(mainLayout);
      mainContainer.validate();
    }
    shown = false;
  }

  private void findMainContainer()
  {
    Container parent = owner.getParent();
    while (true)
    {
      if ( (parent instanceof Applet) || (parent instanceof Frame))
      {
        mainContainer = parent;
        break;
      }
      else
      {
        parent = parent.getParent();
      }
    }
    mainLayout = mainContainer.getLayout();
  }

  public void mouseEntered(MouseEvent me)
  {
    setTipLocation(me);
  }

  public void mouseExited(MouseEvent me)
  {
    removeToolTip();
  }

  public void mousePressed(MouseEvent me)
  {
    removeToolTip();
  }

  public void mouseReleased(MouseEvent me)
  {}

  public void mouseClicked(MouseEvent me)
  {}

  public void mouseMoved(MouseEvent me)
  {
    if (!shown)
    {
      findMainContainer();
      mainContainer.setLayout(null);
      mainContainer.add(this, 0);
      mainContainer.validate();
      shown = true;
      setTipLocation(me);
    }
    else if (setPosition)
    {
      setTipLocation(me);
      setPosition = false;
    }
  }

  public void mouseDragged(MouseEvent me)
  {}
}
