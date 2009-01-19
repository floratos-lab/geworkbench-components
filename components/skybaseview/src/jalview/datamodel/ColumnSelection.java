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

import jalview.util.*;

/**
 * NOTE: Columns are zero based.
 */
public class ColumnSelection
{
  Vector selected = new Vector();

  //Vector of int [] {startCol, endCol}
  Vector hiddenColumns;

  /**
   * Add a column to the selection
   *
   * @param col index of column
   */
  public void addElement(int col)
  {
    Integer column = new Integer(col);
    if (!selected.contains(column))
    {
      selected.addElement(column);
    }
  }

  /**
   * clears column selection
   */
  public void clear()
  {
    selected.removeAllElements();
  }

  /**
   * removes col from selection
   *
   * @param col index of column to be removed
   */
  public void removeElement(int col)
  {
    Integer colInt = new Integer(col);

    if (selected.contains(colInt))
    {
      selected.removeElement(colInt);
    }
  }

  /**
   * removes a range of columns from the selection
   * @param start int - first column in range to be removed
   * @param end int - last col
   */
  public void removeElements(int start, int end)
  {
    Integer colInt;
    for (int i = start; i < end; i++)
    {
      colInt = new Integer(i);
      if (selected.contains(colInt))
      {
        selected.removeElement(colInt);
      }
    }
  }

  /**
   *
   * @return Vector containing selected columns as Integers
   */
  public Vector getSelected()
  {
    return selected;
  }

