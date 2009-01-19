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

import java.awt.*;

import jalview.datamodel.*;
import jalview.schemes.*;
import jalview.util.*;

/**
 *
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AlignSeq
{
  public static final String PEP = "pep";
  public static final String DNA = "dna";
  
  static String[] dna =
      {
    "A", "C", "G", "T", "-"};
    //"C", "T", "A", "G", "-"};
  static String[] pep =
      {
      "A", "R", "N", "D", "C", "Q", "E", "G", "H", "I", "L", "K", "M", "F",
      "P", "S", "T", "W", "Y", "V", "B", "Z", "X", "-"
  };
  int[][] score;
  int[][] E;
  int[][] F;
  int[][] traceback;
  int[] seq1;
  int[] seq2;
  SequenceI s1;
  SequenceI s2;
  public String s1str;
  public String s2str;
  int maxi;
  int maxj;
  int[] aseq1;
  int[] aseq2;
  public String astr1 = "";
  public String astr2 = "";

  /** DOCUMENT ME!! */
  public int seq1start;

  /** DOCUMENT ME!! */
  public int seq1end;

  /** DOCUMENT ME!! */
  public int seq2start;

  /** DOCUMENT ME!! */
  public int seq2end;
  int count;

  /** DOCUMENT ME!! */
  public int maxscore;
  float pid;
  int prev = 0;
  int gapOpen = 120;
  int gapExtend = 20;
  int[][] lookup = ResidueProperties.getBLOSUM62();
  String[] intToStr = pep;
  int defInt = 23;
  StringBuffer output = new StringBuffer();
  String type;

  /**
   * Creates a new AlignSeq object.
   *
   * @param s1 DOCUMENT ME!
   * @param s2 DOCUMENT ME!
   * @param type DOCUMENT ME!
   */
  public AlignSeq(SequenceI s1, SequenceI s2, String type)
  {
    SeqInit(s1, s1.getSequenceAsString(), s2, s2.getSequenceAsString(), type);
  }

  /**
   * Creates a new AlignSeq object.
   *
   * @param s1 DOCUMENT ME!
   * @param s2 DOCUMENT ME!
   * @param type DOCUMENT ME!
   */
  public AlignSeq(SequenceI s1,
                  String string1,
                  SequenceI s2,
                  String string2,
                  String type)
  {
    SeqInit(s1, string1, s2, string2, type);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getMaxScore()
  {
    return maxscore;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getSeq2Start()
  {
    return seq2start;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getSeq2End()
  {
    return seq2end;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getSeq1Start()
  {
    return seq1start;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getSeq1End()
  {
    return seq1end;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getOutput()
  {
    return output.toString();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getAStr1()
  {
    return astr1;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getAStr2()
  {
    return astr2;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int[] getASeq1()
  {
    return aseq1;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int[] getASeq2()
  {
    return aseq2;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceI getS1()
  {
    return s1;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceI getS2()
  {
    return s2;
  }

  /**
   * DOCUMENT ME!
   *
   * @param s1 DOCUMENT ME!
   * @param string1 -  string to align for sequence1
   * @param s2 sequence 2
   * @param string2 -  string to align for sequence2
   * @param type DNA or PEPTIDE
   */
  public void SeqInit(SequenceI s1,
                      String string1,
                      SequenceI s2,
                      String string2,
                      String type)
  {
    this.s1 = s1;
    this.s2 = s2;
    setDefaultParams(type);
    SeqInit(string1, string2);
  }

  public void SeqInit(SequenceI s1,
                      String string1,
                      SequenceI s2,
                      String string2,
                      ScoreMatrix scoreMatrix)
  {
    this.s1 = s1;
    this.s2 = s2;
    setType(scoreMatrix.isDNA() ? AlignSeq.DNA : AlignSeq.PEP);
    lookup = scoreMatrix.getMatrix();
  }

  /**
   * construct score matrix for string1 and string2 (after removing any existing gaps
   * @param string1
   * @param string2
   */
  private void SeqInit(String string1, String string2)
  {
    s1str = extractGaps(jalview.util.Comparison.GapChars, string1);
    s2str = extractGaps(jalview.util.Comparison.GapChars, string2);

    if (s1str.length() == 0 || s2str.length() == 0)
    {
      output.append("ALL GAPS: " +
                    (s1str.length() == 0 ? s1.getName() : " ")
                    + (s2str.length() == 0 ? s2.getName() : ""));
      return;
    }

    //System.out.println("lookuip " + rt.freeMemory() + " "+  rt.totalMemory());
    seq1 = new int[s1str.length()];

    //System.out.println("seq1 " + rt.freeMemory() +" "  + rt.totalMemory());
    seq2 = new int[s2str.length()];

    //System.out.println("seq2 " + rt.freeMemory() + " " + rt.totalMemory());
    score = new int[s1str.length()][s2str.length()];

    //System.out.println("score " + rt.freeMemory() + " " + rt.totalMemory());
    E = new int[s1str.length()][s2str.length()];

    //System.out.println("E " + rt.freeMemory() + " " + rt.totalMemory());
    F = new int[s1str.length()][s2str.length()];
    traceback = new int[s1str.length()][s2str.length()];

    //System.out.println("F " + rt.freeMemory() + " " + rt.totalMemory());
    seq1 = stringToInt(s1str, type);

    //System.out.println("seq1 " + rt.freeMemory() + " " + rt.totalMemory());
    seq2 = stringToInt(s2str, type);

    //System.out.println("Seq2 " + rt.freeMemory() + " " + rt.totalMemory());
    //   long tstart = System.currentTimeMillis();
    //    calcScoreMatrix();
    //long tend = System.currentTimeMillis();
    //System.out.println("Time take to calculate score matrix = " + (tend-tstart) + " ms");
    //   printScoreMatrix(score);
    //System.out.println();
    //printScoreMatrix(traceback);
    //System.out.println();
    //  printScoreMatrix(E);
    //System.out.println();
    ///printScoreMatrix(F);
    //System.out.println();
    // tstart = System.currentTimeMillis();
    //traceAlignment();
    //tend = System.currentTimeMillis();
    //System.out.println("Time take to traceback alignment = " + (tend-tstart) + " ms");
  }

  private void setDefaultParams(String type)
  {
    setType(type);

    if (type.equals(AlignSeq.PEP))
    {
      lookup = ResidueProperties.getDefaultPeptideMatrix();
    }
    else if (type.equals(AlignSeq.DNA))
    {
      lookup = ResidueProperties.getDefaultDnaMatrix();
    }
  }

  private void setType(String type2)
  {
    this.type = type2;
    if (type.equals(AlignSeq.PEP))
    {
      intToStr = pep;
      defInt = 23;
    }
    else if (type.equals(AlignSeq.DNA))
    {
      intToStr = dna;
      defInt = 4;
    }
    else
    {
      output.append("Wrong type = dna or pep only");
      throw new Error("Unknown Type " + type2 +
                      " - dna or pep are the only allowed values.");
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void traceAlignment()
  {
    // Find the maximum score along the rhs or bottom row
    int max = -9999;

    for (int i = 0; i < seq1.length; i++)
    {
      if (score[i][seq2.length - 1] > max)
      {
        max = score[i][seq2.length - 1];
        maxi = i;
        maxj = seq2.length - 1;
      }
    }

    for (int j = 0; j < seq2.length; j++)
    {
      if (score[seq1.length - 1][j] > max)
      {
        max = score[seq1.length - 1][j];
        maxi = seq1.length - 1;
        maxj = j;
      }
    }

    //  System.out.println(maxi + " " + maxj + " " + score[maxi][maxj]);
    int i = maxi;
    int j = maxj;
    int trace;
    maxscore = score[i][j] / 10;

    seq1end = maxi + 1;
    seq2end = maxj + 1;

    aseq1 = new int[seq1.length + seq2.length];
    aseq2 = new int[seq1.length + seq2.length];

    count = (seq1.length + seq2.length) - 1;

    while ( (i > 0) && (j > 0))
    {
      if ( (aseq1[count] != defInt) && (i >= 0))
      {
        aseq1[count] = seq1[i];
        astr1 = s1str.charAt(i) + astr1;
      }

      if ( (aseq2[count] != defInt) && (j > 0))
      {
        aseq2[count] = seq2[j];
        astr2 = s2str.charAt(j) + astr2;
      }

      trace = findTrace(i, j);

      if (trace == 0)
      {
        i--;
        j--;
      }
      else if (trace == 1)
      {
        j--;
        aseq1[count] = defInt;
        astr1 = "-" + astr1.substring(1);
      }
      else if (trace == -1)
      {
        i--;
        aseq2[count] = defInt;
        astr2 = "-" + astr2.substring(1);
      }

      count--;
    }

    seq1start = i + 1;
    seq2start = j + 1;

    if (aseq1[count] != defInt)
    {
      aseq1[count] = seq1[i];
      astr1 = s1str.charAt(i) + astr1;
    }

    if (aseq2[count] != defInt)
    {
      aseq2[count] = seq2[j];
      astr2 = s2str.charAt(j) + astr2;
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void printAlignment(java.io.PrintStream os)
  {
    // TODO: Use original sequence characters rather than re-translated characters in output
    // Find the biggest id length for formatting purposes
    int maxid = s1.getName().length();
    if (s2.getName().length() > maxid)
    {
      maxid = s2.getName().length();
    }

    int len = 72 - maxid - 1;
    int nochunks = ( (aseq1.length - count) / len) + 1;
    pid = 0;

    output.append("Score = " + score[maxi][maxj] + "\n");
    output.append("Length of alignment = " + (aseq1.length - count) + "\n");
    output.append("Sequence ");
    output.append(new Format("%" + maxid + "s").form(s1.getName()));
    output.append(" :  " + s1.getStart() + " - " + s1.getEnd() +
                  " (Sequence length = " +
                  s1str.length() + ")\n");
    output.append("Sequence ");
    output.append(new Format("%" + maxid + "s").form(s2.getName()));
    output.append(" :  " + s2.getStart() + " - " + s2.getEnd() +
                  " (Sequence length = " +
                  s2str.length() + ")\n\n");
    
    for (int j = 0; j < nochunks; j++)
    {
      // Print the first aligned sequence
      output.append(new Format("%" + (maxid) + "s").form(s1.getName()) + " ");

      for (int i = 0; i < len; i++)
      {
        if ( (i + (j * len)) < astr1.length())
        {
          output.append(astr1.charAt(i +
                                              (j * len)));
        }
      }

      output.append("\n");
      output.append(new Format("%" + (maxid) + "s").form(" ") + " ");

      // Print out the matching chars
      for (int i = 0; i < len; i++)
      {
        if ( (i + (j * len)) < astr1.length())
        {
          if (astr1.charAt(i + (j * len))==astr2.charAt(i + (j * len)) &&
              !jalview.util.Comparison.isGap(astr1.charAt(i + (j * len))))
          {
            pid++;
            output.append("|");
          }
          else if (type.equals("pep"))
          {
            if (ResidueProperties.getPAM250(
                    astr1.charAt(i + (j * len)),
                    astr2.charAt(i + (j * len)))>0)
            {
              output.append(".");
            }
            else
            {
              output.append(" ");
            }
          }
          else
          {
            output.append(" ");
          }
        }
      }

      // Now print the second aligned sequence
      output = output.append("\n");
      output = output.append(new Format("%" + (maxid) + "s").form(s2.getName()) +
                             " ");

      for (int i = 0; i < len; i++)
      {
        if ( (i + (j * len)) < astr2.length())
        {
          output.append(astr2.charAt(i + (j * len)));
        }
      }

      output = output.append("\n\n");
    }

    pid = pid / (float) (aseq1.length - count) * 100;
    output = output.append(new Format("Percentage ID = %2.2f\n\n").form(pid));

    try
    {
      os.print(output.toString());
    }
    catch (Exception ex)
    {}
  }

  /**
   * DOCUMENT ME!
   *
   * @param mat DOCUMENT ME!
   */
  public void printScoreMatrix(int[][] mat)
  {
    int n = seq1.length;
    int m = seq2.length;

    for (int i = 0; i < n; i++)
    {
      // Print the top sequence
      if (i == 0)
      {
        Format.print(System.out, "%8s", s2str.substring(0, 1));

        for (int jj = 1; jj < m; jj++)
        {
          Format.print(System.out, "%5s", s2str.substring(jj, jj + 1));
        }

        System.out.println();
      }

      for (int j = 0; j < m; j++)
      {
        if (j == 0)
        {
          Format.print(System.out, "%3s", s1str.substring(i, i + 1));
        }

        Format.print(System.out, "%3d ", mat[i][j] / 10);
      }

      System.out.println();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int findTrace(int i, int j)
  {
    int t = 0;
    int max = score[i - 1][j - 1] + (lookup[seq1[i]][seq2[j]] * 10);

    if (F[i][j] > max)
    {
      max = F[i][j];
      t = -1;
    }
    else if (F[i][j] == max)
    {
      if (prev == -1)
      {
        max = F[i][j];
        t = -1;
      }
    }

    if (E[i][j] >= max)
    {
      max = E[i][j];
      t = 1;
    }
    else if (E[i][j] == max)
    {
      if (prev == 1)
      {
        max = E[i][j];
        t = 1;
      }
    }

    prev = t;

    return t;
  }

  /**
   * DOCUMENT ME!
   */
  public void calcScoreMatrix()
  {
    int n = seq1.length;
    int m = seq2.length;

    // top left hand element
    score[0][0] = lookup[seq1[0]][seq2[0]] * 10;
    E[0][0] = -gapExtend;
    F[0][0] = 0;

    // Calculate the top row first
    for (int j = 1; j < m; j++)
    {
      // What should these values be? 0 maybe
      E[0][j] = max(score[0][j - 1] - gapOpen, E[0][j - 1] - gapExtend);
      F[0][j] = -gapExtend;

      score[0][j] = max(lookup[seq1[0]][seq2[j]] * 10, -gapOpen,
                        -gapExtend);

      traceback[0][j] = 1;
    }

    // Now do the left hand column
    for (int i = 1; i < n; i++)
    {
      E[i][0] = -gapOpen;
      F[i][0] = max(score[i - 1][0] - gapOpen, F[i - 1][0] - gapExtend);

      score[i][0] = max(lookup[seq1[i]][seq2[0]] * 10, E[i][0], F[i][0]);
      traceback[i][0] = -1;
    }

    // Now do all the other rows
    for (int i = 1; i < n; i++)
    {
      for (int j = 1; j < m; j++)
      {
        E[i][j] = max(score[i][j - 1] - gapOpen, E[i][j - 1] -
                      gapExtend);
        F[i][j] = max(score[i - 1][j] - gapOpen, F[i - 1][j] -
                      gapExtend);

        score[i][j] = max(score[i - 1][j - 1] +
                          (lookup[seq1[i]][seq2[j]] * 10), E[i][j], F[i][j]);
        traceback[i][j] = findTrace(i, j);
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param gapChar DOCUMENT ME!
   * @param seq DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public static String extractGaps(String gapChar, String seq)
  {
    StringTokenizer str = new StringTokenizer(seq, gapChar);
    StringBuffer newString = new StringBuffer();

    while (str.hasMoreTokens())
    {
      newString.append(str.nextToken());
    }

    return newString.toString();
  }

  /**
   * DOCUMENT ME!
   *
   * @param i1 DOCUMENT ME!
   * @param i2 DOCUMENT ME!
   * @param i3 DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int max(int i1, int i2, int i3)
  {
    int max = i1;

    if (i2 > i1)
    {
      max = i2;
    }

    if (i3 > max)
    {
      max = i3;
    }

    return max;
  }

  /**
   * DOCUMENT ME!
   *
   * @param i1 DOCUMENT ME!
   * @param i2 DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int max(int i1, int i2)
  {
    int max = i1;

    if (i2 > i1)
    {
      max = i2;
    }

    return max;
  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param type DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int[] stringToInt(String s, String type)
  {
    int[] seq1 = new int[s.length()];

    for (int i = 0; i < s.length(); i++)
    {
      // String ss = s.substring(i, i + 1).toUpperCase();
      char c = s.charAt(i);
      if ('a' <= c && c <= 'z')
      {
        // TO UPPERCASE !!!
        c -= ('a' - 'A');
      }

      try
      {
        if (type.equals("pep"))
        {
          seq1[i] = ResidueProperties.aaIndex[c];
          if (seq1[i] > 23)
          {
            seq1[i] = 23;
          }
        }
        else if (type.equals("dna"))
        {
          seq1[i] = ResidueProperties.nucleotideIndex[c];
          if (seq1[i] > 4)
          {
            seq1[i] = 4;
          }
        }

      }
      catch (Exception e)
      {
        if (type.equals("dna"))
        {
          seq1[i] = 4;
        }
        else
        {
          seq1[i] = 23;
        }
      }
    }

    return seq1;
  }

  /**
   * DOCUMENT ME!
   *
   * @param g DOCUMENT ME!
   * @param mat DOCUMENT ME!
   * @param n DOCUMENT ME!
   * @param m DOCUMENT ME!
   * @param psize DOCUMENT ME!
   */
  public static void displayMatrix(Graphics g, int[][] mat, int n, int m,
                                   int psize)
  {
    int max = -1000;
    int min = 1000;

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < m; j++)
      {
        if (mat[i][j] >= max)
        {
          max = mat[i][j];
        }

        if (mat[i][j] <= min)
        {
          min = mat[i][j];
        }
      }
    }

    System.out.println(max + " " + min);

    for (int i = 0; i < n; i++)
    {
      for (int j = 0; j < m; j++)
      {
        int x = psize * i;
        int y = psize * j;

        //	System.out.println(mat[i][j]);
        float score = (float) (mat[i][j] - min) / (float) (max - min);
        g.setColor(new Color(score, 0, 0));
        g.fillRect(x, y, psize, psize);

        //	System.out.println(x + " " + y + " " + score);
      }
    }
  }
}
