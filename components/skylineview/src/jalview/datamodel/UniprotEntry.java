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

public class UniprotEntry
{

  UniprotSequence sequence;
  Vector name;
  Vector accession;
  Vector feature;
  Vector dbrefs;
  UniprotProteinName protName;

  public void setAccession(Vector items)
  {
    accession = items;
  }

  public void setFeature(Vector items)
  {
    feature = items;
  }

  public Vector getFeature()
  {
    return feature;
  }

  public Vector getAccession()
  {
    return accession;
  }

  public void setProtein(UniprotProteinName names)
  {
    protName = names;
  }

  public UniprotProteinName getProtein()
  {
    return protName;
  }

  public void setName(Vector na)
  {
    name = na;
  }

  public Vector getName()
  {
    return name;
  }

  public UniprotSequence getUniprotSequence()
  {
    return sequence;
  }

  public void setUniprotSequence(UniprotSequence seq)
  {
    sequence = seq;
  }

  public Vector getDbReference()
  {
    return dbrefs;
  }

  public void setDbReference(Vector dbref)
  {
    this.dbrefs = dbref;
  }

}
