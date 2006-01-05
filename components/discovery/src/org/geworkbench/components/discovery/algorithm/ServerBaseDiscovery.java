package org.geworkbench.components.discovery.algorithm;

import org.geworkbench.events.ProgressBarEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.util.session.Session;
import org.geworkbench.util.session.SessionOperationException;
import polgara.soapPD_wsdl.Parameters;

import java.awt.*;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: This class is the base class for SPLASH server base
 * algorithm. It factorizes some commonalities.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public abstract class ServerBaseDiscovery extends AbstractSequenceDiscoveryAlgorithm {
    //the session to connect to.
    private Session session;
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
     * @param session the object to run queries to the server.
     */
    public ServerBaseDiscovery(Session session, Parameters parm) {
        initSession(session);
        if (parm == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [parm=null]");
        }
        this.parms = parm;
    }

    /**
     * This constructor will force a reconnection to an existing running algorithm
     * on the server. It will reconnectAlgorithm.
     *
     * @param session Session
     */
    public ServerBaseDiscovery(Session session) {
        initSession(session);
        this.reconnect = true;
    }

    private void initSession(Session session) {
        if (session == null) {
            throw new NullPointerException("ServerBaseDiscovery Constructor failed: [session=null]");
        }
        this.session = session;
    }

    /**
     * Start the algorithm. The first thing we do is upload sequences
     * to the server. We let subclasses implement the actual algorithm.
     */
    public void start() {
        try {
//Temp change for bug 329.
             if (session.isDone() && (!reconnect)) {
                // if (!session.loadSequenceRemote()) {
                    //upload sequences
                    fireStatusBarEvent("Uploading....");
                    if (!upload()) {
                        return;
                    }
                    session.saveSeqDB();
                // }

                session.setParameters(parms);
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
        Session session = getSession();
        try {
            double percent = session.getCompletion();
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
            session.stop();
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
     * Get the session of this algorithm.
     *
     * @return session the session object.
     */
    protected Session getSession() {
        return session;
    }

    /**
     * Uploads the sequence in the session to the server.
     *
     * @param s
     */
    private boolean upload() {
        final int databaseSize = session.getSequenceDB().getSequenceNo();
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
            session.upload(i);
        } catch (SessionOperationException exp) {
            System.out.println("Session operation exception");
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

    /**
     * Classes inheriting from this class should implement their algorithm
     * in this method.
     */
    protected abstract void runAlgorithm();

    /**
     * Classes inheriting from this class should implement their reconnection
     * code in this method.
     */

    protected abstract void reconnectAlgorithm();
}
