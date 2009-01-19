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

public abstract class CigarBase
{
  /**
   * Base class for compact idiosyncratic representation of gaps and aligned residues
   * Regards to Tom Oldfield for his DynamicArray class.
   * 17th July 2006
   * Not thread safe.
   */
  public CigarBase()
  {
    // nothing to be done (probably)
  }

  protected int length = 0;
  protected int _inc_length = 10; // extension range for addition of new operations
  protected char[] operation = null;
  protected int[] range = null;
  /**
   * Range of Hidden residues in seq (translated as deleted in seq)
   */
  public static final char D = 'D'; /**
  * Range of insertions to seq
  */
 public static final char I = 'I'; /**
  * Range of aligned residues
  */
 public static final char M = 'M';
  static protected final char _case_shift = 'a' - 'A';
  /**
   * Ugly function to get edited sequence string, start and end symbol positions and the deletion regions as an array of int pairs
   * May return null for an empty cigar string.
   * May return null for deletion ranges if there are none.
   * @param reference - the symbol sequence to apply the cigar operations to (or null if no sequence)
   * @param GapChar - the symbol to use for Insert operations
   * @return Object[] { String, int[] {start, startcol, end, endcol}, int[][3] {start, end, col} or null} the gapped sequence, first and last residue index, and the deletion ranges on the reference sequence
   */
  public Object[] getSequenceAndDeletions(String reference, char GapChar)
  {
    int rlength = 0;
    int[][] deletions = new int[length][];
    int[][] trunc_deletions = null;
    StringBuffer sq = new StringBuffer();
    int cursor = 0, alcursor = 0, start = 0, startpos = 0, end = 0, endpos = 0,
        delcount = -1;
    boolean consecutive_del = false;
    if (length == 0)
    {
      return null;
    }
    if (reference != null)
    {
      rlength = reference.length();
    }
    boolean modstart = true;
    for (int i = 0; i < length; i++)
    {
      switch (operation[i])
      {
        case D:
          if (!consecutive_del)
          {
            deletions[++delcount] = new int[]
                {
                cursor, 0, alcursor};
          }
          cursor += range[i];
          deletions[delcount][1] = cursor - 1;
          consecutive_del = true;
          break;
        case I:
          consecutive_del = false;
          for (int r = 0; r < range[i]; r++)
          {
            sq.append(GapChar);
            alcursor++;
          }
          break;
        case M:
          consecutive_del = false;
          if (modstart)
          {
            start = cursor;
            startpos = alcursor;
            modstart = false;
          }
          if (reference != null)
          {
            int sbend = cursor + range[i];
            if (sbend > rlength)
            {
              sq.append(reference.substring(cursor, rlength));
              while (sbend-- >= rlength)
              {
                sq.append(GapChar);
              }
            }
            else
            {
              sq.append(reference.substring(cursor, sbend));
            }
          }
          alcursor += range[i];
          cursor += range[i];
          end = cursor - 1;
          endpos = alcursor;
          break;
        default:
          throw new Error("Unknown SeqCigar operation '" + operation[i] + "'");
      }
    }
    if (++delcount > 0)
    {
      trunc_deletions = new int[delcount][];
      System.arraycopy(deletions, 0, trunc_deletions, 0, delcount);
    }
    deletions = null;
    return new Object[]
        {
        ( (reference != null) ? sq.toString() : null),
        new int[]
        {
        start, startpos, end, endpos}, trunc_deletions};
  }

  protected void compact_operations()
  {
    int i = 1;
    if (operation == null)
    {
      return;
    }
    char last = operation[0];
    while (i < length)
    {
      if (last == operation[i])
      {
        range[i - 1] += range[i];
        int r = length - i;
        if (r > 0)
        {
          System.arraycopy(range, i + 1, range, i, r);
          System.arraycopy(operation, i + 1, operation, i, r);
        }
        length--;
      }
      else
      {
        last = operation[i++];
      }
    }
  }

