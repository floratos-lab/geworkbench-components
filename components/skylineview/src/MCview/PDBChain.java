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

import java.util.*;

import java.awt.*;

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.schemes.*;
import jalview.structure.StructureMapping;

public class PDBChain
{
  /**
   * SequenceFeature group for PDB File features added to sequences
   */
  private static final String PDBFILEFEATURE = "PDBFile";
  private static final String IEASTATUS = "IEA:jalview";
  public String id;
  public Vector bonds = new Vector();
  public Vector atoms = new Vector();
  public Vector residues = new Vector();
  public int offset;
  public Sequence sequence;
  public boolean isVisible = true;
  public int pdbstart = 0;
  public int pdbend = 0;
  public int seqstart = 0;
  public int seqend = 0;
  public String pdbid = "";
  public PDBChain(String pdbid, String id)
  {
    this.pdbid = pdbid.toLowerCase();
    this.id = id;
  }

  public String print()
  {
    String tmp = "";

    for (int i = 0; i < bonds.size(); i++)
    {
      tmp = tmp + ( (Bond) bonds.elementAt(i)).at1.resName + " " +
          ( (Bond) bonds.elementAt(i)).at1.resNumber + " " + offset +
          "\n";
    }

    return tmp;
  }

  public void makeExactMapping(AlignSeq as, SequenceI s1)
  {
    int pdbpos = as.getSeq2Start() - 2;
    int alignpos = s1.getStart() + as.getSeq1Start() - 3;

    for (int i = 0; i < as.astr1.length(); i++)
    {
      if (as.astr1.charAt(i) != '-')
      {
        alignpos++;
      }

      if (as.astr2.charAt(i) != '-')
      {
        pdbpos++;
      }

      if (as.astr1.charAt(i) == as.astr2.charAt(i))
      {
        Residue res = (Residue) residues.elementAt(pdbpos);
        Enumeration en = res.atoms.elements();
        while (en.hasMoreElements())
        {
          Atom atom = (Atom) en.nextElement();
          atom.alignmentMapping = alignpos;
        }
      }
    }
  }
  /**
   * copy over the RESNUM seqfeatures from the internal chain sequence to the mapped sequence
   * @param seq
   * @param status The Status of the transferred annotation
   * @return the features added to sq (or its dataset)
   */
  public SequenceFeature[] transferRESNUMFeatures(SequenceI seq, String status)
  {
    SequenceI sq = seq;
    while (sq!=null && sq.getDatasetSequence()!=null)
    {
      sq = sq.getDatasetSequence();
      if (sq == sequence)
      {
        return null;
      }
    }
    /**
     * Remove any existing features for this chain if they exist ?
     * SequenceFeature[] seqsfeatures=seq.getSequenceFeatures();
        int totfeat=seqsfeatures.length;
        // Remove any features for this exact chain ?
        for (int i=0; i<seqsfeatures.length; i++) {
        } */
    if (status == null)
    {
      status = PDBChain.IEASTATUS;
    }
    SequenceFeature[] features = sequence.getSequenceFeatures();
    for (int i = 0; i < features.length; i++)
    {
      if (features[i].getFeatureGroup().equals(pdbid))
      {
        SequenceFeature tx = new SequenceFeature(features[i]);
        tx.setBegin(1 +
                    ( (Atom) ( (Residue) residues.elementAt(tx.getBegin() - offset)).
                     atoms.elementAt(0)).alignmentMapping);
        tx.setEnd(1 +
                  ( (Atom) ( (Residue) residues.elementAt(tx.getEnd() - offset)).
                   atoms.elementAt(0)).alignmentMapping);
        tx.setStatus(status +
                     ( (tx.getStatus() == null || tx.getStatus().length() == 0) ?
                      "" : ":" + tx.getStatus()));
        if (tx.begin!=0 && tx.end!=0)
          sq.addSequenceFeature(tx);
      }
    }
    return features;
  }

