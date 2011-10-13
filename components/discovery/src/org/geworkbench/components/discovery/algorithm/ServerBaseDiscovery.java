package org.geworkbench.components.discovery.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.components.discovery.PatternDiscoveryAnalysis;
import org.geworkbench.components.discovery.session.DiscoverySession;
import org.geworkbench.components.discovery.session.SessionOperationException;
import org.geworkbench.util.patterns.CSMatchedSeqPattern;
import org.geworkbench.util.patterns.PatternFetchException;
import org.geworkbench.util.patterns.PatternOperations;
import org.geworkbench.util.patterns.SequentialPatternSource;

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

public final class ServerBaseDiscovery implements SequentialPatternSource {
    //the discoverySession to connect to.
    final private DiscoverySession discoverySession;
    //initial parameters for the search
    final private Parameters parms;
    final private PatternResult result;
    final private DSSequenceSet<? extends DSSequence> sequenceInputData;
    final private String algorithmType;
    private volatile boolean algorithmStop = false;
    //Did the algorithm complete?
    private volatile boolean done = false;

    /**
     * This constructor will start the running of algorithm by calling runAlogrithm.
     *
     * @param discoverySession the object to run queries to the server.
     */
    public ServerBaseDiscovery(DiscoverySession discoverySession, Parameters parm,
    		String algorithmType, PatternDiscoveryAnalysis dataSet, PatternResult patternResult) {
        if (discoverySession == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [discoverySession=null]");
        }
        this.discoverySession = discoverySession;
        if (parm == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [parm=null]");
        }
        this.parms = parm;
        this.algorithmType = algorithmType;
		sequenceInputData = dataSet.getSequenceDB();
		System.out.println(sequenceInputData.getSequenceNo());
		this.result = patternResult;
    }

    /**
     * Start the algorithm. The first thing we do is upload sequences
     * to the server. We let subclasses implement the actual algorithm.
     */
    public void start() {

        try {
             if (discoverySession.isDone()) {
                    //upload Sequences
                    if (!upload()) {
                        return;
                    }

                    discoverySession.saveSeqDB();
                discoverySession.setParameters(parms);
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
     * Uploads the sequence in the discoverySession to the server.
     *
     * @param s
     */
    private boolean upload() {
        final int databaseSize = discoverySession.getSequenceDB().getSequenceNo();
        for (int i = 0; i < databaseSize; ++i) {
            //check that we were not stopped.
			if (algorithmStop) {
				return false;
			}
            upload(i);
        }
        return true;
    }

    private void upload(int i) {
    	System.out.print(i);
        try {
            discoverySession.upload(i);
        } catch (SessionOperationException exp) {
            System.out.println("DiscoverySession operation exception");
        }

    }

    /*
     * The following part is refactored from original RegularDicovery and ExhaustiveDsicovery,
     * which are basically the same code except for some trivial differences.
     * The code here is more based on ExhaustiveDsicovery.
     */

    //the locally cached patterns of this session.
    private List<DSMatchedSeqPattern> pattern = new ArrayList<DSMatchedSeqPattern>();

    //marks if a discovery was called on the server
    private boolean started = false;

    //the number of patterns that were found by the last search
    private int discoveredPattern = 0;

    void runAlgorithm() {

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
     * 
     */
	// only in background thread
    private void pollAndUpdate() {

        try {
            while (!done) {
                Thread.sleep(100);
                done = discoverySession.isDone();
            }
            updateResult();
        } catch (SessionOperationException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
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
            	discoverySession.unmask();
            } else {
                for (int i = 0; i < index.length; i++) {
                	discoverySession.maskPattern(index[i], 1);
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
            discoverySession.sortPatterns(i);
        } catch (SessionOperationException ex) {
            System.out.println("DiscoverySession operationException at Sort");
        }
    }

    /**
     * As specified by SequentialPatternSource.
     */
    public synchronized DSMatchedSeqPattern getPattern(int index) {
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
        if (!started) {
            return 0;
        }
        try {
            discoveredPattern = discoverySession.getPatternNo();
        } catch (SessionOperationException exp) {
        }
        return discoveredPattern;
    }

	private void updateResult() {
		int totalPatternNum;
		try {
			totalPatternNum = discoverySession.getPatternNo();
			for (int i = 0; i < totalPatternNum; i++) {
				DSMatchedSeqPattern pattern = getPattern(i);
				PatternOperations.fill(pattern, sequenceInputData);
				result.add(pattern);
			}
		} catch (SessionOperationException e) {
			e.printStackTrace();
		}
	}
}
