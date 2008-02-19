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

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class FormatAdapter
    extends AppletFormatAdapter
{

  public String formatSequences(String format,
                                SequenceI[] seqs,
                                String[] omitHiddenColumns)
  {
    if (omitHiddenColumns != null)
    {
      SequenceI[] tmp = new SequenceI[seqs.length];
      for (int i = 0; i < seqs.length; i++)
      {
        tmp[i] = new Sequence(
            seqs[i].getName(), omitHiddenColumns[i],
            seqs[i].getStart(), seqs[i].getEnd());
        tmp[i].setDescription(seqs[i].getDescription());
      }
      seqs = tmp;
    }

    return formatSequences(format, seqs);
  }

  /**
   * DOCUMENT ME!
   *
   * @param format DOCUMENT ME!
   * @param seqs DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String formatSequences(String format,
                                SequenceI[] seqs)
  {

    try
    {
      AlignFile afile = null;

      if (format.equalsIgnoreCase("FASTA"))
      {
        afile = new FastaFile();
        afile.addJVSuffix(
            jalview.bin.Cache.getDefault("FASTA_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("MSF"))
      {
        afile = new MSFfile();
        afile.addJVSuffix(
            jalview.bin.Cache.getDefault("MSF_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("PileUp"))
      {
        afile = new PileUpfile();
        afile.addJVSuffix(
            jalview.bin.Cache.getDefault("PILEUP_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("CLUSTAL"))
      {
        afile = new ClustalFile();
        afile.addJVSuffix(
            jalview.bin.Cache.getDefault("CLUSTAL_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("BLC"))
      {
        afile = new BLCFile();
        afile.addJVSuffix(
            jalview.bin.Cache.getDefault("BLC_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("PIR"))
      {
        afile = new PIRFile();
        afile.addJVSuffix(
            jalview.bin.Cache.getDefault("PIR_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("PFAM"))
      {
        afile = new PfamFile();
        afile.addJVSuffix(
            jalview.bin.Cache.getDefault("PFAM_JVSUFFIX", true));
      }

      afile.setSeqs(seqs);

      return afile.print();
    }
    catch (Exception e)
    {
      System.err.println("Failed to write alignment as a '" + format +
                         "' file\n");
      e.printStackTrace();
    }

    return null;
  }
}
