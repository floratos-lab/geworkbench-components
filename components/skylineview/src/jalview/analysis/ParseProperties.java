package jalview.analysis;

import com.stevesoft.pat.Regex;

import jalview.datamodel.*;

public class ParseProperties
{
  /**
   * Methods for parsing free text properties on alignments and sequences.
   * There are a number of ways we might want to do this:
   * arbitrary regex. and an associated score name for the number that's extracted.
   * Regex that provides both score and name.
   * 
   * We may also want to :
   * - modify description to remove parsed numbers (this behaviour is dangerous since exporting the alignment would lose the original form then)
   * - 
   * 
   */
  /**
   * The alignment being operated on
   */
  private AlignmentI al=null;
  
  /**
   * initialise a new property parser
   * @param al
   */
  public ParseProperties(AlignmentI al) {
    this.al = al;
  }

  public int getScoresFromDescription(String ScoreName, String ScoreDescriptions, String regex)
  {
    return getScoresFromDescription(new String[] { ScoreName }, new String[] { ScoreDescriptions}, regex);
  }

  public int getScoresFromDescription(String[] ScoreNames, String[] ScoreDescriptions, String regex) 
  {
    return getScoresFromDescription(al.getSequencesArray(), ScoreNames, ScoreDescriptions, regex);
  }
  /**
   * Extract scores for sequences by applying regex to description string.
   * @param seqs seuqences to extract annotation from.
   * @param ScoreNames labels for each numeric field in regex match
   * @param ScoreDescriptions description for each numeric field in regex match
   * @param regex Regular Expression string for passing to <code>new com.stevesoft.patt.Regex(regex)</code>
   * @return total number of sequences that matched the regex
   */
  public int getScoresFromDescription(SequenceI[] seqs, String[] ScoreNames, String[] ScoreDescriptions, String regex) 
  {
    int count=0;
    Regex pattern = new Regex(regex);
    if (pattern.numSubs()>ScoreNames.length)
    {
      // Check that we have enough labels and descriptions for any parsed scores.
      int onamelen = ScoreNames.length;
      String[] tnames = new String[pattern.numSubs()+1];
      System.arraycopy(ScoreNames, 0, tnames, 0, ScoreNames.length);
      String base = tnames[ScoreNames.length-1];
      ScoreNames = tnames;
      String descrbase = ScoreDescriptions[onamelen-1];
      if (descrbase == null)
        descrbase = "Score parsed from ("+regex+")";
      tnames = new String[pattern.numSubs()+1];
      System.arraycopy(ScoreDescriptions, 0, tnames, 0, ScoreDescriptions.length);
      ScoreDescriptions = tnames;
      for (int i=onamelen; i<ScoreNames.length; i++)
      {
        ScoreNames[i] = base+"_"+i;
        ScoreDescriptions[i] = descrbase+" (column "+i+")";
      }
    }
    for (int i=0; i<seqs.length; i++)
    {
      String descr = seqs[i].getDescription();
      if (descr==null)
        continue;
      if (pattern.search(descr))
      {
        boolean added=false;
        for (int cols=0; cols<pattern.numSubs(); cols++)
        {
          String sstring = pattern.stringMatched(cols+1);
          double score=Double.NaN;
          try {
            score = new Double(sstring).doubleValue();
          }
          catch (Exception e)
          {
            // don't try very hard to parse if regex was wrong.
            continue;
          }
          // add score to sequence annotation.
          AlignmentAnnotation an = new AlignmentAnnotation(ScoreNames[cols], ScoreDescriptions[cols], null);
          an.setScore(score);
          System.out.println("Score: "+ScoreNames[cols]+"="+score); // DEBUG
          an.setSequenceRef(seqs[i]);
          seqs[i].addAlignmentAnnotation(an);
          al.addAnnotation(an);
          added=true;
        }
        if (added)
          count++;
      }
      
    }
    return count; 
  }
  public static void main(String argv[]) {
    SequenceI[] seqs = new SequenceI[] { new Sequence("sq1","THISISAPLACEHOLDER"),
            new Sequence("sq2","THISISAPLACEHOLDER"),
            new Sequence("sq3","THISISAPLACEHOLDER"),
            new Sequence("sq4","THISISAPLACEHOLDER")};
    seqs[0].setDescription("1 mydescription1");
    seqs[1].setDescription("mydescription2");
    seqs[2].setDescription("2. 0.1 mydescription3");
    seqs[3].setDescription("3 0.01 mydescription4");
    //seqs[4].setDescription("5 mydescription5");
    Alignment al = new Alignment(seqs);
    ParseProperties pp = new ParseProperties(al);
    String regex = ".*([-0-9.+]+)";
    System.out.println("Matched "+pp.getScoresFromDescription("my Score", "my Score Description",regex)+" for "+regex);
    regex = ".*([-0-9.+]+).+([-0-9.+]+).*";
    System.out.println("Matched "+pp.getScoresFromDescription("my Score", "my Score Description",regex)+" for "+regex);
    System.out.println("Finished.");
  }
}
