/*
 Written by (C) Kai Wang (kw2110@columbia.edu) Columbia University
 modified by Cai Duo and Xuegong Wang
 */

package org.geworkbench.components.promoter.modulediscovery;

import org.geworkbench.bison.datastructure.biocollections.DSCollection;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.CSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.DSMatchedPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPattern;
import org.geworkbench.bison.datastructure.complex.pattern.DSPatternMatch;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqCmplxRegistration;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.CSSeqRegistration;

import java.io.*;
import java.util.*;

/*============================================================================*/
/* Main Discovery program                                                     */
/*============================================================================*/

public class Discovery {
    static Vector finalPattern = new Vector();
    static Vector finalPattern1 = new Vector();

    public static Vector getFinalPattern() {
        return finalPattern;
    }

    public static Vector getRandomPattern() {
        return finalPattern1;
    }

    public static void discovery(SequenceFileReader sfr, int minOccur,
                                 int winSize) throws IOException {

        //        String outfile = "finalResult";
        Hashtable motifTB = new Hashtable(); // record all motifs for maximality check
        Hashtable patternTB = new Hashtable();

        //        PrintStream out = null, std_out = System.out;

        String infile = sfr.getDiscoveryInputFile();
        /** @todo to be fixed
         discover(minOccur, winSize, motifTB, patternTB, infile); */

        /**run again with random input file which is generate from the origin one**/
        Hashtable motifTB1 = new Hashtable(); // record all motifs for maximality check
        Hashtable patternTB1 = new Hashtable();

        sfr.randomizeMotifVector();
        sfr.randomGeneratePosition();
        String randomFile = sfr.randomizeInputFile();
        /** @todo to be fixed
         discover(minOccur, winSize, motifTB1, patternTB1, randomFile); */
        //        returnedFile = new File(outfile);
        //        return returnedFile;

        /**************************************************************************/

    }

    private static void discover(DSSequenceSet seqDB, int minOccur, int winSize,
                                 Hashtable motifTB, Hashtable patternTB,
                                 String infile) throws FileNotFoundException,
            IOException {
        //        System.out.println("numberOfSeq = " + sfr.getNumberOfSeq());
        //        returnedFile[0] = new File(sfr.getMotifPrimeFile());

        if (minOccur <= 0) {
            System.err.println(
                    "Minimal suport must be specified and can not be negative!\n");
            //            System.exit(0);
        }
        if (winSize <= 0) {
            System.err.println(
                    "Window length must be specified and can not be negative!\n");
            //            System.exit(0);
        }

        if (!infile.equals("")) {
            load(seqDB, infile, motifTB, patternTB, winSize, minOccur);
        } else {
            System.out.println("No file name specified, returning.\n");
            //            System.exit(0);
        }

        // get the initial time
        //        long start_time = new Date().getTime();
        findPattern(motifTB, patternTB, winSize, minOccur);

    }

    public static void discover(DSSequenceSet sequenceDB,
                                Hashtable<DSPattern<DSSequence, CSSeqRegistration>, List<DSPatternMatch<DSSequence, CSSeqRegistration>>> patternMatch, int minOccur,
                                int winSize, Hashtable patternPrime) {

        //        System.out.println("numberOfSeq = " + sfr.getNumberOfSeq());
        //        returnedFile[0] = new File(sfr.getMotifPrimeFile());
        Hashtable motifTB = new Hashtable(); // record all motifs for maximality check
        Hashtable patternTB = new Hashtable();
        if (minOccur <= 0) {
            System.err.println(
                    "Minimal suport must be specified and can not be negative!\n");
            return;
        }
        if (winSize <= 0) {
            System.err.println(
                    "Window length must be specified and can not be negative!\n");
            return;
        }

        load(sequenceDB, patternPrime, patternMatch, motifTB, patternTB,
             winSize, minOccur);

        // get the initial time
        //        long start_time = new Date().getTime();
        findPattern(motifTB, patternTB, winSize, minOccur);

    }

