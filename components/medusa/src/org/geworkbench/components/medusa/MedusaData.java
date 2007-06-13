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
 * @version $Id: MedusaData.java,v 1.5 2007-06-13 15:20:20 keshav Exp $
 */
public class MedusaData implements Serializable {
	private Log log = LogFactory.getLog(this.getClass());

	private DSMicroarraySet arraySet;

	// private List<RulesBean> rulesBeans = null; TODO - get from Medusa team

	private List<DSGeneMarker> regulators = null;

	private List<DSGeneMarker> targets = null;

	private MedusaCommand medusaCommand = null;

	/**
	 * 
	 * @param arraySet
	 * @param regulators
	 * @param targets
	 */
	public MedusaData(DSMicroarraySet arraySet, List<DSGeneMarker> regulators,
			List<DSGeneMarker> targets, MedusaCommand medusaCommand) {
		this.arraySet = arraySet;
		this.regulators = regulators;
		this.targets = targets;
		this.medusaCommand = medusaCommand;
	}

	/**
	 * 
	 * @return {@link DSMicroarraySet}
	 */
	public DSMicroarraySet getArraySet() {
		return arraySet;
	}

	/**
	 * 
	 * @param arraySet
	 */
	public void setArraySet(CSMicroarraySet arraySet) {
		this.arraySet = arraySet;
	}

	/**
	 * 
	 * @return List<DSGeneMarker>
	 */
	public List<DSGeneMarker> getRegulators() {
		return regulators;
	}

	/**
	 * 
	 * @return List<DSGeneMarker>
	 */
	public List<DSGeneMarker> getTargets() {
		return targets;
	}

	public MedusaCommand getMedusaCommand() {
		return medusaCommand;
	}
}
