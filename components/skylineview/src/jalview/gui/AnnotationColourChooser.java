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
import javax.swing.event.*;

import jalview.datamodel.*;
import jalview.schemes.*;
import java.awt.Dimension;

public class AnnotationColourChooser
    extends JPanel
{
  JInternalFrame frame;
  AlignViewport av;
  AlignmentPanel ap;
  ColourSchemeI oldcs;
  Hashtable oldgroupColours;
  jalview.datamodel.AlignmentAnnotation currentAnnotation;
  boolean adjusting = false;

  public AnnotationColourChooser(AlignViewport av, final AlignmentPanel ap)
  {
    oldcs = av.getGlobalColourScheme();
    if (av.alignment.getGroups() != null)
    {
      oldgroupColours = new Hashtable();
      Vector allGroups = ap.av.alignment.getGroups();
      SequenceGroup sg;
      for (int g = 0; g < allGroups.size(); g++)
      {
        sg = (SequenceGroup) allGroups.get(g);
        if (sg.cs != null)
        {
          oldgroupColours.put(sg, sg.cs);
        }
      }
    }
    this.av = av;
    this.ap = ap;
    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame, "Colour by Annotation", 480, 145);


    slider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        if (!adjusting)
        {
          thresholdValue.setText( ( (float) slider.getValue() / 1000f) + "");
          valueChanged();
        }
      }
    });
    slider.addMouseListener(new MouseAdapter()
        {
          public void mouseReleased(MouseEvent evt)
          {
            ap.paintAlignment(true);
          }
        });

    if (av.alignment.getAlignmentAnnotation() == null)
    {
      return;
    }

    if (oldcs instanceof AnnotationColourGradient)
    {
      AnnotationColourGradient acg = (AnnotationColourGradient) oldcs;
      minColour.setBackground(acg.getMinColour());
      maxColour.setBackground(acg.getMaxColour());
    }
    else
    {
      minColour.setBackground(Color.orange);
      maxColour.setBackground(Color.red);
    }

    adjusting = true;
    Vector list = new Vector();
    int index = 1;
    for (int i = 0; i < av.alignment.getAlignmentAnnotation().length; i++)
    {
      String label = av.alignment.getAlignmentAnnotation()[i].label;
      if (!list.contains(label))
        list.addElement(label);
      else
        list.addElement(label+"_"+(index++));
    }

    annotations = new JComboBox(list);

    threshold.addItem("No Threshold");
    threshold.addItem("Above Threshold");
    threshold.addItem("Below Threshold");

    try
    {
      jbInit();
    }
    catch (Exception ex)
    {}

    adjusting = false;

    changeColour();

  }

  public AnnotationColourChooser()
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
    minColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    minColour.setBorder(BorderFactory.createEtchedBorder());
    minColour.setPreferredSize(new Dimension(40, 20));
    minColour.setToolTipText("Minimum Colour");
    minColour.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        if (minColour.isEnabled())
        {
          minColour_actionPerformed();
        }
      }
    });
    maxColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    maxColour.setBorder(BorderFactory.createEtchedBorder());
    maxColour.setPreferredSize(new Dimension(40, 20));
    maxColour.setToolTipText("Maximum Colour");
    maxColour.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        if (maxColour.isEnabled())
        {
          maxColour_actionPerformed();
        }
      }
    });
    ok.setOpaque(false);
    ok.setText("OK");
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        ok_actionPerformed(e);
      }
    });
    cancel.setOpaque(false);
    cancel.setText("Cancel");
    cancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancel_actionPerformed(e);
      }
    });
    this.setLayout(borderLayout1);
    jPanel2.setLayout(flowLayout1);
    annotations.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        annotations_actionPerformed(e);
      }
    });
    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);
    threshold.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        threshold_actionPerformed(e);
      }
    });
    jPanel3.setLayout(flowLayout2);
    thresholdValue.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        thresholdValue_actionPerformed(e);
      }
    });
    slider.setPaintLabels(false);
    slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setOpaque(false);
    slider.setPreferredSize(new Dimension(100, 32));
    thresholdValue.setEnabled(false);
    thresholdValue.setColumns(7);
    jPanel3.setBackground(Color.white);
    currentColours.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    currentColours.setOpaque(false);
    currentColours.setText("Use Original Colours");
    currentColours.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        currentColours_actionPerformed(e);
      }
    });
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin.setText("Threshold is Min/Max");
    thresholdIsMin.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent actionEvent)
      {
        thresholdIsMin_actionPerformed(actionEvent);
      }
    });
    jPanel1.add(ok);
    jPanel1.add(cancel);
    jPanel2.add(annotations);
    jPanel2.add(currentColours);
    jPanel2.add(minColour);
    jPanel2.add(maxColour);
    this.add(jPanel3, java.awt.BorderLayout.CENTER);
    jPanel3.add(threshold);
    jPanel3.add(slider);
    jPanel3.add(thresholdValue);
    jPanel3.add(thresholdIsMin);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel2, java.awt.BorderLayout.NORTH);
  }

  JComboBox annotations;
  JPanel minColour = new JPanel();
  JPanel maxColour = new JPanel();
  JButton ok = new JButton();
  JButton cancel = new JButton();
  JPanel jPanel1 = new JPanel();
  JPanel jPanel2 = new JPanel();
  BorderLayout borderLayout1 = new BorderLayout();
  JComboBox threshold = new JComboBox();
  FlowLayout flowLayout1 = new FlowLayout();
  JPanel jPanel3 = new JPanel();
  FlowLayout flowLayout2 = new FlowLayout();
  JSlider slider = new JSlider();
  JTextField thresholdValue = new JTextField(20);
  JCheckBox currentColours = new JCheckBox();
  JCheckBox thresholdIsMin = new JCheckBox();

  public void minColour_actionPerformed()
  {
    Color col = JColorChooser.showDialog(this,
                                         "Select Colour for Minimum Value",
                                         minColour.getBackground());
    if (col != null)
    {
      minColour.setBackground(col);
    }
    minColour.repaint();
    changeColour();
  }

  public void maxColour_actionPerformed()
  {
    Color col = JColorChooser.showDialog(this,
                                         "Select Colour for Maximum Value",
                                         maxColour.getBackground());
    if (col != null)
    {
      maxColour.setBackground(col);
    }
    maxColour.repaint();
    changeColour();
  }

  void changeColour()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }


    currentAnnotation = av.alignment.getAlignmentAnnotation()[annotations.getSelectedIndex()];

    int aboveThreshold = -1;
    if (threshold.getSelectedItem().equals("Above Threshold"))
    {
      aboveThreshold = AnnotationColourGradient.ABOVE_THRESHOLD;
    }
    else if (threshold.getSelectedItem().equals("Below Threshold"))
    {
      aboveThreshold = AnnotationColourGradient.BELOW_THRESHOLD;
    }

    slider.setEnabled(true);
    thresholdValue.setEnabled(true);

    if (aboveThreshold == AnnotationColourGradient.NO_THRESHOLD)
    {
      slider.setEnabled(false);
      thresholdValue.setEnabled(false);
      thresholdValue.setText("");
    }
    else if (aboveThreshold != AnnotationColourGradient.NO_THRESHOLD &&
             currentAnnotation.threshold == null)
    {
      currentAnnotation.setThreshold(new jalview.datamodel.GraphLine
                                     ( (currentAnnotation.graphMax -
                                        currentAnnotation.graphMin) / 2f,
                                      "Threshold",
                                      Color.black));
    }

    if (aboveThreshold != AnnotationColourGradient.NO_THRESHOLD)
    {
      adjusting = true;
      float range = currentAnnotation.graphMax * 1000 -
          currentAnnotation.graphMin * 1000;

      slider.setMinimum( (int) (currentAnnotation.graphMin * 1000));
      slider.setMaximum( (int) (currentAnnotation.graphMax * 1000));
      slider.setValue( (int) (currentAnnotation.threshold.value * 1000));
      thresholdValue.setText(currentAnnotation.threshold.value + "");
      slider.setMajorTickSpacing( (int) (range / 10f));
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      adjusting = false;
    }

    AnnotationColourGradient acg = null;
    if (currentColours.isSelected())
    {
      acg = new AnnotationColourGradient(
          currentAnnotation,
          av.getGlobalColourScheme(), aboveThreshold);
    }
    else
    {
      acg =
          new AnnotationColourGradient(
              currentAnnotation,
              minColour.getBackground(),
              maxColour.getBackground(),
              aboveThreshold);
    }

    if(currentAnnotation.graphMin==0f&& currentAnnotation.graphMax==0f)
    {
      acg.predefinedColours = true;
    }

    acg.thresholdIsMinMax = thresholdIsMin.isSelected();

    av.setGlobalColourScheme(acg);

    if (av.alignment.getGroups() != null)
    {
      Vector allGroups = ap.av.alignment.getGroups();
      SequenceGroup sg;
      for (int g = 0; g < allGroups.size(); g++)
      {
        sg = (SequenceGroup) allGroups.get(g);

        if (sg.cs == null)
        {
          continue;
        }

        if (currentColours.isSelected())
        {
          sg.cs = new AnnotationColourGradient(
              currentAnnotation,
              sg.cs, aboveThreshold);
        }
        else
        {
          sg.cs = new AnnotationColourGradient(
              currentAnnotation,
              minColour.getBackground(),
              maxColour.getBackground(),
              aboveThreshold);
        }

      }
    }

    ap.paintAlignment(false);
  }

  public void ok_actionPerformed(ActionEvent e)
  {
    changeColour();
    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {}
  }

  public void cancel_actionPerformed(ActionEvent e)
  {
    reset();
    try
    {
      frame.setClosed(true);
    }
    catch (Exception ex)
    {}
  }

  void reset()
  {
    av.setGlobalColourScheme(oldcs);
    if (av.alignment.getGroups() != null)
    {
      Vector allGroups = ap.av.alignment.getGroups();
      SequenceGroup sg;
      for (int g = 0; g < allGroups.size(); g++)
      {
        sg = (SequenceGroup) allGroups.get(g);
        sg.cs = (ColourSchemeI) oldgroupColours.get(sg);
      }
    }
  }

  public void thresholdCheck_actionPerformed(ActionEvent e)
  {
    changeColour();
  }

  public void annotations_actionPerformed(ActionEvent e)
  {
    changeColour();
  }

  public void threshold_actionPerformed(ActionEvent e)
  {
    changeColour();
  }

  public void thresholdValue_actionPerformed(ActionEvent e)
  {
    try
    {
      float f = Float.parseFloat(thresholdValue.getText());
      slider.setValue( (int) (f * 1000));
    }
    catch (NumberFormatException ex)
    {}
  }

  public void valueChanged()
  {
    if (currentColours.isSelected()
        && ! (av.getGlobalColourScheme() instanceof AnnotationColourGradient))
    {
      changeColour();
    }

    currentAnnotation.threshold.value = (float) slider.getValue() / 1000f;
    ap.paintAlignment(false);
  }

  public void currentColours_actionPerformed(ActionEvent e)
  {
    if (currentColours.isSelected())
    {
      reset();
    }

    maxColour.setEnabled(!currentColours.isSelected());
    minColour.setEnabled(!currentColours.isSelected());

    changeColour();
  }

  public void thresholdIsMin_actionPerformed(ActionEvent actionEvent)
  {
    changeColour();
  }

}
