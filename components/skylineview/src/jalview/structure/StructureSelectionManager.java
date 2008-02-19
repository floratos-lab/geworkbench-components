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
package jalview.structure;

import java.io.*;
import java.util.*;

import MCview.*;
import jalview.analysis.*;
import jalview.datamodel.*;

public class StructureSelectionManager
{
  static StructureSelectionManager instance;
  StructureMapping[] mappings;
  Hashtable mappingData = new Hashtable();

  public static StructureSelectionManager getStructureSelectionManager()
  {
    if (instance == null)
    {
      instance = new StructureSelectionManager();
    }

    return instance;
  }

  Vector listeners = new Vector();
  public void addStructureViewerListener(Object svl)
  {
    if (!listeners.contains(svl))
    {
      listeners.addElement(svl);
    }
  }

  public String alreadyMappedToFile(String pdbid)
  {
    if (mappings != null)
    {
      for (int i = 0; i < mappings.length; i++)
      {
        if (mappings[i].getPdbId().equals(pdbid))
        {
          return mappings[i].pdbfile;
        }
      }
    }
    return null;
  }

  /*
     There will be better ways of doing this in the future, for now we'll use
     the tried and tested MCview pdb mapping
   */
  synchronized public MCview.PDBfile setMapping(
      SequenceI[] sequence,
      String[] targetChains,
      String pdbFile,
      String protocol)
  {
    MCview.PDBfile pdb = null;
    try
    {
      pdb = new MCview.PDBfile(pdbFile, protocol);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      return null;
    }

    String targetChain;
    for (int s = 0; s < sequence.length; s++)
    {
      if(targetChains!=null && targetChains[s]!=null)
        targetChain = targetChains[s];
      else if (sequence[s].getName().indexOf("|") > -1)
      {
        targetChain = sequence[s].getName().substring(
            sequence[s].getName().lastIndexOf("|") + 1);
      }
      else
        targetChain = "";


      int max = -10;
      AlignSeq maxAlignseq = null;
      String maxChainId = " ";
      PDBChain maxChain = null;

      for (int i = 0; i < pdb.chains.size(); i++)
      {
        AlignSeq as = new AlignSeq(sequence[s],
                                   ( (PDBChain) pdb.chains.elementAt(i)).
                                   sequence,
                                   AlignSeq.PEP);
        as.calcScoreMatrix();
        as.traceAlignment();
        PDBChain chain = ( (PDBChain) pdb.chains.elementAt(i));

        if (as.maxscore > max
            || (as.maxscore == max && chain.id.equals(targetChain)))
        {
          maxChain = chain;
          max = as.maxscore;
          maxAlignseq = as;
          maxChainId = chain.id;
        }
      }

      final StringBuffer mappingDetails = new StringBuffer();
      mappingDetails.append("\n\nPDB Sequence is :\nSequence = " +
                            maxChain.sequence.getSequenceAsString());
      mappingDetails.append("\nNo of residues = " +
                            maxChain.residues.
                            size() +
                            "\n\n");
      PrintStream ps = new PrintStream(System.out)
      {
        public void print(String x)
        {
          mappingDetails.append(x);
        }

        public void println()
        {
          mappingDetails.append("\n");
        }
      };

      maxAlignseq.printAlignment(ps);

      mappingDetails.append("\nPDB start/end " + maxAlignseq.seq2start + " " +
                            maxAlignseq.seq2end);
      mappingDetails.append("\nSEQ start/end "
                            + (maxAlignseq.seq1start + sequence[s].getStart() - 1) +
                            " "
                            + (maxAlignseq.seq1end + sequence[s].getEnd() - 1));

      maxChain.makeExactMapping(maxAlignseq, sequence[s]);

      maxChain.transferRESNUMFeatures(sequence[s], null);

      int[][] mapping = new int[sequence[s].getEnd() + 2][2];
      int resNum = -10000;
      int index = 0;


      do
      {
        Atom tmp = (Atom) maxChain.atoms.elementAt(index);
        if (resNum != tmp.resNumber && tmp.alignmentMapping != -1)
        {
          resNum = tmp.resNumber;
          mapping[tmp.alignmentMapping+1][0] = tmp.resNumber;
          mapping[tmp.alignmentMapping+1][1] = tmp.atomIndex;
        }

        index++;
      }
      while(index < maxChain.atoms.size());

      if (mappings == null)
      {
        mappings = new StructureMapping[1];
      }
      else
      {
        StructureMapping[] tmp = new StructureMapping[mappings.length + 1];
        System.arraycopy(mappings, 0, tmp, 0, mappings.length);
        mappings = tmp;
      }

      if(protocol.equals(jalview.io.AppletFormatAdapter.PASTE))
        pdbFile = "INLINE"+pdb.id;

      mappings[mappings.length - 1]
          = new StructureMapping(sequence[s], pdbFile, pdb.id, maxChainId,
                                 mapping, mappingDetails.toString());
      maxChain.transferResidueAnnotation(mappings[mappings.length-1]);
    }
    /////////

    return pdb;
  }

