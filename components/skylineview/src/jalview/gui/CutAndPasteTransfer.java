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
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.*;

import jalview.datamodel.*;
import jalview.io.*;
import jalview.jbgui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class CutAndPasteTransfer
    extends GCutAndPasteTransfer
{

  AlignViewport viewport;

  public CutAndPasteTransfer()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        textarea.requestFocus();
      }
    });

  }

  /**
   * DOCUMENT ME!
   */
  public void setForInput(AlignViewport viewport)
  {
    this.viewport = viewport;
    if (viewport != null)
    {
      ok.setText("Add");
    }

    getContentPane().add(inputButtonPanel, java.awt.BorderLayout.SOUTH);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getText()
  {
    return textarea.getText();
  }

  /**
   * DOCUMENT ME!
   *
   * @param text DOCUMENT ME!
   */
  public void setText(String text)
  {
    textarea.setText(text);
  }

  public void appendText(String text)
  {
    textarea.append(text);
  }

  public void save_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
        jalview.bin.Cache.getProperty(
            "LAST_DIRECTORY"));

    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Save Text to File");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      try
      {
        java.io.PrintWriter out = new java.io.PrintWriter(
            new java.io.FileWriter(chooser.getSelectedFile()));

        out.print(getText());
        out.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }

    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void copyItem_actionPerformed(ActionEvent e)
  {
    textarea.getSelectedText();
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    c.setContents(new StringSelection(textarea.getSelectedText()), null);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void pasteMenu_actionPerformed(ActionEvent e)
  {
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = c.getContents(this);

    if (contents == null)
    {
      return;
    }

    try
    {
      textarea.append( (String) contents.getTransferData(
          DataFlavor.stringFlavor));
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {
    String format = new IdentifyFile().Identify(getText(), "Paste");
    Alignment al = null;

    if (FormatAdapter.isValidFormat(format))
    {
      try
      {
        al = new FormatAdapter().readFile(getText(), "Paste", format);
      }
      catch (java.io.IOException ex)
      {
        JOptionPane.showInternalMessageDialog(Desktop.desktop,
                                              "Couldn't read the pasted text.\n" +
                                              ex.toString(),
                                              "Error parsing text",
                                              JOptionPane.WARNING_MESSAGE);
      }
    }

    if (al != null)
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
        AlignFrame af = new AlignFrame(al,
                                       AlignFrame.DEFAULT_WIDTH,
                                       AlignFrame.DEFAULT_HEIGHT);
        af.currentFileFormat = format;
        Desktop.addInternalFrame(af, "Cut & Paste input - " + format,
                                 AlignFrame.DEFAULT_WIDTH,
                                 AlignFrame.DEFAULT_HEIGHT);
        af.statusBar.setText("Successfully pasted alignment file");

        try
        {
          af.setMaximum(jalview.bin.Cache.getDefault("SHOW_FULLSCREEN", false));
        }
        catch (Exception ex)
        {
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
    try
    {
      this.setClosed(true);
    }
    catch (Exception ex)
    {
    }
  }

  public void textarea_mousePressed(MouseEvent e)
  {
    if (SwingUtilities.isRightMouseButton(e))
    {
      JPopupMenu popup = new JPopupMenu("Edit");
      JMenuItem item = new JMenuItem("Copy");
      item.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          copyItem_actionPerformed(e);
        }
      });
      popup.add(item);
      item = new JMenuItem("Paste");
      item.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          pasteMenu_actionPerformed(e);
        }
      });
      popup.add(item);
      popup.show(this, e.getX() + 10, e.getY() + textarea.getY() + 40);

    }
  }

}
