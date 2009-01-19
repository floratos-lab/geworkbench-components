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
import java.net.*;
import java.util.*;

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.schemes.*;

public class AnnotationFile
{
  StringBuffer text = new StringBuffer(
      "JALVIEW_ANNOTATION\n"
      + "# Created: "
      + new java.util.Date() + "\n\n");

  public String printAnnotations(AlignmentAnnotation[] annotations,
                                 Vector groups,
                                 Hashtable properties)
  {
    if (annotations != null)
    {
      boolean oneColour = true;
      AlignmentAnnotation row;
      String comma;
      SequenceI refSeq = null;

      StringBuffer colours = new StringBuffer();
      StringBuffer graphLine = new StringBuffer();

      Hashtable graphGroup = new Hashtable();

      java.awt.Color color;

      for (int i = 0; i < annotations.length; i++)
      {
        row = annotations[i];

        if (!row.visible)
        {
          continue;
        }

        color = null;
        oneColour = true;

        if (row.sequenceRef == null)
        {
          if (refSeq != null)
          {
            text.append("\nSEQUENCE_REF\tALIGNMENT\n");
          }

          refSeq = null;
        }

        else if (refSeq == null || refSeq != row.sequenceRef)
        {
          refSeq = row.sequenceRef;
          text.append("\nSEQUENCE_REF\t" + refSeq.getName() + "\n");
        }

        if (row.graph == AlignmentAnnotation.NO_GRAPH)
        {
          text.append("NO_GRAPH\t");
        }
        else
        {
          if (row.graph == AlignmentAnnotation.BAR_GRAPH)
          {
            text.append("BAR_GRAPH\t");
          }
          else if (row.graph == AlignmentAnnotation.LINE_GRAPH)
          {
            text.append("LINE_GRAPH\t");
          }

          if (row.getThreshold() != null)
          {
            graphLine.append("GRAPHLINE\t"
                             + row.label + "\t"
                             + row.getThreshold().value + "\t"
                             + row.getThreshold().label + "\t"
                             + jalview.util.Format.getHexString(
                                 row.getThreshold().colour) + "\n"
                );
          }

          if (row.graphGroup > -1)
          {
            String key = String.valueOf(row.graphGroup);
            if (graphGroup.containsKey(key))
            {
              graphGroup.put(key, graphGroup.get(key)
                             + "\t" + row.label);
            }
            else
            {
              graphGroup.put(key, row.label);
            }
          }
        }

        text.append(row.label + "\t");
        if (row.description != null)
        {
          text.append(row.description + "\t");
        }

        for (int j = 0; j < row.annotations.length; j++)
        {
          if (refSeq != null &&
              jalview.util.Comparison.isGap(refSeq.getCharAt(j)))
          {
            continue;
          }

          if (row.annotations[j] != null)
          {
            comma = "";
            if (row.annotations[j].secondaryStructure != ' ')
            {
              text.append(comma + row.annotations[j].secondaryStructure);
              comma = ",";
            }
            if (row.annotations[j].displayCharacter!=null
                && row.annotations[j].displayCharacter.length() > 0
                && !row.annotations[j].displayCharacter.equals(" "))
            {
              text.append(comma + row.annotations[j].displayCharacter);
              comma = ",";
            }

            if (row.annotations[j] != null)
            {
              if(color!=null && !color.equals(row.annotations[j].colour))
              {
                oneColour = false;
              }

              color = row.annotations[j].colour;
              if (row.annotations[j].value != 0f)
              {
                text.append(comma + row.annotations[j].value);
              }
            }

            if(row.annotations[j].colour!=null
               && row.annotations[j].colour!=java.awt.Color.black)
            {
              text.append(comma+"["+
                          jalview.util.Format.getHexString(
                          row.annotations[j].colour)+"]");
            }
          }
          text.append("|");
        }

        if(row.hasScore())
          text.append("\t"+row.score);

        text.append("\n");

        if (color != null && color != java.awt.Color.black && oneColour)
        {
          colours.append("COLOUR\t"
                         + row.label + "\t"
                         + jalview.util.Format.getHexString(color) + "\n");
        }

      }

      text.append("\n");

      text.append(colours.toString());
      text.append(graphLine.toString());
      if (graphGroup.size() > 0)
      {
        text.append("COMBINE\t");
        Enumeration en = graphGroup.elements();
        while (en.hasMoreElements())
        {
          text.append(en.nextElement() + "\n");
        }
      }
    }

    if (groups != null)
    {
      printGroups(groups);
    }

    if(properties!=null)
    {
      text.append("\n\nALIGNMENT");
      Enumeration en = properties.keys();
      while(en.hasMoreElements())
      {
        String key = en.nextElement().toString();
        text.append("\t"+key+"="+properties.get(key));
      }

    }

    return text.toString();
  }

