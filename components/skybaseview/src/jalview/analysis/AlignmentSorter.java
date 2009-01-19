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
package jalview.analysis;

import java.util.*;

import jalview.datamodel.*;
import jalview.util.*;

/** 
 * Routines for manipulating the order of a multiple sequence alignment
 * TODO: this class retains some global states concerning sort-order which should be made attributes for the caller's alignment visualization. 
 */
public class AlignmentSorter
{
  static boolean sortIdAscending = true;
  static int lastGroupHash = 0;
  static boolean sortGroupAscending = true;
  static AlignmentOrder lastOrder = null;
  static boolean sortOrderAscending = true;
  static NJTree lastTree = null;
  static boolean sortTreeAscending = true;
  private static String lastSortByScore;

  /**
   * Sort by Percentage Identity
   *
   * @param align AlignmentI
   * @param s SequenceI
   */
  public static void sortByPID(AlignmentI align, SequenceI s)
  {
    int nSeq = align.getHeight();

    float[] scores = new float[nSeq];
    SequenceI[] seqs = new SequenceI[nSeq];

    for (int i = 0; i < nSeq; i++)
    {
      scores[i] = Comparison.PID(align.getSequenceAt(i).getSequenceAsString(),
                                 s.getSequenceAsString());
      seqs[i] = align.getSequenceAt(i);
    }

    QuickSort.sort(scores, 0, scores.length - 1, seqs);

    setReverseOrder(align, seqs);
  }

  /**
   * Reverse the order of the sort
   *
   * @param align DOCUMENT ME!
   * @param seqs DOCUMENT ME!
   */
  private static void setReverseOrder(AlignmentI align, SequenceI[] seqs)
  {
    int nSeq = seqs.length;

    int len = 0;

    if ( (nSeq % 2) == 0)
    {
      len = nSeq / 2;
    }
    else
    {
      len = (nSeq + 1) / 2;
    }

    // NOTE: DO NOT USE align.setSequenceAt() here - it will NOT work
    for (int i = 0; i < len; i++)
    {
      //SequenceI tmp = seqs[i];
      align.getSequences().setElementAt(seqs[nSeq - i - 1], i);
      align.getSequences().setElementAt(seqs[i], nSeq - i - 1);
    }
  }

  /**
   * Sets the Alignment object with the given sequences
   *
   * @param align Alignment object to be updated
   * @param tmp sequences as a vector
   */
  private static void setOrder(AlignmentI align, Vector tmp)
  {
    setOrder(align, vectorSubsetToArray(tmp, align.getSequences()));
  }

  /**
   * Sets the Alignment object with the given sequences
   *
   * @param align DOCUMENT ME!
   * @param seqs sequences as an array
   */
  public static void setOrder(AlignmentI align, SequenceI[] seqs)
  {
    // NOTE: DO NOT USE align.setSequenceAt() here - it will NOT work
    Vector algn = align.getSequences();
    Vector tmp = new Vector();

    for (int i = 0; i < seqs.length; i++)
    {
      if (algn.contains(seqs[i]))
      {
        tmp.addElement(seqs[i]);
      }
    }

    algn.removeAllElements();
    //User may have hidden seqs, then clicked undo or redo
    for (int i = 0; i < tmp.size(); i++)
    {
      algn.addElement(tmp.elementAt(i));
    }

  }

  /**
   * Sorts by ID. Numbers are sorted before letters.
   *
   * @param align The alignment object to sort
   */
  public static void sortByID(AlignmentI align)
  {
    int nSeq = align.getHeight();

    String[] ids = new String[nSeq];
    SequenceI[] seqs = new SequenceI[nSeq];

    for (int i = 0; i < nSeq; i++)
    {
      ids[i] = align.getSequenceAt(i).getName();
      seqs[i] = align.getSequenceAt(i);
    }

    QuickSort.sort(ids, seqs);

    if (sortIdAscending)
    {
      setReverseOrder(align, seqs);
    }
    else
    {
      setOrder(align, seqs);
    }

    sortIdAscending = !sortIdAscending;
  }

