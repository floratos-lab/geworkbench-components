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

package jalview.io;

import java.util.*;

import javax.swing.*;

import jalview.datamodel.*;
import jalview.gui.*;

public class FileLoader
    implements Runnable
{
  String file;
  String protocol;
  String format;
  AlignViewport viewport;
  AlignFrame alignFrame;

  public void LoadFile(AlignViewport viewport, String file, String protocol,
                       String format)
  {
    this.viewport = viewport;
    LoadFile(file, protocol, format);
  }

  public void LoadFile(String file, String protocol, String format)
  {
    this.file = file;
    this.protocol = protocol;
    this.format = format;

    final Thread loader = new Thread(this);

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        loader.start();
      }
    });
  }

  public AlignFrame LoadFileWaitTillLoaded(String file, String protocol,
                                           String format)
  {
    this.file = file;
    this.protocol = protocol;
    this.format = format;

    Thread loader = new Thread(this);
    loader.start();

    while (loader.isAlive())
    {
      try
      {
        Thread.sleep(500);
      }
      catch (Exception ex)
      {}
    }

    return alignFrame;
  }

  public void updateRecentlyOpened()
  {
    Vector recent = new Vector();

    String type = protocol.equals(FormatAdapter.FILE)
        ? "RECENT_FILE" : "RECENT_URL";

    String historyItems = jalview.bin.Cache.getProperty(type);

    StringTokenizer st;

    if (historyItems != null)
    {
      st = new StringTokenizer(historyItems, "\t");

      while (st.hasMoreTokens())
      {
        recent.addElement(st.nextElement().toString().trim());
      }
    }

    if (recent.contains(file))
    {
      recent.remove(file);
    }

    StringBuffer newHistory = new StringBuffer(file);
    for (int i = 0; i < recent.size() && i < 10; i++)
    {
      newHistory.append("\t");
      newHistory.append(recent.elementAt(i));
    }

    jalview.bin.Cache.setProperty(type, newHistory.toString());

    if (protocol.equals(FormatAdapter.FILE))
    {
      jalview.bin.Cache.setProperty("DEFAULT_FILE_FORMAT", format);
    }
  }

  public void run()
  {
    String title = protocol.equals(AppletFormatAdapter.PASTE)
        ? "Copied From Clipboard" : file;

    try
    {
      if (Desktop.instance != null)
      {
        Desktop.instance.startLoading(file);
      }

      Alignment al = null;

      if (format.equalsIgnoreCase("Jalview"))
      {
        alignFrame = new Jalview2XML().LoadJalviewAlign(file);
      }
      else
      {
        String error = AppletFormatAdapter.SUPPORTED_FORMATS;

        if (FormatAdapter.isValidFormat(format))
        {
          try
          {
            al = new FormatAdapter().readFile(file, protocol, format);
          }
          catch (java.io.IOException ex)
          {
            error = ex.getMessage();
          }
        }

        if ( (al != null) && (al.getHeight() > 0))
        {
          if (viewport != null)
          {
            for (int i = 0; i < al.getHeight(); i++)
            {
              viewport.getAlignment().addSequence(al.getSequenceAt(i));
            }
            viewport.firePropertyChange("alignment", null,
                                        viewport.getAlignment().getSequences());

          }
          else
          {
            alignFrame = new AlignFrame(al,
                                        AlignFrame.DEFAULT_WIDTH,
                                        AlignFrame.DEFAULT_HEIGHT);

            alignFrame.statusBar.setText("Successfully loaded file " + title);

            if (!protocol.equals(AppletFormatAdapter.PASTE))
              alignFrame.setFileName(file, format);

	    /*            Desktop.addInternalFrame(alignFrame, title,
                                     AlignFrame.DEFAULT_WIDTH,
                                     AlignFrame.DEFAULT_HEIGHT);
	    */

            try
            {
              alignFrame.setMaximum(jalview.bin.Cache.getDefault(
                  "SHOW_FULLSCREEN", false));
            }
            catch (java.beans.PropertyVetoException ex)
            {
            }
          }
        }
        else
        {
          if (Desktop.instance != null)
          {
            Desktop.instance.stopLoading();
          }

          final String errorMessage = "Couldn't load file " + title + "\n" +
              error;

          javax.swing.SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              JOptionPane.showInternalMessageDialog(Desktop.desktop,
                  errorMessage,
                  "Error loading file",
                  JOptionPane.WARNING_MESSAGE);
            }
          });
        }
      }

      //      updateRecentlyOpened();

    }
    catch (OutOfMemoryError er)
    {

      er.printStackTrace();
      alignFrame = null;

      javax.swing.SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          javax.swing.JOptionPane.showInternalMessageDialog(Desktop.desktop,
              "Out of memory loading file " + file + "!!"
              +
              "\nSee help files for increasing Java Virtual Machine memory."
              , "Out of memory",
              javax.swing.JOptionPane.WARNING_MESSAGE);
        }
      });
    }

    System.gc();
    if (Desktop.instance != null)
    {
      Desktop.instance.stopLoading();
    }

  }

}
