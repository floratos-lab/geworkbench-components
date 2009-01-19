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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class MSFfile
    extends AlignFile
{

  /**
   * Creates a new MSFfile object.
   */
  public MSFfile()
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
  public MSFfile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  /**
   * DOCUMENT ME!
   */
  public void parse()
      throws IOException
  {
    int i = 0;
    boolean seqFlag = false;
    String key = new String();
    Vector headers = new Vector();
    Hashtable seqhash = new Hashtable();
    String line;

    try
    {
      while ( (line = nextLine()) != null)
      {
        StringTokenizer str = new StringTokenizer(line);

        while (str.hasMoreTokens())
        {
          String inStr = str.nextToken();

          //If line has header information add to the headers vector
          if (inStr.indexOf("Name:") != -1)
          {
            key = str.nextToken();
            headers.addElement(key);
          }

          //if line has // set SeqFlag to 1 so we know sequences are coming
          if (inStr.indexOf("//") != -1)
          {
            seqFlag = true;
          }

          //Process lines as sequence lines if seqFlag is set
          if ( (inStr.indexOf("//") == -1) && (seqFlag == true))
          {
            //seqeunce id is the first field
            key = inStr;

            StringBuffer tempseq;

            //Get sequence from hash if it exists
            if (seqhash.containsKey(key))
            {
              tempseq = (StringBuffer) seqhash.get(key);
            }
            else
            {
              tempseq = new StringBuffer();
              seqhash.put(key, tempseq);
            }

            //loop through the rest of the words
            while (str.hasMoreTokens())
            {
              //append the word to the sequence
              tempseq.append(str.nextToken());
            }
          }
        }
      }
    }
    catch (IOException e)
    {
      System.err.println("Exception parsing MSFFile " + e);
      e.printStackTrace();
    }

    this.noSeqs = headers.size();

    //Add sequences to the hash
    for (i = 0; i < headers.size(); i++)
    {
      if (seqhash.get(headers.elementAt(i)) != null)
      {
        String head = headers.elementAt(i).toString();
        String seq = seqhash.get(head).toString();

        if (maxLength < head.length())
        {
          maxLength = head.length();
        }

        // Replace ~ with a sensible gap character
        seq = seq.replace('~', '-');

        Sequence newSeq = parseId(head);

        newSeq.setSequence(seq);

        seqs.addElement(newSeq);
      }
      else
      {
        System.err.println("MSFFile Parser: Can't find sequence for " +
                           headers.elementAt(i));
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param seq DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int checkSum(String seq)
  {
    int check = 0;
    String sequence = seq.toUpperCase();

    for (int i = 0; i < sequence.length(); i++)
    {
      try
      {

        int value = sequence.charAt(i);
        if (value != -1)
        {
          check += (i % 57 + 1) * value;
        }
      }
      catch (Exception e)
      {
        System.err.println("Exception during MSF Checksum calculation");
        e.printStackTrace();
      }
    }

    return check % 10000;
  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param is_NA DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String print(SequenceI[] seqs)
  {

    boolean is_NA = jalview.util.Comparison.isNucleotide(seqs);

    SequenceI[] s = new SequenceI[seqs.length];

    StringBuffer out = new StringBuffer("!!" + (is_NA ? "NA" : "AA") +
                                        "_MULTIPLE_ALIGNMENT 1.0\n\n"); // TODO: JBPNote : Jalview doesn't remember NA or AA yet.

    int max = 0;
    int maxid = 0;
    int i = 0;

    while ( (i < seqs.length) && (seqs[i] != null))
    {
      // Replace all internal gaps with . and external spaces with ~
      s[i] = new Sequence(seqs[i].getName(),
                          seqs[i].getSequenceAsString().replace('-', '.'));

      StringBuffer sb = new StringBuffer();
      sb.append(s[i].getSequence());

      for (int ii = 0; ii < sb.length(); ii++)
      {
        if (sb.charAt(ii) == '.')
        {
          sb.setCharAt(ii, '~');
        }
        else
        {
          break;
        }
      }

      for (int ii = sb.length() - 1; ii > 0; ii--)
      {
        if (sb.charAt(ii) == '.')
        {
          sb.setCharAt(ii, '~');
        }
        else
        {
          break;
        }
      }

      s[i].setSequence(sb.toString());

      if (s[i].getSequence().length > max)
      {
        max = s[i].getSequence().length;
      }

      i++;
    }

    Format maxLenpad = new Format("%" + (new String("" + max)).length() +
                                  "d");
    Format maxChkpad = new Format("%" + (new String("1" + max)).length() +
                                  "d");
    i = 0;

    int bigChecksum = 0;
    int[] checksums = new int[s.length];
    while (i < s.length)
    {
      checksums[i] = checkSum(s[i].getSequenceAsString());
      bigChecksum += checksums[i];
      i++;
    }

    long maxNB = 0;
    out.append("   MSF: " + s[0].getSequence().length + "   Type: " +
               (is_NA ? "N" : "P") + "    Check:  " + (bigChecksum % 10000) +
               "   ..\n\n\n");

    String[] nameBlock = new String[s.length];
    String[] idBlock = new String[s.length];

    i = 0;
    while ( (i < s.length) && (s[i] != null))
    {

      nameBlock[i] = new String("  Name: " + printId(s[i]) + " ");

      idBlock[i] = new String("Len: " +
                              maxLenpad.form(s[i].getSequence().length) +
                              "  Check: " +
                              maxChkpad.form(checksums[i]) + "  Weight: 1.00\n");

      if (s[i].getName().length() > maxid)
      {
        maxid = s[i].getName().length();
      }

      if (nameBlock[i].length() > maxNB)
      {
        maxNB = nameBlock[i].length();
      }

      i++;
    }

    if (maxid < 10)
    {
      maxid = 10;
    }

    if (maxNB < 15)
    {
      maxNB = 15;
    }

    Format nbFormat = new Format("%-" + maxNB + "s");

    for (i = 0; (i < s.length) && (s[i] != null); i++)
    {
      out.append(nbFormat.form(nameBlock[i]) + idBlock[i]);
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

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String print()
  {
    return print(getSeqsAsArray());
  }
}