  /**
   * Sorts the alignment by size of group.
   * <br>Maintains the order of sequences in each group
   * by order in given alignment object.
   *
   * @param align sorts the given alignment object by group
   */
  public static void sortByGroup(AlignmentI align)
  {
    //MAINTAINS ORIGNAL SEQUENCE ORDER,
    //ORDERS BY GROUP SIZE
    Vector groups = new Vector();

    if (groups.hashCode() != lastGroupHash)
    {
      sortGroupAscending = true;
      lastGroupHash = groups.hashCode();
    }
    else
    {
      sortGroupAscending = !sortGroupAscending;
    }

    //SORTS GROUPS BY SIZE
    //////////////////////
    for (int i = 0; i < align.getGroups().size(); i++)
    {
      SequenceGroup sg = (SequenceGroup) align.getGroups().elementAt(i);

      for (int j = 0; j < groups.size(); j++)
      {
        SequenceGroup sg2 = (SequenceGroup) groups.elementAt(j);

        if (sg.getSize() > sg2.getSize())
        {
          groups.insertElementAt(sg, j);

          break;
        }
      }

      if (!groups.contains(sg))
      {
        groups.addElement(sg);
      }
    }

    //NOW ADD SEQUENCES MAINTAINING ALIGNMENT ORDER
    ///////////////////////////////////////////////
    Vector seqs = new Vector();

    for (int i = 0; i < groups.size(); i++)
    {
      SequenceGroup sg = (SequenceGroup) groups.elementAt(i);
      SequenceI[] orderedseqs = sg.getSequencesInOrder(align);

      for (int j = 0; j < orderedseqs.length; j++)
      {
        seqs.addElement(orderedseqs[j]);
      }
    }

    if (sortGroupAscending)
    {
      setOrder(align, seqs);
    }
    else
    {
      setReverseOrder(align,
                      vectorSubsetToArray(seqs, align.getSequences()));
    }
  }

  /**
   * Converts Vector to array.
   * java 1.18 does not have Vector.toArray()
   *
   * @param tmp Vector of SequenceI objects
   *
   * @return array of Sequence[]
   */
  private static SequenceI[] vectorToArray(Vector tmp)
  {
    SequenceI[] seqs = new SequenceI[tmp.size()];

    for (int i = 0; i < tmp.size(); i++)
    {
      seqs[i] = (SequenceI) tmp.elementAt(i);
    }

    return seqs;
  }

