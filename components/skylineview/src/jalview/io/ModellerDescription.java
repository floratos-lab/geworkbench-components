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

import jalview.datamodel.*;

public class ModellerDescription
{
  /**
   * Translates between a String containing a set of colon-separated values
   * on a single line, and sequence start/end and other properties.
   * See PIRFile IO for its use.
   */
  final String[] seqTypes =
      {
      "sequence", "structure", "structureX", "structureN"};
  final String[] Fields =
      {
      "objectType", "objectId",
      "startField", "startCode",
      "endField", "endCode",
      "description1", "description2",
      "resolutionField", "tailField"};
  final int TYPE = 0;
  final int LOCALID = 1;
  final int START = 2;
  final int START_CHAIN = 3;
  final int END = 4;
  final int END_CHAIN = 5;
  final int DESCRIPTION1 = 6;
  final int DESCRIPTION2 = 7;
  final int RESOLUTION = 8;
  final int TAIL = 9;

  /**
   * 0 is free text or empty
   * 1 is something that parses to an integer, or \@
   */
  final int Types[] =
      {
      0, 0, 1, 0, 1, 0, 0, 0, 0, 0
  };
  final char Padding[] =
      {
      ' ', ' ', ' ', '.', ' ', '.', '.', '.', '.', '.'
  };

  java.util.Hashtable fields = new java.util.Hashtable();
  ModellerDescription()
  {
    fields.put(Fields[TAIL], "");
  }

  class resCode
  {
    Integer val;
    String field;
    resCode(String f, Integer v)
    {
      val = v;
      field = f;
    }

    resCode(int v)
    {
      val = new Integer(v);
      field = val.toString();
    }
  };

  private resCode validResidueCode(String field)
  {
    Integer val = null;
    com.stevesoft.pat.Regex r = new com.stevesoft.pat.Regex(
        "\\s*((([-0-9]+).?)|FIRST|LAST|@)");

    if (!r.search(field))
    {
      return null; // invalid
    }
    String value = r.stringMatched(3);
    if (value == null)
    {
      value = r.stringMatched(1);
    }
    // jalview.bin.Cache.log.debug("from '" + field + "' matched '" + value +
    //                             "'");
    try
    {
      val = Integer.valueOf(value);
      return new resCode(field, val); // successful numeric extraction
    }
    catch (Exception e)
    {
    }
    return new resCode(field, null);
  }

  private java.util.Hashtable parseDescription(String desc)
  {
    desc = desc.replaceAll("::", ":.:");
    java.util.Hashtable fields = new java.util.Hashtable();
    java.util.StringTokenizer st = new java.util.StringTokenizer(desc, ":");
    String field;
    int type = -1;
    if (st.countTokens() > 0)
    {
      // parse colon-fields
      int i = 0;
      field = st.nextToken(":");
      do
      {
        if (seqTypes[i].equalsIgnoreCase(field))
        {
          break;
        }
      }
      while (++i < seqTypes.length);

      if (i < seqTypes.length)
      {
        // valid seqType for modeller
        type = i;
        i = 1; // continue parsing fields
        while (i < TAIL && st.hasMoreTokens())
        {
          if ((field = st.nextToken(":")) != null)
          {
            // validate residue field value
            if (Types[i] == 1)
            {
              resCode val = validResidueCode(field);
              if (val != null)
              {
                fields.put(new String(Fields[i] + "num"), val);
              }
              else
              {
                //      jalview.bin.Cache.log.debug(
                //           "Ignoring non-Modeller description: invalid integer-like field '" + field + "'");
                type = -1; /* invalid field! - throw the FieldSet away */
              }
              ;
            }
	    fields.put(Fields[i++], field);
          }
        }
        if (i == TAIL)
        {
          // slurp remaining fields
          while (st.hasMoreTokens())
          {
            field += ":" + st.nextToken(":");
          }
          fields.put(Fields[TAIL], field);
        }
      }
    }
    if (type == -1)
    {
      // object is not a proper ModellerPIR object
      fields = new java.util.Hashtable();
      fields.put(Fields[TAIL], new String(desc));
    }
    else
    {
      fields.put(Fields[TYPE], seqTypes[type]);
    }
    return fields;
  }

  ModellerDescription(String desc)
  {
    if (desc == null)
    {
      desc = "";
    }
    fields = parseDescription(desc);
  }

  void setStartCode(int v)
  {
    resCode r;
    fields.put(Fields[START] + "num", r = new resCode(v));
    fields.put(Fields[START], r.field);
  }

