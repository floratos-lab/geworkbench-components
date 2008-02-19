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

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jalview.datamodel.*;
import jalview.io.*;

public class AnnotationExporter
    extends JPanel
{
  JInternalFrame frame;
  AlignmentPanel ap;
  boolean features = true;
  AlignmentAnnotation[] annotations;
  Vector sequenceGroups;
  Hashtable alignmentProperties;

  public AnnotationExporter()
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame,
                             "",
                             260, 125);
  }

  public void exportFeatures(AlignmentPanel ap)
  {
    this.ap = ap;
    features = true;
    frame.setTitle("Export Features");
  }

  public void exportAnnotations(AlignmentPanel ap,
                                AlignmentAnnotation[] annotations,
                                Vector sequenceGroups,
                                Hashtable alProperties)
  {
    this.ap = ap;
    features = false;
    GFFFormat.setVisible(false);
    this.annotations = annotations;
    this.sequenceGroups = sequenceGroups;
    this.alignmentProperties = alProperties;
    frame.setTitle("Export Annotations");
  }

  public void toFile_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
        jalview.bin.Cache.getProperty("LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
        features ? "Save Features to File" : "Save Annotation to File");
    chooser.setToolTipText("Save");

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String text = "No features found on alignment";
      if (features)
      {
        if (GFFFormat.isSelected())
        {
          text = new FeaturesFile().printGFFFormat(
              ap.av.alignment.getDataset().getSequencesArray(),
              ap.av.featuresDisplayed);
        }
        else
        {
          text = new FeaturesFile().printJalviewFormat(
              ap.av.alignment.getDataset().getSequencesArray(),
              ap.av.featuresDisplayed);
        }
      }
      else
      {
        text = new AnnotationFile().printAnnotations(
            annotations,
            sequenceGroups,
            alignmentProperties);
      }

      try
      {
        java.io.PrintWriter out = new java.io.PrintWriter(
            new java.io.FileWriter(chooser.getSelectedFile()));

        out.print(text);
        out.close();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    close_actionPerformed(null);
  }

  public void toTextbox_actionPerformed(ActionEvent e)
  {
    String text = "No features found on alignment";
    if (features)
    {
      if (GFFFormat.isSelected())
      {
        text = new FeaturesFile().printGFFFormat(
            ap.av.alignment.getDataset().getSequencesArray(),
            ap.av.featuresDisplayed);
      }
      else
      {
        text = new FeaturesFile().printJalviewFormat(
            ap.av.alignment.getDataset().getSequencesArray(),
            ap.av.featuresDisplayed);
      }
    }
    else if (!features)
    {
      text = new AnnotationFile().printAnnotations(
          annotations,
          sequenceGroups,
          alignmentProperties);
    }

    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    cap.setText(text);
    Desktop.addInternalFrame(cap,
                             (features ? "Features for - " :
                              "Annotations for - ")
                             + ap.alignFrame.getTitle(),
                             600,
                             500);

    close_actionPerformed(null);
  }

  public void close_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
    }
    catch (java.beans.PropertyVetoException ex)
    {}
  }

  private void jbInit()
      throws Exception
  {
    this.setLayout(flowLayout1);
    toFile.setText("to File");
    toFile.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        toFile_actionPerformed(e);
      }
    });
    toTextbox.setText("to Textbox");
    toTextbox.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        toTextbox_actionPerformed(e);
      }
    });
    close.setText("Close");
    close.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed(e);
      }
    });
    jalviewFormat.setOpaque(false);
    jalviewFormat.setSelected(true);
    jalviewFormat.setText("Jalview");
    GFFFormat.setOpaque(false);
    GFFFormat.setText("GFF");
    jLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel1.setText("Format: ");
    this.setBackground(Color.white);
    jPanel3.setBorder(BorderFactory.createEtchedBorder());
    jPanel3.setOpaque(false);
    jPanel1.setOpaque(false);
    jPanel1.add(toFile);
    jPanel1.add(toTextbox);
    jPanel1.add(close);
    jPanel3.add(jLabel1);
    jPanel3.add(jalviewFormat);
    jPanel3.add(GFFFormat);
    buttonGroup.add(jalviewFormat);
    buttonGroup.add(GFFFormat);
    this.add(jPanel3, null);
    this.add(jPanel1, null);
  }

  JPanel jPanel1 = new JPanel();
  JButton toFile = new JButton();
  JButton toTextbox = new JButton();
  JButton close = new JButton();
  ButtonGroup buttonGroup = new ButtonGroup();
  JRadioButton jalviewFormat = new JRadioButton();
  JRadioButton GFFFormat = new JRadioButton();
  JLabel jLabel1 = new JLabel();
  JPanel jPanel3 = new JPanel();
  FlowLayout flowLayout1 = new FlowLayout();

}
