package org.geworkbench.components.plots;

import javax.swing.AbstractListModel;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

/**
 * ListModel for the marker list.
 */
class MarkerListModel extends AbstractListModel<DSGeneMarker> {
	private static final long serialVersionUID = -2709192821511189399L;

	DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView;
	MarkerListModel(DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView) {
		this.dataSetView = dataSetView; 
		
	}
	public int getSize() {
        if (dataSetView.getMicroarraySet() == null) {
            return 0;
        }

		DSPanel<DSGeneMarker> mp = dataSetView.getMarkerPanel();
		if ((mp != null) && (mp.size() > 0)) {
			return mp.size();
		} else {
			return dataSetView.allMarkers().size();
		}
    }

    public DSGeneMarker getElementAt(int index) {
        if (dataSetView.getMicroarraySet() == null) {
            return null;
        }

		DSPanel<DSGeneMarker> mp = dataSetView.getMarkerPanel();
		if ((mp != null) && (mp.size() > 0) && (index < mp.size())) {
			return mp.get(index);
		} else {
			return dataSetView.allMarkers().get(index);
		}
    }

    /**
     * Indicates to the associated JList that the contents need to be redrawn.
     */
    public void refresh() {
        if (dataSetView.getMicroarraySet() == null) {
            fireContentsChanged(this, 0, 0);
        } else {
       		fireContentsChanged(this, 0, dataSetView.getMarkerPanel().size() - 1);
        }
    }
    
	public void setDatasetView(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView) {
		this.dataSetView = dataSetView; 
	}

}