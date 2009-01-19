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

public class GTreePanel
    extends JInternalFrame
{
  BorderLayout borderLayout1 = new BorderLayout();
  public JScrollPane scrollPane = new JScrollPane();
  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu fileMenu = new JMenu();
  JMenuItem saveAsNewick = new JMenuItem();
  JMenuItem printMenu = new JMenuItem();
  protected JMenu viewMenu = new JMenu();
  public JMenuItem font = new JMenuItem();
  public JCheckBoxMenuItem bootstrapMenu = new JCheckBoxMenuItem();
  public JCheckBoxMenuItem distanceMenu = new JCheckBoxMenuItem();
  public JCheckBoxMenuItem fitToWindow = new JCheckBoxMenuItem();
  public JCheckBoxMenuItem placeholdersMenu = new JCheckBoxMenuItem();
  JMenuItem pngTree = new JMenuItem();
  JMenuItem epsTree = new JMenuItem();
  JMenu saveAsMenu = new JMenu();
  JMenuItem textbox = new JMenuItem();
  public JMenuItem originalSeqData = new JMenuItem();
  protected JMenu associateLeavesMenu = new JMenu();
  public GTreePanel()
  {
    try
    {
      jbInit();
      this.setJMenuBar(jMenuBar1);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void jbInit()
      throws Exception
  {
    this.getContentPane().setLayout(borderLayout1);
    this.setBackground(Color.white);
    this.setFont(new java.awt.Font("Verdana", 0, 12));
    scrollPane.setOpaque(false);
    fileMenu.setText("File");
    saveAsNewick.setText("Newick Format");
    saveAsNewick.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveAsNewick_actionPerformed(e);
      }
    });
    printMenu.setText("Print");
    printMenu.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        printMenu_actionPerformed(e);
      }
    });
    viewMenu.setText("View");
    viewMenu.addMenuListener(new MenuListener()
    {
      public void menuSelected(MenuEvent e)
      {
        viewMenu_menuSelected();
      }

      public void menuDeselected(MenuEvent e)
      {
      }

      public void menuCanceled(MenuEvent e)
      {
      }
    });
    font.setText("Font...");
    font.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        font_actionPerformed(e);
      }
    });
    bootstrapMenu.setText("Show Bootstrap Values");
    bootstrapMenu.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        bootstrapMenu_actionPerformed(e);
      }
    });
    distanceMenu.setText("Show Distances");
    distanceMenu.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        distanceMenu_actionPerformed(e);
      }
    });
    fitToWindow.setSelected(true);
    fitToWindow.setText("Fit To Window");
    fitToWindow.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fitToWindow_actionPerformed(e);
      }
    });
    epsTree.setText("EPS");
    epsTree.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        epsTree_actionPerformed(e);
      }
    });
    pngTree.setText("PNG");
    pngTree.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        pngTree_actionPerformed(e);
      }
    });
    saveAsMenu.setText("Save as");
    placeholdersMenu.setToolTipText(
        "Marks leaves of tree not associated with a sequence");
    placeholdersMenu.setText("Mark Unlinked Leaves");
    placeholdersMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        placeholdersMenu_actionPerformed(e);
      }
    });
    textbox.setText("Output to Textbox...");
    textbox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        textbox_actionPerformed(e);
      }
    });
    originalSeqData.setText("Input Data...");
    originalSeqData.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        originalSeqData_actionPerformed(e);
      }
    });
    associateLeavesMenu.setText("Associate Leaves With");
    this.getContentPane().add(scrollPane, BorderLayout.CENTER);
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(viewMenu);
    fileMenu.add(saveAsMenu);
    fileMenu.add(textbox);
    fileMenu.add(printMenu);
    fileMenu.add(originalSeqData);
    viewMenu.add(fitToWindow);
    viewMenu.add(font);
    viewMenu.add(distanceMenu);
    viewMenu.add(bootstrapMenu);
    viewMenu.add(placeholdersMenu);
    viewMenu.add(associateLeavesMenu);
    saveAsMenu.add(saveAsNewick);
    saveAsMenu.add(epsTree);
    saveAsMenu.add(pngTree);
  }

  public void printMenu_actionPerformed(ActionEvent e)
  {
  }

  public void font_actionPerformed(ActionEvent e)
  {
  }

  public void distanceMenu_actionPerformed(ActionEvent e)
  {
  }

  public void bootstrapMenu_actionPerformed(ActionEvent e)
  {
  }

  public void fitToWindow_actionPerformed(ActionEvent e)
  {
  }

  public void pngTree_actionPerformed(ActionEvent e)
  {
  }

  public void epsTree_actionPerformed(ActionEvent e)
  {
  }

  public void saveAsNewick_actionPerformed(ActionEvent e)
  {
  }

  public void placeholdersMenu_actionPerformed(ActionEvent e)
  {
  }

  public void textbox_actionPerformed(ActionEvent e)
  {
  }

  public void fullid_actionPerformed(ActionEvent e)
  {

  }

  public void originalSeqData_actionPerformed(ActionEvent e)
  {

  }

  public void viewMenu_menuSelected()
  {
  }
}