  void setEndCode(int v)
  {
    resCode r;
    fields.put(Fields[END] + "num", r = new resCode(v));
    fields.put(Fields[END], r.field);
  }

  /**
   * make a possibly updated modeller field line for the sequence object
   * @param seq SequenceI
   */
  ModellerDescription(SequenceI seq)
  {

    if (seq.getDescription() != null)
    {
      fields = parseDescription(seq.getDescription());
    }

    if (isModellerFieldset())
    {
      // Set start and end before we update the type (in the case of a synthesized field set)
      if (getStartNum() != seq.getStart() && getStartCode().val != null)
      {
        setStartCode(seq.getStart());
      }

      if (getEndNum() != seq.getEnd() && getStartCode().val != null)
      {
        setEndCode(seq.getEnd());
      }
    }
    else
    {
      // synthesize fields
      setStartCode(seq.getStart());
      setEndCode(seq.getEnd());
      fields.put(Fields[LOCALID], seq.getName()); // this may be overwritten below...
      // type - decide based on evidence of PDB database references - this also sets the local reference field
      int t = 0; // sequence
      if (seq.getDatasetSequence() != null &&
          seq.getDatasetSequence().getDBRef() != null)
      {
        jalview.datamodel.DBRefEntry[] dbr = seq.getDatasetSequence().getDBRef();
        int i, j;
        for (i = 0, j = dbr.length; i < j; i++)
        {
          if (dbr[i] != null)
          {
            // JBPNote PDB dbRefEntry needs properties to propagate onto ModellerField
            // JBPNote Need to get info from the user about whether the sequence is the one being modelled, or if it is a template.
            if (dbr[i].getSource().equals(jalview.datamodel.DBRefSource.PDB))
            {
              fields.put(Fields[LOCALID], dbr[i].getAccessionId());
              t = 2;
              break;
            }
          }
        }
      }
      fields.put(Fields[TYPE], seqTypes[t]);
    }

  }

  /**
   * Indicate if fields parsed to a modeller-like colon-separated value line
   * @return boolean
   */
  boolean isModellerFieldset()
  {
    return (fields.containsKey(Fields[TYPE]));
  }

  String getDescriptionLine()
  {
    String desc = "";
    int lastfield = Fields.length - 1;

    if (isModellerFieldset())
    {
      String value;
      // try to write a minimal modeller field set, so..

      // find the last valid field in the entry

      for (; lastfield > 6; lastfield--)
      {
        if (fields.containsKey(Fields[lastfield]))
        {
          break;
        }
      }

      for (int i = 0; i < lastfield; i++)
      {
        value = (String) fields.get(Fields[i]);
        if (value != null && value.length() > 0)
        {
          desc += ( (String) fields.get(Fields[i])) + ":";
        }
        else
        {
          desc += Padding[i] + ":";
        }
      }
    }
    // just return the last field if no others were defined.
    if (fields.containsKey(Fields[lastfield]))
    {
      desc += (String) fields.get(Fields[lastfield]);
    }
    else
    {
      desc += ".";
    }
    return desc;
  }

  int getStartNum()
  {
    int start = 0;
    resCode val = getStartCode();
    if (val.val != null)
    {
      return val.val.intValue();
    }
    return start;
  }

  resCode getStartCode()
  {
    if (isModellerFieldset() && fields.containsKey(Fields[START] + "num"))
    {
      return (resCode) fields.get(Fields[START] + "num");
    }
    return null;
  }

  resCode getEndCode()
  {
    if (isModellerFieldset() && fields.containsKey(Fields[END] + "num"))
    {
      return (resCode) fields.get(Fields[END] + "num");
    }
    return null;
  }

  int getEndNum()
  {
    int end = 0;
    resCode val = getEndCode();
    if (val.val != null)
    {
      return val.val.intValue();
    }
    return end;
  }

  /**
   * returns true if sequence object was modifed with a valid modellerField set
   * @param newSeq SequenceI
   * @return boolean
   */
  boolean updateSequenceI(SequenceI newSeq)
  {
    if (isModellerFieldset())
    {
      if (getStartCode().val != null)
      {
        newSeq.setStart(getStartNum());
      }
      else
      {
        newSeq.setStart(1);
      }
      if (getEndCode().val != null)
      {
        newSeq.setEnd(getEndNum());
      }
      else
      {
        newSeq.setEnd(newSeq.getStart() + newSeq.getLength());
      }
      return true;
    }
    return false;
  }
}
