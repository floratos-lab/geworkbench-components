package org.geworkbench.components.medusa;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.apache.commons.lang.RandomStringUtils;
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
import org.geworkbench.components.medusa.gui.MedusaParamPanel;

/**
 * 
 * @author keshav
 * @version $Id: MedusaAnalysisTest.java,v 1.13 2009-09-10 16:40:26 chiangy Exp $
 */
public class MedusaAnalysisTest extends TestCase {

	/*
	 * FIXME This test will fail without first running DiscretizationUtilTest
	 * since the generated labels file from this test does not have targets. The
	 * labels file generated from DiscretizationUtilTest does have both
	 * regulators and targets. As a result of this, you should first run the
	 * DircretizationUtilTest, then this test. NOTE: The .labels file generated
	 * from this test is not used. Instead, the labels file used is the one from
	 * DiscretizationUtilTest. Also, make sure you set the MedusaHelper to use
	 * the method view.markers() instead of view.allMarkers().
	 * 
	 * 
	 * Actually, this test will still fail with:
	 * 
	 * Can't rip an array of length <= 0 at
	 * edu.columbia.ccls.utilities.ArrayUtils.ripIntArray(Unknown Source) at
	 * edu.columbia.ccls.medusa.MedusaLoader.initRandomHoldout(Unknown Source)
	 * at edu.columbia.ccls.medusa.MedusaLoader.initHoldout(Unknown Source) at
	 * edu.columbia.ccls.medusa.MedusaLoader.main(Unknown Source) at
	 * org.geworkbench.components.medusa.MedusaAnalysis.execute(MedusaAnalysis.java:129)
	 * at
	 * org.geworkbench.components.medusa.MedusaAnalysisTest.testExecuteUsingConfigFile(MedusaAnalysisTest.java:121)
	 * 
	 */
	private Log log = LogFactory.getLog(this.getClass());

	MedusaParamPanel panel = null;

	MedusaAnalysis analysis = null;

	int numElements = 2;

	float data[][] = new float[numElements][numElements];

	DSMicroarraySetView view = null;

	@Override
	protected void setUp() {
		panel = new MedusaParamPanel();
		analysis = new MedusaAnalysis();

		Random r = new Random();

		// discreteUtil = new DiscretizationUtil();

		for (int i = 0; i < numElements; i++) {
			for (int j = 0; j < numElements; j++) {
				// data[i][j] = r.nextFloat();
				data[i][j] = i + j + 1;
				log.info("data[" + i + "][" + j + "] = " + data[i][j]);
			}
		}

		view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
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

		List<DSGeneMarker> regulators = new ArrayList<DSGeneMarker>();
		DSGeneMarker geneMarker = (DSGeneMarker) markerPanel.get(0);
		regulators.add(geneMarker);

		List<DSGeneMarker> targets = new ArrayList<DSGeneMarker>();
		DSGeneMarker targetMarker = (DSGeneMarker) markerPanel.get(1);
		targets.add(targetMarker);

		/* order here is important for marker labels */
		view.setMicroarraySet(microarraySet);
		view.useMarkerPanel(true);
		view.setMarkerPanel(markerPanel);
	}

	/**
	 * 
	 * 
	 */
	public void testExecuteUsingConfigFile() {
		panel.setConfigFilePath("data/test/dataset/config.xml");
		panel.setLabelsFilePath("data/test/dataset/"
				+ RandomStringUtils.randomAlphabetic(5) + "_test.labels");
		analysis.setDefaultPanel(panel);
		analysis.execute(view);
	}
}
