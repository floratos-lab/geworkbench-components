package org.geworkbench.components.medusa;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geworkbench.components.medusa.gui.DiscreteHeatMapPanelTest;
import org.geworkbench.components.medusa.gui.DiscreteHitOrMissHeatMapPanel;
import org.geworkbench.components.medusa.gui.MedusaPluginTest;
import org.geworkbench.components.medusa.heatmap.MedusaHeatMapTest;

/**
 * All tests in the medusa component.
 * 
 * @author keshav
 * @version $Id: AllTests.java,v 1.1 2007-05-30 21:17:35 keshav Exp $
 */
public class AllTests extends TestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static Test suite() {
		TestSuite suite = new TestSuite("Medusa Tests");
		suite.addTestSuite(DiscretizationUtilTest.class);
		suite.addTestSuite(MedusaAnalysisTest.class);
		suite.addTestSuite(MedusaUtilTest.class);

		/* gui tests */
		suite.addTestSuite(DiscreteHeatMapPanelTest.class);
		suite.addTestSuite(DiscreteHitOrMissHeatMapPanel.class);
		suite.addTestSuite(MedusaPluginTest.class);

		/* heatmap tests */
		suite.addTestSuite(MedusaHeatMapTest.class);

		return suite;
	}

}
