package org.geworkbench.components.alignment.client;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.StringTokenizer;

import javax.swing.JProgressBar;

import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.
        CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.
        CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.blast.RemoteBlast;
import org.geworkbench.components.alignment.panels.*;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;
import org.globus.progtutorial.clients.BlastService.Client;

/**
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author XZ
 * @version 1.0
 */
public class BlastAlgorithm extends BWAbstractAlgorithm implements SoapClientIn {
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
    private boolean useNCBI = false;
    private JProgressBar progressBar = new JProgressBar();
    private ParameterSetting parameterSetting;

    public void setBlastAppComponent(BlastAppComponent _blastAppComponent) {
        blastAppComponent = _blastAppComponent;
    }

    public BlastAlgorithm() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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
        try {
            if (soapClient == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (useNCBI) {
                String tempFolder = System.getProperties().getProperty(
                        "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";

                }
                String outputFile = tempFolder + "Blast" +
                                    RandomNumberGenerator.getID() +
                                    ".html";

                RemoteBlast blast = null;
                CSSequenceSet activeSequenceDB = soapClient.getSequenceDB();
                progressBar.setMaximum(activeSequenceDB.size());
                int count = 0;
                for (Object sequence : activeSequenceDB) {

                    blast = new RemoteBlast(((CSSequence) sequence).
                                            getSequence(), outputFile,
                                            progressBar);
                    blast.setDbName(parameterSetting.getDbName());
                    blast.setProgamName(parameterSetting.getProgramName());

                    String BLAST_rid = blast.submitBlast();
                    blast.getBlast(BLAST_rid, "HTML");
                    progressBar.setValue(++count);
                    while (!blast.getBlastDone()) {
                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {

                        }

                    }
                    if (stopRequested) {
                        return;
                    }

                }

                progressBar.setIndeterminate(false);
                progressBar.setString("NCBI Blast is finished.");
                if (parameterSetting.isViewInBrowser()) {
                    if ((new File(outputFile)).canRead()) {

                        BrowserLauncher.openURL(new File(outputFile).
                                                getAbsolutePath());
                    } else {
                        System.out.println("CANNOT READ " + outputFile);
                    }

                }
                blastResult = new CSAlignmentResultSet(
                        outputFile, activeSequenceDB.getFASTAFileName(),
                        activeSequenceDB);
                blastResult.setLabel(blastAppComponent.NCBILABEL);
                ProjectNodeAddedEvent event =
                        new ProjectNodeAddedEvent(null, null,
                                                  blastResult);
                if (blastAppComponent != null) {
                    blastAppComponent.publishProjectNodeAddedEvent(event);
                } else {
                    blastAppComponent.publishProjectNodeAddedEvent(event);
                }
                return;
            }
            if (soapClient != null) {
                String cmd = soapClient.getCMD();
                String textFile = "";
                String htmlFile = null;
                if (cmd.startsWith("pb")) {
                    soapClient.startRun(true);
                    htmlFile = ((SoapClient) soapClient).getOutputfile();
                    if (stopRequested) {
                        return;
                    }
                    if (startBrowser && !stopRequested) {
                        if ((new File(htmlFile)).canRead()) {
                            BrowserLauncher.openURL(tempURLFolder +
                                    getFileName(htmlFile));
                        } else {
                            System.out.println("CANNOT READ " + htmlFile);
                        }
                    }
                } else {
                    soapClient.startRun();
                    textFile = ((SoapClient) soapClient).getOutputfile();
                }
                if (blastAppComponent != null) {
                    blastAppComponent.blastFinished(cmd);
                } else {
                    blastAppComponent.blastFinished(cmd);
                }

                if (htmlFile != null) {
                    if (soapClient.getSequenceDB() != null &&
                        soapClient.getSequenceDB().getFASTAFileName() != null) {
                        blastResult = new CSAlignmentResultSet(htmlFile,
                                soapClient.getSequenceDB().getFASTAFileName(),
                                soapClient.getSequenceDB());
                    }
                } else if (cmd.startsWith("btk search")) {

                    blastResult = new SWDataSet(textFile,
                                                soapClient.getInputFileName(),
                                                blastAppComponent.getFastaFile());
                } else if (cmd.startsWith("btk hmm")) {

                    blastResult = new HMMDataSet(textFile,
                                                 soapClient.getInputFileName(),
                                                 blastAppComponent.getFastaFile());
                }
                ProjectNodeAddedEvent event =
                        new ProjectNodeAddedEvent(null, null,
                                                  blastResult);
                if (blastAppComponent != null) {
                    blastAppComponent.publishProjectNodeAddedEvent(event);
                } else {
                    blastAppComponent.publishProjectNodeAddedEvent(event);
                }
            }

            if (gridEnabled) {
                String tempFolder = System.getProperties().getProperty(
                        "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";
                }

                blastResult = new CSAlignmentResultSet(tempFolder + "a.html",
                        inputFilename, soapClient.getSequenceDB());
                org.geworkbench.events.ProjectNodeAddedEvent event = new org.
                        geworkbench.events.ProjectNodeAddedEvent("message", null,
                        blastResult);
                blastAppComponent.publishProjectNodeAddedEvent(event);
                String output = client.submitRequest(inputFilename);
                URL url = new URL(output);
                String filename = "C:\\" + url.getFile();
                ((CSAlignmentResultSet) blastResult).setResultFile(filename);
                jobFinished = true;
                BrowserLauncher.openURL(output);
                PrintWriter bw = new PrintWriter(new FileOutputStream(filename));
                URLConnection urlCon = url.openConnection();
                StringBuffer sb = new StringBuffer("");
                String line = "";
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(urlCon.getInputStream()));
                while ((line = br.readLine()) != null) {
                    bw.println(line);
                }
                br.close();
                bw.close();
                blastResult = new CSAlignmentResultSet(filename, inputFilename,
                        soapClient.getSequenceDB());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void setSoapClient(SoapClient client) {
        soapClient = client;
    }

    public boolean isStartBrowser() {
        return startBrowser;
    }

    public BlastAppComponent getBlastAppComponent() {
        return blastAppComponent;
    }

    public boolean isUseNCBI() {
        return useNCBI;
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public ParameterSetting getParameterSetting() {
        return parameterSetting;
    }

    public void setStartBrowser(boolean startBrowser) {
        this.startBrowser = startBrowser;
    }

//    public void setBlastAppComponent(BlastAppComponent
//                                            blastAppComponent) {
//        this.blastAppComponent = blastAppComponent;
//    }

    public void setUseNCBI(boolean useNCBI) {
        this.useNCBI = useNCBI;
    }

    public void setProgressBar(JProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setParameterSetting(ParameterSetting parameterSetting) {
        this.parameterSetting = parameterSetting;
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

    private void jbInit() throws Exception {
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
