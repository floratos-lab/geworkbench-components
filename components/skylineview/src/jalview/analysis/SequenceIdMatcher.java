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
package jalview.analysis;

import java.util.*;

import jalview.datamodel.*;

/**
 * <p>Title: </p>
 * SequenceIdMatcher
 * <p>Description: </p>
 * Routine which does approximate Sequence Id resolution by name using
 * string containment (on word boundaries) rather than equivalence. It also
 * attempts to resolve ties where no exact match is available by picking the
 * the id closest to the query.
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: Dundee University</p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SequenceIdMatcher
{
  private Hashtable names;

  public SequenceIdMatcher(SequenceI[] seqs)
  {
    names = new Hashtable();
    for (int i = 0; i < seqs.length; i++)
    {
      names.put(new SeqIdName(seqs[i].getName()), seqs[i]);
    }
  }

  /**
   * returns the closest SequenceI in matches to SeqIdName and returns all the matches
   * to the names hash.
   * @param candName SeqIdName
   * @param matches Vector of SequenceI objects
   * @return SequenceI closest SequenceI to SeqIdName
   */
  private SequenceI pickbestMatch(SeqIdName candName, Vector matches)
  {
    SequenceI match = null;
    if (candName == null || matches == null || matches.size() == 0)
    {
      return null;
    }
    match = (SequenceI) matches.elementAt(0);
    matches.removeElementAt(0);
    names.put(new SeqIdName(match.getName()), match);
    int matchlen = match.getName().length();
    int namlen = candName.id.length();
    while (matches.size() > 0)
    {
      // look through for a better one.
      SequenceI cand = (SequenceI) matches.elementAt(0);
      names.put(new SeqIdName(cand.getName()), cand);
      int candlen = cand.getName().length();
      // keep the one with an id 'closer' to the given seqnam string
      if (Math.abs(matchlen - namlen) > Math.abs(candlen - namlen) &&
          candlen > matchlen)
      {
        match = cand;
        matchlen = candlen;
      }
    }
    return match;
  }

  /**
   * get SequenceI with closest SequenceI.getName() to seq.getName()
   * @param seq SequenceI
   * @return SequenceI
   */
  SequenceI findIdMatch(SequenceI seq)
  {
    SeqIdName nam = new SeqIdName(seq.getName());
    return findIdMatch(nam);
  }

  SequenceI findIdMatch(String seqnam)
  {
    SeqIdName nam = new SeqIdName(seqnam);
    return findIdMatch(nam);
  }

  /**
   * findIdMatch
   *
   * Return pointers to sequences (or sequence object containers)
   * which have same Id as a given set of different sequence objects
   *
   * @param seqs SequenceI[]
   * @return SequenceI[]
   */
  SequenceI[] findIdMatch(SequenceI[] seqs)
  {
    SequenceI[] namedseqs = null;
    int i = 0;
    SeqIdName nam;

    if (seqs.length > 0)
    {
      namedseqs = new SequenceI[seqs.length];
      do
      {
        nam = new SeqIdName(seqs[i].getName());

        if (names.containsKey(nam))
        {
          namedseqs[i] = findIdMatch(nam);
        }
        else
        {
          namedseqs[i] = null;
        }
      }
      while (++i < seqs.length);
    }

    return namedseqs;
  }

  /**
   * core findIdMatch search method
   * @param nam SeqIdName
   * @return SequenceI
   */
  private SequenceI findIdMatch(jalview.analysis.SequenceIdMatcher.SeqIdName
                                nam)
  {
    Vector matches = new Vector();
    while (names.containsKey(nam))
    {
      matches.addElement(names.remove(nam));
    }
    return pickbestMatch(nam, matches);
  }

  private class SeqIdName
  {
    String id;

    SeqIdName(String s)
    {
      if (s != null)
      {
        id = new String(s);
      }
      else
      {
        id = "";
      }
    }

    public int hashCode()
    {
      return ( (id.length() >= 4) ? id.substring(0, 4).hashCode() : id.hashCode());
    }

    public boolean equals(Object s)
    {
      if (s instanceof SeqIdName)
      {
        return this.equals( (SeqIdName) s);
      }
      else
      {
        if (s instanceof String)
        {
          return this.equals( (String) s);
        }
      }

      return false;
    }

    /**
     * Characters that define the end of a unique sequence ID at
     * the beginning of an arbitrary ID string
     * JBPNote: This is a heuristic that will fail for arbritrarily extended sequence id's
     * (like portions of an aligned set of repeats from one sequence)
     */
    private String WORD_SEP = "~. |#\\/<>!\"£$%^*)}[@',?_";

    /**
     * matches if one ID properly contains another at a whitespace boundary.
     * TODO: (JBPNote) These are not efficient. should use char[] for speed
     * todo: (JBPNote) Set separator characters appropriately
     * @param s SeqIdName
     * @return boolean
     */
    public boolean equals(SeqIdName s)
    {
      if (id.length() > s.id.length())
      {
        return id.startsWith(s.id) ?
            (WORD_SEP.indexOf(id.charAt(s.id.length())) > -1)
            : false;
      }
      else
      {
        return s.id.startsWith(id) ?
            (s.id.equals(id) ? true :
             (WORD_SEP.indexOf(s.id.charAt(id.length())) > -1))
            : false;
      }
    }

    public boolean equals(String s)
    {
      if (id.length() > s.length())
      {
        return id.startsWith(s) ?
            (WORD_SEP.indexOf(id.charAt(s.length())) > -1)
            : false;
      }
      else
      {
        return s.startsWith(id) ?
            (s.equals(id) ? true :
             (WORD_SEP.indexOf(s.charAt(id.length())) > -1))
            : false;
      }
    }
  }
}
