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

import java.util.*;

import java.awt.*;

import jalview.analysis.*;
import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SequenceGroup
{
  String groupName;
  String description;
  Conservation conserve;
  Vector aaFrequency;
  boolean displayBoxes = true;
  boolean displayText = true;
  boolean colourText = false;
  private Vector sequences = new Vector();
  int width = -1;

  /** DOCUMENT ME!! */
  public ColourSchemeI cs;
  int startRes = 0;
  int endRes = 0;
  Color outlineColour = Color.black;
  public int thresholdTextColour = 0;
  public Color textColour = Color.black;
  public Color textColour2 = Color.white;

  /**
   * Creates a new SequenceGroup object.
   */
  public SequenceGroup()
  {
    groupName = "JGroup:" + this.hashCode();
  }

  /**
   * Creates a new SequenceGroup object.
   *
   * @param sequences DOCUMENT ME!
   * @param groupName DOCUMENT ME!
   * @param scheme DOCUMENT ME!
   * @param displayBoxes DOCUMENT ME!
   * @param displayText DOCUMENT ME!
   * @param colourText DOCUMENT ME!
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   */
  public SequenceGroup(Vector sequences, String groupName,
                       ColourSchemeI scheme, boolean displayBoxes,
                       boolean displayText,
                       boolean colourText, int start, int end)
  {
    this.sequences = sequences;
    this.groupName = groupName;
    this.displayBoxes = displayBoxes;
    this.displayText = displayText;
    this.colourText = colourText;
    this.cs = scheme;
    startRes = start;
    endRes = end;
    recalcConservation();
  }

  public SequenceI[] getSelectionAsNewSequences(AlignmentI align)
  {
    int iSize = sequences.size();
    SequenceI[] seqs = new SequenceI[iSize];
    SequenceI[] inorder = getSequencesInOrder(align);
    
    for (int i = 0,ipos=0; i < inorder.length; i++)
    {
      SequenceI seq = inorder[i];

      seqs[ipos] = seq.getSubSequence(startRes, endRes+1);
      if (seqs[ipos]!=null)
      {
        seqs[ipos].setDescription(seq.getDescription());
        seqs[ipos].setDBRef(seq.getDBRef());
        seqs[ipos].setSequenceFeatures(seq.getSequenceFeatures());
        if (seq.getDatasetSequence() != null)
        {
          seqs[ipos].setDatasetSequence(seq.getDatasetSequence());
        }

        if (seq.getAnnotation() != null)
        {
          AlignmentAnnotation[] alann = align.getAlignmentAnnotation();
          // Only copy annotation that is either a score or referenced by the alignment's annotation vector
          for (int a = 0; a < seq.getAnnotation().length; a++)
          {
            AlignmentAnnotation tocopy = seq.getAnnotation()[a];
            if (alann!=null)
            {
              boolean found=false;
              for (int pos=0;pos<alann.length; pos++)
              {
                if (alann[pos]==tocopy)
                { 
                  found=true;
                  break;
                }
              }
              if (!found)
                continue;
            }
            AlignmentAnnotation newannot = new AlignmentAnnotation(seq
                    .getAnnotation()[a]);
            newannot.restrict(startRes, endRes);
            newannot.setSequenceRef(seqs[ipos]);
            newannot.adjustForAlignment();
            seqs[ipos].addAlignmentAnnotation(newannot);
          }
        }
        ipos++;
      } else {
        iSize--;
      }
    }
    if (iSize!=inorder.length)
    {
      SequenceI[] nseqs = new SequenceI[iSize];
      System.arraycopy(seqs, 0, nseqs, 0, iSize);
      seqs = nseqs;
    }
    return seqs;

  }

  /**
   * If sequence ends in gaps, the end residue can
   * be correctly calculated here
   * @param seq SequenceI
   * @return int
   */
  public int findEndRes(SequenceI seq)
  {
    int eres = 0;
    char ch;

    for (int j = 0; j < endRes + 1 && j < seq.getLength(); j++)
    {
      ch = seq.getCharAt(j);
      if (!jalview.util.Comparison.isGap( (ch)))
      {
        eres++;
      }
    }

    if (eres > 0)
    {
      eres += seq.getStart() - 1;
    }

    return eres;
  }

  public Vector getSequences(Hashtable hiddenReps)
  {
    if (hiddenReps == null)
    {
      return sequences;
    }
    else
    {
      Vector allSequences = new Vector();
      SequenceI seq, seq2;
      for (int i = 0; i < sequences.size(); i++)
      {
        seq = (SequenceI) sequences.elementAt(i);
        allSequences.addElement(seq);
        if (hiddenReps.containsKey(seq))
        {
          SequenceGroup hsg = (SequenceGroup) hiddenReps.get(seq);
          for (int h = 0; h < hsg.getSize(); h++)
          {
            seq2 = hsg.getSequenceAt(h);
            if (seq2 != seq
                && !allSequences.contains(seq2))
            {
              allSequences.addElement(seq2);
            }
          }
        }
      }

      return allSequences;
    }
  }

  public SequenceI[] getSequencesAsArray(Hashtable hiddenReps)
  {
    Vector tmp = getSequences(hiddenReps);
    if (tmp == null)
    {
      return null;
    }
    SequenceI[] result = new SequenceI[tmp.size()];
    for (int i = 0; i < result.length; i++)
    {
      result[i] = (SequenceI) tmp.elementAt(i);
    }

    return result;
  }

  /**
   * DOCUMENT ME!
   *
   * @param col DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean adjustForRemoveLeft(int col)
  {
    // return value is true if the group still exists
    if (startRes >= col)
    {
      startRes = startRes - col;
    }

    if (endRes >= col)
    {
      endRes = endRes - col;

      if (startRes > endRes)
      {
        startRes = 0;
      }
    }
    else
    {
      // must delete this group!!
      return false;
    }

    return true;
  }

  /**
   * DOCUMENT ME!
   *
   * @param col DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean adjustForRemoveRight(int col)
  {
    if (startRes > col)
    {
      // delete this group
      return false;
    }

    if (endRes >= col)
    {
      endRes = col;
    }

    return true;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName()
  {
    return groupName;
  }

  public String getDescription()
  {
    return description;
  }

  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   */
  public void setName(String name)
  {
    groupName = name;
  }

  public void setDescription(String desc)
  {
    description = desc;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Conservation getConservation()
  {
    return conserve;
  }

  /**
   * DOCUMENT ME!
   *
   * @param c DOCUMENT ME!
   */
  public void setConservation(Conservation c)
  {
    conserve = c;
  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param recalc DOCUMENT ME!
   */
  public void addSequence(SequenceI s, boolean recalc)
  {
    if (s != null && !sequences.contains(s))
    {
      sequences.addElement(s);
    }

    if (recalc)
    {
      recalcConservation();
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void recalcConservation()
  {
    if (cs == null)
    {
      return;
    }

    try
    {
      cs.setConsensus(AAFrequency.calculate(sequences, startRes, endRes + 1));

      if (cs instanceof ClustalxColourScheme)
      {
        ( (ClustalxColourScheme) cs).resetClustalX(sequences, getWidth());
      }

      if (cs.conservationApplied())
      {
        Conservation c = new Conservation(groupName,
                                          ResidueProperties.propHash, 3,
                                          sequences,
                                          startRes, endRes + 1);
        c.calculate();
        c.verdict(false, 25);

        cs.setConservation(c);

        if (cs instanceof ClustalxColourScheme)
        {
          ( (ClustalxColourScheme) cs).resetClustalX(sequences,
              getWidth());
        }
      }
    }
    catch (java.lang.OutOfMemoryError err)
    {
      System.out.println("Out of memory loading groups: " + err);
    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param recalc DOCUMENT ME!
   */
  public void addOrRemove(SequenceI s, boolean recalc)
  {
    if (sequences.contains(s))
    {
      deleteSequence(s, recalc);
    }
    else
    {
      addSequence(s, recalc);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param recalc DOCUMENT ME!
   */
  public void deleteSequence(SequenceI s, boolean recalc)
  {
    sequences.removeElement(s);

    if (recalc)
    {
      recalcConservation();
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getStartRes()
  {
    return startRes;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getEndRes()
  {
    return endRes;
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   */
  public void setStartRes(int i)
  {
    startRes = i;
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   */
  public void setEndRes(int i)
  {
    endRes = i;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getSize()
  {
    return sequences.size();
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceI getSequenceAt(int i)
  {
    return (SequenceI) sequences.elementAt(i);
  }

  /**
   * DOCUMENT ME!
   *
   * @param state DOCUMENT ME!
   */
  public void setColourText(boolean state)
  {
    colourText = state;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean getColourText()
  {
    return colourText;
  }

  /**
   * DOCUMENT ME!
   *
   * @param state DOCUMENT ME!
   */
  public void setDisplayText(boolean state)
  {
    displayText = state;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean getDisplayText()
  {
    return displayText;
  }

  /**
   * DOCUMENT ME!
   *
   * @param state DOCUMENT ME!
   */
  public void setDisplayBoxes(boolean state)
  {
    displayBoxes = state;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean getDisplayBoxes()
  {
    return displayBoxes;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getWidth()
  {
    // MC This needs to get reset when characters are inserted and deleted
    if (sequences.size() > 0)
    {
      width = ( (SequenceI) sequences.elementAt(0)).getLength();
    }

    for (int i = 1; i < sequences.size(); i++)
    {
      SequenceI seq = (SequenceI) sequences.elementAt(i);

      if (seq.getLength() > width)
      {
        width = seq.getLength();
      }
    }

    return width;
  }

  /**
   * DOCUMENT ME!
   *
   * @param c DOCUMENT ME!
   */
  public void setOutlineColour(Color c)
  {
    outlineColour = c;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Color getOutlineColour()
  {
    return outlineColour;
  }

  /**
   *
   * returns the sequences in the group ordered by the ordering given by al
   *
   * @param al Alignment
   * @return SequenceI[]
   */
  public SequenceI[] getSequencesInOrder(AlignmentI al)
  {
    int sSize = sequences.size();
    int alHeight = al.getHeight();

    SequenceI[] seqs = new SequenceI[sSize];

    int index = 0;
    for (int i = 0; i < alHeight && index < sSize; i++)
    {
      if (sequences.contains(al.getSequenceAt(i)))
      {
        seqs[index++] = al.getSequenceAt(i);
      }
    }

    return seqs;
  }
}