  public void makeCaBondList()
  {
    for (int i = 0; i < (residues.size() - 1); i++)
    {
      Residue tmpres = (Residue) residues.elementAt(i);
      Residue tmpres2 = (Residue) residues.elementAt(i + 1);
      Atom at1 = tmpres.findAtom("CA");
      Atom at2 = tmpres2.findAtom("CA");

      if ( (at1 != null) && (at2 != null))
      {
        if (at1.chain.equals(at2.chain))
        {
          makeBond(at1, at2);
        }
      }
      else
      {
        System.out.println("not found " + i);
      }
    }
  }

  public void makeBond(Atom at1, Atom at2)
  {
    float[] start = new float[3];
    float[] end = new float[3];

    start[0] = at1.x;
    start[1] = at1.y;
    start[2] = at1.z;

    end[0] = at2.x;
    end[1] = at2.y;
    end[2] = at2.z;

    bonds.addElement(new Bond(start, end, at1, at2));
  }

  public void makeResidueList()
  {
    int count = 0;
    StringBuffer seq = new StringBuffer();
    Vector resFeatures = new Vector();
    Vector resAnnotation = new Vector();
    int i, iSize = atoms.size() - 1;
    int resNumber = -1;
    for (i = 0; i <= iSize; i++)
    {
      Atom tmp = (Atom) atoms.elementAt(i);
      resNumber = tmp.resNumber;
      int res = resNumber;

      if (i == 0)
      {
        offset = resNumber;
      }

      Vector resAtoms = new Vector();
      //Add atoms to a vector while the residue number
      //remains the same as the first atom's resNumber (res)
      while ( (resNumber == res) && (i < atoms.size()))
      {
        resAtoms.addElement( (Atom) atoms.elementAt(i));
        i++;

        if (i < atoms.size())
        {
          resNumber = ( (Atom) atoms.elementAt(i)).resNumber;
        }
        else
        {
          resNumber++;
        }
      }

      //We need this to keep in step with the outer for i = loop
      i--;

      //Make a new Residue object with the new atoms vector
      residues.addElement(new Residue(resAtoms, resNumber - 1, count));

      Residue tmpres = (Residue) residues.lastElement();
      Atom tmpat = (Atom) tmpres.atoms.elementAt(0);
      // Make A new SequenceFeature for the current residue numbering
      SequenceFeature sf =
          new SequenceFeature("RESNUM",
                              tmpat.resName + ":" + tmpat.resNumIns + " " +
                              pdbid + id,
                              "", offset + count, offset + count,
                              pdbid);

      //MCview.PDBChain.PDBFILEFEATURE);
      resFeatures.addElement(sf);
      resAnnotation.addElement(new Annotation(tmpat.tfactor));
      // Keep totting up the sequence
      if (ResidueProperties.getAA3Hash().get(tmpat.resName) == null)
      {
        seq.append("X");
        //  System.err.println("PDBReader:Null aa3Hash for " +
        //     tmpat.resName);
      }
      else
      {

        seq.append(ResidueProperties.aa[ ( (Integer) ResidueProperties.
                                          getAA3Hash()
                                          .get(tmpat.resName)).intValue()]);
      }
      count++;
    }

    if (id.length() < 1)
    {
      id = " ";
    }

    sequence = new Sequence(id, seq.toString(), offset, resNumber - 1); // Note: resNumber-offset ~= seq.size()
    //  System.out.println("PDB Sequence is :\nSequence = " + seq);
    //   System.out.println("No of residues = " + residues.size());
    for (i = 0, iSize = resFeatures.size(); i < iSize; i++)
    {
      sequence.addSequenceFeature( (SequenceFeature) resFeatures.elementAt(i));
      resFeatures.setElementAt(null, i);
    }
    Annotation[] annots = new Annotation[resAnnotation.size()];
    float max=0;
    for (i=0,iSize=annots.length; i<iSize; i++)
    {
      annots[i] = (Annotation) resAnnotation.elementAt(i);
      if (annots[i].value>max)
        max = annots[i].value;
      resAnnotation.setElementAt(null, i);
    }
    AlignmentAnnotation tfactorann = new AlignmentAnnotation("PDB.CATempFactor","CA Temperature Factor for "+sequence.getName(),
            annots, 0, max, AlignmentAnnotation.LINE_GRAPH);
    tfactorann.setSequenceRef(sequence);
    sequence.addAlignmentAnnotation(tfactorann);
  }