  public void printGroups(Vector sequenceGroups)
  {
    SequenceGroup sg;
    for (int i = 0; i < sequenceGroups.size(); i++)
    {
      sg = (SequenceGroup) sequenceGroups.elementAt(i);
      text.append("SEQUENCE_GROUP\t"
                  + sg.getName() + "\t"
                  + (sg.getStartRes() + 1) + "\t"
                  + (sg.getEndRes() + 1) + "\t" + "-1\t");
      for (int s = 0; s < sg.getSize(); s++)
      {
        text.append(sg.getSequenceAt(s).getName() + "\t");
      }

      text.append("\nPROPERTIES\t" + sg.getName() + "\t");

      if (sg.getDescription() != null)
      {
        text.append("description=" + sg.getDescription() + "\t");
      }
      if (sg.cs != null)
      {
        text.append("colour=" + ColourSchemeProperty.getColourName(sg.cs) +
                    "\t");
        if (sg.cs.getThreshold() != 0)
        {
          text.append("pidThreshold=" + sg.cs.getThreshold());
        }
        if (sg.cs.conservationApplied())
        {
          text.append("consThreshold=" + sg.cs.getConservationInc() + "\t");
        }
      }
      text.append("outlineColour=" +
                  jalview.util.Format.getHexString(sg.getOutlineColour()) +
                  "\t");

      text.append("displayBoxes=" + sg.getDisplayBoxes() + "\t");
      text.append("displayText=" + sg.getDisplayText() + "\t");
      text.append("colourText=" + sg.getColourText() + "\t");

      if (sg.textColour != java.awt.Color.black)
      {
        text.append("textCol1=" +
                    jalview.util.Format.getHexString(sg.textColour) + "\t");
      }
      if (sg.textColour2 != java.awt.Color.white)
      {
        text.append("textCol2=" +
                    jalview.util.Format.getHexString(sg.textColour2) + "\t");
      }
      if (sg.thresholdTextColour != 0)
      {
        text.append("textColThreshold=" + sg.thresholdTextColour);
      }

      text.append("\n\n");

    }
  }

