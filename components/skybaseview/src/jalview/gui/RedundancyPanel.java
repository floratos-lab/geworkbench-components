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

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import jalview.commands.*;
import jalview.datamodel.*;
import jalview.jbgui.*;
import jalview.util.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class RedundancyPanel
    extends GSliderPanel implements Runnable
{
  AlignFrame af;
  AlignmentPanel ap;
  Stack historyList = new Stack(); // simpler than synching with alignFrame.
  float[] redundancy;
  SequenceI[] originalSequences;
  JInternalFrame frame;
  Vector redundantSeqs;

  /**
   * Creates a new RedundancyPanel object.
   *
   * @param ap DOCUMENT ME!
   * @param af DOCUMENT ME!
   */
  public RedundancyPanel(final AlignmentPanel ap, AlignFrame af)
  {
    this.ap = ap;
    this.af = af;
    redundantSeqs = new Vector();

    slider.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        valueField.setText(slider.getValue() + "");
        sliderValueChanged();
      }
    });

    applyButton.setText("Remove");
    allGroupsCheck.setVisible(false);
    slider.setMinimum(0);
    slider.setMaximum(100);
    slider.setValue(100);

    Thread worker = new Thread(this);
    worker.start();

    frame = new JInternalFrame();
    frame.setContentPane(this);
    Desktop.addInternalFrame(frame, "Redundancy threshold selection", 400,
                             100, false);
    frame.addInternalFrameListener(new InternalFrameAdapter()
    {
      public void internalFrameClosing(InternalFrameEvent evt)
      {
        ap.idPanel.idCanvas.setHighlighted(null);
      }
    }
    );

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
    JProgressBar progress = new JProgressBar();
    progress.setIndeterminate(true);
    southPanel.add(progress, java.awt.BorderLayout.SOUTH);

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

    if (ap.av.hasHiddenColumns)
    {
      omitHidden = ap.av.getViewAsString(sg != null);
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

        pid = Comparison.PID(seqi, seqj);

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

    progress.setIndeterminate(false);
    progress.setVisible(false);
    progress = null;

    label.setText("Enter the redundancy threshold");
    slider.setVisible(true);
    applyButton.setEnabled(true);
    valueField.setVisible(true);

    validate();
    // System.out.println((System.currentTimeMillis()-start));
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
        redundantSeqs.remove(originalSequences[i]);
      }
      else if (!redundantSeqs.contains(originalSequences[i]))
      {
        redundantSeqs.add(originalSequences[i]);
      }

    }

    ap.idPanel.idCanvas.setHighlighted(redundantSeqs);
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void applyButton_actionPerformed(ActionEvent e)
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

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  public void undoButton_actionPerformed(ActionEvent e)
  {
    CommandI command = (CommandI) historyList.pop();
    command.undoCommand(af.getViewAlignments());

    if (ap.av.historyList.contains(command))
    {
      ap.av.historyList.remove(command);
      af.updateEditMenuBar();
    }

    ap.paintAlignment(true);

    if (historyList.size() == 0)
    {
      undoButton.setEnabled(false);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
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

}
