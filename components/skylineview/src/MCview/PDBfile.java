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
package MCview;

import java.io.*;
import java.util.*;

import java.awt.*;

import jalview.datamodel.*;

public class PDBfile
    extends jalview.io.AlignFile
{
  public Vector chains;
  public String id;

  /**
   * set to true to add chain alignment annotation as visible annotation.
   */
  boolean VisibleChainAnnotation=false;
  public PDBfile(String inFile, String inType)
      throws IOException
  {
    super(inFile, inType);
  }

  public String print()
  {
    return null;
  }

  public void parse()
      throws IOException
  {
    try
    {
      chains = new Vector();

      PDBChain tmpchain;
      String line;
      boolean modelFlag = false;
      boolean terFlag = false;

      int index = 0;
      while ( (line = nextLine()) != null)
      {
        if (line.indexOf("HEADER") == 0)
        {
          id = line.substring(62, 67).trim();
          continue;
        }
        // Were we to do anything with SEQRES - we start it here
        if (line.indexOf("SEQRES") == 0)
        {
        }

        if (line.indexOf("MODEL") == 0)
        {
          modelFlag = true;
        }

        if (line.indexOf("TER") == 0)
        {
          terFlag = true;
        }

        if (modelFlag && line.indexOf("ENDMDL") == 0)
        {
          break;
        }
        if (line.indexOf("ATOM") == 0
            || (line.indexOf("HETATM") == 0 && !terFlag)
            )
        {
          terFlag = false;

          //Jalview is only interested in CA bonds????
          if (!line.substring(12, 15).trim().equals("CA"))
          {
            continue;
          }

          Atom tmpatom = new Atom(line);
          tmpchain = findChain(tmpatom.chain);
          if (tmpchain != null)
          {
            tmpchain.atoms.addElement(tmpatom);
          }
          else
          {
            tmpchain = new PDBChain(id, tmpatom.chain);
            chains.addElement(tmpchain);
            tmpchain.atoms.addElement(tmpatom);
          }
        }
        index++;
      }

      makeResidueList();
      makeCaBondList();

      if (id == null)
      {
        id = inFile.getName();
      }
      for (int i = 0; i < chains.size(); i++)
      {
        SequenceI dataset = ( (PDBChain) chains.elementAt(i)).
            sequence;
        dataset.setName(id + "|" + dataset.getName());
        PDBEntry entry = new PDBEntry();
        entry.setId(id);
        if (inFile != null)
        {
          entry.setFile(inFile.getAbsolutePath());
        }
        dataset.addPDBId(entry);
        SequenceI chainseq = dataset.deriveSequence(); // PDBChain objects maintain reference to dataset
        seqs.addElement(chainseq);
        AlignmentAnnotation[] chainannot = chainseq.getAnnotation();
        if (chainannot!=null)
        {
          for (int ai=0; ai<chainannot.length; ai++)
          {
            chainannot[ai].visible=VisibleChainAnnotation;
            annotations.addElement(chainannot[ai]);
          }
        }
      }
    }
    catch (OutOfMemoryError er)
    {
      System.out.println("OUT OF MEMORY LOADING PDB FILE");
      throw new IOException("Out of memory loading PDB File");
    }
  }

  public void makeResidueList()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      ( (PDBChain) chains.elementAt(i)).makeResidueList();
    }
  }

  public void makeCaBondList()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      ( (PDBChain) chains.elementAt(i)).makeCaBondList();
    }
  }

  public PDBChain findChain(String id)
  {
    for (int i = 0; i < chains.size(); i++)
    {
      if ( ( (PDBChain) chains.elementAt(i)).id.equals(id))
      {
        return (PDBChain) chains.elementAt(i);
      }
    }

    return null;
  }

  public void setChargeColours()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      ( (PDBChain) chains.elementAt(i)).setChargeColours();
    }
  }

  public void setColours(jalview.schemes.ColourSchemeI cs)
  {
    for (int i = 0; i < chains.size(); i++)
    {
      ( (PDBChain) chains.elementAt(i)).setChainColours(cs);
    }
  }

  public void setChainColours()
  {
    for (int i = 0; i < chains.size(); i++)
    {
      ( (PDBChain) chains.elementAt(i)).setChainColours(
          Color.getHSBColor(1.0f / (float) i, .4f, 1.0f)
          );
    }
  }
}
