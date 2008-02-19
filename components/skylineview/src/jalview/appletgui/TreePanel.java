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

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.io.*;

public class TreePanel
    extends Frame implements ActionListener, ItemListener
{
  SequenceI[] seq;
  String type;
  String pwtype;
  int start;
  int end;
  TreeCanvas treeCanvas;
  NJTree tree;
  AlignViewport av;

  public NJTree getTree()
  {
    return tree;
  }

  /**
   * Creates a new TreePanel object.
   *
   * @param av DOCUMENT ME!
   * @param seqVector DOCUMENT ME!
   * @param type DOCUMENT ME!
   * @param pwtype DOCUMENT ME!
   * @param s DOCUMENT ME!
   * @param e DOCUMENT ME!
   */
  public TreePanel(AlignViewport av, String type, String pwtype)
  {
    try
    {
      jbInit();
      this.setMenuBar(jMenuBar1);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    initTreePanel(av, type, pwtype, null);
  }

  /**
   * Creates a new TreePanel object.
   *
   * @param av DOCUMENT ME!
   * @param seqVector DOCUMENT ME!
   * @param newtree DOCUMENT ME!
   * @param type DOCUMENT ME!
   * @param pwtype DOCUMENT ME!
   */
  public TreePanel(AlignViewport av,
                   String type,
                   String pwtype,
                   NewickFile newtree)
  {
    try
    {
      jbInit();
      this.setMenuBar(jMenuBar1);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    initTreePanel(av, type, pwtype, newtree);
  }

  void initTreePanel(AlignViewport av,
                     String type,
                     String pwtype,
                     NewickFile newTree)
  {

    this.av = av;
    this.type = type;
    this.pwtype = pwtype;

    treeCanvas = new TreeCanvas(av, scrollPane);
    scrollPane.add(treeCanvas);

    TreeLoader tl = new TreeLoader(newTree);
    tl.start();

  }

  void showOriginalData()
  {
    // decide if av alignment is sufficiently different to original data to warrant a new window to be created
    // create new alignmnt window with hidden regions (unhiding hidden regions yields unaligned seqs)
    // or create a selection box around columns in alignment view
    // test Alignment(SeqCigar[])
    if (tree.seqData != null)
    {
      Object[] alAndColsel = tree.seqData.getAlignmentAndColumnSelection(av.
          getGapCharacter());

      if (alAndColsel != null && alAndColsel[0] != null)
      {
        Alignment al = new Alignment( (SequenceI[]) alAndColsel[0]);
        AlignFrame af = new AlignFrame(al,
                                       av.applet,
                                       "Original Data for Tree",
                                       false);

        af.viewport.setHiddenColumns( (ColumnSelection) alAndColsel[1]);
      }
    }
    else
    {
      System.out.println("Original Tree Data not available");
    }
  }

  class TreeLoader
      extends Thread
  {
    NewickFile newtree;
    jalview.datamodel.AlignmentView odata = null;

    public TreeLoader(NewickFile newtree)
    {
      this.newtree = newtree;
    }

    public void run()
    {
      if (newtree != null)
      {
        if (odata == null)
        {
          tree = new NJTree(av.alignment.getSequencesArray(),
                            newtree);
        }
        else
        {
          tree = new NJTree(av.alignment.getSequencesArray(), odata, newtree);
        }

      }
      else
      {
        int start, end;
        SequenceI[] seqs;
        AlignmentView seqStrings = av.getAlignmentView(av.getSelectionGroup() != null);
        if (av.getSelectionGroup() == null)
        {
          start = 0;
          end = av.alignment.getWidth();
          seqs = av.alignment.getSequencesArray();
        }
        else
        {
          start = av.getSelectionGroup().getStartRes();
          end = av.getSelectionGroup().getEndRes() + 1;
          seqs = av.getSelectionGroup().getSequencesInOrder(av.alignment);
        }

        tree = new NJTree(seqs, seqStrings, type, pwtype, start, end);
      }

      tree.reCount(tree.getTopNode());
      tree.findHeight(tree.getTopNode());
      treeCanvas.setTree(tree);
      if (newtree != null)
      {
        distanceMenu.setState(newtree.HasDistances());
        bootstrapMenu.setState(newtree.HasBootstrap());
        treeCanvas.setShowBootstrap(newtree.HasBootstrap());
        treeCanvas.setShowDistances(newtree.HasDistances());
      }

      treeCanvas.repaint();

      av.setCurrentTree(tree);

    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == newickOutput)
    {
      newickOutput_actionPerformed();
    }
    else if (evt.getSource() == fontSize)
    {
      fontSize_actionPerformed();
    }
    else if (evt.getSource() == inputData)
    {
      showOriginalData();
    }
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == fitToWindow)
    {
      treeCanvas.fitToWindow = fitToWindow.getState();
    }

    else if (evt.getSource() == distanceMenu)
    {
      treeCanvas.setShowDistances(distanceMenu.getState());
    }

    else if (evt.getSource() == bootstrapMenu)
    {
      treeCanvas.setShowBootstrap(bootstrapMenu.getState());
    }

    else if (evt.getSource() == placeholdersMenu)
    {
      treeCanvas.setMarkPlaceholders(placeholdersMenu.getState());
    }

    treeCanvas.repaint();
  }

  public void newickOutput_actionPerformed()
  {
    jalview.io.NewickFile fout = new jalview.io.NewickFile(tree.getTopNode());
    String output = fout.print(false, true);
    CutAndPasteTransfer cap = new CutAndPasteTransfer(false, null);
    cap.setText(output);
    java.awt.Frame frame = new java.awt.Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, type + " " + pwtype, 500, 100);
  }

  public java.awt.Font getTreeFont()
  {
    return treeCanvas.font;
  }

  public void setTreeFont(java.awt.Font font)
  {
    treeCanvas.font = font;
    treeCanvas.repaint();
  }

  protected void fontSize_actionPerformed()
  {
    if (treeCanvas == null)
    {
      return;
    }

    new FontChooser(this);
  }

  BorderLayout borderLayout1 = new BorderLayout();
  protected ScrollPane scrollPane = new ScrollPane();
  MenuBar jMenuBar1 = new MenuBar();
  Menu jMenu2 = new Menu();
  protected MenuItem fontSize = new MenuItem();
  protected CheckboxMenuItem bootstrapMenu = new CheckboxMenuItem();
  protected CheckboxMenuItem distanceMenu = new CheckboxMenuItem();
  protected CheckboxMenuItem placeholdersMenu = new CheckboxMenuItem();
  protected CheckboxMenuItem fitToWindow = new CheckboxMenuItem();
  Menu fileMenu = new Menu();
  MenuItem newickOutput = new MenuItem();
  MenuItem inputData = new MenuItem();

  private void jbInit()
      throws Exception
  {
    setLayout(borderLayout1);
    this.setBackground(Color.white);
    this.setFont(new java.awt.Font("Verdana", 0, 12));
    jMenu2.setLabel("View");
    fontSize.setLabel("Font...");
    fontSize.addActionListener(this);
    bootstrapMenu.setLabel("Show Bootstrap Values");
    bootstrapMenu.addItemListener(this);
    distanceMenu.setLabel("Show Distances");
    distanceMenu.addItemListener(this);
    placeholdersMenu.setLabel("Mark Unassociated Leaves");
    placeholdersMenu.addItemListener(this);
    fitToWindow.setState(true);
    fitToWindow.setLabel("Fit To Window");
    fitToWindow.addItemListener(this);
    fileMenu.setLabel("File");
    newickOutput.setLabel("Newick Format");
    newickOutput.addActionListener(this);
    inputData.setLabel("Input Data...");

    add(scrollPane, BorderLayout.CENTER);
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(jMenu2);
    jMenu2.add(fitToWindow);
    jMenu2.add(fontSize);
    jMenu2.add(distanceMenu);
    jMenu2.add(bootstrapMenu);
    jMenu2.add(placeholdersMenu);
    fileMenu.add(newickOutput);
    fileMenu.add(inputData);
    inputData.addActionListener(this);
  }

}
