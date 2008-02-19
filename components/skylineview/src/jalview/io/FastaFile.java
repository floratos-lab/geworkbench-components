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

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class FastaFile
    extends AlignFile
{
  /**
   * Length of a sequence line
   */
  int len = 72;

  StringBuffer out;

  /**
   * Creates a new FastaFile object.
   */
  public FastaFile()
  {
  }

  /**
   * Creates a new FastaFile object.
   *
   * @param inFile DOCUMENT ME!
   * @param type DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public FastaFile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  /**
   * DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public void parse()
      throws IOException
  {
    StringBuffer sb = new StringBuffer();
    boolean firstLine = true;

    String line;
    Sequence seq = null;

    boolean annotation = false;

    while ( (line = nextLine()) != null)
    {
      line = line.trim();
      if (line.length() > 0)
      {
        if (line.charAt(0) == '>')
        {
          if (line.startsWith(">#_"))
          {
            if (annotation)
            {
              Annotation[] anots = new Annotation[sb.length()];
              String anotString = sb.toString();
              for (int i = 0; i < sb.length(); i++)
              {
                anots[i] = new Annotation(anotString.substring(i, i + 1),
                                          null,
                                          ' ', 0);
              }
              AlignmentAnnotation aa = new AlignmentAnnotation(
                  seq.getName().substring(2), seq.getDescription(),
                  anots);

              annotations.addElement(aa);
            }
          }
          else
          {
            annotation = false;
          }

          if (!firstLine)
          {
            seq.setSequence(sb.toString());

            if (!annotation)
            {
              seqs.addElement(seq);
            }
          }

          seq = parseId(line.substring(1));
          firstLine = false;

          sb = new StringBuffer();

          if (line.startsWith(">#_"))
          {
            annotation = true;
          }
        }
        else
        {
          sb.append(line);
        }
      }
    }

    if (annotation)
    {
      Annotation[] anots = new Annotation[sb.length()];
      String anotString = sb.toString();
      for (int i = 0; i < sb.length(); i++)
      {
        anots[i] = new Annotation(anotString.substring(i, i + 1),
                                  null,
                                  ' ', 0);
      }
      AlignmentAnnotation aa = new AlignmentAnnotation(
          seq.getName().substring(2), seq.getDescription(),
          anots);

      annotations.addElement(aa);
    }

    else if (!firstLine)
    {
      seq.setSequence(sb.toString());
      seqs.addElement(seq);
    }
  }

  /**
   * called by AppletFormatAdapter to generate
   * an annotated alignment, rather than bare
   * sequences.
   * @param al
   */
  public void addAnnotations(Alignment al)
  {
    addProperties(al);
    for (int i = 0; i < annotations.size(); i++)
    {
      AlignmentAnnotation aa = (AlignmentAnnotation) annotations.elementAt(i);
      aa.setPadGaps(true, al.getGapCharacter());
      al.addAnnotation( aa );
    }
  }


  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param len DOCUMENT ME!
   * @param gaps DOCUMENT ME!
   * @param displayId DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String print(SequenceI[] s)
  {
    out = new StringBuffer();
    int i = 0;

    while ( (i < s.length) && (s[i] != null))
    {
      out.append(">" + printId(s[i]));
      if (s[i].getDescription() != null)
      {
        out.append(" " + s[i].getDescription());
      }

      out.append("\n");

      int nochunks = (s[i].getLength() / len) + 1;

      for (int j = 0; j < nochunks; j++)
      {
        int start = j * len;
        int end = start + len;

        if (end < s[i].getLength())
        {
          out.append(s[i].getSequenceAsString(start, end) + "\n");
        }
        else if (start < s[i].getLength())
        {
          out.append(s[i].getSequenceAsString(start, s[i].getLength()) + "\n");
        }
      }

      i++;
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
