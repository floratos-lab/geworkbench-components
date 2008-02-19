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
package jalview.io;

/**
 * <p>Title: </p>
 *  PileUpfile
 * <p>Description: </p>
 *
 *  Read and write PileUp style MSF Files.
 *  This used to be the MSFFile class, and was written according to the EBI's idea
 *  of a subset of the MSF alignment format. But, that was updated to reflect current
 *  GCG style IO fashion, as found in Emboss (thanks David Martin!)
 *
 **/
import java.io.*;

import jalview.datamodel.*;
import jalview.util.*;

public class PileUpfile
    extends MSFfile
{

  /**
   * Creates a new MSFfile object.
   */
  public PileUpfile()
  {
  }

  /**
   * Creates a new MSFfile object.
   *
   * @param inFile DOCUMENT ME!
   * @param type DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public PileUpfile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String print()
  {
    return print(getSeqsAsArray());
  }

  public String print(SequenceI[] s)
  {
    StringBuffer out = new StringBuffer("PileUp\n\n");

    int max = 0;
    int maxid = 0;

    int i = 0;
    int bigChecksum = 0;
    int[] checksums = new int[s.length];
    while (i < s.length)
    {
      checksums[i] = checkSum(s[i].getSequenceAsString());
      bigChecksum += checksums[i];
      i++;
    }

    out.append("   MSF: " + s[0].getSequence().length +
               "   Type: P    Check:  " + bigChecksum % 10000 + "   ..\n\n\n");

    i = 0;
    while ( (i < s.length) && (s[i] != null))
    {
      String seq = s[i].getSequenceAsString();
      out.append(" Name: " + printId(s[i]) +
                 " oo  Len:  " +
                 seq.length() + "  Check:  " + checksums[i] +
                 "  Weight:  1.00\n");

      if (seq.length() > max)
      {
        max = seq.length();
      }

      if (s[i].getName().length() > maxid)
      {
        maxid = s[i].getName().length();
      }

      i++;
    }

    if (maxid < 10)
    {
      maxid = 10;
    }

    maxid++;
    out.append("\n\n//\n\n");

    int len = 50;

    int nochunks = (max / len) + 1;

    if ( (max % len) == 0)
    {
      nochunks--;
    }

    for (i = 0; i < nochunks; i++)
    {
      int j = 0;

      while ( (j < s.length) && (s[j] != null))
      {
        String name = printId(s[j]);

        out.append(new Format("%-" + maxid + "s").form(name + " "));

        for (int k = 0; k < 5; k++)
        {
          int start = (i * 50) + (k * 10);
          int end = start + 10;

          if ( (end < s[j].getSequence().length) &&
              (start < s[j].getSequence().length))
          {
            out.append(s[j].getSequence(start, end));

            if (k < 4)
            {
              out.append(" ");
            }
            else
            {
              out.append("\n");
            }
          }
          else
          {
            if (start < s[j].getSequence().length)
            {
              out.append(s[j].getSequenceAsString().substring(start));
              out.append("\n");
            }
            else
            {
              if (k == 0)
              {
                out.append("\n");
              }
            }
          }
        }

        j++;
      }

      out.append("\n");
    }

    return out.toString();
  }
}
