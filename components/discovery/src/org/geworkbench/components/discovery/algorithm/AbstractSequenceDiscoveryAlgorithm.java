package org.geworkbench.components.discovery.algorithm;

import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.ProgressChangeListener;
import org.geworkbench.events.listeners.StatusChangeListener;
import org.geworkbench.bison.datastructure.biocollections.sequences.DSSequenceSet;
import org.geworkbench.bison.datastructure.complex.pattern.SoapParmsDataSet;
import org.geworkbench.components.discovery.SequenceDiscoveryViewWidget;

import javax.swing.event.EventListenerList;
import java.util.EventListener;
import java.io.File;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: This abstract class provides
 * the management of listeners for updating the sequenceDiscvoeryWidget</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public abstract class AbstractSequenceDiscoveryAlgorithm {
    /**
     * List of listeners
     */
    private EventListenerList listenerList = new EventListenerList();

    File resultFile;
    SoapParmsDataSet result;
    DSSequenceSet sequenceInputData;

    // SequenceDiscoveryViewWidget which created this stub
    protected SequenceDiscoveryViewWidget viewWidget = null;
    public SequenceDiscoveryViewWidget getViewWidget() {
		return viewWidget;
	}

	public void setViewWidget(SequenceDiscoveryViewWidget viewWidget) {
		this.viewWidget = viewWidget;
	}

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
     * Removes a listener fom the list.
     *
     * @param	listener		the ProgressChangeListener
     */
    public void removeProgressChangeListener(ProgressChangeListener listener) {
        if (listener instanceof ProgressChangeListener) {
            removeListener(ProgressChangeListener.class, listener);
            progressChangeListenerRemoved();
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
     * Removes a listener fom the list.
     *
     * @param	listener		the StatusChangeListener
     */
    public void removeStatusChangeListener(StatusChangeListener listener) {
        if (listener instanceof StatusChangeListener) {
            removeListener(StatusChangeListener.class, listener);
            statusChangedListenerRemoved();
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
    protected void fireProgressChanged(ProgressChangeEvent e) {
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

    protected void fireProgressBarChanged(org.geworkbench.events.ProgressBarEvent e) {
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

    protected void fireStatusBarChanged(StatusBarEvent e) {
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
    protected synchronized void tryWait() {
        if (getListenerListSize() == 0) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException exp) {
                }
            }
        }
    }

    protected void wakeUp() {
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
    private synchronized void addListener(Class listenerClass, EventListener listener) {
        listenerList.add(listenerClass, listener);
    }

    /**
     * Removes a listener to the listener list.
     *
     * @param listenerClass
     * @param listener
     */
    private synchronized void removeListener(Class listenerClass, EventListener listener) {
        listenerList.remove(listenerClass, listener);
    }

    /**
     * This method is called every time a StatusChangeListener is added
     * to this algorithm. The default behaThe default behavior of this method is unblokck the
     * algorithm if it was blocked before.
     * Subclass can overide the method in order to customize behavior.
     * Note: subclasses should first call super.statusChangeListenerAdded()
     * (For example for updating the view)
     */
    protected void statusChangedListenerAdded() {
        wakeUp();
    }

    /**
     * This method is called every time a StatusChangeListener is removed
     * from this algorithm.
     * Subclass can overide the method in order to customize behavior.
     * (For example in order to stop firing some events)
     */
    protected void statusChangedListenerRemoved() {

    }

    /**
     * This method is called every time a ProgressChangeListener is added
     * to this algorithm. The default behaThe default behavior of this method is unblokck the
     * algorithm if it was blocked before.
     * Subclass can overide the method in order to customize behavior.
     * Note: subclasses should first call super.progressChangeListenerAdded()
     * (For example for updating the view)
     */
    protected void progressChangeListenerAdded() {
        wakeUp();
    }

    /**
     * This method is called every time a ProgressChangeListener is
     * removed from the algorithm. The default behavior of this method is unblokck the
     * algorithm if it was blocked before.
     * Subclass can overide the method in order to customize behavior.
     */
    protected void progressChangeListenerRemoved() {
        wakeUp();
    }

    /**
     * Returns the number of listeners.
     *
     * @return the number of listerners.
     */
    private int getListenerListSize() {
        return listenerList.getListenerCount();
    }

    /**
     * As specifiend by the Algorithm. Not used.
     *
     * @return double
     */
    public double getCompletion() {
        return 0.0;
    }

    public File getResultFile() {
        return resultFile;
    }

    public void setResultFile(SoapParmsDataSet result) {
    	this.result = result;
        this.resultFile = result.getResultFile();
    }

    public DSSequenceSet getSequenceInputData() {
        return sequenceInputData;
    }

    public void setSequenceInputData(DSSequenceSet sequenceInputData) {
    	//System.out.println("IN ASDAlgo: " + sequenceInputData.size());
        this.sequenceInputData = sequenceInputData;
    }

    /**
     * Start the algorithm.
     */
    abstract public void start();

    /**
     * Stop the algorithm.
     */
    abstract public void stop();

}
