package org.geworkbench.components.medusa.heatmap;
import javax.swing.JDialog;

import junit.framework.TestCase;

import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaData;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;

/**
 * A test class for the MedusaHeatMap.
 * 
 * @author keshav
 * @version $Id: MedusaHeatMapTest.java,v 1.2 2007-05-30 21:19:51 keshav Exp $
 */
public class MedusaHeatMapTest extends TestCase {

	private MedusaData medusaData = null;

	private int numElements = 2;

	float data[][] = new float[numElements][numElements];

	@Override
	protected void setUp() {
		DSMicroarraySetView view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
		DSMicroarraySet microarraySet = new CSMicroarraySet<DSMicroarray>();
		microarraySet.setLabel(this.getClass().getName());

		// DSItemList markers = view.markers();

		for (int i = 0; i < numElements; i++) {
			DSMicroarray microarray = new CSMicroarray(numElements);
			microarray.setLabel("Microarray " + i);
			for (int j = 0; j < numElements; j++) {
				CSExpressionMarkerValue markerVal = new CSExpressionMarkerValue(
						data[i][j]);

				microarray.setMarkerValue(j, markerVal);
			}
			microarraySet.add(microarray);
		}

		DSPanel markerPanel = view.getMarkerPanel();
		for (int i = 0; i < numElements; i++) {
			DSGeneMarker geneMarker = new CSGeneMarker();
			geneMarker.setGeneId(i);
			geneMarker.setGeneName("gene_name_" + i);
			geneMarker.setLabel("gene_label_" + i);
			geneMarker.setSerial(i);
			markerPanel.add(geneMarker);
		}

		/* order here is important for marker labels */
		view.setMicroarraySet(microarraySet);
		view.useMarkerPanel(true);
		view.setMarkerPanel(markerPanel);
	}

	public void testMedusaHeatMap() {
		JDialog dialog = new JDialog();
		// ModulatorHeatMap heatMap = new ModulatorHeatMap();
		// dialog.add(heatMap);
		dialog.pack();
		dialog.setVisible(true);
		dialog.setModal(true);

	}

}
