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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class AlignmentAnnotation
{
  /** If true, this annotations is calculated every edit,
   * eg consensus, quality or conservation graphs */
  public boolean autoCalculated = false;

  public String annotationId;

  public SequenceI sequenceRef;

  /** DOCUMENT ME!! */
  public String label;

  /** DOCUMENT ME!! */
  public String description;

  /** DOCUMENT ME!! */
  public Annotation[] annotations;

  public java.util.Hashtable sequenceMapping;

  /** DOCUMENT ME!! */
  public float graphMin;

  /** DOCUMENT ME!! */
  public float graphMax;

  /**
   * Score associated with label and description.
   */
  public double score= Double.NaN;
  /**
   * flag indicating if annotation has a score.
   */
  public boolean hasScore=false;

  public GraphLine threshold;

  // Graphical hints and tips

  /** DOCUMENT ME!! */
  public boolean editable = false;

  /** DOCUMENT ME!! */
  public boolean hasIcons; //

  /** DOCUMENT ME!! */
  public boolean hasText;

  /** DOCUMENT ME!! */
  public boolean visible = true;

  public int graphGroup = -1;

  /** DOCUMENT ME!! */
  public int height = 0;

  public int graph = 0;

  public int graphHeight = 40;

  public boolean padGaps = false;

  public static final int NO_GRAPH = 0;

  public static final int BAR_GRAPH = 1;

  public static final int LINE_GRAPH = 2;

  public boolean belowAlignment = true;


  public static int getGraphValueFromString(String string)
  {
    if (string.equalsIgnoreCase("BAR_GRAPH"))
    {
      return BAR_GRAPH;
    }
    else if (string.equalsIgnoreCase("LINE_GRAPH"))
    {
      return LINE_GRAPH;
    }
    else
    {
      return NO_GRAPH;
    }
  }

  /**
   * Creates a new AlignmentAnnotation object.
   *
   * @param label DOCUMENT ME!
   * @param description DOCUMENT ME!
   * @param annotations DOCUMENT ME!about:blank
Loading...
   */
  public AlignmentAnnotation(String label, String description,
                             Annotation[] annotations)
  {
    // always editable?
    editable = true;
    this.label = label;
    this.description = description;
    this.annotations = annotations;

     validateRangeAndDisplay();
  }

  void areLabelsSecondaryStructure()
  {
    boolean nonSSLabel = false;
    for (int i = 0; i < annotations.length; i++)
    {
      if (annotations[i] == null)
      {
        continue;
      }
      if (annotations[i].secondaryStructure == 'H' ||
          annotations[i].secondaryStructure == 'E')
      {
          hasIcons = true;
      }

      if(annotations[i].displayCharacter==null)
      {
        continue;
      }

      if (annotations[i].displayCharacter.length() == 1
          && !annotations[i].displayCharacter.equals("H")
          && !annotations[i].displayCharacter.equals("E")
          && !annotations[i].displayCharacter.equals("-")
          && !annotations[i].displayCharacter.equals("."))
        {
          if (jalview.schemes.ResidueProperties.aaIndex
                  [annotations[i].displayCharacter.charAt(0)] < 23)
          {
            nonSSLabel = true;
          }
        }

        if (annotations[i].displayCharacter.length() > 0)
        {
          hasText = true;
        }
      }

    if (nonSSLabel)
    {
      hasIcons = false;
      for (int j = 0; j < annotations.length; j++)
      {
        if (annotations[j] != null && annotations[j].secondaryStructure != ' ')
        {
          annotations[j].displayCharacter
              = String.valueOf(annotations[j].secondaryStructure);
          annotations[j].secondaryStructure = ' ';
        }

      }
    }

    annotationId = this.hashCode() + "";
  }
  /**
   * Creates a new AlignmentAnnotation object.
   *
   * @param label DOCUMENT ME!
   * @param description DOCUMENT ME!
   * @param annotations DOCUMENT ME!
   * @param min DOCUMENT ME!
   * @param max DOCUMENT ME!
   * @param winLength DOCUMENT ME!
   */
  public AlignmentAnnotation(String label, String description,
                             Annotation[] annotations, float min, float max,
                             int graphType)
  {
    // graphs are not editable
    editable = graphType==0;

    this.label = label;
    this.description = description;
    this.annotations = annotations;
    graph = graphType;
    graphMin = min;
    graphMax = max;
    validateRangeAndDisplay();
  }
  /**
   * checks graphMin and graphMax,
   * secondary structure symbols,
   * sets graphType appropriately,
   * sets null labels to the empty string
   * if appropriate.
   */
  private void validateRangeAndDisplay() {

    if (annotations==null)
    {
      visible=false; // try to prevent renderer from displaying.
      return; // this is a non-annotation row annotation - ie a sequence score.
    }

    int graphType = graph;
    float min = graphMin;
    float max = graphMax;
    boolean drawValues = true;

    if (min == max)
    {
      min = 999999999;
      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] == null)
        {
          continue;
        }

        if (drawValues
            && annotations[i].displayCharacter!=null
            && annotations[i].displayCharacter.length() > 1)
        {
          drawValues = false;
        }

        if (annotations[i].value > max)
        {
          max = annotations[i].value;
        }

        if (annotations[i].value < min)
        {
          min = annotations[i].value;
        }
      }
    }

    graphMin = min;
    graphMax = max;

    areLabelsSecondaryStructure();

    if (!drawValues && graphType != NO_GRAPH)
    {
      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] != null)
        {
          annotations[i].displayCharacter = "";
        }
      }
    }
  }

  /**
   * Copy constructor
   * creates a new independent annotation row with the same associated sequenceRef
   * @param annotation
   */
  public AlignmentAnnotation(AlignmentAnnotation annotation)
  {
    this.label = new String(annotation.label);
    if (annotation.description != null)
      this.description = new String(annotation.description);
    this.graphMin = annotation.graphMin;
    this.graphMax = annotation.graphMax;
    this.graph = annotation.graph;
    this.graphHeight = annotation.graphHeight;
    this.graphGroup = annotation.graphGroup;
    this.editable = annotation.editable;
    this.autoCalculated = annotation.autoCalculated;
    this.hasIcons = annotation.hasIcons;
    this.hasText = annotation.hasText;
    this.height = annotation.height;
    this.label = annotation.label;
    this.padGaps = annotation.padGaps;
    this.visible = annotation.visible;
    if (this.hasScore = annotation.hasScore)
    {
      this.score = annotation.score;
    }
    if (threshold!=null) {
      threshold = new GraphLine(annotation.threshold);
    }
    if (annotation.annotations!=null) {
      Annotation[] ann = annotation.annotations;
      this.annotations = new Annotation[ann.length];
      for (int i=0; i<ann.length; i++) {
        annotations[i] = new Annotation(ann[i]);
      };
      if (annotation.sequenceRef!=null) {
        this.sequenceRef = annotation.sequenceRef;
        if (annotation.sequenceMapping!=null)
        {
          Integer p=null;
          sequenceMapping = new Hashtable();
          Enumeration pos=annotation.sequenceMapping.keys();
          while (pos.hasMoreElements()) {
            // could optimise this!
            p = (Integer) pos.nextElement();
            Annotation a = (Annotation) annotation.sequenceMapping.get(p);
            if (a==null)
            {
              continue;
            }
            for (int i=0; i<ann.length; i++)
            {
              if (ann[i]==a)
              {
                sequenceMapping.put(p, annotations[i]);
              }
            }
          }
        } else {
          this.sequenceMapping = null;
        }
      }
    }
    validateRangeAndDisplay(); // construct hashcodes, etc.
  }

  /**
   * clip the annotation to the columns given by startRes and endRes (inclusive)
   * and prune any existing sequenceMapping to just those columns.
   * @param startRes
   * @param endRes
   */
  public void restrict(int startRes, int endRes)
  {
    if (startRes<0)
      startRes=0;
    if (startRes>=annotations.length)
      startRes = annotations.length-1;
    if (endRes>=annotations.length)
      endRes = annotations.length-1;
    if (annotations==null)
      return;
    Annotation[] temp = new Annotation[endRes-startRes+1];
    if (startRes<annotations.length)
    {
      System.arraycopy(annotations, startRes, temp, 0, endRes-startRes+1);
    }
    if (sequenceRef!=null) {
      // Clip the mapping, if it exists.
      int spos = sequenceRef.findPosition(startRes);
      int epos = sequenceRef.findPosition(endRes);
      if (sequenceMapping!=null)
      {
        Hashtable newmapping = new Hashtable();
        Enumeration e = sequenceMapping.keys();
        while (e.hasMoreElements())
        {
          Integer pos = (Integer) e.nextElement();
          if (pos.intValue()>=spos && pos.intValue()<=epos)
          {
            newmapping.put(pos, sequenceMapping.get(pos));
          }
        }
        sequenceMapping.clear();
        sequenceMapping = newmapping;
      }
    }
    annotations=temp;
  }
  /**
   * set the annotation row to be at least length Annotations
   * @param length minimum number of columns required in the annotation row
   * @return false if the annotation row is greater than length
   */
  public boolean padAnnotation(int length) {
    if (annotations==null)
    {
      return true; // annotation row is correct - null == not visible and undefined length
    }
    if (annotations.length<length)
    {
      Annotation[] na = new Annotation[length];
      System.arraycopy(annotations, 0, na, 0, annotations.length);
      annotations = na;
      return true;
    }
    return annotations.length>length;

  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String toString()
  {
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < annotations.length; i++)
    {
      if (annotations[i] != null)
      {
        if (graph != 0)
        {
          buffer.append(annotations[i].value);
        }
        else if (hasIcons)
        {
          buffer.append(annotations[i].secondaryStructure);
        }
        else
        {
          buffer.append(annotations[i].displayCharacter);
        }
      }

      buffer.append(", ");
    }

    if (label.equals("Consensus"))
    {
      buffer.append("\n");

      for (int i = 0; i < annotations.length; i++)
      {
        if (annotations[i] != null)
        {
          buffer.append(annotations[i].description);
        }

        buffer.append(", ");
      }
    }

    return buffer.toString();
  }

  public void setThreshold(GraphLine line)
  {
    threshold = line;
  }

  public GraphLine getThreshold()
  {
    return threshold;
  }

  /**
   * Attach the annotation to seqRef, starting from startRes position. If alreadyMapped is true then the indices of the annotation[] array are sequence positions rather than alignment column positions.
   * @param seqRef
   * @param startRes
   * @param alreadyMapped
   */
  public void createSequenceMapping(SequenceI seqRef,
                                    int startRes,
                                    boolean alreadyMapped)
  {

    if (seqRef == null)
    {
      return;
    }
    sequenceRef=seqRef;
    if (annotations==null)
    {
      return;
    }
    sequenceMapping = new java.util.Hashtable();

    int seqPos;

    for (int i = 0; i < annotations.length; i++)
    {
      if (annotations[i] != null)
      {
        if (alreadyMapped)
        {
          seqPos = seqRef.findPosition(i);
        }
        else
        {
          seqPos = i + startRes;
        }

        sequenceMapping.put(new Integer(seqPos), annotations[i]);
      }
    }

  }

  public void adjustForAlignment()
  {
    if (sequenceRef==null)
      return;

    if (annotations==null)
    {
      return;
    }

    int a = 0, aSize = sequenceRef.getLength();

    if (aSize == 0)
    {
      //Its been deleted
      return;
    }

    int position;
    Annotation[] temp = new Annotation[aSize];
    Integer index;

    for (a = sequenceRef.getStart(); a <= sequenceRef.getEnd(); a++)
    {
      index = new Integer(a);
      if (sequenceMapping.containsKey(index))
      {
        position = sequenceRef.findIndex(a) - 1;

        temp[position] = (Annotation) sequenceMapping.get(index);
      }
    }

    annotations = temp;
  }
  /**
   * remove any null entries in annotation row and return the
   * number of non-null annotation elements.
   * @return
   */
  private int compactAnnotationArray() {
    int j=0;
    for (int i=0;i<annotations.length; i++) {
      if (annotations[i]!=null && j!=i) {
        annotations[j++] = annotations[i];
      }
    }
    Annotation[] ann = annotations;
    annotations = new Annotation[j];
    System.arraycopy(ann, 0, annotations, 0, j);
    ann = null;
    return j;
  }

  /**
   * Associate this annotion with the aligned residues of a particular sequence.
   * sequenceMapping will be updated in the following way:
   *   null sequenceI - existing mapping will be discarded but annotations left in mapped positions.
   *   valid sequenceI not equal to current sequenceRef: mapping is discarded and rebuilt assuming 1:1 correspondence
   *   TODO: overload with parameter to specify correspondence between current and new sequenceRef
   * @param sequenceI
   */
  public void setSequenceRef(SequenceI sequenceI)
  {
    if (sequenceI != null)
    {
      if (sequenceRef != null)
      {
        if (sequenceRef != sequenceI && !sequenceRef.equals(sequenceI) && sequenceRef.getDatasetSequence()!=sequenceI.getDatasetSequence())
        {
          // if sequenceRef isn't intersecting with sequenceI
          // throw away old mapping and reconstruct.
          sequenceRef = null;
          if (sequenceMapping != null)
          {
            sequenceMapping = null;
            // compactAnnotationArray();
          }
          createSequenceMapping(sequenceI, 1, true);
          adjustForAlignment();
        }
        else
        {
          // Mapping carried over
          sequenceRef = sequenceI;
        }
      }
      else
      {
        // No mapping exists
        createSequenceMapping(sequenceI, 1, true);
        adjustForAlignment();
      }
    }
    else
    {
      // throw away the mapping without compacting.
      sequenceMapping = null;
      sequenceRef = null;
    }
  }

  /**
   * @return the score
   */
  public double getScore()
  {
    return score;
  }

  /**
   * @param score the score to set
   */
  public void setScore(double score)
  {
    hasScore=true;
    this.score = score;
  }
  /**
   *
   * @return true if annotation has an associated score
   */
  public boolean hasScore()
  {
    return hasScore || !Double.isNaN(score);
  }
  /**
   * Score only annotation
   * @param label
   * @param description
   * @param score
   */
  public AlignmentAnnotation(String label, String description, double score)
  {
    this(label, description, null);
    setScore(score);
  }

  public void setPadGaps(boolean padgaps, char gapchar)
  {
    this.padGaps = padgaps;
    if(padgaps)
    {
      hasText = true;
      for(int i=0; i<annotations.length; i++)
      {
        if(annotations[i]==null)
          annotations[i] = new Annotation(String.valueOf(gapchar),null,' ',0f);
        else if(annotations[i].displayCharacter==null ||annotations[i].displayCharacter.equals(" "))
          annotations[i].displayCharacter=String.valueOf(gapchar);
      }
    }
  }
}
