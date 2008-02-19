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

public class PairwiseAlignPanel
    extends Panel implements ActionListener
{
  Vector sequences = new Vector();
  AlignmentPanel ap;

  public PairwiseAlignPanel(AlignmentPanel ap)
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
    sequences = new Vector();

    SequenceI[] seqs;
    String[] seqStrings = ap.av.getViewAsString(true);

    if (ap.av.getSelectionGroup() == null)
    {
      seqs = ap.av.alignment.getSequencesArray();
    }
    else
    {
      seqs = ap.av.getSelectionGroup().getSequencesInOrder(ap.av.alignment);
    }

    float scores[][] = new float[seqs.length][seqs.length];
    double totscore = 0;
    int count = ap.av.getSelectionGroup().getSize();
    String type = (ap.av.alignment.isNucleotide()) ? AlignSeq.DNA :
        AlignSeq.PEP;
    Sequence seq;

    for (int i = 1; i < count; i++)
    {
      for (int j = 0; j < i; j++)
      {

        AlignSeq as = new AlignSeq(seqs[i], seqStrings[i],
                                   seqs[j], seqStrings[j], type);

        if (as.s1str.length() == 0 || as.s2str.length() == 0)
        {
          continue;
        }

        as.calcScoreMatrix();
        as.traceAlignment();

        as.printAlignment(System.out);
        scores[i][j] = (float) as.getMaxScore() / (float) as.getASeq1().length;
        totscore = totscore + scores[i][j];

        textarea.append(as.getOutput());
        seq = new Sequence(as.getS1().getName(),
                           as.getAStr1(),
                           as.getS1().getStart(),
                           as.getS1().getEnd()
            );
        sequences.addElement(seq);

        seq = new Sequence(as.getS2().getName(),
                           as.getAStr2(),
                           as.getS2().getStart(),
                           as.getS2().getEnd());
        sequences.addElement(seq);
      }
    }

    if (count > 2)
    {
      System.out.println(
          "Pairwise alignment scaled similarity score matrix\n");

      for (int i = 0; i < count; i++)
      {
        jalview.util.Format.print(System.out, "%s \n",
                                  ("" + i) + " " +
                                  seqs[i].getName());
      }

      System.out.println("\n");

      for (int i = 0; i < count; i++)
      {
        for (int j = 0; j < i; j++)
        {
          jalview.util.Format.print(System.out, "%7.3f",
                                    scores[i][j] / totscore);
        }
      }

      System.out.println("\n");
    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    if (evt.getSource() == viewInEditorButton)
    {
      viewInEditorButton_actionPerformed();
    }
  }

  protected void viewInEditorButton_actionPerformed()
  {

    Sequence[] seq = new Sequence[sequences.size()];

    for (int i = 0; i < sequences.size(); i++)
    {
      seq[i] = (Sequence) sequences.elementAt(i);
    }

    new AlignFrame(new Alignment(seq),
                   ap.av.applet,
                   "Pairwise Aligned Sequences",
                   false);

  }

  protected ScrollPane scrollPane = new ScrollPane();
  protected TextArea textarea = new TextArea();
  protected Button viewInEditorButton = new Button();
  Panel jPanel1 = new Panel();
  BorderLayout borderLayout1 = new BorderLayout();

  private void jbInit()
      throws Exception
  {
    this.setLayout(borderLayout1);
    textarea.setFont(new java.awt.Font("Monospaced", 0, 12));
    textarea.setText("");
    viewInEditorButton.setFont(new java.awt.Font("Verdana", 0, 12));
    viewInEditorButton.setLabel("View in alignment editor");
    viewInEditorButton.addActionListener(this);
    this.add(scrollPane, BorderLayout.CENTER);
    scrollPane.add(textarea);
    this.add(jPanel1, BorderLayout.SOUTH);
    jPanel1.add(viewInEditorButton, null);
  }

}
