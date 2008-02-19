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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class Sequence
    implements SequenceI
{
  SequenceI datasetSequence;
  String name;
  private char [] sequence;
  String description;
  int start;
  int end;
  Vector pdbIds;
  String vamsasId;
  DBRefEntry[] dbrefs;

  /** This annotation is displayed below the alignment but the
   * positions are tied to the residues of this sequence */
  Vector annotation;

  /** DOCUMENT ME!! */
  public SequenceFeature[] sequenceFeatures;


  /**
   * Creates a new Sequence object.
   *
   * @param name DOCUMENT ME!
   * @param sequence DOCUMENT ME!
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   */
  public Sequence(String name, String sequence, int start, int end)
  {
    this.name = name;
    this.sequence = sequence.toCharArray();
    this.start = start;
    this.end = end;
    parseId();
    checkValidRange();
  }

  public Sequence(String name, char [] sequence, int start, int end)
  {
    this.name = name;
    this.sequence = sequence;
    this.start = start;
    this.end = end;
    parseId();
    checkValidRange();
  }

  com.stevesoft.pat.Regex limitrx = new com.stevesoft.pat.Regex(
      "[/][0-9]{1,}[-][0-9]{1,}$");
  com.stevesoft.pat.Regex endrx = new com.stevesoft.pat.Regex(
      "[0-9]{1,}$");

  void parseId()
  {
    // Does sequence have the /start-end signiature?
    if (limitrx.search(name))
    {
      name = limitrx.left();
      endrx.search(limitrx.stringMatched());
      setStart(Integer.parseInt(limitrx.stringMatched().substring(1,
          endrx.matchedFrom() - 1)));
      setEnd(Integer.parseInt(endrx.stringMatched()));
    }
  }

  void checkValidRange()
  {
    if (end < 1)
    {
      int endRes = 0;
      for (int j = 0; j < sequence.length; j++)
      {
        if (!jalview.util.Comparison.isGap( sequence[j] ))
        {
          endRes++;
        }
      }
      if (endRes > 0)
      {
        endRes += start - 1;
      }

      this.end = endRes;
    }

  }

  /**
   * Creates a new Sequence object.
   *
   * @param name DOCUMENT ME!
   * @param sequence DOCUMENT ME!
   */
  public Sequence(String name, String sequence)
  {
    this(name, sequence, 1, -1);
  }

  /**
   * Creates a new Sequence object with new features, DBRefEntries, AlignmentAnnotations, and PDBIds
   * but inherits any existing dataset sequence reference.
   * @param seq DOCUMENT ME!
   */
  public Sequence(SequenceI seq)
  {
    this(seq, seq.getAnnotation());
  }
  /**
   * Create a new sequence object with new features, DBRefEntries, and PDBIds
   * but inherits any existing dataset sequence reference, and duplicate of
   * any annotation that is present in the given annotation array.
   * @param seq the sequence to be copied
   * @param alAnnotation an array of annotation including some associated with seq 
   */
  public Sequence(SequenceI seq, AlignmentAnnotation[] alAnnotation)
  {
    this(seq.getName(),
            seq.getSequence(),
            seq.getStart(),
            seq.getEnd());
    description = seq.getDescription();
    if (seq.getSequenceFeatures()!=null) {
      SequenceFeature[] sf = seq.getSequenceFeatures();
      for (int i=0; i<sf.length; i++) {
        addSequenceFeature(new SequenceFeature(sf[i]));
      }
    }
    if (seq.getDBRef()!=null) {
      DBRefEntry[] dbr = seq.getDBRef();
      for (int i=0; i<dbr.length; i++) {
        addDBRef(new DBRefEntry(dbr[i]));
      }
    }
    setDatasetSequence(seq.getDatasetSequence());
    if (seq.getAnnotation()!=null) {
      AlignmentAnnotation[] sqann = seq.getAnnotation();
      for (int i=0;i<sqann.length; i++)
      {
        if (sqann[i]==null)
        {
          continue;
        }
        boolean found = (alAnnotation==null);
        if (!found)
        {
          for (int apos = 0; !found && apos<alAnnotation.length; apos++)
          {
            found = (alAnnotation[apos] == sqann[i]);
          }
        }
        if (found)
        {
          // only copy the given annotation
          AlignmentAnnotation newann = new AlignmentAnnotation(sqann[i]);
          addAlignmentAnnotation(newann);
        }
      }
    }
    if (seq.getPDBId()!=null) {
      Vector ids = seq.getPDBId();
      Enumeration e = ids.elements();
      while (e.hasMoreElements()) {
        this.addPDBId(new PDBEntry((PDBEntry) e.nextElement()));
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param v DOCUMENT ME!
   */
  public void setSequenceFeatures(SequenceFeature[] features)
  {
    sequenceFeatures = features;
  }

  public synchronized void addSequenceFeature(SequenceFeature sf)
  {
    if (sequenceFeatures == null)
    {
      sequenceFeatures = new SequenceFeature[0];
    }

    for (int i = 0; i < sequenceFeatures.length; i++)
    {
      if (sequenceFeatures[i].equals(sf))
      {
        return;
      }
    }

    SequenceFeature[] temp = new SequenceFeature[sequenceFeatures.length + 1];
    System.arraycopy(sequenceFeatures, 0, temp, 0, sequenceFeatures.length);
    temp[sequenceFeatures.length] = sf;

    sequenceFeatures = temp;
  }

  public void deleteFeature(SequenceFeature sf)
  {
    if(sequenceFeatures==null)
    {
      return;
    }

    int index=0;
    for (index = 0; index < sequenceFeatures.length; index++)
    {
      if (sequenceFeatures[index].equals(sf))
      {
        break;
      }
    }


    if(index==sequenceFeatures.length)
    {
      return;
    }

    int sfLength = sequenceFeatures.length;
    if(sfLength<2)
    {
      sequenceFeatures = null;
    }
    else
    {
      SequenceFeature[] temp = new SequenceFeature[sfLength-1];
      System.arraycopy(sequenceFeatures, 0, temp, 0, index);

      if(index<sfLength)
      {
        System.arraycopy(sequenceFeatures,
                         index + 1,
                         temp,
                         index, sequenceFeatures.length - index -1);
      }

      sequenceFeatures = temp;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceFeature[] getSequenceFeatures()
  {
    return sequenceFeatures;
  }

  public void addPDBId(PDBEntry entry)
  {
    if (pdbIds == null)
    {
      pdbIds = new Vector();
    }

    pdbIds.addElement(entry);
  }

  /**
   * DOCUMENT ME!
   *
   * @param id DOCUMENT ME!
   */
  public void setPDBId(Vector id)
  {
    pdbIds = id;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Vector getPDBId()
  {
    return pdbIds;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getDisplayId(boolean jvsuffix)
  {
    StringBuffer result = new StringBuffer(name);
    if (jvsuffix)
    {
      result.append("/" + start + "-" + end);
    }

    return result.toString();
  }

  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   */
  public void setName(String name)
  {
    this.name = name;
    this.parseId();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * DOCUMENT ME!
   *
   * @param start DOCUMENT ME!
   */
  public void setStart(int start)
  {
    this.start = start;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getStart()
  {
    return this.start;
  }

  /**
   * DOCUMENT ME!
   *
   * @param end DOCUMENT ME!
   */
  public void setEnd(int end)
  {
    this.end = end;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getEnd()
  {
    return this.end;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getLength()
  {
    return this.sequence.length;
  }

  /**
   * DOCUMENT ME!
   *
   * @param seq DOCUMENT ME!
   */
  public void setSequence(String seq)
  {
    this.sequence = seq.toCharArray();
    checkValidRange();
  }


  public String getSequenceAsString()
  {
    return new String(sequence);
  }

  public String getSequenceAsString(int start, int end)
  {
    return new String(getSequence(start, end));
  }


  public char [] getSequence()
  {
    return sequence;
  }

  /**
   * DOCUMENT ME!
   *
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public char [] getSequence(int start, int end)
  {
    if (start<0)
      start=0;
    // JBPNote - left to user to pad the result here (TODO:Decide on this policy)
    if (start >= sequence.length)
    {
      return new char[0];
    }

    if (end >= sequence.length)
    {
      end = sequence.length;
    }

    char [] reply = new char[end-start];
    System.arraycopy(sequence, start, reply, 0, end-start);

    return reply;
  }


  /**
   * make a new Sequence object from start to end (including gaps) over this seqeunce
   * @param start int
   * @param end int
   * @return SequenceI
   */
  public SequenceI getSubSequence(int start, int end)
  {
    if (start < 0)
    {
      start = 0;
    }
    char [] seq = getSequence(start, end);
    if (seq.length == 0)
    {
      return null;
    }
    int nstart = findPosition(start);
    int nend = findPosition(end) - 1;
    // JBPNote - this is an incomplete copy.
    SequenceI nseq = new Sequence(this.getName(), seq, nstart, nend);
    nseq.setDescription(description);
    if (datasetSequence!=null)
    {
        nseq.setDatasetSequence(datasetSequence);
    }
    else
    {
        nseq.setDatasetSequence(this);
    }
    return nseq;
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public char getCharAt(int i)
  {
    if (i < sequence.length)
    {
      return sequence[i];
    }
    else
    {
      return ' ';
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param desc DOCUMENT ME!
   */
  public void setDescription(String desc)
  {
    this.description = desc;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getDescription()
  {
    return this.description;
  }

  /**
   * Return the alignment position for a sequence position
   *
   * @param pos lying from start to end
   *
   * @return aligned position of residue pos
   */
  public int findIndex(int pos)
  {
    // returns the alignment position for a residue
    int j = start;
    int i = 0;

    while ( (i < sequence.length) && (j <= end) && (j <= pos))
    {
      if (!jalview.util.Comparison.isGap(sequence[i]))
      {
        j++;
      }

      i++;
    }

    if ( (j == end) && (j < pos))
    {
      return end + 1;
    }
    else
    {
      return i;
    }
  }

  /**
   * Returns the sequence position for an alignment position
   *
   * @param i column index in alignment (from 1)
   *
   * @return residue number for residue (left of and) nearest ith column
   */
  public int findPosition(int i)
  {
    int j = 0;
    int pos = start;
    int seqlen = sequence.length;
    while ( (j < i) && (j < seqlen))
    {
      if (!jalview.util.Comparison.isGap( sequence[j] ))
      {
        pos++;
      }

      j++;
    }

    return pos;
  }

  /**
   * Returns an int array where indices correspond to each residue in the sequence and the element value gives its position in the alignment
   *
   * @return int[SequenceI.getEnd()-SequenceI.getStart()+1] or null if no residues in SequenceI object
   */
  public int[] gapMap()
  {
    String seq = jalview.analysis.AlignSeq.extractGaps(jalview.util.Comparison.
        GapChars, new String(sequence));
    int[] map = new int[seq.length()];
    int j = 0;
    int p = 0;

    while (j < sequence.length)
    {
      if (!jalview.util.Comparison.isGap(sequence[j]))
      {
        map[p++] = j;
      }

      j++;
    }

    return map;
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   */
  public void deleteChars(int i, int j)
  {
    if (i >= sequence.length)
    {
      return;
    }

    char [] tmp;

    if (j >= sequence.length)
    {
      tmp = new char[i];
      System.arraycopy(sequence,0,tmp,0,i);
    }
    else
    {
      tmp = new char[sequence.length-j+i];
      System.arraycopy(sequence,0,tmp,0,i);
      System.arraycopy(sequence,j,tmp,i,sequence.length-j);
    }

    if (this.datasetSequence != null)
    {
      for (int s = i; s < j; s++)
      {
        if (jalview.schemes.ResidueProperties.aaIndex[sequence[s]] != 23)
        {

          Sequence ds = new Sequence(name,
                                     AlignSeq.extractGaps(
                                         jalview.util.Comparison.GapChars,
                                         this.getSequenceAsString()
                                     ),
                                     start,
                                     end);
          ds.setDescription(description);
        }
        break;
      }
    }

    sequence = tmp;

  }


  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param c DOCUMENT ME!
   * @param chop DOCUMENT ME!
   */
  public void insertCharAt(int i, int length, char c)
  {
    char [] tmp = new char[sequence.length+length];

    if (i >= sequence.length)
    {
      System.arraycopy(sequence, 0, tmp, 0, sequence.length);
      i = sequence.length;
    }
    else
   {
      System.arraycopy(sequence, 0, tmp, 0, i);
   }


    int index = i;
    while (length > 0)
    {
      tmp[ index++ ] = c;
      length--;
    }

    if (i < sequence.length)
    {
      System.arraycopy(sequence, i, tmp, index, sequence.length-i );
    }

    sequence = tmp;
  }

  public void insertCharAt(int i, char c)
  {
    insertCharAt(i, 1, c);
  }

  public String getVamsasId()
  {
    return vamsasId;
  }

  public void setVamsasId(String id)
  {
    vamsasId = id;
  }

  public void setDBRef(DBRefEntry[] dbref)
  {
    dbrefs = dbref;
  }

  public DBRefEntry[] getDBRef()
  {
    return dbrefs;
  }

  public void addDBRef(DBRefEntry entry)
  {
    if (dbrefs == null)
    {
      dbrefs = new DBRefEntry[0];
    }

    int i, iSize = dbrefs.length;

    for(i=0; i<iSize; i++)
    {
      if(dbrefs[i].equals(entry))
      {
        return;
      }
    }

    DBRefEntry[] temp = new DBRefEntry[iSize + 1];
    System.arraycopy(dbrefs, 0, temp, 0, iSize);
    temp[temp.length - 1] = entry;

    dbrefs = temp;
  }

  public void setDatasetSequence(SequenceI seq)
  {
    datasetSequence = seq;
  }

  public SequenceI getDatasetSequence()
  {
    return datasetSequence;
  }

  public AlignmentAnnotation[] getAnnotation()
  {
    if (annotation == null)
    {
      return null;
    }

    AlignmentAnnotation[] ret = new AlignmentAnnotation[annotation.size()];
    for (int r = 0; r < ret.length; r++)
    {
      ret[r] = (AlignmentAnnotation) annotation.elementAt(r);
    }

    return ret;
  }

  public void addAlignmentAnnotation(AlignmentAnnotation annotation)
  {
    if (this.annotation == null)
    {
      this.annotation = new Vector();
    }

    this.annotation.addElement(annotation);
    annotation.setSequenceRef(this);
  }

  public void removeAlignmentAnnotation(AlignmentAnnotation annotation)
  {
    if(this.annotation!=null)
    {
      this.annotation.removeElement(annotation);
      if(this.annotation.size()==0)
        this.annotation = null;
    }
  }


  /**
   * test if this is a valid candidate for another
   * sequence's dataset sequence.
   *
   */
  private boolean isValidDatasetSequence()
  {
    if (datasetSequence!=null)
    {
          return false;
    }
      for (int i=0;i<sequence.length; i++)
    {
          if (jalview.util.Comparison.isGap(sequence[i]))
      {
              return false;
      }
    }
      return true;
  }
  /* (non-Javadoc)
   * @see jalview.datamodel.SequenceI#deriveSequence()
   */
  public SequenceI deriveSequence()
  {
    SequenceI seq=new Sequence(this);
    if (datasetSequence != null)
    {
      // duplicate current sequence with same dataset
      seq.setDatasetSequence(datasetSequence);
    }
    else
    {
      if (isValidDatasetSequence())
      {
        // Use this as dataset sequence
        seq.setDatasetSequence(this);
      } else {
        // Create a new, valid dataset sequence
        SequenceI ds = seq;
        ds.setSequence(AlignSeq.extractGaps(jalview.util.Comparison.GapChars, new String(sequence)));
        setDatasetSequence(ds);
        seq = this; // and return this sequence as the derived sequence.
      }
    }
    return seq;
  }
  /* (non-Javadoc)
   * @see jalview.datamodel.SequenceI#setAlignmentAnnotation(AlignmmentAnnotation[] annotations)
   */
  public void setAlignmentAnnotation(AlignmentAnnotation[] annotations)
  {
    if (annotation!=null) {
      annotation.removeAllElements();
    }
    if (annotations!=null) {
      for (int i=0; i<annotations.length; i++)
      {
        if (annotations[i]!=null)
          addAlignmentAnnotation(annotations[i]);
      }
    }
  }

  /* (non-Javadoc)
   * @see jalview.datamodel.SequenceI#getAnnotation(java.lang.String)
   */
  public AlignmentAnnotation[] getAnnotation(String label)
  {
    if (annotation==null || annotation.size()==0)
    {
      return null;
    }
    
    Vector subset = new Vector();
    Enumeration e = annotation.elements();
    while (e.hasMoreElements())
    {
      AlignmentAnnotation ann = (AlignmentAnnotation) e.nextElement();
      if (ann.label!=null && ann.label.equals(label))
      {
        subset.addElement(ann);
      }
    }
    if (subset.size()==0)
    {
      return null;
    }
    AlignmentAnnotation[] anns = new AlignmentAnnotation[subset.size()];
    int i=0;
    e = subset.elements();
    while (e.hasMoreElements())
    {
      anns[i++] = (AlignmentAnnotation) e.nextElement();
    }
    subset.removeAllElements();
    return anns;
  }

}


