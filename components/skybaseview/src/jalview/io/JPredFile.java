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

/**
 * PredFile.java
 * JalviewX / Vamsas Project
 * JPred.seq.concise reader
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
public class JPredFile
    extends AlignFile
{
  Vector ids;
  Vector conf;
  Hashtable Scores; // Hash of names and score vectors
  Hashtable Symscores; // indexes of symbol annotation properties in sequenceI vector
  private int QuerySeqPosition;

  /**
   * Creates a new JPredFile object.
   *
   * @param inFile DOCUMENT ME!
   * @param type DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public JPredFile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  /**
   * DOCUMENT ME!
   *
   * @param QuerySeqPosition DOCUMENT ME!
   */
  public void setQuerySeqPosition(int QuerySeqPosition)
  {
    this.QuerySeqPosition = QuerySeqPosition;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getQuerySeqPosition()
  {
    return QuerySeqPosition;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Hashtable getScores()
  {
    return Scores;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Hashtable getSymscores()
  {
    return Symscores;
  }

  /**
   * DOCUMENT ME!
   */
  public void initData()
  {
    super.initData();
    Scores = new Hashtable();
    ids = null;
    conf = null;
  }

  /**
   * parse a JPred concise file into a sequence-alignment like object.
   */
  public void parse()
      throws IOException
  {
    // JBPNote log.System.out.println("all read in ");
    String line;
    QuerySeqPosition = -1;
    noSeqs = 0;

    Vector seq_entries = new Vector();
    Vector ids = new Vector();
    Hashtable Symscores = new Hashtable();

    while ( (line = nextLine()) != null)
    {
      // Concise format allows no comments or non comma-formatted data
      StringTokenizer str = new StringTokenizer(line, ":");
      String id = "";

      if (!str.hasMoreTokens())
      {
        continue;
      }

      id = str.nextToken();

      String seqsym = str.nextToken();
      StringTokenizer symbols = new StringTokenizer(seqsym, ",");

      // decide if we have more than just alphanumeric symbols
      int numSymbols = symbols.countTokens();

      if (numSymbols == 0)
      {
        continue;
      }

      if (seqsym.length() != (2 * numSymbols))
      {
        // Set of scalars for some property
        if (Scores.containsKey(id))
        {
          int i = 1;

          while (Scores.containsKey(id + "_" + i))
          {
            i++;
          }

          id = id + "_" + i;
        }

        Vector scores = new Vector();

        // Typecheck from first entry
        int i = 0;
        String ascore = "dead";

        try
        {
          // store elements as floats...
          while (symbols.hasMoreTokens())
          {
            ascore = symbols.nextToken();

            Float score = new Float(ascore);
            scores.addElement( (Object) score);
          }

          Scores.put(id, scores);
        }
        catch (Exception e)
        {
          // or just keep them as strings
          i = scores.size();

          for (int j = 0; j < i; j++)
          {
            scores.setElementAt(
                (Object) ( (Float) scores.elementAt(j)).toString(), j);
          }

          scores.addElement( (Object) ascore);

          while (symbols.hasMoreTokens())
          {
            ascore = symbols.nextToken();
            scores.addElement( (Object) ascore);
          }

          Scores.put(id, scores);
        }
      }
      else if (id.equals("jnetconf"))
      {
        // log.debug System.out.println("here");
        id = "Prediction Confidence";
        this.conf = new Vector(numSymbols);

        for (int i = 0; i < numSymbols; i++)
        {
          conf.setElementAt(symbols.nextToken(), i);
        }
      }
      else
      {
        // Sequence or a prediction string (rendered as sequence)
        StringBuffer newseq = new StringBuffer();

        for (int i = 0; i < numSymbols; i++)
        {
          newseq.append(symbols.nextToken());
        }

        if (id.indexOf(";") > -1)
        {
          seq_entries.addElement(newseq);

          int i = 1;
          String name = id.substring(id.indexOf(";") + 1);

          while (ids.lastIndexOf(name) > -1)
          {
            name = id.substring(id.indexOf(";") + 1) + "_" + ++i;
          }

          ids.addElement(name);

          noSeqs++;
        }
        else
        {
          if (id.equals("JNETPRED"))
          {
            id = "Predicted Secondary Structure";
          }

          seq_entries.addElement(newseq.toString());
          ids.addElement(id);
          Symscores.put( (Object) id,
                        (Object)new Integer(ids.size() - 1));
        }
      }
    }
    /* leave it to the parser user to actually check this.
             if (noSeqs < 1)
             {
        throw new IOException(
            "JpredFile Parser: No sequence in the prediction!");
             }*/

    maxLength = seq_entries.elementAt(0).toString().length();

    for (int i = 0; i < ids.size(); i++)
    {
      // Add all sequence like objects
      Sequence newSeq = new Sequence(ids.elementAt(i).toString(),
                                     seq_entries.elementAt(i).toString(), 1,
                                     seq_entries.elementAt(i).toString().length());

      if (maxLength != seq_entries.elementAt(i).toString().length())
      {
        throw new IOException("JPredConcise: Entry (" +
                              ids.elementAt(i).toString() +
                              ") has an unexpected number of columns");
      }

      if (newSeq.getName().startsWith("QUERY") &&
          (QuerySeqPosition == -1))
      {
        QuerySeqPosition = seqs.size();
      }

      seqs.addElement(newSeq);
    }
  }

  /**
   * print
   *
   * @return String
   */
  public String print()
  {
    return "Not Supported";
  }

  /**
   * DOCUMENT ME!
   *
   * @param args DOCUMENT ME!
   */
  public static void main(String[] args)
  {
    try
    {
      JPredFile blc = new JPredFile(args[0], "File");

      for (int i = 0; i < blc.seqs.size(); i++)
      {
        System.out.println( ( (Sequence) blc.seqs.elementAt(i)).getName() +
                           "\n" +
                           ( (Sequence) blc.seqs.elementAt(i)).getSequenceAsString() +
                           "\n");
      }
    }
    catch (java.io.IOException e)
    {
      System.err.println("Exception " + e);
      e.printStackTrace();
    }
  }

  Vector annotSeqs = null;
  /**
   * removeNonSequences
   */
  public void removeNonSequences()
  {
    if (annotSeqs != null)
    {
      return;
    }
    annotSeqs = new Vector();
    Vector newseqs = new Vector();
    int i = 0;
    int j = seqs.size();
    for (; i < QuerySeqPosition; i++)
    {
      annotSeqs.addElement(seqs.elementAt(i));
    }
    // check that no stray annotations have been added at the end.
    {
      SequenceI sq = (SequenceI) seqs.elementAt(j - 1);
      if (sq.getName().toUpperCase().startsWith("JPRED"))
      {
        annotSeqs.addElement(sq);
        seqs.removeElementAt(--j);
      }
    }
    for (; i < j; i++)
    {
      newseqs.addElement(seqs.elementAt(i));
    }

    seqs.removeAllElements();
    seqs = newseqs;
  }
}

/*
 StringBuffer out = new StringBuffer();

 out.append("START PRED\n");
 for (int i = 0; i < s[0].sequence.length(); i++)
 {
  out.append(s[0].sequence.substring(i, i + 1) + " ");
  out.append(s[1].sequence.substring(i, i + 1) + " ");
  out.append(s[1].score[0].elementAt(i) + " ");
  out.append(s[1].score[1].elementAt(i) + " ");
  out.append(s[1].score[2].elementAt(i) + " ");
  out.append(s[1].score[3].elementAt(i) + " ");

  out.append("\n");
 }
 out.append("END PRED\n");
 return out.toString();
 }

    public static void main(String[] args)
 {
  try
  {
    BLCFile blc = new BLCFile(args[0], "File");
    DrawableSequence[] s = new DrawableSequence[blc.seqs.size()];
    for (int i = 0; i < blc.seqs.size(); i++)
    {
      s[i] = new DrawableSequence( (Sequence) blc.seqs.elementAt(i));
    }
    String out = BLCFile.print(s);

    AlignFrame af = new AlignFrame(null, s);
    af.resize(700, 500);
    af.show();
    System.out.println(out);
  }
  catch (java.io.IOException e)
  {
    System.out.println("Exception " + e);
  }
 }

 }
 */
