package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.util.sequences.SequenceAnnotationTrack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 * @author not attributable
 * @version 1.0
 */

public class RemoteRepeatMasker {

    private static String request = null;
    private static SequenceAnnotationTrack SeqATObj;

    /**
     *
     */

    public static void runRemoteRepeatMasker(SequenceAnnotation sa, String fil) {

        /* Forming request */
        try {
            BufferedReader br = new BufferedReader(new FileReader(fil));
            /* skipping sequence name */
            String line = br.readLine();

            request =
                "http://www.repeatmasker.org/cgi-bin/WEBRepeatMasker?filename=&sequence=";

            /* adding the sequence */
            while (true) {
                if ( (line = br.readLine()) == null) {
                    break;
                }
                request = request.concat(line);
            }
            request = request.concat("&ReturnFormat=html&ReturnMethod=html&mailto=Your+email+address&submit=Submit+Sequence&speed=default&dnasource=human&contamination=none&repeatoptions=default&artifactcheck=default&alignment=default&masking=default&matrix=default&divergence=&lineage=default&species3=default&.cgifields=speed&.cgifields=ReturnMethod&.cgifields=ReturnFormat&.cgifields=dnasource&.cgifields=Parameters&.cgifields=mrna");
        }
        catch (IOException e) {
            System.out.println(e);
        }
        SeqATObj = new SequenceAnnotationTrack();
        if (GetItToAnnoTrack(SeqATObj, request)) {
            sa.addAdjustZeroAnnoTrack(SeqATObj);
        }
    }

    public static String GetIt(String UrlToGet) {
        int i, j, info;
        StringBuffer oneL = new StringBuffer();
        URL infoLink = null;
        InputStream serverIO = null;
        String buf = null;

        try {
            infoLink = new URL(UrlToGet);
        }
        catch (MalformedURLException ee) {
            System.out.println("Malformed URL " + UrlToGet + " : " + ee);
            return null;
        }

        // start the connection with the httpd and talk
        try {
            serverIO = infoLink.openStream();
        }
        catch (IOException e) {
            System.out.println("Can't open stream " + UrlToGet + " : " + e);
            return null;
        }

        try {
            /* Read */
            while ( (info = serverIO.read()) != -1) {
                oneL.append( (char) info); /* make note of the info */
            }
            serverIO.close();
            buf = new String(oneL);
        }
        catch (IOException e) {
            System.out.println(e);
            return null;
        }
        return buf;
    }

    public static boolean GetItToAnnoTrack(SequenceAnnotationTrack sat,
                                           String UrlToGet) {
        int i, j, start, end, num;
        String name, temp;
        boolean dir;

        int SequenceHitStart = 0;
        int SequenceHitEnd = 0;
        String FeatureName = null;
        boolean FeatureDirection = true;

        /* Get data */
        String buf = GetIt(UrlToGet);

        if (buf == null) {
            return false;
        }

        /* checking for the "no repeats found" */
        if ( (i = buf.indexOf("There were no repetitive sequences detected")) !=
            -1) {
            return false;
        }

        /* First - count number of features in this track */
        num = 0;
        i = 0;
        while (true) {
            /* extracting and writting the title */
            i = buf.indexOf(" UnnamedSequence", i);
            if (i == -1) {
                break;
            }
            i++;
            num++;
        }

        /* Allocation of sequence annotation track */
        sat.setAnnotationName("Remote RepeatMasker");

        /* cycle through the html output - parsing repeat names and start/end features */
        i = 0;
        num = 0;
        while (true) {

            /* extracting and writting the title */
            i = buf.indexOf(" UnnamedSequence", i) + 1;

            /* extracting start */
            for (; i < buf.length(); i++) {
                if (buf.charAt(i) <= 32) {
                    break;
                }
            }
            for (; i < buf.length(); i++) {
                if (buf.charAt(i) > 32) {
                    break;
                }
            }
            for (j = i + 1; j < buf.length(); j++) {
                if (buf.charAt(j) <= 32) {
                    break;
                }
            }

            start = Integer.parseInt(buf.substring(i, j));
            SequenceHitStart = start;

            /* extracting end */
            for (i = j; i < buf.length(); i++) {
                if (buf.charAt(i) > 32) {
                    break;
                }
            }
            for (j = i + 1; j < buf.length(); j++) {
                if (buf.charAt(j) <= 32) {
                    break;
                }
            }
            end = Integer.parseInt(buf.substring(i, j));
            SequenceHitEnd = end;

            /* skipping the reast_length */
            for (i = j; i < buf.length(); i++) {
                if (buf.charAt(i) > 32) {
                    break;
                }
            }
            for (j = i + 1; j < buf.length(); j++) {
                if (buf.charAt(j) <= 32) {
                    break;
                }
            }

            for (i = j; i < buf.length(); i++) {
                if (buf.charAt(i) > 32) {
                    break;
                }
            }

            for (j = i + 1; j < buf.length(); j++) {
                if (buf.charAt(j) <= 32) {
                    break;
                }
            }

            /* extracting directions */
            if (buf.charAt(i) == '+') {
                FeatureDirection = true;
            }
            else {
                FeatureDirection = false;
            }

            /* extracting name */
            for (i = j; i < buf.length(); i++) {
                if (buf.charAt(i) > 32) {
                    break;
                }
            }
            for (j = i + 1; j < buf.length(); j++) {
                if (buf.charAt(j) <= 32) {
                    break;
                }
            }
            name = new String(buf.substring(i, j));
            FeatureName = new String(name);

            sat.addFeature(SequenceHitStart, SequenceHitEnd, SequenceHitStart,
                           SequenceHitEnd,
                           SequenceHitEnd - SequenceHitStart + 1,
                           FeatureDirection, true, 0,
                           FeatureName, FeatureName, null);
            num++;

            for (j = i + 1; j < buf.length(); j++) {
                if (buf.charAt(j) == '\n') {
                    break;
                }
            }

            if (buf.charAt(j + 1) == '<') {
                break;
            }
        }
        return true;
    }
}
