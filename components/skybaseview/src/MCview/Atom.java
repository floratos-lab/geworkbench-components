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

import java.awt.*;

public class Atom
{
  public float x;
  public float y;
  public float z;
  public int number;
  public String name;
  public String resName;
  public int resNumber;
  public char insCode = ' ';
  public String resNumIns = null;
  public int type;
  Color color = Color.lightGray;
  public String chain;
  public int alignmentMapping = -1;
  public int atomIndex;
  public float occupancy=0;
  public float tfactor=0;
  public boolean isSelected = false;

  public Atom(String str)
  {
    atomIndex = Integer.parseInt(str.substring(6, 11).trim());

    name = str.substring(12, 15).trim();

    resName = str.substring(17, 20);

    chain = str.substring(21, 22);

    resNumber = Integer.parseInt(str.substring(22, 26).trim());
    resNumIns = str.substring(22, 27);
    insCode = str.substring(26, 27).charAt(0);
    this.x = (float) (new Float(str.substring(30, 38).trim()).floatValue());
    this.y = (float) (new Float(str.substring(38, 46).trim()).floatValue());
    this.z = (float) (new Float(str.substring(47, 55).trim()).floatValue());
    occupancy = (float) (new Float(str.substring(54,60).trim()).floatValue());
    tfactor = (float) (new Float(str.substring(60,66).trim()).floatValue());
  }

  public Atom(float x, float y, float z)
  {
    this.x = x;
    this.y = y;
    this.z = z;
  }
  //  public void setColor(Color col) {
  //      this.color = col;
  //  }
}
