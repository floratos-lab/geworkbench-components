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

public class GSequenceLink
    extends Panel
{
  public GSequenceLink()
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit()
      throws Exception
  {
    this.setLayout(gridBagLayout1);
    nameTB.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    nameTB.setBounds(new Rectangle(77, 10, 310, 23));
    nameTB.addKeyListener(new KeyAdapter()
    {
      public void keyTyped(KeyEvent e)
      {
        nameTB_keyTyped(e);
      }
    });
    urlTB.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    urlTB.setText("http://www.");
    urlTB.setBounds(new Rectangle(78, 40, 309, 23));
    urlTB.addKeyListener(new KeyAdapter()
    {
      public void keyTyped(KeyEvent e)
      {
        urlTB_keyTyped(e);
      }
    });
    jLabel1.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    jLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel1.setText("Link Name");
    jLabel1.setBounds(new Rectangle(4, 10, 71, 24));
    jLabel2.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    jLabel2.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel2.setText("URL");
    jLabel2.setBounds(new Rectangle(17, 37, 54, 27));
    jLabel3.setFont(new java.awt.Font("Verdana", Font.ITALIC, 11));
    jLabel3.setText("Use $SEQUENCE_ID$ to specify where sequence id is in URL");
    jLabel3.setBounds(new Rectangle(21, 72, 351, 15));
    jPanel1.setBorder(BorderFactory.createEtchedBorder());
    jPanel1.setLayout(null);
    jPanel1.add(jLabel1);
    jPanel1.add(nameTB);
    jPanel1.add(urlTB);
    jPanel1.add(jLabel2);
    jPanel1.add(jLabel3);
    this.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
                                             , GridBagConstraints.CENTER,
                                             GridBagConstraints.BOTH,
                                             new Insets(5, 4, 6, 5), 390, 100));
  }

  public void setName(String name)
  {
    nameTB.setText(name);
  }

  public void setURL(String url)
  {
    urlTB.setText(url);
  }

  public String getName()
  {
    return nameTB.getText();
  }

  public String getURL()
  {
    return urlTB.getText();
  }

  public boolean checkValid()
  {
    if (urlTB.getText().indexOf("$SEQUENCE_ID$") == -1)
    {
      JOptionPane.showInternalMessageDialog(jalview.gui.Desktop.desktop,
                                            "Sequence URL must contain $SEQUENCE_ID$",
                                            "URL not valid",
                                            JOptionPane.WARNING_MESSAGE);
      return false;
    }
    return true;
  }

  JTextField nameTB = new JTextField();
  JTextField urlTB = new JTextField();
  JLabel jLabel1 = new JLabel();
  JLabel jLabel2 = new JLabel();
  JLabel jLabel3 = new JLabel();
  JPanel jPanel1 = new JPanel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  public void nameTB_keyTyped(KeyEvent e)
  {
    if (e.getKeyChar() == '|')
    {
      e.consume();
    }
  }

  public void urlTB_keyTyped(KeyEvent e)
  {
    if (e.getKeyChar() == '|' || e.getKeyChar() == ' ')
    {
      e.consume();
    }

  }
}
