package org.geworkbench.components.sequenceretriever;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Timer;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;

/**
 * <p>Title: geworkbench</p>
 * <p/>
 * <p> Script to retrieve Promoter sequence from UCSC's DAS sequence server</p>
 * <p/>
 * <p>Copyright: Copyright (c) 2003 -2005</p>
 * <p>Company: Columbia University</p>
 *
 * @author Adam Margolin
 * @author Nilanjana Banerjee, manjunath at genomecenter dot columbia dot edu
 */

public class PromoterSequenceFetcher {

    public static int UPSTREAM = 2000;
    public static int DOWNSTREAM = 2000;

    static Timer timer = new Timer(240000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            cachedSequences = null;
            System.gc();
        }
    });

    public PromoterSequenceFetcher() {
    }

    private static CSSequenceSet cachedSequences = null;

    public static void populateSequenceCache() {
//        if (!timer.isRunning())
//            timer.start();
        File file = new File(System.getProperty("temporary.files.directory") +
                             File.separator + "sequences" + File.separator +
                             "cachedSequences");
        if (cachedSequences == null) {
            if (file.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    cachedSequences = (CSSequenceSet) ois.readObject();
                    ois.close();
                    fis.close();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                } catch (ClassNotFoundException cnfe) {
                    cnfe.printStackTrace();
                }
            } else {
                URL url = PromoterSequenceFetcher.class.getResource(
                        "All.NC.-2k+2k.txt");
                File downloadedFile =
                        new File(System.getProperty("temporary.files.directory") +
                                 File.separator + "sequences" + File.separator +
                                 "downloadedSequences");
                try {
                    if (!downloadedFile.exists()) {
                        downloadedFile.getParentFile().mkdirs();
                        downloadedFile.createNewFile();
                        url = new URL(System.getProperty("data.download.site") +
                                      "All.NC.-2k+2k.txt");
                        BufferedReader br = new BufferedReader(new
                                InputStreamReader(url.openStream()));
                        BufferedWriter bw = new BufferedWriter(new FileWriter(
                                downloadedFile));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            bw.write(line);
                            bw.write("\n");
                        }
                        bw.flush();
                        br.close();
                        bw.close();
                    }
                } catch (MalformedURLException mfe) {
                    mfe.printStackTrace();
                } catch (FileNotFoundException fnfe) {

                } catch (IOException ioe) {

                }
                try {
                    cachedSequences = CSSequenceSet.getSequenceDB(
                            downloadedFile);
                    cachedSequences.parseMarkers();
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file.
                            getAbsolutePath());
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(cachedSequences);
                    oos.flush();
                    oos.close();
                } catch (FileNotFoundException fnfe) {
                    fnfe.printStackTrace();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }

    public static CSSequence getCachedPromoterSequence(DSGeneMarker marker,
            int upstream, int fromStart) {
        if (cachedSequences == null) {
            populateSequenceCache();
        }
        if (cachedSequences != null) {
            CSSequence sequence = (CSSequence) cachedSequences.get(marker.
                    getLabel());
            if (sequence != null) {
                return sequence.getSubSequence(UPSTREAM - upstream - 1,
                                               sequence.length() - DOWNSTREAM +
                                               fromStart - 1);
            }
        }
        return null;
    }


}
