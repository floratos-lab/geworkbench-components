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

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GStructureViewer  extends JInternalFrame
{
  public GStructureViewer()
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
    this.setJMenuBar(menuBar);
    fileMenu.setText("File");
    savemenu.setActionCommand("Save Image");
    savemenu.setText("Save As");
    pdbFile.setText("PDB File");
    pdbFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        pdbFile_actionPerformed(actionEvent);
      }
    });
    png.setText("PNG");
    png.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        png_actionPerformed(actionEvent);
      }
    });
    eps.setText("EPS");
    eps.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        eps_actionPerformed(actionEvent);
      }
    });
    viewMapping.setText("View Mapping");
    viewMapping.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        viewMapping_actionPerformed(actionEvent);
      }
    });
    viewMenu.setText("View");
    chainMenu.setText("Show Chain");
    colourMenu.setText("Colours");
    backGround.setText("Background Colour...");
    backGround.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        backGround_actionPerformed(actionEvent);
      }
    });
    seqColour.setSelected(true);
    seqColour.setText("By Sequence");
    seqColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        seqColour_actionPerformed(actionEvent);
      }
    });
    chainColour.setText("By Chain");
    chainColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        chainColour_actionPerformed(actionEvent);
      }
    });
    chargeColour.setText("Charge & Cysteine");
    chargeColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        chargeColour_actionPerformed(actionEvent);
      }
    });
    zappoColour.setText("Zappo");
    zappoColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        zappoColour_actionPerformed(actionEvent);
      }
    });
    taylorColour.setText("Taylor");
    taylorColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        taylorColour_actionPerformed(actionEvent);
      }
    });
    hydroColour.setText("Hydro");
    hydroColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        hydroColour_actionPerformed(actionEvent);
      }
    });
    strandColour.setText("Strand");
    strandColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        strandColour_actionPerformed(actionEvent);
      }
    });
    helixColour.setText("Helix Propensity");
    helixColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        helixColour_actionPerformed(actionEvent);
      }
    });
    turnColour.setText("Turn Propensity");
    turnColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        turnColour_actionPerformed(actionEvent);
      }
    });
    buriedColour.setText("Buried Index");
    buriedColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        buriedColour_actionPerformed(actionEvent);
      }
    });
    userColour.setText("User Defined ...");
    userColour.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        userColour_actionPerformed(actionEvent);
      }
    });
    helpMenu.setText("Help");
    jmolHelp.setText("Jmol Help");
    jmolHelp.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        jmolHelp_actionPerformed(actionEvent);
      }
    });
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(colourMenu);
    menuBar.add(helpMenu);
    fileMenu.add(savemenu);
    fileMenu.add(viewMapping);
    savemenu.add(pdbFile);
    savemenu.add(png);
    savemenu.add(eps);
    viewMenu.add(chainMenu);
    colourMenu.add(seqColour);
    colourMenu.add(chainColour);
    colourMenu.add(chargeColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydroColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(userColour);
    colourMenu.add(backGround);
    helpMenu.add(jmolHelp);
  }

  JMenuBar menuBar = new JMenuBar();
  JMenu fileMenu = new JMenu();
  JMenu savemenu = new JMenu();
  JMenuItem pdbFile = new JMenuItem();
  JMenuItem png = new JMenuItem();
  JMenuItem eps = new JMenuItem();
  JMenuItem viewMapping = new JMenuItem();
  JMenu viewMenu = new JMenu();
  protected JMenu chainMenu = new JMenu();
  JMenu jMenu1 = new JMenu();
  JMenu colourMenu = new JMenu();
  JMenuItem backGround = new JMenuItem();
  protected JCheckBoxMenuItem seqColour = new JCheckBoxMenuItem();
  JMenuItem chainColour = new JMenuItem();
  JMenuItem chargeColour = new JMenuItem();
  JMenuItem zappoColour = new JMenuItem();
  JMenuItem taylorColour = new JMenuItem();
  JMenuItem hydroColour = new JMenuItem();
  JMenuItem strandColour = new JMenuItem();
  JMenuItem helixColour = new JMenuItem();
  JMenuItem turnColour = new JMenuItem();
  JMenuItem buriedColour = new JMenuItem();
  JMenuItem userColour = new JMenuItem();
  JMenu helpMenu = new JMenu();
  JMenuItem jmolHelp = new JMenuItem();
  public void pdbFile_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void png_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void eps_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void viewMapping_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void seqColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void chainColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void chargeColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void zappoColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void taylorColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void hydroColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void helixColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void strandColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void turnColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void buriedColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void userColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void backGround_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void jmolHelp_actionPerformed(ActionEvent actionEvent)
  {

  }
}
