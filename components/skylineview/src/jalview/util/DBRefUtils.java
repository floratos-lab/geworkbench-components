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
package jalview.util;

import java.util.*;

import jalview.datamodel.*;

public class DBRefUtils
{
  /**
   * Utilities for handling DBRef objects and their collections.
   */
  /**
   *
   * @param dbrefs Vector of DBRef objects to search
   * @param sources String[] array of source DBRef IDs to retrieve
   * @return Vector
   */
  public static DBRefEntry[] selectRefs(DBRefEntry[] dbrefs, String[] sources)
  {
    if (dbrefs == null)
    {
      return null;
    }
    if (sources == null)
    {
      return dbrefs;
    }
    Hashtable srcs = new Hashtable();
    Vector res = new Vector();

    for (int i = 0; i < sources.length; i++)
    {
      srcs.put(new String(sources[i]), new Integer(i));
    }
    for (int i = 0, j = dbrefs.length; i < j; i++)
    {
      if (srcs.containsKey(dbrefs[i].getSource()))
      {
        res.add(dbrefs[i]);
      }
    }

    if (res.size() > 0)
    {
      DBRefEntry[] reply = new DBRefEntry[res.size()];
      for (int i = 0; i < res.size(); i++)
      {
        reply[i] = (DBRefEntry) res.elementAt(i);
      }
      return reply;
    }
    res = null;
    // there are probable  memory leaks in the hashtable!
    return null;
  }

  /**
   * isDasCoordinateSystem
   *
   * @param string String
   * @param dBRefEntry DBRefEntry
   * @return boolean true if Source DBRefEntry is compatible with DAS CoordinateSystem name
   */
  public static Hashtable DasCoordinateSystemsLookup = null;
  public static boolean isDasCoordinateSystem(String string,
                                              DBRefEntry dBRefEntry)
  {
    if (DasCoordinateSystemsLookup == null)
    { // Initialise
      DasCoordinateSystemsLookup = new Hashtable();
      DasCoordinateSystemsLookup.put("pdbresnum",
                                     jalview.datamodel.DBRefSource.PDB);
      DasCoordinateSystemsLookup.put("uniprot",
                                     jalview.datamodel.DBRefSource.UNIPROT);
      DasCoordinateSystemsLookup.put("EMBL",
              jalview.datamodel.DBRefSource.EMBL);
      //DasCoordinateSystemsLookup.put("EMBL",
      //        jalview.datamodel.DBRefSource.EMBLCDS);
    }

    String coordsys = (String) DasCoordinateSystemsLookup.get(string.
        toLowerCase());
    if (coordsys != null)
    {
      return coordsys.equals(dBRefEntry.getSource());
    }
    return false;
  }
  public static Hashtable CanonicalSourceNameLookup=null;
  /**
   * look up source in an internal list of database reference sources
   * and return the canonical jalview name for the source, or the original
   * string if it has no canonical form.
   * @param source
   * @return canonical jalview source (one of jalview.datamodel.DBRefSource.*) or original source
   */
  public static String getCanonicalName(String source)
  {
    if (CanonicalSourceNameLookup==null) {
      CanonicalSourceNameLookup = new Hashtable();
      CanonicalSourceNameLookup.put("uniprotkb/swiss-prot", jalview.datamodel.DBRefSource.UNIPROT);
      CanonicalSourceNameLookup.put("pdb", jalview.datamodel.DBRefSource.PDB);
    }
    String canonical = (String) CanonicalSourceNameLookup.get(source.
        toLowerCase());
    if (canonical==null)
    {
      return source;
    }
    return canonical;
  }
}
