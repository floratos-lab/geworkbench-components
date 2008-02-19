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
import javax.swing.event.*;

import jalview.datamodel.*;
import jalview.io.*;

public class GFinder
    extends JPanel
{
  JLabel jLabel1 = new JLabel();
  protected JButton findAll = new JButton();
  protected JButton findNext = new JButton();
  JPanel jPanel1 = new JPanel();
  GridLayout gridLayout1 = new GridLayout();
  protected JButton createNewGroup = new JButton();
  JScrollPane jScrollPane1 = new JScrollPane();
  protected JTextArea textfield = new JTextArea();
  BorderLayout borderLayout1 = new BorderLayout();
  JPanel jPanel2 = new JPanel();
  JPanel jPanel3 = new JPanel();
  JPanel jPanel4 = new JPanel();
  BorderLayout borderLayout2 = new BorderLayout();
  JPanel jPanel6 = new JPanel();
  protected JCheckBox caseSensitive = new JCheckBox();
  public GFinder()
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

  private void jbInit()
      throws Exception
  {
    jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel1.setText("Find");
    this.setLayout(borderLayout1);
    findAll.setFont(new java.awt.Font("Verdana", 0, 12));
    findAll.setText("Find all");
    findAll.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        findAll_actionPerformed(e);
      }
    });
    findNext.setFont(new java.awt.Font("Verdana", 0, 12));
    findNext.setText("Find Next");
    findNext.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        findNext_actionPerformed(e);
      }
    });
    jPanel1.setLayout(gridLayout1);
    gridLayout1.setHgap(0);
    gridLayout1.setRows(3);
    gridLayout1.setVgap(2);
    createNewGroup.setEnabled(false);
    createNewGroup.setFont(new java.awt.Font("Verdana", 0, 12));
    createNewGroup.setMargin(new Insets(0, 0, 0, 0));
    createNewGroup.setText("New Feature");
    createNewGroup.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        createNewGroup_actionPerformed(e);
      }
    });
    textfield.setFont(new java.awt.Font("Verdana", Font.PLAIN, 12));
    textfield.setText("");
    textfield.setLineWrap(true);
    textfield.addCaretListener(new CaretListener()
    {
      public void caretUpdate(CaretEvent e)
      {
        textfield_caretUpdate(e);
      }
    });
    textfield.addKeyListener(new java.awt.event.KeyAdapter()
    {
      public void keyPressed(KeyEvent e)
      {
        textfield_keyPressed(e);
      }
    });

    borderLayout1.setHgap(5);
    borderLayout1.setVgap(5);
    jPanel4.setLayout(borderLayout2);
    jPanel2.setPreferredSize(new Dimension(10, 1));
    jPanel3.setPreferredSize(new Dimension(10, 1));
    caseSensitive.setHorizontalAlignment(SwingConstants.LEFT);
    caseSensitive.setText("Match Case");
    jPanel1.add(findNext, null);
    jPanel1.add(findAll, null);
    jPanel1.add(createNewGroup, null);
    this.add(jLabel1, java.awt.BorderLayout.WEST);
    this.add(jPanel1, java.awt.BorderLayout.EAST);
    this.add(jPanel2, java.awt.BorderLayout.SOUTH);
    this.add(jPanel3, java.awt.BorderLayout.NORTH);
    this.add(jPanel4, java.awt.BorderLayout.CENTER);
    jPanel4.add(jScrollPane1, java.awt.BorderLayout.CENTER);
    jScrollPane1.getViewport().add(textfield);
    jPanel4.add(jPanel6, java.awt.BorderLayout.NORTH);
    jPanel4.add(caseSensitive, java.awt.BorderLayout.SOUTH);
  }

  protected void findNext_actionPerformed(ActionEvent e)
  {
  }

  protected void findAll_actionPerformed(ActionEvent e)
  {
  }

  protected void textfield_keyPressed(KeyEvent e)
  {
    if (e.getKeyCode() == KeyEvent.VK_ENTER)
    {
      e.consume();
      findNext_actionPerformed(null);
    }
  }

  public void createNewGroup_actionPerformed(ActionEvent e)
  {
  }

  public void textfield_caretUpdate(CaretEvent e)
  {
    if (textfield.getText().indexOf(">") > -1)
    {
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          String str = textfield.getText();
          Alignment al = null;
          try
          {
            al = new FormatAdapter().readFile(str, "Paste", "FASTA");
          }
          catch (Exception ex)
          {}
          if (al != null && al.getHeight() > 0)
          {
            str = jalview.analysis.AlignSeq.extractGaps(
                jalview.util.Comparison.GapChars,
                al.getSequenceAt(0).getSequenceAsString());

            textfield.setText(str);
          }
        }
      });
    }
  }
}
