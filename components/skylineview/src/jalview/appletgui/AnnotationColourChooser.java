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

import jalview.datamodel.*;
import jalview.schemes.*;
import java.awt.Rectangle;

public class AnnotationColourChooser
    extends Panel implements ActionListener,
    AdjustmentListener, ItemListener, MouseListener
{
  Frame frame;
  AlignViewport av;
  AlignmentPanel ap;
  ColourSchemeI oldcs;
  Hashtable oldgroupColours;
  jalview.datamodel.AlignmentAnnotation currentAnnotation;
  boolean adjusting = false;

  public AnnotationColourChooser(AlignViewport av, AlignmentPanel ap)
  {
    try
    {
      jbInit();
    }
    catch (Exception ex)
    {}

    oldcs = av.getGlobalColourScheme();
    if (av.alignment.getGroups() != null)
    {
      oldgroupColours = new Hashtable();
      Vector allGroups = ap.av.alignment.getGroups();
      SequenceGroup sg;
      for (int g = 0; g < allGroups.size(); g++)
      {
        sg = (SequenceGroup) allGroups.elementAt(g);
        oldgroupColours.put(sg, sg.cs);
      }
    }
    this.av = av;
    this.ap = ap;

    slider.addAdjustmentListener(this);
    slider.addMouseListener(this);

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

    for (int i = 0; i < list.size(); i++)
    {
        annotations.addItem(list.elementAt(i).toString());
    }

    threshold.addItem("No Threshold");
    threshold.addItem("Above Threshold");
    threshold.addItem("Below Threshold");

    adjusting = false;

    changeColour();

    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame, "Colour by Annotation", 480, 145);
    validate();
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
    minColour.setLabel("Min Colour");
    minColour.addActionListener(this);

    maxColour.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    maxColour.setLabel("Max Colour");
    maxColour.addActionListener(this);

    thresholdIsMin.addItemListener(this);
    ok.setLabel("OK");
    ok.addActionListener(this);

    cancel.setLabel("Cancel");
    cancel.addActionListener(this);

    this.setLayout(borderLayout1);
    jPanel2.setLayout(flowLayout1);
    annotations.addItemListener(this);

    jPanel1.setBackground(Color.white);
    jPanel2.setBackground(Color.white);
    threshold.addItemListener(this);
    jPanel3.setLayout(null);
    thresholdValue.addActionListener(this);

    slider.setBackground(Color.white);
    slider.setEnabled(false);
    slider.setBounds(new Rectangle(153, 3, 93, 21));
    thresholdValue.setEnabled(false);
    thresholdValue.setBounds(new Rectangle(248, 2, 79, 22));
    thresholdValue.setColumns(5);
    jPanel3.setBackground(Color.white);
    currentColours.setFont(new java.awt.Font("Verdana", Font.PLAIN, 11));
    currentColours.setLabel("Use Original Colours");
    currentColours.addItemListener(this);

    threshold.setBounds(new Rectangle(11, 3, 139, 22));
    thresholdIsMin.setBackground(Color.white);
    thresholdIsMin.setLabel("Threshold is min/max");
    thresholdIsMin.setBounds(new Rectangle(328, 3, 135, 23));
    jPanel1.add(ok);
    jPanel1.add(cancel);
    jPanel2.add(annotations);
    jPanel2.add(currentColours);
    jPanel2.add(minColour);
    jPanel2.add(maxColour);
    jPanel3.add(threshold);
    jPanel3.add(slider);
    jPanel3.add(thresholdValue);
    jPanel3.add(thresholdIsMin);
    this.add(jPanel2, java.awt.BorderLayout.NORTH);
    this.add(jPanel3, java.awt.BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
  }

  Choice annotations = new Choice();
  Button minColour = new Button();
  Button maxColour = new Button();
  Button ok = new Button();
  Button cancel = new Button();
  Panel jPanel1 = new Panel();
  Panel jPanel2 = new Panel();
  Choice threshold = new Choice();
  FlowLayout flowLayout1 = new FlowLayout();
  Panel jPanel3 = new Panel();
  Scrollbar slider = new Scrollbar(Scrollbar.HORIZONTAL);
  TextField thresholdValue = new TextField(20);
  Checkbox currentColours = new Checkbox();
  BorderLayout borderLayout1 = new BorderLayout();
  Checkbox thresholdIsMin = new Checkbox();

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == thresholdValue)
    {
      try
      {
        float f = new Float(thresholdValue.getText()).floatValue();
        slider.setValue( (int) (f * 1000));
        adjustmentValueChanged(null);
      }
      catch (NumberFormatException ex)
      {}
    }
    else if (evt.getSource() == minColour)
    {
      minColour_actionPerformed(null);
    }
    else if (evt.getSource() == maxColour)
    {
      maxColour_actionPerformed(null);
    }

    else if (evt.getSource() == ok)
    {
      changeColour();
      frame.setVisible(false);
    }
    else if (evt.getSource() == cancel)
    {
      reset();
      ap.paintAlignment(true);
      frame.setVisible(false);
    }

    else
    {
      changeColour();
    }
  }

  public void itemStateChanged(ItemEvent evt)
  {
    if (evt.getSource() == currentColours)
    {
      if (currentColours.getState())
      {
        reset();
      }

      maxColour.setEnabled(!currentColours.getState());
      minColour.setEnabled(!currentColours.getState());

    }

    changeColour();
  }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    if (!adjusting)
    {
      thresholdValue.setText( ( (float) slider.getValue() / 1000f) + "");
      if (currentColours.getState()
          && ! (av.getGlobalColourScheme() instanceof AnnotationColourGradient))
      {
        changeColour();
      }

      currentAnnotation.threshold.value = (float) slider.getValue() / 1000f;
      ap.paintAlignment(false);
    }
  }

  public void minColour_actionPerformed(Color newCol)
  {
    if (newCol != null)
    {
      minColour.setBackground(newCol);
      minColour.repaint();
      changeColour();
    }
    else
    {
      new UserDefinedColours(this, "Min Colour",
                             minColour.getBackground());
    }

  }

  public void maxColour_actionPerformed(Color newCol)
  {
    if (newCol != null)
    {
      maxColour.setBackground(newCol);
      maxColour.repaint();
      changeColour();
    }
    else
    {
      new UserDefinedColours(this, "Max Colour",
                             maxColour.getBackground());
    }
  }

  void changeColour()
  {
    // Check if combobox is still adjusting
    if (adjusting)
    {
      return;
    }


    currentAnnotation = av.alignment.getAlignmentAnnotation()
        [annotations.getSelectedIndex()];

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

      slider.setMinimum( (int) (currentAnnotation.graphMin * 1000));
      slider.setMaximum( (int) (currentAnnotation.graphMax * 1000));
      slider.setValue( (int) (currentAnnotation.threshold.value * 1000));
      thresholdValue.setText(currentAnnotation.threshold.value + "");
      slider.setEnabled(true);
      thresholdValue.setEnabled(true);
      adjusting = false;
    }

    AnnotationColourGradient acg = null;
    if (currentColours.getState())
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

    if (currentAnnotation.graphMin == 0f && currentAnnotation.graphMax == 0f)
    {
      acg.predefinedColours = true;
    }

    acg.thresholdIsMinMax = thresholdIsMin.getState();

    av.setGlobalColourScheme(acg);

    if (av.alignment.getGroups() != null)
    {
      Vector allGroups = ap.av.alignment.getGroups();
      SequenceGroup sg;
      for (int g = 0; g < allGroups.size(); g++)
      {
        sg = (SequenceGroup) allGroups.elementAt(g);

        if (sg.cs == null)
        {
          continue;
        }

        if (currentColours.getState())
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

  void reset()
  {
    av.setGlobalColourScheme(oldcs);
    if (av.alignment.getGroups() != null)
    {
      Vector allGroups = ap.av.alignment.getGroups();
      SequenceGroup sg;
      for (int g = 0; g < allGroups.size(); g++)
      {
        sg = (SequenceGroup) allGroups.elementAt(g);
        sg.cs = (ColourSchemeI) oldgroupColours.get(sg);
      }
    }
    ap.paintAlignment(true);

  }

  public void mouseClicked(MouseEvent evt){}
  public void mousePressed(MouseEvent evt){}
  public void mouseReleased(MouseEvent evt){ ap.paintAlignment(true);}
  public void mouseEntered(MouseEvent evt){}
  public void mouseExited(MouseEvent evt){}


}
