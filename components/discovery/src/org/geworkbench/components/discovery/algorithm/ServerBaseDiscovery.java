package org.geworkbench.components.discovery.algorithm;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.datastructure.complex.pattern.sequence.DSMatchedSeqPattern;
import org.geworkbench.components.discovery.SequenceDiscoveryViewWidget;
import org.geworkbench.components.discovery.session.DiscoverySession;
import org.geworkbench.components.discovery.session.SessionOperationException;
import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.ProgressChangeListener;
import org.geworkbench.events.listeners.StatusChangeListener;
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
    //Status event
    private org.geworkbench.events.StatusBarEvent statusEvent = new StatusBarEvent();
    private volatile boolean algorithmStop = false;

    //Did the algorithm complete?
    private volatile boolean done = false;

    //Event for updating the progress bar
    private ProgressBarEvent progressBarEvent = new ProgressBarEvent("", Color.gray, 0, 0, 100);

    /**
     * This constructor will start the running of algorithm by calling runAlogrithm.
     *
     * @param discoverySession the object to run queries to the server.
     */
    public ServerBaseDiscovery(DiscoverySession discoverySession, Parameters parm,
    		String algorithmType, SequenceDiscoveryViewWidget viewWidget, PatternResult patternResult) {
        if (discoverySession == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [discoverySession=null]");
        }
        this.discoverySession = discoverySession;
        if (parm == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [parm=null]");
        }
        this.parms = parm;
        this.algorithmType = algorithmType;
		this.viewWidget = viewWidget;
		sequenceInputData = viewWidget.getSequenceDB();
		this.result = patternResult;
    }

    /**
     * Start the algorithm. The first thing we do is upload sequences
     * to the server. We let subclasses implement the actual algorithm.
     */
    public void start() {
        try {
             if (discoverySession.isDone()) {
                    //upload sequences
                    fireStatusBarEvent("Uploading....");
                    if (!upload()) {
                        return;
                    }
                    discoverySession.saveSeqDB();

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
    private void fireProgressBarEvent() {
        try {
            double percent = discoverySession.getCompletion();
            int percentAsInt = (int) (percent * 100);
            progressBarEvent.setPercentDone(percentAsInt);

            if (!done && !algorithmStop) {
                if (percentAsInt < 50) {
                    progressBarEvent.setMessage("Processing Seeds");
                } else if (percentAsInt < 99) {
                    progressBarEvent.setMessage("Discovering");
                } else {
                    progressBarEvent.setMessage("Collating");
                }
            } else {
				if (algorithmStop) {
					progressBarEvent.setMessage("Canceled by user");
				} else {
					progressBarEvent.setMessage("Done");
				}
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
			if (algorithmStop) {
				progressBarEvent = new ProgressBarEvent("Canceled by user",
						Color.black, 0, 0, databaseSize);
				fireProgressBarEvent(progressBarEvent,i);
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
	// only in background thread
    private void pollAndUpdate() {
        try {
            while (!done && !algorithmStop) {
                Thread.sleep(100);
                fireDisplayUpdate();
                done = discoverySession.isDone();
                tryWait();
            }
            if(algorithmStop){
            	return;
            }

            updateResult();
        } catch (SessionOperationException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            System.out.println(ex.toString());
            ex.printStackTrace();
        }
        fireDisplayUpdate();
        if (discoveredPattern == 0){
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, "No patterns were found");
					viewWidget.clearTableView();
				}
				
			});
		} else {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					viewWidget.firePropertyChangeAlgo();
				}
				
			});
		}
    }

    private void fireStatusEvent() {
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
        if (algorithmStop) {
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
            //we have not started the discovery...
            return 0;
        }
        try {
            discoveredPattern = discoverySession.getPatternNo();
        } catch (SessionOperationException exp) {
        }
        return discoveredPattern;
    }

    /**
     * This method is called every time a StatusChangeListener is added
     * to this algorithm. The default  behavior of this method is unblock the
     * algorithm if it was blocked before.
     */
    protected void statusChangedListenerAdded() {
        wakeUp();
        fireDisplayUpdate();
    }

    /**
     * This method is called every time a ProgressChangeListener is added
     * to this algorithm. The default behavior of this method is unblock the
     * algorithm if it was blocked before.
     */
    protected void progressChangeListenerAdded() {
    	wakeUp();
        fireDisplayUpdate();

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

    final private String algorithmType;
    
    /**
     * List of listeners
     */
    private EventListenerList listenerList = new EventListenerList();

    final private PatternResult result;
    final private DSSequenceSet<? extends DSSequence> sequenceInputData;

    // SequenceDiscoveryViewWidget which created this stub
    final private SequenceDiscoveryViewWidget viewWidget;

    /**
     * Adds a listener to the list that's notified each time a change
     * to the progress of the algorithm occurs.
     *
     * @param	listener		the ProgressChangeListener
     */
    public void addProgressChangeListener(ProgressChangeListener listener) {
        if (listener instanceof ProgressChangeListener) {
            addListener(ProgressChangeListener.class, listener);
            progressChangeListenerAdded();
        }
    }

    /**
     * Removes a listener from the list.
     *
     * @param	listener		the ProgressChangeListener
     */
    public void removeProgressChangeListener(ProgressChangeListener listener) {
        if (listener instanceof ProgressChangeListener) {
            removeListener(ProgressChangeListener.class, listener);
            wakeUp();
        }
    }

    /**
     * Adds a listener for status changes.
     *
     * @param	listener		the StatusChangeListener
     */
    public void addStatusChangeListener(StatusChangeListener listener) {
        if (listener instanceof StatusChangeListener) {
            addListener(StatusChangeListener.class, listener);
            statusChangedListenerAdded();
        }
    }

    /**
     * Removes a listener from the list.
     *
     * @param	listener		the StatusChangeListener
     */
    public void removeStatusChangeListener(StatusChangeListener listener) {
        if (listener instanceof StatusChangeListener) {
            removeListener(StatusChangeListener.class, listener);
        }
    }

    /**
     * Forwards the given notification event to all
     * <code>ProgressChangeListeners</code> that registered
     * themselves as listeners.
     *
     * @param e the event to be forwarded
     * @see ProgressChangeEvent
     */
    private void fireProgressChanged(ProgressChangeEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ProgressChangeListener.class) {
                ((ProgressChangeListener) listeners[i + 1]).progressChanged(e);
            }
        }
    }

    private void fireProgressBarChanged(org.geworkbench.events.ProgressBarEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == StatusChangeListener.class) {
                ((StatusChangeListener) listeners[i + 1]).progressBarChanged(e);
            }
        }
    }

    private void fireStatusBarChanged(StatusBarEvent e) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == StatusChangeListener.class) {
                ((StatusChangeListener) listeners[i + 1]).statusBarChanged(e);
            }
        }
    }

    /**
     * This method blocks if no listeners are registered with this object.
     * It will unblock when a lister is added to the object.
     */
    private synchronized void tryWait() {
        if (listenerList.getListenerCount() == 0) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException exp) {
                }
            }
        }
    }

    private void wakeUp() {
        synchronized (this) {
            //we have listener, unblock ourself...
            notifyAll();
        }
    }

    /**
     * Adds a listener to the listener list.
     *
     * @param listenerClass
     * @param listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private synchronized void addListener(Class listenerClass, EventListener listener) {
        listenerList.add(listenerClass, listener);
    }

    /**
     * Removes a listener to the listener list.
     *
     * @param listenerClass
     * @param listener
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private synchronized void removeListener(Class listenerClass, EventListener listener) {
        listenerList.remove(listenerClass, listener);
    }

}
