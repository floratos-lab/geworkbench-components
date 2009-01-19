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

public class PIRFile
    extends AlignFile
{
  public static boolean useModellerOutput = false;

  Vector words = new Vector(); //Stores the words in a line after splitting

  public PIRFile()
  {
  }

  public PIRFile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  public void parse()
      throws IOException
  {
    StringBuffer sequence;
    String line = null;
    ModellerDescription md;

    while ( (line = nextLine()) != null)
    {
      if (line.length() == 0)
      {
        //System.out.println("blank line");
        continue;
      }
      if (line.indexOf("C;") == 0 || line.indexOf("#") == 0)
      {
        continue;
      }
      Sequence newSeq = parseId(line.substring(line.indexOf(";") + 1));

      sequence = new StringBuffer();

      newSeq.setDescription(nextLine()); // this is the title line

      boolean starFound = false;

      while (!starFound)
      {
        line = nextLine();
        sequence.append(line);

        if (line == null)
        {
          break;
        }

        if (line.indexOf("*") > -1)
        {
          starFound = true;
        }
      }

      if (sequence.length() > 0)
      {
        sequence.setLength(sequence.length() - 1);
        newSeq.setSequence(sequence.toString());

        seqs.addElement(newSeq);

        md = new ModellerDescription(newSeq.
                                     getDescription());
        md.updateSequenceI(newSeq);
      }
    }
  }

  public String print()
  {
    return print(getSeqsAsArray());
  }

  public String print(SequenceI[] s)
  {
    boolean is_NA = jalview.util.Comparison.isNucleotide(s);
    int len = 72;
    StringBuffer out = new StringBuffer();
    int i = 0;
    ModellerDescription md;

    while ( (i < s.length) && (s[i] != null))
    {
      String seq = s[i].getSequenceAsString();
      seq = seq + "*";

      if (is_NA)
      {
        // modeller doesn't really do nucleotides, so we don't do anything fancy
        // Official tags area as follows, for now we'll use P1 and DL
        // Protein (complete) P1
        // Protein (fragment) F1
        // DNA (linear) Dl
        // DNA (circular) DC
        // RNA (linear) RL
        // RNA (circular) RC
        // tRNA N3
        // other functional RNA N1

        out.append(">N1;" + s[i].getName() + "\n");
        if (s[i].getDescription() == null)
        {
          out.append(s[i].getName() + " " +
                     (s[i].getEnd() - s[i].getStart() + 1));
          out.append(is_NA ? " bases\n" : " residues\n");
        }
        else
        {
          out.append(s[i].getDescription() + "\n");
        }
      }
      else
      {

        if (useModellerOutput)
        {
          out.append(">P1;" + s[i].getName() + "\n");
          md = new ModellerDescription(s[i]);
          out.append(md.getDescriptionLine() + "\n");
        }
        else
        {
          out.append(">P1;" + printId(s[i]) + "\n");
          if (s[i].getDescription() != null)
          {
            out.append(s[i].getDescription() + "\n");
          }
          else
          {
            out.append(s[i].getName() + " "
                       + (s[i].getEnd() - s[i].getStart() + 1)
                       + " residues\n");
          }
        }
      }
      int nochunks = (seq.length() / len) + 1;

      for (int j = 0; j < nochunks; j++)
      {
        int start = j * len;
        int end = start + len;

        if (end < seq.length())
        {
          out.append(seq.substring(start, end) + "\n");
        }
        else if (start < seq.length())
        {
          out.append(seq.substring(start) + "\n");
        }
      }

      i++;
    }

    return out.toString();
  }

}
