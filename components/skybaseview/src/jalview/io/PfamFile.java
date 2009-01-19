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

import java.io.*;
import java.util.*;

import jalview.datamodel.*;
import jalview.util.*;

public class PfamFile
    extends AlignFile
{

  public PfamFile()
  {
  }

  public PfamFile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  public void initData()
  {
    super.initData();
  }

  public void parse()
      throws IOException
  {
    int i = 0;
    String line;

    Hashtable seqhash = new Hashtable();
    Vector headers = new Vector();

    while ( (line = nextLine()) != null)
    {
      if (line.indexOf(" ") != 0)
      {
        if (line.indexOf("#") != 0)
        {
          StringTokenizer str = new StringTokenizer(line, " ");
          String id = "";

          if (str.hasMoreTokens())
          {
            id = str.nextToken();

            StringBuffer tempseq;

            if (seqhash.containsKey(id))
            {
              tempseq = (StringBuffer) seqhash.get(id);
            }
            else
            {
              tempseq = new StringBuffer();
              seqhash.put(id, tempseq);
            }

            if (! (headers.contains(id)))
            {
              headers.addElement(id);
            }

            tempseq.append(str.nextToken());
          }
        }
      }
    }

    this.noSeqs = headers.size();

    if (noSeqs < 1)
    {
      throw new IOException("No sequences found (PFAM input)");
    }

    for (i = 0; i < headers.size(); i++)
    {
      if (seqhash.get(headers.elementAt(i)) != null)
      {
        if (maxLength < seqhash.get(headers.elementAt(i)).toString()
            .length())
        {
          maxLength = seqhash.get(headers.elementAt(i)).toString()
              .length();
        }

        Sequence newSeq = parseId(headers.elementAt(i).toString());
        newSeq.setSequence(seqhash.get(headers.elementAt(i).toString()).
                           toString());
        seqs.addElement(newSeq);
      }
      else
      {
        System.err.println("PFAM File reader: Can't find sequence for " +
                           headers.elementAt(i));
      }
    }
  }

  public String print(SequenceI[] s)
  {
    StringBuffer out = new StringBuffer("");

    int max = 0;
    int maxid = 0;

    int i = 0;

    while ( (i < s.length) && (s[i] != null))
    {
      String tmp = printId(s[i]);

      if (s[i].getSequence().length > max)
      {
        max = s[i].getSequence().length;
      }

      if (tmp.length() > maxid)
      {
        maxid = tmp.length();
      }

      i++;
    }

    if (maxid < 15)
    {
      maxid = 15;
    }

    int j = 0;

    while ( (j < s.length) && (s[j] != null))
    {
      out.append(new Format("%-" + maxid + "s").form(printId(s[j]) + " "));

      out.append(s[j].getSequenceAsString() + "\n");
      j++;
    }

    out.append("\n");

    return out.toString();
  }

  public String print()
  {
    return print(getSeqsAsArray());
  }
}
