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

public class SliderPanel
    extends Panel implements ActionListener,
    AdjustmentListener,
    MouseListener
{
  AlignmentPanel ap;
  boolean forConservation = true;
  ColourSchemeI cs;

  static Frame conservationSlider;
  static Frame PIDSlider;

  public static int setConservationSlider(AlignmentPanel ap, ColourSchemeI cs,
                                          String source)
  {
    SliderPanel sp = null;

    if (conservationSlider == null)
    {
      sp = new SliderPanel(ap, cs.getConservationInc(), true, cs);
      conservationSlider = new Frame();
      conservationSlider.add(sp);
    }
    else
    {
      sp = (SliderPanel) conservationSlider.getComponent(0);
      sp.cs = cs;
    }

    conservationSlider.setTitle("Conservation Colour Increment  (" + source +
                                ")");
    if (ap.av.alignment.getGroups() != null)
    {
      sp.setAllGroupsCheckEnabled(true);
    }
    else
    {
      sp.setAllGroupsCheckEnabled(false);
    }

    return sp.getValue();
  }

  public static void showConservationSlider()
  {
    try
    {
      PIDSlider.setVisible(false);
      PIDSlider = null;
    }
    catch (Exception ex)
    {}

    if (!conservationSlider.isVisible())
    {
      jalview.bin.JalviewLite.addFrame(conservationSlider,
                                       conservationSlider.getTitle(), 420, 100);
      conservationSlider.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          conservationSlider = null;
        }
      });

    }

  }

  public static int setPIDSliderSource(AlignmentPanel ap, ColourSchemeI cs,
                                       String source)
  {
    SliderPanel pid = null;
    if (PIDSlider == null)
    {
      pid = new SliderPanel(ap, 50, false, cs);
      PIDSlider = new Frame();
      PIDSlider.add(pid);
    }
    else
    {
      pid = (SliderPanel) PIDSlider.getComponent(0);
      pid.cs = cs;
    }
    PIDSlider.setTitle("Percentage Identity Threshold (" + source + ")");

    if (ap.av.alignment.getGroups() != null)
    {
      pid.setAllGroupsCheckEnabled(true);
    }
    else
    {
      pid.setAllGroupsCheckEnabled(false);
    }

    return pid.getValue();

  }

  public static void showPIDSlider()
  {
    try
    {
      conservationSlider.setVisible(false);
      conservationSlider = null;
    }
    catch (Exception ex)
    {}

    if (!PIDSlider.isVisible())
    {
      jalview.bin.JalviewLite.addFrame(PIDSlider, PIDSlider.getTitle(), 420,
                                       100);
      PIDSlider.addWindowListener(new WindowAdapter()
      {
        public void windowClosing(WindowEvent e)
        {
          PIDSlider = null;
        }
      });
    }

  }

  public SliderPanel(AlignmentPanel ap, int value, boolean forConserve,
                     ColourSchemeI cs)
  {
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    this.ap = ap;
    this.cs = cs;
    forConservation = forConserve;
    undoButton.setVisible(false);
    applyButton.setVisible(false);
    if (forConservation)
    {
      label.setText("Modify conservation visibility");
      slider.setMinimum(0);
      slider.setMaximum(50 + slider.getVisibleAmount());
      slider.setUnitIncrement(1);
    }
    else
    {
      label.setText("Colour residues above % occurence");
      slider.setMinimum(0);
      slider.setMaximum(100 + slider.getVisibleAmount());
      slider.setBlockIncrement(1);
    }

    slider.addAdjustmentListener(this);
    slider.addMouseListener(this);

    slider.setValue(value);
    valueField.setText(value + "");
  }

  public void valueChanged(int i)
  {
    if (cs == null)
    {
      return;
    }

    ColourSchemeI toChange = null;
    Vector allGroups = null;
    int groupIndex = 0;

    if (allGroupsCheck.getState())
    {
      allGroups = ap.av.alignment.getGroups();
      groupIndex = allGroups.size() - 1;
    }
    else
    {
      toChange = cs;
    }

    while (groupIndex > -1)
    {
      if (allGroups != null)
      {
        toChange = ( (SequenceGroup) allGroups.elementAt(groupIndex)).cs;
      }

      if (forConservation)
      {
        toChange.setConservationInc(i);
      }
      else
      {
        toChange.setThreshold(i, ap.av.getIgnoreGapsConsensus());
      }

      groupIndex--;
    }

    ap.seqPanel.seqCanvas.repaint();

  }

  public void setAllGroupsCheckEnabled(boolean b)
  {
    allGroupsCheck.setEnabled(b);
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == applyButton)
    {
      applyButton_actionPerformed();
    }
    else if (evt.getSource() == undoButton)
    {
      undoButton_actionPerformed();
    }
    else if (evt.getSource() == valueField)
    {
      valueField_actionPerformed();
    }
  }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    valueField.setText(slider.getValue() + "");
    valueChanged(slider.getValue());
  }

  public void valueField_actionPerformed()
  {
    try
    {
      int i = Integer.parseInt(valueField.getText());
      slider.setValue(i);
    }
    catch (Exception ex)
    {
      valueField.setText(slider.getValue() + "");
    }
  }

  public void setValue(int value)
  {
    slider.setValue(value);
  }

  public int getValue()
  {
    return Integer.parseInt(valueField.getText());
  }

  // this is used for conservation colours, PID colours and redundancy threshold
  protected Scrollbar slider = new Scrollbar();
  protected TextField valueField = new TextField();
  protected Label label = new Label();
  Panel jPanel1 = new Panel();
  Panel jPanel2 = new Panel();
  protected Button applyButton = new Button();
  protected Button undoButton = new Button();
  FlowLayout flowLayout1 = new FlowLayout();
  protected Checkbox allGroupsCheck = new Checkbox();
  BorderLayout borderLayout1 = new BorderLayout();
  BorderLayout borderLayout2 = new BorderLayout();
  FlowLayout flowLayout2 = new FlowLayout();

  private void jbInit()
      throws Exception
  {
    this.setLayout(borderLayout2);

    // slider.setMajorTickSpacing(10);
    //  slider.setMinorTickSpacing(1);
    //  slider.setPaintTicks(true);
    slider.setBackground(Color.white);
    slider.setFont(new java.awt.Font("Verdana", 0, 11));
    slider.setOrientation(0);
    valueField.setFont(new java.awt.Font("Verdana", 0, 11));
    valueField.setText("      ");
    valueField.addActionListener(this);
    label.setFont(new java.awt.Font("Verdana", 0, 11));
    label.setText("set this label text");
    jPanel1.setLayout(borderLayout1);
    jPanel2.setLayout(flowLayout1);
    applyButton.setFont(new java.awt.Font("Verdana", 0, 11));
    applyButton.setLabel("Apply");
    applyButton.addActionListener(this);
    undoButton.setEnabled(false);
    undoButton.setFont(new java.awt.Font("Verdana", 0, 11));
    undoButton.setLabel("Undo");
    undoButton.addActionListener(this);
    allGroupsCheck.setEnabled(false);
    allGroupsCheck.setFont(new java.awt.Font("Verdana", 0, 11));
    allGroupsCheck.setLabel("Apply threshold to all groups");
    allGroupsCheck.setName("Apply to all Groups");
    this.setBackground(Color.white);
    this.setForeground(Color.black);
    jPanel2.add(label, null);
    jPanel2.add(applyButton, null);
    jPanel2.add(undoButton, null);
    jPanel2.add(allGroupsCheck);
    jPanel1.add(valueField, java.awt.BorderLayout.EAST);
    jPanel1.add(slider, java.awt.BorderLayout.CENTER);
    this.add(jPanel1, java.awt.BorderLayout.SOUTH);
    this.add(jPanel2, java.awt.BorderLayout.CENTER);
  }

  protected void applyButton_actionPerformed()
  {}

  protected void undoButton_actionPerformed()
  {}

  public void mousePressed(MouseEvent evt)
  {}

  public void mouseReleased(MouseEvent evt)
  {
      ap.paintAlignment(true);
  }

  public void mouseClicked(MouseEvent evt)
  {}

  public void mouseEntered(MouseEvent evt)
  {}

  public void mouseExited(MouseEvent evt)
  {}
}