  SequenceI refSeq = null;
  public boolean readAnnotationFile(AlignmentI al,
                                    String file,
                                    String protocol)
  {
    try
    {
      BufferedReader in = null;
      if (protocol.equals(AppletFormatAdapter.FILE))
      {
        in = new BufferedReader(new FileReader(file));
      }
      else if (protocol.equals(AppletFormatAdapter.URL))
      {
        URL url = new URL(file);
        in = new BufferedReader(new InputStreamReader(url.openStream()));
      }
      else if (protocol.equals(AppletFormatAdapter.PASTE))
      {
        in = new BufferedReader(new StringReader(file));
      }
      else if (protocol.equals(AppletFormatAdapter.CLASSLOADER))
      {
        java.io.InputStream is = getClass().getResourceAsStream("/" + file);
        if (is != null)
        {
          in = new BufferedReader(new java.io.InputStreamReader(is));
        }
      }

      String line, label, description, token;
      int graphStyle, index;
      int refSeqIndex = 1;
      int existingAnnotations = 0;
      if (al.getAlignmentAnnotation() != null)
      {
        existingAnnotations = al.getAlignmentAnnotation().length;
      }

      int alWidth = al.getWidth();

      StringTokenizer st;
      Annotation[] annotations;
      AlignmentAnnotation annotation = null;

      // First confirm this is an Annotation file
      boolean jvAnnotationFile = false;
      while ( (line = in.readLine()) != null)
      {
        if (line.indexOf("#") == 0)
        {
          continue;
        }

        if (line.indexOf("JALVIEW_ANNOTATION") > -1)
        {
          jvAnnotationFile = true;
          break;
        }
      }

      if (!jvAnnotationFile)
      {
        in.close();
        return false;
      }

      while ( (line = in.readLine()) != null)
      {
        if (line.indexOf("#") == 0
            || line.indexOf("JALVIEW_ANNOTATION") > -1
            || line.length() == 0)
        {
          continue;
        }

        st = new StringTokenizer(line, "\t");
        token = st.nextToken();
        if (token.equalsIgnoreCase("COLOUR"))
        {
          colourAnnotations(al, st.nextToken(), st.nextToken());
          continue;
        }

        else if (token.equalsIgnoreCase("COMBINE"))
        {
          combineAnnotations(al, st);
          continue;
        }

        else if (token.equalsIgnoreCase("GRAPHLINE"))
        {
          addLine(al, st);
          continue;
        }

        else if (token.equalsIgnoreCase("SEQUENCE_REF"))
        {
          refSeq = al.findName(st.nextToken());
          try
          {
            refSeqIndex = Integer.parseInt(st.nextToken());
            if (refSeqIndex < 1)
            {
              refSeqIndex = 1;
              System.out.println(
                  "WARNING: SEQUENCE_REF index must be > 0 in AnnotationFile");
            }
          }
          catch (Exception ex)
          {
            refSeqIndex = 1;
          }

          continue ;
        }

        else if (token.equalsIgnoreCase("SEQUENCE_GROUP"))
        {
          addGroup(al, st);
          continue;
        }

        else if (token.equalsIgnoreCase("PROPERTIES"))
        {
          addProperties(al, st);
          continue;
        }

        else if( token.equalsIgnoreCase("BELOW_ALIGNMENT"))
        {
          setBelowAlignment(al, st);
          continue;
        }
        else if( token.equalsIgnoreCase("ALIGNMENT"))
        {
          addAlignmentDetails(al, st);
          continue;
        }

        graphStyle = AlignmentAnnotation.getGraphValueFromString(token);
        label = st.nextToken();


        index = 0;
        annotations = new Annotation[alWidth];
        description = null;
        float score = Float.NaN;

        if(st.hasMoreTokens())
        {
          line = st.nextToken();

          if (line.indexOf("|") ==-1)
          {
            description = line;
            if (st.hasMoreTokens())
              line = st.nextToken();
          }

          if(st.hasMoreTokens())
          {
            //This must be the score
            score = Float.valueOf(st.nextToken()).floatValue();
          }

          st = new StringTokenizer(line, "|", true);


          boolean emptyColumn = true;
          boolean onlyOneElement = (st.countTokens()==1);

          while (st.hasMoreElements() && index < alWidth)
          {
            token = st.nextToken().trim();

            if(onlyOneElement)
            {
              try
              {
                score = Float.valueOf(token).floatValue();
                break;
              }
              catch(NumberFormatException ex){}
            }

            if (token.equals("|"))
            {
              if (emptyColumn)
              {
                index++;
              }

              emptyColumn = true;
            }
            else
            {
              annotations[index++] = parseAnnotation(token);
              emptyColumn = false;
            }
          }

        }

        annotation = new AlignmentAnnotation(label,
                                             description,
                                             annotations,
                                             0,
                                             0,
                                             graphStyle);

        annotation.score = score;

        if (refSeq != null)
        {
          annotation.belowAlignment=false;
          annotation.createSequenceMapping(refSeq, refSeqIndex, false);
          annotation.adjustForAlignment();
          refSeq.addAlignmentAnnotation(annotation);
        }

        al.addAnnotation(annotation);

        al.setAnnotationIndex(annotation,
                              al.getAlignmentAnnotation().length - existingAnnotations -
                              1);
      }

    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println("Problem reading annotation file: " + ex);
      return false;
    }
    return true;
  }

