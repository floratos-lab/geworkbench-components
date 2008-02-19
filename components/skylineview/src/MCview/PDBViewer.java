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

import java.io.*;

import java.awt.event.*;
import javax.swing.*;

import jalview.datamodel.*;
import jalview.gui.*;
import jalview.io.*;
import jalview.schemes.*;
import java.awt.BorderLayout;

public class PDBViewer
    extends JInternalFrame implements Runnable
{

  /**
   * The associated sequence in an alignment
   */
  PDBCanvas pdbcanvas;

  PDBEntry pdbentry;
  SequenceI[]seq;
  String[]chains;
  AlignmentPanel ap;
  String protocol;
  String tmpPDBFile;

  public PDBViewer(PDBEntry pdbentry,
                   SequenceI[] seq,
                   String[] chains,
                   AlignmentPanel ap,
                   String protocol)

  {
    this.pdbentry = pdbentry;
    this.seq = seq;
    this.chains = chains;
    this.ap = ap;
    this.protocol = protocol;

    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }


    StringBuffer title = new StringBuffer(seq[0].getName()
                                          + ":"
                                          + pdbentry.getFile());

    pdbcanvas = new PDBCanvas();

    setContentPane(pdbcanvas);

    if (pdbentry.getFile() != null)
    {
      try
      {
        tmpPDBFile = pdbentry.getFile();
        PDBfile pdbfile = new PDBfile(tmpPDBFile,
                                      jalview.io.AppletFormatAdapter.FILE);

        pdbcanvas.init(pdbentry, seq, chains, ap, protocol);

      }
      catch (java.io.IOException ex)
      {
        ex.printStackTrace();
      }
    }
    else
    {
      Thread worker = new Thread(this);
      worker.start();
    }



    if (pdbentry.getProperty() != null)
    {
      if (pdbentry.getProperty().get("method") != null)
      {
        title.append(" Method: ");
        title.append(pdbentry.getProperty().get("method"));
      }
      if (pdbentry.getProperty().get("chains") != null)
      {
        title.append(" Chain:");
        title.append(pdbentry.getProperty().get("chains"));
      }
    }
    Desktop.addInternalFrame(this, title.toString(), 400, 400);
  }

  public void run()
  {
    try
    {
      EBIFetchClient ebi = new EBIFetchClient();
      String query = "pdb:" + pdbentry.getId();
      pdbentry.setFile(ebi.fetchDataAsFile(query, "default", "raw")
                       .getAbsolutePath());

      if(pdbentry.getFile()!=null)
        pdbcanvas.init(pdbentry, seq, chains, ap, protocol);
    }
    catch (Exception ex)
    {
      pdbcanvas.errorMessage = "Error retrieving file: "+pdbentry.getId();
      ex.printStackTrace();
    }
  }

  private void jbInit()
      throws Exception
  {
    this.addKeyListener(new KeyAdapter()
    {
      public void keyPressed(KeyEvent evt)
      {
        pdbcanvas.keyPressed(evt);
      }
    });

    this.setJMenuBar(jMenuBar1);
    fileMenu.setText("File");
    coloursMenu.setText("Colours");
    saveMenu.setActionCommand("Save Image");
    saveMenu.setText("Save As");
    png.setText("PNG");
    png.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        png_actionPerformed(e);
      }
    });
    eps.setText("EPS");
    eps.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        eps_actionPerformed(e);
      }
    });
    mapping.setText("View Mapping");
    mapping.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        mapping_actionPerformed(e);
      }
    });
    wire.setText("Wireframe");
    wire.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        wire_actionPerformed(e);
      }
    });
    depth.setSelected(true);
    depth.setText("Depthcue");
    depth.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        depth_actionPerformed(e);
      }
    });
    zbuffer.setSelected(true);
    zbuffer.setText("Z Buffering");
    zbuffer.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        zbuffer_actionPerformed(e);
      }
    });
    charge.setText("Charge & Cysteine");
    charge.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        charge_actionPerformed(e);
      }
    });
    chain.setText("By Chain");
    chain.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        chain_actionPerformed(e);
      }
    });
    seqButton.setSelected(true);
    seqButton.setText("By Sequence");
    seqButton.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        seqButton_actionPerformed(e);
      }
    });
    allchains.setSelected(true);
    allchains.setText("Show All Chains");
    allchains.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent e)
      {
        allchains_itemStateChanged(e);
      }
    });
    zappo.setText("Zappo");
    zappo.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        zappo_actionPerformed(e);
      }
    });
    taylor.setText("Taylor");
    taylor.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        taylor_actionPerformed(e);
      }
    });
    hydro.setText("Hydro");
    hydro.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        hydro_actionPerformed(e);
      }
    });
    helix.setText("Helix");
    helix.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        helix_actionPerformed(e);
      }
    });
    strand.setText("Strand");
    strand.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        strand_actionPerformed(e);
      }
    });
    turn.setText("Turn");
    turn.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        turn_actionPerformed(e);
      }
    });
    buried.setText("Buried");
    buried.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        buried_actionPerformed(e);
      }
    });
    user.setText("User Defined...");
    user.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        user_actionPerformed(e);
      }
    });
    viewMenu.setText("View");
    background.setText("Background Colour...");
    background.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        background_actionPerformed(e);
      }
    });
    savePDB.setText("PDB File");
    savePDB.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        savePDB_actionPerformed(e);
      }
    });
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(coloursMenu);
    jMenuBar1.add(viewMenu);
    fileMenu.add(saveMenu);
    fileMenu.add(mapping);
    saveMenu.add(savePDB);
    saveMenu.add(png);
    saveMenu.add(eps);
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
    coloursMenu.add(background);
    ButtonGroup bg = new ButtonGroup();
    bg.add(seqButton);
    bg.add(chain);
    bg.add(charge);
    bg.add(zappo);
    bg.add(taylor);
    bg.add(hydro);
    bg.add(helix);
    bg.add(strand);
    bg.add(turn);
    bg.add(buried);
    bg.add(user);

    if (jalview.gui.UserDefinedColours.getUserColourSchemes() != null)
    {
      java.util.Enumeration userColours = jalview.gui.UserDefinedColours.
          getUserColourSchemes().keys();

      while (userColours.hasMoreElements())
      {
        final JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem(
            userColours.
            nextElement().toString());
        radioItem.setName("USER_DEFINED");
        radioItem.addMouseListener(new MouseAdapter()
        {
          public void mousePressed(MouseEvent evt)
          {
            if (evt.isControlDown() || SwingUtilities.isRightMouseButton(evt))
            {
              radioItem.removeActionListener(radioItem.getActionListeners()[0]);

              int option = JOptionPane.showInternalConfirmDialog(jalview.gui.
                  Desktop.desktop,
                  "Remove from default list?",
                  "Remove user defined colour",
                  JOptionPane.YES_NO_OPTION);
              if (option == JOptionPane.YES_OPTION)
              {
                jalview.gui.UserDefinedColours.removeColourFromDefaults(
                    radioItem.getText());
                coloursMenu.remove(radioItem);
              }
              else
              {
                radioItem.addActionListener(new ActionListener()
                {
                  public void actionPerformed(ActionEvent evt)
                  {
                    user_actionPerformed(evt);
                  }
                });
              }
            }
          }
        });
        radioItem.addActionListener(new ActionListener()
        {
          public void actionPerformed(ActionEvent evt)
          {
            user_actionPerformed(evt);
          }
        });
        coloursMenu.add(radioItem);
        bg.add(radioItem);
      }
    }

    viewMenu.add(wire);
    viewMenu.add(depth);
    viewMenu.add(zbuffer);
    viewMenu.add(allchains);
  }

  JMenuBar jMenuBar1 = new JMenuBar();
  JMenu fileMenu = new JMenu();
  JMenu coloursMenu = new JMenu();
  JMenu saveMenu = new JMenu();
  JMenuItem png = new JMenuItem();
  JMenuItem eps = new JMenuItem();
  JMenuItem mapping = new JMenuItem();
  JCheckBoxMenuItem wire = new JCheckBoxMenuItem();
  JCheckBoxMenuItem depth = new JCheckBoxMenuItem();
  JCheckBoxMenuItem zbuffer = new JCheckBoxMenuItem();
  JCheckBoxMenuItem allchains = new JCheckBoxMenuItem();

  JRadioButtonMenuItem charge = new JRadioButtonMenuItem();
  JRadioButtonMenuItem chain = new JRadioButtonMenuItem();
  JRadioButtonMenuItem seqButton = new JRadioButtonMenuItem();
  JRadioButtonMenuItem hydro = new JRadioButtonMenuItem();
  JRadioButtonMenuItem taylor = new JRadioButtonMenuItem();
  JRadioButtonMenuItem zappo = new JRadioButtonMenuItem();
  JRadioButtonMenuItem user = new JRadioButtonMenuItem();
  JRadioButtonMenuItem buried = new JRadioButtonMenuItem();
  JRadioButtonMenuItem turn = new JRadioButtonMenuItem();
  JRadioButtonMenuItem strand = new JRadioButtonMenuItem();
  JRadioButtonMenuItem helix = new JRadioButtonMenuItem();
  JMenu viewMenu = new JMenu();
  JMenuItem background = new JMenuItem();
  JMenuItem savePDB = new JMenuItem();

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void eps_actionPerformed(ActionEvent e)
  {
    makePDBImage(jalview.util.ImageMaker.EPS);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void png_actionPerformed(ActionEvent e)
  {
    makePDBImage(jalview.util.ImageMaker.PNG);
  }

  void makePDBImage(int type)
  {
    int width = pdbcanvas.getWidth();
    int height = pdbcanvas.getHeight();

    jalview.util.ImageMaker im;

    if (type == jalview.util.ImageMaker.PNG)
    {
      im = new jalview.util.ImageMaker(this,
                                       jalview.util.ImageMaker.PNG,
                                       "Make PNG image from view",
                                       width, height,
                                       null, null);
    }
    else
    {
      im = new jalview.util.ImageMaker(this,
                                       jalview.util.ImageMaker.EPS,
                                       "Make EPS file from view",
                                       width, height,
                                       null, this.getTitle());
    }

    if (im.getGraphics() != null)
    {
      pdbcanvas.drawAll(im.getGraphics(), width, height);
      im.writeImage();
    }
  }

  public void charge_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setChargeColours();
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void hydro_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new HydrophobicColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void chain_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setChainColours();
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void zbuffer_actionPerformed(ActionEvent e)
  {
    pdbcanvas.zbuffer = !pdbcanvas.zbuffer;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void molecule_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bymolecule = !pdbcanvas.bymolecule;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void depth_actionPerformed(ActionEvent e)
  {
    pdbcanvas.depthcue = !pdbcanvas.depthcue;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void wire_actionPerformed(ActionEvent e)
  {
    pdbcanvas.wire = !pdbcanvas.wire;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void seqButton_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = true;
    pdbcanvas.updateSeqColours();
  }

  public void mapping_actionPerformed(ActionEvent e)
  {
    jalview.gui.CutAndPasteTransfer cap = new jalview.gui.CutAndPasteTransfer();
    Desktop.addInternalFrame(cap, "PDB - Sequence Mapping", 550, 600);
    cap.setText(pdbcanvas.mappingDetails.toString());
  }

  public void allchains_itemStateChanged(ItemEvent e)
  {
    pdbcanvas.setAllchainsVisible(allchains.getState());
  }


  public void zappo_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new ZappoColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void taylor_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new TaylorColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void helix_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new HelixColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void strand_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new StrandColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void turn_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new TurnColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void buried_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new BuriedColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void user_actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals("User Defined..."))
    {
     // new UserDefinedColours(pdbcanvas, null);
    }
    else
    {
      UserColourScheme udc = (UserColourScheme) UserDefinedColours.
          getUserColourSchemes().get(e.getActionCommand());

      pdbcanvas.pdb.setColours(udc);
      pdbcanvas.redrawneeded = true;
      pdbcanvas.repaint();
    }
  }

  public void background_actionPerformed(ActionEvent e)
  {
    java.awt.Color col = JColorChooser.showDialog(this,
                                                  "Select Background Colour",
                                                  pdbcanvas.backgroundColour);

    if (col != null)
    {
      pdbcanvas.backgroundColour = col;
      pdbcanvas.redrawneeded = true;
      pdbcanvas.repaint();
    }
  }

  public void savePDB_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
        jalview.bin.Cache.getProperty(
            "LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Save PDB File");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      try
      {
        BufferedReader in = new BufferedReader(new FileReader(tmpPDBFile));
        File outFile = chooser.getSelectedFile();

        PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
        String data;
        while ( (data = in.readLine()) != null)
        {
          if (
              ! (data.indexOf("<PRE>") > -1 || data.indexOf("</PRE>") > -1)
              )
          {
            out.println(data);
          }
        }
        out.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
}
