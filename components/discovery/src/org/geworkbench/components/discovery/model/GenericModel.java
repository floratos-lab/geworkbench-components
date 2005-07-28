package org.geworkbench.components.discovery.model;

import org.geworkbench.util.patterns.DataSource;
import org.geworkbench.events.listeners.ProgressChangeListener;

/**
 * <p>Title: Sequence and Pattern Plugin</p>
 * <p>Description: A generic model to be implemented by all models
 * that have a view in the SequenceDiscoveryViewWidget.</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

public interface GenericModel extends ProgressChangeListener {
    /**
     * Clears all the data from the model.
     */
    public void clear();

    /**
     * Attach a Data source to the model. The source will supply data
     * to the model.
     *
     * @param dataSource
     * @return true if and only if the source can be attached to this model
     */
    public boolean attach(DataSource source);
}