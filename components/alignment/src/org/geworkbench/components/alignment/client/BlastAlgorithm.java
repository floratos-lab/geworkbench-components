package org.geworkbench.components.alignment.client;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.StringTokenizer;

import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.blast.RemoteBlast;
import org.geworkbench.components.alignment.panels.*;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;
import org.geworkbench.util.BrowserLauncher;
import org.globus.progtutorial.clients.BlastService.Client;

import javax.swing.*;

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
    private static final String TEMPURLFOLDER = "http://adparacel.cu-genome.org/examples/output/";
    //      "http://amdec-bioinfo.cu-genome.org/html/temp/";
    private boolean useNCBI = false;
    private ParameterSetting parameterSetting;
    private final static int TIMEGAP = 4000;
    private final static int SHORTTIMEGAP = 50;

    public void setBlastAppComponent(BlastAppComponent _blastAppComponent) {
        blastAppComponent = _blastAppComponent;
    }

    public BlastAlgorithm() {

    }

    /**
     * Update Progress with finished percentage and related information.
     *
     * @param percent double
     * @param text    String
     */

    void updateProgressStatus(final double percent, final String text) {
        if (blastAppComponent != null) {
            blastAppComponent.updateProgressBar(percent, text);
        }
    }

    /**
     * Update progess only with String information.
     *
     * @param text String
     */
    void updateStatus(String text) {
        if (blastAppComponent != null) {
            blastAppComponent.updateProgressBar(text);
        }
    }

    /**
     * Update the component's progressBar with information and reset the ProgressBar.
     *
     * @param boo  boolean
     * @param text String
     */

    void updateStatus(boolean boo, String text) {
        if (blastAppComponent != null) {
            blastAppComponent.updateProgressBar(boo, text);
        }
    }

    /**
     * Get the percentage of completion.
     *
     * @return double
     */
    public double getCompletion() {
        if (jobFinished) {
            //Make it bigger than 1, it means that job is done.
            return 3;
        }
        return super.getCompletion();
    }

    /**
     * The workhorse to run Blast program.
     */

    public void execute() {
        DSAncillaryDataSet blastResult = null;
        try {
            if (soapClient == null) {
                try {
                    Thread.sleep(this.SHORTTIMEGAP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //Begin process NCBI query.
            if (useNCBI) {
                //display the NCBI URL but only parse the result saved locally.
                String ncbiResultURLStr = null;
                String tempFolder = System.getProperties().getProperty(
                        "temporary.files.directory");
                if (tempFolder == null) {
                    tempFolder = ".";

                }
                //generate a new file name for the coming outputfile.
                String outputFile = tempFolder + "Blast" +
                        RandomNumberGenerator.getID() +
                        ".html";

                RemoteBlast blast;
                CSSequenceSet activeSequenceDB = soapClient.getSequenceDB();
                DSSequenceSet parentSequenceSet = soapClient.getParentSequenceDB();


                int count = 0;
                for (Object sequence : activeSequenceDB) {
                    updateStatus("Uploading sequence: " +
                            ((CSSequence) sequence));
                    blast = new RemoteBlast(((CSSequence) sequence).
                            getSequence(), outputFile);

                    blast.setCmdLine(AlgorithmMatcher.translateToCommandline(parameterSetting));
                    String BLAST_rid = blast.submitBlast();
                    if (BLAST_rid == null) {
                        if (blastAppComponent != null) {
                            blastAppComponent.reportError("Sequence " + sequence + " cannot be blasted, please check your parameters.", "Parameter Error");

                        }
                        updateStatus(false, "NCBI Blast is stopped at " + new Date());
                        return;

                    }
                    updateStatus("Querying sequence: " +
                            ((CSSequence) sequence).getDescriptions().
                                    toString());
                    updateStatus("The Request ID is : " + BLAST_rid);

                    blast.getBlast(BLAST_rid, "HTML");
                    ncbiResultURLStr = blast.getResultURLString();
                    while (!blast.getBlastDone()) {
                        try {
                            if (blastAppComponent != null && !blastAppComponent.isStopButtonPushed() && !stopRequested)
                            {
                                updateStatus("For sequence " + sequence +
                                        ", time since submission is : " +
                                        blast.getWaitingTime());
                                Thread.sleep(this.TIMEGAP);
                            } else {
                                return;
                            }
                        } catch (Exception e) {

                        }
                        updateStatus("Querying sequence: " +
                                ((CSSequence) sequence).getDescriptions().
                                        toString());

                    }
                    if (stopRequested) {
                        return;
                    }

                }
                updateStatus(false, "NCBI Blast is finisheded at " + new Date());
                String outputFilePath = "file://" + new File(outputFile).getAbsolutePath();
                if (parameterSetting.isViewInBrowser()) {
                    if ((new File(outputFile)).canRead()) {
                        try {
                            String osName = System.getProperty("os.name");
                            if (osName.startsWith("Mac OS")) {
                                BrowserLauncher.openURL(outputFilePath);
                            } else {
                                BrowserLauncher.openURL(new File(outputFile).
                                        getAbsolutePath());
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "No web browser can be launched, the result is saved at " + outputFile, "No Web Browser", JOptionPane.ERROR_MESSAGE);

                        }
                    } else {

                        JOptionPane.showMessageDialog(null, "The result cannot be read at " + outputFile, "File cannot be read", JOptionPane.ERROR_MESSAGE);

                    }


                }
                blastResult = new CSAlignmentResultSet(
                        outputFile, activeSequenceDB.getFASTAFileName(),
                        activeSequenceDB, parentSequenceSet);
                blastResult.setLabel(blastAppComponent.NCBILABEL);
                ProjectNodeAddedEvent event =
                        new ProjectNodeAddedEvent(null, null,
                                blastResult);
                if (blastAppComponent != null) {
                    blastAppComponent.publishProjectNodeAddedEvent(event);
                }
                return;
            }//end a NCBI query.
            if (soapClient != null) {
                String cmd = soapClient.getCmd();
                String textFile = "";
                String htmlFile = null;

                try {
                    if (cmd.startsWith("pb")) {

                        if (!soapClient.startRun(true)) {
                            //fail to connect or other problem.
                            blastAppComponent.reportError(blastAppComponent.ERROR2, "Server unreachable");
                            blastAppComponent.setBlastDisplayPanel(blastAppComponent.SERVER);
                            blastAppComponent.blastFinished(blastAppComponent.ERROR1);
                            return;
                        }
                        htmlFile = ((SoapClient) soapClient).getOutputfile();
                        if (stopRequested) {
                            return;
                        }
                        if (startBrowser && !stopRequested) {
                            if ((new File(htmlFile)).canRead()) {
                                BrowserLauncher.openURL(TEMPURLFOLDER +
                                        getFileName(htmlFile));
                            } else {
                                System.out.println("CANNOT READ " + htmlFile);
                            }
                        }

                    } else {
                        soapClient.startRun();
                        textFile = ((SoapClient) soapClient).getOutputfile();
                    }
                } catch (Exception exce) {
                    blastAppComponent.reportError(blastAppComponent.ERROR2, "Server unreachable");

                }
                if (blastAppComponent != null) {
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
//Handle grid situation.
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


    public ParameterSetting getParameterSetting() {
        return parameterSetting;
    }

    public boolean isJobFinished() {
        return jobFinished;
    }

    public void setStartBrowser(boolean startBrowser) {
        this.startBrowser = startBrowser;
    }


    public void setUseNCBI(boolean useNCBI) {
        this.useNCBI = useNCBI;
    }

    public void setParameterSetting(ParameterSetting parameterSetting) {
        this.parameterSetting = parameterSetting;
    }

    public void setJobFinished(boolean jobFinished) {
        this.jobFinished = jobFinished;
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

