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

/** Data structure to hold and manipulate a multiple sequence alignment
 */
public interface AlignmentI
{
  /**
   *  Calculates the number of sequences in an alignment
   *
   * @return Number of sequences in alignment
   */
  public int getHeight();

  /**
   * Calculates the maximum width of the alignment, including gaps.
   *
   * @return Greatest sequence length within alignment.
   */
  public int getWidth();

  /**
   * Calculates if this set of sequences is all the same length
   *
   * @return true if all sequences in alignment are the same length
   */
  public boolean isAligned();

  /**
   * Gets sequences as a Vector
   *
   * @return All sequences in alignment.
   */
  public Vector getSequences();

  /**
   * Gets sequences as a SequenceI[]
   *
   * @return All sequences in alignment.
   */
  public SequenceI[] getSequencesArray();

  /**
   * Find a specific sequence in this alignment.
   *
   * @param i Index of required sequence.
   *
   * @return SequenceI at given index.
   */
  public SequenceI getSequenceAt(int i);

  /**
   * Add a new sequence to this alignment.
   *
   * @param seq New sequence will be added at end of alignment.
   */
  public void addSequence(SequenceI seq);

  /**
   * Used to set a particular index of the alignment with the given sequence.
   *
   * @param i Index of sequence to be updated.
   * @param seq New sequence to be inserted.
   */
  public void setSequenceAt(int i, SequenceI seq);

  /**
   * Deletes a sequence from the alignment
   *
   * @param s Sequence to be deleted.
   */
  public void deleteSequence(SequenceI s);

  /**
   * Deletes a sequence from the alignment.
   *
   * @param i Index of sequence to be deleted.
   */
  public void deleteSequence(int i);

  /**
   * Finds sequence in alignment using sequence name as query.
   *
   * @param name Id of sequence to search for.
   *
   * @return Sequence matching query, if found. If not found returns null.
   */
  public SequenceI findName(String name);

  public SequenceI[] findSequenceMatch(String name);

  /**
   * Finds index of a given sequence in the alignment.
   *
   * @param s Sequence to look for.
   *
   * @return Index of sequence within the alignment.
   */
  public int findIndex(SequenceI s);

  /**
   * Finds group that given sequence is part of.
   *
   * @param s Sequence in alignment.
   *
   * @return First group found for sequence. WARNING :
   * Sequences may be members of several groups. This method is incomplete.
   */
  public SequenceGroup findGroup(SequenceI s);

  /**
   * Finds all groups that a given sequence is part of.
   *
   * @param s Sequence in alignment.
   *
   * @return All groups containing given sequence.
   */
  public SequenceGroup[] findAllGroups(SequenceI s);

  /**
   * Adds a new SequenceGroup to this alignment.
   *
   * @param sg New group to be added.
   */
  public void addGroup(SequenceGroup sg);

  /**
   * Deletes a specific SequenceGroup
   *
   * @param g Group will be deleted from alignment.
   */
  public void deleteGroup(SequenceGroup g);

  /**
   * Get all the groups associated with this alignment.
   *
   * @return All groups as a Vector.
   */
  public Vector getGroups();


  /**
   * Deletes all groups from this alignment.
   */
  public void deleteAllGroups();

  /**
   * Adds a new AlignmentAnnotation to this alignment
   * @note Care should be taken to ensure that annotation is at
   * least as wide as the longest sequence in the alignment
   * for rendering purposes.
   */
  public void addAnnotation(AlignmentAnnotation aa);
  /**
   * moves annotation to a specified index in alignment annotation display stack
   * @param aa the annotation object to be moved
   * @param index the destination position
   */
  public void setAnnotationIndex(AlignmentAnnotation aa, int index);

  /**
   * Deletes a specific AlignmentAnnotation from the alignment,
   * and removes its reference from any SequenceI object's annotation
   * if and only if aa is contained within the alignment's annotation
   * vector. Otherwise, it will do nothing.
   * 
   * @param aa the annotation to delete
   * @return true if annotation was deleted from this alignment.
   */
  public boolean deleteAnnotation(AlignmentAnnotation aa);

  /**
   * Get the annotation associated with this alignment
   *
   * @return array of AlignmentAnnotation objects
   */
  public AlignmentAnnotation[] getAlignmentAnnotation();

  /**
   * Change the gap character used in this alignment to 'gc'
   *
   * @param gc the new gap character.
   */
  public void setGapCharacter(char gc);

  /**
   * Get the gap character used in this alignment
   *
   * @return gap character
   */
  public char getGapCharacter();

  /**
   * Test for all nucleotide alignment
   *
   * @return true if alignment is nucleotide sequence
   */
  public boolean isNucleotide();

  /**
   * Set alignment to be a nucleotide sequence
   *
   */
  public void setNucleotide(boolean b);

  /**
   * Get the associated dataset for the alignment.
   * @return Alignment containing dataset sequences or null of this is a dataset.
   */
  public Alignment getDataset();

  /**
   * Set the associated dataset for the alignment, or create one.
   * @param dataset The dataset alignment or null to construct one.
   */
  public void setDataset(Alignment dataset);

  /**
   * pads sequences with gaps (to ensure the set looks like an alignment)
   * @return boolean true if alignment was modified
   */
  public boolean padGaps();

  public HiddenSequences getHiddenSequences();

  /**
   * Compact representation of alignment
   * @return CigarArray
   */
  public CigarArray getCompactAlignment();

  /**
   * Set an arbitrary key value pair for an alignment.
   * Note: both key and value objects should return a 
   * meaningful, human readable response to .toString()
   * @param key
   * @param value
   */
  public void setProperty(Object key, Object value);
  /**
   * Get a named property from the alignment. 
   * @param key
   * @return value of property
   */
  public Object getProperty(Object key);
  /**
   * Get the property hashtable.
   * @return hashtable of alignment properties (or null if none are defined)
   */
  public Hashtable getProperties();
}
