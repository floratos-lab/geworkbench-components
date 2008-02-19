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

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: Dundee University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AlignmentView
{
  /**
   * Transient object compactly representing a 'view' of an alignment - with discontinuities marked.
   */
  private SeqCigar[] sequences = null;
  private int[] contigs = null;
  private int width = 0;
  private int firstCol = 0;
  public AlignmentView(CigarArray seqcigararray)
  {
    if (!seqcigararray.isSeqCigarArray())
    {
      throw new Error("Implementation Error - can only make an alignment view from a CigarArray of sequences.");
    }
    //contigs = seqcigararray.applyDeletions();
    contigs = seqcigararray.getDeletedRegions();
    sequences = seqcigararray.getSeqCigarArray();
    width = seqcigararray.getWidth(); // visible width
  }

  /**
   * Create an alignmentView where the first column corresponds with the 'firstcol' column of some reference alignment
   * @param sdata
   * @param firstcol
   */
  public AlignmentView(CigarArray sdata, int firstcol)
  {
    this(sdata);
    firstCol = firstcol;
  }

  public void setSequences(SeqCigar[] sequences)
  {
    this.sequences = sequences;
  }

  public void setContigs(int[] contigs)
  {
    this.contigs = contigs;
  }

  public SeqCigar[] getSequences()
  {
    return sequences;
  }

  /**
   * @see CigarArray.getDeletedRegions
   * @return int[] { vis_start, sym_start, length }
   */
  public int[] getContigs()
  {
    return contigs;
  }

  /**
   * get the full alignment and a columnselection object marking the hidden regions
   * @param gapCharacter char
   * @return Object[] { SequenceI[], ColumnSelection}
   */
  public Object[] getAlignmentAndColumnSelection(char gapCharacter)
  {
    ColumnSelection colsel = new ColumnSelection();

    return new Object[]
        {
        SeqCigar.createAlignmentSequences(sequences, gapCharacter, colsel,
                                          contigs), colsel};
  }

  /**
   * getSequenceStrings
   *
   * @param c char
   * @return String[]
   */
  public String[] getSequenceStrings(char c)
  {
    String[] seqs = new String[sequences.length];
    for (int n = 0; n < sequences.length; n++)
    {
      String fullseq = sequences[n].getSequenceString(c);
      if (contigs != null)
      {
        seqs[n] = "";
        int p = 0;
        for (int h = 0; h < contigs.length; h += 3)
        {
          seqs[n] += fullseq.substring(p, contigs[h + 1]);
          p = contigs[h + 1] + contigs[h + 2];
        }
        seqs[n] += fullseq.substring(p);
      }
      else
      {
        seqs[n] = fullseq;
      }
    }
    return seqs;
  }

  /**
   *
   * @return visible number of columns in alignment view
   */
  public int getWidth()
  {
    return width;
  }

  protected void setWidth(int width)
  {
    this.width = width;
  }

  /**
   * get the contiguous subalignments in an alignment view.
   * @param gapCharacter char
   * @return SequenceI[][]
   */
  public SequenceI[][] getVisibleContigs(char gapCharacter)
  {
    SequenceI[][] smsa;
    int njobs = 1;
    if (sequences == null || width <= 0)
    {
      return null;
    }
    if (contigs != null && contigs.length > 0)
    {
      int start = 0;
      njobs = 0;
      int fwidth = width;
      for (int contig = 0; contig < contigs.length; contig += 3)
      {
        if ( (contigs[contig + 1] - start) > 0)
        {
          njobs++;
        }
        fwidth += contigs[contig + 2]; // end up with full region width (including hidden regions)
        start = contigs[contig + 1] + contigs[contig + 2];
      }
      if (start < fwidth)
      {
        njobs++;
      }
      smsa = new SequenceI[njobs][];
      start = 0;
      int j = 0;
      for (int contig = 0; contig < contigs.length; contig += 3)
      {
        if (contigs[contig + 1] - start > 0)
        {
          SequenceI mseq[] = new SequenceI[sequences.length];
          for (int s = 0; s < mseq.length; s++)
          {
            mseq[s] = sequences[s].getSeq(gapCharacter).getSubSequence(start,
                contigs[contig + 1]);
          }
          smsa[j] = mseq;
          j++;
        }
        start = contigs[contig + 1] + contigs[contig + 2];
      }
      if (start < fwidth)
      {
        SequenceI mseq[] = new SequenceI[sequences.length];
        for (int s = 0; s < mseq.length; s++)
        {
          mseq[s] = sequences[s].getSeq(gapCharacter).getSubSequence(start,
              fwidth + 1);
        }
        smsa[j] = mseq;
        j++;
      }
    }
    else
    {
      smsa = new SequenceI[1][];
      smsa[0] = new SequenceI[sequences.length];
      for (int s = 0; s < sequences.length; s++)
      {
        smsa[0][s] = sequences[s].getSeq(gapCharacter);
      }
    }
    return smsa;
  }

  /**
   * return full msa and hidden regions with visible blocks replaced with new sub alignments
   * @param nvismsa SequenceI[][]
   * @param orders AlignmentOrder[] corresponding to each SequenceI[] block.
   * @return Object[]
   */
  public Object[] getUpdatedView(SequenceI[][] nvismsa, AlignmentOrder[] orders,
                                 char gapCharacter)
  {
    if (sequences == null || width <= 0)
    {
      throw new Error("empty view cannot be updated.");
    }
    if (nvismsa == null)
    {
      throw new Error(
          "nvismsa==null. use getAlignmentAndColumnSelection() instead.");
    }
    if (contigs != null && contigs.length > 0)
    {
      SequenceI[] alignment = new SequenceI[sequences.length];
      ColumnSelection columnselection = new ColumnSelection();
      if (contigs != null && contigs.length > 0)
      {
        int start = 0;
        int nwidth = 0;
        int owidth = width;
        int j = 0;
        for (int contig = 0; contig < contigs.length; contig += 3)
        {
          owidth += contigs[contig + 2]; // recover final column width
          if (contigs[contig + 1] - start > 0)
          {
            int swidth = 0; // subalignment width
            if (nvismsa[j] != null)
            {
              SequenceI mseq[] = nvismsa[j];
              AlignmentOrder order = (orders == null) ? null : orders[j];
              j++;
              if (mseq.length != sequences.length)
              {
                throw new Error(
                    "Mismatch between number of sequences in block " + j + " (" +
                    mseq.length + ") and the original view (" +
                    sequences.length + ")");
              }
              swidth = mseq[0].getLength(); // JBPNote: could ensure padded here.
              for (int s = 0; s < mseq.length; s++)
              {
                if (alignment[s] == null)
                {
                  alignment[s] = mseq[s];
                }
                else
                {
                  alignment[s].setSequence(alignment[s].getSequenceAsString() +
                                           mseq[s].getSequenceAsString());
                  if (mseq[s].getStart() <= mseq[s].getEnd())
                  {
                    alignment[s].setEnd(mseq[s].getEnd());
                  }
                  if (order != null)
                  {
                    order.updateSequence(mseq[s], alignment[s]);
                  }
                }
              }
            }
            else
            {
              // recover original alignment block or place gaps
              if (true)
              {
                // recover input data
                for (int s = 0; s < sequences.length; s++)
                {
                  SequenceI oseq = sequences[s].getSeq(gapCharacter).
                      getSubSequence(start,
                                     contigs[contig + 1]);
                  if (swidth < oseq.getLength())
                  {
                    swidth = oseq.getLength();
                  }
                  if (alignment[s] == null)
                  {
                    alignment[s] = oseq;
                  }
                  else
                  {
                    alignment[s].setSequence(alignment[s].getSequenceAsString() +
                                             oseq.getSequenceAsString());
                    if (oseq.getEnd() >= oseq.getStart())
                    {
                      alignment[s].setEnd(oseq.getEnd());
                    }
                  }
                }

              }
              j++;
            }
            nwidth += swidth;
          }
          // advance to begining of visible region
          start = contigs[contig + 1] + contigs[contig + 2];
          // add hidden segment to right of next region
          for (int s = 0; s < sequences.length; s++)
          {
            SequenceI hseq = sequences[s].getSeq(gapCharacter).getSubSequence(
                contigs[contig +
                1], start);
            if (alignment[s] == null)
            {
              alignment[s] = hseq;
            }
            else
            {
              alignment[s].setSequence(alignment[s].getSequenceAsString() +
                                       hseq.getSequenceAsString());
              if (hseq.getEnd() >= hseq.getStart())
              {
                alignment[s].setEnd(hseq.getEnd());
              }
            }
          }
          // mark hidden segment as hidden in the new alignment
          columnselection.hideColumns(nwidth, nwidth + contigs[contig + 2] - 1);
          nwidth += contigs[contig + 2];
        }
        // Do final segment - if it exists
        if (j < nvismsa.length)
        {
          int swidth = 0;
          if (nvismsa[j] != null)
          {
            SequenceI mseq[] = nvismsa[j];
            AlignmentOrder order = (orders != null) ? orders[j] : null;
            swidth = mseq[0].getLength();
            for (int s = 0; s < mseq.length; s++)
            {
              if (alignment[s] == null)
              {
                alignment[s] = mseq[s];
              }
              else
              {
                alignment[s].setSequence(alignment[s].getSequenceAsString() +
                                         mseq[s].getSequenceAsString());
                if (mseq[s].getEnd() >= mseq[s].getStart())
                {
                  alignment[s].setEnd(mseq[s].getEnd());
                }
                if (order != null)
                {
                  order.updateSequence(mseq[s], alignment[s]);
                }
              }
            }
          }
          else
          {
            if (start < owidth)
            {
              // recover input data or place gaps
              if (true)
              {
                // recover input data
                for (int s = 0; s < sequences.length; s++)
                {
                  SequenceI oseq = sequences[s].getSeq(gapCharacter).
                      getSubSequence(start,
                                     owidth + 1);
                  if (swidth < oseq.getLength())
                  {
                    swidth = oseq.getLength();
                  }
                  if (alignment[s] == null)
                  {
                    alignment[s] = oseq;
                  }
                  else
                  {
                    alignment[s].setSequence(alignment[s].getSequenceAsString() +
                                             oseq.getSequenceAsString());
                    if (oseq.getEnd() >= oseq.getStart())
                    {
                      alignment[s].setEnd(oseq.getEnd());
                    }
                  }
                }
                nwidth += swidth;
              }
              else
              {
                // place gaps.
                throw new Error("Padding not yet implemented.");
              }
            }
          }
        }
      }
      return new Object[]
          {
          alignment, columnselection};
    }
    else
    {
      if (nvismsa.length != 1)
      {
        throw new Error("Mismatch between visible blocks to update and number of contigs in view (contigs=0,blocks=" +
                        nvismsa.length);
      }
      if (nvismsa[0] != null)
      {
        return new Object[]
            {
            nvismsa[0], new ColumnSelection()};
      }
      else
      {
        return getAlignmentAndColumnSelection(gapCharacter);
      }
    }
  }

  /**
   * returns simple array of start end positions of visible range on alignment.
   * vis_start and vis_end are inclusive - use SequenceI.getSubSequence(vis_start, vis_end+1) to recover visible sequence from underlying alignment.
   * @return int[] { start_i, end_i } for 1<i<n visible regions.
   */
  public int[] getVisibleContigs()
  {
    if (contigs != null && contigs.length > 0)
    {
      int start = 0;
      int nvis = 0;
      int fwidth = width;
      for (int contig = 0; contig < contigs.length; contig += 3)
      {
        if ( (contigs[contig + 1] - start) > 0)
        {
          nvis++;
        }
        fwidth += contigs[contig + 2]; // end up with full region width (including hidden regions)
        start = contigs[contig + 1] + contigs[contig + 2];
      }
      if (start < fwidth)
      {
        nvis++;
      }
      int viscontigs[] = new int[nvis * 2];
      nvis = 0;
      start = 0;
      for (int contig = 0; contig < contigs.length; contig += 3)
      {
        if ( (contigs[contig + 1] - start) > 0)
        {
          viscontigs[nvis] = start;
          viscontigs[nvis + 1] = contigs[contig + 1] - 1; // end is inclusive
          nvis += 2;
        }
        start = contigs[contig + 1] + contigs[contig + 2];
      }
      if (start < fwidth)
      {
        viscontigs[nvis] = start;
        viscontigs[nvis + 1] = fwidth; // end is inclusive
        nvis += 2;
      }
      return viscontigs;
    }
    else
    {
      return new int[]
          {
          0, width};
    }
  }

  /**
   *
   * @return position of first visible column of AlignmentView within its parent's alignment reference frame
   */
  public int getAlignmentOrigin()
  {
    // TODO Auto-generated method stub
    return firstCol;
  }
}
