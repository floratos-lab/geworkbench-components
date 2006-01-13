package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.util.sequences.SequenceAnnotationTrack;

import java.io.*;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
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

    private static boolean parsePFPstring(String line) {
        int i, j, k;
        int SHitStart;
        int SHitEnd;
        String FeatureName;
        boolean FeatureDirection = true;

        if (line == null) {
            return false;
        }
        if (line.length() < 10) {
            return false;
        }
        i = line.indexOf(" ->");
        if (i == -1) {
            return false;
        }
        for (j = i - 1; j > 0; j--) {
            if (line.charAt(j) == ' ') {
                break;
            }
        }
        String subLine = line.substring(j + 1, i);
        SHitStart = Integer.parseInt(subLine);
        for (j = i + 4; j < 1000; j++) {
            if (line.charAt(j) == ' ') {
                break;
            }
        }
        subLine = line.substring(i + 4, j);
        SHitEnd = Integer.parseInt(subLine);
        for (; j < 1000; j++) {
            if (line.charAt(j) != ' ') {
                break;
            }
        }
        for (i = j; i < 1000; i++) {
            if (line.charAt(i) == ' ') {
                break;
            }
        }
        subLine = line.substring(j, i);
        FeatureName = new String(subLine);
        if (line.charAt(i - 1) == '+') {
            FeatureDirection = true;
        }
        if (line.charAt(i - 1) == '-') {
            FeatureDirection = false;
        }

        fObj.addFeature(SHitStart, SHitEnd, SHitStart, SHitEnd,
                        SHitEnd - SHitStart + 1, FeatureDirection, true, 0,
                        FeatureName, FeatureName, null);
        return true;
    }

    /**
     * Reads in Dots results from file and parses data into DotMatrixObj objects.
     */
    public static void parseResults(String filename) {

        String line;
        fObj = new SequenceAnnotationTrack();

        try {
            File file = new File(filename);
            //server failure
            if (file.length() < 500) {
                System.out.println("Wrong result file size. try again.");
            }
            else {
                //new BufferedReader for parsing file line by line
                BufferedReader br =
                    new BufferedReader(new FileReader(filename));

                /* Walk to the begining of PFP output */
                while (true) {
                    if ( (line = br.readLine()) == null) {
                        break;
                    }
                    if ( line.indexOf(startTag) != -1) {
                        break;
                    }
                }

                int lc;
                for (lc = 0; lc < 20; lc++) {
                    String localTarget=new String("Stage "+lc+":");

                    /* skipping unwanted */
                    while (true) {
                        line = br.readLine();
                        if((line.indexOf("# End PFP output")) != -1){
                            br.close();
                            return;
                        }
                        if (line == null) {
                            break;
                        }
                        if (line.indexOf(localTarget) != -1) {
                            break;
                        }
                    }

                    //loop to proceed the header
                    while (true) {
                        line = br.readLine();
                        if ( (line.indexOf("QName   QStart    QEnd")) != -1) {
                            break;
                        }
                    }
                    // The actual parsing
                    while (true) {
                        line = br.readLine();
                        if (parsePFPstring(line) == false) {
                            break;
                        }
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            //System.exit(1);
        }
        catch (IOException e) {
            System.out.println("IOException!");
        }
    }
}
