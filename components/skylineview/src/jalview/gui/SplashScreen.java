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
public class SplashScreen
    extends JPanel implements Runnable
{
  boolean visible = true;
  JInternalFrame iframe;
  Image image;
  int fontSize = 11;
  int yoffset = 30;

  /**
   * Creates a new SplashScreen object.
   *
   * @param iframe DOCUMENT ME!
   * @param i DOCUMENT ME!
   */
  public SplashScreen()
  {
    try
    {
      java.net.URL url = getClass().getResource("/images/logo.gif");

      if (url != null)
      {
        image = java.awt.Toolkit.getDefaultToolkit().createImage(url);

        MediaTracker mt = new MediaTracker(this);
        mt.addImage(image, 0);
        mt.waitForID(0);
        Desktop.instance.setIconImage(image);
      }
    }
    catch (Exception ex)
    {
    }

    Thread t = new Thread(this);
    t.start();
    addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent evt)
      {
        try
        {
          closeSplash();
        }
        catch (Exception ex)
        {
        }
      }
    });

    iframe = new JInternalFrame();
    iframe.setFrameIcon(null);
    iframe.setClosable(false);
    iframe.setContentPane(this);
    iframe.setLayer(JLayeredPane.PALETTE_LAYER);

    Desktop.desktop.add(iframe);

    iframe.setVisible(true);
    iframe.setBounds( (int) ( (Desktop.instance.getWidth() - 750) / 2),
                      (int) ( (Desktop.instance.getHeight() - 160) / 2),
                      750, 160);
  }

  /**
   * DOCUMENT ME!
   */
  public void run()
  {
    long startTime = System.currentTimeMillis() / 1000;

    while (visible)
    {
      try
      {
        Thread.sleep(1000);
      }
      catch (Exception ex)
      {
      }

      if ( ( (System.currentTimeMillis() / 1000) - startTime) > 5)
      {
        visible = false;
      }
      else
        repaint();
    }

    closeSplash();
  }

  /**
   * DOCUMENT ME!
   */
  public void closeSplash()
  {
    try
    {

      iframe.setClosed(true);
    }
    catch (Exception ex)
    {  }
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
    g.setColor(Color.black);
    g.setFont(new Font("Verdana", Font.BOLD, fontSize + 6));

    if (image != null)
    {
      g.drawImage(image, 5, yoffset + 12, this);
    }

    int y = yoffset;

    g.drawString("Jalview " + jalview.bin.Cache.getProperty("VERSION"), 50, y);

    FontMetrics fm = g.getFontMetrics();
    int vwidth = fm.stringWidth("Jalview " +
                                jalview.bin.Cache.getProperty("VERSION"));
    g.setFont(new Font("Verdana", Font.BOLD, fontSize + 2));
    g.drawString("Last updated: " +
                 jalview.bin.Cache.getDefault("BUILD_DATE", "unknown"),
                 50 + vwidth + 5, y);

    if (jalview.bin.Cache.getDefault("LATEST_VERSION",
        "Checking").equals("Checking"))
    {
      // Displayed when code version and jnlp version do not match
      g.drawString("...Checking latest version...",
                   50, y += fontSize + 10);
      y += 5;
      g.setColor(Color.black);
    }
    else if (!jalview.bin.Cache.getDefault("LATEST_VERSION", "Checking").equals(
        jalview.bin.Cache.getProperty("VERSION")))
    {
      // Displayed when code version and jnlp version do not match
      g.setColor(Color.red);
      g.drawString("!! Jalview version " +
                   jalview.bin.Cache.getDefault("LATEST_VERSION",
                                                "..Checking..")
                   +
                   " is available for download from http://www.jalview.org !!",
                   50, y += fontSize + 10);
      y += 5;
      g.setColor(Color.black);
    }

    g.setFont(new Font("Verdana", Font.BOLD, fontSize));
    g.drawString("Authors: Michele Clamp, James Cuff, Steve Searle, Andrew Waterhouse, Jim Procter & Geoff Barton.",
                 50, y += fontSize + 4);
    g.drawString("Current development managed by Andrew Waterhouse; Barton Group, University of Dundee.",
                 50, y += fontSize + 4);
    g.drawString("If  you use JalView, please cite: Clamp, M., Cuff, J., Searle, S. M. and Barton, G. J. (2004),",
                 50, y += fontSize + 4);
    g.drawString(
        "\"The Jalview Java Alignment Editor\" Bioinformatics,  2004 20; 426-7.",
        50, y += fontSize + 4);
  }
}
