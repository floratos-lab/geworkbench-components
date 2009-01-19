package jalview.analysis;

import java.util.Hashtable;
import java.util.Vector;

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.schemes.ResidueProperties;
import jalview.util.MapList;

public class Dna {
  /**
   * 
   * @param cdp1
   * @param cdp2
   * @return -1 if cdp1 aligns before cdp2, 0 if in the same column or cdp2 is null, +1 if after cdp2
   */
  private static int compare_codonpos(int[] cdp1, int[] cdp2) {
    if (cdp2==null || (cdp1[0]==cdp2[0] && cdp1[1] == cdp2[1] && cdp1[2] == cdp2[2]))
      return 0;
    if (cdp1[0]<cdp2[0] || cdp1[1]<cdp2[1] || cdp1[2]<cdp2[2])
      return -1; // one base in cdp1 precedes the corresponding base in the other codon
    return 1; // one base in cdp1 appears after the corresponding base in the other codon.
  }
  /**
   * create a new alignment of protein sequences
   * by an inframe translation of the provided NA sequences
   * @param selection
   * @param seqstring
   * @param viscontigs
   * @param gapCharacter
   * @param annotations
   * @param aWidth
   * @return
   */
  public static AlignmentI CdnaTranslate(SequenceI[] selection, String[] seqstring, int viscontigs[], char gapCharacter, 
      AlignmentAnnotation[] annotations, int aWidth) {
    int s, sSize = selection.length;
    SequenceI [] newSeq = new SequenceI[sSize];
    int res, resSize;
    StringBuffer protein;
    String seq;

    int[][] codons = new int[aWidth][]; // stores hash of subsequent positions for each codon start position in alignment

    for (res=0;res<aWidth;res++)
      codons[res]=null;
    int aslen=0; // final width of aligned translated aa sequences
    for(s=0; s<sSize; s++)
    {
      int vc,scontigs[]=new int[viscontigs.length];

      for (vc=0;vc<scontigs.length; vc+=2)
      {
        scontigs[vc]=selection[s].findPosition(viscontigs[vc]); // not from 1!
        scontigs[vc+1]=selection[s].findPosition(viscontigs[vc+1]-1); // exclusive
        if (scontigs[vc+1]==selection[s].getEnd())
          break;
      }
      if ((vc+2)<scontigs.length) {
        int t[] = new int[vc+2];
        System.arraycopy(scontigs, 0, t, 0, vc+2);
        scontigs = t;
      }
      protein = new StringBuffer();
      seq = seqstring[s].replace('U', 'T');
      char codon[]=new char[3];
      int cdp[]=new int[3],rf=0,gf=0,nend,npos;
      int aspos=0;
      resSize=0;
      for (npos=0,nend=seq.length(); npos<nend; npos++) {
        if (!jalview.util.Comparison.isGap(seq.charAt(npos))) { 
          cdp[rf] = npos; // store position
          codon[rf++]=seq.charAt(npos); // store base
        }
        // filled an RF yet ?
        if (rf==3) {
          String aa = ResidueProperties.codonTranslate(new String(codon));
          rf=0;
          if(aa==null)
            aa=String.valueOf(gapCharacter);
          else {
            if(aa.equals("STOP"))
            {
              aa="X";
            }
            resSize++;
          }
          // insert/delete gaps prior to this codon - if necessary
          boolean findpos=true;
          while (findpos) 
          {
            // first ensure that the codons array is long enough.
            if (codons.length<=aslen+1) {
              // probably never have to do this ?
              int[][] c = new int[codons.length+10][];
              for (int i=0; i<codons.length; i++) {
                c[i] = codons[i];
                codons[i]=null;
              }
              codons = c;
            }
            // now check to see if we place the aa at the current aspos in the protein alignment
            switch (Dna.compare_codonpos(cdp, codons[aspos])) 
            {
            case -1:
              // this aa appears before the aligned codons at aspos - so shift them.
              aslen++;
              for (int sq=0;sq<s; sq++) {
                newSeq[sq].insertCharAt(aspos, gapCharacter);
              }
              System.arraycopy(codons, aspos, codons, aspos+1, aslen-aspos);
              findpos=false;
              break;
            case +1:
              // this aa appears after the aligned codons at aspos, so prefix it with a gap
              aa = ""+gapCharacter+aa;
              aspos++;
              if (aspos>=aslen)
                aslen=aspos+1;
              break; // check the next position for alignment
            case 0:
              // codon aligns at aspos position.
              findpos = false;
            }
          }
          // codon aligns with all other sequence residues found at aspos
          protein.append(aa);
          if (codons[aspos]==null) 
          {
            // mark this column as aligning to this aligned reading frame 
            codons[aspos] = new int[] { cdp[0], cdp[1], cdp[2] };
          }
          aspos++;
          if (aspos>=aslen)
            aslen=aspos+1;
        }
      }
      if (resSize>0) 
      {
        newSeq[s] = new Sequence(selection[s].getName(),
            protein.toString());
        if (rf!=0) 
        {
          jalview.bin.Cache.log.debug("trimming contigs for incomplete terminal codon.");
          // trim contigs
          vc=scontigs.length-1;
          nend-=rf;
          // incomplete ORF could be broken over one or two visible contig intervals.
          while (vc>0 && scontigs[vc]>nend)
          {
            if (scontigs[vc-1]>nend) 
            {
              vc-=2;
            } else {
              // correct last interval in list.
              scontigs[vc]=nend;
            }
          }
          if ((vc+2)<scontigs.length) {
            // truncate map list
            int t[] = new int[vc+1];
            System.arraycopy(scontigs,0,t,0,vc+1);
            scontigs=t;
          }
        }
        MapList map = new MapList(scontigs, new int[] { 1, resSize },3,1); // TODO: store mapping on newSeq for linked DNA/Protein viewing.
      }
      // register the mapping somehow
      // 
    }
    if (aslen==0)
      return null;
    AlignmentI al = new Alignment(newSeq);
    al.padGaps();  // ensure we look aligned.
    al.setDataset(null);


    ////////////////////////////////
    // Copy annotations across
    //
    // Can only do this for columns with consecutive codons, or where
    // annotation is sequence associated.
    
    int pos,a,aSize;
    if(annotations!=null)
    {
      for (int i = 0; i < annotations.length; i++)
      {
        // Skip any autogenerated annotation
        if (annotations[i].autoCalculated) {
          continue;
        }
        
        aSize = aslen; // aa alignment width.
        jalview.datamodel.Annotation[] anots = 
          (annotations[i].annotations==null) 
          ? null :
            new jalview.datamodel.Annotation[aSize];
        if (anots!=null)
        {
          for (a = 0; a < aSize; a++)
          {
            // process through codon map.
            if (codons[a]!=null && codons[a][0]==(codons[a][2]-2))
            {
              pos = codons[a][0];
              if (annotations[i].annotations[pos] == null
                      || annotations[i].annotations[pos] == null)
                continue;
            
              anots[a] = new Annotation(annotations[i].annotations[pos]);
            }
          }
        }

        jalview.datamodel.AlignmentAnnotation aa
        = new jalview.datamodel.AlignmentAnnotation(annotations[i].label,
            annotations[i].description, anots);
        if (annotations[i].hasScore)
        {
          aa.setScore(annotations[i].getScore());
        }
        al.addAnnotation(aa);
      }
    }
    return al;
  }
}
