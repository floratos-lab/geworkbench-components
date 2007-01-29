package org.geworkbench.components.discovery.algorithm;

import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.util.patterns.PatternFetchException;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.remote.SPLASHDefinition;
import org.geworkbench.util.session.DiscoverySession;
import org.geworkbench.util.session.SessionOperationException;
import polgara.soapPD_wsdl.Parameters;

import java.util.ArrayList;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: The plain vanilla SPLASH discovery.
 * Note:
 * 1) We also make this "algorithm" a data source through the
 * SequentialPatternSource interface. I.e the class saves the result
 * of its own data tranformation.
 * 2) The algorithm is designed to be invoked once only. i.e. Calling start
 * more than once is a mistake. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public final class RegularDiscovery extends ServerBaseDiscovery implements org.geworkbench.util.patterns.SequentialPatternSource {
    //the locally cached patterns of this session.
    private ArrayList pattern = new ArrayList();

    private String statusBarMessage = "";

    //marks if a discovery was called on the server
    private boolean started = false;

    //the number of patterns that were found by the last search
    private int discoveredPattern = 0;

    /**
     * Start a new algorithm.
     *
     * @param s         DiscoverySession
     * @param parameter Parameters
     */
    public RegularDiscovery(DiscoverySession s, Parameters parameter) {
        super(s, parameter);
    }

    /**
     * Reconnect to an already started algorithm.
     *
     * @param s DiscoverySession
     */
    public RegularDiscovery(DiscoverySession s) {
        super(s);
    }

    protected void runAlgorithm() {
        DiscoverySession discoverySession = getSession();
        try {
            //start discovery
            discoverySession.discover(SPLASHDefinition.Algorithm.REGULAR);
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
                update();
                done = discoverySession.isDone();
                tryWait();
            }
        } catch (SessionOperationException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }

        //do last update
        update();
        //fire status change
        fireStatusEvent();
    }

    /**
     * fire updates
     */
    private void update() {
        fireProgressBarEvent();
        fireStatusBarEvent();
    }

    private void fireStatusBarEvent() {
        DiscoverySession discoverySession = getSession();

        try {
            discoveredPattern = discoverySession.getPatternNo();
            statusBarMessage = "Pattern/s found: " + discoveredPattern;
        } catch (SessionOperationException ex) {
        }

        if (isStop()) {
            statusBarMessage += " (Algorithm was stopped).";
        }

        fireStatusBarEvent(statusBarMessage);
    }

    /**
     * Report the number of patterns through a status event.
     */
    private void fireStatusEvent() {
        fireProgressChanged(new ProgressChangeEvent(discoveredPattern));
    }

    /**
     * Mask the patterns of this model
     *
     * @param indeces to mask. if index in null all sequences will be unmasked
     * @param mask    operation
     */
    public void mask(int[] index, boolean maskOperation) {

        try {
            if (index == null) { //unmask all patterns
                getSession().unmask();
            } else {
                for (int i = 0; i < index.length; i++) {
                    getSession().maskPattern(index[i], 1);
                }
            }
        } catch (SessionOperationException ex) {
            System.out.println("DiscoverySession operationException at mask.");
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
            org.geworkbench.util.patterns.CSMatchedSeqPattern pat = new org.geworkbench.util.patterns.CSMatchedSeqPattern(discoverySession.getSequenceDB());
            try {
                discoverySession.getPattern(index, pat);
            } catch (SessionOperationException ext) {
                throw new PatternFetchException(ext.getMessage());
            }
            while (pattern.size() < index) {
                pattern.add(null);
            }
            PatternOperations.fill(pat, discoverySession.getSequenceDB());
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
        fireStatusEvent();
        fireStatusBarEvent(statusBarMessage);
    }

    protected void progressChangeListenerAdded() {
        super.progressChangeListenerAdded();
        fireProgressBarEvent();
    }
}