  /**
   * turn a cigar string into a series of operation range pairs
   * @param cigarString String
   * @return object[] {char[] operation, int[] range}
   * @throws java.lang.Exception for improperly formated cigar strings or ones with unknown operations
   */
  public static Object[] parseCigarString(String cigarString)
      throws Exception
  {
    int ops = 0;
    for (int i = 0, l = cigarString.length(); i < l; i++)
    {
      char c = cigarString.charAt(i);
      if (c == M || c == (M - _case_shift) || c == I || c == (I - _case_shift) ||
          c == D || c == (D - _case_shift))
      {
        ops++;
      }
    }
    char[] operation = new char[ops];
    int[] range = new int[ops];
    int op = 0;
    int i = 0, l = cigarString.length();
    while (i < l)
    {
      char c;
      int j = i;
      do
      {
        c = cigarString.charAt(j++);
      }
      while (c >= '0' && c <= '9' && j < l);
      if (j >= l && c >= '0' && c <= '9')
      {
        throw new Exception("Unterminated cigar string.");
      }
      try
      {
        String rangeint = cigarString.substring(i, j - 1);
        range[op] = Integer.parseInt(rangeint);
        i = j;
      }
      catch (Exception e)
      {
        throw new Error("Implementation bug in parseCigarString");
      }
      if (c >= 'a' && c <= 'z')
      {
        c -= _case_shift;
      }
      if ( (c == M || c == I || c == D))
      {
        operation[op++] = c;
      }
      else
      {
        throw new Exception("Unexpected operation '" + c +
                            "' in cigar string (position " + i + " in '" +
                            cigarString + "'");
      }
    }
    return new Object[]
        {
        operation, range};
  }

  /**
   * add an operation to cigar string
   * @param op char
   * @param range int
   */
  public void addOperation(char op, int range)
  {
    if (op >= 'a' && op <= 'z')
    {
      op -= _case_shift;
    }
    if (op != M && op != D && op != I)
    {
      throw new Error("Implementation error. Invalid operation string.");
    }
    if (range <= 0)
    {
      throw new Error("Invalid range string (must be non-zero positive number)");
    }
    int lngth = 0;
    if (operation == null)
    {
      this.operation = new char[_inc_length];
      this.range = new int[_inc_length];
    }
    if (length + 1 == operation.length)
    {
      char[] ops = this.operation;
      this.operation = new char[length + 1 + _inc_length];
      System.arraycopy(ops, 0, this.operation, 0, length);
      ops = null;
      int[] rng = this.range;
      this.range = new int[length + 1 + _inc_length];
      System.arraycopy(rng, 0, this.range, 0, length);
      rng = null;
    }
    if ( (length > 0) && (operation[length - 1] == op))
    {
      length--; // modify existing operation.
    }
    else
    {
      this.range[length] = 0; // reset range
    }
    this.operation[length] = op;
    this.range[length++] += range;
  }

