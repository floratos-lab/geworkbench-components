package jalview.datamodel.xdb.embl;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class EmblEntry
{
  String accession;

  String version;

  String taxDivision;

  String desc;

  String rCreated;

  String rLastUpdated;

  String lastUpdated;

  Vector keywords;

  Vector refs;

  Vector dbRefs;

  Vector features;

  EmblSequence sequence;

  /**
   * @return the accession
   */
  public String getAccession()
  {
    return accession;
  }

  /**
   * @param accession
   *          the accession to set
   */
  public void setAccession(String accession)
  {
    this.accession = accession;
  }

  /**
   * @return the dbRefs
   */
  public Vector getDbRefs()
  {
    return dbRefs;
  }

  /**
   * @param dbRefs
   *          the dbRefs to set
   */
  public void setDbRefs(Vector dbRefs)
  {
    this.dbRefs = dbRefs;
  }

  /**
   * @return the desc
   */
  public String getDesc()
  {
    return desc;
  }

  /**
   * @param desc
   *          the desc to set
   */
  public void setDesc(String desc)
  {
    this.desc = desc;
  }

  /**
   * @return the features
   */
  public Vector getFeatures()
  {
    return features;
  }

  /**
   * @param features
   *          the features to set
   */
  public void setFeatures(Vector features)
  {
    this.features = features;
  }

  /**
   * @return the keywords
   */
  public Vector getKeywords()
  {
    return keywords;
  }

  /**
   * @param keywords
   *          the keywords to set
   */
  public void setKeywords(Vector keywords)
  {
    this.keywords = keywords;
  }

  /**
   * @return the lastUpdated
   */
  public String getLastUpdated()
  {
    return lastUpdated;
  }

  /**
   * @param lastUpdated
   *          the lastUpdated to set
   */
  public void setLastUpdated(String lastUpdated)
  {
    this.lastUpdated = lastUpdated;
  }

  /**
   * @return the refs
   */
  public Vector getRefs()
  {
    return refs;
  }

  /**
   * @param refs
   *          the refs to set
   */
  public void setRefs(Vector refs)
  {
    this.refs = refs;
  }

  /**
   * @return the releaseCreated
   */
  public String getRCreated()
  {
    return rCreated;
  }

  /**
   * @param releaseCreated
   *          the releaseCreated to set
   */
  public void setRcreated(String releaseCreated)
  {
    this.rCreated = releaseCreated;
  }

  /**
   * @return the releaseLastUpdated
   */
  public String getRLastUpdated()
  {
    return rLastUpdated;
  }

  /**
   * @param releaseLastUpdated
   *          the releaseLastUpdated to set
   */
  public void setRLastUpdated(String releaseLastUpdated)
  {
    this.rLastUpdated = releaseLastUpdated;
  }

  /**
   * @return the sequence
   */
  public EmblSequence getSequence()
  {
    return sequence;
  }

  /**
   * @param sequence
   *          the sequence to set
   */
  public void setSequence(EmblSequence sequence)
  {
    this.sequence = sequence;
  }

  /**
   * @return the taxDivision
   */
  public String getTaxDivision()
  {
    return taxDivision;
  }

  /**
   * @param taxDivision
   *          the taxDivision to set
   */
  public void setTaxDivision(String taxDivision)
  {
    this.taxDivision = taxDivision;
  }

  /**
   * @return the version
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * @param version
   *          the version to set
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  /*
   * EMBL Feature support is limited. The text below is included for the benefit
   * of any developer working on improving EMBL feature import in Jalview.
   * Extract from EMBL feature specification see
   * http://www.embl-ebi.ac.uk/embl/Documentation/FT_definitions/feature_table.html
   * 3.5 Location 3.5.1 Purpose
   * 
   * The location indicates the region of the presented sequence which
   * corresponds to a feature.
   * 
   * 3.5.2 Format and conventions The location contains at least one sequence
   * location descriptor and may contain one or more operators with one or more
   * sequence location descriptors. Base numbers refer to the numbering in the
   * entry. This numbering designates the first base (5' end) of the presented
   * sequence as base 1. Base locations beyond the range of the presented
   * sequence may not be used in location descriptors, the only exception being
   * location in a remote entry (see 3.5.2.1, e).
   * 
   * Location operators and descriptors are discussed in more detail below.
   * 
   * 3.5.2.1 Location descriptors
   * 
   * The location descriptor can be one of the following: (a) a single base
   * number (b) a site between two indicated adjoining bases (c) a single base
   * chosen from within a specified range of bases (not allowed for new entries)
   * (d) the base numbers delimiting a sequence span (e) a remote entry
   * identifier followed by a local location descriptor (i.e., a-d)
   * 
   * A site between two adjoining nucleotides, such as endonucleolytic cleavage
   * site, is indicated by listing the two points separated by a carat (^). The
   * permitted formats for this descriptor are n^n+1 (for example 55^56), or,
   * for circular molecules, n^1, where "n" is the full length of the molecule,
   * ie 1000^1 for circular molecule with length 1000.
   * 
   * A single base chosen from a range of bases is indicated by the first base
   * number and the last base number of the range separated by a single period
   * (e.g., '12.21' indicates a single base taken from between the indicated
   * points). From October 2006 the usage of this descriptor is restricted : it
   * is illegal to use "a single base from a range" (c) either on its own or in
   * combination with the "sequence span" (d) descriptor for newly created
   * entries. The existing entries where such descriptors exist are going to be
   * retrofitted.
   * 
   * Sequence spans are indicated by the starting base number and the ending
   * base number separated by two periods (e.g., '34..456'). The '<' and '>'
   * symbols may be used with the starting and ending base numbers to indicate
   * that an end point is beyond the specified base number. The starting and
   * ending base positions can be represented as distinct base numbers
   * ('34..456') or a site between two indicated adjoining bases.
   * 
   * A location in a remote entry (not the entry to which the feature table
   * belongs) can be specified by giving the accession-number and sequence
   * version of the remote entry, followed by a colon ":", followed by a
   * location descriptor which applies to that entry's sequence (i.e.
   * J12345.1:1..15, see also examples below)
   * 
   * 3.5.2.2 Operators
   * 
   * The location operator is a prefix that specifies what must be done to the
   * indicated sequence to find or construct the location corresponding to the
   * feature. A list of operators is given below with their definitions and most
   * common format.
   * 
   * complement(location) Find the complement of the presented sequence in the
   * span specified by " location" (i.e., read the complement of the presented
   * strand in its 5'-to-3' direction)
   * 
   * join(location,location, ... location) The indicated elements should be
   * joined (placed end-to-end) to form one contiguous sequence
   * 
   * order(location,location, ... location) The elements can be found in the
   * specified order (5' to 3' direction), but nothing is implied about the
   * reasonableness about joining them
   * 
   * Note : location operator "complement" can be used in combination with
   * either " join" or "order" within the same location; combinations of "join"
   * and "order" within the same location (nested operators) are illegal.
   * 
   * 
   * 
   * 3.5.3 Location examples
   * 
   * The following is a list of common location descriptors with their meanings:
   * 
   * Location Description
   * 
   * 467 Points to a single base in the presented sequence
   * 
   * 340..565 Points to a continuous range of bases bounded by and including the
   * starting and ending bases
   * 
   * <345..500 Indicates that the exact lower boundary point of a feature is
   * unknown. The location begins at some base previous to the first base
   * specified (which need not be contained in the presented sequence) and
   * continues to and includes the ending base
   * 
   * <1..888 The feature starts before the first sequenced base and continues to
   * and includes base 888
   * 
   * 1..>888 The feature starts at the first sequenced base and continues beyond
   * base 888
   * 
   * 102.110 Indicates that the exact location is unknown but that it is one of
   * the bases between bases 102 and 110, inclusive
   * 
   * 123^124 Points to a site between bases 123 and 124
   * 
   * join(12..78,134..202) Regions 12 to 78 and 134 to 202 should be joined to
   * form one contiguous sequence
   * 
   * 
   * complement(34..126) Start at the base complementary to 126 and finish at
   * the base complementary to base 34 (the feature is on the strand
   * complementary to the presented strand)
   * 
   * 
   * complement(join(2691..4571,4918..5163)) Joins regions 2691 to 4571 and 4918
   * to 5163, then complements the joined segments (the feature is on the strand
   * complementary to the presented strand)
   * 
   * join(complement(4918..5163),complement(2691..4571)) Complements regions
   * 4918 to 5163 and 2691 to 4571, then joins the complemented segments (the
   * feature is on the strand complementary to the presented strand)
   * 
   * J00194.1:100..202 Points to bases 100 to 202, inclusive, in the entry (in
   * this database) with primary accession number 'J00194'
   * 
   * join(1..100,J00194.1:100..202) Joins region 1..100 of the existing entry
   * with the region 100..202 of remote entry J00194
   * 
   */
  /**
   * Recover annotated sequences from EMBL file
   * 
   * @param noNa
   *          don't return nucleic acid sequences
   * @param sourceDb
   *          TODO
   * @param noProtein
   *          don't return any translated protein sequences marked in features
   * @return dataset sequences with DBRefs and features - DNA always comes first
   */
  public jalview.datamodel.SequenceI[] getSequences(boolean noNa,
          boolean noPeptide, String sourceDb)
  {
    Vector seqs = new Vector();
    Sequence dna = null;
    if (!noNa)
    {
      dna = new Sequence(sourceDb + "|" + accession, sequence.getSequence());
      dna.setDescription(desc);
      dna.addDBRef(new DBRefEntry(sourceDb, version, accession));
      // TODO: add mapping for parentAccession attribute
      // TODO: transform EMBL Database refs to canonical form
      if (dbRefs != null)
        for (Iterator i = dbRefs.iterator(); i.hasNext(); dna
                .addDBRef((DBRefEntry) i.next()))
          ;
    }
    try
    {
      for (Iterator i = features.iterator(); i.hasNext();)
      {
        boolean nextFeature=false;
        EmblFeature feature = (EmblFeature) i.next();
        if (!noNa)
        {
          if (feature.dbRefs != null && feature.dbRefs.size() > 0)
          {
            for (Iterator dbr = feature.dbRefs.iterator(); dbr.hasNext(); dna
                    .addDBRef((DBRefEntry) dbr.next()))
              ;
          }
        }
        if (feature.getName().equalsIgnoreCase("CDS"))
        {
          // extract coding region(s)
          jalview.datamodel.Mapping map = null;
          int[] exon = null;
          if (feature.locations != null && feature.locations.size() > 0)
          {
            for (Enumeration locs = feature.locations.elements(); locs
                    .hasMoreElements();)
            {
              EmblFeatureLocations loc = (EmblFeatureLocations) locs
                      .nextElement();
              int[] se = loc.getElementRanges(accession);
              if (exon == null)
              {
                exon = se;
              }
              else
              {
                int[] t = new int[exon.length + se.length];
                System.arraycopy(exon, 0, t, 0, exon.length);
                System.arraycopy(se, 0, t, exon.length, se.length);
                exon = t;
              }
            }
          }
          String prseq = null;
          String prname = new String();
          String prid = null;
          Hashtable vals = new Hashtable();
          int prstart = 1;
          // get qualifiers
          if (feature.getQualifiers() != null
                  && feature.getQualifiers().size() > 0)
          {
            for (Iterator quals = feature.getQualifiers().iterator(); quals
                    .hasNext();)
            {
              Qualifier q = (Qualifier) quals.next();
              if (q.getName().equals("translation"))
              {
                prseq = q.getValues()[0];
              }
              else if (q.getName().equals("protein_id"))
              {
                prid = q.getValues()[0];
              }
              else if (q.getName().equals("codon_start"))
              {
                prstart = Integer.parseInt(q.getValues()[0]);
              }
              else if (q.getName().equals("product"))
              {
                prname = q.getValues()[0];
              }
              else
              {
                // throw anything else into the additional properties hash
                vals.put(q.getName(), q.getValues().toString());
              }
            }
          }
          Sequence product = null;
          if (prseq != null && prname != null && prid != null)
          {
            // extract proteins.
            if (!noPeptide)
            {
              product = new Sequence(sourceDb + "|" + "EMBLCDS|" + prid
                      + "|" + prname, prseq, prstart, prstart
                      + prseq.length() - 1);
              product.setDescription("Protein Product from " + sourceDb);
              seqs.add(product);
            }
            // we have everything - create the mapping and perhaps the protein
            // sequence
            map = new jalview.datamodel.Mapping(product, exon, new int[]
            { prstart, prstart + prseq.length() - 1 }, 3, 1);
            // add cds feature to dna seq - this may include the stop codon
            for (int xint = 0; xint < exon.length; xint += 2)
            {
              SequenceFeature sf = new SequenceFeature();
              sf.setBegin(exon[xint]);
              sf.setEnd(exon[xint + 1]);
              sf.setType(feature.getName());
              sf.setFeatureGroup(jalview.datamodel.DBRefSource.EMBL);
              sf.setDescription("Exon " + (1 + xint) + " for protein '"
                      + prname + "' EMBLCDS:" + prid);
              if (vals != null && vals.size() > 0)
              {
                Enumeration kv = vals.elements();
                while (kv.hasMoreElements())
                {
                  Object key = kv.nextElement();
                  if (key != null)
                    sf.setValue(key.toString(), vals.get(key));
                }
              }
              dna.addSequenceFeature(sf);
            }
          }
          // add dbRefs to sequence
          if (feature.dbRefs != null && feature.dbRefs.size() > 0)
          {
            for (Iterator dbr = feature.dbRefs.iterator(); dbr.hasNext();)
            {
              DBRefEntry ref = (DBRefEntry) dbr.next();
              ref.setSource(jalview.util.DBRefUtils.getCanonicalName(ref
                      .getSource()));
              if (ref.getSource().equals(
                      jalview.datamodel.DBRefSource.UNIPROT))
              {
                ref.setMap(map);
              }
              if (product != null)
              {
                DBRefEntry pref = new DBRefEntry(ref.getSource(), ref
                        .getVersion(), ref.getAccessionId());
                pref.setMap(null); // reference is direct
              }
              dna.addDBRef(ref);
            }
          }

        }
        else
        {
          // General feature type.
          if (!noNa)
          {
            if (feature.dbRefs != null && feature.dbRefs.size() > 0)
            {
              for (Iterator dbr = feature.dbRefs.iterator(); dbr.hasNext(); dna
                      .addDBRef((DBRefEntry) dbr.next()))
                ;
            }
          }
        }
      }
    } catch (Exception e)
    {
      System.err.println("EMBL Record Features parsing error!");
      System.err.println("Please report the following to help@jalview.org :");
      System.err.println("EMBL Record "+accession);
      System.err.println("Resulted in exception: "+e.getMessage());
      e.printStackTrace(System.err);
    }
    if (!noNa && dna!=null)
    {
      seqs.add(dna);
    }
    SequenceI[] sqs = new SequenceI[seqs.size()];
    for (int i = 0, j = seqs.size(); i < j; i++)
    {
      sqs[i] = (SequenceI) seqs.elementAt(i);
      seqs.set(i, null);
    }
    return sqs;
  }
}
