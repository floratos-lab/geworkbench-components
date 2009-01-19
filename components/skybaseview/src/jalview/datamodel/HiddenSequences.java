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

package jalview.datamodel;

import java.util.*;

public class HiddenSequences
{
  public SequenceI[] hiddenSequences;
  AlignmentI alignment;

  public HiddenSequences(AlignmentI al)
  {
    alignment = al;
  }

  public int getSize()
  {
    if (hiddenSequences == null)
    {
      return 0;
    }
    int count = 0;
    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] != null)
      {
        count++;
      }
    }

    return count;
  }

  public int getWidth()
  {
    int width = 0;
    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] != null && hiddenSequences[i].getLength() > width)
      {
        width = hiddenSequences[i].getLength();
      }
    }

    return width;
  }

  /**
   * Call this method if sequences are removed from the
   * main alignment
   */
  public void adjustHeightSequenceDeleted(int seqIndex)
  {
    if (hiddenSequences == null)
    {
      return;
    }

    int alHeight = alignment.getHeight();

    SequenceI[] tmp = new SequenceI[alHeight + getSize()];
    int deletionIndex = adjustForHiddenSeqs(seqIndex);

    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] == null)
      {
        continue;
      }

      if (i > deletionIndex)
      {
        tmp[i - 1] = hiddenSequences[i];
      }
      else
      {
        tmp[i] = hiddenSequences[i];
      }
    }

    hiddenSequences = tmp;

  }

  /**
   * Call this method if sequences are added to or removed from the
   * main alignment
   */
  public void adjustHeightSequenceAdded()
  {
    if (hiddenSequences == null)
    {
      return;
    }

    int alHeight = alignment.getHeight();

    SequenceI[] tmp = new SequenceI[alHeight + getSize()];
    System.arraycopy(hiddenSequences, 0, tmp, 0, hiddenSequences.length);
    hiddenSequences = tmp;
  }

  public void hideSequence(SequenceI sequence)
  {
    if (hiddenSequences == null)
    {
      hiddenSequences = new SequenceI[alignment.getHeight()];
    }

    int alignmentIndex = alignment.findIndex(sequence);
    alignmentIndex = adjustForHiddenSeqs(alignmentIndex);

    if (hiddenSequences[alignmentIndex] != null)
    {
      System.out.println("ERROR!!!!!!!!!!!");
    }

    hiddenSequences[alignmentIndex] = sequence;

    alignment.deleteSequence(sequence);
  }

  public Vector showAll(Hashtable hiddenReps)
  {
    Vector revealedSeqs = new Vector();
    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] != null)
      {
        Vector tmp = showSequence(i, hiddenReps);
        for (int t = 0; t < tmp.size(); t++)
        {
          revealedSeqs.addElement(tmp.elementAt(t));
        }
      }
    }
    return revealedSeqs;
  }

  public Vector showSequence(int alignmentIndex, Hashtable hiddenReps)
  {
    Vector revealedSeqs = new Vector();
    SequenceI repSequence = alignment.getSequenceAt(alignmentIndex);
    if (repSequence != null &&
        hiddenReps != null
        && hiddenReps.containsKey(repSequence))
    {
      hiddenReps.remove(repSequence);
      revealedSeqs.addElement(repSequence);
    }

    int start = adjustForHiddenSeqs(alignmentIndex - 1);
    int end = adjustForHiddenSeqs(alignmentIndex);
    if (end >= hiddenSequences.length)
    {
      end = hiddenSequences.length - 1;
    }

    for (int index = end; index > start; index--)
    {
      SequenceI seq = hiddenSequences[index];
      hiddenSequences[index] = null;

      if (seq != null)
      {
        if (seq.getLength() > 0)
        {
          revealedSeqs.addElement(seq);
          alignment.getSequences().insertElementAt(seq, alignmentIndex);
        }
        else
        {
          System.out.println(seq.getName() + " has been deleted whilst hidden");
        }
      }

    }

    return revealedSeqs;
  }

  public SequenceI getHiddenSequence(int alignmentIndex)
  {
    return hiddenSequences[alignmentIndex];
  }

  public int findIndexWithoutHiddenSeqs(int alignmentIndex)
  {
    int index = 0;
    int hiddenSeqs = 0;
    if (hiddenSequences.length <= alignmentIndex)
    {
      alignmentIndex = hiddenSequences.length - 1;
    }

    while (index <= alignmentIndex)
    {
      if (hiddenSequences[index] != null)
      {
        hiddenSeqs++;
      }
      index++;
    }
    ;

    return (alignmentIndex - hiddenSeqs);
  }

  public int adjustForHiddenSeqs(int alignmentIndex)
  {
    int index = 0;
    int hSize = hiddenSequences.length;
    while (index <= alignmentIndex && index < hSize)
    {
      if (hiddenSequences[index] != null)
      {
        alignmentIndex++;
      }
      index++;
    }
    ;

    return alignmentIndex;
  }

  public AlignmentI getFullAlignment()
  {
    int isize = hiddenSequences.length;
    SequenceI[] seq = new Sequence[isize];

    int index = 0;
    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] != null)
      {
        seq[i] = hiddenSequences[i];
      }
      else
      {
        seq[i] = alignment.getSequenceAt(index);
        index++;
      }
    }

    return new Alignment(seq);
  }

  public boolean isHidden(SequenceI seq)
  {
    for (int i = 0; i < hiddenSequences.length; i++)
    {
      if (hiddenSequences[i] != null && hiddenSequences[i] == seq)
      {
        return true;
      }
    }

    return false;
  }
}
