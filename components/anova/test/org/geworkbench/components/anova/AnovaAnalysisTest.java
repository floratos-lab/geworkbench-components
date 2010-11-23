/**
 * 
 */
package org.geworkbench.components.anova;

import java.awt.Component;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.analysis.AbstractAnalysis;
import org.geworkbench.bison.annotation.CSAnnotationContextManager;
import org.geworkbench.bison.annotation.DSAnnotationContext;
import org.geworkbench.bison.annotation.DSAnnotationContextManager;
import org.geworkbench.bison.datastructure.biocollections.CSDataSet;
import org.geworkbench.bison.datastructure.biocollections.DSDataSet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.views.CSMicroarraySetView;
import org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView;
import org.geworkbench.bison.datastructure.bioobjects.markers.CSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSAnovaResultSet;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSExpressionMarkerValue;
import org.geworkbench.bison.datastructure.bioobjects.microarray.CSMicroarray;
import org.geworkbench.bison.datastructure.bioobjects.microarray.DSMicroarray;
import org.geworkbench.bison.datastructure.complex.panels.CSAnnotPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSAnnotatedPanel;
import org.geworkbench.bison.datastructure.complex.panels.DSPanel;
import org.geworkbench.bison.model.analysis.AlgorithmExecutionResults;
import org.geworkbench.bison.model.analysis.ParamValidationResults;
import org.geworkbench.components.anova.gui.AnovaAnalysisPanel;
import org.geworkbench.events.SubpanelChangedEvent;

/**
 * @author yc2480
 * 
 */
public class AnovaAnalysisTest extends TestCase {

	/**
	 * @param name
	 */
	public AnovaAnalysisTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	private Log log = LogFactory.getLog(this.getClass());

	AnovaAnalysis analysis = null;
	DSMicroarraySetView<DSGeneMarker, DSMicroarray> view = null;
	int numElements = 9;

	float data[][] = new float[numElements][numElements];

