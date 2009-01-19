package jalview.commands;

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

import jalview.datamodel.*;

public class RemoveGapsCommand
    extends EditCommand
{
  public RemoveGapsCommand(String description,
                           SequenceI[] seqs, AlignmentI al)
  {
    this.description = description;
    int width = 0;
    for (int i = 0; i < seqs.length; i++)
    {
      if (seqs[i].getLength() > width)
      {
        width = seqs[i].getLength();
      }
    }

    findGaps(seqs, 0, width, al);
  }

  public RemoveGapsCommand(String description,
                           SequenceI[] seqs,
                           int start, int end, AlignmentI al)
  {
    this.description = description;
    findGaps(seqs, start, end, al);
  }

  void findGaps(SequenceI[] seqs, int start, int end, AlignmentI al)
  {

    int startCol = -1, endCol = -1;
    int deletedCols = 0;

    int j, jSize;

    edits = new Edit[0];

    boolean delete = true;
    char[] sequence;

    for (int s = 0; s < seqs.length; s++)
    {
      deletedCols = 0;
      startCol = -1;
      endCol = -1;
      sequence = seqs[s].getSequence(start, end + 1);

      jSize = sequence.length;
      for (j = 0; j < jSize; j++)
      {
        delete = true;

        if (!jalview.util.Comparison.isGap(sequence[j]))
        {
          if (delete)
          {
            endCol = j;
          }

          delete = false;
        }

        if (delete && startCol == -1)
        {
          startCol = j;
        }

        if (!delete && startCol > -1)
        {
          this.appendEdit(DELETE_GAP, new SequenceI[]
                          {seqs[s]},
                          start + startCol - deletedCols,
                          endCol - startCol,
                          al,
                          false,null);

          deletedCols += (endCol - startCol);
          startCol = -1;
          endCol = -1;
        }
      }
      if (delete && startCol > -1)
      {
        this.appendEdit(DELETE_GAP, new SequenceI[]
                        {seqs[s]},
                        start + startCol - deletedCols,
                        jSize - startCol,
                        al,
                        false,null);
      }

    }

    performEdit(0,null);
  }

}
