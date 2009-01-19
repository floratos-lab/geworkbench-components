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

public class Provenance
{
  Vector entries = new Vector();
  public Provenance()
  {

  }

  public ProvenanceEntry[] getEntries()
  {
    ProvenanceEntry[] ret = new ProvenanceEntry[entries.size()];
    for (int i = 0; i < entries.size(); i++)
    {
      ret[i] = (ProvenanceEntry) entries.elementAt(i);
    }
    return ret;
  }

  public void addEntry(String user, String action, java.util.Date date,
                       String id)
  {
    entries.addElement(new ProvenanceEntry(user, action, date, id));
  }

}