  /**
   * DOCUMENT ME!
   *
   * @param tmp DOCUMENT ME!
   * @param mask DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  private static SequenceI[] vectorSubsetToArray(Vector tmp, Vector mask)
  {
    Vector seqs = new Vector();
    int i;
    boolean[] tmask = new boolean[mask.size()];

    for (i = 0; i < mask.size(); i++)
    {
      tmask[i] = true;
    }

    for (i = 0; i < tmp.size(); i++)
    {
      Object sq = tmp.elementAt(i);

      if (mask.contains(sq) && tmask[mask.indexOf(sq)])
      {
        tmask[mask.indexOf(sq)] = false;
        seqs.addElement(sq);
      }
    }

    for (i = 0; i < tmask.length; i++)
    {
      if (tmask[i])
      {
        seqs.addElement(mask.elementAt(i));
      }
    }

    return vectorToArray(seqs);
  }

  /**
   * Sorts by a given AlignmentOrder object
   *
   * @param align Alignment to order
   * @param order specified order for alignment
   */
  public static void sortBy(AlignmentI align, AlignmentOrder order)
  {
    // Get an ordered vector of sequences which may also be present in align
    Vector tmp = order.getOrder();

    if (lastOrder == order)
    {
      sortOrderAscending = !sortOrderAscending;
    }
    else
    {
      sortOrderAscending = true;
    }

    if (sortOrderAscending)
    {
      setOrder(align, tmp);
    }
    else
    {
      setReverseOrder(align,
                      vectorSubsetToArray(tmp, align.getSequences()));
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param align alignment to order
   * @param tree tree which has
   *
   * @return DOCUMENT ME!
   */
  private static Vector getOrderByTree(AlignmentI align, NJTree tree)
  {
    int nSeq = align.getHeight();

    Vector tmp = new Vector();

    tmp = _sortByTree(tree.getTopNode(), tmp, align.getSequences());

    if (tmp.size() != nSeq)
    {
      // TODO: JBPNote - decide if this is always an error
      // (eg. not when a tree is associated to another alignment which has more
      //  sequences)
      if (tmp.size() < nSeq)
      {
        addStrays(align, tmp);
      }

      if (tmp.size() != nSeq)
      {
        System.err.println("ERROR: tmp.size()=" + tmp.size() +
                           " != nseq=" + nSeq + " in getOrderByTree");
      }
    }

    return tmp;
  }

  /**
   * Sorts the alignment by a given tree
   *
   * @param align alignment to order
   * @param tree tree which has
   */
  public static void sortByTree(AlignmentI align, NJTree tree)
  {
    Vector tmp = getOrderByTree(align, tree);

    // tmp should properly permute align with tree.
    if (lastTree != tree)
    {
      sortTreeAscending = true;
      lastTree = tree;
    }
    else
    {
      sortTreeAscending = !sortTreeAscending;
    }

    if (sortTreeAscending)
    {
      setOrder(align, tmp);
    }
    else
    {
      setReverseOrder(align,
                      vectorSubsetToArray(tmp, align.getSequences()));
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param align DOCUMENT ME!
   * @param seqs DOCUMENT ME!
   */
  private static void addStrays(AlignmentI align, Vector seqs)
  {
    int nSeq = align.getHeight();

    for (int i = 0; i < nSeq; i++)
    {
      if (!seqs.contains(align.getSequenceAt(i)))
      {
        seqs.addElement(align.getSequenceAt(i));
      }
    }

    if (nSeq != seqs.size())
    {
      System.err.println(
          "ERROR: Size still not right even after addStrays");
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   * @param tmp DOCUMENT ME!
   * @param seqset DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  private static Vector _sortByTree(SequenceNode node, Vector tmp,
                                    Vector seqset)
  {
    if (node == null)
    {
      return tmp;
    }

    SequenceNode left = (SequenceNode) node.left();
    SequenceNode right = (SequenceNode) node.right();

    if ( (left == null) && (right == null))
    {
      if (!node.isPlaceholder() && (node.element() != null))
      {
        if (node.element() instanceof SequenceI)
        {
          if (!tmp.contains(node.element()))
          {
            tmp.addElement( (SequenceI) node.element());
          }
        }
      }

      return tmp;
    }
    else
    {
      _sortByTree(left, tmp, seqset);
      _sortByTree(right, tmp, seqset);
    }

    return tmp;
  }

  // Ordering Objects
  // Alignment.sortBy(OrderObj) - sequence of sequence pointer refs in appropriate order
  //

  /**
   * recover the order of sequences given by the safe numbering scheme introducd
   * SeqsetUtils.uniquify.
   */
  public static void recoverOrder(SequenceI[] alignment)
  {
    float[] ids = new float[alignment.length];

    for (int i = 0; i < alignment.length; i++)
    {
      ids[i] = (new Float(alignment[i].getName().substring(8))).floatValue();
    }

    jalview.util.QuickSort.sort(ids, alignment);
  }
  /**
   * Sort sequence in order of increasing score attribute for annotation with a particular
   * scoreLabel. Or reverse if same label was used previously
   * @param scoreLabel exact label for sequence associated AlignmentAnnotation scores to use for sorting.
   * @param alignment sequences to be sorted
   */
  public static void sortByAnnotationScore(String scoreLabel, AlignmentI alignment)
  {
    SequenceI[] seqs = alignment.getSequencesArray();
    boolean[] hasScore = new boolean[seqs.length]; // per sequence score presence
    int hasScores=0; // number of scores present on set
    double[] scores = new double[seqs.length];
    double min=0,max=0;
    for (int i = 0; i < seqs.length; i++)
    {
      AlignmentAnnotation[] scoreAnn = seqs[i].getAnnotation(scoreLabel);
      if (scoreAnn!=null)
      {
        hasScores++;
        hasScore[i] = true;
        scores[i] = scoreAnn[0].getScore(); // take the first instance of this score.
        if (hasScores==1)
        {
          max = min = scores[i];
        } else
        {
          if (max<scores[i])
          {
            max = scores[i];
          }
          if (min>scores[i])
          {
            min = scores[i];
          }
        }
      }
      else
      {
        hasScore[i] = false;
      }
    }
    if (hasScores==0)
    {
      return; // do nothing - no scores present to sort by.
    }
    if (hasScores<seqs.length)
    {
      for (int i=0; i<seqs.length;i++)
      {
        if (!hasScore[i])
        {
          scores[i] = (max+i);
        }
      }
    }
    
    jalview.util.QuickSort.sort(scores, seqs);
    if (lastSortByScore!=scoreLabel)
    {
      lastSortByScore = scoreLabel;
      setOrder(alignment, seqs);
    } else {
      setReverseOrder(alignment, seqs);
    }
  }
}
