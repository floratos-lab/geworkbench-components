package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.SequenceAnnotation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: pavel
 * Date: Jun 12, 2006
 * Time: 2:30:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasicParser {

    String[] tokens = {"JOB_ID: ", "REQUEST_TYPE: ", "MARKER: ", "PROGRAM: ", "GENOME1: ", "CHR1: ",
            "FROM1: ", "TO1: ", "GENOME2: ", "CHR2: ", "FROM2: ", "X: ", "Y: ", "BEFORE: ",
            "AFTER: ", "RADIUS: ", "N_MARKERS: ", "MARKER: "};


    /**
     * ***************************************
     */
    public static void parseInitialSettings(SequenceAnnotation Anno, String fl, int which) {
        int i;
        boolean flag = false;
        String line;

        try {
            BufferedReader br =
                    new BufferedReader(new FileReader(fl));

            /* Walk to the begining of annotation */
            while (true) {
                if ((line = br.readLine()) == null) {
                    break;
                }
                if (line.indexOf("Start of INITIALS") != -1) {
                    flag = true;
                    continue;
                }

                if (line.indexOf("End of INITIALS") != -1) {
                    break;
                }
                if (flag) {
                    if (which == 1) {
                        if ((i = line.indexOf("GENOME1: ")) != -1) {
                            Anno.setGenome(line.substring(i + 9));
                        }
                        if ((i = line.indexOf("CHR1: ")) != -1) {
                            Anno.setChromosome(line.substring(i + 6));
                        }
                        if ((i = line.indexOf("FROM1: ")) != -1) {
                            Anno.setSeqSegmentStart(Integer.valueOf(line.substring(i + 7)));
                        }
                        if ((i = line.indexOf("TO1: ")) != -1) {
                            Anno.setSeqSegmentEnd(Integer.valueOf(line.substring(i + 5)));
                        }
                    }
                    if (which == 2) {
                        if ((i = line.indexOf("GENOME2: ")) != -1) {
                            Anno.setGenome(line.substring(i + 9));
                        }
                        if ((i = line.indexOf("CHR2: ")) != -1) {
                            Anno.setChromosome(line.substring(i + 6));
                        }
                        if ((i = line.indexOf("FROM2: ")) != -1) {
                            Anno.setSeqSegmentStart(Integer.valueOf(line.substring(i + 7)));
                        }
                        if ((i = line.indexOf("TO2: ")) != -1) {
                            Anno.setSeqSegmentEnd(Integer.valueOf(line.substring(i + 5)));
                        }
                    }
                 }
            }
        }
        catch (IOException e) {
            System.out.println("IOException!");
        }
    }

}
