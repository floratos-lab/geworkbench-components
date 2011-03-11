package org.geworkbench.components.discovery.algorithm;

import org.geworkbench.events.listeners.ProgressChangeListener;
import org.geworkbench.events.listeners.StatusChangeListener;
import org.geworkbench.util.patterns.DataSource;

import javax.swing.*;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: This class provides an interface for dealing with different
 * types of algorithms. The class holds an algorithm which does some discovery
 * work. In addition it also holds reference for the parameter panel of the
 * algorithm</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public class AlgorithmStub {
    //the "empty" algorithm - used in initialization.
    protected final static AbstractSequenceDiscoveryAlgorithm emptyAlgorithm = new AbstractSequenceDiscoveryAlgorithm() {
        public void start() {
        };
        public void stop() {
        };
    };

    //Visual parameter panel
    private JPanel parameterPanel = null;
    //algorithm description
    private String algorithmName = "";

    // handle to enable/disable exec button
    private javax.swing.JButton execbutton = null;

    //the algorithm to run in this stub.
    private AbstractSequenceDiscoveryAlgorithm algorithm;
    //we create a thread to start the algorithm in.
    private Runnable doStart = new Runnable() {
        public void run() {
            algorithm.start();
            enableExec();
        }
    };

    /*
     * re-enables the execute button for running the algorithm
     *
     */
    private void enableExec() {
        if (execbutton != null) {
            execbutton.setEnabled(true);
            execbutton = null;
        }
    }

    public AlgorithmStub() {
        algorithm = AlgorithmStub.emptyAlgorithm;
    }

    /**
     * Start the algorithm in this stub.
     * The method startd the algorithm in its own thread.
     */

    public void start(javax.swing.JButton disable) {
        execbutton = disable;
        execbutton.setEnabled(false);
        Thread thread = new Thread(doStart, "Algorithm thread Start");
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    /**
     * Stop the running of the algorithm in this stub.
     * This method calls stop directly on the algorithm. If the s
     * This method returns immediately.
     */
    public void stop() {
        algorithm.stop();
    }

    /**
     * This method notifies to the underline algorithm not to broadcast
     * to the component.
     *
     * @see ProgressChangeEvent.
     */
    public void lostFocus(ProgressChangeListener listener) {
        algorithm.removeProgressChangeListener(listener);
    }

    /**
     * This method notifies to the underline algorithm that the component is intrested
     * in receiving broadcast events.
     *
     * @see ProgressChangeEvent.
     */
    public void gainedFocus(ProgressChangeListener listener) {
        algorithm.addProgressChangeListener(listener);
    }

    /**
     * Get the algorithm of this stub.
     *
     * @return alogrithm
     */
    public AbstractSequenceDiscoveryAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Set the algorithm of this stub.
     */
    public void setAlgorithm(AbstractSequenceDiscoveryAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public void setDescription(String description) {
        this.algorithmName = description;
    }

    public String getDescription() {
        return this.algorithmName;
    }

    /**
     * Return the result of running the algorithm.
     * Note: in this implementation an Algorithm is a datasource, this may
     * need to be changed.
     *
     * @return
     */
    public DataSource getResultDataSource() {
        return (DataSource) algorithm;
    }

    public void removeStatusChangeListener(StatusChangeListener listener) {
        algorithm.removeStatusChangeListener(listener);
    }

    public void addStatusChangeListener(StatusChangeListener listener) {
        algorithm.addStatusChangeListener(listener);
    }


    public void setParameterPanel(JPanel panel) {
        this.parameterPanel = panel;
    }

    public JPanel getParameterPanel() {
        return this.parameterPanel;
    }

    //  public abstract JPanel getViewPanel();
}
