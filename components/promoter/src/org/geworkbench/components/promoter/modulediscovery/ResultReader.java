package org.geworkbench.components.promoter.modulediscovery;

import java.io.*;
import java.util.HashMap;

public class ResultReader {


    //    private Vector positionVector = new Vector();
    private HashMap modulePosition = new HashMap();

    //    public HashMap readMotifPrimePairFile(String motifPrimePairFile) throws
    //        FileNotFoundException, IOException {
    //        File motifListFile = new File(motifPrimePairFile);
    //       return ReadMotifPrimePair(motifListFile);
    //    }

    //    public HashMap ReadMotifPrimePair(File motifPrimePairFile) throws
    //        FileNotFoundException, IOException {
    //
    //
    //        String line = null;
    //        FileReader fr1 = new FileReader(motifPrimePairFile);
    //        BufferedReader in1 = new BufferedReader(fr1);
    //
    //        while ( (line = in1.readLine()) != null) {
    //             String temp[] = line.split("\\s+");//"\s+" is any length of white space chars
    //            this.primeToMotifMap.put(temp[temp.length-1],temp[0]);
    //
    //        }
    //        fr1.close();
    //        in1.close();
    //        return primeToMotifMap;
    //    }

    public void moduleResultFileReader(String moduleResultFile) throws FileNotFoundException, IOException {
        File moduleFile = new File(moduleResultFile);
        moduleResultFileReader(moduleFile);
    }

    public void moduleResultFileReader(File moduleResultFile) throws FileNotFoundException, IOException {
        FileReader frm = new FileReader(moduleResultFile);
        BufferedReader brm = new BufferedReader(frm);
        String line = new String();
        while ((line = brm.readLine()) != null) {
            String tmpm[] = line.split("->");
            String module = tmpm[0];
            module = module.substring(1, module.length() - 2);
            String[] motifs = module.split("\\s+");
            String display = "";
            for (int i = 0; i < motifs.length; i++) {
                //               display=display+(String)primeToMotifMap.get(motifs[i])+",";
                //               if( primeToMotifMap.get(motifs[i])==null){
                //               System.out.println(motifs[i]);;
                //
                //               }

            }
            //            this.moduleVector.addElement("{"+display+"}");
            //
            //            this.positionVector.addElement(tmpm[1]);
            this.modulePosition.put(display, tmpm[1]);
        }
        frm.close();
        brm.close();
    }

    //    public HashMap getMotifToPrimeMap() {
    //        return primeToMotifMap;
    //    }
    public HashMap getModulePositionMap() {
        return modulePosition;

    }
    //    public Vector getPositionVector() {
    //        return positionVector;
    //    }
    //
    //    public Vector getModuleVector() {
    //        return moduleVector;
    //    }

}
