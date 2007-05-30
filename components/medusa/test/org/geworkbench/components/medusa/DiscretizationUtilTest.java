package org.geworkbench.components.medusa;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * A test class for DiscretizationUtil.
 * 
 * @author keshav
 * @version $Id: DiscretizationUtilTest.java,v 1.1 2007/05/11 17:00:33 keshav
 *          Exp $
 */
public class DiscretizationUtilTest extends TestCase {
	private Log log = LogFactory.getLog(this.getClass());

	int numElements = 2;

	float data[][] = new float[numElements][numElements];

	float base = 1;

	float bound = 1;

	DiscretizationUtil discreteUtil = null;

	DSMicroarraySetView<DSGeneMarker, DSMicroarray> discreteView = null;

	/**
	 * 
	 * 
	 */
	@Override
	protected void setUp() {
		discreteUtil = new DiscretizationUtil();

		for (int i = 0; i < numElements; i++) {
			for (int j = 0; j < numElements; j++) {
				// data[i][j] = r.nextFloat();
				data[i][j] = i + j + 1;
				log.info("data[" + i + "][" + j + "] = " + data[i][j]);
			}
		}
	}

	/**
	 * Tests the discretize method.
	 * 
	 */
	public void testDiscretize() {
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

		List<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();
		DSPanel markerPanel = view.getMarkerPanel();
		for (int i = 0; i < numElements; i++) {
			DSGeneMarker geneMarker = new CSGeneMarker();
			geneMarker.setGeneId(i);
			geneMarker.setGeneName("gene_name_" + i);
			geneMarker.setLabel("gene_label_" + i);
			markerPanel.add(i, geneMarker);
			targets.add(geneMarker);
		}

		/* order here is important for marker labels */
		view.setMicroarraySet(microarraySet);
		view.useMarkerPanel(true);
		view.setMarkerPanel(markerPanel);
		/*
		 * FIXME not sure why the # markers is 0. The MedusaHelper works fine
		 * from within geworkbench, but for some reason this test does not add
		 * the markers to the microarraySet (view). That is, you will not see
		 * anything in the .labels file written from this test. Again, this
		 * works fine from within the app. If you change this to
		 * view.markers().size in teh MedusaHelper, this works but we cannot do
		 * that for geworkench specific reasons.
		 */
		log.debug("marker panel size: " + view.allMarkers().size());

		log.info("base: " + base + ", bound: " + bound);
		discreteView = discreteUtil.discretize(view, base, bound);

		assertNotNull(discreteView);

		printDiscrete(markerPanel);

		/* this tests writing out the labels file */
		List<DSGeneMarker> regulators = new ArrayList<DSGeneMarker>();
		DSGeneMarker geneMarker = (DSGeneMarker) markerPanel.get(0);
		regulators.add(geneMarker);

		// MedusaHelper.writeMedusaLabelsFile(discreteView, "data/test/dataset/"
		// + RandomStringUtils.randomAlphabetic(5) + ".labels",
		// regulators, targets);
		MedusaUtil.writeMedusaLabelsFile(discreteView,
				"data/test/dataset/web100_test.labels", regulators, targets);

	}

	private void printDiscrete(DSPanel markerPanel) {
		for (int i = 0; i < markerPanel.size(); i++) {
			DSGeneMarker obj = (CSGeneMarker) markerPanel.get(i);
			// double[] row = discreteView.getRow(obj);
			double[] row = discreteView.getRow(i);
			for (int j = 0; j < row.length; j++) {
				double val = row[j];
				log.info("discrete[" + i + "][" + j + "] = " + val);
			}

		}
	}
}
