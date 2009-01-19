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

import java.util.*;

import jalview.datamodel.*;
import jalview.util.*;

public class TrimRegionCommand
    extends EditCommand
{
  public static String TRIM_LEFT = "TrimLeft";
  public static String TRIM_RIGHT = "TrimRight";

  public ColumnSelection colSel = null;

  int[] start;

  ShiftList shiftList;

  SequenceGroup selectionGroup;

  Vector deletedHiddenColumns;

  int columnsDeleted;

  public TrimRegionCommand(String description,
                           String command,
                           SequenceI[] seqs,
                           int column,
                           AlignmentI al,
                           ColumnSelection colSel,
                           SequenceGroup selectedRegion)
  {
    this.description = description;
    this.selectionGroup = selectedRegion;
    this.colSel = colSel;
    if (command.equalsIgnoreCase(TRIM_LEFT))
    {
      if (column == 0)
      {
        return;
      }

      columnsDeleted = column;

      edits = new Edit[]
          {
          new Edit(CUT, seqs, 0, column, al)};
    }
    else if (command.equalsIgnoreCase(TRIM_RIGHT))
    {
      int width = al.getWidth() - column - 1;
      if (width < 2)
      {
        return;
      }

      columnsDeleted = width - 1;

      edits = new Edit[]
          {
          new Edit(CUT, seqs, column + 1, width, al)};
    }

    //We need to keep a record of the sequence start
    //in order to restore the state after a redo
    int i, isize = edits[0].seqs.length;
    start = new int[isize];
    for (i = 0; i < isize; i++)
    {
      start[i] = edits[0].seqs[i].getStart();
    }

    performEdit(0,null);
  }

  void cut(Edit command)
  {
    int column, j, jSize = command.seqs.length;
    for (j = 0; j < jSize; j++)
    {
      if (command.position == 0)
      {
        //This is a TRIM_LEFT command
        column = command.seqs[j].findPosition(command.number);
        command.seqs[j].setStart(column);
      }
      else
      {
        //This is a TRIM_RIGHT command
        column = command.seqs[j].findPosition(command.position) - 1;
        command.seqs[j].setEnd(column);
      }
    }

    super.cut(command, null);

    if (command.position == 0)
    {
      deletedHiddenColumns = colSel.compensateForEdit(0, command.number);
      if (selectionGroup != null)
      {
        selectionGroup.adjustForRemoveLeft(command.number);
      }
    }
    else
    {
      deletedHiddenColumns = colSel.compensateForEdit(command.position,
          command.number);
      if (selectionGroup != null)
      {
        selectionGroup.adjustForRemoveRight(command.position);
      }
    }
  }

  void paste(Edit command)
  {
    super.paste(command, null);
    int column, j, jSize = command.seqs.length;
    for (j = 0; j < jSize; j++)
    {
      if (command.position == 0)
      {
        command.seqs[j].setStart(start[j]);
      }
      else
      {
        column = command.seqs[j]
            .findPosition(command.number + command.position) - 1;
        command.seqs[j].setEnd(column);
      }
    }

    if (command.position == 0)
    {
      colSel.compensateForEdit(0, -command.number);
      if (selectionGroup != null)
      {
        selectionGroup.adjustForRemoveLeft( -command.number);
      }
    }

    if (deletedHiddenColumns != null)
    {
      int[] region;
      for (int i = 0; i < deletedHiddenColumns.size(); i++)
      {
        region = (int[]) deletedHiddenColumns.elementAt(i);
        colSel.hideColumns(region[0], region[1]);
      }
    }
  }

  public int getSize()
  {
    return columnsDeleted;
  }

}
