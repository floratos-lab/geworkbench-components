package org.geworkbench.components.alignment.client;

import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.components.alignment.panels.BlastAppComponent;
import org.geworkbench.components.alignment.panels.BrowserLauncher;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;
import org.globus.progtutorial.clients.BlastService.Client;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author XZ
 * @version 1.0
 */
public class BlastAlgorithm
    extends BWAbstractAlgorithm implements SoapClientIn {
    /**
     * BlastAlgorithm
     *
     * @param aBoolean boolean
     */
    public BlastAlgorithm(boolean aBoolean, String inputFile, Client _client) {
        gridEnabled = aBoolean;
        client = _client;
        inputFilename = inputFile;

    }

    private Client client;
    private BlastAppComponent blastAppComponent = null;
    private SoapClient soapClient = null;
    private boolean startBrowser;
    private boolean gridEnabled = false;
    private boolean jobFinished = false;
    private String inputFilename;
    private String tempURLFolder =
        "http://amdec-bioinfo.cu-genome.org/html/temp/";

    public void setBlastAppComponent(BlastAppComponent _blastAppComponent) {
        blastAppComponent = _blastAppComponent;
    }

    public BlastAlgorithm() {

    }

    public double getCompletion() {
        if (jobFinished) {
            //Make it bigger than 1, it means that job is done.
            return 3;
        }
        return super.getCompletion();
    }

    public void execute() {
        DSAncillaryDataSet blastResult = null;
      //  System.out.println("INBlastAlgo");
        try {
            if (soapClient == null) {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (soapClient != null) {

                //update the progressbar to ini.
                //  ProgressBarEvent progressBarEvent =
                // new ProgressBarEvent("Uploading...", Color.pink, 0, 0, 10);
                // blastAppComponent.throwEvent(ProjectNodeAddedListener.class, "projectNodeAdded", progressBarEvent);

                String cmd = soapClient.getCMD();
                String textFile = "";
                String htmlFile = null;

                if (cmd.startsWith("pb")) {
//TEMP out by xq
                    soapClient.startRun(true);
                    htmlFile = ( (SoapClient) soapClient).getOutputfile();
                    if (stopRequested) {
                        return;
                    }
                    if (startBrowser && !stopRequested) {
                        if ( (new File(htmlFile)).canRead()) {

                            BrowserLauncher.openURL(tempURLFolder +
                                getFileName(htmlFile));
                        }
                        else {
                            System.out.println("CANNOT READ " + htmlFile);
                        }
                    }
                }
                else {
                    soapClient.startRun();
                    textFile = ( (SoapClient) soapClient).getOutputfile();
                }

                blastAppComponent.blastFinished(cmd);

                // AlignmentResultEvent event =  new AlignmentResultEvent(outputFile, null, "message", null, null);
                // DataSet blastDataSet = new BlastDataSet(textFile);
                // System.out.println("htmlFile = " + htmlFile + "textfile" + textFile);

                if (htmlFile != null) {
                    blastResult = new CSAlignmentResultSet(htmlFile, soapClient.getInputFileName(), null);
          //          ( (CSAlignmentResultSet) blastResult).setAlgorithm(this);
                }
                else if (cmd.startsWith("btk search")) {

                    blastResult = new SWDataSet(textFile,
                                                soapClient.getInputFileName(), blastAppComponent.getFastFile());
                } else if (cmd.startsWith("btk hmm")) {

                    blastResult = new HMMDataSet(textFile,
                                                 soapClient.getInputFileName(), blastAppComponent.getFastFile());
                }
                ProjectNodeAddedEvent event =
                    new ProjectNodeAddedEvent(null,   null,
                                              blastResult);

                blastAppComponent.publishProjectNodeAddedEvent(event);

            }

            if (gridEnabled) {
                String tempFolder = System.getProperties().getProperty(
                    "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";

                }

                blastResult = new CSAlignmentResultSet(tempFolder + "a.html", inputFilename, null);
        //        ( (CSAlignmentResultSet) blastResult).setAlgorithm(this);
                              org.geworkbench.events.ProjectNodeAddedEvent event = new org.
                      geworkbench.events.ProjectNodeAddedEvent("message", null,
                        blastResult);
              blastAppComponent.publishProjectNodeAddedEvent(event);
//                ProjectNodeAddedEvent event =
//                    new ProjectNodeAddedEvent(null, "message", null,
//                                              blastResult);
//
//                blastAppComponent.throwEvent(ProjectNodeAddedListener.class,
//                                             "projectNodeAdded", event);

                String output = client.submitRequest(inputFilename);
           //     System.out.println(output);
                URL url = new URL(output);

                String filename = "C:\\" + url.getFile();

                ( (CSAlignmentResultSet) blastResult).setResultFile(filename);
                 jobFinished = true;
                 BrowserLauncher.openURL(output);

                PrintWriter bw = new PrintWriter(new FileOutputStream(filename));
                URLConnection urlCon = url.openConnection();

                StringBuffer sb = new StringBuffer("");
                String line = "";

                BufferedReader br = new BufferedReader(
                    new InputStreamReader(urlCon.getInputStream()));
                while ( (line = br.readLine()) != null) {
                    bw.println(line);
                }
                br.close();
                bw.close();

                blastResult = new CSAlignmentResultSet(filename, inputFilename, null);
              //  ( (CSAlignmentResultSet) blastResult).setAlgorithm(this);

            }

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void setSoapClient(SoapClient client) {
        soapClient = client;
    }

    public boolean isStartBrowser() {
        return startBrowser;
    }

    public void setStartBrowser(boolean startBrowser) {
        this.startBrowser = startBrowser;
    }

    public String getFileName(String path) {
        StringTokenizer st = new StringTokenizer(path, "/");
        if (st.countTokens() <= 1) {
            st = new StringTokenizer(path, "\\");
            if (st.countTokens() <= 1) {
                return path;
            }
            int k = st.countTokens();
            for (int i = 0; i < k - 1; i++) {
                st.nextToken();
            }

            String s = st.nextToken();
        //    System.out.println(s + " " + path);
            return s;

        }
        int k = st.countTokens();
        for (int i = 0; i < k - 1; i++) {
            st.nextToken();
        }

        String s = st.nextToken();

        return s;
    }

}

//public class BlastAlgorithm extends BWAbstractAlgorithm implements SoapClientIn {
//    private BlastAppComponent blastAppComponent = null;
//    private SoapClient soapClient = null;
//    private boolean startBrowser;
//    private String tempURLFolder =
//            "http://amdec-bioinfo.cu-genome.org/html/temp/";
//
//    public void setBlastAppComponent(BlastAppComponent _blastAppComponent) {
//        blastAppComponent = _blastAppComponent;
//    }
//
//    public BlastAlgorithm() {
//
//    }
//
//    public void execute() {
//        if (soapClient == null) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        if (soapClient != null) {
//            try {
//
//                //update the progressbar to ini.
//                //  ProgressBarEvent progressBarEvent =
//                // new ProgressBarEvent("Uploading...", Color.pink, 0, 0, 10);
//                // blastAppComponent.throwEvent(ProjectNodeAddedListener.class, "projectNodeAdded", progressBarEvent);
//
//                String cmd = soapClient.getCMD();
//                String textFile = "";
//                String htmlFile = null;
//
//                if (cmd.startsWith("pb")) {
//                    soapClient.startRun(true);
//                    htmlFile = ((org.geworkbench.util.session.SoapClient)
//                                soapClient).getOutputfile();
//                    if (stopRequested) {
//                        return;
//                    }
//                    if (startBrowser && !stopRequested) {
//                        if ((new File(htmlFile)).canRead()) {
//                            BrowserLauncher.openURL(tempURLFolder +
//                                    getFileName(htmlFile));
//                        } else {
//                            System.out.println("CANNOT READ " + htmlFile);
//                        }
//                    }
//                } else {
//                    soapClient.startRun();
//                    textFile = ((SoapClient) soapClient).getOutputfile();
//                }
//
//                blastAppComponent.blastFinished(cmd);
//
//                // AlignmentResultEvent event =  new AlignmentResultEvent(outputFile, null, "message", null, null);
//                // DataSet blastDataSet = new CSAlignmentResultSet(textFile);
//                // System.out.println("htmlFile = " + htmlFile + "textfile" + textFile);
//                DSAncillaryDataSet blastResult = null;
//                if (htmlFile != null) {
//                    blastResult = new CSAlignmentResultSet(htmlFile,
//                            soapClient.getInputFileName());
//                } else if (cmd.startsWith("btk search")) {
//
//                    blastResult = new SWDataSet(textFile,
//                                                soapClient.getInputFileName());
//                } else if (cmd.startsWith("btk hmm")) {
//
//                    blastResult = new HMMDataSet(textFile,
//                                                 soapClient.getInputFileName());
//                }
//
//                //add twice blastDataSet. change!@ ???
//                org.geworkbench.events.ProjectNodeAddedEvent event = new org.
//                        geworkbench.events.ProjectNodeAddedEvent("message", null,
//                        blastResult);
//                blastAppComponent.publishProjectNodeAddedEvent(event);
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//        }
//    }
//
//    public void setSoapClient(SoapClient client) {
//        soapClient = client;
//    }
//
//    public boolean isStartBrowser() {
//        return startBrowser;
//    }
//
//    public void setStartBrowser(boolean startBrowser) {
//        this.startBrowser = startBrowser;
//    }
//
//    public String getFileName(String path) {
//        StringTokenizer st = new StringTokenizer(path, "/");
//        if (st.countTokens() <= 1) {
//            st = new StringTokenizer(path, "\\");
//            if (st.countTokens() <= 1) {
//                return path;
//            }
//            int k = st.countTokens();
//            for (int i = 0; i < k - 1; i++) {
//                st.nextToken();
//            }
//
//            String s = st.nextToken();
//
//            return s;
//
//        }
//        int k = st.countTokens();
//        for (int i = 0; i < k - 1; i++) {
//            st.nextToken();
//        }
//
//        String s = st.nextToken();
//
//        return s;
//    }
//
//}