package org.geworkbench.components.alignment.synteny;

import org.geworkbench.util.sequences.SequenceAnnotation;
import org.geworkbench.util.sequences.SequenceAnnotationTrack;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version 1.0
 */

public class GPAnnoParser {
    private static SequenceAnnotation SeqAObj;
    private static SequenceAnnotationTrack[] SeqATObj;
    private static String startTag = null;
    private static String endTag = null;

    public static void runGPAnnoParser(SequenceAnnotation sa, String gpAnnoFile, int which) {
        SeqAObj = sa;
        if (which == 1) {
            startTag = new String("/# Start of Annotation 1 #/");
            endTag = new String("/# End of Annotation 1 #/");
        }
        if (which == 2) {
            startTag = new String("/# Start of Annotation 2 #/");
            endTag = new String("/# End of Annotation 2 #/");
        }
        parseGoldenPathAnnotation(gpAnnoFile);
    }

    private static void parseGoldenPathAnnotation(String gpAnnoFile) {
        int i, j = 0, numAnno = 0;
        int[] numEach = new int[100], new_count = new int[100];
        String[] types = new String[100];
        String line, tempStr, tempStr1 = null, tempStr2 = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(gpAnnoFile));

            /* Walk to the begining of annotation */
            while (true) {
                if ((line = br.readLine()) == null) break;
                if ((i = line.indexOf(startTag)) != -1) {
                    break;
                }
            }

            /* Counting everithing */
            while (true) {
                if ((line = br.readLine()) == null) break;
                if ((i = line.indexOf(endTag)) != -1) {
                    break;
                }
                if ((i = line.indexOf("<TYPE id=")) != -1) {
                    tempStr = line.substring(i + 10);
                    i = tempStr.indexOf('"');
                    tempStr = tempStr.substring(0, i);
                    for (i = 0; i < numAnno; i++) {
                        if (types[i].equalsIgnoreCase(tempStr)) {
                            break;
                        }
                    }
                    if (i == numAnno) {
                        /* new type */
                        types[i] = new String(tempStr);
                        numEach[i] = 1;
                        new_count[i] = -1;
                        numAnno++;
                    } else {
                        /* existing type */
                        numEach[i]++;
                    }
                }
                if (line.indexOf("</DASGFF>") != -1) {
                    break;
                }
            }

            /* Allocation */
            SeqATObj = new SequenceAnnotationTrack[100];
            for (i = 0; i < numAnno; i++) {
                SeqATObj[i] = new SequenceAnnotationTrack(numEach[i]);
                SeqATObj[i].setAnnotationName(types[i]);
            }

            /****** Extract the annotation */
            br = new BufferedReader(new FileReader(gpAnnoFile));

            /* Walk to the begining of annotation */
            while (true) {
                if ((line = br.readLine()) == null) break;
                if ((i = line.indexOf(startTag)) != -1) {
                    break;
                }
            }

            /* Extracting */
            while (true) {
                if ((line = br.readLine()) == null) break;
                if ((i = line.indexOf(endTag)) != -1) {
                    break;
                }

                if ((i = line.indexOf("<FEATURE id=")) != -1) {
                    tempStr = line.substring(i + 13);
                    i = tempStr.indexOf('\"');
                    tempStr = tempStr.substring(0, i);
                    tempStr1 = new String(tempStr);
                }

                if ((i = line.indexOf("label=\"")) != -1) {
                    tempStr = line.substring(i + 7);
                    i = tempStr.indexOf('\"');
                    tempStr = tempStr.substring(0, i);
                    tempStr2 = new String(tempStr);
                }

                if ((i = line.indexOf("<TYPE id=")) != -1) {
                    tempStr = line.substring(i + 10);
                    i = tempStr.indexOf('"');
                    tempStr = tempStr.substring(0, i);
                    for (j = 0; j < numAnno; j++) {
                        if (types[j].equalsIgnoreCase(tempStr)) {
                            break;
                        }
                    }
                    new_count[j]++;
                    SeqATObj[j].setFeatureName(new_count[j], tempStr1);
                    SeqATObj[j].setFeatureTag(new_count[j], tempStr2);
                }

                if ((i = line.indexOf(" start=\"")) != -1) {
                    tempStr = line.substring(i + 8);
                    i = tempStr.indexOf('\"');
                    tempStr = tempStr.substring(0, i);
                    //          SeqAObj.setSeqSegmentStart((int)Integer.parseInt(tempStr));
                }

                if ((i = line.indexOf(" stop=\"")) != -1) {
                    tempStr = line.substring(i + 7);
                    i = tempStr.indexOf('\"');
                    tempStr = tempStr.substring(0, i);
                    //          SeqAObj.setSeqSegmentEnd((int)Integer.parseInt(tempStr));
                }

                if ((i = line.indexOf("<START>")) != -1) {
                    tempStr = line.substring(i + 7);
                    i = tempStr.indexOf('<');
                    tempStr = tempStr.substring(0, i);
                    SeqATObj[j].setSequenceHitStart(new_count[j], (int) Integer.parseInt(tempStr));
                }

                if ((i = line.indexOf("<END>")) != -1) {
                    tempStr = line.substring(i + 5);
                    i = tempStr.indexOf('<');
                    tempStr = tempStr.substring(0, i);
                    SeqATObj[j].setSequenceHitEnd(new_count[j], (int) Integer.parseInt(tempStr));
                }

                if ((i = line.indexOf("<SCORE>")) != -1) {
                    tempStr = line.substring(i + 7);
                    i = tempStr.indexOf('<');
                    tempStr = tempStr.substring(0, i);
                    if (tempStr.equalsIgnoreCase("-")) {
                        SeqATObj[j].setFeatureEValue(new_count[j], -1);
                    } else {
                        SeqATObj[j].setFeatureEValue(new_count[j], (double) Double.parseDouble(tempStr));
                    }
                }

                if ((i = line.indexOf("<ORIENTATION>")) != -1) {
                    tempStr = line.substring(i + 13);
                    i = tempStr.indexOf('<');
                    tempStr = tempStr.substring(0, i);
                    if (tempStr.equalsIgnoreCase("-")) {
                        SeqATObj[j].setFeatureDirection(new_count[j], false);
                    } else {
                        SeqATObj[j].setFeatureDirection(new_count[j], true);
                    }
                }

                if ((i = line.indexOf("<LINK href=\"")) != -1) {
                    tempStr = line.substring(i + 12);
                    i = tempStr.indexOf('\"');
                    tempStr = tempStr.substring(0, i);
                    SeqATObj[j].setFeatureURL(new_count[j], tempStr);
                }

                if (line.indexOf("</DASGFF>") != -1) {
                    /* The end of DAS output */
                    break;
                }
            }

            /* Set active and non active features */
            for (i = 0; i < numAnno; i++) {
                for (j = 0; j < numEach[i]; j++) {
                    if (SeqATObj[i].getSequenceHitEnd(j) < SeqAObj.getSeqSegmentStart() || SeqATObj[i].getSequenceHitEnd(j) > SeqAObj.getSeqSegmentEnd()) {
                        SeqATObj[i].setFeatureActive(j, false);
                    } else {
                        SeqATObj[i].setFeatureActive(j, true);
                    }
                }
            }

            /* Add annotation to the SequenceAnnotation */
            for (i = 0; i < numAnno; i++) {
                SeqAObj.addAnnoTrack(SeqATObj[i]);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Input file not found.");
            //      System.exit(1);
        } catch (IOException e) {
            System.out.println("IOException!");
        }
    }
}
