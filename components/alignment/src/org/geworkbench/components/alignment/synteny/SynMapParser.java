package org.geworkbench.components.alignment.synteny;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SynMapParser {

        /**
         * Just populate SyntenyMapObject for testing purposes
         * @return SyntenyMapObject
         */

        int i, j, k, upper_num, lower_num;
        int pair_num;
        static SyntenyMapObject smo;
        static SyntenyMapFragment smf;
        String[] unames= new String[500];
        String[] lnames= new String[500];
        String[] ugnm = new String[500];
        String[] uchr = new String[500];
        String[] lgnm = new String[500];
        String[] lchr = new String[500];
        int[] ust = new int[500];
        int[] uen = new int[500];
        int[] lst = new int[500];
        int[] len = new int[500];

        public SynMapParser(String inf, SyntenyMapObject synMO, int fx, int tx, int fy, int ty) {
            int i, j;

            try {
                    //new BufferedReader for parsing file line by line
                    BufferedReader br =
                        new BufferedReader(new FileReader(inf));
                    String line = null;

                    /* Walk to the begining of annotation */
                    while (true) {
                        if ( (line = br.readLine()) == null) {
                            break;
                        }
                        if (line.indexOf("/# Start of SYN_MAP output #/") != -1) {
                            break;
                        }
                    }

                    //loop to proceed the header
                    while (true) {
                        line = br.readLine();

                        if (line.startsWith("SP: ")) {

                            /* new synteny pair */
                            pair_num = (Integer.parseInt(line.substring(line.indexOf("SP: ")+4)));
                        }

                        if (line.startsWith("N1: ")) {
                            /* the conntent of the first region */
                            upper_num = (Integer.parseInt(line.substring(line.indexOf("N1: ")+4)));
                            for(i=0;i<upper_num;i++){
                                line = br.readLine();
                                String[] splits=line.split("\t");
                                unames[i]=new String(splits[0]);
                                ugnm[i]=new String(splits[1]);
                                uchr[i]=new String(splits[2]);
                                ust[i]=(Integer.parseInt(splits[3]));
                                uen[i]=(Integer.parseInt(splits[4]));
                            }
                            /* parse for all upper nums */
                        }

                        if (line.startsWith("N2: ")) {
                            lower_num = (Integer.parseInt(line.substring(line.indexOf("N2: ")+4)));
                            for(i=0;i<lower_num;i++){
                                line = br.readLine();
                                String[] splits=line.split("\t");
                                lnames[i]=new String(splits[0]);
                                lgnm[i]=new String(splits[1]);
                                lchr[i]=new String(splits[2]);
                                lst[i]=(Integer.parseInt(splits[3]));
                                len[i]=(Integer.parseInt(splits[4]));
                            }

                            smf = new SyntenyMapFragment(upper_num,lower_num);
                            /* Filing out this Fragment and add it to the list */
                            for(i=0;i<upper_num;i++){
                                smf.setUpperName(i,unames[i]);
                                smf.setUpperStart(i,ust[i]);
                                smf.setUpperEnd(i,uen[i]);
                                smf.setUpperGenome(ugnm[i]);
                                smf.setUpperChromosome(uchr[i]);
                            }
                            for(i=0;i<lower_num;i++){
                                smf.setLowerName(i,lnames[i]);
                                smf.setLowerStart(i,lst[i]);
                                smf.setLowerEnd(i,len[i]);
                                smf.setLowerGenome(ugnm[i]);
                                smf.setLowerChromosome(uchr[i]);
                            }
                        }

                        if (line.startsWith("DS:")) {
                            int l=0;
                            for(i=0;i<lower_num;i++){
                                for(j=0;j<lower_num;j++){
                                    line = br.readLine();
                                    double dbv = Double.parseDouble(line);
                                    smf.addPair(l++,i,j,dbv);
                                }
                            }
                        }

                        if (line.startsWith("END")) {
                            smf.setUpperCoordinates(fx,tx);
                            smf.setLowerCoordinates(fy,ty);
                            synMO.addSyntenyFragment(smf);
                        }

                        if (line.indexOf("/# End of SYN_MAP output #/") != -1) {
                            br.close();
                            break;
                        }

                    }
            }
            catch (FileNotFoundException e) {
                System.out.println("Input file not found.");
            }
            catch (IOException e) {
                System.out.println("IOException!");
        }
    }

    public SynMapParser() {
    }
}
