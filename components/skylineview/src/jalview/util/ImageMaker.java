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
package jalview.util;

import java.io.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.image.*;

import org.jibble.epsgraphics.*;
import jalview.gui.*;
import jalview.io.*;

public class ImageMaker
{
  public static final int EPS = 0;
  public static final int PNG = 1;
  int type = -1;

  EpsGraphics2D pg;
  Graphics graphics;
  FileOutputStream out;
  BufferedImage bi;

  public ImageMaker(Component parent, int type, String title,
                    int width, int height, File file, String EPStitle)
  {
    this.type = type;

    if (file == null)
    {
      JalviewFileChooser chooser;
      chooser = type == EPS ? getEPSChooser() : getPNGChooser();

      chooser.setFileView(new jalview.io.JalviewFileView());
      chooser.setDialogTitle(title);
      chooser.setToolTipText("Save");

      int value = chooser.showSaveDialog(parent);

      if (value == jalview.io.JalviewFileChooser.APPROVE_OPTION)
      {
        jalview.bin.Cache.setProperty("LAST_DIRECTORY",
                                      chooser.getSelectedFile().getParent());

        file = chooser.getSelectedFile();
      }
    }

    if (file != null)
    {
      try
      {
        out = new FileOutputStream(file);

        if (type == EPS)
        {
          setupEPS(width, height, EPStitle);
        }
        else
        {
          setupPNG(width, height);
        }
      }
      catch (Exception ex)
      {
        System.out.println("Error creating " + (type == EPS ? "EPS" : "PNG") +
                           " file.");
      }
    }
  }

  public Graphics getGraphics()
  {
    return graphics;
  }

  void setupPNG(int width, int height)
  {
    bi = new BufferedImage(width, height,
                           BufferedImage.TYPE_INT_RGB);
    graphics = bi.getGraphics();
    Graphics2D ig2 = (Graphics2D) graphics;
    ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                         RenderingHints.VALUE_ANTIALIAS_ON);
  }

  public void writeImage()
  {
    try
    {
      switch (type)
      {
        case EPS:
          pg.flush();
          pg.close();
          break;
        case PNG:
          ImageIO.write(bi, "png", out);
          out.close();
          break;
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  void setupEPS(int width, int height, String title)
  {
    boolean accurateText = true;

    String renderStyle = jalview.bin.Cache.getDefault("EPS_RENDERING",
        "Prompt each time");

    // If we need to prompt, and if the GUI is visible then
    // Prompt for EPS rendering style
    if (renderStyle.equalsIgnoreCase("Prompt each time")
        && !
        (System.getProperty("java.awt.headless") != null
         && System.getProperty("java.awt.headless").equals("true")))
    {
      EPSOptions eps = new EPSOptions();
      renderStyle = eps.getValue();

      if (renderStyle == null || eps.cancelled)
      {
        return;
      }
    }

    if (renderStyle.equalsIgnoreCase("text"))
    {
      accurateText = false;
    }

    try
    {
      pg = new EpsGraphics2D(title, out, 0, 0, width,
                             height);
      Graphics2D ig2 = (Graphics2D) pg;
      ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);

      pg.setAccurateTextMode(accurateText);

      graphics = pg;
    }
    catch (Exception ex)
    {}
  }

  JalviewFileChooser getPNGChooser()
  {
    return new jalview.io.JalviewFileChooser(jalview.bin.Cache.getProperty(
        "LAST_DIRECTORY"), new String[]
                                             {"png"},
                                             new String[]
                                             {"Portable network graphics"},
                                             "Portable network graphics");
  }

  JalviewFileChooser getEPSChooser()
  {
    return new jalview.io.JalviewFileChooser(jalview.bin.Cache.getProperty(
        "LAST_DIRECTORY"), new String[]
                                             {"eps"},
                                             new String[]
                                             {"Encapsulated Postscript"},
                                             "Encapsulated Postscript");
  }
}
