package org.geworkbench.components.medusa;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbench.bison.datastructure.biocollections.microarrays.CSMicroarraySet;
import org.geworkbench.bison.datastructure.biocollections.microarrays.DSMicroarraySet;
import org.geworkbench.bison.datastructure.bioobjects.markers.DSGeneMarker;

/**
 * 
 * @author keshav
 * @version $Id: MedusaData.java,v 1.4 2007-05-18 21:33:48 keshav Exp $
 */
public class MedusaData implements Serializable {
	private Log log = LogFactory.getLog(this.getClass());

	private DSMicroarraySet arraySet;

	// private List<RulesBean> rulesBeans = null; TODO - get from Medusa team

	private List<DSGeneMarker> regulators = null;

	private List<DSGeneMarker> targets = null;

	/**
	 * 
	 * @param arraySet
	 * @param regulators
	 * @param targets
	 */
	public MedusaData(DSMicroarraySet arraySet, List<DSGeneMarker> regulators,
			List<DSGeneMarker> targets) {
		this.arraySet = arraySet;
		this.regulators = regulators;
		this.targets = targets;
	}

	public DSMicroarraySet getArraySet() {
		return arraySet;
	}

	public void setArraySet(CSMicroarraySet arraySet) {
		this.arraySet = arraySet;
	}

	public List<DSGeneMarker> getRegulators() {
		return regulators;
	}

	public List<DSGeneMarker> getTargets() {
		return targets;
	}

}
