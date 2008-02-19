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
package MCview;

import java.util.*;

public class Residue
{
  Vector atoms = new Vector();
  int number;
  int count;
  int seqnumber;

  public Residue(Vector atoms, int number, int count)
  {
    this.atoms = atoms;
    this.number = number;
    this.count = count;
  }

  public Atom findAtom(String name)
  {
    for (int i = 0; i < atoms.size(); i++)
    {
      if ( ( (Atom) atoms.elementAt(i)).name.equals(name))
      {
        return (Atom) atoms.elementAt(i);
      }
    }

    return null;
  }
}
