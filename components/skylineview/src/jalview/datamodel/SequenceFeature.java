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

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class SequenceFeature
{
  public int begin;
  public int end;
  public float score;
  public String type;
  public String description;
  public Hashtable otherDetails;
  public java.util.Vector links;

  // Feature group can be set from a features file
  // as a group of features between STARTGROUP and ENDGROUP markers
  public String featureGroup;

  public SequenceFeature()
  {}

  /**
   * Constructs a duplicate feature.
   * Note: Uses clone on the otherDetails so only shallow copies are made
   * of additional properties and method will silently fail if unclonable
   * objects are found in the hash.
   * @param cpy
   */
  public SequenceFeature(SequenceFeature cpy)
  {
    if (cpy != null)
    {
      begin = cpy.begin;
      end = cpy.end;
      score = cpy.score;
      if (cpy.type != null)
      {
        type = new String(cpy.type);
      }
      if (cpy.description != null)
      {
        description = new String(cpy.description);
      }
      if (cpy.featureGroup != null)
      {
        featureGroup = new String(cpy.featureGroup);
      }
      if (cpy.otherDetails != null)
      {
        try
        {
          otherDetails = (Hashtable) cpy.otherDetails.clone();
        }
        catch (Exception e)
        {
          // Uncloneable objects in the otherDetails - don't complain
        }
      }
      if (cpy.links != null && cpy.links.size() > 0)
      {
        links = new Vector();
        for (int i = 0, iSize = cpy.links.size(); i < iSize; i++)
        {
          links.addElement(cpy.links.elementAt(i));
        }
      }
    }
  }

  public SequenceFeature(String type,
                         String desc,
                         String status,
                         int begin, int end,
                         String featureGroup)
  {
    this.type = type;
    this.description = desc;
    setValue("status", status);
    this.begin = begin;
    this.end = end;
    this.featureGroup = featureGroup;
  }

  public SequenceFeature(String type,
                         String desc,
                         int begin, int end,
                         float score,
                         String featureGroup)
  {
    this.type = type;
    this.description = desc;
    this.begin = begin;
    this.end = end;
    this.score = score;
    this.featureGroup = featureGroup;
  }

  public boolean equals(SequenceFeature sf)
  {
    if (begin != sf.begin
        || end != sf.end
        || score != sf.score)
    {
      return false;
    }

    if (! (type + description + featureGroup).equals
        (sf.type + sf.description + sf.featureGroup))
    {
      return false;
    }

    return true;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getBegin()
  {
    return begin;
  }

  public void setBegin(int start)
  {
    this.begin = start;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public int getEnd()
  {
    return end;
  }

  public void setEnd(int end)
  {
    this.end = end;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String desc)
  {
    description = desc;
  }

  public String getFeatureGroup()
  {
    return featureGroup;
  }

  public void setFeatureGroup(String featureGroup)
  {
    this.featureGroup = featureGroup;
  }

  public void addLink(String labelLink)
  {
    if (links == null)
    {
      links = new java.util.Vector();
    }

    links.insertElementAt(labelLink, 0);
  }

  public float getScore()
  {
    return score;
  }

  public void setScore(float value)
  {
    score = value;
  }

  /**
   * Used for getting values which are not in the
   * basic set. eg STRAND, FRAME for GFF file
   * @param key String
   */
  public Object getValue(String key)
  {
    if (otherDetails == null)
    {
      return null;
    }
    else
    {
      return otherDetails.get(key);
    }
  }

  /**
   * Used for setting values which are not in the
   * basic set. eg STRAND, FRAME for GFF file
   * @param key   eg STRAND
   * @param value eg +
   */
  public void setValue(String key, Object value)
  {
    if (value != null)
    {
      if (otherDetails == null)
      {
        otherDetails = new Hashtable();
      }

      otherDetails.put(key, value);
    }
  }

  /*
   * The following methods are added to maintain
   * the castor Uniprot mapping file for the moment.
   */
  public void setStatus(String status)
  {
    setValue("status", status);
  }

  public String getStatus()
  {
    if (otherDetails != null)
    {
      return otherDetails.get("status").toString();
    }
    else
    {
      return null;
    }
  }

  public void setPosition(int pos)
  {
    begin = pos;
    end = pos;
  }

  public int getPosition()
  {
    return begin;
  }

}
