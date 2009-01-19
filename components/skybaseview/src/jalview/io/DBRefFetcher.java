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

import org.exolab.castor.mapping.*;
import org.exolab.castor.xml.*;
import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.gui.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class DBRefFetcher
    implements Runnable
{
  SequenceI [] dataset;
  AlignFrame af;
  CutAndPasteTransfer output = new CutAndPasteTransfer();
  StringBuffer sbuffer = new StringBuffer();
  boolean running = false;

  ///This will be a collection of Vectors of sequenceI refs.
  //The key will be the seq name or accession id of the seq
  Hashtable seqRefs;

  public DBRefFetcher()
  {}

  public Vector getUniprotEntries(File file)
  {
    UniprotFile uni = new UniprotFile();
    try
    {
      // 1. Load the mapping information from the file
      org.exolab.castor.mapping.Mapping map = new org.exolab.castor.mapping.Mapping(uni.getClass().getClassLoader());
      java.net.URL url = getClass().getResource("/uniprot_mapping.xml");
      map.loadMapping(url);

      // 2. Unmarshal the data
      Unmarshaller unmar = new Unmarshaller(uni.getClass());
//      unmar.setIgnoreExtraElements(true);
      unmar.setMapping(map);

      uni = (UniprotFile) unmar.unmarshal(new FileReader(file));
    }
    catch (Exception e)
    {
      System.out.println("Error getUniprotEntries() " + e);
    }

    return uni.getUniprotEntries();
  }

  /**
   * Creates a new SequenceFeatureFetcher object.
   *
   * @param align DOCUMENT ME!
   * @param ap DOCUMENT ME!
   */
  public DBRefFetcher(SequenceI [] seqs, AlignFrame af)
  {
    this.af = af;
    SequenceI [] ds = new SequenceI[seqs.length];
    for (int i = 0; i < seqs.length; i++)
    {
      if(seqs[i].getDatasetSequence()!=null)
        ds[i] = seqs[i].getDatasetSequence();
      else
        ds[i] = seqs[i];
    }
    this.dataset = ds;
  }

  public boolean fetchDBRefs(boolean waitTillFinished)
  {
    Thread thread = new Thread(this);
    thread.start();
    running = true;

    if (waitTillFinished)
    {
      while (running)
      {
        try
        {
          Thread.sleep(500);
        }
        catch (Exception ex)
        {}
      }
    }

    return true;
  }

  /**
   * The sequence will be added to a vector of sequences
   * belonging to key which could be either seq name or dbref id
   * @param seq SequenceI
   * @param key String
   */
  void addSeqId(SequenceI seq, String key)
  {
    key = key.toUpperCase();

    Vector seqs;
    if (seqRefs.containsKey(key))
    {
      seqs = (Vector) seqRefs.get(key);

      if (seqs != null && !seqs.contains(seq))
      {
        seqs.addElement(seq);
      }
      else if (seqs == null)
      {
        seqs = new Vector();
        seqs.addElement(seq);
      }

    }
    else
    {
      seqs = new Vector();
      seqs.addElement(seq);
    }

    seqRefs.put(key, seqs);
  }

  /**
   * DOCUMENT ME!
   */
  public void run()
  {
    long startTime = System.currentTimeMillis();
    af.setProgressBar("Fetching db refs", startTime);
    running = true;

    seqRefs = new Hashtable();

    try
    {
      int seqIndex = 0;

      while (seqIndex < dataset.length)
      {
        StringBuffer queryString = new StringBuffer("uniprot:");

        for (int i = 0; (seqIndex < dataset.length) && (i < 50);
             seqIndex++, i++)
        {
          SequenceI sequence = dataset[seqIndex];
          DBRefEntry[] uprefs = jalview.util.DBRefUtils.selectRefs(sequence.
              getDBRef(), new String[]
              {
              jalview.datamodel.DBRefSource.UNIPROT});
          if (uprefs != null)
          {
            if (uprefs.length + i > 50)
            {
              break;
            }

            for (int j = 0; j < uprefs.length; j++)
            {
              addSeqId(sequence, uprefs[j].getAccessionId());
              queryString.append(uprefs[j].getAccessionId() + ";");
            }
          }
          else
          {
            StringTokenizer st = new StringTokenizer(sequence.getName(), "|");
            if (st.countTokens() + i > 50)
            {
              //Dont send more than 50 id strings to dbFetch!!
              seqIndex--;
            }
            else
            {
              while (st.hasMoreTokens())
              {
                String token = st.nextToken();
                addSeqId(sequence, token);
                queryString.append(token + ";");
              }
            }
          }
        }

        ///////////////////////////////////
        ///READ FROM EBI
        EBIFetchClient ebi = new EBIFetchClient();
        File file = ebi.fetchDataAsFile(queryString.toString(), "xml", "raw");
        if (file != null)
        {
          ReadUniprotFile(file);
        }
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }

    if (sbuffer.length() > 0)
    {
      output.setText(
          "Your sequences have been matched to Uniprot. Some of the ids have been\n" +
          "altered, most likely the start/end residue will have been updated.\n" +
          "Save your alignment to maintain the updated id.\n\n" +
          sbuffer.toString());
      Desktop.addInternalFrame(output, "Sequence names updated ", 600, 300);
      // The above is the dataset, we must now find out the index
      // of the viewed sequence

    }

    af.setProgressBar("DBRef search completed", startTime);
    // promptBeforeBlast();

    running = false;

  }

  /**
   * DOCUMENT ME!
   *
   * @param result DOCUMENT ME!
   * @param out DOCUMENT ME!
   * @param align DOCUMENT ME!
   */
  void ReadUniprotFile(File file)
  {
    if (!file.exists())
    {
      return;
    }

    SequenceI sequence = null;

    Vector entries = getUniprotEntries(file);

    int i, iSize = entries == null ? 0 : entries.size();
    UniprotEntry entry;
    for (i = 0; i < iSize; i++)
    {
      entry = (UniprotEntry) entries.elementAt(i);

      //Work out which sequences this Uniprot file has matches to,
      //taking into account all accessionIds and names in the file
      Vector sequenceMatches = new Vector();
      for (int j = 0; j < entry.getAccession().size(); j++)
      {
        String accessionId = entry.getAccession().elementAt(j).toString();
        if (seqRefs.containsKey(accessionId))
        {
          Vector seqs = (Vector) seqRefs.get(accessionId);
          for (int jj = 0; jj < seqs.size(); jj++)
          {
            sequence = (SequenceI) seqs.elementAt(jj);
            if (!sequenceMatches.contains(sequence))
            {
              sequenceMatches.addElement(sequence);
            }
          }
        }
      }
      for (int j = 0; j < entry.getName().size(); j++)
      {
        String name = entry.getName().elementAt(j).toString();
        if (seqRefs.containsKey(name))
        {
          Vector seqs = (Vector) seqRefs.get(name);
          for (int jj = 0; jj < seqs.size(); jj++)
          {
            sequence = (SequenceI) seqs.elementAt(jj);
            if (!sequenceMatches.contains(sequence))
            {
              sequenceMatches.addElement(sequence);
            }
          }
        }
      }

      for (int m = 0; m < sequenceMatches.size(); m++)
      {
        sequence = (SequenceI) sequenceMatches.elementAt(m);
        sequence.addDBRef(new DBRefEntry(DBRefSource.UNIPROT,
                                         "0",
                                         entry.getAccession().elementAt(0).
                                         toString()));

        System.out.println("Adding dbref to " + sequence.getName() + " : " +
                           entry.getAccession().elementAt(0).toString());

        String nonGapped = AlignSeq.extractGaps("-. ",
                                                sequence.getSequenceAsString()).
            toUpperCase();

        int absStart = entry.getUniprotSequence().getContent().indexOf(
            nonGapped.toString());

        if (absStart == -1)
        {
          // Is UniprotSequence contained in dataset sequence?
          absStart = nonGapped.toString().indexOf(entry.getUniprotSequence().
                                                  getContent());
          if (absStart == -1)
          {
            sbuffer.append(sequence.getName() + " SEQUENCE NOT %100 MATCH \n");
            continue;
          }

          if (entry.getFeature() != null)
          {
            Enumeration e = entry.getFeature().elements();
            while (e.hasMoreElements())
            {
              SequenceFeature sf = (SequenceFeature) e.nextElement();
              sf.setBegin(sf.getBegin() + absStart + 1);
              sf.setEnd(sf.getEnd() + absStart + 1);
            }

            sbuffer.append(sequence.getName() +
                           " HAS " + absStart +
                           " PREFIXED RESIDUES COMPARED TO UNIPROT - ANY SEQUENCE FEATURES"
                           + " HAVE BEEN ADJUSTED ACCORDINGLY \n");
            absStart = 0;
          }

        }

        //unknownSequences.remove(sequence);

        int absEnd = absStart + nonGapped.toString().length();
        absStart += 1;

        Enumeration e = entry.getDbReference().elements();
        Vector onlyPdbEntries = new Vector();
        while (e.hasMoreElements())
        {
          PDBEntry pdb = (PDBEntry) e.nextElement();
          if (!pdb.getType().equals(DBRefSource.PDB))
          {
            continue;
          }

          sequence.addDBRef(new DBRefEntry(DBRefSource.PDB,
                                           "0",
                                           pdb.getId()));

          onlyPdbEntries.addElement(pdb);
        }

        sequence.setPDBId(onlyPdbEntries);

        sequence.setStart(absStart);
        sequence.setEnd(absEnd);

      }
    }
  }
}
