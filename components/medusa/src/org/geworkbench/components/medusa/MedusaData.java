package org.geworkbench.components.medusa;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;

/**
 * 
 * @author keshav
 * @version $Id: MedusaData.java,v 1.1 2007-05-15 18:27:38 keshav Exp $
 */
public class MedusaData {
	private Log log = LogFactory.getLog(this.getClass());

	private CSMicroarraySet arraySet;

	// private List<RulesBean> rulesBeans = null; TODO - get from Medusa team
	private List<String> targets = null;
	private List<String> regulators = null;

	public CSMicroarraySet getArraySet() {
		return arraySet;
	}

	public void setArraySet(CSMicroarraySet arraySet) {
		this.arraySet = arraySet;
	}

}
