package org.geworkbench.components.alignment.blast;

import java.io.*;
import java.util.Random;
import java.util.StringTokenizer;

public class HmmResultParser extends BlastParser {
    private String algoName;
    // private String LINEBREAK = "\n";
    private String LINEBREAK = "<br>";

    /**
     * main
     *
     * @param aString String
     */
    public static void main(String[] aString) {

        HmmResultParser trp = new HmmResultParser(//   "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Algo-743651430.html");
                "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Hmm89547134.txt");
        // "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Hmm-1564401368.txt");
        trp.parseResults();
    }

    public HmmResultParser() {
    }

    public HmmResultParser(String filename) {
        super(filename);
    }

    public HmmResultParser(String filename, String algoName) {
        super(filename);
        this.algoName = algoName;
    }

    public boolean parseResults() {
        int tempRandomInt = new Random().nextInt();
        StringTokenizer st;
        BlastObj each;
        String[] fields;
        int index = 0, index2 = 0, start = 0;
        String query = "", subject = "", align = "";
        String errorline = "";

        try {
            File file = new File(filename);
            System.out.println(file.length() + file.getPath());
            //server failure
            if (file.length() < 10) {
                System.out.println("No hit found. Please try again.");
                return false;
            } else {
                //System.out.println("test1");
                //new BufferedReader for parsing file line by line
                BufferedReader br = new BufferedReader(new FileReader(filename));
                String line;
                String sub;
                //loop to proceed to beginning of hit list from Blast output file
                boolean reachTheEnd = false;
                while (!reachTheEnd) {
                    line = br.readLine();
                    //System.out.println("test2: " + line);
                    if (line == null) {
                        reachTheEnd = true;
                        break;
                    }

                    while (line != null && line.trim().startsWith("HMM BTK 5.2.1-88/90 2003-06-12 (Fdf Client 1.500)")) {
                        //begin to get 1 model.
                        do {
                            line = br.readLine();
                        } while (!line.trim().startsWith("Query HMM:"));
                        System.out.println(line.substring(10));
                        String[] hmmObjInfo = (line.substring(10)).split("\\|");
                        HmmObj eachHmmObj = null;
                        if (hmmObjInfo.length >= 3) {
                            eachHmmObj = new HmmObj(hmmObjInfo[0], hmmObjInfo[1], hmmObjInfo[2]);

                        }
                        String detaillines = "";
                        do {
                            line = br.readLine();
                            if (line != null) {
                                detaillines = detaillines + line + LINEBREAK;
                            } else {
                                reachTheEnd = true;
                            }
                        } while (!reachTheEnd && !line.trim().startsWith("HMM BTK 5.2.1-88/90 2003-06-12 (Fdf Client 1.500)"));
                        eachHmmObj.setDetailedAlignment(detaillines);
                        hits.add(eachHmmObj);
                        if (reachTheEnd)
                            break;
                    }
                }

            }
            int k = hits.size();

            for (int i = 0; i < k; i++) {
                HmmObj hmmObj = (HmmObj) hits.get(i);
                System.out.println(hmmObj);
            }
            return true;
        } catch (FileNotFoundException e) {
            System.out.println("file not found.");
            return false;
            //System.exit(1);
        } catch (IOException e) {
            System.out.println("IOException!");
            return false;
        } catch (Exception e) {

            System.out.println(errorline);
            e.printStackTrace();

            //find a blast bug, temp change to true.
            return true;

        }

    }

}
