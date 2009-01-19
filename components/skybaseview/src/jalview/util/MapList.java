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
 * MapList
 * Simple way of bijectively mapping a non-contiguous linear range to another non-contiguous linear range
 * Use at your own risk!
 * TODO: efficient implementation of private posMap method
 * TODO: test/ensure that sense of from and to ratio start position is conserved (codon start position recovery)
 * TODO: optimize to use int[][] arrays rather than vectors.
 */
public class MapList
{
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(MapList obj) {
    if (obj==this)
      return true;
    if (obj!=null && obj.fromRatio==fromRatio && obj.toRatio==toRatio
        && obj.fromShifts!=null && obj.toShifts!=null) {
      int i,iSize=fromShifts.size(),j,jSize=obj.fromShifts.size();
      if (iSize!=jSize)
        return false;
      for (i=0,iSize=fromShifts.size(),j=0, jSize=obj.fromShifts.size(); i<iSize;) {
        int[] mi=(int[]) fromShifts.elementAt(i++);
        int[] mj=(int[]) obj.fromShifts.elementAt(j++);
        if (mi[0]!=mj[0] || mi[1]!=mj[1])
          return false;
      }
      iSize=toShifts.size();
      jSize=obj.toShifts.size();
      if (iSize!=jSize)
        return false;
      for (i=0,j=0; i<iSize;) {
        int[] mi=(int[]) toShifts.elementAt(i++);
        int[] mj=(int[]) obj.toShifts.elementAt(j++);
        if (mi[0]!=mj[0] || mi[1]!=mj[1])
          return false;
      }
      return true;
    }
    return false;
  }
  public Vector fromShifts;
  public Vector toShifts;
  int fromRatio; // number of steps in fromShifts to one toRatio unit
  int toRatio; // number of steps in toShifts to one fromRatio
  /**
   * lowest and highest value in the from Map
   */
  int[] fromRange=null;
  /**
   * lowest and highest value in the to Map
   */
  int[] toRange=null;
  public int getFromRatio()
  {
    return fromRatio;
  }
  public int getToRatio()
  {
    return toRatio;
  }
  public int getFromLowest() {
    return fromRange[0];
  }
  public int getFromHighest() {
    return fromRange[1];
  }
  public int getToLowest() {
    return toRange[0];
  }
  public int getToHighest() {
    return toRange[1];
  }
  private void ensureRange(int[] limits, int pos) {
    if (limits[0]>pos)
      limits[0]=pos;
    if (limits[1]<pos)
      limits[1]=pos;
  }
  public MapList(int from[], int to[], int fromRatio, int toRatio)
  {
    fromRange=new int[] { from[0],from[1] };
    toRange=new int[] { to[0],to[1] };

    fromShifts = new Vector();
    for (int i=0;i<from.length; i+=2)
    {
      ensureRange(fromRange, from[i]);
      ensureRange(fromRange, from[i+1]);

      fromShifts.addElement(new int[]
                             {from[i], from[i + 1]});
    }
    toShifts = new Vector();
    for (int i=0;i<to.length; i+=2)
    {
      ensureRange(toRange, to[i]);
      ensureRange(toRange, to[i+1]);
      toShifts.addElement(new int[]
                           {to[i], to[i + 1]});
    }
    this.fromRatio=fromRatio;
    this.toRatio=toRatio;
  }
  public MapList(MapList map)
  {
    this.fromRange = new int[]
    { map.fromRange[0], map.fromRange[1] };
    this.toRange = new int[]
    { map.toRange[0], map.toRange[1] };
    this.fromRatio = map.fromRatio;
    this.toRatio = map.toRatio;
    if (map.fromShifts != null)
    {
      this.fromShifts = new Vector();
      Enumeration e = map.fromShifts.elements();
      while (e.hasMoreElements())
      {
        int[] el = (int[]) e.nextElement();
        fromShifts.addElement(new int[]
        { el[0], el[1] });
      }
    }
    if (map.toShifts != null)
    {
      this.toShifts = new Vector();
      Enumeration e = map.toShifts.elements();
      while (e.hasMoreElements())
      {
        int[] el = (int[]) e.nextElement();
        toShifts.addElement(new int[]
        { el[0], el[1] });
      }
    }
  }
  /**
   * get all mapped positions from 'from' to 'to'
   * @return int[][] { int[] { fromStart, fromFinish, toStart, toFinish }, int [fromFinish-fromStart+2] { toStart..toFinish mappings}}
   */
  public int[][] makeFromMap()
  {
    return posMap(fromShifts, fromRatio, toShifts, toRatio);
  }
  /**
   * get all mapped positions from 'to' to 'from'
   * @return int[to position]=position mapped in from
   */
  public int[][] makeToMap()
  {
    return posMap(toShifts,toRatio, fromShifts, fromRatio);
  }
  /**
   * construct an int map for intervals in intVals
   * @param intVals
   * @return int[] { from, to pos in range }, int[range.to-range.from+1] returning mapped position
   */
  private int[][] posMap(Vector intVals, int ratio, Vector toIntVals,
      int toRatio)
  {
    int iv=0,ivSize = intVals.size();
    if (iv>=ivSize)
    {
      return null;
    }
    int[] intv=(int[]) intVals.elementAt(iv++);
    int from=intv[0],to=intv[1];
    if (from > to)
    {
      from = intv[1];
      to=intv[0];
    }
    while (iv<ivSize)
    {
      intv = (int[]) intVals.elementAt(iv++);
      if (intv[0]<from)
      {
        from=intv[0];
      }
      if (intv[1]<from)
      {
        from=intv[1];
      }
      if (intv[0]>to)
      {
        to=intv[0];
      }
      if (intv[1]>to)
      {
        to=intv[1];
      }
    }
    int tF=0,tT=0;
    int mp[][] = new int[to-from+2][];
    for (int i = 0; i < mp.length; i++)
    {
      int[] m = shift(i+from,intVals,ratio,toIntVals, toRatio);
      if (m != null)
      {
        if (i == 0)
        {
          tF=tT=m[0];
        }
        else
        {
          if (m[0] < tF)
          {
            tF=m[0];
          }
          if (m[0] > tT)
          {
            tT=m[0];
          }
        }
      }
      mp[i] = m;
    }
    int[][] map = new int[][]
                            {
        new int[]
                {
            from, to, tF, tT}, new int[to - from + 2]};

    map[0][2] = tF;
    map[0][3] = tT;

    for (int i = 0; i < mp.length; i++)
    {
      if (mp[i] != null)
      {
        map[1][i] = mp[i][0]-tF;
      }
      else
      {
        map[1][i] = -1; // indicates an out of range mapping
      }
    }
    return map;
  }
  /**
   * addShift
   * @param pos start position for shift (in original reference frame)
   * @param shift length of shift
   *
  public void addShift(int pos, int shift)
  {
    int sidx = 0;
    int[] rshift=null;
    while (sidx<shifts.size() && (rshift=(int[]) shifts.elementAt(sidx))[0]<pos)
      sidx++;
    if (sidx==shifts.size())
      shifts.insertElementAt(new int[] { pos, shift}, sidx);
    else
      rshift[1]+=shift;
  }
   */
  /**
   * shift from pos to To(pos)
   *
   * @param pos int
   * @return int shifted position in To, frameshift in From, direction of mapped symbol in To
   */
  public int[] shiftFrom(int pos)
  {
    return shift(pos, fromShifts, fromRatio, toShifts, toRatio);
  }