  public void removeStructureViewerListener(Object svl, String pdbfile)
  {
    listeners.removeElement(svl);

    boolean removeMapping = true;

    StructureListener sl;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);
        if (sl.getPdbFile().equals(pdbfile))
        {
          removeMapping = false;
          break;
        }
      }
    }

    if (removeMapping && mappings!=null)
    {
      Vector tmp = new Vector();
      for (int i = 0; i < mappings.length; i++)
      {
        if (!mappings[i].pdbfile.equals(pdbfile))
        {
          tmp.addElement(mappings[i]);
        }
      }

      mappings = new StructureMapping[tmp.size()];
      tmp.copyInto(mappings);
    }
  }

  public void mouseOverStructure(int pdbResNum, String chain, String pdbfile)
  {
    SequenceListener sl;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof SequenceListener)
      {
        sl = (SequenceListener) listeners.elementAt(i);

        for (int j = 0; j < mappings.length; j++)
        {
          if (mappings[j].pdbfile.equals(pdbfile) &&
              mappings[j].pdbchain.equals(chain))
          {
            sl.highlightSequence(mappings[j].sequence,
                                 mappings[j].getSeqPos(pdbResNum));
          }
        }
      }
    }
  }

  public void mouseOverSequence(SequenceI seq, int index)
  {
    StructureListener sl;
    int atomNo = 0;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);

        for (int j = 0; j < mappings.length; j++)
        {
          if (mappings[j].sequence == seq)
          {
            atomNo = mappings[j].getAtomNum(index);

            if (atomNo > 0)
            {
              sl.highlightAtom(atomNo,
                               mappings[j].getPDBResNum(index),
                               mappings[j].pdbchain,
                               mappings[j].pdbfile);
            }
          }
        }
      }
    }
  }

  public Annotation[] colourSequenceFromStructure(SequenceI seq, String pdbid)
  {
    return null;
    //THIS WILL NOT BE AVAILABLE IN JALVIEW 2.3,
    //UNTIL THE COLOUR BY ANNOTATION IS REWORKED
   /* Annotation [] annotations = new Annotation[seq.getLength()];

    StructureListener sl;
    int atomNo = 0;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);

        for (int j = 0; j < mappings.length; j++)
        {

          if (mappings[j].sequence == seq
              && mappings[j].getPdbId().equals(pdbid)
              && mappings[j].pdbfile.equals(sl.getPdbFile()))
          {
            System.out.println(pdbid+" "+mappings[j].getPdbId()
                +" "+mappings[j].pdbfile);

            java.awt.Color col;
            for(int index=0; index<seq.getLength(); index++)
            {
              if(jalview.util.Comparison.isGap(seq.getCharAt(index)))
                continue;

              atomNo = mappings[j].getAtomNum(seq.findPosition(index));
              col = java.awt.Color.white;
              if (atomNo > 0)
              {
                col = sl.getColour(atomNo,
                                 mappings[j].getPDBResNum(index),
                                 mappings[j].pdbchain,
                                 mappings[j].pdbfile);
              }

              annotations[index] = new Annotation("X",null,' ',0,col);
            }
            return annotations;
          }
        }
      }
    }

    return annotations;*/
  }


  public void structureSelectionChanged()
  {  }

  public void sequenceSelectionChanged()
  {  }

  public void sequenceColoursChanged(Object source)
  {
    StructureListener sl;
    for (int i = 0; i < listeners.size(); i++)
    {
      if (listeners.elementAt(i) instanceof StructureListener)
      {
        sl = (StructureListener) listeners.elementAt(i);
        sl.updateColours(source);
      }
    }
  }

  public StructureMapping[] getMapping(String pdbfile)
  {
    Vector tmp = new Vector();
    for (int i = 0; i < mappings.length; i++)
    {
      if (mappings[i].pdbfile.equals(pdbfile))
      {
        tmp.addElement(mappings[i]);
      }
    }

    StructureMapping[] ret = new StructureMapping[tmp.size()];
    for (int i = 0; i < tmp.size(); i++)
    {
      ret[i] = (StructureMapping) tmp.elementAt(i);
    }

    return ret;
  }

  public String printMapping(String pdbfile)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < mappings.length; i++)
    {
      if (mappings[i].pdbfile.equals(pdbfile))
      {
        sb.append(mappings[i].mappingDetails);
      }
    }

    return sb.toString();
  }
}
