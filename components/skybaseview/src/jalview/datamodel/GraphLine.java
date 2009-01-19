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

public class GraphLine
{
  public float value;
  public String label = "";
  public java.awt.Color colour = java.awt.Color.black;
  public boolean displayed = true;

  public GraphLine(float value, String label, java.awt.Color col)
  {
    this.value = value;
    if (label != null)
    {
      this.label = label;
    }

    if (col != null)
    {
      this.colour = col;
    }
  }
  public GraphLine(GraphLine from) {
    if (from!=null) {
      value = from.value;
      label = new String(from.label);
      colour = from.colour;
      displayed = from.displayed;
    }
  }
}
