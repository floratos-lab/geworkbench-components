package org.geworkbench.components.medusa;

import junit.framework.TestCase;
import edu.columbia.ccls.medusa.MedusaLoader;

/**
 * 
 * @author keshav
 * @version $Id: MedusaAnalysisTest.java,v 1.1 2007-05-04 16:52:10 keshav Exp $
 */
public class MedusaAnalysisTest extends TestCase {

	/**
	 * 
	 * 
	 */
	public void testExecute() {
		MedusaAnalysis analysis = new MedusaAnalysis();
		analysis.execute(null);
	}

}
