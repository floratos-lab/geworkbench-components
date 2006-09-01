/*
 * BlatAlgorithm.java
 *
 * Created on August 1, 2006, 5:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.geworkbench.components.alignment.client;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.ArrayList;

import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.bison.datastructure.biocollections.sequences.CSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSAlignmentResultSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.CSSequence;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.alignment.blast.RemoteBlat;
import org.geworkbench.components.alignment.panels.*;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.util.session.SoapClient;
import org.globus.progtutorial.clients.BlastService.Client;
import org.geworkbench.components.alignment.blast.BlatOutput;

/**
 *
 * @author avv2101
 */

public class BlatAlgorithm extends BWAbstractAlgorithm implements SoapClientIn {
    /**
     * BlatAlgorithm
     *
     * @param aBoolean boolean
     */
    public BlatAlgorithm(boolean aBoolean, String inputFile, Client _client) {
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
    private static final String TEMPURLFOLDER =
            "http://amdec-bioinfo.cu-genome.org/html/temp/";
    //private boolean useNCBI = false;
    private ParameterBlatSetting parameterBlatSetting;
    private final static int TIMEGAP = 4000;
    private final static int SHORTTIMEGAP = 50;

    public void setBlastAppComponent(BlastAppComponent blastAppComponent) {
        this.blastAppComponent = blastAppComponent;
    }

    public BlatAlgorithm() {

    }

    /**
     * Update Progress with finished percentage and related information.
     * @param percent double
     * @param text String
     */

    void updateProgressStatus(final double percent, final String text) {
        if (blastAppComponent != null) {
            blastAppComponent.updateProgressBarBlat(percent, text);
        }
    }

    /**
     * Update progess only with String information.
     * @param text String
     */
    void updateStatus(String text) {
        if (blastAppComponent != null) {
            blastAppComponent.updateProgressBarBlat(text);
        }
    }

    public void execute() {
        DSAncillaryDataSet blatResult = null;
        try {
            if (soapClient == null) {
                try {
                    Thread.sleep(this.SHORTTIMEGAP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            String tempFolder = System.getProperties().getProperty("temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";
            }
            //generate a new file name for the coming outputfile.
            String outputFile = tempFolder + "Blat" + RandomNumberGenerator.getID() +".html";
            //generate a structure to get the output
            ArrayList outputStruct = new ArrayList(100);
            RemoteBlat blat = null;
            CSSequenceSet activeSequenceDB = soapClient.getSequenceDB();
            int count = 0;
            for (Object sequence : activeSequenceDB) {
                updateStatus("Uploading sequence: " + ((CSSequence) sequence));       
                blat = new RemoteBlat(((CSSequence) sequence).getSequence(), ((CSSequence) sequence).toString(), outputFile, outputStruct);
                blat.setCmdLine(parameterBlatSetting);
                if (parameterBlatSetting.getBooleanFeelLucky())
                    blat.setStringFeelLucky("I'm feeling lucky");
                String message = blat.submitBlat();
                if(message==null){
                    if(blastAppComponent!=null){
                        blastAppComponent.reportError("please check your parameters.", "Parameter Error");
                        }
                    updateStatus(false, "Web Blat is stopped at " + new Date());
                    return;
                }
                updateStatus("Querying sequence: " + ((CSSequence) sequence).getDescriptions().toString());
                blat.getBlat(message);          
                while (!blat.getBlatDone()){
                    try {
                        if(blastAppComponent!=null && !blastAppComponent.isStopButtonPushed()){
                            updateStatus("For sequence " + sequence +", time since submission is : " +
                                             blat.getWaitingTime());
                            Thread.sleep(this.TIMEGAP);
                        }else{
                            return;
                        }
                    }catch (Exception e) {
                    }
                    updateStatus("Querying sequence: " + ((CSSequence) sequence).getDescriptions().toString());
                    }
                if (stopRequested){
                    return;
                }
            }
            updateStatus(false, "Web Blat is finisheded at " + new Date());

            if (parameterBlatSetting.getBooleanOpenInBrowser()) {
                if ((new File(outputFile)).canRead()) {
                    BrowserLauncher.openURL(new File(outputFile).getAbsolutePath());
                } else {
                    System.out.println("CANNOT READ " + outputFile);
                }
            }
            //will need to reimplement this code to show up the result
            /*blatResult = new CSAlignmentResultSet(outputFile, activeSequenceDB.getFASTAFileName(),
                                                  activeSequenceDB);
            blatResult.setLabel(blastAppComponent.NCBILABEL);
            ProjectNodeAddedEvent event = new ProjectNodeAddedEvent(null, null,blatResult);
            if (blastAppComponent != null) {
                blastAppComponent.publishProjectNodeAddedEvent(event);
            }*/
            return;
            //Handle grid situation.
        /*if (gridEnabled) {
            tempFolder = System.getProperties().getProperty("temporary.files.directory");
            if (tempFolder == null) {
                tempFolder = ".";
            }
            blatResult = new CSAlignmentResultSet(tempFolder + "a.html",inputFilename, soapClient.getSequenceDB());
            org.geworkbench.events.ProjectNodeAddedEvent event = new org.
            geworkbench.events.ProjectNodeAddedEvent("message", null,blatResult);
            blastAppComponent.publishProjectNodeAddedEvent(event);
            String output = client.submitRequest(inputFilename);
            URL url = new URL(output);
            String filename = "C:\\" + url.getFile();
            ((CSAlignmentResultSet) blatResult).setResultFile(filename);
            jobFinished = true;
            BrowserLauncher.openURL(output);
            PrintWriter bw = new PrintWriter(new FileOutputStream(filename));
            URLConnection urlCon = url.openConnection();
            StringBuffer sb = new StringBuffer("");
            String line = "";
            BufferedReader br = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
            while ((line = br.readLine()) != null) {
                bw.println(line);
            }
            br.close();
            bw.close();
            blatResult = new CSAlignmentResultSet(filename, inputFilename,soapClient.getSequenceDB());
            }*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Update the component's progressBar with information and reset the ProgressBar.
     * @param boo boolean
     * @param text String
     */
    void updateStatus(boolean boo, String text) {
        if (blastAppComponent != null) {
            blastAppComponent.updateProgressBarBlat(boo, text);
        }
    }

    /**
     * Get the percentage of completion.
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
     *
     */

    

    public void setSoapClient(SoapClient client) {
        soapClient = client;
    }

    public boolean isStartBrowser() {
        return startBrowser;
    }

    public BlastAppComponent getBlastAppComponent() {
        return blastAppComponent;
    }

    public ParameterBlatSetting getParameterBlatSetting() {
        return parameterBlatSetting;
    }

    public boolean isJobFinished() {
        return jobFinished;
    }

    public void setStartBrowser(boolean startBrowser) {
        this.startBrowser = startBrowser;
    }

    public void setParameterBlatSetting(ParameterBlatSetting parameterBlatSetting) {
        this.parameterBlatSetting = parameterBlatSetting;
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
