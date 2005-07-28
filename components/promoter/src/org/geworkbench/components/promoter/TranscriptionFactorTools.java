package org.geworkbench.components.promoter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class TranscriptionFactorTools {
    private static HashMap hash = new HashMap();
    private static final String path = " TranscriptionFactorInfo";

    static {
        File indx = new File(path);
        if (indx.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(indx));
                String oneline = br.readLine();
                while (oneline != null) {
                    String[] data = oneline.split("\t");
                    hash.put(data[0], data[1]);
                    oneline = br.readLine();
                }
                br.close();
            } catch (Exception e) {
            }

        }
    }


    //  public static double getThreshold(double percent, TranscriptionFactor tf) {
    //
    //    String thstr=(String)hash.get(tf.getLabel());
    //
    //    if (thstr!=null){
    //      return Double.parseDouble(thstr);
    //    }
    //    double scores[] = new double[1000];
    //    for (int i = 0; i < 1000; i++) {
    //      Sequence seq = RandomSequenceGenerator.getRandomSequence(1000, tf.getMatrix().symbols);
    //      for (int offset = 1;
    //           offset < seq.length() - tf.getLength() + 1;
    //           offset++) {
    //        double score = tf.scoreWeightMatrix(seq, offset);
    //        double q = Math.exp(score);
    //        if (q > scores[i]) {
    //          scores[i] = q;
    //
    //        }
    //      }
    //
    //    }
    //    int indx = (int) (1000 - percent * 10);
    //    Arrays.sort(scores);
    //    try{
    //      BufferedWriter br = new BufferedWriter(new FileWriter(path,true));
    //      String result=tf.getLabel()+"\t"+scores[indx]+"\n";
    //      br.write(result);
    //      br.close();
    //      hash.put(tf.getLabel(),(scores[indx]+""));
    //    }catch(Exception e){}
    //    return scores[indx];
    //
    //  }

}
