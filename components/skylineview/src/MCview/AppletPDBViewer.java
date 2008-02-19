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
package MCview;

import java.awt.*;
import java.awt.event.*;

import jalview.appletgui.*;
import jalview.datamodel.*;
import jalview.schemes.*;


public class AppletPDBViewer
    extends Frame implements ActionListener, ItemListener
{
  AppletPDBCanvas pdbcanvas;

  public AppletPDBViewer(PDBEntry pdbentry,
                         SequenceI[] seq,
                         String [] chains,
                         AlignmentPanel ap,
                         String protocol)
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    pdbcanvas = new AppletPDBCanvas(pdbentry, seq, chains, ap, protocol);


    add(pdbcanvas, BorderLayout.CENTER);

    StringBuffer title = new StringBuffer(seq[0].getName()
                                          + ":"
                                          + pdbcanvas.pdbentry.getFile());

    jalview.bin.JalviewLite.addFrame(this, title.toString(), 400, 400);

  }



  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == mapping)
    {
      jalview.appletgui.CutAndPasteTransfer cap
          = new jalview.appletgui.CutAndPasteTransfer(false, null);
      Frame frame = new Frame();
      frame.add(cap);
      jalview.bin.JalviewLite.addFrame(frame, "PDB - Sequence Mapping", 500,
                                       600);
      cap.setText(pdbcanvas.mappingDetails.toString());

    }
    else if (evt.getSource() == charge)
    {
      pdbcanvas.bysequence = false;
      pdbcanvas.pdb.setChargeColours();
    }

    else if (evt.getSource() == chain)
    {
      pdbcanvas.bysequence = false;
      pdbcanvas.pdb.setChainColours();
    }
    else if (evt.getSource() == seqButton)
    {
      pdbcanvas.bysequence = true;
      pdbcanvas.colourBySequence();

    }
    else if (evt.getSource() == zappo)
    {
      pdbcanvas.setColours(new ZappoColourScheme());
    }
    else if (evt.getSource() == taylor)
    {
      pdbcanvas.setColours(new TaylorColourScheme());
    }
    else if (evt.getSource() == hydro)
    {
      pdbcanvas.setColours(new HydrophobicColourScheme());
    }
    else if (evt.getSource() == helix)
    {
      pdbcanvas.setColours(new HelixColourScheme());
    }
    else if (evt.getSource() == strand)
    {
      pdbcanvas.setColours(new StrandColourScheme());
    }
    else if (evt.getSource() == turn)
    {
      pdbcanvas.setColours(new TurnColourScheme());
    }
    else if (evt.getSource() == buried)
    {
      pdbcanvas.setColours(new BuriedColourScheme());
    }
    else if (evt.getSource() == user)
    {
      pdbcanvas.bysequence = false;
      new jalview.appletgui.UserDefinedColours(pdbcanvas);
    }

    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();

  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == allchains)
    {
      pdbcanvas.setAllchainsVisible(allchains.getState());
    }
    else if (evt.getSource() == wire)
    {
          pdbcanvas.wire = !pdbcanvas.wire;
    }
    else if (evt.getSource() == depth)
    {
      pdbcanvas.depthcue = !pdbcanvas.depthcue;
    }
    else if (evt.getSource() == zbuffer)
    {
      pdbcanvas.zbuffer = !pdbcanvas.zbuffer;
    }
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  private void jbInit()
      throws Exception
  {
    setMenuBar(jMenuBar1);
    fileMenu.setLabel("File");
    coloursMenu.setLabel("Colours");
    mapping.setLabel("View Mapping");
    mapping.addActionListener(this);
    wire.setLabel("Wireframe");
    wire.addItemListener(this);
    depth.setState(true);
    depth.setLabel("Depthcue");
    depth.addItemListener(this);
    zbuffer.setState(true);
    zbuffer.setLabel("Z Buffering");
    zbuffer.addItemListener(this);
    charge.setLabel("Charge & Cysteine");
    charge.addActionListener(this);
    hydro.setLabel("Hydrophobicity");
    hydro.addActionListener(this);
    chain.setLabel("By Chain");
    chain.addActionListener(this);
    seqButton.setLabel("By Sequence");
    seqButton.addActionListener(this);
    allchains.setLabel("All Chains Visible");
    allchains.addItemListener(this);
    viewMenu.setLabel("View");
    zappo.setLabel("Zappo");
    zappo.addActionListener(this);
    taylor.setLabel("Taylor");
    taylor.addActionListener(this);
    helix.setLabel("Helix Propensity");
    helix.addActionListener(this);
    strand.setLabel("Strand Propensity");
    strand.addActionListener(this);
    turn.setLabel("Turn Propensity");
    turn.addActionListener(this);
    buried.setLabel("Buried Index");
    buried.addActionListener(this);
    user.setLabel("User Defined...");
    user.addActionListener(this);
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(coloursMenu);
    jMenuBar1.add(viewMenu);
    fileMenu.add(mapping); ;

    coloursMenu.add(seqButton);
    coloursMenu.add(chain);
    coloursMenu.add(charge);
    coloursMenu.add(zappo);
    coloursMenu.add(taylor);
    coloursMenu.add(hydro);
    coloursMenu.add(helix);
    coloursMenu.add(strand);
    coloursMenu.add(turn);
    coloursMenu.add(buried);
    coloursMenu.add(user);
    viewMenu.add(wire);
    viewMenu.add(depth);
    viewMenu.add(zbuffer);
    viewMenu.add(allchains);
    allchains.setState(true);
  }

  MenuBar jMenuBar1 = new MenuBar();
  Menu fileMenu = new Menu();
  Menu coloursMenu = new Menu();
  MenuItem mapping = new MenuItem();
  CheckboxGroup bg = new CheckboxGroup();
  CheckboxMenuItem wire = new CheckboxMenuItem();
  CheckboxMenuItem depth = new CheckboxMenuItem();
  CheckboxMenuItem zbuffer = new CheckboxMenuItem();

  MenuItem charge = new MenuItem();
  MenuItem hydro = new MenuItem();
  MenuItem chain = new MenuItem();
  MenuItem seqButton = new MenuItem();

  CheckboxMenuItem allchains = new CheckboxMenuItem();
  Menu viewMenu = new Menu();
  MenuItem turn = new MenuItem();
  MenuItem strand = new MenuItem();
  MenuItem helix = new MenuItem();
  MenuItem taylor = new MenuItem();
  MenuItem zappo = new MenuItem();
  MenuItem buried = new MenuItem();
  MenuItem user = new MenuItem();


//End StructureListener
////////////////////////////


}
