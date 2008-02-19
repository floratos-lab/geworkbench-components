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
package jalview.appletgui;

import java.util.*;

import java.awt.*;

import jalview.datamodel.*;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.1 $
 */
public class PaintRefresher
{
  static Hashtable components;

  /**
   * DOCUMENT ME!
   *
   * @param comp DOCUMENT ME!
   * @param al DOCUMENT ME!
   */
  public static void Register(Component comp, String seqSetId)
  {
    if (components == null)
    {
      components = new Hashtable();
    }

    if (components.containsKey(seqSetId))
    {
      Vector comps = (Vector) components.get(seqSetId);
      if (!comps.contains(comp))
      {
        comps.addElement(comp);
      }
    }
    else
    {
      Vector vcoms = new Vector();
      vcoms.addElement(comp);
      components.put(seqSetId, vcoms);
    }
  }

  public static void RemoveComponent(Component comp)
  {
    if (components == null)
    {
      return;
    }

    Enumeration en = components.keys();
    while (en.hasMoreElements())
    {
      String id = en.nextElement().toString();
      Vector comps = (Vector) components.get(id);
      comps.removeElement(comp);
      if (comps.size() == 0)
      {
        components.remove(id);
      }
    }
  }

  public static void Refresh(Component source, String id)
  {
    Refresh(source, id, false, false);
  }

  public static void Refresh(Component source,
                             String id,
                             boolean alignmentChanged,
                             boolean validateSequences)
  {
    if (components == null)
    {
      return;
    }

    Component comp;
    Vector comps = (Vector) components.get(id);

    if (comps == null)
    {
      return;
    }

    Enumeration e = comps.elements();
    while (e.hasMoreElements())
    {
      comp = (Component) e.nextElement();

      if (comp == source)
      {
        continue;
      }

      if (!comp.isValid())
      {
        comps.removeElement(comp);
      }
      else if (validateSequences
               && comp instanceof AlignmentPanel
               && source instanceof AlignmentPanel)
      {
        validateSequences( ( (AlignmentPanel) source).av.alignment,
                          ( (AlignmentPanel) comp).av.alignment);
      }

      if (comp instanceof AlignmentPanel && alignmentChanged)
      {
        ( (AlignmentPanel) comp).alignmentChanged();
      }

      comp.repaint();
    }
  }

  static void validateSequences(AlignmentI source, AlignmentI comp)
  {
    SequenceI[] a1;
    if (source.getHiddenSequences().getSize() > 0)
    {
      a1 = source.getHiddenSequences().getFullAlignment().getSequencesArray();
    }
    else
    {
      a1 = source.getSequencesArray();
    }

    SequenceI[] a2;
    if (comp.getHiddenSequences().getSize() > 0)
    {
      a2 = comp.getHiddenSequences().getFullAlignment().getSequencesArray();
    }
    else
    {
      a2 = comp.getSequencesArray();
    }

    int i, iSize = a1.length, j, jSize = a2.length;

    if (iSize == jSize)
    {
      return;
    }

    boolean exists = false;
    for (i = 0; i < iSize; i++)
    {
      exists = false;

      for (j = 0; j < jSize; j++)
      {
        if (a2[j] == a1[i])
        {
          exists = true;
          break;
        }
      }

      if (!exists)
      {
        if (i < comp.getHeight())
        {
          comp.getSequences().insertElementAt(a1[i], i);
        }
        else
        {
          comp.addSequence(a1[i]);
        }

        if (comp.getHiddenSequences().getSize() > 0)
        {
          a2 = comp.getHiddenSequences().getFullAlignment().getSequencesArray();
        }
        else
        {
          a2 = comp.getSequencesArray();
        }

        jSize = a2.length;
      }
    }

    iSize = a1.length;
    jSize = a2.length;

    for (j = 0; j < jSize; j++)
    {
      exists = false;
      for (i = 0; i < iSize; i++)
      {
        if (a2[j] == a1[i])
        {
          exists = true;
          break;
        }
      }

      if (!exists)
      {
        comp.deleteSequence(a2[j]);
      }
    }
  }
}
