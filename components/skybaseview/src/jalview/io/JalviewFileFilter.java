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
package jalview.io;

import java.io.*;
import java.util.*;

import javax.swing.filechooser.FileFilter;

public class JalviewFileFilter
    extends FileFilter
{
  public static Hashtable suffixHash = new Hashtable();
  private Hashtable filters = null;
  private String description = "no description";
  private String fullDescription = "full description";
  private boolean useExtensionsInDescription = true;

  public JalviewFileFilter(String extension, String description)
  {
    StringTokenizer st = new StringTokenizer(extension, ",");

    while (st.hasMoreElements())
    {
      addExtension(st.nextToken().trim());
    }

    setDescription(description);
  }

  public JalviewFileFilter(String[] filts)
  {
    this(filts, null);
  }

  public JalviewFileFilter(String[] filts, String description)
  {
    for (int i = 0; i < filts.length; i++)
    {
      // add filters one by one
      addExtension(filts[i]);
    }

    if (description != null)
    {
      setDescription(description);
    }
  }

  public String getAcceptableExtension()
  {
    return filters.keys().nextElement().toString();
  }

  // takes account of the fact that database is a directory
  public boolean accept(File f)
  {
    if (f != null)
    {
      String extension = getExtension(f);

      if (f.isDirectory())
      {
        return true;
      }

      if ( (extension != null) && (filters.get(getExtension(f)) != null))
      {
        return true;
      }
    }

    return false;
  }

  public String getExtension(File f)
  {
    if (f != null)
    {
      String filename = f.getName();
      int i = filename.lastIndexOf('.');

      if ( (i > 0) && (i < (filename.length() - 1)))
      {
        return filename.substring(i + 1).toLowerCase();
      }

      ;
    }

    return "";
  }

  public void addExtension(String extension)
  {
    if (filters == null)
    {
      filters = new Hashtable(5);
    }

    filters.put(extension.toLowerCase(), this);
    fullDescription = null;
  }

  public String getDescription()
  {
    if (fullDescription == null)
    {
      if ( (description == null) || isExtensionListInDescription())
      {
        fullDescription = (description == null) ? "(" : (description +
            " (");

        // build the description from the extension list
        Enumeration extensions = filters.keys();

        if (extensions != null)
        {
          fullDescription += ("." +
                              (String) extensions.nextElement());

          while (extensions.hasMoreElements())
          {
            fullDescription += (", " +
                                (String) extensions.nextElement());
          }
        }

        fullDescription += ")";
      }
      else
      {
        fullDescription = description;
      }
    }

    return fullDescription;
  }

  public void setDescription(String description)
  {
    this.description = description;
    fullDescription = null;
  }

  public void setExtensionListInDescription(boolean b)
  {
    useExtensionsInDescription = b;
    fullDescription = null;
  }

  public boolean isExtensionListInDescription()
  {
    return useExtensionsInDescription;
  }
}
