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

public class ChangeCaseCommand
    implements CommandI
{
  String description;
  public static int TO_LOWER = 0;
  public static int TO_UPPER = 1;
  public static int TOGGLE_CASE = 2;
  int caseChange = -1;
  SequenceI[] seqs;
  int[][] regions;
  public ChangeCaseCommand(String description,
                           SequenceI[] seqs,
                           int[][] regions,
                           int caseChange)
  {
    this.description = description;
    this.seqs = seqs;
    this.regions = regions;
    this.caseChange = caseChange;
    doCommand(null);
  }

  public String getDescription()
  {
    return description;
  }

  public int getSize()
  {
    return 1;
  }

  public void doCommand(AlignmentI[] views)
  {
    changeCase(true);
  }

  public void undoCommand(AlignmentI[] views)
  {
    changeCase(false);
  }

  void changeCase(boolean doCommand)
  {
    String sequence;
    int start, end;
    char nextChar;
    for (int r = 0; r < regions.length; r++)
    {
      start = regions[r][0];
      for (int s = 0; s < seqs.length; s++)
      {
        sequence = seqs[s].getSequenceAsString();
        StringBuffer newSeq = new StringBuffer();

        if (regions[r][1] > sequence.length())
        {
          end = sequence.length();
        }
        else
        {
          end = regions[r][1];
        }

        if (start > 0)
        {
          newSeq.append(sequence.substring(0, start));
        }

        if ( (caseChange == TO_UPPER && doCommand)
            || (caseChange == TO_LOWER && !doCommand))
        {
          newSeq.append(sequence.substring(start, end).toUpperCase());
        }

        else if ( (caseChange == TO_LOWER && doCommand)
                 || (caseChange == TO_UPPER && !doCommand))
        {
          newSeq.append(sequence.substring(start, end).toLowerCase());
        }

        else //TOGGLE CASE
        {
          for (int c = start; c < end; c++)
          {
            nextChar = sequence.charAt(c);
            if ('a' <= nextChar && nextChar <= 'z')
            {
              // TO UPPERCASE !!!
              nextChar -= ('a' - 'A');
            }
            else if ('A' <= nextChar && nextChar <= 'Z')
            {
              // TO LOWERCASE !!!
              nextChar += ('a' - 'A');
            }
            newSeq.append(nextChar);
          }
        }

        if (end < sequence.length())
        {
          newSeq.append(sequence.substring(end));
        }

        seqs[s].setSequence(newSeq.toString());
      }
    }
  }

}
