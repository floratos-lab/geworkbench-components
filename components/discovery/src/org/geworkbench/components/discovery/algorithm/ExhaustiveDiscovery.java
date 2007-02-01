package org.geworkbench.components.discovery.algorithm;

import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.util.patterns.*;
import org.geworkbench.util.remote.SPLASHDefinition;
import org.geworkbench.util.session.DiscoverySession;
import org.geworkbench.util.session.SessionOperationException;
import polgara.soapPD_wsdl.Parameters;

import java.util.ArrayList;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: The Exahustive SPLASH discovery.
 * Note:
 * 1) We also make this "algorithm" a data source through the
 * SequentialPatternSource interface. I.e the class saves the result
 * from the server here!
 * 2) The algorithm is designed to be invoked once only. i.e. Calling start
 * more than once is a mistake. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public final class ExhaustiveDiscovery extends ServerBaseDiscovery implements SequentialPatternSource {
    //the locally cached patterns of this session.
    private ArrayList pattern = new ArrayList();

    private String statusBarMessage = "";

    //marks if a discovery was called on the server
    private boolean started = false;

    //the number of patterns that were found by the last search
    private int discoveredPattern = 0;

    //the number  of  patterns that were returned on the last call to the server
    private int lastDiscoveredPattern = 0;

    /**
     * Start an exhaustive discovery on the server.
     *
     * @param s         DiscoverySession
     * @param parameter Parameters
     */
    public ExhaustiveDiscovery(DiscoverySession s, Parameters parameter) {
        super(s, parameter);
    }

    /**
     * Reconnect to an Exhaustive algorithm on the server.
     *
     * @param s DiscoverySession
     */
    public ExhaustiveDiscovery(DiscoverySession s) {
        super(s);
    }

    protected void runAlgorithm() {
        DiscoverySession discoverySession = getSession();
        try {
            //start discovery
            discoverySession.discover(SPLASHDefinition.Algorithm.EXHAUSTIVE);
            started = true;
            pollAndUpdate();
        } catch (SessionOperationException ex) { //end try
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
    }

    protected void reconnectAlgorithm() {
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
    }

    private void fireStatusEvent() {
        DiscoverySession discoverySession = getSession();
        //only get patterns if the algorithm
        //was started on the server
        if (started) {
            try {
                discoveredPattern = discoverySession.getPatternNo();
            } catch (SessionOperationException ex) {
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
                getSession().maskPatternLocus(null, 0, 0, 0);
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
        return (DSMatchedSeqPattern) pattern.get(index);
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

    protected void statusChangedListenerAdded() {
        super.statusChangedListenerAdded();
        fireDisplayUpdate();
    }

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

         }catch (Exception e){
             e.printStackTrace();
         }
        return true;
    }
}