    /*============================================================================*/
    /* The main pattern discovery algorithm                                       */
    /*============================================================================*/
    private static void findPattern(Hashtable motifTB, Hashtable patternTB,
                                    int winSize, int minOccur) {

        //        System.out.println("Finding patterns ... ");
        // generating an ascending list of motifs in the dataset
        Object[] motifSet = motifTB.keySet().toArray();
        Arrays.sort(motifSet);

        int itr = 0;
        while (!patternTB.isEmpty()) {
            itr++;
            Object[] patternSet = patternTB.keySet().toArray();
            Arrays.sort(patternSet);

            Hashtable patternTB2 = new Hashtable();
            for (int i = 0; i < patternSet.length; i++) {
                CSMultiSeqPattern p = (CSMultiSeqPattern) patternTB.get(
                        patternSet[i]);

                // check whether a pattern can be combined with itself
                if (p.isSelfCombinable(winSize)) {
                    // only need to consider conbiming with patterns downstairs
                    PatternKey forward_pattern = p.patternKey.findForward();
                    if (patternTB.containsKey(forward_pattern)) {
                        // OK even if the forward_pattern is just p itself
                        CSMultiSeqPattern new_pattern = p.merge((
                                CSMultiSeqPattern) patternTB.get(
                                forward_pattern), winSize, minOccur);
                        // only add pattern that exist and has enough support
                        if (new_pattern != null) {
                            patternTB2.put(new_pattern.patternKey, new_pattern);
                        }
                    }
                }

                // combining with patterns downstairs that share upto the last motif
                int j = i + 1;
                while (j < patternSet.length &&
                       ((PatternKey) patternSet[i]).
                       isCombinable((PatternKey) patternSet[j])) {
                    CSMultiSeqPattern new_pattern = p.merge((CSMultiSeqPattern)
                            patternTB.get(patternSet[j]), winSize, minOccur);

                    // only add pattern that exist and has enough support
                    if (new_pattern != null) {
                        patternTB2.put(new_pattern.patternKey, new_pattern);
                    }
                    j++;
                }
            }

            Object[] patternSet2 = patternTB2.keySet().toArray();
            Arrays.sort(patternSet2);

            // check the maximality of those patterns in patternTB that are currently marked as maximal
            // Add the maximal patterns to the final set
            for (int i = 0; i < patternSet.length; i++) {
                CSMultiSeqPattern the_pattern = (CSMultiSeqPattern) patternTB.
                                                get(patternSet[i]);
                if (the_pattern.isMaximal) {
                    if (checkMaximality(the_pattern, motifSet, patternTB2)) {

                        finalPattern.add(the_pattern);
                    }
                }
            }

            patternTB.clear();
            patternTB = patternTB2;
            patternTB2 = null;
        }
    }

    /*============================================================================*/
    /* Input file reader                                                          */
    /*============================================================================*/
    private static void load(DSSequenceSet seqDB, String infile, Hashtable motifTB,
                             Hashtable<DSPattern<DSSequence, CSSeqRegistration>, DSCollection<DSMatchedPattern<DSSequence, CSSeqRegistration>>> patternTB, int winSize,
                             int minOccur) throws FileNotFoundException,
            IOException {

        //        System.out.println("Reading data from " + infile + " ...\n");
        File f = new File(infile);
        if (!f.exists()) {
            System.err.println("ERROR: Cannot find file: " + infile + "\n");
            //            System.exit(0);
        }

        BufferedReader in = new BufferedReader(new FileReader(infile));
        String line;
        int sid = 0;
        while ((line = in.readLine()) != null) {
            if (line.trim().length() == 0) {
            } else if (line.startsWith(">")) {
            } else {
                sid++;
                String[] motifs = line.split(" ");

                // a Queue to keep track the elements read ahead
                LinkedList buffer = new LinkedList();
                int buffered_position = 0;

                //    for (int i=0; i<motifs.length-1; i++) {
                for (int i = 0; i < motifs.length; i++) {
                    if (buffer.isEmpty()) {
                        int[] m1 = parseMotif(sid, motifs[i]);
                        updateMotifTable(m1, motifTB);
                        buffered_position = i + 1;
                        while (buffered_position < motifs.length) {
                            int[] m2 = parseMotif(sid, motifs[buffered_position]);
                            if (m2[2] - m1[2] < winSize && m2[2] - m1[2] > 6) { // m2[2] - m1[2] > 6 nonoverlapping
                                updateMotifTable(m2, motifTB);
                                updatePatternTable(seqDB, m1, m2, patternTB);
                                buffer.add(m2);
                                buffered_position++;
                            } else {
                                break;
                            }
                        }
                    } else {
                        int[] m1 = (int[]) buffer.removeFirst();
                        ListIterator itr = buffer.listIterator(0);
                        while (itr.hasNext()) {
                            updatePatternTable(seqDB, m1, (int[]) itr.next(),
                                               patternTB);
                        } while (buffered_position < motifs.length) {
                            int[] m2 = parseMotif(sid, motifs[buffered_position]);
                            if (m2[2] - m1[2] < winSize && m2[2] - m1[2] > 6) {
                                updateMotifTable(m2, motifTB);
                                updatePatternTable(seqDB, m1, m2, patternTB);
                                buffer.add(m2);
                                buffered_position++;
                            } else {
                                break;
                            }
                        }
                    }
                }
                motifs = null;
                buffer = null;
            }
        }
        in = null;

        // remove those patterns of pairs that don't have enough support
        for (Enumeration e = patternTB.keys(); e.hasMoreElements(); ) {
            PatternKey key = (PatternKey) e.nextElement();

            if (patternTB.get(key).size() < minOccur) {
                patternTB.remove(key);
            }
        }
    }