  /**
   * inverse of shiftFrom - maps pos in To to a position in From
   * @param pos (in To)
   * @return shifted position in From, frameshift in To, direction of mapped symbol in From
   */
  public int[] shiftTo(int pos)
  {
    return shift(pos, toShifts, toRatio, fromShifts, fromRatio);
  }
  /**
   *
   * @param fromShifts
   * @param fromRatio
   * @param toShifts
   * @param toRatio
   * @return
   */
  private int[] shift(int pos, Vector fromShifts, int fromRatio,
      Vector toShifts, int toRatio)
  {
    int[] fromCount = countPos(fromShifts, pos);
    if (fromCount==null)
    {
      return null;
    }
    int fromRemainder=(fromCount[0]-1) % fromRatio;
    int toCount = 1+(((fromCount[0]-1) / fromRatio) * toRatio);
    int[] toPos = countToPos(toShifts, toCount);
    if (toPos==null)
    {
      return null; // throw new Error("Bad Mapping!");
    }
    //System.out.println(fromCount[0]+" "+fromCount[1]+" "+toCount);
    return new int[]
                   {
        toPos[0], fromRemainder, toPos[1]};
  }
  /**
   * count how many positions pos is along the series of intervals.
   * @param intVals
   * @param pos
   * @return number of positions or null if pos is not within intervals
   */
  private int[] countPos(Vector intVals, int pos)
  {
    int count=0,intv[],iv=0,ivSize=intVals.size();
    while (iv<ivSize)
    {
      intv = (int[])intVals.elementAt(iv++);
      if (intv[0] <= intv[1])
      {
        if (pos >= intv[0] && pos <= intv[1])
        {
          return new int[]
                         {
              count + pos - intv[0] + 1, +1};
        }
        else
        {
          count+=intv[1]-intv[0]+1;
        }
      }
      else
      {
        if (pos >= intv[1] && pos <= intv[0])
        {
          return new int[]
                         {
              count + intv[0] - pos + 1, -1};
        }
        else
        {
          count+=intv[0]-intv[1]+1;
        }
      }
    }
    return null;
  }
  /**
   * count out pos positions into a series of intervals and return the position
   * @param intVals
   * @param pos
   * @return position pos in interval set
   */
  private int[] countToPos(Vector intVals, int pos)
  {
    int count = 0, diff = 0, iv=0,ivSize=intVals.size(), intv[] =
    {
        0, 0};
    while (iv<ivSize)
    {
      intv = (int[])intVals.elementAt(iv++);
      diff = intv[1]-intv[0];
      if (diff >= 0)
      {
        if (pos <= count + 1 + diff)
        {
          return new int[]
                         {
              pos - count - 1 + intv[0], +1};
        }
        else
        {
          count+=1+diff;
        }
      }
      else
      {
        if (pos <= count + 1 - diff)
        {
          return new int[]
                         {
              intv[0] - (pos - count - 1), -1};
        }
        else
        {
          count+=1-diff;
        }
      }
    }
    return null;//(diff<0) ? (intv[1]-1) : (intv[0]+1);
  }
  /**
   * find series of intervals mapping from start-end in the From map.
   * @param start position in to map
   * @param end position in to map
   * @return series of ranges in from map
   */
  public int[] locateInFrom(int start, int end) {
    // inefficient implementation
    int fromStart[] = shiftTo(start);
    int fromEnd[] = shiftTo(end); // needs to be inclusive of end of symbol position
    if (fromStart==null || fromEnd==null)
      return null;
    int iv[] = getIntervals(fromShifts, fromStart, fromEnd,fromRatio);
    return iv;
  }

