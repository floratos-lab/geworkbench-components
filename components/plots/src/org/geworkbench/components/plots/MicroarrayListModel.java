package org.geworkbench.components.plots;

import javax.swing.AbstractListModel;

import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

/**
 * ListModel for the microarray list.
 */
class MicroarrayListModel extends AbstractListModel<DSMicroarray> {
	private static final long serialVersionUID = 1508449468167888966L;

	DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView;
	
	MicroarrayListModel(DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView) {
		this.dataSetView = dataSetView; 
		
	}

	public int getSize() {
        if (dataSetView.getMicroarraySet() == null) {
            return 0;
        }

       	DSPanel<DSMicroarray> ap = dataSetView.getItemPanel();
       	if((ap != null) && (ap.size() > 0)){
       		return ap.size();
       	} else {
       		return dataSetView.size();
       	}
    }

    public DSMicroarray getElementAt(int index) {
        if (dataSetView.getMicroarraySet() == null) {
            return null;
        }

       	DSPanel<DSMicroarray> ap = dataSetView.getItemPanel();
       	if((ap != null) && (ap.size() > 0) && (index < ap.size())){
       		return ap.get(index); //.getLabel();
       	} else {
       		return dataSetView.get(index);
       	}
    }

    /**
     * Indicates to the associated JList that the contents need to be redrawn.
     */
    public void refresh() {        	
        if (dataSetView.getMicroarraySet() == null) {
            fireContentsChanged(this, 0, 0);
        } else {
       		fireContentsChanged(this, 0, dataSetView.getItemPanel().size() - 1);
        }
    }

	public void setDatasetView(
			DSMicroarraySetView<DSGeneMarker, DSMicroarray> dataSetView) {
		this.dataSetView = dataSetView; 
	}
}
