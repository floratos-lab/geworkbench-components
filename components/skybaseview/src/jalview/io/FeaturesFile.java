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
import jalview.schemes.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class FeaturesFile
    extends AlignFile
{
  /**
   * Creates a new FeaturesFile object.
   */
  public FeaturesFile()
  {
  }

  /**
   * Creates a new FeaturesFile object.
   *
   * @param inFile DOCUMENT ME!
   * @param type DOCUMENT ME!
   *
   * @throws IOException DOCUMENT ME!
   */
  public FeaturesFile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  /**
   * The Application can render HTML, but the applet will
   * remove HTML tags and replace links with %LINK%
   * Both need to read links in HTML however
   *
   * @throws IOException DOCUMENT ME!
   */
  public boolean parse(AlignmentI align,
                       Hashtable colours,
                       boolean removeHTML)
  {
    return parse(align, colours, null, removeHTML);
  }

  /**
   * The Application can render HTML, but the applet will
   * remove HTML tags and replace links with %LINK%
   * Both need to read links in HTML however
   *
   * @throws IOException DOCUMENT ME!
   */
  public boolean parse(AlignmentI align,
                       Hashtable colours,
                       Hashtable featureLink,
                       boolean removeHTML)
  {
    String line = null;
    try
    {
      SequenceI seq = null;
      String type, desc, token = null;

      int index, start, end;
      float score;
      StringTokenizer st;
      SequenceFeature sf;
      String featureGroup = null, groupLink = null;
      Hashtable typeLink = new Hashtable();

      boolean GFFFile = true;

      while ( (line = nextLine()) != null)
      {
        if (line.startsWith("#"))
        {
          continue;
        }

        st = new StringTokenizer(line, "\t");
        if (st.countTokens() > 1 && st.countTokens() < 4)
        {
          GFFFile = false;
          type = st.nextToken();
          if (type.equalsIgnoreCase("startgroup"))
          {
            featureGroup = st.nextToken();
            if (st.hasMoreElements())
            {
              groupLink = st.nextToken();
              featureLink.put(featureGroup, groupLink);
            }
          }
          else if (type.equalsIgnoreCase("endgroup"))
          {
            //We should check whether this is the current group,
            //but at present theres no way of showing more than 1 group
            st.nextToken();
            featureGroup = null;
            groupLink = null;
          }
          else
          {
            UserColourScheme ucs = new UserColourScheme(st.nextToken());
            colours.put(type, ucs.findColour('A'));
            if (st.hasMoreElements())
            {
              String link = st.nextToken();
              typeLink.put(type, link);
              if (featureLink == null)
              {
                featureLink = new Hashtable();
              }
              featureLink.put(type, link);
            }

          }
          continue;
        }

        while (st.hasMoreElements())
        {

          if (GFFFile)
          {
            // Still possible this is an old Jalview file,
            // which does not have type colours at the beginning
            token = st.nextToken();
            seq = align.findName(token);
            if (seq != null)
            {
              desc = st.nextToken();
              type = st.nextToken();
              start = Integer.parseInt(st.nextToken());
              end = Integer.parseInt(st.nextToken());
              try
              {
                score = new Float(st.nextToken()).floatValue();
              }
              catch (NumberFormatException ex)
              {
                score = 0;
              }

              sf = new SequenceFeature(type, desc, start, end, score, null);

              try
              {
                sf.setValue("STRAND", st.nextToken());
                sf.setValue("FRAME", st.nextToken());
              }
              catch (Exception ex)
              {}

              if (st.hasMoreTokens())
              {
                StringBuffer attributes = new StringBuffer();
                while (st.hasMoreTokens())
                {
                  attributes.append("\t" + st.nextElement());
                }
                sf.setValue("ATTRIBUTES", attributes.toString());
              }

              seq.addSequenceFeature(sf);

              break;
            }
          }

          if (GFFFile && seq == null)
          {
            desc = token;
          }
          else
          {
            desc = st.nextToken();
          }

          token = st.nextToken();
          if (!token.equals("ID_NOT_SPECIFIED"))
          {
            seq = align.findName(token);
            st.nextToken();
          }
          else
          {
            try
            {
              index = Integer.parseInt(st.nextToken());
              seq = align.getSequenceAt(index);
            }
            catch (NumberFormatException ex)
            {
              seq = null;
            }
          }

          if (seq == null)
          {
            System.out.println("Sequence not found: " + line);
            break;
          }

          start = Integer.parseInt(st.nextToken());
          end = Integer.parseInt(st.nextToken());

          type = st.nextToken();

          if (!colours.containsKey(type))
          {
            // Probably the old style groups file
            UserColourScheme ucs = new UserColourScheme(type);
            colours.put(type, ucs.findColour('A'));
          }

          sf = new SequenceFeature(type, desc, "", start, end, featureGroup);

          seq.addSequenceFeature(sf);

          if (groupLink != null && removeHTML)
          {
            sf.addLink(groupLink);
            sf.description += "%LINK%";
          }
          if (typeLink.containsKey(type) && removeHTML)
          {
            sf.addLink(typeLink.get(type).toString());
            sf.description += "%LINK%";
          }

          parseDescriptionHTML(sf, removeHTML);

          //If we got here, its not a GFFFile
          GFFFile = false;
        }
      }
    }
    catch (Exception ex)
    {
      System.out.println(line);
      System.out.println("Error parsing feature file: " + ex + "\n" + line);
      return false;
    }

    return true;
  }

  public void parseDescriptionHTML(SequenceFeature sf, boolean removeHTML)
  {
    if (sf.getDescription() == null)
    {
      return;
    }

    if (removeHTML && sf.getDescription().toUpperCase().indexOf("<HTML>") == -1)
    {
      removeHTML = false;
    }

    StringBuffer sb = new StringBuffer();
    StringTokenizer st = new StringTokenizer(sf.getDescription(), "<");
    String token, link;
    int startTag;
    String tag = null;
    while (st.hasMoreElements())
    {
      token = st.nextToken("&>");
      if (token.equalsIgnoreCase("html") || token.startsWith("/"))
      {
        continue;
      }

      tag = null;
      startTag = token.indexOf("<");

      if (startTag > -1)
      {
        tag = token.substring(startTag + 1);
        token = token.substring(0, startTag);
      }

      if (tag != null && tag.toUpperCase().startsWith("A HREF="))
      {
        if (token.length() > 0)
        {
          sb.append(token);
        }
        link = tag.substring(tag.indexOf("\"") + 1, tag.length() - 1);
        String label = st.nextToken("<>");
        sf.addLink(label + "|" + link);
        sb.append(label + "%LINK%");
      }
      else if (tag != null && tag.equalsIgnoreCase("br"))
      {
        sb.append("\n");
      }
      else if (token.startsWith("lt;"))
      {
        sb.append("<" + token.substring(3));
      }
      else if (token.startsWith("gt;"))
      {
        sb.append(">" + token.substring(3));
      }
      else if (token.startsWith("amp;"))
      {
        sb.append("&" + token.substring(4));
      }
      else
      {
        sb.append(token);
      }
    }

    if (removeHTML)
    {
      sf.description = sb.toString();
    }

  }

  /**
   * DOCUMENT ME!
   *
   * @param s DOCUMENT ME!
   * @param len DOCUMENT ME!
   * @param gaps DOCUMENT ME!
   * @param displayId DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String printJalviewFormat(SequenceI[] seqs,
                                   Hashtable visible)
  {
    StringBuffer out = new StringBuffer();
    SequenceFeature[] next;

    if (visible == null || visible.size() < 1)
    {
      return "No Features Visible";
    }

    Enumeration en = visible.keys();
    String type;
    int color;
    while (en.hasMoreElements())
    {
      type = en.nextElement().toString();
      color = Integer.parseInt(visible.get(type).toString());
      out.append(type + "\t"
                 + jalview.util.Format.getHexString(
                     new java.awt.Color(color))
                 + "\n");
    }

    //Work out which groups are both present and visible
    Vector groups = new Vector();
    int groupIndex = 0;

    for (int i = 0; i < seqs.length; i++)
    {
      next = seqs[i].getSequenceFeatures();
      if (next != null)
      {
        for (int j = 0; j < next.length; j++)
        {
          if (!visible.containsKey(next[j].type))
          {
            continue;
          }

          if (next[j].featureGroup != null
              && !groups.contains(next[j].featureGroup))
          {
            groups.addElement(next[j].featureGroup);
          }
        }
      }
    }

    String group = null;

    do
    {

      if (groups.size() > 0 && groupIndex < groups.size())
      {
        group = groups.elementAt(groupIndex).toString();
        out.append("\nSTARTGROUP\t" + group + "\n");
      }
      else
      {
        group = null;
      }

      for (int i = 0; i < seqs.length; i++)
      {
        next = seqs[i].getSequenceFeatures();
        if (next != null)
        {
          for (int j = 0; j < next.length; j++)
          {
            if (!visible.containsKey(next[j].type))
            {
              continue;
            }

            if (group != null
                && (next[j].featureGroup == null
                    || !next[j].featureGroup.equals(group))
                )
            {
              continue;
            }

            if (group == null && next[j].featureGroup != null)
            {
              continue;
            }

            if (next[j].description == null || next[j].description.equals(""))
            {
              out.append(next[j].type + "\t");
            }
            else
            {
              if (next[j].links != null
                  && next[j].getDescription().indexOf("<html>") == -1)
              {
                out.append("<html>");
              }

              out.append(next[j].description + " ");
              if (next[j].links != null)
              {
                for (int l = 0; l < next[j].links.size(); l++)
                {
                  String label = next[j].links.elementAt(l).toString();
                  String href = label.substring(label.indexOf("|") + 1);
                  label = label.substring(0, label.indexOf("|"));

                  if (next[j].description.indexOf(href) == -1)
                  {
                    out.append("<a href=\""
                               + href
                               + "\">"
                               + label
                               + "</a>");
                  }
                }

                if (next[j].getDescription().indexOf("</html>") == -1)
                {
                  out.append("</html>");
                }
              }

              out.append("\t");
            }

            out.append(seqs[i].getName() + "\t-1\t"
                       + next[j].begin + "\t"
                       + next[j].end + "\t"
                       + next[j].type + "\n"
                );
          }
        }
      }

      if (group != null)
      {
        out.append("ENDGROUP\t" + group + "\n");
        groupIndex++;
      }
      else
      {
        break;
      }

    }
    while (groupIndex < groups.size() + 1);

    return out.toString();
  }

  public String printGFFFormat(SequenceI[] seqs, Hashtable visible)
  {
    StringBuffer out = new StringBuffer();
    SequenceFeature[] next;
    String source;

    for (int i = 0; i < seqs.length; i++)
    {
      if (seqs[i].getSequenceFeatures() != null)
      {
        next = seqs[i].getSequenceFeatures();
        for (int j = 0; j < next.length; j++)
        {
          if (!visible.containsKey(next[j].type))
          {
            continue;
          }

          source = next[j].featureGroup;
          if (source == null)
          {
            source = next[j].getDescription();
          }

          out.append(seqs[i].getName() + "\t"
                     + source + "\t"
                     + next[j].type + "\t"
                     + next[j].begin + "\t"
                     + next[j].end + "\t"
                     + next[j].score + "\t"
              );

          if (next[j].getValue("STRAND") != null)
          {
            out.append(next[j].getValue("STRAND") + "\t");
          }
          else
          {
            out.append(".\t");
          }

          if (next[j].getValue("FRAME") != null)
          {
            out.append(next[j].getValue("FRAME"));
          }
          else
          {
            out.append(".");
          }

          if (next[j].getValue("ATTRIBUTES") != null)
          {
            out.append(next[j].getValue("ATTRIBUTES"));
          }

          out.append("\n");

        }
      }
    }

    return out.toString();
  }

  public void parse()
  {
    //IGNORED
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String print()
  {
    return "USE printGFFFormat() or printJalviewFormat()";
  }
}
