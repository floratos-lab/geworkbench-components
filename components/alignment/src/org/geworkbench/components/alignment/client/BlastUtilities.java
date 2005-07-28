package org.geworkbench.components.alignment.client;

import javax.swing.text.Utilities;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.RandomAccessFile;

public class BlastUtilities extends Utilities {
    public BlastUtilities() {
    }

    public static boolean changeHeader(String fileName) {
        try {


            BufferedReader in = new BufferedReader(new FileReader(fileName));

            String s = in.readLine();
            String newS = s.replaceAll("1.3.6-Paracel", "2.0.11");
            in.close();
            RandomAccessFile ran = new RandomAccessFile(fileName, "rw");
            ran.writeBytes(newS + "       ");
            ran.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
