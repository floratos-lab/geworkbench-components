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

public class FontChooser
    extends Panel implements ActionListener, ItemListener
{
  AlignmentPanel ap;
  TreePanel tp;
  Font oldFont;
  boolean init = true;
  Frame frame;

  public FontChooser(TreePanel tp)
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    this.tp = tp;
    oldFont = tp.getTreeFont();
    init();
  }

  public FontChooser(AlignmentPanel ap)
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    this.ap = ap;
    oldFont = ap.av.getFont();
    init();
  }

  void init()
  {
    String fonts[] = Toolkit.getDefaultToolkit().getFontList();
    for (int i = 0; i < fonts.length; i++)
    {
      fontName.addItem(fonts[i]);
    }

    for (int i = 1; i < 31; i++)
    {
      fontSize.addItem(i + "");
    }

    fontStyle.addItem("plain");
    fontStyle.addItem("bold");
    fontStyle.addItem("italic");

    fontName.select(oldFont.getName());
    fontSize.select(oldFont.getSize() + "");
    fontStyle.select(oldFont.getStyle());

    Frame frame = new Frame();
    this.frame = frame;
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame, "Change Font", 440, 115);

    init = false;
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == ok)
    {
      ok_actionPerformed();
    }
    else if (evt.getSource() == cancel)
    {
      cancel_actionPerformed();
    }
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == fontName)
    {
      fontName_actionPerformed();
    }
    else if (evt.getSource() == fontSize)
    {
      fontSize_actionPerformed();
    }
    else if (evt.getSource() == fontStyle)
    {
      fontStyle_actionPerformed();
    }
  }

  protected void ok_actionPerformed()
  {
    frame.setVisible(false);
    if (ap != null)
    {
      if (ap.getOverviewPanel() != null)
      {
        ap.getOverviewPanel().updateOverviewImage();
      }
    }

  }

  protected void cancel_actionPerformed()
  {
    if (ap != null)
    {
      ap.av.setFont(oldFont);
      ap.paintAlignment(true);
    }
    else if (tp != null)
    {
      tp.setTreeFont(oldFont);
      tp.treeCanvas.repaint();
    }

    fontName.select(oldFont.getName());
    fontSize.select(oldFont.getSize() + "");
    fontStyle.select(oldFont.getStyle());

    frame.setVisible(false);
  }

  void changeFont()
  {
    Font newFont = new Font(fontName.getSelectedItem().toString(),
                            fontStyle.getSelectedIndex(),
                            Integer.parseInt(fontSize.getSelectedItem().
                                             toString())
        );
    if (ap != null)
    {
      ap.av.setFont(newFont);
      ap.fontChanged();
    }
    else if (tp != null)
    {
      tp.setTreeFont(newFont);
    }
  }

  protected void fontName_actionPerformed()
  {
    if (init)
    {
      return;
    }
    changeFont();
  }

  protected void fontSize_actionPerformed()
  {
    if (init)
    {
      return;
    }
    changeFont();
  }

  protected void fontStyle_actionPerformed()
  {
    if (init)
    {
      return;
    }
    changeFont();
  }

  Label label1 = new Label();
  protected Choice fontSize = new Choice();
  protected Choice fontStyle = new Choice();
  Label label2 = new Label();
  Label label3 = new Label();
  protected Choice fontName = new Choice();
  Button ok = new Button();
  Button cancel = new Button();
  Panel panel1 = new Panel();
  Panel panel2 = new Panel();
  Panel panel3 = new Panel();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  Panel panel4 = new Panel();
  Panel panel5 = new Panel();
  BorderLayout borderLayout4 = new BorderLayout();

  private void jbInit()
      throws Exception
  {
    label1.setFont(new java.awt.Font("Verdana", 0, 11));
    label1.setAlignment(Label.RIGHT);
    label1.setText("Font: ");
    this.setLayout(borderLayout4);
    fontSize.setFont(new java.awt.Font("Verdana", 0, 11));
    fontSize.addItemListener(this);
    fontStyle.setFont(new java.awt.Font("Verdana", 0, 11));
    fontStyle.addItemListener(this);
    label2.setAlignment(Label.RIGHT);
    label2.setFont(new java.awt.Font("Verdana", 0, 11));
    label2.setText("Size: ");
    label3.setAlignment(Label.RIGHT);
    label3.setFont(new java.awt.Font("Verdana", 0, 11));
    label3.setText("Style: ");
    fontName.setFont(new java.awt.Font("Verdana", 0, 11));
    fontName.addItemListener(this);
    ok.setFont(new java.awt.Font("Verdana", 0, 11));
    ok.setLabel("OK");
    ok.addActionListener(this);
    cancel.setFont(new java.awt.Font("Verdana", 0, 11));
    cancel.setLabel("Cancel");
    cancel.addActionListener(this);
    this.setBackground(Color.white);
    panel1.setLayout(borderLayout1);
    panel2.setLayout(borderLayout3);
    panel3.setLayout(borderLayout2);
    panel5.setBackground(Color.white);
    panel4.setBackground(Color.white);
    panel1.setBackground(Color.white);
    panel2.setBackground(Color.white);
    panel3.setBackground(Color.white);
    panel1.add(label1, BorderLayout.WEST);
    panel1.add(fontName, BorderLayout.CENTER);
    panel5.add(panel1, null);
    panel5.add(panel3, null);
    panel5.add(panel2, null);
    panel2.add(label3, BorderLayout.WEST);
    panel2.add(fontStyle, BorderLayout.CENTER);
    panel3.add(label2, BorderLayout.WEST);
    panel3.add(fontSize, BorderLayout.CENTER);
    this.add(panel4, BorderLayout.SOUTH);
    panel4.add(ok, null);
    panel4.add(cancel, null);
    this.add(panel5, BorderLayout.CENTER);
  }

}
