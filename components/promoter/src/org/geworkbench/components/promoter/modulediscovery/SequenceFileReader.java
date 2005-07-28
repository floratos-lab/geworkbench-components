package org.geworkbench.components.promoter.modulediscovery;


import java.io.*;
import java.util.*;


/*============================================================================*/
/*Preprocess input sequence file;                                             */
/*============================================================================*/

public class SequenceFileReader {
    //outFile1: input file for discovery program.
    private String discoveryInputFile;

    public HashMap motiftoPrime = new HashMap();
    //outFile2: Motif-Prime_number pairs.
    //    private String motifPrimeFile;
    //Total number of sequence from the input file.
    private int numberOfSeq = 0;
    private int totalNumberOfMotif = 0;
    public Hashtable motifs = new Hashtable();
    public Hashtable tempmotifs = new Hashtable();
    public Vector lengthOfSeq = new Vector();
    public Vector numberOfMotifInSeq = new Vector();
    public Vector motifInFile = new Vector();
    public Vector randomMotifInFile = new Vector();
    public Vector[] randomPosition = new Vector[5000];


    public SequenceFileReader(File inFile) {
        this.discoveryInputFile = "outfile1";
        //        this.motifPrimeFile = "outFile2";
        int c;
        Integer n = new Integer(1);
        StringBuffer sb = new StringBuffer();
        String motif = new String();
        Integer position;

        //Motif name and prime number pair.
        Hashtable motifs = new Hashtable();
        Hashtable motifInSeq = new Hashtable();

        System.out.println("Preprocess data from " + inFile + " ...\n");
        try {
            FileReader fr = new FileReader(inFile);
            BufferedReader br = new BufferedReader(fr);
            FileOutputStream fos1 = new FileOutputStream(this.discoveryInputFile);
            //          FileOutputStream fos2 = new FileOutputStream(this.motifPrimeFile);
            PrintStream orig = System.out;
            PrintStream out1 = new PrintStream(fos1);
            //          PrintStream out2 = new PrintStream(fos2);
            System.setOut(out1);
            int flag = 0;
            int getKeyFlag = 0;
            while ((c = br.read()) != -1) {
                if ((c == '(') || (c == '$')) flag++;
                if ((c == ')') || (c == ';')) flag--;
                if (flag == 0) {
                    System.out.print(">");
                    numberOfSeq++;
                    //Empty motif-position hashtable
                    motifInSeq.clear();
                    flag = 1;
                }
                if ((flag == 1) && (c != ')') && (c != '\t') && (c != 10)) {
                    fos1.write((char) c);
                }
                if ((flag == 2) && (c == '(')) {
                    System.out.println();
                }
                if (c == '\n') {
                    //Sort motifInSeq;
                    //print out sorting motif position pair by position;
                    // Sort hashtable.
                    Vector v = new Vector(motifInSeq.keySet());
                    Collections.sort(v);
                    if (v.size() != 0) {
                        this.numberOfMotifInSeq.addElement(new Integer(v.size()));
                        this.lengthOfSeq.addElement(v.lastElement());
                    } else {
                        this.numberOfMotifInSeq.addElement(new Integer(v.size()));
                        this.lengthOfSeq.addElement(new Integer(0));
                    }
                    //Print (sorted) hashtable to file.
                    for (Enumeration e = v.elements(); e.hasMoreElements();) {
                        Integer key = (Integer) e.nextElement();
                        String val = (String) motifInSeq.get(key);
                        System.out.print("(" + tempmotifs.get(val) + "," + key + ") ");
                    }
                    System.out.println();
                    flag--;
                }
                if (flag == 3) {
                    switch (c) {
                        case '$':
                            if (getKeyFlag == 1) sb = new StringBuffer();
                            break;
                        case ' ':
                            getKeyFlag = 1;
                            motif = sb.toString();
                            totalNumberOfMotif++;
                            motifInFile.addElement(motif);
                            if (!motifs.containsValue(motif)) {
                                motifs.put(n = nextPrime(n), motif);
                                tempmotifs.put(motif, n);
                            }
                            sb = new StringBuffer();
                            break;
                        default:
                            if (c != ')') sb.append((char) c);
                            break;
                    }
                }
                if ((flag == 4) && (c == '(')) {
                    position = Integer.decode(sb.toString());
                    motifInSeq.put(position, motif);
                }
            }
            fos1.close();
            //          System.setOut(out2);

            // Sort hashtable.
            Vector v = new Vector(motifs.keySet());
            Collections.sort(v);
            //Print (sorted) hashtable to file.
            for (Enumeration e = v.elements(); e.hasMoreElements();) {
                Integer key = (Integer) e.nextElement();
                String val = (String) motifs.get(key);
                motiftoPrime.put(val, key);
                //            System.out.println(val + "    " + key);
            }
            br.close();
            //          fos2.close();
            //          System.setOut(orig);
        } catch (IOException e) {
            System.out.println("Uh oh, got an IOException error in SequenceFileReader!");
            e.printStackTrace();
        }
    }