    private static void load(DSSequenceSet sequenceDB, Hashtable keyPrime,
                             Hashtable<DSPattern<DSSequence, CSSeqRegistration>, List<DSPatternMatch<DSSequence, CSSeqRegistration>>> patternMatch,
                             Hashtable motifTB, Hashtable patternTB,
                             int winSize, int minOccur) {
        for (int k = 0; k < sequenceDB.getSequenceNo(); k++) {
            Vector sortedMatches = new Vector();
            Hashtable<DSPatternMatch, DSPattern<DSSequence,
                    CSSeqRegistration>>
                    lookup = new Hashtable<DSPatternMatch, DSPattern<DSSequence,
                             CSSeqRegistration>>();
            for (DSPattern pattern : patternMatch.keySet()) {
                List<DSPatternMatch<DSSequence,
                        CSSeqRegistration>> matches = patternMatch.get(pattern);
                for (int i = 0; i < matches.size(); i++) {
                    DSPatternMatch<DSSequence,
                            CSSeqRegistration> match = matches.get(i);
                    DSSequence sequence = match.getObject();
                    if (sequence.getSerial() == k) {
                        sortedMatches.add(match);
                        lookup.put(match, pattern);
                        Integer key = (Integer) keyPrime.get(pattern);
                        int[] m = {k, key.intValue(),
                                  match.getRegistration().x1};
                        // m[0] - sid, m[1] - mid, m[2] - position
                        updateMotifTable(m, motifTB);
                    }

                }
            }
            Object[] sortedMatchArray = sortedMatches.toArray();
            Arrays.sort(sortedMatchArray, new PatternMatchComparator());
            for (int i = 0; i < sortedMatchArray.length; i++) {
//                DSSeqPatternMatch m1 = (DSSeqPatternMatch) sortedMatchArray[i];
//                CSSeqRegistration reg = m1.getRegistration();
                DSPatternMatch m1 = (DSPatternMatch) sortedMatchArray[i];
CSSeqRegistration reg = (CSSeqRegistration)m1.getRegistration();

                int n = 1;
                while (n + i < sortedMatchArray.length) {
                    int k2 = i + n++;
//                    DSSeqPatternMatch m2 = (DSSeqPatternMatch) sortedMatchArray[
//                                           k2];
//                    CSSeqRegistration reg2 = m2.getRegistration();

                    DSPatternMatch m2 = (DSPatternMatch) sortedMatchArray[
                                            k2];
                   CSSeqRegistration reg2 = (CSSeqRegistration)m2.getRegistration();

                    if ((reg2.x1 - reg.x1) < winSize) {
                        if (reg.x2 <= reg2.x1) { //no overlap
                            DSPattern p1 = lookup.get(m1);
                            DSPattern p2 = lookup.get(m2);
                            Integer key = (Integer) keyPrime.get(p1);
                            if (key == null) {
                                System.out.println("No key found for pattern:" +
                                        p1);
                            }
                            int[] ml = {k, key.intValue(), reg.x1};
                            Integer key2 = (Integer) keyPrime.get(p2);
                            int[] mt = {k, key2.intValue(), reg2.x1};
                            updatePatternTable(sequenceDB, ml, mt, patternTB);

                        }
                    } else {
                        break;
                    }

                }

            }
        }

        // remove those patterns of pairs that don't have enough support
        for (Enumeration e = patternTB.keys(); e.hasMoreElements(); ) {
            PatternKey key = (PatternKey) e.nextElement();
            if (((CSMultiSeqPattern) patternTB.get(key)).matches().size() <
                minOccur) {
                patternTB.remove(key);
            }
        }
    }

