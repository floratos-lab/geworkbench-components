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
package jalview.util;

import java.util.*;

/**
 * ShiftList
 * Simple way of mapping a linear series to a new linear range with new points introduced.
 * Use at your own risk!
 * Now growing to be used for interval ranges (position, offset) storing deletions/insertions
 */
public class ShiftList
{
  public Vector shifts;
  public ShiftList()
  {
    shifts = new Vector();
  }

  /**
   * addShift
   * @param pos start position for shift (in original reference frame)
   * @param shift length of shift
   */
  public void addShift(int pos, int shift)
  {
    int sidx = 0;
    int[] rshift = null;
    while (sidx < shifts.size() &&
           (rshift = (int[]) shifts.elementAt(sidx))[0] < pos)
    {
      sidx++;
    }
    if (sidx == shifts.size())
    {
      shifts.insertElementAt(new int[]
                             {pos, shift}, sidx);
    }
    else
    {
      rshift[1] += shift;
    }
  }

  /**
   * shift
   *
   * @param pos int
   * @return int shifted position
   */
  public int shift(int pos)
  {
    if (shifts.size() == 0)
    {
      return pos;
    }
    int shifted = pos;
    int sidx = 0;
    int rshift[];
    while (sidx < shifts.size()
           &&
           (rshift = ( (int[]) shifts.elementAt(sidx++)))[0] <= pos)
    {
      shifted += rshift[1];
    }
    return shifted;
  }

  /**
   * clear all shifts
   */
  public void clear()
  {
    shifts.removeAllElements();
  }

  /**
   * invert the shifts
   * @return ShiftList with inverse shift operations
   */
  public ShiftList getInverse()
  {
    ShiftList inverse = new ShiftList();
    if (shifts != null)
    {
      for (int i = 0, j = shifts.size(); i < j; i++)
      {
        int[] sh = (int[]) shifts.elementAt(i);
        if (sh != null)
        {
          inverse.shifts.addElement(new int[]
                                    {sh[0], -sh[1]});
        }
      }
    }
    return inverse;
  }

  /**
   * parse a 1d map of position 1<i<n to L<pos[i]<N
   * such as that returned from SequenceI.gapMap()
   * @param gapMap
   * @return shifts from map index to mapped position
   */
  public static ShiftList parseMap(int[] gapMap)
  {
    ShiftList shiftList = null;
    if (gapMap != null && gapMap.length > 0)
    {
      shiftList = new ShiftList();
      for (int i = 0, p = 0; i < gapMap.length; p++, i++)
      {
        if (p != gapMap[i])
        {
          shiftList.addShift(p, gapMap[i] - p);
          p = gapMap[i];
        }
      }
    }
    return shiftList;
  }
}
