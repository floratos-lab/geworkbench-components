package org.geworkbench.components.mindy;

import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSItemList;
import org.geworkbench.engine.config.VisualPlugin;
import org.geworkbench.engine.management.AcceptTypes;
import org.geworkbench.engine.management.Subscribe;
import org.geworkbench.engine.management.Publish;
import org.geworkbench.events.*;
import org.geworkbench.util.pathwaydecoder.mutualinformation.MindyDataSet;
import org.geworkbench.bison.datastructure.complex.panels.*;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author mhall
 * @author ch2514
 * @version $ID$
 */
@AcceptTypes(MindyDataSet.class)
public class MindyVisualComponent implements VisualPlugin {

    static Log log = LogFactory.getLog(MindyVisualComponent.class);

    private MindyDataSet dataSet;
    private JPanel plugin;
    private MindyPlugin mindyPlugin;
    private ArrayList<DSGeneMarker> selectedMarkers;
    private DSPanel<DSGeneMarker> selectorPanel;

    /**
     * Constructor.
     * Includes a place holder for a MINDY result view (i.e. class MindyPlugin).
     *
     */
    public MindyVisualComponent() {
        // Just a place holder
        plugin = new JPanel(new BorderLayout());
        selectorPanel = null;
    }

    /**
     * @return The MINDY result view component (of class MindyPlugin)
     */
    public Component getComponent() {
        return plugin;
    }

    /**
     * Receives the general ProjectEvent from the framework.
     * Creates MINDY's data set based on data from the ProjectEvent.
     * @param projectEvent 
     * @param source - source of the ProjectEvent
     */
    @Subscribe public void receive(ProjectEvent projectEvent, Object source) {
        log.debug("MINDY received project event.");
        DSDataSet data = projectEvent.getDataSet();
        if ((data != null) && (data instanceof MindyDataSet)) {
            if (dataSet != data) {
                dataSet = ((MindyDataSet) data);
                plugin.removeAll();
                mindyPlugin = new MindyPlugin(dataSet.getData(), this);
                mindyPlugin.limitMarkers(selectedMarkers);
                plugin.add(mindyPlugin, BorderLayout.CENTER);
                plugin.revalidate();
                plugin.repaint();
            }
        }
    }

    /**
     * Receives GeneSelectorEvent from the framework.
     * Extracts markers in the selected marker sets from the Selector Panel.
     * @param e - GeneSelectorEvent
     * @param source - source of the GeneSelectorEvent
     */
    @SuppressWarnings("unchecked")
    @Subscribe
    public void receive(GeneSelectorEvent e, Object source) {
        if (dataSet != null) {
        	if(e.getPanel() != null) this.selectorPanel = e.getPanel();
        	else log.error("Received Gene Selector Event: Selection panel sent was null");
            DSMicroarraySetView<DSGeneMarker, DSMicroarray> maView = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>(dataSet.getData().getArraySet());
            maView.setMarkerPanel(e.getPanel());
            maView.useMarkerPanel(true);
            if((maView.getMarkerPanel() != null) 
            		&& (maView.getMarkerPanel().activeSubset() != null) 
            		&& (maView.getMarkerPanel().activeSubset().size() == 0)
            		) {
                selectedMarkers = null;
            } else {
            	if((maView != null) 
            			&& (maView.getUniqueMarkers() != null)
            			){
	                DSItemList<DSGeneMarker> uniqueMarkers = maView.getUniqueMarkers();
	                if (uniqueMarkers.size() > 0) {
	                    selectedMarkers = new ArrayList<DSGeneMarker>();
	                    for (Iterator<DSGeneMarker> iterator = uniqueMarkers.iterator(); iterator.hasNext();) {
	                        DSGeneMarker marker = iterator.next();
	                        log.debug("Selected " + marker.getShortName());
	                        selectedMarkers.add(marker);
	                    }
	                }
            	}
            }
            if (selectedMarkers != null) {
                mindyPlugin.limitMarkers(selectedMarkers);
            } else {
            	mindyPlugin.limitMarkers(null);
            }
        } else {
        	log.error("Received Gene Selector Event: Dataset in this component is null");
        }
    }

    /**
     * Publish SubpanelChangedEvent to the framework to add selected markers to the marker set(s)
     * on the Selector Panel.
     * @param e - SubpanelChangedEvent
     * @return
     */
    @Publish public SubpanelChangedEvent publishSubpanelChangedEvent(SubpanelChangedEvent e){
    	return e;
    }

    /**
     * Publish ImageSnapshotEvent to the framework to capture an image of the heat map.
     * @param heatmap - MINDY's heat map panel
     * @return ImageSnapshotEvent
     */
    @Publish public ImageSnapshotEvent createImageSnapshot(Component heatmap) {
        Dimension panelSize = heatmap.getSize();
        BufferedImage image = new BufferedImage(panelSize.width, panelSize.height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        heatmap.print(g);
        ImageIcon icon = new ImageIcon(image, "MINDY Heat Map");
        org.geworkbench.events.ImageSnapshotEvent event = new org.geworkbench.events.ImageSnapshotEvent("MINDY Heat Map", icon, org.geworkbench.events.ImageSnapshotEvent.Action.SAVE);
        return event;
    }
    
    DSPanel getSelectorPanel(){
    	return this.selectorPanel;
    }

    ArrayList<DSGeneMarker> getSelectedMarkers(){
    	return this.selectedMarkers;
    }
}