  /**
   * semi-efficient insert an operation on the current cigar string set at column pos (from 1)
   * NOTE: Insertion operations simply extend width of cigar result - affecting registration of alignment
   * Deletion ops will shorten length of result - and affect registration of alignment
   * Match ops will also affect length of result - affecting registration of alignment
   * (ie "10M".insert(4,I,3)->"4M3I3M") - (replace?)
   * (ie "10M".insert(4,D,3)->"4M3D3M") - (shortens alignment)
   * (ie "5I5M".insert(4,I,3)->"8I5M") - real insertion
   * (ie "5I5M".insert(4,D,3)->"4I2D3M") - shortens aligment - I's are removed, Ms changed to Ds
   * (ie "10M".insert(4,M,3)->"13M")  - lengthens - Is changed to M, Ds changed to M.
   * (ie "5I5M".insert(4,M,3)->"4I8M") - effectively shifts sequence left by 1 residue and extends it by 3
   * ( "10D5M".insert(-1,M,3)->"3M7D5M")
   * ( "10D5M".insert(0,M,3)->"7D8M")
   * ( "10D5M".insert(1,M,3)->"10D8M")
   *
   * ( "1M10D5M".insert(0,M,3)->"1M10D8M")
   * ( "1M10D5M".insert(1,M,3)->"
   *
   * if pos is beyond width - I operations are added before the operation
   * @param pos int -1, 0-length of visible region, or greater to append new ops (with insertions in between)
   * @param op char
   * @param range int
     public void addOperationAt(int pos, char op, int range)
     {
   int cursor = -1; // mark the position for the current operation being edited.
    int o = 0;
    boolean last_d = false; // previous op was a deletion.
    if (pos < -1)
      throw new Error("pos<-1 is not supported.");
    while (o<length) {
      if (operation[o] != D)
      {
        if ( (cursor + this.range[o]) < pos)
        {
          cursor += this.range[o];
          o++;
          last_d=false;
        }
        else
        {
          break;
        }
      }
      else {
        last_d=true;
        o++;
      }
    }
    if (o==length) {
      // must insert more operations before pos
      if (pos-cursor>0)
        addInsertion(pos-cursor);
      // then just add the new operation. Regardless of what it is.
      addOperation(op, range);
    } else {
      int diff = pos - cursor;

      int e_length = length-o; // new edit operation array length.
      // diff<0 - can only happen before first insertion or match. - affects op and all following
      // dif==0 - only when at first position of existing op -
      // diff>0 - must preserve some existing operations
      int[] e_range = new int[e_length];
      System.arraycopy(this.range, o, e_range, 0, e_length);
      char[] e_op = new char[e_length];
      System.arraycopy(this.operation, o, e_op, 0, e_length);
      length = o; // can now use add_operation to extend list.
      int e_o=0; // current operation being edited.
      switch (op) {
        case M:
          switch (e_op[e_o])
          {
            case M:
              if (last_d && diff <= 0)
              {
                // reduce D's, if possible
                if (range<=this.range[o-1]) {
                  this.range[o - 1] -= range;
                } else {
                  this.range[o-1]=0;
                }
                if (this.range[o-1]==0)
                  o--; // lose this op.
              }
              e_range[e_o] += range; // just add more matched residues
              break;
            case I:
              // change from insertion to match
              if (last_d && diff<=0)
              {
                // reduce D's, if possible
                if (range<=this.range[o-1]) {
                  this.range[o - 1] -= range;
                } else {
                  this.range[o-1]=0;
                }
                if (this.range[o-1]==0)
                  o--; // lose this op.
              }
              e_range[e_o]
                    break;
                default:
                  throw new Inp
                      }

                      break;
                case I:
                  break;
                case D:
              }
          break;
        default:
   throw new Error("Implementation Error: Unknown operation in addOperation!");
      }
      // finally, add remaining ops.
      while (e_o<e_length) {
        addOperation(e_op[e_o], e_range[e_o]);
        e_o++;
      }
    }
     }
   **/
  /**
   * Mark residues from start to end (inclusive) as deleted from the alignment, and removes any insertions.
   * @param start int
   * @param end int
   * @return deleted int - number of symbols marked as deleted
   */
  public int deleteRange(int start, int end)
  {
    int deleted = 0;
    if (length == 0)
    {
      // nothing to do here
      return deleted;
    }
    if (start < 0 || start > end)
    {
      throw new Error("Implementation Error: deleteRange out of bounds: start must be non-negative and less than end.");
    }
    // find beginning
    int cursor = 0; // mark the position for the current operation being edited.
    int rlength = 1 + end - start; // number of positions to delete
    int oldlen = length;
    int o = 0;
    boolean editing = false;
    char[] oldops = operation;
    int[] oldrange = range;
    length = 0;
    operation = null;
    range = null;
    compact_operations();
    while (o < oldlen && cursor <= end && rlength > 0)
    {
      if (oldops[o] == D)
      {
        // absorbed into new deleted region.
        addDeleted(oldrange[o++]);
        continue;
      }

      int remain = oldrange[o]; // number of op characters left to edit
      if (!editing)
      {
        if ( (cursor + remain) <= start)
        {
          addOperation(oldops[o], oldrange[o]);
          cursor += oldrange[o++];
          continue; // next operation
        }
        editing = true;
        // add operations before hidden region
        if (start - cursor > 0)
        {
          addOperation(oldops[o], start - cursor);
          remain -= start - cursor;
        }
      }
      // start inserting new ops
      if (o < oldlen && editing && rlength > 0 && remain > 0)
      {
        switch (oldops[o])
        {
          case M:
            if (rlength > remain)
            {
              addDeleted(remain);
              deleted += remain;
            }
            else
            {
              deleted += rlength;
              addDeleted(rlength);
              if (remain - rlength > 0)
              {
                this.addOperation(M, remain - rlength); // add remaining back.
              }
              rlength = 0;
              remain = 0;
            }
            break;
          case I:
            if (remain - rlength > 0)
            {
              // only remove some gaps
              addInsertion(remain - rlength);
              rlength = 0;
            }
            break;
          case D:
            throw new Error("Implementation error."); // do nothing;
          default:
            throw new Error("Implementation Error! Unknown operation '" +
                            oldops[o] + "'");
        }
        rlength -= remain;
        remain = oldrange[++o]; // number of op characters left to edit
      }
    }
    // add remaining
    while (o < oldlen)
    {
      addOperation(oldops[o], oldrange[o++]);
    }
    //if (cursor<(start+1)) {
    // ran out of ops - nothing to do here ?
    // addInsertion(start-cursor);
    //}
    return deleted;
  }

