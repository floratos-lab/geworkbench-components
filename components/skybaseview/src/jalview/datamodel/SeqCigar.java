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
package jalview.datamodel;

import jalview.analysis.*;
import jalview.util.*;

public class SeqCigar
    extends CigarSimple
{
  /**
   * start(inclusive) and end(exclusive) of subsequence on refseq
   */
  private int start, end;
  private SequenceI refseq = null;
  /**
   * Reference dataset sequence for the cigar string
   * @return SequenceI
   */
  public SequenceI getRefSeq()
  {
    return refseq;
  }

  /**
   *
   * @return int start index of cigar ops on refSeq
   */
  public int getStart()
  {
    return start;
  }

  /**
   *
   * @return int end index (exclusive) of cigar ops on refSeq
   */
  public int getEnd()
  {
    return end;
  }

  /**
   * Returns sequence as a string with cigar operations applied to it
   * @return String
   */
  public String getSequenceString(char GapChar)
  {
    return (length == 0) ? "" :
        (String) getSequenceAndDeletions(refseq.getSequenceAsString(start, end),
                                         GapChar)[0];
  }

  /**
   * recreates a gapped and edited version of RefSeq or null for an empty cigar string
   * @return SequenceI
   */
  public SequenceI getSeq(char GapChar)
  {
    Sequence seq;
    if (refseq == null || length == 0)
    {
      return null;
    }
    Object[] edit_result = getSequenceAndDeletions(refseq.getSequenceAsString(
        start, end),
        GapChar);
    if (edit_result == null)
    {
      throw new Error(
          "Implementation Error - unexpected null from getSequenceAndDeletions");
    }
    int bounds[] = (int[]) edit_result[1];
    seq = new Sequence(refseq.getName(), (String) edit_result[0],
                       refseq.getStart() + start + bounds[0],
                       refseq.getStart() + start +
                       ( (bounds[2] == 0) ? -1 : bounds[2]));
    // seq.checkValidRange(); probably not needed
    seq.setDatasetSequence(refseq);
    seq.setDescription(refseq.getDescription());
    return seq;
  }

  /*
     We don't allow this - refseq is given at construction time only
   public void setSeq(SequenceI seq) {
    this.seq = seq;
     }
   */
  /**
   * internal constructor - sets seq to a gapless sequence derived from seq
   * and prepends any 'D' operations needed to get to the first residue of seq.
   * @param seq SequenceI
   * @param initialDeletion true to mark initial dataset sequence residues as deleted in subsequence
   * @param _s index of first position in seq
   * @param _e index after last position in (possibly gapped) seq
   * @return true if gaps are present in seq
   */
  private boolean _setSeq(SequenceI seq, boolean initialDeletion, int _s,
                          int _e)
  {
    boolean hasgaps = false;
    if (seq == null)
    {
      throw new Error("Implementation Error - _setSeq(null,...)");
    }
    if (_s < 0)
    {
      throw new Error("Implementation Error: _s=" + _s);
    }
    String seq_string = seq.getSequenceAsString();
    if (_e == 0 || _e < _s || _e > seq_string.length())
    {
      _e = seq_string.length();
    }
    // resolve start and end positions relative to ungapped reference sequence
    start = seq.findPosition(_s) - seq.getStart();
    end = seq.findPosition(_e) - seq.getStart();
    int l_ungapped = end - start;
    // Find correct sequence to reference and correct start and end - if necessary
    SequenceI ds = seq.getDatasetSequence();
    if (ds == null)
    {
      // make a new dataset sequence
      String ungapped = AlignSeq.extractGaps(jalview.util.Comparison.GapChars,
                                             new String(seq_string));
      l_ungapped = ungapped.length();
      // check that we haven't just duplicated an ungapped sequence.
      if (l_ungapped == seq.getLength())
      {
        ds = seq;
      }
      else
      {
        ds = new Sequence(seq.getName(), ungapped,
                          seq.getStart(),
                          seq.getStart() + ungapped.length() - 1);
        // JBPNote: this would be consistent but may not be useful
        //        seq.setDatasetSequence(ds);
      }
    }
    // add in offset between seq and the dataset sequence
    if (ds.getStart() < seq.getStart())
    {
      int offset = seq.getStart() - ds.getStart();
      if (initialDeletion)
      {
        // absolute cigar string
        addDeleted(_s + offset);
        start = 0;
        end += offset;
      }
      else
      {
        // normal behaviour - just mark start and end subsequence
        start += offset;
        end += offset;

      }

    }

    // any gaps to process ?
    if (l_ungapped != (_e - _s))
    {
      hasgaps = true;
    }

    this.refseq = ds;

    // Check  offsets
    if (end > ds.getLength())
    {
      throw new Error("SeqCigar: Possible implementation error: sequence is longer than dataset sequence");
//      end = ds.getLength();
    }

    return hasgaps;
  }

  /**
   * directly initialise a cigar object with a sequence of range, operation pairs and a sequence to apply it to.
   * operation and range should be relative to the seq.getStart()'th residue of the dataset seq resolved from seq.
   * @param seq SequenceI
   * @param operation char[]
   * @param range int[]
   */
  public SeqCigar(SequenceI seq, char operation[], int range[])
  {
    super();
    if (seq == null)
    {
      throw new Error("Implementation Bug. Null seq !");
    }
    if (operation.length != range.length)
    {
      throw new Error("Implementation Bug. Cigar Operation list!= range list");
    }

    if (operation != null)
    {
      this.operation = new char[operation.length + _inc_length];
      this.range = new int[operation.length + _inc_length];

      if (_setSeq(seq, false, 0, 0))
      {
        throw new Error("NOT YET Implemented: Constructing a Cigar object from a cigar string and a gapped sequence.");
      }
      for (int i = this.length, j = 0; j < operation.length; i++, j++)
      {
        char op = operation[j];
        if (op != M && op != I && op != D)
        {
          throw new Error(
              "Implementation Bug. Cigar Operation '" + j + "' '" + op +
              "' not one of '" + M + "', '" + I + "', or '" + D + "'.");
        }
        this.operation[i] = op;
        this.range[i] = range[j];
      }
      this.length += operation.length;
    }
    else
    {
      this.operation = null;
      this.range = null;
      this.length = 0;
      if (_setSeq(seq, false, 0, 0))
      {
        throw new Error("NOT YET Implemented: Constructing a Cigar object from a cigar string and a gapped sequence.");
      }
    }
  }

  /**
   * add range matched residues to cigar string
   * @param range int
   */
  public void addMatch(int range)
  {
    this.addOperation(M, range);
  }

  /**
   * Adds
   * insertion and match operations based on seq to the cigar up to
   * the endpos column of seq.
   *
   * @param cigar CigarBase
   * @param seq SequenceI
   * @param startpos int
   * @param endpos int
   * @param initialDeletions if true then initial deletions will be added from start of seq to startpos
   */
  protected static void addSequenceOps(CigarBase cigar, SequenceI seq,
                                       int startpos, int endpos,
                                       boolean initialDeletions)
  {
    char op = '\0';
    int range = 0;
    int p = 0, res = seq.getLength();

    if (!initialDeletions)
    {
      p = startpos;
    }

    while (p <= endpos)
    {
      boolean isGap = (p < res) ? jalview.util.Comparison.isGap(seq.getCharAt(p)) : true;
      if ( (startpos <= p) && (p <= endpos))
      {
        if (isGap)
        {
          if (range > 0 && op != I)
          {
            cigar.addOperation(op, range);
            range = 0;
          }
          op = I;
          range++;
        }
        else
        {
          if (range > 0 && op != M)
          {
            cigar.addOperation(op, range);
            range = 0;
          }
          op = M;
          range++;
        }
      }
      else
      {
        if (!isGap)
        {
          if (range > 0 && op != D)
          {
            cigar.addOperation(op, range);
            range = 0;
          }
          op = D;
          range++;
        }
        else
        {
          // do nothing - insertions are not made in flanking regions
        }
      }
      p++;
    }
    if (range > 0)
    {
      cigar.addOperation(op, range);
    }
  }

  /**
   * create a cigar string for given sequence
   * @param seq SequenceI
   */
  public SeqCigar(SequenceI seq)
  {
    super();
    if (seq == null)
    {
      throw new Error("Implementation error for new Cigar(SequenceI)");
    }
    _setSeq(seq, false, 0, 0);
    // there is still work to do
    addSequenceOps(this, seq, 0, seq.getLength() - 1, false);
  }

  /**
   * Create Cigar from a range of gaps and residues on a sequence object
   * @param seq SequenceI
   * @param start int - first column in range
   * @param end int - last column in range
   */
  public SeqCigar(SequenceI seq, int start, int end)
  {
    super();
    if (seq == null)
    {
      throw new Error("Implementation error for new Cigar(SequenceI)");
    }
    _setSeq(seq, false, start, end + 1);
    // there is still work to do
    addSequenceOps(this, seq, start, end, false);
  }

  /**
   * Create a cigar object from a cigar string like '[<I|D|M><range>]+'
   * Will fail if the given seq already contains gaps (JBPNote: future implementation will fix)
   * @param seq SequenceI object resolvable to a dataset sequence
   * @param cigarString String
   * @return Cigar
   */
  public static SeqCigar parseCigar(SequenceI seq, String cigarString)
      throws Exception
  {
    Object[] opsandrange = parseCigarString(cigarString);
    return new SeqCigar(seq, (char[]) opsandrange[0], (int[]) opsandrange[1]);
  }

  /**
   * createAlignment
   *
   * @param alseqs SeqCigar[]
   * @param gapCharacter char
   * @return SequenceI[]
   */
  public static SequenceI[] createAlignmentSequences(SeqCigar[] alseqs,
      char gapCharacter, ColumnSelection colsel, int[] segments)
  {
    SequenceI[] seqs = new SequenceI[alseqs.length];
    StringBuffer[] g_seqs = new StringBuffer[alseqs.length];
    String[] alseqs_string = new String[alseqs.length];
    Object[] gs_regions = new Object[alseqs.length];
    for (int i = 0; i < alseqs.length; i++)
    {
      alseqs_string[i] = alseqs[i].getRefSeq().
          getSequenceAsString(alseqs[i].start, alseqs[i].end);
      gs_regions[i] = alseqs[i].getSequenceAndDeletions(alseqs_string[i],
          gapCharacter); // gapped sequence, {start, start col, end. endcol}, hidden regions {{start, end, col}})
      if (gs_regions[i] == null)
      {
        throw new Error("Implementation error: " + i +
                        "'th sequence Cigar has no operations.");
      }
      g_seqs[i] = new StringBuffer( (String) ( (Object[]) gs_regions[i])[0]); // the visible gapped sequence
    }
    // Now account for insertions. (well - deletions)
    // this is complicated because we must keep track of shifted positions in each sequence
    ShiftList shifts = new ShiftList();
    for (int i = 0; i < alseqs.length; i++)
    {
      Object[] gs_region = ( (Object[]) ( (Object[]) gs_regions[i])[2]);
      if (gs_region != null)

      {
        for (int hr = 0; hr < gs_region.length; hr++)
        {
          int[] region = (int[]) gs_region[hr];
          char[] insert = new char[region[1] - region[0] + 1];
          for (int s = 0; s < insert.length; s++)
          {
            insert[s] = gapCharacter;
          }
          int inspos = shifts.shift(region[2]); // resolve insertion position in current alignment frame of reference
          for (int s = 0; s < alseqs.length; s++)
          {
            if (s != i)
            {
              if (g_seqs[s].length() <= inspos)
              {
                // prefix insertion with more gaps.
                for (int l = inspos - g_seqs[s].length(); l > 0; l--)
                {
                  g_seqs[s].append(gapCharacter); // to debug - use a diffferent gap character here
                }
              }
              g_seqs[s].insert(inspos, insert);
            }
            else
            {
              g_seqs[s].insert(inspos,
                               alseqs_string[i].substring(region[0],
                  region[1] + 1));
            }
          }
          shifts.addShift(region[2], insert.length); // update shift in alignment frame of reference
          if (segments == null)
          {
            // add a hidden column for this deletion
            colsel.hideColumns(inspos, inspos + insert.length - 1);
          }
        }
      }
    }
    for (int i = 0; i < alseqs.length; i++)
    {
      int[] bounds = ( (int[]) ( (Object[]) gs_regions[i])[1]);
      SequenceI ref = alseqs[i].getRefSeq();
      seqs[i] = new Sequence(ref.getName(), g_seqs[i].toString(),
                             ref.getStart() + alseqs[i].start + bounds[0],
                             ref.getStart() + alseqs[i].start +
                             (bounds[2] == 0 ? -1 : bounds[2]));
      seqs[i].setDatasetSequence(ref);
      seqs[i].setDescription(ref.getDescription());
    }
    if (segments != null)
    {
      for (int i = 0; i < segments.length; i += 3)
      {
        //int start=shifts.shift(segments[i]-1)+1;
        //int end=shifts.shift(segments[i]+segments[i+1]-1)-1;
        colsel.hideColumns(segments[i + 1],
                           segments[i + 1] + segments[i + 2] - 1);
      }
    }
    return seqs;
  }

  /**
   * non rigorous testing
   */
  /**
   *
   * @param seq Sequence
   * @param ex_cs_gapped String
   * @return String
   */
  public static String testCigar_string(Sequence seq, String ex_cs_gapped)
  {
    SeqCigar c_sgapped = new SeqCigar(seq);
    String cs_gapped = c_sgapped.getCigarstring();
    if (!cs_gapped.equals(ex_cs_gapped))
    {
      System.err.println("Failed getCigarstring: incorect string '" + cs_gapped +
                         "' != " + ex_cs_gapped);
    }
    return cs_gapped;
  }

  public static boolean testSeqRecovery(SeqCigar gen_sgapped,
                                        SequenceI s_gapped)
  {
    // this is non-rigorous - start and end  recovery is not tested.
    SequenceI gen_sgapped_s = gen_sgapped.getSeq('-');
    if (!gen_sgapped_s.getSequence().equals(s_gapped.getSequence()))
    {
      System.err.println("Couldn't reconstruct sequence.\n" +
                         gen_sgapped_s.getSequenceAsString() + "\n" +
                         s_gapped.getSequenceAsString());
      return false;
    }
    return true;
  }

  public static void main(String argv[])
      throws Exception
  {
    String o_seq;
    Sequence s = new Sequence("MySeq",
                              o_seq =
                              "asdfktryasdtqwrtsaslldddptyipqqwaslchvhttt",
                              39, 80);
    String orig_gapped;
    Sequence s_gapped = new Sequence("MySeq",
                                     orig_gapped =
        "----asdf------ktryas---dtqwrtsasll----dddptyipqqwa----slchvhttt",
                                     39, 80);
    String ex_cs_gapped = "4I4M6I6M3I11M4I12M4I9M";
    s_gapped.setDatasetSequence(s);
    String sub_gapped_s;
    Sequence s_subsequence_gapped = new Sequence("MySeq",
                                                 sub_gapped_s =
        "------ktryas---dtqwrtsasll----dddptyipqqwa----slchvh",
                                                 43, 77);

    s_subsequence_gapped.setDatasetSequence(s);
    SeqCigar c_null = new SeqCigar(s);
    String cs_null = c_null.getCigarstring();
    if (!cs_null.equals("42M"))
    {
      System.err.println(
          "Failed to recover ungapped sequence cigar operations:" +
          ( (cs_null == "") ? "empty string" : cs_null));
    }
    testCigar_string(s_gapped, ex_cs_gapped);
    SeqCigar gen_sgapped = SeqCigar.parseCigar(s, ex_cs_gapped);
    if (!gen_sgapped.getCigarstring().equals(ex_cs_gapped))
    {
      System.err.println("Failed parseCigar(" + ex_cs_gapped +
                         ")->getCigarString()->'" + gen_sgapped.getCigarstring() +
                         "'");
    }
    testSeqRecovery(gen_sgapped, s_gapped);
    // Test dataset resolution
    SeqCigar sub_gapped = new SeqCigar(s_subsequence_gapped);
    if (!testSeqRecovery(sub_gapped, s_subsequence_gapped))
    {
      System.err.println("Failed recovery for subsequence of dataset sequence");
    }
    // width functions
    if (sub_gapped.getWidth() != sub_gapped_s.length())
    {
      System.err.println("Failed getWidth()");
    }

    sub_gapped.getFullWidth();
    if (sub_gapped.hasDeletedRegions())
    {
      System.err.println("hasDeletedRegions is incorrect.");
    }
    // Test start-end region SeqCigar
    SeqCigar sub_se_gp = new SeqCigar(s_subsequence_gapped, 8, 48);
    if (sub_se_gp.getWidth() != 41)
    {
      System.err.println(
          "SeqCigar(seq, start, end) not properly clipped alignsequence.");
    }
    System.out.println("Original sequence align:\n" + sub_gapped_s +
                       "\nReconstructed window from 8 to 48\n"
                       + "XXXXXXXX" + sub_se_gp.getSequenceString('-') + "..."
                       + "\nCigar String:" + sub_se_gp.getCigarstring() + "\n"
        );
    SequenceI ssgp = sub_se_gp.getSeq('-');
    System.out.println("\t " + ssgp.getSequenceAsString());
    for (int r = 0; r < 10; r++)
    {
      sub_se_gp = new SeqCigar(s_subsequence_gapped, 8, 48);
      int sl = sub_se_gp.getWidth();
      int st = sl - 1 - r;
      for (int rs = 0; rs < 10; rs++)
      {
        int e = st + rs;
        sub_se_gp.deleteRange(st, e);
        String ssgapedseq = sub_se_gp.getSeq('-').getSequenceAsString();
        System.out.println(st + "," + e + "\t:" + ssgapedseq);
        st -= 3;
      }
    }
    {
      SeqCigar[] set = new SeqCigar[]
          {
          new SeqCigar(s), new SeqCigar(s_subsequence_gapped, 8, 48),
          new SeqCigar(s_gapped)};
      Alignment al = new Alignment(set);
      for (int i = 0; i < al.getHeight(); i++)
      {
        System.out.println("" + al.getSequenceAt(i).getName() + "\t" +
                           al.getSequenceAt(i).getStart() + "\t" +
                           al.getSequenceAt(i).getEnd() + "\t" +
                           al.getSequenceAt(i).getSequenceAsString());
      }
    }
    {
      System.out.println("Gapped.");
      SeqCigar[] set = new SeqCigar[]
          {
          new SeqCigar(s), new SeqCigar(s_subsequence_gapped, 8, 48),
          new SeqCigar(s_gapped)};
      set[0].deleteRange(20, 25);
      Alignment al = new Alignment(set);
      for (int i = 0; i < al.getHeight(); i++)
      {
        System.out.println("" + al.getSequenceAt(i).getName() + "\t" +
                           al.getSequenceAt(i).getStart() + "\t" +
                           al.getSequenceAt(i).getEnd() + "\t" +
                           al.getSequenceAt(i).getSequenceAsString());
      }
    }
//    if (!ssgapedseq.equals("ryas---dtqqwa----slchvh"))
//      System.err.println("Subseqgaped\n------ktryas---dtqwrtsasll----dddptyipqqwa----slchvhryas---dtqwrtsasll--qwa----slchvh\n"+ssgapedseq+"\n"+sub_se_gp.getCigarstring());
  }

}
