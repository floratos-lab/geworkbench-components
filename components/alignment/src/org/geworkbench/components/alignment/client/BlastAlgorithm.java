package org.geworkbench.components.alignment.client;

import org.geworkbench.algorithms.BWAbstractAlgorithm;
import org.geworkbench.components.alignment.panels.BlastAppComponent;
import org.geworkbench.components.alignment.panels.BrowserLauncher;
import org.geworkbench.events.ProjectNodeAddedEvent;
import org.geworkbench.bison.datastructure.biocollections.DSAncillaryDataSet;
import org.geworkbench.util.session.*;
import org.geworkbench.util.session.SoapClient;

import java.io.File;
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

public class BlastAlgorithm extends BWAbstractAlgorithm implements SoapClientIn {
    private BlastAppComponent blastAppComponent = null;
    private SoapClient soapClient = null;
    private boolean startBrowser;
    private String tempURLFolder =
            "http://amdec-bioinfo.cu-genome.org/html/temp/";

    public void setBlastAppComponent(BlastAppComponent _blastAppComponent) {
        blastAppComponent = _blastAppComponent;
    }

    public BlastAlgorithm() {

    }

    public void execute() {
        if (soapClient == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (soapClient != null) {
            try {

                //update the progressbar to ini.
                //  ProgressBarEvent progressBarEvent =
                // new ProgressBarEvent("Uploading...", Color.pink, 0, 0, 10);
                // blastAppComponent.throwEvent(ProjectNodeAddedListener.class, "projectNodeAdded", progressBarEvent);

                String cmd = soapClient.getCMD();
                String textFile = "";
                String htmlFile = null;

                if (cmd.startsWith("pb")) {
                    soapClient.startRun(true);
                    htmlFile = ((org.geworkbench.util.session.SoapClient)
                                soapClient).getOutputfile();
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

                blastAppComponent.blastFinished(cmd);

                // AlignmentResultEvent event =  new AlignmentResultEvent(outputFile, null, "message", null, null);
                // DataSet blastDataSet = new BlastDataSet(textFile);
                // System.out.println("htmlFile = " + htmlFile + "textfile" + textFile);
                DSAncillaryDataSet blastResult = null;
                if (htmlFile != null) {
                    blastResult = new BlastDataSet(htmlFile,
                            soapClient.getInputFileName());
                } else if (cmd.startsWith("btk search")) {

                    blastResult = new SWDataSet(textFile,
                                                soapClient.getInputFileName());
                } else if (cmd.startsWith("btk hmm")) {

                    blastResult = new HMMDataSet(textFile,
                                                 soapClient.getInputFileName());
                }

                //add twice blastDataSet. change!@ ???
                org.geworkbench.events.ProjectNodeAddedEvent event = new org.
                        geworkbench.events.ProjectNodeAddedEvent("message", null,
                        blastResult);
                blastAppComponent.publishProjectNodeAddedEvent(event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

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