  /**
   * Deleted regions mean that there will be discontinuous sequence numbering in the
   * sequence returned by getSeq(char).
   * @return true if there deletions
   */
  public boolean hasDeletedRegions()
  {
    for (int i = 0; i < length; i++)
    {
      if (operation[i] == D)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * enumerate the ranges on seq that are marked as deleted in this cigar
   * @return int[] { vis_start, sym_start, length }
   */
  public int[] getDeletedRegions()
  {
    if (length == 0)
    {
      return null;
    }
    Vector dr = new Vector();
    int cursor = 0, vcursor = 0;
    for (int i = 0; i < length; i++)
    {
      switch (operation[i])
      {
        case M:
          cursor += range[i];
        case I:
          vcursor += range[i];
          break;
        case D:
          dr.addElement(new int[]
                        {vcursor, cursor, range[i]});
          cursor += range[i];
      }
    }
    if (dr.size() == 0)
    {
      return null;
    }
    int[] delregions = new int[dr.size() * 3];
    for (int i = 0, l = dr.size(); i < l; i++)
    {
      int[] reg = (int[]) dr.elementAt(i);
      delregions[i * 3] = reg[0];
      delregions[i * 3 + 1] = reg[1];
      delregions[i * 3 + 2] = reg[2];
    }
    return delregions;
  }

  /**
   * sum of ranges in cigar string
   * @return int number of residues hidden, matched, or gaps inserted into sequence
   */
  public int getFullWidth()
  {
    int w = 0;
    if (range != null)
    {
      for (int i = 0; i < length; i++)
      {
        w += range[i];
      }
    }
    return w;
  }

  /**
   * Visible length of aligned sequence
   * @return int length of including gaps and less hidden regions
   */
  public int getWidth()
  {
    int w = 0;
    if (range != null)
    {
      for (int i = 0; i < length; i++)
      {
        if (operation[i] == M || operation[i] == I)
        {
          w += range[i];
        }
      }
    }
    return w;
  }

  /**
   * mark a range of inserted residues
   * @param range int
   */
  public void addInsertion(int range)
  {
    this.addOperation(I, range);
  }

  /**
   * mark the next range residues as hidden (not aligned) or deleted
   * @param range int
   */
  public void addDeleted(int range)
  {
    this.addOperation(D, range);
  }

  /**
   * Modifies operation list to delete columns from start to end (inclusive)
   * editing will remove insertion operations, and convert matches to deletions
   * @param start alignment column
   * @param end alignment column
   * @return boolean true if residues were marked as deleted.
     public boolean deleteRange(int start, int end)
     {
    boolean deleted = false;
    int op = 0, prevop = -1, firstm = -1,
        lastm = -1, postop = -1;
    int width = 0; // zero'th column
    if (length > 0)
    {
      // find operation bracketing start of the range
      do
      {
        if (operation[op] != D)
        {
          width += range[prevop = op];
        }
        op++;
      }
      while (op < length && width < start);
    }
    if (width < start)
    {
      // run off end - add more operations up to deletion.
      addInsertion(start - width);
    }
    else
    {
      // edit existing operations.
      op = prevop;
      width -= range[prevop];
      int[] oldrange = range;
      char[] oldops = operation;
      range = new int[oldrange.length];
      operation = new char[oldops.length];
      if (op < length)
      {
        do
        {
          if (operation[op] != D)
          {
            width += range[postop = op];
          }
          op++;
        }
        while (op < length && width <= end);
      }
    }
    if (deleted == true)
    {
      addDeleted(end - start + 1);
    }
    return deleted;
     }
   */
  /**
   * Return an ENSEMBL style cigar string where D may indicates excluded parts of seq
   * @return String of form ([0-9]+[IMD])+
   */
  public String getCigarstring()
  {
    StringBuffer cigarString = new StringBuffer();
    for (int i = 0; i < length; i++)
    {
      cigarString.append("" + range[i]);
      cigarString.append(operation[i]);
    }
    return cigarString.toString();
  }
}
