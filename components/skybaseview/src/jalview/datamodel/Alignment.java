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

import jalview.analysis.*;

/** Data structure to hold and manipulate a multiple sequence alignment
 */
public class Alignment
    implements AlignmentI
{
  protected Alignment dataset;
  protected Vector sequences;
  protected Vector groups = new Vector();
  protected char gapCharacter = '-';
  protected int type = NUCLEOTIDE;
  public static final int PROTEIN = 0;
  public static final int NUCLEOTIDE = 1;

  /** DOCUMENT ME!! */
  public AlignmentAnnotation[] annotations;

  HiddenSequences hiddenSequences = new HiddenSequences(this);

  public Hashtable alignmentProperties;

  private void initAlignment(SequenceI[] seqs)
  {
    int i = 0;

    if (jalview.util.Comparison.isNucleotide(seqs))
    {
      type = NUCLEOTIDE;
    }
    else
    {
      type = PROTEIN;
    }

    sequences = new Vector();

    for (i = 0; i < seqs.length; i++)
    {
      sequences.addElement(seqs[i]);
    }

  }

  /** Make an alignment from an array of Sequences.
   *
   * @param sequences
   */
  public Alignment(SequenceI[] seqs)
  {
    initAlignment(seqs);
  }

  /**
   * Make a new alignment from an array of SeqCigars
   * @param seqs SeqCigar[]
   */
  public Alignment(SeqCigar[] alseqs)
  {
    SequenceI[] seqs = SeqCigar.createAlignmentSequences(alseqs, gapCharacter,
        new ColumnSelection(), null);
    initAlignment(seqs);
  }

  /**
   * Make a new alignment from an CigarArray
   * JBPNote - can only do this when compactAlignment does not contain hidden regions.
   * JBPNote - must also check that compactAlignment resolves to a set of SeqCigars - or construct them appropriately.
   * @param compactAlignment CigarArray
   */
  public static AlignmentI createAlignment(CigarArray compactAlignment)
  {
    throw new Error("Alignment(CigarArray) not yet implemented");
    // this(compactAlignment.refCigars);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Vector getSequences()
  {
    return sequences;
  }

  public SequenceI[] getSequencesArray()
  {
    SequenceI[] reply = new SequenceI[sequences.size()];
    for (int i = 0; i < sequences.size(); i++)
    {
      reply[i] = (SequenceI) sequences.elementAt(i);
    }
    return reply;
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
    if (i < sequences.size())
    {
      return (SequenceI) sequences.elementAt(i);
    }

    return null;
  }

  /** Adds a sequence to the alignment.  Recalculates maxLength and size.
   *
   * @param snew
   */
  public void addSequence(SequenceI snew)
  {
    if (dataset != null)
    {
      // maintain dataset integrity
      if (snew.getDatasetSequence() != null)
      {
        getDataset().addSequence(snew.getDatasetSequence());
      }
      else
      {
        // derive new sequence
        SequenceI adding = snew.deriveSequence();
        getDataset().addSequence(adding.getDatasetSequence());
        snew = adding;
      }
    }
    if (sequences==null) {
      initAlignment(new SequenceI[] { snew });
    } else {
      sequences.addElement(snew);
    }
    if (hiddenSequences!=null)
      hiddenSequences.adjustHeightSequenceAdded();
  }

  /** Adds a sequence to the alignment.  Recalculates maxLength and size.
   *
   * @param snew
   */
  public void setSequenceAt(int i, SequenceI snew)
  {
    SequenceI oldseq = getSequenceAt(i);
    deleteSequence(oldseq);

    sequences.setElementAt(snew, i);
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Vector getGroups()
  {
    return groups;
  }

  public void finalize()
  {
    if(getDataset()!=null)
      getDataset().finalize();

    dataset = null;
    sequences = null;
    groups = null;
    annotations = null;
    hiddenSequences = null;
  }


  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   */
  public void deleteSequence(SequenceI s)
  {
    deleteSequence(findIndex(s));
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   */
  public void deleteSequence(int i)
  {
    if (i > -1 && i < getHeight())
    {
      sequences.removeElementAt(i);
      hiddenSequences.adjustHeightSequenceDeleted(i);
    }
  }

  /**    */
  public SequenceGroup findGroup(SequenceI s)
  {
    for (int i = 0; i < this.groups.size(); i++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(i);

      if (sg.getSequences(null).contains(s))
      {
        return sg;
      }
    }

    return null;
  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceGroup[] findAllGroups(SequenceI s)
  {
    Vector temp = new Vector();

    int gSize = groups.size();
    for (int i = 0; i < gSize; i++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(i);
      if (sg == null || sg.getSequences(null) == null)
      {
        this.deleteGroup(sg);
        gSize--;
        continue;
      }

      if (sg.getSequences(null).contains(s))
      {
        temp.addElement(sg);
      }
    }

    SequenceGroup[] ret = new SequenceGroup[temp.size()];

    for (int i = 0; i < temp.size(); i++)
    {
      ret[i] = (SequenceGroup) temp.elementAt(i);
    }

    return ret;
  }

  /**    */
  public void addGroup(SequenceGroup sg)
  {
    if (!groups.contains(sg))
    {
      if (hiddenSequences.getSize() > 0)
      {
        int i, iSize = sg.getSize();
        for (i = 0; i < iSize; i++)
        {
          if (!sequences.contains(sg.getSequenceAt(i)))
          {
            sg.deleteSequence(sg.getSequenceAt(i), false);
            iSize--;
            i--;
          }
        }

        if (sg.getSize() < 1)
        {
          return;
        }
      }

      groups.addElement(sg);
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void deleteAllGroups()
  {
    groups.removeAllElements();
  }

  /**    */
  public void deleteGroup(SequenceGroup g)
  {
    if (groups.contains(g))
    {
      groups.removeElement(g);
    }
  }

  /**    */
  public SequenceI findName(String name)
  {
    int i = 0;

    while (i < sequences.size())
    {
      if (getSequenceAt(i).getName().equals(name))
      {
        return getSequenceAt(i);
      }

      i++;
    }

    return null;
  }

  public SequenceI[] findSequenceMatch(String name)
  {
    Vector matches = new Vector();
    int i = 0;

    while (i < sequences.size())
    {
      if (getSequenceAt(i).getName().equals(name))
      {
        matches.addElement(getSequenceAt(i));
      }
      i++;
    }

    SequenceI[] result = new SequenceI[matches.size()];
    for (i = 0; i < result.length; i++)
    {
      result[i] = (SequenceI) matches.elementAt(i);
    }

    return result;

  }

  /**    */
  public int findIndex(SequenceI s)
  {
    int i = 0;

    while (i < sequences.size())
    {
      if (s == getSequenceAt(i))
      {
        return i;
      }

      i++;
    }

    return -1;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getHeight()
  {
    return sequences.size();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getWidth()
  {
    int maxLength = -1;

    for (int i = 0; i < sequences.size(); i++)
    {
      if (getSequenceAt(i).getLength() > maxLength)
      {
        maxLength = getSequenceAt(i).getLength();
      }
    }

    return maxLength;
  }

  /**
   * DOCUMENT ME!
   *
   * @param gc DOCUMENT ME!
   */
  public void setGapCharacter(char gc)
  {
    gapCharacter = gc;

    for (int i = 0; i < sequences.size(); i++)
    {
      Sequence seq = (Sequence) sequences.elementAt(i);
      seq.setSequence(seq.getSequenceAsString()
                      .replace('.', gc)
                      .replace('-', gc)
                      .replace(' ', gc)
          );
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public char getGapCharacter()
  {
    return gapCharacter;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isAligned()
  {
    int width = getWidth();

    for (int i = 0; i < sequences.size(); i++)
    {
      if (getSequenceAt(i).getLength() != width)
      {
        return false;
      }
    }

    return true;
  }
  /* (non-Javadoc)
   * @see jalview.datamodel.AlignmentI#deleteAnnotation(jalview.datamodel.AlignmentAnnotation)
   */
  public boolean deleteAnnotation(AlignmentAnnotation aa)
  {
    int aSize = 1;

    if (annotations != null)
    {
      aSize = annotations.length;
    }

    if (aSize < 1)
    {
      return false;
    }

    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize - 1];

    boolean swap=false;
    int tIndex = 0;

    for (int i = 0; i < aSize; i++)
    {
      if (annotations[i] == aa)
      {
        swap=true;
        continue;
      }
      if (tIndex<temp.length)
        temp[tIndex++] = annotations[i];
    }

    if (swap)
    {
      annotations = temp;
      if(aa.sequenceRef!=null)
        aa.sequenceRef.removeAlignmentAnnotation(aa);
    }
    return swap;
  }

  /**
   * DOCUMENT ME!
   *
   * @param aa DOCUMENT ME!
   */
  public void addAnnotation(AlignmentAnnotation aa)
  {
    int aSize = 1;
    if (annotations != null)
    {
      aSize = annotations.length + 1;
    }

    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize];

    temp[aSize - 1] = aa;

    int i = 0;

    if (aSize > 1)
    {
      for (i = 0; i < (aSize - 1); i++)
      {
        temp[i] = annotations[i];
      }
    }

    annotations = temp;
  }

  public void setAnnotationIndex(AlignmentAnnotation aa, int index)
  {
    if (aa == null || annotations == null || annotations.length - 1 < index)
    {
      return;
    }

    int aSize = annotations.length;
    AlignmentAnnotation[] temp = new AlignmentAnnotation[aSize];

    temp[index] = aa;

    for (int i = 0; i < aSize; i++)
    {
      if (i == index)
      {
        continue;
      }

      if (i < index)
      {
        temp[i] = annotations[i];
      }
      else
      {
        temp[i] = annotations[i - 1];
      }
    }

    annotations = temp;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public AlignmentAnnotation[] getAlignmentAnnotation()
  {
    return annotations;
  }

  public void setNucleotide(boolean b)
  {
    if (b)
    {
      type = NUCLEOTIDE;
    }
    else
    {
      type = PROTEIN;
    }
  }

  public boolean isNucleotide()
  {
    if (type == NUCLEOTIDE)
    {
      return true;
    }
    else
    {
      return false;
    }
  }

  public void setDataset(Alignment data)
  {
    if (dataset == null && data == null)
    {
      // Create a new dataset for this alignment.
      // Can only be done once, if dataset is not null
      // This will not be performed
      Sequence[] seqs = new Sequence[getHeight()];
      SequenceI currentSeq;
      for (int i = 0; i < getHeight(); i++)
      {
        currentSeq = getSequenceAt(i);
        if (currentSeq.getDatasetSequence() != null)
        {
          seqs[i] = (Sequence) currentSeq.getDatasetSequence();
        }
        else
        {
          seqs[i] = new Sequence(currentSeq.getName(),
                                 AlignSeq.extractGaps(
                                     jalview.util.Comparison.GapChars,
                                     currentSeq.getSequenceAsString()
                                 ),
                                 currentSeq.getStart(),
                                 currentSeq.getEnd());
          seqs[i].sequenceFeatures = currentSeq.getSequenceFeatures();
          seqs[i].setDescription(currentSeq.getDescription());
          getSequenceAt(i).setSequenceFeatures(null);
          getSequenceAt(i).setDatasetSequence(seqs[i]);
        }
      }

      dataset = new Alignment(seqs);
    }
    else if (dataset == null && data != null)
    {
      dataset = data;
    }
  }

  public Alignment getDataset()
  {
    return dataset;
  }

  public boolean padGaps()
  {
    boolean modified = false;

    //Remove excess gaps from the end of alignment
    int maxLength = -1;

    SequenceI current;
    for (int i = 0; i < sequences.size(); i++)
    {
      current = getSequenceAt(i);
      for (int j = current.getLength(); j > maxLength; j--)
      {
        if (j > maxLength && !jalview.util.Comparison.isGap(
            current.getCharAt(j)))
        {
          maxLength = j;
          break;
        }
      }
    }

    maxLength++;

    int cLength;
    for (int i = 0; i < sequences.size();
         i++)
    {
      current = getSequenceAt(i);
      cLength = current.getLength();

      if (cLength < maxLength)
      {
        current.insertCharAt(cLength,
                             maxLength - cLength, gapCharacter);
        modified = true;
      }
      else if (current.getLength() > maxLength)
      {
        current.deleteChars(maxLength, current.getLength());
      }
    }
    return modified;
  }

  public HiddenSequences getHiddenSequences()
  {
    return hiddenSequences;
  }

  public CigarArray getCompactAlignment()
  {
    SeqCigar alseqs[] = new SeqCigar[sequences.size()];
    for (int i = 0; i < sequences.size(); i++)
    {
      alseqs[i] = new SeqCigar( (SequenceI) sequences.elementAt(i));
    }
    CigarArray cal = new CigarArray(alseqs);
    cal.addOperation(CigarArray.M, getWidth());
    return cal;
  }

  public void setProperty(Object key, Object value)
  {
    if(alignmentProperties==null)
      alignmentProperties = new Hashtable();

    alignmentProperties.put(key,value);
  }

  public Object getProperty(Object key)
  {
    if(alignmentProperties!=null)
      return alignmentProperties.get(key);
    else
      return null;
  }

  public Hashtable getProperties()
  {
    return alignmentProperties;
  }

}
