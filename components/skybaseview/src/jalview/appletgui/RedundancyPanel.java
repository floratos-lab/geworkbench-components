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

import jalview.commands.*;
import jalview.datamodel.*;

public class RedundancyPanel
    extends SliderPanel implements Runnable, WindowListener
{
  AlignmentPanel ap;

  Stack historyList = new Stack(); // simpler than synching with alignFrame.
  float[] redundancy;
  SequenceI[] originalSequences;
  Frame frame;
  Vector redundantSeqs;

  public RedundancyPanel(AlignmentPanel ap)
  {
    super(ap, 0, false, null);

    redundantSeqs = new Vector();
    this.ap = ap;
    undoButton.setVisible(true);
    applyButton.setVisible(true);
    allGroupsCheck.setVisible(false);

    label.setText("Enter the redundancy threshold");
    valueField.setText("100");

    slider.setVisibleAmount(1);
    slider.setMinimum(0);
    slider.setMaximum(100 + slider.getVisibleAmount());
    slider.setValue(100);

    slider.addAdjustmentListener(new AdjustmentListener()
    {
      public void adjustmentValueChanged(AdjustmentEvent evt)
      {
        valueField.setText(slider.getValue() + "");
        sliderValueChanged();
      }
    });

    frame = new Frame();
    frame.add(this);
    jalview.bin.JalviewLite.addFrame(frame, "Redundancy threshold selection",
                                     400, 100);

    frame.addWindowListener(this);

    Thread worker = new Thread(this);
    worker.start();
  }

  /**
   * This is a copy of remove redundancy in jalivew.datamodel.Alignment
   * except we dont want to remove redundancy, just calculate once
   * so we can use the slider to dynamically hide redundant sequences
   *
   * @param threshold DOCUMENT ME!
   * @param sel DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public void run()
  {
    label.setText("Calculating....");

    slider.setVisible(false);
    applyButton.setEnabled(false);
    valueField.setVisible(false);

    validate();

    String[] omitHidden = null;

    SequenceGroup sg = ap.av.getSelectionGroup();
    int height;

    int start, end;

    if ( (sg != null) && (sg.getSize() >= 1))
    {
      originalSequences = sg.getSequencesInOrder(ap.av.alignment);
      start = sg.getStartRes();
      end = sg.getEndRes();
    }
    else
    {
      originalSequences = ap.av.alignment.getSequencesArray();
      start = 0;
      end = ap.av.alignment.getWidth();
    }

    height = originalSequences.length;

    redundancy = new float[height];
    for (int i = 0; i < height; i++)
    {
      redundancy[i] = 0f;
    }

    //  if (ap.av.hasHiddenColumns)
    {
      //   omitHidden = ap.av.getSelectionAsString();
    }

    // long start = System.currentTimeMillis();

    float pid;
    String seqi, seqj;
    for (int i = 0; i < height; i++)
    {
      for (int j = 0; j < i; j++)
      {
        if (i == j)
        {
          continue;
        }

        if (omitHidden == null)
        {
          seqi = originalSequences[i].getSequenceAsString(start, end);
          seqj = originalSequences[j].getSequenceAsString(start, end);
        }
        else
        {
          seqi = omitHidden[i];
          seqj = omitHidden[j];
        }

        pid = jalview.util.Comparison.PID(seqi, seqj);

        if (seqj.length() < seqi.length())
        {
          redundancy[j] = Math.max(pid, redundancy[j]);
        }
        else
        {
          redundancy[i] = Math.max(pid, redundancy[i]);
        }

      }
    }

    label.setText("Enter the redundancy threshold");
    slider.setVisible(true);
    applyButton.setEnabled(true);
    valueField.setVisible(true);

    validate();
    // System.out.println("blob done "+ (System.currentTimeMillis()-start));
  }

  void sliderValueChanged()
  {
    if (redundancy == null)
    {
      return;
    }

    float value = slider.getValue();

    for (int i = 0; i < redundancy.length; i++)
    {
      if (value > redundancy[i])
      {
        redundantSeqs.removeElement(originalSequences[i]);
      }
      else if (!redundantSeqs.contains(originalSequences[i]))
      {
        redundantSeqs.addElement(originalSequences[i]);
      }
    }

    ap.idPanel.idCanvas.setHighlighted(redundantSeqs);
    PaintRefresher.Refresh(this,
                           ap.av.getSequenceSetId(),
                           true,
                           true);

  }

  public void applyButton_actionPerformed()
  {
    Vector del = new Vector();

    undoButton.setEnabled(true);

    float value = slider.getValue();
    SequenceGroup sg = ap.av.getSelectionGroup();

    for (int i = 0; i < redundancy.length; i++)
    {
      if (value <= redundancy[i])
      {
        del.addElement(originalSequences[i]);
      }
    }

    // This has to be done before the restoreHistoryItem method of alignFrame will
    // actually restore these sequences.
    if (del.size() > 0)
    {
      SequenceI[] deleted = new SequenceI[del.size()];

      int width = 0;
      for (int i = 0; i < del.size(); i++)
      {
        deleted[i] = (SequenceI) del.elementAt(i);
        if (deleted[i].getLength() > width)
        {
          width = deleted[i].getLength();
        }
      }

      EditCommand cut = new EditCommand("Remove Redundancy",
                                        EditCommand.CUT, deleted, 0, width,
                                        ap.av.alignment);

      for (int i = 0; i < del.size(); i++)
      {
        ap.av.alignment.deleteSequence(deleted[i]);
        PaintRefresher.Refresh(this,
                               ap.av.getSequenceSetId(),
                               true,
                               true);
        if (sg != null)
        {
          sg.deleteSequence(deleted[i], false);
        }
      }

      historyList.push(cut);

      ap.alignFrame.addHistoryItem(cut);

      ap.av.firePropertyChange("alignment", null,
                               ap.av.getAlignment().getSequences());
    }

  }

  public void undoButton_actionPerformed()
  {
    CommandI command = (CommandI) historyList.pop();
    command.undoCommand(null);

    if (ap.av.historyList.contains(command))
    {
      ap.av.historyList.removeElement(command);
      ap.alignFrame.updateEditMenuBar();
    }

    ap.paintAlignment(true);

    if (historyList.size() == 0)
    {
      undoButton.setEnabled(false);
    }
  }

  public void valueField_actionPerformed(ActionEvent e)
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

  public void windowOpened(WindowEvent evt)
  {}

  public void windowClosing(WindowEvent evt)
  {
    ap.idPanel.idCanvas.setHighlighted(null);
  }

  public void windowClosed(WindowEvent evt)
  {}

  public void windowActivated(WindowEvent evt)
  {}

  public void windowDeactivated(WindowEvent evt)
  {}

  public void windowIconified(WindowEvent evt)
  {}

  public void windowDeiconified(WindowEvent evt)
  {}
}
