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

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public abstract class AlignFile
    extends FileParse
{
  int noSeqs = 0;
  int maxLength = 0;
  /**
   * Sequences to be added to form a new alignment.
   */
  protected Vector seqs;
  /**
   * annotation to be added to generated alignment object
   */
  protected Vector annotations;
  /**
   * Properties to be added to generated alignment object
   */
  protected Hashtable properties;
  long start;
  long end;
  boolean jvSuffix = true;

  /**
   * Creates a new AlignFile object.
   */
  public AlignFile()
  {
  }

  /**
   * Constructor which parses the data from a file of some specified type.
   * @param inFile Filename to read from.
   * @param type   What type of file to read from (File, URL)
   */
  public AlignFile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);

    initData();

    parse();
  }

  /**
   * Return the seqs Vector
   */
  public Vector getSeqs()
  {
    return seqs;
  }

  /**
   * Return the Sequences in the seqs Vector as an array of Sequences
   */
  public SequenceI[] getSeqsAsArray()
  {
    SequenceI[] s = new SequenceI[seqs.size()];

    for (int i = 0; i < seqs.size(); i++)
    {
      s[i] = (SequenceI) seqs.elementAt(i);
    }

    return s;
  }
  /**
   * called by AppletFormatAdapter to generate
   * an annotated alignment, rather than bare
   * sequences.
   * @param al
   */
  public void addAnnotations(Alignment al)
  {
    addProperties(al);
    for (int i = 0; i < annotations.size(); i++)
    {
      al.addAnnotation(
          (AlignmentAnnotation) annotations.elementAt(i)
          );
    }

  }
  /**
   * Add any additional information extracted
   * from the file to the alignment properties.
   * @note implicitly called by addAnnotations()
   * @param al
   */
  public void addProperties(Alignment al)
  {
    if (properties!=null && properties.size()>0)
    {
      Enumeration keys = properties.keys();
      Enumeration vals = properties.elements();
      while (keys.hasMoreElements())
      {
        al.setProperty(keys.nextElement(), vals.nextElement());
      }
    }
  }
  /**
   * Store a non-null key-value pair in a hashtable used to set alignment properties
   * note: null keys will raise an error, null values will result in the key/value pair being silently ignored.
   * @param key - non-null key object
   * @param value - non-null value
   */
  protected void setAlignmentProperty(Object key, Object value)
  {
    if (key==null)
    {
      throw new Error("Implementation error: Cannot have null alignment property key.");
    }
    if (value==null)
    {
      return; // null properties are ignored.
    }
    if (properties==null)
    {
      properties = new Hashtable();
    }
    properties.put(key, value);
  }
  protected Object getAlignmentProperty(Object key)
  {
    if (properties!=null && key!=null)
    {
      return properties.get(key);
    }
    return null;
  }
  /**
   * Initialise objects to store sequence data in.
   */
  protected void initData()
  {
    seqs = new Vector();
    annotations = new Vector();
  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   */
  protected void setSeqs(SequenceI[] s)
  {
    seqs = new Vector();

    for (int i = 0; i < s.length; i++)
    {
      seqs.addElement(s[i]);
    }
  }

  /**
   * This method must be implemented to parse the contents of the file.
   */
  public abstract void parse()
      throws IOException;

  /**
   * Print out in alignment file format the Sequences in the seqs Vector.
   */
  public abstract String print();

  public void addJVSuffix(boolean b)
  {
    jvSuffix = b;
  }

  /**
   * A general parser for ids.
   *
   * @String id Id to be parsed
   */
  Sequence parseId(String id)
  {
    Sequence seq = null;
    id = id.trim();
    int space = id.indexOf(" ");
    if (space > -1)
    {
      seq = new Sequence(id.substring(0, space), "");
      seq.setDescription(id.substring(space + 1));
    }
    else
    {
      seq = new Sequence(id, "");
    }

    return seq;
  }

  /**
   * Creates the output id.
   * Adds prefix Uniprot format source|id
   * And suffix Jalview /start-end
   *
   * @String id Id to be parsed
   */
  String printId(SequenceI seq)
  {
    return seq.getDisplayId(jvSuffix);
  }
  /**
   * vector of String[] treeName, newickString pairs
   */
  Vector newickStrings=null;

  protected void addNewickTree(String treeName, String newickString)
  {
    if (newickStrings == null)
    {
      newickStrings = new Vector();
    }
    newickStrings.addElement(new String[] { treeName, newickString});
  }

  protected int getTreeCount()
  {
    if (newickStrings==null)
    {
      return 0;
    }
    return newickStrings.size();
  }

}
