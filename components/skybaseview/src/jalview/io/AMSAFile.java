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

public class AMSAFile
    extends jalview.io.FastaFile
{

  AlignmentI al;
  /**
   * Creates a new AMSAFile object for output.
   */
  public AMSAFile(AlignmentI al)
  {
    this.al = al;
  }

  /**
   * DOCUMENT ME!
   *
   * @return DOCUMENT ME!
   */
  public String print()
  {
    super.print(getSeqsAsArray());

    AlignmentAnnotation aa;
    if (al.getAlignmentAnnotation() != null)
    {

      for (int i = 0; i < al.getAlignmentAnnotation().length; i++)
      {
        aa = al.getAlignmentAnnotation()[i];


        if (aa.autoCalculated || !aa.visible)
        {
          continue;
        }

        out.append(">#_" + aa.label);
        if (aa.description != null)
        {
          out.append(" " + aa.description);
        }

        out.append("\n");

        int nochunks = Math.min(aa.annotations.length, al.getWidth())
                        / len + 1;

        for (int j = 0; j < nochunks; j++)
        {
          int start = j * len;
          int end = start + len;
          if (end > aa.annotations.length)
          {
            end = aa.annotations.length;
          }

          String ch;
          for (int k = start; k < end; k++)
          {
            if (aa.annotations[k] == null)
            {
              ch = " ";
            }
            else
            {
              ch = aa.annotations[k].displayCharacter;
            }

            out.append(ch);

          }
          out.append("\n");
        }
      }
    }
    return out.toString();
  }
}
