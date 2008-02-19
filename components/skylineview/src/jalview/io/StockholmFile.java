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
/*
 * This extension was written by Benjamin Schuster-Boeckler at sanger.ac.uk
 */
package jalview.io;

import java.io.*;
import java.util.*;

import com.stevesoft.pat.*;
import jalview.datamodel.*;

//import org.apache.log4j.*;

/**
 * This class is supposed to parse a Stockholm format file into Jalview
 * @author bsb at sanger.ac.uk
 * @version 0.3
 */
public class StockholmFile
    extends AlignFile
{
  //static Logger logger = Logger.getLogger("jalview.io.StockholmFile");

  public StockholmFile()
  {
  }

  public StockholmFile(String inFile, String type)
      throws IOException
  {
    super(inFile, type);
  }

  public void initData()
  {
    super.initData();
  }

  /**
   * Parse a file in Stockholm format into Jalview's data model. The file has
   * to be passed at construction time
   * @throws IOException If there is an error with the input file
   */
  public void parse()
      throws IOException
  {
    StringBuffer treeString=new StringBuffer();
    String treeName = null;
    // --------------- Variable Definitions -------------------
    String line;
    String version;
    //  String id;
     Hashtable seqAnn = new Hashtable(); // Sequence related annotations
    Hashtable seqs = new Hashtable();
    Regex p, r, rend, s, x;

    // ------------------ Parsing File ----------------------
    // First, we have to check that this file has STOCKHOLM format, i.e. the first line must match
    r = new Regex("# STOCKHOLM ([\\d\\.]+)");
    if (!r.search(nextLine()))
    {
      throw new IOException("This file is not in valid STOCKHOLM format: First line does not contain '# STOCKHOLM'");
    }
    else
    {
      version = r.stringMatched(1);
      //logger.debug("Stockholm version: " + version);
    }

//      We define some Regexes here that will be used regularily later
    rend = new Regex("\\/\\/"); // Find the end of an alignment
    p = new Regex("(\\S+)\\/(\\d+)\\-(\\d+)"); // split sequence id in id/from/to
    s = new Regex("(\\S+)\\s+(\\w{2})\\s+(.*)"); // Parses annotation subtype
    r = new Regex("#=(G[FSRC]?)\\s+(.*)"); // Finds any annotation line
    x = new Regex("(\\S+)\\s+(\\S+)"); //split id from sequence

    rend.optimize();
    p.optimize();
    s.optimize();
    r.optimize();
    x.optimize();

    while ( (line = nextLine()) != null)
    {
      if (line.length() == 0)
      {
        continue;
      }
      if (rend.search(line))
      {
//              End of the alignment, pass stuff back

        this.noSeqs = seqs.size();
        //logger.debug("Number of sequences: " + this.noSeqs);
        Enumeration accs = seqs.keys();
        while (accs.hasMoreElements())
        {
          String acc = (String) accs.nextElement();
          //logger.debug("Processing sequence " + acc);
          String seq = (String) seqs.get(acc);
          if (maxLength < seq.length())
          {
            maxLength = seq.length();
          }
          int start = 1;
          int end = -1;
          String sid = acc;
          // Retrieve hash of annotations for this accession
          Hashtable accAnnotations = null;
          
          if (seqAnn!=null && seqAnn.containsKey(acc))
          {
            accAnnotations = (Hashtable) seqAnn.get(acc);
          }
          
          // Split accession in id and from/to
          if (p.search(acc))
          {
            sid = p.stringMatched(1);
            start = Integer.parseInt(p.stringMatched(2));
            end = Integer.parseInt(p.stringMatched(3));
          }
          //logger.debug(sid + ", " + start + ", " + end);

          Sequence seqO = new Sequence(sid, seq, start, end);
          // Add Description (if any)
          if (accAnnotations!=null && accAnnotations.containsKey("DE"))
          {
            String desc = (String) accAnnotations.get("DE");
            seqO.setDescription((desc==null)?"" : desc);
          }
          // Add DB References (if any)
          if (accAnnotations!=null && accAnnotations.containsKey("DR"))
          {
            String dbr = (String) accAnnotations.get("DR");
            if (dbr!=null && dbr.indexOf(";")>-1)
            {
              String src = dbr.substring(0, dbr.indexOf(";"));
              String acn = dbr.substring(dbr.indexOf(";")+1);
              DBRefEntry dbref = new DBRefEntry(jalview.util.DBRefUtils.getCanonicalName(src), acn, "");
              seqO.addDBRef(dbref);
            }
          }
          Hashtable features = null;
          // We need to adjust the positions of all features to account for gaps
          try
          {
            features = (Hashtable) accAnnotations.get(
                "features");
          }
          catch (java.lang.NullPointerException e)
          {
            //loggerwarn("Getting Features for " + acc + ": " + e.getMessage());
            //continue;
          }
          // if we have features
          if (features != null)
          {
            Enumeration i = features.keys();
            while (i.hasMoreElements())
            {
              // TODO: parse out secondary structure annotation as annotation row
              // TODO: parse out scores as annotation row
              // TODO: map coding region to core jalview feature types
              String type = i.nextElement().toString();
              Hashtable content = (Hashtable) features.get(type);

              Enumeration j = content.keys();
              while (j.hasMoreElements())
              {
                String desc = j.nextElement().toString();
                String ns = content.get(desc).toString();
                char[] byChar = ns.toCharArray();
                for (int k = 0; k < byChar.length; k++)
                {
                  char c = byChar[k];
                  if (! (c == ' ' || c == '_' ||
                         c == '-'))
                  {
                    int new_pos = seqO.findPosition(k);
                    SequenceFeature feat =
                        new SequenceFeature(type,
                                            desc, new_pos, new_pos, 0f, null);

                    seqO.addSequenceFeature(feat);
                  }
                }
              }

            }

          }
          //logger.debug("Adding seq " + acc + " from "  + start + " to " + end + ": " + seq);
          this.seqs.addElement(seqO);
        }
      }
      else if (!r.search(line))
      {
        //System.err.println("Found sequence line: " + line);

        // Split sequence in sequence and accession parts
        if (!x.search(line))
        {
          //logger.error("Could not parse sequence line: " + line);
          throw new IOException("Could not parse sequence line: " + line);
        }
        String ns = (String) seqs.get(x.stringMatched(1));
        if (ns == null)
        {
          ns = "";
        }
        ns += x.stringMatched(2);

        seqs.put(x.stringMatched(1), ns);
      }
      else
      {
        String annType = r.stringMatched(1);
        String annContent = r.stringMatched(2);

        //System.err.println("type:" + annType + " content: " + annContent);

        if (annType.equals("GF"))
        {
          /* Generic per-File annotation, free text
           * Magic features:
           * #=GF NH <tree in New Hampshire eXtended format>
           * #=GF TN <Unique identifier for the next tree>
           * Pfam descriptions:
              7. DESCRIPTION OF FIELDS

                 Compulsory fields:
                 ------------------

                 AC   Accession number:           Accession number in form PFxxxxx.version or PBxxxxxx.
                 ID   Identification:             One word name for family.
                 DE   Definition:                 Short description of family.
                 AU   Author:                     Authors of the entry.
                 SE   Source of seed:             The source suggesting the seed members belong to one family.
           GA   Gathering method:           Search threshold to build the full alignment.
                 TC   Trusted Cutoff:             Lowest sequence score and domain score of match in the full alignment.
                 NC   Noise Cutoff:               Highest sequence score and domain score of match not in full alignment.
                 TP   Type:                       Type of family -- presently Family, Domain, Motif or Repeat.
           SQ   Sequence:                   Number of sequences in alignment.
                 AM   Alignment Method        The order ls and fs hits are aligned to the model to build the full align.
                 //                               End of alignment.

                 Optional fields:
                 ----------------

           DC   Database Comment:           Comment about database reference.
           DR   Database Reference:         Reference to external database.
           RC   Reference Comment:          Comment about literature reference.
                 RN   Reference Number:           Reference Number.
           RM   Reference Medline:          Eight digit medline UI number.
                 RT   Reference Title:            Reference Title.
                 RA   Reference Author:           Reference Author
                 RL   Reference Location:         Journal location.
           PI   Previous identifier:        Record of all previous ID lines.
                 KW   Keywords:                   Keywords.
                 CC   Comment:                    Comments.
                 NE   Pfam accession:         Indicates a nested domain.
                 NL   Location:                   Location of nested domains - sequence ID, start and end of insert.

                 Obsolete fields:
                 -----------
           AL   Alignment method of seed:   The method used to align the seed members.
           */
          // Let's save the annotations, maybe we'll be able to do something with them later...
          Regex an = new Regex("(\\w+)\\s*(.*)");
          if (an.search(annContent))
          {
            if (an.stringMatched(1).equals("NH"))
            {
              treeString.append(an.stringMatched(2));
            } else 
            if (an.stringMatched(1).equals("TN")) {
              if (treeString.length()>0)
              {
                if (treeName==null)
                {
                  treeName = "Tree "+(getTreeCount()+1);
                }
                addNewickTree(treeName, treeString.toString());
              }
              treeName = an.stringMatched(2);
              treeString = new StringBuffer();
            }
            setAlignmentProperty(an.stringMatched(1), an.stringMatched(2));
          }
        }
        else if (annType.equals("GS"))
        {
          // Generic per-Sequence annotation, free text
          /* Pfam uses these features:
              Feature                    Description
              ---------------------      -----------
              AC <accession>             ACcession number
              DE <freetext>              DEscription
              DR <db>; <accession>;      Database Reference
              OS <organism>              OrganiSm (species)
              OC <clade>                 Organism Classification (clade, etc.)
              LO <look>                  Look (Color, etc.)
           */
          if (s.search(annContent))
          {
            String acc = s.stringMatched(1);
            String type = s.stringMatched(2);
            String content = s.stringMatched(3);
            // TODO: store DR in a vector.
            // TODO: store AC according to generic file db annotation.
            Hashtable ann;
            if (seqAnn.containsKey(acc))
            {
              ann = (Hashtable) seqAnn.get(acc);
            }
            else
            {
              ann = new Hashtable();
            }
            ann.put(type, content);
            seqAnn.put(acc, ann);
          }
          else
          {
            throw new IOException("Error parsing " + line);
          }
        }
        else if (annType.equals("GC"))
        {
          // Generic per-Column annotation, exactly 1 char per column
          // always need a label.
          if (x.search(annContent))
          {
            // parse out and create alignment annotation directly.
            AlignmentAnnotation annotation = parseAnnotationRow(x.stringMatched(1), x.stringMatched(2));
            annotations.addElement(annotation);
          }
        }
        else if (annType.equals("GR"))
        {
          // Generic per-Sequence AND per-Column markup, exactly 1 char per column
          /*
              Feature   Description            Markup letters
              -------   -----------            --------------
              SS        Secondary Structure    [HGIEBTSCX]
              SA        Surface Accessibility  [0-9X]
                            (0=0%-10%; ...; 9=90%-100%)
              TM        TransMembrane          [Mio]
              PP        Posterior Probability  [0-9*]
                            (0=0.00-0.05; 1=0.05-0.15; *=0.95-1.00)
              LI        LIgand binding         [*]
              AS        Active Site            [*]
              IN        INtron (in or after)   [0-2]
           */
          if (s.search(annContent))
          {
            String acc = s.stringMatched(1);
            String type = s.stringMatched(2);
            String seq = s.stringMatched(3);
            String description = new String();

            // Check for additional information about the current annotation
            if (x.search(seq))
            {
              description = x.stringMatched(1);
              seq = x.stringMatched(2);
            }
            // sequence id with from-to fields

            Hashtable ann;
            // Get an object with all the annotations for this sequence
            if (seqAnn.containsKey(acc))
            {
              //logger.debug("Found annotations for " + acc);
              ann = (Hashtable) seqAnn.get(acc);
            }
            else
            {
              //logger.debug("Creating new annotations holder for " + acc);
              ann = new Hashtable();
              seqAnn.put(acc, ann);
            }

            Hashtable features;
            // Get an object with all the content for an annotation
            if (ann.containsKey("features"))
            {
              //logger.debug("Found features for " + acc);
              features = (Hashtable) ann.get("features");
            }
            else
            {
              //logger.debug("Creating new features holder for " + acc);
              features = new Hashtable();
              ann.put("features", features);
            }

            Hashtable content;
            if (features.containsKey(this.id2type(type)))
            {
              //logger.debug("Found content for " + this.id2type(type));
              content = (Hashtable) features.get(this.id2type(type));
            }
            else
            {
              //logger.debug("Creating new content holder for " + this.id2type(type));
              content = new Hashtable();
              features.put(this.id2type(type), content);
            }
            String ns = (String) content.get(description);
            if (ns == null)
            {
              ns = "";
            }
            ns += seq;
            content.put(description, seq);
          }
          else
          {
            throw new IOException("Error parsing " + line);
          }
        }
        else
        {
          throw new IOException("Unknown annotation detected: " + annType + " " +
                                annContent);
        }
      }
    }
    if (treeString.length()>0)
    {
      if (treeName==null)
      {
        treeName = "Tree "+(1+getTreeCount());
      }
      addNewickTree(treeName, treeString.toString());
    }
  }

  private AlignmentAnnotation parseAnnotationRow(String label, String annots)
  {
    String type = (label.indexOf("_cons")==label.length()-5) ? label.substring(0, label.length()-5)
            : label;
    boolean ss = false;
    type = id2type(type);
    if (type.equals("secondary structure"))
    {
      ss=true;
    }
    // decide on secondary structure or not.
    Annotation[] els = new Annotation[annots.length()];
    for (int i = 0; i<annots.length(); i++)
    {
      String pos = annots.substring(i,i+1);
      Annotation ann;
      ann = new Annotation(pos, "", ' ', Float.NaN);
      if (ss)
      {
        ann.secondaryStructure = jalview.schemes.ResidueProperties.getDssp3state(pos).charAt(0);
        if (ann.secondaryStructure == pos.charAt(0) || pos.charAt(0)=='C')
        {
          ann.displayCharacter = "";
        } else {
          ann.displayCharacter += " ";
        }
      }
      
      els[i] = ann;
    }
    return new AlignmentAnnotation(label, label, els);
  }
  
  public static String print(SequenceI[] s)
  {
    return "not yet implemented";
  }

  public String print()
  {
    return print(getSeqsAsArray());
  }

  private String id2type(String id)
  {
    // GR ids
    if (id.equals("SS"))
    {
      return "secondary structure";
    }
    else if (id.equals("SA"))
    {
      return "surface accessibility";
    }
    else if (id.equals("TM"))
    {
      return "transmembrane";
    }
    else if (id.equals("PP"))
    {
      return "posterior probability";
    }
    else if (id.equals("LI"))
    {
      return "ligand binding";
    }
    else if (id.equals("AS"))
    {
      return "active site";
    }
    else if (id.equals("IN"))
    {
      return "intron";
    }
    else if (id.equals("IR"))
    {
      return "interacting residue";
    }
    // GS ids
    else if (id.equals("AC"))
    {
      return "accession";
    }
    else if (id.equals("OS"))
    {
      return "organism";
    }
    else if (id.equals("CL"))
    {
      return "class";
    }
    else if (id.equals("DE"))
    {
      return "description";
    }
    else if (id.equals("DR"))
    {
      return "reference";
    }
    else if (id.equals("LO"))
    {
      return "look";
    }
    else
    {
      return null;
    }
  }
}