	protected void setUp() throws Exception {
		super.setUp();
		analysis = new AnovaAnalysis();

		for (int i = 0; i < numElements; i++) {
			for (int j = 0; j < numElements; j++) {
				// data[i][j] = r.nextFloat();
				data[i][j] = i + j + 1;
				log.info("data[" + i + "][" + j + "] = " + data[i][j]);
			}
		}

		view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
		DSMicroarraySet<DSMicroarray> microarraySet = new CSMicroarraySet<DSMicroarray>();
		microarraySet.setLabel(this.getClass().getName());

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

		DSPanel<DSGeneMarker> markerPanel = view.getMarkerPanel();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#getAnalysisType()}.
	 */
	public final void testGetAnalysisType() {
		AnovaAnalysis aa = new AnovaAnalysis();
		assertEquals(AbstractAnalysis.TTEST_TYPE, aa.getAnalysisType());
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#getAnalysisName()}.
	 */
	public final void testGetAnalysisName() {
		AnovaAnalysis aa = new AnovaAnalysis();
		assertEquals("Anova", aa.getAnalysisName());
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#getBisonReturnType()}.
	 */
	public final void testGetBisonReturnType() {
		AnovaAnalysis aa = new AnovaAnalysis();
		assertEquals(CSAnovaResultSet.class, aa.getBisonReturnType());
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#useMicroarraySetView()}.
	 */
	public final void testUseMicroarraySetView() {
		AnovaAnalysis aa = new AnovaAnalysis();
		assertEquals(true, aa.useMicroarraySetView());
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#useOtherDataSet()}.
	 */
	public final void testUseOtherDataSet() {
		AnovaAnalysis aa = new AnovaAnalysis();
		assertEquals(false, aa.useOtherDataSet());
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#getBisonParameters()}.
	 */
	public final void testGetBisonParameters() {
		AnovaAnalysis aa = new AnovaAnalysis();
		//TODO: put some parameters into the panel, and test if we can retrieve from panel.toString() and getGisonparameters()
		Component[] components = aa.getParameterPanel().getComponents();
		assertEquals(components[0].getClass(),JPanel.class);
		assertEquals(3,((JPanel)components[0]).getComponents().length);

		assertEquals("ANOVA parameters:\n"+
				"----------------------------------------\n"+
				"P Value estimation: F-Distribution\n"+
				"P Value threshold: 0.05\n"+
				"Correction-method: alpha\n",aa.getParameterPanel().toString());
		
		Map<Serializable, Serializable> bisonParam = aa.getBisonParameters();
		assertSame(((AnovaAnalysisPanel)aa.getParameterPanel()).falseSignificantGenesLimit, bisonParam.get("falseSignificantGenesLimit"));
//		assertSame(((AnovaAnalysisPanel)aa.getParameterPanel()).anovaParameter, bisonParam.get("anovaParameter"));
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#validInputData(org.geworkbench.bison.datastructure.biocollections.views.DSMicroarraySetView, org.geworkbench.bison.datastructure.biocollections.DSDataSet)}.
	 */
	public final void testValidInputData() {
		DSMicroarraySetView<DSGeneMarker, DSMicroarray> a1 = null;
		DSDataSet<DSMicroarray> a2 = null;
		a1 = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
		a2 = new CSDataSet<DSMicroarray>();
		AnovaAnalysis aa = new AnovaAnalysis();
		ParamValidationResults answer = aa.validInputData(a1, a2);
		assertEquals(answer.getMessage(),
				"A minimum of 3 array groups must be activated.");
		// We test case which do have 3 array groups activated in testExecute()
		//TODO: test isLogNormalized()

		for (int i = 0; i < numElements; i++) {
			for (int j = 0; j < numElements; j++) {
				data[i][j] = (i + j + 1)*100; //to simulate non-logNormalized data
			}
		}

		view = new CSMicroarraySetView<DSGeneMarker, DSMicroarray>();
		DSMicroarraySet<DSMicroarray> microarraySet = new CSMicroarraySet<DSMicroarray>();
		microarraySet.setLabel(this.getClass().getName());

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

		DSPanel<DSGeneMarker> markerPanel = view.getMarkerPanel();
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
		
		/*
		 *un-comment following line to test isLogNormalized(),
		 *but it will then require human interaction for unit testing.
		 */
		//AlgorithmExecutionResults answer2 = aa.execute(view);
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#AnovaAnalysis()}.
	 */
	public final void testAnovaAnalysis() {
		AnovaAnalysis aa = new AnovaAnalysis();
		assertEquals(AbstractAnalysis.TTEST_TYPE, aa.getAnalysisType());
		assertEquals("Anova Analysis", aa.getLabel());
		assertNotNull(aa.getParameterPanel());
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#execute(java.lang.Object)}.
	 * 
	 * in this test, we test:
	 * 1. error report, for less then 3 array groups
	 * 2. if it will return correct markers or not. 
	 * 3. if it will return correct p-value or not.
	 */
	@SuppressWarnings("unchecked")
	public final void testExecute() {
		//Anova get group information from context manager, we'll need to prepare one for Anova.

		DSMicroarraySet<DSMicroarray> maSet = view.getMicroarraySet();
		
		DSAnnotationContextManager manager = CSAnnotationContextManager
		.getInstance();
DSAnnotationContext<DSMicroarray> context = manager
		.getCurrentContext(maSet);

		//test no groups
		AnovaAnalysis aa = new AnovaAnalysis();
		AlgorithmExecutionResults answer = aa.execute(view);
		assertEquals("execution successful for less then 3 array groups", false, answer.isExecutionSuccessful());
		assertEquals("error message for less then 3 array groups", "A minimum of 3 array groups must be activated.", answer.getMessage());

		//test three groups but not activated
		context.addLabel("group a");
		context.addLabel("group b");
		context.addLabel("group c");
		answer = aa.execute(view);
		assertEquals("execution successful for less then 3 array groups", false, answer.isExecutionSuccessful());
		assertEquals("error message for less then 3 array groups", "A minimum of 3 array groups must be activated.", answer.getMessage());
		
		//test three activated groups 
		//FIXME: there is no data in that three groups, it should return error.
		//TODO: file a bug on mantis (#1528, #1529), fix it and change this.
		context.activateLabel("group a");
		DSPanel<DSMicroarray> groupA = context.getItemsWithLabel("group a");
		groupA.setActive(true);
		context.activateLabel("group b");
		DSPanel<DSMicroarray> groupB = context.getItemsWithLabel("group b");
		groupB.setActive(true);
		context.activateLabel("group c");
		DSPanel<DSMicroarray> groupC = context.getItemsWithLabel("group c");
		groupC.setActive(true);
//		aa.setUnitTestMode();		
		answer = aa.execute(view);
		assertEquals(true, answer.isExecutionSuccessful());
		

		//test three activated groups 
		//FIXME: there is no data in that three groups, it should return error.
		context.activateLabel("group a");
		groupA = context.getItemsWithLabel("group a");
		groupA.setActive(true);
		context.labelItem(maSet.get(0), "group a");
		context.labelItem(maSet.get(1), "group a");
		context.labelItem(maSet.get(2), "group a");
		context.activateLabel("group b");
		groupB = context.getItemsWithLabel("group b");
		groupB.setActive(true);
		context.labelItem(maSet.get(3), "group b");
		context.labelItem(maSet.get(4), "group b");
		context.labelItem(maSet.get(5), "group b");
		context.activateLabel("group c");
		groupC = context.getItemsWithLabel("group c");
		groupC.setActive(true);
		context.labelItem(maSet.get(6), "group c");
		context.labelItem(maSet.get(7), "group c");
		context.labelItem(maSet.get(8), "group c");
		answer = aa.execute(view);
		
		CSAnovaResultSet<DSGeneMarker> anovaResult = ((CSAnovaResultSet<DSGeneMarker>)answer.getResults());
		assertEquals("number of significant markers", 9, anovaResult.getSignificantMarkers().size());
		assertEquals(true, answer.isExecutionSuccessful());

		//test an insignificant marker
		((DSMicroarray)(view.getDataSet().get(0))).setMarkerValue(0, new CSExpressionMarkerValue(9));
		answer = aa.execute(view);
		assertEquals("number of significant markers", 8, ((CSAnovaResultSet<DSGeneMarker>)answer.getResults()).getSignificantMarkers().size());

		//test p-values for 1. significant marker, 2. insignificant marker.
		((AnovaAnalysisPanel)aa.getParameterPanel()).pValueThreshold = 1.0f;
		answer = aa.execute(view);
		assertEquals("number of significant markers", 9, ((CSAnovaResultSet<DSGeneMarker>)answer.getResults()).getSignificantMarkers().size());
		DSGeneMarker marker1 = (DSGeneMarker)(((CSAnovaResultSet<DSGeneMarker>)answer.getResults()).getSignificantMarkers().get(0));
		assertEquals("p-values for significant marker",0.0010000000474974513, (((CSAnovaResultSet<DSGeneMarker>)answer.getResults()).getPValue(marker1)));
		DSGeneMarker marker2 = new CSGeneMarker("gene_label_0");		
		assertEquals("p-values for insignificant marker",0.23562487959861755, (((CSAnovaResultSet<DSGeneMarker>)answer.getResults()).getPValue(marker2)));
	}

	/**
	 * Test method for
	 * {@link org.geworkbench.components.anova.AnovaAnalysis#publishSubpanelChangedEvent(org.geworkbench.events.SubpanelChangedEvent)}.
	 */
	public final void testPublishSubpanelChangedEvent() {
		AnovaAnalysis aa = new AnovaAnalysis();
		DSAnnotatedPanel<DSGeneMarker, Float> panelSignificant = new CSAnnotPanel<DSGeneMarker, Float>(
				"Significant Genes");
		org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker> event = new org.geworkbench.events.SubpanelChangedEvent<DSGeneMarker>(
				DSGeneMarker.class, panelSignificant, SubpanelChangedEvent.NEW);
		assertEquals(event, aa.publishSubpanelChangedEvent(event));
		//TODO: we also need to test if execute will publish this event correctly.
	}

}
