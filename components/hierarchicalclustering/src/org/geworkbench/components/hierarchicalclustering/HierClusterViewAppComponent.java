package org.geworkbench.components.hierarchicalclustering;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.event.EventListenerList;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.clusters.DSHierClusterDataSet;
import org.geworkbench.builtin.projects.ProjectPanel;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.events.HierClusterModelEvent;
import org.geworkbench.events.HierClusterModelEventListener;
import org.geworkbench.events.ProjectEvent;

/**
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: First Genetic Trust Inc.</p>
 * @author First Genetic Trust
 * @version $Id$
 */

/**
 * The application component that "wraps" the <code>HierClusterViewWidget</code>
 * which does the actual displaying of the clusters.
 * <p/>
 * This component receives events from the analysis pane when a hierarchical
 * clustering analysis is finished.
 * <p/>
 * Upon receiving such events, it updates
 * the data displayed in <code>hclWidget</code> by throwing a
 * <code>HierClusterModelEvent</code> event that contains the new clustering
 * data.
 */
@AcceptTypes({DSHierClusterDataSet.class})
public class HierClusterViewAppComponent implements VisualPlugin, PropertyChangeListener {
    /**
     * The widget used by the component.
     */
    private final HierClusterViewWidget hclWidget = new HierClusterViewWidget();
    /**
     * For registering the <code>hclWidget</code>, to be notified when a new
     * clustering analysis has produced new data.
     */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * Default Constructor
     */
    public HierClusterViewAppComponent() {
        this.addHierClusterModelListener(hclWidget);
        hclWidget.addPropertyChangeListener(this);
    }

    /**
     * Receives a {@link DSHierClusterDataSet} and renders it.
     *
     * @param event
     * @param source
     */
    @Subscribe public void receive(ProjectEvent event, Object source) {
        DSDataSet<?> dataSet = event.getDataSet();
        if ((dataSet != null) && (dataSet instanceof DSHierClusterDataSet)) {
            DSHierClusterDataSet clusterSet = (DSHierClusterDataSet) dataSet;
            HierClusterModelEvent hcme = new HierClusterModelEvent(source, clusterSet);
            fireModelChanged(hcme);
        }
    }

    /**
     * Adds a new listener.
     *
     * @param hcmel the Hierarchical Clustering widget implementing the
     *              <code>HierClusterModelEventListener</code> interface
     */
    public void addHierClusterModelListener(HierClusterModelEventListener hcmel) {
        listenerList.add(HierClusterModelEventListener.class, hcmel);
    }

    /**
     * Interface <code>VisualPlugin</code> method
     *
     * @return the visual Hierarchical Clustering widget that extends
     *         <code>JPanel</code>
     */
    public Component getComponent() {
        return hclWidget;
    }

    private void fireModelChanged(HierClusterModelEvent event) {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HierClusterModelEventListener.class) {
                ((HierClusterModelEventListener) listeners[i + 1]).hierClusterModelChange(event);
            }
        }
    }

    @Publish
    public org.geworkbench.events.MarkerSelectedEvent publishSingleMarkerEvent(org.geworkbench.events.MarkerSelectedEvent event) {
        return event;
    }

    @SuppressWarnings("rawtypes")
	@Publish
    public org.geworkbench.events.SubpanelChangedEvent publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent event) {
        return event;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        if (propertyName.equals(HierClusterViewWidget.SAVEIMAGE_PROPERTY)) {
        	ProjectPanel.getInstance().addImageNode((ImageIcon) event.getNewValue());
        } else if (propertyName.equals(HierClusterViewWidget.SINGLE_MARKER_SELECTED_PROPERTY)) {
            publishSingleMarkerEvent(new org.geworkbench.events.MarkerSelectedEvent((DSGeneMarker) event.getNewValue()));
        } else if (propertyName.equals(HierClusterViewWidget.MULTIPLE_MARKER_SELECTED_PROPERTY)) {

            DSPanel<DSGeneMarker> clusterBranch = new CSPanel<DSGeneMarker>("Cluster Tree", "Dendrogram");

            DSGeneMarker[] mInfos = (DSGeneMarker[]) event.getNewValue();

            for (int i = 0; i < mInfos.length; i++) {
                clusterBranch.add(mInfos[i]);
            }

            publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent(DSGeneMarker.class, clusterBranch, org.geworkbench.events.SubpanelChangedEvent.NEW));
        } else if (propertyName.equals(HierClusterViewWidget.MULTIPLE_ARRAY_SELECTED_PROPERTY)) {

            DSPanel<DSMicroarray> clusterBranch = new CSPanel<DSMicroarray>("Cluster Tree", "Dendrogram");

            DSMicroarray[] mInfos = (DSMicroarray[]) event.getNewValue();

            for (int i = 0; i < mInfos.length; i++) {
                clusterBranch.add(mInfos[i]);
            }

            publishSubpanelChangedEvent(new org.geworkbench.events.SubpanelChangedEvent(DSMicroarray.class, clusterBranch, org.geworkbench.events.SubpanelChangedEvent.NEW));
        }
    }
}
