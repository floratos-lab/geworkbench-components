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
import jalview.io.*;
import jalview.schemes.*;
import jalview.util.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class NJTree
{
  Vector cluster;
  SequenceI[] sequence;

  //SequenceData is a string representation of what the user
  //sees. The display may contain hidden columns.
  public AlignmentView seqData = null;

  int[] done;
  int noseqs;
  int noClus;
  float[][] distance;
  int mini;
  int minj;
  float ri;
  float rj;
  Vector groups = new Vector();
  SequenceNode maxdist;
  SequenceNode top;
  float maxDistValue;
  float maxheight;
  int ycount;
  Vector node;
  String type;
  String pwtype;
  Object found = null;
  Object leaves = null;

  boolean hasDistances = true; // normal case for jalview trees
  boolean hasBootstrap = false; // normal case for jalview trees

  private boolean hasRootDistance = true;

  /**
   * Create a new NJTree object with leaves associated with sequences in seqs,
   * and original alignment data represented by Cigar strings.
   * @param seqs SequenceI[]
   * @param odata Cigar[]
   * @param treefile NewickFile
   */
  public NJTree(SequenceI[] seqs, AlignmentView odata, NewickFile treefile)
  {
    this(seqs, treefile);
    if (odata != null)
    {
      seqData = odata;
    }
    /*
           sequenceString = new String[odata.length];
           char gapChar = jalview.util.Comparison.GapChars.charAt(0);
           for (int i = 0; i < odata.length; i++)
           {
      SequenceI oseq_aligned = odata[i].getSeq(gapChar);
        sequenceString[i] = oseq_aligned.getSequence();
           } */
  }

  /**
   * Creates a new NJTree object from a tree from an external source
   *
   * @param seqs SequenceI which should be associated with leafs of treefile
   * @param treefile A parsed tree
   */
  public NJTree(SequenceI[] seqs, NewickFile treefile)
  {
    this.sequence = seqs;
    top = treefile.getTree();

    /**
     * There is no dependent alignment to be recovered from an
     * imported tree.
     *
             if (sequenceString == null)
             {
      sequenceString = new String[seqs.length];
      for (int i = 0; i < seqs.length; i++)
      {
        sequenceString[i] = seqs[i].getSequence();
      }
             }
     */

    hasDistances = treefile.HasDistances();
    hasBootstrap = treefile.HasBootstrap();
    hasRootDistance = treefile.HasRootDistance();

    maxheight = findHeight(top);

    SequenceIdMatcher algnIds = new SequenceIdMatcher(seqs);

    Vector leaves = new Vector();
    findLeaves(top, leaves);

    int i = 0;
    int namesleft = seqs.length;

    SequenceNode j;
    SequenceI nam;
    String realnam;
    Vector one2many = new Vector();
    int countOne2Many = 0;
    while (i < leaves.size())
    {
      j = (SequenceNode) leaves.elementAt(i++);
      realnam = j.getName();
      nam = null;

      if (namesleft > -1)
      {
        nam = algnIds.findIdMatch(realnam);
      }

      if (nam != null)
      {
        j.setElement(nam);
        if (one2many.contains(nam))
        {
          countOne2Many++;
          //  if (jalview.bin.Cache.log.isDebugEnabled())
          //    jalview.bin.Cache.log.debug("One 2 many relationship for "+nam.getName());
        }
        else
        {
          one2many.addElement(nam);
          namesleft--;
        }
      }
      else
      {
        j.setElement(new Sequence(realnam, "THISISAPLACEHLDER"));
        j.setPlaceholder(true);
      }
    }
    //  if (jalview.bin.Cache.log.isDebugEnabled() && countOne2Many>0) {
    //    jalview.bin.Cache.log.debug("There were "+countOne2Many+" alignment sequence ids (out of "+one2many.size()+" unique ids) linked to two or more leaves.");
    //  }
    //  one2many.clear();
  }

  /**
   * Creates a new NJTree object.
   *
   * @param sequence DOCUMENT ME!
   * @param type DOCUMENT ME!
   * @param pwtype DOCUMENT ME!
   * @param start DOCUMENT ME!
   * @param end DOCUMENT ME!
   */
  public NJTree(SequenceI[] sequence,
                AlignmentView seqData,
                String type,
                String pwtype,
                int start, int end)
  {
    this.sequence = sequence;
    this.node = new Vector();
    this.type = type;
    this.pwtype = pwtype;
    if (seqData != null)
    {
      this.seqData = seqData;
    }
    else
    {
      SeqCigar[] seqs = new SeqCigar[sequence.length];
      for (int i = 0; i < sequence.length; i++)
      {
        seqs[i] = new SeqCigar(sequence[i], start, end);
      }
      CigarArray sdata = new CigarArray(seqs);
      sdata.addOperation(CigarArray.M, end - start + 1);
      this.seqData = new AlignmentView(sdata, start);
    }

    if (! (type.equals("NJ")))
    {
      type = "AV";
    }

    if (! (pwtype.equals("PID")))
    {
      if (ResidueProperties.getScoreMatrix(pwtype) == null)
      {
        type = "BLOSUM62";
      }
    }

    int i = 0;

    done = new int[sequence.length];

    while ( (i < sequence.length) && (sequence[i] != null))
    {
      done[i] = 0;
      i++;
    }

    noseqs = i++;

    distance = findDistances(this.seqData.getSequenceStrings(Comparison.
        GapChars.charAt(0)));

    makeLeaves();

    noClus = cluster.size();

    cluster();
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String toString()
  {
    jalview.io.NewickFile fout = new jalview.io.NewickFile(getTopNode());

    return fout.print(false, true); // distances only
  }

  /**
   *
   * used when the alignment associated to a tree has changed.
   *
   * @param alignment Vector
   */
  public void UpdatePlaceHolders(Vector alignment)
  {
    Vector leaves = new Vector();
    findLeaves(top, leaves);

    int sz = leaves.size();
    SequenceIdMatcher seqmatcher = null;
    int i = 0;

    while (i < sz)
    {
      SequenceNode leaf = (SequenceNode) leaves.elementAt(i++);

      if (alignment.contains(leaf.element()))
      {
        leaf.setPlaceholder(false);
      }
      else
      {
        if (seqmatcher == null)
        {
          // Only create this the first time we need it
          SequenceI[] seqs = new SequenceI[alignment.size()];

          for (int j = 0; j < seqs.length; j++)
          {
            seqs[j] = (SequenceI) alignment.elementAt(j);
          }

          seqmatcher = new SequenceIdMatcher(seqs);
        }

        SequenceI nam = seqmatcher.findIdMatch(leaf.getName());

        if (nam != null)
        {
          if (!leaf.isPlaceholder())
          {
            // remapping the node to a new sequenceI - should remove any refs to old one.
            // TODO - make many sequenceI to one leaf mappings possible! (JBPNote)
          }
          leaf.setPlaceholder(false);
          leaf.setElement(nam);
        }
        else
        {
          if (!leaf.isPlaceholder())
          {
            // Construct a new placeholder sequence object for this leaf
            leaf.setElement(new Sequence(leaf.getName(), "THISISAPLACEHLDER"));
          }
          leaf.setPlaceholder(true);

        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   */
  public void cluster()
  {
    while (noClus > 2)
    {
      if (type.equals("NJ"))
      {
        findMinNJDistance();
      }
      else
      {
        findMinDistance();
      }

      Cluster c = joinClusters(mini, minj);

      done[minj] = 1;

      cluster.setElementAt(null, minj);
      cluster.setElementAt(c, mini);

      noClus--;
    }

    boolean onefound = false;

    int one = -1;
    int two = -1;

    for (int i = 0; i < noseqs; i++)
    {
      if (done[i] != 1)
      {
        if (onefound == false)
        {
          two = i;
          onefound = true;
        }
        else
        {
          one = i;
        }
      }
    }

    joinClusters(one, two);
    top = (SequenceNode) (node.elementAt(one));

    reCount(top);
    findHeight(top);
    findMaxDist(top);
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Cluster joinClusters(int i, int j)
  {
    float dist = distance[i][j];

    int noi = ( (Cluster) cluster.elementAt(i)).value.length;
    int noj = ( (Cluster) cluster.elementAt(j)).value.length;

    int[] value = new int[noi + noj];

    for (int ii = 0; ii < noi; ii++)
    {
      value[ii] = ( (Cluster) cluster.elementAt(i)).value[ii];
    }

    for (int ii = noi; ii < (noi + noj); ii++)
    {
      value[ii] = ( (Cluster) cluster.elementAt(j)).value[ii - noi];
    }

    Cluster c = new Cluster(value);

    ri = findr(i, j);
    rj = findr(j, i);

    if (type.equals("NJ"))
    {
      findClusterNJDistance(i, j);
    }
    else
    {
      findClusterDistance(i, j);
    }

    SequenceNode sn = new SequenceNode();

    sn.setLeft( (SequenceNode) (node.elementAt(i)));
    sn.setRight( (SequenceNode) (node.elementAt(j)));

    SequenceNode tmpi = (SequenceNode) (node.elementAt(i));
    SequenceNode tmpj = (SequenceNode) (node.elementAt(j));

    if (type.equals("NJ"))
    {
      findNewNJDistances(tmpi, tmpj, dist);
    }
    else
    {
      findNewDistances(tmpi, tmpj, dist);
    }

    tmpi.setParent(sn);
    tmpj.setParent(sn);

    node.setElementAt(sn, i);

    return c;
  }

  /**
   * DOCUMENT ME!
   *
   * @param tmpi DOCUMENT ME!
   * @param tmpj DOCUMENT ME!
   * @param dist DOCUMENT ME!
   */
  public void findNewNJDistances(SequenceNode tmpi, SequenceNode tmpj,
                                 float dist)
  {

    tmpi.dist = ( (dist + ri) - rj) / 2;
    tmpj.dist = (dist - tmpi.dist);

    if (tmpi.dist < 0)
    {
      tmpi.dist = 0;
    }

    if (tmpj.dist < 0)
    {
      tmpj.dist = 0;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param tmpi DOCUMENT ME!
   * @param tmpj DOCUMENT ME!
   * @param dist DOCUMENT ME!
   */
  public void findNewDistances(SequenceNode tmpi, SequenceNode tmpj,
                               float dist)
  {
    float ih = 0;
    float jh = 0;

    SequenceNode sni = tmpi;
    SequenceNode snj = tmpj;

    while (sni != null)
    {
      ih = ih + sni.dist;
      sni = (SequenceNode) sni.left();
    }

    while (snj != null)
    {
      jh = jh + snj.dist;
      snj = (SequenceNode) snj.left();
    }

    tmpi.dist = ( (dist / 2) - ih);
    tmpj.dist = ( (dist / 2) - jh);
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   */
  public void findClusterDistance(int i, int j)
  {
    int noi = ( (Cluster) cluster.elementAt(i)).value.length;
    int noj = ( (Cluster) cluster.elementAt(j)).value.length;

    // New distances from cluster to others
    float[] newdist = new float[noseqs];

    for (int l = 0; l < noseqs; l++)
    {
      if ( (l != i) && (l != j))
      {
        newdist[l] = ( (distance[i][l] * noi) + (distance[j][l] * noj)) / (noi +
            noj);
      }
      else
      {
        newdist[l] = 0;
      }
    }

    for (int ii = 0; ii < noseqs; ii++)
    {
      distance[i][ii] = newdist[ii];
      distance[ii][i] = newdist[ii];
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   */
  public void findClusterNJDistance(int i, int j)
  {

    // New distances from cluster to others
    float[] newdist = new float[noseqs];

    for (int l = 0; l < noseqs; l++)
    {
      if ( (l != i) && (l != j))
      {
        newdist[l] = ( (distance[i][l] + distance[j][l]) -
                      distance[i][j]) / 2;
      }
      else
      {
        newdist[l] = 0;
      }
    }

    for (int ii = 0; ii < noseqs; ii++)
    {
      distance[i][ii] = newdist[ii];
      distance[ii][i] = newdist[ii];
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param i DOCUMENT ME!
   * @param j DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float findr(int i, int j)
  {
    float tmp = 1;

    for (int k = 0; k < noseqs; k++)
    {
      if ( (k != i) && (k != j) && (done[k] != 1))
      {
        tmp = tmp + distance[i][k];
      }
    }

    if (noClus > 2)
    {
      tmp = tmp / (noClus - 2);
    }

    return tmp;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float findMinNJDistance()
  {
    float min = 100000;

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i + 1; j < noseqs; j++)
      {
        if ( (done[i] != 1) && (done[j] != 1))
        {
          float tmp = distance[i][j] - (findr(i, j) + findr(j, i));

          if (tmp < min)
          {
            mini = i;
            minj = j;

            min = tmp;
          }
        }
      }
    }

    return min;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float findMinDistance()
  {
    float min = 100000;

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i + 1; j < noseqs; j++)
      {
        if ( (done[i] != 1) && (done[j] != 1))
        {
          if (distance[i][j] < min)
          {
            mini = i;
            minj = j;

            min = distance[i][j];
          }
        }
      }
    }

    return min;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float[][] findDistances(String[] sequenceString)
  {
    float[][] distance = new float[noseqs][noseqs];

    if (pwtype.equals("PID"))
    {
      for (int i = 0; i < (noseqs - 1); i++)
      {
        for (int j = i; j < noseqs; j++)
        {
          if (j == i)
          {
            distance[i][i] = 0;
          }
          else
          {
            distance[i][j] = 100 -
                Comparison.PID(sequenceString[i], sequenceString[j]);

            distance[j][i] = distance[i][j];
          }
        }
      }
    }
    else
    {
      // Pairwise substitution score (with no gap penalties)
      ScoreMatrix pwmatrix = ResidueProperties.getScoreMatrix(pwtype);
      if (pwmatrix == null)
      {
        pwmatrix = ResidueProperties.getScoreMatrix("BLOSUM62");
      }
      int maxscore = 0;
      int end = sequenceString[0].length();
      for (int i = 0; i < (noseqs - 1); i++)
      {
        for (int j = i; j < noseqs; j++)
        {
          int score = 0;

          for (int k = 0; k < end; k++)
          {
            try
            {
              score += pwmatrix.getPairwiseScore(sequenceString[i].charAt(k),
                                                 sequenceString[j].charAt(k));
            }
            catch (Exception ex)
            {
              System.err.println("err creating BLOSUM62 tree");
              ex.printStackTrace();
            }
          }

          distance[i][j] = (float) score;

          if (score > maxscore)
          {
            maxscore = score;
          }
        }
      }

      for (int i = 0; i < (noseqs - 1); i++)
      {
        for (int j = i; j < noseqs; j++)
        {
          distance[i][j] = (float) maxscore - distance[i][j];
          distance[j][i] = distance[i][j];
        }
      }

    }
    return distance;

    //   else
    /*  else if (pwtype.equals("SW"))
      {
          float max = -1;

          for (int i = 0; i < (noseqs - 1); i++)
          {
              for (int j = i; j < noseqs; j++)
              {
                  AlignSeq as = new AlignSeq(sequence[i], sequence[j], "pep");
                  as.calcScoreMatrix();
                  as.traceAlignment();
                  as.printAlignment(System.out);
                  distance[i][j] = (float) as.maxscore;

                  if (max < distance[i][j])
                  {
                      max = distance[i][j];
                  }
              }
          }

          for (int i = 0; i < (noseqs - 1); i++)
          {
              for (int j = i; j < noseqs; j++)
              {
                  distance[i][j] = max - distance[i][j];
                  distance[j][i] = distance[i][j];
              }
          }
      }/*/
  }

  /**
   * DOCUMENT ME!
   */
  public void makeLeaves()
  {
    cluster = new Vector();

    for (int i = 0; i < noseqs; i++)
    {
      SequenceNode sn = new SequenceNode();

      sn.setElement(sequence[i]);
      sn.setName(sequence[i].getName());
      node.addElement(sn);

      int[] value = new int[1];
      value[0] = i;

      Cluster c = new Cluster(value);
      cluster.addElement(c);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   * @param leaves DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Vector findLeaves(SequenceNode node, Vector leaves)
  {
    if (node == null)
    {
      return leaves;
    }

    if ( (node.left() == null) && (node.right() == null))
    {
      leaves.addElement(node);

      return leaves;
    }
    else
    {
      findLeaves( (SequenceNode) node.left(), leaves);
      findLeaves( (SequenceNode) node.right(), leaves);
    }

    return leaves;
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   * @param count DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Object findLeaf(SequenceNode node, int count)
  {
    found = _findLeaf(node, count);

    return found;
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   * @param count DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Object _findLeaf(SequenceNode node, int count)
  {
    if (node == null)
    {
      return null;
    }

    if (node.ycount == count)
    {
      found = node.element();

      return found;
    }
    else
    {
      _findLeaf( (SequenceNode) node.left(), count);
      _findLeaf( (SequenceNode) node.right(), count);
    }

    return found;
  }

  /**
   * printNode is mainly for debugging purposes.
   *
   * @param node SequenceNode
   */
  public void printNode(SequenceNode node)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.left() == null) && (node.right() == null))
    {
      System.out.println("Leaf = " +
                         ( (SequenceI) node.element()).getName());
      System.out.println("Dist " + ( (SequenceNode) node).dist);
      System.out.println("Boot " + node.getBootstrap());
    }
    else
    {
      System.out.println("Dist " + ( (SequenceNode) node).dist);
      printNode( (SequenceNode) node.left());
      printNode( (SequenceNode) node.right());
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   */
  public void findMaxDist(SequenceNode node)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.left() == null) && (node.right() == null))
    {
      float dist = ( (SequenceNode) node).dist;

      if (dist > maxDistValue)
      {
        maxdist = (SequenceNode) node;
        maxDistValue = dist;
      }
    }
    else
    {
      findMaxDist( (SequenceNode) node.left());
      findMaxDist( (SequenceNode) node.right());
    }
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

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float getMaxHeight()
  {
    return maxheight;
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   * @param threshold DOCUMENT ME!
   */
  public void groupNodes(SequenceNode node, float threshold)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.height / maxheight) > threshold)
    {
      groups.addElement(node);
    }
    else
    {
      groupNodes( (SequenceNode) node.left(), threshold);
      groupNodes( (SequenceNode) node.right(), threshold);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public float findHeight(SequenceNode node)
  {
    if (node == null)
    {
      return maxheight;
    }

    if ( (node.left() == null) && (node.right() == null))
    {
      node.height = ( (SequenceNode) node.parent()).height + node.dist;

      if (node.height > maxheight)
      {
        return node.height;
      }
      else
      {
        return maxheight;
      }
    }
    else
    {
      if (node.parent() != null)
      {
        node.height = ( (SequenceNode) node.parent()).height +
            node.dist;
      }
      else
      {
        maxheight = 0;
        node.height = (float) 0.0;
      }

      maxheight = findHeight( (SequenceNode) (node.left()));
      maxheight = findHeight( (SequenceNode) (node.right()));
    }

    return maxheight;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceNode reRoot()
  {
    if (maxdist != null)
    {
      ycount = 0;

      float tmpdist = maxdist.dist;

      // New top
      SequenceNode sn = new SequenceNode();
      sn.setParent(null);

      // New right hand of top
      SequenceNode snr = (SequenceNode) maxdist.parent();
      changeDirection(snr, maxdist);
      System.out.println("Printing reversed tree");
      printN(snr);
      snr.dist = tmpdist / 2;
      maxdist.dist = tmpdist / 2;

      snr.setParent(sn);
      maxdist.setParent(sn);

      sn.setRight(snr);
      sn.setLeft(maxdist);

      top = sn;

      ycount = 0;
      reCount(top);
      findHeight(top);
    }

    return top;
  }

  /**
   *
   * @return true if original sequence data can be recovered
   */
  public boolean hasOriginalSequenceData()
  {
    return seqData != null;
  }

  /**
   * Returns original alignment data used for calculation - or null where
   * not available.
   *
   * @return null or cut'n'pasteable alignment
   */
  public String printOriginalSequenceData(char gapChar)
  {
    if (seqData == null)
    {
      return null;
    }

    StringBuffer sb = new StringBuffer();
    String[] seqdatas = seqData.getSequenceStrings(gapChar);
    for (int i = 0; i < seqdatas.length; i++)
    {
      sb.append(new jalview.util.Format("%-" + 15 + "s").form(
          sequence[i].getName()));
      sb.append(" " + seqdatas[i] + "\n");
    }
    return sb.toString();
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   */
  public void printN(SequenceNode node)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.left() != null) && (node.right() != null))
    {
      printN( (SequenceNode) node.left());
      printN( (SequenceNode) node.right());
    }
    else
    {
      System.out.println(" name = " +
                         ( (SequenceI) node.element()).getName());
    }

    System.out.println(" dist = " + ( (SequenceNode) node).dist + " " +
                       ( (SequenceNode) node).count + " " +
                       ( (SequenceNode) node).height);
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   */
  public void reCount(SequenceNode node)
  {
    ycount = 0;
    _reCount(node);
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   */
  public void _reCount(SequenceNode node)
  {
    if (node == null)
    {
      return;
    }

    if ( (node.left() != null) && (node.right() != null))
    {
      _reCount( (SequenceNode) node.left());
      _reCount( (SequenceNode) node.right());

      SequenceNode l = (SequenceNode) node.left();
      SequenceNode r = (SequenceNode) node.right();

      ( (SequenceNode) node).count = l.count + r.count;
      ( (SequenceNode) node).ycount = (l.ycount + r.ycount) / 2;
    }
    else
    {
      ( (SequenceNode) node).count = 1;
      ( (SequenceNode) node).ycount = ycount++;
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   */
  public void swapNodes(SequenceNode node)
  {
    if (node == null)
    {
      return;
    }

    SequenceNode tmp = (SequenceNode) node.left();

    node.setLeft(node.right());
    node.setRight(tmp);
  }

  /**
   * DOCUMENT ME!
   *
   * @param node DOCUMENT ME!
   * @param dir DOCUMENT ME!
   */
  public void changeDirection(SequenceNode node, SequenceNode dir)
  {
    if (node == null)
    {
      return;
    }

    if (node.parent() != top)
    {
      changeDirection( (SequenceNode) node.parent(), node);

      SequenceNode tmp = (SequenceNode) node.parent();

      if (dir == node.left())
      {
        node.setParent(dir);
        node.setLeft(tmp);
      }
      else if (dir == node.right())
      {
        node.setParent(dir);
        node.setRight(tmp);
      }
    }
    else
    {
      if (dir == node.left())
      {
        node.setParent(node.left());

        if (top.left() == node)
        {
          node.setRight(top.right());
        }
        else
        {
          node.setRight(top.left());
        }
      }
      else
      {
        node.setParent(node.right());

        if (top.left() == node)
        {
          node.setLeft(top.right());
        }
        else
        {
          node.setLeft(top.left());
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceNode getMaxDist()
  {
    return maxdist;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public SequenceNode getTopNode()
  {
    return top;
  }

  /**
   *
   * @return true if tree has real distances
   */
  public boolean isHasDistances()
  {
    return hasDistances;
  }

  /**
   *
   * @return true if tree has real bootstrap values
   */
  public boolean isHasBootstrap()
  {
    return hasBootstrap;
  }

  public boolean isHasRootDistance()
  {
    return hasRootDistance;
  }

}

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
class Cluster
{
  int[] value;

  /**
   * Creates a new Cluster object.
   *
   * @param value DOCUMENT ME!
   */
  public Cluster(int[] value)
  {
    this.value = value;
  }
}
