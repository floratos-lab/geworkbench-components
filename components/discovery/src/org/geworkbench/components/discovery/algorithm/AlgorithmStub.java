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
 * @version $Id$
 */
public class AlgorithmStub {

    //Visual parameter panel
    private JPanel parameterPanel = null;
    //algorithm description
    private String algorithmName = "";
    //the algorithm to run in this stub.
    private ServerBaseDiscovery algorithm;

    public AlgorithmStub(ServerBaseDiscovery algorithm) {
        this.algorithm = algorithm;
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
     * Set the algorithm of this stub.
     */
    public void setAlgorithm(ServerBaseDiscovery algorithm) {
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
}