    public String getDiscoveryInputFile() {
        return this.discoveryInputFile;
    }

    //    public String getMotifPrimeFile(){
    //      return this.motifPrimeFile;
    //    }

    public int getNumberOfSeq() {
        return this.numberOfSeq;
    }

    public int getTotalMotifNumber() {
        return this.totalNumberOfMotif;
    }

    public void randomizeMotifVector() {
        for (int i = this.totalNumberOfMotif; i > 0; i--) {
            int p = (int) (i * Math.random());
            randomMotifInFile.addElement(motifInFile.elementAt(p));
            motifInFile.remove(p);
        }
    }

    //Random generate n numbers in range of m and sort those n numbers
    //Store those n numbers in Array:
    //motifPosition[totalNumberOfSeq][numberOfMotifInSeq]
    public void randomGeneratePosition() {
        for (int i = 0; i < numberOfSeq; i++) {
            int temp = ((Integer) (lengthOfSeq.elementAt(i))).intValue();
            //    System.out.println("$$$$$$$$$$" + temp);
            if (temp != 0) {
                int tmp = ((Integer) (numberOfMotifInSeq.elementAt(i))).intValue();
                randomPosition[i] = new Vector();
                for (int j = 0; j < tmp; j++) {
                    //        System.out.println("$$$$$$$$$$77&&&&&" + tmp);
                    randomPosition[i].addElement(new Integer((int) (Math.random() * temp)));
                    Collections.sort(randomPosition[i]);
                }
            }
        }
    }


    private static boolean isPrime(int n) {
        if (n == 2 || n == 3) return true;
        if (n == 1 || n % 2 == 0) return false;
        for (int i = 3; i * i <= n; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }


    public static Integer nextPrime(Integer m) {
        int n;
        n = m.intValue();
        if (n <= 1) return new Integer(2);
        if ((++n) % 2 == 0) n++;
        for (; !isPrime(n); n += 2) ;
        return new Integer(n);
    }

    public String randomizeInputFile() {
        String randomFile = "randomFile.txt";
        try {
            FileWriter fw = new FileWriter(randomFile);
            PrintWriter pw = new PrintWriter(fw);
            int index = 0;
            for (int i = 0; i < numberOfSeq;) {
                fw.write(">" + ++i);
                pw.println();
                for (int j = 0; j < ((Integer) (numberOfMotifInSeq.elementAt(i - 1))).intValue(); j++) {
                    index++;
                    fw.write('(');
                    fw.write(tempmotifs.get(randomMotifInFile.elementAt(index)).toString());
                    fw.write(',');
                    fw.write(randomPosition[i - 1].elementAt(j).toString());
                    fw.write(") ");
                }
                pw.println();
            }
            fw.close();
            pw.close();
        } catch (IOException e) {
            System.out.println("Error in SequenceFileReader.random()!");
            e.printStackTrace();
        }

        return randomFile;
    }


}
