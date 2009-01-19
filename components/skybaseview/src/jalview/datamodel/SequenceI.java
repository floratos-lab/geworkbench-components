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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public interface SequenceI
{
  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   */
  public void setName(String name);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName();

  /**
   * DOCUMENT ME!
   *
   * @param start DOCUMENT ME!
   */
  public void setStart(int start);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getStart();

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getDisplayId(boolean jvsuffix);

  /**
   * DOCUMENT ME!
   *
   * @param end DOCUMENT ME!
   */
  public void setEnd(int end);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getEnd();

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getLength();

  /**
   * DOCUMENT ME!
   *
   * @param sequence DOCUMENT ME!
   */
  public void setSequence(String sequence);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getSequenceAsString();

  /**
   * DOCUMENT ME!
   *
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getSequenceAsString(int start, int end);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public char[] getSequence();

  /**
   * DOCUMENT ME!
   *
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public char[] getSequence(int start, int end);

  /**
   * create a new sequence object from start to end of this sequence
   * @param start int
   * @param end int
   * @return SequenceI
   */
  public SequenceI getSubSequence(int start, int end);

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public char getCharAt(int i);

  /**
   * DOCUMENT ME!
   *
   * @param desc DOCUMENT ME!
   */
  public void setDescription(String desc);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getDescription();

  /**
   * DOCUMENT ME!
   *
   * @param pos DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int findIndex(int pos);

  /**
   * Returns the sequence position for an alignment position
   *
   * @param i column index in alignment (from 1)
   *
   * @return residue number for residue (left of and) nearest ith column
   */
  public int findPosition(int i);

  /**
   * Returns an int array where indices correspond to each residue in the sequence and the element value gives its position in the alignment
   *
   * @return int[SequenceI.getEnd()-SequenceI.getStart()+1] or null if no residues in SequenceI object
   */
  public int[] gapMap();

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   */
  public void deleteChars(int i, int j);

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param c DOCUMENT ME!
   */
  public void insertCharAt(int i, char c);

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param c DOCUMENT ME!
   */
  public void insertCharAt(int i, int length, char c);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceFeature[] getSequenceFeatures();

  /**
   * DOCUMENT ME!
   *
   * @param v DOCUMENT ME!
   */
  public void setSequenceFeatures(SequenceFeature[] features);

  /**
   * DOCUMENT ME!
   *
   * @param id DOCUMENT ME!
   */
  public void setPDBId(Vector ids);

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Vector getPDBId();

  public void addPDBId(PDBEntry entry);

  public String getVamsasId();

  public void setVamsasId(String id);

  public void setDBRef(DBRefEntry[] dbs);

  public DBRefEntry[] getDBRef();

  public void addDBRef(DBRefEntry entry);

  public void addSequenceFeature(SequenceFeature sf);

  public void deleteFeature(SequenceFeature sf);

  public void setDatasetSequence(SequenceI seq);

  public SequenceI getDatasetSequence();

  public AlignmentAnnotation[] getAnnotation();

  public void addAlignmentAnnotation(AlignmentAnnotation annotation);

  public void removeAlignmentAnnotation(AlignmentAnnotation annotation);

  /**
   * Derive a sequence (using this one's dataset or as the dataset)
   * @return duplicate sequence with valid dataset sequence
   */
  public SequenceI deriveSequence();
  /**
   * set the array of associated AlignmentAnnotation for this sequenceI
   * @param revealed
   */
  public void setAlignmentAnnotation(AlignmentAnnotation[] annotation);
  /**
   * Get one or more alignment annotations with a particular label.  
   * @param label string which each returned annotation must have as a label.
   * @return null or array of annotations.
   */
  public AlignmentAnnotation[] getAnnotation(String label);

}
