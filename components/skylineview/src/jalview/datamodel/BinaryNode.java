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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class BinaryNode
{
  Object element;
  String name;
  BinaryNode left;
  BinaryNode right;
  BinaryNode parent;

  /** DOCUMENT ME!! */
  public int bootstrap;

  /**
   * Creates a new BinaryNode object.
   */
  public BinaryNode()
  {
    left = right = parent = null;
    bootstrap = 0;
  }

  /**
   * Creates a new BinaryNode object.
   *
   * @param element DOCUMENT ME!
   * @param parent DOCUMENT ME!
   * @param name DOCUMENT ME!
   */
  public BinaryNode(Object element, BinaryNode parent, String name)
  {
    this.element = element;
    this.parent = parent;
    this.name = name;

    left = right = null;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Object element()
  {
    return element;
  }

  /**
   * DOCUMENT ME!
   *
   * @param v DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public Object setElement(Object v)
  {
    return element = v;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public BinaryNode left()
  {
    return left;
  }

  /**
   * DOCUMENT ME!
   *
   * @param n DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public BinaryNode setLeft(BinaryNode n)
  {
    return left = n;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public BinaryNode right()
  {
    return right;
  }

  /**
   * DOCUMENT ME!
   *
   * @param n DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public BinaryNode setRight(BinaryNode n)
  {
    return right = n;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public BinaryNode parent()
  {
    return parent;
  }

  /**
   * DOCUMENT ME!
   *
   * @param n DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public BinaryNode setParent(BinaryNode n)
  {
    return parent = n;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean isLeaf()
  {
    return (left == null) && (right == null);
  }

  /**
   * attaches FIRST and SECOND node arguments as the LEFT and RIGHT children of this node (removing any old references)
   * a null parameter DOES NOT mean that the pointer to the corresponding child node is set to  NULL - you  should use
   * setChild(null), or detach() for this.
   *
   */
  public void SetChildren(BinaryNode leftchild, BinaryNode rightchild)
  {
    if (leftchild != null)
    {
      this.setLeft(leftchild);
      leftchild.detach();
      leftchild.setParent(this);
    }

    if (rightchild != null)
    {
      this.setRight(rightchild);
      rightchild.detach();
      rightchild.setParent(this);
    }
  }

  /**
   * Detaches the node from the binary tree, along with all its child nodes.
   * @return BinaryNode The detached node.
   */
  public BinaryNode detach()
  {
    if (this.parent != null)
    {
      if (this.parent.left == this)
      {
        this.parent.left = null;
      }
      else
      {
        if (this.parent.right == this)
        {
          this.parent.right = null;
        }
      }
    }

    this.parent = null;

    return this;
  }

  /**
   * Traverses up through the tree until a node with a free leftchild is discovered.
   * @return BinaryNode
   */
  public BinaryNode ascendLeft()
  {
    BinaryNode c = this;

    do
    {
      c = c.parent();
    }
    while ( (c != null) && (c.left() != null) && !c.left().isLeaf());

    return c;
  }

  /**
   * Traverses up through the tree until a node with a free rightchild is discovered.
   * Jalview builds trees by descent on the left, so this may be unused.
   * @return BinaryNode
   */
  public BinaryNode ascendRight()
  {
    BinaryNode c = this;

    do
    {
      c = c.parent();
    }
    while ( (c != null) && (c.right() != null) && !c.right().isLeaf());

    return c;
  }

  /**
   * DOCUMENT ME!
   *
   * @param name DOCUMENT ME!
   */
  public void setName(String name)
  {
    this.name = name;
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
   * @param boot DOCUMENT ME!
   */
  public void setBootstrap(int boot)
  {
    this.bootstrap = boot;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getBootstrap()
  {
    return bootstrap;
  }
}
