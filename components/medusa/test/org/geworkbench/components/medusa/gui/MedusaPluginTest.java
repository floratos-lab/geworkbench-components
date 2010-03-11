package org.geworkbench.components.medusa.gui;

import java.util.ArrayList;

import javax.swing.JDialog;

import junit.framework.TestCase;

import org.geworkbench.bison.datastructure.biocollections.medusa.MedusaCommand;
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
 * GUI tests for the {@link MedusaVisualizationPanel}.
 * 
 * @author keshav
 * @version $Id: MedusaPluginTest.java,v 1.6 2008-05-01 14:46:57 chiangy Exp $
 * 
 * TODO: this class is out-dated, should be removed.
 */
public class MedusaPluginTest extends TestCase {

	MedusaVisualComponent visualComponent = new MedusaVisualComponent();

	MedusaData medusaData = null;

	MedusaVisualizationPanel medusaVisualizationPanel = null;

	int numElements = 5;

	float data[][] = new float[numElements][numElements];

	private ArrayList<DSGeneMarker> targets = null;

	private MedusaCommand medusaCommand = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() {

		/* set up the microarray set (and view) */
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

		targets = new ArrayList<DSGeneMarker>();
		DSPanel markerPanel = view.getMarkerPanel();
		for (int i = 0; i < numElements; i++) {
			DSGeneMarker geneMarker = new CSGeneMarker();
			geneMarker.setGeneId(i);
			geneMarker.setGeneName("gene_name_" + i);
			geneMarker.setLabel("gene_label_" + i);
			geneMarker.setSerial(i);
			markerPanel.add(geneMarker);

			targets.add(geneMarker);
		}

		/* order here is important for marker labels */
		view.setMicroarraySet(microarraySet);
		view.useMarkerPanel(true);
		view.setMarkerPanel(markerPanel);

		/* pack medusa data */
		medusaCommand = new MedusaCommand();
		medusaData = new MedusaData(microarraySet, null, targets, medusaCommand);

		/* create visual plugin */
		medusaVisualizationPanel = new MedusaVisualizationPanel(
				visualComponent, medusaData, "");

	}

	/**
	 * Tests setting up the MedusuaPlugin layout.
	 * 
	 */
	public void testMedusaPluginLayout() {

		JDialog dialog = new JDialog();
		dialog.add(medusaVisualizationPanel);
		dialog.pack();
		dialog.setModal(true);
		dialog.setVisible(true);

		/*
		 * Doesn't do anything. This test serves the purpose of testing the
		 * plugin layout.
		 */
		assertNotNull(dialog);

	}

}
