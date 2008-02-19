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
package jalview.jbgui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class GFontChooser
    extends JPanel
{
  JLabel jLabel1 = new JLabel();
  protected JComboBox fontSize = new JComboBox();
  protected JComboBox fontStyle = new JComboBox();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  protected JComboBox fontName = new JComboBox();
  JButton ok = new JButton();
  JButton cancel = new JButton();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  JPanel jPanel3 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  BorderLayout borderLayout3 = new BorderLayout();
  protected JButton defaultButton = new JButton();
  protected JCheckBox smoothFont = new JCheckBox();
  BorderLayout borderLayout4 = new BorderLayout();
  protected JCheckBox monospaced = new JCheckBox();
  JPanel jPanel4 = new JPanel();

  /**
   * Creates a new GFontChooser object.
   */
  public GFontChooser()
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @throws Exception DOCUMENT ME!
   */
  private void jbInit()
      throws Exception
  {
    jLabel1.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel1.setText("Font: ");
    jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
    this.setLayout(null);
    fontSize.setFont(new java.awt.Font("Verdana", 0, 11));
    fontSize.setOpaque(false);
    fontSize.setPreferredSize(new Dimension(50, 21));
    fontSize.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fontSize_actionPerformed(e);
      }
    });
    fontStyle.setFont(new java.awt.Font("Verdana", 0, 11));
    fontStyle.setOpaque(false);
    fontStyle.setPreferredSize(new Dimension(90, 21));
    fontStyle.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fontStyle_actionPerformed(e);
      }
    });
    jLabel2.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel2.setText("Size: ");
    jLabel2.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
    jLabel3.setFont(new java.awt.Font("Verdana", 0, 11));
    jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
    jLabel3.setText("Style: ");
    jLabel3.setVerticalTextPosition(javax.swing.SwingConstants.CENTER);
    fontName.setFont(new java.awt.Font("Verdana", 0, 11));
    fontName.setMaximumSize(new Dimension(32767, 32767));
    fontName.setMinimumSize(new Dimension(300, 21));
    fontName.setOpaque(false);
    fontName.setPreferredSize(new Dimension(180, 21));
    fontName.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fontName_actionPerformed(e);
      }
    });
    ok.setFont(new java.awt.Font("Verdana", 0, 11));
    ok.setText("OK");
    ok.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    cancel.setFont(new java.awt.Font("Verdana", 0, 11));
    cancel.setText("Cancel");
    cancel.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    this.setBackground(Color.white);
    jPanel1.setOpaque(false);
    jPanel1.setBounds(new Rectangle(5, 6, 308, 23));
    jPanel1.setLayout(borderLayout1);
    jPanel2.setOpaque(false);
    jPanel2.setBounds(new Rectangle(5, 37, 128, 21));
    jPanel2.setLayout(borderLayout3);
    jPanel3.setOpaque(false);
    jPanel3.setBounds(new Rectangle(174, 38, 134, 21));
    jPanel3.setLayout(borderLayout2);
    defaultButton.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    defaultButton.setText("Set as Default");
    defaultButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        defaultButton_actionPerformed(e);
      }
    });
    smoothFont.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    smoothFont.setOpaque(false);
    smoothFont.setText("Anti-alias Fonts (Slower to render)");
    smoothFont.setBounds(new Rectangle(41, 65, 223, 23));
    smoothFont.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        smoothFont_actionPerformed(e);
      }
    });
    monospaced.setEnabled(false);
    monospaced.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    monospaced.setOpaque(false);
    monospaced.setToolTipText("Monospaced fonts are faster to render");
    monospaced.setText("Monospaced");
    jPanel4.setOpaque(false);
    jPanel4.setBounds(new Rectangle(24, 92, 259, 35));
    jPanel1.add(jLabel1, BorderLayout.WEST);
    jPanel1.add(fontName, BorderLayout.CENTER);
    jPanel1.add(monospaced, java.awt.BorderLayout.EAST);
    this.add(jPanel3, null);
    this.add(jPanel2, null);
    jPanel2.add(fontSize, java.awt.BorderLayout.CENTER);
    jPanel2.add(jLabel2, java.awt.BorderLayout.WEST);
    jPanel4.add(defaultButton);
    jPanel4.add(ok);
    jPanel4.add(cancel);
    this.add(smoothFont);
    this.add(jPanel4);
    jPanel3.add(jLabel3, java.awt.BorderLayout.WEST);
    jPanel3.add(fontStyle, java.awt.BorderLayout.CENTER);
    this.add(jPanel1, null);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void ok_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void cancel_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void fontName_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void fontSize_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void fontStyle_actionPerformed(ActionEvent e)
  {
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void defaultButton_actionPerformed(ActionEvent e)
  {
  }

  public void smoothFont_actionPerformed(ActionEvent e)
  {

  }
}
