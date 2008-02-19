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

import java.beans.*;
import java.io.*;
import java.util.*;
import javax.imageio.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import org.jibble.epsgraphics.*;
import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.io.*;
import jalview.jbgui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class TreePanel
    extends GTreePanel
{
  String type;
  String pwtype;
  TreeCanvas treeCanvas;
  NJTree tree;
  AlignViewport av;

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
  public TreePanel(AlignmentPanel ap, String type, String pwtype)
  {
    super();
    initTreePanel(ap, type, pwtype, null, null);

    // We know this tree has distances. JBPNote TODO: prolly should add this as a userdefined default
    // showDistances(true);
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
  public TreePanel(AlignmentPanel ap,
                   String type,
                   String pwtype,
                   NewickFile newtree)
  {
    super();
    initTreePanel(ap, type, pwtype, newtree, null);
  }

  public TreePanel(AlignmentPanel av,
                   String type,
                   String pwtype,
                   NewickFile newtree, AlignmentView inputData)
  {
    super();
    initTreePanel(av, type, pwtype, newtree, inputData);
  }

  public AlignmentI getAlignment()
  {
    return treeCanvas.av.getAlignment();
  }

  public AlignViewport getViewPort()
  {
    return treeCanvas.av;
  }

  void initTreePanel(AlignmentPanel ap, String type, String pwtype,
                     NewickFile newTree, AlignmentView inputData)
  {

    av = ap.av;
    this.type = type;
    this.pwtype = pwtype;

    treeCanvas = new TreeCanvas(this, ap, scrollPane);
    scrollPane.setViewportView(treeCanvas);

    PaintRefresher.Register(this, ap.av.getSequenceSetId());

    buildAssociatedViewMenu();

    av.addPropertyChangeListener(new java.beans.PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (evt.getPropertyName().equals("alignment"))
        {
          if (tree == null)
          {
            System.out.println("tree is null");
          }
          if (evt.getNewValue() == null)
          {
            System.out.println("new alignment sequences vector value is null");
          }

          tree.UpdatePlaceHolders( (Vector) evt.getNewValue());
          treeCanvas.nameHash.clear(); // reset the mapping between canvas rectangles and leafnodes
          repaint();
        }
      }
    });

    TreeLoader tl = new TreeLoader(newTree);
    if (inputData != null)
    {
      tl.odata = inputData;
    }
    tl.start();

  }

  public void viewMenu_menuSelected()
  {
    buildAssociatedViewMenu();
  }

  void buildAssociatedViewMenu()
  {
    AlignmentPanel[] aps = PaintRefresher.getAssociatedPanels(av.
        getSequenceSetId());
    if (aps.length == 1 && treeCanvas.ap == aps[0])
    {
      associateLeavesMenu.setVisible(false);
      return;
    }

    associateLeavesMenu.setVisible(true);

    if ( (viewMenu.getItem(viewMenu.getItemCount() - 2) instanceof JMenuItem))
    {
      viewMenu.insertSeparator(viewMenu.getItemCount() - 1);
    }

    associateLeavesMenu.removeAll();

    JRadioButtonMenuItem item;
    ButtonGroup buttonGroup = new ButtonGroup();
    int i, iSize = aps.length;
    final TreePanel thisTreePanel = this;
    for (i = 0; i < iSize; i++)
    {
      final AlignmentPanel ap = aps[i];
      item = new JRadioButtonMenuItem(ap.av.viewName, ap == treeCanvas.ap);
      buttonGroup.add(item);
      item.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
        {
          treeCanvas.applyToAllViews = false;
          treeCanvas.ap = ap;
          treeCanvas.av = ap.av;
          PaintRefresher.Register(thisTreePanel, ap.av.getSequenceSetId());
        }
      });

      associateLeavesMenu.add(item);
    }

    final JRadioButtonMenuItem itemf = new JRadioButtonMenuItem("All Views");
    buttonGroup.add(itemf);
    itemf.setSelected(treeCanvas.applyToAllViews);
    itemf.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        treeCanvas.applyToAllViews = itemf.isSelected();
      }
    });
    associateLeavesMenu.add(itemf);

  }

  class TreeLoader
      extends Thread
  {
    NewickFile newtree;
    jalview.datamodel.AlignmentView odata = null;
    public TreeLoader(NewickFile newtree)
    {
      this.newtree = newtree;
      if (newtree != null)
      {
        // Must be outside run(), as Jalview2XML tries to
        // update distance/bootstrap visibility at the same time
        showBootstrap(newtree.HasBootstrap());
        showDistances(newtree.HasDistances());
      }
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
        if (!tree.hasOriginalSequenceData())
        {
          allowOriginalSeqData(false);
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
        showDistances(true);
      }

      tree.reCount(tree.getTopNode());
      tree.findHeight(tree.getTopNode());
      treeCanvas.setTree(tree);
      treeCanvas.repaint();
      av.setCurrentTree(tree);

    }
  }

  public void showDistances(boolean b)
  {
    treeCanvas.setShowDistances(b);
    distanceMenu.setSelected(b);
  }

  public void showBootstrap(boolean b)
  {
    treeCanvas.setShowBootstrap(b);
    bootstrapMenu.setSelected(b);
  }

  public void showPlaceholders(boolean b)
  {
    placeholdersMenu.setState(b);
    treeCanvas.setMarkPlaceholders(b);
  }

  private void allowOriginalSeqData(boolean b)
  {
    originalSeqData.setVisible(b);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public NJTree getTree()
  {
    return tree;
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void textbox_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();

    StringBuffer buffer = new StringBuffer();

    if (type.equals("AV"))
    {
      buffer.append("Average distance tree using ");
    }
    else
    {
      buffer.append("Neighbour joining tree using ");
    }

    if (pwtype.equals("BL"))
    {
      buffer.append("BLOSUM62");
    }
    else
    {
      buffer.append("PID");
    }

    Desktop.addInternalFrame(cap, buffer.toString(), 500, 100);

    jalview.io.NewickFile fout = new jalview.io.NewickFile(tree.getTopNode());
    cap.setText(fout.print(tree.isHasBootstrap(), tree.isHasDistances(),
                           tree.isHasRootDistance()));
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void saveAsNewick_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(jalview.bin.Cache.
        getProperty(
            "LAST_DIRECTORY"));
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle("Save tree as newick file");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(null);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY",
                                    chooser.getSelectedFile().getParent());

      try
      {
        jalview.io.NewickFile fout = new jalview.io.NewickFile(tree.getTopNode());
        String output = fout.print(tree.isHasBootstrap(), tree.isHasDistances(),
                                   tree.isHasRootDistance());
        java.io.PrintWriter out = new java.io.PrintWriter(new java.io.
            FileWriter(
                choice));
        out.println(output);
        out.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void printMenu_actionPerformed(ActionEvent e)
  {
    //Putting in a thread avoids Swing painting problems
    treeCanvas.startPrinting();
  }

  public void originalSeqData_actionPerformed(ActionEvent e)
  {
    if (!tree.hasOriginalSequenceData())
    {
      jalview.bin.Cache.log.info("Unexpected call to originalSeqData_actionPerformed - should have hidden this menu action.");
      return;
    }
    // decide if av alignment is sufficiently different to original data to warrant a new window to be created
    // create new alignmnt window with hidden regions (unhiding hidden regions yields unaligned seqs)
    // or create a selection box around columns in alignment view
    // test Alignment(SeqCigar[])
    Object[] alAndColsel = tree.seqData.getAlignmentAndColumnSelection(av.
        getGapCharacter());

    if (alAndColsel != null && alAndColsel[0] != null)
    {
      // AlignmentOrder origorder = new AlignmentOrder(alAndColsel[0]);

      Alignment al = new Alignment( (SequenceI[]) alAndColsel[0]);
      Alignment dataset = av.getAlignment().getDataset();
      if (dataset != null)
      {
        al.setDataset(dataset);
      }

      if (true)
      {
        // make a new frame!
        AlignFrame af = new AlignFrame(al, (ColumnSelection) alAndColsel[1],
                                       AlignFrame.DEFAULT_WIDTH,
                                       AlignFrame.DEFAULT_HEIGHT
            );

        //>>>This is a fix for the moment, until a better solution is found!!<<<
        // af.getFeatureRenderer().transferSettings(alignFrame.getFeatureRenderer());

        //           af.addSortByOrderMenuItem(ServiceName + " Ordering",
        //                                     msaorder);

        Desktop.addInternalFrame(af, "Original Data for " + this.title,
                                 AlignFrame.DEFAULT_WIDTH,
                                 AlignFrame.DEFAULT_HEIGHT);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void fitToWindow_actionPerformed(ActionEvent e)
  {
    treeCanvas.fitToWindow = fitToWindow.isSelected();
    repaint();
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void font_actionPerformed(ActionEvent e)
  {
    if (treeCanvas == null)
    {
      return;
    }

    new FontChooser(this);
  }

  public Font getTreeFont()
  {
    return treeCanvas.font;
  }

  public void setTreeFont(Font font)
  {
    if (treeCanvas != null)
    {
      treeCanvas.setFont(font);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void distanceMenu_actionPerformed(ActionEvent e)
  {
    treeCanvas.setShowDistances(distanceMenu.isSelected());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void bootstrapMenu_actionPerformed(ActionEvent e)
  {
    treeCanvas.setShowBootstrap(bootstrapMenu.isSelected());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void placeholdersMenu_actionPerformed(ActionEvent e)
  {
    treeCanvas.setMarkPlaceholders(placeholdersMenu.isSelected());
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void epsTree_actionPerformed(ActionEvent e)
  {
    boolean accurateText = true;

    String renderStyle = jalview.bin.Cache.getDefault("EPS_RENDERING",
        "Prompt each time");

    // If we need to prompt, and if the GUI is visible then
    // Prompt for EPS rendering style
    if (renderStyle.equalsIgnoreCase("Prompt each time")
        && !
        (System.getProperty("java.awt.headless") != null
         && System.getProperty("java.awt.headless").equals("true")))
    {
      EPSOptions eps = new EPSOptions();
      renderStyle = eps.getValue();

      if (renderStyle == null || eps.cancelled)
      {
        return;
      }

    }

    if (renderStyle.equalsIgnoreCase("text"))
    {
      accurateText = false;
    }

    int width = treeCanvas.getWidth();
    int height = treeCanvas.getHeight();

    try
    {
      jalview.io.JalviewFileChooser chooser = new jalview.io.JalviewFileChooser(
          jalview.bin.Cache.getProperty(
              "LAST_DIRECTORY"), new String[]
          {"eps"},
          new String[]
          {"Encapsulated Postscript"},
          "Encapsulated Postscript");
      chooser.setFileView(new jalview.io.JalviewFileView());
      chooser.setDialogTitle("Create EPS file from tree");
      chooser.setToolTipText("Save");

      int value = chooser.showSaveDialog(this);

      if (value != jalview.io.JalviewFileChooser.APPROVE_OPTION)
      {
        return;
      }

      jalview.bin.Cache.setProperty("LAST_DIRECTORY",
                                    chooser.getSelectedFile().getParent());

      FileOutputStream out = new FileOutputStream(chooser.getSelectedFile());
      EpsGraphics2D pg = new EpsGraphics2D("Tree", out, 0, 0, width,
                                           height);

      pg.setAccurateTextMode(accurateText);

      treeCanvas.draw(pg, width, height);

      pg.flush();
      pg.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void pngTree_actionPerformed(ActionEvent e)
  {
    int width = treeCanvas.getWidth();
    int height = treeCanvas.getHeight();

    try
    {
      jalview.io.JalviewFileChooser chooser = new jalview.io.JalviewFileChooser(
          jalview.bin.Cache.getProperty(
              "LAST_DIRECTORY"), new String[]
          {"png"},
          new String[]
          {"Portable network graphics"},
          "Portable network graphics");

      chooser.setFileView(new jalview.io.JalviewFileView());
      chooser.setDialogTitle("Create PNG image from tree");
      chooser.setToolTipText("Save");

      int value = chooser.showSaveDialog(this);

      if (value != jalview.io.JalviewFileChooser.APPROVE_OPTION)
      {
        return;
      }

      jalview.bin.Cache.setProperty("LAST_DIRECTORY",
                                    chooser.getSelectedFile().getParent());

      FileOutputStream out = new FileOutputStream(chooser.getSelectedFile());

      BufferedImage bi = new BufferedImage(width, height,
                                           BufferedImage.TYPE_INT_RGB);
      Graphics png = bi.getGraphics();

      treeCanvas.draw(png, width, height);

      ImageIO.write(bi, "png", out);
      out.close();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
