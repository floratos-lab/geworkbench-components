package org.geworkbench.components.discovery.algorithm;

import java.io.File;
import java.util.EventListener;

import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.sequence.DSSequence;
import org.geworkbench.bison.datastructure.complex.pattern.PatternResult;
import org.geworkbench.bison.util.RandomNumberGenerator;
import org.geworkbench.components.discovery.PatternDataSource;
import org.geworkbench.events.ProgressChangeEvent;
import org.geworkbench.events.StatusBarEvent;
import org.geworkbench.events.listeners.ProgressChangeListener;
import org.geworkbench.events.listeners.StatusChangeListener;

/**
 * This class loads saved patterns from a file.
 * <p>Title: Bioworks</p>
 * <p>Description: Modular Application Framework for Gene Expession, Sequence and Genotype Analysis</p>
 * <p>Copyright: Copyright (c) 2003 -2004</p>
 * <p>Company: Columbia University</p>
 *
 * @author not attributable
 * @version $Id$
 */
public class RegularDiscoveryFileLoader {

    private PatternDataSource patternSource = null;

	// the main functionality of this class
    public PatternResult read(File sequenceFile, File patternFile, DSDataSet<DSSequence> parent) {
        if (sequenceFile == null) {
            JOptionPane.showMessageDialog(null, "Please first select the sequence file that is\n" + "associated with this pattern file. ");

            return null;
        }

        PatternResult patternDB = new PatternResult(sequenceFile, parent);
        String idString =  RandomNumberGenerator.getID();
        patternDB.setID(idString);

        //loading stuff
        if (patternDB.read(patternFile)) {
        	patternSource = new PatternDataSource(patternDB);

            fireChanges(patternDB.getPatternNo(),
            		"Patterns were loaded from: " + patternFile.getAbsoluteFile());

            return patternDB;

        } else {
            return null;
        }

    }

    /* This class is not for loading file instead of getting results, so ProgressChangeEvent is always set to be initial.  */

    private void fireChanges(int patternNumber, String statusBarMessage) {
        fireProgressChanged(new ProgressChangeEvent(true, patternNumber));
        fireStatusBarChanged(new StatusBarEvent(statusBarMessage));
    }
    
    // to separate from AbstractSequenceDiscoveryAlgorithm,
    // similar event listener mechanism is re-implemented here
    private EventListenerList listenerList = new EventListenerList();
    
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

    protected void wakeUp() {
        synchronized (this) {
            //we have listener, unblock ourself...
            notifyAll();
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
            fireChanges(0, "");
        }
    }

	public PatternDataSource getPatternSource() {
		return patternSource;
	}
}
