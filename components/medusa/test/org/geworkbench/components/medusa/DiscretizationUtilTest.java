package org.geworkbench.components.medusa;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
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
 * 
 * @author keshav
 * @version $Id: DiscretizationUtilTest.java,v 1.1 2007-05-11 17:00:33 keshav Exp $
 */
public class DiscretizationUtilTest extends TestCase {

	int numElements = 10;

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
		Random r = new Random();

		discreteUtil = new DiscretizationUtil();

		for (int i = 0; i < numElements; i++) {
			for (int j = 0; j < numElements; j++) {
				// data[i][j] = r.nextFloat();
				data[i][j] = i + j + 1;
			}
		}
	}

	/**
	 * 
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

		DSPanel markerPanel = view.getMarkerPanel();
		for (int i = 0; i < numElements; i++) {
			DSGeneMarker geneMarker = new CSGeneMarker();
			geneMarker.setGeneId(i);
			geneMarker.setGeneName("gene_name_" + i);
			geneMarker.setLabel("gene_label_" + i);
			markerPanel.add(geneMarker);
		}

		/* order here is important for marker labels */
		view.setMicroarraySet(microarraySet);
		view.useMarkerPanel(true);
		view.setMarkerPanel(markerPanel);

		discreteView = discreteUtil.discretize(view, base, bound);

		assertNotNull(discreteView);

		List<String> regulatorNames = new ArrayList<String>();
		List<String> targetNames = new ArrayList<String>();
		for (int i = 0; i < numElements; i++) {
			DSGeneMarker geneMarker = (DSGeneMarker) markerPanel.get(i);
			regulatorNames.add(geneMarker.getLabel());
			targetNames.add(geneMarker.getLabel());
		}
		MedusaHelper.writeMedusaLabelsFile(discreteView,
				"data/test/dataset/output/"
						+ RandomStringUtils.randomAlphabetic(5) + ".labels",
				regulatorNames, targetNames);

	}
}
