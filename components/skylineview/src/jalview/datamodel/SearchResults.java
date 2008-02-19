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

public class SearchResults
{

  Match[] matches;

  /**
   * This method replaces the old search results which merely
   * held an alignment index of search matches. This broke
   * when sequences were moved around the alignment
   * @param seq Sequence
   * @param start int
   * @param end int
   */
  public void addResult(SequenceI seq, int start, int end)
  {
    if (matches == null)
    {
      matches = new Match[]
          {
          new Match(seq, start, end)};
      return;
    }

    int mSize = matches.length;

    Match[] tmp = new Match[mSize + 1];
    int m;
    for (m = 0; m < mSize; m++)
    {
      tmp[m] = matches[m];
    }

    tmp[m] = new Match(seq, start, end);

    matches = tmp;
  }

  /**
   * This Method returns the search matches which lie between the
   * start and end points of the sequence in question. It is
   * optimised for returning objects for drawing on SequenceCanvas
   */
  public int[] getResults(SequenceI sequence, int start, int end)
  {
    if (matches == null)
    {
      return null;
    }

    int[] result = null;
    int[] tmp = null;
    int resultLength;

    for (int m = 0; m < matches.length; m++)
    {
      if (matches[m].sequence == sequence)
      {
        int matchStart = matches[m].sequence.findIndex(matches[m].start) - 1;
        int matchEnd = matches[m].sequence.findIndex(matches[m].end) - 1;

        if (matchStart <= end && matchEnd >= start)
        {
          if (matchStart < start)
          {
            matchStart = start;
          }

          if (matchEnd > end)
          {
            matchEnd = end;
          }

          if (result == null)
          {
            result = new int[]
                {
                matchStart, matchEnd};
          }
          else
          {
            resultLength = result.length;
            tmp = new int[resultLength + 2];
            System.arraycopy(result, 0, tmp, 0, resultLength);
            result = tmp;
            result[resultLength] = matchStart;
            result[resultLength + 1] = matchEnd;
          }
        }
      }
    }
    return result;
  }

  public int getSize()
  {
    return matches == null ? 0 : matches.length;
  }

  public SequenceI getResultSequence(int index)
  {
    return matches[index].sequence;
  }

  public int getResultStart(int index)
  {
    return matches[index].start;
  }

  public int getResultEnd(int index)
  {
    return matches[index].end;
  }

  class Match
  {
    SequenceI sequence;
    int start;
    int end;

    public Match(SequenceI seq, int start, int end)
    {
      sequence = seq;
      this.start = start;
      this.end = end;
    }
  }
}
