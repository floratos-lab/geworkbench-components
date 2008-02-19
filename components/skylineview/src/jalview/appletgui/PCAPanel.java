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

import java.util.*;

import java.awt.*;
import java.awt.event.*;

import jalview.analysis.*;
import jalview.datamodel.*;

public class PCAPanel
    extends Frame implements Runnable, ActionListener, ItemListener
{
  PCA pca;
  int top;
  RotatableCanvas rc;
  AlignViewport av;
  SequenceI[] seqs;
  AlignmentView seqstrings;

  public PCAPanel(AlignViewport av)
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    for (int i = 1; i < 8; i++)
    {
      xCombobox.addItem("dim " + i);
      yCombobox.addItem("dim " + i);
      zCombobox.addItem("dim " + i);
    }

    this.av = av;
    seqstrings = av.getAlignmentView(av.getSelectionGroup() != null);
    if (av.getSelectionGroup() == null)
    {
      seqs = av.alignment.getSequencesArray();
    }
    else
    {
      seqs = av.getSelectionGroup().getSequencesInOrder(av.alignment);
    }
    SeqCigar sq[] = seqstrings.getSequences();
    int length = sq[0].getWidth();

    for (int i = 0; i < seqs.length; i++)
    {
      if (sq[i].getWidth() != length)
      {
        System.out.println("Sequences must be equal length for PCA analysis");
        return;
      }
    }

    rc = new RotatableCanvas(av);
    add(rc, BorderLayout.CENTER);

    jalview.bin.JalviewLite.addFrame(this, "Principal component analysis",
                                     400, 400);

    Thread worker = new Thread(this);
    worker.start();
  }

  /**
   * DOCUMENT ME!
   */
  public void run()
  {
    pca = new PCA(seqstrings.getSequenceStrings(' '));
    pca.run();

    // Now find the component coordinates
    int ii = 0;

    while ( (ii < seqs.length) && (seqs[ii] != null))
    {
      ii++;
    }

    double[][] comps = new double[ii][ii];

    for (int i = 0; i < ii; i++)
    {
      if (pca.getEigenvalue(i) > 1e-4)
      {
        comps[i] = pca.component(i);
      }
    }

    //////////////////
    xCombobox.select(0);
    yCombobox.select(1);
    zCombobox.select(2);

    top = pca.getM().rows - 1;

    Vector points = new Vector();
    float[][] scores = pca.getComponents(top - 1, top - 2, top - 3, 100);

    for (int i = 0; i < pca.getM().rows; i++)
    {
      SequencePoint sp = new SequencePoint(seqs[i], scores[i]);
      points.addElement(sp);
    }

    rc.setPoints(points, pca.getM().rows);
    rc.repaint();
    seqs = null;
    this.repaint();
  }

  void doDimensionChange()
  {
    if (top == 0)
    {
      return;
    }

    int dim1 = top - xCombobox.getSelectedIndex();
    int dim2 = top - yCombobox.getSelectedIndex();
    int dim3 = top - zCombobox.getSelectedIndex();

    float[][] scores = pca.getComponents(dim1, dim2, dim3, 100);
    for (int i = 0; i < pca.getM().rows; i++)
    {
      ( (SequencePoint) rc.points.elementAt(i)).coord = scores[i];
    }

    rc.img = null;
    rc.rotmat.setIdentity();
    rc.initAxes();
    rc.paint(rc.getGraphics());
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == inputData)
    {
      showOriginalData();
    }
    else
    {
      values_actionPerformed();
    }
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == xCombobox)
    {
      xCombobox_actionPerformed();
    }
    else if (evt.getSource() == yCombobox)
    {
      yCombobox_actionPerformed();
    }
    else if (evt.getSource() == zCombobox)
    {
      zCombobox_actionPerformed();
    }
  }

  protected void xCombobox_actionPerformed()
  {
    doDimensionChange();
  }

  protected void yCombobox_actionPerformed()
  {
    doDimensionChange();
  }

  protected void zCombobox_actionPerformed()
  {
    doDimensionChange();
  }

  public void values_actionPerformed()
  {

    CutAndPasteTransfer cap = new CutAndPasteTransfer(false, null);
    Frame frame = new Frame();
    frame.add(cap);
    jalview.bin.JalviewLite.addFrame(frame, "PCA details", 500, 500);

    cap.setText(pca.getDetails());
  }

  void showOriginalData()
  {
    // decide if av alignment is sufficiently different to original data to warrant a new window to be created
    // create new alignmnt window with hidden regions (unhiding hidden regions yields unaligned seqs)
    // or create a selection box around columns in alignment view
    // test Alignment(SeqCigar[])
    Object[] alAndColsel = seqstrings.getAlignmentAndColumnSelection(av.
        getGapCharacter());

    if (alAndColsel != null && alAndColsel[0] != null)
    {
      Alignment al = new Alignment( (SequenceI[]) alAndColsel[0]);
      AlignFrame af = new AlignFrame(al,
                                     av.applet,
                                     "Original Data for PCA",
                                     false);

      af.viewport.setHiddenColumns( (ColumnSelection) alAndColsel[1]);
    }
  }

  public void labels_itemStateChanged(ItemEvent itemEvent)
  {
    rc.showLabels(labels.getState());
  }

  Panel jPanel2 = new Panel();
  Label jLabel1 = new Label();
  Label jLabel2 = new Label();
  Label jLabel3 = new Label();
  protected Choice xCombobox = new Choice();
  protected Choice yCombobox = new Choice();
  protected Choice zCombobox = new Choice();
  FlowLayout flowLayout1 = new FlowLayout();
  BorderLayout borderLayout1 = new BorderLayout();
  MenuBar menuBar1 = new MenuBar();
  Menu menu1 = new Menu();
  Menu menu2 = new Menu();
  protected CheckboxMenuItem labels = new CheckboxMenuItem();
  MenuItem values = new MenuItem();
  MenuItem inputData = new MenuItem();

  private void jbInit()
      throws Exception
  {
    this.setLayout(borderLayout1);
    jPanel2.setLayout(flowLayout1);
    jLabel1.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel1.setText("x=");
    jLabel2.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel2.setText("y=");
    jLabel3.setFont(new java.awt.Font("Verdana", 0, 12));
    jLabel3.setText("z=");
    jPanel2.setBackground(Color.white);
    zCombobox.setFont(new java.awt.Font("Verdana", 0, 12));
    zCombobox.addItemListener(this);
    yCombobox.setFont(new java.awt.Font("Verdana", 0, 12));
    yCombobox.addItemListener(this);
    xCombobox.setFont(new java.awt.Font("Verdana", 0, 12));
    xCombobox.addItemListener(this);
    this.setMenuBar(menuBar1);
    menu1.setLabel("File");
    menu2.setLabel("View");
    labels.setLabel("Labels");
    labels.addItemListener(this);
    values.setLabel("Output Values...");
    values.addActionListener(this);
    inputData.setLabel("Input Data...");
    this.add(jPanel2, BorderLayout.SOUTH);
    jPanel2.add(jLabel1, null);
    jPanel2.add(xCombobox, null);
    jPanel2.add(jLabel2, null);
    jPanel2.add(yCombobox, null);
    jPanel2.add(jLabel3, null);
    jPanel2.add(zCombobox, null);
    menuBar1.add(menu1);
    menuBar1.add(menu2);
    menu2.add(labels);
    menu1.add(values);
    menu1.add(inputData);
    inputData.addActionListener(this);
  }

}