  /**
   *
   * @param col index to search for in column selection
   *
   * @return true if Integer(col) is in selection.
   */
  public boolean contains(int col)
  {
    return selected.contains(new Integer(col));
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int columnAt(int i)
  {
    return ( (Integer) selected.elementAt(i)).intValue();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int size()
  {
    return selected.size();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getMax()
  {
    int max = -1;

    for (int i = 0; i < selected.size(); i++)
    {
      if (columnAt(i) > max)
      {
        max = columnAt(i);
      }
    }

    return max;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getMin()
  {
    int min = 1000000000;

    for (int i = 0; i < selected.size(); i++)
    {
      if (columnAt(i) < min)
      {
        min = columnAt(i);
      }
    }

    return min;
  }

  /**
   * propagate shift in alignment columns to column selection
   *
   * @param start beginning of edit
   * @param left shift in edit (+ve for removal, or -ve for inserts)
   */
  public Vector compensateForEdit(int start, int change)
  {
    Vector deletedHiddenColumns = null;
    for (int i = 0; i < size(); i++)
    {
      int temp = columnAt(i);

      if (temp >= start)
      {
        selected.setElementAt(new Integer(temp - change), i);
      }
    }

    if (hiddenColumns != null)
    {
      deletedHiddenColumns = new Vector();
      int hSize = hiddenColumns.size();
      for (int i = 0; i < hSize; i++)
      {
        int[] region = (int[]) hiddenColumns.elementAt(i);
        if (region[0] > start && start + change > region[1])
        {
          deletedHiddenColumns.addElement(
              hiddenColumns.elementAt(i));

          hiddenColumns.removeElementAt(i);
          i--;
          hSize--;
          continue;
        }

        if (region[0] > start)
        {
          region[0] -= change;
          region[1] -= change;
        }

        if (region[0] < 0)
        {
          region[0] = 0;
        }

      }

      this.revealHiddenColumns(0);
    }

    return deletedHiddenColumns;
  }

  /**
   * propagate shift in alignment columns to column selection
   * special version of compensateForEdit - allowing for edits within hidden regions
   * @param start beginning of edit
   * @param left shift in edit (+ve for removal, or -ve for inserts)
   */
  private void compensateForDelEdits(int start, int change)
  {
    for (int i = 0; i < size(); i++)
    {
      int temp = columnAt(i);

      if (temp >= start)
      {
        selected.setElementAt(new Integer(temp - change), i);
      }
    }

    if (hiddenColumns != null)
    {
      for (int i = 0; i < hiddenColumns.size(); i++)
      {
        int[] region = (int[]) hiddenColumns.elementAt(i);
        if (region[0] >= start)
        {
          region[0] -= change;
        }
        if (region[1] >= start)
        {
          region[1] -= change;
        }
        if (region[1] < region[0])
        {
          hiddenColumns.removeElementAt(i--);
        }

        if (region[0] < 0)
        {
          region[0] = 0;
        }
        if (region[1] < 0)
        {
          region[1] = 0;
        }
      }
    }
  }

  /**
   * Adjust hidden column boundaries based on a series of column
   * additions or deletions in visible regions.
   * @param shiftrecord
   * @return
   */
  public ShiftList compensateForEdits(ShiftList shiftrecord)
  {
    if (shiftrecord != null)
    {
      Vector shifts = shiftrecord.shifts;
      if (shifts != null && shifts.size() > 0)
      {
        int shifted = 0;
        for (int i = 0, j = shifts.size(); i < j; i++)
        {
          int[] sh = (int[]) shifts.elementAt(i);
          //compensateForEdit(shifted+sh[0], sh[1]);
          compensateForDelEdits(shifted + sh[0], sh[1]);
          shifted -= sh[1];
        }
      }
      return shiftrecord.getInverse();
    }
    return null;
  }

  /**
   * removes intersection of position,length ranges in deletions
   * from the start,end regions marked in intervals.
   * @param deletions
   * @param intervals
   * @return
   */
  private boolean pruneIntervalVector(Vector deletions, Vector intervals)
  {
    boolean pruned = false;
    int i = 0, j = intervals.size() - 1, s = 0, t = deletions.size() - 1;
    int hr[] = (int[]) intervals.elementAt(i);
    int sr[] = (int[]) deletions.elementAt(s);
    while (i <= j && s <= t)
    {
      boolean trailinghn = hr[1] >= sr[0];
      if (!trailinghn)
      {
        if (i < j)
        {
          hr = (int[]) intervals.elementAt(++i);
        }
        else
        {
          i++;
        }
        continue;
      }
      int endshift = sr[0] + sr[1]; // deletion ranges - -ve means an insert
      if (endshift < hr[0] || endshift < sr[0])
      { // leadinghc disjoint or not a deletion
        if (s < t)
        {
          sr = (int[]) deletions.elementAt(++s);
        }
        else
        {
          s++;
        }
        continue;
      }
      boolean leadinghn = hr[0] >= sr[0];
      boolean leadinghc = hr[0] < endshift;
      boolean trailinghc = hr[1] < endshift;
      if (leadinghn)
      {
        if (trailinghc)
        { // deleted hidden region.
          intervals.removeElementAt(i);
          pruned = true;
          j--;
          if (i <= j)
          {
            hr = (int[]) intervals.elementAt(i);
          }
          continue;
        }
        if (leadinghc)
        {
          hr[0] = endshift; // clip c terminal region
          leadinghn = !leadinghn;
          pruned = true;
        }
      }
      if (!leadinghn)
      {
        if (trailinghc)
        {
          if (trailinghn)
          {
            hr[1] = sr[0] - 1;
            pruned = true;
          }
        }
        else
        {
          // sr contained in hr
          if (s < t)
          {
            sr = (int[]) deletions.elementAt(++s);
          }
          else
          {
            s++;
          }
          continue;
        }
      }
    }
    return pruned; // true if any interval was removed or modified by operations.
  }

  private boolean pruneColumnList(Vector deletion, Vector list)
  {
    int s = 0, t = deletion.size();
    int[] sr = (int[]) list.elementAt(s++);
    boolean pruned = false;
    int i = 0, j = list.size();
    while (i < j && s <= t)
    {
      int c = ( (Integer) list.elementAt(i++)).intValue();
      if (sr[0] <= c)
      {
        if (sr[1] + sr[0] >= c)
        { // sr[1] -ve means inseriton.
          list.removeElementAt(--i);
          j--;
        }
        else
        {
          if (s < t)
          {
            sr = (int[]) deletion.elementAt(s);
          }
          s++;
        }
      }
    }
    return pruned;
  }

  /**
   * remove any hiddenColumns or selected columns and shift remaining
   * based on a series of position, range deletions.
   * @param deletions
   */
  public void pruneDeletions(ShiftList deletions)
  {
    if (deletions != null)
    {
      Vector shifts = deletions.shifts;
      if (shifts != null && shifts.size() > 0)
      {
        // delete any intervals intersecting.
        if (hiddenColumns != null)
        {
          pruneIntervalVector(shifts, hiddenColumns);
          if (hiddenColumns != null && hiddenColumns.size() == 0)
          {
            hiddenColumns = null;
          }
        }
        if (selected != null && selected.size() > 0)
        {
          pruneColumnList(shifts, selected);
          if (selected != null && selected.size() == 0)
          {
            selected = null;
          }
        }
        // and shift the rest.
        this.compensateForEdits(deletions);
      }
    }
  }

  /**
   * This Method is used to return all the HiddenColumn regions
   * less than the given index.
   * @param end int
   * @return Vector
   */
  public Vector getHiddenColumns()
  {
    return hiddenColumns;
  }

  /**
   * Return absolute column index for a visible column index
   * @param column int column index in alignment view
   * @return alignment column index for column
   */
  public int adjustForHiddenColumns(int column)
  {
    int result = column;
    if (hiddenColumns != null)
    {
      for (int i = 0; i < hiddenColumns.size(); i++)
      {
        int[] region = (int[]) hiddenColumns.elementAt(i);
        if (result >= region[0])
        {
          result += region[1] - region[0] + 1;
        }
      }
    }
    return result;
  }

  /**
   * Use this method to find out where a visible column is in the alignment
   * when hidden columns exist
   * @param hiddenColumn int
   * @return int
   */
  public int findColumnPosition(int hiddenColumn)
  {
    int result = hiddenColumn;
    if (hiddenColumns != null)
    {
      int index = 0;
      int gaps = 0;
      do
      {
        int[] region = (int[]) hiddenColumns.elementAt(index);
        if (hiddenColumn > region[1])
        {
          result -= region[1] + 1 - region[0];
        }
        index++;
      }
      while (index < hiddenColumns.size());

      result -= gaps;
    }

    return result;
  }

  /**
   * Use this method to determine where the next hiddenRegion starts
   */
  public int findHiddenRegionPosition(int hiddenRegion)
  {
    int result = 0;
    if (hiddenColumns != null)
    {
      int index = 0;
      int gaps = 0;
      do
      {
        int[] region = (int[]) hiddenColumns.elementAt(index);
        if (hiddenRegion == 0)
        {
          return region[0];
        }

        gaps += region[1] + 1 - region[0];
        result = region[1] + 1;
        index++;
      }
      while (index < hiddenRegion + 1);

      result -= gaps;
    }

    return result;
  }

  /**
   * THis method returns the rightmost limit of a
   * region of an alignment with hidden columns.
   * In otherwords, the next hidden column.
   * @param index int
   */
  public int getHiddenBoundaryRight(int alPos)
  {
    if (hiddenColumns != null)
    {
      int index = 0;
      do
      {
        int[] region = (int[]) hiddenColumns.elementAt(index);
        if (alPos < region[0])
        {
          return region[0];
        }

        index++;
      }
      while (index < hiddenColumns.size());
    }

    return alPos;

  }

  /**
   * This method returns the leftmost limit of a
   * region of an alignment with hidden columns.
   * In otherwords, the previous hidden column.
   * @param index int
   */
  public int getHiddenBoundaryLeft(int alPos)
  {
    if (hiddenColumns != null)
    {
      int index = hiddenColumns.size() - 1;
      do
      {
        int[] region = (int[]) hiddenColumns.elementAt(index);
        if (alPos > region[1])
        {
          return region[1];
        }

        index--;
      }
      while (index > -1);
    }

    return alPos;

  }

  public void hideSelectedColumns()
  {
    while (size() > 0)
    {
      int column = ( (Integer) getSelected().firstElement()).intValue();
      hideColumns(column);
    }

  }

  public void hideColumns(int start, int end)
  {
    if (hiddenColumns == null)
    {
      hiddenColumns = new Vector();
    }

    boolean added = false;
    boolean overlap = false;

    for (int i = 0; i < hiddenColumns.size(); i++)
    {
      int[] region = (int[]) hiddenColumns.elementAt(i);
      if (start <= region[1] && end >= region[0])
      {
        hiddenColumns.removeElementAt(i);
        overlap = true;
        break;
      }
      else if (end < region[0] && start < region[0])
      {
        hiddenColumns.insertElementAt(new int[]
                                      {start, end}, i);
        added = true;
        break;
      }
    }

    if (overlap)
    {
      hideColumns(start, end);
    }
    else if (!added)
    {
      hiddenColumns.addElement(new int[]
                               {start, end});
    }

  }

  /**
   * This method will find a range of selected columns
   * around the column specified
   * @param res int
   */
  public void hideColumns(int col)
  {
    // First find out range of columns to hide
    int min = col, max = col + 1;
    while (contains(min))
    {
      removeElement(min);
      min--;
    }

    while (contains(max))
    {
      removeElement(max);
      max++;
    }

    min++;
    max--;
    if (min > max)
    {
      min = max;
    }

    hideColumns(min, max);
  }

  public void revealAllHiddenColumns()
  {
    if (hiddenColumns != null)
    {
      for (int i = 0; i < hiddenColumns.size(); i++)
      {
        int[] region = (int[]) hiddenColumns.elementAt(i);
        for (int j = region[0]; j < region[1] + 1; j++)
        {
          addElement(j);
        }
      }
    }

    hiddenColumns = null;
  }

  public void revealHiddenColumns(int res)
  {
    for (int i = 0; i < hiddenColumns.size(); i++)
    {
      int[] region = (int[]) hiddenColumns.elementAt(i);
      if (res == region[0])
      {
        for (int j = region[0]; j < region[1] + 1; j++)
        {
          addElement(j);
        }

        hiddenColumns.removeElement(region);
        break;
      }
    }
    if (hiddenColumns.size() == 0)
    {
      hiddenColumns = null;
    }
  }

  public boolean isVisible(int column)
  {
    if (hiddenColumns != null)
      for (int i = 0; i < hiddenColumns.size(); i++)
      {
        int[] region = (int[]) hiddenColumns.elementAt(i);
        if (column >= region[0] && column <= region[1])
        {
          return false;
        }
      }

    return true;
  }

  /**
   * Copy constructor
   * @param copy
   */
  public ColumnSelection(ColumnSelection copy)
  {
    if (copy != null)
    {
      if (copy.selected != null)
      {
        selected = new Vector();
        for (int i = 0, j = copy.selected.size(); i < j; i++)
        {
          selected.addElement(copy.selected.elementAt(i));
        }
      }
      if (copy.hiddenColumns != null)
      {
        hiddenColumns = new Vector(copy.hiddenColumns.size());
        for (int i = 0, j = copy.hiddenColumns.size(); i < j; i++)
        {
          int[] rh, cp;
          rh = (int[]) copy.hiddenColumns.elementAt(i);
          if (rh != null)
          {
            cp = new int[rh.length];
            System.arraycopy(rh, 0, cp, 0, rh.length);
            hiddenColumns.addElement(cp);
          }
        }
      }
    }
  }

  /**
   * ColumnSelection
   */
  public ColumnSelection()
  {
  }

  public String[] getVisibleSequenceStrings(int start, int end,
                                            SequenceI[] seqs)
  {
    int i, iSize = seqs.length;
    String selection[] = new String[iSize];
    if (hiddenColumns != null && hiddenColumns.size() > 0)
    {
      for (i = 0; i < iSize; i++)
      {
        StringBuffer visibleSeq = new StringBuffer();
        Vector regions = getHiddenColumns();

        int blockStart = start, blockEnd = end;
        int[] region;
        int hideStart, hideEnd;

        for (int j = 0; j < regions.size(); j++)
        {
          region = (int[]) regions.elementAt(j);
          hideStart = region[0];
          hideEnd = region[1];

          if (hideStart < start)
          {
            continue;
          }

          blockStart = Math.min(blockStart, hideEnd + 1);
          blockEnd = Math.min(blockEnd, hideStart);

          if (blockStart > blockEnd)
          {
            break;
          }

          visibleSeq.append(seqs[i].getSequence(blockStart, blockEnd));

          blockStart = hideEnd + 1;
          blockEnd = end;
        }

        if (end > blockStart)
        {
          visibleSeq.append(seqs[i].getSequence(blockStart, end));
        }

        selection[i] = visibleSeq.toString();
      }
    }
    else
    {
      for (i = 0; i < iSize; i++)
      {
        selection[i] = seqs[i].getSequenceAsString(start, end);
      }
    }

    return selection;
  }

  /**
   * return all visible segments between the given start and end boundaries
   *
   * @param start (first column inclusive from 0)
   * @param end (last column - not inclusive)
   * @return int[] {i_start, i_end, ..} where intervals lie in start<=i_start<=i_end<end
   */
  public int[] getVisibleContigs(int start, int end)
  {
    if (hiddenColumns != null && hiddenColumns.size() > 0)
    {
      Vector visiblecontigs = new Vector();
      Vector regions = getHiddenColumns();

      int vstart = start;
      int[] region;
      int hideStart, hideEnd;

      for (int j = 0; vstart < end && j < regions.size(); j++)
      {
        region = (int[]) regions.elementAt(j);
        hideStart = region[0];
        hideEnd = region[1];

        if (hideEnd < vstart)
        {
          continue;
        }
        if (hideStart > vstart)
        {
          visiblecontigs.addElement(new int[]
                                    {vstart, hideStart - 1});
        }
        vstart = hideEnd + 1;
      }

      if (vstart < end)
      {
        visiblecontigs.addElement(new int[]
                                  {vstart, end - 1});
      }
      int[] vcontigs = new int[visiblecontigs.size() * 2];
      for (int i = 0, j = visiblecontigs.size(); i < j; i++)
      {
        int[] vc = (int[]) visiblecontigs.elementAt(i);
        visiblecontigs.setElementAt(null, i);
        vcontigs[i * 2] = vc[0];
        vcontigs[i * 2 + 1] = vc[1];
      }
      visiblecontigs.removeAllElements();
      return vcontigs;
    }
    else
    {
      return new int[]
          {
          start, end - 1};
    }
  }
}