  public void setChargeColours()
  {
    for (int i = 0; i < bonds.size(); i++)
    {
      try
      {
        Bond b = (Bond) bonds.elementAt(i);

        if (b.at1.resName.equalsIgnoreCase("ASP") ||
            b.at1.resName.equalsIgnoreCase("GLU"))
        {
          b.startCol = Color.red;
        }
        else if (b.at1.resName.equalsIgnoreCase("LYS") ||
                 b.at1.resName.equalsIgnoreCase("ARG"))
        {
          b.startCol = Color.blue;
        }
        else if (b.at1.resName.equalsIgnoreCase("CYS"))
        {
          b.startCol = Color.yellow;
        }
        else
        {
          b.startCol = Color.lightGray;
        }

        if (b.at2.resName.equalsIgnoreCase("ASP") ||
            b.at2.resName.equalsIgnoreCase("GLU"))
        {
          b.endCol = Color.red;
        }
        else if (b.at2.resName.equalsIgnoreCase("LYS") ||
                 b.at2.resName.equalsIgnoreCase("ARG"))
        {
          b.endCol = Color.blue;
        }
        else if (b.at2.resName.equalsIgnoreCase("CYS"))
        {
          b.endCol = Color.yellow;
        }
        else
        {
          b.endCol = Color.lightGray;
        }
      }
      catch (Exception e)
      {
        Bond b = (Bond) bonds.elementAt(i);
        b.startCol = Color.gray;
        b.endCol = Color.gray;
      }
    }
  }

  public void setChainColours(jalview.schemes.ColourSchemeI cs)
  {
    Bond b;
    int index;
    for (int i = 0; i < bonds.size(); i++)
    {
      try
      {
        b = (Bond) bonds.elementAt(i);

        index = ( (Integer) ResidueProperties.aa3Hash.get(b.at1.
            resName)).intValue();
        b.startCol = cs.findColour(ResidueProperties.aa[index].charAt(0));

        index = ( (Integer) ResidueProperties.aa3Hash.get(b.at2.resName)).
            intValue();
        b.endCol = cs.findColour(ResidueProperties.aa[index].charAt(0));

      }
      catch (Exception e)
      {
        b = (Bond) bonds.elementAt(i);
        b.startCol = Color.gray;
        b.endCol = Color.gray;
      }
    }
  }

  public void setChainColours(Color col)
  {
    for (int i = 0; i < bonds.size(); i++)
    {
      Bond tmp = (Bond) bonds.elementAt(i);
      tmp.startCol = col;
      tmp.endCol = col;
    }
  }

  public AlignmentAnnotation[] transferResidueAnnotation(SequenceI seq, String status)
  {
    AlignmentAnnotation[] transferred = null;

    return transferred;

  }

  /**
   * copy any sequence annotation onto the sequence mapped using the provided StructureMapping
   * @param mapping
   */
  public void transferResidueAnnotation(StructureMapping mapping)
  {
    SequenceI sq = mapping.getSequence();
    if (sq!=null)
    {
      if (sequence!=null && sequence.getAnnotation()!=null)
      {

      }
      float min=-1,max=0;
      Annotation[] an=new Annotation[sq.getEnd()-sq.getStart()+1];
      for (int i=sq.getStart(),j=sq.getEnd(),k=0; i<=j; i++,k++)
      {
        int prn = mapping.getPDBResNum(k+1);

        an[k] = new Annotation((float)prn);
        if (min==-1)
        {
          min=k;
          max=k;
        } else {
          if (min>k)
          {
            min=k;
          } else
            if (max<k)
            {
              max=k;
            }
        }
      }
      sq.addAlignmentAnnotation(new AlignmentAnnotation("PDB.RESNUM", "PDB Residue Numbering for "+this.pdbid+":"+this.id, an, (float)min,(float)max, AlignmentAnnotation.LINE_GRAPH));
    }
  }
}
