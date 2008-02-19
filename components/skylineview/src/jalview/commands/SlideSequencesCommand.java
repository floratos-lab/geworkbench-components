
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

package jalview.commands;

import jalview.datamodel.*;

public class SlideSequencesCommand extends EditCommand
{
  boolean gapsInsertedBegin = false;

  public SlideSequencesCommand(String description,
                               SequenceI[] seqsLeft,
                               SequenceI[] seqsRight,
                               int slideSize,
                               char gapChar)
  {
    this.description = description;

    int lSize = seqsLeft.length;
    gapsInsertedBegin = false;
    int i, j;
    for (i = 0; i < lSize; i++)
    {
      for (j = 0; j < slideSize; j++)
        if (!jalview.util.Comparison.isGap(seqsLeft[i].getCharAt(j)))
        {
          gapsInsertedBegin = true;
          break;
        }
    }

    if (!gapsInsertedBegin)
      edits = new Edit[]
          {  new Edit(DELETE_GAP, seqsLeft, 0, slideSize, gapChar)};
    else
      edits = new Edit[]
          {  new Edit(INSERT_GAP, seqsRight, 0, slideSize, gapChar)};

    performEdit(0,null);
  }

  public boolean getGapsInsertedBegin()
  {
    return gapsInsertedBegin;
  }

  public boolean appendSlideCommand(SlideSequencesCommand command)
  {
    boolean same = false;

    if(command.edits[0].seqs.length==edits[0].seqs.length)
    {
      same = true;
      for (int i = 0; i < command.edits[0].seqs.length; i++)
      {
        if (edits[0].seqs[i] != command.edits[0].seqs[i])
        {
          same = false;
        }
      }
    }

    if(same)
    {
      Edit[] temp = new Edit[command.edits.length + 1];
      System.arraycopy(command.edits, 0, temp, 0, command.edits.length);
      command.edits = temp;
      command.edits[command.edits.length - 1] = edits[0];
    }

    return same;
  }
}


