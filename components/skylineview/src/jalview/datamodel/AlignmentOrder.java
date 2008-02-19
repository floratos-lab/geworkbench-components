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
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: Dundee University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AlignmentOrder
{
  // JBPNote : this method would return a vector containing all sequences in seqset
  // with those also contained in order at the beginning of the vector in the order
  // given by order. AlignmentSorter.vectorSubsetToArray already does this, but that method
  // should be here for completeness.

  /*  public Vector getOrder(AlignmentI seqset)
     {
       Vector perm = new Vector(seqset.getHeight());
       for (i=0, o = 0, n=seqset.getHeight(), p = Order.size(); i<n; i++)
    perm.setElement(i,...).
       return Order;
     }
   */

  /** DOCUMENT ME!! */
  public static final int FILE = 0;

  /** DOCUMENT ME!! */
  public static final int MSA = 1;

  /** DOCUMENT ME!! */
  public static final int USER = 2;
  private int Type = 0;
  private String Name;
  private Vector Order = null;

  /**
   * Creates a new AlignmentOrder object.
   */
  public AlignmentOrder()
  {
  }

  /**
   * AlignmentOrder
   *
   * @param anOrder Vector
   */
  public AlignmentOrder(Vector anOrder)
  {
    Order = anOrder;
  }

  /**
   * AlignmentOrder
   *
   * @param orderFrom AlignmentI
   */
  public AlignmentOrder(AlignmentI orderFrom)
  {
    Order = new Vector();

    for (int i = 0, ns = orderFrom.getHeight(); i < ns; i++)
    {
      Order.addElement(orderFrom.getSequenceAt(i));
    }
  }

  /**
   * Creates a new AlignmentOrder object.
   *
   * @param orderFrom DOCUMENT ME!
   */
  public AlignmentOrder(SequenceI[] orderFrom)
  {
    Order = new Vector();

    for (int i = 0, ns = orderFrom.length; i < ns; i++)
    {
      Order.addElement(orderFrom[i]);
    }
  }

  /**
   * DOCUMENT ME!
   *
   * @param Type DOCUMENT ME!
   */
  public void setType(int Type)
  {
    this.Type = Type;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getType()
  {
    return Type;
  }

  /**
   * DOCUMENT ME!
   *
   * @param Name DOCUMENT ME!
   */
  public void setName(String Name)
  {
    this.Name = Name;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getName()
  {
    return Name;
  }

  /**
   * DOCUMENT ME!
   *
   * @param Order DOCUMENT ME!
   */
  public void setOrder(Vector Order)
  {
    this.Order = Order;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Vector getOrder()
  {
    return Order;
  }

  /**
   * replaces oldref with newref in the alignment order.
   * @param oldref
   * @param newref
   * @return true if oldref was contained in order and replaced with newref
   */
  public boolean updateSequence(SequenceI oldref, SequenceI newref)
  {
    int found = Order.indexOf(oldref);
    if (found > -1)
    {
      Order.setElementAt(newref, found);
    }
    return found > -1;
  }

  /**
   * Exact equivalence of two AlignmentOrders
   * @param o
   * @return true if o orders the same sequenceI objects in the same way
   */
  public boolean equals(AlignmentOrder o)
  {
    return equals(o, true);
  }

  /**
   * Exact equivalence of two AlignmentOrders
   *  // TODO: Weak SequenceI equivalence - will throw Error at moment
   * @param o
   * @param identity - false - use weak equivalence (refers to same or different parts of same sequence)
   * @return true if o orders equivalent sequenceI objects in the same way
   */
  public boolean equals(AlignmentOrder o, boolean identity)
  {
    if (o != this)
    {
      if (o == null)
      {
        return false;
      }
      if (Order != null && o.Order != null && Order.size() == o.Order.size())
      {
        if (!identity)
        {
          throw new Error("Weak sequenceI equivalence not yet implemented.");
        }
        else
        {
          for (int i = 0, j = o.Order.size(); i < j; i++)
          {
            if (Order.elementAt(i) != o.Order.elementAt(i))
            {
              return false;
            }
          }
        }
      }
      else
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Consistency test for alignmentOrders
   * @param o
   * @return true if o contains or is contained by this and the common SequenceI objects are ordered in the same way
   */
  public boolean isConsistent(AlignmentOrder o)
  {
    return isConsistent(o, true);
  }

  /**
   * Consistency test for alignmentOrders
   * @param o
   *  // TODO: Weak SequenceI equivalence - will throw Error at moment
   * @param identity - false - use weak equivalence (refers to same or different parts of same sequence)
   * @return true if o contains or is contained by this and the common SequenceI objects are ordered in the same way
   */
  public boolean isConsistent(AlignmentOrder o, boolean identity)
  {
    if (o != this)
    {
      if (o == null)
      {
        return false;
      }
      if (Order != null && o.Order != null)
      {
        Vector c, s;
        if (o.Order.size() > Order.size())
        {
          c = o.Order;
          s = Order;
        }
        else
        {
          c = Order;
          s = o.Order;
        }
        if (!identity)
        {
          throw new Error("Weak sequenceI equivalence not yet implemented.");
        }
        else
        {
          // test if c contains s and order in s is conserved in c
          int last = -1;
          for (int i = 0, j = s.size(); i < j; i++)
          {
            int pos = c.indexOf(s.elementAt(i)); // JBPNote - optimize by incremental position search
            if (pos > last)
            {
              last = pos;
            }
            else
            {
              return false;
            }
          }
        }
      }
      else
      {
        return false;
      }
    }
    return true;
  }
  /**
   * AlignmentOrder
   *
   * @param orderThis AlignmentI
   * @param byThat AlignmentI
   */

  /* public AlignmentOrder(AlignmentI orderThis, AlignmentI byThat)
   {
     // Vector is an ordering of this alignment using the order of sequence objects in byThat,
     // where ids and unaligned sequences must match

   } */
}