  Annotation parseAnnotation(String string)
  {
    String desc = null, displayChar = null;
    char ss = ' '; // secondaryStructure
    float value = 0;
    boolean parsedValue = false;

    //find colour here
    java.awt.Color colour = null;
    int i=string.indexOf("[");
    int j=string.indexOf("]");
    if(i>-1 && j>-1)
    {
      UserColourScheme ucs = new UserColourScheme();

      colour = ucs.getColourFromString(string.substring(i+1,j));

      string = string.substring(0,i)+string.substring(j+1);
    }

    StringTokenizer st = new StringTokenizer(string, ",");
    String token;
    while (st.hasMoreTokens())
    {
      token = st.nextToken().trim();
      if (token.length() == 0)
      {
        continue;
      }

      if (!parsedValue)
      {
        try
        {
          displayChar = token;
          value = new Float(token).floatValue();
          parsedValue = true;
          continue;
        }
        catch (NumberFormatException ex)
        {}
      }

      if (token.equals("H") || token.equals("E"))
      {
        // Either this character represents a helix or sheet
        // or an integer which can be displayed
        ss = token.charAt(0);
        if (displayChar.equals(token.substring(0, 1)))
        {
          displayChar = "";
        }
      }
      else if (desc == null)
      {
        desc = token;
      }

    }

    if (displayChar!=null
        && displayChar.length() > 1
        &&  desc!=null
        && desc.length() == 1)
    {
      String tmp = displayChar;
      displayChar = desc;
      desc = tmp;
    }

    Annotation anot = new Annotation(displayChar, desc, ss, value);

    anot.colour = colour;

    return anot;
  }

  void colourAnnotations(AlignmentI al, String label, String colour)
  {
    UserColourScheme ucs = new UserColourScheme(colour);
    Annotation[] annotations;
    for (int i = 0; i < al.getAlignmentAnnotation().length; i++)
    {
      if (al.getAlignmentAnnotation()[i].label.equalsIgnoreCase(label))
      {
        annotations = al.getAlignmentAnnotation()[i].annotations;
        for (int j = 0; j < annotations.length; j++)
        {
          if (annotations[j] != null)
          {
            annotations[j].colour = ucs.findColour('A');
          }
        }
      }
    }
  }

  void combineAnnotations(AlignmentI al, StringTokenizer st)
  {
    int graphGroup = -1;
    String group = st.nextToken();
    //First make sure we are not overwriting the graphIndex
    for (int i = 0; i < al.getAlignmentAnnotation().length; i++)
    {
      if (al.getAlignmentAnnotation()[i].label.equalsIgnoreCase(group))
      {
        graphGroup = al.getAlignmentAnnotation()[i].graphGroup + 1;
        al.getAlignmentAnnotation()[i].graphGroup = graphGroup;
        break;
      }
    }

    //Now update groups
    while (st.hasMoreTokens())
    {
      group = st.nextToken();
      for (int i = 0; i < al.getAlignmentAnnotation().length; i++)
      {
        if (al.getAlignmentAnnotation()[i].label.equalsIgnoreCase(group))
        {
          al.getAlignmentAnnotation()[i].graphGroup = graphGroup;
          break;
        }
      }
    }
  }

  void addLine(AlignmentI al, StringTokenizer st)
  {
    String group = st.nextToken();
    AlignmentAnnotation annotation = null;

    for (int i = 0; i < al.getAlignmentAnnotation().length; i++)
    {
      if (al.getAlignmentAnnotation()[i].label.equalsIgnoreCase(group))
      {
        annotation = al.getAlignmentAnnotation()[i];
        break;
      }
    }

    if (annotation == null)
    {
      return;
    }
    float value = new Float(st.nextToken()).floatValue();
    String label = st.hasMoreTokens() ? st.nextToken() : null;
    java.awt.Color colour = null;
    if (st.hasMoreTokens())
    {
      UserColourScheme ucs = new UserColourScheme(st.nextToken());
      colour = ucs.findColour('A');
    }

    annotation.setThreshold(new GraphLine(value, label, colour));
  }

  void addGroup(AlignmentI al, StringTokenizer st)
  {
    SequenceGroup sg = new SequenceGroup();
    sg.setName(st.nextToken());
    sg.setStartRes(Integer.parseInt(st.nextToken()) - 1);
    sg.setEndRes(Integer.parseInt(st.nextToken()) - 1);

    String index = st.nextToken();
    if (index.equals("-1"))
    {
      while (st.hasMoreElements())
      {
        sg.addSequence(al.findName(st.nextToken()), false);
      }
    }
    else
    {
      StringTokenizer st2 = new StringTokenizer(index, ",");

      while (st2.hasMoreTokens())
      {
        String tmp = st2.nextToken();
        if (tmp.equals("*"))
        {
          for (int i = 0; i < al.getHeight(); i++)
          {
            sg.addSequence(al.getSequenceAt(i), false);
          }
        }
        else if (tmp.indexOf("-") >= 0)
        {
          StringTokenizer st3 = new StringTokenizer(tmp, "-");

          int start = (Integer.parseInt(st3.nextToken()));
          int end = (Integer.parseInt(st3.nextToken()));

          if (end > start)
          {
            for (int i = start; i <= end; i++)
            {
              sg.addSequence(al.getSequenceAt(i - 1), false);
            }
          }
        }
        else
        {
          sg.addSequence(al.getSequenceAt(Integer.parseInt(tmp) - 1), false);
        }
      }
    }



    if (refSeq != null)
    {
      sg.setStartRes(refSeq.findIndex(sg.getStartRes() + 1) - 1);
      sg.setEndRes(refSeq.findIndex(sg.getEndRes() + 1) - 1);
    }

    if (sg.getSize() > 0)
    {
      al.addGroup(sg);
    }
  }

