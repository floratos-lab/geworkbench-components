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

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.jbgui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class PairwiseAlignPanel
    extends GPairwiseAlignPanel
{

  AlignViewport av;
  Vector sequences;

  /**
   * Creates a new PairwiseAlignPanel object.
   *
   * @param av DOCUMENT ME!
   */
  public PairwiseAlignPanel(AlignViewport av)
  {
    super();
    this.av = av;

    sequences = new Vector();

    SequenceI[] seqs;
    String[] seqStrings = av.getViewAsString(true);

    if (av.getSelectionGroup() == null)
    {
      seqs = av.alignment.getSequencesArray();
    }
    else
    {
      seqs = av.getSelectionGroup().getSequencesInOrder(av.alignment);
    }

    String type = (av.alignment.isNucleotide()) ? AlignSeq.DNA : AlignSeq.PEP;

    float[][] scores = new float[seqs.length][seqs.length];
    double totscore = 0;
    int count = seqs.length;

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
        sequences.add(seq);

        seq = new Sequence(as.getS2().getName(),
                           as.getAStr2(),
                           as.getS2().getStart(),
                           as.getS2().getEnd());
        sequences.add(seq);
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

  /**
   * DOCUMENT ME!
   *
   * @param e DOCUMENT ME!
   */
  protected void viewInEditorButton_actionPerformed(ActionEvent e)
  {
    Sequence[] seq = new Sequence[sequences.size()];

    for (int i = 0; i < sequences.size(); i++)
    {
      seq[i] = (Sequence) sequences.elementAt(i);
    }

    AlignFrame af = new AlignFrame(new Alignment(seq),
                                   AlignFrame.DEFAULT_WIDTH,
                                   AlignFrame.DEFAULT_HEIGHT);

    Desktop.addInternalFrame(af, "Pairwise Aligned Sequences",
                             AlignFrame.DEFAULT_WIDTH,
                             AlignFrame.DEFAULT_HEIGHT);
  }
}
