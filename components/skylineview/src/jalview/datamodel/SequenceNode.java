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

import java.awt.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SequenceNode
    extends BinaryNode
{
  /** DOCUMENT ME!! */
  public float dist;

  /** DOCUMENT ME!! */
  public int count;

  /** DOCUMENT ME!! */
  public float height;

  /** DOCUMENT ME!! */
  public float ycount;

  /** DOCUMENT ME!! */
  public Color color = Color.black;

  /** DOCUMENT ME!! */
  public boolean dummy = false;
  private boolean placeholder = false;

  /**
   * Creates a new SequenceNode object.
   */
  public SequenceNode()
  {
    super();
  }

  /**
   * Creates a new SequenceNode object.
   *
   * @param val DOCUMENT ME!
   * @param parent DOCUMENT ME!
   * @param dist DOCUMENT ME!
   * @param name DOCUMENT ME!
   */
  public SequenceNode(Object val, SequenceNode parent, float dist, String name)
  {
    super(val, parent, name);
    this.dist = dist;
  }

  /**
   * Creates a new SequenceNode object.
   *
   * @param val DOCUMENT ME!
   * @param parent DOCUMENT ME!
   * @param name DOCUMENT ME!
   * @param dist DOCUMENT ME!
   * @param bootstrap DOCUMENT ME!
   * @param dummy DOCUMENT ME!
   */
  public SequenceNode(Object val, SequenceNode parent, String name,
                      float dist, int bootstrap, boolean dummy)
  {
    super(val, parent, name);
    this.dist = dist;
    this.bootstrap = bootstrap;
    this.dummy = dummy;
  }

  /**
   * @param dummy true if node is created for the representation of polytomous trees
   */
  public boolean isDummy()
  {
    return dummy;
  }

  /* @param placeholder is true if the sequence refered to in the
   *  element node is not actually present in the associated alignment
   */
  public boolean isPlaceholder()
  {
    return placeholder;
  }

  /**
   * DOCUMENT ME!
   *
   * @param newstate DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public boolean setDummy(boolean newstate)
  {
    boolean oldstate = dummy;
    dummy = newstate;

    return oldstate;
  }

  /**
   * DOCUMENT ME!
   *
   * @param Placeholder DOCUMENT ME!
   */
  public void setPlaceholder(boolean Placeholder)
  {
    this.placeholder = Placeholder;
  }

  /**
   * ascends the tree but doesn't stop until a non-dummy node is discovered.
   * This will probably break if the tree is a mixture of BinaryNodes and SequenceNodes.
   */
  public SequenceNode AscendTree()
  {
    SequenceNode c = this;

    do
    {
      c = (SequenceNode) c.parent();
    }
    while ( (c != null) && c.dummy);

    return c;
  }
}
