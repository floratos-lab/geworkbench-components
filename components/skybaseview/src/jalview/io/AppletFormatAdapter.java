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
public class AppletFormatAdapter
{
  /** DOCUMENT ME!! */
  public static final String[] READABLE_FORMATS = new String[]
      {
      "BLC", "CLUSTAL", "FASTA", "MSF", "PileUp", "PIR", "PFAM", "STH", "PDB"
  };

  public static final String[] WRITEABLE_FORMATS = new String[]
      {
      "BLC", "CLUSTAL", "FASTA", "MSF", "PileUp", "PIR", "PFAM" //, "AMSA"
  };

  public static String INVALID_CHARACTERS = "Contains invalid characters";

  public static String SUPPORTED_FORMATS = "Formats currently supported are\n" +
      "Fasta, MSF, Clustal, BLC, PIR, MSP, and PFAM";

  public static String FILE = "File";
  public static String URL = "URL";
  public static String PASTE = "Paste";
  public static String CLASSLOADER = "ClassLoader";

  AlignFile afile = null;
  String inFile;

  public static final boolean isValidFormat(String format)
  {
    boolean valid = false;
    for (int i = 0; i < READABLE_FORMATS.length; i++)
    {
      if (READABLE_FORMATS[i].equalsIgnoreCase(format))
      {
        return true;
      }
    }

    return valid;
  }

  /**
   * Constructs the correct filetype parser for a characterised datasource
   *
   * @param inFile data/data location
   * @param type type of datasource
   * @param format File format of data provided by datasource
   *
   * @return DOCUMENT ME!
   */
  public Alignment readFile(String inFile, String type, String format)
      throws java.io.IOException
  {
    this.inFile = inFile;
    try
    {
      if (format.equals("FASTA"))
      {
        afile = new FastaFile(inFile, type);
      }
      else if (format.equals("MSF"))
      {
        afile = new MSFfile(inFile, type);
      }
      else if (format.equals("PileUp"))
      {
        afile = new PileUpfile(inFile, type);
      }
      else if (format.equals("CLUSTAL"))
      {
        afile = new ClustalFile(inFile, type);
      }
      else if (format.equals("BLC"))
      {
        afile = new BLCFile(inFile, type);
      }
      else if (format.equals("PIR"))
      {
        afile = new PIRFile(inFile, type);
      }
      else if (format.equals("PFAM"))
      {
        afile = new PfamFile(inFile, type);
      }
      else if (format.equals("JnetFile"))
      {
        afile = new JPredFile(inFile, type);
        ( (JPredFile) afile).removeNonSequences();
      }
      else if (format.equals("PDB"))
      {
        afile = new MCview.PDBfile(inFile, type);
      }
      else if (format.equals("STH"))
      {
        afile = new StockholmFile(inFile, type);
      }

      Alignment al = new Alignment(afile.getSeqsAsArray());

      afile.addAnnotations(al);

      return al;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.err.println("Failed to read alignment using the '" + format +
                         "' reader.\n" + e);

      if (e.getMessage() != null &&
          e.getMessage().startsWith(INVALID_CHARACTERS))
      {
        throw new java.io.IOException(e.getMessage());
      }

      // Finally test if the user has pasted just the sequence, no id
      if (type.equalsIgnoreCase("Paste"))
      {
        try
        {
          // Possible sequence is just residues with no label
          afile = new FastaFile(">UNKNOWN\n" + inFile, "Paste");
          Alignment al = new Alignment(afile.getSeqsAsArray());
          afile.addAnnotations(al);
          return al;

        }
        catch (Exception ex)
        {
          if (ex.toString().startsWith(INVALID_CHARACTERS))
          {
            throw new java.io.IOException(e.getMessage());
          }

          ex.printStackTrace();
        }
      }

      // If we get to this stage, the format was not supported
      throw new java.io.IOException(SUPPORTED_FORMATS);
    }
  }

  /**
   * Construct an output class for an alignment in a particular filetype
   *
   * @param format DOCUMENT ME!
   * @param seqs DOCUMENT ME!
   * @param jvsuffix passed to AlnFile class
   *
   * @return alignment flat file contents
   */
  public String formatSequences(String format,
                                AlignmentI alignment,
                                boolean jvsuffix)
  {
    try
    {
      AlignFile afile = null;

      if (format.equalsIgnoreCase("FASTA"))
      {
        afile = new FastaFile();
      }
      else if (format.equalsIgnoreCase("MSF"))
      {
        afile = new MSFfile();
      }
      else if (format.equalsIgnoreCase("PileUp"))
      {
        afile = new PileUpfile();
      }
      else if (format.equalsIgnoreCase("CLUSTAL"))
      {
        afile = new ClustalFile();
      }
      else if (format.equalsIgnoreCase("BLC"))
      {
        afile = new BLCFile();
      }
      else if (format.equalsIgnoreCase("PIR"))
      {
        afile = new PIRFile();
      }
      else if (format.equalsIgnoreCase("PFAM"))
      {
        afile = new PfamFile();
      }
      else if (format.equalsIgnoreCase("STH"))
      {
        afile = new StockholmFile();
      }
      else if (format.equals("AMSA"))
      {
        afile = new AMSAFile(alignment);
      }

      afile.addJVSuffix(jvsuffix);

      afile.setSeqs(alignment.getSequencesArray());

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
