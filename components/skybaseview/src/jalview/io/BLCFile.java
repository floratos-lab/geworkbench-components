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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class BLCFile
extends AlignFile
{
  Vector titles;

  /**
   * Creates a new BLCFile object.
   */
  public BLCFile()
  {
  }

  /**
   * Creates a new BLCFile object.
   *
   * @param inFile DOCUMENT ME!
   * @param type DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public BLCFile(String inFile, String type)
  throws IOException
  {
    super(inFile, type);
  }

  /**
   * DOCUMENT ME!
   */
  public void initData()
  {
    super.initData();
    titles = new Vector();
  }
  /**
   * Control the number of block iterations to skip before returning.
   * set to 0 to read first block file entry only
   * set to -1 to read last block file entry only
   * set to greater than zero to skip at most that many entries before parsing
   */
  int iterationSkips=0;
  /**
   * The iteration number for the alignment actually parsed from the blc file 
   */
  int iterationCount=0;

  /**
   * DOCUMENT ME!
   */
  public void parse()
  throws IOException
  {
    StringBuffer headerLines=new StringBuffer();
    int numHeaderLines = 0; // number of lines appended.
    StringBuffer[] seqstrings=null;
    if (suffix!=null) {
      try {
        iterationSkips = Integer.parseInt(suffix); 
      } catch (NumberFormatException e) {
        iterationSkips = 0; // first
      }
    }

    String line = null;

    do {
      boolean idsFound = false;
      boolean newids = false;
        // search for ID header.
      do
      {
        line = nextLine();
        if (line==null)
          break;
        // seek end of ids
        if (line.indexOf("*") > -1)
        {
          idsFound = true;

          break;
        }
        
        int abracket = line.indexOf(">");

        if (abracket > -1)
        {

          if (iterationCount>0 && !newids) {
            // we have a new set of IDs to record.
            newids = true;
            seqs.removeAllElements(); 
          }

          line = line.substring(abracket + 1);

          Sequence seq = parseId(line);
          seqs.addElement(seq);
        } else {
          // header lines - keep them for the alignment comments.
          headerLines.append(line);
          headerLines.append("\n");
          numHeaderLines++;
        }
      }
      while (!idsFound);
      if (line==null)
        break; // end of file.
      int starCol = line.indexOf("*");
      seqstrings = new StringBuffer[seqs.size()];

      for (int i = 0; i < seqs.size(); i++)
      {
        if (seqstrings[i] == null)
        {
          seqstrings[i] = new StringBuffer();
        }
      }

      try {
        line = nextLine();
        while (line!=null && line.indexOf("*") == -1)
        {
          for (int i = 0; i < seqs.size(); i++)
          {
            if (line.length() > (i + starCol))
            {
              seqstrings[i].append(line.charAt(i + starCol));
            }
          }
          line = nextLine();
        }
      } catch (IOException e) {
        if (iterationCount==0) {
          throw(e); // otherwise we've just run out of iterations.
        } else {
          iterationSkips=0;
        }
      }
      iterationCount++;
    } while (--iterationSkips!=-1);
    
    for (int i = 0; i < seqs.size(); i++)
    {
      Sequence newSeq = (Sequence) seqs.elementAt(i);

      newSeq.setSequence(seqstrings[i].toString());
    }
    if (seqs.size()>0)
    {
      if (headerLines.length()>1+numHeaderLines) // could see if buffer is just whitespace or not. 
        setAlignmentProperty("Comments", headerLines.toString());
      setAlignmentProperty("iteration", ""+iterationCount);
    }
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

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String print(SequenceI[] s)
  {
    StringBuffer out = new StringBuffer();
    /**
     * A general parser for ids. Will look for dbrefs in
     * Uniprot format source|id
     * And also Jalview /start-end
     *
     * @String id Id to be parsed
     */
    int i = 0;
    int max = -1;

    while ( (i < s.length) && (s[i] != null))
    {
      out.append(">" + printId(s[i]));
      if (s[i].getDescription() != null)
      {
        out.append(" " + s[i].getDescription());
      }

      out.append("\n");

      if (s[i].getSequence().length > max)
      {
        max = s[i].getSequence().length;
      }

      i++;
    }

    out.append("* iteration 1\n");

    for (int j = 0; j < max; j++)
    {
      i = 0;

      while ( (i < s.length) && (s[i] != null))
      {
        if (s[i].getSequence().length > j)
        {
          out.append(s[i].getSequenceAsString(j, j + 1));
        }
        else
        {
          out.append("-");
        }

        i++;
      }

      out.append("\n");
    }

    out.append("*\n");

    return out.toString();
  }
}
