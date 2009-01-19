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

public class DBRefEntry
{
  String source="", version="", accessionId="";
  /**
   * maps from associated sequence to the database sequence's coordinate system
   */
  Mapping map=null;
  public DBRefEntry() {
      
  }
  public DBRefEntry(String source, String version, String accessionId)
  {
    this(source, version, accessionId, null);
  }
  public DBRefEntry(String source, String version, String accessionId, Mapping map) {
    this.source = source;
    this.version = version;
    this.accessionId = accessionId;
    this.map = map;
  }
  public DBRefEntry(DBRefEntry entry)
  {
    this(new String(entry.source), new String(entry.version), new String(entry.accessionId), new Mapping(entry.map));
  }
  public boolean equals(DBRefEntry entry) {
      if (entry==this)
          return true;
      if (entry==null)
          return false;
      if ((source!=null && entry.source!=null && source.equals(entry.source))
          &&
          (accessionId!=null && entry.accessionId!=null && accessionId.equals(entry.accessionId))
          &&
          (version!=null && entry.version!=null && version.equals(entry.version))
          &&
          ((map==null && entry.map==null) || (map!=null && entry.map!=null && map.equals(entry.map)))) {
              return true;
          }
      return false;
  }
  public String getSource()
  {
    return source;
  }

  public String getVersion()
  {
    return version;
  }

  public String getAccessionId()
  {
    return accessionId;
  }
/**
 * @param accessionId the accessionId to set
 */
public void setAccessionId(String accessionId) {
    this.accessionId = accessionId;
}
/**
 * @param source the source to set
 */
public void setSource(String source) {
    this.source = source;
}
/**
 * @param version the version to set
 */
public void setVersion(String version) {
    this.version = version;
}
/**
 * @return the map
 */
public Mapping getMap() {
    return map;
}
/**
 * @param map the map to set
 */
public void setMap(Mapping map) {
    this.map = map;
}
}
