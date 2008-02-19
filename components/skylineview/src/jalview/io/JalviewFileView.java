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

import javax.swing.*;
import javax.swing.filechooser.*;

public class JalviewFileView
    extends FileView
{
  static Hashtable alignSuffix = new Hashtable();

  static
  {
    alignSuffix.put("fasta", "Fasta file");
    alignSuffix.put("fa", "Fasta file");
    alignSuffix.put("fastq", "Fasta file");
    alignSuffix.put("blc", "BLC file");
    alignSuffix.put("msf", "MSF file");
    alignSuffix.put("pfam", "PFAM file");
    alignSuffix.put("aln", "Clustal file");
    alignSuffix.put("pir", "PIR file");
    alignSuffix.put("jar", "Jalview file");
  }

  public String getTypeDescription(File f)
  {
    String extension = getExtension(f);
    String type = null;

    if (extension != null)
    {
      if (alignSuffix.containsKey(extension))
      {
        type = alignSuffix.get(extension).toString();
      }
    }

    return type;
  }

  public Icon getIcon(File f)
  {
    String extension = getExtension(f);
    Icon icon = null;

    if (extension != null)
    {
      if (alignSuffix.containsKey(extension))
      {
        icon = createImageIcon("/images/file.png");
      }
    }

    return icon;
  }

  /*
   * Get the extension of a file.
   */
  public static String getExtension(File f)
  {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if ( (i > 0) && (i < (s.length() - 1)))
    {
      ext = s.substring(i + 1).toLowerCase();
    }

    return ext;
  }

  /** Returns an ImageIcon, or null if the path was invalid. */
  protected static ImageIcon createImageIcon(String path)
  {
    java.net.URL imgURL = JalviewFileView.class.getResource(path);

    if (imgURL != null)
    {
      return new ImageIcon(imgURL);
    }
    else
    {
      System.err.println(
          "JalviewFileView.createImageIcon: Couldn't find file: " + path);

      return null;
    }
  }
}
