package org.geworkbench.components.alignment.blast;

import java.io.*;
import java.util.Random;
import java.util.StringTokenizer;

public class TextResultParser extends BlastParser {
    private String algoName;
    private String LINEBREAK = "\n";

    /**
     * main
     *
     * @param aString String
     */
    public static void main(String[] aString) {

        TextResultParser trp = new TextResultParser(//   "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Algo-743651430.html");

                "C:\\FromOldCDisk\\cvsProject\\project\\BioWorks\\temp\\GEAW\\Algo-221092603.html");
        trp.parseResults();
    }

    public TextResultParser() {
    }

    public TextResultParser(String filename) {
        super(filename);
    }

    public TextResultParser(String filename, String algoName) {
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
                while (true) {
                    line = br.readLine();
                    //System.out.println("test2: " + line);

                    if (line.startsWith("Sequences producing significant alignments:")) {
                        break;
                    }
                }

                /* parsing section of the blast Hit info text*/
                br.readLine();
                line = br.readLine();

                //line = "gi|20663779|pdb|1H8P|A Chain A, Bull Seminal Plasma  Pdc-10...    59  3e-09";
                int added = 0;
                while (line.length() != 0) {
                    String s = "    "; //"\\|";
                    String[] strA = line.split(s);
                    //System.out.println(line);
                    for (int i = 0; i < strA.length; i++) {
                        //  System.out.println(i  +"(" + strA[i] +")");
                    }

                    each = new BlastObj(); //create new BlastObj for hit
                    if (strA.length < 2) {
                        each.setRetriveWholeSeq(false);
                    } else {
                        each.setRetriveWholeSeq(true);
                        StringTokenizer str = new StringTokenizer(strA[0], "|");
                        if (str.countTokens() > 3) {
                            if (str.nextToken().equals("gi")) {
                                each.setSeqID(str.nextToken());
                            }
                            each.setDatabaseID(str.nextToken());
                            each.setName(str.nextToken());
                            each.setDescription(str.nextToken());
                        }
                        str = new StringTokenizer(strA[1], "  ");

                        each.setScore(new Integer(str.nextToken().trim()).intValue());
                        each.setEvalue(str.nextToken());

                        System.out.println(each.databaseID + each.description + ")(" + each.evalue + each.getSeqID());

                        hits.add(each);
                        added++;
                        System.out.println(added);
                    }
                    //System.out.println(each.getDatabaseID() + each.getDescription()
                    //                   + each.getEvalue() + each.getScore());
                    line = br.readLine();
                }
                index = 0;
                line = br.readLine();
                boolean endofResult = false;
                /* parsing the section with alignments */
                while (!endofResult && line.trim().startsWith(">")) {

                    String detaillines = line + LINEBREAK;
                    //get BlastObj hit for which alignment is for
                    System.out.println(index + " = index " + line + endofResult);
                    each = (BlastObj) hits.get(index);

                    do {
                        line = br.readLine();
                        detaillines = line + LINEBREAK;
                    } while (!line.trim().startsWith("Length"));
                    //StringTokenizer str = new StringTokenizer(line, " =");
                    String str[] = line.split("=");
                    each.setLength(new Integer(str[1].trim()).intValue());

                    do {
                        line = br.readLine();
                        detaillines = line + LINEBREAK;
                    } while (!line.trim().startsWith("Identities"));

                    if (line.trim().startsWith("Identities")) {
                        str = line.split(" = ");
                        each.setIdentity(str[1]);
                        StringTokenizer st1 = new StringTokenizer(str[1], "(");
                        st1.nextToken();
                        String identity = st1.nextToken();
                        String[] s = identity.split("%");
                        each.setPercentAligned(new Integer(s[0]).intValue());

                    }

                    do {
                        line = br.readLine();
                        detaillines = line + LINEBREAK;
                    } while (!line.trim().startsWith("Query"));

                    do {
                        System.out.println("(" + line + ")");
                        line = br.readLine();
                        if (line != null) {

                            detaillines += line + NEWLINESIGN;
                        } else {
                            endofResult = true;
                        }
                        System.out.println("(" + endofResult + ")");
                    } while (!endofResult && !line.trim().startsWith(">"));

                    //System.out.println(detaillines);
                    each.setDetailedAlignment(detaillines);
                    if (!endofResult) {
                        index++;
                    }
                }

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
