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
public class Annotation
{
  /** DOCUMENT ME!! */
  public String displayCharacter = "";

  /** DOCUMENT ME!! */
  public String description = ""; // currently used as mouse over

  /** DOCUMENT ME!! */
  public char secondaryStructure = ' '; // recognises H and E

  /** DOCUMENT ME!! */
  public float value;

  // add visual cues here

  /** DOCUMENT ME!! */
  public Color colour;

  /**
   * Creates a new Annotation object.
   *
   * @param displayChar DOCUMENT ME!
   * @param desc DOCUMENT ME!
   * @param ss DOCUMENT ME!
   * @param val DOCUMENT ME!
   */
  public Annotation(String displayChar, String desc, char ss, float val)
  {
    displayCharacter = displayChar;
    description = desc;
    secondaryStructure = ss;
    value = val;
  }

  /**
   * Creates a new Annotation object.
   *
   * @param displayChar DOCUMENT ME!
   * @param desc DOCUMENT ME!
   * @param ss DOCUMENT ME!
   * @param val DOCUMENT ME!
   * @param colour DOCUMENT ME!
   */
  public Annotation(String displayChar, String desc, char ss, float val,
                    Color colour)
  {
    this(displayChar, desc, ss, val);
    this.colour = colour;
  }
  /**
   * Copy constructor
   * New annotation takes on the same (or duplicated) attributes as the given template
   * @param that template annotation
   */
  public Annotation(Annotation that) {
    if (that==null || this==that)
    {
      return;
    }
    if (that.displayCharacter!=null)
      displayCharacter = new String(that.displayCharacter);
    if (that.description!=null)
      description = new String(that.description);
    secondaryStructure = that.secondaryStructure;
    value = that.value;
    colour = that.colour;
  }

  /**
   * Value only annotation.
   * @param val value at this annotation position
   */
  public Annotation(float val)
  {
    this(null, null, ' ', val);
  }
}
