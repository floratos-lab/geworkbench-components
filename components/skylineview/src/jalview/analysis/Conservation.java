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
 * Calculates conservation values for a given set of sequences
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Conservation
{
  SequenceI[] sequences;
  int start;
  int end;
  Vector seqNums; // vector of int vectors where first is sequence checksum
  int maxLength = 0; //  used by quality calcs
  boolean seqNumsChanged = false; // updated after any change via calcSeqNum;
  Hashtable[] total;

  /** Stores calculated quality values */
  public Vector quality;

  /** Stores maximum and minimum values of quality values  */
  public Double[] qualityRange = new Double[2];
  String consString = "";
  Sequence consSequence;
  Hashtable propHash;
  int threshold;
  String name = "";
  int[][] cons2;

  /**
   * Creates a new Conservation object.
   *
   * @param name Name of conservation
   * @param propHash DOCUMENT ME!
   * @param threshold to count the residues in residueHash(). commonly used value is 3
   * @param sequences sequences to be used in calculation
   * @param start start residue position
   * @param end end residue position
   */
  public Conservation(String name, Hashtable propHash, int threshold,
                      Vector sequences, int start, int end)
  {

    this.name = name;
    this.propHash = propHash;
    this.threshold = threshold;
    this.start = start;
    this.end = end;

    maxLength = end - start + 1; // default width includes bounds of calculation

    int s, sSize = sequences.size();
    SequenceI[] sarray = new SequenceI[sSize];
    this.sequences = sarray;

    for (s = 0; s < sSize; s++)
    {
      sarray[s] = (SequenceI) sequences.elementAt(s);
      if (sarray[s].getLength() > maxLength)
      {
        maxLength = sarray[s].getLength();
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   */
  private void calcSeqNum(int i)
  {
    String sq = null; // for dumb jbuilder not-inited exception warning
    int[] sqnum = null;

    int sSize = sequences.length;

    if ( (i > -1) && (i < sSize))
    {
      sq = sequences[i].getSequenceAsString();

      if (seqNums.size() <= i)
      {
        seqNums.addElement(new int[sq.length() + 1]);
      }

      if (sq.hashCode() != ( (int[]) seqNums.elementAt(i))[0])
      {
        int j;
        int len;
        seqNumsChanged = true;
        len = sq.length();

        if (maxLength < len)
        {
          maxLength = len;
        }

        sqnum = new int[len + 1]; // better to always make a new array - sequence can change its length
        sqnum[0] = sq.hashCode();

        for (j = 1; j <= len; j++)
        {
          sqnum[j] = jalview.schemes.ResidueProperties.aaIndex[sq.charAt(j - 1)];
        }

        seqNums.setElementAt(sqnum, i);
      }
      else
      {
        System.out.println("SEQUENCE HAS BEEN DELETED!!!");
      }
    }
    else
    {
      // JBPNote INFO level debug
      System.err.println(
          "ERROR: calcSeqNum called with out of range sequence index for Alignment\n");
    }
  }

  /**
   * Calculates the conservation values for given set of sequences
   */
  public void calculate()
  {
    Hashtable resultHash, ht;
    int thresh, j, jSize = sequences.length;
    int[] values; // Replaces residueHash
    String type, res = null;
    char c;
    Enumeration enumeration2;

    total = new Hashtable[maxLength];

    for (int i = start; i <= end; i++)
    {
      values = new int[255];

      for (j = 0; j < jSize; j++)
      {
        if (sequences[j].getLength() > i)
        {
          c = sequences[j].getCharAt(i);

          // No need to check if its a '-'
          if (c == '.' || c == ' ')
          {
            c = '-';
          }

          if ('a' <= c && c <= 'z')
          {
            c -= (32); // 32 = 'a' - 'A'
          }

          values[c]++;
        }
        else
        {
          values['-']++;
        }
      }

      //What is the count threshold to count the residues in residueHash()
      thresh = (threshold * (jSize)) / 100;

      //loop over all the found residues
      resultHash = new Hashtable();
      for (char v = '-'; v < 'Z'; v++)
      {

        if (values[v] > thresh)
        {
          res = String.valueOf(v);

          //Now loop over the properties
          enumeration2 = propHash.keys();

          while (enumeration2.hasMoreElements())
          {
            type = (String) enumeration2.nextElement();
            ht = (Hashtable) propHash.get(type);

            //Have we ticked this before?
            if (!resultHash.containsKey(type))
            {
              if (ht.containsKey(res))
              {
                resultHash.put(type, ht.get(res));
              }
              else
              {
                resultHash.put(type, ht.get("-"));
              }
            }
            else if ( ( (Integer) resultHash.get(type)).equals(
                (Integer) ht.get(res)) == false)
            {
              resultHash.put(type, new Integer( -1));
            }
          }
        }
      }

      total[i - start] = resultHash;
    }
  }

  /***
   * countConsNGaps
   * returns gap count in int[0], and conserved residue count in int[1]
   */
  public int[] countConsNGaps(int j)
  {
    int count = 0;
    int cons = 0;
    int nres = 0;
    int[] r = new int[2];
    char f = '$';
    int i, iSize = sequences.length;
    char c;

    for (i = 0; i < iSize; i++)
    {
      if (j >= sequences[i].getLength())
      {
        count++;
        continue;
      }

      c = sequences[i].getCharAt(j); // gaps do not have upper/lower case

      if (jalview.util.Comparison.isGap( (c)))
      {
        count++;
      }
      else
      {
        nres++;

        if (nres == 1)
        {
          f = c;
          cons++;
        }
        else if (f == c)
        {
          cons++;
        }
      }
    }

    r[0] = (nres == cons) ? 1 : 0;
    r[1] = count;

    return r;
  }

  /**
   * Calculates the conservation sequence
   *
   * @param consflag if true, poitiveve conservation; false calculates negative conservation
   * @param percentageGaps commonly used value is 25
   */
  public void verdict(boolean consflag, float percentageGaps)
  {
    StringBuffer consString = new StringBuffer();
    String type;
    Integer result;
    int[] gapcons;
    int totGaps, count;
    float pgaps;
    Hashtable resultHash;
    Enumeration enumeration;

    //NOTE THIS SHOULD CHECK IF THE CONSEQUENCE ALREADY
    //EXISTS AND NOT OVERWRITE WITH '-', BUT THIS CASE
    //DOES NOT EXIST IN JALVIEW 2.1.2
    for (int i = 0; i < start; i++)
    {
      consString.append('-');
    }

    for (int i = start; i <= end; i++)
    {
      gapcons = countConsNGaps(i);
      totGaps = gapcons[1];
      pgaps = ( (float) totGaps * 100) / (float) sequences.length;

      if (percentageGaps > pgaps)
      {
        resultHash = total[i - start];

        //Now find the verdict
        count = 0;
        enumeration = resultHash.keys();

        while (enumeration.hasMoreElements())
        {
          type = (String) enumeration.nextElement();
          result = (Integer) resultHash.get(type);

          //Do we want to count +ve conservation or +ve and -ve cons.?
          if (consflag)
          {
            if (result.intValue() == 1)
            {
              count++;
            }
          }
          else
          {
            if (result.intValue() != -1)
            {
              count++;
            }
          }
        }

        if (count < 10)
        {
          consString.append(count); // Conserved props!=Identity
        }
        else
        {
          consString.append( (gapcons[0] == 1) ? "*" : "+");
        }
      }
      else
      {
        consString.append('-');
      }
    }

    consSequence = new Sequence(name, consString.toString(), start, end);
  }

  /**
   *
   *
   * @return Conservation sequence
   */
  public Sequence getConsSequence()
  {
    return consSequence;
  }

  // From Alignment.java in jalview118
  public void findQuality()
  {
    findQuality(0, maxLength - 1);
  }

  /**
   * DOCUMENT ME!
   */
  private void percentIdentity2()
  {
    seqNums = new Vector();
    // calcSeqNum(s);
    int i = 0, iSize = sequences.length;
    //Do we need to calculate this again?
    for (i = 0; i < iSize; i++)
    {
      calcSeqNum(i);
    }

    if ( (cons2 == null) || seqNumsChanged)
    {
      cons2 = new int[maxLength][24];

      // Initialize the array
      for (int j = 0; j < 24; j++)
      {
        for (i = 0; i < maxLength; i++)
        {
          cons2[i][j] = 0;
        }
      }

      int[] sqnum;
      int j = 0;

      while (j < sequences.length)
      {
        sqnum = (int[]) seqNums.elementAt(j);

        for (i = 1; i < sqnum.length; i++)
        {
          cons2[i - 1][sqnum[i]]++;
        }

        for (i = sqnum.length - 1; i < maxLength; i++)
        {
          cons2[i][23]++; // gap count
        }

        j++;
      }

      // unnecessary ?

      /* for (int i=start; i <= end; i++) {
           int max = -1000;
                   int maxi = -1;
                   int maxj = -1;

                   for (int j=0;j<24;j++) {
        if (cons2[i][j] > max) {
        max = cons2[i][j];
        maxi = i;
        maxj = j;
                   }

                   }
                   } */
    }
  }

  /**
   * Calculates the quality of the set of sequences
   *
   * @param start Start residue
   * @param end End residue
   */
  public void findQuality(int start, int end)
  {
    quality = new Vector();

    double max = -10000;
    int[][] BLOSUM62 = jalview.schemes.ResidueProperties.getBLOSUM62();

    //Loop over columns // JBPNote Profiling info
    //long ts = System.currentTimeMillis();
    //long te = System.currentTimeMillis();
    percentIdentity2();

    int size = seqNums.size();
    int[] lengths = new int[size];
    double tot, bigtot, sr, tmp;
    double[] x, xx;
    int l, j, i, ii, i2, k, seqNum;

    for (l = 0; l < size; l++)
    {
      lengths[l] = ( (int[]) seqNums.elementAt(l)).length - 1;
    }

    for (j = start; j <= end; j++)
    {
      bigtot = 0;

      // First Xr = depends on column only
      x = new double[24];

      for (ii = 0; ii < 24; ii++)
      {
        x[ii] = 0;

        for (i2 = 0; i2 < 24; i2++)
        {
          x[ii] += ( ( (double) cons2[j][i2] * BLOSUM62[ii][i2]) +
                    4);
        }

        x[ii] /= size;
      }

      // Now calculate D for each position and sum
      for (k = 0; k < size; k++)
      {
        tot = 0;
        xx = new double[24];
        seqNum = (j < lengths[k])
            ? ( (int[]) seqNums.elementAt(k))[j + 1] : 23; // Sequence, or gap at the end

        // This is a loop over r
        for (i = 0; i < 23; i++)
        {
          sr = 0;

          sr = (double) BLOSUM62[i][seqNum] + 4;

          //Calculate X with another loop over residues
          //  System.out.println("Xi " + i + " " + x[i] + " " + sr);
          xx[i] = x[i] - sr;

          tot += (xx[i] * xx[i]);
        }

        bigtot += Math.sqrt(tot);
      }

      // This is the quality for one column
      if (max < bigtot)
      {
        max = bigtot;
      }

      //      bigtot  = bigtot * (size-cons2[j][23])/size;
      quality.addElement(new Double(bigtot));

      // Need to normalize by gaps
    }

    double newmax = -10000;

    for (j = start; j <= end; j++)
    {
      tmp = ( (Double) quality.elementAt(j)).doubleValue();
      tmp = ( (max - tmp) * (size - cons2[j][23])) / size;

      //     System.out.println(tmp+ " " + j);
      quality.setElementAt(new Double(tmp), j);

      if (tmp > newmax)
      {
        newmax = tmp;
      }
    }

    //    System.out.println("Quality " + s);
    qualityRange[0] = new Double(0);
    qualityRange[1] = new Double(newmax);
  }
}
