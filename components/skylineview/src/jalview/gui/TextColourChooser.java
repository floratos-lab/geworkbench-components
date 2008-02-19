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

public class TextColourChooser
{
  AlignmentPanel ap;
  SequenceGroup sg;

  public void chooseColour(AlignmentPanel ap, SequenceGroup sg)
  {
    this.ap = ap;
    this.sg = sg;

    int original1, original2, originalThreshold;
    if (sg == null)
    {
      original1 = ap.av.textColour.getRGB();
      original2 = ap.av.textColour2.getRGB();
      originalThreshold = ap.av.thresholdTextColour;
    }
    else
    {
      original1 = sg.textColour.getRGB();
      original2 = sg.textColour2.getRGB();
      originalThreshold = sg.thresholdTextColour;
    }

    final JSlider slider = new JSlider(0, 750, originalThreshold);
    final JPanel col1 = new JPanel();
    col1.setPreferredSize(new Dimension(40, 20));
    col1.setBorder(BorderFactory.createEtchedBorder());
    col1.setToolTipText("Dark Colour");
    col1.setBackground(new Color(original1));
    final JPanel col2 = new JPanel();
    col2.setPreferredSize(new Dimension(40, 20));
    col2.setBorder(BorderFactory.createEtchedBorder());
    col2.setToolTipText("Light Colour");
    col2.setBackground(new Color(original2));
    final JPanel bigpanel = new JPanel(new BorderLayout());
    JPanel panel = new JPanel();
    bigpanel.add(panel, BorderLayout.CENTER);
    bigpanel.add(new JLabel(
        "<html><i>Select a dark and light text colour, then set the threshold to"
        + "<br>switch between colours, based on background colour</i></html>"),
                 BorderLayout.NORTH);
    panel.add(col1);
    panel.add(slider);
    panel.add(col2);

    col1.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        Color col = JColorChooser.showDialog(bigpanel,
                                             "Select Colour for Text",
                                             col1.getBackground());
        if (col != null)
        {
          colour1Changed(col);
          col1.setBackground(col);
        }
      }
    });

    col2.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent e)
      {
        Color col = JColorChooser.showDialog(bigpanel,
                                             "Select Colour for Text",
                                             col2.getBackground());
        if (col != null)
        {
          colour2Changed(col);
          col2.setBackground(col);
        }
      }
    });

    slider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        thresholdChanged(slider.getValue());
      }
    });

    int reply = JOptionPane.showInternalOptionDialog(
        ap,
        bigpanel,
        "Adjust Foreground Text Colour Threshold",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        null, null);

    if (reply == JOptionPane.CANCEL_OPTION)
    {
      if (sg == null)
      {
        ap.av.textColour = new Color(original1);
        ap.av.textColour2 = new Color(original2);
        ap.av.thresholdTextColour = originalThreshold;
      }
      else
      {
        sg.textColour = new Color(original1);
        sg.textColour2 = new Color(original2);
        sg.thresholdTextColour = originalThreshold;
      }
    }
  }

  void colour1Changed(Color col)
  {
    if (sg == null)
    {
      ap.av.textColour = col;
      if (ap.av.colourAppliesToAllGroups)
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.textColour = col;
    }

    ap.paintAlignment(true);
  }

  void colour2Changed(Color col)
  {
    if (sg == null)
    {
      ap.av.textColour2 = col;
      if (ap.av.colourAppliesToAllGroups)
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.textColour2 = col;
    }

    ap.paintAlignment(true);
  }

  void thresholdChanged(int value)
  {
    if (sg == null)
    {
      ap.av.thresholdTextColour = value;
      if (ap.av.colourAppliesToAllGroups)
      {
        setGroupTextColour();
      }
    }
    else
    {
      sg.thresholdTextColour = value;
    }

    ap.paintAlignment(true);
  }

  void setGroupTextColour()
  {
    if (ap.av.alignment.getGroups() == null)
    {
      return;
    }

    Vector groups = ap.av.alignment.getGroups();

    for (int i = 0; i < groups.size(); i++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(i);
      sg.textColour = ap.av.textColour;
      sg.textColour2 = ap.av.textColour2;
      sg.thresholdTextColour = ap.av.thresholdTextColour;
    }
  }

}