  void addProperties(AlignmentI al, StringTokenizer st)
  {

    //So far we have only added groups to the annotationHash,
    //the idea is in the future properties can be added to
    //alignments, other annotations etc
    if (al.getGroups() == null)
    {
      return;
    }
    SequenceGroup sg = null;

    String name = st.nextToken();

    Vector groups = al.getGroups();
    for (int i = 0; i < groups.size(); i++)
    {
      sg = (SequenceGroup) groups.elementAt(i);
      if (sg.getName().equals(name))
      {
        break;
      }
      else
      {
        sg = null;
      }
    }

    if (sg != null)
    {
      String keyValue, key, value;
      while (st.hasMoreTokens())
      {
        keyValue = st.nextToken();
        key = keyValue.substring(0, keyValue.indexOf("="));
        value = keyValue.substring(keyValue.indexOf("=") + 1);

        if (key.equalsIgnoreCase("description"))
        {
          sg.setDescription(value);
        }
        else if (key.equalsIgnoreCase("colour"))
        {
          sg.cs = ColourSchemeProperty.getColour(al, value);
        }
        else if (key.equalsIgnoreCase("pidThreshold"))
        {
          sg.cs.setThreshold(Integer.parseInt(value), true);

        }
        else if (key.equalsIgnoreCase("consThreshold"))
        {
          sg.cs.setConservationInc(Integer.parseInt(value));
          Conservation c = new Conservation("Group",
                                            ResidueProperties.propHash, 3,
                                            sg.getSequences(null),
                                            sg.getStartRes(),
                                            sg.getEndRes() + 1);

          c.calculate();
          c.verdict(false, 25);

          sg.cs.setConservation(c);

        }
        else if (key.equalsIgnoreCase("outlineColour"))
        {
          sg.setOutlineColour(new UserColourScheme(value).findColour('A'));
        }
        else if (key.equalsIgnoreCase("displayBoxes"))
        {
          sg.setDisplayBoxes(Boolean.valueOf(value).booleanValue());
        }
        else if (key.equalsIgnoreCase("displayText"))
        {
          sg.setDisplayText(Boolean.valueOf(value).booleanValue());
        }
        else if (key.equalsIgnoreCase("colourText"))
        {
          sg.setColourText(Boolean.valueOf(value).booleanValue());
        }
        else if (key.equalsIgnoreCase("textCol1"))
        {
          sg.textColour = new UserColourScheme(value).findColour('A');
        }
        else if (key.equalsIgnoreCase("textCol2"))
        {
          sg.textColour2 = new UserColourScheme(value).findColour('A');
        }
        else if (key.equalsIgnoreCase("textColThreshold"))
        {
          sg.thresholdTextColour = Integer.parseInt(value);
        }

        sg.recalcConservation();
      }
    }
  }

  void setBelowAlignment(AlignmentI al, StringTokenizer st)
  {
    String token;
    AlignmentAnnotation aa;
    while(st.hasMoreTokens())
    {
      token = st.nextToken();
      for(int i=0; i<al.getAlignmentAnnotation().length; i++)
      {
        aa = al.getAlignmentAnnotation()[i];
        if(aa.sequenceRef==refSeq && aa.label.equals(token))
        {
          aa.belowAlignment = true;
        }
      }
    }
  }

  void addAlignmentDetails(AlignmentI al, StringTokenizer st)
  {
    String keyValue, key, value;
    while (st.hasMoreTokens())
    {
      keyValue = st.nextToken();
      key = keyValue.substring(0, keyValue.indexOf("="));
      value = keyValue.substring(keyValue.indexOf("=") + 1);
      al.setProperty(key,value);
    }
  }
}