    /*============================================================================*/
    /*                                                           */
    /*============================================================================*/
    private static int[] parseMotif(int sid, String motif) {
        int sep = (int) motif.indexOf(",");
        int mid = Integer.parseInt(motif.substring(1, sep));
        int pos = Integer.parseInt(motif.substring(sep + 1, motif.length() - 1));
        int[] m = {sid, mid, pos};
        return m;
    }

    /*============================================================================*/
    /*                                                          */
    /*============================================================================*/
    private static void updateMotifTable(int[] m, Hashtable motifTB) {
        // m[0] - sid, m[1] - mid, m[2] - position
        Integer key = new Integer(m[1]);
        //what does these following code do?
        //        if (m[0] == 1) {
        //            System.out.println("Have sequence 1");
        //        }
        //        if (m[0] == 4193) {
        //            System.out.println(m[0]);
        //        }
        int[] position = {m[0], m[2]};
        if (motifTB.containsKey(key)) {
            ((ArrayList) (motifTB.get(key))).add(position);
            //  key = null; // delete the temp key
        } else {
            ArrayList positions = new ArrayList();
            positions.add(position);
            motifTB.put(key, positions);
        }
    }

    /*============================================================================*/
    /*                                                           */
    /*============================================================================*/
    private static void updatePatternTable(DSSequenceSet<? extends DSSequence> seqDB, int[] m1, int[] m2,
                                           Hashtable patternTB) {
        // m[0] - sid, m[1] - mid, m[2] - position
        int[] comb = {m1[1], m2[1]};
        PatternKey pattern_key = new PatternKey(comb);
        int[] support = {m1[0], m1[2], m2[2]};
        CSPatternMatch<DSSequence,
                CSSeqRegistration>
                match = new CSPatternMatch<DSSequence,
                        CSSeqRegistration>(seqDB.get(m1[0]));
        CSSeqCmplxRegistration reg = new CSSeqCmplxRegistration();
        reg.offsets.add(m1[2]);
        reg.offsets.add(m2[2]);
        match.setRegistration(reg);
        CSMultiSeqPattern pattern = null;
        if (patternTB.containsKey(pattern_key)) {
            pattern = ((CSMultiSeqPattern) patternTB.get(pattern_key));
        } else {
            pattern = new CSMultiSeqPattern(pattern_key);
            patternTB.put(pattern_key, pattern);
        }
        pattern.matches().add(match);
    }

    /*============================================================================*/
    /*                                                           */
    /*============================================================================*/
    private static boolean checkMaximality(CSMultiSeqPattern p,
                                           Object[] motifSet,
                                           Hashtable patternTB) {
        // only check sub-patterns that are from upstairs
        boolean result = true;
        int mm = p.patternKey.maxMotif();
        int i = 0;
        while (((Integer) motifSet[i]).intValue() < mm) {
            PatternKey sub_pattern = p.patternKey.addOne((Integer) motifSet[i]);
            i++;
            if (patternTB.containsKey(sub_pattern) &&
                ((CSMultiSeqPattern) patternTB.get(sub_pattern)).matches().size() >=
                p.matches().size()) {
                p.isMaximal = false;
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * clean the pattern.
     */
    public static void clear() {
        finalPattern.clear();
    }
}
