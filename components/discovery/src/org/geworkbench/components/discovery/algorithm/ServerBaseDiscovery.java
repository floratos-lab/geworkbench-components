package org.geworkbench.components.discovery.algorithm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.components.discovery.SequenceDiscoveryViewWidget;
import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternDB;
import org.geworkbench.util.patterns.PatternFetchException;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.patterns.SequentialPatternSource;
import org.geworkbench.util.session.DiscoverySession;
import org.geworkbench.util.session.SessionOperationException;

import polgara.soapPD_wsdl.Parameters;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: This class is the base class for SPLASH server base
 * algorithm. It factorizes some commonalities.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version $Id$
 */

public final class ServerBaseDiscovery extends
		AbstractSequenceDiscoveryAlgorithm implements SequentialPatternSource {
    //the discoverySession to connect to.
    private DiscoverySession discoverySession;
    //initial parameters for the search
    private Parameters parms;
    //Status event
    private org.geworkbench.events.StatusBarEvent statusEvent = new StatusBarEvent();
    private boolean algorithmStop = false;
    protected boolean reconnect = false;

    //Did the algorithm complete?
    protected boolean done = false;

    //Event for updating the progress bar
    private ProgressBarEvent progressBarEvent = new ProgressBarEvent("", Color.gray, 0, 0, 100);

    /**
     * This constructor will start the running of algorithm by calling runAlogrithm.
     *
     * @param discoverySession the object to run queries to the server.
     */
    public ServerBaseDiscovery(DiscoverySession discoverySession, Parameters parm,
    		String algorithmType) {
        initSession(discoverySession);
        if (parm == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [parm=null]");
        }
        this.parms = parm;
        this.algorithmType = algorithmType;
    }

    // TODO not used anywhere. maybe we should remove this constructor.
    /**
     * This constructor will force a reconnection to an existing running algorithm
     * on the server. It will reconnectAlgorithm.
     *
     * @param discoverySession DiscoverySession
     */
    public ServerBaseDiscovery(DiscoverySession discoverySession) {
        initSession(discoverySession);
        this.reconnect = true;
    }

    private void initSession(DiscoverySession discoverySession) {
        if (discoverySession == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [discoverySession=null]");
        }
        this.discoverySession = discoverySession;
    }

    /**
     * Start the algorithm. The first thing we do is upload sequences
     * to the server. We let subclasses implement the actual algorithm.
     */
    @Override
    public void start() {
        try {
//Temp change for bug 329.
             if (discoverySession.isDone() && (!reconnect)) {
                // if (!discoverySession.loadSequenceRemote()) {
                    //upload sequences
                    fireStatusBarEvent("Uploading....");
                    if (!upload()) {
                        return;
                    }
                    discoverySession.saveSeqDB();
                // }

                discoverySession.setParameters(parms);
                fireStatusBarEvent("Discovering...");
                runAlgorithm();
            } else {
                //reconnection code
                reconnectAlgorithm();
            }

        } //end try
        catch (SessionOperationException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }

    /**
     * fires a progress change event.
     */
    protected void fireProgressBarEvent() {
        DiscoverySession discoverySession = getSession();
        try {
            double percent = discoverySession.getCompletion();
            int percentAsInt = (int) (percent * 100);
            progressBarEvent.setPercentDone(percentAsInt);
//            System.out.println("ProgessBar at " + percentAsInt);
            if (!done && !isStop()) {
                if (percentAsInt < 50) {
                    progressBarEvent.setMessage("Processing Seeds");
                } else if (percentAsInt < 99) {
                    progressBarEvent.setMessage("Discovering");
                } else {
                    progressBarEvent.setMessage("Collating");
                }
            } else {
                progressBarEvent.setMessage("Done");
                progressBarEvent.setPercentDone(100);
            }

            fireProgressBarChanged(progressBarEvent);
        } catch (SessionOperationException exp) {
            System.out.println(exp);
        }
    }

    public synchronized void stop() {
        algorithmStop = true;
        try {
            discoverySession.stop();
        } catch (SessionOperationException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }

    /**
     * The method return true if a stop was called on this object.
     *
     * @return true if and only if stop was called on this object.
     */
    public synchronized boolean isStop() {
        return algorithmStop;
    }

    /**
     * Get the discoverySession of this algorithm.
     *
     * @return discoverySession the discoverySession object.
     */
    protected DiscoverySession getSession() {
        return discoverySession;
    }

    /**
     * Uploads the sequence in the discoverySession to the server.
     *
     * @param s
     */
    private boolean upload() {
        final int databaseSize = discoverySession.getSequenceDB().getSequenceNo();
        ProgressBarEvent progressBarEvent = new ProgressBarEvent("Uploading...", Color.pink, 0, 0, databaseSize);
        fireProgressBarChanged(progressBarEvent);
        for (int i = 0; i < databaseSize; ++i) {
            //check that we were not stopped.
            if (isStop()) {
                return false;
            }
            upload(i);
            fireProgressBarEvent(progressBarEvent, i);
        }
        return true;
    }

    private void upload(int i) {
        try {
            discoverySession.upload(i);
        } catch (SessionOperationException exp) {
            System.out.println("DiscoverySession operation exception");
        }

    }

    private void fireProgressBarEvent(ProgressBarEvent evt, int percent) {
        evt.setPercentDone(percent);
        fireProgressBarChanged(evt);
    }

    /**
     * A helper method for firing a status bar event.
     *
     * @param s the message to display on the status bar.
     */
    protected void fireStatusBarEvent(String s) {
        statusEvent.setStatus(s);
        fireStatusBarChanged(statusEvent);
    }

    /*
     * The following part is refactored from original RegularDicovery and ExhaustiveDsicovery,
     * which are basically the same code except for some trivial differences.
     * The code here is more based on ExhaustiveDsicovery.
     */

    //the locally cached patterns of this session.
    private List<DSMatchedSeqPattern> pattern = new ArrayList<DSMatchedSeqPattern>();

    private String statusBarMessage = "";

    //marks if a discovery was called on the server
    private boolean started = false;

    //the number of patterns that were found by the last search
    private int discoveredPattern = 0;

    //the number  of  patterns that were returned on the last call to the server
    private int lastDiscoveredPattern = 0;

    void runAlgorithm() {
        DiscoverySession discoverySession = getSession();
        try {
            //start discovery
            discoverySession.discover(algorithmType);
            started = true;
            pollAndUpdate();
        } catch (SessionOperationException ex) { //end try
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }

    void reconnectAlgorithm() {
        started = true;
        pollAndUpdate();
    }

    /**
     * The method polls the server for the status of the discovery.
     * If no listeners are listening for updates we suspend the polling
     * with the "tryWait()"
     */
    private void pollAndUpdate() {
        DiscoverySession discoverySession = getSession();
        try {
            while (!done && !isStop()) {
                Thread.sleep(100);
                fireDisplayUpdate();
                done = discoverySession.isDone();
                tryWait();
            }
            writeToResultfile();
        } catch (SessionOperationException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
        fireDisplayUpdate();
		int viewId = SequenceDiscoveryViewWidget.DEFAULT_VIEW;
        if (discoveredPattern == 0){
			JOptionPane.showMessageDialog(null, "No patterns were found");
		} else {
			viewWidget.firePropertyChangeAlgo();
			viewId = SequenceDiscoveryViewWidget.PATTERN_TABLE;
		}
		// replace the view and model
		viewWidget.setCurrentView(viewId);
    }

    private void fireStatusEvent() {
        DiscoverySession discoverySession = getSession();
        //only get patterns if the algorithm
        //was started on the server
        if (started) {
            try {
                discoveredPattern = discoverySession.getPatternNo();
            } catch (SessionOperationException ex) {
            	ex.printStackTrace();
            }
        }

        statusBarMessage = "Pattern/s found: " + discoveredPattern;
        if (isStop()) {
            statusBarMessage += " (Algorithm was stopped).";
        }

        if ((discoveredPattern > lastDiscoveredPattern)) {
            lastDiscoveredPattern = discoveredPattern;
            fireProgressChanged(new ProgressChangeEvent(discoveredPattern));
        }

        fireStatusBarEvent(statusBarMessage);
    }

    /**
     * fire updates.
     */
    private void fireDisplayUpdate() {
        fireStatusEvent();
        fireProgressBarEvent();
    }

    /**
     * Mask the patterns of this model
     *
     * @param indeces to mask.
     * @param mask    operation
     */
    public void mask(int[] index, boolean maskOperation) {
        //note: currently the server does not support correctly masking
        //of all patterns. Hence the maskOperation is not used
        try {
            if (index == null) { //unmask all patterns
                getSession().unmask();
            } else {
                for (int i = 0; i < index.length; i++) {
                    getSession().maskPattern(index[i], 1);
                }
            }
        } catch (SessionOperationException ex) {
            System.out.println("DiscoverySession operationException at mask");
        }

    }

    public void sort(int i) {
        try {
            //clear the locally cached patterns
            pattern.clear();
            getSession().sortPatterns(i);
        } catch (SessionOperationException ex) {
            System.out.println("DiscoverySession operationException at Sort");
        }
    }

    /**
     * As specified by SequentialPatternSource.
     */
    public synchronized DSMatchedSeqPattern getPattern(int index) {
        DiscoverySession discoverySession = getSession();
        if (index >= pattern.size() || pattern.get(index) == null) {
            CSMatchedSeqPattern pat = new org.geworkbench.util.patterns.CSMatchedSeqPattern(discoverySession.getSequenceDB());
            try {
                discoverySession.getPattern(index, pat);
            } catch (SessionOperationException ext) {
                throw new PatternFetchException(ext.getMessage());
            }
            while (pattern.size() < index) {
                pattern.add(null);
            }
            org.geworkbench.util.patterns.PatternOperations.fill(pat, discoverySession.getSequenceDB());
            pattern.add(index, pat);
        }
        return pattern.get(index);
    }

    /**
     * As specified by SequentialPatternSource.
     */
    public synchronized int getPatternSourceSize() {
        DiscoverySession discoverySession = getSession();
        if (!started) {
            //we have not started the discovery...
            return 0;
        }
        try {
            discoveredPattern = discoverySession.getPatternNo();
        } catch (SessionOperationException exp) {
        }
        return discoveredPattern;
    }

    @Override
    protected void statusChangedListenerAdded() {
        super.statusChangedListenerAdded();
        fireDisplayUpdate();
    }

    @Override
    protected void progressChangeListenerAdded() {
        super.progressChangeListenerAdded();
        fireDisplayUpdate();

    }
    
    public boolean writeToResultfile(){
         try{
        DiscoverySession session = getSession();
        org.geworkbench.util.patterns.PatternDB patternDB = new PatternDB(sequenceInputData.getFile(), null);
        int totalPatternNum = session.getPatternNo();
        for (int i = 0; i <totalPatternNum; i++) {
            DSMatchedSeqPattern pattern = getPattern(i);
            PatternOperations.fill(pattern, sequenceInputData);
            patternDB.add(pattern);
        }
        //patternDB.setParameters(widget.getParameters());
        patternDB.write(resultFile);
        result.setPatternDB(patternDB);

         }catch (Exception e){
             e.printStackTrace();
         }
        return true;
    }
    
    private String algorithmType = null;
}
