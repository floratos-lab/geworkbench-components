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

public class PDBEntry
{
  String file;
  String type;
  String id;
  Hashtable properties;

  public PDBEntry()
  {}
  public PDBEntry(PDBEntry entry) {
    file = entry.file;
    type = entry.type;
    id = entry.id;
    if (entry.properties!=null)
    {
      properties = (Hashtable) entry.properties.clone();
    }
  }
  public void setFile(String file)
  {
    this.file = file;
  }

  public String getFile()
  {
    return file;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getType()
  {
    return type;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getId()
  {
    return id;
  }

  public void setProperty(Hashtable property)
  {
    this.properties = property;
  }

  public Hashtable getProperty()
  {
    return properties;
  }

}
