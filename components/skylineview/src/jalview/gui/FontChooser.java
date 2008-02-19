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

import jalview.bin.*;
import jalview.jbgui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class FontChooser
    extends GFontChooser
{
  AlignmentPanel ap;
  TreePanel tp;
  Font oldFont;
  boolean init = true;
  JInternalFrame frame;

  /**
   * Creates a new FontChooser object.
   *
   * @param ap DOCUMENT ME!
   */
  public FontChooser(TreePanel tp)
  {
    this.tp = tp;
    ap = tp.treeCanvas.ap;
    oldFont = tp.getTreeFont();
    defaultButton.setVisible(false);
    smoothFont.setEnabled(false);
    init();
  }

  /**
   * Creates a new FontChooser object.
   *
   * @param ap DOCUMENT ME!
   */
  public FontChooser(AlignmentPanel ap)
  {
    oldFont = ap.av.getFont();
    this.ap = ap;
    init();
  }

  void init()
  {
    frame = new JInternalFrame();
    frame.setContentPane(this);

    smoothFont.setSelected(ap.av.antiAlias);

    if (tp != null)
    {
      Desktop.addInternalFrame(frame, "Change Font (Tree Panel)", 340, 170, false);
    }
    else
    {
      Desktop.addInternalFrame(frame, "Change Font", 340, 170, false);
    }

    frame.setLayer(JLayeredPane.PALETTE_LAYER);

    String[] fonts = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getAvailableFontFamilyNames();

    for (int i = 0; i < fonts.length; i++)
    {
      fontName.addItem(fonts[i]);
    }

    for (int i = 1; i < 51; i++)
    {
      fontSize.addItem(i + "");
    }

    fontStyle.addItem("plain");
    fontStyle.addItem("bold");
    fontStyle.addItem("italic");

    fontName.setSelectedItem(oldFont.getName());
    fontSize.setSelectedItem(oldFont.getSize() + "");
    fontStyle.setSelectedIndex(oldFont.getStyle());

    FontMetrics fm = getGraphics().getFontMetrics(oldFont);
    monospaced.setSelected(fm.getStringBounds("M", getGraphics()).getWidth()
                           == fm.getStringBounds("|", getGraphics()).getWidth());

    init = false;
  }

  public void smoothFont_actionPerformed(ActionEvent e)
  {
    ap.av.antiAlias = smoothFont.isSelected();
    ap.annotationPanel.image = null;
    ap.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void ok_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {
    }

    if (ap != null)
    {
      if (ap.getOverviewPanel() != null)
      {
        ap.getOverviewPanel().updateOverviewImage();
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void cancel_actionPerformed(ActionEvent e)
  {
    if (ap != null)
    {
      ap.av.setFont(oldFont);
      ap.paintAlignment(true);
    }
    else if (tp != null)
    {
      tp.setTreeFont(oldFont);
    }
    fontName.setSelectedItem(oldFont.getName());
    fontSize.setSelectedItem(oldFont.getSize() + "");
    fontStyle.setSelectedIndex(oldFont.getStyle());

    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   */
  void changeFont()
  {
    Font newFont = new Font(fontName.getSelectedItem().toString(),
                            fontStyle.getSelectedIndex(),
                            Integer.parseInt(fontSize.getSelectedItem().
                                             toString()));
    if (tp != null)
    {
      tp.setTreeFont(newFont);
    }
    else if (ap != null)
    {
      ap.av.setFont(newFont);
      ap.fontChanged();
    }

    FontMetrics fm = getGraphics().getFontMetrics(newFont);

    monospaced.setSelected(fm.getStringBounds("M", getGraphics()).getWidth()
                           == fm.getStringBounds("|", getGraphics()).getWidth());

  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void fontName_actionPerformed(ActionEvent e)
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void fontSize_actionPerformed(ActionEvent e)
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void fontStyle_actionPerformed(ActionEvent e)
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void defaultButton_actionPerformed(ActionEvent e)
  {
    Cache.setProperty("FONT_NAME", fontName.getSelectedItem().toString());
    Cache.setProperty("FONT_STYLE", fontStyle.getSelectedIndex() + "");
    Cache.setProperty("FONT_SIZE", fontSize.getSelectedItem().toString());
    Cache.setProperty("ANTI_ALIAS", Boolean.toString(smoothFont.isSelected()));
  }
}
