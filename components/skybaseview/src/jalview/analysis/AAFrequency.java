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
package jalview.analysis;

import java.util.*;

import jalview.datamodel.*;

/**
 * Takes in a vector or array of sequences and column start and column end
 * and returns a new Hashtable[] of size maxSeqLength, if Hashtable not supplied.
 * This class is used extensively in calculating alignment colourschemes
 * that depend on the amount of conservation in each alignment column.
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AAFrequency
{
  //No need to store 1000s of strings which are not
  //visible to the user.
  public static final String MAXCOUNT = "C";
  public static final String MAXRESIDUE = "R";
  public static final String PID_GAPS = "G";
  public static final String PID_NOGAPS = "N";

  public static final Hashtable[] calculate(Vector sequences, int start,
                                            int end)
  {
    SequenceI[] seqs = new SequenceI[sequences.size()];
    int width = 0;
    for (int i = 0; i < sequences.size(); i++)
    {
      seqs[i] = (SequenceI) sequences.elementAt(i);
      if (seqs[i].getLength() > width)
      {
        width = seqs[i].getLength();
      }
    }

    Hashtable[] reply = new Hashtable[width];

    if (end >= width)
    {
      end = width;
    }

    calculate(seqs, start, end, reply);

    return reply;
  }

  public static final void calculate(SequenceI[] sequences,
                                     int start, int end,
                                     Hashtable[] result)
  {
    Hashtable residueHash;
    int maxCount, nongap, i, j, v, jSize = sequences.length;
    String maxResidue;
    char c;
    float percentage;

    int[] values = new int[255];

    char[] seq;

    for (i = start; i < end; i++)
    {
      residueHash = new Hashtable();
      maxCount = 0;
      maxResidue = "";
      nongap = 0;
      values = new int[255];

      for (j = 0; j < jSize; j++)
      {
        seq = sequences[j].getSequence();
        if (seq.length > i)
        {
          c = seq[i];

          if (c == '.' || c == ' ')
          {
            c = '-';
          }

          if (c == '-')
          {
            values['-']++;
            continue;
          }
          else if ('a' <= c && c <= 'z')
          {
            c -= 32; //('a' - 'A');
          }

          nongap++;
          values[c]++;

        }
        else
        {
          values['-']++;
        }
      }

      for (v = 'A'; v < 'Z'; v++)
      {
        if (values[v] < 2 || values[v] < maxCount)
        {
          continue;
        }

        if (values[v] > maxCount)
        {
          maxResidue = String.valueOf( (char) v);
        }
        else if (values[v] == maxCount)
        {
          maxResidue += String.valueOf( (char) v);
        }
        maxCount = values[v];
      }

      if (maxResidue.length() == 0)
      {
        maxResidue = "-";
      }

      residueHash.put(MAXCOUNT, new Integer(maxCount));
      residueHash.put(MAXRESIDUE, maxResidue);

      percentage = ( (float) maxCount * 100) / (float) jSize;
      residueHash.put(PID_GAPS, new Float(percentage));

      percentage = ( (float) maxCount * 100) / (float) nongap;
      residueHash.put(PID_NOGAPS, new Float(percentage));
      result[i] = residueHash;
    }
  }
}
