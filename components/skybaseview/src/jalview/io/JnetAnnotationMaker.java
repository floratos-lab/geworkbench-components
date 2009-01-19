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
*/package jalview.io;

import jalview.datamodel.*;

public class JnetAnnotationMaker
{
  public static void add_annotation(JPredFile prediction, AlignmentI al,
                                    int firstSeq, boolean noMsa)
      throws Exception
  {
    JnetAnnotationMaker.add_annotation(prediction, al, firstSeq, noMsa, (int[])null);
  }

  /**
   * adds the annotation parsed by prediction to al.
   * @param prediction JPredFile
   * @param al AlignmentI
   * @param firstSeq int -
   * @param noMsa boolean
   * @param delMap mapping from columns in JPredFile prediction to residue number in al.getSequence(firstSeq)
   */
  public static void add_annotation(JPredFile prediction, AlignmentI al,
                                    int firstSeq, boolean noMsa, int[] delMap)
      throws Exception
  {
    int i = 0;
    SequenceI[] preds = prediction.getSeqsAsArray();
    // in the future we could search for the query
    // sequence in the alignment before calling this function.
    SequenceI seqRef = al.getSequenceAt(firstSeq);
    int width = preds[0].getSequence().length;
    int[] gapmap = al.getSequenceAt(firstSeq).gapMap();
    if ( (delMap != null && delMap.length > width) ||
        (delMap == null && gapmap.length != width))
    {
      throw (new Exception(
          "Number of residues in " + (delMap == null ? "" : " mapped ") +
          "supposed query sequence ('" +
          al.getSequenceAt(firstSeq).getName() + "'\n" +
          al.getSequenceAt(firstSeq).getSequenceAsString() +
          ")\ndiffer from number of prediction sites in prediction (" + width +
          ")"));
    }

    AlignmentAnnotation annot;
    Annotation[] annotations = null;

    int existingAnnotations = 0;
    if (al.getAlignmentAnnotation() != null)
    {
      existingAnnotations = al.getAlignmentAnnotation().length;
    }

    while (i < preds.length)
    {
      String id = preds[i].getName().toUpperCase();

      if (id.startsWith("LUPAS") || id.startsWith("JNET") ||
          id.startsWith("JPRED"))
      {
        annotations = new Annotation[al.getWidth()];
        /*        if (delMap!=null) {
          for (int j=0; j<annotations.length; j++)
            annotations[j] = new Annotation("","",'',0);
                 }
         */
        if (id.equals("JNETPRED") || id.equals("JNETPSSM") ||
            id.equals("JNETFREQ") || id.equals("JNETHMM") ||
            id.equals("JNETALIGN") || id.equals("JPRED"))
        {
          if (delMap == null)
          {
            for (int j = 0; j < width; j++)
            {
              annotations[gapmap[j]] = new Annotation("", "",
                  preds[i].getCharAt(j), 0);
            }
          }
          else
          {
            for (int j = 0; j < width; j++)
            {
              annotations[gapmap[delMap[j]]] = new Annotation("", "",
                  preds[i].getCharAt(j), 0);
            }
          }
        }
        else if (id.equals("JNETCONF"))
        {
          if (delMap == null)
          {
            for (int j = 0; j < width; j++)
            {
              float value = new Float(preds[i].getCharAt(
                  j) + "").floatValue();
              annotations[gapmap[j]] = new Annotation(preds[i].getCharAt(
                  j) + "", "", preds[i].getCharAt(j),
                  value);
            }
          }
          else
          {
            for (int j = 0; j < width; j++)
            {
              float value = new Float(preds[i].getCharAt(
                  j) + "").floatValue();
              annotations[gapmap[delMap[j]]] = new Annotation(preds[i].
                  getCharAt(
                      j) + "", "", preds[i].getCharAt(j),
                  value);
            }
          }
        }
        else
        {
          if (delMap == null)
          {
            for (int j = 0; j < width; j++)
            {
              annotations[gapmap[j]] = new Annotation(preds[i].getCharAt(
                  j) + "", "", ' ', 0);
            }
          }
          else
          {
            for (int j = 0; j < width; j++)
            {
              annotations[gapmap[delMap[j]]] = new Annotation(preds[i].
                  getCharAt(
                      j) + "", "", ' ', 0);
            }
          }
        }

        if (id.equals("JNETCONF"))
        {
          annot = new AlignmentAnnotation(preds[i].getName(),
                                          "JNet Output", annotations, 0f,
                                          10f,
                                          AlignmentAnnotation.BAR_GRAPH);
        }
        else
        {
          annot = new AlignmentAnnotation(preds[i].getName(),
                                          "JNet Output", annotations);
        }

        if (seqRef != null)
        {
          annot.createSequenceMapping(seqRef, 1, true);
          seqRef.addAlignmentAnnotation(annot);
        }

        al.addAnnotation(annot);
        al.setAnnotationIndex(annot,
                              al.getAlignmentAnnotation().
                              length - existingAnnotations - 1);

        if (noMsa)
        {
          al.deleteSequence(preds[i]);
        }
      }

      i++;
    }

    //Hashtable scores = prediction.getScores();

    /*  addFloatAnnotations(al, gapmap,  (Vector)scores.get("JNETPROPH"),
                          "JnetpropH", "Jnet Helix Propensity", 0f,1f,1);

      addFloatAnnotations(al, gapmap,  (Vector)scores.get("JNETPROPB"),
     "JnetpropB", "Jnet Beta Sheet Propensity", 0f,1f,1);

      addFloatAnnotations(al, gapmap,  (Vector)scores.get("JNETPROPC"),
                          "JnetpropC", "Jnet Coil Propensity", 0f,1f,1);
     */

  }
}