  /**
   * find series of intervals mapping from start-end in the to map.
   * @param start position in from map
   * @param end position in from map
   * @return series of ranges in to map
   */
  public int[] locateInTo(int start, int end) {
    // inefficient implementation
    int toStart[] = shiftFrom(start);
    int toEnd[] = shiftFrom(end);
    if (toStart==null || toEnd==null)
      return null;
    int iv[] = getIntervals(toShifts, toStart, toEnd, toRatio);
    return iv;
  }
  /**
   * like shift - except returns the intervals in the given vector of shifts which were spanned
   * in traversing fromStart to fromEnd
   * @param fromShifts2
   * @param fromStart
   * @param fromEnd
   * @param fromRatio2
   * @return series of from,to intervals from from first position of starting region to final position of ending region inclusive
   */
  private int[] getIntervals(Vector fromShifts2, int[] fromStart, int[] fromEnd, int fromRatio2)
  {
    int startpos,endpos;
    startpos = fromStart[0]; // first position in fromStart 
    endpos = fromEnd[0]+fromEnd[2]*(fromRatio2-1); // last position in fromEnd
    int intv=0,intvSize= fromShifts2.size();
    int iv[],i=0,fs=-1,fe=-1; // containing intervals
    while (intv<intvSize && (fs==-1 || fe==-1)) {
      iv = (int[]) fromShifts2.elementAt(intv++);
      if (iv[0]<=iv[1]) {
        if (fs==-1 && startpos>=iv[0] && startpos<=iv[1]) {
          fs = i;
        }
        if (fe==-1 && endpos>=iv[0] && endpos<=iv[1]) {
          fe = i;
        }
      } else {
        if (fs==-1 && startpos<=iv[0] && startpos>=iv[1]) {
          fs = i;
        }
        if (fe==-1 && endpos<=iv[0] && endpos>=iv[1]) {
          fe = i;
        }
      }
      i++;
    }
    if (fs==fe && fe==-1)
      return null;
    Vector ranges=new Vector();
    if (fs<=fe) {
      intv = fs;
      i=fs;
      // truncate initial interval
      iv = (int[]) fromShifts2.elementAt(intv++);
      iv = new int[] { iv[0], iv[1]};// clone
      if (i==fs)
        iv[0] = startpos;
      while (i!=fe) {
        ranges.addElement(iv); // add initial range
        iv = (int[]) fromShifts2.elementAt(intv++); // get next interval
        iv = new int[] { iv[0], iv[1]};// clone
        i++;
      }
      if (i==fe)
        iv[1] = endpos;
      ranges.addElement(iv); // add only - or final range
    } else {
      // walk from end of interval.
      i=fromShifts2.size()-1;
      while (i>fs) {
        i--;
      }
      iv = (int[]) fromShifts2.elementAt(i);
      iv = new int[] { iv[1], iv[0]};//  reverse and clone
      // truncate initial interval
      if (i==fs)
      {
        iv[0] = startpos;
      }
      while (i!=fe) {
        ranges.addElement(iv); // add (truncated) reversed interval
        iv = (int[]) fromShifts2.elementAt(--i);
        iv = new int[] { iv[1], iv[0] }; // reverse and clone
      }
      if (i==fe) {
        // interval is already reversed
        iv[1] = endpos;
      }
      ranges.addElement(iv); // add only - or final range
    }
    // create array of start end intervals.
    int[] range = null;
    if (ranges!=null && ranges.size()>0)
    {
      range = new int[ranges.size()*2];
      intv = 0;
      intvSize=ranges.size();
      i=0;
      while (intv<intvSize)
      {
        iv = (int[]) ranges.elementAt(intv);
        range[i++] = iv[0];
        range[i++] = iv[1];
        ranges.setElementAt(null, intv++); // remove
      }
    }
    return range;
  }
  /**
 * get the 'initial' position of mpos in To
 * @param mpos position in from
 * @return position of first word in to reference frame
 */
public int getToPosition(int mpos)
{
  int[] mp = shiftTo(mpos);
  if (mp!=null)
  {
    return mp[0];
  }
  return mpos;
}
/**
 * get range of positions in To frame for the mpos word in From
 * @param mpos position in From
 * @return null or int[] first position in To for mpos, last position in to for Mpos
 */
public int[] getToWord(int mpos) {
  int[] mp=shiftTo(mpos);
  if (mp!=null) {
      return new int[] {mp[0], mp[0]+mp[2]*(getFromRatio()-1)};
  }
  return null;
}
/**
 * get From position in the associated
 * reference frame for position pos in the
 * associated sequence.
 * @param pos
 * @return
 */
public int getMappedPosition(int pos) {
  int[] mp = shiftFrom(pos);
  if (mp!=null)
  {
    return mp[0];
  }
  return pos;
}
public int[] getMappedWord(int pos) {
  int[] mp = shiftFrom(pos);
  if (mp!=null)
  {
    return new int[] { mp[0], mp[0]+mp[2]*(getToRatio()-1)};
  }
  return null;
}

