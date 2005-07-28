package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.util.sequences.SequenceAnnotationTrack;

import java.io.*;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class PFPParser {
    /**
     * Features Object
     */
    private static SequenceAnnotationTrack fObj = null;
    private static SequenceAnnotation SeqAObj = null;
    private static String startTag = null;
    private static String endTag = null;

    /**
     * The name of file with results.
     */
    private static String filename;

    /**
     * Creates a new PFP Parser.
     *
     * @param aString String
     */

    public static void runPFPParser(SequenceAnnotation sa, String fl, int which) {
        SeqAObj = sa;
        filename = fl;

        if (which == 1) {
            startTag = new String("/# Start of PFP output 1 #/");
            endTag = new String("/# End of PFP output 1 #/");
        }

        if (which == 2) {
            startTag = new String("/# Start of PFP output 2 #/");
            endTag = new String("/# End of PFP output 2 #/");
        }
        parseResults(filename);

        fObj.setAnnotationName("PFP");
        SeqAObj.addAdjustZeroAnnoTrack(fObj);
    }

    /**
     * Creates a new PFP Parser.
     *
     * @param aString String
     */

    private static boolean parsePFPstring(String line, int curf) {
        int i, j, k;

        if (line == null) return false;
        if (line.length() < 10) return false;
        i = line.indexOf(" ->");
        if (i == -1) return false;
        for (j = i - 1; j > 0; j--) {
            if (line.charAt(j) == ' ') break;
        }
        String subLine = line.substring(j + 1, i);
        fObj.setSequenceHitStart(curf, Integer.parseInt(subLine));
        for (j = i + 4; j < 1000; j++) {
            if (line.charAt(j) == ' ') break;
        }
        subLine = line.substring(i + 4, j);
        fObj.setSequenceHitEnd(curf, Integer.parseInt(subLine));
        for (; j < 1000; j++) {
            if (line.charAt(j) != ' ') break;
        }
        for (i = j; i < 1000; i++) {
            if (line.charAt(i) == ' ') break;
        }
        subLine = line.substring(j, i);
        fObj.setFeatureName(curf, subLine);
        fObj.setFeatureTag(curf, subLine);
        if (line.charAt(i - 1) == '+') {
            fObj.setFeatureDirection(curf, true);
        }
        if (line.charAt(i - 1) == '-') {
            fObj.setFeatureDirection(curf, false);
        }
        return true;
    }

    /**
     * Reads in Dots results from file and parses data into DotMatrixObj objects.
     */
    public static void parseResults(String filename) {

        String line;
        String infile1, infile2;
        int fn = 0, i, j, curf;

        /* First - count the number of features */
        try {
            File file = new File(filename);

            if (file.length() < 500) {
                System.out.println("Wrong result file size. try again.");
            } else {
                //new BufferedReader for parsing file line by line
                BufferedReader br = new BufferedReader(new FileReader(filename));

                /* Walk to the begining of PFP output */
                while (true) {
                    if ((line = br.readLine()) == null) {
                        break;
                    }
                    if ((i = line.indexOf(startTag)) != -1) {
                        break;
                    }
                }

                /* skipping unwanted */
                for (i = 0; i < 7; i++) {
                    line = br.readLine();
                }

                //loop to proceed the header
                while (true) {
                    line = br.readLine();
                    if (line.length() < 10) {
                        break;
                    } else {
                        fn++;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            //System.exit(1);
        } catch (IOException e) {
            System.out.println("IOException!");
        }

        /* Real parsing */
        fObj = new SequenceAnnotationTrack(fn);
        System.out.println("reading of " + fn + " from " + filename);

        try {
            File file = new File(filename);
            //server failure
            if (file.length() < 500) {
                System.out.println("Wrong result file size. try again.");
            } else {
                //new BufferedReader for parsing file line by line
                BufferedReader br = new BufferedReader(new FileReader(filename));

                /* Walk to the begining of PFP output */
                while (true) {
                    if ((line = br.readLine()) == null) {
                        break;
                    }
                    if ((i = line.indexOf(startTag)) != -1) {
                        break;
                    }
                }
                /*        --------------------------------------------------------------------------------
                         [08/17 17:12:07] Stage 0: DUST
                         [08/17 17:12:07] Alg:DUST  Threshold:22  Action:mask
                 --------------------------------------------------------------------------------
                                QName   QStart    QEnd                      DName   DStart    DEnd         Sim Score
                         chr2" start="9775000+     1152 -> 1212                    T-rich+        0 -> 60         0.00%    23
                 --------------------------------------------------------------------------------
                         [08/17 17:12:07] Stage 1: REPEATS_HASTE
                         [08/17 17:12:07] Reference:/paracel/pfp/reference/human.repeats  Alg:HASTE  Threshold:187  Action:mask
                 --------------------------------------------------------------------------------
                                QName   QStart    QEnd                      DName   DStart    DEnd         Sim Score
                         chr2" start="9775000+     1591 -> 1942                     MER4B+      192 -> 538       78.98%  1933
                         chr2" start="9775000+     2786 -> 3063                       ALU-        8 -> 288       84.34%  1702
                 --------------------------------------------------------------------------------
                         [08/17 17:12:07] Stage 2: REPEATS_GM
                         [08/17 17:12:07] Reference:/paracel/pfp/reference/human.repeats  Alg:HASTE  Threshold:220  Action:mask
                 --------------------------------------------------------------------------------
                                QName   QStart    QEnd                      DName   DStart    DEnd         Sim Score
                 */
                int lc;
                for (lc = 0; lc < 3; lc++) { /* Three stages above */
                    /* skipping unwanted */
                    while (true) {
                        line = br.readLine();
                        if (line == null) break;
                        if (line.indexOf("QName   QStart    QEnd") != -1) break;
                    }
                    //loop to proceed the header
                    curf = 0;
                    while (true) {
                        line = br.readLine();
                        if (parsePFPstring(line, curf) == false) break;
                        curf++;
                    }
                }

            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            //System.exit(1);
        } catch (IOException e) {
            System.out.println("IOException!");
        }
    }

}