  /**
   * test routine. not incremental.
   * @param ml
   * @param fromS
   * @param fromE
   */
  public static void testMap(MapList ml, int fromS, int fromE)
  {
    for (int from = 1; from <= 25; from++)
    {
      int[] too=ml.shiftFrom(from);
      System.out.print("ShiftFrom("+from+")==");
      if (too==null)
      {
        System.out.print("NaN\n");
      }
      else
      {
        System.out.print(too[0]+" % "+too[1]+" ("+too[2]+")");
        System.out.print("\t+--+\t");
        int[] toofrom=ml.shiftTo(too[0]);
        if (toofrom != null)
        {
          if (toofrom[0]!=from)
          {
            System.err.println("Mapping not reflexive:" + from + " " + too[0] +
                "->" + toofrom[0]);
          }
          System.out.println("ShiftTo(" + too[0] + ")==" + toofrom[0] + " % " +
              toofrom[1]+" ("+toofrom[2]+")");
        }
        else
        {
          System.out.println("ShiftTo(" + too[0] + ")==" +
          "NaN! - not Bijective Mapping!");
        }
      }
    }
    int mmap[][] = ml.makeFromMap();
    System.out.println("FromMap : (" + mmap[0][0] + " " + mmap[0][1] + " " +
        mmap[0][2] + " " + mmap[0][3] + " ");
    for (int i = 1; i <= mmap[1].length; i++)
    {
      if (mmap[1][i - 1] == -1)
      {
        System.out.print(i+"=XXX");

      }
      else
      {
        System.out.print(i+"="+(mmap[0][2]+mmap[1][i-1]));
      }
      if (i % 20==0)
      {
        System.out.print("\n");
      }
      else
      {
        System.out.print(",");
      }
    }
    //test range function
    System.out.print("\nTest locateInFrom\n");
    {
      int f=mmap[0][2],t=mmap[0][3];
      while (f<=t) {
        System.out.println("Range "+f+" to "+t);
        int rng[] = ml.locateInFrom(f,t);
        if (rng!=null)
        {
          for (int i=0; i<rng.length; i++) {
            System.out.print(rng[i]+((i%2==0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        System.out.print("\nReversed\n");
        rng = ml.locateInFrom(t,f);
        if (rng!=null)
        {
          for (int i=0; i<rng.length; i++) {
            System.out.print(rng[i]+((i%2==0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        System.out.print("\n");
        f++;t--;
      }
    }
    System.out.print("\n");
    mmap = ml.makeToMap();
    System.out.println("ToMap : (" + mmap[0][0] + " " + mmap[0][1] + " " +
        mmap[0][2] + " " + mmap[0][3] + " ");
    for (int i = 1; i <= mmap[1].length; i++)
    {
      if (mmap[1][i - 1] == -1)
      {
        System.out.print(i+"=XXX");

      }
      else
      {
        System.out.print(i+"="+(mmap[0][2]+mmap[1][i-1]));
      }
      if (i % 20==0)
      {
        System.out.print("\n");
      }
      else
      {
        System.out.print(",");
      }
    }
    System.out.print("\n");
    //test range function
    System.out.print("\nTest locateInTo\n");
    {
      int f=mmap[0][2],t=mmap[0][3];
      while (f<=t) {
        System.out.println("Range "+f+" to "+t);
        int rng[] = ml.locateInTo(f,t);
        if (rng!=null) {
          for (int i=0; i<rng.length; i++) {
            System.out.print(rng[i]+((i%2==0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        System.out.print("\nReversed\n");
        rng = ml.locateInTo(t,f);
        if (rng!=null)
        {
          for (int i=0; i<rng.length; i++) {
            System.out.print(rng[i]+((i%2==0) ? "," : ";"));
          }
        }
        else
        {
          System.out.println("No range!");
        }
        f++; t--;
        System.out.print("\n");
      }
    }

  }

  public static void main(String argv[])
  {
    MapList ml = new MapList(new int[]
                                     {1, 5, 10, 15, 25, 20},
                                     new int[]
                                             {51, 1}, 1, 3);
    MapList ml1 = new MapList(new int[]
                                      {1, 3, 17, 4},
                                      new int[]
                                              {51, 1}, 1, 3);
    MapList ml2 = new MapList(new int[] { 1, 60 },
        new int[] { 1, 20 }, 3, 1);
    // test internal consistency
    int to[] = new int[51];
    MapList.testMap(ml, 1, 60);
    /*
      for (int from=1; from<=51; from++) {
          int[] too=ml.shiftTo(from);
          int[] toofrom=ml.shiftFrom(too[0]);
          System.out.println("ShiftFrom("+from+")=="+too[0]+" % "+too[1]+"\t+-+\tShiftTo("+too[0]+")=="+toofrom[0]+" % "+toofrom[1]);
      }*/
    System.out.print("Success?\n"); // if we get here - something must be working!
  }
}
